package nc.noumea.mairie.abs.repository;

import java.util.Date;
import java.util.List;

import javax.persistence.FlushModeType;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.RefEtat;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;

public interface IDemandeRepository {

	void persistEntity(Object obj);

	<T> T getEntity(Class<T> Tclass, Object Id);
	
	void clear();
	
	void flush();
	
	void setFlushMode(FlushModeType flushMode);

	List<Demande> listeDemandesAgent(Integer idAgentConnecte, Integer idAgentConcerne, Date fromDate, Date toDate, Integer idRefType);

	List<RefEtat> findRefEtatNonPris();

	List<RefEtat> findRefEtatEnCours();
	
	void removeEntity(Object obj);
	
	List<RefEtat> findAllRefEtats();
	
	List<Integer> getListViseursDemandesSaisiesJourDonne(Integer typeRecup, Integer typeRC);
	
	List<Integer> getListApprobateursDemandesSaisiesViseesJourDonne(Integer typeRecup, Integer typeRC);

	List<RefTypeAbsence> findAllRefTypeAbsences();
}
