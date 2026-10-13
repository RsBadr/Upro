# Upro

> Plateforme d'entraînement personnelle pour passer de **développeur Java confirmé** à **Lead / Staff engineer / Architecte** sur les 10 prochaines années.

Upro a un double rôle :

1. **Un outil** : des modules de questions (Java, Architecture, Best practices, Anglais technique, Leadership) révisés par **répétition espacée** (système de Leitner).
2. **Un terrain de jeu** : le code de l'application applique lui-même ce qu'elle enseigne (architecture hexagonale, règles d'architecture testées, ADR, CI). Chaque évolution du projet est un exercice — voir [`docs/ROADMAP.md`](docs/ROADMAP.md).

## Démarrer

Prérequis : JDK 21.

```bash
./mvnw verify            # build + tests + règles ArchUnit
./mvnw spring-boot:run   # puis http://localhost:8080
```

## Déploiement (Railway)

En ligne : **https://backend-production-c18d7.up.railway.app**

Même modèle que `comptes-app` :

- projet Railway `upro`, service `backend`, construit depuis le `Dockerfile` (build Maven multi-stage, JRE 21, utilisateur non-root) ;
- redéploiement automatique à chaque push sur la branche suivie par Railway ;
- health check sur `/actuator/health`, port lu depuis la variable `PORT` (fixée à `8080`).

⚠️ Tant que la persistance n'est pas faite (voir roadmap), la progression est remise à zéro à chaque redéploiement.

## Modules

| Module | Fichier de contenu | Cible |
|---|---|---|
| Java & JVM | `content/01-java.yml` | Java moderne, concurrence, GC, perf |
| Architecture & Conception | `content/02-architecture.yml` | Hexagonal, DDD, microservices, distribué |
| Best practices & Qualité | `content/03-best-practices.yml` | SOLID, tests, sécurité, observabilité |
| Technical English | `content/04-english.yml` | Vocabulaire, réunions, écrit (ADR), entretiens |
| Leadership & Soft skills | `content/05-leadership.yml` | Tech lead, staff, mentoring, influence |

Chaque question porte un **niveau** (`CONFIRMED`, `SENIOR`, `LEAD`, `ARCHITECT`) pour mesurer la progression.

### Ajouter du contenu

Ajouter une entrée dans un fichier YAML de `src/main/resources/content/` (ou créer un fichier pour un nouveau module), sans écrire de Java :

```yaml
- id: java-streams-01          # unique dans tout le catalogue
  topic: Streams
  level: SENIOR
  prompt: La question ?
  choices: [Choix A, Choix B, Choix C]
  answer: 1                    # index (à partir de 0) de la bonne réponse
  explanation: Pourquoi c'est la bonne réponse, avec des références.
```

`YamlCatalogRepositoryTest` valide le contenu (ids uniques, index valides, explications présentes) à chaque build.

## Répétition espacée

| Boîte | Prochaine révision |
|---|---|
| 1 | le jour même (nouvelle question ou mauvaise réponse) |
| 2 | J+1 |
| 3 | J+3 |
| 4 | J+7 |
| 5 | J+15 |
| 6 | J+30 (question maîtrisée) |

Bonne réponse : boîte suivante. Mauvaise réponse : retour en boîte 1.

## Architecture

Architecture hexagonale, découpée par contexte métier (cf. [ADR 0001](docs/adr/0001-architecture-hexagonale.md)) :

```
io.upro
├── catalog                     # contexte « contenu pédagogique »
│   ├── domain                  # LearningModule, Question, Level (Java pur)
│   ├── application             # CatalogService (cas d'usage) + port CatalogRepository
│   └── infrastructure
│       ├── yaml                # adapter : chargement des fichiers YAML
│       └── web                 # adapter : REST /api/modules
├── practice                    # contexte « entraînement & progression »
│   ├── domain                  # ReviewCard, LeitnerSchedule (Java pur)
│   ├── application             # PracticeService + port ReviewCardRepository
│   └── infrastructure
│       ├── memory              # adapter : stockage en mémoire (provisoire)
│       └── web                 # adapter : REST /api/practice
└── config                      # composition root (câblage Spring), gestion d'erreurs
```

Règles vérifiées par `ArchitectureTest` (le build casse si elles sont violées) :

- `domain` et `application` ne dépendent ni de Spring, ni de Jakarta, ni de l'infrastructure ;
- `catalog` ne dépend pas de `practice` ;
- les adapters ne se parlent pas entre eux.

## API

| Méthode | URL | Description |
|---|---|---|
| GET | `/api/modules` | Liste des modules |
| GET | `/api/modules/{id}` | Détail d'un module |
| GET | `/api/practice/{moduleId}/next?count=5` | Questions à réviser aujourd'hui (sans la réponse) |
| POST | `/api/practice/answers` | `{"questionId": "...", "choice": 1}` → correction + prochaine révision |
| GET | `/api/practice/progress` | Progression par module |

Les erreurs sont renvoyées au format RFC 9457 (Problem Details).

## Stack

Java 21 · Spring Boot 4 · Jackson 3 (YAML) · JUnit 5 · AssertJ · ArchUnit · GitHub Actions
