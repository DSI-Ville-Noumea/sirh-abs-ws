package nc.noumea.mairie.abs.service.rules.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentReposCompCount;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeReposComp;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IReposCompensateurRepository;
import nc.noumea.mairie.abs.vo.CheckCompteurAgentVo;
import nc.noumea.mairie.domain.Spcarr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("AbsReposCompensateurDataConsistencyRulesImpl")
public class AbsReposCompensateurDataConsistencyRulesImpl extends AbstractAbsenceDataConsistencyRules {

	@Autowired
	protected IReposCompensateurRepository reposCompensateurRepository;

	/**
	 * Processes the data consistency of a set of Pointages being input by a
	 * user. It will check the different business rules in order to make sure
	 * they're consistent
	 */
	@Override
	public void processDataConsistencyDemande(ReturnMessageDto srm, Integer idAgent, Demande demande, boolean isProvenanceSIRH) {
		checkStatutAgent(srm, demande.getIdAgent(), isProvenanceSIRH);
		checkEtatsDemandeAcceptes(srm, demande, Arrays.asList(RefEtatEnum.PROVISOIRE, RefEtatEnum.SAISIE));
		if (srm.getErrors().size() == 0)
			checkDepassementDroitsAcquis(srm, demande, null);

		super.processDataConsistencyDemande(srm, idAgent, demande, isProvenanceSIRH);
	}

	public ReturnMessageDto checkStatutAgent(ReturnMessageDto srm, Integer idAgent, boolean isProvenanceSIRH) {
		if (!isProvenanceSIRH) {
			// on recherche sa carriere pour avoir son statut (Fonctionnaire,
			// contractuel,convention coll
			Spcarr carr = sirhRepository.getAgentCurrentCarriere(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(idAgent), helperService.getCurrentDate());
			if (carr != null && !(carr.getCdcate() == 4 || carr.getCdcate() == 7)) {
				logger.warn(String.format(STATUT_AGENT, idAgent));
				srm.getErrors().add(String.format(STATUT_AGENT, idAgent));
			}
		}

		return srm;
	}

	@Override
	public ReturnMessageDto checkDepassementDroitsAcquis(ReturnMessageDto srm, Demande demande, CheckCompteurAgentVo checkCompteurAgentVo) {

		// on recupere le solde de l agent
		Integer soldeReposCompNetN1 = 0;
		Integer sommeDemandeEnCours = 0;

		if (null != checkCompteurAgentVo && null != checkCompteurAgentVo.getCompteurRecup() && null != checkCompteurAgentVo.getDureeDemandeEnCoursRecup()) {
			soldeReposCompNetN1 = checkCompteurAgentVo.getCompteurReposComp();
			sommeDemandeEnCours = checkCompteurAgentVo.getDureeDemandeEnCoursReposComp();
		} else {
			AgentReposCompCount soldeReposComp = counterRepository.getAgentCounter(AgentReposCompCount.class, demande.getIdAgent());

			sommeDemandeEnCours = reposCompensateurRepository.getSommeDureeDemandeReposCompEnCoursSaisieouVisee(demande.getIdAgent(), demande.getIdDemande());

			soldeReposCompNetN1 = soldeReposComp.getTotalMinutes() + soldeReposComp.getTotalMinutesAnneeN1();

			if (null != checkCompteurAgentVo) {
				checkCompteurAgentVo.setCompteurReposComp(soldeReposCompNetN1);
				checkCompteurAgentVo.setDureeDemandeEnCoursReposComp(sommeDemandeEnCours);
			}
		}

		if (soldeReposCompNetN1 - sommeDemandeEnCours - ((DemandeReposComp) demande).getDuree() < 0) {
			logger.warn(String.format(DEPASSEMENT_DROITS_ACQUIS_MSG, demande.getIdDemande()));
			srm.getErrors().add(String.format(DEPASSEMENT_DROITS_ACQUIS_MSG, demande.getIdDemande()));
		}

		return srm;
	}

	// #17483
	protected boolean isAfficherBoutonAnnuler(DemandeDto demandeDto, boolean isOperateur) {
		return demandeDto.getIdRefEtat().equals(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat()) || demandeDto.getIdRefEtat().equals(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat())
				|| (isOperateur && demandeDto.getIdRefEtat().equals(RefEtatEnum.APPROUVEE.getCodeEtat())) || (isOperateur && demandeDto.getIdRefEtat().equals(RefEtatEnum.VALIDEE.getCodeEtat()))
				|| (isOperateur && demandeDto.getIdRefEtat().equals(RefEtatEnum.EN_ATTENTE.getCodeEtat())) || (isOperateur && demandeDto.getIdRefEtat().equals(RefEtatEnum.A_VALIDER.getCodeEtat()))
				|| (isOperateur && demandeDto.getIdRefEtat().equals(RefEtatEnum.PRISE.getCodeEtat()));
	}

	@Override
	public ReturnMessageDto checkEtatsDemandeAnnulee(ReturnMessageDto srm, Demande demande, List<RefEtatEnum> listEtatsAcceptes) {

		List<RefEtatEnum> listEtats = new ArrayList<RefEtatEnum>();
		listEtats.addAll(listEtatsAcceptes);
		listEtats.addAll(Arrays.asList(RefEtatEnum.VISEE_FAVORABLE, RefEtatEnum.VISEE_DEFAVORABLE, RefEtatEnum.APPROUVEE, RefEtatEnum.PRISE, RefEtatEnum.VALIDEE, RefEtatEnum.EN_ATTENTE,
				RefEtatEnum.A_VALIDER));
		// dans le cas des CONGES ANNUELS, on peut tout annuler sauf
		// saisie,provisoire,refuse,rejeté et annulé
		return super.checkEtatsDemandeAnnulee(srm, demande, listEtats);
	}
}
