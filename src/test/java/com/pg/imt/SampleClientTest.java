package com.pg.imt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.pg.imt.entities.RequestStopWatchInterceptor;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;

public class SampleClientTest {

	IGenericClient client = null;

	/**
	 * 
	 */
	@Before
	public void RestfulGenericClient() {

		FhirContext fhirContext = FhirContext.forR4();
		client = fhirContext.newRestfulGenericClient("http://hapi.fhir.org/baseR4");
		client.registerInterceptor(new LoggingInterceptor(false));
	}

	/**
	 * 
	 */
	@Test
	public void test() {
		assertNotNull(client);
	}
	@Test
	public void testGenerateUniqueLastNames() {
		int startRecord =0, pageSize=20;//assigned offsets as per default and it can be changed as per requirement
        int uniqueNameMaxCount = 3;
        String path = "src/test/resources/uniqueLastNames.txt";
        try {
			SampleClient.generateUniqueNameList(client, startRecord, pageSize, uniqueNameMaxCount,
					path);
			BufferedReader reader;
	        reader = new BufferedReader(new FileReader(path));
			String lastName = reader.readLine();
			Set<String> names = new HashSet<String>();//to avoid duplicate
			while (lastName != null) {
				
				names.add(lastName);
				lastName = reader.readLine();
			}
			assertEquals(uniqueNameMaxCount, names.size());
		} catch (IOException e) {
			assertNotNull(null);
		} catch (Exception e) {
			assertNotNull(null);
		}
        
        
	}
	
	@Test
	public void testResponseTimes() {
		int startRecord =0, pageSize=20;//assigned offsets as per default and it can be changed as per requirement
        int uniqueNameMaxCount = 3;
        String path = "src/test/resources/uniqueLastNames2.txt";
        String resTimepath = "src/test/resources/avgResponseTime.txt";
        try {
			BufferedReader reader;
            List<Long> avgTimeList = new ArrayList<Long>();
    		for (int i = 1; i <= 3; i++) {
    			RequestStopWatchInterceptor sw = new RequestStopWatchInterceptor();
            	client.registerInterceptor(sw);
            	long totalResponseTime = 0;
            	try {
        			reader = new BufferedReader(new FileReader(path));
        			String lastName = reader.readLine();
        			while (lastName != null) {
        				if(i==3 || i==1) {// since its same search added no cache for loop 1 as well
    						totalResponseTime+=SampleClient.searchPatientByLastName(client, startRecord, pageSize, lastName, true);
    					} else {
    						totalResponseTime+=SampleClient.searchPatientByLastName(client, startRecord, pageSize, lastName, false);
    					}
    					lastName = reader.readLine();
        			}
        			avgTimeList.add(totalResponseTime / uniqueNameMaxCount);
        			
        			reader.close();
        		} catch (IOException e) {
        			assertNotNull(null);
        		}
            }
    		SampleClient.writeFile(avgTimeList, resTimepath);
    		int count = 0;
	        BufferedReader reader1;
	        reader1 = new BufferedReader(new FileReader(resTimepath));
			String lastName = reader1.readLine();
			Map<Integer,Long> map = new HashMap<Integer,Long>();
			while (lastName != null) {
				count++;
				map.put(count, Long.parseLong(lastName));
				lastName = reader1.readLine();
			}
			System.out.println(map.toString());
			assertTrue(map.get(2)<=map.get(1));
			assertTrue(map.get(2)<=map.get(3));
			assertEquals(3, count);
			reader1.close();
		} catch (IOException e) {
			assertNotNull(null);
		} catch (Exception e) {
			assertNotNull(null);
		}
        
        
	}

}
