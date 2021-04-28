package com.pg.basic;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;

import ca.uhn.fhir.context.FhirContext;
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
     * 
     * 	
     * @param theArgs
     */
    public static void main(String[] theArgs) {
    	
    	
    	try {
    		// Create a FHIR client
            FhirContext fhirContext = FhirContext.forR4();
            IGenericClient client = fhirContext.newRestfulGenericClient("http://hapi.fhir.org/baseR4");
            client.registerInterceptor(new LoggingInterceptor(false));
            
            List<PatientInfo> consolidatedResults = new ArrayList<PatientInfo>();
                    
            int startRecord =0, pageSize=20;//assigned offsets as per default and it can be changed as per requirement
            String lastName = "SMITH";
            Result res = null;
            int retried = 0;
            String path = "src/main/resources/PatientInfo.txt";
            do {
            	try {
            		res = getPatientDetailsByLastName(client, startRecord, pageSize,lastName);
                	startRecord+=pageSize;
                	consolidatedResults.addAll(res.getPatientInfos());
                	if (retried>0) 
                		logger.info("Retry count reset to 0");
                	retried = 0;// reset the re-try variable if it is success
            	} catch (FhirClientConnectionException e) {
            		if (retried >= RETRY_LIMT) {
            			logger.warn("Retry limit["+RETRY_LIMT+"] exceeded");
            			throw new Exception("Unexpected Error. Try again after sometime");
            		} else {
            			//log the error and re-try
                		logger.error(e.getMessage(), e);
                		logger.info("Connection Error, Search will be re-tried");
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
            logger.info("Total Patients Fetched for lastName="+lastName+": "+consolidatedResults.size());
			/*
			 * logger.info("Results:"); logPatientsDetails(consolidatedResults);
			 */
            Collections.sort(consolidatedResults);
            logPatientsDetails(consolidatedResults,path);
            logger.info("Results are ordered by the patient's first name and logged in a file at :"+path);
    	} catch(Exception e) {
    		logger.error(e.getMessage());
    	}

    }

	/**
	 * To log Patient Details
	 * @param consolidatedResults
	 * @throws IOException 
	 */
	private static void logPatientsDetails(List<PatientInfo> consolidatedResults, String path) 
			throws IOException {
		int index = 0;
		File file = new File(path);
		try (PrintWriter pw = new PrintWriter(file)) {
			for (PatientInfo patientsInfo : consolidatedResults) {
				Files.write(Paths.get(path),patientsInfo.toString().getBytes(),StandardOpenOption.APPEND);
				if(!(index==consolidatedResults.size()-1))
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
    private static Result getPatientDetailsByLastName(IGenericClient client, 
    		int startRecord, int recordsPerPage, String lastName) throws Exception{
    	String url="Patient?family="+lastName+"&_getpagesoffset="+startRecord+"&_count="+recordsPerPage;
        Bundle response = client
                .search().byUrl(url)
                .returnBundle(Bundle.class)
                .execute();
        List<BundleEntryComponent> list = response.getEntry();
        List<PatientInfo> searchResults = new ArrayList<PatientInfo>();
        for (BundleEntryComponent bundleEntryComponent : list) {
        	Patient pat = (Patient)bundleEntryComponent.getResource();
        	PatientInfo modifiedPat = new PatientInfo();
        	modifiedPat.setBirthDate(pat.getBirthDate());
        	List<HumanName> name = pat.getName();
        	for (HumanName humanName : name) {
        		modifiedPat.setLastName((humanName.getFamily()!= null 
        				? humanName.getFamily()
						:""));
        		modifiedPat.setFirstName((humanName.getGiven().size()>0 
        				? humanName.getGiven().get(0).getValue()!=null 
        					? humanName.getGiven().get(0).getValue()
        					:"" 
        				: ""));
        		modifiedPat.setFullName((humanName.getNameAsSingleString()!= null 
        				? humanName.getNameAsSingleString()
						:""));
 			}
        	if(modifiedPat.getLastName().equalsIgnoreCase(lastName))
        		searchResults.add(modifiedPat);
 		}
        Result result = new Result(searchResults, response.getLink(Bundle.LINK_NEXT)!=null);
		return result;
    	
    }

}
