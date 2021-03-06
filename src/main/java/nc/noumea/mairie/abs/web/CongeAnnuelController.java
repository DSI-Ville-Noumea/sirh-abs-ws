package nc.noumea.mairie.abs.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.MoisAlimAutoCongesAnnuelsDto;
import nc.noumea.mairie.abs.dto.RefAlimCongesAnnuelsDto;
import nc.noumea.mairie.abs.dto.RestitutionMassiveDto;
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
@RequestMapping("/congeannuel")
public class CongeAnnuelController {

	private Logger logger = LoggerFactory.getLogger(CongeAnnuelController.class);

	@Autowired
	@Qualifier("CongeAnnuelCounterServiceImpl")
	private ICounterService counterService;

	@Autowired
	private IAgentMatriculeConverterService converterService;

	@Autowired
	private ISirhWSConsumer sirhWSConsumer;

	@Autowired
	private IAbsenceService absenceService;

	/**
	 * Initialise le compteur de congé annuel si l'agent n'en avait pas. <br />
	 * utile a SIRH lors de la creation d'une toute premiere affectation d'un
	 * agent
	 */
	@ResponseBody
	@RequestMapping(value = "/intitCompteurCongeAnnuel", method = RequestMethod.POST)
	public ReturnMessageDto initCompteurCongeAnnuel(@RequestParam(value = "idAgent", required = true) Integer idAgent,
			@RequestParam(value = "idAgentConcerne", required = true) Integer idAgentConcerne,
			HttpServletResponse response) {

		logger.debug(
				"entered GET [congeannuel/intitCompteurCongeAnnuel] => initCompteurCongeAnnuel with parameters idAgent = {}, idAgentConcerne = {}",
				idAgent, idAgentConcerne);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);
		int convertedIdAgentConcerne = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgentConcerne);

		ReturnMessageDto srm = counterService.initCompteurCongeAnnuel(convertedIdAgent, convertedIdAgentConcerne);

		if (!srm.getErrors().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_CONFLICT);
		}

		return srm;
	}

	/**
	 * Liste des compteurs a remettre a zero
	 */
	@ResponseBody
	@RequestMapping(value = "/getListeCompteurCongeAnnuel", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<Integer> getListeCompteurCongeAnnuel() {

		logger.debug("entered GET [congeannuel/getListeCompteurCongeAnnuel] => getListeCompteurCongeAnnuel");

		List<Integer> listCompteur = counterService.getListAgentCongeAnnuelCountForReset();

		return listCompteur;
	}

	/**
	 * Remise a zero des compteurs de conges annuels de l annee en cours <br />
	 * Et report vers le compteur de l annee precedente
	 * utile a SIRH-JOBS le 31/12
	 */
	@ResponseBody
	@RequestMapping(value = "/resetCompteurCongeAnnuel", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public ReturnMessageDto resetCompteurCongeAnnuel(
			@RequestParam("idAgentCongeAnnuelCount") int idAgentCongeAnnuelCount, HttpServletResponse response) {

		logger.debug(
				"entered POST [congeannuel/resetCompteurCongeAnnuel] => resetCompteurCongeAnnuel with parameters idAgentCongeAnnuelCount = {}",
				idAgentCongeAnnuelCount);

		ReturnMessageDto srm = counterService.resetCompteurCongeAnnuel(idAgentCongeAnnuelCount);

		return srm;
	}

	/**
	 * Ajout/retire manuel au compteur de recuperation pour un agent <br />
	 * RequestBody : Format du type timestamp : "/Date(1396306800000+1100)/"
	 */
	@ResponseBody
	@RequestMapping(value = "/addManual", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto addCongeAnnuelManuelForAgent(@RequestParam("idAgent") int idAgent,
			@RequestBody(required = true) CompteurDto compteurDto, HttpServletResponse response) {

		logger.debug(
				"entered POST [congeannuel/addManual] => addCongeAnnuelManuelForAgent with parameters idAgent = {}",
				idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		ReturnMessageDto srm = counterService.majManuelleCompteurToAgent(convertedIdAgent, compteurDto, false);

		if (!srm.getErrors().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_CONFLICT);
		}

		return srm;
	}

	/**
	 * Ajout/retire manuel au compteur de recuperation pour un agent <br />
	 * RequestBody : Format du type timestamp : "/Date(1396306800000+1100)/"
	 */
	@ResponseBody
	@RequestMapping(value = "/alimentationAutoCongesAnnuels", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto alimentationAutoCongesAnnuels(@RequestParam("nomatr") Integer nomatrAgent,
			@RequestParam(value = "dateDebut", required = true) @DateTimeFormat(pattern = "yyyyMMdd") Date dateDebut,
			@RequestParam(value = "dateFin", required = true) @DateTimeFormat(pattern = "yyyyMMdd") Date dateFin,
			HttpServletResponse response) {

		logger.debug(
				"entered POST [congeannuel/alimentationAutoCongesAnnuels] => alimentationAutoCongesAnnuels with parameters nomatr = {}, dateDebut = {}, dateFin = {}",
				nomatrAgent, dateDebut, dateFin);

		Integer convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(nomatrAgent);

		return counterService.alimentationAutoCompteur(convertedIdAgent, dateDebut, dateFin);
	}

	/**
	 * Pour connaire sur une periode données si un agent est en conge annuel <br />
	 * Parametres en entree : format du type timestamp : dd/MM/yyyy HH:mm
	 */
	@ResponseBody
	@RequestMapping(value = "/checkCongesAnnuels", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public ReturnMessageDto checkCongesAnnuels(
			@RequestParam("idAgent") int idAgent,
			@RequestParam(value = "dateDebut", required = false) @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm") Date fromDate,
			@RequestParam(value = "dateFin", required = false) @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm") Date toDate) {

		logger.debug(
				"entered GET [congeannuel/checkCongesAnnuels] => checkCongesAnnuels with parameters idAgent = {}, dateDebut = {}, dateFin = {}",
				idAgent, fromDate, toDate);

		Integer convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		AgentGeneriqueDto agent = sirhWSConsumer.getAgent(convertedIdAgent);
		if (agent == null || agent.getIdAgent() == null)
			throw new NotFoundException();

		ReturnMessageDto result = absenceService.checkCongesAnnuels(convertedIdAgent, fromDate, toDate);

		return result;
	}

	/**
	 * Pour connaire sur les mois sur lesquels la routine d'alimentation auto de
	 * fin de mois des conges annuels est passée
	 */
	@ResponseBody
	@RequestMapping(value = "/getListeMoisAlimAutoCongeAnnuel", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<MoisAlimAutoCongesAnnuelsDto> getListeMoisAlimAutoCongeAnnuel() {

		logger.debug("entered GET [congeannuel/getListeMoisAlimAutoCongeAnnuel] => getListeMoisAlimAutoCongeAnnuel ");

		List<MoisAlimAutoCongesAnnuelsDto> result = absenceService.getListeMoisAlimAutoCongeAnnuel();

		return result;
	}

	/**
	 * Restitution masive de conge annuel <br />
	 * RequestBody : Format du type timestamp : "/Date(1396306800000+1100)/"
	 */
	@ResponseBody
	@RequestMapping(value = "/restitutionMassive", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto addRestitutionMassive(@RequestParam("idAgent") int idAgent,
			@RequestBody(required = true) RestitutionMassiveDto dto, HttpServletResponse response) {

		logger.debug(
				"entered POST [congeannuel/restitutionMassive] => addRestitutionMassive with parameters idAgent = {}",
				idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		ReturnMessageDto srm = new ReturnMessageDto();

		// ///////////////////////////////////
		// on check le DTO
		srm = counterService.checkRestitutionMassiveDto(dto, srm);
		if (0 < srm.getErrors().size()) {
			return srm;
		}

		// rechercher les agents concernés
		List<Integer> listIdAgent = new ArrayList<Integer>();
		listIdAgent = absenceService.getListeIdAgentConcerneRestitutionMassive(dto);

		srm = counterService.restitutionMassiveCA(convertedIdAgent, dto, listIdAgent);
		return srm;
	}

	/**
	 * Retourne l'historique des restitutions masives de conge annuel <br />
	 * RequestBody : Format du type timestamp : "/Date(1396306800000+1100)/"
	 */
	@ResponseBody
	@RequestMapping(value = "/getHistoRestitutionMassive", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<RestitutionMassiveDto> getHistoRestitutionMassive(@RequestParam("idAgent") int idAgent,
			HttpServletResponse response) {

		logger.debug(
				"entered GET [congeannuel/getHistoRestitutionMassive] => getHistoRestitutionMassive with parameters idAgent = {}",
				idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		return counterService.getHistoRestitutionMassiveCA(convertedIdAgent);
	}

	/**
	 * #15586
	 * Retourne l'historique des restitutions masives de conge annuel pour un agent <br />
	 * RequestBody : Format du type timestamp : "/Date(1396306800000+1100)/"
	 */
	@ResponseBody
	@RequestMapping(value = "/getHistoRestitutionMassiveByIdAgent", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<RestitutionMassiveDto> getHistoRestitutionMassiveByIdAgent(@RequestParam("idAgent") int idAgent,
			HttpServletResponse response) {

		logger.debug(
				"entered GET [congeannuel/getHistoRestitutionMassiveByIdAgent] => getHistoRestitutionMassiveByIdAgent with parameters idAgent = {}",
				idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		return counterService.getHistoRestitutionMassiveCAByAgent(convertedIdAgent);
	}

	/**
	 * Retourne l'historique des restitutions masives de conge annuel <br />
	 * RequestBody : Format du type timestamp : "/Date(1396306800000+1100)/"
	 */
	@ResponseBody
	@RequestMapping(value = "/getDetailsHistoRestitutionMassive", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public RestitutionMassiveDto getDetailsHistoRestitutionMassive(@RequestParam("idAgent") int idAgent,
			@RequestBody(required = true) RestitutionMassiveDto dto, HttpServletResponse response) {

		logger.debug(
				"entered GET [congeannuel/getDetailsHistoRestitutionMassive] => getDetailsHistoRestitutionMassive with parameters idAgent = {}",
				idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		return counterService.getDetailsHistoRestitutionMassive(convertedIdAgent, dto);
	}

	/**
	 * Pour un mois donné, donne toutes les informations d'alimenation auto des
	 * congés annuels <br />
	 * RequestBody : Format du type timestamp : "/Date(1396306800000+1100)/"
	 */
	@ResponseBody
	@RequestMapping(value = "/getListeAlimAutoCongeAnnuel", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public List<MoisAlimAutoCongesAnnuelsDto> getListeAlimAutoCongeAnnuel(
			@RequestParam("onlyErreur") boolean onlyErreur,
			@RequestBody(required = true) MoisAlimAutoCongesAnnuelsDto moisAlimAutoDto, HttpServletResponse response) {

		logger.debug(
				"entered POST [congeannuel/getListeAlimAutoCongeAnnuel] => alimentationAutoCongesAnnuels with parameter moisAlimAutoDto = {}",
				moisAlimAutoDto.getDtoToString(moisAlimAutoDto));

		List<MoisAlimAutoCongesAnnuelsDto> result = absenceService.getListeAlimAutoCongeAnnuelByMois(
				moisAlimAutoDto.getDateMois(), onlyErreur);

		return result;
	}

	/**
	 * Retourne l'historique des alimentations auto de conge annuel <br />
	 * RequestBody : Format du type timestamp : "/Date(1396306800000+1100)/"
	 */
	@ResponseBody
	@RequestMapping(value = "/getHistoAlimAutoCongeAnnuel", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<MoisAlimAutoCongesAnnuelsDto> getHistoAlimAutoCongeAnnuel(@RequestParam("idAgent") int idAgent,
			HttpServletResponse response) {

		logger.debug(
				"entered GET [congeannuel/getHistoAlimAutoCongeAnnuel] => getHistoAlimAutoCongeAnnuel with parameters idAgent = {}",
				idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		return absenceService.getHistoAlimAutoCongeAnnuel(convertedIdAgent);
	}

	/**
	 * Liste de toutes les alimentations mensuelles par Base Congé
	 */
	@ResponseBody
	@RequestMapping(value = "/getListRefAlimCongeAnnuel", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<RefAlimCongesAnnuelsDto> getListRefAlimCongeAnnuel(
			@RequestParam(required = true) Integer idRefTypeSaisiCongeAnnuel) {

		logger.debug(
				"entered GET [congeannuel/getListRefAlimCongeAnnuel] => getListRefAlimCongeAnnuel with parameter idRefTypeSaisiCongeAnnuel = {}",
				idRefTypeSaisiCongeAnnuel);

		List<RefAlimCongesAnnuelsDto> result = absenceService
				.getListeRefAlimCongeAnnuelByBaseConge(idRefTypeSaisiCongeAnnuel);

		return result;
	}

	/**
	 * Ajout ou modification de la table abs_ref_alim_conges_annuels <br />
	 */
	@ResponseBody
	@RequestMapping(value = "/setRefAlimCongeAnnuel", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto setRefAlimCongeAnnuel(
			@RequestBody(required = true) RefAlimCongesAnnuelsDto refAlimCongesAnnuelsDto,
			@RequestParam("idAgent") int idAgent, HttpServletResponse response) {

		logger.debug(
				"entered POST [congeannuel/setRefAlimCongeAnnuel] => setRefAlimCongeAnnuel with parameter refAlimCongesAnnuelsDto = {} and idAgent = {}",
				refAlimCongesAnnuelsDto.getDtoToString(refAlimCongesAnnuelsDto), idAgent);

		Integer convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		ReturnMessageDto result = absenceService.setRefAlimCongeAnnuel(convertedIdAgent, refAlimCongesAnnuelsDto);

		if (null == result)
			throw new NoContentException();

		return result;
	}

	/**
	 * Mise à jour de SPSOLD pour les bulletins de salaires <br />
	 * utile a SIRH-JOBS
	 */
	@ResponseBody
	@RequestMapping(value = "/miseAJourSpsold", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public ReturnMessageDto miseAJourSpsold(@RequestParam("idAgent") int idAgent) {

		logger.debug("entered GET [congeannuel/miseAJourSpsold] => miseAJourSpsold with parameters idAgent = {}",
				idAgent);
		
		Integer convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		ReturnMessageDto srm = absenceService.miseAJourSpsold(convertedIdAgent);

		return srm;
	}

	/**
	 * Création automatique d'une annee dans REF_ALIM_CONGES_ANNUELS avec les
	 * valeurs de l'année précédente <br />
	 * utile a SIRH lors de la creation d'une nouvelle année dans le paramétrage
	 * des jours fériés
	 */
	@ResponseBody
	@RequestMapping(value = "/createRefAlimCongeAnnuelAnnee", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public ReturnMessageDto createRefAlimCongeAnnuelAnnee(@RequestParam("annee") Integer anneeCreation) {

		logger.debug(
				"entered GET [congeannuel/createRefAlimCongeAnnuelAnnee] => createRefAlimCongeAnnuelAnnee with parameters annee = {}",
				anneeCreation);

		ReturnMessageDto srm = absenceService.createRefAlimCongeAnnuelAnnee(anneeCreation);

		return srm;
	}
	
	/**
	 * Retourne la liste des demandes avec l'etat ayant debite ou credite le compteur de CA de l agent <br />
	 * utile a SIRH dans Agents > Absences > Historique Solde CA
	 */
	@ResponseBody
	@RequestMapping(value = "/getListeDemandeCAWhichAddOrRemoveOnCounterAgent", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<DemandeDto> getListeDemandeCAWhichAddOrRemoveOnCounterAgent(@RequestParam("idAgent") int idAgent,
			@RequestParam("idAgentConcerne") int idAgentConcerne) {
		
		logger.debug(
				"entered GET [congeannuel/getListeDemandeCAWhichAddOrRemoveOnCounterAgent] => getListeDemandeCAWhichAddOrRemoveOnCounterAgent with parameter idAgent = {} and idAgentConcerne = {}",
				idAgent, idAgentConcerne);

		Integer convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);
		
		List<DemandeDto> result = absenceService
				.getListDemandesCAToAddOrRemoveOnAgentCounter(convertedIdAgent, idAgentConcerne);

		return result;
	}
}
