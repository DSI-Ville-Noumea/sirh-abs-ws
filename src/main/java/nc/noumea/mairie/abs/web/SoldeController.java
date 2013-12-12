package nc.noumea.mairie.abs.web;

import nc.noumea.mairie.abs.dto.SoldeDto;
import nc.noumea.mairie.abs.service.IAgentMatriculeConverterService;
import nc.noumea.mairie.abs.service.IRecuperationService;
import nc.noumea.mairie.abs.service.ISoldeService;
import nc.noumea.mairie.sirh.domain.Agent;

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

@Controller
@RequestMapping("/solde")
public class SoldeController {

	private Logger logger = LoggerFactory.getLogger(SoldeController.class);

	@Autowired
	private IRecuperationService recuperationService;

	@Autowired
	private IAgentMatriculeConverterService converterService;

	@Autowired
	private ISoldeService soldeSrv;

	@ResponseBody
	@RequestMapping(value = "soldeRecup", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public ResponseEntity<String> getSoldeRecuperation(@RequestParam(value = "idAgent", required = true) Integer idAgent) {

		logger.debug("entered GET [solde/soldeRecup] => getSoldeRecuperation with parameter idAgent = {}", idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (Agent.findAgent(convertedIdAgent) == null)
			throw new NotFoundException();

		SoldeDto result = recuperationService.getAgentSoldeRecuperation(convertedIdAgent);

		return new ResponseEntity<String>(result.serializeInJSON(), HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "soldeCongeAnnee", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public ResponseEntity<String> getSoldeCongeAnnee(@RequestParam(value = "idAgent", required = true) Integer idAgent) {

		logger.debug("entered GET [solde/soldeCongeAnnee] => getSoldeCongeAnnee with parameter idAgent = {}", idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (Agent.findAgent(convertedIdAgent) == null)
			throw new NotFoundException();

		SoldeDto result = soldeSrv.getAgentSoldeCongeAnnee(convertedIdAgent);

		return new ResponseEntity<String>(result.serializeInJSON(), HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "soldeCongeAnneePrec", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public ResponseEntity<String> getSoldeCongeAnneePrec(
			@RequestParam(value = "idAgent", required = true) Integer idAgent) {

		logger.debug("entered GET [solde/soldeCongeAnneePrec] => getSoldeCongeAnneePrec with parameter idAgent = {}",
				idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (Agent.findAgent(convertedIdAgent) == null)
			throw new NotFoundException();

		SoldeDto result = soldeSrv.getAgentSoldeCongeAnneePrec(convertedIdAgent);

		return new ResponseEntity<String>(result.serializeInJSON(), HttpStatus.OK);
	}
}
