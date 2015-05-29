package nc.noumea.mairie.abs.web;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import nc.noumea.mairie.abs.dto.AccessRightsDto;
import nc.noumea.mairie.abs.dto.ActeursDto;
import nc.noumea.mairie.abs.dto.AgentDto;
import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.abs.dto.ApprobateurDto;
import nc.noumea.mairie.abs.dto.InputterDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.dto.ViseursDto;
import nc.noumea.mairie.abs.service.IAccessRightsService;
import nc.noumea.mairie.abs.service.IAgentMatriculeConverterService;
import nc.noumea.mairie.ws.ISirhWSConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/droits")
public class AccessRightsController {

	private Logger logger = LoggerFactory.getLogger(AccessRightsController.class);

	@Autowired
	private IAccessRightsService accessRightService;

	@Autowired
	private IAgentMatriculeConverterService converterService;

	@Autowired
	private ISirhWSConsumer sirhWSConsumer;

	/**
	 * Retourne les droits d un agent
	 */
	@ResponseBody
	@RequestMapping(value = "listeDroitsAgent", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public AccessRightsDto listAgentAccessRights(@RequestParam("idAgent") Integer idAgent) {

		logger.debug("entered GET [droits/listeDroitsAgent] => listAgentAccessRights with parameter idAgent = {}",
				idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		AgentGeneriqueDto agent = sirhWSConsumer.getAgent(convertedIdAgent);
		if (agent == null || agent.getIdAgent() == null)
			throw new NotFoundException();

		return accessRightService.getAgentAccessRights(convertedIdAgent);
	}

	/**
	 * Retourne la liste des approbateurs
	 */
	@ResponseBody
	@RequestMapping(value = "approbateurs", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<ApprobateurDto> listApprobateurs(@RequestParam(value = "idAgent", required = false) Integer idAgent,
			@RequestParam(value = "codeService", required = false) String codeService) {

		logger.debug(
				"entered GET [droits/approbateurs] => listApprobateurs with parameter idAgent = {} and codeService = {} --> for SIRH ",
				idAgent, codeService);

		return accessRightService.getApprobateurs(idAgent, codeService);
	}

	/**
	 * Cree/modifie les approbateurs
	 */
	@ResponseBody
	@RequestMapping(value = "approbateurs", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto setApprobateur(@RequestBody AgentWithServiceDto agDtos) {
		logger.debug("entered POST [droits/approbateurs] => setApprobateur --> for SIRH ");

		ReturnMessageDto agentErreur = new ReturnMessageDto();
		try {
			agentErreur = accessRightService.setApprobateur(agDtos);
		} catch (Exception e) {
			logger.debug(e.getMessage());
			throw new ConflictException(e.getMessage());
		}

		return agentErreur;
	}

	/**
	 * Supprime les approbateurs
	 */
	@ResponseBody
	@RequestMapping(value = "deleteApprobateurs", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto deleteApprobateur(@RequestBody AgentWithServiceDto agDtos) {
		logger.debug("entered POST [droits/deleteApprobateurs] => deleteApprobateur --> for SIRH ");

		ReturnMessageDto agentErreur = new ReturnMessageDto();
		try {
			agentErreur = accessRightService.deleteApprobateur(agDtos);
		} catch (Exception e) {
			logger.debug(e.getMessage());
			throw new ConflictException(e.getMessage());
		}

		return agentErreur;
	}

	/**
	 * Retourne le delegataire et les operateurs d un approbateur
	 */
	@ResponseBody
	@RequestMapping(value = "inputter", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public InputterDto getInputter(@RequestParam("idAgent") Integer idAgent) {
		logger.debug("entered GET [droits/inputter] => getInputter with parameter idAgent = {}", idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (!accessRightService.canUserAccessAccessRights(convertedIdAgent))
			throw new AccessForbiddenException();

		return accessRightService.getInputter(convertedIdAgent);
	}

	/**
	 * Saisie/modification du delegataire et les operateurs d un approbateur
	 */
	@ResponseBody
	@RequestMapping(value = "inputter", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto setInputter(@RequestParam("idAgent") Integer idAgent, @RequestBody InputterDto inputterDto,
			HttpServletResponse response) {

		logger.debug("entered POST [droits/inputter] => setInputter with parameter idAgent = {}", idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (!accessRightService.canUserAccessAccessRights(convertedIdAgent))
			throw new AccessForbiddenException();

		ReturnMessageDto result = accessRightService.setInputter(convertedIdAgent, inputterDto);

		if (result.getErrors().size() != 0)
			response.setStatus(HttpServletResponse.SC_CONFLICT);

		return result;
	}

	/**
	 * #15713 special pour SIRH : one shot
	 * Saisie/modification d un operateurs d un approbateur
	 * WS specifique a SIRH
	 */
	@ResponseBody
	@RequestMapping(value = "operateurSIRH", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto setOperateur(@RequestParam("idAgent") Integer idAgent, @RequestBody AgentDto agentDto,
			HttpServletResponse response) {

		logger.debug("entered POST [droits/operateur] => setOperateur with parameter idAgent = {}", idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (!accessRightService.canUserAccessAccessRights(convertedIdAgent))
			throw new AccessForbiddenException();

		ReturnMessageDto result = accessRightService.setOperateur(convertedIdAgent, agentDto);

		if (result.getErrors().size() != 0)
			response.setStatus(HttpServletResponse.SC_CONFLICT);

		return result;
	}

	/**
	 * #15713 special pour SIRH : one shot
	 * Saisie/modification d un operateurs d un approbateur
	 * WS specifique a SIRH
	 */
	@ResponseBody
	@RequestMapping(value = "deleteOperateurSIRH", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto deleteOperateur(@RequestParam("idAgent") Integer idAgent, @RequestBody AgentDto agentDto,
			HttpServletResponse response) {

		logger.debug("entered DELETE [droits/operateur] => deleteOperateur with parameter idAgent = {}", idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (!accessRightService.canUserAccessAccessRights(convertedIdAgent))
			throw new AccessForbiddenException();

		ReturnMessageDto result = accessRightService.deleteOperateur(convertedIdAgent, agentDto);

		if (result.getErrors().size() != 0)
			response.setStatus(HttpServletResponse.SC_CONFLICT);

		return result;
	}

	/**
	 * Retourne les viseurs d un approbateur
	 */
	@ResponseBody
	@RequestMapping(value = "viseur", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public ViseursDto getViseurs(@RequestParam("idAgent") Integer idAgent) {
		logger.debug("entered GET [droits/viseur] => getViseurs with parameter idAgent = {}", idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (!accessRightService.canUserAccessAccessRights(convertedIdAgent))
			throw new AccessForbiddenException();

		return accessRightService.getViseurs(convertedIdAgent);
	}

	/**
	 * Saisie/modification des viseurs d un approbateur
	 */
	@ResponseBody
	@RequestMapping(value = "viseur", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto setViseurs(@RequestParam("idAgent") Integer idAgent, @RequestBody ViseursDto viseursDto,
			HttpServletResponse response) {

		logger.debug("entered POST [droits/viseur] => setViseurs with parameter idAgent = {}", idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (!accessRightService.canUserAccessAccessRights(convertedIdAgent))
			throw new AccessForbiddenException();

		ReturnMessageDto result = accessRightService.setViseurs(convertedIdAgent, viseursDto);

		if (result.getErrors().size() != 0)
			response.setStatus(HttpServletResponse.SC_CONFLICT);

		return result;
	}

	/**
	 * #15713 special pour SIRH : one shot
	 * Saisie/modification d un operateurs d un approbateur
	 * WS specifique a SIRH
	 */
	@ResponseBody
	@RequestMapping(value = "viseurSIRH", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto setViseur(@RequestParam("idAgent") Integer idAgent, @RequestBody AgentDto agentDto,
			HttpServletResponse response) {

		logger.debug("entered POST [droits/viseurSIRH] => setViseur with parameter idAgent = {}", idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (!accessRightService.canUserAccessAccessRights(convertedIdAgent))
			throw new AccessForbiddenException();

		ReturnMessageDto result = accessRightService.setViseur(convertedIdAgent, agentDto);

		if (result.getErrors().size() != 0)
			response.setStatus(HttpServletResponse.SC_CONFLICT);

		return result;
	}

	/**
	 * #15713 special pour SIRH : one shot
	 * Saisie/modification d un operateurs d un approbateur
	 * WS specifique a SIRH
	 */
	@ResponseBody
	@RequestMapping(value = "deleteViseurSIRH", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto deleteViseur(@RequestParam("idAgent") Integer idAgent, @RequestBody AgentDto agentDto,
			HttpServletResponse response) {

		logger.debug("entered DELETE [droits/viseur] => deleteViseur with parameter idAgent = {}", idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (!accessRightService.canUserAccessAccessRights(convertedIdAgent))
			throw new AccessForbiddenException();

		ReturnMessageDto result = accessRightService.deleteViseur(convertedIdAgent, agentDto);

		if (result.getErrors().size() != 0)
			response.setStatus(HttpServletResponse.SC_CONFLICT);

		return result;
	}

	/**
	 * Retourne la liste des agents que l approbateur doit approuver
	 */
	@ResponseBody
	@RequestMapping(value = "agentsApprouves", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<AgentDto> getApprovedAgents(@RequestParam("idAgent") Integer idAgent) {

		logger.debug("entered GET [droits/agentsApprouves] => getApprovedAgents with parameter idAgent = {}", idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (!accessRightService.canUserAccessAccessRights(convertedIdAgent))
			throw new AccessForbiddenException();

		List<AgentDto> result = accessRightService.getAgentsToApproveOrInput(convertedIdAgent, convertedIdAgent);

		if (result.size() == 0)
			throw new NoContentException();

		return result;
	}

	/**
	 * Saisie/modifie la liste des agents que l approbateur doit approuver
	 */
	@ResponseBody
	@RequestMapping(value = "agentsApprouves", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto setApprovedAgents(@RequestParam("idAgent") Integer idAgent,
			@RequestBody List<AgentDto> agDtos, HttpServletResponse response) {

		logger.debug("entered POST [droits/agentsApprouves] => setApprovedAgents with parameter idAgent = {}", idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (!accessRightService.canUserAccessAccessRights(convertedIdAgent))
			throw new AccessForbiddenException();

		ReturnMessageDto result = accessRightService.setAgentsToApprove(convertedIdAgent, agDtos);

		if (result.getErrors().size() != 0)
			response.setStatus(HttpServletResponse.SC_CONFLICT);

		return result;
	}

	/**
	 * Retourne la liste des agents affectes a un operateur
	 */
	@ResponseBody
	@RequestMapping(value = "agentsSaisisByOperateur", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<AgentDto> getInputAgentsByOperateur(@RequestParam("idAgent") Integer idAgent,
			@RequestParam(value = "idOperateur") Integer idOperateur) {

		logger.debug(
				"entered GET [droits/agentsSaisisByOperateur] => getInputAgentsByOperateur with parameter idAgent = {} and idOperateur = {} ",
				idAgent, idOperateur);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (!accessRightService.canUserAccessAccessRights(convertedIdAgent))
			throw new AccessForbiddenException();

		int convertedIdOperateur = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idOperateur);

		List<AgentDto> result = accessRightService.getAgentsToInputByOperateur(convertedIdAgent,
				convertedIdOperateur);

		if (result.size() == 0)
			throw new NoContentException();

		return result;
	}
	
	/**
	 * Retourne la liste des agents affectes a un viseur
	 */
	@ResponseBody
	@RequestMapping(value = "agentsSaisisByViseur", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<AgentDto> getInputAgentsByViseur(@RequestParam("idAgent") Integer idAgent,
			@RequestParam(value = "idViseur") Integer idViseur) {

		logger.debug(
				"entered GET [droits/agentsSaisisByViseur] => getInputAgentsByViseur with parameter idAgent = {} and idViseur = {} ",
				idAgent, idViseur);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (!accessRightService.canUserAccessAccessRights(convertedIdAgent))
			throw new AccessForbiddenException();

		int convertedIdViseur = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idViseur);

		List<AgentDto> result = accessRightService.getAgentsToInputByViseur(convertedIdAgent,
				convertedIdViseur);

		if (result.size() == 0)
			throw new NoContentException();

		return result;
	}

	/**
	 * Saisie/modifie la liste des agents affectes a un operateur
	 */
	@ResponseBody
	@RequestMapping(value = "agentsSaisisByOperateur", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto setInputAgentsByOperateur(@RequestParam("idAgent") Integer idAgent,
			@RequestParam("idOperateur") Integer idOperateur,
			@RequestBody List<AgentDto> agentsApprouves, HttpServletResponse response) {

		logger.debug(
				"entered POST [droits/agentsSaisisByOperateur] => setInputAgentsByOperateur with parameter idAgent = {} and idOperateur = {}",
				idAgent, idOperateur);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (!accessRightService.canUserAccessAccessRights(convertedIdAgent))
			throw new AccessForbiddenException();

		int convertedIdOperateur = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idOperateur);

		AgentGeneriqueDto agent = sirhWSConsumer.getAgent(convertedIdOperateur);
		if (agent == null || agent.getIdAgent() == null)
			throw new NotFoundException();

		ReturnMessageDto result = accessRightService.setAgentsToInputByOperateur(convertedIdAgent, convertedIdOperateur,
				agentsApprouves);

		if (result.getErrors().size() != 0)
			response.setStatus(HttpServletResponse.SC_CONFLICT);

		return result;
	}

	/**
	 * Saisie/modifie la liste des agents affectes a un viseur
	 */
	@ResponseBody
	@RequestMapping(value = "agentsSaisisByViseur", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto setInputAgentsByViseur(@RequestParam("idAgent") Integer idAgent,
			@RequestParam("idViseur") Integer idViseur,
			@RequestBody List<AgentDto> agentsApprouves, HttpServletResponse response) {

		logger.debug(
				"entered POST [droits/agentsSaisisByViseur] => setInputAgentsByViseur with parameter idAgent = {} and idViseur = {}",
				idAgent, idViseur);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (!accessRightService.canUserAccessAccessRights(convertedIdAgent))
			throw new AccessForbiddenException();

		int convertedIdViseur = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idViseur);

		AgentGeneriqueDto agent = sirhWSConsumer.getAgent(convertedIdViseur);
		if (agent == null || agent.getIdAgent() == null)
			throw new NotFoundException();

		ReturnMessageDto result = accessRightService.setAgentsToInputByViseur(convertedIdAgent, convertedIdViseur,
				agentsApprouves);

		if (result.getErrors().size() != 0)
			response.setStatus(HttpServletResponse.SC_CONFLICT);

		return result;
	}

	/**
	 * Saisie/modification du delegataire d un approbateur --> UTILE à SIRH
	 */
	@ResponseBody
	@RequestMapping(value = "delegataire", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto setDelegataire(@RequestParam("idAgent") Integer idAgent,
			@RequestBody InputterDto inputterDto, HttpServletResponse response) {

		logger.debug("entered POST [droits/delegataire] => setDelegataire with parameter idAgent = {}", idAgent);
		ReturnMessageDto result = new ReturnMessageDto();

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (!accessRightService.canUserAccessAccessRights(convertedIdAgent)) {
			result.getErrors().add(String.format("L'agent [%s] n'est pas approbateur.", convertedIdAgent));
			return result;
		}

		result = accessRightService.setDelegataire(convertedIdAgent, inputterDto, result);

		return result;
	}

	/**
	 * Liste des acteurs (opérateur(s), viseur(s), approbateur(s) (+ delegataire) d un agent
	 */
	@ResponseBody
	@RequestMapping(value = "listeActeurs", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public ActeursDto getListeActeurs(@RequestParam("idAgent") Integer idAgent) {

		logger.debug("entered POST [droits/listeActeurs] => getListeActeurs with parameter idAgent = {}", idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		return accessRightService.getListeActeurs(convertedIdAgent);
	}
}
