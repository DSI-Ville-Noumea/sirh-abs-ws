package nc.noumea.mairie.abs.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.NoResultException;

import nc.noumea.mairie.abs.domain.Droit;
import nc.noumea.mairie.abs.domain.DroitProfil;
import nc.noumea.mairie.abs.domain.DroitsAgent;
import nc.noumea.mairie.abs.domain.Profil;
import nc.noumea.mairie.abs.domain.ProfilEnum;
import nc.noumea.mairie.abs.dto.AccessRightsDto;
import nc.noumea.mairie.abs.dto.AgentDto;
import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.abs.dto.InputterDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IAccessRightsRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.abs.service.IAccessRightsService;
import nc.noumea.mairie.sirh.comparator.AgentWithServiceDtoComparator;
import nc.noumea.mairie.sirh.domain.Agent;
import nc.noumea.mairie.ws.ISirhWSConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

	@Override
	public AccessRightsDto getAgentAccessRights(Integer idAgent) {

		AccessRightsDto result = new AccessRightsDto();
		try {
			Droit da = accessRightsRepository.getAgentAccessRights(idAgent);

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
		} catch (NoResultException e) {
			logger.debug("Aucun droit trouvé pour l'agent {}" + idAgent);
			return result;
		}

		return result;
	}

	@Override
	public List<AgentWithServiceDto> getApprobateurs() {
		List<AgentWithServiceDto> agentDtos = new ArrayList<AgentWithServiceDto>();
		for (Droit da : accessRightsRepository.getAgentsApprobateurs()) {
			AgentWithServiceDto agentDto = sirhWSConsumer.getAgentService(da.getIdAgent(),
					helperService.getCurrentDate());
			agentDtos.add(agentDto);
		}
		Collections.sort(agentDtos, new AgentWithServiceDtoComparator());
		return agentDtos;
	}

	@Override
	public List<AgentWithServiceDto> setApprobateurs(List<AgentWithServiceDto> listeDto) {

		List<AgentWithServiceDto> listeAgentErreur = new ArrayList<AgentWithServiceDto>();
		List<Droit> listeAgentAppro = accessRightsRepository.getAgentsApprobateurs();

		List<Droit> droitsToDelete = new ArrayList<Droit>(listeAgentAppro);

		for (AgentWithServiceDto agentDto : listeDto) {
			if (accessRightsRepository.isUserOperateur(agentDto.getIdAgent())
					|| accessRightsRepository.isUserViseur(agentDto.getIdAgent())) {
				listeAgentErreur.add(agentDto);
				continue;
			}

			Droit d = null;

			for (Droit existingDroit : listeAgentAppro) {
				if (existingDroit.getIdAgent().equals(agentDto.getIdAgent())) {
					d = existingDroit;
					break;
				}
			}

			if (d != null) {
				droitsToDelete.remove(d);
				continue;
			}

			d = new Droit();
			DroitProfil dp = new DroitProfil();
			dp.setDroit(d);
			dp.setDroitApprobateur(d);
			dp.setProfil(accessRightsRepository.getProfilByName(ProfilEnum.APPROBATEUR.toString()));
			d.getDroitProfils().add(dp);
			d.setDateModification(helperService.getCurrentDate());
			d.setIdAgent(agentDto.getIdAgent());
			accessRightsRepository.persisEntity(d);
		}

		for (Droit droitToDelete : droitsToDelete) {
			// on supprime tous les inputters (et sous agents) de l'approbateur
			setInputter(droitToDelete.getIdAgent(), new InputterDto());

			// enfin on supprime l'approbateur
			// First, we remove all the agents this approbateur was approving
			// this will also delete all the agents its operateurs were filling
			// in for
			for (DroitsAgent agentSaisiToDelete : droitToDelete.getAgents()) {
				agentSaisiToDelete.getDroits().clear();
				agentSaisiToDelete.remove();
			}
			for (DroitProfil dp : droitToDelete.getDroitProfils()) {
				deleteDroitProfil(dp);
			}
		}

		return listeAgentErreur;

	}

	private DroitProfil getDelegataireApprobateur(Integer idAgentApprobateur, List<Droit> droitSousAgentsByApprobateur) {

		DroitProfil droitProfil = null;
		for (Droit droit : droitSousAgentsByApprobateur) {
			if (accessRightsRepository.isUserDelegataireOfApprobateur(idAgentApprobateur, droit.getIdAgent())) {
				for (DroitProfil dp : droit.getDroitProfils()) {
					if (dp.getDroitApprobateur().getIdAgent().equals(idAgentApprobateur)) {
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
				result.add(droit);
			}
		}

		return result;
	}

	private ArrayList<Droit> getOperateursApprobateur(Integer idAgentApprobateur,
			List<Droit> droitSousAgentsByApprobateur) {
		ArrayList<Droit> result = new ArrayList<Droit>();
		for (Droit droit : droitSousAgentsByApprobateur) {
			if (accessRightsRepository.isUserOperateurOfApprobateur(idAgentApprobateur, droit.getIdAgent())) {
				result.add(droit);
			}
		}

		return result;
	}

	@Override
	public boolean canUserAccessAccessRights(Integer idAgent) {
		return accessRightsRepository.isUserApprobateur(idAgent);
	}

	@Override
	public InputterDto getInputter(int idAgent) {

		InputterDto result = new InputterDto();

		List<Droit> droit = accessRightsRepository.getDroitSousApprobateur(idAgent);

		if (droit == null) {
			logger.warn("L'agent {} n'est pas approbateur.", idAgent);
			return result;
		}
		for (Droit d : droit) {
			if (accessRightsRepository.isUserDelegataireOfApprobateur(idAgent, d.getIdAgent())) {
				Agent delegataire = sirhRepository.getAgent(d.getIdAgent());

				if (delegataire == null)
					logger.warn("L'agent délégataire {} n'existe pas.", d.getIdAgent());
				else
					result.setDelegataire(new AgentDto(delegataire));

			} else if (accessRightsRepository.isUserOperateurOfApprobateur(idAgent, d.getIdAgent())) {
				Agent ope = sirhRepository.getAgent(d.getIdAgent());
				if (ope == null)
					logger.warn("L'agent opérateur {} n'existe pas.", d.getIdAgent());
				else
					result.getOperateurs().add(new AgentDto(ope));
			} else if (accessRightsRepository.isUserViseurOfApprobateur(idAgent, d.getIdAgent())) {
				Agent ope = sirhRepository.getAgent(d.getIdAgent());
				if (ope == null)
					logger.warn("L'agent viseur {} n'existe pas.", d.getIdAgent());
				else
					result.getViseurs().add(new AgentDto(ope));
			}

		}

		return result;
	}

	@Override
	public ReturnMessageDto setInputter(Integer idAgentAppro, InputterDto dto) {

		ReturnMessageDto result = new ReturnMessageDto();
		Droit droitApprobateur = accessRightsRepository.getAgentAccessRights(idAgentAppro);

		// on recupere la liste des
		List<Droit> droitSousAgentsByApprobateur = accessRightsRepository.getDroitSousApprobateur(idAgentAppro);

		// on trie la liste des sous agents
		ArrayList<Droit> originalOperateurs = getOperateursApprobateur(idAgentAppro, droitSousAgentsByApprobateur);
		ArrayList<Droit> originalViseurs = getViseursApprobateur(idAgentAppro, droitSousAgentsByApprobateur);
		DroitProfil delegataire = getDelegataireApprobateur(idAgentAppro, droitSousAgentsByApprobateur);

		// /////////////////// DELEGATAIRE /////////////////////////////////////
		// on traite le delegataire
		traiteDelegataire(dto, delegataire, droitApprobateur, result);
		// //////////////////// FIN DELEGATAIRE ///////////////////////////////

		// ////////////////////// OPERATEURS //////////////////////////////////
		// on traite les operateurs
		// traiteOperateurs(dto, originalOperateurs, idAgentAppro,
		// droitApprobateur, result);
		// //////////////////// FIN OPERATEURS /////////////////////////

		// ////////////////////// VISEURS //////////////////////////////////
		// on traite les viseurs
		// traiteViseurs(dto, originalViseurs, idAgentAppro, droitApprobateur,
		// result);
		// //////////////////// FIN VISEURS //////////////////////////////////

		return result;
	}

	private void traiteViseurs(InputterDto dto, ArrayList<Droit> originalViseurs, Integer idAgentAppro,
			Droit droitApprobateur, ReturnMessageDto result) {
		for (AgentDto viseurDto : dto.getViseurs()) {

			Droit existingViseur = null;

			// on verifie si le viseur existe deja ou non
			for (Droit viseur : originalViseurs) {
				if (accessRightsRepository.isUserViseurOfApprobateur(idAgentAppro, viseurDto.getIdAgent())) {
					existingViseur = viseur;
					originalViseurs.remove(existingViseur);
					break;
				}
			}

			if (existingViseur != null)
				continue;

			Agent ag = sirhRepository.getAgent(viseurDto.getIdAgent());
			// on verifie que l idAgent existe
			if (null == ag) {
				logger.warn("L'agent viseur {} n'existe pas.", viseurDto.getIdAgent());
				result.getErrors().add(String.format("L'agent viseur [%d] n'existe pas.", viseurDto.getIdAgent()));
				continue;
			}
			// Check that the new viseur is not already approbateur or viseur
			if (accessRightsRepository.isUserApprobateur(viseurDto.getIdAgent())) {
				logger.warn("L'agent %s %s [%d] ne peut pas être viseur car il ou elle est déjà approbateur.",
						ag.getDisplayNom(), ag.getDisplayPrenom(), ag.getIdAgent());
				result.getErrors().add(
						String.format(
								"L'agent %s %s [%d] ne peut pas être viseur car il ou elle est déjà approbateur.",
								ag.getDisplayNom(), ag.getDisplayPrenom(), ag.getIdAgent()));
				continue;
			} else if (accessRightsRepository.isUserOperateur(viseurDto.getIdAgent())) {
				logger.warn("L'agent %s %s [%d] ne peut pas être viseur car il ou elle est déjà opérateur.",
						ag.getDisplayNom(), ag.getDisplayPrenom(), ag.getIdAgent());
				result.getErrors().add(
						String.format("L'agent %s %s [%d] ne peut pas être viseur car il ou elle est déjà opérateur.",
								ag.getDisplayNom(), ag.getDisplayPrenom(), ag.getIdAgent()));
				continue;
			}

			// on regarde si le droit existe deja pour cette personne
			existingViseur = accessRightsRepository.getAgentAccessRights(viseurDto.getIdAgent());

			DroitProfil dp = new DroitProfil();
			dp.setDroit(existingViseur);
			dp.setDroitApprobateur(droitApprobateur);
			dp.setProfil(accessRightsRepository.getProfilByName(ProfilEnum.VISEUR.toString()));

			existingViseur.setDateModification(helperService.getCurrentDate());
			existingViseur.getDroitProfils().add(dp);
			existingViseur.setIdAgent(viseurDto.getIdAgent());

			accessRightsRepository.persisEntity(existingViseur);
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

	private void traiteOperateurs(InputterDto dto, ArrayList<Droit> originalOperateurs, Integer idAgentAppro,
			Droit droitApprobateur, ReturnMessageDto result) {
		for (AgentDto operateurDto : dto.getOperateurs()) {

			Droit existingOperateur = null;

			// on verifie si l operateur existe deja ou non
			for (Droit operateur : originalOperateurs) {
				if (accessRightsRepository.isUserOperateurOfApprobateur(idAgentAppro, operateurDto.getIdAgent())) {
					existingOperateur = operateur;
					originalOperateurs.remove(existingOperateur);
					break;
				}
			}

			if (existingOperateur != null)
				continue;

			Agent ag = sirhRepository.getAgent(operateurDto.getIdAgent());
			// on verifie que l idAgent existe
			if (null == ag) {
				logger.warn("L'agent opérateur {} n'existe pas.", operateurDto.getIdAgent());
				result.getErrors()
						.add(String.format("L'agent opérateur [%d] n'existe pas.", operateurDto.getIdAgent()));
				continue;
			}
			// Check that the new operateur is not already delegataire or
			// approbateur or viseur
			if (accessRightsRepository.isUserApprobateur(operateurDto.getIdAgent())) {
				logger.warn("L'agent %s %s [%d] ne peut pas être opérateur car il ou elle est déjà approbateur.",
						ag.getDisplayNom(), ag.getDisplayPrenom(), ag.getIdAgent());
				result.getErrors().add(
						String.format(
								"L'agent %s %s [%d] ne peut pas être opérateur car il ou elle est déjà approbateur.",
								ag.getDisplayNom(), ag.getDisplayPrenom(), ag.getIdAgent()));
				continue;
			} else if (accessRightsRepository.isUserViseur(operateurDto.getIdAgent())) {
				logger.warn("L'agent %s %s [%d] ne peut pas être opérateur car il ou elle est déjà viseur.",
						ag.getDisplayNom(), ag.getDisplayPrenom(), ag.getIdAgent());
				result.getErrors().add(
						String.format("L'agent %s %s [%d] ne peut pas être opérateur car il ou elle est déjà viseur.",
								ag.getDisplayNom(), ag.getDisplayPrenom(), ag.getIdAgent()));
				continue;
			} else if (accessRightsRepository.isUserDelegataire(operateurDto.getIdAgent())) {
				logger.warn("L'agent %s %s [%d] ne peut pas être opérateur car il ou elle est déjà délégataire.",
						ag.getDisplayNom(), ag.getDisplayPrenom(), ag.getIdAgent());
				result.getErrors().add(
						String.format(
								"L'agent %s %s [%d] ne peut pas être opérateur car il ou elle est déjà délégataire.",
								ag.getDisplayNom(), ag.getDisplayPrenom(), ag.getIdAgent()));
				continue;
			}

			// on regarde si le droit existe deja pour cette personne

			existingOperateur = accessRightsRepository.getAgentAccessRights(operateurDto.getIdAgent());

			DroitProfil dp = new DroitProfil();
			dp.setDroit(existingOperateur);
			dp.setDroitApprobateur(droitApprobateur);
			dp.setProfil(accessRightsRepository.getProfilByName(ProfilEnum.OPERATEUR.toString()));

			existingOperateur.setDateModification(helperService.getCurrentDate());
			existingOperateur.getDroitProfils().add(dp);
			existingOperateur.setIdAgent(operateurDto.getIdAgent());

			accessRightsRepository.persisEntity(existingOperateur);
		}

		// on supprime les operateurs en trop
		ArrayList<DroitProfil> dpASupp = new ArrayList<>();
		Droit droitAppro = accessRightsRepository.getAgentAccessRights(idAgentAppro);
		for (Droit droitOperateurToDelete : originalOperateurs) {
			for (DroitProfil droitProfilOperateur : droitOperateurToDelete.getDroitProfils()) {
				if (droitProfilOperateur.getDroitApprobateur().getIdDroit().equals(droitAppro.getIdDroit())) {
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

	private void traiteDelegataire(InputterDto dto, DroitProfil delegataire, Droit droitApprobateur,
			ReturnMessageDto result) {

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

		Agent ag = sirhRepository.getAgent(dto.getDelegataire().getIdAgent());
		// on verifie que l idAgent existe
		if (null == ag) {
			logger.warn("L'agent délégataire {} n'existe pas.", dto.getDelegataire().getIdAgent());
			result.getErrors().add(
					String.format("L'agent délégataire [%d] n'existe pas.", dto.getDelegataire().getIdAgent()));
			return;

		}

		// Check that the new delegataire is not an operator
		if (accessRightsRepository.isUserOperateur(dto.getDelegataire().getIdAgent())) {
			logger.warn("L'agent %s %s [%d] ne peut pas être délégataire car il ou elle est déjà opérateur.",
					ag.getDisplayNom(), ag.getDisplayPrenom(), ag.getIdAgent());
			result.getErrors().add(
					String.format("L'agent %s %s [%d] ne peut pas être délégataire car il ou elle est déjà opérateur.",
							ag.getDisplayNom(), ag.getDisplayPrenom(), ag.getIdAgent()));
			return;

		}

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

	@Override
	public List<AgentDto> getAgentsToApproveOrInput(int idAgent) {
		return getAgentsToApproveOrInput(idAgent, null);
	}

	/**
	 * Retrieves the agent an approbator is set to Approve or an Operator is set
	 * to Input. This service also filters by service
	 */
	@Override
	public List<AgentDto> getAgentsToApproveOrInput(Integer idAgent, String codeService) {

		List<AgentDto> result = new ArrayList<AgentDto>();

		for (DroitsAgent da : accessRightsRepository.getListOfAgentsToInputOrApprove(idAgent, codeService)) {
			AgentDto agDto = new AgentDto();
			Agent ag = sirhRepository.getAgent(da.getIdAgent());
			agDto.setIdAgent(da.getIdAgent());
			agDto.setNom(ag.getDisplayNom());
			agDto.setPrenom(ag.getDisplayPrenom());
			result.add(agDto);
		}

		return result;

	}

	private void deleteDroitProfil(DroitProfil droitProfil) {

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
}
