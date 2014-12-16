package nc.noumea.mairie.abs.service.rules.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeAsa;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IAsaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("AbsAsaDataConsistencyRulesImpl")
public class AbsAsaDataConsistencyRulesImpl extends AbstractAbsenceDataConsistencyRules {

	public static final String DEPASSEMENT_DROITS_ASA_MSG = "Les droits pour ce type d'absence syndicale sont dépassés.";
	public static final String AUCUN_DROITS_ASA_MSG = "L'agent [%d] ne possède pas de droit pour les absences syndicales.";
	protected static final String OS_INEXISTANT = "L'organisation syndicale n'existe pas.";
	protected static final String OS_INACTIVE = "L'organisation syndicale n'est pas active.";

	@Autowired
	protected IAsaRepository asaRepository;

	@Override
	public void processDataConsistencyDemande(ReturnMessageDto srm, Integer idAgent, Demande demande, Date dateLundi,
			boolean isProvenanceSIRH) {
		checkEtatsDemandeAcceptes(srm, demande, Arrays.asList(RefEtatEnum.PROVISOIRE, RefEtatEnum.SAISIE));
		super.processDataConsistencyDemande(srm, idAgent, demande, dateLundi, isProvenanceSIRH);
	}

	protected boolean isAfficherBoutonAnnuler(DemandeDto demandeDto, boolean isOperateur) {
		return demandeDto.getIdRefEtat().equals(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.APPROUVEE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.VALIDEE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.EN_ATTENTE.getCodeEtat())
				|| (isOperateur && demandeDto.getIdRefEtat().equals(RefEtatEnum.PRISE.getCodeEtat()));
	}

	@Override
	public DemandeDto filtreDroitOfDemandeSIRH(DemandeDto demandeDto) {

		demandeDto.setAffichageBoutonAnnuler(isAfficherBoutonAnnuler(demandeDto, true));
		demandeDto.setAffichageValidation(demandeDto.getIdRefEtat().equals(RefEtatEnum.APPROUVEE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.EN_ATTENTE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.VALIDEE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.REJETE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.PRISE.getCodeEtat()));
		demandeDto.setModifierValidation(demandeDto.getIdRefEtat().equals(RefEtatEnum.APPROUVEE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.EN_ATTENTE.getCodeEtat()));
		demandeDto.setAffichageEnAttente(demandeDto.getIdRefEtat().equals(RefEtatEnum.APPROUVEE.getCodeEtat()));
		demandeDto.setAffichageBoutonDupliquer(true);

		return demandeDto;
	}

	@Override
	public ReturnMessageDto checkEtatsDemandeAnnulee(ReturnMessageDto srm, Demande demande,
			List<RefEtatEnum> listEtatsAcceptes) {

		List<RefEtatEnum> listEtats = new ArrayList<RefEtatEnum>();
		listEtats.addAll(listEtatsAcceptes);
		listEtats.addAll(Arrays.asList(RefEtatEnum.VALIDEE, RefEtatEnum.EN_ATTENTE, RefEtatEnum.PRISE));
		// dans le cas des ASA A48, on peut annuler en plus les demandes a l
		// etat VALIDEE et EN_ATTENTE
		return super.checkEtatsDemandeAnnulee(srm, demande, listEtats);
	}

	protected void checkOrganisationSyndicale(ReturnMessageDto srm, DemandeAsa demande) {

		if (null == demande.getOrganisationSyndicale()) {
			logger.warn(OS_INEXISTANT);
			srm.getErrors().add(String.format(OS_INEXISTANT));
		} else if (!demande.getOrganisationSyndicale().isActif()) {
			logger.warn(OS_INACTIVE);
			srm.getErrors().add(String.format(OS_INACTIVE));
		}
	}

	public boolean checkEtatDemandePourDepassementCompteurAgent(DemandeDto demandeDto) {

		if (demandeDto.getIdRefEtat().equals(RefEtatEnum.VALIDEE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.REJETE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.PRISE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.ANNULEE.getCodeEtat())) {
			return false;
		}

		return true;
	}

}
