package nc.noumea.mairie.abs.service.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import nc.noumea.mairie.abs.domain.RefTypeSaisi;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.domain.Spcarr;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class HelperService {

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
		if (!typeSaisi.isCalendarDateFin() && !typeSaisi.isCalendarHeureFin() 
				&& !typeSaisi.isChkDateFin()
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
			return duree;
		}

		return 0.0;
	}

	public Double calculMinutesAlimManuelleCompteur(CompteurDto compteurDto) {

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

	public Double calculJoursAlimManuelleCompteur(CompteurDto compteurDto) {

		Double minutes = 0.0;
		if (null != compteurDto.getDureeAAjouter()) {
			minutes = compteurDto.getDureeAAjouter();
		}
		return minutes;
	}

	public double calculNombreJoursArrondiDemiJournee(Date dateDebut, Date dateFin) {

		double diff = dateFin.getTime() - dateDebut.getTime();
		// calcul nombre jour
		double nbrJour = diff / MILLISECONDS_PER_DAY;
		// arrondi a 0.5
		return ((double) Math.round(nbrJour * 2) / 2);
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
}
