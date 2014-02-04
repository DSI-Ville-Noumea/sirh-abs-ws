package nc.noumea.mairie.abs.web;

import java.util.Date;

import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.service.IAgentMatriculeConverterService;
import nc.noumea.mairie.abs.service.ICounterService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

@Controller
@RequestMapping("/recuperations")
public class RecuperationController {

	private Logger logger = LoggerFactory.getLogger(RecuperationController.class);

	@Autowired
	@Qualifier("RecupCounterServiceImpl")
	private ICounterService counterService;

	@Autowired
	private IAgentMatriculeConverterService converterService;

	@RequestMapping(value = "/addForPTG", method = RequestMethod.POST)
	@Transactional(value = "absTransactionManager")
	public ResponseEntity<String> addRecuperationForAgentAndWeek(@RequestParam("idAgent") Integer idAgent,
			@RequestParam("dateLundi") @DateTimeFormat(pattern = "YYYYMMdd") Date dateMonday,
			@RequestParam("minutes") int minutes) {

		logger.debug(
				"entered POST [recuperations/addForPTG] => addRecuperationForAgentAndWeek with parameters idAgent = {}, dateMonday = {} and minutes = {}",
				idAgent, dateMonday, minutes);

		counterService.addToAgentForPTG(idAgent, dateMonday, minutes);

		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@ResponseBody
	@RequestMapping(value = "/addManual", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	@Transactional(value = "absTransactionManager")
	public ResponseEntity<String> addRecuperationManuelForAgent(@RequestParam("idAgent") int idAgent,
			@RequestBody(required = true) String compteurDto) {

		logger.debug("entered POST [recuperations/addManual] => addRecuperationManuelForAgent with parameters idAgent = {}", idAgent);
		
		CompteurDto dto = new JSONDeserializer<CompteurDto>().deserializeInto(compteurDto, new CompteurDto());

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);
		
		ReturnMessageDto srm = counterService.majManuelleCompteurToAgent(convertedIdAgent, dto);

		String response = new JSONSerializer().exclude("*.class").deepSerialize(srm);

		if (!srm.getErrors().isEmpty()) {
			return new ResponseEntity<>(response, HttpStatus.CONFLICT);
		} else {
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
	}
}
