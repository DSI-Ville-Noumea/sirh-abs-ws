package nc.noumea.mairie.abs.repository;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeAsa;
import nc.noumea.mairie.abs.domain.DemandeRecup;
import nc.noumea.mairie.abs.domain.DemandeReposComp;
import nc.noumea.mairie.abs.domain.Droit;
import nc.noumea.mairie.abs.domain.DroitDroitsAgent;
import nc.noumea.mairie.abs.domain.DroitProfil;
import nc.noumea.mairie.abs.domain.DroitsAgent;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.Profil;
import nc.noumea.mairie.abs.domain.ProfilEnum;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefGroupeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.domain.RefTypeGroupeAbsenceEnum;

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

	SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

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
		List<Demande> result = repository.listeDemandesAgent(null, 9005138, null, null, null, null);

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
		List<Demande> result = repository.listeDemandesAgent(null, 9005138, null, null, null, null);

		// Then
		assertEquals(0, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void listeDemandesAgent_DateFilter_Return2Demande() throws ParseException {
		// Given

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
		List<Demande> result = repository.listeDemandesAgent(null, 9005138, sdf.parse("01/06/2013"), null, null, null);

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
				sdf.parse("16/06/2013"), null, null);

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

		RefTypeAbsence typeMaladie = new RefTypeAbsence();
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
		List<Demande> result = repository.listeDemandesAgent(null, 9005138, null, null, 99, null);

		// Then
		assertEquals(0, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void listeDemandesAgent_TypeFilter_Return2Demande() throws ParseException {
		// Given

		RefTypeAbsence typeMaladie = new RefTypeAbsence();
		typeMaladie.setLabel("Maladies");
		absEntityManager.persist(typeMaladie);
		RefTypeAbsence typeRecup = new RefTypeAbsence();
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
		List<Demande> result = repository.listeDemandesAgent(null, 9005138, null, null,
				typeRecup.getIdRefTypeAbsence(), null);

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
		List<Demande> result = repository.listeDemandesAgent(9005138, null, null, null, null, null);

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
		List<Demande> result = repository.listeDemandesAgent(9005138, null, null, null, null, null);

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

		// Recup
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

		// Repos Comp
		Droit droitViseur2 = new Droit();
		droitViseur2.setIdAgent(9000002);
		absEntityManager.persist(droitViseur2);
		Profil profil2 = new Profil();
		profil2.setLibelle(ProfilEnum.VISEUR.toString());
		absEntityManager.persist(profil2);
		DroitProfil droitProfil2 = new DroitProfil();
		droitProfil2.setProfil(profil2);
		droitProfil2.setDroit(droitViseur2);
		absEntityManager.persist(droitProfil2);

		DroitsAgent droitsAgent2 = new DroitsAgent();
		droitsAgent2.setIdAgent(9000012);
		absEntityManager.persist(droitsAgent2);
		DroitDroitsAgent dda2 = new DroitDroitsAgent();
		dda2.setDroit(droitViseur2);
		dda2.setDroitProfil(droitProfil2);
		dda2.setDroitsAgent(droitsAgent2);
		absEntityManager.persist(dda2);

		RefTypeAbsence rta2 = new RefTypeAbsence();
		rta2.setIdRefTypeAbsence(RefTypeAbsenceEnum.REPOS_COMP.getValue());
		absEntityManager.persist(rta2);
		Demande demande2 = new Demande();
		demande2.setIdAgent(9000012);
		demande2.setType(rta2);
		absEntityManager.persist(demande2);
		EtatDemande etatDemande2 = new EtatDemande();
		etatDemande2.setDemande(demande2);
		etatDemande2.setIdAgent(9000001);
		etatDemande2.setEtat(RefEtatEnum.VISEE_FAVORABLE);
		absEntityManager.persist(etatDemande2);

		List<Integer> listeTypes = new ArrayList<Integer>();
		listeTypes.add(RefTypeGroupeAbsenceEnum.RECUP.getValue());
		listeTypes.add(RefTypeGroupeAbsenceEnum.REPOS_COMP.getValue());
		listeTypes.add(RefTypeGroupeAbsenceEnum.ASA.getValue());
		listeTypes.add(RefTypeGroupeAbsenceEnum.CONGES_EXCEP.getValue());

		// When
		List<Integer> result = repository.getListViseursDemandesSaisiesJourDonne(listeTypes);

		assertEquals(2, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	// @Test
	@Transactional("absTransactionManager")
	public void getListApprobateursDemandesSaisiesJourDonne() {

		// Recup
		Droit droitApprobateur = new Droit();
		droitApprobateur.setIdAgent(9000001);
		absEntityManager.persist(droitApprobateur);
		Profil profil = new Profil();
		profil.setLibelle(ProfilEnum.APPROBATEUR.toString());
		absEntityManager.persist(profil);
		DroitProfil droitProfil = new DroitProfil();
		droitProfil.setProfil(profil);
		droitProfil.setDroit(droitApprobateur);
		absEntityManager.persist(droitProfil);

		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(9000011);
		absEntityManager.persist(droitsAgent);
		DroitDroitsAgent dda = new DroitDroitsAgent();
		dda.setDroit(droitApprobateur);
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

		// Repos Comp
		Droit droitAppro2 = new Droit();
		droitAppro2.setIdAgent(9000002);
		absEntityManager.persist(droitAppro2);
		Profil profil2 = new Profil();
		profil2.setLibelle(ProfilEnum.APPROBATEUR.toString());
		absEntityManager.persist(profil2);
		DroitProfil droitProfil2 = new DroitProfil();
		droitProfil2.setProfil(profil2);
		droitProfil2.setDroit(droitAppro2);
		absEntityManager.persist(droitProfil2);

		DroitsAgent droitsAgent2 = new DroitsAgent();
		droitsAgent2.setIdAgent(9000012);
		absEntityManager.persist(droitsAgent2);
		DroitDroitsAgent dda2 = new DroitDroitsAgent();
		dda2.setDroit(droitAppro2);
		dda2.setDroitProfil(droitProfil2);
		dda2.setDroitsAgent(droitsAgent2);
		absEntityManager.persist(dda2);

		RefTypeAbsence rta2 = new RefTypeAbsence();
		rta2.setIdRefTypeAbsence(RefTypeAbsenceEnum.REPOS_COMP.getValue());
		absEntityManager.persist(rta2);
		Demande demande2 = new Demande();
		demande2.setIdAgent(9000012);
		demande2.setType(rta2);
		absEntityManager.persist(demande2);
		EtatDemande etatDemande2 = new EtatDemande();
		etatDemande2.setDemande(demande2);
		etatDemande2.setIdAgent(9000001);
		etatDemande2.setEtat(RefEtatEnum.VISEE_FAVORABLE);
		absEntityManager.persist(etatDemande2);

		// ASA A48
		Droit droitAppro3 = new Droit();
		droitAppro3.setIdAgent(9000002);
		absEntityManager.persist(droitAppro3);
		DroitProfil droitProfil3 = new DroitProfil();
		droitProfil3.setProfil(profil2);
		droitProfil3.setDroit(droitAppro3);
		absEntityManager.persist(droitProfil3);

		DroitsAgent droitsAgent3 = new DroitsAgent();
		droitsAgent3.setIdAgent(9000012);
		absEntityManager.persist(droitsAgent3);
		DroitDroitsAgent dda3 = new DroitDroitsAgent();
		dda3.setDroit(droitAppro3);
		dda3.setDroitProfil(droitProfil3);
		dda3.setDroitsAgent(droitsAgent3);
		absEntityManager.persist(dda3);

		RefTypeAbsence rta3 = new RefTypeAbsence();
		rta3.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A48.getValue());
		absEntityManager.persist(rta3);
		Demande demande3 = new Demande();
		demande3.setIdAgent(9000012);
		demande3.setType(rta3);
		absEntityManager.persist(demande3);
		EtatDemande etatDemande3 = new EtatDemande();
		etatDemande3.setDemande(demande3);
		etatDemande3.setIdAgent(9000001);
		etatDemande3.setEtat(RefEtatEnum.VISEE_DEFAVORABLE);
		absEntityManager.persist(etatDemande3);

		// ASA A54
		Droit droitAppro4 = new Droit();
		droitAppro4.setIdAgent(9000002);
		absEntityManager.persist(droitAppro4);
		DroitProfil droitProfil4 = new DroitProfil();
		droitProfil4.setProfil(profil2);
		droitProfil4.setDroit(droitAppro4);
		absEntityManager.persist(droitProfil4);

		DroitsAgent droitsAgent4 = new DroitsAgent();
		droitsAgent4.setIdAgent(9000012);
		absEntityManager.persist(droitsAgent4);
		DroitDroitsAgent dda4 = new DroitDroitsAgent();
		dda4.setDroit(droitAppro4);
		dda4.setDroitProfil(droitProfil4);
		dda4.setDroitsAgent(droitsAgent4);
		absEntityManager.persist(dda4);

		RefTypeAbsence rta4 = new RefTypeAbsence();
		rta4.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A54.getValue());
		absEntityManager.persist(rta4);
		Demande demande4 = new Demande();
		demande4.setIdAgent(9000012);
		demande4.setType(rta4);
		absEntityManager.persist(demande4);
		EtatDemande etatDemande4 = new EtatDemande();
		etatDemande4.setDemande(demande4);
		etatDemande4.setIdAgent(9000001);
		etatDemande4.setEtat(RefEtatEnum.VISEE_DEFAVORABLE);
		absEntityManager.persist(etatDemande4);

		// ASA A55
		Droit droitAppro5 = new Droit();
		droitAppro4.setIdAgent(9000002);
		absEntityManager.persist(droitAppro5);
		DroitProfil droitProfil5 = new DroitProfil();
		droitProfil5.setProfil(profil2);
		droitProfil5.setDroit(droitAppro5);
		absEntityManager.persist(droitProfil5);

		DroitsAgent droitsAgent5 = new DroitsAgent();
		droitsAgent5.setIdAgent(9000012);
		absEntityManager.persist(droitsAgent5);
		DroitDroitsAgent dda5 = new DroitDroitsAgent();
		dda5.setDroit(droitAppro5);
		dda5.setDroitProfil(droitProfil5);
		dda5.setDroitsAgent(droitsAgent5);
		absEntityManager.persist(dda5);

		Demande demande5 = new Demande();
		demande5.setIdAgent(9000012);
		demande5.setType(rta4);
		absEntityManager.persist(demande5);
		EtatDemande etatDemande5 = new EtatDemande();
		etatDemande5.setDemande(demande5);
		etatDemande5.setIdAgent(9000001);
		etatDemande5.setEtat(RefEtatEnum.VISEE_DEFAVORABLE);
		absEntityManager.persist(etatDemande5);

		List<Integer> listeTypes = new ArrayList<Integer>();
		listeTypes.add(RefTypeAbsenceEnum.RECUP.getValue());
		listeTypes.add(RefTypeAbsenceEnum.REPOS_COMP.getValue());
		listeTypes.add(RefTypeAbsenceEnum.ASA_A48.getValue());
		listeTypes.add(RefTypeAbsenceEnum.ASA_A54.getValue());
		listeTypes.add(RefTypeAbsenceEnum.ASA_A55.getValue());

		// When
		List<Integer> result = repository.getListApprobateursDemandesSaisiesViseesJourDonne(listeTypes);

		assertEquals(5, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void listeDemandesSIRH_NoFilter_Return2Demande() {
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
		List<Demande> result = repository.listeDemandesSIRH(null, null, null, null, null, null);

		// Then
		assertEquals(2, result.size());
		assertEquals("30", ((DemandeRecup) result.get(1)).getDuree().toString());
		assertEquals("15", ((DemandeReposComp) result.get(0)).getDuree().toString());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void listeDemandesSIRH_TypeFilter_Return0Demande() throws ParseException {
		// Given

		RefTypeAbsence typeMaladie = new RefTypeAbsence();
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
		List<Demande> result = repository.listeDemandesSIRH(null, null, null, 99, null, null);

		// Then
		assertEquals(0, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void listeDemandesSIRH_TypeFilter_Return2Demande() throws ParseException {
		// Given
		RefTypeAbsence typeMaladie = new RefTypeAbsence();
		typeMaladie.setLabel("Maladies");
		absEntityManager.persist(typeMaladie);
		RefTypeAbsence typeRecup = new RefTypeAbsence();
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
		List<Demande> result = repository.listeDemandesSIRH(null, null, null, typeRecup.getIdRefTypeAbsence(), null,
				null);

		// Then
		assertEquals(2, result.size());
		assertEquals("40", ((DemandeRecup) result.get(1)).getDuree().toString());
		assertEquals("20", ((DemandeReposComp) result.get(0)).getDuree().toString());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void listeDemandesSIRH_DateFilter_Return2Demande() throws ParseException {
		// Given

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
		List<Demande> result = repository.listeDemandesSIRH(sdf.parse("01/06/2013"), null, null, null, null, null);

		// Then
		assertEquals(2, result.size());
		assertEquals("20", ((DemandeReposComp) result.get(0)).getDuree().toString());
		assertEquals("30", ((DemandeRecup) result.get(1)).getDuree().toString());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void listeDemandesSIRH_DateFinFilter_Return2Demande() throws ParseException {
		// Given

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
		List<Demande> result = repository.listeDemandesSIRH(sdf.parse("01/06/2013"), sdf.parse("16/06/2013"), null,
				null, null, null);

		// Then
		assertEquals(2, result.size());
		assertEquals("40", ((DemandeRecup) result.get(1)).getDuree().toString());
		assertEquals("20", ((DemandeReposComp) result.get(0)).getDuree().toString());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void listeDemandesAgent_GroupeFilter_Return4Demande() throws ParseException {
		// Given
		RefGroupeAbsence groupeAsa = new RefGroupeAbsence();
		groupeAsa.setIdRefGroupeAbsence(2);
		absEntityManager.persist(groupeAsa);

		RefTypeAbsence typeAsaA48 = new RefTypeAbsence();
		typeAsaA48.setLabel("A48");
		typeAsaA48.setGroupe(groupeAsa);
		absEntityManager.persist(typeAsaA48);

		RefTypeAbsence typeAsaA49 = new RefTypeAbsence();
		typeAsaA49.setLabel("A49");
		typeAsaA49.setGroupe(groupeAsa);
		absEntityManager.persist(typeAsaA49);

		DemandeAsa d = new DemandeAsa();
		d.setIdAgent(9005138);
		d.setDateDebut(sdf.parse("15/05/2013"));
		d.setDateFin(null);
		d.setDuree(10.0);
		d.setType(typeAsaA48);
		absEntityManager.persist(d);

		DemandeAsa d2 = new DemandeAsa();
		d2.setIdAgent(9005138);
		d2.setDateDebut(sdf.parse("15/06/2013"));
		d2.setDateFin(null);
		d2.setDuree(20.0);
		d2.setType(typeAsaA48);
		absEntityManager.persist(d2);

		DemandeAsa drp = new DemandeAsa();
		drp.setIdAgent(9005138);
		drp.setDateDebut(sdf.parse("15/05/2013"));
		drp.setDateFin(null);
		drp.setDuree(30.0);
		drp.setType(typeAsaA49);
		absEntityManager.persist(drp);

		DemandeAsa drp2 = new DemandeAsa();
		drp2.setIdAgent(9005138);
		drp2.setDateDebut(sdf.parse("15/06/2013"));
		drp2.setDateFin(null);
		drp2.setDuree(40.0);
		drp2.setType(typeAsaA49);
		absEntityManager.persist(drp2);

		// When
		List<Demande> result = repository.listeDemandesAgent(null, 9005138, null, null, null, 2);

		// Then
		assertEquals(4, result.size());
		assertEquals("10.0", ((DemandeAsa) result.get(3)).getDuree().toString());
		assertEquals("20.0", ((DemandeAsa) result.get(2)).getDuree().toString());
		assertEquals("30.0", ((DemandeAsa) result.get(1)).getDuree().toString());
		assertEquals("40.0", ((DemandeAsa) result.get(0)).getDuree().toString());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void listeDemandesAgent_GroupeFilter_Return2Demande() throws ParseException {
		// Given
		RefGroupeAbsence groupeReposComp = new RefGroupeAbsence();
		groupeReposComp.setIdRefGroupeAbsence(2);
		absEntityManager.persist(groupeReposComp);
		RefGroupeAbsence groupeRecup = new RefGroupeAbsence();
		groupeRecup.setIdRefGroupeAbsence(1);
		absEntityManager.persist(groupeRecup);

		RefTypeAbsence typeReposComp = new RefTypeAbsence();
		typeReposComp.setLabel("Maladies");
		typeReposComp.setGroupe(groupeReposComp);
		absEntityManager.persist(typeReposComp);
		RefTypeAbsence typeRecup = new RefTypeAbsence();
		typeRecup.setLabel("Récupération");
		typeRecup.setGroupe(groupeRecup);
		absEntityManager.persist(typeRecup);

		DemandeRecup d = new DemandeRecup();
		d.setIdAgent(9005138);
		d.setDateDebut(sdf.parse("15/05/2013"));
		d.setDateFin(null);
		d.setDuree(30);
		d.setType(typeRecup);
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
		drp.setType(typeReposComp);
		absEntityManager.persist(drp);

		DemandeReposComp drp2 = new DemandeReposComp();
		drp2.setIdAgent(9005138);
		drp2.setDateDebut(sdf.parse("15/06/2013"));
		drp2.setDateFin(null);
		drp2.setDuree(20);
		drp2.setDureeAnneeN1(10);
		drp2.setType(typeReposComp);
		absEntityManager.persist(drp2);

		// When
		List<Demande> result = repository.listeDemandesAgent(null, 9005138, null, null, null, 1);

		// Then
		assertEquals(2, result.size());
		assertEquals("30", ((DemandeRecup) result.get(1)).getDuree().toString());
		assertEquals("40", ((DemandeRecup) result.get(0)).getDuree().toString());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void listeDemandesSIRHAValider_Return0Demande() throws ParseException {
		// Given

		RefTypeAbsence typeMaladie = new RefTypeAbsence();
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
		List<Demande> result = repository.listeDemandesSIRHAValider();

		// Then
		assertEquals(0, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void listeDemandesSIRHAValider_Return2Demande() throws ParseException {
		// Given
		RefGroupeAbsence groupeRecup = new RefGroupeAbsence();
		groupeRecup.setIdRefGroupeAbsence(1);
		absEntityManager.persist(groupeRecup);

		RefGroupeAbsence groupeAsa = new RefGroupeAbsence();
		groupeAsa.setIdRefGroupeAbsence(3);
		absEntityManager.persist(groupeAsa);

		RefTypeAbsence typeRecup = new RefTypeAbsence();
		typeRecup.setGroupe(groupeRecup);
		typeRecup.setLabel("RECUP");
		absEntityManager.persist(typeRecup);

		RefTypeAbsence typeAsa = new RefTypeAbsence();
		typeAsa.setGroupe(groupeAsa);
		typeAsa.setLabel("ASA");
		absEntityManager.persist(typeAsa);

		DemandeAsa d = new DemandeAsa();
		d.setIdAgent(9005138);
		d.setDateDebut(sdf.parse("15/05/2013"));
		d.setDateFin(null);
		d.setType(typeAsa);
		absEntityManager.persist(d);

		EtatDemande etatDemandeAppr = new EtatDemande();
		etatDemandeAppr.setDemande(d);
		etatDemandeAppr.setIdAgent(9005138);
		etatDemandeAppr.setEtat(RefEtatEnum.APPROUVEE);
		absEntityManager.persist(etatDemandeAppr);

		DemandeAsa dR2 = new DemandeAsa();
		dR2.setIdAgent(9005139);
		dR2.setDateDebut(sdf.parse("15/06/2013"));
		dR2.setDateFin(null);
		dR2.setType(typeAsa);
		absEntityManager.persist(dR2);

		EtatDemande etatDemandeSaisie = new EtatDemande();
		etatDemandeSaisie.setDemande(dR2);
		etatDemandeSaisie.setIdAgent(9005138);
		etatDemandeSaisie.setEtat(RefEtatEnum.SAISIE);
		absEntityManager.persist(etatDemandeSaisie);

		DemandeRecup drp = new DemandeRecup();
		drp.setIdAgent(9005139);
		drp.setDateDebut(sdf.parse("15/05/2013"));
		drp.setDateFin(null);
		drp.setType(typeRecup);
		absEntityManager.persist(drp);

		EtatDemande etatDemandeAttente = new EtatDemande();
		etatDemandeAttente.setDemande(drp);
		etatDemandeAttente.setIdAgent(9005138);
		etatDemandeAttente.setEtat(RefEtatEnum.EN_ATTENTE);
		absEntityManager.persist(etatDemandeAttente);

		// When
		List<Demande> result = repository.listeDemandesSIRHAValider();

		// Then
		assertEquals(2, result.size());
		assertEquals("9005138", ((DemandeAsa) result.get(1)).getIdAgent().toString());
		assertEquals("9005139", ((DemandeAsa) result.get(0)).getIdAgent().toString());

		absEntityManager.flush();
		absEntityManager.clear();
	}
}
