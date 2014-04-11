package nc.noumea.mairie.abs.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nc.noumea.mairie.abs.domain.RefEtat;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeSaisi;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/META-INF/spring/applicationContext-test.xml" })
public class FiltreRepositoryTest {

	@Autowired
	FiltreRepository repository;

	@PersistenceContext(unitName = "absPersistenceUnit")
	private EntityManager absEntityManager;

	@Test
	@Transactional("absTransactionManager")
	public void findRefEtatEnCours() {
		// Given
		RefEtat etatProvisoire = new RefEtat();
		etatProvisoire.setLabel("PROVISOIRE");
		absEntityManager.persist(etatProvisoire);
		RefEtat etatSaisie = new RefEtat();
		etatSaisie.setLabel("SAISIE");
		absEntityManager.persist(etatSaisie);
		RefEtat etatViseFav = new RefEtat();
		etatViseFav.setLabel("VISEE_FAVORABLE");
		absEntityManager.persist(etatViseFav);
		RefEtat etatViseDefav = new RefEtat();
		etatViseDefav.setLabel("VISEE_DEFAVORABLE");
		absEntityManager.persist(etatViseDefav);
		RefEtat etatApprouve = new RefEtat();
		etatApprouve.setLabel("APPROUVEE");
		absEntityManager.persist(etatApprouve);
		RefEtat etatRefuse = new RefEtat();
		etatRefuse.setLabel("REFUSEE");
		absEntityManager.persist(etatRefuse);
		RefEtat etatPris = new RefEtat();
		etatPris.setLabel("PRISE");
		absEntityManager.persist(etatPris);
		RefEtat etatValide = new RefEtat();
		etatValide.setLabel("VALIDEE");
		absEntityManager.persist(etatValide);
		RefEtat etatRejete = new RefEtat();
		etatRejete.setLabel("REJETEE");
		absEntityManager.persist(etatRejete);
		RefEtat etatAttente = new RefEtat();
		etatAttente.setLabel("EN ATTENTE");
		absEntityManager.persist(etatAttente);

		// When
		List<RefEtat> result = repository.findRefEtatEnCours();

		// Then
		assertEquals(5, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void findRefEtatNonPris() {
		// Given
		RefEtat etatProvisoire = new RefEtat();
		etatProvisoire.setLabel("PROVISOIRE");
		absEntityManager.persist(etatProvisoire);
		RefEtat etatSaisie = new RefEtat();
		etatSaisie.setLabel("SAISIE");
		absEntityManager.persist(etatSaisie);
		RefEtat etatViseFav = new RefEtat();
		etatViseFav.setLabel("VISEE_FAVORABLE");
		absEntityManager.persist(etatViseFav);
		RefEtat etatViseDefav = new RefEtat();
		etatViseDefav.setLabel("VISEE_DEFAVORABLE");
		absEntityManager.persist(etatViseDefav);
		RefEtat etatApprouve = new RefEtat();
		etatApprouve.setLabel("APPROUVEE");
		absEntityManager.persist(etatApprouve);
		RefEtat etatRefuse = new RefEtat();
		etatRefuse.setLabel("REFUSEE");
		absEntityManager.persist(etatRefuse);
		RefEtat etatPris = new RefEtat();
		etatPris.setLabel("PRISE");
		absEntityManager.persist(etatPris);

		// When
		List<RefEtat> result = repository.findRefEtatNonPris();

		// Then
		assertEquals(6, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void findAllRefTypeAbsences_NoRefTypeAbsence() {
		// Given

		// When
		List<RefTypeAbsence> result = repository.findAllRefTypeAbsences();

		// Then
		assertEquals(0, result.size());
		assertNotNull(result);

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void findAllRefTypeAbsences() {
		// Given
		RefTypeAbsence org1 = new RefTypeAbsence();
		org1.setIdRefTypeAbsence(1);
		org1.setLabel("lib1");
		org1.setGroupe("A");
		absEntityManager.persist(org1);
		RefTypeAbsence org2 = new RefTypeAbsence();
		org2.setIdRefTypeAbsence(2);
		org2.setLabel("lib2");
		org2.setGroupe("A");
		absEntityManager.persist(org2);

		// When
		List<RefTypeAbsence> result = repository.findAllRefTypeAbsences();

		// Then
		assertEquals(2, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void findAllRefTypeSaisi() {

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(1);
		absEntityManager.persist(type);

		RefTypeAbsence type2 = new RefTypeAbsence();
		type2.setIdRefTypeAbsence(2);
		absEntityManager.persist(type2);

		RefTypeSaisi rts = new RefTypeSaisi();
		rts.setIdRefTypeAbsence(1);
		rts.setType(type);
		rts.setCalendarDateDebut(true);
		rts.setCalendarHeureDebut(true);
		absEntityManager.persist(rts);

		RefTypeSaisi rts2 = new RefTypeSaisi();
		rts2.setIdRefTypeAbsence(2);
		rts2.setType(type2);
		rts2.setCalendarDateFin(true);
		rts2.setCalendarHeureFin(true);
		absEntityManager.persist(rts2);

		List<RefTypeSaisi> result = repository.findAllRefTypeSaisi();

		// Then
		assertEquals(2, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void findRefTypeSaisi_type1() {

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(1);
		absEntityManager.persist(type);

		RefTypeAbsence type2 = new RefTypeAbsence();
		type2.setIdRefTypeAbsence(2);
		absEntityManager.persist(type2);

		RefTypeSaisi rts = new RefTypeSaisi();
		rts.setIdRefTypeAbsence(1);
		rts.setType(type);
		rts.setCalendarDateDebut(true);
		rts.setCalendarHeureDebut(true);
		absEntityManager.persist(rts);

		RefTypeSaisi rts2 = new RefTypeSaisi();
		rts2.setIdRefTypeAbsence(2);
		rts2.setType(type2);
		rts2.setCalendarDateFin(true);
		rts2.setCalendarHeureFin(true);
		absEntityManager.persist(rts2);

		RefTypeSaisi result = repository.findRefTypeSaisi(1);

		// Then
		assertTrue(result.isCalendarDateDebut());
		assertTrue(result.isCalendarHeureDebut());
		assertFalse(result.isCalendarDateFin());
		assertFalse(result.isCalendarHeureFin());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void findRefTypeSaisi_type2() {

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(1);
		absEntityManager.persist(type);

		RefTypeAbsence type2 = new RefTypeAbsence();
		type2.setIdRefTypeAbsence(2);
		absEntityManager.persist(type2);

		RefTypeSaisi rts = new RefTypeSaisi();
		rts.setIdRefTypeAbsence(1);
		rts.setType(type);
		rts.setCalendarDateDebut(true);
		rts.setCalendarHeureDebut(true);
		absEntityManager.persist(rts);

		RefTypeSaisi rts2 = new RefTypeSaisi();
		rts2.setIdRefTypeAbsence(2);
		rts2.setType(type2);
		rts2.setCalendarDateFin(true);
		rts2.setCalendarHeureFin(true);
		absEntityManager.persist(rts2);

		RefTypeSaisi result = repository.findRefTypeSaisi(2);

		// Then
		assertFalse(result.isCalendarDateDebut());
		assertFalse(result.isCalendarHeureDebut());
		assertTrue(result.isCalendarDateFin());
		assertTrue(result.isCalendarHeureFin());

		absEntityManager.flush();
		absEntityManager.clear();
	}
}
