package nc.noumea.mairie.abs.web;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import nc.noumea.mairie.abs.dto.MotifCompteurDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.service.IMotifService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/motifCompteur")
public class MotifCompteurController {

	private Logger logger = LoggerFactory.getLogger(MotifCompteurController.class);

	@Autowired
	private IMotifService motifService;

	/**
	 * Retourne la liste des motifs lors de la saisie d un compteur donne
	 */
	@ResponseBody
	@RequestMapping(value = "/getListeMotifCompteur", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public List<MotifCompteurDto> getListeMotifCompteur(@RequestParam(value = "idRefType", required = false) Integer idRefType) {

		logger.debug("entered GET [motifCompteur/getListeMotifCompteur] => getListeMotifCompteur");

		List<MotifCompteurDto> motifs = motifService.getListeMotifCompteur(idRefType);

		return motifs;
	}
	
	/**
	 * Saisie/modification d un motif compteur
	 */
	@ResponseBody
	@RequestMapping(value = "/setMotifCompteur", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	@Transactional(value = "absTransactionManager")
	public ReturnMessageDto setMotifCompteur(@RequestBody(required = true) MotifCompteurDto motifCompteurDto, 
			HttpServletResponse response) {

		logger.debug("entered POST [motifCompteur/setMotifCompteur] => setMotifCompteur");
		
		ReturnMessageDto srm = motifService.setMotifCompteur(motifCompteurDto);

		if (!srm.getErrors().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_CONFLICT);
		}
		
		return srm;
	}
}
