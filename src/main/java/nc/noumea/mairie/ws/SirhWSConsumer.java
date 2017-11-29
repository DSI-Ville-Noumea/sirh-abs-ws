package nc.noumea.mairie.ws;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.sun.jersey.api.client.ClientResponse;

import flexjson.JSONSerializer;
import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.abs.dto.EntiteDto;
import nc.noumea.mairie.abs.dto.InfosAlimAutoCongesAnnuelsDto;
import nc.noumea.mairie.abs.dto.JourDto;
import nc.noumea.mairie.abs.dto.LightUser;
import nc.noumea.mairie.abs.dto.RefTypeSaisiCongeAnnuelDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

@Service
public class SirhWSConsumer extends BaseWsConsumer implements ISirhWSConsumer {

	@Autowired
	@Qualifier("sirhWsBaseUrl")
	private String				sirhWsBaseUrl;

	private static final String	sirhAgentServiceUrl							= "services/agent";
	private static final String	sirhListAgentServiceUrl						= "services/agentsWithEntiteParent";
	private static final String	sirhListAgentsWithServiceUrl				= "services/listAgentsWithService";
	private static final String	sirhListAgentsWithServiceOldAffectationUrl	= "services/listAgentsWithServiceOldAffectation";
	private static final String	sirhAgentUrl								= "agents/getAgent";
	private static final String	sirhListAgentsUrl							= "agents/getListAgents";
	private static final String	sirhIsPeriodeEssaiUrl						= "agents/isPeriodeEssai";
	private static final String	isUtilisateurSIRHServiceUrl					= "utilisateur/isUtilisateurSIRH";
	private static final String	isJourHolidayUrl							= "utils/isHoliday";
	private static final String	sirhBaseCongeUrl							= "absences/baseHoraire";
	private static final String	sirhOldBaseCongeUrl							= "absences/oldBaseHoraire";
	private static final String	sirhListPAPourAlimAutoCongesAnnuelsUrl		= "absences/listPAPourAlimAutoCongesAnnuels";
	private static final String	sirhListPAByAgentUrl						= "absences/listPAByAgentSansFuture";
	private static final String	listeJoursFeriesUrl							= "utils/listeJoursFeries";
	private static final String	isPaieEnCoursUrl							= "utils/isPaieEnCours";
	private static final String	sirhAgentDirectionUrl						= "agents/direction";
	private static final String	sirhListEmailDestinataireUrl				= "utilisateur/getEmailDestinataire";

	@Override
	public AgentWithServiceDto getAgentService(Integer idAgent, Date date) {

		String url = String.format(sirhWsBaseUrl + sirhAgentServiceUrl);

		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("idAgent", String.valueOf(idAgent));

		if (date != null) {
			SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
			parameters.put("date", sf.format(date));
		}

		ClientResponse res = createAndFireGetRequest(parameters, url);

		return readResponse(AgentWithServiceDto.class, res, url);
	}

	@Override
	public List<AgentWithServiceDto> getListAgentsWithService(List<Integer> listAgentDto, Date date, boolean withoutLibelleService) {
		// le boolean withoutLibelleService permet a SIRH-WS de ne pas appeler ADS inutilement
		// car deja appeler dans ce projet

		String url = String.format(sirhWsBaseUrl + sirhListAgentsWithServiceUrl);

		Map<String, String> parameters = new HashMap<String, String>();

		String json = new JSONSerializer().exclude("*.class").deepSerialize(listAgentDto);

		if (date != null) {
			SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
			parameters.put("date", sf.format(date));
		}

		if (withoutLibelleService) {
			parameters.put("withoutLibelleService", "true");
		}

		ClientResponse res = createAndFirePostRequest(parameters, url, json);

		return readResponseAsList(AgentWithServiceDto.class, res, url);
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
	public List<AgentGeneriqueDto> getListAgents(List<Integer> listAgentDto) {

		String url = String.format(sirhWsBaseUrl + sirhListAgentsUrl);

		Map<String, String> parameters = new HashMap<String, String>();

		String json = new JSONSerializer().exclude("*.class").deepSerialize(listAgentDto);

		ClientResponse res = createAndFirePostRequest(parameters, url, json);

		return readResponseAsList(AgentGeneriqueDto.class, res, url);
	}

	@Override
	public boolean isJourHoliday(Date date) {
		String url = String.format(sirhWsBaseUrl + isJourHolidayUrl);
		HashMap<String, String> params = new HashMap<>();
		SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
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
		SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");

		String url = String.format(sirhWsBaseUrl + sirhBaseCongeUrl);

		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("idAgent", String.valueOf(idAgent));
		parameters.put("date", sf.format(date));

		ClientResponse res = createAndFireGetRequest(parameters, url);

		return readResponse(RefTypeSaisiCongeAnnuelDto.class, res, url);
	}

	@Override
	public List<InfosAlimAutoCongesAnnuelsDto> getListPAPourAlimAutoCongesAnnuels(Integer idAgent, Date dateDebut, Date dateFin) {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

		String url = String.format(sirhWsBaseUrl + sirhListPAPourAlimAutoCongesAnnuelsUrl);

		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("idAgent", String.valueOf(idAgent));
		parameters.put("dateDebut", sdf.format(dateDebut));
		parameters.put("dateFin", sdf.format(dateFin));

		ClientResponse res = createAndFireGetRequest(parameters, url);

		return readResponseAsList(InfosAlimAutoCongesAnnuelsDto.class, res, url);
	}

	@Override
	public List<JourDto> getListeJoursFeries(Date dateDebut, Date dateFin) {
		// #16811 : on ajoute 3 jours à la date de fin pour recuperer les jours
		// feries suivant
		DateTime endDate = new DateTime(dateFin);
		endDate = endDate.plusDays(3);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

		String url = String.format(sirhWsBaseUrl + listeJoursFeriesUrl);

		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("dateDebut", sdf.format(dateDebut));
		parameters.put("dateFin", sdf.format(endDate.toDate()));

		ClientResponse res = createAndFireGetRequest(parameters, url);

		return readResponseAsList(JourDto.class, res, url);
	}

	// #18617 bug avec l ajout des 3 jours à la date de fin de la methode
	// getListeJoursFeries() ci-dessus
	@Override
	public List<JourDto> getListeJoursFeriesForSaisiDPM(Date dateDebut, Date dateFin) {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

		String url = String.format(sirhWsBaseUrl + listeJoursFeriesUrl);

		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("dateDebut", sdf.format(dateDebut));
		parameters.put("dateFin", sdf.format(dateFin));

		ClientResponse res = createAndFireGetRequest(parameters, url);

		return readResponseAsList(JourDto.class, res, url);
	}

	@Override
	public ReturnMessageDto isPaieEnCours() {
		String url = String.format(sirhWsBaseUrl + isPaieEnCoursUrl);
		Map<String, String> parameters = new HashMap<String, String>();

		ClientResponse res = createAndFireGetRequest(parameters, url);

		return readResponse(ReturnMessageDto.class, res, url);
	}

	@Override
	public RefTypeSaisiCongeAnnuelDto getOldBaseHoraireAbsence(Integer idAgent) {
		String url = String.format(sirhWsBaseUrl + sirhOldBaseCongeUrl);

		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("idAgent", String.valueOf(idAgent));

		ClientResponse res = createAndFireGetRequest(parameters, url);

		return readResponse(RefTypeSaisiCongeAnnuelDto.class, res, url);
	}

	@Override
	public List<InfosAlimAutoCongesAnnuelsDto> getListPAByAgentSansPAFuture(Integer idAgent, Date dateFin) {

		String url = String.format(sirhWsBaseUrl + sirhListPAByAgentUrl);

		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("idAgent", String.valueOf(idAgent));
		SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
		parameters.put("dateFin", sf.format(dateFin));

		ClientResponse res = createAndFireGetRequest(parameters, url);

		return readResponseAsList(InfosAlimAutoCongesAnnuelsDto.class, res, url);
	}

	@Override
	public EntiteDto getAgentDirection(Integer idAgent, Date date) {

		String url = String.format(sirhWsBaseUrl + sirhAgentDirectionUrl);

		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("idAgent", String.valueOf(idAgent));
		if (date != null) {
			SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
			parameters.put("dateAffectation", sf.format(date));
		}

		ClientResponse res = createAndFireGetRequest(parameters, url);

		return readResponse(EntiteDto.class, res, url);
	}

	@Override
	public List<AgentWithServiceDto> getListAgentServiceWithParent(Integer idServiceADS, Date date) {

		String url = String.format(sirhWsBaseUrl + sirhListAgentServiceUrl);
		SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");

		Map<String, String> parameters = new HashMap<String, String>();

		parameters.put("idServiceADS", idServiceADS.toString());
		parameters.put("date", sf.format(date));

		ClientResponse res = createAndFireGetRequest(parameters, url);

		return readResponseAsList(AgentWithServiceDto.class, res, url);
	}

	@Override
	public List<AgentWithServiceDto> getListAgentsWithServiceOldAffectation(List<Integer> listAgentSansAffectation, boolean withoutLibelleService) {
		// le boolean withoutLibelleService permet a SIRH-WS de ne pas appeler ADS inutilement
		// car deja appeler dans ce projet

		String url = String.format(sirhWsBaseUrl + sirhListAgentsWithServiceOldAffectationUrl);

		Map<String, String> parameters = new HashMap<String, String>();

		if (withoutLibelleService) {
			parameters.put("withoutLibelleService", "true");
		}

		String json = new JSONSerializer().exclude("*.class").deepSerialize(listAgentSansAffectation);

		ClientResponse res = createAndFirePostRequest(parameters, url, json);

		return readResponseAsList(AgentWithServiceDto.class, res, url);
	}

	@Override
	public boolean isPeriodeEssai(Integer idAgent, Date date) {

		String url = String.format(sirhWsBaseUrl + sirhIsPeriodeEssaiUrl);
		HashMap<String, String> params = new HashMap<>();
		SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
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
	public List<LightUser> getEmailDestinataire() {
		String url = String.format(sirhWsBaseUrl + sirhListEmailDestinataireUrl);

		Map<String, String> parameters = new HashMap<String, String>();

		ClientResponse res = createAndFireGetRequest(parameters, url);

		return readResponseAsList(LightUser.class, res, url);
	}
}
