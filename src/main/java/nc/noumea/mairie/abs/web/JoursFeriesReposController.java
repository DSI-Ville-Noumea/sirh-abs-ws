package nc.noumea.mairie.abs.web;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.dto.AgentJoursFeriesReposDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.dto.SaisieReposDto;
import nc.noumea.mairie.abs.service.ISaisieJoursFeriesReposService;

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
@RequestMapping("/joursFeriesRepos")
public class JoursFeriesReposController {

	private Logger logger = LoggerFactory.getLogger(JoursFeriesReposController.class);

	@Autowired
	private ISaisieJoursFeriesReposService saisieJoursFeriesReposService;

	@ResponseBody
	@RequestMapping(value = "/getListAgentsWithJoursFeriesEnRepos", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public SaisieReposDto getListAgentsWithJoursFeriesEnRepos(
			@RequestParam(value = "idAgent", required = true) Integer idAgent,
			@RequestParam(value = "codeService", required = false) String codeService,
			@RequestParam(value = "dateDebut", required = true) @DateTimeFormat(pattern = "yyyyMMdd") Date dateDebut,
			@RequestParam(value = "dateFin", required = true) @DateTimeFormat(pattern = "yyyyMMdd") Date dateFin) {

		logger.debug("entered GET [motifCompteur/getListAgentsWithJoursFeriesEnRepos] => getListAgentsWithJoursFeriesEnRepos "
				+ "with parameter idAgent = {}, codeService = {}, dateDebut = {}, dateFin = {}, ",
				idAgent, codeService, dateDebut, dateFin);

		return saisieJoursFeriesReposService.getListAgentsWithJoursFeriesEnRepos(idAgent, codeService, dateDebut, dateFin);
	}

	@ResponseBody
	@RequestMapping(value = "/setListAgentsWithJoursFeriesEnRepos", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto setListAgentsWithJoursFeriesEnRepos(
			@RequestParam(value = "idAgent", required = true) Integer idAgent,
			@RequestParam(value = "dateDebut", required = true) @DateTimeFormat(pattern = "yyyyMMdd") Date dateDebut,
			@RequestParam(value = "dateFin", required = true) @DateTimeFormat(pattern = "yyyyMMdd") Date dateFin,
			@RequestBody(required = true) List<AgentJoursFeriesReposDto> listDto) {

		logger.debug("entered POST [motifCompteur/setListAgentsWithJoursFeriesEnRepos] => setListAgentsWithJoursFeriesEnRepos"
				+ "with parameter idAgent = {}, dateDebut = {}, dateFin = {}, ",
				idAgent, dateDebut, dateFin);

		return saisieJoursFeriesReposService.setListAgentsWithJoursFeriesEnRepos(idAgent, listDto, dateDebut, dateFin);
	}
}
