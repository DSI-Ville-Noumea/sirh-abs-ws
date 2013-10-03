package nc.noumea.mairie.abs.web;

import java.util.Date;

import nc.noumea.mairie.abs.service.IRecuperationService;

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

@Controller
@RequestMapping("/recuperations")
public class RecuperationController {

	private Logger logger = LoggerFactory.getLogger(RecuperationController.class);
	
	@Autowired
	private IRecuperationService recuperationService;

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@Transactional(value = "absTransactionManager")
	public ResponseEntity<String> addRecuperationForAgentAndWeek(
			@RequestParam("idAgent") Integer idAgent,
			@RequestParam("dateLundi") @DateTimeFormat(pattern = "YYYYMMdd") Date dateMonday,
			@RequestParam("minutes") int minutes) {

		logger.debug(
				"entered GET [recuperations/add] => addRecuperationForAgentAndWeek with parameters idAgent = {}, dateMonday = {} and minutes = {}",
				idAgent, dateMonday, minutes);
		
		recuperationService.addRecuperationToAgent(idAgent, dateMonday, minutes);
		
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
