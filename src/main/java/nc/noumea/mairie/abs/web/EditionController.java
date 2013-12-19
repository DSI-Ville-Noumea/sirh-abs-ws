package nc.noumea.mairie.abs.web;

import nc.noumea.mairie.abs.service.IAccessRightsService;
import nc.noumea.mairie.abs.service.IAgentMatriculeConverterService;
import nc.noumea.mairie.abs.service.IReportingService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/edition")
public class EditionController {

	private Logger logger = LoggerFactory.getLogger(EditionController.class);

	@Autowired
	private IAccessRightsService accessRightService;

	@Autowired
	private IAgentMatriculeConverterService converterService;

	@Autowired
	private IReportingService reportingService;

	@ResponseBody
	@RequestMapping(value = "/downloadDemandeRecuperation", method = RequestMethod.GET)
	@Transactional(readOnly = true)
	public ResponseEntity<byte[]> downloadDemandeRecuperation(@RequestParam("idAgent") int idAgent,
			@RequestParam("idDemande") int idDemande, @RequestParam("idTypeDemande") int idTypeDemande) {

		logger.debug(
				"entered GET [edition/downloadDemandeRecuperation] => downloadDemandeRecuperation with parameters  idDemande = {}, idAgent = {}",
				idDemande, idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		byte[] responseData = null;

		try {
			responseData = reportingService.getDemandeRecuperationReportAsByteArray(convertedIdAgent, idDemande,
					idTypeDemande);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<byte[]>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/pdf");
		headers.add("Content-Disposition", String.format("attachment; filename=\"demandeRecuperation.pdf\""));

		return new ResponseEntity<byte[]>(responseData, headers, HttpStatus.OK);
	}
}
