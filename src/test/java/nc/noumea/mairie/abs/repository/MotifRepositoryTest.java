package nc.noumea.mairie.abs.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nc.noumea.mairie.abs.domain.MotifCompteur;
import nc.noumea.mairie.abs.domain.MotifRefus;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/META-INF/spring/applicationContext-test.xml" })
public class MotifRepositoryTest {

	@Autowired
	MotifRepository repository;

	@PersistenceContext(unitName = "absPersistenceUnit")
	private EntityManager absEntityManager;

	@Test
	@Transactional("absTransactionManager")
	public void getListeMotifRefus() {
		// Given
		Integer idRefType = 3;

		RefTypeAbsence typeRecup = new RefTypeAbsence();
		typeRecup.setIdRefTypeAbsence(3);
		typeRecup.setLabel("Récupération");
		absEntityManager.persist(typeRecup);

		RefTypeAbsence typeConge = new RefTypeAbsence();
		typeConge.setIdRefTypeAbsence(1);
		typeConge.setLabel("Congé annuel");
		absEntityManager.persist(typeConge);

		MotifRefus refus1 = new MotifRefus();
		refus1.setLibelle("motif1 refus recup");
		refus1.setRefTypeAbsence(typeRecup);
		absEntityManager.persist(refus1);

		MotifRefus refus2 = new MotifRefus();
		refus2.setLibelle("motif2 refus congé");
		refus2.setRefTypeAbsence(typeConge);
		absEntityManager.persist(refus2);

		MotifRefus refus3 = new MotifRefus();
		refus3.setLibelle("motif3 refus recup");
		refus3.setRefTypeAbsence(typeRecup);
		absEntityManager.persist(refus3);

		// When
		List<MotifRefus> result = repository.getListeMotifRefus(idRefType);

		// Then
		assertEquals(2, result.size());
		assertEquals(refus1.getLibelle(), result.get(0).getLibelle());
		assertEquals(refus3.getLibelle(), result.get(1).getLibelle());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getListeMotifCompteur() {
		// Given
		Integer idRefType = 3;

		RefTypeAbsence typeRecup = new RefTypeAbsence();
		typeRecup.setIdRefTypeAbsence(3);
		typeRecup.setLabel("Récupération");
		absEntityManager.persist(typeRecup);

		RefTypeAbsence typeConge = new RefTypeAbsence();
		typeConge.setIdRefTypeAbsence(1);
		typeConge.setLabel("Congé annuel");
		absEntityManager.persist(typeConge);

		MotifCompteur refus1 = new MotifCompteur();
		refus1.setLibelle("motif1 compteur recup");
		refus1.setRefTypeAbsence(typeRecup);
		refus1.setMotifTechnique(false);
		absEntityManager.persist(refus1);

		MotifCompteur refus2 = new MotifCompteur();
		refus2.setLibelle("motif2 compteur congé");
		refus2.setRefTypeAbsence(typeConge);
		refus2.setMotifTechnique(false);
		absEntityManager.persist(refus2);

		MotifCompteur refus3 = new MotifCompteur();
		refus3.setLibelle("motif3 compteur recup");
		refus3.setRefTypeAbsence(typeRecup);
		refus3.setMotifTechnique(true);
		absEntityManager.persist(refus3);

		// When
		List<MotifCompteur> result = repository.getListeMotifCompteur(idRefType);

		// Then
		assertEquals(1, result.size());
		assertEquals(refus1.getLibelle(), result.get(0).getLibelle());
		assertFalse(result.get(0).isMotifTechnique());

		absEntityManager.flush();
		absEntityManager.clear();
	}
}
