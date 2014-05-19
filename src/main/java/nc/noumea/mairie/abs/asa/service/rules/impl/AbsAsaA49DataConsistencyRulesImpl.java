package nc.noumea.mairie.abs.asa.service.rules.impl;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.asa.domain.DemandeAsa;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

import org.springframework.stereotype.Service;

@Service("AbsAsaA49DataConsistencyRulesImpl")
public class AbsAsaA49DataConsistencyRulesImpl extends AbsAsaDataConsistencyRulesImpl {

	@Override
	public void processDataConsistencyDemande(ReturnMessageDto srm, Integer idAgent, Demande demande, Date dateLundi) {

		super.processDataConsistencyDemande(srm, idAgent, demande, dateLundi);
		checkDroitAsaA49(srm, demande);
	}

	public ReturnMessageDto checkDroitAsaA49(ReturnMessageDto srm, Demande demande) {

		int sommeDemandeEnCours = getSommeDureeDemandeAsaPourMoisDemande(demande.getIdDemande(), demande.getIdAgent(),
				demande.getDateDebut());

		// on signale par un message d info que l'agent a déjà pris son heure
		// autorisée, mais on ne bloque pas la demande
		if (60 < sommeDemandeEnCours + ((DemandeAsa) demande).getDuree()) {
			logger.warn(String.format(DEPASSEMENT_DROITS_ASA_MSG));
			srm.getInfos().add(DEPASSEMENT_DROITS_ASA_MSG);
		}

		return srm;
	}

	private int getSommeDureeDemandeAsaPourMoisDemande(Integer idDemande, Integer idAgent, Date dateDemande) {

		List<DemandeAsa> listAsa = asaRepository.getListDemandeAsaPourMois(idAgent, idDemande,
				helperService.getDateDebutMoisForOneDate(dateDemande),
				helperService.getDateFinMoisForOneDate(dateDemande), RefTypeAbsenceEnum.ASA_A49);

		int somme = 0;

		if (null != listAsa) {
			for (DemandeAsa asa : listAsa) {
				somme += helperService.calculNombreMinutes(asa.getDateDebut(), asa.getDateFin());
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

		int sommeDemandeEnCours = getSommeDureeDemandeAsaPourMoisDemande(demandeDto.getIdDemande(), demandeDto
				.getAgentWithServiceDto().getIdAgent(), demandeDto.getDateDebut());

		// on signale par un message d info que l'agent a déjà pris son heure
		// autorisée, mais on ne bloque pas la demande
		if (60 < sommeDemandeEnCours + demandeDto.getDuree()) {
			return true;
		}

		return false;
	}
}
