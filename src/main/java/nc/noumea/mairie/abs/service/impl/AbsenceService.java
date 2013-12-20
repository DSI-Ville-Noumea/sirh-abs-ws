package nc.noumea.mairie.abs.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.PersistenceContext;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeRecup;
import nc.noumea.mairie.abs.domain.Droit;
import nc.noumea.mairie.abs.domain.DroitDroitsAgent;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.RefEtat;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.RefEtatDto;
import nc.noumea.mairie.abs.dto.RefTypeAbsenceDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IAccessRightsRepository;
import nc.noumea.mairie.abs.repository.IDemandeRepository;
import nc.noumea.mairie.abs.service.IAbsenceDataConsistencyRules;
import nc.noumea.mairie.abs.service.IAbsenceService;

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
	@Qualifier("AbsRecuperationDataConsistencyRulesImpl")
	private IAbsenceDataConsistencyRules absRecupDataConsistencyRules;

	@PersistenceContext(unitName = "absPersistenceUnit")
	private EntityManager absEntityManager;

	@Autowired
	private HelperService helperService;

	@Override
	public List<RefEtatDto> getRefEtats() {
		List<RefEtatDto> res = new ArrayList<RefEtatDto>();
		List<RefEtat> refEtats = RefEtat.findAllRefEtats();
		for (RefEtat etat : refEtats) {
			RefEtatDto dto = new RefEtatDto(etat);
			res.add(dto);
		}
		return res;
	}

	@Override
	public List<RefTypeAbsenceDto> getRefTypesAbsence() {
		List<RefTypeAbsenceDto> res = new ArrayList<RefTypeAbsenceDto>();
		List<RefTypeAbsence> refTypeAbs = RefTypeAbsence.findAllRefTypeAbsences();
		for (RefTypeAbsence type : refTypeAbs) {
			RefTypeAbsenceDto dto = new RefTypeAbsenceDto(type);
			res.add(dto);
		}
		return res;
	}

	@Override
	public ReturnMessageDto saveDemande(Integer idAgent, DemandeDto demandeDto) {

		absEntityManager.setFlushMode(FlushModeType.COMMIT);
		ReturnMessageDto returnDto = new ReturnMessageDto();

		// verification des droits
		if (!verifAccessRightDemande(idAgent, demandeDto, returnDto))
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
				// TODO
				break;
			case RECUP:
				DemandeRecup demandeRecup = getDemande(DemandeRecup.class, demandeDto);
				demandeRecup.setDuree(demandeDto.getDuree());
				demande = mappingDemandeDtoToDemande(demandeDto, demandeRecup, idAgent, dateJour);
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
				absEntityManager.clear();
				return returnDto;
		}
		
		rules.processDataConsistencyDemande(returnDto, idAgent, demande, dateJour);
		
		if (returnDto.getErrors().size() != 0) {
			absEntityManager.clear();
			return returnDto;
		}
		
		demandeRepository.mergeEntity(demande);
		absEntityManager.flush();
		absEntityManager.clear();

		return returnDto;
	}
	
	protected <T> T getDemande(Class<T> Tclass, DemandeDto demandeDto) {
		if(null != demandeDto.getIdDemande()) {
			return demandeRepository.getEntity(Tclass, demandeDto.getIdDemande());
		}
		
		try {
			return Tclass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			return null;
		}
	}
	
	protected Demande mappingDemandeDtoToDemande(DemandeDto demandeDto, Demande demande, Integer idAgent, Date dateJour){
		
		// on mappe le DTO dans la Demande generique
		demande.setDateDebut(demandeDto.getDateDebut());
		demande.setIdAgent(demandeDto.getIdAgent());
		RefTypeAbsence rta = new RefTypeAbsence();
		rta.setIdRefTypeAbsence(demandeDto.getIdTypeDemande());
		demande.setType(rta);

		EtatDemande etatDemande = new EtatDemande();
		etatDemande.setDate(dateJour);
		etatDemande.setIdAgent(idAgent);
		if (demandeDto.isEtatDefinitif()) {
			etatDemande.setEtat(RefEtatEnum.SAISIE);
		} else {
			etatDemande.setEtat(RefEtatEnum.PROVISOIRE);
		}
		demande.addEtatDemande(etatDemande);
		
		return demande;
	}

	@Override
	public boolean verifAccessRightDemande(Integer idAgent, DemandeDto demandeDto, ReturnMessageDto returnDto) {
		boolean res = true;
		// si l'agent est un operateur alors on verifie qu'il a bien les droits
		// sur l'agent pour qui il effectue la demande
		if (idAgent != demandeDto.getIdAgent()) {
			if (accessRightsRepository.isUserOperateur(idAgent)) {

				// on recherche tous les sous agents de la personne
				Droit droitOperateur = accessRightsRepository.getAgentDroitFetchAgents(idAgent);
				boolean trouve = false;
				for (DroitDroitsAgent dda : droitOperateur.getDroitDroitsAgent()) {
					if (dda.getDroitsAgent().getIdAgent().equals(demandeDto.getIdAgent())) {
						trouve = true;
						break;
					}
				}
				if (!trouve) {
					res = false;
					logger.warn("Vous n'êtes pas opérateur de l'agent {}. Vous ne pouvez pas saisir de demandes.",
							demandeDto.getIdAgent());
					returnDto.getErrors().add(
							String.format(
									"Vous n'êtes pas opérateur de l'agent %s. Vous ne pouvez pas saisir de demandes.",
									demandeDto.getIdAgent()));
				}
			} else {
				res = false;
				logger.warn("Vous n'êtes pas opérateur. Vous ne pouvez pas saisir de demandes.");
				returnDto.getErrors().add(
						String.format("Vous n'êtes pas opérateur. Vous ne pouvez pas saisir de demandes."));
			}
		}
		return res;
	}

	public DemandeDto getDemandeDto(Integer idDemande, Integer idTypeDemande) {
		DemandeDto demandeDto = null;

		switch (RefTypeAbsenceEnum.getRefTypeAbsenceEnum(idTypeDemande)) {
			case CONGE_ANNUEL:
				// TODO
				break;
			case REPOS_COMP:
				// TODO
				break;
			case RECUP:

				DemandeRecup demandeRecup = demandeRepository.getEntity(DemandeRecup.class, idDemande);
				if (null == demandeRecup) {
					return demandeDto;
				}

				demandeDto = new DemandeDto(demandeRecup);
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

		return demandeDto;
	}

	@Override
	public List<DemandeDto> getListeDemandes(Integer idAgentConnecte, String ongletDemande, Date fromDate, Date toDate,
			Date dateDemande, Integer idRefEtat, Integer idRefType) {
		List<Demande> listeSansFiltre = new ArrayList<Demande>();

		List<RefEtat> etats = new ArrayList<RefEtat>();
		switch (ongletDemande) {
			case "NON_PRISES":
				listeSansFiltre = demandeRepository.listeDemandesAgent(idAgentConnecte, fromDate, toDate, idRefType);
				etats = demandeRepository.findRefEtatNonPris();
				break;
			case "EN_COURS":
				listeSansFiltre = demandeRepository.listeDemandesAgent(idAgentConnecte, fromDate, toDate, idRefType);
				etats = demandeRepository.findRefEtatEnCours();
				break;
			case "TOUTES":
				listeSansFiltre = demandeRepository.listeDemandesAgent(idAgentConnecte, fromDate, toDate, idRefType);
				if (idRefEtat != null) {
					etats.add(absEntityManager.find(RefEtat.class, idRefEtat));
				} else {
					etats = null;
				}
				break;
		}

		return filterDateDemandeAndEtatFromList(listeSansFiltre, etats, dateDemande);
	}

	private List<DemandeDto> filterDateDemandeAndEtatFromList(List<Demande> listeSansFiltre, List<RefEtat> etats,
			Date dateDemande) {
		List<DemandeDto> listeDemandeDto = new ArrayList<DemandeDto>();
		if (listeSansFiltre.size() == 0)
			return listeDemandeDto;

		if (dateDemande == null && etats == null) {
			for (Demande d : listeSansFiltre) {
				DemandeDto dto = new DemandeDto(d);
				listeDemandeDto.add(dto);
			}
			return listeDemandeDto;
		}

		// ON TRAITE LA DATE DE DEMANDE
		if (dateDemande != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			String dateDemandeSDF = sdf.format(dateDemande);
			for (Demande d : listeSansFiltre) {
				String dateEtatSDF = sdf.format(d.getLatestEtatDemande().getDate());
				if (dateEtatSDF.equals(dateDemandeSDF)) {
					DemandeDto dto = new DemandeDto(d);
					listeDemandeDto.add(dto);
				}
			}
		}

		// ON TRAITE L'ETAT
		if (etats != null) {
			for (Demande d : listeSansFiltre) {
				DemandeDto dto = new DemandeDto(d);
				if (etats.contains(absEntityManager.find(RefEtat.class, d.getLatestEtatDemande().getEtat()
						.getCodeEtat()))) {
					if (!listeDemandeDto.contains(dto))
						listeDemandeDto.add(dto);
				} else {
					if (listeDemandeDto.contains(dto))
						listeDemandeDto.remove(dto);
				}
			}
		}

		return listeDemandeDto;
	}
}
