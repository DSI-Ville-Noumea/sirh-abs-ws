package nc.noumea.mairie.abs.service.counter.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import nc.noumea.mairie.abs.domain.AgentCount;
import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeMaladies;
import nc.noumea.mairie.abs.domain.MotifCompteur;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.AgentOrganisationSyndicaleDto;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.OrganisationSyndicaleDto;
import nc.noumea.mairie.abs.dto.RestitutionMassiveDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.dto.SoldeEnfantMaladeDto;
import nc.noumea.mairie.abs.dto.SoldeMaladiesDto;
import nc.noumea.mairie.abs.dto.SoldeSpecifiqueDto;
import nc.noumea.mairie.abs.repository.IAccessRightsRepository;
import nc.noumea.mairie.abs.repository.ICounterRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.abs.service.ICounterService;
import nc.noumea.mairie.abs.service.impl.HelperService;
import nc.noumea.mairie.abs.vo.CalculDroitsMaladiesVo;
import nc.noumea.mairie.ws.ISirhWSConsumer;

public abstract class AbstractCounterService implements ICounterService {

	protected Logger					logger							= LoggerFactory.getLogger(AbstractCounterService.class);

	@Autowired
	protected ISirhWSConsumer			sirhWSConsumer;

	@Autowired
	protected ICounterRepository		counterRepository;

	@Autowired
	protected ISirhRepository			sirhRepository;

	@Autowired
	protected HelperService				helperService;

	@Autowired
	protected IAccessRightsRepository	accessRightsRepository;

	protected static final String		MOTIF_COMPTEUR_INEXISTANT		= "Le motif n'existe pas.";
	protected static final String		SOLDE_COMPTEUR_NEGATIF			= "Le solde du compteur de l'agent ne peut pas être négatif.";
	protected static final String		SOLDE_COMPTEUR_NEGATIF_AUTORISE	= "Le solde du compteur de l'agent est négatif.";
	protected static final String		OPERATEUR_INEXISTANT			= "Vous n'êtes pas habilité à mettre à jour le compteur de cet agent.";
	protected static final String		DUREE_A_SAISIR					= "La durée à ajouter ou retrancher n'est pas saisie.";
	protected static final String		ERREUR_DUREE_SAISIE				= "Un seul des champs Durée à ajouter ou Durée à retrancher doit être saisi.";
	protected static final String		COMPTEUR_INEXISTANT				= "Le compteur n'existe pas.";
	protected static final String		COMPTEUR_EXISTANT				= "Le compteur existe déjà %s.";
	protected static final String		COMPTEUR_EXISTANT_DATE			= "Un compteur pour l'organisation syndicale [%s] existe déjà sur ces dates.";
	protected static final String		TYPE_COMPTEUR_INEXISTANT		= "Le type de compteur n'existe pas.";
	protected static final String		OS_INEXISTANT					= "L'organisation syndicale n'existe pas.";
	protected static final String		OS_INACTIVE						= "L'organisation syndicale n'est pas active.";
	protected static final String		AGENT_OS_EXISTANT				= "L'agent [%d] fait déja partie d'une autre organisation syndicale.";

	protected static final String		RESET_COMPTEUR_ANNEE_PRECEDENTE	= "Remise à 0 du compteur Année précédente";
	protected static final String		INITIATE_COMPTEUR				= "Initialisation du compteur";
	protected static final String		RESET_COMPTEUR_ANNEE_EN_COURS	= "Remise à 0 du compteur de l'année en cours";

	protected static final String		ERROR_TECHNIQUE					= "Erreur technique : ICounterService défaut d'implémentation";

	protected static final String		AGENT_NON_HABILITE				= "L'agent n'est pas habilité à saisir une demande.";

	/**
	 * appeler par PTG exclusivement l historique utilise a pour seul but de
	 * rectifier le compteur en cas de modification par l agent dans ses
	 * pointages
	 */
	@Override
	public int addToAgentForPTG(Integer idAgent, Date dateMonday, Integer minutes) {
		return 0;
	}
	
	/**
	 * Appelé par les maladies, pour savoir si le solde a été dépassé.
	 */
	@Override
	public SoldeEnfantMaladeDto getSoldeEnfantMalade(Integer idAgent) {
		return null;
	}

	/**
	 * appeler par PTG exclusivement l historique utilise a pour seul but de
	 * rectifier le compteur en cas de modification par l agent dans ses
	 * pointages #16761
	 */
	@Override
	public int addToAgentForPTG(Integer idAgent, Date date, Integer minutes, Integer idPointage, Integer idPointageParent) {
		return 0;
	}

	/**
	 * appeler depuis ABSENCE l historique ABS_AGENT_WEEK_... n est pas utilise
	 */
	@Override
	public ReturnMessageDto majCompteurToAgent(ReturnMessageDto srm, Demande demande, DemandeEtatChangeDto demandeEtatChangeDto) {
		// pas de maj de compteur, ex : ASA A49 et A50
		return srm;
	}

	/**
	 * appeler depuis Kiosque ou SIRH l historique ABS_AGENT_WEEK_ALIM_MANUELLE
	 * mise a jour
	 */
	@Override
	@Transactional(value = "absTransactionManager")
	public ReturnMessageDto majManuelleCompteurToAgent(Integer idAgent, CompteurDto compteurDto, boolean compteurExistantBloquant) {

		logger.info("Trying to update manually counters for Agent {} ...", compteurDto.getIdAgent());

		ReturnMessageDto result = new ReturnMessageDto();

		// tester si agent est un utilisateur SIRH
		ReturnMessageDto isUtilisateurSIRH = sirhWSConsumer.isUtilisateurSIRH(idAgent);
		if (!isUtilisateurSIRH.getErrors().isEmpty()) {
			// seul l operateur peut mettre a jour les compteurs de ses agents
			if (!accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())) {
				logger.warn(OPERATEUR_INEXISTANT);
				result.getErrors().add(String.format(OPERATEUR_INEXISTANT));
				return result;
			}
		}

		controlSaisieAlimManuelleCompteur(compteurDto, result);

		MotifCompteur motifCompteur = null;
		if (compteurDto.getMotifCompteurDto() != null) {
			motifCompteur = counterRepository.getEntity(MotifCompteur.class, compteurDto.getMotifCompteurDto().getIdMotifCompteur());
		}
		if (null == motifCompteur) {
			logger.warn(MOTIF_COMPTEUR_INEXISTANT);
			result.getErrors().add(String.format(MOTIF_COMPTEUR_INEXISTANT));
		}

		if (!result.getErrors().isEmpty()) {
			return result;
		}

		majManuelleCompteurToAgent(idAgent, compteurDto, result, motifCompteur, compteurExistantBloquant);

		return result;
	}

	protected ReturnMessageDto majManuelleCompteurToAgent(Integer idAgent, CompteurDto compteurDto, ReturnMessageDto result,
			MotifCompteur motifCompteur, boolean compteurExistantBloquant) {

		logger.debug(TYPE_COMPTEUR_INEXISTANT);
		result.getErrors().add(String.format(TYPE_COMPTEUR_INEXISTANT));
		return result;
	}

	public void controlSaisieAlimManuelleCompteur(CompteurDto compteurDto, ReturnMessageDto result) {

		if (null == compteurDto.getDureeAAjouter() && null == compteurDto.getDureeARetrancher()) {
			logger.debug(DUREE_A_SAISIR);
			result.getErrors().add(String.format(DUREE_A_SAISIR));
		}

		if (null != compteurDto.getDureeAAjouter() && null != compteurDto.getDureeARetrancher()) {
			logger.debug(ERREUR_DUREE_SAISIE);
			result.getErrors().add(String.format(ERREUR_DUREE_SAISIE));
		}
	}

	protected void controlCompteurPositif(Double minutes, Double totalMinutes, ReturnMessageDto srm) {
		if (minutes == null)
			minutes = 0.0;
		if (totalMinutes == null)
			totalMinutes = 0.0;

		if (null != minutes && 0 > totalMinutes + minutes) {
			logger.warn(SOLDE_COMPTEUR_NEGATIF);
			srm.getErrors().add(String.format(SOLDE_COMPTEUR_NEGATIF));
		}
	}

	protected void majAgentHistoAlimManuelle(Integer idAgentOperateur, Integer idAgentConcerne, MotifCompteur motifCompteur, String textLog,
			AgentCount compteurAgent, Integer idRefTypeAbsence) {

		AgentHistoAlimManuelle histo = new AgentHistoAlimManuelle();
		histo.setIdAgent(idAgentOperateur);
		histo.setIdAgentConcerne(idAgentConcerne);
		histo.setDateModification(helperService.getCurrentDate());
		histo.setMotifCompteur(motifCompteur);
		histo.setText(textLog);
		histo.setCompteurAgent(compteurAgent);

		RefTypeAbsence rta = new RefTypeAbsence();
		rta.setIdRefTypeAbsence(idRefTypeAbsence);
		histo.setType(rta);

		counterRepository.persistEntity(histo);
	}

	@Override
	public ReturnMessageDto resetCompteurRCAnneePrecedente(Integer idAgentReposCompCount) {

		ReturnMessageDto srm = new ReturnMessageDto();
		srm.getErrors().add(String.format(ERROR_TECHNIQUE));

		return srm;
	}

	@Override
	public ReturnMessageDto resetCompteurRCAnneenCours(Integer idAgentReposCompCount) {

		ReturnMessageDto srm = new ReturnMessageDto();
		srm.getErrors().add(String.format(ERROR_TECHNIQUE));

		return srm;
	}

	@Override
	public List<Integer> getListAgentReposCompCountForResetAnneePrcd() {
		return new ArrayList<Integer>();
	}

	@Override
	public List<Integer> getListAgentCongeAnnuelCountForReset() {
		return new ArrayList<Integer>();
	}

	@Override
	public List<Integer> getListAgentReposCompCountForResetAnneeEnCours() {
		return new ArrayList<Integer>();
	}

	@Override
	public List<CompteurDto> getListeCompteur(Integer idOrganisation, Integer annee) {
		return null;
	}

	@Override
	public List<CompteurDto> getListeCompteurAmicale(Integer idAgentRecherche, Integer annee, Boolean actif) {
		return null;
	}

	@Override
	public Integer countAllByYear(Integer annee, Integer idOS, Integer idAgentRecherche, Date dateMin, Date dateMax) {
		return null;
	}

	@Override
	public List<CompteurDto> getListeCompteur(Integer idOrganisationSyndicale, Integer annee, Integer pageSize, Integer pageNumber,Integer idAgentRecherche) {
		return null;
	}
	
	@Override
	public List<CompteurDto> getListeCompteurWithDate(Integer pageSize, Integer pageNumber, Integer idAgentRecherche, String dateMin, String dateMax) throws ParseException {
		return null;
	}

	@Override
	public List<SoldeSpecifiqueDto> getListAgentCounterByDate(Integer idAgent, Date dateDebut, Date dateFin) {
		return null;
	}

	@Override
	public ReturnMessageDto initCompteurCongeAnnuel(Integer idAgent, Integer idAgentConcerne) {
		return null;
	}

	@Override
	public ReturnMessageDto resetCompteurCongeAnnuel(Integer idAgentCongeAnnuelCount) {
		ReturnMessageDto srm = new ReturnMessageDto();
		srm.getErrors().add(String.format(ERROR_TECHNIQUE));

		return srm;
	}

	@Override
	public ReturnMessageDto saveRepresentantA52(Integer idOrganisationSyndicale, List<AgentOrganisationSyndicaleDto> listeAgentDto) {
		ReturnMessageDto srm = new ReturnMessageDto();
		srm.getErrors().add(String.format(ERROR_TECHNIQUE));

		return srm;
	}

	@Override
	public ReturnMessageDto alimentationAutoCompteur(Integer idAgentCongeAnnuelCount, Date dateDebut, Date dateFin) {
		ReturnMessageDto srm = new ReturnMessageDto();
		srm.getErrors().add(String.format(ERROR_TECHNIQUE));

		return srm;
	}

	@Override
	public ReturnMessageDto restitutionMassiveCA(Integer idAgent, RestitutionMassiveDto dto, List<Integer> listIdAgent) {
		ReturnMessageDto srm = new ReturnMessageDto();
		srm.getErrors().add(String.format(ERROR_TECHNIQUE));
		return srm;
	}

	@Override
	public ReturnMessageDto checkRestitutionMassiveDto(RestitutionMassiveDto dto, ReturnMessageDto srm) {
		srm.getErrors().add(String.format(ERROR_TECHNIQUE));
		return srm;
	}

	@Override
	public List<RestitutionMassiveDto> getHistoRestitutionMassiveCA(Integer idAgentConnecte) {
		return null;
	}

	@Override
	public RestitutionMassiveDto getDetailsHistoRestitutionMassive(Integer idAgentConnecte, RestitutionMassiveDto dto) {
		return null;
	}

	@Override
	public List<RestitutionMassiveDto> getHistoRestitutionMassiveCAByAgent(Integer idAgent) {
		return null;
	}

	@Override
	public List<OrganisationSyndicaleDto> getlisteOrganisationSyndicaleA52() {
		return null;
	}

	@Override
	public List<AgentOrganisationSyndicaleDto> listeRepresentantA52(Integer idOrganisationSyndicale) {
		return null;
	}

	/**
	 * appeler depuis SIRH pour mettre à jour en masse les compteurs des
	 * elections mise a jour
	 */
	@Override
	@Transactional(value = "absTransactionManager")
	public ReturnMessageDto majManuelleCompteurToListAgent(Integer idAgent, List<CompteurDto> listeCompteurDto, boolean compteurExistantBloquant) {
		ReturnMessageDto resultGlobal = new ReturnMessageDto();
		// tester si agent est un utilisateur SIRH
		ReturnMessageDto isUtilisateurSIRH = sirhWSConsumer.isUtilisateurSIRH(idAgent);
		if (!isUtilisateurSIRH.getErrors().isEmpty()) {
			logger.warn(OPERATEUR_INEXISTANT);
			resultGlobal.getErrors().add(String.format(OPERATEUR_INEXISTANT));
			return resultGlobal;
		}

		for (CompteurDto compteurDto : listeCompteurDto) {
			ReturnMessageDto result = new ReturnMessageDto();
			logger.info("Trying to update manually counters for Agent {} ...", compteurDto.getIdAgent());

			controlSaisieAlimManuelleCompteur(compteurDto, result);

			MotifCompteur motifCompteur = null;
			if (compteurDto.getMotifCompteurDto() != null) {
				motifCompteur = counterRepository.getEntity(MotifCompteur.class, compteurDto.getMotifCompteurDto().getIdMotifCompteur());
			}
			if (null == motifCompteur) {
				logger.warn(MOTIF_COMPTEUR_INEXISTANT);
				result.getErrors().add(String.format(MOTIF_COMPTEUR_INEXISTANT));
			}

			if (!result.getErrors().isEmpty()) {
				resultGlobal.getErrors().addAll(result.getErrors());
				continue;
			}

			ReturnMessageDto resMAJ = majManuelleCompteurToAgent(idAgent, compteurDto, new ReturnMessageDto(), motifCompteur,
					compteurExistantBloquant);

			if (!resMAJ.getErrors().isEmpty()) {
				resultGlobal.getErrors().addAll(resMAJ.getErrors());
			}
		}

		return resultGlobal;
	}

	@Override
	public ReturnMessageDto saveRepresentantA54(Integer idOrganisationSyndicale, Integer idAgent) {
		ReturnMessageDto srm = new ReturnMessageDto();
		srm.getErrors().add(String.format(ERROR_TECHNIQUE));

		return srm;
	}

	@Override
	public ReturnMessageDto saveRepresentantA48(Integer idOrganisationSyndicale, Integer idAgent) {
		ReturnMessageDto srm = new ReturnMessageDto();
		srm.getErrors().add(String.format(ERROR_TECHNIQUE));

		return srm;
	}

	@Override
	public SoldeMaladiesDto getSoldeByAgent(Integer idAgent, Date dateFinAnneeGlissante, AgentGeneriqueDto agentDto) {
		return null;

	}

	@Override
	public CalculDroitsMaladiesVo calculDroitsMaladiesForDemandeMaladies(Integer idAgent, DemandeDto demandeMaladie) {
		return null;
	}

	@Override
	public Integer getNombeJourMaladies(Integer idAgent, Date dateDebutAnneeGlissante, Date dateFinAnneeGlissante, List<DemandeMaladies> listMaladies) {
		return null;
	}
	
	@Override
	public List<DemandeMaladies> getHistoriqueMaladiesWithDroits(Integer idAgent, Date date) {
		return null;
	}
	
	@Override
	public ReturnMessageDto dupliqueCompteursA48(Integer idOS, Integer idAgent, Integer annee) {
		return null;
	}
	
	@Override
	public ReturnMessageDto dupliqueCompteursA54(Integer idOS, Integer idAgent, Integer annee) {
		return null;
	}
}
