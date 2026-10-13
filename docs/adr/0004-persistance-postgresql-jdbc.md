# 0004 — Persistance PostgreSQL avec JdbcClient et Flyway

- **Statut** : acceptée (remplace le stockage en mémoire)
- **Date** : 2026-10-13

## Contexte

Le niveau et l'historique quotidien n'ont de valeur que s'ils survivent aux redéploiements. Le projet est déployé sur Railway, comme `comptes-app`, qui fournit PostgreSQL.

## Décision

- PostgreSQL en production (service Railway), schéma versionné avec **Flyway** (`db/migration`).
- Adapters écrits avec **`JdbcClient`** (Spring 6.1+) et du SQL explicite, un par port : `JdbcReviewCardRepository`, `JdbcSkillRatingRepository`, `JdbcAnswerLog`, `JdbcDailyQuizRepository`, `JdbcDailySnapshotRepository`.
- Les transactions passent par le port `Transactions` (couche application, sans Spring), implémenté par `SpringTransactions` (`TransactionTemplate`).
- Tests : H2 en mode PostgreSQL pour le test d'intégration, fakes en mémoire pour les tests des cas d'usage.

## Alternatives considérées

- *Spring Data JPA* : moins de SQL à écrire, mais il faut des entités qui doublonnent les records du domaine, et le modèle (quelques tables, requêtes simples) n'en tire pas profit.
- *Testcontainers* pour les tests : plus fidèle que H2, mais demande Docker en CI. C'est une bonne évolution (voir roadmap).

## Conséquences

- ➕ SQL visible et maîtrisé ; le domaine reste fait de records immuables.
- ➕ Changer de base ne touche que les adapters `infrastructure.jdbc`.
- ➖ Les upserts sont écrits à la main (update puis insert).
- ➖ H2 ne détecte pas toutes les incompatibilités PostgreSQL. Le schéma a été vérifié une fois sur un vrai PostgreSQL 16, et Testcontainers rendrait cette vérification systématique.
