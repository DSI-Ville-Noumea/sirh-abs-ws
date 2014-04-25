package nc.noumea.mairie.ws;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.sun.jersey.api.client.ClientResponse;

@Service
public class SirhWSConsumer extends BaseWsConsumer implements ISirhWSConsumer {

	@Autowired
	@Qualifier("sirhWsBaseUrl")
	private String sirhWsBaseUrl;

	private static final String sirhAgentServiceUrl = "services/agent";

	private static final String isUtilisateurSIRHServiceUrl = "utilisateur/isUtilisateurSIRH";

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
}
