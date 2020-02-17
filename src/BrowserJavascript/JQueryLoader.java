package BrowserJavascript;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

public class JQueryLoader {
	
	  // its nice to keep JavaScript snippets in separate files.
    private static final String JQUERY_LOAD_SCRIPT = "resources\\jQuerify.js";
	
	JavascriptExecutor javascript_executor;
	
	// javascript_executor is a WebDriver that connected to a URL and has selected a browser tab
	public JQueryLoader(JavascriptExecutor javascript_executor) {
		this.javascript_executor = javascript_executor;
	}

	// 
	
	public boolean loadJQuery() {

        String jQueryLoader;
        WebDriver driver = (WebDriver) javascript_executor;
        
		try {
			jQueryLoader = readFile(JQUERY_LOAD_SCRIPT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

        // give jQuery time to load asynchronously
        driver.manage().timeouts().setScriptTimeout(10, TimeUnit.SECONDS);
//        JavascriptExecutor js = (JavascriptExecutor) driver;
        javascript_executor.executeAsyncScript(jQueryLoader /*, "https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js" */);

        // ready to rock
        javascript_executor.executeScript(
            "jQuery(function($) { " +
                " $('#lst-ib').val('bada-BANG'); " +
            " }); "
        );		
		return true;
	}
	

    // helper method
    private static String readFile(String file) throws IOException {
        Charset cs = Charset.forName("UTF-8");
        FileInputStream stream = new FileInputStream(file);
        try {
            Reader reader = new BufferedReader(new InputStreamReader(stream, cs));
            StringBuilder builder = new StringBuilder();
            char[] buffer = new char[8192];
            int read;
            while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
                builder.append(buffer, 0, read);
            }
            return builder.toString();
        }
        finally {
            stream.close();
        }        
    }
}
