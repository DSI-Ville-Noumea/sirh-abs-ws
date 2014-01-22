package nc.noumea.mairie.abs.web;

import nc.noumea.mairie.abs.dto.EmailInfoDto;
import nc.noumea.mairie.abs.service.IAbsenceService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import flexjson.JSONSerializer;

@Controller
@RequestMapping("/email")
public class EmailController {

	private Logger logger = LoggerFactory.getLogger(FiltreController.class);

	@Autowired
	private IAbsenceService absenceService;
	
	@ResponseBody
	@RequestMapping(value = "/listDestinatairesEmailInfo", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public ResponseEntity<String> getListIdDestinatairesEmailInfo() {

		logger.debug("entered GET [email/listDestinatairesEmailInfo] => getListIdDestinatairesEmailInfo");

		EmailInfoDto result = absenceService.getListIdDestinatairesEmailInfo();
		
		String json = new JSONSerializer().exclude("*.class").deepSerialize(result);

		return new ResponseEntity<String>(json, HttpStatus.OK);
	}
}