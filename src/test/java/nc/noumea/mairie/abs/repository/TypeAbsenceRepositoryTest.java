package nc.noumea.mairie.abs.repository;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nc.noumea.mairie.abs.domain.RefGroupeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeSaisi;
import nc.noumea.mairie.abs.domain.RefTypeSaisiCongeAnnuel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/META-INF/spring/applicationContext-test.xml" })
public class TypeAbsenceRepositoryTest {

	@Autowired
	TypeAbsenceRepository repository;

	@PersistenceContext(unitName = "absPersistenceUnit")
	private EntityManager absEntityManager;

	@Test
	@Transactional("absTransactionManager")
	public void getListeTypAbsence_1Result() {

		RefTypeAbsence rta = new RefTypeAbsence();
		rta.setActif(true);

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setDescription("description");
		typeSaisi.setInfosComplementaires("infosComplementaires");
		typeSaisi.setMessageAlerte("messageAlerte");
		typeSaisi.setQuotaMax(10);
		typeSaisi.setType(rta);
		typeSaisi.setUniteDecompte("uniteDecompte");

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(1);
		groupe.setCode("code");
		groupe.setLibelle("libelle");

		rta.setLabel("label");
		rta.setGroupe(groupe);
		rta.setTypeSaisi(typeSaisi);

		absEntityManager.persist(groupe);
		absEntityManager.persist(rta);
		absEntityManager.persist(typeSaisi);

		List<RefTypeAbsence> result = repository.getListeTypAbsence(null);

		assertEquals(1, result.size());
		assertEquals("label", result.get(0).getLabel());

		assertEquals("code", result.get(0).getGroupe().getCode());
		assertEquals("libelle", result.get(0).getGroupe().getLibelle());

		assertEquals("description", result.get(0).getTypeSaisi().getDescription());

	}

	@Test
	@Transactional("absTransactionManager")
	public void getListeTypAbsence_2Results() {

		RefTypeAbsence rta = new RefTypeAbsence();

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setDescription("description");
		typeSaisi.setInfosComplementaires("infosComplementaires");
		typeSaisi.setMessageAlerte("messageAlerte");
		typeSaisi.setQuotaMax(10);
		typeSaisi.setType(rta);
		typeSaisi.setUniteDecompte("uniteDecompte");

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(1);
		groupe.setCode("code");
		groupe.setLibelle("libelle");

		rta.setLabel("label");
		rta.setGroupe(groupe);
		rta.setTypeSaisi(typeSaisi);
		rta.setActif(true);

		absEntityManager.persist(groupe);
		absEntityManager.persist(rta);
		absEntityManager.persist(typeSaisi);

		RefTypeAbsence rta2 = new RefTypeAbsence();
		rta2.setActif(true);

		RefTypeSaisi typeSaisi2 = new RefTypeSaisi();
		typeSaisi2.setDescription("description 2");
		typeSaisi2.setInfosComplementaires("infosComplementaires 2");
		typeSaisi2.setMessageAlerte("messageAlerte 2");
		typeSaisi2.setQuotaMax(20);
		typeSaisi2.setType(rta2);
		typeSaisi2.setUniteDecompte("uniteDecompte 2");

		RefGroupeAbsence groupe2 = new RefGroupeAbsence();
		groupe2.setIdRefGroupeAbsence(2);
		groupe2.setCode("code 2");
		groupe2.setLibelle("libelle 2");

		rta2.setLabel("label 2");
		rta2.setGroupe(groupe2);
		rta2.setTypeSaisi(typeSaisi2);

		absEntityManager.persist(groupe2);
		absEntityManager.persist(rta2);
		absEntityManager.persist(typeSaisi2);

		List<RefTypeAbsence> result = repository.getListeTypAbsence(null);

		assertEquals(2, result.size());
		assertEquals("label", result.get(0).getLabel());
		assertEquals("code", result.get(0).getGroupe().getCode());
		assertEquals("libelle", result.get(0).getGroupe().getLibelle());
		assertEquals("description", result.get(0).getTypeSaisi().getDescription());

		assertEquals("label 2", result.get(1).getLabel());
		assertEquals("code 2", result.get(1).getGroupe().getCode());
		assertEquals("libelle 2", result.get(1).getGroupe().getLibelle());
		assertEquals("description 2", result.get(1).getTypeSaisi().getDescription());
	}
	
	@Test
	@Transactional("absTransactionManager")
	public void getListeTypeSaisiCongeAnnuel_1Result() {

		RefTypeSaisiCongeAnnuel typeSaisi = new RefTypeSaisiCongeAnnuel();
		typeSaisi.setCodeBaseHoraireAbsence("A");
		absEntityManager.persist(typeSaisi);

		List<RefTypeSaisiCongeAnnuel> result = repository.getListeTypeSaisiCongeAnnuel();

		assertEquals(1, result.size());
		assertEquals("A", result.get(0).getCodeBaseHoraireAbsence());

	}
	
	@Test
	@Transactional("absTransactionManager")
	public void getListeTypeSaisiCongeAnnuel_2Result() {

		RefTypeSaisiCongeAnnuel typeSaisi = new RefTypeSaisiCongeAnnuel();
		typeSaisi.setCodeBaseHoraireAbsence("A");
		absEntityManager.persist(typeSaisi);

		RefTypeSaisiCongeAnnuel typeSaisi2 = new RefTypeSaisiCongeAnnuel();
		typeSaisi2.setCodeBaseHoraireAbsence("C");
		absEntityManager.persist(typeSaisi2);

		List<RefTypeSaisiCongeAnnuel> result = repository.getListeTypeSaisiCongeAnnuel();

		assertEquals(2, result.size());
		assertEquals("A", result.get(0).getCodeBaseHoraireAbsence());
		assertEquals("C", result.get(1).getCodeBaseHoraireAbsence());

	}
}
