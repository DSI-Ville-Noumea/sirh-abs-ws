package nc.noumea.mairie.abs.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentAsaA48Count;
import nc.noumea.mairie.abs.domain.AgentAsaA54Count;
import nc.noumea.mairie.abs.domain.AgentAsaA55Count;
import nc.noumea.mairie.abs.domain.AgentCount;
import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;
import nc.noumea.mairie.abs.domain.AgentRecupCount;
import nc.noumea.mairie.abs.domain.AgentReposCompCount;
import nc.noumea.mairie.abs.domain.MotifCompteur;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.dto.HistoriqueSoldeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.dto.SoldeDto;
import nc.noumea.mairie.abs.dto.SoldeSpecifiqueDto;
import nc.noumea.mairie.abs.repository.ICounterRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.abs.service.counter.impl.CongesExcepCounterServiceImpl;
import nc.noumea.mairie.abs.service.rules.impl.AbsReposCompensateurDataConsistencyRulesImpl;
import nc.noumea.mairie.domain.SpSold;

import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

public class SoldeServiceTest {

	@Test
	public void getAgentSolde_ZeroSolde() {

		// Given
		Integer idAgent = 9008765;
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		Integer annee = cal.get(Calendar.YEAR);

		ICounterRepository cr = Mockito.mock(ICounterRepository.class);
		Mockito.when(cr.getAgentCounter(AgentRecupCount.class, idAgent)).thenReturn(null);
		Mockito.when(
				cr.getAgentCounterByDate(AgentAsaA48Count.class, idAgent, new DateTime(annee, 1, 1, 0, 0, 0).toDate()))
				.thenReturn(null);
		Mockito.when(
				cr.getAgentCounterByDate(AgentAsaA54Count.class, idAgent, new DateTime(annee, 1, 1, 0, 0, 0).toDate()))
				.thenReturn(null);
		Mockito.when(
				cr.getAgentCounterByDate(AgentAsaA55Count.class, idAgent, new DateTime(annee, 1, 1, 0, 0, 0).toDate()))
				.thenReturn(null);

		ISirhRepository sirh = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirh.getSpsold(idAgent)).thenReturn(null);

		AbsReposCompensateurDataConsistencyRulesImpl absDataConsistencyRules = Mockito
				.mock(AbsReposCompensateurDataConsistencyRulesImpl.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		}).when(absDataConsistencyRules)
				.checkStatutAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class));

		List<SoldeSpecifiqueDto> listeSoldeSpecifiqueDto = new ArrayList<SoldeSpecifiqueDto>();
		
		CongesExcepCounterServiceImpl congesExcepCounterServiceImpl = Mockito.mock(CongesExcepCounterServiceImpl.class);
		Mockito.when(congesExcepCounterServiceImpl.getListAgentCounterByDate(idAgent, null, null)).thenReturn(listeSoldeSpecifiqueDto);
		
		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "counterRepository", cr);
		ReflectionTestUtils.setField(service, "sirhRepository", sirh);
		ReflectionTestUtils.setField(service, "absReposCompDataConsistencyRules", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "congesExcepCounterServiceImpl", congesExcepCounterServiceImpl);

		// When
		SoldeDto dto = service.getAgentSolde(idAgent, null, null);

		assertEquals("0.0", dto.getSoldeCongeAnnee().toString());
		assertEquals("0.0", dto.getSoldeCongeAnneePrec().toString());
		assertEquals("0.0", dto.getSoldeRecup().toString());
		assertEquals("0.0", dto.getSoldeReposCompAnnee().toString());
		assertEquals("0.0", dto.getSoldeReposCompAnneePrec().toString());
		assertEquals("0.0", dto.getSoldeAsaA48().toString());
		assertEquals("0.0", dto.getSoldeAsaA54().toString());
		assertEquals("0.0", dto.getSoldeAsaA55().toString());
		assertTrue(dto.isAfficheSoldeConge());
		assertTrue(dto.isAfficheSoldeRecup());
		assertTrue(dto.isAfficheSoldeReposComp());
		assertFalse(dto.isAfficheSoldeAsaA48());
		assertFalse(dto.isAfficheSoldeAsaA54());
		assertFalse(dto.isAfficheSoldeAsaA55());
	}

	@Test
	public void getAgentSolde_GetAllSolde() throws ParseException {

		// Given

		Date dateDeb = new DateTime(2014, 1, 1, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 31, 23, 59, 0).toDate();
		Integer idAgent = 9008765;
		double cotaSoldeAnnee = 62.0;
		double cotaSoldeAnneePrec = 25.5;

		AgentRecupCount arc = new AgentRecupCount();
		arc.setIdAgent(idAgent);
		arc.setTotalMinutes(72);

		AgentReposCompCount arcc = new AgentReposCompCount();
		arcc.setIdAgent(idAgent);
		arcc.setTotalMinutes(12);
		arcc.setTotalMinutesAnneeN1(10);

		AgentAsaA48Count arccc = new AgentAsaA48Count();
		arccc.setIdAgent(idAgent);
		arccc.setTotalJours(12.0);
		arccc.setDateDebut(dateDeb);
		arccc.setDateFin(dateFin);

		AgentAsaA54Count arc54 = new AgentAsaA54Count();
		arc54.setIdAgent(idAgent);
		arc54.setTotalJours(12.0);
		arc54.setDateDebut(dateDeb);
		arc54.setDateFin(dateFin);

		AgentAsaA55Count arc55 = new AgentAsaA55Count();
		arc55.setIdAgent(idAgent);
		arc55.setTotalMinutes(12 * 60);
		arc55.setDateDebut(new DateTime(2014, 1, 1, 0, 0, 0).toDate());
		arc55.setDateFin(new DateTime(2014, 1, 31, 23, 59, 0).toDate());

		AgentAsaA55Count arc55bis = new AgentAsaA55Count();
		arc55bis.setIdAgent(idAgent);
		arc55bis.setTotalMinutes(2 * 60);
		arc55bis.setDateDebut(new DateTime(2014, 3, 1, 0, 0, 0).toDate());
		arc55bis.setDateFin(new DateTime(2014, 3, 31, 23, 59, 0).toDate());

		List<AgentAsaA55Count> listeArc55 = new ArrayList<AgentAsaA55Count>();
		listeArc55.add(arc55);
		listeArc55.add(arc55bis);

		SpSold solde = new SpSold();
		solde.setNomatr(8765);
		solde.setSoldeAnneeEnCours(cotaSoldeAnnee);
		solde.setSoldeAnneePrec(cotaSoldeAnneePrec);

		ICounterRepository cr = Mockito.mock(ICounterRepository.class);
		Mockito.when(cr.getAgentCounter(AgentRecupCount.class, idAgent)).thenReturn(arc);
		Mockito.when(cr.getAgentCounter(AgentReposCompCount.class, idAgent)).thenReturn(arcc);
		Mockito.when(cr.getAgentCounterByDate(AgentAsaA48Count.class, 9008765, dateDeb)).thenReturn(arccc);
		Mockito.when(cr.getAgentCounterByDate(AgentAsaA54Count.class, 9008765, dateDeb)).thenReturn(arc54);
		Mockito.when(cr.getAgentCounterByDate(AgentAsaA55Count.class, 9008765, dateDeb)).thenReturn(arc55);
		Mockito.when(cr.getListAgentCounterByDate(9008765, dateDeb, dateFin)).thenReturn(listeArc55);

		ISirhRepository sirh = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirh.getSpsold(idAgent)).thenReturn(solde);

		AbsReposCompensateurDataConsistencyRulesImpl absDataConsistencyRules = Mockito
				.mock(AbsReposCompensateurDataConsistencyRulesImpl.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				// result.getErrors().add("L'agent [%d] ne peut pas avoir de repos compensateur. Les repos compensateurs sont pour les contractuels ou les conventions collectives.");
				return result;
			}
		}).when(absDataConsistencyRules)
				.checkStatutAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class));

		SoldeSpecifiqueDto soldeSpecifiqueDto = new SoldeSpecifiqueDto();
		soldeSpecifiqueDto.setLibelle("libelle 1");
		soldeSpecifiqueDto.setSolde(10.0);
		
		SoldeSpecifiqueDto soldeSpecifiqueDto2 = new SoldeSpecifiqueDto();
		soldeSpecifiqueDto2.setLibelle("libelle 2");
		soldeSpecifiqueDto2.setSolde(20.0);
		
		List<SoldeSpecifiqueDto> listeSoldeSpecifiqueDto = new ArrayList<SoldeSpecifiqueDto>();
		listeSoldeSpecifiqueDto.addAll(Arrays.asList(soldeSpecifiqueDto, soldeSpecifiqueDto2));
		
		CongesExcepCounterServiceImpl congesExcepCounterServiceImpl = Mockito.mock(CongesExcepCounterServiceImpl.class);
		Mockito.when(congesExcepCounterServiceImpl.getListAgentCounterByDate(idAgent, dateDeb, dateFin)).thenReturn(listeSoldeSpecifiqueDto);
		
		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "counterRepository", cr);
		ReflectionTestUtils.setField(service, "sirhRepository", sirh);
		ReflectionTestUtils.setField(service, "absReposCompDataConsistencyRules", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "congesExcepCounterServiceImpl", congesExcepCounterServiceImpl);
		// When
		SoldeDto dto = service.getAgentSolde(idAgent, dateDeb, dateFin);

		assertEquals("72.0", dto.getSoldeRecup().toString());
		assertEquals("62.0", dto.getSoldeCongeAnnee().toString());
		assertEquals("25.5", dto.getSoldeCongeAnneePrec().toString());
		assertEquals("12.0", dto.getSoldeReposCompAnnee().toString());
		assertEquals("10.0", dto.getSoldeReposCompAnneePrec().toString());
		assertEquals(12, dto.getSoldeAsaA48().intValue());
		assertEquals(12, dto.getSoldeAsaA54().intValue());
		assertEquals(12 * 60, dto.getSoldeAsaA55().intValue());
		assertTrue(dto.isAfficheSoldeConge());
		assertTrue(dto.isAfficheSoldeRecup());
		assertTrue(dto.isAfficheSoldeReposComp());
		assertTrue(dto.isAfficheSoldeAsaA48());
		assertTrue(dto.isAfficheSoldeAsaA54());
		assertTrue(dto.isAfficheSoldeAsaA55());
		assertEquals(2, dto.getListeSoldeAsaA55().size());
		assertEquals(2, dto.getListeSoldeCongesExcep().size());
	}

	@Test
	public void getAgentSolde_GetAllSolde_WithDate() throws ParseException {

		// Given
		Integer idAgent = 9008765;
		double cotaSoldeAnnee = 62.0;
		double cotaSoldeAnneePrec = 25.5;

		AgentRecupCount arc = new AgentRecupCount();
		arc.setIdAgent(idAgent);
		arc.setTotalMinutes(72);

		AgentReposCompCount arcc = new AgentReposCompCount();
		arcc.setIdAgent(idAgent);
		arcc.setTotalMinutes(12);
		arcc.setTotalMinutesAnneeN1(10);

		AgentAsaA48Count arccc = new AgentAsaA48Count();
		arccc.setIdAgent(idAgent);
		arccc.setTotalJours(12.0);
		arccc.setDateDebut(new DateTime(2014, 1, 1, 0, 0, 0).toDate());
		arccc.setDateFin(new DateTime(2014, 12, 31, 23, 59, 0).toDate());

		AgentAsaA54Count arcc54 = new AgentAsaA54Count();
		arcc54.setIdAgent(idAgent);
		arcc54.setTotalJours(12.0);
		arcc54.setDateDebut(new DateTime(2014, 1, 1, 0, 0, 0).toDate());
		arcc54.setDateFin(new DateTime(2014, 12, 31, 23, 59, 0).toDate());

		AgentAsaA55Count arcc55 = new AgentAsaA55Count();
		arcc55.setIdAgent(idAgent);
		arcc55.setTotalMinutes(12 * 60);
		arcc55.setDateDebut(new DateTime(2014, 1, 1, 0, 0, 0).toDate());
		arcc55.setDateFin(new DateTime(2014, 1, 31, 23, 59, 0).toDate());

		AgentAsaA55Count arc55bis = new AgentAsaA55Count();
		arc55bis.setIdAgent(idAgent);
		arc55bis.setTotalMinutes(2 * 60);
		arc55bis.setDateDebut(new DateTime(2013, 3, 1, 0, 0, 0).toDate());
		arc55bis.setDateFin(new DateTime(2013, 3, 31, 23, 59, 0).toDate());

		List<AgentAsaA55Count> listeArc55 = new ArrayList<AgentAsaA55Count>();
		listeArc55.add(arcc55);
		listeArc55.add(arc55bis);

		SpSold solde = new SpSold();
		solde.setNomatr(8765);
		solde.setSoldeAnneeEnCours(cotaSoldeAnnee);
		solde.setSoldeAnneePrec(cotaSoldeAnneePrec);

		ICounterRepository cr = Mockito.mock(ICounterRepository.class);
		Mockito.when(cr.getAgentCounter(AgentRecupCount.class, idAgent)).thenReturn(arc);
		Mockito.when(cr.getAgentCounter(AgentReposCompCount.class, idAgent)).thenReturn(arcc);
		Mockito.when(
				cr.getAgentCounterByDate(AgentAsaA48Count.class, 9008765, new DateTime(2014, 1, 1, 0, 0, 0).toDate()))
				.thenReturn(arccc);
		Mockito.when(
				cr.getAgentCounterByDate(AgentAsaA54Count.class, 9008765, new DateTime(2014, 1, 1, 0, 0, 0).toDate()))
				.thenReturn(arcc54);
		Mockito.when(
				cr.getAgentCounterByDate(AgentAsaA55Count.class, 9008765, new DateTime(2014, 1, 1, 0, 0, 0).toDate()))
				.thenReturn(arcc55);
		Mockito.when(
				cr.getListAgentCounterByDate(9008765, new DateTime(2014, 1, 1, 0, 0, 0).toDate(), new DateTime(2014,
						12, 31, 23, 59, 0).toDate())).thenReturn(listeArc55);

		ISirhRepository sirh = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirh.getSpsold(idAgent)).thenReturn(solde);

		AbsReposCompensateurDataConsistencyRulesImpl absDataConsistencyRules = Mockito
				.mock(AbsReposCompensateurDataConsistencyRulesImpl.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				// result.getErrors().add("L'agent [%d] ne peut pas avoir de repos compensateur. Les repos compensateurs sont pour les contractuels ou les conventions collectives.");
				return result;
			}
		}).when(absDataConsistencyRules)
				.checkStatutAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class));

		List<SoldeSpecifiqueDto> listeSoldeSpecifiqueDto = new ArrayList<SoldeSpecifiqueDto>();
		
		CongesExcepCounterServiceImpl congesExcepCounterServiceImpl = Mockito.mock(CongesExcepCounterServiceImpl.class);
		Mockito.when(congesExcepCounterServiceImpl.getListAgentCounterByDate(idAgent, null, null)).thenReturn(listeSoldeSpecifiqueDto);
		
		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "counterRepository", cr);
		ReflectionTestUtils.setField(service, "sirhRepository", sirh);
		ReflectionTestUtils.setField(service, "absReposCompDataConsistencyRules", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "congesExcepCounterServiceImpl", congesExcepCounterServiceImpl);

		Date dateDeb = new DateTime(2013, 1, 1, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 31, 23, 59, 0).toDate();
		SoldeDto dto = service.getAgentSolde(idAgent, dateDeb, dateFin);

		assertEquals("72.0", dto.getSoldeRecup().toString());
		assertEquals("62.0", dto.getSoldeCongeAnnee().toString());
		assertEquals("25.5", dto.getSoldeCongeAnneePrec().toString());
		assertEquals("12.0", dto.getSoldeReposCompAnnee().toString());
		assertEquals("10.0", dto.getSoldeReposCompAnneePrec().toString());
		assertEquals(0, dto.getSoldeAsaA48().intValue());
		assertEquals(0, dto.getSoldeAsaA54().intValue());
		assertEquals(0, dto.getSoldeAsaA55().intValue());
		assertTrue(dto.isAfficheSoldeConge());
		assertTrue(dto.isAfficheSoldeRecup());
		assertTrue(dto.isAfficheSoldeReposComp());
		assertFalse(dto.isAfficheSoldeAsaA48());
		assertFalse(dto.isAfficheSoldeAsaA54());
		assertFalse(dto.isAfficheSoldeAsaA55());
		assertEquals(0, dto.getListeSoldeAsaA55().size());
	}

	@Test
	public void getAgentSolde_AgentExists_NoAsaA48() {

		// Given
		Integer idAgent = 9008765;
		double cotaSoldeAnnee = 62.0;
		double cotaSoldeAnneePrec = 25.5;
		ReturnMessageDto srm = new ReturnMessageDto();
		srm.getErrors().add("erreur");

		AgentRecupCount arc = new AgentRecupCount();
		arc.setIdAgent(idAgent);
		arc.setTotalMinutes(72);

		AgentReposCompCount arcc = new AgentReposCompCount();
		arcc.setIdAgent(idAgent);
		arcc.setTotalMinutes(12);
		arcc.setTotalMinutesAnneeN1(10);

		SpSold solde = new SpSold();
		solde.setNomatr(8765);
		solde.setSoldeAnneeEnCours(cotaSoldeAnnee);
		solde.setSoldeAnneePrec(cotaSoldeAnneePrec);

		ICounterRepository cr = Mockito.mock(ICounterRepository.class);
		Mockito.when(cr.getAgentCounter(AgentRecupCount.class, idAgent)).thenReturn(arc);
		Mockito.when(cr.getAgentCounter(AgentReposCompCount.class, idAgent)).thenReturn(arcc);
		Mockito.when(
				cr.getAgentCounterByDate(AgentAsaA48Count.class, 9008765, new DateTime(2014, 1, 1, 0, 0, 0).toDate()))
				.thenReturn(null);

		ISirhRepository sirh = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirh.getSpsold(idAgent)).thenReturn(solde);

		AbsReposCompensateurDataConsistencyRulesImpl absDataConsistencyRules = Mockito
				.mock(AbsReposCompensateurDataConsistencyRulesImpl.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				result.getErrors()
						.add("L'agent [%d] ne peut pas avoir de repos compensateur. Les repos compensateurs sont pour les contractuels ou les conventions collectives.");
				return result;
			}
		}).when(absDataConsistencyRules)
				.checkStatutAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class));

		List<SoldeSpecifiqueDto> listeSoldeSpecifiqueDto = new ArrayList<SoldeSpecifiqueDto>();
		
		CongesExcepCounterServiceImpl congesExcepCounterServiceImpl = Mockito.mock(CongesExcepCounterServiceImpl.class);
		Mockito.when(congesExcepCounterServiceImpl.getListAgentCounterByDate(idAgent, null, null)).thenReturn(listeSoldeSpecifiqueDto);
		
		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "counterRepository", cr);
		ReflectionTestUtils.setField(service, "sirhRepository", sirh);
		ReflectionTestUtils.setField(service, "absReposCompDataConsistencyRules", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "congesExcepCounterServiceImpl", congesExcepCounterServiceImpl);

		// When
		SoldeDto dto = service.getAgentSolde(idAgent, null, null);

		assertEquals("72.0", dto.getSoldeRecup().toString());
		assertEquals("62.0", dto.getSoldeCongeAnnee().toString());
		assertEquals("25.5", dto.getSoldeCongeAnneePrec().toString());
		assertEquals("12.0", dto.getSoldeReposCompAnnee().toString());
		assertEquals("10.0", dto.getSoldeReposCompAnneePrec().toString());
		assertEquals(0, dto.getSoldeAsaA48().intValue());
		assertTrue(dto.isAfficheSoldeConge());
		assertTrue(dto.isAfficheSoldeRecup());
		assertFalse(dto.isAfficheSoldeReposComp());
		assertFalse(dto.isAfficheSoldeAsaA48());
		assertEquals(0, dto.getListeSoldeAsaA55().size());
	}

	@Test
	public void getAgentSolde_AgentExists_NoAsaA54() {

		// Given
		Integer idAgent = 9008765;
		double cotaSoldeAnnee = 62.0;
		double cotaSoldeAnneePrec = 25.5;
		ReturnMessageDto srm = new ReturnMessageDto();
		srm.getErrors().add("erreur");

		AgentRecupCount arc = new AgentRecupCount();
		arc.setIdAgent(idAgent);
		arc.setTotalMinutes(72);

		AgentReposCompCount arcc = new AgentReposCompCount();
		arcc.setIdAgent(idAgent);
		arcc.setTotalMinutes(12);
		arcc.setTotalMinutesAnneeN1(10);

		SpSold solde = new SpSold();
		solde.setNomatr(8765);
		solde.setSoldeAnneeEnCours(cotaSoldeAnnee);
		solde.setSoldeAnneePrec(cotaSoldeAnneePrec);

		ICounterRepository cr = Mockito.mock(ICounterRepository.class);
		Mockito.when(cr.getAgentCounter(AgentRecupCount.class, idAgent)).thenReturn(arc);
		Mockito.when(cr.getAgentCounter(AgentReposCompCount.class, idAgent)).thenReturn(arcc);
		Mockito.when(
				cr.getAgentCounterByDate(AgentAsaA54Count.class, 9008765, new DateTime(2014, 1, 1, 0, 0, 0).toDate()))
				.thenReturn(null);

		ISirhRepository sirh = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirh.getSpsold(idAgent)).thenReturn(solde);

		AbsReposCompensateurDataConsistencyRulesImpl absDataConsistencyRules = Mockito
				.mock(AbsReposCompensateurDataConsistencyRulesImpl.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				result.getErrors()
						.add("L'agent [%d] ne peut pas avoir de repos compensateur. Les repos compensateurs sont pour les contractuels ou les conventions collectives.");
				return result;
			}
		}).when(absDataConsistencyRules)
				.checkStatutAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class));
		
		List<SoldeSpecifiqueDto> listeSoldeSpecifiqueDto = new ArrayList<SoldeSpecifiqueDto>();
		
		CongesExcepCounterServiceImpl congesExcepCounterServiceImpl = Mockito.mock(CongesExcepCounterServiceImpl.class);
		Mockito.when(congesExcepCounterServiceImpl.getListAgentCounterByDate(idAgent, null, null)).thenReturn(listeSoldeSpecifiqueDto);
		
		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "counterRepository", cr);
		ReflectionTestUtils.setField(service, "sirhRepository", sirh);
		ReflectionTestUtils.setField(service, "absReposCompDataConsistencyRules", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "congesExcepCounterServiceImpl", congesExcepCounterServiceImpl);

		// When
		SoldeDto dto = service.getAgentSolde(idAgent, null, null);

		assertEquals("72.0", dto.getSoldeRecup().toString());
		assertEquals("62.0", dto.getSoldeCongeAnnee().toString());
		assertEquals("25.5", dto.getSoldeCongeAnneePrec().toString());
		assertEquals("12.0", dto.getSoldeReposCompAnnee().toString());
		assertEquals("10.0", dto.getSoldeReposCompAnneePrec().toString());
		assertEquals(0, dto.getSoldeAsaA54().intValue());
		assertTrue(dto.isAfficheSoldeConge());
		assertTrue(dto.isAfficheSoldeRecup());
		assertFalse(dto.isAfficheSoldeReposComp());
		assertFalse(dto.isAfficheSoldeAsaA54());
		assertEquals(0, dto.getListeSoldeAsaA55().size());
	}

	@Test
	public void getAgentSolde_AgentExists_NoAsaA55() {

		// Given
		Integer idAgent = 9008765;
		double cotaSoldeAnnee = 62.0;
		double cotaSoldeAnneePrec = 25.5;
		ReturnMessageDto srm = new ReturnMessageDto();
		srm.getErrors().add("erreur");

		AgentRecupCount arc = new AgentRecupCount();
		arc.setIdAgent(idAgent);
		arc.setTotalMinutes(72);

		AgentReposCompCount arcc = new AgentReposCompCount();
		arcc.setIdAgent(idAgent);
		arcc.setTotalMinutes(12);
		arcc.setTotalMinutesAnneeN1(10);

		SpSold solde = new SpSold();
		solde.setNomatr(8765);
		solde.setSoldeAnneeEnCours(cotaSoldeAnnee);
		solde.setSoldeAnneePrec(cotaSoldeAnneePrec);

		ICounterRepository cr = Mockito.mock(ICounterRepository.class);
		Mockito.when(cr.getAgentCounter(AgentRecupCount.class, idAgent)).thenReturn(arc);
		Mockito.when(cr.getAgentCounter(AgentReposCompCount.class, idAgent)).thenReturn(arcc);
		Mockito.when(
				cr.getAgentCounterByDate(AgentAsaA55Count.class, 9008765, new DateTime(2014, 1, 1, 0, 0, 0).toDate()))
				.thenReturn(null);

		ISirhRepository sirh = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirh.getSpsold(idAgent)).thenReturn(solde);

		AbsReposCompensateurDataConsistencyRulesImpl absDataConsistencyRules = Mockito
				.mock(AbsReposCompensateurDataConsistencyRulesImpl.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				result.getErrors()
						.add("L'agent [%d] ne peut pas avoir de repos compensateur. Les repos compensateurs sont pour les contractuels ou les conventions collectives.");
				return result;
			}
		}).when(absDataConsistencyRules)
				.checkStatutAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class));

		List<SoldeSpecifiqueDto> listeSoldeSpecifiqueDto = new ArrayList<SoldeSpecifiqueDto>();
		
		CongesExcepCounterServiceImpl congesExcepCounterServiceImpl = Mockito.mock(CongesExcepCounterServiceImpl.class);
		Mockito.when(congesExcepCounterServiceImpl.getListAgentCounterByDate(idAgent, null, null)).thenReturn(listeSoldeSpecifiqueDto);
		
		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "counterRepository", cr);
		ReflectionTestUtils.setField(service, "sirhRepository", sirh);
		ReflectionTestUtils.setField(service, "absReposCompDataConsistencyRules", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "congesExcepCounterServiceImpl", congesExcepCounterServiceImpl);

		// When
		SoldeDto dto = service.getAgentSolde(idAgent, null, null);

		assertEquals("72.0", dto.getSoldeRecup().toString());
		assertEquals("62.0", dto.getSoldeCongeAnnee().toString());
		assertEquals("25.5", dto.getSoldeCongeAnneePrec().toString());
		assertEquals("12.0", dto.getSoldeReposCompAnnee().toString());
		assertEquals("10.0", dto.getSoldeReposCompAnneePrec().toString());
		assertEquals(0, dto.getSoldeAsaA55().intValue());
		assertTrue(dto.isAfficheSoldeConge());
		assertTrue(dto.isAfficheSoldeRecup());
		assertFalse(dto.isAfficheSoldeReposComp());
		assertFalse(dto.isAfficheSoldeAsaA55());
		assertEquals(0, dto.getListeSoldeAsaA55().size());
	}

	@Test
	public void getHistoriqueSoldeAgentByTypeAbsence_returnEmptyListe() {
		AgentCount compteurAgent = new AgentCount();
		compteurAgent.setIdAgentCount(1);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getListHisto(9005138, compteurAgent)).thenReturn(
				new ArrayList<AgentHistoAlimManuelle>());

		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		List<HistoriqueSoldeDto> listResult = service.getHistoriqueSoldeAgent(9005138, 7, new Date(), null);

		assertEquals(0, listResult.size());
	}

	@Test
	public void getHistoriqueSoldeAgent_return1Liste_A48() {
		// Given

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(7);
		AgentAsaA48Count compteurAgent = new AgentAsaA48Count();
		compteurAgent.setIdAgentCount(1);
		compteurAgent.setDateDebut(new DateTime(2014, 1, 1, 0, 0, 0).toDate());
		compteurAgent.setDateFin(new DateTime(2014, 1, 31, 0, 0, 0).toDate());
		MotifCompteur motifCompteur = new MotifCompteur();
		motifCompteur.setLibelle("lib motif");
		motifCompteur.setRefTypeAbsence(type);
		AgentHistoAlimManuelle e = new AgentHistoAlimManuelle();
		e.setIdAgent(9005138);
		e.setIdAgentConcerne(9005138);
		e.setType(type);
		e.setText("texte test");
		e.setMotifCompteur(motifCompteur);
		e.setCompteurAgent(compteurAgent);
		List<AgentHistoAlimManuelle> list = new ArrayList<AgentHistoAlimManuelle>();
		list.add(e);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getListHisto(9005138, compteurAgent)).thenReturn(list);
		Mockito.when(
				counterRepository.getAgentCounterByDate(AgentAsaA48Count.class, 9005138, new DateTime(2014, 1, 24, 0,
						0, 0).toDate())).thenReturn(compteurAgent);

		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		List<HistoriqueSoldeDto> listResult = service.getHistoriqueSoldeAgent(9005138, 7, new DateTime(2014, 1, 24, 0,
				0, 0).toDate(), null);

		assertEquals(1, listResult.size());
		assertEquals(e.getText(), listResult.get(0).getTextModification());
		assertEquals(e.getDateModification(), listResult.get(0).getDateModifcation());
		assertEquals(e.getIdAgent(), listResult.get(0).getIdAgentModification());
		assertEquals(motifCompteur.getLibelle(), listResult.get(0).getMotif().getLibelle());
	}

	@Test
	public void getHistoriqueSoldeAgent_return1Liste_A54() {
		// Given

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(8);
		AgentAsaA54Count compteurAgent = new AgentAsaA54Count();
		compteurAgent.setIdAgentCount(1);
		compteurAgent.setDateDebut(new DateTime(2014, 1, 1, 0, 0, 0).toDate());
		compteurAgent.setDateFin(new DateTime(2014, 1, 31, 0, 0, 0).toDate());
		MotifCompteur motifCompteur = new MotifCompteur();
		motifCompteur.setLibelle("lib motif");
		motifCompteur.setRefTypeAbsence(type);
		AgentHistoAlimManuelle e = new AgentHistoAlimManuelle();
		e.setIdAgent(9005138);
		e.setIdAgentConcerne(9005138);
		e.setType(type);
		e.setText("texte test");
		e.setMotifCompteur(motifCompteur);
		e.setCompteurAgent(compteurAgent);
		List<AgentHistoAlimManuelle> list = new ArrayList<AgentHistoAlimManuelle>();
		list.add(e);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getListHisto(9005138, compteurAgent)).thenReturn(list);
		Mockito.when(
				counterRepository.getAgentCounterByDate(AgentAsaA54Count.class, 9005138, new DateTime(2014, 1, 24, 0,
						0, 0).toDate())).thenReturn(compteurAgent);

		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		List<HistoriqueSoldeDto> listResult = service.getHistoriqueSoldeAgent(9005138, 8, new DateTime(2014, 1, 24, 0,
				0, 0).toDate(), null);

		assertEquals(1, listResult.size());
		assertEquals(e.getText(), listResult.get(0).getTextModification());
		assertEquals(e.getDateModification(), listResult.get(0).getDateModifcation());
		assertEquals(e.getIdAgent(), listResult.get(0).getIdAgentModification());
		assertEquals(motifCompteur.getLibelle(), listResult.get(0).getMotif().getLibelle());
	}

	@Test
	public void getHistoriqueSoldeAgent_return1Liste_Recup() {
		// Given

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(3);
		AgentRecupCount compteurAgent = new AgentRecupCount();
		compteurAgent.setIdAgentCount(1);
		MotifCompteur motifCompteur = new MotifCompteur();
		motifCompteur.setLibelle("lib motif");
		motifCompteur.setRefTypeAbsence(type);
		AgentHistoAlimManuelle e = new AgentHistoAlimManuelle();
		e.setIdAgent(9005138);
		e.setIdAgentConcerne(9005138);
		e.setType(type);
		e.setText("texte test");
		e.setMotifCompteur(motifCompteur);
		e.setCompteurAgent(compteurAgent);
		List<AgentHistoAlimManuelle> list = new ArrayList<AgentHistoAlimManuelle>();
		list.add(e);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getListHisto(9005138, compteurAgent)).thenReturn(list);
		Mockito.when(counterRepository.getAgentCounter(AgentRecupCount.class, 9005138)).thenReturn(compteurAgent);

		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		List<HistoriqueSoldeDto> listResult = service.getHistoriqueSoldeAgent(9005138, 3, new DateTime(2014, 1, 24, 0,
				0, 0).toDate(), null);

		assertEquals(1, listResult.size());
		assertEquals(e.getText(), listResult.get(0).getTextModification());
		assertEquals(e.getDateModification(), listResult.get(0).getDateModifcation());
		assertEquals(e.getIdAgent(), listResult.get(0).getIdAgentModification());
		assertEquals(motifCompteur.getLibelle(), listResult.get(0).getMotif().getLibelle());
	}

	@Test
	public void getHistoriqueSoldeAgent_return1Liste_ReposComp() {
		// Given

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(2);
		AgentReposCompCount compteurAgent = new AgentReposCompCount();
		compteurAgent.setIdAgentCount(1);
		MotifCompteur motifCompteur = new MotifCompteur();
		motifCompteur.setLibelle("lib motif");
		motifCompteur.setRefTypeAbsence(type);
		AgentHistoAlimManuelle e = new AgentHistoAlimManuelle();
		e.setIdAgent(9005138);
		e.setIdAgentConcerne(9005138);
		e.setType(type);
		e.setText("texte test");
		e.setMotifCompteur(motifCompteur);
		e.setCompteurAgent(compteurAgent);
		List<AgentHistoAlimManuelle> list = new ArrayList<AgentHistoAlimManuelle>();
		list.add(e);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getListHisto(9005138, compteurAgent)).thenReturn(list);
		Mockito.when(counterRepository.getAgentCounter(AgentReposCompCount.class, 9005138)).thenReturn(compteurAgent);

		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		List<HistoriqueSoldeDto> listResult = service.getHistoriqueSoldeAgent(9005138, 2, new DateTime(2014, 1, 24, 0,
				0, 0).toDate(), null);

		assertEquals(1, listResult.size());
		assertEquals(e.getText(), listResult.get(0).getTextModification());
		assertEquals(e.getDateModification(), listResult.get(0).getDateModifcation());
		assertEquals(e.getIdAgent(), listResult.get(0).getIdAgentModification());
		assertEquals(motifCompteur.getLibelle(), listResult.get(0).getMotif().getLibelle());
	}

	@Test
	public void getHistoriqueSoldeAgent_return1Liste_A55() {
		// Given

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(9);
		AgentAsaA55Count compteurAgent = new AgentAsaA55Count();
		compteurAgent.setIdAgentCount(1);
		compteurAgent.setDateDebut(new DateTime(2014, 1, 1, 0, 0, 0).toDate());
		compteurAgent.setDateFin(new DateTime(2014, 1, 31, 0, 0, 0).toDate());
		MotifCompteur motifCompteur = new MotifCompteur();
		motifCompteur.setLibelle("lib motif");
		motifCompteur.setRefTypeAbsence(type);
		AgentHistoAlimManuelle e = new AgentHistoAlimManuelle();
		e.setIdAgent(9005138);
		e.setIdAgentConcerne(9005138);
		e.setType(type);
		e.setText("texte test");
		e.setMotifCompteur(motifCompteur);
		e.setCompteurAgent(compteurAgent);
		List<AgentHistoAlimManuelle> list = new ArrayList<AgentHistoAlimManuelle>();
		list.add(e);

		List<AgentAsaA55Count> listCompteurAgent = new ArrayList<AgentAsaA55Count>();
		listCompteurAgent.add(compteurAgent);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getListHisto(9005138, compteurAgent)).thenReturn(list);
		Mockito.when(
				counterRepository.getListAgentCounterByDate(9005138, new DateTime(2014, 1, 1, 0, 0, 0).toDate(),
						new DateTime(2014, 12, 31, 23, 59, 59).toDate())).thenReturn(listCompteurAgent);

		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		List<HistoriqueSoldeDto> listResult = service.getHistoriqueSoldeAgent(9005138, 9, new DateTime(2014, 1, 1, 0,
				0, 0).toDate(), new DateTime(2014, 12, 31, 23, 59, 59).toDate());

		assertEquals(1, listResult.size());
		assertEquals(e.getText(), listResult.get(0).getTextModification());
		assertEquals(e.getDateModification(), listResult.get(0).getDateModifcation());
		assertEquals(e.getIdAgent(), listResult.get(0).getIdAgentModification());
		assertEquals(motifCompteur.getLibelle(), listResult.get(0).getMotif().getLibelle());
	}
}
