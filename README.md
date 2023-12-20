# GéoTortue

## 💡 Présentation 

[GéoTortue](http://geotortue.free.fr/) est un logiciel inspiré du langage LOGO pour découvrir les mathématiques et l’algorithmique.

Le logiciel GéoTortue se distingue sur deux points&nbsp;:

- il a été conçu pour tous, que ce soit en classe (de l’école élémentaire au lycée), chez-soi, entre amis, ...&nbsp;
- il étend le champ d’application à la géométrie dans l’espace et à des géométries non-euclidiennes.

## 🏁 Démarrage rapide

### Pré-requis

[GIT](https://git-scm.com/) et [Maven](https://maven.apache.org/) 3.6 & s. installés.

### Sous linux

Vérifier que Maven est disponible en version 3.6 ou plus.

S'assurer que Java est disponible avec une version 9 & s.

Cloner l'application puis la lancer avec Maven :


``` bash
mvn -v
> Apache Maven 3.6.3
> Maven home: /usr/share/maven
> Java version: 17.0.8, vendor: GraalVM Community, runtime: /usr/lib/jvm/graalvm-community-openjdk-17.0.8+7.1
> Default locale: en_US, platform encoding: UTF-8
> OS name: "linux", version: "5.15.0-84-generic", arch: "amd64", family: "unix"

git clone clone https://github.com/turiot/geotortue

cd geotortue

MAVEN_OPTS="--add-exports java.desktop/sun.swing=ALL-UNNAMED" mvn compile exec:java
> Scanning for projects...
> [...]
```

L'application s'ouvre, la tortue est prête à répondre à tous vos souhaits.

## 🛠️ Développement

Voir le [Guide pour contribuer](documentation/CONTRIBUTING.md).

## 🛡️ Licence

[GPL 3](./LICENSE)

## 📜 Crédit

- [GéoTortue](http://geotortue.free.fr/) a été créé et est maintenu par [Salvatore Tummarello](mailto:geotortue@free.fr).  
Le logiciel doit beaucoup aux idées, suggestions et remarques enthousiastes de Stéphan Petitjean, Erwan Adam, Jean-François Jamart et Frédéric Clerc.

- [GéoTortue](http://geotortue.free.fr/) a été développé au sein de l'[IREM Paris-Nord](https://www-irem.univ-paris13.fr) :

  - [GéoTortue 3D : utilisation et exemples d’activités](https://www-irem.univ-paris13.fr/site_spip/spip.php?article352)
  - [LOGO, ordinateurs et apprentissages](https://www-irem.univ-paris13.fr/site_spip/spip.php?article32)
  - ...

- R. Hartig a dessiné la [mascotte](src/main/resources/cfg/tortue-v4.png).