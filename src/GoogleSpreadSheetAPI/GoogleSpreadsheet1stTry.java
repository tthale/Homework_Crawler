package GoogleSpreadSheetAPI;

/*
 * This module attempts to (successfully) read in a copy of Yanmin's VCHS spreadsheet's primary worksheet and then
 * (unsuccessfully) create a new worksheet with the intention of subsequently populating it with tbale data.
 * Sadly, after 2 weeks of try this/that involving getting the right Google credentials for the new sheet, it fails with
 * various exceptions usually relating to authorization. Various forums complain that the (recommended) JSON credentials
 * aren't allowing the same operations permitted by the p12 based credentials (which is definitely sonething to try later,
 * similar to https://stackoverflow.com/questions/38107237/write-data-to-google-sheet-using-google-sheet-api-v4-java-sample-code
 * so, create a new CreateWorksheet when ready]). Another possibility is to just use the friggin' HTTP APIs (modeled first on
 * a JS before perhaps inplementing in Java)
 * 
 * For now, I've given up in the hopes SOMEBODY besides the Google API samples will come through with a relevant working example.
 */
import batch_update.CreateWorksheet;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.admin.directory.DirectoryScopes;
import com.google.api.services.sheets.v4.Sheets.Spreadsheets;
import com.google.api.services.sheets.v4.Sheets.Spreadsheets.Values;
import com.google.api.services.sheets.v4.Sheets.Spreadsheets.Values.Get;
import com.google.api.services.sheets.v4.SheetsRequest;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import com.google.api.services.sheets.v4.Sheets;
// import com.google.api.services.plus.Plus;	// part of one of the GoogleCredential authorization monstrosities
// import com.google.api.services.plus.PlusScopes;



import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class GoogleSpreadsheet1stTry {
    /** Application name. */
    private static final String APPLICATION_NAME =
        "VCHS_Spreadsheet";

    private static final String CLIENT_SECRET =    
    	"9q64bNFL8lyfIvModcvuQ2zV";

    private static final String GMAIL =    
		"ellayararwhy@gmail.com";
		
    /** Directory to store user credentials for this application. */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
        System.getProperty("user.home"), ".credentials/sheets.googleapis.com-java-quickstart");	// aka C:\Users\lhale_000\.credentials\sheets.googleapis.com-java-quickstart

    private static final java.io.File DATA_STORE_DIR_2 = new java.io.File(
        "./resources/data_store_2");
    
    /** Global instance of the {@link FileDataStoreFactory}. */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    // Downloaded from the Wizard @ https://console.developers.google.com/apis/credentials?project=academic-torch-178823 for the VCHS_Spreadsheet project
    private static String JSON_PROJECT_CREDENTIALS  = "../../resources/client_secret_162522261062-pk64ksoen5jae9uaii4qs7cpig9fekv1.apps.googleusercontent.com.json";
    private static String APPLICATION_JSON_CRED_FILENAME = 			  "client_secret_162522261062-pk64ksoen5jae9uaii4qs7cpig9fekv1.apps.googleusercontent.com.json";
    private static String SYSTEM_JSON_CRED_FILENAME = "My Project-552ecd170323.json";
    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY =
        JacksonFactory.getDefaultInstance();
 
    private static final JsonFactory JSON_FACTORY2 =
    		new GsonFactory();

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;

    /** Global instance of the user's credential */
    private static Credential credential;
    private static GoogleCredential goog_credential;	// Some APIs need a different flavor of the credential (1 size doesn't fit all - actually there's several - sheesh )
    static final String USER_FILE_TYPE = "authorized_user";				// Copied from GoogleCredentials for file type - must be in the JSON credentials file like so: "type": "service_account" 
    static final String SERVICE_ACCOUNT_FILE_TYPE = "service_account";	// Copied from GoogleCredentials for file type
    
    /** Global instance of the scopes required by this quickstart.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/sheets.googleapis.com-java-quickstart
     */
    private static final List<String> SCOPES =
        Arrays.asList(SheetsScopes.SPREADSHEETS);	// SPREADSHEETS_READONLY

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR_2);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */ 
    public static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in =
        		GoogleSpreadsheet1stTry.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets =
            GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(DATA_STORE_FACTORY)
                .setAccessType("offline")
                .build();
        Credential credential = new AuthorizationCodeInstalledApp(
            flow, new LocalServerReceiver()).authorize("user");
        System.out.println(
                "Credentials saved to " + DATA_STORE_DIR_2.getAbsolutePath());
        return credential;
    }

    // My own rendition of authorize. Upon turning on your Google Sheets API based off of an existing gmail account, follow the instructions @
    // https://developers.google.com/sheets/api/quickstart/java in order to download your secret API key in JSON format & then put in in a 
    // project resource area that's part of the (classloader) classpath, which in my case is the ./respources sibling directory
     public static Credential authorize_me(String credential_resource_filename) throws IOException {
         // Load client secrets.
    	 // Dbg says the 1st one is the one (after an adjustment on classpath was made in project settings):
         InputStream in =
         		GoogleSpreadsheet1stTry.class.getResourceAsStream("../../resources/" + credential_resource_filename);
         if ( in == null) {
        	 in = GoogleSpreadsheet1stTry.class.getResourceAsStream("/../../resources/" + credential_resource_filename);
         }
         if ( in == null) {
        	 in = GoogleSpreadsheet1stTry.class.getResourceAsStream("/../../../resources/" + credential_resource_filename);
         }
         if ( in == null) {
        	 in = GoogleSpreadsheet1stTry.class.getResourceAsStream("../../../resources/" + credential_resource_filename);
         }
         if ( in == null) {
        	 in = GoogleSpreadsheet1stTry.class.getResourceAsStream("../resources/" + credential_resource_filename);
         }
         if ( in == null) {
        	 in = GoogleSpreadsheet1stTry.class.getResourceAsStream("" + credential_resource_filename);
         }
         if ( in == null) {
        	 in = GoogleSpreadsheet1stTry.class.getResourceAsStream("/resources/" + credential_resource_filename);
         }
         if ( in == null) {
        	 in = GoogleSpreadsheet1stTry.class.getResourceAsStream("/" + credential_resource_filename);
         }
         if ( in == null) {
        	 in = GoogleSpreadsheet1stTry.class.getResourceAsStream("C:\\Applications\\MyEclipse2015CI\\Workspaces\\VCHS_HomeworkCrawler\\resources\\" + credential_resource_filename);
         }
         if ( in == null) {
        	 System.err.println("Can't find JSON_PROJECT_CREDENTIALS - " + credential_resource_filename);
        	 System.exit(1);
         }
         GoogleClientSecrets clientSecrets =
             GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

         // Build flow and trigger user authorization request.
         GoogleAuthorizationCodeFlow flow =
                 new GoogleAuthorizationCodeFlow.Builder(
                         HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                 .setDataStoreFactory(DATA_STORE_FACTORY)
                 .setAccessType("offline")
                 .build();
          	credential = new AuthorizationCodeInstalledApp(
             flow, new LocalServerReceiver()).authorize("user");
          	
         System.out.println(
                 "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
         return credential;
     }	// authorize_me

     public static GoogleCredential system_authorize_me(String credential_resource_filename) throws IOException {
    	 GoogleCredential credentials = null;
    	 
    	 // Dbg says the 1st one is the one (after an adjustment on classpath was made in project settings):
         InputStream system_level_creds =
         		GoogleSpreadsheet1stTry.class.getResourceAsStream("../../resources/" + credential_resource_filename);
         if ( system_level_creds == null) {
        	 system_level_creds = GoogleSpreadsheet1stTry.class.getResourceAsStream("/../../resources/" + credential_resource_filename);
         }
         if ( system_level_creds == null) {
        	 system_level_creds = GoogleSpreadsheet1stTry.class.getResourceAsStream("/../../../resources/" + credential_resource_filename);
         }
         if ( system_level_creds == null) {
        	 system_level_creds = GoogleSpreadsheet1stTry.class.getResourceAsStream("../../../resources/" + credential_resource_filename);
         }
         if ( system_level_creds == null) {
        	 system_level_creds = GoogleSpreadsheet1stTry.class.getResourceAsStream("../resources/" + credential_resource_filename);
         }
         if ( system_level_creds == null) {
        	 system_level_creds = GoogleSpreadsheet1stTry.class.getResourceAsStream("" + credential_resource_filename);
         }
         if ( system_level_creds == null) {
        	 system_level_creds = GoogleSpreadsheet1stTry.class.getResourceAsStream("/resources/" + credential_resource_filename);
         }
         if ( system_level_creds == null) {
        	 system_level_creds = GoogleSpreadsheet1stTry.class.getResourceAsStream("/" + credential_resource_filename);
         }
         if ( system_level_creds == null) {
        	 system_level_creds = GoogleSpreadsheet1stTry.class.getResourceAsStream("C:\\Applications\\MyEclipse2015CI\\Workspaces\\VCHS_HomeworkCrawler\\resources\\" + credential_resource_filename);
         }
         if ( system_level_creds == null) {
        	 System.err.println("Can't find JSON_PROJECT_CREDENTIALS - " + credential_resource_filename);
        	 System.exit(1);
         }
          	// Try to create a GoogleCredential for those APIs requiring it (wtf)
//            InputStream in_again =
//             		GoogleSpreadsheet1stTry.class.getResourceAsStream("../../resources/" + credential_resource_filename);
//          	goog_credential = GoogleCredential.fromStream(in_again, HTTP_TRANSPORT, JSON_FACTORY);	// IOException: Error reading credentials from stream, 'type' field not specified.
//          	goog_credential = GoogleCredential.fromStream(in_again);	// IOException: Error reading credentials from stream, 'type' field not specified.
/*            GoogleCredential credential = new GoogleCredential.Builder()
            .setTransport(HTTP_TRANSPORT)
            .setJsonFactory(JSON_FACTORY)
            .setServiceAccountId(GMAIL)
            .setServiceAccountScopes(SCOPES)
            .setClientSecrets(clientSecrets);
*/
/*
 *             try {
				goog_credential = createCredentialForServiceAccount(HTTP_TRANSPORT,JSON_FACTORY, GMAIL, SCOPES, clientSecrets);
			} catch (GeneralSecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
*/
/*
 *             try {
            	STOPPED HERE  - Need to add this friggin type to the JSON input stream (WTF)
            	String file_type = SERVICE_ACCOUNT_FILE_TYPE;	// Add to JSON cfedentials - "type": "service_account" OR "authorized_user"
            	goog_credential = getGoogleCredentials(in_again, HTTP_TRANSPORT, JSON_FACTORY);
			} catch (GeneralSecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
 */           
            try {
//            	String file_type = SERVICE_ACCOUNT_FILE_TYPE;	// Add to JSON credentials - "type": "service_account" OR "authorized_user"
            	goog_credential = getGoogleCredentials(system_level_creds, HTTP_TRANSPORT, JSON_FACTORY);
			} catch (GeneralSecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
         System.out.println(
                 "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
         return credentials;
     }	// system_authorize_me
     
     // Ref: https://github.com/google/google-api-java-client/issues/1007
 	
     public static GoogleCredential getGoogleCredentials(InputStream credentialsJSON, HttpTransport httpTransport, JsonFactory jsonFactory) throws GeneralSecurityException, IOException {
    	 List scopes = new ArrayList<>();
    	 scopes.add(DirectoryScopes.ADMIN_DIRECTORY_USER);
    	 scopes.add(DirectoryScopes.ADMIN_DIRECTORY_USER_READONLY);
    	 GoogleCredential gcFromJson = GoogleCredential.fromStream(credentialsJSON, httpTransport, jsonFactory).createScoped(scopes);    	 // IOException: Error reading credentials from stream, 'type' field not specified.

    	 return new GoogleCredential.Builder()
    	         .setTransport(gcFromJson.getTransport())
    	         .setJsonFactory(gcFromJson.getJsonFactory())
    	         .setServiceAccountId(gcFromJson.getServiceAccountId())
    	         .setServiceAccountUser("user@example.com")
    	         .setServiceAccountPrivateKey(gcFromJson.getServiceAccountPrivateKey())
    	         .setServiceAccountScopes(gcFromJson.getServiceAccountScopes())
    	         .build();
     }
     
     // This damn thing fails b/c it needs to .setServiceAccountPrivateKey() - WTF - don't have one
     public static GoogleCredential createCredentialForServiceAccount(
    	      HttpTransport transport,
    	      JsonFactory jsonFactory,
    	      String serviceAccountId,
    	      Collection<String> serviceAccountScopes,
    	      GoogleClientSecrets clientSecrets) throws GeneralSecurityException, IOException {
    	    return new GoogleCredential.Builder().setTransport(transport)
    	        .setJsonFactory(jsonFactory)
    	        .setServiceAccountId(serviceAccountId)
    	        .setServiceAccountUser(serviceAccountId)
    	        .setServiceAccountScopes(serviceAccountScopes)
    	        .setClientSecrets(clientSecrets)
    	        .build();
    	  }
     
/*
 *  Ref: https://developers.google.com/api-client-library/java/google-api-java-client/reference/1.20.0/com/google/api/client/googleapis/auth/oauth2/GoogleCredential
 *  Needs cutting down & tweaking
     public static GoogleCredential getCredentialInstanceFromStream(InputStream credentialsJSON,
             Set<String> scopes) throws GeneralSecurityException, IOException {

         HashMap result = new ObjectMapper().readValue(
                 credentialsJSON, HashMap.class);
         JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
         HttpTransport httpTransport = GoogleNetHttpTransport
                 .newTrustedTransport();
         String clientId = (String) result.get("client_id");
         String clientEmail = (String) result.get("client_email");
         String privateKeyPem = (String) result.get("private_key");
         String privateKeyId = (String) result.get("private_key_id");
         PrivateKey privateKey = privateKeyFromPkcs8(privateKeyPem);
         GoogleCredential credential = new GoogleCredential.Builder()
                 .setTransport(HTTP_TRANSPORT).setJsonFactory(JSON_FACTORY)
                 .setServiceAccountId(GMAIL)
                 .setServiceAccountScopes(SCOPES)
                 .setClientSecrets(clientSecrets);
         return credential;
     }
*/     
     // Generate credential directly from secret key (could use others, like .p12, secret,json, etc)
    // Ref: https://stackoverflow.com/questions/37986171/google-sheet-api-v4java-append-date-in-cells
    // Not sure HOW to get it to consume the JSON credential file like authorize does.
    public static Credential authorize2() throws IOException {
		GoogleCredential credential = new GoogleCredential.Builder()
        .setTransport(HTTP_TRANSPORT)
        .setJsonFactory(JSON_FACTORY)
        .setServiceAccountId(GMAIL )
        .setClientSecrets(APPLICATION_NAME, CLIENT_SECRET )
        .setServiceAccountScopes(SCOPES)
        .build();
        credential.refreshToken();

        return credential;
    }
    /**
     * Build and return an authorized Sheets API client service.
     * @return an authorized Sheets API client service
     * @throws IOException
     */
    public static Sheets getSheetsService() throws IOException {
        Credential credential = authorize_me(APPLICATION_JSON_CRED_FILENAME);
        return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static void main(String[] args) throws IOException {
        // Build a new authorized API client service.
        Sheets service = getSheetsService();

        // Prints portions of 'Copy of ThomasHW_Tracker_fall2017.xlsx spreadsheet:
        // https://docs.google.com/spreadsheets/d/1Xr5IjGZ7jPYoeljLhvgqWFq6WN7uk4IF3FlJRt5dAh8/edit#gid=1358083693
        String spreadsheetId = "1Xr5IjGZ7jPYoeljLhvgqWFq6WN7uk4IF3FlJRt5dAh8";	// Copy of Tom's VCHS Spreadsheet
//        String range = "Class Data!A2:E";
//        String range = "Sheet1!A2:E10";	// GoogleJsonResponseException: 400 Bad Request "Unable to parse range: ..."
        String WorksheetId = "HomeWorkTracker";	// Ref: https://stackoverflow.com/questions/37893515/google-spreadsheet-api-400-error-bad-request-unable-to-parse-range
        String full_range = WorksheetId ;	// Providing this below will grab all Worksheet rows (that the API thinks is relevant)
        String partial_range = WorksheetId + "!" + "A11:F12";	// Range = top left corner to bottom right corner
        Integer number_of_columns = 6;	// This has to match the range above and expected size below
        
        Sheets.Spreadsheets.Get spread_sheets = service.spreadsheets().get(spreadsheetId);
        String sheets_request = spread_sheets.getFields();
        
        /* Useless for getting current sprdsht props
        Spreadsheet sprd_shit = new Spreadsheet();	// only available constructor
        SpreadsheetProperties ssp = sprd_shit.getProperties();
        */
        
        ValueRange response = service.spreadsheets().values()
            .get(spreadsheetId, full_range)
            .execute();	// specify full_range or partial_range
        List<List<Object>> values = response.getValues();
        if (values == null || values.size() == 0) {
            System.err.println("No data found.");
        } else {
          System.out.println("Course #, Course Name, Course Date, Homework Item, HW Due Date, Status, Comments, Submitted");
          for (List row : values) {
        	  int row_size = row.size();
    		  // Let's try to write something in the blank cell (BOGUS)
    		  for ( int col_index = 0; col_index < number_of_columns; col_index++) {	// The API doesn't handle blank cells at all (BOGUS)
    			  String col_info = "notta";
    			  try {
    				  col_info = (String) row.get(col_index);
    			  } catch( IndexOutOfBoundsException ioob) {
    				  col_info = "________";
    				  row.add(col_index, col_info);	// There has to be something in the blank cell (BOGUS)
    			  }
    			  catch (Exception e) {
    				  System.err.println("General unhandled exception:" + e.getMessage());
    		          System.exit(1);
    			  }
    	            // Print whatever columns are there
    	            System.out.printf("%s\t", col_info);
    		  }
              System.out.printf("%s", "\n");
            // Print columns A and E, which correspond to indices 0 and 4 (aka columns A thru E)
//            System.out.printf("%s, %s, %s, %s, %s, %s\n", row.get(0), row.get(1), row.get(2), row.get(3), row.get(4), row.get(5));
          }
        }
        
        // OK, looks like a system-level credential is needed to create/manage a new worksheet (WTF)

        GoogleCredential goog_credential = system_authorize_me(SYSTEM_JSON_CRED_FILENAME);
        // try to write out simulated data to a newly created worksheet
        CreateWorksheet new_worksheet = new CreateWorksheet(spreadsheetId, goog_credential, "New Sheet");
    }
/*
    static boolean addWorkSheet (String worksheet_name) {
    	
    	AddSheetRequest added_worksheet = new AddSheetRequest();	// Ref: https://developers.google.com/resources/api-libraries/documentation/sheets/v4/java/latest/
    	SheetProperties added_worksheet_properties = added_worksheet.getProperties();
    	added_worksheet_properties.getSheetId();
    	added_worksheet_properties.
    	return true;
    }
    */
}