package nc.noumea.mairie.abs.service.rules.impl;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentAsaA48Count;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeAsa;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

import org.springframework.stereotype.Service;

@Service("AbsAsaA48DataConsistencyRulesImpl")
public class AbsAsaA48DataConsistencyRulesImpl extends AbsAsaDataConsistencyRulesImpl {
	
	@Override
	public void processDataConsistencyDemande(ReturnMessageDto srm, Integer idAgent, Demande demande,
			Date dateLundi) {
		
		super.processDataConsistencyDemande(srm, idAgent, demande, dateLundi);
		checkDroitCompteurAsaA48(srm, demande);
	}
	
	protected ReturnMessageDto checkDroitCompteurAsaA48(ReturnMessageDto srm, Demande demande) {
		
		AgentAsaA48Count soldeAsaA48 = counterRepository.getAgentCounterByDate(AgentAsaA48Count.class, demande.getIdAgent(),
				demande.getDateDebut());
		
		if(null == soldeAsaA48) {
			logger.warn(String.format(AUCUN_DROITS_ASA_MSG));
			srm.getErrors().add(AUCUN_DROITS_ASA_MSG);
			return srm;
		}
		
		double sommeDemandeEnCours = getSommeDureeDemandeAsaEnCours(demande);
		
		// on signale par un message d info que le compteur est epuise, mais on ne bloque pas la demande
		if(0 > soldeAsaA48.getTotalJours() - sommeDemandeEnCours - helperService.calculNombreJoursArrondiDemiJournee(demande.getDateDebut(), demande.getDateFin())) {
			logger.warn(String.format(DEPASSEMENT_DROITS_ASA_MSG));
			srm.getInfos().add(DEPASSEMENT_DROITS_ASA_MSG);
		}
		
		return srm;
	}
	
	private double getSommeDureeDemandeAsaEnCours(Demande demande) {
		
		List<DemandeAsa> listAsa = asaRepository.getListDemandeAsaEnCours(
				demande.getIdAgent(), demande.getIdDemande());
		
		double somme = 0.0;
		
		if(null != listAsa) {
			for(DemandeAsa asa : listAsa) {
				somme += helperService.calculNombreJoursArrondiDemiJournee(asa.getDateDebut(), asa.getDateFin());
			}
		}
		return somme;
	}
}
