package nc.noumea.mairie.abs.service.impl;

import java.util.ArrayList;
import java.util.List;

import nc.noumea.mairie.abs.domain.RefEtat;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.domain.RefTypeSaisi;
import nc.noumea.mairie.abs.dto.RefEtatDto;
import nc.noumea.mairie.abs.dto.RefTypeAbsenceDto;
import nc.noumea.mairie.abs.dto.RefTypeSaisiDto;
import nc.noumea.mairie.abs.repository.IDemandeRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.abs.service.IFiltresService;
import nc.noumea.mairie.domain.Spcarr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FiltresService implements IFiltresService {


	@Autowired
	private IDemandeRepository demandeRepository;

	@Autowired
	private ISirhRepository sirhRepository;

	@Autowired
	private HelperService helperService;

	public static final String ONGLET_NON_PRISES = "NON_PRISES";
	public static final String ONGLET_EN_COURS = "EN_COURS";
	public static final String ONGLET_TOUTES = "TOUTES";

	@Override
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
	public List<RefTypeAbsenceDto> getRefTypesAbsence(Integer idAgentConcerne) {
		List<RefTypeAbsenceDto> res = new ArrayList<RefTypeAbsenceDto>();
		List<RefTypeAbsence> refTypeAbs = demandeRepository.findAllRefTypeAbsences();

		Spcarr carr = null;
		if (null != idAgentConcerne) {
			carr = sirhRepository.getAgentCurrentCarriere(idAgentConcerne, helperService.getCurrentDate());
		}

		for (RefTypeAbsence type : refTypeAbs) {
			if (null == carr
					|| carr.getCdcate() == 4
					|| carr.getCdcate() == 7
					|| !RefTypeAbsenceEnum.getRefTypeAbsenceEnum(type.getIdRefTypeAbsence()).equals(
							RefTypeAbsenceEnum.REPOS_COMP)) {

				RefTypeAbsenceDto dto = new RefTypeAbsenceDto(type);
				res.add(dto);
			}
		}
		return res;
	}

	@Override
	public List<RefEtat> getListeEtatsByOnglet(String ongletDemande, Integer idRefEtat) {

		List<RefEtat> etats = new ArrayList<RefEtat>();

		if (null == ongletDemande) {
			etats = demandeRepository.findAllRefEtats();
			return etats;
		}

		switch (ongletDemande) {
			case ONGLET_NON_PRISES:
				if (idRefEtat != null) {
					etats.add(demandeRepository.getEntity(RefEtat.class, idRefEtat));
				} else {
					etats = demandeRepository.findRefEtatNonPris();
				}
				break;
			case ONGLET_EN_COURS:
				if (idRefEtat != null) {
					etats.add(demandeRepository.getEntity(RefEtat.class, idRefEtat));
				} else {
					etats = demandeRepository.findRefEtatEnCours();
				}
				break;
			case ONGLET_TOUTES:
				if (idRefEtat != null) {
					etats.add(demandeRepository.getEntity(RefEtat.class, idRefEtat));
				} else {
					etats = demandeRepository.findAllRefEtats();
				}
				break;
		}

		return etats;
	}
	
	@Override
	public List<RefTypeSaisiDto> getRefTypeSaisi(Integer idRefTypeAbsence) {
		
		List<RefTypeSaisiDto> resultDto = new ArrayList<RefTypeSaisiDto>();
		List<RefTypeSaisi> result = demandeRepository.findRefTypeSaisi(idRefTypeAbsence);
		if(null != result) {
			for(RefTypeSaisi typeSaisi : result) {
				RefTypeSaisiDto dto = new RefTypeSaisiDto(typeSaisi);
				resultDto.add(dto);
			}
		}
		
		return resultDto;
	}

}
