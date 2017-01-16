# ChatApplication
Small Android chat application with java backend using dropwizard

Running the App

1) Import the chatapp.sql into mysql server and add edit the DB details in ChatAppBackend/resources/config.yml
2) Build the ChatAppBackend maven project by running "mvn clean install"
3) Start the server by running "java -jar target/com.univ.chat-1.0-SNAPSHOT-jar-with-dependencies.jar"
4) Edit the IP address of server in URL.java (src/main/java/com/univ/chat/util/URL.java)
5) Build the android application and deploy it in emulator/mobile.
