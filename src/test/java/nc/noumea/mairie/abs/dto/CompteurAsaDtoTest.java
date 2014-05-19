package nc.noumea.mairie.abs.dto;

import static org.junit.Assert.assertEquals;

import javax.xml.bind.annotation.XmlRootElement;

import nc.noumea.mairie.abs.asa.domain.AgentAsaA48Count;
import nc.noumea.mairie.abs.asa.domain.AgentAsaA52Count;
import nc.noumea.mairie.abs.asa.domain.AgentAsaA53Count;
import nc.noumea.mairie.abs.asa.domain.AgentAsaA54Count;
import nc.noumea.mairie.abs.asa.domain.AgentAsaA55Count;
import nc.noumea.mairie.abs.domain.OrganisationSyndicale;

import org.joda.time.DateTime;
import org.junit.Test;

@XmlRootElement
public class CompteurAsaDtoTest {

	@Test
	public void ctor_withAgentAsaA48Count() {

		// Given
		AgentAsaA48Count ref = new AgentAsaA48Count();
		ref.setIdAgent(9005138);
		ref.setDateDebut(new DateTime(2013, 1, 1, 0, 0, 0).toDate());
		ref.setDateFin(new DateTime(2013, 12, 31, 0, 0, 0).toDate());
		ref.setTotalJours(10.0);

		// When
		CompteurAsaDto result = new CompteurAsaDto(ref);

		// Then
		assertEquals(ref.getDateDebut(), result.getDateDebut());
		assertEquals(ref.getDateFin(), result.getDateFin());
		assertEquals(ref.getIdAgent(), result.getIdAgent());
		assertEquals(ref.getTotalJours(), result.getNb());
	}

	@Test
	public void ctor_withAgentAsaA54Count() {

		// Given
		AgentAsaA54Count ref = new AgentAsaA54Count();
		ref.setIdAgent(9005138);
		ref.setDateDebut(new DateTime(2013, 1, 1, 0, 0, 0).toDate());
		ref.setDateFin(new DateTime(2013, 12, 31, 0, 0, 0).toDate());
		ref.setTotalJours(10.0);

		// When
		CompteurAsaDto result = new CompteurAsaDto(ref);

		// Then
		assertEquals(ref.getDateDebut(), result.getDateDebut());
		assertEquals(ref.getDateFin(), result.getDateFin());
		assertEquals(ref.getIdAgent(), result.getIdAgent());
		assertEquals(ref.getTotalJours(), result.getNb());
	}

	@Test
	public void ctor_withAgentAsaA55Count() {

		// Given
		AgentAsaA55Count ref = new AgentAsaA55Count();
		ref.setIdAgent(9005138);
		ref.setDateDebut(new DateTime(2013, 1, 1, 0, 0, 0).toDate());
		ref.setDateFin(new DateTime(2013, 12, 31, 0, 0, 0).toDate());
		ref.setTotalMinutes(5 * 60);

		// When
		CompteurAsaDto result = new CompteurAsaDto(ref);

		// Then
		assertEquals(ref.getDateDebut(), result.getDateDebut());
		assertEquals(ref.getDateFin(), result.getDateFin());
		assertEquals(ref.getIdAgent(), result.getIdAgent());
		assertEquals(ref.getTotalMinutes(), result.getNb().intValue());
	}

	@Test
	public void ctor_withAgentAsaA52Count() {

		// Given
		OrganisationSyndicale orga = new OrganisationSyndicale();
		orga.setIdOrganisationSyndicale(1);
		AgentAsaA52Count ref = new AgentAsaA52Count();
		ref.setIdAgent(9005138);
		ref.setDateDebut(new DateTime(2013, 1, 1, 0, 0, 0).toDate());
		ref.setDateFin(new DateTime(2013, 12, 31, 0, 0, 0).toDate());
		ref.setTotalMinutes(5 * 60);
		ref.setOrganisationSyndicale(orga);

		// When
		CompteurAsaDto result = new CompteurAsaDto(ref);

		// Then
		assertEquals(ref.getDateDebut(), result.getDateDebut());
		assertEquals(ref.getDateFin(), result.getDateFin());
		assertEquals(ref.getIdAgent(), result.getIdAgent());
		assertEquals(ref.getTotalMinutes(), result.getNb().intValue());
		assertEquals(ref.getOrganisationSyndicale().getIdOrganisationSyndicale(), result.getOrganisationSyndicaleDto()
				.getIdOrganisation());
	}

	@Test
	public void ctor_withAgentAsaA53Count() {

		// Given
		OrganisationSyndicale orga = new OrganisationSyndicale();
		orga.setIdOrganisationSyndicale(1);
		AgentAsaA53Count ref = new AgentAsaA53Count();
		ref.setIdAgent(9005138);
		ref.setDateDebut(new DateTime(2013, 1, 1, 0, 0, 0).toDate());
		ref.setDateFin(new DateTime(2013, 12, 31, 0, 0, 0).toDate());
		ref.setTotalJours(1.0);
		ref.setOrganisationSyndicale(orga);

		// When
		CompteurAsaDto result = new CompteurAsaDto(ref);

		// Then
		assertEquals(ref.getDateDebut(), result.getDateDebut());
		assertEquals(ref.getDateFin(), result.getDateFin());
		assertEquals(ref.getIdAgent(), result.getIdAgent());
		assertEquals(ref.getTotalJours(), result.getNb());
		assertEquals(ref.getOrganisationSyndicale().getIdOrganisationSyndicale(), result.getOrganisationSyndicaleDto()
				.getIdOrganisation());
	}
}
