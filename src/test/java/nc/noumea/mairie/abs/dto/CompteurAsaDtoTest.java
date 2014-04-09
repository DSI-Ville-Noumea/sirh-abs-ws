package nc.noumea.mairie.abs.dto;

import static org.junit.Assert.assertEquals;

import javax.xml.bind.annotation.XmlRootElement;

import nc.noumea.mairie.abs.domain.AgentAsaA48Count;

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
}
