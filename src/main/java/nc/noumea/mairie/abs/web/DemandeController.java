package nc.noumea.mairie.abs.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import com.sun.jersey.multipart.FormDataParam;

import nc.noumea.mairie.abs.domain.DemandeCongesAnnuels;
import nc.noumea.mairie.abs.domain.RefTypeSaisiCongeAnnuel;
import nc.noumea.mairie.abs.dto.AgentDto;
import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.ControleMedicalDto;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.ResultListDemandeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDemandeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDtoException;
import nc.noumea.mairie.abs.repository.IFiltreRepository;
import nc.noumea.mairie.abs.service.IAbsenceService;
import nc.noumea.mairie.abs.service.IAccessRightsService;
import nc.noumea.mairie.abs.service.IAgentMatriculeConverterService;
import nc.noumea.mairie.abs.service.ISuppressionService;
import nc.noumea.mairie.abs.service.impl.FiltreService;
import nc.noumea.mairie.abs.service.impl.HelperService;
import nc.noumea.mairie.ws.ISirhWSConsumer;

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
	private IFiltreRepository filtreRepository;

	/**
	 * Creation/modification d'une demande : SI idDemande IS NULL ALORS creation
	 * SINON modification <br />
	 * RequestBody : Format du type timestamp : "/Date(1396306800000+1100)/"
	 */
	@ResponseBody
	@RequestMapping(value = "/dureeDemandeCongeAnnuel", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public DemandeDto getDureeDemandeAbsenceCongeAnnuel(@RequestBody(required = true) DemandeDto demandeDto,
			HttpServletResponse response) {

		logger.debug(
				"entered POST [demandes/dureeDemandeCongeAnnuel] => getDureeDemandeAbsenceCongeAnnuel for Kiosque with parameters  demandeDto={} ",
				demandeDto.getDtoToString(demandeDto));

		RefTypeSaisiCongeAnnuel typeCongeAnnuel = filtreRepository.getEntity(RefTypeSaisiCongeAnnuel.class,
				demandeDto.getTypeSaisiCongeAnnuel().getIdRefTypeSaisiCongeAnnuel());

		demandeDto.setDateDebut(helperService.getDateDebutCongeAnnuel(typeCongeAnnuel, demandeDto.getDateDebut(),
				demandeDto.isDateDebutAM(), demandeDto.isDateDebutPM()));
		demandeDto.setDateFin(
				helperService.getDateFinCongeAnnuel(typeCongeAnnuel, demandeDto.getDateFin(), demandeDto.getDateDebut(),
						demandeDto.isDateFinAM(), demandeDto.isDateFinPM(), demandeDto.getDateReprise()));

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setIdDemande(demandeDto.getIdDemande());
		demande.setIdAgent(demandeDto.getAgentWithServiceDto().getIdAgent());
		demande.setDateDebut(demandeDto.getDateDebut());
		demande.setDateFin(demandeDto.getDateFin());
		demande.setTypeSaisiCongeAnnuel(typeCongeAnnuel);

		Double duree = helperService.getDureeCongeAnnuel(demande, demandeDto.getDateReprise(), false, null);

		DemandeDto res = new DemandeDto();
		res.setDuree(null == duree || duree < 0 ? 0.0 : duree);
		res.setSamediOffert(helperService.getNombreSamediOffert(demande) == 0.0 ? false : true);

		return res;
	}

	/**
	 * Creation/modification d'une demande : SI idDemande IS NULL ALORS creation
	 * SINON modification <br />
	 * RequestBody : Format du type timestamp : "/Date(1396306800000+1100)/"
	 */
	@ResponseBody
	@RequestMapping(value = "/demande", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDemandeDto setDemandeAbsence(@RequestParam("idAgent") int idAgent,
			@RequestBody(required = true) DemandeDto demandeDto, HttpServletResponse response) {

		logger.debug(
				"entered POST [demandes/demande] => setDemandeAbsence for Kiosque with parameters idAgent = {} and demandeDto={} ",
				idAgent, demandeDto.getDtoToString(demandeDto));

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);
		ReturnMessageDemandeDto srm = null;
		try {
			srm = absenceService.saveDemande(convertedIdAgent, demandeDto);
		} catch (ReturnMessageDtoException e) {
			srm = new ReturnMessageDemandeDto(e.getErreur());
		}

		if (srm != null && !srm.getErrors().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_CONFLICT);
		}
		return srm;
	}

	/**
	 * Recuperation d une demande <br />
	 * ResponseBody : Format du type timestamp : "/Date(1396306800000+1100)/"
	 * ATTENTION UTILISE DANS SIRH-JOBS
	 */
	@ResponseBody
	@RequestMapping(value = "/demande", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public DemandeDto getDemandeAbsence(@RequestParam("idAgent") int idAgent,
			@RequestParam("idDemande") int idDemande) {

		logger.debug(
				"entered GET [demandes/demande] => getDemandeAbsence for Kiosque with parameters idAgent = {} and idDemande = {}",
				idAgent, idDemande);

		DemandeDto result = absenceService.getDemandeDto(idDemande);

		if (null == result)
			throw new NoContentException();

		return result;
	}

	/**
	 * Historique d'une demande d absence <br />
	 * ResponseBody : Format du type timestamp : "/Date(1396306800000+1100)/"
	 */
	@ResponseBody
	@RequestMapping(value = "/historique", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<DemandeDto> getAbsenceArchives(@RequestParam("idAgent") int idAgent,
			@RequestParam("idDemande") Integer idDemande) {

		logger.debug(
				"entered GET [demandes/historique] => getAbsenceArchives with parameters idAgent = {} and idDemande = {}",
				idAgent, idDemande);

		Integer convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		AgentGeneriqueDto agent = sirhWSConsumer.getAgent(convertedIdAgent);
		if (agent == null || agent.getIdAgent() == null)
			throw new NotFoundException();

		List<DemandeDto> result = absenceService.getDemandesArchives(idDemande);

		if (result.size() == 0)
			throw new NoContentException();

		return result;
	}

	/**
	 * Liste des demandes d un agent <br />
	 * Parametres en entree : format du type timestamp : yyyyMMdd <br />
	 * ResponseBody : Format du type timestamp : "/Date(1396306800000+1100)/"
	 * 
	 * Ce WS est appelé depuis SIRH et KiosqueRH
	 */
	@ResponseBody
	@RequestMapping(value = "/listeDemandesAgent", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public ResultListDemandeDto getListeDemandesAbsenceAgent(@RequestParam("idAgent") int idAgent,
			@RequestParam(value = "ongletDemande", required = true) String ongletDemande,
			@RequestParam(value = "from", required = false) @DateTimeFormat(pattern = "yyyyMMdd") Date fromDate,
			@RequestParam(value = "to", required = false) @DateTimeFormat(pattern = "yyyyMMdd") Date toDate,
			@RequestParam(value = "dateDemande", required = false) @DateTimeFormat(pattern = "yyyyMMdd") Date dateDemande,
			@RequestParam(value = "etat", required = false) String listIdRefEtat,
			@RequestParam(value = "type", required = false) Integer idRefType,
			@RequestParam(value = "groupe", required = false) Integer idRefGroupeAbsence) {

		logger.debug(
				"entered GET [demandes/listeDemandesAgent] => getListeDemandesAbsenceAgent with parameters idAgent = {}, ongletDemande = {}, from = {}, to = {}, dateDemande = {}, etat = {}, groupe = {} and type = {}",
				idAgent, ongletDemande, fromDate, toDate, dateDemande, listIdRefEtat, idRefGroupeAbsence, idRefType);

		Integer convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		AgentGeneriqueDto agent = sirhWSConsumer.getAgent(convertedIdAgent);
		if (agent == null || agent.getIdAgent() == null)
			throw new NotFoundException();

		ResultListDemandeDto result = absenceService.getListeDemandes(convertedIdAgent, Arrays.asList(convertedIdAgent),
				ongletDemande, fromDate, toDate, dateDemande, listIdRefEtat, idRefType, idRefGroupeAbsence, true);

		return result;
	}

	/**
	 * Gestion des demandes <br />
	 * Parametres en entree : format du type timestamp : yyyyMMdd <br />
	 * ResponseBody : Format du type timestamp : "/Date(1396306800000+1100)/"
	 * 
	 * Ce WS est appelé depuis le KiosqueRH uniquement
	 */
	@ResponseBody
	@RequestMapping(value = "/listeDemandes", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public ResultListDemandeDto getListeDemandesAbsence(@RequestParam("idAgent") int idAgent,
			@RequestParam(value = "ongletDemande", required = true) String ongletDemande,
			@RequestParam(value = "from", required = false) @DateTimeFormat(pattern = "yyyyMMdd") Date fromDate,
			@RequestParam(value = "to", required = false) @DateTimeFormat(pattern = "yyyyMMdd") Date toDate,
			@RequestParam(value = "dateDemande", required = false) @DateTimeFormat(pattern = "yyyyMMdd") Date dateDemande,
			@RequestParam(value = "etat", required = false) String listIdRefEtat,
			@RequestParam(value = "type", required = false) Integer idRefType,
			@RequestParam(value = "groupe", required = false) Integer idRefGroupeAbsence,
			@RequestParam(value = "idAgentRecherche", required = false) Integer idAgentRecherche,
			@RequestParam(value = "idServiceADS", required = false) Integer idServiceADS) {

		logger.debug(
				"entered GET [demandes/listeDemandes] => getListeDemandesAbsence with parameters idInputter = {}, ongletDemande = {}, from = {}, to = {}, dateDemande = {}, etat = {}, type = {}, groupe = {}, idAgentConcerne = {} and idServiceADS = {}",
				idAgent, ongletDemande, fromDate, toDate, dateDemande, listIdRefEtat, idRefType, idRefGroupeAbsence,
				idAgentRecherche, idServiceADS);

		Integer convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		AgentGeneriqueDto agent = sirhWSConsumer.getAgent(convertedIdAgent);
		if (agent == null || agent.getIdAgent() == null)
			throw new NotFoundException();

		List<Integer> listAgents = new ArrayList<Integer>();
		// ON VERIFIE LES DROITS
		if (idAgentRecherche != null) {
			ReturnMessageDto srm = new ReturnMessageDto();
			if (!accessRightService.verifAccessRightListDemande(convertedIdAgent, idAgentRecherche, srm)) {
				if (!srm.getErrors().isEmpty()) {
					throw new AccessForbiddenException();
				}
			} else {
				listAgents.add(idAgentRecherche);
			}
		} else if (idServiceADS != null) {
			// #16262 : on cherche tous les agents qui ont ce service
			List<Integer> listAgentService = accessRightService.getListAgentByService(idServiceADS, new Date());
			for (Integer idAgentService : listAgentService) {
				if (accessRightService.verifAccessRightListDemande(convertedIdAgent, idAgentService,
						new ReturnMessageDto())) {
					listAgents.add(idAgentService);
				}
			}
		} else {
			for (AgentDto da : accessRightService.getAgentsToApproveOrInputByService(convertedIdAgent, null)) {
				if (!listAgents.contains(da.getIdAgent()))
					listAgents.add(da.getIdAgent());
			}

		}

		ResultListDemandeDto result = absenceService.getListeDemandes(convertedIdAgent,
				listAgents.size() == 0 ? null : listAgents, ongletDemande, fromDate, toDate, dateDemande, listIdRefEtat,
				idRefType, idRefGroupeAbsence, false);
		
		return result;
	}

	/**
	 * Renvoie le nombre de demandes NON PRISES à approuver ou viser pour la page d accueil du Kiosque RH <br />
	 * Parametres en entree : format du type timestamp : yyyyMMdd <br />
	 * ResponseBody : Format du type timestamp : "/Date(1396306800000+1100)/"
	 * 
	 * Ce WS est appelé depuis le KiosqueRH page accueil uniquement
	 */
	@ResponseBody
	@RequestMapping(value = "/countDemandesAViserOuApprouver", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public Integer countDemandesAViserOuApprouver(@RequestParam("idAgent") int idAgent,
			@RequestParam(value = "viseur") boolean viseur, @RequestParam(value = "approbateur") boolean approbateur) {

		logger.debug(
				"entered GET [demandes/countDemandesAViserOuApprouver] => countDemandesAViserOuApprouver with parameters idAgent = {}, viseur = {}, approbateur = {}",
				idAgent, viseur, approbateur);

		Integer convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		AgentGeneriqueDto agent = sirhWSConsumer.getAgent(convertedIdAgent);
		if (agent == null || agent.getIdAgent() == null)
			throw new NotFoundException();

		List<Integer> listAgents = new ArrayList<Integer>();
		
		for (AgentDto da : accessRightService.getAgentsToApproveOrInputByService(convertedIdAgent, null)) {
			if (!listAgents.contains(da.getIdAgent()))
				listAgents.add(da.getIdAgent());
		}

		Integer result = absenceService.countDemandesAViserOuApprouver(
				convertedIdAgent, listAgents.isEmpty() ? null : listAgents, viseur, approbateur);
		
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
	
	@ResponseBody
	@RequestMapping(value = "/listeATReferenceForAgent", produces = "application/json; charset=utf-8", method = RequestMethod.GET)
	public List<DemandeDto> getListeATReferenceForAgent(@RequestParam("idAgent") int idAgent,
			HttpServletResponse response) {

		logger.debug("entered GET [demandes/listeATReferenceForAgent] => setATReference with parameters idAgent = {}", idAgent);

		Integer convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		return absenceService.getListeATReferenceForAgent(convertedIdAgent);
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

		ReturnMessageDto result = null;
		try {
			result = absenceService.saveDemandeSIRH(convertedIdAgent, demandeDto);
		} catch (ReturnMessageDtoException e) {
			result = e.getErreur();
		}

		if (result != null && result.getErrors().size() != 0)
			response.setStatus(HttpServletResponse.SC_CONFLICT);

		return result;
	}

	/**
	 * Liste des demandes pour SIRH <br />
	 * Parametres en entree : format du type timestamp : yyyyMMdd <br />
	 * ResponseBody : Format du type timestamp : "/Date(1396306800000+1100)/"
	 * 
	 * Ce WS est appelé depuis SIRH et PTG
	 */
	@ResponseBody
	@RequestMapping(value = "/listeDemandesSIRH", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<DemandeDto> getListeDemandesAbsenceSIRH(
			@RequestParam(value = "from", required = false) @DateTimeFormat(pattern = "yyyyMMdd") Date fromDate,
			@RequestParam(value = "to", required = false) @DateTimeFormat(pattern = "yyyyMMdd") Date toDate,
			@RequestParam(value = "etat", required = false) Integer idRefEtat,
			@RequestParam(value = "type", required = false) Integer idRefType,
			@RequestParam(value = "idAgentRecherche", required = false) Integer idAgentRecherche,
			@RequestParam(value = "groupe", required = false) Integer idRefGroupeAbsence,
			@RequestParam(value = "aValider", required = true) boolean aValider,
			@RequestParam(value = "idAgents", required = false) String idAgents) {

		logger.debug(
				"entered GET [demandes/listeDemandesSIRH] => getListeDemandesAbsenceSIRH with parameters  from = {}, to = {},  etat = {}, groupe = {}, type = {}, idAgentConcerne= {}, aValider= {} and idAgents = {}",
				fromDate, toDate, idRefEtat, idRefGroupeAbsence, idRefType, idAgentRecherche, aValider, idAgents);

		List<Integer> agentIds = new ArrayList<Integer>();
		if (idAgents != null) {
			for (String id : idAgents.split(",")) {
				if (!"".equals(id)) {
					agentIds.add(Integer.valueOf(id));
				}
			}
		}

		List<DemandeDto> result = new ArrayList<DemandeDto>();
		if (aValider) {
			result = absenceService.getListeDemandesSIRHAValider(fromDate, toDate, idRefEtat, idRefType,
					idAgentRecherche, idRefGroupeAbsence, agentIds);
		} else {
			result = absenceService.getListeDemandesSIRH(fromDate, toDate, idRefEtat, idRefType, idAgentRecherche,
					idRefGroupeAbsence, agentIds, null, null);
		}

		if (result.size() == 0)
			throw new NoContentException();

		return result;
	}

	/**
	 * Liste des demandes pour le planning du KiosqueRH <br />
	 * Parametres en entree : format du type timestamp : yyyyMMdd <br />
	 * ResponseBody : Format du type timestamp : "/Date(1396306800000+1100)/"
	 */
	@ResponseBody
	@RequestMapping(value = "/listeDemandesPlanningKiosque", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<DemandeDto> getListeDemandesAbsencePlanningKiosque(
			@RequestParam(value = "from", required = false) @DateTimeFormat(pattern = "yyyyMMdd") Date fromDate,
			@RequestParam(value = "to", required = false) @DateTimeFormat(pattern = "yyyyMMdd") Date toDate,
			@RequestParam(value = "etat", required = false) String listIdRefEtat,
			@RequestParam(value = "type", required = false) Integer idRefType,
			@RequestParam(value = "groupe", required = false) Integer idRefGroupeAbsence,
			@RequestParam(value = "idAgents", required = false) String idAgents) {

		logger.debug(
				"entered GET [demandes/getListeDemandesAbsencePlanningKiosque] => getListeDemandesAbsencePlanningKiosque with parameters  from = {}, to = {},  "
						+ "etat = {}, groupe = {}, type = {}, idAgentConcerne= {}, aValider= {} and idAgents = {}",
				fromDate, toDate, listIdRefEtat, idRefGroupeAbsence, idRefType, idAgents);

		List<Integer> agentIds = new ArrayList<Integer>();
		if (idAgents != null) {
			for (String id : idAgents.split(",")) {
				if (!"".equals(id)) {
					agentIds.add(Integer.valueOf(id));
				}
			}
		}

		List<DemandeDto> result = absenceService.getListeDemandesSIRH(fromDate, toDate, null, idRefType, null,
				idRefGroupeAbsence, agentIds, listIdRefEtat, FiltreService.ONGLET_PLANNING);

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

	/**
	 * Pour connaire sur une periode données si un agent a pose une absence
	 * <br />
	 * Parametres en entree : format du type timestamp : dd/MM/yyyy HH:mm
	 */
	@ResponseBody
	@RequestMapping(value = "/checkAbsences", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public ReturnMessageDto checkAbsences(@RequestParam("idAgent") int idAgent,
			@RequestParam(value = "dateDebut", required = false) @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm") Date fromDate,
			@RequestParam(value = "dateFin", required = false) @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm") Date toDate) {

		logger.debug(
				"entered GET [demandes/checkAbsences] => checkAbsences with parameters idAgent = {}, dateDebut = {}, dateFin = {}",
				idAgent, fromDate, toDate);

		Integer convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		AgentGeneriqueDto agent = sirhWSConsumer.getAgent(convertedIdAgent);
		if (agent == null || agent.getIdAgent() == null)
			throw new NotFoundException();

		ReturnMessageDto result = absenceService.checkAbsences(convertedIdAgent, fromDate, toDate);

		return result;
	}

	/**
	 * Utile à SIRH-JOBS pour l'envoi de mail des demandes "REJET_DRH de la
	 * veille
	 */
	@ResponseBody
	@RequestMapping(value = "/listDemandeRejetDRHStatutVeille", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<DemandeDto> getListDemandeRejetDRHStatutVeille() {

		logger.debug("entered GET [demandes/listDemandeRejetDRHStatutVeille] => getListDemandeRejetDRHStatutVeille ");

		List<DemandeDto> result = absenceService.getListDemandeRejetDRHStatutVeille();

		return result;
	}

	/**
	 * ajout d'une pièce jointe a une demande depuis SIRH quelque soit son etat
	 */
	@ResponseBody
	@RequestMapping(value = "/addPieceJointeSIRH", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto addPieceJointeSIRH(@RequestParam("idAgent") int idAgent,
			@RequestBody(required = true) DemandeDto demandeDto, HttpServletResponse response) {

		logger.debug(
				"entered POST [demandes/addPieceJointeSIRH] => addPieceJointeSIRH for SIRH with parameters idAgent = {}",
				idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		ReturnMessageDto result = null;
		try {
			result = absenceService.addPieceJointeSIRH(convertedIdAgent, demandeDto);
		} catch (ReturnMessageDtoException e) {
			result = e.getErreur();
		}

		if (result != null && result.getErrors().size() != 0)
			response.setStatus(HttpServletResponse.SC_CONFLICT);

		return result;
	}

	/**
	 * sauvegarde du commentaire DRH sur une demande depuis SIRH
	 */
	@ResponseBody
	@RequestMapping(value = "/saveCommentaireDRH", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto saveCommentaireDRH(@RequestParam("idDemande") Integer idDemande,
			@RequestBody String commentaire, HttpServletResponse response) {

		logger.debug(
				"entered POST [demandes/saveCommentaireDRH] => saveCommentaireDRH for SIRH with parameters idDemande = {}",
				idDemande);

		ReturnMessageDto result = null;
		try {
			result = absenceService.saveCommentaireDRH(idDemande,
					commentaire == null || commentaire.trim().equals("") ? null : commentaire);
		} catch (ReturnMessageDtoException e) {
			result = e.getErreur();
		}

		return result;
	}

	/**
	 * sauvegarde d'une demande de contrôle médical.
	 */
	@ResponseBody
	@RequestMapping(value = "/persistDemandeControleMedical", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto persistDemandeControleMedical(@RequestBody ControleMedicalDto dto,
			HttpServletResponse response) {

		logger.debug(
				"entered POST [demandes/persistDemandeControleMedical] => persistDemandeControleMedical for Kiosque and SIRH.");

		ReturnMessageDto result = null;
		try {
			result = absenceService.persistDemandeControleMedical(dto);
		} catch (ReturnMessageDtoException e) {
			result = e.getErreur();
		}

		return result;
	}

	/**
	 * Utile à SIRH-JOBS pour l'envoi de mail des demandes "REJET_DRH de la
	 * veille
	 */
	@ResponseBody
	@RequestMapping(value = "/getDemandeControleMedical", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public ControleMedicalDto getDemandeControleMedical(@RequestParam("idDemandeMaladie") int idDemandeMaladie) {

		logger.debug("entered GET [demandes/getDemandeControleMedical] => getDemandeControleMedical ");

		ControleMedicalDto result = absenceService.getDemandeControleMedical(idDemandeMaladie);

		return result;
	}

	/**
	 * #37756 Evolution pour stabiliser l ajout de pieces jointes au demande d absence
	 * 
	 * Est utilisé a ce jour 29/06/2017 que par le kiosqueRH
	 * 
	 * @param idAgent
	 * @param idPieceJointe
	 * @param inputStream
	 * @return ReturnMessageDto
	 * @throws IOException 
	 */
	@ResponseBody
	@RequestMapping(value = "/savePieceJointeWithStream", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto savePieceJointesWithStream(@RequestParam("idAgent") Integer idAgent, @RequestParam("idAgentOperateur") Integer idAgentOperateur,
			@RequestParam("idDemande") Integer idDemande, @RequestParam("typeFile") String typeFile, 
			@FormDataParam("fileInputStream") InputStream inputStream,
			HttpServletRequest request
			) throws IOException {

		logger.debug(
				"entered POST [demandes/savePieceJointesWithStream] => savePieceJointesWithStream with idAgent {}, idAgentOperateur {}, idDemande {} ",
				idAgent, idAgentOperateur, idDemande);
		
		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);
		int convertedIdAgentOperateur = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgentOperateur);

		ReturnMessageDto returnMessage = null;
		if("application/pdf".equals(typeFile)) {
			logger.info("inputStream.available(); " + inputStream.available());
			returnMessage = absenceService.savePieceJointesWithStream(inputStream, convertedIdAgent, convertedIdAgentOperateur, idDemande, typeFile);
			inputStream.close();
		}else {
			logger.info("request.getInputStream() " +  request.getInputStream().available());
			returnMessage = absenceService.savePieceJointesWithStream(request.getInputStream(), convertedIdAgent, convertedIdAgentOperateur, idDemande, typeFile);
			request.getInputStream().close();
		}
		
		return returnMessage;
	}

}
