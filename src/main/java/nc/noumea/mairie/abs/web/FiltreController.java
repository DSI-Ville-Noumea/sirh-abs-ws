package nc.noumea.mairie.abs.web;

import java.util.List;

import nc.noumea.mairie.abs.dto.AgentDto;
import nc.noumea.mairie.abs.dto.RefEtatDto;
import nc.noumea.mairie.abs.dto.RefTypeAbsenceDto;
import nc.noumea.mairie.abs.dto.RefTypeSaisiDto;
import nc.noumea.mairie.abs.dto.ServiceDto;
import nc.noumea.mairie.abs.service.IAccessRightsService;
import nc.noumea.mairie.abs.service.IAgentMatriculeConverterService;
import nc.noumea.mairie.abs.service.IFiltresService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import flexjson.JSONSerializer;

@Controller
@RequestMapping("/filtres")
public class FiltreController {

	private Logger logger = LoggerFactory.getLogger(FiltreController.class);

	@Autowired
	private IFiltresService filtresService;

	@Autowired
	private IAgentMatriculeConverterService converterService;

	@Autowired
	private IAccessRightsService accessRightsService;

	@ResponseBody
	@RequestMapping(value = "/getEtats", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public ResponseEntity<String> getEtats(@RequestParam(value = "ongletDemande", required = false) String ongletDemande) {

		logger.debug("entered GET [filtres/getEtats] => getEtats");

		List<RefEtatDto> etats = filtresService.getRefEtats(ongletDemande);

		String json = new JSONSerializer().exclude("*.class").serialize(etats);

		return new ResponseEntity<String>(json, HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "/getTypes", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public ResponseEntity<String> getTypes(
			@RequestParam(value = "idAgentConcerne", required = false) Integer idAgentConcerne) {

		logger.debug("entered GET [filtres/getTypes] => getTypes");

		List<RefTypeAbsenceDto> types = filtresService.getRefTypesAbsence(idAgentConcerne);

		String json = new JSONSerializer().exclude("*.class").serialize(types);

		return new ResponseEntity<String>(json, HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "/services", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public ResponseEntity<String> getServices(@RequestParam("idAgent") Integer idAgent) {

		logger.debug("entered GET [filtres/services] => getServices with parameter idAgent = {}", idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		List<ServiceDto> services = accessRightsService.getAgentsServicesToApproveOrInput(convertedIdAgent);

		if (services.size() == 0)
			throw new NoContentException();

		String json = new JSONSerializer().exclude("*.class").serialize(services);

		return new ResponseEntity<String>(json, HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "/agents", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public ResponseEntity<String> getAgents(@RequestParam("idAgent") Integer idAgent,
			@RequestParam(value = "codeService", required = false) String codeService) {

		logger.debug("entered GET [filtres/agents] => getAgents with parameter idAgent = {} and codeService = {}",
				idAgent, codeService);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		List<AgentDto> services = accessRightsService.getAgentsToApproveOrInput(convertedIdAgent, codeService);

		if (services.size() == 0)
			throw new NoContentException();

		String json = new JSONSerializer().exclude("*.class").serialize(services);

		return new ResponseEntity<String>(json, HttpStatus.OK);
	}
	
	@ResponseBody
	@RequestMapping(value = "/getTypesSaisi", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public ResponseEntity<String> getTypesSaisi(
			@RequestParam(value = "idRefTypeAbsence", required = false) Integer idRefTypeAbsence) {

		logger.debug("entered GET [filtres/getTypesSaisi] => getTypesSaisi");

		List<RefTypeSaisiDto> typesSaisi = filtresService.getRefTypeSaisi(idRefTypeAbsence);

		String json = new JSONSerializer().exclude("*.class").serialize(typesSaisi);

		return new ResponseEntity<String>(json, HttpStatus.OK);
	}
}
