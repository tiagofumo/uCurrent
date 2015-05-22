package uCurrent;

import java.io.Serializable;

import org.jfree.data.time.Millisecond;

public class CurrentValue implements Serializable{
	public Millisecond time;
	public int value;
	
	public CurrentValue(Millisecond t, int v){
		time = t;
		value = v;
	}	
}