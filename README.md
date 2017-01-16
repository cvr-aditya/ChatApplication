# ChatApplication
Small Android chat application with java backend using dropwizard

Running the App

* Import the chatapp.sql into mysql server and add edit the DB details in ChatAppBackend/resources/config.yml
* Build the ChatAppBackend maven project by running "mvn clean install"
* Start the server by running "java -jar target/com.univ.chat-1.0-SNAPSHOT-jar-with-dependencies.jar"
* Edit the IP address of server in URL.java (src/main/java/com/univ/chat/util/URL.java)
* Build the android application and deploy it in emulator/mobile.
