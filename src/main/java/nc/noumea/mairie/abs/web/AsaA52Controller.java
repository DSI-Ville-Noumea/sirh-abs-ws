package nc.noumea.mairie.abs.web;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import nc.noumea.mairie.abs.dto.AgentOrganisationSyndicaleDto;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.OrganisationSyndicaleDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.service.IAgentMatriculeConverterService;
import nc.noumea.mairie.abs.service.ICounterService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/asaA52")
public class AsaA52Controller {

	private Logger logger = LoggerFactory.getLogger(AsaA52Controller.class);

	@Autowired
	@Qualifier("AsaA52CounterServiceImpl")
	private ICounterService counterService;

	@Autowired
	private IAgentMatriculeConverterService converterService;

	/**
	 * Modifie manuellement le compteur ASA A52 d un agent RequestBody : Format
	 * du type timestamp : "/Date(1396306800000+1100)/"
	 */
	@ResponseBody
	@RequestMapping(value = "/addManual", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto addAsaA52ManuelForAgent(@RequestParam("idAgent") int idAgent,
			@RequestBody(required = true) CompteurDto compteurDto, HttpServletResponse response) {

		logger.debug("entered POST [asaA52/addManual] => addAsaA52ManuelForAgent with parameters idAgent = {}", idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		ReturnMessageDto srm = counterService.majManuelleCompteurToAgent(convertedIdAgent, compteurDto);

		if (!srm.getErrors().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_CONFLICT);
		}

		return srm;
	}

	/**
	 * Liste des compteurs ASA A52 ResponseBody : Format du type timestamp :
	 * "/Date(1396306800000+1100)/"
	 */
	@ResponseBody
	@RequestMapping(value = "/listeCompteurA52", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<CompteurDto> getListeCompteur(
			@RequestParam("idOrganisationSyndicale") Integer idOrganisationSyndicale) {

		logger.debug("entered GET [asaA52/listeCompteurA52] => getListeCompteur ");

		List<CompteurDto> result = counterService.getListeCompteur(idOrganisationSyndicale);

		if (result.size() == 0)
			throw new NoContentException();

		return result;
	}

	/**
	 * Liste des compteurs ASA A52 ResponseBody : Format du type timestamp :
	 * "/Date(1396306800000+1100)/"
	 */
	@ResponseBody
	@RequestMapping(value = "/listeRepresentantA52", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<AgentOrganisationSyndicaleDto> listeRepresentantA52(
			@RequestParam("idOrganisationSyndicale") Integer idOrganisationSyndicale) {

		logger.debug("entered GET [asaA52/listeRepresentantA52] => listeRepresentantA52 ");

		List<AgentOrganisationSyndicaleDto> result = counterService.listeRepresentantA52(idOrganisationSyndicale);

		if (result.size() == 0)
			throw new NoContentException();

		return result;
	}

	/**
	 * Modifie les representants de compteur ASA A52 d une organisation
	 * syndicale
	 */
	@ResponseBody
	@RequestMapping(value = "/saveRepresentant", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto saveRepresentantA52(@RequestParam("idOrganisationSyndicale") int idOrganisationSyndicale,
			@RequestBody(required = true) List<AgentOrganisationSyndicaleDto> listeAgentDto,
			HttpServletResponse response) {

		logger.debug(
				"entered POST [asaA52/saveRepresentant] => saveRepresentantA52 with parameters idOrganisationSyndicale = {}",
				idOrganisationSyndicale);

		ReturnMessageDto srm = counterService.saveRepresentantA52(idOrganisationSyndicale, listeAgentDto);

		return srm;
	}

	/**
	 * Liste des OS présents dans le compteur ASA A52 ResponseBody : Format du
	 * type timestamp : "/Date(1396306800000+1100)/"
	 */
	@ResponseBody
	@RequestMapping(value = "/listeOrganisationSyndicaleA52", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<OrganisationSyndicaleDto> listeOrganisationSyndicaleA52() {

		logger.debug("entered GET [asaA52/listeOrganisationSyndicaleA52] => listeOrganisationSyndicaleA52 ");

		List<OrganisationSyndicaleDto> result = counterService.getlisteOrganisationSyndicaleA52();

		if (result.size() == 0)
			throw new NoContentException();

		return result;
	}
}
