package nc.noumea.mairie.abs.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentJoursFeriesGarde;
import nc.noumea.mairie.abs.dto.AgentDto;
import nc.noumea.mairie.abs.dto.AgentJoursFeriesGardeDto;
import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.abs.dto.JourDto;
import nc.noumea.mairie.abs.dto.JoursFeriesSaisiesGardeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.dto.SaisieGardeDto;
import nc.noumea.mairie.abs.repository.IAgentJoursFeriesGardeRepository;
import nc.noumea.mairie.abs.service.IAccessRightsService;
import nc.noumea.mairie.abs.service.ISaisieJoursFeriesGardeService;
import nc.noumea.mairie.ws.IAdsWSConsumer;
import nc.noumea.mairie.ws.ISirhWSConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SaisieJoursFeriesGardeService implements ISaisieJoursFeriesGardeService {

	private Logger logger = LoggerFactory.getLogger(SaisieJoursFeriesGardeService.class);

	@Autowired
	private IAgentJoursFeriesGardeRepository agentJoursFeriesGardeRepository;

	@Autowired
	private IAccessRightsService accessRightsService;

	@Autowired
	protected ISirhWSConsumer sirhWSConsumer;

	@Autowired
	protected IAdsWSConsumer adsWsConsumer;

	protected final static String ERREUR_JOUR_FERIE_ERRONE = "Le jour de garde %s n'est pas un jour férié ou chômé.";

	private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

	@Override
	@Transactional(readOnly = true)
	public SaisieGardeDto getListAgentsWithJoursFeriesEnGarde(Integer idAgent, Integer idServiveADS, Date dateDebut,
			Date dateFin) {

		logger.debug("Start getListAgentsWithJoursFeriesEnGarde");

		SaisieGardeDto result = new SaisieGardeDto();

		List<AgentDto> listAgentTemp = accessRightsService.getAgentsToApproveOrInputByService(idAgent, idServiveADS);
		List<AgentDto> listAgent = new ArrayList<>();
		List<Integer> listIdsAgent = new ArrayList<Integer>();
		for (AgentDto ag : listAgentTemp) {
			listIdsAgent.add(ag.getIdAgent());
		}
		List<AgentWithServiceDto> listAgentWithServiceDto = sirhWSConsumer.getListAgentsWithService(listIdsAgent, new Date(), false);
		for(AgentWithServiceDto agentWithServiceDto : listAgentWithServiceDto) {
			if (null != agentWithServiceDto 
					&& null != agentWithServiceDto.getSigleDirection() 
					&& agentWithServiceDto.getSigleDirection().toUpperCase().equals("DPM")) {
				listAgent.add((AgentDto)agentWithServiceDto);
			}
		}

		// bug #18617
		List<JourDto> listjourFerie = sirhWSConsumer.getListeJoursFeriesForSaisiDPM(dateDebut, dateFin);
		result.setJoursFerieHeader(listjourFerie);

		// on boucle sur les lignes Agents
		for (AgentDto agent : listAgent) {

			AgentJoursFeriesGardeDto dto = new AgentJoursFeriesGardeDto();
			dto.setAgent(agent);

			List<AgentJoursFeriesGarde> listJoursGardeAgent = agentJoursFeriesGardeRepository
					.getAgentJoursFeriesGardeByIdAgentAndPeriode(agent.getIdAgent(), dateDebut, dateFin);
			// on boucle sur les jours feries existant sous SIRH
			if (null != listjourFerie) {
				for (JourDto jourFerieSirh : listjourFerie) {
					JoursFeriesSaisiesGardeDto joursDto = new JoursFeriesSaisiesGardeDto();
					joursDto.setJourFerie(jourFerieSirh.getJour());
					joursDto.setCheck(checkAgentGardeJourDonne(listJoursGardeAgent, jourFerieSirh.getJour()));
					dto.getJoursFeriesEnGarde().add(joursDto);
				}
			}
			result.getListAgentAvecGarde().add(dto);
		}

		return result;
	}

	protected boolean checkAgentGardeJourDonne(List<AgentJoursFeriesGarde> listJoursGardeAgent, Date dateFerie) {

		if (null != listJoursGardeAgent) {
			for (AgentJoursFeriesGarde jourFerie : listJoursGardeAgent) {
				if (jourFerie.getJourFerieChome().equals(dateFerie))
					return true;
			}
		}

		return false;
	}

	@Override
	@Transactional(value = "absTransactionManager")
	public ReturnMessageDto setListAgentsWithJoursFeriesEnGarde(Integer idAgent,
			List<AgentJoursFeriesGardeDto> listDto, Date dateDebut, Date dateFin) {

		logger.debug("Start setListAgentsWithJoursFeriesEnGarde for idAgent = {}", idAgent);

		ReturnMessageDto result = new ReturnMessageDto();

		for (AgentJoursFeriesGardeDto dto : listDto) {
			// on verifie les droits
			result = accessRightsService.verifAccessRightDemande(idAgent, dto.getAgent().getIdAgent(), result);
			if (0 < result.getErrors().size()) {
				return result;
			}

			// garde deja existants
			List<AgentJoursFeriesGarde> listJoursGardeAgent = agentJoursFeriesGardeRepository
					.getAgentJoursFeriesGardeByIdAgentAndPeriode(dto.getAgent().getIdAgent(), dateDebut, dateFin);

			List<AgentJoursFeriesGarde> listJoursGardeAgentASupprimer = new ArrayList<AgentJoursFeriesGarde>();
			listJoursGardeAgentASupprimer.addAll(listJoursGardeAgent);

			// on met à jour
			for (JoursFeriesSaisiesGardeDto jourDto : dto.getJoursFeriesEnGarde()) {

				// on verifie que c est bien un jour ferie ou chome
				if (!sirhWSConsumer.isJourHoliday(jourDto.getJourFerie())) {
					logger.debug(String.format(ERREUR_JOUR_FERIE_ERRONE, sdf.format(jourDto.getJourFerie())));
					result.getErrors().add(String.format(ERREUR_JOUR_FERIE_ERRONE, sdf.format(jourDto.getJourFerie())));
					break;
				}

				boolean isExist = false;
				for (AgentJoursFeriesGarde joursGardeAgentExist : listJoursGardeAgent) {
					// on verifie s il existe deja en BDD
					if (jourDto.getJourFerie().equals(joursGardeAgentExist.getJourFerieChome()) && jourDto.isCheck()) {
						isExist = true;
						listJoursGardeAgentASupprimer.remove(joursGardeAgentExist);
						break;
					}
				}
				// si non on le cree
				if (!isExist && jourDto.isCheck()) {
					AgentJoursFeriesGarde agentJourGarde = new AgentJoursFeriesGarde();
					agentJourGarde.setDateModification(new Date());
					agentJourGarde.setIdAgent(dto.getAgent().getIdAgent());
					agentJourGarde.setJourFerieChome(jourDto.getJourFerie());
					agentJoursFeriesGardeRepository.persistEntity(agentJourGarde);
				}
			}

			// on supprime le reste
			for (AgentJoursFeriesGarde gardeToDelete : listJoursGardeAgentASupprimer) {
				agentJoursFeriesGardeRepository.removeEntity(gardeToDelete);
			}
		}

		return result;
	}

}
