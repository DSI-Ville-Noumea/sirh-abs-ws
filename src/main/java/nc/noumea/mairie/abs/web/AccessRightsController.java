package nc.noumea.mairie.abs.web;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import nc.noumea.mairie.abs.dto.AccessRightsDto;
import nc.noumea.mairie.abs.dto.AgentDto;
import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
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
	public List<AgentWithServiceDto> listApprobateurs() {

		logger.debug("entered GET [droits/approbateurs] => listApprobateurs with no parameter --> for SIRH ");

		return accessRightService.getApprobateurs();
	}

	/**
	 * Cree/modifie les approbateurs
	 */
	@ResponseBody
	@RequestMapping(value = "approbateurs", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public List<AgentWithServiceDto> setApprobateur(@RequestBody List<AgentWithServiceDto> agDtos) {
		logger.debug("entered POST [droits/approbateurs] => setApprobateur --> for SIRH ");

		List<AgentWithServiceDto> agentErreur = new ArrayList<AgentWithServiceDto>();
		try {
			agentErreur = accessRightService.setApprobateurs(agDtos);
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
	 * Retourne la liste des agents affectes a un operateur ou viseur
	 */
	@ResponseBody
	@RequestMapping(value = "agentsSaisis", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<AgentDto> getInputAgents(@RequestParam("idAgent") Integer idAgent,
			@RequestParam(value = "idOperateurOrViseur") Integer idOperateurOrViseur) {

		logger.debug(
				"entered GET [droits/agentsSaisis] => getInputAgents with parameter idAgent = {} and idOperateurOrViseur = {} ",
				idAgent, idOperateurOrViseur);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (!accessRightService.canUserAccessAccessRights(convertedIdAgent))
			throw new AccessForbiddenException();

		int convertedIdOperateurOrViseur = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idOperateurOrViseur);

		List<AgentDto> result = accessRightService.getAgentsToApproveOrInput(convertedIdAgent,
				convertedIdOperateurOrViseur);

		if (result.size() == 0)
			throw new NoContentException();

		return result;
	}

	/**
	 * Saisie/modifie la liste des agents affectes a un operateur ou viseur
	 */
	@ResponseBody
	@RequestMapping(value = "agentsSaisis", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto setInputAgents(@RequestParam("idAgent") Integer idAgent,
			@RequestParam("idOperateurOrViseur") Integer idOperateurOrViseur,
			@RequestBody List<AgentDto> agentsApprouves, HttpServletResponse response) {

		logger.debug(
				"entered POST [droits/agentsSaisis] => setInputAgents with parameter idAgent = {} and idOperateurOrViseur = {}",
				idAgent, idOperateurOrViseur);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (!accessRightService.canUserAccessAccessRights(convertedIdAgent))
			throw new AccessForbiddenException();

		int convertedIdOperateurOrViseur = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idOperateurOrViseur);

		AgentGeneriqueDto agent = sirhWSConsumer.getAgent(convertedIdOperateurOrViseur);
		if (agent == null || agent.getIdAgent() == null)
			throw new NotFoundException();

		ReturnMessageDto result = accessRightService.setAgentsToInput(convertedIdAgent, convertedIdOperateurOrViseur,
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
}
