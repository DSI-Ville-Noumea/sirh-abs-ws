package nc.noumea.mairie.abs.web;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import nc.noumea.mairie.abs.dto.CompteurAsaDto;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.service.IAgentMatriculeConverterService;
import nc.noumea.mairie.abs.service.ICounterService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/asaA48")
public class AsaA48Controller {

	private Logger logger = LoggerFactory.getLogger(AsaA48Controller.class);

	@Autowired
	@Qualifier("AsaA48CounterServiceImpl")
	private ICounterService counterService;

	@Autowired
	private IAgentMatriculeConverterService converterService;

	/**
	 * Modifie manuellement le compteur ASA A48 d un agent
	 * RequestBody : Format du type timestamp : "/Date(1396306800000+1100)/"
	 */
	@ResponseBody
	@RequestMapping(value = "/addManual", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	@Transactional(value = "absTransactionManager")
	public ReturnMessageDto addAsaA48ManuelForAgent(@RequestParam("idAgent") int idAgent,
			@RequestBody(required = true) CompteurDto compteurDto, 
			HttpServletResponse response) {

		logger.debug("entered POST [asaA48/addManual] => addAsaA48ManuelForAgent with parameters idAgent = {}", idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		ReturnMessageDto srm = counterService.majManuelleCompteurToAgent(convertedIdAgent, compteurDto);

		if (!srm.getErrors().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_CONFLICT);
		}

		return srm;
	}

	/**
	 * Liste des compteurs ASA A48
	 * ResponseBody : Format du type timestamp : "/Date(1396306800000+1100)/"
	 */
	@ResponseBody
	@RequestMapping(value = "/listeCompteurA48", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public List<CompteurAsaDto> getListeCompteur() {

		logger.debug("entered GET [asaA48/listeCompteurA48] => getListeCompteur ");

		List<CompteurAsaDto> result = counterService.getListeCompteur();

		if (result.size() == 0)
			throw new NoContentException();

		return result;
	}
}
