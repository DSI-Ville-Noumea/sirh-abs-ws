package nc.noumea.mairie.abs.repository;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeRecup;
import nc.noumea.mairie.abs.domain.Droit;
import nc.noumea.mairie.abs.domain.DroitDroitsAgent;
import nc.noumea.mairie.abs.domain.DroitsAgent;
import nc.noumea.mairie.abs.domain.RefEtat;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/META-INF/spring/applicationContext-test.xml" })
public class DemandeRepositoryTest {

	@Autowired
	DemandeRepository repository;

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

		// When
		List<RefEtat> result = repository.findRefEtatEnCours();

		// Then
		assertEquals(4, result.size());

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
	public void listeDemandesAgent_NoFilter_Return1Demande() {
		// Given
		DemandeRecup d = new DemandeRecup();
		d.setIdAgent(9005138);
		d.setDateDebut(new Date());
		d.setDateFin(null);
		d.setDuree(30);
		absEntityManager.persist(d);

		// When
		List<Demande> result = repository.listeDemandesAgent(null, 9005138, null, null, null);

		// Then
		assertEquals(1, result.size());
		assertEquals("30", ((DemandeRecup) result.get(0)).getDuree().toString());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void listeDemandesAgent_NoFilter_Return0Demande() {
		// Given
		DemandeRecup d = new DemandeRecup();
		d.setIdAgent(9005131);
		d.setDateDebut(new Date());
		d.setDateFin(null);
		d.setDuree(30);
		absEntityManager.persist(d);

		// When
		List<Demande> result = repository.listeDemandesAgent(null, 9005138, null, null, null);

		// Then
		assertEquals(0, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void listeDemandesAgent_DateFilter_Return0Demande() throws ParseException {
		// Given
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

		DemandeRecup d = new DemandeRecup();
		d.setIdAgent(9005138);
		d.setDateDebut(sdf.parse("15/05/2013"));
		d.setDateFin(null);
		d.setDuree(30);
		absEntityManager.persist(d);

		DemandeRecup d2 = new DemandeRecup();
		d2.setIdAgent(9005138);
		d2.setDateDebut(sdf.parse("15/06/2013"));
		d2.setDateFin(null);
		d2.setDuree(40);
		absEntityManager.persist(d2);

		// When
		List<Demande> result = repository.listeDemandesAgent(null, 9005138, sdf.parse("01/06/2013"), null, null);

		// Then
		assertEquals(0, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void listeDemandesAgent_DateFilter_Return1Demande() throws ParseException {
		// Given
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

		DemandeRecup d = new DemandeRecup();
		d.setIdAgent(9005138);
		d.setDateDebut(sdf.parse("15/05/2013"));
		d.setDateFin(null);
		d.setDuree(30);
		absEntityManager.persist(d);

		DemandeRecup d2 = new DemandeRecup();
		d2.setIdAgent(9005138);
		d2.setDateDebut(sdf.parse("15/06/2013"));
		d2.setDateFin(null);
		d2.setDuree(40);
		absEntityManager.persist(d2);

		// When
		List<Demande> result = repository.listeDemandesAgent(null, 9005138, sdf.parse("01/06/2013"), sdf.parse("16/06/2013"),
				null);

		// Then
		assertEquals(1, result.size());
		assertEquals("40", ((DemandeRecup) result.get(0)).getDuree().toString());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void listeDemandesAgent_TypeFilter_Return0Demande() throws ParseException {
		// Given
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

		RefTypeAbsence typeMaladie = new RefTypeAbsence();
		typeMaladie.setIdRefTypeAbsence(6);
		typeMaladie.setLabel("Maladies");
		absEntityManager.persist(typeMaladie);

		DemandeRecup d = new DemandeRecup();
		d.setIdAgent(9005138);
		d.setDateDebut(sdf.parse("15/05/2013"));
		d.setDateFin(null);
		d.setDuree(30);
		d.setType(typeMaladie);
		absEntityManager.persist(d);

		DemandeRecup d2 = new DemandeRecup();
		d2.setIdAgent(9005138);
		d2.setDateDebut(sdf.parse("15/06/2013"));
		d2.setDateFin(null);
		d2.setDuree(40);
		d2.setType(typeMaladie);
		absEntityManager.persist(d2);

		// When
		List<Demande> result = repository.listeDemandesAgent(null, 9005138, null, null, 3);

		// Then
		assertEquals(0, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void listeDemandesAgent_TypeFilter_Return1Demande() throws ParseException {
		// Given
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		RefTypeAbsence typeMaladie = new RefTypeAbsence();
		typeMaladie.setIdRefTypeAbsence(6);
		typeMaladie.setLabel("Maladies");
		absEntityManager.persist(typeMaladie);
		RefTypeAbsence typeRecup = new RefTypeAbsence();
		typeRecup.setIdRefTypeAbsence(3);
		typeRecup.setLabel("Récupération");
		absEntityManager.persist(typeRecup);

		DemandeRecup d = new DemandeRecup();
		d.setIdAgent(9005138);
		d.setDateDebut(sdf.parse("15/05/2013"));
		d.setDateFin(null);
		d.setDuree(30);
		d.setType(typeMaladie);
		absEntityManager.persist(d);

		DemandeRecup dR2 = new DemandeRecup();
		dR2.setIdAgent(9005138);
		dR2.setDateDebut(sdf.parse("15/06/2013"));
		dR2.setDateFin(null);
		dR2.setDuree(40);
		dR2.setType(typeRecup);
		absEntityManager.persist(dR2);

		// When
		List<Demande> result = repository.listeDemandesAgent(null, 9005138, null, null, 3);

		// Then
		assertEquals(1, result.size());
		assertEquals("40", ((DemandeRecup) result.get(0)).getDuree().toString());

		absEntityManager.flush();
		absEntityManager.clear();
	}
	
	@Test
	@Transactional("absTransactionManager")
	public void listeDemandesAgent_MultiAgent_Return3Demandes() throws ParseException {
		// Given
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

		DemandeRecup d = new DemandeRecup();
		d.setIdAgent(9005130);
		d.setDateDebut(sdf.parse("15/05/2013"));
		d.setDateFin(null);
		d.setDuree(30);
		absEntityManager.persist(d);

		DemandeRecup d2 = new DemandeRecup();
		d2.setIdAgent(9005131);
		d2.setDateDebut(sdf.parse("15/06/2013"));
		d2.setDateFin(null);
		d2.setDuree(40);
		absEntityManager.persist(d2);
		
		DemandeRecup d3 = new DemandeRecup();
		d3.setIdAgent(9005132);
		d3.setDateDebut(sdf.parse("15/07/2013"));
		d3.setDateFin(null);
		d3.setDuree(50);
		absEntityManager.persist(d3);

		Droit droit = new Droit();
		droit.setIdAgent(9005138);
		absEntityManager.persist(droit);
		
		DroitsAgent da1 = new DroitsAgent();
		da1.setIdAgent(9005130);
		absEntityManager.persist(da1);
		DroitsAgent da2 = new DroitsAgent();
		da2.setIdAgent(9005131);
		absEntityManager.persist(da2);
		DroitsAgent da3 = new DroitsAgent();
		da3.setIdAgent(9005132);
		absEntityManager.persist(da3);
		
		DroitDroitsAgent dda = new DroitDroitsAgent();
		dda.setDroitsAgent(da1);
		dda.setDroit(droit);
		absEntityManager.persist(dda);
		DroitDroitsAgent dda2 = new DroitDroitsAgent();
		dda2.setDroitsAgent(da2);
		dda2.setDroit(droit);
		absEntityManager.persist(dda2);
		DroitDroitsAgent dda3 = new DroitDroitsAgent();
		dda3.setDroitsAgent(da3);
		dda3.setDroit(droit);
		absEntityManager.persist(dda3);
		
		// When
		List<Demande> result = repository.listeDemandesAgent(9005138, null, null, null, null);
		
		// Then
		assertEquals(3, result.size());

		assertEquals(50, ((DemandeRecup)result.get(0)).getDuree().intValue());
		assertEquals(40, ((DemandeRecup)result.get(1)).getDuree().intValue());
		assertEquals(30, ((DemandeRecup)result.get(2)).getDuree().intValue());
		
		absEntityManager.flush();
		absEntityManager.clear();
	}
	
	@Test
	@Transactional("absTransactionManager")
	public void listeDemandesAgent_MultiAgent_Return2DemandesOf3() throws ParseException {
		// Given
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

		DemandeRecup d = new DemandeRecup();
		d.setIdAgent(9005130);
		d.setDateDebut(sdf.parse("15/05/2013"));
		d.setDateFin(null);
		d.setDuree(30);
		absEntityManager.persist(d);

		DemandeRecup d2 = new DemandeRecup();
		d2.setIdAgent(9005131);
		d2.setDateDebut(sdf.parse("15/06/2013"));
		d2.setDateFin(null);
		d2.setDuree(40);
		absEntityManager.persist(d2);
		
		DemandeRecup d3 = new DemandeRecup();
		d3.setIdAgent(9005132);
		d3.setDateDebut(sdf.parse("15/07/2013"));
		d3.setDateFin(null);
		d3.setDuree(50);
		absEntityManager.persist(d3);

		Droit droit = new Droit();
		droit.setIdAgent(9005138);
		absEntityManager.persist(droit);
		
		Droit droit2 = new Droit();
		droit2.setIdAgent(9005139);
		absEntityManager.persist(droit2);
		
		DroitsAgent da1 = new DroitsAgent();
		da1.setIdAgent(9005130);
		absEntityManager.persist(da1);
		DroitsAgent da2 = new DroitsAgent();
		da2.setIdAgent(9005131);
		absEntityManager.persist(da2);
		DroitsAgent da3 = new DroitsAgent();
		da3.setIdAgent(9005132);
		absEntityManager.persist(da3);
		
		DroitDroitsAgent dda = new DroitDroitsAgent();
		dda.setDroitsAgent(da1);
		dda.setDroit(droit2);
		absEntityManager.persist(dda);
		DroitDroitsAgent dda2 = new DroitDroitsAgent();
		dda2.setDroitsAgent(da2);
		dda2.setDroit(droit);
		absEntityManager.persist(dda2);
		DroitDroitsAgent dda3 = new DroitDroitsAgent();
		dda3.setDroitsAgent(da3);
		dda3.setDroit(droit);
		absEntityManager.persist(dda3);
		
		// When
		List<Demande> result = repository.listeDemandesAgent(9005138, null, null, null, null);
		
		// Then
		assertEquals(2, result.size());
		assertEquals(50, ((DemandeRecup)result.get(0)).getDuree().intValue());
		assertEquals(40, ((DemandeRecup)result.get(1)).getDuree().intValue());
		
		absEntityManager.flush();
		absEntityManager.clear();
	}
}
