package nc.noumea.mairie.abs.service.impl;

import java.util.ArrayList;
import java.util.List;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeAsa;
import nc.noumea.mairie.abs.domain.DemandeCongesAnnuels;
import nc.noumea.mairie.abs.domain.DemandeCongesExceptionnels;
import nc.noumea.mairie.abs.domain.DemandeMaladies;
import nc.noumea.mairie.abs.domain.DemandeRecup;
import nc.noumea.mairie.abs.domain.DemandeReposComp;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeGroupeAbsenceEnum;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IDemandeRepository;
import nc.noumea.mairie.abs.service.IAbsenceDataConsistencyRules;
import nc.noumea.mairie.abs.service.IAccessRightsService;
import nc.noumea.mairie.abs.service.ISuppressionService;
import nc.noumea.mairie.abs.service.rules.impl.DataConsistencyRulesFactory;
import nc.noumea.mairie.abs.service.rules.impl.DefaultAbsenceDataConsistencyRulesImpl;
import nc.noumea.mairie.alfresco.cmis.IAlfrescoCMISService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SuppressionService implements ISuppressionService {

	private Logger logger = LoggerFactory.getLogger(SuppressionService.class);

	@Autowired
	private IDemandeRepository demandeRepository;

	@Autowired
	private IAccessRightsService accessRightsService;

	@Autowired
	private DataConsistencyRulesFactory dataConsistencyRulesFactory;

	@Autowired
	private IAlfrescoCMISService alfrescoCMISService;

	@Override
	@Transactional(value = "absTransactionManager")
	public ReturnMessageDto supprimerDemandeEtatProvisoire(Integer idDemande) {

		logger.info("Trying to delete demande id {} ...", idDemande);

		ReturnMessageDto result = new ReturnMessageDto();

		Demande demande = getDemande(Demande.class, idDemande);

		if (demande == null) {
			result.getErrors().add(String.format("La demande %s n'existe pas.", idDemande));
			logger.error("Demande id {} does not exists. Stopping process.", idDemande);
			return result;
		}
		if (demande.getLatestEtatDemande().getEtat() != RefEtatEnum.PROVISOIRE) {
			result.getErrors().add(
					String.format("La demande %s n'est pas à l'état %s mais %s.", idDemande,
							RefEtatEnum.PROVISOIRE.toString(), demande.getLatestEtatDemande().getEtat()));
			logger.error("Demande id {} is not in state [{}] but [{}]. Stopping process.", idDemande,
					RefEtatEnum.PROVISOIRE.toString(), demande.getLatestEtatDemande().getEtat().toString());
			return result;
		}

		// on supprime la demande et ses etats
		demandeRepository.removeEntity(demande);
		logger.info("Deleted demande id {}.", idDemande);

		return result;
	}

	@Override
	@Transactional(value = "absTransactionManager")
	public ReturnMessageDto supprimerDemande(Integer idAgent, Integer idDemande) {

		ReturnMessageDto returnDto = new ReturnMessageDto();

		Demande demande = demandeRepository.getEntity(Demande.class, idDemande);

		IAbsenceDataConsistencyRules absenceDataConsistencyRulesImpl = new DefaultAbsenceDataConsistencyRulesImpl();
		if (null != demande) {
			absenceDataConsistencyRulesImpl = dataConsistencyRulesFactory.getFactory(demande.getType().getGroupe()
					.getIdRefGroupeAbsence(), demande.getType().getIdRefTypeAbsence());
		}

		// on verifie si la demande existe
		returnDto = absenceDataConsistencyRulesImpl.verifDemandeExiste(demande, returnDto);
		if (0 < returnDto.getErrors().size())
			return returnDto;

		// selon le type de demande, on mappe les donnees specifiques de la
		// demande
		// et on effectue les verifications appropriees
		switch (RefTypeGroupeAbsenceEnum.getRefTypeGroupeAbsenceEnum(demande.getType().getGroupe()
				.getIdRefGroupeAbsence())) {
			case REPOS_COMP:
				demande = getDemande(DemandeReposComp.class, idDemande);
				break;
			case RECUP:
				demande = getDemande(DemandeRecup.class, idDemande);
				break;
			case AS:
				demande = getDemande(DemandeAsa.class, idDemande);
				break;
			case CONGES_EXCEP:
				demande = getDemande(DemandeCongesExceptionnels.class, idDemande);
				break;
			case CONGES_ANNUELS:
				demande = getDemande(DemandeCongesAnnuels.class, idDemande);
				break;
			case MALADIES:
				demande = getDemande(DemandeMaladies.class, idDemande);
				break;
			default:
				returnDto.getErrors().add(
						String.format("Le type [%d] de la demande n'est pas reconnu.", demande.getType()
								.getIdRefTypeAbsence()));
				demandeRepository.clear();
				return returnDto;
		}

		// on verifie si la demande existe
		returnDto = absenceDataConsistencyRulesImpl.verifDemandeExiste(demande, returnDto);
		if (0 < returnDto.getErrors().size())
			return returnDto;

		// verification des droits
		returnDto = accessRightsService.verifAccessRightDemande(idAgent, demande.getIdAgent(), returnDto);
		if (!returnDto.getErrors().isEmpty())
			return returnDto;

		// verifier l etat de la demande
		List<RefEtatEnum> listEtatAccepte = new ArrayList<RefEtatEnum>();
		listEtatAccepte.add(RefEtatEnum.PROVISOIRE);
		listEtatAccepte.add(RefEtatEnum.SAISIE);
		// #19711
		if(demande.getType().getGroupe().getIdRefGroupeAbsence().equals(RefTypeGroupeAbsenceEnum.MALADIES.getValue())) {
			listEtatAccepte.add(RefEtatEnum.A_VALIDER);
		}
		
		returnDto = absenceDataConsistencyRulesImpl.checkEtatsDemandeAcceptes(returnDto, demande,
				listEtatAccepte);

		if (0 < returnDto.getErrors().size()) {
			return returnDto;
		}
		
		returnDto = alfrescoCMISService.removeAllDocument(returnDto, demande);
		if (0 < returnDto.getErrors().size()) {
			return returnDto;
		}

		// suppression
		demandeRepository.removeEntity(demande);

		returnDto.getInfos().add(String.format("La demande est supprimée."));

		return returnDto;
	}

	protected <T> T getDemande(Class<T> Tclass, Integer idDemande) {
		if (null != idDemande) {
			return demandeRepository.getEntity(Tclass, idDemande);
		}

		try {
			return Tclass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			return null;
		}
	}

}
