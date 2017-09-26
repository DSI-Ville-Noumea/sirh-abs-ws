package nc.noumea.mairie.abs.service.counter.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentAsaA55Count;
import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeAsa;
import nc.noumea.mairie.abs.domain.MotifCompteur;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.service.AgentNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("AsaA55CounterServiceImpl")
public class AsaA55CounterServiceImpl extends AsaCounterServiceImpl {

	/**
	 * appeler depuis Kiosque ou SIRH l historique ABS_AGENT_WEEK_ALIM_MANUELLE
	 * mise a jour
	 */
	@Override
	protected ReturnMessageDto majManuelleCompteurToAgent(Integer idAgent, CompteurDto compteurDto,
			ReturnMessageDto result, MotifCompteur motifCompteur, boolean compteurExistantBloquant) {

		logger.info("Trying to update manually ASA A55 counters for Agent {} ...", compteurDto.getIdAgent());

		try {
			Double dMinutes = helperService.calculAlimManuelleCompteur(compteurDto);
			Integer minutes = null != dMinutes ? dMinutes.intValue() : 0;
			return majManuelleCompteurToAgent(idAgent, compteurDto, minutes, RefTypeAbsenceEnum.ASA_A55.getValue(),
					result, motifCompteur);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("An error occured while trying to update recuperation counters :", e);
		}
	}
	
	/**
	 * Retourne le nombre total d'enregistrement par année si spécifiée, pour la pagination des données.
	 */
	@Override
	@Transactional(value = "absTransactionManager")
	public Integer countAllByYear(Integer annee, Integer idOS) {
		return counterRepository.countAllByYear(AgentAsaA55Count.class, annee);
	}

	/**
	 * Mise à jour manuelle du compteur de ASA A55
	 * 
	 * @param T1
	 *            inherits BaseAgentCount
	 * @param idAgent
	 * @param minutes
	 *            : negatif pour debiter, positif pour crediter
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	protected <T1, T2> ReturnMessageDto majManuelleCompteurToAgent(Integer idAgentOperateur, CompteurDto compteurDto,
			int nbMinutes, Integer idRefTypeAbsence, ReturnMessageDto srm, MotifCompteur motifCompteur)
			throws InstantiationException, IllegalAccessException {

		if (sirhWSConsumer.getAgent(compteurDto.getIdAgent()) == null) {
			logger.error("There is no Agent [{}]. Impossible to update its counters.", compteurDto.getIdAgent());
			throw new AgentNotFoundException();
		}

		logger.info("updating counters for Agent [{}] with {} minutes for dateDeb {} and dateFin {}...",
				compteurDto.getIdAgent(), nbMinutes, compteurDto.getDateDebut(), compteurDto.getDateFin());

		AgentAsaA55Count arc = (AgentAsaA55Count) counterRepository.getAgentCounterByDate(AgentAsaA55Count.class,
				compteurDto.getIdAgent(), compteurDto.getDateDebut());

		if (arc == null) {
			arc = new AgentAsaA55Count();
			arc.setIdAgent(compteurDto.getIdAgent());
		}

		String textLog = "";
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		if (null != compteurDto.getDureeAAjouter()) {
			textLog = "Mise en place de " + helperService.getHeureMinuteToString(nbMinutes) + " pour la période du "
					+ sdf.format(compteurDto.getDateDebut()) + " au " + sdf.format(compteurDto.getDateFin()) + ".";
		}

		arc.setTotalMinutes(nbMinutes);
		arc.setDateDebut(compteurDto.getDateDebut());
		arc.setDateFin(compteurDto.getDateFin());
		arc.setLastModification(helperService.getCurrentDate());

		counterRepository.persistEntity(arc);
		majAgentHistoAlimManuelle(idAgentOperateur, compteurDto.getIdAgent(), motifCompteur, textLog, arc,
				idRefTypeAbsence);

		return srm;
	}

	@Override
	@Transactional(readOnly = true)
	public List<CompteurDto> getListeCompteur(Integer idOrganisation, Integer annee) {
		List<CompteurDto> result = new ArrayList<>();

		List<AgentAsaA55Count> listeArc = counterRepository.getListCounter(AgentAsaA55Count.class);
		for (AgentAsaA55Count arc : listeArc) {
			List<AgentHistoAlimManuelle> list = counterRepository.getListHisto(arc.getIdAgent(), arc);
			CompteurDto dto = new CompteurDto(arc, list.size() > 0 ? list.get(0) : null);
			result.add(dto);
		}
		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public List<CompteurDto> getListeCompteur(Integer idOrganisation, Integer annee, Integer pageSize, Integer pageNumber) {
		List<CompteurDto> result = new ArrayList<>();

		List<AgentAsaA55Count> listeArc = counterRepository.getListCounterByAnnee(AgentAsaA55Count.class, null, pageSize, pageNumber);
		for (AgentAsaA55Count arc : listeArc) {
			List<AgentHistoAlimManuelle> list = counterRepository.getListHisto(arc.getIdAgent(), arc);
			CompteurDto dto = new CompteurDto(arc, list.size() > 0 ? list.get(0) : null);
			result.add(dto);
		}
		return result;
	}

	/**
	 * appeler depuis ABSENCE l historique ABS_AGENT_WEEK_... n est pas utilise
	 */
	@Override
	public ReturnMessageDto majCompteurToAgent(ReturnMessageDto srm, Demande demande,
			DemandeEtatChangeDto demandeEtatChangeDto) {

		logger.info("Trying to update ASA_A55 counters for Agent [{}] ...", demande.getIdAgent());

		int minutes = calculMinutesAlimAutoCompteur(demandeEtatChangeDto, demande, demande.getDateDebut(),
				demande.getDateFin());
		if (0 != minutes) {
			try {
				srm = majCompteurToAgent((DemandeAsa) demande, minutes, srm);
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException("An error occured while trying to update ASA_A55 counters :", e);
			}
		}
		return srm;
	}

	/**
	 * Mets à jour le compteur de minutes désiré (en fonction des types passés
	 * en paramètre) sans mettre a jour l historique
	 * 
	 * Dans le cas des ReposComp, il faut gérer l'année N-1 et N dans le debit
	 * et le credit
	 * 
	 * @param T1
	 *            inherits BaseAgentCount
	 * @param idAgent
	 * @param minutes
	 *            : negatif pour debiter, positif pour crediter
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	protected <T1, T2> ReturnMessageDto majCompteurToAgent(DemandeAsa demande, int minutes, ReturnMessageDto srm)
			throws InstantiationException, IllegalAccessException {

		if (sirhWSConsumer.getAgent(demande.getIdAgent()) == null) {
			logger.error("There is no Agent [{}]. Impossible to update its counters.", demande.getIdAgent());
			throw new AgentNotFoundException();
		}

		logger.info("updating counters for Agent [{}] with {} minutes...", demande.getIdAgent(), minutes);

		// #174004 : on cherche le bon compteur par rapport à la date de debut
		// de la demande
		AgentAsaA55Count arc = (AgentAsaA55Count) counterRepository.getAgentCounterByDate(AgentAsaA55Count.class,
				demande.getIdAgent(), demande.getDateDebut());

		if (arc == null) {
			logger.warn(COMPTEUR_INEXISTANT);
			srm.getErrors().add(String.format(COMPTEUR_INEXISTANT));
			return srm;
		}

		// on verifie que le solde est positif seulement si on debite le
		// compteur
		if (0 > minutes && 0.0 > arc.getTotalMinutes() + minutes) {
			logger.warn(SOLDE_COMPTEUR_NEGATIF_AUTORISE);
			srm.getInfos().add(String.format(SOLDE_COMPTEUR_NEGATIF_AUTORISE));
		}

		// #13519 maj solde sur la demande
		Integer minutesOld = arc.getTotalMinutes();

		arc.setTotalMinutes(arc.getTotalMinutes() + minutes);
		arc.setLastModification(helperService.getCurrentDate());

		super.updateDemandeWithNewSolde(demande, 0.0, 0.0, minutesOld, arc.getTotalMinutes());

		counterRepository.persistEntity(arc);

		return srm;
	}

}
