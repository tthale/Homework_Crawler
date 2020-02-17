package ChromeBrowser;

/**
 * VCHS_HomeworkCrawler - connect to vcs.learn.com & traverse all of Tom's 
 * homework items and export to XL spreadsheet
 * 
 */

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
public class VCHS_HomeworkCrawler {
	// String constants
	static String HOMEWORK = "homework";
		
	static String login_destination = "https://learn.vcs.net/login/index.php";
	static String username = "Thomas.Hale";
	static String password = "@Vcs7500";
	
	// &pageid corresponds to particular days. So, I'll need to derive these based on today's date
	// &startlastseen=no used to transition to other courses
	static String history_link = "https://learn.vcs.net/mod/lesson/view.php?id=122405";	// Long_month A/B, 2017
	static String mandarin_link = "https://learn.vcs.net/mod/lesson/view.php?id=122577&pageid=238743";	// Long_month A/B, 2017
	static String english_link = "https://learn.vcs.net/mod/lesson/view.php?id=124191&pageid=245743";	// Long_month A/B, 2017 (A&B)
	static String threeD_film_animiation = "https://learn.vcs.net/mod/lesson/view.php?id=102399";	// Mon XX, 2017
	static String algebra2honors = "https://learn.vcs.net/mod/lesson/view.php?id=127472";	// Long_month A/B -OR- Long_month XX (rarer) 
	static String chemistryhonors = "https://learn.vcs.net/mod/lesson/view.php?id=124125";	// Long_day, Long_month XX
	static String bible = "https://learn.vcs.net/mod/lesson/view.php?id=102106";	// Mon A/B, 2017
	
	static String google_jquery_script = "jQuery(function($) { " + " $('#lst-ib').val('bada-BANG'); " + " }); ";
	static String google_js_script = "document.find'#lst-ib').val('bada-bing'); " + " }); ";
	static String blank_slate_url = "https://www.google.com";
	
	static String logout_a_img_link = "";	// STOPPED HERE - what's the link? <a> or <img> or ...
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
        Date curDate = new Date();
		SimpleDateFormat  formatter = new SimpleDateFormat("MMMM dd, yyyy");	// https://examples.javacodegeeks.com/core-java/text/java-simpledateformat-example/
		String DateToStr = formatter.format(curDate);
		System.out.println("Today's date: " + DateToStr);
		// NOTE: Some courses use the short month format ("MMM dd, yyyy")  w/ A&B days like so : Mon A/B, 2017, or even something different
		//	Ref: https://stackoverflow.com/questions/2219139/how-to-parse-month-full-form-string-using-dateformat-in-java
		String short_month = (new SimpleDateFormat("MMM")).format(curDate);
		String long_month = (new SimpleDateFormat("MMMM")).format(curDate);
		String day = (new SimpleDateFormat("dd")).format(curDate);
		String prev_day = Integer.toString(Integer.parseInt(day) - 1);	// TODO: revert to previous month & last day if required
		String prev_prev_day = Integer.toString(Integer.parseInt(day) - 2);
		String tomorrow_day = Integer.toString(Integer.parseInt(day) +1);	// TODO: advance to next month & first day if required
		
		
		System.setProperty("webdriver.chrome.driver", "C:\\Applications\\Selenium\\chromedriver_win32\\chromedriver.exe");
        DesiredCapabilities capabilities = DesiredCapabilities.chrome();	// https://stackoverflow.com/questions/43143014/chrome-is-being-controlled-by-automated-test-software
        ChromeOptions options = new ChromeOptions();
        options.addArguments(Arrays.asList("--no-sandbox","--ignore-certificate-errors","--homepage=about:blank","--no-first-run"));
        options.addArguments("disable-infobars");
        capabilities.setCapability(ChromeOptions.CAPABILITY, options);
		WebDriver driver = new ChromeDriver(capabilities);
		driver.get(login_destination);
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
		
		// Time to navigate to today's ALGEBRA lesson
		driver.navigate().to(algebra2honors);
		WebElement aside_tag_elements = driver.findElement(By.tagName("aside"));
		String aside_tag_content = aside_tag_elements.getAttribute("innerHTML");
		System.out.println("Menu:\n" + aside_tag_content);
		// Iterate through the list to find today's lessons
		List<WebElement> menu_list_elements = aside_tag_elements.findElements(By.tagName("li"));	// Each menu selection is an <li>
		String search_date_str = long_month + " " + day + "/";
		String next_search_date_str = "/" + day;
		WebElement previous_list_elem = null;
		String link_to_lesson = null;
		
		for ( WebElement web_elem : menu_list_elements ) {
			String web_elem_html = web_elem.getAttribute("innerHTML");
			if (checkMatch(web_elem_html, long_month, day)) {
				WebElement link_element = web_elem.findElement(By.tagName("a"));
				link_to_lesson = link_element.getAttribute("href");
				break;
			}
		}
		if ( link_to_lesson != null) {
			driver.navigate().to(link_to_lesson);	// Should I click the link instead ?
		} else { // perhaps today is not on the menu roster ... try a couple days back (in case it's Sunday)
			for ( WebElement web_elem : menu_list_elements ) {
				String web_elem_html = web_elem.getAttribute("innerHTML");
				if (checkMatch(web_elem_html, long_month, prev_day) ||
						checkMatch(web_elem_html, long_month, prev_prev_day) ||
							checkMatch(web_elem_html, long_month, tomorrow_day)) {	// 
					WebElement link_element = web_elem.findElement(By.tagName("a"));
					link_to_lesson = link_element.getAttribute("href");
					break;
				}
			}
			if ( link_to_lesson != null) {
				driver.navigate().to(link_to_lesson);	// Should I click the link instead ?
			} else {
				System.err.println("can't find appropriate Algebra menu link for current time - sorry");
				System.exit(1);
			}
		}
		// Collect up the Homework (section ) info and do:
		// a) Dump HTML to the blank slate page, or (better yet
		// b) Mark up the Google shared spreadsheet
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

		List <WebElement> h3_tag_elements = driver.findElements(By.tagName("h3"));
		for ( WebElement web_elem : h3_tag_elements) {
			String web_elem_html = web_elem.getAttribute("innerHTML");
			if (web_elem_html.toLowerCase().contains(HOMEWORK)) {
				// The next sibling tag contains the homework items
				WebElement homework_list = web_elem.findElement(By.xpath("following-sibling::*[1]"));	// <ul> or <ol>
				List <WebElement> homework_items = homework_list.findElements(By.tagName("li"));
				System.out.println("Today's Algebra Homework:");
				int item_number = 1;
				for (WebElement homework_item : homework_items) {
					String web_elem_homework = homework_item.getAttribute("innerHTML");
					try {
						WebElement possible_inner_span_element = homework_item.findElement(By.tagName("span"));
//						if (possible_inner_span_element != null ) {	// Strip it off (it's used for optional color decoration)
							web_elem_homework = possible_inner_span_element.getAttribute("innerHTML");
//						}
					} catch (org.openqa.selenium.NoSuchElementException nsee) {
						// OK - so it doesn't have a inner <span> - fine ...
					}
					System.out.println(item_number + ") " + web_elem_homework);
					item_number++;
				}
				break;
			}
		}

		// .. and on to the last ...
		// Time to navigate to today's ENGLISH lesson
//		driver.navigate().to(bible);		// bible
		
		/*
		 *  Major#1) After navigating to target, grab selected HTML content (for later HTML-tokenizing)
		 */
		// The days selection menu found here is encapsulated within an <aside> tag. An <ul><li> is used for each A/B day
		
		/*
		 * Major#2: Open up another tab w/ a 'blank slate' page and load up the jQuery/JS
		 */
		openNgotoNewTab ( driver, blank_slate_url);	// Use a relatively blank slate page to do some re-painting (no JQuery)

		if ( loadJQuery( driver) == true)
			System.out.println("jQuery loading SUCCESS");
		else
			System.out.println("jQuery loading FAIL");

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
	
	// Do easy search first : "MMMMM XX/", followed by the harder B dates
	// such as "MMMM X" or "MMMM XX"
	static boolean checkMatch(String web_elem_html, String month_str, String target_day_str) {
		if (web_elem_html.contains(month_str + " " + target_day_str + "/") ||
		web_elem_html.contains(month_str) && web_elem_html.contains("/" + target_day_str) ||
		web_elem_html.contains(month_str) && web_elem_html.contains(" " + target_day_str))
			return true;
		return false;
	}
}
