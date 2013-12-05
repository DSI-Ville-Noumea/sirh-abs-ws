package nc.noumea.mairie.abs.repository;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nc.noumea.mairie.sirh.domain.Agent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/META-INF/spring/applicationContext-test.xml" })
public class SirhRepositoryTest {

	@Autowired
	SirhRepository repository;

	@PersistenceContext(unitName = "sirhPersistenceUnit")
	private EntityManager sirhEntityManager;

	@Test
	@Transactional("sirhTransactionManager")
	public void getAgent_ReturnNull() {

		// When
		Agent result = repository.getAgent(9005138);

		// Then
		assertEquals(null, result);
	}

	@Test
	@Transactional("sirhTransactionManager")
	public void getAgent_ReturnResult() {

		Agent ag = new Agent();
		ag.setIdAgent(9005138);
		ag.setNomatr(5138);
		ag.setPrenom("NON");
		ag.setDateNaissance(new Date());
		ag.setNomPatronymique("TEST");
		ag.setNomUsage("USAGE");
		ag.setPrenomUsage("NONO");
		sirhEntityManager.persist(ag);
		
		sirhEntityManager.flush();

		// When
		Agent result = repository.getAgent(9005138);

		// Then
		assertEquals("9005138", result.getIdAgent().toString());
		assertEquals("USAGE", result.getDisplayNom());

		sirhEntityManager.flush();
		sirhEntityManager.clear();
	}
}
