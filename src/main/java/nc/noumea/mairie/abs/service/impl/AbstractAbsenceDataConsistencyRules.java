package nc.noumea.mairie.abs.service.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IDemandeRepository;
import nc.noumea.mairie.abs.repository.IRecuperationRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.abs.service.IAbsenceDataConsistencyRules;
import nc.noumea.mairie.domain.Spadmn;
import nc.noumea.mairie.sirh.domain.Agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public abstract class AbstractAbsenceDataConsistencyRules implements IAbsenceDataConsistencyRules {
	
	protected Logger logger = LoggerFactory.getLogger(AbstractAbsenceDataConsistencyRules.class);

	@Autowired
	protected IRecuperationRepository recuperationRepository;

	@Autowired
	protected ISirhRepository sirhRepository;

	@Autowired
	protected HelperService helperService;
	
	@Autowired
	protected IDemandeRepository demandeRepository;

	public static final String ETAT_NON_PROVISOIRE_OU_SAISIE_MSG = "La modification de la demande [%d] n'est autorisée que si l'état est à Provisoire ou Saisie.";
	public static final String DEPASSEMENT_DROITS_ACQUIS_MSG = "Le dépassement des droits acquis n'est pas autorisé.";
	public static final String INACTIVITE_MSG = "L'agent n'est pas en activité sur cette période.";
	public static final String DEMANDE_DEJA_COUVERTE_MSG = "La demande ne peut être couverte totalement ou partiellement par une autre absence.";

	public static final List<String> ACTIVITE_CODES = Arrays.asList("01", "02", "03", "04", "23", "24", "60", "61",
			"62", "63", "64", "65", "66");

	/**
	 * Processes the data consistency of a set of Pointages being input by a
	 * user. It will check the different business rules in order to make sure
	 * they're consistent
	 */
	@Override
	public void processDataConsistencyDemandeRecup(ReturnMessageDto srm, Integer idAgent, Demande demande,
			Date dateLundi) {
		checkDemandeDejaSaisieSurMemePeriode(srm, demande);
		checkAgentInactivity(srm, idAgent, dateLundi, demande);
	}

	@Override
	public ReturnMessageDto checkDemandeDejaSaisieSurMemePeriode(ReturnMessageDto srm, Demande demande) {
		
		List<Demande> listDemande = demandeRepository.listeDemandesAgent(demande.getIdAgent(), null, null, null);
		
		for(Demande demandeExistante : listDemande) {
			
			if(!RefEtatEnum.REFUSEE.equals(demandeExistante.getLatestEtatDemande().getEtat())
					&& !RefEtatEnum.PROVISOIRE.equals(demandeExistante.getLatestEtatDemande().getEtat())){
				
				// date de debut couverte par une autre demande
				if(demande.getDateDebut().before(demandeExistante.getDateFin())
						&& demande.getDateDebut().after(demandeExistante.getDateDebut())) {
					logger.warn(String.format(DEMANDE_DEJA_COUVERTE_MSG));
					srm.getErrors().add(DEMANDE_DEJA_COUVERTE_MSG);
					return srm;
				}
				if(demande.getDateFin().before(demandeExistante.getDateFin())
						&& demande.getDateFin().after(demandeExistante.getDateDebut())) {
					logger.warn(String.format(DEMANDE_DEJA_COUVERTE_MSG));
					srm.getErrors().add(DEMANDE_DEJA_COUVERTE_MSG);
					return srm;
				}
			}
		}
		
		return srm;
	}

	@Override
	public ReturnMessageDto checkAgentInactivity(ReturnMessageDto srm, Integer idAgent, Date dateLundi, Demande demande) {

		Agent ag = sirhRepository.getAgent(idAgent);
		
		Spadmn adm = sirhRepository.getAgentCurrentPosition(ag, dateLundi);
		 
		if (null == adm
				|| !ACTIVITE_CODES.contains(adm.getCdpadm())){
			logger.warn(String.format(INACTIVITE_MSG)); 
			srm.getErrors().add(INACTIVITE_MSG); 
		}

		return srm;
	}

}
