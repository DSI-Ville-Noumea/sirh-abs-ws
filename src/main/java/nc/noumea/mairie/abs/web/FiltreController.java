package nc.noumea.mairie.abs.web;

import java.util.List;

import nc.noumea.mairie.abs.dto.AgentDto;
import nc.noumea.mairie.abs.dto.RefEtatDto;
import nc.noumea.mairie.abs.dto.RefTypeAbsenceDto;
import nc.noumea.mairie.abs.dto.RefTypeSaisiDto;
import nc.noumea.mairie.abs.dto.ServiceDto;
import nc.noumea.mairie.abs.service.IAccessRightsService;
import nc.noumea.mairie.abs.service.IAgentMatriculeConverterService;
import nc.noumea.mairie.abs.service.IFiltreService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/filtres")
public class FiltreController {

	private Logger logger = LoggerFactory.getLogger(FiltreController.class);

	@Autowired
	private IFiltreService filtresService;

	@Autowired
	private IAgentMatriculeConverterService converterService;

	@Autowired
	private IAccessRightsService accessRightsService;

	/**
	 * Liste des etats possibles selon l onglet selectionne
	 */
	@ResponseBody
	@RequestMapping(value = "/getEtats", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public List<RefEtatDto> getEtats(@RequestParam(value = "ongletDemande", required = false) String ongletDemande) {

		logger.debug("entered GET [filtres/getEtats] => getEtats");

		List<RefEtatDto> etats = filtresService.getRefEtats(ongletDemande);

		return etats;
	}

	/**
	 * Liste des types d absence possibles pour un agent donne
	 */
	@ResponseBody
	@RequestMapping(value = "/getTypes", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public List<RefTypeAbsenceDto> getTypes(
			@RequestParam(value = "idAgentConcerne", required = false) Integer idAgentConcerne) {

		logger.debug("entered GET [filtres/getTypes] => getTypes");

		List<RefTypeAbsenceDto> types = filtresService.getRefTypesAbsence(idAgentConcerne);

		return types;
	}

	/**
	 * Liste des services pour un agent donne
	 */
	@ResponseBody
	@RequestMapping(value = "/services", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public List<ServiceDto> getServices(@RequestParam("idAgent") Integer idAgent) {

		logger.debug("entered GET [filtres/services] => getServices with parameter idAgent = {}", idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		List<ServiceDto> services = accessRightsService.getAgentsServicesToApproveOrInput(convertedIdAgent);

		if (services.size() == 0)
			throw new NoContentException();

		return services;
	}

	/**
	 * Liste des agents affectes a un operateur, viseur ou approbateur selon son service
	 */
	@ResponseBody
	@RequestMapping(value = "/agents", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public List<AgentDto> getAgents(@RequestParam("idAgent") Integer idAgent,
			@RequestParam(value = "codeService", required = false) String codeService) {

		logger.debug("entered GET [filtres/agents] => getAgents with parameter idAgent = {} and codeService = {}",
				idAgent, codeService);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		List<AgentDto> services = accessRightsService.getAgentsToApproveOrInput(convertedIdAgent, codeService);

		if (services.size() == 0)
			throw new NoContentException();

		return services;
	}
	
	/**
	 * Retourne les types de saisie (champs de saisie, checkbox, etc) d une absence donnee
	 */
	@ResponseBody
	@RequestMapping(value = "/getTypesSaisi", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public List<RefTypeSaisiDto> getTypesSaisi(
			@RequestParam(value = "idRefTypeAbsence", required = false) Integer idRefTypeAbsence) {

		logger.debug("entered GET [filtres/getTypesSaisi] => getTypesSaisi");

		List<RefTypeSaisiDto> typesSaisi = filtresService.getRefTypeSaisi(idRefTypeAbsence);

		return typesSaisi;
	}
}
