package nc.noumea.mairie.abs.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nc.noumea.mairie.abs.domain.AgentAsaA48Count;
import nc.noumea.mairie.abs.domain.AgentAsaA52Count;
import nc.noumea.mairie.abs.domain.AgentAsaA53Count;
import nc.noumea.mairie.abs.domain.AgentAsaA54Count;
import nc.noumea.mairie.abs.domain.AgentAsaA55Count;
import nc.noumea.mairie.abs.domain.AgentCongeAnnuelCount;
import nc.noumea.mairie.abs.domain.AgentCount;
import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;
import nc.noumea.mairie.abs.domain.AgentRecupCount;
import nc.noumea.mairie.abs.domain.AgentReposCompCount;
import nc.noumea.mairie.abs.domain.AgentWeekRecup;
import nc.noumea.mairie.abs.domain.OrganisationSyndicale;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/META-INF/spring/applicationContext-test.xml" })
public class CounterRepositoryTest {

	@Autowired
	CounterRepository repository;

	@PersistenceContext(unitName = "absPersistenceUnit")
	EntityManager absEntityManager;

	@Test
	@Transactional("absTransactionManager")
	public void getAgentRecupCount_NoAgent_ReturnNull() {

		// When
		AgentRecupCount result = repository.getAgentCounter(AgentRecupCount.class, 9008767);

		// Then
		assertNull(result);

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getAgentRecupCount_1Agent_ReturnRecord() {

		// Given
		AgentRecupCount record = new AgentRecupCount();
		record.setIdAgent(9008767);
		record.setTotalMinutes(90);
		absEntityManager.persist(record);

		// When
		AgentRecupCount result = repository.getAgentCounter(AgentRecupCount.class, 9008767);

		// Then
		assertNotNull(result);
		assertEquals(result, record);

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getWeekRecupForAgentAndDate_NoResultForWeek_ReturnNull() {

		// Given
		AgentWeekRecup r1 = new AgentWeekRecup();
		r1.setIdAgent(9008989);
		r1.setDateMonday(new LocalDate(2013, 11, 11).toDate());
		absEntityManager.persist(r1);

		// When
		AgentWeekRecup result = repository.getWeekHistoForAgentAndDate(AgentWeekRecup.class, 9008990, new LocalDate(
				2013, 11, 11).toDate());

		// Then
		assertNull(result);

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getWeekRecupForAgentAndDate_NoResultForAgent_ReturnNull() {

		// Given
		AgentWeekRecup r1 = new AgentWeekRecup();
		r1.setIdAgent(9008989);
		r1.setDateMonday(new LocalDate(2013, 11, 11).toDate());
		absEntityManager.persist(r1);

		// When
		AgentWeekRecup result = repository.getWeekHistoForAgentAndDate(AgentWeekRecup.class, 9008989, new LocalDate(
				2013, 11, 18).toDate());

		// Then
		assertNull(result);

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getWeekRecupForAgentAndDate_1ResultForAgent_ReturnNull() {

		// Given
		AgentWeekRecup r1 = new AgentWeekRecup();
		r1.setIdAgent(9008989);
		r1.setDateMonday(new LocalDate(2013, 11, 11).toDate());
		absEntityManager.persist(r1);

		// When
		AgentWeekRecup result = repository.getWeekHistoForAgentAndDate(AgentWeekRecup.class, 9008989, new LocalDate(
				2013, 11, 11).toDate());

		// Then
		assertEquals(result, r1);

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getListAgentReposCompCountForResetAnneePrcd() {

		AgentReposCompCount arcc = new AgentReposCompCount();
		arcc.setIdAgent(9005138);
		arcc.setLastModification(new Date());
		absEntityManager.persist(arcc);

		AgentReposCompCount arcc2 = new AgentReposCompCount();
		arcc2.setIdAgent(9005138);
		arcc2.setLastModification(new Date());
		arcc2.setTotalMinutesAnneeN1(10);
		absEntityManager.persist(arcc2);

		AgentReposCompCount arcc3 = new AgentReposCompCount();
		arcc3.setIdAgent(9005138);
		arcc3.setLastModification(new Date());
		arcc3.setTotalMinutesAnneeN1(0);
		absEntityManager.persist(arcc3);

		List<Integer> result = repository.getListAgentReposCompCountForResetAnneePrcd();

		assertEquals(1, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getListAgentReposCompCountForResetAnneeEnCours() {

		AgentReposCompCount arcc = new AgentReposCompCount();
		arcc.setIdAgent(9005138);
		arcc.setLastModification(new Date());
		absEntityManager.persist(arcc);

		AgentReposCompCount arcc2 = new AgentReposCompCount();
		arcc2.setIdAgent(9005138);
		arcc2.setLastModification(new Date());
		arcc2.setTotalMinutes(10);
		absEntityManager.persist(arcc2);

		AgentReposCompCount arcc3 = new AgentReposCompCount();
		arcc3.setIdAgent(9005138);
		arcc3.setLastModification(new Date());
		arcc3.setTotalMinutes(0);
		absEntityManager.persist(arcc3);

		List<Integer> result = repository.getListAgentReposCompCountForResetAnneeEnCours();

		assertEquals(1, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getAgentCounterByDate_NoAgent_ReturnNull() {

		// Given
		AgentAsaA48Count record = new AgentAsaA48Count();
		record.setIdAgent(9008767);
		record.setTotalJours(7.0);
		record.setDateDebut(new DateTime(2013, 1, 1, 0, 0, 0).toDate());
		record.setDateFin(new DateTime(2013, 12, 31, 0, 0, 0).toDate());
		absEntityManager.persist(record);

		// When
		AgentAsaA48Count result = repository.getAgentCounterByDate(AgentAsaA48Count.class, 9008767, new DateTime(2014,
				1, 1, 0, 0, 0).toDate());

		// Then
		assertNull(result);

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getAgentCounterByDate_1Agent_ReturnRecord() {

		// Given
		AgentAsaA48Count record = new AgentAsaA48Count();
		record.setIdAgent(9008767);
		record.setTotalJours(7.0);
		record.setDateDebut(new DateTime(2014, 1, 1, 0, 0, 0).toDate());
		record.setDateFin(new DateTime(2014, 12, 31, 0, 0, 0).toDate());
		absEntityManager.persist(record);

		// When
		AgentAsaA48Count result = repository.getAgentCounterByDate(AgentAsaA48Count.class, 9008767, new DateTime(2014,
				1, 1, 0, 0, 0).toDate());

		// Then
		assertNotNull(result);
		assertEquals(result, record);

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getListCounter_ReturnEmptyList() {

		// When
		List<AgentAsaA48Count> result = repository.getListCounter(AgentAsaA48Count.class);

		// Then
		assertEquals(0, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getListCounter_ReturnListCompteur() {

		// Given
		AgentAsaA48Count record = new AgentAsaA48Count();
		record.setIdAgent(9001767);
		record.setTotalJours(7.0);
		record.setDateDebut(new DateTime(2014, 1, 1, 0, 0, 0).toDate());
		record.setDateFin(new DateTime(2014, 12, 31, 0, 0, 0).toDate());
		absEntityManager.persist(record);
		AgentAsaA48Count record2 = new AgentAsaA48Count();
		record2.setIdAgent(9005138);
		record2.setTotalJours(10.0);
		record2.setDateDebut(new DateTime(2014, 1, 1, 0, 0, 0).toDate());
		record2.setDateFin(new DateTime(2014, 12, 31, 0, 0, 0).toDate());
		absEntityManager.persist(record2);

		// When
		List<AgentAsaA48Count> result = repository.getListCounter(AgentAsaA48Count.class);

		// Then
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals(record.getTotalJours(), result.get(0).getTotalJours());
		assertEquals(record.getIdAgent(), result.get(0).getIdAgent());
		assertEquals(record2.getTotalJours(), result.get(1).getTotalJours());
		assertEquals(record2.getIdAgent(), result.get(1).getIdAgent());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getListHisto_ReturnEmptyList() {
		AgentCount count = new AgentCount();
		count.setIdAgentCount(1);
		// When
		List<AgentHistoAlimManuelle> result = repository.getListHisto(9005138, count);

		// Then
		assertEquals(0, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getListHisto_ReturnListCompteur() {

		// Given
		RefTypeAbsence type = new RefTypeAbsence();
		absEntityManager.persist(type);

		AgentAsaA48Count count = new AgentAsaA48Count();
		count.setTotalJours(10.0);
		count.setIdAgent(9005138);
		count.setDateDebut(new Date());
		count.setDateFin(new Date());
		count.setLastModification(new Date());
		absEntityManager.persist(count);

		AgentHistoAlimManuelle record = new AgentHistoAlimManuelle();
		record.setIdAgent(9005138);
		record.setIdAgentConcerne(9001767);
		record.setText("1er text");
		record.setType(type);
		record.setCompteurAgent(count);
		absEntityManager.persist(record);

		AgentHistoAlimManuelle record2 = new AgentHistoAlimManuelle();
		record2.setIdAgent(9005138);
		record2.setIdAgentConcerne(9001768);
		record2.setText("2eme text");
		record2.setType(type);
		record2.setCompteurAgent(count);
		absEntityManager.persist(record2);

		// When
		List<AgentHistoAlimManuelle> result = repository.getListHisto(9001767, count);

		// Then
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(record.getText(), result.get(0).getText());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getAgentCounterA54ByDate_NoAgent_ReturnNull() {

		// Given
		AgentAsaA54Count record = new AgentAsaA54Count();
		record.setIdAgent(9008767);
		record.setTotalJours(7.0);
		record.setDateDebut(new DateTime(2013, 1, 1, 0, 0, 0).toDate());
		record.setDateFin(new DateTime(2013, 12, 31, 0, 0, 0).toDate());
		absEntityManager.persist(record);

		// When
		AgentAsaA54Count result = repository.getAgentCounterByDate(AgentAsaA54Count.class, 9008767, new DateTime(2014,
				1, 1, 0, 0, 0).toDate());

		// Then
		assertNull(result);

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getAgentCounterA54ByDate_1Agent_ReturnRecord() {

		// Given
		AgentAsaA54Count record = new AgentAsaA54Count();
		record.setIdAgent(9008767);
		record.setTotalJours(7.0);
		record.setDateDebut(new DateTime(2014, 1, 1, 0, 0, 0).toDate());
		record.setDateFin(new DateTime(2014, 12, 31, 0, 0, 0).toDate());
		absEntityManager.persist(record);

		// When
		AgentAsaA54Count result = repository.getAgentCounterByDate(AgentAsaA54Count.class, 9008767, new DateTime(2014,
				1, 1, 0, 0, 0).toDate());

		// Then
		assertNotNull(result);
		assertEquals(result, record);

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getListCounterA54_ReturnEmptyList() {

		// When
		List<AgentAsaA54Count> result = repository.getListCounter(AgentAsaA54Count.class);

		// Then
		assertEquals(0, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getListCounterA54_ReturnListCompteur() {

		// Given
		AgentAsaA54Count record = new AgentAsaA54Count();
		record.setIdAgent(9001767);
		record.setTotalJours(7.0);
		record.setDateDebut(new DateTime(2014, 1, 1, 0, 0, 0).toDate());
		record.setDateFin(new DateTime(2014, 12, 31, 0, 0, 0).toDate());
		absEntityManager.persist(record);
		AgentAsaA54Count record2 = new AgentAsaA54Count();
		record2.setIdAgent(9005138);
		record2.setTotalJours(10.0);
		record2.setDateDebut(new DateTime(2014, 1, 1, 0, 0, 0).toDate());
		record2.setDateFin(new DateTime(2014, 12, 31, 0, 0, 0).toDate());
		absEntityManager.persist(record2);

		// When
		List<AgentAsaA54Count> result = repository.getListCounter(AgentAsaA54Count.class);

		// Then
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals(record.getTotalJours(), result.get(0).getTotalJours());
		assertEquals(record.getIdAgent(), result.get(0).getIdAgent());
		assertEquals(record2.getTotalJours(), result.get(1).getTotalJours());
		assertEquals(record2.getIdAgent(), result.get(1).getIdAgent());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getAgentCounterA55ByDate_NoAgent_ReturnNull() {

		// Given
		AgentAsaA55Count record = new AgentAsaA55Count();
		record.setIdAgent(9008767);
		record.setTotalMinutes(7 * 60);
		record.setDateDebut(new DateTime(2013, 1, 1, 0, 0, 0).toDate());
		record.setDateFin(new DateTime(2013, 1, 31, 0, 0, 0).toDate());
		absEntityManager.persist(record);

		// When
		AgentAsaA55Count result = repository.getAgentCounterByDate(AgentAsaA55Count.class, 9008767, new DateTime(2014,
				1, 1, 0, 0, 0).toDate());

		// Then
		assertNull(result);

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getAgentCounterA55ByDate_1Agent_ReturnRecord() {

		// Given
		AgentAsaA55Count record = new AgentAsaA55Count();
		record.setIdAgent(9008767);
		record.setTotalMinutes(7 * 60);
		record.setDateDebut(new DateTime(2014, 1, 1, 0, 0, 0).toDate());
		record.setDateFin(new DateTime(2014, 12, 31, 0, 0, 0).toDate());
		absEntityManager.persist(record);

		// When
		AgentAsaA55Count result = repository.getAgentCounterByDate(AgentAsaA55Count.class, 9008767, new DateTime(2014,
				1, 1, 0, 0, 0).toDate());

		// Then
		assertNotNull(result);
		assertEquals(result, record);

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getListCounterA55_ReturnEmptyList() {

		// When
		List<AgentAsaA55Count> result = repository.getListCounter(AgentAsaA55Count.class);

		// Then
		assertEquals(0, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getListCounterA55_ReturnListCompteur() {

		// Given
		AgentAsaA55Count record = new AgentAsaA55Count();
		record.setIdAgent(9001767);
		record.setTotalMinutes(7 * 60);
		record.setDateDebut(new DateTime(2014, 1, 1, 0, 0, 0).toDate());
		record.setDateFin(new DateTime(2014, 1, 31, 0, 0, 0).toDate());
		absEntityManager.persist(record);
		AgentAsaA55Count record2 = new AgentAsaA55Count();
		record2.setIdAgent(9005138);
		record2.setTotalMinutes(10 * 60);
		record2.setDateDebut(new DateTime(2014, 3, 1, 0, 0, 0).toDate());
		record2.setDateFin(new DateTime(2014, 3, 31, 0, 0, 0).toDate());
		absEntityManager.persist(record2);

		// When
		List<AgentAsaA55Count> result = repository.getListCounter(AgentAsaA55Count.class);

		// Then
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals(record.getTotalMinutes(), result.get(0).getTotalMinutes());
		assertEquals(record.getIdAgent(), result.get(0).getIdAgent());
		assertEquals(record2.getTotalMinutes(), result.get(1).getTotalMinutes());
		assertEquals(record2.getIdAgent(), result.get(1).getIdAgent());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getListAgentCounterByDate_ForASA_A55_ReturnListAgentAsaA55Count() {

		// Given
		Date dateDeb = new DateTime(2014, 1, 1, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 31, 23, 59, 59).toDate();
		AgentAsaA55Count record = new AgentAsaA55Count();
		record.setIdAgent(9001767);
		record.setTotalMinutes(7 * 60);
		record.setDateDebut(new DateTime(2014, 1, 1, 0, 0, 0).toDate());
		record.setDateFin(new DateTime(2014, 1, 31, 0, 0, 0).toDate());
		absEntityManager.persist(record);
		AgentAsaA55Count record2 = new AgentAsaA55Count();
		record2.setIdAgent(9001767);
		record2.setTotalMinutes(10 * 60);
		record2.setDateDebut(new DateTime(2014, 3, 1, 0, 0, 0).toDate());
		record2.setDateFin(new DateTime(2014, 3, 31, 0, 0, 0).toDate());
		absEntityManager.persist(record2);
		AgentAsaA55Count record3 = new AgentAsaA55Count();
		record3.setIdAgent(9005138);
		record3.setTotalMinutes(10 * 60);
		record3.setDateDebut(new DateTime(2014, 3, 1, 0, 0, 0).toDate());
		record3.setDateFin(new DateTime(2014, 3, 31, 0, 0, 0).toDate());
		absEntityManager.persist(record3);

		// When
		List<AgentAsaA55Count> result = repository.getListAgentCounterA55ByDate(9001767, dateDeb, dateFin);

		// Then
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals(record.getTotalMinutes(), result.get(0).getTotalMinutes());
		assertEquals(record.getIdAgent(), result.get(0).getIdAgent());
		assertEquals(record2.getTotalMinutes(), result.get(1).getTotalMinutes());
		assertEquals(record2.getIdAgent(), result.get(1).getIdAgent());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getOSCounterByDate_1Agent_ReturnRecord() {

		// Given
		AgentAsaA54Count record = new AgentAsaA54Count();
		record.setIdAgent(9008767);
		record.setTotalJours(7.0);
		record.setDateDebut(new DateTime(2014, 1, 1, 0, 0, 0).toDate());
		record.setDateFin(new DateTime(2014, 12, 31, 0, 0, 0).toDate());
		absEntityManager.persist(record);

		// When
		AgentAsaA54Count result = repository.getAgentCounterByDate(AgentAsaA54Count.class, 9008767, new DateTime(2014,
				1, 1, 0, 0, 0).toDate());

		// Then
		assertNotNull(result);
		assertEquals(result, record);

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getOSCounterByDate_ReturnNull() {

		// When
		AgentAsaA53Count result = repository.getOSCounterByDate(AgentAsaA53Count.class, 1, new Date());

		// Then
		assertNull(result);

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getListAgentCongeAnnuelCountForReset() {

		AgentCongeAnnuelCount arcc = new AgentCongeAnnuelCount();
		arcc.setIdAgent(9005138);
		arcc.setLastModification(new Date());
		absEntityManager.persist(arcc);

		AgentCongeAnnuelCount arcc2 = new AgentCongeAnnuelCount();
		arcc2.setIdAgent(9005138);
		arcc2.setLastModification(new Date());
		arcc2.setTotalJours(10.0);
		absEntityManager.persist(arcc2);

		AgentCongeAnnuelCount arcc3 = new AgentCongeAnnuelCount();
		arcc3.setIdAgent(9005138);
		arcc3.setLastModification(new Date());
		arcc3.setTotalJours(5.0);
		absEntityManager.persist(arcc3);

		List<Integer> result = repository.getListAgentCongeAnnuelCountForReset();

		assertEquals(3, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getListOSCounterByDate_ForASA_A52_ReturnListAgentAsaA52Count() {
		Date dateDeb = new DateTime(2014, 1, 1, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 31, 23, 59, 59).toDate();

		// Given
		OrganisationSyndicale organisationSyndicale2 = new OrganisationSyndicale();
		organisationSyndicale2.setIdOrganisationSyndicale(2);
		absEntityManager.persist(organisationSyndicale2);
		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(1);
		absEntityManager.persist(organisationSyndicale);
		AgentAsaA52Count record = new AgentAsaA52Count();
		record.setIdAgent(9005138);
		record.setOrganisationSyndicale(organisationSyndicale);
		record.setTotalMinutes(7 * 60);
		record.setDateDebut(new DateTime(2014, 1, 1, 0, 0, 0).toDate());
		record.setDateFin(new DateTime(2014, 1, 31, 0, 0, 0).toDate());
		absEntityManager.persist(record);
		AgentAsaA52Count record2 = new AgentAsaA52Count();
		record2.setIdAgent(9005138);
		record.setOrganisationSyndicale(organisationSyndicale);
		record2.setTotalMinutes(10 * 60);
		record2.setDateDebut(new DateTime(2014, 3, 1, 0, 0, 0).toDate());
		record2.setDateFin(new DateTime(2014, 3, 31, 0, 0, 0).toDate());
		absEntityManager.persist(record2);
		AgentAsaA52Count record3 = new AgentAsaA52Count();
		record3.setIdAgent(9005138);
		record.setOrganisationSyndicale(organisationSyndicale2);
		record3.setTotalMinutes(10 * 60);
		record3.setDateDebut(new DateTime(2014, 3, 1, 0, 0, 0).toDate());
		record3.setDateFin(new DateTime(2014, 3, 31, 0, 0, 0).toDate());
		absEntityManager.persist(record3);

		// When
		List<AgentAsaA52Count> result = repository.getListOSCounterByDateAndOrganisation(1, dateDeb, dateFin, null);

		// Then
		assertNotNull(result);
//		assertEquals(1, result.size());
//		assertEquals(record.getTotalMinutes(), result.get(0).getTotalMinutes());
//		assertEquals(record.getIdAgent(), result.get(0).getIdAgent());
//		assertEquals(record.getOrganisationSyndicale().getIdOrganisationSyndicale(), result.get(0)
//				.getOrganisationSyndicale().getIdOrganisationSyndicale());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getListOSCounterByDateAndOrganisation_Return0() throws ParseException {
		// Given
		Date fromDate = new DateTime(2014, 1, 1, 0, 0).toDate();
		Date toDate = new DateTime(2014, 1, 2, 0, 0).toDate();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setSigle("SIGLE");
		absEntityManager.persist(organisationSyndicale);

		AgentAsaA52Count d = new AgentAsaA52Count();
		d.setOrganisationSyndicale(organisationSyndicale);
		d.setDateDebut(sdf.parse("15/05/2013"));
		d.setDateFin(sdf.parse("16/05/2013"));
		absEntityManager.persist(d);

		// When
		List<AgentAsaA52Count> result = repository.getListOSCounterByDateAndOrganisation(
				organisationSyndicale.getIdOrganisationSyndicale(), fromDate, toDate, null);

		// Then
		assertEquals(0, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getListOSCounterByDateAndOrganisation_Return1() throws ParseException {
		// Given
		Date fromDate = new DateTime(2014, 5, 15, 2, 0).toDate();
		Date toDate = new DateTime(2014, 5, 15, 4, 0).toDate();

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setSigle("SIGLE");
		absEntityManager.persist(organisationSyndicale);

		AgentAsaA52Count d = new AgentAsaA52Count();
		d.setOrganisationSyndicale(organisationSyndicale);
		d.setDateDebut(new DateTime(2014, 5, 15, 0, 0).toDate());
		d.setDateFin(new DateTime(2014, 5, 16, 0, 0).toDate());
		absEntityManager.persist(d);

		// When
		List<AgentAsaA52Count> result = repository.getListOSCounterByDateAndOrganisation(
				organisationSyndicale.getIdOrganisationSyndicale(), fromDate, toDate, null);

		// Then
		assertEquals(1, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getWeekRecupTempForAgentAndDate_NoResultForWeek_ReturnNull() {

		// Given
		AgentWeekRecup r1 = new AgentWeekRecup();
		r1.setIdAgent(9008989);
		r1.setDateDay(new LocalDate(2013, 11, 11).toDate());
		r1.setIdPointage(1);
		absEntityManager.persist(r1);

		// When
		AgentWeekRecup result = repository.getWeekHistoRecupCountByIdAgentAndIdPointage(9009999, 1);

		// Then
		assertNull(result);

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getWeekRecupTempForAgentAndDate_NoResultForAgent_ReturnNull() {

		// Given
		AgentWeekRecup r1 = new AgentWeekRecup();
		r1.setIdAgent(9008989);
		r1.setDateDay(new LocalDate(2013, 11, 11).toDate());
		r1.setIdPointage(1);
		absEntityManager.persist(r1);

		// When
		AgentWeekRecup result = repository.getWeekHistoRecupCountByIdAgentAndIdPointage(9008989, 2);

		// Then
		assertNull(result);

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getWeekRecupTempForAgentAndDate_1ResultForAgent() {

		// Given
		AgentWeekRecup r1 = new AgentWeekRecup();
		r1.setIdAgent(9008989);
		r1.setDateDay(new LocalDate(2013, 11, 11).toDate());
		r1.setIdPointage(1);
		absEntityManager.persist(r1);

		// When
		AgentWeekRecup result = repository.getWeekHistoRecupCountByIdAgentAndIdPointage(9008989, 1);

		// Then
		assertEquals(result, r1);

		absEntityManager.flush();
		absEntityManager.clear();
	}
	


	@Test
	@Transactional("absTransactionManager")
	public void getListCounterByAnnee_ReturnEmptyList() {

		// When
		List<AgentAsaA54Count> result = repository.getListCounterByAnnee(AgentAsaA54Count.class,2015);

		// Then
		assertEquals(0, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getListCounterByAnnee_ReturnListCompteur() {

		// Given
		AgentAsaA54Count record = new AgentAsaA54Count();
		record.setIdAgent(9001767);
		record.setTotalJours(7.0);
		record.setDateDebut(new DateTime(2014, 1, 1, 0, 0, 0).toDate());
		record.setDateFin(new DateTime(2014, 12, 31, 0, 0, 0).toDate());
		absEntityManager.persist(record);
		AgentAsaA54Count record2 = new AgentAsaA54Count();
		record2.setIdAgent(9005138);
		record2.setTotalJours(10.0);
		record2.setDateDebut(new DateTime(2015, 1, 1, 0, 0, 0).toDate());
		record2.setDateFin(new DateTime(2015, 12, 31, 0, 0, 0).toDate());
		absEntityManager.persist(record2);

		// When
		List<AgentAsaA54Count> result = repository.getListCounterByAnnee(AgentAsaA54Count.class,2015);

		// Then
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(record2.getTotalJours(), result.get(0).getTotalJours());
		assertEquals(record2.getIdAgent(), result.get(0).getIdAgent());

		absEntityManager.flush();
		absEntityManager.clear();
	}
}
