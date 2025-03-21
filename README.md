# Calculateur de Crédit

Ce projet est une application Java Swing pour calculer les crédits et générer des tableaux d'amortissement. Il permet de calculer le coût mensuel, le montant cumulé des intérêts et de générer des graphiques d'amortissement.

## Structure du projet

```
Amortissement.png
Amortissement.txt
launch.json
pom.xml
src/
    main/
        java/
            com/
                example/
                    CreditCalculator.java
target/
    credit-calculator-1.0-SNAPSHOT.jar
    classes/
        com/
            example/
                Amortissement.class
                CreditCalculator.class
                CreditCalculator$1.class
                CreditCalculator$2.class
    generated-sources/
        annotations/
    maven-archiver/
        pom.properties
    maven-status/
        maven-compiler-plugin/
            compile/
                default-compile/
                    createdFiles.lst
                    inputFiles.lst
```

## Prérequis

- Java 8 ou supérieur
- Maven

## Installation

1. Clonez le dépôt :

    ```sh
    git clone https://github.com/cutkiller09/CreditCalculator.git 
    git clone git@github.com:cutkiller09/CreditCalculator.git
    ```

2. Compilez le projet avec Maven :

    ```sh
    mvn clean install
    ```

## Utilisation

1. Exécutez l'application :

    ```sh
    java -jar target/credit-calculator-1.0-SNAPSHOT.jar
    ```

2. Entrez les détails du crédit (durée, montant, taux) et cliquez sur "Calculer" pour voir les résultats.

## Fonctionnalités

- Calcul du coût mensuel et du montant cumulé des intérêts.
- Génération d'un tableau d'amortissement.
- Sauvegarde du tableau d'amortissement dans un fichier `Amortissement.txt`.
- Génération de graphiques d'amortissement et sauvegarde en tant qu'image `Amortissement.png`.

## Dépendances

- [JFreeChart](https://github.com/jfree/jfreechart) pour la génération des graphiques.

## Auteur

- [Votre Nom]

## Licence

Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.