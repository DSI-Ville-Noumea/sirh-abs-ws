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
	public void getAgentSolde_AgentDoesNotExists() {

		// Given
		Integer idAgent = 9008765;

		IRecuperationRepository rr = Mockito.mock(IRecuperationRepository.class);
		Mockito.when(rr.getAgentRecupCount(idAgent)).thenReturn(null);

		ISirhRepository sirh = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirh.getSpsold(idAgent)).thenReturn(null);

		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "recuperationRepository", rr);
		ReflectionTestUtils.setField(service, "sirhRepository", sirh);

		// When
		SoldeDto dto = service.getAgentSolde(idAgent);

		assertEquals("0.0", dto.getSoldeCongeAnnee().toString());
		assertEquals("0.0", dto.getSoldeCongeAnneePrec().toString());
		assertEquals("0.0", dto.getSoldeRecup().toString());
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

		SpSold solde = new SpSold();
		solde.setNomatr(8765);
		solde.setSoldeAnneeEnCours(cotaSoldeAnnee);
		solde.setSoldeAnneePrec(cotaSoldeAnneePrec);

		IRecuperationRepository rr = Mockito.mock(IRecuperationRepository.class);
		Mockito.when(rr.getAgentRecupCount(idAgent)).thenReturn(arc);

		ISirhRepository sirh = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirh.getSpsold(idAgent)).thenReturn(solde);

		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "recuperationRepository", rr);
		ReflectionTestUtils.setField(service, "sirhRepository", sirh);

		// When
		SoldeDto dto = service.getAgentSolde(idAgent);

		assertEquals("72.0", dto.getSoldeRecup().toString());
		assertEquals("62.0", dto.getSoldeCongeAnnee().toString());
		assertEquals("25.5", dto.getSoldeCongeAnneePrec().toString());
	}
}
