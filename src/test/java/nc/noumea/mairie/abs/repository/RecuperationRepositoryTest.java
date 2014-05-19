package nc.noumea.mairie.abs.repository;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.recup.domain.DemandeRecup;
import nc.noumea.mairie.abs.recup.repository.RecuperationRepository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/META-INF/spring/applicationContext-test.xml"})
public class RecuperationRepositoryTest {

	@Autowired
	RecuperationRepository repository;
	
	@PersistenceContext(unitName = "absPersistenceUnit")
	EntityManager absEntityManager;
	
	@Test
	@Transactional("absTransactionManager")
	public void getSommeDureeDemandeRecupEnCoursSaisieouVisee_returnViseeF() {
		
		Date dateJour = new Date();
		DemandeRecup dr1 = new DemandeRecup();
		List<EtatDemande> listEtatDemande = new ArrayList<EtatDemande>();
		EtatDemande et = new EtatDemande();
		et.setDate(dateJour);
		et.setEtat(RefEtatEnum.VISEE_FAVORABLE); 
		et.setIdAgent(9005168); 
		et.setDemande(dr1);
		listEtatDemande.add(et);
		
		RefTypeAbsence rta = new RefTypeAbsence();
		rta.setIdRefTypeAbsence(3);
		absEntityManager.persist(rta);
		// Given
		
		dr1.setDateDebut(dateJour);
		dr1.setDateFin(dateJour);
		dr1.setDuree(10);
		dr1.setIdAgent(9005168);
		dr1.setType(rta);
		dr1.setEtatsDemande(listEtatDemande);
		absEntityManager.persist(dr1);
		
		DemandeRecup dr2 = new DemandeRecup();
		List<EtatDemande> listEtatDemande2 = new ArrayList<EtatDemande>();
		EtatDemande et2 = new EtatDemande();
		et2.setDate(dateJour);
		et2.setEtat(RefEtatEnum.VISEE_FAVORABLE); 
		et2.setIdAgent(9005168); 
		et2.setDemande(dr2);
		listEtatDemande2.add(et2);
		
		dr2.setDateDebut(dateJour);
		dr2.setDateFin(dateJour);
		dr2.setDuree(15);
		dr2.setIdAgent(9005168);
		dr2.setType(rta);
		dr2.setEtatsDemande(listEtatDemande2);
		absEntityManager.persist(dr2);
		
		// When
		Integer result = repository.getSommeDureeDemandeRecupEnCoursSaisieouVisee(9005168, null);
		
		// Then
		assertEquals(result.intValue(), 25);
	}
	
	@Test
	@Transactional("absTransactionManager")
	public void getSommeDureeDemandeRecupEnCoursSaisieouVisee_returnViseeD() {
		
		Date dateJour = new Date();
		DemandeRecup dr1 = new DemandeRecup();
		List<EtatDemande> listEtatDemande = new ArrayList<EtatDemande>();
		EtatDemande et = new EtatDemande();
		et.setDate(dateJour);
		et.setEtat(RefEtatEnum.VISEE_DEFAVORABLE); 
		et.setIdAgent(9005168); 
		et.setDemande(dr1);
		listEtatDemande.add(et);
		
		RefTypeAbsence rta = new RefTypeAbsence();
		rta.setIdRefTypeAbsence(3);
		absEntityManager.persist(rta);
		// Given
		
		dr1.setDateDebut(dateJour);
		dr1.setDateFin(dateJour);
		dr1.setDuree(10);
		dr1.setIdAgent(9005168);
		dr1.setType(rta);
		dr1.setEtatsDemande(listEtatDemande);
		absEntityManager.persist(dr1);
		
		DemandeRecup dr2 = new DemandeRecup();
		List<EtatDemande> listEtatDemande2 = new ArrayList<EtatDemande>();
		EtatDemande et2 = new EtatDemande();
		et2.setDate(dateJour);
		et2.setEtat(RefEtatEnum.VISEE_DEFAVORABLE); 
		et2.setIdAgent(9005168); 
		et2.setDemande(dr2);
		listEtatDemande2.add(et2);
		
		dr2.setDateDebut(dateJour);
		dr2.setDateFin(dateJour);
		dr2.setDuree(15);
		dr2.setIdAgent(9005168);
		dr2.setType(rta);
		dr2.setEtatsDemande(listEtatDemande2);
		absEntityManager.persist(dr2);
		
		// When
		Integer result = repository.getSommeDureeDemandeRecupEnCoursSaisieouVisee(9005168, null);
		
		// Then
		assertEquals(result.intValue(), 25);
	}
	
	@Test
	@Transactional("absTransactionManager")
	public void getSommeDureeDemandeRecupEnCoursSaisieouVisee_returnSaisie() {
		
		Date dateJour = new Date();
		DemandeRecup dr1 = new DemandeRecup();
		List<EtatDemande> listEtatDemande = new ArrayList<EtatDemande>();
		EtatDemande et = new EtatDemande();
		et.setDate(dateJour);
		et.setEtat(RefEtatEnum.SAISIE); 
		et.setIdAgent(9005168); 
		et.setDemande(dr1);
		listEtatDemande.add(et);
		
		RefTypeAbsence rta = new RefTypeAbsence();
		rta.setIdRefTypeAbsence(3);
		absEntityManager.persist(rta);
		// Given
		
		dr1.setDateDebut(dateJour);
		dr1.setDateFin(dateJour);
		dr1.setDuree(10);
		dr1.setIdAgent(9005168);
		dr1.setType(rta);
		dr1.setEtatsDemande(listEtatDemande);
		absEntityManager.persist(dr1);
		
		DemandeRecup dr2 = new DemandeRecup();
		List<EtatDemande> listEtatDemande2 = new ArrayList<EtatDemande>();
		EtatDemande et2 = new EtatDemande();
		et2.setDate(dateJour);
		et2.setEtat(RefEtatEnum.SAISIE); 
		et2.setIdAgent(9005168); 
		et2.setDemande(dr2);
		listEtatDemande2.add(et2);
		
		dr2.setDateDebut(dateJour);
		dr2.setDateFin(dateJour);
		dr2.setDuree(15);
		dr2.setIdAgent(9005168);
		dr2.setType(rta);
		dr2.setEtatsDemande(listEtatDemande2);
		absEntityManager.persist(dr2);
		
		// When
		Integer result = repository.getSommeDureeDemandeRecupEnCoursSaisieouVisee(9005168, null);
		
		// Then
		assertEquals(result.intValue(), 25);
	}
	
	@Test
	@Transactional("absTransactionManager")
	public void getSommeDureeDemandeRecupEnCoursSaisieouVisee_returnSaisieEtVisee() {
		
		Date dateJour = new Date();
		DemandeRecup dr1 = new DemandeRecup();
		List<EtatDemande> listEtatDemande = new ArrayList<EtatDemande>();
		EtatDemande et = new EtatDemande();
		et.setDate(dateJour);
		et.setEtat(RefEtatEnum.SAISIE); 
		et.setIdAgent(9005168); 
		et.setDemande(dr1);
		listEtatDemande.add(et);
		
		RefTypeAbsence rta = new RefTypeAbsence();
		rta.setIdRefTypeAbsence(3);
		absEntityManager.persist(rta);
		// Given
		
		dr1.setDateDebut(dateJour);
		dr1.setDateFin(dateJour);
		dr1.setDuree(10);
		dr1.setIdAgent(9005168);
		dr1.setType(rta);
		dr1.setEtatsDemande(listEtatDemande);
		absEntityManager.persist(dr1);
		
		DemandeRecup dr2 = new DemandeRecup();
		List<EtatDemande> listEtatDemande2 = new ArrayList<EtatDemande>();
		EtatDemande et2 = new EtatDemande();
		et2.setDate(dateJour);
		et2.setEtat(RefEtatEnum.VISEE_DEFAVORABLE); 
		et2.setIdAgent(9005168); 
		et2.setDemande(dr2);
		listEtatDemande2.add(et2);
		
		dr2.setDateDebut(dateJour);
		dr2.setDateFin(dateJour);
		dr2.setDuree(15);
		dr2.setIdAgent(9005168);
		dr2.setType(rta);
		dr2.setEtatsDemande(listEtatDemande2);
		absEntityManager.persist(dr2);
		
		DemandeRecup dr3 = new DemandeRecup();
		List<EtatDemande> listEtatDemande3 = new ArrayList<EtatDemande>();
		EtatDemande et3 = new EtatDemande();
		et3.setDate(dateJour);
		et3.setEtat(RefEtatEnum.VISEE_FAVORABLE); 
		et3.setIdAgent(9005168); 
		et3.setDemande(dr3);
		listEtatDemande3.add(et3);
		
		dr3.setDateDebut(dateJour);
		dr3.setDateFin(dateJour);
		dr3.setDuree(20);
		dr3.setIdAgent(9005168);
		dr3.setType(rta);
		dr3.setEtatsDemande(listEtatDemande3);
		absEntityManager.persist(dr3);
		
		// When
		Integer result = repository.getSommeDureeDemandeRecupEnCoursSaisieouVisee(9005168, null);
		
		// Then
		assertEquals(result.intValue(), 45);
	}
	
	@Test
	@Transactional("absTransactionManager")
	public void getSommeDureeDemandeRecupEnCoursSaisieouVisee_returnZero() {
		
		Date dateJour = new Date();
		DemandeRecup dr1 = new DemandeRecup();
		List<EtatDemande> listEtatDemande = new ArrayList<EtatDemande>();
		EtatDemande et = new EtatDemande();
		et.setDate(dateJour);
		et.setEtat(RefEtatEnum.PROVISOIRE); 
		et.setIdAgent(9005168); 
		et.setDemande(dr1);
		listEtatDemande.add(et);
		
		RefTypeAbsence rta = new RefTypeAbsence();
		rta.setIdRefTypeAbsence(3);
		absEntityManager.persist(rta);
		// Given
		
		dr1.setDateDebut(dateJour);
		dr1.setDateFin(dateJour);
		dr1.setDuree(10);
		dr1.setIdAgent(9005168);
		dr1.setType(rta);
		dr1.setEtatsDemande(listEtatDemande);
		absEntityManager.persist(dr1);
		
		DemandeRecup dr2 = new DemandeRecup();
		List<EtatDemande> listEtatDemande2 = new ArrayList<EtatDemande>();
		EtatDemande et2 = new EtatDemande();
		et2.setDate(dateJour);
		et2.setEtat(RefEtatEnum.PRISE); 
		et2.setIdAgent(9005168); 
		et2.setDemande(dr2);
		listEtatDemande2.add(et2);
		
		dr2.setDateDebut(dateJour);
		dr2.setDateFin(dateJour);
		dr2.setDuree(15);
		dr2.setIdAgent(9005168);
		dr2.setType(rta);
		dr2.setEtatsDemande(listEtatDemande2);
		absEntityManager.persist(dr2);
		
		DemandeRecup dr3 = new DemandeRecup();
		List<EtatDemande> listEtatDemande3 = new ArrayList<EtatDemande>();
		EtatDemande et3 = new EtatDemande();
		et3.setDate(dateJour);
		et3.setEtat(RefEtatEnum.APPROUVEE); 
		et3.setIdAgent(9005168); 
		et3.setDemande(dr3);
		listEtatDemande3.add(et3);
		
		dr3.setDateDebut(dateJour);
		dr3.setDateFin(dateJour);
		dr3.setDuree(20);
		dr3.setIdAgent(9005168);
		dr3.setType(rta);
		dr3.setEtatsDemande(listEtatDemande3);
		absEntityManager.persist(dr3);
		
		DemandeRecup dr4 = new DemandeRecup();
		List<EtatDemande> listEtatDemande4 = new ArrayList<EtatDemande>();
		EtatDemande et4 = new EtatDemande();
		et4.setDate(dateJour);
		et4.setEtat(RefEtatEnum.REFUSEE); 
		et4.setIdAgent(9005168); 
		et4.setDemande(dr4);
		listEtatDemande3.add(et4);
		
		dr4.setDateDebut(dateJour);
		dr4.setDateFin(dateJour);
		dr4.setDuree(20);
		dr4.setIdAgent(9005168);
		dr4.setType(rta);
		dr4.setEtatsDemande(listEtatDemande4);
		absEntityManager.persist(dr4);
		
		// When
		Integer result = repository.getSommeDureeDemandeRecupEnCoursSaisieouVisee(9005168, null);
		
		// Then
		assertEquals(result.intValue(), 0);
	}
	
	@Test
	@Transactional("absTransactionManager")
	public void getSommeDureeDemandeRecupEnCoursSaisieouVisee_returnResultWithMultiEtats() {
		
		Date dateJour = new Date();
		
		DemandeRecup dr1 = new DemandeRecup();
		List<EtatDemande> listEtatDemande = new ArrayList<EtatDemande>();
		
		EtatDemande et = new EtatDemande();
		et.setDate(dateJour);
		et.setEtat(RefEtatEnum.PROVISOIRE); 
		et.setIdAgent(9005168); 
		et.setDemande(dr1);
		
		EtatDemande et2 = new EtatDemande();
		et2.setDate(dateJour);
		et2.setEtat(RefEtatEnum.APPROUVEE); 
		et2.setIdAgent(9005168); 
		et2.setDemande(dr1);
		
		EtatDemande et3 = new EtatDemande();
		et3.setDate(dateJour);
		et3.setEtat(RefEtatEnum.VISEE_FAVORABLE); 
		et3.setIdAgent(9005168); 
		et3.setDemande(dr1);
		
		listEtatDemande.addAll(Arrays.asList(et, et2, et3));
		
		RefTypeAbsence rta = new RefTypeAbsence();
		rta.setIdRefTypeAbsence(3);
		absEntityManager.persist(rta);
		// Given
		
		dr1.setDateDebut(dateJour);
		dr1.setDateFin(dateJour);
		dr1.setDuree(10);
		dr1.setIdAgent(9005168);
		dr1.setType(rta);
		dr1.setEtatsDemande(listEtatDemande);
		absEntityManager.persist(dr1);
		
		// When
		Integer result = repository.getSommeDureeDemandeRecupEnCoursSaisieouVisee(9005168, null);
		
		// Then
		assertEquals(result.intValue(), 10);
	}
	
	@Test
	@Transactional("absTransactionManager")
	public void getSommeDureeDemandeRecupEnCoursSaisieouVisee_returnNoResultWithMultiEtats() {
		
		Date dateJour = new Date();
		
		DemandeRecup dr1 = new DemandeRecup();
		List<EtatDemande> listEtatDemande = new ArrayList<EtatDemande>();
		
		EtatDemande et = new EtatDemande();
		et.setDate(dateJour);
		et.setEtat(RefEtatEnum.PROVISOIRE); 
		et.setIdAgent(9005168); 
		et.setDemande(dr1);
		
		EtatDemande et2 = new EtatDemande();
		et2.setDate(dateJour);
		et2.setEtat(RefEtatEnum.VISEE_FAVORABLE); 
		et2.setIdAgent(9005168); 
		et2.setDemande(dr1);
		
		EtatDemande et3 = new EtatDemande();
		et3.setDate(dateJour);
		et3.setEtat(RefEtatEnum.APPROUVEE); 
		et3.setIdAgent(9005168); 
		et3.setDemande(dr1);
		
		listEtatDemande.addAll(Arrays.asList(et, et2, et3));
		
		RefTypeAbsence rta = new RefTypeAbsence();
		rta.setIdRefTypeAbsence(3);
		absEntityManager.persist(rta);
		// Given
		
		dr1.setDateDebut(dateJour);
		dr1.setDateFin(dateJour);
		dr1.setDuree(10);
		dr1.setIdAgent(9005168);
		dr1.setType(rta);
		dr1.setEtatsDemande(listEtatDemande);
		absEntityManager.persist(dr1);
		
		// When
		Integer result = repository.getSommeDureeDemandeRecupEnCoursSaisieouVisee(9005168, null);
		
		// Then
		assertEquals(result.intValue(), 0);
	}
	
	@Test
	@Transactional("absTransactionManager")
	public void getSommeDureeDemandeRecupEnCoursSaisieouVisee_WithSameIdDemande() {
		
		Date dateJour = new Date();
		
		DemandeRecup dr1 = new DemandeRecup();
		List<EtatDemande> listEtatDemande = new ArrayList<EtatDemande>();
		
		EtatDemande et2 = new EtatDemande();
		et2.setDate(dateJour);
		et2.setEtat(RefEtatEnum.VISEE_FAVORABLE); 
		et2.setIdAgent(9005168); 
		et2.setDemande(dr1);
		
		listEtatDemande.addAll(Arrays.asList(et2));
		
		RefTypeAbsence rta = new RefTypeAbsence();
		rta.setIdRefTypeAbsence(3);
		absEntityManager.persist(rta);
		// Given
		
		dr1.setDateDebut(dateJour);
		dr1.setDateFin(dateJour);
		dr1.setDuree(10);
		dr1.setIdAgent(9005168);
		dr1.setType(rta);
		dr1.setEtatsDemande(listEtatDemande);
		absEntityManager.persist(dr1);
		
		// When
		Integer noResult = repository.getSommeDureeDemandeRecupEnCoursSaisieouVisee(9005168, dr1.getIdDemande());
		
		// When
		Integer result = repository.getSommeDureeDemandeRecupEnCoursSaisieouVisee(9005168, 10);
		
		// Then
		assertEquals(result.intValue(), 10);
		assertEquals(noResult.intValue(), 0);
	}
}
