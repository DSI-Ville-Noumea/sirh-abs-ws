package nc.noumea.mairie.abs.service.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.ws.ISirhWSConsumer;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class AgentServiceTest {

	@Test
	public void getAgentOptimise_1appel() {
		
		List<AgentGeneriqueDto> listAgentsExistants  = new ArrayList<AgentGeneriqueDto>();
		Integer idAgent = 9005138;
		AgentGeneriqueDto agentDto = new AgentGeneriqueDto();
		agentDto.setNomUsage("CHARVET");
		
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(idAgent)).thenReturn(agentDto);
		
		AgentService service = new AgentService();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		
		AgentGeneriqueDto agent = service.getAgentOptimise(listAgentsExistants, idAgent);
		
		assertEquals(agent.getDisplayNom(), "CHARVET");
		Mockito.verify(sirhWSConsumer, Mockito.times(1)).getAgent(idAgent);
	}

	@Test
	public void getAgentOptimise_PasDAppel() {

		Integer idAgent = 9005138;
		
		AgentGeneriqueDto agentDto = new AgentGeneriqueDto();
		agentDto.setNomUsage("CHARVET");
		agentDto.setIdAgent(idAgent);
		
		List<AgentGeneriqueDto> listAgentsExistants  = new ArrayList<AgentGeneriqueDto>();
		listAgentsExistants.add(agentDto);
		
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(idAgent)).thenReturn(agentDto);
		
		AgentService service = new AgentService();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		
		AgentGeneriqueDto agent = service.getAgentOptimise(listAgentsExistants, idAgent);
		
		assertEquals(agent.getDisplayNom(), "CHARVET");
		Mockito.verify(sirhWSConsumer, Mockito.times(0)).getAgent(idAgent);
	}

	@Test
	public void getAgentOptimiseWithAgentWithServiceDto_1appel() {
		
		Date date = new Date();
		
		List<AgentWithServiceDto> listAgentsExistants  = new ArrayList<AgentWithServiceDto>();
		Integer idAgent = 9005138;
		AgentWithServiceDto agentDto = new AgentWithServiceDto();
		agentDto.setNom("CHARVET");
		
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgentService(idAgent, date)).thenReturn(agentDto);
		
		AgentService service = new AgentService();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		
		AgentWithServiceDto agent = service.getAgentOptimise(listAgentsExistants, idAgent, date);
		
		assertEquals(agent.getNom(), "CHARVET");
		Mockito.verify(sirhWSConsumer, Mockito.times(1)).getAgentService(idAgent, date);
	}

	@Test
	public void getAgentOptimiseWithAgentWithServiceDto_PasDAppel() {
		
		Date date = new Date();

		Integer idAgent = 9005138;
		
		AgentWithServiceDto agentDto = new AgentWithServiceDto();
		agentDto.setNom("CHARVET");
		agentDto.setIdAgent(idAgent);
		
		List<AgentWithServiceDto> listAgentsExistants  = new ArrayList<AgentWithServiceDto>();
		listAgentsExistants.add(agentDto);
		
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgentService(idAgent, date)).thenReturn(agentDto);
		
		AgentService service = new AgentService();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		
		AgentWithServiceDto agent = service.getAgentOptimise(listAgentsExistants, idAgent, date);
		
		assertEquals(agent.getNom(), "CHARVET");
		Mockito.verify(sirhWSConsumer, Mockito.times(0)).getAgentService(idAgent, date);
	}
}
