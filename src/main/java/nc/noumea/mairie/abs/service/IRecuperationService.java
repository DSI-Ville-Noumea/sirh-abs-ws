package nc.noumea.mairie.abs.service;

import java.util.Date;

public interface IRecuperationService {

	int addRecuperationToAgent(Integer idAgent, Date dateMonday, Integer minutes);
}
