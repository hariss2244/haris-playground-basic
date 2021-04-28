package com.pg.imt;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;

import com.pg.imt.entities.PatientInfo;
import com.pg.imt.entities.RequestStopWatchInterceptor;
import com.pg.imt.entities.Result;

import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.exceptions.FhirClientConnectionException;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;

public class SampleClient {
	//Logger Initialization
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SampleClient.class);
	private static final int RETRY_LIMT = 3;
	
    /**
     * Main Method
     * 	- prints the first and last name, and birth date of each Patient who has the lastName=SMITH
     *  - Sort the output so that the results are ordered by the patient's first name
     * 	- Uses logger.warn to log the status. 
     * 	
     * @param theArgs
     */
    public static void main(String[] theArgs) {
    	
    	
    	try {
    		// Create a FHIR client
            FhirContext fhirContext = FhirContext.forR4();
            IGenericClient client = fhirContext.newRestfulGenericClient("http://hapi.fhir.org/baseR4");
            client.registerInterceptor(new LoggingInterceptor(false));
            
            //List<PatientInfo> consolidatedResults = new ArrayList<PatientInfo>();
                    
            int startRecord =0, pageSize=20;//assigned offsets as per default and it can be changed as per requirement
            int uniqueNameMaxCount = 20;
            String path = "src/main/resources/uniqueLastNames.txt";
            String resTimepath = "src/main/resources/avgResponseTime.txt";
            generateUniqueNameList(client, startRecord, pageSize, uniqueNameMaxCount,
					path);
            BufferedReader reader;
            List<Long> avgTimeList = new ArrayList<Long>();
    		for (int i = 1; i <= 3; i++) {
    			logger.warn("Loop Count: "+i);
            	RequestStopWatchInterceptor sw = new RequestStopWatchInterceptor();
            	client.registerInterceptor(sw);
            	long totalResponseTime = 0;
            	try {
        			reader = new BufferedReader(new FileReader(path));
        			String lastName = reader.readLine();
        			while (lastName != null) {
        				logger.warn("Searching Patients with Lastname: "+lastName);
    					if(i==3) {
    						logger.warn("Search - caching disabled");
    						totalResponseTime+=searchPatientByLastName(client, startRecord, pageSize, lastName, true);
    					} else {
    						totalResponseTime+=searchPatientByLastName(client, startRecord, pageSize, lastName, false);
    					}
    					lastName = reader.readLine();
        			}
        			avgTimeList.add(totalResponseTime / uniqueNameMaxCount);
        			logger.warn("[Total,Average] Time Taken in Loop Count: "+i+ " is ["+totalResponseTime+","+totalResponseTime / uniqueNameMaxCount+"]");
    				
        			reader.close();
        		} catch (IOException e) {
        			logger.error(e.getMessage(),e);
        		}
            }
    		logger.warn("Final Result - Average Response Times [loop 1, loop 2, loop 3] is "+avgTimeList.toString());
    		writeFile(avgTimeList, resTimepath);
    		logger.warn("File created with Average Response Times. Location: "+resTimepath);
    	} catch(Exception e) {
    		logger.error(e.getMessage());
    	}

    }

	/**
	 * @param client
	 * @param startRecord
	 * @param pageSize
	 * @param uniqueNameMaxCount
	 * @param path
	 * @throws Exception
	 * @throws IOException
	 */
	public static void generateUniqueNameList(IGenericClient client, int startRecord, int pageSize,
			int uniqueNameMaxCount, String path) throws Exception, IOException {
		Result res = null;
		Set<String> uniqueLastNames = new HashSet<String>();
        int retried = 0;
        
		do {
			try {
				res = getPatientsUniqueLastNames(client, startRecord, pageSize,uniqueLastNames, uniqueNameMaxCount);
		    	startRecord+=pageSize;
		    	if (retried>0) 
		    		logger.warn("Retry count reset to 0");
		    	retried = 0;// reset the re-try variable if it is success
			} catch (FhirClientConnectionException e) {
				if (retried >= RETRY_LIMT) {
					logger.warn("Retry limit["+RETRY_LIMT+"] exceeded");
					throw new Exception("Unexpected Error. Try again after sometime");
				} else {
					//log the error and re-try
		    		logger.error(e.getMessage(), e);
		    		logger.warn("Connection Error, Search will be re-tried");
		    		retried++;
		    		if(res==null) {//assuming 1st search is failed
		    			res = new Result(null, true);//hard coding it to true so it can re-try the same search
		    		}
				}
			} catch (Exception e) {
				//log the error and re-try
				logger.error(e.getMessage(), e);
				throw new Exception("Unexpected Error. Try again after sometime");
			}
			if(uniqueLastNames.size()<uniqueNameMaxCount)
				logger.warn("Valid Unique Lastnames found: "+uniqueLastNames.size()+". Searching Next Page...");
		}
		while (res != null && res.isHasNext());
		writeFile(uniqueLastNames,uniqueNameMaxCount,path);
		logger.warn("File created with Unique Lastnames. Location: "+path);
	}

	
	/**
	 * To log Unique Names
	 * @param uniqueLastNames
	 * @param max
	 * @param path
	 * @throws IOException
	 */
	public static void writeFile(Set<String> uniqueLastNames, int max, String path) throws IOException {
		int index = 0;
		File file = new File(path);
		try (PrintWriter pw = new PrintWriter(file)) {
			for (String name : uniqueLastNames) {
				if (index>=max) {
					break;
				} else {
					Files.write(Paths.get(path),name.getBytes(),StandardOpenOption.APPEND);
					if(!(index==max-1))
					Files.write(Paths.get(path),System.getProperty("line.separator").getBytes(),StandardOpenOption.APPEND);
				}
				index++;
			}
		}
        catch (IOException e) {
            logger.error(e.getMessage(),e);
            throw e;
        }
		
		
	}
    
	/* To log AverageResponse
	 * @param uniqueLastNames
	 * @param max
	 * @param path
	 * @throws IOException
	 */
	public static void writeFile(List<Long> uniqueLastNames, String path) throws IOException {
		int index = 0;
		File file = new File(path);
		try (PrintWriter pw = new PrintWriter(file)) {
			for (Long name : uniqueLastNames) {
				Files.write(Paths.get(path),name.toString().getBytes(),StandardOpenOption.APPEND);
				if(!(index==uniqueLastNames.size()-1))
				Files.write(Paths.get(path),System.getProperty("line.separator").getBytes(),StandardOpenOption.APPEND);
				index++;
			}
		}
        catch (IOException e) {
            logger.error(e.getMessage(),e);
            throw e;
        }
		
		
	}
    
    /**
     * getPatientsUniqueLastNames - This method will fetch the N Unique patient LastName. This method accepts offsets (start and total records per search)
     * 
     * @param client
     * @param startRecord
     * @param recordsPerPage
     * @param uniqueLastName
     * @return Result
     * @throws Exception
     */
    public static Result getPatientsUniqueLastNames(IGenericClient client, 
    		int startRecord, int recordsPerPage, Set<String> uniqueLastName, int max) throws Exception{
    	String url="Patient?_getpagesoffset="+startRecord+"&_count="+recordsPerPage;
        Bundle response = client
                .search().byUrl(url)
                .returnBundle(Bundle.class)
                .execute();
        List<BundleEntryComponent> list = response.getEntry();
        List<PatientInfo> searchResults = new ArrayList<PatientInfo>();
        for (BundleEntryComponent bundleEntryComponent : list) {
        	Patient pat = (Patient)bundleEntryComponent.getResource();
        	PatientInfo modifiedPat = new PatientInfo();
        	List<HumanName> name = pat.getName();
        	String regex = "^[a-zA-Z]*";
        	Pattern pattern = Pattern.compile(regex);
        	for (HumanName humanName : name) {
        		if (humanName.getFamily()!= null && pattern.matcher(humanName.getFamily()).matches())
        			uniqueLastName.add(humanName.getFamily());
 			}
        	searchResults.add(modifiedPat);
 		}
        Result result = new Result(searchResults, response.getLink(Bundle.LINK_NEXT)!=null &&
        		uniqueLastName.size()<max?true:false);
		return result;
    	
    }
   
    /**
     * getPatientDetailsByLastName - This method will fetch the patient FirstName, LastName and BirthDate based
     * on the given lastName. This method accepts offsets (start and total records per search)
     * 
     * @param client
     * @param startRecord
     * @param recordsPerPage
     * @param lastName
     * @return Result
     * @throws Exception
     */
    public static Result getPatientDetailsByLastName(IGenericClient client, 
    		int startRecord, int recordsPerPage, String lastName, boolean noCache, int page) throws Exception{
    	String url="Patient?family="+lastName+"&_getpagesoffset="+startRecord+"&_count="+recordsPerPage;
    	Bundle response = null;
    	Result result = null;
    	RequestStopWatchInterceptor sw = new RequestStopWatchInterceptor();
    	client.registerInterceptor(sw);
    	if (noCache) {
    		response = client
                    .search().byUrl(url)
                    .cacheControl(new CacheControlDirective().setNoCache(true))
                    .returnBundle(Bundle.class)
                    .execute();
    		logger.warn("Time taken for retriving page "+page+" for Patients with Lastname: "+lastName+" is "+sw.getResponseTime()+"ms");
    		result = new Result(null, response.getLink(Bundle.LINK_NEXT)!=null,sw.getResponseTime());
    	} else {
    		response = client
                    .search().byUrl(url)
                    .returnBundle(Bundle.class)
                    .execute();
    		logger.warn("Time taken for retriving page "+page+" for Patients with Lastname: "+lastName+" is "+sw.getResponseTime()+"ms");
    		result = new Result(null, response.getLink(Bundle.LINK_NEXT)!=null,sw.getResponseTime());
    	}
 		return result;
    	
    }
    
    /* @param client
	 * @param consolidatedResults
	 * @param startRecord
	 * @param pageSize
	 * @param lastName
	 * @param res
	 * @param retried
	 * @throws Exception
	 */
	public static long searchPatientByLastName(IGenericClient client, 
			int startRecord, int pageSize, String lastName, boolean noCache) throws Exception {
		Result res = null;
		int retried = 0;
		long totalResponseTime = 0;
		int page = 0;
		do {
			page++;
			try {
				res = getPatientDetailsByLastName(client, startRecord, pageSize,lastName, noCache, page);
				totalResponseTime = totalResponseTime + res.getResponseTime();
		    	startRecord+=pageSize;
		    	
		    	if (retried>0) 
		    		logger.warn("Retry count reset to 0");
		    	retried = 0;// reset the re-try variable if it is success
		    	
			} catch (FhirClientConnectionException e) {
				if (retried >= RETRY_LIMT) {
					logger.warn("Retry limit["+RETRY_LIMT+"] exceeded");
					throw new Exception("Unexpected Error. Try again after sometime");
				} else {
					//log the error and re-try
		    		logger.error(e.getMessage(), e);
		    		logger.warn("Connection Error, Search will be re-tried");
		    		retried++;
		    		if(res==null) {//assuming 1st search is failed
		    			res = new Result(null, true);//hard coding it to true so it can re-try the same search
		    		}
				}
			} catch (Exception e) {
				//log the error and re-try
				logger.error(e.getMessage(), e);
				throw new Exception("Unexpected Error. Try again after sometime");
			}
		}
		while (res != null && res.isHasNext());
		logger.warn("Total Time taken for searching Patients with Lastname: "+lastName+" in "+page+" page(s) is "+totalResponseTime+"ms");
		return totalResponseTime;
	}

	
}
