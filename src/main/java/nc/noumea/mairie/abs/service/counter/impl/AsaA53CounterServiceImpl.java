package nc.noumea.mairie.abs.service.counter.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentAsaA53Count;
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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("AsaA53CounterServiceImpl")
public class AsaA53CounterServiceImpl extends AsaCounterServiceImpl {

	/**
	 * appeler depuis Kiosque ou SIRH l historique ABS_AGENT_WEEK_ALIM_MANUELLE
	 * mise a jour
	 */
	@Override
	protected ReturnMessageDto majManuelleCompteurToAgent(Integer idAgent, CompteurDto compteurDto,
			ReturnMessageDto result, MotifCompteur motifCompteur) {

		logger.info("Trying to update manually ASA A53 counters for Agent {} ...", compteurDto.getIdAgent());

		try {
			Double nbJours = helperService.calculAlimManuelleCompteur(compteurDto);
			return majManuelleCompteurToAgent(idAgent, compteurDto, nbJours, RefTypeAbsenceEnum.ASA_A53.getValue(),
					result, motifCompteur);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("An error occured while trying to update recuperation counters :", e);
		}
	}

	/**
	 * Mise à jour manuelle du compteur de ASA A53
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
			Double nbJours, Integer idRefTypeAbsence, ReturnMessageDto srm, MotifCompteur motifCompteur)
			throws InstantiationException, IllegalAccessException {

		logger.info("updating counters for Agent [{}] with {} nbJours for dateDeb {} and dateFin {}...",
				compteurDto.getIdAgent(), nbJours, compteurDto.getDateDebut(), compteurDto.getDateFin());

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

		AgentAsaA53Count arc = (AgentAsaA53Count) counterRepository.getOSCounterByDate(AgentAsaA53Count.class,
				compteurDto.getOrganisationSyndicaleDto().getIdOrganisation(), compteurDto.getDateDebut());
		if (arc == null) {
			arc = new AgentAsaA53Count();
			arc.setOrganisationSyndicale(organisationSyndicale);
		}

		String textLog = "";
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		if (null != compteurDto.getDureeAAjouter()) {
			textLog = "Mise en place de " + nbJours + " jours pour la période du "
					+ sdf.format(compteurDto.getDateDebut()) + " au " + sdf.format(compteurDto.getDateFin()) + ".";
		}

		arc.setTotalJours(nbJours);
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

		List<AgentAsaA53Count> listeArc = counterRepository.getListCounter(AgentAsaA53Count.class);
		for (AgentAsaA53Count arc : listeArc) {
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

		logger.info("Trying to update ASA_A53 counters for Agent [{}] ...", demande.getIdAgent());

		Double jours = calculJoursAlimAutoCompteur(demandeEtatChangeDto, demande, demande.getDateDebut(),
				demande.getDateFin());
		if (0 != jours) {
			try {
				srm = majCompteurToAgent((DemandeAsa)demande, ((DemandeAsa) demande).getOrganisationSyndicale()
						.getIdOrganisationSyndicale(), jours, demande.getDateDebut(), srm);
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException("An error occured while trying to update ASA_A53 counters :", e);
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
	protected <T1, T2> ReturnMessageDto majCompteurToAgent(DemandeAsa demande, Integer idOrganisationSyndicale,
			Double jours, Date dateDebutDemande, ReturnMessageDto srm) throws InstantiationException,
			IllegalAccessException {

		if (sirhWSConsumer.getAgent(demande.getIdAgent()) == null) {
			logger.error("There is no Agent [{}]. Impossible to update its counters.", demande.getIdAgent());
			throw new AgentNotFoundException();
		}

		logger.info("updating counters for Agent [{}] with {} jours...", demande.getIdAgent(), jours);

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

		AgentAsaA53Count arc = (AgentAsaA53Count) counterRepository.getOSCounterByDate(AgentAsaA53Count.class,
				idOrganisationSyndicale, dateDebutDemande);

		if (arc == null) {
			logger.warn(COMPTEUR_INEXISTANT);
			srm.getErrors().add(String.format(COMPTEUR_INEXISTANT));
			return srm;
		}

		// on verifie que le solde est positif seulement si on debite le
		// compteur
		if (0 > jours && 0.0 > arc.getTotalJours() + jours) {
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
}
