package nc.noumea.mairie.abs.reposComp.service.rules.impl;

import java.util.Arrays;
import java.util.Date;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.reposComp.domain.AgentReposCompCount;
import nc.noumea.mairie.abs.reposComp.domain.DemandeReposComp;
import nc.noumea.mairie.abs.service.rules.impl.AbstractAbsenceDataConsistencyRules;
import nc.noumea.mairie.domain.Spcarr;

import org.springframework.stereotype.Service;

@Service("AbsReposCompensateurDataConsistencyRulesImpl")
public class AbsReposCompensateurDataConsistencyRulesImpl extends AbstractAbsenceDataConsistencyRules {

	/**
	 * Processes the data consistency of a set of Pointages being input by a
	 * user. It will check the different business rules in order to make sure
	 * they're consistent
	 */
	@Override
	public void processDataConsistencyDemande(ReturnMessageDto srm, Integer idAgent, Demande demande, Date dateLundi) {
		checkStatutAgent(srm, demande.getIdAgent());
		checkEtatsDemandeAcceptes(srm, demande, Arrays.asList(RefEtatEnum.PROVISOIRE, RefEtatEnum.SAISIE));
		checkDepassementDroitsAcquis(srm, demande);

		super.processDataConsistencyDemande(srm, idAgent, demande, dateLundi);
	}

	public ReturnMessageDto checkStatutAgent(ReturnMessageDto srm, Integer idAgent) {
		// on recherche sa carriere pour avoir son statut (Fonctionnaire,
		// contractuel,convention coll
		Spcarr carr = sirhRepository.getAgentCurrentCarriere(idAgent, helperService.getCurrentDate());
		if (!(carr.getCdcate() == 4 || carr.getCdcate() == 7)) {
			logger.warn(String.format(STATUT_AGENT, idAgent));
			srm.getErrors().add(String.format(STATUT_AGENT, idAgent));
		}

		return srm;
	}

	public ReturnMessageDto checkDepassementDroitsAcquis(ReturnMessageDto srm, Demande demande) {
		// on recupere le solde de l agent
		AgentReposCompCount soldeReposComp = counterRepository.getAgentCounter(AgentReposCompCount.class,
				demande.getIdAgent());

		Integer sommeDemandeEnCours = reposCompensateurRepository.getSommeDureeDemandeReposCompEnCoursSaisieouVisee(
				demande.getIdAgent(), demande.getIdDemande());

		if (null == soldeReposComp
				|| (soldeReposComp.getTotalMinutes() + soldeReposComp.getTotalMinutesAnneeN1()) - sommeDemandeEnCours
						- ((DemandeReposComp) demande).getDuree() < 0) {
			logger.warn(String.format(DEPASSEMENT_DROITS_ACQUIS_MSG, demande.getIdDemande()));
			srm.getErrors().add(String.format(DEPASSEMENT_DROITS_ACQUIS_MSG, demande.getIdDemande()));
		}

		return srm;
	}
	
	public boolean isAfficherBoutonImprimer(DemandeDto demandeDto) {
		
		return demandeDto.getIdRefEtat().equals(RefEtatEnum.APPROUVEE.getCodeEtat());
	}
}
