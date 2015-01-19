package nc.noumea.mairie.abs.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentJoursFeriesRepos;
import nc.noumea.mairie.abs.dto.AgentDto;
import nc.noumea.mairie.abs.dto.AgentJoursFeriesReposDto;
import nc.noumea.mairie.abs.dto.JourDto;
import nc.noumea.mairie.abs.dto.JoursFeriesSaisiesReposDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.dto.SaisieReposDto;
import nc.noumea.mairie.abs.dto.SirhWsServiceDto;
import nc.noumea.mairie.abs.repository.IAgentJoursFeriesReposRepository;
import nc.noumea.mairie.abs.service.IAccessRightsService;
import nc.noumea.mairie.ws.ISirhWSConsumer;

import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class SaisieJoursFeriesReposServiceTest {

	@Test
	public void checkAgentReposJourDonne_listeVide() {
		
		List<AgentJoursFeriesRepos> listJoursReposAgent = new ArrayList<>();
		Date dateFerie = new DateTime().toDate();
		
		SaisieJoursFeriesReposService service = new SaisieJoursFeriesReposService();
		assertFalse(service.checkAgentReposJourDonne(listJoursReposAgent, dateFerie));
	}

	@Test
	public void checkAgentReposJourDonne_mauvaiseDate() {
		
		List<AgentJoursFeriesRepos> listJoursReposAgent = new ArrayList<>();
		AgentJoursFeriesRepos a = new AgentJoursFeriesRepos();
		a.setJourFerieChome(new DateTime(2014,12,25,0,0,0).toDate());
		listJoursReposAgent.add(a);
		
		Date dateFerie = new DateTime(2014,12,24,0,0,0).toDate();
		
		SaisieJoursFeriesReposService service = new SaisieJoursFeriesReposService();
		assertFalse(service.checkAgentReposJourDonne(listJoursReposAgent, dateFerie));
	}

	@Test
	public void checkAgentReposJourDonne_ok() {
		
		List<AgentJoursFeriesRepos> listJoursReposAgent = new ArrayList<>();
		AgentJoursFeriesRepos a = new AgentJoursFeriesRepos();
		a.setJourFerieChome(new DateTime(2014,12,25,0,0,0).toDate());
		listJoursReposAgent.add(a);
		
		Date dateFerie = new DateTime(2014,12,25,0,0,0).toDate();
		
		SaisieJoursFeriesReposService service = new SaisieJoursFeriesReposService();
		assertTrue(service.checkAgentReposJourDonne(listJoursReposAgent, dateFerie));
	}
	
	@Test
	public void getListAgentsWithJoursFeriesEnRepos_aucunResultat() {
		
		Date dateDebut = new Date();
		Date dateFin = new Date();
		
		List<AgentDto> listAgent = new ArrayList<AgentDto>();
		
		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.getAgentsToApproveOrInput(9005138, "codeService")).thenReturn(listAgent);
		
		List<JourDto> listJoursDto = new ArrayList<JourDto>();
		
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getListeJoursFeries(dateDebut, dateFin)).thenReturn(listJoursDto);
		
		SaisieJoursFeriesReposService service = new SaisieJoursFeriesReposService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		
		SaisieReposDto result = service.getListAgentsWithJoursFeriesEnRepos(9005138, "codeService", dateDebut, dateFin);
		
		assertEquals(0, result.getListAgentAvecRepos().size());
	}
	
	@Test
	public void getListAgentsWithJoursFeriesEnRepos_1agent_aucunJourFerie() {
		
		Date dateDebut = new Date();
		Date dateFin = new Date();
		
		List<AgentDto> listAgent = new ArrayList<AgentDto>();
		AgentDto agent = new AgentDto();
		agent.setIdAgent(9005131);
		listAgent.add(agent);
		
		SirhWsServiceDto serviceAgent =  new SirhWsServiceDto();
		serviceAgent.setSigle("dpm");
		
		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.getAgentsToApproveOrInput(9005138, "codeService")).thenReturn(listAgent);
		
		List<JourDto> listJoursDto = new ArrayList<JourDto>();
		
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getListeJoursFeries(dateDebut, dateFin)).thenReturn(listJoursDto);
		Mockito.when(sirhWSConsumer.getAgentDirection(Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(serviceAgent);
		
		List<AgentJoursFeriesRepos> listJoursReposAgent = new ArrayList<AgentJoursFeriesRepos>();
		
		IAgentJoursFeriesReposRepository agentJoursFeriesReposRepository = Mockito.mock(IAgentJoursFeriesReposRepository.class);
		Mockito.when(agentJoursFeriesReposRepository.getAgentJoursFeriesReposByIdAgentAndPeriode(9005138, dateDebut, dateFin))
			.thenReturn(listJoursReposAgent);
		
		SaisieJoursFeriesReposService service = new SaisieJoursFeriesReposService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "agentJoursFeriesReposRepository", agentJoursFeriesReposRepository);
		
		SaisieReposDto result = service.getListAgentsWithJoursFeriesEnRepos(9005138, "codeService", dateDebut, dateFin);
		
		assertEquals(1, result.getListAgentAvecRepos().size());
		assertEquals(0, result.getListAgentAvecRepos().get(0).getJoursFeriesEnRepos().size());
	}
	
	@Test
	public void getListAgentsWithJoursFeriesEnRepos_1agent_1JourFerie_nonRepos() {
		
		Date dateDebut = new Date();
		Date dateFin = new Date();
		
		SirhWsServiceDto serviceAgent =  new SirhWsServiceDto();
		serviceAgent.setSigle("dpm");
		
		List<AgentDto> listAgent = new ArrayList<AgentDto>();
		AgentDto agent = new AgentDto();
		agent.setIdAgent(9005138);
		listAgent.add(agent);
		
		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.getAgentsToApproveOrInput(9005138, "codeService")).thenReturn(listAgent);
		
		List<JourDto> listJoursDto = new ArrayList<JourDto>();
		JourDto jour = new JourDto();
		jour.setJour(new DateTime(2014,12,25,0,0,0).toDate());
		jour.setFerie(true);
		listJoursDto.add(jour);
		
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getListeJoursFeries(dateDebut, dateFin)).thenReturn(listJoursDto);
		Mockito.when(sirhWSConsumer.getAgentDirection(Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(serviceAgent);
		
		List<AgentJoursFeriesRepos> listJoursReposAgent = new ArrayList<AgentJoursFeriesRepos>();
		
		IAgentJoursFeriesReposRepository agentJoursFeriesReposRepository = Mockito.mock(IAgentJoursFeriesReposRepository.class);
		Mockito.when(agentJoursFeriesReposRepository.getAgentJoursFeriesReposByIdAgentAndPeriode(9005138, dateDebut, dateFin))
			.thenReturn(listJoursReposAgent);
		
		SaisieJoursFeriesReposService service = new SaisieJoursFeriesReposService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "agentJoursFeriesReposRepository", agentJoursFeriesReposRepository);
		
		SaisieReposDto result = service.getListAgentsWithJoursFeriesEnRepos(9005138, "codeService", dateDebut, dateFin);
		
		assertEquals(1, result.getListAgentAvecRepos().size());
		assertEquals(1, result.getListAgentAvecRepos().get(0).getJoursFeriesEnRepos().size());
		assertEquals(new DateTime(2014,12,25,0,0,0).toDate(), result.getListAgentAvecRepos().get(0).getJoursFeriesEnRepos().get(0).getJourFerie());
		assertFalse(result.getListAgentAvecRepos().get(0).getJoursFeriesEnRepos().get(0).isCheck());
	}
	
	@Test
	public void getListAgentsWithJoursFeriesEnRepos_1agent_1JourFerie_Repos() {
		
		Date dateDebut = new Date();
		Date dateFin = new Date();
		
		SirhWsServiceDto serviceAgent =  new SirhWsServiceDto();
		serviceAgent.setSigle("dpm");
		
		List<AgentDto> listAgent = new ArrayList<AgentDto>();
		AgentDto agent = new AgentDto();
		agent.setIdAgent(9005138);
		listAgent.add(agent);
		
		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.getAgentsToApproveOrInput(9005138, "codeService")).thenReturn(listAgent);
		
		List<JourDto> listJoursDto = new ArrayList<JourDto>();
		JourDto jour = new JourDto();
		jour.setJour(new DateTime(2014,12,25,0,0,0).toDate());
		jour.setFerie(true);
		listJoursDto.add(jour);
		
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getListeJoursFeries(dateDebut, dateFin)).thenReturn(listJoursDto);
		Mockito.when(sirhWSConsumer.getAgentDirection(Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(serviceAgent);
		
		List<AgentJoursFeriesRepos> listJoursReposAgent = new ArrayList<AgentJoursFeriesRepos>();
		AgentJoursFeriesRepos repos = new AgentJoursFeriesRepos();
		repos.setIdAgent(9005138);
		repos.setJourFerieChome(new DateTime(2014,12,25,0,0,0).toDate());
		listJoursReposAgent.add(repos);
		
		IAgentJoursFeriesReposRepository agentJoursFeriesReposRepository = Mockito.mock(IAgentJoursFeriesReposRepository.class);
		Mockito.when(agentJoursFeriesReposRepository.getAgentJoursFeriesReposByIdAgentAndPeriode(9005138, dateDebut, dateFin))
			.thenReturn(listJoursReposAgent);
		
		SaisieJoursFeriesReposService service = new SaisieJoursFeriesReposService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "agentJoursFeriesReposRepository", agentJoursFeriesReposRepository);
		
		SaisieReposDto result = service.getListAgentsWithJoursFeriesEnRepos(9005138, "codeService", dateDebut, dateFin);
		
		assertEquals(1, result.getListAgentAvecRepos().size());
		assertEquals(1, result.getListAgentAvecRepos().get(0).getJoursFeriesEnRepos().size());
		assertEquals(new DateTime(2014,12,25,0,0,0).toDate(), result.getListAgentAvecRepos().get(0).getJoursFeriesEnRepos().get(0).getJourFerie());
		assertTrue(result.getListAgentAvecRepos().get(0).getJoursFeriesEnRepos().get(0).isCheck());
	}
	
	@Test
	public void getListAgentsWithJoursFeriesEnRepos_2agents_1JourFerie_Repos() {
		
		Date dateDebut = new Date();
		Date dateFin = new Date();
		
		SirhWsServiceDto serviceAgent =  new SirhWsServiceDto();
		serviceAgent.setSigle("dpm");
		
		List<AgentDto> listAgent = new ArrayList<AgentDto>();
		AgentDto agent = new AgentDto();
		agent.setIdAgent(9005138);
		AgentDto agent2 = new AgentDto();
		agent2.setIdAgent(9002990);
		listAgent.add(agent);
		listAgent.add(agent2);
		
		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.getAgentsToApproveOrInput(9005138, "codeService")).thenReturn(listAgent);
		
		List<JourDto> listJoursDto = new ArrayList<JourDto>();
		JourDto jour = new JourDto();
		jour.setJour(new DateTime(2014,12,25,0,0,0).toDate());
		jour.setFerie(true);
		JourDto jour2 = new JourDto();
		jour2.setJour(new DateTime(2014,12,26,0,0,0).toDate());
		jour2.setFerie(true);
		listJoursDto.add(jour);
		listJoursDto.add(jour2);
		
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getListeJoursFeries(dateDebut, dateFin)).thenReturn(listJoursDto);
		Mockito.when(sirhWSConsumer.getAgentDirection(Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(serviceAgent);
		
		List<AgentJoursFeriesRepos> listJoursReposAgent = new ArrayList<AgentJoursFeriesRepos>();
		AgentJoursFeriesRepos repos = new AgentJoursFeriesRepos();
		repos.setIdAgent(9005138);
		repos.setJourFerieChome(new DateTime(2014,12,25,0,0,0).toDate());
		AgentJoursFeriesRepos repos2 = new AgentJoursFeriesRepos();
		repos2.setIdAgent(9005138);
		repos2.setJourFerieChome(new DateTime(2014,12,26,0,0,0).toDate());
		listJoursReposAgent.add(repos);
		listJoursReposAgent.add(repos2);
		
		List<AgentJoursFeriesRepos> listJoursReposAgent2 = new ArrayList<AgentJoursFeriesRepos>();
		AgentJoursFeriesRepos repos3 = new AgentJoursFeriesRepos();
		repos3.setIdAgent(9002990);
		repos3.setJourFerieChome(new DateTime(2014,12,25,0,0,0).toDate());
		listJoursReposAgent2.add(repos3);
		
		IAgentJoursFeriesReposRepository agentJoursFeriesReposRepository = Mockito.mock(IAgentJoursFeriesReposRepository.class);
		Mockito.when(agentJoursFeriesReposRepository.getAgentJoursFeriesReposByIdAgentAndPeriode(9005138, dateDebut, dateFin))
			.thenReturn(listJoursReposAgent);
		Mockito.when(agentJoursFeriesReposRepository.getAgentJoursFeriesReposByIdAgentAndPeriode(9002990, dateDebut, dateFin))
			.thenReturn(listJoursReposAgent2);
		
		SaisieJoursFeriesReposService service = new SaisieJoursFeriesReposService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "agentJoursFeriesReposRepository", agentJoursFeriesReposRepository);
		
		SaisieReposDto result = service.getListAgentsWithJoursFeriesEnRepos(9005138, "codeService", dateDebut, dateFin);
		
		assertEquals(2, result.getListAgentAvecRepos().size());
		assertEquals(2, result.getListAgentAvecRepos().get(0).getJoursFeriesEnRepos().size());
		assertEquals(2, result.getListAgentAvecRepos().get(1).getJoursFeriesEnRepos().size());
		assertEquals(new DateTime(2014,12,25,0,0,0).toDate(), result.getListAgentAvecRepos().get(0).getJoursFeriesEnRepos().get(0).getJourFerie());
		assertEquals(new DateTime(2014,12,26,0,0,0).toDate(), result.getListAgentAvecRepos().get(0).getJoursFeriesEnRepos().get(1).getJourFerie());
		assertTrue(result.getListAgentAvecRepos().get(0).getJoursFeriesEnRepos().get(0).isCheck());
		assertTrue(result.getListAgentAvecRepos().get(0).getJoursFeriesEnRepos().get(1).isCheck());
		
		assertEquals(new DateTime(2014,12,25,0,0,0).toDate(), result.getListAgentAvecRepos().get(1).getJoursFeriesEnRepos().get(0).getJourFerie());
		assertTrue(result.getListAgentAvecRepos().get(1).getJoursFeriesEnRepos().get(0).isCheck());
		assertEquals(new DateTime(2014,12,26,0,0,0).toDate(), result.getListAgentAvecRepos().get(1).getJoursFeriesEnRepos().get(1).getJourFerie());
		assertFalse(result.getListAgentAvecRepos().get(1).getJoursFeriesEnRepos().get(1).isCheck());
	}
	
	@Test
	public void setListAgentsWithJoursFeriesEnRepos_DroitKo() {
		
		Integer idAgent = 9005138; 
		Date dateDebut = new DateTime(2014,12,01,0,0,0).toDate();
		Date dateFin = new DateTime(2014,12,31,0,0,0).toDate();
		List<AgentJoursFeriesReposDto> listDto = new ArrayList<>();
		
		AgentDto agent = new AgentDto();
		agent.setIdAgent(idAgent);
		
		List<JoursFeriesSaisiesReposDto> joursFeriesEnRepos = new ArrayList<JoursFeriesSaisiesReposDto>();
		JoursFeriesSaisiesReposDto jourRepos = new JoursFeriesSaisiesReposDto();
		jourRepos.setJourFerie(new DateTime(2014,12,25,0,0,0).toDate());
		jourRepos.setCheck(true);
		
		joursFeriesEnRepos.add(jourRepos);
		
		AgentJoursFeriesReposDto dto = new AgentJoursFeriesReposDto();
		dto.setAgent(agent);
		dto.setJoursFeriesEnRepos(joursFeriesEnRepos);
		listDto.add(dto);
		
		ReturnMessageDto rmd = new ReturnMessageDto();
		rmd.getErrors().add("erreur droit");
		
		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(), Mockito.any(ReturnMessageDto.class)))
			.thenReturn(rmd);
		
		IAgentJoursFeriesReposRepository agentJoursFeriesReposRepository = Mockito.mock(IAgentJoursFeriesReposRepository.class);
		
		SaisieJoursFeriesReposService service = new SaisieJoursFeriesReposService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		
		ReturnMessageDto result = service.setListAgentsWithJoursFeriesEnRepos(idAgent, listDto, dateDebut, dateFin);
		
		assertEquals("erreur droit", result.getErrors().get(0));
		Mockito.verify(agentJoursFeriesReposRepository, Mockito.never()).persistEntity(Mockito.isA(AgentJoursFeriesRepos.class));
		Mockito.verify(agentJoursFeriesReposRepository, Mockito.never()).removeEntity(Mockito.isA(AgentJoursFeriesRepos.class));
	}
	
	@Test
	public void setListAgentsWithJoursFeriesEnRepos_jourFerieKo() {
		
		Integer idAgent = 9005138; 
		Date dateDebut = new DateTime(2014,12,01,0,0,0).toDate();
		Date dateFin = new DateTime(2014,12,31,0,0,0).toDate();
		List<AgentJoursFeriesReposDto> listDto = new ArrayList<>();
		
		AgentDto agent = new AgentDto();
		agent.setIdAgent(idAgent);
		
		List<JoursFeriesSaisiesReposDto> joursFeriesEnRepos = new ArrayList<JoursFeriesSaisiesReposDto>();
		JoursFeriesSaisiesReposDto jourRepos = new JoursFeriesSaisiesReposDto();
		jourRepos.setJourFerie(new DateTime(2014,12,25,0,0,0).toDate());
		jourRepos.setCheck(true);
		
		joursFeriesEnRepos.add(jourRepos);
		
		AgentJoursFeriesReposDto dto = new AgentJoursFeriesReposDto();
		dto.setAgent(agent);
		dto.setJoursFeriesEnRepos(joursFeriesEnRepos);
		listDto.add(dto);
		
		ReturnMessageDto rmd = new ReturnMessageDto();
		
		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(), Mockito.any(ReturnMessageDto.class)))
			.thenReturn(rmd);
		
		List<AgentJoursFeriesRepos> listJoursReposAgent = new ArrayList<AgentJoursFeriesRepos>();
				
		IAgentJoursFeriesReposRepository agentJoursFeriesReposRepository = Mockito.mock(IAgentJoursFeriesReposRepository.class);
		Mockito.when(agentJoursFeriesReposRepository.getAgentJoursFeriesReposByIdAgentAndPeriode(idAgent, dateDebut, dateFin)).thenReturn(listJoursReposAgent);
		
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isJourHoliday(jourRepos.getJourFerie())).thenReturn(false);
		
		SaisieJoursFeriesReposService service = new SaisieJoursFeriesReposService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "agentJoursFeriesReposRepository", agentJoursFeriesReposRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		
		ReturnMessageDto result = service.setListAgentsWithJoursFeriesEnRepos(idAgent, listDto, dateDebut, dateFin);

		assertEquals("Le jour de repos 25/12/2014 n'est pas un jour férié ou chômé.", result.getErrors().get(0));
		Mockito.verify(agentJoursFeriesReposRepository, Mockito.never()).persistEntity(Mockito.isA(AgentJoursFeriesRepos.class));
		Mockito.verify(agentJoursFeriesReposRepository, Mockito.never()).removeEntity(Mockito.isA(AgentJoursFeriesRepos.class));
	}
	
	@Test
	public void setListAgentsWithJoursFeriesEnRepos_1insert() {
		
		Integer idAgent = 9005138; 
		Date dateDebut = new DateTime(2014,12,01,0,0,0).toDate();
		Date dateFin = new DateTime(2014,12,31,0,0,0).toDate();
		List<AgentJoursFeriesReposDto> listDto = new ArrayList<>();
		
		AgentDto agent = new AgentDto();
		agent.setIdAgent(idAgent);
		
		List<JoursFeriesSaisiesReposDto> joursFeriesEnRepos = new ArrayList<JoursFeriesSaisiesReposDto>();
		JoursFeriesSaisiesReposDto jourRepos = new JoursFeriesSaisiesReposDto();
		jourRepos.setJourFerie(new DateTime(2014,12,25,0,0,0).toDate());
		jourRepos.setCheck(true);
		
		joursFeriesEnRepos.add(jourRepos);
		
		AgentJoursFeriesReposDto dto = new AgentJoursFeriesReposDto();
		dto.setAgent(agent);
		dto.setJoursFeriesEnRepos(joursFeriesEnRepos);
		listDto.add(dto);
		
		ReturnMessageDto rmd = new ReturnMessageDto();
		
		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(), Mockito.any(ReturnMessageDto.class)))
			.thenReturn(rmd);
		
		List<AgentJoursFeriesRepos> listJoursReposAgent = new ArrayList<AgentJoursFeriesRepos>();
				
		IAgentJoursFeriesReposRepository agentJoursFeriesReposRepository = Mockito.mock(IAgentJoursFeriesReposRepository.class);
		Mockito.when(agentJoursFeriesReposRepository.getAgentJoursFeriesReposByIdAgentAndPeriode(idAgent, dateDebut, dateFin)).thenReturn(listJoursReposAgent);
		
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isJourHoliday(jourRepos.getJourFerie())).thenReturn(true);
		
		SaisieJoursFeriesReposService service = new SaisieJoursFeriesReposService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "agentJoursFeriesReposRepository", agentJoursFeriesReposRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		
		ReturnMessageDto result = service.setListAgentsWithJoursFeriesEnRepos(idAgent, listDto, dateDebut, dateFin);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(agentJoursFeriesReposRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentJoursFeriesRepos.class));
		Mockito.verify(agentJoursFeriesReposRepository, Mockito.never()).removeEntity(Mockito.isA(AgentJoursFeriesRepos.class));
	}
	
	@Test
	public void setListAgentsWithJoursFeriesEnRepos_3insert_1delete() {
		
		Integer idAgent = 9005138; 
		Date dateDebut = new DateTime(2014,12,01,0,0,0).toDate();
		Date dateFin = new DateTime(2014,12,31,0,0,0).toDate();
		List<AgentJoursFeriesReposDto> listDto = new ArrayList<>();
		
		// 1er agent avec 2 jours repos
		AgentDto agent = new AgentDto();
		agent.setIdAgent(idAgent);
		
		List<JoursFeriesSaisiesReposDto> joursFeriesEnRepos = new ArrayList<JoursFeriesSaisiesReposDto>();
		JoursFeriesSaisiesReposDto jourRepos = new JoursFeriesSaisiesReposDto();
		jourRepos.setJourFerie(new DateTime(2014,12,25,0,0,0).toDate());
		jourRepos.setCheck(true);
		
		JoursFeriesSaisiesReposDto jourRepos2 = new JoursFeriesSaisiesReposDto();
		jourRepos2.setJourFerie(new DateTime(2014,12,26,0,0,0).toDate());
		jourRepos2.setCheck(true);
		
		joursFeriesEnRepos.add(jourRepos);
		joursFeriesEnRepos.add(jourRepos2);
		
		AgentJoursFeriesReposDto dto = new AgentJoursFeriesReposDto();
		dto.setAgent(agent);
		dto.setJoursFeriesEnRepos(joursFeriesEnRepos);
		
		// 2e agent un jour de repos
		AgentDto agent2 = new AgentDto();
		agent2.setIdAgent(9002990);
		
		List<JoursFeriesSaisiesReposDto> joursFeriesEnRepos2 = new ArrayList<JoursFeriesSaisiesReposDto>();
		JoursFeriesSaisiesReposDto jourRepos3 = new JoursFeriesSaisiesReposDto();
		jourRepos3.setJourFerie(new DateTime(2014,12,25,0,0,0).toDate());
		jourRepos3.setCheck(true);
		
		JoursFeriesSaisiesReposDto jourRepos4 = new JoursFeriesSaisiesReposDto();
		jourRepos4.setJourFerie(new DateTime(2014,12,26,0,0,0).toDate());
		jourRepos4.setCheck(false);
		
		joursFeriesEnRepos2.add(jourRepos3);
		joursFeriesEnRepos2.add(jourRepos4);
		
		AgentJoursFeriesReposDto dto2 = new AgentJoursFeriesReposDto();
		dto2.setAgent(agent2);
		dto2.setJoursFeriesEnRepos(joursFeriesEnRepos2);

		listDto.add(dto);
		listDto.add(dto2);
		
		ReturnMessageDto rmd = new ReturnMessageDto();
		
		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(), Mockito.any(ReturnMessageDto.class)))
			.thenReturn(rmd);
		
		List<AgentJoursFeriesRepos> listJoursReposAgent = new ArrayList<AgentJoursFeriesRepos>();
		AgentJoursFeriesRepos reposExist = new AgentJoursFeriesRepos();
		reposExist.setIdAgent(9005138);
		reposExist.setJourFerieChome(new DateTime(2014,12,11,0,0,0).toDate());
		listJoursReposAgent.add(reposExist);
				
		IAgentJoursFeriesReposRepository agentJoursFeriesReposRepository = Mockito.mock(IAgentJoursFeriesReposRepository.class);
		Mockito.when(agentJoursFeriesReposRepository.getAgentJoursFeriesReposByIdAgentAndPeriode(idAgent, dateDebut, dateFin)).thenReturn(listJoursReposAgent);
		Mockito.when(agentJoursFeriesReposRepository.getAgentJoursFeriesReposByIdAgentAndPeriode(9002990, dateDebut, dateFin)).thenReturn(new ArrayList<AgentJoursFeriesRepos>());
		
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isJourHoliday(jourRepos.getJourFerie())).thenReturn(true);
		Mockito.when(sirhWSConsumer.isJourHoliday(jourRepos2.getJourFerie())).thenReturn(true);
		Mockito.when(sirhWSConsumer.isJourHoliday(jourRepos3.getJourFerie())).thenReturn(true);
		
		SaisieJoursFeriesReposService service = new SaisieJoursFeriesReposService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "agentJoursFeriesReposRepository", agentJoursFeriesReposRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		
		ReturnMessageDto result = service.setListAgentsWithJoursFeriesEnRepos(idAgent, listDto, dateDebut, dateFin);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(agentJoursFeriesReposRepository, Mockito.times(3)).persistEntity(Mockito.isA(AgentJoursFeriesRepos.class));
		Mockito.verify(agentJoursFeriesReposRepository, Mockito.times(1)).removeEntity(Mockito.isA(AgentJoursFeriesRepos.class));
	}
}
