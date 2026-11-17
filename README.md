COMP 3005 Assignment 3 Part 1
Safwan Khan, 101270732

Video: https://youtu.be/l6eG-VeDZ84

This is a Java app that connects to a PostgreSQL database using JDBC and does CRUD operations on a students table.


Files:

database folder - SQL scripts to create the database, create the table and insert the starting data
src/StudentApp.java - the app
lib folder - the PostgreSQL JDBC driver (postgresql-42.7.2.jar)


Setup:

You need PostgreSQL (I used pgAdmin) and Java JDK 11 or newer.

1. In pgAdmin, open a Query Tool on the postgres database and run database/01_create_database.sql. This creates students_db.
2. Open a Query Tool on students_db and run database/02_create_table.sql, then database/03_insert_initial_data.sql.
3. In src/StudentApp.java, change USER and PASSWORD at the top to your own PostgreSQL login. The URL uses localhost and port 5432, so change that too if yours is different.


Compile and run:

Run these from the main project folder (the one with src and lib in it).

Windows:
javac -cp "lib/*" -d out src/StudentApp.java
java -cp "out;lib/*" StudentApp

Mac/Linux:
javac -cp "lib/*" -d out src/StudentApp.java
java -cp "out:lib/*" StudentApp


Functions:

When the app starts it shows a menu, and you pick which function to run.

getAllStudents() - gets every student from the table and prints them.
addStudent(first_name, last_name, email, enrollment_date) - adds a new student. The date has to be in YYYY-MM-DD format. student_id is created automatically.
updateStudentEmail(student_id, new_email) - changes the email of the student with that id.
deleteStudent(student_id) - deletes the student with that id.

Add, update and delete use PreparedStatement. If something goes wrong, like a duplicate email or an id that doesn't exist, the app prints an error message instead of crashing.