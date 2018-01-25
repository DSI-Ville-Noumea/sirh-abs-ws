package nc.noumea.mairie.abs.service.impl;

import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import nc.noumea.mairie.abs.domain.DemandeCongesAnnuels;
import nc.noumea.mairie.abs.domain.DemandeMaladies;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.domain.RefTypeSaisi;
import nc.noumea.mairie.abs.domain.RefTypeSaisiCongeAnnuel;
import nc.noumea.mairie.abs.domain.RefUnitePeriodeQuota;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.JourDto;
import nc.noumea.mairie.abs.repository.IDemandeRepository;
import nc.noumea.mairie.domain.Spcarr;
import nc.noumea.mairie.domain.TypeChainePaieEnum;
import nc.noumea.mairie.ws.ISirhWSConsumer;

@Service
public class HelperService {

	@Autowired
	private ISirhWSConsumer		sirhWSConsumer;

	@Autowired
	private IDemandeRepository	demandeRepository;

	private static int			HEURE_JOUR_DEBUT_AM						= 0;
	private static int			HEURE_JOUR_FIN_AM						= 11;
	private static int			HEURE_JOUR_DEBUT_PM						= 12;
	private static int			HEURE_JOUR_FIN_PM						= 23;
	private static int			MINUTES_JOUR_FIN						= 59;
	private static int			MINUTES_JOUR_DEBUT						= 0;
	private static int			SECONDS_DEBUT							= 0;
	private static int			SECONDS_FIN								= 59;
	private static int			MILLISECONDS							= 0;

	private final static long	MILLISECONDS_PER_DAY					= 1000 * 60 * 60 * 24;
	private final static long	MILLISECONDS_PER_MINUTES				= 1000 * 60;

	public static String		UNITE_DECOMPTE_AN						= "an";
	public static String		UNITE_DECOMPTE_MOIS						= "mois";
	public static String		UNITE_DECOMPTE_JOURS					= "jours";
	public static String		UNITE_DECOMPTE_MINUTES					= "minutes";

	private static int			NOMBRE_SAMEDI_OFFERT_PAR_AN_PAR_AGENT	= 1;

	public Date getCurrentDate() {
		return new Date();
	}

	public boolean isDateAMonday(Date dateMonday) {
		return new LocalDate(dateMonday).getDayOfWeek() == DateTimeConstants.MONDAY;
	}

	public Date getDateFin(RefTypeSaisi typeSaisi, Date dateFin, Date dateDeb, Double duree, boolean dateFinAM, boolean dateFinPM) {

		if (typeSaisi.isCalendarDateFin() && typeSaisi.isCalendarHeureFin() && !typeSaisi.isChkDateFin()) {
			return dateFin;
		}
		if (typeSaisi.isCalendarDateFin() && !typeSaisi.isCalendarHeureFin() && typeSaisi.isChkDateFin()) {

			if (dateFinAM && !dateFinPM) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(dateFin);
				cal.set(Calendar.HOUR_OF_DAY, HEURE_JOUR_FIN_AM);
				cal.set(Calendar.MINUTE, MINUTES_JOUR_FIN);
				cal.set(Calendar.SECOND, SECONDS_FIN);
				cal.set(Calendar.MILLISECOND, MILLISECONDS);
				return cal.getTime();
			}
			if (dateFinPM) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(dateFin);
				cal.set(Calendar.HOUR_OF_DAY, HEURE_JOUR_FIN_PM);
				cal.set(Calendar.MINUTE, MINUTES_JOUR_FIN);
				cal.set(Calendar.SECOND, SECONDS_FIN);
				cal.set(Calendar.MILLISECOND, MILLISECONDS);
				return cal.getTime();
			}
		}
		if (!typeSaisi.isCalendarDateFin() && typeSaisi.isDuree()) {
			if (typeSaisi.getUniteDecompte() != null && typeSaisi.getUniteDecompte().equals(UNITE_DECOMPTE_MINUTES)) {
				DateTime recupDateFin = new DateTime(dateDeb);
				String durEntier = duree.toString().substring(0, duree.toString().indexOf("."));
				String durDecimal = duree.toString().substring(duree.toString().indexOf(".") + 1, duree.toString().length());
				Double heure = new Double(durEntier) * 60;
				Double minute = durDecimal.substring(0, 1).equals("0") ? new Double(durDecimal)
						: durDecimal.length() == 1 ? 10.0 * new Double(durDecimal) : new Double(durDecimal);
				Double dur = heure + minute;
				return recupDateFin.plusMinutes(dur.intValue()).toDate();
			}
			DateTime recupDateFin = new DateTime(dateDeb);
			return recupDateFin.plusMinutes(duree.intValue()).toDate();

		}
		if (typeSaisi.isCalendarDateFin() && !typeSaisi.isCalendarHeureFin() && !typeSaisi.isChkDateFin()) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(dateFin);
			cal.set(Calendar.HOUR_OF_DAY, HEURE_JOUR_FIN_PM);
			cal.set(Calendar.MINUTE, MINUTES_JOUR_FIN);
			cal.set(Calendar.SECOND, SECONDS_FIN);
			cal.set(Calendar.MILLISECOND, MILLISECONDS);
			return cal.getTime();
		}
		if (!typeSaisi.isCalendarDateFin() && !typeSaisi.isCalendarHeureFin() && !typeSaisi.isChkDateFin() && !typeSaisi.isDuree()) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(dateDeb);
			cal.set(Calendar.HOUR_OF_DAY, HEURE_JOUR_FIN_PM);
			cal.set(Calendar.MINUTE, MINUTES_JOUR_FIN);
			cal.set(Calendar.SECOND, SECONDS_FIN);
			cal.set(Calendar.MILLISECOND, MILLISECONDS);
			return cal.getTime();
		}

		return null;
	}

	public Date getDateDebut(RefTypeSaisi typeSaisi, Date dateDeb, boolean dateDebutAM, boolean dateDebutPM) {

		if (typeSaisi.isCalendarDateDebut() && typeSaisi.isCalendarHeureDebut() && !typeSaisi.isChkDateDebut()) {
			return dateDeb;
		}
		if (typeSaisi.isCalendarDateDebut() && !typeSaisi.isCalendarHeureDebut() && typeSaisi.isChkDateDebut()) {

			if (dateDebutAM && !dateDebutPM) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(dateDeb);
				cal.set(Calendar.HOUR_OF_DAY, HEURE_JOUR_DEBUT_AM);
				cal.set(Calendar.MINUTE, MINUTES_JOUR_DEBUT);
				cal.set(Calendar.SECOND, SECONDS_DEBUT);
				cal.set(Calendar.MILLISECOND, MILLISECONDS);
				return cal.getTime();
			}
			if (dateDebutPM) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(dateDeb);
				cal.set(Calendar.HOUR_OF_DAY, HEURE_JOUR_DEBUT_PM);
				cal.set(Calendar.MINUTE, MINUTES_JOUR_DEBUT);
				cal.set(Calendar.SECOND, SECONDS_DEBUT);
				cal.set(Calendar.MILLISECOND, MILLISECONDS);
				return cal.getTime();
			}
		}

		if (typeSaisi.isCalendarDateDebut() && !typeSaisi.isCalendarHeureDebut() && !typeSaisi.isChkDateDebut()) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(dateDeb);
			cal.set(Calendar.HOUR_OF_DAY, HEURE_JOUR_DEBUT_AM);
			cal.set(Calendar.MINUTE, MINUTES_JOUR_DEBUT);
			cal.set(Calendar.SECOND, SECONDS_DEBUT);
			cal.set(Calendar.MILLISECOND, MILLISECONDS);
			return cal.getTime();
		}
		return null;
	}

	public Double getDuree(RefTypeSaisi typeSaisi, Date dateDebut, Date dateFin, Double duree) {

		if (typeSaisi.isCalendarDateFin()) {
			if (UNITE_DECOMPTE_JOURS.equals(typeSaisi.getUniteDecompte())) {
				Double dureeCalculee = calculNombreJoursArrondiDemiJournee(dateDebut, dateFin);
				// #30248 : pour CCSP ou congé unique on ne decompte pas les
				// jours fériés/dimanche...
				if (typeSaisi.getType() == null) {
					List<JourDto> listJoursFeries = sirhWSConsumer.getListeJoursFeries(dateDebut, dateFin);
					dureeCalculee -= calculJoursNonComptesDimancheFerieChome(dateDebut, dateFin, listJoursFeries);
				} else {
					if (!String.valueOf(RefTypeAbsenceEnum.CE_CONGE_UNIQUE.getValue()).equals(typeSaisi.getType().getIdRefTypeAbsence().toString())
							&& !String.valueOf(RefTypeAbsenceEnum.CE_CONGE_UNIQUE_CCSP.getValue())
									.equals(typeSaisi.getType().getIdRefTypeAbsence().toString())) {
						List<JourDto> listJoursFeries = sirhWSConsumer.getListeJoursFeries(dateDebut, dateFin);
						dureeCalculee -= calculJoursNonComptesDimancheFerieChome(dateDebut, dateFin, listJoursFeries);
					}
				}

				return dureeCalculee;
			}
			if (UNITE_DECOMPTE_MINUTES.equals(typeSaisi.getUniteDecompte())) {
				return new Double(calculNombreMinutes(dateDebut, dateFin));
			}
		}

		if (!typeSaisi.isCalendarDateFin() && typeSaisi.isDuree()) {
			if (UNITE_DECOMPTE_MINUTES.equals(typeSaisi.getUniteDecompte())) {
				String durEntier = duree.toString().substring(0, duree.toString().indexOf("."));
				String durDecimal = duree.toString().substring(duree.toString().indexOf(".") + 1, duree.toString().length());
				Double heure = new Double(durEntier) * 60;
				Double minute = durDecimal.length() == 1 ? new Double(durDecimal) * 10 : new Double(durDecimal);

				Double dur = heure + minute;
				return dur;

			}
			return duree;
		}
		if (!typeSaisi.isCalendarDateFin()) {
			if (UNITE_DECOMPTE_JOURS.equals(typeSaisi.getUniteDecompte())) {
				return calculNombreJoursArrondiDemiJournee(dateDebut, dateFin);
			}
		}

		return 0.0;
	}

	public Double calculAlimManuelleCompteur(CompteurDto compteurDto) {

		Double minutes = 0.0;
		if (null != compteurDto.getDureeAAjouter()) {
			minutes = compteurDto.getDureeAAjouter();
		}
		if (null != compteurDto.getDureeARetrancher()) {
			minutes = 0 - compteurDto.getDureeARetrancher();
		}
		return minutes;
	}

	public Date getCurrentDateMoinsUnAn() {
		GregorianCalendar calStr1 = new GregorianCalendar();
		calStr1.setTime(new Date());
		calStr1.add(GregorianCalendar.YEAR, -1);
		return calStr1.getTime();
	}

	public Date getCurrentDateMoinsXMois(int mois) {
		GregorianCalendar calStr1 = new GregorianCalendar();
		calStr1.setTime(new Date());
		calStr1.add(GregorianCalendar.MONTH, -mois);
		return calStr1.getTime();
	}

	public double calculNombreJoursArrondiDemiJournee(Date dateDebut, Date dateFin) {

		double diff = dateFin.getTime() - dateDebut.getTime();
		// calcul nombre jour
		double nbrJour = diff / MILLISECONDS_PER_DAY;
		// arrondi a 0.5
		return ((double) Math.round(nbrJour * 2) / 2);
	}

	public double calculNombreJours(Date dateDebut, Date dateFin) {

		double diff = dateFin.getTime() - dateDebut.getTime();
		// calcul nombre jour
		double nbrJour = diff / MILLISECONDS_PER_DAY;
		return Math.ceil(nbrJour);
	}

	public double calculNombreJoursITT(DemandeMaladies demande) {
		return calculNombreJoursITT(new DemandeDto(demande, true));
	}

	public double calculNombreJoursITT(DemandeDto demande) {
		Long nbITT = null;
	    
		// #41504 : Si la case "Sans arrêt de travail" est cochée, alors le nombre d'ITT doit être 0
		if (demande.isSansArretTravail())
			return 0;
		
		switch (RefTypeAbsenceEnum.getRefTypeAbsenceEnum(demande.getTypeSaisi().getIdRefTypeDemande())) {
			case MALADIE_AT :
				nbITT = ChronoUnit.DAYS.between(demande.getDateDebut().toInstant(), demande.getDateFin().toInstant());
				// #40134 : Une prolongation fonctionne comme une rechute
				// Il faut donc ajouter une journée supplémentaire.
				if (demande.isProlongation())
					++nbITT;
				break;
			case MALADIE_AT_RECHUTE :
				nbITT = ChronoUnit.DAYS.between(demande.getDateDebut().toInstant(), demande.getDateFin().toInstant()) + 1;
				break;
			default:
				break;
		}
		
		// On n'autorise pas un nombre de jour négatif (dans le cas d'un AT sur une journée)
		if (nbITT != null) {
			nbITT = nbITT > 0 ? nbITT : 0;
		}
		return nbITT;
	}

	public int calculNombreMinutes(Date dateDebut, Date dateFin) {

		long diff = dateFin.getTime() - dateDebut.getTime();
		// calcul nombre minutes
		int nbrMinutes = (int) (diff / MILLISECONDS_PER_MINUTES);
		return nbrMinutes;
	}

	public Date getDateDebutMoisForOneDate(Date dateDemande) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dateDemande);
		int minDay = calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH) + 1;
		int year = calendar.get(Calendar.YEAR);
		// on recupere le 1er jour du mois de la demande
		return new DateTime(year, month, minDay, 0, 0, 0).toDate();
	}

	public Date getDateDebutMoisForOneDate(Date dateDemande, int nombreMois) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dateDemande);
		int minDay = calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH) + 1 - (nombreMois - 1);
		int year = calendar.get(Calendar.YEAR);
		// on recupere le 1er jour du mois de la demande
		return new DateTime(year, month, minDay, 0, 0, 0).toDate();
	}

	public Date getDateDebutAnneeForOneDate(Date dateDemande, int nombreAnnees) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dateDemande);
		int minDay = calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
		int month = calendar.getActualMinimum(Calendar.MONTH) + 1;
		int year = calendar.get(Calendar.YEAR) - (nombreAnnees - 1);
		// on recupere le 1er jour du mois de la demande
		return new DateTime(year, month, minDay, 0, 0, 0).toDate();
	}

	public Date getDateFinAnneeForOneDate(Date dateDemande, int nombreAnnees) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dateDemande);
		int minDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		int month = calendar.getActualMaximum(Calendar.MONTH) + 1;
		int year = calendar.get(Calendar.YEAR) + (nombreAnnees - 1);
		return new DateTime(year, month, minDay, 23, 59, 59).toDate();
	}

	public Date getDateDebutJourneeForOneDate(Date dateDemande, int nombreJours) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dateDemande);
		int minDay = calendar.get(Calendar.DAY_OF_MONTH) - (nombreJours - 1);
		int month = calendar.get(Calendar.MONTH) + 1;
		int year = calendar.get(Calendar.YEAR);
		// on recupere le 1er jour du mois de la demande
		return new DateTime(year, month, minDay, 0, 0, 0).toDate();
	}

	public Date getDateFinMoisForOneDate(Date dateDemande) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dateDemande);
		int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH) + 1;
		int year = calendar.get(Calendar.YEAR);
		// on recupere le dernier jour du mois de la demande
		return new DateTime(year, month, maxDay, 23, 59, 59).toDate();
	}

	public boolean isFonctionnaire(Spcarr carr) {
		return (carr.getCdcate() != 4 && carr.getCdcate() != 7);
	}

	public boolean isContractuel(Spcarr carr) {
		return (carr.getCdcate() == 4);
	}

	public boolean isConventionCollective(Spcarr carr) {
		return (carr.getCdcate() == 7);
	}

	public boolean isAgentEligibleCongeAnnuel(Spcarr carr) {
		return !(carr.getCdcate() == 9 || carr.getCdcate() == 10 || carr.getCdcate() == 11);
	}

	public Date getDateDebutByUnitePeriodeQuotaAndDebutDemande(RefUnitePeriodeQuota upq, Date dateDebutDemande) {

		if (UNITE_DECOMPTE_AN.equals(upq.getUnite())) {
			if (upq.isGlissant()) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(dateDebutDemande);
				calendar.add(Calendar.YEAR, -upq.getValeur());
				return calendar.getTime();
			} else {
				return getDateDebutAnneeForOneDate(dateDebutDemande, upq.getValeur());
			}
		}
		if (UNITE_DECOMPTE_MOIS.equals(upq.getUnite())) {
			if (upq.isGlissant()) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(dateDebutDemande);
				calendar.add(Calendar.MONTH, -upq.getValeur());
				return calendar.getTime();
			} else {
				return getDateDebutMoisForOneDate(dateDebutDemande, upq.getValeur());
			}
		}
		if (UNITE_DECOMPTE_JOURS.equals(upq.getUnite())) {
			if (upq.isGlissant()) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(dateDebutDemande);
				calendar.add(Calendar.DAY_OF_YEAR, -upq.getValeur());
				return calendar.getTime();
			} else {
				return getDateDebutJourneeForOneDate(dateDebutDemande, upq.getValeur());
			}
		}

		return null;
	}

	public Date getDateDebutCongeAnnuel(RefTypeSaisiCongeAnnuel refTypeSaisiCongeAnnuel, Date dateDebut, boolean dateDebutAM, boolean dateDebutPM) {

		if (refTypeSaisiCongeAnnuel.isChkDateDebut()) {
			if (dateDebutAM && !dateDebutPM) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(dateDebut);
				cal.set(Calendar.HOUR_OF_DAY, HEURE_JOUR_DEBUT_AM);
				cal.set(Calendar.MINUTE, MINUTES_JOUR_DEBUT);
				cal.set(Calendar.SECOND, SECONDS_DEBUT);
				cal.set(Calendar.MILLISECOND, MILLISECONDS);
				return cal.getTime();
			} else if (dateDebutPM) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(dateDebut);
				cal.set(Calendar.HOUR_OF_DAY, HEURE_JOUR_DEBUT_PM);
				cal.set(Calendar.MINUTE, MINUTES_JOUR_DEBUT);
				cal.set(Calendar.SECOND, SECONDS_DEBUT);
				cal.set(Calendar.MILLISECOND, MILLISECONDS);
				return cal.getTime();
			} else {
				return dateDebut;
			}
		} else if (!refTypeSaisiCongeAnnuel.isChkDateDebut()) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(dateDebut);
			cal.set(Calendar.HOUR_OF_DAY, HEURE_JOUR_DEBUT_AM);
			cal.set(Calendar.MINUTE, MINUTES_JOUR_DEBUT);
			cal.set(Calendar.SECOND, SECONDS_DEBUT);
			cal.set(Calendar.MILLISECOND, MILLISECONDS);
			return cal.getTime();
		}
		return null;
	}

	public Date getDateFinCongeAnnuel(RefTypeSaisiCongeAnnuel refTypeSaisiCongeAnnuel, Date dateFin, Date dateDebut, boolean dateFinAM,
			boolean dateFinPM, Date dateReprise) {

		if (refTypeSaisiCongeAnnuel.isCalendarDateFin() && !refTypeSaisiCongeAnnuel.isChkDateFin()) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(dateFin);
			cal.set(Calendar.HOUR_OF_DAY, HEURE_JOUR_FIN_PM);
			cal.set(Calendar.MINUTE, MINUTES_JOUR_FIN);
			cal.set(Calendar.SECOND, SECONDS_FIN);
			cal.set(Calendar.MILLISECOND, MILLISECONDS);
			return cal.getTime();
		} else if (refTypeSaisiCongeAnnuel.isCalendarDateFin() && refTypeSaisiCongeAnnuel.isChkDateFin()) {
			if (dateFinAM && !dateFinPM) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(dateFin);
				cal.set(Calendar.HOUR_OF_DAY, HEURE_JOUR_FIN_AM);
				cal.set(Calendar.MINUTE, MINUTES_JOUR_FIN);
				cal.set(Calendar.SECOND, SECONDS_FIN);
				cal.set(Calendar.MILLISECOND, MILLISECONDS);
				return cal.getTime();
			} else if (dateFinPM) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(dateFin);
				cal.set(Calendar.HOUR_OF_DAY, HEURE_JOUR_FIN_PM);
				cal.set(Calendar.MINUTE, MINUTES_JOUR_FIN);
				cal.set(Calendar.SECOND, SECONDS_FIN);
				cal.set(Calendar.MILLISECOND, MILLISECONDS);
				return cal.getTime();
			} else {
				return dateFin;
			}
		} else if (refTypeSaisiCongeAnnuel.isCalendarDateReprise()) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(dateReprise);
			cal.add(Calendar.DAY_OF_MONTH, -1);
			cal.set(Calendar.HOUR_OF_DAY, HEURE_JOUR_FIN_PM);
			cal.set(Calendar.MINUTE, MINUTES_JOUR_FIN);
			cal.set(Calendar.SECOND, SECONDS_FIN);
			cal.set(Calendar.MILLISECOND, MILLISECONDS);
			return cal.getTime();
		}

		return null;
	}

	public Double getDureeCongeAnnuel(DemandeCongesAnnuels demande, Date dateReprise, boolean forcerSaisieManuelleDuree, Double dureeSaisieManuelle) {
		Double duree = 0.0;
		switch (demande.getTypeSaisiCongeAnnuel().getCodeBaseHoraireAbsence()) {
			case "A":
			case "D":
			case "S":

				List<JourDto> listJoursFeries = sirhWSConsumer.getListeJoursFeries(demande.getDateDebut(), demande.getDateFin());

				duree = calculNombreJoursArrondiDemiJournee(demande.getDateDebut(), demande.getDateFin())
						- calculJoursNonComptesDimancheFerieChome(demande.getDateDebut(), demande.getDateFin(), listJoursFeries)
						- getNombreJourSemaineWithoutFerie(demande.getDateDebut(), demande.getDateFin(), DateTimeConstants.SATURDAY, listJoursFeries)
						// on retire le nombre de samedi
						+ getNombreSamediDecompte(demande, listJoursFeries) - getNombreSamediOffert(demande, listJoursFeries);
				// puis on calcule le nombre de samedi decompte selon les RG
				break;

			case "E":
			case "F":
				duree = calculNombreJours(demande.getDateDebut(), demande.getDateFin());
				break;

			case "C":

				if (forcerSaisieManuelleDuree)
					return dureeSaisieManuelle;

				duree = calculNombreJours(demande.getDateDebut(), dateReprise);
				duree = Math
						.ceil((duree / demande.getTypeSaisiCongeAnnuel().getQuotaMultiple()) * demande.getTypeSaisiCongeAnnuel().getQuotaDecompte());
				if (duree < demande.getTypeSaisiCongeAnnuel().getQuotaDecompte()) {
					duree = calculNombreJours(demande.getDateDebut(), dateReprise);
				} else if (duree > demande.getTypeSaisiCongeAnnuel().getQuotaDecompte()
						&& duree <= demande.getTypeSaisiCongeAnnuel().getQuotaMultiple()) {
					duree = 3.0;
				}
				break;

			default:
				break;
		}

		return duree;
	}

	private double calculJoursNonComptesDimancheFerieChome(Date dateDebut, Date dateFin, List<JourDto> listJoursFeries) {
		Double res = 0.0;
		// on compte le nombre de dimanches entre les 2 dates
		res += getNombreDimanche(dateDebut, dateFin);
		// on compte le nombre de jours fériés ou chomes entre les 2 dates
		res += getNombreJoursFeriesChomes(dateDebut, dateFin, listJoursFeries);
		return res;

	}

	protected Double getNombreJoursFeriesChomes(Date dateDebut, Date dateFin, List<JourDto> listJoursFeries) {
		int compteur = 0;

		Calendar calendarDebut = new GregorianCalendar();
		calendarDebut.setTime(dateDebut);

		Calendar calendarFin = new GregorianCalendar();
		calendarFin.setTime(dateFin);

		while (calendarDebut.compareTo(calendarFin) <= 0) {

			if (calendarDebut.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
				if (isJourHoliday(listJoursFeries, calendarDebut.getTime())) {
					compteur++;
				}
			}
			calendarDebut.add(Calendar.DAY_OF_MONTH, 1);
		}
		return (double) compteur;
	}

	protected Double getNombreDimanche(Date dateDebut, Date dateFin) {

		double compteur = 0;
		// on calcule le nombre de vendredi
		DateTime startDate = new DateTime(dateDebut).withHourOfDay(0).withMinuteOfHour(0); // on met les heures et minutes à zéro afin de bien comptabiliser dans la boucle while
		DateTime endDate = new DateTime(dateFin);

		DateTime thisMonday = startDate.withDayOfWeek(DateTimeConstants.SUNDAY);

		if (startDate.isAfter(thisMonday)) {
			startDate = thisMonday.plusWeeks(1); // start on next SUNDAY
		} else {
			startDate = thisMonday; // start on this SUNDAY
		}
		while (startDate.isBefore(endDate)) {
			// #43822 : On gère le cas des demi-journées
			if ((startDate.getDayOfYear() == new DateTime(dateDebut).getDayOfYear() && HEURE_JOUR_DEBUT_PM == new DateTime(dateDebut).getHourOfDay())
					|| (startDate.getDayOfYear() == new DateTime(dateFin).getDayOfYear() && HEURE_JOUR_FIN_AM == new DateTime(dateFin).getHourOfDay()))
				compteur += 0.5;
			else
				compteur++;
			startDate = startDate.plusWeeks(1);
		}
		return compteur;
	}

	protected Double getNombreJourSemaineWithoutFerie(Date dateDebut, Date dateFin, int jourDonne, List<JourDto> listJoursFeries) {

		double compteur = 0;
		// on calcule le nombre de vendredi
		DateTime startDate = new DateTime(dateDebut).withHourOfDay(0).withMinuteOfHour(0);
		// on met les heures et minutes a zero afin de bien comptabiliser dans
		// la
		// boucle while
		DateTime endDate = new DateTime(dateFin);

		DateTime thisDay = startDate.withDayOfWeek(jourDonne);

		if (startDate.isAfter(thisDay)) {
			startDate = thisDay.plusWeeks(1); // start on next SUNDAY
		} else {
			startDate = thisDay; // start on this SUNDAY
		}
		while (startDate.isBefore(endDate)) {
			if (!isJourHoliday(listJoursFeries, startDate.toDate())) {
				// #43822 : On gère le cas des demi-journées
				if (startDate.getDayOfYear() == new DateTime(dateDebut).getDayOfYear() && HEURE_JOUR_DEBUT_PM == new DateTime(dateDebut).getHourOfDay()) {
					compteur = compteur + 0.5;
				} else if (startDate.getDayOfYear() == new DateTime(dateFin).getDayOfYear() && HEURE_JOUR_FIN_AM == new DateTime(dateFin).getHourOfDay()) {
					compteur = compteur + 0.5;
				} else {
					compteur++;
				}
			}
			startDate = startDate.plusWeeks(1);
		}
		return compteur;
	}

	public Double getNombreSamediDecompte(DemandeCongesAnnuels demande) {
		List<JourDto> listJoursFeries = sirhWSConsumer.getListeJoursFeries(demande.getDateDebut(), demande.getDateFin());
		return getNombreSamediDecompte(demande, listJoursFeries);
	}

	public Double getNombreSamediDecompte(DemandeCongesAnnuels demande, List<JourDto> listJoursFeries) {

		Double compteur = 0.0;

		if (demande.getTypeSaisiCongeAnnuel() != null && demande.getTypeSaisiCongeAnnuel().isDecompteSamedi()) {

			// on calcule le nombre de vendredi
			DateTime startDate = new DateTime(demande.getDateDebut()).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0); 
			// on met les heures et minutes a zero afin de bien comptabiliser le nombre de vendredi dans la boucle while
			DateTime endDate = new DateTime(demande.getDateFin());

			// on boucle sur tous les jours de la periode
			while (startDate.isBefore(endDate)) {

				// si jeudi
				if (startDate.getDayOfWeek() == DateTimeConstants.THURSDAY) {
					// est ce que vendredi ferie
					if (isJourHoliday(listJoursFeries, startDate.plusDays(1).toDate())) {
						// est ce que samedi non chome
						if (!isJourHoliday(listJoursFeries, startDate.plusDays(2).toDate())) {
							// alors on ajoute un samedi decompte
							compteur += 1;
						}
					}
				}

				// si vendredi et non ferie
				if (startDate.getDayOfWeek() == DateTimeConstants.FRIDAY && !isJourHoliday(listJoursFeries, startDate.toDate())) {
					// est ce que samedi non chome
					if (!isJourHoliday(listJoursFeries, startDate.plusDays(1).toDate())) {
						compteur += 1;
					}
				}

				startDate = startDate.plusDays(1);
			}

			// cas ou le 1er jour est un vendredi
			// on gere le cas ou l agent a pose l apres-midi
			DateTime dateDebut = new DateTime(demande.getDateDebut());
			if (dateDebut.getDayOfWeek() == DateTimeConstants.FRIDAY && !isJourHoliday(listJoursFeries, dateDebut.plusDays(1).toDate())) {
				if (dateDebut.getHourOfDay() == HEURE_JOUR_DEBUT_PM) {
					compteur -= 0.5; // si commence l apres-midi, on ne decompte
										// qu un demi-samedi
				}
				// cas ou le 1er jour est un jeudi
			} else if (dateDebut.getDayOfWeek() == DateTimeConstants.THURSDAY) {
				// et vendredi ferie et samedi non chome
				if (isJourHoliday(listJoursFeries, dateDebut.plusDays(1).toDate())
						&& !isJourHoliday(listJoursFeries, dateDebut.plusDays(2).toDate())) {
					if (dateDebut.getHourOfDay() == HEURE_JOUR_DEBUT_PM) {
						compteur -= 0.5; // si commence l apres-midi, on ne
											// decompte qu un demi-samedi
					}
				}
			}

			// cas ou le dernier jour est un vendredi
			// on gere le cas ou l agent a pose que le matin ou que l apres-midi
			DateTime dateFin = new DateTime(demande.getDateFin());
			if (dateFin.getDayOfWeek() == DateTimeConstants.FRIDAY && !isJourHoliday(listJoursFeries, dateFin.plusDays(1).toDate())) {
				if (dateFin.getHourOfDay() == HEURE_JOUR_FIN_AM) {
					compteur -= 1; // si la personne revient travailler le
									// vendredi apres-midi, on ne decompte pas
									// le samedi
				}
				// cas ou le dernier jour est un jeudi ET vendredi ferie
			} else if (dateFin.getDayOfWeek() == DateTimeConstants.THURSDAY) {
				// et vendredi ferie et samedi non chome
				if (isJourHoliday(listJoursFeries, dateFin.plusDays(1).toDate()) && !isJourHoliday(listJoursFeries, dateFin.plusDays(2).toDate())) {
					if (dateFin.getHourOfDay() == HEURE_JOUR_FIN_AM) {
						compteur -= 1; // si la personne revient travailler le
										// jeudi apres-midi, on ne decompte pas
										// le samedi
					}
				}
			}
		}
		return compteur;
	}

	private boolean isJourHoliday(List<JourDto> listJoursFeries, Date dateJour) {
		if (null != listJoursFeries) {
			DateTime dateTimeJour = new DateTime(dateJour);
			for (JourDto jourFerie : listJoursFeries) {
				DateTime dateTimeFerie = new DateTime(jourFerie.getJour());
				if (dateTimeFerie.getDayOfYear() == dateTimeJour.getDayOfYear()) {
					return true;
				}
			}
		}
		return false;
	}

	public Double getNombreSamediOffert(DemandeCongesAnnuels demande) {
		List<JourDto> listJoursFeries = sirhWSConsumer.getListeJoursFeries(demande.getDateDebut(), demande.getDateFin());
		return getNombreSamediOffert(demande, listJoursFeries);
	}

	public Double getNombreSamediOffert(DemandeCongesAnnuels demande, List<JourDto> listJoursFeries) {

		// on cherche le nombre de samedi deja offert
		Integer nombreSamediOffert = demandeRepository.getNombreSamediOffertSurAnnee(demande.getIdAgent(),
				new DateTime(demande.getDateDebut()).getYear(), demande.getIdDemande());

		// si le nombre de samedi superieur ou egal au quota, on n offre plus de
		// samedi
		if (NOMBRE_SAMEDI_OFFERT_PAR_AN_PAR_AGENT <= nombreSamediOffert) {
			return 0.0;
		}

		// on recupere le nombre de samedi complet a decompter
		// si au moins un samedi complet, on offre un samedi
		if (1 > getNombreSamediDecompte(demande, listJoursFeries))
			return 0.0;

		// si samedi pas encore offert, on retourne un samedi offert
		return (double) NOMBRE_SAMEDI_OFFERT_PAR_AN_PAR_AGENT;
	}

	public TypeChainePaieEnum getTypeChainePaieFromStatut(Spcarr carr) {
		if (isConventionCollective(carr))
			return TypeChainePaieEnum.SCV;
		else
			return TypeChainePaieEnum.SHC;
	}

	public String getHeureMinuteToString(int nombreMinute) {
		if (nombreMinute < 0) {
			nombreMinute = -nombreMinute;
		}
		int heure = nombreMinute / 60;
		int minute = nombreMinute % 60;
		String res = "";
		if (heure > 0)
			res += heure + "h";
		if (minute > 0)
			res += minute + "m";

		return res;

	}

}
