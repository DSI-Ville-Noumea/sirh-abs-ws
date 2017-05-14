package nc.noumea.mairie.abs.service.rules.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentRecupCount;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeRecup;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IRecuperationRepository;
import nc.noumea.mairie.abs.vo.CheckCompteurAgentVo;

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
		checkDepassementDroitsAcquis(srm, demande, null);

		super.processDataConsistencyDemande(srm, idAgent, demande,  isProvenanceSIRH);
	}

	@Override
	public ReturnMessageDto checkDepassementDroitsAcquis(ReturnMessageDto srm, Demande demande, CheckCompteurAgentVo checkCompteurAgentVo) {

		Integer solde = 0;
		Integer sommeDemandeEnCours = 0;
		
		// on recupere le solde de l agent
		if(null != checkCompteurAgentVo
				&& null != checkCompteurAgentVo.getCompteurRecup()
				&& null != checkCompteurAgentVo.getDureeDemandeEnCoursRecup()) {
			solde = checkCompteurAgentVo.getCompteurRecup();
			sommeDemandeEnCours = checkCompteurAgentVo.getDureeDemandeEnCoursRecup();
		}else{
			AgentRecupCount soldeRecup = counterRepository.getAgentCounter(AgentRecupCount.class, demande.getIdAgent());

			sommeDemandeEnCours = recuperationRepository.getSommeDureeDemandeRecupEnCoursSaisieouVisee(
					demande.getIdAgent(), demande.getIdDemande());
			

			solde = null == soldeRecup ? 0 : soldeRecup.getTotalMinutes();
			
			if(null != checkCompteurAgentVo) {
				checkCompteurAgentVo.setCompteurRecup(solde);
				checkCompteurAgentVo.setDureeDemandeEnCoursRecup(sommeDemandeEnCours);
			}
		}
		
		if (solde - sommeDemandeEnCours - ((DemandeRecup) demande).getDuree() < 0) {
			logger.warn(String.format(DEPASSEMENT_DROITS_ACQUIS_MSG, demande.getIdDemande()));
			srm.getErrors().add(String.format(DEPASSEMENT_DROITS_ACQUIS_MSG, demande.getIdDemande()));
		}

		return srm;
	}

	// #17483
	protected boolean isAfficherBoutonAnnuler(DemandeDto demandeDto, boolean isOperateur, boolean isFromSIRH) {
		return demandeDto.getIdRefEtat().equals(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat())
				|| (isOperateur && demandeDto.getIdRefEtat().equals(RefEtatEnum.APPROUVEE.getCodeEtat()))
				|| (isOperateur && demandeDto.getIdRefEtat().equals(RefEtatEnum.VALIDEE.getCodeEtat()))
				|| (isOperateur && demandeDto.getIdRefEtat().equals(RefEtatEnum.EN_ATTENTE.getCodeEtat()))
				|| (isOperateur && demandeDto.getIdRefEtat().equals(RefEtatEnum.A_VALIDER.getCodeEtat()))
				|| (isOperateur && demandeDto.getIdRefEtat().equals(RefEtatEnum.PRISE.getCodeEtat()));
	}

	@Override
	public ReturnMessageDto checkEtatsDemandeAnnulee(ReturnMessageDto srm, Demande demande,
			List<RefEtatEnum> listEtatsAcceptes) {

		List<RefEtatEnum> listEtats = new ArrayList<RefEtatEnum>();
		listEtats.addAll(listEtatsAcceptes);
		listEtats.addAll(Arrays.asList(RefEtatEnum.VISEE_FAVORABLE, RefEtatEnum.VISEE_DEFAVORABLE,
				RefEtatEnum.APPROUVEE, RefEtatEnum.PRISE, RefEtatEnum.VALIDEE, RefEtatEnum.EN_ATTENTE,
				RefEtatEnum.A_VALIDER));
		// dans le cas des CONGES ANNUELS, on peut tout annuler sauf
		// saisie,provisoire,refuse,rejeté et annulé
		return super.checkEtatsDemandeAnnulee(srm, demande, listEtats);
	}

	protected boolean isAfficherBoutonImprimer(DemandeDto demandeDto) {
		if (demandeDto.getIdRefEtat().equals(RefEtatEnum.APPROUVEE.getCodeEtat()) 
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.PRISE.getCodeEtat())) {
			return true;
		}
		return false;
	}
}
