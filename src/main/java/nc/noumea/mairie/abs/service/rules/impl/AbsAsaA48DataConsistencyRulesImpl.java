package nc.noumea.mairie.abs.service.rules.impl;

import java.util.List;

import nc.noumea.mairie.abs.domain.AgentAsaA48Count;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeAsa;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

import org.springframework.stereotype.Service;

@Service("AbsAsaA48DataConsistencyRulesImpl")
public class AbsAsaA48DataConsistencyRulesImpl extends AbsAsaDataConsistencyRulesImpl {

	@Override
	public void processDataConsistencyDemande(ReturnMessageDto srm, Integer idAgent, Demande demande,
			boolean isProvenanceSIRH) {

		super.processDataConsistencyDemande(srm, idAgent, demande, isProvenanceSIRH);
		checkDroitCompteurAsaA48(srm, demande);
	}

	public ReturnMessageDto checkDroitCompteurAsaA48(ReturnMessageDto srm, Demande demande) {

		AgentAsaA48Count soldeAsaA48 = counterRepository.getAgentCounterByDate(AgentAsaA48Count.class,
				demande.getIdAgent(), demande.getDateDebut());

		if (null == soldeAsaA48) {
			logger.warn(String.format(AUCUN_DROITS_ASA_MSG, demande.getIdAgent()));
			srm.getErrors().add(String.format(AUCUN_DROITS_ASA_MSG, demande.getIdAgent()));
			return srm;
		}

		double sommeDemandeEnCours = getSommeDureeDemandeAsaEnCours(demande.getIdDemande(), demande.getIdAgent());

		// on signale par un message d info que le compteur est epuise, mais on
		// ne bloque pas la demande
		if (0 > soldeAsaA48.getTotalJours() - sommeDemandeEnCours - ((DemandeAsa) demande).getDuree()) {
			logger.warn(String.format(DEPASSEMENT_DROITS_ASA_MSG));
			srm.getInfos().add(DEPASSEMENT_DROITS_ASA_MSG);
		}

		return srm;
	}

	private double getSommeDureeDemandeAsaEnCours(Integer idDemande, Integer idAgent) {

		List<DemandeAsa> listAsa = asaRepository.getListDemandeAsaEnCours(idAgent, idDemande,
				RefTypeAbsenceEnum.ASA_A48.getValue());

		double somme = 0.0;

		if (null != listAsa) {
			for (DemandeAsa asa : listAsa) {
				somme += helperService.calculNombreJoursArrondiDemiJournee(asa.getDateDebut(), asa.getDateFin());
			}
		}
		return somme;
	}

	@Override
	public boolean checkDepassementCompteurAgent(DemandeDto demandeDto) {

		// on verifie d abord l etat de la demande
		// si ANNULE PRIS VALIDE ou REFUSE, on n affiche pas d alerte de
		// depassement de compteur
		if (!super.checkEtatDemandePourDepassementCompteurAgent(demandeDto))
			return false;

		AgentAsaA48Count soldeAsaA48 = counterRepository.getAgentCounterByDate(AgentAsaA48Count.class, demandeDto
				.getAgentWithServiceDto().getIdAgent(), demandeDto.getDateDebut());

		if (null == soldeAsaA48) {
			return true;
		}

		double sommeDemandeEnCours = getSommeDureeDemandeAsaEnCours(demandeDto.getIdDemande(), demandeDto
				.getAgentWithServiceDto().getIdAgent());

		// on signale par un message d info que le compteur est epuise, mais on
		// ne bloque pas la demande
		if (0 > soldeAsaA48.getTotalJours() - sommeDemandeEnCours - demandeDto.getDuree()) {
			return true;
		}

		return false;
	}
}
