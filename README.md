A Java desktop application for project journal management.

In summary the application allows the user to:

Create a journal for a project they are working on.
Add entries to the journal, including the amount of time spent working on the project for each entry
View the entries in a journal and summary data about the journal, such as the total amount of time
spent working on the project and total number of journal entries.

The application depends upon an external MySQL database existing on the host system.

To get the application up and running follow the steps below:

1) Install MySQL onto your system if not already installed.
2) Create the learning_journal database on your system using the script found in the file: src/main/resources/createDatabase.sql
3) Edit the USERNAME and PASSWORD constants at the top of the DataAccessObject class in the file: src/main/java/core/DataAccessObject.java
to match the user name and password for your MySQL database.
4) Run the project using the Netbeans IDE, which using Maven will download the project's dependencies. 

If the project is not built using Maven, you will need to manually download a dependency the project relies upon, 
the Mysql Connector for Java (https://dev.mysql.com/downloads/connector/j/), and add this to the classpath so that 
the program can communicate with the external MySQL database
