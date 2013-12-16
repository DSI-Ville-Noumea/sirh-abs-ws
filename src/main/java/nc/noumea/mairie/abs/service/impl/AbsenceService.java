package nc.noumea.mairie.abs.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import org.springframework.stereotype.Service;

@Service
public class AbsenceService implements IAbsenceService {

	private Logger logger = LoggerFactory.getLogger(AbsenceService.class);

	@Autowired
	private IDemandeRepository demandeRepository;

	@Autowired
	private IAccessRightsRepository accessRightsRepository;

	@Autowired
	private IAbsenceDataConsistencyRules absDataConsistencyRules;

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

		ReturnMessageDto returnDto = new ReturnMessageDto();

		// verification des droits
		verifAccessRightSaveDemande(idAgent, demandeDto, returnDto);

		Demande demande = demandeRepository.getEntity(Demande.class, demandeDto.getIdDemande());

		Date dateJour = new Date();

		// on mappe le DTO dans la Demande generique
		if (null == demande) {
			demande = new Demande();
			demande.setEtatsDemande(new ArrayList<EtatDemande>());
		}
		demande.setDateDebut(demandeDto.getDateDebut());
		demande.setIdAgent(demandeDto.getIdAgent());
		RefTypeAbsence rta = new RefTypeAbsence();
		rta.setIdRefTypeAbsence(demandeDto.getIdTypeDemande());
		demande.setType(rta);

		EtatDemande etatDemande = new EtatDemande();
		etatDemande.setDate(dateJour);
		etatDemande.setDemande(demande);
		etatDemande.setIdAgent(idAgent);
		demande.getEtatsDemande().add(etatDemande);

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
				DemandeRecup demandeRecup = (DemandeRecup) demande;
				demandeRecup.setDuree(demandeDto.getDuree());
				absDataConsistencyRules.processDataConsistencyDemandeRecup(returnDto, idAgent, demandeRecup, null);
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
				return returnDto;
		}

		if (demandeDto.isEtatDefinitif()) {
			etatDemande.setEtat(RefEtatEnum.SAISIE);
		} else {
			etatDemande.setEtat(RefEtatEnum.PROVISOIRE);
		}

		demandeRepository.persisEntity(demande);

		return returnDto;
	}

	public void verifAccessRightSaveDemande(Integer idAgent, DemandeDto demandeDto, ReturnMessageDto returnDto) {
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
					logger.warn("Vous n'êtes pas opérateur de l'agent {}. Vous ne pouvez pas saisir de demandes.",
							demandeDto.getIdAgent());
					returnDto.getErrors().add(
							String.format(
									"Vous n'êtes pas opérateur de l'agent %s. Vous ne pouvez pas saisir de demandes.",
									demandeDto.getIdAgent()));
				}
			} else {
				logger.warn("Vous n'êtes pas opérateur. Vous ne pouvez pas saisir de demandes.");
				returnDto.getErrors().add(
						String.format("Vous n'êtes pas opérateur. Vous ne pouvez pas saisir de demandes."));
			}
		}

	}

	public DemandeDto getDemande(Integer idDemande, Integer idTypeDemande) {
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
				demandeDto.setDuree(demandeRecup.getDuree());
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
	public List<DemandeDto> getListeDemandesAgent(Integer idAgentConnecte, String ongletDemande, Date fromDate,
			Date toDate, Date dateDemande, Integer idRefEtat, Integer idRefType) {
		List<DemandeDto> listeDemandeDto = new ArrayList<DemandeDto>();
		switch (ongletDemande) {
			case "NON_PRISES":
				List<Demande> listeDemandeNonPrises = demandeRepository.listeDemandesAgentNonPrises(idAgentConnecte,
						fromDate, toDate, dateDemande, idRefEtat, idRefType);
				for (Demande d : listeDemandeNonPrises) {
					DemandeDto dto = new DemandeDto(d);
					listeDemandeDto.add(dto);
				}
				break;
			case "EN_COURS":
				/*
				 * listeDemandeDto =
				 * demandeRepository.listeDemandesAgentEnCours(idAgentConnecte,
				 * fromDate, toDate, dateDemande, idRefEtat, idRefType);
				 */
				break;
			case "TOUTES":
				/*
				 * listeDemandeDto =
				 * demandeRepository.listeDemandesAgent(idAgentConnecte,
				 * fromDate, toDate, dateDemande, idRefEtat, idRefType);
				 */
				break;

			default:
				return listeDemandeDto;
		}

		return listeDemandeDto;
	}
}
