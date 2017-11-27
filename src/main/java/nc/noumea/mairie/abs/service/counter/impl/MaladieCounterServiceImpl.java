package nc.noumea.mairie.abs.service.counter.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeMaladies;
import nc.noumea.mairie.abs.domain.RefDroitsMaladies;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.dto.SoldeEnfantMaladeDto;
import nc.noumea.mairie.abs.dto.SoldeMaladiesDto;
import nc.noumea.mairie.abs.repository.IMaladiesRepository;
import nc.noumea.mairie.abs.service.IAgentMatriculeConverterService;
import nc.noumea.mairie.abs.transformer.DemandeComparator;
import nc.noumea.mairie.abs.vo.CalculDroitsMaladiesVo;
import nc.noumea.mairie.domain.Spadmn;
import nc.noumea.mairie.domain.Spcarr;

@Service("MaladieCounterServiceImpl")
public class MaladieCounterServiceImpl extends AbstractCounterService {

	private static Integer QUOTA_ABSENCES_ANNUEL = 364;

	@Autowired
	protected IAgentMatriculeConverterService agentMatriculeService;

	@Autowired
	protected IMaladiesRepository maladiesRepository;

	@Override
	public SoldeMaladiesDto getSoldeByAgent(Integer idAgent, Date dateFinAnneeGlissante, AgentGeneriqueDto agentDto) {

		logger.info("MaladieCounterServiceImpl getSoldeByAgent : " + idAgent);

		SoldeMaladiesDto dto = new SoldeMaladiesDto();
		
		CalculDroitsMaladiesVo vo = calculDroitsMaladies(idAgent, dateFinAnneeGlissante, null, null, false, false);

		dto.setDroitsDemiSalaire(vo.getDroitsDemiSalaire());
		dto.setDroitsPleinSalaire(vo.getDroitsPleinSalaire());
		dto.setRapDemiSalaire(vo.getNombreJoursResteAPrendreDemiSalaire());
		dto.setRapPleinSalaire(vo.getNombreJoursResteAPrendrePleinSalaire());
		dto.setTotalPris(vo.getTotalPris());

		return dto;
	}

	@Override
	public SoldeEnfantMaladeDto getSoldeEnfantMalade(Integer idAgent) {

		logger.info("MaladieCounterServiceImpl getSoldeEnfantMalade pour l'agent : " + idAgent);

		SoldeEnfantMaladeDto dto = new SoldeEnfantMaladeDto();
		
		// Le solde des enfants malades se calcul sur une année civile.
		// On prend la date du jour comme date de fin, et le 1e Janvier de l'année comme date de début.
		Calendar c = Calendar.getInstance();
		c.setTime(helperService.getCurrentDate());
		Date today = c.getTime();
		c.set(Calendar.MONTH, 0);
		c.set(Calendar.DAY_OF_MONTH, 1);
		Date firstDayOfYear = c.getTime();
		
		List<DemandeMaladies> listMaladiesEnfantSurAnneeCivile = maladiesRepository.getListEnfantMaladeAnneeCivileByAgent(idAgent, firstDayOfYear, today);
		Integer totalPris = getNombeJourMaladies(idAgent, firstDayOfYear, today, listMaladiesEnfantSurAnneeCivile, null);
		
		dto.setTotalPris(totalPris);
		dto.setTotalRestant(SoldeEnfantMaladeDto.QUOTA_ENFANT_MALADE - totalPris);

		return dto;
	}

	@Override
	public CalculDroitsMaladiesVo calculDroitsMaladiesForDemandeMaladies(Integer idAgent, DemandeDto demandeMaladie) {
		return calculDroitsMaladies(idAgent, demandeMaladie.getDateFin(), demandeMaladie.getIdDemande(), demandeMaladie.getDuree(), false, true);
	}

	/**
	 * Calcul les compteurs maladies
	 * 
	 * @param idAgent
	 * @param dateFinAnneeGlissante
	 * @param idDemande
	 * @param duree
	 * @param isCancel : true means that the current demande (idDemande param) will be removed from the list of demandes
	 * @param isRetroactif : true means that the current demande (idDemande param) will be added to the list of demandes, anyway (regardless of its state)
	 * @return
	 */
	protected CalculDroitsMaladiesVo calculDroitsMaladies(Integer idAgent, Date dateFinAnneeGlissante, Integer idDemande, Double duree, boolean isCancel, boolean isRetroactif) {

		logger.info("MaladieCounterServiceImpl calculDroitsMaladiesRetroactivement for agent {}, demande id {}", idAgent, idDemande);

		CalculDroitsMaladiesVo result = new CalculDroitsMaladiesVo();

		// a. calcul periode de reference
		Date dateDebutAnneeGlissante = new DateTime(dateFinAnneeGlissante).minusYears(1).plusDays(1).withMillisOfDay(0).toDate();
		
		// b. on calcul le nombre de jours maladies a l'état 'PRISE' ou 'VALIDEE PAR LA DRH' sur une année glissantes
		List<DemandeMaladies> listMaladies = maladiesRepository.getListMaladiesAnneGlissanteRetroactiveByAgent(idAgent,
						dateDebutAnneeGlissante, dateFinAnneeGlissante, idDemande, isCancel, isRetroactif);

		Integer totalPris = getNombeJourMaladies(idAgent, dateDebutAnneeGlissante, dateFinAnneeGlissante, listMaladies, null);

		Integer nombreJoursMaladiesDemandeEnCours = null != duree ? duree.intValue() : 0;

		// on recupere le statut de l agent on recherche sa carriere pour 
		// avoir son statut (Fonctionnaire, contractuel, convention coll)
		Spcarr carr = sirhRepository.getAgentCurrentCarriere(
				agentMatriculeService.fromIdAgentToSIRHNomatrAgent(idAgent),
				dateFinAnneeGlissante);
		
		// on teste si l agent est en periode d essai si oui, aucun droit pour lui
		// #40519 : Gérer le cas d'un agent sans carrière
		if (carr == null || (!helperService.isFonctionnaire(carr) && isAgentPeriodeEssai(idAgent, dateFinAnneeGlissante))) {
			result.setDroitsDemiSalaire(0);
			result.setDroitsPleinSalaire(0);
			result.setNombreJoursCoupeDemiSalaire(0);
			result.setNombreJoursCoupePleinSalaire(nombreJoursMaladiesDemandeEnCours);
			result.setNombreJoursResteAPrendreDemiSalaire(0);
			result.setNombreJoursResteAPrendrePleinSalaire(0);
			result.setTotalPris(totalPris + nombreJoursMaladiesDemandeEnCours);
			return result;
		}

		// on recupere les droits de l agent
		RefDroitsMaladies droitsMaladies = getDroitsAgent(idAgent, dateFinAnneeGlissante, null, carr);
		
		Integer droitPS = droitsMaladies.getNombreJoursPleinSalaire();
		Integer droitDS = droitsMaladies.getNombreJoursDemiSalaire();

		// CAS PARTICULIER : si nombre de jours MALADIES >= 364 (malade toute une année), 
		// l'agent ne percoit aucun salaire sur la totalite de la maladie posee
		if (totalPris >= QUOTA_ABSENCES_ANNUEL) {
			result.setDroitsDemiSalaire(droitDS);
			result.setDroitsPleinSalaire(droitPS);
			result.setNombreJoursCoupeDemiSalaire(0);
			result.setNombreJoursCoupePleinSalaire(nombreJoursMaladiesDemandeEnCours);
			result.setNombreJoursResteAPrendreDemiSalaire(0);
			result.setNombreJoursResteAPrendrePleinSalaire(0);
			result.setTotalPris(totalPris);
			return result;
		}
		
		// Reste à prendre
		Integer rapPS = droitPS - totalPris;
		if (rapPS < 0)
			rapPS = 0;
		
		Integer joursSansDroitPS = totalPris - droitPS;
		Integer rapDS = droitDS;
		if (joursSansDroitPS >= 0)
			rapDS = droitDS - joursSansDroitPS;
		if (rapDS < 0)
			rapDS = 0;
		// Fin du reste à prendre
		
		// Nombre de jours coupés
		Integer coupeDS = 0;
		Integer coupePS = 0;
		
		// On ne calcul les jours coupés uniquement s'il n'y a plus de reste à prendre plein salaire
		if (rapPS == 0) {
			// Il n'y a que des jours coupés demi-salaire si le total pris moins les droits est négatif
			if (totalPris - droitPS <= droitDS) {
				coupeDS = totalPris - droitPS;
				coupeDS = (duree != null && coupeDS > nombreJoursMaladiesDemandeEnCours) ? nombreJoursMaladiesDemandeEnCours : coupeDS;
			// Sinon il faut aussi calculer les jours coupés plein salaire
			} else {
				if (duree != null) {
					if (totalPris - droitDS - droitPS >= nombreJoursMaladiesDemandeEnCours)
						coupePS = nombreJoursMaladiesDemandeEnCours;
					else {
						coupePS = (totalPris - droitDS - droitPS);
						coupeDS = nombreJoursMaladiesDemandeEnCours - coupePS;
						if (coupeDS > droitDS)
							coupeDS = droitDS;
					}
				} else {
					coupePS = totalPris - droitDS - droitPS;
				}
			}
		}

		result.setDroitsDemiSalaire(droitDS);
		result.setDroitsPleinSalaire(droitPS);
		result.setNombreJoursCoupeDemiSalaire(coupeDS);
		result.setNombreJoursCoupePleinSalaire(coupePS);
		result.setNombreJoursResteAPrendreDemiSalaire(rapDS);
		result.setNombreJoursResteAPrendrePleinSalaire(rapPS);
		result.setTotalPris(totalPris);

		return result;
	}

	@Override
	public Integer getNombeJourMaladies(Integer idAgent, Date dateDebutAnneeGlissante, Date dateFinAnneeGlissante, List<DemandeMaladies> listMaladies, Integer idDemande) {

		Integer result = 0;

		if (null != listMaladies && !listMaladies.isEmpty()) {
			for (DemandeMaladies demande : listMaladies) {
				if (demande.getDateFin().after(dateDebutAnneeGlissante) && (demande.getIdDemande() == null 
						|| (demande.getIdDemande() != null && !demande.getIdDemande().equals(idDemande)))) {
					if (demande.getDateDebut().before(dateDebutAnneeGlissante)) {
						Date dateFin = new DateTime(demande.getDateFin())
								.withMillisOfDay(0).plusDays(1).toDate();
						Duration period = new Duration(
								dateDebutAnneeGlissante.getTime(),
								dateFin.getTime());
						result += new Long(period.getStandardDays()).intValue();
					} else {
						result += demande.getDuree().intValue();
					}
				}
			}
		}

		logger.debug("MaladieCounterServiceImpl getNombeJourMaladies for Agent "
				+ idAgent + ": " + result);

		return result;
	}

	protected Integer getNombeJourMaladiesCoupesDemiSalaire(Integer idAgent,
			Date dateDebutAnneeGlissante, Date dateFinAnneeGlissante,
			List<DemandeMaladies> listMaladies, Integer idDemande) {

		Integer result = 0;

		if (null != listMaladies && !listMaladies.isEmpty()) {
			for (DemandeMaladies demande : listMaladies) {
				if (demande.getDateFin().after(dateDebutAnneeGlissante) && (demande.getIdDemande() == null 
						|| (demande.getIdDemande() != null && !demande.getIdDemande().equals(idDemande)))) {
					result += null != demande.getNombreJoursCoupeDemiSalaire() ? demande
							.getNombreJoursCoupeDemiSalaire() : 0;
				}
			}
		}

		logger.debug("MaladieCounterServiceImpl getNombeJourMaladiesCoupesDemiSalaire for Agent "
				+ idAgent + ": " + result);

		return result;
	}

	protected Integer getNombeJourMaladiesCoupesPleinSalaire(Integer idAgent,
			Date dateDebutAnneeGlissante, Date dateFinAnneeGlissante,
			List<DemandeMaladies> listMaladies, Integer idDemande) {

		Integer result = 0;

		if (null != listMaladies && !listMaladies.isEmpty()) {
			for (DemandeMaladies demande : listMaladies) {
				if (demande.getDateFin().after(dateDebutAnneeGlissante) && (demande.getIdDemande() == null 
						|| (demande.getIdDemande() != null && !demande.getIdDemande().equals(idDemande)))) {
					result += null != demande.getNombreJoursCoupePleinSalaire() ? demande
							.getNombreJoursCoupePleinSalaire() : 0;
				}
			}
		}

		logger.debug("MaladieCounterServiceImpl getNombeJourMaladiesCoupesPleinSalaire for Agent "
				+ idAgent + ": " + result);

		return result;
	}

	private RefDroitsMaladies getDroitsAgent(Integer idAgent, Date date,
			AgentGeneriqueDto agentDto, Spcarr carr) {

		// on recupere son anciennete
		Integer anneeAnciennete = getNombreAnneeAnciennete(idAgent, date, agentDto);

		// on recupere ses droits
		return maladiesRepository.getDroitsMaladies(
				helperService.isFonctionnaire(carr),
				helperService.isContractuel(carr),
				helperService.isConventionCollective(carr), anneeAnciennete);
	}

	/**
	 * Calcul de l anciennete : N = date Fin Maladie - date derniere embauche +1
	 * - nombre jours en PA50
	 * 
	 * @param idAgent
	 *            Integer
	 * @param date
	 *            Date derniere embauche
	 * @param agentDto
	 *            AgentGeneriqueDto peut etre NULL
	 * @return Integer nombre annee arrondi a l entier inferieur
	 */
	protected Integer getNombreAnneeAnciennete(Integer idAgent, Date date,
			AgentGeneriqueDto agentDto) {

		// on recupere la date de derniere embauche de l agent
		if (null == agentDto || null == agentDto.getDateDerniereEmbauche()) {
			agentDto = sirhWSConsumer.getAgent(idAgent);
		}

		// on recupere ses PA50
		List<Spadmn> listPA50 = sirhRepository.getPA50OfAgent(
				agentDto.getNomatr(), agentDto.getDateDerniereEmbauche());

		// date Fin Maladie - date derniere embauche +1 jour
		Duration duree = new Duration(agentDto.getDateDerniereEmbauche()
				.getTime(), new DateTime(date).plusDays(1).toDate().getTime());

		Duration dureeTotalePA50 = new Duration(agentDto
				.getDateDerniereEmbauche().getTime(), agentDto
				.getDateDerniereEmbauche().getTime());
		// calcul du nombre de jour en PA50
		if (null != listPA50 && !listPA50.isEmpty()) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			for (Spadmn pa : listPA50) {
				if (pa.getCdpadm().equals("50")) {
					try {
						Date dateDebutPA = sdf.parse(pa.getId().getDatdeb()
								.toString());
						Date dateFinPA = sdf.parse(pa.getDatfin().toString());

						Duration dureePA50 = new Duration(
								dateDebutPA.getTime(), dateFinPA.getTime());

						dureeTotalePA50 = dureeTotalePA50.plus(dureePA50);
					} catch (ParseException e) {
						logger.warn(e.getMessage());
						return null;
					}
				}
			}
		}
		// - nombre de jours PA50
		duree = duree.minus(dureeTotalePA50);
		// ceci est une bidouille a mon sens
		// car si on fait "duree" peut parfois prendre la valeur +4Y-15M
		// ca retourne 4 ans au lieu de 2ans9mois
		// voir TU getNombreAnneeAnciennete_2_PA50_4ans8mois
		DateTime dateTemoin = new DateTime();
		Period period = new Period(dateTemoin, dateTemoin.plus(duree));

		logger.debug("MaladieCounterServiceImpl getNombreAnneeAnciennete for Agent "
				+ idAgent + ": " + period.getYears());

		return period.getYears();
	}

	/**
	 * Retourne si l agent est en periode d essai
	 * 
	 * @param idAgent
	 *            Integer
	 * @param date
	 *            Date
	 * @return boolean
	 */
	protected boolean isAgentPeriodeEssai(Integer idAgent, Date date) {
		return sirhWSConsumer.isPeriodeEssai(idAgent, date);
	}

	/**
	 * Retourne l'historique des maladies avec leur calcul de droits respectif
	 */
	@Override
	public List<DemandeMaladies> getHistoriqueMaladiesWithDroits(
			Integer idAgent, Date date) {

		return maladiesRepository.getListMaladiesAnneGlissanteByAgent(idAgent,
				null, date);
	}

	@Override
	@Transactional
	public ReturnMessageDto majCompteurToAgent(ReturnMessageDto srm,
			Demande demande, DemandeEtatChangeDto demandeEtatChangeDto) {
		
		boolean isCancel = false;
		// Si c'est une annulation de demande, on met à zéro les compteurs de cette demande.
		if (demandeEtatChangeDto.getIdRefEtat() != null &&
				demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.ANNULEE.getCodeEtat())) {
			updateDemandeWithNewSolde((DemandeMaladies) demande,
					null, null,
					null, null, null);
			// Pour une annulation, il ne faut pas que la demande soit prise en compte dans le recalcul des soldes.
			isCancel = true;
		} else {			
			CalculDroitsMaladiesVo vo = calculDroitsMaladies(demande.getIdAgent(),
					demande.getDateFin(), demande.getIdDemande(), ((DemandeMaladies) (demande)).getDuree(), isCancel, true);
	
			updateDemandeWithNewSolde((DemandeMaladies) demande,
					vo.getTotalPris(), vo.getNombreJoursCoupeDemiSalaire(),
					vo.getNombreJoursCoupePleinSalaire(),
					vo.getNombreJoursResteAPrendreDemiSalaire(),
					vo.getNombreJoursResteAPrendrePleinSalaire());
		}

		// #43076 : Mise à jour rétroactive des soldes des maladies futures
		List<DemandeMaladies> listDemandesFutures = maladiesRepository.getListMaladiesFuturesForDemande(demande.getIdAgent(), demande.getDateDebut());
		// Les soldes des demandes doivent être mises à jour par ordre chronologique sur les dates de début !
		listDemandesFutures.sort(new DemandeComparator());
		
		if (listDemandesFutures != null && !listDemandesFutures.isEmpty()) {
			for(DemandeMaladies mal : listDemandesFutures) {
				logger.debug("Mise à jour du solde de la demande {}, pour l'agent matricule {}", mal.getIdDemande(), demande.getIdAgent());
				CalculDroitsMaladiesVo dm = calculDroitsMaladies(mal.getIdAgent(),
						mal.getDateFin(), demande.getIdDemande(), mal.getDuree(), isCancel, true);

				updateDemandeWithNewSolde((DemandeMaladies) mal,
						dm.getTotalPris(), dm.getNombreJoursCoupeDemiSalaire(),
						dm.getNombreJoursCoupePleinSalaire(),
						dm.getNombreJoursResteAPrendreDemiSalaire(),
						dm.getNombreJoursResteAPrendrePleinSalaire());
			}
		}

		return srm;
	}

	/**
	 * #31488 maj solde sur la demande
	 * 
	 * @param demande
	 *            demande a mettre a jour
	 * @param totalPris
	 *            totalPris en jours
	 * @param nombreJoursCoupeDemiSalaire
	 *            nombre de jour coupé en demi salaire
	 * @param nombreJoursCoupePleinSalaire
	 *            nombre de jour coupé en plein salaire
	 * @param nombreJoursResteAPrendreDemiSalaire
	 *            nombre de jour restant à prendre en demi salaire
	 * @param nombreJoursResteAPrendrePleinSalaire
	 *            nombre de jour restant à prendre en plein salaire
	 */
	protected void updateDemandeWithNewSolde(DemandeMaladies demande,
			Integer totalPris, Integer nombreJoursCoupeDemiSalaire,
			Integer nombreJoursCoupePleinSalaire,
			Integer nombreJoursResteAPrendreDemiSalaire,
			Integer nombreJoursResteAPrendrePleinSalaire) {
		demande.setTotalPris(totalPris);
		demande.setNombreJoursCoupeDemiSalaire(nombreJoursCoupeDemiSalaire);
		demande.setNombreJoursCoupePleinSalaire(nombreJoursCoupePleinSalaire);
		demande.setNombreJoursResteAPrendreDemiSalaire(nombreJoursResteAPrendreDemiSalaire);
		demande.setNombreJoursResteAPrendrePleinSalaire(nombreJoursResteAPrendrePleinSalaire);
	}

	@Override
	@Transactional
	public boolean updateSoldesMaladies(Integer idAgent) {
		List<Integer> listIdAgentsActifs = Lists.newArrayList();
		// 1 : Récupérer la liste des agents actifs
		if (idAgent == null)
			listIdAgentsActifs = sirhWSConsumer.getListAgentActiviteAnnuaire();
		else
			listIdAgentsActifs.add(idAgent);
		
		for (Integer matrAgent : listIdAgentsActifs) {
			// 2 : Récupérer la liste des maladies à l'état 'Prise' et 'Validée par la DRH' de chacun des agent depuis le 1e janvier 2016
			List<DemandeMaladies> listDemandes =  maladiesRepository.getListMaladiesAnneGlissanteByAgent(matrAgent, new DateTime(2016, 1, 1, 0, 0, 0).toDate(), new Date());
			listDemandes.sort(new DemandeComparator());
			for (DemandeMaladies maladie : listDemandes) {
				// 3 : Mettre à jour les soldes
				CalculDroitsMaladiesVo dm = calculDroitsMaladies(maladie.getIdAgent(),
						maladie.getDateFin(), null, maladie.getDuree(), false, false);
	
				updateDemandeWithNewSolde((DemandeMaladies) maladie,
						dm.getTotalPris(), dm.getNombreJoursCoupeDemiSalaire(),
						dm.getNombreJoursCoupePleinSalaire(),
						dm.getNombreJoursResteAPrendreDemiSalaire(),
						dm.getNombreJoursResteAPrendrePleinSalaire());
			}
			logger.info("Soldes mis à jour pour l'agent matricule {}", matrAgent);
		}
		
		return true;
	}
}
