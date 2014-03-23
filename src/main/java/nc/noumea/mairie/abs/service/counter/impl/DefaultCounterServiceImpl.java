package nc.noumea.mairie.abs.service.counter.impl;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.dto.CompteurAsaDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

import org.springframework.stereotype.Service;

@Service("DefaultCounterServiceImpl")
public class DefaultCounterServiceImpl extends AbstractCounterService {

	@Override
	public int addToAgentForPTG(Integer idAgent, Date dateMonday, Integer minutes) {
		return 0;
	}

	@Override
	public ReturnMessageDto majCompteurToAgent(ReturnMessageDto srm, Demande demande, Integer minutes) {
		srm.getErrors().add(String.format(ERROR_TECHNIQUE));
		return srm;
	}

	@Override
	public int calculMinutesCompteur(DemandeEtatChangeDto demandeEtatChangeDto, Demande demande) {
		return 0;
	}

	@Override
	public List<CompteurAsaDto> getListeCompteur() {
		return null;
	}

}
