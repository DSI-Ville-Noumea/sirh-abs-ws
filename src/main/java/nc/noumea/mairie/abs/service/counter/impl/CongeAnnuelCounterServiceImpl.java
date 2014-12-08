package nc.noumea.mairie.abs.service.counter.impl;

import java.util.List;

import nc.noumea.mairie.abs.domain.AgentCongeAnnuelCount;
import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("CongeAnnuelCounterServiceImpl")
public class CongeAnnuelCounterServiceImpl extends AbstractCounterService {

	@Override
	@Transactional(value = "absTransactionManager")
	public ReturnMessageDto intitCompteurCongeAnnuel(Integer idAgent, Integer idAgentConcerne) {

		logger.info("Trying to initiate manually counters for Agent {} ...", idAgentConcerne);

		// tester si agent est un utilisateur SIRH
		ReturnMessageDto result = sirhWSConsumer.isUtilisateurSIRH(idAgent);
		if (!result.getErrors().isEmpty()) {
			// seuls les utilisateurs de SIRH ont le droit de faire cette action
			return result;
		}

		// on verifie que l'agent n'a pas deja un compteur
		AgentCongeAnnuelCount arcExistant = (AgentCongeAnnuelCount) counterRepository.getAgentCounter(
				AgentCongeAnnuelCount.class, idAgentConcerne);

		if (arcExistant != null) {
			logger.warn(COMPTEUR_EXISTANT);
			result.getErrors().add(String.format(COMPTEUR_EXISTANT));
			return result;
		} else {
			AgentCongeAnnuelCount arc = new AgentCongeAnnuelCount();
			arc.setIdAgent(idAgentConcerne);

			arc.setTotalJours(0.0);
			arc.setTotalJoursAnneeN1(0.0);
			arc.setLastModification(helperService.getCurrentDate());

			AgentHistoAlimManuelle histo = new AgentHistoAlimManuelle();
			histo.setIdAgent(idAgent);
			histo.setIdAgentConcerne(arc.getIdAgent());
			histo.setDateModification(helperService.getCurrentDate());
			histo.setMotifCompteur(null);
			histo.setMotifTechnique(INITIATE_COMPTEUR);
			histo.setText(INITIATE_COMPTEUR);
			histo.setCompteurAgent(arc);

			RefTypeAbsence rta = new RefTypeAbsence();
			rta.setIdRefTypeAbsence(RefTypeAbsenceEnum.CONGE_ANNUEL.getValue());
			histo.setType(rta);

			counterRepository.persistEntity(arc);
			counterRepository.persistEntity(histo);
		}

		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Integer> getListAgentCongeAnnuelCountForReset() {
		return counterRepository.getListAgentCongeAnnuelCountForReset();
	}

	@Override
	@Transactional(value = "absTransactionManager")
	public ReturnMessageDto resetCompteurCongeAnnuel(Integer idAgentCount) {

		logger.info("reset CompteurCongeAnnuel for idAgentCount {} ...", idAgentCount);

		ReturnMessageDto srm = new ReturnMessageDto();

		AgentCongeAnnuelCount arc = counterRepository.getEntity(AgentCongeAnnuelCount.class, idAgentCount);

		if (arc == null) {
			logger.warn(COMPTEUR_INEXISTANT);
			srm.getErrors().add(String.format(COMPTEUR_INEXISTANT));
			return srm;
		}

		// selon la SFD, compteur annee en cours à ajouter au compteur de
		// l'année precedente
		// et on remet le compteur de l'année à 0

		AgentHistoAlimManuelle histo = new AgentHistoAlimManuelle();
		histo.setIdAgent(arc.getIdAgent());
		histo.setIdAgentConcerne(arc.getIdAgent());
		histo.setDateModification(helperService.getCurrentDate());
		histo.setMotifCompteur(null);
		histo.setMotifTechnique(RESET_COMPTEUR_ANNEE_EN_COURS);
		String textLog = "Retrait de " + (0 - arc.getTotalJours()) + " jours sur la nouvelle année.";
		histo.setText(textLog);
		histo.setCompteurAgent(arc);

		RefTypeAbsence rta = new RefTypeAbsence();
		rta.setIdRefTypeAbsence(RefTypeAbsenceEnum.CONGE_ANNUEL.getValue());
		histo.setType(rta);

		arc.setTotalJoursAnneeN1(arc.getTotalJoursAnneeN1() + arc.getTotalJours());
		arc.setTotalJours(0.0);

		counterRepository.persistEntity(arc);
		counterRepository.persistEntity(histo);

		return srm;
	}
}
