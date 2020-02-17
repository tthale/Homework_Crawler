(This file @ https://drive.google.com/drive/u/0/folders/0B7obCzqNDdg3dE5ZdmZzbXR0elU )

   VCHS_HomeworkCrawler project uses Selenium to automate the process of signing in to the
vcs.learn student portal and walks through the student's courses to collect up the current
homework assignments due for each class. The results are dumped to a CSV spreadsheet, outputted on
the standard output, and if elected, deposits the classwork assignments up to a Google
Spreadsheet by communicating externally with Google_Spreadsheet_Updater, a Python program
(which resides up on GitHub, too). 

   Build/Development notes for this VCHS_HomeworkCrawler  project:
  

Attempts at running via the command line pretty well illustrates the more useful route of creating a jar file:
(Otherwise, lots of .m2 repo jars will have to be specified (& exported to other machines)
 
   ChromeBrowser # java -cp "C:/Applications/MyEclipse2015CI/Workspaces/VCHS_HomeworkCrawler/target/classes;C:/Users/lhale_000/.m2/repository/org/seleniumhq/sele
nium/selenium-chrome-driver/3.5.1/*.jar;C:/Users/lhale_000/.m2/repository/org/seleniumhq/selenium/selenium-server/3.5.1/*.jar" ChromeBrowser.VCHS_HomeworkCraw
ler2
Error: A JNI error has occurred, please check your installation and try again
Exception in thread "main" java.lang.NoClassDefFoundError: org/openqa/selenium/NoSuchElementException
        at java.lang.Class.getDeclaredMethods0(Native Method)
        at java.lang.Class.privateGetDeclaredMethods(Unknown Source)
        at java.lang.Class.privateGetMethodRecursive(Unknown Source)
        at java.lang.Class.getMethod0(Unknown Source)
        at java.lang.Class.getMethod(Unknown Source)
        at sun.launcher.LauncherHelper.validateMainClass(Unknown Source)
        at sun.launcher.LauncherHelper.checkAndLoadMain(Unknown Source)
        
        
Caused by: java.lang.ClassNotFoundException: org.openqa.selenium.NoSuchElementException
        at java.net.URLClassLoader.findClass(Unknown Source)
        at java.lang.ClassLoader.loadClass(Unknown Source)
        at sun.misc.Launcher$AppClassLoader.loadClass(Unknown Source)
        at java.lang.ClassLoader.loadClass(Unknown Source)
        ... 7 more
        
        To create an executable with Eclipse, do
        
        right click VCHS_HomeworkCrawler->export->runnable jar file, extract required libs into generated jar radio button.
        
        to run:
        java -cp VCHS_HomeworkCrawler.jar ChromeBrowser.VCHS_HomeworkCrawler3 all or list of classes
        
        
        
        