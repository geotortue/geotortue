## Distribution

Pour le moment, seuls un exécutable java (archive jar) et un exécutable pour Windows sont mis à disposition ici :

S'assurer que la jdk installée est de version 9 ou supérieure.
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

java --add-exports java.desktop/sun.swing=ALL-UNNAMED -jar target/geotortue.jar
> [...]

```

L'application s'ouvre, la tortue est prête à répondre à tous vos souhaits.

> L'exécutable Windows n'a pas été testé. Tout retour sera le bienvenu.

Pour d'autres distributions voir [GéoTortue - Téléchargement](http://geotortue.free.fr/index.php?page=telechargement).

