package nc.noumea.mairie.abs.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/META-INF/spring/applicationContext-test.xml"})
public class DemandeRepositoryTest {

	@Autowired
	DemandeRepository repository;
	
	@PersistenceContext(unitName = "absPersistenceUnit")
	private EntityManager absEntityManager;
	
	
}
