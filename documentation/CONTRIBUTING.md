# GéoTortue - Guide pour contribuer


**Bienvenue !**

:tada::+1: Pour commencer, merci d'envisager de contribuer à **GéoTortue**&nbsp;! :tada::+1:

**Sommaire**

- [Recommandations](#recommandations)
  - [Licence](#licence)
- [Développement](#développement)
  - [Tests unitaires et couverture de code](#tests-unitaires-et-couverture-de-code)
  - [Suivi de couverture avec SonarQube](#suivi-de-couverture-avec-sonarqube)
- [Distribution](#distribution)

## Recommandations

Nous aimerions souligner les points suivants&nbsp;:

  1. Soyez respectueux
     * Nous apprécions les contributions à GéoPartie et nous vous demandons de vous respecter les uns les autres.
  2. Soyez responsable
     * Vous êtes responsable des soumissions de vos Pull Request.
  3. Donnez crédit
     * Si une soumission ou une contribution est basée sur le travail d'un tiers (par ex. un article de recherche, un projet open-dource, du code publique), veuillez citer ou joindre toute information au sujet des sources d'origine. Toute personne doit être crédité du travail qu'elle a fait.

### Licence

En contribuant à **GéoTortue**, vous acceptez que vos contributions seront sous licence [GPL 3](../LICENSE).


## Développement

### Tests unitaires et couverture de code

Les tests unitaires sont réalisés avec [JUnit](https://junit.org/junit4). 

Les rapports de couverture de code sont réalisés avec [Jacoco](https://www.jacoco.org/jacoco/).

Pour lancer les tests&nbsp;:

```bash
mvn clean verify
> [...]

```

Pour consulter les taux de couverture, depuis un navigateur ouvrir le fichier `./target/site/jacoco/index.html`.

### Suivi de couverture avec SonarQube

  **TODO** décrire l'installation et le démarrage du serveur SonarQube

Pré-requis : 
- docker et docker-compose

> Utilisation de `docker-compose` pour configurer 2 images docker : `sonarqube:community` & `postgres:12`.

La configuration du serveur SonarQube se fait avec le fichier ./sonar-project.properties.
Elle est prise en compte via le pom grâce au paquetage `properties-maven-plugin`.

```bash
mvn clean verify sonar:sonar -Dsonar.host.url=http://localhost:9000   -Dsonar.token=<clé sonar du projet>
```

Aller sur http://localhost:9000/dashboard?id=geotortue

## Distribution

Pour le moment, seuls un exécutable java (archive jar) et un exécutable pour Windows sont mis à disposition ici :

S'assurer que la jdk installée est de version 8 ou supérieure.
Puis construire l'archive exécutable jar et la lancer :

``` bash
java --version

mvn -U clean package
> Scanning for projects...
> [...]

ls -Al target
> total 11860
> drwxrwsr-x  2 pierre developers    4096 Dec 20 14:45 antrun
> drwxrwsr-x 13 pierre developers    4096 Dec 20 14:45 classes
> drwxrwsr-x  3 pierre developers    4096 Dec 20 14:45 generated-sources
> -rw-rw-r--  1 pierre developers 4012525 Dec 20 14:45 geotortue-4.23.12.20.jar
> -rwxrwxr-x  1 pierre developers 4100077 Dec 20 14:45 geotortue.exe
> -rw-rw-r--  1 pierre developers 4012525 Dec 20 14:45 geotortue.jar
> drwxrwsr-x  3 pierre developers    4096 Dec 20 14:45 maven-status

java -jar target/geotortue.jar
> [...]

```

L'application s'ouvre, la tortue est prête à répondre à tous vos souhaits.

> L'exécutable Windows n'a pas été testé. Tout retour sera le bienvenu.

Pour d'autres distributions voir [GéoTortue - Téléchargement](http://geotortue.free.fr/index.php?page=telechargement).

