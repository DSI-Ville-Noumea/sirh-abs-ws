package nc.noumea.mairie.abs.repository;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nc.noumea.mairie.abs.domain.Droit;
import nc.noumea.mairie.abs.domain.DroitsAgent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/META-INF/spring/applicationContext-test.xml" })
public class AccessRightsRepositoryTest {

	@Autowired
	AccessRightsRepository repository;

	@PersistenceContext(unitName = "absPersistenceUnit")
	private EntityManager absEntityManager;

	@Test
	@Transactional("absTransactionManager")
	public void getAgentAccessRights_ReturnNull() {

		// When
		List<Droit> result = repository.getAgentAccessRights(9005138);

		// Then
		assertEquals(0, result.size());
	}

	@Test
	@Transactional("absTransactionManager")
	public void getAgentAccessRights_ReturnResult() {

		DroitsAgent agent = new DroitsAgent();
		agent.setIdAgent(9008767);
		agent.setIdDroitsAgent(1);
		agent.setCodeService("DEAB");
		agent.setLibelleService("DASP Pôle Administratif et Budgétaire");
		agent.setDateModification(new Date());
		absEntityManager.persist(agent);

		Droit droit = new Droit();
		droit.setDateModification(new Date());
		droit.setIdAgent(9008767);
		droit.setIdDroit(1);
		absEntityManager.persist(droit);

		// When
		List<Droit> result = repository.getAgentAccessRights(9008767);

		// Then
		assertEquals(1, result.size());
		assertEquals("9008767", result.get(0).getIdAgent().toString());

		absEntityManager.flush();
		absEntityManager.clear();
	}
}
