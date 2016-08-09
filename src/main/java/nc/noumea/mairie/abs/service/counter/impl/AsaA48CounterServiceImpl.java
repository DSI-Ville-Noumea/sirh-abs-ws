package nc.noumea.mairie.abs.service.counter.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nc.noumea.mairie.abs.domain.AgentA48OrganisationSyndicale;
import nc.noumea.mairie.abs.domain.AgentAsaA48Count;
import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeAsa;
import nc.noumea.mairie.abs.domain.MotifCompteur;
import nc.noumea.mairie.abs.domain.OrganisationSyndicale;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.service.AgentNotFoundException;

@Service("AsaA48CounterServiceImpl")
public class AsaA48CounterServiceImpl extends AsaCounterServiceImpl {

	/**
	 * appeler depuis Kiosque ou SIRH l historique ABS_AGENT_WEEK_ALIM_MANUELLE
	 * mise a jour
	 */
	@Override
	protected ReturnMessageDto majManuelleCompteurToAgent(Integer idAgent, CompteurDto compteurDto, ReturnMessageDto result,
			MotifCompteur motifCompteur, boolean compteurExistantBloquant) {

		logger.info("Trying to update manually ASA A48 counters for Agent {} ...", compteurDto.getIdAgent());

		Double nbJours = helperService.calculAlimManuelleCompteur(compteurDto);

		try {
			return majManuelleCompteurToAgent(idAgent, compteurDto, nbJours, RefTypeAbsenceEnum.ASA_A48.getValue(), result, motifCompteur);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("An error occured while trying to update recuperation counters :", e);
		}
	}

	/**
	 * Mise à jour manuelle du compteur de ASA A48
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
	protected ReturnMessageDto majManuelleCompteurToAgent(Integer idAgentOperateur, CompteurDto compteurDto, Double nbJours, Integer idRefTypeAbsence,
			ReturnMessageDto srm, MotifCompteur motifCompteur) throws InstantiationException, IllegalAccessException {

		if (sirhWSConsumer.getAgent(compteurDto.getIdAgent()) == null) {
			logger.error("There is no Agent [{}]. Impossible to update its counters.", compteurDto.getIdAgent());
			throw new AgentNotFoundException();
		}

		Calendar cal = Calendar.getInstance();
		cal.setTime(compteurDto.getDateDebut());
		int annee = cal.get(Calendar.YEAR);

		logger.info("updating counters for Agent [{}] with {} nbJours for Year {}...", compteurDto.getIdAgent(), nbJours, annee);

		AgentAsaA48Count arc = (AgentAsaA48Count) counterRepository.getAgentCounterByDate(AgentAsaA48Count.class, compteurDto.getIdAgent(),
				compteurDto.getDateDebut());

		if (arc == null) {
			arc = new AgentAsaA48Count();
			arc.setIdAgent(compteurDto.getIdAgent());
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
		if (idOrganisation == null) {
			List<AgentAsaA48Count> listeArc = counterRepository.getListCounterByAnnee(AgentAsaA48Count.class, annee);
			for (AgentAsaA48Count arc : listeArc) {
				List<AgentHistoAlimManuelle> list = counterRepository.getListHisto(arc.getIdAgent(), arc);
				// on regarde si il y a une saisie OS
				List<AgentA48OrganisationSyndicale> listeAgentOrganisationSyndicale = OSRepository.getAgentA48Organisation(arc.getIdAgent());
				CompteurDto dto = new CompteurDto(arc, list.size() > 0 ? list.get(0) : null,
						listeAgentOrganisationSyndicale == null || listeAgentOrganisationSyndicale.size() == 0 ? null
								: listeAgentOrganisationSyndicale.get(0));
				result.add(dto);
			}
		} else {
			List<AgentA48OrganisationSyndicale> listAg = OSRepository.getAgentA48OrganisationByOS(idOrganisation);
			for (AgentA48OrganisationSyndicale agOrga : listAg) {
				AgentAsaA48Count compteurAg = counterRepository.getAgentCounterByDate(AgentAsaA48Count.class, agOrga.getIdAgent(),
						new DateTime(annee, 1, 1, 0, 0, 0).toDate());
				List<AgentHistoAlimManuelle> list = counterRepository.getListHisto(compteurAg.getIdAgent(), compteurAg);
				// on regarde si il y a une saisie OS
				List<AgentA48OrganisationSyndicale> listeAgentOrganisationSyndicale = OSRepository.getAgentA48Organisation(compteurAg.getIdAgent());
				CompteurDto dto = new CompteurDto(compteurAg, list.size() > 0 ? list.get(0) : null,
						listeAgentOrganisationSyndicale == null || listeAgentOrganisationSyndicale.size() == 0 ? null
								: listeAgentOrganisationSyndicale.get(0));
					result.add(dto);
			}
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
		AgentAsaA48Count arc = (AgentAsaA48Count) counterRepository.getAgentCounterByDate(AgentAsaA48Count.class, demande.getIdAgent(),
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
	public ReturnMessageDto saveRepresentantA48(Integer idOrganisationSyndicale, Integer idAgent) {
		ReturnMessageDto srm = new ReturnMessageDto();
		if (idOrganisationSyndicale == 0) {
			// on est dans le cas d'une suppression
			List<AgentA48OrganisationSyndicale> listeAgentOrganisationSyndicale = OSRepository.getAgentA48Organisation(idAgent);
			AgentA48OrganisationSyndicale agentOrganisationSyndicale = null;
			if (listeAgentOrganisationSyndicale.size() == 1) {
				agentOrganisationSyndicale = listeAgentOrganisationSyndicale.get(0);
				OrganisationSyndicale organisationSyndicale = OSRepository.getEntity(OrganisationSyndicale.class,
						agentOrganisationSyndicale.getOrganisationSyndicale().getIdOrganisationSyndicale());
				organisationSyndicale.getAgentsA48().remove(agentOrganisationSyndicale);
				logger.info("Deleted AgentA48OrganisationSyndicale id {}.", agentOrganisationSyndicale.getIdA48AgentOrganisationSyndicale());

			} else if (listeAgentOrganisationSyndicale.size() > 1) {
				// si pas la bonne organisation
				logger.warn(AGENT_OS_EXISTANT, idAgent);
				srm.getErrors().add(String.format(AGENT_OS_EXISTANT, idAgent));
				return srm;
			}
			return srm;
		} else {
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

			// verifier si pas deja dans une autre organisation
			List<AgentA48OrganisationSyndicale> listeAgentOrganisationSyndicale = OSRepository.getAgentA48Organisation(idAgent);
			AgentA48OrganisationSyndicale agentOrganisationSyndicale = null;
			if (listeAgentOrganisationSyndicale.size() == 0) {
				agentOrganisationSyndicale = new AgentA48OrganisationSyndicale();
			} else if (listeAgentOrganisationSyndicale.size() == 1) {
				agentOrganisationSyndicale = listeAgentOrganisationSyndicale.get(0);
			} else {
				// si pas la bonne organisation
				logger.warn(AGENT_OS_EXISTANT, idAgent);
				srm.getErrors().add(String.format(AGENT_OS_EXISTANT, idAgent));
				return srm;
			}

			agentOrganisationSyndicale.setIdAgent(idAgent);
			agentOrganisationSyndicale.setOrganisationSyndicale(organisationSyndicale);

			// insert nouvelle ligne Agent Organisation syndicale
			counterRepository.persistEntity(agentOrganisationSyndicale);

			logger.info("Updated AgentA48OrganisationSyndicale id {}.", agentOrganisationSyndicale.getIdA48AgentOrganisationSyndicale());

			return srm;
		}
	}
}
