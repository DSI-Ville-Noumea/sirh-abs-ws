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
import nc.noumea.mairie.abs.domain.DemandeReposComp;
import nc.noumea.mairie.abs.domain.Droit;
import nc.noumea.mairie.abs.domain.DroitDroitsAgent;
import nc.noumea.mairie.abs.domain.DroitProfil;
import nc.noumea.mairie.abs.domain.DroitsAgent;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.Profil;
import nc.noumea.mairie.abs.domain.ProfilEnum;
import nc.noumea.mairie.abs.domain.RefEtat;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;

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
	public void listeDemandesAgent_NoFilter_Return2Demande() {
		// Given
		DemandeRecup dr = new DemandeRecup();
		dr.setIdAgent(9005138);
		dr.setDateDebut(new Date());
		dr.setDateFin(null);
		dr.setDuree(30);
		absEntityManager.persist(dr);

		DemandeReposComp drc = new DemandeReposComp();
		drc.setIdAgent(9005138);
		drc.setDateDebut(new Date());
		drc.setDateFin(null);
		drc.setDuree(15);
		drc.setDureeAnneeN1(10);
		absEntityManager.persist(drc);

		// When
		List<Demande> result = repository.listeDemandesAgent(null, 9005138, null, null, null);

		// Then
		assertEquals(2, result.size());
		assertEquals("30", ((DemandeRecup) result.get(1)).getDuree().toString());
		assertEquals("15", ((DemandeReposComp) result.get(0)).getDuree().toString());

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

		DemandeReposComp drp = new DemandeReposComp();
		drp.setIdAgent(9005131);
		drp.setDateDebut(new Date());
		drp.setDateFin(null);
		drp.setDuree(15);
		drp.setDureeAnneeN1(10);
		absEntityManager.persist(drp);

		// When
		List<Demande> result = repository.listeDemandesAgent(null, 9005138, null, null, null);

		// Then
		assertEquals(0, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void listeDemandesAgent_DateFilter_Return2Demande() throws ParseException {
		// Given
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

		DemandeRecup d = new DemandeRecup();
		d.setIdAgent(9005138);
		d.setDateDebut(sdf.parse("15/06/2013"));
		d.setDateFin(null);
		d.setDuree(30);
		absEntityManager.persist(d);

		DemandeRecup d2 = new DemandeRecup();
		d2.setIdAgent(9005138);
		d2.setDateDebut(sdf.parse("15/05/2013"));
		d2.setDateFin(null);
		d2.setDuree(40);
		absEntityManager.persist(d2);

		DemandeReposComp drp = new DemandeReposComp();
		drp.setIdAgent(9005138);
		drp.setDateDebut(sdf.parse("02/05/2013"));
		drp.setDateFin(null);
		drp.setDuree(15);
		drp.setDureeAnneeN1(10);
		absEntityManager.persist(drp);

		DemandeReposComp drp2 = new DemandeReposComp();
		drp2.setIdAgent(9005138);
		drp2.setDateDebut(sdf.parse("15/06/2013"));
		drp2.setDateFin(null);
		drp2.setDuree(20);
		drp2.setDureeAnneeN1(10);
		absEntityManager.persist(drp2);

		// When
		List<Demande> result = repository.listeDemandesAgent(null, 9005138, sdf.parse("01/06/2013"), null, null);

		// Then
		assertEquals(2, result.size());
		assertEquals("20", ((DemandeReposComp) result.get(0)).getDuree().toString());
		assertEquals("30", ((DemandeRecup) result.get(1)).getDuree().toString());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void listeDemandesAgent_DateFinFilter_Return2Demande() throws ParseException {
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

		DemandeReposComp drp = new DemandeReposComp();
		drp.setIdAgent(9005138);
		drp.setDateDebut(sdf.parse("15/05/2013"));
		drp.setDateFin(null);
		drp.setDuree(15);
		drp.setDureeAnneeN1(10);
		absEntityManager.persist(drp);

		DemandeReposComp drp2 = new DemandeReposComp();
		drp2.setIdAgent(9005138);
		drp2.setDateDebut(sdf.parse("15/06/2013"));
		drp2.setDateFin(null);
		drp2.setDuree(20);
		drp2.setDureeAnneeN1(10);
		absEntityManager.persist(drp2);

		// When
		List<Demande> result = repository.listeDemandesAgent(null, 9005138, sdf.parse("01/06/2013"),
				sdf.parse("16/06/2013"), null);

		// Then
		assertEquals(2, result.size());
		assertEquals("40", ((DemandeRecup) result.get(1)).getDuree().toString());
		assertEquals("20", ((DemandeReposComp) result.get(0)).getDuree().toString());

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

		DemandeReposComp drp = new DemandeReposComp();
		drp.setIdAgent(9005138);
		drp.setDateDebut(sdf.parse("15/05/2013"));
		drp.setDateFin(null);
		drp.setDuree(15);
		drp.setDureeAnneeN1(10);
		drp.setType(typeMaladie);
		absEntityManager.persist(drp);

		DemandeReposComp drp2 = new DemandeReposComp();
		drp2.setIdAgent(9005138);
		drp2.setDateDebut(sdf.parse("15/06/2013"));
		drp2.setDateFin(null);
		drp2.setDuree(20);
		drp2.setDureeAnneeN1(10);
		drp2.setType(typeMaladie);
		absEntityManager.persist(drp2);

		// When
		List<Demande> result = repository.listeDemandesAgent(null, 9005138, null, null, 3);

		// Then
		assertEquals(0, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void listeDemandesAgent_TypeFilter_Return2Demande() throws ParseException {
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

		DemandeReposComp drp = new DemandeReposComp();
		drp.setIdAgent(9005138);
		drp.setDateDebut(sdf.parse("15/05/2013"));
		drp.setDateFin(null);
		drp.setDuree(15);
		drp.setDureeAnneeN1(10);
		drp.setType(typeMaladie);
		absEntityManager.persist(drp);

		DemandeReposComp drp2 = new DemandeReposComp();
		drp2.setIdAgent(9005138);
		drp2.setDateDebut(sdf.parse("15/06/2013"));
		drp2.setDateFin(null);
		drp2.setDuree(20);
		drp2.setDureeAnneeN1(10);
		drp2.setType(typeRecup);
		absEntityManager.persist(drp2);

		// When
		List<Demande> result = repository.listeDemandesAgent(null, 9005138, null, null, 3);

		// Then
		assertEquals(2, result.size());
		assertEquals("40", ((DemandeRecup) result.get(1)).getDuree().toString());
		assertEquals("20", ((DemandeReposComp) result.get(0)).getDuree().toString());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void listeDemandesAgent_MultiAgent_Return6Demandes() throws ParseException {
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

		DemandeReposComp dpr = new DemandeReposComp();
		dpr.setIdAgent(9005130);
		dpr.setDateDebut(sdf.parse("15/05/2013"));
		dpr.setDateFin(null);
		dpr.setDuree(30);
		dpr.setDureeAnneeN1(10);
		absEntityManager.persist(dpr);

		DemandeReposComp dpr2 = new DemandeReposComp();
		dpr2.setIdAgent(9005131);
		dpr2.setDateDebut(sdf.parse("15/06/2013"));
		dpr2.setDateFin(null);
		dpr2.setDuree(40);
		dpr2.setDureeAnneeN1(10);
		absEntityManager.persist(dpr2);

		DemandeReposComp dpr3 = new DemandeReposComp();
		dpr3.setIdAgent(9005132);
		dpr3.setDateDebut(sdf.parse("15/07/2013"));
		dpr3.setDateFin(null);
		dpr3.setDuree(50);
		dpr3.setDureeAnneeN1(10);
		absEntityManager.persist(dpr3);

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
		assertEquals(6, result.size());

		assertEquals(50, ((DemandeRecup) result.get(3)).getDuree().intValue());
		assertEquals(40, ((DemandeRecup) result.get(4)).getDuree().intValue());
		assertEquals(30, ((DemandeRecup) result.get(5)).getDuree().intValue());

		assertEquals(50, ((DemandeReposComp) result.get(0)).getDuree().intValue());
		assertEquals(40, ((DemandeReposComp) result.get(1)).getDuree().intValue());
		assertEquals(30, ((DemandeReposComp) result.get(2)).getDuree().intValue());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void listeDemandesAgent_MultiAgent_Return4DemandesOf6() throws ParseException {
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

		DemandeReposComp drp = new DemandeReposComp();
		drp.setIdAgent(9005130);
		drp.setDateDebut(sdf.parse("15/05/2013"));
		drp.setDateFin(null);
		drp.setDuree(30);
		drp.setDureeAnneeN1(10);
		absEntityManager.persist(drp);

		DemandeReposComp drp2 = new DemandeReposComp();
		drp2.setIdAgent(9005131);
		drp2.setDateDebut(sdf.parse("15/06/2013"));
		drp2.setDateFin(null);
		drp2.setDuree(40);
		drp2.setDureeAnneeN1(10);
		absEntityManager.persist(drp2);

		DemandeReposComp drp3 = new DemandeReposComp();
		drp3.setIdAgent(9005132);
		drp3.setDateDebut(sdf.parse("15/07/2013"));
		drp3.setDateFin(null);
		drp3.setDuree(50);
		drp3.setDureeAnneeN1(10);
		absEntityManager.persist(drp3);

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
		assertEquals(4, result.size());

		assertEquals(50, ((DemandeRecup) result.get(2)).getDuree().intValue());
		assertEquals(40, ((DemandeRecup) result.get(3)).getDuree().intValue());

		assertEquals(50, ((DemandeReposComp) result.get(0)).getDuree().intValue());
		assertEquals(40, ((DemandeReposComp) result.get(1)).getDuree().intValue());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	// @Test
	@Transactional("absTransactionManager")
	public void getListViseursDemandesSaisiesJourDonne() {

		Droit droitViseur = new Droit();
		droitViseur.setIdAgent(9000001);
		absEntityManager.persist(droitViseur);
		Profil profil = new Profil();
		profil.setLibelle(ProfilEnum.VISEUR.toString());
		absEntityManager.persist(profil);
		DroitProfil droitProfil = new DroitProfil();
		droitProfil.setProfil(profil);
		droitProfil.setDroit(droitViseur);
		absEntityManager.persist(droitProfil);

		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(9000011);
		absEntityManager.persist(droitsAgent);
		DroitDroitsAgent dda = new DroitDroitsAgent();
		dda.setDroit(droitViseur);
		dda.setDroitProfil(droitProfil);
		dda.setDroitsAgent(droitsAgent);
		absEntityManager.persist(dda);

		RefTypeAbsence rta = new RefTypeAbsence();
		rta.setIdRefTypeAbsence(RefTypeAbsenceEnum.RECUP.getValue());
		absEntityManager.persist(rta);
		Demande demande = new Demande();
		demande.setIdAgent(9000011);
		demande.setType(rta);
		absEntityManager.persist(demande);
		EtatDemande etatDemande = new EtatDemande();
		etatDemande.setDemande(demande);
		etatDemande.setIdAgent(9000001);
		etatDemande.setEtat(RefEtatEnum.SAISIE);
		absEntityManager.persist(etatDemande);

		// When
		List<Integer> result = repository.getListViseursDemandesSaisiesJourDonne(RefTypeAbsenceEnum.RECUP.getValue(), RefTypeAbsenceEnum.REPOS_COMP.getValue());

		assertEquals(1, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}
}
