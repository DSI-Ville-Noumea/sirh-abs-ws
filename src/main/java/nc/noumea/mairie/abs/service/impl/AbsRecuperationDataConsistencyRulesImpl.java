package nc.noumea.mairie.abs.service.impl;

import java.util.Date;

import org.springframework.stereotype.Service;

import nc.noumea.mairie.abs.domain.AgentRecupCount;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeRecup;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

@Service
public class AbsRecuperationDataConsistencyRulesImpl extends AbstractAbsenceDataConsistencyRules {

	/**
	 * Processes the data consistency of a set of Pointages being input by a
	 * user. It will check the different business rules in order to make sure
	 * they're consistent
	 */
	@Override
	public void processDataConsistencyDemandeRecup(ReturnMessageDto srm, Integer idAgent, Demande demande,
			Date dateLundi) {
		checkEtatDemandeIsProvisoireOuSaisie(srm, demande);
		checkDepassementDroitsAcquis(srm, (DemandeRecup) demande);
		
		super.processDataConsistencyDemandeRecup(srm, idAgent, demande, dateLundi);
	}

	@Override
	public ReturnMessageDto checkEtatDemandeIsProvisoireOuSaisie(ReturnMessageDto srm, Demande demande) {

		for (EtatDemande etatDemande : demande.getEtatsDemande()) {
			if (!RefEtatEnum.PROVISOIRE.equals(etatDemande.getEtat())
					&& !RefEtatEnum.SAISIE.equals(etatDemande.getEtat())) {
				logger.warn(String.format(ETAT_NON_PROVISOIRE_OU_SAISIE_MSG, demande.getIdDemande()));
				srm.getErrors().add(String.format(ETAT_NON_PROVISOIRE_OU_SAISIE_MSG, demande.getIdDemande()));
			}
		}

		return srm;
	}

	@Override
	public ReturnMessageDto checkDepassementDroitsAcquis(ReturnMessageDto srm, DemandeRecup demande) {

		// on recupere le solde de l agent
		AgentRecupCount soldeRecup = recuperationRepository.getAgentRecupCount(demande.getIdAgent());

		Integer sommeDemandeEnCours = recuperationRepository.getSommeDureeDemandeRecupEnCoursSaisieouVisee(demande
				.getIdAgent());

		if (soldeRecup.getTotalMinutes() + sommeDemandeEnCours - demande.getDuree() < 0) {
			logger.warn(String.format(DEPASSEMENT_DROITS_ACQUIS_MSG, demande.getIdDemande()));
			srm.getErrors().add(String.format(DEPASSEMENT_DROITS_ACQUIS_MSG, demande.getIdDemande()));
		}

		return srm;
	}
}
