package nc.noumea.mairie.abs.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import nc.noumea.mairie.abs.domain.RefTypeSaisi;
import nc.noumea.mairie.abs.dto.CompteurDto;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class HelperService {

	private static SimpleDateFormat mairieDateFormat = new SimpleDateFormat("yyyyMMdd");

	private static int HEURE_JOUR_DEBUT_AM = 0;
	private static int HEURE_JOUR_FIN_AM = 12;
	private static int HEURE_JOUR_DEBUT_PM = 12;
	private static int HEURE_JOUR_FIN_PM = 23;
	private static int MINUTES_JOUR_FIN_PM = 59;
	
	private final static long MILLISECONDS_PER_DAY = 1000 * 60 * 60 * 24;
	
	public Date getCurrentDate() {
		return new Date();
	}

	public boolean isDateAMonday(Date dateMonday) {
		return new LocalDate(dateMonday).getDayOfWeek() == DateTimeConstants.MONDAY;
	}

	public Date getDateFromMairieInteger(Integer dateAsInteger) {
		if (dateAsInteger == null || dateAsInteger.equals(0))
			return null;

		try {
			return mairieDateFormat.parse(String.valueOf(dateAsInteger));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return null;
	}

	public Date getDateFin(
			RefTypeSaisi typeSaisi, Date dateFin, Date dateDeb,	Integer duree, 
			boolean dateFinAM, boolean dateFinPM) {
		
		if(typeSaisi.isCalendarDateFin() && typeSaisi.isCalendarHeureFin()
				&& !typeSaisi.isChkDateFin()) {
			return dateFin;
		}
		if(typeSaisi.isCalendarDateFin() && !typeSaisi.isCalendarHeureFin()
				&& typeSaisi.isChkDateFin()) {
			
			if(dateFinAM && !dateFinPM) {
				Calendar cal = Calendar.getInstance();
					cal.setTime(dateFin);
					cal.set(Calendar.HOUR, HEURE_JOUR_FIN_AM);
				return cal.getTime();
			}
			if(dateFinPM) {
				Calendar cal = Calendar.getInstance();
					cal.setTime(dateFin);
					cal.set(Calendar.HOUR, HEURE_JOUR_FIN_PM);
					cal.set(Calendar.MINUTE, MINUTES_JOUR_FIN_PM);
				return cal.getTime();
			}
		}
		if(!typeSaisi.isCalendarDateFin() &&  typeSaisi.isDuree()) {
			DateTime recupDateFin = new DateTime(dateDeb);
			return recupDateFin.plusMinutes(duree).toDate();
		}

		return null;
	}
	
	public Date getDateDebut(
			RefTypeSaisi typeSaisi, Date dateDeb, boolean dateDebutAM, boolean dateDebutPM) {
		
		if(typeSaisi.isCalendarDateDebut() && typeSaisi.isCalendarHeureDebut()
				&& !typeSaisi.isChkDateDebut()) {
			return dateDeb;
		}
		if(typeSaisi.isCalendarDateDebut() && !typeSaisi.isCalendarHeureDebut()
				&& typeSaisi.isChkDateDebut()) {
			
			if(dateDebutAM && !dateDebutPM) {
				Calendar cal = Calendar.getInstance();
					cal.setTime(dateDeb);
					cal.set(Calendar.HOUR, HEURE_JOUR_DEBUT_AM);
				return cal.getTime();
			}
			if(!dateDebutAM && dateDebutPM) {
				Calendar cal = Calendar.getInstance();
					cal.setTime(dateDeb);
					cal.set(Calendar.HOUR, HEURE_JOUR_DEBUT_PM);
				return cal.getTime();
			}
		}

		return null;
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
	
	public double calculNombreJourEntre2Dates(Date dateDebut, Date dateFin) {
		
		double diff = dateFin.getTime() - dateDebut.getTime();
		return diff / MILLISECONDS_PER_DAY;
	}
}
