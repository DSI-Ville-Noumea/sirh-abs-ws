package nc.noumea.mairie.abs.service.rules.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import nc.noumea.mairie.abs.domain.RefTypeSaisi;
import nc.noumea.mairie.abs.domain.RefTypeSaisiCongeAnnuel;
import nc.noumea.mairie.abs.domain.RefUnitePeriodeQuota;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.service.impl.HelperService;

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
		List<RefTypeSaisiCongeAnnuel> typeSaisi = new ArrayList<>();

		ReturnMessageDto result = new ReturnMessageDto();
		result = impl.checkSaisiNewTypeAbsence(null, typeSaisi, result);

		assertEquals(0, result.getErrors().size());
	}

	@Test
	public void checkSaisiNewTypeAbsence_ErreurDateDebut() {
		List<RefTypeSaisiCongeAnnuel> typeSaisi = new ArrayList<>();
		RefTypeSaisiCongeAnnuel type = new RefTypeSaisiCongeAnnuel();
		type.setCalendarDateDebut(false);
		typeSaisi.add(type);

		ReturnMessageDto result = new ReturnMessageDto();
		result = impl.checkSaisiNewTypeAbsence(null, typeSaisi, result);

		assertEquals(3, result.getErrors().size());
		assertEquals(result.getErrors().get(0), "La date de début est obligatoire.");
		assertEquals(result.getErrors().get(1), "Si date de reprise est à non, alors date de fin doit être à oui.");
		assertEquals(result.getErrors().get(2), "Si consécutif est à non, alors décompte du samedi doit être à oui.");
	}

	@Test
	public void checkSaisiNewTypeAbsence_ErreurDateFin_Reprise() {
		List<RefTypeSaisiCongeAnnuel> typeSaisi = new ArrayList<>();
		RefTypeSaisiCongeAnnuel type = new RefTypeSaisiCongeAnnuel();
		type.setCalendarDateDebut(true);
		type.setCalendarDateFin(true);
		type.setCalendarDateReprise(true);
		typeSaisi.add(type);

		ReturnMessageDto result = new ReturnMessageDto();
		result = impl.checkSaisiNewTypeAbsence(null, typeSaisi, result);

		assertEquals(2, result.getErrors().size());
		assertEquals(result.getErrors().get(0), "Si date de reprise est à oui, alors date de fin doit être à non.");
		assertEquals(result.getErrors().get(1), "Si consécutif est à non, alors décompte du samedi doit être à oui.");
	}

	@Test
	public void checkSaisiNewTypeAbsence_ErreurDateFin_Reprise_Bis() {
		List<RefTypeSaisiCongeAnnuel> typeSaisi = new ArrayList<>();
		RefTypeSaisiCongeAnnuel type = new RefTypeSaisiCongeAnnuel();
		type.setCalendarDateDebut(true);
		type.setCalendarDateFin(false);
		type.setCalendarDateReprise(false);
		typeSaisi.add(type);

		ReturnMessageDto result = new ReturnMessageDto();
		result = impl.checkSaisiNewTypeAbsence(null, typeSaisi, result);

		assertEquals(2, result.getErrors().size());
		assertEquals(result.getErrors().get(0), "Si date de reprise est à non, alors date de fin doit être à oui.");
		assertEquals(result.getErrors().get(1), "Si consécutif est à non, alors décompte du samedi doit être à oui.");
	}

	@Test
	public void checkSaisiNewTypeAbsence_ErreurDecompteSamed_Consecutif() {
		List<RefTypeSaisiCongeAnnuel> typeSaisi = new ArrayList<>();
		RefTypeSaisiCongeAnnuel type = new RefTypeSaisiCongeAnnuel();
		type.setCalendarDateDebut(true);
		type.setCalendarDateFin(true);
		type.setCalendarDateReprise(false);
		type.setDecompteSamedi(true);
		type.setConsecutif(true);
		typeSaisi.add(type);

		ReturnMessageDto result = new ReturnMessageDto();
		result = impl.checkSaisiNewTypeAbsence(null, typeSaisi, result);

		assertEquals(1, result.getErrors().size());
		assertEquals(result.getErrors().get(0), "Si consécutif est à oui, alors décompte du samedi doit être à non.");
	}

	@Test
	public void checkSaisiNewTypeAbsence_ErreurDecompteSamed_Consecutif_Bis() {
		List<RefTypeSaisiCongeAnnuel> typeSaisi = new ArrayList<>();
		RefTypeSaisiCongeAnnuel type = new RefTypeSaisiCongeAnnuel();
		type.setCalendarDateDebut(true);
		type.setCalendarDateFin(true);
		type.setCalendarDateReprise(false);
		type.setDecompteSamedi(false);
		type.setConsecutif(false);
		typeSaisi.add(type);

		ReturnMessageDto result = new ReturnMessageDto();
		result = impl.checkSaisiNewTypeAbsence(null, typeSaisi, result);

		assertEquals(1, result.getErrors().size());
		assertEquals(result.getErrors().get(0), "Si consécutif est à non, alors décompte du samedi doit être à oui.");
	}

	@Test
	public void checkSaisiNewTypeAbsence_ok() {
		List<RefTypeSaisiCongeAnnuel> typeSaisi = new ArrayList<>();
		RefTypeSaisiCongeAnnuel type = new RefTypeSaisiCongeAnnuel();
		type.setCalendarDateDebut(true);
		type.setCalendarDateFin(true);
		type.setCalendarDateReprise(false);
		type.setDecompteSamedi(false);
		type.setConsecutif(true);
		typeSaisi.add(type);

		ReturnMessageDto result = new ReturnMessageDto();
		result = impl.checkSaisiNewTypeAbsence(null, typeSaisi, result);

		assertEquals(0, result.getErrors().size());
	}
}
