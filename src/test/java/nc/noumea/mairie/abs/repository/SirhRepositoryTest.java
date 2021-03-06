package nc.noumea.mairie.abs.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.domain.Spadmn;
import nc.noumea.mairie.domain.SpadmnId;
import nc.noumea.mairie.domain.Spcarr;
import nc.noumea.mairie.domain.SpcarrId;
import nc.noumea.mairie.domain.Spcc;
import nc.noumea.mairie.domain.SpccId;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/META-INF/spring/applicationContext-test.xml" })
public class SirhRepositoryTest {

	@Autowired
	SirhRepository			repository;

	@PersistenceContext(unitName = "sirhPersistenceUnit")
	private EntityManager	sirhEntityManager;

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

	@Test
	@Transactional("sirhTransactionManager")
	public void getSpcc_returnNoResult_badNoMatr() {

		SpccId id = new SpccId();
		id.setDatjou(20130901);
		id.setNomatr(5138);
		Spcc spcc = new Spcc();
		spcc.setId(id);
		spcc.setCode(1);
		sirhEntityManager.persist(spcc);

		AgentGeneriqueDto agent = new AgentGeneriqueDto();
		agent.setIdAgent(9005138);
		agent.setNomatr(5138);
		agent.setNomPatronymique("patro");
		agent.setPrenom("prenom");
		agent.setPrenomUsage("prenom");

		Spcc result = repository.getSpcc(5199, new DateTime(2013, 9, 1, 0, 0, 0).toDate(), 1);

		assertNull(result);

		sirhEntityManager.flush();
		sirhEntityManager.clear();
	}

	@Test
	@Transactional("sirhTransactionManager")
	public void getSpcc_returnNoResult_badDateJour() {

		SpccId id = new SpccId();
		id.setDatjou(20130901);
		id.setNomatr(5138);
		Spcc spcc = new Spcc();
		spcc.setId(id);
		spcc.setCode(1);
		sirhEntityManager.persist(spcc);

		AgentGeneriqueDto agent = new AgentGeneriqueDto();
		agent.setIdAgent(9005138);
		agent.setNomatr(5138);
		agent.setNomPatronymique("patro");
		agent.setPrenom("prenom");
		agent.setPrenomUsage("prenom");

		Spcc result = repository.getSpcc(5138, new DateTime(2013, 9, 11, 0, 0, 0).toDate(), 1);

		assertNull(result);

		sirhEntityManager.flush();
		sirhEntityManager.clear();
	}

	@Test
	@Transactional("sirhTransactionManager")
	public void getSpcc_returnNoResult_badCode() {

		SpccId id = new SpccId();
		id.setDatjou(20130901);
		id.setNomatr(5138);
		Spcc spcc = new Spcc();
		spcc.setId(id);
		spcc.setCode(1);
		sirhEntityManager.persist(spcc);

		AgentGeneriqueDto agent = new AgentGeneriqueDto();
		agent.setIdAgent(9005138);
		agent.setNomatr(5138);
		agent.setNomPatronymique("patro");
		agent.setPrenom("prenom");
		agent.setPrenomUsage("prenom");

		Spcc result = repository.getSpcc(5138, new DateTime(2013, 9, 1, 0, 0, 0).toDate(), 2);

		assertNull(result);

		sirhEntityManager.flush();
		sirhEntityManager.clear();
	}

	@Test
	@Transactional("sirhTransactionManager")
	public void getSpcc_returnResult_ok() {

		SpccId id = new SpccId();
		id.setDatjou(20130901);
		id.setNomatr(5138);
		Spcc spcc = new Spcc();
		spcc.setId(id);
		spcc.setCode(1);
		sirhEntityManager.persist(spcc);

		AgentGeneriqueDto agent = new AgentGeneriqueDto();
		agent.setIdAgent(9005138);
		agent.setNomatr(5138);
		agent.setNomPatronymique("patro");
		agent.setPrenom("prenom");
		agent.setPrenomUsage("prenom");

		Spcc result = repository.getSpcc(5138, new DateTime(2013, 9, 1, 0, 0, 0).toDate(), 1);

		assertNotNull(result);

		sirhEntityManager.flush();
		sirhEntityManager.clear();
	}

	@Test
	@Transactional("sirhTransactionManager")
	public void getSpccWithDatJouAndNoMatr_returnNoResult_badNoMatr() {

		SpccId id = new SpccId();
		id.setDatjou(20130901);
		id.setNomatr(5138);
		Spcc spcc = new Spcc();
		spcc.setId(id);
		spcc.setCode(1);
		sirhEntityManager.persist(spcc);

		AgentGeneriqueDto agent = new AgentGeneriqueDto();
		agent.setIdAgent(9005138);
		agent.setNomatr(5138);
		agent.setNomPatronymique("patro");
		agent.setPrenom("prenom");
		agent.setPrenomUsage("prenom");

		Spcc result = repository.getSpcc(5199, new DateTime(2013, 9, 1, 0, 0, 0).toDate());

		assertNull(result);

		sirhEntityManager.flush();
		sirhEntityManager.clear();
	}

	@Test
	@Transactional("sirhTransactionManager")
	public void getSpccWithDatJouAndNoMatr_returnNoResult_badDateJour() {

		SpccId id = new SpccId();
		id.setDatjou(20130901);
		id.setNomatr(5138);
		Spcc spcc = new Spcc();
		spcc.setId(id);
		spcc.setCode(1);
		sirhEntityManager.persist(spcc);

		AgentGeneriqueDto agent = new AgentGeneriqueDto();
		agent.setIdAgent(9005138);
		agent.setNomatr(5138);
		agent.setNomPatronymique("patro");
		agent.setPrenom("prenom");
		agent.setPrenomUsage("prenom");

		Spcc result = repository.getSpcc(5138, new DateTime(2013, 9, 11, 0, 0, 0).toDate());

		assertNull(result);

		sirhEntityManager.flush();
		sirhEntityManager.clear();
	}

	@Test
	@Transactional("sirhTransactionManager")
	public void getSpccWithDatJouAndNoMatr_returnResult_ok() {

		SpccId id = new SpccId();
		id.setDatjou(20130901);
		id.setNomatr(5138);
		Spcc spcc = new Spcc();
		spcc.setId(id);
		spcc.setCode(1);
		sirhEntityManager.persist(spcc);

		AgentGeneriqueDto agent = new AgentGeneriqueDto();
		agent.setIdAgent(9005138);
		agent.setNomatr(5138);
		agent.setNomPatronymique("patro");
		agent.setPrenom("prenom");
		agent.setPrenomUsage("prenom");

		Spcc result = repository.getSpcc(5138, new DateTime(2013, 9, 1, 0, 0, 0).toDate());

		assertNotNull(result);

		sirhEntityManager.flush();
		sirhEntityManager.clear();
	}

	@Test
	@Transactional("sirhTransactionManager")
	@Rollback
	public void getPA50OfAgent_1result() {

		SpadmnId id = new SpadmnId();
		id.setDatdeb(20130901);
		id.setNomatr(5138);
		Spadmn adm = new Spadmn();
		adm.setId(id);
		adm.setCdpadm("50");
		adm.setDatfin(20130930);

		sirhEntityManager.persist(adm);

		List<Spadmn> result = repository.getPA50OfAgent(id.getNomatr(), new DateTime(2013, 9, 1, 0, 0, 0).toDate());
		assertEquals(1, result.size());
	}

	@Test
	@Transactional("sirhTransactionManager")
	@Rollback
	public void getPA50OfAgent_1result_1old_1between_1notPA50() {

		SpadmnId id = new SpadmnId();
		id.setDatdeb(20100101);
		id.setNomatr(5138);
		Spadmn adm = new Spadmn();
		adm.setId(id);
		adm.setCdpadm("50");
		adm.setDatfin(20110930);
		sirhEntityManager.persist(adm);

		SpadmnId id2 = new SpadmnId();
		id2.setDatdeb(20111001);
		id2.setNomatr(5138);
		Spadmn adm2 = new Spadmn();
		adm2.setId(id2);
		adm2.setCdpadm("50");
		adm2.setDatfin(20131231);
		sirhEntityManager.persist(adm2);

		SpadmnId id3 = new SpadmnId();
		id3.setDatdeb(20140101);
		id3.setNomatr(5138);
		Spadmn adm3 = new Spadmn();
		adm3.setId(id3);
		adm3.setCdpadm("1");
		adm3.setDatfin(0);
		sirhEntityManager.persist(adm3);

		List<Spadmn> result = repository.getPA50OfAgent(id.getNomatr(), new DateTime(2013, 9, 1, 0, 0, 0).toDate());
		assertEquals(1, result.size());
	}

	@Test
	@Transactional("sirhTransactionManager")
	@Rollback
	public void getPA50OfAgent_badAgent() {

		SpadmnId id2 = new SpadmnId();
		id2.setDatdeb(20111001);
		id2.setNomatr(5138);
		Spadmn adm2 = new Spadmn();
		adm2.setId(id2);
		adm2.setCdpadm("50");
		adm2.setDatfin(20131231);
		sirhEntityManager.persist(adm2);

		List<Spadmn> result = repository.getPA50OfAgent(id2.getNomatr() + 1, new DateTime(2013, 9, 1, 0, 0, 0).toDate());
		assertNull(result);
	}
}
