package nc.noumea.mairie.abs.service;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.RefEtat;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

public interface IAbsenceDataConsistencyRules {

	void processDataConsistencyDemande(ReturnMessageDto srm, Integer idAgent, Demande demande, Date dateLundi);
	
	ReturnMessageDto checkEtatsDemandeAcceptes(ReturnMessageDto srm, Demande demande, List<RefEtatEnum> listEtatsAcceptes );
	ReturnMessageDto checkDepassementDroitsAcquis(ReturnMessageDto srm, Demande demande);
	ReturnMessageDto checkDemandeDejaSaisieSurMemePeriode(ReturnMessageDto srm, Demande demande);
	ReturnMessageDto checkAgentInactivity(ReturnMessageDto srm, Integer idAgent, Date dateLundi);
	ReturnMessageDto checkChampMotifPourEtatDonne(ReturnMessageDto srm, Integer etat, String motif);
	ReturnMessageDto verifDemandeExiste(Demande demande, ReturnMessageDto returnDto);
	List<DemandeDto> filtreListDemande(Integer idAgentConnecte, Integer idAgentConcerne, List<Demande> listeSansFiltre, List<RefEtat> etats, Date dateDemande);
}
