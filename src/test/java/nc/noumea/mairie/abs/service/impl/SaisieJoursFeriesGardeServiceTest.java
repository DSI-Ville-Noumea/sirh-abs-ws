package nc.noumea.mairie.abs.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentJoursFeriesGarde;
import nc.noumea.mairie.abs.dto.AgentDto;
import nc.noumea.mairie.abs.dto.AgentJoursFeriesGardeDto;
import nc.noumea.mairie.abs.dto.JourDto;
import nc.noumea.mairie.abs.dto.JoursFeriesSaisiesGardeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.dto.SaisieGardeDto;
import nc.noumea.mairie.abs.dto.SirhWsServiceDto;
import nc.noumea.mairie.abs.repository.IAgentJoursFeriesGardeRepository;
import nc.noumea.mairie.abs.service.IAccessRightsService;
import nc.noumea.mairie.ws.ISirhWSConsumer;

import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class SaisieJoursFeriesGardeServiceTest {

	@Test
	public void checkAgentGardeJourDonne_listeVide() {

		List<AgentJoursFeriesGarde> listJoursGardeAgent = new ArrayList<>();
		Date dateFerie = new DateTime().toDate();

		SaisieJoursFeriesGardeService service = new SaisieJoursFeriesGardeService();
		assertFalse(service.checkAgentGardeJourDonne(listJoursGardeAgent, dateFerie));
	}

	@Test
	public void checkAgentGardeJourDonne_mauvaiseDate() {

		List<AgentJoursFeriesGarde> listJoursGardeAgent = new ArrayList<>();
		AgentJoursFeriesGarde a = new AgentJoursFeriesGarde();
		a.setJourFerieChome(new DateTime(2014, 12, 25, 0, 0, 0).toDate());
		listJoursGardeAgent.add(a);

		Date dateFerie = new DateTime(2014, 12, 24, 0, 0, 0).toDate();

		SaisieJoursFeriesGardeService service = new SaisieJoursFeriesGardeService();
		assertFalse(service.checkAgentGardeJourDonne(listJoursGardeAgent, dateFerie));
	}

	@Test
	public void checkAgentGardeJourDonne_ok() {

		List<AgentJoursFeriesGarde> listJoursGardeAgent = new ArrayList<>();
		AgentJoursFeriesGarde a = new AgentJoursFeriesGarde();
		a.setJourFerieChome(new DateTime(2014, 12, 25, 0, 0, 0).toDate());
		listJoursGardeAgent.add(a);

		Date dateFerie = new DateTime(2014, 12, 25, 0, 0, 0).toDate();

		SaisieJoursFeriesGardeService service = new SaisieJoursFeriesGardeService();
		assertTrue(service.checkAgentGardeJourDonne(listJoursGardeAgent, dateFerie));
	}

	@Test
	public void getListAgentsWithJoursFeriesEnGarde_aucunResultat() {

		Date dateDebut = new Date();
		Date dateFin = new Date();

		List<AgentDto> listAgent = new ArrayList<AgentDto>();

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.getAgentsToApproveOrInput(9005138, "codeService")).thenReturn(listAgent);

		List<JourDto> listJoursDto = new ArrayList<JourDto>();

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getListeJoursFeries(dateDebut, dateFin)).thenReturn(listJoursDto);

		SaisieJoursFeriesGardeService service = new SaisieJoursFeriesGardeService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		SaisieGardeDto result = service.getListAgentsWithJoursFeriesEnGarde(9005138, "codeService", dateDebut, dateFin);

		assertEquals(0, result.getListAgentAvecGarde().size());
	}

	@Test
	public void getListAgentsWithJoursFeriesEnGarde_1agent_aucunJourFerie() {

		Date dateDebut = new Date();
		Date dateFin = new Date();

		List<AgentDto> listAgent = new ArrayList<AgentDto>();
		AgentDto agent = new AgentDto();
		agent.setIdAgent(9005131);
		listAgent.add(agent);

		SirhWsServiceDto serviceAgent = new SirhWsServiceDto();
		serviceAgent.setSigle("dpm");

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.getAgentsToApproveOrInput(9005138, "codeService")).thenReturn(listAgent);

		List<JourDto> listJoursDto = new ArrayList<JourDto>();

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getListeJoursFeries(dateDebut, dateFin)).thenReturn(listJoursDto);
		Mockito.when(sirhWSConsumer.getAgentDirection(Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(
				serviceAgent);

		List<AgentJoursFeriesGarde> listJoursGardeAgent = new ArrayList<AgentJoursFeriesGarde>();

		IAgentJoursFeriesGardeRepository agentJoursFeriesGardeRepository = Mockito
				.mock(IAgentJoursFeriesGardeRepository.class);
		Mockito.when(
				agentJoursFeriesGardeRepository
						.getAgentJoursFeriesGardeByIdAgentAndPeriode(9005138, dateDebut, dateFin)).thenReturn(
				listJoursGardeAgent);

		SaisieJoursFeriesGardeService service = new SaisieJoursFeriesGardeService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "agentJoursFeriesGardeRepository", agentJoursFeriesGardeRepository);

		SaisieGardeDto result = service.getListAgentsWithJoursFeriesEnGarde(9005138, "codeService", dateDebut, dateFin);

		assertEquals(1, result.getListAgentAvecGarde().size());
		assertEquals(0, result.getListAgentAvecGarde().get(0).getJoursFeriesEnGarde().size());
	}

	@Test
	public void getListAgentsWithJoursFeriesEnGarde_1agent_1JourFerie_nonGarde() {

		Date dateDebut = new Date();
		Date dateFin = new Date();

		SirhWsServiceDto serviceAgent = new SirhWsServiceDto();
		serviceAgent.setSigle("dpm");

		List<AgentDto> listAgent = new ArrayList<AgentDto>();
		AgentDto agent = new AgentDto();
		agent.setIdAgent(9005138);
		listAgent.add(agent);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.getAgentsToApproveOrInput(9005138, "codeService")).thenReturn(listAgent);

		List<JourDto> listJoursDto = new ArrayList<JourDto>();
		JourDto jour = new JourDto();
		jour.setJour(new DateTime(2014, 12, 25, 0, 0, 0).toDate());
		jour.setFerie(true);
		listJoursDto.add(jour);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getListeJoursFeries(dateDebut, dateFin)).thenReturn(listJoursDto);
		Mockito.when(sirhWSConsumer.getAgentDirection(Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(
				serviceAgent);

		List<AgentJoursFeriesGarde> listJoursGardeAgent = new ArrayList<AgentJoursFeriesGarde>();

		IAgentJoursFeriesGardeRepository agentJoursFeriesGardeRepository = Mockito
				.mock(IAgentJoursFeriesGardeRepository.class);
		Mockito.when(
				agentJoursFeriesGardeRepository
						.getAgentJoursFeriesGardeByIdAgentAndPeriode(9005138, dateDebut, dateFin)).thenReturn(
				listJoursGardeAgent);

		SaisieJoursFeriesGardeService service = new SaisieJoursFeriesGardeService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "agentJoursFeriesGardeRepository", agentJoursFeriesGardeRepository);

		SaisieGardeDto result = service.getListAgentsWithJoursFeriesEnGarde(9005138, "codeService", dateDebut, dateFin);

		assertEquals(1, result.getListAgentAvecGarde().size());
		assertEquals(1, result.getListAgentAvecGarde().get(0).getJoursFeriesEnGarde().size());
		assertEquals(new DateTime(2014, 12, 25, 0, 0, 0).toDate(), result.getListAgentAvecGarde().get(0)
				.getJoursFeriesEnGarde().get(0).getJourFerie());
		assertFalse(result.getListAgentAvecGarde().get(0).getJoursFeriesEnGarde().get(0).isCheck());
	}

	@Test
	public void getListAgentsWithJoursFeriesEnGarde_1agent_1JourFerie_Garde() {

		Date dateDebut = new Date();
		Date dateFin = new Date();

		SirhWsServiceDto serviceAgent = new SirhWsServiceDto();
		serviceAgent.setSigle("dpm");

		List<AgentDto> listAgent = new ArrayList<AgentDto>();
		AgentDto agent = new AgentDto();
		agent.setIdAgent(9005138);
		listAgent.add(agent);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.getAgentsToApproveOrInput(9005138, "codeService")).thenReturn(listAgent);

		List<JourDto> listJoursDto = new ArrayList<JourDto>();
		JourDto jour = new JourDto();
		jour.setJour(new DateTime(2014, 12, 25, 0, 0, 0).toDate());
		jour.setFerie(true);
		listJoursDto.add(jour);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getListeJoursFeries(dateDebut, dateFin)).thenReturn(listJoursDto);
		Mockito.when(sirhWSConsumer.getAgentDirection(Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(
				serviceAgent);

		List<AgentJoursFeriesGarde> listJoursGardeAgent = new ArrayList<AgentJoursFeriesGarde>();
		AgentJoursFeriesGarde repos = new AgentJoursFeriesGarde();
		repos.setIdAgent(9005138);
		repos.setJourFerieChome(new DateTime(2014, 12, 25, 0, 0, 0).toDate());
		listJoursGardeAgent.add(repos);

		IAgentJoursFeriesGardeRepository agentJoursFeriesGardeRepository = Mockito
				.mock(IAgentJoursFeriesGardeRepository.class);
		Mockito.when(
				agentJoursFeriesGardeRepository
						.getAgentJoursFeriesGardeByIdAgentAndPeriode(9005138, dateDebut, dateFin)).thenReturn(
				listJoursGardeAgent);

		SaisieJoursFeriesGardeService service = new SaisieJoursFeriesGardeService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "agentJoursFeriesGardeRepository", agentJoursFeriesGardeRepository);

		SaisieGardeDto result = service.getListAgentsWithJoursFeriesEnGarde(9005138, "codeService", dateDebut, dateFin);

		assertEquals(1, result.getListAgentAvecGarde().size());
		assertEquals(1, result.getListAgentAvecGarde().get(0).getJoursFeriesEnGarde().size());
		assertEquals(new DateTime(2014, 12, 25, 0, 0, 0).toDate(), result.getListAgentAvecGarde().get(0)
				.getJoursFeriesEnGarde().get(0).getJourFerie());
		assertTrue(result.getListAgentAvecGarde().get(0).getJoursFeriesEnGarde().get(0).isCheck());
	}

	@Test
	public void getListAgentsWithJoursFeriesEnGarde_2agents_1JourFerie_Garde() {

		Date dateDebut = new Date();
		Date dateFin = new Date();

		SirhWsServiceDto serviceAgent = new SirhWsServiceDto();
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
		jour.setJour(new DateTime(2014, 12, 25, 0, 0, 0).toDate());
		jour.setFerie(true);
		JourDto jour2 = new JourDto();
		jour2.setJour(new DateTime(2014, 12, 26, 0, 0, 0).toDate());
		jour2.setFerie(true);
		listJoursDto.add(jour);
		listJoursDto.add(jour2);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getListeJoursFeries(dateDebut, dateFin)).thenReturn(listJoursDto);
		Mockito.when(sirhWSConsumer.getAgentDirection(Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(
				serviceAgent);

		List<AgentJoursFeriesGarde> listJoursGardeAgent = new ArrayList<AgentJoursFeriesGarde>();
		AgentJoursFeriesGarde repos = new AgentJoursFeriesGarde();
		repos.setIdAgent(9005138);
		repos.setJourFerieChome(new DateTime(2014, 12, 25, 0, 0, 0).toDate());
		AgentJoursFeriesGarde repos2 = new AgentJoursFeriesGarde();
		repos2.setIdAgent(9005138);
		repos2.setJourFerieChome(new DateTime(2014, 12, 26, 0, 0, 0).toDate());
		listJoursGardeAgent.add(repos);
		listJoursGardeAgent.add(repos2);

		List<AgentJoursFeriesGarde> listJoursGardeAgent2 = new ArrayList<AgentJoursFeriesGarde>();
		AgentJoursFeriesGarde repos3 = new AgentJoursFeriesGarde();
		repos3.setIdAgent(9002990);
		repos3.setJourFerieChome(new DateTime(2014, 12, 25, 0, 0, 0).toDate());
		listJoursGardeAgent2.add(repos3);

		IAgentJoursFeriesGardeRepository agentJoursFeriesGardeRepository = Mockito
				.mock(IAgentJoursFeriesGardeRepository.class);
		Mockito.when(
				agentJoursFeriesGardeRepository
						.getAgentJoursFeriesGardeByIdAgentAndPeriode(9005138, dateDebut, dateFin)).thenReturn(
				listJoursGardeAgent);
		Mockito.when(
				agentJoursFeriesGardeRepository
						.getAgentJoursFeriesGardeByIdAgentAndPeriode(9002990, dateDebut, dateFin)).thenReturn(
				listJoursGardeAgent2);

		SaisieJoursFeriesGardeService service = new SaisieJoursFeriesGardeService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "agentJoursFeriesGardeRepository", agentJoursFeriesGardeRepository);

		SaisieGardeDto result = service.getListAgentsWithJoursFeriesEnGarde(9005138, "codeService", dateDebut, dateFin);

		assertEquals(2, result.getListAgentAvecGarde().size());
		assertEquals(2, result.getListAgentAvecGarde().get(0).getJoursFeriesEnGarde().size());
		assertEquals(2, result.getListAgentAvecGarde().get(1).getJoursFeriesEnGarde().size());
		assertEquals(new DateTime(2014, 12, 25, 0, 0, 0).toDate(), result.getListAgentAvecGarde().get(0)
				.getJoursFeriesEnGarde().get(0).getJourFerie());
		assertEquals(new DateTime(2014, 12, 26, 0, 0, 0).toDate(), result.getListAgentAvecGarde().get(0)
				.getJoursFeriesEnGarde().get(1).getJourFerie());
		assertTrue(result.getListAgentAvecGarde().get(0).getJoursFeriesEnGarde().get(0).isCheck());
		assertTrue(result.getListAgentAvecGarde().get(0).getJoursFeriesEnGarde().get(1).isCheck());

		assertEquals(new DateTime(2014, 12, 25, 0, 0, 0).toDate(), result.getListAgentAvecGarde().get(1)
				.getJoursFeriesEnGarde().get(0).getJourFerie());
		assertTrue(result.getListAgentAvecGarde().get(1).getJoursFeriesEnGarde().get(0).isCheck());
		assertEquals(new DateTime(2014, 12, 26, 0, 0, 0).toDate(), result.getListAgentAvecGarde().get(1)
				.getJoursFeriesEnGarde().get(1).getJourFerie());
		assertFalse(result.getListAgentAvecGarde().get(1).getJoursFeriesEnGarde().get(1).isCheck());
	}

	@Test
	public void setListAgentsWithJoursFeriesEnGarde_DroitKo() {

		Integer idAgent = 9005138;
		Date dateDebut = new DateTime(2014, 12, 01, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 31, 0, 0, 0).toDate();
		List<AgentJoursFeriesGardeDto> listDto = new ArrayList<>();

		AgentDto agent = new AgentDto();
		agent.setIdAgent(idAgent);

		List<JoursFeriesSaisiesGardeDto> joursFeriesEnGarde = new ArrayList<JoursFeriesSaisiesGardeDto>();
		JoursFeriesSaisiesGardeDto jourGarde = new JoursFeriesSaisiesGardeDto();
		jourGarde.setJourFerie(new DateTime(2014, 12, 25, 0, 0, 0).toDate());
		jourGarde.setCheck(true);

		joursFeriesEnGarde.add(jourGarde);

		AgentJoursFeriesGardeDto dto = new AgentJoursFeriesGardeDto();
		dto.setAgent(agent);
		dto.setJoursFeriesEnGarde(joursFeriesEnGarde);
		listDto.add(dto);

		ReturnMessageDto rmd = new ReturnMessageDto();
		rmd.getErrors().add("erreur droit");

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.any(ReturnMessageDto.class))).thenReturn(rmd);

		IAgentJoursFeriesGardeRepository agentJoursFeriesGardeRepository = Mockito
				.mock(IAgentJoursFeriesGardeRepository.class);

		SaisieJoursFeriesGardeService service = new SaisieJoursFeriesGardeService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);

		ReturnMessageDto result = service.setListAgentsWithJoursFeriesEnGarde(idAgent, listDto, dateDebut, dateFin);

		assertEquals("erreur droit", result.getErrors().get(0));
		Mockito.verify(agentJoursFeriesGardeRepository, Mockito.never()).persistEntity(
				Mockito.isA(AgentJoursFeriesGarde.class));
		Mockito.verify(agentJoursFeriesGardeRepository, Mockito.never()).removeEntity(
				Mockito.isA(AgentJoursFeriesGarde.class));
	}

	@Test
	public void setListAgentsWithJoursFeriesEnGarde_jourFerieKo() {

		Integer idAgent = 9005138;
		Date dateDebut = new DateTime(2014, 12, 01, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 31, 0, 0, 0).toDate();
		List<AgentJoursFeriesGardeDto> listDto = new ArrayList<>();

		AgentDto agent = new AgentDto();
		agent.setIdAgent(idAgent);

		List<JoursFeriesSaisiesGardeDto> joursFeriesEnGarde = new ArrayList<JoursFeriesSaisiesGardeDto>();
		JoursFeriesSaisiesGardeDto jourGarde = new JoursFeriesSaisiesGardeDto();
		jourGarde.setJourFerie(new DateTime(2014, 12, 25, 0, 0, 0).toDate());
		jourGarde.setCheck(true);

		joursFeriesEnGarde.add(jourGarde);

		AgentJoursFeriesGardeDto dto = new AgentJoursFeriesGardeDto();
		dto.setAgent(agent);
		dto.setJoursFeriesEnGarde(joursFeriesEnGarde);
		listDto.add(dto);

		ReturnMessageDto rmd = new ReturnMessageDto();

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.any(ReturnMessageDto.class))).thenReturn(rmd);

		List<AgentJoursFeriesGarde> listJoursGardeAgent = new ArrayList<AgentJoursFeriesGarde>();

		IAgentJoursFeriesGardeRepository agentJoursFeriesGardeRepository = Mockito
				.mock(IAgentJoursFeriesGardeRepository.class);
		Mockito.when(
				agentJoursFeriesGardeRepository
						.getAgentJoursFeriesGardeByIdAgentAndPeriode(idAgent, dateDebut, dateFin)).thenReturn(
				listJoursGardeAgent);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isJourHoliday(jourGarde.getJourFerie())).thenReturn(false);

		SaisieJoursFeriesGardeService service = new SaisieJoursFeriesGardeService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "agentJoursFeriesGardeRepository", agentJoursFeriesGardeRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		ReturnMessageDto result = service.setListAgentsWithJoursFeriesEnGarde(idAgent, listDto, dateDebut, dateFin);

		assertEquals("Le jour de garde 25/12/2014 n'est pas un jour férié ou chômé.", result.getErrors().get(0));
		Mockito.verify(agentJoursFeriesGardeRepository, Mockito.never()).persistEntity(
				Mockito.isA(AgentJoursFeriesGarde.class));
		Mockito.verify(agentJoursFeriesGardeRepository, Mockito.never()).removeEntity(
				Mockito.isA(AgentJoursFeriesGarde.class));
	}

	@Test
	public void setListAgentsWithJoursFeriesEnGarde_1insert() {

		Integer idAgent = 9005138;
		Date dateDebut = new DateTime(2014, 12, 01, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 31, 0, 0, 0).toDate();
		List<AgentJoursFeriesGardeDto> listDto = new ArrayList<>();

		AgentDto agent = new AgentDto();
		agent.setIdAgent(idAgent);

		List<JoursFeriesSaisiesGardeDto> joursFeriesEnGarde = new ArrayList<JoursFeriesSaisiesGardeDto>();
		JoursFeriesSaisiesGardeDto jourGarde = new JoursFeriesSaisiesGardeDto();
		jourGarde.setJourFerie(new DateTime(2014, 12, 25, 0, 0, 0).toDate());
		jourGarde.setCheck(true);

		joursFeriesEnGarde.add(jourGarde);

		AgentJoursFeriesGardeDto dto = new AgentJoursFeriesGardeDto();
		dto.setAgent(agent);
		dto.setJoursFeriesEnGarde(joursFeriesEnGarde);
		listDto.add(dto);

		ReturnMessageDto rmd = new ReturnMessageDto();

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.any(ReturnMessageDto.class))).thenReturn(rmd);

		List<AgentJoursFeriesGarde> listJoursGardeAgent = new ArrayList<AgentJoursFeriesGarde>();

		IAgentJoursFeriesGardeRepository agentJoursFeriesGardeRepository = Mockito
				.mock(IAgentJoursFeriesGardeRepository.class);
		Mockito.when(
				agentJoursFeriesGardeRepository
						.getAgentJoursFeriesGardeByIdAgentAndPeriode(idAgent, dateDebut, dateFin)).thenReturn(
				listJoursGardeAgent);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isJourHoliday(jourGarde.getJourFerie())).thenReturn(true);

		SaisieJoursFeriesGardeService service = new SaisieJoursFeriesGardeService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "agentJoursFeriesGardeRepository", agentJoursFeriesGardeRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		ReturnMessageDto result = service.setListAgentsWithJoursFeriesEnGarde(idAgent, listDto, dateDebut, dateFin);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(agentJoursFeriesGardeRepository, Mockito.times(1)).persistEntity(
				Mockito.isA(AgentJoursFeriesGarde.class));
		Mockito.verify(agentJoursFeriesGardeRepository, Mockito.never()).removeEntity(
				Mockito.isA(AgentJoursFeriesGarde.class));
	}

	@Test
	public void setListAgentsWithJoursFeriesEnGarde_3insert_1delete() {

		Integer idAgent = 9005138;
		Date dateDebut = new DateTime(2014, 12, 01, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 31, 0, 0, 0).toDate();
		List<AgentJoursFeriesGardeDto> listDto = new ArrayList<>();

		// 1er agent avec 2 jours repos
		AgentDto agent = new AgentDto();
		agent.setIdAgent(idAgent);

		List<JoursFeriesSaisiesGardeDto> joursFeriesEnGarde = new ArrayList<JoursFeriesSaisiesGardeDto>();
		JoursFeriesSaisiesGardeDto jourGarde = new JoursFeriesSaisiesGardeDto();
		jourGarde.setJourFerie(new DateTime(2014, 12, 25, 0, 0, 0).toDate());
		jourGarde.setCheck(true);

		JoursFeriesSaisiesGardeDto jourGarde2 = new JoursFeriesSaisiesGardeDto();
		jourGarde2.setJourFerie(new DateTime(2014, 12, 26, 0, 0, 0).toDate());
		jourGarde2.setCheck(true);

		joursFeriesEnGarde.add(jourGarde);
		joursFeriesEnGarde.add(jourGarde2);

		AgentJoursFeriesGardeDto dto = new AgentJoursFeriesGardeDto();
		dto.setAgent(agent);
		dto.setJoursFeriesEnGarde(joursFeriesEnGarde);

		// 2e agent un jour de repos
		AgentDto agent2 = new AgentDto();
		agent2.setIdAgent(9002990);

		List<JoursFeriesSaisiesGardeDto> joursFeriesEnGarde2 = new ArrayList<JoursFeriesSaisiesGardeDto>();
		JoursFeriesSaisiesGardeDto jourGarde3 = new JoursFeriesSaisiesGardeDto();
		jourGarde3.setJourFerie(new DateTime(2014, 12, 25, 0, 0, 0).toDate());
		jourGarde3.setCheck(true);

		JoursFeriesSaisiesGardeDto jourGarde4 = new JoursFeriesSaisiesGardeDto();
		jourGarde4.setJourFerie(new DateTime(2014, 12, 26, 0, 0, 0).toDate());
		jourGarde4.setCheck(false);

		joursFeriesEnGarde2.add(jourGarde3);
		joursFeriesEnGarde2.add(jourGarde4);

		AgentJoursFeriesGardeDto dto2 = new AgentJoursFeriesGardeDto();
		dto2.setAgent(agent2);
		dto2.setJoursFeriesEnGarde(joursFeriesEnGarde2);

		listDto.add(dto);
		listDto.add(dto2);

		ReturnMessageDto rmd = new ReturnMessageDto();

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.any(ReturnMessageDto.class))).thenReturn(rmd);

		List<AgentJoursFeriesGarde> listJoursGardeAgent = new ArrayList<AgentJoursFeriesGarde>();
		AgentJoursFeriesGarde reposExist = new AgentJoursFeriesGarde();
		reposExist.setIdAgent(9005138);
		reposExist.setJourFerieChome(new DateTime(2014, 12, 11, 0, 0, 0).toDate());
		listJoursGardeAgent.add(reposExist);

		IAgentJoursFeriesGardeRepository agentJoursFeriesGardeRepository = Mockito
				.mock(IAgentJoursFeriesGardeRepository.class);
		Mockito.when(
				agentJoursFeriesGardeRepository
						.getAgentJoursFeriesGardeByIdAgentAndPeriode(idAgent, dateDebut, dateFin)).thenReturn(
				listJoursGardeAgent);
		Mockito.when(
				agentJoursFeriesGardeRepository
						.getAgentJoursFeriesGardeByIdAgentAndPeriode(9002990, dateDebut, dateFin)).thenReturn(
				new ArrayList<AgentJoursFeriesGarde>());

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isJourHoliday(jourGarde.getJourFerie())).thenReturn(true);
		Mockito.when(sirhWSConsumer.isJourHoliday(jourGarde2.getJourFerie())).thenReturn(true);
		Mockito.when(sirhWSConsumer.isJourHoliday(jourGarde3.getJourFerie())).thenReturn(true);

		SaisieJoursFeriesGardeService service = new SaisieJoursFeriesGardeService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "agentJoursFeriesGardeRepository", agentJoursFeriesGardeRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		ReturnMessageDto result = service.setListAgentsWithJoursFeriesEnGarde(idAgent, listDto, dateDebut, dateFin);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(agentJoursFeriesGardeRepository, Mockito.times(3)).persistEntity(
				Mockito.isA(AgentJoursFeriesGarde.class));
		Mockito.verify(agentJoursFeriesGardeRepository, Mockito.times(1)).removeEntity(
				Mockito.isA(AgentJoursFeriesGarde.class));
	}
}
