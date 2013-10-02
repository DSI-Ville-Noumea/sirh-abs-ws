package nc.noumea.mairie.abs.service.impl;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import nc.noumea.mairie.abs.domain.AgentRecupCount;
import nc.noumea.mairie.abs.domain.AgentWeekRecup;
import nc.noumea.mairie.abs.repository.IRecuperationRepository;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class RecuperationServiceTest {

	@Test
	public void addRecuperationToAgent_AgentHasNoRecupForThatWeek_CreateItemAndAddQteToAccount() {
		
		// Given
		Integer idAgent = 9008765;
		Date dateMonday = new LocalDate(2013, 9, 30).toDate();
		
		IRecuperationRepository rr = Mockito.mock(IRecuperationRepository.class);
		Mockito.when(rr.getWeekRecupForAgentAndDate(idAgent, dateMonday)).thenReturn(null);
		AgentRecupCount arc = new AgentRecupCount();
		arc.setTotalMinutes(10);
		Mockito.when(rr.getAgentRecupCount(idAgent)).thenReturn(arc);
		
		RecuperationService service = new RecuperationService();
		ReflectionTestUtils.setField(service, "recuperationRepository", rr);
		
		// When
		int result = service.addRecuperationToAgent(idAgent, dateMonday, 90);
		
		// Then
		assertEquals(100, result);
		assertEquals(100, (int) arc.getTotalMinutes());
	}
	
	@Test
	public void addRecuperationToAgent_AgentHasARecupForThatWeek_UpdateItemAndAddQteToAccount() {
	
		// Given
		Integer idAgent = 9008765;
		Date dateMonday = new LocalDate(2013, 9, 30).toDate();
		
		AgentWeekRecup awr = new AgentWeekRecup();
		awr.setMinutesRecup(80);
		IRecuperationRepository rr = Mockito.mock(IRecuperationRepository.class);
		Mockito.when(rr.getWeekRecupForAgentAndDate(idAgent, dateMonday)).thenReturn(awr);
		AgentRecupCount arc = new AgentRecupCount();
		arc.setTotalMinutes(10);
		Mockito.when(rr.getAgentRecupCount(idAgent)).thenReturn(arc);
		
		RecuperationService service = new RecuperationService();
		ReflectionTestUtils.setField(service, "recuperationRepository", rr);
		
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
		
		AgentWeekRecup awr = new AgentWeekRecup();
		awr.setMinutesRecup(80);
		IRecuperationRepository rr = Mockito.mock(IRecuperationRepository.class);
		Mockito.when(rr.getWeekRecupForAgentAndDate(idAgent, dateMonday)).thenReturn(awr);
		AgentRecupCount arc = new AgentRecupCount();
		arc.setTotalMinutes(10);
		Mockito.when(rr.getAgentRecupCount(idAgent)).thenReturn(arc);
		
		RecuperationService service = new RecuperationService();
		ReflectionTestUtils.setField(service, "recuperationRepository", rr);
		
		// When
		int result = service.addRecuperationToAgent(idAgent, dateMonday, 70);
		
		// Then
		assertEquals(0, result);
		assertEquals(0, (int) arc.getTotalMinutes());
		assertEquals(70, (int) awr.getMinutesRecup());
	}
}
