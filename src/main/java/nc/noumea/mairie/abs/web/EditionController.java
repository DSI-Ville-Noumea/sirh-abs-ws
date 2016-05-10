package nc.noumea.mairie.abs.web;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.EditionDemandeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IDemandeRepository;
import nc.noumea.mairie.abs.service.IAbsenceService;
import nc.noumea.mairie.abs.service.IAccessRightsService;
import nc.noumea.mairie.abs.service.IAgentMatriculeConverterService;
import nc.noumea.mairie.abs.service.ITitreDemandeReportingService;
import nc.noumea.mairie.ws.ISirhWSConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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
	private ITitreDemandeReportingService titreDemandeReportingService;

	@Autowired
	private IAccessRightsService accessRightsService;

	@Autowired
	private IDemandeRepository demandeRepository;

	@Autowired
	private ISirhWSConsumer sirhWSConsumer;

	@Autowired
	private IAbsenceService absenceService;

	@ResponseBody
	@RequestMapping(value = "/downloadTitreDemande", method = RequestMethod.GET)
	public ResponseEntity<byte[]> downloadTitreDemande(@RequestParam("idAgent") int idAgent, @RequestParam("idDemande") int idDemande) {
		logger.debug("entered GET [edition/downloadTitreDemande] => downloadTitreDemande with parameters  idDemande = {}, idAgent = {}", idDemande, idAgent);

		ReturnMessageDto returnDto = new ReturnMessageDto();
		// verification des droits
		Demande demande = demandeRepository.getEntity(Demande.class, idDemande);
		if (null == demande)
			return new ResponseEntity<byte[]>(HttpStatus.NO_CONTENT);

		returnDto = accessRightsService.verifAccessRightDemande(idAgent, demande.getIdAgent(), returnDto);
		if (!returnDto.getErrors().isEmpty())
			return new ResponseEntity<byte[]>(HttpStatus.UNAUTHORIZED);

		Integer convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		AgentGeneriqueDto agent = sirhWSConsumer.getAgent(convertedIdAgent);
		if (agent == null || agent.getIdAgent() == null)
			return new ResponseEntity<byte[]>(HttpStatus.NO_CONTENT);

		DemandeDto demandeDto = absenceService.getDemandeDto(idDemande);

		AgentWithServiceDto approbateurDto = accessRightService.getApprobateurOfAgent(demandeDto.getAgentWithServiceDto().getIdAgent());

		EditionDemandeDto dtoFinal = new EditionDemandeDto(demandeDto, approbateurDto);

		byte[] responseData = null;

		try {
			responseData = titreDemandeReportingService.getTitreDemandeAsByteArray(dtoFinal);
		} catch (AccessForbiddenException e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<byte[]>(HttpStatus.UNAUTHORIZED);
		} catch (NoContentException e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<byte[]>(HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<byte[]>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/pdf");
		headers.add("Content-Disposition", String.format("attachment; filename=\"titreDemande.pdf\""));

		return new ResponseEntity<byte[]>(responseData, headers, HttpStatus.OK);
	}
}
