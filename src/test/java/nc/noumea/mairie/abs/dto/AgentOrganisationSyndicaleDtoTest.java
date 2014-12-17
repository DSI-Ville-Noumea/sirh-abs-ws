package nc.noumea.mairie.abs.dto;

import static org.junit.Assert.assertEquals;

import javax.xml.bind.annotation.XmlRootElement;

import nc.noumea.mairie.abs.domain.AgentOrganisationSyndicale;

import org.junit.Test;

@XmlRootElement
public class AgentOrganisationSyndicaleDtoTest {

	@Test
	public void ctor() {

		// Given
		AgentOrganisationSyndicale ref = new AgentOrganisationSyndicale();
		ref.setIdAgent(9005138);
		ref.setActif(true);

		// When
		AgentOrganisationSyndicaleDto result = new AgentOrganisationSyndicaleDto();
		result.setActif(ref.isActif());
		result.setIdAgent(ref.getIdAgent());

		// Then
		assertEquals(ref.isActif(), result.isActif());
		assertEquals(ref.getIdAgent(), result.getIdAgent());
	}

	@Test
	public void ctor_WithAgentOrganisationSyndicale() {

		// Given
		AgentOrganisationSyndicale ref = new AgentOrganisationSyndicale();
		ref.setIdAgent(9005138);
		ref.setActif(true);

		// When
		AgentOrganisationSyndicaleDto result = new AgentOrganisationSyndicaleDto(ref);

		// Then
		assertEquals(ref.isActif(), result.isActif());
		assertEquals(ref.getIdAgent(), result.getIdAgent());
	}
}
