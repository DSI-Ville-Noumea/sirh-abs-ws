package nc.noumea.mairie.abs.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nc.noumea.mairie.abs.domain.Droit;
import nc.noumea.mairie.abs.domain.DroitDroitsAgent;
import nc.noumea.mairie.abs.domain.DroitProfil;
import nc.noumea.mairie.abs.domain.DroitsAgent;
import nc.noumea.mairie.abs.domain.Profil;
import nc.noumea.mairie.abs.domain.ProfilEnum;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/META-INF/spring/applicationContext-test.xml" })
public class AccessRightsRepositoryTest {

	@Autowired
	AccessRightsRepository repository;

	@PersistenceContext(unitName = "absPersistenceUnit")
	private EntityManager absEntityManager;

	@Test
	@Transactional("absTransactionManager")
	public void getAgentAccessRights_ReturnNull() {

		// When
		Droit result = repository.getAgentAccessRights(9005138);

		// Then
		assertEquals(null, result);
	}

	@Test
	@Transactional("absTransactionManager")
	public void getAgentAccessRights_ReturnResult() {

		Profil pr = new Profil();
		pr.setLibelle("libelle test");
		pr.setSaisie(true);
		pr.setModification(true);
		pr.setSuppression(true);
		pr.setImpression(true);
		pr.setViserVisu(false);
		pr.setViserModif(false);
		pr.setApprouverVisu(false);
		pr.setApprouverModif(false);
		pr.setAnnuler(true);
		pr.setVisuSolde(true);
		pr.setMajSolde(true);
		pr.setDroitAcces(false);
		absEntityManager.persist(pr);

		DroitProfil dpr = new DroitProfil();
		dpr.setProfil(pr);
		absEntityManager.persist(dpr);

		Droit droit = new Droit();
		droit.setDateModification(new Date());
		droit.setIdAgent(9008767);
		droit.setIdDroit(1);
		droit.getDroitProfils().add(dpr);
		absEntityManager.persist(droit);

		// When
		Droit result = repository.getAgentAccessRights(9008767);

		// Then
		assertEquals("9008767", result.getIdAgent().toString());
		assertEquals(1, result.getDroitProfils().size());
		for (DroitProfil dp : result.getDroitProfils()) {
			assertTrue(dp.getProfil().isSaisie());
		}

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getAgentsApprobateurs_noResult() {
		List<Droit> result = repository.getAgentsApprobateurs();

		assertEquals(0, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getAgentsApprobateurs_result() {
		Profil p = new Profil();
		p.setLibelle("APPROBATEUR");
		absEntityManager.persist(p);

		Droit droitApprobateur1 = new Droit();
		DroitProfil dp1 = new DroitProfil();
		dp1.setDroit(droitApprobateur1);
		dp1.setDroitApprobateur(droitApprobateur1);
		dp1.setProfil(p);
		droitApprobateur1.setIdAgent(9008767);
		droitApprobateur1.getDroitProfils().add(dp1);
		absEntityManager.persist(droitApprobateur1);

		Droit droitApprobateur2 = new Droit();
		DroitProfil dp2 = new DroitProfil();
		dp2.setDroit(droitApprobateur2);
		dp2.setDroitApprobateur(droitApprobateur2);
		dp2.setProfil(p);
		droitApprobateur2.setIdAgent(9008768);
		droitApprobateur2.getDroitProfils().add(dp2);
		absEntityManager.persist(droitApprobateur2);

		List<Droit> listDroits = repository.getAgentsApprobateurs();

		assertEquals(2, listDroits.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void isUserOperateur() {
		Profil p1 = new Profil();
		p1.setLibelle("OPERATEUR");
		absEntityManager.persist(p1);
		Profil p2 = new Profil();
		p2.setLibelle("APPROBATEUR");
		absEntityManager.persist(p2);

		Droit droitApprobateur1 = new Droit();
		DroitProfil dp1 = new DroitProfil();
		dp1.setDroit(droitApprobateur1);
		dp1.setDroitApprobateur(droitApprobateur1);
		dp1.setProfil(p1);
		droitApprobateur1.setIdAgent(9008767);
		droitApprobateur1.getDroitProfils().add(dp1);
		absEntityManager.persist(droitApprobateur1);

		Droit droitApprobateur2 = new Droit();
		DroitProfil dp2 = new DroitProfil();
		dp2.setDroit(droitApprobateur2);
		dp2.setDroitApprobateur(droitApprobateur2);
		dp2.setProfil(p2);
		droitApprobateur2.setIdAgent(9008768);
		droitApprobateur2.getDroitProfils().add(dp2);
		absEntityManager.persist(droitApprobateur2);

		// When
		assertTrue(repository.isUserOperateur(9008767));
		assertFalse(repository.isUserOperateur(9008768));

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void isUserViseur() {
		Profil p1 = new Profil();
		p1.setLibelle("VISEUR");
		absEntityManager.persist(p1);
		Profil p2 = new Profil();
		p2.setLibelle("APPROBATEUR");
		absEntityManager.persist(p2);

		Droit droitApprobateur1 = new Droit();
		DroitProfil dp1 = new DroitProfil();
		dp1.setDroit(droitApprobateur1);
		dp1.setDroitApprobateur(droitApprobateur1);
		dp1.setProfil(p1);
		droitApprobateur1.setIdAgent(9008767);
		droitApprobateur1.getDroitProfils().add(dp1);
		absEntityManager.persist(droitApprobateur1);

		Droit droitApprobateur2 = new Droit();
		DroitProfil dp2 = new DroitProfil();
		dp2.setDroit(droitApprobateur2);
		dp2.setDroitApprobateur(droitApprobateur2);
		dp2.setProfil(p2);
		droitApprobateur2.setIdAgent(9008768);
		droitApprobateur2.getDroitProfils().add(dp2);
		absEntityManager.persist(droitApprobateur2);

		// When
		assertTrue(repository.isUserViseur(9008767));
		assertFalse(repository.isUserViseur(9008768));

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void isUserApprobateur() {
		Profil p1 = new Profil();
		p1.setLibelle(ProfilEnum.VISEUR.toString());
		absEntityManager.persist(p1);
		Profil p2 = new Profil();
		p2.setLibelle("APPROBATEUR");
		absEntityManager.persist(p2);

		Droit droitApprobateur = new Droit();
		DroitProfil dp2 = new DroitProfil();
		dp2.setDroit(droitApprobateur);
		dp2.setDroitApprobateur(droitApprobateur);
		dp2.setProfil(p2);
		droitApprobateur.setIdAgent(9008768);
		droitApprobateur.getDroitProfils().add(dp2);
		absEntityManager.persist(droitApprobateur);

		Droit droitViseur = new Droit();
		DroitProfil dp1 = new DroitProfil();
		dp1.setDroit(droitViseur);
		dp1.setDroitApprobateur(droitApprobateur);
		dp1.setProfil(p1);
		droitViseur.setIdAgent(9008767);
		droitViseur.getDroitProfils().add(dp1);
		absEntityManager.persist(droitViseur);

		// When
		assertFalse(repository.isUserApprobateur(9008767));
		assertTrue(repository.isUserApprobateur(9008768));

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void isUserDelegataire() {
		Profil p1 = new Profil();
		p1.setLibelle("DELEGATAIRE");
		absEntityManager.persist(p1);
		Profil p2 = new Profil();
		p2.setLibelle("APPROBATEUR");
		absEntityManager.persist(p2);

		Droit droitApprobateur1 = new Droit();
		DroitProfil dp1 = new DroitProfil();
		dp1.setDroit(droitApprobateur1);
		dp1.setDroitApprobateur(droitApprobateur1);
		dp1.setProfil(p1);
		droitApprobateur1.setIdAgent(9008767);
		droitApprobateur1.getDroitProfils().add(dp1);
		absEntityManager.persist(droitApprobateur1);

		Droit droitApprobateur2 = new Droit();
		DroitProfil dp2 = new DroitProfil();
		dp2.setDroit(droitApprobateur2);
		dp2.setDroitApprobateur(droitApprobateur2);
		dp2.setProfil(p2);
		droitApprobateur2.setIdAgent(9008768);
		droitApprobateur2.getDroitProfils().add(dp2);
		absEntityManager.persist(droitApprobateur2);

		// When
		assertTrue(repository.isUserDelegataire(9008767));
		assertFalse(repository.isUserDelegataire(9008768));

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getProfilByName() {
		Profil p1 = new Profil();
		p1.setLibelle("DELEGATAIRE");
		absEntityManager.persist(p1);

		Profil result = repository.getProfilByName(ProfilEnum.DELEGATAIRE.toString());

		// When
		assertEquals("DELEGATAIRE", result.getLibelle());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getDroitSousApprobateur() {
		Profil p1 = new Profil();
		p1.setLibelle("APPROBATEUR");
		absEntityManager.persist(p1);
		Profil p2 = new Profil();
		p2.setLibelle("VISEUR");
		absEntityManager.persist(p2);

		Droit droitApprobateur1 = new Droit();
		Droit droitViseur = new Droit();

		DroitProfil dp1 = new DroitProfil();
		dp1.setDroit(droitApprobateur1);
		dp1.setDroitApprobateur(droitApprobateur1);
		dp1.setProfil(p1);
		DroitProfil dp2 = new DroitProfil();
		dp2.setDroit(droitViseur);
		dp2.setDroitApprobateur(droitApprobateur1);
		dp2.setProfil(p2);

		droitApprobateur1.setIdAgent(9005138);
		droitApprobateur1.getDroitProfils().add(dp1);
		droitViseur.setIdAgent(9005131);
		droitViseur.getDroitProfils().add(dp2);

		absEntityManager.persist(droitApprobateur1);
		absEntityManager.persist(droitViseur);
		absEntityManager.persist(dp1);
		absEntityManager.persist(dp2);

		List<Droit> result = repository.getDroitSousApprobateur(9005138);

		// When
		assertEquals(1, result.size());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getDroitSousApprobateur_AgentAutreProfil() {
		Profil p1 = new Profil();
		p1.setLibelle("APPROBATEUR");
		absEntityManager.persist(p1);
		Profil p2 = new Profil();
		p2.setLibelle("DELEGATAIRE");
		absEntityManager.persist(p2);

		// appro1
		Droit droitApprobateur1 = new Droit();
		droitApprobateur1.setIdDroit(1);
		droitApprobateur1.setIdAgent(9005138);

		// appro2
		Droit droitApprobateur2 = new Droit();
		droitApprobateur2.setIdAgent(9003041);
		droitApprobateur2.setIdDroit(3);

		// appro3
		Droit droitApprobateur3 = new Droit();
		droitApprobateur3.setIdAgent(9002990);
		droitApprobateur3.setIdDroit(4);

		// delegataire
		Droit droitDelegataire = new Droit();
		droitDelegataire.setIdAgent(9005131);
		droitDelegataire.setIdDroit(2);

		// on cree un profil a l'appro 1
		DroitProfil dp1 = new DroitProfil();
		dp1.setDroit(droitApprobateur1);
		dp1.setDroitApprobateur(droitApprobateur1);
		dp1.setProfil(p1);
		droitApprobateur1.getDroitProfils().add(dp1);

		// on cree un profil a l'appro 2
		DroitProfil dp2 = new DroitProfil();
		dp2.setDroit(droitApprobateur2);
		dp2.setDroitApprobateur(droitApprobateur2);
		dp2.setProfil(p1);
		droitApprobateur2.getDroitProfils().add(dp2);

		// on cree un profil a l'appro 3
		DroitProfil dp3 = new DroitProfil();
		dp3.setDroit(droitApprobateur3);
		dp3.setDroitApprobateur(droitApprobateur3);
		dp3.setProfil(p1);
		droitApprobateur3.getDroitProfils().add(dp3);

		// on cree un profil au delegataire pour l'appro 1
		DroitProfil dp4 = new DroitProfil();
		dp4.setDroit(droitDelegataire);
		dp4.setDroitApprobateur(droitApprobateur1);
		dp4.setProfil(p2);
		droitDelegataire.getDroitProfils().add(dp4);

		// on cree un profil au delegataire pour l'appro 2
		DroitProfil dp5 = new DroitProfil();
		dp5.setDroit(droitDelegataire);
		dp5.setDroitApprobateur(droitApprobateur2);
		dp5.setProfil(p2);
		droitDelegataire.getDroitProfils().add(dp5);

		// on cree un profil au delegataire pour l'appro 3
		DroitProfil dp6 = new DroitProfil();
		dp6.setDroit(droitDelegataire);
		dp6.setDroitApprobateur(droitApprobateur3);
		dp6.setProfil(p2);
		droitDelegataire.getDroitProfils().add(dp6);

		absEntityManager.persist(droitApprobateur1);
		absEntityManager.persist(droitApprobateur2);
		absEntityManager.persist(droitApprobateur3);
		absEntityManager.persist(droitDelegataire);
		absEntityManager.persist(dp1);
		absEntityManager.persist(dp2);
		absEntityManager.persist(dp3);
		absEntityManager.persist(dp4);
		absEntityManager.persist(dp5);
		absEntityManager.persist(dp6);

		List<Droit> result = repository.getDroitSousApprobateur(9005138);

		// When
		assertEquals(1, result.size());
		assertEquals(3, result.get(0).getDroitProfils().size());
		assertTrue(result.get(0).getDroitProfils().contains(dp4));
		assertTrue(result.get(0).getDroitProfils().contains(dp5));
		assertTrue(result.get(0).getDroitProfils().contains(dp6));

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getListOfAgentsToInputOrApproveByService() {

		Profil p1 = new Profil();
		p1.setLibelle("APPROBATEUR");
		absEntityManager.persist(p1);

		Droit droit = new Droit();
		droit.setDateModification(new Date());
		droit.setIdAgent(9008767);
		droit.setIdDroit(1);
		absEntityManager.persist(droit);

		DroitDroitsAgent droitDroitsAgent = new DroitDroitsAgent();
		DroitsAgent agent = new DroitsAgent();
		Set<DroitDroitsAgent> droitDroitsAgents = new HashSet<DroitDroitsAgent>();

		DroitProfil droitProfil = new DroitProfil();
		droitProfil.setDroit(droit);
		droitProfil.setProfil(p1);
		absEntityManager.persist(droitProfil);

		droitDroitsAgent.setDroit(droit);
		droitDroitsAgent.setDroitProfil(droitProfil);
		droitDroitsAgent.setDroitsAgent(agent);

		droitDroitsAgents.add(droitDroitsAgent);

		agent.setIdAgent(9008767);
		agent.setIdDroitsAgent(1);
		agent.setCodeService("DEAB");
		agent.setLibelleService("DASP Pôle Administratif et Budgétaire");
		agent.setDateModification(new Date());
		agent.setDroitDroitsAgent(droitDroitsAgents);

		absEntityManager.persist(agent);
		absEntityManager.persist(droitDroitsAgent);

		List<DroitsAgent> result = repository.getListOfAgentsToInputOrApprove(9008767, "DEAB",
				droitProfil.getIdDroitProfil());

		assertEquals(1, result.size());
		assertEquals("9008767", result.get(0).getIdAgent().toString());
		assertEquals("DASP Pôle Administratif et Budgétaire", result.get(0).getLibelleService());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void isUserOperateurOfApprobateur() {
		Profil p1 = new Profil();
		p1.setLibelle("OPERATEUR");
		absEntityManager.persist(p1);
		Profil appro = new Profil();
		appro.setLibelle("APPROBATEUR");
		absEntityManager.persist(appro);

		Integer idAgentAppro1 = 9005138;
		Integer idAgentAppro2 = 9005131;
		Integer idAgentOperateur1 = 9002990;
		Integer idAgentOperateur2 = 9003041;

		// on definit un approbateur 1
		Droit droitApprobateur1 = new Droit();
		droitApprobateur1.setIdAgent(idAgentAppro1);
		DroitProfil dp1 = new DroitProfil();
		dp1.setDroit(droitApprobateur1);
		dp1.setDroitApprobateur(droitApprobateur1);
		dp1.setProfil(appro);
		droitApprobateur1.getDroitProfils().add(dp1);

		// on definit un approbateur 2
		Droit droitApprobateur2 = new Droit();
		droitApprobateur2.setIdAgent(idAgentAppro2);
		DroitProfil dp2 = new DroitProfil();
		dp2.setDroit(droitApprobateur2);
		dp2.setDroitApprobateur(droitApprobateur2);
		dp2.setProfil(appro);
		droitApprobateur2.getDroitProfils().add(dp2);

		// on ajoute les opérateurs a chaque approbateur
		Droit droitOpe1 = new Droit();
		droitOpe1.setIdAgent(idAgentOperateur1);
		DroitProfil dp3 = new DroitProfil();
		dp3.setDroit(droitOpe1);
		dp3.setDroitApprobateur(droitApprobateur1);
		dp3.setProfil(p1);
		droitApprobateur1.getDroitProfils().add(dp3);
		Droit droitOpe2 = new Droit();
		droitOpe2.setIdAgent(idAgentOperateur2);
		DroitProfil dp4 = new DroitProfil();
		dp4.setDroit(droitOpe2);
		dp4.setDroitApprobateur(droitApprobateur2);
		dp4.setProfil(p1);
		droitApprobateur2.getDroitProfils().add(dp4);

		// on persist les objets
		absEntityManager.persist(droitApprobateur1);
		absEntityManager.persist(droitApprobateur2);
		absEntityManager.persist(droitOpe1);
		absEntityManager.persist(droitOpe2);

		// When
		assertTrue(repository.isUserOperateurOfApprobateur(idAgentAppro1, idAgentOperateur1));
		assertFalse(repository.isUserOperateurOfApprobateur(idAgentAppro2, idAgentOperateur1));
		assertTrue(repository.isUserOperateurOfApprobateur(idAgentAppro2, idAgentOperateur2));
		assertFalse(repository.isUserOperateurOfApprobateur(idAgentAppro1, idAgentOperateur2));

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void isUserDelagataireOfApprobateur() {
		Profil p1 = new Profil();
		p1.setLibelle("DELEGATAIRE");
		absEntityManager.persist(p1);
		Profil appro = new Profil();
		appro.setLibelle("APPROBATEUR");
		absEntityManager.persist(appro);

		Integer idAgentAppro1 = 9005138;
		Integer idAgentAppro2 = 9005131;
		Integer idAgentDele1 = 9002990;
		Integer idAgentDele2 = 9003041;

		// on definit un approbateur 1
		Droit droitApprobateur1 = new Droit();
		droitApprobateur1.setIdAgent(idAgentAppro1);
		DroitProfil dp1 = new DroitProfil();
		dp1.setDroit(droitApprobateur1);
		dp1.setDroitApprobateur(droitApprobateur1);
		dp1.setProfil(appro);
		droitApprobateur1.getDroitProfils().add(dp1);

		// on definit un approbateur 2
		Droit droitApprobateur2 = new Droit();
		droitApprobateur2.setIdAgent(idAgentAppro2);
		DroitProfil dp2 = new DroitProfil();
		dp2.setDroit(droitApprobateur2);
		dp2.setDroitApprobateur(droitApprobateur2);
		dp2.setProfil(appro);
		droitApprobateur2.getDroitProfils().add(dp2);

		// on ajoute les delegataires a chaque approbateur
		Droit droitDele1 = new Droit();
		droitDele1.setIdAgent(idAgentDele1);
		DroitProfil dp3 = new DroitProfil();
		dp3.setDroit(droitDele1);
		dp3.setDroitApprobateur(droitApprobateur1);
		dp3.setProfil(p1);
		droitApprobateur1.getDroitProfils().add(dp3);
		Droit droitDele2 = new Droit();
		droitDele2.setIdAgent(idAgentDele2);
		DroitProfil dp4 = new DroitProfil();
		dp4.setDroit(droitDele2);
		dp4.setDroitApprobateur(droitApprobateur2);
		dp4.setProfil(p1);
		droitApprobateur2.getDroitProfils().add(dp4);

		// on persist les objets
		absEntityManager.persist(droitApprobateur1);
		absEntityManager.persist(droitApprobateur2);
		absEntityManager.persist(droitDele1);
		absEntityManager.persist(droitDele2);

		// When
		assertTrue(repository.isUserDelegataireOfApprobateur(idAgentAppro1, idAgentDele1));
		assertFalse(repository.isUserDelegataireOfApprobateur(idAgentAppro2, idAgentDele1));
		assertTrue(repository.isUserDelegataireOfApprobateur(idAgentAppro2, idAgentDele2));
		assertFalse(repository.isUserDelegataireOfApprobateur(idAgentAppro1, idAgentDele2));

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void isUserViseurOfApprobateur() {
		Profil p1 = new Profil();
		p1.setLibelle("VISEUR");
		absEntityManager.persist(p1);
		Profil appro = new Profil();
		appro.setLibelle("APPROBATEUR");
		absEntityManager.persist(appro);

		Integer idAgentAppro1 = 9005138;
		Integer idAgentAppro2 = 9005131;
		Integer idAgentVis1 = 9002990;
		Integer idAgentVis2 = 9003041;

		// on definit un approbateur 1
		Droit droitApprobateur1 = new Droit();
		droitApprobateur1.setIdAgent(idAgentAppro1);
		DroitProfil dp1 = new DroitProfil();
		dp1.setDroit(droitApprobateur1);
		dp1.setDroitApprobateur(droitApprobateur1);
		dp1.setProfil(appro);
		droitApprobateur1.getDroitProfils().add(dp1);

		// on definit un approbateur 2
		Droit droitApprobateur2 = new Droit();
		droitApprobateur2.setIdAgent(idAgentAppro2);
		DroitProfil dp2 = new DroitProfil();
		dp2.setDroit(droitApprobateur2);
		dp2.setDroitApprobateur(droitApprobateur2);
		dp2.setProfil(appro);
		droitApprobateur2.getDroitProfils().add(dp2);

		// on ajoute les viseurs a chaque approbateur
		Droit droitVis1 = new Droit();
		droitVis1.setIdAgent(idAgentVis1);
		DroitProfil dp3 = new DroitProfil();
		dp3.setDroit(droitVis1);
		dp3.setDroitApprobateur(droitApprobateur1);
		dp3.setProfil(p1);
		droitApprobateur1.getDroitProfils().add(dp3);
		Droit droitVis2 = new Droit();
		droitVis2.setIdAgent(idAgentVis2);
		DroitProfil dp4 = new DroitProfil();
		dp4.setDroit(droitVis2);
		dp4.setDroitApprobateur(droitApprobateur2);
		dp4.setProfil(p1);
		droitApprobateur2.getDroitProfils().add(dp4);

		// on persist les objets
		absEntityManager.persist(droitApprobateur1);
		absEntityManager.persist(droitApprobateur2);
		absEntityManager.persist(droitVis1);
		absEntityManager.persist(droitVis2);

		// When
		assertTrue(repository.isUserViseurOfApprobateur(idAgentAppro1, idAgentVis1));
		assertFalse(repository.isUserViseurOfApprobateur(idAgentAppro2, idAgentVis1));
		assertTrue(repository.isUserViseurOfApprobateur(idAgentAppro2, idAgentVis2));
		assertFalse(repository.isUserViseurOfApprobateur(idAgentAppro1, idAgentVis2));

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getAgentDroitFetchAgents_withResult() {

		Date d = new Date();

		Droit droit = new Droit();
		droit.setDateModification(d);
		droit.setIdAgent(9008767);
		absEntityManager.persist(droit);

		Set<DroitDroitsAgent> droitDroitsAgents = new HashSet<DroitDroitsAgent>();
		DroitDroitsAgent droitDroitsAgent = new DroitDroitsAgent();
		droitDroitsAgent.setDroit(droit);
		droitDroitsAgents.add(droitDroitsAgent);

		DroitsAgent agent = new DroitsAgent();
		agent.setIdAgent(9008768);
		agent.setIdDroitsAgent(1);
		agent.setCodeService("TEST");
		agent.setLibelleService("DASP Pôle Administratif et test");
		agent.setDateModification(new Date());
		agent.setDroitDroitsAgent(droitDroitsAgents);
		absEntityManager.persist(agent);

		Droit result = repository.getAgentDroitFetchAgents(9008767);

		assertNotNull(result);
		assertEquals(d, result.getDateModification());
		for (DroitDroitsAgent ddaResult : result.getDroitDroitsAgent()) {
			assertEquals("TEST", ddaResult.getDroitsAgent().getCodeService());
			assertEquals("DASP Pôle Administratif et test", ddaResult.getDroitsAgent().getLibelleService());
		}

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getAgentDroitFetchAgents_noResult() {

		Date d = new Date();

		Droit droit = new Droit();
		droit.setDateModification(d);
		droit.setIdAgent(9008767);
		absEntityManager.persist(droit);

		Set<DroitDroitsAgent> droitDroitsAgents = new HashSet<DroitDroitsAgent>();
		DroitDroitsAgent droitDroitsAgent = new DroitDroitsAgent();
		droitDroitsAgent.setDroit(droit);
		droitDroitsAgents.add(droitDroitsAgent);

		DroitsAgent agent = new DroitsAgent();
		agent.setIdAgent(9008767);
		agent.setIdDroitsAgent(1);
		agent.setCodeService("DEAB");
		agent.setLibelleService("DASP Pôle Administratif et Budgétaire");
		agent.setDateModification(new Date());
		agent.setDroitDroitsAgent(droitDroitsAgents);
		absEntityManager.persist(agent);

		Droit result = repository.getAgentDroitFetchAgents(9008768);

		assertNull(result);

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getDroitProfilByAgent_withResultApprobateur() {

		Profil p1 = new Profil();
		p1.setLibelle("OPERATEUR");
		absEntityManager.persist(p1);

		Droit droitApprobateur = new Droit();
		droitApprobateur.setDateModification(new Date());
		droitApprobateur.setIdAgent(9005127);
		absEntityManager.persist(droitApprobateur);

		Droit droit = new Droit();
		droit.setDateModification(new Date());
		droit.setIdAgent(9005126);
		absEntityManager.persist(droit);

		DroitProfil dp = new DroitProfil();
		dp.setProfil(p1);
		dp.setDroitApprobateur(droitApprobateur);
		dp.setDroit(droit);
		absEntityManager.persist(dp);

		DroitProfil result = repository.getDroitProfilByAgent(droitApprobateur.getIdAgent(), droit.getIdAgent());

		assertNotNull(result);
		assertEquals("OPERATEUR", result.getProfil().getLibelle());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getDroitProfilByAgent_withResultOperateur() {

		Profil p1 = new Profil();
		p1.setLibelle("APPROBATEUR");
		absEntityManager.persist(p1);

		Droit droit = new Droit();
		droit.setDateModification(new Date());
		droit.setIdAgent(9005126);
		absEntityManager.persist(droit);

		DroitProfil dp = new DroitProfil();
		dp.setProfil(p1);
		dp.setDroitApprobateur(droit);
		dp.setDroit(droit);
		absEntityManager.persist(dp);

		DroitProfil result = repository.getDroitProfilByAgent(droit.getIdAgent(), droit.getIdAgent());

		assertNotNull(result);
		assertEquals("APPROBATEUR", result.getProfil().getLibelle());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getDroitProfilByAgent_noResultOperateur() {

		Profil p1 = new Profil();
		p1.setLibelle("OPERATEUR");
		absEntityManager.persist(p1);

		Droit droitApprobateur = new Droit();
		droitApprobateur.setDateModification(new Date());
		droitApprobateur.setIdAgent(9005127);
		absEntityManager.persist(droitApprobateur);

		Droit droit = new Droit();
		droit.setDateModification(new Date());
		droit.setIdAgent(9005126);
		absEntityManager.persist(droit);

		DroitProfil dp = new DroitProfil();
		dp.setProfil(p1);
		dp.setDroitApprobateur(droitApprobateur);
		dp.setDroit(droit);
		absEntityManager.persist(dp);

		DroitProfil result = repository.getDroitProfilByAgent(droit.getIdAgent(), droit.getIdAgent());

		assertNull(result);

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getDroitsAgent_ReturnNull() {

		// When
		DroitsAgent result = repository.getDroitsAgent(9005138);

		// Then
		assertEquals(null, result);
	}

	@Test
	@Transactional("absTransactionManager")
	public void getDroitsAgent_ReturnResult() {
		Integer idAgent = 9005138;

		Profil p1 = new Profil();
		p1.setLibelle("APPROBATEUR");
		absEntityManager.persist(p1);

		DroitsAgent da = new DroitsAgent();
		da.setIdAgent(idAgent);
		da.setLibelleService("TEST");

		Droit d = new Droit();
		d.setIdAgent(idAgent);

		DroitProfil dp = new DroitProfil();
		dp.setProfil(p1);
		dp.setDroitApprobateur(d);
		dp.setDroit(d);

		DroitDroitsAgent dda = new DroitDroitsAgent();
		dda.setDroit(d);
		dda.setDroitProfil(dp);
		dda.setDroitsAgent(da);
		da.getDroitDroitsAgent().add(dda);

		absEntityManager.persist(d);
		absEntityManager.persist(dp);
		absEntityManager.persist(da);
		absEntityManager.persist(dda);

		// When
		DroitsAgent result = repository.getDroitsAgent(idAgent);

		// Then
		assertEquals(1, result.getDroitDroitsAgent().size());
		assertEquals(idAgent, result.getIdAgent());
		assertEquals("TEST", result.getLibelleService());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getDroitProfilApprobateur_ReturnNull() {

		// When
		DroitProfil result = repository.getDroitProfilApprobateur(9005138);

		// Then
		assertEquals(null, result);
	}

	@Test
	@Transactional("absTransactionManager")
	public void getDroitProfilApprobateur_ReturnResult() {
		Integer idAgent = 9005138;

		Profil p1 = new Profil();
		p1.setLibelle("APPROBATEUR");
		absEntityManager.persist(p1);

		Droit d = new Droit();
		d.setIdAgent(idAgent);

		DroitProfil dp = new DroitProfil();
		dp.setProfil(p1);
		dp.setDroitApprobateur(d);
		dp.setDroit(d);

		absEntityManager.persist(d);
		absEntityManager.persist(dp);

		// When
		DroitProfil result = repository.getDroitProfilApprobateur(idAgent);

		// Then
		assertEquals(idAgent, result.getDroit().getIdAgent());
		assertEquals(idAgent, result.getDroitApprobateur().getIdAgent());
		assertEquals("APPROBATEUR", result.getProfil().getLibelle());

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getApprobateurOfAgent_ReturnNoResult() {
		DroitsAgent da = new DroitsAgent();
		da.setIdAgent(9005131);
		absEntityManager.persist(da);

		// When
		Droit result = repository.getApprobateurOfAgent(da);

		// Then
		assertNull(result);

		absEntityManager.flush();
		absEntityManager.clear();
	}

	@Test
	@Transactional("absTransactionManager")
	public void getApprobateurOfAgent_ReturnResult() {
		DroitsAgent da = new DroitsAgent();
		da.setIdAgent(9005131);
		da.setCodeService("DCCB");
		da.setDateModification(new Date());
		da.setLibelleService("SED");

		Profil pAppro = new Profil();
		pAppro.setLibelle("APPROBATEUR");

		Droit droitAppro = new Droit();
		droitAppro.setDateModification(new Date());
		droitAppro.setIdAgent(9005138);

		DroitProfil dpAppr = new DroitProfil();
		dpAppr.setDroit(droitAppro);
		dpAppr.setDroitApprobateur(droitAppro);
		dpAppr.setProfil(pAppro);

		DroitDroitsAgent dda = new DroitDroitsAgent();
		dda.setDroit(droitAppro);
		dda.setDroitProfil(dpAppr);
		dda.setDroitsAgent(da);

		da.getDroitDroitsAgent().add(dda);
		dpAppr.getDroitDroitsAgent().add(dda);
		droitAppro.getDroitDroitsAgent().add(dda);
		droitAppro.getDroitProfils().add(dpAppr);

		absEntityManager.persist(da);
		absEntityManager.persist(pAppro);
		absEntityManager.persist(droitAppro);
		absEntityManager.persist(dpAppr);
		absEntityManager.persist(dda);

		absEntityManager.flush();

		// When
		Droit result = repository.getApprobateurOfAgent(da);

		// Then
		assertNotNull(result);
		assertEquals("9005138", result.getIdAgent().toString());

		absEntityManager.flush();
		absEntityManager.clear();
	}
	
	
	@Test
	@Transactional("absTransactionManager")
	public void isViseurOfAgent_returnTrue() {
		
		DroitsAgent da = new DroitsAgent();
		da.setIdAgent(9005131);
		da.setCodeService("DCCB");
		da.setDateModification(new Date());
		da.setLibelleService("SED");

		Profil pAppro = new Profil();
		pAppro.setLibelle("VISEUR");

		Droit droitAppro = new Droit();
		droitAppro.setDateModification(new Date());
		droitAppro.setIdAgent(9005138);

		DroitProfil dpAppr = new DroitProfil();
		dpAppr.setDroit(droitAppro);
		dpAppr.setDroitApprobateur(droitAppro);
		dpAppr.setProfil(pAppro);

		DroitDroitsAgent dda = new DroitDroitsAgent();
		dda.setDroit(droitAppro);
		dda.setDroitProfil(dpAppr);
		dda.setDroitsAgent(da);

		da.getDroitDroitsAgent().add(dda);
		dpAppr.getDroitDroitsAgent().add(dda);
		droitAppro.getDroitDroitsAgent().add(dda);
		droitAppro.getDroitProfils().add(dpAppr);

		absEntityManager.persist(da);
		absEntityManager.persist(pAppro);
		absEntityManager.persist(droitAppro);
		absEntityManager.persist(dpAppr);
		absEntityManager.persist(dda);

		absEntityManager.flush();
		
		boolean result = repository.isViseurOfAgent(9005138, 9005131);
		
		assertTrue(result);
		
		absEntityManager.flush();
		absEntityManager.clear();
	}
	
	@Test
	@Transactional("absTransactionManager")
	public void isViseurOfAgent_returnFalse() {
		
		DroitsAgent da = new DroitsAgent();
		da.setIdAgent(9005131);
		da.setCodeService("DCCB");
		da.setDateModification(new Date());
		da.setLibelleService("SED");

		Profil pAppro = new Profil();
		pAppro.setLibelle("APPROBATEUR");

		Droit droitAppro = new Droit();
		droitAppro.setDateModification(new Date());
		droitAppro.setIdAgent(9005138);

		DroitProfil dpAppr = new DroitProfil();
		dpAppr.setDroit(droitAppro);
		dpAppr.setDroitApprobateur(droitAppro);
		dpAppr.setProfil(pAppro);

		DroitDroitsAgent dda = new DroitDroitsAgent();
		dda.setDroit(droitAppro);
		dda.setDroitProfil(dpAppr);
		dda.setDroitsAgent(da);

		da.getDroitDroitsAgent().add(dda);
		dpAppr.getDroitDroitsAgent().add(dda);
		droitAppro.getDroitDroitsAgent().add(dda);
		droitAppro.getDroitProfils().add(dpAppr);

		absEntityManager.persist(da);
		absEntityManager.persist(pAppro);
		absEntityManager.persist(droitAppro);
		absEntityManager.persist(dpAppr);
		absEntityManager.persist(dda);

		absEntityManager.flush();
		
		boolean result = repository.isViseurOfAgent(9005138, 9005131);
		
		assertFalse(result);
		
		absEntityManager.flush();
		absEntityManager.clear();
	}
	
	@Test
	@Transactional("absTransactionManager")
	public void isApprobateurOfAgent_returnFalse() {
		
		DroitsAgent da = new DroitsAgent();
		da.setIdAgent(9005131);
		da.setCodeService("DCCB");
		da.setDateModification(new Date());
		da.setLibelleService("SED");

		Profil pAppro = new Profil();
		pAppro.setLibelle("VISEUR");

		Droit droitAppro = new Droit();
		droitAppro.setDateModification(new Date());
		droitAppro.setIdAgent(9005138);

		DroitProfil dpAppr = new DroitProfil();
		dpAppr.setDroit(droitAppro);
		dpAppr.setDroitApprobateur(droitAppro);
		dpAppr.setProfil(pAppro);

		DroitDroitsAgent dda = new DroitDroitsAgent();
		dda.setDroit(droitAppro);
		dda.setDroitProfil(dpAppr);
		dda.setDroitsAgent(da);

		da.getDroitDroitsAgent().add(dda);
		dpAppr.getDroitDroitsAgent().add(dda);
		droitAppro.getDroitDroitsAgent().add(dda);
		droitAppro.getDroitProfils().add(dpAppr);

		absEntityManager.persist(da);
		absEntityManager.persist(pAppro);
		absEntityManager.persist(droitAppro);
		absEntityManager.persist(dpAppr);
		absEntityManager.persist(dda);

		absEntityManager.flush();
		
		boolean result = repository.isApprobateurOfAgent(9005138, 9005131);
		
		assertFalse(result);
		
		absEntityManager.flush();
		absEntityManager.clear();
	}
	
	@Test
	@Transactional("absTransactionManager")
	public void isApprobateurOfAgent_returnTrue() {
		
		DroitsAgent da = new DroitsAgent();
		da.setIdAgent(9005131);
		da.setCodeService("DCCB");
		da.setDateModification(new Date());
		da.setLibelleService("SED");

		Profil pAppro = new Profil();
		pAppro.setLibelle("APPROBATEUR");

		Droit droitAppro = new Droit();
		droitAppro.setDateModification(new Date());
		droitAppro.setIdAgent(9005138);

		DroitProfil dpAppr = new DroitProfil();
		dpAppr.setDroit(droitAppro);
		dpAppr.setDroitApprobateur(droitAppro);
		dpAppr.setProfil(pAppro);

		DroitDroitsAgent dda = new DroitDroitsAgent();
		dda.setDroit(droitAppro);
		dda.setDroitProfil(dpAppr);
		dda.setDroitsAgent(da);

		da.getDroitDroitsAgent().add(dda);
		dpAppr.getDroitDroitsAgent().add(dda);
		droitAppro.getDroitDroitsAgent().add(dda);
		droitAppro.getDroitProfils().add(dpAppr);

		absEntityManager.persist(da);
		absEntityManager.persist(pAppro);
		absEntityManager.persist(droitAppro);
		absEntityManager.persist(dpAppr);
		absEntityManager.persist(dda);

		absEntityManager.flush();
		
		boolean result = repository.isApprobateurOfAgent(9005138, 9005131);
		
		assertTrue(result);
		
		absEntityManager.flush();
		absEntityManager.clear();
	}
	
	@Test
	@Transactional("absTransactionManager")
	public void isApprobateurOfAgent_isDelegataire_returnTrue() {
		
		DroitsAgent da = new DroitsAgent();
		da.setIdAgent(9005131);
		da.setCodeService("DCCB");
		da.setDateModification(new Date());
		da.setLibelleService("SED");

		Profil pAppro = new Profil();
		pAppro.setLibelle("DELEGATAIRE");

		Droit droitAppro = new Droit();
		droitAppro.setDateModification(new Date());
		droitAppro.setIdAgent(9005138);
		
		Droit droitDelegataire = new Droit();
		droitDelegataire.setDateModification(new Date());
		droitDelegataire.setIdAgent(9005139);

		DroitProfil dpAppr = new DroitProfil();
		dpAppr.setDroit(droitDelegataire);
		dpAppr.setDroitApprobateur(droitAppro);
		dpAppr.setProfil(pAppro);

		DroitDroitsAgent dda = new DroitDroitsAgent();
		dda.setDroit(droitAppro);
		dda.setDroitProfil(dpAppr);
		dda.setDroitsAgent(da);

		da.getDroitDroitsAgent().add(dda);
		dpAppr.getDroitDroitsAgent().add(dda);
		droitAppro.getDroitDroitsAgent().add(dda);
		droitAppro.getDroitProfils().add(dpAppr);

		absEntityManager.persist(da);
		absEntityManager.persist(pAppro);
		absEntityManager.persist(droitAppro);
		absEntityManager.persist(droitDelegataire);
		absEntityManager.persist(dpAppr);
		absEntityManager.persist(dda);

		absEntityManager.flush();
		
		boolean result = repository.isApprobateurOfAgent(9005139, 9005131);
		
		assertTrue(result);
		
		absEntityManager.flush();
		absEntityManager.clear();
	}
	
	@Test
	@Transactional("absTransactionManager")
	public void isApprobateurOfAgent_isDelegataire_returnFalse() {
		
		DroitsAgent da = new DroitsAgent();
		da.setIdAgent(9005131);
		da.setCodeService("DCCB");
		da.setDateModification(new Date());
		da.setLibelleService("SED");

		Profil pAppro = new Profil();
		pAppro.setLibelle("DELEGATAIRE");

		Droit droitAppro = new Droit();
		droitAppro.setDateModification(new Date());
		droitAppro.setIdAgent(9005138);
		
		Droit droitDelegataire = new Droit();
		droitDelegataire.setDateModification(new Date());
		droitDelegataire.setIdAgent(9005139);

		DroitProfil dpAppr = new DroitProfil();
		dpAppr.setDroit(droitDelegataire);
		dpAppr.setDroitApprobateur(droitAppro);
		dpAppr.setProfil(pAppro);

		DroitDroitsAgent dda = new DroitDroitsAgent();
		dda.setDroit(droitAppro);
		dda.setDroitProfil(dpAppr);
		dda.setDroitsAgent(da);

		da.getDroitDroitsAgent().add(dda);
		dpAppr.getDroitDroitsAgent().add(dda);
		droitAppro.getDroitDroitsAgent().add(dda);
		droitAppro.getDroitProfils().add(dpAppr);

		absEntityManager.persist(da);
		absEntityManager.persist(pAppro);
		absEntityManager.persist(droitAppro);
		absEntityManager.persist(droitDelegataire);
		absEntityManager.persist(dpAppr);
		absEntityManager.persist(dda);

		absEntityManager.flush();
		
		boolean result = repository.isApprobateurOfAgent(9005138, 9005131);
		
		assertFalse(result);
		
		absEntityManager.flush();
		absEntityManager.clear();
	}
}
