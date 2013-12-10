package nc.noumea.mairie.abs.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Date;

import nc.noumea.mairie.abs.domain.AgentRecupCount;
import nc.noumea.mairie.abs.domain.AgentWeekRecup;
import nc.noumea.mairie.abs.dto.SoldeDto;
import nc.noumea.mairie.abs.repository.IRecuperationRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.abs.service.AgentNotFoundException;
import nc.noumea.mairie.abs.service.NotAMondayException;
import nc.noumea.mairie.sirh.domain.Agent;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class RecuperationServiceTest {

	@Test
	public void addRecuperationToAgent_AgentDoesNotExists_ThrowAgentNotFoundException() {

		// Given
		Integer idAgent = 9008765;
		Date dateMonday = new LocalDate(2013, 9, 30).toDate();

		ISirhRepository sR = Mockito.mock(ISirhRepository.class);
		Mockito.when(sR.getAgent(idAgent)).thenReturn(null);

		RecuperationService service = new RecuperationService();
		ReflectionTestUtils.setField(service, "sirhRepository", sR);

		// When
		try {
			service.addRecuperationToAgent(idAgent, dateMonday, 90);
		} catch (AgentNotFoundException ex) {
			return;
		}

		fail("Should have thrown an AgentNotFoundException");
	}

	@Test
	public void addRecuperationToAgent_DateIsNotAMonday_ThrowDateIsNotAMondayException() {

		// Given
		Integer idAgent = 9008765;
		Date dateMonday = new LocalDate(2013, 9, 29).toDate();

		ISirhRepository sR = Mockito.mock(ISirhRepository.class);
		Mockito.when(sR.getAgent(idAgent)).thenReturn(new Agent());

		HelperService hS = Mockito.mock(HelperService.class);
		Mockito.when(hS.isDateAMonday(dateMonday)).thenReturn(false);

		RecuperationService service = new RecuperationService();
		ReflectionTestUtils.setField(service, "sirhRepository", sR);
		ReflectionTestUtils.setField(service, "helperService", hS);

		// When
		try {
			service.addRecuperationToAgent(idAgent, dateMonday, 90);
		} catch (NotAMondayException ex) {
			return;
		}

		fail("Should have thrown an NotAMondayException");
	}

	@Test
	public void addRecuperationToAgent_AgentHasNoRecupForThatWeek_AgentHasNoAccount_CreateItemsAndAddQteToAccount() {

		// Given
		Integer idAgent = 9008765;
		Date dateMonday = new LocalDate(2013, 9, 30).toDate();

		ISirhRepository sR = Mockito.mock(ISirhRepository.class);
		Mockito.when(sR.getAgent(idAgent)).thenReturn(new Agent());

		IRecuperationRepository rr = Mockito.mock(IRecuperationRepository.class);
		Mockito.when(rr.getWeekRecupForAgentAndDate(idAgent, dateMonday)).thenReturn(null);
		Mockito.when(rr.getAgentRecupCount(idAgent)).thenReturn(null);

		HelperService hS = Mockito.mock(HelperService.class);
		Mockito.when(hS.getCurrentDate()).thenReturn(new DateTime(2013, 4, 2, 8, 56, 12).toDate());
		Mockito.when(hS.isDateAMonday(dateMonday)).thenReturn(true);

		RecuperationService service = new RecuperationService();
		ReflectionTestUtils.setField(service, "recuperationRepository", rr);
		ReflectionTestUtils.setField(service, "helperService", hS);
		ReflectionTestUtils.setField(service, "sirhRepository", sR);

		// When
		int result = service.addRecuperationToAgent(idAgent, dateMonday, 90);

		// Then
		assertEquals(90, result);

		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(AgentWeekRecup.class));
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(AgentRecupCount.class));
	}

	@Test
	public void addRecuperationToAgent_AgentHasARecupForThatWeek_UpdateItemAndAddQteToAccount() {

		// Given
		Integer idAgent = 9008765;
		Date dateMonday = new LocalDate(2013, 9, 30).toDate();

		ISirhRepository sR = Mockito.mock(ISirhRepository.class);
		Mockito.when(sR.getAgent(idAgent)).thenReturn(new Agent());

		AgentWeekRecup awr = new AgentWeekRecup();
		awr.setMinutesRecup(80);
		IRecuperationRepository rr = Mockito.mock(IRecuperationRepository.class);
		Mockito.when(rr.getWeekRecupForAgentAndDate(idAgent, dateMonday)).thenReturn(awr);
		AgentRecupCount arc = new AgentRecupCount();
		arc.setTotalMinutes(10);
		Mockito.when(rr.getAgentRecupCount(idAgent)).thenReturn(arc);

		HelperService hS = Mockito.mock(HelperService.class);
		Mockito.when(hS.isDateAMonday(dateMonday)).thenReturn(true);

		RecuperationService service = new RecuperationService();
		ReflectionTestUtils.setField(service, "recuperationRepository", rr);
		ReflectionTestUtils.setField(service, "sirhRepository", sR);
		ReflectionTestUtils.setField(service, "helperService", hS);

		// When
		int result = service.addRecuperationToAgent(idAgent, dateMonday, 90);

		// Then
		assertEquals(20, result);
		assertEquals(20, (int) arc.getTotalMinutes());
		assertEquals(90, (int) awr.getMinutesRecup());
	}

	@Test
	public void addRecuperationToAgent_AgentHasARecupForThatWeek_NewValueIsLower_UpdateItemAndAddQteToAccount() {

		// Given
		Integer idAgent = 9008765;
		Date dateMonday = new LocalDate(2013, 9, 30).toDate();

		ISirhRepository sR = Mockito.mock(ISirhRepository.class);
		Mockito.when(sR.getAgent(idAgent)).thenReturn(new Agent());

		AgentWeekRecup awr = new AgentWeekRecup();
		awr.setMinutesRecup(80);
		IRecuperationRepository rr = Mockito.mock(IRecuperationRepository.class);
		Mockito.when(rr.getWeekRecupForAgentAndDate(idAgent, dateMonday)).thenReturn(awr);
		AgentRecupCount arc = new AgentRecupCount();
		arc.setTotalMinutes(10);
		Mockito.when(rr.getAgentRecupCount(idAgent)).thenReturn(arc);

		HelperService hS = Mockito.mock(HelperService.class);
		Mockito.when(hS.isDateAMonday(dateMonday)).thenReturn(true);

		RecuperationService service = new RecuperationService();
		ReflectionTestUtils.setField(service, "recuperationRepository", rr);
		ReflectionTestUtils.setField(service, "sirhRepository", sR);
		ReflectionTestUtils.setField(service, "helperService", hS);

		// When
		int result = service.addRecuperationToAgent(idAgent, dateMonday, 70);

		// Then
		assertEquals(0, result);
		assertEquals(0, (int) arc.getTotalMinutes());
		assertEquals(70, (int) awr.getMinutesRecup());
	}

	@Test
	public void getAgentSoldeRecuperation_AgentDoesNotExists() {

		// Given
		Integer idAgent = 9008765;

		IRecuperationRepository rr = Mockito.mock(IRecuperationRepository.class);
		Mockito.when(rr.getAgentRecupCount(idAgent)).thenReturn(null);

		RecuperationService service = new RecuperationService();
		ReflectionTestUtils.setField(service, "recuperationRepository", rr);

		// When
		SoldeDto dto = service.getAgentSoldeRecuperation(idAgent);

		assertEquals((Integer) 0, dto.getSoldeRecup());
	}

	@Test
	public void getAgentSoldeRecuperation_AgentExists() {

		// Given
		Integer idAgent = 9008765;
		AgentRecupCount arc = new AgentRecupCount();
		arc.setIdAgent(idAgent);
		arc.setTotalMinutes(72);

		IRecuperationRepository rr = Mockito.mock(IRecuperationRepository.class);
		Mockito.when(rr.getAgentRecupCount(idAgent)).thenReturn(arc);

		RecuperationService service = new RecuperationService();
		ReflectionTestUtils.setField(service, "recuperationRepository", rr);

		// When
		SoldeDto dto = service.getAgentSoldeRecuperation(idAgent);

		assertEquals((Integer) 72, dto.getSoldeRecup());
	}
}
