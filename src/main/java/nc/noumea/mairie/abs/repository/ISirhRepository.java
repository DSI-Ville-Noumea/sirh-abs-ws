package nc.noumea.mairie.abs.repository;

import java.util.Date;

import nc.noumea.mairie.domain.SpSold;
import nc.noumea.mairie.domain.Spadmn;
import nc.noumea.mairie.domain.Spcarr;

public interface ISirhRepository {

	SpSold getSpsold(Integer idAgent);

	Spadmn getAgentCurrentPosition(Integer nomatr, Date asOfDate);

	Spcarr getAgentCurrentCarriere(Integer nomatr, Date asOfDate);
}
