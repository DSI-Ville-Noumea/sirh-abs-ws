package nc.noumea.mairie.abs.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.FlushModeType;

import nc.noumea.mairie.abs.domain.AgentCongeAnnuelCount;
import nc.noumea.mairie.abs.domain.AgentReposCompCount;
import nc.noumea.mairie.abs.domain.AgentWeekCongeAnnuel;
import nc.noumea.mairie.abs.domain.CongeAnnuelAlimAutoHisto;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeAsa;
import nc.noumea.mairie.abs.domain.DemandeCongesAnnuels;
import nc.noumea.mairie.abs.domain.DemandeCongesExceptionnels;
import nc.noumea.mairie.abs.domain.DemandeRecup;
import nc.noumea.mairie.abs.domain.DemandeReposComp;
import nc.noumea.mairie.abs.domain.DroitsAgent;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.EtatDemandeAsa;
import nc.noumea.mairie.abs.domain.EtatDemandeCongesAnnuels;
import nc.noumea.mairie.abs.domain.EtatDemandeCongesExceptionnels;
import nc.noumea.mairie.abs.domain.EtatDemandeRecup;
import nc.noumea.mairie.abs.domain.EtatDemandeReposComp;
import nc.noumea.mairie.abs.domain.OrganisationSyndicale;
import nc.noumea.mairie.abs.domain.RefAlimCongeAnnuel;
import nc.noumea.mairie.abs.domain.RefAlimCongeAnnuelId;
import nc.noumea.mairie.abs.domain.RefEtat;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefGroupeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.domain.RefTypeGroupeAbsenceEnum;
import nc.noumea.mairie.abs.domain.RefTypeSaisiCongeAnnuel;
import nc.noumea.mairie.abs.dto.AgentDto;
import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.MoisAlimAutoCongesAnnuelsDto;
import nc.noumea.mairie.abs.dto.RefAlimCongesAnnuelsDto;
import nc.noumea.mairie.abs.dto.RefTypeSaisiCongeAnnuelDto;
import nc.noumea.mairie.abs.dto.RestitutionMassiveDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IAccessRightsRepository;
import nc.noumea.mairie.abs.repository.ICongesAnnuelsRepository;
import nc.noumea.mairie.abs.repository.ICounterRepository;
import nc.noumea.mairie.abs.repository.IDemandeRepository;
import nc.noumea.mairie.abs.repository.IFiltreRepository;
import nc.noumea.mairie.abs.repository.IOrganisationSyndicaleRepository;
import nc.noumea.mairie.abs.repository.IReposCompensateurRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.abs.repository.ITypeAbsenceRepository;
import nc.noumea.mairie.abs.service.IAbsenceDataConsistencyRules;
import nc.noumea.mairie.abs.service.IAbsenceService;
import nc.noumea.mairie.abs.service.IAccessRightsService;
import nc.noumea.mairie.abs.service.IAgentMatriculeConverterService;
import nc.noumea.mairie.abs.service.IAgentService;
import nc.noumea.mairie.abs.service.ICounterService;
import nc.noumea.mairie.abs.service.IFiltreService;
import nc.noumea.mairie.abs.service.counter.impl.CounterServiceFactory;
import nc.noumea.mairie.abs.service.rules.impl.DataConsistencyRulesFactory;
import nc.noumea.mairie.domain.SpSold;
import nc.noumea.mairie.domain.SpSorc;
import nc.noumea.mairie.domain.Spcarr;
import nc.noumea.mairie.domain.Spcc;
import nc.noumea.mairie.domain.SpccId;
import nc.noumea.mairie.domain.Spmatr;
import nc.noumea.mairie.domain.TypeChainePaieEnum;
import nc.noumea.mairie.ws.ISirhWSConsumer;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AbsenceService implements IAbsenceService {

	private Logger logger = LoggerFactory.getLogger(AbsenceService.class);

	@Autowired
	private IDemandeRepository demandeRepository;

	@Autowired
	private IFiltreRepository filtreRepository;

	@Autowired
	private IAccessRightsRepository accessRightsRepository;

	@Autowired
	private IAccessRightsService accessRightsService;

	@Autowired
	private ISirhRepository sirhRepository;

	@Autowired
	private IFiltreService filtresService;

	@Autowired
	private DataConsistencyRulesFactory dataConsistencyRulesFactory;

	@Autowired
	@Qualifier("DefaultAbsenceDataConsistencyRulesImpl")
	private IAbsenceDataConsistencyRules absenceDataConsistencyRulesImpl;

	@Autowired
	@Qualifier("AbsCongesAnnuelsDataConsistencyRulesImpl")
	private IAbsenceDataConsistencyRules absCongesAnnuelsDataConsistencyRulesImpl;

	@Autowired
	private HelperService helperService;

	@Autowired
	private IAgentMatriculeConverterService agentMatriculeService;

	@Autowired
	private CounterServiceFactory counterServiceFactory;

	@Autowired
	private ISirhWSConsumer sirhWSConsumer;

	@Autowired
	private IOrganisationSyndicaleRepository OSRepository;

	@Autowired
	private ICongesAnnuelsRepository congeAnnuelRepository;

	@Autowired
	private IReposCompensateurRepository reposCompensateurRepository;

	@Autowired
	private ITypeAbsenceRepository typeAbsenceRepository;

	@Autowired
	@Qualifier("typeEnv")
	private String typeEnvironnement;

	@Autowired
	private ICounterRepository counterRepository;

	@Autowired
	private IAgentService agentService;

	private static final String ETAT_DEMANDE_INCHANGE = "L'état de la demande est inchangé.";
	private static final String DEMANDE_INEXISTANTE = "La demande n'existe pas.";
	private static final String ETAT_DEMANDE_INCORRECT = "L'état de la demande envoyée n'est pas correct.";
	protected static final String BASE_CA_NON_TROUVEE = "Base congé non trouvée pour l'agent [%d].";
	protected static final String MAUVAIS_BASE_CA = "Mauvaise base congé pour l'agent [%d].";
	public static final String AGENT_NON_HABILITE = "L'agent n'est pas habilité pour cette opération.";
	protected static final String COMPTEUR_INEXISTANT_SPSOLD = "Le compteur de congé annuel n'existe pas.";
	protected static final String COMPTEUR_INEXISTANT_SPSORC = "Le compteur de repos compensateur n'existe pas.";
	public static final String STATUT_AGENT = "L'agent [%d] ne peut pas avoir de repos compensateur. Les repos compensateurs sont pour les contractuels ou les conventions collectives.";

	// POUR LES MESSAGE A ENVOYE AU PROJET SIRH-PTG-WS
	public static final String AVERT_MESSAGE_ABS = "%s : Soyez vigilant, vous avez pointé sur une absence de type '%s' pour l'agent %s.";
	public static final String RECUP_MSG = "%s : L'agent %s est en récupération sur cette période.";
	public static final String REPOS_COMP_MSG = "%s : L'agent %s est en repos compensateur sur cette période.";
	public static final String ASA_MSG = "%s : L'agent %s est en absence syndicale sur cette période.";
	public static final String CONGE_EXCEP_MSG = "%s : L'agent %s est en congé exceptionnel sur cette période.";
	public static final String CONGE_ANNUEL_MSG = "%s : L'agent %s est en congé annuel sur cette période.";

	@Override
	@Transactional(value = "absTransactionManager")
	public ReturnMessageDto saveDemande(Integer idAgent, DemandeDto demandeDto) {

		demandeRepository.setFlushMode(FlushModeType.COMMIT);
		ReturnMessageDto returnDto = new ReturnMessageDto();

		// verification des droits
		returnDto = accessRightsService.verifAccessRightDemande(idAgent, demandeDto.getAgentWithServiceDto()
				.getIdAgent(), returnDto);
		if (!returnDto.getErrors().isEmpty())
			return returnDto;

		Demande demande = null;
		Date dateJour = new Date();

		demande = mappingDemandeSpecifique(demandeDto, demande, idAgent, dateJour, returnDto);

		if (returnDto.getErrors().size() != 0) {
			demandeRepository.clear();
			return returnDto;
		}

		IAbsenceDataConsistencyRules absenceDataConsistencyRulesImpl = dataConsistencyRulesFactory.getFactory(
				demandeDto.getGroupeAbsence().getIdRefGroupeAbsence(), demandeDto.getIdTypeDemande());

		absenceDataConsistencyRulesImpl.processDataConsistencyDemande(returnDto, idAgent, demande, false);

		if (returnDto.getErrors().size() != 0) {
			demandeRepository.clear();
			return returnDto;
		}

		demandeRepository.persistEntity(demande);
		demandeRepository.flush();
		demandeRepository.clear();

		if (null == demandeDto.getIdDemande()) {
			returnDto.getInfos().add(String.format("La demande a bien été créée."));
		} else {
			returnDto.getInfos().add(String.format("La demande a bien été modifiée."));
		}

		return returnDto;
	}

	protected <T> T getDemande(Class<T> Tclass, Integer idDemande) {
		if (null != idDemande) {
			return demandeRepository.getEntity(Tclass, idDemande);
		}

		try {
			return Tclass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			return null;
		}
	}

	@Transactional(readOnly = true)
	public DemandeDto getDemandeDto(Integer idDemande) {
		DemandeDto demandeDto = null;

		Demande demande = demandeRepository.getEntity(Demande.class, idDemande);

		if (null == demande) {
			return demandeDto;
		}

		switch (RefTypeGroupeAbsenceEnum.getRefTypeGroupeAbsenceEnum(demande.getType().getGroupe()
				.getIdRefGroupeAbsence())) {
			case REPOS_COMP:

				DemandeReposComp demandeReposComp = demandeRepository.getEntity(DemandeReposComp.class, idDemande);
				if (null == demandeReposComp) {
					return demandeDto;
				}

				demandeDto = new DemandeDto(demandeReposComp, sirhWSConsumer.getAgentService(demande.getIdAgent(),
						helperService.getCurrentDate()));
				demandeDto.updateEtat(demandeReposComp.getLatestEtatDemande(), sirhWSConsumer.getAgentService(
						demandeReposComp.getLatestEtatDemande().getIdAgent(), helperService.getCurrentDate()),
						demandeReposComp.getType().getGroupe());
				break;
			case RECUP:

				DemandeRecup demandeRecup = demandeRepository.getEntity(DemandeRecup.class, idDemande);
				if (null == demandeRecup) {
					return demandeDto;
				}

				demandeDto = new DemandeDto(demandeRecup, sirhWSConsumer.getAgentService(demande.getIdAgent(),
						helperService.getCurrentDate()));
				demandeDto.updateEtat(
						demandeRecup.getLatestEtatDemande(),
						sirhWSConsumer.getAgentService(demandeRecup.getLatestEtatDemande().getIdAgent(),
								helperService.getCurrentDate()), demandeRecup.getType().getGroupe());
				break;
			case AS:
				DemandeAsa demandeAsa = demandeRepository.getEntity(DemandeAsa.class, idDemande);
				if (null == demandeAsa) {
					return demandeDto;
				}

				demandeDto = new DemandeDto(demandeAsa, sirhWSConsumer.getAgentService(demande.getIdAgent(),
						helperService.getCurrentDate()));
				demandeDto.updateEtat(
						demandeAsa.getLatestEtatDemande(),
						sirhWSConsumer.getAgentService(demandeAsa.getLatestEtatDemande().getIdAgent(),
								helperService.getCurrentDate()), demandeAsa.getType().getGroupe());
				break;
			case CONGES_EXCEP:
				DemandeCongesExceptionnels demandeCongesExcep = demandeRepository.getEntity(
						DemandeCongesExceptionnels.class, idDemande);
				if (null == demandeCongesExcep) {
					return demandeDto;
				}
				demandeDto = new DemandeDto(demandeCongesExcep, sirhWSConsumer.getAgentService(demande.getIdAgent(),
						helperService.getCurrentDate()));
				demandeDto.updateEtat(demandeCongesExcep.getLatestEtatDemande(), sirhWSConsumer.getAgentService(
						demandeCongesExcep.getLatestEtatDemande().getIdAgent(), helperService.getCurrentDate()),
						demandeCongesExcep.getType().getGroupe());
				break;
			case CONGES_ANNUELS:
				DemandeCongesAnnuels demandeCongesAnnuels = demandeRepository.getEntity(DemandeCongesAnnuels.class,
						idDemande);
				if (null == demandeCongesAnnuels) {
					return demandeDto;
				}
				demandeDto = new DemandeDto(demandeCongesAnnuels, sirhWSConsumer.getAgentService(demande.getIdAgent(),
						helperService.getCurrentDate()));
				demandeDto.updateEtat(demandeCongesAnnuels.getLatestEtatDemande(), sirhWSConsumer.getAgentService(
						demandeCongesAnnuels.getLatestEtatDemande().getIdAgent(), helperService.getCurrentDate()),
						demandeCongesAnnuels.getType().getGroupe());
				break;
			default:
				return demandeDto;
		}

		return demandeDto;
	}

	@Override
	@Transactional(readOnly = true)
	public List<DemandeDto> getListeDemandes(Integer idAgentConnecte, Integer idAgentConcerne, String ongletDemande,
			Date fromDate, Date toDate, Date dateDemande, String listIdRefEtat, Integer idRefType,
			Integer idRefGroupeAbsence, boolean isAgent) {

		// si date de debut et de fin nulles, alors on filtre sur 12 mois
		// glissants

		if (null == fromDate && null == toDate) {
			fromDate = helperService.getCurrentDateMoinsUnAn();
		}

		List<Demande> listeSansFiltre = getListeNonFiltreeDemandes(idAgentConnecte, idAgentConcerne, fromDate, toDate,
				idRefType, idRefGroupeAbsence);

		List<Integer> etatIds = new ArrayList<Integer>();
		if (listIdRefEtat != null) {
			for (String id : listIdRefEtat.split(",")) {
				etatIds.add(Integer.valueOf(id));
			}
		}

		List<RefEtat> listEtats = filtresService.getListeEtatsByOnglet(ongletDemande, etatIds);

		List<DemandeDto> listeDto = absenceDataConsistencyRulesImpl.filtreDateAndEtatDemandeFromList(listeSansFiltre,
				listEtats, dateDemande);

		// si idAgentConnecte == idAgentConcerne, alors nous sommes dans le cas
		// du WS listeDemandesAgent
		// donc inutile de recuperer les droits en bdd
		List<DroitsAgent> listDroitAgent = new ArrayList<DroitsAgent>();
		if (null != idAgentConnecte && !idAgentConnecte.equals(idAgentConcerne)) {

			// redmine #14201 : on cherche si l'agent est délégataire
			List<Integer> idsApprobateurOfDelegataire = accessRightsService.getIdApprobateurOfDelegataire(
					idAgentConnecte, null);

			List<Integer> idsUserForAllDroits = new ArrayList<Integer>();
			idsUserForAllDroits.add(idAgentConnecte);

			if (idsApprobateurOfDelegataire != null) {
				idsUserForAllDroits.addAll(idsApprobateurOfDelegataire);
			}

			listDroitAgent.addAll(accessRightsRepository.getListOfAgentsForListDemandes(idsUserForAllDroits, null));
		}

		for (DemandeDto demandeDto : listeDto) {
			IAbsenceDataConsistencyRules absenceDataConsistencyRulesImpl = dataConsistencyRulesFactory.getFactory(
					demandeDto.getGroupeAbsence().getIdRefGroupeAbsence(), demandeDto.getIdTypeDemande());
			demandeDto = absenceDataConsistencyRulesImpl.filtreDroitOfDemande(idAgentConnecte, demandeDto,
					listDroitAgent, isAgent);
			demandeDto
					.setDepassementCompteur(absenceDataConsistencyRulesImpl.checkDepassementCompteurAgent(demandeDto));
			demandeDto
					.setDepassementMultiple(absenceDataConsistencyRulesImpl.checkDepassementMultipleAgent(demandeDto));
		}
		return listeDto;
	}

	protected List<Demande> getListeNonFiltreeDemandes(Integer idAgentConnecte, Integer idAgentConcerne, Date fromDate,
			Date toDate, Integer idRefType, Integer idRefGroupeAbsence) {

		List<Demande> listeSansFiltre = new ArrayList<Demande>();
		List<Demande> listeSansFiltredelegataire = new ArrayList<Demande>();

		List<Integer> idsApprobateurOfDelegataire = accessRightsService.getIdApprobateurOfDelegataire(idAgentConnecte,
				idAgentConcerne);

		listeSansFiltre = demandeRepository.listeDemandesAgent(idAgentConnecte, idAgentConcerne, fromDate, toDate,
				idRefType, idRefGroupeAbsence);
		if (null != idsApprobateurOfDelegataire) {
			for (Integer idApprobateurOfDelegataire : idsApprobateurOfDelegataire) {
				listeSansFiltredelegataire.addAll(demandeRepository.listeDemandesAgent(idApprobateurOfDelegataire,
						idAgentConcerne, fromDate, toDate, idRefType, idRefGroupeAbsence));
			}
		}

		for (Demande demandeDeleg : listeSansFiltredelegataire) {
			if (!listeSansFiltre.contains(demandeDeleg)) {
				listeSansFiltre.add(demandeDeleg);
			}
		}

		return listeSansFiltre;
	}

	@Override
	@Transactional(value = "chainedTransactionManager")
	public ReturnMessageDto setDemandeEtat(Integer idAgent, DemandeEtatChangeDto demandeEtatChangeDto) {

		ReturnMessageDto result = new ReturnMessageDto();

		if (!demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat())
				&& !demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat())
				&& !demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.APPROUVEE.getCodeEtat())
				&& !demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.REFUSEE.getCodeEtat())
				&& !demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.ANNULEE.getCodeEtat())) {

			logger.warn(ETAT_DEMANDE_INCORRECT);
			result.getErrors().add(String.format(ETAT_DEMANDE_INCORRECT));
			return result;
		}

		Demande demande = getDemande(Demande.class, demandeEtatChangeDto.getIdDemande());

		if (null == demande) {
			logger.warn(DEMANDE_INEXISTANTE);
			result.getErrors().add(String.format(DEMANDE_INEXISTANTE));
			return result;
		}

		if (null != demande.getLatestEtatDemande()
				&& demandeEtatChangeDto.getIdRefEtat().equals(demande.getLatestEtatDemande().getEtat().getCodeEtat())) {
			logger.warn(ETAT_DEMANDE_INCHANGE);
			result.getErrors().add(String.format(ETAT_DEMANDE_INCHANGE));
			return result;
		}

		if (demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat())
				|| demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat())) {

			return setDemandeEtatVisa(idAgent, demandeEtatChangeDto, demande, result);
		}

		if (demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.APPROUVEE.getCodeEtat())
				|| demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.REFUSEE.getCodeEtat())) {

			return setDemandeEtatApprouve(idAgent, demandeEtatChangeDto, demande, result);
		}

		if (demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.ANNULEE.getCodeEtat())) {
			// on verifie les droits
			// verification des droits
			result = accessRightsService.verifAccessRightDemande(idAgent, demande.getIdAgent(), result);
			if (!result.getErrors().isEmpty())
				return result;

			return setDemandeEtatAnnule(idAgent, demandeEtatChangeDto, demande, result);
		}

		return result;
	}

	protected ReturnMessageDto setDemandeEtatVisa(Integer idAgent, DemandeEtatChangeDto demandeEtatChangeDto,
			Demande demande, ReturnMessageDto result) {

		// on verifie les droits
		if (!accessRightsRepository.isViseurOfAgent(idAgent, demande.getIdAgent())) {
			logger.warn("L'agent Viseur n'est pas habilité pour viser la demande de cet agent.");
			result.getErrors().add(
					String.format("L'agent Viseur n'est pas habilité pour viser la demande de cet agent."));
			return result;
		}

		// on verifie l etat de la demande
		result = absenceDataConsistencyRulesImpl.checkEtatsDemandeAcceptes(result, demande,
				Arrays.asList(RefEtatEnum.SAISIE, RefEtatEnum.VISEE_FAVORABLE, RefEtatEnum.VISEE_DEFAVORABLE));

		result = absenceDataConsistencyRulesImpl.checkChampMotifPourEtatDonne(result,
				demandeEtatChangeDto.getIdRefEtat(), demandeEtatChangeDto.getMotif());

		result = absenceDataConsistencyRulesImpl.checkSaisieKiosqueAutorisee(result, demande.getType().getTypeSaisi(),
				false);

		if (0 < result.getErrors().size()) {
			return result;
		}

		// maj de la demande
		majEtatDemande(idAgent, demandeEtatChangeDto, demande, false);

		if (demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat())) {
			result.getInfos().add(String.format("La demande est visée favorablement."));
		}
		if (demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat())) {
			result.getInfos().add(String.format("La demande est visée défavorablement."));
		}

		return result;
	}

	protected ReturnMessageDto setDemandeEtatApprouve(Integer idAgent, DemandeEtatChangeDto demandeEtatChangeDto,
			Demande demande, ReturnMessageDto result) {

		// on verifie les droits
		if (!accessRightsRepository.isApprobateurOrDelegataireOfAgent(idAgent, demande.getIdAgent())) {
			logger.warn("L'agent Approbateur n'est pas habilité à approuver la demande de cet agent.");
			result.getErrors().add(
					String.format("L'agent Approbateur n'est pas habilité à approuver la demande de cet agent."));
			return result;
		}

		result = absenceDataConsistencyRulesImpl.checkEtatsDemandeAcceptes(result, demande, Arrays.asList(
				RefEtatEnum.SAISIE, RefEtatEnum.VISEE_FAVORABLE, RefEtatEnum.VISEE_DEFAVORABLE, RefEtatEnum.APPROUVEE,
				RefEtatEnum.REFUSEE));

		result = absenceDataConsistencyRulesImpl.checkChampMotifPourEtatDonne(result,
				demandeEtatChangeDto.getIdRefEtat(), demandeEtatChangeDto.getMotif());

		if (demande.getType().getTypeSaisi() != null) {
			result = absenceDataConsistencyRulesImpl.checkSaisieKiosqueAutorisee(result, demande.getType()
					.getTypeSaisi(), false);
		}

		if (0 < result.getErrors().size()) {
			return result;
		}

		// #12664
		if (!demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.REFUSEE.getCodeEtat()))
			absenceDataConsistencyRulesImpl.processDataConsistencyDemande(result, idAgent, demande, false);

		if (result.getErrors().size() != 0) {
			return result;
		}

		// #13362 dans le cadre des congés annuels, on regarde si le samedi
		// offert n est pas offert une autre demande
		IAbsenceDataConsistencyRules absenceDataConsistencyRulesImpl = dataConsistencyRulesFactory.getFactory(demande
				.getType().getGroupe().getIdRefGroupeAbsence(), demande.getType().getIdRefTypeAbsence());
		absenceDataConsistencyRulesImpl.checkSamediOffertToujoursOk(demandeEtatChangeDto, demande);

		ReturnMessageDto srmIsDepassementCompteurCA = new ReturnMessageDto();
		srmIsDepassementCompteurCA = absenceDataConsistencyRulesImpl.checkDepassementDroitsAcquis(
				srmIsDepassementCompteurCA, demande);
		boolean isDepassementCA = false;
		if (0 < srmIsDepassementCompteurCA.getInfos().size()) {
			isDepassementCA = true;
		}

		ICounterService counterService = counterServiceFactory.getFactory(demande.getType().getGroupe()
				.getIdRefGroupeAbsence(), demande.getType().getIdRefTypeAbsence());
		result = counterService.majCompteurToAgent(result, demande, demandeEtatChangeDto);

		if (0 < result.getErrors().size()) {
			return result;
		}

		// maj de la demande
		majEtatDemande(idAgent, demandeEtatChangeDto, demande, isDepassementCA);

		if (demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.REFUSEE.getCodeEtat())) {
			result.getInfos().add(String.format("La demande est refusée."));
		}
		if (demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.APPROUVEE.getCodeEtat())) {
			result.getInfos().add(String.format("La demande est approuvée."));
		}
		if (demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.A_VALIDER.getCodeEtat())) {
			result.getInfos().add(String.format("La demande est en attente de validation par la DRH."));
		}

		return result;
	}

	protected ReturnMessageDto setDemandeEtatAnnule(Integer idAgent, DemandeEtatChangeDto demandeEtatChangeDto,
			Demande demande, ReturnMessageDto result) {

		// redmine #12994 : bloque ce job si une paye est en cours pour les
		// congés annuels
		ReturnMessageDto paieEnCours = sirhWSConsumer.isPaieEnCours();
		if (demande.getType().getGroupe().getIdRefGroupeAbsence() == RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue()
				&& paieEnCours.getErrors().size() > 0) {
			result.getErrors()
					.add("Vous ne pouvez annuler cette demande car un calcul de salaire est en cours. Merci de réessayer ultérieurement.");
		} else {
			IAbsenceDataConsistencyRules absenceDataConsistencyRulesImpl = dataConsistencyRulesFactory.getFactory(
					demande.getType().getGroupe().getIdRefGroupeAbsence(), demande.getType().getIdRefTypeAbsence());

			result = absenceDataConsistencyRulesImpl.checkEtatsDemandeAnnulee(result, demande, Arrays.asList(
					RefEtatEnum.VISEE_FAVORABLE, RefEtatEnum.VISEE_DEFAVORABLE, RefEtatEnum.APPROUVEE,
					RefEtatEnum.A_VALIDER));

			result = absenceDataConsistencyRulesImpl.checkChampMotifPourEtatDonne(result,
					demandeEtatChangeDto.getIdRefEtat(), demandeEtatChangeDto.getMotif());

			if (0 < result.getErrors().size()) {
				return result;
			}

			ICounterService counterService = counterServiceFactory.getFactory(demande.getType().getGroupe()
					.getIdRefGroupeAbsence(), demande.getType().getIdRefTypeAbsence());
			result = counterService.majCompteurToAgent(result, demande, demandeEtatChangeDto);

			if (0 < result.getErrors().size()) {
				return result;
			}

			// redmine #12994 : on traite l'incidence en paie dans le cas d'un
			// congé annuel
			supprimeIncidencePaie(demande);

			// maj de la demande
			majEtatDemande(idAgent, demandeEtatChangeDto, demande, false);

			result.getInfos().add(String.format("La demande est annulée."));
		}

		return result;
	}

	private void majEtatDemande(Integer idAgent, DemandeEtatChangeDto demandeEtatChangeDto, Demande demande,
			boolean isDepassementCA) {

		if (demande.getType() != null && demande.getType().getTypeSaisi() == null
				&& demande.getType().getTypeSaisiCongeAnnuel() != null) {
			// cas des congés annuels
			if (isDepassementCA && demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.APPROUVEE.getCodeEtat())) {
				EtatDemande etatDemande = new EtatDemande();

				etatDemande = mappingEtatDemandeSpecifique(etatDemande, demande, new ReturnMessageDto(), demande
						.getType().getGroupe().getIdRefGroupeAbsence());
				etatDemande.setDate(new Date());
				etatDemande.setMotif(demandeEtatChangeDto.getMotif());
				etatDemande.setEtat(RefEtatEnum.A_VALIDER);
				demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.A_VALIDER.getCodeEtat());
				etatDemande.setIdAgent(idAgent);
				demande.addEtatDemande(etatDemande);
			} else {
				EtatDemande etatDemande = new EtatDemande();
				etatDemande = mappingEtatDemandeSpecifique(etatDemande, demande, new ReturnMessageDto(), demande
						.getType().getGroupe().getIdRefGroupeAbsence());
				etatDemande.setDate(new Date());
				etatDemande.setMotif(demandeEtatChangeDto.getMotif());
				etatDemande.setEtat(RefEtatEnum.getRefEtatEnum(demandeEtatChangeDto.getIdRefEtat()));
				etatDemande.setIdAgent(idAgent);
				demande.addEtatDemande(etatDemande);
			}
		} else {
			EtatDemande etatDemande = new EtatDemande();
			etatDemande = mappingEtatDemandeSpecifique(etatDemande, demande, new ReturnMessageDto(), demande.getType()
					.getGroupe().getIdRefGroupeAbsence());
			etatDemande.setDate(new Date());
			etatDemande.setMotif(demandeEtatChangeDto.getMotif());
			etatDemande.setEtat(RefEtatEnum.getRefEtatEnum(demandeEtatChangeDto.getIdRefEtat()));
			etatDemande.setIdAgent(idAgent);
			demande.addEtatDemande(etatDemande);
		}
	}

	@Override
	@Transactional(value = "chainedTransactionManager")
	public ReturnMessageDto setDemandeEtatPris(Integer idDemande) {

		logger.info("Trying to update demande id {} to Etat PRISE...", idDemande);

		ReturnMessageDto result = new ReturnMessageDto();

		// on cherche la demande
		Demande demande = getDemande(Demande.class, idDemande);
		if (demande == null) {
			result.getErrors().add(String.format("La demande %s n'existe pas.", idDemande));
			logger.error("Demande id {} does not exists. Stopping process.", idDemande);
			return result;
		}

		switch (RefTypeGroupeAbsenceEnum.getRefTypeGroupeAbsenceEnum((demande.getType().getGroupe()
				.getIdRefGroupeAbsence()))) {
			case REPOS_COMP:
			case RECUP:
				if (demande.getLatestEtatDemande().getEtat() != RefEtatEnum.APPROUVEE) {
					result.getErrors().add(
							String.format("La demande %s n'est pas à l'état %s mais %s.", idDemande,
									RefEtatEnum.APPROUVEE.toString(), demande.getLatestEtatDemande().getEtat()
											.toString()));
					logger.error("Demande id {} is not in state [{}] but [{}]. Stopping process.", idDemande,
							RefEtatEnum.APPROUVEE.toString(), demande.getLatestEtatDemande().getEtat().toString());
					return result;
				}
				break;
			case CONGES_EXCEP:
			case AS:
				if (demande.getLatestEtatDemande().getEtat() != RefEtatEnum.VALIDEE) {
					result.getErrors()
							.add(String
									.format("La demande %s n'est pas à l'état %s mais %s.", idDemande,
											RefEtatEnum.VALIDEE.toString(), demande.getLatestEtatDemande().getEtat()
													.toString()));
					logger.error("Demande id {} is not in state [{}] but [{}]. Stopping process.", idDemande,
							RefEtatEnum.VALIDEE.toString(), demande.getLatestEtatDemande().getEtat().toString());
					return result;
				}
				break;
			case CONGES_ANNUELS:
				if (!(demande.getLatestEtatDemande().getEtat() == RefEtatEnum.VALIDEE || demande.getLatestEtatDemande()
						.getEtat() == RefEtatEnum.APPROUVEE)) {
					result.getErrors().add(
							String.format("La demande %s n'est pas à l'état %s mais %s.", idDemande,
									RefEtatEnum.VALIDEE.toString() + " ou " + RefEtatEnum.APPROUVEE.toString(), demande
											.getLatestEtatDemande().getEtat().toString()));
					logger.error("Demande id {} is not in state [{}] but [{}]. Stopping process.", idDemande,
							RefEtatEnum.VALIDEE.toString() + " ou " + RefEtatEnum.APPROUVEE.toString(), demande
									.getLatestEtatDemande().getEtat().toString());
					return result;
				}

				result = traiteIncidencePaie(demande, result);
				if (result.getErrors().size() > 0) {
					return result;
				}

				break;
			default:
				break;
		}
		ReturnMessageDto returnDto = new ReturnMessageDto();
		EtatDemande epNew = new EtatDemande();
		epNew = mappingEtatDemandeSpecifique(epNew, demande, returnDto, demande.getType().getGroupe()
				.getIdRefGroupeAbsence());
		epNew.setDemande(demande);
		epNew.setDate(helperService.getCurrentDate());
		epNew.setEtat(RefEtatEnum.PRISE);
		epNew.setIdAgent(demande.getIdAgent());

		if (returnDto.getErrors().size() != 0) {
			demandeRepository.clear();
			return returnDto;
		}
		demande.addEtatDemande(epNew);

		// insert nouvelle ligne EtatAbsence avec nouvel etat
		demandeRepository.persistEntity(epNew);

		logger.info("Updated demande id {}.", idDemande);

		return result;
	}

	/**
	 * Pour les contractuels et CC uniquement : Les demandes à l’état « prise »
	 * ont déjà été injectées dans la paye. Les demandes annulées doivent être
	 * supprimées de SPCC : supprimer toutes les lignes concernées par le congé
	 * annulé. Une ligne doit être créée dans SPMATR.
	 */
	protected void supprimeIncidencePaie(Demande demande) {

		// uniquement pour les conges annuels
		if (!demande.getType().getGroupe().getIdRefGroupeAbsence()
				.equals(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue())) {
			return;
		}

		SimpleDateFormat sdfMairiePerrap = new SimpleDateFormat("yyyyMM");

		DateTime dateTimeDebut = new DateTime(demande.getDateDebut());
		DateTime dateTimeFin = new DateTime(demande.getDateFin());

		if (dateTimeDebut.getDayOfMonth() == dateTimeFin.getDayOfMonth()) {
			supprimeSpcc(demande, dateTimeDebut.toDate(), new Integer(sdfMairiePerrap.format(dateTimeDebut.toDate())),
					isDemiJourneeForSpcc(dateTimeDebut.toDate(), dateTimeFin.toDate()));

		} else {
			// ////////////////////////////////
			// plusieurs journées de posées //
			// ////////////////////////////////

			// on traite le premier jour
			supprimeSpcc(demande, demande.getDateDebut(), new Integer(sdfMairiePerrap.format(demande.getDateDebut())),
					isDemiJourneeForSpcc(demande.getDateDebut(), null));

			dateTimeDebut = dateTimeDebut.plusDays(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);

			// ///////////////////////////
			// on traite le dernier jour
			supprimeSpcc(demande, demande.getDateFin(), new Integer(sdfMairiePerrap.format(demande.getDateFin())),
					isDemiJourneeForSpcc(null, demande.getDateFin()));

			dateTimeFin = dateTimeFin.minusDays(1).withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59);

			// //////////////////////
			// on traite le reste
			while (dateTimeDebut.isBefore(dateTimeFin)) {

				supprimeSpcc(demande, dateTimeDebut.toDate(),
						new Integer(sdfMairiePerrap.format(dateTimeDebut.toDate())), false);

				dateTimeDebut = dateTimeDebut.plusDays(1);
			}
		}
	}

	/**
	 * Envoie a la paie
	 */
	protected ReturnMessageDto traiteIncidencePaie(Demande demande, ReturnMessageDto result) {
		// dans le cas des congés annuels, il faut regarder si l'agent
		// est Convention ou contractuel pour gerer l'incidence en paie
		// on fait cette demarche pour chaque jour de la demande de
		// congé annuel

		// uniquement pour les conges annuels
		if (!demande.getType().getGroupe().getIdRefGroupeAbsence()
				.equals(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue())) {
			return result;
		}

		DateTime dateTimeDebut = new DateTime(demande.getDateDebut());
		DateTime dateTimeFin = new DateTime(demande.getDateFin());

		SimpleDateFormat sdfMairiePerrap = new SimpleDateFormat("yyyyMM");

		// ///////////////////////////////
		// si 1 seule journée de posée //
		// ///////////////////////////////
		if (dateTimeDebut.getDayOfMonth() == dateTimeFin.getDayOfMonth()) {
			result = creeSpcc(result, demande, dateTimeDebut.toDate(),
					new Integer(sdfMairiePerrap.format(dateTimeDebut.toDate())),
					isDemiJourneeForSpcc(dateTimeDebut.toDate(), dateTimeFin.toDate()));

			if (!result.getErrors().isEmpty())
				return result;

		} else {
			// ////////////////////////////////
			// plusieurs journées de posées //
			// ////////////////////////////////

			// on traite le premier jour
			result = creeSpcc(result, demande, demande.getDateDebut(),
					new Integer(sdfMairiePerrap.format(demande.getDateDebut())),
					isDemiJourneeForSpcc(demande.getDateDebut(), null));

			if (!result.getErrors().isEmpty())
				return result;

			dateTimeDebut = dateTimeDebut.plusDays(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);

			// ///////////////////////////
			// on traite le dernier jour
			result = creeSpcc(result, demande, demande.getDateFin(),
					new Integer(sdfMairiePerrap.format(demande.getDateFin())),
					isDemiJourneeForSpcc(null, demande.getDateFin()));

			if (!result.getErrors().isEmpty())
				return result;

			dateTimeFin = dateTimeFin.minusDays(1).withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59);

			// //////////////////////
			// on traite le reste
			while (dateTimeDebut.isBefore(dateTimeFin)) {

				result = creeSpcc(result, demande, dateTimeDebut.toDate(),
						new Integer(sdfMairiePerrap.format(dateTimeDebut.toDate())), false);

				if (!result.getErrors().isEmpty())
					return result;

				dateTimeDebut = dateTimeDebut.plusDays(1);
			}

		}
		return result;
	}

	/**
	 * verifie si demi journee ou journee entiere
	 */
	private boolean isDemiJourneeForSpcc(Date dateDebut, Date dateFin) {

		if (null == dateFin && null != dateDebut && 12 == new DateTime(dateDebut).getHourOfDay()) {
			return true;
		}

		if (null == dateDebut && null != dateFin && 11 == new DateTime(dateFin).getHourOfDay()) {
			return true;
		}

		if ((0 == new DateTime(dateDebut).getHourOfDay() && 11 == new DateTime(dateFin).getHourOfDay())
				|| (12 == new DateTime(dateDebut).getHourOfDay() && 23 == new DateTime(dateFin).getHourOfDay())) {
			return true;
		}
		return false;
	}

	/**
	 * log les erreurs
	 */
	private ReturnMessageDto setErrorIncidencePaie(ReturnMessageDto result, Integer idDemande, Integer idAgent,
			Date date) {

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

		result.getErrors()
				.add(String
						.format("La demande %s de l'agent %s ne peut pas passer à l'état pris car celui-ci n'a pas de carrière en cours à la date %s.",
								idDemande, idAgent, sdf.format(date)));
		logger.error(
				"Demande id {} de l'agent {} ne peut pas passer à l'état pris car celui-ci n'a pas de carrière en cours à la date {}. Stopping process.",
				idDemande, idAgent, sdf.format(date));

		return result;
	}

	/**
	 * Cree SPCC et met a jour SPMATR pour un unique jour donne
	 * 
	 * @param result
	 *            ReturnMessageDto
	 * @param demande
	 *            Demande
	 * @param datjou
	 *            Date
	 * @param perrap
	 *            Integer
	 * @param isDemijournee
	 *            boolean
	 * @return ReturnMessageDto
	 */
	private ReturnMessageDto creeSpcc(ReturnMessageDto result, Demande demande, Date datjou, Integer perrap,
			boolean isDemijournee) {

		Spcarr carr = sirhRepository.getAgentCurrentCarriere(
				agentMatriculeService.fromIdAgentToSIRHNomatrAgent(demande.getIdAgent()), datjou);

		if (carr == null) {
			return setErrorIncidencePaie(result, demande.getIdDemande(), demande.getIdAgent(), datjou);
		} else if (helperService.isContractuel(carr) || helperService.isConventionCollective(carr)) {

			SimpleDateFormat sdfMairie = new SimpleDateFormat("yyyyMMdd");

			Spcc spcc = sirhRepository.getSpcc(
					agentMatriculeService.fromIdAgentToSIRHNomatrAgent(demande.getIdAgent()), datjou);

			if (null == spcc) {
				SpccId spccId = new SpccId();
				spccId.setNomatr(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(demande.getIdAgent()));
				spccId.setDatjou(new Integer(sdfMairie.format(datjou)));
				spcc = new Spcc();
				spcc.setId(spccId);
			}

			// journee entiere, code = 1
			// demi journee, code = 2
			spcc.setCode(isDemijournee ? 2 : 1);
			// on met à jour SPMATR
			Spmatr matr = miseAjourSpmatr(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(demande.getIdAgent()),
					perrap, carr);

			sirhRepository.persistEntity(spcc);
			sirhRepository.persistEntity(matr);
		}
		return result;
	}

	private void supprimeSpcc(Demande demande, Date datjou, Integer perrap, boolean isDemijournee) {

		Spcarr carr = sirhRepository.getAgentCurrentCarriere(
				agentMatriculeService.fromIdAgentToSIRHNomatrAgent(demande.getIdAgent()), datjou);

		if (carr == null) {
			return;
		}

		Integer code = isDemijournee ? 2 : 1;

		Spcc spcc = sirhRepository.getSpcc(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(demande.getIdAgent()),
				datjou, code);

		if (null != spcc) {
			sirhRepository.removeEntity(spcc);
			// on met à jour SPMATR
			Spmatr matr = miseAjourSpmatr(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(demande.getIdAgent()),
					perrap, carr);
			sirhRepository.persistEntity(matr);
		}
	}

	/**
	 * Met a jour SPMATR
	 * 
	 * @param nomatr
	 *            Integer
	 * @param perrap
	 *            Integer
	 * @param carr
	 *            Spcarr
	 * @return Spmatr
	 */
	private Spmatr miseAjourSpmatr(Integer nomatr, Integer perrap, Spcarr carr) {

		Spmatr matr = sirhRepository.findSpmatrForAgent(nomatr);

		if (matr == null) {
			TypeChainePaieEnum chainePaie = helperService.getTypeChainePaieFromStatut(carr);
			matr = new Spmatr();
			matr.setNomatr(nomatr);
			matr.setPerrap(perrap);
			matr.setTypeChainePaie(chainePaie);
			return matr;
		}

		if (matr.getPerrap() > perrap) {
			matr.setPerrap(perrap);
		}
		return matr;
	}

	private EtatDemande mappingEtatDemandeSpecifique(EtatDemande etatDemande, Demande demande,
			ReturnMessageDto returnDto, Integer idRefGroupeAbsence) {
		switch (RefTypeGroupeAbsenceEnum.getRefTypeGroupeAbsenceEnum(idRefGroupeAbsence)) {

			case REPOS_COMP:
				DemandeReposComp demandeReposComp = (DemandeReposComp) demande;
				EtatDemandeReposComp etatDemandeReposComp = new EtatDemandeReposComp();
				etatDemande = (EtatDemande) etatDemandeReposComp;
				etatDemandeReposComp.setDateDebut(demandeReposComp.getDateDebut());
				etatDemandeReposComp.setDateFin(demandeReposComp.getDateFin());
				etatDemandeReposComp.setDuree(demandeReposComp.getDuree());
				etatDemandeReposComp.setDureeAnneeN1(demandeReposComp.getDureeAnneeN1());
				etatDemandeReposComp.setTotalMinutesAnneeN1Old(demandeReposComp.getTotalMinutesAnneeN1Old());
				etatDemandeReposComp.setTotalMinutesAnneeN1New(demandeReposComp.getTotalMinutesAnneeN1New());
				etatDemandeReposComp.setTotalMinutesOld(demandeReposComp.getTotalMinutesOld());
				etatDemandeReposComp.setTotalMinutesNew(demandeReposComp.getTotalMinutesNew());
				break;
			case RECUP:
				DemandeRecup demandeRecup = (DemandeRecup) demande;
				EtatDemandeRecup etatDemandeRecup = new EtatDemandeRecup();
				etatDemande = (EtatDemande) etatDemandeRecup;
				etatDemandeRecup.setDateDebut(demandeRecup.getDateDebut());
				etatDemandeRecup.setDateFin(demandeRecup.getDateFin());
				etatDemandeRecup.setDuree(demandeRecup.getDuree());
				etatDemandeRecup.setTotalMinutesOld(demandeRecup.getTotalMinutesOld());
				etatDemandeRecup.setTotalMinutesNew(demandeRecup.getTotalMinutesNew());
				break;
			case AS:
				DemandeAsa demandeAsa = (DemandeAsa) demande;
				EtatDemandeAsa etatDemandeAsa = new EtatDemandeAsa();
				etatDemande = (EtatDemande) etatDemandeAsa;
				etatDemandeAsa.setDateDebut(demandeAsa.getDateDebut());
				etatDemandeAsa.setDateFin(demandeAsa.getDateFin());
				etatDemandeAsa.setDuree(demandeAsa.getDuree());
				etatDemandeAsa.setDateDebutAM(demandeAsa.isDateDebutAM());
				etatDemandeAsa.setDateDebutPM(demandeAsa.isDateDebutPM());
				etatDemandeAsa.setDateFinAM(demandeAsa.isDateFinAM());
				etatDemandeAsa.setDateFinPM(demandeAsa.isDateFinPM());
				if (demandeAsa.getOrganisationSyndicale() != null)
					etatDemandeAsa.setOrganisationSyndicale(demandeAsa.getOrganisationSyndicale());

				etatDemandeAsa.setTotalMinutesOld(demandeAsa.getTotalMinutesOld());
				etatDemandeAsa.setTotalMinutesNew(demandeAsa.getTotalMinutesNew());
				etatDemandeAsa.setTotalJoursOld(demandeAsa.getTotalJoursOld());
				etatDemandeAsa.setTotalJoursNew(demandeAsa.getTotalJoursNew());
				break;
			case CONGES_EXCEP:
				DemandeCongesExceptionnels demandeCongeExcep = (DemandeCongesExceptionnels) demande;
				EtatDemandeCongesExceptionnels etatDemandeCongeExcep = new EtatDemandeCongesExceptionnels();
				etatDemande = (EtatDemande) etatDemandeCongeExcep;
				etatDemandeCongeExcep.setDateDebut(demandeCongeExcep.getDateDebut());
				etatDemandeCongeExcep.setDateFin(demandeCongeExcep.getDateFin());
				etatDemandeCongeExcep.setDuree(demandeCongeExcep.getDuree());
				etatDemandeCongeExcep.setDateDebutAM(demandeCongeExcep.isDateDebutAM());
				etatDemandeCongeExcep.setDateDebutPM(demandeCongeExcep.isDateDebutPM());
				etatDemandeCongeExcep.setDateFinAM(demandeCongeExcep.isDateFinAM());
				etatDemandeCongeExcep.setDateFinPM(demandeCongeExcep.isDateFinPM());
				etatDemandeCongeExcep.setCommentaire(demandeCongeExcep.getCommentaire());
				break;
			case CONGES_ANNUELS:
				DemandeCongesAnnuels demandeCongeAnnuel = (DemandeCongesAnnuels) demande;
				EtatDemandeCongesAnnuels etatDemandeCongeAnnuel = new EtatDemandeCongesAnnuels();
				etatDemande = (EtatDemande) etatDemandeCongeAnnuel;
				etatDemandeCongeAnnuel.setDateDebut(demandeCongeAnnuel.getDateDebut());
				etatDemandeCongeAnnuel.setDateFin(demandeCongeAnnuel.getDateFin());
				etatDemandeCongeAnnuel.setDuree(demandeCongeAnnuel.getDuree());
				etatDemandeCongeAnnuel.setDureeAnneeN1(demandeCongeAnnuel.getDureeAnneeN1());
				etatDemandeCongeAnnuel.setDateDebutAM(demandeCongeAnnuel.isDateDebutAM());
				etatDemandeCongeAnnuel.setDateDebutPM(demandeCongeAnnuel.isDateDebutPM());
				etatDemandeCongeAnnuel.setDateFinAM(demandeCongeAnnuel.isDateFinAM());
				etatDemandeCongeAnnuel.setDateFinPM(demandeCongeAnnuel.isDateFinPM());
				etatDemandeCongeAnnuel.setNbSamediDecompte(demandeCongeAnnuel.getNbSamediDecompte());
				etatDemandeCongeAnnuel.setNbSamediOffert(demandeCongeAnnuel.getNbSamediOffert());
				etatDemandeCongeAnnuel.setCommentaire(demandeCongeAnnuel.getCommentaire());
				etatDemandeCongeAnnuel.setTypeSaisiCongeAnnuel(demandeCongeAnnuel.getTypeSaisiCongeAnnuel());
				etatDemandeCongeAnnuel.setTotalJoursAnneeN1Old(demandeCongeAnnuel.getTotalJoursAnneeN1Old());
				etatDemandeCongeAnnuel.setTotalJoursAnneeN1New(demandeCongeAnnuel.getTotalJoursAnneeN1New());
				etatDemandeCongeAnnuel.setTotalJoursOld(demandeCongeAnnuel.getTotalJoursOld());
				etatDemandeCongeAnnuel.setTotalJoursNew(demandeCongeAnnuel.getTotalJoursNew());
				break;
			default:
				break;
		}
		return etatDemande;
	}

	@Override
	@Transactional(value = "absTransactionManager")
	public ReturnMessageDto saveDemandeSIRH(Integer idAgent, DemandeDto demandeDto) {

		demandeRepository.setFlushMode(FlushModeType.COMMIT);
		ReturnMessageDto returnDto = new ReturnMessageDto();

		// verification des droits SIRH
		ReturnMessageDto isUtilisateurSIRH = sirhWSConsumer.isUtilisateurSIRH(idAgent);
		if (!isUtilisateurSIRH.getErrors().isEmpty()) {
			logger.warn("L'agent n'est pas habilité à saisir une demande.");
			returnDto.getErrors().add(String.format("L'agent n'est pas habilité à saisir une demande."));
			return returnDto;
		}

		Demande demande = null;
		Date dateJour = new Date();

		demande = mappingDemandeSpecifique(demandeDto, demande, idAgent, dateJour, returnDto);

		if (returnDto.getErrors().size() != 0) {
			demandeRepository.clear();
			return returnDto;
		}

		// si la demande provient de SIRH, et que la SAISIE KIOSQUE dans la
		// table de parametrage est a FALSE
		// l etat de la demande passe automatiquement a VALIDE en ajoutant une
		// ligne dans la table ABS_ETAT_DEMANDE
		if (demande.getType().getTypeSaisi() != null && !demande.getType().getTypeSaisi().isSaisieKiosque()) {
			DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
			demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());
			demandeEtatChangeDto.setMotif(null);
			majEtatDemande(idAgent, demandeEtatChangeDto, demande, false);
		}

		IAbsenceDataConsistencyRules absenceDataConsistencyRulesImpl = dataConsistencyRulesFactory.getFactory(
				demandeDto.getGroupeAbsence().getIdRefGroupeAbsence(), demandeDto.getIdTypeDemande());

		absenceDataConsistencyRulesImpl.processDataConsistencyDemande(returnDto, idAgent, demande, true);

		if (returnDto.getErrors().size() != 0) {
			demandeRepository.clear();
			return returnDto;
		}

		demandeRepository.persistEntity(demande);
		demandeRepository.flush();
		demandeRepository.clear();

		if (null == demandeDto.getIdDemande()) {
			returnDto.getInfos().add(String.format("La demande a bien été créée."));
		} else {
			returnDto.getInfos().add(String.format("La demande a bien été modifiée."));
		}

		return returnDto;
	}

	@Override
	@Transactional(readOnly = true)
	public List<DemandeDto> getListeDemandesSIRH(Date fromDate, Date toDate, Integer idRefEtat, Integer idRefType,
			Integer idAgentRecherche, Integer idRefGroupeAbsence, List<Integer> agentIds) {

		if (null != idAgentRecherche && 0 != idAgentRecherche) {
			agentIds = new ArrayList<Integer>();
			agentIds.add(idAgentRecherche);
		}

		List<Demande> listeSansFiltre = demandeRepository.listeDemandesSIRH(fromDate, toDate, idRefEtat, idRefType,
				agentIds, idRefGroupeAbsence);

		List<RefEtat> listEtats = null;
		if (idRefEtat != null) {
			RefEtat etat = demandeRepository.getEntity(RefEtat.class, idRefEtat);
			listEtats = new ArrayList<RefEtat>();
			listEtats.add(etat);
		}

		List<DemandeDto> listeDto = absenceDataConsistencyRulesImpl.filtreDateAndEtatDemandeFromList(listeSansFiltre,
				listEtats, null);

		for (DemandeDto dto : listeDto) {
			IAbsenceDataConsistencyRules absenceDataConsistencyRulesImpl = dataConsistencyRulesFactory.getFactory(dto
					.getGroupeAbsence().getIdRefGroupeAbsence(), dto.getIdTypeDemande());
			dto = absenceDataConsistencyRulesImpl.filtreDroitOfDemandeSIRH(dto);
			dto.setDepassementCompteur(absenceDataConsistencyRulesImpl.checkDepassementCompteurAgent(dto));
			dto.setDepassementMultiple(absenceDataConsistencyRulesImpl.checkDepassementMultipleAgent(dto));
		}

		return listeDto;
	}

	@Override
	@Transactional(readOnly = true)
	public List<DemandeDto> getDemandesArchives(Integer idDemande) {

		List<DemandeDto> result = new ArrayList<DemandeDto>();
		Demande dem = demandeRepository.getEntity(Demande.class, idDemande);

		List<AgentWithServiceDto> listAgentsExistants = new ArrayList<AgentWithServiceDto>();
		for (EtatDemande etat : dem.getEtatsDemande()) {
			DemandeDto dto = new DemandeDto(dem, agentService.getAgentOptimise(listAgentsExistants, etat.getIdAgent(),
					helperService.getCurrentDate()));
			dto.updateEtat(
					etat,
					agentService.getAgentOptimise(listAgentsExistants, etat.getIdAgent(),
							helperService.getCurrentDate()), dem.getType().getGroupe());
			result.add(dto);
		}

		return result;
	}

	@Override
	@Transactional(value = "chainedTransactionManager")
	public ReturnMessageDto setDemandeEtatSIRH(Integer idAgent, List<DemandeEtatChangeDto> listDemandeEtatChangeDto) {

		ReturnMessageDto result = new ReturnMessageDto();

		// verification des droits SIRH
		ReturnMessageDto isUtilisateurSIRH = sirhWSConsumer.isUtilisateurSIRH(idAgent);
		if (!isUtilisateurSIRH.getErrors().isEmpty()) {
			logger.warn("L'agent n'est pas habilité à valider ou rejeter la demande de cet agent.");
			result.getErrors().add(
					String.format("L'agent n'est pas habilité à valider ou rejeter la demande de cet agent."));
			return result;
		}

		for (DemandeEtatChangeDto demandeEtatChangeDto : listDemandeEtatChangeDto) {

			if (!demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.VALIDEE.getCodeEtat())
					&& !demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.REJETE.getCodeEtat())
					&& !demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.EN_ATTENTE.getCodeEtat())
					&& !demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.ANNULEE.getCodeEtat())) {

				logger.warn(ETAT_DEMANDE_INCORRECT);
				result.getErrors().add(String.format(ETAT_DEMANDE_INCORRECT));
				continue;
			}

			Demande demande = getDemande(Demande.class, demandeEtatChangeDto.getIdDemande());

			if (null == demande) {
				logger.warn(DEMANDE_INEXISTANTE);
				result.getErrors().add(String.format(DEMANDE_INEXISTANTE));
				continue;
			}

			if (null != demande.getLatestEtatDemande()
					&& demandeEtatChangeDto.getIdRefEtat().equals(
							demande.getLatestEtatDemande().getEtat().getCodeEtat())) {
				logger.warn(ETAT_DEMANDE_INCHANGE);
				result.getErrors().add(String.format(ETAT_DEMANDE_INCHANGE));
				continue;
			}

			if (demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.VALIDEE.getCodeEtat())
					|| demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.REJETE.getCodeEtat())) {
				setDemandeEtatValide(idAgent, demandeEtatChangeDto, demande, result);
				continue;
			}

			if (demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.EN_ATTENTE.getCodeEtat())) {
				setDemandeEtatEnAttente(idAgent, demandeEtatChangeDto, demande, result);
				continue;
			}

			if (demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.ANNULEE.getCodeEtat())) {
				setDemandeEtatAnnule(idAgent, demandeEtatChangeDto, demande, result);
				continue;
			}
		}

		return result;
	}

	protected void setDemandeEtatValide(Integer idAgent, DemandeEtatChangeDto demandeEtatChangeDto, Demande demande,
			ReturnMessageDto result) {

		// #15224 : on ajoute de nouveaux états pour les congés annuels
		List<RefEtatEnum> listEtat = new ArrayList<RefEtatEnum>();
		listEtat.add(RefEtatEnum.APPROUVEE);
		listEtat.add(RefEtatEnum.EN_ATTENTE);
		listEtat.add(RefEtatEnum.A_VALIDER);
		if (demande.getType().getGroupe().getIdRefGroupeAbsence() == RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue()) {
			listEtat.add(RefEtatEnum.SAISIE);
			listEtat.add(RefEtatEnum.VISEE_FAVORABLE);
			listEtat.add(RefEtatEnum.VISEE_DEFAVORABLE);
			listEtat.add(RefEtatEnum.REFUSEE);
		}
		result = absenceDataConsistencyRulesImpl.checkEtatsDemandeAcceptes(result, demande, listEtat);

		if (0 < result.getErrors().size()) {
			return;
		}

		// #12664
		absenceDataConsistencyRulesImpl.processDataConsistencyDemande(result, idAgent, demande, false);

		if (0 < result.getErrors().size()) {
			return;
		}

		if (demande.getType() != null && demande.getType().getTypeSaisi() == null
				&& demande.getType().getTypeSaisiCongeAnnuel() != null) {

			ICounterService counterService = counterServiceFactory.getFactory(demande.getType().getGroupe()
					.getIdRefGroupeAbsence(), demande.getType().getIdRefTypeAbsence());
			result = counterService.majCompteurToAgent(result, demande, demandeEtatChangeDto);

			if (0 < result.getErrors().size()) {
				return;
			}

			// maj de la demande
			majEtatDemande(idAgent, demandeEtatChangeDto, demande, false);
		} else {
			ICounterService counterService = counterServiceFactory.getFactory(demande.getType().getGroupe()
					.getIdRefGroupeAbsence(), demande.getType().getIdRefTypeAbsence());
			result = counterService.majCompteurToAgent(result, demande, demandeEtatChangeDto);

			if (0 < result.getErrors().size()) {
				return;
			}

			// maj de la demande
			majEtatDemande(idAgent, demandeEtatChangeDto, demande, false);
		}

		if (demandeEtatChangeDto.getIdRefEtat() == RefEtatEnum.REJETE.getCodeEtat()) {
			result.getInfos().add(String.format("La demande est rejetée."));
		}
		if (demandeEtatChangeDto.getIdRefEtat() == RefEtatEnum.VALIDEE.getCodeEtat()) {
			result.getInfos().add(String.format("La demande est validée."));
		}
	}

	protected void setDemandeEtatEnAttente(Integer idAgent, DemandeEtatChangeDto demandeEtatChangeDto, Demande demande,
			ReturnMessageDto result) {

		List<RefEtatEnum> listeEtats = new ArrayList<>();
		if (demande.getType() != null
				&& demande.getType().getIdRefTypeAbsence() == RefTypeAbsenceEnum.CONGE_ANNUEL.getValue()) {
			listeEtats.add(RefEtatEnum.A_VALIDER);

		} else {
			// #14697 ajout de l etat A VALIDER
			// car erreur lors de la reprise de donnees des conges exceptionnels
			// mis l etat A VALIDER au lieu de SAISI ou APPROUVE
			listeEtats.add(RefEtatEnum.APPROUVEE);
			listeEtats.add(RefEtatEnum.A_VALIDER);
		}

		result = absenceDataConsistencyRulesImpl.checkEtatsDemandeAcceptes(result, demande, listeEtats);

		if (0 < result.getErrors().size()) {
			return;
		}

		// maj de la demande
		majEtatDemande(idAgent, demandeEtatChangeDto, demande, false);

		result.getInfos().add(String.format("La demande est en attente."));
	}

	/**
	 * Mapping des saisies (creation/modif) de demande depuis kiosque et SIRH
	 * 
	 * @param demandeDto
	 *            DTO
	 * @param demande
	 *            objet Demande
	 * @param idAgent
	 *            agent connecte
	 * @param dateJour
	 *            date du jour
	 * @param returnDto
	 *            ReturnMessageDto
	 * @return la demande a enregistrer
	 */
	private Demande mappingDemandeSpecifique(DemandeDto demandeDto, Demande demande, Integer idAgent, Date dateJour,
			ReturnMessageDto returnDto) {
		// selon le type de demande, on mappe les donnees specifiques de la
		// demande
		// et on effectue les verifications appropriees
		RefGroupeAbsence groupe = demandeRepository.getEntity(RefGroupeAbsence.class, demandeDto.getGroupeAbsence()
				.getIdRefGroupeAbsence());
		switch (RefTypeGroupeAbsenceEnum.getRefTypeGroupeAbsenceEnum(demandeDto.getGroupeAbsence()
				.getIdRefGroupeAbsence())) {
			case REPOS_COMP:
				DemandeReposComp demandeReposComp = getDemande(DemandeReposComp.class, demandeDto.getIdDemande());

				demande = Demande.mappingDemandeDtoToDemande(demandeDto, demandeReposComp);

				if (null == demande.getType().getTypeSaisi())
					demande.getType().setTypeSaisi(filtreRepository.findRefTypeSaisi(demandeDto.getIdTypeDemande()));

				demandeReposComp.setDuree(helperService.getDuree(demande.getType().getTypeSaisi(),
						demande.getDateDebut(), demande.getDateFin(), demandeDto.getDuree()).intValue());

				demande.setDateFin(helperService.getDateFin(demande.getType().getTypeSaisi(), demandeDto.getDateFin(),
						demandeDto.getDateDebut(), demandeDto.getDuree(), demandeDto.isDateFinAM(),
						demandeDto.isDateFinPM()));
				break;
			case RECUP:
				DemandeRecup demandeRecup = getDemande(DemandeRecup.class, demandeDto.getIdDemande());

				demande = Demande.mappingDemandeDtoToDemande(demandeDto, demandeRecup);

				if (null == demande.getType().getTypeSaisi())
					demande.getType().setTypeSaisi(filtreRepository.findRefTypeSaisi(demandeDto.getIdTypeDemande()));

				demandeRecup.setDuree(helperService.getDuree(demande.getType().getTypeSaisi(), demande.getDateDebut(),
						demande.getDateFin(), demandeDto.getDuree()).intValue());

				demande.setDateFin(helperService.getDateFin(demande.getType().getTypeSaisi(), demandeDto.getDateFin(),
						demandeDto.getDateDebut(), demandeDto.getDuree(), demandeDto.isDateFinAM(),
						demandeDto.isDateFinPM()));
				break;
			case AS:
				DemandeAsa demandeAsa = getDemande(DemandeAsa.class, demandeDto.getIdDemande());
				demande = Demande.mappingDemandeDtoToDemande(demandeDto, demandeAsa);

				if (null == demande.getType().getTypeSaisi())
					demande.getType().setTypeSaisi(filtreRepository.findRefTypeSaisi(demandeDto.getIdTypeDemande()));

				// dans l ordre, 1 - calcul date de debut, 2 - calcul date de
				// fin, 3 - calcul duree
				// car dependance entre ces 3 donnees pour les calculs
				demande.setDateDebut(helperService.getDateDebut(demande.getType().getTypeSaisi(),
						demandeDto.getDateDebut(), demandeDto.isDateDebutAM(), demandeDto.isDateDebutPM()));
				demande.setDateFin(helperService.getDateFin(demande.getType().getTypeSaisi(), demandeDto.getDateFin(),
						demande.getDateDebut(), demandeDto.getDuree(), demandeDto.isDateFinAM(),
						demandeDto.isDateFinPM()));

				demandeAsa = (DemandeAsa) demande;
				demandeAsa.setDuree(helperService.getDuree(demande.getType().getTypeSaisi(), demande.getDateDebut(),
						demande.getDateFin(), demandeDto.getDuree()));
				demandeAsa.setDateDebutAM(demande.getType().getTypeSaisi().isChkDateDebut() ? demandeDto
						.isDateDebutAM() : false);
				demandeAsa.setDateDebutPM(demande.getType().getTypeSaisi().isChkDateDebut() ? demandeDto
						.isDateDebutPM() : false);
				demandeAsa.setDateFinAM(demande.getType().getTypeSaisi().isChkDateFin() ? demandeDto.isDateFinAM()
						: false);
				demandeAsa.setDateFinPM(demande.getType().getTypeSaisi().isChkDateFin() ? demandeDto.isDateFinPM()
						: false);

				if (null != demandeDto.getOrganisationSyndicale()
						&& null != demandeDto.getOrganisationSyndicale().getIdOrganisation()) {
					demandeAsa.setOrganisationSyndicale(OSRepository.getEntity(OrganisationSyndicale.class, demandeDto
							.getOrganisationSyndicale().getIdOrganisation()));
				}
				break;
			case CONGES_EXCEP:
				DemandeCongesExceptionnels demandeCongesExcep = getDemande(DemandeCongesExceptionnels.class,
						demandeDto.getIdDemande());
				demande = Demande.mappingDemandeDtoToDemande(demandeDto, demandeCongesExcep);

				if (null == demande.getType().getTypeSaisi())
					demande.getType().setTypeSaisi(filtreRepository.findRefTypeSaisi(demandeDto.getIdTypeDemande()));

				// dans l ordre, 1 - calcul date de debut, 2 - calcul date de
				// fin, 3 - calcul duree
				// car dependance entre ces 3 donnees pour les calculs
				demande.setDateDebut(helperService.getDateDebut(demande.getType().getTypeSaisi(),
						demandeDto.getDateDebut(), demandeDto.isDateDebutAM(), demandeDto.isDateDebutPM()));
				demande.setDateFin(helperService.getDateFin(demande.getType().getTypeSaisi(), demandeDto.getDateFin(),
						demande.getDateDebut(), demandeDto.getDuree(), demandeDto.isDateFinAM(),
						demandeDto.isDateFinPM()));

				demandeCongesExcep = (DemandeCongesExceptionnels) demande;
				demandeCongesExcep.setDuree(helperService.getDuree(demande.getType().getTypeSaisi(),
						demande.getDateDebut(), demande.getDateFin(), demandeDto.getDuree()));
				demandeCongesExcep.setDateDebutAM(demande.getType().getTypeSaisi().isChkDateDebut() ? demandeDto
						.isDateDebutAM() : false);
				demandeCongesExcep.setDateDebutPM(demande.getType().getTypeSaisi().isChkDateDebut() ? demandeDto
						.isDateDebutPM() : false);
				demandeCongesExcep.setDateFinAM(demande.getType().getTypeSaisi().isChkDateFin() ? demandeDto
						.isDateFinAM() : false);
				demandeCongesExcep.setDateFinPM(demande.getType().getTypeSaisi().isChkDateFin() ? demandeDto
						.isDateFinPM() : false);
				demandeCongesExcep.setCommentaire(demande.getType().getTypeSaisi().isMotif() ? demandeDto
						.getCommentaire() : null);
				break;
			case CONGES_ANNUELS:
				DemandeCongesAnnuels demandeCongesAnnuels = getDemande(DemandeCongesAnnuels.class,
						demandeDto.getIdDemande());
				demandeCongesAnnuels.setTypeSaisiCongeAnnuel(filtreRepository.getEntity(RefTypeSaisiCongeAnnuel.class,
						demandeDto.getTypeSaisiCongeAnnuel().getIdRefTypeSaisiCongeAnnuel()));
				demande = demandeCongesAnnuels;
				demande.setType(filtreRepository.getEntity(RefTypeAbsence.class, demandeDto.getIdTypeDemande()));

				demande = Demande.mappingDemandeDtoToDemande(demandeDto, demandeCongesAnnuels);

				// dans l ordre, 1 - calcul date de debut, 2 - calcul date de
				// fin, 3 - calcul duree
				// car dependance entre ces 3 donnees pour les calculs
				demande.setDateDebut(helperService.getDateDebutCongeAnnuel(
						demande.getType().getTypeSaisiCongeAnnuel() == null ? null : demande.getType()
								.getTypeSaisiCongeAnnuel(), demandeDto.getDateDebut(), demandeDto.isDateDebutAM(),
						demandeDto.isDateDebutPM()));
				demande.setDateFin(helperService.getDateFinCongeAnnuel(
						demandeCongesAnnuels.getTypeSaisiCongeAnnuel() == null ? null : demandeCongesAnnuels
								.getTypeSaisiCongeAnnuel(), demandeDto.getDateFin(), demande.getDateDebut(), demandeDto
								.isDateFinAM(), demandeDto.isDateFinPM(), demandeDto.getDateReprise()));

				demandeCongesAnnuels = (DemandeCongesAnnuels) demande;
				demandeCongesAnnuels.setDuree(helperService.getDureeCongeAnnuel(demandeCongesAnnuels,
						demandeDto.getDateReprise()));
				demandeCongesAnnuels.setDureeAnneeN1(0.0);
				demandeCongesAnnuels.setNbSamediOffert(helperService.getNombreSamediOffert(demandeCongesAnnuels));
				demandeCongesAnnuels.setNbSamediDecompte(helperService.getNombreSamediDecompte(demandeCongesAnnuels));
				demandeCongesAnnuels.setDateDebutAM(demandeCongesAnnuels.getTypeSaisiCongeAnnuel() == null ? false
						: demandeCongesAnnuels.getTypeSaisiCongeAnnuel().isChkDateDebut() ? demandeDto.isDateDebutAM()
								: false);
				demandeCongesAnnuels.setDateDebutPM(demandeCongesAnnuels.getTypeSaisiCongeAnnuel() == null ? false
						: demandeCongesAnnuels.getTypeSaisiCongeAnnuel().isChkDateDebut() ? demandeDto.isDateDebutPM()
								: false);
				demandeCongesAnnuels.setDateFinAM(demandeCongesAnnuels.getTypeSaisiCongeAnnuel() == null ? false
						: demandeCongesAnnuels.getTypeSaisiCongeAnnuel().isChkDateFin() ? demandeDto.isDateFinAM()
								: false);
				demandeCongesAnnuels.setDateFinPM(demandeCongesAnnuels.getTypeSaisiCongeAnnuel() == null ? false
						: demandeCongesAnnuels.getTypeSaisiCongeAnnuel().isChkDateFin() ? demandeDto.isDateFinPM()
								: false);
				demandeCongesAnnuels.setCommentaire(demandeDto.getCommentaire());
				break;
			default:
				returnDto.getErrors().add(
						String.format("Le groupe [%d] de la demande n'est pas reconnu.", demandeDto.getGroupeAbsence()
								.getIdRefGroupeAbsence()));
		}
		demande.getType().setGroupe(groupe);

		// ON TRAITE l'ETAT
		EtatDemande etatDemande = new EtatDemande();
		etatDemande = mappingEtatDemandeSpecifique(etatDemande, demande, returnDto, demandeDto.getGroupeAbsence()
				.getIdRefGroupeAbsence());
		etatDemande.setDate(dateJour);
		etatDemande.setIdAgent(idAgent);
		etatDemande.setEtat(RefEtatEnum.getRefEtatEnum(demandeDto.getIdRefEtat()));
		demande.addEtatDemande(etatDemande);

		return demande;
	}

	@Override
	@Transactional(readOnly = true)
	public List<DemandeDto> getListeDemandesSIRHAValider(Date fromDate, Date toDate, Integer idRefEtat,
			Integer idRefType, Integer idAgentRecherche, Integer idRefGroupeAbsence, List<Integer> agentIds) {

		if (null != idAgentRecherche && 0 != idAgentRecherche) {
			agentIds = new ArrayList<Integer>();
			agentIds.add(idAgentRecherche);
		}

		List<Demande> listeSansFiltre = new ArrayList<Demande>();
		List<Integer> listGroupe = new ArrayList<Integer>();
		listGroupe.add(RefTypeGroupeAbsenceEnum.AS.getValue());
		listGroupe.add(RefTypeGroupeAbsenceEnum.CONGES_EXCEP.getValue());
		if (idRefGroupeAbsence != null) {
			if (idRefGroupeAbsence == RefTypeGroupeAbsenceEnum.AS.getValue()
					|| idRefGroupeAbsence == RefTypeGroupeAbsenceEnum.CONGES_EXCEP.getValue()) {
				listGroupe = new ArrayList<Integer>();
				listGroupe.add(idRefGroupeAbsence);
				listeSansFiltre = demandeRepository.listeDemandesASAAndCongesExcepSIRHAValider(fromDate, toDate,
						listGroupe, idRefType, agentIds);

			} else if (idRefGroupeAbsence == RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue()) {
				listeSansFiltre = demandeRepository.listeDemandesCongesAnnuelsSIRHAValider(fromDate, toDate, agentIds);
			} else {
				return new ArrayList<DemandeDto>();
			}
		} else {
			listeSansFiltre = demandeRepository.listeDemandesCongesAnnuelsSIRHAValider(fromDate, toDate, agentIds);
			listeSansFiltre.addAll(demandeRepository.listeDemandesASAAndCongesExcepSIRHAValider(fromDate, toDate,
					listGroupe, idRefType, agentIds));
		}

		List<RefEtat> listEtats = null;
		if (idRefEtat != null) {
			if (idRefEtat == RefEtatEnum.APPROUVEE.getCodeEtat() || idRefEtat == RefEtatEnum.EN_ATTENTE.getCodeEtat()
					|| idRefEtat == RefEtatEnum.A_VALIDER.getCodeEtat()) {
				RefEtat etat = demandeRepository.getEntity(RefEtat.class, idRefEtat);
				listEtats = new ArrayList<RefEtat>();
				listEtats.add(etat);
			} else {
				return new ArrayList<DemandeDto>();
			}
		} else {
			listEtats = new ArrayList<RefEtat>();
			listEtats = filtreRepository.findRefEtatAValider();
		}

		List<DemandeDto> listeDto = absenceDataConsistencyRulesImpl.filtreDateAndEtatDemandeFromList(listeSansFiltre,
				listEtats, null);
		for (DemandeDto dto : listeDto) {
			IAbsenceDataConsistencyRules absenceDataConsistencyRulesImpl = dataConsistencyRulesFactory.getFactory(dto
					.getGroupeAbsence().getIdRefGroupeAbsence(), dto.getIdTypeDemande());
			dto = absenceDataConsistencyRulesImpl.filtreDroitOfDemandeSIRH(dto);
			dto.setDepassementCompteur(absenceDataConsistencyRulesImpl.checkDepassementCompteurAgent(dto));
			dto.setDepassementMultiple(absenceDataConsistencyRulesImpl.checkDepassementMultipleAgent(dto));
		}

		return listeDto;
	}

	@Override
	@Transactional(readOnly = true)
	public ReturnMessageDto checkRecuperations(Integer convertedIdAgent, Date fromDate, Date toDate) {
		ReturnMessageDto result = new ReturnMessageDto();
		// on cherche toutes les demandes de recup de l'agent entre les dates

		List<Demande> listeDemande = demandeRepository.listeDemandesAgentVerification(convertedIdAgent, fromDate,
				toDate, RefTypeGroupeAbsenceEnum.RECUP.getValue());

		AgentGeneriqueDto agent = sirhWSConsumer.getAgent(convertedIdAgent);
		for (Demande d : listeDemande) {
			DemandeRecup demandeRecup = demandeRepository.getEntity(DemandeRecup.class, d.getIdDemande());
			// si la demande est dans un bon etat
			if (RefEtatEnum.APPROUVEE.equals(demandeRecup.getLatestEtatDemande().getEtat())
					|| RefEtatEnum.PRISE.equals(demandeRecup.getLatestEtatDemande().getEtat())) {
				String msg = String.format(RECUP_MSG, new DateTime(fromDate).toString("dd/MM/yyyy HH:mm"),
						agent.getNomUsage() + " " + agent.getPrenomUsage());
				result.getErrors().add(msg);
			} else {
				result.getInfos().add(
						String.format(AVERT_MESSAGE_ABS,
								new DateTime(demandeRecup.getDateDebut()).toString("dd/MM/yyyy"), "récupération",
								agent.getNomUsage() + " " + agent.getPrenomUsage()));
			}
		}

		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public ReturnMessageDto checkReposCompensateurs(Integer convertedIdAgent, Date fromDate, Date toDate) {
		ReturnMessageDto result = new ReturnMessageDto();
		// on cherche toutes les demandes de repos comp de l'agent entre les
		// dates

		List<Demande> listeDemande = demandeRepository.listeDemandesAgentVerification(convertedIdAgent, fromDate,
				toDate, RefTypeGroupeAbsenceEnum.REPOS_COMP.getValue());
		for (Demande d : listeDemande) {
			DemandeReposComp demandeReposComp = demandeRepository.getEntity(DemandeReposComp.class, d.getIdDemande());

			AgentGeneriqueDto agent = sirhWSConsumer.getAgent(convertedIdAgent);
			// si la demande est dans un bon etat
			if (RefEtatEnum.APPROUVEE.equals(demandeReposComp.getLatestEtatDemande().getEtat())
					|| RefEtatEnum.PRISE.equals(demandeReposComp.getLatestEtatDemande().getEtat())) {
				String msg = String.format(REPOS_COMP_MSG, new DateTime(fromDate).toString("dd/MM/yyyy HH:mm"),
						agent.getNomUsage() + " " + agent.getPrenomUsage());
				result.getErrors().add(msg);
			} else {
				result.getInfos().add(
						String.format(AVERT_MESSAGE_ABS,
								new DateTime(demandeReposComp.getDateDebut()).toString("dd/MM/yyyy"),
								"repos compensateur", agent.getNomUsage() + " " + agent.getPrenomUsage()));
			}
		}

		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public ReturnMessageDto checkAbsencesSyndicales(Integer convertedIdAgent, Date fromDate, Date toDate) {
		ReturnMessageDto result = new ReturnMessageDto();
		// on cherche toutes les demandes d'ASA de l'agent entre les dates

		List<Demande> listeDemande = demandeRepository.listeDemandesAgentVerification(convertedIdAgent, fromDate,
				toDate, RefTypeGroupeAbsenceEnum.AS.getValue());
		for (Demande d : listeDemande) {
			DemandeAsa demandeAsa = demandeRepository.getEntity(DemandeAsa.class, d.getIdDemande());

			AgentGeneriqueDto agent = sirhWSConsumer.getAgent(convertedIdAgent);
			// si la demande est dans un bon etat
			if (RefEtatEnum.VALIDEE.equals(demandeAsa.getLatestEtatDemande().getEtat())
					|| RefEtatEnum.PRISE.equals(demandeAsa.getLatestEtatDemande().getEtat())) {
				String msg = String.format(ASA_MSG, new DateTime(fromDate).toString("dd/MM/yyyy HH:mm"),
						agent.getNomUsage() + " " + agent.getPrenomUsage());
				result.getErrors().add(msg);
			} else {
				result.getInfos().add(
						String.format(AVERT_MESSAGE_ABS,
								new DateTime(demandeAsa.getDateDebut()).toString("dd/MM/yyyy"), "absence syndicale",
								agent.getNomUsage() + " " + agent.getPrenomUsage()));
			}
		}

		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public ReturnMessageDto checkCongesExceptionnels(Integer convertedIdAgent, Date fromDate, Date toDate) {
		ReturnMessageDto result = new ReturnMessageDto();
		// on cherche toutes les demandes de congés exceptionnels de l'agent
		// entre les dates
		List<Demande> listeDemande = demandeRepository.listeDemandesAgentVerification(convertedIdAgent, fromDate,
				toDate, RefTypeGroupeAbsenceEnum.CONGES_EXCEP.getValue());
		for (Demande d : listeDemande) {
			DemandeCongesExceptionnels demandeExcep = demandeRepository.getEntity(DemandeCongesExceptionnels.class,
					d.getIdDemande());

			AgentGeneriqueDto agent = sirhWSConsumer.getAgent(convertedIdAgent);
			// si la demande est dans un bon etat
			if (RefEtatEnum.VALIDEE.equals(demandeExcep.getLatestEtatDemande().getEtat())
					|| RefEtatEnum.PRISE.equals(demandeExcep.getLatestEtatDemande().getEtat())) {
				String msg = String.format(CONGE_EXCEP_MSG, new DateTime(fromDate).toString("dd/MM/yyyy HH:mm"),
						agent.getNomUsage() + " " + agent.getPrenomUsage());
				result.getErrors().add(msg);
			} else {
				result.getInfos().add(
						String.format(AVERT_MESSAGE_ABS,
								new DateTime(demandeExcep.getDateDebut()).toString("dd/MM/yyyy"), "congé exceptionnel",
								agent.getNomUsage() + " " + agent.getPrenomUsage()));
			}
		}

		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public ReturnMessageDto checkCongesAnnuels(Integer convertedIdAgent, Date fromDate, Date toDate) {
		ReturnMessageDto result = new ReturnMessageDto();
		// on cherche toutes les demandes de congés annuels de l'agent
		// entre les dates
		List<Demande> listeDemande = demandeRepository.listeDemandesAgentVerification(convertedIdAgent, fromDate,
				toDate, RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());
		for (Demande d : listeDemande) {
			DemandeCongesAnnuels demandeCongeAnnuel = demandeRepository.getEntity(DemandeCongesAnnuels.class,
					d.getIdDemande());

			AgentGeneriqueDto agent = sirhWSConsumer.getAgent(convertedIdAgent);
			// si la demande est dans un bon etat
			if (RefEtatEnum.APPROUVEE.equals(demandeCongeAnnuel.getLatestEtatDemande().getEtat())
					|| RefEtatEnum.VALIDEE.equals(demandeCongeAnnuel.getLatestEtatDemande().getEtat())
					|| RefEtatEnum.PRISE.equals(demandeCongeAnnuel.getLatestEtatDemande().getEtat())) {
				String msg = String.format(CONGE_ANNUEL_MSG, new DateTime(fromDate).toString("dd/MM/yyyy HH:mm"),
						agent.getNomUsage() + " " + agent.getPrenomUsage());
				result.getErrors().add(msg);
			} else {
				result.getInfos().add(
						String.format(AVERT_MESSAGE_ABS,
								new DateTime(demandeCongeAnnuel.getDateDebut()).toString("dd/MM/yyyy"), "congé annuel",
								agent.getNomUsage() + " " + agent.getPrenomUsage()));
			}
		}

		return result;
	}

	@Override
	public List<MoisAlimAutoCongesAnnuelsDto> getListeMoisAlimAutoCongeAnnuel() {
		List<MoisAlimAutoCongesAnnuelsDto> result = new ArrayList<MoisAlimAutoCongesAnnuelsDto>();
		for (Date d : congeAnnuelRepository.getListeMoisAlimAutoCongeAnnuel()) {
			MoisAlimAutoCongesAnnuelsDto mois = new MoisAlimAutoCongesAnnuelsDto();
			mois.setDateMois(d);
			result.add(mois);
		}

		return result;
	}

	@Override
	public List<MoisAlimAutoCongesAnnuelsDto> getListeAlimAutoCongeAnnuelByMois(Date dateMois, boolean onlyErreur) {
		List<MoisAlimAutoCongesAnnuelsDto> result = new ArrayList<MoisAlimAutoCongesAnnuelsDto>();
		for (CongeAnnuelAlimAutoHisto histo : congeAnnuelRepository.getListeAlimAutoCongeAnnuelByMois(dateMois,
				onlyErreur)) {
			MoisAlimAutoCongesAnnuelsDto mois = new MoisAlimAutoCongesAnnuelsDto();
			AgentGeneriqueDto ag = sirhWSConsumer.getAgent(histo.getIdAgent());
			AgentDto agDto = new AgentDto();
			if (ag != null && ag.getIdAgent() != null) {
				agDto = new AgentDto(ag);
			}
			mois.setAgent(agDto);
			mois.setDateModification(histo.getDateModification());
			mois.setStatus(histo.getStatus());
			mois.setInfos(histo.getMessageInfos());
			mois.setDateMois(histo.getDateMonth());
			result.add(mois);
		}

		return result;
	}

	@Override
	public List<MoisAlimAutoCongesAnnuelsDto> getHistoAlimAutoCongeAnnuel(Integer idAgent) {
		List<MoisAlimAutoCongesAnnuelsDto> result = new ArrayList<MoisAlimAutoCongesAnnuelsDto>();
		for (CongeAnnuelAlimAutoHisto histo : congeAnnuelRepository.getListeAlimAutoCongeAnnuelByAgent(idAgent)) {
			MoisAlimAutoCongesAnnuelsDto mois = new MoisAlimAutoCongesAnnuelsDto();
			AgentGeneriqueDto ag = sirhWSConsumer.getAgent(histo.getIdAgent());
			AgentDto agDto = new AgentDto();
			if (ag != null && ag.getIdAgent() != null) {
				agDto = new AgentDto(ag);
			}
			mois.setAgent(agDto);
			mois.setDateModification(histo.getDateModification());
			mois.setStatus(histo.getStatus());
			mois.setInfos(histo.getMessageInfos());
			mois.setDateMois(histo.getDateMonth());
			AgentWeekCongeAnnuel weekConge = congeAnnuelRepository.getWeekHistoForAgentAndDate(idAgent,
					mois.getDateMois());
			mois.setNbJours(weekConge == null ? 0 : weekConge.getJours());
			result.add(mois);
		}

		return result;
	}

	@Override
	public List<Integer> getListeIdAgentConcerneRestitutionMassive(RestitutionMassiveDto dto) {
		List<Integer> result = new ArrayList<>();
		// on cherche tous les agents en congés pour cette date
		List<Integer> listIdAgentConge = congeAnnuelRepository.getListeDemandesCongesAnnuelsPrisesForDate(dto
				.getDateRestitution());
		for (Integer idAgent : listIdAgentConge) {
			// on cherche sa base horaire
			RefTypeSaisiCongeAnnuelDto dtoBase = sirhWSConsumer
					.getBaseHoraireAbsence(idAgent, dto.getDateRestitution());
			if (null != dtoBase && null != dtoBase.getIdRefTypeSaisiCongeAnnuel()) {
				RefTypeSaisiCongeAnnuel typeConge = typeAbsenceRepository.getEntity(RefTypeSaisiCongeAnnuel.class,
						dtoBase.getIdRefTypeSaisiCongeAnnuel());
				if (null == typeConge
						|| null == typeConge.getCodeBaseHoraireAbsence()
						|| (!"A".equals(typeConge.getCodeBaseHoraireAbsence().trim()) && !"D".equals(typeConge
								.getCodeBaseHoraireAbsence().trim()))) {
					logger.debug(String.format(MAUVAIS_BASE_CA, idAgent));
				} else {
					if (!result.contains(idAgent))
						result.add(idAgent);
				}
			} else {
				logger.error(String.format(BASE_CA_NON_TROUVEE, idAgent));
			}
		}
		return result;
	}

	@Override
	public List<RefAlimCongesAnnuelsDto> getListeRefAlimCongeAnnuelByBaseConge(Integer idRefTypeSaisiCongeAnnuel) {
		List<RefAlimCongesAnnuelsDto> result = new ArrayList<RefAlimCongesAnnuelsDto>();
		for (RefAlimCongeAnnuel ref : congeAnnuelRepository
				.getListeRefAlimCongeAnnuelByBaseConge(idRefTypeSaisiCongeAnnuel)) {
			RefAlimCongesAnnuelsDto dto = new RefAlimCongesAnnuelsDto(ref);
			result.add(dto);
		}
		return result;
	}

	@Override
	@Transactional(value = "chainedTransactionManager")
	public ReturnMessageDto setRefAlimCongeAnnuel(Integer convertedIdAgent,
			RefAlimCongesAnnuelsDto refAlimCongesAnnuelsDto) {

		ReturnMessageDto result = new ReturnMessageDto();

		// verification des droits SIRH
		ReturnMessageDto isUtilisateurSIRH = sirhWSConsumer.isUtilisateurSIRH(convertedIdAgent);
		if (!isUtilisateurSIRH.getErrors().isEmpty()) {
			logger.warn(AGENT_NON_HABILITE);
			result.getErrors().add(String.format(AGENT_NON_HABILITE));
			return result;
		}

		RefAlimCongeAnnuel refAlim = congeAnnuelRepository.getRefAlimCongeAnnuel(
				refAlimCongesAnnuelsDto.getIdRefTypeSaisiCongeAnnuel(), refAlimCongesAnnuelsDto.getAnnee());

		if (null == refAlim) {
			refAlim = new RefAlimCongeAnnuel();
			RefAlimCongeAnnuelId id = new RefAlimCongeAnnuelId();
			id.setAnnee(refAlimCongesAnnuelsDto.getAnnee());
			id.setIdRefTypeSaisiCongeAnnuel(refAlimCongesAnnuelsDto.getIdRefTypeSaisiCongeAnnuel());
			refAlim.setId(id);
		}

		refAlim.setJanvier(refAlimCongesAnnuelsDto.getJanvier());
		refAlim.setFevrier(refAlimCongesAnnuelsDto.getFevrier());
		refAlim.setMars(refAlimCongesAnnuelsDto.getMars());
		refAlim.setAvril(refAlimCongesAnnuelsDto.getAvril());
		refAlim.setMai(refAlimCongesAnnuelsDto.getMai());
		refAlim.setJuin(refAlimCongesAnnuelsDto.getJuin());
		refAlim.setJuillet(refAlimCongesAnnuelsDto.getJuillet());
		refAlim.setAout(refAlimCongesAnnuelsDto.getAout());
		refAlim.setSeptembre(refAlimCongesAnnuelsDto.getSeptembre());
		refAlim.setOctobre(refAlimCongesAnnuelsDto.getOctobre());
		refAlim.setNovembre(refAlimCongesAnnuelsDto.getNovembre());
		refAlim.setDecembre(refAlimCongesAnnuelsDto.getDecembre());

		if (!result.getErrors().isEmpty()) {
			return result;
		}

		// congeAnnuelRepository.persistEntity(refAlim);
		demandeRepository.persistEntity(refAlim);

		logger.debug("Alimentation des congés annuels sauvegardée.");
		result.getInfos().add("Alimentation des congés annuels sauvegardée.");

		return result;
	}

	@Override
	@Transactional(value = "chainedTransactionManager")
	public ReturnMessageDto miseAJourSpsold(Integer idAgent) {
		ReturnMessageDto result = new ReturnMessageDto();
		if (typeEnvironnement.equals("RECETTE")) {
			// on cherche le solde de l'agent
			AgentCongeAnnuelCount soldeCongeAgent = counterRepository.getAgentCounter(AgentCongeAnnuelCount.class,
					idAgent);
			if (soldeCongeAgent == null) {
				logger.warn(COMPTEUR_INEXISTANT_SPSOLD);
				result.getInfos().add(String.format(COMPTEUR_INEXISTANT_SPSOLD));
				return result;
			}

			// on cherche Spsold
			SpSold soldeConge = sirhRepository.getSpsold(idAgent);
			// si pas de ligne on en crée une
			if (soldeConge == null) {
				soldeConge = new SpSold();
				soldeConge.setNomatr(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(idAgent));
			}
			soldeConge.setSoldeAnneeEnCours(soldeCongeAgent.getTotalJours());
			soldeConge.setSoldeAnneePrec(soldeCongeAgent.getTotalJoursAnneeN1());

			sirhRepository.persistEntity(soldeConge);
			result.getInfos().add("Mise à jour SPSOLD OK");
		}
		return result;
	}

	@Override
	@Transactional(value = "chainedTransactionManager")
	public ReturnMessageDto miseAJourSpsorc(Integer idAgent) {
		ReturnMessageDto result = new ReturnMessageDto();

		// on recherche sa carriere pour avoir son statut (Fonctionnaire,
		// contractuel,convention coll
		Spcarr carr = sirhRepository.getAgentCurrentCarriere(
				agentMatriculeService.fromIdAgentToSIRHNomatrAgent(idAgent), helperService.getCurrentDate());
		if (!(carr.getCdcate() == 4 || carr.getCdcate() == 7)) {
			logger.debug(String.format(STATUT_AGENT, idAgent));
			result.getInfos().add(String.format(STATUT_AGENT, idAgent));
			return result;
		}

		if (typeEnvironnement.equals("RECETTE")) {
			// on cherche le solde de l'agent
			AgentReposCompCount soldeReposCompAgent = counterRepository.getAgentCounter(AgentReposCompCount.class,
					idAgent);
			if (soldeReposCompAgent == null) {
				logger.warn(COMPTEUR_INEXISTANT_SPSORC);
				result.getInfos().add(String.format(COMPTEUR_INEXISTANT_SPSORC));
				return result;
			}

			// on cherche Spsorc
			SpSorc soldeReposComp = sirhRepository.getSpsorc(idAgent);
			// si pas de ligne on en crée une
			if (soldeReposComp == null) {
				soldeReposComp = new SpSorc();
				soldeReposComp.setNomatr(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(idAgent));
			}

			soldeReposComp.setSoldeAnneeEnCours((double) (soldeReposCompAgent.getTotalMinutes() / 60));
			soldeReposComp.setSoldeAnneePrec((double) (soldeReposCompAgent.getTotalMinutesAnneeN1() / 60));
			soldeReposComp.setNombrePris(reposCompensateurRepository.getSommeDureeDemandePrises2Ans(idAgent) / 60);

			sirhRepository.persistEntity(soldeReposComp);
			result.getInfos().add("Mise à jour SPSORC OK");
		}
		return result;
	}

	@Override
	@Transactional(value = "absTransactionManager")
	public ReturnMessageDto createRefAlimCongeAnnuelAnnee(Integer anneeCreation) {
		// cf #15284
		ReturnMessageDto result = new ReturnMessageDto();

		List<RefAlimCongeAnnuel> listRefAlimAnneePrecedente = congeAnnuelRepository
				.getListeRefAlimCongeAnnuelByYear(anneeCreation - 1);

		if (null == listRefAlimAnneePrecedente || listRefAlimAnneePrecedente.size() == 0
				|| listRefAlimAnneePrecedente.size() != typeAbsenceRepository.getListeTypeSaisiCongeAnnuel().size()) {
			result.getErrors().add(
					"Aucun paramétrage trouvé pour l'année précédente. Merci de contacter le responsable du projet.");
			return result;
		}
		for (RefAlimCongeAnnuel oldRefAlim : listRefAlimAnneePrecedente) {
			RefAlimCongeAnnuel newRefAlim = new RefAlimCongeAnnuel();
			newRefAlim.setJanvier(oldRefAlim.getJanvier());
			newRefAlim.setFevrier(oldRefAlim.getFevrier());
			newRefAlim.setMars(oldRefAlim.getMars());
			newRefAlim.setAvril(oldRefAlim.getAvril());
			newRefAlim.setMai(oldRefAlim.getMai());
			newRefAlim.setJuin(oldRefAlim.getJuin());
			newRefAlim.setJuillet(oldRefAlim.getJuillet());
			newRefAlim.setAout(oldRefAlim.getAout());
			newRefAlim.setSeptembre(oldRefAlim.getSeptembre());
			newRefAlim.setOctobre(oldRefAlim.getOctobre());
			newRefAlim.setNovembre(oldRefAlim.getNovembre());
			newRefAlim.setDecembre(oldRefAlim.getDecembre());
			RefAlimCongeAnnuelId newId = new RefAlimCongeAnnuelId();
			newId.setAnnee(anneeCreation);
			newId.setIdRefTypeSaisiCongeAnnuel(oldRefAlim.getId().getIdRefTypeSaisiCongeAnnuel());
			newRefAlim.setId(newId);
			demandeRepository.persistEntity(newRefAlim);
		}

		logger.debug("Alimentation des congés annuels sauvegardée.");
		result.getInfos().add("Alimentation des congés annuels sauvegardée.");

		return result;
	}
}
