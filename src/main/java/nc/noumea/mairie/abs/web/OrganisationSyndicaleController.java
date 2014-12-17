package nc.noumea.mairie.abs.web;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import nc.noumea.mairie.abs.dto.OrganisationSyndicaleDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.service.IOrganisationSyndicaleService;

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
@RequestMapping("/organisation")
public class OrganisationSyndicaleController {

	private Logger logger = LoggerFactory.getLogger(OrganisationSyndicaleController.class);

	@Autowired
	private IOrganisationSyndicaleService organisationService;

	/**
	 * Saisie/modification d une organisation syndicale
	 */
	@ResponseBody
	@RequestMapping(value = "/addOS", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto setOrganisationSyndicale(
			@RequestBody(required = true) OrganisationSyndicaleDto organisationDto, HttpServletResponse response) {

		logger.debug("entered POST [organisation/addOS] => setOrganisationSyndicale for SIRH");

		ReturnMessageDto srm = organisationService.saveOrganisation(organisationDto);

		if (!srm.getErrors().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_CONFLICT);
		}

		return srm;
	}

	/**
	 * Liste des organisations syndicales
	 */
	@ResponseBody
	@RequestMapping(value = "/listOrganisation", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<OrganisationSyndicaleDto> listOrganisationSyndicale() {

		logger.debug("entered GET [organisation/listOrganisation] => listOrganisationSyndicale");

		List<OrganisationSyndicaleDto> orga = organisationService.getListOrganisationSyndicale();

		return orga;
	}

	/**
	 * Liste des organisations syndicales actives
	 */
	@ResponseBody
	@RequestMapping(value = "/listOrganisationActif", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<OrganisationSyndicaleDto> listOrganisationSyndicaleActives(
			@RequestParam(value = "idAgent", required = true) Integer idAgent,
			@RequestParam(value = "idRefTypeAbsence", required = true) Integer idRefTypeAbsence) {

		logger.debug(
				"entered GET [organisation/listOrganisationActif] => listOrganisationSyndicaleActives with parameter idAgent = {} and idRefTypeAbsence = {}",
				idAgent, idRefTypeAbsence);

		List<OrganisationSyndicaleDto> orga = organisationService.getListOrganisationSyndicaleActives(idAgent,
				idRefTypeAbsence);

		return orga;
	}
}
