package nc.noumea.mairie.abs.web;

import java.util.List;

import nc.noumea.mairie.abs.dto.OrganisationSyndicaleDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.service.IOrganisationSyndicaleService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

@Controller
@RequestMapping("/organisation")
public class OrganisationSyndicaleController {

	private Logger logger = LoggerFactory.getLogger(OrganisationSyndicaleController.class);

	@Autowired
	private IOrganisationSyndicaleService organisationService;

	@ResponseBody
	@RequestMapping(value = "/addOS", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	@Transactional(value = "absTransactionManager")
	public ResponseEntity<String> setOrganisationSyndicale(@RequestBody(required = true) String organisationDto) {

		logger.debug("entered POST [organisation/addOS] => setOrganisationSyndicale for SIRH");

		OrganisationSyndicaleDto dto = new JSONDeserializer<OrganisationSyndicaleDto>().deserializeInto(
				organisationDto, new OrganisationSyndicaleDto());

		ReturnMessageDto srm = organisationService.saveOrganisation(dto);

		String response = new JSONSerializer().exclude("*.class").deepSerialize(srm);

		if (!srm.getErrors().isEmpty()) {
			return new ResponseEntity<>(response, HttpStatus.CONFLICT);
		} else {
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/listOrganisation", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public ResponseEntity<String> listOrganisationSyndicale() {

		logger.debug("entered GET [organisation/listOrganisation] => listOrganisationSyndicale");

		List<OrganisationSyndicaleDto> orga = organisationService.getListOrganisationSyndicale();

		String json = new JSONSerializer().exclude("*.class").serialize(orga);

		return new ResponseEntity<String>(json, HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "/listOrganisationActif", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public ResponseEntity<String> listOrganisationSyndicaleActives() {

		logger.debug("entered GET [organisation/listOrganisationActif] => listOrganisationSyndicaleActives");

		List<OrganisationSyndicaleDto> orga = organisationService.getListOrganisationSyndicaleActives();

		String json = new JSONSerializer().exclude("*.class").serialize(orga);

		return new ResponseEntity<String>(json, HttpStatus.OK);
	}
}
