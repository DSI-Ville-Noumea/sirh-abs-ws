package nc.noumea.mairie.abs.repository;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeAsa;
import nc.noumea.mairie.abs.domain.DemandeCongesAnnuels;
import nc.noumea.mairie.abs.domain.DemandeRecup;
import nc.noumea.mairie.abs.domain.DemandeReposComp;
import nc.noumea.mairie.abs.domain.Droit;
import nc.noumea.mairie.abs.domain.DroitDroitsAgent;
import nc.noumea.mairie.abs.domain.DroitProfil;
import nc.noumea.mairie.abs.domain.DroitsAgent;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.EtatDemandeCongesAnnuels;
import nc.noumea.mairie.abs.domain.Profil;
import nc.noumea.mairie.abs.domain.ProfilEnum;
import nc.noumea.mairie.abs.domain.RefEtat;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefGroupeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.domain.RefTypeGroupeAbsenceEnum;

import org.joda.time.DateTime;
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

		EtatDemande etat = new EtatDemande();
		etat.setIdAgent(9005138);
		etat.setDate(new Date());
		etat.setDateDebut(new Date());
		etat.setDateFin(new Date());
		etat.setMotif("motif");
		etat.setEtat(RefEtatEnum.APPROUVEE);

		DemandeRecup dr = new DemandeRecup();
		dr.setIdAgent(9005138);
		dr.setDateDebut(new Date());
		dr.setDateFin(null);
		dr.setDuree(30);
		dr.addEtatDemande(etat);
		absEntityManager.persist(dr);

		EtatDemande etatdrc = new EtatDemande();
		etatdrc.setIdAgent(9005138);
		etatdrc.setDate(new Date());
		etatdrc.setDateDebut(new Date());
		etatdrc.setDateFin(new Date());
		etatdrc.setMotif("motif");
		etatdrc.setEtat(RefEtatEnum.APPROUVEE);

		DemandeReposComp drc = new DemandeReposComp();
		drc.setIdAgent(9005138);
		drc.setDateDebut(new Date());
		drc.setDateFin(null);
		drc.setDuree(15);
		drc.setDureeAnneeN1(10);
		drc.addEtatDemande(etatdrc);
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
		EtatDemande etat = new EtatDemande();
		etat.setIdAgent(9005138);
		etat.setDate(new Date());
		etat.setDateDebut(new Date());
		etat.setDateFin(new Date());
		etat.setMotif("motif");
		etat.setEtat(RefEtatEnum.APPROUVEE);

		DemandeRecup d = new DemandeRecup();
		d.setIdAgent(9005138);
		d.setDateDebut(sdf.parse("15/06/2013"));
		d.setDateFin(null);
		d.setDuree(30);
		d.addEtatDemande(etat);
		absEntityManager.persist(d);

		EtatDemande etat2 = new EtatDemande();
		etat2.setIdAgent(9005138);
		etat2.setDate(new Date());
		etat2.setDateDebut(new Date());
		etat2.setDateFin(new Date());
		etat2.setMotif("motif");
		etat2.setEtat(RefEtatEnum.APPROUVEE);

		DemandeRecup d2 = new DemandeRecup();
		d2.setIdAgent(9005138);
		d2.setDateDebut(sdf.parse("15/05/2013"));
		d2.setDateFin(null);
		d2.setDuree(40);
		d2.addEtatDemande(etat2);
		absEntityManager.persist(d2);

		EtatDemande etatrp = new EtatDemande();
		etatrp.setIdAgent(9005138);
		etatrp.setDate(new Date());
		etatrp.setDateDebut(new Date());
		etatrp.setDateFin(new Date());
		etatrp.setMotif("motif");
		etatrp.setEtat(RefEtatEnum.APPROUVEE);

		DemandeReposComp drp = new DemandeReposComp();
		drp.setIdAgent(9005138);
		drp.setDateDebut(sdf.parse("02/05/2013"));
		drp.setDateFin(null);
		drp.setDuree(15);
		drp.setDureeAnneeN1(10);
		drp.addEtatDemande(etatrp);
		absEntityManager.persist(drp);

		EtatDemande etatrp2 = new EtatDemande();
		etatrp2.setIdAgent(9005138);
		etatrp2.setDate(new Date());
		etatrp2.setDateDebut(new Date());
		etatrp2.setDateFin(new Date());
		etatrp2.setMotif("motif");
		etatrp2.setEtat(RefEtatEnum.APPROUVEE);

		DemandeReposComp drp2 = new DemandeReposComp();
		drp2.setIdAgent(9005138);
		drp2.setDateDebut(sdf.parse("15/06/2013"));
		drp2.setDateFin(null);
		drp2.setDuree(20);
		drp2.setDureeAnneeN1(10);
		drp2.addEtatDemande(etatrp2);
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

		EtatDemande etat = new EtatDemande();
		etat.setIdAgent(9005138);
		etat.setDate(new Date());
		etat.setDateDebut(new Date());
		etat.setDateFin(new Date());
		etat.setMotif("motif");
		etat.setEtat(RefEtatEnum.APPROUVEE);

		DemandeRecup d = new DemandeRecup();
		d.setIdAgent(9005138);
		d.setDateDebut(sdf.parse("15/05/2013"));
		d.setDateFin(null);
		d.setDuree(30);
		d.addEtatDemande(etat);
		absEntityManager.persist(d);

		EtatDemande etat2 = new EtatDemande();
		etat2.setIdAgent(9005138);
		etat2.setDate(new Date());
		etat2.setDateDebut(new Date());
		etat2.setDateFin(new Date());
		etat2.setMotif("motif");
		etat2.setEtat(RefEtatEnum.APPROUVEE);

		DemandeRecup d2 = new DemandeRecup();
		d2.setIdAgent(9005138);
		d2.setDateDebut(sdf.parse("15/06/2013"));
		d2.setDateFin(null);
		d2.setDuree(40);
		d2.addEtatDemande(etat2);
		absEntityManager.persist(d2);

		EtatDemande etatdrp = new EtatDemande();
		etatdrp.setIdAgent(9005138);
		etatdrp.setDate(new Date());
		etatdrp.setDateDebut(new Date());
		etatdrp.setDateFin(new Date());
		etatdrp.setMotif("motif");
		etatdrp.setEtat(RefEtatEnum.APPROUVEE);

		DemandeReposComp drp = new DemandeReposComp();
		drp.setIdAgent(9005138);
		drp.setDateDebut(sdf.parse("15/05/2013"));
		drp.setDateFin(null);
		drp.setDuree(15);
		drp.setDureeAnneeN1(10);
		drp.addEtatDemande(etatdrp);
		absEntityManager.persist(drp);

		EtatDemande etatdrp2 = new EtatDemande();
		etatdrp2.setIdAgent(9005138);
		etatdrp2.setDate(new Date());
		etatdrp2.setDateDebut(new Date());
		etatdrp2.setDateFin(new Date());
		etatdrp2.setMotif("motif");
		etatdrp2.setEtat(RefEtatEnum.APPROUVEE);

		DemandeReposComp drp2 = new DemandeReposComp();
		drp2.setIdAgent(9005138);
		drp2.setDateDebut(sdf.parse("15/06/2013"));
		drp2.setDateFin(null);
		drp2.setDuree(20);
		drp2.setDureeAnneeN1(10);
		drp2.addEtatDemande(etatdrp2);
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

		EtatDemande etat = new EtatDemande();
		etat.setIdAgent(9005138);
		etat.setDate(new Date());
		etat.setDateDebut(new Date());
		etat.setDateFin(new Date());
		etat.setMotif("motif");
		etat.setEtat(RefEtatEnum.APPROUVEE);

		DemandeRecup d = new DemandeRecup();
		d.setIdAgent(9005138);
		d.setDateDebut(sdf.parse("15/05/2013"));
		d.setDateFin(null);
		d.setDuree(30);
		d.setType(typeMaladie);
		d.addEtatDemande(etat);
		absEntityManager.persist(d);

		EtatDemande etat2 = new EtatDemande();
		etat2.setIdAgent(9005138);
		etat2.setDate(new Date());
		etat2.setDateDebut(new Date());
		etat2.setDateFin(new Date());
		etat2.setMotif("motif");
		etat2.setEtat(RefEtatEnum.APPROUVEE);

		DemandeRecup dR2 = new DemandeRecup();
		dR2.setIdAgent(9005138);
		dR2.setDateDebut(sdf.parse("15/06/2013"));
		dR2.setDateFin(null);
		dR2.setDuree(40);
		dR2.setType(typeRecup);
		dR2.addEtatDemande(etat2);
		absEntityManager.persist(dR2);

		EtatDemande etat3 = new EtatDemande();
		etat3.setIdAgent(9005138);
		etat3.setDate(new Date());
		etat3.setDateDebut(new Date());
		etat3.setDateFin(new Date());
		etat3.setMotif("motif");
		etat3.setEtat(RefEtatEnum.APPROUVEE);

		DemandeReposComp drp = new DemandeReposComp();
		drp.setIdAgent(9005138);
		drp.setDateDebut(sdf.parse("15/05/2013"));
		drp.setDateFin(null);
		drp.setDuree(15);
		drp.setDureeAnneeN1(10);
		drp.setType(typeMaladie);
		drp.addEtatDemande(etat3);
		absEntityManager.persist(drp);

		EtatDemande etatdrp2 = new EtatDemande();
		etatdrp2.setIdAgent(9005138);
		etatdrp2.setDate(new Date());
		etatdrp2.setDateDebut(new Date());
		etatdrp2.setDateFin(new Date());
		etatdrp2.setMotif("motif");
		etatdrp2.setEtat(RefEtatEnum.APPROUVEE);

		DemandeReposComp drp2 = new DemandeReposComp();
		drp2.setIdAgent(9005138);
		drp2.setDateDebut(sdf.parse("15/06/2013"));
		drp2.setDateFin(null);
		drp2.setDuree(20);
		drp2.setDureeAnneeN1(10);
		drp2.setType(typeRecup);
		drp2.addEtatDemande(etatdrp2);
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

		EtatDemande etat = new EtatDemande();
		etat.setIdAgent(9005138);
		etat.setDate(new Date());
		etat.setDateDebut(new Date());
		etat.setDateFin(new Date());
		etat.setMotif("motif");
		etat.setEtat(RefEtatEnum.APPROUVEE);

		DemandeRecup d = new DemandeRecup();
		d.setIdAgent(9005130);
		d.setDateDebut(sdf.parse("15/05/2013"));
		d.setDateFin(null);
		d.setDuree(30);
		d.addEtatDemande(etat);
		absEntityManager.persist(d);

		EtatDemande etat2 = new EtatDemande();
		etat2.setIdAgent(9005138);
		etat2.setDate(new Date());
		etat2.setDateDebut(new Date());
		etat2.setDateFin(new Date());
		etat2.setMotif("motif");
		etat2.setEtat(RefEtatEnum.APPROUVEE);

		DemandeRecup d2 = new DemandeRecup();
		d2.setIdAgent(9005131);
		d2.setDateDebut(sdf.parse("17/06/2013"));
		d2.setDateFin(null);
		d2.setDuree(40);
		d2.addEtatDemande(etat2);
		absEntityManager.persist(d2);

		EtatDemande etat3 = new EtatDemande();
		etat3.setIdAgent(9005138);
		etat3.setDate(new Date());
		etat3.setDateDebut(new Date());
		etat3.setDateFin(new Date());
		etat3.setMotif("motif");
		etat3.setEtat(RefEtatEnum.APPROUVEE);

		DemandeRecup d3 = new DemandeRecup();
		d3.setIdAgent(9005132);
		d3.setDateDebut(sdf.parse("14/07/2013"));
		d3.setDateFin(null);
		d3.setDuree(50);
		d3.addEtatDemande(etat3);
		absEntityManager.persist(d3);

		EtatDemande etat4 = new EtatDemande();
		etat4.setIdAgent(9005138);
		etat4.setDate(new Date());
		etat4.setDateDebut(new Date());
		etat4.setDateFin(new Date());
		etat4.setMotif("motif");
		etat4.setEtat(RefEtatEnum.APPROUVEE);

		DemandeReposComp dpr = new DemandeReposComp();
		dpr.setIdAgent(9005130);
		dpr.setDateDebut(sdf.parse("16/05/2013"));
		dpr.setDateFin(null);
		dpr.setDuree(30);
		dpr.setDureeAnneeN1(10);
		dpr.addEtatDemande(etat4);
		absEntityManager.persist(dpr);

		EtatDemande etatdpr2 = new EtatDemande();
		etatdpr2.setIdAgent(9005138);
		etatdpr2.setDate(new Date());
		etatdpr2.setDateDebut(new Date());
		etatdpr2.setDateFin(new Date());
		etatdpr2.setMotif("motif");
		etatdpr2.setEtat(RefEtatEnum.APPROUVEE);

		DemandeReposComp dpr2 = new DemandeReposComp();
		dpr2.setIdAgent(9005131);
		dpr2.setDateDebut(sdf.parse("15/06/2013"));
		dpr2.setDateFin(null);
		dpr2.setDuree(40);
		dpr2.setDureeAnneeN1(10);
		dpr2.addEtatDemande(etatdpr2);
		absEntityManager.persist(dpr2);

		EtatDemande etatdpr3 = new EtatDemande();
		etatdpr3.setIdAgent(9005138);
		etatdpr3.setDate(new Date());
		etatdpr3.setDateDebut(new Date());
		etatdpr3.setDateFin(new Date());
		etatdpr3.setMotif("motif");
		etatdpr3.setEtat(RefEtatEnum.APPROUVEE);

		DemandeReposComp dpr3 = new DemandeReposComp();
		dpr3.setIdAgent(9005132);
		dpr3.setDateDebut(sdf.parse("15/07/2013"));
		dpr3.setDateFin(null);
		dpr3.setDuree(50);
		dpr3.setDureeAnneeN1(10);
		dpr3.addEtatDemande(etatdpr3);
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

		assertEquals(50, ((DemandeRecup) result.get(1)).getDuree().intValue());
		assertEquals(40, ((DemandeRecup) result.get(2)).getDuree().intValue());
		assertEquals(30, ((DemandeRecup) result.get(5)).getDuree().intValue());

		assertEquals(50, ((DemandeReposComp) result.get(0)).getDuree().intValue());
		assertEquals(40, ((DemandeReposComp) result.get(3)).getDuree().intValue());
		assertEquals(30, ((DemandeReposComp) result.get(4)).getDuree().intValue());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void listeDemandesAgent_MultiAgent_Return4DemandesOf6() throws ParseException {
		// Given

		EtatDemande etat = new EtatDemande();
		etat.setIdAgent(9005138);
		etat.setDate(new Date());
		etat.setDateDebut(new Date());
		etat.setDateFin(new Date());
		etat.setMotif("motif");
		etat.setEtat(RefEtatEnum.APPROUVEE);

		DemandeRecup d = new DemandeRecup();
		d.setIdAgent(9005130);
		d.setDateDebut(sdf.parse("15/05/2013"));
		d.setDateFin(null);
		d.setDuree(30);
		d.addEtatDemande(etat);
		absEntityManager.persist(d);

		EtatDemande etat2 = new EtatDemande();
		etat2.setIdAgent(9005138);
		etat2.setDate(new Date());
		etat2.setDateDebut(new Date());
		etat2.setDateFin(new Date());
		etat2.setMotif("motif");
		etat2.setEtat(RefEtatEnum.APPROUVEE);

		DemandeRecup d2 = new DemandeRecup();
		d2.setIdAgent(9005131);
		d2.setDateDebut(sdf.parse("15/06/2013"));
		d2.setDateFin(null);
		d2.setDuree(40);
		d2.addEtatDemande(etat2);
		absEntityManager.persist(d2);

		EtatDemande etat3 = new EtatDemande();
		etat3.setIdAgent(9005138);
		etat3.setDate(new Date());
		etat3.setDateDebut(new Date());
		etat3.setDateFin(new Date());
		etat3.setMotif("motif");
		etat3.setEtat(RefEtatEnum.APPROUVEE);

		DemandeRecup d3 = new DemandeRecup();
		d3.setIdAgent(9005132);
		d3.setDateDebut(sdf.parse("14/07/2013"));
		d3.setDateFin(null);
		d3.setDuree(50);
		d3.addEtatDemande(etat3);
		absEntityManager.persist(d3);

		EtatDemande etatdrp = new EtatDemande();
		etatdrp.setIdAgent(9005138);
		etatdrp.setDate(new Date());
		etatdrp.setDateDebut(new Date());
		etatdrp.setDateFin(new Date());
		etatdrp.setMotif("motif");
		etatdrp.setEtat(RefEtatEnum.APPROUVEE);

		DemandeReposComp drp = new DemandeReposComp();
		drp.setIdAgent(9005130);
		drp.setDateDebut(sdf.parse("14/05/2013"));
		drp.setDateFin(null);
		drp.setDuree(30);
		drp.setDureeAnneeN1(10);
		drp.addEtatDemande(etatdrp);
		absEntityManager.persist(drp);

		EtatDemande etatdrp2 = new EtatDemande();
		etatdrp2.setIdAgent(9005138);
		etatdrp2.setDate(new Date());
		etatdrp2.setDateDebut(new Date());
		etatdrp2.setDateFin(new Date());
		etatdrp2.setMotif("motif");
		etatdrp2.setEtat(RefEtatEnum.APPROUVEE);

		DemandeReposComp drp2 = new DemandeReposComp();
		drp2.setIdAgent(9005131);
		drp2.setDateDebut(sdf.parse("14/06/2013"));
		drp2.setDateFin(null);
		drp2.setDuree(40);
		drp2.setDureeAnneeN1(10);
		drp2.addEtatDemande(etatdrp2);
		absEntityManager.persist(drp2);

		EtatDemande etatdrp3 = new EtatDemande();
		etatdrp3.setIdAgent(9005138);
		etatdrp3.setDate(new Date());
		etatdrp3.setDateDebut(new Date());
		etatdrp3.setDateFin(new Date());
		etatdrp3.setMotif("motif");
		etatdrp3.setEtat(RefEtatEnum.APPROUVEE);

		DemandeReposComp drp3 = new DemandeReposComp();
		drp3.setIdAgent(9005132);
		drp3.setDateDebut(sdf.parse("15/07/2013"));
		drp3.setDateFin(null);
		drp3.setDuree(50);
		drp3.setDureeAnneeN1(10);
		drp3.addEtatDemande(etatdrp3);
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

		assertEquals(50, ((DemandeRecup) result.get(1)).getDuree().intValue());
		assertEquals(40, ((DemandeRecup) result.get(2)).getDuree().intValue());

		assertEquals(50, ((DemandeReposComp) result.get(0)).getDuree().intValue());
		assertEquals(40, ((DemandeReposComp) result.get(3)).getDuree().intValue());

		absEntityManager.flush();
		absEntityManager.clear();
	}

//	 @Test
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
		listeTypes.add(RefTypeGroupeAbsenceEnum.AS.getValue());
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

		EtatDemande etat = new EtatDemande();
		etat.setIdAgent(9005138);
		etat.setDate(new Date());
		etat.setDateDebut(new Date());
		etat.setDateFin(new Date());
		etat.setMotif("motif");
		etat.setEtat(RefEtatEnum.APPROUVEE);

		DemandeRecup dr = new DemandeRecup();
		dr.setIdAgent(9005138);
		dr.setDateDebut(new Date());
		dr.setDateFin(null);
		dr.setDuree(30);
		dr.addEtatDemande(etat);
		absEntityManager.persist(dr);

		EtatDemande etatdrc = new EtatDemande();
		etatdrc.setIdAgent(9005138);
		etatdrc.setDate(new Date());
		etatdrc.setDateDebut(new Date());
		etatdrc.setDateFin(new Date());
		etatdrc.setMotif("motif");
		etatdrc.setEtat(RefEtatEnum.APPROUVEE);

		DemandeReposComp drc = new DemandeReposComp();
		drc.setIdAgent(9005138);
		drc.setDateDebut(new Date());
		drc.setDateFin(null);
		drc.setDuree(15);
		drc.setDureeAnneeN1(10);
		drc.addEtatDemande(etatdrc);
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
	public void listeDemandesSIRH_Filter_2idAgents_Return2Demande() {
		// Given

		EtatDemande etat = new EtatDemande();
		etat.setIdAgent(9005138);
		etat.setDate(new Date());
		etat.setDateDebut(new Date());
		etat.setDateFin(new Date());
		etat.setMotif("motif");
		etat.setEtat(RefEtatEnum.APPROUVEE);

		DemandeRecup dr = new DemandeRecup();
		dr.setIdAgent(9005140);
		dr.setDateDebut(new DateTime(2013, 05, 02, 0, 0).toDate());
		dr.setDateFin(null);
		dr.setDuree(30);
		dr.addEtatDemande(etat);
		absEntityManager.persist(dr);

		EtatDemande etatca = new EtatDemande();
		etatca.setIdAgent(9005138);
		etatca.setDate(new Date());
		etatca.setDateDebut(new Date());
		etatca.setDateFin(new Date());
		etatca.setMotif("motif");
		etatca.setEtat(RefEtatEnum.APPROUVEE);

		DemandeCongesAnnuels ca = new DemandeCongesAnnuels();
		ca.setIdAgent(9005142);
		ca.setDateDebut(new DateTime(2013, 05, 03, 0, 0).toDate());
		ca.setDateFin(null);
		ca.setDuree(30.0);
		ca.addEtatDemande(etatca);
		absEntityManager.persist(ca);

		EtatDemande etatdrc = new EtatDemande();
		etatdrc.setIdAgent(9005138);
		etatdrc.setDate(new Date());
		etatdrc.setDateDebut(new Date());
		etatdrc.setDateFin(new Date());
		etatdrc.setMotif("motif");
		etatdrc.setEtat(RefEtatEnum.APPROUVEE);

		DemandeReposComp drc = new DemandeReposComp();
		drc.setIdAgent(9005138);
		drc.setDateDebut(new Date());
		drc.setDateFin(null);
		drc.setDuree(15);
		drc.setDureeAnneeN1(10);
		drc.addEtatDemande(etatdrc);
		absEntityManager.persist(drc);

		// When
		List<Demande> result = repository.listeDemandesSIRH(null, null, null, null, Arrays.asList(9005142, 9005138),
				null);

		// Then
		assertEquals(2, result.size());
		assertEquals("30.0", ((DemandeCongesAnnuels) result.get(1)).getDuree().toString());
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

		EtatDemande etat = new EtatDemande();
		etat.setIdAgent(9005138);
		etat.setDate(new Date());
		etat.setDateDebut(new Date());
		etat.setDateFin(new Date());
		etat.setMotif("motif");
		etat.setEtat(RefEtatEnum.APPROUVEE);

		DemandeRecup d = new DemandeRecup();
		d.setIdAgent(9005138);
		d.setDateDebut(sdf.parse("15/05/2013"));
		d.setDateFin(null);
		d.setDuree(30);
		d.setType(typeMaladie);
		d.addEtatDemande(etat);
		absEntityManager.persist(d);

		EtatDemande etatr2 = new EtatDemande();
		etatr2.setIdAgent(9005138);
		etatr2.setDate(new Date());
		etatr2.setDateDebut(new Date());
		etatr2.setDateFin(new Date());
		etatr2.setMotif("motif");
		etatr2.setEtat(RefEtatEnum.APPROUVEE);

		DemandeRecup dR2 = new DemandeRecup();
		dR2.setIdAgent(9005138);
		dR2.setDateDebut(sdf.parse("15/06/2013"));
		dR2.setDateFin(null);
		dR2.setDuree(40);
		dR2.setType(typeRecup);
		dR2.addEtatDemande(etatr2);
		absEntityManager.persist(dR2);

		EtatDemande etatdrp = new EtatDemande();
		etatdrp.setIdAgent(9005138);
		etatdrp.setDate(new Date());
		etatdrp.setDateDebut(new Date());
		etatdrp.setDateFin(new Date());
		etatdrp.setMotif("motif");
		etatdrp.setEtat(RefEtatEnum.APPROUVEE);

		DemandeReposComp drp = new DemandeReposComp();
		drp.setIdAgent(9005138);
		drp.setDateDebut(sdf.parse("15/05/2013"));
		drp.setDateFin(null);
		drp.setDuree(15);
		drp.setDureeAnneeN1(10);
		drp.setType(typeMaladie);
		drp.addEtatDemande(etatdrp);
		absEntityManager.persist(drp);

		EtatDemande etatdrp2 = new EtatDemande();
		etatdrp2.setIdAgent(9005138);
		etatdrp2.setDate(new Date());
		etatdrp2.setDateDebut(new Date());
		etatdrp2.setDateFin(new Date());
		etatdrp2.setMotif("motif");
		etatdrp2.setEtat(RefEtatEnum.APPROUVEE);

		DemandeReposComp drp2 = new DemandeReposComp();
		drp2.setIdAgent(9005138);
		drp2.setDateDebut(sdf.parse("15/06/2013"));
		drp2.setDateFin(null);
		drp2.setDuree(20);
		drp2.setDureeAnneeN1(10);
		drp2.setType(typeRecup);
		drp2.addEtatDemande(etatdrp2);
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

		EtatDemande etat = new EtatDemande();
		etat.setIdAgent(9005138);
		etat.setDate(new Date());
		etat.setDateDebut(new Date());
		etat.setDateFin(new Date());
		etat.setMotif("motif");
		etat.setEtat(RefEtatEnum.APPROUVEE);

		DemandeRecup d = new DemandeRecup();
		d.setIdAgent(9005138);
		d.setDateDebut(sdf.parse("15/06/2013"));
		d.setDateFin(null);
		d.setDuree(30);
		d.addEtatDemande(etat);
		absEntityManager.persist(d);

		EtatDemande etat2 = new EtatDemande();
		etat2.setIdAgent(9005138);
		etat2.setDate(new Date());
		etat2.setDateDebut(new Date());
		etat2.setDateFin(new Date());
		etat2.setMotif("motif");
		etat2.setEtat(RefEtatEnum.APPROUVEE);

		DemandeRecup d2 = new DemandeRecup();
		d2.setIdAgent(9005138);
		d2.setDateDebut(sdf.parse("15/05/2013"));
		d2.setDateFin(null);
		d2.setDuree(40);
		d2.addEtatDemande(etat2);
		absEntityManager.persist(d2);

		EtatDemande etatdrp = new EtatDemande();
		etatdrp.setIdAgent(9005138);
		etatdrp.setDate(new Date());
		etatdrp.setDateDebut(new Date());
		etatdrp.setDateFin(new Date());
		etatdrp.setMotif("motif");
		etatdrp.setEtat(RefEtatEnum.APPROUVEE);

		DemandeReposComp drp = new DemandeReposComp();
		drp.setIdAgent(9005138);
		drp.setDateDebut(sdf.parse("02/05/2013"));
		drp.setDateFin(null);
		drp.setDuree(15);
		drp.setDureeAnneeN1(10);
		drp.addEtatDemande(etatdrp);
		absEntityManager.persist(drp);

		EtatDemande etatdrp2 = new EtatDemande();
		etatdrp2.setIdAgent(9005138);
		etatdrp2.setDate(new Date());
		etatdrp2.setDateDebut(new Date());
		etatdrp2.setDateFin(new Date());
		etatdrp2.setMotif("motif");
		etatdrp2.setEtat(RefEtatEnum.APPROUVEE);

		DemandeReposComp drp2 = new DemandeReposComp();
		drp2.setIdAgent(9005138);
		drp2.setDateDebut(sdf.parse("16/06/2013"));
		drp2.setDateFin(null);
		drp2.setDuree(20);
		drp2.setDureeAnneeN1(10);
		drp2.addEtatDemande(etatdrp2);
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

		EtatDemande etat = new EtatDemande();
		etat.setIdAgent(9005138);
		etat.setDate(new Date());
		etat.setDateDebut(new Date());
		etat.setDateFin(new Date());
		etat.setMotif("motif");
		etat.setEtat(RefEtatEnum.APPROUVEE);

		DemandeRecup d = new DemandeRecup();
		d.setIdAgent(9005138);
		d.setDateDebut(sdf.parse("15/05/2013"));
		d.setDateFin(null);
		d.setDuree(30);
		d.addEtatDemande(etat);
		absEntityManager.persist(d);

		EtatDemande etat2 = new EtatDemande();
		etat2.setIdAgent(9005138);
		etat2.setDate(new Date());
		etat2.setDateDebut(new Date());
		etat2.setDateFin(new Date());
		etat2.setMotif("motif");
		etat2.setEtat(RefEtatEnum.APPROUVEE);

		DemandeRecup d2 = new DemandeRecup();
		d2.setIdAgent(9005138);
		d2.setDateDebut(sdf.parse("15/06/2013"));
		d2.setDateFin(null);
		d2.setDuree(40);
		d2.addEtatDemande(etat2);
		absEntityManager.persist(d2);

		EtatDemande etatdrp = new EtatDemande();
		etatdrp.setIdAgent(9005138);
		etatdrp.setDate(new Date());
		etatdrp.setDateDebut(new Date());
		etatdrp.setDateFin(new Date());
		etatdrp.setMotif("motif");
		etatdrp.setEtat(RefEtatEnum.APPROUVEE);

		DemandeReposComp drp = new DemandeReposComp();
		drp.setIdAgent(9005138);
		drp.setDateDebut(sdf.parse("15/05/2013"));
		drp.setDateFin(null);
		drp.setDuree(15);
		drp.setDureeAnneeN1(10);
		drp.addEtatDemande(etatdrp);
		absEntityManager.persist(drp);

		EtatDemande etatdrp2 = new EtatDemande();
		etatdrp2.setIdAgent(9005138);
		etatdrp2.setDate(new Date());
		etatdrp2.setDateDebut(new Date());
		etatdrp2.setDateFin(new Date());
		etatdrp2.setMotif("motif");
		etatdrp2.setEtat(RefEtatEnum.APPROUVEE);

		DemandeReposComp drp2 = new DemandeReposComp();
		drp2.setIdAgent(9005138);
		drp2.setDateDebut(sdf.parse("15/06/2013"));
		drp2.setDateFin(null);
		drp2.setDuree(20);
		drp2.setDureeAnneeN1(10);
		drp2.addEtatDemande(etatdrp2);
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

		EtatDemande etat = new EtatDemande();
		etat.setIdAgent(9005138);
		etat.setDate(new Date());
		etat.setDateDebut(new Date());
		etat.setDateFin(new Date());
		etat.setMotif("motif");
		etat.setEtat(RefEtatEnum.APPROUVEE);

		DemandeAsa d = new DemandeAsa();
		d.setIdAgent(9005138);
		d.setDateDebut(sdf.parse("15/04/2013"));
		d.setDateFin(null);
		d.setDuree(10.0);
		d.setType(typeAsaA48);
		d.addEtatDemande(etat);
		absEntityManager.persist(d);

		EtatDemande etat2 = new EtatDemande();
		etat2.setIdAgent(9005138);
		etat2.setDate(new Date());
		etat2.setDateDebut(new Date());
		etat2.setDateFin(new Date());
		etat2.setMotif("motif");
		etat2.setEtat(RefEtatEnum.APPROUVEE);

		DemandeAsa d2 = new DemandeAsa();
		d2.setIdAgent(9005138);
		d2.setDateDebut(sdf.parse("15/05/2013"));
		d2.setDateFin(null);
		d2.setDuree(20.0);
		d2.setType(typeAsaA48);
		d2.addEtatDemande(etat2);
		absEntityManager.persist(d2);

		EtatDemande etatdrp = new EtatDemande();
		etatdrp.setIdAgent(9005138);
		etatdrp.setDate(new Date());
		etatdrp.setDateDebut(new Date());
		etatdrp.setDateFin(new Date());
		etatdrp.setMotif("motif");
		etatdrp.setEtat(RefEtatEnum.APPROUVEE);

		DemandeAsa drp = new DemandeAsa();
		drp.setIdAgent(9005138);
		drp.setDateDebut(sdf.parse("15/06/2013"));
		drp.setDateFin(null);
		drp.setDuree(30.0);
		drp.setType(typeAsaA49);
		drp.addEtatDemande(etatdrp);
		absEntityManager.persist(drp);

		EtatDemande etatdrp2 = new EtatDemande();
		etatdrp2.setIdAgent(9005138);
		etatdrp2.setDate(new Date());
		etatdrp2.setDateDebut(new Date());
		etatdrp2.setDateFin(new Date());
		etatdrp2.setMotif("motif");
		etatdrp2.setEtat(RefEtatEnum.APPROUVEE);

		DemandeAsa drp2 = new DemandeAsa();
		drp2.setIdAgent(9005138);
		drp2.setDateDebut(sdf.parse("15/07/2013"));
		drp2.setDateFin(null);
		drp2.setDuree(40.0);
		drp2.setType(typeAsaA49);
		drp2.addEtatDemande(etatdrp2);
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

		EtatDemande etat = new EtatDemande();
		etat.setIdAgent(9005138);
		etat.setDate(new Date());
		etat.setDateDebut(new Date());
		etat.setDateFin(new Date());
		etat.setMotif("motif");
		etat.setEtat(RefEtatEnum.APPROUVEE);

		DemandeRecup d = new DemandeRecup();
		d.setIdAgent(9005138);
		d.setDateDebut(sdf.parse("15/05/2013"));
		d.setDateFin(null);
		d.setDuree(30);
		d.setType(typeRecup);
		d.addEtatDemande(etat);
		absEntityManager.persist(d);

		EtatDemande etat2 = new EtatDemande();
		etat2.setIdAgent(9005138);
		etat2.setDate(new Date());
		etat2.setDateDebut(new Date());
		etat2.setDateFin(new Date());
		etat2.setMotif("motif");
		etat2.setEtat(RefEtatEnum.APPROUVEE);

		DemandeRecup dR2 = new DemandeRecup();
		dR2.setIdAgent(9005138);
		dR2.setDateDebut(sdf.parse("15/06/2013"));
		dR2.setDateFin(null);
		dR2.setDuree(40);
		dR2.setType(typeRecup);
		dR2.addEtatDemande(etat2);
		absEntityManager.persist(dR2);

		EtatDemande etatdrp = new EtatDemande();
		etatdrp.setIdAgent(9005138);
		etatdrp.setDate(new Date());
		etatdrp.setDateDebut(new Date());
		etatdrp.setDateFin(new Date());
		etatdrp.setMotif("motif");
		etatdrp.setEtat(RefEtatEnum.APPROUVEE);

		DemandeReposComp drp = new DemandeReposComp();
		drp.setIdAgent(9005138);
		drp.setDateDebut(sdf.parse("15/05/2013"));
		drp.setDateFin(null);
		drp.setDuree(15);
		drp.setDureeAnneeN1(10);
		drp.setType(typeReposComp);
		drp.addEtatDemande(etatdrp);
		absEntityManager.persist(drp);

		EtatDemande etatdrp2 = new EtatDemande();
		etatdrp2.setIdAgent(9005138);
		etatdrp2.setDate(new Date());
		etatdrp2.setDateDebut(new Date());
		etatdrp2.setDateFin(new Date());
		etatdrp2.setMotif("motif");
		etatdrp2.setEtat(RefEtatEnum.APPROUVEE);

		DemandeReposComp drp2 = new DemandeReposComp();
		drp2.setIdAgent(9005138);
		drp2.setDateDebut(sdf.parse("15/06/2013"));
		drp2.setDateFin(null);
		drp2.setDuree(20);
		drp2.setDureeAnneeN1(10);
		drp2.setType(typeReposComp);
		drp2.addEtatDemande(etatdrp2);
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
	public void listeDemandesASAAndCongesExcepSIRHAValider_Return0Demande() throws ParseException {
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
		List<Demande> result = repository.listeDemandesASAAndCongesExcepAndMaladiesSIRHAValider(null, null, null, null, null);

		// Then
		assertEquals(0, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void listeDemandesCongesAnnuelsSIRHAValider_Return0Demande() throws ParseException {
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
		List<Demande> result = repository.listeDemandesCongesAnnuelsSIRHAValider(null, null, null);

		// Then
		assertEquals(0, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void listeDemandesASAAndCongesExcepSIRHAValider_Return1Demandes() throws ParseException {
		// Given
		RefGroupeAbsence groupeRecup = new RefGroupeAbsence();
		groupeRecup.setIdRefGroupeAbsence(1);
		absEntityManager.persist(groupeRecup);

		RefGroupeAbsence groupeCongeAnnuel = new RefGroupeAbsence();
		groupeCongeAnnuel.setIdRefGroupeAbsence(5);
		absEntityManager.persist(groupeCongeAnnuel);

		RefGroupeAbsence groupeAsa = new RefGroupeAbsence();
		groupeAsa.setIdRefGroupeAbsence(3);
		absEntityManager.persist(groupeAsa);

		RefTypeAbsence typeRecup = new RefTypeAbsence();
		typeRecup.setGroupe(groupeRecup);
		typeRecup.setLabel("RECUP");
		absEntityManager.persist(typeRecup);

		RefTypeAbsence typeCongeAnnuel = new RefTypeAbsence();
		typeCongeAnnuel.setGroupe(groupeCongeAnnuel);
		typeCongeAnnuel.setLabel("CONGES");
		absEntityManager.persist(typeCongeAnnuel);

		RefTypeAbsence typeAsa = new RefTypeAbsence();
		typeAsa.setGroupe(groupeAsa);
		typeAsa.setLabel("AS");
		absEntityManager.persist(typeAsa);

		DemandeCongesAnnuels dConge = new DemandeCongesAnnuels();
		dConge.setIdAgent(9005131);
		dConge.setDateDebut(sdf.parse("15/05/2013"));
		dConge.setDateFin(null);
		dConge.setType(typeCongeAnnuel);
		absEntityManager.persist(dConge);

		EtatDemande etatCongeDemandeAppr = new EtatDemande();
		etatCongeDemandeAppr.setDemande(dConge);
		etatCongeDemandeAppr.setIdAgent(9005138);
		etatCongeDemandeAppr.setEtat(RefEtatEnum.APPROUVEE);
		absEntityManager.persist(etatCongeDemandeAppr);

		DemandeAsa d = new DemandeAsa();
		d.setIdAgent(9005138);
		d.setDateDebut(sdf.parse("16/05/2013"));
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
		dR2.setDateDebut(sdf.parse("17/06/2013"));
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
		drp.setDateDebut(sdf.parse("18/05/2013"));
		drp.setDateFin(null);
		drp.setType(typeRecup);
		absEntityManager.persist(drp);

		EtatDemande etatDemandeAttente = new EtatDemande();
		etatDemandeAttente.setDemande(drp);
		etatDemandeAttente.setIdAgent(9005138);
		etatDemandeAttente.setEtat(RefEtatEnum.EN_ATTENTE);
		absEntityManager.persist(etatDemandeAttente);

		List<Integer> listGroupe = new ArrayList<Integer>();
		listGroupe.add(RefTypeGroupeAbsenceEnum.AS.getValue());
		listGroupe.add(RefTypeGroupeAbsenceEnum.CONGES_EXCEP.getValue());

		// When
		List<Demande> result = repository.listeDemandesASAAndCongesExcepAndMaladiesSIRHAValider(null, null, listGroupe, null, null);

		// Then
		assertEquals(1, result.size());
		assertEquals("9005138", ((DemandeAsa) result.get(0)).getIdAgent().toString());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void listeDemandesCongesAnnuelsSIRHAValider_Return1Demandes() throws ParseException {
		// Given
		RefGroupeAbsence groupeRecup = new RefGroupeAbsence();
		groupeRecup.setIdRefGroupeAbsence(1);
		absEntityManager.persist(groupeRecup);

		RefGroupeAbsence groupeCongeAnnuel = new RefGroupeAbsence();
		groupeCongeAnnuel.setIdRefGroupeAbsence(5);
		absEntityManager.persist(groupeCongeAnnuel);

		RefGroupeAbsence groupeAsa = new RefGroupeAbsence();
		groupeAsa.setIdRefGroupeAbsence(3);
		absEntityManager.persist(groupeAsa);

		RefTypeAbsence typeRecup = new RefTypeAbsence();
		typeRecup.setGroupe(groupeRecup);
		typeRecup.setLabel("RECUP");
		absEntityManager.persist(typeRecup);

		RefTypeAbsence typeCongeAnnuel = new RefTypeAbsence();
		typeCongeAnnuel.setGroupe(groupeCongeAnnuel);
		typeCongeAnnuel.setLabel("CONGES");
		absEntityManager.persist(typeCongeAnnuel);

		RefTypeAbsence typeAsa = new RefTypeAbsence();
		typeAsa.setGroupe(groupeAsa);
		typeAsa.setLabel("AS");
		absEntityManager.persist(typeAsa);

		DemandeCongesAnnuels dConge = new DemandeCongesAnnuels();
		dConge.setIdAgent(9005131);
		dConge.setDateDebut(sdf.parse("15/05/2013"));
		dConge.setDateFin(null);
		dConge.setType(typeCongeAnnuel);
		absEntityManager.persist(dConge);

		EtatDemande etatCongeDemandeAppr = new EtatDemande();
		etatCongeDemandeAppr.setDemande(dConge);
		etatCongeDemandeAppr.setIdAgent(9005138);
		etatCongeDemandeAppr.setEtat(RefEtatEnum.A_VALIDER);
		absEntityManager.persist(etatCongeDemandeAppr);

		DemandeAsa d = new DemandeAsa();
		d.setIdAgent(9005138);
		d.setDateDebut(sdf.parse("16/05/2013"));
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
		dR2.setDateDebut(sdf.parse("17/06/2013"));
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
		drp.setDateDebut(sdf.parse("18/05/2013"));
		drp.setDateFin(null);
		drp.setType(typeRecup);
		absEntityManager.persist(drp);

		EtatDemande etatDemandeAttente = new EtatDemande();
		etatDemandeAttente.setDemande(drp);
		etatDemandeAttente.setIdAgent(9005138);
		etatDemandeAttente.setEtat(RefEtatEnum.EN_ATTENTE);
		absEntityManager.persist(etatDemandeAttente);

		// When
		List<Demande> result = repository.listeDemandesCongesAnnuelsSIRHAValider(null, null, null);

		// Then
		assertEquals(1, result.size());
		assertEquals("9005131", ((DemandeCongesAnnuels) result.get(0)).getIdAgent().toString());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void listeDemandesASAAndCongesExcepSIRHAValider_withListIdsAgent() throws ParseException {
		// Given
		RefGroupeAbsence groupeRecup = new RefGroupeAbsence();
		groupeRecup.setIdRefGroupeAbsence(1);
		absEntityManager.persist(groupeRecup);

		RefGroupeAbsence groupeCongeAnnuel = new RefGroupeAbsence();
		groupeCongeAnnuel.setIdRefGroupeAbsence(5);
		absEntityManager.persist(groupeCongeAnnuel);

		RefGroupeAbsence groupeAsa = new RefGroupeAbsence();
		groupeAsa.setIdRefGroupeAbsence(3);
		absEntityManager.persist(groupeAsa);

		RefTypeAbsence typeRecup = new RefTypeAbsence();
		typeRecup.setGroupe(groupeRecup);
		typeRecup.setLabel("RECUP");
		absEntityManager.persist(typeRecup);

		RefTypeAbsence typeCongeAnnuel = new RefTypeAbsence();
		typeCongeAnnuel.setGroupe(groupeCongeAnnuel);
		typeCongeAnnuel.setLabel("CONGES");
		absEntityManager.persist(typeCongeAnnuel);

		RefTypeAbsence typeAsa = new RefTypeAbsence();
		typeAsa.setGroupe(groupeAsa);
		typeAsa.setLabel("AS");
		absEntityManager.persist(typeAsa);

		// 1er agent
		DemandeCongesAnnuels dConge = new DemandeCongesAnnuels();
		dConge.setIdAgent(9005131);
		dConge.setDateDebut(sdf.parse("15/05/2013"));
		dConge.setDateFin(null);
		dConge.setType(typeCongeAnnuel);
		absEntityManager.persist(dConge);

		EtatDemande etatCongeDemandeAppr = new EtatDemande();
		etatCongeDemandeAppr.setDemande(dConge);
		etatCongeDemandeAppr.setIdAgent(9005138);
		etatCongeDemandeAppr.setEtat(RefEtatEnum.APPROUVEE);
		absEntityManager.persist(etatCongeDemandeAppr);

		DemandeCongesAnnuels dConge2 = new DemandeCongesAnnuels();
		dConge2.setIdAgent(9005131);
		dConge2.setDateDebut(sdf.parse("15/05/2013"));
		dConge2.setDateFin(null);
		dConge2.setType(typeCongeAnnuel);
		absEntityManager.persist(dConge2);

		EtatDemande etatCongeDemandeAppr2 = new EtatDemande();
		etatCongeDemandeAppr2.setDemande(dConge2);
		etatCongeDemandeAppr2.setIdAgent(9005138);
		etatCongeDemandeAppr2.setEtat(RefEtatEnum.APPROUVEE);
		absEntityManager.persist(etatCongeDemandeAppr2);

		DemandeAsa d = new DemandeAsa();
		d.setIdAgent(9005138);
		d.setDateDebut(sdf.parse("16/05/2013"));
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
		dR2.setDateDebut(sdf.parse("17/06/2013"));
		dR2.setDateFin(null);
		dR2.setType(typeAsa);
		absEntityManager.persist(dR2);

		EtatDemande etatDemandedR2 = new EtatDemande();
		etatDemandedR2.setDemande(dR2);
		etatDemandedR2.setIdAgent(9005138);
		etatDemandedR2.setEtat(RefEtatEnum.APPROUVEE);
		absEntityManager.persist(etatDemandedR2);

		List<Integer> listGroupe = new ArrayList<Integer>();
		listGroupe.add(RefTypeGroupeAbsenceEnum.AS.getValue());
		listGroupe.add(RefTypeGroupeAbsenceEnum.CONGES_EXCEP.getValue());

		// When
		List<Demande> result = repository.listeDemandesASAAndCongesExcepAndMaladiesSIRHAValider(null, null, listGroupe, null,
				Arrays.asList(9005138, 9005139, 9005131));

		// Then
		assertEquals(2, result.size());
		assertEquals("9005139", ((DemandeAsa) result.get(0)).getIdAgent().toString());
		assertEquals("9005138", ((DemandeAsa) result.get(1)).getIdAgent().toString());

		// When
		List<Demande> result2 = repository.listeDemandesASAAndCongesExcepAndMaladiesSIRHAValider(null, null, listGroupe, null,
				Arrays.asList(9005139, 9005131));

		// Then
		assertEquals(1, result2.size());
		assertEquals("9005139", ((DemandeAsa) result2.get(0)).getIdAgent().toString());

		// When
		List<Demande> result3 = repository.listeDemandesASAAndCongesExcepAndMaladiesSIRHAValider(null, null, listGroupe, null,
				Arrays.asList(9005138, 9005139));

		// Then
		assertEquals(2, result3.size());
		assertEquals("9005139", ((DemandeAsa) result3.get(0)).getIdAgent().toString());
		assertEquals("9005138", ((DemandeAsa) result3.get(1)).getIdAgent().toString());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void listeDemandesCongesAnnuelsSIRHAValider_withListIdsAgent() throws ParseException {
		// Given
		RefGroupeAbsence groupeRecup = new RefGroupeAbsence();
		groupeRecup.setIdRefGroupeAbsence(1);
		absEntityManager.persist(groupeRecup);

		RefGroupeAbsence groupeCongeAnnuel = new RefGroupeAbsence();
		groupeCongeAnnuel.setIdRefGroupeAbsence(5);
		absEntityManager.persist(groupeCongeAnnuel);

		RefGroupeAbsence groupeAsa = new RefGroupeAbsence();
		groupeAsa.setIdRefGroupeAbsence(3);
		absEntityManager.persist(groupeAsa);

		RefTypeAbsence typeRecup = new RefTypeAbsence();
		typeRecup.setGroupe(groupeRecup);
		typeRecup.setLabel("RECUP");
		absEntityManager.persist(typeRecup);

		RefTypeAbsence typeCongeAnnuel = new RefTypeAbsence();
		typeCongeAnnuel.setGroupe(groupeCongeAnnuel);
		typeCongeAnnuel.setLabel("CONGES");
		absEntityManager.persist(typeCongeAnnuel);

		RefTypeAbsence typeAsa = new RefTypeAbsence();
		typeAsa.setGroupe(groupeAsa);
		typeAsa.setLabel("AS");
		absEntityManager.persist(typeAsa);

		// 1er agent
		DemandeCongesAnnuels dConge = new DemandeCongesAnnuels();
		dConge.setIdAgent(9005131);
		dConge.setDateDebut(sdf.parse("15/05/2013"));
		dConge.setDateFin(null);
		dConge.setType(typeCongeAnnuel);
		absEntityManager.persist(dConge);

		EtatDemande etatCongeDemandeAppr = new EtatDemande();
		etatCongeDemandeAppr.setDemande(dConge);
		etatCongeDemandeAppr.setIdAgent(9005138);
		etatCongeDemandeAppr.setEtat(RefEtatEnum.APPROUVEE); // ne sera pas
																// retourne
		absEntityManager.persist(etatCongeDemandeAppr);

		DemandeCongesAnnuels dConge2 = new DemandeCongesAnnuels();
		dConge2.setIdAgent(9005131);
		dConge2.setDateDebut(sdf.parse("15/05/2013"));
		dConge2.setDateFin(null);
		dConge2.setType(typeCongeAnnuel);
		absEntityManager.persist(dConge2);

		EtatDemande etatCongeDemandeAppr2 = new EtatDemande();
		etatCongeDemandeAppr2.setDemande(dConge2);
		etatCongeDemandeAppr2.setIdAgent(9005138);
		etatCongeDemandeAppr2.setEtat(RefEtatEnum.A_VALIDER);
		absEntityManager.persist(etatCongeDemandeAppr2);

		DemandeAsa d = new DemandeAsa();
		d.setIdAgent(9005138);
		d.setDateDebut(sdf.parse("16/05/2013"));
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
		dR2.setDateDebut(sdf.parse("17/06/2013"));
		dR2.setDateFin(null);
		dR2.setType(typeAsa);
		absEntityManager.persist(dR2);

		EtatDemande etatDemandedR2 = new EtatDemande();
		etatDemandedR2.setDemande(dR2);
		etatDemandedR2.setIdAgent(9005138);
		etatDemandedR2.setEtat(RefEtatEnum.APPROUVEE);
		absEntityManager.persist(etatDemandedR2);

		// When
		List<Demande> result = repository.listeDemandesCongesAnnuelsSIRHAValider(null, null,
				Arrays.asList(9005138, 9005139, 9005131));

		// Then
		assertEquals(1, result.size());
		assertEquals("9005131", ((DemandeCongesAnnuels) result.get(0)).getIdAgent().toString());

		// When
		List<Demande> result2 = repository.listeDemandesCongesAnnuelsSIRHAValider(null, null,
				Arrays.asList(9005139, 9005131));

		// Then
		assertEquals(1, result2.size());
		assertEquals("9005131", ((DemandeCongesAnnuels) result2.get(0)).getIdAgent().toString());

		// When
		List<Demande> result3 = repository.listeDemandesCongesAnnuelsSIRHAValider(null, null,
				Arrays.asList(9005138, 9005139));

		// Then
		assertEquals(0, result3.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getNombreSamediOffertSurAnnee_Return0() throws ParseException {
		// Given
		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());
		absEntityManager.persist(groupe);

		RefTypeAbsence type = new RefTypeAbsence();
		type.setGroupe(groupe);
		absEntityManager.persist(type);

		DemandeCongesAnnuels d = new DemandeCongesAnnuels();
		d.setIdAgent(9005138);
		d.setType(type);
		d.setDateDebut(sdf.parse("15/05/2013"));
		d.setDateFin(sdf.parse("16/05/2013"));
		d.setNbSamediOffert(0.0);
		absEntityManager.persist(d);

		// When
		Integer result = repository.getNombreSamediOffertSurAnnee(d.getIdAgent(), 2013, null);

		// Then
		assertEquals(0, (int) result);

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getNombreSamediOffertSurAnnee_Return0BecauseEtatDemandeRefusee() throws ParseException {
		// Given
		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());
		absEntityManager.persist(groupe);

		RefTypeAbsence type = new RefTypeAbsence();
		type.setGroupe(groupe);
		absEntityManager.persist(type);

		EtatDemandeCongesAnnuels etatDemande = new EtatDemandeCongesAnnuels();
		etatDemande.setEtat(RefEtatEnum.REFUSEE);

		DemandeCongesAnnuels d = new DemandeCongesAnnuels();
		d.setIdAgent(9005138);
		d.setType(type);
		d.setDateDebut(sdf.parse("15/05/2013"));
		d.setDateFin(sdf.parse("16/05/2013"));
		d.setNbSamediOffert(1.0);
		d.addEtatDemande(etatDemande);
		absEntityManager.persist(d);
		absEntityManager.persist(etatDemande);

		// When
		Integer result = repository.getNombreSamediOffertSurAnnee(d.getIdAgent(), 2013, d.getIdDemande() + 1);

		// Then
		assertEquals(0, (int) result);

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getNombreSamediOffertSurAnnee_Return0BecauseEtatDemandeAnnulee() throws ParseException {
		// Given
		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());
		absEntityManager.persist(groupe);

		RefTypeAbsence type = new RefTypeAbsence();
		type.setGroupe(groupe);
		absEntityManager.persist(type);

		EtatDemandeCongesAnnuels etatDemande = new EtatDemandeCongesAnnuels();
		etatDemande.setEtat(RefEtatEnum.ANNULEE);

		DemandeCongesAnnuels d = new DemandeCongesAnnuels();
		d.setIdAgent(9005138);
		d.setType(type);
		d.setDateDebut(sdf.parse("15/05/2013"));
		d.setDateFin(sdf.parse("16/05/2013"));
		d.setNbSamediOffert(1.0);
		d.addEtatDemande(etatDemande);
		absEntityManager.persist(d);
		absEntityManager.persist(etatDemande);

		// When
		Integer result = repository.getNombreSamediOffertSurAnnee(d.getIdAgent(), 2013, d.getIdDemande() + 1);

		// Then
		assertEquals(0, (int) result);

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getNombreSamediOffertSurAnnee_Return0BecauseEtatDemandeRejetee() throws ParseException {
		// Given
		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());
		absEntityManager.persist(groupe);

		RefTypeAbsence type = new RefTypeAbsence();
		type.setGroupe(groupe);
		absEntityManager.persist(type);

		EtatDemandeCongesAnnuels etatDemande = new EtatDemandeCongesAnnuels();
		etatDemande.setEtat(RefEtatEnum.REJETE);

		DemandeCongesAnnuels d = new DemandeCongesAnnuels();
		d.setIdAgent(9005138);
		d.setType(type);
		d.setDateDebut(sdf.parse("15/05/2013"));
		d.setDateFin(sdf.parse("16/05/2013"));
		d.setNbSamediOffert(1.0);
		d.addEtatDemande(etatDemande);
		absEntityManager.persist(d);
		absEntityManager.persist(etatDemande);

		// When
		Integer result = repository.getNombreSamediOffertSurAnnee(d.getIdAgent(), 2013, d.getIdDemande() + 1);

		// Then
		assertEquals(0, (int) result);

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getNombreSamediOffertSurAnnee_Return0_sameDemande() throws ParseException {
		// Given
		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());
		absEntityManager.persist(groupe);

		RefTypeAbsence type = new RefTypeAbsence();
		type.setGroupe(groupe);
		absEntityManager.persist(type);

		EtatDemandeCongesAnnuels etatDemande = new EtatDemandeCongesAnnuels();
		etatDemande.setEtat(RefEtatEnum.APPROUVEE);

		DemandeCongesAnnuels d = new DemandeCongesAnnuels();
		d.setIdAgent(9005138);
		d.setType(type);
		d.setDateDebut(sdf.parse("15/05/2013"));
		d.setDateFin(sdf.parse("16/05/2013"));
		d.setNbSamediOffert(1.0);
		d.addEtatDemande(etatDemande);
		absEntityManager.persist(d);
		absEntityManager.persist(etatDemande);

		// When
		Integer result = repository.getNombreSamediOffertSurAnnee(d.getIdAgent(), 2013, d.getIdDemande());

		// Then
		assertEquals(0, (int) result);

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getNombreSamediOffertSurAnnee_Return1() throws ParseException {
		// Given
		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());
		absEntityManager.persist(groupe);

		RefTypeAbsence type = new RefTypeAbsence();
		type.setGroupe(groupe);
		absEntityManager.persist(type);

		EtatDemandeCongesAnnuels etatDemande = new EtatDemandeCongesAnnuels();
		etatDemande.setEtat(RefEtatEnum.APPROUVEE);

		DemandeCongesAnnuels d = new DemandeCongesAnnuels();
		d.setIdAgent(9005138);
		d.setType(type);
		d.setDateDebut(sdf.parse("15/05/2013"));
		d.setDateFin(sdf.parse("16/05/2013"));
		d.setNbSamediOffert(1.0);
		d.addEtatDemande(etatDemande);
		absEntityManager.persist(d);
		absEntityManager.persist(etatDemande);

		// When
		Integer result = repository.getNombreSamediOffertSurAnnee(d.getIdAgent(), 2013, d.getIdDemande() + 1);

		// Then
		assertEquals(1, (int) result);

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getNombreSamediOffertSurAnnee_badDate() throws ParseException {
		// Given
		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());
		absEntityManager.persist(groupe);

		RefTypeAbsence type = new RefTypeAbsence();
		type.setGroupe(groupe);
		absEntityManager.persist(type);

		DemandeCongesAnnuels d = new DemandeCongesAnnuels();
		d.setIdAgent(9005138);
		d.setType(type);
		d.setDateDebut(sdf.parse("15/05/2013"));
		d.setDateFin(sdf.parse("16/05/2013"));
		d.setNbSamediOffert(1.0);
		absEntityManager.persist(d);

		// When
		Integer result = repository.getNombreSamediOffertSurAnnee(d.getIdAgent(), 2012, null);

		// Then
		assertEquals(0, (int) result);

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void listeDemandesAgentVerification_Return0_badDate() throws ParseException {
		// Given
		Integer idAgent = 9005138;
		Date fromDate = new DateTime(2014, 1, 1, 0, 0).toDate();
		Date toDate = new DateTime(2014, 1, 2, 0, 0).toDate();
		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());
		absEntityManager.persist(groupe);

		RefTypeAbsence type = new RefTypeAbsence();
		type.setGroupe(groupe);
		absEntityManager.persist(type);

		EtatDemande etat = new EtatDemande();
		etat.setDate(new Date());
		etat.setEtat(RefEtatEnum.APPROUVEE);

		Demande d = new Demande();
		d.setIdAgent(idAgent);
		d.setType(type);
		d.setDateDebut(sdf.parse("15/05/2013"));
		d.setDateFin(sdf.parse("16/05/2013"));
		absEntityManager.persist(d);
		etat.setDemande(d);
		absEntityManager.persist(etat);

		// When
		List<Demande> result = repository.listeDemandesAgentVerification(idAgent, fromDate, toDate,
				RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());

		// Then
		assertEquals(0, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void listeDemandesAgentVerification_Return1() throws ParseException {
		// Given
		Integer idAgent = 9005138;
		Date fromDate = new DateTime(2014, 5, 15, 2, 0).toDate();
		Date toDate = new DateTime(2014, 5, 15, 4, 0).toDate();

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());
		absEntityManager.persist(groupe);

		RefTypeAbsence type = new RefTypeAbsence();
		type.setGroupe(groupe);
		absEntityManager.persist(type);

		DemandeCongesAnnuels d = new DemandeCongesAnnuels();
		d.setIdAgent(idAgent);
		d.setType(type);
		d.setDateDebut(new DateTime(2014, 5, 15, 0, 0).toDate());
		d.setDateFin(new DateTime(2014, 5, 16, 0, 0).toDate());
		d.setNbSamediOffert(1.0);
		absEntityManager.persist(d);

		for (int i = 0; i < 12; i++) {

			EtatDemande etat = new EtatDemande();
			etat.setDate(new Date());
			etat.setEtat(RefEtatEnum.getRefEtatEnum(i));
			etat.setDemande(d);
			d.getEtatsDemande().add(etat);
			absEntityManager.persist(d);
			absEntityManager.persist(etat);

			// When
			List<Demande> result = repository.listeDemandesAgentVerification(idAgent, fromDate, toDate,
					RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());

			// Then
			if (i == RefEtatEnum.SAISIE.getCodeEtat() ||
					i == RefEtatEnum.VISEE_FAVORABLE.getCodeEtat() || i == RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat()
					|| i == RefEtatEnum.APPROUVEE.getCodeEtat() || i == RefEtatEnum.A_VALIDER.getCodeEtat()
					|| i == RefEtatEnum.EN_ATTENTE.getCodeEtat() || i == RefEtatEnum.PRISE.getCodeEtat()
					|| i == RefEtatEnum.VALIDEE.getCodeEtat()) {
				assertEquals(1, result.size());
			} else {
				assertEquals(0, result.size());
			}
		}

		absEntityManager.flush();
		absEntityManager.clear();
	}
	
	@Test
	@Transactional("absTransactionManager")
	public void countListeDemandesForListAgent_DateFilter_Return2Demande() throws ParseException {
		// Given
		EtatDemande etat = new EtatDemande();
		etat.setIdAgent(9005138);
		etat.setDate(new Date());
		etat.setDateDebut(new Date());
		etat.setDateFin(new Date());
		etat.setMotif("motif");
		etat.setEtat(RefEtatEnum.APPROUVEE);

		DemandeRecup d = new DemandeRecup();
		d.setIdAgent(9005138);
		d.setDateDebut(sdf.parse("15/06/2013"));
		d.setDateFin(null);
		d.setDuree(30);
		d.addEtatDemande(etat);
		absEntityManager.persist(d);

		EtatDemande etat2 = new EtatDemande();
		etat2.setIdAgent(9005138);
		etat2.setDate(new Date());
		etat2.setDateDebut(new Date());
		etat2.setDateFin(new Date());
		etat2.setMotif("motif");
		etat2.setEtat(RefEtatEnum.APPROUVEE);

		DemandeRecup d2 = new DemandeRecup();
		d2.setIdAgent(9005138);
		d2.setDateDebut(sdf.parse("15/05/2013"));
		d2.setDateFin(null);
		d2.setDuree(40);
		d2.addEtatDemande(etat2);
		absEntityManager.persist(d2);

		EtatDemande etatrp = new EtatDemande();
		etatrp.setIdAgent(9005138);
		etatrp.setDate(new Date());
		etatrp.setDateDebut(new Date());
		etatrp.setDateFin(new Date());
		etatrp.setMotif("motif");
		etatrp.setEtat(RefEtatEnum.APPROUVEE);

		DemandeReposComp drp = new DemandeReposComp();
		drp.setIdAgent(9005138);
		drp.setDateDebut(sdf.parse("02/05/2013"));
		drp.setDateFin(null);
		drp.setDuree(15);
		drp.setDureeAnneeN1(10);
		drp.addEtatDemande(etatrp);
		absEntityManager.persist(drp);

		EtatDemande etatrp2 = new EtatDemande();
		etatrp2.setIdAgent(9005138);
		etatrp2.setDate(new Date());
		etatrp2.setDateDebut(new Date());
		etatrp2.setDateFin(new Date());
		etatrp2.setMotif("motif");
		etatrp2.setEtat(RefEtatEnum.APPROUVEE);

		DemandeReposComp drp2 = new DemandeReposComp();
		drp2.setIdAgent(9005138);
		drp2.setDateDebut(sdf.parse("15/06/2013"));
		drp2.setDateFin(null);
		drp2.setDuree(20);
		drp2.setDureeAnneeN1(10);
		drp2.addEtatDemande(etatrp2);
		absEntityManager.persist(drp2);

		RefEtat refEtatApprouve = new RefEtat();
		refEtatApprouve.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());
		
		// When
		int result = repository.countListeDemandesForListAgent(
				null, Arrays.asList(9005138), sdf.parse("01/06/2013"), null, null, null, Arrays.asList(refEtatApprouve));

		// Then
		assertEquals(2, result);
		
		// When
		result = repository.countListeDemandesForListAgent(
				null, Arrays.asList(9005138), sdf.parse("01/06/2015"), null, null, null, Arrays.asList(refEtatApprouve));

		// Then
		assertEquals(0, result);

		absEntityManager.flush();
		absEntityManager.clear();
	}
	
	@Test
	@Transactional("absTransactionManager")
	public void countListeDemandesForListAgent_EtatFilter_Return1Demande() throws ParseException {
		// Given
		EtatDemande etat = new EtatDemande();
		etat.setIdAgent(9005138);
		etat.setDate(new Date());
		etat.setDateDebut(new Date());
		etat.setDateFin(new Date());
		etat.setMotif("motif");
		etat.setEtat(RefEtatEnum.APPROUVEE);

		DemandeRecup d = new DemandeRecup();
		d.setIdAgent(9005138);
		d.setDateDebut(sdf.parse("15/06/2013"));
		d.setDateFin(null);
		d.setDuree(30);
		d.addEtatDemande(etat);
		absEntityManager.persist(d);

		EtatDemande etat2 = new EtatDemande();
		etat2.setIdAgent(9005138);
		etat2.setDate(new Date());
		etat2.setDateDebut(new Date());
		etat2.setDateFin(new Date());
		etat2.setMotif("motif");
		etat2.setEtat(RefEtatEnum.APPROUVEE);

		EtatDemande etat2_2 = new EtatDemande();
		etat2_2.setIdAgent(9005138);
		etat2_2.setDate(new Date());
		etat2_2.setDateDebut(new Date());
		etat2_2.setDateFin(new Date());
		etat2_2.setMotif("motif");
		etat2_2.setEtat(RefEtatEnum.ANNULEE);

		DemandeRecup d2 = new DemandeRecup();
		d2.setIdAgent(9005138);
		d2.setDateDebut(sdf.parse("15/07/2013"));
		d2.setDateFin(null);
		d2.setDuree(40);
		d2.addEtatDemande(etat2);
		d2.addEtatDemande(etat2_2);
		absEntityManager.persist(d2);

		EtatDemande etatrp = new EtatDemande();
		etatrp.setIdAgent(9005138);
		etatrp.setDate(new Date());
		etatrp.setDateDebut(new Date());
		etatrp.setDateFin(new Date());
		etatrp.setMotif("motif");
		etatrp.setEtat(RefEtatEnum.APPROUVEE);

		DemandeReposComp drp = new DemandeReposComp();
		drp.setIdAgent(9005138);
		drp.setDateDebut(sdf.parse("02/05/2013"));
		drp.setDateFin(null);
		drp.setDuree(15);
		drp.setDureeAnneeN1(10);
		drp.addEtatDemande(etatrp);
		absEntityManager.persist(drp);

		EtatDemande etatrp2 = new EtatDemande();
		etatrp2.setIdAgent(9005138);
		etatrp2.setDate(new Date());
		etatrp2.setDateDebut(new Date());
		etatrp2.setDateFin(new Date());
		etatrp2.setMotif("motif");
		etatrp2.setEtat(RefEtatEnum.APPROUVEE);

		DemandeReposComp drp2 = new DemandeReposComp();
		drp2.setIdAgent(9005138);
		drp2.setDateDebut(sdf.parse("15/06/2013"));
		drp2.setDateFin(null);
		drp2.setDuree(20);
		drp2.setDureeAnneeN1(10);
		drp2.addEtatDemande(etatrp2);
		absEntityManager.persist(drp2);

		RefEtat refEtatANNULEE = new RefEtat();
		refEtatANNULEE.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
		
		// When
		int result = repository.countListeDemandesForListAgent(
				null, Arrays.asList(9005138), sdf.parse("01/06/2013"), null, null, null, Arrays.asList(refEtatANNULEE));

		// Then
		assertEquals(1, result);

		absEntityManager.flush();
		absEntityManager.clear();
	}
	
	@Test
	@Transactional("absTransactionManager")
	public void countListeDemandesForListAgent_Delegataire_Return1Demande() throws ParseException {
		
		
		DroitsAgent da = new DroitsAgent();
		da.setIdAgent(9005138);
		
		DroitDroitsAgent dda = new DroitDroitsAgent();
		dda.setDroitsAgent(da);
		
		da.setDroitDroitsAgent(new HashSet<DroitDroitsAgent>(Arrays.asList(dda)));
		
		Droit droit = new Droit();
		droit.setIdAgent(9005100);
		droit.setDroitDroitsAgent(new HashSet<DroitDroitsAgent>(Arrays.asList(dda)));
		
		dda.setDroit(droit);
		absEntityManager.persist(droit);
		absEntityManager.persist(da);
		absEntityManager.persist(dda);
		
		// Given
		EtatDemande etat = new EtatDemande();
		etat.setIdAgent(9005138);
		etat.setDate(new Date());
		etat.setDateDebut(new Date());
		etat.setDateFin(new Date());
		etat.setMotif("motif");
		etat.setEtat(RefEtatEnum.APPROUVEE);

		DemandeRecup d = new DemandeRecup();
		d.setIdAgent(9005138);
		d.setDateDebut(sdf.parse("15/06/2013"));
		d.setDateFin(null);
		d.setDuree(30);
		d.addEtatDemande(etat);
		absEntityManager.persist(d);

		EtatDemande etat2 = new EtatDemande();
		etat2.setIdAgent(9005138);
		etat2.setDate(new Date());
		etat2.setDateDebut(new Date());
		etat2.setDateFin(new Date());
		etat2.setMotif("motif");
		etat2.setEtat(RefEtatEnum.APPROUVEE);

		EtatDemande etat2_2 = new EtatDemande();
		etat2_2.setIdAgent(9005138);
		etat2_2.setDate(new Date());
		etat2_2.setDateDebut(new Date());
		etat2_2.setDateFin(new Date());
		etat2_2.setMotif("motif");
		etat2_2.setEtat(RefEtatEnum.ANNULEE);

		DemandeRecup d2 = new DemandeRecup();
		d2.setIdAgent(9005138);
		d2.setDateDebut(sdf.parse("15/07/2013"));
		d2.setDateFin(null);
		d2.setDuree(40);
		d2.addEtatDemande(etat2);
		d2.addEtatDemande(etat2_2);
		absEntityManager.persist(d2);

		EtatDemande etatrp = new EtatDemande();
		etatrp.setIdAgent(9005138);
		etatrp.setDate(new Date());
		etatrp.setDateDebut(new Date());
		etatrp.setDateFin(new Date());
		etatrp.setMotif("motif");
		etatrp.setEtat(RefEtatEnum.APPROUVEE);

		DemandeReposComp drp = new DemandeReposComp();
		drp.setIdAgent(9005138);
		drp.setDateDebut(sdf.parse("02/05/2013"));
		drp.setDateFin(null);
		drp.setDuree(15);
		drp.setDureeAnneeN1(10);
		drp.addEtatDemande(etatrp);
		absEntityManager.persist(drp);

		EtatDemande etatrp2 = new EtatDemande();
		etatrp2.setIdAgent(9005138);
		etatrp2.setDate(new Date());
		etatrp2.setDateDebut(new Date());
		etatrp2.setDateFin(new Date());
		etatrp2.setMotif("motif");
		etatrp2.setEtat(RefEtatEnum.APPROUVEE);

		DemandeReposComp drp2 = new DemandeReposComp();
		drp2.setIdAgent(9005138);
		drp2.setDateDebut(sdf.parse("15/06/2013"));
		drp2.setDateFin(null);
		drp2.setDuree(20);
		drp2.setDureeAnneeN1(10);
		drp2.addEtatDemande(etatrp2);
		absEntityManager.persist(drp2);

		RefEtat refEtatANNULEE = new RefEtat();
		refEtatANNULEE.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
		
		// When
		int result = repository.countListeDemandesForListAgent(
				9005100, null, sdf.parse("01/06/2013"), null, null, null, Arrays.asList(refEtatANNULEE));

		// Then
		assertEquals(1, result);

		absEntityManager.flush();
		absEntityManager.clear();
	}
	@Test
	@Transactional("absTransactionManager")
	public void listeIdsDemandesForListAgent_DateFilter_Return2Demande() throws ParseException {
		// Given
		EtatDemande etat = new EtatDemande();
		etat.setIdAgent(9005138);
		etat.setDate(new Date());
		etat.setDateDebut(new Date());
		etat.setDateFin(new Date());
		etat.setMotif("motif");
		etat.setEtat(RefEtatEnum.APPROUVEE);

		DemandeRecup d = new DemandeRecup();
		d.setIdAgent(9005138);
		d.setDateDebut(sdf.parse("15/06/2013"));
		d.setDateFin(null);
		d.setDuree(30);
		d.addEtatDemande(etat);
		absEntityManager.persist(d);

		EtatDemande etat2 = new EtatDemande();
		etat2.setIdAgent(9005138);
		etat2.setDate(new Date());
		etat2.setDateDebut(new Date());
		etat2.setDateFin(new Date());
		etat2.setMotif("motif");
		etat2.setEtat(RefEtatEnum.APPROUVEE);

		DemandeRecup d2 = new DemandeRecup();
		d2.setIdAgent(9005138);
		d2.setDateDebut(sdf.parse("15/05/2013"));
		d2.setDateFin(null);
		d2.setDuree(40);
		d2.addEtatDemande(etat2);
		absEntityManager.persist(d2);

		EtatDemande etatrp = new EtatDemande();
		etatrp.setIdAgent(9005138);
		etatrp.setDate(new Date());
		etatrp.setDateDebut(new Date());
		etatrp.setDateFin(new Date());
		etatrp.setMotif("motif");
		etatrp.setEtat(RefEtatEnum.APPROUVEE);

		DemandeReposComp drp = new DemandeReposComp();
		drp.setIdAgent(9005138);
		drp.setDateDebut(sdf.parse("02/05/2013"));
		drp.setDateFin(null);
		drp.setDuree(15);
		drp.setDureeAnneeN1(10);
		drp.addEtatDemande(etatrp);
		absEntityManager.persist(drp);

		EtatDemande etatrp2 = new EtatDemande();
		etatrp2.setIdAgent(9005138);
		etatrp2.setDate(new Date());
		etatrp2.setDateDebut(new Date());
		etatrp2.setDateFin(new Date());
		etatrp2.setMotif("motif");
		etatrp2.setEtat(RefEtatEnum.APPROUVEE);

		DemandeReposComp drp2 = new DemandeReposComp();
		drp2.setIdAgent(9005138);
		drp2.setDateDebut(sdf.parse("15/06/2013"));
		drp2.setDateFin(null);
		drp2.setDuree(20);
		drp2.setDureeAnneeN1(10);
		drp2.addEtatDemande(etatrp2);
		absEntityManager.persist(drp2);

		RefEtat refEtatApprouve = new RefEtat();
		refEtatApprouve.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());
		
		// When
		List<Integer> result = repository.listeIdsDemandesForListAgent(
				null, Arrays.asList(9005138), sdf.parse("01/06/2013"), null, null, null, Arrays.asList(refEtatApprouve), null);

		// Then
		assertEquals(2, result.size());
		
		// When
		result = repository.listeIdsDemandesForListAgent(
				null, Arrays.asList(9005138), sdf.parse("01/06/2015"), null, null, null, Arrays.asList(refEtatApprouve), null);

		// Then
		assertEquals(0, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}
	
	@Test
	@Transactional("absTransactionManager")
	public void listeIdsDemandesForListAgent_EtatFilter_Return1Demande() throws ParseException {
		// Given
		EtatDemande etat = new EtatDemande();
		etat.setIdAgent(9005138);
		etat.setDate(new Date());
		etat.setDateDebut(new Date());
		etat.setDateFin(new Date());
		etat.setMotif("motif");
		etat.setEtat(RefEtatEnum.APPROUVEE);

		DemandeRecup d = new DemandeRecup();
		d.setIdAgent(9005138);
		d.setDateDebut(sdf.parse("15/06/2013"));
		d.setDateFin(null);
		d.setDuree(30);
		d.addEtatDemande(etat);
		absEntityManager.persist(d);

		EtatDemande etat2 = new EtatDemande();
		etat2.setIdAgent(9005138);
		etat2.setDate(new Date());
		etat2.setDateDebut(new Date());
		etat2.setDateFin(new Date());
		etat2.setMotif("motif");
		etat2.setEtat(RefEtatEnum.APPROUVEE);

		EtatDemande etat2_2 = new EtatDemande();
		etat2_2.setIdAgent(9005138);
		etat2_2.setDate(new Date());
		etat2_2.setDateDebut(new Date());
		etat2_2.setDateFin(new Date());
		etat2_2.setMotif("motif");
		etat2_2.setEtat(RefEtatEnum.ANNULEE);

		DemandeRecup d2 = new DemandeRecup();
		d2.setIdAgent(9005138);
		d2.setDateDebut(sdf.parse("15/07/2013"));
		d2.setDateFin(null);
		d2.setDuree(40);
		d2.addEtatDemande(etat2);
		d2.addEtatDemande(etat2_2);
		absEntityManager.persist(d2);

		EtatDemande etatrp = new EtatDemande();
		etatrp.setIdAgent(9005138);
		etatrp.setDate(new Date());
		etatrp.setDateDebut(new Date());
		etatrp.setDateFin(new Date());
		etatrp.setMotif("motif");
		etatrp.setEtat(RefEtatEnum.APPROUVEE);

		DemandeReposComp drp = new DemandeReposComp();
		drp.setIdAgent(9005138);
		drp.setDateDebut(sdf.parse("02/05/2013"));
		drp.setDateFin(null);
		drp.setDuree(15);
		drp.setDureeAnneeN1(10);
		drp.addEtatDemande(etatrp);
		absEntityManager.persist(drp);

		EtatDemande etatrp2 = new EtatDemande();
		etatrp2.setIdAgent(9005138);
		etatrp2.setDate(new Date());
		etatrp2.setDateDebut(new Date());
		etatrp2.setDateFin(new Date());
		etatrp2.setMotif("motif");
		etatrp2.setEtat(RefEtatEnum.APPROUVEE);

		DemandeReposComp drp2 = new DemandeReposComp();
		drp2.setIdAgent(9005138);
		drp2.setDateDebut(sdf.parse("15/06/2013"));
		drp2.setDateFin(null);
		drp2.setDuree(20);
		drp2.setDureeAnneeN1(10);
		drp2.addEtatDemande(etatrp2);
		absEntityManager.persist(drp2);

		RefEtat refEtatANNULEE = new RefEtat();
		refEtatANNULEE.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
		
		// When
		List<Integer> result = repository.listeIdsDemandesForListAgent(
				null, Arrays.asList(9005138), sdf.parse("01/06/2013"), null, null, null, Arrays.asList(refEtatANNULEE), null);

		// Then
		assertEquals(1, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}
	
	@Test
	@Transactional("absTransactionManager")
	public void listeIdsDemandesForListAgent_Delegataire_Return1Demande() throws ParseException {
		
		
		DroitsAgent da = new DroitsAgent();
		da.setIdAgent(9005138);
		
		DroitDroitsAgent dda = new DroitDroitsAgent();
		dda.setDroitsAgent(da);
		
		da.setDroitDroitsAgent(new HashSet<DroitDroitsAgent>(Arrays.asList(dda)));
		
		Droit droit = new Droit();
		droit.setIdAgent(9005100);
		droit.setDroitDroitsAgent(new HashSet<DroitDroitsAgent>(Arrays.asList(dda)));
		
		dda.setDroit(droit);
		absEntityManager.persist(droit);
		absEntityManager.persist(da);
		absEntityManager.persist(dda);
		
		// Given
		EtatDemande etat = new EtatDemande();
		etat.setIdAgent(9005138);
		etat.setDate(new Date());
		etat.setDateDebut(new Date());
		etat.setDateFin(new Date());
		etat.setMotif("motif");
		etat.setEtat(RefEtatEnum.APPROUVEE);

		DemandeRecup d = new DemandeRecup();
		d.setIdAgent(9005138);
		d.setDateDebut(sdf.parse("15/06/2013"));
		d.setDateFin(null);
		d.setDuree(30);
		d.addEtatDemande(etat);
		absEntityManager.persist(d);

		EtatDemande etat2 = new EtatDemande();
		etat2.setIdAgent(9005138);
		etat2.setDate(new Date());
		etat2.setDateDebut(new Date());
		etat2.setDateFin(new Date());
		etat2.setMotif("motif");
		etat2.setEtat(RefEtatEnum.APPROUVEE);

		EtatDemande etat2_2 = new EtatDemande();
		etat2_2.setIdAgent(9005138);
		etat2_2.setDate(new Date());
		etat2_2.setDateDebut(new Date());
		etat2_2.setDateFin(new Date());
		etat2_2.setMotif("motif");
		etat2_2.setEtat(RefEtatEnum.ANNULEE);

		DemandeRecup d2 = new DemandeRecup();
		d2.setIdAgent(9005138);
		d2.setDateDebut(sdf.parse("15/07/2013"));
		d2.setDateFin(null);
		d2.setDuree(40);
		d2.addEtatDemande(etat2);
		d2.addEtatDemande(etat2_2);
		absEntityManager.persist(d2);

		EtatDemande etatrp = new EtatDemande();
		etatrp.setIdAgent(9005138);
		etatrp.setDate(new Date());
		etatrp.setDateDebut(new Date());
		etatrp.setDateFin(new Date());
		etatrp.setMotif("motif");
		etatrp.setEtat(RefEtatEnum.APPROUVEE);

		DemandeReposComp drp = new DemandeReposComp();
		drp.setIdAgent(9005138);
		drp.setDateDebut(sdf.parse("02/05/2013"));
		drp.setDateFin(null);
		drp.setDuree(15);
		drp.setDureeAnneeN1(10);
		drp.addEtatDemande(etatrp);
		absEntityManager.persist(drp);

		EtatDemande etatrp2 = new EtatDemande();
		etatrp2.setIdAgent(9005138);
		etatrp2.setDate(new Date());
		etatrp2.setDateDebut(new Date());
		etatrp2.setDateFin(new Date());
		etatrp2.setMotif("motif");
		etatrp2.setEtat(RefEtatEnum.APPROUVEE);

		DemandeReposComp drp2 = new DemandeReposComp();
		drp2.setIdAgent(9005138);
		drp2.setDateDebut(sdf.parse("15/06/2013"));
		drp2.setDateFin(null);
		drp2.setDuree(20);
		drp2.setDureeAnneeN1(10);
		drp2.addEtatDemande(etatrp2);
		absEntityManager.persist(drp2);

		RefEtat refEtatANNULEE = new RefEtat();
		refEtatANNULEE.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
		
		// When
		List<Integer> result = repository.listeIdsDemandesForListAgent(
				9005100, null, sdf.parse("01/06/2013"), null, null, null, Arrays.asList(refEtatANNULEE), null);

		// Then
		assertEquals(1, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}
	
	@Test
	@Transactional("absTransactionManager")
	public void listeDemandesByListIdsDemande_Return1Demande() throws ParseException {
		
		
		DroitsAgent da = new DroitsAgent();
		da.setIdAgent(9005138);
		
		DroitDroitsAgent dda = new DroitDroitsAgent();
		dda.setDroitsAgent(da);
		
		da.setDroitDroitsAgent(new HashSet<DroitDroitsAgent>(Arrays.asList(dda)));
		
		Droit droit = new Droit();
		droit.setIdAgent(9005100);
		droit.setDroitDroitsAgent(new HashSet<DroitDroitsAgent>(Arrays.asList(dda)));
		
		dda.setDroit(droit);
		absEntityManager.persist(droit);
		absEntityManager.persist(da);
		absEntityManager.persist(dda);
		
		// Given
		EtatDemande etat = new EtatDemande();
		etat.setIdAgent(9005138);
		etat.setDate(new Date());
		etat.setDateDebut(new Date());
		etat.setDateFin(new Date());
		etat.setMotif("motif");
		etat.setEtat(RefEtatEnum.APPROUVEE);

		DemandeRecup d = new DemandeRecup();
		d.setIdAgent(9005138);
		d.setDateDebut(sdf.parse("15/06/2013"));
		d.setDateFin(null);
		d.setDuree(30);
		d.addEtatDemande(etat);
		absEntityManager.persist(d);

		EtatDemande etat2 = new EtatDemande();
		etat2.setIdAgent(9005138);
		etat2.setDate(new Date());
		etat2.setDateDebut(new Date());
		etat2.setDateFin(new Date());
		etat2.setMotif("motif");
		etat2.setEtat(RefEtatEnum.APPROUVEE);

		EtatDemande etat2_2 = new EtatDemande();
		etat2_2.setIdAgent(9005138);
		etat2_2.setDate(new Date());
		etat2_2.setDateDebut(new Date());
		etat2_2.setDateFin(new Date());
		etat2_2.setMotif("motif");
		etat2_2.setEtat(RefEtatEnum.ANNULEE);

		DemandeRecup d2 = new DemandeRecup();
		d2.setIdAgent(9005138);
		d2.setDateDebut(sdf.parse("15/07/2013"));
		d2.setDateFin(null);
		d2.setDuree(40);
		d2.addEtatDemande(etat2);
		d2.addEtatDemande(etat2_2);
		absEntityManager.persist(d2);

		EtatDemande etatrp = new EtatDemande();
		etatrp.setIdAgent(9005138);
		etatrp.setDate(new Date());
		etatrp.setDateDebut(new Date());
		etatrp.setDateFin(new Date());
		etatrp.setMotif("motif");
		etatrp.setEtat(RefEtatEnum.APPROUVEE);

		DemandeReposComp drp = new DemandeReposComp();
		drp.setIdAgent(9005138);
		drp.setDateDebut(sdf.parse("02/05/2013"));
		drp.setDateFin(null);
		drp.setDuree(15);
		drp.setDureeAnneeN1(10);
		drp.addEtatDemande(etatrp);
		absEntityManager.persist(drp);

		EtatDemande etatrp2 = new EtatDemande();
		etatrp2.setIdAgent(9005138);
		etatrp2.setDate(new Date());
		etatrp2.setDateDebut(new Date());
		etatrp2.setDateFin(new Date());
		etatrp2.setMotif("motif");
		etatrp2.setEtat(RefEtatEnum.APPROUVEE);

		DemandeReposComp drp2 = new DemandeReposComp();
		drp2.setIdAgent(9005138);
		drp2.setDateDebut(sdf.parse("15/06/2013"));
		drp2.setDateFin(null);
		drp2.setDuree(20);
		drp2.setDureeAnneeN1(10);
		drp2.addEtatDemande(etatrp2);
		absEntityManager.persist(drp2);

		RefEtat refEtatANNULEE = new RefEtat();
		refEtatANNULEE.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
		
		// When
		List<Demande> result = repository.listeDemandesByListIdsDemande(Arrays.asList(d2.getIdDemande()));

		// Then
		assertEquals(1, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}
}
