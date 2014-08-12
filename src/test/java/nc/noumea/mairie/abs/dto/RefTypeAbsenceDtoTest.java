package nc.noumea.mairie.abs.dto;

import static org.junit.Assert.assertEquals;

import javax.xml.bind.annotation.XmlRootElement;

import nc.noumea.mairie.abs.domain.RefGroupeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeSaisi;

import org.junit.Test;

@XmlRootElement
public class RefTypeAbsenceDtoTest {

	@Test
	public void ctor_withRefTypeAbsence() {

		RefGroupeAbsence groupe = new RefGroupeAbsence();
			groupe.setCode("groupe");
		
		// Given
		RefTypeAbsence ref = new RefTypeAbsence();
			ref.setIdRefTypeAbsence(12);
			ref.setLabel("test lib");
			ref.setGroupe(groupe);

		// When
		RefTypeAbsenceDto result = new RefTypeAbsenceDto(ref);

		// Then
		assertEquals(ref.getLabel(), result.getLibelle());
		assertEquals(ref.getGroupe().getCode(), result.getGroupeAbsence().getCode());
		assertEquals(ref.getIdRefTypeAbsence(), result.getIdRefTypeAbsence());
	}
	
	@Test
	public void ctor_withRefTypeSaisi() {

		RefGroupeAbsence groupe = new RefGroupeAbsence();
			groupe.setCode("groupe");
		
		// Given
		RefTypeAbsence ref = new RefTypeAbsence();
			ref.setIdRefTypeAbsence(12);
			ref.setLabel("test lib");
			ref.setGroupe(groupe);
		
		RefTypeSaisi typeSaisi = new RefTypeSaisi();
//			typeSaisi.setIdRefTypeAbsence(ref.getIdRefTypeAbsence());
			typeSaisi.setType(ref);
		
		// When
		RefTypeAbsenceDto result = new RefTypeAbsenceDto(ref, typeSaisi);

		// Then
		assertEquals(ref.getLabel(), result.getLibelle());
		assertEquals(ref.getGroupe().getCode(), result.getGroupeAbsence().getCode());
		assertEquals(ref.getIdRefTypeAbsence(), result.getIdRefTypeAbsence());
		assertEquals(typeSaisi.getType().getIdRefTypeAbsence(), result.getTypeSaisiDto().getIdRefTypeDemande());
	}
}
