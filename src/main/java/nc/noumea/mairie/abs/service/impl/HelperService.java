package nc.noumea.mairie.abs.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeSaisi;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class HelperService {

	private static SimpleDateFormat mairieDateFormat = new SimpleDateFormat("yyyyMMdd");

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
RefTypeSaisi typeSaisi, Date dateFin, Date dateDeb, Double duree, 
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
					cal.set(Calendar.HOUR_OF_DAY, HEURE_JOUR_FIN_AM);
					cal.set(Calendar.MINUTE, MINUTES_JOUR_FIN);
					cal.set(Calendar.SECOND, SECONDS_FIN);
					cal.set(Calendar.MILLISECOND, MILLISECONDS);
				return cal.getTime();
			}
			if(dateFinPM) {
				Calendar cal = Calendar.getInstance();
					cal.setTime(dateFin);
					cal.set(Calendar.HOUR_OF_DAY, HEURE_JOUR_FIN_PM);
					cal.set(Calendar.MINUTE, MINUTES_JOUR_FIN);
					cal.set(Calendar.SECOND, SECONDS_FIN);
					cal.set(Calendar.MILLISECOND, MILLISECONDS);
				return cal.getTime();
			}
		}
		if(!typeSaisi.isCalendarDateFin() &&  typeSaisi.isDuree()) {
			DateTime recupDateFin = new DateTime(dateDeb);
			return recupDateFin.plusMinutes(duree.intValue()).toDate();
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
					cal.set(Calendar.HOUR_OF_DAY, HEURE_JOUR_DEBUT_AM);
					cal.set(Calendar.MINUTE, MINUTES_JOUR_DEBUT);
					cal.set(Calendar.SECOND, SECONDS_DEBUT);
					cal.set(Calendar.MILLISECOND, MILLISECONDS);
				return cal.getTime();
			}
			if(dateDebutPM) {
				Calendar cal = Calendar.getInstance();
					cal.setTime(dateDeb);
					cal.set(Calendar.HOUR_OF_DAY, HEURE_JOUR_DEBUT_PM);
					cal.set(Calendar.MINUTE, MINUTES_JOUR_DEBUT);
					cal.set(Calendar.SECOND, SECONDS_DEBUT);
					cal.set(Calendar.MILLISECOND, MILLISECONDS);
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
	
	public Double calculJoursAlimAutoCompteur(DemandeEtatChangeDto demandeEtatChangeDto, Demande demande, Date dateDebut, Date dateFin) {
		Double jours = 0.0;
		// si on approuve, le compteur decremente
		if (demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.VALIDEE.getCodeEtat())) {
			jours = 0.0 - calculNombreJoursArrondiDemiJournee(dateDebut, dateFin);
		}
		// si on passe de Approuve a Refuse, le compteur incremente
		if ((demandeEtatChangeDto.getIdRefEtat().equals(RefEtatEnum.REJETE.getCodeEtat()) || demandeEtatChangeDto
				.getIdRefEtat().equals(RefEtatEnum.EN_ATTENTE.getCodeEtat()))
				&& demande.getLatestEtatDemande().getEtat().equals(RefEtatEnum.VALIDEE)) {
			jours = calculNombreJoursArrondiDemiJournee(dateDebut, dateFin);
		}

		return jours;
	}
	
	public double calculNombreJoursArrondiDemiJournee(Date dateDebut, Date dateFin) {
		
		double diff = dateFin.getTime() - dateDebut.getTime();
		// calcul nombre jour
		double nbrJour = diff / MILLISECONDS_PER_DAY;
		// arrondi a 0.5
		return ((double)Math.round(nbrJour * 2) / 2);
	}
}
