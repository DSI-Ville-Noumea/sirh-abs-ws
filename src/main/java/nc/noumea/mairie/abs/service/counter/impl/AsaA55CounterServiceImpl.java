package nc.noumea.mairie.abs.service.counter.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentAsaA55Count;
import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.MotifCompteur;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.dto.CompteurAsaDto;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.service.AgentNotFoundException;

import org.springframework.stereotype.Service;

@Service("AsaA55CounterServiceImpl")
public class AsaA55CounterServiceImpl extends AbstractCounterService {

	@Override
	public ReturnMessageDto majManuelleCompteurToAgent(Integer idAgent, CompteurDto compteurDto) {

		logger.info("Trying to update ASA A55 manually counters for Agent {} ...", compteurDto.getIdAgent());

		ReturnMessageDto result = new ReturnMessageDto();

		result = super.majManuelleCompteurToAgent(idAgent, compteurDto);
		if (!result.getErrors().isEmpty())
			return result;

		result = majManuelleCompteurAsaA55ToAgent(idAgent, compteurDto, result, RefTypeAbsenceEnum.ASA_A55.getValue());

		return result;
	}

	/**
	 * appeler depuis Kiosque ou SIRH l historique ABS_AGENT_WEEK_ALIM_MANUELLE
	 * mise a jour
	 */
	private ReturnMessageDto majManuelleCompteurAsaA55ToAgent(Integer idAgent, CompteurDto compteurDto,
			ReturnMessageDto result, Integer idRefTypeAbsence) {

		logger.info("Trying to update manually ASA A55 counters for Agent {} ...", compteurDto.getIdAgent());

		Double nbHeures = helperService.calculJoursAlimManuelleCompteur(compteurDto);

		try {
			return majManuelleCompteurToAgent(idAgent, compteurDto, nbHeures, idRefTypeAbsence, result);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("An error occured while trying to update recuperation counters :", e);
		}
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
			Double nbHeures, Integer idRefTypeAbsence, ReturnMessageDto srm) throws InstantiationException,
			IllegalAccessException {

		if (sirhRepository.getAgent(compteurDto.getIdAgent()) == null) {
			logger.error("There is no Agent [{}]. Impossible to update its counters.", compteurDto.getIdAgent());
			throw new AgentNotFoundException();
		}

		logger.info("updating counters for Agent [{}] with {} heures for dateDeb {} and dateFin {}...",
				compteurDto.getIdAgent(), nbHeures, compteurDto.getDateDebut(), compteurDto.getDateFin());

		AgentAsaA55Count arc = (AgentAsaA55Count) counterRepository.getAgentCounterByDate(AgentAsaA55Count.class,
				compteurDto.getIdAgent(), compteurDto.getDateDebut());

		if (arc == null) {
			arc = new AgentAsaA55Count();
			arc.setIdAgent(compteurDto.getIdAgent());
		}

		if (!srm.getErrors().isEmpty()) {
			return srm;
		}

		MotifCompteur motifCompteur = counterRepository
				.getEntity(MotifCompteur.class, compteurDto.getIdMotifCompteur());
		if (null == motifCompteur) {
			logger.warn(MOTIF_COMPTEUR_INEXISTANT);
			srm.getErrors().add(String.format(MOTIF_COMPTEUR_INEXISTANT));
			return srm;
		}

		AgentHistoAlimManuelle histo = new AgentHistoAlimManuelle();
		histo.setIdAgent(idAgentOperateur);
		histo.setIdAgentConcerne(compteurDto.getIdAgent());
		histo.setDateModification(helperService.getCurrentDate());
		histo.setMotifCompteur(motifCompteur);
		String textLog = "";
		if (null != compteurDto.getDureeAAjouter()) {
			textLog = "Mise en place de " + nbHeures + " heures pour la période du " + compteurDto.getDateDebut()
					+ " au " + compteurDto.getDateFin() + ".";
		}
		histo.setText(textLog);

		RefTypeAbsence rta = new RefTypeAbsence();
		rta.setIdRefTypeAbsence(idRefTypeAbsence);
		histo.setType(rta);

		arc.setTotalHeures(nbHeures);
		arc.setDateDebut(compteurDto.getDateDebut());
		arc.setDateFin(compteurDto.getDateFin());
		arc.setLastModification(helperService.getCurrentDate());

		counterRepository.persistEntity(arc);
		counterRepository.persistEntity(histo);

		return srm;
	}

	@Override
	public List<CompteurAsaDto> getListeCompteur() {
		List<CompteurAsaDto> result = new ArrayList<>();

		List<AgentAsaA55Count> listeArc = counterRepository.getListCounter(AgentAsaA55Count.class);
		for (AgentAsaA55Count arc : listeArc) {
			CompteurAsaDto dto = new CompteurAsaDto(arc);
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

		Double heures = calculHeuresAlimAutoCompteur(demandeEtatChangeDto, demande, demande.getDateDebut(),
				demande.getDateFin());
		if (0.0 != heures) {
			try {
				srm = majCompteurToAgent(demande.getIdAgent(), heures, srm);
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
	protected <T1, T2> ReturnMessageDto majCompteurToAgent(Integer idAgent, Double heures, ReturnMessageDto srm)
			throws InstantiationException, IllegalAccessException {

		if (sirhRepository.getAgent(idAgent) == null) {
			logger.error("There is no Agent [{}]. Impossible to update its counters.", idAgent);
			throw new AgentNotFoundException();
		}

		logger.info("updating counters for Agent [{}] with {} heures...", idAgent, heures);

		AgentAsaA55Count arc = (AgentAsaA55Count) counterRepository.getAgentCounter(AgentAsaA55Count.class, idAgent);

		if (arc == null) {
			logger.warn(COMPTEUR_INEXISTANT);
			srm.getErrors().add(String.format(COMPTEUR_INEXISTANT));
			return srm;
		}

		// on verifie que le solde est positif seulement si on debite le
		// compteur
		if (0.0 > heures && 0.0 > arc.getTotalHeures() + heures) {
			logger.warn(SOLDE_COMPTEUR_NEGATIF_AUTORISE);
			srm.getInfos().add(String.format(SOLDE_COMPTEUR_NEGATIF_AUTORISE));
		}

		arc.setTotalHeures(arc.getTotalHeures() + heures);
		arc.setLastModification(helperService.getCurrentDate());

		counterRepository.persistEntity(arc);

		return srm;
	}

	/**
	 * 
	 * @param demandeEtatChangeDto
	 * @param demande
	 * @param dateDebut
	 * @param dateFin
	 * @return Double nombre heures a incrementer/decrementer du compteur
	 */
	protected Double calculHeuresAlimAutoCompteur(DemandeEtatChangeDto demandeEtatChangeDto, Demande demande,
			Date dateDebut, Date dateFin) {
		Double heures = 0.0;
		// si on approuve, le compteur decremente
		if (demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.VALIDEE.getCodeEtat())) {
			heures = 0.0 - helperService.calculNombreHeures(dateDebut, dateFin);
		}
		// si on passe de Approuve a Refuse, le compteur incremente
		if (demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.ANNULEE.getCodeEtat())
				&& (demande.getLatestEtatDemande().getEtat().equals(RefEtatEnum.VALIDEE) || demande
						.getLatestEtatDemande().getEtat().equals(RefEtatEnum.PRISE))) {
			heures = helperService.calculNombreHeures(dateDebut, dateFin);
		}

		return heures;
	}
}
