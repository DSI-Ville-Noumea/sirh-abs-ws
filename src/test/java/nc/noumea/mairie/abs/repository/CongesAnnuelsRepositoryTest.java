package nc.noumea.mairie.abs.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nc.noumea.mairie.abs.domain.AgentWeekCongeAnnuel;
import nc.noumea.mairie.abs.domain.CongeAnnuelAlimAutoHisto;
import nc.noumea.mairie.abs.domain.CongeAnnuelRestitutionMassiveHisto;
import nc.noumea.mairie.abs.domain.DemandeCongesAnnuels;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.dto.RestitutionMassiveDto;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/META-INF/spring/applicationContext-test.xml" })
public class CongesAnnuelsRepositoryTest {

	@Autowired
	CongesAnnuelsRepository repository;

	@PersistenceContext(unitName = "absPersistenceUnit")
	private EntityManager absEntityManager;

	SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

	@Test
	@Transactional("absTransactionManager")
	public void countDureeByPeriodeAndTypeDemandeTest_etatDemandeKo() throws ParseException {

		Integer idAgent = 9005138;

		RefTypeAbsence type = new RefTypeAbsence();
		absEntityManager.persist(type);

		DemandeCongesAnnuels demandePROVISOIRE = new DemandeCongesAnnuels();
		demandePROVISOIRE.setDuree(10.0);
		demandePROVISOIRE.setDateDebut(sdf.parse("15/06/2013"));
		demandePROVISOIRE.setDateFin(sdf.parse("25/06/2013"));
		demandePROVISOIRE.setIdAgent(idAgent);
		demandePROVISOIRE.setType(type);
		absEntityManager.persist(demandePROVISOIRE);

		EtatDemande etatDemandePROVISOIRE = new EtatDemande();
		etatDemandePROVISOIRE.setDemande(demandePROVISOIRE);
		etatDemandePROVISOIRE.setIdAgent(9000001);
		etatDemandePROVISOIRE.setEtat(RefEtatEnum.PROVISOIRE);
		absEntityManager.persist(etatDemandePROVISOIRE);

		DemandeCongesAnnuels demandeREFUSEE = new DemandeCongesAnnuels();
		demandeREFUSEE.setDuree(10.0);
		demandeREFUSEE.setDateDebut(sdf.parse("15/06/2013"));
		demandeREFUSEE.setDateFin(sdf.parse("25/06/2013"));
		demandeREFUSEE.setIdAgent(idAgent);
		demandeREFUSEE.setType(type);
		absEntityManager.persist(demandeREFUSEE);

		EtatDemande etatDemandeREFUSEE = new EtatDemande();
		etatDemandeREFUSEE.setDemande(demandeREFUSEE);
		etatDemandeREFUSEE.setIdAgent(9000001);
		etatDemandeREFUSEE.setEtat(RefEtatEnum.REFUSEE);
		absEntityManager.persist(etatDemandeREFUSEE);

		DemandeCongesAnnuels demandeANNULEE = new DemandeCongesAnnuels();
		demandeANNULEE.setDuree(10.0);
		demandeANNULEE.setDateDebut(sdf.parse("15/06/2013"));
		demandeANNULEE.setDateFin(sdf.parse("25/06/2013"));
		demandeANNULEE.setIdAgent(idAgent);
		demandeANNULEE.setType(type);
		absEntityManager.persist(demandeANNULEE);

		EtatDemande etatDemandeANNULEE = new EtatDemande();
		etatDemandeANNULEE.setDemande(demandeANNULEE);
		etatDemandeANNULEE.setIdAgent(9000001);
		etatDemandeANNULEE.setEtat(RefEtatEnum.ANNULEE);
		absEntityManager.persist(etatDemandeANNULEE);

		DemandeCongesAnnuels demandeREJETE = new DemandeCongesAnnuels();
		demandeREJETE.setDuree(10.0);
		demandeREJETE.setDateDebut(sdf.parse("15/06/2013"));
		demandeREJETE.setDateFin(sdf.parse("25/06/2013"));
		demandeREJETE.setIdAgent(idAgent);
		demandeREJETE.setType(type);
		absEntityManager.persist(demandeREJETE);

		EtatDemande etatDemandeREJETE = new EtatDemande();
		etatDemandeREJETE.setDemande(demandeREJETE);
		etatDemandeREJETE.setIdAgent(9000001);
		etatDemandeREJETE.setEtat(RefEtatEnum.REJETE);
		absEntityManager.persist(etatDemandeREJETE);

		Double result = repository.getSommeDureeDemandeCongeAnnuelEnCoursSaisieouViseeouAValider(idAgent,
				demandePROVISOIRE.getIdDemande());

		assertEquals(result, new Double(0));

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void countDureeByPeriodeAndTypeDemandeTest_etatDemandeOK() throws ParseException {

		Integer idAgent = 9005138;

		RefTypeAbsence type = new RefTypeAbsence();
		absEntityManager.persist(type);

		DemandeCongesAnnuels demandeSAISIE = new DemandeCongesAnnuels();
		demandeSAISIE.setDuree(10.0);
		demandeSAISIE.setDateDebut(sdf.parse("15/06/2013"));
		demandeSAISIE.setDateFin(sdf.parse("25/06/2013"));
		demandeSAISIE.setIdAgent(idAgent);
		demandeSAISIE.setType(type);
		absEntityManager.persist(demandeSAISIE);

		EtatDemande etatDemandeSAISIE = new EtatDemande();
		etatDemandeSAISIE.setDemande(demandeSAISIE);
		etatDemandeSAISIE.setIdAgent(9000001);
		etatDemandeSAISIE.setEtat(RefEtatEnum.SAISIE);
		absEntityManager.persist(etatDemandeSAISIE);

		DemandeCongesAnnuels demandeVISEE_FAVORABLE = new DemandeCongesAnnuels();
		demandeVISEE_FAVORABLE.setDuree(10.0);
		demandeVISEE_FAVORABLE.setDateDebut(sdf.parse("15/06/2013"));
		demandeVISEE_FAVORABLE.setDateFin(sdf.parse("25/06/2013"));
		demandeVISEE_FAVORABLE.setIdAgent(idAgent);
		demandeVISEE_FAVORABLE.setType(type);
		absEntityManager.persist(demandeVISEE_FAVORABLE);

		EtatDemande etatDemandeVISEE_FAVORABLE = new EtatDemande();
		etatDemandeVISEE_FAVORABLE.setDemande(demandeVISEE_FAVORABLE);
		etatDemandeVISEE_FAVORABLE.setIdAgent(9000001);
		etatDemandeVISEE_FAVORABLE.setEtat(RefEtatEnum.VISEE_FAVORABLE);
		absEntityManager.persist(etatDemandeVISEE_FAVORABLE);

		DemandeCongesAnnuels demandeVISEE_DEFAVORABLE = new DemandeCongesAnnuels();
		demandeVISEE_DEFAVORABLE.setDuree(10.0);
		demandeVISEE_DEFAVORABLE.setDateDebut(sdf.parse("15/06/2013"));
		demandeVISEE_DEFAVORABLE.setDateFin(sdf.parse("25/06/2013"));
		demandeVISEE_DEFAVORABLE.setIdAgent(idAgent);
		demandeVISEE_DEFAVORABLE.setType(type);
		absEntityManager.persist(demandeVISEE_DEFAVORABLE);

		EtatDemande etatDemandeVISEE_DEFAVORABLE = new EtatDemande();
		etatDemandeVISEE_DEFAVORABLE.setDemande(demandeVISEE_DEFAVORABLE);
		etatDemandeVISEE_DEFAVORABLE.setIdAgent(9000001);
		etatDemandeVISEE_DEFAVORABLE.setEtat(RefEtatEnum.VISEE_DEFAVORABLE);
		absEntityManager.persist(etatDemandeVISEE_DEFAVORABLE);

		DemandeCongesAnnuels demandeAPPROUVE = new DemandeCongesAnnuels();
		demandeAPPROUVE.setDuree(10.0);
		demandeAPPROUVE.setDateDebut(sdf.parse("15/06/2013"));
		demandeAPPROUVE.setDateFin(sdf.parse("25/06/2013"));
		demandeAPPROUVE.setIdAgent(idAgent);
		demandeAPPROUVE.setType(type);
		absEntityManager.persist(demandeAPPROUVE);

		EtatDemande etatDemandeAPPROUVE = new EtatDemande();
		etatDemandeAPPROUVE.setDemande(demandeAPPROUVE);
		etatDemandeAPPROUVE.setIdAgent(9000001);
		etatDemandeAPPROUVE.setEtat(RefEtatEnum.APPROUVEE);
		absEntityManager.persist(etatDemandeAPPROUVE);

		DemandeCongesAnnuels demandeEN_ATTENTE = new DemandeCongesAnnuels();
		demandeEN_ATTENTE.setDuree(10.0);
		demandeEN_ATTENTE.setDateDebut(sdf.parse("15/06/2013"));
		demandeEN_ATTENTE.setDateFin(sdf.parse("25/06/2013"));
		demandeEN_ATTENTE.setIdAgent(idAgent);
		demandeEN_ATTENTE.setType(type);
		absEntityManager.persist(demandeEN_ATTENTE);

		EtatDemande etatDemandeEN_ATTENTE = new EtatDemande();
		etatDemandeEN_ATTENTE.setDemande(demandeEN_ATTENTE);
		etatDemandeEN_ATTENTE.setIdAgent(9000001);
		etatDemandeEN_ATTENTE.setEtat(RefEtatEnum.EN_ATTENTE);
		absEntityManager.persist(etatDemandeEN_ATTENTE);

		DemandeCongesAnnuels demandePRISE = new DemandeCongesAnnuels();
		demandePRISE.setDuree(10.0);
		demandePRISE.setDateDebut(sdf.parse("15/06/2013"));
		demandePRISE.setDateFin(sdf.parse("25/06/2013"));
		demandePRISE.setIdAgent(idAgent);
		demandePRISE.setType(type);
		absEntityManager.persist(demandePRISE);

		EtatDemande etatDemandePRISE = new EtatDemande();
		etatDemandePRISE.setDemande(demandePRISE);
		etatDemandePRISE.setIdAgent(9000001);
		etatDemandePRISE.setEtat(RefEtatEnum.PRISE);
		absEntityManager.persist(etatDemandePRISE);

		DemandeCongesAnnuels demandeVALIDEE = new DemandeCongesAnnuels();
		demandeVALIDEE.setDuree(10.0);
		demandeVALIDEE.setDateDebut(sdf.parse("15/06/2013"));
		demandeVALIDEE.setDateFin(sdf.parse("25/06/2013"));
		demandeVALIDEE.setIdAgent(idAgent);
		demandeVALIDEE.setType(type);
		absEntityManager.persist(demandeVALIDEE);

		EtatDemande etatDemandeVALIDEE = new EtatDemande();
		etatDemandeVALIDEE.setDemande(demandeVALIDEE);
		etatDemandeVALIDEE.setIdAgent(9000001);
		etatDemandeVALIDEE.setEtat(RefEtatEnum.VALIDEE);
		absEntityManager.persist(etatDemandeVALIDEE);

		DemandeCongesAnnuels demandeAVALIDEE = new DemandeCongesAnnuels();
		demandeAVALIDEE.setDuree(10.0);
		demandeAVALIDEE.setDateDebut(sdf.parse("15/06/2013"));
		demandeAVALIDEE.setDateFin(sdf.parse("25/06/2013"));
		demandeAVALIDEE.setIdAgent(idAgent);
		demandeAVALIDEE.setType(type);
		absEntityManager.persist(demandeAVALIDEE);

		EtatDemande etatDemandeAVALIDEE = new EtatDemande();
		etatDemandeAVALIDEE.setDemande(demandeVALIDEE);
		etatDemandeAVALIDEE.setIdAgent(9000001);
		etatDemandeAVALIDEE.setEtat(RefEtatEnum.A_VALIDER);
		absEntityManager.persist(etatDemandeAVALIDEE);

		Double result = repository.getSommeDureeDemandeCongeAnnuelEnCoursSaisieouViseeouAValider(idAgent,
				demandeSAISIE.getIdDemande());

		assertEquals(result, new Double(30));

		absEntityManager.flush();
		absEntityManager.clear();
	}
	
	@Test 
	@Transactional("absTransactionManager")
	public void getWeekHistoForAgentAndDate_ok() {
		
		Date dateMonth = new Date();
		
		AgentWeekCongeAnnuel d = new AgentWeekCongeAnnuel();
		d.setDateMonth(dateMonth);
		d.setIdAgent(9005138);
		d.setIdAgentWeekCongeAnnuel(1);
		d.setJours(10.0);
		d.setLastModification(new Date());
		absEntityManager.persist(d);
		
		AgentWeekCongeAnnuel result = repository.getWeekHistoForAgentAndDate(9005138, dateMonth);
		
		assertNotNull(result);
		
		absEntityManager.flush();
		absEntityManager.clear();
	}
	
	@Test 
	@Transactional("absTransactionManager")
	public void getWeekHistoForAgentAndDate_badAgent() {
		
		Date dateMonth = new Date();
		
		AgentWeekCongeAnnuel d = new AgentWeekCongeAnnuel();
		d.setDateMonth(dateMonth);
		d.setIdAgent(9005138);
		d.setIdAgentWeekCongeAnnuel(1);
		d.setJours(10.0);
		d.setLastModification(new Date());
		absEntityManager.persist(d);
		
		AgentWeekCongeAnnuel result = repository.getWeekHistoForAgentAndDate(9009999, dateMonth);
		
		assertNull(result);
		
		absEntityManager.flush();
		absEntityManager.clear();
	}
	
	@Test 
	@Transactional("absTransactionManager")
	public void getWeekHistoForAgentAndDate_badDate() {
		
		DateTime dateMonth = new DateTime();
		
		AgentWeekCongeAnnuel d = new AgentWeekCongeAnnuel();
		d.setDateMonth(dateMonth.toDate());
		d.setIdAgent(9005138);
		d.setIdAgentWeekCongeAnnuel(1);
		d.setJours(10.0);
		d.setLastModification(new Date());
		absEntityManager.persist(d);
		
		AgentWeekCongeAnnuel result = repository.getWeekHistoForAgentAndDate(9005138, dateMonth.plusDays(1).toDate());
		
		assertNull(result);
		
		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getListeMoisAlimAutoCongeAnnuel_0Date() {

		List<Date> result = repository.getListeMoisAlimAutoCongeAnnuel();

		assertEquals(result.size(), 0);

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getListeMoisAlimAutoCongeAnnuel_OK() {

		DateTime dateMonth = new DateTime(2014, 12, 1, 0, 0, 0);
		DateTime dateMonth2 = new DateTime(2014, 11, 1, 0, 0, 0);

		CongeAnnuelAlimAutoHisto d3 = new CongeAnnuelAlimAutoHisto();
		d3.setDateMonth(dateMonth2.toDate());
		absEntityManager.persist(d3);

		CongeAnnuelAlimAutoHisto d2 = new CongeAnnuelAlimAutoHisto();
		d2.setDateMonth(dateMonth.toDate());
		absEntityManager.persist(d2);

		CongeAnnuelAlimAutoHisto d = new CongeAnnuelAlimAutoHisto();
		d.setDateMonth(dateMonth.toDate());
		absEntityManager.persist(d);

		List<Date> result = repository.getListeMoisAlimAutoCongeAnnuel();

		assertEquals(result.size(), 2);
		assertEquals(dateMonth.toDate(), result.get(0));
		assertEquals(dateMonth2.toDate(), result.get(1));

		absEntityManager.flush();
		absEntityManager.clear();
	}
}
	
	@Test 
	@Transactional("absTransactionManager")
	public void listeDemandesCongesAnnuelsPrisesByAgent_testEtat() throws ParseException {
		
		Integer idAgent = 9005138;

		RefTypeAbsence type = new RefTypeAbsence();
		absEntityManager.persist(type);

		DemandeCongesAnnuels demandeSAISIE = new DemandeCongesAnnuels();
		demandeSAISIE.setDuree(10.0);
		demandeSAISIE.setDateDebut(sdf.parse("15/06/2013"));
		demandeSAISIE.setDateFin(sdf.parse("25/06/2013"));
		demandeSAISIE.setIdAgent(idAgent);
		demandeSAISIE.setType(type);
		absEntityManager.persist(demandeSAISIE);

		EtatDemande etatDemandeSAISIE = new EtatDemande();
		etatDemandeSAISIE.setDemande(demandeSAISIE);
		etatDemandeSAISIE.setIdAgent(9000001);
		etatDemandeSAISIE.setEtat(RefEtatEnum.SAISIE);
		absEntityManager.persist(etatDemandeSAISIE);

		DemandeCongesAnnuels demandeVISEE_FAVORABLE = new DemandeCongesAnnuels();
		demandeVISEE_FAVORABLE.setDuree(10.0);
		demandeVISEE_FAVORABLE.setDateDebut(sdf.parse("15/06/2013"));
		demandeVISEE_FAVORABLE.setDateFin(sdf.parse("25/06/2013"));
		demandeVISEE_FAVORABLE.setIdAgent(idAgent);
		demandeVISEE_FAVORABLE.setType(type);
		absEntityManager.persist(demandeVISEE_FAVORABLE);

		EtatDemande etatDemandeVISEE_FAVORABLE = new EtatDemande();
		etatDemandeVISEE_FAVORABLE.setDemande(demandeVISEE_FAVORABLE);
		etatDemandeVISEE_FAVORABLE.setIdAgent(9000001);
		etatDemandeVISEE_FAVORABLE.setEtat(RefEtatEnum.VISEE_FAVORABLE);
		absEntityManager.persist(etatDemandeVISEE_FAVORABLE);

		DemandeCongesAnnuels demandeVISEE_DEFAVORABLE = new DemandeCongesAnnuels();
		demandeVISEE_DEFAVORABLE.setDuree(10.0);
		demandeVISEE_DEFAVORABLE.setDateDebut(sdf.parse("15/06/2013"));
		demandeVISEE_DEFAVORABLE.setDateFin(sdf.parse("25/06/2013"));
		demandeVISEE_DEFAVORABLE.setIdAgent(idAgent);
		demandeVISEE_DEFAVORABLE.setType(type);
		absEntityManager.persist(demandeVISEE_DEFAVORABLE);

		EtatDemande etatDemandeVISEE_DEFAVORABLE = new EtatDemande();
		etatDemandeVISEE_DEFAVORABLE.setDemande(demandeVISEE_DEFAVORABLE);
		etatDemandeVISEE_DEFAVORABLE.setIdAgent(9000001);
		etatDemandeVISEE_DEFAVORABLE.setEtat(RefEtatEnum.VISEE_DEFAVORABLE);
		absEntityManager.persist(etatDemandeVISEE_DEFAVORABLE);

		DemandeCongesAnnuels demandeAPPROUVE = new DemandeCongesAnnuels();
		demandeAPPROUVE.setDuree(10.0);
		demandeAPPROUVE.setDateDebut(sdf.parse("15/06/2013"));
		demandeAPPROUVE.setDateFin(sdf.parse("25/06/2013"));
		demandeAPPROUVE.setIdAgent(idAgent);
		demandeAPPROUVE.setType(type);
		absEntityManager.persist(demandeAPPROUVE);

		EtatDemande etatDemandeAPPROUVE = new EtatDemande();
		etatDemandeAPPROUVE.setDemande(demandeAPPROUVE);
		etatDemandeAPPROUVE.setIdAgent(9000001);
		etatDemandeAPPROUVE.setEtat(RefEtatEnum.APPROUVEE);
		absEntityManager.persist(etatDemandeAPPROUVE);

		DemandeCongesAnnuels demandeEN_ATTENTE = new DemandeCongesAnnuels();
		demandeEN_ATTENTE.setDuree(10.0);
		demandeEN_ATTENTE.setDateDebut(sdf.parse("15/06/2013"));
		demandeEN_ATTENTE.setDateFin(sdf.parse("25/06/2013"));
		demandeEN_ATTENTE.setIdAgent(idAgent);
		demandeEN_ATTENTE.setType(type);
		absEntityManager.persist(demandeEN_ATTENTE);

		EtatDemande etatDemandeEN_ATTENTE = new EtatDemande();
		etatDemandeEN_ATTENTE.setDemande(demandeEN_ATTENTE);
		etatDemandeEN_ATTENTE.setIdAgent(9000001);
		etatDemandeEN_ATTENTE.setEtat(RefEtatEnum.EN_ATTENTE);
		absEntityManager.persist(etatDemandeEN_ATTENTE);

		DemandeCongesAnnuels demandePRISE = new DemandeCongesAnnuels();
		demandePRISE.setDuree(99.0);
		demandePRISE.setDateDebut(sdf.parse("15/06/2013"));
		demandePRISE.setDateFin(sdf.parse("25/06/2013"));
		demandePRISE.setIdAgent(idAgent);
		demandePRISE.setType(type);
		absEntityManager.persist(demandePRISE);

		EtatDemande etatDemandePRISE = new EtatDemande();
		etatDemandePRISE.setDemande(demandePRISE);
		etatDemandePRISE.setIdAgent(9000001);
		etatDemandePRISE.setEtat(RefEtatEnum.PRISE);
		absEntityManager.persist(etatDemandePRISE);

		DemandeCongesAnnuels demandeVALIDEE = new DemandeCongesAnnuels();
		demandeVALIDEE.setDuree(10.0);
		demandeVALIDEE.setDateDebut(sdf.parse("15/06/2013"));
		demandeVALIDEE.setDateFin(sdf.parse("25/06/2013"));
		demandeVALIDEE.setIdAgent(idAgent);
		demandeVALIDEE.setType(type);
		absEntityManager.persist(demandeVALIDEE);

		EtatDemande etatDemandeVALIDEE = new EtatDemande();
		etatDemandeVALIDEE.setDemande(demandeVALIDEE);
		etatDemandeVALIDEE.setIdAgent(9000001);
		etatDemandeVALIDEE.setEtat(RefEtatEnum.VALIDEE);
		absEntityManager.persist(etatDemandeVALIDEE);

		DemandeCongesAnnuels demandeAVALIDEE = new DemandeCongesAnnuels();
		demandeAVALIDEE.setDuree(10.0);
		demandeAVALIDEE.setDateDebut(sdf.parse("15/06/2013"));
		demandeAVALIDEE.setDateFin(sdf.parse("25/06/2013"));
		demandeAVALIDEE.setIdAgent(idAgent);
		demandeAVALIDEE.setType(type);
		absEntityManager.persist(demandeAVALIDEE);

		EtatDemande etatDemandeAVALIDEE = new EtatDemande();
		etatDemandeAVALIDEE.setDemande(demandeVALIDEE);
		etatDemandeAVALIDEE.setIdAgent(9000001);
		etatDemandeAVALIDEE.setEtat(RefEtatEnum.A_VALIDER);
		absEntityManager.persist(etatDemandeAVALIDEE);
		
		List<DemandeCongesAnnuels> listResults = repository.getListeDemandesCongesAnnuelsPrisesByAgent(
				idAgent, sdf.parse("15/06/2013"), sdf.parse("25/06/2013"));
		
		assertEquals(1, listResults.size());
		assertEquals(listResults.get(0).getDuree(), 99,0);
	}
	
	@Test 
	@Transactional("absTransactionManager")
	public void listeDemandesCongesAnnuelsPrisesByAgent_testAgent() throws ParseException {
		
		Integer idAgent = 9005138;

		RefTypeAbsence type = new RefTypeAbsence();
		absEntityManager.persist(type);

		DemandeCongesAnnuels demandePRISE = new DemandeCongesAnnuels();
		demandePRISE.setDuree(99.0);
		demandePRISE.setDateDebut(sdf.parse("15/06/2013"));
		demandePRISE.setDateFin(sdf.parse("25/06/2013"));
		demandePRISE.setIdAgent(idAgent);
		demandePRISE.setType(type);
		absEntityManager.persist(demandePRISE);

		EtatDemande etatDemandePRISE = new EtatDemande();
		etatDemandePRISE.setDemande(demandePRISE);
		etatDemandePRISE.setIdAgent(9000001);
		etatDemandePRISE.setEtat(RefEtatEnum.PRISE);
		absEntityManager.persist(etatDemandePRISE);

		DemandeCongesAnnuels demandePRISE2 = new DemandeCongesAnnuels();
		demandePRISE2.setDuree(44.0);
		demandePRISE2.setDateDebut(sdf.parse("15/06/2013"));
		demandePRISE2.setDateFin(sdf.parse("25/06/2013"));
		demandePRISE2.setIdAgent(9002990);
		demandePRISE2.setType(type);
		absEntityManager.persist(demandePRISE2);

		EtatDemande etatDemandePRISE2 = new EtatDemande();
		etatDemandePRISE2.setDemande(demandePRISE2);
		etatDemandePRISE2.setIdAgent(9000001);
		etatDemandePRISE2.setEtat(RefEtatEnum.PRISE);
		absEntityManager.persist(etatDemandePRISE2);

		List<DemandeCongesAnnuels> listResults = repository.getListeDemandesCongesAnnuelsPrisesByAgent(
				idAgent, sdf.parse("15/06/2013"), sdf.parse("25/06/2013"));
		
		assertEquals(1, listResults.size());
		assertEquals(listResults.get(0).getDuree(), 99,0);
	}
	
	@Test 
	@Transactional("absTransactionManager")
	public void listeDemandesCongesAnnuelsPrisesByAgent_testDate() throws ParseException {
		
		Integer idAgent = 9005138;

		RefTypeAbsence type = new RefTypeAbsence();
		absEntityManager.persist(type);

		DemandeCongesAnnuels demandePRISE = new DemandeCongesAnnuels();
		demandePRISE.setDuree(99.0);
		demandePRISE.setDateDebut(sdf.parse("05/06/2013"));
		demandePRISE.setDateFin(sdf.parse("14/06/2013"));
		demandePRISE.setIdAgent(idAgent);
		demandePRISE.setType(type);
		absEntityManager.persist(demandePRISE);

		EtatDemande etatDemandePRISE = new EtatDemande();
		etatDemandePRISE.setDemande(demandePRISE);
		etatDemandePRISE.setIdAgent(9000001);
		etatDemandePRISE.setEtat(RefEtatEnum.PRISE);
		absEntityManager.persist(etatDemandePRISE);

		DemandeCongesAnnuels demandePRISE2 = new DemandeCongesAnnuels();
		demandePRISE2.setDuree(44.0);
		demandePRISE2.setDateDebut(sdf.parse("15/06/2013"));
		demandePRISE2.setDateFin(sdf.parse("25/06/2013"));
		demandePRISE2.setIdAgent(idAgent);
		demandePRISE2.setType(type);
		absEntityManager.persist(demandePRISE2);

		EtatDemande etatDemandePRISE2 = new EtatDemande();
		etatDemandePRISE2.setDemande(demandePRISE2);
		etatDemandePRISE2.setIdAgent(9000001);
		etatDemandePRISE2.setEtat(RefEtatEnum.PRISE);
		absEntityManager.persist(etatDemandePRISE2);

		DemandeCongesAnnuels demandePRISE3 = new DemandeCongesAnnuels();
		demandePRISE3.setDuree(44.0);
		demandePRISE3.setDateDebut(sdf.parse("26/06/2013"));
		demandePRISE3.setDateFin(sdf.parse("30/06/2013"));
		demandePRISE3.setIdAgent(idAgent);
		demandePRISE3.setType(type);
		absEntityManager.persist(demandePRISE3);

		EtatDemande etatDemandePRISE3 = new EtatDemande();
		etatDemandePRISE3.setDemande(demandePRISE3);
		etatDemandePRISE3.setIdAgent(9000001);
		etatDemandePRISE3.setEtat(RefEtatEnum.PRISE);
		absEntityManager.persist(etatDemandePRISE3);

		List<DemandeCongesAnnuels> listResults = repository.getListeDemandesCongesAnnuelsPrisesByAgent(
				idAgent, sdf.parse("15/06/2013"), sdf.parse("25/06/2013"));
		
		assertEquals(1, listResults.size());
		assertEquals(listResults.get(0).getDuree(), 44,0);
	}
	

	
	@Test 
	@Transactional("absTransactionManager")
	public void listeDemandesCongesAnnuelsPrisesByAgent_PeriodeRechercheeIncluseDansUnCA() throws ParseException {
		
		Integer idAgent = 9005138;

		RefTypeAbsence type = new RefTypeAbsence();
		absEntityManager.persist(type);

		DemandeCongesAnnuels demandePRISE = new DemandeCongesAnnuels();
		demandePRISE.setDuree(99.0);
		demandePRISE.setDateDebut(sdf.parse("05/06/2013"));
		demandePRISE.setDateFin(sdf.parse("14/06/2013"));
		demandePRISE.setIdAgent(idAgent);
		demandePRISE.setType(type);
		absEntityManager.persist(demandePRISE);

		EtatDemande etatDemandePRISE = new EtatDemande();
		etatDemandePRISE.setDemande(demandePRISE);
		etatDemandePRISE.setIdAgent(9000001);
		etatDemandePRISE.setEtat(RefEtatEnum.PRISE);
		absEntityManager.persist(etatDemandePRISE);

		List<DemandeCongesAnnuels> listResults = repository.getListeDemandesCongesAnnuelsPrisesByAgent(
				idAgent, new DateTime(2013,6,10,0,0,0).toDate(), new DateTime(2013,6,10,11,59,59).toDate());
		
		assertEquals(1, listResults.size());
		assertEquals(listResults.get(0).getDuree(), 99,0);
	}
	
	@Test 
	@Transactional("absTransactionManager")
	public void getRestitutionCAByAgentAndDate_1result() {
		
		CongeAnnuelRestitutionMassiveHisto histo = new CongeAnnuelRestitutionMassiveHisto();
			histo.setIdAgent(9005138);
			histo.setDateModification(new Date());
			histo.setDateRestitution(new DateTime(2015,1,23,0,0,0).toDate());
			histo.setJours(1.0);
			histo.setStatus("OK");
			histo.setMotif("motif");
			histo.setJournee(true);
			histo.setMatin(false);
			histo.setApresMidi(false);
		absEntityManager.persist(histo);
		
		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setIdAgent(9005138);
		dto.setDateRestitution(new DateTime(2015,1,23,0,0,0).toDate());
		
		List<CongeAnnuelRestitutionMassiveHisto> result = repository.getRestitutionCAByAgentAndDate(dto);
		
		assertEquals(1, result.size());
	}
	
	@Test 
	@Transactional("absTransactionManager")
	public void getRestitutionCAByAgentAndDate_badDate() {
		
		CongeAnnuelRestitutionMassiveHisto histo = new CongeAnnuelRestitutionMassiveHisto();
			histo.setIdAgent(9005138);
			histo.setDateModification(new Date());
			histo.setDateRestitution(new DateTime(2015,12,23,0,0,0).toDate());
			histo.setJours(1.0);
			histo.setStatus("OK");
			histo.setMotif("motif");
			histo.setJournee(true);
			histo.setMatin(false);
			histo.setApresMidi(false);
		absEntityManager.persist(histo);
		
		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setIdAgent(9005138);
		dto.setDateRestitution(new DateTime(2015,1,23,0,0,0).toDate());
		
		List<CongeAnnuelRestitutionMassiveHisto> result = repository.getRestitutionCAByAgentAndDate(dto);
		
		assertEquals(0, result.size());
	}
	
	@Test 
	@Transactional("absTransactionManager")
	public void getRestitutionCAByAgentAndDate_badAgent() {
		
		CongeAnnuelRestitutionMassiveHisto histo = new CongeAnnuelRestitutionMassiveHisto();
			histo.setIdAgent(9009999);
			histo.setDateModification(new Date());
			histo.setDateRestitution(new DateTime(2015,1,23,0,0,0).toDate());
			histo.setJours(1.0);
			histo.setStatus("OK");
			histo.setMotif("motif");
			histo.setJournee(true);
			histo.setMatin(false);
			histo.setApresMidi(false);
		absEntityManager.persist(histo);
		
		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setIdAgent(9005138);
		dto.setDateRestitution(new DateTime(2015,1,23,0,0,0).toDate());
		
		List<CongeAnnuelRestitutionMassiveHisto> result = repository.getRestitutionCAByAgentAndDate(dto);
		
		assertEquals(0, result.size());
	}
	
	@Test 
	@Transactional("absTransactionManager")
	public void getRestitutionCAByAgentAndDate_badStatus() {
		
		CongeAnnuelRestitutionMassiveHisto histo = new CongeAnnuelRestitutionMassiveHisto();
			histo.setIdAgent(9005138);
			histo.setDateModification(new Date());
			histo.setDateRestitution(new DateTime(2015,1,23,0,0,0).toDate());
			histo.setJours(1.0);
			histo.setStatus("error");
			histo.setMotif("motif");
			histo.setJournee(true);
			histo.setMatin(false);
			histo.setApresMidi(false);
		absEntityManager.persist(histo);
		
		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setIdAgent(9005138);
		dto.setDateRestitution(new DateTime(2015,1,23,0,0,0).toDate());
		
		List<CongeAnnuelRestitutionMassiveHisto> result = repository.getRestitutionCAByAgentAndDate(dto);
		
		assertEquals(0, result.size());
	}
}
