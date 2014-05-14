package nc.noumea.mairie.abs.service.rules.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.DemandeAsa;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IAsaRepository;
import nc.noumea.mairie.abs.service.impl.HelperService;

import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.staticmock.MockStaticEntityMethods;
import org.springframework.test.util.ReflectionTestUtils;

@MockStaticEntityMethods
public class AbsAsaA49DataConsistencyRulesImplTest extends AbsAsaDataConsistencyRulesImplTest {

	@Test
	public void testMethodeParenteHeritage() throws Throwable {

		super.impl = new AbsAsaA49DataConsistencyRulesImpl();
		super.allTest();
	}

	@Test
	public void checkDroitCompteurAsaA49_compteurNegatif() {

		Date dateDebut = new DateTime(2014, 05, 13, 12, 30, 0).toDate();

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreMinutes(Mockito.isA(Date.class), Mockito.isA(Date.class)))
				.thenReturn(10);

		DemandeAsa asa1 = new DemandeAsa();
		asa1.setDateDebut(new Date());
		asa1.setDateFin(new Date());
		List<DemandeAsa> listDemandeAsa = new ArrayList<DemandeAsa>();
		listDemandeAsa.addAll(Arrays.asList(asa1));

		IAsaRepository asaRepository = Mockito.mock(IAsaRepository.class);
		Mockito.when(
				asaRepository.getListDemandeAsaPourMois(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(Date.class),
						Mockito.isA(Date.class), Mockito.isA(RefTypeAbsenceEnum.class))).thenReturn(listDemandeAsa);

		AbsAsaA49DataConsistencyRulesImpl impl = new AbsAsaA49DataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "helperService", helperService);
		ReflectionTestUtils.setField(impl, "asaRepository", asaRepository);

		ReturnMessageDto srm = new ReturnMessageDto();
		DemandeAsa demande = new DemandeAsa();
		demande.setIdAgent(9005138);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(new Date());
		demande.setDuree(1.0);

		srm = impl.checkDroitAsaA49(srm, demande);

		assertEquals(0, srm.getErrors().size());
		assertEquals(1, srm.getInfos().size());
		assertEquals(srm.getInfos().get(0), AbsAsaDataConsistencyRulesImpl.DEPASSEMENT_DROITS_ASA_MSG);
	}

	@Test
	public void checkDroitCompteurAsaA49_ok() {

		Date dateDebut = new DateTime(2014, 05, 13, 12, 30, 0).toDate();

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreMinutes(Mockito.isA(Date.class), Mockito.isA(Date.class))).thenReturn(0);

		DemandeAsa asa1 = new DemandeAsa();
		asa1.setDateDebut(new Date());
		asa1.setDateFin(new Date());
		List<DemandeAsa> listDemandeAsa = new ArrayList<DemandeAsa>();
		listDemandeAsa.addAll(Arrays.asList(asa1));

		IAsaRepository asaRepository = Mockito.mock(IAsaRepository.class);
		Mockito.when(
				asaRepository.getListDemandeAsaPourMois(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(Date.class),
						Mockito.isA(Date.class), Mockito.isA(RefTypeAbsenceEnum.class))).thenReturn(listDemandeAsa);

		AbsAsaA49DataConsistencyRulesImpl impl = new AbsAsaA49DataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "helperService", helperService);
		ReflectionTestUtils.setField(impl, "asaRepository", asaRepository);

		ReturnMessageDto srm = new ReturnMessageDto();
		DemandeAsa demande = new DemandeAsa();
		demande.setIdAgent(9005138);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(new Date());
		demande.setDuree(1.0);

		srm = impl.checkDroitAsaA49(srm, demande);

		assertEquals(0, srm.getErrors().size());
		assertEquals(0, srm.getInfos().size());
	}

	@Test
	public void checkDroitCompteurAsaA49_ok_bis() {

		Date dateDebut = new DateTime(2014, 05, 13, 12, 30, 0).toDate();

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreMinutes(Mockito.isA(Date.class), Mockito.isA(Date.class))).thenReturn(0);

		IAsaRepository asaRepository = Mockito.mock(IAsaRepository.class);
		Mockito.when(
				asaRepository.getListDemandeAsaPourMois(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(Date.class),
						Mockito.isA(Date.class), Mockito.isA(RefTypeAbsenceEnum.class))).thenReturn(
				new ArrayList<DemandeAsa>());

		AbsAsaA49DataConsistencyRulesImpl impl = new AbsAsaA49DataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "helperService", helperService);
		ReflectionTestUtils.setField(impl, "asaRepository", asaRepository);

		ReturnMessageDto srm = new ReturnMessageDto();
		DemandeAsa demande = new DemandeAsa();
		demande.setIdAgent(9005138);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(new Date());
		demande.setDuree(1.0);

		srm = impl.checkDroitAsaA49(srm, demande);

		assertEquals(0, srm.getErrors().size());
		assertEquals(0, srm.getInfos().size());
	}
}