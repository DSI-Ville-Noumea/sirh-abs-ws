package nc.noumea.mairie.abs.repository;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nc.noumea.mairie.abs.domain.DemandeAsa;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.OrganisationSyndicale;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/META-INF/spring/applicationContext-test.xml" })
public class AsaRepositoryTest {

	@Autowired
	AsaRepository repository;

	@PersistenceContext(unitName = "absPersistenceUnit")
	EntityManager absEntityManager;

	@Test
	@Transactional("absTransactionManager")
	public void getListDemandeAsaEnCours_testEtatDemande() {

		Date dateJour = new DateTime(2015, 05, 1, 0, 0, 0).toDate();
		Date dateDebMois = new DateTime(2014, 05, 1, 0, 0, 0).toDate();
		Date dateFinMois = new DateTime(2016, 05, 31, 23, 59, 59).toDate();

		DemandeAsa dr1 = new DemandeAsa();
		List<EtatDemande> listEtatDemande = new ArrayList<EtatDemande>();

		EtatDemande et = new EtatDemande();
		et.setDate(dateJour);
		et.setEtat(RefEtatEnum.SAISIE);
		et.setIdAgent(9005168);
		et.setDemande(dr1);
		listEtatDemande.add(et);

		RefTypeAbsence rta = new RefTypeAbsence();
		absEntityManager.persist(rta);
		// Given

		dr1.setDateDebut(dateJour);
		dr1.setDateFin(dateJour);
		dr1.setDuree(10.0);
		dr1.setIdAgent(9005168);
		dr1.setType(rta);
		dr1.setEtatsDemande(listEtatDemande);
		absEntityManager.persist(dr1);

		DemandeAsa dr2 = new DemandeAsa();
		List<EtatDemande> listEtatDemande2 = new ArrayList<EtatDemande>();
		EtatDemande et2 = new EtatDemande();
		et2.setDate(dateJour);
		et2.setEtat(RefEtatEnum.VISEE_FAVORABLE);
		et2.setIdAgent(9005168);
		et2.setDemande(dr2);
		listEtatDemande2.add(et2);

		dr2.setDateDebut(dateJour);
		dr2.setDateFin(dateJour);
		dr2.setDuree(15.0);
		dr2.setIdAgent(9005168);
		dr2.setType(rta);
		dr2.setEtatsDemande(listEtatDemande2);
		absEntityManager.persist(dr2);

		DemandeAsa dr3 = new DemandeAsa();
		List<EtatDemande> listEtatDemande3 = new ArrayList<EtatDemande>();
		EtatDemande et3 = new EtatDemande();
		et3.setDate(dateJour);
		et3.setEtat(RefEtatEnum.VISEE_DEFAVORABLE);
		et3.setIdAgent(9005168);
		et3.setDemande(dr3);
		listEtatDemande3.add(et3);

		dr3.setDateDebut(dateJour);
		dr3.setDateFin(dateJour);
		dr3.setDuree(15.5);
		dr3.setIdAgent(9005168);
		dr3.setType(rta);
		dr3.setEtatsDemande(listEtatDemande3);
		absEntityManager.persist(dr3);

		DemandeAsa dr4 = new DemandeAsa();
		List<EtatDemande> listEtatDemande4 = new ArrayList<EtatDemande>();
		EtatDemande et4 = new EtatDemande();
		et4.setDate(dateJour);
		et4.setEtat(RefEtatEnum.APPROUVEE);
		et4.setIdAgent(9005168);
		et4.setDemande(dr4);
		listEtatDemande4.add(et4);

		dr4.setDateDebut(dateJour);
		dr4.setDateFin(dateJour);
		dr4.setDuree(15.5);
		dr4.setIdAgent(9005168);
		dr4.setType(rta);
		dr4.setEtatsDemande(listEtatDemande4);
		absEntityManager.persist(dr4);

		DemandeAsa dr5 = new DemandeAsa();
		List<EtatDemande> listEtatDemande5 = new ArrayList<EtatDemande>();
		EtatDemande et5 = new EtatDemande();
		et5.setDate(dateJour);
		et5.setEtat(RefEtatEnum.EN_ATTENTE);
		et5.setIdAgent(9005168);
		et5.setDemande(dr5);
		listEtatDemande2.add(et5);

		dr5.setDateDebut(dateJour);
		dr5.setDateFin(dateJour);
		dr5.setDuree(15.0);
		dr5.setIdAgent(9005168);
		dr5.setType(rta);
		dr5.setEtatsDemande(listEtatDemande5);
		absEntityManager.persist(dr5);

		DemandeAsa dr6 = new DemandeAsa();
		List<EtatDemande> listEtatDemande6 = new ArrayList<EtatDemande>();
		EtatDemande et6 = new EtatDemande();
		et6.setDate(dateJour);
		et6.setEtat(RefEtatEnum.REJETE);
		et6.setIdAgent(9005168);
		et6.setDemande(dr6);
		listEtatDemande6.add(et6);

		dr6.setDateDebut(dateJour);
		dr6.setDateFin(dateJour);
		dr6.setDuree(15.5);
		dr6.setIdAgent(9005168);
		dr6.setType(rta);
		dr6.setEtatsDemande(listEtatDemande6);
		absEntityManager.persist(dr6);

		DemandeAsa dr7 = new DemandeAsa();
		List<EtatDemande> listEtatDemande7 = new ArrayList<EtatDemande>();
		EtatDemande et7 = new EtatDemande();
		et7.setDate(dateJour);
		et7.setEtat(RefEtatEnum.REFUSEE);
		et7.setIdAgent(9005168);
		et7.setDemande(dr7);
		listEtatDemande7.add(et7);

		dr7.setDateDebut(dateJour);
		dr7.setDateFin(dateJour);
		dr7.setDuree(15.0);
		dr7.setIdAgent(9005168);
		dr7.setType(rta);
		dr7.setEtatsDemande(listEtatDemande5);
		absEntityManager.persist(dr7);

		DemandeAsa dr8 = new DemandeAsa();
		List<EtatDemande> listEtatDemande8 = new ArrayList<EtatDemande>();
		EtatDemande et8 = new EtatDemande();
		et8.setDate(dateJour);
		et8.setEtat(RefEtatEnum.PROVISOIRE);
		et8.setIdAgent(9005168);
		et8.setDemande(dr8);
		listEtatDemande8.add(et8);

		dr8.setDateDebut(dateJour);
		dr8.setDateFin(dateJour);
		dr8.setDuree(15.5);
		dr8.setIdAgent(9005168);
		dr8.setType(rta);
		dr8.setEtatsDemande(listEtatDemande8);
		absEntityManager.persist(dr8);

		DemandeAsa dr9 = new DemandeAsa();
		List<EtatDemande> listEtatDemande9 = new ArrayList<EtatDemande>();
		EtatDemande et9 = new EtatDemande();
		et9.setDate(dateJour);
		et9.setEtat(RefEtatEnum.PRISE);
		et9.setIdAgent(9005168);
		et9.setDemande(dr9);
		listEtatDemande9.add(et9);

		dr9.setDateDebut(dateJour);
		dr9.setDateFin(dateJour);
		dr9.setDuree(15.0);
		dr9.setIdAgent(9005168);
		dr9.setType(rta);
		dr9.setEtatsDemande(listEtatDemande9);
		absEntityManager.persist(dr9);

		DemandeAsa dr10 = new DemandeAsa();
		List<EtatDemande> listEtatDemande10 = new ArrayList<EtatDemande>();
		EtatDemande et10 = new EtatDemande();
		et10.setDate(dateJour);
		et10.setEtat(RefEtatEnum.VALIDEE);
		et10.setIdAgent(9005168);
		et10.setDemande(dr10);
		listEtatDemande8.add(et10);

		dr10.setDateDebut(dateJour);
		dr10.setDateFin(dateJour);
		dr10.setDuree(15.0);
		dr10.setIdAgent(9005168);
		dr10.setType(rta);
		dr10.setEtatsDemande(listEtatDemande10);
		absEntityManager.persist(dr10);

		DemandeAsa dr11 = new DemandeAsa();
		List<EtatDemande> listEtatDemande11 = new ArrayList<EtatDemande>();
		EtatDemande et11 = new EtatDemande();
		et11.setDate(dateJour);
		et11.setEtat(RefEtatEnum.ANNULEE);
		et11.setIdAgent(9005168);
		et11.setDemande(dr11);
		listEtatDemande8.add(et11);

		dr11.setDateDebut(dateJour);
		dr11.setDateFin(dateJour);
		dr11.setDuree(15.5);
		dr11.setIdAgent(9005168);
		dr11.setType(rta);
		dr11.setEtatsDemande(listEtatDemande11);
		absEntityManager.persist(dr11);

		// When
		List<DemandeAsa> result = repository.getListDemandeAsaEnCours(9005168, null, dateDebMois, dateFinMois, rta.getIdRefTypeAbsence());

		assertEquals(5, result.size());
		
		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getListDemandeAsaEnCours_noResult_sameDemande() {

		Date dateJour = new DateTime(2014, 05, 1, 0, 0, 0).toDate();
		Date dateDebMois = new DateTime(2014, 05, 1, 0, 0, 0).toDate();
		Date dateFinMois = new DateTime(2014, 05, 31, 23, 59, 59).toDate();

		DemandeAsa dr1 = new DemandeAsa();
		List<EtatDemande> listEtatDemande = new ArrayList<EtatDemande>();

		EtatDemande et = new EtatDemande();
		et.setDate(dateJour);
		et.setEtat(RefEtatEnum.SAISIE);
		et.setIdAgent(9005168);
		et.setDemande(dr1);
		listEtatDemande.add(et);

		RefTypeAbsence rta = new RefTypeAbsence();
		absEntityManager.persist(rta);
		// Given

		dr1.setDateDebut(dateJour);
		dr1.setDateFin(dateJour);
		dr1.setDuree(10.0);
		dr1.setIdAgent(9005168);
		dr1.setType(rta);
		dr1.setEtatsDemande(listEtatDemande);
		absEntityManager.persist(dr1);

		// When
		List<DemandeAsa> result = repository.getListDemandeAsaEnCours(9005168, dr1.getIdDemande(), dateDebMois,dateFinMois,
				rta.getIdRefTypeAbsence());

		assertEquals(0, result.size());
		
		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getListDemandeAsaEnCours_testTypeDemande() {
		
		Date dateJour = new DateTime(2015, 05, 1, 0, 0, 0).toDate();
		Date dateDebMois = new DateTime(2014, 05, 1, 0, 0, 0).toDate();
		Date dateFinMois = new DateTime(2016, 05, 31, 23, 59, 59).toDate();

		DemandeAsa dr1 = new DemandeAsa();
		List<EtatDemande> listEtatDemande = new ArrayList<EtatDemande>();

		EtatDemande et = new EtatDemande();
		et.setDate(dateJour);
		et.setEtat(RefEtatEnum.SAISIE);
		et.setIdAgent(9005168);
		et.setDemande(dr1);
		listEtatDemande.add(et);

		RefTypeAbsence rta = new RefTypeAbsence();
		absEntityManager.persist(rta);
		// Given

		dr1.setDateDebut(dateJour);
		dr1.setDateFin(dateJour);
		dr1.setDuree(10.0);
		dr1.setIdAgent(9005168);
		dr1.setType(rta);
		dr1.setEtatsDemande(listEtatDemande);
		absEntityManager.persist(dr1);

		DemandeAsa dr2 = new DemandeAsa();
		List<EtatDemande> listEtatDemande2 = new ArrayList<EtatDemande>();

		EtatDemande et2 = new EtatDemande();
		et2.setDate(dateJour);
		et2.setEtat(RefEtatEnum.SAISIE);
		et2.setIdAgent(9005168);
		et2.setDemande(dr2);
		listEtatDemande2.add(et2);

		RefTypeAbsence rta2 = new RefTypeAbsence();
		absEntityManager.persist(rta2);
		// Given

		dr2.setDateDebut(dateJour);
		dr2.setDateFin(dateJour);
		dr2.setDuree(10.0);
		dr2.setIdAgent(9005168);
		dr2.setType(rta2);
		dr2.setEtatsDemande(listEtatDemande2);
		absEntityManager.persist(dr2);

		DemandeAsa dr3 = new DemandeAsa();
		List<EtatDemande> listEtatDemande3 = new ArrayList<EtatDemande>();

		EtatDemande et3 = new EtatDemande();
		et3.setDate(dateJour);
		et3.setEtat(RefEtatEnum.SAISIE);
		et3.setIdAgent(9005168);
		et3.setDemande(dr3);
		listEtatDemande3.add(et3);

		RefTypeAbsence rta3 = new RefTypeAbsence();
		absEntityManager.persist(rta3);
		// Given

		dr3.setDateDebut(dateJour);
		dr3.setDateFin(dateJour);
		dr3.setDuree(10.0);
		dr3.setIdAgent(9005168);
		dr3.setType(rta3);
		dr3.setEtatsDemande(listEtatDemande3);
		absEntityManager.persist(dr3);

		DemandeAsa dr4 = new DemandeAsa();
		List<EtatDemande> listEtatDemande4 = new ArrayList<EtatDemande>();

		EtatDemande et4 = new EtatDemande();
		et4.setDate(dateJour);
		et4.setEtat(RefEtatEnum.SAISIE);
		et4.setIdAgent(9005168);
		et4.setDemande(dr1);
		listEtatDemande4.add(et4);

		RefTypeAbsence rta4 = new RefTypeAbsence();
		absEntityManager.persist(rta4);
		// Given

		dr4.setDateDebut(dateJour);
		dr4.setDateFin(dateJour);
		dr4.setDuree(10.0);
		dr4.setIdAgent(9005168);
		dr4.setType(rta4);
		dr4.setEtatsDemande(listEtatDemande4);
		absEntityManager.persist(dr4);

		// When
		List<DemandeAsa> result_ASA_A48 = repository.getListDemandeAsaEnCours(9005168, null, dateDebMois, dateFinMois, rta.getIdRefTypeAbsence());
		assertEquals(1, result_ASA_A48.size());

		List<DemandeAsa> result_ASA_A54 = repository
				.getListDemandeAsaEnCours(9005168, null, dateDebMois, dateFinMois, rta2.getIdRefTypeAbsence());
		assertEquals(1, result_ASA_A54.size());

		List<DemandeAsa> result_ASA_A55 = repository
				.getListDemandeAsaEnCours(9005168, null, dateDebMois, dateFinMois, rta3.getIdRefTypeAbsence());
		assertEquals(1, result_ASA_A55.size());
		
		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getListDemandeAsaPourMoisByAgent_testEtatDemande() {

		Date dateJour = new DateTime(2014, 05, 13, 12, 30, 0).toDate();
		Date dateDebMois = new DateTime(2014, 05, 1, 0, 0, 0).toDate();
		Date dateFinMois = new DateTime(2014, 05, 31, 23, 59, 59).toDate();

		DemandeAsa dr1 = new DemandeAsa();
		List<EtatDemande> listEtatDemande = new ArrayList<EtatDemande>();

		EtatDemande et = new EtatDemande();
		et.setDate(dateJour);
		et.setEtat(RefEtatEnum.SAISIE);
		et.setIdAgent(9005168);
		et.setDemande(dr1);
		listEtatDemande.add(et);

		RefTypeAbsence rta = new RefTypeAbsence();
		absEntityManager.persist(rta);
		// Given

		dr1.setDateDebut(dateJour);
		dr1.setDateFin(dateJour);
		dr1.setDuree(10.0);
		dr1.setIdAgent(9005168);
		dr1.setType(rta);
		dr1.setEtatsDemande(listEtatDemande);
		absEntityManager.persist(dr1);

		DemandeAsa dr2 = new DemandeAsa();
		List<EtatDemande> listEtatDemande2 = new ArrayList<EtatDemande>();
		EtatDemande et2 = new EtatDemande();
		et2.setDate(dateJour);
		et2.setEtat(RefEtatEnum.VISEE_FAVORABLE);
		et2.setIdAgent(9005168);
		et2.setDemande(dr2);
		listEtatDemande2.add(et2);

		dr2.setDateDebut(dateJour);
		dr2.setDateFin(dateJour);
		dr2.setDuree(15.0);
		dr2.setIdAgent(9005168);
		dr2.setType(rta);
		dr2.setEtatsDemande(listEtatDemande2);
		absEntityManager.persist(dr2);

		DemandeAsa dr3 = new DemandeAsa();
		List<EtatDemande> listEtatDemande3 = new ArrayList<EtatDemande>();
		EtatDemande et3 = new EtatDemande();
		et3.setDate(dateJour);
		et3.setEtat(RefEtatEnum.VISEE_DEFAVORABLE);
		et3.setIdAgent(9005168);
		et3.setDemande(dr3);
		listEtatDemande3.add(et3);

		dr3.setDateDebut(dateJour);
		dr3.setDateFin(dateJour);
		dr3.setDuree(15.5);
		dr3.setIdAgent(9005168);
		dr3.setType(rta);
		dr3.setEtatsDemande(listEtatDemande3);
		absEntityManager.persist(dr3);

		DemandeAsa dr4 = new DemandeAsa();
		List<EtatDemande> listEtatDemande4 = new ArrayList<EtatDemande>();
		EtatDemande et4 = new EtatDemande();
		et4.setDate(dateJour);
		et4.setEtat(RefEtatEnum.APPROUVEE);
		et4.setIdAgent(9005168);
		et4.setDemande(dr4);
		listEtatDemande4.add(et4);

		dr4.setDateDebut(dateJour);
		dr4.setDateFin(dateJour);
		dr4.setDuree(15.5);
		dr4.setIdAgent(9005168);
		dr4.setType(rta);
		dr4.setEtatsDemande(listEtatDemande4);
		absEntityManager.persist(dr4);

		DemandeAsa dr5 = new DemandeAsa();
		List<EtatDemande> listEtatDemande5 = new ArrayList<EtatDemande>();
		EtatDemande et5 = new EtatDemande();
		et5.setDate(dateJour);
		et5.setEtat(RefEtatEnum.EN_ATTENTE);
		et5.setIdAgent(9005168);
		et5.setDemande(dr5);
		listEtatDemande2.add(et5);

		dr5.setDateDebut(dateJour);
		dr5.setDateFin(dateJour);
		dr5.setDuree(15.0);
		dr5.setIdAgent(9005168);
		dr5.setType(rta);
		dr5.setEtatsDemande(listEtatDemande5);
		absEntityManager.persist(dr5);

		DemandeAsa dr6 = new DemandeAsa();
		List<EtatDemande> listEtatDemande6 = new ArrayList<EtatDemande>();
		EtatDemande et6 = new EtatDemande();
		et6.setDate(dateJour);
		et6.setEtat(RefEtatEnum.REJETE);
		et6.setIdAgent(9005168);
		et6.setDemande(dr6);
		listEtatDemande6.add(et6);

		dr6.setDateDebut(dateJour);
		dr6.setDateFin(dateJour);
		dr6.setDuree(15.5);
		dr6.setIdAgent(9005168);
		dr6.setType(rta);
		dr6.setEtatsDemande(listEtatDemande6);
		absEntityManager.persist(dr6);

		DemandeAsa dr7 = new DemandeAsa();
		List<EtatDemande> listEtatDemande7 = new ArrayList<EtatDemande>();
		EtatDemande et7 = new EtatDemande();
		et7.setDate(dateJour);
		et7.setEtat(RefEtatEnum.REFUSEE);
		et7.setIdAgent(9005168);
		et7.setDemande(dr7);
		listEtatDemande7.add(et7);

		dr7.setDateDebut(dateJour);
		dr7.setDateFin(dateJour);
		dr7.setDuree(15.0);
		dr7.setIdAgent(9005168);
		dr7.setType(rta);
		dr7.setEtatsDemande(listEtatDemande5);
		absEntityManager.persist(dr7);

		DemandeAsa dr8 = new DemandeAsa();
		List<EtatDemande> listEtatDemande8 = new ArrayList<EtatDemande>();
		EtatDemande et8 = new EtatDemande();
		et8.setDate(dateJour);
		et8.setEtat(RefEtatEnum.PROVISOIRE);
		et8.setIdAgent(9005168);
		et8.setDemande(dr8);
		listEtatDemande8.add(et8);

		dr8.setDateDebut(dateJour);
		dr8.setDateFin(dateJour);
		dr8.setDuree(15.5);
		dr8.setIdAgent(9005168);
		dr8.setType(rta);
		dr8.setEtatsDemande(listEtatDemande8);
		absEntityManager.persist(dr8);

		DemandeAsa dr9 = new DemandeAsa();
		List<EtatDemande> listEtatDemande9 = new ArrayList<EtatDemande>();
		EtatDemande et9 = new EtatDemande();
		et9.setDate(dateJour);
		et9.setEtat(RefEtatEnum.PRISE);
		et9.setIdAgent(9005168);
		et9.setDemande(dr9);
		listEtatDemande9.add(et9);

		dr9.setDateDebut(dateJour);
		dr9.setDateFin(dateJour);
		dr9.setDuree(15.0);
		dr9.setIdAgent(9005168);
		dr9.setType(rta);
		dr9.setEtatsDemande(listEtatDemande9);
		absEntityManager.persist(dr9);

		DemandeAsa dr10 = new DemandeAsa();
		List<EtatDemande> listEtatDemande10 = new ArrayList<EtatDemande>();
		EtatDemande et10 = new EtatDemande();
		et10.setDate(dateJour);
		et10.setEtat(RefEtatEnum.VALIDEE);
		et10.setIdAgent(9005168);
		et10.setDemande(dr10);
		listEtatDemande8.add(et10);

		dr10.setDateDebut(dateJour);
		dr10.setDateFin(dateJour);
		dr10.setDuree(15.0);
		dr10.setIdAgent(9005168);
		dr10.setType(rta);
		dr10.setEtatsDemande(listEtatDemande10);
		absEntityManager.persist(dr10);

		DemandeAsa dr11 = new DemandeAsa();
		List<EtatDemande> listEtatDemande11 = new ArrayList<EtatDemande>();
		EtatDemande et11 = new EtatDemande();
		et11.setDate(dateJour);
		et11.setEtat(RefEtatEnum.ANNULEE);
		et11.setIdAgent(9005168);
		et11.setDemande(dr11);
		listEtatDemande8.add(et11);

		dr11.setDateDebut(dateJour);
		dr11.setDateFin(dateJour);
		dr11.setDuree(15.5);
		dr11.setIdAgent(9005168);
		dr11.setType(rta);
		dr11.setEtatsDemande(listEtatDemande11);
		absEntityManager.persist(dr11);

		// When
		List<DemandeAsa> result = repository.getListDemandeAsaPourMoisByAgent(9005168, null, dateDebMois, dateFinMois,
				rta.getIdRefTypeAbsence());

		assertEquals(7, result.size());
		
		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getListDemandeAsaPourMoisByAgent_noResult_sameDemande() {

		Date dateJour = new DateTime(2014, 05, 13, 12, 30, 0).toDate();
		Date dateDebMois = new DateTime(2014, 05, 1, 0, 0, 0).toDate();
		Date dateFinMois = new DateTime(2014, 05, 31, 23, 59, 59).toDate();

		DemandeAsa dr1 = new DemandeAsa();
		List<EtatDemande> listEtatDemande = new ArrayList<EtatDemande>();

		EtatDemande et = new EtatDemande();
		et.setDate(dateJour);
		et.setEtat(RefEtatEnum.SAISIE);
		et.setIdAgent(9005168);
		et.setDemande(dr1);
		listEtatDemande.add(et);

		RefTypeAbsence rta = new RefTypeAbsence();
		absEntityManager.persist(rta);
		// Given

		dr1.setDateDebut(dateJour);
		dr1.setDateFin(dateJour);
		dr1.setDuree(10.0);
		dr1.setIdAgent(9005168);
		dr1.setType(rta);
		dr1.setEtatsDemande(listEtatDemande);
		absEntityManager.persist(dr1);

		// When
		List<DemandeAsa> result = repository.getListDemandeAsaPourMoisByAgent(9005168, dr1.getIdDemande(), dateDebMois,
				dateFinMois, rta.getIdRefTypeAbsence());

		assertEquals(0, result.size());
		
		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getListDemandeAsaPourMoisByAgent_testTypeDemande() {

		Date dateJour = new DateTime(2014, 05, 13, 12, 30, 0).toDate();
		Date dateDebMois = new DateTime(2014, 05, 1, 0, 0, 0).toDate();
		Date dateFinMois = new DateTime(2014, 05, 31, 23, 59, 59).toDate();

		DemandeAsa dr1 = new DemandeAsa();
		List<EtatDemande> listEtatDemande = new ArrayList<EtatDemande>();

		EtatDemande et = new EtatDemande();
		et.setDate(dateJour);
		et.setEtat(RefEtatEnum.SAISIE);
		et.setIdAgent(9005168);
		et.setDemande(dr1);
		listEtatDemande.add(et);

		RefTypeAbsence rta = new RefTypeAbsence();
		absEntityManager.persist(rta);
		// Given

		dr1.setDateDebut(dateJour);
		dr1.setDateFin(dateJour);
		dr1.setDuree(10.0);
		dr1.setIdAgent(9005168);
		dr1.setType(rta);
		dr1.setEtatsDemande(listEtatDemande);
		absEntityManager.persist(dr1);

		DemandeAsa dr2 = new DemandeAsa();
		List<EtatDemande> listEtatDemande2 = new ArrayList<EtatDemande>();

		EtatDemande et2 = new EtatDemande();
		et2.setDate(dateJour);
		et2.setEtat(RefEtatEnum.SAISIE);
		et2.setIdAgent(9005168);
		et2.setDemande(dr2);
		listEtatDemande2.add(et2);

		RefTypeAbsence rta2 = new RefTypeAbsence();
		absEntityManager.persist(rta2);
		// Given

		dr2.setDateDebut(dateJour);
		dr2.setDateFin(dateJour);
		dr2.setDuree(10.0);
		dr2.setIdAgent(9005168);
		dr2.setType(rta2);
		dr2.setEtatsDemande(listEtatDemande2);
		absEntityManager.persist(dr2);

		DemandeAsa dr3 = new DemandeAsa();
		List<EtatDemande> listEtatDemande3 = new ArrayList<EtatDemande>();

		EtatDemande et3 = new EtatDemande();
		et3.setDate(dateJour);
		et3.setEtat(RefEtatEnum.SAISIE);
		et3.setIdAgent(9005168);
		et3.setDemande(dr3);
		listEtatDemande3.add(et3);

		RefTypeAbsence rta3 = new RefTypeAbsence();
		absEntityManager.persist(rta3);
		// Given

		dr3.setDateDebut(dateJour);
		dr3.setDateFin(dateJour);
		dr3.setDuree(10.0);
		dr3.setIdAgent(9005168);
		dr3.setType(rta3);
		dr3.setEtatsDemande(listEtatDemande3);
		absEntityManager.persist(dr3);

		DemandeAsa dr4 = new DemandeAsa();
		List<EtatDemande> listEtatDemande4 = new ArrayList<EtatDemande>();

		EtatDemande et4 = new EtatDemande();
		et4.setDate(dateJour);
		et4.setEtat(RefEtatEnum.SAISIE);
		et4.setIdAgent(9005168);
		et4.setDemande(dr1);
		listEtatDemande4.add(et4);

		RefTypeAbsence rta4 = new RefTypeAbsence();
		absEntityManager.persist(rta4);
		// Given

		dr4.setDateDebut(dateJour);
		dr4.setDateFin(dateJour);
		dr4.setDuree(10.0);
		dr4.setIdAgent(9005168);
		dr4.setType(rta4);
		dr4.setEtatsDemande(listEtatDemande4);
		absEntityManager.persist(dr4);

		// When
		List<DemandeAsa> result_ASA_A49 = repository.getListDemandeAsaPourMoisByAgent(9005168, null, dateDebMois,
				dateFinMois, rta.getIdRefTypeAbsence());
		assertEquals(1, result_ASA_A49.size());

		List<DemandeAsa> result_ASA_A50 = repository.getListDemandeAsaPourMoisByAgent(9005168, null, dateDebMois,
				dateFinMois, rta.getIdRefTypeAbsence());
		assertEquals(1, result_ASA_A50.size());
		
		absEntityManager.flush();
		absEntityManager.clear();
	}

//	@Test
	@Transactional("absTransactionManager")
	public void getListDemandeAsaPourMoisByOS_testEtatDemande() {

		Date dateJour = new DateTime(2014, 05, 13, 12, 30, 0).toDate();
		Date dateDebMois = new DateTime(2014, 05, 1, 0, 0, 0).toDate();
		Date dateFinMois = new DateTime(2014, 05, 31, 23, 59, 59).toDate();

		DemandeAsa dr1 = new DemandeAsa();
		List<EtatDemande> listEtatDemande = new ArrayList<EtatDemande>();

		EtatDemande et = new EtatDemande();
		et.setDate(dateJour);
		et.setEtat(RefEtatEnum.SAISIE);
		et.setIdAgent(9005168);
		et.setDemande(dr1);
		listEtatDemande.add(et);

		RefTypeAbsence rta = new RefTypeAbsence();
		absEntityManager.persist(rta);

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(1);
		absEntityManager.persist(organisationSyndicale);

		// Given
		dr1.setDateDebut(dateJour);
		dr1.setDateFin(dateJour);
		dr1.setDuree(10.0);
		dr1.setIdAgent(9005168);
		dr1.setType(rta);
		dr1.setOrganisationSyndicale(organisationSyndicale);
		dr1.setEtatsDemande(listEtatDemande);
		absEntityManager.persist(dr1);

		DemandeAsa dr2 = new DemandeAsa();
		List<EtatDemande> listEtatDemande2 = new ArrayList<EtatDemande>();
		EtatDemande et2 = new EtatDemande();
		et2.setDate(dateJour);
		et2.setEtat(RefEtatEnum.VISEE_FAVORABLE);
		et2.setIdAgent(9005168);
		et2.setDemande(dr2);
		listEtatDemande2.add(et2);

		dr2.setDateDebut(dateJour);
		dr2.setDateFin(dateJour);
		dr2.setDuree(15.0);
		dr2.setIdAgent(9005168);
		dr2.setType(rta);
		dr2.setOrganisationSyndicale(organisationSyndicale);
		dr2.setEtatsDemande(listEtatDemande2);
		absEntityManager.persist(dr2);

		DemandeAsa dr3 = new DemandeAsa();
		List<EtatDemande> listEtatDemande3 = new ArrayList<EtatDemande>();
		EtatDemande et3 = new EtatDemande();
		et3.setDate(dateJour);
		et3.setEtat(RefEtatEnum.VISEE_DEFAVORABLE);
		et3.setIdAgent(9005168);
		et3.setDemande(dr3);
		listEtatDemande3.add(et3);

		dr3.setDateDebut(dateJour);
		dr3.setDateFin(dateJour);
		dr3.setDuree(15.5);
		dr3.setIdAgent(9005168);
		dr3.setType(rta);
		dr3.setOrganisationSyndicale(organisationSyndicale);
		dr3.setEtatsDemande(listEtatDemande3);
		absEntityManager.persist(dr3);

		DemandeAsa dr4 = new DemandeAsa();
		List<EtatDemande> listEtatDemande4 = new ArrayList<EtatDemande>();
		EtatDemande et4 = new EtatDemande();
		et4.setDate(dateJour);
		et4.setEtat(RefEtatEnum.APPROUVEE);
		et4.setIdAgent(9005168);
		et4.setDemande(dr4);
		listEtatDemande4.add(et4);

		dr4.setDateDebut(dateJour);
		dr4.setDateFin(dateJour);
		dr4.setDuree(15.5);
		dr4.setIdAgent(9005168);
		dr4.setType(rta);
		dr4.setOrganisationSyndicale(organisationSyndicale);
		dr4.setEtatsDemande(listEtatDemande4);
		absEntityManager.persist(dr4);

		DemandeAsa dr5 = new DemandeAsa();
		List<EtatDemande> listEtatDemande5 = new ArrayList<EtatDemande>();
		EtatDemande et5 = new EtatDemande();
		et5.setDate(dateJour);
		et5.setEtat(RefEtatEnum.EN_ATTENTE);
		et5.setIdAgent(9005168);
		et5.setDemande(dr5);
		listEtatDemande2.add(et5);

		dr5.setDateDebut(dateJour);
		dr5.setDateFin(dateJour);
		dr5.setDuree(15.0);
		dr5.setIdAgent(9005168);
		dr5.setType(rta);
		dr5.setOrganisationSyndicale(organisationSyndicale);
		dr5.setEtatsDemande(listEtatDemande5);
		absEntityManager.persist(dr5);

		DemandeAsa dr6 = new DemandeAsa();
		List<EtatDemande> listEtatDemande6 = new ArrayList<EtatDemande>();
		EtatDemande et6 = new EtatDemande();
		et6.setDate(dateJour);
		et6.setEtat(RefEtatEnum.REJETE);
		et6.setIdAgent(9005168);
		et6.setDemande(dr6);
		listEtatDemande6.add(et6);

		dr6.setDateDebut(dateJour);
		dr6.setDateFin(dateJour);
		dr6.setDuree(15.5);
		dr6.setIdAgent(9005168);
		dr6.setType(rta);
		dr6.setOrganisationSyndicale(organisationSyndicale);
		dr6.setEtatsDemande(listEtatDemande6);
		absEntityManager.persist(dr6);

		DemandeAsa dr7 = new DemandeAsa();
		List<EtatDemande> listEtatDemande7 = new ArrayList<EtatDemande>();
		EtatDemande et7 = new EtatDemande();
		et7.setDate(dateJour);
		et7.setEtat(RefEtatEnum.REFUSEE);
		et7.setIdAgent(9005168);
		et7.setDemande(dr7);
		listEtatDemande7.add(et7);

		dr7.setDateDebut(dateJour);
		dr7.setDateFin(dateJour);
		dr7.setDuree(15.0);
		dr7.setIdAgent(9005168);
		dr7.setType(rta);
		dr7.setOrganisationSyndicale(organisationSyndicale);
		dr7.setEtatsDemande(listEtatDemande5);
		absEntityManager.persist(dr7);

		DemandeAsa dr8 = new DemandeAsa();
		List<EtatDemande> listEtatDemande8 = new ArrayList<EtatDemande>();
		EtatDemande et8 = new EtatDemande();
		et8.setDate(dateJour);
		et8.setEtat(RefEtatEnum.PROVISOIRE);
		et8.setIdAgent(9005168);
		et8.setDemande(dr8);
		listEtatDemande8.add(et8);

		dr8.setDateDebut(dateJour);
		dr8.setDateFin(dateJour);
		dr8.setDuree(15.5);
		dr8.setIdAgent(9005168);
		dr8.setType(rta);
		dr8.setOrganisationSyndicale(organisationSyndicale);
		dr8.setEtatsDemande(listEtatDemande8);
		absEntityManager.persist(dr8);

		DemandeAsa dr9 = new DemandeAsa();
		List<EtatDemande> listEtatDemande9 = new ArrayList<EtatDemande>();
		EtatDemande et9 = new EtatDemande();
		et9.setDate(dateJour);
		et9.setEtat(RefEtatEnum.PRISE);
		et9.setIdAgent(9005168);
		et9.setDemande(dr9);
		listEtatDemande9.add(et9);

		dr9.setDateDebut(dateJour);
		dr9.setDateFin(dateJour);
		dr9.setDuree(15.0);
		dr9.setIdAgent(9005168);
		dr9.setType(rta);
		dr9.setOrganisationSyndicale(organisationSyndicale);
		dr9.setEtatsDemande(listEtatDemande9);
		absEntityManager.persist(dr9);

		DemandeAsa dr10 = new DemandeAsa();
		List<EtatDemande> listEtatDemande10 = new ArrayList<EtatDemande>();
		EtatDemande et10 = new EtatDemande();
		et10.setDate(dateJour);
		et10.setEtat(RefEtatEnum.VALIDEE);
		et10.setIdAgent(9005168);
		et10.setDemande(dr10);
		listEtatDemande8.add(et10);

		dr10.setDateDebut(dateJour);
		dr10.setDateFin(dateJour);
		dr10.setDuree(15.0);
		dr10.setIdAgent(9005168);
		dr10.setType(rta);
		dr10.setOrganisationSyndicale(organisationSyndicale);
		dr10.setEtatsDemande(listEtatDemande10);
		absEntityManager.persist(dr10);

		DemandeAsa dr11 = new DemandeAsa();
		List<EtatDemande> listEtatDemande11 = new ArrayList<EtatDemande>();
		EtatDemande et11 = new EtatDemande();
		et11.setDate(dateJour);
		et11.setEtat(RefEtatEnum.ANNULEE);
		et11.setIdAgent(9005168);
		et11.setDemande(dr11);
		listEtatDemande8.add(et11);

		dr11.setDateDebut(dateJour);
		dr11.setDateFin(dateJour);
		dr11.setDuree(15.5);
		dr11.setIdAgent(9005168);
		dr11.setType(rta);
		dr11.setOrganisationSyndicale(organisationSyndicale);
		dr11.setEtatsDemande(listEtatDemande11);
		absEntityManager.persist(dr11);

		// When
		List<DemandeAsa> result = repository.getListDemandeAsaPourMoisByOS(1, null, dateDebMois, dateFinMois,
				rta.getIdRefTypeAbsence());

		assertEquals(7, result.size());
		
		absEntityManager.flush();
		absEntityManager.clear();
	}

//	@Test
	@Transactional("absTransactionManager")
	public void getListDemandeAsaPourMoisByOS_noResult_sameDemande() {

		Date dateJour = new DateTime(2014, 05, 13, 12, 30, 0).toDate();
		Date dateDebMois = new DateTime(2014, 05, 1, 0, 0, 0).toDate();
		Date dateFinMois = new DateTime(2014, 05, 31, 23, 59, 59).toDate();

		DemandeAsa dr1 = new DemandeAsa();
		List<EtatDemande> listEtatDemande = new ArrayList<EtatDemande>();

		EtatDemande et = new EtatDemande();
		et.setDate(dateJour);
		et.setEtat(RefEtatEnum.SAISIE);
		et.setIdAgent(9005168);
		et.setDemande(dr1);
		listEtatDemande.add(et);

		RefTypeAbsence rta = new RefTypeAbsence();
		absEntityManager.persist(rta);

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(1);
		absEntityManager.persist(organisationSyndicale);

		// Given
		dr1.setDateDebut(dateJour);
		dr1.setDateFin(dateJour);
		dr1.setDuree(10.0);
		dr1.setIdAgent(9005168);
		dr1.setType(rta);
		dr1.setOrganisationSyndicale(organisationSyndicale);
		dr1.setEtatsDemande(listEtatDemande);
		absEntityManager.persist(dr1);

		// When
		List<DemandeAsa> result = repository.getListDemandeAsaPourMoisByOS(1, dr1.getIdDemande(), dateDebMois,
				dateFinMois, rta.getIdRefTypeAbsence());

		assertEquals(0, result.size());
		
		absEntityManager.flush();
		absEntityManager.clear();
	}

//	@Test
	@Transactional("absTransactionManager")
	public void getListDemandeAsaPourMoisByOS_testTypeDemande() {

		Date dateJour = new DateTime(2014, 05, 13, 12, 30, 0).toDate();
		Date dateDebMois = new DateTime(2014, 05, 1, 0, 0, 0).toDate();
		Date dateFinMois = new DateTime(2014, 05, 31, 23, 59, 59).toDate();

		DemandeAsa dr1 = new DemandeAsa();
		List<EtatDemande> listEtatDemande = new ArrayList<EtatDemande>();

		EtatDemande et = new EtatDemande();
		et.setDate(dateJour);
		et.setEtat(RefEtatEnum.SAISIE);
		et.setIdAgent(9005168);
		et.setDemande(dr1);
		listEtatDemande.add(et);

		RefTypeAbsence rta = new RefTypeAbsence();
		absEntityManager.persist(rta);

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(1);
		absEntityManager.persist(organisationSyndicale);
		
		// Given
		dr1.setDateDebut(dateJour);
		dr1.setDateFin(dateJour);
		dr1.setDuree(10.0);
		dr1.setIdAgent(9005168);
		dr1.setType(rta);
		dr1.setOrganisationSyndicale(organisationSyndicale);
		dr1.setEtatsDemande(listEtatDemande);
		absEntityManager.persist(dr1);

		DemandeAsa dr2 = new DemandeAsa();
		List<EtatDemande> listEtatDemande2 = new ArrayList<EtatDemande>();

		EtatDemande et2 = new EtatDemande();
		et2.setDate(dateJour);
		et2.setEtat(RefEtatEnum.SAISIE);
		et2.setIdAgent(9005168);
		et2.setDemande(dr2);
		listEtatDemande2.add(et2);

		RefTypeAbsence rta2 = new RefTypeAbsence();
		absEntityManager.persist(rta2);
		// Given

		dr2.setDateDebut(dateJour);
		dr2.setDateFin(dateJour);
		dr2.setDuree(10.0);
		dr2.setIdAgent(9005168);
		dr2.setType(rta2);
		dr2.setOrganisationSyndicale(organisationSyndicale);
		dr2.setEtatsDemande(listEtatDemande2);
		absEntityManager.persist(dr2);

		DemandeAsa dr3 = new DemandeAsa();
		List<EtatDemande> listEtatDemande3 = new ArrayList<EtatDemande>();

		EtatDemande et3 = new EtatDemande();
		et3.setDate(dateJour);
		et3.setEtat(RefEtatEnum.SAISIE);
		et3.setIdAgent(9005168);
		et3.setDemande(dr3);
		listEtatDemande3.add(et3);

		RefTypeAbsence rta3 = new RefTypeAbsence();
		absEntityManager.persist(rta3);
		// Given

		dr3.setDateDebut(dateJour);
		dr3.setDateFin(dateJour);
		dr3.setDuree(10.0);
		dr3.setIdAgent(9005168);
		dr3.setType(rta3);
		dr3.setOrganisationSyndicale(organisationSyndicale);
		dr3.setEtatsDemande(listEtatDemande3);
		absEntityManager.persist(dr3);

		DemandeAsa dr4 = new DemandeAsa();
		List<EtatDemande> listEtatDemande4 = new ArrayList<EtatDemande>();

		EtatDemande et4 = new EtatDemande();
		et4.setDate(dateJour);
		et4.setEtat(RefEtatEnum.SAISIE);
		et4.setIdAgent(9005168);
		et4.setDemande(dr1);
		listEtatDemande4.add(et4);

		RefTypeAbsence rta4 = new RefTypeAbsence();
		absEntityManager.persist(rta4);
		// Given

		dr4.setDateDebut(dateJour);
		dr4.setDateFin(dateJour);
		dr4.setDuree(10.0);
		dr4.setIdAgent(9005168);
		dr4.setType(rta4);
		dr4.setOrganisationSyndicale(organisationSyndicale);
		dr4.setEtatsDemande(listEtatDemande4);
		absEntityManager.persist(dr4);

		// When
		List<DemandeAsa> result_ASA_A49 = repository.getListDemandeAsaPourMoisByOS(1, null, dateDebMois, dateFinMois,
				rta.getIdRefTypeAbsence());
		assertEquals(1, result_ASA_A49.size());

		List<DemandeAsa> result_ASA_A50 = repository.getListDemandeAsaPourMoisByOS(1, null, dateDebMois, dateFinMois,
				rta.getIdRefTypeAbsence());
		assertEquals(1, result_ASA_A50.size());
		
		absEntityManager.flush();
		absEntityManager.clear();
	}
}
