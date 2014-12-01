package nc.noumea.mairie.abs.service.rules.impl;

import nc.noumea.mairie.abs.domain.RefTypeSaisi;
import nc.noumea.mairie.abs.domain.RefTypeSaisiCongeAnnuel;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

import org.springframework.stereotype.Service;

@Service("AbsCongesAnnuelsDataConsistencyRulesImpl")
public class AbsCongesAnnuelsDataConsistencyRulesImpl extends AbstractAbsenceDataConsistencyRules {

	@Override
	public ReturnMessageDto checkSaisiNewTypeAbsence(RefTypeSaisi typeSaisi,
			RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel, ReturnMessageDto srm) {
		if (typeSaisiCongeAnnuel == null || typeSaisiCongeAnnuel.getIdRefTypeSaisiCongeAnnuel() == null) {
			logger.warn(String.format(SAISIE_TYPE_ABSENCE_NON_AUTORISEE));
			srm.getErrors().add(String.format(SAISIE_TYPE_ABSENCE_NON_AUTORISEE));
			return srm;
		}
		if (!typeSaisiCongeAnnuel.isCalendarDateDebut())
			srm.getErrors().add(String.format("La date de début est obligatoire."));

		if (typeSaisiCongeAnnuel.isCalendarDateFin() && typeSaisiCongeAnnuel.isCalendarDateReprise())
			srm.getErrors().add(String.format("Si date de reprise est à oui, alors date de fin doit être à non."));

		if (!typeSaisiCongeAnnuel.isCalendarDateFin() && !typeSaisiCongeAnnuel.isCalendarDateReprise())
			srm.getErrors().add(String.format("Si date de reprise est à non, alors date de fin doit être à oui."));

		if (typeSaisiCongeAnnuel.isDecompteSamedi() && typeSaisiCongeAnnuel.isConsecutif())
			srm.getErrors().add(String.format("Si consécutif est à oui, alors décompte du samedi doit être à non."));

		if (!typeSaisiCongeAnnuel.isDecompteSamedi() && !typeSaisiCongeAnnuel.isConsecutif())
			srm.getErrors().add(String.format("Si consécutif est à non, alors décompte du samedi doit être à oui."));

		return srm;
	}
}
