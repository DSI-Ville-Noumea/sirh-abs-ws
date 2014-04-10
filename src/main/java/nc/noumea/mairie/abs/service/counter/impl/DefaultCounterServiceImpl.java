package nc.noumea.mairie.abs.service.counter.impl;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;

import org.springframework.stereotype.Service;

@Service("DefaultCounterServiceImpl")
public class DefaultCounterServiceImpl extends AbstractCounterService {

	

	

	@Override
	public int calculMinutesCompteur(DemandeEtatChangeDto demandeEtatChangeDto, Demande demande) {
		return 0;
	}
}
