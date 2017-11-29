package nc.noumea.mairie.abs.service.rules.impl;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentAsaA52Count;
import nc.noumea.mairie.abs.domain.AgentOrganisationSyndicale;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeAsa;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.vo.CheckCompteurAgentVo;

import org.springframework.stereotype.Service;

@Service("AbsAsaA52DataConsistencyRulesImpl")
public class AbsAsaA52DataConsistencyRulesImpl extends AbsAsaDataConsistencyRulesImpl {

	@Override
	public void processDataConsistencyDemande(ReturnMessageDto srm, Integer idAgent, Demande demande,
			boolean isProvenanceSIRH) {

		super.processDataConsistencyDemande(srm, idAgent, demande, isProvenanceSIRH);
		super.checkOrganisationSyndicale(srm, (DemandeAsa) demande);
		if (!srm.getErrors().isEmpty())
			return;

		checkDroitCompteurAsaA52(srm, (DemandeAsa) demande);
	}

	public ReturnMessageDto checkDroitCompteurAsaA52(ReturnMessageDto srm, DemandeAsa demande) {
		AgentAsaA52Count soldeAsaA52 = counterRepository.getOSCounterByDate(AgentAsaA52Count.class, demande
				.getOrganisationSyndicale().getIdOrganisationSyndicale(), demande.getDateDebut());

		if (null == soldeAsaA52) {
			logger.warn(String.format(AUCUN_DROITS_ASA_MSG, demande.getIdAgent()));
			srm.getErrors().add(String.format(AUCUN_DROITS_ASA_MSG, demande.getIdAgent()));
			return srm;
		}

		// on regarde si l'agent fait partie de l'organisation syndicale
		AgentOrganisationSyndicale agentOrag = organisationSyndicaleRepository.getAgentOrganisation(
				demande.getIdAgent(), demande.getOrganisationSyndicale().getIdOrganisationSyndicale());
		if (null == agentOrag || !agentOrag.isActif()) {
			logger.warn(String.format(AUCUN_DROITS_ASA_MSG, demande.getIdAgent()));
			srm.getErrors().add(String.format(AUCUN_DROITS_ASA_MSG, demande.getIdAgent()));
			return srm;
		}

		double sommeDemandeEnCours = getSommeDureeDemandeAsaEnCoursByOs(demande.getIdDemande(), demande.getIdAgent(),
				soldeAsaA52.getDateDebut(), soldeAsaA52.getDateFin(), soldeAsaA52.getOrganisationSyndicale().getIdOrganisationSyndicale());

		// on signale par un message d info que le compteur est epuise, mais on
		// ne bloque pas la demande
		if (0 > soldeAsaA52.getTotalMinutes() - sommeDemandeEnCours - ((DemandeAsa) demande).getDuree()) {
			logger.warn(String.format(DEPASSEMENT_DROITS_ASA_MSG));
			srm.getErrors().add(DEPASSEMENT_DROITS_ASA_MSG);
		}

		return srm;
	}

	@Override
	public double getSommeDureeDemandeAsaEnCoursByOs(Integer idDemande, Integer idAgent, Date dateDebut, Date dateFin, Integer idOs) {

		List<DemandeAsa> listAsa = asaRepository.getListDemandeAsaPourMoisByOS(idOs, idDemande, dateDebut, dateFin,
				RefTypeAbsenceEnum.ASA_A52.getValue());

		double somme = 0.0;

		if (null != listAsa) {
			for (DemandeAsa asa : listAsa) {
				somme += (double)helperService.calculNombreMinutes(asa.getDateDebut(), asa.getDateFin());
			}
		}
		return somme;
	}

	@Override
	public boolean checkDepassementCompteurAgent(DemandeDto demandeDto, CheckCompteurAgentVo checkCompteurAgentVo) {

		// on verifie d abord l etat de la demande
		// si ANNULE PRIS VALIDE ou REFUSE, on n affiche pas d alerte de
		// depassement de compteur
		if (!super.checkEtatDemandePourDepassementCompteurAgent(demandeDto))
			return false;

		AgentAsaA52Count soldeAsaA52 = counterRepository.getOSCounterByDate(AgentAsaA52Count.class, demandeDto
				.getOrganisationSyndicale().getIdOrganisation(), demandeDto.getDateDebut());

		if (null == soldeAsaA52) {
			return true;
		}

		double sommeDemandeEnCours = getSommeDureeDemandeAsaEnCours(demandeDto.getIdDemande(), demandeDto
				.getAgentWithServiceDto().getIdAgent(), soldeAsaA52.getDateDebut(), soldeAsaA52.getDateFin());

		// on signale par un message d info que le compteur est epuise, mais on
		// ne bloque pas la demande
		if (0 > soldeAsaA52.getTotalMinutes() - sommeDemandeEnCours - demandeDto.getDuree()) {
			return true;
		}

		return false;
	}
}
