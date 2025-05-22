package com.jio.crm.dms.rest.xmlparser;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name="counters")
public class Counters {

	@ElementList(name = "counter", empty = false, required = true, inline = true, type = Counter.class)
	private List<Counter> counter;
	
	public Counters(){
		counter = new ArrayList<>();
	}
	
	public List<Counter> getCounter() {
		return counter;
	}
	
	public void setCounter(List<Counter> cntr){
		this.counter.addAll(cntr);
	}
	
	public void addToCounterList(Counter cntr){
		if(cntr != null)
			this.counter.add(cntr);
	}

	@Override
	public String toString() {
		return "Counters [counter=" + counter + "]";
	}
	
}