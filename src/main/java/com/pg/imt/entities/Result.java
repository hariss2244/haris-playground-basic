package com.pg.imt.entities;
import java.util.List;

/**
 * 
 */

/**
 * @author haris
 *
 */
public class Result {
	protected List<PatientInfo> patientsInfo;
	protected boolean hasNext;
	protected long responseTime;
	
	public Result(List<PatientInfo> patientsInfo, boolean hasNext) {
		super();
		this.hasNext = hasNext;
		this.patientsInfo = patientsInfo;
	}
	public Result(List<PatientInfo> patientsInfo, boolean hasNext, long responseTime) {
		super();
		this.hasNext = hasNext;
		this.patientsInfo = patientsInfo;
		this.responseTime = responseTime;
	}
	
	/**
	 * @return the patientsInfo
	 */
	public List<PatientInfo> getPatientInfos() {
		return patientsInfo;
	}
	/**
	 * @param patientsInfo the patientsInfo to set
	 */
	public void setPatientInfos(List<PatientInfo> patientsInfo) {
		this.patientsInfo = patientsInfo;
	}
	/**
	 * @return the hasNext
	 */
	public boolean isHasNext() {
		return hasNext;
	}
	/**
	 * @param hasNext the hasNext to set
	 */
	public void setHasNext(boolean hasNext) {
		this.hasNext = hasNext;
	}


	/**
	 * @return the patientsInfo
	 */
	public List<PatientInfo> getPatientsInfo() {
		return patientsInfo;
	}


	/**
	 * @param patientsInfo the patientsInfo to set
	 */
	public void setPatientsInfo(List<PatientInfo> patientsInfo) {
		this.patientsInfo = patientsInfo;
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
	
}
