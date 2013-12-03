package nc.noumea.mairie.abs.web;

import java.util.ArrayList;
import java.util.List;

import nc.noumea.mairie.abs.dto.AccessRightsDto;
import nc.noumea.mairie.abs.dto.AgentDto;
import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.abs.dto.InputterDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.service.IAccessRightsService;
import nc.noumea.mairie.abs.service.IAgentMatriculeConverterService;
import nc.noumea.mairie.sirh.domain.Agent;

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

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

@Controller
@RequestMapping("/droits")
public class AccessRightsController {

	private Logger logger = LoggerFactory.getLogger(AccessRightsController.class);

	@Autowired
	private IAccessRightsService accessRightService;

	@Autowired
	private IAgentMatriculeConverterService converterService;

	@ResponseBody
	@RequestMapping(value = "listeDroitsAgent", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public ResponseEntity<String> listAgentAccessRights(@RequestParam("idAgent") Integer idAgent) {

		logger.debug("entered GET [droits/listeDroitsAgent] => listAgentAccessRights with parameter idAgent = {}",
				idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (Agent.findAgent(convertedIdAgent) == null)
			throw new NotFoundException();

		AccessRightsDto result = accessRightService.getAgentAccessRights(convertedIdAgent);

		return new ResponseEntity<String>(result.serializeInJSON(), HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "approbateurs", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public ResponseEntity<String> listApprobateurs() {

		logger.debug("entered GET [droits/approbateurs] => listApprobateurs with no parameter --> for SIRH ");

		List<AgentWithServiceDto> result = accessRightService.getApprobateurs();
		return new ResponseEntity<String>(new JSONSerializer().exclude("*.class").serialize(result), HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "approbateurs", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	@Transactional(value = "absTransactionManager")
	public ResponseEntity<String> setApprobateur(@RequestBody String agentsDtoJson) {
		logger.debug("entered POST [droits/approbateurs] => setApprobateur --> for SIRH ");

		List<AgentWithServiceDto> agDtos = new JSONDeserializer<List<AgentWithServiceDto>>().use(null, ArrayList.class)
				.use("values", AgentWithServiceDto.class).deserialize(agentsDtoJson);

		List<AgentWithServiceDto> agentErreur = new ArrayList<AgentWithServiceDto>();
		try {
			agentErreur = accessRightService.setApprobateurs(agDtos);
		} catch (Exception e) {
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.CONFLICT);
		}

		return new ResponseEntity<String>(new JSONSerializer().exclude("*.class").serialize(agentErreur), HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "inputter", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public ResponseEntity<String> getInputter(@RequestParam("idAgent") Integer idAgent) {
		logger.debug("entered GET [droits/inputter] => getInputter with parameter idAgent = {}", idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (!accessRightService.canUserAccessAccessRights(convertedIdAgent))
			throw new AccessForbiddenException();

		InputterDto result = accessRightService.getInputter(convertedIdAgent);

		return new ResponseEntity<String>(result.serializeInJSON(), HttpStatus.OK);
	}

	
	@ResponseBody
	@RequestMapping(value = "inputter", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	@Transactional(value = "absTransactionManager") 
	public ResponseEntity<String> setInputter(@RequestParam("idAgent") Integer idAgent, @RequestBody String inputterDtoJson) { 
	 
		logger.debug("entered POST [droits/inputter] => setInputter with parameter idAgent = {}", idAgent);
		
		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);
		
		if (!accessRightService.canUserAccessAccessRights(convertedIdAgent))
			throw new AccessForbiddenException();
		
		ReturnMessageDto result = accessRightService.setInputter(convertedIdAgent, new InputterDto().deserializeFromJSON(inputterDtoJson));
			  
		String jsonResult = new JSONSerializer().exclude("*.class").deepSerialize(result);
		 
		if (result.getErrors().size() != 0) 
			return new ResponseEntity<String>(jsonResult, HttpStatus.CONFLICT); 
		else 
			return new ResponseEntity<String>(jsonResult, HttpStatus.OK); 
	}


	@ResponseBody
	@RequestMapping(value = "agentsApprouves", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public ResponseEntity<String> getApprovedAgents(@RequestParam("idAgent") Integer idAgent) {

		logger.debug("entered GET [droits/agentsApprouves] => getApprovedAgents with parameter idAgent = {}", idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (!accessRightService.canUserAccessAccessRights(convertedIdAgent))
			throw new AccessForbiddenException();

		List<AgentDto> result = accessRightService.getAgentsToApproveOrInput(convertedIdAgent, convertedIdAgent);

		if (result.size() == 0)
			return new ResponseEntity<String>(HttpStatus.NO_CONTENT);

		String response = new JSONSerializer().exclude("*.class").serialize(result);

		return new ResponseEntity<String>(response, HttpStatus.OK);
	}
	
	@ResponseBody
	@RequestMapping(value = "agentsSaisis", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public ResponseEntity<String> getInputAgents(
			@RequestParam("idAgent") Integer idAgent,
			@RequestParam(value="idOperateurOrViseur") Integer idOperateurOrViseur) {

		logger.debug("entered GET [droits/agentsSaisis] => getInputAgents with parameter idAgent = {} and idOperateurOrViseur = {} ", idAgent, idOperateurOrViseur);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (!accessRightService.canUserAccessAccessRights(convertedIdAgent))
			throw new AccessForbiddenException();

		int convertedIdOperateurOrViseur = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idOperateurOrViseur);
		
		List<AgentDto> result = accessRightService.getAgentsToApproveOrInput(convertedIdAgent, convertedIdOperateurOrViseur);

		if (result.size() == 0)
			return new ResponseEntity<String>(HttpStatus.NO_CONTENT);

		String response = new JSONSerializer().exclude("*.class").serialize(result);

		return new ResponseEntity<String>(response, HttpStatus.OK);
	}
	
	@ResponseBody
	@RequestMapping(value = "agentsSaisis", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	@Transactional(value = "absTransactionManager")
	public ResponseEntity<String> setInputAgents(@RequestParam("idAgent") Integer idAgent, 
			@RequestParam("idOperateurOrViseur") Integer idOperateurOrViseur,
			@RequestBody String agentsApprouvesJson) {

		logger.debug("entered POST [droits/agentsSaisis] => setInputAgents with parameter idAgent = {} and idOperateurOrViseur = {}", idAgent, idOperateurOrViseur);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (!accessRightService.canUserAccessAccessRights(convertedIdAgent))
			throw new AccessForbiddenException();

		int convertedIdOperateurOrViseur = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idOperateurOrViseur);

		if (Agent.findAgent(convertedIdOperateurOrViseur) == null)
			throw new NotFoundException();

		List<AgentDto> agDtos = new JSONDeserializer<List<AgentDto>>().use(null, ArrayList.class).use("values", AgentDto.class)
				.deserialize(agentsApprouvesJson);

		accessRightService.setAgentsToInput(convertedIdAgent, convertedIdOperateurOrViseur, agDtos);

		return new ResponseEntity<String>(HttpStatus.OK);
	}
}
