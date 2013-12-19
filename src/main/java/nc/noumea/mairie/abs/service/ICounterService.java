package nc.noumea.mairie.abs.service;

import java.util.Date;

public interface ICounterService {

	int addRecuperationToAgent(Integer idAgent, Date dateMonday, Integer minutes);
	
	int addReposCompensateurToAgent(Integer idAgent, Date dateMonday, Integer minutes);
}
