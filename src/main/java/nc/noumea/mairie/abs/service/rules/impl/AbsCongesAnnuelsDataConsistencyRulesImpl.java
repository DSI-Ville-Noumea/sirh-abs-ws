package nc.noumea.mairie.abs.service.rules.impl;

import java.util.List;

import nc.noumea.mairie.abs.domain.RefTypeSaisi;
import nc.noumea.mairie.abs.domain.RefTypeSaisiCongeAnnuel;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

import org.springframework.stereotype.Service;

@Service("AbsCongesAnnuelsDataConsistencyRulesImpl")
public class AbsCongesAnnuelsDataConsistencyRulesImpl extends AbstractAbsenceDataConsistencyRules {

	@Override
	public ReturnMessageDto checkSaisiNewTypeAbsence(RefTypeSaisi typeSaisi,
			List<RefTypeSaisiCongeAnnuel> listeTypeSaisiCongeAnnuel, ReturnMessageDto srm) {
		for (RefTypeSaisiCongeAnnuel type : listeTypeSaisiCongeAnnuel) {
			if (!type.isCalendarDateDebut())
				srm.getErrors().add(String.format("La date de début est obligatoire."));

			if (type.isCalendarDateFin() && type.isCalendarDateReprise())
				srm.getErrors().add(String.format("Si date de reprise est à oui, alors date de fin doit être à non."));

			if (!type.isCalendarDateFin() && !type.isCalendarDateReprise())
				srm.getErrors().add(String.format("Si date de reprise est à non, alors date de fin doit être à oui."));

			if (type.isDecompteSamedi() && type.isConsecutif())
				srm.getErrors()
						.add(String.format("Si consécutif est à oui, alors décompte du samedi doit être à non."));

			if (!type.isDecompteSamedi() && !type.isConsecutif())
				srm.getErrors()
						.add(String.format("Si consécutif est à non, alors décompte du samedi doit être à oui."));
		}

		return srm;
	}
}
