package nc.noumea.mairie.abs.dto;

import static org.junit.Assert.assertEquals;

import javax.xml.bind.annotation.XmlRootElement;

import nc.noumea.mairie.abs.domain.RefTypeAbsence;

import org.junit.Test;

@XmlRootElement
public class RefTypeAbsenceDtoTest {

	@Test
	public void ctor_withRefTypeAbsence() {

		// Given
		RefTypeAbsence ref = new RefTypeAbsence();
		ref.setIdRefTypeAbsence(12);
		ref.setLabel("test lib");
		ref.setGroupe("grup");

		// When
		RefTypeAbsenceDto result = new RefTypeAbsenceDto(ref);

		// Then
		assertEquals(ref.getLabel(), result.getLibelle());
		assertEquals(ref.getGroupe(), result.getGroupe());
		assertEquals(ref.getIdRefTypeAbsence(), result.getIdRefTypeAbsence());
	}
}
