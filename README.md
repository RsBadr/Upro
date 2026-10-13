# Upro

> Plateforme d'entraînement personnelle pour passer de **développeur Java confirmé** à **Lead / Staff engineer / Architecte** sur les 10 prochaines années.

Upro a un double rôle :

1. **Un outil** : un **quiz quotidien** de 10 questions (Java, Architecture, Best practices, Anglais technique, Leadership), adapté à ton **niveau estimé par module**, avec révisions par **répétition espacée** et historique de progression.
2. **Un terrain de jeu** : le code de l'application applique lui-même ce qu'elle enseigne (architecture hexagonale, règles d'architecture testées, ADR, CI). Chaque évolution du projet est un exercice — voir [`docs/ROADMAP.md`](docs/ROADMAP.md).

## Démarrer

Prérequis : JDK 21 et Docker (pour PostgreSQL en local).

```bash
./mvnw verify            # build + tests + règles ArchUnit (H2, pas besoin de Docker)
docker compose up -d     # PostgreSQL local sur le port 5434
./mvnw spring-boot:run   # puis http://localhost:8080
```

## Le quiz quotidien

1. **Chaque jour, un quiz de 10 questions** (`upro.daily.size`), le même toute la journée :
   - ~40 % de **révisions** (questions ratées ou à revoir selon la répétition espacée) ;
   - des **questions nouvelles un cran au-dessus de ton niveau estimé**, réparties entre les modules.
2. **Jugement de niveau par module**, façon Elo ([ADR 0003](docs/adr/0003-niveau-elo-et-quiz-quotidien.md)). Réussir une question difficile fait beaucoup monter ton niveau, rater une question facile le fait baisser. Les 15 premières réponses d'un module servent au calibrage.

   | Niveau | Difficulté |
   |---|---|
   | Confirmé | 1000 |
   | Senior (point de départ) | 1200 |
   | Lead | 1400 |
   | Architecte | 1600 |

3. **Traitement quotidien** à 4 h (heure de Paris, `upro.daily.cron`), et au démarrage pour rattraper les jours manqués :
   - bilan de la veille par module (réponses, réussite, niveau en fin de journée) → historique ;
   - préparation du quiz du jour.

L'entraînement libre par module reste disponible, et ses réponses comptent aussi pour le niveau.

## Déploiement (Railway)

En ligne : **https://backend-production-c18d7.up.railway.app**

Même modèle que `comptes-app` :

- projet Railway `upro`, service `backend`, construit depuis le `Dockerfile` (build Maven multi-stage, JRE 21, utilisateur non-root) ;
- redéploiement automatique à chaque push sur la branche suivie par Railway ;
- health check sur `/actuator/health`, port lu depuis la variable `PORT` (fixée à `8080`).

Base de données : service PostgreSQL du projet Railway, branché via `SPRING_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD`. Schéma géré par Flyway ([ADR 0004](docs/adr/0004-persistance-postgresql-jdbc.md)).

## Modules

| Module | Fichier de contenu | Cible |
|---|---|---|
| Java & JVM | `content/01-java.yml` | Java moderne, concurrence, GC, perf |
| Architecture & Conception | `content/02-architecture.yml` | Hexagonal, DDD, microservices, distribué |
| Best practices & Qualité | `content/03-best-practices.yml` | SOLID, tests, sécurité, observabilité |
| Technical English | `content/04-english.yml` | Vocabulaire, réunions, écrit (ADR), entretiens |
| Leadership & Soft skills | `content/05-leadership.yml` | Tech lead, staff, mentoring, influence |

Chaque module contient 12 questions, 3 par **niveau** (`CONFIRMED`, `SENIOR`, `LEAD`, `ARCHITECT`). Plus la banque est grande, plus le quiz s'adapte finement : vise 30+ questions par module.

### Ajouter du contenu

Ajouter une entrée dans un fichier YAML de `src/main/resources/content/` (ou créer un fichier pour un nouveau module), sans écrire de Java :

```yaml
- id: java-streams-01          # unique dans tout le catalogue
  topic: Streams
  level: SENIOR
  prompt: La question ?
  choices: [Choix A, Choix B, Choix C]
  answer: 1                    # index (à partir de 0) de la bonne réponse ; l'ordre affiché est mélangé
  explanation: Pourquoi c'est la bonne réponse, avec des références.
```

`YamlCatalogRepositoryTest` valide le contenu à chaque build : ids uniques, index valides, explications présentes, tous les niveaux couverts dans chaque module.

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
│   ├── domain                  # ReviewCard, LeitnerSchedule, SkillRating (Elo), AnswerRecord
│   ├── application             # PracticeService + ports ReviewCardRepository, SkillRatingRepository, AnswerLog
│   └── infrastructure
│       ├── jdbc                # adapters PostgreSQL (JdbcClient)
│       └── web                 # adapter : REST /api/practice
├── daily                       # contexte « quiz quotidien & bilans »
│   ├── domain                  # DailyQuizComposer, DailyQuiz, DailySnapshot
│   ├── application             # DailyService (quiz du jour, traitement quotidien, historique)
│   └── infrastructure
│       ├── jdbc                # adapters PostgreSQL
│       ├── scheduling          # déclenchement planifié (@Scheduled) et au démarrage
│       └── web                 # adapter : REST /api/daily
├── shared                      # port Transactions et son adapter Spring
└── config                      # composition root (câblage Spring), gestion d'erreurs
```

Règles vérifiées par `ArchitectureTest` (le build casse si elles sont violées) :

- `domain` et `application` ne dépendent ni de Spring, ni de Jakarta, ni de l'infrastructure ;
- `catalog` ne dépend pas de `practice`, et ni l'un ni l'autre ne dépend de `daily` ;
- les adapters ne se parlent pas entre eux.

## API

| Méthode | URL | Description |
|---|---|---|
| GET | `/api/modules` | Liste des modules |
| GET | `/api/modules/{id}` | Détail d'un module |
| GET | `/api/practice/{moduleId}/next?count=5` | Questions à réviser aujourd'hui (sans la réponse) |
| POST | `/api/practice/answers` | `{"questionId": "...", "choice": 1}` → correction + prochaine révision |
| GET | `/api/practice/progress` | Progression Leitner par module |
| GET | `/api/practice/levels` | Niveau estimé par module (rating, calibrage, avancement vers le niveau suivant) |
| GET | `/api/daily` | Quiz du jour (sans les réponses) avec l'état d'avancement |
| GET | `/api/daily/history?days=30` | Bilans quotidiens : réponses, réussite et niveau par module |

Les erreurs sont renvoyées au format RFC 9457 (Problem Details).

## Stack

Java 21 · Spring Boot 4 · PostgreSQL · Flyway · JdbcClient · Jackson 3 (YAML) · JUnit 5 · AssertJ · ArchUnit · H2 (tests) · GitHub Actions · Railway
