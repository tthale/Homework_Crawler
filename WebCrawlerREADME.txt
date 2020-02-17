	VCHS Homework Crawler
	
   The VCHS_HomeworkCrawler project uses Selenium to automate the process of signing in to the
learn.vcs student portal and walks through the student's courses to collect up the current
homework assignments due for each class. The results are dumped to a CSV spreadsheet, outputted on
the standard output, and if elected, deposits the classwork assignments up to a Google
Spreadsheet by communicating externally with Google_Spreadsheet_Updater, a Python program
(which resides up on GitHub, too).

    This project uses the following solution technologies:
1) Selenium ChromeDriver
2) Google Spreadsheet APIs
   (Set up for new Google API project @ https://developers.google.com/sheets/api/quickstart/java)
	Application = other, Name = VCHS_Spreadsheet
	OAuth 2.0 Client ID: 162522261062-pk64ksoen5jae9uaii4qs7cpig9fekv1.apps.googleusercontent.com
	OAuth 2.0 Client secret: 9q64bNFL8lyfIvModcvuQ2zV
   ( Steps to grab the "Google Sheets API Quickstart" project were bypassed )
3) Whatever maven repo libraries are required (manually goes into pom.xml dependencies)
4) Download Google_Spreadsheet_Updater or any other alternative that you have enough time to get it to work.

 in order to :

a) Connect to Learn.VCHS via standard login process, and
b) Use a set of seven quarterly root links from the student portal pointing to a student's seven classes
	(These links must be updated every quarter)
c) Figure out from the Course's date schedule (in upper left corner) today's assignments 
d) Keep track of a student's A/B classes
d) Walk back a few days to fully capture assignments due the next day or so
	(The due date of each assignment is not currently captured)
e) Be aware of weekends, which don't have dates in the assignment schedules

Description of prototype programs:
NOTE: Design of this source code is rather single-file monolithic and as simple as possible. Further
true software development enhancements would be to split the monoliths into fucntionally
separate class files.

VCHS_HomeworkCrawler - 1st iteration opens up a Chrome browser via Selenium libraries and
walks to the class(es) that were uncommented. The idea was to use the JQueryLoader to
load up the VCHS page with Javascript modules that would do the client-side walking. The
JQuery loading was successful, but decided against it for now in favor of continuing along
the Selecnium route. Program hardcoded to a student's classes and credentials.

VCHS_HomeworkCrawler2 - Iterates through all the classes in ALL_COURSES via Selenium libraries and
walks through the classes looking backwards three days and forward one day. Each teacher
appears to encode the date and the hoemwork items a bit differently, so pains have been taken
in the logic to handle all cases discovered. Program hardcoded to a student's classes and credentials.

VCHS_HomeworkCrawler3 - an extension of VCHS_HomeworkCrawler2 that uses Tom's Python-based filler
program that updates a Google spreadsheet and makes use of a config file that contains all
the sensitive credential information.

Other prototype programs:
GoogleSpreadsheet1stTry - Multiple failed attempts to use the Google Drive v4 API to open
up a Google spreadsheet and worksheet combo and append rows to it (output of the homework
crawler). Unfortunately, there appears to be mutliple flavors of credentials for different
parts of the API, so I ran out of time getting this working (will use Tom's Python-based
filler program which uses 'system' APIs from his account - see VCHS_HomeworkCrawler3 above).

runSpreadsheetUpdater.sh:
An 'adapter' bash program that is invoked on the DOS cmd line with arguments corresponding in
the same order as is needed by the fill_spreadsheet.py program. Since Java on Windows can only 
execute a DOS command via the system exec function, this adapter is straight-off called by it
and duly maintains the arg ordering for the execution of the Python program. The magic of the
 adapter is done via the IFS mechanism.

How to test runSpreadsheetUpdater.sh + fill_spreadsheet.py (with getopts.py) combo using CMD:
C:...\VCHS_HomeworkCrawler> C:\Applications\Git2_0\bin\bash.exe ./runSpreadsheetUpdater.sh python 
"C:\Applications\MyEclipse2015CI\Workspaces\Google_Spreadsheet_Updater/getopts.py" 
"Tom_Spreadsheet" "Jan_14" "threeD film animation,January_11,Sketchbook #2,HW Due Date,,Not submitted onc
e more again"
-- Output of getopts.py (each arg should be on a separate line):
CWD=/c/Applications/MyEclipse2015CI/Workspaces/Google_Spreadsheet_Updater
Tom_Spreadsheet
Jan_14
threeD film animation
January_11
Sketchbook #2
HW Due Date


Not submitted once more again

C:\...\VCHS_HomeworkCrawler>

How to test runSpreadsheetUpdater.sh + fill_spreadsheet.py (with getopts.py) combo using bash:
/c/.../VCHS_HomeworkCrawler#  "C:\Applications\Git2_0\bin\bash.exe" ./runSpreadsheetUpdater.sh python "C:\Applications\MyEclipse2015CI\Workspaces\Google_Spreadsheet_Updater\getopts.py" "Tom_Spreadsheet" "Oct_31" "APstats,October_29,Lesson_27_VchsStatsDemo01_Review_CA01_CA02_CA03.java,HW Due Date,,Not submitted"

				APPENDIX
Getting the Google API keys:
https://developers.google.com/docs/api/how-tos/authorizing
(We've also documented an outline of the above in the Google_Spreadsheet_Updater/README.md Git project doc 
(it's also found under the Google_Spreadsheet_Updater MyEclipse project on our local systems) )

https://science-fair.org/students-parents/project-resources/