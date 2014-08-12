package nc.noumea.mairie.abs.service.impl;

import java.util.ArrayList;
import java.util.List;

import nc.noumea.mairie.abs.domain.RefGroupeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeSaisi;
import nc.noumea.mairie.abs.domain.RefUnitePeriodeQuota;
import nc.noumea.mairie.abs.dto.RefTypeAbsenceDto;
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
	public static final String TYPE_ABSENCE_SUPPRIME = "Le type d'absence est supprimé.";
	public static final String AGENT_NON_HABILITE = "L'agent n'est pas habilité pour cette opération.";

	@Autowired
	private ITypeAbsenceRepository typeAbsenceRepository;

	@Autowired
	private ISirhWSConsumer sirhWSConsumer;

	@Autowired
	private DataConsistencyRulesFactory dataConsistencyRulesFactory;
	
	@Override
	@Transactional(readOnly = true)
	public List<RefTypeAbsenceDto> getListeTypAbsence() {

		List<RefTypeAbsence> listTypeAbsence = typeAbsenceRepository.getListeTypAbsence();

		List<RefTypeAbsenceDto> res = new ArrayList<RefTypeAbsenceDto>();
		if (listTypeAbsence != null) {
			for (RefTypeAbsence typeAbsence : listTypeAbsence) {
				RefTypeAbsenceDto dto = new RefTypeAbsenceDto(typeAbsence, typeAbsence.getTypeSaisi());
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
		}

		if (null != typeAbsenceDto.getGroupeAbsence() && !"".equals(typeAbsenceDto.getGroupeAbsence().getIdRefGroupeAbsence())) {
			RefGroupeAbsence groupe = typeAbsenceRepository.getEntity(RefGroupeAbsence.class,
					typeAbsenceDto.getGroupeAbsence().getIdRefGroupeAbsence());

			if (null == groupe) {
				logger.debug(TYPE_GROUPE_INEXISTANT);
				result.getErrors().add(TYPE_GROUPE_INEXISTANT);
				return result;
			}
			typeAbsence.setGroupe(groupe);
		}else{
			logger.debug(TYPE_GROUPE_INEXISTANT);
			result.getErrors().add(TYPE_GROUPE_INEXISTANT);
			return result;
		}

		typeAbsence.setLabel(typeAbsenceDto.getLibelle());

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
			}else{
				typeSaisi.setRefUnitePeriodeQuota(null);
			}
			typeAbsence.setTypeSaisi(typeSaisi);
		}
		
		// on check la saisie 
		IAbsenceDataConsistencyRules absenceDataConsistencyRulesImpl = dataConsistencyRulesFactory.getFactory(
				typeAbsence.getGroupe().getIdRefGroupeAbsence(), typeAbsence.getIdRefTypeAbsence());
		
		result = absenceDataConsistencyRulesImpl.checkSaisiNewTypeAbsence(typeAbsence.getTypeSaisi(), result);
		
		if(!result.getErrors().isEmpty()) {
			return result;
		}
		
		typeAbsenceRepository.persistEntity(typeAbsence);

		addMessageConfirmation(typeAbsenceDto.getIdRefTypeAbsence(), result);

		return result;
	}

	@Override
	@Transactional(value = "absTransactionManager")
	public ReturnMessageDto deleteTypeAbsence(Integer idAgent, Integer idTypeDemande) {

		ReturnMessageDto result = new ReturnMessageDto();

		// verification des droits SIRH
		ReturnMessageDto isUtilisateurSIRH = sirhWSConsumer.isUtilisateurSIRH(idAgent);
		if (!isUtilisateurSIRH.getErrors().isEmpty()) {
			logger.warn(AGENT_NON_HABILITE);
			result.getErrors().add(String.format(AGENT_NON_HABILITE));
			return result;
		}

		RefTypeAbsence typeAbsence = typeAbsenceRepository.getEntity(RefTypeAbsence.class, idTypeDemande);

		if (null == typeAbsence) {
			logger.debug(TYPE_ABSENCE_INEXISTANT);
			result.getErrors().add(TYPE_ABSENCE_INEXISTANT);
			return result;
		}
		// suppression
		typeAbsenceRepository.removeEntity(typeAbsence);

		result.getInfos().add(String.format(TYPE_ABSENCE_SUPPRIME));

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

}
