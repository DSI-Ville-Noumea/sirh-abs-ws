package nc.noumea.mairie.abs.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import nc.noumea.mairie.abs.domain.DemandeCongesAnnuels;
import nc.noumea.mairie.abs.domain.RefEtat;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeSaisi;
import nc.noumea.mairie.abs.domain.RefTypeSaisiCongeAnnuel;
import nc.noumea.mairie.abs.domain.RefUnitePeriodeQuota;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.JourDto;
import nc.noumea.mairie.abs.repository.IDemandeRepository;
import nc.noumea.mairie.abs.repository.IFiltreRepository;
import nc.noumea.mairie.domain.Spcarr;
import nc.noumea.mairie.ws.ISirhWSConsumer;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class HelperServiceTest {

	SimpleDateFormat	sdf	= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Test
	public void getCurrentDate() {
		HelperService service = new HelperService();

		assertEquals(new DateTime(new Date()), new DateTime(service.getCurrentDate()));
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
		typeSaisi.setUniteDecompte("minutes");

		HelperService service = new HelperService();

		java.util.Date dateFinAttendue = new java.util.Date();
		java.util.Date dateDebut = new java.util.Date();
		try {
			dateDebut = sdf.parse("2013-12-17 10:00:00");
			dateFinAttendue = sdf.parse("2013-12-17 12:00:00");
		} catch (ParseException pe) {
			pe.printStackTrace();
		}

		Date result = service.getDateFin(typeSaisi, null, dateDebut, 2.0, false, false);

		assertEquals(result, dateFinAttendue);
	}

	@Test
	public void getDateFin_typeSaisiDuree_resultKo() {

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setDuree(true);
		typeSaisi.setCalendarDateFin(false);
		typeSaisi.setUniteDecompte("minutes");

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
	public void getDateFin_typeSaisiJustIsCalendarDateFin_resultOk() {

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setDuree(false);
		typeSaisi.setCalendarDateFin(true);
		typeSaisi.setCalendarHeureFin(false);
		typeSaisi.setChkDateFin(false);

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
	public void getDateFin_typeSaisiNoDateFinNoDuree_resultOk() {

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setDuree(false);
		typeSaisi.setCalendarDateFin(false);
		typeSaisi.setCalendarHeureFin(false);
		typeSaisi.setChkDateFin(false);

		HelperService service = new HelperService();

		java.util.Date dateDebutSaisie = new java.util.Date();
		java.util.Date dateFinAttendue = new java.util.Date();
		try {
			dateDebutSaisie = sdf.parse("2013-12-17 00:00:00");
			dateFinAttendue = sdf.parse("2013-12-17 23:59:59");
		} catch (ParseException pe) {
			pe.printStackTrace();
		}

		Date result = service.getDateFin(typeSaisi, null, dateDebutSaisie, null, false, true);

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
	public void getDateDebut_typeSaisiNoHeureNoChk_resultOk() {

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setCalendarDateDebut(true);
		typeSaisi.setChkDateDebut(false);
		typeSaisi.setCalendarHeureDebut(false);

		HelperService service = new HelperService();

		java.util.Date dateDebSaisie = new java.util.Date();
		java.util.Date datedebutAttendue = new java.util.Date();
		try {
			dateDebSaisie = sdf.parse("2013-12-17 00:00:00");
			datedebutAttendue = sdf.parse("2013-12-17 00:00:00");
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
		Double minutes = service.calculAlimManuelleCompteur(compteurDto);

		assertEquals(10, minutes.intValue());
	}

	@Test
	public void calculMinutesAlimManuelleCompteur_retireMinutes() {

		CompteurDto compteurDto = new CompteurDto();
		compteurDto.setDureeARetrancher(10.0);

		HelperService service = new HelperService();
		Double minutes = service.calculAlimManuelleCompteur(compteurDto);

		assertEquals(-10, minutes.intValue());
	}

	@Test
	public void calculMinutesAlimManuelleCompteur_zero() {

		CompteurDto compteurDto = new CompteurDto();

		HelperService service = new HelperService();
		Double minutes = service.calculAlimManuelleCompteur(compteurDto);

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
	public void getCurrentDateMoinsXMois() {
		
		DateTime dateTime = DateTime.now().minusMonths(2);

		HelperService service = new HelperService();
		Date result = service.getCurrentDateMoinsXMois(2);

		assertEquals(sdf.format(result), sdf.format(dateTime.toDate()));
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
	public void calculNombreJourEntre2Dates_3jours() {

		Date dateDebut = new DateTime(2015, 1, 18, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2015, 1, 20, 23, 59, 59).toDate();

		HelperService service = new HelperService();
		Double result = service.calculNombreJoursArrondiDemiJournee(dateDebut, dateFin);

		assertEquals(result.intValue(), 3);
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
	public void getDuree_returnDureeWithMinutes_1Heure8minutes() {

		Double duree = 1.08;

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setDuree(true);
		typeSaisi.setUniteDecompte(HelperService.UNITE_DECOMPTE_MINUTES);

		HelperService service = new HelperService();
		Double result = service.getDuree(typeSaisi, new Date(), new Date(), duree);

		assertEquals(68, result.intValue());
	}

	@Test
	public void getDuree_returnDureeWithMinutes_1Heure15minutes() {

		Double duree = 1.15;

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setDuree(true);
		typeSaisi.setUniteDecompte(HelperService.UNITE_DECOMPTE_MINUTES);

		HelperService service = new HelperService();
		Double result = service.getDuree(typeSaisi, new Date(), new Date(), duree);

		assertEquals(75, result.intValue());
	}

	@Test
	public void getDuree_returnDureeWithMinutes_1Heure30minutes() {

		Double duree = 1.30;

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setDuree(true);
		typeSaisi.setUniteDecompte(HelperService.UNITE_DECOMPTE_MINUTES);

		HelperService service = new HelperService();
		Double result = service.getDuree(typeSaisi, new Date(), new Date(), duree);

		assertEquals(90, result.intValue());
	}

	@Test
	public void getDuree_returnDureeWithMinutes_30minutes() {

		Double duree = 0.30;

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setDuree(true);
		typeSaisi.setUniteDecompte(HelperService.UNITE_DECOMPTE_MINUTES);

		HelperService service = new HelperService();
		Double result = service.getDuree(typeSaisi, new Date(), new Date(), duree);

		assertEquals(30, result.intValue());
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

		assertEquals(result, new Double(12 * 60));
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

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getListeJoursFeries(calDebut.getTime(), calFin.getTime())).thenReturn(null);

		HelperService service = new HelperService();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		Double result = service.getDuree(typeSaisi, calDebut.getTime(), calFin.getTime(), duree);

		assertEquals(result, 0, 5);
	}

	@Test
	public void getDuree_returnJours_CongeUnique_NoCompteDimanche() {
		Double duree = 1.0;

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(44);

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setType(type);
		typeSaisi.setCalendarDateFin(true);
		typeSaisi.setUniteDecompte(HelperService.UNITE_DECOMPTE_JOURS);

		Date dateDebut = new DateTime(2015, 7, 8, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2015, 7, 13, 23, 59, 59).toDate();

		HelperService service = new HelperService();

		Double result = service.getDuree(typeSaisi, dateDebut, dateFin, duree);

		assertEquals(result, 0, 6);
	}

	@Test
	public void getDuree_returnJours_CongeUniqueCCSP_NoCompteDimanche() {
		Double duree = 1.0;

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(45);

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setType(type);
		typeSaisi.setCalendarDateFin(true);
		typeSaisi.setUniteDecompte(HelperService.UNITE_DECOMPTE_JOURS);

		Date dateDebut = new DateTime(2016, 7, 8, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2016, 7, 13, 23, 59, 59).toDate();

		HelperService service = new HelperService();

		Double result = service.getDuree(typeSaisi, dateDebut, dateFin, duree);

		assertEquals(result, 0, 6);
	}

	@Test
	public void getDuree_returnJours_Autre_CompteDimanche() {
		Double duree = 1.0;

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(42);

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setType(type);
		typeSaisi.setCalendarDateFin(true);
		typeSaisi.setUniteDecompte(HelperService.UNITE_DECOMPTE_JOURS);

		Date dateDebut = new DateTime(2016, 7, 8, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2016, 7, 13, 23, 59, 59).toDate();

		JourDto jourFerie = new JourDto();
		jourFerie.setJour(new DateTime(2016, 7, 11, 0, 0, 0).toDate());
		jourFerie.setFerie(true);
		List<JourDto> joursFeries = new ArrayList<JourDto>();
		joursFeries.add(jourFerie);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getListeJoursFeries(dateDebut, dateFin)).thenReturn(joursFeries);

		HelperService service = new HelperService();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		Double result = service.getDuree(typeSaisi, dateDebut, dateFin, duree);

		assertEquals(result, 0, 5);
	}

	@Test
	public void getDuree_returnJours_1Dimanche_1JourFerie() {
		Double duree = 1.0;

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setCalendarDateFin(true);
		typeSaisi.setUniteDecompte(HelperService.UNITE_DECOMPTE_JOURS);

		Date dateDebut = new DateTime(2015, 4, 28, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2015, 5, 6, 0, 0, 0).toDate();

		JourDto jourFerie = new JourDto();
		jourFerie.setJour(new DateTime(2015, 5, 1, 0, 0, 0).toDate());
		jourFerie.setFerie(true);
		List<JourDto> joursFeries = new ArrayList<JourDto>();
		joursFeries.add(jourFerie);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getListeJoursFeries(dateDebut, dateFin)).thenReturn(joursFeries);

		HelperService service = new HelperService();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		Double result = service.getDuree(typeSaisi, dateDebut, dateFin, duree);

		assertEquals(result, 0, 7);
	}

	@Test
	public void getDateDebutMoisForOneDate_returnFirstDAy() {
		Date dateDemande = new DateTime(2014, 05, 13, 12, 30, 0).toDate();

		HelperService service = new HelperService();
		Date result = service.getDateDebutMoisForOneDate(dateDemande);

		assertEquals(result, new DateTime(2014, 05, 1, 0, 0, 0).toDate());
	}

	@Test
	public void getDateFinMoisForOneDate_returnLastDAy() {

		Date dateDemande = new DateTime(2014, 05, 13, 12, 30, 0).toDate();

		HelperService service = new HelperService();
		Date result = service.getDateFinMoisForOneDate(dateDemande);

		assertEquals(result, new DateTime(2014, 05, 31, 23, 59, 59).toDate());
	}

	@Test
	public void isFonctionnaire_false() {
		Spcarr carr = new Spcarr();
		carr.setCdcate(4);

		HelperService service = new HelperService();
		assertFalse(service.isFonctionnaire(carr));
	}

	@Test
	public void isFonctionnaire_falseBis() {
		Spcarr carr = new Spcarr();
		carr.setCdcate(7);

		HelperService service = new HelperService();
		assertFalse(service.isFonctionnaire(carr));
	}

	@Test
	public void isFonctionnaire_true() {
		Spcarr carr = new Spcarr();
		carr.setCdcate(1);

		HelperService service = new HelperService();
		assertTrue(service.isFonctionnaire(carr));
	}

	@Test
	public void isContractuel_false() {
		Spcarr carr = new Spcarr();
		carr.setCdcate(2);

		HelperService service = new HelperService();
		assertFalse(service.isContractuel(carr));
	}

	@Test
	public void isContractuel_true() {
		Spcarr carr = new Spcarr();
		carr.setCdcate(4);

		HelperService service = new HelperService();
		assertTrue(service.isContractuel(carr));
	}

	@Test
	public void isConventionCollective_false() {
		Spcarr carr = new Spcarr();
		carr.setCdcate(3);

		HelperService service = new HelperService();
		assertFalse(service.isConventionCollective(carr));
	}

	@Test
	public void isConventionCollective_true() {
		Spcarr carr = new Spcarr();
		carr.setCdcate(7);

		HelperService service = new HelperService();
		assertTrue(service.isConventionCollective(carr));
	}

	@Test
	public void getDateDebutAnneeForOneDate_returnFirstDAy() {
		Date dateDemande = new DateTime(2014, 05, 13, 12, 30, 0).toDate();

		HelperService service = new HelperService();
		Date result = service.getDateDebutAnneeForOneDate(dateDemande, 1);

		assertEquals(result, new DateTime(2014, 1, 1, 0, 0, 0).toDate());
	}

	@Test
	public void getDateFinAnneeForOneDate_returnLastDAy() {
		Date dateDemande = new DateTime(2014, 05, 13, 12, 30, 0).toDate();

		HelperService service = new HelperService();
		Date result = service.getDateFinAnneeForOneDate(dateDemande, 1);

		assertEquals(result, new DateTime(2014, 12, 31, 23, 59, 59).toDate());
	}

	@Test
	public void getDateDebutJourneeForOneDate_returnFirstDAy() {
		Date dateDemande = new DateTime(2014, 05, 13, 12, 30, 0).toDate();

		HelperService service = new HelperService();
		Date result = service.getDateDebutJourneeForOneDate(dateDemande, 1);

		assertEquals(result, new DateTime(2014, 05, 13, 0, 0, 0).toDate());
	}

	@Test
	public void getDateDebutMoisForOneDate_Moins3Mois() {
		Date dateDemande = new DateTime(2014, 05, 13, 12, 30, 0).toDate();

		HelperService service = new HelperService();
		Date result = service.getDateDebutMoisForOneDate(dateDemande, 3);

		assertEquals(result, new DateTime(2014, 03, 1, 0, 0, 0).toDate());
	}

	@Test
	public void getDateDebutByUnitePeriodeQuotaAndDebutDemande_1AnCivil() {
		Date dateDebutDemande = new DateTime(2014, 05, 13, 12, 30, 0).toDate();

		RefUnitePeriodeQuota upq = new RefUnitePeriodeQuota();
		upq.setGlissant(false);
		upq.setUnite("an");
		upq.setValeur(1);

		HelperService service = new HelperService();
		Date result = service.getDateDebutByUnitePeriodeQuotaAndDebutDemande(upq, dateDebutDemande);

		assertEquals(result, new DateTime(2014, 1, 1, 0, 0, 0).toDate());
	}

	@Test
	public void getDateDebutByUnitePeriodeQuotaAndDebutDemande_3MoisNonGlissants() {
		Date dateDebutDemande = new DateTime(2014, 05, 13, 12, 30, 0).toDate();

		RefUnitePeriodeQuota upq = new RefUnitePeriodeQuota();
		upq.setGlissant(false);
		upq.setUnite("mois");
		upq.setValeur(3);

		HelperService service = new HelperService();
		Date result = service.getDateDebutByUnitePeriodeQuotaAndDebutDemande(upq, dateDebutDemande);

		assertEquals(result, new DateTime(2014, 3, 1, 0, 0, 0).toDate());
	}

	@Test
	public void getDateDebutByUnitePeriodeQuotaAndDebutDemande_ParMoisNonGlissant() {
		Date dateDebutDemande = new DateTime(2014, 05, 13, 12, 30, 0).toDate();

		RefUnitePeriodeQuota upq = new RefUnitePeriodeQuota();
		upq.setGlissant(false);
		upq.setUnite("mois");
		upq.setValeur(1);

		HelperService service = new HelperService();
		Date result = service.getDateDebutByUnitePeriodeQuotaAndDebutDemande(upq, dateDebutDemande);

		assertEquals(result, new DateTime(2014, 5, 1, 0, 0, 0).toDate());
	}

	@Test
	public void getDateDebutByUnitePeriodeQuotaAndDebutDemande_12MoisGlissants() {
		Date dateDebutDemande = new DateTime(2014, 05, 13, 12, 30, 0).toDate();

		RefUnitePeriodeQuota upq = new RefUnitePeriodeQuota();
		upq.setGlissant(true);
		upq.setUnite("mois");
		upq.setValeur(12);

		HelperService service = new HelperService();
		Date result = service.getDateDebutByUnitePeriodeQuotaAndDebutDemande(upq, dateDebutDemande);

		assertEquals(result, new DateTime(2013, 5, 13, 12, 30, 0).toDate());
	}

	@Test
	public void getDateDebutByUnitePeriodeQuotaAndDebutDemande_1MoisGlissant() {
		Date dateDebutDemande = new DateTime(2014, 05, 13, 12, 30, 0).toDate();

		RefUnitePeriodeQuota upq = new RefUnitePeriodeQuota();
		upq.setGlissant(true);
		upq.setUnite("mois");
		upq.setValeur(1);

		HelperService service = new HelperService();
		Date result = service.getDateDebutByUnitePeriodeQuotaAndDebutDemande(upq, dateDebutDemande);

		assertEquals(result, new DateTime(2014, 4, 13, 12, 30, 0).toDate());
	}

	@Test
	public void getDureeCongeAnnuel_Base_A_returnZero() {

		Double duree = 0.0;

		RefTypeSaisiCongeAnnuel typeSaisi = new RefTypeSaisiCongeAnnuel();
		typeSaisi.setCodeBaseHoraireAbsence("A");
		typeSaisi.setDecompteSamedi(true);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setDateDebut(new DateTime(2015, 4, 9, 0, 0, 0).toDate());
		demande.setDateFin(new DateTime(2015, 4, 9, 0, 0, 0).toDate());
		demande.setTypeSaisiCongeAnnuel(typeSaisi);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getNombreSamediOffertSurAnnee(demande.getIdAgent(), 2014, demande.getIdDemande())).thenReturn(1);

		HelperService service = new HelperService();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		Double result = service.getDureeCongeAnnuel(demande, new Date(), false, null);

		assertEquals(duree, result);
	}

	@Test
	public void getDureeCongeAnnuel_Base_D_returnDimanche() {
		Double duree = 12.0;

		Date dateDebut = new DateTime(2014, 12, 1, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 14, 23, 59, 0).toDate();
		Date dateReprise = new DateTime(2014, 12, 15, 0, 0, 0).toDate();

		RefTypeSaisiCongeAnnuel typeSaisi = new RefTypeSaisiCongeAnnuel();
		typeSaisi.setCodeBaseHoraireAbsence("D");
		typeSaisi.setDecompteSamedi(true);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setDateDebut(dateDebut);
		demande.setDateFin(dateFin);
		demande.setTypeSaisiCongeAnnuel(typeSaisi);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getNombreSamediOffertSurAnnee(demande.getIdAgent(), 2014, demande.getIdDemande())).thenReturn(1);

		HelperService service = new HelperService();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		Double result = service.getDureeCongeAnnuel(demande, dateReprise, false, null);

		assertEquals(duree, result);
	}

	@Test
	public void getDureeCongeAnnuel_Base_A_returnDimancheAndJourFerie() {
		Double duree = 3.0;

		Date dateDebut = new DateTime(2014, 12, 4, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 8, 23, 59, 0).toDate();
		Date dateReprise = new DateTime(2014, 12, 9, 0, 0, 0).toDate();

		RefTypeSaisiCongeAnnuel typeSaisi = new RefTypeSaisiCongeAnnuel();
		typeSaisi.setCodeBaseHoraireAbsence("A");
		typeSaisi.setDecompteSamedi(true);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setDateDebut(dateDebut);
		demande.setDateFin(dateFin);
		demande.setTypeSaisiCongeAnnuel(typeSaisi);

		List<JourDto> listJoursFeries = new ArrayList<JourDto>();
		JourDto jourFerie = new JourDto();
		jourFerie.setJour(new DateTime(2014, 12, 8, 0, 0, 0).toDate());
		listJoursFeries.add(jourFerie);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getListeJoursFeries(dateDebut, dateFin)).thenReturn(listJoursFeries);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getNombreSamediOffertSurAnnee(demande.getIdAgent(), 2014, demande.getIdDemande())).thenReturn(1);

		HelperService service = new HelperService();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		Double result = service.getDureeCongeAnnuel(demande, dateReprise, false, null);

		assertEquals(duree, result);
	}

	@Test
	public void getDureeCongeAnnuel_Base_A_returnDimancheAndJourFerieDimanche() {
		Double duree = 4.0;

		Date dateDebut = new DateTime(2014, 12, 4, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 8, 23, 59, 0).toDate();
		Date dateReprise = new DateTime(2014, 12, 9, 0, 0, 0).toDate();

		RefTypeSaisiCongeAnnuel typeSaisi = new RefTypeSaisiCongeAnnuel();
		typeSaisi.setCodeBaseHoraireAbsence("A");
		typeSaisi.setDecompteSamedi(true);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setDateDebut(dateDebut);
		demande.setDateFin(dateFin);
		demande.setTypeSaisiCongeAnnuel(typeSaisi);

		List<JourDto> listJoursFeries = new ArrayList<JourDto>();
		JourDto jourFerie = new JourDto();
		jourFerie.setJour(new DateTime(2014, 12, 7, 0, 0, 0).toDate());
		listJoursFeries.add(jourFerie);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getListeJoursFeries(dateDebut, dateFin)).thenReturn(listJoursFeries);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getNombreSamediOffertSurAnnee(demande.getIdAgent(), 2014, demande.getIdDemande())).thenReturn(1);

		HelperService service = new HelperService();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		Double result = service.getDureeCongeAnnuel(demande, dateReprise, false, null);

		assertEquals(duree, result);
	}

	@Test
	public void getDateDebutCongeAnnuel_withCheckDateDebut_AM() {

		Date dateDebut = new DateTime(2014, 12, 2, 15, 24, 0).toDate();

		RefTypeSaisiCongeAnnuel refTypeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		refTypeSaisiCongeAnnuel.setChkDateDebut(true);

		HelperService service = new HelperService();
		Date result = service.getDateDebutCongeAnnuel(refTypeSaisiCongeAnnuel, dateDebut, true, false);

		assertEquals(new DateTime(2014, 12, 2, 0, 0, 0).toDate(), result);
	}

	@Test
	public void getDateDebutCongeAnnuel_withCheckDateDebut_PM() {

		Date dateDebut = new DateTime(2014, 12, 2, 15, 24, 0).toDate();

		RefTypeSaisiCongeAnnuel refTypeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		refTypeSaisiCongeAnnuel.setChkDateDebut(true);

		HelperService service = new HelperService();
		Date result = service.getDateDebutCongeAnnuel(refTypeSaisiCongeAnnuel, dateDebut, false, true);

		assertEquals(new DateTime(2014, 12, 2, 12, 0, 0).toDate(), result);
	}

	@Test
	public void getDateDebutCongeAnnuel_withCheckDateDebut() {

		Date dateDebut = new DateTime(2014, 12, 2, 15, 24, 0).toDate();

		RefTypeSaisiCongeAnnuel refTypeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		refTypeSaisiCongeAnnuel.setChkDateDebut(true);

		HelperService service = new HelperService();
		Date result = service.getDateDebutCongeAnnuel(refTypeSaisiCongeAnnuel, dateDebut, false, false);

		assertEquals(new DateTime(2014, 12, 2, 15, 24, 0).toDate(), result);
	}

	@Test
	public void getDateDebutCongeAnnuel_withNoCheckDateDebut() {

		Date dateDebut = new DateTime(2014, 12, 2, 15, 24, 0).toDate();

		RefTypeSaisiCongeAnnuel refTypeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		refTypeSaisiCongeAnnuel.setChkDateDebut(false);

		HelperService service = new HelperService();
		Date result = service.getDateDebutCongeAnnuel(refTypeSaisiCongeAnnuel, dateDebut, false, false);

		assertEquals(new DateTime(2014, 12, 2, 0, 0, 0).toDate(), result);
	}

	@Test
	public void getDateFinCongeAnnuel_withDateFin_withCheckDateFin() {

		Date dateDebut = new DateTime(2014, 12, 2, 15, 24, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 2, 15, 24, 0).toDate();
		Date dateReprise = new DateTime(2014, 12, 2, 15, 24, 0).toDate();

		RefTypeSaisiCongeAnnuel refTypeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		refTypeSaisiCongeAnnuel.setCalendarDateFin(true);
		refTypeSaisiCongeAnnuel.setChkDateFin(false);

		HelperService service = new HelperService();
		Date result = service.getDateFinCongeAnnuel(refTypeSaisiCongeAnnuel, dateFin, dateDebut, false, false, dateReprise);

		assertEquals(new DateTime(2014, 12, 2, 23, 59, 59).toDate(), result);
	}

	@Test
	public void getDateFinCongeAnnuel_withDateFin_withNoCheckDateFin_AM() {

		Date dateDebut = new DateTime(2014, 12, 2, 15, 24, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 2, 15, 24, 0).toDate();
		Date dateReprise = new DateTime(2014, 12, 2, 15, 24, 0).toDate();

		RefTypeSaisiCongeAnnuel refTypeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		refTypeSaisiCongeAnnuel.setCalendarDateFin(true);
		refTypeSaisiCongeAnnuel.setChkDateFin(true);

		HelperService service = new HelperService();
		Date result = service.getDateFinCongeAnnuel(refTypeSaisiCongeAnnuel, dateFin, dateDebut, true, false, dateReprise);

		assertEquals(new DateTime(2014, 12, 2, 11, 59, 59).toDate(), result);
	}

	@Test
	public void getDateFinCongeAnnuel_withDateFin_withNoCheckDateFin_PM() {

		Date dateDebut = new DateTime(2014, 12, 2, 15, 24, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 2, 15, 24, 0).toDate();
		Date dateReprise = new DateTime(2014, 12, 2, 15, 24, 0).toDate();

		RefTypeSaisiCongeAnnuel refTypeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		refTypeSaisiCongeAnnuel.setCalendarDateFin(true);
		refTypeSaisiCongeAnnuel.setChkDateFin(true);

		HelperService service = new HelperService();
		Date result = service.getDateFinCongeAnnuel(refTypeSaisiCongeAnnuel, dateFin, dateDebut, false, true, dateReprise);

		assertEquals(new DateTime(2014, 12, 2, 23, 59, 59).toDate(), result);
	}

	@Test
	public void getDateFinCongeAnnuel_withDateReprise() {

		Date dateDebut = new DateTime(2014, 12, 2, 15, 24, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 2, 15, 24, 0).toDate();
		Date dateReprise = new DateTime(2014, 12, 2, 15, 24, 0).toDate();

		RefTypeSaisiCongeAnnuel refTypeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		refTypeSaisiCongeAnnuel.setCalendarDateReprise(true);

		HelperService service = new HelperService();
		Date result = service.getDateFinCongeAnnuel(refTypeSaisiCongeAnnuel, dateFin, dateDebut, false, false, dateReprise);

		assertEquals(new DateTime(2014, 12, 1, 23, 59, 59).toDate(), result);
	}

	@Test
	public void getDateFinCongeAnnuel_returnNull() {

		Date dateDebut = new DateTime(2014, 12, 2, 15, 24, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 2, 15, 24, 0).toDate();
		Date dateReprise = new DateTime(2014, 12, 2, 15, 24, 0).toDate();

		RefTypeSaisiCongeAnnuel refTypeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();

		HelperService service = new HelperService();
		Date result = service.getDateFinCongeAnnuel(refTypeSaisiCongeAnnuel, dateFin, dateDebut, false, false, dateReprise);

		assertNull(result);
	}

	@Test
	public void getDureeCongeAnnuel_baseA() {
		Double duree = 1.0;

		Date dateDebut = new DateTime(2014, 12, 2, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 2, 23, 59, 59).toDate();
		Date dateReprise = new DateTime(2014, 12, 2, 15, 24, 0).toDate();

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeSaisiCongeAnnuel.setCodeBaseHoraireAbsence("A");
		typeSaisiCongeAnnuel.setDecompteSamedi(true);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(dateFin);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getNombreSamediOffertSurAnnee(demande.getIdAgent(), 2014, demande.getIdDemande())).thenReturn(0);

		HelperService service = new HelperService();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		Double result = service.getDureeCongeAnnuel(demande, dateReprise, false, null);

		assertEquals(duree, result);
	}

	@Test
	public void getDureeCongeAnnuel_AvecSamedi_baseA_SamediOffert() {
		Double duree = 1.0;

		Date dateDebut = new DateTime(2014, 12, 5, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 5, 23, 59, 59).toDate();
		Date dateReprise = new DateTime(2014, 12, 5, 15, 24, 0).toDate();

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeSaisiCongeAnnuel.setCodeBaseHoraireAbsence("A");
		typeSaisiCongeAnnuel.setDecompteSamedi(true);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(dateFin);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getNombreSamediOffertSurAnnee(demande.getIdAgent(), 2014, demande.getIdDemande())).thenReturn(0);

		HelperService service = new HelperService();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		Double result = service.getDureeCongeAnnuel(demande, dateReprise, false, null);

		assertEquals(duree, result);
	}

	@Test
	public void getDureeCongeAnnuel_AvecDemiSamedi_baseA_SamediOffert() {
		Double duree = 1.0; // on decompte un jour : vendredi PM et samedi AM

		Date dateDebut = new DateTime(2014, 12, 5, 12, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 5, 23, 59, 59).toDate();
		Date dateReprise = new DateTime(2014, 12, 5, 15, 24, 0).toDate();

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeSaisiCongeAnnuel.setCodeBaseHoraireAbsence("A");
		typeSaisiCongeAnnuel.setDecompteSamedi(true);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(dateFin);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getNombreSamediOffertSurAnnee(demande.getIdAgent(), 2014, demande.getIdDemande())).thenReturn(0);

		HelperService service = new HelperService();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		Double result = service.getDureeCongeAnnuel(demande, dateReprise, false, null);

		assertEquals(duree, result);
	}

	@Test
	public void getDureeCongeAnnuel_AvecSamedi_baseA_SamediNonOffert() {
		Double duree = 2.0;

		Date dateDebut = new DateTime(2014, 12, 5, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 5, 23, 59, 59).toDate();
		Date dateReprise = new DateTime(2014, 12, 5, 15, 24, 0).toDate();

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeSaisiCongeAnnuel.setCodeBaseHoraireAbsence("A");
		typeSaisiCongeAnnuel.setDecompteSamedi(true);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(dateFin);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getNombreSamediOffertSurAnnee(demande.getIdAgent(), 2014, demande.getIdDemande())).thenReturn(1);

		HelperService service = new HelperService();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		Double result = service.getDureeCongeAnnuel(demande, dateReprise, false, null);

		assertEquals(duree, result);
	}

	@Test
	public void getDureeCongeAnnuel_AvecSamedi_baseA_bug31346() {

		Double duree = 1.0;

		Date dateDebut = new DateTime(2016, 6, 18, 12, 0, 0).toDate();
		Date dateFin = new DateTime(2016, 6, 20, 23, 59, 59).toDate();
		Date dateReprise = null;

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeSaisiCongeAnnuel.setCodeBaseHoraireAbsence("A");
		typeSaisiCongeAnnuel.setDecompteSamedi(true);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(dateFin);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getNombreSamediOffertSurAnnee(demande.getIdAgent(), 2014, demande.getIdDemande())).thenReturn(1);

		HelperService service = new HelperService();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		Double result = service.getDureeCongeAnnuel(demande, dateReprise, false, null);

		assertEquals(duree, result);
	}

	@Test
	public void getNombreJourSemaineWithoutFerie_bug31346() {

		Double duree = 0.5;

		Date dateDebut = new DateTime(2016, 6, 18, 12, 0, 0).toDate();
		Date dateFin = new DateTime(2016, 6, 20, 23, 59, 59).toDate();

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setDateDebut(dateDebut);
		demande.setDateFin(dateFin);

		HelperService service = new HelperService();

		Double result = service.getNombreJourSemaineWithoutFerie(demande.getDateDebut(), demande.getDateFin(), DateTimeConstants.SATURDAY,
				new ArrayList<JourDto>());

		assertEquals(duree, result);
	}

	@Test
	public void getNombreJourSemaineWithoutFerie_bug31346_test2() {

		Double duree = 1.0;

		Date dateDebut = new DateTime(2016, 6, 17, 12, 0, 0).toDate();
		Date dateFin = new DateTime(2016, 6, 20, 23, 59, 59).toDate();

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setDateDebut(dateDebut);
		demande.setDateFin(dateFin);

		HelperService service = new HelperService();

		Double result = service.getNombreJourSemaineWithoutFerie(demande.getDateDebut(), demande.getDateFin(), DateTimeConstants.SATURDAY,
				new ArrayList<JourDto>());

		assertEquals(duree, result);
	}

	@Test
	public void getDureeCongeAnnuel_AvecSamedi_baseA_SamediNonOffert_DimancheNonCompte() {
		Double duree = 3.0;

		Date dateDebut = new DateTime(2014, 12, 5, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 8, 23, 59, 59).toDate();
		Date dateReprise = new DateTime(2014, 12, 5, 15, 24, 0).toDate();

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeSaisiCongeAnnuel.setCodeBaseHoraireAbsence("A");
		typeSaisiCongeAnnuel.setDecompteSamedi(true);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(dateFin);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getNombreSamediOffertSurAnnee(demande.getIdAgent(), 2014, demande.getIdDemande())).thenReturn(1);

		HelperService service = new HelperService();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		Double result = service.getDureeCongeAnnuel(demande, dateReprise, false, null);

		assertEquals(duree, result);
	}

	@Test
	public void getDureeCongeAnnuel_AvecSamedi_baseA_SamediNonOffert_JourFerieNonCompte() {
		Double duree = 2.0;

		Date dateDebut = new DateTime(2014, 12, 5, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 8, 23, 59, 59).toDate();
		Date dateReprise = new DateTime(2014, 12, 5, 15, 24, 0).toDate();

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeSaisiCongeAnnuel.setCodeBaseHoraireAbsence("A");
		typeSaisiCongeAnnuel.setDecompteSamedi(true);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(dateFin);

		List<JourDto> listJoursFerie = new ArrayList<JourDto>();
		JourDto jourFerie1 = new JourDto();
		jourFerie1.setJour(new DateTime(2014, 12, 8, 0, 0, 0).toDate());
		listJoursFerie.add(jourFerie1);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getListeJoursFeries(dateDebut, dateFin)).thenReturn(listJoursFerie);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getNombreSamediOffertSurAnnee(demande.getIdAgent(), 2014, demande.getIdDemande())).thenReturn(1);

		HelperService service = new HelperService();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		Double result = service.getDureeCongeAnnuel(demande, dateReprise, false, null);

		assertEquals(duree, result);
	}

	@Test
	public void getDureeCongeAnnuel_baseE() {
		Double duree = 7.0;

		Date dateDebut = new DateTime(2014, 12, 2, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 8, 23, 59, 59).toDate();
		Date dateReprise = new DateTime(2014, 12, 2, 15, 24, 0).toDate();

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeSaisiCongeAnnuel.setCodeBaseHoraireAbsence("E");

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(dateFin);

		HelperService service = new HelperService();
		Double result = service.getDureeCongeAnnuel(demande, dateReprise, false, null);

		assertEquals(duree, result);
	}

	@Test
	public void getDureeCongeAnnuel_baseE_1Jour() {
		Double duree = 1.0;

		Date dateDebut = new DateTime(2014, 12, 2, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 2, 23, 59, 59).toDate();
		Date dateReprise = new DateTime(2014, 12, 2, 15, 24, 0).toDate();

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeSaisiCongeAnnuel.setCodeBaseHoraireAbsence("E");

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(dateFin);

		HelperService service = new HelperService();
		Double result = service.getDureeCongeAnnuel(demande, dateReprise, false, null);

		assertEquals(duree, result);
	}

	@Test
	public void getDureeCongeAnnuel_baseC_1Jour() {
		Double duree = 1.0;

		Date dateDebut = new DateTime(2014, 12, 2, 0, 0, 0).toDate();
		Date dateReprise = new DateTime(2014, 12, 3, 0, 0, 0).toDate();

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeSaisiCongeAnnuel.setCodeBaseHoraireAbsence("C");
		typeSaisiCongeAnnuel.setQuotaMultiple(5);
		typeSaisiCongeAnnuel.setQuotaDecompte(3);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);
		demande.setDateDebut(dateDebut);

		HelperService service = new HelperService();
		Double result = service.getDureeCongeAnnuel(demande, dateReprise, false, null);

		assertEquals(duree, result);
	}

	@Test
	public void getDureeCongeAnnuel_baseC_DureeInferieur3Jours() {
		Double duree = 3.0;

		Date dateDebut = new DateTime(2014, 12, 2, 0, 0, 0).toDate();
		Date dateReprise = new DateTime(2014, 12, 5, 0, 0, 0).toDate();

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeSaisiCongeAnnuel.setCodeBaseHoraireAbsence("C");
		typeSaisiCongeAnnuel.setQuotaMultiple(5);
		typeSaisiCongeAnnuel.setQuotaDecompte(3);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);
		demande.setDateDebut(dateDebut);

		HelperService service = new HelperService();
		Double result = service.getDureeCongeAnnuel(demande, dateReprise, false, null);

		assertEquals(duree, result);
	}

	@Test
	public void getDureeCongeAnnuel_baseC_DureeSuperieur3Jours_InferieurQuota() {
		Double duree = 3.0;

		Date dateDebut = new DateTime(2014, 12, 2, 0, 0, 0).toDate();
		Date dateReprise = new DateTime(2014, 12, 6, 0, 0, 0).toDate();

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeSaisiCongeAnnuel.setCodeBaseHoraireAbsence("C");
		typeSaisiCongeAnnuel.setQuotaMultiple(5);
		typeSaisiCongeAnnuel.setQuotaDecompte(3);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);
		demande.setDateDebut(dateDebut);

		HelperService service = new HelperService();
		Double result = service.getDureeCongeAnnuel(demande, dateReprise, false, null);

		assertEquals(duree, result);
	}

	@Test
	public void getDureeCongeAnnuel_baseC() {
		Double duree = 3.0;

		Date dateDebut = new DateTime(2014, 12, 2, 0, 0, 0).toDate();
		Date dateReprise = new DateTime(2014, 12, 8, 0, 0, 0).toDate();

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeSaisiCongeAnnuel.setCodeBaseHoraireAbsence("C");
		typeSaisiCongeAnnuel.setQuotaMultiple(5);
		typeSaisiCongeAnnuel.setQuotaDecompte(3);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);
		demande.setDateDebut(dateDebut);

		HelperService service = new HelperService();
		Double result = service.getDureeCongeAnnuel(demande, dateReprise, false, null);

		assertEquals(duree, result);
	}

	@Test
	public void getDureeCongeAnnuel_baseC_forceSaisieManuelle() {

		Date dateDebut = new DateTime(2014, 12, 2, 0, 0, 0).toDate();
		Date dateReprise = new DateTime(2014, 12, 8, 0, 0, 0).toDate();

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeSaisiCongeAnnuel.setCodeBaseHoraireAbsence("C");
		typeSaisiCongeAnnuel.setQuotaMultiple(5);
		typeSaisiCongeAnnuel.setQuotaDecompte(3);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);
		demande.setDateDebut(dateDebut);

		HelperService service = new HelperService();
		Double result = service.getDureeCongeAnnuel(demande, dateReprise, true, 2.0);

		assertEquals(new Double(2.0), result);
	}

	// #15248 cas de test concret en recette
	// avec des samedis chomes
	@Test
	public void getDureeCongeAnnuel_Base_A_withSamediChomes() {

		Double duree = 10.0;

		Date dateDebut = new DateTime(2015, 5, 4, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2015, 5, 20, 23, 59, 0).toDate();

		RefTypeSaisiCongeAnnuel typeSaisi = new RefTypeSaisiCongeAnnuel();
		typeSaisi.setCodeBaseHoraireAbsence("A");
		typeSaisi.setDecompteSamedi(true);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setDateDebut(dateDebut);
		demande.setDateFin(dateFin);
		demande.setTypeSaisiCongeAnnuel(typeSaisi);

		List<JourDto> listJoursFeries = new ArrayList<JourDto>();
		JourDto mai8 = new JourDto();
		mai8.setJour(new DateTime(2015, 5, 8, 0, 0, 0).toDate());
		JourDto mai9 = new JourDto();
		mai9.setJour(new DateTime(2015, 5, 9, 0, 0, 0).toDate());
		JourDto mai14 = new JourDto();
		mai14.setJour(new DateTime(2015, 5, 14, 0, 0, 0).toDate());
		JourDto mai15 = new JourDto();
		mai15.setJour(new DateTime(2015, 5, 15, 0, 0, 0).toDate());
		JourDto mai16 = new JourDto();
		mai16.setJour(new DateTime(2015, 5, 16, 0, 0, 0).toDate());
		listJoursFeries.addAll(Arrays.asList(mai8, mai9, mai14, mai15, mai16));

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getListeJoursFeries(dateDebut, dateFin)).thenReturn(listJoursFeries);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getNombreSamediOffertSurAnnee(demande.getIdAgent(), 2015, demande.getIdDemande())).thenReturn(1);

		HelperService service = new HelperService();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		Double result = service.getDureeCongeAnnuel(demande, null, false, null);

		assertEquals(duree, result);
	}

	@Test
	public void isSamediDecompte_returnOk() {
		Double duree = 1.0;

		Date dateDebut = new DateTime(2014, 12, 2, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 7, 23, 59, 59).toDate();

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeSaisiCongeAnnuel.setDecompteSamedi(true);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(dateFin);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);

		HelperService service = new HelperService();
		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		Double result = service.getNombreSamediDecompte(demande);

		assertEquals(duree, result);
	}

	@Test
	public void isSamediDecompte_returnKo() {
		Double duree = 0.0;

		Date dateDebut = new DateTime(2014, 12, 2, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 4, 23, 59, 59).toDate();

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeSaisiCongeAnnuel.setDecompteSamedi(true);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(dateFin);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);

		HelperService service = new HelperService();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		Double result = service.getNombreSamediDecompte(demande);

		assertEquals(duree, result);
	}

	@Test
	public void isNotSamediDecompte() {
		Double duree = 0.0;

		Date dateDebut = new DateTime(2014, 12, 2, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 7, 23, 59, 59).toDate();

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeSaisiCongeAnnuel.setDecompteSamedi(false);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(dateFin);

		HelperService service = new HelperService();
		Double result = service.getNombreSamediDecompte(demande, null);

		assertEquals(duree, result);
	}

	@Test
	public void getNombreSamediDecompte_2Samedi() {
		Double duree = 2.0;

		Date dateDebut = new DateTime(2014, 12, 2, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 15, 23, 59, 59).toDate();

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeSaisiCongeAnnuel.setDecompteSamedi(true);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(dateFin);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);

		HelperService service = new HelperService();
		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		Double result = service.getNombreSamediDecompte(demande);

		assertEquals(duree, result);
	}

	@Test
	public void getNombreSamediDecompte_2Samedi_dont1SamediFerie() {
		Double duree = 1.0;

		Date dateDebut = new DateTime(2014, 12, 2, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 15, 23, 59, 59).toDate();

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeSaisiCongeAnnuel.setDecompteSamedi(true);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(dateFin);

		List<JourDto> listJoursFeries = new ArrayList<>();
		JourDto jourFerie = new JourDto();
		jourFerie.setJour(new DateTime(2014, 12, 13, 0, 0, 0).toDate());
		listJoursFeries.add(jourFerie);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getListeJoursFeries(dateDebut, dateFin)).thenReturn(listJoursFeries);

		HelperService service = new HelperService();
		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		Double result = service.getNombreSamediDecompte(demande);

		assertEquals(duree, result);
	}

	@Test
	public void getNombreSamediDecompte_1SamediEtDemi() {
		Double duree = 1.5;
		
		RefEtat etatValide = new RefEtat();
		etatValide.setIdRefEtat(8);
		RefEtat etatPrise = new RefEtat();
		etatPrise.setIdRefEtat(6);

		Date dateDebut = new DateTime(2014, 12, 5, 12, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 15, 23, 59, 59).toDate();

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeSaisiCongeAnnuel.setDecompteSamedi(true);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(dateFin);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);

		HelperService service = new HelperService();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);

		// On initialise la liste dans le premier appel. Ensuite, c'est gardé en mémoire, pas besoin de le refaire pour les autres tests
		Mockito.when(filtreRepository.getEntity(RefEtat.class, RefEtatEnum.VALIDEE.getCodeEtat())).thenReturn(etatValide);
		Mockito.when(filtreRepository.getEntity(RefEtat.class, RefEtatEnum.PRISE.getCodeEtat())).thenReturn(etatPrise);

		Double result = service.getNombreSamediDecompte(demande);

		assertEquals(duree, result);
	}

	@Test
	public void getNombreSamediDecompte_1Samedi_repriseVendrediMidi() {
		Double duree = 1.0;

		Date dateDebut = new DateTime(2014, 12, 4, 12, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 12, 11, 59, 59).toDate();

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeSaisiCongeAnnuel.setDecompteSamedi(true);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(dateFin);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);

		HelperService service = new HelperService();
		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		Double result = service.getNombreSamediDecompte(demande);

		assertEquals(duree, result);
	}

	@Test
	public void getNombreSamediDecompte_1Samedi_congeVendredi() {
		Double duree = 1.0;

		Date dateDebut = new DateTime(2014, 12, 12, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 12, 23, 59, 59).toDate();

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeSaisiCongeAnnuel.setDecompteSamedi(true);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(dateFin);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);

		HelperService service = new HelperService();
		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		Double result = service.getNombreSamediDecompte(demande);

		assertEquals(duree, result);
	}

	@Test
	public void getNombreSamediDecompte_1Samedi_congeJeudi_VendrediFerie() {
		Double duree = 1.0;

		Date dateDebut = new DateTime(2014, 12, 11, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 11, 23, 59, 59).toDate();

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeSaisiCongeAnnuel.setDecompteSamedi(true);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(dateFin);

		List<JourDto> listJoursFeries = new ArrayList<>();
		JourDto jourFerie = new JourDto();
		jourFerie.setJour(new DateTime(2014, 12, 12, 0, 0, 0).toDate());
		listJoursFeries.add(jourFerie);

		HelperService service = new HelperService();
		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		Double result = service.getNombreSamediDecompte(demande, listJoursFeries);

		assertEquals(duree, result);
	}

	@Test
	public void getNombreSamediDecompte_1Samedi_congeJeudiAuJeudi_VendrediFerie() {
		Double duree = 1.0;

		Date dateDebut = new DateTime(2014, 12, 11, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 18, 23, 59, 59).toDate();

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeSaisiCongeAnnuel.setDecompteSamedi(true);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(dateFin);

		List<JourDto> listJoursFeries = new ArrayList<>();
		JourDto jourFerie = new JourDto();
		jourFerie.setJour(new DateTime(2014, 12, 12, 0, 0, 0).toDate());
		listJoursFeries.add(jourFerie);

		HelperService service = new HelperService();
		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		Double result = service.getNombreSamediDecompte(demande, listJoursFeries);

		assertEquals(duree, result);
	}

	@Test
	public void getNombreSamediDecompte_1Samedi_congeJeudiAM_VendrediFerie() {
		Double duree = 0.0;

		Date dateDebut = new DateTime(2014, 12, 11, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 11, 11, 59, 59).toDate();

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeSaisiCongeAnnuel.setDecompteSamedi(true);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(dateFin);

		List<JourDto> listJoursFeries = new ArrayList<>();
		JourDto jourFerie = new JourDto();
		jourFerie.setJour(new DateTime(2014, 12, 12, 0, 0, 0).toDate());
		listJoursFeries.add(jourFerie);

		HelperService service = new HelperService();
		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		Double result = service.getNombreSamediDecompte(demande, listJoursFeries);

		assertEquals(duree, result);
	}

	@Test
	public void getNombreSamediDecompte_1Samedi_congeJeudiPM_VendrediFerie() {
		Double duree = 0.5;

		Date dateDebut = new DateTime(2014, 12, 11, 12, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 11, 23, 59, 59).toDate();

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeSaisiCongeAnnuel.setDecompteSamedi(true);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(dateFin);

		List<JourDto> listJoursFeries = new ArrayList<>();
		JourDto jourFerie = new JourDto();
		jourFerie.setJour(new DateTime(2014, 12, 12, 0, 0, 0).toDate());
		listJoursFeries.add(jourFerie);

		HelperService service = new HelperService();
		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		Double result = service.getNombreSamediDecompte(demande, listJoursFeries);

		assertEquals(duree, result);
	}

	@Test
	public void getNombreSamediDecompte_0Samedi_congeJeudi_VendrediFerieEtSamediChome() {
		Double duree = 0.0;

		Date dateDebut = new DateTime(2014, 12, 11, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 11, 23, 59, 59).toDate();

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeSaisiCongeAnnuel.setDecompteSamedi(true);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(dateFin);

		List<JourDto> listJoursFeries = new ArrayList<>();
		JourDto jourFerie = new JourDto();
		jourFerie.setJour(new DateTime(2014, 12, 12, 0, 0, 0).toDate());
		listJoursFeries.add(jourFerie);
		JourDto jourFerie2 = new JourDto();
		jourFerie2.setJour(new DateTime(2014, 12, 13, 0, 0, 0).toDate());
		listJoursFeries.add(jourFerie2);

		HelperService service = new HelperService();
		Double result = service.getNombreSamediDecompte(demande, listJoursFeries);

		assertEquals(duree, result);
	}

	@Test
	public void getNombreSamediDecompte_DemiSamedi_repriseVendrediMidi() {
		Double duree = 0.5;

		Date dateDebut = new DateTime(2014, 12, 5, 12, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 12, 11, 59, 59).toDate();

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeSaisiCongeAnnuel.setDecompteSamedi(true);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(dateFin);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);

		HelperService service = new HelperService();
		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		Double result = service.getNombreSamediDecompte(demande);

		assertEquals(duree, result);
	}

	@Test
	public void getNombreSamediDecompte_0Samedi_repriseVendrediMidi() {
		Double duree = 0.0;

		Date dateDebut = new DateTime(2014, 12, 8, 12, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 12, 11, 59, 59).toDate();

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeSaisiCongeAnnuel.setDecompteSamedi(true);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(dateFin);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);

		HelperService service = new HelperService();
		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		Double result = service.getNombreSamediDecompte(demande);

		assertEquals(duree, result);
	}

	@Test
	public void getNombreSamediDecompte_CA_DemiJournee_VendrediMatin() {
		Double duree = 0.0;

		Date dateDebut = new DateTime(2014, 12, 12, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 12, 11, 59, 59).toDate();

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeSaisiCongeAnnuel.setDecompteSamedi(true);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(dateFin);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);

		HelperService service = new HelperService();
		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		Double result = service.getNombreSamediDecompte(demande);

		assertEquals(duree, result);
	}

	@Test
	public void getNombreSamediDecompte_DemiSamedi() {
		Double duree = 0.5;

		Date dateDebut = new DateTime(2014, 12, 5, 12, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 5, 23, 59, 59).toDate();

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeSaisiCongeAnnuel.setDecompteSamedi(true);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(dateFin);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);

		HelperService service = new HelperService();
		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		Double result = service.getNombreSamediDecompte(demande);

		assertEquals(duree, result);
	}

	@Test
	public void getNombreSamediDecompte_DemiSamediBis() {
		Double duree = 0.0;

		Date dateDebut = new DateTime(2014, 12, 5, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 5, 11, 59, 59).toDate();

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeSaisiCongeAnnuel.setDecompteSamedi(true);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(dateFin);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);

		HelperService service = new HelperService();
		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		Double result = service.getNombreSamediDecompte(demande);

		assertEquals(duree, result);
	}

	@Test
	public void getNombreSamediDecompte_samediFerie() {
		Double duree = 0.0;

		Date dateDebut = new DateTime(2014, 12, 5, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 5, 23, 59, 59).toDate();

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeSaisiCongeAnnuel.setDecompteSamedi(true);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(dateFin);

		List<JourDto> listJoursFeries = new ArrayList<>();
		JourDto jourFerie = new JourDto();
		jourFerie.setJour(new DateTime(2014, 12, 6, 0, 0, 0).toDate());
		listJoursFeries.add(jourFerie);

		HelperService service = new HelperService();
		Double result = service.getNombreSamediDecompte(demande, listJoursFeries);

		assertEquals(duree, result);
	}

	@Test
	public void getNombreDimanche_2Dimanche() {

		Double duree = 2.0;

		Date dateDebut = new DateTime(2015, 1, 5, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2015, 1, 19, 11, 59, 59).toDate();

		HelperService service = new HelperService();
		Double result = service.getNombreDimanche(dateDebut, dateFin);

		assertEquals(duree, result);
	}

	@Test
	public void getNombreDimanche_3Dimanche_DebutDimancheMidi() {

		Double duree = 2.5;

		Date dateDebut = new DateTime(2015, 1, 4, 12, 0, 0).toDate();
		Date dateFin = new DateTime(2015, 1, 19, 11, 59, 59).toDate();

		HelperService service = new HelperService();
		Double result = service.getNombreDimanche(dateDebut, dateFin);

		assertEquals(duree, result);
	}

	@Test
	public void getNombreDimanche_3Dimanche_FinDimancheMidi() {

		Double duree = 2.0;

		Date dateDebut = new DateTime(2015, 1, 4, 12, 0, 0).toDate();
		Date dateFin = new DateTime(2015, 1, 18, 11, 59, 59).toDate();

		HelperService service = new HelperService();
		Double result = service.getNombreDimanche(dateDebut, dateFin);

		assertEquals(duree, result);
	}

	@Test
	public void getNombreDimanche_1Dimanche_DebutDimanche_FinDimancheMidi() {

		Double duree = 0.5;

		Date dateDebut = new DateTime(2015, 1, 18, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2015, 1, 18, 11, 59, 59).toDate();

		HelperService service = new HelperService();
		Double result = service.getNombreDimanche(dateDebut, dateFin);

		assertEquals(duree, result);
	}

	@Test
	public void getNombreDimanche_1Dimanche_DebutDimancheMidi_FinDimancheSoir() {

		Double duree = 0.5;

		Date dateDebut = new DateTime(2015, 1, 18, 12, 0, 0).toDate();
		Date dateFin = new DateTime(2015, 1, 18, 23, 59, 59).toDate();

		HelperService service = new HelperService();
		Double result = service.getNombreDimanche(dateDebut, dateFin);

		assertEquals(duree, result);
	}

	@Test
	public void getNombreJoursFeriesChomes_1JourFerie() {

		Double nombre = 1.0;

		Date dateDebut = new DateTime(2015, 1, 1, 12, 0, 0).toDate();
		Date dateFin = new DateTime(2015, 1, 18, 23, 59, 59).toDate();

		List<JourDto> listJoursFeries = new ArrayList<JourDto>();
		JourDto jourFerie = new JourDto();
		jourFerie.setJour(new DateTime(2015, 1, 1, 12, 0, 0).toDate());
		listJoursFeries.add(jourFerie);

		HelperService service = new HelperService();
		Double result = service.getNombreJoursFeriesChomes(dateDebut, dateFin, listJoursFeries);

		assertEquals(nombre, result);
	}

	@Test
	public void getNombreJoursFeriesChomes_2JoursFerie() {

		Double nombre = 2.0;

		Date dateDebut = new DateTime(2015, 1, 1, 12, 0, 0).toDate();
		Date dateFin = new DateTime(2015, 1, 18, 23, 59, 59).toDate();

		List<JourDto> listJoursFeries = new ArrayList<JourDto>();
		JourDto jourFerie = new JourDto();
		jourFerie.setJour(new DateTime(2015, 1, 1, 0, 0, 0).toDate());
		JourDto jourFerie2 = new JourDto();
		jourFerie2.setJour(new DateTime(2015, 1, 10, 0, 0, 0).toDate());
		listJoursFeries.add(jourFerie);
		listJoursFeries.add(jourFerie2);

		HelperService service = new HelperService();
		Double result = service.getNombreJoursFeriesChomes(dateDebut, dateFin, listJoursFeries);

		assertEquals(nombre, result);
	}

	@Test
	public void getNombreJoursFeriesChomes_1JourFerie_SameDateDebutEtFin() {

		Double nombre = 1.0;

		Date dateDebut = new DateTime(2015, 1, 19, 12, 0, 0).toDate();
		Date dateFin = new DateTime(2015, 1, 19, 23, 59, 59).toDate();

		List<JourDto> listJoursFeries = new ArrayList<JourDto>();
		JourDto jourFerie = new JourDto();
		jourFerie.setJour(new DateTime(2015, 1, 19, 0, 0, 0).toDate());
		listJoursFeries.add(jourFerie);

		HelperService service = new HelperService();
		Double result = service.getNombreJoursFeriesChomes(dateDebut, dateFin, listJoursFeries);

		assertEquals(nombre, result);
	}

	@Test
	public void getNombreJoursFeriesChomes_1JourFerie_SameDateDebutEtFin_DateDimanche() {

		Double nombre = 0.0;

		Date dateDebut = new DateTime(2015, 1, 18, 12, 0, 0).toDate();
		Date dateFin = new DateTime(2015, 1, 18, 23, 59, 59).toDate();

		List<JourDto> listJoursFeries = new ArrayList<JourDto>();
		JourDto jourFerie = new JourDto();
		jourFerie.setJour(new DateTime(2015, 1, 18, 0, 0, 0).toDate());
		listJoursFeries.add(jourFerie);

		HelperService service = new HelperService();
		Double result = service.getNombreJoursFeriesChomes(dateDebut, dateFin, listJoursFeries);

		assertEquals(nombre, result);
	}

	@Test
	public void getNombreSamediOffert_samediDejaOffert() {

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setDateDebut(new DateTime(2014, 12, 1, 0, 0, 0).toDate());
		demande.setDateFin(new DateTime(2014, 12, 31, 0, 0, 0).toDate());

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getNombreSamediOffertSurAnnee(demande.getIdAgent(), new DateTime(demande.getDateDebut()).getYear(),
				demande.getIdDemande())).thenReturn(1);

		HelperService service = new HelperService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		Double result = service.getNombreSamediOffert(demande, null);

		assertEquals(result.doubleValue(), 0, 0);
	}

	@Test
	public void getNombreSamediOffert_samediOffertOK() {

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeSaisiCongeAnnuel.setDecompteSamedi(true);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);
		demande.setDateDebut(new DateTime(2014, 12, 1, 0, 0, 0).toDate());
		demande.setDateFin(new DateTime(2014, 12, 31, 0, 0, 0).toDate());

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getNombreSamediOffertSurAnnee(demande.getIdAgent(), new DateTime(demande.getDateDebut()).getYear(),
				demande.getIdDemande())).thenReturn(0);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);

		HelperService service = new HelperService();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		Double result = service.getNombreSamediOffert(demande);

		assertEquals(result.doubleValue(), 1, 0);
	}

	@Test
	public void getNombreSamediOffert_samediNonOffert_demiSamedi() {

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeSaisiCongeAnnuel.setDecompteSamedi(true);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);
		demande.setDateDebut(new DateTime(2014, 12, 5, 12, 0, 0).toDate());
		demande.setDateFin(new DateTime(2014, 12, 10, 23, 59, 0).toDate());

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getNombreSamediOffertSurAnnee(demande.getIdAgent(), new DateTime(demande.getDateDebut()).getYear(),
				demande.getIdDemande())).thenReturn(0);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);

		HelperService service = new HelperService();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		Double result = service.getNombreSamediOffert(demande);

		assertEquals(result.doubleValue(), 0, 0);
	}

	@Test
	public void getHeureMinuteToString_Negatif() {

		HelperService service = new HelperService();
		String result = service.getHeureMinuteToString(-50);

		assertEquals(result, "50m");
	}

	@Test
	public void getHeureMinuteToString_Positif() {

		HelperService service = new HelperService();
		String result = service.getHeureMinuteToString(150);

		assertEquals(result, "2h30m");
	}

	@Test
	public void calculNombreJours_Zero() {
		Date dateDeb = new DateTime(2010, 01, 01, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2010, 01, 01, 0, 0, 0).toDate();

		HelperService service = new HelperService();
		double result = service.calculNombreJours(dateDeb, dateFin);

		assertEquals((int) result, 0);
	}

	@Test
	public void calculNombreJours_DeuxBis() {
		Date dateDeb = new DateTime(2016, 7, 06, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2016, 07, 07, 23, 59, 59).toDate();

		HelperService service = new HelperService();
		double result = service.calculNombreJours(dateDeb, dateFin);

		assertEquals((int) result, 2);
	}

	@Test
	public void calculNombreJours_Deux() {
		Date dateDeb = new DateTime(2010, 01, 01, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2010, 01, 03, 0, 0, 0).toDate();

		HelperService service = new HelperService();
		double result = service.calculNombreJours(dateDeb, dateFin);

		assertEquals((int) result, 2);
	}
}
