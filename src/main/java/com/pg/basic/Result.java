package com.pg.basic;
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
	
	Result(List<PatientInfo> patientsInfo, boolean hasNext) {
		super();
		this.hasNext = hasNext;
		this.patientsInfo = patientsInfo;
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
	
}
