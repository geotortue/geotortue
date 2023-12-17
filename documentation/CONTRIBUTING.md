## Distribution

Pour le moment, seuls un exécutable java (archive jar) et un exécutable pour Windows sont mis à disposition ici :

S'assurer que la jdk installée est de version 9 ou supérieure.
Puis construire l'archive exécutable jar et la lancer :

``` bash
java --version

mvn -U clean package

ls -Al target
> total 8188
> drwxrwsr-x 13 pierre developers    4096 Dec 16 15:50 classes
> drwxrwsr-x  3 pierre developers    4096 Dec 16 15:50 generated-sources
> -rw-rw-r--  1 pierre developers 4139520 Dec 16 15:51 geotortue-4.23.12.15.jar
> -rwxrwxr-x  1 pierre developers 4227072 Dec 16 15:51 geotortue.exe
> drwxrwsr-x  3 pierre developers    4096 Dec 16 15:50 maven-status

java --add-exports java.desktop/sun.swing=ALL-UNNAMED -jar target/geotortue-4.23.12.15.jar

```

> L'exécutable Windows n'a pas été testé.

Pour d'autres distributions voir [GéoTortue - Téléchargment](http://geotortue.free.fr/index.php?page=telechargement).

