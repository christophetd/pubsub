== Tampon de commandes ==

Le premier problème était de permettre aux différents threads CommandHandler (dont le nombre est fixé grâce à la constante TCPAcceptor.NB_COMMAND_HANDLERS) de récupérer des commandes à exécuter en même temps que des threads TCPReader en ajoutent. Cela est un problème de producteurs / consommateurs classique, que nous avons choisi de résoudre en implémentant une classe BlockingQueue (MyBlockingQueue pour ne pas avoir le même nom que la classe de l'API Java) qui permet d'avoir une solution assez générale en utilisant les moniteurs, les verrous et les conditions Java. 
-> Un consommateur ne peut pas récupérer un élément de la BlockingQueue en même temps qu'un autre, ni en même temps qu'un producteur est en train d'ajouter un élément, ni si la BlockingQueue est vide. Dans ces trois cas, le thread consommateur (en l'occurrence CommandHandler) se bloquera en attendant que toutes les conditions soient réunies.
-> Un producteur ne peut pas ajouter un élément dans la BlockingQueue en même temps qu'un autre, ni en même temps qu'un consommateur est en train d'accéder à un élément, ni si la BlockingQueue est pleine (taille maximale fixée par une constante). Ici aussi, un thread producteur se bloquera si les trois conditions ci-dessus ne sont pas vérifiées, en attendant qu'elles le soient.

Le tampon de commandes est représenté par une instance de MyBlockingQueue, créé dans TCPAcceptor et par la suite transmis aux threads TCPReader. 
Nous avons choisi de représenter une commande par la classe Command, pour une meilleure encapsulation. Même chose pour un client, représenté par une classe Client.
Etant donné que seul un thread à la fois doit pouvoir envoyer des données sur le flux de sortie d'un client, la classe Client propose (entre autres) une méthode sendMessage synchronisée.

== Souscriptions des clients à des sujets ==
Un autre problème auquel nous avons été confronté a été celui du stockage des souscriptions d'un client, qui peut avoir souscrit à plusieurs sujets. Pour cela, nous avons créé une classe SubscriptionsStore dont une instance est passée à chaque CommandHandler, et qui leur permet d'accéder aux souscriptions d'une manière sécurisée. Le fonctionnement de la classe SubscriptionsStore est décrit un peu plus bas.
À noter que nous conservons un double mapping pour les relations "clients <-> sujets" (sujet -> clients dans SubscriptionsStore et client -> sujets dans Client) pour récupérer les clients abonnés à un sujet ou les sujets auquels est abonné un client en un temps constant (qui aurait été linéaire si le mapping n'était fait que dans un sens).


== Fonctionnement de SubscriptionsStore ==
Chaque CommandHandler doit savoir quel client est inscrit à quel sujet et doit pouvoir modifier les relations. Evidemment, cette information doit être synchronisée entre tous les CommandHandlers (ils doivent tous avoir la même modélisation de la situation à un même moment). La solution la plus simple consiste à créer une structure de données partagée par tous les CommandHandlers (SubscriptionsStore) qui gèrera elle-même les problèmes de concurrence.
Cette structure est très spécifique à l'application (contrairement à MyBlockingQueue), elle expose les méthodes subscribe, unsubscribe et iterateSubject.
Les deux premières font ce que leur nom indique de façon "thread-safe" en bloquant si nécessaire. La troisième méthode permet d'itérer sur chaque client d'un sujet en gérant la concurrence, c'est-à-dire que strictement tous les clients d'un sujet seront visités (la liste ne peut être modifiée par un autre thread pendant l'itération).
En interne, subscriptionStore stocke une map de subject(String) vers Set<Client> avec un petit wrapper autour de Set<Client> pour les raisons expliquées plus bas.
L'implémentation contient deux niveaux de lock de type lecteur/rédacteur. Le premier niveau est global à tout le store : un nombre illimité de lecteurs est autorisé à accéder à des éléments de la Map mais lorsqu'un rédacteur veut modifier la map (i.e. ajouter/supprimer un sujet), il doit être seul et aucun lecteur ne doit être en train d'accéder à un Set. La même logique est appliquée à chaque set séparément. Il est donc possible que deux CommandHandlers modifient en même temps deux subjects (set de clients) différents, ou que plusieurs CommandHandlers lisent le même subject en même temps, mais si un CommandHandler ajoute ou supprime un sujet, il est le seul CommandHandler actif sur l'ensemble du store. Aussi, si un CommandHandler inscrit ou désinscrit un client à un sujet, aucun CommandHandler ne peut itérer sur ce sujet mais d'autres CommandHandlers peuvent utiliser d'autres sujets (en lecture ou écriture).
Les locks ne donnent pas de priorité particulière ce qui a pour effet de favoriser les lecteurs (un rédacteur se bloquera jusqu'à ce que la voie soit libre) et c'est très bien pour notre application : les tâches répétitives sont favorisées (cf. tableau ci dessous).

__________________________________________________________________________
Tâche                    Lock principal      Lock local        Fréquence 
--------------------------------------------------------------------------
publish                   |   read        |       read     |        +++
subscribe/unsubscribe     |   read        |       write    |        ++
* et add/remove subject   |   write       |       write    |        +

Fig. 1 : Utilisation des locks pour les actions d'un point de vue haut niveau. On voit clairement que les actions fréquentes (publish) sont favorisées par rapport aux actions peu fréquentes (ajout/suppression d'un sujet).


*Note - notre application collecte ses déchets : si un client se désinscrit d'un sujet dont il était le seul participant, le sujet est physiquement détruit.
