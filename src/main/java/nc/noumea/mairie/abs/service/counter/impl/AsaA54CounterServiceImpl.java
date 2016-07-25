package nc.noumea.mairie.abs.service.counter.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nc.noumea.mairie.abs.domain.AgentA54OrganisationSyndicale;
import nc.noumea.mairie.abs.domain.AgentAsaA54Count;
import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;
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

@Service("AsaA54CounterServiceImpl")
public class AsaA54CounterServiceImpl extends AsaCounterServiceImpl {

	/**
	 * appeler depuis Kiosque ou SIRH l historique ABS_AGENT_WEEK_ALIM_MANUELLE
	 * mise a jour
	 */
	@Override
	protected ReturnMessageDto majManuelleCompteurToAgent(Integer idAgent, CompteurDto compteurDto, ReturnMessageDto result,
			MotifCompteur motifCompteur, boolean compteurExistantBloquant) {

		logger.info("Trying to update manually ASA A54 counters for Agent {} ...", compteurDto.getIdAgent());

		Double nbJours = helperService.calculAlimManuelleCompteur(compteurDto);

		try {
			return majManuelleCompteurToAgent(idAgent, compteurDto, nbJours, RefTypeAbsenceEnum.ASA_A54.getValue(), result, motifCompteur,
					compteurExistantBloquant);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("An error occured while trying to update ASA A54 counters :", e);
		}
	}

	/**
	 * Mise à jour manuelle du compteur de ASA A54
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
	protected <T1, T2> ReturnMessageDto majManuelleCompteurToAgent(Integer idAgentOperateur, CompteurDto compteurDto, Double nbJours,
			Integer idRefTypeAbsence, ReturnMessageDto srm, MotifCompteur motifCompteur, boolean compteurExistantBloquant)
			throws InstantiationException, IllegalAccessException {

		if (sirhWSConsumer.getAgent(compteurDto.getIdAgent()) == null) {
			logger.error("There is no Agent [{}]. Impossible to update its counters.", compteurDto.getIdAgent());
			throw new AgentNotFoundException();
		}

		Calendar cal = Calendar.getInstance();
		cal.setTime(compteurDto.getDateDebut());
		int annee = cal.get(Calendar.YEAR);

		logger.info("updating counters for Agent [{}] with {} nbJours for Year {}...", compteurDto.getIdAgent(), nbJours, annee);

		AgentAsaA54Count arc = (AgentAsaA54Count) counterRepository.getAgentCounterByDate(AgentAsaA54Count.class, compteurDto.getIdAgent(),
				compteurDto.getDateDebut());

		if (arc == null) {
			arc = new AgentAsaA54Count();
			arc.setIdAgent(compteurDto.getIdAgent());
		} else {
			if (compteurExistantBloquant) {
				logger.warn(String.format(COMPTEUR_EXISTANT, "pour l'agent " + compteurDto.getIdAgent()));
				srm.getErrors().add(String.format(COMPTEUR_EXISTANT, "pour l'agent " + compteurDto.getIdAgent()));
				return srm;
			}
		}

		String textLog = "";
		if (null != compteurDto.getDureeAAjouter()) {
			textLog = "Mise en place de " + nbJours + " jours pour l'année " + annee + ".";
		}

		arc.setTotalJours(nbJours);
		arc.setDateDebut(compteurDto.getDateDebut());
		arc.setDateFin(compteurDto.getDateFin());
		arc.setLastModification(helperService.getCurrentDate());
		arc.setActif(compteurDto.isActif());

		counterRepository.persistEntity(arc);
		majAgentHistoAlimManuelle(idAgentOperateur, compteurDto.getIdAgent(), motifCompteur, textLog, arc, idRefTypeAbsence);

		return srm;
	}

	@Override
	@Transactional(readOnly = true)
	public List<CompteurDto> getListeCompteur(Integer idOrganisation, Integer annee) {
		List<CompteurDto> result = new ArrayList<>();

		List<AgentAsaA54Count> listeArc = counterRepository.getListCounterByAnnee(AgentAsaA54Count.class, annee);
		for (AgentAsaA54Count arc : listeArc) {
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
	public ReturnMessageDto majCompteurToAgent(ReturnMessageDto srm, Demande demande, DemandeEtatChangeDto demandeEtatChangeDto) {

		logger.info("Trying to update recuperation counters for Agent [{}] ...", demande.getIdAgent());

		Double jours = calculJoursAlimAutoCompteur(demandeEtatChangeDto, demande, demande.getDateDebut(), demande.getDateFin());
		if (0.0 != jours) {
			try {
				srm = majCompteurToAgent((DemandeAsa) demande, jours, srm);
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException("An error occured while trying to update recuperation counters :", e);
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
	protected <T1, T2> ReturnMessageDto majCompteurToAgent(DemandeAsa demande, Double jours, ReturnMessageDto srm)
			throws InstantiationException, IllegalAccessException {

		if (sirhWSConsumer.getAgent(demande.getIdAgent()) == null) {
			logger.error("There is no Agent [{}]. Impossible to update its counters.", demande.getIdAgent());
			throw new AgentNotFoundException();
		}

		logger.info("updating counters for Agent [{}] with {} jours...", demande.getIdAgent(), jours);

		// #174004 : on cherche le bon compteur par rapport à la date de debut
		// de la demande
		AgentAsaA54Count arc = (AgentAsaA54Count) counterRepository.getAgentCounterByDate(AgentAsaA54Count.class, demande.getIdAgent(),
				demande.getDateDebut());

		if (arc == null) {
			logger.warn(COMPTEUR_INEXISTANT);
			srm.getErrors().add(String.format(COMPTEUR_INEXISTANT));
			return srm;
		}

		// on verifie que le solde est positif seulement si on debite le
		// compteur
		if (0.0 > jours && 0.0 > arc.getTotalJours() + jours) {
			logger.warn(SOLDE_COMPTEUR_NEGATIF_AUTORISE);
			srm.getInfos().add(String.format(SOLDE_COMPTEUR_NEGATIF_AUTORISE));
		}

		// #13519 maj solde sur la demande
		Double joursOld = arc.getTotalJours();

		arc.setTotalJours(arc.getTotalJours() + jours);
		arc.setLastModification(helperService.getCurrentDate());

		super.updateDemandeWithNewSolde(demande, joursOld, arc.getTotalJours(), 0, 0);

		counterRepository.persistEntity(arc);

		return srm;
	}

	@Override
	@Transactional(value = "absTransactionManager")
	public ReturnMessageDto saveRepresentantA54(Integer idOrganisationSyndicale, List<AgentOrganisationSyndicaleDto> listeAgentDto) {
		ReturnMessageDto srm = new ReturnMessageDto();

		// on verifie l'existante de l'OS
		OrganisationSyndicale organisationSyndicale = OSRepository.getEntity(OrganisationSyndicale.class, idOrganisationSyndicale);
		if (null == organisationSyndicale) {
			logger.warn(OS_INEXISTANT);
			srm.getErrors().add(String.format(OS_INEXISTANT));
			return srm;
		} else if (!organisationSyndicale.isActif()) {
			logger.warn(OS_INACTIVE);
			srm.getErrors().add(String.format(OS_INACTIVE));
			return srm;
		}

		List<AgentA54OrganisationSyndicale> listeDepart = OSRepository.getListeAgentA54Organisation(idOrganisationSyndicale);
		List<AgentA54OrganisationSyndicale> droitsToDelete = new ArrayList<AgentA54OrganisationSyndicale>(listeDepart);
		for (AgentOrganisationSyndicaleDto ag : listeAgentDto) {
			AgentA54OrganisationSyndicale agentOrganisationSyndicale = null;
			for (AgentA54OrganisationSyndicale agOrga : listeDepart) {
				if (agOrga.getIdAgent().equals(ag.getIdAgent())) {
					agentOrganisationSyndicale = agOrga;
					break;
				}
			}

			if (agentOrganisationSyndicale != null) {
				// verifier si pas deja actif dans une autre organisation
				List<AgentA54OrganisationSyndicale> listeAgentOrganisationSyndicale = OSRepository.getAgentA54Organisation(ag.getIdAgent());
				for (AgentA54OrganisationSyndicale agTest : listeAgentOrganisationSyndicale) {
					if (agTest.getOrganisationSyndicale().getIdOrganisationSyndicale() != idOrganisationSyndicale) {
						// si pas la bonne organisation
						logger.warn(AGENT_OS_EXISTANT, ag.getIdAgent());
						srm.getErrors().add(String.format(AGENT_OS_EXISTANT, ag.getIdAgent()));
						continue;
					}
				}
				droitsToDelete.remove(agentOrganisationSyndicale);
				agentOrganisationSyndicale.setIdAgent(ag.getIdAgent());
				agentOrganisationSyndicale.setOrganisationSyndicale(organisationSyndicale);

				// insert nouvelle ligne Agent Organisation syndicale
				counterRepository.persistEntity(agentOrganisationSyndicale);

				logger.info("Updated AgentA54OrganisationSyndicale id {}.", agentOrganisationSyndicale.getIdA54AgentOrganisationSyndicale());
				continue;
			} else {
				// verifier si pas deja actif dans une autre organisation
				List<AgentA54OrganisationSyndicale> listeAgentOrganisationSyndicale = OSRepository.getAgentA54Organisation(ag.getIdAgent());
				for (AgentA54OrganisationSyndicale agTest : listeAgentOrganisationSyndicale) {
					if (agTest.getOrganisationSyndicale().getIdOrganisationSyndicale() != idOrganisationSyndicale) {
						// si pas la bonne organisation
						logger.warn(AGENT_OS_EXISTANT, ag.getIdAgent());
						srm.getErrors().add(String.format(AGENT_OS_EXISTANT, ag.getIdAgent()));
						continue;
					}
				}
				agentOrganisationSyndicale = new AgentA54OrganisationSyndicale();
				agentOrganisationSyndicale.setIdAgent(ag.getIdAgent());
				agentOrganisationSyndicale.setOrganisationSyndicale(organisationSyndicale);

				// insert nouvelle ligne Agent Organisation syndicale
				counterRepository.persistEntity(agentOrganisationSyndicale);

				logger.info("Added AgentA54OrganisationSyndicale id {}.", agentOrganisationSyndicale.getIdA54AgentOrganisationSyndicale());
			}
		}

		// on supprime les autres
		for (AgentA54OrganisationSyndicale agToDelete : droitsToDelete) {
			if (null != organisationSyndicale.getAgentsA54() && organisationSyndicale.getAgentsA54().contains(agToDelete)) {
				organisationSyndicale.getAgentsA54().remove(agToDelete);
				logger.info("Deleted AgentA54OrganisationSyndicale id {}.", agToDelete.getIdA54AgentOrganisationSyndicale());
			}
		}

		return srm;
	}

	@Override
	@Transactional(value = "absTransactionManager")
	public List<AgentOrganisationSyndicaleDto> listeRepresentantA54(Integer idOrganisationSyndicale) {
		List<AgentOrganisationSyndicaleDto> result = new ArrayList<>();

		List<AgentA54OrganisationSyndicale> listeOrg = OSRepository.getListeAgentA54Organisation(idOrganisationSyndicale);
		for (AgentA54OrganisationSyndicale ag : listeOrg) {
			AgentOrganisationSyndicaleDto dto = new AgentOrganisationSyndicaleDto(ag);
			result.add(dto);
		}
		return result;
	}

}
