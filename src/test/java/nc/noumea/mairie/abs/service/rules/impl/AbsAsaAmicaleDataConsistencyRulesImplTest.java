package nc.noumea.mairie.abs.service.rules.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentAsaAmicaleCount;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeAsa;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IAsaRepository;
import nc.noumea.mairie.abs.repository.ICounterRepository;
import nc.noumea.mairie.abs.service.impl.HelperService;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.staticmock.MockStaticEntityMethods;
import org.springframework.test.util.ReflectionTestUtils;

@MockStaticEntityMethods
public class AbsAsaAmicaleDataConsistencyRulesImplTest extends AbsAsaDataConsistencyRulesImplTest {

	@Test
	public void testMethodeParenteHeritage() throws Throwable {

		super.impl = new AbsAsaAmicaleDataConsistencyRulesImpl();
		super.allTest();
	}

	@Test
	public void checkDroitCompteurAsaAmicale_aucunDroit() {

		Date dateDebut = new Date();
		AgentAsaAmicaleCount soldeAsaAmicale = null;

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounterByDate(AgentAsaAmicaleCount.class, 9005138, dateDebut)).thenReturn(soldeAsaAmicale);

		AbsAsaAmicaleDataConsistencyRulesImpl impl = new AbsAsaAmicaleDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);

		ReturnMessageDto srm = new ReturnMessageDto();
		Demande demande = new Demande();
		demande.setIdAgent(9005138);
		demande.setDateDebut(dateDebut);

		srm = impl.checkDroitCompteurAsaAmicale(srm, demande);

		assertEquals(1, srm.getErrors().size());
		assertEquals(srm.getErrors().get(0), "L'agent [9005138] ne possède pas de droit pour les absences syndicales.");
	}

	@Test
	public void checkDroitCompteurAsaAmicale_pasActif() {

		Date dateDebut = new Date();
		AgentAsaAmicaleCount soldeAsaAmicale = new AgentAsaAmicaleCount();
		soldeAsaAmicale.setActif(false);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounterByDate(AgentAsaAmicaleCount.class, 9005138, dateDebut)).thenReturn(soldeAsaAmicale);

		AbsAsaAmicaleDataConsistencyRulesImpl impl = new AbsAsaAmicaleDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);

		ReturnMessageDto srm = new ReturnMessageDto();
		Demande demande = new Demande();
		demande.setIdAgent(9005138);
		demande.setDateDebut(dateDebut);

		srm = impl.checkDroitCompteurAsaAmicale(srm, demande);

		assertEquals(1, srm.getErrors().size());
		assertEquals(srm.getErrors().get(0), "L'agent [9005138] ne possède pas de droit pour les absences syndicales.");
	}

	@Test
	public void checkDroitCompteurAsaAmicale_compteurNegatif() {

		Date dateDebut = new Date();
		AgentAsaAmicaleCount soldeAsaAmicale = new AgentAsaAmicaleCount();
		soldeAsaAmicale.setTotalMinutes(0);
		soldeAsaAmicale.setActif(true);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounterByDate(AgentAsaAmicaleCount.class, 9005138, dateDebut)).thenReturn(soldeAsaAmicale);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreJoursArrondiDemiJournee(Mockito.isA(Date.class), Mockito.isA(Date.class))).thenReturn(10.0);

		List<DemandeAsa> listDemandeAsa = new ArrayList<DemandeAsa>();
		listDemandeAsa.addAll(Arrays.asList(new DemandeAsa(), new DemandeAsa()));

		IAsaRepository asaRepository = Mockito.mock(IAsaRepository.class);
		Mockito.when(asaRepository.getListDemandeAsaEnCours(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(listDemandeAsa);

		AbsAsaAmicaleDataConsistencyRulesImpl impl = new AbsAsaAmicaleDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(impl, "helperService", helperService);
		ReflectionTestUtils.setField(impl, "asaRepository", asaRepository);

		ReturnMessageDto srm = new ReturnMessageDto();
		DemandeAsa demande = new DemandeAsa();
		demande.setIdAgent(9005138);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(new Date());
		demande.setDuree(10.5);

		srm = impl.checkDroitCompteurAsaAmicale(srm, demande);

		assertEquals(0, srm.getInfos().size());
		assertEquals(1, srm.getErrors().size());
		assertEquals(srm.getErrors().get(0), AbsAsaDataConsistencyRulesImpl.DEPASSEMENT_DROITS_ASA_MSG);
	}

	@Test
	public void checkDroitCompteurAsaAmicale_compteurNegatifBis() {

		Date dateDebut = new Date();
		AgentAsaAmicaleCount soldeAsaAmicale = new AgentAsaAmicaleCount();
		soldeAsaAmicale.setTotalMinutes(9);
		soldeAsaAmicale.setActif(true);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounterByDate(AgentAsaAmicaleCount.class, 9005138, dateDebut)).thenReturn(soldeAsaAmicale);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreJoursArrondiDemiJournee(Mockito.isA(Date.class), Mockito.isA(Date.class))).thenReturn(10.0);

		List<DemandeAsa> listDemandeAsa = new ArrayList<DemandeAsa>();
		listDemandeAsa.addAll(Arrays.asList(new DemandeAsa(), new DemandeAsa()));

		IAsaRepository asaRepository = Mockito.mock(IAsaRepository.class);
		Mockito.when(asaRepository.getListDemandeAsaEnCours(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(listDemandeAsa);

		AbsAsaAmicaleDataConsistencyRulesImpl impl = new AbsAsaAmicaleDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(impl, "helperService", helperService);
		ReflectionTestUtils.setField(impl, "asaRepository", asaRepository);

		ReturnMessageDto srm = new ReturnMessageDto();
		DemandeAsa demande = new DemandeAsa();
		demande.setIdAgent(9005138);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(new Date());
		demande.setDuree(10.5);

		srm = impl.checkDroitCompteurAsaAmicale(srm, demande);

		assertEquals(0, srm.getInfos().size());
		assertEquals(1, srm.getErrors().size());
		assertEquals(srm.getErrors().get(0), AbsAsaDataConsistencyRulesImpl.DEPASSEMENT_DROITS_ASA_MSG);
	}

	@Test
	public void checkDroitCompteurAsaAmicale_ok() {

		Date dateDebut = new Date();
		AgentAsaAmicaleCount soldeAsaAmicale = new AgentAsaAmicaleCount();
		soldeAsaAmicale.setTotalMinutes(10);
		soldeAsaAmicale.setActif(true);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounterByDate(AgentAsaAmicaleCount.class, 9005138, dateDebut)).thenReturn(soldeAsaAmicale);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreMinutes(Mockito.isA(Date.class), Mockito.isA(Date.class))).thenReturn(10);

		List<DemandeAsa> listDemandeAsa = new ArrayList<DemandeAsa>();
		listDemandeAsa.addAll(Arrays.asList(new DemandeAsa(), new DemandeAsa()));

		IAsaRepository asaRepository = Mockito.mock(IAsaRepository.class);
		Mockito.when(asaRepository.getListDemandeAsaEnCours(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(listDemandeAsa);

		AbsAsaAmicaleDataConsistencyRulesImpl impl = new AbsAsaAmicaleDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(impl, "helperService", helperService);
		ReflectionTestUtils.setField(impl, "asaRepository", asaRepository);

		ReturnMessageDto srm = new ReturnMessageDto();
		DemandeAsa demande = new DemandeAsa();
		demande.setIdAgent(9005138);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(new Date());
		demande.setDuree(9.0);

		srm = impl.checkDroitCompteurAsaAmicale(srm, demande);

		assertEquals(0, srm.getErrors().size());
		assertEquals(0, srm.getInfos().size());
	}

	@Test
	public void checkDepassementCompteurAgent_aucunCompteur() {

		Date dateDebut = new Date();
		AgentAsaAmicaleCount soldeAsaAmicale = null;

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounterByDate(AgentAsaAmicaleCount.class, 9005138, dateDebut)).thenReturn(soldeAsaAmicale);

		AbsAsaAmicaleDataConsistencyRulesImpl impl = new AbsAsaAmicaleDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(9005138);
		DemandeDto demande = new DemandeDto();
		demande.setAgentWithServiceDto(agDto);
		demande.setDateDebut(dateDebut);
		demande.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());

		boolean srm = impl.checkDepassementCompteurAgent(demande, null);

		assertTrue(srm);
	}

	@Test
	public void checkDepassementCompteurAgent_compteurNegatif() {

		Date dateDebut = new Date();
		AgentAsaAmicaleCount soldeAsaAmicale = new AgentAsaAmicaleCount();
		soldeAsaAmicale.setTotalMinutes(0);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounterByDate(AgentAsaAmicaleCount.class, 9005138, dateDebut)).thenReturn(soldeAsaAmicale);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreJoursArrondiDemiJournee(Mockito.isA(Date.class), Mockito.isA(Date.class))).thenReturn(10.0);

		List<DemandeAsa> listDemandeAsa = new ArrayList<DemandeAsa>();
		listDemandeAsa.addAll(Arrays.asList(new DemandeAsa(), new DemandeAsa()));

		IAsaRepository asaRepository = Mockito.mock(IAsaRepository.class);
		Mockito.when(asaRepository.getListDemandeAsaEnCours(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(listDemandeAsa);

		AbsAsaAmicaleDataConsistencyRulesImpl impl = new AbsAsaAmicaleDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(impl, "helperService", helperService);
		ReflectionTestUtils.setField(impl, "asaRepository", asaRepository);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(9005138);
		DemandeDto demande = new DemandeDto();
		demande.setAgentWithServiceDto(agDto);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(new Date());
		demande.setDuree(10.5);
		demande.setIdTypeDemande(7);
		demande.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());

		boolean srm = impl.checkDepassementCompteurAgent(demande, null);

		assertTrue(srm);
	}

	@Test
	public void checkDepassementCompteurAgent_compteurOk() {

		Date dateDebut = new Date();
		AgentAsaAmicaleCount soldeAsaAmicale = new AgentAsaAmicaleCount();
		soldeAsaAmicale.setTotalMinutes(3);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounterByDate(AgentAsaAmicaleCount.class, 9005138, dateDebut)).thenReturn(soldeAsaAmicale);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreJoursArrondiDemiJournee(Mockito.isA(Date.class), Mockito.isA(Date.class))).thenReturn(1.0);

		List<DemandeAsa> listDemandeAsa = new ArrayList<DemandeAsa>();
		listDemandeAsa.addAll(Arrays.asList(new DemandeAsa(), new DemandeAsa()));

		IAsaRepository asaRepository = Mockito.mock(IAsaRepository.class);
		Mockito.when(asaRepository.getListDemandeAsaEnCours(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(listDemandeAsa);

		AbsAsaAmicaleDataConsistencyRulesImpl impl = new AbsAsaAmicaleDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(impl, "helperService", helperService);
		ReflectionTestUtils.setField(impl, "asaRepository", asaRepository);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(9005138);
		DemandeDto demande = new DemandeDto();
		demande.setAgentWithServiceDto(agDto);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(new Date());
		demande.setDuree(1.5);
		demande.setIdTypeDemande(7);
		demande.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());

		boolean srm = impl.checkDepassementCompteurAgent(demande, null);

		assertFalse(srm);
	}
}
