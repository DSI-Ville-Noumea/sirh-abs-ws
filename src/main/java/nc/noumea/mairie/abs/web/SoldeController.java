package nc.noumea.mairie.abs.web;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.FiltreSoldeDto;
import nc.noumea.mairie.abs.dto.HistoriqueSoldeDto;
import nc.noumea.mairie.abs.dto.SoldeDto;
import nc.noumea.mairie.abs.service.IAgentMatriculeConverterService;
import nc.noumea.mairie.abs.service.ISoldeService;
import nc.noumea.mairie.ws.ISirhWSConsumer;

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
@RequestMapping("/solde")
public class SoldeController {

	private Logger logger = LoggerFactory.getLogger(SoldeController.class);

	@Autowired
	private IAgentMatriculeConverterService converterService;

	@Autowired
	private ISoldeService soldeService;

	@Autowired
	private ISirhWSConsumer sirhWSConsumer;

	/**
	 * Retourne tous les compteurs d un agent <br />
	 * RequestBody : Format du type timestamp : "/Date(1396306800000+1100)/"
	 */
	@ResponseBody
	@RequestMapping(value = "soldeAgent", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public SoldeDto getSoldeAgent(@RequestParam(value = "idAgent", required = true) Integer idAgent,
			@RequestBody(required = true) FiltreSoldeDto filtreSoldeDto) {

		logger.debug(
				"entered POST [solde/soldeAgent] => getSoldeAgent with parameter idAgent = {}, dateDebut = {}, dateFin = {}, dateDemande = {}",
				idAgent, filtreSoldeDto.getDateDebut(), filtreSoldeDto.getDateFin(), filtreSoldeDto.getDateDemande());

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		AgentGeneriqueDto agent = sirhWSConsumer.getAgent(convertedIdAgent);
		if (agent == null || agent.getIdAgent() == null)
			throw new NotFoundException();

		return soldeService.getAgentSolde(convertedIdAgent, filtreSoldeDto.getDateDebut(), filtreSoldeDto.getDateFin(),
				filtreSoldeDto.getTypeDemande(),
				filtreSoldeDto.getDateDemande() == null ? new Date() : filtreSoldeDto.getDateDemande());
	}

	/**
	 * Historique d un compteur de type absence donne pour un agent <br />
	 * ResponseBody : Format du type timestamp : "/Date(1396306800000+1100)/"
	 */
	@ResponseBody
	@RequestMapping(value = "historiqueSolde", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public List<HistoriqueSoldeDto> getHistoriqueSolde(
			@RequestParam(value = "idAgent", required = true) Integer idAgent,
			@RequestParam(value = "codeRefTypeAbsence", required = true) Integer codeRefTypeAbsence,
			@RequestParam(value = "isSIRH", required = true) boolean isSIRH,
			@RequestBody(required = true) FiltreSoldeDto filtreSoldeDto) {

		logger.debug(
				"entered GET [solde/historiqueSolde] => getHistoriqueSolde with parameter idAgent = {}, codeRefTypeAbsence = {}",
				idAgent, codeRefTypeAbsence);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		List<HistoriqueSoldeDto> result = soldeService.getHistoriqueSoldeAgent(convertedIdAgent, codeRefTypeAbsence,
				filtreSoldeDto.getDateDebut(), filtreSoldeDto.getDateFin(), isSIRH);

		return result;
	}
}
