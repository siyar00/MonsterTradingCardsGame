# MonsterTradingCardsGame

***
In diesem Projekt wurde ein HTTP/REST-based Server erstellt, welcher unterschiedlichen Anfragen parsen und verarbeiten
kann.
***

### SETUP

***
Man downloadet sich die neuste pgAdmin Software (zurzeit verwendet wird pgAdmin4 v6.19). Dabei wird als User ***postgres***
und als Password ***admin*** verwendet. Das alles kann man jedoch in src/main/resources/hikari.properties ändern. Als
Nächstes wird eine Datenbank mit dem Namen ***mtcg*** erstellt. Als Schnittstelle wird der ***localhost*** mit Port ***5432***
genommen, welche aber auch die Standardeinstellungen sind. Die Tabellen erstellen sich von alleine beim Starten
des Projektes, wenn auch alles richtig eingestellt wurde.

### ENDPOINTS

***
Die Endpunkte sind in der ***swagger.yml*** Datei beschrieben. Für eine bessere Lesbarkeit kann man
auf [Swagger](https://editor-next.swagger.io/)
in der Kopfzeile auf `File > Import File` klicken und dann die swagger.yml Datei auswählen und einfügen.

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
Während des Projektes konnte ich umfassende Erfahrungen in Bezug auf den Aufbau eines Java-Projekts sammeln und diese
direkt in meine Arbeit bei Erste Digital integrieren. Zu dieser Zeit hatte ich die Aufgabe, einen Endpunkt zu erstellen,
bei der mir das erworbene Wissen sehr geholfen hat. Dabei konnte ich auch meine Kenntnisse in Bezug auf die Verwendung
von Multithreading in Java erweitern und verstehen, wie Interfaces in Java eingesetzt werden können.

Die Arbeit bei Erste Digital hat meine bereits vorhandenen Kenntnisse in Java weiter vertieft und mir die Möglichkeit
gegeben, in einem professionellen Umfeld mit dieser Programmiersprache zu arbeiten. Durch die regelmäßige Anwendung von
Java konnte ich meine Fähigkeiten in diesem Bereich kontinuierlich verbessern und neue Herausforderungen meistern.

Insgesamt hat mich das Projekt und die Arbeit bei Erste Digital in meinem Verständnis und meiner Anwendung von Java
weitergebracht und meine Fähigkeiten als Entwickler gestärkt. Es war eine wertvolle Erfahrung, die mich auf meinem
beruflichen Weg vorangebracht hat.

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
Ich hatte zunächst Schwierigkeiten, mit dem Projekt zu beginnen, da ich aufgrund anderer Fächer und Verpflichtungen
zunächst nur begrenzt Zeit hatte. Beim zweiten Antritt kam es dann durch eine Kombination aus Betriebspraxisphase und
Krankheit zu weiteren Verzögerungen. Es war also kein optimaler Start in das Projekt, und ich musste hart arbeiten, um
aufzuholen.

Es hat einige Zeit gedauert, bis ich mich in das Thema eingearbeitet hatte und die erforderlichen Kenntnisse erworben
hatte. Doch nachdem ich erstmal drin war, konnte ich schnell Fortschritte machen. Für das Hauptprogramm habe ich fünf
volle Tage benötigt, um es vollständig zu implementieren. Für die Erweiterung des Markt-Endpunktes musste ich noch einen
weiteren Tag aufwenden. Schließlich habe ich noch zwei Tage für das Schreiben der Tests gebraucht. Insgesamt habe ich
also acht volle Tage damit verbracht, das Projekt zu bearbeiten. Diese Tage habe ich voll ausgenutzt, indem ich pro Tag
zwölf Stunden gearbeitet habe.

Letztendlich habe ich 96 Stunden in das Projekt investiert. Obwohl es nicht einfach war, das Projekt zu starten und es
gab einige Hindernisse zu bewältigen, war es mir letztendlich möglich, das Projekt erfolgreich abzuschließen. Durch die
intensive Arbeit konnte ich mein Verständnis und meine Fähigkeiten in Bezug auf das Thema erweitern und mich weiter als
Entwickler verbessern.

[GitHub-Link](https://github.com/siyar00/MonsterTradingCardsGame.git)

#### Ersteller

> Siyar Yamin
