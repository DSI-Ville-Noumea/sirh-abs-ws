package nc.noumea.mairie.abs.repository;

import java.util.Date;

public interface ICongesExceptionnelsRepository {

	Double countDureeByPeriodeAndTypeDemande(Integer idAgentConcerne,
			Date fromDate, Date toDate, Integer idRefType);
}
