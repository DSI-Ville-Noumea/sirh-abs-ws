package nc.noumea.mairie.abs.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentJoursFeriesRepos;
import nc.noumea.mairie.abs.dto.AgentDto;
import nc.noumea.mairie.abs.dto.AgentJoursFeriesReposDto;
import nc.noumea.mairie.abs.dto.JourDto;
import nc.noumea.mairie.abs.dto.JoursFeriesSaisiesReposDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.dto.SaisieReposDto;
import nc.noumea.mairie.abs.dto.SirhWsServiceDto;
import nc.noumea.mairie.abs.repository.IAgentJoursFeriesReposRepository;
import nc.noumea.mairie.abs.service.IAccessRightsService;
import nc.noumea.mairie.abs.service.ISaisieJoursFeriesReposService;
import nc.noumea.mairie.ws.ISirhWSConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SaisieJoursFeriesReposService implements ISaisieJoursFeriesReposService {

	private Logger logger = LoggerFactory.getLogger(SaisieJoursFeriesReposService.class);

	@Autowired
	private IAgentJoursFeriesReposRepository agentJoursFeriesReposRepository;

	@Autowired
	private IAccessRightsService accessRightsService;

	@Autowired
	protected ISirhWSConsumer sirhWSConsumer;

	protected final static String ERREUR_JOUR_FERIE_ERRONE = "Le jour de repos %s n'est pas un jour férié ou chômé.";

	private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

	@Override
	@Transactional(readOnly = true)
	public SaisieReposDto getListAgentsWithJoursFeriesEnRepos(Integer idAgent, String codeService, Date dateDebut,
			Date dateFin) {

		logger.debug("Start getListAgentsWithJoursFeriesEnRepos");

		SaisieReposDto result = new SaisieReposDto();

		List<AgentDto> listAgentTemp = accessRightsService.getAgentsToApproveOrInput(idAgent, codeService);
		List<AgentDto> listAgent = new ArrayList<>();
		for (AgentDto ag : listAgentTemp) {
			SirhWsServiceDto service = sirhWSConsumer.getAgentDirection(ag.getIdAgent(), new Date());
			if (null != service && null != service.getSigle() && service.getSigle().toUpperCase().equals("DPM")) {
				listAgent.add(ag);
			}
		}

		List<JourDto> listjourFerie = sirhWSConsumer.getListeJoursFeries(dateDebut, dateFin);
		result.setJoursFerieHeader(listjourFerie);

		// on boucle sur les lignes Agents
		for (AgentDto agent : listAgent) {

			AgentJoursFeriesReposDto dto = new AgentJoursFeriesReposDto();
			dto.setAgent(agent);

			List<AgentJoursFeriesRepos> listJoursReposAgent = agentJoursFeriesReposRepository
					.getAgentJoursFeriesReposByIdAgentAndPeriode(agent.getIdAgent(), dateDebut, dateFin);
			// on boucle sur les jours feries existant sous SIRH
			if (null != listjourFerie) {
				for (JourDto jourFerieSirh : listjourFerie) {
					JoursFeriesSaisiesReposDto joursDto = new JoursFeriesSaisiesReposDto();
					joursDto.setJourFerie(jourFerieSirh.getJour());
					joursDto.setCheck(checkAgentReposJourDonne(listJoursReposAgent, jourFerieSirh.getJour()));
					dto.getJoursFeriesEnRepos().add(joursDto);
				}
			}
			result.getListAgentAvecRepos().add(dto);
		}

		return result;
	}

	protected boolean checkAgentReposJourDonne(List<AgentJoursFeriesRepos> listJoursReposAgent, Date dateFerie) {

		if (null != listJoursReposAgent) {
			for (AgentJoursFeriesRepos jourFerie : listJoursReposAgent) {
				if (jourFerie.getJourFerieChome().equals(dateFerie))
					return true;
			}
		}

		return false;
	}

	@Override
	@Transactional(value = "absTransactionManager")
	public ReturnMessageDto setListAgentsWithJoursFeriesEnRepos(Integer idAgent,
			List<AgentJoursFeriesReposDto> listDto, Date dateDebut, Date dateFin) {

		logger.debug("Start setListAgentsWithJoursFeriesEnRepos for idAgent = {}", idAgent);

		ReturnMessageDto result = new ReturnMessageDto();

		for (AgentJoursFeriesReposDto dto : listDto) {
			// on verifie les droits
			result = accessRightsService.verifAccessRightDemande(idAgent, dto.getAgent().getIdAgent(), result);
			if (0 < result.getErrors().size()) {
				return result;
			}

			// repos deja existants
			List<AgentJoursFeriesRepos> listJoursReposAgent = agentJoursFeriesReposRepository
					.getAgentJoursFeriesReposByIdAgentAndPeriode(dto.getAgent().getIdAgent(), dateDebut, dateFin);

			List<AgentJoursFeriesRepos> listJoursReposAgentASupprimer = new ArrayList<AgentJoursFeriesRepos>();
			listJoursReposAgentASupprimer.addAll(listJoursReposAgent);

			// on met à jour
			for (JoursFeriesSaisiesReposDto jourDto : dto.getJoursFeriesEnRepos()) {

				// on verifie que c est bien un jour ferie ou chome
				if (!sirhWSConsumer.isJourHoliday(jourDto.getJourFerie())) {
					logger.debug(String.format(ERREUR_JOUR_FERIE_ERRONE, sdf.format(jourDto.getJourFerie())));
					result.getErrors().add(String.format(ERREUR_JOUR_FERIE_ERRONE, sdf.format(jourDto.getJourFerie())));
					break;
				}

				boolean isExist = false;
				for (AgentJoursFeriesRepos joursReposAgentExist : listJoursReposAgent) {
					// on verifie s il existe deja en BDD
					if (jourDto.getJourFerie().equals(joursReposAgentExist.getJourFerieChome()) && jourDto.isCheck()) {
						isExist = true;
						listJoursReposAgentASupprimer.remove(joursReposAgentExist);
						break;
					}
				}
				// si non on le cree
				if (!isExist && jourDto.isCheck()) {
					AgentJoursFeriesRepos agentJourRepos = new AgentJoursFeriesRepos();
					agentJourRepos.setDateModification(new Date());
					agentJourRepos.setIdAgent(dto.getAgent().getIdAgent());
					agentJourRepos.setJourFerieChome(jourDto.getJourFerie());
					agentJoursFeriesReposRepository.persistEntity(agentJourRepos);
				}
			}

			// on supprime le reste
			for (AgentJoursFeriesRepos reposToDelete : listJoursReposAgentASupprimer) {
				agentJoursFeriesReposRepository.removeEntity(reposToDelete);
			}
		}

		return result;
	}

}
