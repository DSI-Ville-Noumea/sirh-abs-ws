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
		/*
		 * Droit appro = accessRightsRepository.getAgentAccessRights(idAgent);
		 * 
		 * List<Droit> droitApprobateur =
		 * accessRightsRepository.getDroitSousApprobateur(idAgent);
		 * 
		 * List<Droit> originalOperateurs = new ArrayList<Droit>(); for (Droit
		 * droits : droitApprobateur) { for (DroitProfil dp :
		 * droits.getDroitProfils()) { if
		 * (accessRightsRepository.isUserOperator(dp.getDroit().getIdAgent())) {
		 * originalOperateurs.add(dp.getDroit()); } } }
		 * 
		 * if (dto.getDelegataire() != null) { // Check that the new delegataire
		 * is not an operator if
		 * (accessRightsRepository.isUserOperator(dto.getDelegataire
		 * ().getIdAgent())) { Agent ag =
		 * sirhRepository.getAgent(dto.getDelegataire().getIdAgent());
		 * result.getErrors().add( String.format(
		 * "L'agent %s %s [%d] ne peut pas être délégataire car il ou elle est déjà opérateur."
		 * , ag.getDisplayNom(), ag.getDisplayPrenom(), ag.getIdAgent())); }
		 * else { Droit d = new Droit(); DroitProfil dp = new DroitProfil();
		 * 
		 * d.setIdAgent(dto.getDelegataire().getIdAgent());
		 * d.setDateModification(helperService.getCurrentDate());
		 * d.setDroitProfils(Arrays.asList(dp)); dp.setDroit(d);
		 * dp.setProfil(accessRightsRepository.getProfilByName("DELEGATAIRE"));
		 * dp.setDroitApprobateur(appro);
		 * accessRightsRepository.persisEntity(d); } } else { //
		 * droitApprobateur.setIdAgentDelegataire(null); }
		 */

		/*
		 * for (AgentDto operateurDto : dto.getSaisisseurs()) {
		 * 
		 * Droit existingOperateur = null;
		 * 
		 * for (Droit operateur : droitApprobateur.getOperateurs()) { if
		 * (operateur.getIdAgent().equals(operateurDto.getIdAgent())) {
		 * existingOperateur = operateur;
		 * originalOperateurs.remove(existingOperateur); break; } }
		 * 
		 * if (existingOperateur != null) continue;
		 * 
		 * // Check that the new operateur is not already delegataire or //
		 * approbateur if
		 * (accessRightsRepository.isUserApprobatorOrDelegataire(operateurDto
		 * .getIdAgent())) { Agent ag =
		 * sirhRepository.getAgent(operateurDto.getIdAgent());
		 * result.getErrors() .add(String .format(
		 * "L'agent %s %s [%d] ne peut pas être opérateur car il ou elle est déjà approbateur ou délégataire."
		 * , ag.getDisplayNom(), ag.getDisplayPrenom(), ag.getIdAgent()));
		 * continue; }
		 * 
		 * existingOperateur = new Droit();
		 * existingOperateur.setDroitApprobateur(droitApprobateur);
		 * existingOperateur.setOperateur(true);
		 * existingOperateur.setIdAgent(operateurDto.getIdAgent());
		 * existingOperateur
		 * .setDateModification(helperService.getCurrentDate());
		 * droitApprobateur.getOperateurs().add(existingOperateur); }
		 * 
		 * for (Droit droitOperateurToDelete : originalOperateurs) {
		 * droitApprobateur.getOperateurs().remove(droitOperateurToDelete);
		 * droitOperateurToDelete.remove(); }
		 */

		return result;
	}
}
