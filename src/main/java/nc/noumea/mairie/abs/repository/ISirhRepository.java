package nc.noumea.mairie.abs.repository;

import nc.noumea.mairie.domain.SpSold;
import nc.noumea.mairie.sirh.domain.Agent;

public interface ISirhRepository {

	Agent getAgent(Integer idAgent);

	SpSold getSpsold(Integer idAgent);
}
