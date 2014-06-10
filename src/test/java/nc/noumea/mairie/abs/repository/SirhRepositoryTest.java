package nc.noumea.mairie.abs.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.domain.SpSold;
import nc.noumea.mairie.domain.Spadmn;
import nc.noumea.mairie.domain.SpadmnId;
import nc.noumea.mairie.domain.Spcarr;
import nc.noumea.mairie.domain.SpcarrId;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/META-INF/spring/applicationContext-test.xml" })
public class SirhRepositoryTest {

	@Autowired
	SirhRepository repository;

	@PersistenceContext(unitName = "sirhPersistenceUnit")
	private EntityManager sirhEntityManager;

	@Test
	@Transactional("sirhTransactionManager")
	public void getSpsold_ReturnNull() {

		// When
		SpSold result = repository.getSpsold(9005138);

		// Then
		assertEquals(null, result);
	}

	@Test
	@Transactional("sirhTransactionManager")
	public void getSpsold_ReturnResult() {

		SpSold solde = new SpSold();
		solde.setNomatr(5138);
		solde.setSoldeAnneeEnCours(72.0);
		solde.setSoldeAnneePrec(12.5);
		sirhEntityManager.persist(solde);

		sirhEntityManager.flush();

		// When
		SpSold result = repository.getSpsold(9005138);

		// Then
		assertEquals("5138", result.getNomatr().toString());
		assertEquals("72.0", result.getSoldeAnneeEnCours().toString());
		assertEquals("12.5", result.getSoldeAnneePrec().toString());

		sirhEntityManager.flush();
		sirhEntityManager.clear();
	}

	@Test
	@Transactional("sirhTransactionManager")
	public void getAgentCurrentPosition_returnResult() {

		SpadmnId id = new SpadmnId();
		id.setDatdeb(20130901);
		id.setNomatr(5138);
		Spadmn adm = new Spadmn();
		adm.setId(id);
		adm.setCdpadm("");
		adm.setDatfin(20130930);

		sirhEntityManager.persist(adm);

		AgentGeneriqueDto agent = new AgentGeneriqueDto();
		agent.setNomatr(5138);

		Spadmn result = repository.getAgentCurrentPosition(agent.getNomatr(), new LocalDate(2013, 9, 22).toDate());

		assertNotNull(result);

		sirhEntityManager.flush();
		sirhEntityManager.clear();
	}

	@Test
	@Transactional("sirhTransactionManager")
	public void getAgentCurrentPosition_returnNoResult() {

		SpadmnId id = new SpadmnId();
		id.setDatdeb(20130901);
		id.setNomatr(5138);
		Spadmn adm = new Spadmn();
		adm.setId(id);
		adm.setCdpadm("");
		adm.setDatfin(20130930);

		sirhEntityManager.persist(adm);

		AgentGeneriqueDto agent = new AgentGeneriqueDto();
		agent.setNomatr(5138);

		Spadmn result = repository.getAgentCurrentPosition(agent.getNomatr(), new LocalDate(2013, 10, 22).toDate());

		assertNull(result);

		sirhEntityManager.flush();
		sirhEntityManager.clear();
	}

	@Test
	@Transactional("sirhTransactionManager")
	public void getAgentCurrentCarriere_returnResult() {

		SpcarrId id = new SpcarrId();
			id.setDatdeb(20130901);
			id.setNomatr(5138);
		Spcarr adm = new Spcarr();
			adm.setId(id);
			adm.setCdcate(1);
			adm.setDateFin(20130930);

		sirhEntityManager.persist(adm);

		AgentGeneriqueDto agent = new AgentGeneriqueDto();
			agent.setIdAgent(9005138);
			agent.setNomatr(5138);

		Spcarr result = repository.getAgentCurrentCarriere(agent.getNomatr(), new LocalDate(2013, 9, 22).toDate());

		assertNotNull(result);

		sirhEntityManager.flush();
		sirhEntityManager.clear();
	}

	@Test
	@Transactional("sirhTransactionManager")
	public void getAgentCurrentCarriere_returnNoResult() {

		SpcarrId id = new SpcarrId();
			id.setDatdeb(20130901);
			id.setNomatr(5138);
		Spcarr adm = new Spcarr();
			adm.setId(id);
			adm.setCdcate(1);
			adm.setDateFin(20130930);
		sirhEntityManager.persist(adm);

		AgentGeneriqueDto agent = new AgentGeneriqueDto();
			agent.setIdAgent(9005138);
			agent.setNomatr(5138);
			agent.setNomPatronymique("patro");
			agent.setPrenom("prenom");
			agent.setPrenomUsage("prenom");

		Spcarr result = repository.getAgentCurrentCarriere(agent.getIdAgent(), new LocalDate(2013, 10, 22).toDate());

		assertNull(result);

		sirhEntityManager.flush();
		sirhEntityManager.clear();
	}
}
