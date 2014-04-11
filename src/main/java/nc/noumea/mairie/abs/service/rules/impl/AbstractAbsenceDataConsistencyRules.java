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
import nc.noumea.mairie.abs.domain.DroitsAgent;
import nc.noumea.mairie.abs.domain.ProfilEnum;
import nc.noumea.mairie.abs.domain.RefEtat;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IAccessRightsRepository;
import nc.noumea.mairie.abs.repository.IAsaRepository;
import nc.noumea.mairie.abs.repository.ICounterRepository;
import nc.noumea.mairie.abs.repository.IDemandeRepository;
import nc.noumea.mairie.abs.repository.IRecuperationRepository;
import nc.noumea.mairie.abs.repository.IReposCompensateurRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.abs.service.IAbsenceDataConsistencyRules;
import nc.noumea.mairie.abs.service.impl.HelperService;
import nc.noumea.mairie.domain.Spadmn;
import nc.noumea.mairie.sirh.domain.Agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractAbsenceDataConsistencyRules implements IAbsenceDataConsistencyRules {

	protected Logger logger = LoggerFactory.getLogger(AbstractAbsenceDataConsistencyRules.class);

	@Autowired
	protected IRecuperationRepository recuperationRepository;

	@Autowired
	protected IReposCompensateurRepository reposCompensateurRepository;

	@Autowired
	protected ICounterRepository counterRepository;

	@Autowired
	protected ISirhRepository sirhRepository;

	@Autowired
	protected HelperService helperService;

	@Autowired
	protected IDemandeRepository demandeRepository;

	@Autowired
	protected IAccessRightsRepository accessRightsRepository;
	
	@Autowired
	protected IAsaRepository asaRepository;

	@PersistenceContext(unitName = "absPersistenceUnit")
	private EntityManager absEntityManager;

	public static final String ETAT_NON_AUTORISE_MSG = "La modification de la demande [%d] n'est autorisée que si l'état est à [%s].";
	public static final String DEPASSEMENT_DROITS_ACQUIS_MSG = "Le dépassement des droits acquis n'est pas autorisé.";
	public static final String INACTIVITE_MSG = "L'agent n'est pas en activité sur cette période.";
	public static final String DEMANDE_DEJA_COUVERTE_MSG = "La demande ne peut être couverte totalement ou partiellement par une autre absence.";
	public static final String MOTIF_OBLIGATOIRE = "Le motif est obligatoire.";
	public static final String DEMANDE_INEXISTANTE = "La demande n'existe pas.";
	public static final String STATUT_AGENT = "L'agent [%d] ne peut pas avoir de repos compensateur. Les repos compensateurs sont pour les contractuels ou les conventions collectives.";

	public static final List<String> ACTIVITE_CODES = Arrays.asList("01", "02", "03", "04", "23", "24", "60", "61",
			"62", "63", "64", "65", "66");

	/**
	 * Processes the data consistency of a set of Pointages being input by a
	 * user. It will check the different business rules in order to make sure
	 * they're consistent
	 */
	@Override
	public void processDataConsistencyDemande(ReturnMessageDto srm, Integer idAgent, Demande demande, Date dateLundi) {

		checkDemandeDejaSaisieSurMemePeriode(srm, demande);
		checkAgentInactivity(srm, idAgent, dateLundi);
	}

	@Override
	public ReturnMessageDto checkDemandeDejaSaisieSurMemePeriode(ReturnMessageDto srm, Demande demande) {

		List<Demande> listDemande = demandeRepository.listeDemandesAgent(null, demande.getIdAgent(), null, null, null);

		for (Demande demandeExistante : listDemande) {

			if ((null == demande.getIdDemande() || (null != demande.getIdDemande() && !demandeExistante.getIdDemande()
					.equals(demande.getIdDemande())))
					&& null != demandeExistante.getLatestEtatDemande()
					&& !RefEtatEnum.REFUSEE.equals(demandeExistante.getLatestEtatDemande().getEtat())
					&& !RefEtatEnum.PROVISOIRE.equals(demandeExistante.getLatestEtatDemande().getEtat())) {

				// date de debut couverte par une autre demande
				if ((demande.getDateDebut().before(demandeExistante.getDateFin()) || demande.getDateDebut().equals(
						demandeExistante.getDateFin()))
						&& (demande.getDateDebut().after(demandeExistante.getDateDebut()) || demande.getDateDebut()
								.equals(demandeExistante.getDateDebut()))

				) {
					logger.warn(String.format(DEMANDE_DEJA_COUVERTE_MSG));
					srm.getErrors().add(DEMANDE_DEJA_COUVERTE_MSG);
					return srm;
				}
				if ((demande.getDateFin().before(demandeExistante.getDateFin()) || demande.getDateFin().equals(
						demandeExistante.getDateFin()))
						&& (demande.getDateFin().after(demandeExistante.getDateDebut()) || demande.getDateFin().equals(
								demandeExistante.getDateDebut()))) {
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

		Agent ag = sirhRepository.getAgent(idAgent);

		Spadmn adm = sirhRepository.getAgentCurrentPosition(ag, dateLundi);

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
	public List<DemandeDto> filtreListDemande(Integer idAgentConnecte, Integer idAgentConcerne,
			List<Demande> listeSansFiltre, List<RefEtat> etats, Date dateDemande) {
		List<DemandeDto> resultListDto = filtreDateAndEtatDemandeFromList(listeSansFiltre, etats, dateDemande);

		if (null != resultListDto && !resultListDto.isEmpty()) {
			resultListDto = filtreDroitOfListeDemandesByDemande(idAgentConnecte, idAgentConcerne, resultListDto);
		}

		return resultListDto;
	}

	protected List<DemandeDto> filtreDroitOfListeDemandesByDemande(Integer idAgentConnecte, Integer idAgentConcerne,
			List<DemandeDto> resultListDto) {

		// si idAgentConnecte == idAgentConcerne, alors nous sommes dans le cas
		// du WS listeDemandesAgent
		// donc inutile de recuperer les droits en bdd
		// le test 1 sera toujours vrai
		List<DroitsAgent> listDroitAgent = new ArrayList<DroitsAgent>();
		if (null != idAgentConnecte && !idAgentConnecte.equals(idAgentConcerne)) {
			listDroitAgent = accessRightsRepository.getListOfAgentsToInputOrApprove(idAgentConnecte, null);
		}

		for (DemandeDto demandeDto : resultListDto) {
			// test 1
			if (demandeDto.getIdAgent().equals(idAgentConnecte)) {
				demandeDto.setAffichageBoutonModifier(demandeDto.getIdRefEtat().equals(
						RefEtatEnum.PROVISOIRE.getCodeEtat())
						|| demandeDto.getIdRefEtat().equals(RefEtatEnum.SAISIE.getCodeEtat()));
				demandeDto.setAffichageBoutonSupprimer(demandeDto.getIdRefEtat().equals(
						RefEtatEnum.PROVISOIRE.getCodeEtat())
						|| demandeDto.getIdRefEtat().equals(RefEtatEnum.SAISIE.getCodeEtat()));
				demandeDto.setAffichageBoutonImprimer(isAfficherBoutonImprimer(demandeDto));

				demandeDto.setAffichageBoutonAnnuler(demandeDto.getIdRefEtat().equals(
						RefEtatEnum.VISEE_FAVORABLE.getCodeEtat())
						|| demandeDto.getIdRefEtat().equals(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat())
						|| demandeDto.getIdRefEtat().equals(RefEtatEnum.APPROUVEE.getCodeEtat()));

				continue;
			}

			for (DroitsAgent droitsAgent : listDroitAgent) {

				if (demandeDto.getIdAgent().equals(droitsAgent.getIdAgent())) {

					for (DroitDroitsAgent dda : droitsAgent.getDroitDroitsAgent()) {

						if (dda.getDroitProfil().getProfil().getLibelle().equals(ProfilEnum.OPERATEUR.toString())) {

							demandeDto.setAffichageBoutonModifier(demandeDto.getIdRefEtat().equals(
									RefEtatEnum.PROVISOIRE.getCodeEtat())
									|| demandeDto.getIdRefEtat().equals(RefEtatEnum.SAISIE.getCodeEtat()));
							demandeDto.setAffichageBoutonSupprimer(demandeDto.getIdRefEtat().equals(
									RefEtatEnum.PROVISOIRE.getCodeEtat())
									|| demandeDto.getIdRefEtat().equals(RefEtatEnum.SAISIE.getCodeEtat()));
							demandeDto.setAffichageBoutonImprimer(isAfficherBoutonImprimer(demandeDto));

							demandeDto.setAffichageBoutonAnnuler(demandeDto.getIdRefEtat().equals(
									RefEtatEnum.VISEE_FAVORABLE.getCodeEtat())
									|| demandeDto.getIdRefEtat().equals(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat())
									|| demandeDto.getIdRefEtat().equals(RefEtatEnum.APPROUVEE.getCodeEtat()));

							continue;
						}
						if (dda.getDroitProfil().getProfil().getLibelle().equals(ProfilEnum.VISEUR.toString())) {

							demandeDto.setModifierVisa(demandeDto.getIdRefEtat().equals(
									RefEtatEnum.SAISIE.getCodeEtat())
									|| demandeDto.getIdRefEtat().equals(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat())
									|| demandeDto.getIdRefEtat().equals(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat()));
							demandeDto.setAffichageVisa(true);
							demandeDto.setAffichageApprobation(true);

							continue;
						}
						if (dda.getDroitProfil().getProfil().getLibelle().equals(ProfilEnum.APPROBATEUR.toString())
								|| dda.getDroitProfil().getProfil().getLibelle()
										.equals(ProfilEnum.DELEGATAIRE.toString())) {

							demandeDto.setAffichageVisa(true);
							demandeDto.setAffichageApprobation(true);
							demandeDto.setModifierApprobation(demandeDto.getIdRefEtat().equals(
									RefEtatEnum.SAISIE.getCodeEtat())
									|| demandeDto.getIdRefEtat().equals(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat())
									|| demandeDto.getIdRefEtat().equals(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat())
									|| demandeDto.getIdRefEtat().equals(RefEtatEnum.APPROUVEE.getCodeEtat())
									|| demandeDto.getIdRefEtat().equals(RefEtatEnum.REFUSEE.getCodeEtat()));

							continue;
						}
					}

					break;
				}
			}
		}

		return resultListDto;
	}

	private boolean isAfficherBoutonImprimer(DemandeDto demandeDto) {
		// a completer au fur et à mesure des developpements
		// TODO
		if (RefTypeAbsenceEnum.RECUP.getValue() == demandeDto.getIdTypeDemande()
				|| RefTypeAbsenceEnum.REPOS_COMP.getValue() == demandeDto.getIdTypeDemande()) {
			return demandeDto.getIdRefEtat().equals(RefEtatEnum.APPROUVEE.getCodeEtat());
		} else {
			return false;
		}

	}

	protected List<DemandeDto> filtreDateAndEtatDemandeFromList(List<Demande> listeSansFiltre, List<RefEtat> etats,
			Date dateDemande) {
		List<DemandeDto> listeDemandeDto = new ArrayList<DemandeDto>();
		if (listeSansFiltre.size() == 0)
			return listeDemandeDto;

		if (dateDemande == null && etats == null) {
			for (Demande d : listeSansFiltre) {
				DemandeDto dto = new DemandeDto(d, sirhRepository.getAgent(d.getIdAgent()));
				listeDemandeDto.add(dto);
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
					DemandeDto dto = new DemandeDto(d, sirhRepository.getAgent(d.getIdAgent()));
					listeDemandeDto.add(dto);
				}
				isfiltreDateDemande = true;
			}
		}

		// ON TRAITE L'ETAT
		if (etats != null) {
			for (Demande d : listeSansFiltre) {
				DemandeDto dto = new DemandeDto(d, sirhRepository.getAgent(d.getIdAgent()));
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

		return listeDemandeDto;
	}
}
