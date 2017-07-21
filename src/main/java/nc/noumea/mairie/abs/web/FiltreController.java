package nc.noumea.mairie.abs.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import nc.noumea.mairie.abs.domain.Droit;
import nc.noumea.mairie.abs.domain.RefTypeAccidentTravail;
import nc.noumea.mairie.abs.domain.RefTypeMaladiePro;
import nc.noumea.mairie.abs.domain.RefTypeSiegeLesion;
import nc.noumea.mairie.abs.dto.AgentDto;
import nc.noumea.mairie.abs.dto.EntiteDto;
import nc.noumea.mairie.abs.dto.RefEtatDto;
import nc.noumea.mairie.abs.dto.RefGroupeAbsenceDto;
import nc.noumea.mairie.abs.dto.RefTypeAbsenceDto;
import nc.noumea.mairie.abs.dto.RefTypeDto;
import nc.noumea.mairie.abs.dto.RefTypeSaisiDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.dto.UnitePeriodeQuotaDto;
import nc.noumea.mairie.abs.service.IAccessRightsService;
import nc.noumea.mairie.abs.service.IAgentMatriculeConverterService;
import nc.noumea.mairie.abs.service.IFiltreService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/filtres")
public class FiltreController {

	private Logger logger = LoggerFactory.getLogger(FiltreController.class);

	@Autowired
	private IFiltreService filtresService;

	@Autowired
	private IAgentMatriculeConverterService converterService;

	@Autowired
	private IAccessRightsService accessRightsService;

	/**
	 * Liste des etats possibles selon l onglet selectionne
	 */
	@ResponseBody
	@RequestMapping(value = "/getEtats", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<RefEtatDto> getEtats(@RequestParam(value = "ongletDemande", required = false) String ongletDemande) {

		logger.debug("entered GET [filtres/getEtats] => getEtats with parameter ongletDemande = {}", ongletDemande);

		List<RefEtatDto> etats = filtresService.getRefEtats(ongletDemande);

		return etats;
	}

	/**
	 * Liste des types d absence possibles pour un agent donne
	 */
	@ResponseBody
	@RequestMapping(value = "/getTypes", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<RefTypeAbsenceDto> getTypes(@RequestParam(value = "idAgentConcerne", required = false) Integer idAgentConcerne) {

		logger.debug("entered GET [filtres/getTypes] => getTypes with parameter idAgentConcerne = {} ", idAgentConcerne);

		List<RefTypeAbsenceDto> types = filtresService.getRefTypesAbsence(idAgentConcerne);

		return types;
	}

	/**
	 * Liste des services pour un agent donne
	 */
	@ResponseBody
	@RequestMapping(value = "/services", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<EntiteDto> getServices(@RequestParam("idAgent") Integer idAgent) {

		logger.debug("entered GET [filtres/services] => getServices with parameter idAgent = {}", idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		List<EntiteDto> services = accessRightsService.getAgentsServicesToApproveOrInput(convertedIdAgent, new Date());

		if (services.size() == 0)
			throw new NoContentException();

		return services;
	}

	/**
	 * Liste des services pour un agent donne pour la gestion des compteurs
	 * uniquement accessible aux opérateurs
	 */
	@ResponseBody
	@RequestMapping(value = "/servicesOperateur", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<EntiteDto> getServicesOperateur(@RequestParam("idAgent") Integer idAgent) {

		logger.debug("entered GET [filtres/servicesOperateur] => getServicesOperateur with parameter idAgent = {}", idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		List<EntiteDto> services = accessRightsService.getAgentsServicesForOperateur(convertedIdAgent, new Date());

		if (services.size() == 0)
			throw new NoContentException();

		return services;
	}

	/**
	 * Liste des agents affectes a un operateur, viseur ou approbateur selon son
	 * service
	 */
	@ResponseBody
	@RequestMapping(value = "/agents", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<AgentDto> getAgents(@RequestParam("idAgent") Integer idAgent, @RequestParam(value = "idServiceADS", required = false) Integer idServiceADS) {

		logger.debug("entered GET [filtres/agents] => getAgents with parameter idAgent = {} and idServiceADS = {}", idAgent, idServiceADS);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		List<AgentDto> services = accessRightsService.getAgentsToApproveOrInputByService(convertedIdAgent, idServiceADS);

		if (services.size() == 0)
			throw new NoContentException();

		return services;
	}

	/**
	 * Liste des agents affectes a un operateur , selon le service, pour la
	 * gestion des compteurs uniquement accessible aux opérateurs
	 */
	@ResponseBody
	@RequestMapping(value = "/agentsOperateur", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<AgentDto> getAgentsOperateur(@RequestParam("idAgent") Integer idAgent, @RequestParam(value = "idServiceADS", required = false) Integer idServiceADS) {

		logger.debug("entered GET [filtres/agentsOperateur] => getAgentsOperateur with parameter idAgent = {} and idServiceADS = {}", idAgent, idServiceADS);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		List<AgentDto> services = new ArrayList<AgentDto>();

		List<Droit> listeApprobateurs = accessRightsService.getListApprobateursOfOperateur(convertedIdAgent);
		for (Droit approbateur : listeApprobateurs) {
			for (AgentDto ag : accessRightsService.getAgentsToInputByOperateur(approbateur.getIdAgent(), convertedIdAgent, idServiceADS)) {
				if (!services.contains(ag))
					services.add(ag);
			}
		}

		if (services.size() == 0)
			throw new NoContentException();

		return services;
	}

	/**
	 * Retourne les types de saisie (champs de saisie, checkbox, etc) d une
	 * absence donnee
	 */
	@ResponseBody
	@RequestMapping(value = "/getTypesSaisi", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<RefTypeSaisiDto> getTypesSaisi(@RequestParam(value = "idRefTypeAbsence", required = false) Integer idRefTypeAbsence) {

		logger.debug("entered GET [filtres/getTypesSaisi] => getTypesSaisi");

		List<RefTypeSaisiDto> typesSaisi = filtresService.getRefTypeSaisi(idRefTypeAbsence);

		return typesSaisi;
	}

	/**
	 * Retourne les groupes d absence
	 */
	@ResponseBody
	@RequestMapping(value = "/getGroupesAbsence", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<RefGroupeAbsenceDto> getGroupesAbsence(@RequestParam(value = "idRefGroupeAbsence", required = false) Integer idRefGroupeAbsence) {

		logger.debug("entered GET [filtres/getGroupesAbsence] => getGroupesAbsence");

		List<RefGroupeAbsenceDto> groupes = filtresService.getRefGroupeAbsence(idRefGroupeAbsence);

		return groupes;
	}

	/**
	 * Retourne les groupes d absence
	 */
	@ResponseBody
	@RequestMapping(value = "/getGroupesAbsenceForAgent", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<RefGroupeAbsenceDto> getGroupesAbsenceForAgent(@RequestParam(value = "idRefGroupeAbsence", required = false) Integer idRefGroupeAbsence) {

		logger.debug("entered GET [filtres/getGroupesAbsenceForAgent] => getGroupesAbsenceForAgent");

		List<RefGroupeAbsenceDto> groupes = filtresService.getRefGroupeAbsenceForAgent(idRefGroupeAbsence);

		return groupes;
	}

	/**
	 * Retourne les groupes d absence
	 */
	@ResponseBody
	@RequestMapping(value = "/getUnitePeriodeQuota", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<UnitePeriodeQuotaDto> getUnitePeriodeQuota() {

		logger.debug("entered GET [filtres/getUnitePeriodeQuota] => getUnitePeriodeQuota");

		List<UnitePeriodeQuotaDto> upq = filtresService.getUnitePeriodeQuota();

		return upq;
	}

	/**
	 * Liste des types d absence saisissable dans le kiosque RH pour un agent
	 * donné et un groupe donné
	 */
	@ResponseBody
	@RequestMapping(value = "/getTypeAbsenceKiosque", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<RefTypeAbsenceDto> getTypeAbsenceKiosque(@RequestParam(value = "idRefGroupeAbsence", required = false) Integer idRefGroupeAbsence,
			@RequestParam(value = "idAgent", required = false) Integer idAgent) {

		logger.debug("entered GET [filtres/getTypeAbsenceKiosque] => getTypeAbsenceKiosque with parameter  idRefGroupeAbsence = {} and  idAgent = {}", idRefGroupeAbsence, idAgent);

		List<RefTypeAbsenceDto> types = filtresService.getRefTypesAbsenceSaisieKiosque(idRefGroupeAbsence, idAgent);

		return types;
	}

	/**
	 * Liste des types d absence saisissable dans le kiosque RH pour un agent
	 * donné et un groupe donné
	 */
	@ResponseBody
	@RequestMapping(value = "/getTypeAbsenceForFilterKiosque", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<RefTypeAbsenceDto> getTypeAbsenceForFilterKiosque(@RequestParam(value = "idRefGroupeAbsence", required = false) Integer idRefGroupeAbsence,
			@RequestParam(value = "idAgent", required = false) Integer idAgent) {

		logger.debug("entered GET [filtres/getTypeAbsenceKiosque] => getTypeAbsenceKiosque with parameter  idRefGroupeAbsence = {} and  idAgent = {}", idRefGroupeAbsence, idAgent);

		List<RefTypeAbsenceDto> types = filtresService.getAllRefTypesAbsenceFiltre(idRefGroupeAbsence, idAgent);

		return types;
	}

	/**
	 * Liste des types d absence pour l'alimenation des compteurs dans le
	 * kiosque Pour le moment que pour recup et repos comp
	 */
	@ResponseBody
	@RequestMapping(value = "/getTypeAbsenceCompteurKiosque", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<RefTypeAbsenceDto> getTypeAbsenceCompteurKiosque() {

		logger.debug("entered GET [filtres/getTypeAbsenceCompteurKiosque] => getTypeAbsenceCompteurKiosque");

		List<RefTypeAbsenceDto> types = filtresService.getRefTypesAbsenceCompteurKiosque();

		return types;
	}

	/**
	 * Liste de tous les types d absence possibles
	 */
	@ResponseBody
	@RequestMapping(value = "/getAllTypes", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<RefTypeAbsenceDto> getAllTypes() {

		logger.debug("entered GET [filtres/getAllTypes] => getAllTypes");

		List<RefTypeAbsenceDto> types = filtresService.getAllRefTypesAbsence();

		return types;
	}

	/**
	 * Liste de tous les types d accident de travail
	 */
	@ResponseBody
	@RequestMapping(value = "/getAllTypeAccidentTravail", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<RefTypeDto> getAllTypeAccidentTravail() {

		logger.debug("entered GET [filtres/getAllTypeAccidentTravail] => getAllTypeAccidentTravail");

		return filtresService.getAllRefTypeAccidentTravail();
	}

	/**
	 * Liste de tous les types de siege de lesion
	 */
	@ResponseBody
	@RequestMapping(value = "/getAllTypeSiegeLesion", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<RefTypeDto> getAllTypeSiegeLesion() {

		logger.debug("entered GET [filtres/getAllTypeSiegeLesion] => getAllTypeSiegeLesion");

		return filtresService.getAllRefTypeSiegeLesion();
	}

	/**
	 * Liste de tous les types de maladie pro
	 */
	@ResponseBody
	@RequestMapping(value = "/getAllTypeMaladiePro", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<RefTypeDto> getAllTypeMaladiePro() {

		logger.debug("entered GET [filtres/getAllTypeMaladiePro] => getAllTypeMaladiePro");

		return filtresService.getAllRefTypeMaladiePro();
	}

	/**
	 * Saisie/modification d un type d accident de travail
	 */
	@ResponseBody
	@RequestMapping(value = "/setTypeAccidentTravail", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto setTypeAccidentTravail(@RequestParam(value = "idAgent", required = true) Integer idAgent, 
			@RequestBody(required = true) RefTypeDto typeDto, 
			HttpServletResponse response) {

		logger.debug("entered POST [filtres/setTypeAccidentTravail] => setTypeAccidentTravail");

		ReturnMessageDto srm = filtresService.setTypeGenerique(idAgent, RefTypeAccidentTravail.class, typeDto);

		if (!srm.getErrors().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_CONFLICT);
		}
		
		return srm;
	}

	/**
	 * Saisie/modification d un type de siege lesion
	 */
	@ResponseBody
	@RequestMapping(value = "/setTypeSiegeLesion", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto setTypeSiegeLesion(@RequestParam(value = "idAgent", required = true) Integer idAgent, 
			@RequestBody(required = true) RefTypeDto typeDto, 
			HttpServletResponse response) {

		logger.debug("entered POST [filtres/setTypeSiegeLesion] => setTypeSiegeLesion");

		ReturnMessageDto srm = filtresService.setTypeGenerique(idAgent, RefTypeSiegeLesion.class, typeDto);

		if (!srm.getErrors().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_CONFLICT);
		}
		
		return srm;
	}

	/**
	 * Saisie/modification d un type de maladie pro
	 */
	@ResponseBody
	@RequestMapping(value = "/setTypeMaladiePro", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto setTypeMaladiePro(@RequestParam(value = "idAgent", required = true) Integer idAgent, 
			@RequestBody(required = true) RefTypeDto typeDto, 
			HttpServletResponse response) {

		logger.debug("entered POST [filtres/setTypeMaladiePro] => setTypeMaladiePro");

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);
		
		ReturnMessageDto srm = filtresService.setTypeGenerique(convertedIdAgent, RefTypeMaladiePro.class, typeDto);

		if (!srm.getErrors().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_CONFLICT);
		}
		
		return srm;
	}
	
	/**
	 * Supprime un type d'accident de travail
	 * 
	 * @param idAgent Integer Agent connecte
	 * @param typeDto RefTypeDto L accident de travail a supprimer
	 * @param response HttpServletResponse
	 * @return ReturnMessageDto
	 */
	@ResponseBody
	@RequestMapping(value = "/deleteTypeAccidentTravail", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto deleteTypeAccidentTravail(@RequestParam(value = "idAgent", required = true) Integer idAgent,
			@RequestBody(required = true) RefTypeDto typeDto, HttpServletResponse response) {

		logger.debug("entered POST [filtres/deleteTypeAccidentTravail] => deleteTypeAccidentTravail");

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);
		
		ReturnMessageDto srm = filtresService.deleteTypeGenerique(convertedIdAgent, RefTypeAccidentTravail.class, typeDto);

		if (!srm.getErrors().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_CONFLICT);
		}
		
		return srm;
	}

	/**
	 * Supprime un type de siège de lesion
	 * 
	 * @param idAgent Integer Agent connecte
	 * @param typeDto RefTypeDto Le siege lesion a supprimer
	 * @param response HttpServletResponse
	 * @return ReturnMessageDto
	 */
	@ResponseBody
	@RequestMapping(value = "/deleteTypeSiegeLesion", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto deleteTypeSiegeLesion(@RequestParam(value = "idAgent", required = true) Integer idAgent,
			@RequestBody(required = true) RefTypeDto typeDto, HttpServletResponse response) {

		logger.debug("entered POST [filtres/deleteTypeSiegeLesion] => deleteTypeSiegeLesion");

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);
		
		ReturnMessageDto srm = filtresService.deleteTypeGenerique(convertedIdAgent, RefTypeSiegeLesion.class, typeDto);

		if (!srm.getErrors().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_CONFLICT);
		}
		
		return srm;
	}

	/**
	 * Supprime un type de maladie pro
	 * 
	 * @param idAgent Integer Agent connecte
	 * @param typeDto RefTypeDto La maladie pro a supprimer
	 * @param response HttpServletResponse
	 * @return ReturnMessageDto
	 */
	@ResponseBody
	@RequestMapping(value = "/deleteTypeMaladiePro", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto deleteTypeMaladiePro(@RequestParam(value = "idAgent", required = true) Integer idAgent,
			@RequestBody(required = true) RefTypeDto typeDto, HttpServletResponse response) {

		logger.debug("entered POST [filtres/deleteTypeMaladiePro] => deleteTypeMaladiePro");

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);
		
		ReturnMessageDto srm = filtresService.deleteTypeGenerique(convertedIdAgent, RefTypeMaladiePro.class, typeDto);

		if (!srm.getErrors().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_CONFLICT);
		}
		
		return srm;
	}
}
