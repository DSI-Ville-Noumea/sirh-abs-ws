package nc.noumea.mairie.abs.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nc.noumea.mairie.abs.domain.AgentRecupCount;
import nc.noumea.mairie.abs.domain.AgentWeekRecup;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/META-INF/spring/applicationContext-test.xml"})
public class RecuperationRepositoryTest {

	@Autowired
	RecuperationRepository repository;
	
	@PersistenceContext(unitName = "absPersistenceUnit")
	EntityManager absEntityManager;
	
	@Test
	@Transactional("absTransactionManager")
	public void getAgentRecupCount_NoAgent_ReturnNull() {
		
		// When
		AgentRecupCount result = repository.getAgentRecupCount(9008767);
		
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
		AgentRecupCount result = repository.getAgentRecupCount(9008767);
		
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
		AgentWeekRecup result = repository.getWeekRecupForAgentAndDate(9008990, new LocalDate(2013, 11, 11).toDate());
		
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
		AgentWeekRecup result = repository.getWeekRecupForAgentAndDate(9008989, new LocalDate(2013, 11, 18).toDate());
		
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
		AgentWeekRecup result = repository.getWeekRecupForAgentAndDate(9008989, new LocalDate(2013, 11, 11).toDate());
		
		// Then
		assertEquals(result, r1);
	}
	
}
