package nc.noumea.mairie.abs.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentAsaA48Count;
import nc.noumea.mairie.abs.domain.AgentAsaA54Count;
import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;
import nc.noumea.mairie.abs.domain.AgentRecupCount;
import nc.noumea.mairie.abs.domain.AgentReposCompCount;
import nc.noumea.mairie.abs.domain.MotifCompteur;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.dto.HistoriqueSoldeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.dto.SoldeDto;
import nc.noumea.mairie.abs.repository.ICounterRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
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

		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "counterRepository", cr);
		ReflectionTestUtils.setField(service, "sirhRepository", sirh);
		ReflectionTestUtils.setField(service, "absReposCompDataConsistencyRules", absDataConsistencyRules);

		// When
		SoldeDto dto = service.getAgentSolde(idAgent, null);

		assertEquals("0.0", dto.getSoldeCongeAnnee().toString());
		assertEquals("0.0", dto.getSoldeCongeAnneePrec().toString());
		assertEquals("0.0", dto.getSoldeRecup().toString());
		assertEquals("0.0", dto.getSoldeReposCompAnnee().toString());
		assertEquals("0.0", dto.getSoldeReposCompAnneePrec().toString());
		assertEquals("0.0", dto.getSoldeAsaA48().toString());
		assertEquals("0.0", dto.getSoldeAsaA54().toString());
		assertTrue(dto.isAfficheSoldeConge());
		assertTrue(dto.isAfficheSoldeRecup());
		assertTrue(dto.isAfficheSoldeReposComp());
		assertFalse(dto.isAfficheSoldeAsaA48());
		assertFalse(dto.isAfficheSoldeAsaA54());
	}

	@Test
	public void getAgentSolde_GetAllSolde() throws ParseException {

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

		AgentAsaA54Count arc54 = new AgentAsaA54Count();
		arc54.setIdAgent(idAgent);
		arc54.setTotalJours(12.0);
		arc54.setDateDebut(new DateTime(2014, 1, 1, 0, 0, 0).toDate());
		arc54.setDateFin(new DateTime(2014, 12, 31, 23, 59, 0).toDate());

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
				.thenReturn(arc54);

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

		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "counterRepository", cr);
		ReflectionTestUtils.setField(service, "sirhRepository", sirh);
		ReflectionTestUtils.setField(service, "absReposCompDataConsistencyRules", absDataConsistencyRules);

		Date dateDeb = new DateTime(2014, 1, 1, 0, 0, 0).toDate();
		// Date dateFin = new DateTime(2014, 12, 31, 23, 59, 0).toDate();
		// When
		SoldeDto dto = service.getAgentSolde(idAgent, dateDeb);

		assertEquals("72.0", dto.getSoldeRecup().toString());
		assertEquals("62.0", dto.getSoldeCongeAnnee().toString());
		assertEquals("25.5", dto.getSoldeCongeAnneePrec().toString());
		assertEquals("12.0", dto.getSoldeReposCompAnnee().toString());
		assertEquals("10.0", dto.getSoldeReposCompAnneePrec().toString());
		assertEquals(12, dto.getSoldeAsaA48().intValue());
		assertEquals(12, dto.getSoldeAsaA54().intValue());
		assertTrue(dto.isAfficheSoldeConge());
		assertTrue(dto.isAfficheSoldeRecup());
		assertTrue(dto.isAfficheSoldeReposComp());
		assertTrue(dto.isAfficheSoldeAsaA48());
		assertTrue(dto.isAfficheSoldeAsaA54());
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

		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "counterRepository", cr);
		ReflectionTestUtils.setField(service, "sirhRepository", sirh);
		ReflectionTestUtils.setField(service, "absReposCompDataConsistencyRules", absDataConsistencyRules);

		Date dateDeb = new DateTime(2013, 1, 1, 0, 0, 0).toDate();
		SoldeDto dto = service.getAgentSolde(idAgent, dateDeb);

		assertEquals("72.0", dto.getSoldeRecup().toString());
		assertEquals("62.0", dto.getSoldeCongeAnnee().toString());
		assertEquals("25.5", dto.getSoldeCongeAnneePrec().toString());
		assertEquals("12.0", dto.getSoldeReposCompAnnee().toString());
		assertEquals("10.0", dto.getSoldeReposCompAnneePrec().toString());
		assertEquals(0, dto.getSoldeAsaA48().intValue());
		assertEquals(0, dto.getSoldeAsaA54().intValue());
		assertTrue(dto.isAfficheSoldeConge());
		assertTrue(dto.isAfficheSoldeRecup());
		assertTrue(dto.isAfficheSoldeReposComp());
		assertFalse(dto.isAfficheSoldeAsaA48());
		assertFalse(dto.isAfficheSoldeAsaA54());
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

		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "counterRepository", cr);
		ReflectionTestUtils.setField(service, "sirhRepository", sirh);
		ReflectionTestUtils.setField(service, "absReposCompDataConsistencyRules", absDataConsistencyRules);

		// When
		SoldeDto dto = service.getAgentSolde(idAgent, null);

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

		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "counterRepository", cr);
		ReflectionTestUtils.setField(service, "sirhRepository", sirh);
		ReflectionTestUtils.setField(service, "absReposCompDataConsistencyRules", absDataConsistencyRules);

		// When
		SoldeDto dto = service.getAgentSolde(idAgent, null);

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
	}

	@Test
	public void getHistoriqueSoldeAgentByTypeAbsence_returnEmptyListe() {

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getListHistoByRefTypeAbsenceAndAgent(9005138, 7)).thenReturn(
				new ArrayList<AgentHistoAlimManuelle>());

		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		List<HistoriqueSoldeDto> listResult = service.getHistoriqueSoldeAgentByTypeAbsence(9005138, 7);

		assertEquals(0, listResult.size());
	}

	@Test
	public void getHistoriqueSoldeAgentByTypeAbsence_return1Liste() {
		// Given
		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(7);
		MotifCompteur motifCompteur = new MotifCompteur();
		motifCompteur.setLibelle("lib motif");
		motifCompteur.setRefTypeAbsence(type);
		AgentHistoAlimManuelle e = new AgentHistoAlimManuelle();
		e.setIdAgent(9005138);
		e.setIdAgentConcerne(9005138);
		e.setType(type);
		e.setText("texte test");
		e.setMotifCompteur(motifCompteur);
		List<AgentHistoAlimManuelle> list = new ArrayList<AgentHistoAlimManuelle>();
		list.add(e);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getListHistoByRefTypeAbsenceAndAgent(9005138, 7)).thenReturn(list);

		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		List<HistoriqueSoldeDto> listResult = service.getHistoriqueSoldeAgentByTypeAbsence(9005138, 7);

		assertEquals(1, listResult.size());
		assertEquals(e.getText(), listResult.get(0).getTextModification());
		assertEquals(e.getDateModification(), listResult.get(0).getDateModifcation());
		assertEquals(e.getIdAgent(), listResult.get(0).getIdAgentModification());
		assertEquals(motifCompteur.getLibelle(), listResult.get(0).getMotif().getLibelle());
	}
}
