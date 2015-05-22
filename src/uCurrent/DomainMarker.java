package uCurrent;

import java.io.Serializable;

public class DomainMarker implements Serializable{
	public int inicio;
	public int fim;
	public String caption;
	
	public DomainMarker(String c, int i, int f){
		inicio = i;
		fim = f;
		caption = c;
	}
		
}