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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/droits")
public class AccessRightsController {

	private Logger							logger	= LoggerFactory.getLogger(AccessRightsController.class);

	@Autowired
	private IAccessRightsService			accessRightService;

	@Autowired
	private IAgentMatriculeConverterService	converterService;

	@Autowired
	private ISirhWSConsumer					sirhWSConsumer;

	/**
	 * Retourne les droits d un agent
	 */
	@ResponseBody
	@RequestMapping(value = "listeDroitsAgent", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public AccessRightsDto listAgentAccessRights(@RequestParam("idAgent") Integer idAgent) {

		logger.debug("entered GET [droits/listeDroitsAgent] => listAgentAccessRights with parameter idAgent = {}", idAgent);

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
			@RequestParam(value = "idServiceADS", required = false) Integer idServiceADS) {

		logger.debug("entered GET [droits/approbateurs] => listApprobateurs with parameter idAgent = {} and idServiceADS = {} --> for SIRH ", idAgent,
				idServiceADS);

		return accessRightService.getApprobateurs(idAgent, idServiceADS);
	}

	/**
	 * Cree/modifie les approbateurs
	 */
	@ResponseBody
	@RequestMapping(value = "approbateurs", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto setApprobateur(@RequestBody AgentWithServiceDto agDtos, @RequestParam("idAgentConnecte") Integer idAgentConnecte) {
		logger.debug("entered POST [droits/approbateurs] => setApprobateur --> for SIRH ");

		ReturnMessageDto agentErreur = new ReturnMessageDto();
		try {
			agentErreur = accessRightService.setApprobateur(agDtos, idAgentConnecte);
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
	public ReturnMessageDto deleteApprobateur(@RequestBody AgentWithServiceDto agDtos, @RequestParam("idAgentConnecte") Integer idAgentConnecte) {
		logger.debug("entered POST [droits/deleteApprobateurs] => deleteApprobateur --> for SIRH ");

		ReturnMessageDto agentErreur = new ReturnMessageDto();
		try {
			agentErreur = accessRightService.deleteApprobateur(agDtos, idAgentConnecte);
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
	public ReturnMessageDto setInputter(@RequestParam("idAgentConnecte") Integer idAgentConnecte, @RequestParam("idAgent") Integer idAgent,
			@RequestBody InputterDto inputterDto, HttpServletResponse response) {

		logger.debug("entered POST [droits/inputter] => setInputter with parameter idAgent = {}", idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (!accessRightService.canUserAccessAccessRights(convertedIdAgent))
			throw new AccessForbiddenException();

		ReturnMessageDto result = accessRightService.setInputter(convertedIdAgent, inputterDto, idAgentConnecte);

		if (result.getErrors().size() != 0)
			response.setStatus(HttpServletResponse.SC_CONFLICT);

		return result;
	}

	/**
	 * #15713 special pour SIRH : one shot Saisie/modification d un operateurs d
	 * un approbateur WS specifique a SIRH
	 */
	@ResponseBody
	@RequestMapping(value = "operateurSIRH", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto setOperateur(@RequestParam("idAgentConnecte") Integer idAgentConnecte, @RequestParam("idAgent") Integer idAgent,
			@RequestBody AgentDto agentDto, HttpServletResponse response) {

		logger.debug("entered POST [droits/operateur] => setOperateur with parameter idAgent = {}", idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (!accessRightService.canUserAccessAccessRights(convertedIdAgent))
			throw new AccessForbiddenException();

		ReturnMessageDto result = accessRightService.setOperateur(convertedIdAgent, agentDto, idAgentConnecte);

		if (result.getErrors().size() != 0)
			response.setStatus(HttpServletResponse.SC_CONFLICT);

		return result;
	}

	/**
	 * #15713 special pour SIRH : one shot Saisie/modification d un operateurs d
	 * un approbateur WS specifique a SIRH
	 */
	@ResponseBody
	@RequestMapping(value = "deleteOperateurSIRH", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto deleteOperateur(@RequestParam("idAgentConnecte") Integer idAgentConnecte, @RequestParam("idAgent") Integer idAgent,
			@RequestBody AgentDto agentDto, HttpServletResponse response) {

		logger.debug("entered DELETE [droits/operateur] => deleteOperateur with parameter idAgent = {}", idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (!accessRightService.canUserAccessAccessRights(convertedIdAgent))
			throw new AccessForbiddenException();

		ReturnMessageDto result = accessRightService.deleteOperateur(convertedIdAgent, agentDto, idAgentConnecte);

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
	public ReturnMessageDto setViseurs(@RequestParam("idAgentConnecte") Integer idAgentConnecte, @RequestParam("idAgent") Integer idAgent,
			@RequestBody ViseursDto viseursDto, HttpServletResponse response) {

		logger.debug("entered POST [droits/viseur] => setViseurs with parameter idAgent = {}", idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (!accessRightService.canUserAccessAccessRights(convertedIdAgent))
			throw new AccessForbiddenException();

		ReturnMessageDto result = accessRightService.setViseurs(convertedIdAgent, viseursDto, idAgentConnecte);

		if (result.getErrors().size() != 0)
			response.setStatus(HttpServletResponse.SC_CONFLICT);

		return result;
	}

	/**
	 * #15713 special pour SIRH : one shot Saisie/modification d un operateurs d
	 * un approbateur WS specifique a SIRH
	 */
	@ResponseBody
	@RequestMapping(value = "viseurSIRH", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto setViseur(@RequestParam("idAgentConnecte") Integer idAgentConnecte, @RequestParam("idAgent") Integer idAgent,
			@RequestBody AgentDto agentDto, HttpServletResponse response) {

		logger.debug("entered POST [droits/viseurSIRH] => setViseur with parameter idAgent = {}", idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (!accessRightService.canUserAccessAccessRights(convertedIdAgent))
			throw new AccessForbiddenException();

		ReturnMessageDto result = accessRightService.setViseur(convertedIdAgent, agentDto, idAgentConnecte);

		if (result.getErrors().size() != 0)
			response.setStatus(HttpServletResponse.SC_CONFLICT);

		return result;
	}

	/**
	 * #15713 special pour SIRH : one shot Saisie/modification d un operateurs d
	 * un approbateur WS specifique a SIRH
	 */
	@ResponseBody
	@RequestMapping(value = "deleteViseurSIRH", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto deleteViseur(@RequestParam("idAgentConnecte") Integer idAgentConnecte, @RequestParam("idAgent") Integer idAgent,
			@RequestBody AgentDto agentDto, HttpServletResponse response) {

		logger.debug("entered DELETE [droits/viseur] => deleteViseur with parameter idAgent = {}", idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (!accessRightService.canUserAccessAccessRights(convertedIdAgent))
			throw new AccessForbiddenException();

		ReturnMessageDto result = accessRightService.deleteViseur(convertedIdAgent, agentDto, idAgentConnecte);

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

		List<AgentDto> result = accessRightService.getAgentsToApproveOrInputByAgent(convertedIdAgent, convertedIdAgent, null);

		if (result.size() == 0)
			throw new NoContentException();

		return result;
	}

	/**
	 * Saisie/modifie la liste des agents que l approbateur doit approuver
	 */
	@ResponseBody
	@RequestMapping(value = "agentsApprouves", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto setApprovedAgents(@RequestParam("idAgentConnecte") Integer idAgentConnecte, @RequestParam("idAgent") Integer idAgent,
			@RequestBody List<AgentDto> agDtos, HttpServletResponse response) {

		logger.debug("entered POST [droits/agentsApprouves] => setApprovedAgents with parameter idAgent = {}", idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (!accessRightService.canUserAccessAccessRights(convertedIdAgent))
			throw new AccessForbiddenException();

		ReturnMessageDto result = accessRightService.setAgentsToApprove(convertedIdAgent, agDtos, idAgentConnecte);

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

		logger.debug("entered GET [droits/agentsSaisisByOperateur] => getInputAgentsByOperateur with parameter idAgent = {} and idOperateur = {} ",
				idAgent, idOperateur);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (!accessRightService.canUserAccessAccessRights(convertedIdAgent))
			throw new AccessForbiddenException();

		int convertedIdOperateur = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idOperateur);

		List<AgentDto> result = accessRightService.getAgentsToInputByOperateur(convertedIdAgent, convertedIdOperateur, null);

		if (result.size() == 0)
			throw new NoContentException();

		return result;
	}

	/**
	 * Retourne la liste des agents affectes a un viseur
	 */
	@ResponseBody
	@RequestMapping(value = "agentsSaisisByViseur", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<AgentDto> getInputAgentsByViseur(@RequestParam("idAgent") Integer idAgent, @RequestParam(value = "idViseur") Integer idViseur) {

		logger.debug("entered GET [droits/agentsSaisisByViseur] => getInputAgentsByViseur with parameter idAgent = {} and idViseur = {} ", idAgent,
				idViseur);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (!accessRightService.canUserAccessAccessRights(convertedIdAgent))
			throw new AccessForbiddenException();

		int convertedIdViseur = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idViseur);

		List<AgentDto> result = accessRightService.getAgentsToInputByViseur(convertedIdAgent, convertedIdViseur, null);

		if (result.size() == 0)
			throw new NoContentException();

		return result;
	}

	/**
	 * Saisie/modifie la liste des agents affectes a un operateur
	 */
	@ResponseBody
	@RequestMapping(value = "agentsSaisisByOperateur", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto setInputAgentsByOperateur(@RequestParam("idAgentConnecte") Integer idAgentConnecte,
			@RequestParam("idAgent") Integer idAgent, @RequestParam("idOperateur") Integer idOperateur, @RequestBody List<AgentDto> agentsApprouves,
			HttpServletResponse response) {

		logger.debug("entered POST [droits/agentsSaisisByOperateur] => setInputAgentsByOperateur with parameter idAgent = {} and idOperateur = {}",
				idAgent, idOperateur);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (!accessRightService.canUserAccessAccessRights(convertedIdAgent))
			throw new AccessForbiddenException();

		int convertedIdOperateur = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idOperateur);

		AgentGeneriqueDto agent = sirhWSConsumer.getAgent(convertedIdOperateur);
		if (agent == null || agent.getIdAgent() == null)
			throw new NotFoundException();

		ReturnMessageDto result = accessRightService.setAgentsToInputByOperateur(convertedIdAgent, convertedIdOperateur, agentsApprouves,
				idAgentConnecte);

		if (result.getErrors().size() != 0)
			response.setStatus(HttpServletResponse.SC_CONFLICT);

		return result;
	}

	/**
	 * Saisie/modifie la liste des agents affectes a un viseur
	 */
	@ResponseBody
	@RequestMapping(value = "agentsSaisisByViseur", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto setInputAgentsByViseur(@RequestParam("idAgentConnecte") Integer idAgentConnecte, @RequestParam("idAgent") Integer idAgent,
			@RequestParam("idViseur") Integer idViseur, @RequestBody List<AgentDto> agentsApprouves, HttpServletResponse response) {

		logger.debug("entered POST [droits/agentsSaisisByViseur] => setInputAgentsByViseur with parameter idAgent = {} and idViseur = {}", idAgent,
				idViseur);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (!accessRightService.canUserAccessAccessRights(convertedIdAgent))
			throw new AccessForbiddenException();

		int convertedIdViseur = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idViseur);

		AgentGeneriqueDto agent = sirhWSConsumer.getAgent(convertedIdViseur);
		if (agent == null || agent.getIdAgent() == null)
			throw new NotFoundException();

		ReturnMessageDto result = accessRightService.setAgentsToInputByViseur(convertedIdAgent, convertedIdViseur, agentsApprouves, idAgentConnecte);

		if (result.getErrors().size() != 0)
			response.setStatus(HttpServletResponse.SC_CONFLICT);

		return result;
	}

	/**
	 * Saisie/modification du delegataire d un approbateur --> UTILE à SIRH
	 */
	@ResponseBody
	@RequestMapping(value = "delegataire", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto setDelegataire(@RequestParam("idAgentConnecte") Integer idAgentConnecte, @RequestParam("idAgent") Integer idAgent,
			@RequestBody InputterDto inputterDto, HttpServletResponse response) {

		logger.debug("entered POST [droits/delegataire] => setDelegataire with parameter idAgent = {}", idAgent);
		ReturnMessageDto result = new ReturnMessageDto();

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (!accessRightService.canUserAccessAccessRights(convertedIdAgent)) {
			result.getErrors().add(String.format("L'agent [%s] n'est pas approbateur.", convertedIdAgent));
			return result;
		}

		result = accessRightService.setDelegataire(convertedIdAgent, inputterDto, result, idAgentConnecte);

		return result;
	}

	/**
	 * Liste des acteurs (opérateur(s), viseur(s), approbateur(s) (+
	 * delegataire) d un agent
	 */
	@ResponseBody
	@RequestMapping(value = "listeActeurs", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public ActeursDto getListeActeurs(@RequestParam("idAgent") Integer idAgent) {

		logger.debug("entered POST [droits/listeActeurs] => getListeActeurs with parameter idAgent = {}", idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		return accessRightService.getListeActeurs(convertedIdAgent);
	}

	@ResponseBody
	@RequestMapping(value = "isUserApprobateur", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public ResponseEntity<String> isUserApprobateur(@RequestParam("idAgent") Integer idAgent) {

		logger.debug("entered GET [droits/isUserApprobateur] => isUserApprobateur with parameter idAgent = {}", idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (accessRightService.findAgent(convertedIdAgent) == null)
			throw new NotFoundException();

		if (accessRightService.isUserApprobateur(convertedIdAgent))
			return new ResponseEntity<String>(HttpStatus.OK);
		else
			return new ResponseEntity<String>(HttpStatus.CONFLICT);
	}

	@ResponseBody
	@RequestMapping(value = "isUserOperateur", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public ResponseEntity<String> isUserOperateur(@RequestParam("idAgent") Integer idAgent) {

		logger.debug("entered GET [droits/isUserOperateur] => isUserOperateur with parameter idAgent = {}", idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (accessRightService.findAgent(convertedIdAgent) == null)
			throw new NotFoundException();

		if (accessRightService.isUserOperateur(convertedIdAgent))
			return new ResponseEntity<String>(HttpStatus.OK);
		else
			return new ResponseEntity<String>(HttpStatus.CONFLICT);
	}

	@ResponseBody
	@RequestMapping(value = "isUserViseur", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public ResponseEntity<String> isUserViseur(@RequestParam("idAgent") Integer idAgent) {

		logger.debug("entered GET [droits/isUserViseur] => isUserViseur with parameter idAgent = {}", idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (accessRightService.findAgent(convertedIdAgent) == null)
			throw new NotFoundException();

		if (accessRightService.isUserViseur(convertedIdAgent))
			return new ResponseEntity<String>(HttpStatus.OK);
		else
			return new ResponseEntity<String>(HttpStatus.CONFLICT);
	}

	/**
	 * duplique un approbateur vers un nouvel approbateur --> UTILE à SIRH
	 */
	@ResponseBody
	@RequestMapping(value = "dupliqueDroitsApprobateur", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto dupliqueDroitsApprobateur(@RequestParam("idAgentConnecte") Integer idAgentConnecte,
			@RequestParam("idAgentSource") Integer idAgentSource, @RequestParam("idAgentDest") Integer idAgentDest, HttpServletResponse response) {

		logger.debug(
				"entered POST [droits/dupliqueDroitsApprobateur] => dupliqueDroitsApprobateur with parameter idAgentConnecte = {} and idAgentSource = {} and idAgentDest = {}",
				idAgentConnecte, idAgentSource, idAgentDest);

		ReturnMessageDto result = new ReturnMessageDto();

		// on verifie que l agent ait les droits
		int convertedIdAgentConnecte = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgentConnecte);
		result = sirhWSConsumer.isUtilisateurSIRH(convertedIdAgentConnecte);
		if (!result.getErrors().isEmpty())
			throw new AccessForbiddenException();

		int convertedIdAgentSource = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgentSource);
		int convertedIdAgentDest = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgentDest);

		result = accessRightService.dupliqueDroitsApprobateur(convertedIdAgentSource, convertedIdAgentDest, idAgentConnecte);

		return result;
	}

}
