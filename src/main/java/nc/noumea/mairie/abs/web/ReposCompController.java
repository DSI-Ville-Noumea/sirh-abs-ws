package nc.noumea.mairie.abs.web;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.MoisAlimAutoCongesAnnuelsDto;
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
@RequestMapping("/reposcomps")
public class ReposCompController {

	private Logger logger = LoggerFactory.getLogger(ReposCompController.class);

	@Autowired
	@Qualifier("ReposCompCounterServiceImpl")
	private ICounterService counterService;

	@Autowired
	private IAgentMatriculeConverterService converterService;

	@Autowired
	private ISirhWSConsumer sirhWSConsumer;

	@Autowired
	private IAbsenceService absenceService;

	/**
	 * Ajoute des minutes au compteur de repos compensateur a un agent pour une
	 * semaine donnee <br />
	 * utile a SIRH-PTG-WS <br />
	 * Parametres en entree : format du type timestamp : yyyyMMdd
	 */
	@ResponseBody
	@RequestMapping(value = "/addForPTG", method = RequestMethod.POST)
	public void addReposCompForAgentAndWeek(@RequestParam("idAgent") Integer idAgent,
			@RequestParam("dateLundi") @DateTimeFormat(pattern = "yyyyMMdd") Date dateMonday,
			@RequestParam("minutes") int minutes) {

		logger.debug(
				"entered GET [reposcomps/add] => addReposCompForAgentAndWeek with parameters idAgent = {}, dateMonday = {} and minutes = {}",
				idAgent, dateMonday, minutes);

		counterService.addToAgentForPTG(idAgent, dateMonday, minutes);
	}

	/**
	 * Ajout/retire manuel au compteur de repos compensateur pour un agent <br />
	 * RequestBody : Format du type timestamp : "/Date(1396306800000+1100)/"
	 */
	@ResponseBody
	@RequestMapping(value = "/addManual", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto addRecuperationManuelForAgent(@RequestParam("idAgent") int idAgent,
			@RequestBody(required = true) CompteurDto compteurDto, HttpServletResponse response) {

		logger.debug(
				"entered POST [reposcomps/addManual] => addRecuperationManuelForAgent with parameters idAgent = {}",
				idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		ReturnMessageDto srm = counterService.majManuelleCompteurToAgent(convertedIdAgent, compteurDto, false);

		if (!srm.getErrors().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_CONFLICT);
		}

		return srm;
	}

	/**
	 * Remise a zero des compteurs de repos compensateur de l annee precedente <br />
	 * utile a SIRH-JOBS le 31/08
	 */
	@ResponseBody
	@RequestMapping(value = "/resetCompteurAnneePrecedente", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public ReturnMessageDto resetCompteurAnneePrecedente(
			@RequestParam("idAgentReposCompCount") int idAgentReposCompCount, HttpServletResponse response) {

		logger.debug(
				"entered POST [reposcomps/resetCompteurAnneePrecedente] => resetCompteurAnneePrecedente with parameters idAgentReposCompCount = {}",
				idAgentReposCompCount);

		ReturnMessageDto srm = counterService.resetCompteurRCAnneePrecedente(idAgentReposCompCount);

		if (!srm.getErrors().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_CONFLICT);
		}

		return srm;
	}

	/**
	 * Remise a zero des compteurs de repos compensateur de l annee en cours <br />
	 * utile a SIRH-JOBS le 31/12
	 */
	@ResponseBody
	@RequestMapping(value = "/resetCompteurAnneenCours", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public ReturnMessageDto resetCompteurRCAnneenCours(
			@RequestParam("idAgentReposCompCount") int idAgentReposCompCount, HttpServletResponse response) {

		logger.debug(
				"entered POST [reposcomps/resetCompteurRCAnneenCours] => resetCompteurRCAnneenCours with parameters idAgentReposCompCount = {}",
				idAgentReposCompCount);

		ReturnMessageDto srm = counterService.resetCompteurRCAnneenCours(idAgentReposCompCount);

		if (!srm.getErrors().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_CONFLICT);
		}

		return srm;
	}

	/**
	 * Liste des compteurs de l annee precedente a remettre a zero
	 */
	@ResponseBody
	@RequestMapping(value = "/getListeCompteurAnneePrecedente", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<Integer> getListeCompteurAnneePrecedente() {

		logger.debug("entered GET [reposcomps/getListeCompteurAnneePrecedente] => getListeCompteurAnneePrecedente");

		List<Integer> listCompteur = counterService.getListAgentReposCompCountForResetAnneePrcd();

		return listCompteur;
	}

	/**
	 * Liste des compteurs de l annee en cours a remettre a zero
	 */
	@ResponseBody
	@RequestMapping(value = "/getListeCompteurAnneeEnCours", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<Integer> getListeCompteurAnneeEnCours() {

		logger.debug("entered GET [reposcomps/getListeCompteurAnneeEnCours] => getListeCompteurAnneeEnCours");

		List<Integer> listCompteur = counterService.getListAgentReposCompCountForResetAnneeEnCours();

		return listCompteur;
	}

	/**
	 * Pour connaire sur une periode données si un agent est en repos
	 * compensateur <br />
	 * Parametres en entree : format du type timestamp : dd/MM/yyyy HH:mm
	 */
	@ResponseBody
	@RequestMapping(value = "/checkReposCompensateurs", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public ReturnMessageDto checkReposCompensateurs(
			@RequestParam("idAgent") int idAgent,
			@RequestParam(value = "dateDebut", required = false) @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm") Date fromDate,
			@RequestParam(value = "dateFin", required = false) @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm") Date toDate) {

		logger.debug(
				"entered GET [reposcomps/checkReposCompensateurs] => checkReposCompensateurs with parameters idAgent = {}, dateDebut = {}, dateFin = {}",
				idAgent, fromDate, toDate);

		Integer convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		AgentGeneriqueDto agent = sirhWSConsumer.getAgent(convertedIdAgent);
		if (agent == null || agent.getIdAgent() == null)
			throw new NotFoundException();

		ReturnMessageDto result = absenceService.checkReposCompensateurs(convertedIdAgent, fromDate, toDate);

		return result;
	}

	/**
	 * Mise à jour de SPSORC pour les bulletins de salaires <br />
	 * utile a SIRH-JOBS
	 */
	@ResponseBody
	@RequestMapping(value = "/miseAJourSpsorc", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public ReturnMessageDto miseAJourSpsorc(@RequestParam("idAgent") int idAgent) {

		logger.debug("entered GET [reposcomps/miseAJourSpsorc] => miseAJourSpsorc with parameters idAgent = {}",
				idAgent);
		
		Integer convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		ReturnMessageDto srm = absenceService.miseAJourSpsorc(convertedIdAgent);

		return srm;
	}

	/**
	 * Retourne l'historique des alimentations auto de reops compensateurs <br />
	 * RequestBody : Format du type timestamp : "/Date(1396306800000+1100)/"
	 */
	@ResponseBody
	@RequestMapping(value = "/getHistoAlimAutoReposComp", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<MoisAlimAutoCongesAnnuelsDto> getHistoAlimAutoReposComp(@RequestParam("idAgent") int idAgent,
			HttpServletResponse response) {

		logger.debug(
				"entered GET [recuperations/getHistoAlimAutoReposComp] => getHistoAlimAutoReposComp with parameters idAgent = {}",
				idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		return absenceService.getHistoAlimAutoReposComp(convertedIdAgent);
	}
}
