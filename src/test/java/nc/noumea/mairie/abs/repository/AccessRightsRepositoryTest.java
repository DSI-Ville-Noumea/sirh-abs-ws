package nc.noumea.mairie.abs.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nc.noumea.mairie.abs.domain.Droit;
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
		assertEquals(null, result.getIdDroit());
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

		//appro1		
		Droit droitApprobateur1 = new Droit();
		droitApprobateur1.setIdDroit(1);
		droitApprobateur1.setIdAgent(9005138);
		
		//appro2
		Droit droitApprobateur2 = new Droit();
		droitApprobateur2.setIdAgent(9003041);
		droitApprobateur2.setIdDroit(3);
		
		//appro3
		Droit droitApprobateur3 = new Droit();
		droitApprobateur3.setIdAgent(9002990);
		droitApprobateur3.setIdDroit(4);
		
		//delegataire		
		Droit droitDelegataire = new Droit();
		droitDelegataire.setIdAgent(9005131);
		droitDelegataire.setIdDroit(2);
		
		//on cree un profil a l'appro 1
		DroitProfil dp1 = new DroitProfil();
		dp1.setDroit(droitApprobateur1);
		dp1.setDroitApprobateur(droitApprobateur1);
		dp1.setProfil(p1);
		droitApprobateur1.getDroitProfils().add(dp1);
		
		//on cree un profil a l'appro 2		
		DroitProfil dp2 = new DroitProfil();
		dp2.setDroit(droitApprobateur2);
		dp2.setDroitApprobateur(droitApprobateur2);
		dp2.setProfil(p1);
		droitApprobateur2.getDroitProfils().add(dp2);
		
		//on cree un profil a l'appro 3
		DroitProfil dp3 = new DroitProfil();
		dp3.setDroit(droitApprobateur3);
		dp3.setDroitApprobateur(droitApprobateur3);
		dp3.setProfil(p1);
		droitApprobateur3.getDroitProfils().add(dp3);
		
		//on cree un profil au delegataire pour l'appro 1
		DroitProfil dp4 = new DroitProfil();
		dp4.setDroit(droitDelegataire);
		dp4.setDroitApprobateur(droitApprobateur1);
		dp4.setProfil(p2);
		droitDelegataire.getDroitProfils().add(dp4);
		
		//on cree un profil au delegataire pour l'appro 2
		DroitProfil dp5 = new DroitProfil();
		dp5.setDroit(droitDelegataire);
		dp5.setDroitApprobateur(droitApprobateur2);
		dp5.setProfil(p2);
		droitDelegataire.getDroitProfils().add(dp5);
		
		//on cree un profil au delegataire pour l'appro 3
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

		Droit droit = new Droit();
		droit.setDateModification(new Date());
		droit.setIdAgent(9008767);
		droit.setIdDroit(1);
		absEntityManager.persist(droit);

		Set<Droit> droits = new HashSet<Droit>();
		droits.add(droit);

		DroitsAgent agent = new DroitsAgent();
		agent.setIdAgent(9008767);
		agent.setIdDroitsAgent(1);
		agent.setCodeService("DEAB");
		agent.setLibelleService("DASP Pôle Administratif et Budgétaire");
		agent.setDateModification(new Date());
		agent.setDroits(droits);
		absEntityManager.persist(agent);

		List<DroitsAgent> result = repository.getListOfAgentsToInputOrApprove(9008767, "DEAB");

		assertEquals(1, result.size());
		assertEquals("9008767", result.get(0).getIdAgent().toString());

		absEntityManager.flush();
		absEntityManager.clear();
	}

}
