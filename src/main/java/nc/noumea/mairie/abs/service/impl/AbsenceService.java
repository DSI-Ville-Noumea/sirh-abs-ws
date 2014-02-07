package nc.noumea.mairie.abs.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.FlushModeType;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeRecup;
import nc.noumea.mairie.abs.domain.DemandeReposComp;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.RefEtat;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.EmailInfoDto;
import nc.noumea.mairie.abs.dto.RefEtatDto;
import nc.noumea.mairie.abs.dto.RefTypeAbsenceDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IAccessRightsRepository;
import nc.noumea.mairie.abs.repository.IDemandeRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.abs.service.IAbsenceDataConsistencyRules;
import nc.noumea.mairie.abs.service.IAbsenceService;
import nc.noumea.mairie.abs.service.IAccessRightsService;
import nc.noumea.mairie.abs.service.ICounterService;
import nc.noumea.mairie.abs.service.counter.impl.CounterServiceFactory;
import nc.noumea.mairie.domain.Spcarr;
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
	private IAccessRightsRepository accessRightsRepository;

	@Autowired
	private IAccessRightsService accessRightsService;

	@Autowired
	private ISirhRepository sirhRepository;
	
	@Autowired
	@Qualifier("DefaultAbsenceDataConsistencyRulesImpl")
	private IAbsenceDataConsistencyRules defaultAbsenceDataConsistencyRulesImpl;

	@Autowired
	@Qualifier("AbsRecuperationDataConsistencyRulesImpl")
	private IAbsenceDataConsistencyRules absRecupDataConsistencyRules;
	
	@Autowired
	@Qualifier("AbsReposCompensateurDataConsistencyRulesImpl")
	private IAbsenceDataConsistencyRules absReposCompDataConsistencyRules;
	
	@Autowired
	private HelperService helperService;

	@Autowired
	@Qualifier("DefaultCounterServiceImpl")
	private ICounterService counterService;
	
	@Autowired
	private CounterServiceFactory counterServiceFactory;
	
	@Autowired
	private ISirhWSConsumer sirhWSConsumer;
	 

	public static final String ONGLET_NON_PRISES = "NON_PRISES";
	public static final String ONGLET_EN_COURS = "EN_COURS";
	public static final String ONGLET_TOUTES = "TOUTES";

	@Override
	public List<RefEtatDto> getRefEtats(String ongletDemande) {

		List<RefEtatDto> res = new ArrayList<RefEtatDto>();
		List<RefEtat> refEtats = getListeEtatsByOnglet(ongletDemande, null);

		for (RefEtat etat : refEtats) {
			RefEtatDto dto = new RefEtatDto(etat);
			res.add(dto);
		}
		return res;
	}

	@Override
	public List<RefTypeAbsenceDto> getRefTypesAbsence(Integer idAgentConcerne) {
		List<RefTypeAbsenceDto> res = new ArrayList<RefTypeAbsenceDto>();
		List<RefTypeAbsence> refTypeAbs = demandeRepository.findAllRefTypeAbsences();
		
		Spcarr carr = null;
		if(null != idAgentConcerne) {
			carr = sirhRepository.getAgentCurrentCarriere(idAgentConcerne, helperService.getCurrentDate());
		}
		
		for (RefTypeAbsence type : refTypeAbs) {
			if (null == carr
					|| carr.getCdcate() == 4 
					|| carr.getCdcate() == 7
					|| !RefTypeAbsenceEnum.getRefTypeAbsenceEnum(type.getIdRefTypeAbsence()).equals(RefTypeAbsenceEnum.REPOS_COMP)) {
				
				RefTypeAbsenceDto dto = new RefTypeAbsenceDto(type);
				res.add(dto);
			}
		}
		return res;
	}

	@Override
	public ReturnMessageDto saveDemande(Integer idAgent, DemandeDto demandeDto) {

		demandeRepository.setFlushMode(FlushModeType.COMMIT);
		ReturnMessageDto returnDto = new ReturnMessageDto();

		// verification des droits
		returnDto = accessRightsService.verifAccessRightDemande(idAgent, demandeDto.getIdAgent(), returnDto);
		if (!returnDto.getErrors().isEmpty())
			return returnDto;

		Demande demande = null;
		IAbsenceDataConsistencyRules rules = null;
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
				demandeReposComp.setDuree(demandeDto.getDuree());
				demande = Demande.mappingDemandeDtoToDemande(demandeDto, demandeReposComp, idAgent, dateJour);
				demande.setDateFin(helperService.getDateFin(demandeDto.getDateDebut(), demandeDto.getDuree()));
				rules = absReposCompDataConsistencyRules;
				break;
			case RECUP:
				DemandeRecup demandeRecup = getDemande(DemandeRecup.class, demandeDto.getIdDemande());
				demandeRecup.setDuree(demandeDto.getDuree());
				demande = Demande.mappingDemandeDtoToDemande(demandeDto, demandeRecup, idAgent, dateJour);
				demande.setDateFin(helperService.getDateFin(demandeDto.getDateDebut(), demandeDto.getDuree()));
				rules = absRecupDataConsistencyRules;
				break;
			case ASA:
				// TODO
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
		// dans le cas des types de demande non geres
		if (null == rules) {
			rules = defaultAbsenceDataConsistencyRulesImpl;
			demande = getDemande(Demande.class, demandeDto.getIdDemande());
			if (null == demande) {
				demande = new Demande();
			}
			demande = Demande.mappingDemandeDtoToDemande(demandeDto, demande, idAgent, dateJour);
			demande.setDateFin(helperService.getDateFin(demandeDto.getDateDebut(), demandeDto.getDuree()));
		}

		rules.processDataConsistencyDemande(returnDto, idAgent, demande, dateJour);

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

				demandeDto = new DemandeDto(demandeReposComp, sirhWSConsumer.getAgentService(demande.getIdAgent(), helperService.getCurrentDate()));
				break;
			case RECUP:

				DemandeRecup demandeRecup = demandeRepository.getEntity(DemandeRecup.class, idDemande);
				if (null == demandeRecup) {
					return demandeDto;
				}

				demandeDto = new DemandeDto(demandeRecup, sirhWSConsumer.getAgentService(demande.getIdAgent(), helperService.getCurrentDate()));
				break;
			case ASA:
				// TODO
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
			demandeDto = new DemandeDto(demande, sirhWSConsumer.getAgentService(demande.getIdAgent(), helperService.getCurrentDate()));
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

		List<RefEtat> etats = getListeEtatsByOnglet(ongletDemande, idRefEtat);

		return defaultAbsenceDataConsistencyRulesImpl.filtreListDemande(idAgentConnecte, idAgentConcerne,
				listeSansFiltre, etats, dateDemande);
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

	protected List<RefEtat> getListeEtatsByOnglet(String ongletDemande, Integer idRefEtat) {

		List<RefEtat> etats = new ArrayList<RefEtat>();

		if (null == ongletDemande) {
			etats = demandeRepository.findAllRefEtats();
			return etats;
		}

		switch (ongletDemande) {
			case ONGLET_NON_PRISES:
				if (idRefEtat != null) {
					etats.add(demandeRepository.getEntity(RefEtat.class, idRefEtat));
				} else {
					etats = demandeRepository.findRefEtatNonPris();
				}
				break;
			case ONGLET_EN_COURS:
				if (idRefEtat != null) {
					etats.add(demandeRepository.getEntity(RefEtat.class, idRefEtat));
				} else {
					etats = demandeRepository.findRefEtatEnCours();
				}
				break;
			case ONGLET_TOUTES:
				if (idRefEtat != null) {
					etats.add(demandeRepository.getEntity(RefEtat.class, idRefEtat));
				} else {
					etats = demandeRepository.findAllRefEtats();
				}
				break;
		}

		return etats;
	}

	@Override
	public ReturnMessageDto setDemandeEtat(Integer idAgent, DemandeEtatChangeDto demandeEtatChangeDto) {

		ReturnMessageDto result = new ReturnMessageDto();

		if (!demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat())
				&& !demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat())
				&& !demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.APPROUVEE.getCodeEtat())
				&& !demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.REFUSEE.getCodeEtat())
				&& !demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.ANNULEE.getCodeEtat())) {

			logger.warn("L'état de la demande envoyé n'est pas correcte.");
			result.getErrors().add(String.format("L'état de la demande envoyé n'est pas correcte."));
			return result;
		}

		Demande demande = getDemande(Demande.class, demandeEtatChangeDto.getIdDemande());

		if (null == demande) {
			logger.warn("La demande n'existe pas.");
			result.getErrors().add(String.format("La demande n'existe pas."));
			return result;
		}

		if (null != demande.getLatestEtatDemande()
				&& demandeEtatChangeDto.getIdRefEtat().equals(demande.getLatestEtatDemande().getEtat().getCodeEtat())) {
			logger.warn("L'état de la demande est inchangé.");
			result.getErrors().add(String.format("L'état de la demande est inchangé."));
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
		result = defaultAbsenceDataConsistencyRulesImpl.checkEtatsDemandeAcceptes(result, demande,
				Arrays.asList(RefEtatEnum.SAISIE, RefEtatEnum.VISEE_FAVORABLE, RefEtatEnum.VISEE_DEFAVORABLE));

		if (0 < result.getErrors().size()) {
			return result;
		}

		// maj de la demande
		majEtatDemande(idAgent, demandeEtatChangeDto, demande);

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

		result = defaultAbsenceDataConsistencyRulesImpl.checkEtatsDemandeAcceptes(result, demande, Arrays.asList(
				RefEtatEnum.SAISIE, RefEtatEnum.VISEE_FAVORABLE, RefEtatEnum.VISEE_DEFAVORABLE, RefEtatEnum.APPROUVEE,
				RefEtatEnum.REFUSEE));

		result = defaultAbsenceDataConsistencyRulesImpl.checkChampMotifPourEtatDonne(result,
				demandeEtatChangeDto.getIdRefEtat(), demandeEtatChangeDto.getIdMotifAvis());

		if (0 < result.getErrors().size()) {
			return result;
		}

		counterService = counterServiceFactory.getFactory(demande.getType().getIdRefTypeAbsence());
		int minutes = counterService.calculMinutesCompteur(demandeEtatChangeDto, demande);
		if (0 != minutes) {
			result = counterService.majCompteurToAgent(result, demande, minutes);
		}

		if (0 < result.getErrors().size()) {
			return result;
		}

		// maj de la demande
		majEtatDemande(idAgent, demandeEtatChangeDto, demande);

		return result;
	}

	protected ReturnMessageDto setDemandeEtatAnnule(Integer idAgent, DemandeEtatChangeDto demandeEtatChangeDto,
			Demande demande, ReturnMessageDto result) {

		// on verifie les droits
		// verification des droits
		result = accessRightsService.verifAccessRightDemande(idAgent, demande.getIdAgent(), result);
		if (!result.getErrors().isEmpty())
			return result;

		result = defaultAbsenceDataConsistencyRulesImpl
				.checkEtatsDemandeAcceptes(result, demande, Arrays.asList(RefEtatEnum.VISEE_FAVORABLE,
						RefEtatEnum.VISEE_DEFAVORABLE, RefEtatEnum.APPROUVEE, RefEtatEnum.REFUSEE));

		if (0 < result.getErrors().size()) {
			return result;
		}

		counterService = counterServiceFactory.getFactory(demande.getType().getIdRefTypeAbsence());
		int minutes = counterService.calculMinutesCompteur(demandeEtatChangeDto, demande);
		if (0 != minutes) {
			result = counterService.majCompteurToAgent(result, demande, minutes);
		}

		if (0 < result.getErrors().size()) {
			return result;
		}

		// maj de la demande
		majEtatDemande(idAgent, demandeEtatChangeDto, demande);

		return result;
	}

	private void majEtatDemande(Integer idAgent, DemandeEtatChangeDto demandeEtatChangeDto, Demande demande) {
		EtatDemande etatDemande = new EtatDemande();
		etatDemande.setDate(demandeEtatChangeDto.getDateAvis());
		etatDemande.setIdMotifRefus(demandeEtatChangeDto.getIdMotifAvis());
		etatDemande.setMotifViseur(demandeEtatChangeDto.getMotifViseur());

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
	public ReturnMessageDto supprimerDemandeEtatProvisoire(Integer idDemande) {

		logger.info("Trying to delete demande id {} ...", idDemande);

		ReturnMessageDto result = new ReturnMessageDto();

		Demande demande = getDemande(Demande.class, idDemande);

		if (demande == null) {
			result.getErrors().add(String.format("La demande %s n'existe pas.", idDemande));
			logger.error("Demande id {} does not exists. Stopping process.", idDemande);
			return result;
		}
		if (demande.getLatestEtatDemande().getEtat() != RefEtatEnum.PROVISOIRE) {
			result.getErrors().add(
					String.format("La demande %s n'est pas à l'état %s mais %s.", idDemande,
							RefEtatEnum.PROVISOIRE.toString(), demande.getLatestEtatDemande().getEtat()));
			logger.error("Demande id {} is not in state [{}] but [{}]. Stopping process.", idDemande,
					RefEtatEnum.PROVISOIRE.toString(), demande.getLatestEtatDemande().getEtat().toString());
			return result;
		}

		// on supprime la demande et ses etats
		demandeRepository.removeEntity(demande);
		logger.info("Deleted demande id {}.", idDemande);

		return result;
	}

	@Override
	public ReturnMessageDto supprimerDemande(Integer idAgent, Integer idDemande) {

		ReturnMessageDto returnDto = new ReturnMessageDto();

		Demande demande = demandeRepository.getEntity(Demande.class, idDemande);
		IAbsenceDataConsistencyRules rules = defaultAbsenceDataConsistencyRulesImpl;

		// on verifie si la demande existe
		returnDto = rules.verifDemandeExiste(demande, returnDto);
		if (0 < returnDto.getErrors().size())
			return returnDto;

		// selon le type de demande, on mappe les donnees specifiques de la
		// demande
		// et on effectue les verifications appropriees
		switch (RefTypeAbsenceEnum.getRefTypeAbsenceEnum(demande.getType().getIdRefTypeAbsence())) {
			case CONGE_ANNUEL:
				// TODO
				break;
			case REPOS_COMP:
				demande = getDemande(DemandeReposComp.class, idDemande);
				rules = absReposCompDataConsistencyRules;
				break;
			case RECUP:
				demande = getDemande(DemandeRecup.class, idDemande);
				rules = absRecupDataConsistencyRules;
				break;
			case ASA:
				// TODO
				break;
			case AUTRES:
				// TODO
				break;
			case MALADIES:
				// TODO
				break;
			default:
				returnDto.getErrors().add(
						String.format("Le type [%d] de la demande n'est pas reconnu.", demande.getType()
								.getIdRefTypeAbsence()));
				demandeRepository.clear();
				return returnDto;
		}

		// on verifie si la demande existe
		returnDto = rules.verifDemandeExiste(demande, returnDto);
		if (0 < returnDto.getErrors().size())
			return returnDto;

		// verification des droits
		returnDto = accessRightsService.verifAccessRightDemande(idAgent, demande.getIdAgent(), returnDto);
		if (!returnDto.getErrors().isEmpty())
			return returnDto;

		// verifier l etat de la demande
		returnDto = rules.checkEtatsDemandeAcceptes(returnDto, demande,
				Arrays.asList(RefEtatEnum.PROVISOIRE, RefEtatEnum.SAISIE));

		if (0 < returnDto.getErrors().size()) {
			return returnDto;
		}

		// suppression
		demandeRepository.removeEntity(demande);

		returnDto.getInfos().add(String.format("La demande est supprimée."));
		
		return returnDto;
	}

	@Override
	public EmailInfoDto getListIdDestinatairesEmailInfo() {

		EmailInfoDto dto = new EmailInfoDto();

		List<Integer> listeTypes = new ArrayList<Integer>();
		listeTypes.add(RefTypeAbsenceEnum.RECUP.getValue());
		listeTypes.add(RefTypeAbsenceEnum.REPOS_COMP.getValue());

		dto.setListViseurs(demandeRepository.getListViseursDemandesSaisiesJourDonne(listeTypes));

		dto.setListApprobateurs(demandeRepository.getListApprobateursDemandesSaisiesViseesJourDonne(listeTypes));

		return dto;
	}

}
