# 0001 — Architecture hexagonale découpée par contexte

- **Statut** : acceptée
- **Date** : 2026-10-13

## Contexte

Upro doit évoluer longtemps et servir de terrain d'apprentissage : on prévoit de changer la persistance (mémoire → PostgreSQL), d'ajouter des canaux (REST, UI, peut-être CLI ou messaging) et d'extraire éventuellement des services. Le cœur métier (répétition espacée, catalogue) doit rester stable pendant ces changements.

## Décision

- Découper le code par **contexte métier** (`catalog`, `practice`), chacun en `domain` / `application` / `infrastructure`.
- Le `domain` et l'`application` sont en **Java pur** : aucune annotation Spring. Les services sont instanciés dans un *composition root* (`io.upro.config.ApplicationConfig`).
- Les dépendances externes passent par des **ports** (interfaces dans `application.port`) implémentés par des **adapters** (`infrastructure.*`).
- Ces règles sont **exécutables** via ArchUnit (`ArchitectureTest`).

## Alternatives considérées

- *Architecture en couches classique (controller / service / repository)* : plus simple, mais le métier finit couplé à JPA et à Spring.
- *Spring Modulith* : intéressant pour vérifier les frontières de modules ; à réévaluer lors de l'étape « Découpage » de la roadmap.

## Conséquences

- ➕ Changer d'adapter (ex. persistance) ne touche ni le domaine ni les cas d'usage ; tests unitaires rapides sans Spring.
- ➕ Les violations d'architecture cassent le build au lieu d'être découvertes en revue.
- ➖ Un peu plus de code de câblage et de mapping (DTO web distincts des objets du domaine).
