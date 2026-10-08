## Lancer Symfony
symfony serve --no-tls --listen-ip=0.0.0.0  


## Serveur dev  
### Commande concernant le docker  
- Pour démarrer la base    
docker compose up -d database	  
- Pour l'arrêter sans rien perdre (tes données restent)    
docker compose stop	  
- Pour l'arrêter en effaçant les données    
docker compose down -v  
- Pour voir si elle tourne    
docker compose ps	 

### Commande pour initialiser la base de dev  
1. Créer les tables  
php bin/console doctrine:schema:create  
2. Exécuter les migrations  
php bin/console doctrine:migrations:sync-metadata-storage  
php bin/console doctrine:migrations:version --add --all --no-interaction  

### Remplir les données de base
- Ajouter les notes  
docker compose exec -T database psql -U app -d app < ../database/scripts/addNotes.sql  
- Ajouter les accords
php bin/console app:seed-chords  
- Ajouter les gammes  
php bin/console app:seed-scales  

## Migration

1. Génère le fichier de migration (compare ton entité avec la base)  
symfony console doctrine:migrations:diff  

2. Applique la migration en base  
symfony console doctrine:migrations:migrate  
