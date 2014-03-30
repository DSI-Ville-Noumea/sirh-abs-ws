package nc.noumea.mairie.abs.service.impl;

import java.util.ArrayList;
import java.util.List;

import nc.noumea.mairie.abs.domain.Motif;
import nc.noumea.mairie.abs.domain.MotifCompteur;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.dto.MotifCompteurDto;
import nc.noumea.mairie.abs.dto.MotifDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IMotifRepository;
import nc.noumea.mairie.abs.service.IMotifService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MotifService implements IMotifService {

	private Logger logger = LoggerFactory.getLogger(MotifService.class);

	// message d erreur
	public static final String MOTIF_MODIFIE_INEXISTANT = "Le motif à modifier n'existe pas.";
	public static final String TYPE_ABS_INEXISTANT = "Le type d'absence saisi n'existe pas.";
	public static final String LIBELLE_MOTIF_VIDE = "Le libellé du motif n'est pas saisi.";

	public static final String MOTIF_CREE = "Le motif est bien créé.";
	public static final String MOTIF_MODIFIE = "Le motif est bien modifié.";

	@Autowired
	private IMotifRepository motifRepository;

	@Override
	public List<MotifDto> getListeMotif() {

		List<MotifDto> res = new ArrayList<MotifDto>();
		List<Motif> listMotif = motifRepository.getListeMotif();
		if (listMotif != null) {
			for (Motif motif : listMotif) {
				MotifDto dto = new MotifDto(motif);
				res.add(dto);
			}
		}
		return res;
	}

	@Override
	public List<MotifCompteurDto> getListeMotifCompteur(Integer idRefType) {

		List<MotifCompteurDto> res = new ArrayList<MotifCompteurDto>();
		List<MotifCompteur> motifCompteur = motifRepository.getListeMotifCompteur(idRefType);

		if (motifCompteur != null) {
			for (MotifCompteur motif : motifCompteur) {
				MotifCompteurDto dto = new MotifCompteurDto(motif);
				res.add(dto);
			}
		}
		return res;
	}

	@Override
	public ReturnMessageDto setMotif(MotifDto motifDto) {

		ReturnMessageDto result = new ReturnMessageDto();
		Motif motif = null;

		if (null != motifDto.getIdMotif()) {
			motif = motifRepository.getEntity(Motif.class, motifDto.getIdMotif());
			if (null == motif) {
				logger.debug(MOTIF_MODIFIE_INEXISTANT);
				result.getErrors().add(MOTIF_MODIFIE_INEXISTANT);
				return result;
			}
		}

		if (!controlLibelleMotif(motifDto.getLibelle(), result))
			return result;

		if (null == motif) {
			motif = new Motif();
		}

		motif.setLibelle(motifDto.getLibelle());

		motifRepository.persistEntity(motif);

		addMessageConfirmation(motifDto.getIdMotif(), result);

		return result;
	}

	@Override
	public ReturnMessageDto setMotifCompteur(MotifCompteurDto motifCompteurDto) {

		ReturnMessageDto result = new ReturnMessageDto();
		MotifCompteur motifCompteur = null;

		if (null != motifCompteurDto.getIdMotifCompteur()) {
			motifCompteur = motifRepository.getEntity(MotifCompteur.class, motifCompteurDto.getIdMotifCompteur());
			if (null == motifCompteur) {
				logger.debug(MOTIF_MODIFIE_INEXISTANT);
				result.getErrors().add(MOTIF_MODIFIE_INEXISTANT);
				return result;
			}
		}

		RefTypeAbsence refTypeAbsence = getRefTypeAbsence(motifCompteurDto.getIdRefTypeAbsence(), result);
		if (!result.getErrors().isEmpty()) {
			return result;
		}

		if (!controlLibelleMotif(motifCompteurDto.getLibelle(), result))
			return result;

		if (null == motifCompteur) {
			motifCompteur = new MotifCompteur();
		}

		motifCompteur.setLibelle(motifCompteurDto.getLibelle());
		motifCompteur.setRefTypeAbsence(refTypeAbsence);

		motifRepository.persistEntity(motifCompteur);

		addMessageConfirmation(motifCompteurDto.getIdMotifCompteur(), result);

		return result;
	}

	protected RefTypeAbsence getRefTypeAbsence(Integer idRefTypeAbs, ReturnMessageDto message) {

		RefTypeAbsence refTypeAbsence = null;
		if (null == idRefTypeAbs) {
			logger.debug(TYPE_ABS_INEXISTANT);
			message.getErrors().add(TYPE_ABS_INEXISTANT);
			return refTypeAbsence;
		} else {
			refTypeAbsence = motifRepository.getEntity(RefTypeAbsence.class, idRefTypeAbs);
		}
		if (null == refTypeAbsence) {
			logger.debug(TYPE_ABS_INEXISTANT);
			message.getErrors().add(TYPE_ABS_INEXISTANT);
		}
		return refTypeAbsence;
	}

	private void addMessageConfirmation(Integer idMotif, ReturnMessageDto result) {

		if (null != idMotif) {
			logger.debug(MOTIF_MODIFIE);
			result.getInfos().add(MOTIF_MODIFIE);
		} else {
			logger.debug(MOTIF_CREE);
			result.getInfos().add(MOTIF_CREE);
		}
	}

	protected boolean controlLibelleMotif(String libelle, ReturnMessageDto result) {

		if (null == libelle || "".equals(libelle)) {
			logger.debug(LIBELLE_MOTIF_VIDE);
			result.getErrors().add(LIBELLE_MOTIF_VIDE);
			return false;
		}
		return true;
	}

}
