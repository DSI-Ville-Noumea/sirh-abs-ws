package nc.noumea.mairie.abs.repository;

import java.util.Date;
import java.util.List;

import javax.persistence.FlushModeType;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeCongesAnnuels;

public interface IDemandeRepository {

	void persistEntity(Object obj);

	<T> T getEntity(Class<T> Tclass, Object Id);

	void clear();

	void flush();

	void setFlushMode(FlushModeType flushMode);

	List<Demande> listeDemandesAgent(Integer idAgentConnecte, Integer idAgentConcerne, Date fromDate, Date toDate,
			Integer idRefType, Integer idRefGroupeAbsence);

	void removeEntity(Object obj);

	List<Integer> getListViseursDemandesSaisiesJourDonne(List<Integer> listeTypesGroupe);

	List<Integer> getListApprobateursDemandesSaisiesViseesJourDonne(List<Integer> listeTypesGroupe);

	List<Demande> listeDemandesSIRH(Date fromDate, Date toDate, Integer idRefEtat, Integer idRefType,
			Integer idAgentRecherche, Integer idRefGroupeAbsence);

	List<Demande> listeDemandesSIRHAValider();

	Integer getNombreSamediOffertSurAnnee(DemandeCongesAnnuels demande, Integer year);

	List<Demande> listeDemandesAgentVerification(Integer idAgentConcerne, Date fromDate, Date toDate, Integer idRefType);
}
