package batch_update;
/*
 * Refs: 1) https://developers.google.com/sheets/api/samples/sheet
		 2) https://developers.google.com/sheets/api/reference/rest/v4/spreadsheets/batchUpdate
 * 
 * BEFORE RUNNING:
 * ---------------
 * 1. If not already done, enable the Google Sheets API
 *    and check the quota for your project at
 *    https://console.developers.google.com/apis/api/sheets
 * 2. Install the Java client library on Maven or Gradle. Check installation
 *    instructions at https://github.com/google/google-api-java-client.
 *    On other build systems, you can add the jar files to your project from
 *    https://developers.google.com/resources/api-libraries/download/sheets/v4/java
 */

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetResponse;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class CreateWorksheet {
	
	private String new_sheet_name = null;
	
	public CreateWorksheet(String spreadsheetId, Credential credential, String new_sheet_name) {
		this.new_sheet_name = new_sheet_name;

    // TODO: Assign values to desired fields of `requestBody`:
    BatchUpdateSpreadsheetRequest requestBody = new BatchUpdateSpreadsheetRequest();

    Sheets sheetsService = null;
	try {
		sheetsService = createSheetsService((GoogleCredential) credential);
	} catch (IOException | GeneralSecurityException e) {
		System.err.println(e.getMessage());
		e.printStackTrace();
	}
    Sheets.Spreadsheets.BatchUpdate request = null;
	try {
		request = sheetsService.spreadsheets().batchUpdate(spreadsheetId, requestBody);
	} catch (IOException e) {
		System.err.println(e.getMessage());
		e.printStackTrace();
	}

    BatchUpdateSpreadsheetResponse response = null;
	try {
		response = request.execute();
	} catch (IOException e) {
		System.err.println(e.getMessage());
		e.printStackTrace();
	}

	finally {
		if (sheetsService != null && request != null &&  response != null)
		    // TODO: Change code below to process the `response` object:
		    System.out.println(response);
	}
  }

  public CreateWorksheet(String spreadsheetId) {
		// TODO Auto-generated constructor stub
	}

public Sheets createSheetsService(GoogleCredential credential) throws IOException, GeneralSecurityException {
    HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

    // TODO: Change placeholder below to generate authentication credentials. See
    // https://developers.google.com/sheets/quickstart/java#step_3_set_up_the_sample
    //
    // Authorize using one of the following scopes:
    //   "https://www.googleapis.com/auth/drive"
    //   "https://www.googleapis.com/auth/drive.file"
    //   "https://www.googleapis.com/auth/spreadsheets"

    return new Sheets.Builder(httpTransport, jsonFactory, credential)
        .setApplicationName(new_sheet_name )
        .build();	// was "Google-SheetsSample/0.1"
  }
}
