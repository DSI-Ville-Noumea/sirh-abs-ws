package nc.noumea.mairie.abs.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.FlushModeType;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeAsa;
import nc.noumea.mairie.abs.domain.DemandeCongesAnnuels;
import nc.noumea.mairie.abs.domain.DemandeCongesExceptionnels;
import nc.noumea.mairie.abs.domain.DemandeRecup;
import nc.noumea.mairie.abs.domain.DemandeReposComp;
import nc.noumea.mairie.abs.domain.DroitsAgent;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.OrganisationSyndicale;
import nc.noumea.mairie.abs.domain.RefEtat;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.domain.RefTypeGroupeAbsenceEnum;
import nc.noumea.mairie.abs.domain.RefTypeSaisiCongeAnnuel;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IAccessRightsRepository;
import nc.noumea.mairie.abs.repository.IDemandeRepository;
import nc.noumea.mairie.abs.repository.IFiltreRepository;
import nc.noumea.mairie.abs.repository.IOrganisationSyndicaleRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.abs.service.IAbsenceDataConsistencyRules;
import nc.noumea.mairie.abs.service.IAbsenceService;
import nc.noumea.mairie.abs.service.IAccessRightsService;
import nc.noumea.mairie.abs.service.ICounterService;
import nc.noumea.mairie.abs.service.IFiltreService;
import nc.noumea.mairie.abs.service.counter.impl.CounterServiceFactory;
import nc.noumea.mairie.abs.service.rules.impl.DataConsistencyRulesFactory;
import nc.noumea.mairie.ws.ISirhWSConsumer;

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
	private HelperService helperService;

	@Autowired
	private CounterServiceFactory counterServiceFactory;

	@Autowired
	private ISirhWSConsumer sirhWSConsumer;

	@Autowired
	private IOrganisationSyndicaleRepository OSRepository;

	private static final String ETAT_DEMANDE_INCHANGE = "L'état de la demande est inchangé.";
	private static final String DEMANDE_INEXISTANTE = "La demande n'existe pas.";
	private static final String ETAT_DEMANDE_INCORRECT = "L'état de la demande envoyée n'est pas correct.";

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

		absenceDataConsistencyRulesImpl.processDataConsistencyDemande(returnDto, idAgent, demande, dateJour, false);

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
						demandeReposComp.getLatestEtatDemande().getIdAgent(), helperService.getCurrentDate()));
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
								helperService.getCurrentDate()));
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
								helperService.getCurrentDate()));
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
						demandeCongesExcep.getLatestEtatDemande().getIdAgent(), helperService.getCurrentDate()));
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
						demandeCongesAnnuels.getLatestEtatDemande().getIdAgent(), helperService.getCurrentDate()));
				break;
			default:
				return demandeDto;
		}

		return demandeDto;
	}

	@Override
	@Transactional(readOnly = true)
	public List<DemandeDto> getListeDemandes(Integer idAgentConnecte, Integer idAgentConcerne, String ongletDemande,
			Date fromDate, Date toDate, Date dateDemande, Integer idRefEtat, Integer idRefType,
			Integer idRefGroupeAbsence) {

		// si date de debut et de fin nulles, alors on filtre sur 12 mois
		// glissants

		if (null == fromDate && null == toDate) {
			fromDate = helperService.getCurrentDateMoinsUnAn();
		}

		List<Demande> listeSansFiltre = getListeNonFiltreeDemandes(idAgentConnecte, idAgentConcerne, fromDate, toDate,
				idRefType, idRefGroupeAbsence);

		List<RefEtat> listEtats = filtresService.getListeEtatsByOnglet(ongletDemande, idRefEtat);

		List<DemandeDto> listeDto = absenceDataConsistencyRulesImpl.filtreDateAndEtatDemandeFromList(listeSansFiltre,
				listEtats, dateDemande);

		// si idAgentConnecte == idAgentConcerne, alors nous sommes dans le cas
		// du WS listeDemandesAgent
		// donc inutile de recuperer les droits en bdd
		List<DroitsAgent> listDroitAgent = new ArrayList<DroitsAgent>();
		if (null != idAgentConnecte && !idAgentConnecte.equals(idAgentConcerne)) {
			listDroitAgent = accessRightsRepository.getListOfAgentsToInputOrApprove(idAgentConnecte, null);
		}

		for (DemandeDto demandeDto : listeDto) {
			IAbsenceDataConsistencyRules absenceDataConsistencyRulesImpl = dataConsistencyRulesFactory.getFactory(
					demandeDto.getGroupeAbsence().getIdRefGroupeAbsence(), demandeDto.getIdTypeDemande());
			demandeDto = absenceDataConsistencyRulesImpl.filtreDroitOfDemande(idAgentConnecte, demandeDto,
					listDroitAgent);
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

		Integer idApprobateurOfDelegataire = accessRightsService.getIdApprobateurOfDelegataire(idAgentConnecte,
				idAgentConcerne);

		listeSansFiltre = demandeRepository.listeDemandesAgent(idAgentConnecte, idAgentConcerne, fromDate, toDate,
				idRefType, idRefGroupeAbsence);
		if (null != idApprobateurOfDelegataire) {
			listeSansFiltredelegataire = demandeRepository.listeDemandesAgent(idApprobateurOfDelegataire,
					idAgentConcerne, fromDate, toDate, idRefType, idRefGroupeAbsence);
		}

		for (Demande demandeDeleg : listeSansFiltredelegataire) {
			if (!listeSansFiltre.contains(demandeDeleg)) {
				listeSansFiltre.add(demandeDeleg);
			}
		}

		return listeSansFiltre;
	}

	@Override
	@Transactional(value = "absTransactionManager")
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
		majEtatDemande(idAgent, demandeEtatChangeDto, demande);

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

		ICounterService counterService = counterServiceFactory.getFactory(demande.getType().getGroupe()
				.getIdRefGroupeAbsence(), demande.getType().getIdRefTypeAbsence());
		result = counterService.majCompteurToAgent(result, demande, demandeEtatChangeDto);

		if (0 < result.getErrors().size()) {
			return result;
		}

		// maj de la demande
		majEtatDemande(idAgent, demandeEtatChangeDto, demande);

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

		IAbsenceDataConsistencyRules absenceDataConsistencyRulesImpl = dataConsistencyRulesFactory.getFactory(demande
				.getType().getGroupe().getIdRefGroupeAbsence(), demande.getType().getIdRefTypeAbsence());

		result = absenceDataConsistencyRulesImpl.checkEtatsDemandeAnnulee(result, demande,
				Arrays.asList(RefEtatEnum.VISEE_FAVORABLE, RefEtatEnum.VISEE_DEFAVORABLE, RefEtatEnum.APPROUVEE));

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

		// maj de la demande
		majEtatDemande(idAgent, demandeEtatChangeDto, demande);

		result.getInfos().add(String.format("La demande est annulée."));

		return result;
	}

	private void majEtatDemande(Integer idAgent, DemandeEtatChangeDto demandeEtatChangeDto, Demande demande) {

		if (demande.getType() != null && demande.getType().getTypeSaisiCongeAnnuel() != null) {
			// cas des congés annuels
			IAbsenceDataConsistencyRules absenceDataConsistencyRulesImpl = dataConsistencyRulesFactory.getFactory(
					RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue(), null);
			ReturnMessageDto srm = new ReturnMessageDto();
			srm = absenceDataConsistencyRulesImpl.checkDepassementDroitsAcquis(srm, demande);
			if (srm.getErrors().size() > 0) {
				EtatDemande etatDemande = new EtatDemande();
				etatDemande.setDate(new Date());
				etatDemande.setMotif(demandeEtatChangeDto.getMotif());
				etatDemande.setEtat(RefEtatEnum.A_VALIDER);
				demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.A_VALIDER.getCodeEtat());
				etatDemande.setIdAgent(idAgent);
				demande.addEtatDemande(etatDemande);
			} else {
				EtatDemande etatDemande = new EtatDemande();
				etatDemande.setDate(new Date());
				etatDemande.setMotif(demandeEtatChangeDto.getMotif());
				etatDemande.setEtat(RefEtatEnum.getRefEtatEnum(demandeEtatChangeDto.getIdRefEtat()));
				etatDemande.setIdAgent(idAgent);
				demande.addEtatDemande(etatDemande);
			}
		} else {
			EtatDemande etatDemande = new EtatDemande();
			etatDemande.setDate(new Date());
			etatDemande.setMotif(demandeEtatChangeDto.getMotif());
			etatDemande.setEtat(RefEtatEnum.getRefEtatEnum(demandeEtatChangeDto.getIdRefEtat()));
			etatDemande.setIdAgent(idAgent);
			demande.addEtatDemande(etatDemande);
		}
	}

	@Override
	@Transactional(value = "absTransactionManager")
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
				break;
			default:
				break;
		}

		EtatDemande epNew = new EtatDemande();
		epNew.setDemande(demande);
		epNew.setDate(helperService.getCurrentDate());
		epNew.setEtat(RefEtatEnum.PRISE);
		epNew.setIdAgent(demande.getIdAgent());
		demande.addEtatDemande(epNew);

		// insert nouvelle ligne EtatAbsence avec nouvel etat
		demandeRepository.persistEntity(epNew);

		logger.info("Updated demande id {}.", idDemande);

		return result;
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
			majEtatDemande(idAgent, demandeEtatChangeDto, demande);
		}

		IAbsenceDataConsistencyRules absenceDataConsistencyRulesImpl = dataConsistencyRulesFactory.getFactory(
				demandeDto.getGroupeAbsence().getIdRefGroupeAbsence(), demandeDto.getIdTypeDemande());

		absenceDataConsistencyRulesImpl.processDataConsistencyDemande(returnDto, demandeDto.getAgentWithServiceDto()
				.getIdAgent(), demande, dateJour, true);

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
			Integer idAgentRecherche, Integer idRefGroupeAbsence) {
		List<Demande> listeSansFiltre = demandeRepository.listeDemandesSIRH(fromDate, toDate, idRefEtat, idRefType,
				idAgentRecherche, idRefGroupeAbsence);
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

		for (EtatDemande etat : dem.getEtatsDemande()) {
			DemandeDto dto = new DemandeDto(dem, sirhWSConsumer.getAgentService(etat.getIdAgent(),
					helperService.getCurrentDate()));
			dto.updateEtat(etat, sirhWSConsumer.getAgentService(etat.getIdAgent(), helperService.getCurrentDate()));
			result.add(dto);
		}

		return result;
	}

	@Override
	@Transactional(value = "absTransactionManager")
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

		result = absenceDataConsistencyRulesImpl.checkEtatsDemandeAcceptes(result, demande,
				Arrays.asList(RefEtatEnum.APPROUVEE, RefEtatEnum.EN_ATTENTE));

		if (0 < result.getErrors().size()) {
			return;
		}

		if (demande.getType() != null && demande.getType().getTypeSaisiCongeAnnuel() != null) {
			// maj de la demande
			majEtatDemande(idAgent, demandeEtatChangeDto, demande);

			ICounterService counterService = counterServiceFactory.getFactory(demande.getType().getGroupe()
					.getIdRefGroupeAbsence(), demande.getType().getIdRefTypeAbsence());
			result = counterService.majCompteurToAgent(result, demande, demandeEtatChangeDto);

			if (0 < result.getErrors().size()) {
				return;
			}
		} else {
			ICounterService counterService = counterServiceFactory.getFactory(demande.getType().getGroupe()
					.getIdRefGroupeAbsence(), demande.getType().getIdRefTypeAbsence());
			result = counterService.majCompteurToAgent(result, demande, demandeEtatChangeDto);

			if (0 < result.getErrors().size()) {
				return;
			}

			// maj de la demande
			majEtatDemande(idAgent, demandeEtatChangeDto, demande);
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
			listeEtats.add(RefEtatEnum.APPROUVEE);
		}

		result = absenceDataConsistencyRulesImpl.checkEtatsDemandeAcceptes(result, demande, listeEtats);

		if (0 < result.getErrors().size()) {
			return;
		}

		// maj de la demande
		majEtatDemande(idAgent, demandeEtatChangeDto, demande);

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
		switch (RefTypeGroupeAbsenceEnum.getRefTypeGroupeAbsenceEnum(demandeDto.getGroupeAbsence()
				.getIdRefGroupeAbsence())) {
			case REPOS_COMP:
				DemandeReposComp demandeReposComp = getDemande(DemandeReposComp.class, demandeDto.getIdDemande());
				demandeReposComp.setDuree(demandeDto.getDuree().intValue());
				demande = Demande.mappingDemandeDtoToDemande(demandeDto, demandeReposComp, idAgent, dateJour);

				if (null == demande.getType().getTypeSaisi())
					demande.getType().setTypeSaisi(filtreRepository.findRefTypeSaisi(demandeDto.getIdTypeDemande()));

				demande.setDateFin(helperService.getDateFin(demande.getType().getTypeSaisi(), demandeDto.getDateFin(),
						demandeDto.getDateDebut(), demandeDto.getDuree(), demandeDto.isDateFinAM(),
						demandeDto.isDateFinPM()));
				break;
			case RECUP:
				DemandeRecup demandeRecup = getDemande(DemandeRecup.class, demandeDto.getIdDemande());
				demandeRecup.setDuree(demandeDto.getDuree().intValue());
				demande = Demande.mappingDemandeDtoToDemande(demandeDto, demandeRecup, idAgent, dateJour);

				if (null == demande.getType().getTypeSaisi())
					demande.getType().setTypeSaisi(filtreRepository.findRefTypeSaisi(demandeDto.getIdTypeDemande()));

				demande.setDateFin(helperService.getDateFin(demande.getType().getTypeSaisi(), demandeDto.getDateFin(),
						demandeDto.getDateDebut(), demandeDto.getDuree(), demandeDto.isDateFinAM(),
						demandeDto.isDateFinPM()));
				break;
			case AS:
				DemandeAsa demandeAsa = getDemande(DemandeAsa.class, demandeDto.getIdDemande());
				demande = Demande.mappingDemandeDtoToDemande(demandeDto, demandeAsa, idAgent, dateJour);

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
				demande = Demande.mappingDemandeDtoToDemande(demandeDto, demandeCongesExcep, idAgent, dateJour);

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

				demande = Demande.mappingDemandeDtoToDemande(demandeDto, demandeCongesAnnuels, idAgent, dateJour);

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
				demandeCongesAnnuels.setSamediOffert(helperService.isSamediOffert(demandeCongesAnnuels));
				demandeCongesAnnuels.setSamediDecompte(demandeCongesAnnuels.isSamediOffert() ? false : helperService
						.isSamediDecompte(demandeCongesAnnuels));
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

		return demande;
	}

	@Override
	public List<DemandeDto> getListeDemandesSIRHAValider() {
		List<Demande> listeSansFiltre = demandeRepository.listeDemandesSIRHAValider();

		List<DemandeDto> listeDto = absenceDataConsistencyRulesImpl.filtreDateAndEtatDemandeFromList(listeSansFiltre,
				filtreRepository.findRefEtatAValider(), null);
		for (DemandeDto dto : listeDto) {
			IAbsenceDataConsistencyRules absenceDataConsistencyRulesImpl = dataConsistencyRulesFactory.getFactory(dto
					.getGroupeAbsence().getIdRefGroupeAbsence(), dto.getIdTypeDemande());
			dto = absenceDataConsistencyRulesImpl.filtreDroitOfDemandeSIRH(dto);
			dto.setDepassementCompteur(absenceDataConsistencyRulesImpl.checkDepassementCompteurAgent(dto));
			dto.setDepassementMultiple(absenceDataConsistencyRulesImpl.checkDepassementMultipleAgent(dto));
		}

		return listeDto;
	}

}
