package nc.noumea.mairie.abs.web;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.dto.AgentJoursFeriesGardeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.dto.SaisieGardeDto;
import nc.noumea.mairie.abs.service.ISaisieJoursFeriesGardeService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/joursFeriesGarde")
public class JoursFeriesGardeController {

	private Logger logger = LoggerFactory.getLogger(JoursFeriesGardeController.class);

	@Autowired
	private ISaisieJoursFeriesGardeService saisieJoursFeriesGardeService;

	@ResponseBody
	@RequestMapping(value = "/getListAgentsWithJoursFeriesEnGarde", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public SaisieGardeDto getListAgentsWithJoursFeriesEnGarde(
			@RequestParam(value = "idAgent", required = true) Integer idAgent,
			@RequestParam(value = "idServiceADS", required = false) Integer idServiceADS,
			@RequestParam(value = "dateDebut", required = true) @DateTimeFormat(pattern = "yyyyMMdd") Date dateDebut,
			@RequestParam(value = "dateFin", required = true) @DateTimeFormat(pattern = "yyyyMMdd") Date dateFin) {

		logger.debug(
				"entered GET [joursFeriesGarde/getListAgentsWithJoursFeriesEnGarde] => getListAgentsWithJoursFeriesEnGarde "
						+ "with parameter idAgent = {}, idServiceADS = {}, dateDebut = {}, dateFin = {}, ", idAgent,
				idServiceADS, dateDebut, dateFin);

		return saisieJoursFeriesGardeService.getListAgentsWithJoursFeriesEnGarde(idAgent, idServiceADS, dateDebut,
				dateFin);
	}

	@ResponseBody
	@RequestMapping(value = "/setListAgentsWithJoursFeriesEnGarde", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto setListAgentsWithJoursFeriesEnGarde(
			@RequestParam(value = "idAgent", required = true) Integer idAgent,
			@RequestParam(value = "dateDebut", required = true) @DateTimeFormat(pattern = "yyyyMMdd") Date dateDebut,
			@RequestParam(value = "dateFin", required = true) @DateTimeFormat(pattern = "yyyyMMdd") Date dateFin,
			@RequestBody(required = true) List<AgentJoursFeriesGardeDto> listDto) {

		logger.debug(
				"entered POST [joursFeriesGarde/setListAgentsWithJoursFeriesEnGarde] => setListAgentsWithJoursFeriesEnGarde"
						+ "with parameter idAgent = {}, dateDebut = {}, dateFin = {}, ", idAgent, dateDebut, dateFin);

		return saisieJoursFeriesGardeService.setListAgentsWithJoursFeriesEnGarde(idAgent, listDto, dateDebut, dateFin);
	}
}
