package nc.noumea.mairie.abs.web;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import nc.noumea.mairie.abs.dto.MotifDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.service.IMotifService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/motif")
public class MotifController {

	private Logger logger = LoggerFactory.getLogger(MotifController.class);

	@Autowired
	private IMotifService motifService;

	/**
	 * Retourne la liste des motifs pour un refus par exemple
	 */
	@ResponseBody
	@RequestMapping(value = "/getListeMotif", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<MotifDto> getListeMotif() {

		logger.debug("entered GET [motif/getListeMotif] => getListeMotif");

		List<MotifDto> motifs = motifService.getListeMotif();

		return motifs;
	}

	/**
	 * Saisie/modification d un motif
	 */
	@ResponseBody
	@RequestMapping(value = "/setMotif", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto setMotif(@RequestBody(required = true) MotifDto motifDto, 
			HttpServletResponse response) {

		logger.debug("entered POST [motif/setMotif] => setMotif");

		ReturnMessageDto srm = motifService.setMotif(motifDto);

		if (!srm.getErrors().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_CONFLICT);
		}
		
		return srm;
	}
}
