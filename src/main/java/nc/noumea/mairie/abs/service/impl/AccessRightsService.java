package nc.noumea.mairie.abs.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.NoResultException;

import nc.noumea.mairie.abs.domain.Droit;
import nc.noumea.mairie.abs.domain.DroitDroitsAgent;
import nc.noumea.mairie.abs.domain.DroitProfil;
import nc.noumea.mairie.abs.domain.DroitsAgent;
import nc.noumea.mairie.abs.domain.Profil;
import nc.noumea.mairie.abs.domain.ProfilEnum;
import nc.noumea.mairie.abs.dto.AccessRightsDto;
import nc.noumea.mairie.abs.dto.ActeursDto;
import nc.noumea.mairie.abs.dto.AgentDto;
import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.abs.dto.ApprobateurDto;
import nc.noumea.mairie.abs.dto.EntiteDto;
import nc.noumea.mairie.abs.dto.InputterDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.dto.StatutEntiteEnum;
import nc.noumea.mairie.abs.dto.ViseursDto;
import nc.noumea.mairie.abs.repository.IAccessRightsRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.abs.service.IAccessRightsService;
import nc.noumea.mairie.abs.service.IAgentService;
import nc.noumea.mairie.sirh.comparator.ApprobateurDtoComparator;
import nc.noumea.mairie.ws.IAdsWSConsumer;
import nc.noumea.mairie.ws.ISirhWSConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccessRightsService implements IAccessRightsService {

	private Logger logger = LoggerFactory.getLogger(AccessRightsService.class);

	@Autowired
	private IAccessRightsRepository accessRightsRepository;

	@Autowired
	private HelperService helperService;

	@Autowired
	private ISirhWSConsumer sirhWSConsumer;

	@Autowired
	private ISirhRepository sirhRepository;

	@Autowired
	private IAgentService agentService;

	@Autowired
	protected IAdsWSConsumer adsWsConsumer;

	@Override
	@Transactional(readOnly = true)
	public AccessRightsDto getAgentAccessRights(Integer idAgent) {

		AccessRightsDto result = new AccessRightsDto();
		try {
			Droit da = accessRightsRepository.getAgentAccessRights(idAgent);

			if (null == da) {
				logger.debug("Aucun droit trouvé pour l'agent {}" + idAgent);
				return result;
			}

			for (DroitProfil dpr : da.getDroitProfils()) {
				Profil pr = dpr.getProfil();
				result.setSaisie(result.isSaisie() || pr.isSaisie());
				result.setModification(result.isModification() || pr.isModification());
				result.setSuppression(result.isSuppression() || pr.isSuppression());
				result.setImpression(result.isImpression() || pr.isImpression());
				result.setViserVisu(result.isViserVisu() || pr.isViserVisu());
				result.setViserModif(result.isViserModif() || pr.isViserModif());
				result.setApprouverVisu(result.isApprouverVisu() || pr.isApprouverVisu());
				result.setApprouverModif(result.isApprouverModif() || pr.isApprouverModif());
				result.setAnnuler(result.isAnnuler() || pr.isAnnuler());
				result.setVisuSolde(result.isVisuSolde() || pr.isVisuSolde());
				result.setMajSolde(result.isMajSolde() || pr.isMajSolde());
				result.setDroitAcces(result.isDroitAcces() || pr.isDroitAcces());
			}

			// seuls les operateurs de la DPM peuvent saisir les jours de repos
			// seuls les operateurs peuvent mettre a jour les compteurs (solde)
			// on recup les agents de l'operateur
			List<Integer> listAgentDto = new ArrayList<Integer>();
			for (DroitDroitsAgent droitAg : da.getDroitDroitsAgent()) {
				listAgentDto.add(droitAg.getDroitsAgent().getIdAgent());
			}

			List<AgentWithServiceDto> listAgentsServiceDto = sirhWSConsumer.getListAgentsWithService(listAgentDto, null);

			if (result.isMajSolde()) {
				boolean contientAgentDPM = false;
				for (DroitDroitsAgent droitAg : da.getDroitDroitsAgent()) {
					AgentWithServiceDto agentServiceDto = getAgentOfListAgentWithServiceDto(listAgentsServiceDto, droitAg.getDroitsAgent().getIdAgent());
					if (null != agentServiceDto && null != agentServiceDto.getSigleDirection() && agentServiceDto.getSigleDirection().toUpperCase().equals("DPM")) {
						contientAgentDPM = true;
						break;
					}
				}

				result.setSaisieGarde(contientAgentDPM);
			} else {
				result.setSaisieGarde(false);
			}
		} catch (NoResultException e) {
			logger.debug("Aucun droit trouvé pour l'agent {}" + idAgent);
			return result;
		}

		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public List<ApprobateurDto> getApprobateurs(Integer idAgent, Integer idServiceADS) {

		List<ApprobateurDto> agentDtos = new ArrayList<ApprobateurDto>();
		List<Droit> listeDroit = new ArrayList<Droit>();
		if (idAgent != null) {
			Droit d = accessRightsRepository.getDroitByProfilAndAgent(ProfilEnum.APPROBATEUR.toString(), idAgent);
			if (d != null) {
				listeDroit.add(d);
			}
		} else {
			listeDroit = accessRightsRepository.getAgentsApprobateurs();
		}
		List<Integer> listeSouService = new ArrayList<>();

		if (idServiceADS != null) {
			// on charge la liste des sous-services du service
			List<EntiteDto> liste = new ArrayList<EntiteDto>();
			EntiteDto entiteParent = adsWsConsumer.getEntiteWithChildrenByIdEntite(idServiceADS);
			liste.addAll(entiteParent.getEnfants());

			for (EntiteDto s : liste) {
				listeSouService.add(s.getIdEntite());
			}
			listeSouService.add(idServiceADS);
		}

		List<Integer> listAgentDto = new ArrayList<Integer>();
		for (Droit da : listeDroit) {
			listAgentDto.add(da.getIdAgent());
		}

		List<AgentWithServiceDto> listAgentsServiceDto = sirhWSConsumer.getListAgentsWithService(listAgentDto, helperService.getCurrentDate());

		for (Droit da : listeDroit) {
			AgentWithServiceDto agentServiceDto = getAgentOfListAgentWithServiceDto(listAgentsServiceDto, da.getIdAgent());
			if (idServiceADS != null) {
				if (agentServiceDto != null && listeSouService.contains(agentServiceDto.getIdServiceADS())) {
					ApprobateurDto agentDto = new ApprobateurDto();
					agentDto.setApprobateur(agentServiceDto);
					InputterDto deleg = getDelegator(da.getIdAgent());
					agentDto.setDelegataire(deleg != null ? deleg.getDelegataire() : null);
					agentDtos.add(agentDto);
				}
			} else {
				if (null == agentServiceDto) {
					// c'est que l'agent n'a pas d'affectation en cours alors on
					// cherche l'agent sans son service
					AgentGeneriqueDto agGenerique = sirhWSConsumer.getAgent(da.getIdAgent());
					if (null == agGenerique) {
						logger.debug("Aucun agent actif trouvé dans SIRH {}" + da.getIdAgent());
					} else {
						AgentWithServiceDto ag = new AgentWithServiceDto(agGenerique);
						ApprobateurDto agentDto = new ApprobateurDto();
						agentDto.setApprobateur(ag);
						InputterDto deleg = getDelegator(da.getIdAgent());
						agentDto.setDelegataire(deleg != null ? deleg.getDelegataire() : null);
						agentDtos.add(agentDto);
					}
				} else {
					ApprobateurDto agentDto = new ApprobateurDto();
					agentDto.setApprobateur(agentServiceDto);
					InputterDto deleg = getDelegator(da.getIdAgent());
					agentDto.setDelegataire(deleg != null ? deleg.getDelegataire() : null);
					agentDtos.add(agentDto);
				}
			}
		}
		Collections.sort(agentDtos, new ApprobateurDtoComparator());
		return agentDtos;
	}

	private AgentWithServiceDto getAgentOfListAgentWithServiceDto(List<AgentWithServiceDto> listAgents, Integer idAgent) {

		if (null != listAgents && null != idAgent) {
			for (AgentWithServiceDto agent : listAgents) {
				if (agent.getIdAgent().equals(idAgent)) {
					return agent;
				}
			}
		}
		return null;
	}

	@Override
	@Transactional(value = "absTransactionManager")
	public ReturnMessageDto setApprobateur(AgentWithServiceDto dto) {
		ReturnMessageDto res = new ReturnMessageDto();

		Droit d = accessRightsRepository.getDroitByProfilAndAgent(ProfilEnum.APPROBATEUR.toString(), dto.getIdAgent());

		if (d == null) {
			// on cherche si il a deja un droit
			d = accessRightsRepository.getAgentAccessRights(dto.getIdAgent());
			if (d == null)
				d = new Droit();
			DroitProfil dp = new DroitProfil();
			dp.setDroit(d);
			dp.setDroitApprobateur(d);
			dp.setProfil(accessRightsRepository.getProfilByName(ProfilEnum.APPROBATEUR.toString()));
			d.getDroitProfils().add(dp);
			d.setDateModification(helperService.getCurrentDate());
			d.setIdAgent(dto.getIdAgent());
			accessRightsRepository.persisEntity(d);
		}

		return res;

	}

	@Override
	@Transactional(value = "absTransactionManager")
	public ReturnMessageDto deleteApprobateur(AgentWithServiceDto dto) {
		ReturnMessageDto res = new ReturnMessageDto();
		Droit d = accessRightsRepository.getDroitByProfilAndAgent(ProfilEnum.APPROBATEUR.toString(), dto.getIdAgent());

		if (d != null) {
			// on supprime tous les inputters (et sous agents) de l'approbateur
			setInputter(d.getIdAgent(), new InputterDto());

			setViseurs(d.getIdAgent(), new ViseursDto());
			// enfin on supprime l'approbateur
			// First, we remove all the agents this approbateur was approving
			// this will also delete all the agents its operateurs were filling
			// in for
			for (DroitDroitsAgent agentSaisiToDelete : d.getDroitDroitsAgent()) {
				// accessRightsRepository.clear();
				accessRightsRepository.removeEntity(agentSaisiToDelete);
			}
			for (DroitProfil dp : d.getDroitProfils()) {
				if (dp.getProfil().getLibelle().equals(ProfilEnum.APPROBATEUR.toString())) {
					deleteDroitProfil(dp);
					break;
				}
			}
		} else {
			res.getErrors().add("L'agent " + dto.getIdAgent() + " n'est pas approbateur.");
		}

		return res;
	}

	private DroitProfil getDelegataireApprobateur(Integer idAgentApprobateur, List<Droit> droitSousAgentsByApprobateur) {

		DroitProfil droitProfil = null;
		for (Droit droit : droitSousAgentsByApprobateur) {
			if (accessRightsRepository.isUserDelegataireOfApprobateur(idAgentApprobateur, droit.getIdAgent())) {
				for (DroitProfil dp : droit.getDroitProfils()) {
					if (dp.getDroitApprobateur().getIdAgent().equals(idAgentApprobateur)
					// #17859
							&& dp.getProfil().getLibelle().equals(ProfilEnum.DELEGATAIRE.toString())) {
						droitProfil = dp;
					}
				}
			}
		}
		return droitProfil;
	}

	private ArrayList<Droit> getViseursApprobateur(Integer idAgentApprobateur, List<Droit> droitSousAgentsByApprobateur) {
		ArrayList<Droit> result = new ArrayList<Droit>();
		for (Droit droit : droitSousAgentsByApprobateur) {
			if (accessRightsRepository.isUserViseurOfApprobateur(idAgentApprobateur, droit.getIdAgent())) {
				if (!result.contains(droit))
					result.add(droit);
			}
		}

		return result;
	}

	private ArrayList<Droit> getOperateursOfApprobateur(Integer idAgentApprobateur, List<Droit> droitSousAgentsByApprobateur) {
		ArrayList<Droit> result = new ArrayList<Droit>();
		for (Droit droit : droitSousAgentsByApprobateur) {
			if (accessRightsRepository.isUserOperateurOfApprobateur(idAgentApprobateur, droit.getIdAgent())) {
				if (!result.contains(droit))
					result.add(droit);
			}
		}

		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public boolean canUserAccessAccessRights(Integer idAgent) {
		return accessRightsRepository.isUserApprobateur(idAgent);
	}

	@Override
	@Transactional(readOnly = true)
	public InputterDto getDelegator(int idAgent) {
		InputterDto result = new InputterDto();

		List<Droit> droit = accessRightsRepository.getDroitSousApprobateur(idAgent);

		if (droit == null) {
			logger.warn("L'agent {} n'est pas approbateur.", idAgent);
			return result;
		}
		for (Droit d : droit) {
			if (accessRightsRepository.isUserDelegataireOfApprobateur(idAgent, d.getIdAgent())) {
				AgentGeneriqueDto delegataire = sirhWSConsumer.getAgent(d.getIdAgent());

				if (delegataire == null)
					logger.warn("L'agent délégataire {} n'existe pas.", d.getIdAgent());
				else
					result.setDelegataire(new AgentDto(delegataire));

			}
		}

		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public InputterDto getInputter(int idAgent) {

		InputterDto result = new InputterDto();

		List<Droit> droit = accessRightsRepository.getDroitSousApprobateur(idAgent);

		if (droit == null) {
			logger.warn("L'agent {} n'est pas approbateur.", idAgent);
			return result;
		}
		for (Droit d : droit) {
			if (accessRightsRepository.isUserDelegataireOfApprobateur(idAgent, d.getIdAgent())) {
				AgentGeneriqueDto delegataire = sirhWSConsumer.getAgent(d.getIdAgent());

				if (delegataire == null)
					logger.warn("L'agent délégataire {} n'existe pas.", d.getIdAgent());
				else
					result.setDelegataire(new AgentDto(delegataire));

			}
			if (accessRightsRepository.isUserOperateurOfApprobateur(idAgent, d.getIdAgent())) {
				AgentGeneriqueDto ope = sirhWSConsumer.getAgent(d.getIdAgent());
				if (ope == null)
					logger.warn("L'agent opérateur {} n'existe pas.", d.getIdAgent());
				else {
					if (!result.getOperateurs().contains(new AgentDto(ope)))
						result.getOperateurs().add(new AgentDto(ope));
				}
			}

		}

		return result;
	}

	@Override
	@Transactional(value = "absTransactionManager")
	public ReturnMessageDto setInputter(Integer idAgentAppro, InputterDto dto) {

		ReturnMessageDto result = new ReturnMessageDto();
		Droit droitApprobateur = accessRightsRepository.getAgentAccessRights(idAgentAppro);

		// on recupere la liste des
		List<Droit> droitSousAgentsByApprobateur = accessRightsRepository.getDroitSousApprobateur(idAgentAppro);

		// on trie la liste des sous agents
		ArrayList<Droit> originalOperateurs = getOperateursOfApprobateur(idAgentAppro, droitSousAgentsByApprobateur);
		DroitProfil delegataire = getDelegataireApprobateur(idAgentAppro, droitSousAgentsByApprobateur);

		// /////////////////// DELEGATAIRE /////////////////////////////////////
		// on traite le delegataire
		traiteDelegataire(dto, delegataire, droitApprobateur, result);
		// //////////////////// FIN DELEGATAIRE ///////////////////////////////

		// ////////////////////// OPERATEURS //////////////////////////////////
		// on traite les operateurs
		traiteOperateurs(dto, originalOperateurs, idAgentAppro, droitApprobateur, result);
		// //////////////////// FIN OPERATEURS /////////////////////////

		return result;
	}

	// #15713 special pour SIRH : one shot
	@Override
	@Transactional(value = "absTransactionManager")
	public ReturnMessageDto setOperateur(Integer idAgentAppro, AgentDto operateurDto) {

		ReturnMessageDto result = new ReturnMessageDto();
		Droit droitApprobateur = accessRightsRepository.getAgentAccessRights(idAgentAppro);

		// on verifie que l agent n est pas deja operateur de l approbateur
		if (accessRightsRepository.isUserOperateurOfApprobateur(idAgentAppro, operateurDto.getIdAgent())) {
			logger.debug("L'agent {} est déjà opérateur de l'approbateur {}.", operateurDto.getIdAgent(), idAgentAppro);
			result.getErrors().add(String.format("L'agent [%d] est déjà opérateur de l'approbateur [%d].", operateurDto.getIdAgent(), idAgentAppro));
			return result;
		}

		creeOperateur(droitApprobateur, operateurDto, result);

		return result;
	}

	// #15713 special pour SIRH : one shot
	@Override
	@Transactional(value = "absTransactionManager")
	public ReturnMessageDto deleteOperateur(Integer idAgentAppro, AgentDto operateurDto) {

		ReturnMessageDto result = new ReturnMessageDto();

		// on verifie que l agent est bien operateur de l approbateur
		DroitProfil droitProfilOperateur = accessRightsRepository.getUserOperateurOfApprobateur(idAgentAppro, operateurDto.getIdAgent());
		if (null == droitProfilOperateur) {
			logger.debug("L'agent {} n'est pas opérateur de l'approbateur {}.", operateurDto.getIdAgent(), idAgentAppro);
			result.getErrors().add(String.format("L'agent [%d] n'est pas opérateur de l'approbateur [%d].", operateurDto.getIdAgent(), idAgentAppro));
			return result;
		}

		deleteDroitProfil(droitProfilOperateur);

		return result;
	}

	// #15713 special pour SIRH : one shot
	@Override
	@Transactional(value = "absTransactionManager")
	public ReturnMessageDto setViseur(Integer idAgentAppro, AgentDto viseurDto) {

		ReturnMessageDto result = new ReturnMessageDto();
		Droit droitApprobateur = accessRightsRepository.getAgentAccessRights(idAgentAppro);

		// on verifie que l agent n est pas deja operateur de l approbateur
		if (accessRightsRepository.isUserViseurOfApprobateur(idAgentAppro, viseurDto.getIdAgent())) {
			logger.debug("L'agent {} est déjà viseur de l'approbateur {}.", viseurDto.getIdAgent(), idAgentAppro);
			result.getErrors().add(String.format("L'agent [%d] est déjà viseur de l'approbateur [%d].", viseurDto.getIdAgent(), idAgentAppro));
			return result;
		}

		creeViseur(droitApprobateur, viseurDto, result);

		return result;
	}

	// #15713 special pour SIRH : one shot
	@Override
	@Transactional(value = "absTransactionManager")
	public ReturnMessageDto deleteViseur(Integer idAgentAppro, AgentDto viseurDto) {

		ReturnMessageDto result = new ReturnMessageDto();

		// on verifie que l agent est bien operateur de l approbateur
		DroitProfil droitProfilViseur = accessRightsRepository.getUserViseurOfApprobateur(idAgentAppro, viseurDto.getIdAgent());
		if (null == droitProfilViseur) {
			logger.debug("L'agent {} n'est pas viseur de l'approbateur {}.", viseurDto.getIdAgent(), idAgentAppro);
			result.getErrors().add(String.format("L'agent [%d] n'est pas viseur de l'approbateur [%d].", viseurDto.getIdAgent(), idAgentAppro));
			return result;
		}

		deleteDroitProfil(droitProfilViseur);

		return result;
	}

	private void traiteViseurs(ViseursDto dto, ArrayList<Droit> originalViseurs, Integer idAgentAppro, Droit droitApprobateur, ReturnMessageDto result) {

		for (AgentDto viseurDto : dto.getViseurs()) {

			Droit newViseur = null;

			// on verifie si le viseur existe deja ou non
			// #15711 bug lors de la suppression
			for (Droit existingViseur : originalViseurs) {
				if (existingViseur.getIdAgent().equals(viseurDto.getIdAgent())) {
					newViseur = existingViseur;
					originalViseurs.remove(newViseur);
					break;
				}
			}

			if (newViseur != null)
				continue;

			creeViseur(droitApprobateur, viseurDto, result);
		}

		// on supprime les viseurs en trop
		ArrayList<DroitProfil> dpASupp = new ArrayList<>();
		Droit droitAppro = accessRightsRepository.getAgentAccessRights(idAgentAppro);
		for (Droit droitViseurToDelete : originalViseurs) {
			for (DroitProfil droitProfilViseur : droitViseurToDelete.getDroitProfils()) {
				if (droitProfilViseur.getDroitApprobateur().getIdDroit().equals(droitAppro.getIdDroit())) {
					if (!dpASupp.contains(droitProfilViseur)) {
						dpASupp.add(droitProfilViseur);
					}
				}
			}
		}
		for (DroitProfil dp : dpASupp) {
			deleteDroitProfil(dp);
		}
	}

	private void creeViseur(Droit droitApprobateur, AgentDto viseurDto, ReturnMessageDto result) {

		AgentGeneriqueDto ag = sirhWSConsumer.getAgent(viseurDto.getIdAgent());
		// on verifie que l idAgent existe
		if (null == ag) {
			logger.warn("L'agent viseur {} n'existe pas.", viseurDto.getIdAgent());
			result.getErrors().add(String.format("L'agent viseur [%d] n'existe pas.", viseurDto.getIdAgent()));
			return;
		}
		// Check that the new viseur is not already approbateur or viseur
		// #14306

		// on regarde si le droit existe deja pour cette personne
		Droit newViseur = accessRightsRepository.getAgentAccessRights(viseurDto.getIdAgent());

		if (newViseur == null) {
			newViseur = new Droit();
			newViseur.setIdAgent(viseurDto.getIdAgent());
		}

		DroitProfil dp = new DroitProfil();
		newViseur.setDateModification(helperService.getCurrentDate());
		dp.setDroit(newViseur);
		dp.setProfil(accessRightsRepository.getProfilByName(ProfilEnum.VISEUR.toString()));
		dp.setDroitApprobateur(droitApprobateur);
		newViseur.getDroitProfils().add(dp);

		if (newViseur.getIdDroit() == null)
			accessRightsRepository.persisEntity(newViseur);
	}

	private void traiteOperateurs(InputterDto dto, ArrayList<Droit> originalOperateurs, Integer idAgentAppro, Droit droitApprobateur, ReturnMessageDto result) {

		for (AgentDto operateurDto : dto.getOperateurs()) {

			Droit newOperateur = null;

			// on verifie si l operateur existe deja ou non.
			// #15711 bug lors de la suppression
			for (Droit existingOperateur : originalOperateurs) {
				if (existingOperateur.getIdAgent().equals(operateurDto.getIdAgent())) {
					newOperateur = existingOperateur;
					originalOperateurs.remove(newOperateur);
					break;
				}
			}

			if (newOperateur != null)
				continue;

			creeOperateur(droitApprobateur, operateurDto, result);
		}

		// on supprime les operateurs en trop
		ArrayList<DroitProfil> dpASupp = new ArrayList<>();
		Droit droitAppro = accessRightsRepository.getAgentAccessRights(idAgentAppro);
		for (Droit droitOperateurToDelete : originalOperateurs) {
			for (DroitProfil droitProfilOperateur : droitOperateurToDelete.getDroitProfils()) {
				if (droitProfilOperateur.getDroitApprobateur().getIdDroit().equals(droitAppro.getIdDroit()) && droitProfilOperateur.getProfil().getLibelle().equals(ProfilEnum.OPERATEUR.toString())) {
					if (!dpASupp.contains(droitProfilOperateur)) {
						dpASupp.add(droitProfilOperateur);
					}
				}
			}
		}
		for (DroitProfil dp : dpASupp) {
			deleteDroitProfil(dp);
		}
	}

	private void creeOperateur(Droit droitApprobateur, AgentDto operateurDto, ReturnMessageDto result) {

		AgentGeneriqueDto ag = sirhWSConsumer.getAgent(operateurDto.getIdAgent());
		// on verifie que l idAgent existe
		if (null == ag) {
			logger.warn("L'agent opérateur {} n'existe pas.", operateurDto.getIdAgent());
			result.getErrors().add(String.format("L'agent opérateur [%d] n'existe pas.", operateurDto.getIdAgent()));
			return;
		}
		// Check that the new operateur is not already delegataire or
		// approbateur or viseur
		// #14306

		// on regarde si le droit existe deja pour cette personne
		Droit newOperateur = accessRightsRepository.getAgentAccessRights(operateurDto.getIdAgent());

		if (newOperateur == null) {
			newOperateur = new Droit();
			newOperateur.setIdAgent(operateurDto.getIdAgent());
		}

		DroitProfil dp = new DroitProfil();
		newOperateur.setDateModification(helperService.getCurrentDate());
		dp.setDroit(newOperateur);
		dp.setProfil(accessRightsRepository.getProfilByName(ProfilEnum.OPERATEUR.toString()));
		dp.setDroitApprobateur(droitApprobateur);
		newOperateur.getDroitProfils().add(dp);

		if (newOperateur.getIdDroit() == null)
			accessRightsRepository.persisEntity(newOperateur);
	}

	private void traiteDelegataire(InputterDto dto, DroitProfil delegataire, Droit droitApprobateur, ReturnMessageDto result) {

		// Si le nouveau délégataire est null
		if (dto.getDelegataire() == null) {

			// Si un délégataire existant, on le supprime
			if (delegataire != null)
				deleteDroitProfil(delegataire);
			return;
		}

		// Si le délégataire n'a pas changé, on ne fait rien
		if (delegataire != null && delegataire.getDroit().getIdAgent().equals(dto.getDelegataire().getIdAgent())) {
			return;
		}

		AgentGeneriqueDto ag = sirhWSConsumer.getAgent(dto.getDelegataire().getIdAgent());
		// on verifie que l idAgent existe
		if (null == ag) {
			logger.warn("L'agent délégataire {} n'existe pas.", dto.getDelegataire().getIdAgent());
			result.getErrors().add(String.format("L'agent délégataire [%d] n'existe pas.", dto.getDelegataire().getIdAgent()));
			return;

		}

		// Check that the new delegataire is not an operator
		// #14306
		// if
		// (accessRightsRepository.isUserOperateur(dto.getDelegataire().getIdAgent()))
		// {
		// logger.warn("L'agent %s %s [%d] ne peut pas être délégataire car il ou elle est déjà opérateur.",
		// ag.getDisplayNom(), ag.getDisplayPrenom(), ag.getIdAgent());
		// result.getErrors().add(
		// String.format("L'agent %s %s [%d] ne peut pas être délégataire car il ou elle est déjà opérateur.",
		// ag.getDisplayNom(), ag.getDisplayPrenom(), ag.getIdAgent()));
		// return;
		//
		// }

		// on supprime d abord le delegataire precedent
		if (delegataire != null) {
			deleteDroitProfil(delegataire);
		}

		// on regarde si le droit existe deja pour cette personne
		Droit d = accessRightsRepository.getAgentAccessRights(dto.getDelegataire().getIdAgent());
		if (d == null) {
			d = new Droit();
			d.setIdAgent(dto.getDelegataire().getIdAgent());
		}

		DroitProfil dp = new DroitProfil();
		d.setDateModification(helperService.getCurrentDate());
		dp.setDroit(d);
		dp.setProfil(accessRightsRepository.getProfilByName(ProfilEnum.DELEGATAIRE.toString()));
		dp.setDroitApprobateur(droitApprobateur);
		d.getDroitProfils().add(dp);

		if (d.getIdDroit() == null)
			accessRightsRepository.persisEntity(d);

	}

	/**
	 * Liste des agents que l approbateur approuve
	 */
	@Override
	@Transactional(readOnly = true)
	public List<AgentDto> getAgentsToApproveOrInputByAgent(Integer idAgentApprobateur, Integer idAgent, Integer idServiceADS) {
		return getAgentsToApproveOrInput(idAgentApprobateur, idAgent, ProfilEnum.APPROBATEUR, idServiceADS);
	}

	/**
	 * #15688 bug cumul de rôles sous un même approbateur Liste des agents que l
	 * operateur saisit
	 */
	@Override
	@Transactional(readOnly = true)
	public List<AgentDto> getAgentsToInputByOperateur(Integer idAgentApprobateur, Integer idAgentOperateur, Integer idServiceADS) {
		return getAgentsToApproveOrInput(idAgentApprobateur, idAgentOperateur, ProfilEnum.OPERATEUR, idServiceADS);
	}

	/**
	 * #15688 bug cumul de rôles sous un même approbateur Liste des agents que
	 * le viseur saisit
	 */
	@Override
	@Transactional(readOnly = true)
	public List<AgentDto> getAgentsToInputByViseur(Integer idAgentApprobateur, Integer idAgentViseur, Integer idServiceADS) {
		return getAgentsToApproveOrInput(idAgentApprobateur, idAgentViseur, ProfilEnum.VISEUR, idServiceADS);
	}

	/**
	 * Retrieves the agent an approbator is set to Approve or an Operator is set
	 * to Input. This service also filters by service
	 */
	protected List<AgentDto> getAgentsToApproveOrInput(Integer idAgentApprobateur, Integer idAgent, ProfilEnum profil, Integer idServiceADS) {

		List<AgentDto> result = new ArrayList<AgentDto>();
		List<DroitProfil> listDp = accessRightsRepository.getDroitProfilByAgent(idAgentApprobateur, idAgent);

		if (listDp == null || listDp.isEmpty()) {
			logger.warn("L'agent {} ne possède pas de DroitProfil associé à l'approbateur {}.", idAgent, idAgentApprobateur);
			return result;
		}

		DroitProfil dp = null;
		for (DroitProfil droitProfil : listDp) {
			if (profil.toString().equals(droitProfil.getProfil().getLibelle())) {
				dp = droitProfil;
				break;
			}
		}
		if (null == dp) {
			logger.warn("L'agent {} ne possède pas de rôle {} associé à l'approbateur {}.", idAgent, profil.toString(), idAgentApprobateur);
			return result;
		}

		if (idServiceADS == null) {
			for (DroitsAgent da : accessRightsRepository.getListOfAgentsToInputOrApprove(idAgent, dp.getIdDroitProfil())) {
				AgentDto agDto = new AgentDto();
				AgentGeneriqueDto ag = sirhWSConsumer.getAgent(da.getIdAgent());
				if (null == ag) {
					logger.warn("L'agent {} n'existe pas.", da.getIdAgent());
					continue;
				}
				agDto.setIdAgent(da.getIdAgent());
				agDto.setNom(ag.getDisplayNom());
				agDto.setPrenom(ag.getDisplayPrenom());
				result.add(agDto);
			}

		} else {
			// #18722 : pour chaque agent on va recuperer son
			// service
			List<Integer> listAgentDto = new ArrayList<Integer>();
			for (DroitsAgent da : accessRightsRepository.getListOfAgentsToInputOrApprove(idAgent, dp.getIdDroitProfil())) {
				if (!listAgentDto.contains(da.getIdAgent()))
					listAgentDto.add(da.getIdAgent());
			}
			List<AgentWithServiceDto> listAgentsServiceDto = sirhWSConsumer.getListAgentsWithService(listAgentDto, new Date());

			for (DroitsAgent da : accessRightsRepository.getListOfAgentsToInputOrApprove(idAgent, dp.getIdDroitProfil())) {
				AgentWithServiceDto agDtoServ = getAgentOfListAgentWithServiceDto(listAgentsServiceDto, da.getIdAgent());
				if (agDtoServ != null && agDtoServ.getIdServiceADS() != null && agDtoServ.getIdServiceADS().toString().equals(idServiceADS.toString())) {
					AgentDto agDto = new AgentDto();
					AgentGeneriqueDto ag = sirhWSConsumer.getAgent(da.getIdAgent());
					if (null == ag) {
						logger.warn("L'agent {} n'existe pas.", da.getIdAgent());
						continue;
					}
					agDto.setIdAgent(da.getIdAgent());
					agDto.setNom(ag.getDisplayNom());
					agDto.setPrenom(ag.getDisplayPrenom());
					result.add(agDto);
				}
			}
		}

		return result;
	}

	private void deleteDroitProfil(DroitProfil droitProfil) {

		// on supprime les droits agent associes au droit profil
		for (DroitDroitsAgent agToDelete : droitProfil.getDroitDroitsAgent()) {
			deleteDroitDroitsAgent(agToDelete);
		}

		// on supprime le profil
		Droit droit = droitProfil.getDroit();
		droit.getDroitProfils().remove(droitProfil);
		accessRightsRepository.removeEntity(droitProfil);

		// on verifie que l agent n a pas d autre profil, si non on supprime son
		// droit
		if (droit.getDroitProfils().size() == 0) {
			accessRightsRepository.removeEntity(droit);
		}
	}

	/**
	 * #15688 bug cumul de rôles sous un même approbateur sauvegarde les agents
	 * saisis par un operateur
	 */
	@Override
	@Transactional(value = "absTransactionManager")
	public ReturnMessageDto setAgentsToInputByOperateur(Integer idAgentApprobateur, Integer idAgentOperateur, List<AgentDto> agents) {
		return setAgentsToInput(idAgentApprobateur, idAgentOperateur, agents, ProfilEnum.OPERATEUR);
	}

	/**
	 * #15688 bug cumul de rôles sous un même approbateur sauvegarde les agents
	 * saisis par un viseur
	 */
	@Override
	@Transactional(value = "absTransactionManager")
	public ReturnMessageDto setAgentsToInputByViseur(Integer idAgentApprobateur, Integer idAgentOperateur, List<AgentDto> agents) {
		return setAgentsToInput(idAgentApprobateur, idAgentOperateur, agents, ProfilEnum.VISEUR);
	}

	protected ReturnMessageDto setAgentsToInput(Integer idAgentApprobateur, Integer idAgentOperateurOrViseur, List<AgentDto> agents, ProfilEnum profil) {

		ReturnMessageDto result = new ReturnMessageDto();

		List<DroitProfil> listDroitsProfilApprobateur = accessRightsRepository.getDroitProfilByAgent(idAgentApprobateur, idAgentApprobateur);
		List<DroitProfil> listDroitsProfil = accessRightsRepository.getDroitProfilByAgent(idAgentApprobateur, idAgentOperateurOrViseur);

		// #16432 bug cumul de rôles de l approbateur
		DroitProfil droitProfilApprobateur = null;
		for (DroitProfil droitProfil : listDroitsProfilApprobateur) {
			if (ProfilEnum.APPROBATEUR.toString().equals(droitProfil.getProfil().getLibelle())) {
				droitProfilApprobateur = droitProfil;
				break;
			}
		}

		// #15688 bug cumul de rôles sous un même approbateur
		DroitProfil droitProfilOperateurOrViseur = null;
		for (DroitProfil droitProfil : listDroitsProfil) {
			if (profil.toString().equals(droitProfil.getProfil().getLibelle())) {
				droitProfilOperateurOrViseur = droitProfil;
				break;
			}
		}
		if (null == droitProfilOperateurOrViseur) {
			logger.warn("Impossible de modifier la liste des agents saisis de l'opérateur ou du viseur {} car il n'est pas un opérateur ou viseur de l'agent {}.", idAgentOperateurOrViseur,
					idAgentApprobateur);
			result.getErrors().add(
					String.format("Impossible de modifier la liste des agents saisis de l'opérateur ou du viseur [%d] car il n'est pas un opérateur ou viseur de l'agent [%d].",
							idAgentOperateurOrViseur, idAgentApprobateur));
			return result;
		}

		List<DroitDroitsAgent> agentsToUnlink = new ArrayList<DroitDroitsAgent>(droitProfilOperateurOrViseur.getDroitDroitsAgent());

		for (AgentDto ag : agents) {

			// on verifie que l agent n est pas deja saisi
			boolean isAgentDejaSaisi = false;
			for (DroitDroitsAgent ddaOperateurViseur : agentsToUnlink) {
				if (ddaOperateurViseur.getDroitsAgent().getIdAgent().equals(ag.getIdAgent())) {
					isAgentDejaSaisi = true;
					agentsToUnlink.remove(ddaOperateurViseur);
					break;
				}
			}
			if (isAgentDejaSaisi) {
				continue;
			}

			for (DroitDroitsAgent ddaInAppro : droitProfilApprobateur.getDroitDroitsAgent()) {

				// if this is not the agent we're currently looking for,
				// continue
				if (!ddaInAppro.getDroitsAgent().getIdAgent().equals(ag.getIdAgent()))
					continue;

				// once found, if this agent is not in the operator list, add it
				if (!droitProfilOperateurOrViseur.getDroitDroitsAgent().contains(ddaInAppro)) {
					DroitDroitsAgent dda = new DroitDroitsAgent();
					dda.setDroit(droitProfilOperateurOrViseur.getDroit());
					dda.setDroitsAgent(ddaInAppro.getDroitsAgent());
					dda.setDroitProfil(droitProfilOperateurOrViseur);

					droitProfilOperateurOrViseur.getDroitDroitsAgent().add(dda);

					if (dda.getIdDroitDroitsAgent() == null)
						accessRightsRepository.persisEntity(dda);

					continue;
				}

				// remove this agent from the list of agents to be unlinked
				agentsToUnlink.remove(ddaInAppro);

				// we're done with the list for now
				break;
			}
		}

		for (DroitDroitsAgent agToUnlink : agentsToUnlink) {
			deleteDroitDroitsAgent(agToUnlink);
		}
		return result;
	}

	@Override
	@Transactional(value = "absTransactionManager")
	public ReturnMessageDto setAgentsToApprove(Integer idAgentApprobateur, List<AgentDto> agents) {

		ReturnMessageDto result = new ReturnMessageDto();

		DroitProfil droitProfilApprobateur = accessRightsRepository.getDroitProfilApprobateur(idAgentApprobateur);

		Set<DroitDroitsAgent> agentsToDelete = droitProfilApprobateur.getDroitDroitsAgent();

		for (AgentDto ag : agents) {

			DroitDroitsAgent existingAgent = null;

			for (DroitDroitsAgent da : droitProfilApprobateur.getDroitDroitsAgent()) {
				if (da.getDroitsAgent().getIdAgent().equals(ag.getIdAgent())) {
					existingAgent = da;
					agentsToDelete.remove(existingAgent);
					break;
				}
			}

			if (existingAgent != null)
				continue;

			AgentGeneriqueDto dto = sirhWSConsumer.getAgent(ag.getIdAgent());
			if (dto == null) {
				logger.warn("L'agent {} n'existe pas.", ag.getIdAgent());
				result.getErrors().add(String.format("L'agent [%d] n'existe pas.", ag.getIdAgent()));
				continue;
			}

			// on regarde si le droit existe deja pour cette personne
			DroitsAgent newDroitAgent = accessRightsRepository.getDroitsAgent(dto.getIdAgent());

			if (newDroitAgent == null) {
				newDroitAgent = new DroitsAgent();
				newDroitAgent.setIdAgent(dto.getIdAgent());
			}

			newDroitAgent.setDateModification(helperService.getCurrentDate());

			existingAgent = new DroitDroitsAgent();
			existingAgent.setDroit(droitProfilApprobateur.getDroit());
			existingAgent.setDroitProfil(droitProfilApprobateur);
			existingAgent.setDroitsAgent(newDroitAgent);

			newDroitAgent.getDroitDroitsAgent().add(existingAgent);

			accessRightsRepository.persisEntity(newDroitAgent);
		}

		// #18738
		// on supprime egalement dans la table DroitDroitsAgent
		// pour les operateurs et viseurs de l approbateur

		// on recupere la liste des
		List<Droit> droitSousAgentsByApprobateur = accessRightsRepository.getDroitSousApprobateur(idAgentApprobateur);

		// ///////// on supprime les agents affectes aux operateurs de l
		// approbateur
		List<Droit> droitsOperateurs = getOperateursOfApprobateur(idAgentApprobateur, droitSousAgentsByApprobateur);
		if (null != droitsOperateurs) {
			for (Droit operateur : droitsOperateurs) {
				for (DroitProfil droitProfilOperateur : operateur.getDroitProfils()) {
					if (droitProfilOperateur.getDroitApprobateur().getIdDroit().equals(droitProfilApprobateur.getDroit().getIdDroit())
							&& droitProfilOperateur.getProfil().getLibelle().equals(ProfilEnum.OPERATEUR.toString())) {

						for (DroitDroitsAgent droitDroitsAgentOperateurToDelete : droitProfilOperateur.getDroitDroitsAgent()) {
							for (DroitDroitsAgent agToDelete : agentsToDelete) {
								if (droitDroitsAgentOperateurToDelete.getDroitsAgent().getIdAgent().equals(agToDelete.getDroitsAgent().getIdAgent())) {
									deleteDroitDroitsAgent(droitDroitsAgentOperateurToDelete);
								}
							}
						}
					}
				}
			}
		}

		// ///////// on supprime les agents affectes aux viseurs de l
		// approbateur
		List<Droit> droitsViseurs = getViseursApprobateur(idAgentApprobateur, droitSousAgentsByApprobateur);
		if (null != droitsViseurs) {
			for (Droit viseur : droitsViseurs) {
				for (DroitProfil droitProfilViseur : viseur.getDroitProfils()) {
					if (droitProfilViseur.getDroitApprobateur().getIdDroit().equals(droitProfilApprobateur.getDroit().getIdDroit())
							&& droitProfilViseur.getProfil().getLibelle().equals(ProfilEnum.VISEUR.toString())) {

						for (DroitDroitsAgent droitDroitsAgentViseurToDelete : droitProfilViseur.getDroitDroitsAgent()) {
							for (DroitDroitsAgent agToDelete : agentsToDelete) {
								if (droitDroitsAgentViseurToDelete.getDroitsAgent().getIdAgent().equals(agToDelete.getDroitsAgent().getIdAgent())) {
									deleteDroitDroitsAgent(droitDroitsAgentViseurToDelete);
								}
							}
						}
					}
				}
			}
		}

		for (DroitDroitsAgent agToDelete : agentsToDelete) {
			deleteDroitDroitsAgent(agToDelete);

		}

		return result;
	}

	private void deleteDroitDroitsAgent(DroitDroitsAgent agToDelete) {

		DroitsAgent droitAgent = agToDelete.getDroitsAgent();
		droitAgent.getDroitDroitsAgent().remove(agToDelete);
		accessRightsRepository.removeEntity(agToDelete);

		// on verifie que l agent n a pas d autre droits, si non on supprime
		// son droit
		if (droitAgent.getDroitDroitsAgent().size() == 0) {
			accessRightsRepository.removeEntity(droitAgent);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public ViseursDto getViseurs(int idAgent) {

		ViseursDto result = new ViseursDto();

		List<Droit> droit = accessRightsRepository.getDroitSousApprobateur(idAgent);

		if (droit == null) {
			logger.warn("L'agent {} n'est pas approbateur.", idAgent);
			return result;
		}
		for (Droit d : droit) {
			if (accessRightsRepository.isUserViseurOfApprobateur(idAgent, d.getIdAgent())) {
				AgentGeneriqueDto viseur = sirhWSConsumer.getAgent(d.getIdAgent());
				if (viseur == null)
					logger.warn("L'agent viseur {} n'existe pas.", d.getIdAgent());
				else {
					if (!result.getViseurs().contains(new AgentDto(viseur)))
						result.getViseurs().add(new AgentDto(viseur));
				}
			}

		}

		return result;
	}

	@Override
	@Transactional(value = "absTransactionManager")
	public ReturnMessageDto setViseurs(Integer idAgentAppro, ViseursDto dto) {

		ReturnMessageDto result = new ReturnMessageDto();
		Droit droitApprobateur = accessRightsRepository.getAgentAccessRights(idAgentAppro);

		// on recupere la liste des
		List<Droit> droitSousAgentsByApprobateur = accessRightsRepository.getDroitSousApprobateur(idAgentAppro);

		// on trie la liste des sous agents
		ArrayList<Droit> originalViseurs = getViseursApprobateur(idAgentAppro, droitSousAgentsByApprobateur);

		// ////////////////////// VISEURS //////////////////////////////////
		// on traite les viseurs
		traiteViseurs(dto, originalViseurs, idAgentAppro, droitApprobateur, result);
		// //////////////////// FIN VISEURS //////////////////////////////////

		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public AgentWithServiceDto getApprobateurOfAgent(Integer idAgent) {
		DroitsAgent droitAgent = accessRightsRepository.getDroitsAgent(idAgent);

		Droit droitApprobateur = accessRightsRepository.getApprobateurOfAgent(droitAgent);
		AgentWithServiceDto agentApprobateurDto = sirhWSConsumer.getAgentService(droitApprobateur.getIdAgent(), helperService.getCurrentDate());
		return agentApprobateurDto;
	}

	@Override
	@Transactional(readOnly = true)
	public boolean verifAccessRightListDemande(Integer idAgentConnecte, Integer idAgentOfDemande, ReturnMessageDto returnDto) {

		boolean res = true;

		// l agent connecte a-t-il les droits de visualiser la liste de demande
		// de l agent
		if (!idAgentConnecte.equals(idAgentOfDemande)) {

			if (!accessRightsRepository.isViseurOfAgent(idAgentConnecte, idAgentOfDemande) && !accessRightsRepository.isApprobateurOrDelegataireOfAgent(idAgentConnecte, idAgentOfDemande)
					&& !accessRightsRepository.isOperateurOfAgent(idAgentConnecte, idAgentOfDemande)) {

				res = false;
				logger.warn("Vous n'êtes pas habilité à consulter la liste des demandes de cet agent.");
				returnDto.getErrors().add(String.format("Vous n'êtes pas habilité à consulter la liste des demandes de cet agent."));
			}
		}

		return res;
	}

	/**
	 * Returns the list of distinct services approved/input agents have Used to
	 * build the filters (by service)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<EntiteDto> getAgentsServicesToApproveOrInput(Integer idAgent, Date date) {

		List<EntiteDto> result = new ArrayList<EntiteDto>();

		List<Integer> codeServices = new ArrayList<Integer>();

		// #18709 optimiser les appels ADS
		EntiteDto root = adsWsConsumer.getWholeTree();

		// #18722 : pour chaque agent on va recuperer son
		// service
		List<Integer> listAgentDtoAppro = new ArrayList<Integer>();
		for (DroitsAgent da : accessRightsRepository.getListOfAgentsToInputOrApprove(idAgent)) {
			if (!listAgentDtoAppro.contains(da.getIdAgent()))
				listAgentDtoAppro.add(da.getIdAgent());
		}
		List<AgentWithServiceDto> listAgentsApproServiceDto = sirhWSConsumer.getListAgentsWithService(listAgentDtoAppro, date);

		// #19250
		// pour chaque agent présent dans les droits, si il n'a pas de service
		// alors on cherche sa derniere affectation
		List<Integer> listAgentSansAffectation = new ArrayList<Integer>();
		for (Integer idAgentListApprobation : listAgentDtoAppro) {
			AgentWithServiceDto temp = new AgentWithServiceDto();
			temp.setIdAgent(idAgentListApprobation);
			if (!listAgentsApproServiceDto.contains(temp)) {
				listAgentSansAffectation.add(idAgentListApprobation);
			}
		}
		List<AgentWithServiceDto> listAgentsoldAff = new ArrayList<AgentWithServiceDto>();
		if (listAgentSansAffectation.size() > 0) {
			List<AgentWithServiceDto> listAgentsSansAffectation = sirhWSConsumer.getListAgentsWithServiceOldAffectation(listAgentSansAffectation);
			for (AgentWithServiceDto t : listAgentsSansAffectation) {
				if (!listAgentsApproServiceDto.contains(t)) {
					listAgentsoldAff.add(t);
				}
			}
		}
		//on ajoute tous les agents manquants à la liste
		List<AgentWithServiceDto> listeFinale = new ArrayList<AgentWithServiceDto>();
		listeFinale.addAll(listAgentsApproServiceDto);
		listeFinale.addAll(listAgentsoldAff);

		for (DroitsAgent da : accessRightsRepository.getListOfAgentsToInputOrApprove(idAgent)) {
			AgentWithServiceDto agDto = getAgentOfListAgentWithServiceDto(listeFinale, da.getIdAgent());
			if (agDto != null && agDto.getIdServiceADS() != null) {
				if (codeServices.contains(agDto.getIdServiceADS()))
					continue;

				EntiteDto svDto = adsWsConsumer.getEntiteByIdEntiteOptimiseWithWholeTree(agDto.getIdServiceADS(), root);
				if (svDto != null) {
					codeServices.add(agDto.getIdServiceADS());
					if (!svDto.getIdStatut().toString().equals(String.valueOf(StatutEntiteEnum.ACTIF.getIdRefStatutEntite()))) {
						svDto.setLabel(svDto.getLabel() + "(" + StatutEntiteEnum.getStatutEntiteEnum(svDto.getIdStatut()).getLibStatutEntite() + ")");
					} else {
						svDto.setLabel(svDto.getLabel());
					}
					result.add(svDto);
				}
			}
		}

		// redmine #14201 : on cherche si l'agent est délégataire
		List<Integer> idsApprobateurOfDelegataire = getIdApprobateurOfDelegataire(idAgent, null);
		if (idsApprobateurOfDelegataire != null) {
			for (Integer idApprobateurOfDelegataire : idsApprobateurOfDelegataire) {
				if (idApprobateurOfDelegataire != null) {
					// #18722 : pour chaque agent on va recuperer son
					// service
					List<Integer> listAgentDtoDeleg = new ArrayList<Integer>();
					for (DroitsAgent da : accessRightsRepository.getListOfAgentsToInputOrApprove(idApprobateurOfDelegataire)) {
						if (!listAgentDtoDeleg.contains(da.getIdAgent()))
							listAgentDtoDeleg.add(da.getIdAgent());
					}
					List<AgentWithServiceDto> listAgentsDelegServiceDto = sirhWSConsumer.getListAgentsWithService(listAgentDtoDeleg, date);

					for (DroitsAgent da : accessRightsRepository.getListOfAgentsToInputOrApprove(idApprobateurOfDelegataire)) {
						AgentWithServiceDto agDto = getAgentOfListAgentWithServiceDto(listAgentsDelegServiceDto, da.getIdAgent());
						if (agDto != null && agDto.getIdServiceADS() != null) {

							if (codeServices.contains(agDto.getIdServiceADS()))
								continue;

							EntiteDto svDto = adsWsConsumer.getEntiteByIdEntiteOptimiseWithWholeTree(agDto.getIdServiceADS(), root);
							if (svDto != null) {
								codeServices.add(agDto.getIdServiceADS());
								if (!svDto.getIdStatut().toString().equals(String.valueOf(StatutEntiteEnum.ACTIF.getIdRefStatutEntite()))) {
									svDto.setLabel(svDto.getLabel() + "(" + StatutEntiteEnum.getStatutEntiteEnum(svDto.getIdStatut()).getLibStatutEntite() + ")");
								} else {
									svDto.setLabel(svDto.getLabel());
								}
								result.add(svDto);
							}
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * Retrieves the agent an approbator is set to Approve or an Operator is set
	 * to Input. This service also filters by service
	 */
	@Override
	@Transactional(readOnly = true)
	public List<AgentDto> getAgentsToApproveOrInputByService(Integer idAgent, Integer idServiceADS) {

		List<AgentDto> result = new ArrayList<AgentDto>();

		List<DroitsAgent> listDroitsAgent = accessRightsRepository.getListOfAgentsToInputOrApprove(idAgent);

		List<DroitsAgent> listDroitsAgentDelegataire = new ArrayList<DroitsAgent>();
		List<Integer> idsApprobateurOfDelegataire = getIdApprobateurOfDelegataire(idAgent, null);
		if (idsApprobateurOfDelegataire != null) {
			for (Integer idApprobateurOfDelegataire : idsApprobateurOfDelegataire) {
				listDroitsAgentDelegataire.addAll(accessRightsRepository.getListOfAgentsToInputOrApprove(idApprobateurOfDelegataire));
			}
		}

		// seuls les operateurs de la DPM peuvent saisir les jours de repos
		// seuls les operateurs peuvent mettre a jour les compteurs (solde)
		// on recup les agents de l'operateur
		List<Integer> listAgentDtoTemp = new ArrayList<Integer>();
		for (DroitsAgent droitAg : listDroitsAgent) {
			listAgentDtoTemp.add(droitAg.getIdAgent());
		}

		if (null != listDroitsAgentDelegataire && !listDroitsAgentDelegataire.isEmpty()) {
			for (DroitsAgent droitAg : listDroitsAgentDelegataire) {
				listAgentDtoTemp.add(droitAg.getIdAgent());
			}
		}

		List<Integer> listAgentDtoFinal = new ArrayList<Integer>();
		if (idServiceADS == null) {
			listAgentDtoFinal.addAll(listAgentDtoTemp);
		} else {

			// #18722 : pour chaque agent on va recuperer son
			// service
			List<Integer> listAgentDtoServ = new ArrayList<Integer>();
			for (Integer idAgentTemp : listAgentDtoTemp) {
				if (!listAgentDtoServ.contains(idAgentTemp))
					listAgentDtoServ.add(idAgentTemp);
			}
			List<AgentWithServiceDto> listAgentsDelegServiceDto = sirhWSConsumer.getListAgentsWithService(listAgentDtoServ, new Date());
			// #19250
			// pour chaque agent présent dans les droits, si il n'a pas de
			// service alors on cherche sa derniere affectation
			List<Integer> listAgentSansAffectation = new ArrayList<Integer>();
			for (Integer idAgentListApprobation : listAgentDtoServ) {
				AgentWithServiceDto temp = new AgentWithServiceDto();
				temp.setIdAgent(idAgentListApprobation);
				if (!listAgentsDelegServiceDto.contains(temp)) {
					listAgentSansAffectation.add(idAgentListApprobation);
				}
			}
			if (listAgentSansAffectation.size() > 0) {
				List<AgentWithServiceDto> listAgentsSansAffectation = sirhWSConsumer.getListAgentsWithServiceOldAffectation(listAgentSansAffectation);
				for (AgentWithServiceDto t : listAgentsSansAffectation) {
					if (!listAgentsDelegServiceDto.contains(t)) {
						listAgentsDelegServiceDto.add(t);
					}
				}
			}

			for (Integer idAgentTemp : listAgentDtoTemp) {
				AgentWithServiceDto agDto = getAgentOfListAgentWithServiceDto(listAgentsDelegServiceDto, idAgentTemp);
				if (agDto != null && agDto.getIdServiceADS() != null && agDto.getIdServiceADS().toString().equals(idServiceADS.toString())) {
					listAgentDtoFinal.add(idAgentTemp);
				}
			}
		}

		List<AgentGeneriqueDto> listAgentsServiceDto = sirhWSConsumer.getListAgents(listAgentDtoFinal);

		for (DroitsAgent da : listDroitsAgent) {
			if (isContainAgentInList(result, da)) {
				AgentDto agDto = new AgentDto();
				AgentGeneriqueDto ag = getAgentOfListAgentGeneriqueDto(listAgentsServiceDto, da.getIdAgent());
				if (ag != null) {
					agDto.setIdAgent(da.getIdAgent());
					agDto.setNom(ag.getDisplayNom());
					agDto.setPrenom(ag.getDisplayPrenom());
					result.add(agDto);
				}
			}
		}

		// redmine #14201 : on cherche si l'agent est délégataire(s)
		if (idsApprobateurOfDelegataire != null) {
			for (DroitsAgent da : listDroitsAgentDelegataire) {
				if (isContainAgentInList(result, da)) {
					AgentDto agDto = new AgentDto();
					AgentGeneriqueDto ag = getAgentOfListAgentGeneriqueDto(listAgentsServiceDto, da.getIdAgent());
					if (ag != null) {
						agDto.setIdAgent(da.getIdAgent());
						agDto.setNom(ag.getDisplayNom());
						agDto.setPrenom(ag.getDisplayPrenom());
						if (!result.contains(agDto)) {
							result.add(agDto);
						}
					}
				}
			}
		}

		return result;
	}

	private AgentGeneriqueDto getAgentOfListAgentGeneriqueDto(List<AgentGeneriqueDto> listAgents, Integer idAgent) {

		if (null != listAgents && null != idAgent) {
			for (AgentGeneriqueDto agent : listAgents) {
				if (agent.getIdAgent().equals(idAgent)) {
					return agent;
				}
			}
		}
		return null;
	}

	private boolean isContainAgentInList(List<AgentDto> listAgents, DroitsAgent ag) {

		if (null == ag) {
			return false;
		}

		if (null != listAgents) {
			for (AgentDto agent : listAgents) {
				if (null != agent && null != agent.getIdAgent() && null != ag.getIdAgent() && agent.getIdAgent().equals(ag.getIdAgent())) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public ReturnMessageDto verifAccessRightDemande(Integer idAgent, Integer idAgentOfDemande, ReturnMessageDto returnDto) {

		// si l'agent est un operateur alors on verifie qu'il a bien les droits
		// sur l'agent pour qui il effectue la demande
		if (!idAgent.equals(idAgentOfDemande)) {
			if (accessRightsRepository.isUserOperateur(idAgent) || accessRightsRepository.isUserApprobateur(idAgent) || accessRightsRepository.isUserDelegataire(idAgent)
					|| accessRightsRepository.isUserViseur(idAgent)) {

				// on recherche tous les sous agents de la personne
				Droit droitOperateurApproViseur = accessRightsRepository.getAgentDroitFetchAgents(idAgent);
				boolean trouve = false;
				for (DroitDroitsAgent dda : droitOperateurApproViseur.getDroitDroitsAgent()) {
					if (dda.getDroitsAgent().getIdAgent().equals(idAgentOfDemande)) {
						trouve = true;
						break;
					}
				}

				if (accessRightsRepository.isUserDelegataire(idAgent) && !trouve) {
					// redmine #14201 : on cherche si l'agent est délégataire
					List<Integer> idsApprobateurOfDelegataire = getIdApprobateurOfDelegataire(idAgent, null);

					for (Integer idApprobateurOfDelegataire : idsApprobateurOfDelegataire) {
						Droit droitApprobateurOfDelegataire = accessRightsRepository.getAgentDroitFetchAgents(idApprobateurOfDelegataire);
						for (DroitDroitsAgent dda : droitApprobateurOfDelegataire.getDroitDroitsAgent()) {
							if (dda.getDroitsAgent().getIdAgent().equals(idAgentOfDemande)) {
								trouve = true;
								break;
							}
						}
						if (trouve) {
							break;
						}
					}
				}

				if (!trouve) {
					logger.warn("Vous n'êtes ni opérateur, ni approbateur, ni viseur de l'agent {}. Vous ne pouvez pas saisir de demandes.", idAgentOfDemande);
					returnDto.getErrors().add(String.format("Vous n'êtes ni opérateur, ni approbateur, ni viseur de l'agent %s. Vous ne pouvez pas saisir de demandes.", idAgentOfDemande));
				}
			} else {
				logger.warn("Vous n'êtes ni opérateur, ni approbateur, ni viseur. Vous ne pouvez pas saisir de demandes.");
				returnDto.getErrors().add(String.format("Vous n'êtes ni opérateur, ni approbateur, ni viseur. Vous ne pouvez pas saisir de demandes."));
			}
		}
		return returnDto;
	}

	@Override
	public List<Integer> getIdApprobateurOfDelegataire(Integer idAgentConnecte, Integer idAgentConcerne) {

		List<Integer> idsApprobateurOfDelegataire = new ArrayList<Integer>();
		// on recupere les profils de l agent connectee
		// si idAgentConcerne renseigne, inutile de recuperer les profils pour l
		// execution de la requete ensuite
		if (null == idAgentConcerne) {
			// on verifie si l agent est delegataire ou non
			if (accessRightsRepository.isUserDelegataire(idAgentConnecte)) {
				List<DroitProfil> droitsProfils = accessRightsRepository.getDroitProfilByAgentAndLibelle(idAgentConnecte, ProfilEnum.DELEGATAIRE.toString());
				if (null != droitsProfils) {
					for (DroitProfil droitProfil : droitsProfils) {
						idsApprobateurOfDelegataire.add(droitProfil.getDroitApprobateur().getIdAgent());
					}
				}
			}
		}
		return idsApprobateurOfDelegataire;
	}

	@Override
	@Transactional(value = "absTransactionManager")
	public ReturnMessageDto setDelegataire(Integer idAgentAppro, InputterDto inputterDto, ReturnMessageDto result) {
		Droit droitApprobateur = accessRightsRepository.getAgentAccessRights(idAgentAppro);

		// on recupere la liste des
		List<Droit> droitSousAgentsByApprobateur = accessRightsRepository.getDroitSousApprobateur(idAgentAppro);

		// on trie la liste des sous agents
		DroitProfil delegataire = getDelegataireApprobateur(idAgentAppro, droitSousAgentsByApprobateur);

		// /////////////////// DELEGATAIRE /////////////////////////////////////
		// on traite le delegataire
		traiteDelegataire(inputterDto, delegataire, droitApprobateur, result);
		// //////////////////// FIN DELEGATAIRE ///////////////////////////////
		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public ActeursDto getListeActeurs(Integer idAgent) {

		ActeursDto result = new ActeursDto();

		// en QUALIF, un agent peut avoir plusieurs lignes dans la table
		// DROITS_AGENT
		// ce n est pas normal mais je pense que cela provient de la reprise de
		// donnees des droits
		List<DroitsAgent> listDroitsAgent = accessRightsRepository.getListeActeursOfAgent(idAgent);

		if (null != listDroitsAgent) {
			List<AgentGeneriqueDto> listAgentsExistants = new ArrayList<AgentGeneriqueDto>();
			for (DroitsAgent droitsAgent : listDroitsAgent) {

				if (null != droitsAgent.getDroitDroitsAgent()) {
					for (DroitDroitsAgent dda : droitsAgent.getDroitDroitsAgent()) {
						if (ProfilEnum.OPERATEUR.toString().equals(dda.getDroitProfil().getProfil().getLibelle())) {
							AgentDto operateur = new AgentDto(agentService.getAgentOptimise(listAgentsExistants, dda.getDroitProfil().getDroit().getIdAgent()));

							if (!result.getListOperateurs().contains(operateur))
								result.getListOperateurs().add(operateur);

							continue;
						}
						if (ProfilEnum.VISEUR.toString().equals(dda.getDroitProfil().getProfil().getLibelle())) {
							AgentDto viseur = new AgentDto(agentService.getAgentOptimise(listAgentsExistants, dda.getDroitProfil().getDroit().getIdAgent()));

							if (!result.getListViseurs().contains(viseur))
								result.getListViseurs().add(viseur);

							continue;
						}
						if (ProfilEnum.APPROBATEUR.toString().equals(dda.getDroitProfil().getProfil().getLibelle())) {
							AgentWithServiceDto approbateur = new AgentWithServiceDto(agentService.getAgentOptimise(listAgentsExistants, dda.getDroitProfil().getDroit().getIdAgent()));

							ApprobateurDto approbateurWithDelegataire = new ApprobateurDto();
							approbateurWithDelegataire.setApprobateur(approbateur);

							if (!result.getListApprobateurs().contains(approbateurWithDelegataire)) {
								DroitProfil profilDelegataire = getDelegataireApprobateur(approbateur.getIdAgent(), accessRightsRepository.getDroitSousApprobateur(approbateur.getIdAgent()));

								if (null != profilDelegataire) {
									AgentDto delegataire = new AgentDto(agentService.getAgentOptimise(listAgentsExistants, profilDelegataire.getDroit().getIdAgent()));
									delegataire.setIdAgent(profilDelegataire.getDroit().getIdAgent());
									approbateurWithDelegataire.setDelegataire(delegataire);
								}

								result.getListApprobateurs().add(approbateurWithDelegataire);
							}

							continue;
						}
					}
				}
			}
		}

		return result;
	}

	@Override
	public AgentGeneriqueDto findAgent(Integer idAgent) {
		return sirhWSConsumer.getAgent(idAgent);
	}

	@Override
	@Transactional(value = "absTransactionManager")
	public boolean isUserApprobateur(Integer idAgent) {
		return accessRightsRepository.isUserApprobateur(idAgent) || accessRightsRepository.isUserDelegataire(idAgent);
	}

	@Override
	@Transactional(value = "absTransactionManager")
	public boolean isUserOperateur(Integer idAgent) {
		return accessRightsRepository.isUserOperateur(idAgent);
	}

	@Override
	@Transactional(value = "absTransactionManager")
	public boolean isUserViseur(Integer idAgent) {
		return accessRightsRepository.isUserViseur(idAgent);
	}

	@Override
	public List<Integer> getListAgentByService(Integer idServiceADS, Date date) {
		// #18722
		// on recupere les agents en activité du service
		List<Integer> listIdAgent = new ArrayList<Integer>();
		for (AgentWithServiceDto ag : sirhWSConsumer.getListAgentServiceWithParent(idServiceADS, date)) {
			if (ag != null && ag.getIdServiceADS() != null && ag.getIdServiceADS().toString().equals(idServiceADS.toString()) && !listIdAgent.contains(ag.getIdAgent()))
				listIdAgent.add(ag.getIdAgent());
		}
		List<Integer> result = new ArrayList<Integer>();
		for (DroitsAgent da : accessRightsRepository.getListDroitsAgent(listIdAgent)) {
			result.add(da.getIdAgent());
		}
		return result;
	}

	@Override
	public List<EntiteDto> getAgentsServicesForOperateur(Integer idAgentOperateur, Date date) {

		List<EntiteDto> result = new ArrayList<EntiteDto>();

		List<Integer> codeServices = new ArrayList<Integer>();

		List<DroitProfil> listeDroitProfilOperateur = accessRightsRepository.getDroitProfilByAgentAndLibelle(idAgentOperateur, ProfilEnum.OPERATEUR.toString());
		for (DroitProfil dp : listeDroitProfilOperateur) {

			List<Integer> listAgentDto = new ArrayList<Integer>();
			for (DroitsAgent daTemp : accessRightsRepository.getListOfAgentsToInputOrApprove(idAgentOperateur, dp.getIdDroitProfil())) {
				if (!accessRightsRepository.isOperateurOfAgent(idAgentOperateur, daTemp.getIdAgent())) {
					continue;
				}

				listAgentDto.add(daTemp.getIdAgent());
			}

			// #18722 : pour chaque agent on va recuperer son
			// service
			List<AgentWithServiceDto> listAgentsServiceDto = sirhWSConsumer.getListAgentsWithService(listAgentDto, date);
			for (DroitsAgent da : accessRightsRepository.getListOfAgentsToInputOrApprove(idAgentOperateur, dp.getIdDroitProfil())) {

				AgentWithServiceDto agDto = getAgentOfListAgentWithServiceDto(listAgentsServiceDto, da.getIdAgent());
				if (agDto != null && agDto.getIdServiceADS() != null) {
					if (codeServices.contains(agDto.getIdServiceADS()))
						continue;

					codeServices.add(agDto.getIdServiceADS());
					EntiteDto svDto = new EntiteDto();
					svDto.setIdEntite(agDto.getIdServiceADS());
					svDto.setLabel(agDto.getService());
					result.add(svDto);
				}
			}
		}

		return result;
	}

	@Override
	public List<Droit> getListApprobateursOfOperateur(Integer idAgentOperateur) {

		List<Droit> result = new ArrayList<Droit>();

		List<DroitProfil> listeDroitProfilOperateur = accessRightsRepository.getDroitProfilByAgentAndLibelle(idAgentOperateur, ProfilEnum.OPERATEUR.toString());
		for (DroitProfil dp : listeDroitProfilOperateur) {
			if (dp.getDroitApprobateur() != null && !result.contains(dp.getDroitApprobateur())) {
				result.add(dp.getDroitApprobateur());
			}
		}
		return result;
	}
}
