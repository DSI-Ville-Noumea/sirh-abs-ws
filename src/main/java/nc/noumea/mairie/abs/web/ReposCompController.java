package nc.noumea.mairie.abs.web;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.service.IAgentMatriculeConverterService;
import nc.noumea.mairie.abs.service.ICounterService;

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

	/**
	 * Ajoute des minutes au compteur de repos compensateur a un agent pour une semaine donnee
	 * <br />
	 * utile a SIRH-PTG-WS
	 * <br />
	 * Parametres en entree : format du type timestamp  : YYYYMMdd
	 */
	@ResponseBody
	@RequestMapping(value = "/addForPTG", method = RequestMethod.POST)
	public void addReposCompForAgentAndWeek(@RequestParam("idAgent") Integer idAgent,
			@RequestParam("dateLundi") @DateTimeFormat(pattern = "YYYYMMdd") Date dateMonday,
			@RequestParam("minutes") int minutes) {

		logger.debug(
				"entered GET [reposcomps/add] => addReposCompForAgentAndWeek with parameters idAgent = {}, dateMonday = {} and minutes = {}",
				idAgent, dateMonday, minutes);

		counterService.addToAgentForPTG(idAgent, dateMonday, minutes);
	}

	/**
	 * Ajout/retire manuel au compteur de repos compensateur pour un agent
	 * <br />
	 * RequestBody : Format du type timestamp : "/Date(1396306800000+1100)/"
	 */
	@ResponseBody
	@RequestMapping(value = "/addManual", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto addRecuperationManuelForAgent(@RequestParam("idAgent") int idAgent,
			@RequestBody(required = true) CompteurDto compteurDto, 
			HttpServletResponse response) {

		logger.debug(
				"entered POST [reposcomps/addManual] => addRecuperationManuelForAgent with parameters idAgent = {}",
				idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		ReturnMessageDto srm = counterService.majManuelleCompteurToAgent(convertedIdAgent, compteurDto);

		if (!srm.getErrors().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_CONFLICT);
		}
		
		return srm;
	}

	/**
	 * Remise a zero des compteurs de repos compensateur de l annee precedente
	 * <br />
	 * utile a SIRH-JOBS le 31/08
	 */
	@ResponseBody
	@RequestMapping(value = "/resetCompteurAnneePrecedente", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public ReturnMessageDto resetCompteurAnneePrecedente(
			@RequestParam("idAgentReposCompCount") int idAgentReposCompCount, 
			HttpServletResponse response) {

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
	 * Remise a zero des compteurs de repos compensateur de l annee en cours
	 * <br />
	 * utile a SIRH-JOBS le 31/12
	 */
	@ResponseBody
	@RequestMapping(value = "/resetCompteurAnneenCours", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public ReturnMessageDto resetCompteurRCAnneenCours(
			@RequestParam("idAgentReposCompCount") int idAgentReposCompCount, 
			HttpServletResponse response) {

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
}
