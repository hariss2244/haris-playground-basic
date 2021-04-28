package com.pg.basic;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 */

/**
 * @author haris
 *
 */
public class PatientInfo implements Comparable<PatientInfo>{ 
	protected String birthDate;
	protected String lastName;
	protected String firstName;
	protected String middleName;
	protected String fullName;
	
	
	/**
	 * @return the birthDate
	 */
	public String getBirthDate() {
		return birthDate;
	}
	/**
	 * @param birthDate the birthDate to set
	 */
	public void setBirthDate(Date birthDate) {
		String strDate = "";
		if(birthDate!= null) {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
			strDate = dateFormat.format(birthDate);
		}
		this.birthDate = strDate;
	}
	/**
	 * @return the fullName
	 */
	public String getFullName() {
		return fullName;
	}
	/**
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}
	/**
	 * @param lastName the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	/**
	 * @return the firstName
	 */
	public String getFirstName() {
		return firstName;
	}
	/**
	 * @param firstName the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	/**
	 * @return the middleName
	 */
	public String getMiddleName() {
		return middleName;
	}
	/**
	 * @param middleName the middleName to set
	 */
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}
	
	/**
	 * @param fullName the fullName to set
	 */
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	@Override
	public int compareTo(PatientInfo processedPatient) {
		// TODO Auto-generated method stub
		if (processedPatient!= null) {
			return this.getFirstName().compareToIgnoreCase(processedPatient.getFirstName());
		} else {
			return this.getFirstName().compareToIgnoreCase("");
		}
			
	}
	@Override
	public String toString() {
		return "[FirstName: "+this.getFirstName()+", LastName: "+this.getLastName()+
				", BirthDate: "+this.getBirthDate()+
				"]";
		
	}

}
