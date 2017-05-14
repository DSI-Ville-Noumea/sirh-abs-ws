package nc.noumea.mairie.abs.repository;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nc.noumea.mairie.abs.domain.DemandeMaladies;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.RefDroitsMaladies;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;

import org.hibernate.engine.spi.IdentifierValue;
import org.hibernate.tuple.IdentifierProperty;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/META-INF/spring/applicationContext-test.xml" })
public class MaladiesRepositoryTest {

	@Autowired
	private MaladiesRepository repository;

	@PersistenceContext(unitName = "absPersistenceUnit")
	private EntityManager absEntityManager;
	
	private RefTypeAbsence type;
	
	@Before
	@Transactional("absTransactionManager")
	public void init() throws Throwable {
		type = new RefTypeAbsence();
//		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.MALADIE.getValue());
		type.setLabel("Maladies");
		absEntityManager.persist(type);
	}

	public IdentifierValue setUnsavedValue(IdentifierProperty ip, IdentifierValue newUnsavedValue) throws Throwable {
	    IdentifierValue backup = ip.getUnsavedValue();
	    Field f = ip.getClass().getDeclaredField("unsavedValue");
	    f.setAccessible(true);
	    f.set(ip, newUnsavedValue);
	  	return backup;
	}

//	@Test
	@Transactional("absTransactionManager")
	public void getListMaladiesAnneGlissanteByAgent_ok_3results_testsDates() {
		
		Integer idAgent = 9005138;

		Date dateFinSearch = new DateTime(2015,12,1,0,0,0).toDate();
		Date dateDebutSearch = new DateTime(dateFinSearch).minusYears(1).plusDays(1).toDate();
		
		assertEquals(new DateTime(2014,12,2,0,0,0).toDate(), dateDebutSearch);
		
		Date dateJour = new Date();
		
		// demande avant la date de debut de la periode 
		List<EtatDemande> listEtatDemande1 = new ArrayList<EtatDemande>();
		EtatDemande et1 = new EtatDemande();
		et1.setDate(dateJour);
		et1.setEtat(RefEtatEnum.PRISE);
		et1.setIdAgent(idAgent);
		listEtatDemande1.add(et1);
		
		DemandeMaladies demandeMaladie1 = new DemandeMaladies();
		demandeMaladie1.setType(type);
		demandeMaladie1.setDateDebut(new DateTime(dateDebutSearch).minusWeeks(2).toDate());
		demandeMaladie1.setDateFin(new DateTime(dateDebutSearch).minusWeeks(1).toDate());
		demandeMaladie1.setDuree(3.0);
		demandeMaladie1.setIdAgent(idAgent);
		demandeMaladie1.setEtatsDemande(listEtatDemande1);
		et1.setDemande(demandeMaladie1);
		absEntityManager.persist(demandeMaladie1);
		
		// demande a cheval sur la date de debut de la periode 
		List<EtatDemande> listEtatDemande2 = new ArrayList<EtatDemande>();
		EtatDemande et2 = new EtatDemande();
		et2.setDate(dateJour);
		et2.setEtat(RefEtatEnum.PRISE);
		et2.setIdAgent(idAgent);
		listEtatDemande2.add(et2);
		
		DemandeMaladies demandeMaladie2 = new DemandeMaladies();
		demandeMaladie2.setType(type);
		demandeMaladie2.setDateDebut(new DateTime(dateDebutSearch).minusDays(1).toDate());
		demandeMaladie2.setDateFin(new DateTime(dateDebutSearch).plusDays(1).toDate());
		demandeMaladie2.setDuree(3.0);
		demandeMaladie2.setIdAgent(idAgent);
		demandeMaladie2.setEtatsDemande(listEtatDemande2);
		et2.setDemande(demandeMaladie2);
		absEntityManager.persist(demandeMaladie2);
		
		// demande dans la periode 
		List<EtatDemande> listEtatDemande3 = new ArrayList<EtatDemande>();
		EtatDemande et3 = new EtatDemande();
		et3.setDate(dateJour);
		et3.setEtat(RefEtatEnum.PRISE);
		et3.setIdAgent(idAgent);
		listEtatDemande3.add(et3);
		
		DemandeMaladies demandeMaladie3 = new DemandeMaladies();
		demandeMaladie3.setType(type);
		demandeMaladie3.setDateDebut(new DateTime(dateDebutSearch).plusDays(1).toDate());
		demandeMaladie3.setDateFin(new DateTime(dateFinSearch).minusDays(1).toDate());
		demandeMaladie3.setDuree(3.0);
		demandeMaladie3.setIdAgent(idAgent);
		demandeMaladie3.setEtatsDemande(listEtatDemande3);
		et3.setDemande(demandeMaladie3);
		absEntityManager.persist(demandeMaladie3);
		
		// demande a cheval sur la date de fin de la periode
		List<EtatDemande> listEtatDemande4 = new ArrayList<EtatDemande>();
		EtatDemande et4 = new EtatDemande();
		et4.setDate(dateJour);
		et4.setEtat(RefEtatEnum.PRISE);
		et4.setIdAgent(idAgent);
		listEtatDemande4.add(et4);
		
		DemandeMaladies demandeMaladie4 = new DemandeMaladies();
		demandeMaladie4.setType(type);
		demandeMaladie4.setDateDebut(new DateTime(dateFinSearch).minusDays(1).toDate());
		demandeMaladie4.setDateFin(new DateTime(dateFinSearch).plusDays(1).toDate());
		demandeMaladie4.setDuree(3.0);
		demandeMaladie4.setIdAgent(idAgent);
		demandeMaladie4.setEtatsDemande(listEtatDemande4);
		et4.setDemande(demandeMaladie4);
		absEntityManager.persist(demandeMaladie4);
		
		// demande apres la periode
		List<EtatDemande> listEtatDemande5 = new ArrayList<EtatDemande>();
		EtatDemande et5 = new EtatDemande();
		et5.setDate(dateJour);
		et5.setEtat(RefEtatEnum.PRISE);
		et5.setIdAgent(idAgent);
		listEtatDemande5.add(et5);
		
		DemandeMaladies demandeMaladie5 = new DemandeMaladies();
		demandeMaladie5.setType(type);
		demandeMaladie5.setDateDebut(new DateTime(dateFinSearch).plusDays(1).toDate());
		demandeMaladie5.setDateFin(new DateTime(dateFinSearch).plusDays(10).toDate());
		demandeMaladie5.setDuree(3.0);
		demandeMaladie5.setIdAgent(idAgent);
		demandeMaladie5.setEtatsDemande(listEtatDemande5);
		et5.setDemande(demandeMaladie5);
		absEntityManager.persist(demandeMaladie5);
		
		Date dateDebutAnneeGlissante = new DateTime(dateFinSearch).minusYears(1).plusDays(1).toDate();
		
		List<DemandeMaladies> result = repository.getListMaladiesAnneGlissanteByAgent(idAgent, dateDebutAnneeGlissante, dateFinSearch);
		
		assertEquals(3, result.size());
		assertEquals(result.get(0), demandeMaladie2);
		assertEquals(result.get(1), demandeMaladie3);
		assertEquals(result.get(2), demandeMaladie4);
	}

	@Test
	@Transactional("absTransactionManager")
	public void getListMaladiesAnneGlissanteByAgent_badEtat() {
		
		Integer idAgent = 9005138;
		
		Date dateJour = new Date();
		
		List<EtatDemande> listEtatDemande = new ArrayList<EtatDemande>();
		EtatDemande et = new EtatDemande();
		et.setDate(dateJour);
		et.setEtat(RefEtatEnum.VALIDEE);
		et.setIdAgent(9005168);
		listEtatDemande.add(et);
		
		DemandeMaladies demandeMaladie1 = new DemandeMaladies();
		demandeMaladie1.setType(type);
		demandeMaladie1.setDateDebut(new DateTime(2011,10,3,0,0,0).toDate());
		demandeMaladie1.setDateFin(new DateTime(2011,10,5,0,0,0).toDate());
		demandeMaladie1.setDuree(3.0);
		demandeMaladie1.setIdAgent(idAgent);
		demandeMaladie1.setEtatsDemande(listEtatDemande);
		et.setDemande(demandeMaladie1);
		absEntityManager.persist(demandeMaladie1);
		
		Date dateFinSearch = new DateTime(demandeMaladie1.getDateFin()).plusDays(1).toDate();
		
		Date dateDebutAnneeGlissante = new DateTime(dateFinSearch).minusYears(1).plusDays(1).toDate();
		
		List<DemandeMaladies> result = repository.getListMaladiesAnneGlissanteByAgent(idAgent, dateDebutAnneeGlissante, dateFinSearch);
		
		assertEquals(0, result.size());
	}

	@Test
	@Transactional("absTransactionManager")
	public void getListMaladiesAnneGlissanteByAgent_badAgent() {
		
		Integer idAgent = 9005138;
		
		Date dateJour = new Date();
		
		List<EtatDemande> listEtatDemande = new ArrayList<EtatDemande>();
		EtatDemande et = new EtatDemande();
		et.setDate(dateJour);
		et.setEtat(RefEtatEnum.PRISE);
		et.setIdAgent(idAgent);
		listEtatDemande.add(et);
		
		DemandeMaladies demandeMaladie1 = new DemandeMaladies();
		demandeMaladie1.setType(type);
		demandeMaladie1.setDateDebut(new DateTime(2011,10,3,0,0,0).toDate());
		demandeMaladie1.setDateFin(new DateTime(2011,10,5,0,0,0).toDate());
		demandeMaladie1.setDuree(3.0);
		demandeMaladie1.setIdAgent(idAgent);
		demandeMaladie1.setEtatsDemande(listEtatDemande);
		et.setDemande(demandeMaladie1);
		absEntityManager.persist(demandeMaladie1);
		
		Date dateFinSearch = new DateTime(demandeMaladie1.getDateFin()).plusDays(1).toDate();
		
		Date dateDebutAnneeGlissante = new DateTime(dateFinSearch).minusYears(1).plusDays(1).toDate();
		
		List<DemandeMaladies> result = repository.getListMaladiesAnneGlissanteByAgent(idAgent+1, dateDebutAnneeGlissante, dateFinSearch);
		
		assertEquals(0, result.size());
	}
	
	@Test
	@Transactional("absTransactionManager")
	public void getDroitsMaladies() {
		
		RefDroitsMaladies d1 = new RefDroitsMaladies();
		d1.setFonctionnaire(true);
		d1.setContractuel(false);
		d1.setConventionCollective(false);
		d1.setAnneeAnciennete(99);
		d1.setNombreJoursPleinSalaire(90);
		d1.setNombreJoursDemiSalaire(270);
		absEntityManager.persist(d1);
		
		RefDroitsMaladies d2 = new RefDroitsMaladies();
		d2.setFonctionnaire(false);
		d2.setContractuel(true);
		d2.setConventionCollective(true);
		d2.setAnneeAnciennete(1);
		d2.setNombreJoursPleinSalaire(15);
		d2.setNombreJoursDemiSalaire(0);
		absEntityManager.persist(d2);

		RefDroitsMaladies d3 = new RefDroitsMaladies();
		d3.setFonctionnaire(false);
		d3.setContractuel(true);
		d3.setConventionCollective(true);
		d3.setAnneeAnciennete(3);
		d3.setNombreJoursPleinSalaire(30);
		d3.setNombreJoursDemiSalaire(30);
		absEntityManager.persist(d3);
		
		RefDroitsMaladies d4 = new RefDroitsMaladies();
		d4.setFonctionnaire(false);
		d4.setContractuel(true);
		d4.setConventionCollective(true);
		d4.setAnneeAnciennete(5);
		d4.setNombreJoursPleinSalaire(30);
		d4.setNombreJoursDemiSalaire(60);
		absEntityManager.persist(d4);
		
		RefDroitsMaladies d5 = new RefDroitsMaladies();
		d5.setFonctionnaire(false);
		d5.setContractuel(true);
		d5.setConventionCollective(true);
		d5.setAnneeAnciennete(10);
		d5.setNombreJoursPleinSalaire(45);
		d5.setNombreJoursDemiSalaire(75);
		absEntityManager.persist(d5);
		
		RefDroitsMaladies d6 = new RefDroitsMaladies();
		d6.setFonctionnaire(false);
		d6.setContractuel(true);
		d6.setConventionCollective(true);
		d6.setAnneeAnciennete(15);
		d6.setNombreJoursPleinSalaire(60);
		d6.setNombreJoursDemiSalaire(90);
		absEntityManager.persist(d6);
		
		RefDroitsMaladies d7 = new RefDroitsMaladies();
		d7.setFonctionnaire(false);
		d7.setContractuel(true);
		d7.setConventionCollective(true);
		d7.setAnneeAnciennete(99);
		d7.setNombreJoursPleinSalaire(90);
		d7.setNombreJoursDemiSalaire(90);
		absEntityManager.persist(d7);

		RefDroitsMaladies result = repository.getDroitsMaladies(true, false, false, null);
		assertEquals(result, d1);
		
		result = repository.getDroitsMaladies(false, true, false, 0);
		assertEquals(result, d2);
		
		result = repository.getDroitsMaladies(false, false, true, 1);
		assertEquals(result, d3);

		result = repository.getDroitsMaladies(false, false, true, 4);
		assertEquals(result, d4);

		result = repository.getDroitsMaladies(false, false, true, 7);
		assertEquals(result, d5);
		
		result = repository.getDroitsMaladies(false, false, true, 10);
		assertEquals(result, d6);

		result = repository.getDroitsMaladies(false, false, true, 22);
		assertEquals(result, d7);
	}

}
