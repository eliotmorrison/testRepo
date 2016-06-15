
// High level notes.
// Just supports firefox.  I have only run it on a Mac. 
// Implement to support one test case at a time.   
// Modify TestData for other cases, e.g., 
//  "rolling stones,jefferson airplane,abcd"  => fails expected must match first or second value
//  "rolling stones, jefferson airplane,jefferson airplane" => fails.   Rolling Stones have more references.
//  "rolling stones,  jefferson airplane"   => fails.   Three fields expected.
// Limiation : subject must not include a comma. 



package googletest;


import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import junit.framework.AssertionFailedError;
import junit.framework.TestResult;


import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;


public class GoogleTest {

	
	  private static WebDriver driver;
	  private static String baseUrl;
	  private static String googleSearchBox = "lst-ib";
	  private TestResult result;

	  @Before
	  public void setUp() throws Exception {
		  driver = new FirefoxDriver();
		  baseUrl = "https://www.google.com/";
	      driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);  


	  }
	  
	 // Scrap the screen.   We were able to reach google.   
	 private static int getHits(String subject, WebDriver driver)
	 {
		 				 
		 int hits = -1;  // Initialize to a negative number. 
		 
		 try {
			 			    
			    driver.get(baseUrl + "/?gws_rd=ssl");
			    
			    // If google changes its UI, the element id may still remain unchanged.  If it does change repair by updating var googleSearchBox.
			    driver.findElement(By.id(googleSearchBox)).clear();
			    driver.findElement(By.id(googleSearchBox)).sendKeys(subject);
			    driver.findElement(By.id(googleSearchBox)).sendKeys(Keys.RETURN);
			    String text = driver.findElement(By.id("resultStats")).getText();
			    
			    String [] results = text.split(" ");
			    hits = Integer.parseInt(results[1].replaceAll(",", ""));
			    return hits;
			 
		    	
		}
		catch (Exception e)
		{
		    	System.out.print("Test failed trying to get result stats.   Invalid execution" + e.getMessage());
		    	assertTrue(false);
		    }
		 
		return hits;
				    		 
	 }
	 
	 // Return a string vector with subject 1, subject 2, and expected hit count winner (must be 1 or 2)
	 private static String [] getSubjects()
	 {
		    BufferedReader reader = null;
		    String fields [] = null;
			
			String fullPath = System.getProperty("user.dir") + "/target/classes/TestData";
			
			try 
			{
				reader = new BufferedReader(new FileReader (fullPath));
				String line = reader.readLine();
				fields = line.split(",");
				
				
			}
			

			catch (Exception e)
			{
					System.out.print(e.getMessage());
					System.out.println("Can't execute test.  Data file not found or not well formed");
					Assert.assertFalse(true);
				
			}

			return fields;

			
	 }
	 	    
	 
	  
	  @Test
	  public void testGoogleFrequencies() throws Exception {
		  
	    // Read and parse the input data. 
		String [] subjects = getSubjects();
		
		try {
			Assert.assertTrue(subjects.length == 3);
		}
		catch (AssertionFailedError e)
		{
			result.addFailure((junit.framework.Test) this , e);

		}

		
		String subject1 = subjects[0].trim();
		String subject2 = subjects[1].trim();
		String expectedWinner = subjects[2].trim();
		
		// A few simple data integrity checks 
		try
		{
			Assert.assertTrue(subjects.length == 3);
			Assert.assertTrue(  (subject1.equals(expectedWinner))  ||  (subject2.equals(expectedWinner))  );
			Assert.assertTrue(  !  subject1.equals(subject2 ) );
		}
		catch (AssertionFailedError e)
		{
			 System.out.println("Invalid input data" + e.toString());
			 
			 result.addFailure((junit.framework.Test) this , e);
		}
		
		
		
		
		// Enter the subjects in google search.   Extract hits. 
		int hitsSubject1 = getHits(subject1, driver);
		int hitsSubject2 = getHits(subject2, driver);
		
		// N.B.: In the event of a tie, either expected winner is accepted. 
		System.out.println("subject1/hits1, subject2/hits2, expectedWinnder: " + subject1+'/'+hitsSubject1 + ' ' + subject2+'/'+hitsSubject2 + ' ' + expectedWinner );
		try 
		{
			assertTrue(  ( (hitsSubject1 >= hitsSubject2)   && (expectedWinner.equals(subject1) )   ||  (hitsSubject2 >= hitsSubject1) && (expectedWinner.equals(subject2))  ));
		}
		catch (AssertionFailedError e){
			 System.out.println("Expected winner had fewer hits" + e.toString());
			 result.addFailure((junit.framework.Test) this ,e );
			
		}
			
	  
	  }

	// Print a stack trace for failed tests to show the assertion which failed in context.   Also the test header. 
	public static void main(String[] args)
	{
		
		
		 Result result = null;
		 try {
			 result = JUnitCore.runClasses(GoogleTest.class);
			 if ( result.wasSuccessful())
				 System.out.println("Test passed");
		 }
		 catch (Exception e)
		 {
			 System.out.print("Test failed" + e.getMessage());
		 }

		 
		 for (Failure failure : result.getFailures()) {
			    System.out.println("Test failed: --- " + failure.getTestHeader());
			    
	            System.out.println(failure.getTrace());
		 }
		 
		 driver.quit();

	}
	
	
}
