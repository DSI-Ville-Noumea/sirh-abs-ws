package nc.noumea.mairie.abs.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nc.noumea.mairie.abs.domain.OrganisationSyndicale;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/META-INF/spring/applicationContext-test.xml" })
public class OrganisationSyndicaleRepositoryTest {

	@Autowired
	OrganisationSyndicaleRepository repository;

	@PersistenceContext(unitName = "absPersistenceUnit")
	private EntityManager absEntityManager;

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
}
