package com.jio.crm.dms.registry;

import java.util.ArrayList;
import java.util.Iterator;

public class Observable implements ObservableIntf {

	private ArrayList<Observer> observerList = new ArrayList<>();

	private static final Observable instance = new Observable();

	private boolean handled = false;

	protected Observable() {

	}

	public static Observable getInstance() {
		return instance;
	}

	public void addObserver(Observer o) {
		this.observerList.add(o);
	}

	public int countObservers() {

		Iterator<Observer> iterator = this.observerList.listIterator();
		int count = 0;
		while (iterator.hasNext())
			count++;
		return count;
	}

	public void deleteObserver(Observer o) {
		if (this.observerList.contains(o))
			this.observerList.remove(o);
	}

	public void deleteObservers() {
		this.observerList.clear();
	}

	public void notifyObservers(String reason, String httpBody, String method, String serverState) {
		if (this.isHandled()) {
			Iterator<Observer> iterator = this.observerList.listIterator();
			while (iterator.hasNext())
				iterator.next().update(reason, httpBody, method, serverState);
		}

	}

	public void notifyObserver(Observer o, String reason, String httpBody, String method, String serverState) {
		if (this.isHandled() && observerList.contains(o)) {

			o.update(reason, httpBody, method, serverState);
		}
	}

	public boolean isHandled() {
		return handled;
	}

	public void setHandled(boolean handled) {
		this.handled = handled;
	}

	public boolean contains(Observer o) {
		if (this.observerList.contains(o)) {
			return true;
		}
		return false;
	}

}
