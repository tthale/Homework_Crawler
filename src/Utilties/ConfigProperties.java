package Utilties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

/*
 * A collection of all configuration properties needed by the Homework crawler
 * @author: Mean Green Software
 * 
 * NOTE: all class URL links must add a "_link" suffix to the class name (as defined for each class in the classes property)
 */
public class ConfigProperties {
	private String login_destination;
	private String username;	// Google drive account username
	private String password;
	private Boolean fill_spreadsheet_run;
	private String fill_spreadsheet_location;
	private String getopts_location;
	private String webdriver_chrome_driver;
	private String classes;	// the student's classes we care about
	private HashMap<String, String> class_links;
	private String shell;
	private String spreadsheet_name;
	private String worksheet_caption;
	
	public ConfigProperties(String login_destination, String username,
			String password, Boolean fill_spreadsheet_run, String fill_spreadsheet_location, String getopts_location,
			String webdriver_chrome_driver, String classes, String shell, String spreadsheet_name, String worksheet_caption) {
		super();
		this.login_destination = login_destination;
		this.username = username;
		this.password = password;
		this.fill_spreadsheet_run = fill_spreadsheet_run;
		this.fill_spreadsheet_location = fill_spreadsheet_location;
		this.getopts_location = getopts_location;
		this.webdriver_chrome_driver = webdriver_chrome_driver;
		this.classes = classes;
		this.shell = shell;
		this.spreadsheet_name = spreadsheet_name;
		this.worksheet_caption = worksheet_caption;
	}

	public void extract_class_links(Properties props) {
		StringTokenizer st = new StringTokenizer(classes, " ");	// Ref: http://crunchify.com/java-stringtokenizer-and-string-split-example/
		class_links = new HashMap<String, String>();
		while ( st.hasMoreElements()) {
			String class_link_key = st.nextElement() + "_link";
			String class_url = props.getProperty(class_link_key);
			// TODO: Check to see that a property value is returned for this class key
			class_links.put(class_link_key, class_url);
		}
	}

	public String getLogin_destination() {
		return login_destination;
	}

	// TODO decrypt this property
	public String getUsername() {
		return username;
	}

	// TODO decrypt this property
	public String getPassword() {
		return password;
	}

	public Boolean getFill_spreadsheet_run() {
		return fill_spreadsheet_run;
	}

	public String getFill_spreadsheet_location() {
		return fill_spreadsheet_location;
	}

	public String getGetopts_location() {
		return getopts_location;
	}

	public String getWebdriver_chrome_driver() {
		return webdriver_chrome_driver;
	}

	public String getClasses() {
		return classes;
	}

	public HashMap<String, String> getClass_links() {
		return class_links;
	}

	public String getWorksheet_caption() {
		return worksheet_caption;
	}

	public String getSpreadsheet_name() {
		return spreadsheet_name;
	}

	public String getShell() {
		return shell;
	}
}

/*
 * login_destination=https\://learn.vcs.net/login/index.php
username=Thomas.Hale
password=@Vcs7500
fill_spreadsheet_location=C\:\\Applications\\Python
webdriver.chrome.driver=C\:\\\\Applications\\\\Selenium\\\\chromedriver_win32\\\\chromedriver.exe
classes=chemistry algebra history mandarin threeD_animiation english bible
history_link=https\://learn.vcs.net/mod/lesson/view.php?id\=122406&pageid\=235910&startlastseen\=yes
mandarin_link=https\://learn.vcs.net/mod/lesson/view.php?id\=122578&startlastseen\=yes
english_link=https\://learn.vcs.net/mod/lesson/view.php?id\=124192&startlastseen\=yes
threeD_animiation_link=https\://learn.vcs.net/mod/lesson/view.php?id\=102400&startlastseen\=yes
algebra2honors_link=https\://learn.vcs.net/mod/lesson/view.php?id\=127440&startlastseen\=yes
chemistryhonors_link=https\://learn.vcs.net/mod/lesson/view.php?id\=124126&startlastseen\=yes
# bible_link=https\://learn.vcs.net/mod/lesson/view.php?id\=102106&pageid\=194834&startlastseen\=yes

# Properties used by fill_spreadsheet (sharing ids are comma-separated)
worksheet_caption=Course Name,Course Date,Homework Item, HW Due Date,Status,Submission,Comments
 */