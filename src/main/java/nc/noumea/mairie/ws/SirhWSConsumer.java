package nc.noumea.mairie.ws;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.abs.dto.InfosAlimAutoCongesAnnuelsDto;
import nc.noumea.mairie.abs.dto.RefTypeSaisiCongeAnnuelDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.sun.jersey.api.client.ClientResponse;

@Service
public class SirhWSConsumer extends BaseWsConsumer implements ISirhWSConsumer {

	@Autowired
	@Qualifier("sirhWsBaseUrl")
	private String sirhWsBaseUrl;

	private static final String sirhAgentServiceUrl = "services/agent";
	private static final String sirhAgentUrl = "agents/getAgent";
	private static final String isUtilisateurSIRHServiceUrl = "utilisateur/isUtilisateurSIRH";
	private static final String isJourHolidayUrl = "utils/isHoliday";
	private static final String sirhBaseCongeUrl = "absences/baseHoraire";
	private static final String sirhListPAPourAlimAutoCongesAnnuelsUrl = "absences/listPAPourAlimAutoCongesAnnuels";

	@Override
	public AgentWithServiceDto getAgentService(Integer idAgent, Date date) {

		String url = String.format(sirhWsBaseUrl + sirhAgentServiceUrl);

		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("idAgent", String.valueOf(idAgent));

		if (date != null) {
			SimpleDateFormat sf = new SimpleDateFormat("YYYYMMdd");
			parameters.put("date", sf.format(date));
		}

		ClientResponse res = createAndFireGetRequest(parameters, url);

		return readResponse(AgentWithServiceDto.class, res, url);
	}

	@Override
	public ReturnMessageDto isUtilisateurSIRH(Integer idAgent) {

		String url = String.format(sirhWsBaseUrl + isUtilisateurSIRHServiceUrl);

		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("idAgent", String.valueOf(idAgent));

		ClientResponse res = createAndFireGetRequest(parameters, url);

		ReturnMessageDto result = new ReturnMessageDto();
		try {
			result = readResponse(ReturnMessageDto.class, res, url);
		} catch (WSConsumerException e) {
			result.getErrors().add("L'agent n'existe pas dans l'AD.");
		}

		return result;
	}

	@Override
	public AgentGeneriqueDto getAgent(Integer idAgent) {
		String url = String.format(sirhWsBaseUrl + sirhAgentUrl);

		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("idAgent", String.valueOf(idAgent));

		ClientResponse res = createAndFireGetRequest(parameters, url);

		return readResponse(AgentGeneriqueDto.class, res, url);
	}

	@Override
	public boolean isJourHoliday(Date date) {
		String url = String.format(sirhWsBaseUrl + isJourHolidayUrl);
		HashMap<String, String> params = new HashMap<>();
		SimpleDateFormat sf = new SimpleDateFormat("YYYYMMdd");
		params.put("date", sf.format(date));

		ClientResponse res = createAndFireGetRequest(params, url);

		if (res.getStatus() == HttpStatus.NO_CONTENT.value()) {
			return false;
		} else if (res.getStatus() == HttpStatus.OK.value()) {
			return true;
		}
		return false;
	}

	@Override
	public RefTypeSaisiCongeAnnuelDto getBaseHoraireAbsence(Integer idAgent, Date date) {
		SimpleDateFormat sf = new SimpleDateFormat("YYYYMMdd");

		String url = String.format(sirhWsBaseUrl + sirhBaseCongeUrl);

		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("idAgent", String.valueOf(idAgent));
		parameters.put("date", sf.format(date));

		ClientResponse res = createAndFireGetRequest(parameters, url);

		return readResponse(RefTypeSaisiCongeAnnuelDto.class, res, url);
	}
	
	@Override
	public List<InfosAlimAutoCongesAnnuelsDto> getListPAPourAlimAutoCongesAnnuels(Integer idAgent, Date dateDebut, Date dateFin) {
		
		SimpleDateFormat sf = new SimpleDateFormat("YYYYMMdd");

		String url = String.format(sirhWsBaseUrl + sirhListPAPourAlimAutoCongesAnnuelsUrl);

		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("idAgent", String.valueOf(idAgent));
		parameters.put("dateDebut", sf.format(dateDebut));
		parameters.put("dateFin", sf.format(dateFin));

		ClientResponse res = createAndFireGetRequest(parameters, url);

		return readResponseAsList(InfosAlimAutoCongesAnnuelsDto.class, res, url);
	}
}
