package nc.noumea.mairie.abs.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import nc.noumea.mairie.abs.domain.RefTypeSaisi;
import nc.noumea.mairie.abs.dto.CompteurDto;

import org.joda.time.LocalDate;
import org.junit.Test;

public class HelperServiceTest {

	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Test
	public void getCurrentDate() {
		HelperService service = new HelperService();

		assertEquals(new Date(), service.getCurrentDate());
	}

	@Test
	public void isDateAMonday_DateIsMonday_ReturnTrue() {
		HelperService service = new HelperService();

		assertTrue(service.isDateAMonday(new LocalDate(2013, 7, 1).toDate()));
	}

	@Test
	public void isDateAMonday_DateIsNotAMonday_ReturnFalse() {
		HelperService service = new HelperService();

		assertFalse(service.isDateAMonday(new LocalDate(2013, 7, 2).toDate()));
	}

	@Test
	public void getDateFin_typeSaisiDuree_resultOk() {

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setDuree(true);
		typeSaisi.setCalendarDateFin(false);

		HelperService service = new HelperService();

		java.util.Date dateFinAttendue = new java.util.Date();
		java.util.Date dateDebut = new java.util.Date();
		try {
			dateDebut = sdf.parse("2013-12-17 10:00:00");
			dateFinAttendue = sdf.parse("2013-12-17 10:22:00");
		} catch (ParseException pe) {
			pe.printStackTrace();
		}

		Date result = service.getDateFin(typeSaisi, null, dateDebut, 22.0, false, false);

		assertEquals(result, dateFinAttendue);
	}

	@Test
	public void getDateFin_typeSaisiDuree_resultKo() {

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setDuree(true);
		typeSaisi.setCalendarDateFin(false);

		HelperService service = new HelperService();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		java.util.Date dateFinAttendue = new java.util.Date();
		java.util.Date dateDebut = new java.util.Date();
		try {
			dateDebut = sdf.parse("2013-12-17 10:00:00");
			dateFinAttendue = sdf.parse("2013-12-17 10:22:00");
		} catch (ParseException pe) {
			pe.printStackTrace();
		}

		Date result = service.getDateFin(typeSaisi, null, dateDebut, 21.0, false, false);

		assertNotEquals(result, dateFinAttendue);
	}

	@Test
	public void getDateFin_typeSaisiDateEtHeureFin_resultOk() {

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setDuree(false);
		typeSaisi.setCalendarDateFin(true);
		typeSaisi.setCalendarHeureFin(true);

		HelperService service = new HelperService();

		java.util.Date dateFinAttendue = new java.util.Date();
		java.util.Date dateDebut = new java.util.Date();
		try {
			dateDebut = sdf.parse("2013-12-17 10:00:00");
			dateFinAttendue = sdf.parse("2013-12-17 10:22:00");
		} catch (ParseException pe) {
			pe.printStackTrace();
		}

		Date result = service.getDateFin(typeSaisi, dateFinAttendue, dateDebut, null, false, false);

		assertEquals(result, dateFinAttendue);
	}

	@Test
	public void getDateFin_typeSaisiChkDateFin_matin_resultOk() {

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setDuree(false);
		typeSaisi.setCalendarDateFin(true);
		typeSaisi.setCalendarHeureFin(false);
		typeSaisi.setChkDateFin(true);

		HelperService service = new HelperService();

		java.util.Date dateFinSaisie = new java.util.Date();
		java.util.Date dateFinAttendue = new java.util.Date();
		try {
			dateFinSaisie = sdf.parse("2013-12-17 00:00:00");
			dateFinAttendue = sdf.parse("2013-12-17 11:59:59");
		} catch (ParseException pe) {
			pe.printStackTrace();
		}

		Date result = service.getDateFin(typeSaisi, dateFinSaisie, null, null, true, false);

		assertEquals(result, dateFinAttendue);
	}

	@Test
	public void getDateFin_typeSaisiChkDateFin_apresmidi_resultOk() {

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setDuree(true);
		typeSaisi.setCalendarDateFin(true);
		typeSaisi.setCalendarHeureFin(false);
		typeSaisi.setChkDateFin(true);

		HelperService service = new HelperService();

		java.util.Date dateFinSaisie = new java.util.Date();
		java.util.Date dateFinAttendue = new java.util.Date();
		try {
			dateFinSaisie = sdf.parse("2013-12-17 00:00:00");
			dateFinAttendue = sdf.parse("2013-12-17 23:59:59");
		} catch (ParseException pe) {
			pe.printStackTrace();
		}

		Date result = service.getDateFin(typeSaisi, dateFinSaisie, null, null, false, true);

		assertEquals(result, dateFinAttendue);
	}

	@Test
	public void getDateDebut_typeSaisiDateEtHeureFin_resultOk() {

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setCalendarDateDebut(true);
		typeSaisi.setCalendarHeureDebut(true);

		HelperService service = new HelperService();

		java.util.Date dateDebutAttendue = new java.util.Date();
		java.util.Date dateDebut = new java.util.Date();
		try {
			dateDebut = sdf.parse("2013-12-17 10:22:00");
			dateDebutAttendue = sdf.parse("2013-12-17 10:22:00");
		} catch (ParseException pe) {
			pe.printStackTrace();
		}

		Date result = service.getDateDebut(typeSaisi, dateDebut, false, false);

		assertEquals(result, dateDebutAttendue);
	}

	@Test
	public void getDateDebut_typeSaisiChkDateFin_matin_resultOk() {

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setCalendarDateDebut(true);
		typeSaisi.setChkDateDebut(true);

		HelperService service = new HelperService();

		java.util.Date dateDebSaisie = new java.util.Date();
		java.util.Date dateDebutAttendue = new java.util.Date();
		try {
			dateDebSaisie = sdf.parse("2013-12-17 00:00:00");
			dateDebutAttendue = sdf.parse("2013-12-17 00:00:00");
		} catch (ParseException pe) {
			pe.printStackTrace();
		}

		Date result = service.getDateDebut(typeSaisi, dateDebSaisie, true, false);

		assertEquals(result, dateDebutAttendue);
	}

	@Test
	public void getDateDebut_typeSaisiChkDateFin_apresmidi_resultOk() {

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setCalendarDateDebut(true);
		typeSaisi.setChkDateDebut(true);

		HelperService service = new HelperService();

		java.util.Date dateDebSaisie = new java.util.Date();
		java.util.Date datedebutAttendue = new java.util.Date();
		try {
			dateDebSaisie = sdf.parse("2013-12-17 00:00:00");
			datedebutAttendue = sdf.parse("2013-12-17 12:00:00");
		} catch (ParseException pe) {
			pe.printStackTrace();
		}

		Date result = service.getDateDebut(typeSaisi, dateDebSaisie, false, true);

		assertEquals(result, datedebutAttendue);
	}

	@Test
	public void calculMinutesAlimManuelleCompteur_ajoutMinutes() {

		CompteurDto compteurDto = new CompteurDto();
		compteurDto.setDureeAAjouter(10.0);

		HelperService service = new HelperService();
		Double minutes = service.calculMinutesAlimManuelleCompteur(compteurDto);

		assertEquals(10, minutes.intValue());
	}

	@Test
	public void calculMinutesAlimManuelleCompteur_retireMinutes() {

		CompteurDto compteurDto = new CompteurDto();
		compteurDto.setDureeARetrancher(10.0);

		HelperService service = new HelperService();
		Double minutes = service.calculMinutesAlimManuelleCompteur(compteurDto);

		assertEquals(-10, minutes.intValue());
	}

	@Test
	public void calculMinutesAlimManuelleCompteur_zero() {

		CompteurDto compteurDto = new CompteurDto();

		HelperService service = new HelperService();
		Double minutes = service.calculMinutesAlimManuelleCompteur(compteurDto);

		assertEquals(0, minutes.intValue());
	}

	@Test
	public void getCurrentDateMoinsUnAn() {

		GregorianCalendar calStr1 = new GregorianCalendar();
		calStr1.setTime(new Date());
		calStr1.add(GregorianCalendar.YEAR, -1);

		HelperService service = new HelperService();
		Date result = service.getCurrentDateMoinsUnAn();

		assertEquals(sdf.format(result), sdf.format(calStr1.getTime()));
	}

	@Test
	public void calculNombreJourEntre2Dates_entier() {

		Date dateJ = new Date();
		GregorianCalendar calDebut = new GregorianCalendar();
		calDebut.setTime(dateJ);

		GregorianCalendar calFin = new GregorianCalendar();
		calFin.setTime(dateJ);
		calFin.add(Calendar.DAY_OF_YEAR, 8);

		HelperService service = new HelperService();
		Double result = service.calculNombreJoursArrondiDemiJournee(calDebut.getTime(), calFin.getTime());

		assertEquals(result.intValue(), 8);
	}

	@Test
	public void calculNombreJourEntre2Dates_demiJournee() {

		Date dateJ = new Date();
		GregorianCalendar calDebut = new GregorianCalendar();
		calDebut.setTime(dateJ);

		GregorianCalendar calFin = new GregorianCalendar();
		calFin.setTime(dateJ);
		calFin.add(Calendar.DAY_OF_YEAR, 8);
		calFin.add(Calendar.HOUR, 12);

		HelperService service = new HelperService();
		Double result = service.calculNombreJoursArrondiDemiJournee(calDebut.getTime(), calFin.getTime());

		assertEquals(result.floatValue(), 8, 5);
	}

	@Test
	public void calculNombreJourEntre2Dates_demiJourneeArrondiInferieur() {

		Date dateJ = new Date();
		GregorianCalendar calDebut = new GregorianCalendar();
		calDebut.setTime(dateJ);

		GregorianCalendar calFin = new GregorianCalendar();
		calFin.setTime(dateJ);
		calFin.add(Calendar.DAY_OF_YEAR, 8);
		calFin.add(Calendar.HOUR, 14);

		HelperService service = new HelperService();
		Double result = service.calculNombreJoursArrondiDemiJournee(calDebut.getTime(), calFin.getTime());

		assertEquals(result.floatValue(), 8, 5);
	}

	@Test
	public void calculNombreJourEntre2Dates_demiJourneeArrondiSuperieur() {

		Date dateJ = new Date();
		GregorianCalendar calDebut = new GregorianCalendar();
		calDebut.setTime(dateJ);

		GregorianCalendar calFin = new GregorianCalendar();
		calFin.setTime(dateJ);
		calFin.add(Calendar.DAY_OF_YEAR, 8);
		calFin.add(Calendar.HOUR, 10);

		HelperService service = new HelperService();
		Double result = service.calculNombreJoursArrondiDemiJournee(calDebut.getTime(), calFin.getTime());

		assertEquals(result.floatValue(), 8, 5);
	}
	
	@Test
	public void getDuree_returnDuree() {
		
		Double duree = 1.0;
		
		RefTypeSaisi typeSaisi = new RefTypeSaisi();
			typeSaisi.setDuree(true);
		
		HelperService service = new HelperService();
		Double result = service.getDuree(typeSaisi, new Date(), new Date(), duree);
		
		assertEquals(duree, result);
	}

	@Test
	public void getDuree_returnMinutes() {

		Double duree = 1.0;
		
		RefTypeSaisi typeSaisi = new RefTypeSaisi();
			typeSaisi.setCalendarDateFin(true);
			typeSaisi.setUniteDecompte(HelperService.UNITE_DECOMPTE_MINUTES);
		
		Date dateJ = new Date();
			GregorianCalendar calDebut = new GregorianCalendar();
			calDebut.setTime(dateJ);

		GregorianCalendar calFin = new GregorianCalendar();
			calFin.setTime(dateJ);
			calFin.add(Calendar.HOUR, 12);
		
		HelperService service = new HelperService();
		Double result = service.getDuree(typeSaisi, calDebut.getTime(), calFin.getTime(), duree);
		
		assertEquals(result, new Double(12*60));
	}
	
	@Test
	public void getDuree_returnJours() {
		Double duree = 1.0;
		
		RefTypeSaisi typeSaisi = new RefTypeSaisi();
			typeSaisi.setCalendarDateFin(true);
			typeSaisi.setUniteDecompte(HelperService.UNITE_DECOMPTE_JOURS);
		
		Date dateJ = new Date();
		GregorianCalendar calDebut = new GregorianCalendar();
			calDebut.setTime(dateJ);

		GregorianCalendar calFin = new GregorianCalendar();
			calFin.setTime(dateJ);
			calFin.add(Calendar.HOUR, 12);
		
		HelperService service = new HelperService();
		Double result = service.getDuree(typeSaisi, calDebut.getTime(), calFin.getTime(), duree);
		
		assertEquals(result, 0,5);
	}
}
