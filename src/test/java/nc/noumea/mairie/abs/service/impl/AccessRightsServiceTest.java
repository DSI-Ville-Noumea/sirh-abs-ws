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

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.staticmock.MockStaticEntityMethods;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import nc.noumea.mairie.abs.domain.Droit;
import nc.noumea.mairie.abs.domain.DroitDroitsAgent;
import nc.noumea.mairie.abs.domain.DroitProfil;
import nc.noumea.mairie.abs.domain.DroitsAgent;
import nc.noumea.mairie.abs.domain.Profil;
import nc.noumea.mairie.abs.domain.ProfilEnum;
import nc.noumea.mairie.abs.dto.AccessRightsDto;
import nc.noumea.mairie.abs.dto.ActeursDto;
import nc.noumea.mairie.abs.dto.AgentDto;
import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.abs.dto.ApprobateurDto;
import nc.noumea.mairie.abs.dto.EntiteDto;
import nc.noumea.mairie.abs.dto.InputterDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.dto.ViseursDto;
import nc.noumea.mairie.abs.repository.AccessRightsRepository;
import nc.noumea.mairie.abs.repository.IAccessRightsRepository;
import nc.noumea.mairie.abs.service.IAgentService;
import nc.noumea.mairie.ws.IAdsWSConsumer;
import nc.noumea.mairie.ws.ISirhWSConsumer;

@MockStaticEntityMethods
public class AccessRightsServiceTest {

	@Autowired
	AccessRightsRepository repository;

	@PersistenceContext(unitName = "absPersistenceUnit")
	private EntityManager absEntityManager;
	
	private Profil profilApprobateur;
	private Profil profilOperateur;
	
	@Before
	public void init() {
		profilApprobateur = new Profil();
		profilApprobateur.setLibelle(ProfilEnum.APPROBATEUR.toString());
		
		profilOperateur = new Profil();
		profilOperateur.setLibelle(ProfilEnum.OPERATEUR.toString());
	}

	@Test
	public void getAgentAccessRights_AgentHasNoRights_ReturnFalseEverywhere() {

		// Given
		Integer idAgent = 906543;
		Droit droits = new Droit();

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentAccessRights(idAgent)).thenReturn(droits);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgentDirection(Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(new EntiteDto());

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
		assertFalse(result.isSaisieGarde());
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
		Mockito.when(sirhWSConsumer.getAgentDirection(Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(new EntiteDto());

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
		assertFalse(result.isSaisieGarde());
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
		Mockito.when(sirhWSConsumer.getAgentDirection(Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(new EntiteDto());

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
		assertFalse(result.isSaisieGarde());
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

		AgentWithServiceDto agentWithServiceDto = new AgentWithServiceDto();
		agentWithServiceDto.setSigleDirection("DPM");
		agentWithServiceDto.setIdAgent(droitsAgent.getIdAgent());

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		List<AgentWithServiceDto> listAgentsExistants = new ArrayList<AgentWithServiceDto>();
		listAgentsExistants.add(agentWithServiceDto);
		Mockito.when(sirhWSConsumer.getListAgentsWithService(Arrays.asList(droitsAgent.getIdAgent()), null, false)).thenReturn(listAgentsExistants);

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
		assertTrue(result.isSaisieGarde());
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

		EntiteDto serviceDto = new EntiteDto();
		serviceDto.setSigle("DPM");

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgentDirection(Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(serviceDto);

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
		assertFalse(result.isSaisieGarde());
	}

	@Test
	public void setAgentsApprobateurs_nonExisting() {
		// Given
		AgentWithServiceDto agentDto = new AgentWithServiceDto();
		agentDto.setIdAgent(9005138);

		final Date d = new Date();

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getDroitByProfilAndAgent("APPROBATEUR", 9005138)).thenReturn(null);
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
		ReturnMessageDto res = service.setApprobateur(agentDto,9005138);

		// Then
		Mockito.verify(arRepo, Mockito.times(1)).persisEntity(Mockito.isA(Droit.class));
		assertEquals(0, res.getErrors().size());
	}

	@Test
	public void setAgentsApprobateurs_1inList_nonExisting() {
		// Given
		AgentWithServiceDto agentDto = new AgentWithServiceDto();
		agentDto.setIdAgent(9005138);

		final Date d = new Date();

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getDroitByProfilAndAgent("APPROBATEUR", 9005138)).thenReturn(null);
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
		ReturnMessageDto res = service.setApprobateur(agentDto,9005138);

		// Then
		Mockito.verify(arRepo, Mockito.times(1)).persisEntity(Mockito.isA(Droit.class));
		assertEquals(0, res.getErrors().size());
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
		Mockito.when(arRepo.getDroitByProfilAndAgent("APPROBATEUR", 9005138)).thenReturn(d);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// When
		ReturnMessageDto res = service.setApprobateur(agentDto,9005138);

		// Then
		Mockito.verify(arRepo, Mockito.never()).persisEntity(Mockito.isA(Droit.class));
		assertEquals(0, res.getErrors().size());
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
		Mockito.when(arRepo.getDroitByProfilAndAgent("APPROBATEUR", 9005138)).thenReturn(null);
		Mockito.when(arRepo.isUserOperateur(9005138)).thenReturn(true);

		HelperService helpServ = Mockito.mock(HelperService.class);
		Mockito.when(helpServ.getCurrentDate()).thenReturn(new Date());

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "helperService", helpServ);

		// When
		ReturnMessageDto res = service.setApprobateur(agentDto,9005138);

		// Then
		Mockito.verify(arRepo, Mockito.times(1)).persisEntity(Mockito.isA(Droit.class));
		assertEquals(0, res.getErrors().size());
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
		Mockito.when(arRepo.getDroitByProfilAndAgent("APPROBATEUR", 9005138)).thenReturn(d);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).removeEntity(Mockito.any(DroitDroitsAgent.class));

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// When
		ReturnMessageDto res = service.deleteApprobateur(new AgentWithServiceDto(),9005138);

		// Then
		Mockito.verify(arRepo, Mockito.never()).persisEntity(Mockito.isA(Droit.class));
		assertEquals(1, res.getErrors().size());
		assertEquals("L'agent null n'est pas approbateur.", res.getErrors().get(0));
	}

	@Test
	public void setAgentsApprobateurs_Suppression() {
		// Given
		AgentWithServiceDto agentDto = new AgentWithServiceDto();
		agentDto.setIdAgent(9005131);

		Profil p = new Profil();
		p.setLibelle("APPROBATEUR");

		Droit d = new Droit();
		d.setIdAgent(9005138);

		DroitProfil dp = new DroitProfil();
		dp.setIdDroitProfil(1);
		dp.setDroit(d);
		dp.setProfil(p);
		dp.setDroitApprobateur(d);
		d.getDroitProfils().add(dp);

		Date date = new Date();

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getDroitByProfilAndAgent("APPROBATEUR", 9005131)).thenReturn(d);
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
		ReturnMessageDto res = service.deleteApprobateur(agentDto,9005138);

		// Then
		Mockito.verify(arRepo, Mockito.times(1)).removeEntity(Mockito.isA(Droit.class));
		assertEquals(0, res.getErrors().size());
	}

	@Test
	public void getApprobateurs_ReturnEmptyDto() {

		// Given
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentsApprobateurs()).thenReturn(new ArrayList<Droit>());

		Date currentDate = new DateTime(2013, 4, 9, 12, 9, 34).toDate();

		HelperService hS = Mockito.mock(HelperService.class);
		Mockito.when(hS.getCurrentDate()).thenReturn(currentDate);

		ISirhWSConsumer wsMock = Mockito.mock(ISirhWSConsumer.class);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "helperService", hS);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);

		// When
		List<ApprobateurDto> result = service.getApprobateurs(null, null);

		// Then
		assertEquals(0, result.size());
	}

	@Test
	public void getApprobateurs_ReturnEmptyDtoBis() {

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
		agDto1.setIdServiceADS(1);
		agDto1.setService("DIRECTION");
		agDto1.setStatut("F");

		Date currentDate = new DateTime(2013, 4, 9, 12, 9, 34).toDate();

		HelperService hS = Mockito.mock(HelperService.class);
		Mockito.when(hS.getCurrentDate()).thenReturn(currentDate);

		ISirhWSConsumer wsMock = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(wsMock.getAgentService(9005138, currentDate)).thenReturn(agDto1);
		Mockito.when(wsMock.getAgentService(9003041, currentDate)).thenReturn(null);

		List<AgentWithServiceDto> listAgentsServiceDto = new ArrayList<AgentWithServiceDto>();
		listAgentsServiceDto.add(agDto1);

		Mockito.when(wsMock.getListAgentsWithService(Arrays.asList(9005138, 9003041), currentDate, false)).thenReturn(listAgentsServiceDto);

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentsApprobateurs()).thenReturn(listeDroits);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);
		ReflectionTestUtils.setField(service, "helperService", hS);

		// When
		List<ApprobateurDto> dto = service.getApprobateurs(null, null);

		// Then
		assertEquals(1, dto.size());
		assertEquals(1, (int) dto.get(0).getApprobateur().getIdServiceADS());
		assertEquals("DIRECTION", dto.get(0).getApprobateur().getService());
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
		agDto1.setIdServiceADS(1);
		agDto1.setStatut("F");
		AgentWithServiceDto agDto2 = new AgentWithServiceDto();
		agDto2.setIdAgent(9003041);
		agDto2.setNom("TITO");
		agDto2.setService("service2");
		agDto2.setIdServiceADS(2);
		agDto2.setStatut("C");

		Date currentDate = new DateTime(2013, 4, 9, 12, 9, 34).toDate();

		HelperService hS = Mockito.mock(HelperService.class);
		Mockito.when(hS.getCurrentDate()).thenReturn(currentDate);

		ISirhWSConsumer wsMock = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(wsMock.getAgentService(9005138, currentDate)).thenReturn(agDto1);
		Mockito.when(wsMock.getAgentService(9003041, currentDate)).thenReturn(agDto2);

		List<AgentWithServiceDto> listAgentsServiceDto = new ArrayList<AgentWithServiceDto>();
		listAgentsServiceDto.add(agDto1);
		listAgentsServiceDto.add(agDto2);

		Mockito.when(wsMock.getListAgentsWithService(Arrays.asList(9005138, 9003041), currentDate, false)).thenReturn(listAgentsServiceDto);

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentsApprobateurs()).thenReturn(listeDroits);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);
		ReflectionTestUtils.setField(service, "helperService", hS);

		// When
		List<ApprobateurDto> dto = service.getApprobateurs(null, null);

		// Then
		assertEquals(2, dto.size());
		assertEquals(2, (int) dto.get(0).getApprobateur().getIdServiceADS());
		assertEquals("service2", dto.get(0).getApprobateur().getService());
		assertEquals(1, (int) dto.get(1).getApprobateur().getIdServiceADS());
		assertEquals("service", dto.get(1).getApprobateur().getService());
		assertEquals("F", dto.get(1).getApprobateur().getStatut());
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

		Profil profilOperateur = new Profil();
		profilOperateur.setLibelle("OPERATEUR");
		DroitProfil dp = new DroitProfil();
		dp.setIdDroitProfil(1);
		dp.setProfil(profilOperateur);

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getListOfAgentsToInputOrApprove(idAgent, idAgent)).thenReturn(new ArrayList<DroitsAgent>());
		Mockito.when(arRepo.getDroitProfilByAgent(idAgent, idAgent)).thenReturn(Arrays.asList(dp));

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// When
		List<AgentDto> result = service.getAgentsToApproveOrInputByAgent(idAgent, idAgent, null);

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

		Profil profilOperateur = new Profil();
		profilOperateur.setLibelle("APPROBATEUR");

		DroitProfil dp = new DroitProfil();
		dp.setIdDroitProfil(1);
		dp.setProfil(profilOperateur);

		DroitsAgent da1 = new DroitsAgent();
		da1.setIdAgent(1);
		DroitsAgent da2 = new DroitsAgent();
		da2.setIdAgent(2);

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getDroitProfilByAgent(idAgent, idAgent)).thenReturn(Arrays.asList(dp));
		Mockito.when(arRepo.getListOfAgentsToInputOrApprove(idAgent, dp.getIdDroitProfil())).thenReturn(Arrays.asList(da1, da2));

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(1)).thenReturn(a1);
		Mockito.when(sirhWSConsumer.getAgent(2)).thenReturn(a2);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		// When
		List<AgentDto> result = service.getAgentsToApproveOrInputByAgent(idAgent, idAgent, null);

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
		ReturnMessageDto msgDto = service.setInputter(9005138, dto,9005138);

		// Then
		assertEquals(2, msgDto.getErrors().size());
		assertTrue(msgDto.getErrors().get(0).contains("n'existe pas"));
		assertTrue(msgDto.getErrors().get(1).contains("n'existe pas"));
	}

	@Test
	public void setInputter_delegataireOperateurCanBe() {
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

		HelperService helpServ = Mockito.mock(HelperService.class);
		Mockito.when(helpServ.getCurrentDate()).thenReturn(new Date());

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", helpServ);

		// When
		ReturnMessageDto msgDto = service.setInputter(9005138, dto,9005138);

		// Then
		assertEquals(0, msgDto.getErrors().size());
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
		ReturnMessageDto msgDto = service.setInputter(9005138, dto,9005138);

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

		Profil profil = new Profil();
		profil.setLibelle(ProfilEnum.DELEGATAIRE.toString());
		droitProfilDelegataireExistant.setProfil(profil);

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

		Mockito.when(arRepo.isUserOperateurOfApprobateur(idAgent, droitOperateurExistant.getIdAgent())).thenReturn(true);
		Mockito.when(arRepo.isUserDelegataireOfApprobateur(idAgent, droitDelegataireExistant.getIdAgent())).thenReturn(true);

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
		ReturnMessageDto msgDto = service.setInputter(9005138, dto,9005138);

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

		Profil profilOperateur = new Profil();
		profilOperateur.setLibelle(ProfilEnum.OPERATEUR.toString());

		Profil profilDelegataire = new Profil();
		profilDelegataire.setLibelle(ProfilEnum.DELEGATAIRE.toString());

		List<Droit> listDroitSousAgentsByApprobateur = new ArrayList<Droit>();

		// operateur existant a supprimer
		DroitProfil droitProfilOperateurExistant = new DroitProfil();
		droitProfilOperateurExistant.setDroitApprobateur(droitApprobateur);
		droitProfilOperateurExistant.setProfil(profilOperateur);
		Set<DroitProfil> droitProfilOperateurExistants = new HashSet<DroitProfil>();
		droitProfilOperateurExistants.add(droitProfilOperateurExistant);

		Droit droitOperateurExistant = new Droit();
		droitOperateurExistant.setIdAgent(1);
		droitProfilOperateurExistant.setDroit(droitOperateurExistant);
		droitOperateurExistant.setDroitProfils(droitProfilOperateurExistants);

		// delegataire a supprimer
		DroitProfil droitProfilDelegataireExistant = new DroitProfil();
		droitProfilDelegataireExistant.setDroitApprobateur(droitApprobateur);
		droitProfilDelegataireExistant.setProfil(profilDelegataire);
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
		Mockito.when(arRepo.isUserOperateurOfApprobateur(idAgent, droitOperateurExistant.getIdAgent())).thenReturn(true);
		Mockito.when(arRepo.isUserDelegataireOfApprobateur(idAgent, droitDelegataireExistant.getIdAgent())).thenReturn(true);

		Mockito.when(arRepo.getProfilByName(ProfilEnum.DELEGATAIRE.toString())).thenReturn(new Profil());
		Mockito.when(arRepo.getProfilByName(ProfilEnum.OPERATEUR.toString())).thenReturn(new Profil());
		Mockito.when(arRepo.getProfilByName(ProfilEnum.VISEUR.toString())).thenReturn(new Profil());


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
		ReflectionTestUtils.setField(service, "helperService", helpServ);

		// When
		ReturnMessageDto msgDto = service.setInputter(9005138, dto,9005138);

		// Then
		Mockito.verify(arRepo, Mockito.times(0)).persisEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(2)).removeEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(2)).removeEntity(Mockito.isA(DroitProfil.class));
		assertEquals(0, msgDto.getErrors().size());
	}

	// #15711
	@Test
	public void setInputter_3OperateursExistants_1ASupprimer() {
		// Given
		Integer idAgent = 9005138;

		// approbateur
		Droit droitApprobateur = new Droit();
		droitApprobateur.setIdAgent(idAgent);
		droitApprobateur.setIdDroit(13);

		AgentDto operateurAGarder = new AgentDto();
		operateurAGarder.setIdAgent(9005131);

		InputterDto dto = new InputterDto();
		dto.setDelegataire(null);
		dto.setOperateurs(Arrays.asList(operateurAGarder));

		Profil profilOperateur = new Profil();
		profilOperateur.setLibelle(ProfilEnum.OPERATEUR.toString());

		List<Droit> listDroitSousAgentsByApprobateur = new ArrayList<Droit>();

		// operateur existant : le second a garder, le 1er a supprimer
		// 1er
		Droit droitOperateurExistant = Mockito.spy(new Droit());

		DroitProfil droitProfilOperateurExistant = new DroitProfil();
		droitProfilOperateurExistant.setDroitApprobateur(droitApprobateur);
		droitProfilOperateurExistant.setProfil(profilOperateur);
		droitProfilOperateurExistant.setDroit(droitOperateurExistant);

		Set<DroitProfil> droitProfilOperateurExistants = new HashSet<DroitProfil>();
		droitProfilOperateurExistants.add(droitProfilOperateurExistant);

		droitOperateurExistant.setIdAgent(9005131);
		droitProfilOperateurExistant.setDroit(droitOperateurExistant);
		droitOperateurExistant.setDroitProfils(droitProfilOperateurExistants);

		// 2e
		Droit droitOperateurExistant2 = Mockito.spy(new Droit());

		DroitProfil droitProfilOperateurExistant2 = new DroitProfil();
		droitProfilOperateurExistant2.setDroitApprobateur(droitApprobateur);
		droitProfilOperateurExistant2.setProfil(profilOperateur);
		droitProfilOperateurExistant2.setDroit(droitOperateurExistant2);

		Set<DroitProfil> droitProfilOperateurExistants2 = new HashSet<DroitProfil>();
		droitProfilOperateurExistants2.add(droitProfilOperateurExistant2);

		droitOperateurExistant2.setIdAgent(9006124);
		droitProfilOperateurExistant2.setDroit(droitOperateurExistant2);
		droitOperateurExistant2.setDroitProfils(droitProfilOperateurExistants2);

		listDroitSousAgentsByApprobateur.add(droitOperateurExistant2);
		listDroitSousAgentsByApprobateur.add(droitOperateurExistant);

		// mock
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentAccessRights(idAgent)).thenReturn(droitApprobateur);
		Mockito.when(arRepo.getDroitSousApprobateur(idAgent)).thenReturn(listDroitSousAgentsByApprobateur);
		Mockito.when(arRepo.isUserOperateurOfApprobateur(idAgent, droitOperateurExistant.getIdAgent())).thenReturn(true);
		Mockito.when(arRepo.isUserOperateurOfApprobateur(idAgent, droitOperateurExistant2.getIdAgent())).thenReturn(true);

		Mockito.when(arRepo.getProfilByName(ProfilEnum.DELEGATAIRE.toString())).thenReturn(new Profil());
		Mockito.when(arRepo.getProfilByName(ProfilEnum.OPERATEUR.toString())).thenReturn(new Profil());
		Mockito.when(arRepo.getProfilByName(ProfilEnum.VISEUR.toString())).thenReturn(new Profil());


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

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(droitOperateurExistant.getIdAgent())).thenReturn(new AgentGeneriqueDto());
		Mockito.when(sirhWSConsumer.getAgent(droitOperateurExistant2.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "helperService", helpServ);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		// When
		ReturnMessageDto msgDto = service.setInputter(9005138, dto,9005138);

		// Then
		Mockito.verify(arRepo, Mockito.never()).persisEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(1)).removeEntity(droitOperateurExistant2);
		Mockito.verify(arRepo, Mockito.times(1)).removeEntity(Mockito.isA(DroitProfil.class));
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
		Profil profilOperateur = new Profil();
		profilOperateur.setLibelle(ProfilEnum.OPERATEUR.toString());

		Profil profilDelegataire = new Profil();
		profilDelegataire.setLibelle(ProfilEnum.DELEGATAIRE.toString());

		List<Droit> listDroitSousAgentsByApprobateur = new ArrayList<Droit>();

		// operateur deja existant
		DroitProfil droitProfilOperateurExistant = new DroitProfil();
		droitProfilOperateurExistant.setDroitApprobateur(droitApprobateur);
		droitProfilOperateurExistant.setProfil(profilOperateur);
		Set<DroitProfil> droitProfilOperateurExistants = new HashSet<DroitProfil>();
		droitProfilOperateurExistants.add(droitProfilOperateurExistant);

		Droit droitOperateurExistant = new Droit();
		droitOperateurExistant.setIdAgent(9001235);
		droitProfilOperateurExistant.setDroit(droitOperateurExistant);
		droitOperateurExistant.setDroitProfils(droitProfilOperateurExistants);

		// delegataire deja existant
		DroitProfil droitProfilDelegataireExistant = new DroitProfil();
		droitProfilDelegataireExistant.setDroitApprobateur(droitApprobateur);
		droitProfilDelegataireExistant.setProfil(profilDelegataire);
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
		droitProfilOperateurASupprimer.setProfil(profilOperateur);
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

		Mockito.when(arRepo.isUserOperateurOfApprobateur(idAgent, droitOperateurExistant.getIdAgent())).thenReturn(true);
		Mockito.when(arRepo.isUserDelegataireOfApprobateur(idAgent, droitDelegataireExistant.getIdAgent())).thenReturn(true);

		Mockito.when(arRepo.isUserOperateurOfApprobateur(idAgent, droitOperateurASupprimer.getIdAgent())).thenReturn(true);

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
		ReturnMessageDto msgDto = service.setInputter(9005138, dto,9005138);

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

		Profil profil = new Profil();
		profil.setLibelle(ProfilEnum.DELEGATAIRE.toString());
		droitProfilDelegataireExistant.setProfil(profil);

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
		Mockito.when(arRepo.isUserDelegataireOfApprobateur(idAgent, droitDelegataireExistant.getIdAgent())).thenReturn(true);

		Mockito.when(arRepo.isUserOperateur(delegataire.getIdAgent())).thenReturn(false);

		Mockito.when(arRepo.getProfilByName(ProfilEnum.DELEGATAIRE.toString())).thenReturn(new Profil());


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
		ReflectionTestUtils.setField(service, "helperService", helpServ);

		// /////////// When //////////////
		ReturnMessageDto msgDto = service.setInputter(9005138, dto,9005138);

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

		Profil profil = new Profil();
		profil.setLibelle(ProfilEnum.DELEGATAIRE.toString());
		droitProfilDelegataireExistant.setProfil(profil);

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
		Mockito.when(arRepo.isUserDelegataireOfApprobateur(idAgent, droitDelegataireExistant.getIdAgent())).thenReturn(true);

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
		ReturnMessageDto msgDto = service.setInputter(9005138, dto,9005138);

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

		Profil profil = new Profil();
		profil.setLibelle(ProfilEnum.OPERATEUR.toString());

		List<Droit> listDroitSousAgentsByApprobateur = new ArrayList<Droit>();

		// operateur existant a supprimer avec 2 DroitProfil de 2 approbateurs
		DroitProfil droitProfiltOperateurExistant = new DroitProfil();
		droitProfiltOperateurExistant.setDroitApprobateur(droitApprobateur);
		droitProfiltOperateurExistant.setProfil(profil);

		DroitProfil droitProfiltOperateurExistant2 = new DroitProfil();
		droitProfiltOperateurExistant2.setDroitApprobateur(droitApprobateur2);
		droitProfiltOperateurExistant2.setProfil(profil);

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
		Mockito.when(arRepo.isUserOperateurOfApprobateur(idAgent, droitOperateurExistant.getIdAgent())).thenReturn(true);

		Mockito.when(arRepo.getProfilByName(ProfilEnum.OPERATEUR.toString())).thenReturn(new Profil());


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
		ReflectionTestUtils.setField(service, "helperService", helpServ);

		// When
		ReturnMessageDto msgDto = service.setInputter(9005138, dto,9005138);

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

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentDroitFetchAgents(idAgentApprobateur)).thenReturn(droitApprobateur);
		Mockito.when(arRepo.getDroitProfilByAgent(idAgentApprobateur, idAgentOperateurOrViseur)).thenReturn(new ArrayList<DroitProfil>());

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// /////////// WHEN /////////////
		ReturnMessageDto msgDto = service.setAgentsToInput(idAgentApprobateur, idAgentOperateurOrViseur, agents, ProfilEnum.OPERATEUR,9005138);

		// Then
		assertEquals(1, msgDto.getErrors().size());
		assertEquals("Impossible de modifier la liste des agents saisis de l'opérateur ou du viseur [9005139] car il n'est pas un opérateur ou viseur de l'agent [9005138].", msgDto.getErrors().get(0));
	}

	@Test
	public void setAgentsToInput_NoOperateurNoViseur() {

		Integer idAgentApprobateur = 9005138;
		Integer idAgentOperateurOrViseur = 9005139;
		List<AgentDto> agents = new ArrayList<AgentDto>();

		Droit droitApprobateur = new Droit();
		Droit droitOperateurOrViseur = new Droit();

		Profil profilOperateur = new Profil();
		profilOperateur.setLibelle("OPERATEUR");

		DroitProfil droitProfilOperateurOrViseur = new DroitProfil();
		droitProfilOperateurOrViseur.setProfil(profilOperateur);

		List<Droit> droitSousAgentsByApprobateur = new ArrayList<Droit>();
		droitSousAgentsByApprobateur.add(droitOperateurOrViseur);

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentDroitFetchAgents(idAgentApprobateur)).thenReturn(droitApprobateur);
		Mockito.when(arRepo.getDroitProfilByAgent(idAgentApprobateur, idAgentOperateurOrViseur)).thenReturn(Arrays.asList(droitProfilOperateurOrViseur));

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// /////////// WHEN /////////////
		ReturnMessageDto msgDto = service.setAgentsToInput(idAgentApprobateur, idAgentOperateurOrViseur, agents, ProfilEnum.VISEUR,9005138);

		// Then
		assertEquals(1, msgDto.getErrors().size());
		assertEquals("Impossible de modifier la liste des agents saisis de l'opérateur ou du viseur [9005139] car il n'est pas un opérateur ou viseur de l'agent [9005138].", msgDto.getErrors().get(0));
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

		Profil profilApprobateur = new Profil();
		profilApprobateur.setLibelle("APPROBATEUR");

		DroitProfil droitProfilApprobateur = new DroitProfil();
		droitProfilApprobateur.setProfil(profilApprobateur);

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

		droitProfilApprobateur.setDroitDroitsAgent(droitDroitsAgentAppro);

		Set<DroitProfil> droitProfilsAppro = new HashSet<DroitProfil>();
		droitProfilsAppro.add(droitProfilApprobateur);

		Droit droitApprobateur = new Droit();
		droitApprobateur.setDroitProfils(droitProfilsAppro);

		// /////////////////////////////////////////////////////
		Droit droitOperateurOrViseur = new Droit();
		List<Droit> droitSousAgentsByApprobateur = new ArrayList<Droit>();
		droitSousAgentsByApprobateur.add(droitOperateurOrViseur);

		Profil profilOperateur = new Profil();
		profilOperateur.setLibelle("OPERATEUR");

		DroitProfil droitProfilOperateurOrViseur = new DroitProfil();
		droitProfilOperateurOrViseur.setProfil(profilOperateur);

		// ////////////// Mockito /////////////////////////
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getDroitProfilByAgent(idAgentApprobateur, idAgentApprobateur)).thenReturn(Arrays.asList(droitProfilApprobateur));
		Mockito.when(arRepo.getDroitProfilByAgent(idAgentApprobateur, idAgentOperateurOrViseur)).thenReturn(Arrays.asList(droitProfilOperateurOrViseur));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).persisEntity(Mockito.any(DroitDroitsAgent.class));

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// /////////// WHEN /////////////
		ReturnMessageDto msgDto = service.setAgentsToInput(idAgentApprobateur, idAgentOperateurOrViseur, agents, ProfilEnum.OPERATEUR,9005138);

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

		Profil profilOperateur = new Profil();
		profilOperateur.setLibelle("OPERATEUR");

		DroitProfil droitProfilOperateurOrViseur = new DroitProfil();
		droitProfilOperateurOrViseur.setDroitDroitsAgent(droitDroitsAgent);
		droitProfilOperateurOrViseur.setProfil(profilOperateur);

		// ////////////// Mockito /////////////////////////
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentDroitFetchAgents(idAgentApprobateur)).thenReturn(droitApprobateur);
		Mockito.when(arRepo.getDroitProfilByAgent(idAgentApprobateur, idAgentOperateurOrViseur)).thenReturn(Arrays.asList(droitProfilOperateurOrViseur));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).removeEntity(Mockito.any(DroitDroitsAgent.class));

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// /////////// WHEN /////////////
		ReturnMessageDto msgDto = service.setAgentsToInput(idAgentApprobateur, idAgentOperateurOrViseur, agents, ProfilEnum.OPERATEUR,9005138);

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

		Profil profilApprobateur = new Profil();
		profilApprobateur.setLibelle("APPROBATEUR");

		DroitProfil droitProfilApprobateur = new DroitProfil();
		droitProfilApprobateur.setProfil(profilApprobateur);

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

		droitProfilApprobateur.setDroitDroitsAgent(droitDroitsAgentAppro);

		Set<DroitProfil> droitProfilsAppro = new HashSet<DroitProfil>();
		droitProfilsAppro.add(droitProfilApprobateur);

		Droit droitApprobateur = new Droit();
		droitApprobateur.setDroitProfils(droitProfilsAppro);

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

		Profil profilOperateur = new Profil();
		profilOperateur.setLibelle("OPERATEUR");

		DroitProfil droitProfilOperateurOrViseur = new DroitProfil();
		droitProfilOperateurOrViseur.setDroitDroitsAgent(droitDroitsAgentSuppr);
		droitProfilOperateurOrViseur.setProfil(profilOperateur);

		// ////////////// Mockito /////////////////////////
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getDroitProfilByAgent(idAgentApprobateur, idAgentApprobateur)).thenReturn(Arrays.asList(droitProfilApprobateur));
		Mockito.when(arRepo.getDroitProfilByAgent(idAgentApprobateur, idAgentOperateurOrViseur)).thenReturn(Arrays.asList(droitProfilOperateurOrViseur));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).persisEntity(Mockito.any(DroitDroitsAgent.class));

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// /////////// WHEN /////////////
		ReturnMessageDto msgDto = service.setAgentsToInput(idAgentApprobateur, idAgentOperateurOrViseur, agents, ProfilEnum.OPERATEUR,9005138);

		// //////////// THEN ///////////////
		Mockito.verify(arRepo, Mockito.times(2)).persisEntity(Mockito.isA(DroitDroitsAgent.class));
		Mockito.verify(arRepo, Mockito.times(1)).removeEntity(Mockito.isA(DroitDroitsAgent.class));
		assertEquals(0, msgDto.getErrors().size());
	}

	// #15688 bug : cumul de rôles sous un meme approbateur
	@Test
	@Transactional("absTransactionManager")
	public void setAgentsToInput_add_and_Remove_MultiRoles() {

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

		Profil profilApprobateur = new Profil();
		profilApprobateur.setLibelle("APPROBATEUR");

		DroitProfil droitProfilApprobateur = new DroitProfil();
		droitProfilApprobateur.setProfil(profilApprobateur);

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
		droitProfilApprobateur.setDroitDroitsAgent(droitDroitsAgentAppro);

		Set<DroitProfil> droitProfilsApprobateur = new HashSet<DroitProfil>();
		Droit droitApprobateur = new Droit();
		droitApprobateur.setDroitProfils(droitProfilsApprobateur);

		// //////////// 1 agent a supprimer ///////////////

		DroitDroitsAgent dda3Suppr = new DroitDroitsAgent();
		DroitsAgent droitsAgentSuppr3 = new DroitsAgent();
		droitsAgentSuppr3.setIdAgent(3);
		dda3Suppr.setDroitsAgent(droitsAgentSuppr3);

		Set<DroitDroitsAgent> droitDroitsAgentSuppr = new HashSet<DroitDroitsAgent>();
		droitDroitsAgentSuppr.add(dda3Suppr);
		Droit droitOperateurOrViseur = new Droit();
		droitOperateurOrViseur.setDroitDroitsAgent(droitDroitsAgentSuppr);

		// ////////////// Profil de l operateur + delegataire ////////////
		List<Droit> droitSousAgentsByApprobateur = new ArrayList<Droit>();
		droitSousAgentsByApprobateur.add(droitOperateurOrViseur);

		Profil profilOperateur = new Profil();
		profilOperateur.setLibelle("OPERATEUR");

		DroitProfil droitProfilOperateurOrViseur = new DroitProfil();
		droitProfilOperateurOrViseur.setDroitDroitsAgent(droitDroitsAgentSuppr);
		droitProfilOperateurOrViseur.setProfil(profilOperateur);

		Profil profilDelegataire = new Profil();
		profilDelegataire.setLibelle("DELEGATAIRE");

		DroitProfil droitProfilDelegataire = new DroitProfil();
		droitProfilDelegataire.setProfil(profilDelegataire);
		droitProfilDelegataire.setDroitApprobateur(droitApprobateur);

		// ////////////// Mockito /////////////////////////
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getDroitProfilByAgent(idAgentApprobateur, idAgentApprobateur)).thenReturn(Arrays.asList(droitProfilApprobateur));
		Mockito.when(arRepo.getDroitProfilByAgent(idAgentApprobateur, idAgentOperateurOrViseur)).thenReturn(Arrays.asList(droitProfilDelegataire, droitProfilOperateurOrViseur));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).persisEntity(Mockito.any(DroitDroitsAgent.class));

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// /////////// WHEN /////////////
		ReturnMessageDto msgDto = service.setAgentsToInput(idAgentApprobateur, idAgentOperateurOrViseur, agents, ProfilEnum.OPERATEUR,9005138);

		// //////////// THEN ///////////////
		Mockito.verify(arRepo, Mockito.times(2)).persisEntity(Mockito.isA(DroitDroitsAgent.class));
		Mockito.verify(arRepo, Mockito.times(1)).removeEntity(Mockito.isA(DroitDroitsAgent.class));
		assertEquals(0, msgDto.getErrors().size());
	}

	// #15688 bug : cumul de rôles sous un meme approbateur
	@Test
	@Transactional("absTransactionManager")
	public void setAgentsToInput_MultiRoles_badRole() {

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

		// ////////////// Profil de l operateur + delegataire ////////////
		List<Droit> droitSousAgentsByApprobateur = new ArrayList<Droit>();
		droitSousAgentsByApprobateur.add(droitOperateurOrViseur);

		Profil profilOperateur = new Profil();
		profilOperateur.setLibelle("VISEUR");

		DroitProfil droitProfilOperateurOrViseur = new DroitProfil();
		droitProfilOperateurOrViseur.setDroitDroitsAgent(droitDroitsAgentSuppr);
		droitProfilOperateurOrViseur.setProfil(profilOperateur);

		Profil profilDelegataire = new Profil();
		profilDelegataire.setLibelle("DELEGATAIRE");

		DroitProfil droitProfilDelegataire = new DroitProfil();
		droitProfilDelegataire.setProfil(profilOperateur);
		droitProfilDelegataire.setDroitApprobateur(droitApprobateur);

		// ////////////// Mockito /////////////////////////
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentDroitFetchAgents(idAgentApprobateur)).thenReturn(droitApprobateur);
		Mockito.when(arRepo.getDroitProfilByAgent(idAgentApprobateur, idAgentOperateurOrViseur)).thenReturn(Arrays.asList(droitProfilDelegataire, droitProfilOperateurOrViseur));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).persisEntity(Mockito.any(DroitDroitsAgent.class));

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// /////////// WHEN /////////////
		ReturnMessageDto msgDto = service.setAgentsToInput(idAgentApprobateur, idAgentOperateurOrViseur, agents, ProfilEnum.OPERATEUR,9005138);

		// //////////// THEN ///////////////
		Mockito.verify(arRepo, Mockito.never()).persisEntity(Mockito.isA(DroitDroitsAgent.class));
		Mockito.verify(arRepo, Mockito.never()).removeEntity(Mockito.isA(DroitDroitsAgent.class));
		assertEquals(1, msgDto.getErrors().size());
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

		Profil profilOperateur = new Profil();
		profilOperateur.setLibelle("OPERATEUR");

		DroitProfil droitProfilOperateurOrViseur = new DroitProfil();
		droitProfilOperateurOrViseur.setDroitDroitsAgent(droitDroitsAgentExisting);
		droitProfilOperateurOrViseur.setProfil(profilOperateur);

		// ////////////// Mockito /////////////////////////
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentDroitFetchAgents(idAgentApprobateur)).thenReturn(droitApprobateur);
		Mockito.when(arRepo.getDroitProfilByAgent(idAgentApprobateur, idAgentOperateurOrViseur)).thenReturn(Arrays.asList(droitProfilOperateurOrViseur));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).persisEntity(Mockito.any(DroitDroitsAgent.class));

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// /////////// WHEN /////////////
		ReturnMessageDto msgDto = service.setAgentsToInput(idAgentApprobateur, idAgentOperateurOrViseur, agents, ProfilEnum.OPERATEUR,9005138);

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

		Profil profilApprobateur = new Profil();
		profilApprobateur.setLibelle("APPROBATEUR");

		DroitProfil droitProfilApprobateur = new DroitProfil();
		droitProfilApprobateur.setProfil(profilApprobateur);

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

		Set<DroitProfil> droitProfilsApprobateur = new HashSet<DroitProfil>();
		droitProfilsApprobateur.add(droitProfilApprobateur);

		Droit droitApprobateur = new Droit();
		droitApprobateur.setDroitProfils(droitProfilsApprobateur);

		// /////////////////////////////////////////////////////
		Droit droitOperateurOrViseur = new Droit();
		List<Droit> droitSousAgentsByApprobateur = new ArrayList<Droit>();
		droitSousAgentsByApprobateur.add(droitOperateurOrViseur);

		Profil profilOperateur = new Profil();
		profilOperateur.setLibelle("OPERATEUR");

		DroitProfil droitProfilOperateurOrViseur = new DroitProfil();
		droitProfilOperateurOrViseur.setProfil(profilOperateur);

		// ////////////// Mockito /////////////////////////
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getDroitSousApprobateur(idAgentApprobateur)).thenReturn(droitSousAgentsByApprobateur);
		Mockito.when(arRepo.getDroitProfilByAgent(idAgentApprobateur, idAgentOperateurOrViseur)).thenReturn(Arrays.asList(droitProfilOperateurOrViseur));
		Mockito.when(arRepo.getDroitProfilByAgent(idAgentApprobateur, idAgentApprobateur)).thenReturn(Arrays.asList(droitProfilApprobateur));

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
		ReturnMessageDto msgDto = service.setAgentsToInput(idAgentApprobateur, idAgentOperateurOrViseur, agents, ProfilEnum.OPERATEUR,9005138);

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

		AgentGeneriqueDto agDto = new AgentGeneriqueDto();
		agDto.setIdAgent(9008765);

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
		Mockito.when(wsMock.getAgent(9008765)).thenReturn(agDto);

		HelperService hS = Mockito.mock(HelperService.class);
		Mockito.when(hS.getCurrentDate()).thenReturn(currentDate);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);
		ReflectionTestUtils.setField(service, "helperService", hS);

		// /////////// WHEN /////////////
		ReturnMessageDto msgDto = service.setAgentsToApprove(idAgentApprobateur, agsDto,9005138);

		// //////////// THEN ///////////////
		Mockito.verify(arRepo, Mockito.times(1)).persisEntity(Mockito.isA(DroitsAgent.class));
		assertEquals(0, msgDto.getErrors().size());
	}

	// bug #16013
	@Test
	@Transactional("absTransactionManager")
	public void setAgentsToApprove_cumulRole_addAgent() {

		Integer idAgentApprobateur = 9005138;

		// //////// agents a creer //////////////
		AgentDto ag = new AgentDto();
		ag.setIdAgent(9008765);
		List<AgentDto> agsDto = Arrays.asList(ag);

		// //////////// agents de l approbateur ///////////////
		DroitDroitsAgent dda = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(9008765);
		dda.setDroitsAgent(droitsAgent);

		Set<DroitDroitsAgent> droitDroitsAgentAppro = new HashSet<DroitDroitsAgent>();
		droitDroitsAgentAppro.add(dda);

		// APPROBATEUR
		Profil prAPPROBATEUR = new Profil();
		prAPPROBATEUR.setLibelle("APPROBATEUR");

		DroitProfil dprAPPROBATEUR = new DroitProfil();
		dprAPPROBATEUR.setProfil(prAPPROBATEUR);

		// OPERATEUR
		Profil prOPERATEUR = new Profil();
		prOPERATEUR.setLibelle("APPROBATEUR");

		DroitProfil dprOPERATEUR = new DroitProfil();
		dprOPERATEUR.setProfil(prOPERATEUR);
		dprOPERATEUR.setDroitDroitsAgent(droitDroitsAgentAppro);

		dda.setDroitProfil(dprOPERATEUR);

		Droit da = new Droit();
		da.setIdAgent(idAgentApprobateur);
		da.getDroitProfils().add(dprOPERATEUR);
		da.getDroitProfils().add(dprAPPROBATEUR);
		da.setDroitDroitsAgent(droitDroitsAgentAppro);

		Date currentDate = new DateTime(2013, 4, 9, 12, 9, 34).toDate();

		AgentGeneriqueDto agDto = new AgentGeneriqueDto();
		agDto.setIdAgent(9008765);

		// ////////////// Mockito /////////////////////////
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentAccessRights(idAgentApprobateur)).thenReturn(da);
		Mockito.when(arRepo.getDroitsAgent(1)).thenReturn(new DroitsAgent());
		Mockito.when(arRepo.getDroitProfilApprobateur(idAgentApprobateur)).thenReturn(dprAPPROBATEUR);

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).persisEntity(Mockito.any(DroitsAgent.class));

		ISirhWSConsumer wsMock = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(wsMock.getAgent(9008765)).thenReturn(agDto);

		HelperService hS = Mockito.mock(HelperService.class);
		Mockito.when(hS.getCurrentDate()).thenReturn(currentDate);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);
		ReflectionTestUtils.setField(service, "helperService", hS);

		// /////////// WHEN /////////////
		ReturnMessageDto msgDto = service.setAgentsToApprove(idAgentApprobateur, agsDto,9005138);

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
		agDto.setIdServiceADS(1);

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
		ReturnMessageDto msgDto = service.setAgentsToApprove(idAgentApprobateur, agsDto,9005138);

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
		dpr.setDroitDroitsAgent(droitDroitsAgentAppro);

		Droit da = new Droit();
		da.setIdAgent(idAgentApprobateur);
		da.getDroitProfils().add(dpr);
		da.setDroitDroitsAgent(droitDroitsAgentAppro);

		Date currentDate = new DateTime(2013, 4, 9, 12, 9, 34).toDate();

		AgentGeneriqueDto agDto = new AgentGeneriqueDto();
		agDto.setIdAgent(9008765);

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
		Mockito.when(wsMock.getAgent(9008765)).thenReturn(agDto);

		HelperService hS = Mockito.mock(HelperService.class);
		Mockito.when(hS.getCurrentDate()).thenReturn(currentDate);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);
		ReflectionTestUtils.setField(service, "helperService", hS);

		// /////////// WHEN /////////////
		ReturnMessageDto msgDto = service.setAgentsToApprove(idAgentApprobateur, agsDto,9005138);

		// //////////// THEN ///////////////
		Mockito.verify(arRepo, Mockito.times(0)).persisEntity(Mockito.isA(DroitDroitsAgent.class));
		Mockito.verify(arRepo, Mockito.times(1)).removeEntity(Mockito.isA(DroitDroitsAgent.class));
		assertEquals(0, msgDto.getErrors().size());
	}

	@Test
	@Transactional("absTransactionManager")
	public void setAgentsToApprove_removeAgent_removeAgentFromOperateur_butnotViseur() {

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
		dpr.setDroitDroitsAgent(droitDroitsAgentAppro);

		Droit da = new Droit();
		da.setIdDroit(11);
		da.setIdAgent(idAgentApprobateur);
		da.getDroitProfils().add(dpr);
		da.setDroitDroitsAgent(droitDroitsAgentAppro);
		dpr.setDroit(da);

		Date currentDate = new DateTime(2013, 4, 9, 12, 9, 34).toDate();

		AgentGeneriqueDto agDto = new AgentGeneriqueDto();
		agDto.setIdAgent(9008765);

		// ////////////// Mockito /////////////////////////
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentAccessRights(idAgentApprobateur)).thenReturn(da);
		Mockito.when(arRepo.getDroitsAgent(1)).thenReturn(new DroitsAgent());
		Mockito.when(arRepo.getDroitProfilApprobateur(idAgentApprobateur)).thenReturn(dpr);

		List<Droit> droitSousAgentsByApprobateur = new ArrayList<Droit>();

		Droit droitOperateur = new Droit();
		droitOperateur.setIdAgent(9008888);
		DroitProfil droitProfilOperateur = new DroitProfil();
		droitProfilOperateur.setDroitApprobateur(da);
		droitProfilOperateur.setDroit(droitOperateur);
		Profil profilOperateur = new Profil();
		profilOperateur.setLibelle(ProfilEnum.OPERATEUR.toString());
		droitProfilOperateur.setProfil(profilOperateur);
		droitOperateur.getDroitProfils().add(droitProfilOperateur);
		DroitDroitsAgent ddaOperateur = new DroitDroitsAgent();
		ddaOperateur.setDroitsAgent(droitsAgent);
		droitProfilOperateur.getDroitDroitsAgent().add(ddaOperateur);

		Droit droitViseur = new Droit();
		droitViseur.setIdAgent(9007777);
		DroitProfil droitProfilViseur = new DroitProfil();
		droitProfilViseur.setDroitApprobateur(da);
		droitProfilViseur.setDroit(droitViseur);
		Profil profilViseur = new Profil();
		profilViseur.setLibelle(ProfilEnum.VISEUR.toString());
		droitProfilViseur.setProfil(profilViseur);
		droitViseur.getDroitProfils().add(droitProfilViseur);
		DroitDroitsAgent ddaViseur = new DroitDroitsAgent();
		DroitsAgent droitsAgentFictif = new DroitsAgent();
		droitsAgentFictif.setIdAgent(2);
		ddaViseur.setDroitsAgent(droitsAgentFictif);
		droitProfilViseur.getDroitDroitsAgent().add(ddaViseur);

		droitSousAgentsByApprobateur.add(droitOperateur);
		droitSousAgentsByApprobateur.add(droitViseur);

		Mockito.when(arRepo.getDroitSousApprobateur(idAgentApprobateur)).thenReturn(droitSousAgentsByApprobateur);
		Mockito.when(arRepo.isUserOperateurOfApprobateur(idAgentApprobateur, droitOperateur.getIdAgent())).thenReturn(true);
		Mockito.when(arRepo.isUserOperateurOfApprobateur(idAgentApprobateur, droitViseur.getIdAgent())).thenReturn(false);
		Mockito.when(arRepo.isUserViseurOfApprobateur(idAgentApprobateur, droitOperateur.getIdAgent())).thenReturn(false);
		Mockito.when(arRepo.isUserViseurOfApprobateur(idAgentApprobateur, droitViseur.getIdAgent())).thenReturn(true);

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).removeEntity(Mockito.any(DroitDroitsAgent.class));

		ISirhWSConsumer wsMock = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(wsMock.getAgent(9008765)).thenReturn(agDto);

		HelperService hS = Mockito.mock(HelperService.class);
		Mockito.when(hS.getCurrentDate()).thenReturn(currentDate);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);
		ReflectionTestUtils.setField(service, "helperService", hS);

		// /////////// WHEN /////////////
		ReturnMessageDto msgDto = service.setAgentsToApprove(idAgentApprobateur, agsDto,9005138);

		// //////////// THEN ///////////////
		Mockito.verify(arRepo, Mockito.times(0)).persisEntity(Mockito.isA(DroitDroitsAgent.class));
		Mockito.verify(arRepo, Mockito.times(2)).removeEntity(Mockito.isA(DroitDroitsAgent.class));
		Mockito.verify(arRepo, Mockito.times(2)).removeEntity(Mockito.isA(DroitsAgent.class));
		assertEquals(0, msgDto.getErrors().size());
	}

	@Test
	@Transactional("absTransactionManager")
	public void setAgentsToApprove_removeAgent_removeAgentFromOperateurAndViseur() {

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
		dpr.setDroitDroitsAgent(droitDroitsAgentAppro);

		Droit da = new Droit();
		da.setIdDroit(11);
		da.setIdAgent(idAgentApprobateur);
		da.getDroitProfils().add(dpr);
		da.setDroitDroitsAgent(droitDroitsAgentAppro);
		dpr.setDroit(da);

		Date currentDate = new DateTime(2013, 4, 9, 12, 9, 34).toDate();

		AgentGeneriqueDto agDto = new AgentGeneriqueDto();
		agDto.setIdAgent(9008765);

		// ////////////// Mockito /////////////////////////
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentAccessRights(idAgentApprobateur)).thenReturn(da);
		Mockito.when(arRepo.getDroitsAgent(1)).thenReturn(new DroitsAgent());
		Mockito.when(arRepo.getDroitProfilApprobateur(idAgentApprobateur)).thenReturn(dpr);

		List<Droit> droitSousAgentsByApprobateur = new ArrayList<Droit>();

		Droit droitOperateur = new Droit();
		droitOperateur.setIdAgent(9008888);
		DroitProfil droitProfilOperateur = new DroitProfil();
		droitProfilOperateur.setDroitApprobateur(da);
		droitProfilOperateur.setDroit(droitOperateur);
		Profil profilOperateur = new Profil();
		profilOperateur.setLibelle(ProfilEnum.OPERATEUR.toString());
		droitProfilOperateur.setProfil(profilOperateur);
		droitOperateur.getDroitProfils().add(droitProfilOperateur);
		DroitDroitsAgent ddaOperateur = new DroitDroitsAgent();
		ddaOperateur.setDroitsAgent(droitsAgent);
		droitProfilOperateur.getDroitDroitsAgent().add(ddaOperateur);

		Droit droitViseur = new Droit();
		droitViseur.setIdAgent(9007777);
		DroitProfil droitProfilViseur = new DroitProfil();
		droitProfilViseur.setDroitApprobateur(da);
		droitProfilViseur.setDroit(droitViseur);
		Profil profilViseur = new Profil();
		profilViseur.setLibelle(ProfilEnum.VISEUR.toString());
		droitProfilViseur.setProfil(profilViseur);
		droitViseur.getDroitProfils().add(droitProfilViseur);
		DroitDroitsAgent ddaViseur = new DroitDroitsAgent();
		ddaViseur.setDroitsAgent(droitsAgent);
		droitProfilViseur.getDroitDroitsAgent().add(ddaViseur);

		droitSousAgentsByApprobateur.add(droitOperateur);
		droitSousAgentsByApprobateur.add(droitViseur);

		Mockito.when(arRepo.getDroitSousApprobateur(idAgentApprobateur)).thenReturn(droitSousAgentsByApprobateur);
		Mockito.when(arRepo.isUserOperateurOfApprobateur(idAgentApprobateur, droitOperateur.getIdAgent())).thenReturn(true);
		Mockito.when(arRepo.isUserOperateurOfApprobateur(idAgentApprobateur, droitViseur.getIdAgent())).thenReturn(false);
		Mockito.when(arRepo.isUserViseurOfApprobateur(idAgentApprobateur, droitOperateur.getIdAgent())).thenReturn(false);
		Mockito.when(arRepo.isUserViseurOfApprobateur(idAgentApprobateur, droitViseur.getIdAgent())).thenReturn(true);

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(arRepo).removeEntity(Mockito.any(DroitDroitsAgent.class));

		ISirhWSConsumer wsMock = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(wsMock.getAgent(9008765)).thenReturn(agDto);

		HelperService hS = Mockito.mock(HelperService.class);
		Mockito.when(hS.getCurrentDate()).thenReturn(currentDate);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);
		ReflectionTestUtils.setField(service, "helperService", hS);

		// /////////// WHEN /////////////
		ReturnMessageDto msgDto = service.setAgentsToApprove(idAgentApprobateur, agsDto,9005138);

		// //////////// THEN ///////////////
		Mockito.verify(arRepo, Mockito.times(0)).persisEntity(Mockito.isA(DroitDroitsAgent.class));
		Mockito.verify(arRepo, Mockito.times(3)).removeEntity(Mockito.isA(DroitDroitsAgent.class));
		Mockito.verify(arRepo, Mockito.times(3)).removeEntity(Mockito.isA(DroitsAgent.class));
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
		Mockito.when(arRepo.getDroitSousApprobateur(idAgentAppro)).thenReturn(Arrays.asList(droitViseur1, droitViseur2));
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
		ReturnMessageDto msgDto = service.setViseurs(9005138, dto,9005138);

		// Then
		assertEquals(1, msgDto.getErrors().size());
		assertTrue(msgDto.getErrors().get(0).contains("n'existe pas"));
	}

	@Test
	public void setViseurs_ViseurisOperateurAndApprobateur_CanBe() {
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

		HelperService helpServ = Mockito.mock(HelperService.class);
		Mockito.when(helpServ.getCurrentDate()).thenReturn(new Date());

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", helpServ);

		// When
		ReturnMessageDto msgDto = service.setViseurs(9005138, dto,9005138);

		// Then
		assertEquals(0, msgDto.getErrors().size());
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
		ReturnMessageDto msgDto = service.setViseurs(9005138, dto,9005138);

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
		ReturnMessageDto msgDto = service.setViseurs(9005138, dto,9005138);

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
		ReflectionTestUtils.setField(service, "helperService", helpServ);

		// When
		ReturnMessageDto msgDto = service.setViseurs(9005138, dto,9005138);

		// Then
		Mockito.verify(arRepo, Mockito.times(0)).persisEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(1)).removeEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(1)).removeEntity(Mockito.isA(DroitProfil.class));
		assertEquals(0, msgDto.getErrors().size());
	}

	// #15711
	@Test
	public void setViseurs_2ViseursExistants_1SeulASupprimer() {
		// Given
		Integer idAgent = 9005138;

		// approbateur
		Droit droitApprobateur = new Droit();
		droitApprobateur.setIdAgent(idAgent);
		droitApprobateur.setIdDroit(13);

		AgentDto viseursDto = new AgentDto();
		viseursDto.setIdAgent(9002990);

		ViseursDto dto = new ViseursDto();
		dto.setViseurs(Arrays.asList(viseursDto));

		List<Droit> listDroitSousAgentsByApprobateur = new ArrayList<Droit>();

		// viseur existant a supprimer
		DroitProfil droitProfilViseurExistant = new DroitProfil();
		droitProfilViseurExistant.setDroitApprobateur(droitApprobateur);

		Set<DroitProfil> droitProfilViseurExistants = new HashSet<DroitProfil>();
		droitProfilViseurExistants.add(droitProfilViseurExistant);

		Droit droitViseurExistant = Mockito.spy(new Droit());
		droitViseurExistant.setIdAgent(9006324);
		droitProfilViseurExistant.setDroit(droitViseurExistant);
		droitViseurExistant.setDroitProfils(droitProfilViseurExistants);

		listDroitSousAgentsByApprobateur.add(droitViseurExistant);

		// viseur existant a garder
		DroitProfil droitProfilViseurExistant2 = new DroitProfil();
		droitProfilViseurExistant2.setDroitApprobateur(droitApprobateur);

		Set<DroitProfil> droitProfilViseurExistants2 = new HashSet<DroitProfil>();
		droitProfilViseurExistants2.add(droitProfilViseurExistant2);

		Droit droitViseurExistant2 = Mockito.spy(new Droit());
		droitViseurExistant2.setIdAgent(9002990);
		droitProfilViseurExistant2.setDroit(droitViseurExistant2);
		droitViseurExistant2.setDroitProfils(droitProfilViseurExistants2);

		listDroitSousAgentsByApprobateur.add(droitViseurExistant2);

		// mock
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentAccessRights(idAgent)).thenReturn(droitApprobateur);
		Mockito.when(arRepo.getDroitSousApprobateur(idAgent)).thenReturn(listDroitSousAgentsByApprobateur);
		Mockito.when(arRepo.isUserViseurOfApprobateur(idAgent, droitViseurExistant.getIdAgent())).thenReturn(true);
		Mockito.when(arRepo.isUserViseurOfApprobateur(idAgent, droitViseurExistant2.getIdAgent())).thenReturn(true);

		Mockito.when(arRepo.getProfilByName(ProfilEnum.DELEGATAIRE.toString())).thenReturn(new Profil());
		Mockito.when(arRepo.getProfilByName(ProfilEnum.OPERATEUR.toString())).thenReturn(new Profil());
		Mockito.when(arRepo.getProfilByName(ProfilEnum.VISEUR.toString())).thenReturn(new Profil());


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
		ReflectionTestUtils.setField(service, "helperService", helpServ);

		// When
		ReturnMessageDto msgDto = service.setViseurs(9005138, dto,9005138);

		// Then
		Mockito.verify(arRepo, Mockito.never()).persisEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(1)).removeEntity(droitViseurExistant);
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
		ReturnMessageDto msgDto = service.setViseurs(9005138, dto,9005138);

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
		ReflectionTestUtils.setField(service, "helperService", helpServ);

		// When
		ReturnMessageDto msgDto = service.setViseurs(9005138, dto,9005138);

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
		Integer idServiceADS = 1;

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getListOfAgentsToInputOrApprove(idAgent)).thenReturn(new ArrayList<DroitsAgent>());

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		// When
		List<AgentDto> result = service.getAgentsToApproveOrInputByService(idAgent, idServiceADS);

		// Then
		assertEquals(0, result.size());
	}

	@Test
	public void getAgentsToApproveWithoutProfil_2Agents_ReturnListOf2() {

		// Given
		Integer idAgent = 9007654;
		Integer idServiceADS = null;

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
		Mockito.when(arRepo.getListOfAgentsToInputOrApprove(idAgent)).thenReturn(Arrays.asList(da1, da2));

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getListAgents(Arrays.asList(1, 2))).thenReturn(Arrays.asList(a1, a2));

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		// When
		List<AgentDto> result = service.getAgentsToApproveOrInputByService(idAgent, idServiceADS);

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
		Date dateJour = new Date();

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

		EntiteDto entiteDto2 = new EntiteDto();
		entiteDto2.setLabel("SERVICE 2");
		entiteDto2.setIdStatut(1);

		EntiteDto entiteDto = new EntiteDto();
		entiteDto.setLabel("SERVICE 1");
		entiteDto.setIdStatut(1);

		AgentWithServiceDto ag2 = new AgentWithServiceDto();
		ag2.setIdAgent(2);
		ag2.setIdServiceADS(2);
		AgentWithServiceDto ag1 = new AgentWithServiceDto();
		ag1.setIdAgent(1);
		ag1.setIdServiceADS(1);
		List<AgentWithServiceDto> listAg = new ArrayList<AgentWithServiceDto>();
		listAg.add(ag1);
		listAg.add(ag2);

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getListOfAgentsToInputOrApprove(idAgent)).thenReturn(Arrays.asList(da1, da2));

		IAdsWSConsumer adsWsConsumer = Mockito.mock(IAdsWSConsumer.class);
		Mockito.when(adsWsConsumer.getWholeTreeLight()).thenReturn(entiteDto);
		Mockito.when(adsWsConsumer.getEntiteByIdEntiteOptimiseWithWholeTree(ag1.getIdServiceADS(), entiteDto)).thenReturn(entiteDto);
		Mockito.when(adsWsConsumer.getEntiteByIdEntiteOptimiseWithWholeTree(ag2.getIdServiceADS(), entiteDto)).thenReturn(entiteDto2);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getListAgentsWithService(Arrays.asList(da1.getIdAgent(), da2.getIdAgent()), dateJour, true)).thenReturn(Arrays.asList(ag1,ag2));

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "adsWsConsumer", adsWsConsumer);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		// When
		List<EntiteDto> result = service.getAgentsServicesToApproveOrInput(idAgent, dateJour);

		// Then
		assertEquals(2, result.size());
		assertEquals("SERVICE 1", result.get(0).getLabel());
		assertEquals("SERVICE 2", result.get(1).getLabel());
	}

	@Test
	public void getAgentsServicesToApproveOrInput_OldAffectation_2agents_return2Dtos() {

		// Given
		Integer idAgent = 9007654;
		Date dateJour = new Date();

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

		EntiteDto entiteDto2 = new EntiteDto();
		entiteDto2.setLabel("SERVICE 2");
		entiteDto2.setIdStatut(1);

		EntiteDto entiteDto = new EntiteDto();
		entiteDto.setLabel("SERVICE 1");
		entiteDto.setIdStatut(1);

		AgentWithServiceDto ag2 = new AgentWithServiceDto();
		ag2.setIdAgent(2);
		ag2.setIdServiceADS(2);
		AgentWithServiceDto ag1 = new AgentWithServiceDto();
		ag1.setIdAgent(1);
		ag1.setIdServiceADS(1);
		List<AgentWithServiceDto> listAg = new ArrayList<AgentWithServiceDto>();
		listAg.add(ag1);
		listAg.add(ag2);

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getListOfAgentsToInputOrApprove(idAgent)).thenReturn(Arrays.asList(da1, da2));

		IAdsWSConsumer adsWsConsumer = Mockito.mock(IAdsWSConsumer.class);
		Mockito.when(adsWsConsumer.getWholeTreeLight()).thenReturn(entiteDto);
		Mockito.when(adsWsConsumer.getEntiteByIdEntiteOptimiseWithWholeTree(ag1.getIdServiceADS(), entiteDto)).thenReturn(entiteDto);
		Mockito.when(adsWsConsumer.getEntiteByIdEntiteOptimiseWithWholeTree(ag2.getIdServiceADS(), entiteDto)).thenReturn(entiteDto2);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getListAgentsWithService(Arrays.asList(da1.getIdAgent(),da2.getIdAgent()), dateJour, true)).thenReturn(Arrays.asList(ag1));
		Mockito.when(sirhWSConsumer.getListAgentsWithServiceOldAffectation(Arrays.asList(da2.getIdAgent()), true)).thenReturn(Arrays.asList(ag2));

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "adsWsConsumer", adsWsConsumer);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		// When
		List<EntiteDto> result = service.getAgentsServicesToApproveOrInput(idAgent, dateJour);

		// Then
		assertEquals(2, result.size());
		assertEquals("SERVICE 1", result.get(0).getLabel());
		assertEquals("SERVICE 2", result.get(1).getLabel());
	}

	@Test
	public void getAgentsServicesToApproveOrInput_2agentsSameService_return1Dtos() {

		// Given
		Integer idAgent = 9007654;
		Date dateJour = new Date();

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

		EntiteDto entiteDto = new EntiteDto();
		entiteDto.setLabel("TEST");
		entiteDto.setIdStatut(1);

		AgentWithServiceDto ag2 = new AgentWithServiceDto();
		ag2.setIdAgent(2);
		ag2.setIdServiceADS(1);
		AgentWithServiceDto ag1 = new AgentWithServiceDto();
		ag1.setIdAgent(1);
		ag1.setIdServiceADS(1);
		List<AgentWithServiceDto> listAg = new ArrayList<AgentWithServiceDto>();
		listAg.add(ag1);
		listAg.add(ag2);

		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.addAll(Arrays.asList(dda, dda2));

		d.setDroitDroitsAgent(droitDroitsAgent);

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getListOfAgentsToInputOrApprove(idAgent)).thenReturn(Arrays.asList(da1, da2));

		IAdsWSConsumer adsWsConsumer = Mockito.mock(IAdsWSConsumer.class);
		Mockito.when(adsWsConsumer.getWholeTreeLight()).thenReturn(entiteDto);
		Mockito.when(adsWsConsumer.getEntiteByIdEntiteOptimiseWithWholeTree(ag1.getIdServiceADS(), entiteDto)).thenReturn(entiteDto);
		Mockito.when(adsWsConsumer.getEntiteByIdEntiteOptimiseWithWholeTree(ag2.getIdServiceADS(), entiteDto)).thenReturn(entiteDto);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getListAgentsWithService(Arrays.asList(da1.getIdAgent(),da2.getIdAgent()), dateJour, true)).thenReturn(Arrays.asList(ag1,ag2));
		
		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "adsWsConsumer", adsWsConsumer);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		// When
		List<EntiteDto> result = service.getAgentsServicesToApproveOrInput(idAgent, dateJour);

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
		assertEquals("Vous n'êtes ni opérateur, ni approbateur, ni viseur. Vous ne pouvez pas saisir de demandes.", returnDto.getErrors().get(0));
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
		assertEquals("Vous n'êtes ni opérateur, ni approbateur, ni viseur de l'agent 9005138. Vous ne pouvez pas saisir de demandes.", returnDto.getErrors().get(0));
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

		List<Integer> result = service.getIdApprobateurOfDelegataire(idAgentConnecte, idAgentConcerne);

		assertEquals(0, result.size());
	}

	@Test
	public void getIdApprobateurOfDelegataire_noDelegataire() {

		Integer idAgentConnecte = 9005138;
		Integer idAgentConcerne = null;

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isUserDelegataire(idAgentConnecte)).thenReturn(false);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);

		List<Integer> result = service.getIdApprobateurOfDelegataire(idAgentConnecte, idAgentConcerne);

		assertEquals(0, result.size());
	}

	@Test
	public void getIdApprobateurOfDelegataire_isDelegataire() {

		Integer idAgentConnecte = 9005138;
		Integer idAgentConcerne = null;

		Droit droitApprobateur = new Droit();
		droitApprobateur.setIdAgent(9001234);

		List<DroitProfil> droitsProfils = new ArrayList<DroitProfil>();
		DroitProfil droitProfil = new DroitProfil();
		droitProfil.setDroitApprobateur(droitApprobateur);
		droitsProfils.add(droitProfil);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isUserDelegataire(idAgentConnecte)).thenReturn(true);
		Mockito.when(accessRightsRepository.getDroitProfilByAgentAndLibelle(idAgentConnecte, ProfilEnum.DELEGATAIRE.toString())).thenReturn(droitsProfils);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);

		List<Integer> result = service.getIdApprobateurOfDelegataire(idAgentConnecte, idAgentConcerne);

		assertEquals(9001234, result.get(0).intValue());
	}

	@Test
	public void setDelegataire_delegataireOperateurNoExisting() {
		// Given
		Integer idAgent = 9005138;
		ReturnMessageDto msgDto = new ReturnMessageDto();

		InputterDto dto = new InputterDto();

		AgentDto delegataire = new AgentDto();
		delegataire.setIdAgent(9001234);
		dto.setDelegataire(delegataire);

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
		msgDto = service.setDelegataire(9005138, dto, msgDto,9005138);

		// Then
		assertEquals(1, msgDto.getErrors().size());
		assertTrue(msgDto.getErrors().get(0).contains("n'existe pas"));
	}

	@Test
	public void setDelegataire_delegataireOperateurCanNotBe() {
		// Given
		Integer idAgent = 9005138;
		ReturnMessageDto msgDto = new ReturnMessageDto();

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

		HelperService helpServ = Mockito.mock(HelperService.class);
		Mockito.when(helpServ.getCurrentDate()).thenReturn(new Date());

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", helpServ);

		// When
		msgDto = service.setDelegataire(9005138, dto, msgDto,9005138);

		// Then
		assertEquals(0, msgDto.getErrors().size());
	}

	@Test
	public void setDelegataire_delegataireOperateurPersist() {
		// Given
		Integer idAgent = 9005138;
		ReturnMessageDto msgDto = new ReturnMessageDto();

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
		msgDto = service.setDelegataire(9005138, dto, msgDto,9005138);

		// Then
		Mockito.verify(arRepo, Mockito.times(1)).persisEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(0)).removeEntity(Mockito.isA(Droit.class));
		assertEquals(0, msgDto.getErrors().size());
	}

	@Test
	public void setDelegataire_delegataireOperateurPersist_AvecDelegataireOperateurDejaExistants() {
		// Given
		Integer idAgent = 9005138;
		ReturnMessageDto msgDto = new ReturnMessageDto();

		// /////////////////// OPERATEUR DELEGATAIRE A CREER
		// /////////////////
		InputterDto dto = new InputterDto();
		// 1 delegataire
		AgentDto delegataire = new AgentDto();
		delegataire.setIdAgent(9001234);
		dto.setDelegataire(delegataire);
		// 3 operateurs dont un deja existant

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

		Profil profil = new Profil();
		profil.setLibelle(ProfilEnum.DELEGATAIRE.toString());
		droitProfilDelegataireExistant.setProfil(profil);

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

		Mockito.when(arRepo.isUserOperateurOfApprobateur(idAgent, droitOperateurExistant.getIdAgent())).thenReturn(true);
		Mockito.when(arRepo.isUserDelegataireOfApprobateur(idAgent, droitDelegataireExistant.getIdAgent())).thenReturn(true);

		Mockito.when(arRepo.isUserOperateur(delegataire.getIdAgent())).thenReturn(false);
		Mockito.when(arRepo.getAgentAccessRights(delegataire.getIdAgent())).thenReturn(new Droit());
		Mockito.when(arRepo.getProfilByName(ProfilEnum.DELEGATAIRE.toString())).thenReturn(new Profil());

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(delegataire.getIdAgent())).thenReturn(new AgentGeneriqueDto());

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
		msgDto = service.setDelegataire(9005138, dto, msgDto,9005138);

		// Then
		assertEquals(0, msgDto.getErrors().size());
		Mockito.verify(arRepo, Mockito.times(0)).persisEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(0)).removeEntity(Mockito.isA(Droit.class));
	}

	@Test
	public void setDelegataire_delegataireOperateurRemove() {
		// Given
		Integer idAgent = 9005138;
		ReturnMessageDto msgDto = new ReturnMessageDto();

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

		Profil profil = new Profil();
		profil.setLibelle(ProfilEnum.DELEGATAIRE.toString());
		droitProfilDelegataireExistant.setProfil(profil);

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
		Mockito.when(arRepo.isUserOperateurOfApprobateur(idAgent, droitOperateurExistant.getIdAgent())).thenReturn(true);
		Mockito.when(arRepo.isUserDelegataireOfApprobateur(idAgent, droitDelegataireExistant.getIdAgent())).thenReturn(true);

		Mockito.when(arRepo.getProfilByName(ProfilEnum.DELEGATAIRE.toString())).thenReturn(new Profil());
		Mockito.when(arRepo.getProfilByName(ProfilEnum.OPERATEUR.toString())).thenReturn(new Profil());
		Mockito.when(arRepo.getProfilByName(ProfilEnum.VISEUR.toString())).thenReturn(new Profil());


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
		ReflectionTestUtils.setField(service, "helperService", helpServ);

		// When
		msgDto = service.setDelegataire(9005138, dto, msgDto,9005138);

		// Then
		Mockito.verify(arRepo, Mockito.times(0)).persisEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(1)).removeEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(1)).removeEntity(Mockito.isA(DroitProfil.class));
		assertEquals(0, msgDto.getErrors().size());
	}

	@Test
	public void setDelegataire_delegataireOperateurAnd_PersistAndRemove_AndAgentDejaExistant() {
		// Given
		Integer idAgent = 9005138;
		ReturnMessageDto msgDto = new ReturnMessageDto();

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

		Profil profil = new Profil();
		profil.setLibelle(ProfilEnum.DELEGATAIRE.toString());
		droitProfilDelegataireExistant.setProfil(profil);

		Set<DroitProfil> droitProfilDelegataireExistants = new HashSet<DroitProfil>();
		droitProfilDelegataireExistants.add(droitProfilDelegataireExistant);

		Droit droitDelegataireExistant = new Droit();
		droitDelegataireExistant.setIdAgent(9001234);
		droitProfilDelegataireExistant.setDroit(droitDelegataireExistant);
		droitDelegataireExistant.setDroitProfils(droitProfilDelegataireExistants);

		listDroitSousAgentsByApprobateur.add(droitOperateurExistant);
		listDroitSousAgentsByApprobateur.add(droitDelegataireExistant);
		// ////////////////////////////////////////////////////

		// ////////////// mock //////////////////////
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentAccessRights(idAgent)).thenReturn(droitApprobateur);
		Mockito.when(arRepo.getDroitSousApprobateur(idAgent)).thenReturn(listDroitSousAgentsByApprobateur);

		Mockito.when(arRepo.isUserOperateurOfApprobateur(idAgent, droitOperateurExistant.getIdAgent())).thenReturn(true);
		Mockito.when(arRepo.isUserDelegataireOfApprobateur(idAgent, droitDelegataireExistant.getIdAgent())).thenReturn(true);

		Mockito.when(arRepo.isUserOperateur(delegataire.getIdAgent())).thenReturn(false);

		Mockito.when(arRepo.getProfilByName(ProfilEnum.DELEGATAIRE.toString())).thenReturn(new Profil());
		Mockito.when(arRepo.getProfilByName(ProfilEnum.OPERATEUR.toString())).thenReturn(new Profil());
		Mockito.when(arRepo.getProfilByName(ProfilEnum.VISEUR.toString())).thenReturn(new Profil());

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
		ReflectionTestUtils.setField(service, "helperService", helpServ);

		// /////////// When //////////////
		msgDto = service.setDelegataire(9005138, dto, msgDto,9005138);

		// ///////////// Then //////////////
		assertEquals(0, msgDto.getErrors().size());
		Mockito.verify(arRepo, Mockito.times(0)).persisEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(0)).removeEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(0)).removeEntity(Mockito.isA(DroitProfil.class));
	}

	@Test
	public void setDelegataire_delegataireDejaExistant() {
		// Given
		Integer idAgent = 9005138;
		ReturnMessageDto msgDto = new ReturnMessageDto();

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

		Profil profil = new Profil();
		profil.setLibelle(ProfilEnum.DELEGATAIRE.toString());
		droitProfilDelegataireExistant.setProfil(profil);

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
		Mockito.when(arRepo.isUserDelegataireOfApprobateur(idAgent, droitDelegataireExistant.getIdAgent())).thenReturn(true);

		Mockito.when(arRepo.isUserOperateur(delegataire.getIdAgent())).thenReturn(false);

		Mockito.when(arRepo.getProfilByName(ProfilEnum.DELEGATAIRE.toString())).thenReturn(new Profil());

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
		ReflectionTestUtils.setField(service, "helperService", helpServ);

		// /////////// When //////////////
		msgDto = service.setDelegataire(9005138, dto, msgDto,9005138);

		// ///////////// Then //////////////
		assertEquals(0, msgDto.getErrors().size());
		Mockito.verify(arRepo, Mockito.times(0)).persisEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(0)).removeEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(0)).removeEntity(Mockito.isA(DroitProfil.class));
	}

	// #17859 bug sur changement delegataire
	@Test
	public void setDelegataire_newDelagatire_EtAutreDelegataireDejaExistant_etProfilViseur_bug17859() {
		// Given
		Integer idAgent = 9005138;
		ReturnMessageDto msgDto = new ReturnMessageDto();

		// ///////////// CREATION //////////////////
		// approbateur
		Droit droitApprobateur = new Droit();
		droitApprobateur.setIdAgent(idAgent);
		droitApprobateur.setIdDroit(13);

		// delegataire a creer
		InputterDto dto = new InputterDto();
		AgentDto delegataire = new AgentDto();
		delegataire.setIdAgent(9004321);
		dto.setDelegataire(delegataire);
		// ////////////////////////////////////////////////////

		// /////////////// OPERATEUR VISEUR DELEGATAIRE DEJA EXISTANTS
		// /////////////////////
		List<Droit> listDroitSousAgentsByApprobateur = new ArrayList<Droit>();

		// delegataire deja existant
		DroitProfil droitProfilDelegataireExistant = new DroitProfil();
		droitProfilDelegataireExistant.setDroitApprobateur(droitApprobateur);

		Profil profilDelegataire = new Profil();
		profilDelegataire.setLibelle(ProfilEnum.DELEGATAIRE.toString());
		droitProfilDelegataireExistant.setProfil(profilDelegataire);

		// delegataire deja existant
		DroitProfil droitProfilViseurExistant = new DroitProfil();
		droitProfilViseurExistant.setDroitApprobateur(droitApprobateur);

		Profil profilViseur = new Profil();
		profilViseur.setLibelle(ProfilEnum.VISEUR.toString());
		droitProfilViseurExistant.setProfil(profilViseur);

		Set<DroitProfil> droitProfilDelegataireExistants = new HashSet<DroitProfil>();
		droitProfilDelegataireExistants.add(droitProfilViseurExistant);
		droitProfilDelegataireExistants.add(droitProfilDelegataireExistant);

		Droit droitDelegataireExistant = Mockito.spy(new Droit());
		droitDelegataireExistant.setIdAgent(9001234);
		droitProfilDelegataireExistant.setDroit(droitDelegataireExistant);
		droitProfilViseurExistant.setDroit(droitDelegataireExistant);
		droitDelegataireExistant.setDroitProfils(droitProfilDelegataireExistants);

		listDroitSousAgentsByApprobateur.add(droitDelegataireExistant);
		// ////////////////////////////////////////////////////

		// ////////////// mock //////////////////////
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentAccessRights(idAgent)).thenReturn(droitApprobateur);
		Mockito.when(arRepo.getDroitSousApprobateur(idAgent)).thenReturn(listDroitSousAgentsByApprobateur);
		Mockito.when(arRepo.isUserDelegataireOfApprobateur(idAgent, droitDelegataireExistant.getIdAgent())).thenReturn(true);

		Mockito.when(arRepo.isUserOperateur(delegataire.getIdAgent())).thenReturn(false);

		Mockito.when(arRepo.getProfilByName(ProfilEnum.DELEGATAIRE.toString())).thenReturn(new Profil());


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

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(delegataire.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "helperService", helpServ);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		// /////////// When //////////////
		msgDto = service.setDelegataire(9005138, dto, msgDto,9005138);

		// ///////////// Then //////////////
		assertEquals(0, msgDto.getErrors().size());
		Mockito.verify(arRepo, Mockito.times(1)).persisEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(0)).removeEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(1)).removeEntity(Mockito.isA(DroitProfil.class));
		assertEquals(droitDelegataireExistant.getDroitProfils().size(), 1);
		DroitProfil droitProfilDelegataireRestant = droitDelegataireExistant.getDroitProfils().iterator().next();
		assertEquals(droitProfilDelegataireRestant.getProfil().getLibelle(), ProfilEnum.VISEUR.toString());
	}

	@Test
	public void setDelegataire_delegataireDejaExistantASupprimer_And_delegataireACreer() {
		// Given
		Integer idAgent = 9005138;
		ReturnMessageDto msgDto = new ReturnMessageDto();

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

		Profil profil = new Profil();
		profil.setLibelle(ProfilEnum.DELEGATAIRE.toString());
		droitProfilDelegataireExistant.setProfil(profil);

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
		Mockito.when(arRepo.isUserDelegataireOfApprobateur(idAgent, droitDelegataireExistant.getIdAgent())).thenReturn(true);

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
		msgDto = service.setDelegataire(9005138, dto, msgDto,9005138);

		// ///////////// Then //////////////
		assertEquals(0, msgDto.getErrors().size());
		Mockito.verify(arRepo, Mockito.times(1)).persisEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(1)).removeEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(1)).removeEntity(Mockito.isA(DroitProfil.class));
	}

	@Test
	public void getListeActeurs_2Approbateurs1Viseurs2Operateurs() {

		Integer idAgent = 9005131;

		// l agent
		DroitsAgent da = new DroitsAgent();
		da.setIdAgent(idAgent);
		da.setDateModification(new Date());

		Profil pAppro = new Profil();
		pAppro.setLibelle("APPROBATEUR");

		Profil pViseur = new Profil();
		pViseur.setLibelle("VISEUR");

		Profil pOperateur = new Profil();
		pOperateur.setLibelle("OPERATEUR");
		// 1er approbateur
		Droit droitAppro = new Droit();
		droitAppro.setDateModification(new Date());
		droitAppro.setIdAgent(9005138);
		// 2e approbateur
		Droit droitAppro2 = new Droit();
		droitAppro2.setDateModification(new Date());
		droitAppro2.setIdAgent(9005148);
		// viseur
		Droit droitViseur = new Droit();
		droitViseur.setDateModification(new Date());
		droitViseur.setIdAgent(9005140);
		// 1er operateur
		Droit droitOperateur = new Droit();
		droitOperateur.setDateModification(new Date());
		droitOperateur.setIdAgent(9005142);
		// 2e operateur
		Droit droitOperateur2 = new Droit();
		droitOperateur2.setDateModification(new Date());
		droitOperateur2.setIdAgent(9005152);

		DroitProfil dpAppr = new DroitProfil();
		dpAppr.setDroit(droitAppro);
		dpAppr.setDroitApprobateur(droitAppro);
		dpAppr.setProfil(pAppro);

		DroitProfil dpAppr2 = new DroitProfil();
		dpAppr2.setDroit(droitAppro2);
		dpAppr2.setDroitApprobateur(droitAppro2);
		dpAppr2.setProfil(pAppro);

		DroitProfil dpViseur = new DroitProfil();
		dpViseur.setDroit(droitViseur);
		dpViseur.setDroitApprobateur(droitAppro);
		dpViseur.setProfil(pViseur);

		DroitProfil dpOperateur = new DroitProfil();
		dpOperateur.setDroit(droitOperateur);
		dpOperateur.setDroitApprobateur(droitAppro);
		dpOperateur.setProfil(pOperateur);

		DroitProfil dpOperateur2 = new DroitProfil();
		dpOperateur2.setDroit(droitOperateur2);
		dpOperateur2.setDroitApprobateur(droitAppro2);
		dpOperateur2.setProfil(pOperateur);

		DroitDroitsAgent ddaAppro = new DroitDroitsAgent();
		ddaAppro.setDroit(droitAppro);
		ddaAppro.setDroitProfil(dpAppr);
		ddaAppro.setDroitsAgent(da);

		DroitDroitsAgent ddaAppro2 = new DroitDroitsAgent();
		ddaAppro2.setDroit(droitAppro2);
		ddaAppro2.setDroitProfil(dpAppr2);
		ddaAppro2.setDroitsAgent(da);

		DroitDroitsAgent ddaViseur = new DroitDroitsAgent();
		ddaViseur.setDroit(droitViseur);
		ddaViseur.setDroitProfil(dpViseur);
		ddaViseur.setDroitsAgent(da);

		DroitDroitsAgent ddaOperateur = new DroitDroitsAgent();
		ddaOperateur.setDroit(droitOperateur);
		ddaOperateur.setDroitProfil(dpOperateur);
		ddaOperateur.setDroitsAgent(da);

		DroitDroitsAgent ddaOperateur2 = new DroitDroitsAgent();
		ddaOperateur2.setDroit(droitOperateur2);
		ddaOperateur2.setDroitProfil(dpOperateur2);
		ddaOperateur2.setDroitsAgent(da);

		da.getDroitDroitsAgent().add(ddaAppro);
		dpAppr.getDroitDroitsAgent().add(ddaAppro);
		droitAppro.getDroitDroitsAgent().add(ddaAppro);
		droitAppro.getDroitProfils().add(dpAppr);

		da.getDroitDroitsAgent().add(ddaAppro2);
		dpAppr2.getDroitDroitsAgent().add(ddaAppro2);
		droitAppro2.getDroitDroitsAgent().add(ddaAppro2);
		droitAppro2.getDroitProfils().add(dpAppr2);

		da.getDroitDroitsAgent().add(ddaViseur);
		dpViseur.getDroitDroitsAgent().add(ddaViseur);
		droitViseur.getDroitDroitsAgent().add(ddaViseur);
		droitViseur.getDroitProfils().add(dpViseur);

		da.getDroitDroitsAgent().add(ddaOperateur);
		dpOperateur.getDroitDroitsAgent().add(ddaOperateur);
		droitOperateur.getDroitDroitsAgent().add(ddaOperateur);
		droitOperateur.getDroitProfils().add(dpOperateur);

		da.getDroitDroitsAgent().add(ddaOperateur2);
		dpOperateur2.getDroitDroitsAgent().add(ddaOperateur2);
		droitOperateur2.getDroitDroitsAgent().add(ddaOperateur2);
		droitOperateur2.getDroitProfils().add(dpOperateur2);

		List<DroitsAgent> listDroitsAgent = new ArrayList<DroitsAgent>();
		listDroitsAgent.add(da);

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getListeActeursOfAgent(idAgent)).thenReturn(listDroitsAgent);

		AgentGeneriqueDto approDto = new AgentGeneriqueDto();
		approDto.setIdAgent(droitAppro.getIdAgent());
		AgentGeneriqueDto approDto2 = new AgentGeneriqueDto();
		approDto2.setIdAgent(droitAppro2.getIdAgent());
		AgentGeneriqueDto viseurDto = new AgentGeneriqueDto();
		viseurDto.setIdAgent(droitViseur.getIdAgent());
		AgentGeneriqueDto operateurDto = new AgentGeneriqueDto();
		operateurDto.setIdAgent(droitOperateur.getIdAgent());
		AgentGeneriqueDto operateurDto2 = new AgentGeneriqueDto();
		operateurDto2.setIdAgent(droitOperateur2.getIdAgent());

		IAgentService agentService = Mockito.mock(IAgentService.class);
		Mockito.when(agentService.getAgentOptimise(new ArrayList<AgentGeneriqueDto>(), droitAppro.getIdAgent())).thenReturn(approDto);
		Mockito.when(agentService.getAgentOptimise(new ArrayList<AgentGeneriqueDto>(), droitAppro2.getIdAgent())).thenReturn(approDto2);
		Mockito.when(agentService.getAgentOptimise(new ArrayList<AgentGeneriqueDto>(), droitViseur.getIdAgent())).thenReturn(viseurDto);
		Mockito.when(agentService.getAgentOptimise(new ArrayList<AgentGeneriqueDto>(), droitOperateur.getIdAgent())).thenReturn(operateurDto);
		Mockito.when(agentService.getAgentOptimise(new ArrayList<AgentGeneriqueDto>(), droitOperateur2.getIdAgent())).thenReturn(operateurDto2);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "agentService", agentService);

		ActeursDto dto = service.getListeActeurs(idAgent);

		assertEquals(2, dto.getListApprobateurs().size());
		assertEquals(1, dto.getListViseurs().size());
		assertEquals(2, dto.getListOperateurs().size());
	}

	@Test
	public void getListeActeurs_2Approbateurs1Viseurs2Operateurs_enDouble() {

		Integer idAgent = 9005131;

		// l agent
		DroitsAgent da = new DroitsAgent();
		da.setIdAgent(idAgent);
		da.setDateModification(new Date());

		Profil pAppro = new Profil();
		pAppro.setLibelle("APPROBATEUR");

		Profil pViseur = new Profil();
		pViseur.setLibelle("VISEUR");

		Profil pOperateur = new Profil();
		pOperateur.setLibelle("OPERATEUR");
		// 1er approbateur
		Droit droitAppro = new Droit();
		droitAppro.setDateModification(new Date());
		droitAppro.setIdAgent(9005138);
		// 2e approbateur
		Droit droitAppro2 = new Droit();
		droitAppro2.setDateModification(new Date());
		droitAppro2.setIdAgent(9005148);
		// viseur
		Droit droitViseur = new Droit();
		droitViseur.setDateModification(new Date());
		droitViseur.setIdAgent(9005140);
		// 1er operateur
		Droit droitOperateur = new Droit();
		droitOperateur.setDateModification(new Date());
		droitOperateur.setIdAgent(9005142);
		// 2e operateur
		Droit droitOperateur2 = new Droit();
		droitOperateur2.setDateModification(new Date());
		droitOperateur2.setIdAgent(9005152);

		DroitProfil dpAppr = new DroitProfil();
		dpAppr.setDroit(droitAppro);
		dpAppr.setDroitApprobateur(droitAppro);
		dpAppr.setProfil(pAppro);

		DroitProfil dpAppr2 = new DroitProfil();
		dpAppr2.setDroit(droitAppro2);
		dpAppr2.setDroitApprobateur(droitAppro2);
		dpAppr2.setProfil(pAppro);

		DroitProfil dpViseur = new DroitProfil();
		dpViseur.setDroit(droitViseur);
		dpViseur.setDroitApprobateur(droitAppro);
		dpViseur.setProfil(pViseur);

		DroitProfil dpOperateur = new DroitProfil();
		dpOperateur.setDroit(droitOperateur);
		dpOperateur.setDroitApprobateur(droitAppro);
		dpOperateur.setProfil(pOperateur);

		DroitProfil dpOperateur2 = new DroitProfil();
		dpOperateur2.setDroit(droitOperateur2);
		dpOperateur2.setDroitApprobateur(droitAppro2);
		dpOperateur2.setProfil(pOperateur);

		DroitDroitsAgent ddaAppro = new DroitDroitsAgent();
		ddaAppro.setDroit(droitAppro);
		ddaAppro.setDroitProfil(dpAppr);
		ddaAppro.setDroitsAgent(da);

		DroitDroitsAgent ddaAppro2 = new DroitDroitsAgent();
		ddaAppro2.setDroit(droitAppro2);
		ddaAppro2.setDroitProfil(dpAppr2);
		ddaAppro2.setDroitsAgent(da);

		DroitDroitsAgent ddaViseur = new DroitDroitsAgent();
		ddaViseur.setDroit(droitViseur);
		ddaViseur.setDroitProfil(dpViseur);
		ddaViseur.setDroitsAgent(da);

		DroitDroitsAgent ddaOperateur = new DroitDroitsAgent();
		ddaOperateur.setDroit(droitOperateur);
		ddaOperateur.setDroitProfil(dpOperateur);
		ddaOperateur.setDroitsAgent(da);

		DroitDroitsAgent ddaOperateur2 = new DroitDroitsAgent();
		ddaOperateur2.setDroit(droitOperateur2);
		ddaOperateur2.setDroitProfil(dpOperateur2);
		ddaOperateur2.setDroitsAgent(da);

		da.getDroitDroitsAgent().add(ddaAppro);
		dpAppr.getDroitDroitsAgent().add(ddaAppro);
		droitAppro.getDroitDroitsAgent().add(ddaAppro);
		droitAppro.getDroitProfils().add(dpAppr);

		da.getDroitDroitsAgent().add(ddaAppro2);
		dpAppr2.getDroitDroitsAgent().add(ddaAppro2);
		droitAppro2.getDroitDroitsAgent().add(ddaAppro2);
		droitAppro2.getDroitProfils().add(dpAppr2);

		da.getDroitDroitsAgent().add(ddaViseur);
		dpViseur.getDroitDroitsAgent().add(ddaViseur);
		droitViseur.getDroitDroitsAgent().add(ddaViseur);
		droitViseur.getDroitProfils().add(dpViseur);

		da.getDroitDroitsAgent().add(ddaOperateur);
		dpOperateur.getDroitDroitsAgent().add(ddaOperateur);
		droitOperateur.getDroitDroitsAgent().add(ddaOperateur);
		droitOperateur.getDroitProfils().add(dpOperateur);

		da.getDroitDroitsAgent().add(ddaOperateur2);
		dpOperateur2.getDroitDroitsAgent().add(ddaOperateur2);
		droitOperateur2.getDroitDroitsAgent().add(ddaOperateur2);
		droitOperateur2.getDroitProfils().add(dpOperateur2);

		List<DroitsAgent> listDroitsAgent = new ArrayList<DroitsAgent>();
		listDroitsAgent.add(da);
		listDroitsAgent.add(da);

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getListeActeursOfAgent(idAgent)).thenReturn(listDroitsAgent);

		AgentGeneriqueDto approDto = new AgentGeneriqueDto();
		approDto.setIdAgent(droitAppro.getIdAgent());
		AgentGeneriqueDto approDto2 = new AgentGeneriqueDto();
		approDto2.setIdAgent(droitAppro2.getIdAgent());
		AgentGeneriqueDto viseurDto = new AgentGeneriqueDto();
		viseurDto.setIdAgent(droitViseur.getIdAgent());
		AgentGeneriqueDto operateurDto = new AgentGeneriqueDto();
		operateurDto.setIdAgent(droitOperateur.getIdAgent());
		AgentGeneriqueDto operateurDto2 = new AgentGeneriqueDto();
		operateurDto2.setIdAgent(droitOperateur2.getIdAgent());

		IAgentService agentService = Mockito.mock(IAgentService.class);
		Mockito.when(agentService.getAgentOptimise(new ArrayList<AgentGeneriqueDto>(), droitAppro.getIdAgent())).thenReturn(approDto);
		Mockito.when(agentService.getAgentOptimise(new ArrayList<AgentGeneriqueDto>(), droitAppro2.getIdAgent())).thenReturn(approDto2);
		Mockito.when(agentService.getAgentOptimise(new ArrayList<AgentGeneriqueDto>(), droitViseur.getIdAgent())).thenReturn(viseurDto);
		Mockito.when(agentService.getAgentOptimise(new ArrayList<AgentGeneriqueDto>(), droitOperateur.getIdAgent())).thenReturn(operateurDto);
		Mockito.when(agentService.getAgentOptimise(new ArrayList<AgentGeneriqueDto>(), droitOperateur2.getIdAgent())).thenReturn(operateurDto2);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "agentService", agentService);

		ActeursDto dto = service.getListeActeurs(idAgent);

		assertEquals(2, dto.getListApprobateurs().size());
		assertEquals(1, dto.getListViseurs().size());
		assertEquals(2, dto.getListOperateurs().size());
	}

	@Test
	public void setOperateur_dejaOperateur() {

		Integer idAgentAppro = 9002990;
		AgentDto operateurDto = new AgentDto();
		operateurDto.setIdAgent(9005138);

		Droit droitApprobateur = new Droit();

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isUserOperateurOfApprobateur(idAgentAppro, operateurDto.getIdAgent())).thenReturn(true);
		Mockito.when(accessRightsRepository.getAgentAccessRights(idAgentAppro)).thenReturn(droitApprobateur);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);

		ReturnMessageDto result = service.setOperateur(idAgentAppro, operateurDto,9005138);

		assertEquals(1, result.getErrors().size());
		assertEquals("L'agent [9005138] est déjà opérateur de l'approbateur [9002990].", result.getErrors().get(0).toString());
		Mockito.verify(accessRightsRepository, Mockito.never()).persisEntity(Mockito.isA(Droit.class));
		Mockito.verify(accessRightsRepository, Mockito.never()).persisEntity(Mockito.isA(DroitProfil.class));
		Mockito.verify(accessRightsRepository, Mockito.never()).removeEntity(Mockito.isA(Droit.class));
		Mockito.verify(accessRightsRepository, Mockito.never()).removeEntity(Mockito.isA(DroitProfil.class));
	}

	@Test
	public void setOperateur_agentInexistant() {

		Integer idAgentAppro = 9002990;
		AgentDto operateurDto = new AgentDto();
		operateurDto.setIdAgent(9005138);

		Droit droitApprobateur = new Droit();

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isUserOperateurOfApprobateur(idAgentAppro, operateurDto.getIdAgent())).thenReturn(false);
		Mockito.when(accessRightsRepository.getAgentAccessRights(idAgentAppro)).thenReturn(droitApprobateur);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(operateurDto.getIdAgent())).thenReturn(null);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		ReturnMessageDto result = service.setOperateur(idAgentAppro, operateurDto,9005138);

		assertEquals(1, result.getErrors().size());
		assertEquals("L'agent opérateur [9005138] n'existe pas.", result.getErrors().get(0).toString());
		Mockito.verify(accessRightsRepository, Mockito.never()).persisEntity(Mockito.isA(Droit.class));
		Mockito.verify(accessRightsRepository, Mockito.never()).persisEntity(Mockito.isA(DroitProfil.class));
		Mockito.verify(accessRightsRepository, Mockito.never()).removeEntity(Mockito.isA(Droit.class));
		Mockito.verify(accessRightsRepository, Mockito.never()).removeEntity(Mockito.isA(DroitProfil.class));
	}

	@Test
	public void setOperateur_creeOperateur_DroitExistant() {

		Integer idAgentAppro = 9002990;
		AgentDto operateurDto = new AgentDto();
		operateurDto.setIdAgent(9005138);

		Droit droitApprobateur = new Droit();
		Droit droitOperateur = Mockito.spy(new Droit());

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isUserOperateurOfApprobateur(idAgentAppro, operateurDto.getIdAgent())).thenReturn(false);
		Mockito.when(accessRightsRepository.getAgentAccessRights(idAgentAppro)).thenReturn(droitApprobateur);
		Mockito.when(accessRightsRepository.getAgentAccessRights(operateurDto.getIdAgent())).thenReturn(droitOperateur);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(operateurDto.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", helperService);

		ReturnMessageDto result = service.setOperateur(idAgentAppro, operateurDto,9005138);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(accessRightsRepository, Mockito.times(1)).persisEntity(droitOperateur);
		assertEquals(1, droitOperateur.getDroitProfils().size());
		Mockito.verify(accessRightsRepository, Mockito.never()).removeEntity(Mockito.isA(Droit.class));
		Mockito.verify(accessRightsRepository, Mockito.never()).removeEntity(Mockito.isA(DroitProfil.class));
	}

	@Test
	public void setOperateur_creeOperateur_DroitInexistant() {

		Integer idAgentAppro = 9002990;
		AgentDto operateurDto = new AgentDto();
		operateurDto.setIdAgent(9005138);

		Droit droitApprobateur = new Droit();
		Droit droitOperateur = Mockito.spy(new Droit());

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isUserOperateurOfApprobateur(idAgentAppro, operateurDto.getIdAgent())).thenReturn(false);
		Mockito.when(accessRightsRepository.getAgentAccessRights(idAgentAppro)).thenReturn(droitApprobateur);
		Mockito.when(accessRightsRepository.getAgentAccessRights(operateurDto.getIdAgent())).thenReturn(null);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(operateurDto.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", helperService);

		ReturnMessageDto result = service.setOperateur(idAgentAppro, operateurDto,9005138);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(accessRightsRepository, Mockito.never()).persisEntity(droitOperateur);
		Mockito.verify(accessRightsRepository, Mockito.times(1)).persisEntity(Mockito.isA(Droit.class));
		Mockito.verify(accessRightsRepository, Mockito.never()).removeEntity(Mockito.isA(Droit.class));
		Mockito.verify(accessRightsRepository, Mockito.never()).removeEntity(Mockito.isA(DroitProfil.class));
	}

	@Test
	public void setViseur_dejaViseur() {

		Integer idAgentAppro = 9002990;
		AgentDto viseurDto = new AgentDto();
		viseurDto.setIdAgent(9005138);

		Droit droitApprobateur = new Droit();

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isUserViseurOfApprobateur(idAgentAppro, viseurDto.getIdAgent())).thenReturn(true);
		Mockito.when(accessRightsRepository.getAgentAccessRights(idAgentAppro)).thenReturn(droitApprobateur);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);

		ReturnMessageDto result = service.setViseur(idAgentAppro, viseurDto,9005138);

		assertEquals(0, result.getErrors().size());
		assertEquals(1, result.getInfos().size());
		assertEquals("L'agent [9005138] est déjà viseur de l'approbateur [9002990].", result.getInfos().get(0).toString());
		Mockito.verify(accessRightsRepository, Mockito.never()).persisEntity(Mockito.isA(Droit.class));
		Mockito.verify(accessRightsRepository, Mockito.never()).persisEntity(Mockito.isA(DroitProfil.class));
		Mockito.verify(accessRightsRepository, Mockito.never()).removeEntity(Mockito.isA(Droit.class));
		Mockito.verify(accessRightsRepository, Mockito.never()).removeEntity(Mockito.isA(DroitProfil.class));
	}

	@Test
	public void setViseur_agentInexistant() {

		Integer idAgentAppro = 9002990;
		AgentDto viseurDto = new AgentDto();
		viseurDto.setIdAgent(9005138);

		Droit droitApprobateur = new Droit();

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isUserViseurOfApprobateur(idAgentAppro, viseurDto.getIdAgent())).thenReturn(false);
		Mockito.when(accessRightsRepository.getAgentAccessRights(idAgentAppro)).thenReturn(droitApprobateur);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(viseurDto.getIdAgent())).thenReturn(null);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		ReturnMessageDto result = service.setViseur(idAgentAppro, viseurDto,9005138);

		assertEquals(1, result.getErrors().size());
		assertEquals("L'agent viseur [9005138] n'existe pas.", result.getErrors().get(0).toString());
		Mockito.verify(accessRightsRepository, Mockito.never()).persisEntity(Mockito.isA(Droit.class));
		Mockito.verify(accessRightsRepository, Mockito.never()).persisEntity(Mockito.isA(DroitProfil.class));
		Mockito.verify(accessRightsRepository, Mockito.never()).removeEntity(Mockito.isA(Droit.class));
		Mockito.verify(accessRightsRepository, Mockito.never()).removeEntity(Mockito.isA(DroitProfil.class));
	}

	@Test
	public void setViseur_creeOperateur_DroitExistant() {

		Integer idAgentAppro = 9002990;
		AgentDto viseurDto = new AgentDto();
		viseurDto.setIdAgent(9005138);

		Droit droitApprobateur = new Droit();
		Droit droitOperateur = Mockito.spy(new Droit());

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isUserViseurOfApprobateur(idAgentAppro, viseurDto.getIdAgent())).thenReturn(false);
		Mockito.when(accessRightsRepository.getAgentAccessRights(idAgentAppro)).thenReturn(droitApprobateur);
		Mockito.when(accessRightsRepository.getAgentAccessRights(viseurDto.getIdAgent())).thenReturn(droitOperateur);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(viseurDto.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", helperService);

		ReturnMessageDto result = service.setViseur(idAgentAppro, viseurDto,9005138);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(accessRightsRepository, Mockito.times(1)).persisEntity(droitOperateur);
		assertEquals(1, droitOperateur.getDroitProfils().size());
		Mockito.verify(accessRightsRepository, Mockito.never()).removeEntity(Mockito.isA(Droit.class));
		Mockito.verify(accessRightsRepository, Mockito.never()).removeEntity(Mockito.isA(DroitProfil.class));
	}

	@Test
	public void setViseur_creeOperateur_DroitInexistant() {

		Integer idAgentAppro = 9002990;
		AgentDto viseurDto = new AgentDto();
		viseurDto.setIdAgent(9005138);

		Droit droitApprobateur = new Droit();
		Droit droitOperateur = Mockito.spy(new Droit());

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isUserViseurOfApprobateur(idAgentAppro, viseurDto.getIdAgent())).thenReturn(false);
		Mockito.when(accessRightsRepository.getAgentAccessRights(idAgentAppro)).thenReturn(droitApprobateur);
		Mockito.when(accessRightsRepository.getAgentAccessRights(viseurDto.getIdAgent())).thenReturn(null);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(viseurDto.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", helperService);

		ReturnMessageDto result = service.setViseur(idAgentAppro, viseurDto,9005138);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(accessRightsRepository, Mockito.never()).persisEntity(droitOperateur);
		Mockito.verify(accessRightsRepository, Mockito.times(1)).persisEntity(Mockito.isA(Droit.class));
		Mockito.verify(accessRightsRepository, Mockito.never()).removeEntity(Mockito.isA(Droit.class));
		Mockito.verify(accessRightsRepository, Mockito.never()).removeEntity(Mockito.isA(DroitProfil.class));
	}

	@Test
	public void deleteOperateur_notOperateur() {

		Integer idAgentAppro = 9002990;
		AgentDto operateurDto = new AgentDto();
		operateurDto.setIdAgent(9005138);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.getUserOperateurOfApprobateur(idAgentAppro, operateurDto.getIdAgent())).thenReturn(null);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);

		ReturnMessageDto result = service.deleteOperateur(idAgentAppro, operateurDto,9005138);

		assertEquals(1, result.getErrors().size());
		assertEquals("L'agent [9005138] n'est pas opérateur de l'approbateur [9002990].", result.getErrors().get(0).toString());
		Mockito.verify(accessRightsRepository, Mockito.never()).persisEntity(Mockito.isA(Droit.class));
		Mockito.verify(accessRightsRepository, Mockito.never()).persisEntity(Mockito.isA(DroitProfil.class));
		Mockito.verify(accessRightsRepository, Mockito.never()).removeEntity(Mockito.isA(Droit.class));
		Mockito.verify(accessRightsRepository, Mockito.never()).removeEntity(Mockito.isA(DroitProfil.class));
	}

	@Test
	public void deleteOperateur_deleteDroitEtDroitProfil() {

		Integer idAgentAppro = 9002990;
		AgentDto operateurDto = new AgentDto();
		operateurDto.setIdAgent(9005138);

		Droit droitOperateur = new Droit();

		DroitProfil droitProfilOperateur = new DroitProfil();
		droitProfilOperateur.setDroit(droitOperateur);
		Set<DroitProfil> droitsProfilOperateur = new HashSet<DroitProfil>();
		droitsProfilOperateur.add(droitProfilOperateur);

		droitOperateur.setDroitProfils(droitsProfilOperateur);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.getUserOperateurOfApprobateur(idAgentAppro, operateurDto.getIdAgent())).thenReturn(droitProfilOperateur);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);

		ReturnMessageDto result = service.deleteOperateur(idAgentAppro, operateurDto,9005138);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(accessRightsRepository, Mockito.never()).persisEntity(Mockito.isA(Droit.class));
		Mockito.verify(accessRightsRepository, Mockito.never()).persisEntity(Mockito.isA(DroitProfil.class));
		Mockito.verify(accessRightsRepository, Mockito.times(1)).removeEntity(Mockito.isA(Droit.class));
		Mockito.verify(accessRightsRepository, Mockito.times(1)).removeEntity(Mockito.isA(DroitProfil.class));
	}

	@Test
	public void deleteOperateur_deleteOnlyDroitProfil() {

		Integer idAgentAppro = 9002990;
		AgentDto operateurDto = new AgentDto();
		operateurDto.setIdAgent(9005138);

		Droit droitOperateur = new Droit();

		DroitProfil droitProfilOperateur = new DroitProfil();
		droitProfilOperateur.setDroit(droitOperateur);
		DroitProfil droitProfilOperateur2 = new DroitProfil();
		droitProfilOperateur2.setDroit(droitOperateur);
		Set<DroitProfil> droitsProfilOperateur = new HashSet<DroitProfil>();
		droitsProfilOperateur.add(droitProfilOperateur);
		droitsProfilOperateur.add(droitProfilOperateur2);

		droitOperateur.setDroitProfils(droitsProfilOperateur);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.getUserOperateurOfApprobateur(idAgentAppro, operateurDto.getIdAgent())).thenReturn(droitProfilOperateur);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);

		ReturnMessageDto result = service.deleteOperateur(idAgentAppro, operateurDto,9005138);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(accessRightsRepository, Mockito.never()).persisEntity(Mockito.isA(Droit.class));
		Mockito.verify(accessRightsRepository, Mockito.never()).persisEntity(Mockito.isA(DroitProfil.class));
		Mockito.verify(accessRightsRepository, Mockito.never()).removeEntity(Mockito.isA(Droit.class));
		Mockito.verify(accessRightsRepository, Mockito.times(1)).removeEntity(Mockito.isA(DroitProfil.class));
	}

	@Test
	public void deleteViseur_notOperateur() {
		Integer idAgentAppro = 9002990;
		AgentDto viseurDto = new AgentDto();
		viseurDto.setIdAgent(9005138);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.getUserOperateurOfApprobateur(idAgentAppro, viseurDto.getIdAgent())).thenReturn(null);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);

		ReturnMessageDto result = service.deleteViseur(idAgentAppro, viseurDto,9005138);

		assertEquals(1, result.getErrors().size());
		assertEquals("L'agent [9005138] n'est pas viseur de l'approbateur [9002990].", result.getErrors().get(0).toString());
		Mockito.verify(accessRightsRepository, Mockito.never()).persisEntity(Mockito.isA(Droit.class));
		Mockito.verify(accessRightsRepository, Mockito.never()).persisEntity(Mockito.isA(DroitProfil.class));
		Mockito.verify(accessRightsRepository, Mockito.never()).removeEntity(Mockito.isA(Droit.class));
		Mockito.verify(accessRightsRepository, Mockito.never()).removeEntity(Mockito.isA(DroitProfil.class));
	}

	@Test
	public void deleteViseur_deleteDroitEtDroitProfil() {

		Integer idAgentAppro = 9002990;
		AgentDto viseurDto = new AgentDto();
		viseurDto.setIdAgent(9005138);

		Droit droitViseur = new Droit();

		DroitProfil droitProfilViseur = new DroitProfil();
		droitProfilViseur.setDroit(droitViseur);
		Set<DroitProfil> droitsProfilViseur = new HashSet<DroitProfil>();
		droitsProfilViseur.add(droitProfilViseur);

		droitViseur.setDroitProfils(droitsProfilViseur);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.getUserViseurOfApprobateur(idAgentAppro, viseurDto.getIdAgent())).thenReturn(droitProfilViseur);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);

		ReturnMessageDto result = service.deleteViseur(idAgentAppro, viseurDto,9005138);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(accessRightsRepository, Mockito.never()).persisEntity(Mockito.isA(Droit.class));
		Mockito.verify(accessRightsRepository, Mockito.never()).persisEntity(Mockito.isA(DroitProfil.class));
		Mockito.verify(accessRightsRepository, Mockito.times(1)).removeEntity(Mockito.isA(Droit.class));
		Mockito.verify(accessRightsRepository, Mockito.times(1)).removeEntity(Mockito.isA(DroitProfil.class));
	}

	@Test
	public void deleteViseur_deleteOnlyDroitProfil() {

		Integer idAgentAppro = 9002990;
		AgentDto viseurDto = new AgentDto();
		viseurDto.setIdAgent(9005138);

		Droit droitViseur = new Droit();

		DroitProfil droitProfilViseur = new DroitProfil();
		droitProfilViseur.setDroit(droitViseur);
		DroitProfil droitProfilViseur2 = new DroitProfil();
		droitProfilViseur2.setDroit(droitViseur);
		Set<DroitProfil> droitsProfilViseur = new HashSet<DroitProfil>();
		droitsProfilViseur.add(droitProfilViseur);
		droitsProfilViseur.add(droitProfilViseur2);

		droitViseur.setDroitProfils(droitsProfilViseur);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.getUserViseurOfApprobateur(idAgentAppro, viseurDto.getIdAgent())).thenReturn(droitProfilViseur);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);

		ReturnMessageDto result = service.deleteViseur(idAgentAppro, viseurDto,9005138);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(accessRightsRepository, Mockito.never()).persisEntity(Mockito.isA(Droit.class));
		Mockito.verify(accessRightsRepository, Mockito.never()).persisEntity(Mockito.isA(DroitProfil.class));
		Mockito.verify(accessRightsRepository, Mockito.never()).removeEntity(Mockito.isA(Droit.class));
		Mockito.verify(accessRightsRepository, Mockito.times(1)).removeEntity(Mockito.isA(DroitProfil.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getListAgentByService_NoAgents() {
		Date dateJour = new Date();
		// mock
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getListDroitsAgent(Mockito.anyList())).thenReturn(new ArrayList<DroitsAgent>());

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getListAgentServiceWithParent(1, dateJour)).thenReturn(new ArrayList<AgentWithServiceDto>());

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// When
		List<Integer> dto = service.getListAgentByService(1, dateJour);

		// Then
		assertEquals(0, dto.size());
	}

	@Test
	public void getListAgentByService_Agents() {
		// Given
		List<DroitsAgent> listDroitAg = new ArrayList<DroitsAgent>();
		Date dateJour = new Date();

		DroitsAgent droitAgent2 = new DroitsAgent();
		droitAgent2.setIdAgent(9005138);
		listDroitAg.add(droitAgent2);
		DroitsAgent droitAgent = new DroitsAgent();
		droitAgent.setIdAgent(9005131);
		listDroitAg.add(droitAgent);

		AgentWithServiceDto ag2 = new AgentWithServiceDto();
		ag2.setIdAgent(9005131);
		ag2.setIdServiceADS(1);
		AgentWithServiceDto ag1 = new AgentWithServiceDto();
		ag1.setIdAgent(9005138);
		ag1.setIdServiceADS(1);
		List<AgentWithServiceDto> listAg = new ArrayList<AgentWithServiceDto>();
		listAg.add(ag1);
		listAg.add(ag2);

		// mock
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getListDroitsAgent(Arrays.asList(ag1.getIdAgent(), ag2.getIdAgent()))).thenReturn(listDroitAg);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getListAgentServiceWithParent(1, dateJour)).thenReturn(listAg);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		// When
		List<Integer> dto = service.getListAgentByService(1, dateJour);

		// Then
		assertEquals(2, dto.size());
		assertEquals(droitAgent2.getIdAgent(), dto.get(0));
		assertEquals(droitAgent.getIdAgent(), dto.get(1));
	}

	@Test
	public void getAgentsServicesForOperateur_2agents_return2Dtos() {

		// Given
		Integer idAgent = 9007654;
		Date dateJour = new Date();

		AgentWithServiceDto ag2 = new AgentWithServiceDto();
		ag2.setIdAgent(2);
		ag2.setIdServiceADS(2);
		ag2.setService("SERVICE 2");
		AgentWithServiceDto ag1 = new AgentWithServiceDto();
		ag1.setIdAgent(1);
		ag1.setIdServiceADS(1);
		ag1.setService("SERVICE 1");
		List<AgentWithServiceDto> listAg = new ArrayList<AgentWithServiceDto>();
		listAg.add(ag1);
		listAg.add(ag2);

		DroitProfil dp = new DroitProfil();

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
		Mockito.when(arRepo.getDroitProfilByAgentAndLibelle(idAgent, ProfilEnum.OPERATEUR.toString())).thenReturn(Arrays.asList(dp));
		Mockito.when(arRepo.getListOfAgentsToInputOrApprove(idAgent, dp.getIdDroitProfil())).thenReturn(Arrays.asList(da1, da2));
		Mockito.when(arRepo.isOperateurOfAgent(idAgent, da1.getIdAgent())).thenReturn(true);
		Mockito.when(arRepo.isOperateurOfAgent(idAgent, da2.getIdAgent())).thenReturn(true);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getListAgentsWithService(Arrays.asList(da1.getIdAgent(),da2.getIdAgent()), dateJour, false)).thenReturn(Arrays.asList(ag1,ag2));

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		// When
		List<EntiteDto> result = service.getAgentsServicesForOperateur(idAgent, dateJour);

		// Then
		assertEquals(2, result.size());
		assertEquals(1, (int) result.get(0).getIdEntite());
		assertEquals("SERVICE 2", result.get(1).getLabel());
	}

	@Test
	public void getAgentsServicesForOperateur_2agentsSameService_return1Dtos() {

		// Given
		Integer idAgent = 9007654;
		Date dateJour = new Date();

		AgentWithServiceDto ag2 = new AgentWithServiceDto();
		ag2.setIdAgent(2);
		ag2.setIdServiceADS(1);
		ag2.setService("SERVICE 1");
		AgentWithServiceDto ag1 = new AgentWithServiceDto();
		ag1.setIdAgent(1);
		ag1.setIdServiceADS(1);
		ag1.setService("SERVICE 1");
		List<AgentWithServiceDto> listAg = new ArrayList<AgentWithServiceDto>();
		listAg.add(ag1);
		listAg.add(ag2);

		DroitProfil dp = new DroitProfil();

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
		Mockito.when(arRepo.getListOfAgentsToInputOrApprove(idAgent, dp.getIdDroitProfil())).thenReturn(Arrays.asList(da1, da2));
		Mockito.when(arRepo.getDroitProfilByAgentAndLibelle(idAgent, ProfilEnum.OPERATEUR.toString())).thenReturn(Arrays.asList(dp));
		Mockito.when(arRepo.isOperateurOfAgent(idAgent, da1.getIdAgent())).thenReturn(true);
		Mockito.when(arRepo.isOperateurOfAgent(idAgent, da2.getIdAgent())).thenReturn(true);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getListAgentsWithService(Arrays.asList(da1.getIdAgent(),da2.getIdAgent()), dateJour, false)).thenReturn(Arrays.asList(ag1,ag2));

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		// When
		List<EntiteDto> result = service.getAgentsServicesForOperateur(idAgent, dateJour);

		// Then
		assertEquals(1, result.size());
	}
	

	
	@Test
	public void dupliqueDroitsApprobateur_errorDatas() {
		
		Integer idAgentSource = null;
		Integer idAgentDest = 9005138;
		
		AccessRightsService service = new AccessRightsService();
		ReturnMessageDto result = service.dupliqueDroitsApprobateur(idAgentSource, idAgentDest,9005138);
		
		assertEquals("L'agent dupliqué ou à dupliquer n'est pas correcte.", result.getErrors().get(0));
		
		idAgentSource = 9005138;
		idAgentDest = null;
		result = service.dupliqueDroitsApprobateur(idAgentSource, idAgentDest,9005138);
		assertEquals("L'agent dupliqué ou à dupliquer n'est pas correcte.", result.getErrors().get(0));
		
		idAgentSource = 9005138;
		idAgentDest = 9005138;
		result = service.dupliqueDroitsApprobateur(idAgentSource, idAgentDest,9005138);
		assertEquals("L'agent dupliqué ou à dupliquer n'est pas correcte.", result.getErrors().get(0));
	}
	
	@Test
	public void dupliqueDroitsApprobateur_agentSourceNonApprobateur() {
		
		Integer idAgentSource = 9005660;
		Integer idAgentDest = 9005138;

		AccessRightsService service = new AccessRightsService();
		
		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.getDroitByProfilAndAgent(ProfilEnum.APPROBATEUR.toString(), idAgentSource)).thenReturn(null);
		
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		
		ReturnMessageDto result = service.dupliqueDroitsApprobateur(idAgentSource, idAgentDest,9005138);
		
		assertEquals("L'agent 9005660 n'est pas approbateur.", result.getErrors().get(0));
	}
	
	@Test
	public void dupliqueDroitsApprobateur_agentDestinataireDejaApprobateur() {
		
		Integer idAgentSource = 9005660;
		Integer idAgentDest = 9005138;

		Droit droitApproSource = new Droit();
		Droit droitApproDest = new Droit();
		
		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.getDroitByProfilAndAgent(ProfilEnum.APPROBATEUR.toString(), idAgentSource)).thenReturn(droitApproSource);
		Mockito.when(accessRightsRepository.getDroitByProfilAndAgent(ProfilEnum.APPROBATEUR.toString(), idAgentDest)).thenReturn(droitApproDest);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		
		ReturnMessageDto result = service.dupliqueDroitsApprobateur(idAgentSource, idAgentDest,9005138);
		
		assertEquals("L'agent 9005138 est déjà approbateur.", result.getErrors().get(0));
	}
	
	@Test
	public void dupliqueDroitsApprobateur_ok() {
		
		Integer idAgentSource = 9005660;
		Integer idAgentDest = 9005138;
		
		DroitsAgent da = new DroitsAgent();
		da.setIdAgent(9005131);

		DroitProfil droitProfilOperateur = new DroitProfil();
		droitProfilOperateur.setProfil(profilOperateur);
		
		Droit operateur = Mockito.spy(new Droit());
		operateur.setIdAgent(9003623);
		operateur.getDroitProfils().add(droitProfilOperateur);
		
		DroitProfil droitProfilApprobateur = new DroitProfil();
		droitProfilApprobateur.setProfil(profilApprobateur);

		Droit droitApproSource = new Droit();
		droitApproSource.setIdDroit(1);
		droitApproSource.setIdAgent(9005660);
		droitApproSource.getDroitProfils().add(droitProfilApprobateur);
		droitProfilApprobateur.setDroitApprobateur(droitApproSource);
		
		droitProfilOperateur.setDroitApprobateur(droitApproSource);
		
		DroitDroitsAgent dda = new DroitDroitsAgent();
		dda.setDroit(droitApproSource);
		dda.setDroitProfil(droitProfilApprobateur);
		dda.setDroitsAgent(da);
		
		DroitDroitsAgent dda2 = new DroitDroitsAgent();
		dda2.setDroit(operateur);
		dda2.setDroitProfil(droitProfilOperateur);
		dda2.setDroitsAgent(da);
		
		droitProfilApprobateur.getDroitDroitsAgent().add(dda);
		droitProfilOperateur.getDroitDroitsAgent().add(dda2);
		
		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.getDroitByProfilAndAgent(ProfilEnum.APPROBATEUR.toString(), idAgentSource)).thenReturn(droitApproSource);
		Mockito.when(accessRightsRepository.getDroitByProfilAndAgent(ProfilEnum.APPROBATEUR.toString(), idAgentDest)).thenReturn(null);
		Mockito.when(accessRightsRepository.getDroitSousApprobateur(idAgentSource)).thenReturn(Arrays.asList(operateur));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				Droit obj = (Droit) args[0];

				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(1, obj.getDroitProfils().size());
				DroitProfil dp = obj.getDroitProfils().iterator().next();
				assertEquals(ProfilEnum.APPROBATEUR.toString(), dp.getProfil().getLibelle());
				assertEquals(obj, dp.getDroitApprobateur());

				return true;
			}
		}).when(accessRightsRepository).persisEntity(Mockito.isA(Droit.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DroitDroitsAgent obj = (DroitDroitsAgent) args[0];

				// dans le cas de l approbateur
				if(obj.getDroitProfil().getDroit().getIdAgent().equals(9005138)) {
					assertEquals(9005138, obj.getDroitProfil().getDroit().getIdAgent().intValue());
				}else{
					// dans le cas de l operateur
					assertEquals(9003623, obj.getDroitProfil().getDroit().getIdAgent().intValue());
				}
				
				assertEquals(9005131, obj.getDroitsAgent().getIdAgent().intValue());

				return true;
			}
		}).when(accessRightsRepository).persisEntity(Mockito.isA(DroitDroitsAgent.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DroitProfil obj = (DroitProfil) args[0];

				assertEquals(9003623, obj.getDroit().getIdAgent().intValue());
				assertEquals(9005138, obj.getDroitApprobateur().getIdAgent().intValue());

				return true;
			}
		}).when(accessRightsRepository).persisEntity(Mockito.isA(DroitProfil.class));
		
		HelperService helperService = Mockito.mock(HelperService.class);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		
		ReturnMessageDto result = service.dupliqueDroitsApprobateur(idAgentSource, idAgentDest,9005138);
		
		Mockito.verify(accessRightsRepository, Mockito.times(2)).persisEntity(Mockito.isA(Droit.class));
		Mockito.verify(accessRightsRepository, Mockito.times(2)).persisEntity(Mockito.isA(DroitDroitsAgent.class));
		Mockito.verify(accessRightsRepository, Mockito.times(1)).persisEntity(Mockito.isA(DroitProfil.class));
		assertEquals(0, result.getErrors().size());
		assertEquals("Nouvel approbateur 9005138 bien créé.", result.getInfos().get(0));
	}
}
