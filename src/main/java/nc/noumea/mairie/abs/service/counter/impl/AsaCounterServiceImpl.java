package nc.noumea.mairie.abs.service.counter.impl;

import java.util.Date;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.MotifCompteur;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("AsaCounterServiceImpl")
public class AsaCounterServiceImpl extends AbstractCounterService {
	
	@Override
	@Transactional(value = "absTransactionManager")
	public ReturnMessageDto majManuelleCompteurToAgent(Integer idAgent, CompteurDto compteurDto) {

		logger.info("Trying to update ASA A53 manually counters for Agent {} ...", compteurDto.getIdAgent());

		ReturnMessageDto result = new ReturnMessageDto();

		result = super.majManuelleCompteurToAgent(idAgent, compteurDto);
		if (!result.getErrors().isEmpty())
			return result;
		
		MotifCompteur motifCompteur = counterRepository
				.getEntity(MotifCompteur.class, compteurDto.getIdMotifCompteur());
		if (null == motifCompteur) {
			logger.warn(MOTIF_COMPTEUR_INEXISTANT);
			result.getErrors().add(String.format(MOTIF_COMPTEUR_INEXISTANT));
			return result;
		}
		
		result = majManuelleCompteurAsaToAgent(idAgent, compteurDto, result, motifCompteur);

		return result;
	}
	
	/**
	 * Methode implementee pour chaque type d ASA
	 */
	protected ReturnMessageDto majManuelleCompteurAsaToAgent(Integer idAgent,
			CompteurDto compteurDto, ReturnMessageDto result, MotifCompteur motifCompteur) {
		
		return result;
	}

	/**
	 * 
	 * @param demandeEtatChangeDto
	 * @param demande
	 * @param dateDebut
	 * @param dateFin
	 * @return int nombre minutes a incrementer/decrementer du compteur
	 */
	protected int calculMinutesAlimAutoCompteur(DemandeEtatChangeDto demandeEtatChangeDto, Demande demande,
			Date dateDebut, Date dateFin) {
		int minutes = 0;
		// si on approuve, le compteur decremente
		if (demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.VALIDEE.getCodeEtat())) {

			minutes = 0 - helperService.calculNombreMinutes(dateDebut, dateFin);
		}
		// si on passe de Approuve a Refuse, le compteur incremente
		if (demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.ANNULEE.getCodeEtat())
				&& (demande.getLatestEtatDemande().getEtat().equals(RefEtatEnum.VALIDEE) || demande
						.getLatestEtatDemande().getEtat().equals(RefEtatEnum.PRISE))) {
			minutes = helperService.calculNombreMinutes(dateDebut, dateFin);
		}

		return minutes;
	}
	
	/**
	 * 
	 * @param demandeEtatChangeDto
	 * @param demande
	 * @param dateDebut
	 * @param dateFin
	 * @return Double nombre jour a incrementer/decrementer du compteur
	 */
	protected Double calculJoursAlimAutoCompteur(DemandeEtatChangeDto demandeEtatChangeDto, Demande demande,
			Date dateDebut, Date dateFin) {
		Double jours = 0.0;
		// si on approuve, le compteur decremente
		if (demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.VALIDEE.getCodeEtat())) {
			jours = 0.0 - helperService.calculNombreJoursArrondiDemiJournee(dateDebut, dateFin);
		}
		// si on passe de Approuve a Refuse, le compteur incremente
		if (demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.ANNULEE.getCodeEtat())
				&& (demande.getLatestEtatDemande().getEtat().equals(RefEtatEnum.VALIDEE) || demande
						.getLatestEtatDemande().getEtat().equals(RefEtatEnum.PRISE))) {
			jours = helperService.calculNombreJoursArrondiDemiJournee(dateDebut, dateFin);
		}

		return jours;
	}
}
