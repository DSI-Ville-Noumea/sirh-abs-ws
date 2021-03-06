package nc.noumea.mairie.abs.service.impl;

import java.util.ArrayList;
import java.util.List;

import nc.noumea.mairie.abs.domain.RefGroupeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.domain.RefTypeSaisi;
import nc.noumea.mairie.abs.domain.RefTypeSaisiCongeAnnuel;
import nc.noumea.mairie.abs.domain.RefUnitePeriodeQuota;
import nc.noumea.mairie.abs.dto.RefTypeAbsenceDto;
import nc.noumea.mairie.abs.dto.RefTypeSaisiCongeAnnuelDto;
import nc.noumea.mairie.abs.dto.RefTypeSaisiDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.ITypeAbsenceRepository;
import nc.noumea.mairie.abs.service.IAbsenceDataConsistencyRules;
import nc.noumea.mairie.abs.service.ITypeAbsenceService;
import nc.noumea.mairie.abs.service.rules.impl.DataConsistencyRulesFactory;
import nc.noumea.mairie.ws.ISirhWSConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TypeAbsenceServiceImpl implements ITypeAbsenceService {

	private Logger logger = LoggerFactory.getLogger(TypeAbsenceServiceImpl.class);

	public static final String UNITE_PERIODE_QUOTA_INEXISTANT = "L'unité pour la période quota n'existe pas";
	public static final String TYPE_ABSENCE_INEXISTANT = "Le type d'absence n'existe pas";
	public static final String TYPE_GROUPE_INEXISTANT = "Le type de groupe d'absence n'existe pas";

	public static final String TYPE_ABSENCE_CREE = "Le type d'absence est bien créé.";
	public static final String TYPE_ABSENCE_MODIFIE = "Le type d'absence est bien modifié.";
	public static final String TYPE_ABSENCE_INACTIVE = "Le type d'absence est inactivé.";
	public static final String AGENT_NON_HABILITE = "L'agent n'est pas habilité pour cette opération.";

	@Autowired
	private ITypeAbsenceRepository typeAbsenceRepository;

	@Autowired
	private ISirhWSConsumer sirhWSConsumer;

	@Autowired
	private DataConsistencyRulesFactory dataConsistencyRulesFactory;

	@Override
	@Transactional(readOnly = true)
	public List<RefTypeAbsenceDto> getListeTypAbsence(Integer idRefGroupeAbsence) {

		List<RefTypeAbsence> listTypeAbsence = typeAbsenceRepository.getListeTypAbsence(idRefGroupeAbsence);

		List<RefTypeAbsenceDto> res = new ArrayList<RefTypeAbsenceDto>();
		if (listTypeAbsence != null) {
			for (RefTypeAbsence typeAbsence : listTypeAbsence) {
				RefTypeAbsenceDto dto = new RefTypeAbsenceDto(typeAbsence, typeAbsence.getTypeSaisi(), null);
				res.add(dto);
			}
		}
		return res;
	}

	@Override
	@Transactional(readOnly = true)
	public List<RefTypeAbsenceDto> getListeAllTypeAbsence() {

		List<RefTypeAbsence> listTypeAbsence = typeAbsenceRepository.getListeAllTypeAbsence();

		List<RefTypeAbsenceDto> res = new ArrayList<RefTypeAbsenceDto>();
		if (listTypeAbsence != null) {
			for (RefTypeAbsence typeAbsence : listTypeAbsence) {
				RefTypeAbsenceDto dto = new RefTypeAbsenceDto(typeAbsence, typeAbsence.getTypeSaisi(), null);
				res.add(dto);
			}
		}
		return res;
	}

	@Override
	@Transactional(value = "absTransactionManager")
	public ReturnMessageDto setTypAbsence(Integer idAgent, RefTypeAbsenceDto typeAbsenceDto) {

		ReturnMessageDto result = new ReturnMessageDto();

		// verification des droits SIRH
		ReturnMessageDto isUtilisateurSIRH = sirhWSConsumer.isUtilisateurSIRH(idAgent);
		if (!isUtilisateurSIRH.getErrors().isEmpty()) {
			logger.warn(AGENT_NON_HABILITE);
			result.getErrors().add(String.format(AGENT_NON_HABILITE));
			return result;
		}

		RefTypeAbsence typeAbsence = null;

		if (null != typeAbsenceDto.getIdRefTypeAbsence()) {
			typeAbsence = typeAbsenceRepository.getEntity(RefTypeAbsence.class, typeAbsenceDto.getIdRefTypeAbsence());
			if (null == typeAbsence) {
				logger.debug(TYPE_ABSENCE_INEXISTANT);
				result.getErrors().add(TYPE_ABSENCE_INEXISTANT);
				return result;
			}
		}

		if (null == typeAbsence) {
			typeAbsence = new RefTypeAbsence();
			typeAbsence.setActif(true);
		}

		if (null != typeAbsenceDto.getGroupeAbsence()
				&& !"".equals(typeAbsenceDto.getGroupeAbsence().getIdRefGroupeAbsence())) {
			RefGroupeAbsence groupe = typeAbsenceRepository.getEntity(RefGroupeAbsence.class, typeAbsenceDto
					.getGroupeAbsence().getIdRefGroupeAbsence());

			if (null == groupe) {
				logger.debug(TYPE_GROUPE_INEXISTANT);
				result.getErrors().add(TYPE_GROUPE_INEXISTANT);
				return result;
			}
			typeAbsence.setGroupe(groupe);
		} else {
			logger.debug(TYPE_GROUPE_INEXISTANT);
			result.getErrors().add(TYPE_GROUPE_INEXISTANT);
			return result;
		}
		
		// #37350 + #45162 : on check que le libellé n'est pas null
		if (typeAbsence.getLabel() == null || typeAbsence.getLabel().trim().equals("")) {
			if ((typeAbsenceDto.getLibelle() == null || typeAbsenceDto.getLibelle().trim().equals(""))) {
				logger.debug("Le libellé est obligatoire.");
				result.getErrors().add("Le libellé est obligatoire.");
				return result;
			} else {
				typeAbsence.setLabel(typeAbsenceDto.getLibelle());
			}
		}

		if (null != typeAbsenceDto.getTypeSaisiDto()) {

			RefTypeSaisiDto typeSaisiDto = typeAbsenceDto.getTypeSaisiDto();

			RefTypeSaisi typeSaisi = typeAbsence.getTypeSaisi();

			if (null == typeSaisi) {
				typeSaisi = new RefTypeSaisi();
			}

			typeSaisi.setAlerte(typeSaisiDto.isAlerte());
			typeSaisi.setCalendarDateDebut(typeSaisiDto.isCalendarDateDebut());
			typeSaisi.setCalendarDateFin(typeSaisiDto.isCalendarDateFin());
			typeSaisi.setCalendarHeureDebut(typeSaisiDto.isCalendarHeureDebut());
			typeSaisi.setCalendarHeureFin(typeSaisiDto.isCalendarHeureFin());
			typeSaisi.setChkDateDebut(typeSaisiDto.isChkDateDebut());
			typeSaisi.setChkDateFin(typeSaisiDto.isChkDateFin());
			typeSaisi.setContractuel(typeSaisiDto.isContractuel());
			typeSaisi.setConventionCollective(typeSaisiDto.isConventionCollective());
			typeSaisi.setDescription(typeSaisiDto.getDescription());
			typeSaisi.setFonctionnaire(typeSaisiDto.isFonctionnaire());
			typeSaisi.setInfosComplementaires(typeSaisiDto.getInfosComplementaires());
			typeSaisi.setMessageAlerte(typeSaisiDto.getMessageAlerte());
			typeSaisi.setPieceJointe(typeSaisiDto.isPieceJointe());
			typeSaisi.setInfosPieceJointe(typeSaisiDto.getInfosPieceJointe());
			typeSaisi.setQuotaMax(typeSaisiDto.getQuotaMax());
			typeSaisi.setSaisieKiosque(typeSaisiDto.isSaisieKiosque());
			typeSaisi.setUniteDecompte(typeSaisiDto.getUniteDecompte());
			typeSaisi.setMotif(typeSaisiDto.isMotif());
			typeSaisi.setType(typeAbsence);

			if (null != typeSaisiDto.getUnitePeriodeQuotaDto()
					&& null != typeSaisiDto.getUnitePeriodeQuotaDto().getIdRefUnitePeriodeQuota()) {

				RefUnitePeriodeQuota refUnitePeriodeQuota = typeAbsenceRepository.getEntity(RefUnitePeriodeQuota.class,
						typeSaisiDto.getUnitePeriodeQuotaDto().getIdRefUnitePeriodeQuota());

				if (null == refUnitePeriodeQuota) {
					logger.debug(UNITE_PERIODE_QUOTA_INEXISTANT);
					result.getErrors().add(UNITE_PERIODE_QUOTA_INEXISTANT);
					return result;
				}
				typeSaisi.setRefUnitePeriodeQuota(refUnitePeriodeQuota);
			} else {
				typeSaisi.setRefUnitePeriodeQuota(null);
			}
			
			// MALADIES
			typeSaisi.setPrescripteur(typeSaisiDto.isPrescripteur());
			typeSaisi.setDateDeclaration(typeSaisiDto.isDateDeclaration());
			typeSaisi.setProlongation(typeSaisiDto.isProlongation());
			typeSaisi.setNomEnfant(typeSaisiDto.isNomEnfant());
			typeSaisi.setNombreITT(typeSaisiDto.isNombreITT());
			typeSaisi.setSiegeLesion(typeSaisiDto.isSiegeLesion());
			typeSaisi.setAtReference(typeSaisiDto.isAtReference());
			typeSaisi.setMaladiePro(typeSaisiDto.isMaladiePro());
			
			typeAbsence.setTypeSaisi(typeSaisi);
		}
		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		if (null != typeAbsenceDto.getTypeSaisiCongeAnnuelDto()
				&& null != typeAbsenceDto.getTypeSaisiCongeAnnuelDto().getIdRefTypeSaisiCongeAnnuel()) {
			typeSaisiCongeAnnuel = typeAbsenceRepository.getEntity(RefTypeSaisiCongeAnnuel.class, typeAbsenceDto
					.getTypeSaisiCongeAnnuelDto().getIdRefTypeSaisiCongeAnnuel());
			RefTypeSaisiCongeAnnuelDto typeSaisiCongeAnnuelDto = typeAbsenceDto.getTypeSaisiCongeAnnuelDto();
			typeSaisiCongeAnnuel.setCalendarDateDebut(typeSaisiCongeAnnuelDto.isCalendarDateDebut());
			typeSaisiCongeAnnuel.setCalendarDateFin(typeSaisiCongeAnnuelDto.isCalendarDateFin());
			typeSaisiCongeAnnuel.setCalendarDateReprise(typeSaisiCongeAnnuelDto.isCalendarDateReprise());
			typeSaisiCongeAnnuel.setChkDateDebut(typeSaisiCongeAnnuelDto.isChkDateDebut());
			typeSaisiCongeAnnuel.setChkDateFin(typeSaisiCongeAnnuelDto.isChkDateFin());
			typeSaisiCongeAnnuel.setDescription(typeSaisiCongeAnnuelDto.getDescription());
			typeSaisiCongeAnnuel.setCodeBaseHoraireAbsence(typeSaisiCongeAnnuelDto.getCodeBaseHoraireAbsence());
			typeSaisiCongeAnnuel.setQuotaMultiple(typeSaisiCongeAnnuelDto.getQuotaMultiple());
			typeSaisiCongeAnnuel.setDecompteSamedi(typeSaisiCongeAnnuelDto.isDecompteSamedi());
			typeSaisiCongeAnnuel.setConsecutif(typeSaisiCongeAnnuelDto.isConsecutif());

		}

		// on check la saisie
		IAbsenceDataConsistencyRules absenceDataConsistencyRulesImpl = dataConsistencyRulesFactory.getFactory(
				typeAbsence.getGroupe().getIdRefGroupeAbsence(), typeAbsence.getIdRefTypeAbsence());

		result = absenceDataConsistencyRulesImpl.checkSaisiNewTypeAbsence(typeAbsence.getTypeSaisi(),
				typeSaisiCongeAnnuel, result);

		if (!result.getErrors().isEmpty()) {
			return result;
		}

		typeAbsenceRepository.persistEntity(typeAbsence);

		addMessageConfirmation(typeAbsenceDto.getIdRefTypeAbsence(), result);

		return result;
	}

	@Override
	@Transactional(value = "absTransactionManager")
	public ReturnMessageDto inactiveTypeAbsence(Integer idAgent, Integer idRefTypeAbsence) {

		ReturnMessageDto result = new ReturnMessageDto();

		// verification des droits SIRH
		ReturnMessageDto isUtilisateurSIRH = sirhWSConsumer.isUtilisateurSIRH(idAgent);
		if (!isUtilisateurSIRH.getErrors().isEmpty()) {
			logger.warn(AGENT_NON_HABILITE);
			result.getErrors().add(String.format(AGENT_NON_HABILITE));
			return result;
		}

		RefTypeAbsence typeAbsence = typeAbsenceRepository.getEntity(RefTypeAbsence.class, idRefTypeAbsence);

		if (null == typeAbsence) {
			logger.debug(TYPE_ABSENCE_INEXISTANT);
			result.getErrors().add(TYPE_ABSENCE_INEXISTANT);
			return result;
		}
		// inactivation
		typeAbsence.setActif(false);
		typeAbsenceRepository.persistEntity(typeAbsence);

		result.getInfos().add(String.format(TYPE_ABSENCE_INACTIVE));

		return result;
	}

	private void addMessageConfirmation(Integer idRefTypeAbsence, ReturnMessageDto result) {

		if (null != idRefTypeAbsence) {
			logger.debug(TYPE_ABSENCE_MODIFIE);
			result.getInfos().add(TYPE_ABSENCE_MODIFIE);
		} else {
			logger.debug(TYPE_ABSENCE_CREE);
			result.getInfos().add(TYPE_ABSENCE_CREE);
		}
	}

	@Override
	public List<RefTypeAbsenceDto> getListeTypAbsenceCongeAnnuel() {
		RefTypeAbsence typeAbsence = typeAbsenceRepository.getEntity(RefTypeAbsence.class,
				RefTypeAbsenceEnum.CONGE_ANNUEL.getValue());

		List<RefTypeAbsenceDto> res = new ArrayList<RefTypeAbsenceDto>();
		for (RefTypeSaisiCongeAnnuel typeCong : typeAbsenceRepository.getListeTypeSaisiCongeAnnuel()) {
			RefTypeAbsenceDto dtoType = new RefTypeAbsenceDto(typeAbsence, null, null);
			RefTypeSaisiCongeAnnuelDto dto = new RefTypeSaisiCongeAnnuelDto(typeCong);
			dtoType.setTypeSaisiCongeAnnuelDto(dto);
			if (!res.contains(dto))
				res.add(dtoType);
		}

		return res;
	}

	@Override
	public RefTypeAbsenceDto getTypeAbsenceByBaseHoraire(Integer idBaseHoraireAbsence) {
		RefTypeSaisiCongeAnnuel typeSaisieConge = typeAbsenceRepository.getEntity(RefTypeSaisiCongeAnnuel.class,
				idBaseHoraireAbsence);
		RefTypeAbsence typeAbsence = typeAbsenceRepository.getEntity(RefTypeAbsence.class, typeSaisieConge.getType()
				.getIdRefTypeAbsence());
		typeAbsence.setTypeSaisiCongeAnnuel(typeSaisieConge);
		RefTypeAbsenceDto res = new RefTypeAbsenceDto(typeAbsence, typeAbsence.getTypeSaisi(),
				typeAbsence.getTypeSaisiCongeAnnuel());
		return res;
	}

	@Override
	public RefTypeAbsenceDto getTypeAbsenceById(Integer idTypeAbsence) {
		RefTypeAbsence typeAbsence = typeAbsenceRepository.getEntity(RefTypeAbsence.class, idTypeAbsence);
		RefTypeAbsenceDto res = new RefTypeAbsenceDto(typeAbsence, typeAbsence.getTypeSaisi(),
				typeAbsence.getTypeSaisiCongeAnnuel());
		return res;
	}

}
