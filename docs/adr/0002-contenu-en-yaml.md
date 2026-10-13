# 0002 — Contenu pédagogique en fichiers YAML versionnés

- **Statut** : acceptée
- **Date** : 2026-10-13

## Contexte

Le contenu (questions, explications) va croître en continu. Il doit être facile à écrire, relu en PR, et ne pas nécessiter de code Java ni de base de données pour être ajouté.

## Décision

Un fichier YAML par module dans `src/main/resources/content/`, chargé au démarrage par l'adapter `YamlCatalogRepository`. La validité du contenu est garantie par les invariants du domaine (`Question`) et par `YamlCatalogRepositoryTest`.

## Alternatives considérées

- *Base de données + écran d'administration* : prématuré pour un seul auteur.
- *Markdown avec front-matter* : plus agréable pour les longues explications, mais parsing plus fragile. À reconsidérer si les explications s'allongent.

## Conséquences

- ➕ Contenu versionné, diffable, relu comme du code ; erreurs détectées en CI.
- ➖ Modifier le contenu nécessite un redéploiement (acceptable pour un usage personnel).
- Le port `CatalogRepository` permet de basculer vers une autre source plus tard sans toucher au reste.
