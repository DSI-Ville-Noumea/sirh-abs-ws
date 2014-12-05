package nc.noumea.mairie.abs.dto;

import static org.junit.Assert.assertEquals;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeSaisiCongeAnnuel;

import org.junit.Test;

public class RefTypeSaisiCongeAnnuelDtoTest {

	@Test
	public void ctor_withUnitePeriodeQuotaDto() {

		RefTypeAbsence type = new RefTypeAbsence();

		RefTypeSaisiCongeAnnuel typeSaisi = new RefTypeSaisiCongeAnnuel();
		typeSaisi.setCodeBaseHoraireAbsence("A");
		typeSaisi.setCalendarDateDebut(false);
		typeSaisi.setCalendarDateFin(true);
		typeSaisi.setCalendarDateReprise(false);
		typeSaisi.setChkDateDebut(true);
		typeSaisi.setChkDateFin(true);
		typeSaisi.setDescription("description");
		typeSaisi.setConsecutif(false);
		typeSaisi.setDecompteSamedi(true);
		typeSaisi.setQuotaMultiple(null);
		typeSaisi.setType(type);

		RefTypeSaisiCongeAnnuelDto result = new RefTypeSaisiCongeAnnuelDto(typeSaisi);

		assertEquals(typeSaisi.getCodeBaseHoraireAbsence(), result.getCodeBaseHoraireAbsence());
		assertEquals(typeSaisi.isCalendarDateDebut(), result.isCalendarDateDebut());
		assertEquals(typeSaisi.isCalendarDateFin(), result.isCalendarDateFin());
		assertEquals(typeSaisi.isCalendarDateReprise(), result.isCalendarDateReprise());
		assertEquals(typeSaisi.isChkDateDebut(), result.isChkDateDebut());
		assertEquals(typeSaisi.isChkDateFin(), result.isChkDateFin());
		assertEquals(typeSaisi.getDescription(), result.getDescription());
		assertEquals(typeSaisi.isConsecutif(), result.isConsecutif());
		assertEquals(typeSaisi.isDecompteSamedi(), result.isDecompteSamedi());
		assertEquals(typeSaisi.getQuotaMultiple(), result.getQuotaMultiple());
	}

}
