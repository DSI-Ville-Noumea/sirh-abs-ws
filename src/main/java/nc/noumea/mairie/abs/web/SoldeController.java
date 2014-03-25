package nc.noumea.mairie.abs.web;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.dto.HistoriqueSoldeDto;
import nc.noumea.mairie.abs.dto.SoldeDto;
import nc.noumea.mairie.abs.service.IAgentMatriculeConverterService;
import nc.noumea.mairie.abs.service.ISoldeService;
import nc.noumea.mairie.abs.transformer.MSDateTransformer;
import nc.noumea.mairie.sirh.service.ISirhService;

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
@RequestMapping("/solde")
public class SoldeController {

	private Logger logger = LoggerFactory.getLogger(SoldeController.class);

	@Autowired
	private IAgentMatriculeConverterService converterService;

	@Autowired
	private ISoldeService soldeService;

	@Autowired
	private ISirhService sirhService;

	@ResponseBody
	@RequestMapping(value = "soldeAgent", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public ResponseEntity<String> getSoldeAgent(@RequestParam(value = "idAgent", required = true) Integer idAgent) {

		logger.debug("entered GET [solde/soldeAgent] => getSoldeAgent with parameter idAgent = {}", idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (sirhService.findAgent(convertedIdAgent) == null)
			throw new NotFoundException();

		SoldeDto result = soldeService.getAgentSolde(convertedIdAgent);

		return new ResponseEntity<String>(result.serializeInJSON(), HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "historiqueSolde", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public ResponseEntity<String> getHistoriqueSolde(@RequestParam(value = "idAgent", required = true) Integer idAgent,
			@RequestParam(value = "codeRefTypeAbsence", required = true) Integer codeRefTypeAbsence) {

		logger.debug(
				"entered GET [solde/historiqueSolde] => getHistoriqueSolde with parameter codeRefTypeAbsence = {}",
				codeRefTypeAbsence);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		List<HistoriqueSoldeDto> result = soldeService.getHistoriqueSoldeAgentByTypeAbsence(convertedIdAgent,codeRefTypeAbsence);
		
		String response = new JSONSerializer().exclude("*.class").transform(new MSDateTransformer(), Date.class)
				.deepSerialize(result);

		return new ResponseEntity<String>(response, HttpStatus.OK);
	}
}
