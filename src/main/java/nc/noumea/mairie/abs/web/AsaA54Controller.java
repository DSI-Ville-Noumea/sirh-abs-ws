package nc.noumea.mairie.abs.web;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import nc.noumea.mairie.abs.dto.CompteurDto;
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
@RequestMapping("/asaA54")
public class AsaA54Controller {

	private Logger logger = LoggerFactory.getLogger(AsaA54Controller.class);

	@Autowired
	@Qualifier("AsaA54CounterServiceImpl")
	private ICounterService counterService;

	@Autowired
	private IAgentMatriculeConverterService converterService;

	/**
	 * Modifie manuellement le compteur ASA A54 d un agent RequestBody : Format
	 * du type timestamp : "/Date(1396306800000+1100)/"
	 */
	@ResponseBody
	@RequestMapping(value = "/addManual", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto addAsaA54ManuelForAgent(@RequestParam("idAgent") int idAgent,
			@RequestBody(required = true) CompteurDto compteurDto, HttpServletResponse response) {

		logger.debug("entered POST [asaA54/addManual] => addAsaA54ManuelForAgent with parameters idAgent = {}", idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		ReturnMessageDto srm = counterService.majManuelleCompteurToAgent(convertedIdAgent, compteurDto);

		if (!srm.getErrors().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_CONFLICT);
		}

		return srm;
	}

	/**
	 * Liste des compteurs ASA A54 ResponseBody : Format du type timestamp :
	 * "/Date(1396306800000+1100)/"
	 */
	@ResponseBody
	@RequestMapping(value = "/listeCompteurA54", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<CompteurDto> getListeCompteur() {

		logger.debug("entered GET [asaA54/listeCompteurA54] => getListeCompteur ");

		List<CompteurDto> result = counterService.getListeCompteur();

		if (result.size() == 0)
			throw new NoContentException();

		return result;
	}
}
