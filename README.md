# Learning Guitar
Application Android d'apprentissage de la guitare : accords, gammes et tablatures qui défilent en rythme avec le morceau.

- **`api/`** : API Symfony + API Platform (PostgreSQL), déployée sur Render
- **`mobile/`** : application Android (Java)
- **`database/`** : scripts SQL

---

## Lancer l'application

**Prérequis** : PHP 8.2 (avec l'extension `pdo_pgsql`), Composer, Docker avec le plugin compose, Symfony CLI, Android Studio.

**1. Cloner le dépôt**
```bash
git clone git@github.com:AnaeeH/learningGuitar.git
cd learningGuitar/api
```

**2. Installer les dépendances**
```bash
composer install
```

**3. Créer le fichier d'environnement local** (`api/.env.local`, ignoré par git)
```dotenv
APP_SECRET=une-valeur-aleatoire-de-32-caracteres
DATABASE_URL="postgresql://app:!ChangeMe!@127.0.0.1:5432/app?serverVersion=16&charset=utf8"
```
> Le fichier `.env` est commité mais ne contient que des valeurs d'exemple (`CHANGE_ME`). Les vrais secrets ne doivent jamais y figurer.

**4. Démarrer la base de données** (Docker)
```bash
docker compose up -d database
```

**5. Initialiser la base de dev** (à faire une seule fois)
```bash
php bin/console doctrine:schema:create
php bin/console doctrine:migrations:sync-metadata-storage
php bin/console doctrine:migrations:version --add --all --no-interaction
```
> L'historique des migrations ne peut pas reconstruire la base depuis zéro (les tables `t_chord_chr` et `t_note_nte` ont été créées à la main à l'origine). On crée donc le schéma à partir des entités, puis on marque toutes les migrations comme déjà exécutées.

**6. Remplir les données de base**
```bash
docker compose exec -T database psql -U app -d app < ../database/scripts/addNotes.sql   # les notes
php bin/console app:seed-chords                                                          # les accords
php bin/console app:seed-scales                                                          # les gammes
```

**7. Lancer l'API**
```bash
symfony serve --no-tls --listen-ip=0.0.0.0
```

**8. Accéder aux services**

| Service              | URL                                           |
|----------------------|-----------------------------------------------|
| API (Swagger UI)     | http://127.0.0.1:8000/api                     |
| Base PostgreSQL      | `127.0.0.1:5432` (base `app`, utilisateur `app`) |
| API de production    | https://guitarapi-yl09.onrender.com/api       |

La base de dev démarre sans aucune musique : voir [Importer une musique](#importer-une-musique).

---

## Docker

```bash
docker compose up -d database    # Démarrer la base
docker compose stop              # L'arrêter sans rien perdre (les données restent)
docker compose down -v           # L'arrêter ET effacer les données
docker compose ps                # Voir si elle tourne
```

**Consulter la base**
```bash
docker compose exec database psql -U app -d app
```
Commandes utiles dans `psql` : `\dt` (liste des tables), `SELECT * FROM t_chord_chr;`, `\q` (quitter).

---

## Importer une musique

Route `POST /api/post/music` (format `multipart/form-data`), utilisable depuis Swagger UI :

| Champ       | Description                                                      |
|-------------|------------------------------------------------------------------|
| `xmlFile`   | Fichier MusicXML (exporté depuis MuseScore)                      |
| `audioFile` | Fichier audio (optionnel)                                        |
| `video`     | JSON : `{"videoId": "...", "startSec": 12.5}` (vidéo YouTube)    |
| `riff`      | `true` ou `false`                                                |

---

## Variables d'environnement

| Où                      | Rôle                                                              | Commité ? |
|-------------------------|-------------------------------------------------------------------|-----------|
| `api/.env`              | Valeurs d'exemple (placeholders)                                  | oui       |
| `api/.env.local`        | Configuration de dev (base Docker locale)                         | non       |
| `api/.env.prod.local`   | URL de la base de prod, utilisée seulement pour les migrations    | non       |
| Render > Environment    | `DATABASE_URL`, `APP_SECRET`, `APP_ENV=prod` pour l'API déployée  | non       |

---

## Migrations

### En dev

```bash
# 1. Générer le fichier de migration (compare les entités avec la base)
php bin/console doctrine:migrations:diff

# 2. Relire le fichier créé dans api/migrations/, puis l'appliquer et tester
php bin/console doctrine:migrations:migrate
```

> Ne pas utiliser `doctrine:schema:update --force` : il modifierait la base locale sans créer de migration, et la prod ne recevrait jamais le changement.

### En production

Les migrations sont versionnées avec le code. On les applique à la base de prod (Supabase) depuis son ordinateur, grâce au fichier `api/.env.prod.local`.

**Une seule fois** : créer `api/.env.prod.local` (ignoré par git et par Docker) :
```dotenv
DATABASE_URL="postgresql://postgres.<reference-projet>:<mot-de-passe>@aws-1-eu-west-3.pooler.supabase.com:5432/postgres?serverVersion=15"
```
(copier l'URL « Session pooler » depuis le bouton **Connect** de Supabase.)

**À chaque changement de structure :**
```bash
# 1. (optionnel) Sauvegarder la base depuis le tableau de bord Supabase

# 2. Voir ce qui reste à appliquer (lecture seule)
php bin/console doctrine:migrations:status --env=prod

# 3. Appliquer les migrations à la prod
php bin/console doctrine:migrations:migrate --env=prod
```
Le `--env=prod` est volontaire : sans lui, les commandes agissent toujours sur la base locale.

**Ordre à respecter** : pour un simple ajout (nouvelle colonne, nouvelle table), appliquer la migration **avant** de déployer le nouveau code. Pour une suppression ou un renommage, procéder en deux temps (d'abord le code qui n'utilise plus l'ancien élément, puis la migration).

> Ne jamais lancer `doctrine:schema:create` ni `doctrine:schema:update` avec `--env=prod`.

---

## Déploiement
 
L'API est construite avec le `Dockerfile` de `api/` et hébergée sur Render. **À chaque push sur `main`, Render reconstruit et redéploie l'API.** 
 
Construire l'image en local (depuis la racine du projet) :
```bash
docker build -t guitar-api api
docker run --rm guitar-api ls -a /var/www/html   # vérifier qu'aucun secret n'est dans l'image
```
 
> Le serveur gratuit de Render s'endort après un moment d'inactivité : l'application mobile appelle `/api/ping` toutes les 13 minutes pour le garder éveillé.
 
---

## Application Android
 
Ouvrir le dossier `mobile/` dans Android Studio.
 
### Choisir l'environnement (variantes de build)
 
L'URL de l'API dépend de la **variante de build** choisie (`BuildConfig.API_BASE_URL`, lu dans `GuitarAPI.java`).
 
| Variante  | Nom de l'appli | API appelée                       | Base de données              |
|-----------|----------------|-----------------------------------|------------------------------|
| `debug`   | Guitar Dev     | ordinateur local (`symfony serve`) | PostgreSQL local (Docker)    |
| `prod`    | Guitar         | Render                            | Supabase (production)        |
| `release` | Guitar         | Render                            | Supabase (non utilisée : pas de publication prévue) |
 
Les deux applis (`Guitar Dev` et `Guitar`) ont des identifiants différents (`.dev` pour le debug) : elles peuvent être installées en même temps sur le téléphone.
 
### Configuration (une seule fois)
 
Ajouter l'adresse de l'API locale dans `mobile/local.properties` (fichier ignoré par git, propre à chaque machine) :
```properties
api.dev.url=http://192.168.1.1:8000/api
```
- Remplacer l'adresse par l'IP de l'ordinateur (`hostname -I` dans un terminal). Elle peut changer si la box en attribue une nouvelle.
- Avec l'émulateur Android, supprimer cette ligne : l'adresse `http://10.0.2.2:8000/api` est utilisée par défaut.
### Lancer l'appli
 
1. Dans **Build Variants** (View → Tool Windows → Build Variants), choisir `debug` ou `prod`.
2. Cliquer sur **Run**. Android Studio construit, installe et lance l'appli.
**Avec `debug`**, démarrer d'abord la base et l'API sur l'ordinateur :
```bash
docker compose up -d database
symfony serve --no-tls --listen-ip=0.0.0.0
```
Le `--listen-ip=0.0.0.0` est indispensable pour qu'un téléphone puisse joindre l'API.
 
**Avec `prod`**, rien à lancer en local. Le premier appel peut être lent si Render s'est endormi.
 
> **Si le téléphone n'arrive pas à joindre l'API locale** : vérifier que le téléphone et l'ordinateur sont sur le même Wi-Fi, que l'adresse IP de `local.properties` est la bonne, et que le pare-feu laisse passer le port 8000 (`sudo ufw allow 8000` si `ufw` est actif). Le HTTP non chiffré n'est autorisé que dans la variante `debug` (`mobile/app/src/debug/AndroidManifest.xml`).
