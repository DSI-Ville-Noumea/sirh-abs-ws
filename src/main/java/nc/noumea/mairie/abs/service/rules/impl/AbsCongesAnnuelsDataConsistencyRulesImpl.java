package nc.noumea.mairie.abs.service.rules.impl;

import java.util.Arrays;
import java.util.Date;

import nc.noumea.mairie.abs.domain.AgentCongeAnnuelCount;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeCongesAnnuels;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeSaisi;
import nc.noumea.mairie.abs.domain.RefTypeSaisiCongeAnnuel;
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
		checkMultipleCycle(srm, (DemandeCongesAnnuels) demande);

		super.processDataConsistencyDemande(srm, idAgent, demande, dateLundi, isProvenanceSIRH);
	}

	protected ReturnMessageDto checkMultipleCycle(ReturnMessageDto srm, DemandeCongesAnnuels demande) {
		if (demande.getTypeSaisiCongeAnnuel().getQuotaMultiple() != null) {
			switch (demande.getTypeSaisiCongeAnnuel().getCodeBaseHoraireAbsence()) {
				case "C":
					// TODO à finir
					logger.warn(String.format(SAISIE_NON_MULTIPLE, demande.getTypeSaisiCongeAnnuel()
							.getCodeBaseHoraireAbsence(), demande.getTypeSaisiCongeAnnuel().getQuotaMultiple()));
					srm.getErrors()
							.add(String.format(SAISIE_NON_MULTIPLE, demande.getTypeSaisiCongeAnnuel()
									.getCodeBaseHoraireAbsence(), demande.getTypeSaisiCongeAnnuel().getQuotaMultiple()));

					break;
				case "E":
				case "F":
					double nbJours = helperService.calculNombreJours(demande.getDateDebut(),
							demande.getDateFin());
					if (nbJours % demande.getTypeSaisiCongeAnnuel().getQuotaMultiple() != 0) {

						logger.warn(String.format(SAISIE_NON_MULTIPLE, demande.getTypeSaisiCongeAnnuel()
								.getCodeBaseHoraireAbsence(), demande.getTypeSaisiCongeAnnuel().getQuotaMultiple()));
						srm.getErrors().add(
								String.format(SAISIE_NON_MULTIPLE, demande.getTypeSaisiCongeAnnuel()
										.getCodeBaseHoraireAbsence(), demande.getTypeSaisiCongeAnnuel()
										.getQuotaMultiple()));
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

	protected ReturnMessageDto checkDepassementDroitsAcquis(ReturnMessageDto srm, Demande demande) {

		// on recupere le solde de l agent
		AgentCongeAnnuelCount soldeCongeAnnuel = counterRepository.getAgentCounter(AgentCongeAnnuelCount.class,
				demande.getIdAgent());

		Double sommeDemandeEnCours = congesAnnuelsRepository
				.getSommeDureeDemandeCongeAnnuelEnCoursSaisieouViseeouAValider(demande.getIdAgent(),
						demande.getIdDemande());

		if (null == soldeCongeAnnuel
				|| soldeCongeAnnuel.getTotalJours() - sommeDemandeEnCours - ((DemandeCongesAnnuels) demande).getDuree() < 0) {
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
}
