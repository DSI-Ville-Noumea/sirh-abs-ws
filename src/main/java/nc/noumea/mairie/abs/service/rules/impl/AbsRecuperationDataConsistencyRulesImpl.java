package nc.noumea.mairie.abs.service.rules.impl;

import java.util.Arrays;

import nc.noumea.mairie.abs.domain.AgentRecupCount;
import nc.noumea.mairie.abs.domain.AgentRecupCountTemp;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeRecup;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IRecuperationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("AbsRecuperationDataConsistencyRulesImpl")
public class AbsRecuperationDataConsistencyRulesImpl extends AbstractAbsenceDataConsistencyRules {

	@Autowired
	protected IRecuperationRepository recuperationRepository;

	/**
	 * Processes the data consistency of a set of Pointages being input by a
	 * user. It will check the different business rules in order to make sure
	 * they're consistent
	 */
	@Override
	public void processDataConsistencyDemande(ReturnMessageDto srm, Integer idAgent, Demande demande, 
			boolean isProvenanceSIRH) {
		checkEtatsDemandeAcceptes(srm, demande, Arrays.asList(RefEtatEnum.PROVISOIRE, RefEtatEnum.SAISIE));
		checkDepassementDroitsAcquis(srm, demande);

		super.processDataConsistencyDemande(srm, idAgent, demande,  isProvenanceSIRH);
	}

	@Override
	public ReturnMessageDto checkDepassementDroitsAcquis(ReturnMessageDto srm, Demande demande) {

		// on recupere le solde de l agent
		AgentRecupCount soldeRecup = counterRepository.getAgentCounter(AgentRecupCount.class, demande.getIdAgent());
		AgentRecupCountTemp agentRecupCountTemp = counterRepository.getAgentRecupCountTempByIdAgent(demande.getIdAgent());

		Integer soldeRecupTemp = 0;
		if(null != agentRecupCountTemp) {
			soldeRecupTemp = agentRecupCountTemp.getTotalMinutes();
		}
		
		Integer sommeDemandeEnCours = recuperationRepository.getSommeDureeDemandeRecupEnCoursSaisieouVisee(
				demande.getIdAgent(), demande.getIdDemande());

		Integer solde = null == soldeRecup ? 0 : soldeRecup.getTotalMinutes();
		
		if (solde + soldeRecupTemp - sommeDemandeEnCours - ((DemandeRecup) demande).getDuree() < 0) {
			logger.warn(String.format(DEPASSEMENT_DROITS_ACQUIS_MSG, demande.getIdDemande()));
			srm.getErrors().add(String.format(DEPASSEMENT_DROITS_ACQUIS_MSG, demande.getIdDemande()));
		}

		return srm;
	}
}
