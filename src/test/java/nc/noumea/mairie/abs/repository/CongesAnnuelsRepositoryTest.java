package nc.noumea.mairie.abs.repository;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nc.noumea.mairie.abs.domain.DemandeCongesAnnuels;
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
}
