package nc.noumea.mairie.abs.dto;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import nc.noumea.mairie.abs.domain.AgentOrganisationSyndicale;
import nc.noumea.mairie.abs.domain.OrganisationSyndicale;

import org.junit.Test;

@XmlRootElement
public class OrganisationSyndicaleDtoTest {

	@Test
	public void ctor() {

		// Given
		OrganisationSyndicale ref = new OrganisationSyndicale();
		ref.setIdOrganisationSyndicale(1);
		ref.setLibelle("lib");
		ref.setSigle("sigle");
		ref.setActif(true);
		List<AgentOrganisationSyndicaleDto> listeAgents = new ArrayList<>();

		// When
		OrganisationSyndicaleDto result = new OrganisationSyndicaleDto();
		result.setActif(ref.isActif());
		result.setIdOrganisation(ref.getIdOrganisationSyndicale());
		result.setLibelle(ref.getLibelle());
		result.setSigle(ref.getSigle());
		result.setListeAgents(listeAgents);

		// Then
		assertEquals(ref.isActif(), result.isActif());
		assertEquals(ref.getIdOrganisationSyndicale(), result.getIdOrganisation());
		assertEquals(ref.getLibelle(), result.getLibelle());
		assertEquals(ref.getSigle(), result.getSigle());
		assertEquals(0, result.getListeAgents().size());
	}

	@Test
	public void ctor_WithOrganisationSyndicale() {

		// Given
		AgentOrganisationSyndicale ag = new AgentOrganisationSyndicale();
		ag.setIdAgent(9005138);
		List<AgentOrganisationSyndicale> agents = new ArrayList<>();
		agents.add(ag);
		OrganisationSyndicale ref = new OrganisationSyndicale();
		ref.setIdOrganisationSyndicale(1);
		ref.setLibelle("lib");
		ref.setSigle("sigle");
		ref.setActif(true);
		ref.setAgents(agents);

		// When
		OrganisationSyndicaleDto result = new OrganisationSyndicaleDto(ref);

		// Then
		assertEquals(ref.isActif(), result.isActif());
		assertEquals(ref.getIdOrganisationSyndicale(), result.getIdOrganisation());
		assertEquals(ref.getLibelle(), result.getLibelle());
		assertEquals(ref.getSigle(), result.getSigle());
		assertEquals(1, result.getListeAgents().size());
		assertEquals(ag.getIdAgent(), result.getListeAgents().get(0).getIdAgent());
	}
}
