package nc.noumea.mairie.abs.web;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.service.IAbsenceService;
import nc.noumea.mairie.abs.service.IAccessRightsService;
import nc.noumea.mairie.abs.service.IAgentMatriculeConverterService;
import nc.noumea.mairie.abs.transformer.MSDateTransformer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/demandes")
public class DemandeController {

	private Logger logger = LoggerFactory.getLogger(DemandeController.class);

	@Autowired
	private IAbsenceService absenceService;

	@Autowired
	private IAgentMatriculeConverterService converterService;

	@Autowired
	private IAccessRightsService accessRightService;

	@ResponseBody
	@RequestMapping(value = "/demande", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	@Transactional(value = "absTransactionManager")
	public ResponseEntity<String> setDemandeAbsence(@RequestParam("idAgent") int idAgent,
			@RequestBody(required = true) String demandeDto) {

		logger.debug("entered POST [demandes/demande] => setDemandeAbsence for Kiosque with parameters idAgent = {}",
				idAgent);

		DemandeDto dto = new JSONDeserializer<DemandeDto>().use(Date.class, new MSDateTransformer()).deserializeInto(
				demandeDto, new DemandeDto());

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);
		ReturnMessageDto srm = absenceService.saveDemande(convertedIdAgent, dto);

		String response = new JSONSerializer().exclude("*.class").deepSerialize(srm);

		if (!srm.getErrors().isEmpty()) {
			return new ResponseEntity<>(response, HttpStatus.CONFLICT);
		} else {
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/demande", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public ResponseEntity<String> getDemandeAbsence(@RequestParam("idAgent") int idAgent,
			@RequestParam("idDemande") int idDemande, @RequestParam("idTypeDemande") int idTypeDemande) {

		logger.debug(
				"entered GET [demandes/demande] => getDemandeAbsence for Kiosque with parameters idAgent = {} and idDemande = {}",
				idAgent, idDemande);

		DemandeDto result = absenceService.getDemande(idDemande, idTypeDemande);

		if (null == result)
			return new ResponseEntity<String>(HttpStatus.NO_CONTENT);

		String response = new JSONSerializer().exclude("*.class").transform(new MSDateTransformer(), Date.class)
				.deepSerialize(result);
		return new ResponseEntity<String>(response, HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "/listeDemandesAgent", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public ResponseEntity<String> getListeDemandesAbsenceAgent(
			@RequestParam("idAgent") int idAgent,
			@RequestParam(value = "ongletDemande", required = true) String ongletDemande,
			@RequestParam(value = "from", required = false) @DateTimeFormat(pattern = "YYYYMMdd") Date fromDate,
			@RequestParam(value = "to", required = false) @DateTimeFormat(pattern = "YYYYMMdd") Date toDate,
			@RequestParam(value = "dateDemande", required = false) @DateTimeFormat(pattern = "YYYYMMdd") Date dateDemande,
			@RequestParam(value = "etat", required = false) Integer idRefEtat,
			@RequestParam(value = "type", required = false) Integer idRefType) {

		logger.debug(
				"entered GET [demandes/listeDemandesAgent] => getListeDemandesAbsenceAgent with parameters idAgent = {}, ongletDemande = {}, from = {}, to = {}, dateDemande = {}, etat = {} and type = {}",
				idAgent, ongletDemande, fromDate, toDate, dateDemande, idRefEtat, idRefType);

		Integer convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		List<DemandeDto> result = absenceService.getListeDemandesAgent(convertedIdAgent, ongletDemande, fromDate,
				toDate, dateDemande, idRefEtat, idRefType);

		if (result.size() == 0)
			return new ResponseEntity<String>(HttpStatus.NO_CONTENT);

		String response = new JSONSerializer().exclude("*.class").transform(new MSDateTransformer(), Date.class)
				.deepSerialize(result);
		return new ResponseEntity<String>(response, HttpStatus.OK);
	}
}
