[TODO: xtof]
Pour le command buffer:

Problème de producteur/consommateur classique avec plusieurs producteurs, plusieurs consommateurs, priorité aux consommateurs, résolu avec les moniteurs et les conditions java.

[end TODO]

[TODO: check syntax & typo :]

SubscriptionsStore:

Chaque worker doit savoir quel client est inscrit à quel sujet et doit pouvoir modifier les relations. Evidemment, cette information doit être synchronisée entre tous les workers (ils doivent tous avoir la même modélisation de la situation à un même moment). La solution la plus simple consiste à créer une structure de données partagée par tous les workers ( SubscriptionsStore ) qui gèrera elle-même les problèmes de concurrence.
Cette structure est très spécifique à l'application (contrairement à BlockingQueue), elle expose les méthodes subscribe, unsubscribe et iterateSubject.
Les deux premières font ce que leur nom indique de façon "thread-safe" en bloquant si nécessaire. La troisième méthode permet d'itérer sur chaque client d'un sujet en gérant la concurrence, c'est à dire que strictement tous les clients d'un sujet seront visités (la liste ne peut être modifiée par un autre thread pendant l'itération).

En interne, subscriptionStore stocke une map de subject(String) vers Set<Client> avec un petit wrapper autour de Set<Client> pour les raisons expliquées plus bas.
L'implémentation contient deux niveaux de lock de type lecteur/rédacteur. Le premier niveau est global à tout le store: un nombre illimité de lecteur est autorisé à accéder à des éléments de la Map mais lorsqu'un rédacteur veut modifier la map (ie. ajouter/supprimer un sujet), il doit être seul et aucun lecteur ne doit être en train d'accéder à un Set. La même logique est appliquée à chaque set séparément. Il est donc possible que deux workers modifient en même temps deux subjects (set de client) différents, ou que plusieurs workers lisent le même subject en même temps mais si un worker ajoute ou supprime un sujet, il est le seul worker actif sur l'ensemble du store. Aussi, si un worker inscrit ou désinscrit un client à un sujet, aucun worker ne peut itérer sur ce sujet mais d'autres workers peuvent utiliser d'autres sujets (en lecture ou écriture).
Les locks ne donnent pas de priorité particulière ce qui a pour effet de favoriser les lecteurs (un rédacteur se bloquera jusqu'à ce que la voie soit libre) et c'est très bien pour notre application: les tâches répétitives sont favorisées (cf. tableau ci dessous).


tâche                    lock principal      lock local        fréquence
--------------------------------------------------------------------------
publish                      read               read             +++
subscribe/unsubscribe        read               write            ++
* et add/remove subject      write              write            +

fig.1: Utilisation des locks pour les actions d'un point de vue haut niveau. On voit clairement que les actions fréquentes (publish) sont favorisées par rapport aux actions peu fréquentes (ajout/suppression d'un sujet).


*Note: notre application collecte ses déchets: si un client se désinscrit d'un sujet dont il était le seul participant, le sujet est phyisquement détruit.
