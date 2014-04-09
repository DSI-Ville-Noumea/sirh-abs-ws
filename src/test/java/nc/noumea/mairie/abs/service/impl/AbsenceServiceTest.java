package nc.noumea.mairie.abs.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.FlushModeType;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeRecup;
import nc.noumea.mairie.abs.domain.DemandeReposComp;
import nc.noumea.mairie.abs.domain.Droit;
import nc.noumea.mairie.abs.domain.DroitDroitsAgent;
import nc.noumea.mairie.abs.domain.DroitProfil;
import nc.noumea.mairie.abs.domain.DroitsAgent;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.ProfilEnum;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.domain.RefTypeSaisi;
import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IAccessRightsRepository;
import nc.noumea.mairie.abs.repository.IDemandeRepository;
import nc.noumea.mairie.abs.service.IAbsenceDataConsistencyRules;
import nc.noumea.mairie.abs.service.IAccessRightsService;
import nc.noumea.mairie.abs.service.ICounterService;
import nc.noumea.mairie.abs.service.counter.impl.CounterServiceFactory;
import nc.noumea.mairie.ws.ISirhWSConsumer;

import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

public class AbsenceServiceTest {

	@Test
	public void getListeNonFiltreeDemandes_return1Liste() {

		Integer idAgentConnecte = 9005138;

		List<Demande> listdemande = new ArrayList<Demande>();
		listdemande.add(new Demande());

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.isUserDelegataire(idAgentConnecte)).thenReturn(false);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(
				demandeRepository.listeDemandesAgent(idAgentConnecte, null, null, null,
						RefTypeAbsenceEnum.RECUP.getValue())).thenReturn(listdemande);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				null);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		List<Demande> listResult = service.getListeNonFiltreeDemandes(idAgentConnecte, null, null, null,
				RefTypeAbsenceEnum.RECUP.getValue());

		assertEquals(1, listResult.size());
	}

	@Test
	public void getListeNonFiltreeDemandes_return2Listes() {

		Integer idAgentConnecte = 9005138;
		Integer idApprobateurOfDelegataire = 9005140;

		Demande demande1 = new Demande();
		demande1.setIdDemande(1);
		Demande demande2 = new Demande();
		demande2.setIdDemande(2);
		Demande demande3 = new Demande();
		demande3.setIdDemande(3);

		List<Demande> listdemande = new ArrayList<Demande>();
		listdemande.addAll(Arrays.asList(demande1, demande2));
		List<Demande> listdemandeDeleg = new ArrayList<Demande>();
		listdemandeDeleg.addAll(Arrays.asList(demande3, demande2));

		Droit droitApprobateur = new Droit();
		droitApprobateur.setIdAgent(idApprobateurOfDelegataire);
		DroitProfil dp = new DroitProfil();
		dp.setDroitApprobateur(droitApprobateur);

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.isUserDelegataire(idAgentConnecte)).thenReturn(true);
		Mockito.when(arRepo.getDroitProfilByAgentAndLibelle(idAgentConnecte, ProfilEnum.DELEGATAIRE.toString()))
				.thenReturn(dp);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				idApprobateurOfDelegataire);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(
				demandeRepository.listeDemandesAgent(idAgentConnecte, null, null, null,
						RefTypeAbsenceEnum.RECUP.getValue())).thenReturn(listdemande);
		Mockito.when(
				demandeRepository.listeDemandesAgent(idApprobateurOfDelegataire, null, null, null,
						RefTypeAbsenceEnum.RECUP.getValue())).thenReturn(listdemandeDeleg);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		List<Demande> listResult = service.getListeNonFiltreeDemandes(idAgentConnecte, null, null, null,
				RefTypeAbsenceEnum.RECUP.getValue());

		assertEquals(3, listResult.size());
	}

	@Test
	public void getDemande_Recup_WithResult_isEtatDefinitif() {

		Date dateDebut = new Date();
		Date dateFin = new Date();
		Date dateMaj = new Date();
		Date dateMaj2 = new Date();
		Integer idDemande = 1;

		RefTypeAbsence rta = new RefTypeAbsence();
		rta.setIdRefTypeAbsence(3);

		Demande d = new Demande();
		d.setType(rta);

		DemandeRecup dr = new DemandeRecup();

		EtatDemande ed = new EtatDemande();
		ed.setDate(dateMaj);
		ed.setDemande((Demande) dr);
		ed.setEtat(RefEtatEnum.PROVISOIRE);
		ed.setIdAgent(9005138);
		ed.setIdEtatDemande(1);
		EtatDemande ed2 = new EtatDemande();
		ed2.setDate(dateMaj2);
		ed2.setDemande((Demande) dr);
		ed2.setEtat(RefEtatEnum.SAISIE);
		ed2.setIdAgent(9005138);
		ed2.setIdEtatDemande(2);
		List<EtatDemande> listEtatDemande = new ArrayList<EtatDemande>();
		listEtatDemande.addAll(Arrays.asList(ed2, ed));

		dr.setDateDebut(dateDebut);
		dr.setDateFin(dateFin);
		dr.setDuree(10);
		dr.setEtatsDemande(listEtatDemande);
		dr.setIdAgent(9005138);
		dr.setIdDemande(idDemande);
		dr.setType(rta);

		IDemandeRepository demandeRepo = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepo.getEntity(Demande.class, idDemande)).thenReturn(d);
		Mockito.when(demandeRepo.getEntity(DemandeRecup.class, idDemande)).thenReturn(dr);

		Date date = new Date();
		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(date);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgentService(d.getIdAgent(), date)).thenReturn(new AgentWithServiceDto());
		Mockito.when(sirhWSConsumer.getAgentService(dr.getIdAgent(), date)).thenReturn(new AgentWithServiceDto());

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", helperService);

		// When
		DemandeDto result = service.getDemandeDto(idDemande);

		// Then
		assertEquals(result.getDateDebut(), dateDebut);
		assertEquals(result.getDuree(), dr.getDuree());
		assertEquals(result.getIdAgent(), new Integer(9005138));
		assertEquals(result.getIdDemande(), idDemande);
		assertEquals(result.getIdRefEtat().intValue(), RefEtatEnum.SAISIE.getCodeEtat());
		assertEquals(result.getIdTypeDemande().intValue(), RefTypeAbsenceEnum.RECUP.getValue());
		assertFalse(result.isAffichageBoutonImprimer());
		assertFalse(result.isAffichageBoutonModifier());
		assertFalse(result.isAffichageBoutonSupprimer());
	}

	@Test
	public void getDemande_Recup_WithResult_isNoEtatDefinitif() {

		Date dateDebut = new Date();
		Date dateFin = new Date();
		Date dateMaj = new Date();
		Date dateMaj2 = new Date();
		Integer idDemande = 1;

		RefTypeAbsence rta = new RefTypeAbsence();
		rta.setIdRefTypeAbsence(3);

		Demande d = new Demande();
		d.setType(rta);

		DemandeRecup dr = new DemandeRecup();

		EtatDemande ed = new EtatDemande();
		ed.setDate(dateMaj);
		ed.setDemande((Demande) dr);
		ed.setEtat(RefEtatEnum.SAISIE);
		ed.setIdAgent(9005138);
		ed.setIdEtatDemande(1);
		EtatDemande ed2 = new EtatDemande();
		ed2.setDate(dateMaj2);
		ed2.setDemande((Demande) dr);
		ed2.setEtat(RefEtatEnum.APPROUVEE);
		ed2.setIdAgent(9005138);
		ed2.setIdEtatDemande(2);
		List<EtatDemande> listEtatDemande = new ArrayList<EtatDemande>();
		listEtatDemande.addAll(Arrays.asList(ed2, ed));

		dr.setDateDebut(dateDebut);
		dr.setDateFin(dateFin);
		dr.setDuree(10);
		dr.setEtatsDemande(listEtatDemande);
		dr.setIdAgent(9005138);
		dr.setIdDemande(idDemande);
		dr.setType(rta);

		IDemandeRepository demandeRepo = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepo.getEntity(Demande.class, idDemande)).thenReturn(d);
		Mockito.when(demandeRepo.getEntity(DemandeRecup.class, idDemande)).thenReturn(dr);

		Date date = new Date();
		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(date);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgentService(d.getIdAgent(), date)).thenReturn(new AgentWithServiceDto());
		Mockito.when(sirhWSConsumer.getAgentService(dr.getIdAgent(), date)).thenReturn(new AgentWithServiceDto());

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepo);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		// When
		DemandeDto result = service.getDemandeDto(idDemande);

		// Then
		assertEquals(result.getDateDebut(), dateDebut);
		assertEquals(result.getDuree(), dr.getDuree());
		assertEquals(result.getIdAgent(), new Integer(9005138));
		assertEquals(result.getIdDemande(), idDemande);
		assertEquals(result.getIdRefEtat().intValue(), RefEtatEnum.APPROUVEE.getCodeEtat());
		assertEquals(result.getIdTypeDemande().intValue(), RefTypeAbsenceEnum.RECUP.getValue());
		assertFalse(result.isAffichageBoutonImprimer());
		assertFalse(result.isAffichageBoutonModifier());
		assertFalse(result.isAffichageBoutonSupprimer());
	}

	@Test
	public void saveDemandeRecup_OK_avecEtatProvisoire() {

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		dto.setIdAgent(idAgent);
		dto.setIdRefEtat(0);
		dto.setDateDemande(new Date());
		dto.setAffichageBoutonModifier(false);
		dto.setAffichageBoutonSupprimer(false);
		dto.setAffichageBoutonImprimer(false);

		Droit droitOperateur = new Droit();
		DroitDroitsAgent droitDroitAgent = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(idAgent);
		droitDroitAgent.setDroitsAgent(droitsAgent);
		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.add(droitDroitAgent);
		droitOperateur.setDroitDroitsAgent(droitDroitsAgent);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isUserOperateur(idAgent)).thenReturn(true);
		Mockito.when(accessRightsRepository.getAgentDroitFetchAgents(idAgent)).thenReturn(droitOperateur);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(DemandeRecup.class, dto.getIdDemande()))
				.thenReturn(new DemandeRecup());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeRecup obj = (DemandeRecup) args[0];

				assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(10, obj.getDuree().intValue());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeRecup.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyInt(), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeRecup.class), Mockito.isA(Date.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(demandeRepository).clear();
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(demandeRepository).setFlushMode(FlushModeType.COMMIT);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				null);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.isA(ReturnMessageDto.class))).thenReturn(new ReturnMessageDto());

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "defaultAbsenceDataConsistencyRulesImpl", absDataConsistencyRules);

		result = service.saveDemande(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void saveDemandeRecup_OK_avecEtatSaisie() {

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		dto.setIdAgent(idAgent);
		dto.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());

		Droit droitOperateur = new Droit();
		DroitDroitsAgent droitDroitAgent = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(idAgent);
		droitDroitAgent.setDroitsAgent(droitsAgent);
		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.add(droitDroitAgent);
		droitOperateur.setDroitDroitsAgent(droitDroitsAgent);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isUserOperateur(idAgent)).thenReturn(true);
		Mockito.when(accessRightsRepository.getAgentDroitFetchAgents(idAgent)).thenReturn(droitOperateur);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(DemandeRecup.class, dto.getIdDemande()))
				.thenReturn(new DemandeRecup());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeRecup obj = (DemandeRecup) args[0];

				assertEquals(RefEtatEnum.SAISIE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(10, obj.getDuree().intValue());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeRecup.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyInt(), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeRecup.class), Mockito.isA(Date.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(demandeRepository).clear();
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(demandeRepository).setFlushMode(FlushModeType.COMMIT);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.isA(ReturnMessageDto.class))).thenReturn(result);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				null);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "defaultAbsenceDataConsistencyRulesImpl", absDataConsistencyRules);

		result = service.saveDemande(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void saveDemandeRecup_Ko() {

		ReturnMessageDto result = new ReturnMessageDto();
		result.getErrors().add("erreur droit");

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		dto.setIdAgent(9005139);

		Droit droitOperateur = new Droit();
		DroitDroitsAgent droitDroitAgent = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(idAgent);
		droitDroitAgent.setDroitsAgent(droitsAgent);
		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.add(droitDroitAgent);
		droitOperateur.setDroitDroitsAgent(droitDroitsAgent);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(null);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeRecup obj = (DemandeRecup) args[0];

				assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(10, obj.getDuree().intValue());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeRecup.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyInt(), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeRecup.class), Mockito.isA(Date.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(demandeRepository).clear();
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(demandeRepository).setFlushMode(FlushModeType.COMMIT);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.isA(ReturnMessageDto.class))).thenReturn(result);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "defaultAbsenceDataConsistencyRulesImpl", absDataConsistencyRules);

		result = service.saveDemande(idAgent, dto);

		assertEquals(1, result.getErrors().size());
		assertEquals("erreur droit", result.getErrors().get(0).toString());
		Mockito.verify(demandeRepository, Mockito.times(0)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void setDemandeEtat_EtatIncorrect() {

		Integer idAgent = 9005138;
		DemandeEtatChangeDto dto1 = new DemandeEtatChangeDto();
		dto1.setIdRefEtat(RefEtatEnum.PRISE.getCodeEtat());
		dto1.setDateAvis(new Date());
		dto1.setMotif("motif 1");
		DemandeEtatChangeDto dto2 = new DemandeEtatChangeDto();
		dto2.setIdRefEtat(RefEtatEnum.PROVISOIRE.getCodeEtat());
		dto2.setDateAvis(new Date());
		dto2.setMotif("motif 2");
		DemandeEtatChangeDto dto3 = new DemandeEtatChangeDto();
		dto3.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());
		dto3.setDateAvis(new Date());
		dto3.setMotif("motif 3");

		ReturnMessageDto result1 = new ReturnMessageDto();
		ReturnMessageDto result2 = new ReturnMessageDto();
		ReturnMessageDto result3 = new ReturnMessageDto();

		AbsenceService service = new AbsenceService();
		result1 = service.setDemandeEtat(idAgent, dto1);
		result2 = service.setDemandeEtat(idAgent, dto2);
		result3 = service.setDemandeEtat(idAgent, dto3);

		assertEquals(1, result1.getErrors().size());
		assertEquals("L'état de la demande envoyé n'est pas correcte.", result1.getErrors().get(0).toString());
		assertEquals(1, result2.getErrors().size());
		assertEquals("L'état de la demande envoyé n'est pas correcte.", result2.getErrors().get(0).toString());
		assertEquals(1, result3.getErrors().size());
		assertEquals("L'état de la demande envoyé n'est pas correcte.", result3.getErrors().get(0).toString());
	}

	@Test
	public void setDemandeEtat_demandeInexistante() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = new ReturnMessageDto();

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());
		dto.setIdDemande(1);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(null);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		result = service.setDemandeEtat(idAgent, dto);

		assertEquals(1, result.getErrors().size());
		assertEquals("La demande n'existe pas.", result.getErrors().get(0).toString());
	}

	@Test
	public void setDemandeEtat_setDemandeEtatVisa_noViseurOfAgent() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = new ReturnMessageDto();

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat());
		dto.setIdDemande(1);

		Demande demande = Mockito.spy(new Demande());

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(demande);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isViseurOfAgent(idAgent, demande.getIdAgent())).thenReturn(false);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);

		result = service.setDemandeEtat(idAgent, dto);

		assertEquals(1, result.getErrors().size());
		assertEquals("L'agent Viseur n'est pas habilité pour viser la demande de cet agent.", result.getErrors().get(0)
				.toString());
		Mockito.verify(demande, Mockito.times(0)).addEtatDemande(Mockito.isA(EtatDemande.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setDemandeEtat_setDemandeEtatVisa_badEtat() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = new ReturnMessageDto();

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat());
		dto.setIdDemande(1);

		Demande demande = Mockito.spy(new Demande());

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(demande);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isViseurOfAgent(idAgent, demande.getIdAgent())).thenReturn(true);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				result.getErrors().add("Erreur etat incorrect");
				return result;
			}
		})
				.when(absDataConsistencyRules)
				.checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(List.class));

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "defaultAbsenceDataConsistencyRulesImpl", absDataConsistencyRules);

		result = service.setDemandeEtat(idAgent, dto);

		assertEquals(1, result.getErrors().size());
		assertEquals("Erreur etat incorrect", result.getErrors().get(0).toString());
		Mockito.verify(demande, Mockito.times(0)).addEtatDemande(Mockito.isA(EtatDemande.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setDemandeEtat_setDemandeEtatVisa_ok() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = new ReturnMessageDto();

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat());
		dto.setIdDemande(1);
		dto.setMotif("motif");

		Demande demande = Mockito.spy(new Demande());

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(demande);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isViseurOfAgent(idAgent, demande.getIdAgent())).thenReturn(true);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				return args[0];
			}
		})
				.when(absDataConsistencyRules)
				.checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(List.class));

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "defaultAbsenceDataConsistencyRulesImpl", absDataConsistencyRules);

		result = service.setDemandeEtat(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		assertEquals("La demande est visée favorablement.", result.getInfos().get(0));
		Mockito.verify(demande, Mockito.times(1)).addEtatDemande(Mockito.isA(EtatDemande.class));
	}

	@Test
	public void setDemandeEtat_setDemandeEtatApprouve_AgentApprobateurNonHabilite() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = new ReturnMessageDto();

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());
		dto.setIdDemande(1);

		Demande demande = Mockito.spy(new Demande());

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(demande);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isApprobateurOrDelegataireOfAgent(idAgent, demande.getIdAgent()))
				.thenReturn(false);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);

		result = service.setDemandeEtat(idAgent, dto);

		assertEquals(1, result.getErrors().size());
		assertEquals("L'agent Approbateur n'est pas habilité à approuver la demande de cet agent.", result.getErrors()
				.get(0).toString());
		Mockito.verify(demande, Mockito.times(0)).addEtatDemande(Mockito.isA(EtatDemande.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setDemandeEtat_setDemandeEtatApprouve_checkEtatDemandeAndChampMotif() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = new ReturnMessageDto();

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());
		dto.setIdDemande(1);

		Demande demande = Mockito.spy(new Demande());

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(demande);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isApprobateurOrDelegataireOfAgent(idAgent, demande.getIdAgent()))
				.thenReturn(true);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				result.getErrors().add("Erreur etat incorrect");
				return result;
			}
		})
				.when(absDataConsistencyRules)
				.checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(List.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				result.getErrors().add("Erreur motif incorrect");
				return result;
			}
		})
				.when(absDataConsistencyRules)
				.checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(),
						Mockito.anyString());

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "defaultAbsenceDataConsistencyRulesImpl", absDataConsistencyRules);

		result = service.setDemandeEtat(idAgent, dto);

		assertEquals(2, result.getErrors().size());
		assertEquals("Erreur etat incorrect", result.getErrors().get(0).toString());
		assertEquals("Erreur motif incorrect", result.getErrors().get(1).toString());
		Mockito.verify(demande, Mockito.times(0)).addEtatDemande(Mockito.isA(EtatDemande.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setDemandeEtat_setDemandeEtatApprouve_majCompteurKo() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = null;

		ReturnMessageDto srm = new ReturnMessageDto();
		srm.getErrors().add("erreur maj compteur");

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());
		dto.setIdDemande(1);

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(2);

		DemandeRecup demande = Mockito.spy(new DemandeRecup());
		demande.setDuree(10);
		demande.setType(type);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(demande);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isApprobateurOrDelegataireOfAgent(idAgent, demande.getIdAgent()))
				.thenReturn(true);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		})
				.when(absDataConsistencyRules)
				.checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(List.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		})
				.when(absDataConsistencyRules)
				.checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(),
						Mockito.anyString());

		ICounterService counterService = Mockito.mock(ICounterService.class);
		Mockito.when(
				counterService.majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.anyInt())).thenReturn(srm);
		Mockito.when(counterService.calculMinutesCompteur(dto, demande)).thenReturn(10);

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(counterServiceFactory.getFactory(demande.getType().getIdRefTypeAbsence())).thenReturn(
				counterService);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "defaultAbsenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "counterService", counterService);
		ReflectionTestUtils.setField(service, "counterServiceFactory", counterServiceFactory);

		result = service.setDemandeEtat(idAgent, dto);

		assertEquals(1, result.getErrors().size());
		assertEquals("erreur maj compteur", result.getErrors().get(0).toString());
		Mockito.verify(demande, Mockito.times(0)).addEtatDemande(Mockito.isA(EtatDemande.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setDemandeEtat_setDemandeEtatApprouve_ok() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = null;

		ReturnMessageDto srm = new ReturnMessageDto();

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());
		dto.setIdDemande(1);

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.RECUP.getValue());
		DemandeRecup demande = Mockito.spy(new DemandeRecup());
		demande.setDuree(10);
		demande.setType(type);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(demande);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isApprobateurOrDelegataireOfAgent(idAgent, demande.getIdAgent()))
				.thenReturn(true);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		})
				.when(absDataConsistencyRules)
				.checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(List.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		})
				.when(absDataConsistencyRules)
				.checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(),
						Mockito.anyString());

		ICounterService counterService = Mockito.mock(ICounterService.class);
		Mockito.when(
				counterService.majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.anyInt())).thenReturn(srm);
		Mockito.when(counterService.calculMinutesCompteur(dto, demande)).thenReturn(10);

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(counterServiceFactory.getFactory(demande.getType().getIdRefTypeAbsence())).thenReturn(
				counterService);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "defaultAbsenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "counterService", counterService);
		ReflectionTestUtils.setField(service, "counterServiceFactory", counterServiceFactory);

		result = service.setDemandeEtat(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		assertEquals("La demande est approuvée.", result.getInfos().get(0));
		Mockito.verify(demande, Mockito.times(1)).addEtatDemande(Mockito.isA(EtatDemande.class));
	}

	// @Test
	// public void setDemandesEtatPris_EtatIncorrect_DemandeInexistante_EtOK() {
	// EtatDemande etat1 = new EtatDemande();
	// Demande demande1 = new Demande();
	// etat1.setDemande(demande1);
	// etat1.setEtat(RefEtatEnum.SAISIE);
	// demande1.getEtatsDemande().add(etat1);
	//
	// EtatDemande etat2 = new EtatDemande();
	// Demande demande2 = new Demande();
	// etat2.setDemande(demande2);
	// etat2.setEtat(RefEtatEnum.APPROUVEE);
	// demande2.getEtatsDemande().add(etat2);
	// demande2.setIdAgent(9005138);
	//
	// ReturnMessageDto result = new ReturnMessageDto();
	//
	// IDemandeRepository demandeRepository =
	// Mockito.mock(IDemandeRepository.class);
	// Mockito.when(demandeRepository.getEntity(Demande.class,
	// 1)).thenReturn(demande1);
	// Mockito.when(demandeRepository.getEntity(Demande.class,
	// 2)).thenReturn(demande2);
	// Mockito.when(demandeRepository.getEntity(Demande.class,
	// 3)).thenReturn(null);
	// Mockito.doAnswer(new Answer<Object>() {
	// public Object answer(InvocationOnMock invocation) {
	// Object[] args = invocation.getArguments();
	// EtatDemande obj = (EtatDemande) args[0];
	//
	// assertEquals(RefEtatEnum.PRISE.getCodeEtat(),
	// obj.getEtat().getCodeEtat());
	// assertEquals(9005138, obj.getIdAgent().intValue());
	//
	// return true;
	// }
	// }).when(demandeRepository).persistEntity(Mockito.isA(EtatDemande.class));
	//
	// String csvListIdDemande = "1,2,3";
	//
	// AbsenceService service = new AbsenceService();
	// ReflectionTestUtils.setField(service, "demandeRepository",
	// demandeRepository);
	//
	// // When
	// result = service.setDemandeEtatPris(csvListIdDemande);
	// ReturnMessageDto result2 = service.setDemandeEtatPris("");
	//
	// // Then
	// assertEquals(0, result2.getErrors().size());
	// assertEquals(2, result.getErrors().size());
	// assertEquals("La demande 1 n'est pas à l'état approuvé.",
	// result.getErrors().get(0).toString());
	// assertEquals("La demande 3 n'existe pas.",
	// result.getErrors().get(1).toString());
	// Mockito.verify(demandeRepository,
	// Mockito.times(1)).persistEntity(Mockito.isA(EtatDemande.class));
	// }

	@Test
	public void setDemandeEtat_setDemandeEtatAnnule_noAccessRight() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = new ReturnMessageDto();
		result.getErrors().add("Vous n'êtes pas opérateur. Vous ne pouvez pas saisir de demandes.");

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
		dto.setIdDemande(1);

		Demande demande = Mockito.spy(new Demande());

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(demande);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isUserOperateur(idAgent)).thenReturn(false);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.isA(ReturnMessageDto.class))).thenReturn(result);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);

		result = service.setDemandeEtat(idAgent, dto);

		assertEquals(1, result.getErrors().size());
		assertEquals("Vous n'êtes pas opérateur. Vous ne pouvez pas saisir de demandes.", result.getErrors().get(0)
				.toString());
		Mockito.verify(demande, Mockito.times(0)).addEtatDemande(Mockito.isA(EtatDemande.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setDemandeEtat_setDemandeEtatAnnule_checkEtatDemande() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = new ReturnMessageDto();

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
		dto.setIdDemande(1);

		Demande demande = Mockito.spy(new Demande());
		demande.setIdAgent(idAgent);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(demande);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isApprobateurOrDelegataireOfAgent(idAgent, demande.getIdAgent()))
				.thenReturn(true);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				result.getErrors().add("Erreur etat incorrect");
				return result;
			}
		})
				.when(absDataConsistencyRules)
				.checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(List.class));

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.isA(ReturnMessageDto.class))).thenReturn(new ReturnMessageDto());

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "defaultAbsenceDataConsistencyRulesImpl", absDataConsistencyRules);

		result = service.setDemandeEtat(idAgent, dto);

		assertEquals(1, result.getErrors().size());
		assertEquals("Erreur etat incorrect", result.getErrors().get(0).toString());
		Mockito.verify(demande, Mockito.times(0)).addEtatDemande(Mockito.isA(EtatDemande.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setDemandeEtat_setDemandeEtatAnnule_majCompteurKo() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = null;

		ReturnMessageDto srm = new ReturnMessageDto();
		srm.getErrors().add("erreur maj compteur");

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
		dto.setIdDemande(1);

		EtatDemande etatDemande = new EtatDemande();
		etatDemande.setEtat(RefEtatEnum.APPROUVEE);
		List<EtatDemande> listEtat = new ArrayList<EtatDemande>();
		listEtat.add(etatDemande);

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(2);

		DemandeRecup demande = Mockito.spy(new DemandeRecup());
		demande.setDuree(10);
		demande.setIdAgent(idAgent);
		demande.setEtatsDemande(listEtat);
		demande.setType(type);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(demande);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isApprobateurOrDelegataireOfAgent(idAgent, demande.getIdAgent()))
				.thenReturn(true);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		})
				.when(absDataConsistencyRules)
				.checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(List.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		})
				.when(absDataConsistencyRules)
				.checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(),
						Mockito.anyString());

		ICounterService counterService = Mockito.mock(ICounterService.class);
		Mockito.when(
				counterService.majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.anyInt())).thenReturn(srm);
		Mockito.when(counterService.calculMinutesCompteur(dto, demande)).thenReturn(10);

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(counterServiceFactory.getFactory(demande.getType().getIdRefTypeAbsence())).thenReturn(
				counterService);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.isA(ReturnMessageDto.class))).thenReturn(new ReturnMessageDto());

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "defaultAbsenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "counterService", counterService);
		ReflectionTestUtils.setField(service, "counterServiceFactory", counterServiceFactory);

		result = service.setDemandeEtat(idAgent, dto);

		assertEquals(1, result.getErrors().size());
		assertEquals("erreur maj compteur", result.getErrors().get(0).toString());
		Mockito.verify(demande, Mockito.times(0)).addEtatDemande(Mockito.isA(EtatDemande.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setDemandeEtat_setDemandeEtatAnnule_ok() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = null;

		ReturnMessageDto srm = new ReturnMessageDto();

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
		dto.setIdDemande(1);

		EtatDemande etatDemande = new EtatDemande();
		etatDemande.setEtat(RefEtatEnum.APPROUVEE);
		List<EtatDemande> listEtat = new ArrayList<EtatDemande>();
		listEtat.add(etatDemande);

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(2);

		DemandeRecup demande = Mockito.spy(new DemandeRecup());
		demande.setDuree(10);
		demande.setIdAgent(idAgent);
		demande.setEtatsDemande(listEtat);
		demande.setType(type);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(demande);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isApprobateurOrDelegataireOfAgent(idAgent, demande.getIdAgent()))
				.thenReturn(true);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		})
				.when(absDataConsistencyRules)
				.checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(List.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		})
				.when(absDataConsistencyRules)
				.checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(),
						Mockito.anyString());

		ICounterService counterService = Mockito.mock(ICounterService.class);
		Mockito.when(
				counterService.majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.anyInt())).thenReturn(srm);
		Mockito.when(counterService.calculMinutesCompteur(dto, demande)).thenReturn(10);

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(counterServiceFactory.getFactory(demande.getType().getIdRefTypeAbsence())).thenReturn(
				counterService);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.isA(ReturnMessageDto.class))).thenReturn(new ReturnMessageDto());

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "defaultAbsenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "counterService", counterService);
		ReflectionTestUtils.setField(service, "counterServiceFactory", counterServiceFactory);

		result = service.setDemandeEtat(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		assertEquals("La demande est annulée.", result.getInfos().get(0));
		Mockito.verify(demande, Mockito.times(1)).addEtatDemande(Mockito.isA(EtatDemande.class));
	}

	@Test
	public void setDemandeEtatPris_EtatIncorrect_ReturnError() {

		RefTypeAbsence typeRecup = new RefTypeAbsence();
		typeRecup.setLabel("Récupération");
		typeRecup.setIdRefTypeAbsence(3);

		EtatDemande etat1 = new EtatDemande();
		DemandeRecup demande1 = new DemandeRecup();
		etat1.setDemande(demande1);
		etat1.setEtat(RefEtatEnum.SAISIE);
		demande1.getEtatsDemande().add(etat1);
		demande1.setType(typeRecup);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, 1)).thenReturn(demande1);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		// When
		ReturnMessageDto result = service.setDemandeEtatPris(1);

		// Then
		assertEquals(1, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
		assertEquals("La demande 1 n'est pas à l'état APPROUVEE mais SAISIE.", result.getErrors().get(0).toString());
		Mockito.verify(demandeRepository, Mockito.never()).removeEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void setDemandeEtatPris_DemandeDoesNotExist_ReturnError() {

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, 1)).thenReturn(null);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		// When
		ReturnMessageDto result = service.setDemandeEtatPris(1);

		// Then
		assertEquals(1, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
		assertEquals("La demande 1 n'existe pas.", result.getErrors().get(0).toString());
		Mockito.verify(demandeRepository, Mockito.never()).removeEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void setDemandeEtatPris_EtatIsOk_AddEtatDemande() {

		RefTypeAbsence typeRecup = new RefTypeAbsence();
		typeRecup.setLabel("Récupération");
		typeRecup.setIdRefTypeAbsence(3);

		EtatDemande etat1 = new EtatDemande();
		final DemandeRecup demande1 = new DemandeRecup();
		etat1.setDemande(demande1);
		etat1.setEtat(RefEtatEnum.APPROUVEE);
		demande1.getEtatsDemande().add(etat1);
		demande1.setType(typeRecup);
		demande1.setIdAgent(9006565);

		final Date date = new DateTime(2014, 1, 26, 15, 16, 35).toDate();

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, 1)).thenReturn(demande1);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				EtatDemande etat = (EtatDemande) args[0];

				assertEquals(date, etat.getDate());
				assertEquals(demande1, etat.getDemande());
				assertEquals(RefEtatEnum.PRISE, etat.getEtat());
				assertEquals(9006565, (int) etat.getIdAgent());
				assertNull(etat.getMotif());
				return null;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(EtatDemande.class));

		HelperService helper = Mockito.mock(HelperService.class);
		Mockito.when(helper.getCurrentDate()).thenReturn(date);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helper);

		// When
		ReturnMessageDto result = service.setDemandeEtatPris(1);

		// Then
		assertEquals(0, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(EtatDemande.class));
	}

	@Test
	public void saveDemandeReposComp_OK_avecEtatProvisoire() {

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.REPOS_COMP.getValue());
		dto.setIdAgent(idAgent);
		dto.setIdRefEtat(0);
		dto.setDateDemande(new Date());
		dto.setAffichageBoutonModifier(false);
		dto.setAffichageBoutonSupprimer(false);
		dto.setAffichageBoutonImprimer(false);

		Droit droitOperateur = new Droit();
		DroitDroitsAgent droitDroitAgent = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(idAgent);
		droitDroitAgent.setDroitsAgent(droitsAgent);
		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.add(droitDroitAgent);
		droitOperateur.setDroitDroitsAgent(droitDroitsAgent);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isUserOperateur(idAgent)).thenReturn(true);
		Mockito.when(accessRightsRepository.getAgentDroitFetchAgents(idAgent)).thenReturn(droitOperateur);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(DemandeReposComp.class, dto.getIdDemande())).thenReturn(
				new DemandeReposComp());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeReposComp obj = (DemandeReposComp) args[0];

				assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(10, obj.getDuree().intValue());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeReposComp.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyInt(), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeReposComp.class), Mockito.isA(Date.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(demandeRepository).clear();
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(demandeRepository).setFlushMode(FlushModeType.COMMIT);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				null);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.isA(ReturnMessageDto.class))).thenReturn(new ReturnMessageDto());

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "defaultAbsenceDataConsistencyRulesImpl", absDataConsistencyRules);

		result = service.saveDemande(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void saveDemandeReposComp_OK_avecEtatSaisie() {

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.REPOS_COMP.getValue());
		dto.setIdAgent(idAgent);
		dto.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());

		Droit droitOperateur = new Droit();
		DroitDroitsAgent droitDroitAgent = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(idAgent);
		droitDroitAgent.setDroitsAgent(droitsAgent);
		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.add(droitDroitAgent);
		droitOperateur.setDroitDroitsAgent(droitDroitsAgent);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isUserOperateur(idAgent)).thenReturn(true);
		Mockito.when(accessRightsRepository.getAgentDroitFetchAgents(idAgent)).thenReturn(droitOperateur);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(DemandeReposComp.class, dto.getIdDemande())).thenReturn(
				new DemandeReposComp());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeReposComp obj = (DemandeReposComp) args[0];

				assertEquals(RefEtatEnum.SAISIE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(10, obj.getDuree().intValue());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeReposComp.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyInt(), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeReposComp.class), Mockito.isA(Date.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(demandeRepository).clear();
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(demandeRepository).setFlushMode(FlushModeType.COMMIT);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.isA(ReturnMessageDto.class))).thenReturn(result);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				null);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "defaultAbsenceDataConsistencyRulesImpl", absDataConsistencyRules);

		result = service.saveDemande(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void saveDemandeReposComp_Ko() {

		ReturnMessageDto result = new ReturnMessageDto();
		result.getErrors().add("erreur droit");

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.REPOS_COMP.getValue());
		dto.setIdAgent(9005139);

		Droit droitOperateur = new Droit();
		DroitDroitsAgent droitDroitAgent = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(idAgent);
		droitDroitAgent.setDroitsAgent(droitsAgent);
		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.add(droitDroitAgent);
		droitOperateur.setDroitDroitsAgent(droitDroitsAgent);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(null);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeReposComp obj = (DemandeReposComp) args[0];

				assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(10, obj.getDuree().intValue());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeReposComp.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyInt(), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeReposComp.class), Mockito.isA(Date.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(demandeRepository).clear();
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(demandeRepository).setFlushMode(FlushModeType.COMMIT);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.isA(ReturnMessageDto.class))).thenReturn(result);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "defaultAbsenceDataConsistencyRulesImpl", absDataConsistencyRules);

		result = service.saveDemande(idAgent, dto);

		assertEquals(1, result.getErrors().size());
		assertEquals("erreur droit", result.getErrors().get(0).toString());
		Mockito.verify(demandeRepository, Mockito.times(0)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void getDemande_ReposComp_WithResult_isEtatDefinitif() {

		Date dateDebut = new Date();
		Date dateFin = new Date();
		Date dateMaj = new Date();
		Date dateMaj2 = new Date();
		Integer idDemande = 1;

		RefTypeAbsence rta = new RefTypeAbsence();
		rta.setIdRefTypeAbsence(2);

		Demande d = new Demande();
		d.setType(rta);

		DemandeReposComp dr = new DemandeReposComp();

		EtatDemande ed = new EtatDemande();
		ed.setDate(dateMaj);
		ed.setDemande((Demande) dr);
		ed.setEtat(RefEtatEnum.PROVISOIRE);
		ed.setIdAgent(9005138);
		ed.setIdEtatDemande(1);
		EtatDemande ed2 = new EtatDemande();
		ed2.setDate(dateMaj2);
		ed2.setDemande((Demande) dr);
		ed2.setEtat(RefEtatEnum.SAISIE);
		ed2.setIdAgent(9005138);
		ed2.setIdEtatDemande(2);
		ed2.setMotif("motif");
		List<EtatDemande> listEtatDemande = new ArrayList<EtatDemande>();
		listEtatDemande.addAll(Arrays.asList(ed2, ed));

		dr.setDateDebut(dateDebut);
		dr.setDateFin(dateFin);
		dr.setDuree(10);
		dr.setDureeAnneeN1(10);
		dr.setEtatsDemande(listEtatDemande);
		dr.setIdAgent(9005138);
		dr.setIdDemande(idDemande);
		dr.setType(rta);

		Date date = new Date();
		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(date);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgentService(d.getIdAgent(), date)).thenReturn(new AgentWithServiceDto());
		Mockito.when(sirhWSConsumer.getAgentService(dr.getIdAgent(), date)).thenReturn(new AgentWithServiceDto());

		IDemandeRepository demandeRepo = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepo.getEntity(Demande.class, idDemande)).thenReturn(d);
		Mockito.when(demandeRepo.getEntity(DemandeReposComp.class, idDemande)).thenReturn(dr);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", helperService);

		// When
		DemandeDto result = service.getDemandeDto(idDemande);

		// Then
		assertEquals(result.getDateDebut(), dateDebut);
		assertEquals(result.getDuree().toString(), "20");
		assertEquals(result.getIdAgent(), new Integer(9005138));
		assertEquals(result.getIdDemande(), idDemande);
		assertEquals(result.getIdRefEtat().intValue(), RefEtatEnum.SAISIE.getCodeEtat());
		assertEquals(result.getIdTypeDemande().intValue(), RefTypeAbsenceEnum.REPOS_COMP.getValue());
		assertFalse(result.isAffichageBoutonImprimer());
		assertFalse(result.isAffichageBoutonModifier());
		assertFalse(result.isAffichageBoutonSupprimer());
		assertEquals("motif", result.getMotif());
	}

	@Test
	public void getDemande_ReposComp_WithResult_isNoEtatDefinitif() {

		Date dateDebut = new Date();
		Date dateFin = new Date();
		Date dateMaj = new Date();
		Date dateMaj2 = new Date();
		Integer idDemande = 1;

		RefTypeAbsence rta = new RefTypeAbsence();
		rta.setIdRefTypeAbsence(2);

		Demande d = new Demande();
		d.setType(rta);

		DemandeReposComp dr = new DemandeReposComp();

		EtatDemande ed = new EtatDemande();
		ed.setDate(dateMaj);
		ed.setDemande((Demande) dr);
		ed.setEtat(RefEtatEnum.SAISIE);
		ed.setIdAgent(9005138);
		ed.setIdEtatDemande(1);
		EtatDemande ed2 = new EtatDemande();
		ed2.setDate(dateMaj2);
		ed2.setDemande((Demande) dr);
		ed2.setEtat(RefEtatEnum.APPROUVEE);
		ed2.setIdAgent(9005138);
		ed2.setIdEtatDemande(2);
		ed2.setMotif("motif");
		List<EtatDemande> listEtatDemande = new ArrayList<EtatDemande>();
		listEtatDemande.addAll(Arrays.asList(ed2, ed));

		dr.setDateDebut(dateDebut);
		dr.setDateFin(dateFin);
		dr.setDuree(10);
		dr.setDureeAnneeN1(10);
		dr.setEtatsDemande(listEtatDemande);
		dr.setIdAgent(9005138);
		dr.setIdDemande(idDemande);
		dr.setType(rta);

		IDemandeRepository demandeRepo = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepo.getEntity(Demande.class, idDemande)).thenReturn(d);
		Mockito.when(demandeRepo.getEntity(DemandeReposComp.class, idDemande)).thenReturn(dr);

		Date date = new Date();
		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(date);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgentService(d.getIdAgent(), date)).thenReturn(new AgentWithServiceDto());
		Mockito.when(sirhWSConsumer.getAgentService(dr.getIdAgent(), date)).thenReturn(new AgentWithServiceDto());

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepo);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		// When
		DemandeDto result = service.getDemandeDto(idDemande);

		// Then
		assertEquals(result.getDateDebut(), dateDebut);
		assertEquals(result.getDuree().toString(), "20");
		assertEquals(result.getIdAgent(), new Integer(9005138));
		assertEquals(result.getIdDemande(), idDemande);
		assertEquals(result.getIdRefEtat().intValue(), RefEtatEnum.APPROUVEE.getCodeEtat());
		assertEquals(result.getIdTypeDemande().intValue(), RefTypeAbsenceEnum.REPOS_COMP.getValue());
		assertFalse(result.isAffichageBoutonImprimer());
		assertFalse(result.isAffichageBoutonModifier());
		assertFalse(result.isAffichageBoutonSupprimer());
		assertEquals("motif", result.getMotif());
	}
}
