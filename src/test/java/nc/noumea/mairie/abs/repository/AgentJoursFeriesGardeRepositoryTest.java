package nc.noumea.mairie.abs.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nc.noumea.mairie.abs.domain.AgentJoursFeriesGarde;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/META-INF/spring/applicationContext-test.xml" })
public class AgentJoursFeriesGardeRepositoryTest {

	@Autowired
	AgentJoursFeriesGardeRepository repository;

	@PersistenceContext(unitName = "absPersistenceUnit")
	private EntityManager absEntityManager;

	@Test
	@Transactional("absTransactionManager")
	public void getAgentJoursFeriesGardeByIdAgentAndJourFerie_ok() {

		AgentJoursFeriesGarde a = new AgentJoursFeriesGarde();
		a.setIdAgent(9005134);
		a.setDateModification(new Date());
		a.setJourFerieChome(new DateTime(2014, 12, 25, 0, 0, 0).toDate());
		absEntityManager.persist(a);

		AgentJoursFeriesGarde result = repository.getAgentJoursFeriesGardeByIdAgentAndJourFerie(9005134, new DateTime(
				2014, 12, 25, 0, 0, 0).toDate());

		assertEquals(7, result.getIdAgentJoursFeriesGarde().intValue());

		absEntityManager.clear();
		absEntityManager.flush();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getAgentJoursFeriesGardeByIdAgentAndJourFerie_badAgent() {

		AgentJoursFeriesGarde a = new AgentJoursFeriesGarde();
		a.setIdAgent(9005138);
		a.setDateModification(new Date());
		a.setJourFerieChome(new DateTime(2014, 12, 25, 0, 0, 0).toDate());
		absEntityManager.persist(a);

		AgentJoursFeriesGarde result = repository.getAgentJoursFeriesGardeByIdAgentAndJourFerie(9005199, new DateTime(
				2014, 12, 25, 0, 0, 0).toDate());

		assertNull(result);

		absEntityManager.clear();
		absEntityManager.flush();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getAgentJoursFeriesGardeByIdAgentAndJourFerie_badDate() {

		AgentJoursFeriesGarde a = new AgentJoursFeriesGarde();
		a.setIdAgent(9005138);
		a.setDateModification(new Date());
		a.setJourFerieChome(new DateTime(2014, 12, 25, 0, 0, 0).toDate());
		absEntityManager.persist(a);

		AgentJoursFeriesGarde result = repository.getAgentJoursFeriesGardeByIdAgentAndJourFerie(9005138, new DateTime(
				2015, 12, 25, 0, 0, 0).toDate());

		assertNull(result);

		absEntityManager.clear();
		absEntityManager.flush();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getAgentJoursFeriesGardeByIdAgentAndPeriode_ok() {

		AgentJoursFeriesGarde a = new AgentJoursFeriesGarde();
		a.setIdAgent(9005138);
		a.setDateModification(new Date());
		a.setJourFerieChome(new DateTime(2014, 12, 25, 0, 0, 0).toDate());
		absEntityManager.persist(a);

		List<AgentJoursFeriesGarde> result = repository.getAgentJoursFeriesGardeByIdAgentAndPeriode(9005138,
				new DateTime(2014, 12, 1, 0, 0, 0).toDate(), new DateTime(2014, 12, 31, 0, 0, 0).toDate());

		assertEquals(1, result.size());

		absEntityManager.clear();
		absEntityManager.flush();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getAgentJoursFeriesGardeByIdAgentAndPeriode_badAgent() {

		AgentJoursFeriesGarde a = new AgentJoursFeriesGarde();
		a.setIdAgent(9005138);
		a.setDateModification(new Date());
		a.setJourFerieChome(new DateTime(2014, 12, 25, 0, 0, 0).toDate());
		absEntityManager.persist(a);

		List<AgentJoursFeriesGarde> result = repository.getAgentJoursFeriesGardeByIdAgentAndPeriode(9009999,
				new DateTime(2014, 12, 1, 0, 0, 0).toDate(), new DateTime(2014, 12, 31, 0, 0, 0).toDate());

		assertEquals(0, result.size());

		absEntityManager.clear();
		absEntityManager.flush();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getAgentJoursFeriesGardeByIdAgentAndPeriode_badDate() {

		AgentJoursFeriesGarde a = new AgentJoursFeriesGarde();
		a.setIdAgent(9005138);
		a.setDateModification(new Date());
		a.setJourFerieChome(new DateTime(2014, 1, 25, 0, 0, 0).toDate());
		absEntityManager.persist(a);

		List<AgentJoursFeriesGarde> result = repository.getAgentJoursFeriesGardeByIdAgentAndPeriode(9005138,
				new DateTime(2014, 12, 1, 0, 0, 0).toDate(), new DateTime(2014, 12, 31, 0, 0, 0).toDate());

		assertEquals(0, result.size());

		absEntityManager.clear();
		absEntityManager.flush();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getAgentJoursFeriesGardeByPeriode_ok() {

		AgentJoursFeriesGarde a = new AgentJoursFeriesGarde();
		a.setIdAgent(9005138);
		a.setDateModification(new Date());
		a.setJourFerieChome(new DateTime(2014, 12, 25, 0, 0, 0).toDate());
		absEntityManager.persist(a);

		AgentJoursFeriesGarde b = new AgentJoursFeriesGarde();
		b.setIdAgent(9005140);
		b.setDateModification(new Date());
		b.setJourFerieChome(new DateTime(2014, 12, 15, 0, 0, 0).toDate());
		absEntityManager.persist(b);

		List<AgentJoursFeriesGarde> result = repository.getAgentJoursFeriesGardeByPeriode(new DateTime(2014, 12, 1, 0,
				0, 0).toDate(), new DateTime(2014, 12, 31, 0, 0, 0).toDate());

		assertEquals(2, result.size());

		absEntityManager.clear();
		absEntityManager.flush();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getAgentJoursFeriesGardeByPeriode_badDate() {

		AgentJoursFeriesGarde a = new AgentJoursFeriesGarde();
		a.setIdAgent(9005138);
		a.setDateModification(new Date());
		a.setJourFerieChome(new DateTime(2014, 1, 25, 0, 0, 0).toDate());
		absEntityManager.persist(a);

		List<AgentJoursFeriesGarde> result = repository.getAgentJoursFeriesGardeByPeriode(new DateTime(2014, 12, 1, 0,
				0, 0).toDate(), new DateTime(2014, 12, 31, 0, 0, 0).toDate());

		assertEquals(0, result.size());

		absEntityManager.clear();
		absEntityManager.flush();
	}
}
