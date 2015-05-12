package com.example.stolenalarm;

public class Password {

	private String activate;
	private String diactivate;
	
	/**
	 * Setters
	 * @param pas
	 */
	public void setActivatePassword(String pas){ this.activate = pas; }
	public void setDiactivatePassword(String pas){ this.diactivate = pas; }
	
	/**
	 * Getters
	 * @return
	 */
	public String getActivatePassword(){ return this.activate; }
	public String getDiactivatePassword(){ return this.diactivate; }
	
	public String toString(){ return "Activate:"+ activate +", Diactivate:"+ diactivate; }
}
