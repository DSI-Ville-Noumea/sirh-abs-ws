package nc.noumea.mairie.abs.dto;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AgentDtoTest {

	@Test
	public void ctor_withagent() {

		// Given
		AgentGeneriqueDto ag = new AgentGeneriqueDto();
		ag.setNomUsage("RAYNAUD");
		ag.setPrenomUsage("Nicolas");
		ag.setIdAgent(9006765);

		// When
		AgentWithServiceDto result = new AgentWithServiceDto(ag);

		// Then
		assertEquals("RAYNAUD", result.getNom());
		assertEquals("Nicolas", result.getPrenom());
		assertEquals(9006765, (int) result.getIdAgent());
	}
}
