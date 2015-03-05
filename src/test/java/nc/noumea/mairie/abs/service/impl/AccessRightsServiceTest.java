package nc.noumea.mairie.abs.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nc.noumea.mairie.abs.domain.Droit;
import nc.noumea.mairie.abs.domain.DroitDroitsAgent;
import nc.noumea.mairie.abs.domain.DroitProfil;
import nc.noumea.mairie.abs.domain.DroitsAgent;
import nc.noumea.mairie.abs.domain.Profil;
import nc.noumea.mairie.abs.domain.ProfilEnum;
import nc.noumea.mairie.abs.dto.AccessRightsDto;
import nc.noumea.mairie.abs.dto.AgentDto;
import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.abs.dto.InputterDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.dto.ServiceDto;
import nc.noumea.mairie.abs.dto.SirhWsServiceDto;
import nc.noumea.mairie.abs.dto.ViseursDto;
import nc.noumea.mairie.abs.repository.AccessRightsRepository;
import nc.noumea.mairie.abs.repository.IAccessRightsRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.ws.ISirhWSConsumer;

import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.staticmock.MockStaticEntityMethods;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@MockStaticEntityMethods
public class AccessRightsServiceTest {

	@Autowired
	AccessRightsRepository repository;

	@PersistenceContext(unitName = "absPersistenceUnit")
	private EntityManager absEntityManager;

	@Test
	public void getAgentAccessRights_AgentHasNoRights_ReturnFalseEverywhere() {

		// Given
		Integer idAgent = 906543;
		Droit droits = new Droit();

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentAccessRights(idAgent)).thenReturn(droits);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgentDirection(Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(
				new SirhWsServiceDto());

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		// When
		AccessRightsDto result = service.getAgentAccessRights(idAgent);

		// Then
		assertFalse(result.isSaisie());
		assertFalse(result.isModification());
		assertFalse(result.isSuppression());
		assertFalse(result.isImpression());
		assertFalse(result.isViserVisu());
		assertFalse(result.isViserModif());
		assertFalse(result.isApprouverVisu());
		assertFalse(result.isApprouverModif());
		assertFalse(result.isAnnuler());
		assertFalse(result.isVisuSolde());
		assertFalse(result.isMajSolde());
		assertFalse(result.isDroitAcces());
		assertFalse(result.isSaisieRepos());
	}

	@Test
	public void getAgentAccessRights_AgentHas1Right_ReturnThisRights() {

		// Given
		Integer idAgent = 906543;

		Profil pr = new Profil();
		pr.setSaisie(true);
		pr.setModification(true);
		pr.setSuppression(true);
		pr.setImpression(true);
		pr.setViserVisu(false);
		pr.setViserModif(false);
		pr.setApprouverVisu(false);
		pr.setApprouverModif(false);
		pr.setAnnuler(true);
		pr.setVisuSolde(true);
		pr.setMajSolde(true);
		pr.setDroitAcces(false);

		DroitProfil dpr = new DroitProfil();
		dpr.setProfil(pr);

		Droit da = new Droit();
		da.setIdAgent(idAgent);
		da.getDroitProfils().add(dpr);

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentAccessRights(idAgent)).thenReturn(da);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgentDirection(Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(
				new SirhWsServiceDto());

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		// When
		AccessRightsDto result = service.getAgentAccessRights(idAgent);

		// Then
		assertTrue(result.isSaisie());
		assertTrue(result.isModification());
		assertTrue(result.isSuppression());
		assertTrue(result.isImpression());
		assertFalse(result.isViserVisu());
		assertFalse(result.isViserModif());
		assertFalse(result.isApprouverVisu());
		assertFalse(result.isApprouverModif());
		assertTrue(result.isAnnuler());
		assertTrue(result.isVisuSolde());
		assertTrue(result.isMajSolde());
		assertFalse(result.isDroitAcces());
		assertFalse(result.isSaisieRepos());
	}

	@Test
	public void getAgentAccessRights_AgentHas2Rights_ReturnOrLogicRights() {

		// Given
		Integer idAgent = 906543;

		Profil pr = new Profil();
		pr.setSaisie(true);
		pr.setModification(true);
		pr.setSuppression(true);
		pr.setImpression(true);
		pr.setViserVisu(false);
		pr.setViserModif(false);
		pr.setApprouverVisu(false);
		pr.setApprouverModif(false);
		pr.setAnnuler(true);
		pr.setVisuSolde(true);
		pr.setMajSolde(true);
		pr.setDroitAcces(false);

		DroitProfil dpr = new DroitProfil();
		dpr.setProfil(pr);

		Profil pr2 = new Profil();
		pr2.setSaisie(true);
		pr2.setModification(true);
		pr2.setSuppression(true);
		pr2.setImpression(true);
		pr2.setViserVisu(false);
		pr2.setViserModif(true);
		pr2.setApprouverVisu(false);
		pr2.setApprouverModif(true);
		pr2.setAnnuler(true);
		pr2.setVisuSolde(true);
		pr2.setMajSolde(true);
		pr2.setDroitAcces(false);

		DroitProfil dpr2 = new DroitProfil();
		dpr2.setProfil(pr2);

		Droit da = new Droit();
		da.setIdAgent(900);
		da.getDroitProfils().add(dpr);
		da.getDroitProfils().add(dpr2);

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentAccessRights(idAgent)).thenReturn(da);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgentDirection(Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(
				new SirhWsServiceDto());

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		// When
		AccessRightsDto result = service.getAgentAccessRights(idAgent);

		// Then
		assertTrue(result.isSaisie());
		assertTrue(result.isModification());
		assertTrue(result.isSuppression());
		assertTrue(result.isImpression());
		assertFalse(result.isViserVisu());
		assertTrue(result.isViserModif());
		assertFalse(result.isApprouverVisu());
		assertTrue(result.isApprouverModif());
		assertTrue(result.isAnnuler());
		assertTrue(result.isVisuSolde());
		assertTrue(result.isMajSolde());
		assertFalse(result.isDroitAcces());
		assertFalse(result.isSaisieRepos());
	}

	@Test
	public void getAgentAccessRights_AgentHasRightSaisieRepos_AgentDPM() {

		// Given
		Integer idAgent = 906543;

		Profil pr = new Profil();
		pr.setSaisie(true);
		pr.setModification(true);
		pr.setSuppression(true);
		pr.setImpression(true);
		pr.setViserVisu(false);
		pr.setViserModif(false);
		pr.setApprouverVisu(false);
		pr.setApprouverModif(false);
		pr.setAnnuler(true);
		pr.setVisuSolde(true);
		pr.setMajSolde(true);
		pr.setDroitAcces(false);

		DroitProfil dpr = new DroitProfil();
		dpr.setProfil(pr);
		
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(9005138);

		DroitDroitsAgent dda = new DroitDroitsAgent();
		dda.setDroitsAgent(droitsAgent);
		dda.setDroitProfil(dpr);

		Droit da = new Droit();
		da.setIdAgent(idAgent);
		da.getDroitProfils().add(dpr);
		da.getDroitDroitsAgent().add(dda);

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentAccessRights(idAgent)).thenReturn(da);

		SirhWsServiceDto serviceDto = new SirhWsServiceDto();
		serviceDto.setSigle("DPM");

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgentDirection(Mockito.anyInt(), Mockito.any(Date.class)))
				.thenReturn(serviceDto);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		// When
		AccessRightsDto result = service.getAgentAccessRights(idAgent);

		// Then
		assertTrue(result.isSaisie());
		assertTrue(result.isModification());
		assertTrue(result.isSuppression());
		assertTrue(result.isImpression());
		assertFalse(result.isViserVisu());
		assertFalse(result.isViserModif());
		assertFalse(result.isApprouverVisu());
		assertFalse(result.isApprouverModif());
		assertTrue(result.isAnnuler());
		assertTrue(result.isVisuSolde());
		assertTrue(result.isMajSolde());
		assertFalse(result.isDroitAcces());
		assertTrue(result.isSaisieRepos());
	}

	@Test
	public void getAgentAccessRights_AgentHasNotRightSaisieRepos_AgentDPM_noDroitMAJCompteur() {

		// Given
		Integer idAgent = 906543;

		Profil pr = new Profil();
		pr.setSaisie(true);
		pr.setModification(true);
		pr.setSuppression(true);
		pr.setImpression(true);
		pr.setViserVisu(false);
		pr.setViserModif(false);
		pr.setApprouverVisu(false);
		pr.setApprouverModif(false);
		pr.setAnnuler(true);
		pr.setVisuSolde(true);
		pr.setMajSolde(false);
		pr.setDroitAcces(false);

		DroitProfil dpr = new DroitProfil();
		dpr.setProfil(pr);

		Droit da = new Droit();
		da.setIdAgent(idAgent);
		da.getDroitProfils().add(dpr);

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentAccessRights(idAgent)).thenReturn(da);

		SirhWsServiceDto serviceDto = new SirhWsServiceDto();
		serviceDto.setSigle("DPM");

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgentDirection(Mockito.anyInt(), Mockito.any(Date.class)))
				.thenReturn(serviceDto);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		// When
		AccessRightsDto result = service.getAgentAccessRights(idAgent);

		// Then
		assertTrue(result.isSaisie());
		assertTrue(result.isModification());
		assertTrue(result.isSuppression());
		assertTrue(result.isImpression());
		assertFalse(result.isViserVisu());
		assertFalse(result.isViserModif());
		assertFalse(result.isApprouverVisu());
		assertFalse(result.isApprouverModif());
		assertTrue(result.isAnnuler());
		assertTrue(result.isVisuSolde());
		assertFalse(result.isMajSolde());
		assertFalse(result.isDroitAcces());
		assertFalse(result.isSaisieRepos());
	}

	@Test
	public void setAgentsApprobateurs_nonExisting() {
		// Given
		AgentWithServiceDto agentDto = new AgentWithServiceDto();
		agentDto.setIdAgent(9005138);

		final Date d = new Date();

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentsApprobateurs()).thenReturn(new ArrayList<Droit>());
		Mockito.when(arRepo.isUserOperateur(9005138)).thenReturn(false);
		Mockito.when(arRepo.isUserViseur(9005138)).thenReturn(false);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				Droit obj = (Droit) args[0];

				assertEquals(9005138, (int) obj.getIdAgent());
				assertEquals(d, obj.getDateModification());

				return true;

			}
		}).when(arRepo).persisEntity(Mockito.any(Droit.class));

		HelperService helpServ = Mockito.mock(HelperService.class);
		Mockito.when(helpServ.getCurrentDate()).thenReturn(d);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "helperService", helpServ);

		// When
		service.setApprobateurs(Arrays.asList(agentDto));

		// Then
		Mockito.verify(arRepo, Mockito.times(1)).persisEntity(Mockito.isA(Droit.class));
	}

	@Test
	public void setAgentsApprobateurs_1inList_nonExisting() {
		// Given
		AgentWithServiceDto agentDto = new AgentWithServiceDto();
		agentDto.setIdAgent(9005138);

		Droit dd = Mockito.spy(new Droit());
		dd.setIdAgent(5);

		final Date d = new Date();

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentsApprobateurs()).thenReturn(Arrays.asList(dd));
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				Droit obj = (Droit) args[0];

				assertEquals(9005138, (int) obj.getIdAgent());
				assertEquals(d, obj.getDateModification());

				return true;

			}
		}).when(arRepo).persisEntity(Mockito.any(Droit.class));
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).removeEntity(Mockito.any(DroitDroitsAgent.class));

		HelperService helpServ = Mockito.mock(HelperService.class);
		Mockito.when(helpServ.getCurrentDate()).thenReturn(d);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "helperService", helpServ);

		// When
		service.setApprobateurs(Arrays.asList(agentDto));

		// Then
		Mockito.verify(arRepo, Mockito.times(1)).persisEntity(Mockito.isA(Droit.class));
	}

	@Test
	public void setAgentsApprobateurs_Existing() {
		// Given
		AgentWithServiceDto agentDto = new AgentWithServiceDto();
		agentDto.setIdAgent(9005138);

		Profil p = new Profil();
		p.setLibelle("APPROBATEUR");

		DroitProfil dp = new DroitProfil();
		dp.setProfil(p);

		Droit d = new Droit();
		d.setIdAgent(9005138);
		d.getDroitProfils().add(dp);

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentsApprobateurs()).thenReturn(Arrays.asList(d));

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// When
		service.setApprobateurs(Arrays.asList(agentDto));

		// Then
		Mockito.verify(arRepo, Mockito.never()).persisEntity(Mockito.isA(Droit.class));
	}

	@Test
	public void setAgentsApprobateurs_OperateurExisting() {
		// Given
		AgentWithServiceDto agentDto = new AgentWithServiceDto();
		agentDto.setIdAgent(9005138);

		Profil p = new Profil();
		p.setLibelle("APPROBATEUR");

		DroitProfil dp = new DroitProfil();
		dp.setProfil(p);

		Droit d = new Droit();
		d.setIdAgent(9005138);
		d.getDroitProfils().add(dp);

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentsApprobateurs()).thenReturn(new ArrayList<Droit>());
		Mockito.when(arRepo.isUserOperateur(9005138)).thenReturn(true);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// When
		service.setApprobateurs(Arrays.asList(agentDto));

		// Then
		Mockito.verify(arRepo, Mockito.never()).persisEntity(Mockito.isA(Droit.class));
	}

	@Test
	public void setAgentsApprobateurs_SuppressionDtoEmpty() {
		// Given

		Profil p = new Profil();
		p.setLibelle("APPROBATEUR");

		Droit d = Mockito.spy(new Droit());
		d.setIdAgent(9005138);

		DroitProfil dp = new DroitProfil();
		dp.setIdDroitProfil(1);
		dp.setDroit(d);
		dp.setProfil(p);

		d.getDroitProfils().add(dp);

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentsApprobateurs()).thenReturn(Arrays.asList(d));
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).removeEntity(Mockito.any(DroitDroitsAgent.class));

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// When
		service.setApprobateurs(new ArrayList<AgentWithServiceDto>());

		// Then
		Mockito.verify(arRepo, Mockito.never()).persisEntity(Mockito.isA(Droit.class));
	}

	@Test
	public void setAgentsApprobateurs_Suppression() {
		// Given
		AgentWithServiceDto agentDto = new AgentWithServiceDto();
		agentDto.setIdAgent(9005131);

		Profil p = new Profil();
		p.setLibelle("APPROBATEUR");

		Droit d = Mockito.spy(new Droit());
		d.setIdAgent(9005138);

		DroitProfil dp = new DroitProfil();
		dp.setIdDroitProfil(1);
		dp.setDroit(d);
		dp.setProfil(p);
		dp.setDroitApprobateur(d);
		d.getDroitProfils().add(dp);

		Date date = new Date();

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentsApprobateurs()).thenReturn(Arrays.asList(d));
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).removeEntity(Mockito.any(DroitDroitsAgent.class));

		HelperService helpServ = Mockito.mock(HelperService.class);
		Mockito.when(helpServ.getCurrentDate()).thenReturn(date);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "helperService", helpServ);

		// When
		service.setApprobateurs(Arrays.asList(agentDto));

		// Then
		Mockito.verify(arRepo, Mockito.times(1)).persisEntity(Mockito.isA(Droit.class));
		// Mockito.verify(d, Mockito.times(1)).remove();
	}

	@Test
	public void getApprobateurs_ReturnEmptyDto() {

		// Given
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentsApprobateurs()).thenReturn(new ArrayList<Droit>());

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// When
		List<AgentWithServiceDto> result = service.getApprobateurs();

		// Then
		assertEquals(0, result.size());
	}

	@Test
	public void getApprobateurs_noEmptyDto() {

		// Given
		List<Droit> listeDroits = new ArrayList<Droit>();
		Droit d1 = new Droit();
		d1.setIdAgent(9005138);
		Droit d2 = new Droit();
		d2.setIdAgent(9003041);
		listeDroits.add(d1);
		listeDroits.add(d2);

		AgentWithServiceDto agDto1 = new AgentWithServiceDto();
		agDto1.setIdAgent(9005138);
		agDto1.setNom("TOTO");
		agDto1.setService("service");
		agDto1.setCodeService("CODE");
		agDto1.setDirection("DIRECTION");
		agDto1.setStatut("F");
		AgentWithServiceDto agDto2 = new AgentWithServiceDto();
		agDto2.setIdAgent(9003041);
		agDto2.setNom("TITO");
		agDto2.setService("service");
		agDto2.setCodeService("CODE2");
		agDto2.setDirection("DIRECTION2");
		agDto2.setStatut("C");

		Date currentDate = new DateTime(2013, 4, 9, 12, 9, 34).toDate();

		HelperService hS = Mockito.mock(HelperService.class);
		Mockito.when(hS.getCurrentDate()).thenReturn(currentDate);

		ISirhWSConsumer wsMock = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(wsMock.getAgentService(9005138, currentDate)).thenReturn(agDto1);
		Mockito.when(wsMock.getAgentService(9003041, currentDate)).thenReturn(agDto2);

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentsApprobateurs()).thenReturn(listeDroits);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);
		ReflectionTestUtils.setField(service, "helperService", hS);

		// When
		List<AgentWithServiceDto> dto = service.getApprobateurs();

		// Then
		assertEquals(2, dto.size());
		assertEquals("CODE2", dto.get(0).getCodeService());
		assertEquals("DIRECTION2", dto.get(0).getDirection());
		assertEquals("CODE", dto.get(1).getCodeService());
		assertEquals("F", dto.get(1).getStatut());
	}

	@Test
	public void getInputter_ReturnEmptyDto() {

		// Given
		Integer idAgent = 9005138;

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getDroitSousApprobateur(idAgent)).thenReturn(new ArrayList<Droit>());

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// When
		InputterDto result = service.getInputter(idAgent);

		// Then
		assertNull(result.getDelegataire());
		assertEquals(0, result.getOperateurs().size());
	}

	@Test
	public void getInputter_Return1Delegataire() {

		// Given
		Integer idAgentAppro = 9005138;
		Droit droitAppro = new Droit();
		droitAppro.setIdAgent(idAgentAppro);
		Integer idAgentDelegataire = 9005131;
		Droit droitDelegataire = new Droit();
		DroitProfil dp = new DroitProfil();
		Profil p = new Profil();
		p.setLibelle("DELEGATAIRE");
		dp.setProfil(p);
		dp.setDroitApprobateur(droitAppro);
		dp.setDroit(droitDelegataire);
		droitDelegataire.setIdAgent(idAgentDelegataire);
		droitDelegataire.getDroitProfils().add(dp);

		AgentGeneriqueDto agentDelegataire = new AgentGeneriqueDto();
		agentDelegataire.setIdAgent(idAgentDelegataire);
		agentDelegataire.setPrenomUsage("TEST");

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getDroitSousApprobateur(idAgentAppro)).thenReturn(Arrays.asList(droitDelegataire));
		Mockito.when(arRepo.isUserDelegataireOfApprobateur(idAgentAppro, idAgentDelegataire)).thenReturn(true);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(idAgentDelegataire)).thenReturn(agentDelegataire);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		// When
		InputterDto result = service.getInputter(idAgentAppro);

		// Then
		assertEquals(idAgentDelegataire, result.getDelegataire().getIdAgent());
		assertEquals("TEST", result.getDelegataire().getPrenom());
		assertEquals(0, result.getOperateurs().size());
	}

	@Test
	public void getInputter_Return1Operateur() {

		// Given
		Integer idAgentAppro = 9005138;
		Droit droitAppro = new Droit();
		droitAppro.setIdAgent(idAgentAppro);
		Integer idAgentOperateur = 9005131;
		Droit droitOperateur = new Droit();
		DroitProfil dp = new DroitProfil();
		Profil p = new Profil();
		p.setLibelle("OPERATEUR");
		dp.setProfil(p);
		dp.setDroitApprobateur(droitAppro);
		dp.setDroit(droitOperateur);
		droitOperateur.setIdAgent(idAgentOperateur);
		droitOperateur.getDroitProfils().add(dp);

		AgentGeneriqueDto agentOperateur = new AgentGeneriqueDto();
		agentOperateur.setIdAgent(idAgentOperateur);
		agentOperateur.setPrenomUsage("TEST");
		agentOperateur.setNomatr(5138);

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getDroitSousApprobateur(idAgentAppro)).thenReturn(Arrays.asList(droitOperateur));
		Mockito.when(arRepo.isUserOperateurOfApprobateur(idAgentAppro, idAgentOperateur)).thenReturn(true);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(idAgentOperateur)).thenReturn(agentOperateur);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		// When
		InputterDto result = service.getInputter(idAgentAppro);

		// Then
		assertNull(result.getDelegataire());
		assertEquals(1, result.getOperateurs().size());
		assertEquals(9005131, (int) result.getOperateurs().get(0).getIdAgent());
	}

	@Test
	public void getAgentsToApprove_NoAgents_ReturnEmptyList() {

		// Given
		Integer idAgent = 9007654;
		DroitProfil dp = new DroitProfil();
		dp.setIdDroitProfil(1);

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getListOfAgentsToInputOrApprove(idAgent, null, idAgent)).thenReturn(
				new ArrayList<DroitsAgent>());
		Mockito.when(arRepo.getDroitProfilByAgent(idAgent, idAgent)).thenReturn(dp);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// When
		List<AgentDto> result = service.getAgentsToApproveOrInput(idAgent, idAgent);

		// Then
		assertEquals(0, result.size());
	}

	@Test
	public void getAgentsToApprove_2Agents_ReturnListOf2() {

		// Given
		Integer idAgent = 9007654;

		AgentGeneriqueDto a1 = new AgentGeneriqueDto();
		a1.setIdAgent(1);
		a1.setNomUsage("TEST 1");
		AgentGeneriqueDto a2 = new AgentGeneriqueDto();
		a2.setIdAgent(2);
		a2.setNomUsage("TEST 2");

		DroitProfil dp = new DroitProfil();
		dp.setIdDroitProfil(1);

		DroitsAgent da1 = new DroitsAgent();
		da1.setIdAgent(1);
		DroitsAgent da2 = new DroitsAgent();
		da2.setIdAgent(2);

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getDroitProfilByAgent(idAgent, idAgent)).thenReturn(dp);
		Mockito.when(arRepo.getListOfAgentsToInputOrApprove(idAgent, null, dp.getIdDroitProfil())).thenReturn(
				Arrays.asList(da1, da2));

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(1)).thenReturn(a1);
		Mockito.when(sirhWSConsumer.getAgent(2)).thenReturn(a2);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		// When
		List<AgentDto> result = service.getAgentsToApproveOrInput(idAgent, idAgent);

		// Then
		assertEquals(2, result.size());
		assertEquals(1, result.get(0).getIdAgent().intValue());
		assertEquals("TEST 1", result.get(0).getNom());
		assertEquals(2, result.get(1).getIdAgent().intValue());
		assertEquals("TEST 2", result.get(1).getNom());
	}

	@Test
	public void setInputter_delegataireOperateurNoExisting() {
		// Given
		Integer idAgent = 9005138;

		InputterDto dto = new InputterDto();

		AgentDto delegataire = new AgentDto();
		delegataire.setIdAgent(9001234);
		dto.setDelegataire(delegataire);

		AgentDto operateur = new AgentDto();
		operateur.setIdAgent(9001234);
		dto.setOperateurs(Arrays.asList(operateur));

		Droit dd = Mockito.spy(new Droit());

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentAccessRights(idAgent)).thenReturn(dd);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).removeEntity(Mockito.any(DroitDroitsAgent.class));

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(delegataire.getIdAgent())).thenReturn(null);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		// When
		ReturnMessageDto msgDto = service.setInputter(9005138, dto);

		// Then
		assertEquals(2, msgDto.getErrors().size());
		assertTrue(msgDto.getErrors().get(0).contains("n'existe pas"));
		assertTrue(msgDto.getErrors().get(1).contains("n'existe pas"));
	}

	@Test
	public void setInputter_delegataireOperateurCanNotBe() {
		// Given
		Integer idAgent = 9005138;

		InputterDto dto = new InputterDto();
		AgentDto delegataire = new AgentDto();
		delegataire.setIdAgent(9001234);
		dto.setDelegataire(delegataire);

		AgentDto operateur = new AgentDto();
		operateur.setIdAgent(9001235);
		AgentDto operateur2 = new AgentDto();
		operateur2.setIdAgent(9001236);
		AgentDto operateur3 = new AgentDto();
		operateur3.setIdAgent(9001237);
		dto.setOperateurs(Arrays.asList(operateur, operateur2, operateur3));

		Droit dd = Mockito.spy(new Droit());

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentAccessRights(idAgent)).thenReturn(dd);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).removeEntity(Mockito.any(DroitDroitsAgent.class));

		Mockito.when(arRepo.isUserOperateur(delegataire.getIdAgent())).thenReturn(true);

		Mockito.when(arRepo.isUserApprobateur(operateur.getIdAgent())).thenReturn(true);
		Mockito.when(arRepo.isUserViseur(operateur2.getIdAgent())).thenReturn(true);
		Mockito.when(arRepo.isUserDelegataire(operateur3.getIdAgent())).thenReturn(true);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(delegataire.getIdAgent())).thenReturn(new AgentGeneriqueDto());
		Mockito.when(sirhWSConsumer.getAgent(operateur.getIdAgent())).thenReturn(new AgentGeneriqueDto());
		Mockito.when(sirhWSConsumer.getAgent(operateur2.getIdAgent())).thenReturn(new AgentGeneriqueDto());
		Mockito.when(sirhWSConsumer.getAgent(operateur3.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		// When
		ReturnMessageDto msgDto = service.setInputter(9005138, dto);

		// Then
		assertEquals(4, msgDto.getErrors().size());
		assertTrue(msgDto.getErrors().get(0)
				.contains("ne peut pas être délégataire car il ou elle est déjà opérateur."));
		assertTrue(msgDto.getErrors().get(1)
				.contains("ne peut pas être opérateur car il ou elle est déjà approbateur."));
		assertTrue(msgDto.getErrors().get(2).contains("ne peut pas être opérateur car il ou elle est déjà viseur."));
		assertTrue(msgDto.getErrors().get(3)
				.contains("ne peut pas être opérateur car il ou elle est déjà délégataire."));
	}

	@Test
	public void setInputter_delegataireOperateurPersist() {
		// Given
		Integer idAgent = 9005138;

		InputterDto dto = new InputterDto();
		AgentDto delegataire = new AgentDto();
		delegataire.setIdAgent(9001234);
		dto.setDelegataire(delegataire);

		AgentDto operateur = new AgentDto();
		operateur.setIdAgent(9001235);
		AgentDto operateur2 = new AgentDto();
		operateur2.setIdAgent(9001236);
		AgentDto operateur3 = new AgentDto();
		operateur3.setIdAgent(9001237);
		dto.setOperateurs(Arrays.asList(operateur, operateur2, operateur3));

		Droit dd = Mockito.spy(new Droit());

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentAccessRights(idAgent)).thenReturn(dd);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).removeEntity(Mockito.any(DroitDroitsAgent.class));

		Mockito.when(arRepo.isUserOperateur(delegataire.getIdAgent())).thenReturn(false);

		Mockito.when(arRepo.isUserApprobateur(operateur.getIdAgent())).thenReturn(false);
		Mockito.when(arRepo.isUserViseur(operateur2.getIdAgent())).thenReturn(false);
		Mockito.when(arRepo.isUserDelegataire(operateur3.getIdAgent())).thenReturn(false);

		Mockito.when(arRepo.getAgentAccessRights(delegataire.getIdAgent())).thenReturn(new Droit());
		Mockito.when(arRepo.getAgentAccessRights(operateur.getIdAgent())).thenReturn(new Droit());
		Mockito.when(arRepo.getAgentAccessRights(operateur2.getIdAgent())).thenReturn(new Droit());
		Mockito.when(arRepo.getAgentAccessRights(operateur3.getIdAgent())).thenReturn(new Droit());

		Mockito.when(arRepo.getProfilByName(ProfilEnum.DELEGATAIRE.toString())).thenReturn(new Profil());
		Mockito.when(arRepo.getProfilByName(ProfilEnum.OPERATEUR.toString())).thenReturn(new Profil());
		Mockito.when(arRepo.getProfilByName(ProfilEnum.VISEUR.toString())).thenReturn(new Profil());

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(delegataire.getIdAgent())).thenReturn(new AgentGeneriqueDto());
		Mockito.when(sirhWSConsumer.getAgent(operateur.getIdAgent())).thenReturn(new AgentGeneriqueDto());
		Mockito.when(sirhWSConsumer.getAgent(operateur2.getIdAgent())).thenReturn(new AgentGeneriqueDto());
		Mockito.when(sirhWSConsumer.getAgent(operateur3.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		final Date d = new Date();

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).persisEntity(Mockito.any(Droit.class));

		HelperService helpServ = Mockito.mock(HelperService.class);
		Mockito.when(helpServ.getCurrentDate()).thenReturn(d);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", helpServ);

		// When
		ReturnMessageDto msgDto = service.setInputter(9005138, dto);

		// Then
		Mockito.verify(arRepo, Mockito.times(4)).persisEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(0)).removeEntity(Mockito.isA(Droit.class));
		assertEquals(0, msgDto.getErrors().size());
	}

	@Test
	public void setInputter_delegataireOperateurPersist_AvecDelegataireOperateurDejaExistants() {
		// Given
		Integer idAgent = 9005138;

		// /////////////////// OPERATEUR DELEGATAIRE A CREER
		// /////////////////
		InputterDto dto = new InputterDto();
		// 1 delegataire
		AgentDto delegataire = new AgentDto();
		delegataire.setIdAgent(9001234);
		dto.setDelegataire(delegataire);
		// 3 operateurs dont un deja existant
		AgentDto operateur = new AgentDto();
		operateur.setIdAgent(9001235);
		AgentDto operateur2 = new AgentDto();
		operateur2.setIdAgent(9001236);
		AgentDto operateur3 = new AgentDto();
		operateur3.setIdAgent(9001237);
		dto.setOperateurs(Arrays.asList(operateur, operateur2, operateur3));
		// //////////////////////////////////////////////////////////////

		// /////////////// OPERATEUR DELEGATAIRE DEJA EXISTANT
		// /////////////////////
		List<Droit> listDroitSousAgentsByApprobateur = new ArrayList<Droit>();
		// approbateur
		Droit droitApprobateur = new Droit();
		droitApprobateur.setIdAgent(idAgent);
		droitApprobateur.setIdDroit(13);

		// operateur deja existant
		DroitProfil droitProfilOperateurExistant = new DroitProfil();
		droitProfilOperateurExistant.setDroitApprobateur(droitApprobateur);
		Set<DroitProfil> droitProfilOperateurExistants = new HashSet<DroitProfil>();
		droitProfilOperateurExistants.add(droitProfilOperateurExistant);

		Droit droitOperateurExistant = new Droit();
		droitOperateurExistant.setIdAgent(9001235);
		droitProfilOperateurExistant.setDroit(droitOperateurExistant);
		droitOperateurExistant.setDroitProfils(droitProfilOperateurExistants);

		// delegataire deja existant
		DroitProfil droitProfilDelegataireExistant = new DroitProfil();
		droitProfilDelegataireExistant.setDroitApprobateur(droitApprobateur);
		Set<DroitProfil> droitProfilDelegataireExistants = new HashSet<DroitProfil>();
		droitProfilDelegataireExistants.add(droitProfilDelegataireExistant);

		Droit droitDelegataireExistant = new Droit();
		droitDelegataireExistant.setIdAgent(9001234);
		droitProfilDelegataireExistant.setDroit(droitDelegataireExistant);
		droitDelegataireExistant.setDroitProfils(droitProfilDelegataireExistants);

		listDroitSousAgentsByApprobateur.add(droitOperateurExistant);
		listDroitSousAgentsByApprobateur.add(droitDelegataireExistant);
		// ////////////////////////////////////////////////////

		// ////////////////// Mockito ////////////////////
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentAccessRights(idAgent)).thenReturn(droitApprobateur);
		Mockito.when(arRepo.getDroitSousApprobateur(idAgent)).thenReturn(listDroitSousAgentsByApprobateur);

		Mockito.when(arRepo.isUserOperateurOfApprobateur(idAgent, droitOperateurExistant.getIdAgent()))
				.thenReturn(true);
		Mockito.when(arRepo.isUserDelegataireOfApprobateur(idAgent, droitDelegataireExistant.getIdAgent())).thenReturn(
				true);

		Mockito.when(arRepo.isUserOperateur(delegataire.getIdAgent())).thenReturn(false);

		Mockito.when(arRepo.isUserApprobateur(operateur.getIdAgent())).thenReturn(false);
		Mockito.when(arRepo.isUserViseur(operateur2.getIdAgent())).thenReturn(false);
		Mockito.when(arRepo.isUserDelegataire(operateur3.getIdAgent())).thenReturn(false);

		Mockito.when(arRepo.getAgentAccessRights(delegataire.getIdAgent())).thenReturn(new Droit());
		Mockito.when(arRepo.getAgentAccessRights(operateur.getIdAgent())).thenReturn(new Droit());
		Mockito.when(arRepo.getAgentAccessRights(operateur2.getIdAgent())).thenReturn(new Droit());
		Mockito.when(arRepo.getAgentAccessRights(operateur3.getIdAgent())).thenReturn(new Droit());

		Mockito.when(arRepo.getProfilByName(ProfilEnum.DELEGATAIRE.toString())).thenReturn(new Profil());
		Mockito.when(arRepo.getProfilByName(ProfilEnum.OPERATEUR.toString())).thenReturn(new Profil());
		Mockito.when(arRepo.getProfilByName(ProfilEnum.VISEUR.toString())).thenReturn(new Profil());

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(delegataire.getIdAgent())).thenReturn(new AgentGeneriqueDto());
		Mockito.when(sirhWSConsumer.getAgent(operateur.getIdAgent())).thenReturn(new AgentGeneriqueDto());
		Mockito.when(sirhWSConsumer.getAgent(operateur2.getIdAgent())).thenReturn(new AgentGeneriqueDto());
		Mockito.when(sirhWSConsumer.getAgent(operateur3.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		final Date d = new Date();

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).persisEntity(Mockito.any(Droit.class));

		HelperService helpServ = Mockito.mock(HelperService.class);
		Mockito.when(helpServ.getCurrentDate()).thenReturn(d);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", helpServ);

		// ///////////////////////// TESTS ////////////////////////
		// When
		ReturnMessageDto msgDto = service.setInputter(9005138, dto);

		// Then
		Mockito.verify(arRepo, Mockito.times(2)).persisEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(0)).removeEntity(Mockito.isA(Droit.class));
		assertEquals(0, msgDto.getErrors().size());
	}

	@Test
	public void setInputter_delegataireOperateurRemove() {
		// Given
		Integer idAgent = 9005138;

		// approbateur
		Droit droitApprobateur = new Droit();
		droitApprobateur.setIdAgent(idAgent);
		droitApprobateur.setIdDroit(13);

		InputterDto dto = new InputterDto();
		dto.setDelegataire(null);
		dto.setOperateurs(new ArrayList<AgentDto>());

		List<Droit> listDroitSousAgentsByApprobateur = new ArrayList<Droit>();

		// operateur existant a supprimer
		DroitProfil droitProfilOperateurExistant = new DroitProfil();
		droitProfilOperateurExistant.setDroitApprobateur(droitApprobateur);
		Set<DroitProfil> droitProfilOperateurExistants = new HashSet<DroitProfil>();
		droitProfilOperateurExistants.add(droitProfilOperateurExistant);

		Droit droitOperateurExistant = new Droit();
		droitOperateurExistant.setIdAgent(1);
		droitProfilOperateurExistant.setDroit(droitOperateurExistant);
		droitOperateurExistant.setDroitProfils(droitProfilOperateurExistants);

		// delegataire a supprimer
		DroitProfil droitProfilDelegataireExistant = new DroitProfil();
		droitProfilDelegataireExistant.setDroitApprobateur(droitApprobateur);
		Set<DroitProfil> droitProfilDelegataireExistants = new HashSet<DroitProfil>();
		droitProfilDelegataireExistants.add(droitProfilDelegataireExistant);

		Droit droitDelegataireExistant = new Droit();
		droitDelegataireExistant.setIdAgent(3);
		droitProfilDelegataireExistant.setDroit(droitDelegataireExistant);
		droitDelegataireExistant.setDroitProfils(droitProfilDelegataireExistants);

		listDroitSousAgentsByApprobateur.add(droitOperateurExistant);
		listDroitSousAgentsByApprobateur.add(droitDelegataireExistant);

		// mock
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentAccessRights(idAgent)).thenReturn(droitApprobateur);
		Mockito.when(arRepo.getDroitSousApprobateur(idAgent)).thenReturn(listDroitSousAgentsByApprobateur);
		Mockito.when(arRepo.isUserOperateurOfApprobateur(idAgent, droitOperateurExistant.getIdAgent()))
				.thenReturn(true);
		Mockito.when(arRepo.isUserDelegataireOfApprobateur(idAgent, droitDelegataireExistant.getIdAgent())).thenReturn(
				true);

		Mockito.when(arRepo.getProfilByName(ProfilEnum.DELEGATAIRE.toString())).thenReturn(new Profil());
		Mockito.when(arRepo.getProfilByName(ProfilEnum.OPERATEUR.toString())).thenReturn(new Profil());
		Mockito.when(arRepo.getProfilByName(ProfilEnum.VISEUR.toString())).thenReturn(new Profil());

		ISirhRepository sirhRepo = Mockito.mock(ISirhRepository.class);

		final Date d = new Date();

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).persisEntity(Mockito.any(Droit.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).removeEntity(Mockito.any(Droit.class));

		HelperService helpServ = Mockito.mock(HelperService.class);
		Mockito.when(helpServ.getCurrentDate()).thenReturn(d);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepo);
		ReflectionTestUtils.setField(service, "helperService", helpServ);

		// When
		ReturnMessageDto msgDto = service.setInputter(9005138, dto);

		// Then
		Mockito.verify(arRepo, Mockito.times(0)).persisEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(2)).removeEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(2)).removeEntity(Mockito.isA(DroitProfil.class));
		assertEquals(0, msgDto.getErrors().size());
	}

	@Test
	public void setInputter_delegataireOperateurAnd_PersistAndRemove_AndAgentDejaExistant() {
		// Given
		Integer idAgent = 9005138;

		// ///////////// CREATION //////////////////
		// approbateur
		Droit droitApprobateur = new Droit();
		droitApprobateur.setIdAgent(idAgent);
		droitApprobateur.setIdDroit(13);

		// delegataire a creer
		InputterDto dto = new InputterDto();
		AgentDto delegataire = new AgentDto();
		delegataire.setIdAgent(9001234);
		dto.setDelegataire(delegataire);
		// operateur a creer
		AgentDto operateur = new AgentDto();
		operateur.setIdAgent(9001235);
		AgentDto operateur2 = new AgentDto();
		operateur2.setIdAgent(9001236);
		AgentDto operateur3 = new AgentDto();
		operateur3.setIdAgent(9001237);
		dto.setOperateurs(Arrays.asList(operateur, operateur2, operateur3));
		// ////////////////////////////////////////////////////

		// /////////////// OPERATEUR DELEGATAIRE DEJA EXISTANTS
		// /////////////////////
		List<Droit> listDroitSousAgentsByApprobateur = new ArrayList<Droit>();

		// operateur deja existant
		DroitProfil droitProfilOperateurExistant = new DroitProfil();
		droitProfilOperateurExistant.setDroitApprobateur(droitApprobateur);
		Set<DroitProfil> droitProfilOperateurExistants = new HashSet<DroitProfil>();
		droitProfilOperateurExistants.add(droitProfilOperateurExistant);

		Droit droitOperateurExistant = new Droit();
		droitOperateurExistant.setIdAgent(9001235);
		droitProfilOperateurExistant.setDroit(droitOperateurExistant);
		droitOperateurExistant.setDroitProfils(droitProfilOperateurExistants);

		// delegataire deja existant
		DroitProfil droitProfilDelegataireExistant = new DroitProfil();
		droitProfilDelegataireExistant.setDroitApprobateur(droitApprobateur);
		Set<DroitProfil> droitProfilDelegataireExistants = new HashSet<DroitProfil>();
		droitProfilDelegataireExistants.add(droitProfilDelegataireExistant);

		Droit droitDelegataireExistant = new Droit();
		droitDelegataireExistant.setIdAgent(9001234);
		droitProfilDelegataireExistant.setDroit(droitDelegataireExistant);
		droitDelegataireExistant.setDroitProfils(droitProfilDelegataireExistants);

		listDroitSousAgentsByApprobateur.add(droitOperateurExistant);
		listDroitSousAgentsByApprobateur.add(droitDelegataireExistant);
		// ////////////////////////////////////////////////////

		// ///////////// SUPPRESSION //////////////////
		// operateur existant a supprimer
		DroitProfil droitProfilOperateurASupprimer = new DroitProfil();
		droitProfilOperateurASupprimer.setDroitApprobateur(droitApprobateur);
		Set<DroitProfil> droitProfilOperateursASupprimer = new HashSet<DroitProfil>();
		droitProfilOperateursASupprimer.add(droitProfilOperateurASupprimer);

		Droit droitOperateurASupprimer = new Droit();
		droitOperateurASupprimer.setIdAgent(1);
		droitProfilOperateurASupprimer.setDroit(droitOperateurASupprimer);
		droitOperateurASupprimer.setDroitProfils(droitProfilOperateursASupprimer);

		listDroitSousAgentsByApprobateur.add(droitOperateurASupprimer);

		// ////////////// mock //////////////////////
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentAccessRights(idAgent)).thenReturn(droitApprobateur);
		Mockito.when(arRepo.getDroitSousApprobateur(idAgent)).thenReturn(listDroitSousAgentsByApprobateur);

		Mockito.when(arRepo.isUserOperateurOfApprobateur(idAgent, droitOperateurExistant.getIdAgent()))
				.thenReturn(true);
		Mockito.when(arRepo.isUserDelegataireOfApprobateur(idAgent, droitDelegataireExistant.getIdAgent())).thenReturn(
				true);

		Mockito.when(arRepo.isUserOperateurOfApprobateur(idAgent, droitOperateurASupprimer.getIdAgent())).thenReturn(
				true);

		Mockito.when(arRepo.isUserOperateur(delegataire.getIdAgent())).thenReturn(false);

		Mockito.when(arRepo.isUserViseur(operateur2.getIdAgent())).thenReturn(false);
		Mockito.when(arRepo.isUserDelegataire(operateur3.getIdAgent())).thenReturn(false);

		Mockito.when(arRepo.getAgentAccessRights(operateur2.getIdAgent())).thenReturn(new Droit());
		Mockito.when(arRepo.getAgentAccessRights(operateur3.getIdAgent())).thenReturn(new Droit());

		Mockito.when(arRepo.getProfilByName(ProfilEnum.DELEGATAIRE.toString())).thenReturn(new Profil());
		Mockito.when(arRepo.getProfilByName(ProfilEnum.OPERATEUR.toString())).thenReturn(new Profil());
		Mockito.when(arRepo.getProfilByName(ProfilEnum.VISEUR.toString())).thenReturn(new Profil());

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(operateur2.getIdAgent())).thenReturn(new AgentGeneriqueDto());
		Mockito.when(sirhWSConsumer.getAgent(operateur3.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		final Date d = new Date();

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).persisEntity(Mockito.any(Droit.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).removeEntity(Mockito.any(Droit.class));

		HelperService helpServ = Mockito.mock(HelperService.class);
		Mockito.when(helpServ.getCurrentDate()).thenReturn(d);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", helpServ);

		// /////////// When //////////////
		ReturnMessageDto msgDto = service.setInputter(9005138, dto);

		// ///////////// Then //////////////
		assertEquals(0, msgDto.getErrors().size());
		Mockito.verify(arRepo, Mockito.times(2)).persisEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(1)).removeEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(1)).removeEntity(Mockito.isA(DroitProfil.class));
	}

	@Test
	public void setInputter_delegataireDejaExistant() {
		// Given
		Integer idAgent = 9005138;

		// ///////////// CREATION //////////////////
		// approbateur
		Droit droitApprobateur = new Droit();
		droitApprobateur.setIdAgent(idAgent);
		droitApprobateur.setIdDroit(13);

		// delegataire a creer
		InputterDto dto = new InputterDto();
		AgentDto delegataire = new AgentDto();
		delegataire.setIdAgent(9001234);
		dto.setDelegataire(delegataire);
		// ////////////////////////////////////////////////////

		// /////////////// OPERATEUR VISEUR DELEGATAIRE DEJA EXISTANTS
		// /////////////////////
		List<Droit> listDroitSousAgentsByApprobateur = new ArrayList<Droit>();

		// delegataire deja existant
		DroitProfil droitProfilDelegataireExistant = new DroitProfil();
		droitProfilDelegataireExistant.setDroitApprobateur(droitApprobateur);
		Set<DroitProfil> droitProfilDelegataireExistants = new HashSet<DroitProfil>();
		droitProfilDelegataireExistants.add(droitProfilDelegataireExistant);

		Droit droitDelegataireExistant = new Droit();
		droitDelegataireExistant.setIdAgent(9001234);
		droitProfilDelegataireExistant.setDroit(droitDelegataireExistant);
		droitDelegataireExistant.setDroitProfils(droitProfilDelegataireExistants);

		listDroitSousAgentsByApprobateur.add(droitDelegataireExistant);
		// ////////////////////////////////////////////////////

		// ////////////// mock //////////////////////
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentAccessRights(idAgent)).thenReturn(droitApprobateur);
		Mockito.when(arRepo.getDroitSousApprobateur(idAgent)).thenReturn(listDroitSousAgentsByApprobateur);
		Mockito.when(arRepo.isUserDelegataireOfApprobateur(idAgent, droitDelegataireExistant.getIdAgent())).thenReturn(
				true);

		Mockito.when(arRepo.isUserOperateur(delegataire.getIdAgent())).thenReturn(false);

		Mockito.when(arRepo.getProfilByName(ProfilEnum.DELEGATAIRE.toString())).thenReturn(new Profil());

		ISirhRepository sirhRepo = Mockito.mock(ISirhRepository.class);

		final Date d = new Date();

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).persisEntity(Mockito.any(Droit.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).removeEntity(Mockito.any(Droit.class));

		HelperService helpServ = Mockito.mock(HelperService.class);
		Mockito.when(helpServ.getCurrentDate()).thenReturn(d);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepo);
		ReflectionTestUtils.setField(service, "helperService", helpServ);

		// /////////// When //////////////
		ReturnMessageDto msgDto = service.setInputter(9005138, dto);

		// ///////////// Then //////////////
		assertEquals(0, msgDto.getErrors().size());
		Mockito.verify(arRepo, Mockito.times(0)).persisEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(0)).removeEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(0)).removeEntity(Mockito.isA(DroitProfil.class));
	}

	@Test
	public void setInputter_delegataireDejaExistantASupprimer_And_delegataireACreer() {
		// Given
		Integer idAgent = 9005138;

		// ///////////// CREATION //////////////////
		// approbateur
		Droit droitApprobateur = new Droit();
		droitApprobateur.setIdAgent(idAgent);
		droitApprobateur.setIdDroit(13);

		// delegataire a creer
		InputterDto dto = new InputterDto();
		AgentDto delegataire = new AgentDto();
		delegataire.setIdAgent(9001234);
		dto.setDelegataire(delegataire);
		// ////////////////////////////////////////////////////

		// /////////////// OPERATEUR VISEUR DELEGATAIRE DEJA EXISTANTS
		// /////////////////////
		List<Droit> listDroitSousAgentsByApprobateur = new ArrayList<Droit>();

		// delegataire deja existant
		DroitProfil droitProfilDelegataireExistant = new DroitProfil();
		droitProfilDelegataireExistant.setDroitApprobateur(droitApprobateur);
		Set<DroitProfil> droitProfilDelegataireExistants = new HashSet<DroitProfil>();
		droitProfilDelegataireExistants.add(droitProfilDelegataireExistant);

		Droit droitDelegataireExistant = new Droit();
		droitDelegataireExistant.setIdAgent(9001235);
		droitProfilDelegataireExistant.setDroit(droitDelegataireExistant);
		droitDelegataireExistant.setDroitProfils(droitProfilDelegataireExistants);

		listDroitSousAgentsByApprobateur.add(droitDelegataireExistant);
		// ////////////////////////////////////////////////////

		// ////////////// mock //////////////////////
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentAccessRights(idAgent)).thenReturn(droitApprobateur);
		Mockito.when(arRepo.getDroitSousApprobateur(idAgent)).thenReturn(listDroitSousAgentsByApprobateur);
		Mockito.when(arRepo.isUserDelegataireOfApprobateur(idAgent, droitDelegataireExistant.getIdAgent())).thenReturn(
				true);

		Mockito.when(arRepo.isUserOperateur(delegataire.getIdAgent())).thenReturn(false);

		Mockito.when(arRepo.getProfilByName(ProfilEnum.DELEGATAIRE.toString())).thenReturn(new Profil());

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(delegataire.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		final Date d = new Date();

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).persisEntity(Mockito.any(Droit.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).removeEntity(Mockito.any(Droit.class));

		HelperService helpServ = Mockito.mock(HelperService.class);
		Mockito.when(helpServ.getCurrentDate()).thenReturn(d);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", helpServ);

		// /////////// When //////////////
		ReturnMessageDto msgDto = service.setInputter(9005138, dto);

		// ///////////// Then //////////////
		assertEquals(0, msgDto.getErrors().size());
		Mockito.verify(arRepo, Mockito.times(1)).persisEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(1)).removeEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(1)).removeEntity(Mockito.isA(DroitProfil.class));
	}

	@Test
	public void setInputter_OperateurDeDeuxApprobateursRemove() {
		// Given
		Integer idAgent = 9005138;
		Integer idAgent2 = 9005139;

		// approbateur
		Droit droitApprobateur = new Droit();
		droitApprobateur.setIdAgent(idAgent);
		droitApprobateur.setIdDroit(13);

		Droit droitApprobateur2 = new Droit();
		droitApprobateur2.setIdAgent(idAgent2);
		droitApprobateur2.setIdDroit(14);

		InputterDto dto = new InputterDto();
		dto.setDelegataire(null);
		dto.setOperateurs(new ArrayList<AgentDto>());

		List<Droit> listDroitSousAgentsByApprobateur = new ArrayList<Droit>();

		// operateur existant a supprimer avec 2 DroitProfil de 2 approbateurs
		DroitProfil droitProfiltOperateurExistant = new DroitProfil();
		droitProfiltOperateurExistant.setDroitApprobateur(droitApprobateur);

		DroitProfil droitProfiltOperateurExistant2 = new DroitProfil();
		droitProfiltOperateurExistant2.setDroitApprobateur(droitApprobateur2);

		Set<DroitProfil> droitProfiltOperateurExistants = new HashSet<DroitProfil>();
		droitProfiltOperateurExistants.add(droitProfiltOperateurExistant);
		droitProfiltOperateurExistants.add(droitProfiltOperateurExistant2);

		Droit droitOperateurExistant = new Droit();
		droitOperateurExistant.setIdAgent(1);
		droitProfiltOperateurExistant.setDroit(droitOperateurExistant);
		droitProfiltOperateurExistant2.setDroit(droitOperateurExistant);
		droitOperateurExistant.setDroitProfils(droitProfiltOperateurExistants);

		listDroitSousAgentsByApprobateur.add(droitOperateurExistant);

		// mock
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentAccessRights(idAgent)).thenReturn(droitApprobateur);
		Mockito.when(arRepo.getDroitSousApprobateur(idAgent)).thenReturn(listDroitSousAgentsByApprobateur);
		Mockito.when(arRepo.isUserOperateurOfApprobateur(idAgent, droitOperateurExistant.getIdAgent()))
				.thenReturn(true);

		Mockito.when(arRepo.getProfilByName(ProfilEnum.OPERATEUR.toString())).thenReturn(new Profil());

		ISirhRepository sirhRepo = Mockito.mock(ISirhRepository.class);

		final Date d = new Date();

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).persisEntity(Mockito.any(Droit.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).removeEntity(Mockito.any(Droit.class));

		HelperService helpServ = Mockito.mock(HelperService.class);
		Mockito.when(helpServ.getCurrentDate()).thenReturn(d);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepo);
		ReflectionTestUtils.setField(service, "helperService", helpServ);

		// When
		ReturnMessageDto msgDto = service.setInputter(9005138, dto);

		// Then
		assertEquals(0, msgDto.getErrors().size());
		Mockito.verify(arRepo, Mockito.times(0)).persisEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(0)).removeEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(1)).removeEntity(Mockito.isA(DroitProfil.class));

	}

	@Test
	public void setAgentsToInput_NoOperatorOfApprobator() {

		Integer idAgentApprobateur = 9005138;
		Integer idAgentOperateurOrViseur = 9005139;
		List<AgentDto> agents = new ArrayList<AgentDto>();

		Droit droitApprobateur = new Droit();
		Droit droitOperateurOrViseur = new Droit();
		DroitProfil droitProfilOperateurOrViseur = new DroitProfil();

		List<Droit> droitSousAgentsByApprobateur = new ArrayList<Droit>();

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentDroitFetchAgents(idAgentApprobateur)).thenReturn(droitApprobateur);
		Mockito.when(arRepo.getAgentDroitFetchAgents(idAgentOperateurOrViseur)).thenReturn(droitOperateurOrViseur);
		Mockito.when(arRepo.getDroitSousApprobateur(idAgentApprobateur)).thenReturn(droitSousAgentsByApprobateur);
		Mockito.when(arRepo.getDroitProfilByAgent(idAgentApprobateur, idAgentOperateurOrViseur)).thenReturn(droitProfilOperateurOrViseur);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// /////////// WHEN /////////////
		ReturnMessageDto msgDto = service.setAgentsToInput(idAgentApprobateur, idAgentOperateurOrViseur, agents);

		// Then
		assertEquals(1, msgDto.getErrors().size());
		assertEquals(
				"Impossible de modifier la liste des agents saisis de l'opérateur ou du viseur [9005139] car il n'est pas un opérateur ou viseur de l'agent [9005138].",
				msgDto.getErrors().get(0));
	}

	@Test
	public void setAgentsToInput_NoOperateurNoViseur() {

		Integer idAgentApprobateur = 9005138;
		Integer idAgentOperateurOrViseur = 9005139;
		List<AgentDto> agents = new ArrayList<AgentDto>();

		Droit droitApprobateur = new Droit();
		Droit droitOperateurOrViseur = new Droit();
		DroitProfil droitProfilOperateurOrViseur = new DroitProfil();

		List<Droit> droitSousAgentsByApprobateur = new ArrayList<Droit>();
		droitSousAgentsByApprobateur.add(droitOperateurOrViseur);

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentDroitFetchAgents(idAgentApprobateur)).thenReturn(droitApprobateur);
		Mockito.when(arRepo.getAgentDroitFetchAgents(idAgentOperateurOrViseur)).thenReturn(droitOperateurOrViseur);
		Mockito.when(arRepo.getDroitSousApprobateur(idAgentApprobateur)).thenReturn(droitSousAgentsByApprobateur);
		Mockito.when(arRepo.getDroitProfilByAgent(idAgentApprobateur, idAgentOperateurOrViseur)).thenReturn(droitProfilOperateurOrViseur);
		Mockito.when(arRepo.isUserOperateur(idAgentOperateurOrViseur)).thenReturn(false);
		Mockito.when(arRepo.isUserViseur(idAgentOperateurOrViseur)).thenReturn(false);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// /////////// WHEN /////////////
		ReturnMessageDto msgDto = service.setAgentsToInput(idAgentApprobateur, idAgentOperateurOrViseur, agents);

		// Then
		assertEquals(1, msgDto.getErrors().size());
		assertEquals(
				"Impossible de modifier la liste des agents saisis de l'opérateur ou du viseur [9005139] car il n'est pas un opérateur ou viseur de l'agent [9005138].",
				msgDto.getErrors().get(0));
	}

	@Test
	@Transactional("absTransactionManager")
	public void setAgentsToInput_addAgent() {

		Integer idAgentApprobateur = 9005138;
		Integer idAgentOperateurOrViseur = 9005139;

		// //////// agents a creer //////////////
		List<AgentDto> agents = new ArrayList<AgentDto>();
		AgentDto agent1 = new AgentDto();
		agent1.setIdAgent(1);
		agent1.setNom("Nico");
		AgentDto agent2 = new AgentDto();
		agent2.setIdAgent(2);
		agent2.setNom("Noemie");
		AgentDto agent3 = new AgentDto();
		agent3.setIdAgent(3);
		agent3.setNom("Johann");
		agents.add(agent1);
		agents.add(agent2);
		agents.add(agent3);

		// //////////// agents de l approbateur ///////////////
		DroitDroitsAgent dda = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(1);
		dda.setDroitsAgent(droitsAgent);

		DroitDroitsAgent dda2 = new DroitDroitsAgent();
		DroitsAgent droitsAgent2 = new DroitsAgent();
		droitsAgent2.setIdAgent(2);
		dda2.setDroitsAgent(droitsAgent2);

		DroitDroitsAgent dda3 = new DroitDroitsAgent();
		DroitsAgent droitsAgent3 = new DroitsAgent();
		droitsAgent3.setIdAgent(3);
		dda3.setDroitsAgent(droitsAgent3);

		Set<DroitDroitsAgent> droitDroitsAgentAppro = new HashSet<DroitDroitsAgent>();
		droitDroitsAgentAppro.add(dda);
		droitDroitsAgentAppro.add(dda2);
		droitDroitsAgentAppro.add(dda3);
		Droit droitApprobateur = new Droit();
		droitApprobateur.setDroitDroitsAgent(droitDroitsAgentAppro);

		// /////////////////////////////////////////////////////
		Droit droitOperateurOrViseur = new Droit();
		List<Droit> droitSousAgentsByApprobateur = new ArrayList<Droit>();
		droitSousAgentsByApprobateur.add(droitOperateurOrViseur);

		DroitProfil droitProfilOperateurOrViseur = new DroitProfil();
		
		// ////////////// Mockito /////////////////////////
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentDroitFetchAgents(idAgentApprobateur)).thenReturn(droitApprobateur);
		Mockito.when(arRepo.getAgentDroitFetchAgents(idAgentOperateurOrViseur)).thenReturn(droitOperateurOrViseur);
		Mockito.when(arRepo.getDroitSousApprobateur(idAgentApprobateur)).thenReturn(droitSousAgentsByApprobateur);
		Mockito.when(arRepo.getDroitProfilByAgent(idAgentApprobateur, idAgentOperateurOrViseur)).thenReturn(droitProfilOperateurOrViseur);
		Mockito.when(arRepo.isUserOperateur(idAgentOperateurOrViseur)).thenReturn(true);
		Mockito.when(arRepo.isUserViseur(idAgentOperateurOrViseur)).thenReturn(false);

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).persisEntity(Mockito.any(DroitDroitsAgent.class));

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// /////////// WHEN /////////////
		ReturnMessageDto msgDto = service.setAgentsToInput(idAgentApprobateur, idAgentOperateurOrViseur, agents);

		// //////////// THEN ///////////////
		assertEquals(0, msgDto.getErrors().size());
		Mockito.verify(arRepo, Mockito.times(3)).persisEntity(Mockito.isA(DroitDroitsAgent.class));
		Mockito.verify(arRepo, Mockito.times(0)).removeEntity(Mockito.isA(DroitDroitsAgent.class));
	}

	@Test
	@Transactional("absTransactionManager")
	public void setAgentsToInput_removeAgent() {

		Integer idAgentApprobateur = 9005138;
		Integer idAgentOperateurOrViseur = 9005139;

		// //////// agents a creer //////////////
		List<AgentDto> agents = new ArrayList<AgentDto>();

		// //////////// agents a supprimer ///////////////
		DroitDroitsAgent dda = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(1);
		dda.setDroitsAgent(droitsAgent);

		DroitDroitsAgent dda2 = new DroitDroitsAgent();
		DroitsAgent droitsAgent2 = new DroitsAgent();
		droitsAgent2.setIdAgent(2);
		dda2.setDroitsAgent(droitsAgent2);

		DroitDroitsAgent dda3 = new DroitDroitsAgent();
		DroitsAgent droitsAgent3 = new DroitsAgent();
		droitsAgent3.setIdAgent(3);
		dda3.setDroitsAgent(droitsAgent3);

		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.add(dda);
		droitDroitsAgent.add(dda2);
		droitDroitsAgent.add(dda3);
		Droit droitOperateurOrViseur = new Droit();
		droitOperateurOrViseur.setDroitDroitsAgent(droitDroitsAgent);

		// /////////////////////////////////////////////////////
		Droit droitApprobateur = new Droit();
		List<Droit> droitSousAgentsByApprobateur = new ArrayList<Droit>();
		droitSousAgentsByApprobateur.add(droitOperateurOrViseur);

		DroitProfil droitProfilOperateurOrViseur = new DroitProfil();
		droitProfilOperateurOrViseur.setDroitDroitsAgent(droitDroitsAgent);
		
		// ////////////// Mockito /////////////////////////
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentDroitFetchAgents(idAgentApprobateur)).thenReturn(droitApprobateur);
		Mockito.when(arRepo.getAgentDroitFetchAgents(idAgentOperateurOrViseur)).thenReturn(droitOperateurOrViseur);
		Mockito.when(arRepo.getDroitSousApprobateur(idAgentApprobateur)).thenReturn(droitSousAgentsByApprobateur);
		Mockito.when(arRepo.getDroitProfilByAgent(idAgentApprobateur, idAgentOperateurOrViseur)).thenReturn(droitProfilOperateurOrViseur);
		Mockito.when(arRepo.isUserOperateur(idAgentOperateurOrViseur)).thenReturn(true);
		Mockito.when(arRepo.isUserViseur(idAgentOperateurOrViseur)).thenReturn(false);

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).removeEntity(Mockito.any(DroitDroitsAgent.class));

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// /////////// WHEN /////////////
		ReturnMessageDto msgDto = service.setAgentsToInput(idAgentApprobateur, idAgentOperateurOrViseur, agents);

		// //////////// THEN ///////////////
		Mockito.verify(arRepo, Mockito.times(0)).persisEntity(Mockito.isA(DroitDroitsAgent.class));
		Mockito.verify(arRepo, Mockito.times(3)).removeEntity(Mockito.isA(DroitDroitsAgent.class));
		assertEquals(0, msgDto.getErrors().size());
	}

	@Test
	@Transactional("absTransactionManager")
	public void setAgentsToInput_add_and_Remove() {

		Integer idAgentApprobateur = 9005138;
		Integer idAgentOperateurOrViseur = 9005139;

		// //////// 2 agents a creer //////////////
		List<AgentDto> agents = new ArrayList<AgentDto>();
		AgentDto agent1 = new AgentDto();
		agent1.setIdAgent(1);
		agent1.setNom("Nico");
		AgentDto agent2 = new AgentDto();
		agent2.setIdAgent(2);
		agent2.setNom("Noemie");
		agents.add(agent1);
		agents.add(agent2);

		// //////////// 3 agents de l approbateur ///////////////
		DroitDroitsAgent dda = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(1);
		dda.setDroitsAgent(droitsAgent);

		DroitDroitsAgent dda2 = new DroitDroitsAgent();
		DroitsAgent droitsAgent2 = new DroitsAgent();
		droitsAgent2.setIdAgent(2);
		dda2.setDroitsAgent(droitsAgent2);

		DroitDroitsAgent dda3 = new DroitDroitsAgent();
		DroitsAgent droitsAgent3 = new DroitsAgent();
		droitsAgent3.setIdAgent(3);
		dda3.setDroitsAgent(droitsAgent3);

		Set<DroitDroitsAgent> droitDroitsAgentAppro = new HashSet<DroitDroitsAgent>();
		droitDroitsAgentAppro.add(dda);
		droitDroitsAgentAppro.add(dda2);
		droitDroitsAgentAppro.add(dda3);
		Droit droitApprobateur = new Droit();
		droitApprobateur.setDroitDroitsAgent(droitDroitsAgentAppro);

		// //////////// 1 agent a supprimer ///////////////

		DroitDroitsAgent dda3Suppr = new DroitDroitsAgent();
		DroitsAgent droitsAgentSuppr3 = new DroitsAgent();
		droitsAgentSuppr3.setIdAgent(3);
		dda3Suppr.setDroitsAgent(droitsAgentSuppr3);

		Set<DroitDroitsAgent> droitDroitsAgentSuppr = new HashSet<DroitDroitsAgent>();
		droitDroitsAgentSuppr.add(dda3Suppr);
		Droit droitOperateurOrViseur = new Droit();
		droitOperateurOrViseur.setDroitDroitsAgent(droitDroitsAgentSuppr);

		// /////////////////////////////////////////////////////
		List<Droit> droitSousAgentsByApprobateur = new ArrayList<Droit>();
		droitSousAgentsByApprobateur.add(droitOperateurOrViseur);

		DroitProfil droitProfilOperateurOrViseur = new DroitProfil();
		droitProfilOperateurOrViseur.setDroitDroitsAgent(droitDroitsAgentSuppr);
		
		// ////////////// Mockito /////////////////////////
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentDroitFetchAgents(idAgentApprobateur)).thenReturn(droitApprobateur);
		Mockito.when(arRepo.getAgentDroitFetchAgents(idAgentOperateurOrViseur)).thenReturn(droitOperateurOrViseur);
		Mockito.when(arRepo.getDroitSousApprobateur(idAgentApprobateur)).thenReturn(droitSousAgentsByApprobateur);
		Mockito.when(arRepo.getDroitProfilByAgent(idAgentApprobateur, idAgentOperateurOrViseur)).thenReturn(droitProfilOperateurOrViseur);
		Mockito.when(arRepo.isUserOperateur(idAgentOperateurOrViseur)).thenReturn(true);
		Mockito.when(arRepo.isUserViseur(idAgentOperateurOrViseur)).thenReturn(false);

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).persisEntity(Mockito.any(DroitDroitsAgent.class));

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// /////////// WHEN /////////////
		ReturnMessageDto msgDto = service.setAgentsToInput(idAgentApprobateur, idAgentOperateurOrViseur, agents);

		// //////////// THEN ///////////////
		Mockito.verify(arRepo, Mockito.times(2)).persisEntity(Mockito.isA(DroitDroitsAgent.class));
		Mockito.verify(arRepo, Mockito.times(1)).removeEntity(Mockito.isA(DroitDroitsAgent.class));
		assertEquals(0, msgDto.getErrors().size());
	}

	@Test
	@Transactional("absTransactionManager")
	public void setAgentsToInput_addAgent_DejaExistant() {

		Integer idAgentApprobateur = 9005138;
		Integer idAgentOperateurOrViseur = 9005139;

		// //////// 2 agents a creer //////////////
		List<AgentDto> agents = new ArrayList<AgentDto>();
		AgentDto agent1 = new AgentDto();
		agent1.setIdAgent(1);
		agent1.setNom("Nico");
		AgentDto agent2 = new AgentDto();
		agent2.setIdAgent(2);
		agent2.setNom("Noemie");
		agents.add(agent1);
		agents.add(agent2);

		// //////////// 3 agents de l approbateur ///////////////
		DroitDroitsAgent dda = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(1);
		dda.setDroitsAgent(droitsAgent);

		DroitDroitsAgent dda2 = new DroitDroitsAgent();
		DroitsAgent droitsAgent2 = new DroitsAgent();
		droitsAgent2.setIdAgent(2);
		dda2.setDroitsAgent(droitsAgent2);

		DroitDroitsAgent dda3 = new DroitDroitsAgent();
		DroitsAgent droitsAgent3 = new DroitsAgent();
		droitsAgent3.setIdAgent(3);
		dda3.setDroitsAgent(droitsAgent3);

		Set<DroitDroitsAgent> droitDroitsAgentAppro = new HashSet<DroitDroitsAgent>();
		droitDroitsAgentAppro.add(dda);
		droitDroitsAgentAppro.add(dda2);
		droitDroitsAgentAppro.add(dda3);
		Droit droitApprobateur = new Droit();
		droitApprobateur.setDroitDroitsAgent(droitDroitsAgentAppro);

		// //////////// 2 agents deja existant ///////////////

		DroitDroitsAgent ddaExisting = new DroitDroitsAgent();
		DroitsAgent droitsAgentExisting = new DroitsAgent();
		droitsAgentExisting.setIdAgent(1);
		ddaExisting.setDroitsAgent(droitsAgentExisting);

		DroitDroitsAgent ddaExisting2 = new DroitDroitsAgent();
		DroitsAgent droitsAgentExisting2 = new DroitsAgent();
		droitsAgentExisting2.setIdAgent(2);
		ddaExisting2.setDroitsAgent(droitsAgentExisting2);

		Set<DroitDroitsAgent> droitDroitsAgentExisting = new HashSet<DroitDroitsAgent>();
		droitDroitsAgentExisting.add(ddaExisting);
		droitDroitsAgentExisting.add(ddaExisting2);
		Droit droitOperateurOrViseur = new Droit();
		droitOperateurOrViseur.setDroitDroitsAgent(droitDroitsAgentExisting);

		// /////////////////////////////////////////////////////
		List<Droit> droitSousAgentsByApprobateur = new ArrayList<Droit>();
		droitSousAgentsByApprobateur.add(droitOperateurOrViseur);

		DroitProfil droitProfilOperateurOrViseur = new DroitProfil();
		droitProfilOperateurOrViseur.setDroitDroitsAgent(droitDroitsAgentExisting);
		
		// ////////////// Mockito /////////////////////////
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentDroitFetchAgents(idAgentApprobateur)).thenReturn(droitApprobateur);
		Mockito.when(arRepo.getAgentDroitFetchAgents(idAgentOperateurOrViseur)).thenReturn(droitOperateurOrViseur);
		Mockito.when(arRepo.getDroitSousApprobateur(idAgentApprobateur)).thenReturn(droitSousAgentsByApprobateur);
		Mockito.when(arRepo.getDroitProfilByAgent(idAgentApprobateur, idAgentOperateurOrViseur)).thenReturn(droitProfilOperateurOrViseur);
		Mockito.when(arRepo.isUserOperateur(idAgentOperateurOrViseur)).thenReturn(true);
		Mockito.when(arRepo.isUserViseur(idAgentOperateurOrViseur)).thenReturn(false);

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).persisEntity(Mockito.any(DroitDroitsAgent.class));

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// /////////// WHEN /////////////
		ReturnMessageDto msgDto = service.setAgentsToInput(idAgentApprobateur, idAgentOperateurOrViseur, agents);

		// //////////// THEN ///////////////
		Mockito.verify(arRepo, Mockito.times(0)).persisEntity(Mockito.isA(DroitDroitsAgent.class));
		Mockito.verify(arRepo, Mockito.times(0)).removeEntity(Mockito.isA(DroitDroitsAgent.class));
		assertEquals(0, msgDto.getErrors().size());
	}

	@Test
	@Transactional("absTransactionManager")
	public void setAgentsToInput_addAgent_NonApprouve() {

		Integer idAgentApprobateur = 9005138;
		Integer idAgentOperateurOrViseur = 9005139;

		// //////// 2 agents a creer //////////////
		List<AgentDto> agents = new ArrayList<AgentDto>();
		AgentDto agent1 = new AgentDto();
		agent1.setIdAgent(4);
		agent1.setNom("Nico");
		AgentDto agent2 = new AgentDto();
		agent2.setIdAgent(5);
		agent2.setNom("Noemie");
		agents.add(agent1);
		agents.add(agent2);

		// //////////// 3 agents de l approbateur ///////////////
		DroitDroitsAgent dda = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(1);
		dda.setDroitsAgent(droitsAgent);

		DroitDroitsAgent dda2 = new DroitDroitsAgent();
		DroitsAgent droitsAgent2 = new DroitsAgent();
		droitsAgent2.setIdAgent(2);
		dda2.setDroitsAgent(droitsAgent2);

		DroitDroitsAgent dda3 = new DroitDroitsAgent();
		DroitsAgent droitsAgent3 = new DroitsAgent();
		droitsAgent3.setIdAgent(3);
		dda3.setDroitsAgent(droitsAgent3);

		Set<DroitDroitsAgent> droitDroitsAgentAppro = new HashSet<DroitDroitsAgent>();
		droitDroitsAgentAppro.add(dda);
		droitDroitsAgentAppro.add(dda2);
		droitDroitsAgentAppro.add(dda3);
		Droit droitApprobateur = new Droit();
		droitApprobateur.setDroitDroitsAgent(droitDroitsAgentAppro);

		// /////////////////////////////////////////////////////
		Droit droitOperateurOrViseur = new Droit();
		List<Droit> droitSousAgentsByApprobateur = new ArrayList<Droit>();
		droitSousAgentsByApprobateur.add(droitOperateurOrViseur);

		DroitProfil droitProfilOperateurOrViseur = new DroitProfil();
		
		// ////////////// Mockito /////////////////////////
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentDroitFetchAgents(idAgentApprobateur)).thenReturn(droitApprobateur);
		Mockito.when(arRepo.getAgentDroitFetchAgents(idAgentOperateurOrViseur)).thenReturn(droitOperateurOrViseur);
		Mockito.when(arRepo.getDroitSousApprobateur(idAgentApprobateur)).thenReturn(droitSousAgentsByApprobateur);
		Mockito.when(arRepo.getDroitProfilByAgent(idAgentApprobateur, idAgentOperateurOrViseur)).thenReturn(droitProfilOperateurOrViseur);
		Mockito.when(arRepo.isUserOperateur(idAgentOperateurOrViseur)).thenReturn(true);
		Mockito.when(arRepo.isUserViseur(idAgentOperateurOrViseur)).thenReturn(false);

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).persisEntity(Mockito.any(DroitDroitsAgent.class));

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// /////////// WHEN /////////////
		ReturnMessageDto msgDto = service.setAgentsToInput(idAgentApprobateur, idAgentOperateurOrViseur, agents);

		// //////////// THEN ///////////////
		Mockito.verify(arRepo, Mockito.times(0)).persisEntity(Mockito.isA(DroitDroitsAgent.class));
		Mockito.verify(arRepo, Mockito.times(0)).removeEntity(Mockito.isA(DroitDroitsAgent.class));
		assertEquals(0, msgDto.getErrors().size());
	}

	@Test
	@Transactional("absTransactionManager")
	public void setAgentsToApprove_addAgent() {

		Integer idAgentApprobateur = 9005138;

		// //////// agents a creer //////////////
		AgentDto ag = new AgentDto();
		ag.setIdAgent(9008765);
		List<AgentDto> agsDto = Arrays.asList(ag);

		// //////////// agents de l approbateur ///////////////
		DroitDroitsAgent dda = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(1);
		dda.setDroitsAgent(droitsAgent);

		Set<DroitDroitsAgent> droitDroitsAgentAppro = new HashSet<DroitDroitsAgent>();
		droitDroitsAgentAppro.add(dda);

		// APPROBATEUR///////////////////////
		Profil pr = new Profil();
		pr.setLibelle("APPROBATEUR");

		DroitProfil dpr = new DroitProfil();
		dpr.setProfil(pr);

		Droit da = new Droit();
		da.setIdAgent(idAgentApprobateur);
		da.getDroitProfils().add(dpr);
		da.setDroitDroitsAgent(droitDroitsAgentAppro);

		Date currentDate = new DateTime(2013, 4, 9, 12, 9, 34).toDate();

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(9008765);
		agDto.setService("service");
		agDto.setCodeService("CODE");

		// ////////////// Mockito /////////////////////////
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentAccessRights(idAgentApprobateur)).thenReturn(da);
		Mockito.when(arRepo.getDroitsAgent(1)).thenReturn(new DroitsAgent());
		Mockito.when(arRepo.getDroitProfilApprobateur(idAgentApprobateur)).thenReturn(dpr);

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).persisEntity(Mockito.any(DroitsAgent.class));

		ISirhWSConsumer wsMock = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(wsMock.getAgentService(9008765, currentDate)).thenReturn(agDto);

		HelperService hS = Mockito.mock(HelperService.class);
		Mockito.when(hS.getCurrentDate()).thenReturn(currentDate);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);
		ReflectionTestUtils.setField(service, "helperService", hS);

		// /////////// WHEN /////////////
		ReturnMessageDto msgDto = service.setAgentsToApprove(idAgentApprobateur, agsDto);

		// //////////// THEN ///////////////
		Mockito.verify(arRepo, Mockito.times(1)).persisEntity(Mockito.isA(DroitsAgent.class));
		assertEquals(0, msgDto.getErrors().size());
	}

	@Test
	@Transactional("absTransactionManager")
	public void setAgentsToApprove_addAgent_NoServiceExisting() {

		Integer idAgentApprobateur = 9005138;

		// //////// agents a creer //////////////
		AgentDto ag = new AgentDto();
		ag.setIdAgent(9008765);
		List<AgentDto> agsDto = Arrays.asList(ag);

		// //////////// agents de l approbateur ///////////////
		DroitDroitsAgent dda = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(1);
		dda.setDroitsAgent(droitsAgent);

		Set<DroitDroitsAgent> droitDroitsAgentAppro = new HashSet<DroitDroitsAgent>();
		droitDroitsAgentAppro.add(dda);

		// APPROBATEUR///////////////////////
		Profil pr = new Profil();
		pr.setLibelle("APPROBATEUR");

		DroitProfil dpr = new DroitProfil();
		dpr.setProfil(pr);

		Droit da = new Droit();
		da.setIdAgent(idAgentApprobateur);
		da.getDroitProfils().add(dpr);
		da.setDroitDroitsAgent(droitDroitsAgentAppro);

		Date currentDate = new DateTime(2013, 4, 9, 12, 9, 34).toDate();

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(9008765);
		agDto.setService("service");
		agDto.setCodeService("CODE");

		// ////////////// Mockito /////////////////////////
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentAccessRights(idAgentApprobateur)).thenReturn(da);
		Mockito.when(arRepo.getDroitsAgent(1)).thenReturn(new DroitsAgent());
		Mockito.when(arRepo.getDroitProfilApprobateur(idAgentApprobateur)).thenReturn(dpr);

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).persisEntity(Mockito.any(DroitsAgent.class));

		ISirhWSConsumer wsMock = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(wsMock.getAgentService(9008765, currentDate)).thenReturn(null);

		HelperService hS = Mockito.mock(HelperService.class);
		Mockito.when(hS.getCurrentDate()).thenReturn(currentDate);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);
		ReflectionTestUtils.setField(service, "helperService", hS);

		// /////////// WHEN /////////////
		ReturnMessageDto msgDto = service.setAgentsToApprove(idAgentApprobateur, agsDto);

		// //////////// THEN ///////////////
		Mockito.verify(arRepo, Mockito.times(0)).persisEntity(Mockito.isA(DroitsAgent.class));
		assertEquals(1, msgDto.getErrors().size());
		assertEquals("L'agent [9008765] n'existe pas.", msgDto.getErrors().get(0));
	}

	@Test
	@Transactional("absTransactionManager")
	public void setAgentsToApprove_removeAgent() {

		Integer idAgentApprobateur = 9005138;

		// //////// agents a creer //////////////
		AgentDto ag = new AgentDto();
		ag.setIdAgent(9008765);
		List<AgentDto> agsDto = Arrays.asList(ag);

		// //////////// agents de l approbateur ///////////////
		DroitDroitsAgent dda = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(1);
		dda.setDroitsAgent(droitsAgent);

		Set<DroitDroitsAgent> droitDroitsAgentAppro = new HashSet<DroitDroitsAgent>();
		droitDroitsAgentAppro.add(dda);

		// APPROBATEUR///////////////////////
		Profil pr = new Profil();
		pr.setLibelle("APPROBATEUR");

		DroitProfil dpr = new DroitProfil();
		dpr.setProfil(pr);

		Droit da = new Droit();
		da.setIdAgent(idAgentApprobateur);
		da.getDroitProfils().add(dpr);
		da.setDroitDroitsAgent(droitDroitsAgentAppro);

		Date currentDate = new DateTime(2013, 4, 9, 12, 9, 34).toDate();

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(9008765);
		agDto.setService("service");
		agDto.setCodeService("CODE");

		// ////////////// Mockito /////////////////////////
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentAccessRights(idAgentApprobateur)).thenReturn(da);
		Mockito.when(arRepo.getDroitsAgent(1)).thenReturn(new DroitsAgent());
		Mockito.when(arRepo.getDroitProfilApprobateur(idAgentApprobateur)).thenReturn(dpr);

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).removeEntity(Mockito.any(DroitDroitsAgent.class));

		ISirhWSConsumer wsMock = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(wsMock.getAgentService(9008765, currentDate)).thenReturn(agDto);

		HelperService hS = Mockito.mock(HelperService.class);
		Mockito.when(hS.getCurrentDate()).thenReturn(currentDate);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);
		ReflectionTestUtils.setField(service, "helperService", hS);

		// /////////// WHEN /////////////
		ReturnMessageDto msgDto = service.setAgentsToApprove(idAgentApprobateur, agsDto);

		// //////////// THEN ///////////////
		Mockito.verify(arRepo, Mockito.times(0)).persisEntity(Mockito.isA(DroitDroitsAgent.class));
		Mockito.verify(arRepo, Mockito.times(1)).removeEntity(Mockito.isA(DroitDroitsAgent.class));
		assertEquals(0, msgDto.getErrors().size());
	}

	@Test
	public void getViseurs_ReturnEmptyDto() {

		// Given
		Integer idAgent = 9005138;

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getDroitSousApprobateur(idAgent)).thenReturn(new ArrayList<Droit>());

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// When
		ViseursDto result = service.getViseurs(idAgent);

		// Then
		assertEquals(0, result.getViseurs().size());
	}

	@Test
	public void getViseurs_Return1Delegataire() {

		// Given
		Integer idAgentAppro = 9005138;
		Droit droitAppro = new Droit();
		droitAppro.setIdAgent(idAgentAppro);
		Integer idAgentDelegataire = 9005131;
		Droit droitDelegataire = new Droit();
		DroitProfil dp = new DroitProfil();
		Profil p = new Profil();
		p.setLibelle("DELEGATAIRE");
		dp.setProfil(p);
		dp.setDroitApprobateur(droitAppro);
		dp.setDroit(droitDelegataire);
		droitDelegataire.setIdAgent(idAgentDelegataire);
		droitDelegataire.getDroitProfils().add(dp);
		AgentGeneriqueDto agentDelegataire = new AgentGeneriqueDto();
		agentDelegataire.setIdAgent(idAgentDelegataire);
		agentDelegataire.setPrenomUsage("TEST");
		agentDelegataire.setNomatr(5138);

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getDroitSousApprobateur(idAgentAppro)).thenReturn(Arrays.asList(droitDelegataire));
		Mockito.when(arRepo.isUserDelegataireOfApprobateur(idAgentAppro, idAgentDelegataire)).thenReturn(true);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(idAgentDelegataire)).thenReturn(agentDelegataire);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		// When
		ViseursDto result = service.getViseurs(idAgentAppro);

		// Then
		assertEquals(0, result.getViseurs().size());
	}

	@Test
	public void getViseurs_Return2Viseurs() {

		// Given
		Integer idAgentAppro = 9005138;
		Integer idAgentViseur1 = 9005131;
		Integer idAgentViseur2 = 9002990;

		// on declare un profil approbateur
		Profil appro = new Profil();
		appro.setLibelle("APPROBATEUR");

		// on declare un profil viseur
		Profil viseur = new Profil();
		appro.setLibelle("VISEUR");

		// on declare un approbateur
		Droit droitAppro = new Droit();
		droitAppro.setIdAgent(idAgentAppro);
		DroitProfil dpAppro = new DroitProfil();
		dpAppro.setDroit(droitAppro);
		dpAppro.setDroitApprobateur(droitAppro);
		dpAppro.setProfil(appro);
		droitAppro.getDroitProfils().add(dpAppro);

		// on declare un viseur 1
		Droit droitViseur1 = new Droit();
		droitViseur1.setIdAgent(idAgentViseur1);
		DroitProfil dp1 = new DroitProfil();
		dp1.setDroitApprobateur(droitAppro);
		dp1.setDroit(droitViseur1);
		dp1.setProfil(viseur);
		droitViseur1.getDroitProfils().add(dp1);

		// on declare un viseur 2
		Droit droitViseur2 = new Droit();
		droitViseur2.setIdAgent(idAgentViseur2);
		DroitProfil dp2 = new DroitProfil();
		dp2.setDroitApprobateur(droitAppro);
		dp2.setDroit(droitViseur2);
		dp2.setProfil(viseur);
		droitViseur2.getDroitProfils().add(dp2);

		// on nomme les viseurs
		AgentGeneriqueDto agentViseur1 = new AgentGeneriqueDto();
		agentViseur1.setIdAgent(idAgentViseur1);
		agentViseur1.setPrenomUsage("TEST");

		AgentGeneriqueDto agentViseur2 = new AgentGeneriqueDto();
		agentViseur2.setIdAgent(idAgentViseur2);
		agentViseur2.setPrenomUsage("TEST2");

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getDroitSousApprobateur(idAgentAppro))
				.thenReturn(Arrays.asList(droitViseur1, droitViseur2));
		Mockito.when(arRepo.isUserViseurOfApprobateur(idAgentAppro, idAgentViseur1)).thenReturn(true);
		Mockito.when(arRepo.isUserViseurOfApprobateur(idAgentAppro, idAgentViseur2)).thenReturn(true);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(idAgentViseur1)).thenReturn(agentViseur1);
		Mockito.when(sirhWSConsumer.getAgent(idAgentViseur2)).thenReturn(agentViseur2);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		// When
		ViseursDto result = service.getViseurs(idAgentAppro);

		// Then
		assertEquals(2, result.getViseurs().size());
		assertEquals(9005131, (int) result.getViseurs().get(0).getIdAgent());
		assertEquals("TEST", (String) result.getViseurs().get(0).getPrenom());
		assertEquals(9002990, (int) result.getViseurs().get(1).getIdAgent());
		assertEquals("TEST2", (String) result.getViseurs().get(1).getPrenom());
	}

	@Test
	public void setViseurs_ViseurNoExisting() {
		// Given
		Integer idAgent = 9005138;

		ViseursDto dto = new ViseursDto();

		AgentDto viseur = new AgentDto();
		viseur.setIdAgent(9001234);
		dto.setViseurs(Arrays.asList(viseur));

		Droit dd = Mockito.spy(new Droit());

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentAccessRights(idAgent)).thenReturn(dd);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).removeEntity(Mockito.any(DroitDroitsAgent.class));

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(viseur.getIdAgent())).thenReturn(null);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		// When
		ReturnMessageDto msgDto = service.setViseurs(9005138, dto);

		// Then
		assertEquals(1, msgDto.getErrors().size());
		assertTrue(msgDto.getErrors().get(0).contains("n'existe pas"));
	}

	@Test
	public void setViseurs_ViseurCanNotBe() {
		// Given
		Integer idAgent = 9005138;

		ViseursDto dto = new ViseursDto();

		AgentDto viseur = new AgentDto();
		viseur.setIdAgent(9001238);
		AgentDto viseur2 = new AgentDto();
		viseur2.setIdAgent(9001239);
		dto.setViseurs(Arrays.asList(viseur, viseur2));

		Droit dd = Mockito.spy(new Droit());

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentAccessRights(idAgent)).thenReturn(dd);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).removeEntity(Mockito.any(DroitDroitsAgent.class));

		Mockito.when(arRepo.isUserApprobateur(viseur.getIdAgent())).thenReturn(true);
		Mockito.when(arRepo.isUserOperateur(viseur2.getIdAgent())).thenReturn(true);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(viseur.getIdAgent())).thenReturn(new AgentGeneriqueDto());
		Mockito.when(sirhWSConsumer.getAgent(viseur2.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		// When
		ReturnMessageDto msgDto = service.setViseurs(9005138, dto);

		// Then
		assertEquals(2, msgDto.getErrors().size());
		assertTrue(msgDto.getErrors().get(0).contains("ne peut pas être viseur car il ou elle est déjà approbateur."));
		assertTrue(msgDto.getErrors().get(1).contains("ne peut pas être viseur car il ou elle est déjà opérateur."));
	}

	@Test
	public void setViseurs_ViseurPersist() {
		// Given
		Integer idAgent = 9005138;

		ViseursDto dto = new ViseursDto();

		AgentDto viseur = new AgentDto();
		viseur.setIdAgent(9001238);
		AgentDto viseur2 = new AgentDto();
		viseur2.setIdAgent(9001239);
		dto.setViseurs(Arrays.asList(viseur, viseur2));

		Droit dd = Mockito.spy(new Droit());

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentAccessRights(idAgent)).thenReturn(dd);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).removeEntity(Mockito.any(DroitDroitsAgent.class));

		Mockito.when(arRepo.isUserApprobateur(viseur.getIdAgent())).thenReturn(false);
		Mockito.when(arRepo.isUserOperateur(viseur2.getIdAgent())).thenReturn(false);
		Mockito.when(arRepo.getAgentAccessRights(viseur.getIdAgent())).thenReturn(new Droit());
		Mockito.when(arRepo.getAgentAccessRights(viseur2.getIdAgent())).thenReturn(new Droit());

		Mockito.when(arRepo.getProfilByName(ProfilEnum.DELEGATAIRE.toString())).thenReturn(new Profil());
		Mockito.when(arRepo.getProfilByName(ProfilEnum.OPERATEUR.toString())).thenReturn(new Profil());
		Mockito.when(arRepo.getProfilByName(ProfilEnum.VISEUR.toString())).thenReturn(new Profil());

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(viseur.getIdAgent())).thenReturn(new AgentGeneriqueDto());
		Mockito.when(sirhWSConsumer.getAgent(viseur2.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		final Date d = new Date();

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).persisEntity(Mockito.any(Droit.class));

		HelperService helpServ = Mockito.mock(HelperService.class);
		Mockito.when(helpServ.getCurrentDate()).thenReturn(d);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", helpServ);

		// When
		ReturnMessageDto msgDto = service.setViseurs(9005138, dto);

		// Then
		Mockito.verify(arRepo, Mockito.times(2)).persisEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(0)).removeEntity(Mockito.isA(Droit.class));
		assertEquals(0, msgDto.getErrors().size());
	}

	@Test
	public void setViseurs_ViseurPersist_AvecViseurDejaExistant() {
		// Given
		Integer idAgent = 9005138;

		// /////////////////// VISEUR A CREER
		// /////////////////
		ViseursDto dto = new ViseursDto();
		// 2 viseurs dont un deja existant
		AgentDto viseur = new AgentDto();
		viseur.setIdAgent(9001238);
		AgentDto viseur2 = new AgentDto();
		viseur2.setIdAgent(9001239);
		dto.setViseurs(Arrays.asList(viseur, viseur2));
		// //////////////////////////////////////////////////////////////

		// /////////////// VISEUR DEJA EXISTANT
		// /////////////////////
		List<Droit> listDroitSousAgentsByApprobateur = new ArrayList<Droit>();
		// approbateur
		Droit droitApprobateur = new Droit();
		droitApprobateur.setIdAgent(idAgent);
		droitApprobateur.setIdDroit(13);

		// viseur deja existant
		DroitProfil droitProfilViseurExistant = new DroitProfil();
		droitProfilViseurExistant.setDroitApprobateur(droitApprobateur);
		Set<DroitProfil> droitProfilViseurExistants = new HashSet<DroitProfil>();
		droitProfilViseurExistants.add(droitProfilViseurExistant);

		Droit droitViseurExistant = new Droit();
		droitViseurExistant.setIdAgent(9001238);
		droitProfilViseurExistant.setDroit(droitViseurExistant);
		droitViseurExistant.setDroitProfils(droitProfilViseurExistants);

		listDroitSousAgentsByApprobateur.add(droitViseurExistant);
		// ////////////////////////////////////////////////////

		// ////////////////// Mockito ////////////////////
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentAccessRights(idAgent)).thenReturn(droitApprobateur);
		Mockito.when(arRepo.getDroitSousApprobateur(idAgent)).thenReturn(listDroitSousAgentsByApprobateur);

		Mockito.when(arRepo.isUserViseurOfApprobateur(idAgent, droitViseurExistant.getIdAgent())).thenReturn(true);

		Mockito.when(arRepo.isUserApprobateur(viseur.getIdAgent())).thenReturn(false);
		Mockito.when(arRepo.isUserOperateur(viseur2.getIdAgent())).thenReturn(false);

		Mockito.when(arRepo.getAgentAccessRights(viseur.getIdAgent())).thenReturn(new Droit());
		Mockito.when(arRepo.getAgentAccessRights(viseur2.getIdAgent())).thenReturn(new Droit());

		Mockito.when(arRepo.getProfilByName(ProfilEnum.VISEUR.toString())).thenReturn(new Profil());

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(viseur.getIdAgent())).thenReturn(new AgentGeneriqueDto());
		Mockito.when(sirhWSConsumer.getAgent(viseur2.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		final Date d = new Date();

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).persisEntity(Mockito.any(Droit.class));

		HelperService helpServ = Mockito.mock(HelperService.class);
		Mockito.when(helpServ.getCurrentDate()).thenReturn(d);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", helpServ);

		// ///////////////////////// TESTS ////////////////////////
		// When
		ReturnMessageDto msgDto = service.setViseurs(9005138, dto);

		// Then
		Mockito.verify(arRepo, Mockito.times(1)).persisEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(0)).removeEntity(Mockito.isA(Droit.class));
		assertEquals(0, msgDto.getErrors().size());
	}

	@Test
	public void setViseurs_ViseurRemove() {
		// Given
		Integer idAgent = 9005138;

		// approbateur
		Droit droitApprobateur = new Droit();
		droitApprobateur.setIdAgent(idAgent);
		droitApprobateur.setIdDroit(13);

		ViseursDto dto = new ViseursDto();
		dto.setViseurs(new ArrayList<AgentDto>());

		List<Droit> listDroitSousAgentsByApprobateur = new ArrayList<Droit>();

		// viseur existant a supprimer
		DroitProfil droitProfilViseurExistant = new DroitProfil();
		droitProfilViseurExistant.setDroitApprobateur(droitApprobateur);
		Set<DroitProfil> droitProfilViseurExistants = new HashSet<DroitProfil>();
		droitProfilViseurExistants.add(droitProfilViseurExistant);

		Droit droitViseurExistant = new Droit();
		droitViseurExistant.setIdAgent(2);
		droitProfilViseurExistant.setDroit(droitViseurExistant);
		droitViseurExistant.setDroitProfils(droitProfilViseurExistants);

		listDroitSousAgentsByApprobateur.add(droitViseurExistant);

		// mock
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentAccessRights(idAgent)).thenReturn(droitApprobateur);
		Mockito.when(arRepo.getDroitSousApprobateur(idAgent)).thenReturn(listDroitSousAgentsByApprobateur);
		Mockito.when(arRepo.isUserViseurOfApprobateur(idAgent, droitViseurExistant.getIdAgent())).thenReturn(true);

		Mockito.when(arRepo.getProfilByName(ProfilEnum.DELEGATAIRE.toString())).thenReturn(new Profil());
		Mockito.when(arRepo.getProfilByName(ProfilEnum.OPERATEUR.toString())).thenReturn(new Profil());
		Mockito.when(arRepo.getProfilByName(ProfilEnum.VISEUR.toString())).thenReturn(new Profil());

		ISirhRepository sirhRepo = Mockito.mock(ISirhRepository.class);

		final Date d = new Date();

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).persisEntity(Mockito.any(Droit.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).removeEntity(Mockito.any(Droit.class));

		HelperService helpServ = Mockito.mock(HelperService.class);
		Mockito.when(helpServ.getCurrentDate()).thenReturn(d);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepo);
		ReflectionTestUtils.setField(service, "helperService", helpServ);

		// When
		ReturnMessageDto msgDto = service.setViseurs(9005138, dto);

		// Then
		Mockito.verify(arRepo, Mockito.times(0)).persisEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(1)).removeEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(1)).removeEntity(Mockito.isA(DroitProfil.class));
		assertEquals(0, msgDto.getErrors().size());
	}

	@Test
	public void setViseurs_Viseur_PersistAndRemove_AndAgentDejaExistant() {
		// Given
		Integer idAgent = 9005138;
		ViseursDto dto = new ViseursDto();

		// ///////////// CREATION //////////////////
		// approbateur
		Droit droitApprobateur = new Droit();
		droitApprobateur.setIdAgent(idAgent);
		droitApprobateur.setIdDroit(13);

		// viseur a creer
		AgentDto viseur = new AgentDto();
		viseur.setIdAgent(9001238);
		AgentDto viseur2 = new AgentDto();
		viseur2.setIdAgent(9001239);
		dto.setViseurs(Arrays.asList(viseur, viseur2));
		// ////////////////////////////////////////////////////

		// /////////////// VISEUR DEJA EXISTANTS
		// /////////////////////
		List<Droit> listDroitSousAgentsByApprobateur = new ArrayList<Droit>();

		// viseur deja existant
		DroitProfil droitProfilViseurExistant = new DroitProfil();
		droitProfilViseurExistant.setDroitApprobateur(droitApprobateur);
		Set<DroitProfil> droitProfilViseurExistants = new HashSet<DroitProfil>();
		droitProfilViseurExistants.add(droitProfilViseurExistant);

		Droit droitViseurExistant = new Droit();
		droitViseurExistant.setIdAgent(9001238);
		droitProfilViseurExistant.setDroit(droitViseurExistant);
		droitViseurExistant.setDroitProfils(droitProfilViseurExistants);

		listDroitSousAgentsByApprobateur.add(droitViseurExistant);
		// ////////////////////////////////////////////////////

		// ///////////// SUPPRESSION //////////////////
		// viseur existant a supprimer
		DroitProfil droitProfilViseurASupprimer = new DroitProfil();
		droitProfilViseurASupprimer.setDroitApprobateur(droitApprobateur);
		Set<DroitProfil> droitProfilViseursASupprimer = new HashSet<DroitProfil>();
		droitProfilViseursASupprimer.add(droitProfilViseurASupprimer);

		Droit droitViseurASupprimer = new Droit();
		droitViseurASupprimer.setIdAgent(2);
		droitProfilViseurASupprimer.setDroit(droitViseurASupprimer);
		droitViseurASupprimer.setDroitProfils(droitProfilViseursASupprimer);

		listDroitSousAgentsByApprobateur.add(droitViseurASupprimer);

		// ////////////// mock //////////////////////
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentAccessRights(idAgent)).thenReturn(droitApprobateur);
		Mockito.when(arRepo.getDroitSousApprobateur(idAgent)).thenReturn(listDroitSousAgentsByApprobateur);

		Mockito.when(arRepo.isUserViseurOfApprobateur(idAgent, droitViseurExistant.getIdAgent())).thenReturn(true);

		Mockito.when(arRepo.isUserViseurOfApprobateur(idAgent, droitViseurASupprimer.getIdAgent())).thenReturn(true);

		Mockito.when(arRepo.isUserOperateur(viseur2.getIdAgent())).thenReturn(false);

		Mockito.when(arRepo.getAgentAccessRights(viseur2.getIdAgent())).thenReturn(new Droit());

		Mockito.when(arRepo.getProfilByName(ProfilEnum.DELEGATAIRE.toString())).thenReturn(new Profil());
		Mockito.when(arRepo.getProfilByName(ProfilEnum.OPERATEUR.toString())).thenReturn(new Profil());
		Mockito.when(arRepo.getProfilByName(ProfilEnum.VISEUR.toString())).thenReturn(new Profil());

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(viseur2.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		final Date d = new Date();

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).persisEntity(Mockito.any(Droit.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).removeEntity(Mockito.any(Droit.class));

		HelperService helpServ = Mockito.mock(HelperService.class);
		Mockito.when(helpServ.getCurrentDate()).thenReturn(d);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", helpServ);

		// /////////// When //////////////
		ReturnMessageDto msgDto = service.setViseurs(9005138, dto);

		// ///////////// Then //////////////
		assertEquals(0, msgDto.getErrors().size());
		Mockito.verify(arRepo, Mockito.times(1)).persisEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(1)).removeEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(1)).removeEntity(Mockito.isA(DroitProfil.class));
	}

	@Test
	public void setViseurs_ViseurDeDeuxApprobateursRemove() {
		// Given
		Integer idAgent = 9005138;
		Integer idAgent2 = 9005139;

		// approbateur
		Droit droitApprobateur = new Droit();
		droitApprobateur.setIdAgent(idAgent);
		droitApprobateur.setIdDroit(13);

		Droit droitApprobateur2 = new Droit();
		droitApprobateur2.setIdAgent(idAgent2);
		droitApprobateur2.setIdDroit(14);

		ViseursDto dto = new ViseursDto();
		dto.setViseurs(new ArrayList<AgentDto>());

		List<Droit> listDroitSousAgentsByApprobateur = new ArrayList<Droit>();

		// operateur existant a supprimer avec 2 DroitProfil de 2 approbateurs
		DroitProfil droitProfilViseurExistant = new DroitProfil();
		droitProfilViseurExistant.setDroitApprobateur(droitApprobateur);

		DroitProfil droitProfilViseurExistant2 = new DroitProfil();
		droitProfilViseurExistant2.setDroitApprobateur(droitApprobateur2);

		Set<DroitProfil> droitProfiltOperateurExistants = new HashSet<DroitProfil>();
		droitProfiltOperateurExistants.add(droitProfilViseurExistant);
		droitProfiltOperateurExistants.add(droitProfilViseurExistant2);

		Droit droitViseurExistant = new Droit();
		droitViseurExistant.setIdAgent(1);
		droitProfilViseurExistant.setDroit(droitViseurExistant);
		droitProfilViseurExistant2.setDroit(droitViseurExistant);
		droitViseurExistant.setDroitProfils(droitProfiltOperateurExistants);

		listDroitSousAgentsByApprobateur.add(droitViseurExistant);

		// mock
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentAccessRights(idAgent)).thenReturn(droitApprobateur);
		Mockito.when(arRepo.getDroitSousApprobateur(idAgent)).thenReturn(listDroitSousAgentsByApprobateur);
		Mockito.when(arRepo.isUserViseurOfApprobateur(idAgent, droitViseurExistant.getIdAgent())).thenReturn(true);

		Mockito.when(arRepo.getProfilByName(ProfilEnum.VISEUR.toString())).thenReturn(new Profil());

		ISirhRepository sirhRepo = Mockito.mock(ISirhRepository.class);

		final Date d = new Date();

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).persisEntity(Mockito.any(Droit.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).removeEntity(Mockito.any(Droit.class));

		HelperService helpServ = Mockito.mock(HelperService.class);
		Mockito.when(helpServ.getCurrentDate()).thenReturn(d);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepo);
		ReflectionTestUtils.setField(service, "helperService", helpServ);

		// When
		ReturnMessageDto msgDto = service.setViseurs(9005138, dto);

		// Then
		assertEquals(0, msgDto.getErrors().size());
		Mockito.verify(arRepo, Mockito.times(0)).persisEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(0)).removeEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(1)).removeEntity(Mockito.isA(DroitProfil.class));

	}

	@Test
	public void getApprobateurOfAgent_1Approbateur() {
		// Given
		Integer idAgent = 9005131;
		Integer idApprobateur = 9005138;
		final Date d = new Date();

		AgentWithServiceDto dtoAppro = new AgentWithServiceDto();
		dtoAppro.setIdAgent(idApprobateur);
		dtoAppro.setNom("TEST");

		DroitsAgent droitAgent = new DroitsAgent();
		droitAgent.setIdAgent(idAgent);

		Droit droitAppro = new Droit();
		droitAppro.setIdAgent(idApprobateur);

		// mock
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getDroitsAgent(idAgent)).thenReturn(droitAgent);
		Mockito.when(arRepo.getApprobateurOfAgent(droitAgent)).thenReturn(droitAppro);

		ISirhWSConsumer wsMock = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(wsMock.getAgentService(idApprobateur, d)).thenReturn(dtoAppro);

		HelperService helpServ = Mockito.mock(HelperService.class);
		Mockito.when(helpServ.getCurrentDate()).thenReturn(d);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);
		ReflectionTestUtils.setField(service, "helperService", helpServ);

		// When
		AgentWithServiceDto dto = service.getApprobateurOfAgent(idAgent);

		// Then
		assertEquals("TEST", dto.getNom());
		assertEquals(idApprobateur, dto.getIdAgent());
	}

	@Test
	public void getAgentsToApproveWithoutProfil_NoAgents_ReturnEmptyList() {

		// Given
		Integer idAgent = 9007654;
		String codeService = "TEST";

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getListOfAgentsToInputOrApprove(idAgent, codeService)).thenReturn(
				new ArrayList<DroitsAgent>());

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// When
		List<AgentDto> result = service.getAgentsToApproveOrInput(idAgent, codeService);

		// Then
		assertEquals(0, result.size());
	}

	@Test
	public void getAgentsToApproveWithoutProfil_2Agents_ReturnListOf2() {

		// Given
		Integer idAgent = 9007654;
		String codeService = null;

		AgentGeneriqueDto a1 = new AgentGeneriqueDto();
		a1.setIdAgent(1);
		a1.setNomUsage("NOM TEST 1");
		a1.setPrenomUsage("PRENOM TEST 1");
		AgentGeneriqueDto a2 = new AgentGeneriqueDto();
		a2.setIdAgent(2);
		a2.setNomUsage("NOM TEST 2");
		a2.setPrenomUsage("PRENOM TEST 2");

		DroitsAgent da1 = new DroitsAgent();
		da1.setIdAgent(1);
		DroitsAgent da2 = new DroitsAgent();
		da2.setIdAgent(2);

		Droit d = new Droit();

		DroitDroitsAgent dda = new DroitDroitsAgent();
		dda.setDroit(d);
		dda.setDroitsAgent(da1);
		DroitDroitsAgent dda2 = new DroitDroitsAgent();
		dda2.setDroit(d);
		dda2.setDroitsAgent(da2);

		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.addAll(Arrays.asList(dda, dda2));

		d.setDroitDroitsAgent(droitDroitsAgent);

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getListOfAgentsToInputOrApprove(idAgent, null)).thenReturn(Arrays.asList(da1, da2));

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(1)).thenReturn(a1);
		Mockito.when(sirhWSConsumer.getAgent(2)).thenReturn(a2);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		// When
		List<AgentDto> result = service.getAgentsToApproveOrInput(idAgent, codeService);

		// Then
		assertEquals(2, result.size());
		assertEquals(result.get(0).getIdAgent(), new Integer(1));
		assertEquals(result.get(0).getNom(), "NOM TEST 1");
		assertEquals(result.get(0).getPrenom(), "PRENOM TEST 1");
		assertEquals(result.get(1).getIdAgent(), new Integer(2));
		assertEquals(result.get(1).getNom(), "NOM TEST 2");
		assertEquals(result.get(1).getPrenom(), "PRENOM TEST 2");
	}

	@Test
	public void getAgentsServicesToApproveOrInput_2agents_return2Dtos() {

		// Given
		Integer idAgent = 9007654;

		DroitsAgent da1 = new DroitsAgent();
		da1.setIdAgent(1);
		da1.setCodeService("SERV 1");
		da1.setLibelleService("SERVICE 1");
		DroitsAgent da2 = new DroitsAgent();
		da2.setIdAgent(2);
		da2.setCodeService("SERV 2");
		da2.setLibelleService("SERVICE 2");

		Droit d = new Droit();

		DroitDroitsAgent dda = new DroitDroitsAgent();
		dda.setDroit(d);
		dda.setDroitsAgent(da1);
		DroitDroitsAgent dda2 = new DroitDroitsAgent();
		dda2.setDroit(d);
		dda2.setDroitsAgent(da2);

		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.addAll(Arrays.asList(dda, dda2));

		d.setDroitDroitsAgent(droitDroitsAgent);

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getListOfAgentsToInputOrApprove(idAgent, null)).thenReturn(Arrays.asList(da1, da2));

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// When
		List<ServiceDto> result = service.getAgentsServicesToApproveOrInput(idAgent);

		// Then
		assertEquals(2, result.size());
		assertEquals("SERV 1", result.get(0).getCodeService());
		assertEquals("SERVICE 2", result.get(1).getService());
	}

	@Test
	public void getAgentsServicesToApproveOrInput_2agentsSameService_return1Dtos() {

		// Given
		Integer idAgent = 9007654;

		DroitsAgent da1 = new DroitsAgent();
		da1.setIdAgent(1);
		da1.setCodeService("SERV 1");
		da1.setLibelleService("SERVICE 1");
		DroitsAgent da2 = new DroitsAgent();
		da2.setIdAgent(2);
		da2.setCodeService("SERV 1");
		da2.setLibelleService("SERVICE 1");

		Droit d = new Droit();

		DroitDroitsAgent dda = new DroitDroitsAgent();
		dda.setDroit(d);
		dda.setDroitsAgent(da1);
		DroitDroitsAgent dda2 = new DroitDroitsAgent();
		dda2.setDroit(d);
		dda2.setDroitsAgent(da2);

		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.addAll(Arrays.asList(dda, dda2));

		d.setDroitDroitsAgent(droitDroitsAgent);

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getListOfAgentsToInputOrApprove(idAgent, null)).thenReturn(Arrays.asList(da1, da2));

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// When
		List<ServiceDto> result = service.getAgentsServicesToApproveOrInput(idAgent);

		// Then
		assertEquals(1, result.size());
	}

	@Test
	public void verifAccessRightSaveDemande_AgentNotOperateur() {

		// Given
		ReturnMessageDto returnDto = new ReturnMessageDto();
		Integer idAgent = 9006543;

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.isUserOperateur(idAgent)).thenReturn(false);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// When
		returnDto = service.verifAccessRightDemande(idAgent, 9005138, returnDto);

		// Then
		assertEquals(1, returnDto.getErrors().size());
		assertEquals("Vous n'êtes ni opérateur,ni approbateur, ni viseur. Vous ne pouvez pas saisir de demandes.",
				returnDto.getErrors().get(0));
	}

	@Test
	public void verifAccessRightSaveDemande_Operateur_NotAgentOfOperateur() {

		// Given
		ReturnMessageDto returnDto = new ReturnMessageDto();
		Integer idAgent = 9006543;

		Profil p = new Profil();
		p.setLibelle(ProfilEnum.OPERATEUR.toString());

		DroitProfil dpOpe = new DroitProfil();
		dpOpe.setDroitApprobateur(new Droit());
		dpOpe.setProfil(p);
		Set<DroitProfil> setDroitProfil = new HashSet<DroitProfil>();
		setDroitProfil.add(dpOpe);

		DroitsAgent da = new DroitsAgent();
		da.setIdAgent(9005131);

		DroitDroitsAgent dda = new DroitDroitsAgent();
		dda.setDroitProfil(dpOpe);
		dda.setDroitsAgent(da);
		Set<DroitDroitsAgent> setDroitDroitsAgent = new HashSet<DroitDroitsAgent>();
		setDroitDroitsAgent.add(dda);

		Droit d = new Droit();
		d.setIdAgent(idAgent);
		d.setIdDroit(1);
		d.setDroitProfils(setDroitProfil);
		d.setDroitDroitsAgent(setDroitDroitsAgent);

		dpOpe.setDroit(d);
		dda.setDroit(d);

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.isUserOperateur(idAgent)).thenReturn(true);
		Mockito.when(arRepo.getAgentDroitFetchAgents(idAgent)).thenReturn(d);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// When
		returnDto = service.verifAccessRightDemande(idAgent, 9005138, returnDto);

		// Then
		assertEquals(1, returnDto.getErrors().size());
		assertEquals(
				"Vous n'êtes ni opérateur,ni approbateur, ni viseur de l'agent 9005138. Vous ne pouvez pas saisir de demandes.",
				returnDto.getErrors().get(0));
	}

	@Test
	public void verifAccessRightSaveDemande_AllOk() {

		// Given
		ReturnMessageDto returnDto = new ReturnMessageDto();
		Integer idAgent = 9006543;

		Profil p = new Profil();
		p.setLibelle(ProfilEnum.OPERATEUR.toString());

		DroitProfil dpOpe = new DroitProfil();
		dpOpe.setDroitApprobateur(new Droit());
		dpOpe.setProfil(p);
		Set<DroitProfil> setDroitProfil = new HashSet<DroitProfil>();
		setDroitProfil.add(dpOpe);

		DroitsAgent da = new DroitsAgent();
		da.setIdAgent(9005138);

		DroitDroitsAgent dda = new DroitDroitsAgent();
		dda.setDroitProfil(dpOpe);
		dda.setDroitsAgent(da);
		Set<DroitDroitsAgent> setDroitDroitsAgent = new HashSet<DroitDroitsAgent>();
		setDroitDroitsAgent.add(dda);

		Droit d = new Droit();
		d.setIdAgent(idAgent);
		d.setIdDroit(1);
		d.setDroitProfils(setDroitProfil);
		d.setDroitDroitsAgent(setDroitDroitsAgent);

		dpOpe.setDroit(d);
		dda.setDroit(d);

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.isUserOperateur(idAgent)).thenReturn(true);
		Mockito.when(arRepo.getAgentDroitFetchAgents(idAgent)).thenReturn(d);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// When
		returnDto = service.verifAccessRightDemande(idAgent, 9005138, returnDto);

		// Then
		assertEquals(0, returnDto.getErrors().size());
	}

	@Test
	public void getIdApprobateurOfDelegataire_returnNull() {

		Integer idAgentConnecte = 9005138;
		Integer idAgentConcerne = 9005140;

		AccessRightsService service = new AccessRightsService();

		Integer result = service.getIdApprobateurOfDelegataire(idAgentConnecte, idAgentConcerne);

		assertNull(result);
	}

	@Test
	public void getIdApprobateurOfDelegataire_noDelegataire() {

		Integer idAgentConnecte = 9005138;
		Integer idAgentConcerne = null;

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isUserDelegataire(idAgentConnecte)).thenReturn(false);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);

		Integer result = service.getIdApprobateurOfDelegataire(idAgentConnecte, idAgentConcerne);

		assertNull(result);
	}

	@Test
	public void getIdApprobateurOfDelegataire_isDelegataire() {

		Integer idAgentConnecte = 9005138;
		Integer idAgentConcerne = null;

		Droit droitApprobateur = new Droit();
		droitApprobateur.setIdAgent(9001234);

		DroitProfil droitProfil = new DroitProfil();
		droitProfil.setDroitApprobateur(droitApprobateur);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isUserDelegataire(idAgentConnecte)).thenReturn(true);
		Mockito.when(
				accessRightsRepository.getDroitProfilByAgentAndLibelle(idAgentConnecte,
						ProfilEnum.DELEGATAIRE.toString())).thenReturn(droitProfil);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);

		Integer result = service.getIdApprobateurOfDelegataire(idAgentConnecte, idAgentConcerne);

		assertEquals(9001234, result.intValue());
	}
}
