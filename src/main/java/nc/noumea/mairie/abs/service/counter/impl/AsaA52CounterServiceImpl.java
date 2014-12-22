package nc.noumea.mairie.abs.service.counter.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentAsaA52Count;
import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;
import nc.noumea.mairie.abs.domain.AgentOrganisationSyndicale;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeAsa;
import nc.noumea.mairie.abs.domain.MotifCompteur;
import nc.noumea.mairie.abs.domain.OrganisationSyndicale;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.dto.AgentOrganisationSyndicaleDto;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.service.AgentNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("AsaA52CounterServiceImpl")
public class AsaA52CounterServiceImpl extends AsaCounterServiceImpl {

	/**
	 * appeler depuis Kiosque ou SIRH l historique ABS_AGENT_WEEK_ALIM_MANUELLE
	 * mise a jour
	 */
	@Override
	protected ReturnMessageDto majManuelleCompteurToAgent(Integer idAgent, CompteurDto compteurDto,
			ReturnMessageDto result, MotifCompteur motifCompteur) {

		logger.info("Trying to update manually ASA A52 counters for Agent {} ...", compteurDto.getIdAgent());

		try {
			Double dMinutes = helperService.calculAlimManuelleCompteur(compteurDto);
			Integer minutes = null != dMinutes ? dMinutes.intValue() : 0;
			return majManuelleCompteurToAgent(idAgent, compteurDto, minutes, RefTypeAbsenceEnum.ASA_A52.getValue(),
					result, motifCompteur);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("An error occured while trying to update recuperation counters :", e);
		}
	}

	/**
	 * Mise à jour manuelle du compteur de ASA A52
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

		logger.info("updating counters for Agent [{}] with {} heures for dateDeb {} and dateFin {}...",
				compteurDto.getIdAgent(), nbMinutes, compteurDto.getDateDebut(), compteurDto.getDateFin());

		OrganisationSyndicale organisationSyndicale = OSRepository.getEntity(OrganisationSyndicale.class, compteurDto
				.getOrganisationSyndicaleDto().getIdOrganisation());
		if (null == organisationSyndicale) {
			logger.warn(OS_INEXISTANT);
			srm.getErrors().add(String.format(OS_INEXISTANT));
			return srm;
		} else if (!organisationSyndicale.isActif()) {
			logger.warn(OS_INACTIVE);
			srm.getErrors().add(String.format(OS_INACTIVE));
			return srm;
		}

		AgentAsaA52Count arc = (AgentAsaA52Count) counterRepository.getOSCounterByDate(AgentAsaA52Count.class,
				compteurDto.getOrganisationSyndicaleDto().getIdOrganisation(), compteurDto.getDateDebut());

		if (arc == null) {
			arc = new AgentAsaA52Count();
			arc.setOrganisationSyndicale(organisationSyndicale);
		}

		String textLog = "";
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		if (null != compteurDto.getDureeAAjouter()) {
			textLog = "Mise en place de " + nbMinutes + " minutes pour la période du "
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
	public List<CompteurDto> getListeCompteur() {
		List<CompteurDto> result = new ArrayList<>();

		List<AgentAsaA52Count> listeArc = counterRepository.getListCounter(AgentAsaA52Count.class);
		for (AgentAsaA52Count arc : listeArc) {
			List<AgentHistoAlimManuelle> list = counterRepository.getListHistoOrganisationSyndicale(arc);
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

		logger.info("Trying to update ASA_A52 counters for Agent [{}] ...", demande.getIdAgent());

		int minutes = calculMinutesAlimAutoCompteur(demandeEtatChangeDto, demande, demande.getDateDebut(),
				demande.getDateFin());
		if (0 != minutes) {
			try {
				srm = majCompteurToAgent(demande.getIdAgent(), ((DemandeAsa) demande).getOrganisationSyndicale()
						.getIdOrganisationSyndicale(), minutes, demande.getDateDebut(), srm);
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException("An error occured while trying to update ASA_A52 counters :", e);
			}
		}
		return srm;
	}

	/**
	 * Mets à jour le compteur de minutes désiré (en fonction des types passés
	 * en paramètre) sans mettre a jour l historique
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
	protected <T1, T2> ReturnMessageDto majCompteurToAgent(Integer idAgent, Integer idOrganisationSyndicale,
			int minutes, Date dateDebutDemande, ReturnMessageDto srm) throws InstantiationException,
			IllegalAccessException {

		if (sirhWSConsumer.getAgent(idAgent) == null) {
			logger.error("There is no Agent [{}]. Impossible to update its counters.", idAgent);
			throw new AgentNotFoundException();
		}

		logger.info("updating counters for Agent [{}] with {} minutes...", idAgent, minutes);

		OrganisationSyndicale organisationSyndicale = OSRepository.getEntity(OrganisationSyndicale.class,
				idOrganisationSyndicale);
		if (null == organisationSyndicale) {
			logger.warn(OS_INEXISTANT);
			srm.getErrors().add(String.format(OS_INEXISTANT));
			return srm;
		} else if (!organisationSyndicale.isActif()) {
			logger.warn(OS_INACTIVE);
			srm.getErrors().add(String.format(OS_INACTIVE));
			return srm;
		}

		AgentAsaA52Count arc = (AgentAsaA52Count) counterRepository.getOSCounterByDate(AgentAsaA52Count.class,
				idOrganisationSyndicale, dateDebutDemande);

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

		arc.setTotalMinutes(arc.getTotalMinutes() + minutes);
		arc.setLastModification(helperService.getCurrentDate());

		counterRepository.persistEntity(arc);

		return srm;
	}

	@Override
	@Transactional(value = "absTransactionManager")
	public ReturnMessageDto saveRepresentantA52(Integer idOrganisationSyndicale,
			List<AgentOrganisationSyndicaleDto> listeAgentDto) {
		ReturnMessageDto srm = new ReturnMessageDto();

		// on verifie l'existante de l'OS
		OrganisationSyndicale organisationSyndicale = OSRepository.getEntity(OrganisationSyndicale.class,
				idOrganisationSyndicale);
		if (null == organisationSyndicale) {
			logger.warn(OS_INEXISTANT);
			srm.getErrors().add(String.format(OS_INEXISTANT));
			return srm;
		} else if (!organisationSyndicale.isActif()) {
			logger.warn(OS_INACTIVE);
			srm.getErrors().add(String.format(OS_INACTIVE));
			return srm;
		}

		List<AgentOrganisationSyndicale> listeDepart = OSRepository.getListeAgentOrganisation(idOrganisationSyndicale);
		List<AgentOrganisationSyndicale> droitsToDelete = new ArrayList<AgentOrganisationSyndicale>(listeDepart);
		for (AgentOrganisationSyndicaleDto ag : listeAgentDto) {
			AgentOrganisationSyndicale agentOrganisationSyndicale = null;
			for (AgentOrganisationSyndicale agOrga : listeDepart) {
				if (agOrga.getIdAgent().equals(ag.getIdAgent())) {
					agentOrganisationSyndicale = agOrga;
					break;
				}
			}

			if (agentOrganisationSyndicale != null) {
				// verifier si pas deja actif dans une autre organisation
				List<AgentOrganisationSyndicale> listeAgentOrganisationSyndicale = OSRepository.getAgentOrganisation(ag
						.getIdAgent());
				for (AgentOrganisationSyndicale agTest : listeAgentOrganisationSyndicale) {
					if (agTest.getOrganisationSyndicale().getIdOrganisationSyndicale() != idOrganisationSyndicale
							&& agTest.isActif()) {
						// si pas la bonne organisation et que agent actif
						logger.warn(AGENT_OS_EXISTANT, agTest.getIdAgent());
						srm.getErrors().add(String.format(AGENT_OS_EXISTANT, agTest.getIdAgent()));
						continue;
					}
				}
				droitsToDelete.remove(agentOrganisationSyndicale);
				agentOrganisationSyndicale.setActif(ag.isActif());
				agentOrganisationSyndicale.setIdAgent(ag.getIdAgent());
				agentOrganisationSyndicale.setOrganisationSyndicale(organisationSyndicale);

				// insert nouvelle ligne Agent Organisation syndicale
				counterRepository.persistEntity(agentOrganisationSyndicale);

				logger.info("Updated AgentOrganisationSyndicale id {}.",
						agentOrganisationSyndicale.getIdAgentOrganisationSyndicale());
				continue;
			} else {
				agentOrganisationSyndicale = new AgentOrganisationSyndicale();
				agentOrganisationSyndicale.setActif(ag.isActif());
				agentOrganisationSyndicale.setIdAgent(ag.getIdAgent());
				agentOrganisationSyndicale.setOrganisationSyndicale(organisationSyndicale);

				// insert nouvelle ligne Agent Organisation syndicale
				counterRepository.persistEntity(agentOrganisationSyndicale);

				logger.info("Added AgentOrganisationSyndicale id {}.",
						agentOrganisationSyndicale.getIdAgentOrganisationSyndicale());
			}

		}

		// on supprime les autres
		for (AgentOrganisationSyndicale agToDelete : droitsToDelete) {
			counterRepository.removeEntity(agToDelete);
			logger.info("Deleted AgentOrganisationSyndicale id {}.", agToDelete.getIdAgentOrganisationSyndicale());

		}

		return srm;
	}
}
