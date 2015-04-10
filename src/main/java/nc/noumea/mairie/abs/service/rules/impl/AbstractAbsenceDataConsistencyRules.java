package nc.noumea.mairie.abs.service.rules.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DroitDroitsAgent;
import nc.noumea.mairie.abs.domain.DroitProfil;
import nc.noumea.mairie.abs.domain.DroitsAgent;
import nc.noumea.mairie.abs.domain.Profil;
import nc.noumea.mairie.abs.domain.RefEtat;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeGroupeAbsenceEnum;
import nc.noumea.mairie.abs.domain.RefTypeSaisi;
import nc.noumea.mairie.abs.domain.RefTypeSaisiCongeAnnuel;
import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IAccessRightsRepository;
import nc.noumea.mairie.abs.repository.ICounterRepository;
import nc.noumea.mairie.abs.repository.IDemandeRepository;
import nc.noumea.mairie.abs.repository.IOrganisationSyndicaleRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.abs.service.IAbsenceDataConsistencyRules;
import nc.noumea.mairie.abs.service.IAgentMatriculeConverterService;
import nc.noumea.mairie.abs.service.IAgentService;
import nc.noumea.mairie.abs.service.impl.HelperService;
import nc.noumea.mairie.domain.Spadmn;
import nc.noumea.mairie.domain.Spcarr;
import nc.noumea.mairie.ws.IPtgWsConsumer;
import nc.noumea.mairie.ws.ISirhWSConsumer;

import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractAbsenceDataConsistencyRules implements IAbsenceDataConsistencyRules {

	protected Logger logger = LoggerFactory.getLogger(AbstractAbsenceDataConsistencyRules.class);

	@Autowired
	protected ICounterRepository counterRepository;

	@Autowired
	protected IOrganisationSyndicaleRepository organisationSyndicaleRepository;

	@Autowired
	protected ISirhRepository sirhRepository;

	@Autowired
	protected HelperService helperService;

	@Autowired
	protected IDemandeRepository demandeRepository;

	@Autowired
	protected IAccessRightsRepository accessRightsRepository;

	@Autowired
	protected ISirhWSConsumer sirhWSConsumer;

	@Autowired
	protected IPtgWsConsumer ptgWSConsumer;

	@Autowired
	protected IAgentService agentService;

	@Autowired
	protected IAgentMatriculeConverterService agentMatriculeService;

	@PersistenceContext(unitName = "absPersistenceUnit")
	private EntityManager absEntityManager;

	public static final String ETAT_NON_AUTORISE_MSG = "La modification de la demande [%d] n'est autorisée que si l'état est à [%s].";
	public static final String DEPASSEMENT_DROITS_ACQUIS_MSG = "Le dépassement des droits acquis n'est pas autorisé.";
	public static final String INACTIVITE_MSG = "L'agent n'est pas en activité sur cette période.";
	public static final String DEMANDE_DEJA_COUVERTE_MSG = "La demande ne peut être couverte totalement ou partiellement par une autre absence.";
	public static final String DEMANDE_MAUVAISE_DATE_MSG = "La date de fin ne peut pas être inférieure à la date de début.";
	public static final String MOTIF_OBLIGATOIRE = "Le motif est obligatoire.";
	public static final String DEMANDE_INEXISTANTE = "La demande n'existe pas.";
	public static final String STATUT_AGENT = "L'agent [%d] ne peut pas avoir de repos compensateur. Les repos compensateurs sont pour les contractuels ou les conventions collectives.";
	public static final String STATUT_AGENT_FONCTIONNAIRE = "Ce type de demande ne peut pas être saisi par les fonctionnaires.";
	public static final String STATUT_AGENT_CONTRACTUEL = "Ce type de demande ne peut pas être saisi par les contractuels.";
	public static final String STATUT_AGENT_CONV_COLL = "Ce type de demande ne peut pas être saisi par les conventions collectives.";
	public static final String SAISIE_KIOSQUE_NON_AUTORISEE = "Ce type de demande n'est pas géré depuis le Kiosque RH.";
	public static final String SAISIE_TYPE_ABSENCE_NON_AUTORISEE = "La saisie de nouveau type d'absence pour ce groupe d'absence n'est pas autorisée.";
	public static final String BASE_HORAIRE_AGENT = "L'agent [%d] n'a pas de base congé défini. Merci de contacter votre référent RH.";
	public static final String CHAMP_COMMENTAIRE_OBLIGATOIRE = "Le champ Commentaire est obligatoire.";
	public static final String SAISIE_NON_MULTIPLE = "Pour la base congé %s, la durée du congé doit être un multiple de %d jours.";
	public static final String STATUT_AGENT_NON_ELIGIBLE_CONGE_ANNUEL = "Ce type de demande ne peut pas être saisi par les adjoints, conseillers municipaux et le maire.";

	public static final List<String> ACTIVITE_CODES = Arrays.asList("01", "02", "03", "04", "23", "24", "60", "61",
			"62", "63", "64", "65", "66");

	/**
	 * Processes the data consistency of a set of Pointages being input by a
	 * user. It will check the different business rules in order to make sure
	 * they're consistent
	 */
	@Override
	public void processDataConsistencyDemande(ReturnMessageDto srm, Integer idAgent, Demande demande, Date dateLundi,
			boolean isProvenanceSIRH) {
		checkDateDebutInferieurDateFin(srm, demande.getDateDebut(), demande.getDateFin());
		checkSaisieKiosqueAutorisee(srm, demande.getType().getTypeSaisi(), isProvenanceSIRH);
		if (srm.getErrors().size() == 0)
			checkDemandeDejaSaisieSurMemePeriode(srm, demande);
		checkAgentInactivity(srm, demande.getIdAgent(), dateLundi);
		checkStatutAgent(srm, demande);
		checkNoPointages(srm, demande);

	}

	private void checkNoPointages(ReturnMessageDto srm, Demande demande) {
		ReturnMessageDto result = ptgWSConsumer.checkPointage(demande.getIdAgent(), demande.getDateDebut(),
				demande.getDateFin());

		for (String info : result.getInfos()) {
			srm.getInfos().add(info);
		}
		for (String erreur : result.getErrors()) {
			srm.getErrors().add(erreur);
		}
	}

	@Override
	public ReturnMessageDto checkDateDebutInferieurDateFin(ReturnMessageDto srm, Date dateDebut, Date dateFin) {

		if (dateFin != null && dateFin.before(dateDebut)) {
			logger.warn(String.format(DEMANDE_MAUVAISE_DATE_MSG));
			srm.getErrors().add(DEMANDE_MAUVAISE_DATE_MSG);
		}

		return srm;
	}

	@Override
	public ReturnMessageDto checkDemandeDejaSaisieSurMemePeriode(ReturnMessageDto srm, Demande demande) {

		List<Demande> listDemande = demandeRepository.listeDemandesAgent(null, demande.getIdAgent(), null, null, null,
				null);

		Interval intervalDemande = new Interval(demande.getDateDebut().getTime(), demande.getDateFin().getTime());

		for (Demande demandeExistante : listDemande) {

			if ((null == demande.getIdDemande() || (null != demande.getIdDemande() && !demandeExistante.getIdDemande()
					.equals(demande.getIdDemande())))
					&& null != demandeExistante.getLatestEtatDemande()
					&& !RefEtatEnum.REFUSEE.equals(demandeExistante.getLatestEtatDemande().getEtat())
					&& !RefEtatEnum.PROVISOIRE.equals(demandeExistante.getLatestEtatDemande().getEtat())
					&& !RefEtatEnum.ANNULEE.equals(demandeExistante.getLatestEtatDemande().getEtat())
					&& !RefEtatEnum.REJETE.equals(demandeExistante.getLatestEtatDemande().getEtat())) {

				Interval intervalDemandeExistante = new Interval(demandeExistante.getDateDebut().getTime(),
						demandeExistante.getDateFin().getTime());

				if (intervalDemandeExistante.overlaps(intervalDemande)) {
					logger.warn(String.format(DEMANDE_DEJA_COUVERTE_MSG));
					srm.getErrors().add(DEMANDE_DEJA_COUVERTE_MSG);
					return srm;
				}
			}
		}

		return srm;
	}

	@Override
	public ReturnMessageDto checkAgentInactivity(ReturnMessageDto srm, Integer idAgent, Date dateLundi) {

		AgentGeneriqueDto ag = sirhWSConsumer.getAgent(idAgent);

		Spadmn adm = sirhRepository.getAgentCurrentPosition(ag.getNomatr(), dateLundi);

		if (null == adm || !ACTIVITE_CODES.contains(adm.getCdpadm())) {
			logger.warn(String.format(INACTIVITE_MSG));
			srm.getErrors().add(INACTIVITE_MSG);
		}

		return srm;
	}

	@Override
	public ReturnMessageDto checkChampMotifPourEtatDonne(ReturnMessageDto srm, Integer etat, String motif) {

		if (null == motif && etat.equals(RefEtatEnum.REFUSEE.getCodeEtat())) {
			logger.warn(String.format(MOTIF_OBLIGATOIRE));
			srm.getErrors().add(MOTIF_OBLIGATOIRE);
		}

		if (null == motif && etat.equals(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat())) {
			logger.warn(String.format(MOTIF_OBLIGATOIRE));
			srm.getErrors().add(MOTIF_OBLIGATOIRE);
		}

		if (null == motif && etat.equals(RefEtatEnum.ANNULEE.getCodeEtat())) {
			logger.warn(String.format(MOTIF_OBLIGATOIRE));
			srm.getErrors().add(MOTIF_OBLIGATOIRE);
		}

		return srm;
	}

	@Override
	public ReturnMessageDto checkSaisieKiosqueAutorisee(ReturnMessageDto srm, RefTypeSaisi typeSaisi,
			boolean isProvenanceSIRH) {
		if (typeSaisi != null) {
			if (!isProvenanceSIRH && !typeSaisi.isSaisieKiosque()) {
				logger.warn(String.format(SAISIE_KIOSQUE_NON_AUTORISEE));
				srm.getErrors().add(SAISIE_KIOSQUE_NON_AUTORISEE);
			}
		}
		return srm;
	}

	@Override
	public ReturnMessageDto verifDemandeExiste(Demande demande, ReturnMessageDto returnDto) {
		if (null == demande) {
			logger.warn(DEMANDE_INEXISTANTE);
			returnDto.getErrors().add(String.format(DEMANDE_INEXISTANTE));
			return returnDto;
		}
		return returnDto;
	}

	@Override
	public ReturnMessageDto checkEtatsDemandeAnnulee(ReturnMessageDto srm, Demande demande,
			List<RefEtatEnum> listEtatsAcceptes) {

		return checkEtatsDemandeAcceptes(srm, demande, listEtatsAcceptes);
	}

	@Override
	public ReturnMessageDto checkEtatsDemandeAcceptes(ReturnMessageDto srm, Demande demande,
			List<RefEtatEnum> listEtatsAcceptes) {

		if (null != demande.getLatestEtatDemande()
				&& !listEtatsAcceptes.contains(demande.getLatestEtatDemande().getEtat())) {
			logger.warn(String.format(ETAT_NON_AUTORISE_MSG, demande.getIdDemande(),
					RefEtatEnum.listToString(listEtatsAcceptes)));
			srm.getErrors().add(
					String.format(ETAT_NON_AUTORISE_MSG, demande.getIdDemande(),
							RefEtatEnum.listToString(listEtatsAcceptes)));
		}

		return srm;
	}

	@Override
	public DemandeDto filtreDroitOfDemande(Integer idAgentConnecte, DemandeDto demandeDto,
			List<DroitsAgent> listDroitAgent) {

		// test 1
		if (demandeDto.getAgentWithServiceDto().getIdAgent().equals(idAgentConnecte)) {
			demandeDto.setAffichageBoutonModifier(demandeDto.getIdRefEtat()
					.equals(RefEtatEnum.PROVISOIRE.getCodeEtat())
					|| demandeDto.getIdRefEtat().equals(RefEtatEnum.SAISIE.getCodeEtat()));
			demandeDto.setAffichageBoutonSupprimer(demandeDto.getIdRefEtat().equals(
					RefEtatEnum.PROVISOIRE.getCodeEtat())
					|| demandeDto.getIdRefEtat().equals(RefEtatEnum.SAISIE.getCodeEtat()));
			demandeDto.setAffichageBoutonImprimer(isAfficherBoutonImprimer(demandeDto));
			demandeDto.setAffichageBoutonAnnuler(isAfficherBoutonAnnuler(demandeDto, false));

			return demandeDto;
		}

		for (DroitsAgent droitsAgent : listDroitAgent) {

			if (demandeDto.getAgentWithServiceDto().getIdAgent().equals(droitsAgent.getIdAgent())) {

				for (DroitDroitsAgent dda : droitsAgent.getDroitDroitsAgent()) {

					// #14306
					DroitProfil droitProfil = dda.getDroitProfil();
					Profil currentProfil = droitProfil.getProfil();

					demandeDto.setAffichageBoutonModifier(demandeDto.isAffichageBoutonModifier()
							|| ((demandeDto.getIdRefEtat().equals(RefEtatEnum.PROVISOIRE.getCodeEtat()) || demandeDto
									.getIdRefEtat().equals(RefEtatEnum.SAISIE.getCodeEtat())) && currentProfil
									.isModification()));

					demandeDto.setAffichageBoutonSupprimer(demandeDto.isAffichageBoutonSupprimer()
							|| ((demandeDto.getIdRefEtat().equals(RefEtatEnum.PROVISOIRE.getCodeEtat()) || demandeDto
									.getIdRefEtat().equals(RefEtatEnum.SAISIE.getCodeEtat())) && currentProfil
									.isSuppression()));

					demandeDto.setAffichageBoutonImprimer(demandeDto.isAffichageBoutonImprimer()
							|| (isAfficherBoutonImprimer(demandeDto) && currentProfil.isImpression()));
					demandeDto.setAffichageBoutonAnnuler(demandeDto.isAffichageBoutonAnnuler()
							|| (isAfficherBoutonAnnuler(demandeDto, currentProfil.isAnnuler()) && currentProfil
									.isAnnuler()));
					demandeDto.setAffichageBoutonDupliquer(false);

					demandeDto.setAffichageVisa(demandeDto.isAffichageVisa() || currentProfil.isViserVisu());
					demandeDto
							.setModifierVisa(demandeDto.isModifierVisa()
									|| ((demandeDto.getIdRefEtat().equals(RefEtatEnum.SAISIE.getCodeEtat())
											|| demandeDto.getIdRefEtat().equals(
													RefEtatEnum.VISEE_FAVORABLE.getCodeEtat()) || demandeDto
											.getIdRefEtat().equals(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat())) && currentProfil
												.isViserModif()));

					demandeDto.setAffichageApprobation(demandeDto.isAffichageApprobation()
							|| currentProfil.isApprouverVisu());
					demandeDto
							.setModifierApprobation(demandeDto.isModifierApprobation()
									|| ((demandeDto.getIdRefEtat().equals(RefEtatEnum.SAISIE.getCodeEtat())
											|| demandeDto.getIdRefEtat().equals(
													RefEtatEnum.VISEE_FAVORABLE.getCodeEtat())
											|| demandeDto.getIdRefEtat().equals(
													RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat())
											|| demandeDto.getIdRefEtat().equals(RefEtatEnum.APPROUVEE.getCodeEtat()) || demandeDto
											.getIdRefEtat().equals(RefEtatEnum.REFUSEE.getCodeEtat())) && currentProfil
												.isApprouverModif()));
				}
			}
		}

		return demandeDto;
	}

	@Override
	public DemandeDto filtreDroitOfDemandeSIRH(DemandeDto demandeDto) {

		demandeDto.setAffichageBoutonAnnuler(isAfficherBoutonAnnuler(demandeDto, true));
		demandeDto.setAffichageValidation(false);
		demandeDto.setModifierValidation(false);
		demandeDto.setAffichageEnAttente(false);
		demandeDto.setAffichageBoutonDupliquer(true);

		return demandeDto;
	}

	protected boolean isAfficherBoutonImprimer(DemandeDto demandeDto) {
		// cf redmine #13378
		if (demandeDto.getGroupeAbsence() != null) {
			switch (RefTypeGroupeAbsenceEnum.getRefTypeGroupeAbsenceEnum(demandeDto.getGroupeAbsence()
					.getIdRefGroupeAbsence())) {
				case REPOS_COMP:
				case RECUP:
					if (demandeDto.getIdRefEtat().equals(RefEtatEnum.APPROUVEE.getCodeEtat())
							|| demandeDto.getIdRefEtat().equals(RefEtatEnum.PRISE.getCodeEtat())) {
						return true;
					}
					break;
				case AS:
				case CONGES_EXCEP:
					if (demandeDto.getIdRefEtat().equals(RefEtatEnum.VALIDEE.getCodeEtat())
							|| demandeDto.getIdRefEtat().equals(RefEtatEnum.PRISE.getCodeEtat())) {
						return true;
					}
					break;
				case CONGES_ANNUELS:
					if (demandeDto.getIdRefEtat().equals(RefEtatEnum.APPROUVEE.getCodeEtat())
							|| demandeDto.getIdRefEtat().equals(RefEtatEnum.VALIDEE.getCodeEtat())
							|| demandeDto.getIdRefEtat().equals(RefEtatEnum.PRISE.getCodeEtat())) {
						return true;
					}
					break;
				default:
					break;
			}
		}
		return false;

	}

	protected boolean isAfficherBoutonAnnuler(DemandeDto demandeDto, boolean isOperateur) {
		return demandeDto.getIdRefEtat().equals(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat())
				|| demandeDto.getIdRefEtat().equals(RefEtatEnum.APPROUVEE.getCodeEtat());
	}

	@Override
	public List<DemandeDto> filtreDateAndEtatDemandeFromList(List<Demande> listeSansFiltre, List<RefEtat> etats,
			Date dateDemande) {
		List<DemandeDto> listeDemandeDto = new ArrayList<DemandeDto>();
		if (listeSansFiltre.size() == 0)
			return listeDemandeDto;

		List<AgentWithServiceDto> listAgentsExistants = new ArrayList<AgentWithServiceDto>();

		if (dateDemande == null && etats == null) {
			for (Demande d : listeSansFiltre) {
				AgentWithServiceDto agentOptimise = agentService.getAgentOptimise(
						listAgentsExistants, d.getIdAgent(), d.getDateDebut());
				if (agentOptimise != null) {
					DemandeDto dto = new DemandeDto(d, agentOptimise);
					dto.updateEtat(
							d.getLatestEtatDemande(),
							agentService.getAgentOptimise(listAgentsExistants, d.getLatestEtatDemande()
									.getIdAgent(), d.getLatestEtatDemande().getDate()), d.getType().getGroupe());
					if (!listeDemandeDto.contains(dto)) {
						listeDemandeDto.add(dto);
					}
				}
			}
			return listeDemandeDto;
		}

		boolean isfiltreDateDemande = false;
		// ON TRAITE LA DATE DE DEMANDE
		if (dateDemande != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			String dateDemandeSDF = sdf.format(dateDemande);
			for (Demande d : listeSansFiltre) {
				String dateEtatSDF = sdf.format(d.getLatestEtatDemande().getDate());
				if (dateEtatSDF.equals(dateDemandeSDF)) {
					AgentWithServiceDto agentOptimise = agentService.getAgentOptimise(
							listAgentsExistants, d.getIdAgent(), d.getDateDebut());
					if (agentOptimise != null) {
						DemandeDto dto = new DemandeDto(d, agentOptimise);
						dto.updateEtat(
								d.getLatestEtatDemande(),
								agentService.getAgentOptimise(listAgentsExistants, d.getLatestEtatDemande()
										.getIdAgent(), d.getLatestEtatDemande().getDate()), d.getType().getGroupe());
						if (!listeDemandeDto.contains(dto)) {
							listeDemandeDto.add(dto);
						}
					}
				}
				isfiltreDateDemande = true;
			}
		}

		// ON TRAITE L'ETAT
		if (etats != null) {
			for (Demande d : listeSansFiltre) {
				AgentWithServiceDto agentOptimise = agentService.getAgentOptimise(
						listAgentsExistants, d.getIdAgent(), d.getDateDebut());
				if (agentOptimise != null) {
					DemandeDto dto = new DemandeDto(d, agentOptimise);
					dto.updateEtat(
							d.getLatestEtatDemande(),
							agentService.getAgentOptimise(listAgentsExistants, d.getLatestEtatDemande()
									.getIdAgent(), d.getLatestEtatDemande().getDate()), d.getType().getGroupe());
					if (etats.contains(absEntityManager.find(RefEtat.class, d.getLatestEtatDemande().getEtat()
							.getCodeEtat()))) {
						if (!listeDemandeDto.contains(dto) && !isfiltreDateDemande)
							listeDemandeDto.add(dto);
					} else {
						if (listeDemandeDto.contains(dto))
							listeDemandeDto.remove(dto);
					}
				}
			}
		}

		return listeDemandeDto;
	}

	@Override
	public boolean checkDepassementCompteurAgent(DemandeDto demandeDto) {
		return false;
	}

	@Override
	public boolean checkDepassementMultipleAgent(DemandeDto demandeDto) {
		return false;
	}

	public ReturnMessageDto checkStatutAgent(ReturnMessageDto srm, Demande demande) {
		// on recherche sa carriere pour avoir son statut (Fonctionnaire,
		// contractuel, convention coll
		Spcarr carr = sirhRepository.getAgentCurrentCarriere(
				agentMatriculeService.fromIdAgentToSIRHNomatrAgent(demande.getIdAgent()),
				helperService.getCurrentDate());

		if (null != demande.getType().getTypeSaisi()) {
			if (helperService.isFonctionnaire(carr) && !demande.getType().getTypeSaisi().isFonctionnaire()) {
				logger.warn(String.format(STATUT_AGENT_FONCTIONNAIRE, demande.getIdAgent()));
				srm.getErrors().add(String.format(STATUT_AGENT_FONCTIONNAIRE, demande.getIdAgent()));
				return srm;
			}
			if (helperService.isContractuel(carr) && !demande.getType().getTypeSaisi().isContractuel()) {
				logger.warn(String.format(STATUT_AGENT_CONTRACTUEL, demande.getIdAgent()));
				srm.getErrors().add(String.format(STATUT_AGENT_CONTRACTUEL, demande.getIdAgent()));
				return srm;
			}
			if (helperService.isConventionCollective(carr)
					&& !demande.getType().getTypeSaisi().isConventionCollective()) {
				logger.warn(String.format(STATUT_AGENT_CONV_COLL, demande.getIdAgent()));
				srm.getErrors().add(String.format(STATUT_AGENT_CONV_COLL, demande.getIdAgent()));
				return srm;
			}
		}
		if (null != demande.getType().getTypeSaisiCongeAnnuel()) {
			if (!helperService.isAgentEligibleCongeAnnuel(carr)) {
				logger.warn(String.format(STATUT_AGENT_NON_ELIGIBLE_CONGE_ANNUEL, demande.getIdAgent()));
				srm.getErrors().add(String.format(STATUT_AGENT_NON_ELIGIBLE_CONGE_ANNUEL, demande.getIdAgent()));
				return srm;
			}
		}

		return srm;
	}

	@Override
	public ReturnMessageDto checkSaisiNewTypeAbsence(RefTypeSaisi typeSaisi,
			RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel, ReturnMessageDto srm) {
		logger.warn(String.format(SAISIE_TYPE_ABSENCE_NON_AUTORISEE));
		srm.getErrors().add(String.format(SAISIE_TYPE_ABSENCE_NON_AUTORISEE));
		return srm;
	}

	@Override
	public ReturnMessageDto checkDepassementDroitsAcquis(ReturnMessageDto srm, Demande demande) {
		return srm;
	}

	@Override
	public void checkSamediOffertToujoursOk(DemandeEtatChangeDto demandeEtatChangeDto, Demande demande) {
		// ne concerne que les conges annuels
	}
}
