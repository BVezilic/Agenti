package rest;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class ContextApp {

	public static Context context = null;
	
	public ContextApp() {
		super();
	}
	
	public static Context getContext() {
		if (context == null) {
			try {
				context = new InitialContext();				
			} catch (NamingException e) {
				System.out.println("Pukla inicijlazicaija konteksta");
				e.printStackTrace();
			}
			return context;
		} else {
			return context;
		}
	}
	
	public static Object lookup(String type) throws NamingException {
		return getContext().lookup("java:module/" + type);
	}
	
}
