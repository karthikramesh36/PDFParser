# Introduction 
A PDF Parser project that allows you to extract information from PDF intended for other purposes.

# Getting Started
1.	Include JAR file into your project to make use of functions provided by this project
2.	TableExtractor allows user to extract table information from PDF document into a table object . its intuitive but for best 	table recognition eliminate lines from document that are outside the table
3.	TableExtractor allows user to extract Area information from PDF document into a string. The area here is a Rectangle2D 	object and its size denoted in point coordinate system. (TIP: use GIMP application for windows to simple drag and draw 	custom rectangles over your PDF document to find the coordinates) . 
4.	LineExtractor allows user to extract information by line number from PDF document into a List<String> object. 
5.	All classes are method chained and have attributes that enable to modify document to our needs before extraction. For ex: 	we can choose to extract or except certain pages and lines using addPage(),exceptPage() functions.
6.	the _Docs folder consists of sample files for unit testing and class diagrams for high level overview

# Build and Test
-   unit tests can give a clear idea of all possible usages of each class in project.
-   most of the edge cases are covered in the unit tests
-   Assertion is not performed , real purpose of unit tests to learn possible usages of each Class .of course there is room for improvement 

# Contribute
Feel free to contribute and make changes to project according to your needs.
A lot more advancements can be made in terms catering the output returned from extract() method. 
