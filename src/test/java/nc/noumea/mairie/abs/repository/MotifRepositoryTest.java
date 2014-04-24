package nc.noumea.mairie.abs.repository;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nc.noumea.mairie.abs.domain.Motif;
import nc.noumea.mairie.abs.domain.MotifCompteur;
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
	public void getListeMotif() {
		// Given
		Motif refus1 = new Motif();
		refus1.setLibelle("motif1 refus recup");
		absEntityManager.persist(refus1);

		Motif refus2 = new Motif();
		refus2.setLibelle("motif2 refus congé");
		absEntityManager.persist(refus2);

		Motif refus3 = new Motif();
		refus3.setLibelle("motif3 refus recup");
		absEntityManager.persist(refus3);

		// When
		List<Motif> result = repository.getListeMotif();

		// Then
		assertEquals(3, result.size());
		assertEquals(refus1.getLibelle(), result.get(0).getLibelle());
		assertEquals(refus2.getLibelle(), result.get(1).getLibelle());
		assertEquals(refus3.getLibelle(), result.get(2).getLibelle());

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
		absEntityManager.persist(refus1);

		MotifCompteur refus2 = new MotifCompteur();
		refus2.setLibelle("motif2 compteur congé");
		refus2.setRefTypeAbsence(typeConge);
		absEntityManager.persist(refus2);

		MotifCompteur refus3 = new MotifCompteur();
		refus3.setLibelle("motif3 compteur recup");
		refus3.setRefTypeAbsence(typeRecup);
		absEntityManager.persist(refus3);

		// When
		List<MotifCompteur> result = repository.getListeMotifCompteur(idRefType);

		// Then
		assertEquals(2, result.size());
		assertEquals(refus1.getLibelle(), result.get(0).getLibelle());
		assertEquals(refus3.getLibelle(), result.get(1).getLibelle());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getListeMotifCompteur_NoType() {
		// Given
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
		absEntityManager.persist(refus1);

		MotifCompteur refus2 = new MotifCompteur();
		refus2.setLibelle("motif2 compteur congé");
		refus2.setRefTypeAbsence(typeConge);
		absEntityManager.persist(refus2);

		MotifCompteur refus3 = new MotifCompteur();
		refus3.setLibelle("motif3 compteur recup");
		refus3.setRefTypeAbsence(typeRecup);
		absEntityManager.persist(refus3);

		// When
		List<MotifCompteur> result = repository.getListeMotifCompteur(null);

		// Then
		assertEquals(3, result.size());
		assertEquals(refus1.getLibelle(), result.get(0).getLibelle());
		assertEquals(refus2.getLibelle(), result.get(1).getLibelle());
		assertEquals(refus3.getLibelle(), result.get(2).getLibelle());

		absEntityManager.flush();
		absEntityManager.clear();
	}
}
