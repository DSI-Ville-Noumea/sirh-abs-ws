package nc.noumea.mairie.abs.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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
import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.abs.dto.InputterDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.AccessRightsRepository;
import nc.noumea.mairie.abs.repository.IAccessRightsRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.abs.web.AccessForbiddenException;
import nc.noumea.mairie.sirh.domain.Agent;
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

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

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

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

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

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

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
		Mockito.doNothing().when(dd).remove();
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

		HelperService helpServ = Mockito.mock(HelperService.class);
		Mockito.when(helpServ.getCurrentDate()).thenReturn(d);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "helperService", helpServ);

		// When
		service.setApprobateurs(Arrays.asList(agentDto));

		// Then
		Mockito.verify(arRepo, Mockito.times(1)).persisEntity(Mockito.isA(Droit.class));
		// Mockito.verify(dd, Mockito.times(1)).remove();
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
		Mockito.doNothing().when(d).remove();
		d.setIdAgent(9005138);

		DroitProfil dp = new DroitProfil();
		dp.setIdDroitProfil(1);
		dp.setDroit(d);
		dp.setProfil(p);

		d.getDroitProfils().add(dp);

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentsApprobateurs()).thenReturn(Arrays.asList(d));

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// When
		service.setApprobateurs(new ArrayList<AgentWithServiceDto>());

		// Then
		Mockito.verify(arRepo, Mockito.never()).persisEntity(Mockito.isA(Droit.class));
		// Mockito.verify(d, Mockito.times(1)).remove();
	}

	@Test
	public void setAgentsApprobateurs_Suppression() {
		// Given
		AgentWithServiceDto agentDto = new AgentWithServiceDto();
		agentDto.setIdAgent(9005131);

		Profil p = new Profil();
		p.setLibelle("APPROBATEUR");

		Droit d = Mockito.spy(new Droit());
		Mockito.doNothing().when(d).remove();
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
		AgentWithServiceDto agDto2 = new AgentWithServiceDto();
		agDto2.setIdAgent(9003041);
		agDto2.setNom("TITO");
		agDto2.setService("service");
		agDto2.setCodeService("CODE2");

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
		assertEquals("CODE", dto.get(1).getCodeService());
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
		assertEquals(0, result.getViseurs().size());
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
		Agent agentDelegataire = new Agent();
		agentDelegataire.setIdAgent(idAgentDelegataire);
		agentDelegataire.setPrenomUsage("TEST");

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getDroitSousApprobateur(idAgentAppro)).thenReturn(Arrays.asList(droitDelegataire));
		Mockito.when(arRepo.isUserDelegataireOfApprobateur(idAgentAppro, idAgentDelegataire)).thenReturn(true);

		ISirhRepository sirhRepo = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepo.getAgent(idAgentDelegataire)).thenReturn(agentDelegataire);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepo);

		// When
		InputterDto result = service.getInputter(idAgentAppro);

		// Then
		assertEquals(idAgentDelegataire, result.getDelegataire().getIdAgent());
		assertEquals("TEST", result.getDelegataire().getPrenom());
		assertEquals(0, result.getViseurs().size());
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
		Agent agentOperateur = new Agent();
		agentOperateur.setIdAgent(idAgentOperateur);
		agentOperateur.setPrenomUsage("TEST");

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getDroitSousApprobateur(idAgentAppro)).thenReturn(Arrays.asList(droitOperateur));
		Mockito.when(arRepo.isUserOperateurOfApprobateur(idAgentAppro, idAgentOperateur)).thenReturn(true);

		ISirhRepository sirhRepo = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepo.getAgent(idAgentOperateur)).thenReturn(agentOperateur);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepo);

		// When
		InputterDto result = service.getInputter(idAgentAppro);

		// Then
		assertNull(result.getDelegataire());
		assertEquals(0, result.getViseurs().size());
		assertEquals(1, result.getOperateurs().size());
		assertEquals(9005131, (int) result.getOperateurs().get(0).getIdAgent());
	}

	@Test
	public void getInputter_Return2Viseurs() {

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
		Agent agentViseur1 = new Agent();
		agentViseur1.setIdAgent(idAgentViseur1);
		agentViseur1.setPrenomUsage("TEST");

		Agent agentViseur2 = new Agent();
		agentViseur2.setIdAgent(idAgentViseur2);
		agentViseur2.setPrenomUsage("TEST2");

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getDroitSousApprobateur(idAgentAppro))
				.thenReturn(Arrays.asList(droitViseur1, droitViseur2));
		Mockito.when(arRepo.isUserViseurOfApprobateur(idAgentAppro, idAgentViseur1)).thenReturn(true);
		Mockito.when(arRepo.isUserViseurOfApprobateur(idAgentAppro, idAgentViseur2)).thenReturn(true);

		ISirhRepository sirhRepo = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepo.getAgent(idAgentViseur1)).thenReturn(agentViseur1);
		Mockito.when(sirhRepo.getAgent(idAgentViseur2)).thenReturn(agentViseur2);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepo);

		// When
		InputterDto result = service.getInputter(idAgentAppro);

		// Then
		assertNull(result.getDelegataire());
		assertEquals(2, result.getViseurs().size());
		assertEquals(0, result.getOperateurs().size());
		assertEquals(9005131, (int) result.getViseurs().get(0).getIdAgent());
		assertEquals("TEST", (String) result.getViseurs().get(0).getPrenom());
		assertEquals(9002990, (int) result.getViseurs().get(1).getIdAgent());
		assertEquals("TEST2", (String) result.getViseurs().get(1).getPrenom());
	}

	@Test
	public void getAgentsToApprove_NoAgents_ReturnEmptyList() {

		// Given
		Integer idAgent = 9007654;
		DroitProfil dp = new DroitProfil();
		dp.setIdDroitProfil(1);
		
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getListOfAgentsToInputOrApprove(idAgent, null, idAgent)).thenReturn(new ArrayList<DroitsAgent>());
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

		Agent a1 = new Agent();
		a1.setIdAgent(1);
		a1.setNomUsage("TEST 1");
		Agent a2 = new Agent();
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
		Mockito.when(arRepo.getListOfAgentsToInputOrApprove(idAgent, null, dp.getIdDroitProfil())).thenReturn(Arrays.asList(da1, da2));

		ISirhRepository mRepo = Mockito.mock(ISirhRepository.class);
		Mockito.when(mRepo.getAgent(1)).thenReturn(a1);
		Mockito.when(mRepo.getAgent(2)).thenReturn(a2);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhRepository", mRepo);

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
	public void setInputter_delegataireOperateurAndViseurNoExisting() {
		// Given
		Integer idAgent = 9005138;
		
		InputterDto dto = new InputterDto();
		
		AgentDto delegataire = new AgentDto();
		delegataire.setIdAgent(9001234);
		dto.setDelegataire(delegataire);
		
		AgentDto operateur = new AgentDto();
		operateur.setIdAgent(9001234);
		dto.setOperateurs(Arrays.asList(operateur));
		
		AgentDto viseur = new AgentDto();
		viseur.setIdAgent(9001234);
		dto.setViseurs(Arrays.asList(viseur));
		
		Droit dd = Mockito.spy(new Droit());
		Mockito.doNothing().when(dd).remove();
		
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentAccessRights(idAgent)).thenReturn(dd);
		
		ISirhRepository sirhRepo = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepo.getAgent(delegataire.getIdAgent())).thenReturn(null);
		
		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepo);
		
		// When
		ReturnMessageDto msgDto = service.setInputter(9005138, dto);
		
		// Then
		assertEquals(3, msgDto.getErrors().size());
		assertTrue(msgDto.getErrors().get(0).contains("n'existe pas"));
		assertTrue(msgDto.getErrors().get(1).contains("n'existe pas"));
		assertTrue(msgDto.getErrors().get(2).contains("n'existe pas"));
	}
	
	@Test
	public void setInputter_delegataireOperateurAndViseurCanNotBe() {
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
		
		AgentDto viseur = new AgentDto();
		viseur.setIdAgent(9001238);
		AgentDto viseur2 = new AgentDto();
		viseur2.setIdAgent(9001239);
		dto.setViseurs(Arrays.asList(viseur, viseur2));
		
		Droit dd = Mockito.spy(new Droit());
		Mockito.doNothing().when(dd).remove();
		
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentAccessRights(idAgent)).thenReturn(dd);
		
		Mockito.when(arRepo.isUserOperateur(delegataire.getIdAgent())).thenReturn(true);
		
		Mockito.when(arRepo.isUserApprobateur(operateur.getIdAgent())).thenReturn(true);
		Mockito.when(arRepo.isUserViseur(operateur2.getIdAgent())).thenReturn(true);
		Mockito.when(arRepo.isUserDelegataire(operateur3.getIdAgent())).thenReturn(true);
		
		Mockito.when(arRepo.isUserApprobateur(viseur.getIdAgent())).thenReturn(true);
		Mockito.when(arRepo.isUserOperateur(viseur2.getIdAgent())).thenReturn(true);
		
		ISirhRepository sirhRepo = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepo.getAgent(delegataire.getIdAgent())).thenReturn(new Agent());
		Mockito.when(sirhRepo.getAgent(operateur.getIdAgent())).thenReturn(new Agent());
		Mockito.when(sirhRepo.getAgent(operateur2.getIdAgent())).thenReturn(new Agent());
		Mockito.when(sirhRepo.getAgent(operateur3.getIdAgent())).thenReturn(new Agent());
		Mockito.when(sirhRepo.getAgent(viseur.getIdAgent())).thenReturn(new Agent());
		Mockito.when(sirhRepo.getAgent(viseur2.getIdAgent())).thenReturn(new Agent());
		
		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepo);
		
		// When
		ReturnMessageDto msgDto = service.setInputter(9005138, dto);
		
		// Then
		assertEquals(6, msgDto.getErrors().size());
		assertTrue(msgDto.getErrors().get(0).contains("ne peut pas être délégataire car il ou elle est déjà opérateur."));
		assertTrue(msgDto.getErrors().get(1).contains("ne peut pas être opérateur car il ou elle est déjà approbateur."));
		assertTrue(msgDto.getErrors().get(2).contains("ne peut pas être opérateur car il ou elle est déjà viseur."));
		assertTrue(msgDto.getErrors().get(3).contains("ne peut pas être opérateur car il ou elle est déjà délégataire."));
		assertTrue(msgDto.getErrors().get(4).contains("ne peut pas être viseur car il ou elle est déjà approbateur."));
		assertTrue(msgDto.getErrors().get(5).contains("ne peut pas être viseur car il ou elle est déjà opérateur."));
	}
	
	@Test
	public void setInputter_delegataireOperateurAndViseurPersist() {
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
		
		AgentDto viseur = new AgentDto();
		viseur.setIdAgent(9001238);
		AgentDto viseur2 = new AgentDto();
		viseur2.setIdAgent(9001239);
		dto.setViseurs(Arrays.asList(viseur, viseur2));
		
		Droit dd = Mockito.spy(new Droit());
		Mockito.doNothing().when(dd).remove();
		
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentAccessRights(idAgent)).thenReturn(dd);
		
		Mockito.when(arRepo.isUserOperateur(delegataire.getIdAgent())).thenReturn(false);
		
		Mockito.when(arRepo.isUserApprobateur(operateur.getIdAgent())).thenReturn(false);
		Mockito.when(arRepo.isUserViseur(operateur2.getIdAgent())).thenReturn(false);
		Mockito.when(arRepo.isUserDelegataire(operateur3.getIdAgent())).thenReturn(false);
		
		Mockito.when(arRepo.isUserApprobateur(viseur.getIdAgent())).thenReturn(false);
		Mockito.when(arRepo.isUserOperateur(viseur2.getIdAgent())).thenReturn(false);
		
		Mockito.when(arRepo.getAgentAccessRights(delegataire.getIdAgent())).thenReturn(new Droit());
		Mockito.when(arRepo.getAgentAccessRights(operateur.getIdAgent())).thenReturn(new Droit());
		Mockito.when(arRepo.getAgentAccessRights(operateur2.getIdAgent())).thenReturn(new Droit());
		Mockito.when(arRepo.getAgentAccessRights(operateur3.getIdAgent())).thenReturn(new Droit());
		Mockito.when(arRepo.getAgentAccessRights(viseur.getIdAgent())).thenReturn(new Droit());
		Mockito.when(arRepo.getAgentAccessRights(viseur2.getIdAgent())).thenReturn(new Droit());
		
		Mockito.when(arRepo.getProfilByName(ProfilEnum.DELEGATAIRE.toString())).thenReturn(new Profil());
		Mockito.when(arRepo.getProfilByName(ProfilEnum.OPERATEUR.toString())).thenReturn(new Profil());
		Mockito.when(arRepo.getProfilByName(ProfilEnum.VISEUR.toString())).thenReturn(new Profil());
		
		ISirhRepository sirhRepo = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepo.getAgent(delegataire.getIdAgent())).thenReturn(new Agent());
		Mockito.when(sirhRepo.getAgent(operateur.getIdAgent())).thenReturn(new Agent());
		Mockito.when(sirhRepo.getAgent(operateur2.getIdAgent())).thenReturn(new Agent());
		Mockito.when(sirhRepo.getAgent(operateur3.getIdAgent())).thenReturn(new Agent());
		Mockito.when(sirhRepo.getAgent(viseur.getIdAgent())).thenReturn(new Agent());
		Mockito.when(sirhRepo.getAgent(viseur2.getIdAgent())).thenReturn(new Agent());
		
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
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepo);
		ReflectionTestUtils.setField(service, "helperService", helpServ);
		
		// When
		ReturnMessageDto msgDto = service.setInputter(9005138, dto);
		
		// Then
		Mockito.verify(arRepo, Mockito.times(6)).persisEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(0)).removeEntity(Mockito.isA(Droit.class));
		assertEquals(0, msgDto.getErrors().size());
	}
	
	@Test
	public void setInputter_delegataireOperateurAndViseurRemove() {
		// Given
		Integer idAgent = 9005138;
		
		// approbateur
		Droit droitApprobateur = new Droit();
		droitApprobateur.setIdAgent(idAgent);
		droitApprobateur.setIdDroit(13);
		
		InputterDto dto = new InputterDto();
		dto.setDelegataire(null);
		dto.setOperateurs(new ArrayList<AgentDto>());
		dto.setViseurs(new ArrayList<AgentDto>());
		
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
		
		// viseur existant a supprimer
		DroitProfil droitProfilViseurExistant = new DroitProfil();
		droitProfilViseurExistant.setDroitApprobateur(droitApprobateur);
		Set<DroitProfil> droitProfilViseurExistants = new HashSet<DroitProfil>();
		droitProfilViseurExistants.add(droitProfilViseurExistant);
		
		Droit droitViseurExistant = new Droit();
		droitViseurExistant.setIdAgent(2);
		droitProfilViseurExistant.setDroit(droitViseurExistant);
		droitViseurExistant.setDroitProfils(droitProfilViseurExistants);
		
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
		listDroitSousAgentsByApprobateur.add(droitViseurExistant);
		listDroitSousAgentsByApprobateur.add(droitDelegataireExistant);
		
		// mock
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentAccessRights(idAgent)).thenReturn(droitApprobateur);
		Mockito.when(arRepo.getDroitSousApprobateur(idAgent)).thenReturn(listDroitSousAgentsByApprobateur);
		Mockito.when(arRepo.isUserOperateurOfApprobateur(idAgent, droitOperateurExistant.getIdAgent())).thenReturn(true);
		Mockito.when(arRepo.isUserViseurOfApprobateur(idAgent, droitViseurExistant.getIdAgent())).thenReturn(true);
		Mockito.when(arRepo.isUserDelegataireOfApprobateur(idAgent, droitDelegataireExistant.getIdAgent())).thenReturn(true);
		
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
		Mockito.verify(arRepo, Mockito.times(3)).removeEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(3)).removeEntity(Mockito.isA(DroitProfil.class));
		assertEquals(0, msgDto.getErrors().size());
	}
	
	@Test
	public void setInputter_OperateurDeDeuxApprobateursRemove() {
		// Given
		Integer idAgent = 9005138;
		
		// approbateur
		Droit droitApprobateur = new Droit();
		droitApprobateur.setIdAgent(idAgent);
		droitApprobateur.setIdDroit(13);
		
		InputterDto dto = new InputterDto();
		dto.setDelegataire(null);
		dto.setOperateurs(new ArrayList<AgentDto>());
		dto.setViseurs(new ArrayList<AgentDto>());
		
		List<Droit> listDroitSousAgentsByApprobateur = new ArrayList<Droit>();
		
		// operateur existant a supprimer
		DroitProfil droitProfiltOperateurExistant = new DroitProfil();
		droitProfiltOperateurExistant.setDroitApprobateur(droitApprobateur);
		Set<DroitProfil> droitProfiltOperateurExistants = new HashSet<DroitProfil>();
		droitProfiltOperateurExistants.add(droitProfiltOperateurExistant);
		
		Droit droitOperateurExistant = new Droit();
		droitOperateurExistant.setIdAgent(1);
		droitProfiltOperateurExistant.setDroit(droitOperateurExistant);
		droitOperateurExistant.setDroitProfils(droitProfiltOperateurExistants);
		
		listDroitSousAgentsByApprobateur.add(droitOperateurExistant);
		
		// mock
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentAccessRights(idAgent)).thenReturn(droitApprobateur);
		Mockito.when(arRepo.getDroitSousApprobateur(idAgent)).thenReturn(listDroitSousAgentsByApprobateur);
		Mockito.when(arRepo.isUserOperateurOfApprobateur(idAgent, droitOperateurExistant.getIdAgent())).thenReturn(true);
		
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
		Mockito.verify(arRepo, Mockito.times(1)).removeEntity(Mockito.isA(Droit.class));
		Mockito.verify(arRepo, Mockito.times(1)).removeEntity(Mockito.isA(DroitProfil.class));
		assertEquals(0, msgDto.getErrors().size());
	}
	
	@Test
	public void setAgentsToInput_NoOperatorOfApprobator() {
		
		Integer idAgentApprobateur = 9005138; 
		Integer idAgentOperateurOrViseur = 9005139;
		List<AgentDto> agents = new ArrayList<AgentDto>();
		
		Droit droitApprobateur = new Droit();
		Droit droitOperateurOrViseur = new Droit();

		List<Droit> droitSousAgentsByApprobateur = new ArrayList<Droit>();
		
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentDroitFetchAgents(idAgentApprobateur)).thenReturn(droitApprobateur);
		Mockito.when(arRepo.getAgentDroitFetchAgents(idAgentOperateurOrViseur)).thenReturn(droitOperateurOrViseur);
		Mockito.when(arRepo.getDroitSousApprobateur(idAgentApprobateur)).thenReturn(droitSousAgentsByApprobateur);
		
		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		
		AccessForbiddenException afe = null;
		try {
			service.setAgentsToInput(idAgentApprobateur, idAgentOperateurOrViseur, agents);
		} catch(AccessForbiddenException e) {
			afe = e;
		}
		
		assertNotNull(afe);
		assertEquals("Impossible de modifier la liste des agents saisis de l'opérateur ou du viseurs car il n'est pas un opérateur ou viseur de l'agent", afe.getMessage());
	}
	
	@Test
	public void setAgentsToInput_NoOperateurNoViseur() {
		
		Integer idAgentApprobateur = 9005138; 
		Integer idAgentOperateurOrViseur = 9005139;
		List<AgentDto> agents = new ArrayList<AgentDto>();
		
		Droit droitApprobateur = new Droit();
		Droit droitOperateurOrViseur = new Droit();

		List<Droit> droitSousAgentsByApprobateur = new ArrayList<Droit>();
		droitSousAgentsByApprobateur.add(droitOperateurOrViseur);
		
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentDroitFetchAgents(idAgentApprobateur)).thenReturn(droitApprobateur);
		Mockito.when(arRepo.getAgentDroitFetchAgents(idAgentOperateurOrViseur)).thenReturn(droitOperateurOrViseur);
		Mockito.when(arRepo.getDroitSousApprobateur(idAgentApprobateur)).thenReturn(droitSousAgentsByApprobateur);
		Mockito.when(arRepo.isUserOperateur(idAgentOperateurOrViseur)).thenReturn(false);
		Mockito.when(arRepo.isUserViseur(idAgentOperateurOrViseur)).thenReturn(false);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		
		AccessForbiddenException afe = null;
		try {
			service.setAgentsToInput(idAgentApprobateur, idAgentOperateurOrViseur, agents);
		} catch(AccessForbiddenException e) {
			afe = e;
		}
		
		assertNotNull(afe);
		assertEquals("Impossible de modifier la liste des agents saisis de l'opérateur car il n'est pas ni opérateur, ni viseur", afe.getMessage());
	}
	
	@Test
	@Transactional("absTransactionManager")
	public void setAgentsToInput_addAgent() {
		
		Integer idAgentApprobateur = 9005138; 
		Integer idAgentOperateurOrViseur = 9005139;
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
		
		Droit droitApprobateur = new Droit();
		Droit droitOperateurOrViseur = new Droit();

		List<Droit> droitSousAgentsByApprobateur = new ArrayList<Droit>();
		droitSousAgentsByApprobateur.add(droitOperateurOrViseur);
		
		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentDroitFetchAgents(idAgentApprobateur)).thenReturn(droitApprobateur);
		Mockito.when(arRepo.getAgentDroitFetchAgents(idAgentOperateurOrViseur)).thenReturn(droitOperateurOrViseur);
		Mockito.when(arRepo.getDroitSousApprobateur(idAgentApprobateur)).thenReturn(droitSousAgentsByApprobateur);
		Mockito.when(arRepo.isUserOperateur(idAgentOperateurOrViseur)).thenReturn(true);
		Mockito.when(arRepo.isUserViseur(idAgentOperateurOrViseur)).thenReturn(false);

		DroitsAgent daInAppro = new DroitsAgent();
//		Mockito.when(arRepo.isUserViseur(daInAppro.getDroits().add)).thenReturn(false);
		
		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		
		DroitsAgent da = new DroitsAgent();
		ReflectionTestUtils.setField(da, "daInAppro", daInAppro);
//		daInAppro.getDroits().add
		AccessForbiddenException afe = null;
		try {
			service.setAgentsToInput(idAgentApprobateur, idAgentOperateurOrViseur, agents);
		} catch(AccessForbiddenException e) {
			afe = e;
		}
		
		assertNull(afe);
		
//		Mockito.verify(.get, Mockito.times(3)).add(Mockito.isA(Droit.class));


		absEntityManager.flush();
		absEntityManager.clear();
	}
}
