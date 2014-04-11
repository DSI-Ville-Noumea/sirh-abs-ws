package nc.noumea.mairie.abs.service.rules.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentAsaA48Count;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeAsa;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IAsaRepository;
import nc.noumea.mairie.abs.repository.ICounterRepository;
import nc.noumea.mairie.abs.service.impl.HelperService;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.staticmock.MockStaticEntityMethods;
import org.springframework.test.util.ReflectionTestUtils;

@MockStaticEntityMethods
public class AbsAsaA48DataConsistencyRulesImplTest {

	@Test
	public void checkDroitCompteurAsaA48_aucunDroit() {

		Date dateDebut = new Date();
		AgentAsaA48Count soldeAsaA48 = null;

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounterByDate(AgentAsaA48Count.class, 9005138, dateDebut)).thenReturn(
				soldeAsaA48);

		AbsAsaA48DataConsistencyRulesImpl impl = new AbsAsaA48DataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);

		ReturnMessageDto srm = new ReturnMessageDto();
		Demande demande = new Demande();
		demande.setIdAgent(9005138);
		demande.setDateDebut(dateDebut);

		srm = impl.checkDroitCompteurAsaA48(srm, demande);

		assertEquals(1, srm.getErrors().size());
		assertEquals(srm.getErrors().get(0), "L'agent [9005138] ne possède pas de droit ASA.");
	}

	@Test
	public void checkDroitCompteurAsaA48_compteurNegatif() {

		Date dateDebut = new Date();
		AgentAsaA48Count soldeAsaA48 = new AgentAsaA48Count();
		soldeAsaA48.setTotalJours(0.0);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounterByDate(AgentAsaA48Count.class, 9005138, dateDebut)).thenReturn(
				soldeAsaA48);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.calculNombreJoursArrondiDemiJournee(Mockito.isA(Date.class), Mockito.isA(Date.class)))
				.thenReturn(10.0);

		List<DemandeAsa> listDemandeAsa = new ArrayList<DemandeAsa>();
		listDemandeAsa.addAll(Arrays.asList(new DemandeAsa(), new DemandeAsa()));

		IAsaRepository asaRepository = Mockito.mock(IAsaRepository.class);
		Mockito.when(asaRepository.getListDemandeAsaEnCours(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				listDemandeAsa);

		AbsAsaA48DataConsistencyRulesImpl impl = new AbsAsaA48DataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(impl, "helperService", helperService);
		ReflectionTestUtils.setField(impl, "asaRepository", asaRepository);

		ReturnMessageDto srm = new ReturnMessageDto();
		Demande demande = new Demande();
		demande.setIdAgent(9005138);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(new Date());

		srm = impl.checkDroitCompteurAsaA48(srm, demande);

		assertEquals(0, srm.getErrors().size());
		assertEquals(1, srm.getInfos().size());
		assertEquals(srm.getInfos().get(0), AbsAsaDataConsistencyRulesImpl.DEPASSEMENT_DROITS_ASA_MSG);
	}

	@Test
	public void checkDroitCompteurAsaA48_compteurNegatifBis() {

		Date dateDebut = new Date();
		AgentAsaA48Count soldeAsaA48 = new AgentAsaA48Count();
		soldeAsaA48.setTotalJours(9.0);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounterByDate(AgentAsaA48Count.class, 9005138, dateDebut)).thenReturn(
				soldeAsaA48);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.calculNombreJoursArrondiDemiJournee(Mockito.isA(Date.class), Mockito.isA(Date.class)))
				.thenReturn(10.0);

		List<DemandeAsa> listDemandeAsa = new ArrayList<DemandeAsa>();
		listDemandeAsa.addAll(Arrays.asList(new DemandeAsa(), new DemandeAsa()));

		IAsaRepository asaRepository = Mockito.mock(IAsaRepository.class);
		Mockito.when(asaRepository.getListDemandeAsaEnCours(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				listDemandeAsa);

		AbsAsaA48DataConsistencyRulesImpl impl = new AbsAsaA48DataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(impl, "helperService", helperService);
		ReflectionTestUtils.setField(impl, "asaRepository", asaRepository);

		ReturnMessageDto srm = new ReturnMessageDto();
		Demande demande = new Demande();
		demande.setIdAgent(9005138);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(new Date());

		srm = impl.checkDroitCompteurAsaA48(srm, demande);

		assertEquals(0, srm.getErrors().size());
		assertEquals(1, srm.getInfos().size());
		assertEquals(srm.getInfos().get(0), AbsAsaDataConsistencyRulesImpl.DEPASSEMENT_DROITS_ASA_MSG);
	}

	@Test
	public void checkDroitCompteurAsaA48_ok() {

		Date dateDebut = new Date();
		AgentAsaA48Count soldeAsaA48 = new AgentAsaA48Count();
		soldeAsaA48.setTotalJours(10.5);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounterByDate(AgentAsaA48Count.class, 9005138, dateDebut)).thenReturn(
				soldeAsaA48);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.calculNombreJoursArrondiDemiJournee(Mockito.isA(Date.class), Mockito.isA(Date.class)))
				.thenReturn(10.0);

		List<DemandeAsa> listDemandeAsa = new ArrayList<DemandeAsa>();
		listDemandeAsa.addAll(Arrays.asList(new DemandeAsa(), new DemandeAsa()));

		IAsaRepository asaRepository = Mockito.mock(IAsaRepository.class);
		Mockito.when(asaRepository.getListDemandeAsaEnCours(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				listDemandeAsa);

		AbsAsaA48DataConsistencyRulesImpl impl = new AbsAsaA48DataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(impl, "helperService", helperService);
		ReflectionTestUtils.setField(impl, "asaRepository", asaRepository);

		ReturnMessageDto srm = new ReturnMessageDto();
		Demande demande = new Demande();
		demande.setIdAgent(9005138);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(new Date());

		srm = impl.checkDroitCompteurAsaA48(srm, demande);

		assertEquals(0, srm.getErrors().size());
		assertEquals(0, srm.getInfos().size());
	}
}
