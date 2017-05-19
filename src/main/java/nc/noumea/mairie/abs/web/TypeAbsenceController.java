package nc.noumea.mairie.abs.web;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import nc.noumea.mairie.abs.domain.RefTypeGroupeAbsenceEnum;
import nc.noumea.mairie.abs.dto.RefTypeAbsenceDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.service.IAgentMatriculeConverterService;
import nc.noumea.mairie.abs.service.ITypeAbsenceService;

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
@RequestMapping("/typeAbsence")
public class TypeAbsenceController {

	private Logger logger = LoggerFactory.getLogger(TypeAbsenceController.class);

	@Autowired
	private ITypeAbsenceService typeAbsenceService;

	@Autowired
	private IAgentMatriculeConverterService converterService;

	/**
	 * Retourne la liste des types d absence
	 */
	@ResponseBody
	@RequestMapping(value = "/getListeTypeAbsence", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<RefTypeAbsenceDto> getListeTypeAbsence(
			@RequestParam(value = "idRefGroupeAbsence", required = false) Integer idRefGroupeAbsence) {

		logger.debug(
				"entered GET [typeAbsence/getListeTypeAbsence] => getListeTypeAbsence with parameters idRefGroupeAbsence = {}",
				idRefGroupeAbsence);
		if (idRefGroupeAbsence != null
				&& idRefGroupeAbsence.toString().equals(
						String.valueOf(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue()))) {
			return typeAbsenceService.getListeTypAbsenceCongeAnnuel();
		} else {
			return typeAbsenceService.getListeTypAbsence(idRefGroupeAbsence);
		}
	}

	/**
	 * Retourne la complète des types d'absence, avec les absences désactivées, pour affichage dans l'historique (#39247)
	 */
	@ResponseBody
	@RequestMapping(value = "/getListeAllTypeAbsence", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<RefTypeAbsenceDto> getListeAllTypeAbsence() {

		logger.debug("entered GET [typeAbsence/getListeAllTypeAbsence] => getListeAllTypeAbsence");

		return typeAbsenceService.getListeAllTypeAbsence();
	}

	/**
	 * Saisie/modification d un type d absence
	 */
	@ResponseBody
	@RequestMapping(value = "/setTypeAbsence", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto setTypeAbsence(@RequestBody(required = true) RefTypeAbsenceDto typeAbsenceDto,
			@RequestParam("idAgent") int idAgent, HttpServletResponse response) {

		logger.debug("entered POST [typeAbsence/setTypeAbsence] => setTypeAbsence");

		Integer convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		ReturnMessageDto srm = typeAbsenceService.setTypAbsence(convertedIdAgent, typeAbsenceDto);

		if (!srm.getErrors().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_CONFLICT);
		}

		return srm;
	}

	/**
	 * suppression d un type d absence
	 */
	@ResponseBody
	@RequestMapping(value = "/inactiveTypeAbsence", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public ReturnMessageDto inactiveTypeAbsence(@RequestParam("idAgent") int idAgent,
			@RequestParam("idRefTypeAbsence") int idRefTypeAbsence, HttpServletResponse response) {

		logger.debug("entered POST [typeAbsence/inactiveTypeAbsence] => inactiveTypeAbsence");

		Integer convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		ReturnMessageDto srm = typeAbsenceService.inactiveTypeAbsence(convertedIdAgent, idRefTypeAbsence);

		if (!srm.getErrors().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_CONFLICT);
		}

		return srm;
	}

	/**
	 * Retourne une type d absence
	 */
	@ResponseBody
	@RequestMapping(value = "/getTypeAbsence", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public RefTypeAbsenceDto getTypeAbsence(
			@RequestParam(value = "idBaseHoraireAbsence", required = true) Integer idBaseHoraireAbsence) {

		logger.debug(
				"entered GET [typeAbsence/getTypeAbsence] => getTypeAbsence with parameters idBaseHoraireAbsence = {}",
				idBaseHoraireAbsence);

		return typeAbsenceService.getTypeAbsenceByBaseHoraire(idBaseHoraireAbsence);

	}
}
