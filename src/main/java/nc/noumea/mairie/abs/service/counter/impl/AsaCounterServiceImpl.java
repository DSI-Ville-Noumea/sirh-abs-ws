package nc.noumea.mairie.abs.service.counter.impl;

import java.util.Date;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.repository.IOrganisationSyndicaleRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("AsaCounterServiceImpl")
public class AsaCounterServiceImpl extends AbstractCounterService {
	
	@Autowired
	protected IOrganisationSyndicaleRepository OSRepository;
	
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
