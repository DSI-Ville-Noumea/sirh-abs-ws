package nc.noumea.mairie.abs.web;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.service.IAgentMatriculeConverterService;
import nc.noumea.mairie.abs.service.ICounterService;
import nc.noumea.mairie.abs.transformer.MSDateTransformer;

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
@RequestMapping("/reposcomps")
public class ReposCompController {

	private Logger logger = LoggerFactory.getLogger(ReposCompController.class);

	@Autowired
	@Qualifier("ReposCompCounterServiceImpl")
	private ICounterService counterService;

	@Autowired
	private IAgentMatriculeConverterService converterService;

	@RequestMapping(value = "/addForPTG", method = RequestMethod.POST)
	@Transactional(value = "absTransactionManager")
	public ResponseEntity<String> addReposCompForAgentAndWeek(@RequestParam("idAgent") Integer idAgent,
			@RequestParam("dateLundi") @DateTimeFormat(pattern = "YYYYMMdd") Date dateMonday,
			@RequestParam("minutes") int minutes) {

		logger.debug(
				"entered GET [reposcomps/add] => addReposCompForAgentAndWeek with parameters idAgent = {}, dateMonday = {} and minutes = {}",
				idAgent, dateMonday, minutes);

		counterService.addToAgentForPTG(idAgent, dateMonday, minutes);

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "/addManual", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	@Transactional(value = "absTransactionManager")
	public ResponseEntity<String> addRecuperationManuelForAgent(@RequestParam("idAgent") int idAgent,
			@RequestBody(required = true) String compteurDto) {

		logger.debug(
				"entered POST [reposcomps/addManual] => addRecuperationManuelForAgent with parameters idAgent = {}",
				idAgent);

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
	@RequestMapping(value = "/resetCompteurAnneePrecedente", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(value = "absTransactionManager")
	public ResponseEntity<String> resetCompteurAnneePrecedente(
			@RequestParam("idAgentReposCompCount") int idAgentReposCompCount) {

		logger.debug(
				"entered POST [reposcomps/resetCompteurAnneePrecedente] => resetCompteurAnneePrecedente with parameters idAgentReposCompCount = {}",
				idAgentReposCompCount);

		ReturnMessageDto srm = counterService.resetCompteurRCAnneePrecedente(idAgentReposCompCount);

		String response = new JSONSerializer().exclude("*.class").deepSerialize(srm);

		if (!srm.getErrors().isEmpty()) {
			return new ResponseEntity<>(response, HttpStatus.CONFLICT);
		} else {
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/resetCompteurAnneenCours", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(value = "absTransactionManager")
	public ResponseEntity<String> resetCompteurRCAnneenCours(
			@RequestParam("idAgentReposCompCount") int idAgentReposCompCount) {

		logger.debug(
				"entered POST [reposcomps/resetCompteurRCAnneenCours] => resetCompteurRCAnneenCours with parameters idAgentReposCompCount = {}",
				idAgentReposCompCount);

		ReturnMessageDto srm = counterService.resetCompteurRCAnneenCours(idAgentReposCompCount);

		String response = new JSONSerializer().exclude("*.class").deepSerialize(srm);

		if (!srm.getErrors().isEmpty()) {
			return new ResponseEntity<>(response, HttpStatus.CONFLICT);
		} else {
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/getListeCompteurAnneePrecedente", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public ResponseEntity<String> getListeCompteurAnneePrecedente() {

		logger.debug("entered GET [reposcomps/getListeCompteurAnneePrecedente] => getListeCompteurAnneePrecedente");

		List<Integer> listCompteur = counterService.getListAgentReposCompCountForResetAnneePrcd();

		String json = new JSONSerializer().exclude("*.class").serialize(listCompteur);

		return new ResponseEntity<String>(json, HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "/getListeCompteurAnneeEnCours", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public ResponseEntity<String> getListeCompteurAnneeEnCours() {

		logger.debug("entered GET [reposcomps/getListeCompteurAnneeEnCours] => getListeCompteurAnneeEnCours");

		List<Integer> listCompteur = counterService.getListAgentReposCompCountForResetAnneeEnCours();

		String json = new JSONSerializer().exclude("*.class").serialize(listCompteur);

		return new ResponseEntity<String>(json, HttpStatus.OK);
	}
}
