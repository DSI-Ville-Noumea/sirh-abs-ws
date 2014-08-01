package nc.noumea.mairie.abs.service.impl;

import java.util.ArrayList;
import java.util.List;

import nc.noumea.mairie.abs.domain.RefEtat;
import nc.noumea.mairie.abs.domain.RefGroupeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.domain.RefTypeSaisi;
import nc.noumea.mairie.abs.domain.RefUnitePeriodeQuota;
import nc.noumea.mairie.abs.dto.RefEtatDto;
import nc.noumea.mairie.abs.dto.RefGroupeAbsenceDto;
import nc.noumea.mairie.abs.dto.RefTypeAbsenceDto;
import nc.noumea.mairie.abs.dto.RefTypeSaisiDto;
import nc.noumea.mairie.abs.dto.UnitePeriodeQuotaDto;
import nc.noumea.mairie.abs.repository.IFiltreRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.abs.service.IAgentMatriculeConverterService;
import nc.noumea.mairie.abs.service.IFiltreService;
import nc.noumea.mairie.domain.Spcarr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FiltreService implements IFiltreService {

	@Autowired
	private IFiltreRepository filtreRepository;

	@Autowired
	private ISirhRepository sirhRepository;

	@Autowired
	private HelperService helperService;

	@Autowired
	private IAgentMatriculeConverterService agentMatriculeService;

	public static final String ONGLET_NON_PRISES = "NON_PRISES";
	public static final String ONGLET_EN_COURS = "EN_COURS";
	public static final String ONGLET_TOUTES = "TOUTES";

	@Override
	@Transactional(readOnly = true)
	public List<RefEtatDto> getRefEtats(String ongletDemande) {

		List<RefEtatDto> res = new ArrayList<RefEtatDto>();
		List<RefEtat> refEtats = getListeEtatsByOnglet(ongletDemande, null);

		for (RefEtat etat : refEtats) {
			RefEtatDto dto = new RefEtatDto(etat);
			res.add(dto);
		}
		return res;
	}

	@Override
	@Transactional(readOnly = true)
	public List<RefTypeAbsenceDto> getRefTypesAbsence(Integer idAgentConcerne) {
		List<RefTypeAbsenceDto> res = new ArrayList<RefTypeAbsenceDto>();
		List<RefTypeAbsence> refTypeAbs = filtreRepository.findAllRefTypeAbsences();

		Spcarr carr = null;
		if (null != idAgentConcerne) {
			carr = sirhRepository
					.getAgentCurrentCarriere(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(idAgentConcerne),
							helperService.getCurrentDate());
		}

		for (RefTypeAbsence type : refTypeAbs) {
			if (null != carr) {
				boolean ajout = true;
				if (carr.getCdcate() == 4 || carr.getCdcate() == 7) {
					if (type.getIdRefTypeAbsence() == RefTypeAbsenceEnum.ASA_A55.getValue()) {
						// si contractuel ou convention
						ajout = false;
					}

				} else {
					// si fonctionanire
					if (type.getIdRefTypeAbsence() == RefTypeAbsenceEnum.REPOS_COMP.getValue()) {
						ajout = false;
					}
				}
				if (ajout) {
					RefTypeAbsenceDto dto = new RefTypeAbsenceDto(type);
					res.add(dto);
				}
			}
		}
		return res;
	}

	@Override
	public List<RefEtat> getListeEtatsByOnglet(String ongletDemande, Integer idRefEtat) {

		List<RefEtat> etats = new ArrayList<RefEtat>();

		if (null == ongletDemande) {
			etats = filtreRepository.findAllRefEtats();
			return etats;
		}

		switch (ongletDemande) {
			case ONGLET_NON_PRISES:
				if (idRefEtat != null) {
					etats.add(filtreRepository.getEntity(RefEtat.class, idRefEtat));
				} else {
					etats = filtreRepository.findRefEtatNonPris();
				}
				break;
			case ONGLET_EN_COURS:
				if (idRefEtat != null) {
					etats.add(filtreRepository.getEntity(RefEtat.class, idRefEtat));
				} else {
					etats = filtreRepository.findRefEtatEnCours();
				}
				break;
			case ONGLET_TOUTES:
				if (idRefEtat != null) {
					etats.add(filtreRepository.getEntity(RefEtat.class, idRefEtat));
				} else {
					etats = filtreRepository.findAllRefEtats();
				}
				break;
		}

		return etats;
	}

	@Override
	@Transactional(readOnly = true)
	public List<RefTypeSaisiDto> getRefTypeSaisi(Integer idRefTypeAbsence) {

		List<RefTypeSaisiDto> resultDto = new ArrayList<RefTypeSaisiDto>();
		if (null == idRefTypeAbsence) {
			List<RefTypeSaisi> result = filtreRepository.findAllRefTypeSaisi();
			if (null != result) {
				for (RefTypeSaisi typeSaisi : result) {
					RefTypeSaisiDto dto = new RefTypeSaisiDto(typeSaisi);
					resultDto.add(dto);
				}
			}
		} else {
			RefTypeSaisi result = filtreRepository.findRefTypeSaisi(idRefTypeAbsence);
			if (null != result) {
				RefTypeSaisiDto dto = new RefTypeSaisiDto(result);
				resultDto.add(dto);
			}
		}

		return resultDto;
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<RefGroupeAbsenceDto> getRefGroupeAbsence(Integer idRefGroupeAbsence) {

		List<RefGroupeAbsenceDto> resultDto = new ArrayList<RefGroupeAbsenceDto>();
		if (null == idRefGroupeAbsence) {
			List<RefGroupeAbsence> result = filtreRepository.findAllRefGroupeAbsence();
			if (null != result) {
				for (RefGroupeAbsence groupe : result) {
					RefGroupeAbsenceDto dto = new RefGroupeAbsenceDto(groupe);
					resultDto.add(dto);
				}
			}
		} else {
			RefGroupeAbsence result = filtreRepository.findRefGroupeAbsence(idRefGroupeAbsence);
			if (null != result) {
				RefGroupeAbsenceDto dto = new RefGroupeAbsenceDto(result);
				resultDto.add(dto);
			}
		}

		return resultDto;
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<UnitePeriodeQuotaDto> getUnitePeriodeQuota() {
		
		List<UnitePeriodeQuotaDto> resultDto = new ArrayList<UnitePeriodeQuotaDto>();
		
		List<RefUnitePeriodeQuota> result = filtreRepository.findAllRefUnitePeriodeQuota();
		if (null != result) {
			for (RefUnitePeriodeQuota upq : result) {
				UnitePeriodeQuotaDto dto = new UnitePeriodeQuotaDto(upq);
				resultDto.add(dto);
			}
		}
		return resultDto;
	}

}
