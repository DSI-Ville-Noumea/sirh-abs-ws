package nc.noumea.mairie.abs.web;

import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.service.IAbsenceService;
import nc.noumea.mairie.abs.service.IAgentMatriculeConverterService;
import nc.noumea.mairie.abs.service.ICounterService;
import nc.noumea.mairie.ws.ISirhWSConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/recuperations")
public class RecuperationController {

	private Logger logger = LoggerFactory.getLogger(RecuperationController.class);

	@Autowired
	@Qualifier("RecupCounterServiceImpl")
	private ICounterService counterService;

	@Autowired
	private IAgentMatriculeConverterService converterService;

	@Autowired
	private ISirhWSConsumer sirhWSConsumer;

	@Autowired
	private IAbsenceService absenceService;

	/**
	 * Ajoute des minutes au compteur de recuperation a un agent pour une
	 * semaine donnee <br />
	 * utile a SIRH-PTG-WS <br />
	 * Parametres en entree : format du type timestamp : YYYYMMdd
	 */
	@ResponseBody
	@RequestMapping(value = "/addForPTG", method = RequestMethod.POST)
	public void addRecuperationForAgentAndWeek(@RequestParam("idAgent") Integer idAgent,
			@RequestParam("dateLundi") @DateTimeFormat(pattern = "YYYYMMdd") Date dateMonday,
			@RequestParam("minutes") int minutes) {

		logger.debug(
				"entered POST [recuperations/addForPTG] => addRecuperationForAgentAndWeek with parameters idAgent = {}, dateMonday = {} and minutes = {}",
				idAgent, dateMonday, minutes);

		counterService.addToAgentForPTG(idAgent, dateMonday, minutes);
	}

	/**
	 * Ajout/retire manuel au compteur de recuperation pour un agent <br />
	 * RequestBody : Format du type timestamp : "/Date(1396306800000+1100)/"
	 */
	@ResponseBody
	@RequestMapping(value = "/addManual", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto addRecuperationManuelForAgent(@RequestParam("idAgent") int idAgent,
			@RequestBody(required = true) CompteurDto compteurDto, HttpServletResponse response) {

		logger.debug(
				"entered POST [recuperations/addManual] => addRecuperationManuelForAgent with parameters idAgent = {}",
				idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		ReturnMessageDto srm = counterService.majManuelleCompteurToAgent(convertedIdAgent, compteurDto);

		if (!srm.getErrors().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_CONFLICT);
		}

		return srm;
	}

	/**
	 * Pour connaire sur une periode données si un agent est en récupération <br />
	 * Parametres en entree : format du type timestamp : YYYYMMdd
	 */
	@ResponseBody
	@RequestMapping(value = "/checkRecuperations", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public ReturnMessageDto checkRecuperations(@RequestParam("idAgent") int idAgent,
			@RequestParam(value = "dateDebut", required = false) @DateTimeFormat(pattern = "YYYYMMdd") Date fromDate,
			@RequestParam(value = "dateFin", required = false) @DateTimeFormat(pattern = "YYYYMMdd") Date toDate) {

		logger.debug(
				"entered GET [recuperations/checkRecuperations] => checkRecuperations with parameters idAgent = {}, dateDebut = {}, dateFin = {}",
				idAgent, fromDate, toDate);

		Integer convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		AgentGeneriqueDto agent = sirhWSConsumer.getAgent(convertedIdAgent);
		if (agent == null || agent.getIdAgent() == null)
			throw new NotFoundException();

		ReturnMessageDto result = absenceService.checkRecuperations(convertedIdAgent, fromDate, toDate);

		return result;
	}
}
