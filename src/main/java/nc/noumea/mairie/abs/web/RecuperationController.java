package nc.noumea.mairie.abs.web;

import java.util.Date;

import nc.noumea.mairie.abs.dto.SoldeDto;
import nc.noumea.mairie.abs.service.IAgentMatriculeConverterService;
import nc.noumea.mairie.abs.service.IRecuperationService;
import nc.noumea.mairie.sirh.domain.Agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/recuperations")
public class RecuperationController {

	private Logger logger = LoggerFactory.getLogger(RecuperationController.class);

	@Autowired
	private IRecuperationService recuperationService;

	@Autowired
	private IAgentMatriculeConverterService converterService;

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@Transactional(value = "absTransactionManager")
	public ResponseEntity<String> addRecuperationForAgentAndWeek(@RequestParam("idAgent") Integer idAgent,
			@RequestParam("dateLundi") @DateTimeFormat(pattern = "YYYYMMdd") Date dateMonday,
			@RequestParam("minutes") int minutes) {

		logger.debug(
				"entered GET [recuperations/add] => addRecuperationForAgentAndWeek with parameters idAgent = {}, dateMonday = {} and minutes = {}",
				idAgent, dateMonday, minutes);

		recuperationService.addRecuperationToAgent(idAgent, dateMonday, minutes);

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "soldeRecup", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public ResponseEntity<String> getSoldeRecuperation(@RequestParam(value = "idAgent", required = true) Integer idAgent) {

		logger.debug("entered GET [recuperations/soldeRecup] => getSoldeRecuperation with parameter idAgent = {}",
				idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (Agent.findAgent(convertedIdAgent) == null)
			throw new NotFoundException();

		SoldeDto result = recuperationService.getAgentSoldeRecuperation(convertedIdAgent);

		return new ResponseEntity<String>(result.serializeInJSON(), HttpStatus.OK);
	}
}
