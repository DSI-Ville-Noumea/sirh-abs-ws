package nc.noumea.mairie.abs.service.impl;

import nc.noumea.mairie.abs.domain.Droit;
import nc.noumea.mairie.abs.domain.Profil;
import nc.noumea.mairie.abs.dto.AccessRightsDto;
import nc.noumea.mairie.abs.repository.IAccessRightsRepository;
import nc.noumea.mairie.abs.service.IAccessRightsService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccessRightsService implements IAccessRightsService {

	private Logger logger = LoggerFactory.getLogger(AccessRightsService.class);

	@Autowired
	private IAccessRightsRepository accessRightsRepository;

	@Override
	public AccessRightsDto getAgentAccessRights(Integer idAgent) {

		AccessRightsDto result = new AccessRightsDto();

		for (Droit da : accessRightsRepository.getAgentAccessRights(idAgent)) {

			for (Profil pr : da.getProfils()) {
				result.setSaisie(result.isSaisie() || pr.isSaisie());
				result.setModification(result.isModification() || pr.isModification());
				result.setSuppression(result.isSuppression() || pr.isSuppression());
				result.setImpression(result.isImpression() || pr.isImpression());
				result.setViserVisu(result.isViserVisu() || pr.isViserVisu());
				result.setViserModif(result.isViserModif() || pr.isViserModif());
				result.setApprouverVisu(result.isApprouverVisu() || pr.isApprouverVisu());
				result.setApprouverModif(result.isApprouverModif() || pr.isApprouverModif());
				result.setAnnuler(result.isAnnuler() || pr.isAnnuler());
				result.setVisuSolde(result.isVisuSolde() || pr.isVisuSolde());
				result.setMajSolde(result.isMajSolde() || pr.isMajSolde());
				result.setDroitAcces(result.isDroitAcces() || pr.isDroitAcces());
			}
		}

		return result;
	}
}
