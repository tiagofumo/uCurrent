package uCurrent;

import java.io.Serializable;

public class FileSavedObject implements Serializable{	
	int nv;
	int nd;
	
	public FileSavedObject(int d, int v){		
		nv = v;
		nd = d;
	}
	
	public FileSavedObject(){
		
	}
	
	public String toString(){
		return "nd: "+ nd+ ", nv: "+ nv;
	}
}