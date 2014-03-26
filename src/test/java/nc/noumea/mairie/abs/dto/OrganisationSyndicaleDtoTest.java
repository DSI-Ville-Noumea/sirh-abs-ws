package nc.noumea.mairie.abs.dto;

import static org.junit.Assert.assertEquals;

import javax.xml.bind.annotation.XmlRootElement;

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

		// When
		OrganisationSyndicaleDto result = new OrganisationSyndicaleDto();
		result.setActif(ref.isActif());
		result.setIdOrganisation(ref.getIdOrganisationSyndicale());
		result.setLibelle(ref.getLibelle());
		result.setSigle(ref.getSigle());

		// Then
		assertEquals(ref.isActif(), result.isActif());
		assertEquals(ref.getIdOrganisationSyndicale(), result.getIdOrganisation());
		assertEquals(ref.getLibelle(), result.getLibelle());
		assertEquals(ref.getSigle(), result.getSigle());
	}

	@Test
	public void ctor_WithOrganisationSyndicale() {

		// Given
		OrganisationSyndicale ref = new OrganisationSyndicale();
		ref.setIdOrganisationSyndicale(1);
		ref.setLibelle("lib");
		ref.setSigle("sigle");
		ref.setActif(true);

		// When
		OrganisationSyndicaleDto result = new OrganisationSyndicaleDto(ref);

		// Then
		assertEquals(ref.isActif(), result.isActif());
		assertEquals(ref.getIdOrganisationSyndicale(), result.getIdOrganisation());
		assertEquals(ref.getLibelle(), result.getLibelle());
		assertEquals(ref.getSigle(), result.getSigle());
	}
}
