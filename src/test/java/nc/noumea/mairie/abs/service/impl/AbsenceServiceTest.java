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

import javax.persistence.FlushModeType;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeAsa;
import nc.noumea.mairie.abs.domain.DemandeCongesExceptionnels;
import nc.noumea.mairie.abs.domain.DemandeRecup;
import nc.noumea.mairie.abs.domain.DemandeReposComp;
import nc.noumea.mairie.abs.domain.Droit;
import nc.noumea.mairie.abs.domain.DroitDroitsAgent;
import nc.noumea.mairie.abs.domain.DroitProfil;
import nc.noumea.mairie.abs.domain.DroitsAgent;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.ProfilEnum;
import nc.noumea.mairie.abs.domain.RefEtat;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefGroupeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.domain.RefTypeGroupeAbsenceEnum;
import nc.noumea.mairie.abs.domain.RefTypeSaisi;
import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.RefGroupeAbsenceDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IAccessRightsRepository;
import nc.noumea.mairie.abs.repository.IDemandeRepository;
import nc.noumea.mairie.abs.repository.IFiltreRepository;
import nc.noumea.mairie.abs.service.IAbsenceDataConsistencyRules;
import nc.noumea.mairie.abs.service.IAccessRightsService;
import nc.noumea.mairie.abs.service.ICounterService;
import nc.noumea.mairie.abs.service.IFiltreService;
import nc.noumea.mairie.abs.service.counter.impl.CounterServiceFactory;
import nc.noumea.mairie.abs.service.rules.impl.DataConsistencyRulesFactory;
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
						RefTypeAbsenceEnum.RECUP.getValue(), null)).thenReturn(listdemande);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				null);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		List<Demande> listResult = service.getListeNonFiltreeDemandes(idAgentConnecte, null, null, null,
				RefTypeAbsenceEnum.RECUP.getValue(), null);

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
						RefTypeAbsenceEnum.RECUP.getValue(), null)).thenReturn(listdemande);
		Mockito.when(
				demandeRepository.listeDemandesAgent(idApprobateurOfDelegataire, null, null, null,
						RefTypeAbsenceEnum.RECUP.getValue(), null)).thenReturn(listdemandeDeleg);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		List<Demande> listResult = service.getListeNonFiltreeDemandes(idAgentConnecte, null, null, null,
				RefTypeAbsenceEnum.RECUP.getValue(), null);

		assertEquals(3, listResult.size());
	}

	@Test
	public void getDemande_Recup_WithResult_isEtatDefinitif() {

		Date dateDebut = new Date();
		Date dateFin = new Date();
		Date dateMaj = new Date();
		Date dateMaj2 = new Date();
		Integer idDemande = 1;

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.RECUP.getValue());

		RefTypeAbsence rta = new RefTypeAbsence();
		rta.setIdRefTypeAbsence(RefTypeAbsenceEnum.RECUP.getValue());
		rta.setGroupe(groupe);

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
		assertEquals(result.getDuree().toString(), "10.0");
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

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.RECUP.getValue());

		RefTypeAbsence rta = new RefTypeAbsence();
		rta.setIdRefTypeAbsence(RefTypeAbsenceEnum.RECUP.getValue());
		rta.setGroupe(groupe);

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
		assertEquals(result.getDuree().toString(), "10.0");
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

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.RECUP.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.0);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		dto.setGroupeAbsence(groupeAbsence);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(idAgent);
		dto.setAgentWithServiceDto(agDto);
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
		Mockito.when(
				helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.any(Date.class), Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateFin);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeRecup.class), Mockito.isA(Date.class), Mockito.anyBoolean());

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

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				null);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.isA(ReturnMessageDto.class))).thenReturn(new ReturnMessageDto());

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(new RefTypeSaisi());

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

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

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.RECUP.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.0);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		dto.setGroupeAbsence(groupeAbsence);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(idAgent);
		dto.setAgentWithServiceDto(agDto);
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
		Mockito.when(
				helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.any(Date.class), Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateFin);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeRecup.class), Mockito.isA(Date.class), Mockito.anyBoolean());

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

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.isA(ReturnMessageDto.class))).thenReturn(result);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				null);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(new RefTypeSaisi());

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

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
		dto.setDuree(10.0);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(9005139);
		dto.setAgentWithServiceDto(agDto);

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
		Mockito.when(
				helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.any(Date.class), Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateFin);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeRecup.class), Mockito.isA(Date.class), Mockito.anyBoolean());

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
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);

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
		DemandeEtatChangeDto dto4 = new DemandeEtatChangeDto();
		dto4.setIdRefEtat(RefEtatEnum.EN_ATTENTE.getCodeEtat());
		dto4.setDateAvis(new Date());
		dto4.setMotif("motif 4");
		DemandeEtatChangeDto dto5 = new DemandeEtatChangeDto();
		dto5.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());
		dto5.setDateAvis(new Date());
		dto5.setMotif("motif 5");
		DemandeEtatChangeDto dto6 = new DemandeEtatChangeDto();
		dto6.setIdRefEtat(RefEtatEnum.REJETE.getCodeEtat());
		dto6.setDateAvis(new Date());
		dto6.setMotif("motif 6");

		ReturnMessageDto result1 = new ReturnMessageDto();
		ReturnMessageDto result2 = new ReturnMessageDto();
		ReturnMessageDto result3 = new ReturnMessageDto();
		ReturnMessageDto result4 = new ReturnMessageDto();
		ReturnMessageDto result5 = new ReturnMessageDto();
		ReturnMessageDto result6 = new ReturnMessageDto();

		AbsenceService service = new AbsenceService();
		result1 = service.setDemandeEtat(idAgent, dto1);
		result2 = service.setDemandeEtat(idAgent, dto2);
		result3 = service.setDemandeEtat(idAgent, dto3);
		result4 = service.setDemandeEtat(idAgent, dto4);
		result5 = service.setDemandeEtat(idAgent, dto5);
		result6 = service.setDemandeEtat(idAgent, dto6);

		assertEquals(1, result1.getErrors().size());
		assertEquals("L'état de la demande envoyée n'est pas correct.", result1.getErrors().get(0).toString());
		assertEquals(1, result2.getErrors().size());
		assertEquals("L'état de la demande envoyée n'est pas correct.", result2.getErrors().get(0).toString());
		assertEquals(1, result3.getErrors().size());
		assertEquals("L'état de la demande envoyée n'est pas correct.", result3.getErrors().get(0).toString());
		assertEquals(1, result4.getErrors().size());
		assertEquals("L'état de la demande envoyée n'est pas correct.", result4.getErrors().get(0).toString());
		assertEquals(1, result5.getErrors().size());
		assertEquals("L'état de la demande envoyée n'est pas correct.", result5.getErrors().get(0).toString());
		assertEquals(1, result6.getErrors().size());
		assertEquals("L'état de la demande envoyée n'est pas correct.", result6.getErrors().get(0).toString());
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

		RefTypeSaisi typeSaisi = new RefTypeSaisi();

		RefTypeAbsence type = new RefTypeAbsence();
		type.setTypeSaisi(typeSaisi);

		Demande demande = Mockito.spy(new Demande());
		demande.setType(type);

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

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		})
				.when(absDataConsistencyRules)
				.checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(),
						Mockito.anyString());

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		})
				.when(absDataConsistencyRules)
				.checkSaisieKiosqueAutorisee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(RefTypeSaisi.class),
						Mockito.eq(false));

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);

		result = service.setDemandeEtat(idAgent, dto);

		assertEquals(1, result.getErrors().size());
		assertEquals("Erreur etat incorrect", result.getErrors().get(0).toString());
		Mockito.verify(demande, Mockito.times(0)).addEtatDemande(Mockito.isA(EtatDemande.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setDemandeEtat_setDemandeEtatVisa_saisieKiosqueNonAutorisee() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = new ReturnMessageDto();

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat());
		dto.setIdDemande(1);

		RefTypeSaisi typeSaisi = new RefTypeSaisi();

		RefTypeAbsence type = new RefTypeAbsence();
		type.setTypeSaisi(typeSaisi);

		Demande demande = Mockito.spy(new Demande());
		demande.setType(type);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(demande);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isViseurOfAgent(idAgent, demande.getIdAgent())).thenReturn(true);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
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
				return result;
			}
		})
				.when(absDataConsistencyRules)
				.checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(),
						Mockito.anyString());

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				result.getErrors().add("Erreur saisie kiosque");
				return result;
			}
		})
				.when(absDataConsistencyRules)
				.checkSaisieKiosqueAutorisee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(RefTypeSaisi.class),
						Mockito.eq(false));

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);

		result = service.setDemandeEtat(idAgent, dto);

		assertEquals(1, result.getErrors().size());
		assertEquals("Erreur saisie kiosque", result.getErrors().get(0).toString());
		Mockito.verify(demande, Mockito.times(0)).addEtatDemande(Mockito.isA(EtatDemande.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setDemandeEtat_setDemandeEtatVisa_motifNull() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = new ReturnMessageDto();

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat());
		dto.setIdDemande(1);

		RefTypeSaisi typeSaisi = new RefTypeSaisi();

		RefTypeAbsence type = new RefTypeAbsence();
		type.setTypeSaisi(typeSaisi);

		Demande demande = Mockito.spy(new Demande());
		demande.setType(type);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(demande);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isViseurOfAgent(idAgent, demande.getIdAgent())).thenReturn(true);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
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
				String motif = (String) args[2];
				if (null == motif) {
					result.getErrors().add("Le motif est obligatoire.");
				}
				return result;
			}
		})
				.when(absDataConsistencyRules)
				.checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(),
						Mockito.anyString());

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		})
				.when(absDataConsistencyRules)
				.checkSaisieKiosqueAutorisee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(RefTypeSaisi.class),
						Mockito.eq(false));

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);

		result = service.setDemandeEtat(idAgent, dto);

		assertEquals(1, result.getErrors().size());
		assertEquals("Le motif est obligatoire.", result.getErrors().get(0).toString());
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

		RefTypeSaisi typeSaisi = new RefTypeSaisi();

		RefTypeAbsence type = new RefTypeAbsence();
		type.setTypeSaisi(typeSaisi);

		Demande demande = Mockito.spy(new Demande());
		demande.setType(type);

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

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		})
				.when(absDataConsistencyRules)
				.checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(),
						Mockito.anyString());

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		})
				.when(absDataConsistencyRules)
				.checkSaisieKiosqueAutorisee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(RefTypeSaisi.class),
						Mockito.eq(false));

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);

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

		RefTypeSaisi typeSaisi = new RefTypeSaisi();

		RefTypeAbsence type = new RefTypeAbsence();
		type.setTypeSaisi(typeSaisi);

		Demande demande = Mockito.spy(new Demande());
		demande.setType(type);

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

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				result.getErrors().add("Erreur saisie kiosque incorrect");
				return result;
			}
		})
				.when(absDataConsistencyRules)
				.checkSaisieKiosqueAutorisee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(RefTypeSaisi.class),
						Mockito.eq(false));

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);

		result = service.setDemandeEtat(idAgent, dto);

		assertEquals(3, result.getErrors().size());
		assertEquals("Erreur etat incorrect", result.getErrors().get(0).toString());
		assertEquals("Erreur motif incorrect", result.getErrors().get(1).toString());
		assertEquals("Erreur saisie kiosque incorrect", result.getErrors().get(2).toString());
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

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.REPOS_COMP.getValue());

		RefTypeSaisi typeSaisi = new RefTypeSaisi();

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.REPOS_COMP.getValue());
		type.setGroupe(groupe);
		type.setTypeSaisi(typeSaisi);

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

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		})
				.when(absDataConsistencyRules)
				.checkSaisieKiosqueAutorisee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(RefTypeSaisi.class),
						Mockito.eq(false));

		ICounterService counterService = Mockito.mock(ICounterService.class);
		Mockito.when(
				counterService.majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(DemandeEtatChangeDto.class))).thenReturn(srm);

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(
				counterServiceFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande
						.getType().getIdRefTypeAbsence())).thenReturn(counterService);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		// ReflectionTestUtils.setField(service, "counterService",
		// counterService);
		ReflectionTestUtils.setField(service, "counterServiceFactory", counterServiceFactory);

		result = service.setDemandeEtat(idAgent, dto);

		assertEquals(1, result.getErrors().size());
		assertEquals("erreur maj compteur", result.getErrors().get(0).toString());
		Mockito.verify(demande, Mockito.times(0)).addEtatDemande(Mockito.isA(EtatDemande.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setDemandeEtat_setDemandeEtatApprouve_OK_noMajCompteur_ASA48() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = null;

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());
		dto.setIdDemande(1);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		RefTypeSaisi typeSaisi = new RefTypeSaisi();

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A48.getValue());
		type.setGroupe(groupe);
		type.setTypeSaisi(typeSaisi);

		DemandeAsa demande = Mockito.spy(new DemandeAsa());
		demande.setDuree(10.0);
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
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		})
				.when(counterService)
				.majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(DemandeEtatChangeDto.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		})
				.when(absDataConsistencyRules)
				.checkSaisieKiosqueAutorisee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(RefTypeSaisi.class),
						Mockito.eq(false));

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(
				counterServiceFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande
						.getType().getIdRefTypeAbsence())).thenReturn(counterService);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		// ReflectionTestUtils.setField(service, "counterService",
		// counterService);
		ReflectionTestUtils.setField(service, "counterServiceFactory", counterServiceFactory);

		result = service.setDemandeEtat(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demande, Mockito.times(1)).addEtatDemande(Mockito.isA(EtatDemande.class));
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

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.RECUP.getValue());

		RefTypeSaisi typeSaisi = new RefTypeSaisi();

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.RECUP.getValue());
		type.setGroupe(groupe);
		type.setTypeSaisi(typeSaisi);

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

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		})
				.when(absDataConsistencyRules)
				.checkSaisieKiosqueAutorisee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(RefTypeSaisi.class),
						Mockito.eq(false));

		ICounterService counterService = Mockito.mock(ICounterService.class);
		Mockito.when(
				counterService.majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(DemandeEtatChangeDto.class))).thenReturn(srm);

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(
				counterServiceFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande
						.getType().getIdRefTypeAbsence())).thenReturn(counterService);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		// ReflectionTestUtils.setField(service, "counterService",
		// counterService);
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
		dto.setMotif(null);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.RECUP.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.RECUP.getValue());
		type.setGroupe(groupe);

		Demande demande = Mockito.spy(new Demande());
		demande.setIdAgent(idAgent);
		demande.setType(type);
		demande.setDateDebut(new DateTime(2014, 1, 26, 15, 16, 35).toDate());

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
				.checkEtatsDemandeAnnulee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(List.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		})
				.when(absDataConsistencyRules)
				.checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(),
						Mockito.anyString());

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.isA(ReturnMessageDto.class))).thenReturn(new ReturnMessageDto());

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

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

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.REPOS_COMP.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.REPOS_COMP.getValue());
		type.setGroupe(groupe);

		DemandeReposComp demande = Mockito.spy(new DemandeReposComp());
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
				.checkEtatsDemandeAnnulee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(List.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		})
				.when(absDataConsistencyRules)
				.checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(),
						Mockito.anyString());

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		ICounterService counterService = Mockito.mock(ICounterService.class);
		Mockito.when(
				counterService.majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(DemandeEtatChangeDto.class))).thenReturn(srm);

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(
				counterServiceFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande
						.getType().getIdRefTypeAbsence())).thenReturn(counterService);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.isA(ReturnMessageDto.class))).thenReturn(new ReturnMessageDto());

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		// ReflectionTestUtils.setField(service, "counterService",
		// counterService);
		ReflectionTestUtils.setField(service, "counterServiceFactory", counterServiceFactory);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

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

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(2);
		type.setGroupe(groupe);

		DemandeReposComp demande = Mockito.spy(new DemandeReposComp());
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
				.checkEtatsDemandeAnnulee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(List.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		})
				.when(absDataConsistencyRules)
				.checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(),
						Mockito.anyString());

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		ICounterService counterService = Mockito.mock(ICounterService.class);
		Mockito.when(
				counterService.majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(DemandeEtatChangeDto.class))).thenReturn(srm);

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(
				counterServiceFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande
						.getType().getIdRefTypeAbsence())).thenReturn(counterService);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.isA(ReturnMessageDto.class))).thenReturn(new ReturnMessageDto());

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		// ReflectionTestUtils.setField(service, "counterService",
		// counterService);
		ReflectionTestUtils.setField(service, "counterServiceFactory", counterServiceFactory);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.setDemandeEtat(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		assertEquals("La demande est annulée.", result.getInfos().get(0));
		Mockito.verify(demande, Mockito.times(1)).addEtatDemande(Mockito.isA(EtatDemande.class));
	}

	@Test
	public void setDemandeEtatPris_EtatIncorrect_ReturnError_RecupReposComp() {

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.RECUP.getValue());

		RefTypeAbsence typeRecup = new RefTypeAbsence();
		typeRecup.setLabel("Récupération");
		typeRecup.setIdRefTypeAbsence(3);
		typeRecup.setGroupe(groupe);

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
	public void setDemandeEtatPris_EtatIsOk_AddEtatDemande_Recup_ReposComp() {

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.RECUP.getValue());

		RefTypeAbsence typeRecup = new RefTypeAbsence();
		typeRecup.setLabel("Récupération");
		typeRecup.setIdRefTypeAbsence(3);
		typeRecup.setGroupe(groupe);

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

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.REPOS_COMP.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.0);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.REPOS_COMP.getValue());
		dto.setGroupeAbsence(groupeAbsence);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(idAgent);
		dto.setAgentWithServiceDto(agDto);
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
		Mockito.when(
				helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.any(Date.class), Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateFin);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeReposComp.class), Mockito.isA(Date.class), Mockito.anyBoolean());

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

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				null);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.isA(ReturnMessageDto.class))).thenReturn(new ReturnMessageDto());

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(new RefTypeSaisi());

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

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

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.REPOS_COMP.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.5);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.REPOS_COMP.getValue());
		dto.setGroupeAbsence(groupeAbsence);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(idAgent);
		dto.setAgentWithServiceDto(agDto);
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
		Mockito.when(
				helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.any(Date.class), Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateFin);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeReposComp.class), Mockito.isA(Date.class), Mockito.anyBoolean());

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

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.isA(ReturnMessageDto.class))).thenReturn(result);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				null);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(new RefTypeSaisi());

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

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
		dto.setDuree(10.0);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.REPOS_COMP.getValue());
		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(9005139);
		dto.setAgentWithServiceDto(agDto);

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
		Mockito.when(
				helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.any(Date.class), Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateFin);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeReposComp.class), Mockito.isA(Date.class), Mockito.anyBoolean());

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
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);

		result = service.saveDemande(idAgent, dto);

		assertEquals(1, result.getErrors().size());
		assertEquals("erreur droit", result.getErrors().get(0).toString());
		Mockito.verify(demandeRepository, Mockito.times(0)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void saveDemandeAsa48_OK_avecEtatProvisoire() {

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.5);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.ASA_A48.getValue());
		dto.setGroupeAbsence(groupeAbsence);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(idAgent);

		dto.setAgentWithServiceDto(agDto);
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

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setDuree(true);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isUserOperateur(idAgent)).thenReturn(true);
		Mockito.when(accessRightsRepository.getAgentDroitFetchAgents(idAgent)).thenReturn(droitOperateur);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(DemandeAsa.class, dto.getIdDemande())).thenReturn(new DemandeAsa());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeAsa obj = (DemandeAsa) args[0];

				assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(obj.getDuree(), 10, 5);

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeAsa.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.any(Date.class), Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateFin);
		Mockito.when(
				helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeAsa.class), Mockito.isA(Date.class), Mockito.anyBoolean());

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

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				null);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.isA(ReturnMessageDto.class))).thenReturn(new ReturnMessageDto());

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.saveDemande(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void saveDemandeAsa48_OK_avecEtatSaisie() {

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.0);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.ASA_A48.getValue());
		dto.setGroupeAbsence(groupeAbsence);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(idAgent);
		dto.setAgentWithServiceDto(agDto);
		dto.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());

		Droit droitOperateur = new Droit();
		DroitDroitsAgent droitDroitAgent = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(idAgent);
		droitDroitAgent.setDroitsAgent(droitsAgent);
		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.add(droitDroitAgent);
		droitOperateur.setDroitDroitsAgent(droitDroitsAgent);

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setDuree(true);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isUserOperateur(idAgent)).thenReturn(true);
		Mockito.when(accessRightsRepository.getAgentDroitFetchAgents(idAgent)).thenReturn(droitOperateur);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(DemandeAsa.class, dto.getIdDemande())).thenReturn(new DemandeAsa());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeAsa obj = (DemandeAsa) args[0];

				assertEquals(RefEtatEnum.SAISIE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(obj.getDuree(), 10, 5);

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeAsa.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.any(Date.class), Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateFin);
		Mockito.when(
				helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeAsa.class), Mockito.isA(Date.class), Mockito.anyBoolean());

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

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.isA(ReturnMessageDto.class))).thenReturn(result);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				null);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.saveDemande(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void saveDemandeAsa_Ko() {

		ReturnMessageDto result = new ReturnMessageDto();
		result.getErrors().add("erreur droit");

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.0);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.REPOS_COMP.getValue());
		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(9005139);
		dto.setAgentWithServiceDto(agDto);

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
				DemandeAsa obj = (DemandeAsa) args[0];

				assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(10, obj.getDuree().intValue());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeAsa.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.any(Date.class), Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateFin);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeAsa.class), Mockito.isA(Date.class), Mockito.anyBoolean());

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
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);

		result = service.saveDemande(idAgent, dto);

		assertEquals(1, result.getErrors().size());
		assertEquals("erreur droit", result.getErrors().get(0).toString());
		Mockito.verify(demandeRepository, Mockito.times(0)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void saveDemandeCongesExcep_OK_avecEtatProvisoire() {

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_EXCEP.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.5);
		dto.setIdTypeDemande(24);
		dto.setGroupeAbsence(groupeAbsence);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(idAgent);

		dto.setAgentWithServiceDto(agDto);
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

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setDuree(true);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isUserOperateur(idAgent)).thenReturn(true);
		Mockito.when(accessRightsRepository.getAgentDroitFetchAgents(idAgent)).thenReturn(droitOperateur);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(DemandeCongesExceptionnels.class, dto.getIdDemande())).thenReturn(
				new DemandeCongesExceptionnels());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeCongesExceptionnels obj = (DemandeCongesExceptionnels) args[0];

				assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(obj.getDuree(), 10, 5);

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeCongesExceptionnels.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.any(Date.class), Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateFin);
		Mockito.when(
				helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeCongesExceptionnels.class), Mockito.isA(Date.class), Mockito.anyBoolean());

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

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				null);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.isA(ReturnMessageDto.class))).thenReturn(new ReturnMessageDto());

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.saveDemande(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void saveDemandeCongesExcep_OK_avecEtatSaisie() {

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_EXCEP.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.0);
		dto.setGroupeAbsence(groupeAbsence);
		dto.setIdTypeDemande(24);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(idAgent);
		dto.setAgentWithServiceDto(agDto);
		dto.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());

		Droit droitOperateur = new Droit();
		DroitDroitsAgent droitDroitAgent = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(idAgent);
		droitDroitAgent.setDroitsAgent(droitsAgent);
		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.add(droitDroitAgent);
		droitOperateur.setDroitDroitsAgent(droitDroitsAgent);

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setDuree(true);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isUserOperateur(idAgent)).thenReturn(true);
		Mockito.when(accessRightsRepository.getAgentDroitFetchAgents(idAgent)).thenReturn(droitOperateur);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(DemandeCongesExceptionnels.class, dto.getIdDemande())).thenReturn(
				new DemandeCongesExceptionnels());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeCongesExceptionnels obj = (DemandeCongesExceptionnels) args[0];

				assertEquals(RefEtatEnum.SAISIE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(obj.getDuree(), 10, 5);

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeCongesExceptionnels.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.any(Date.class), Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateFin);
		Mockito.when(
				helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeAsa.class), Mockito.isA(Date.class), Mockito.anyBoolean());

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

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.isA(ReturnMessageDto.class))).thenReturn(result);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				null);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.saveDemande(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void saveDemandeCongesExcep_Ko() {

		ReturnMessageDto result = new ReturnMessageDto();
		result.getErrors().add("erreur droit");

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_EXCEP.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.0);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(9005139);
		dto.setAgentWithServiceDto(agDto);

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
				DemandeCongesExceptionnels obj = (DemandeCongesExceptionnels) args[0];

				assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(10, obj.getDuree().intValue());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeCongesExceptionnels.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.any(Date.class), Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateFin);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeCongesExceptionnels.class), Mockito.isA(Date.class), Mockito.anyBoolean());

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
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);

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

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.REPOS_COMP.getValue());

		RefTypeAbsence rta = new RefTypeAbsence();
		rta.setIdRefTypeAbsence(RefTypeAbsenceEnum.REPOS_COMP.getValue());
		rta.setGroupe(groupe);

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
		assertEquals(result.getDuree().toString(), "20.0");
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

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.REPOS_COMP.getValue());

		RefTypeAbsence rta = new RefTypeAbsence();
		rta.setIdRefTypeAbsence(RefTypeAbsenceEnum.REPOS_COMP.getValue());
		rta.setGroupe(groupe);

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
		assertEquals(result.getDuree().toString(), "20.0");
		assertEquals(result.getIdDemande(), idDemande);
		assertEquals(result.getIdRefEtat().intValue(), RefEtatEnum.APPROUVEE.getCodeEtat());
		assertEquals(result.getIdTypeDemande().intValue(), RefTypeAbsenceEnum.REPOS_COMP.getValue());
		assertFalse(result.isAffichageBoutonImprimer());
		assertFalse(result.isAffichageBoutonModifier());
		assertFalse(result.isAffichageBoutonSupprimer());
		assertEquals("motif", result.getMotif());
	}

	@Test
	public void getDemande_CongesExcep_WithResult_isEtatDefinitif() {

		Date dateDebut = new Date();
		Date dateFin = new Date();
		Date dateMaj = new Date();
		Date dateMaj2 = new Date();
		Integer idDemande = 1;

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_EXCEP.getValue());

		RefTypeAbsence rta = new RefTypeAbsence();
		rta.setIdRefTypeAbsence(26);
		rta.setGroupe(groupe);

		Demande d = new Demande();
		d.setType(rta);

		DemandeCongesExceptionnels dr = new DemandeCongesExceptionnels();

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
		dr.setDuree(10.0);
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
		Mockito.when(demandeRepo.getEntity(DemandeCongesExceptionnels.class, idDemande)).thenReturn(dr);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", helperService);

		// When
		DemandeDto result = service.getDemandeDto(idDemande);

		// Then
		assertEquals(result.getDateDebut(), dateDebut);
		assertEquals(result.getDuree().toString(), "10.0");
		assertEquals(result.getIdDemande(), idDemande);
		assertEquals(result.getIdRefEtat().intValue(), RefEtatEnum.SAISIE.getCodeEtat());
		assertEquals(result.getIdTypeDemande().intValue(), 26);
		assertFalse(result.isAffichageBoutonImprimer());
		assertFalse(result.isAffichageBoutonModifier());
		assertFalse(result.isAffichageBoutonSupprimer());
		assertEquals("motif", result.getMotif());
	}

	@Test
	public void getDemande_CongesExceptionnels_WithResult_isNoEtatDefinitif() {

		Date dateDebut = new Date();
		Date dateFin = new Date();
		Date dateMaj = new Date();
		Date dateMaj2 = new Date();
		Integer idDemande = 1;

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_EXCEP.getValue());

		RefTypeAbsence rta = new RefTypeAbsence();
		rta.setIdRefTypeAbsence(26);
		rta.setGroupe(groupe);

		Demande d = new Demande();
		d.setType(rta);

		DemandeCongesExceptionnels dr = new DemandeCongesExceptionnels();

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
		dr.setDuree(10.0);
		dr.setEtatsDemande(listEtatDemande);
		dr.setIdAgent(9005138);
		dr.setIdDemande(idDemande);
		dr.setType(rta);

		IDemandeRepository demandeRepo = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepo.getEntity(Demande.class, idDemande)).thenReturn(d);
		Mockito.when(demandeRepo.getEntity(DemandeCongesExceptionnels.class, idDemande)).thenReturn(dr);

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
		assertEquals(result.getDuree().toString(), "10.0");
		assertEquals(result.getIdDemande(), idDemande);
		assertEquals(result.getIdRefEtat().intValue(), RefEtatEnum.APPROUVEE.getCodeEtat());
		assertEquals(result.getIdTypeDemande().intValue(), 26);
		assertFalse(result.isAffichageBoutonImprimer());
		assertFalse(result.isAffichageBoutonModifier());
		assertFalse(result.isAffichageBoutonSupprimer());
		assertEquals("motif", result.getMotif());
	}

	@Test
	public void saveDemandeSIRHReposComp_OK_avecEtatProvisoire() {

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.REPOS_COMP.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.0);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.REPOS_COMP.getValue());
		dto.setGroupeAbsence(groupeAbsence);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(idAgent);
		dto.setAgentWithServiceDto(agDto);
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

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(Mockito.anyInt())).thenReturn(result);

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
		Mockito.when(
				helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.any(Date.class), Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateFin);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeReposComp.class), Mockito.isA(Date.class), Mockito.anyBoolean());

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

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(new RefTypeSaisi());

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.saveDemandeSIRH(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void saveDemandeSIRHReposComp_OK_avecEtatSaisie() {

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.REPOS_COMP.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.5);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.REPOS_COMP.getValue());
		dto.setGroupeAbsence(groupeAbsence);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(idAgent);
		dto.setAgentWithServiceDto(agDto);
		dto.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());

		Droit droitOperateur = new Droit();
		DroitDroitsAgent droitDroitAgent = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(idAgent);
		droitDroitAgent.setDroitsAgent(droitsAgent);
		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.add(droitDroitAgent);
		droitOperateur.setDroitDroitsAgent(droitDroitsAgent);

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
		Mockito.when(
				helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.any(Date.class), Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateFin);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeReposComp.class), Mockito.isA(Date.class), Mockito.anyBoolean());

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

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(Mockito.anyInt())).thenReturn(result);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(new RefTypeSaisi());

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.saveDemandeSIRH(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void saveDemandeSIRHCongesExcep_OK_avecEtatSaisie_ParametrageSaisieKiosqueTrue() {

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_EXCEP.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.5);
		dto.setIdTypeDemande(26);
		dto.setGroupeAbsence(groupeAbsence);
		dto.setDateDebutPM(true);
		dto.setDateFinPM(true);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(idAgent);
		dto.setAgentWithServiceDto(agDto);
		dto.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());

		Droit droitOperateur = new Droit();
		DroitDroitsAgent droitDroitAgent = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(idAgent);
		droitDroitAgent.setDroitsAgent(droitsAgent);
		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.add(droitDroitAgent);
		droitOperateur.setDroitDroitsAgent(droitDroitsAgent);

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setDuree(true);
		typeSaisi.setChkDateDebut(true);
		typeSaisi.setChkDateFin(true);
		typeSaisi.setSaisieKiosque(true);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(DemandeCongesExceptionnels.class, dto.getIdDemande())).thenReturn(
				new DemandeCongesExceptionnels());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeCongesExceptionnels obj = (DemandeCongesExceptionnels) args[0];

				assertEquals(1, obj.getEtatsDemande().size());
				assertEquals(RefEtatEnum.SAISIE.getCodeEtat(), obj.getLatestEtatDemande().getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(obj.getDuree(), 10, 5);
				assertFalse(obj.isDateDebutAM());
				assertFalse(obj.isDateFinAM());
				assertTrue(obj.isDateDebutPM());
				assertTrue(obj.isDateFinPM());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeCongesExceptionnels.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.any(Date.class), Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateFin);
		Mockito.when(
				helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeCongesExceptionnels.class), Mockito.isA(Date.class), Mockito.anyBoolean());

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

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(Mockito.anyInt())).thenReturn(result);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.saveDemandeSIRH(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void saveDemandeSIRHCongesExcep_OK_avecEtatValide_ParametrageSaisieKiosqueFalse() {

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_EXCEP.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.5);
		dto.setIdTypeDemande(26);
		dto.setGroupeAbsence(groupeAbsence);
		dto.setDateDebutPM(true);
		dto.setDateFinPM(true);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(idAgent);
		dto.setAgentWithServiceDto(agDto);
		dto.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());

		Droit droitOperateur = new Droit();
		DroitDroitsAgent droitDroitAgent = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(idAgent);
		droitDroitAgent.setDroitsAgent(droitsAgent);
		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.add(droitDroitAgent);
		droitOperateur.setDroitDroitsAgent(droitDroitsAgent);

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setDuree(true);
		typeSaisi.setChkDateDebut(true);
		typeSaisi.setChkDateFin(true);
		typeSaisi.setSaisieKiosque(false);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(DemandeCongesExceptionnels.class, dto.getIdDemande())).thenReturn(
				new DemandeCongesExceptionnels());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeCongesExceptionnels obj = (DemandeCongesExceptionnels) args[0];

				assertEquals(2, obj.getEtatsDemande().size());
				assertEquals(RefEtatEnum.VALIDEE.getCodeEtat(), obj.getEtatsDemande().get(1).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(obj.getDuree(), 10, 5);
				assertFalse(obj.isDateDebutAM());
				assertFalse(obj.isDateFinAM());
				assertTrue(obj.isDateDebutPM());
				assertTrue(obj.isDateFinPM());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeCongesExceptionnels.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.any(Date.class), Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateFin);
		Mockito.when(
				helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeCongesExceptionnels.class), Mockito.isA(Date.class), Mockito.anyBoolean());

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

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(Mockito.anyInt())).thenReturn(result);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.saveDemandeSIRH(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void saveDemandeSIRHReposComp_Ko() {

		ReturnMessageDto result = new ReturnMessageDto();
		result.getErrors().add("erreur droit");

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.0);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.REPOS_COMP.getValue());
		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(9005139);
		dto.setAgentWithServiceDto(agDto);

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
		Mockito.when(
				helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.any(Date.class), Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateFin);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeReposComp.class), Mockito.isA(Date.class), Mockito.anyBoolean());

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

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(Mockito.anyInt())).thenReturn(result);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);

		result = service.saveDemandeSIRH(idAgent, dto);

		assertEquals(1, result.getErrors().size());
		assertEquals("L'agent n'est pas habilité à saisir une demande.", result.getErrors().get(0).toString());
		Mockito.verify(demandeRepository, Mockito.times(0)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void saveDemandeSIRHAsa48_OK_avecEtatProvisoire() {

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDateFin(dateFin);
		dto.setDuree(10.5);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.ASA_A48.getValue());
		dto.setGroupeAbsence(groupeAbsence);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(idAgent);

		dto.setAgentWithServiceDto(agDto);
		dto.setIdRefEtat(0);
		dto.setDateDemande(new Date());
		dto.setAffichageBoutonModifier(false);
		dto.setAffichageBoutonSupprimer(false);
		dto.setAffichageBoutonImprimer(false);

		dto.setDateDebutAM(false);
		dto.setDateDebutPM(true);
		dto.setDateFinAM(false);
		dto.setDateFinPM(true);

		Droit droitOperateur = new Droit();
		DroitDroitsAgent droitDroitAgent = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(idAgent);
		droitDroitAgent.setDroitsAgent(droitsAgent);

		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.add(droitDroitAgent);
		droitOperateur.setDroitDroitsAgent(droitDroitsAgent);

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setDuree(true);
		typeSaisi.setChkDateDebut(true);
		typeSaisi.setChkDateFin(true);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(DemandeAsa.class, dto.getIdDemande())).thenReturn(new DemandeAsa());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeAsa obj = (DemandeAsa) args[0];

				assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(obj.getDuree(), 10, 5);
				assertFalse(obj.isDateDebutAM());
				assertFalse(obj.isDateFinAM());
				assertTrue(obj.isDateDebutPM());
				assertTrue(obj.isDateFinPM());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeAsa.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.any(Date.class), Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateFin);
		Mockito.when(
				helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeAsa.class), Mockito.isA(Date.class), Mockito.anyBoolean());

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

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(Mockito.anyInt())).thenReturn(result);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.saveDemandeSIRH(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void saveDemandeSIRHAsa48_OK_avecEtatSaisie() {

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.5);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.ASA_A48.getValue());
		dto.setGroupeAbsence(groupeAbsence);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(idAgent);

		dto.setAgentWithServiceDto(agDto);
		dto.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());

		dto.setDateDebutAM(false);
		dto.setDateDebutPM(true);
		dto.setDateFinAM(false);
		dto.setDateFinPM(true);

		Droit droitOperateur = new Droit();
		DroitDroitsAgent droitDroitAgent = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(idAgent);
		droitDroitAgent.setDroitsAgent(droitsAgent);

		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.add(droitDroitAgent);
		droitOperateur.setDroitDroitsAgent(droitDroitsAgent);

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setDuree(true);
		typeSaisi.setChkDateDebut(false);
		typeSaisi.setChkDateFin(false);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(DemandeAsa.class, dto.getIdDemande())).thenReturn(new DemandeAsa());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeAsa obj = (DemandeAsa) args[0];

				assertEquals(RefEtatEnum.SAISIE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(10, obj.getDuree().intValue());
				assertFalse(obj.isDateDebutAM());
				assertFalse(obj.isDateFinAM());
				assertFalse(obj.isDateDebutPM());
				assertFalse(obj.isDateFinPM());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeAsa.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.any(Date.class), Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateFin);
		Mockito.when(
				helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeAsa.class), Mockito.isA(Date.class), Mockito.anyBoolean());

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

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(Mockito.anyInt())).thenReturn(result);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.saveDemandeSIRH(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void saveDemandeSIRHAsa_Ko() {

		ReturnMessageDto result = new ReturnMessageDto();
		result.getErrors().add("erreur droit");

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.5);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.REPOS_COMP.getValue());
		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(9005139);
		dto.setAgentWithServiceDto(agDto);

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
				DemandeAsa obj = (DemandeAsa) args[0];

				assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(10, obj.getDuree().intValue());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeAsa.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.any(Date.class), Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateFin);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeAsa.class), Mockito.isA(Date.class), Mockito.anyBoolean());

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

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(Mockito.anyInt())).thenReturn(result);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);

		result = service.saveDemandeSIRH(idAgent, dto);

		assertEquals(1, result.getErrors().size());
		assertEquals("L'agent n'est pas habilité à saisir une demande.", result.getErrors().get(0).toString());
		Mockito.verify(demandeRepository, Mockito.times(0)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void saveDemandeSIRHRecup_OK_avecEtatProvisoire() {

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.RECUP.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.0);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		dto.setGroupeAbsence(groupeAbsence);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(idAgent);
		dto.setAgentWithServiceDto(agDto);
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
		Mockito.when(
				helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.any(Date.class), Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateFin);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeRecup.class), Mockito.isA(Date.class), Mockito.anyBoolean());

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

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(Mockito.anyInt())).thenReturn(result);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(new RefTypeSaisi());

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.saveDemandeSIRH(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void saveDemandeSIRHRecup_OK_avecEtatSaisie() {

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.RECUP.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.0);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		dto.setGroupeAbsence(groupeAbsence);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(idAgent);
		dto.setAgentWithServiceDto(agDto);
		dto.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());

		Droit droitOperateur = new Droit();
		DroitDroitsAgent droitDroitAgent = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(idAgent);
		droitDroitAgent.setDroitsAgent(droitsAgent);
		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.add(droitDroitAgent);
		droitOperateur.setDroitDroitsAgent(droitDroitsAgent);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(Mockito.anyInt())).thenReturn(result);

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
		Mockito.when(
				helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.any(Date.class), Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateFin);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeRecup.class), Mockito.isA(Date.class), Mockito.anyBoolean());

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

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(new RefTypeSaisi());

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.saveDemandeSIRH(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void saveDemandeSIRHRecup_Ko() {

		ReturnMessageDto result = new ReturnMessageDto();
		result.getErrors().add("erreur droit");

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.0);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(9005139);
		dto.setAgentWithServiceDto(agDto);

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
		Mockito.when(
				helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.any(Date.class), Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateFin);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeRecup.class), Mockito.isA(Date.class), Mockito.anyBoolean());

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

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(Mockito.anyInt())).thenReturn(result);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.saveDemandeSIRH(idAgent, dto);

		assertEquals(1, result.getErrors().size());
		assertEquals("L'agent n'est pas habilité à saisir une demande.", result.getErrors().get(0).toString());
		Mockito.verify(demandeRepository, Mockito.times(0)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void getListeDemandesSIRH_return1Liste_WithA48() {

		List<Demande> listdemande = new ArrayList<Demande>();
		Demande d = new Demande();
		d.setIdDemande(1);
		listdemande.add(d);

		List<DemandeDto> listdemandeDto = new ArrayList<DemandeDto>();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.ASA_A48.getValue());
		dto.setGroupeAbsence(groupeAbsence);

		listdemandeDto.add(dto);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.listeDemandesSIRH(null, null, null, null, null, null)).thenReturn(listdemande);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.when(absDataConsistencyRules.filtreDateAndEtatDemandeFromList(listdemande, null, null)).thenReturn(
				listdemandeDto);
		Mockito.when(absDataConsistencyRules.filtreDroitOfDemandeSIRH(dto)).thenReturn(dto);
		Mockito.when(absDataConsistencyRules.checkDepassementCompteurAgent(dto)).thenReturn(true);

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		List<DemandeDto> listResult = service.getListeDemandesSIRH(null, null, null, null, null, null, false);

		assertEquals(1, listResult.size());
		assertTrue(listResult.get(0).isDepassementCompteur());

	}

	@Test
	public void getDemandesArchives_return1Liste() {

		List<Demande> listdemande = new ArrayList<Demande>();
		listdemande.add(new Demande());

		List<DemandeDto> listdemandeDto = new ArrayList<DemandeDto>();
		listdemandeDto.add(new DemandeDto());

		Date dateDebut = new Date();
		Date dateFin = new Date();
		Date dateMaj = new Date();
		Date dateMaj2 = new Date();
		Integer idDemande = 1;

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.RECUP.getValue());

		RefTypeAbsence rta = new RefTypeAbsence();
		rta.setIdRefTypeAbsence(RefTypeAbsenceEnum.RECUP.getValue());
		rta.setGroupe(groupe);

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

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, idDemande)).thenReturn(dr);

		Date date = new Date();
		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(date);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgentService(dr.getIdAgent(), date)).thenReturn(new AgentWithServiceDto());

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", helperService);

		List<DemandeDto> listResult = service.getDemandesArchives(idDemande);

		assertEquals(2, listResult.size());
	}

	@Test
	public void setDemandeEtatSIRH_etatIncorrect() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = new ReturnMessageDto();

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.PROVISOIRE.getCodeEtat());

		ReturnMessageDto messageAgent = new ReturnMessageDto();
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(messageAgent);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		result = service.setDemandeEtatSIRH(idAgent, Arrays.asList(demandeEtatChangeDto));

		assertEquals(1, result.getErrors().size());
		assertEquals("L'état de la demande envoyée n'est pas correct.", result.getErrors().get(0));
	}

	@Test
	public void setDemandeEtatSIRH_demandeInexistante() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = new ReturnMessageDto();

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.EN_ATTENTE.getCodeEtat());
		demandeEtatChangeDto.setIdDemande(1);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, demandeEtatChangeDto.getIdDemande())).thenReturn(null);

		ReturnMessageDto messageAgent = new ReturnMessageDto();
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(messageAgent);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		result = service.setDemandeEtatSIRH(idAgent, Arrays.asList(demandeEtatChangeDto));

		assertEquals(1, result.getErrors().size());
		assertEquals("La demande n'existe pas.", result.getErrors().get(0));
	}

	@Test
	public void setDemandeEtatSIRH_demandeEtatInchange() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = new ReturnMessageDto();

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.EN_ATTENTE.getCodeEtat());
		demandeEtatChangeDto.setIdDemande(1);

		EtatDemande etatDemande = new EtatDemande();
		etatDemande.setEtat(RefEtatEnum.EN_ATTENTE);

		Demande demande = new Demande();
		demande.addEtatDemande(etatDemande);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, demandeEtatChangeDto.getIdDemande())).thenReturn(
				demande);

		ReturnMessageDto messageAgent = new ReturnMessageDto();
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(messageAgent);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		result = service.setDemandeEtatSIRH(idAgent, Arrays.asList(demandeEtatChangeDto));

		assertEquals(1, result.getErrors().size());
		assertEquals("L'état de la demande est inchangé.", result.getErrors().get(0));
	}

	@Test
	public void setDemandeEtatSIRH_AgentSIRHNonHabilite() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = new ReturnMessageDto();

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());
		dto.setIdDemande(1);

		Demande demande = Mockito.spy(new Demande());

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(demande);

		ReturnMessageDto messageAgent = new ReturnMessageDto();
		messageAgent.getErrors().add("erreur");

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(messageAgent);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		result = service.setDemandeEtatSIRH(idAgent, Arrays.asList(dto));

		assertEquals(1, result.getErrors().size());
		assertEquals("L'agent n'est pas habilité à valider ou rejeter la demande de cet agent.", result.getErrors()
				.get(0).toString());
		Mockito.verify(demande, Mockito.times(0)).addEtatDemande(Mockito.isA(EtatDemande.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setDemandeEtatSIRH_setDemandeEtatRejete_checkEtatDemande() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = new ReturnMessageDto();

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.REJETE.getCodeEtat());
		dto.setIdDemande(1);

		Demande demande = Mockito.spy(new Demande());

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(demande);

		ReturnMessageDto messageAgent = new ReturnMessageDto();
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(messageAgent);

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
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);

		result = service.setDemandeEtatSIRH(idAgent, Arrays.asList(dto));

		assertEquals(1, result.getErrors().size());
		assertEquals("Erreur etat incorrect", result.getErrors().get(0).toString());
		Mockito.verify(demande, Mockito.times(0)).addEtatDemande(Mockito.isA(EtatDemande.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setDemandeEtatSIRH_setDemandeEtatValide_majCompteurKo() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = null;

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());
		dto.setIdDemande(1);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.REPOS_COMP.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.REPOS_COMP.getValue());
		type.setGroupe(groupe);

		DemandeReposComp demande = Mockito.spy(new DemandeReposComp());
		demande.setDuree(10);
		demande.setType(type);
		demande.setDateDebut(new Date());
		demande.setDateFin(new Date());

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(demande);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		})
				.when(absDataConsistencyRules)
				.checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(List.class));

		ICounterService counterService = Mockito.mock(ICounterService.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				result.getErrors().add("erreur maj compteur");
				return result;
			}
		})
				.when(counterService)
				.majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(DemandeEtatChangeDto.class));

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(
				counterServiceFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande
						.getType().getIdRefTypeAbsence())).thenReturn(counterService);

		ReturnMessageDto messageAgent = new ReturnMessageDto();
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(messageAgent);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		// ReflectionTestUtils.setField(service, "counterService",
		// counterService);
		ReflectionTestUtils.setField(service, "counterServiceFactory", counterServiceFactory);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		result = service.setDemandeEtatSIRH(idAgent, Arrays.asList(dto));

		assertEquals(1, result.getErrors().size());
		assertEquals("erreur maj compteur", result.getErrors().get(0).toString());
		Mockito.verify(demande, Mockito.times(0)).addEtatDemande(Mockito.isA(EtatDemande.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setDemandeEtatSIRH_setDemandeEtatValide_OK_noMajCompteur_ASA48() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = null;

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());
		dto.setIdDemande(1);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A48.getValue());
		type.setGroupe(groupe);

		DemandeAsa demande = Mockito.spy(new DemandeAsa());
		demande.setDuree(10.0);
		demande.setType(type);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(demande);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		})
				.when(absDataConsistencyRules)
				.checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(List.class));

		ReturnMessageDto message = new ReturnMessageDto();
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(message);

		ICounterService counterService = Mockito.mock(ICounterService.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		})
				.when(counterService)
				.majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(DemandeEtatChangeDto.class));

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(
				counterServiceFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande
						.getType().getIdRefTypeAbsence())).thenReturn(counterService);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "counterServiceFactory", counterServiceFactory);

		result = service.setDemandeEtatSIRH(idAgent, Arrays.asList(dto));

		assertEquals(0, result.getErrors().size());
		assertEquals("La demande est validée.", result.getInfos().get(0));
		Mockito.verify(demande, Mockito.times(1)).addEtatDemande(Mockito.isA(EtatDemande.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setDemandeEtatSIRH_setDemandeEtatValide_ok() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = null;

		ReturnMessageDto srm = new ReturnMessageDto();

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());
		dto.setIdDemande(1);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A48.getValue());
		type.setGroupe(groupe);

		DemandeAsa demande = Mockito.spy(new DemandeAsa());
		demande.setDuree(10.0);
		demande.setType(type);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(demande);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		})
				.when(absDataConsistencyRules)
				.checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(List.class));

		ICounterService counterService = Mockito.mock(ICounterService.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		})
				.when(counterService)
				.majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(DemandeEtatChangeDto.class));

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(
				counterServiceFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande
						.getType().getIdRefTypeAbsence())).thenReturn(counterService);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(srm);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "counterServiceFactory", counterServiceFactory);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		result = service.setDemandeEtatSIRH(idAgent, Arrays.asList(dto));

		assertEquals(0, result.getErrors().size());
		assertEquals("La demande est validée.", result.getInfos().get(0));
		Mockito.verify(demande, Mockito.times(1)).addEtatDemande(Mockito.isA(EtatDemande.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setDemandeEtatSIRH_setDemandeEtatRejete_ok() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = null;

		ReturnMessageDto srm = new ReturnMessageDto();

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.REJETE.getCodeEtat());
		dto.setIdDemande(1);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A48.getValue());
		type.setGroupe(groupe);

		DemandeAsa demande = Mockito.spy(new DemandeAsa());
		demande.setDuree(10.0);
		demande.setType(type);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(demande);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		})
				.when(absDataConsistencyRules)
				.checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(List.class));

		ICounterService counterService = Mockito.mock(ICounterService.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		})
				.when(counterService)
				.majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(DemandeEtatChangeDto.class));

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(
				counterServiceFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande
						.getType().getIdRefTypeAbsence())).thenReturn(counterService);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(srm);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "counterServiceFactory", counterServiceFactory);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		result = service.setDemandeEtatSIRH(idAgent, Arrays.asList(dto));

		assertEquals(0, result.getErrors().size());
		assertEquals("La demande est rejetée.", result.getInfos().get(0));
		Mockito.verify(demande, Mockito.times(1)).addEtatDemande(Mockito.isA(EtatDemande.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setDemandeEtatSIRH_setDemandeEtatEnAttente_checkEtatDemande() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = new ReturnMessageDto();

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.EN_ATTENTE.getCodeEtat());
		dto.setIdDemande(1);

		Demande demande = Mockito.spy(new Demande());

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(demande);

		ReturnMessageDto messageAgent = new ReturnMessageDto();
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(messageAgent);

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
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);

		result = service.setDemandeEtatSIRH(idAgent, Arrays.asList(dto));

		assertEquals(1, result.getErrors().size());
		assertEquals("Erreur etat incorrect", result.getErrors().get(0));
		Mockito.verify(demande, Mockito.times(0)).addEtatDemande(Mockito.isA(EtatDemande.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setDemandeEtatSIRH_setDemandeEtatEnAttente_ok() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = new ReturnMessageDto();

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.EN_ATTENTE.getCodeEtat());
		dto.setIdDemande(1);

		Demande demande = Mockito.spy(new Demande());

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(demande);

		ReturnMessageDto messageAgent = new ReturnMessageDto();
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(messageAgent);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		})
				.when(absDataConsistencyRules)
				.checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(List.class));

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);

		result = service.setDemandeEtatSIRH(idAgent, Arrays.asList(dto));

		assertEquals(0, result.getErrors().size());
		assertEquals("La demande est en attente.", result.getInfos().get(0));
		Mockito.verify(demande, Mockito.times(1)).addEtatDemande(Mockito.isA(EtatDemande.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setDemandeEtatSIRH_setDemandeA48EtatAnnule_checkEtatDemande() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = new ReturnMessageDto();

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
		dto.setIdDemande(1);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A48.getValue());
		type.setGroupe(groupe);

		Demande demande = Mockito.spy(new Demande());
		demande.setIdAgent(idAgent);
		demande.setType(type);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(demande);

		ReturnMessageDto messageAgent = new ReturnMessageDto();
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(messageAgent);

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
				.checkEtatsDemandeAnnulee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(List.class));
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				result.getErrors().add("Le motif est obligatoire.");
				return result;
			}
		})
				.when(absDataConsistencyRules)
				.checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(),
						Mockito.anyString());

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.setDemandeEtatSIRH(idAgent, Arrays.asList(dto));

		assertEquals(2, result.getErrors().size());
		assertEquals("Erreur etat incorrect", result.getErrors().get(0).toString());
		assertEquals("Le motif est obligatoire.", result.getErrors().get(1).toString());
		Mockito.verify(demande, Mockito.times(0)).addEtatDemande(Mockito.isA(EtatDemande.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setDemandeEtatSIRH_setDemandeEtatAnnule_majCompteurKo() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = null;

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
		dto.setIdDemande(1);

		EtatDemande etatDemande = new EtatDemande();
		etatDemande.setEtat(RefEtatEnum.APPROUVEE);
		List<EtatDemande> listEtat = new ArrayList<EtatDemande>();
		listEtat.add(etatDemande);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(2);
		type.setGroupe(groupe);

		DemandeReposComp demande = Mockito.spy(new DemandeReposComp());
		demande.setDuree(10);
		demande.setIdAgent(idAgent);
		demande.setEtatsDemande(listEtat);
		demande.setType(type);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(demande);

		ReturnMessageDto messageAgent = new ReturnMessageDto();
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(messageAgent);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		})
				.when(absDataConsistencyRules)
				.checkEtatsDemandeAnnulee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(List.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		})
				.when(absDataConsistencyRules)
				.checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(),
						Mockito.anyString());

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		ICounterService counterService = Mockito.mock(ICounterService.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				result.getErrors().add("erreur maj compteur");
				return result;
			}
		})
				.when(counterService)
				.majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(DemandeEtatChangeDto.class));

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(
				counterServiceFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande
						.getType().getIdRefTypeAbsence())).thenReturn(counterService);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		// ReflectionTestUtils.setField(service, "counterService",
		// counterService);
		ReflectionTestUtils.setField(service, "counterServiceFactory", counterServiceFactory);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.setDemandeEtatSIRH(idAgent, Arrays.asList(dto));

		assertEquals(1, result.getErrors().size());
		assertEquals("erreur maj compteur", result.getErrors().get(0).toString());
		Mockito.verify(demande, Mockito.times(0)).addEtatDemande(Mockito.isA(EtatDemande.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setDemandeEtatSIRH_setDemandeEtatAnnule_ok() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = null;

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
		dto.setIdDemande(1);

		EtatDemande etatDemande = new EtatDemande();
		etatDemande.setEtat(RefEtatEnum.APPROUVEE);
		List<EtatDemande> listEtat = new ArrayList<EtatDemande>();
		listEtat.add(etatDemande);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(2);
		type.setGroupe(groupe);

		DemandeReposComp demande = Mockito.spy(new DemandeReposComp());
		demande.setDuree(10);
		demande.setIdAgent(idAgent);
		demande.setEtatsDemande(listEtat);
		demande.setType(type);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(demande);

		ReturnMessageDto messageAgent = new ReturnMessageDto();
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(messageAgent);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		})
				.when(absDataConsistencyRules)
				.checkEtatsDemandeAnnulee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(List.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		})
				.when(absDataConsistencyRules)
				.checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(),
						Mockito.anyString());

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		ICounterService counterService = Mockito.mock(ICounterService.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		})
				.when(counterService)
				.majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(DemandeEtatChangeDto.class));

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(
				counterServiceFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande
						.getType().getIdRefTypeAbsence())).thenReturn(counterService);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "counterServiceFactory", counterServiceFactory);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.setDemandeEtatSIRH(idAgent, Arrays.asList(dto));

		assertEquals(0, result.getErrors().size());
		assertEquals("La demande est annulée.", result.getInfos().get(0));
		Mockito.verify(demande, Mockito.times(1)).addEtatDemande(Mockito.isA(EtatDemande.class));
	}

	@Test
	public void saveDemandeAsa54_OK_avecEtatProvisoire() {

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.5);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.ASA_A54.getValue());
		dto.setGroupeAbsence(groupeAbsence);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(idAgent);

		dto.setAgentWithServiceDto(agDto);
		dto.setIdRefEtat(0);
		dto.setDateDemande(new Date());
		dto.setAffichageBoutonModifier(false);
		dto.setAffichageBoutonSupprimer(false);
		dto.setAffichageBoutonImprimer(false);
		dto.setDateDebutAM(true);
		dto.setDateDebutPM(false);
		dto.setDateFinAM(true);
		dto.setDateFinPM(false);

		Droit droitOperateur = new Droit();
		DroitDroitsAgent droitDroitAgent = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(idAgent);
		droitDroitAgent.setDroitsAgent(droitsAgent);
		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.add(droitDroitAgent);
		droitOperateur.setDroitDroitsAgent(droitDroitsAgent);

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setDuree(true);
		typeSaisi.setChkDateDebut(true);
		typeSaisi.setChkDateFin(true);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isUserOperateur(idAgent)).thenReturn(true);
		Mockito.when(accessRightsRepository.getAgentDroitFetchAgents(idAgent)).thenReturn(droitOperateur);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(DemandeAsa.class, dto.getIdDemande())).thenReturn(new DemandeAsa());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeAsa obj = (DemandeAsa) args[0];

				assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(10, obj.getDuree().intValue());
				assertTrue(obj.isDateDebutAM());
				assertTrue(obj.isDateFinAM());
				assertFalse(obj.isDateDebutPM());
				assertFalse(obj.isDateFinPM());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeAsa.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.any(Date.class), Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateFin);
		Mockito.when(
				helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeAsa.class), Mockito.isA(Date.class), Mockito.anyBoolean());

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

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				null);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.isA(ReturnMessageDto.class))).thenReturn(new ReturnMessageDto());

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.saveDemande(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void saveDemandeAsa54_OK_avecEtatSaisie() {

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.0);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.ASA_A54.getValue());
		dto.setGroupeAbsence(groupeAbsence);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(idAgent);

		dto.setAgentWithServiceDto(agDto);
		dto.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());
		dto.setDateDebutAM(true);
		dto.setDateDebutPM(true);
		dto.setDateFinAM(true);
		dto.setDateFinPM(true);

		Droit droitOperateur = new Droit();
		DroitDroitsAgent droitDroitAgent = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(idAgent);
		droitDroitAgent.setDroitsAgent(droitsAgent);
		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.add(droitDroitAgent);
		droitOperateur.setDroitDroitsAgent(droitDroitsAgent);

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setDuree(true);
		typeSaisi.setChkDateDebut(false);
		typeSaisi.setChkDateFin(false);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isUserOperateur(idAgent)).thenReturn(true);
		Mockito.when(accessRightsRepository.getAgentDroitFetchAgents(idAgent)).thenReturn(droitOperateur);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(DemandeAsa.class, dto.getIdDemande())).thenReturn(new DemandeAsa());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeAsa obj = (DemandeAsa) args[0];

				assertEquals(RefEtatEnum.SAISIE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(10, obj.getDuree().intValue());
				assertFalse(obj.isDateDebutAM());
				assertFalse(obj.isDateFinAM());
				assertFalse(obj.isDateDebutPM());
				assertFalse(obj.isDateFinPM());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeAsa.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.any(Date.class), Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateFin);
		Mockito.when(
				helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeAsa.class), Mockito.isA(Date.class), Mockito.anyBoolean());

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

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.isA(ReturnMessageDto.class))).thenReturn(result);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				null);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.saveDemande(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void saveDemandeAsa52_OK_avecEtatProvisoire() {

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.5);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.ASA_A52.getValue());
		dto.setGroupeAbsence(groupeAbsence);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(idAgent);

		dto.setAgentWithServiceDto(agDto);
		dto.setIdRefEtat(0);
		dto.setDateDemande(new Date());
		dto.setAffichageBoutonModifier(false);
		dto.setAffichageBoutonSupprimer(false);
		dto.setAffichageBoutonImprimer(false);
		dto.setDateDebutAM(false);
		dto.setDateDebutPM(true);
		dto.setDateFinAM(false);
		dto.setDateFinPM(true);

		Droit droitOperateur = new Droit();
		DroitDroitsAgent droitDroitAgent = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(idAgent);
		droitDroitAgent.setDroitsAgent(droitsAgent);
		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.add(droitDroitAgent);
		droitOperateur.setDroitDroitsAgent(droitDroitsAgent);

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setDuree(true);
		typeSaisi.setChkDateDebut(true);
		typeSaisi.setChkDateFin(true);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isUserOperateur(idAgent)).thenReturn(true);
		Mockito.when(accessRightsRepository.getAgentDroitFetchAgents(idAgent)).thenReturn(droitOperateur);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(DemandeAsa.class, dto.getIdDemande())).thenReturn(new DemandeAsa());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeAsa obj = (DemandeAsa) args[0];

				assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(10, obj.getDuree().intValue());
				assertFalse(obj.isDateDebutAM());
				assertFalse(obj.isDateFinAM());
				assertTrue(obj.isDateDebutPM());
				assertTrue(obj.isDateFinPM());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeAsa.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.any(Date.class), Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateFin);
		Mockito.when(
				helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeAsa.class), Mockito.isA(Date.class), Mockito.anyBoolean());

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

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				null);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.isA(ReturnMessageDto.class))).thenReturn(new ReturnMessageDto());

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.saveDemande(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void saveDemandeAsa52_OK_avecEtatSaisie() {

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.0);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.ASA_A52.getValue());
		dto.setGroupeAbsence(groupeAbsence);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(idAgent);

		dto.setAgentWithServiceDto(agDto);
		dto.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());
		dto.setDateDebutAM(true);
		dto.setDateDebutPM(true);
		dto.setDateFinAM(true);
		dto.setDateFinPM(true);

		Droit droitOperateur = new Droit();
		DroitDroitsAgent droitDroitAgent = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(idAgent);
		droitDroitAgent.setDroitsAgent(droitsAgent);
		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.add(droitDroitAgent);
		droitOperateur.setDroitDroitsAgent(droitDroitsAgent);

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setDuree(true);
		typeSaisi.setChkDateDebut(true);
		typeSaisi.setChkDateFin(false);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isUserOperateur(idAgent)).thenReturn(true);
		Mockito.when(accessRightsRepository.getAgentDroitFetchAgents(idAgent)).thenReturn(droitOperateur);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(DemandeAsa.class, dto.getIdDemande())).thenReturn(new DemandeAsa());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeAsa obj = (DemandeAsa) args[0];

				assertEquals(RefEtatEnum.SAISIE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(10, obj.getDuree().intValue());
				assertTrue(obj.isDateDebutAM());
				assertFalse(obj.isDateFinAM());
				assertTrue(obj.isDateDebutPM());
				assertFalse(obj.isDateFinPM());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeAsa.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.any(Date.class), Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateFin);
		Mockito.when(
				helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeAsa.class), Mockito.isA(Date.class), Mockito.anyBoolean());

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

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.isA(ReturnMessageDto.class))).thenReturn(result);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				null);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.saveDemande(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void saveDemandeSIRHAsa54_OK_avecEtatProvisoire() {

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.5);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.ASA_A54.getValue());
		dto.setGroupeAbsence(groupeAbsence);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(idAgent);

		dto.setAgentWithServiceDto(agDto);
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

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setDuree(true);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(DemandeAsa.class, dto.getIdDemande())).thenReturn(new DemandeAsa());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeAsa obj = (DemandeAsa) args[0];

				assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(obj.getDuree(), 10, 5);

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeAsa.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.any(Date.class), Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateFin);
		Mockito.when(
				helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeAsa.class), Mockito.isA(Date.class), Mockito.anyBoolean());

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

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(Mockito.anyInt())).thenReturn(result);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.saveDemandeSIRH(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void saveDemandeSIRHAsa54_OK_avecEtatSaisie() {

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.5);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.ASA_A54.getValue());
		dto.setGroupeAbsence(groupeAbsence);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(idAgent);

		dto.setAgentWithServiceDto(agDto);
		dto.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());

		Droit droitOperateur = new Droit();
		DroitDroitsAgent droitDroitAgent = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(idAgent);
		droitDroitAgent.setDroitsAgent(droitsAgent);

		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.add(droitDroitAgent);
		droitOperateur.setDroitDroitsAgent(droitDroitsAgent);

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setDuree(true);
		typeSaisi.setChkDateDebut(true);
		typeSaisi.setChkDateFin(false);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(DemandeAsa.class, dto.getIdDemande())).thenReturn(new DemandeAsa());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeAsa obj = (DemandeAsa) args[0];

				assertEquals(RefEtatEnum.SAISIE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(obj.getDuree().intValue(), 10, 5);

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeAsa.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.any(Date.class), Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateFin);
		Mockito.when(
				helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeAsa.class), Mockito.isA(Date.class), Mockito.anyBoolean());

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

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(Mockito.anyInt())).thenReturn(result);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.saveDemandeSIRH(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void saveDemandeSIRHAsa53_OK_avecEtatProvisoire() {

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.5);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.ASA_A53.getValue());
		dto.setGroupeAbsence(groupeAbsence);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(idAgent);

		dto.setAgentWithServiceDto(agDto);
		dto.setIdRefEtat(0);
		dto.setDateDemande(new Date());
		dto.setAffichageBoutonModifier(false);
		dto.setAffichageBoutonSupprimer(false);
		dto.setAffichageBoutonImprimer(false);

		dto.setDateDebutAM(true);
		dto.setDateDebutPM(true);
		dto.setDateFinAM(true);
		dto.setDateFinPM(true);

		Droit droitOperateur = new Droit();
		DroitDroitsAgent droitDroitAgent = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(idAgent);
		droitDroitAgent.setDroitsAgent(droitsAgent);
		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.add(droitDroitAgent);
		droitOperateur.setDroitDroitsAgent(droitDroitsAgent);

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setDuree(true);
		typeSaisi.setChkDateDebut(false);
		typeSaisi.setChkDateFin(false);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(DemandeAsa.class, dto.getIdDemande())).thenReturn(new DemandeAsa());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeAsa obj = (DemandeAsa) args[0];

				assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(obj.getDuree(), 10, 5);
				assertFalse(obj.isDateDebutAM());
				assertFalse(obj.isDateFinAM());
				assertFalse(obj.isDateDebutPM());
				assertFalse(obj.isDateFinPM());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeAsa.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.any(Date.class), Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateFin);
		Mockito.when(
				helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeAsa.class), Mockito.isA(Date.class), Mockito.anyBoolean());

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

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(Mockito.anyInt())).thenReturn(result);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.saveDemandeSIRH(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void saveDemandeSIRHAsa53_OK_avecEtatSaisie() {

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.5);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.ASA_A53.getValue());
		dto.setGroupeAbsence(groupeAbsence);

		dto.setDateDebutAM(true);
		dto.setDateDebutPM(true);
		dto.setDateFinAM(true);
		dto.setDateFinPM(true);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(idAgent);

		dto.setAgentWithServiceDto(agDto);
		dto.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());

		Droit droitOperateur = new Droit();
		DroitDroitsAgent droitDroitAgent = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(idAgent);
		droitDroitAgent.setDroitsAgent(droitsAgent);

		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.add(droitDroitAgent);
		droitOperateur.setDroitDroitsAgent(droitDroitsAgent);

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setDuree(true);
		typeSaisi.setChkDateDebut(true);
		typeSaisi.setChkDateFin(true);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(DemandeAsa.class, dto.getIdDemande())).thenReturn(new DemandeAsa());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeAsa obj = (DemandeAsa) args[0];

				assertEquals(RefEtatEnum.SAISIE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(obj.getDuree().intValue(), 10, 5);
				assertTrue(obj.isDateDebutAM());
				assertTrue(obj.isDateFinAM());
				assertTrue(obj.isDateDebutPM());
				assertTrue(obj.isDateFinPM());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeAsa.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.any(Date.class), Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateFin);
		Mockito.when(
				helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeAsa.class), Mockito.isA(Date.class), Mockito.anyBoolean());

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

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(Mockito.anyInt())).thenReturn(result);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.saveDemandeSIRH(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void saveDemandeSIRHAsa49_OK_avecEtatProvisoire() {

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.5);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.ASA_A49.getValue());
		dto.setGroupeAbsence(groupeAbsence);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(idAgent);

		dto.setAgentWithServiceDto(agDto);
		dto.setIdRefEtat(0);
		dto.setDateDemande(new Date());
		dto.setAffichageBoutonModifier(false);
		dto.setAffichageBoutonSupprimer(false);
		dto.setAffichageBoutonImprimer(false);

		dto.setDateDebutAM(false);
		dto.setDateDebutPM(false);
		dto.setDateFinAM(false);
		dto.setDateFinPM(false);

		Droit droitOperateur = new Droit();
		DroitDroitsAgent droitDroitAgent = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(idAgent);
		droitDroitAgent.setDroitsAgent(droitsAgent);
		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.add(droitDroitAgent);
		droitOperateur.setDroitDroitsAgent(droitDroitsAgent);

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setDuree(true);
		typeSaisi.setChkDateDebut(false);
		typeSaisi.setChkDateFin(false);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(DemandeAsa.class, dto.getIdDemande())).thenReturn(new DemandeAsa());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeAsa obj = (DemandeAsa) args[0];

				assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(obj.getDuree(), 10, 5);
				assertFalse(obj.isDateDebutAM());
				assertFalse(obj.isDateFinAM());
				assertFalse(obj.isDateDebutPM());
				assertFalse(obj.isDateFinPM());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeAsa.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.any(Date.class), Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateFin);
		Mockito.when(
				helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeAsa.class), Mockito.isA(Date.class), Mockito.anyBoolean());

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

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(Mockito.anyInt())).thenReturn(result);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.saveDemandeSIRH(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void saveDemandeSIRHAsa49_OK_avecEtatSaisie() {

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.5);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.ASA_A49.getValue());
		dto.setGroupeAbsence(groupeAbsence);

		dto.setDateDebutAM(false);
		dto.setDateDebutPM(false);
		dto.setDateFinAM(false);
		dto.setDateFinPM(false);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(idAgent);

		dto.setAgentWithServiceDto(agDto);
		dto.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());

		Droit droitOperateur = new Droit();
		DroitDroitsAgent droitDroitAgent = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(idAgent);
		droitDroitAgent.setDroitsAgent(droitsAgent);

		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.add(droitDroitAgent);
		droitOperateur.setDroitDroitsAgent(droitDroitsAgent);

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setDuree(true);
		typeSaisi.setChkDateDebut(true);
		typeSaisi.setChkDateFin(true);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(DemandeAsa.class, dto.getIdDemande())).thenReturn(new DemandeAsa());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeAsa obj = (DemandeAsa) args[0];

				assertEquals(RefEtatEnum.SAISIE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(obj.getDuree().intValue(), 10, 5);
				assertFalse(obj.isDateDebutAM());
				assertFalse(obj.isDateFinAM());
				assertFalse(obj.isDateDebutPM());
				assertFalse(obj.isDateFinPM());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeAsa.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.any(Date.class), Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateFin);
		Mockito.when(
				helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeAsa.class), Mockito.isA(Date.class), Mockito.anyBoolean());

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

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(Mockito.anyInt())).thenReturn(result);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.saveDemandeSIRH(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void saveDemandeSIRHAsa50_OK_avecEtatProvisoire() {

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.5);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.ASA_A50.getValue());
		dto.setGroupeAbsence(groupeAbsence);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(idAgent);

		dto.setAgentWithServiceDto(agDto);
		dto.setIdRefEtat(RefEtatEnum.PROVISOIRE.getCodeEtat());
		dto.setDateDemande(new Date());
		dto.setAffichageBoutonModifier(false);
		dto.setAffichageBoutonSupprimer(false);
		dto.setAffichageBoutonImprimer(false);

		dto.setDateDebutAM(false);
		dto.setDateDebutPM(false);
		dto.setDateFinAM(true);
		dto.setDateFinPM(true);

		Droit droitOperateur = new Droit();
		DroitDroitsAgent droitDroitAgent = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(idAgent);
		droitDroitAgent.setDroitsAgent(droitsAgent);
		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.add(droitDroitAgent);
		droitOperateur.setDroitDroitsAgent(droitDroitsAgent);

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setDuree(true);
		typeSaisi.setChkDateDebut(true);
		typeSaisi.setChkDateFin(true);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(DemandeAsa.class, dto.getIdDemande())).thenReturn(new DemandeAsa());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeAsa obj = (DemandeAsa) args[0];

				assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(obj.getDuree(), 10, 5);
				assertFalse(obj.isDateDebutAM());
				assertTrue(obj.isDateFinAM());
				assertFalse(obj.isDateDebutPM());
				assertTrue(obj.isDateFinPM());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeAsa.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.any(Date.class), Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateFin);
		Mockito.when(
				helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeAsa.class), Mockito.isA(Date.class), Mockito.anyBoolean());

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

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(Mockito.anyInt())).thenReturn(result);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.saveDemandeSIRH(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void saveDemandeSIRHAsa50_OK_avecEtatSaisie() {

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.5);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.ASA_A50.getValue());
		dto.setGroupeAbsence(groupeAbsence);

		dto.setDateDebutAM(true);
		dto.setDateDebutPM(true);
		dto.setDateFinAM(false);
		dto.setDateFinPM(false);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(idAgent);

		dto.setAgentWithServiceDto(agDto);
		dto.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());

		Droit droitOperateur = new Droit();
		DroitDroitsAgent droitDroitAgent = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(idAgent);
		droitDroitAgent.setDroitsAgent(droitsAgent);

		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.add(droitDroitAgent);
		droitOperateur.setDroitDroitsAgent(droitDroitsAgent);

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setDuree(true);
		typeSaisi.setChkDateDebut(true);
		typeSaisi.setChkDateFin(true);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(DemandeAsa.class, dto.getIdDemande())).thenReturn(new DemandeAsa());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeAsa obj = (DemandeAsa) args[0];

				assertEquals(RefEtatEnum.SAISIE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(obj.getDuree().intValue(), 10, 5);
				assertTrue(obj.isDateDebutAM());
				assertFalse(obj.isDateFinAM());
				assertTrue(obj.isDateDebutPM());
				assertFalse(obj.isDateFinPM());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeAsa.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.any(Date.class), Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateFin);
		Mockito.when(
				helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeAsa.class), Mockito.isA(Date.class), Mockito.anyBoolean());

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

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(Mockito.anyInt())).thenReturn(result);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.saveDemandeSIRH(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(Demande.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setDemandeEtat_setDemandeEtatApprouve_OK_noMajCompteur_ASA54() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = null;

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());
		dto.setIdDemande(1);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		RefTypeSaisi typeSaisi = new RefTypeSaisi();

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A54.getValue());
		type.setGroupe(groupe);
		type.setTypeSaisi(typeSaisi);

		DemandeAsa demande = Mockito.spy(new DemandeAsa());
		demande.setDuree(10.0);
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
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		})
				.when(counterService)
				.majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(DemandeEtatChangeDto.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		})
				.when(absDataConsistencyRules)
				.checkSaisieKiosqueAutorisee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(RefTypeSaisi.class),
						Mockito.eq(false));

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(
				counterServiceFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande
						.getType().getIdRefTypeAbsence())).thenReturn(counterService);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		// ReflectionTestUtils.setField(service, "counterService",
		// counterService);
		ReflectionTestUtils.setField(service, "counterServiceFactory", counterServiceFactory);

		result = service.setDemandeEtat(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demande, Mockito.times(1)).addEtatDemande(Mockito.isA(EtatDemande.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setDemandeEtat_setDemandeEtatApprouve_OK_noMajCompteur_ASA55() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = null;

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());
		dto.setIdDemande(1);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		RefTypeSaisi typeSaisi = new RefTypeSaisi();

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A55.getValue());
		type.setGroupe(groupe);
		type.setTypeSaisi(typeSaisi);

		DemandeAsa demande = Mockito.spy(new DemandeAsa());
		demande.setDuree(10.0);
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

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		})
				.when(absDataConsistencyRules)
				.checkSaisieKiosqueAutorisee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(RefTypeSaisi.class),
						Mockito.eq(false));

		ICounterService counterService = Mockito.mock(ICounterService.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		})
				.when(counterService)
				.majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(DemandeEtatChangeDto.class));

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(
				counterServiceFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande
						.getType().getIdRefTypeAbsence())).thenReturn(counterService);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		// ReflectionTestUtils.setField(service, "counterService",
		// counterService);
		ReflectionTestUtils.setField(service, "counterServiceFactory", counterServiceFactory);

		result = service.setDemandeEtat(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demande, Mockito.times(1)).addEtatDemande(Mockito.isA(EtatDemande.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setDemandeEtatSIRH_setDemandeA54EtatAnnule_checkEtatDemande() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = new ReturnMessageDto();

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
		dto.setIdDemande(1);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A54.getValue());
		type.setGroupe(groupe);

		Demande demande = Mockito.spy(new Demande());
		demande.setIdAgent(idAgent);
		demande.setType(type);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(demande);

		ReturnMessageDto messageAgent = new ReturnMessageDto();
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(messageAgent);

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
				.checkEtatsDemandeAnnulee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(List.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		})
				.when(absDataConsistencyRules)
				.checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(),
						Mockito.anyString());

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.setDemandeEtatSIRH(idAgent, Arrays.asList(dto));

		assertEquals(1, result.getErrors().size());
		assertEquals("Erreur etat incorrect", result.getErrors().get(0).toString());
		Mockito.verify(demande, Mockito.times(0)).addEtatDemande(Mockito.isA(EtatDemande.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setDemandeEtatSIRH_setDemandeEtatValide_OK_noMajCompteur_ASA54() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = null;

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());
		dto.setIdDemande(1);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A54.getValue());
		type.setGroupe(groupe);

		DemandeAsa demande = Mockito.spy(new DemandeAsa());
		demande.setDuree(10.0);
		demande.setType(type);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(demande);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		})
				.when(absDataConsistencyRules)
				.checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(List.class));

		ReturnMessageDto message = new ReturnMessageDto();
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(message);

		ICounterService counterService = Mockito.mock(ICounterService.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		})
				.when(counterService)
				.majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(DemandeEtatChangeDto.class));

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(
				counterServiceFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande
						.getType().getIdRefTypeAbsence())).thenReturn(counterService);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "counterServiceFactory", counterServiceFactory);

		result = service.setDemandeEtatSIRH(idAgent, Arrays.asList(dto));

		assertEquals(0, result.getErrors().size());
		assertEquals("La demande est validée.", result.getInfos().get(0));
		Mockito.verify(demande, Mockito.times(1)).addEtatDemande(Mockito.isA(EtatDemande.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setDemandeEtatSIRH_setDemandeEtatValide_ok_A54() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = null;

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());
		dto.setIdDemande(1);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A54.getValue());
		type.setGroupe(groupe);

		DemandeAsa demande = Mockito.spy(new DemandeAsa());
		demande.setDuree(10.0);
		demande.setType(type);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(demande);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		})
				.when(absDataConsistencyRules)
				.checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(List.class));

		ICounterService counterService = Mockito.mock(ICounterService.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		})
				.when(counterService)
				.majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(DemandeEtatChangeDto.class));

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(
				counterServiceFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande
						.getType().getIdRefTypeAbsence())).thenReturn(counterService);

		ReturnMessageDto messageAgent = new ReturnMessageDto();
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(messageAgent);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "counterServiceFactory", counterServiceFactory);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		result = service.setDemandeEtatSIRH(idAgent, Arrays.asList(dto));

		assertEquals(0, result.getErrors().size());
		assertEquals("La demande est validée.", result.getInfos().get(0));
		Mockito.verify(demande, Mockito.times(1)).addEtatDemande(Mockito.isA(EtatDemande.class));
	}

	@Test
	public void setDemandeEtatPris_EtatIncorrect_ReturnError_A48() {

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setLabel("ASA");
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A48.getValue());
		type.setGroupe(groupe);

		EtatDemande etat1 = new EtatDemande();
		DemandeAsa demande1 = new DemandeAsa();
		etat1.setDemande(demande1);
		etat1.setEtat(RefEtatEnum.SAISIE);
		demande1.getEtatsDemande().add(etat1);
		demande1.setType(type);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, 1)).thenReturn(demande1);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		// When
		ReturnMessageDto result = service.setDemandeEtatPris(1);

		// Then
		assertEquals(1, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
		assertEquals("La demande 1 n'est pas à l'état VALIDEE mais SAISIE.", result.getErrors().get(0).toString());
		Mockito.verify(demandeRepository, Mockito.never()).removeEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void setDemandeEtatPris_EtatIncorrect_ReturnError_A54() {

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setLabel("ASA");
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A54.getValue());
		type.setGroupe(groupe);

		EtatDemande etat1 = new EtatDemande();
		DemandeAsa demande1 = new DemandeAsa();
		etat1.setDemande(demande1);
		etat1.setEtat(RefEtatEnum.SAISIE);
		demande1.getEtatsDemande().add(etat1);
		demande1.setType(type);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, 1)).thenReturn(demande1);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		// When
		ReturnMessageDto result = service.setDemandeEtatPris(1);

		// Then
		assertEquals(1, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
		assertEquals("La demande 1 n'est pas à l'état VALIDEE mais SAISIE.", result.getErrors().get(0).toString());
		Mockito.verify(demandeRepository, Mockito.never()).removeEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void setDemandeEtatPris_EtatIncorrect_ReturnError_A55() {

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setLabel("ASA");
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A55.getValue());
		type.setGroupe(groupe);

		EtatDemande etat1 = new EtatDemande();
		DemandeAsa demande1 = new DemandeAsa();
		etat1.setDemande(demande1);
		etat1.setEtat(RefEtatEnum.SAISIE);
		demande1.getEtatsDemande().add(etat1);
		demande1.setType(type);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, 1)).thenReturn(demande1);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		// When
		ReturnMessageDto result = service.setDemandeEtatPris(1);

		// Then
		assertEquals(1, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
		assertEquals("La demande 1 n'est pas à l'état VALIDEE mais SAISIE.", result.getErrors().get(0).toString());
		Mockito.verify(demandeRepository, Mockito.never()).removeEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void setDemandeEtatPris_EtatIncorrect_ReturnError_A52() {

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setLabel("ASA");
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A52.getValue());
		type.setGroupe(groupe);

		EtatDemande etat1 = new EtatDemande();
		DemandeAsa demande1 = new DemandeAsa();
		etat1.setDemande(demande1);
		etat1.setEtat(RefEtatEnum.SAISIE);
		demande1.getEtatsDemande().add(etat1);
		demande1.setType(type);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, 1)).thenReturn(demande1);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		// When
		ReturnMessageDto result = service.setDemandeEtatPris(1);

		// Then
		assertEquals(1, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
		assertEquals("La demande 1 n'est pas à l'état VALIDEE mais SAISIE.", result.getErrors().get(0).toString());
		Mockito.verify(demandeRepository, Mockito.never()).removeEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void setDemandeEtatPris_EtatIncorrect_ReturnError_A53() {

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setLabel("ASA");
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A53.getValue());
		type.setGroupe(groupe);

		EtatDemande etat1 = new EtatDemande();
		DemandeAsa demande1 = new DemandeAsa();
		etat1.setDemande(demande1);
		etat1.setEtat(RefEtatEnum.SAISIE);
		demande1.getEtatsDemande().add(etat1);
		demande1.setType(type);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, 1)).thenReturn(demande1);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		// When
		ReturnMessageDto result = service.setDemandeEtatPris(1);

		// Then
		assertEquals(1, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
		assertEquals("La demande 1 n'est pas à l'état VALIDEE mais SAISIE.", result.getErrors().get(0).toString());
		Mockito.verify(demandeRepository, Mockito.never()).removeEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void setDemandeEtatPris_EtatIncorrect_ReturnError_A50() {

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setLabel("ASA");
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A50.getValue());
		type.setGroupe(groupe);

		EtatDemande etat1 = new EtatDemande();
		DemandeAsa demande1 = new DemandeAsa();
		etat1.setDemande(demande1);
		etat1.setEtat(RefEtatEnum.SAISIE);
		demande1.getEtatsDemande().add(etat1);
		demande1.setType(type);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, 1)).thenReturn(demande1);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		// When
		ReturnMessageDto result = service.setDemandeEtatPris(1);

		// Then
		assertEquals(1, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
		assertEquals("La demande 1 n'est pas à l'état VALIDEE mais SAISIE.", result.getErrors().get(0).toString());
		Mockito.verify(demandeRepository, Mockito.never()).removeEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void setDemandeEtatPris_EtatIncorrect_ReturnError_A49() {

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setLabel("ASA");
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A49.getValue());
		type.setGroupe(groupe);

		EtatDemande etat1 = new EtatDemande();
		DemandeAsa demande1 = new DemandeAsa();
		etat1.setDemande(demande1);
		etat1.setEtat(RefEtatEnum.SAISIE);
		demande1.getEtatsDemande().add(etat1);
		demande1.setType(type);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, 1)).thenReturn(demande1);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		// When
		ReturnMessageDto result = service.setDemandeEtatPris(1);

		// Then
		assertEquals(1, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
		assertEquals("La demande 1 n'est pas à l'état VALIDEE mais SAISIE.", result.getErrors().get(0).toString());
		Mockito.verify(demandeRepository, Mockito.never()).removeEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void setDemandeEtatPris_EtatIsOk_AddEtatDemande_A48() {

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		RefTypeAbsence typeA54 = new RefTypeAbsence();
		typeA54.setLabel("ASA");
		typeA54.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A48.getValue());
		typeA54.setGroupe(groupe);

		EtatDemande etat1 = new EtatDemande();
		final DemandeAsa demande1 = new DemandeAsa();
		etat1.setDemande(demande1);
		etat1.setEtat(RefEtatEnum.VALIDEE);
		demande1.getEtatsDemande().add(etat1);
		demande1.setType(typeA54);
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
	public void setDemandeEtatPris_EtatIsOk_AddEtatDemande_A54() {

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		RefTypeAbsence typeA54 = new RefTypeAbsence();
		typeA54.setLabel("ASA");
		typeA54.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A54.getValue());
		typeA54.setGroupe(groupe);

		EtatDemande etat1 = new EtatDemande();
		final DemandeAsa demande1 = new DemandeAsa();
		etat1.setDemande(demande1);
		etat1.setEtat(RefEtatEnum.VALIDEE);
		demande1.getEtatsDemande().add(etat1);
		demande1.setType(typeA54);
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
	public void setDemandeEtatPris_EtatIsOk_AddEtatDemande_A55() {

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		RefTypeAbsence typeA54 = new RefTypeAbsence();
		typeA54.setLabel("ASA");
		typeA54.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A55.getValue());
		typeA54.setGroupe(groupe);

		EtatDemande etat1 = new EtatDemande();
		final DemandeAsa demande1 = new DemandeAsa();
		etat1.setDemande(demande1);
		etat1.setEtat(RefEtatEnum.VALIDEE);
		demande1.getEtatsDemande().add(etat1);
		demande1.setType(typeA54);
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
	public void setDemandeEtatPris_EtatIsOk_AddEtatDemande_A52() {

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		RefTypeAbsence typeA54 = new RefTypeAbsence();
		typeA54.setLabel("ASA");
		typeA54.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A52.getValue());
		typeA54.setGroupe(groupe);

		EtatDemande etat1 = new EtatDemande();
		final DemandeAsa demande1 = new DemandeAsa();
		etat1.setDemande(demande1);
		etat1.setEtat(RefEtatEnum.VALIDEE);
		demande1.getEtatsDemande().add(etat1);
		demande1.setType(typeA54);
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
	public void setDemandeEtatPris_EtatIsOk_AddEtatDemande_A53() {

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		RefTypeAbsence typeA54 = new RefTypeAbsence();
		typeA54.setLabel("ASA");
		typeA54.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A53.getValue());
		typeA54.setGroupe(groupe);

		EtatDemande etat1 = new EtatDemande();
		final DemandeAsa demande1 = new DemandeAsa();
		etat1.setDemande(demande1);
		etat1.setEtat(RefEtatEnum.VALIDEE);
		demande1.getEtatsDemande().add(etat1);
		demande1.setType(typeA54);
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
	public void setDemandeEtatPris_EtatIsOk_AddEtatDemande_A49() {

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		RefTypeAbsence typeA54 = new RefTypeAbsence();
		typeA54.setLabel("ASA");
		typeA54.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A49.getValue());
		typeA54.setGroupe(groupe);

		EtatDemande etat1 = new EtatDemande();
		final DemandeAsa demande1 = new DemandeAsa();
		etat1.setDemande(demande1);
		etat1.setEtat(RefEtatEnum.VALIDEE);
		demande1.getEtatsDemande().add(etat1);
		demande1.setType(typeA54);
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
	public void setDemandeEtatPris_EtatIsOk_AddEtatDemande_A50() {

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		RefTypeAbsence typeA54 = new RefTypeAbsence();
		typeA54.setLabel("ASA");
		typeA54.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A50.getValue());
		typeA54.setGroupe(groupe);

		EtatDemande etat1 = new EtatDemande();
		final DemandeAsa demande1 = new DemandeAsa();
		etat1.setDemande(demande1);
		etat1.setEtat(RefEtatEnum.VALIDEE);
		demande1.getEtatsDemande().add(etat1);
		demande1.setType(typeA54);
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
	public void getListeDemandesSIRH_return1Liste_WithA54() {

		List<Demande> listdemande = new ArrayList<Demande>();
		Demande d = new Demande();
		d.setIdDemande(1);
		listdemande.add(d);

		List<DemandeDto> listdemandeDto = new ArrayList<DemandeDto>();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.ASA_A54.getValue());
		dto.setGroupeAbsence(groupeAbsence);

		listdemandeDto.add(dto);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.listeDemandesSIRH(null, null, null, null, null, null)).thenReturn(listdemande);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.when(absDataConsistencyRules.filtreDateAndEtatDemandeFromList(listdemande, null, null)).thenReturn(
				listdemandeDto);
		Mockito.when(absDataConsistencyRules.filtreDroitOfDemandeSIRH(dto)).thenReturn(dto);
		Mockito.when(absDataConsistencyRules.checkDepassementCompteurAgent(dto)).thenReturn(true);

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		List<DemandeDto> listResult = service.getListeDemandesSIRH(null, null, null, null, null, null, false);

		assertEquals(1, listResult.size());
		assertTrue(listResult.get(0).isDepassementCompteur());
	}

	@Test
	public void getDemande_ASA_WithResult_isEtatDefinitif() {

		Date dateDebut = new Date();
		Date dateFin = new Date();
		Date dateMaj = new Date();
		Date dateMaj2 = new Date();
		Integer idDemande = 1;

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		RefTypeAbsence rta = new RefTypeAbsence();
		rta.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A54.getValue());
		rta.setGroupe(groupe);

		Demande d = new Demande();
		d.setType(rta);

		DemandeAsa dr = new DemandeAsa();

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
		dr.setDuree(20.0);
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
		Mockito.when(demandeRepo.getEntity(DemandeAsa.class, idDemande)).thenReturn(dr);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepo);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", helperService);

		// When
		DemandeDto result = service.getDemandeDto(idDemande);

		// Then
		assertEquals(result.getDateDebut(), dateDebut);
		assertEquals(result.getDuree().toString(), "20.0");
		assertEquals(result.getIdDemande(), idDemande);
		assertEquals(result.getIdRefEtat().intValue(), RefEtatEnum.SAISIE.getCodeEtat());
		assertEquals(result.getIdTypeDemande().intValue(), RefTypeAbsenceEnum.ASA_A54.getValue());
		assertFalse(result.isAffichageBoutonImprimer());
		assertFalse(result.isAffichageBoutonModifier());
		assertFalse(result.isAffichageBoutonSupprimer());
		assertEquals("motif", result.getMotif());
	}

	@Test
	public void getDemande_ASA_WithNoResult() {

		Date dateDebut = new Date();
		Date dateFin = new Date();
		Date dateMaj = new Date();
		Date dateMaj2 = new Date();
		Integer idDemande = 1;

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.NOT_EXIST.getValue());

		RefTypeAbsence rta = new RefTypeAbsence();
		rta.setIdRefTypeAbsence(7);
		rta.setGroupe(groupe);

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
		assertNull(result);
	}

	@Test
	public void getListeDemandes_return1Liste_WithA54() {

		List<Demande> listdemande = new ArrayList<Demande>();
		Demande d = new Demande();
		d.setIdDemande(1);
		d.setIdAgent(9005131);
		listdemande.add(d);

		List<DemandeDto> listdemandeDto = new ArrayList<DemandeDto>();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.ASA_A54.getValue());
		dto.setGroupeAbsence(groupeAbsence);
		listdemandeDto.add(dto);

		RefEtat etat = new RefEtat();
		etat.setLabel("APPROUVE");
		List<RefEtat> listEtat = new ArrayList<RefEtat>();
		listEtat.add(etat);

		Date dat = new Date();

		List<DroitsAgent> listDroitAgent = new ArrayList<DroitsAgent>();

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.listeDemandesAgent(9005138, 9005131, dat, null, null, null)).thenReturn(
				listdemande);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.when(absDataConsistencyRules.filtreDateAndEtatDemandeFromList(listdemande, listEtat, null)).thenReturn(
				listdemandeDto);

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);
		Mockito.when(absDataConsistencyRules.filtreDroitOfDemande(9005138, dto, listDroitAgent)).thenReturn(dto);
		Mockito.when(absDataConsistencyRules.checkDepassementCompteurAgent(dto)).thenReturn(false);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDateMoinsUnAn()).thenReturn(dat);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				9005138);

		IFiltreService filtresService = Mockito.mock(IFiltreService.class);
		Mockito.when(filtresService.getListeEtatsByOnglet("TOUTES", null)).thenReturn(listEtat);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.getListOfAgentsToInputOrApprove(9005138, null)).thenReturn(listDroitAgent);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "filtresService", filtresService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);

		List<DemandeDto> listResult = service.getListeDemandes(9005138, 9005131, "TOUTES", null, null, null, null,
				null, null);

		assertEquals(1, listResult.size());
		assertFalse(listResult.get(0).isDepassementCompteur());
	}

	@Test
	public void getListeDemandes_noResult() {

		List<Demande> listdemande = new ArrayList<Demande>();

		List<DemandeDto> listdemandeDto = new ArrayList<DemandeDto>();

		RefEtat etat = new RefEtat();
		etat.setLabel("APPROUVE");
		List<RefEtat> listEtat = new ArrayList<RefEtat>();
		listEtat.add(etat);

		Date date = new Date();

		List<DroitsAgent> listDroitAgent = new ArrayList<DroitsAgent>();

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.listeDemandesAgent(9005138, 9005131, date, null, null, null)).thenReturn(
				listdemande);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.when(absDataConsistencyRules.filtreDateAndEtatDemandeFromList(listdemande, listEtat, null)).thenReturn(
				listdemandeDto);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDateMoinsUnAn()).thenReturn(date);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				9005138);

		IFiltreService filtresService = Mockito.mock(IFiltreService.class);
		Mockito.when(filtresService.getListeEtatsByOnglet("TOUTES", null)).thenReturn(listEtat);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.getListOfAgentsToInputOrApprove(9005138, null)).thenReturn(listDroitAgent);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "filtresService", filtresService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);

		List<DemandeDto> listResult = service.getListeDemandes(9005138, 9005131, "TOUTES", null, null, null, null,
				null, null);

		assertEquals(0, listResult.size());
	}

	@Test
	public void saveDemandeAsa50_OK_avecEtatProvisoire() {

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.5);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.ASA_A50.getValue());
		dto.setGroupeAbsence(groupeAbsence);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(idAgent);

		dto.setAgentWithServiceDto(agDto);
		dto.setIdRefEtat(0);
		dto.setDateDemande(new Date());
		dto.setAffichageBoutonModifier(false);
		dto.setAffichageBoutonSupprimer(false);
		dto.setAffichageBoutonImprimer(false);
		dto.setDateDebutAM(true);
		dto.setDateDebutPM(false);
		dto.setDateFinAM(true);
		dto.setDateFinPM(false);

		Droit droitOperateur = new Droit();
		DroitDroitsAgent droitDroitAgent = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(idAgent);
		droitDroitAgent.setDroitsAgent(droitsAgent);
		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.add(droitDroitAgent);
		droitOperateur.setDroitDroitsAgent(droitDroitsAgent);

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setDuree(true);
		typeSaisi.setChkDateDebut(true);
		typeSaisi.setChkDateFin(true);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isUserOperateur(idAgent)).thenReturn(true);
		Mockito.when(accessRightsRepository.getAgentDroitFetchAgents(idAgent)).thenReturn(droitOperateur);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(DemandeAsa.class, dto.getIdDemande())).thenReturn(new DemandeAsa());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeAsa obj = (DemandeAsa) args[0];

				assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(10, obj.getDuree().intValue());
				assertTrue(obj.isDateDebutAM());
				assertTrue(obj.isDateFinAM());
				assertFalse(obj.isDateDebutPM());
				assertFalse(obj.isDateFinPM());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeAsa.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.any(Date.class), Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateFin);
		Mockito.when(
				helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeAsa.class), Mockito.isA(Date.class), Mockito.anyBoolean());

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

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				null);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.isA(ReturnMessageDto.class))).thenReturn(new ReturnMessageDto());

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.saveDemande(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void saveDemandeAsa50_OK_avecEtatSaisie() {

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.0);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.ASA_A50.getValue());
		dto.setGroupeAbsence(groupeAbsence);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(idAgent);

		dto.setAgentWithServiceDto(agDto);
		dto.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());
		dto.setDateDebutAM(true);
		dto.setDateDebutPM(true);
		dto.setDateFinAM(true);
		dto.setDateFinPM(true);

		Droit droitOperateur = new Droit();
		DroitDroitsAgent droitDroitAgent = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(idAgent);
		droitDroitAgent.setDroitsAgent(droitsAgent);
		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.add(droitDroitAgent);
		droitOperateur.setDroitDroitsAgent(droitDroitsAgent);

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setDuree(true);
		typeSaisi.setChkDateDebut(false);
		typeSaisi.setChkDateFin(false);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isUserOperateur(idAgent)).thenReturn(true);
		Mockito.when(accessRightsRepository.getAgentDroitFetchAgents(idAgent)).thenReturn(droitOperateur);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(DemandeAsa.class, dto.getIdDemande())).thenReturn(new DemandeAsa());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeAsa obj = (DemandeAsa) args[0];

				assertEquals(RefEtatEnum.SAISIE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(10, obj.getDuree().intValue());
				assertFalse(obj.isDateDebutAM());
				assertFalse(obj.isDateFinAM());
				assertFalse(obj.isDateDebutPM());
				assertFalse(obj.isDateFinPM());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeAsa.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.any(Date.class), Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateFin);
		Mockito.when(
				helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeAsa.class), Mockito.isA(Date.class), Mockito.anyBoolean());

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

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.isA(ReturnMessageDto.class))).thenReturn(result);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				null);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.saveDemande(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void saveDemandeAsa49_OK_avecEtatProvisoire() {

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDuree(10.0);
		dto.setDateDebut(dateDebut);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.ASA_A49.getValue());
		dto.setGroupeAbsence(groupeAbsence);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(idAgent);

		dto.setAgentWithServiceDto(agDto);
		dto.setIdRefEtat(RefEtatEnum.PROVISOIRE.getCodeEtat());
		dto.setDateDemande(new Date());
		dto.setAffichageBoutonModifier(false);
		dto.setAffichageBoutonSupprimer(false);
		dto.setAffichageBoutonImprimer(false);
		dto.setDateDebutAM(false);
		dto.setDateDebutPM(false);
		dto.setDateFinAM(false);
		dto.setDateFinPM(false);

		Droit droitOperateur = new Droit();
		DroitDroitsAgent droitDroitAgent = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(idAgent);
		droitDroitAgent.setDroitsAgent(droitsAgent);
		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.add(droitDroitAgent);
		droitOperateur.setDroitDroitsAgent(droitDroitsAgent);

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setDuree(true);
		typeSaisi.setChkDateDebut(false);
		typeSaisi.setChkDateFin(false);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isUserOperateur(idAgent)).thenReturn(true);
		Mockito.when(accessRightsRepository.getAgentDroitFetchAgents(idAgent)).thenReturn(droitOperateur);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(DemandeAsa.class, dto.getIdDemande())).thenReturn(new DemandeAsa());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeAsa obj = (DemandeAsa) args[0];

				assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(10, obj.getDuree().intValue());
				assertFalse(obj.isDateDebutAM());
				assertFalse(obj.isDateFinAM());
				assertFalse(obj.isDateDebutPM());
				assertFalse(obj.isDateFinPM());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeAsa.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.any(Date.class), Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateFin);
		Mockito.when(
				helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeAsa.class), Mockito.isA(Date.class), Mockito.anyBoolean());

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

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				null);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.isA(ReturnMessageDto.class))).thenReturn(new ReturnMessageDto());

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.saveDemande(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void saveDemandeAsa49_OK_avecEtatSaisie() {

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.ASA.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.0);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.ASA_A49.getValue());
		dto.setGroupeAbsence(groupeAbsence);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(idAgent);

		dto.setAgentWithServiceDto(agDto);
		dto.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());
		dto.setDateDebutAM(false);
		dto.setDateDebutPM(false);
		dto.setDateFinAM(false);
		dto.setDateFinPM(false);

		Droit droitOperateur = new Droit();
		DroitDroitsAgent droitDroitAgent = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(idAgent);
		droitDroitAgent.setDroitsAgent(droitsAgent);
		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.add(droitDroitAgent);
		droitOperateur.setDroitDroitsAgent(droitDroitsAgent);

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setDuree(true);
		typeSaisi.setChkDateDebut(false);
		typeSaisi.setChkDateFin(false);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isUserOperateur(idAgent)).thenReturn(true);
		Mockito.when(accessRightsRepository.getAgentDroitFetchAgents(idAgent)).thenReturn(droitOperateur);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(DemandeAsa.class, dto.getIdDemande())).thenReturn(new DemandeAsa());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeAsa obj = (DemandeAsa) args[0];

				assertEquals(RefEtatEnum.SAISIE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(10, obj.getDuree().intValue());
				assertFalse(obj.isDateDebutAM());
				assertFalse(obj.isDateFinAM());
				assertFalse(obj.isDateDebutPM());
				assertFalse(obj.isDateFinPM());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeAsa.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.any(Date.class), Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateFin);
		Mockito.when(
				helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class),
						Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeAsa.class), Mockito.isA(Date.class), Mockito.anyBoolean());

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

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				absDataConsistencyRules);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.isA(ReturnMessageDto.class))).thenReturn(result);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				null);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.saveDemande(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(Demande.class));
	}
}
