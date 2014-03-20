package nc.noumea.mairie.abs.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import nc.noumea.mairie.abs.dto.CompteurDto;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class HelperService {

	private static SimpleDateFormat mairieDateFormat = new SimpleDateFormat("yyyyMMdd");

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

	public Date getDateFin(Date dateDeb, Integer duree) {

		DateTime recupDateFin = new DateTime(dateDeb);
		return recupDateFin.plusMinutes(duree).toDate();
	}

	public int calculMinutesAlimManuelleCompteur(CompteurDto compteurDto) {

		int minutes = 0;
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

	public int calculJoursAlimManuelleCompteur(CompteurDto compteurDto) {

		int minutes = 0;
		if (null != compteurDto.getDureeAAjouter()) {
			minutes = compteurDto.getDureeAAjouter();
		}
		return minutes;
	}
}
