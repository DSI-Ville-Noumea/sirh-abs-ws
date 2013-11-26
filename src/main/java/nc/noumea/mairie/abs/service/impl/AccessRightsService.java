package nc.noumea.mairie.abs.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
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
			// on remove tous les sous droits des approbateurs
			// ie : suppression opérateurs,viseurs,delegataire et tous les
			// agents liés à ces droits.
			// removeDroitAndSousDroit(droitToDelete);

			// First, we remove all the agents this approbateur was approving
			// this will also delete all the agents its operateurs were filling
			// in for
			for (DroitsAgent agentSaisiToDelete : droitToDelete.getAgents()) {
				agentSaisiToDelete.getDroits().clear();
				agentSaisiToDelete.remove();
			}
			for (DroitProfil dp : droitToDelete.getDroitProfils()) {
				logger.debug("Droit profil : [ idDroitProfil : " + dp.getIdDroitProfil() + ",idDroit :  "
						+ dp.getDroit().getIdDroit() + "]");
				droitToDelete.getDroitProfils().remove(dp);
				dp.remove();
			}

			// Then we delete the approbateur
			droitToDelete.remove();
		}

		return listeAgentErreur;

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
			for (DroitProfil dp : d.getDroitProfils()) {
				if (accessRightsRepository.isUserDelegataire(dp.getDroit().getIdAgent())) {
					Agent delegataire = sirhRepository.getAgent(dp.getDroit().getIdAgent());

					if (delegataire == null)
						logger.warn("L'agent délégataire {} n'existe pas.", dp.getDroit().getIdAgent());
					else
						result.setDelegataire(new AgentDto(delegataire));

				} else if (accessRightsRepository.isUserOperateur(dp.getDroit().getIdAgent())) {
					Agent ope = sirhRepository.getAgent(dp.getDroit().getIdAgent());
					if (ope == null)
						logger.warn("L'agent opérateur {} n'existe pas.", dp.getDroit().getIdAgent());
					else
						result.getOperateurs().add(new AgentDto(ope));
				} else if (accessRightsRepository.isUserViseur(dp.getDroit().getIdAgent())) {
					Agent ope = sirhRepository.getAgent(dp.getDroit().getIdAgent());
					if (ope == null)
						logger.warn("L'agent viseur {} n'existe pas.", dp.getDroit().getIdAgent());
					else
						result.getViseurs().add(new AgentDto(ope));
				}
			}
		}

		return result;
	}

	@Override
	public ReturnMessageDto setInputter(Integer idAgent, InputterDto dto) {

		ReturnMessageDto result = new ReturnMessageDto();
		Droit droitApprobateur = accessRightsRepository.getAgentAccessRights(idAgent);
		
		// on recupere la liste des 
		List<Droit> droitSousAgentsByApprobateur = accessRightsRepository.getDroitSousApprobateur(idAgent);
		
		// on trie la liste des sous agents
		List<Droit> originalOperateurs = new ArrayList<Droit>();
		List<Droit> originalViseurs = new ArrayList<Droit>();
		DroitProfil delegataire = null;
		if(null != droitSousAgentsByApprobateur) {
			for (Droit droits : droitSousAgentsByApprobateur) {
				for (DroitProfil dp : droits.getDroitProfils()) {
					if(accessRightsRepository.isUserOperateur(dp.getDroit().getIdAgent())) {
						originalOperateurs.add(dp.getDroit());
						continue;
					}
					if(accessRightsRepository.isUserViseur(dp.getDroit().getIdAgent())) {
						originalViseurs.add(dp.getDroit());
						continue;
					}
					if(accessRightsRepository.isUserDelegataire(dp.getDroit().getIdAgent())) {
						delegataire = dp;
						continue;
					}
				}
			}
		}
		
		///////////////////// DELEGATAIRE /////////////////////////////////////
		// on traite le delegataire
		if (dto.getDelegataire() != null) {
			
			 if(null != delegataire
						&& !delegataire.getDroit().getIdAgent().equals(dto.getDelegataire().getIdAgent())){
				Agent ag = sirhRepository.getAgent(dto.getDelegataire().getIdAgent());
				// on verifie que l idAgent existe
				if(null == ag) {
					logger.warn("L'agent délégataire {} n'existe pas.", dto.getDelegataire().getIdAgent());
					result.getErrors()
					.add(String
							.format("L'agent délégataire [%d] n'existe pas.",
									dto.getDelegataire().getIdAgent()));
				// Check that the new delegataire is not an operator
				} else if (accessRightsRepository.isUserOperateur(dto.getDelegataire().getIdAgent())) {
					result.getErrors()
							.add(String
									.format("L'agent %s %s [%d] ne peut pas être délégataire car il ou elle est déjà opérateur.",
											ag.getDisplayNom(),
											ag.getDisplayPrenom(), ag.getIdAgent()));
				} else {
					
					// on supprime d abord le delegataire precedent
					if(null != delegataire) {
						deleteDroitProfil(delegataire);
					}
					
					Droit d = new Droit();
					DroitProfil dp = new DroitProfil();
	
					d.setIdAgent(dto.getDelegataire().getIdAgent());
					d.setDateModification(helperService.getCurrentDate());
					d.setDroitProfils(Arrays.asList(dp));
					dp.setDroit(d);
					dp.setProfil(accessRightsRepository.getProfilByName(ProfilEnum.DELEGATAIRE.toString()));
					dp.setDroitApprobateur(droitApprobateur);
					accessRightsRepository.persisEntity(d);
				}
			}
		} else if(null != delegataire) {
			deleteDroitProfil(delegataire);
		}
		////////////////////// FIN DELEGATAIRE ///////////////////////////////

		//////////////////////// OPERATEURS //////////////////////////////////
		// on traite les operateurs	
		for (AgentDto operateurDto : dto.getOperateurs()) {

			Droit existingOperateur = null;
			
			// on verifie si l operateur existe deja ou non
			for (Droit operateur : originalOperateurs) {
				if (operateur.getIdAgent().equals(operateurDto.getIdAgent())) {
					existingOperateur = operateur;
					originalOperateurs.remove(existingOperateur);
					break;
				}
			}

			if (existingOperateur != null)
				continue;

			Agent ag = sirhRepository.getAgent(operateurDto.getIdAgent());
			// on verifie que l idAgent existe
			if(null == ag) {
				logger.warn("L'agent opérateur {} n'existe pas.", operateurDto.getIdAgent());
				result.getErrors()
				.add(String
						.format("L'agent opérateur [%d] n'existe pas.",
								operateurDto.getIdAgent()));
				continue;
			}
			// Check that the new operateur is not already delegataire or approbateur or viseur
			if (accessRightsRepository.isUserApprobateur(operateurDto.getIdAgent())) {
				result.getErrors()
						.add(String
								.format("L'agent %s %s [%d] ne peut pas être opérateur car il ou elle est déjà approbateur.",
										ag.getDisplayNom(),
										ag.getDisplayPrenom(), ag.getIdAgent()));
				continue;
			}
			if (accessRightsRepository.isUserViseur(operateurDto.getIdAgent())) {
				result.getErrors()
						.add(String
								.format("L'agent %s %s [%d] ne peut pas être opérateur car il ou elle est déjà viseur.",
										ag.getDisplayNom(),
										ag.getDisplayPrenom(), ag.getIdAgent()));
				continue;
			}
			if (accessRightsRepository.isUserDelegataire(operateurDto.getIdAgent())) {
				result.getErrors()
						.add(String
								.format("L'agent %s %s [%d] ne peut pas être opérateur car il ou elle est déjà délégataire.",
										ag.getDisplayNom(),
										ag.getDisplayPrenom(), ag.getIdAgent()));
				continue;
			}
			
			existingOperateur = new Droit();
			
			DroitProfil dp = new DroitProfil();
			dp.setDroit(existingOperateur);
			dp.setDroitApprobateur(droitApprobateur);
			dp.setProfil(accessRightsRepository.getProfilByName(ProfilEnum.OPERATEUR.toString()));
			
			existingOperateur.setDateModification(helperService.getCurrentDate());
			existingOperateur.setDroitProfils(Arrays.asList(dp));
			existingOperateur.setIdAgent(operateurDto.getIdAgent());
			
			accessRightsRepository.persisEntity(existingOperateur);
		}

		// on supprime les operateurs en trop
		for (Droit droitOperateurToDelete : originalOperateurs) {
			for(DroitProfil droitProfilOperateur : droitOperateurToDelete.getDroitProfils()) {
				if(null != droitProfilOperateur.getDroitApprobateur()
						&& idAgent.equals(droitProfilOperateur.getDroitApprobateur().getIdAgent())) {
					deleteDroitProfil(droitProfilOperateur);
				}
			}
		}
		////////////////////// FIN OPERATEURS //////////////////////////////////

		//////////////////////// VISEURS //////////////////////////////////
		// on traite les viseurs	
		for (AgentDto viseurDto : dto.getViseurs()) {

			Droit existingViseur = null;
			
			// on verifie si le viseur existe deja ou non
			for (Droit viseur : originalViseurs) {
				if (viseur.getIdAgent().equals(viseurDto.getIdAgent())) {
					existingViseur = viseur;
					originalViseurs.remove(existingViseur);
					break;
				}
			}

			if (existingViseur != null)
				continue;
			
			Agent ag = sirhRepository.getAgent(viseurDto.getIdAgent());
			// on verifie que l idAgent existe
			if(null == ag) {
				logger.warn("L'agent viseur {} n'existe pas.", viseurDto.getIdAgent());
				result.getErrors()
				.add(String
						.format("L'agent viseur [%d] n'existe pas.",
								viseurDto.getIdAgent()));
				continue;
			}
			// Check that the new viseur is not already approbateur or viseur
			if (accessRightsRepository.isUserApprobateur(viseurDto.getIdAgent())) {
				result.getErrors()
						.add(String
								.format("L'agent %s %s [%d] ne peut pas être viseur car il ou elle est déjà approbateur.",
										ag.getDisplayNom(),
										ag.getDisplayPrenom(), ag.getIdAgent()));
				continue;
			}
			if (accessRightsRepository.isUserViseur(viseurDto.getIdAgent())) {
				result.getErrors()
						.add(String
								.format("L'agent %s %s [%d] ne peut pas être viseur car il ou elle est déjà viseur.",
										ag.getDisplayNom(),
										ag.getDisplayPrenom(), ag.getIdAgent()));
				continue;
			}
			
			existingViseur = new Droit();
			
			DroitProfil dp = new DroitProfil();
			dp.setDroit(existingViseur);
			dp.setDroitApprobateur(droitApprobateur);
			dp.setProfil(accessRightsRepository.getProfilByName(ProfilEnum.VISEUR.toString()));
			
			existingViseur.setDateModification(helperService.getCurrentDate());
			existingViseur.setDroitProfils(Arrays.asList(dp));
			existingViseur.setIdAgent(viseurDto.getIdAgent());
			
			accessRightsRepository.persisEntity(existingViseur);
		}

		// on supprime les operateurs en trop
		for (Droit droitViseurToDelete : originalViseurs) {
			for(DroitProfil droitProfilViseur : droitViseurToDelete.getDroitProfils()) {
				if(null != droitProfilViseur.getDroitApprobateur()
						&& idAgent.equals(droitProfilViseur.getDroitApprobateur().getIdAgent())) {
					deleteDroitProfil(droitProfilViseur);
				}
			}
		}
		////////////////////// FIN VISEURS //////////////////////////////////
		
		return result;
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
		accessRightsRepository.deleteDroitProfilByIdDroitAndIdProfil(droitProfil.getIdDroitProfil());
		
		// on verifie que l agent n a pas d autre profil, si non on supprime son droit
		if(null != droitProfil.getDroit().getDroitProfils()
				&& 2 > droitProfil.getDroit().getDroitProfils().size()) {
			accessRightsRepository.removeEntity(droitProfil.getDroit());
		}
	}
}
