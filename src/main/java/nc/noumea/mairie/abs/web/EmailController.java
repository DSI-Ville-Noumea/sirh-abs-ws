package nc.noumea.mairie.abs.web;

import nc.noumea.mairie.abs.dto.EmailInfoDto;
import nc.noumea.mairie.abs.service.IEmailService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/email")
public class EmailController {

	private Logger logger = LoggerFactory.getLogger(FiltreController.class);

	@Autowired
	private IEmailService emailService;

	/**
	 * Liste des destinataires pour l envoi d emails d information aux viseurs et approbateurs
	 */
	@ResponseBody
	@RequestMapping(value = "/listDestinatairesEmailInfo", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public EmailInfoDto getListIdDestinatairesEmailInfo() {

		logger.debug("entered GET [email/listDestinatairesEmailInfo] => getListIdDestinatairesEmailInfo");

		EmailInfoDto result = emailService.getListIdDestinatairesEmailInfo();

		return result;
	}

	/**
	 * Liste des destinataires pour l envoi d'emails d information aux approbateurs
	 */
	@ResponseBody
	@RequestMapping(value = "/listApprobateursEmailMaladie", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public EmailInfoDto getListIdApprobateursEmailMaladie() {

		logger.debug("entered GET [email/listApprobateursEmailMaladie] => getListApprobateursEmailMaladie");

		EmailInfoDto result = emailService.getListIdApprobateursEmailMaladie();

		return result;
	}
}
