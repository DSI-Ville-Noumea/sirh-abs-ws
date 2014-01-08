package nc.noumea.mairie.abs.service.impl;

import java.util.Arrays;
import java.util.Date;

import nc.noumea.mairie.abs.domain.AgentRecupCount;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeRecup;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

import org.springframework.stereotype.Service;

@Service("AbsRecuperationDataConsistencyRulesImpl")
public class AbsRecuperationDataConsistencyRulesImpl extends AbstractAbsenceDataConsistencyRules {
	
	/**
	 * Processes the data consistency of a set of Pointages being input by a
	 * user. It will check the different business rules in order to make sure
	 * they're consistent
	 */
	@Override
	public void processDataConsistencyDemande(ReturnMessageDto srm, Integer idAgent, Demande demande,
			Date dateLundi) {
		checkEtatsDemandeAcceptes(srm, demande, Arrays.asList(RefEtatEnum.PROVISOIRE, RefEtatEnum.SAISIE));
		checkDepassementDroitsAcquis(srm, demande);
		
		super.processDataConsistencyDemande(srm, idAgent, demande, dateLundi);
	}
	
	protected ReturnMessageDto checkDepassementDroitsAcquis(ReturnMessageDto srm, Demande demande) {

		// on recupere le solde de l agent
		AgentRecupCount soldeRecup = counterRepository.getAgentCounter(AgentRecupCount.class, demande.getIdAgent());

		Integer sommeDemandeEnCours = recuperationRepository.getSommeDureeDemandeRecupEnCoursSaisieouVisee(
				demande.getIdAgent(), demande.getIdDemande());
		
		if (null == soldeRecup ||
				soldeRecup.getTotalMinutes() - sommeDemandeEnCours - ((DemandeRecup)demande).getDuree() < 0) {
			logger.warn(String.format(DEPASSEMENT_DROITS_ACQUIS_MSG, demande.getIdDemande()));
			srm.getErrors().add(String.format(DEPASSEMENT_DROITS_ACQUIS_MSG, demande.getIdDemande()));
		}

		return srm;
	}
}
