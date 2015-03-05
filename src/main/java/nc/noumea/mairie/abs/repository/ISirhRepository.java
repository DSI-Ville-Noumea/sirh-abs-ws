package nc.noumea.mairie.abs.repository;

import java.util.Date;

import nc.noumea.mairie.domain.SpSold;
import nc.noumea.mairie.domain.SpSorc;
import nc.noumea.mairie.domain.Spadmn;
import nc.noumea.mairie.domain.Spcarr;
import nc.noumea.mairie.domain.Spcc;
import nc.noumea.mairie.domain.Spmatr;

public interface ISirhRepository {

	SpSold getSpsold(Integer idAgent);

	void persistEntity(Object obj);

	Spadmn getAgentCurrentPosition(Integer nomatr, Date asOfDate);

	Spcarr getAgentCurrentCarriere(Integer nomatr, Date asOfDate);

	Spmatr findSpmatrForAgent(Integer nomatr);

	void removeEntity(Object obj);

	Spcc getSpcc(Integer nomatr, Date asOfDate, Integer code);

	SpSorc getSpsorc(Integer idAgent);
}
