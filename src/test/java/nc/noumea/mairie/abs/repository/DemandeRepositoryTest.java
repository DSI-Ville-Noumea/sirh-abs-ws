package nc.noumea.mairie.abs.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/META-INF/spring/applicationContext-test.xml"})
public class DemandeRepositoryTest {

	@Autowired
	DemandeRepository repository;
	
	@PersistenceContext(unitName = "absPersistenceUnit")
	private EntityManager absEntityManager;
	
	@Test
	@Transactional("absTransactionManager")
	public void getLastEtatDemandeByIdDemande_withResult() {
		
		Date dateJour = new Date();
		
		Demande d1 = new Demande();
		List<EtatDemande> listEtatDemande = new ArrayList<EtatDemande>();
		EtatDemande et = new EtatDemande();
		et.setDate(dateJour);
		et.setEtat(RefEtatEnum.SAISIE); 
		et.setIdAgent(9005168); 
		et.setDemande(d1);
		
		EtatDemande et2 = new EtatDemande();
		et2.setDate(dateJour);
		et2.setEtat(RefEtatEnum.VISEE_FAVORABLE); 
		et2.setIdAgent(9005168); 
		et2.setDemande(d1);
		
		EtatDemande et3 = new EtatDemande();
		et3.setDate(dateJour);
		et3.setEtat(RefEtatEnum.APPROUVEE);
		et3.setIdAgent(9005168); 
		et3.setDemande(d1);
		listEtatDemande.addAll(Arrays.asList(et, et2, et3));
		
		RefTypeAbsence rta = new RefTypeAbsence();
		rta.setIdRefTypeAbsence(3);
		repository.persistEntity(rta);
		// Given
		
		d1.setDateDebut(dateJour);
		d1.setDateFin(dateJour);
		d1.setIdAgent(9005168);
		d1.setType(rta);
		d1.setEtatsDemande(listEtatDemande);
		repository.persistEntity(d1);
		
		EtatDemande result = repository.getLastEtatDemandeByIdDemande(d1.getIdDemande());
		
		assertNotNull(result);
		assertEquals(result.getEtat().getCodeEtat(), RefEtatEnum.APPROUVEE.getCodeEtat());
	}
	
	@Test
	@Transactional("absTransactionManager")
	public void getLastEtatDemandeByIdDemande_withNoResult() {
		
		Date dateJour = new Date();
		
		Demande d1 = new Demande();
		List<EtatDemande> listEtatDemande = new ArrayList<EtatDemande>();
		EtatDemande et = new EtatDemande();
		et.setDate(dateJour);
		et.setEtat(RefEtatEnum.SAISIE); 
		et.setIdAgent(9005168); 
		et.setDemande(d1);
		
		EtatDemande et2 = new EtatDemande();
		et2.setDate(dateJour);
		et2.setEtat(RefEtatEnum.VISEE_FAVORABLE); 
		et2.setIdAgent(9005168); 
		et2.setDemande(d1);
		
		EtatDemande et3 = new EtatDemande();
		et3.setDate(dateJour);
		et3.setEtat(RefEtatEnum.APPROUVEE);
		et3.setIdAgent(9005168); 
		et3.setDemande(d1);
		listEtatDemande.addAll(Arrays.asList(et, et2, et3));
		
		RefTypeAbsence rta = new RefTypeAbsence();
		rta.setIdRefTypeAbsence(3);
		repository.persistEntity(rta);
		// Given
		
		d1.setDateDebut(dateJour);
		d1.setDateFin(dateJour);
		d1.setIdAgent(9005168);
		d1.setType(rta);
		d1.setEtatsDemande(listEtatDemande);
		repository.persistEntity(d1);
		
		EtatDemande result = repository.getLastEtatDemandeByIdDemande(12);
		
		assertNull(result);
	}
}
