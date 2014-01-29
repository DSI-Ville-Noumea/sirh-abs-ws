package nc.noumea.mairie.abs.service.impl;

import static org.junit.Assert.assertEquals;
import nc.noumea.mairie.abs.domain.AgentRecupCount;
import nc.noumea.mairie.abs.domain.AgentReposCompCount;
import nc.noumea.mairie.abs.dto.SoldeDto;
import nc.noumea.mairie.abs.repository.ICounterRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.domain.SpSold;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class SoldeServiceTest {

	@Test
	public void getAgentSolde_AgentDoesNotExists() {

		// Given
		Integer idAgent = 9008765;

		ICounterRepository cr = Mockito.mock(ICounterRepository.class);
		Mockito.when(cr.getAgentCounter(AgentRecupCount.class, idAgent)).thenReturn(null);

		ISirhRepository sirh = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirh.getSpsold(idAgent)).thenReturn(null);

		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "counterRepository", cr);
		ReflectionTestUtils.setField(service, "sirhRepository", sirh);

		// When
		SoldeDto dto = service.getAgentSolde(idAgent);

		assertEquals("0.0", dto.getSoldeCongeAnnee().toString());
		assertEquals("0.0", dto.getSoldeCongeAnneePrec().toString());
		assertEquals("0.0", dto.getSoldeRecup().toString());
		assertEquals("0.0", dto.getSoldeReposCompAnnee().toString());
		assertEquals("0.0", dto.getSoldeReposCompAnneePrec().toString());
	}

	@Test
	public void getAgentSolde_AgentExists() {

		// Given
		Integer idAgent = 9008765;
		double cotaSoldeAnnee = 62.0;
		double cotaSoldeAnneePrec = 25.5;

		AgentRecupCount arc = new AgentRecupCount();
		arc.setIdAgent(idAgent);
		arc.setTotalMinutes(72);

		AgentReposCompCount arcc = new AgentReposCompCount();
		arcc.setIdAgent(idAgent);
		arcc.setTotalMinutes(12);
		arcc.setTotalMinutesAnneeN1(10);

		SpSold solde = new SpSold();
		solde.setNomatr(8765);
		solde.setSoldeAnneeEnCours(cotaSoldeAnnee);
		solde.setSoldeAnneePrec(cotaSoldeAnneePrec);

		ICounterRepository cr = Mockito.mock(ICounterRepository.class);
		Mockito.when(cr.getAgentCounter(AgentRecupCount.class, idAgent)).thenReturn(arc);
		Mockito.when(cr.getAgentCounter(AgentReposCompCount.class, idAgent)).thenReturn(arcc);

		ISirhRepository sirh = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirh.getSpsold(idAgent)).thenReturn(solde);

		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "counterRepository", cr);
		ReflectionTestUtils.setField(service, "sirhRepository", sirh);

		// When
		SoldeDto dto = service.getAgentSolde(idAgent);

		assertEquals("72.0", dto.getSoldeRecup().toString());
		assertEquals("62.0", dto.getSoldeCongeAnnee().toString());
		assertEquals("25.5", dto.getSoldeCongeAnneePrec().toString());
		assertEquals("12.0", dto.getSoldeReposCompAnnee().toString());
		assertEquals("10.0", dto.getSoldeReposCompAnneePrec().toString());
	}
}
