package nc.noumea.mairie.abs.service.rules.impl;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentAsaA53Count;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeAsa;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.vo.CheckCompteurAgentVo;

import org.springframework.stereotype.Service;

@Service("AbsAsaA53DataConsistencyRulesImpl")
public class AbsAsaA53DataConsistencyRulesImpl extends AbsAsaDataConsistencyRulesImpl {

	@Override
	public void processDataConsistencyDemande(ReturnMessageDto srm, Integer idAgent, Demande demande, boolean isProvenanceSIRH) {

		super.processDataConsistencyDemande(srm, idAgent, demande, isProvenanceSIRH);
		super.checkOrganisationSyndicale(srm, (DemandeAsa) demande);
		if (!srm.getErrors().isEmpty())
			return;

		checkDroitCompteurAsaA53(srm, (DemandeAsa) demande);
	}

	public ReturnMessageDto checkDroitCompteurAsaA53(ReturnMessageDto srm, DemandeAsa demande) {

		AgentAsaA53Count soldeAsaA53 = counterRepository.getOSCounterByDate(AgentAsaA53Count.class, demande.getOrganisationSyndicale().getIdOrganisationSyndicale(), demande.getDateDebut());

		if (null == soldeAsaA53) {
			logger.warn(String.format(AUCUN_DROITS_ASA_MSG, demande.getIdAgent()));
			srm.getErrors().add(String.format(AUCUN_DROITS_ASA_MSG, demande.getIdAgent()));
			return srm;
		}

		double sommeDemandeEnCours = getSommeDureeDemandeAsaEnCours(demande.getIdDemande(), demande.getIdAgent(), soldeAsaA53.getDateDebut(), soldeAsaA53.getDateFin());

		// on signale par un message d info que le compteur est epuise, mais on
		// ne bloque pas la demande
		if (0 > soldeAsaA53.getTotalJours() - sommeDemandeEnCours - ((DemandeAsa) demande).getDuree()) {
			logger.warn(String.format(DEPASSEMENT_DROITS_ASA_MSG));
			srm.getErrors().add(DEPASSEMENT_DROITS_ASA_MSG);
		}

		return srm;
	}

	@Override
	public double getSommeDureeDemandeAsaEnCours(Integer idDemande, Integer idAgent, Date dateDebut, Date dateFin) {

		List<DemandeAsa> listAsa = asaRepository.getListDemandeAsaEnCours(idAgent, idDemande, dateDebut, dateFin, RefTypeAbsenceEnum.ASA_A53.getValue());

		double somme = 0.0;

		if (null != listAsa) {
			for (DemandeAsa asa : listAsa) {
				somme += helperService.calculNombreJoursArrondiDemiJournee(asa.getDateDebut(), asa.getDateFin());
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

		AgentAsaA53Count soldeAsaA53 = counterRepository.getOSCounterByDate(AgentAsaA53Count.class, demandeDto.getOrganisationSyndicale().getIdOrganisation(), demandeDto.getDateDebut());

		if (null == soldeAsaA53) {
			return true;
		}

		double sommeDemandeEnCours = getSommeDureeDemandeAsaEnCours(demandeDto.getIdDemande(), demandeDto.getAgentWithServiceDto().getIdAgent(), soldeAsaA53.getDateDebut(), soldeAsaA53.getDateFin());

		// on signale par un message d info que le compteur est epuise, mais on
		// ne bloque pas la demande
		if (0 > soldeAsaA53.getTotalJours() - sommeDemandeEnCours - demandeDto.getDuree()) {
			return true;
		}

		return false;
	}
}
