package ChromeBrowser;

/**
 * VCHS_HomeworkCrawler - connect to vcs.learn.com & traverse all of Tom's 
 * homework items and export to CSV spreadsheets
 * 
 * Build & export all to an executable jar and run like so:
 * java -cp VCHS_HomeworkCrawler.jar ChromeBrowser.VCHS_HomeworkCrawler3 all
 * 
 * @author: Mean Green Software
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;	// https://mvnrepository.com/artifact/org.seleniumhq.selenium/selenium-server/3.5.1
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;	// https://mvnrepository.com/artifact/org.seleniumhq.selenium/selenium-chrome-driver/3.5.1
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import Utilties.ConfigProperties;
import Utilties.CrunchifyGetPropertyValues;
import BrowserJavascript.JQueryLoader;

/* NOTE: This is a Java8 program. Anything less gets java.lang.UnsupportedClassVersionError: org/openqa/selenium/Capabilities : Unsupported major.minor version 52.0 ,
 * Thanks to 1) https://www.youtube.com/watch?v=BtmeQOcdIKI
 *			2) https://sites.google.com/a/chromium.org/chromedriver/extensions
 *			3) https://groups.google.com/forum/#!topic/selenium-users/CL8kdxhgdj0
 *			4) https://sites.google.com/a/chromium.org/chromedriver/capabilities
 *			5) 
 *
 *	What this program version does:
 *		1) Opens a Chrome browser and uses Selenium to sign on to Powerschool and walk down through each pre-defined class top link 
 *		2) After navigating to pre-defined class targets, grabs selected HTML content & collects homework from 3 days past to tomorrow
 *		3) Dumps all class homework items into a date-labelled CSV spreadsheet file (Another same-day run just appends more rows to it)
 *		4) Executes the fill_spreadsheet.py Python program and feeds it the list of collected homeworks, one at a time.
 *			(fill.py creates a new worksheet for current day in the defined Google spreadsheet and populates it with the homework items)
 *			NOTE: This program will externally execute fill_spreadsheet.py for now, until it can read from config.properties. 
 
 *	Major TODO items:
 *		1) Genericize this program for all VCHS students (possibly host a server running this code or integrate into Powerschool)
 *		2) The Google APIs have quotas we're bumping up against - see https://developers.google.com/analytics/devguides/config/mgmt/v3/limits-quotas
 *			(I beieve the rate is the main violater, so either throttle the crawler calling fill_spreadsheet or figure out if batching the whole worksheet would work)
 *		3) Add smarts to discern weekends, holidays, holiday breaks and in-service days or equivalent off days
 *		4) Given any studen't credentials, a first-pass of the crawler will figure out student's classes, 
 *			grab the top link to each class and write them out to the config file. This is important b/c each teacher
 *			is just starting to put in class homework at the start of each semester, which may have different classes from the previous semester.
 *		5) Close out the Chrome browser after program is complete (or provide a confirm window at the end, in case user wants to keep it open)
 *		6) Transform all println's into log4j (or equivalent) logging (either to console or log file)
 *		7) Convert this program into a web application, perhaps even integrate into VCHS's learn.vcs web infrastructure using its credential session.
 *			Also, all config.properties would become user preferences residing online
 *		8) Catch all top-level exceptions instead of barfing out the stack backtrace. Things like session resets, timeouts, etc to do a better wazzup for the user
 *		9) A system installer that installs this application, JVM (if needed), Python 2.x (if needed) and the Google Spreadsheet Updater application.
 *			(This would be much easier if #6 above gets implemented)
 */

/**
 * @author lhale - Mean Green Software
 *
 */
public class VCHS_HomeworkCrawler3 {
	// String constants
	static String HOMEWORK = "homework";
	static String WORKSHEET_CAPTION = "Course Name,Course Date,Homework Item, HW Due Date,Status,Submission,Comments";
		
	static String login_destination = "https://learn.vcs.net/login/index.php";
	static String username = "Thomas.Hale";
	static String password = "@Vcs7500";
	
	// Query params:
	// id= class (course) within a quarter (So, needs to be updated every quarter)
	// pageid corresponds to particular days in a quarter. So, I'll need to derive these based on today's date
	// startlastseen=no used to transition to other courses
	static String history_link = "https://learn.vcs.net/mod/lesson/view.php?id=122406&pageid=235910&startlastseen=yes";	// Long_month A/B, 2017 OR Short month A/(next) Short month B
	static String mandarin_link = "https://learn.vcs.net/mod/lesson/view.php?id=122578&startlastseen=yes";	// Long_month A, 2017 (A single or dbl digit)
	static String english_link = "https://learn.vcs.net/mod/lesson/view.php?id=124192&startlastseen=yes";	// Long_month A/B, 2017 (A&B)
	static String threeD_film_animiation = "https://learn.vcs.net/mod/lesson/view.php?id=102400&startlastseen=yes";	// Short_Mon A, 2017 (A single or dbl digit)
	static String algebra2honors = "https://learn.vcs.net/mod/lesson/view.php?id=127440&startlastseen=yes";	// Long_month A/B -OR- Long_month XX (rarer) 
	static String chemistryhonors = "https://learn.vcs.net/mod/lesson/view.php?id=124126&startlastseen=yes";	// Long_day, Long_month A/B (one or two digits)
	static String bible = "https://learn.vcs.net/mod/lesson/view.php?id=102106&pageid=194834&startlastseen=yes";	// Mon A/B, 2017
	static String link_preamble = "https://learn.vcs.net/mod/lesson/view.php?id=";
	static String link_postamble = "&startlastseen=yes";
	static String google_jquery_script = "jQuery(function($) { " + " $('#lst-ib').val('bada-BANG'); " + " }); ";
	static String google_js_script = "document.find'#lst-ib').val('bada-bing'); " + " }); ";
	static String blank_slate_url = "https://www.google.com";

	static String logout_a_img_link = "";	// STOPPED HERE - what's the link? <a> or <img> or ...
	
	static String CHEMISTRY = "chemistry";
	static String ALGEBRA = "algebra";
	static String HISTORY = "history";
	static String MANDARIN = "mandarin";
	static String THREE_D = "3D";
	static String ENGLISH = "english";
	static String BIBLE = "bible";
	static String ALL = "all";
	static String[] ALL_COURSES = new String[] {ALGEBRA ,CHEMISTRY ,HISTORY ,ENGLISH ,MANDARIN ,THREE_D};
	
	// Misc.
	static Integer SECONDS_PER_DAY = 86400;
	static String UNKNOWN = "Unknown";
	static HashMap homeworkLookup = new HashMap();	// Each homework entry encoded with the course name and homework text (to eliminate dups)
	
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
        Date curDate = new Date();	// NOTE: A msecs long can be inserted here to spoof a time e.g. 1234567890999L
        Long curTime = curDate.getTime() / 1000;	// getTime returns milliseconds from epoch (1970)
        // Unfortunately, each teacher does not standardize on a date format, so...
		SimpleDateFormat  formatter = new SimpleDateFormat("MMMM dd, yyyy");	// https://examples.javacodegeeks.com/core-java/text/java-simpledateformat-example/
		SimpleDateFormat  formatter_weekday = new SimpleDateFormat("EEEE MMMM dd, yyyy");	// https://stackoverflow.com/questions/5121976/is-there-a-date-format-to-display-the-day-of-the-week-in-java
		SimpleDateFormat  formatter2 = new SimpleDateFormat("MMMM_dd");
		SimpleDateFormat  formatter3 = new SimpleDateFormat("MMM_dd");
		String todayDateToStr = formatter.format(curDate);
		String DateToWeekdayStr = formatter_weekday.format(curDate);	// Needed for Chemistry
		String template_filename = formatter2.format(curDate);
		String google_worksheetname = formatter3.format(curDate);	// Each day's worksheet is just a short date moniker
		ConfigProperties config_properties = null;
		
	    if(args.length == 0)   // Should have at least 1 argument passed in
	    {
	        System.out.println("Proper Usage is: java VCHS_HomeworkCrawler3 all or a combination of the following course names:");
	        System.out.println("chemistry algebra history mandarin 3D english bible");
	        System.exit(0);
	    }
	    
	    // 'all' indicates to grab from the config properties file
	    if ( args[0].contains(ALL)) {
	    	args = ALL_COURSES;
	    	CrunchifyGetPropertyValues properties = new CrunchifyGetPropertyValues();
	    	try {
	    		config_properties = properties.getPropValues();
	    		if ( config_properties == null) {
	    			throw new IOException("Can't get the properties from config.properties - exiting");
	    		}
	    		args = config_properties.getClasses().split(" ");
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	    
		System.out.println("Today's date: " + todayDateToStr);
		// NOTE: Some courses use the short month format ("MMM dd, yyyy")  w/ A&B days like so : Mon A/B, 2017, or even something different
		//	Ref: https://stackoverflow.com/questions/2219139/how-to-parse-month-full-form-string-using-dateformat-in-java
		String short_month = (new SimpleDateFormat("MMM")).format(curDate);
		String long_month = (new SimpleDateFormat("MMMM")).format(curDate);
		String day = (new SimpleDateFormat("dd")).format(curDate);
		
		System.setProperty("webdriver.chrome.driver", config_properties.getWebdriver_chrome_driver());
        DesiredCapabilities capabilities = DesiredCapabilities.chrome();	// https://stackoverflow.com/questions/43143014/chrome-is-being-controlled-by-automated-test-software
        ChromeOptions options = new ChromeOptions();
        options.addArguments(Arrays.asList("--no-sandbox","--ignore-certificate-errors","--homepage=about:blank","--no-first-run"));
        options.addArguments("disable-infobars");
        capabilities.setCapability(ChromeOptions.CAPABILITY, options);
		WebDriver driver = new ChromeDriver(capabilities);	// This opens up 2 new windows (aka browser tabs)
		driver.get(config_properties.getLogin_destination());
		// Apparently, the only way to get rid of the 1st Settings tab is to switch to it, close it and then switch over to the VCHS one (Don't know why their array order is reversed)
		ArrayList<String> two_tabs = new ArrayList<String> (driver.getWindowHandles());
		// Apparently (also), some system's Chrome browsers only open up a single tab, so check for this
		if (two_tabs.size() > 1)  {
			driver.switchTo().window(two_tabs.get(1));
			driver.close();
			driver.switchTo().window(two_tabs.get(0));	// 0 is the 2nd VCHS tab (?!?)
		}
		
		System.out.println("Title:" + driver.getTitle());
		// Time to login ... Ref: https://groups.google.com/forum/#!topic/selenium-users/CL8kdxhgdj0
		WebElement uname = driver.findElement(By.name("username"));
		uname.sendKeys(config_properties.getUsername());
		WebElement pwd = driver.findElement(By.name("password"));
		pwd.sendKeys(config_properties.getPassword());
		WebElement loginbtn = driver.findElement(By.id("loginbtn"));
		loginbtn.click();	// submit login form (doesn't appear to need a user-agent)
		// Time to navigate to today's HISTORY lesson
//		driver.navigate().to(history_link);
		
		// Time to navigate to today's MANDARIN lesson
//		driver.navigate().to(mandarin_link);

		// Time to navigate to today's ENGLISH lesson
//		driver.navigate().to(english_link);

		// Time to navigate to today's 3D lesson
//		driver.navigate().to(threeD_film_animiation);

		// Time to navigate to today's CHEMISTRY lesson
//		driver.navigate().to(chemistryhonors);

		// Create the CSV template file used to dump all course information
		FileOutputStream spreadsheet_fos = createCSV_OutputStream(template_filename, config_properties);
		Boolean call_python = config_properties.getFill_spreadsheet_run();
		if ( spreadsheet_fos == null) {
			System.err.println("Can't create CSV file " + template_filename);
			System.exit(1);
		}
		
		String spreadsheet_updater_loc = config_properties.getFill_spreadsheet_location();
		String getopts_loc = config_properties.getGetopts_location();
		// Courses tested: algebra chemistry history (some A/B dups need to be filtered out), english (redundant A/B day items), mandarin, 3D
	    for (String course : args) {

	    	String course_link = link_preamble + config_properties.getClass_links().get(course + "_link") + link_postamble;
    		System.out.println(course + "@" + course_link + ':');
			driver.navigate().to(course_link);
/*	    	
	    	// Check for the recurring prompt page (it shows up on 50% of the courses)
	    	<span class="lessonbutton standardbutton"><a href="https://learn.vcs.net/mod/lesson/view.php?id=122405&amp;pageid=235901&amp;startlastseen=yes">Yes</a></span>
	    	"last page you saw?"
	    	WebElement recurring_prompt = driver.findElement(By.xpath("div[@class='lessonbutton standardbutton']"));
	    	Ref: https://sqa.stackexchange.com/questions/20724/how-to-find-element-by-class-name-or-xpath
	    	(For now, appending the &startlastseen=yes to the course link seems to be working)
*/	    	
			WebElement aside_tag_elements = driver.findElement(By.tagName("aside"));
/*	DBG			String aside_tag_content = aside_tag_elements.getAttribute("innerHTML");
				System.out.println("Menu:\n" + aside_tag_content);
*/
			// Iterate through the list to find recent-date lessons
			List<WebElement> menu_list_elements = aside_tag_elements.findElements(By.tagName("li"));	// Each menu selection is an <li>
			String link_to_lesson = null;
			
			String course_date = UNKNOWN;
				
				/*
				for ( WebElement web_elem : menu_list_elements ) {
					String web_elem_html = web_elem.getAttribute("innerHTML");
					if (checkMatch(web_elem_html, long_month, day)) {
						WebElement link_element = web_elem.findElement(By.tagName("a"));
						link_to_lesson = link_element.getAttribute("href");
						course_date = template_filename ; 	// Just borrow the Month_day
						break;
					}
				}
				*/
//				if ( link_to_lesson != null) {
//					driver.navigate().to(link_to_lesson);	// Should I click the link instead ?
//				} else { // perhaps today is not on the menu roster ... try a few days back (in case it's Monday) and tomorrow
			boolean found_it = false;
			for (int day_idx = -3; day_idx <= 1; day_idx++) {
				Long day_seconds = curTime + (day_idx * SECONDS_PER_DAY);
				Date targetDate = new Date(day_seconds * 1000);
				String targetDateToStr = formatter.format(targetDate);
				String targetDateToWeekdayStr = formatter_weekday.format(targetDate);	// Needed for courses like Chemistry
				short_month = (new SimpleDateFormat("MMM")).format(targetDate);
				long_month = (new SimpleDateFormat("MMMM")).format(targetDate);
				String month_format = (course == THREE_D ? short_month : long_month);
				day = (new SimpleDateFormat("dd")).format(targetDate);
				found_it = false;
				boolean navigate_back = true;
				for ( WebElement web_elem : menu_list_elements ) {
					String web_elem_html = web_elem.getAttribute("innerHTML");
					WebElement link_element = null;
					// There's teachers that use long and short month formats, so try to match on both of those
					if (checkMatch(web_elem_html, long_month, short_month, day)) {
						course_date = formatter2.format(targetDate);
						try {
							link_element = web_elem.findElement(By.tagName("a"));
							link_to_lesson = link_element.getAttribute("href");
						} catch( org.openqa.selenium.NoSuchElementException nsee) {
							// Perhaps this page is already on the chosen day (sometimes happens if it's the first day of the quarter)
							if ( elementHasClass(web_elem, "selected" )) {	// Ref: https://stackoverflow.com/questions/32713009/how-to-check-if-element-contains-specific-class-attribute
								link_to_lesson = driver.getCurrentUrl();	// Just link to itself to fake out down below
								found_it = true;
								navigate_back = false;
								break;
							} else {
								System.err.println("WARN: No anchor link @ " + course + ": " + month_format + '/' + day + " (" + web_elem_html + ')' );
								continue;
							}
						}
						found_it = true;
						break;
					}
				}
					
				if ( found_it == true && link_to_lesson != null) {
					driver.navigate().to(link_to_lesson);	// Should I click the link instead ?
				} else {
					if ( day_idx > 1) {	// Never reached
						System.err.println("Can't find appropriate " + course + " menu link for previous three days, today and tomorrow - perhaps there's nothing to show ?");
					}
					continue;
				}
//				}
				// Collect up the Homework (section ) info and do:
				// a) Dump HTML to the blank slate page, or (better yet
				// b) Mark up the Google shared spreadsheet (see GoogleSpreadsheetAPI package)
				/*
				 * <h3>&nbsp;<b>Homework:&nbsp;</b></h3>
				 * <ul>
						<li>2.3&nbsp; #18-45m3, 46, 47, 54-60m3</li>
						<li><span style="color: #ff0000;">Study for 2.1-2.3 Quiz next class</span></li>
					</ul>
					// Tom wuz here
					OR
					
					<h3><span style="font-family: 'times new roman', times, serif;">Homework:</span></h3>
					<ol>
						<li><span style="font-family: 'times new roman', times, serif;"><span style="font-family: 'times new roman', times, serif;"><strong><span style="font-size: large;">To help you prepare for the test you need to do:&nbsp;</span></strong></span></span>
						<p><span style="font-family: 'times new roman', times, serif; font-size: large;"><strong>- Dimensional analysis worksheet</strong></span></p>
						<span style="font-family: 'times new roman', times, serif; font-size: large;"><strong></strong></span>
						<p><span style="font-family: 'times new roman', times, serif; font-size: large;"><strong>- Significant figures worksheet</strong></span></p>
						<span style="font-family: 'times new roman', times, serif;"><strong><span style="font-size: large;"></span></strong></span></li>
						<li><span style="font-family: 'times new roman', times, serif;"><strong><span style="font-size: large;">Quiz on chapters 1-2</span></strong></span></li>
						<li><span style="font-family: 'times new roman', times, serif;"><strong><span style="font-size: large;">Chapter 1-2 test 09/13</span></strong></span></li>
					</ol>
				 */
				// Soooo - it turns out that some homework sections are <h3> bounded while others are <p> bounded, so gotta check both
				String [] tag_enclosures = new String [] {"h3", "h4", "p"};	// Courses like Mandarin, Chem & Algebra appear to use <p> now (sheesh)
				for ( String tag_name : tag_enclosures) {
					List <WebElement> enclosure_tag_elements = driver.findElements(By.tagName(tag_name));
					WebElement preface = null;	// Start each course's day item with no indents, which are used to highlight a homework preface (i.e. history below)
					Boolean found_course_enclosure = false;
					for ( WebElement web_elem : enclosure_tag_elements) {
						String web_elem_html = web_elem.getAttribute("innerHTML");
						if (web_elem_html.toLowerCase().contains(HOMEWORK)) {
							found_course_enclosure = true;
							// The next sibling tag contains the homework items
							WebElement homework_list = web_elem.findElement(By.xpath("following-sibling::*[1]"));	// <ul> or <ol>
							List <WebElement> homework_items = homework_list.findElements(By.tagName("li"));
							if ( homework_items.size() > 0)
								System.out.println((course_date == UNKNOWN ? template_filename : course_date) + " Homework for " + course + ":");
							else {	// Uh-oh - perhaps the homework items are outside of (or sibling to) the homework header (teachers are allowed this formatting transgression - ugh)
								List <WebElement> probable_homework_items = new ArrayList<WebElement>();
								// Walk backwards to the homework header's ul and then walk down its siblings (UGH - later)
								
								// Observed case are successive ul's after the HOMEWORK header (some of which ae empty (so check for that)
								try {
									WebElement possible_homework_list = web_elem;
									while (( possible_homework_list = possible_homework_list.findElement(By.xpath("following-sibling::*"))) != null) {	// Could be any tag
										// WebElement parent = (WebElement) ((JavascriptExecutor) driver).executeScript( "return arguments[0].parentNode;", web_elem);
										if (possible_homework_list.getTagName().equals("ul")) {
											try {
												// TODO: Count the # of ul's and prepend a 4-space indent for each ul
												// TODO2: Count the # of homework items and attempt to somehow reduce the # to mitigate Google API quotas (globbing?)
												homework_items = possible_homework_list.findElements(By.tagName("li"));
												probable_homework_items.addAll(homework_items);
											} catch (org.openqa.selenium.NoSuchElementException nsee) {
												// Weird to have a <ul> not contain <li>'s, but it happens ...
												System.out.println("False homework list - contents: " + possible_homework_list.getAttribute("innerHTML"));
											}
										} else if (possible_homework_list.getTagName().equals("p")) {	// Some teachers really messed up with creating an non-list (oh well)
											try {
												probable_homework_items.add(possible_homework_list);	// TODO - LDH - unsure if this works or has side effects
											} catch (org.openqa.selenium.NoSuchElementException nsee) {
												// Weird to have a <ul> not contain <li>'s, but it happens ...
												System.out.println("False homework paragraph - contents: " + possible_homework_list.getAttribute("innerHTML"));
											}
										} else {
											System.out.println("Homework section list - contents: " + possible_homework_list.getAttribute("innerHTML"));
											continue;	// next sibling (hopefully ul)
										}
									}
								} catch (org.openqa.selenium.NoSuchElementException nsee) {
									// End of the line - no more siblings
								}
								if ( probable_homework_items.size() == 0) {
									System.out.println(" Homework for " + course + " can't be located - sorry");	// TODO: Maybe add a homework row in the worksheet saying this
									continue;
								} else
									homework_items = probable_homework_items;
							}
							int item_number = 1;
							for (WebElement homework_item : homework_items) {
								String web_elem_homework = homework_item.getAttribute("innerHTML");
								try {
									if((preface = homework_item.findElement(By.tagName("li"))) != null) {
	//									if ( ! course.contains(HISTORY))	// This might be a preface to a bunch of enumerated items below it
											continue;	// Pass over the nested <ul><li>...<ul><li> nonsense found in courses like history and let it go through the parsing process below.
	//									web_elem_homework = preface.getAttribute("innerHTML");
									} else {
									WebElement possible_inner_span_element = homework_item.findElement(By.tagName("span"));
			//						  if (possible_inner_span_element != null ) {	// Strip it off (it's used for optional color decoration)
										web_elem_homework = possible_inner_span_element.getAttribute("innerHTML");
										// Strip off the <strong> decoration if present
										WebElement possible_stronged_span_element = null;
										if ( (possible_stronged_span_element = possible_inner_span_element.findElement(By.tagName("strong"))) != null) {
											// There is also a possible <span><strong><span> - EGADS
											WebElement possible_spanned_stronged_span_element = null;
											if ( (possible_spanned_stronged_span_element = possible_stronged_span_element.findElement(By.tagName("span"))) != null) {
												web_elem_homework = possible_spanned_stronged_span_element.getAttribute("innerHTML");
											} else
												web_elem_homework = possible_stronged_span_element.getAttribute("innerHTML");
										}
			//						  }
									}
								} catch (org.openqa.selenium.NoSuchElementException nsee) {
									// OK - so it doesn't have a inner <span> - fine ...
								}
								// TODO: Filter out all the &nbsp; 's and 8-bit unicode characters (discovered in algebra [highly suspect copy/paste from somewhere else by teacher  ] - ugh)
								if (web_elem_homework.length() > 0 && web_elem_homework.contains("</")) {	// Previous selective filtering may not have purged all the HTML tags, so let's do that now 
									web_elem_homework = web_elem_homework.replaceAll("\\<.*?>","");	// Ref: https://stackoverflow.com/questions/240546/remove-html-tags-from-a-string
								}
								if (web_elem_homework.length() > 0 && web_elem_homework.contains(",")) {	// Gotta replace those commas before it's dumped into the CSV file
									web_elem_homework = web_elem_homework.replaceAll(",", ";");	// .replace didn't replace all
								}
								if (web_elem_homework.length() > 0 && web_elem_homework.contains("\"")) {	// Gotta replace those commas before it's dumped into the CSV file
									web_elem_homework = web_elem_homework.replaceAll("\"", "'");	// .replace didn't replace all
								}
								if (web_elem_homework.length() > 0 && web_elem_homework.contains("&nbsp;")) {	// Gotta replace those double quotes before it's dumped into the CSV file
									web_elem_homework = web_elem_homework.replaceAll("&nbsp;", " ");	// .replace didn't replace all
								}
								web_elem_homework = web_elem_homework.trim();
								web_elem_homework = unicode_strip(web_elem_homework);	// rare, but it is happening (EGADS)
								// See if this homework was previously 'seen'
								Integer home_work_hash = (course + '_' + web_elem_homework).hashCode();
								if ( homeworkLookup.containsKey(home_work_hash)) {
									System.out.println("Duplicate " + course + " homework found (" + web_elem_homework + ')');
									continue;
								}
								homeworkLookup.put(home_work_hash, web_elem_homework);
								if ( call_python == true) {
									try {
										String indent = (preface != null & item_number > 1 ? "    " : "");
										String worksheet_line = new String(course + "," + (course_date == UNKNOWN ? template_filename : course_date) + "," + indent + web_elem_homework + "," +   "HW Due Date" + "," + "," + "Not submitted");
										spreadsheet_fos.write(worksheet_line.getBytes());
							    		/* Start spreadsheet updater support. Because JAVA can only execute a DOS system command, have it
							    		 * immediately invoke a bash script that invokes the fill_spreadsheet.py Python program (residing in another Git project, aka Google_Spreadsheet_updater)
							    		 * The bash adapter script has to do some input field separator magic (crickey) to have the Pyhton script ingest all arguments correctly.
							    		 * TODO: 1) Figure out the OS and adjust the command accordingly
							    		 * 		2) Add a flag check here for command execution debugging
							    		 */
										String python_command = "python " + spreadsheet_updater_loc + "/fill_spreadsheet.py " + config_properties.getSpreadsheet_name() + " " + google_worksheetname + " " + worksheet_line;
										// String bin_bash = "C:\\Applications\\Git\\bin\\bash.exe ";
										String bin_bash = config_properties.getShell();
										// Run bash adapter and directly call Python:
										String python_adapter_command = bin_bash + " ./runSpreadsheetUpdater.sh python \"" + spreadsheet_updater_loc + "/fill_spreadsheet.py\" \"" + config_properties.getSpreadsheet_name() + "\" \"" + google_worksheetname + "\" \"" + worksheet_line + "\"";
										// Have runSprdShtUpdater invoke getopts
										python_adapter_command = bin_bash + " ./runSpreadsheetUpdater.sh python \"" + getopts_loc + "\" \"" + config_properties.getSpreadsheet_name() + "\" \"" + google_worksheetname + "\" \"" + worksheet_line + "\"";
						    			System.out.println(">>> " + python_adapter_command);
						    			Process p = Runtime.getRuntime().exec(python_adapter_command);
						    			p.waitFor();
										/* TODO - maybe add this handler later
										 * catch (InterruptedException e) {	// p.waitFor
												// TODO Auto-generated catch block
												e.printStackTrace();
												System.exit(1);
											}
										*/
						    			BufferedReader stdInput = new BufferedReader(new 
						    	                 InputStreamReader(p.getInputStream()));
						    			String s;
						                while ((s = stdInput.readLine()) != null) {
						                    System.out.println(">>> " + s);	// Show the output of the cmd line executable
						                }
									} catch (IOException e) {
										System.err.println("ERROR: IOException");
										e.printStackTrace();
										System.exit(1);
									} catch (InterruptedException e) {
										System.err.println("ERROR: InterruptedException");
										e.printStackTrace();
									}
								}
					            /* */
								System.out.println(item_number + ") " + web_elem_homework);
								item_number++;
							}	// for homework items
							break;
						}	// fi: homework(s) for course on day found
					}	// for homework page sections
					if ( found_course_enclosure == true)
						break;
				}	// for tag_enclosures
				if ( navigate_back == true)
					driver.navigate().back();	// Gotta get back to the selectable day(s) panel page
				else
					navigate_back = true;	// special case - flip it from false back to true for the next course menu link
				aside_tag_elements = driver.findElement(By.tagName("aside"));
				menu_list_elements = aside_tag_elements.findElements(By.tagName("li"));	// Each menu selection is an <li>
			}	// for previous days and tomorrow
	    }	// for courses specified	

		// .. and on to the last ...
		// Time to navigate to today's BIBLE lesson
//		driver.navigate().to(bible);		// bible
		
		/*
		 *  Major#1) After navigating to target, grab selected HTML content (for later HTML-tokenizing)
		 */
		// The days selection menu found here is encapsulated within an <aside> tag. An <ul><li> is used for each A/B day
		

	}

	public static String get_html_content( WebElement webelement, String web_element_id) {
		return null;	// https://stackoverflow.com/questions/32234205/how-to-get-html-code-of-a-webelement-in-selenium
	}
	
	// Search for 'java html tokenizer':
	// class StringTokenizer (used it before)
	// class HTMLTokenizer:
	// http://www.cs.princeton.edu/courses/archive/spr06/cos444/assignments/a1/sources/HTMLTokenizer.java
	// https://nlp.stanford.edu/nlp/javadoc/javanlp/edu/stanford/nlp/process/Tokenizer.html
	
	// Opens a new tab under Chrome browser and goes to it, with the intention that its contents will be read or modified.
	// Refs: https://stackoverflow.com/questions/34829329/how-to-open-a-link-in-new-tab-chrome-using-selenium-webdriver
	// 		https://sqa.stackexchange.com/questions/2921/webdriver-can-i-inject-a-jquery-script-for-a-page-that-isnt-using-jquery/3453#3453
	public static WebDriver openNgotoNewTab (WebDriver driver, String goto_url) {
		driver.findElement(By.cssSelector("body")).sendKeys(Keys.CONTROL +"t");
		
		ArrayList<String> tabs = new ArrayList<String> (driver.getWindowHandles());	// TODO: perhaps make this sharable by other methods (otherwise, can't get back to other tabs)
	    driver.switchTo().window(tabs.get(1)); //switches to new tab
	    driver.get(goto_url);
		
		// Use JS to do some client-side changes to this page
		// Refs: https://stackoverflow.com/questions/31911293/run-javascript-file-with-selenium-chrome-driver
		// 		
		JavascriptExecutor js = (JavascriptExecutor) driver;
		if (goto_url.contains("oogle") ) {	// TODO: Will need to 'teach' the page to load JQuery (see https://sqa.stackexchange.com/questions/2921/webdriver-can-i-inject-a-jquery-script-for-a-page-that-isnt-using-jquery/3453#3453 )
			js.executeScript(
		            "document.getElementById('lst-ib').value = 'bada-bing'; " 
		        );
		}
		
		return null;
	}
	
	public static boolean loadJQuery(WebDriver driver) {
		JQueryLoader jquery_loader = new JQueryLoader((JavascriptExecutor) driver);
		
		return jquery_loader.loadJQuery();
	}
	
	public static void closeTab(WebDriver driver) {
		driver.close();
	}
	
	/*
	 *  Misc functionality
	 */
	
	// Do long month search first : "MMMMM XX/", followed by the harder B dates
	// such as "MMMM X" or "MMMM XX"
	// and then do the (rarer) short month searches
	// Requires: at least a two digit target_day_str (even though some courses have single digit days if < 10) - stripping down to a single digit is done here
	static boolean checkMatch(String web_elem_html, String long_month_str, String short_month_str, String target_day_str) {
		byte[] date_bytes = target_day_str.getBytes();
//		byte[] lsb_byte = new byte[date_bytes[1]];	// BUG - this'll create an array of bytes corresponding to the decimal value of the character
//		String single_char_day = (date_bytes[0] == '0' ? new String(lsb_byte) : target_day_str );	// Resulting in a long array of null characters
		byte lsb_byte = date_bytes[1];	// TODO: possibly change this to use SimpleDateFromat - let it do the nasty work
		String single_char_day = "0";
		try {
			single_char_day = (date_bytes[0] == '0' ? new String(new byte[]{ lsb_byte }, "US-ASCII") : target_day_str );	// StandardCharsets.US_ASCII better than specifying "US-ASCII"
		} catch (UnsupportedEncodingException uee) {
			System.err.println("checkMatch ERROR on single_char_day - aborting");
			System.exit(1);
		}
		if (single_char_day.equals("0")) {
			System.err.println("checkMatch WARN: single_char_day set to 0 (no such day #) for " + short_month_str + " " + target_day_str);
		}
		for ( int i=0; i < 2; i++) {
// TODO - BUG - it's matching October 5 for October 15
//			single_char_day = target_day_str.substring(1);
			String month_str = (i == 0 ? long_month_str : short_month_str);
			if (web_elem_html.contains(month_str + " " + single_char_day + "/") ||
				web_elem_html.contains(month_str) && web_elem_html.contains("/" + single_char_day) ||
						web_elem_html.contains(month_str + " " + target_day_str + "/") ||
						web_elem_html.contains(month_str) && web_elem_html.contains("/" + target_day_str) ||
							web_elem_html.contains(month_str) && web_elem_html.contains(" " + single_char_day) ||
							web_elem_html.contains(month_str) && web_elem_html.contains(" " + target_day_str))
				return true;
		}
		return false;
	}
	
	// Create requested file's output stream. Add the 1st row caption if not present
	static FileOutputStream createCSV_OutputStream(String filename, ConfigProperties config_properties) {

	     File file = null;
	     FileOutputStream  spreadsheet_fos = null;
	     boolean has_caption = true;
	     
    	try {

  	      file = new File(".\\" + filename + ".csv");

  	      if (file.createNewFile()) {
  		     has_caption = false;
  	        System.out.println(filename + " spreadsheet created with caption");
  	      } else {
  	    	  // Check to see if the first row already has the header (which means it might have row data if run already)
  	    	FileInputStream fip = new FileInputStream(file);
  	    	String any_caption = " (may already have previous worksheet data)";
  	    	try {
  	    		String first_line = (new Scanner(fip)).nextLine();
  	    	} catch (NoSuchElementException nsee) {
  	  		     has_caption = false;
  	    		any_caption = " (caption row added)";
  	    	}
  	    	fip.close();
  	        System.out.println("Reusing " + filename + " spreadsheet" + any_caption);
  	      }
  	      spreadsheet_fos = new FileOutputStream(file, true);	// append multiple lines to the file
      	} catch (IOException e) {	// FileNotFoundException thrown when another process such as Excel has it open
  	      e.printStackTrace();
      	}
    	if ( has_caption == false) {
    		try {
    			String spreadsheet_caption = config_properties.getWorksheet_caption();
    			String spreadsheet_updater_loc = config_properties.getFill_spreadsheet_location();
    			spreadsheet_caption = (spreadsheet_caption.length() > 10 ? spreadsheet_caption : new String(WORKSHEET_CAPTION)) + "\n";
    			spreadsheet_fos.write(spreadsheet_caption.getBytes());
    		} catch (IOException e) {
    			System.err.println("Unable to write to ");
    			e.printStackTrace();
    		}
    	} else {	// Advance spreadsheet_fos to the end
    		
    	}
    	return spreadsheet_fos;
	}	// createCSV_OutputStream
	
	static public boolean elementHasClass(WebElement element, String class_name) {
	    return element.getAttribute("class").contains(class_name);
	}
	
	// Something is throwing in some 8-bit characters, so strip them down to a 7 bit one so that
	// it's human and machine-readable
	static String unicode_strip(String web_elem_homework_string) {
		
		return web_elem_homework_string;
	}
}

/*
 * Other possibly helpful links:
 * Wait for page load in Selenium:
 * https://stackoverflow.com/questions/5868439/wait-for-page-load-in-selenium
 * 
 */
