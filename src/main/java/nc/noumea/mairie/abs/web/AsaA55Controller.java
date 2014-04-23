package nc.noumea.mairie.abs.web;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.dto.CompteurAsaDto;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.service.IAgentMatriculeConverterService;
import nc.noumea.mairie.abs.service.ICounterService;
import nc.noumea.mairie.abs.transformer.MSDateTransformer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
@RequestMapping("/asaA55")
public class AsaA55Controller {

	private Logger logger = LoggerFactory.getLogger(AsaA55Controller.class);

	@Autowired
	@Qualifier("AsaA55CounterServiceImpl")
	private ICounterService counterService;

	@Autowired
	private IAgentMatriculeConverterService converterService;

	@ResponseBody
	@RequestMapping(value = "/addManual", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	@Transactional(value = "absTransactionManager")
	public ResponseEntity<String> addAsaA55ManuelForAgent(@RequestParam("idAgent") int idAgent,
			@RequestBody(required = true) String compteurDto) {

		logger.debug("entered POST [asaA55/addManual] => addAsaA55ManuelForAgent with parameters idAgent = {}", idAgent);

		CompteurDto dto = new JSONDeserializer<CompteurDto>().use(Date.class, new MSDateTransformer()).deserializeInto(
				compteurDto, new CompteurDto());

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		ReturnMessageDto srm = counterService.majManuelleCompteurToAgent(convertedIdAgent, dto);

		String response = new JSONSerializer().exclude("*.class").deepSerialize(srm);

		if (!srm.getErrors().isEmpty()) {
			return new ResponseEntity<>(response, HttpStatus.CONFLICT);
		} else {
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/listeCompteurA55", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public ResponseEntity<String> getListeCompteur() {

		logger.debug("entered GET [asaA55/listeCompteurA55] => getListeCompteur ");

		List<CompteurAsaDto> result = counterService.getListeCompteur();

		if (result.size() == 0)
			return new ResponseEntity<String>(HttpStatus.NO_CONTENT);

		String response = new JSONSerializer().exclude("*.class").transform(new MSDateTransformer(), Date.class)
				.deepSerialize(result);
		return new ResponseEntity<String>(response, HttpStatus.OK);
	}
}
