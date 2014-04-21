package nc.noumea.mairie.abs.service.rules.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentAsaA54Count;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeAsa;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

import org.springframework.stereotype.Service;

@Service("AbsAsaA54DataConsistencyRulesImpl")
public class AbsAsaA54DataConsistencyRulesImpl extends AbsAsaDataConsistencyRulesImpl {

	@Override
	public void processDataConsistencyDemande(ReturnMessageDto srm, Integer idAgent, Demande demande, Date dateLundi) {

		super.processDataConsistencyDemande(srm, idAgent, demande, dateLundi);
		checkDroitCompteurAsaA54(srm, demande);
	}

	public ReturnMessageDto checkDroitCompteurAsaA54(ReturnMessageDto srm, Demande demande) {

		AgentAsaA54Count soldeAsaA54 = counterRepository.getAgentCounterByDate(AgentAsaA54Count.class,
				demande.getIdAgent(), demande.getDateDebut());

		if (null == soldeAsaA54) {
			logger.warn(String.format(AUCUN_DROITS_ASA_MSG, demande.getIdAgent()));
			srm.getErrors().add(String.format(AUCUN_DROITS_ASA_MSG, demande.getIdAgent()));
			return srm;
		}

		double sommeDemandeEnCours = getSommeDureeDemandeAsaEnCours(demande.getIdDemande(), demande.getIdAgent());

		// on signale par un message d info que le compteur est epuise, mais on
		// ne bloque pas la demande
		if (0 > soldeAsaA54.getTotalJours() - sommeDemandeEnCours - ((DemandeAsa) demande).getDuree()) {
			logger.warn(String.format(DEPASSEMENT_DROITS_ASA_MSG));
			srm.getInfos().add(DEPASSEMENT_DROITS_ASA_MSG);
		}

		return srm;
	}

	private double getSommeDureeDemandeAsaEnCours(Integer idDemande, Integer idAgent) {

		List<DemandeAsa> listAsa = asaRepository.getListDemandeAsaEnCours(idAgent, idDemande);

		double somme = 0.0;

		if (null != listAsa) {
			for (DemandeAsa asa : listAsa) {
				somme += helperService.calculNombreJoursArrondiDemiJournee(asa.getDateDebut(), asa.getDateFin());
			}
		}
		return somme;
	}

	@Override
	public ReturnMessageDto checkEtatsDemandeAnnulee(ReturnMessageDto srm, Demande demande,
			List<RefEtatEnum> listEtatsAcceptes) {

		List<RefEtatEnum> listEtats = new ArrayList<RefEtatEnum>();
		listEtats.addAll(listEtatsAcceptes);
		listEtats.addAll(Arrays.asList(RefEtatEnum.VALIDEE, RefEtatEnum.EN_ATTENTE, RefEtatEnum.PRISE));
		// dans le cas des ASA A54, on peut annuler en plus les demandes a l
		// etat VALIDEE et EN_ATTENTE
		return super.checkEtatsDemandeAnnulee(srm, demande, listEtats);
	}

	@Override
	public boolean checkDepassementCompteurAgent(DemandeDto demandeDto) {

		AgentAsaA54Count soldeAsaA54 = counterRepository.getAgentCounterByDate(AgentAsaA54Count.class, demandeDto
				.getAgentWithServiceDto().getIdAgent(), demandeDto.getDateDebut());

		if (null == soldeAsaA54) {
			return true;
		}

		double sommeDemandeEnCours = getSommeDureeDemandeAsaEnCours(demandeDto.getIdDemande(), demandeDto
				.getAgentWithServiceDto().getIdAgent());

		// on signale par un message d info que le compteur est epuise, mais on
		// ne bloque pas la demande
		if (0 > soldeAsaA54.getTotalJours() - sommeDemandeEnCours - demandeDto.getDuree()) {
			return true;
		}

		return false;
	}
}
