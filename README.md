# MonsterTradingCardsGame

***
In diesem Projekt wurde ein HTTP/REST-based Server erstellt, welcher unterschiedlichen Anfragen parsen und verarbeiten
kann.
***

### SETUP

***
Man downloadet sich die neuste pgAdmin Software (zurzeit verwendet wird pgAdmin4 v6.19). Dabei wird als User ***
postgres***
und als Password ***admin*** verwendet. Das alles kann man jedoch in src/main/resources/hikari.properties ändern. Als
nächstes wird eine Datenbank mit dem Namen ***mtcg*** erstellt. Als Schnittstelle wird der ***localhost*** mit Port ***
5432*** genommen, welche aber auch die Standardeinstellungen sind. Die Tabellen erstellen sich von alleine beim Starten
des Projektes, wenn auch alles richtig eingestellt wurde.

### ENDPOINTS

***
Die Endpunkte sind in der ***swagger.yml*** Datei beschrieben. Für eine bessere Lesbarkeit kann man
auf [Swagger](https://editor-next.swagger.io/)
in der Kopfzeile auf File > Import File klicken und dann die swagger.yml Datei auswählen.

### DESIGN

***
Das Projekt wurde in 2 Packages aufgeteilt. Im http-package wird die ankommende Anfrage geparst und im
application-Package wird die Anfrage darauffolgend verarbeitet. Dort wird auch alles rund um die Datenbank behandelt.
Hier wurde eine ähnliches Design-Pattern wie das Controller-Service-Repository-Pattern verwendet. Des Weiteren gibt es
noch das Model-Packages, welches die Modelle für die JSON-Objekte die ankommen oder versendet werden als einfachere
Speicherobjekte genützt werden. Dabei wurde das Lombok-Framework für übersichtlicheren Code und einfachere Nutzung
verwendet. Im Util-Package wurden noch zwei Hilfsklassen implementiert wie die Authorization-Klasse, da man sie in
mehreren Klassen verwendet.

### UNIQUE-FEATURE

***
Als einzigartige Erweiterung habe ich mich von anderen Kartenspielen inspirieren lassen, die oft einen eigenen Markt für
Kartenkauf und -verkaufe haben. Als Nutzer kann man eine Karte verkaufen und kaufen. Damit man nicht zu viel verlangt,
ist die Obergrenze bei 5 Münzen abgegrenzt. Um Münzen zu erlangen, habe ich eine weitere Erweiterung eingebaut, sodass
man bei einem Kampf immer Mana erhalten kann. Mana kann man danach in Münzen verwandeln, dabei beläuft sich der Kurs auf
5 Mana zu 1 Münze. Je öfter man kämpft, umso mehr Mana erhaltet man, die man in Münzen umwandeln kann.

### LESSON-LEARNED

***
Während des Projektes habe ich gelernt wie der Aufbau in einem Java-Projekt aussieht und es gleich auch in die Arbeit
bei Erste Digital verwenden können, da ich zu der gleichen Zeit einen Task für das Erstellen eines Endpunktes hatte.

### TEST

***
Es wurden in etwa 50 Unit-Tests geschrieben, welche die einzelnen Funktionalitäten der Methoden überprüft haben. Dabei
wurden die Frameworks TestNG, JUNIT und Mockito verwendet.

Das Design von Unit-Tests ist ein wichtiger Aspekt, um sicherzustellen, dass die Tests effektiv und effizient sind. Ein
gut gestalteter Unit-Test sollte unabhängig sein, wiederholbar sein und gut strukturiert sein.

Unabhängigkeit bedeutet, dass jeder Test isoliert und unabhängig von anderen Tests durchgeführt werden sollte. Wenn ein
Test auf eine andere Einheit des Codes oder einen anderen Test angewiesen ist, kann dies dazu führen, dass ein Fehler
nicht entdeckt wird oder ein Fehler in mehreren Tests gleichzeitig auftritt, was die Fehlersuche erschwert.

Wiederholbarkeit ist ein weiterer wichtiger Aspekt des Unit-Test-Designs. Tests sollten so gestaltet sein, dass sie bei
jeder Ausführung das gleiche Ergebnis liefern. Dazu sollten Tests keine Abhängigkeiten von externen Ressourcen wie
Datenbanken oder Netzwerkverbindungen haben.

Eine gute Strukturierung der Tests erleichtert das Verständnis und die Wartung der Tests im Laufe der Zeit. Tests
sollten in einer hierarchischen Struktur organisiert sein, die logisch und leicht zu verstehen ist. Einige gängige
Organisationsmethoden sind das Testen von Einzelteilen, Klassen oder Modulen.

Zusammenfassend ist das Design von Unit-Tests ein wichtiger Aspekt, um sicherzustellen, dass die Tests effektiv und
effizient sind. Ein gut gestalteter Unit-Test sollte unabhängig, wiederholbar und gut strukturiert sein. Durch die
Verwendung dieser Designprinzipien können Entwickler sicherstellen, dass ihre Tests zuverlässig sind und dazu beitragen,
die Qualität ihrer Software zu verbessern.

Es gibt verschiedene Design-Strategien, die bei der Erstellung von Unit-Tests verwendet werden können. Ich habe mich
hier mehr auf das Test-Suite-Design konzentriert. Das Test-Suite-Design bezieht sich auf die Strukturierung der Tests in
einer hierarchischen Struktur. Einzelne Tests werden in Test-Suiten organisiert, die auf einer höheren Ebene die
Komponenten, Module oder Funktionen der Software testen. Ein gutes Test-Suite-Design erleichtert das Verständnis der
Tests und deren Wartung.

Weitere Test wären auch der Integrationstest, die als Bash-Script in der Kommandozeile ausgeführt werden können. Ein
Integrationstest ist dabei ein Test, der dazu dient, das Zusammenspiel von mehreren Komponenten oder Modulen eines
Systems zu testen. Im Gegensatz zu Unit-Tests, die sich auf die Isolation und das Testen einzelner Einheiten des Codes
konzentrieren, zielen Integrationstests darauf ab, zu prüfen, ob die verschiedenen Komponenten des Systems ordnungsgemäß
miteinander interagieren und das System als Ganzes wie erwartet funktioniert.

### TIME-SPENT

***
Ich habe sehr spät mit dem Projekt angefangen, da beim ersten Antritt durch die anderen Fächer mir nicht soviel Zeit
geblieben ist und beim zweiten Antritt die Betriebspraxisphase und eine Krankheit mir in den Weg gekommen sind. In das
Thema hereinzukommen war etwas schwer, aber als ich es einmal drinnen war, lief alles wie am Schnürchen. Für das
Hauptprogramm habe ich 5 ganze Tage gebraucht, für die Erweiterung des Markt-Endpunktes ging noch ein Tag drauf und für
das Schreiben der Tests habe ich mir 2 Tage genommen. Insgesamt habe ich somit 8 volle Tage gebraucht, die ich auch voll
ausgenützt habe, das heißt pro Tag 12 Stunden. Daraus folgt, es wurden 96 Stunden für das Projekt verwendet.

[GitHub-Link](https://github.com/siyar00/MonsterTradingCardsGame.git)

#### Ersteller

> Siyar Yamin
