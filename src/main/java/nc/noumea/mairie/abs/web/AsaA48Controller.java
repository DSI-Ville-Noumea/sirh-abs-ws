package nc.noumea.mairie.abs.web;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

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

import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.service.IAgentMatriculeConverterService;
import nc.noumea.mairie.abs.service.ICounterService;

@Controller
@RequestMapping("/asaA48")
public class AsaA48Controller {

	private Logger							logger	= LoggerFactory.getLogger(AsaA48Controller.class);

	@Autowired
	@Qualifier("AsaA48CounterServiceImpl")
	private ICounterService					counterService;

	@Autowired
	private IAgentMatriculeConverterService	converterService;

	/**
	 * Modifie manuellement le compteur ASA A48 d un agent RequestBody : Format
	 * du type timestamp : "/Date(1396306800000+1100)/"
	 */
	@ResponseBody
	@RequestMapping(value = "/addManual", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto addAsaA48ManuelForAgent(@RequestParam("idAgent") int idAgent, @RequestBody(required = true) CompteurDto compteurDto,
			HttpServletResponse response) {

		logger.debug("entered POST [asaA48/addManual] => addAsaA48ManuelForAgent with parameters idAgent = {}", idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		ReturnMessageDto srm = counterService.majManuelleCompteurToAgent(convertedIdAgent, compteurDto, false);

		if (!srm.getErrors().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_CONFLICT);
		}

		return srm;
	}

	/**
	 * Liste des compteurs ASA A48 ResponseBody : Format du type timestamp :
	 * "/Date(1396306800000+1100)/"
	 */
	@ResponseBody
	@RequestMapping(value = "/listeCompteurA48", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<CompteurDto> getListeCompteur(@RequestParam(value = "annee", required = false) Integer annee,
			@RequestParam(value = "idOrganisation", required = false) Integer idOrganisation,
			@RequestParam(value = "pageSize", required = false) Integer pageSize,
			@RequestParam(value = "pageNumber", required = false) Integer pageNumber, 
			@RequestParam(value = "idAgentRecherche", required = false) Integer idAgentRecherche ) {

		logger.debug("entered GET [asaA48/listeCompteurA48] => getListeCompteur ");

		List<CompteurDto> result = counterService.getListeCompteur(idOrganisation, annee, pageSize, pageNumber,idAgentRecherche);

		if (result.size() == 0)
			throw new NoContentException();

		return result;
	}
	
	@ResponseBody
	@RequestMapping(value = "/countAllByYear", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public Integer countAllByYear(@RequestParam(value = "year", required = false) Integer annee, 
			@RequestParam(value = "idOS", required = false) Integer idOS, 
			@RequestParam(value = "idAgentRecherche", required = false) Integer idAgentRecherche, HttpServletResponse response) {

		logger.debug("entered GET [asaA48/countAll]");

		return counterService.countAllByYear(annee, idOS, idAgentRecherche, null, null);
	}

	/**
	 * Modifie manuellement le compteur ASA A54 d un agent RequestBody : Format
	 * du type timestamp : "/Date(1396306800000+1100)/" Sert à SIRH pour
	 * dupliquer en boucle
	 */
	@ResponseBody
	@RequestMapping(value = "/addManualByList", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto addAsaA48ManuelForListAgent(@RequestParam("idAgent") int idAgent,
			@RequestBody(required = true) List<CompteurDto> listeCompteurDto, HttpServletResponse response) {

		logger.debug("entered POST [asaA48/addManualByList] => addAsaA48ManuelForListAgent with parameters idAgent = {}", idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		ReturnMessageDto srm = counterService.majManuelleCompteurToListAgent(convertedIdAgent, listeCompteurDto, true);

		if (!srm.getErrors().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_CONFLICT);
		}

		return srm;
	}

	/**
	 * Modifie les representants ASA A48 d une organisation syndicale
	 */
	@ResponseBody
	@RequestMapping(value = "/saveRepresentant", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public ReturnMessageDto saveRepresentantA48(@RequestParam("idOrganisationSyndicale") int idOrganisationSyndicale,
			@RequestParam("idAgent") int idAgent) {

		logger.debug("entered GET [asaA48/saveRepresentant] => saveRepresentantA48 with parameters idOrganisationSyndicale = {},idAgent = {}",
				idOrganisationSyndicale, idAgent);

		ReturnMessageDto srm = counterService.saveRepresentantA48(idOrganisationSyndicale, idAgent);

		return srm;
	}
}
