package nc.noumea.mairie.abs.web;

import java.util.Date;

import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.service.IAbsenceService;
import nc.noumea.mairie.abs.service.IAgentMatriculeConverterService;
import nc.noumea.mairie.ws.ISirhWSConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/asa")
public class AsaController {

	private Logger logger = LoggerFactory.getLogger(AsaController.class);

	@Autowired
	private IAgentMatriculeConverterService converterService;

	@Autowired
	private ISirhWSConsumer sirhWSConsumer;

	@Autowired
	private IAbsenceService absenceService;

	/**
	 * Pour connaire sur une periode données si un agent est en absence
	 * syndicale <br />
	 * Parametres en entree : format du type timestamp : dd/MM/yyyy HH:mm
	 */
	@ResponseBody
	@RequestMapping(value = "/checkAbsencesSyndicales", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public ReturnMessageDto checkAbsencesSyndicales(
			@RequestParam("idAgent") int idAgent,
			@RequestParam(value = "dateDebut", required = false) @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm") Date fromDate,
			@RequestParam(value = "dateFin", required = false) @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm") Date toDate) {

		logger.debug(
				"entered GET [asa/checkAbsencesSyndicales] => checkAbsencesSyndicales with parameters idAgent = {}, dateDebut = {}, dateFin = {}",
				idAgent, fromDate, toDate);

		Integer convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		AgentGeneriqueDto agent = sirhWSConsumer.getAgent(convertedIdAgent);
		if (agent == null || agent.getIdAgent() == null)
			throw new NotFoundException();

		ReturnMessageDto result = absenceService.checkAbsencesSyndicales(convertedIdAgent, fromDate, toDate);

		return result;
	}
}
