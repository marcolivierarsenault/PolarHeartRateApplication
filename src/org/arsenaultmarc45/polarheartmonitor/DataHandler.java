package org.arsenaultmarc45.polarheartmonitor;

import java.util.Observable;

public class DataHandler extends Observable{
	private static DataHandler dd = new DataHandler();
	
	int pos=0;
	int val=0;
	int min=0;
	int max=0;
	
	private DataHandler(){
		
	}
	
	public static DataHandler getInstance(){
		return dd;
	}

	public void acqui(int i){
		if (i==254){
			pos=0;
		}
		else if (pos==5){
			val=i;
			if(val<min||min==0)
				min=val;
			else if(val>max)
				max=val;
			setChanged();
			notifyObservers();
		}
		pos++;
	}
	
	public int getLastValue(){
		return val;
	}
	
	public int getMin(){
		return min;
	}
	
	public int getMax(){
		return max;
	}
	
	
}
