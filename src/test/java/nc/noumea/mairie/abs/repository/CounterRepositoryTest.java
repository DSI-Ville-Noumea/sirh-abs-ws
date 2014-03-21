package nc.noumea.mairie.abs.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nc.noumea.mairie.abs.domain.AgentAsaA48Count;
import nc.noumea.mairie.abs.domain.AgentRecupCount;
import nc.noumea.mairie.abs.domain.AgentReposCompCount;
import nc.noumea.mairie.abs.domain.AgentWeekRecup;

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
	}

	@Test
	@Transactional("absTransactionManager")
	public void getAgentReposCompCountByIdCounter_KO() {

		AgentReposCompCount arcc = new AgentReposCompCount();
		arcc.setIdAgent(9005138);
		arcc.setLastModification(new Date());
		absEntityManager.persist(arcc);

		AgentReposCompCount result = repository.getAgentReposCompCountByIdCounter(3);

		assertNull(result);

		absEntityManager.clear();
		absEntityManager.flush();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getAgentReposCompCountByIdCounter_OK() {

		AgentReposCompCount arcc = new AgentReposCompCount();
		arcc.setIdAgentReposCompCount(2);
		arcc.setIdAgent(9005138);
		arcc.setLastModification(new Date());
		absEntityManager.persist(arcc);

		AgentReposCompCount result = repository.getAgentReposCompCountByIdCounter(2);

		assertNotNull(result);
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
	}

	@Test
	@Transactional("absTransactionManager")
	public void getAgentAsaA48Count_NoAgent_ReturnNull() {

		// Given
		AgentAsaA48Count record = new AgentAsaA48Count();
		record.setIdAgent(9008767);
		record.setTotalJours(7);
		record.setDateDebut(new DateTime(2013, 1, 1, 0, 0, 0).toDate());
		record.setDateFin(new DateTime(2013, 12, 31, 0, 0, 0).toDate());
		absEntityManager.persist(record);

		// When
		AgentAsaA48Count result = repository.getAgentCounterByDate(AgentAsaA48Count.class, 9008767, new DateTime(2014,
				1, 1, 0, 0, 0).toDate(), new DateTime(2014, 12, 31, 0, 0, 0).toDate());

		// Then
		assertNull(result);
	}

	@Test
	@Transactional("absTransactionManager")
	public void getAgentAsaA48Count_1Agent_ReturnRecord() {

		// Given
		AgentAsaA48Count record = new AgentAsaA48Count();
		record.setIdAgent(9008767);
		record.setTotalJours(7);
		record.setDateDebut(new DateTime(2014, 1, 1, 0, 0, 0).toDate());
		record.setDateFin(new DateTime(2014, 12, 31, 0, 0, 0).toDate());
		absEntityManager.persist(record);

		// When
		AgentAsaA48Count result = repository.getAgentCounterByDate(AgentAsaA48Count.class, 9008767, new DateTime(2014,
				1, 1, 0, 0, 0).toDate(), new DateTime(2014, 12, 31, 0, 0, 0).toDate());

		// Then
		assertNotNull(result);
		assertEquals(result, record);
	}
}
