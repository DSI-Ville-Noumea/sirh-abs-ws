package nc.noumea.mairie.abs.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.RefEtat;
import nc.noumea.mairie.abs.domain.RefGroupeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeGroupeAbsenceEnum;
import nc.noumea.mairie.abs.domain.RefTypeSaisi;
import nc.noumea.mairie.abs.domain.RefTypeSaisiCongeAnnuel;
import nc.noumea.mairie.abs.domain.RefUnitePeriodeQuota;
import nc.noumea.mairie.abs.dto.RefEtatDto;
import nc.noumea.mairie.abs.dto.RefGroupeAbsenceDto;
import nc.noumea.mairie.abs.dto.RefTypeAbsenceDto;
import nc.noumea.mairie.abs.dto.RefTypeSaisiCongeAnnuelDto;
import nc.noumea.mairie.abs.dto.RefTypeSaisiDto;
import nc.noumea.mairie.abs.dto.UnitePeriodeQuotaDto;
import nc.noumea.mairie.abs.repository.IFiltreRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.abs.service.IAgentMatriculeConverterService;
import nc.noumea.mairie.abs.service.IFiltreService;
import nc.noumea.mairie.domain.Spcarr;
import nc.noumea.mairie.ws.ISirhWSConsumer;

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
	private ISirhWSConsumer sirhWSConsumer;

	@Autowired
	private IAgentMatriculeConverterService agentMatriculeService;

	public static final String ONGLET_NON_PRISES = "NON_PRISES";
	public static final String ONGLET_EN_COURS = "EN_COURS";
	public static final String ONGLET_TOUTES = "TOUTES";
	public static final String ONGLET_PLANNING = "PLANNING";

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
				boolean ajout = false;

				if (null != type.getTypeSaisi()) {
					if (helperService.isFonctionnaire(carr) && type.getTypeSaisi().isFonctionnaire()) {
						ajout = true;
					}
					if (helperService.isContractuel(carr) && type.getTypeSaisi().isContractuel()) {
						ajout = true;
					}
					if (helperService.isConventionCollective(carr) && type.getTypeSaisi().isConventionCollective()) {
						ajout = true;
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
	public List<RefEtat> getListeEtatsByOnglet(String ongletDemande, List<Integer> lisIdRefEtat) {

		List<RefEtat> etats = new ArrayList<RefEtat>();

		if (null == ongletDemande) {
			etats = filtreRepository.findAllRefEtats();
			return etats;
		}

		switch (ongletDemande) {
			case ONGLET_NON_PRISES:
				if (lisIdRefEtat != null && lisIdRefEtat.size() != 0) {
					for (Integer idEtat : lisIdRefEtat) {
						etats.add(filtreRepository.getEntity(RefEtat.class, idEtat));
					}
				} else {
					etats = filtreRepository.findRefEtatNonPris();
				}
				break;
			case ONGLET_EN_COURS:
				if (lisIdRefEtat != null && lisIdRefEtat.size() != 0) {
					for (Integer idEtat : lisIdRefEtat) {
						etats.add(filtreRepository.getEntity(RefEtat.class, idEtat));
					}
				} else {
					etats = filtreRepository.findRefEtatEnCours();
				}
				break;
			case ONGLET_TOUTES:
				// #12159
			case ONGLET_PLANNING:
				if (lisIdRefEtat != null && lisIdRefEtat.size() != 0) {
					for (Integer idEtat : lisIdRefEtat) {
						etats.add(filtreRepository.getEntity(RefEtat.class, idEtat));
					}
				} else {
					etats = filtreRepository.findRefEtatPlanning();
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

	@Override
	public List<RefTypeAbsenceDto> getRefTypesAbsenceSaisieKiosque(Integer idRefGroupeAbsence, Integer idAgent) {
		List<RefTypeAbsenceDto> res = new ArrayList<RefTypeAbsenceDto>();
		List<RefTypeAbsence> refTypeAbsTemp = new ArrayList<RefTypeAbsence>();
		if (idRefGroupeAbsence == null) {
			refTypeAbsTemp = filtreRepository.findAllRefTypeAbsences();
		} else {
			refTypeAbsTemp = filtreRepository.findAllRefTypeAbsencesWithGroup(idRefGroupeAbsence);
		}

		List<RefTypeAbsence> refTypeAbs = new ArrayList<RefTypeAbsence>();
		for (RefTypeAbsence typeAbs : refTypeAbsTemp) {
			if (typeAbs.getGroupe().getIdRefGroupeAbsence() == RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue()) {
				// si conge annuel on ajoute tout
				refTypeAbs.add(typeAbs);
			} else {
				// sinon on regarde que la saisie kiosque est autorisée
				if (typeAbs.getTypeSaisi() != null && typeAbs.getTypeSaisi().isSaisieKiosque()) {
					refTypeAbs.add(typeAbs);
				}
			}
		}

		RefTypeSaisiCongeAnnuel typeSaisieCongeAnnuel = null;
		for (RefTypeAbsence type : refTypeAbs) {
			if (idAgent != null
					&& type.getGroupe().getIdRefGroupeAbsence() == RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue()) {
				// on cherche le code base horaire absence de l'agent
				RefTypeSaisiCongeAnnuelDto dtoBase = sirhWSConsumer.getBaseHoraireAbsence(idAgent, new Date());
				// #15000 si pas d'affectation active alors on cherche la
				// precedente
				if (dtoBase.getIdRefTypeSaisiCongeAnnuel() == null) {
					dtoBase = sirhWSConsumer.getOldBaseHoraireAbsence(idAgent);
				}
				if (dtoBase != null && dtoBase.getIdRefTypeSaisiCongeAnnuel() != null) {
					RefTypeSaisiCongeAnnuel typeConge = filtreRepository.getEntity(RefTypeSaisiCongeAnnuel.class,
							dtoBase.getIdRefTypeSaisiCongeAnnuel());
					typeSaisieCongeAnnuel = typeConge;
				}
			}
			RefTypeAbsenceDto dto = new RefTypeAbsenceDto(type, type.getTypeSaisi(), typeSaisieCongeAnnuel);
			res.add(dto);
		}
		return res;
	}

	@Override
	public List<RefTypeAbsenceDto> getRefTypesAbsenceCompteurKiosque() {
		List<RefTypeAbsenceDto> result = new ArrayList<RefTypeAbsenceDto>();
		List<RefTypeAbsence> listeRecup = filtreRepository
				.findAllRefTypeAbsencesWithGroup(RefTypeGroupeAbsenceEnum.RECUP.getValue());
		for (RefTypeAbsence r : listeRecup) {
			RefTypeAbsenceDto dtoRecup = new RefTypeAbsenceDto(r);
			result.add(dtoRecup);
		}
		List<RefTypeAbsence> listeReposComp = filtreRepository
				.findAllRefTypeAbsencesWithGroup(RefTypeGroupeAbsenceEnum.REPOS_COMP.getValue());
		for (RefTypeAbsence r : listeReposComp) {
			RefTypeAbsenceDto dtoRecup = new RefTypeAbsenceDto(r);
			result.add(dtoRecup);
		}
		return result;
	}

	@Override
	public List<RefTypeAbsenceDto> getAllRefTypesAbsence() {
		List<RefTypeAbsenceDto> res = new ArrayList<RefTypeAbsenceDto>();
		List<RefTypeAbsence> refTypeAbs = filtreRepository.findAllRefTypeAbsences();

		for (RefTypeAbsence type : refTypeAbs) {
			RefTypeAbsenceDto dto = new RefTypeAbsenceDto(type, type.getTypeSaisi(), null);
			res.add(dto);

		}
		return res;
	}
}
