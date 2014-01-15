package nc.noumea.mairie.abs.service.impl;

import java.util.ArrayList;
import java.util.List;

import nc.noumea.mairie.abs.domain.MotifCompteur;
import nc.noumea.mairie.abs.domain.MotifRefus;
import nc.noumea.mairie.abs.dto.MotifCompteurDto;
import nc.noumea.mairie.abs.dto.MotifRefusDto;
import nc.noumea.mairie.abs.repository.IMotifRepository;
import nc.noumea.mairie.abs.service.IMotifService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MotifService implements IMotifService {

	private Logger logger = LoggerFactory.getLogger(MotifService.class);

	@Autowired
	private IMotifRepository motifRepository;

	@Override
	public List<MotifRefusDto> getListeMotifRefus(Integer idRefType) {

		List<MotifRefusDto> res = new ArrayList<MotifRefusDto>();
		List<MotifRefus> motifRefus = motifRepository.getListeMotifRefus(idRefType);

		for (MotifRefus motif : motifRefus) {
			MotifRefusDto dto = new MotifRefusDto(motif);
			res.add(dto);
		}
		return res;
	}

	@Override
	public List<MotifCompteurDto> getListeMotifCompteur(Integer idRefType) {

		List<MotifCompteurDto> res = new ArrayList<MotifCompteurDto>();
		List<MotifCompteur> motifCompteur = motifRepository.getListeMotifCompteur(idRefType);

		for (MotifCompteur motif : motifCompteur) {
			MotifCompteurDto dto = new MotifCompteurDto(motif);
			res.add(dto);
		}
		return res;
	}

}
