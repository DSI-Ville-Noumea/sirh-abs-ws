package nc.noumea.mairie.abs.web;

import javax.servlet.http.HttpServletResponse;

import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.service.IAgentMatriculeConverterService;
import nc.noumea.mairie.abs.service.ICounterService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/congeannuel")
public class CongeAnnuelController {

	private Logger logger = LoggerFactory.getLogger(CongeAnnuelController.class);

	@Autowired
	@Qualifier("CongeAnnuelCounterServiceImpl")
	private ICounterService counterService;

	@Autowired
	private IAgentMatriculeConverterService converterService;

	/**
	 * Initialise le compteur de congé annuel si l'agent n'en avait pas. <br />
	 * utile a SIRH lors de la creation d'une toute premiere affectation d'un
	 * agent
	 */
	@ResponseBody
	@RequestMapping(value = "/intitCompteurCongeAnnuel", method = RequestMethod.POST)
	public ReturnMessageDto intitCompteurCongeAnnuel(@RequestParam(value = "idAgent", required = true) Integer idAgent,
			@RequestParam(value = "idAgentConcerne", required = true) Integer idAgentConcerne,
			HttpServletResponse response) {

		logger.debug(
				"entered GET [congeannuel/intitCompteurCongeAnnuel] => intitCompteurCongeAnnuel with parameters idAgent = {}, idAgentConcerne = {}",
				idAgent, idAgentConcerne);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);
		int convertedIdAgentConcerne = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgentConcerne);

		ReturnMessageDto srm = counterService.intitCompteurCongeAnnuel(convertedIdAgent, convertedIdAgentConcerne);

		if (!srm.getErrors().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_CONFLICT);
		}

		return srm;
	}
}
