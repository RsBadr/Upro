# 0003 — Jugement de niveau (Elo) et quiz quotidien adaptatif

- **Statut** : acceptée
- **Date** : 2026-10-13

## Contexte

L'usage visé est un petit quiz chaque jour, avec un jugement de niveau par module (Confirmé → Senior → Lead → Architecte), et des questions qui s'ajustent à ce niveau. Il faut aussi pouvoir suivre la progression dans le temps.

## Décision

**Niveau estimé façon Elo, par module.**
- Chaque niveau de question a une difficulté : Confirmé 1000, Senior 1200, Lead 1400, Architecte 1600.
- Chaque réponse est un « match » contre la question : `attendu = 1 / (1 + 10^((difficulté - rating) / 400))`, puis `rating += K × (résultat - attendu)`.
- K = 40 pendant le calibrage (15 premières réponses du module), puis 20 pour stabiliser.
- Point de départ : 1200 (Senior), affiné par les réponses. Le niveau affiché est celui dont la difficulté est la plus proche du rating.

**Quiz du jour composé à partir de la banque** (`DailyQuizComposer`, Java pur) :
1. ~40 % de révisions : questions déjà vues et dues (Leitner), les plus fragiles d'abord ;
2. des nouvelles questions au plus près de `rating + 75`, c'est-à-dire un cran au-dessus du niveau estimé, réparties entre les modules ;
3. si la banque est épuisée, complément avec les questions les moins bien réussies.

Le quiz est déterministe pour une date donnée et persisté : il ne change pas dans la journée.

**Traitement quotidien** (`DailyService.runDailyRoutine`), à 4 h (heure de Paris) et au démarrage :
1. clôture des journées passées : un bilan par module et par jour (réponses, réussites, rating en fin de journée), recalculé depuis le journal des réponses ;
2. préparation du quiz du jour.

Il est idempotent et rattrape jusqu'à 60 jours manqués (redéploiement, mise en veille).

## Alternatives considérées

- *Taux de réussite par niveau* (ex. « niveau validé si 70 % de réussite ») : simple, mais demande beaucoup de réponses par niveau et produit des paliers brutaux.
- *Théorie de réponse à l'item (IRT)* : plus juste (calibre aussi la difficulté réelle de chaque question), mais nécessite des données. C'est une évolution naturelle une fois l'historique accumulé.
- *Génération de questions par IA* : écartée pour l'instant au profit d'une banque relue et maîtrisée (voir ADR 0002).

## Conséquences

- ➕ Le niveau bouge dès la première réponse et reste explicable : réussir une question difficile rapporte plus que réussir une question facile.
- ➕ Le bilan quotidien est recalculable depuis `answer_log`, qui sert de source de vérité.
- ➖ La difficulté des questions est déclarée par l'auteur, pas mesurée. À recalibrer plus tard avec les données (IRT).
- ➖ Le planificateur suppose une seule instance de l'application. Avec plusieurs instances, il faudra un verrou distribué (ShedLock par exemple).
