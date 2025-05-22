package com.jio.crm.dms.registry;

public interface ObservableIntf {
  
    public void addObserver(Observer o); 

    public void deleteObserver(Observer o);

    public void deleteObservers();

    public void notifyObservers(String reason,String httpBody,String method,String serverState);

    public void notifyObserver(Observer o,String reason,String httpBody,String method,String serverState);
    
    public boolean contains(Observer o);

}

