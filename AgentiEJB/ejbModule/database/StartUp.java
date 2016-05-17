package database;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Startup
@Singleton
public class StartUp {

	@EJB
	Database database;
	
	@PostConstruct
	public void startUp(){
		database.onStartup();
	}
}
