package nc.noumea.mairie.abs.web;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

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
	public List<RefTypeAbsenceDto> getListeTypeAbsence() {

		logger.debug("entered GET [typeAbsence/getListeTypeAbsence] => getListeTypeAbsence");

		return typeAbsenceService.getListeTypAbsence();
	}

	/**
	 * Saisie/modification d un type d absence
	 */
	@ResponseBody
	@RequestMapping(value = "/setTypeAbsence", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto setTypeAbsence(@RequestBody(required = true) RefTypeAbsenceDto typeAbsenceDto, 
			@RequestParam("idAgent") int idAgent,
			HttpServletResponse response) {

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
	@RequestMapping(value = "/deleteTypeAbsence", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public ReturnMessageDto deleteTypeAbsence(@RequestParam("idAgent") int idAgent,
			@RequestParam("idTypeDemande") int idTypeDemande,
			HttpServletResponse response) {

		logger.debug("entered POST [typeAbsence/deleteTypeAbsence] => deleteTypeAbsence");

		Integer convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);
		
		ReturnMessageDto srm = typeAbsenceService.deleteTypeAbsence(convertedIdAgent, idTypeDemande);

		if (!srm.getErrors().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_CONFLICT);
		}
		
		return srm;
	}
}
