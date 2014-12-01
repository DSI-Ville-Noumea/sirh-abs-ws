package nc.noumea.mairie.abs.service.rules.impl;

import static org.junit.Assert.assertEquals;
import nc.noumea.mairie.abs.domain.RefTypeSaisiCongeAnnuel;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

import org.junit.Test;
import org.springframework.mock.staticmock.MockStaticEntityMethods;

@MockStaticEntityMethods
public class AbsCongesAnnuelsDataConsistencyRulesImplTest extends DefaultAbsenceDataConsistencyRulesImplTest {

	protected AbsCongesAnnuelsDataConsistencyRulesImpl impl = new AbsCongesAnnuelsDataConsistencyRulesImpl();

	@Test
	public void testMethodeParenteHeritage() throws Throwable {
		super.impl = new AbsCongesExcepDataConsistencyRulesImpl();
		super.allTest(impl);
	}

	@Test
	public void checkSaisiNewTypeAbsence() {
		RefTypeSaisiCongeAnnuel typeSaisi = new RefTypeSaisiCongeAnnuel();

		ReturnMessageDto result = new ReturnMessageDto();
		result = impl.checkSaisiNewTypeAbsence(null, typeSaisi, result);

		assertEquals(1, result.getErrors().size());
		assertEquals(result.getErrors().get(0), "La saisie de nouveau type d'absence pour ce groupe d'absence n'est pas autorisée.");
	}

	@Test
	public void checkSaisiNewTypeAbsence_ErreurDateDebut() {
		RefTypeSaisiCongeAnnuel typeSaisi = new RefTypeSaisiCongeAnnuel();
		typeSaisi.setIdRefTypeSaisiCongeAnnuel(1);
		typeSaisi.setCalendarDateDebut(false);

		ReturnMessageDto result = new ReturnMessageDto();
		result = impl.checkSaisiNewTypeAbsence(null, typeSaisi, result);

		assertEquals(3, result.getErrors().size());
		assertEquals(result.getErrors().get(0), "La date de début est obligatoire.");
		assertEquals(result.getErrors().get(1), "Si date de reprise est à non, alors date de fin doit être à oui.");
		assertEquals(result.getErrors().get(2), "Si consécutif est à non, alors décompte du samedi doit être à oui.");
	}

	@Test
	public void checkSaisiNewTypeAbsence_ErreurDateFin_Reprise() {
		RefTypeSaisiCongeAnnuel typeSaisi = new RefTypeSaisiCongeAnnuel();
		typeSaisi.setIdRefTypeSaisiCongeAnnuel(1);
		typeSaisi.setCalendarDateDebut(true);
		typeSaisi.setCalendarDateFin(true);
		typeSaisi.setCalendarDateReprise(true);

		ReturnMessageDto result = new ReturnMessageDto();
		result = impl.checkSaisiNewTypeAbsence(null, typeSaisi, result);

		assertEquals(2, result.getErrors().size());
		assertEquals(result.getErrors().get(0), "Si date de reprise est à oui, alors date de fin doit être à non.");
		assertEquals(result.getErrors().get(1), "Si consécutif est à non, alors décompte du samedi doit être à oui.");
	}

	@Test
	public void checkSaisiNewTypeAbsence_ErreurDateFin_Reprise_Bis() {
		RefTypeSaisiCongeAnnuel typeSaisi = new RefTypeSaisiCongeAnnuel();
		typeSaisi.setIdRefTypeSaisiCongeAnnuel(1);
		typeSaisi.setCalendarDateDebut(true);
		typeSaisi.setCalendarDateFin(false);
		typeSaisi.setCalendarDateReprise(false);

		ReturnMessageDto result = new ReturnMessageDto();
		result = impl.checkSaisiNewTypeAbsence(null, typeSaisi, result);

		assertEquals(2, result.getErrors().size());
		assertEquals(result.getErrors().get(0), "Si date de reprise est à non, alors date de fin doit être à oui.");
		assertEquals(result.getErrors().get(1), "Si consécutif est à non, alors décompte du samedi doit être à oui.");
	}

	@Test
	public void checkSaisiNewTypeAbsence_ErreurDecompteSamed_Consecutif() {
		RefTypeSaisiCongeAnnuel typeSaisi = new RefTypeSaisiCongeAnnuel();
		typeSaisi.setIdRefTypeSaisiCongeAnnuel(1);
		typeSaisi.setCalendarDateDebut(true);
		typeSaisi.setCalendarDateFin(true);
		typeSaisi.setCalendarDateReprise(false);
		typeSaisi.setDecompteSamedi(true);
		typeSaisi.setConsecutif(true);

		ReturnMessageDto result = new ReturnMessageDto();
		result = impl.checkSaisiNewTypeAbsence(null, typeSaisi, result);

		assertEquals(1, result.getErrors().size());
		assertEquals(result.getErrors().get(0), "Si consécutif est à oui, alors décompte du samedi doit être à non.");
	}

	@Test
	public void checkSaisiNewTypeAbsence_ErreurDecompteSamed_Consecutif_Bis() {
		RefTypeSaisiCongeAnnuel typeSaisi = new RefTypeSaisiCongeAnnuel();
		typeSaisi.setIdRefTypeSaisiCongeAnnuel(1);
		typeSaisi.setCalendarDateDebut(true);
		typeSaisi.setCalendarDateFin(true);
		typeSaisi.setCalendarDateReprise(false);
		typeSaisi.setDecompteSamedi(false);
		typeSaisi.setConsecutif(false);

		ReturnMessageDto result = new ReturnMessageDto();
		result = impl.checkSaisiNewTypeAbsence(null, typeSaisi, result);

		assertEquals(1, result.getErrors().size());
		assertEquals(result.getErrors().get(0), "Si consécutif est à non, alors décompte du samedi doit être à oui.");
	}

	@Test
	public void checkSaisiNewTypeAbsence_ok() {
		RefTypeSaisiCongeAnnuel typeSaisi = new RefTypeSaisiCongeAnnuel();
		typeSaisi.setIdRefTypeSaisiCongeAnnuel(1);
		typeSaisi.setCalendarDateDebut(true);
		typeSaisi.setCalendarDateFin(true);
		typeSaisi.setCalendarDateReprise(false);
		typeSaisi.setDecompteSamedi(false);
		typeSaisi.setConsecutif(true);

		ReturnMessageDto result = new ReturnMessageDto();
		result = impl.checkSaisiNewTypeAbsence(null, typeSaisi, result);

		assertEquals(0, result.getErrors().size());
	}
}
