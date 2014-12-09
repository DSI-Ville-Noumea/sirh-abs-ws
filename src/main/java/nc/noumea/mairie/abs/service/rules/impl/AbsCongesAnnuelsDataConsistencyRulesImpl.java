package nc.noumea.mairie.abs.service.rules.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentCongeAnnuelCount;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeCongesAnnuels;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeSaisi;
import nc.noumea.mairie.abs.domain.RefTypeSaisiCongeAnnuel;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.RefTypeSaisiCongeAnnuelDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.ICongesAnnuelsRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("AbsCongesAnnuelsDataConsistencyRulesImpl")
public class AbsCongesAnnuelsDataConsistencyRulesImpl extends AbstractAbsenceDataConsistencyRules {

	@Autowired
	protected ICongesAnnuelsRepository congesAnnuelsRepository;

	@Override
	public void processDataConsistencyDemande(ReturnMessageDto srm, Integer idAgent, Demande demande, Date dateLundi,
			boolean isProvenanceSIRH) {
		checkEtatsDemandeAcceptes(srm, demande, Arrays.asList(RefEtatEnum.PROVISOIRE, RefEtatEnum.SAISIE));
		checkBaseHoraireAbsenceAgent(srm, demande.getIdAgent(), demande.getDateDebut());
		checkDepassementDroitsAcquis(srm, demande);
		checkChampMotifDemandeSaisi(srm, (DemandeCongesAnnuels) demande);
		checkMultipleCycle(srm, (DemandeCongesAnnuels) demande, idAgent);

		super.processDataConsistencyDemande(srm, idAgent, demande, dateLundi, isProvenanceSIRH);
	}

	protected ReturnMessageDto checkMultipleCycle(ReturnMessageDto srm, DemandeCongesAnnuels demande, Integer idAgent) {
		double nbJours = 0.0;
		if (demande.getTypeSaisiCongeAnnuel().getQuotaMultiple() != null) {
			switch (demande.getTypeSaisiCongeAnnuel().getCodeBaseHoraireAbsence()) {
				case "C":
				case "E":
				case "F":
					nbJours = helperService.calculNombreJours(demande.getDateDebut(), demande.getDateFin());
					if (nbJours % demande.getTypeSaisiCongeAnnuel().getQuotaMultiple() != 0) {
						if (accessRightsRepository.isOperateurOfAgent(idAgent, demande.getIdAgent())
								|| sirhWSConsumer.isUtilisateurSIRH(idAgent).getErrors().size() == 0) {
							logger.warn(String.format(SAISIE_NON_MULTIPLE, demande.getTypeSaisiCongeAnnuel()
									.getCodeBaseHoraireAbsence(), demande.getTypeSaisiCongeAnnuel().getQuotaMultiple()));
							srm.getInfos().add(
									String.format(SAISIE_NON_MULTIPLE, demande.getTypeSaisiCongeAnnuel()
											.getCodeBaseHoraireAbsence(), demande.getTypeSaisiCongeAnnuel()
											.getQuotaMultiple()));
						} else {
							logger.warn(String.format(SAISIE_NON_MULTIPLE, demande.getTypeSaisiCongeAnnuel()
									.getCodeBaseHoraireAbsence(), demande.getTypeSaisiCongeAnnuel().getQuotaMultiple()));
							srm.getErrors().add(
									String.format(SAISIE_NON_MULTIPLE, demande.getTypeSaisiCongeAnnuel()
											.getCodeBaseHoraireAbsence(), demande.getTypeSaisiCongeAnnuel()
											.getQuotaMultiple()));
						}
					}
					break;

				default:
					break;
			}
		}

		return srm;
	}

	protected ReturnMessageDto checkChampMotifDemandeSaisi(ReturnMessageDto srm, DemandeCongesAnnuels demande) {
		if (null == demande.getCommentaire() || "".equals(demande.getCommentaire().trim())) {
			logger.warn(String.format(CHAMP_COMMENTAIRE_OBLIGATOIRE, demande.getIdAgent()));
			srm.getErrors().add(String.format(CHAMP_COMMENTAIRE_OBLIGATOIRE, demande.getIdAgent()));
		}

		return srm;
	}

	protected ReturnMessageDto checkBaseHoraireAbsenceAgent(ReturnMessageDto srm, Integer idAgent, Date dateDemande) {
		// on recherche sa base horaire d'absence
		RefTypeSaisiCongeAnnuelDto dtoBase = sirhWSConsumer.getBaseHoraireAbsence(idAgent, dateDemande);
		if (dtoBase.getIdRefTypeSaisiCongeAnnuel() == null) {
			logger.warn(String.format(BASE_HORAIRE_AGENT, idAgent));
			srm.getErrors().add(String.format(BASE_HORAIRE_AGENT, idAgent));
		}

		return srm;
	}

	@Override
	public ReturnMessageDto checkDepassementDroitsAcquis(ReturnMessageDto srm, Demande demande) {
		// on recupere le solde de l agent
		AgentCongeAnnuelCount soldeCongeAnnuel = counterRepository.getAgentCounter(AgentCongeAnnuelCount.class,
				demande.getIdAgent());

		Double sommeDemandeEnCours = congesAnnuelsRepository
				.getSommeDureeDemandeCongeAnnuelEnCoursSaisieouViseeouAValider(demande.getIdAgent(),
						demande.getIdDemande());

		// TODO traiter le cas du -5jours

		if (null == soldeCongeAnnuel
				|| (soldeCongeAnnuel.getTotalJours() + soldeCongeAnnuel.getTotalJoursAnneeN1()) - sommeDemandeEnCours
						- ((DemandeCongesAnnuels) demande).getDuree() < 0) {
			logger.warn(String.format(DEPASSEMENT_DROITS_ACQUIS_MSG, demande.getIdDemande()));
			srm.getErrors().add(String.format(DEPASSEMENT_DROITS_ACQUIS_MSG, demande.getIdDemande()));
		}

		return srm;
	}

	@Override
	public ReturnMessageDto checkSaisiNewTypeAbsence(RefTypeSaisi typeSaisi,
			RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel, ReturnMessageDto srm) {
		if (typeSaisiCongeAnnuel == null || typeSaisiCongeAnnuel.getIdRefTypeSaisiCongeAnnuel() == null) {
			logger.warn(String.format(SAISIE_TYPE_ABSENCE_NON_AUTORISEE));
			srm.getErrors().add(String.format(SAISIE_TYPE_ABSENCE_NON_AUTORISEE));
			return srm;
		}
		if (!typeSaisiCongeAnnuel.isCalendarDateDebut())
			srm.getErrors().add(String.format("La date de début est obligatoire."));

		if (typeSaisiCongeAnnuel.isCalendarDateFin() && typeSaisiCongeAnnuel.isCalendarDateReprise())
			srm.getErrors().add(String.format("Si date de reprise est à oui, alors date de fin doit être à non."));

		if (!typeSaisiCongeAnnuel.isCalendarDateFin() && !typeSaisiCongeAnnuel.isCalendarDateReprise())
			srm.getErrors().add(String.format("Si date de reprise est à non, alors date de fin doit être à oui."));

		if (typeSaisiCongeAnnuel.isDecompteSamedi() && typeSaisiCongeAnnuel.isConsecutif())
			srm.getErrors().add(String.format("Si consécutif est à oui, alors décompte du samedi doit être à non."));

		if (!typeSaisiCongeAnnuel.isDecompteSamedi() && !typeSaisiCongeAnnuel.isConsecutif())
			srm.getErrors().add(String.format("Si consécutif est à non, alors décompte du samedi doit être à oui."));

		return srm;
	}

	@Override
	public boolean checkDepassementMultipleAgent(DemandeDto demandeDto) {
		return checkDepassementMultipleAgent(demandeDto.getTypeSaisiCongeAnnuel(), demandeDto.getDuree());
	}

	private boolean checkDepassementMultipleAgent(RefTypeSaisiCongeAnnuelDto refTypeSaisiCongeAnnuelDto, Double duree) {

		RefTypeSaisiCongeAnnuel typeSaisi = demandeRepository.getEntity(RefTypeSaisiCongeAnnuel.class,
				refTypeSaisiCongeAnnuelDto.getIdRefTypeSaisiCongeAnnuel());

		switch (typeSaisi.getCodeBaseHoraireAbsence()) {
			case "E":
			case "F":

				// si le quota max pour ce type de demande est a zero
				// on renvoie une alerte de depassement dans tous les cas
				if (typeSaisi.getQuotaMultiple() == null)
					return false;

				if (duree % typeSaisi.getQuotaMultiple() != 0) {
					return true;
				}

				break;
			case "C":

				// si le quota max pour ce type de demande est a zero
				// on renvoie une alerte de depassement dans tous les cas
				if (typeSaisi.getQuotaMultiple() == null)
					return false;

				if (duree % 3 != 0) {
					return true;
				}
				break;

			default:
				break;
		}

		return false;
	}

	protected boolean isAfficherBoutonAnnuler(DemandeDto demandeDto, boolean isOperateur) {
		return demandeDto.getIdRefEtat().equals(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.APPROUVEE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.VALIDEE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.EN_ATTENTE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.A_VALIDER.getCodeEtat())
				|| (isOperateur && demandeDto.getIdRefEtat().equals(RefEtatEnum.PRISE.getCodeEtat()));
	}

	@Override
	public ReturnMessageDto checkEtatsDemandeAnnulee(ReturnMessageDto srm, Demande demande,
			List<RefEtatEnum> listEtatsAcceptes) {

		List<RefEtatEnum> listEtats = new ArrayList<RefEtatEnum>();
		listEtats.addAll(listEtatsAcceptes);
		listEtats.addAll(Arrays.asList(RefEtatEnum.VISEE_FAVORABLE, RefEtatEnum.VISEE_DEFAVORABLE,
				RefEtatEnum.APPROUVEE, RefEtatEnum.PRISE, RefEtatEnum.VALIDEE, RefEtatEnum.EN_ATTENTE,
				RefEtatEnum.A_VALIDER));
		// dans le cas des CONGES ANNUELS, on peut tout annuler sauf
		// saisie,provisoire,refuse,rejeté et annulé
		return super.checkEtatsDemandeAnnulee(srm, demande, listEtats);
	}

	@Override
	public DemandeDto filtreDroitOfDemandeSIRH(DemandeDto demandeDto) {

		demandeDto.setAffichageBoutonAnnuler(isAfficherBoutonAnnuler(demandeDto, true));
		demandeDto.setAffichageValidation(demandeDto.getIdRefEtat().equals(RefEtatEnum.APPROUVEE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.A_VALIDER.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.EN_ATTENTE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.VALIDEE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.REJETE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.PRISE.getCodeEtat()));
		demandeDto.setModifierValidation(demandeDto.getIdRefEtat().equals(RefEtatEnum.A_VALIDER.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.EN_ATTENTE.getCodeEtat()));
		demandeDto.setAffichageEnAttente(demandeDto.getIdRefEtat().equals(RefEtatEnum.A_VALIDER.getCodeEtat()));
		demandeDto.setAffichageBoutonDupliquer(true);

		return demandeDto;
	}

	@Override
	public boolean checkDepassementCompteurAgent(DemandeDto demandeDto) {
		ReturnMessageDto dtoErreur = new ReturnMessageDto();
		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setIdAgent(demandeDto.getAgentWithServiceDto().getIdAgent());
		demande.setIdDemande(demandeDto.getIdDemande());
		demande.setDuree(demandeDto.getDuree());
		dtoErreur = checkDepassementDroitsAcquis(dtoErreur, demande);
		if (dtoErreur.getErrors().size() > 0) {
			return true;
		} else {
			return false;
		}
	}
}
