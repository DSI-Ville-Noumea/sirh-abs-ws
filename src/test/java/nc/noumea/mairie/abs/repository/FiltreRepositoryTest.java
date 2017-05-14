package nc.noumea.mairie.abs.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nc.noumea.mairie.abs.domain.RefEtat;
import nc.noumea.mairie.abs.domain.RefGroupeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeSaisi;
import nc.noumea.mairie.abs.domain.RefUnitePeriodeQuota;

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
	public void findRefEtatPlanning() {
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
		List<RefEtat> result = repository.findRefEtatPlanning();

		// Then
		assertEquals(9, result.size());

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
		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(1);
		groupe.setCode("A");
		absEntityManager.persist(groupe);
		RefTypeAbsence org1 = new RefTypeAbsence();
		org1.setLabel("lib1");
		org1.setGroupe(groupe);
		org1.setActif(true);
		absEntityManager.persist(org1);
		RefTypeAbsence org2 = new RefTypeAbsence();
		org2.setLabel("lib2");
		org2.setGroupe(groupe);
		org2.setActif(true);
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
	public void findAllRefTypeAbsences_NoActif() {
		// Given
		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(1);
		groupe.setCode("A");
		absEntityManager.persist(groupe);
		RefTypeAbsence org1 = new RefTypeAbsence();
		org1.setLabel("lib1");
		org1.setGroupe(groupe);
		absEntityManager.persist(org1);
		RefTypeAbsence org2 = new RefTypeAbsence();
		org2.setLabel("lib2");
		org2.setGroupe(groupe);
		absEntityManager.persist(org2);

		// When
		List<RefTypeAbsence> result = repository.findAllRefTypeAbsences();

		// Then
		assertEquals(0, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void findAllRefTypeSaisi() {

		RefTypeAbsence type = new RefTypeAbsence();
		absEntityManager.persist(type);

		RefTypeAbsence type2 = new RefTypeAbsence();
		absEntityManager.persist(type2);

		RefTypeSaisi rts = new RefTypeSaisi();
		rts.setType(type);
		rts.setCalendarDateDebut(true);
		rts.setCalendarHeureDebut(true);
		rts.setUniteDecompte("jours");
		absEntityManager.persist(rts);

		RefTypeSaisi rts2 = new RefTypeSaisi();
		rts2.setType(type2);
		rts2.setCalendarDateFin(true);
		rts2.setCalendarHeureFin(true);
		rts2.setUniteDecompte("jours");
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
		absEntityManager.persist(type);

		RefTypeAbsence type2 = new RefTypeAbsence();
		absEntityManager.persist(type2);

		RefTypeSaisi rts = new RefTypeSaisi();
		rts.setType(type);
		rts.setCalendarDateDebut(true);
		rts.setCalendarHeureDebut(true);
		rts.setUniteDecompte("jours");
		absEntityManager.persist(rts);

		RefTypeSaisi rts2 = new RefTypeSaisi();
		rts2.setType(type2);
		rts2.setCalendarDateFin(true);
		rts2.setCalendarHeureFin(true);
		rts2.setUniteDecompte("jours");
		absEntityManager.persist(rts2);

		RefTypeSaisi result = repository.findRefTypeSaisi(type.getIdRefTypeAbsence());

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
		absEntityManager.persist(type);

		RefTypeAbsence type2 = new RefTypeAbsence();
		absEntityManager.persist(type2);

		RefTypeSaisi rts = new RefTypeSaisi();
		rts.setType(type);
		rts.setCalendarDateDebut(true);
		rts.setCalendarHeureDebut(true);
		rts.setUniteDecompte("jours");
		absEntityManager.persist(rts);

		RefTypeSaisi rts2 = new RefTypeSaisi();
		rts2.setType(type2);
		rts2.setCalendarDateFin(true);
		rts2.setCalendarHeureFin(true);
		rts2.setUniteDecompte("jours");
		absEntityManager.persist(rts2);

		RefTypeSaisi result = repository.findRefTypeSaisi(type2.getIdRefTypeAbsence());

		// Then
		assertFalse(result.isCalendarDateDebut());
		assertFalse(result.isCalendarHeureDebut());
		assertTrue(result.isCalendarDateFin());
		assertTrue(result.isCalendarHeureFin());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void findAllRefGroupeAbsence() {

		RefGroupeAbsence rga = new RefGroupeAbsence();
		rga.setIdRefGroupeAbsence(1);
		rga.setCode("code 1");
		rga.setLibelle("libelle 1");
		absEntityManager.persist(rga);

		RefGroupeAbsence rga2 = new RefGroupeAbsence();
		rga2.setIdRefGroupeAbsence(2);
		rga2.setCode("code 2");
		rga2.setLibelle("libelle 2");
		absEntityManager.persist(rga2);

		List<RefGroupeAbsence> result = repository.findAllRefGroupeAbsence();

		assertEquals(2, result.size());
		assertEquals("code 1", result.get(0).getCode());
		assertEquals("code 2", result.get(1).getCode());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void findRefGroupeAbsence() {

		RefGroupeAbsence rga = new RefGroupeAbsence();
		rga.setIdRefGroupeAbsence(1);
		rga.setCode("code 1");
		rga.setLibelle("libelle 1");
		absEntityManager.persist(rga);

		RefGroupeAbsence rga2 = new RefGroupeAbsence();
		rga2.setIdRefGroupeAbsence(2);
		rga2.setCode("code 2");
		rga2.setLibelle("libelle 2");
		absEntityManager.persist(rga2);

		RefGroupeAbsence result = repository.findRefGroupeAbsence(1);

		assertEquals("code 1", result.getCode());
		assertEquals("libelle 1", result.getLibelle());

		result = repository.findRefGroupeAbsence(2);

		assertEquals("code 2", result.getCode());
		assertEquals("libelle 2", result.getLibelle());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void findAllRefUnitePeriodeQuota() {

		RefUnitePeriodeQuota rupq = new RefUnitePeriodeQuota();
		rupq.setIdRefUnitePeriodeQuota(1);
		rupq.setUnite("jours");
		rupq.setValeur(10);
		rupq.setGlissant(true);
		absEntityManager.persist(rupq);

		RefUnitePeriodeQuota rupq2 = new RefUnitePeriodeQuota();
		rupq2.setIdRefUnitePeriodeQuota(2);
		rupq2.setUnite("minutes");
		rupq2.setValeur(13);
		rupq2.setGlissant(false);
		absEntityManager.persist(rupq2);

		List<RefUnitePeriodeQuota> result = repository.findAllRefUnitePeriodeQuota();

		assertEquals(2, result.size());
		assertEquals("jours", result.get(0).getUnite());
		assertEquals(10, result.get(0).getValeur().intValue());
		assertTrue(result.get(0).isGlissant());
		assertEquals("minutes", result.get(1).getUnite());
		assertEquals(13, result.get(1).getValeur().intValue());
		assertFalse(result.get(1).isGlissant());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void findAllRefTypeAbsencesWithGroup() {

		RefGroupeAbsence groupeRecup = new RefGroupeAbsence();
		groupeRecup.setIdRefGroupeAbsence(2);
		groupeRecup.setLibelle("recup");
		absEntityManager.persist(groupeRecup);
		RefTypeAbsence rupq = new RefTypeAbsence();
		rupq.setActif(true);
		rupq.setGroupe(groupeRecup);
		absEntityManager.persist(rupq);

		RefGroupeAbsence groupeAsa = new RefGroupeAbsence();
		groupeAsa.setIdRefGroupeAbsence(1);
		groupeAsa.setLibelle("asa");
		absEntityManager.persist(groupeAsa);
		RefTypeAbsence rupq2 = new RefTypeAbsence();
		rupq2.setGroupe(groupeAsa);
		rupq2.setActif(true);
		absEntityManager.persist(rupq2);

		List<RefTypeAbsence> result = repository.findAllRefTypeAbsencesWithGroup(1);

		assertEquals(1, result.size());
		assertEquals("asa", result.get(0).getGroupe().getLibelle());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void findAllRefTypeAbsencesWithGroup_NoActif() {

		RefGroupeAbsence groupeRecup = new RefGroupeAbsence();
		groupeRecup.setIdRefGroupeAbsence(2);
		groupeRecup.setLibelle("recup");
		absEntityManager.persist(groupeRecup);
		RefTypeAbsence rupq = new RefTypeAbsence();
		rupq.setActif(true);
		rupq.setGroupe(groupeRecup);
		absEntityManager.persist(rupq);

		RefGroupeAbsence groupeAsa = new RefGroupeAbsence();
		groupeAsa.setIdRefGroupeAbsence(1);
		groupeAsa.setLibelle("asa");
		absEntityManager.persist(groupeAsa);
		RefTypeAbsence rupq2 = new RefTypeAbsence();
		rupq2.setGroupe(groupeAsa);
		rupq2.setActif(false);
		absEntityManager.persist(rupq2);

		List<RefTypeAbsence> result = repository.findAllRefTypeAbsencesWithGroup(1);

		assertEquals(0, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void findRefEtatAValider() {
		RefEtat etatAttente = new RefEtat();
		etatAttente.setLabel("EN ATTENTE");
		absEntityManager.persist(etatAttente);

		RefEtat etatAppr = new RefEtat();
		etatAppr.setLabel("APPROUVEE");
		absEntityManager.persist(etatAppr);

		RefEtat etatSaisie = new RefEtat();
		etatSaisie.setLabel("SAISIE");
		absEntityManager.persist(etatSaisie);

		RefEtat etatAValider = new RefEtat();
		etatAValider.setLabel("A VALIDER");
		absEntityManager.persist(etatAValider);

		List<RefEtat> result = repository.findRefEtatAValider();

		assertEquals(3, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}
}
