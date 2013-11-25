package nc.noumea.mairie.abs.repository;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nc.noumea.mairie.abs.domain.Droit;
import nc.noumea.mairie.abs.domain.DroitProfil;
import nc.noumea.mairie.abs.domain.Profil;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
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
		Droit result = new Droit();
		try {
			result = repository.getAgentAccessRights(9005138);
		} catch (EmptyResultDataAccessException e) {
			result = null;
		}

		// Then
		assertEquals(null, result);
	}

	@Test
	@Transactional("absTransactionManager")
	public void getAgentAccessRights_ReturnResult() {

		Profil pr = new Profil();
		pr.setLibelle("libelle test");
		pr.setSaisie(true);
		pr.setModification(true);
		pr.setSuppression(true);
		pr.setImpression(true);
		pr.setViserVisu(false);
		pr.setViserModif(false);
		pr.setApprouverVisu(false);
		pr.setApprouverModif(false);
		pr.setAnnuler(true);
		pr.setVisuSolde(true);
		pr.setMajSolde(true);
		pr.setDroitAcces(false);
		absEntityManager.persist(pr);

		DroitProfil dpr = new DroitProfil();
		dpr.setProfil(pr);
		absEntityManager.persist(dpr);

		Droit droit = new Droit();
		droit.setDateModification(new Date());
		droit.setIdAgent(9008767);
		droit.setIdDroit(1);
		droit.getDroitProfils().add(dpr);
		absEntityManager.persist(droit);

		// When
		Droit result = repository.getAgentAccessRights(9008767);

		// Then
		assertEquals("9008767", result.getIdAgent().toString());
		assertEquals(1, result.getDroitProfils().size());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getAgentsApprobateurs_noResult() {
		List<Droit> result = repository.getAgentsApprobateurs();

		assertEquals(0, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getAgentsApprobateurs_result() {
		Profil p = new Profil();
		p.setLibelle("APPROBATEUR");
		absEntityManager.persist(p);
		
		Droit droitApprobateur1 = new Droit();
		DroitProfil dp1 = new DroitProfil();
		dp1.setDroit(droitApprobateur1);
		dp1.setDroitApprobateur(droitApprobateur1);
		dp1.setProfil(p);		
		droitApprobateur1.setIdAgent(9008767);
		droitApprobateur1.setDroitProfils(Arrays.asList(dp1));
		absEntityManager.persist(droitApprobateur1);

		Droit droitApprobateur2 = new Droit();
		DroitProfil dp2 = new DroitProfil();
		dp2.setDroit(droitApprobateur2);
		dp2.setDroitApprobateur(droitApprobateur2);
		dp2.setProfil(p);		
		droitApprobateur2.setIdAgent(9008767);
		droitApprobateur2.setDroitProfils(Arrays.asList(dp2));
		absEntityManager.persist(droitApprobateur2);

		List<Droit> listDroits = repository.getAgentsApprobateurs();

		assertEquals(2, listDroits.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}

}
