# Roadmap

Deux axes qui avancent ensemble : **toi** (compétences visées) et **Upro** (l'appli, qui sert d'exercice).
Règle du jeu : chaque compétence travaillée se matérialise par une évolution du code, une ADR ou du contenu.

## 1. Trajectoire sur 10 ans

| Horizon | Rôle visé | Ce qui change | Preuves à accumuler |
|---|---|---|---|
| 0 – 2 ans | **Senior / Tech lead d'une équipe** | Tu portes la qualité et la conception d'une équipe | Revues de code, ADR, mentoring, ownership d'un service en prod |
| 2 – 5 ans | **Lead dev / Staff engineer** | Ton impact dépasse ton équipe | Design docs transverses, standards adoptés par plusieurs équipes, talks internes |
| 5 – 10 ans | **Principal / Architecte (solution ou entreprise)** | Tu cadres la stratégie technique | Cartographie SI, arbitrages build/buy, trajectoires de migration, gouvernance |

Compétences par pilier :

- **Java & JVM** : Java 21 → 25 (records, sealed, pattern matching, virtual threads, structured concurrency), JVM internals, GC, JFR, profiling, performance.
- **Architecture** : hexagonal, DDD (stratégique + tactique), monolithe modulaire vs microservices, event-driven (Kafka, outbox, saga), CQRS, systèmes distribués, cloud & Kubernetes, sécurité (OAuth2/OIDC), coûts.
- **Best practices** : tests (pyramide, Testcontainers, mutation testing), clean code, refactoring, CI/CD, observabilité (OpenTelemetry), métriques DORA.
- **Anglais** : lire → écrire (ADR, design docs) → parler (réunions, entretiens, talks). Objectif : être à l'aise en entretien technique en anglais.
- **Leadership** : communication, estimation, mentoring, influence sans autorité, gestion de la dette, conduite du changement.

Lectures de référence : *Clean Architecture* (Martin), *Domain-Driven Design* (Evans) / *DDD Distilled* (Vernon), *Designing Data-Intensive Applications* (Kleppmann), *Fundamentals of Software Architecture* (Richards & Ford), *Building Microservices* (Newman), *Staff Engineer* (Larson), *The Staff Engineer's Path* (Reilly), *Accelerate* (Forsgren).

## 2. Évolutions d'Upro = exercices

Chaque étape est un exercice volontairement laissé à faire. Pour chacune : écrire l'ADR **avant** de coder (en anglais à partir de la phase « Industrialisation »).

### Fondations (niveau Senior)
- [x] Squelette hexagonal, règles ArchUnit, CI, contenu en YAML, UI minimale
- [x] Persistance PostgreSQL + Flyway (JdbcClient), déploiement Railway
- [x] Quiz quotidien adaptatif, niveau Elo par module, traitement quotidien et historique
- [ ] **Testcontainers** : remplacer H2 par un vrai PostgreSQL dans les tests d'intégration. *(fidélité des tests)*
- [ ] **Variante JPA** : réécrire un adapter avec Spring Data JPA et comparer (lisibilité, requêtes générées, N+1). *(ports & adapters : changer d'adapter sans toucher au domaine)*
- [ ] **Calibrer la difficulté des questions** à partir des réponses (taux de réussite réel vs niveau déclaré, puis IRT). *(données, statistiques)*
- [ ] **Nouveaux types d'exercices** : question ouverte, kata de code, « trouve le bug ». Modéliser avec une hiérarchie `sealed`. *(Java moderne, conception objet)*
- [ ] **Contenu** : viser 30 questions par module ; ajouter un module *Spring & écosystème* et un module *Cloud & DevOps*.

### Industrialisation (niveau Lead)
- [ ] **Sécurité** : multi-utilisateur avec Spring Security + OAuth2/OIDC (Keycloak). *(sécurité applicative)*
- [ ] **Observabilité** : Micrometer, OpenTelemetry, dashboard Grafana ; métrique « quiz du jour terminé ». *(prod-readiness)*
- [ ] **Multi-instance** : verrou distribué (ShedLock) sur le traitement quotidien, puis passage à 2 réplicas. *(systèmes distribués)*
- [ ] **Notifications** : rappel quotidien (email ou push) si le quiz n'est pas fait. *(intégration, événements)*
- [ ] **Conteneurisation** : Dockerfile, docker-compose (app + Postgres + Keycloak), déploiement sur un cloud. *(DevOps)*
- [ ] **Qualité** : Sonar, mutation testing (PIT), Spotless/Checkstyle en CI. *(standards d'équipe)*

### Architecture distribuée (niveau Staff / Architecte)
- [ ] **Événements** : publier `QuestionAnswered` (outbox + Kafka), un contexte *statistics* qui les consomme. *(event-driven, cohérence à terme)*
- [ ] **Découpage** : extraire `catalog` en service séparé, comparer avec Spring Modulith et documenter le trade-off. *(monolithe modulaire vs microservices)*
- [ ] **Coach IA** : générer des questions / corriger des réponses ouvertes via un LLM derrière un port. *(intégration d'un système externe non déterministe)*
- [ ] **Design doc** : rédiger en anglais le design doc de la v2 comme si tu le présentais à un comité d'architecture.

## 3. Rituel proposé

- **Quotidien (10-15 min)** : faire le quiz du jour dans Upro.
- **Hebdomadaire (2 h)** : une case de la roadmap, ou 5 nouvelles questions écrites à partir d'une lecture.
- **Trimestriel** : bilan à partir de l'historique (évolution du niveau par module) et mise à jour de cette roadmap.
