package nc.noumea.mairie.abs.web;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.EditionDemandeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.dto.SoldeDto;
import nc.noumea.mairie.abs.service.IAbsenceService;
import nc.noumea.mairie.abs.service.IAccessRightsService;
import nc.noumea.mairie.abs.service.IAgentMatriculeConverterService;
import nc.noumea.mairie.abs.service.ISoldeService;
import nc.noumea.mairie.abs.service.ISuppressionService;
import nc.noumea.mairie.abs.service.impl.HelperService;
import nc.noumea.mairie.abs.transformer.MSDateTransformer;
import nc.noumea.mairie.sirh.service.ISirhService;
import nc.noumea.mairie.ws.ISirhWSConsumer;

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
import org.springframework.web.servlet.ModelAndView;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

@Controller
@RequestMapping("/demandes")
public class DemandeController {

	private Logger logger = LoggerFactory.getLogger(DemandeController.class);

	@Autowired
	private IAbsenceService absenceService;

	@Autowired
	private ISuppressionService suppressionService;

	@Autowired
	private IAgentMatriculeConverterService converterService;

	@Autowired
	private IAccessRightsService accessRightService;

	@Autowired
	private HelperService helperService;

	@Autowired
	private ISirhWSConsumer sirhWSConsumer;

	@Autowired
	private ISoldeService soldeService;

	@Autowired
	private ISirhService sirhService;

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
			@RequestParam("idDemande") int idDemande) {

		logger.debug(
				"entered GET [demandes/demande] => getDemandeAbsence for Kiosque with parameters idAgent = {} and idDemande = {}",
				idAgent, idDemande);

		DemandeDto result = absenceService.getDemandeDto(idDemande);

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

		if (sirhService.findAgent(convertedIdAgent) == null)
			throw new NotFoundException();

		List<DemandeDto> result = absenceService.getListeDemandes(convertedIdAgent, convertedIdAgent, ongletDemande,
				fromDate, toDate, dateDemande, idRefEtat, idRefType);

		if (result.size() == 0)
			return new ResponseEntity<String>(HttpStatus.NO_CONTENT);

		String response = new JSONSerializer().exclude("*.class").transform(new MSDateTransformer(), Date.class)
				.deepSerialize(result);
		return new ResponseEntity<String>(response, HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "/xml/getDemande", produces = "application/xml", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public ModelAndView getXmlDemande(@RequestParam("idAgent") int idAgent, @RequestParam("idDemande") int idDemande)
			throws ParseException {

		logger.debug(
				"entered GET [demandes/xml/getDemande] => getXmlDemande with parameters idAgent = {}, idDemande = {}",
				idAgent, idDemande);
		Integer convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (sirhService.findAgent(convertedIdAgent) == null)
			throw new NotFoundException();

		DemandeDto demandeDto = absenceService.getDemandeDto(idDemande);

		SoldeDto soldeDto = soldeService.getAgentSolde(demandeDto.getAgentWithServiceDto().getIdAgent(), new Date());

		AgentWithServiceDto approbateurDto = accessRightService.getApprobateurOfAgent(demandeDto
				.getAgentWithServiceDto().getIdAgent());

		EditionDemandeDto dtoFinal = new EditionDemandeDto(demandeDto, soldeDto, approbateurDto);

		return new ModelAndView("xmlView", "object", dtoFinal);
	}

	@ResponseBody
	@RequestMapping(value = "/listeDemandes", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public ResponseEntity<String> getListeDemandesAbsence(
			@RequestParam("idAgent") int idAgent,
			@RequestParam(value = "ongletDemande", required = true) String ongletDemande,
			@RequestParam(value = "from", required = false) @DateTimeFormat(pattern = "YYYYMMdd") Date fromDate,
			@RequestParam(value = "to", required = false) @DateTimeFormat(pattern = "YYYYMMdd") Date toDate,
			@RequestParam(value = "dateDemande", required = false) @DateTimeFormat(pattern = "YYYYMMdd") Date dateDemande,
			@RequestParam(value = "etat", required = false) Integer idRefEtat,
			@RequestParam(value = "type", required = false) Integer idRefType,
			@RequestParam(value = "idAgentRecherche", required = false) Integer idAgentRecherche) {

		logger.debug(
				"entered GET [demandes/listeDemandes] => getListeDemandesAbsence with parameters idInputter = {}, ongletDemande = {}, from = {}, to = {}, dateDemande = {}, etat = {}, type = {} and idAgentConcerne= {}",
				idAgent, ongletDemande, fromDate, toDate, dateDemande, idRefEtat, idRefType, idAgentRecherche);

		Integer convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		if (sirhService.findAgent(convertedIdAgent) == null)
			throw new NotFoundException();

		// ON VERIFIE LES DROITS
		if (idAgentRecherche != null) {
			ReturnMessageDto srm = new ReturnMessageDto();
			if (!accessRightService.verifAccessRightListDemande(convertedIdAgent, idAgentRecherche, srm)) {
				if (!srm.getErrors().isEmpty()) {
					String response = new JSONSerializer().exclude("*.class").deepSerialize(srm);
					return new ResponseEntity<>(response, HttpStatus.CONFLICT);
				}
			}
		}

		List<DemandeDto> result = absenceService.getListeDemandes(convertedIdAgent, idAgentRecherche, ongletDemande,
				fromDate, toDate, dateDemande, idRefEtat, idRefType);

		if (result.size() == 0)
			return new ResponseEntity<String>(HttpStatus.NO_CONTENT);

		String response = new JSONSerializer().exclude("*.class").transform(new MSDateTransformer(), Date.class)
				.deepSerialize(result);
		return new ResponseEntity<String>(response, HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "/changerEtats", produces = "application/json; charset=utf-8", method = RequestMethod.POST)
	@Transactional(value = "absTransactionManager")
	public ResponseEntity<String> setAbsencesEtat(@RequestParam("idAgent") int idAgent,
			@RequestBody(required = true) String demandeEtatChangeDtoString) {

		logger.debug("entered POST [demandes/changerEtats] => setAbsencesEtat with parameters idAgent = {}", idAgent);

		Integer convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		DemandeEtatChangeDto dto = new JSONDeserializer<DemandeEtatChangeDto>()
				.use(Date.class, new MSDateTransformer()).deserializeInto(demandeEtatChangeDtoString,
						new DemandeEtatChangeDto());

		ReturnMessageDto result = absenceService.setDemandeEtat(convertedIdAgent, dto);

		String response = new JSONSerializer().exclude("*.class").deepSerialize(result);

		if (result.getErrors().size() != 0)
			return new ResponseEntity<String>(response, HttpStatus.CONFLICT);

		return new ResponseEntity<String>(response, HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "/updateToEtatPris", produces = "application/json; charset=utf-8", method = RequestMethod.POST)
	@Transactional(value = "absTransactionManager")
	public ResponseEntity<String> setAbsencesEtatPris(@RequestParam("idDemande") Integer idDemande) {

		logger.debug(
				"entered POST [demandes/updateToEtatPris] => setAbsencesEtatPris for SIRH-JOBS with parameters idDemande = {}",
				idDemande);

		ReturnMessageDto result = absenceService.setDemandeEtatPris(idDemande);

		String response = new JSONSerializer().exclude("*.class").deepSerialize(result);

		if (result.getErrors().size() != 0)
			return new ResponseEntity<String>(response, HttpStatus.CONFLICT);

		return new ResponseEntity<String>(response, HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "/deleteDemande", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(value = "absTransactionManager")
	public ResponseEntity<String> supprimerDemande(@RequestParam("idAgent") int idAgent,
			@RequestParam("idDemande") int idDemande) {

		logger.debug(
				"entered GET [demandes/deleteDemande] => supprimerDemande with parameters idAgent = {}, idDemande = {}",
				idAgent, idDemande);

		Integer convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		ReturnMessageDto result = suppressionService.supprimerDemande(convertedIdAgent, idDemande);

		String response = new JSONSerializer().exclude("*.class").deepSerialize(result);

		if (result.getErrors().size() != 0)
			return new ResponseEntity<String>(response, HttpStatus.CONFLICT);

		return new ResponseEntity<String>(response, HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "/supprimerDemandeProvisoire", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	@Transactional(value = "absTransactionManager")
	public ResponseEntity<String> supprimerAbsenceProvisoire(@RequestParam("idDemande") Integer idDemande) {

		logger.debug(
				"entered POST [demandes/supprimerDemandeProvisoire] => supprimerAbsenceProvisoire for SIRH-JOBS with parameters idDemande = {}",
				idDemande);

		ReturnMessageDto result = suppressionService.supprimerDemandeEtatProvisoire(idDemande);

		String response = new JSONSerializer().exclude("*.class").deepSerialize(result);

		if (result.getErrors().size() != 0)
			return new ResponseEntity<String>(response, HttpStatus.CONFLICT);

		return new ResponseEntity<String>(response, HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "/demandeSIRH", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	@Transactional(value = "absTransactionManager")
	public ResponseEntity<String> setDemandeAbsenceSIRH(@RequestParam("idAgent") int idAgent,
			@RequestBody(required = true) String demandeDto) {

		logger.debug(
				"entered POST [demandes/demandeSIRH] => setDemandeAbsenceSIRH for SIRH with parameters idAgent = {}",
				idAgent);

		DemandeDto dto = new JSONDeserializer<DemandeDto>().use(Date.class, new MSDateTransformer()).deserializeInto(
				demandeDto, new DemandeDto());

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);
		ReturnMessageDto srm = absenceService.saveDemandeSIRH(convertedIdAgent, dto);

		String response = new JSONSerializer().exclude("*.class").deepSerialize(srm);

		if (!srm.getErrors().isEmpty()) {
			return new ResponseEntity<>(response, HttpStatus.CONFLICT);
		} else {
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/listeDemandesSIRH", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public ResponseEntity<String> getListeDemandesAbsenceSIRH(
			@RequestParam(value = "from", required = false) @DateTimeFormat(pattern = "YYYYMMdd") Date fromDate,
			@RequestParam(value = "to", required = false) @DateTimeFormat(pattern = "YYYYMMdd") Date toDate,
			@RequestParam(value = "etat", required = false) Integer idRefEtat,
			@RequestParam(value = "type", required = false) Integer idRefType,
			@RequestParam(value = "idAgentRecherche", required = false) Integer idAgentRecherche) {

		logger.debug(
				"entered GET [demandes/listeDemandesSIRH] => getListeDemandesAbsenceSIRH with parameters  from = {}, to = {},  etat = {}, type = {} and idAgentConcerne= {}",
				fromDate, toDate, idRefEtat, idRefType, idAgentRecherche);

		List<DemandeDto> result = absenceService.getListeDemandesSIRH(fromDate, toDate, idRefEtat, idRefType,
				idAgentRecherche);

		if (result.size() == 0)
			return new ResponseEntity<String>(HttpStatus.NO_CONTENT);

		String response = new JSONSerializer().exclude("*.class").transform(new MSDateTransformer(), Date.class)
				.deepSerialize(result);
		return new ResponseEntity<String>(response, HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "/historiqueSIRH", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public ResponseEntity<String> getDemandesArchives(@RequestParam("idDemande") Integer idDemande) {

		logger.debug("entered GET [demandes/historiqueSIRH] => getDemandesArchives with parameter idDemande = {}",
				idDemande);

		List<DemandeDto> result = absenceService.getDemandesArchives(idDemande);
		if (result.size() == 0)
			return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
		String response = new JSONSerializer().exclude("*.class").transform(new MSDateTransformer(), Date.class)
				.deepSerialize(result);
		return new ResponseEntity<String>(response, HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "/changerEtatsSIRH", produces = "application/json; charset=utf-8", method = RequestMethod.POST)
	@Transactional(value = "absTransactionManager")
	public ResponseEntity<String> setAbsencesEtatSIRH(@RequestParam("idAgent") int idAgent,
			@RequestBody(required = true) String demandeEtatChangeDtoString) {

		logger.debug("entered POST [demandes/changerEtatsSIRH] => setAbsencesEtatSIRH with parameters idAgent = {}",
				idAgent);

		Integer convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		List<DemandeEtatChangeDto> dto = new JSONDeserializer<List<DemandeEtatChangeDto>>().use(null, ArrayList.class)
				.use("values", DemandeEtatChangeDto.class).deserialize(demandeEtatChangeDtoString);

		ReturnMessageDto result = absenceService.setDemandeEtatSIRH(convertedIdAgent, dto);

		String response = new JSONSerializer().exclude("*.class").deepSerialize(result);

		if (result.getErrors().size() != 0)
			return new ResponseEntity<String>(response, HttpStatus.CONFLICT);

		return new ResponseEntity<String>(response, HttpStatus.OK);
	}
}
