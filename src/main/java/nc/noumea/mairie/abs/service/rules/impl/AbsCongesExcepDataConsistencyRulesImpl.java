package nc.noumea.mairie.abs.service.rules.impl;

import java.util.Arrays;
import java.util.Date;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeCongesExceptionnels;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

import org.springframework.stereotype.Service;

@Service("AbsCongesExcepDataConsistencyRulesImpl")
public class AbsCongesExcepDataConsistencyRulesImpl extends AbstractAbsenceDataConsistencyRules {

	public static final String CHAMP_COMMENTAIRE_OBLIGATOIRE = "Le champ Commentaire est obligatoire.";
	
	@Override
	public void processDataConsistencyDemande(ReturnMessageDto srm, Integer idAgent, Demande demande, Date dateLundi) {
		checkEtatsDemandeAcceptes(srm, demande, Arrays.asList(RefEtatEnum.PROVISOIRE, RefEtatEnum.SAISIE));
		checkChampMotifDemandeSaisi(srm, (DemandeCongesExceptionnels)demande);
		super.processDataConsistencyDemande(srm, idAgent, demande, dateLundi);
	}
	
	protected ReturnMessageDto checkChampMotifDemandeSaisi(ReturnMessageDto srm, DemandeCongesExceptionnels demande) {
		
		if(null != demande.getType().getTypeSaisi()) {
			if((null == demande.getCommentaire()
					|| "".equals(demande.getCommentaire().trim()))
					&& demande.getType().getTypeSaisi().isMotif()){
				logger.warn(String.format(CHAMP_COMMENTAIRE_OBLIGATOIRE, demande.getIdAgent()));
				srm.getErrors().add(String.format(CHAMP_COMMENTAIRE_OBLIGATOIRE, demande.getIdAgent()));
			}
		}

		return srm;
	}
	
	protected boolean isAfficherBoutonImprimer(DemandeDto demandeDto) {
		return demandeDto.getIdRefEtat().equals(RefEtatEnum.VALIDEE.getCodeEtat());
	}
	
	protected boolean isAfficherBoutonAnnuler(DemandeDto demandeDto, boolean isOperateur) {
		return demandeDto.getIdRefEtat().equals(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.APPROUVEE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.VALIDEE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.EN_ATTENTE.getCodeEtat())
				|| (isOperateur && demandeDto.getIdRefEtat().equals(RefEtatEnum.PRISE.getCodeEtat()));
	}
}
