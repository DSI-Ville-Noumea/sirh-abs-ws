package nc.noumea.mairie.ws;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import nc.noumea.mairie.abs.dto.ReturnMessageDto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.sun.jersey.api.client.ClientResponse;

@Service
public class PtgWsConsumer extends BaseWsConsumer implements IPtgWsConsumer {

	@Autowired
	@Qualifier("sirhPtgWsBaseUrl")
	private String sirhPtgWsBaseUrl;

	protected Logger logger = LoggerFactory.getLogger(PtgWsConsumer.class);

	private static final String checkPointageUrl = "pointages/checkPointage";

	@Override
	public ReturnMessageDto checkPointage(Integer idAgent, Date dateDebut, Date dateFin) {
		SimpleDateFormat sf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

		String url = String.format(sirhPtgWsBaseUrl + checkPointageUrl);

		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("idAgent", String.valueOf(idAgent));
		parameters.put("dateDebut", sf.format(dateDebut));
		parameters.put("dateFin", sf.format(dateFin));

		ClientResponse res = createAndFireGetRequest(parameters, url);
		try {
			return readResponse(ReturnMessageDto.class, res, url);
		} catch (Exception e) {
			logger.error("L'application des pointages ne repond pas." + e.getMessage());
			return new ReturnMessageDto();
		}
	}

}
