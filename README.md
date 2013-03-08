# O'HOI ETT SKJEMA!

Et skjema for de invidde for å kunne oppdatere bokanbefalinger i RDF-basen.

## Deployment

Pakk som jar med:

    lein ring uberjar

Start applikasjonen med:

    java -jar askjema-<version>-standalone.jar

Eller pakk som war for å deployes i tomcat e.l:

     lein ring uberwar