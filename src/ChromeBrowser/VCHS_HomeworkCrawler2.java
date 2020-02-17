package ChromeBrowser;

/**
 * VCHS_HomeworkCrawler - connect to vcs.learn.com & traverse all of Tom's 
 * homework items and export to CSV spreadsheets
 * 
 * Export all to an executable jar and run liek so:
 * java -cp VCHS_HomeworkCrawler.jar ChromeBrowser.VCHS_HomeworkCrawler2 all
 * 
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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

import BrowserJavascript.JQueryLoader;

/* NOTE: This is a Java8 program. Anything less gets java.lang.UnsupportedClassVersionError: org/openqa/selenium/Capabilities : Unsupported major.minor version 52.0 ,
 * Thanks to 1) https://www.youtube.com/watch?v=BtmeQOcdIKI
 *			2) https://sites.google.com/a/chromium.org/chromedriver/extensions
 *			3) https://groups.google.com/forum/#!topic/selenium-users/CL8kdxhgdj0
 *			4) https://sites.google.com/a/chromium.org/chromedriver/capabilities
 *			5) 
 *
 *	Major TODO items:
 *		1) After navigating to target, grab selected HTML content (for later HTML-tokenizing)
 *		2) Open up another tab and dump tabular report to it (maybe use ExecuteScript so JS builds the table content & any otehr interactivity)
 */

/**
 * @author lhale - Mean Green Software
 *
 */
public class VCHS_HomeworkCrawler2 {
	// String constants
	static String HOMEWORK = "homework";
	static String WORKSHEET_CAPTION = "Course Name,Course Date,Homework Item, HW Due Date,Status,Comments,Submission";
		
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
	static HashMap homeworkLookup = new HashMap();	// Each entry encoded with the course name and homework text
	
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
        Date curDate = new Date();
        Long curTime = curDate.getTime() / 1000;	// getTime returns milliseconds from epoch (1970)
        // Unfortunately, each teacher does not standardize on a date format, so...
		SimpleDateFormat  formatter = new SimpleDateFormat("MMMM dd, yyyy");	// https://examples.javacodegeeks.com/core-java/text/java-simpledateformat-example/
		SimpleDateFormat  formatter_weekday = new SimpleDateFormat("EEEE MMMM dd, yyyy");	// https://stackoverflow.com/questions/5121976/is-there-a-date-format-to-display-the-day-of-the-week-in-java
		SimpleDateFormat  formatter2 = new SimpleDateFormat("MMMM_dd");
		String todayDateToStr = formatter.format(curDate);
		String DateToWeekdayStr = formatter_weekday.format(curDate);	// Needed for Chemistry
		String template_filename = formatter2.format(curDate);
	 
	    if(args.length == 0)   // Should have at least 1 argument passed in
	    {
	        System.out.println("Proper Usage is: java VCHS_HomeworkCrawler all or a combination of the following course names:");
	        System.out.println("chemistry algebra history mandarin 3D english bible");
	        System.exit(0);
	    }
	    if ( args[0].contains(ALL))
	    	args = ALL_COURSES;
	    
		System.out.println("Today's date: " + todayDateToStr);
		// NOTE: Some courses use the short month format ("MMM dd, yyyy")  w/ A&B days like so : Mon A/B, 2017, or even something different
		//	Ref: https://stackoverflow.com/questions/2219139/how-to-parse-month-full-form-string-using-dateformat-in-java
		String short_month = (new SimpleDateFormat("MMM")).format(curDate);
		String long_month = (new SimpleDateFormat("MMMM")).format(curDate);
		String day = (new SimpleDateFormat("dd")).format(curDate);
		
		System.setProperty("webdriver.chrome.driver", "C:\\Applications\\Selenium\\chromedriver_win32\\chromedriver.exe");
        DesiredCapabilities capabilities = DesiredCapabilities.chrome();	// https://stackoverflow.com/questions/43143014/chrome-is-being-controlled-by-automated-test-software
        ChromeOptions options = new ChromeOptions();
        options.addArguments(Arrays.asList("--no-sandbox","--ignore-certificate-errors","--homepage=about:blank","--no-first-run"));
        options.addArguments("disable-infobars");
        capabilities.setCapability(ChromeOptions.CAPABILITY, options);
		WebDriver driver = new ChromeDriver(capabilities);	// This opens up 2 new windows (aka browser tabs)
		driver.get(login_destination);
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
		uname.sendKeys(username);
		WebElement pwd = driver.findElement(By.name("password"));
		pwd.sendKeys(password);
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
		FileOutputStream fos = createCSV_OutputStream(template_filename);
		if ( fos == null) {
			System.err.println("Can't create CSV file " + template_filename);
			System.exit(1);
		}
		
		// Courses tested: algebra chemistry history (some A/B dups need to be filtered out), english (redundant A/B day items), mandarin, 3D
	    for (String course : args) {
	        /* // Convert each String arg to char array
	        caMainArg = arg.toCharArray();

	        // Convert each char array to String
	        strMainArg = new String(caMainArg);
	        */
    		System.out.println(course + ':');
	    	if (((String) course).toLowerCase().contains(ALGEBRA)) {
				// Time to navigate to today's ALGEBRA lesson
				driver.navigate().to(algebra2honors);
	    	}
	    	if (((String) course).toLowerCase().contains(CHEMISTRY)) {
				// Time to navigate to today's ALGEBRA lesson
				driver.navigate().to(chemistryhonors);
	    	}
	    	if (((String) course).toLowerCase().contains(HISTORY)) {
				// Time to navigate to today's ALGEBRA lesson
				driver.navigate().to(history_link);
	    	}
	    	if (((String) course).toLowerCase().contains(ENGLISH)) {
				// Time to navigate to today's ALGEBRA lesson
				driver.navigate().to(english_link);
	    	}
	    	if (((String) course).toLowerCase().contains(MANDARIN)) {
				// Time to navigate to today's ALGEBRA lesson
				driver.navigate().to(mandarin_link);
	    	}
	    	if (((String) course).toLowerCase().contains(THREE_D.toLowerCase())) {
				// Time to navigate to today's ALGEBRA lesson
				driver.navigate().to(threeD_film_animiation);
	    	}
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
				String tag_name = (((String) course).toLowerCase().contains(MANDARIN) ? "p" : "h3");	// Mandarin HAS to be different, of course
				List <WebElement> h3_tag_elements = driver.findElements(By.tagName(tag_name));
				WebElement preface = null;	// Start each course's day item with no indents, which are used to highlight a homework preface (i.e. history below)
				for ( WebElement web_elem : h3_tag_elements) {
					String web_elem_html = web_elem.getAttribute("innerHTML");
					if (web_elem_html.toLowerCase().contains(HOMEWORK)) {
						// The next sibling tag contains the homework items
						WebElement homework_list = web_elem.findElement(By.xpath("following-sibling::*[1]"));	// <ul> or <ol>
						List <WebElement> homework_items = homework_list.findElements(By.tagName("li"));
						System.out.println((course_date == UNKNOWN ? template_filename : course_date) + " Homework for " + course + ":");
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
							// TODO: Filter out all the &nbsp; 's
							if (web_elem_homework.length() > 0 && web_elem_homework.contains("</")) {	// Previous selective filtering may not have purged all the HTML tags, so let's do that now 
								web_elem_homework = web_elem_homework.replaceAll("\\<.*?>","");	// Ref: https://stackoverflow.com/questions/240546/remove-html-tags-from-a-string
							}
							if (web_elem_homework.length() > 0 && web_elem_homework.contains(",")) {	// Gotta replace those commas before it's dumped into the CSV file
								web_elem_homework = web_elem_homework.replaceAll(",", ";");	// .replace didn't replace all
							}
							if (web_elem_homework.length() > 0 && web_elem_homework.contains("&nbsp;")) {	// Gotta replace those commas before it's dumped into the CSV file
								web_elem_homework = web_elem_homework.replaceAll("&nbsp;", " ");	// .replace didn't replace all
							}
							web_elem_homework = web_elem_homework.trim();
							// See if this homework was previously 'seen'
							Integer home_work_hash = (course + '_' + web_elem_homework).hashCode();
							if ( homeworkLookup.containsKey(home_work_hash)) {
								System.out.println("Duplicate " + course + " homework found (" + web_elem_homework + ')');
								continue;
							}
							homeworkLookup.put(home_work_hash, web_elem_homework);
							try {
								String indent = (preface != null & item_number > 1 ? "    " : "");
								fos.write((new String("\n" + course + "," + (course_date == UNKNOWN ? template_filename : course_date) + "," + indent + web_elem_homework + "," +   "HW Due Date" + "," + "," + "," + "Not submitted")).getBytes());
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							System.out.println(item_number + ") " + web_elem_homework);
							item_number++;
						}	// for homework items
						break;
					}
				}	// for homework page sections
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
// STOPPED HERE - it's matching October 5 for October 15
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
	static FileOutputStream createCSV_OutputStream(String filename) {

	     File file = null;
	     FileOutputStream  fos = null;
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
  	      fos = new FileOutputStream(file, true);	// append multiple lines to the file
      	} catch (IOException e) {	// FileNotFoundException thrown when another process such as Excel has it open
  	      e.printStackTrace();
      	}
    	if ( has_caption == false) {
    		try {
    			fos.write((new String(WORKSHEET_CAPTION + "\n")).getBytes());
    		} catch (IOException e) {
    			System.err.println("Unable to write to ");
    			e.printStackTrace();
    		}
    	} else {	// Advance fos to the end
    		
    	}
    	return fos;
	}
	
	static public boolean elementHasClass(WebElement element, String class_name) {
	    return element.getAttribute("class").contains(class_name);
	}
}

/*
 * Other possibly helpful links:
 * Wait for page load in Selenium:
 * https://stackoverflow.com/questions/5868439/wait-for-page-load-in-selenium
 * 
 */
