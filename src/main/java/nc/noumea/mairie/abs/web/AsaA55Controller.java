package nc.noumea.mairie.abs.web;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.service.IAgentMatriculeConverterService;
import nc.noumea.mairie.abs.service.ICounterService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/asaA55")
public class AsaA55Controller {
	
	private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

	private Logger logger = LoggerFactory.getLogger(AsaA55Controller.class);

	@Autowired
	@Qualifier("AsaA55CounterServiceImpl")
	private ICounterService counterService;

	@Autowired
	private IAgentMatriculeConverterService converterService;

	/**
	 * Modifie manuellement le compteur ASA A55 d un agent RequestBody : Format
	 * du type timestamp : "/Date(1396306800000+1100)/"
	 */
	@ResponseBody
	@RequestMapping(value = "/addManual", produces = "application/json;charset=utf-8", method = RequestMethod.POST)
	public ReturnMessageDto addAsaA55ManuelForAgent(@RequestParam("idAgent") int idAgent,
			@RequestBody(required = true) CompteurDto compteurDto, HttpServletResponse response) {

		logger.debug("entered POST [asaA55/addManual] => addAsaA55ManuelForAgent with parameters idAgent = {}", idAgent);

		int convertedIdAgent = converterService.tryConvertFromADIdAgentToSIRHIdAgent(idAgent);

		ReturnMessageDto srm = counterService.majManuelleCompteurToAgent(convertedIdAgent, compteurDto, false);

		if (!srm.getErrors().isEmpty()) {
			response.setStatus(HttpServletResponse.SC_CONFLICT);
		}

		return srm;
	}
	
	@ResponseBody
	@RequestMapping(value = "/countAllByYear", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public Integer countAllByYear(@RequestParam(value = "year", required = false) Integer annee, 
			@RequestParam(value = "idOS", required = false) Integer idOS, 
			@RequestParam(value = "idAgentRecherche", required = false) Integer idAgentRecherche, 
			@RequestParam(value = "dateMin", required = false) String dateMin, 
			@RequestParam(value = "dateMax", required = false) String dateMax, HttpServletResponse response) throws ParseException {

		logger.debug("entered GET [asaA48/countAll]");
		
		Date dateDeb = dateMin != null ? sdf.parse(dateMin) : null;
		Date dateFin = dateMax != null ? sdf.parse(dateMax) : null;

		return counterService.countAllByYear(annee, idOS, idAgentRecherche, dateDeb, dateFin);
	}

	/**
	 * Liste des compteurs ASA A55 ResponseBody : Format du type timestamp :
	 * "/Date(1396306800000+1100)/"
	 * @throws ParseException 
	 */
	@ResponseBody
	@RequestMapping(value = "/listeCompteurA55", produces = "application/json;charset=utf-8", method = RequestMethod.GET)
	public List<CompteurDto> getListeCompteur(@RequestParam(value = "pageSize", required = false) Integer pageSize, 
			@RequestParam(value = "pageNumber", required = false) Integer pageNumber, 
			@RequestParam(value = "idAgentRecherche", required = false) Integer idAgentRecherche, 
			@RequestParam(value = "dateMin", required = false) String dateMin, 
			@RequestParam(value = "dateMax", required = false) String dateMax) throws ParseException {

		logger.debug("entered GET [asaA55/listeCompteurA55] => getListeCompteur ");

		List<CompteurDto> result = counterService.getListeCompteurWithDate(pageSize, pageNumber, idAgentRecherche, dateMin, dateMax);

		if (result.size() == 0)
			throw new NoContentException();

		return result;
	}
}
