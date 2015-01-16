package nc.noumea.mairie.abs.service.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import nc.noumea.mairie.abs.domain.DemandeCongesAnnuels;
import nc.noumea.mairie.abs.domain.RefTypeSaisi;
import nc.noumea.mairie.abs.domain.RefTypeSaisiCongeAnnuel;
import nc.noumea.mairie.abs.domain.RefUnitePeriodeQuota;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.repository.IDemandeRepository;
import nc.noumea.mairie.domain.Spcarr;
import nc.noumea.mairie.ws.ISirhWSConsumer;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HelperService {

	@Autowired
	private ISirhWSConsumer sirhWSConsumer;

	@Autowired
	private IDemandeRepository demandeRepository;

	private static int HEURE_JOUR_DEBUT_AM = 0;
	private static int HEURE_JOUR_FIN_AM = 11;
	private static int HEURE_JOUR_DEBUT_PM = 12;
	private static int HEURE_JOUR_FIN_PM = 23;
	private static int MINUTES_JOUR_FIN = 59;
	private static int MINUTES_JOUR_DEBUT = 0;
	private static int SECONDS_DEBUT = 0;
	private static int SECONDS_FIN = 59;
	private static int MILLISECONDS = 0;

	private final static long MILLISECONDS_PER_DAY = 1000 * 60 * 60 * 24;
	private final static long MILLISECONDS_PER_MINUTES = 1000 * 60;

	public static String UNITE_DECOMPTE_AN = "an";
	public static String UNITE_DECOMPTE_MOIS = "mois";
	public static String UNITE_DECOMPTE_JOURS = "jours";
	public static String UNITE_DECOMPTE_MINUTES = "minutes";

	public Date getCurrentDate() {
		return new Date();
	}

	public boolean isDateAMonday(Date dateMonday) {
		return new LocalDate(dateMonday).getDayOfWeek() == DateTimeConstants.MONDAY;
	}

	public Date getDateFin(RefTypeSaisi typeSaisi, Date dateFin, Date dateDeb, Double duree, boolean dateFinAM,
			boolean dateFinPM) {

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
			DateTime recupDateFin = new DateTime(dateDeb);
			return recupDateFin.plusMinutes(duree.intValue() * 60).toDate();

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
		if (!typeSaisi.isCalendarDateFin() && !typeSaisi.isCalendarHeureFin() && !typeSaisi.isChkDateFin()
				&& !typeSaisi.isDuree()) {
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
				return calculNombreJoursArrondiDemiJournee(dateDebut, dateFin);
			}
			if (UNITE_DECOMPTE_MINUTES.equals(typeSaisi.getUniteDecompte())) {
				return new Double(calculNombreMinutes(dateDebut, dateFin));
			}
		}

		if (!typeSaisi.isCalendarDateFin() && typeSaisi.isDuree()) {
			if (UNITE_DECOMPTE_MINUTES.equals(typeSaisi.getUniteDecompte())) {
				return duree * 60;
			}
			return duree;
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

	public Date getDateDebutCongeAnnuel(RefTypeSaisiCongeAnnuel refTypeSaisiCongeAnnuel, Date dateDebut,
			boolean dateDebutAM, boolean dateDebutPM) {

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

	public Date getDateFinCongeAnnuel(RefTypeSaisiCongeAnnuel refTypeSaisiCongeAnnuel, Date dateFin, Date dateDebut,
			boolean dateFinAM, boolean dateFinPM, Date dateReprise) {

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

	public Double getDureeCongeAnnuel(DemandeCongesAnnuels demande, Date dateReprise) {
		Double duree = 0.0;
		switch (demande.getTypeSaisiCongeAnnuel().getCodeBaseHoraireAbsence()) {
			case "A":
			case "D":
				duree = calculNombreJoursArrondiDemiJournee(demande.getDateDebut(), demande.getDateFin())
						- calculJoursNonComptesDimancheFerieChome(demande.getDateDebut(), demande.getDateFin())
						+ getNombreSamediDecompte(demande) - getNombreSamediOffert(demande);
				break;

			case "E":
			case "F":
				duree = calculNombreJours(demande.getDateDebut(), demande.getDateFin());
				break;

			case "C":
				duree = calculNombreJours(demande.getDateDebut(), dateReprise);
				duree = Math.ceil((duree / demande.getTypeSaisiCongeAnnuel().getQuotaMultiple()) * 3);
				if (duree < 3) {
					duree = calculNombreJours(demande.getDateDebut(), dateReprise);
				} else if (duree > 3 && duree <= demande.getTypeSaisiCongeAnnuel().getQuotaMultiple()) {
					duree = 3.0;
				}
				break;

			default:
				break;
		}

		return duree;
	}

	private double calculJoursNonComptesDimancheFerieChome(Date dateDebut, Date dateFin) {
		Double res = 0.0;
		// on compte le nombre de dimanches entre les 2 dates
		res += getNombreDimanche(dateDebut, dateFin);
		// on compte le nombre de jours fériés ou chomes entre les 2 dates
		res += getNombreJoursFeriesChomes(dateDebut, dateFin);
		return res;

	}

	private Double getNombreJoursFeriesChomes(Date dateDebut, Date dateFin) {
		int compteur = 0;
		Calendar calendarDebut = new GregorianCalendar();
		calendarDebut.setTime(dateDebut);

		Calendar calendarFin = new GregorianCalendar();
		calendarFin.setTime(dateFin);

		while (calendarDebut.compareTo(calendarFin) <= 0) {
			if (calendarDebut.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
				if (sirhWSConsumer.isJourHoliday(calendarDebut.getTime())) {
					compteur++;
				}
			}
			calendarDebut.add(Calendar.DAY_OF_MONTH, 1);
		}
		return (double) compteur;

	}

	private Double getNombreDimanche(Date dateDebut, Date dateFin) {
		int compteur = 0;
		Calendar calendarDebut = new GregorianCalendar();
		calendarDebut.setTime(dateDebut);

		Calendar calendarFin = new GregorianCalendar();
		calendarFin.setTime(dateFin);

		// Différence
		long diff = Math.abs(dateFin.getTime() - dateDebut.getTime());
		long numberOfDay = (long) diff / 86400000;

		for (int i = 0; i <= numberOfDay; i++) {
			if (calendarDebut.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
				compteur++;
			}
			calendarDebut.add(Calendar.DAY_OF_MONTH, 1);
		}
		return (double) compteur;
	}

	public Double getNombreSamediDecompte(DemandeCongesAnnuels demande) {
		Double compteur = 0.0;
		if (demande.getTypeSaisiCongeAnnuel() != null && demande.getTypeSaisiCongeAnnuel().isDecompteSamedi()) {
			Calendar calendarDebut = new GregorianCalendar();
			calendarDebut.setTime(demande.getDateDebut());

			Calendar calendarFin = new GregorianCalendar();
			calendarFin.setTime(demande.getDateFin());

			// Différence
			long diff = Math.abs(demande.getDateFin().getTime() - demande.getDateDebut().getTime());
			long numberOfDay = (long) diff / 86400000;

			for (int i = 0; i <= numberOfDay; i++) {
				if (calendarDebut.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
						&& calendarFin.get(Calendar.HOUR_OF_DAY) != 11) {
					// si vendredi ou demi-vendredi
					if (calendarDebut.get(Calendar.HOUR_OF_DAY) == 0) {
						compteur = compteur + 1;
					} else {
						compteur = compteur + 0.5;
					}
				}
				calendarDebut.add(Calendar.DAY_OF_MONTH, 1);
			}

		}
		return compteur;
	}

	public Double getNombreSamediOffert(DemandeCongesAnnuels demande) {
		Double compteur = 0.0;

		return compteur;
	}

	public Date getFirstMondayOfCurrentMonth() {
		DateTime date = new DateTime().withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);

		return date.dayOfMonth() // Accès à la propriété 'Jour du Mois'
				.withMinimumValue() // prendre sa valeur minimum
				.plusDays(6) // Ajouter 6 jours
				.dayOfWeek() // Accès à la propriété 'Jour de la Semaine'
				.setCopy(DateTimeConstants.MONDAY) // Le positionner à lundi
													// (arrondi à la valeur
													// inférieure)
				.toDate();
	}

}
