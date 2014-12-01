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
import nc.noumea.mairie.abs.domain.RefTypeSaisiCongeAnnuel;
import nc.noumea.mairie.abs.domain.RefUnitePeriodeQuota;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.domain.Spcarr;
import nc.noumea.mairie.ws.ISirhWSConsumer;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

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

		HelperService service = new HelperService();
		Double result = service.getDuree(typeSaisi, calDebut.getTime(), calFin.getTime(), duree);

		assertEquals(result, 0, 5);
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

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isJourHoliday(Mockito.any(Date.class))).thenReturn(false);

		HelperService service = new HelperService();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		Double result = service.getDuree(typeSaisi, new Date(), new Date());

		assertEquals(duree, result);
	}

	@Test
	public void getDureeCongeAnnuel_Base_D_returnDimanche() {
		Double duree = 12.0;

		Date dateDebut = new DateTime(2014, 12, 1, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 14, 23, 59, 0).toDate();

		RefTypeSaisiCongeAnnuel typeSaisi = new RefTypeSaisiCongeAnnuel();
		typeSaisi.setCodeBaseHoraireAbsence("D");

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isJourHoliday(Mockito.any(Date.class))).thenReturn(false);

		HelperService service = new HelperService();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		Double result = service.getDuree(typeSaisi, dateDebut, dateFin);

		assertEquals(duree, result);
	}

	@Test
	public void getDureeCongeAnnuel_Base_A_returnDimancheAndJourFerie() {
		Double duree = 3.0;

		Date dateDebut = new DateTime(2014, 12, 4, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 8, 23, 59, 0).toDate();

		RefTypeSaisiCongeAnnuel typeSaisi = new RefTypeSaisiCongeAnnuel();
		typeSaisi.setCodeBaseHoraireAbsence("A");

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isJourHoliday(new DateTime(2014, 12, 4, 0, 0, 0).toDate())).thenReturn(false);
		Mockito.when(sirhWSConsumer.isJourHoliday(new DateTime(2014, 12, 5, 0, 0, 0).toDate())).thenReturn(false);
		Mockito.when(sirhWSConsumer.isJourHoliday(new DateTime(2014, 12, 6, 0, 0, 0).toDate())).thenReturn(false);
		Mockito.when(sirhWSConsumer.isJourHoliday(new DateTime(2014, 12, 7, 0, 0, 0).toDate())).thenReturn(false);
		Mockito.when(sirhWSConsumer.isJourHoliday(new DateTime(2014, 12, 8, 0, 0, 0).toDate())).thenReturn(true);
		Mockito.when(sirhWSConsumer.isJourHoliday(new DateTime(2014, 12, 9, 0, 0, 0).toDate())).thenReturn(false);

		HelperService service = new HelperService();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		Double result = service.getDuree(typeSaisi, dateDebut, dateFin);

		assertEquals(duree, result);
	}

	@Test
	public void getDureeCongeAnnuel_Base_A_returnDimancheAndJourFerieDimanche() {
		Double duree = 4.0;

		Date dateDebut = new DateTime(2014, 12, 4, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 8, 23, 59, 0).toDate();

		RefTypeSaisiCongeAnnuel typeSaisi = new RefTypeSaisiCongeAnnuel();
		typeSaisi.setCodeBaseHoraireAbsence("A");

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isJourHoliday(new DateTime(2014, 12, 4, 0, 0, 0).toDate())).thenReturn(false);
		Mockito.when(sirhWSConsumer.isJourHoliday(new DateTime(2014, 12, 5, 0, 0, 0).toDate())).thenReturn(false);
		Mockito.when(sirhWSConsumer.isJourHoliday(new DateTime(2014, 12, 6, 0, 0, 0).toDate())).thenReturn(false);
		Mockito.when(sirhWSConsumer.isJourHoliday(new DateTime(2014, 12, 7, 0, 0, 0).toDate())).thenReturn(true);
		Mockito.when(sirhWSConsumer.isJourHoliday(new DateTime(2014, 12, 8, 0, 0, 0).toDate())).thenReturn(false);
		Mockito.when(sirhWSConsumer.isJourHoliday(new DateTime(2014, 12, 9, 0, 0, 0).toDate())).thenReturn(false);

		HelperService service = new HelperService();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		Double result = service.getDuree(typeSaisi, dateDebut, dateFin);

		assertEquals(duree, result);
	}

}
