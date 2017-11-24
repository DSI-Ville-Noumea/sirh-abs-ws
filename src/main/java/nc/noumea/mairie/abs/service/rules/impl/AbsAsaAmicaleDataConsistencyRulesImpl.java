package nc.noumea.mairie.abs.service.rules.impl;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentAsaAmicaleCount;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeAsa;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.vo.CheckCompteurAgentVo;

import org.springframework.stereotype.Service;

@Service("AbsAsaAmicaleDataConsistencyRulesImpl")
public class AbsAsaAmicaleDataConsistencyRulesImpl extends AbsAsaDataConsistencyRulesImpl {

	@Override
	public void processDataConsistencyDemande(ReturnMessageDto srm, Integer idAgent, Demande demande, boolean isProvenanceSIRH) {
		super.processDataConsistencyDemande(srm, idAgent, demande, isProvenanceSIRH);
		checkDroitCompteurAsaAmicale(srm, demande);
	}

	public ReturnMessageDto checkDroitCompteurAsaAmicale(ReturnMessageDto srm, Demande demande) {

		AgentAsaAmicaleCount soldeAsaAmicale = counterRepository.getAgentCounterByDate(AgentAsaAmicaleCount.class, demande.getIdAgent(), demande.getDateDebut());

		if (null == soldeAsaAmicale || !soldeAsaAmicale.isActif()) {
			logger.warn(String.format(AUCUN_DROITS_ASA_MSG, demande.getIdAgent()));
			srm.getErrors().add(String.format(AUCUN_DROITS_ASA_MSG, demande.getIdAgent()));
			return srm;
		}

		double sommeDemandeEnCours = getSommeDureeDemandeAsaEnCours(demande.getIdDemande(), demande.getIdAgent(), soldeAsaAmicale.getDateDebut(), soldeAsaAmicale.getDateFin());

		// on signale par un message d info que le compteur est epuise, mais on
		// ne bloque pas la demande
		if (0 > soldeAsaAmicale.getTotalMinutes() - sommeDemandeEnCours - ((DemandeAsa) demande).getDuree()) {
			logger.warn(String.format(DEPASSEMENT_DROITS_ASA_MSG));
			srm.getErrors().add(DEPASSEMENT_DROITS_ASA_MSG);
		}

		return srm;
	}

	@Override
	public double getSommeDureeDemandeAsaEnCours(Integer idDemande, Integer idAgent, Date dateDebut, Date dateFin) {

		List<DemandeAsa> listAsa = asaRepository.getListDemandeAsaEnCours(idAgent, idDemande, dateDebut, dateFin, RefTypeAbsenceEnum.ASA_AMICALE.getValue());

		double somme = 0.0;

		if (null != listAsa) {
			for (DemandeAsa asa : listAsa) {
				somme += (double) helperService.calculNombreMinutes(asa.getDateDebut(), asa.getDateFin());
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

		AgentAsaAmicaleCount soldeAsaAmicale = counterRepository.getAgentCounterByDate(AgentAsaAmicaleCount.class, demandeDto.getAgentWithServiceDto().getIdAgent(), demandeDto.getDateDebut());

		if (null == soldeAsaAmicale) {
			return true;
		}

		double sommeDemandeEnCours = getSommeDureeDemandeAsaEnCours(demandeDto.getIdDemande(), demandeDto.getAgentWithServiceDto().getIdAgent(), soldeAsaAmicale.getDateDebut(),
				soldeAsaAmicale.getDateFin());

		// on signale par un message d info que le compteur est epuise, mais on
		// ne bloque pas la demande
		if (0 > soldeAsaAmicale.getTotalMinutes() - sommeDemandeEnCours - demandeDto.getDuree()) {
			return true;
		}

		return false;
	}
}
