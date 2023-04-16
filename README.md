# MonsterTradingCardsGame
***
In diesem Projekt wurde ein HTTP/REST-based Server erstellt, welcher unterschiedlichen Anfragen parsen und verarbeiten kann.
***
### SETUP
***
Man downloadet sich die neuste pgAdmin Software (zurzeit verwendet wird pgAdmin4 v6.19). Dabei wird als User ***postgres***
und als Password ***admin*** verwendet. Das alles kann man jedoch in src/main/resources/hikari.properties ändern. Als nächstes
wird eine Datenbank mit dem Namen ***mtcg*** erstellt. Als Schnittstelle wird der ***localhost*** mit Port ***5432*** genommen,
welche aber auch die Standardeinstellungen sind. Die Tabellen erstellen sich von alleine beim Starten des Projektes, wenn auch alles
richtig eingestellt wurde.
### ENDPOINTS
***
Die Endpunkte sind in der ***swagger.yml*** Datei beschrieben. Für eine bessere Lesbarkeit kann man auf [Swagger](https://editor-next.swagger.io/)
in der Kopfzeile auf File > Import File klicken und dann die swagger.yml Datei auswählen.
### DESIGN
***
Das Projekt wurde in 2 Packages aufgeteilt. Im http-package wird die ankommende Anfrage geparst und im application-Package
wird die Anfrage darauffolgend verarbeitet. Dort wird auch alles rund um die Datenbank behandelt. Hier wurde eine ähnliches
Design-Pattern wie das Controller-Service-Repository-Pattern verwendet. Des Weiteren gibt es noch das Model-Packages, welches 
die Modelle für die JSON-Objekte die ankommen oder versendet werden als einfachere Speicherobjekte genützt werden. Dabei
wurde das Lombok-Framework für übersichtlicheren Code und einfachere Nutzung verwendet. Im Util-Package wurden noch zwei
Hilfsklassen implementiert wie die Authorization-Klasse, da man sie in mehreren Klassen verwendet.
### UNIQUE-FEATURE
***
Als einzigartige Erweiterung habe ich mich von anderen Kartenspielen inspirieren lassen, die oft einen eigenen Markt für
Kartenkauf und -verkaufe haben. Als Nutzer kann man eine Karte verkaufen und kaufen. Damit man nicht zu viel verlangt, ist die Obergrenze
bei 5 Münzen abgegrenzt. Um Münzen zu erlangen, habe ich eine weitere Erweiterung eingebaut, sodass man bei einem Kampf immer Mana
erhalten kann. Mana kann man danach in Münzen verwandeln, dabei beläuft sich der Kurs auf 5 Mana zu 1 Münze. Je öfter man
kämpft, umso mehr Mana erhaltet man, die man in Münzen umwandeln kann.
### LESSON-LEARNED
***
Während des Projektes habe ich gelernt wie der Aufbau in einem Java-Projekt aussieht und es gleich auch in die Arbeit bei
Erste Digital verwenden können, da ich zu der gleichen Zeit einen Task für das Erstellen eines Endpunktes hatte.
### TEST
***

### TIME-SPENT
***
Ich habe sehr spät mit dem Projekt angefangen, da beim ersten Antritt durch die anderen Fächer mir nicht soviel Zeit geblieben
ist und beim zweiten Antritt die Betriebspraxisphase und eine Krankheit mir in den Weg gekommen sind. In das Thema hereinzukommen
war etwas schwer, aber als ich es einmal drinnen war, lief alles wie am Schnürchen. Für das Hauptprogramm habe ich 5 ganze Tage 
gebraucht, für die Erweiterung des Markt-Endpunktes ging noch ein Tag drauf und für das Schreiben der Tests habe ich mir 2 Tage genommen.
Insgesamt habe ich somit 8 volle Tage gebraucht, die ich auch voll ausgenützt habe, das heißt pro Tag 12 Stunden.
Daraus folgt, es wurden 96 Stunden für das Projekt verwendet.

[GitHub-Link](https://github.com/siyar00/MonsterTradingCardsGame.git)
#### Ersteller
>Siyar Yamin
