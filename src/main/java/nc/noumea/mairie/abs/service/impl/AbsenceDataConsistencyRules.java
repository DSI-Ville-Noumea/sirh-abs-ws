package nc.noumea.mairie.abs.service.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentRecupCount;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeRecup;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IRecuperationRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.abs.service.IAbsenceDataConsistencyRules;
import nc.noumea.mairie.abs.service.impl.HelperService;
import nc.noumea.mairie.sirh.domain.Agent;
import nc.noumea.mairie.sirh.domain.Spadmn;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class AbsenceDataConsistencyRules implements	IAbsenceDataConsistencyRules {

	private Logger logger = LoggerFactory.getLogger(AbsenceDataConsistencyRules.class);

	@Autowired
	private IRecuperationRepository recuperationRepository;
	
	@Autowired
	private ISirhRepository sirhRepository;
	
	@Autowired
	private HelperService helperService;
	

	public static final String ETAT_NON_PROVISOIRE_OU_SAISIE_MSG = "La modification de la demande [%d] n'est autorisée que si l'état est à Provisoire ou Saisie.";
	public static final String DEPASSEMENT_DROITS_ACQUIS_MSG = "Le dépassement des droits acquis n'est pas autorisé.";
	public static final String INACTIVITE_MSG = "L'agent n'est pas en activité sur cette période.";
	
	public static final List<String> ACTIVITE_CODES = Arrays.asList("01", "02", "03", "04", "23", "24", "60", "61", "62", "63", "64", "65", "66");
	
	/**
	 * Processes the data consistency of a set of Pointages being input by a user.
	 * It will check the different business rules in order to make sure they're consistent
	 */
	@Override
	public void processDataConsistencyDemandeRecup(ReturnMessageDto srm, Integer idAgent, Demande demande, Date dateLundi) {
		checkEtatDemandeIsProvisoireOuSaisie(srm, demande);
		checkDepassementDroitsAcquis(srm, (DemandeRecup)demande);
		
		checkAgentInactivity(srm, idAgent, dateLundi, demande);
	}
	
	@Override
	public ReturnMessageDto checkEtatDemandeIsProvisoireOuSaisie(ReturnMessageDto srm, Demande demande) {
		
		for(EtatDemande etatDemande : demande.getEtatsDemande()){
			if(!RefEtatEnum.PROVISOIRE.equals(etatDemande.getEtat())
					&& !RefEtatEnum.SAISIE.equals(etatDemande.getEtat())){
				logger.warn(String.format(ETAT_NON_PROVISOIRE_OU_SAISIE_MSG, demande.getIdDemande()));
				srm.getErrors().add(String.format(ETAT_NON_PROVISOIRE_OU_SAISIE_MSG, 
						demande.getIdDemande()));
			}
		}
		
		return srm;
	}
	
	@Override
	public ReturnMessageDto checkDepassementDroitsAcquis(ReturnMessageDto srm, DemandeRecup demande) {
		
		// on recupere le solde de l agent
		AgentRecupCount soldeRecup = recuperationRepository.getAgentRecupCount(demande.getIdAgent());
		
		Integer sommeDemandeEnCours = recuperationRepository.getSommeDureeDemandeRecupEnCoursSaisieouVisee(demande.getIdAgent());
		
		if(soldeRecup.getTotalMinutes() + sommeDemandeEnCours - demande.getDuree() < 0) {
			logger.warn(String.format(DEPASSEMENT_DROITS_ACQUIS_MSG, demande.getIdDemande()));
			srm.getErrors().add(String.format(DEPASSEMENT_DROITS_ACQUIS_MSG, 
					demande.getIdDemande()));
		}
		
		return srm;
	}
	
	@Override
	public ReturnMessageDto checkDemandeDejaSaisieSurMemePeriode(ReturnMessageDto srm, DemandeRecup demande) {
		
		
		
		return srm;
	}
	
	@Override
	public ReturnMessageDto checkAgentInactivity(ReturnMessageDto srm, Integer idAgent, Date dateLundi, Demande demande) {

		Agent ag = sirhRepository.getAgent(idAgent);
		Spadmn adm = sirhRepository.getAgentCurrentPosition(ag, dateLundi);
		
		if (!ACTIVITE_CODES.contains(adm.getCdpadm())){
			logger.warn(String.format(ETAT_NON_PROVISOIRE_OU_SAISIE_MSG, demande.getIdDemande()));
			srm.getErrors().add(INACTIVITE_MSG);
		}

		return srm;
	}
	
	protected DateTime getDateFin(Integer dateDeb, Integer duree) {
		
		DateTime recupDateFin = new DateTime(helperService.getDateFromMairieInteger(dateDeb));
		recupDateFin = recupDateFin.plusMinutes(duree); 

		return recupDateFin;
	}
}
