package watsonNLC;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.ibm.watson.developer_cloud.natural_language_classifier.v1.NaturalLanguageClassifier;
import com.ibm.watson.developer_cloud.natural_language_classifier.v1.model.ClassificationCollection;
import com.ibm.watson.developer_cloud.natural_language_classifier.v1.model.ClassifiedClass;
import com.ibm.watson.developer_cloud.natural_language_classifier.v1.model.Classifier;
import com.ibm.watson.developer_cloud.natural_language_classifier.v1.model.ClassifierList;
import com.ibm.watson.developer_cloud.natural_language_classifier.v1.model.ClassifyCollectionOptions;
import com.ibm.watson.developer_cloud.natural_language_classifier.v1.model.ClassifyInput;
import com.ibm.watson.developer_cloud.natural_language_classifier.v1.model.CollectionItem;
import com.ibm.watson.developer_cloud.natural_language_classifier.v1.model.CreateClassifierOptions;
import com.ibm.watson.developer_cloud.natural_language_classifier.v1.model.DeleteClassifierOptions;
import com.ibm.watson.developer_cloud.natural_language_classifier.v1.model.GetClassifierOptions;
import com.ibm.watson.developer_cloud.service.exception.ServiceResponseException;
import com.ibm.watson.developer_cloud.service.security.IamOptions;
import com.opencsv.CSVReader;


public class WatsonNLC {

	public static void main(String[] args) {
		
		// argument check
		if (args.length == 0) {
			
			System.err.println("Error: No input arguments provided!");
			System.exit(1);
			
		}
		if (!args[0].equals("train") && !args[0].equals("query") && !args[0].equals("delete")
				&& !args[0].equals("list") && !args[0].equals("info")) {
			
			System.err.println("Error: First argument must be either `train`" + 
				", `query`, `delete`, `list` or `info`!".replace('`', '"'));
			System.exit(1);
			
		}
		String use = args[0];
		
		// Train classifier path
		if (use.equals("train")) {
			
			// Treat arguments			
			if (args.length == 6) {
				
				String url = args[1];
				String username = args[2];
				String password = args[3];
				String trainingDataPath = args[4];
				String metadataPath = args[5];
				
				// Initialize classifier with credentials
				NaturalLanguageClassifier nlc = new NaturalLanguageClassifier(username, password);
				// Set end-point
				nlc.setEndPoint(url);
				// Protect data
				Map<String, String> headers = new HashMap<String, String>();
				headers.put("X-Watson-Learning-Opt-Out", "true");
				nlc.setDefaultHeaders(headers);
				// Create classifier options
				CreateClassifierOptions createOptions;
				try {
					
					createOptions = new CreateClassifierOptions.Builder()
						.metadata(new File(metadataPath))
						.trainingData(new File(trainingDataPath))
						.build();
					// Execute call
					try {
						
						nlc.createClassifier(createOptions).execute();
						
					} catch (ServiceResponseException e) {
						
					    System.err.println("Error: Service returned status code " + 
					    	e.getStatusCode() + ": " + e.getMessage());
					    System.exit(1);
					    
					}
				} catch (FileNotFoundException e1) {
					
					System.err.println("Error: Training data and/or metadata files missing!");
					System.exit(1);
					
				}
				
			} else if (args.length == 5) {
				
				String url = args[1];
				String apikey = args[2];
				String trainingDataPath = args[3];
				String metadataPath = args[4];
				
				// Initialize classifier with credentials
				IamOptions options = new IamOptions.Builder().apiKey(apikey).build();
				NaturalLanguageClassifier nlc = new NaturalLanguageClassifier(options);
				// Set end-point
				nlc.setEndPoint(url);
				// Protect data
				Map<String, String> headers = new HashMap<String, String>();
				headers.put("X-Watson-Learning-Opt-Out", "true");
				nlc.setDefaultHeaders(headers);
				// Create classifier options
				CreateClassifierOptions createOptions;
				try {
					
					createOptions = new CreateClassifierOptions.Builder()
						.metadata(new File(metadataPath))
						.trainingData(new File(trainingDataPath))
						.build();
					// Execute call
					try {
						
						nlc.createClassifier(createOptions).execute();
					} catch (ServiceResponseException e) {
						
					    System.err.println("Error: Service returned status code " + 
					    	e.getStatusCode() + ": " + e.getMessage());
					    System.exit(1);
					    
					}
				} catch (FileNotFoundException e1) {
					
					System.err.println("Error: Training data and/or metadata files missing!");
					System.exit(1);
					
				}
			} else {
				
				System.err.println("Error: Expected 5 or 6 arguments for training a classifier." + 
						" 5 arguments for api-key authentication in the following order: use(train) url api-key trainingDataPath metadataPath." + 
						" 6 arguments for username-password authentication in the following order: use(train) url username password trainingDataPath metadataPath");
				System.exit(1);
				
			}
			
		}
		
		// Query classifier path
		if (use.equals("query")) {
			
			// Treat arguments
			if (args.length == 7) {
				
				String url = args[1];
				String username = args[2];
				String password = args[3];
				String classifierId = args[4];
				String queriesFilePath = args[5];
				float confidenceThreshold = (float) 0.5;
				try {
					
					confidenceThreshold = Float.parseFloat(args[6]);
					if (confidenceThreshold < 0 || confidenceThreshold > 1) throw new Exception();
					
				} catch (Exception e) {
					
					System.err.println("Error: confidenceThreshold argument must be a numeric value " + 
									   "between 0 and 1");
					System.exit(1);
					
				}
				
				// Initialize classifier with credentials
				NaturalLanguageClassifier nlc = new NaturalLanguageClassifier(username, password);
				// Set end-point
				nlc.setEndPoint(url);
				// Protect data
				Map<String, String> headers = new HashMap<String, String>();
				headers.put("X-Watson-Learning-Opt-Out", "true");
				nlc.setDefaultHeaders(headers);
				// Read .csv with queries
				List<ClassifyInput> queries = new ArrayList<ClassifyInput>();
				try {
					
					CSVReader reader = new CSVReader(new FileReader(queriesFilePath), ',');
					String[] line = null;
					try {
						
						while ((line = reader.readNext()) != null) {
							
							for (String s: line) {
								
								ClassifyInput input = new ClassifyInput();
								input.setText(s);
								queries.add(input);
								
							}
							
						}
						
						reader.close();
					} catch (IOException e) {
						
						System.err.println("Error: " + e.getMessage());
						System.exit(1);
						
					}
				} catch (FileNotFoundException e) {
					
					System.err.println("Error: .csv file with queries missing!");
					System.exit(1);
					
				}
				if (queries.isEmpty()) {
					
					System.err.println("Error: List of queries is empty!");
					System.exit(1);
					
				}
				// Create classifier options
				ClassifyCollectionOptions classifyOptions = new ClassifyCollectionOptions.Builder()
				  .classifierId(classifierId)
				  .collection(queries)
				  .build();
				List<String> classes = new ArrayList<String>();
				try {
					
					// Execute call
					ClassificationCollection results = nlc.classifyCollection(classifyOptions).execute();
					for (CollectionItem item : results.getCollection()) {
						
						String topClass = item.getTopClass();
						for (ClassifiedClass someClass : item.getClasses()) {
							
							if (someClass.getClassName().equals(topClass)) {
								
								if (someClass.getConfidence() >= confidenceThreshold) {
									
									classes.add(topClass);
									
								} else {
									
									classes.add("-");
									
								}
								
							}
							
						}
						
					}
					
				} catch (ServiceResponseException e) {
					
				    System.err.println("Error: Service returned status code " + 
				    	e.getStatusCode() + ": " + e.getMessage());
				    System.exit(1);
				    
				}
				Gson gson = new Gson();
				System.out.println(gson.toJson(classes));
				
			} else if (args.length == 6) {
				
				String url = args[1];
				String apikey = args[2];
				String classifierId = args[3];
				String queriesFilePath = args[4];
				float confidenceThreshold = (float) 0.5;
				try {
					
					confidenceThreshold = Float.parseFloat(args[5]);
					if (confidenceThreshold < 0 || confidenceThreshold > 1) throw new Exception();
					
				} catch (Exception e) {
					
					System.err.println("Error: confidenceThreshold argument must be a numeric value " + 
									   "between 0 and 1");
					System.exit(1);
					
				}
				
				// Initialize classifier with credentials
				IamOptions options = new IamOptions.Builder().apiKey(apikey).build();
				NaturalLanguageClassifier nlc = new NaturalLanguageClassifier(options);
				// Set end-point
				nlc.setEndPoint(url);
				// Protect data
				Map<String, String> headers = new HashMap<String, String>();
				headers.put("X-Watson-Learning-Opt-Out", "true");
				nlc.setDefaultHeaders(headers);
				// Read .csv with queries
				List<ClassifyInput> queries = new ArrayList<ClassifyInput>();
				try {
					
					CSVReader reader = new CSVReader(new FileReader(queriesFilePath), ',');
					String[] line = null;
					try {
						
						while ((line = reader.readNext()) != null) {
							
							for (String s: line) {
								
								ClassifyInput input = new ClassifyInput();
								input.setText(s);
								queries.add(input);
								
							}
							
						}
						reader.close();
					} catch (IOException e) {
						
						System.err.println("Error: " + e.getMessage());
						System.exit(1);
						
					}
				} catch (FileNotFoundException e) {
					
					System.err.println("Error: .csv file with queries missing!");
					System.exit(1);
					
				}
				if (queries.isEmpty()) {
					
					System.err.println("Error: List of queries is empty!");
					System.exit(1);
					
				}
				// Create classifier options
				ClassifyCollectionOptions classifyOptions = new ClassifyCollectionOptions.Builder()
				  .classifierId(classifierId)
				  .collection(queries)
				  .build();
				List<String> classes = new ArrayList<String>();
				try {
					
					// Execute call
					ClassificationCollection results = nlc.classifyCollection(classifyOptions).execute();
					for (CollectionItem item : results.getCollection()) {
						
						String topClass = item.getTopClass();
						for (ClassifiedClass someClass : item.getClasses()) {
							
							if (someClass.getClassName().equals(topClass)) {
								
								if (someClass.getConfidence() >= confidenceThreshold) {
									
									classes.add(topClass);
									
								} else {
									
									classes.add("-");
									
								}
								
							}
							
						}
						
					}
					
				} catch (ServiceResponseException e) {
					
				    System.err.println("Error: Service returned status code " + 
				    	e.getStatusCode() + ": " + e.getMessage());
				    System.exit(1);
				    
				}
				Gson gson = new Gson();
				System.out.println(gson.toJson(classes));
				
			} else {
				
				System.err.println("Error: Expected 6 or 7 arguments for querying a classifier." + 
						" 6 arguments for api-key authentication in the following order: use(query) url api-key classifierId queriesFilePath confidenceThreshold." + 
						" 7 arguments for username-password authentication in the following order: use(query) url username password classifierId queriesFilePath confidenceThreshold");
				System.exit(1);
				
			}
			
		}
		
		// Delete classifier path
		if (use.equals("delete")) {
			
			// Treat arguments
			if (args.length == 5) {
				
				String url = args[1];
				String username = args[2];
				String password = args[3];
				String classifierId = args[4];
				// Initialize classifier with credentials
				NaturalLanguageClassifier nlc = new NaturalLanguageClassifier(username, password);
				// Set end-point
				nlc.setEndPoint(url);
				try {
					
					// Execute call
					DeleteClassifierOptions deleteOptions = new DeleteClassifierOptions.Builder()
						.classifierId(classifierId)
						.build();
					nlc.deleteClassifier(deleteOptions).execute();
					
				} catch (ServiceResponseException e) {
					
				    System.err.println("Error: Service returned status code " + 
				    	e.getStatusCode() + ": " + e.getMessage());
				    System.exit(1);
				    
				}
				
			} else if (args.length == 4) {
				
				String url = args[1];
				String apikey = args[2];
				String classifierId = args[3];
				
				// Initialize classifier with credentials
				IamOptions options = new IamOptions.Builder().apiKey(apikey).build();
				NaturalLanguageClassifier nlc = new NaturalLanguageClassifier(options);
				// Set end-point
				nlc.setEndPoint(url);
				try {
					
					// Execute call
					DeleteClassifierOptions deleteOptions = new DeleteClassifierOptions.Builder()
						.classifierId(classifierId)
						.build();
					nlc.deleteClassifier(deleteOptions).execute();
					
				} catch (ServiceResponseException e) {
					
				    System.err.println("Error: Service returned status code " + 
				    	e.getStatusCode() + ": " + e.getMessage());
				    System.exit(1);
				    
				}
				
			} else {
				
				System.err.println("Error: Expected 4 or 5 arguments for deleting a classifier." + 
						" 4 arguments for api-key authentication in the following order: use(delete) url api-key classifierId." + 
						" 5 arguments for username-password authentication in the following order: use(delete) url username password classifierId.");
				System.exit(1);
				
			}
		
		}
		
		// List classifiers path
		if (use.equals("list")) {
			
			// Treat arguments
			if (args.length == 4) {
				
				String url = args[1];
				String username = args[2];
				String password = args[3];
				// Initialize classifier with credentials
				NaturalLanguageClassifier nlc = new NaturalLanguageClassifier(username, password);
				// Set end-point
				nlc.setEndPoint(url);
				try {
					
					// Execute call
					ClassifierList classifiers = nlc.listClassifiers().execute();
					System.out.println(classifiers);
					
				} catch (ServiceResponseException e) {
					
				    System.err.println("Error: Service returned status code " + 
				    	e.getStatusCode() + ": " + e.getMessage());
				    System.exit(1);
				    
				}
				
			} else if (args.length == 3) {
				
				String url = args[1];
				String apikey= args[2];
				// Initialize classifier with credentials
				IamOptions options = new IamOptions.Builder().apiKey(apikey).build();
				NaturalLanguageClassifier nlc = new NaturalLanguageClassifier(options);
				// Set end-point
				nlc.setEndPoint(url);
				try {
					
					// Execute call
					ClassifierList classifiers = nlc.listClassifiers().execute();
					System.out.println(classifiers);
					
				} catch (ServiceResponseException e) {
					
				    System.err.println("Error: Service returned status code " + 
				    	e.getStatusCode() + ": " + e.getMessage());
				    System.exit(1);
				    
				}
				
			} else {
				
				System.err.println("Error: Expected 3 or 4 arguments for deleting a classifier." + 
						" 3 arguments for api-key authentication in the following order: use(list) url api-key." + 
						" 4 arguments for username-password authentication in the following order: use(list) url username password.");
				System.exit(1);
				
			}
			
		}
		
		// Get info about classifier path
		if (use.equals("info")) {
			
			// Treat arguments
			if (args.length == 5) {
				
				String url = args[1];
				String username = args[2];
				String password = args[3];
				String classifierId = args[4];
				// Initialize classifier with credentials
				NaturalLanguageClassifier nlc = new NaturalLanguageClassifier(username, password);
				// Set end-point
				nlc.setEndPoint(url);
				try {
					
					// Execute call
					GetClassifierOptions getOptions = new GetClassifierOptions.Builder()
						.classifierId(classifierId)
						.build();
					Classifier classifier = nlc.getClassifier(getOptions).execute();
					System.out.println(classifier);
					
				} catch (ServiceResponseException e) {
					
				    System.err.println("Error: Service returned status code " + 
				    	e.getStatusCode() + ": " + e.getMessage());
				    System.exit(1);
				    
				}
				
			} else if (args.length == 4) {
				
				String url = args[1];
				String apikey = args[2];
				String classifierId = args[3];
				// Initialize classifier with credentials
				IamOptions options = new IamOptions.Builder().apiKey(apikey).build();
				NaturalLanguageClassifier nlc = new NaturalLanguageClassifier(options);
				// Set end-point
				nlc.setEndPoint(url);
				try {
					
					// Execute call
					GetClassifierOptions getOptions = new GetClassifierOptions.Builder()
						.classifierId(classifierId)
						.build();
					Classifier classifier = nlc.getClassifier(getOptions).execute();
					System.out.println(classifier);
					
				} catch (ServiceResponseException e) {
					
				    System.err.println("Error: Service returned status code " + 
				    	e.getStatusCode() + ": " + e.getMessage());
				    System.exit(1);
				    
				}
				
			} else {
				
				System.err.println("Error: Expected 4 or 5 arguments for getting a classifier's information." + 
						" 4 arguments for api-key authentication in the following order: use(info) url api-key classifierId." + 
						" 5 arguments for username-password authentication in the following order: use(info) url username password classifierId.");
				System.exit(1);
				
			} 
			
		}
		
	}
	
}
