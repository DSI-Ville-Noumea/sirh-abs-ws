package nc.noumea.mairie.abs.service.impl;

import static org.junit.Assert.assertEquals;
import nc.noumea.mairie.abs.domain.AgentRecupCount;
import nc.noumea.mairie.abs.dto.SoldeDto;
import nc.noumea.mairie.abs.repository.IRecuperationRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.domain.SpSold;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class SoldeServiceTest {

	@Test
	public void getAgentSoldeRecuperation_AgentDoesNotExists() {

		// Given
		Integer idAgent = 9008765;

		IRecuperationRepository rr = Mockito.mock(IRecuperationRepository.class);
		Mockito.when(rr.getAgentRecupCount(idAgent)).thenReturn(null);

		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "recuperationRepository", rr);

		// When
		SoldeDto dto = service.getAgentSoldeRecuperation(idAgent);

		assertEquals("0.0", dto.getSolde().toString());
	}

	@Test
	public void getAgentSoldeRecuperation_AgentExists() {

		// Given
		Integer idAgent = 9008765;
		AgentRecupCount arc = new AgentRecupCount();
		arc.setIdAgent(idAgent);
		arc.setTotalMinutes(72);

		IRecuperationRepository rr = Mockito.mock(IRecuperationRepository.class);
		Mockito.when(rr.getAgentRecupCount(idAgent)).thenReturn(arc);

		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "recuperationRepository", rr);

		// When
		SoldeDto dto = service.getAgentSoldeRecuperation(idAgent);

		assertEquals("72.0", dto.getSolde().toString());
	}

	@Test
	public void getAgentSoldeCongeAnnee_AgentDoesNotExists() {

		// Given
		Integer idAgent = 9008765;

		ISirhRepository rr = Mockito.mock(ISirhRepository.class);
		Mockito.when(rr.getSpsold(idAgent)).thenReturn(null);

		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "sirhRepository", rr);

		// When
		SoldeDto dto = service.getAgentSoldeCongeAnnee(idAgent);

		assertEquals("0.0", dto.getSolde().toString());
	}

	@Test
	public void getAgentSoldeCongeAnnee_AgentExists() {
		double solde = 72.0;

		// Given
		Integer idAgent = 9008765;
		SpSold arc = new SpSold();
		arc.setNomatr(8765);
		arc.setSoldeAnneeEnCours(solde);

		ISirhRepository rr = Mockito.mock(ISirhRepository.class);
		Mockito.when(rr.getSpsold(idAgent)).thenReturn(arc);

		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "sirhRepository", rr);

		// When
		SoldeDto dto = service.getAgentSoldeCongeAnnee(idAgent);

		assertEquals("72.0", dto.getSolde().toString());
	}

	@Test
	public void getAgentSoldeCongeAnneePrec_AgentDoesNotExists() {

		// Given
		Integer idAgent = 9008765;

		ISirhRepository rr = Mockito.mock(ISirhRepository.class);
		Mockito.when(rr.getSpsold(idAgent)).thenReturn(null);

		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "sirhRepository", rr);

		// When
		SoldeDto dto = service.getAgentSoldeCongeAnneePrec(idAgent);

		assertEquals("0.0", dto.getSolde().toString());
	}

	@Test
	public void getAgentSoldeCongeAnneePrec_AgentExists() {
		double solde = 72.0;

		// Given
		Integer idAgent = 9008765;
		SpSold arc = new SpSold();
		arc.setNomatr(8765);
		arc.setSoldeAnneeEnCours(solde);

		ISirhRepository rr = Mockito.mock(ISirhRepository.class);
		Mockito.when(rr.getSpsold(idAgent)).thenReturn(arc);

		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "sirhRepository", rr);

		// When
		SoldeDto dto = service.getAgentSoldeCongeAnneePrec(idAgent);

		assertEquals("72.0", dto.getSolde().toString());
	}
}
