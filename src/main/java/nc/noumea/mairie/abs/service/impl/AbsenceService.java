package nc.noumea.mairie.abs.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.FlushModeType;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeAsa;
import nc.noumea.mairie.abs.domain.DemandeRecup;
import nc.noumea.mairie.abs.domain.DemandeReposComp;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.RefEtat;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IAccessRightsRepository;
import nc.noumea.mairie.abs.repository.IDemandeRepository;
import nc.noumea.mairie.abs.repository.IFiltreRepository;
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
	@Qualifier("DefaultCounterServiceImpl")
	private ICounterService counterService;

	@Autowired
	private CounterServiceFactory counterServiceFactory;

	@Autowired
	private ISirhWSConsumer sirhWSConsumer;

	private static final String ETAT_DEMANDE_INCHANGE = "L'état de la demande est inchangé.";
	private static final String DEMANDE_INEXISTANTE = "La demande n'existe pas.";
	private static final String ETAT_DEMANDE_INCORRECT = "L'état de la demande envoyée n'est pas correct.";

	@Override
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

		// selon le type de demande, on mappe les donnees specifiques de la
		// demande
		// et on effectue les verifications appropriees
		switch (RefTypeAbsenceEnum.getRefTypeAbsenceEnum(demandeDto.getIdTypeDemande())) {
			case CONGE_ANNUEL:
				// TODO
				break;
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
			case ASA_A48:
				DemandeAsa demandeAsa = getDemande(DemandeAsa.class, demandeDto.getIdDemande());
				demandeAsa.setDuree(demandeDto.getDuree());
				demandeAsa.setDateDebutAM(demandeDto.isDateDebutAM());
				demandeAsa.setDateDebutPM(demandeDto.isDateDebutPM());
				demandeAsa.setDateFinAM(demandeDto.isDateFinAM());
				demandeAsa.setDateFinPM(demandeDto.isDateFinPM());
				demande = Demande.mappingDemandeDtoToDemande(demandeDto, demandeAsa, idAgent, dateJour);

				if (null == demande.getType().getTypeSaisi())
					demande.getType().setTypeSaisi(filtreRepository.findRefTypeSaisi(demandeDto.getIdTypeDemande()));

				demande.setDateFin(helperService.getDateFin(demande.getType().getTypeSaisi(), demandeDto.getDateFin(),
						demandeDto.getDateDebut(), demandeDto.getDuree(), demandeDto.isDateFinAM(),
						demandeDto.isDateFinPM()));
				demande.setDateDebut(helperService.getDateDebut(demande.getType().getTypeSaisi(),
						demandeDto.getDateDebut(), demandeDto.isDateDebutAM(), demandeDto.isDateDebutPM()));
				break;
			case AUTRES:
				// TODO
				break;
			case MALADIES:
				// TODO
				break;
			default:
				returnDto.getErrors().add(
						String.format("Le type [%d] de la demande n'est pas reconnu.", demandeDto.getIdTypeDemande()));
				demandeRepository.clear();
				return returnDto;
		}
		absenceDataConsistencyRulesImpl = dataConsistencyRulesFactory.getFactory(demandeDto.getIdTypeDemande());
		// dans le cas des types de demande non geres
		if (null == demande) {
			demande = getDemande(Demande.class, demandeDto.getIdDemande());
			if (null == demande) {
				demande = new Demande();
			}
			demande = Demande.mappingDemandeDtoToDemande(demandeDto, demande, idAgent, dateJour);

			if (null == demande.getType().getTypeSaisi())
				demande.getType().setTypeSaisi(filtreRepository.findRefTypeSaisi(demandeDto.getIdTypeDemande()));

			demande.setDateFin(helperService.getDateFin(demande.getType().getTypeSaisi(), demandeDto.getDateFin(),
					demandeDto.getDateDebut(), demandeDto.getDuree(), demandeDto.isDateFinAM(),
					demandeDto.isDateFinPM()));
		}

		absenceDataConsistencyRulesImpl.processDataConsistencyDemande(returnDto, idAgent, demande, dateJour);

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

	public DemandeDto getDemandeDto(Integer idDemande) {
		DemandeDto demandeDto = null;

		Demande demande = demandeRepository.getEntity(Demande.class, idDemande);

		if (null == demande) {
			return demandeDto;
		}

		switch (RefTypeAbsenceEnum.getRefTypeAbsenceEnum(demande.getType().getIdRefTypeAbsence())) {
			case CONGE_ANNUEL:
				// TODO
				break;
			case REPOS_COMP:

				DemandeReposComp demandeReposComp = demandeRepository.getEntity(DemandeReposComp.class, idDemande);
				if (null == demandeReposComp) {
					return demandeDto;
				}

				demandeDto = new DemandeDto(demandeReposComp, sirhWSConsumer.getAgentService(demande.getIdAgent(),
						helperService.getCurrentDate()));
				demandeDto.updateEtat(demandeReposComp.getLatestEtatDemande());
				break;
			case RECUP:

				DemandeRecup demandeRecup = demandeRepository.getEntity(DemandeRecup.class, idDemande);
				if (null == demandeRecup) {
					return demandeDto;
				}

				demandeDto = new DemandeDto(demandeRecup, sirhWSConsumer.getAgentService(demande.getIdAgent(),
						helperService.getCurrentDate()));
				demandeDto.updateEtat(demandeRecup.getLatestEtatDemande());
				break;
			case ASA_A48:
				DemandeAsa demandeAsaA48 = demandeRepository.getEntity(DemandeAsa.class, idDemande);
				if (null == demandeAsaA48) {
					return demandeDto;
				}

				demandeDto = new DemandeDto(demandeAsaA48, sirhWSConsumer.getAgentService(demande.getIdAgent(),
						helperService.getCurrentDate()));
				demandeDto.updateEtat(demandeAsaA48.getLatestEtatDemande());
				break;
			case AUTRES:
				// TODO
				break;
			case MALADIES:
				// TODO
				break;
			default:
				return demandeDto;
		}

		if (null == demandeDto && null != demande) {
			demandeDto = new DemandeDto(demande, sirhWSConsumer.getAgentService(demande.getIdAgent(),
					helperService.getCurrentDate()));
			demandeDto.updateEtat(demande.getLatestEtatDemande());
		}

		return demandeDto;
	}

	@Override
	public List<DemandeDto> getListeDemandes(Integer idAgentConnecte, Integer idAgentConcerne, String ongletDemande,
			Date fromDate, Date toDate, Date dateDemande, Integer idRefEtat, Integer idRefType) {

		// si date de debut et de fin nulles, alors on filtre sur 12 mois
		// glissants

		if (null == fromDate && null == toDate) {
			fromDate = helperService.getCurrentDateMoinsUnAn();
		}

		List<Demande> listeSansFiltre = getListeNonFiltreeDemandes(idAgentConnecte, idAgentConcerne, fromDate, toDate,
				idRefType);

		List<RefEtat> etats = filtresService.getListeEtatsByOnglet(ongletDemande, idRefEtat);

		return absenceDataConsistencyRulesImpl.filtreListDemande(idAgentConnecte, idAgentConcerne, listeSansFiltre,
				etats, dateDemande);
	}

	protected List<Demande> getListeNonFiltreeDemandes(Integer idAgentConnecte, Integer idAgentConcerne, Date fromDate,
			Date toDate, Integer idRefType) {

		List<Demande> listeSansFiltre = new ArrayList<Demande>();
		List<Demande> listeSansFiltredelegataire = new ArrayList<Demande>();

		Integer idApprobateurOfDelegataire = accessRightsService.getIdApprobateurOfDelegataire(idAgentConnecte,
				idAgentConcerne);

		listeSansFiltre = demandeRepository.listeDemandesAgent(idAgentConnecte, idAgentConcerne, fromDate, toDate,
				idRefType);
		if (null != idApprobateurOfDelegataire) {
			listeSansFiltredelegataire = demandeRepository.listeDemandesAgent(idApprobateurOfDelegataire,
					idAgentConcerne, fromDate, toDate, idRefType);
		}

		for (Demande demandeDeleg : listeSansFiltredelegataire) {
			if (!listeSansFiltre.contains(demandeDeleg)) {
				listeSansFiltre.add(demandeDeleg);
			}
		}

		return listeSansFiltre;
	}

	@Override
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

		if (0 < result.getErrors().size()) {
			return result;
		}

		counterService = counterServiceFactory.getFactory(demande.getType().getIdRefTypeAbsence());
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

		return result;
	}

	protected ReturnMessageDto setDemandeEtatAnnule(Integer idAgent, DemandeEtatChangeDto demandeEtatChangeDto,
			Demande demande, ReturnMessageDto result) {

		absenceDataConsistencyRulesImpl = dataConsistencyRulesFactory.getFactory(demande.getType()
				.getIdRefTypeAbsence());
		result = absenceDataConsistencyRulesImpl.checkEtatsDemandeAnnulee(result, demande,
				Arrays.asList(RefEtatEnum.VISEE_FAVORABLE, RefEtatEnum.VISEE_DEFAVORABLE, RefEtatEnum.APPROUVEE));

		if (0 < result.getErrors().size()) {
			return result;
		}

		counterService = counterServiceFactory.getFactory(demande.getType().getIdRefTypeAbsence());
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

		EtatDemande etatDemande = new EtatDemande();
		etatDemande.setDate(new Date());
		etatDemande.setMotif(demandeEtatChangeDto.getMotif());
		etatDemande.setEtat(RefEtatEnum.getRefEtatEnum(demandeEtatChangeDto.getIdRefEtat()));
		etatDemande.setIdAgent(idAgent);
		demande.addEtatDemande(etatDemande);
	}

	@Override
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
		if (demande.getLatestEtatDemande().getEtat() != RefEtatEnum.APPROUVEE) {
			result.getErrors().add(
					String.format("La demande %s n'est pas à l'état %s mais %s.", idDemande,
							RefEtatEnum.APPROUVEE.toString(), demande.getLatestEtatDemande().getEtat().toString()));
			logger.error("Demande id {} is not in state [{}] but [{}]. Stopping process.", idDemande,
					RefEtatEnum.APPROUVEE.toString(), demande.getLatestEtatDemande().getEtat().toString());
			return result;
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

		// selon le type de demande, on mappe les donnees specifiques de la
		// demande
		// et on effectue les verifications appropriees
		switch (RefTypeAbsenceEnum.getRefTypeAbsenceEnum(demandeDto.getIdTypeDemande())) {
			case CONGE_ANNUEL:
				// TODO
				break;
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
			case ASA_A48:
				DemandeAsa demandeAsa = getDemande(DemandeAsa.class, demandeDto.getIdDemande());
				demandeAsa.setDuree(demandeDto.getDuree());
				demandeAsa.setDateDebutAM(demandeDto.isDateDebutAM());
				demandeAsa.setDateDebutPM(demandeDto.isDateDebutPM());
				demandeAsa.setDateFinAM(demandeDto.isDateFinAM());
				demandeAsa.setDateFinPM(demandeDto.isDateFinPM());
				demande = Demande.mappingDemandeDtoToDemande(demandeDto, demandeAsa, idAgent, dateJour);

				if (null == demande.getType().getTypeSaisi())
					demande.getType().setTypeSaisi(filtreRepository.findRefTypeSaisi(demandeDto.getIdTypeDemande()));

				demande.setDateFin(helperService.getDateFin(demande.getType().getTypeSaisi(), demandeDto.getDateFin(),
						demandeDto.getDateDebut(), demandeDto.getDuree(), demandeDto.isDateFinAM(),
						demandeDto.isDateFinPM()));
				demande.setDateDebut(helperService.getDateDebut(demande.getType().getTypeSaisi(),
						demandeDto.getDateDebut(), demandeDto.isDateDebutAM(), demandeDto.isDateDebutPM()));
				break;
			case AUTRES:
				// TODO
				break;
			case MALADIES:
				// TODO
				break;
			default:
				returnDto.getErrors().add(
						String.format("Le type [%d] de la demande n'est pas reconnu.", demandeDto.getIdTypeDemande()));
				demandeRepository.clear();
				return returnDto;
		}

		absenceDataConsistencyRulesImpl = dataConsistencyRulesFactory.getFactory(demandeDto.getIdTypeDemande());
		// dans le cas des types de demande non geres ==> //TODO a supprimer par
		// la suite
		if (null == demande) {
			demande = getDemande(Demande.class, demandeDto.getIdDemande());
			if (null == demande) {
				demande = new Demande();
			}
			demande = Demande.mappingDemandeDtoToDemande(demandeDto, demande, idAgent, dateJour);
			demande.setDateFin(helperService.getDateFin(demande.getType().getTypeSaisi(), demandeDto.getDateFin(),
					demandeDto.getDateDebut(), demandeDto.getDuree(), demandeDto.isDateFinAM(),
					demandeDto.isDateFinPM()));
		}

		absenceDataConsistencyRulesImpl.processDataConsistencyDemande(returnDto, idAgent, demande, dateJour);

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
	public List<DemandeDto> getListeDemandesSIRH(Date fromDate, Date toDate, Integer idRefEtat, Integer idRefType,
			Integer idAgentRecherche) {

		List<Demande> listeSansFiltre = demandeRepository.listeDemandesSIRH(fromDate, toDate, idRefEtat, idRefType,
				idAgentRecherche);
		List<RefEtat> listEtats = null;
		if (idRefEtat != null) {
			RefEtat etat = demandeRepository.getEntity(RefEtat.class, idRefEtat);
			listEtats = new ArrayList<RefEtat>();
			listEtats.add(etat);
		}

		List<DemandeDto> listeDto = absenceDataConsistencyRulesImpl.filtreDateAndEtatDemandeFromList(listeSansFiltre,
				listEtats, null);
		for (DemandeDto dto : listeDto) {
			absenceDataConsistencyRulesImpl = dataConsistencyRulesFactory.getFactory(dto.getIdTypeDemande());
			dto.setDepassementCompteur(absenceDataConsistencyRulesImpl.checkDepassementCompteurAgent(dto));
		}

		return listeDto;
	}

	@Override
	public List<DemandeDto> getDemandesArchives(Integer idDemande) {

		List<DemandeDto> result = new ArrayList<DemandeDto>();
		Demande dem = demandeRepository.getEntity(Demande.class, idDemande);

		for (EtatDemande etat : dem.getEtatsDemande()) {
			DemandeDto dto = new DemandeDto(dem, sirhWSConsumer.getAgentService(etat.getIdAgent(),
					helperService.getCurrentDate()));
			dto.updateEtat(etat);
			result.add(dto);
		}

		return result;
	}

	@Override
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

		counterService = counterServiceFactory.getFactory(demande.getType().getIdRefTypeAbsence());
		result = counterService.majCompteurToAgent(result, demande, demandeEtatChangeDto);

		if (0 < result.getErrors().size()) {
			return;
		}

		// maj de la demande
		majEtatDemande(idAgent, demandeEtatChangeDto, demande);

		if (demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.REJETE.getCodeEtat())) {
			result.getInfos().add(String.format("La demande est rejetée."));
		}
		if (demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.VALIDEE.getCodeEtat())) {
			result.getInfos().add(String.format("La demande est validée."));
		}
	}

	protected void setDemandeEtatEnAttente(Integer idAgent, DemandeEtatChangeDto demandeEtatChangeDto, Demande demande,
			ReturnMessageDto result) {

		result = absenceDataConsistencyRulesImpl.checkEtatsDemandeAcceptes(result, demande,
				Arrays.asList(RefEtatEnum.APPROUVEE));

		if (0 < result.getErrors().size()) {
			return;
		}

		// maj de la demande
		majEtatDemande(idAgent, demandeEtatChangeDto, demande);

		result.getInfos().add(String.format("La demande est en attente."));
	}

}
