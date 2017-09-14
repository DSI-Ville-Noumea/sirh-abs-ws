package nc.noumea.mairie.abs.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import nc.noumea.mairie.abs.domain.AgentA48OrganisationSyndicale;
import nc.noumea.mairie.abs.domain.AgentA54OrganisationSyndicale;
import nc.noumea.mairie.abs.domain.AgentAsaA48Count;
import nc.noumea.mairie.abs.domain.AgentAsaA54Count;
import nc.noumea.mairie.abs.domain.AgentOrganisationSyndicale;
import nc.noumea.mairie.abs.domain.OrganisationSyndicale;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/META-INF/spring/applicationContext-test.xml" })
public class OrganisationSyndicaleRepositoryTest {

	@Autowired
	OrganisationSyndicaleRepository	repository;

	@PersistenceContext(unitName = "absPersistenceUnit")
	private EntityManager			absEntityManager;

	@Test
	@Transactional("absTransactionManager")
	public void findAllOrganisation_NoOrganisation() {
		// Given

		// When
		List<OrganisationSyndicale> result = repository.findAllOrganisation();

		// Then
		assertEquals(0, result.size());
		assertNotNull(result);

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void findAllOrganisation() {
		// Given
		OrganisationSyndicale org1 = new OrganisationSyndicale();
		org1.setLibelle("PROVISOIRE");
		org1.setIdOrganisationSyndicale(1);
		org1.setSigle("sigle1");
		org1.setActif(true);
		absEntityManager.persist(org1);
		OrganisationSyndicale org2 = new OrganisationSyndicale();
		org2.setLibelle("SAISIE");
		org2.setIdOrganisationSyndicale(2);
		org2.setSigle("sigle2");
		org2.setActif(false);
		absEntityManager.persist(org2);

		// When
		List<OrganisationSyndicale> result = repository.findAllOrganisation();

		// Then
		assertEquals(2, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void findAllOrganisationActives() {
		// Given
		OrganisationSyndicale org1 = new OrganisationSyndicale();
		org1.setLibelle("PROVISOIRE");
		org1.setIdOrganisationSyndicale(1);
		org1.setSigle("sigle1");
		org1.setActif(true);
		absEntityManager.persist(org1);
		OrganisationSyndicale org2 = new OrganisationSyndicale();
		org2.setLibelle("SAISIE");
		org2.setIdOrganisationSyndicale(2);
		org2.setSigle("sigle2");
		org2.setActif(false);
		absEntityManager.persist(org2);

		// When
		List<OrganisationSyndicale> result = repository.findAllOrganisationActives();

		// Then
		assertEquals(1, result.size());
		assertEquals(org1.getSigle(), result.get(0).getSigle());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getListeAgentOrganisation() {
		// Given
		OrganisationSyndicale orga = new OrganisationSyndicale();
		orga.setIdOrganisationSyndicale(1);
		orga.setActif(true);
		absEntityManager.persist(orga);
		OrganisationSyndicale orga2 = new OrganisationSyndicale();
		orga2.setIdOrganisationSyndicale(2);
		orga2.setActif(false);
		absEntityManager.persist(orga2);
		AgentOrganisationSyndicale org1 = new AgentOrganisationSyndicale();
		org1.setIdAgent(9005138);
		org1.setOrganisationSyndicale(orga);
		org1.setActif(true);
		absEntityManager.persist(org1);
		AgentOrganisationSyndicale org2 = new AgentOrganisationSyndicale();
		org2.setIdAgent(9005138);
		org2.setOrganisationSyndicale(orga2);
		org2.setActif(false);
		absEntityManager.persist(org2);

		// When
		List<AgentOrganisationSyndicale> result = repository.getListeAgentOrganisation(orga.getIdOrganisationSyndicale());

		// Then
		assertEquals(1, result.size());
		assertEquals(org1.getIdAgent(), result.get(0).getIdAgent());
		assertEquals(org1.getOrganisationSyndicale().getIdOrganisationSyndicale(),
				result.get(0).getOrganisationSyndicale().getIdOrganisationSyndicale());
		assertEquals(org1.isActif(), result.get(0).isActif());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getAgentOrganisation() {
		// Given
		OrganisationSyndicale orga = new OrganisationSyndicale();
		orga.setIdOrganisationSyndicale(1);
		orga.setActif(true);
		absEntityManager.persist(orga);
		OrganisationSyndicale orga2 = new OrganisationSyndicale();
		orga2.setIdOrganisationSyndicale(2);
		orga2.setActif(true);
		absEntityManager.persist(orga2);
		AgentOrganisationSyndicale org1 = new AgentOrganisationSyndicale();
		org1.setIdAgent(9005138);
		org1.setOrganisationSyndicale(orga);
		org1.setActif(true);
		absEntityManager.persist(org1);
		AgentOrganisationSyndicale org2 = new AgentOrganisationSyndicale();
		org2.setIdAgent(9005138);
		org2.setOrganisationSyndicale(orga2);
		org2.setActif(false);
		absEntityManager.persist(org2);

		// When
		List<AgentOrganisationSyndicale> result = repository.getAgentOrganisation(9005138);

		// Then
		assertEquals(2, result.size());
		assertEquals(org1.getIdAgent(), result.get(0).getIdAgent());
		assertEquals(org1.getOrganisationSyndicale().getIdOrganisationSyndicale(),
				result.get(0).getOrganisationSyndicale().getIdOrganisationSyndicale());
		assertEquals(org1.isActif(), result.get(0).isActif());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getAgentOrganisation_WithOrganisation() {
		// Given
		OrganisationSyndicale orga = new OrganisationSyndicale();
		orga.setIdOrganisationSyndicale(1);
		absEntityManager.persist(orga);
		AgentOrganisationSyndicale org1 = new AgentOrganisationSyndicale();
		org1.setIdAgent(9005138);
		org1.setOrganisationSyndicale(orga);
		org1.setActif(true);
		absEntityManager.persist(org1);

		// When
		AgentOrganisationSyndicale result = repository.getAgentOrganisation(9005138, orga.getIdOrganisationSyndicale());

		// Then
		assertEquals(org1.getIdAgent(), result.getIdAgent());
		assertEquals(org1.getOrganisationSyndicale().getIdOrganisationSyndicale(), result.getOrganisationSyndicale().getIdOrganisationSyndicale());
		assertEquals(org1.isActif(), result.isActif());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getAgentA54Organisation() {
		// Given
		OrganisationSyndicale orga = new OrganisationSyndicale();
		orga.setIdOrganisationSyndicale(1);
		orga.setActif(true);
		absEntityManager.persist(orga);
		OrganisationSyndicale orga2 = new OrganisationSyndicale();
		orga2.setIdOrganisationSyndicale(2);
		orga2.setActif(true);
		absEntityManager.persist(orga2);
		AgentA54OrganisationSyndicale org1 = new AgentA54OrganisationSyndicale();
		org1.setIdAgent(9005138);
		org1.setOrganisationSyndicale(orga);
		absEntityManager.persist(org1);
		AgentA54OrganisationSyndicale org2 = new AgentA54OrganisationSyndicale();
		org2.setIdAgent(9005138);
		org2.setOrganisationSyndicale(orga2);
		absEntityManager.persist(org2);

		// When
		List<AgentA54OrganisationSyndicale> result = repository.getAgentA54Organisation(9005138);

		// Then
		assertEquals(2, result.size());
		assertEquals(org1.getIdAgent(), result.get(0).getIdAgent());
		assertEquals(org1.getOrganisationSyndicale().getIdOrganisationSyndicale(),
				result.get(0).getOrganisationSyndicale().getIdOrganisationSyndicale());

		absEntityManager.flush();
		absEntityManager.clear();
	}


	@Test
	@Transactional("absTransactionManager")
	public void getAgentA54OrganisationByOS_OK_1result() {
		AgentAsaA54Count a54 = new AgentAsaA54Count();
		a54.setDateDebut(new DateTime(2015,7,1,0,0,0).toDate());
		a54.setIdAgent(9005138);
		absEntityManager.persist(a54);
		// Given
		OrganisationSyndicale orga = new OrganisationSyndicale();
		orga.setIdOrganisationSyndicale(2);
		orga.setActif(true);
		absEntityManager.persist(orga);

		AgentA54OrganisationSyndicale org1 = new AgentA54OrganisationSyndicale();
		org1.setIdAgent(9005138);
		org1.setOrganisationSyndicale(orga);
		absEntityManager.persist(org1);
		
		// When
		List<AgentA54OrganisationSyndicale> result = repository.getAgentA54OrganisationByOS(orga.getIdOrganisationSyndicale(), null, null, 2015);

		// Then
		assertEquals(1, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}


	@Test
	@Transactional("absTransactionManager")
	public void getAgentA54OrganisationByOS_OK_noResult() {
		AgentAsaA54Count a54 = new AgentAsaA54Count();
		a54.setDateDebut(new Date(2015, 1, 1));
		a54.setIdAgent(9005138);
		absEntityManager.persist(a54);
		// Given
		OrganisationSyndicale orga = new OrganisationSyndicale();
		orga.setIdOrganisationSyndicale(1);
		orga.setActif(true);
		absEntityManager.persist(orga);

		AgentA54OrganisationSyndicale org1 = new AgentA54OrganisationSyndicale();
		org1.setIdAgent(9005138);
		org1.setOrganisationSyndicale(orga);
		absEntityManager.persist(org1);
		
		// When
		List<AgentA54OrganisationSyndicale> result = repository.getAgentA54OrganisationByOS(orga.getIdOrganisationSyndicale(), null, null, 2016);

		// Then
		assertEquals(0, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}


	@Test
	@Transactional("absTransactionManager")
	public void getAgentA54OrganisationByOS() {
		AgentAsaA54Count a54 = new AgentAsaA54Count();
		a54.setDateDebut(new Date(2015, 1, 1));
		a54.setIdAgent(9005138);
		absEntityManager.persist(a54);
		// Given
		OrganisationSyndicale orga = new OrganisationSyndicale();
		orga.setIdOrganisationSyndicale(1);
		orga.setActif(true);
		absEntityManager.persist(orga);
		OrganisationSyndicale orga2 = new OrganisationSyndicale();
		orga2.setIdOrganisationSyndicale(2);
		orga2.setActif(true);
		absEntityManager.persist(orga2);
		AgentA54OrganisationSyndicale org1 = new AgentA54OrganisationSyndicale();
		org1.setIdAgent(9005138);
		org1.setOrganisationSyndicale(orga);
		absEntityManager.persist(org1);
		AgentA54OrganisationSyndicale org2 = new AgentA54OrganisationSyndicale();
		org2.setIdAgent(9005138);
		org2.setOrganisationSyndicale(orga2);
		absEntityManager.persist(org2);
		
		// When
		List<AgentA54OrganisationSyndicale> result = repository.getAgentA54OrganisationByOS(orga.getIdOrganisationSyndicale(), null, null, null);

		// Then
		assertEquals(1, result.size());
		assertEquals(org1.getIdAgent(), result.get(0).getIdAgent());
		assertEquals(org1.getOrganisationSyndicale().getIdOrganisationSyndicale(),
				result.get(0).getOrganisationSyndicale().getIdOrganisationSyndicale());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getAgentA48Organisation() {
		// Given
		OrganisationSyndicale orga = new OrganisationSyndicale();
		orga.setIdOrganisationSyndicale(1);
		orga.setActif(true);
		absEntityManager.persist(orga);
		OrganisationSyndicale orga2 = new OrganisationSyndicale();
		orga2.setIdOrganisationSyndicale(2);
		orga2.setActif(true);
		absEntityManager.persist(orga2);
		AgentA48OrganisationSyndicale org1 = new AgentA48OrganisationSyndicale();
		org1.setIdAgent(9005138);
		org1.setOrganisationSyndicale(orga);
		absEntityManager.persist(org1);
		AgentA48OrganisationSyndicale org2 = new AgentA48OrganisationSyndicale();
		org2.setIdAgent(9005138);
		org2.setOrganisationSyndicale(orga2);
		absEntityManager.persist(org2);

		// When
		List<AgentA48OrganisationSyndicale> result = repository.getAgentA48Organisation(9005138);

		// Then
		assertEquals(2, result.size());
		assertEquals(org1.getIdAgent(), result.get(0).getIdAgent());
		assertEquals(org1.getOrganisationSyndicale().getIdOrganisationSyndicale(),
				result.get(0).getOrganisationSyndicale().getIdOrganisationSyndicale());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getAgentA48OrganisationByOS() {
		AgentAsaA48Count a48 = new AgentAsaA48Count();
		a48.setDateDebut(new Date(2015, 1, 1));
		a48.setIdAgent(9005138);
		absEntityManager.persist(a48);
		// Given
		OrganisationSyndicale orga = new OrganisationSyndicale();
		orga.setIdOrganisationSyndicale(1);
		orga.setActif(true);
		absEntityManager.persist(orga);
		OrganisationSyndicale orga2 = new OrganisationSyndicale();
		orga2.setIdOrganisationSyndicale(2);
		orga2.setActif(true);
		absEntityManager.persist(orga2);
		AgentA48OrganisationSyndicale org1 = new AgentA48OrganisationSyndicale();
		org1.setIdAgent(9005138);
		org1.setOrganisationSyndicale(orga);
		absEntityManager.persist(org1);
		AgentA48OrganisationSyndicale org2 = new AgentA48OrganisationSyndicale();
		org2.setIdAgent(9005138);
		org2.setOrganisationSyndicale(orga2);
		absEntityManager.persist(org2);

		// When
		List<AgentA48OrganisationSyndicale> result = repository.getAgentA48OrganisationByOS(orga.getIdOrganisationSyndicale(), null, null, null);

		// Then
		assertEquals(1, result.size());
		assertEquals(org1.getIdAgent(), result.get(0).getIdAgent());
		assertEquals(org1.getOrganisationSyndicale().getIdOrganisationSyndicale(),
				result.get(0).getOrganisationSyndicale().getIdOrganisationSyndicale());

		absEntityManager.flush();
		absEntityManager.clear();
	}
}
