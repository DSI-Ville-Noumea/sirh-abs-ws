package nc.noumea.mairie.abs.web;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.EditionDemandeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.dto.SoldeDto;
import nc.noumea.mairie.abs.service.IAbsenceService;
import nc.noumea.mairie.abs.service.IAccessRightsService;
import nc.noumea.mairie.abs.service.IAgentMatriculeConverterService;
import nc.noumea.mairie.abs.service.ISoldeService;
import nc.noumea.mairie.abs.service.ISuppressionService;
import nc.noumea.mairie.abs.service.impl.HelperService;
import nc.noumea.mairie.ws.ISirhWSConsumer;

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
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/demandes")
public class DemandeController {

	private Logger logger = LoggerFactory.getLogger(DemandeController.class);

	@Autowired
	private IAbsenceService absenceService;

	@Autowired
	private ISuppressionService suppressionService;

	@Autowired
	private IAgentMatriculeConverterService converterService;

	@Autowired
	private IAccessRightsService accessRightService;

	@Autowired
	private HelperService helperService;

	@Autowired
	private ISirhWSConsumer sirhWSConsumer;

	@Autowired
	private ISoldeService soldeService;

	/**
	 * Creation/modification d'une demande : SI idDemande IS NULL ALORS creation
	 * SINON modification <br />
	 * RequestBody : Format du type timestamp : "/Date(1396306800000+1100)/"
	 */
	@ResponseBody
	@RequestMapping(value = "/demande", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto setDemandeAbsence(@RequestParam("idAgent") int idAgent,
			@RequestBody(required = true) DemandeDto demandeDto, HttpServletResponse response) {

		logger.debug(
				"entered POST [demandes/demande] => setDemandeAbsence for Kiosque with parameters idAgent = {} and demandeDto={} ",
				idAgent, demandeDto.getDtoToString(demandeDto));

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);
		ReturnMessageDto srm = absenceService.saveDemande(convertedIdAgent, demandeDto);

		if (!srm.getErrors().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_CONFLICT);
		}
		return srm;
	}

	/**
	 * Recuperation d une demande <br />
	 * ResponseBody : Format du type timestamp : "/Date(1396306800000+1100)/"
	 */
	@ResponseBody
	@RequestMapping(value = "/demande", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public DemandeDto getDemandeAbsence(@RequestParam("idAgent") int idAgent, @RequestParam("idDemande") int idDemande) {

		logger.debug(
				"entered GET [demandes/demande] => getDemandeAbsence for Kiosque with parameters idAgent = {} and idDemande = {}",
				idAgent, idDemande);

		DemandeDto result = absenceService.getDemandeDto(idDemande);

		if (null == result)
			throw new NoContentException();

		return result;
	}

	/**
	 * Liste des demandes d un agent <br />
	 * Parametres en entree : format du type timestamp : YYYYMMdd <br />
	 * ResponseBody : Format du type timestamp : "/Date(1396306800000+1100)/"
	 */
	@ResponseBody
	@RequestMapping(value = "/listeDemandesAgent", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<DemandeDto> getListeDemandesAbsenceAgent(
			@RequestParam("idAgent") int idAgent,
			@RequestParam(value = "ongletDemande", required = true) String ongletDemande,
			@RequestParam(value = "from", required = false) @DateTimeFormat(pattern = "YYYYMMdd") Date fromDate,
			@RequestParam(value = "to", required = false) @DateTimeFormat(pattern = "YYYYMMdd") Date toDate,
			@RequestParam(value = "dateDemande", required = false) @DateTimeFormat(pattern = "YYYYMMdd") Date dateDemande,
			@RequestParam(value = "etat", required = false) Integer idRefEtat,
			@RequestParam(value = "type", required = false) Integer idRefType,
			@RequestParam(value = "groupe", required = false) Integer idRefGroupeAbsence) {

		logger.debug(
				"entered GET [demandes/listeDemandesAgent] => getListeDemandesAbsenceAgent with parameters idAgent = {}, ongletDemande = {}, from = {}, to = {}, dateDemande = {}, etat = {}, groupe = {} and type = {}",
				idAgent, ongletDemande, fromDate, toDate, dateDemande, idRefEtat, idRefGroupeAbsence, idRefType);

		Integer convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		AgentGeneriqueDto agent = sirhWSConsumer.getAgent(convertedIdAgent);
		if (agent == null || agent.getIdAgent() == null)
			throw new NotFoundException();

		List<DemandeDto> result = absenceService.getListeDemandes(convertedIdAgent, convertedIdAgent, ongletDemande,
				fromDate, toDate, dateDemande, idRefEtat, idRefType, idRefGroupeAbsence);

		if (result.size() == 0)
			throw new NoContentException();

		return result;
	}

	/**
	 * Retourne une demande au format XML pour le report
	 */
	@ResponseBody
	@RequestMapping(value = "/xml/getDemande", produces = "application/xml", method = RequestMethod.GET)
	public ModelAndView getXmlDemande(@RequestParam("idAgent") int idAgent, @RequestParam("idDemande") int idDemande)
			throws ParseException {

		logger.debug(
				"entered GET [demandes/xml/getDemande] => getXmlDemande with parameters idAgent = {}, idDemande = {}",
				idAgent, idDemande);
		Integer convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		AgentGeneriqueDto agent = sirhWSConsumer.getAgent(convertedIdAgent);
		if (agent == null || agent.getIdAgent() == null)
			throw new NotFoundException();

		DemandeDto demandeDto = absenceService.getDemandeDto(idDemande);

		SoldeDto soldeDto = soldeService.getAgentSolde(demandeDto.getAgentWithServiceDto().getIdAgent(), new Date(),
				new Date(), null);

		AgentWithServiceDto approbateurDto = accessRightService.getApprobateurOfAgent(demandeDto
				.getAgentWithServiceDto().getIdAgent());

		EditionDemandeDto dtoFinal = new EditionDemandeDto(demandeDto, soldeDto, approbateurDto);

		return new ModelAndView("xmlView", "object", dtoFinal);
	}

	/**
	 * Gestion des demandes <br />
	 * Parametres en entree : format du type timestamp : YYYYMMdd <br />
	 * ResponseBody : Format du type timestamp : "/Date(1396306800000+1100)/"
	 */
	@ResponseBody
	@RequestMapping(value = "/listeDemandes", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<DemandeDto> getListeDemandesAbsence(
			@RequestParam("idAgent") int idAgent,
			@RequestParam(value = "ongletDemande", required = true) String ongletDemande,
			@RequestParam(value = "from", required = false) @DateTimeFormat(pattern = "YYYYMMdd") Date fromDate,
			@RequestParam(value = "to", required = false) @DateTimeFormat(pattern = "YYYYMMdd") Date toDate,
			@RequestParam(value = "dateDemande", required = false) @DateTimeFormat(pattern = "YYYYMMdd") Date dateDemande,
			@RequestParam(value = "etat", required = false) Integer idRefEtat,
			@RequestParam(value = "type", required = false) Integer idRefType,
			@RequestParam(value = "groupe", required = false) Integer idRefGroupeAbsence,
			@RequestParam(value = "idAgentRecherche", required = false) Integer idAgentRecherche) {

		logger.debug(
				"entered GET [demandes/listeDemandes] => getListeDemandesAbsence with parameters idInputter = {}, ongletDemande = {}, from = {}, to = {}, dateDemande = {}, etat = {}, type = {}, groupe = {} and idAgentConcerne= {}",
				idAgent, ongletDemande, fromDate, toDate, dateDemande, idRefEtat, idRefType, idRefGroupeAbsence,
				idAgentRecherche);

		Integer convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		AgentGeneriqueDto agent = sirhWSConsumer.getAgent(convertedIdAgent);
		if (agent == null || agent.getIdAgent() == null)
			throw new NotFoundException();

		// ON VERIFIE LES DROITS
		if (idAgentRecherche != null) {
			ReturnMessageDto srm = new ReturnMessageDto();
			if (!accessRightService.verifAccessRightListDemande(convertedIdAgent, idAgentRecherche, srm)) {
				if (!srm.getErrors().isEmpty()) {
					throw new AccessForbiddenException();
				}
			}
		}

		List<DemandeDto> result = absenceService.getListeDemandes(convertedIdAgent, idAgentRecherche, ongletDemande,
				fromDate, toDate, dateDemande, idRefEtat, idRefType, idRefGroupeAbsence);

		if (result.size() == 0)
			throw new NoContentException();

		return result;
	}

	/**
	 * changer l etat d une demande depuis le kiosque pour le VISA et
	 * l'APPROBATION et l'ANNULATION <br />
	 * ResponseBody : Format du type timestamp : "/Date(1396306800000+1100)/"
	 */
	@ResponseBody
	@RequestMapping(value = "/changerEtats", produces = "application/json; charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto setAbsencesEtat(@RequestParam("idAgent") int idAgent,
			@RequestBody(required = true) DemandeEtatChangeDto dto, HttpServletResponse response) {

		logger.debug("entered POST [demandes/changerEtats] => setAbsencesEtat with parameters idAgent = {}", idAgent);

		Integer convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		ReturnMessageDto result = absenceService.setDemandeEtat(convertedIdAgent, dto);

		if (result.getErrors().size() != 0)
			response.setStatus(HttpServletResponse.SC_CONFLICT);

		return result;
	}

	/**
	 * Modifier l etat d une demande pour le PRISE --> utile à SIRH-JOBS
	 */
	@ResponseBody
	@RequestMapping(value = "/updateToEtatPris", produces = "application/json; charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto setAbsencesEtatPris(@RequestParam("idDemande") Integer idDemande,
			HttpServletResponse response) {

		logger.debug(
				"entered POST [demandes/updateToEtatPris] => setAbsencesEtatPris for SIRH-JOBS with parameters idDemande = {}",
				idDemande);

		ReturnMessageDto result = absenceService.setDemandeEtatPris(idDemande);

		if (result.getErrors().size() != 0)
			response.setStatus(HttpServletResponse.SC_CONFLICT);

		return result;
	}

	/**
	 * Supprime une demande
	 */
	@ResponseBody
	@RequestMapping(value = "/deleteDemande", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public ReturnMessageDto supprimerDemande(@RequestParam("idAgent") int idAgent,
			@RequestParam("idDemande") int idDemande, HttpServletResponse response) {

		logger.debug(
				"entered GET [demandes/deleteDemande] => supprimerDemande with parameters idAgent = {}, idDemande = {}",
				idAgent, idDemande);

		Integer convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		ReturnMessageDto result = suppressionService.supprimerDemande(convertedIdAgent, idDemande);

		if (result.getErrors().size() != 0)
			response.setStatus(HttpServletResponse.SC_CONFLICT);

		return result;
	}

	/**
	 * Supprime les demandes a l etat Provisoire avec date >= DateDuJour <br />
	 * Utile à SIRH-JOBS
	 */
	@ResponseBody
	@RequestMapping(value = "/supprimerDemandeProvisoire", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto supprimerAbsenceProvisoire(@RequestParam("idDemande") Integer idDemande,
			HttpServletResponse response) {

		logger.debug(
				"entered POST [demandes/supprimerDemandeProvisoire] => supprimerAbsenceProvisoire for SIRH-JOBS with parameters idDemande = {}",
				idDemande);

		ReturnMessageDto result = suppressionService.supprimerDemandeEtatProvisoire(idDemande);

		if (result.getErrors().size() != 0)
			response.setStatus(HttpServletResponse.SC_CONFLICT);

		return result;
	}

	/**
	 * Saisie et modification d une demande d absence depuis SIRH : SI idDemande
	 * IS NULL ALORS creation SINON modification <br />
	 * RequestBody : Format du type timestamp : "/Date(1396306800000+1100)/"
	 */
	@ResponseBody
	@RequestMapping(value = "/demandeSIRH", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto setDemandeAbsenceSIRH(@RequestParam("idAgent") int idAgent,
			@RequestBody(required = true) DemandeDto demandeDto, HttpServletResponse response) {

		logger.debug(
				"entered POST [demandes/demandeSIRH] => setDemandeAbsenceSIRH for SIRH with parameters idAgent = {}",
				idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);
		ReturnMessageDto result = absenceService.saveDemandeSIRH(convertedIdAgent, demandeDto);

		if (result.getErrors().size() != 0)
			response.setStatus(HttpServletResponse.SC_CONFLICT);

		return result;
	}

	/**
	 * Liste des demandes pour SIRH <br />
	 * Parametres en entree : format du type timestamp : YYYYMMdd <br />
	 * ResponseBody : Format du type timestamp : "/Date(1396306800000+1100)/"
	 */
	@ResponseBody
	@RequestMapping(value = "/listeDemandesSIRH", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<DemandeDto> getListeDemandesAbsenceSIRH(
			@RequestParam(value = "from", required = false) @DateTimeFormat(pattern = "YYYYMMdd") Date fromDate,
			@RequestParam(value = "to", required = false) @DateTimeFormat(pattern = "YYYYMMdd") Date toDate,
			@RequestParam(value = "etat", required = false) Integer idRefEtat,
			@RequestParam(value = "type", required = false) Integer idRefType,
			@RequestParam(value = "idAgentRecherche", required = false) Integer idAgentRecherche,
			@RequestParam(value = "groupe", required = false) Integer idRefGroupeAbsence,
			@RequestParam(value = "aValider", required = true) boolean aValider) {

		logger.debug(
				"entered GET [demandes/listeDemandesSIRH] => getListeDemandesAbsenceSIRH with parameters  from = {}, to = {},  etat = {}, groupe = {}, type = {}, idAgentConcerne= {} and aValider= {}",
				fromDate, toDate, idRefEtat, idRefGroupeAbsence, idRefType, idAgentRecherche, aValider);

		List<DemandeDto> result = new ArrayList<DemandeDto>();
		if (aValider) {
			result = absenceService.getListeDemandesSIRHAValider();
		} else {
			result = absenceService.getListeDemandesSIRH(fromDate, toDate, idRefEtat, idRefType, idAgentRecherche,
					idRefGroupeAbsence);
		}

		if (result.size() == 0)
			throw new NoContentException();

		return result;
	}

	/**
	 * Historique d une demande <br />
	 * ResponseBody : Format du type timestamp : "/Date(1396306800000+1100)/"
	 */
	@ResponseBody
	@RequestMapping(value = "/historiqueSIRH", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<DemandeDto> getDemandesArchives(@RequestParam("idDemande") Integer idDemande) {

		logger.debug("entered GET [demandes/historiqueSIRH] => getDemandesArchives with parameter idDemande = {}",
				idDemande);

		List<DemandeDto> result = absenceService.getDemandesArchives(idDemande);

		if (result.size() == 0)
			throw new NoContentException();

		return result;
	}

	/**
	 * Change l etat d une demande depuis SIRH pour VALIDER ou REJETER <br />
	 * RequestBody : Format du type timestamp : "/Date(1396306800000+1100)/"
	 */
	@ResponseBody
	@RequestMapping(value = "/changerEtatsSIRH", produces = "application/json; charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto setAbsencesEtatSIRH(@RequestParam("idAgent") int idAgent,
			@RequestBody(required = true) List<DemandeEtatChangeDto> dto, HttpServletResponse response) {

		logger.debug("entered POST [demandes/changerEtatsSIRH] => setAbsencesEtatSIRH with parameters idAgent = {}",
				idAgent);

		Integer convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		ReturnMessageDto result = absenceService.setDemandeEtatSIRH(convertedIdAgent, dto);

		if (result.getErrors().size() != 0)
			response.setStatus(HttpServletResponse.SC_CONFLICT);

		return result;
	}
}
