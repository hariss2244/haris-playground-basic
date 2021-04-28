package com.pg.imt.entities;
import java.io.IOException;

import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
import ca.uhn.fhir.util.StopWatch;

/**
 * 
 */

/**
 * @author haris
 *
 */
public class RequestStopWatchInterceptor implements IClientInterceptor {

	private StopWatch stopWatch;
	private long responseTime;
	
	/**
	 * @return the stopWatch
	 */
	public StopWatch getStopWatch() {
		return stopWatch;
	}

	/**
	 * @param stopWatch the stopWatch to set
	 */
	public void setStopWatch(StopWatch stopWatch) {
		this.stopWatch = stopWatch;
	}

	/**
	 * @return the responseTime
	 */
	public long getResponseTime() {
		return responseTime;
	}

	/**
	 * @param responseTime the responseTime to set
	 */
	public void setResponseTime(long responseTime) {
		this.responseTime = responseTime;
	}

	@Override
	public void interceptRequest(IHttpRequest theRequest) {
		stopWatch = new StopWatch();
		
	}

	@Override
	public void interceptResponse(IHttpResponse theResponse) throws IOException {
		responseTime = stopWatch.getMillis();
		
	}

}
