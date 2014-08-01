package nc.noumea.mairie.abs.dto;

import static org.junit.Assert.assertEquals;
import nc.noumea.mairie.abs.domain.RefGroupeAbsence;

import org.junit.Test;

public class RefGroupeAbsenceDtoTest {

	@Test
	public void ctor() {
		
		RefGroupeAbsence rga = new RefGroupeAbsence();
		rga.setIdRefGroupeAbsence(1);
		rga.setCode("code 1");
		rga.setLibelle("libelle 1");
		
		RefGroupeAbsenceDto result = new RefGroupeAbsenceDto(rga);

		assertEquals(1, result.getIdRefGroupeAbsence().intValue());
		assertEquals("code 1", result.getCode());
		assertEquals("libelle 1", result.getLibelle());
	}
}
