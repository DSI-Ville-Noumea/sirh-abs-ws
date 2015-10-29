package nc.noumea.mairie.abs.service.rules.impl;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentAsaA55Count;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeAsa;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.vo.CheckCompteurAgentVo;

import org.springframework.stereotype.Service;

@Service("AbsAsaA55DataConsistencyRulesImpl")
public class AbsAsaA55DataConsistencyRulesImpl extends AbsAsaDataConsistencyRulesImpl {

	@Override
	public void processDataConsistencyDemande(ReturnMessageDto srm, Integer idAgent, Demande demande, boolean isProvenanceSIRH) {
		super.processDataConsistencyDemande(srm, idAgent, demande, isProvenanceSIRH);
		checkDroitCompteurAsaA55(srm, demande);
	}

	public ReturnMessageDto checkDroitCompteurAsaA55(ReturnMessageDto srm, Demande demande) {

		AgentAsaA55Count soldeAsaA55 = counterRepository.getAgentCounterByDate(AgentAsaA55Count.class, demande.getIdAgent(), demande.getDateDebut());

		if (null == soldeAsaA55) {
			logger.warn(String.format(AUCUN_DROITS_ASA_MSG, demande.getIdAgent()));
			srm.getErrors().add(String.format(AUCUN_DROITS_ASA_MSG, demande.getIdAgent()));
			return srm;
		}

		int sommeDemandeEnCours = getSommeDureeDemandeAsaEnCours(demande.getIdDemande(), demande.getIdAgent(), soldeAsaA55.getDateDebut(), soldeAsaA55.getDateFin());

		// on signale par un message d info que le compteur est epuise, mais on
		// ne bloque pas la demande
		if (0 > soldeAsaA55.getTotalMinutes() - sommeDemandeEnCours - ((DemandeAsa) demande).getDuree()) {
			logger.warn(String.format(DEPASSEMENT_DROITS_ASA_MSG));
			srm.getErrors().add(DEPASSEMENT_DROITS_ASA_MSG);
		}

		return srm;
	}

	private int getSommeDureeDemandeAsaEnCours(Integer idDemande, Integer idAgent, Date dateDebut, Date dateFin) {
		List<DemandeAsa> listAsa = asaRepository.getListDemandeAsaEnCours(idAgent, idDemande, dateDebut, dateFin, RefTypeAbsenceEnum.ASA_A55.getValue());

		int somme = 0;

		if (null != listAsa) {
			for (DemandeAsa asa : listAsa) {
				somme += helperService.calculNombreMinutes(asa.getDateDebut(), asa.getDateFin());
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

		AgentAsaA55Count soldeAsaA55 = counterRepository.getAgentCounterByDate(AgentAsaA55Count.class, demandeDto.getAgentWithServiceDto().getIdAgent(), demandeDto.getDateDebut());

		if (null == soldeAsaA55) {
			return true;
		}

		int sommeDemandeEnCours = getSommeDureeDemandeAsaEnCours(demandeDto.getIdDemande(), demandeDto.getAgentWithServiceDto().getIdAgent(), soldeAsaA55.getDateDebut(), soldeAsaA55.getDateFin());

		// on signale par un message d info que le compteur est epuise, mais on
		// ne bloque pas la demande
		if (0 > soldeAsaA55.getTotalMinutes() - sommeDemandeEnCours - demandeDto.getDuree()) {
			return true;
		}

		return false;
	}
}
