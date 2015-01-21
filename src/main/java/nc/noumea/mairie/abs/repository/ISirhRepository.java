package nc.noumea.mairie.abs.repository;

import java.util.Date;

import nc.noumea.mairie.domain.Spadmn;
import nc.noumea.mairie.domain.Spcarr;
import nc.noumea.mairie.domain.Spmatr;

public interface ISirhRepository {

	void persistEntity(Object obj);

	Spadmn getAgentCurrentPosition(Integer nomatr, Date asOfDate);

	Spcarr getAgentCurrentCarriere(Integer nomatr, Date asOfDate);

	Spmatr findSpmatrForAgent(Integer nomatr);
}
