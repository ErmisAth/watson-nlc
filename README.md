# watson-nlc
java project for training querying deleting and listing available classifiers under a Watson Natural Language Classifier service

This is a small Java program that is easy to interface with BluePrism.
The program accepts 5 different combinations of arguments depending on whether the user wants
to train a new classifier, delete an existing classifier, query an existing classifier, 
request details about an existing classifier or list all existing classifiers:
- Train: The user must provide 6 arguments. 
	- 1st argument is the word "train".
	- 2nd argument is the url for the Watson service.
	- 3rd argument is the username for the Watson service.
	- 4th argument is the password for the Watson service.
	- 5th argument is the path to a .csv file containing training data.
	- 6th argument is the path to a .json file with the classifier's metadata.
- Query: The user must provide 7 arguments.
	- 1st argument is the word "query".
	- 2nd argument is the url for the Watson service.
	- 3rd argument is the username for the Watson service.
	- 4th argument is the password for the Watson service.
	- 5th argument is the id of the classifier.
	- 6th argument is the path to a .csv file with the queries.
	- 7th argument is a confidence threshold between 0 and 1 above which the classifier's prediction can be considered reliable
- Delete: The user must provide 5 arguments.
	- 1st argument is the word "delete".
	- 2nd argument is the url for the Watson service.
	- 3rd argument is the username for the Watson service.
	- 4th argument is the password for the Watson service.
	- 5th argument is the id of the classifier.
- List: The user must provide 4 arguments.
	- 1st argument is the word "list"
	- 2nd argument is the url for the Watson service.
	- 3rd argument is the username for the Watson service.
	- 4th argument is the password for the Watson service.
- Info: The user must provide 5 arguments.
	- 1st argument is the word "info".
	- 2nd argument is the url for the Watson service.
	- 3rd argument is the username for the Watson service.
	- 4th argument is the password for the Watson service.
	- 5th argument is the id of the classifier.

The results are printed out and can be easily read into BluePrism data items from the console.

Examples:
- Train: java -jar watson-nlc-1.0.jar "train" "url" "username" "password" "/path/to/training_data.csv" "path/to/metadata.json" 
- Query: java -jar watson-nlc-1.0.jar "query" "url" "username" "password" "classifier-Id" "path/to/query_data.csv" "0.95"
- Delete: java -jar watson-nlc-1.0.jar "delete" "url" "username" "password" "classifier-Id"
- List: java -jar watson-nlc-1.0.jar "list" "url" "username" "password"
- Details: java -jar watson-nlc-1.0.jar "info" "url" "username" "password" "classifier-Id"

How to get it working:
- clone the repository
- download and install Java if you don't have it: https://www.oracle.com/technetwork/java/javase/downloads/index.html
- install maven on your machine: https://spring.io/guides/gs/maven/
- go into the Java project folder inside the repository and run: mvn package
- the .jar file should be inside the WatsonNLC/target/ folder
- create a Watson NLC service on your IBM account and use the credentials provided
- run the .jar file from the command line: java -jar <file-name>.jar "use" "username" "password" ...
