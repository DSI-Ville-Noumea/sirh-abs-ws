package nc.noumea.mairie.abs.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

import javax.persistence.FlushModeType;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.test.util.ReflectionTestUtils;

import nc.noumea.mairie.abs.domain.AgentCongeAnnuelCount;
import nc.noumea.mairie.abs.domain.AgentReposCompCount;
import nc.noumea.mairie.abs.domain.AgentWeekCongeAnnuel;
import nc.noumea.mairie.abs.domain.AgentWeekRecup;
import nc.noumea.mairie.abs.domain.AgentWeekReposComp;
import nc.noumea.mairie.abs.domain.CongeAnnuelAlimAutoHisto;
import nc.noumea.mairie.abs.domain.CongeAnnuelRestitutionMassive;
import nc.noumea.mairie.abs.domain.CongeAnnuelRestitutionMassiveHisto;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeAsa;
import nc.noumea.mairie.abs.domain.DemandeCongesAnnuels;
import nc.noumea.mairie.abs.domain.DemandeCongesExceptionnels;
import nc.noumea.mairie.abs.domain.DemandeMaladies;
import nc.noumea.mairie.abs.domain.DemandeRecup;
import nc.noumea.mairie.abs.domain.DemandeReposComp;
import nc.noumea.mairie.abs.domain.Droit;
import nc.noumea.mairie.abs.domain.DroitDroitsAgent;
import nc.noumea.mairie.abs.domain.DroitProfil;
import nc.noumea.mairie.abs.domain.DroitsAgent;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.EtatDemandeAsa;
import nc.noumea.mairie.abs.domain.EtatDemandeCongesAnnuels;
import nc.noumea.mairie.abs.domain.EtatDemandeCongesExceptionnels;
import nc.noumea.mairie.abs.domain.EtatDemandeRecup;
import nc.noumea.mairie.abs.domain.EtatDemandeReposComp;
import nc.noumea.mairie.abs.domain.ProfilEnum;
import nc.noumea.mairie.abs.domain.RefAlimCongeAnnuel;
import nc.noumea.mairie.abs.domain.RefAlimCongeAnnuelId;
import nc.noumea.mairie.abs.domain.RefEtat;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefGroupeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.domain.RefTypeGroupeAbsenceEnum;
import nc.noumea.mairie.abs.domain.RefTypeSaisi;
import nc.noumea.mairie.abs.domain.RefTypeSaisiCongeAnnuel;
import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.LightUser;
import nc.noumea.mairie.abs.dto.MoisAlimAutoCongesAnnuelsDto;
import nc.noumea.mairie.abs.dto.RefEtatDto;
import nc.noumea.mairie.abs.dto.RefGroupeAbsenceDto;
import nc.noumea.mairie.abs.dto.RefTypeSaisiCongeAnnuelDto;
import nc.noumea.mairie.abs.dto.RestitutionMassiveDto;
import nc.noumea.mairie.abs.dto.ResultListDemandeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDtoException;
import nc.noumea.mairie.abs.repository.IAccessRightsRepository;
import nc.noumea.mairie.abs.repository.ICongesAnnuelsRepository;
import nc.noumea.mairie.abs.repository.ICounterRepository;
import nc.noumea.mairie.abs.repository.IDemandeRepository;
import nc.noumea.mairie.abs.repository.IFiltreRepository;
import nc.noumea.mairie.abs.repository.IRecuperationRepository;
import nc.noumea.mairie.abs.repository.IReposCompensateurRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.abs.repository.ITypeAbsenceRepository;
import nc.noumea.mairie.abs.service.IAbsenceDataConsistencyRules;
import nc.noumea.mairie.abs.service.IAccessRightsService;
import nc.noumea.mairie.abs.service.IAgentMatriculeConverterService;
import nc.noumea.mairie.abs.service.IAgentService;
import nc.noumea.mairie.abs.service.ICounterService;
import nc.noumea.mairie.abs.service.IFiltreService;
import nc.noumea.mairie.abs.service.counter.impl.CounterServiceFactory;
import nc.noumea.mairie.abs.service.multiThread.DemandeRecursiveTask;
import nc.noumea.mairie.abs.service.multiThread.DemandeRecursiveTaskSimple;
import nc.noumea.mairie.abs.service.rules.impl.AbsCongesAnnuelsDataConsistencyRulesImpl;
import nc.noumea.mairie.abs.service.rules.impl.DataConsistencyRulesFactory;
import nc.noumea.mairie.abs.vo.CheckCompteurAgentVo;
import nc.noumea.mairie.abs.web.AccessForbiddenException;
import nc.noumea.mairie.alfresco.cmis.IAlfrescoCMISService;
import nc.noumea.mairie.domain.SpSold;
import nc.noumea.mairie.domain.SpSorc;
import nc.noumea.mairie.domain.Spcarr;
import nc.noumea.mairie.domain.SpcarrId;
import nc.noumea.mairie.domain.Spcc;
import nc.noumea.mairie.domain.SpccId;
import nc.noumea.mairie.domain.Spmatr;
import nc.noumea.mairie.ws.ISirhWSConsumer;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ AbsenceService.class })
public class AbsenceServiceTest {

	@Test
	public void getListeNonFiltreeDemandes_return1Liste() {

		Integer idAgentConnecte = 9005138;

		List<Demande> listdemande = new ArrayList<Demande>();
		listdemande.add(new Demande());

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.isUserDelegataire(idAgentConnecte)).thenReturn(false);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.listeDemandesAgent(idAgentConnecte, null, null, null, RefTypeAbsenceEnum.RECUP.getValue(), null))
				.thenReturn(listdemande);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(null);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		List<Demande> listResult = service.getListeNonFiltreeDemandes(idAgentConnecte, null, null, null, RefTypeAbsenceEnum.RECUP.getValue(), null, null, null);

		assertEquals(1, listResult.size());
	}

	@Test
	public void getListeNonFiltreeDemandes_return2Listes() {

		Integer idAgentConnecte = 9005138;
		Integer idApprobateurOfDelegataire = 9005140;
		List<Integer> idsApprobateurOfDelegataire = new ArrayList<Integer>();

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

		List<DroitProfil> droitsProfils = new ArrayList<DroitProfil>();
		DroitProfil dp = new DroitProfil();
		dp.setDroitApprobateur(droitApprobateur);

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.isUserDelegataire(idAgentConnecte)).thenReturn(true);
		Mockito.when(arRepo.getDroitProfilByAgentAndLibelle(idAgentConnecte, ProfilEnum.DELEGATAIRE.toString())).thenReturn(droitsProfils);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(idsApprobateurOfDelegataire);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.listeDemandesAgent(idAgentConnecte, null, null, null, RefTypeAbsenceEnum.RECUP.getValue(), null))
				.thenReturn(listdemande);
		Mockito.when(demandeRepository.listeDemandesAgent(idApprobateurOfDelegataire, null, null, null, RefTypeAbsenceEnum.RECUP.getValue(), null))
				.thenReturn(listdemandeDeleg);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		List<Demande> listResult = service.getListeNonFiltreeDemandes(idAgentConnecte, null, null, null, RefTypeAbsenceEnum.RECUP.getValue(), null, null, null);

		assertEquals(2, listResult.size());
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

		EtatDemandeRecup ed = new EtatDemandeRecup();
		ed.setDate(dateMaj);
		ed.setDemande((Demande) dr);
		ed.setEtat(RefEtatEnum.PROVISOIRE);
		ed.setIdAgent(9005138);
		ed.setIdEtatDemande(1);
		ed.setDuree(5);
		EtatDemandeRecup ed2 = new EtatDemandeRecup();
		ed2.setDate(dateMaj2);
		ed2.setDemande((Demande) dr);
		ed2.setEtat(RefEtatEnum.SAISIE);
		ed2.setIdAgent(9005138);
		ed2.setIdEtatDemande(2);
		ed2.setDuree(10);
		ed2.setDateDebut(dateDebut);
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

		EtatDemandeRecup ed = new EtatDemandeRecup();
		ed.setDate(dateMaj);
		ed.setDemande((Demande) dr);
		ed.setEtat(RefEtatEnum.SAISIE);
		ed.setIdAgent(9005138);
		ed.setIdEtatDemande(1);
		EtatDemandeRecup ed2 = new EtatDemandeRecup();
		ed2.setDate(dateMaj2);
		ed2.setDemande((Demande) dr);
		ed2.setEtat(RefEtatEnum.APPROUVEE);
		ed2.setIdAgent(9005138);
		ed2.setIdEtatDemande(2);
		ed2.setDuree(10);
		ed2.setDateDebut(dateDebut);
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
		Mockito.when(demandeRepository.getEntity(DemandeRecup.class, dto.getIdDemande())).thenReturn(new DemandeRecup());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeRecup obj = (DemandeRecup) args[0];

				assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(600, obj.getDuree().intValue());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeRecup.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);
		Mockito.when(helperService.getDuree(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble()))
				.thenReturn(600.0);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeRecup.class), Mockito.anyBoolean());

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
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(null);
		Mockito.when(accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(ReturnMessageDto.class)))
				.thenReturn(new ReturnMessageDto());

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(new RefTypeSaisi());

		IAlfrescoCMISService alfrescoCMISService = Mockito.mock(IAlfrescoCMISService.class);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "alfrescoCMISService", alfrescoCMISService);

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
		Mockito.when(demandeRepository.getEntity(DemandeRecup.class, dto.getIdDemande())).thenReturn(new DemandeRecup());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeRecup obj = (DemandeRecup) args[0];

				assertEquals(RefEtatEnum.SAISIE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(600, obj.getDuree().intValue());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeRecup.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);
		Mockito.when(helperService.getDuree(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble()))
				.thenReturn(600.0);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeRecup.class), Mockito.anyBoolean());

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
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(ReturnMessageDto.class))).thenReturn(result);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(null);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(new RefTypeSaisi());

		IAlfrescoCMISService alfrescoCMISService = Mockito.mock(IAlfrescoCMISService.class);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "alfrescoCMISService", alfrescoCMISService);

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
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeRecup.class), Mockito.anyBoolean());

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
		Mockito.when(accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(ReturnMessageDto.class))).thenReturn(result);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);

		try {
			result = service.saveDemande(idAgent, dto);
		} catch (ReturnMessageDtoException e) {
			result = e.getErreur();
		}

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
		assertEquals("L'agent Viseur n'est pas habilité pour viser la demande de cet agent.", result.getErrors().get(0).toString());
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
		}).when(absDataConsistencyRules).checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(List.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		}).when(absDataConsistencyRules).checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(), Mockito.anyString());

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		}).when(absDataConsistencyRules).checkSaisieKiosqueAutorisee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(RefTypeSaisi.class), Mockito.eq(false));

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
		}).when(absDataConsistencyRules).checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(List.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		}).when(absDataConsistencyRules).checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(), Mockito.anyString());

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				result.getErrors().add("Erreur saisie kiosque");
				return result;
			}
		}).when(absDataConsistencyRules).checkSaisieKiosqueAutorisee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(RefTypeSaisi.class), Mockito.eq(false));

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
		}).when(absDataConsistencyRules).checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(List.class));

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
		}).when(absDataConsistencyRules).checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(), Mockito.anyString());

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		}).when(absDataConsistencyRules).checkSaisieKiosqueAutorisee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(RefTypeSaisi.class), Mockito.eq(false));

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

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.RECUP.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setTypeSaisi(typeSaisi);
		type.setGroupe(groupe);

		DemandeRecup demande = Mockito.spy(new DemandeRecup());
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
		}).when(absDataConsistencyRules).checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(List.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		}).when(absDataConsistencyRules).checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(), Mockito.anyString());

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		}).when(absDataConsistencyRules).checkSaisieKiosqueAutorisee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(RefTypeSaisi.class), Mockito.eq(false));

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
		Mockito.when(accessRightsRepository.isApprobateurOrDelegataireOfAgent(idAgent, demande.getIdAgent())).thenReturn(false);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);

		result = service.setDemandeEtat(idAgent, dto);

		assertEquals(1, result.getErrors().size());
		assertEquals("L'agent Approbateur n'est pas habilité à approuver la demande de cet agent.", result.getErrors().get(0).toString());
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
		Mockito.when(accessRightsRepository.isApprobateurOrDelegataireOfAgent(idAgent, demande.getIdAgent())).thenReturn(true);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				result.getErrors().add("Erreur etat incorrect");
				return result;
			}
		}).when(absDataConsistencyRules).checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(List.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				result.getErrors().add("Erreur motif incorrect");
				return result;
			}
		}).when(absDataConsistencyRules).checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(), Mockito.anyString());

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				result.getErrors().add("Erreur saisie kiosque incorrect");
				return result;
			}
		}).when(absDataConsistencyRules).checkSaisieKiosqueAutorisee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(RefTypeSaisi.class), Mockito.eq(false));

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
		Mockito.when(accessRightsRepository.isApprobateurOrDelegataireOfAgent(idAgent, demande.getIdAgent())).thenReturn(true);

		AbsCongesAnnuelsDataConsistencyRulesImpl absenceDataConsistencyRulesImpl = Mockito.mock(AbsCongesAnnuelsDataConsistencyRulesImpl.class);

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		}).when(absenceDataConsistencyRulesImpl).checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
				Mockito.isA(List.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		}).when(absenceDataConsistencyRulesImpl).checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(), Mockito.anyString());

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		}).when(absenceDataConsistencyRulesImpl).checkSaisieKiosqueAutorisee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(RefTypeSaisi.class),
				Mockito.eq(false));

		ICounterService counterService = Mockito.mock(ICounterService.class);
		Mockito.when(
				counterService.majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(DemandeEtatChangeDto.class)))
				.thenReturn(srm);

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(counterServiceFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande.getType().getIdRefTypeAbsence()))
				.thenReturn(counterService);

		Mockito.doNothing().when(absenceDataConsistencyRulesImpl).checkSamediOffertToujoursOk(dto, demande);

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande.getType().getIdRefTypeAbsence()))
				.thenReturn(absenceDataConsistencyRulesImpl);

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		}).when(absenceDataConsistencyRulesImpl).checkDepassementDroitsAcquis(Mockito.any(ReturnMessageDto.class), Mockito.any(DemandeRecup.class),
				Mockito.any(CheckCompteurAgentVo.class));

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absenceDataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
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
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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
		Mockito.when(accessRightsRepository.isApprobateurOrDelegataireOfAgent(idAgent, demande.getIdAgent())).thenReturn(true);

		AbsCongesAnnuelsDataConsistencyRulesImpl absenceDataConsistencyRulesImpl = Mockito.mock(AbsCongesAnnuelsDataConsistencyRulesImpl.class);

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		}).when(absenceDataConsistencyRulesImpl).checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
				Mockito.isA(List.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		}).when(absenceDataConsistencyRulesImpl).checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(), Mockito.anyString());

		ICounterService counterService = Mockito.mock(ICounterService.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		}).when(counterService).majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(DemandeEtatChangeDto.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		}).when(absenceDataConsistencyRulesImpl).checkSaisieKiosqueAutorisee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(RefTypeSaisi.class),
				Mockito.eq(false));

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(counterServiceFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande.getType().getIdRefTypeAbsence()))
				.thenReturn(counterService);

		Mockito.doNothing().when(absenceDataConsistencyRulesImpl).checkSamediOffertToujoursOk(dto, demande);

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande.getType().getIdRefTypeAbsence()))
				.thenReturn(absenceDataConsistencyRulesImpl);

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		}).when(absenceDataConsistencyRulesImpl).checkDepassementDroitsAcquis(Mockito.any(ReturnMessageDto.class), Mockito.any(DemandeRecup.class),
				Mockito.any(CheckCompteurAgentVo.class));

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absenceDataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "counterServiceFactory", counterServiceFactory);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

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
		Mockito.when(accessRightsRepository.isApprobateurOrDelegataireOfAgent(idAgent, demande.getIdAgent())).thenReturn(true);

		AbsCongesAnnuelsDataConsistencyRulesImpl absenceDataConsistencyRulesImpl = Mockito.mock(AbsCongesAnnuelsDataConsistencyRulesImpl.class);

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		}).when(absenceDataConsistencyRulesImpl).checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
				Mockito.isA(List.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		}).when(absenceDataConsistencyRulesImpl).checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(), Mockito.anyString());

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		}).when(absenceDataConsistencyRulesImpl).checkSaisieKiosqueAutorisee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(RefTypeSaisi.class),
				Mockito.eq(false));

		ICounterService counterService = Mockito.mock(ICounterService.class);
		Mockito.when(
				counterService.majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(DemandeEtatChangeDto.class)))
				.thenReturn(srm);

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(counterServiceFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande.getType().getIdRefTypeAbsence()))
				.thenReturn(counterService);

		Mockito.doNothing().when(absenceDataConsistencyRulesImpl).checkSamediOffertToujoursOk(dto, demande);

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande.getType().getIdRefTypeAbsence()))
				.thenReturn(absenceDataConsistencyRulesImpl);

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		}).when(absenceDataConsistencyRulesImpl).checkDepassementDroitsAcquis(Mockito.any(ReturnMessageDto.class), Mockito.any(DemandeRecup.class),
				Mockito.any(CheckCompteurAgentVo.class));

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absenceDataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "counterServiceFactory", counterServiceFactory);

		result = service.setDemandeEtat(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		assertEquals("La demande est approuvée.", result.getInfos().get(0));
		Mockito.verify(demande, Mockito.times(1)).addEtatDemande(Mockito.isA(EtatDemande.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setDemandeEtat_setDemandeEtatApprouve_CA_depassementCompteur() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = null;

		ReturnMessageDto srm = new ReturnMessageDto();

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());
		dto.setIdDemande(1);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.CONGE_ANNUEL.getValue());
		type.setGroupe(groupe);
		type.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);

		DemandeCongesAnnuels demande = Mockito.spy(new DemandeCongesAnnuels());
		demande.setDuree(10.0);
		demande.setType(type);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(demande);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isApprobateurOrDelegataireOfAgent(idAgent, demande.getIdAgent())).thenReturn(true);

		IAbsenceDataConsistencyRules congeAnnuelDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		}).when(congeAnnuelDataConsistencyRules).checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
				Mockito.isA(List.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		}).when(congeAnnuelDataConsistencyRules).checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(), Mockito.anyString());

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		}).when(congeAnnuelDataConsistencyRules).checkSaisieKiosqueAutorisee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(RefTypeSaisi.class),
				Mockito.eq(false));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				result.getInfos().add("depassement compteur");
				return result;
			}
		}).when(congeAnnuelDataConsistencyRules).checkDepassementDroitsAcquis(Mockito.isA(ReturnMessageDto.class), Mockito.isA(DemandeCongesAnnuels.class),
				Mockito.any(CheckCompteurAgentVo.class));

		ICounterService counterService = Mockito.mock(ICounterService.class);
		Mockito.when(
				counterService.majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(DemandeEtatChangeDto.class)))
				.thenReturn(srm);

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(counterServiceFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande.getType().getIdRefTypeAbsence()))
				.thenReturn(counterService);

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(congeAnnuelDataConsistencyRules);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", congeAnnuelDataConsistencyRules);
		ReflectionTestUtils.setField(service, "counterServiceFactory", counterServiceFactory);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.setDemandeEtat(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		assertEquals("La demande est en attente de validation par la DRH.", result.getInfos().get(0));
		Mockito.verify(demande, Mockito.times(1)).addEtatDemande(Mockito.isA(EtatDemande.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setDemandeEtat_setDemandeEtatApprouve_CA_ok() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = null;

		ReturnMessageDto srm = new ReturnMessageDto();

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());
		dto.setIdDemande(1);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.CONGE_ANNUEL.getValue());
		type.setGroupe(groupe);
		type.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);

		DemandeCongesAnnuels demande = Mockito.spy(new DemandeCongesAnnuels());
		demande.setDuree(10.0);
		demande.setType(type);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(demande);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isApprobateurOrDelegataireOfAgent(idAgent, demande.getIdAgent())).thenReturn(true);

		IAbsenceDataConsistencyRules congeAnnuelDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		}).when(congeAnnuelDataConsistencyRules).checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
				Mockito.isA(List.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		}).when(congeAnnuelDataConsistencyRules).checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(), Mockito.anyString());

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		}).when(congeAnnuelDataConsistencyRules).checkSaisieKiosqueAutorisee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(RefTypeSaisi.class),
				Mockito.eq(false));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		}).when(congeAnnuelDataConsistencyRules).checkDepassementDroitsAcquis(Mockito.isA(ReturnMessageDto.class), Mockito.isA(DemandeCongesAnnuels.class),
				Mockito.any(CheckCompteurAgentVo.class));

		ICounterService counterService = Mockito.mock(ICounterService.class);
		Mockito.when(
				counterService.majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(DemandeEtatChangeDto.class)))
				.thenReturn(srm);

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(counterServiceFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande.getType().getIdRefTypeAbsence()))
				.thenReturn(counterService);

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(congeAnnuelDataConsistencyRules);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", congeAnnuelDataConsistencyRules);
		ReflectionTestUtils.setField(service, "counterServiceFactory", counterServiceFactory);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.setDemandeEtat(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		assertEquals("La demande est approuvée.", result.getInfos().get(0));
		Mockito.verify(demande, Mockito.times(1)).addEtatDemande(Mockito.isA(EtatDemande.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setDemandeEtat_setDemandeEtatAttenteValidationDRH_CA_ok() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = null;

		ReturnMessageDto srm = new ReturnMessageDto();

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());
		dto.setIdDemande(1);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.CONGE_ANNUEL.getValue());
		type.setGroupe(groupe);
		type.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);

		DemandeCongesAnnuels demande = Mockito.spy(new DemandeCongesAnnuels());
		demande.setDuree(10.0);
		demande.setType(type);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(demande);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isApprobateurOrDelegataireOfAgent(idAgent, demande.getIdAgent())).thenReturn(true);

		IAbsenceDataConsistencyRules congeAnnuelDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		}).when(congeAnnuelDataConsistencyRules).checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
				Mockito.isA(List.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		}).when(congeAnnuelDataConsistencyRules).checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(), Mockito.anyString());

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		}).when(congeAnnuelDataConsistencyRules).checkSaisieKiosqueAutorisee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(RefTypeSaisi.class),
				Mockito.eq(false));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				result.getInfos().add("depassement");
				return result;
			}
		}).when(congeAnnuelDataConsistencyRules).checkDepassementDroitsAcquis(Mockito.isA(ReturnMessageDto.class), Mockito.isA(DemandeCongesAnnuels.class),
				Mockito.any(CheckCompteurAgentVo.class));

		ICounterService counterService = Mockito.mock(ICounterService.class);
		Mockito.when(
				counterService.majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(DemandeEtatChangeDto.class)))
				.thenReturn(srm);

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(counterServiceFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande.getType().getIdRefTypeAbsence()))
				.thenReturn(counterService);

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(congeAnnuelDataConsistencyRules);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", congeAnnuelDataConsistencyRules);
		ReflectionTestUtils.setField(service, "counterServiceFactory", counterServiceFactory);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.setDemandeEtat(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		assertEquals("La demande est en attente de validation par la DRH.", result.getInfos().get(0));
		Mockito.verify(demande, Mockito.times(1)).addEtatDemande(Mockito.isA(EtatDemande.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setDemandeEtat_setDemandeEtatRefuse_CA_depassementCompteur() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = null;

		ReturnMessageDto srm = new ReturnMessageDto();

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.REFUSEE.getCodeEtat());
		dto.setIdDemande(1);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.CONGE_ANNUEL.getValue());
		type.setGroupe(groupe);
		type.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);

		DemandeCongesAnnuels demande = Mockito.spy(new DemandeCongesAnnuels());
		demande.setDuree(10.0);
		demande.setType(type);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(demande);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isApprobateurOrDelegataireOfAgent(idAgent, demande.getIdAgent())).thenReturn(true);

		IAbsenceDataConsistencyRules congeAnnuelDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		}).when(congeAnnuelDataConsistencyRules).checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
				Mockito.isA(List.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		}).when(congeAnnuelDataConsistencyRules).checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(), Mockito.anyString());

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		}).when(congeAnnuelDataConsistencyRules).checkSaisieKiosqueAutorisee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(RefTypeSaisi.class),
				Mockito.eq(false));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				result.getInfos().add("depassement compteur");
				return result;
			}
		}).when(congeAnnuelDataConsistencyRules).checkDepassementDroitsAcquis(Mockito.isA(ReturnMessageDto.class), Mockito.isA(DemandeCongesAnnuels.class),
				Mockito.any(CheckCompteurAgentVo.class));

		ICounterService counterService = Mockito.mock(ICounterService.class);
		Mockito.when(
				counterService.majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(DemandeEtatChangeDto.class)))
				.thenReturn(srm);

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(counterServiceFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande.getType().getIdRefTypeAbsence()))
				.thenReturn(counterService);

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(congeAnnuelDataConsistencyRules);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", congeAnnuelDataConsistencyRules);
		ReflectionTestUtils.setField(service, "counterServiceFactory", counterServiceFactory);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.setDemandeEtat(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		assertEquals("La demande est refusée.", result.getInfos().get(0));
		Mockito.verify(demande, Mockito.times(1)).addEtatDemande(Mockito.isA(EtatDemande.class));
	}

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
		Mockito.when(accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(ReturnMessageDto.class))).thenReturn(result);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);

		result = service.setDemandeEtat(idAgent, dto);

		assertEquals(1, result.getErrors().size());
		assertEquals("Vous n'êtes pas opérateur. Vous ne pouvez pas saisir de demandes.", result.getErrors().get(0).toString());
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
		Mockito.when(accessRightsRepository.isApprobateurOrDelegataireOfAgent(idAgent, demande.getIdAgent())).thenReturn(true);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				result.getErrors().add("Erreur etat incorrect");
				return result;
			}
		}).when(absDataConsistencyRules).checkEtatsDemandeAnnulee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(List.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		}).when(absDataConsistencyRules).checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(), Mockito.anyString());

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(ReturnMessageDto.class)))
				.thenReturn(new ReturnMessageDto());

		ReturnMessageDto resultPaieEnCours = new ReturnMessageDto();

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isPaieEnCours()).thenReturn(resultPaieEnCours);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

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
		Mockito.when(accessRightsRepository.isApprobateurOrDelegataireOfAgent(idAgent, demande.getIdAgent())).thenReturn(true);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		}).when(absDataConsistencyRules).checkEtatsDemandeAnnulee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(List.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		}).when(absDataConsistencyRules).checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(), Mockito.anyString());

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		ICounterService counterService = Mockito.mock(ICounterService.class);
		Mockito.when(
				counterService.majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(DemandeEtatChangeDto.class)))
				.thenReturn(srm);

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(counterServiceFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande.getType().getIdRefTypeAbsence()))
				.thenReturn(counterService);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(ReturnMessageDto.class)))
				.thenReturn(new ReturnMessageDto());

		ReturnMessageDto resultPaieEnCours = new ReturnMessageDto();

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isPaieEnCours()).thenReturn(resultPaieEnCours);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		// ReflectionTestUtils.setField(service, "counterService",
		// counterService);
		ReflectionTestUtils.setField(service, "counterServiceFactory", counterServiceFactory);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

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
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.REPOS_COMP.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.REPOS_COMP.getValue());
		type.setGroupe(groupe);

		DemandeReposComp demande = Mockito.spy(new DemandeReposComp());
		demande.setDuree(10);
		demande.setIdAgent(idAgent);
		demande.setEtatsDemande(listEtat);
		demande.setType(type);
		demande.setDateDebut(new Date());
		demande.setDateFin(new Date());

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(demande);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isApprobateurOrDelegataireOfAgent(idAgent, demande.getIdAgent())).thenReturn(true);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		}).when(absDataConsistencyRules).checkEtatsDemandeAnnulee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(List.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		}).when(absDataConsistencyRules).checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(), Mockito.anyString());

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		ICounterService counterService = Mockito.mock(ICounterService.class);
		Mockito.when(
				counterService.majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(DemandeEtatChangeDto.class)))
				.thenReturn(srm);

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(counterServiceFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande.getType().getIdRefTypeAbsence()))
				.thenReturn(counterService);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(ReturnMessageDto.class)))
				.thenReturn(new ReturnMessageDto());

		ReturnMessageDto resultPaieEnCours = new ReturnMessageDto();

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isPaieEnCours()).thenReturn(resultPaieEnCours);

		Spcarr carr = new Spcarr(5138, 20140101);
		carr.setDateFin(0);
		carr.setCdcate(8);

		HelperService helper = Mockito.mock(HelperService.class);
		Mockito.when(helper.isContractuel(carr)).thenReturn(false);
		Mockito.when(helper.isConventionCollective(carr)).thenReturn(false);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(carr);

		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(demande.getIdAgent())).thenReturn(5138);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "counterServiceFactory", counterServiceFactory);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeService);
		ReflectionTestUtils.setField(service, "helperService", helper);

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
		Mockito.when(demandeRepository.getEntity(DemandeReposComp.class, dto.getIdDemande())).thenReturn(new DemandeReposComp());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeReposComp obj = (DemandeReposComp) args[0];

				assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeReposComp.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);
		Mockito.when(helperService.getDuree(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble()))
				.thenReturn(600.0);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeReposComp.class), Mockito.anyBoolean());

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
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(null);
		Mockito.when(accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(ReturnMessageDto.class)))
				.thenReturn(new ReturnMessageDto());

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(new RefTypeSaisi());

		IAlfrescoCMISService alfrescoCMISService = Mockito.mock(IAlfrescoCMISService.class);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "alfrescoCMISService", alfrescoCMISService);

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
		Mockito.when(demandeRepository.getEntity(DemandeReposComp.class, dto.getIdDemande())).thenReturn(new DemandeReposComp());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeReposComp obj = (DemandeReposComp) args[0];

				assertEquals(RefEtatEnum.SAISIE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(650, obj.getDuree().intValue());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeReposComp.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);
		Mockito.when(helperService.getDuree(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble()))
				.thenReturn(650.0);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeReposComp.class), Mockito.anyBoolean());

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
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(ReturnMessageDto.class))).thenReturn(result);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(null);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(new RefTypeSaisi());

		IAlfrescoCMISService alfrescoCMISService = Mockito.mock(IAlfrescoCMISService.class);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "alfrescoCMISService", alfrescoCMISService);

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
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeReposComp.class), Mockito.anyBoolean());

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
		Mockito.when(accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(ReturnMessageDto.class))).thenReturn(result);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);

		try {
			result = service.saveDemande(idAgent, dto);
		} catch (ReturnMessageDtoException e) {
			result = e.getErreur();
		}

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
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);
		Mockito.when(helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeAsa.class), Mockito.anyBoolean());

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
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(null);
		Mockito.when(accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(ReturnMessageDto.class)))
				.thenReturn(new ReturnMessageDto());

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		IAlfrescoCMISService alfrescoCMISService = Mockito.mock(IAlfrescoCMISService.class);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "alfrescoCMISService", alfrescoCMISService);

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
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);
		Mockito.when(helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeAsa.class), Mockito.anyBoolean());

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
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(ReturnMessageDto.class))).thenReturn(result);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(null);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		IAlfrescoCMISService alfrescoCMISService = Mockito.mock(IAlfrescoCMISService.class);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "alfrescoCMISService", alfrescoCMISService);

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
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeAsa.class), Mockito.anyBoolean());

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
		Mockito.when(accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(ReturnMessageDto.class))).thenReturn(result);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);

		try {
			result = service.saveDemande(idAgent, dto);
		} catch (ReturnMessageDtoException e) {
			result = e.getErreur();
		}

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
		Mockito.when(demandeRepository.getEntity(DemandeCongesExceptionnels.class, dto.getIdDemande())).thenReturn(new DemandeCongesExceptionnels());
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
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);
		Mockito.when(helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeCongesExceptionnels.class), Mockito.anyBoolean());

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
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(null);
		Mockito.when(accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(ReturnMessageDto.class)))
				.thenReturn(new ReturnMessageDto());

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		IAlfrescoCMISService alfrescoCMISService = Mockito.mock(IAlfrescoCMISService.class);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "alfrescoCMISService", alfrescoCMISService);

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
		Mockito.when(demandeRepository.getEntity(DemandeCongesExceptionnels.class, dto.getIdDemande())).thenReturn(new DemandeCongesExceptionnels());
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
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);
		Mockito.when(helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeAsa.class), Mockito.anyBoolean());

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
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(ReturnMessageDto.class))).thenReturn(result);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(null);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		IAlfrescoCMISService alfrescoCMISService = Mockito.mock(IAlfrescoCMISService.class);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "alfrescoCMISService", alfrescoCMISService);

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
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeCongesExceptionnels.class), Mockito.anyBoolean());

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
		Mockito.when(accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(ReturnMessageDto.class))).thenReturn(result);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);

		try {
			result = service.saveDemande(idAgent, dto);
		} catch (ReturnMessageDtoException e) {
			result = e.getErreur();
		}

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

		EtatDemandeReposComp ed = new EtatDemandeReposComp();
		ed.setDate(dateMaj);
		ed.setDemande((Demande) dr);
		ed.setEtat(RefEtatEnum.PROVISOIRE);
		ed.setIdAgent(9005138);
		ed.setIdEtatDemande(1);
		EtatDemandeReposComp ed2 = new EtatDemandeReposComp();
		ed2.setDate(dateMaj2);
		ed2.setDemande((Demande) dr);
		ed2.setEtat(RefEtatEnum.SAISIE);
		ed2.setIdAgent(9005138);
		ed2.setIdEtatDemande(2);
		ed2.setMotif("motif");
		ed2.setDuree(10);
		ed2.setDureeAnneeN1(10);
		ed2.setDateDebut(dateDebut);
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

		EtatDemandeReposComp ed = new EtatDemandeReposComp();
		ed.setDate(dateMaj);
		ed.setDemande((Demande) dr);
		ed.setEtat(RefEtatEnum.SAISIE);
		ed.setIdAgent(9005138);
		ed.setIdEtatDemande(1);
		EtatDemandeReposComp ed2 = new EtatDemandeReposComp();
		ed2.setDate(dateMaj2);
		ed2.setDemande((Demande) dr);
		ed2.setEtat(RefEtatEnum.APPROUVEE);
		ed2.setIdAgent(9005138);
		ed2.setIdEtatDemande(2);
		ed2.setMotif("motif");
		ed2.setDuree(10);
		ed2.setDureeAnneeN1(10);
		ed2.setDateDebut(dateDebut);
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

		EtatDemandeCongesExceptionnels ed = new EtatDemandeCongesExceptionnels();
		ed.setDate(dateMaj);
		ed.setDemande((Demande) dr);
		ed.setEtat(RefEtatEnum.PROVISOIRE);
		ed.setIdAgent(9005138);
		ed.setIdEtatDemande(1);
		ed.setDuree(20.0);
		EtatDemandeCongesExceptionnels ed2 = new EtatDemandeCongesExceptionnels();
		ed2.setDate(dateMaj2);
		ed2.setDemande((Demande) dr);
		ed2.setEtat(RefEtatEnum.SAISIE);
		ed2.setIdAgent(9005138);
		ed2.setIdEtatDemande(2);
		ed2.setMotif("motif");
		ed2.setDuree(10.0);
		ed2.setDateDebut(dateDebut);
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

		EtatDemandeCongesExceptionnels ed = new EtatDemandeCongesExceptionnels();
		ed.setDate(dateMaj);
		ed.setDemande((Demande) dr);
		ed.setEtat(RefEtatEnum.SAISIE);
		ed.setIdAgent(9005138);
		ed.setIdEtatDemande(1);
		ed.setDuree(2.0);
		EtatDemandeCongesExceptionnels ed2 = new EtatDemandeCongesExceptionnels();
		ed2.setDate(dateMaj2);
		ed2.setDemande((Demande) dr);
		ed2.setEtat(RefEtatEnum.APPROUVEE);
		ed2.setIdAgent(9005138);
		ed2.setIdEtatDemande(2);
		ed2.setMotif("motif");
		ed2.setDuree(10.0);
		ed2.setDateDebut(dateDebut);
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

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.REPOS_COMP.getValue());

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(Mockito.anyInt())).thenReturn(result);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(RefGroupeAbsence.class, dto.getGroupeAbsence().getIdRefGroupeAbsence())).thenReturn(groupe);
		Mockito.when(demandeRepository.getEntity(DemandeReposComp.class, dto.getIdDemande())).thenReturn(new DemandeReposComp());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeReposComp obj = (DemandeReposComp) args[0];

				assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(600, obj.getDuree().intValue());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeReposComp.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);
		Mockito.when(helperService.getDuree(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble()))
				.thenReturn(600.0);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeReposComp.class), Mockito.anyBoolean());

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
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(new RefTypeSaisi());

		IAlfrescoCMISService alfrescoCMISService = Mockito.mock(IAlfrescoCMISService.class);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "alfrescoCMISService", alfrescoCMISService);

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

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.REPOS_COMP.getValue());

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(RefGroupeAbsence.class, dto.getGroupeAbsence().getIdRefGroupeAbsence())).thenReturn(groupe);
		Mockito.when(demandeRepository.getEntity(DemandeReposComp.class, dto.getIdDemande())).thenReturn(new DemandeReposComp());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeReposComp obj = (DemandeReposComp) args[0];

				assertEquals(RefEtatEnum.SAISIE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeReposComp.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);
		Mockito.when(helperService.getDuree(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble()))
				.thenReturn(650.0);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeReposComp.class), Mockito.anyBoolean());

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
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(Mockito.anyInt())).thenReturn(result);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(new RefTypeSaisi());

		IAlfrescoCMISService alfrescoCMISService = Mockito.mock(IAlfrescoCMISService.class);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "alfrescoCMISService", alfrescoCMISService);

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
		Mockito.when(demandeRepository.getEntity(DemandeCongesExceptionnels.class, dto.getIdDemande())).thenReturn(new DemandeCongesExceptionnels());
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
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);
		Mockito.when(helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeCongesExceptionnels.class), Mockito.anyBoolean());

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
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(Mockito.anyInt())).thenReturn(result);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		IAlfrescoCMISService alfrescoCMISService = Mockito.mock(IAlfrescoCMISService.class);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "alfrescoCMISService", alfrescoCMISService);

		result = service.saveDemandeSIRH(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void saveDemandeSIRHCongesExcep_OK_avecEtatAttente_ParametrageSaisieKiosqueFalse() {

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

		RefEtatDto etatDto = new RefEtatDto();
		etatDto.setIdRefEtat(RefEtatEnum.EN_ATTENTE.getCodeEtat());
		dto.setEtatDto(etatDto);

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

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_EXCEP.getValue());

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(RefGroupeAbsence.class, dto.getGroupeAbsence().getIdRefGroupeAbsence())).thenReturn(groupe);
		Mockito.when(demandeRepository.getEntity(DemandeCongesExceptionnels.class, dto.getIdDemande())).thenReturn(new DemandeCongesExceptionnels());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeCongesExceptionnels obj = (DemandeCongesExceptionnels) args[0];

				assertEquals(2, obj.getEtatsDemande().size());
				assertEquals(RefEtatEnum.EN_ATTENTE.getCodeEtat(), obj.getEtatsDemande().get(1).getEtat().getCodeEtat());
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
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);
		Mockito.when(helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeCongesExceptionnels.class), Mockito.anyBoolean());

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
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(Mockito.anyInt())).thenReturn(result);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		IAlfrescoCMISService alfrescoCMISService = Mockito.mock(IAlfrescoCMISService.class);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "alfrescoCMISService", alfrescoCMISService);

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

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_EXCEP.getValue());

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(RefGroupeAbsence.class, dto.getGroupeAbsence().getIdRefGroupeAbsence())).thenReturn(groupe);
		Mockito.when(demandeRepository.getEntity(DemandeCongesExceptionnels.class, dto.getIdDemande())).thenReturn(new DemandeCongesExceptionnels());
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
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);
		Mockito.when(helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeCongesExceptionnels.class), Mockito.anyBoolean());

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
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(Mockito.anyInt())).thenReturn(result);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		IAlfrescoCMISService alfrescoCMISService = Mockito.mock(IAlfrescoCMISService.class);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "alfrescoCMISService", alfrescoCMISService);

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
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeReposComp.class), Mockito.anyBoolean());

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
		try {
			result = service.saveDemandeSIRH(idAgent, dto);
		} catch (ReturnMessageDtoException e) {
			result = e.getErreur();
		}

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
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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
		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(RefGroupeAbsence.class, dto.getGroupeAbsence().getIdRefGroupeAbsence())).thenReturn(groupe);
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
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);
		Mockito.when(helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeAsa.class), Mockito.anyBoolean());

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
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(Mockito.anyInt())).thenReturn(result);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		IAlfrescoCMISService alfrescoCMISService = Mockito.mock(IAlfrescoCMISService.class);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "alfrescoCMISService", alfrescoCMISService);

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
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(RefGroupeAbsence.class, dto.getGroupeAbsence().getIdRefGroupeAbsence())).thenReturn(groupe);
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
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);
		Mockito.when(helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeAsa.class), Mockito.anyBoolean());

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
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(Mockito.anyInt())).thenReturn(result);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		IAlfrescoCMISService alfrescoCMISService = Mockito.mock(IAlfrescoCMISService.class);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "alfrescoCMISService", alfrescoCMISService);

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
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeAsa.class), Mockito.anyBoolean());

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

		try {
			result = service.saveDemandeSIRH(idAgent, dto);
		} catch (ReturnMessageDtoException e) {
			result = e.getErreur();
		}

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

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.RECUP.getValue());

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(RefGroupeAbsence.class, dto.getGroupeAbsence().getIdRefGroupeAbsence())).thenReturn(groupe);
		Mockito.when(demandeRepository.getEntity(DemandeRecup.class, dto.getIdDemande())).thenReturn(new DemandeRecup());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeRecup obj = (DemandeRecup) args[0];

				assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeRecup.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeRecup.class), Mockito.anyBoolean());

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
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(Mockito.anyInt())).thenReturn(result);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(new RefTypeSaisi());

		IAlfrescoCMISService alfrescoCMISService = Mockito.mock(IAlfrescoCMISService.class);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "alfrescoCMISService", alfrescoCMISService);

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

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.RECUP.getValue());

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(Mockito.anyInt())).thenReturn(result);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(RefGroupeAbsence.class, dto.getGroupeAbsence().getIdRefGroupeAbsence())).thenReturn(groupe);
		Mockito.when(demandeRepository.getEntity(DemandeRecup.class, dto.getIdDemande())).thenReturn(new DemandeRecup());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeRecup obj = (DemandeRecup) args[0];

				assertEquals(RefEtatEnum.SAISIE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeRecup.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeRecup.class), Mockito.anyBoolean());

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
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(new RefTypeSaisi());

		IAlfrescoCMISService alfrescoCMISService = Mockito.mock(IAlfrescoCMISService.class);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "alfrescoCMISService", alfrescoCMISService);

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
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeRecup.class), Mockito.anyBoolean());

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
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(Mockito.anyInt())).thenReturn(result);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		try {
			result = service.saveDemandeSIRH(idAgent, dto);
		} catch (ReturnMessageDtoException e) {
			result = e.getErreur();
		}

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
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

		AgentWithServiceDto agentWithServiceDto = new AgentWithServiceDto();
		agentWithServiceDto.setIdAgent(9005138);

		DemandeDto dto = new DemandeDto();
		dto.setAgentWithServiceDto(agentWithServiceDto);
		dto.setIdDemande(1);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.ASA_A48.getValue());
		dto.setGroupeAbsence(groupeAbsence);

		listdemandeDto.add(dto);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.listeDemandesSIRH(null, null, null, null, null, null)).thenReturn(listdemande);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.when(absDataConsistencyRules.filtreDateAndEtatDemandeFromList(listdemande, null, null, true)).thenReturn(listdemandeDto);
		Mockito.when(absDataConsistencyRules.filtreDroitOfDemandeSIRH(dto)).thenReturn(dto);
		Mockito.when(absDataConsistencyRules.checkDepassementCompteurAgent(Mockito.any(DemandeDto.class), Mockito.any(CheckCompteurAgentVo.class)))
				.thenReturn(true);

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		ICongesAnnuelsRepository congeAnnuelRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congeAnnuelRepository.getListRestitutionMassiveByIdAgent(Arrays.asList(9005138), null, null)).thenReturn(null);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "congeAnnuelRepository", congeAnnuelRepository);

		List<DemandeDto> listResult = service.getListeDemandesSIRH(null, null, null, null, null, null, null, null, null);

		assertEquals(1, listResult.size());
		assertTrue(listResult.get(0).isDepassementCompteur());
		assertFalse(listResult.get(0).isDepassementMultiple());

	}

	@SuppressWarnings("unchecked")
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

		EtatDemandeRecup ed = new EtatDemandeRecup();
		ed.setDate(dateMaj);
		ed.setDemande((Demande) dr);
		ed.setEtat(RefEtatEnum.PROVISOIRE);
		ed.setIdAgent(9005138);
		ed.setIdEtatDemande(1);
		ed.setDuree(30);
		EtatDemandeRecup ed2 = new EtatDemandeRecup();
		ed2.setDate(dateMaj2);
		ed2.setDemande((Demande) dr);
		ed2.setEtat(RefEtatEnum.SAISIE);
		ed2.setIdAgent(9005138);
		ed2.setIdEtatDemande(2);
		ed2.setDuree(10);
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

		IAgentService agentService = Mockito.mock(IAgentService.class);
		Mockito.when(agentService.getAgentOptimise(Mockito.anyList(), Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(new AgentWithServiceDto());

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "agentService", agentService);

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
		Mockito.when(demandeRepository.getEntity(Demande.class, demandeEtatChangeDto.getIdDemande())).thenReturn(demande);

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
		assertEquals("L'agent n'est pas habilité à valider ou rejeter la demande de cet agent.", result.getErrors().get(0).toString());
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

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());
		RefTypeAbsence type = new RefTypeAbsence();
		type.setGroupe(groupe);
		Demande d = new Demande();
		d.setType(type);
		Demande demande = Mockito.spy(d);

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
		}).when(absDataConsistencyRules).checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(List.class));

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
		}).when(absDataConsistencyRules).checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(List.class));
		
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeAsa.class), Mockito.anyBoolean());
		
		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		ICounterService counterService = Mockito.mock(ICounterService.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				result.getErrors().add("erreur maj compteur");
				return result;
			}
		}).when(counterService).majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(DemandeEtatChangeDto.class));

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(counterServiceFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande.getType().getIdRefTypeAbsence()))
				.thenReturn(counterService);

		ReturnMessageDto messageAgent = new ReturnMessageDto();
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(messageAgent);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		// ReflectionTestUtils.setField(service, "counterService", counterService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "counterServiceFactory", counterServiceFactory);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

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
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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
		}).when(absDataConsistencyRules).checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(List.class));
		
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeAsa.class), Mockito.anyBoolean());

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
		}).when(counterService).majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(DemandeEtatChangeDto.class));

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(counterServiceFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande.getType().getIdRefTypeAbsence()))
				.thenReturn(counterService);

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "counterServiceFactory", counterServiceFactory);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

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
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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
		}).when(absDataConsistencyRules).checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(List.class));
		
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeAsa.class), Mockito.anyBoolean());

		ICounterService counterService = Mockito.mock(ICounterService.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		}).when(counterService).majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(DemandeEtatChangeDto.class));

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(counterServiceFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande.getType().getIdRefTypeAbsence()))
				.thenReturn(counterService);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(srm);

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "counterServiceFactory", counterServiceFactory);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

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
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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
		}).when(absDataConsistencyRules).checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(List.class));
		
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeAsa.class), Mockito.anyBoolean());

		ICounterService counterService = Mockito.mock(ICounterService.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		}).when(counterService).majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(DemandeEtatChangeDto.class));

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(counterServiceFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande.getType().getIdRefTypeAbsence()))
				.thenReturn(counterService);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(srm);

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "counterServiceFactory", counterServiceFactory);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

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
		}).when(absDataConsistencyRules).checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(List.class));

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
	public void setDemandeEtatSIRH_setDemandeEtatEnAttente_ok_Autre() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = new ReturnMessageDto();

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.EN_ATTENTE.getCodeEtat());
		dto.setIdDemande(1);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.RECUP.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.RECUP.getValue());
		type.setGroupe(groupe);

		DemandeRecup demande = Mockito.spy(new DemandeRecup());
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
		}).when(absDataConsistencyRules).checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(List.class));

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
	public void setDemandeEtatSIRH_setDemandeEtatEnAttente_ok_Conge() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = new ReturnMessageDto();

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.EN_ATTENTE.getCodeEtat());
		dto.setIdDemande(1);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.CONGE_ANNUEL.getValue());
		type.setGroupe(groupe);

		DemandeCongesAnnuels demande = Mockito.spy(new DemandeCongesAnnuels());
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
		}).when(absDataConsistencyRules).checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(List.class));

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
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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
		}).when(absDataConsistencyRules).checkEtatsDemandeAnnulee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(List.class));
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				result.getErrors().add("Le motif est obligatoire.");
				return result;
			}
		}).when(absDataConsistencyRules).checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(), Mockito.anyString());

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

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
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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
		}).when(absDataConsistencyRules).checkEtatsDemandeAnnulee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(List.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		}).when(absDataConsistencyRules).checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(), Mockito.anyString());

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		ICounterService counterService = Mockito.mock(ICounterService.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				result.getErrors().add("erreur maj compteur");
				return result;
			}
		}).when(counterService).majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(DemandeEtatChangeDto.class));

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(counterServiceFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande.getType().getIdRefTypeAbsence()))
				.thenReturn(counterService);

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

		EtatDemandeReposComp etatDemande = new EtatDemandeReposComp();
		etatDemande.setEtat(RefEtatEnum.APPROUVEE);
		etatDemande.setDuree(10);
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
		demande.setDateDebut(new Date());
		demande.setDateFin(new Date());

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
		}).when(absDataConsistencyRules).checkEtatsDemandeAnnulee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(List.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		}).when(absDataConsistencyRules).checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(), Mockito.anyString());

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		ICounterService counterService = Mockito.mock(ICounterService.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		}).when(counterService).majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(DemandeEtatChangeDto.class));

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(counterServiceFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande.getType().getIdRefTypeAbsence()))
				.thenReturn(counterService);

		Spcarr carr = new Spcarr(5138, 20140101);
		carr.setDateFin(0);
		carr.setCdcate(8);

		HelperService helper = Mockito.mock(HelperService.class);
		Mockito.when(helper.isContractuel(carr)).thenReturn(false);
		Mockito.when(helper.isConventionCollective(carr)).thenReturn(false);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(carr);

		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(demande.getIdAgent())).thenReturn(5138);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "counterServiceFactory", counterServiceFactory);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeService);
		ReflectionTestUtils.setField(service, "helperService", helper);

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
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);
		Mockito.when(helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeAsa.class), Mockito.anyBoolean());

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
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(null);
		Mockito.when(accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(ReturnMessageDto.class)))
				.thenReturn(new ReturnMessageDto());

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		IAlfrescoCMISService alfrescoCMISService = Mockito.mock(IAlfrescoCMISService.class);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "alfrescoCMISService", alfrescoCMISService);

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
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);
		Mockito.when(helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeAsa.class), Mockito.anyBoolean());

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
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(ReturnMessageDto.class))).thenReturn(result);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(null);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		IAlfrescoCMISService alfrescoCMISService = Mockito.mock(IAlfrescoCMISService.class);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "alfrescoCMISService", alfrescoCMISService);

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
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);
		Mockito.when(helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeAsa.class), Mockito.anyBoolean());

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
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(null);
		Mockito.when(accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(ReturnMessageDto.class)))
				.thenReturn(new ReturnMessageDto());

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		IAlfrescoCMISService alfrescoCMISService = Mockito.mock(IAlfrescoCMISService.class);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "alfrescoCMISService", alfrescoCMISService);

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
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);
		Mockito.when(helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeAsa.class), Mockito.anyBoolean());

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
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(ReturnMessageDto.class))).thenReturn(result);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(null);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		IAlfrescoCMISService alfrescoCMISService = Mockito.mock(IAlfrescoCMISService.class);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "alfrescoCMISService", alfrescoCMISService);

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
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(RefGroupeAbsence.class, dto.getGroupeAbsence().getIdRefGroupeAbsence())).thenReturn(groupe);
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
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);
		Mockito.when(helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeAsa.class), Mockito.anyBoolean());

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
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(Mockito.anyInt())).thenReturn(result);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		IAlfrescoCMISService alfrescoCMISService = Mockito.mock(IAlfrescoCMISService.class);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "alfrescoCMISService", alfrescoCMISService);

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
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(RefGroupeAbsence.class, dto.getGroupeAbsence().getIdRefGroupeAbsence())).thenReturn(groupe);
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
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);
		Mockito.when(helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeAsa.class), Mockito.anyBoolean());

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
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(Mockito.anyInt())).thenReturn(result);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		IAlfrescoCMISService alfrescoCMISService = Mockito.mock(IAlfrescoCMISService.class);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "alfrescoCMISService", alfrescoCMISService);

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
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(RefGroupeAbsence.class, dto.getGroupeAbsence().getIdRefGroupeAbsence())).thenReturn(groupe);
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
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);
		Mockito.when(helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeAsa.class), Mockito.anyBoolean());

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
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(Mockito.anyInt())).thenReturn(result);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		IAlfrescoCMISService alfrescoCMISService = Mockito.mock(IAlfrescoCMISService.class);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "alfrescoCMISService", alfrescoCMISService);

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
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(RefGroupeAbsence.class, dto.getGroupeAbsence().getIdRefGroupeAbsence())).thenReturn(groupe);
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
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);
		Mockito.when(helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeAsa.class), Mockito.anyBoolean());

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
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(Mockito.anyInt())).thenReturn(result);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		IAlfrescoCMISService alfrescoCMISService = Mockito.mock(IAlfrescoCMISService.class);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "alfrescoCMISService", alfrescoCMISService);

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
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(RefGroupeAbsence.class, dto.getGroupeAbsence().getIdRefGroupeAbsence())).thenReturn(groupe);
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
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);
		Mockito.when(helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeAsa.class), Mockito.anyBoolean());

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
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(Mockito.anyInt())).thenReturn(result);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		IAlfrescoCMISService alfrescoCMISService = Mockito.mock(IAlfrescoCMISService.class);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "alfrescoCMISService", alfrescoCMISService);

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
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(RefGroupeAbsence.class, dto.getGroupeAbsence().getIdRefGroupeAbsence())).thenReturn(groupe);
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
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);
		Mockito.when(helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeAsa.class), Mockito.anyBoolean());

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
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(Mockito.anyInt())).thenReturn(result);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		IAlfrescoCMISService alfrescoCMISService = Mockito.mock(IAlfrescoCMISService.class);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "alfrescoCMISService", alfrescoCMISService);

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
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(RefGroupeAbsence.class, dto.getGroupeAbsence().getIdRefGroupeAbsence())).thenReturn(groupe);
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
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);
		Mockito.when(helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeAsa.class), Mockito.anyBoolean());

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
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(Mockito.anyInt())).thenReturn(result);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		IAlfrescoCMISService alfrescoCMISService = Mockito.mock(IAlfrescoCMISService.class);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "alfrescoCMISService", alfrescoCMISService);

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
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(RefGroupeAbsence.class, dto.getGroupeAbsence().getIdRefGroupeAbsence())).thenReturn(groupe);
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
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);
		Mockito.when(helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeAsa.class), Mockito.anyBoolean());

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
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(Mockito.anyInt())).thenReturn(result);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		IAlfrescoCMISService alfrescoCMISService = Mockito.mock(IAlfrescoCMISService.class);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "alfrescoCMISService", alfrescoCMISService);

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
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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
		Mockito.when(accessRightsRepository.isApprobateurOrDelegataireOfAgent(idAgent, demande.getIdAgent())).thenReturn(true);

		AbsCongesAnnuelsDataConsistencyRulesImpl absenceDataConsistencyRulesImpl = Mockito.mock(AbsCongesAnnuelsDataConsistencyRulesImpl.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		}).when(absenceDataConsistencyRulesImpl).checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
				Mockito.isA(List.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		}).when(absenceDataConsistencyRulesImpl).checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(), Mockito.anyString());

		ICounterService counterService = Mockito.mock(ICounterService.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		}).when(counterService).majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(DemandeEtatChangeDto.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		}).when(absenceDataConsistencyRulesImpl).checkSaisieKiosqueAutorisee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(RefTypeSaisi.class),
				Mockito.eq(false));

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(counterServiceFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande.getType().getIdRefTypeAbsence()))
				.thenReturn(counterService);

		Mockito.doNothing().when(absenceDataConsistencyRulesImpl).checkSamediOffertToujoursOk(dto, demande);

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande.getType().getIdRefTypeAbsence()))
				.thenReturn(absenceDataConsistencyRulesImpl);

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		}).when(absenceDataConsistencyRulesImpl).checkDepassementDroitsAcquis(Mockito.any(ReturnMessageDto.class), Mockito.any(DemandeRecup.class),
				Mockito.any(CheckCompteurAgentVo.class));

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absenceDataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "counterServiceFactory", counterServiceFactory);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

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
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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
		Mockito.when(accessRightsRepository.isApprobateurOrDelegataireOfAgent(idAgent, demande.getIdAgent())).thenReturn(true);

		AbsCongesAnnuelsDataConsistencyRulesImpl absenceDataConsistencyRulesImpl = Mockito.mock(AbsCongesAnnuelsDataConsistencyRulesImpl.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		}).when(absenceDataConsistencyRulesImpl).checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
				Mockito.isA(List.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		}).when(absenceDataConsistencyRulesImpl).checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(), Mockito.anyString());

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		}).when(absenceDataConsistencyRulesImpl).checkSaisieKiosqueAutorisee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(RefTypeSaisi.class),
				Mockito.eq(false));

		ICounterService counterService = Mockito.mock(ICounterService.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		}).when(counterService).majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(DemandeEtatChangeDto.class));

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(counterServiceFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande.getType().getIdRefTypeAbsence()))
				.thenReturn(counterService);

		Mockito.doNothing().when(absenceDataConsistencyRulesImpl).checkSamediOffertToujoursOk(dto, demande);

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande.getType().getIdRefTypeAbsence()))
				.thenReturn(absenceDataConsistencyRulesImpl);

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		}).when(absenceDataConsistencyRulesImpl).checkDepassementDroitsAcquis(Mockito.any(ReturnMessageDto.class), Mockito.any(DemandeRecup.class),
				Mockito.any(CheckCompteurAgentVo.class));

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absenceDataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "counterServiceFactory", counterServiceFactory);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

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
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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
		}).when(absDataConsistencyRules).checkEtatsDemandeAnnulee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(List.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		}).when(absDataConsistencyRules).checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(), Mockito.anyString());

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

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
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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
		}).when(absDataConsistencyRules).checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(List.class));
		
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeAsa.class), Mockito.anyBoolean());

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
		}).when(counterService).majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(DemandeEtatChangeDto.class));

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(counterServiceFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande.getType().getIdRefTypeAbsence()))
				.thenReturn(counterService);

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "counterServiceFactory", counterServiceFactory);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

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
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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
		}).when(absDataConsistencyRules).checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(List.class));

		ICounterService counterService = Mockito.mock(ICounterService.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		}).when(counterService).majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(DemandeEtatChangeDto.class));
		
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeAsa.class), Mockito.anyBoolean());

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(counterServiceFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande.getType().getIdRefTypeAbsence()))
				.thenReturn(counterService);

		ReturnMessageDto messageAgent = new ReturnMessageDto();
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(messageAgent);

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "counterServiceFactory", counterServiceFactory);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.setDemandeEtatSIRH(idAgent, Arrays.asList(dto));

		assertEquals(0, result.getErrors().size());
		assertEquals("La demande est validée.", result.getInfos().get(0));
		Mockito.verify(demande, Mockito.times(1)).addEtatDemande(Mockito.isA(EtatDemande.class));
	}

	@Test
	public void setDemandeEtatPris_EtatIncorrect_ReturnError_A48() {

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

		AgentWithServiceDto agentWithServiceDto = new AgentWithServiceDto();
		agentWithServiceDto.setIdAgent(9005138);

		DemandeDto dto = new DemandeDto();
		dto.setAgentWithServiceDto(agentWithServiceDto);
		dto.setIdDemande(1);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.ASA_A54.getValue());
		dto.setGroupeAbsence(groupeAbsence);

		listdemandeDto.add(dto);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.listeDemandesSIRH(null, null, null, null, null, null)).thenReturn(listdemande);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.when(absDataConsistencyRules.filtreDateAndEtatDemandeFromList(listdemande, null, null, true)).thenReturn(listdemandeDto);
		Mockito.when(absDataConsistencyRules.filtreDroitOfDemandeSIRH(dto)).thenReturn(dto);
		Mockito.when(absDataConsistencyRules.checkDepassementCompteurAgent(Mockito.any(DemandeDto.class), Mockito.any(CheckCompteurAgentVo.class)))
				.thenReturn(true);

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		ICongesAnnuelsRepository congeAnnuelRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congeAnnuelRepository.getListRestitutionMassiveByIdAgent(Arrays.asList(9005138), null, null)).thenReturn(null);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "congeAnnuelRepository", congeAnnuelRepository);

		List<DemandeDto> listResult = service.getListeDemandesSIRH(null, null, null, null, null, null, null, null, null);

		assertEquals(1, listResult.size());
		assertTrue(listResult.get(0).isDepassementCompteur());
		assertFalse(listResult.get(0).isDepassementMultiple());
	}

	@Test
	public void getDemande_ASA_WithResult_isEtatDefinitif() {

		Date dateDebut = new Date();
		Date dateFin = new Date();
		Date dateMaj = new Date();
		Date dateMaj2 = new Date();
		Integer idDemande = 1;

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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
		EtatDemandeAsa ed2 = new EtatDemandeAsa();
		ed2.setDate(dateMaj2);
		ed2.setDemande((Demande) dr);
		ed2.setEtat(RefEtatEnum.SAISIE);
		ed2.setIdAgent(9005138);
		ed2.setIdEtatDemande(2);
		ed2.setMotif("motif");
		ed2.setDuree(20.0);
		ed2.setDateDebut(dateDebut);
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
	public void getListeDemandes_return1Liste_WithA54() throws Exception {

		List<Demande> listdemande = new ArrayList<Demande>();
		Demande d = new Demande();
		d.setIdDemande(1);
		d.setIdAgent(9005131);
		listdemande.add(d);

		List<DemandeDto> listdemandeDto = new ArrayList<DemandeDto>();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

		AgentWithServiceDto agentWithServiceDto = new AgentWithServiceDto();

		DemandeDto dto = new DemandeDto();
		dto.setAgentWithServiceDto(agentWithServiceDto);
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
		Mockito.when(demandeRepository.listeIdsDemandesForListAgent(9005138, Arrays.asList(9005131), dat, null, null, null, null, null)).thenReturn(Arrays.asList(1,2));
		Mockito.when(demandeRepository.listeDemandesByListIdsDemande(Arrays.asList(1,2))).thenReturn(listdemande);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.when(absDataConsistencyRules.filtreDateAndEtatDemandeFromList(listdemande, listEtat, null, false)).thenReturn(listdemandeDto);

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);
		Mockito.when(absDataConsistencyRules.filtreDroitOfDemande(9005138, dto, listDroitAgent, false)).thenReturn(dto);
		Mockito.when(absDataConsistencyRules.checkDepassementCompteurAgent(Mockito.any(DemandeDto.class), Mockito.any(CheckCompteurAgentVo.class)))
				.thenReturn(false);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDateMoinsUnAn()).thenReturn(dat);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(Arrays.asList(9005138));

		IFiltreService filtresService = Mockito.mock(IFiltreService.class);
		Mockito.when(filtresService.getListeEtatsByOnglet("TOUTES", new ArrayList<Integer>())).thenReturn(listEtat);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.getListOfAgentsToInputOrApprove(9005138)).thenReturn(listDroitAgent);

		ICongesAnnuelsRepository congeAnnuelRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congeAnnuelRepository.getListRestitutionMassiveByIdAgent(Arrays.asList(9005138), null, null)).thenReturn(null);

		DemandeRecursiveTask multiTask = PowerMockito.mock(DemandeRecursiveTask.class);
		PowerMockito.whenNew(DemandeRecursiveTask.class)
				.withArguments(Mockito.anyMap(), Mockito.anyList(), Mockito.anyInt(), Mockito.anyList(), Mockito.anyBoolean()).thenReturn(multiTask);

		ForkJoinPool pool = PowerMockito.mock(ForkJoinPool.class);
		PowerMockito.whenNew(ForkJoinPool.class).withNoArguments().thenReturn(pool);
		Mockito.when(pool.invoke(multiTask)).thenReturn(listdemandeDto);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "filtresService", filtresService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "congeAnnuelRepository", congeAnnuelRepository);

		ResultListDemandeDto listResult = service.getListeDemandes(9005138, Arrays.asList(9005131), "TOUTES", null, null, null, null, null, null, false);

		assertEquals(1, listResult.getListDemandesDto().size());
		assertFalse(listResult.getListDemandesDto().get(0).isDepassementCompteur());
		assertFalse(listResult.getListDemandesDto().get(0).isDepassementMultiple());
	}

	@Test
	public void getListeDemandes_noResult() throws Exception {

		List<Demande> listdemande = new ArrayList<Demande>();

		List<DemandeDto> listdemandeDto = new ArrayList<DemandeDto>();

		RefEtat etat = new RefEtat();
		etat.setLabel("APPROUVE");
		List<RefEtat> listEtat = new ArrayList<RefEtat>();
		listEtat.add(etat);

		Date date = new Date();

		List<DroitsAgent> listDroitAgent = new ArrayList<DroitsAgent>();

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.listeDemandesAgent(9005138, 9005131, date, null, null, null)).thenReturn(listdemande);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.when(absDataConsistencyRules.filtreDateAndEtatDemandeFromList(listdemande, listEtat, null, false)).thenReturn(listdemandeDto);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDateMoinsUnAn()).thenReturn(date);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(Arrays.asList(9005138));

		IFiltreService filtresService = Mockito.mock(IFiltreService.class);
		Mockito.when(filtresService.getListeEtatsByOnglet("TOUTES", null)).thenReturn(listEtat);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.getListOfAgentsToInputOrApprove(9005138)).thenReturn(listDroitAgent);

		ICongesAnnuelsRepository congeAnnuelRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congeAnnuelRepository.getListRestitutionMassiveByIdAgent(Arrays.asList(9005138), null, null)).thenReturn(null);

		DemandeRecursiveTask multiTask = PowerMockito.mock(DemandeRecursiveTask.class);
		PowerMockito.whenNew(DemandeRecursiveTask.class)
				.withArguments(Mockito.anyMap(), Mockito.anyList(), Mockito.anyInt(), Mockito.anyList(), Mockito.anyBoolean()).thenReturn(multiTask);

		ForkJoinPool pool = PowerMockito.mock(ForkJoinPool.class);
		PowerMockito.whenNew(ForkJoinPool.class).withNoArguments().thenReturn(pool);
		Mockito.when(pool.invoke(multiTask)).thenReturn(listdemandeDto);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "filtresService", filtresService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "congeAnnuelRepository", congeAnnuelRepository);

		ResultListDemandeDto listResult = service.getListeDemandes(9005138, Arrays.asList(9005131), "TOUTES", null, null, null, null, null, null, false);

		assertEquals(0, listResult.getListDemandesDto().size());
	}

	@Test
	public void saveDemandeAsa50_OK_avecEtatProvisoire() {

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);
		Mockito.when(helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeAsa.class), Mockito.anyBoolean());

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
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(null);
		Mockito.when(accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(ReturnMessageDto.class)))
				.thenReturn(new ReturnMessageDto());

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		IAlfrescoCMISService alfrescoCMISService = Mockito.mock(IAlfrescoCMISService.class);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "alfrescoCMISService", alfrescoCMISService);

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
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);
		Mockito.when(helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeAsa.class), Mockito.anyBoolean());

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
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(ReturnMessageDto.class))).thenReturn(result);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(null);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		IAlfrescoCMISService alfrescoCMISService = Mockito.mock(IAlfrescoCMISService.class);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "alfrescoCMISService", alfrescoCMISService);

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
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);
		Mockito.when(helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeAsa.class), Mockito.anyBoolean());

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
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(null);
		Mockito.when(accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(ReturnMessageDto.class)))
				.thenReturn(new ReturnMessageDto());

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		IAlfrescoCMISService alfrescoCMISService = Mockito.mock(IAlfrescoCMISService.class);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "alfrescoCMISService", alfrescoCMISService);

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
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

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
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);
		Mockito.when(helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateDebut);
		Mockito.when(helperService.getDuree(typeSaisi, dateDebut, dateFin, dto.getDuree())).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeAsa.class), Mockito.anyBoolean());

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
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(ReturnMessageDto.class))).thenReturn(result);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(null);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		IAlfrescoCMISService alfrescoCMISService = Mockito.mock(IAlfrescoCMISService.class);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "alfrescoCMISService", alfrescoCMISService);

		result = service.saveDemande(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void getListeDemandesSIRHAValider_return1Liste_WithA48() {

		List<Demande> listdemande = new ArrayList<Demande>();
		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());
		RefTypeAbsence type = new RefTypeAbsence();
		type.setGroupe(groupe);
		Demande d = new Demande();
		d.setIdDemande(1);
		d.setType(type);
		listdemande.add(d);

		List<DemandeDto> listdemandeDto = new ArrayList<DemandeDto>();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

		AgentWithServiceDto agentWithServiceDto = new AgentWithServiceDto();
		agentWithServiceDto.setIdAgent(9005138);

		DemandeDto dto = new DemandeDto();
		dto.setAgentWithServiceDto(agentWithServiceDto);
		dto.setIdDemande(1);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.ASA_A48.getValue());
		dto.setGroupeAbsence(groupeAbsence);
		dto.setIdRefEtat(10);

		listdemandeDto.add(dto);

		RefEtat refAppro = new RefEtat();
		refAppro.setIdRefEtat(4);
		RefEtat refAttente = new RefEtat();
		refAttente.setIdRefEtat(10);
		List<RefEtat> listEtat = new ArrayList<>();
		listEtat.add(refAttente);
		listEtat.add(refAppro);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.listeDemandesCongesAnnuelsSIRHAValider(null, null, null, 150)).thenReturn(listdemande);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.when(absDataConsistencyRules.filtreDateAndEtatDemandeFromList(listdemande, listEtat, null, true)).thenReturn(listdemandeDto);
		Mockito.when(absDataConsistencyRules.filtreDroitOfDemandeSIRH(dto)).thenReturn(dto);
		Mockito.when(absDataConsistencyRules.checkDepassementCompteurAgent(Mockito.any(DemandeDto.class), Mockito.any(CheckCompteurAgentVo.class)))
				.thenReturn(true);

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefEtatAValider()).thenReturn(listEtat);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);

		List<DemandeDto> listResult = service.getListeDemandesSIRHAValider(null, null, null, null, null, null, null);

		assertEquals(1, listResult.size());
		assertTrue(listResult.get(0).isDepassementCompteur());
		assertFalse(listResult.get(0).isDepassementMultiple());

	}

	@Test
	public void saveDemandeCongesAnnuel_OK_avecEtatProvisoire() {

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();
		Date dateReprise = new Date();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.5);
		dto.setIdTypeDemande(1);
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

		RefTypeSaisiCongeAnnuel typeSaisi = new RefTypeSaisiCongeAnnuel();
		typeSaisi.setIdRefTypeSaisiCongeAnnuel(1);
		typeSaisi.setCodeBaseHoraireAbsence("A");
		dto.setTypeSaisiCongeAnnuel(new RefTypeSaisiCongeAnnuelDto(typeSaisi));

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setDateDebut(dateDebut);
		demande.setDateFin(dateFin);
		demande.setTypeSaisiCongeAnnuel(typeSaisi);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isUserOperateur(idAgent)).thenReturn(true);
		Mockito.when(accessRightsRepository.getAgentDroitFetchAgents(idAgent)).thenReturn(droitOperateur);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(DemandeCongesAnnuels.class, dto.getIdDemande())).thenReturn(new DemandeCongesAnnuels());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeCongesAnnuels obj = (DemandeCongesAnnuels) args[0];

				assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(obj.getDuree(), new Double(0.0));

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeCongesAnnuels.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getDateFinCongeAnnuel(Mockito.any(RefTypeSaisiCongeAnnuel.class), Mockito.any(Date.class), Mockito.any(Date.class),
				Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.any(Date.class))).thenReturn(dateFin);
		Mockito.when(helperService.getDateDebutCongeAnnuel(Mockito.any(RefTypeSaisiCongeAnnuel.class), Mockito.any(Date.class), Mockito.anyBoolean(),
				Mockito.anyBoolean())).thenReturn(dateDebut);
		Mockito.when(helperService.getDureeCongeAnnuel(demande, dateReprise, dto.isForceSaisieManuelleDuree(), dto.getDuree())).thenReturn(2.0);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeCongesAnnuels.class), Mockito.anyBoolean());

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
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(null);
		Mockito.when(accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(ReturnMessageDto.class)))
				.thenReturn(new ReturnMessageDto());

		RefTypeAbsence ref = new RefTypeAbsence();
		ref.setIdRefTypeAbsence(RefTypeAbsenceEnum.CONGE_ANNUEL.getValue());

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.getEntity(RefTypeAbsence.class, 1)).thenReturn(ref);
		Mockito.when(filtreRepository.getEntity(RefTypeSaisiCongeAnnuel.class, 1)).thenReturn(typeSaisi);

		IAlfrescoCMISService alfrescoCMISService = Mockito.mock(IAlfrescoCMISService.class);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "alfrescoCMISService", alfrescoCMISService);

		result = service.saveDemande(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void saveDemandeCongesAnnuel_OK_avecEtatSaisie() {

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();
		Date dateReprise = new Date();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.0);
		dto.setGroupeAbsence(groupeAbsence);
		dto.setIdTypeDemande(1);

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

		RefTypeSaisiCongeAnnuel typeSaisi = new RefTypeSaisiCongeAnnuel();
		typeSaisi.setIdRefTypeSaisiCongeAnnuel(1);
		typeSaisi.setCodeBaseHoraireAbsence("A");
		dto.setTypeSaisiCongeAnnuel(new RefTypeSaisiCongeAnnuelDto(typeSaisi));

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setDateDebut(dateDebut);
		demande.setDateFin(dateFin);
		demande.setTypeSaisiCongeAnnuel(typeSaisi);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isUserOperateur(idAgent)).thenReturn(true);
		Mockito.when(accessRightsRepository.getAgentDroitFetchAgents(idAgent)).thenReturn(droitOperateur);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(DemandeCongesAnnuels.class, dto.getIdDemande())).thenReturn(new DemandeCongesAnnuels());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeCongesAnnuels obj = (DemandeCongesAnnuels) args[0];

				assertEquals(RefEtatEnum.SAISIE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(obj.getDuree(), new Double(0.0));

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeCongesAnnuels.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getDateFinCongeAnnuel(Mockito.any(RefTypeSaisiCongeAnnuel.class), Mockito.any(Date.class), Mockito.any(Date.class),
				Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.any(Date.class))).thenReturn(dateFin);
		Mockito.when(helperService.getDateDebutCongeAnnuel(Mockito.any(RefTypeSaisiCongeAnnuel.class), Mockito.any(Date.class), Mockito.anyBoolean(),
				Mockito.anyBoolean())).thenReturn(dateDebut);
		Mockito.when(helperService.getDureeCongeAnnuel(demande, dateReprise, dto.isForceSaisieManuelleDuree(), dto.getDuree())).thenReturn(3.5);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeAsa.class), Mockito.anyBoolean());

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
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(ReturnMessageDto.class))).thenReturn(result);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(null);

		RefTypeAbsence ref = new RefTypeAbsence();
		ref.setIdRefTypeAbsence(RefTypeAbsenceEnum.CONGE_ANNUEL.getValue());

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.getEntity(RefTypeAbsence.class, 1)).thenReturn(ref);
		Mockito.when(filtreRepository.getEntity(RefTypeSaisiCongeAnnuel.class, 1)).thenReturn(typeSaisi);

		IAlfrescoCMISService alfrescoCMISService = Mockito.mock(IAlfrescoCMISService.class);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "alfrescoCMISService", alfrescoCMISService);

		result = service.saveDemande(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void saveDemandeCongesAnnuel_Ko() {

		ReturnMessageDto result = new ReturnMessageDto();
		result.getErrors().add("erreur droit");

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());

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
				DemandeCongesAnnuels obj = (DemandeCongesAnnuels) args[0];

				assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(10, obj.getDuree().intValue());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeCongesAnnuels.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getDateFinCongeAnnuel(Mockito.any(RefTypeSaisiCongeAnnuel.class), Mockito.any(Date.class), Mockito.any(Date.class),
				Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.any(Date.class))).thenReturn(dateFin);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeCongesAnnuels.class), Mockito.anyBoolean());

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
		Mockito.when(accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(ReturnMessageDto.class))).thenReturn(result);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);

		try {
			result = service.saveDemande(idAgent, dto);
		} catch (ReturnMessageDtoException e) {
			result = e.getErreur();
		}

		assertEquals(1, result.getErrors().size());
		assertEquals("erreur droit", result.getErrors().get(0).toString());
		Mockito.verify(demandeRepository, Mockito.times(0)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void getDemande_CongesAnnuel_WithResult_isEtatDefinitif() {

		Date dateDebut = new Date();
		Date dateFin = new Date();
		Date dateMaj = new Date();
		Date dateMaj2 = new Date();
		Integer idDemande = 1;

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());

		RefTypeAbsence rta = new RefTypeAbsence();
		rta.setIdRefTypeAbsence(26);
		rta.setGroupe(groupe);

		Demande d = new Demande();
		d.setType(rta);

		DemandeCongesAnnuels dr = new DemandeCongesAnnuels();

		EtatDemandeCongesAnnuels ed = new EtatDemandeCongesAnnuels();
		ed.setDate(dateMaj);
		ed.setDemande((Demande) dr);
		ed.setEtat(RefEtatEnum.PROVISOIRE);
		ed.setIdAgent(9005138);
		ed.setIdEtatDemande(1);
		EtatDemandeCongesAnnuels ed2 = new EtatDemandeCongesAnnuels();
		ed2.setDate(dateMaj2);
		ed2.setDemande((Demande) dr);
		ed2.setEtat(RefEtatEnum.SAISIE);
		ed2.setIdAgent(9005138);
		ed2.setIdEtatDemande(2);
		ed2.setMotif("motif");
		ed2.setDuree(10.0);
		ed2.setDateDebut(dateDebut);
		ed2.setNbSamediOffert(0.0);
		List<EtatDemande> listEtatDemande = new ArrayList<EtatDemande>();
		listEtatDemande.addAll(Arrays.asList(ed2, ed));

		dr.setDateDebut(dateDebut);
		dr.setDateFin(dateFin);
		dr.setDuree(10.0);
		dr.setEtatsDemande(listEtatDemande);
		dr.setIdAgent(9005138);
		dr.setIdDemande(idDemande);
		dr.setType(rta);
		dr.setNbSamediOffert(0.0);

		Date date = new Date();
		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(date);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgentService(d.getIdAgent(), date)).thenReturn(new AgentWithServiceDto());
		Mockito.when(sirhWSConsumer.getAgentService(dr.getIdAgent(), date)).thenReturn(new AgentWithServiceDto());

		IDemandeRepository demandeRepo = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepo.getEntity(Demande.class, idDemande)).thenReturn(d);
		Mockito.when(demandeRepo.getEntity(DemandeCongesAnnuels.class, idDemande)).thenReturn(dr);

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
		assertFalse(result.isSamediOffert());
		assertEquals("motif", result.getMotif());
	}

	@Test
	public void getDemande_CongesAnnuel_WithResult_isNoEtatDefinitif() {

		Date dateDebut = new Date();
		Date dateFin = new Date();
		Date dateMaj = new Date();
		Date dateMaj2 = new Date();
		Integer idDemande = 1;

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());

		RefTypeAbsence rta = new RefTypeAbsence();
		rta.setIdRefTypeAbsence(26);
		rta.setGroupe(groupe);

		Demande d = new Demande();
		d.setType(rta);

		DemandeCongesAnnuels dr = new DemandeCongesAnnuels();

		EtatDemandeCongesAnnuels ed = new EtatDemandeCongesAnnuels();
		ed.setDate(dateMaj);
		ed.setDemande((Demande) dr);
		ed.setEtat(RefEtatEnum.SAISIE);
		ed.setIdAgent(9005138);
		ed.setIdEtatDemande(1);
		EtatDemandeCongesAnnuels ed2 = new EtatDemandeCongesAnnuels();
		ed2.setDate(dateMaj2);
		ed2.setDemande((Demande) dr);
		ed2.setEtat(RefEtatEnum.APPROUVEE);
		ed2.setIdAgent(9005138);
		ed2.setIdEtatDemande(2);
		ed2.setMotif("motif");
		ed2.setDuree(10.0);
		ed2.setDateDebut(dateDebut);
		ed2.setNbSamediOffert(1.0);
		List<EtatDemande> listEtatDemande = new ArrayList<EtatDemande>();
		listEtatDemande.addAll(Arrays.asList(ed2, ed));

		dr.setDateDebut(dateDebut);
		dr.setDateFin(dateFin);
		dr.setDuree(10.0);
		dr.setEtatsDemande(listEtatDemande);
		dr.setIdAgent(9005138);
		dr.setIdDemande(idDemande);
		dr.setType(rta);
		dr.setNbSamediOffert(1.0);

		IDemandeRepository demandeRepo = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepo.getEntity(Demande.class, idDemande)).thenReturn(d);
		Mockito.when(demandeRepo.getEntity(DemandeCongesAnnuels.class, idDemande)).thenReturn(dr);

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
		assertTrue(result.isSamediOffert());
	}

	@Test
	public void saveDemandeCongeAnnuel_OK_avecEtatProvisoire() {

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.5);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.CONGE_ANNUEL.getValue());
		dto.setGroupeAbsence(groupeAbsence);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(idAgent);

		RefTypeSaisiCongeAnnuelDto typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuelDto();
		typeSaisiCongeAnnuel.setIdRefTypeSaisiCongeAnnuel(1);

		dto.setAgentWithServiceDto(agDto);
		dto.setIdRefEtat(0);
		dto.setDateDemande(new Date());
		dto.setAffichageBoutonModifier(false);
		dto.setAffichageBoutonSupprimer(false);
		dto.setAffichageBoutonImprimer(false);
		dto.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);

		Droit droitOperateur = new Droit();
		DroitDroitsAgent droitDroitAgent = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(idAgent);
		droitDroitAgent.setDroitsAgent(droitsAgent);
		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.add(droitDroitAgent);
		droitOperateur.setDroitDroitsAgent(droitDroitsAgent);

		RefTypeSaisiCongeAnnuel typeSaisi = new RefTypeSaisiCongeAnnuel();
		typeSaisi.setIdRefTypeSaisiCongeAnnuel(1);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isUserOperateur(idAgent)).thenReturn(true);
		Mockito.when(accessRightsRepository.getAgentDroitFetchAgents(idAgent)).thenReturn(droitOperateur);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(DemandeCongesAnnuels.class, dto.getIdDemande())).thenReturn(new DemandeCongesAnnuels());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeCongesAnnuels obj = (DemandeCongesAnnuels) args[0];

				assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(obj.getDuree(), new Double(0.0));

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeCongesAnnuels.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);
		Mockito.when(helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateDebut);
		Mockito.when(helperService.getDureeCongeAnnuel(new DemandeCongesAnnuels(), dto.getDateReprise(), dto.isForceSaisieManuelleDuree(), dto.getDuree()))
				.thenReturn(2.0);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeCongesAnnuels.class), Mockito.anyBoolean());

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
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(null);
		Mockito.when(accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(ReturnMessageDto.class)))
				.thenReturn(new ReturnMessageDto());

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.getEntity(RefTypeSaisiCongeAnnuel.class, dto.getTypeSaisiCongeAnnuel().getIdRefTypeSaisiCongeAnnuel()))
				.thenReturn(typeSaisi);

		IAlfrescoCMISService alfrescoCMISService = Mockito.mock(IAlfrescoCMISService.class);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "alfrescoCMISService", alfrescoCMISService);

		result = service.saveDemande(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void saveDemandeCongeAnnuel_OK_avecEtatSaisie() {

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.0);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.CONGE_ANNUEL.getValue());
		dto.setGroupeAbsence(groupeAbsence);

		RefTypeSaisiCongeAnnuelDto typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuelDto();
		typeSaisiCongeAnnuel.setIdRefTypeSaisiCongeAnnuel(1);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(idAgent);
		dto.setAgentWithServiceDto(agDto);
		dto.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());
		dto.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);

		Droit droitOperateur = new Droit();
		DroitDroitsAgent droitDroitAgent = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(idAgent);
		droitDroitAgent.setDroitsAgent(droitsAgent);
		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.add(droitDroitAgent);
		droitOperateur.setDroitDroitsAgent(droitDroitsAgent);

		RefTypeSaisiCongeAnnuel typeSaisi = new RefTypeSaisiCongeAnnuel();
		typeSaisi.setIdRefTypeSaisiCongeAnnuel(1);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isUserOperateur(idAgent)).thenReturn(true);
		Mockito.when(accessRightsRepository.getAgentDroitFetchAgents(idAgent)).thenReturn(droitOperateur);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(DemandeCongesAnnuels.class, dto.getIdDemande())).thenReturn(new DemandeCongesAnnuels());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeCongesAnnuels obj = (DemandeCongesAnnuels) args[0];

				assertEquals(RefEtatEnum.SAISIE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(obj.getDuree(), new Double(0.0));

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeCongesAnnuels.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);
		Mockito.when(helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateDebut);
		Mockito.when(helperService.getDureeCongeAnnuel(new DemandeCongesAnnuels(), dto.getDateReprise(), dto.isForceSaisieManuelleDuree(), dto.getDuree()))
				.thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeCongesAnnuels.class), Mockito.anyBoolean());

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
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(ReturnMessageDto.class))).thenReturn(result);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(null);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.getEntity(RefTypeSaisiCongeAnnuel.class, dto.getTypeSaisiCongeAnnuel().getIdRefTypeSaisiCongeAnnuel()))
				.thenReturn(typeSaisi);

		IAlfrescoCMISService alfrescoCMISService = Mockito.mock(IAlfrescoCMISService.class);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "alfrescoCMISService", alfrescoCMISService);

		result = service.saveDemande(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void saveDemandeCongeAnnuel_Ko() {

		ReturnMessageDto result = new ReturnMessageDto();
		result.getErrors().add("erreur droit");

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.0);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.CONGE_ANNUEL.getValue());
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
				DemandeCongesAnnuels obj = (DemandeCongesAnnuels) args[0];

				assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(10, obj.getDuree().intValue());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeCongesAnnuels.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeCongesAnnuels.class), Mockito.anyBoolean());

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
		Mockito.when(accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(ReturnMessageDto.class))).thenReturn(result);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);

		try {
			result = service.saveDemande(idAgent, dto);
		} catch (ReturnMessageDtoException e) {
			result = e.getErreur();
		}

		assertEquals(1, result.getErrors().size());
		assertEquals("erreur droit", result.getErrors().get(0).toString());
		Mockito.verify(demandeRepository, Mockito.times(0)).persistEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void getListeDemandesSIRH_return1Liste_WithCongeAnnuel() {

		List<Demande> listdemande = new ArrayList<Demande>();
		Demande d = new Demande();
		d.setIdDemande(1);
		listdemande.add(d);

		List<DemandeDto> listdemandeDto = new ArrayList<DemandeDto>();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());

		AgentWithServiceDto agentWithServiceDto = new AgentWithServiceDto();
		agentWithServiceDto.setIdAgent(9005138);

		DemandeDto dto = new DemandeDto();
		dto.setAgentWithServiceDto(agentWithServiceDto);
		dto.setIdDemande(1);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.CONGE_ANNUEL.getValue());
		dto.setGroupeAbsence(groupeAbsence);

		listdemandeDto.add(dto);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.listeDemandesSIRH(null, null, null, null, null, null)).thenReturn(listdemande);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.when(absDataConsistencyRules.filtreDateAndEtatDemandeFromList(listdemande, null, null, true)).thenReturn(listdemandeDto);
		Mockito.when(absDataConsistencyRules.filtreDroitOfDemandeSIRH(dto)).thenReturn(dto);
		Mockito.when(absDataConsistencyRules.checkDepassementCompteurAgent(Mockito.any(DemandeDto.class), Mockito.any(CheckCompteurAgentVo.class)))
				.thenReturn(true);
		Mockito.when(absDataConsistencyRules.checkDepassementMultipleAgent(dto)).thenReturn(true);

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		ICongesAnnuelsRepository congeAnnuelRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congeAnnuelRepository.getListRestitutionMassiveByIdAgent(Arrays.asList(9005138), null, null)).thenReturn(null);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "congeAnnuelRepository", congeAnnuelRepository);

		List<DemandeDto> listResult = service.getListeDemandesSIRH(null, null, null, null, null, null, null, null, null);

		assertEquals(1, listResult.size());
		assertTrue(listResult.get(0).isDepassementCompteur());
		assertTrue(listResult.get(0).isDepassementMultiple());
	}

	@Test
	public void getListeDemandes_return1Liste_WithCongeAnnuel() throws Exception {

		List<Demande> listdemande = new ArrayList<Demande>();
		Demande d = new Demande();
		d.setIdDemande(1);
		d.setIdAgent(9005131);
		listdemande.add(d);

		List<DemandeDto> listdemandeDto = new ArrayList<DemandeDto>();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());

		AgentWithServiceDto agentWithServiceDto = new AgentWithServiceDto();

		DemandeDto dto = new DemandeDto();
		dto.setAgentWithServiceDto(agentWithServiceDto);
		dto.setIdDemande(1);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.CONGE_ANNUEL.getValue());
		dto.setGroupeAbsence(groupeAbsence);
		listdemandeDto.add(dto);

		RefEtat etat = new RefEtat();
		etat.setLabel("APPROUVE");
		List<RefEtat> listEtat = new ArrayList<RefEtat>();
		listEtat.add(etat);

		Date dat = new Date();

		List<DroitsAgent> listDroitAgent = new ArrayList<DroitsAgent>();

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.listeIdsDemandesForListAgent(9005138, Arrays.asList(9005131), dat, null, null, null, null, null)).thenReturn(Arrays.asList(1,2));
		Mockito.when(demandeRepository.listeDemandesByListIdsDemande(Arrays.asList(1,2))).thenReturn(listdemande);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.when(absDataConsistencyRules.filtreDateAndEtatDemandeFromList(listdemande, listEtat, null, false)).thenReturn(listdemandeDto);

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);
		Mockito.when(absDataConsistencyRules.filtreDroitOfDemande(9005138, dto, listDroitAgent, false)).thenReturn(dto);
		Mockito.when(absDataConsistencyRules.checkDepassementCompteurAgent(Mockito.any(DemandeDto.class), Mockito.any(CheckCompteurAgentVo.class)))
				.thenReturn(false);
		Mockito.when(absDataConsistencyRules.checkDepassementMultipleAgent(dto)).thenReturn(true);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDateMoinsUnAn()).thenReturn(dat);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(Arrays.asList(9005138));

		IFiltreService filtresService = Mockito.mock(IFiltreService.class);
		Mockito.when(filtresService.getListeEtatsByOnglet("TOUTES", new ArrayList<Integer>())).thenReturn(listEtat);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.getListOfAgentsToInputOrApprove(9005138)).thenReturn(listDroitAgent);

		ICongesAnnuelsRepository congeAnnuelRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congeAnnuelRepository.getListRestitutionMassiveByIdAgent(Arrays.asList(9005138), null, null)).thenReturn(null);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);

		DemandeRecursiveTask multiTask = PowerMockito.mock(DemandeRecursiveTask.class);
		PowerMockito.whenNew(DemandeRecursiveTask.class)
				.withArguments(Mockito.anyMap(), Mockito.anyList(), Mockito.anyInt(), Mockito.anyList(), Mockito.anyBoolean()).thenReturn(multiTask);

		ForkJoinPool pool = PowerMockito.mock(ForkJoinPool.class);
		PowerMockito.whenNew(ForkJoinPool.class).withNoArguments().thenReturn(pool);

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				DemandeDto demande = new DemandeDto();
				demande.setDepassementCompteur(false);
				demande.setDepassementMultiple(true);

				List<DemandeDto> list = new ArrayList<DemandeDto>();
				list.add(demande);

				return list;
			}
		}).when(pool).invoke(multiTask);

		AbsCongesAnnuelsDataConsistencyRulesImpl absCongesAnnuelsDataConsistencyRulesImpl = Mockito.mock(AbsCongesAnnuelsDataConsistencyRulesImpl.class);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "filtresService", filtresService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "congeAnnuelRepository", congeAnnuelRepository);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "absCongesAnnuelsDataConsistencyRulesImpl", absCongesAnnuelsDataConsistencyRulesImpl);

		ResultListDemandeDto listResult = service.getListeDemandes(9005138, Arrays.asList(9005131), "TOUTES", null, null, null, null, null, null, false);

		assertEquals(1, listResult.getListDemandesDto().size());
		assertFalse(listResult.getListDemandesDto().get(0).isDepassementCompteur());
		assertTrue(listResult.getListDemandesDto().get(0).isDepassementMultiple());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setDemandeEtat_CongeAnnuel_setDemandeEtatApprouve_Ko_Avalider() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = null;

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());
		dto.setIdDemande(1);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());

		RefTypeSaisiCongeAnnuel typeSaisi = new RefTypeSaisiCongeAnnuel();

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.CONGE_ANNUEL.getValue());
		type.setGroupe(groupe);
		type.setTypeSaisiCongeAnnuel(typeSaisi);

		DemandeCongesAnnuels demande = Mockito.spy(new DemandeCongesAnnuels());
		demande.setDuree(10.0);
		demande.setType(type);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(demande);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isApprobateurOrDelegataireOfAgent(idAgent, demande.getIdAgent())).thenReturn(true);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		}).when(absDataConsistencyRules).checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(List.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		}).when(absDataConsistencyRules).checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(), Mockito.anyString());

		ICounterService counterService = Mockito.mock(ICounterService.class);

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		}).when(counterService).majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(DemandeEtatChangeDto.class));

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(counterServiceFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande.getType().getIdRefTypeAbsence()))
				.thenReturn(counterService);

		AbsCongesAnnuelsDataConsistencyRulesImpl absCongesAnnuelsDataConsistencyRulesImpl = Mockito.mock(AbsCongesAnnuelsDataConsistencyRulesImpl.class);

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				result.getInfos().add("Le dépassement des droits acquis n'est pas autorisé.");
				return result;
			}
		}).when(absCongesAnnuelsDataConsistencyRulesImpl).checkDepassementDroitsAcquis(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
				Mockito.any(CheckCompteurAgentVo.class));

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue(), null))
				.thenReturn(absCongesAnnuelsDataConsistencyRulesImpl);
		Mockito.when(dataConsistencyRulesFactory.getFactory(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue(), RefTypeAbsenceEnum.CONGE_ANNUEL.getValue()))
				.thenReturn(absCongesAnnuelsDataConsistencyRulesImpl);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "counterServiceFactory", counterServiceFactory);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.setDemandeEtat(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		assertEquals("La demande est en attente de validation par la DRH.", result.getInfos().get(0));
		Mockito.verify(demande, Mockito.times(1)).addEtatDemande(Mockito.isA(EtatDemande.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setDemandeEtat_CongeAnnuel_setDemandeEtatApprouve_ok() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = null;

		ReturnMessageDto srm = new ReturnMessageDto();

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());
		dto.setIdDemande(1);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());

		RefTypeSaisiCongeAnnuel typeSaisi = new RefTypeSaisiCongeAnnuel();

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.CONGE_ANNUEL.getValue());
		type.setGroupe(groupe);
		type.setTypeSaisiCongeAnnuel(typeSaisi);

		DemandeCongesAnnuels demande = Mockito.spy(new DemandeCongesAnnuels());
		demande.setDuree(10.0);
		demande.setType(type);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(demande);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isApprobateurOrDelegataireOfAgent(idAgent, demande.getIdAgent())).thenReturn(true);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		}).when(absDataConsistencyRules).checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(List.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		}).when(absDataConsistencyRules).checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(), Mockito.anyString());

		ICounterService counterService = Mockito.mock(ICounterService.class);
		Mockito.when(
				counterService.majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(DemandeEtatChangeDto.class)))
				.thenReturn(srm);

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(counterServiceFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande.getType().getIdRefTypeAbsence()))
				.thenReturn(counterService);

		AbsCongesAnnuelsDataConsistencyRulesImpl absCongesAnnuelsDataConsistencyRulesImpl = Mockito.mock(AbsCongesAnnuelsDataConsistencyRulesImpl.class);

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		}).when(absCongesAnnuelsDataConsistencyRulesImpl).checkDepassementDroitsAcquis(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
				Mockito.any(CheckCompteurAgentVo.class));

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue(), RefTypeAbsenceEnum.CONGE_ANNUEL.getValue()))
				.thenReturn(absCongesAnnuelsDataConsistencyRulesImpl);
		Mockito.when(dataConsistencyRulesFactory.getFactory(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue(), null))
				.thenReturn(absCongesAnnuelsDataConsistencyRulesImpl);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "counterServiceFactory", counterServiceFactory);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		result = service.setDemandeEtat(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		assertEquals("La demande est approuvée.", result.getInfos().get(0));
		Mockito.verify(demande, Mockito.times(1)).addEtatDemande(Mockito.isA(EtatDemande.class));
	}

	@Test
	public void setDemandeEtatPris_EtatIncorrect_ReturnError_CongeAnnuel() {

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());

		RefTypeAbsence typeRecup = new RefTypeAbsence();
		typeRecup.setLabel("Congé annuel");
		typeRecup.setIdRefTypeAbsence(RefTypeAbsenceEnum.CONGE_ANNUEL.getValue());
		typeRecup.setGroupe(groupe);

		EtatDemande etat1 = new EtatDemande();
		DemandeCongesAnnuels demande1 = new DemandeCongesAnnuels();
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
		assertEquals("La demande 1 n'est pas à l'état VALIDEE ou APPROUVEE mais SAISIE.", result.getErrors().get(0).toString());
		Mockito.verify(demandeRepository, Mockito.never()).removeEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void setDemandeEtatPris_EtatIsOk_AddEtatDemande_CongeAnnuel() {

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());

		RefTypeAbsence typeRecup = new RefTypeAbsence();
		typeRecup.setLabel("Congé annuel");
		typeRecup.setIdRefTypeAbsence(RefTypeAbsenceEnum.CONGE_ANNUEL.getValue());
		typeRecup.setGroupe(groupe);

		EtatDemande etat1 = new EtatDemande();
		final DemandeCongesAnnuels demande1 = new DemandeCongesAnnuels();
		etat1.setDemande(demande1);
		etat1.setEtat(RefEtatEnum.APPROUVEE);
		demande1.getEtatsDemande().add(etat1);
		demande1.setType(typeRecup);
		demande1.setIdAgent(9006565);
		demande1.setDateDebut(new DateTime(2014, 1, 26, 0, 0, 0).toDate());
		demande1.setDateFin(new DateTime(2014, 1, 26, 23, 59, 59).toDate());

		SpcarrId spcarrId = new SpcarrId();
		spcarrId.setNomatr(6565);
		spcarrId.setDatdeb(20140101);
		Spcarr carr = new Spcarr();
		carr.setId(spcarrId);
		carr.setDateFin(0);
		carr.setCdcate(8);

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
		Mockito.when(helper.isContractuel(carr)).thenReturn(false);
		Mockito.when(helper.isConventionCollective(carr)).thenReturn(false);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(carr);

		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(demande1.getIdAgent())).thenReturn(6565);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helper);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeService);

		// When
		ReturnMessageDto result = service.setDemandeEtatPris(1);

		// Then
		assertEquals(0, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(EtatDemande.class));
	}

	@Test
	public void getListeDemandesSIRHAValider_return1Liste_WithCongeAnnuel() {

		List<Demande> listdemande = new ArrayList<Demande>();
		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());
		RefTypeAbsence type = new RefTypeAbsence();
		type.setGroupe(groupe);
		Demande d = new Demande();
		d.setIdDemande(1);
		d.setType(type);
		listdemande.add(d);

		List<DemandeDto> listdemandeDto = new ArrayList<DemandeDto>();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());

		AgentWithServiceDto agentWithServiceDto = new AgentWithServiceDto();
		agentWithServiceDto.setIdAgent(9005138);

		DemandeDto dto = new DemandeDto();
		dto.setAgentWithServiceDto(agentWithServiceDto);
		dto.setIdDemande(1);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.CONGE_ANNUEL.getValue());
		dto.setGroupeAbsence(groupeAbsence);
		dto.setIdRefEtat(10);

		listdemandeDto.add(dto);

		RefEtat refAppro = new RefEtat();
		refAppro.setIdRefEtat(4);
		RefEtat refAttente = new RefEtat();
		refAttente.setIdRefEtat(10);
		List<RefEtat> listEtat = new ArrayList<>();
		listEtat.add(refAttente);
		listEtat.add(refAppro);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.listeDemandesCongesAnnuelsSIRHAValider(null, null, null, 150)).thenReturn(listdemande);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.when(absDataConsistencyRules.filtreDateAndEtatDemandeFromList(listdemande, listEtat, null, true)).thenReturn(listdemandeDto);
		Mockito.when(absDataConsistencyRules.filtreDroitOfDemandeSIRH(dto)).thenReturn(dto);
		Mockito.when(absDataConsistencyRules.checkDepassementCompteurAgent(Mockito.any(DemandeDto.class), Mockito.any(CheckCompteurAgentVo.class)))
				.thenReturn(true);

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefEtatAValider()).thenReturn(listEtat);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);

		List<DemandeDto> listResult = service.getListeDemandesSIRHAValider(null, null, null, null, null, null, null);

		assertEquals(1, listResult.size());
		assertTrue(listResult.get(0).isDepassementCompteur());
		assertFalse(listResult.get(0).isDepassementMultiple());
	}

	@Test
	public void checkRecuperations_returnErrors() {
		Integer idAgent = 9005138;
		Date dateDebut = new DateTime(2014, 01, 01, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 01, 07, 0, 0).toDate();

		List<Demande> listdemande = new ArrayList<Demande>();
		EtatDemande etat = new EtatDemande();
		etat.setEtat(RefEtatEnum.PRISE);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.RECUP.getValue());
		RefTypeAbsence type = new RefTypeAbsence();
		type.setGroupe(groupe);
		DemandeRecup d = new DemandeRecup();
		d.setIdDemande(1);
		d.setType(type);
		d.getEtatsDemande().add(etat);
		listdemande.add(d);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.listeDemandesAgentVerification(idAgent, dateDebut, dateFin, RefTypeGroupeAbsenceEnum.RECUP.getValue()))
				.thenReturn(listdemande);
		Mockito.when(demandeRepository.listeDemandesAgentVerification(idAgent, dateDebut, dateFin, null)).thenReturn(listdemande);
		Mockito.when(demandeRepository.getEntity(DemandeRecup.class, d.getIdDemande())).thenReturn(d);

		AgentGeneriqueDto agent = new AgentGeneriqueDto();
		agent.setPrenomUsage("prenomUsage");
		agent.setNomUsage("nomUsage");
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(idAgent)).thenReturn(agent);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		ReturnMessageDto result = service.checkRecuperations(idAgent, dateDebut, dateFin);

		assertEquals(1, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
		assertEquals("01/01/2014 00:00 : L'agent nomUsage prenomUsage est en récupération sur cette période.", result.getErrors().get(0));

		result = service.checkAbsences(idAgent, dateDebut, dateFin);

		assertEquals(1, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
		assertEquals("01/01/2014 00:00 : L'agent nomUsage prenomUsage est en récupération sur cette période.", result.getErrors().get(0));
	}

	@Test
	public void checkRecuperations_returnOK() {
		Integer idAgent = 9005138;
		Date dateDebut = new DateTime(2014, 01, 01, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 01, 07, 0, 0).toDate();

		List<Demande> listdemande = new ArrayList<Demande>();
		EtatDemande etat = new EtatDemande();
		etat.setEtat(RefEtatEnum.SAISIE);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.RECUP.getValue());
		RefTypeAbsence type = new RefTypeAbsence();
		type.setGroupe(groupe);
		DemandeRecup d = new DemandeRecup();
		d.setDateDebut(new DateTime(2014, 01, 01, 0, 0).toDate());
		d.setIdDemande(1);
		d.setType(type);
		d.getEtatsDemande().add(etat);
		listdemande.add(d);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.listeDemandesAgentVerification(idAgent, dateDebut, dateFin, RefTypeGroupeAbsenceEnum.RECUP.getValue()))
				.thenReturn(listdemande);
		Mockito.when(demandeRepository.listeDemandesAgentVerification(idAgent, dateDebut, dateFin, null)).thenReturn(listdemande);
		Mockito.when(demandeRepository.getEntity(DemandeRecup.class, d.getIdDemande())).thenReturn(d);

		AgentGeneriqueDto agent = new AgentGeneriqueDto();
		agent.setPrenomUsage("prenomUsage");
		agent.setNomUsage("nomUsage");
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(idAgent)).thenReturn(agent);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		ReturnMessageDto result = service.checkRecuperations(idAgent, dateDebut, dateFin);

		assertEquals(0, result.getErrors().size());
		assertEquals(1, result.getInfos().size());
		assertEquals("01/01/2014 : Soyez vigilant, vous avez pointé sur une absence de type 'récupération' pour l'agent nomUsage prenomUsage.",
				result.getInfos().get(0));

		result = service.checkAbsences(idAgent, dateDebut, dateFin);

		assertEquals(0, result.getErrors().size());
		assertEquals(1, result.getInfos().size());
		assertEquals("01/01/2014 : Soyez vigilant, vous avez pointé sur une absence de type 'récupération' pour l'agent nomUsage prenomUsage.",
				result.getInfos().get(0));
	}

	@Test
	public void checkReposCompensateurs_returnErrors() {
		Integer idAgent = 9005138;
		Date dateDebut = new DateTime(2014, 01, 01, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 01, 07, 0, 0).toDate();

		List<Demande> listdemande = new ArrayList<Demande>();
		EtatDemande etat = new EtatDemande();
		etat.setEtat(RefEtatEnum.PRISE);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.REPOS_COMP.getValue());
		RefTypeAbsence type = new RefTypeAbsence();
		type.setGroupe(groupe);
		DemandeReposComp d = new DemandeReposComp();
		d.setDateDebut(new DateTime(2014, 01, 01, 0, 0).toDate());
		d.setIdDemande(1);
		d.setType(type);
		d.getEtatsDemande().add(etat);
		listdemande.add(d);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.listeDemandesAgentVerification(idAgent, dateDebut, dateFin, RefTypeGroupeAbsenceEnum.REPOS_COMP.getValue()))
				.thenReturn(listdemande);
		Mockito.when(demandeRepository.listeDemandesAgentVerification(idAgent, dateDebut, dateFin, null)).thenReturn(listdemande);
		Mockito.when(demandeRepository.getEntity(DemandeReposComp.class, d.getIdDemande())).thenReturn(d);

		AgentGeneriqueDto agent = new AgentGeneriqueDto();
		agent.setPrenomUsage("prenomUsage");
		agent.setNomUsage("nomUsage");
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(idAgent)).thenReturn(agent);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		ReturnMessageDto result = service.checkReposCompensateurs(idAgent, dateDebut, dateFin);

		assertEquals(1, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
		assertEquals("01/01/2014 00:00 : L'agent nomUsage prenomUsage est en repos compensateur sur cette période.", result.getErrors().get(0));

		result = service.checkAbsences(idAgent, dateDebut, dateFin);

		assertEquals(1, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
		assertEquals("01/01/2014 00:00 : L'agent nomUsage prenomUsage est en repos compensateur sur cette période.", result.getErrors().get(0));
	}

	@Test
	public void checkReposCompensateurs_returnOK() {
		Integer idAgent = 9005138;
		Date dateDebut = new DateTime(2014, 01, 01, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 01, 07, 0, 0).toDate();

		List<Demande> listdemande = new ArrayList<Demande>();
		EtatDemande etat = new EtatDemande();
		etat.setEtat(RefEtatEnum.SAISIE);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.REPOS_COMP.getValue());
		RefTypeAbsence type = new RefTypeAbsence();
		type.setGroupe(groupe);
		DemandeReposComp d = new DemandeReposComp();
		d.setDateDebut(new DateTime(2014, 1, 5, 0, 0).toDate());
		d.setIdDemande(1);
		d.setType(type);
		d.getEtatsDemande().add(etat);
		listdemande.add(d);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.listeDemandesAgentVerification(idAgent, dateDebut, dateFin, RefTypeGroupeAbsenceEnum.REPOS_COMP.getValue()))
				.thenReturn(listdemande);
		Mockito.when(demandeRepository.listeDemandesAgentVerification(idAgent, dateDebut, dateFin, null)).thenReturn(listdemande);
		Mockito.when(demandeRepository.getEntity(DemandeReposComp.class, d.getIdDemande())).thenReturn(d);

		AgentGeneriqueDto agent = new AgentGeneriqueDto();
		agent.setPrenomUsage("prenomUsage");
		agent.setNomUsage("nomUsage");
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(idAgent)).thenReturn(agent);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		ReturnMessageDto result = service.checkReposCompensateurs(idAgent, dateDebut, dateFin);

		assertEquals(0, result.getErrors().size());
		assertEquals(1, result.getInfos().size());
		assertEquals("05/01/2014 : Soyez vigilant, vous avez pointé sur une absence de type 'repos compensateur' pour l'agent nomUsage prenomUsage.",
				result.getInfos().get(0));

		result = service.checkAbsences(idAgent, dateDebut, dateFin);

		assertEquals(0, result.getErrors().size());
		assertEquals(1, result.getInfos().size());
		assertEquals("05/01/2014 : Soyez vigilant, vous avez pointé sur une absence de type 'repos compensateur' pour l'agent nomUsage prenomUsage.",
				result.getInfos().get(0));
	}

	@Test
	public void checkAbsencesSyndicales_returnErrors() {
		Integer idAgent = 9005138;
		Date dateDebut = new DateTime(2014, 01, 01, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 01, 07, 0, 0).toDate();

		List<Demande> listdemande = new ArrayList<Demande>();
		EtatDemande etat = new EtatDemande();
		etat.setEtat(RefEtatEnum.PRISE);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());
		RefTypeAbsence type = new RefTypeAbsence();
		type.setGroupe(groupe);
		DemandeAsa d = new DemandeAsa();
		d.setIdDemande(1);
		d.setType(type);
		d.getEtatsDemande().add(etat);
		listdemande.add(d);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.listeDemandesAgentVerification(idAgent, dateDebut, dateFin, RefTypeGroupeAbsenceEnum.AS.getValue()))
				.thenReturn(listdemande);
		Mockito.when(demandeRepository.listeDemandesAgentVerification(idAgent, dateDebut, dateFin, null)).thenReturn(listdemande);
		Mockito.when(demandeRepository.getEntity(DemandeAsa.class, d.getIdDemande())).thenReturn(d);

		AgentGeneriqueDto agent = new AgentGeneriqueDto();
		agent.setPrenomUsage("prenomUsage");
		agent.setNomUsage("nomUsage");
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(idAgent)).thenReturn(agent);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		ReturnMessageDto result = service.checkAbsencesSyndicales(idAgent, dateDebut, dateFin);

		assertEquals(1, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
		assertEquals("01/01/2014 00:00 : L'agent nomUsage prenomUsage est en absence syndicale sur cette période.", result.getErrors().get(0));

		result = service.checkAbsences(idAgent, dateDebut, dateFin);

		assertEquals(1, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
		assertEquals("01/01/2014 00:00 : L'agent nomUsage prenomUsage est en absence syndicale sur cette période.", result.getErrors().get(0));
	}

	@Test
	public void checkAbsencesSyndicales_returnOK() {
		Integer idAgent = 9005138;
		Date dateDebut = new DateTime(2014, 01, 01, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 01, 07, 0, 0).toDate();

		List<Demande> listdemande = new ArrayList<Demande>();
		EtatDemande etat = new EtatDemande();
		etat.setEtat(RefEtatEnum.SAISIE);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());
		RefTypeAbsence type = new RefTypeAbsence();
		type.setGroupe(groupe);
		DemandeAsa d = new DemandeAsa();
		d.setDateDebut(new DateTime(2014, 01, 2, 0, 0).toDate());
		d.setIdDemande(1);
		d.setType(type);
		d.getEtatsDemande().add(etat);
		listdemande.add(d);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.listeDemandesAgentVerification(idAgent, dateDebut, dateFin, RefTypeGroupeAbsenceEnum.AS.getValue()))
				.thenReturn(listdemande);
		Mockito.when(demandeRepository.listeDemandesAgentVerification(idAgent, dateDebut, dateFin, null)).thenReturn(listdemande);
		Mockito.when(demandeRepository.getEntity(DemandeAsa.class, d.getIdDemande())).thenReturn(d);

		AgentGeneriqueDto agent = new AgentGeneriqueDto();
		agent.setPrenomUsage("prenomUsage");
		agent.setNomUsage("nomUsage");
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(idAgent)).thenReturn(agent);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		ReturnMessageDto result = service.checkAbsencesSyndicales(idAgent, dateDebut, dateFin);

		assertEquals(0, result.getErrors().size());
		assertEquals(1, result.getInfos().size());
		assertEquals("02/01/2014 : Soyez vigilant, vous avez pointé sur une absence de type 'absence syndicale' pour l'agent nomUsage prenomUsage.",
				result.getInfos().get(0));

		result = service.checkAbsences(idAgent, dateDebut, dateFin);

		assertEquals(0, result.getErrors().size());
		assertEquals(1, result.getInfos().size());
		assertEquals("02/01/2014 : Soyez vigilant, vous avez pointé sur une absence de type 'absence syndicale' pour l'agent nomUsage prenomUsage.",
				result.getInfos().get(0));
	}

	@Test
	public void checkCongesExceptionnels_returnErrors() {
		Integer idAgent = 9005138;
		Date dateDebut = new DateTime(2014, 01, 01, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 01, 07, 0, 0).toDate();

		List<Demande> listdemande = new ArrayList<Demande>();
		EtatDemande etat = new EtatDemande();
		etat.setEtat(RefEtatEnum.PRISE);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_EXCEP.getValue());
		RefTypeAbsence type = new RefTypeAbsence();
		type.setGroupe(groupe);
		DemandeCongesExceptionnels d = new DemandeCongesExceptionnels();
		d.setIdDemande(1);
		d.setType(type);
		d.getEtatsDemande().add(etat);
		listdemande.add(d);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.listeDemandesAgentVerification(idAgent, dateDebut, dateFin, RefTypeGroupeAbsenceEnum.CONGES_EXCEP.getValue()))
				.thenReturn(listdemande);
		Mockito.when(demandeRepository.listeDemandesAgentVerification(idAgent, dateDebut, dateFin, null)).thenReturn(listdemande);
		Mockito.when(demandeRepository.getEntity(DemandeCongesExceptionnels.class, d.getIdDemande())).thenReturn(d);

		AgentGeneriqueDto agent = new AgentGeneriqueDto();
		agent.setPrenomUsage("prenomUsage");
		agent.setNomUsage("nomUsage");
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(idAgent)).thenReturn(agent);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		ReturnMessageDto result = service.checkCongesExceptionnels(idAgent, dateDebut, dateFin);

		assertEquals(1, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
		assertEquals("01/01/2014 00:00 : L'agent nomUsage prenomUsage est en congé exceptionnel sur cette période.", result.getErrors().get(0));

		result = service.checkAbsences(idAgent, dateDebut, dateFin);

		assertEquals(1, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
		assertEquals("01/01/2014 00:00 : L'agent nomUsage prenomUsage est en congé exceptionnel sur cette période.", result.getErrors().get(0));
	}

	@Test
	public void checkCongesExceptionnels_returnOK() {
		Integer idAgent = 9005138;
		Date dateDebut = new DateTime(2014, 01, 01, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 01, 07, 0, 0).toDate();

		List<Demande> listdemande = new ArrayList<Demande>();
		EtatDemande etat = new EtatDemande();
		etat.setEtat(RefEtatEnum.SAISIE);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_EXCEP.getValue());
		RefTypeAbsence type = new RefTypeAbsence();
		type.setGroupe(groupe);
		DemandeCongesExceptionnels d = new DemandeCongesExceptionnels();
		d.setDateDebut(new DateTime(2014, 1, 4, 0, 0).toDate());
		d.setIdDemande(1);
		d.setType(type);
		d.getEtatsDemande().add(etat);
		listdemande.add(d);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.listeDemandesAgentVerification(idAgent, dateDebut, dateFin, RefTypeGroupeAbsenceEnum.CONGES_EXCEP.getValue()))
				.thenReturn(listdemande);
		Mockito.when(demandeRepository.listeDemandesAgentVerification(idAgent, dateDebut, dateFin, null)).thenReturn(listdemande);
		Mockito.when(demandeRepository.getEntity(DemandeCongesExceptionnels.class, d.getIdDemande())).thenReturn(d);

		AgentGeneriqueDto agent = new AgentGeneriqueDto();
		agent.setPrenomUsage("prenomUsage");
		agent.setNomUsage("nomUsage");
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(idAgent)).thenReturn(agent);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		ReturnMessageDto result = service.checkCongesExceptionnels(idAgent, dateDebut, dateFin);

		assertEquals(0, result.getErrors().size());
		assertEquals(1, result.getInfos().size());
		assertEquals("04/01/2014 : Soyez vigilant, vous avez pointé sur une absence de type 'congé exceptionnel' pour l'agent nomUsage prenomUsage.",
				result.getInfos().get(0));

		result = service.checkAbsences(idAgent, dateDebut, dateFin);

		assertEquals(0, result.getErrors().size());
		assertEquals(1, result.getInfos().size());
		assertEquals("04/01/2014 : Soyez vigilant, vous avez pointé sur une absence de type 'congé exceptionnel' pour l'agent nomUsage prenomUsage.",
				result.getInfos().get(0));
	}

	@Test
	public void checkCongesAnnuels_returnErrors() {
		Integer idAgent = 9005138;
		Date dateDebut = new DateTime(2014, 01, 01, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 01, 07, 0, 0).toDate();

		List<Demande> listdemande = new ArrayList<Demande>();
		EtatDemande etat = new EtatDemande();
		etat.setEtat(RefEtatEnum.PRISE);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());
		RefTypeAbsence type = new RefTypeAbsence();
		type.setGroupe(groupe);
		DemandeCongesAnnuels d = new DemandeCongesAnnuels();
		d.setIdDemande(1);
		d.setType(type);
		d.getEtatsDemande().add(etat);
		listdemande.add(d);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.listeDemandesAgentVerification(idAgent, dateDebut, dateFin, RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue()))
				.thenReturn(listdemande);
		Mockito.when(demandeRepository.listeDemandesAgentVerification(idAgent, dateDebut, dateFin, null)).thenReturn(listdemande);
		Mockito.when(demandeRepository.getEntity(DemandeCongesAnnuels.class, d.getIdDemande())).thenReturn(d);

		AgentGeneriqueDto agent = new AgentGeneriqueDto();
		agent.setPrenomUsage("prenomUsage");
		agent.setNomUsage("nomUsage");
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(idAgent)).thenReturn(agent);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		ReturnMessageDto result = service.checkCongesAnnuels(idAgent, dateDebut, dateFin);

		assertEquals(1, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
		assertEquals("01/01/2014 00:00 : L'agent nomUsage prenomUsage est en congé annuel sur cette période.", result.getErrors().get(0));

		result = service.checkAbsences(idAgent, dateDebut, dateFin);

		assertEquals(1, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
		assertEquals("01/01/2014 00:00 : L'agent nomUsage prenomUsage est en congé annuel sur cette période.", result.getErrors().get(0));
	}

	@Test
	public void checkCongesAnnuels_returnOK() {
		Integer idAgent = 9005138;
		Date dateDebut = new DateTime(2014, 01, 01, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 01, 07, 0, 0).toDate();

		List<Demande> listdemande = new ArrayList<Demande>();
		EtatDemande etat = new EtatDemande();
		etat.setEtat(RefEtatEnum.SAISIE);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());
		RefTypeAbsence type = new RefTypeAbsence();
		type.setGroupe(groupe);
		DemandeCongesAnnuels d = new DemandeCongesAnnuels();
		d.setDateDebut(new DateTime(2014, 1, 3, 0, 0).toDate());
		d.setIdDemande(1);
		d.setType(type);
		d.getEtatsDemande().add(etat);
		listdemande.add(d);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.listeDemandesAgentVerification(idAgent, dateDebut, dateFin, RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue()))
				.thenReturn(listdemande);
		Mockito.when(demandeRepository.listeDemandesAgentVerification(idAgent, dateDebut, dateFin, null)).thenReturn(listdemande);
		Mockito.when(demandeRepository.getEntity(DemandeCongesAnnuels.class, d.getIdDemande())).thenReturn(d);

		AgentGeneriqueDto agent = new AgentGeneriqueDto();
		agent.setPrenomUsage("prenomUsage");
		agent.setNomUsage("nomUsage");
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(idAgent)).thenReturn(agent);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		ReturnMessageDto result = service.checkCongesAnnuels(idAgent, dateDebut, dateFin);

		assertEquals(0, result.getErrors().size());
		assertEquals(1, result.getInfos().size());
		assertEquals("03/01/2014 : Soyez vigilant, vous avez pointé sur une absence de type 'congé annuel' pour l'agent nomUsage prenomUsage.",
				result.getInfos().get(0));

		result = service.checkAbsences(idAgent, dateDebut, dateFin);

		assertEquals(0, result.getErrors().size());
		assertEquals(1, result.getInfos().size());
		assertEquals("03/01/2014 : Soyez vigilant, vous avez pointé sur une absence de type 'congé annuel' pour l'agent nomUsage prenomUsage.",
				result.getInfos().get(0));
	}

	@Test
	public void checkMaladies_returnErrors() {
		Integer idAgent = 9005138;
		Date dateDebut = new DateTime(2014, 01, 01, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 01, 07, 0, 0).toDate();

		List<Demande> listdemande = new ArrayList<Demande>();
		EtatDemande etat = new EtatDemande();
		etat.setEtat(RefEtatEnum.PRISE);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.MALADIES.getValue());
		RefTypeAbsence type = new RefTypeAbsence();
		type.setGroupe(groupe);
		DemandeMaladies d = new DemandeMaladies();
		d.setIdDemande(1);
		d.setType(type);
		d.getEtatsDemande().add(etat);
		d.setDateDebut(new DateTime(2014, 1, 3, 0, 0, 0).toDate());
		listdemande.add(d);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.listeDemandesAgentVerification(idAgent, dateDebut, dateFin, RefTypeGroupeAbsenceEnum.MALADIES.getValue()))
				.thenReturn(listdemande);
		Mockito.when(demandeRepository.listeDemandesAgentVerification(idAgent, dateDebut, dateFin, null)).thenReturn(listdemande);
		Mockito.when(demandeRepository.getEntity(DemandeMaladies.class, d.getIdDemande())).thenReturn(d);

		AgentGeneriqueDto agent = new AgentGeneriqueDto();
		agent.setPrenomUsage("prenomUsage");
		agent.setNomUsage("nomUsage");
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(idAgent)).thenReturn(agent);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		ReturnMessageDto result = service.checkMaladies(idAgent, dateDebut, dateFin);

		assertEquals(1, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
		assertEquals("03/01/2014 00:00 : L'agent nomUsage prenomUsage est en maladie sur cette période.", result.getErrors().get(0));

		result = service.checkAbsences(idAgent, dateDebut, dateFin);

		assertEquals(1, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
		assertEquals("03/01/2014 00:00 : L'agent nomUsage prenomUsage est en maladie sur cette période.", result.getErrors().get(0));
	}

	@Test
	public void checkMaladies_returnOK() {
		Integer idAgent = 9005138;
		Date dateDebut = new DateTime(2014, 01, 01, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 01, 07, 0, 0).toDate();

		List<Demande> listdemande = new ArrayList<Demande>();
		EtatDemande etat = new EtatDemande();
		etat.setEtat(RefEtatEnum.SAISIE);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.MALADIES.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setGroupe(groupe);

		DemandeMaladies d = new DemandeMaladies();
		d.setDateDebut(new DateTime(2014, 1, 3, 0, 0).toDate());
		d.setIdDemande(1);
		d.setType(type);
		d.getEtatsDemande().add(etat);
		listdemande.add(d);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.listeDemandesAgentVerification(idAgent, dateDebut, dateFin, RefTypeGroupeAbsenceEnum.MALADIES.getValue()))
				.thenReturn(listdemande);
		Mockito.when(demandeRepository.listeDemandesAgentVerification(idAgent, dateDebut, dateFin, null)).thenReturn(listdemande);
		Mockito.when(demandeRepository.getEntity(DemandeMaladies.class, d.getIdDemande())).thenReturn(d);

		AgentGeneriqueDto agent = new AgentGeneriqueDto();
		agent.setPrenomUsage("prenomUsage");
		agent.setNomUsage("nomUsage");
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(idAgent)).thenReturn(agent);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		ReturnMessageDto result = service.checkMaladies(idAgent, dateDebut, dateFin);

		assertEquals(1, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
		assertEquals("03/01/2014 00:00 : L'agent nomUsage prenomUsage est en maladie sur cette période.", result.getErrors().get(0));

		result = service.checkAbsences(idAgent, dateDebut, dateFin);

		assertEquals(1, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
		assertEquals("03/01/2014 00:00 : L'agent nomUsage prenomUsage est en maladie sur cette période.", result.getErrors().get(0));
	}

	@Test
	public void setDemandeEtatPris_EtatIsOk_MAJ_Paie_CongeAnnuel() {

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());

		RefTypeAbsence typeRecup = new RefTypeAbsence();
		typeRecup.setLabel("Congé annuel");
		typeRecup.setIdRefTypeAbsence(RefTypeAbsenceEnum.CONGE_ANNUEL.getValue());
		typeRecup.setGroupe(groupe);

		EtatDemande etat1 = new EtatDemande();
		final DemandeCongesAnnuels demande1 = new DemandeCongesAnnuels();
		etat1.setDemande(demande1);
		etat1.setEtat(RefEtatEnum.APPROUVEE);
		demande1.getEtatsDemande().add(etat1);
		demande1.setType(typeRecup);
		demande1.setIdAgent(9006565);
		demande1.setDateDebut(new DateTime(2014, 1, 26, 12, 0, 0).toDate());
		demande1.setDateFin(new DateTime(2014, 1, 27, 23, 59, 59).toDate());

		SpcarrId spcarrId = new SpcarrId();
		spcarrId.setNomatr(6565);
		spcarrId.setDatdeb(20140101);
		Spcarr carr = new Spcarr();
		carr.setId(spcarrId);
		carr.setDateFin(0);
		carr.setCdcate(4);

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
		Mockito.when(helper.isContractuel(carr)).thenReturn(true);
		Mockito.when(helper.isConventionCollective(carr)).thenReturn(false);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(carr);

		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(demande1.getIdAgent())).thenReturn(6565);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helper);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeService);

		// When
		ReturnMessageDto result = service.setDemandeEtatPris(1);

		// Then
		assertEquals(0, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(EtatDemande.class));
		Mockito.verify(sirhRepository, Mockito.times(2)).persistEntity(Mockito.isA(Spcc.class));
	}

	@Test
	public void traiteIncidencePaie_NoSpcarr() {
		ReturnMessageDto result = new ReturnMessageDto();

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setGroupe(groupe);

		Demande demande = new Demande();
		demande.setDateDebut(new Date());
		demande.setDateFin(new Date());
		demande.setIdAgent(9005138);
		demande.setType(type);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(null);

		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(demande.getIdAgent())).thenReturn(5138);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeService);

		// When
		result = service.traiteIncidencePaie(demande, result);

		// Then
		assertEquals(1, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
		Mockito.verify(sirhRepository, Mockito.times(0)).persistEntity(Mockito.isA(Spcc.class));
		Mockito.verify(sirhRepository, Mockito.times(0)).persistEntity(Mockito.isA(Spmatr.class));
	}

	@Test
	public void traiteIncidencePaie_Fonctionnaire() {
		ReturnMessageDto result = new ReturnMessageDto();

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setGroupe(groupe);

		Demande demande = new Demande();
		demande.setDateDebut(new Date());
		demande.setDateFin(new Date());
		demande.setIdAgent(9005138);
		demande.setType(type);

		SpcarrId id = new SpcarrId(5138, 20140101);
		Spcarr carr = new Spcarr();
		carr.setId(id);
		carr.setDateFin(0);
		carr.setCdcate(6);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(carr);

		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(demande.getIdAgent())).thenReturn(5138);

		HelperService helper = Mockito.mock(HelperService.class);
		Mockito.when(helper.isContractuel(carr)).thenReturn(false);
		Mockito.when(helper.isConventionCollective(carr)).thenReturn(false);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeService);
		ReflectionTestUtils.setField(service, "helperService", helper);

		// When
		result = service.traiteIncidencePaie(demande, result);

		// Then
		assertEquals(0, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
		Mockito.verify(sirhRepository, Mockito.times(0)).persistEntity(Mockito.isA(Spcc.class));
		Mockito.verify(sirhRepository, Mockito.times(0)).persistEntity(Mockito.isA(Spmatr.class));
	}

	@Test
	public void traiteIncidencePaie_Contractuel_1Jour() {
		ReturnMessageDto result = new ReturnMessageDto();

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setGroupe(groupe);

		Demande demande = new Demande();
		demande.setDateDebut(new DateTime(2014, 2, 2, 0, 0, 0).toDate());
		demande.setDateFin(new DateTime(2014, 2, 2, 23, 59, 59).toDate());
		demande.setIdAgent(9005138);
		demande.setType(type);

		SpcarrId id = new SpcarrId(5138, 20140101);
		Spcarr carr = new Spcarr();
		carr.setId(id);
		carr.setDateFin(0);
		carr.setCdcate(4);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(carr);

		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(demande.getIdAgent())).thenReturn(5138);

		HelperService helper = Mockito.mock(HelperService.class);
		Mockito.when(helper.isContractuel(carr)).thenReturn(true);
		Mockito.when(helper.isConventionCollective(carr)).thenReturn(false);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeService);
		ReflectionTestUtils.setField(service, "helperService", helper);

		// When
		result = service.traiteIncidencePaie(demande, result);

		// Then
		assertEquals(0, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
		Mockito.verify(sirhRepository, Mockito.times(1)).persistEntity(Mockito.isA(Spcc.class));
		Mockito.verify(sirhRepository, Mockito.times(1)).persistEntity(Mockito.isA(Spmatr.class));
	}

	@Test
	public void traiteIncidencePaie_Contractuel_1DemiJour() {
		ReturnMessageDto result = new ReturnMessageDto();

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setGroupe(groupe);

		Demande demande = new Demande();
		demande.setDateDebut(new DateTime(2014, 2, 2, 0, 0, 0).toDate());
		demande.setDateFin(new DateTime(2014, 2, 2, 11, 59, 59).toDate());
		demande.setIdAgent(9005138);
		demande.setType(type);

		SpcarrId id = new SpcarrId(5138, 20140101);
		Spcarr carr = new Spcarr();
		carr.setId(id);
		carr.setDateFin(0);
		carr.setCdcate(4);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(carr);

		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(demande.getIdAgent())).thenReturn(5138);

		HelperService helper = Mockito.mock(HelperService.class);
		Mockito.when(helper.isContractuel(carr)).thenReturn(true);
		Mockito.when(helper.isConventionCollective(carr)).thenReturn(false);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeService);
		ReflectionTestUtils.setField(service, "helperService", helper);

		// When
		result = service.traiteIncidencePaie(demande, result);

		// Then
		assertEquals(0, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
		Mockito.verify(sirhRepository, Mockito.times(1)).persistEntity(Mockito.isA(Spcc.class));
		Mockito.verify(sirhRepository, Mockito.times(1)).persistEntity(Mockito.isA(Spmatr.class));
	}

	@Test
	public void traiteIncidencePaie_Contractuel_SpccDejaExistant_Journee_majCode() {
		ReturnMessageDto result = new ReturnMessageDto();

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setGroupe(groupe);

		Demande demande = new Demande();
		demande.setDateDebut(new DateTime(2014, 2, 2, 0, 0, 0).toDate());
		demande.setDateFin(new DateTime(2014, 2, 2, 23, 59, 59).toDate());
		demande.setIdAgent(9005138);
		demande.setType(type);

		SpcarrId id = new SpcarrId(5138, 20140101);
		Spcarr carr = new Spcarr();
		carr.setId(id);
		carr.setDateFin(0);
		carr.setCdcate(4);

		SpccId spccId = new SpccId();
		spccId.setDatjou(20140202);
		spccId.setNomatr(5138);
		Spcc spcc = Mockito.spy(new Spcc());
		spcc.setCode(1);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(carr);
		Mockito.when(sirhRepository.getSpcc(5138, demande.getDateDebut())).thenReturn(spcc);

		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(demande.getIdAgent())).thenReturn(5138);

		HelperService helper = Mockito.mock(HelperService.class);
		Mockito.when(helper.isContractuel(carr)).thenReturn(true);
		Mockito.when(helper.isConventionCollective(carr)).thenReturn(false);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeService);
		ReflectionTestUtils.setField(service, "helperService", helper);

		// When
		result = service.traiteIncidencePaie(demande, result);

		// Then
		assertEquals(0, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
		Mockito.verify(sirhRepository, Mockito.times(1)).persistEntity(Mockito.isA(Spcc.class));
		Mockito.verify(sirhRepository, Mockito.times(1)).persistEntity(Mockito.isA(Spmatr.class));
		assertEquals(1, spcc.getCode().intValue());
	}

	@Test
	public void traiteIncidencePaie_Contractuel_SpccDejaExistant_demiJournee() {
		ReturnMessageDto result = new ReturnMessageDto();

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setGroupe(groupe);

		Demande demande = new Demande();
		demande.setDateDebut(new DateTime(2014, 2, 2, 0, 0, 0).toDate());
		demande.setDateFin(new DateTime(2014, 2, 2, 11, 59, 59).toDate());
		demande.setIdAgent(9005138);
		demande.setType(type);

		SpcarrId id = new SpcarrId(5138, 20140101);
		Spcarr carr = new Spcarr();
		carr.setId(id);
		carr.setDateFin(0);
		carr.setCdcate(4);

		SpccId spccId = new SpccId();
		spccId.setDatjou(20140202);
		spccId.setNomatr(5138);
		Spcc spcc = Mockito.spy(new Spcc());
		spcc.setCode(1);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(carr);
		Mockito.when(sirhRepository.getSpcc(5138, demande.getDateDebut())).thenReturn(spcc);

		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(demande.getIdAgent())).thenReturn(5138);

		HelperService helper = Mockito.mock(HelperService.class);
		Mockito.when(helper.isContractuel(carr)).thenReturn(true);
		Mockito.when(helper.isConventionCollective(carr)).thenReturn(false);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeService);
		ReflectionTestUtils.setField(service, "helperService", helper);

		// When
		result = service.traiteIncidencePaie(demande, result);

		// Then
		assertEquals(0, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
		Mockito.verify(sirhRepository, Mockito.times(1)).persistEntity(Mockito.isA(Spcc.class));
		Mockito.verify(sirhRepository, Mockito.times(1)).persistEntity(Mockito.isA(Spmatr.class));
		assertEquals(2, spcc.getCode().intValue());
	}

	@Test
	public void traiteIncidencePaie_Contractuel_1JourEtDemiJour() {
		ReturnMessageDto result = new ReturnMessageDto();

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setGroupe(groupe);

		Demande demande = new Demande();
		demande.setDateDebut(new DateTime(2014, 2, 2, 0, 0, 0).toDate());
		demande.setDateFin(new DateTime(2014, 2, 3, 11, 59, 59).toDate());
		demande.setIdAgent(9005138);
		demande.setType(type);

		SpcarrId id = new SpcarrId(5138, 20140101);
		Spcarr carr = new Spcarr();
		carr.setId(id);
		carr.setDateFin(0);
		carr.setCdcate(4);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(carr);

		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(demande.getIdAgent())).thenReturn(5138);

		HelperService helper = Mockito.mock(HelperService.class);
		Mockito.when(helper.isContractuel(carr)).thenReturn(true);
		Mockito.when(helper.isConventionCollective(carr)).thenReturn(false);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeService);
		ReflectionTestUtils.setField(service, "helperService", helper);

		// When
		result = service.traiteIncidencePaie(demande, result);

		// Then
		assertEquals(0, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
		Mockito.verify(sirhRepository, Mockito.times(2)).persistEntity(Mockito.isA(Spcc.class));
		Mockito.verify(sirhRepository, Mockito.times(2)).persistEntity(Mockito.isA(Spmatr.class));
	}

	@Test
	public void traiteIncidencePaie_Contractuel_PlusieursJour() {
		ReturnMessageDto result = new ReturnMessageDto();

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setGroupe(groupe);

		Demande demande = new Demande();
		demande.setDateDebut(new DateTime(2014, 2, 2, 12, 0, 0).toDate());
		demande.setDateFin(new DateTime(2014, 2, 4, 11, 59, 59).toDate());
		demande.setIdAgent(9005138);
		demande.setType(type);

		SpcarrId id = new SpcarrId(5138, 20140101);
		Spcarr carr = new Spcarr();
		carr.setId(id);
		carr.setDateFin(0);
		carr.setCdcate(4);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(carr);

		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(demande.getIdAgent())).thenReturn(5138);

		HelperService helper = Mockito.mock(HelperService.class);
		Mockito.when(helper.isContractuel(carr)).thenReturn(true);
		Mockito.when(helper.isConventionCollective(carr)).thenReturn(false);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeService);
		ReflectionTestUtils.setField(service, "helperService", helper);

		// When
		result = service.traiteIncidencePaie(demande, result);

		// Then
		assertEquals(0, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
		Mockito.verify(sirhRepository, Mockito.times(3)).persistEntity(Mockito.isA(Spcc.class));
		Mockito.verify(sirhRepository, Mockito.times(3)).persistEntity(Mockito.isA(Spmatr.class));
	}

	// bug #29188
	@Test
	public void traiteIncidencePaie_Contractuel_UnMois_bug29188() {
		ReturnMessageDto result = new ReturnMessageDto();

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setGroupe(groupe);

		Demande demande = new Demande();
		demande.setDateDebut(new DateTime(2015, 12, 28, 0, 0, 0).toDate());
		demande.setDateFin(new DateTime(2016, 1, 28, 23, 59, 59).toDate());
		demande.setIdAgent(9004004);
		demande.setType(type);

		SpcarrId id = new SpcarrId(5138, 20140101);
		Spcarr carr = new Spcarr();
		carr.setId(id);
		carr.setDateFin(0);
		carr.setCdcate(4);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(carr);

		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(demande.getIdAgent())).thenReturn(5138);

		HelperService helper = Mockito.mock(HelperService.class);
		Mockito.when(helper.isContractuel(carr)).thenReturn(true);
		Mockito.when(helper.isConventionCollective(carr)).thenReturn(false);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeService);
		ReflectionTestUtils.setField(service, "helperService", helper);

		// When
		result = service.traiteIncidencePaie(demande, result);

		// Then
		assertEquals(0, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
		Mockito.verify(sirhRepository, Mockito.times(32)).persistEntity(Mockito.isA(Spcc.class));
		Mockito.verify(sirhRepository, Mockito.times(32)).persistEntity(Mockito.isA(Spmatr.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setDemandeEtat_setDemandeEtatAnnule_checkEtatDemande_CongeAnnuel() {

		Integer idAgent = 9005138;
		ReturnMessageDto result = new ReturnMessageDto();

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
		dto.setIdDemande(1);
		dto.setMotif(null);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.CONGE_ANNUEL.getValue());
		type.setGroupe(groupe);

		Demande demande = Mockito.spy(new Demande());
		demande.setIdAgent(idAgent);
		demande.setType(type);
		demande.setDateDebut(new DateTime(2014, 1, 26, 15, 16, 35).toDate());

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(demande);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isApprobateurOrDelegataireOfAgent(idAgent, demande.getIdAgent())).thenReturn(true);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				result.getErrors().add("Erreur etat incorrect");
				return result;
			}
		}).when(absDataConsistencyRules).checkEtatsDemandeAnnulee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(List.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		}).when(absDataConsistencyRules).checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(), Mockito.anyString());

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(ReturnMessageDto.class)))
				.thenReturn(new ReturnMessageDto());

		ReturnMessageDto resultPaieEnCours = new ReturnMessageDto();
		resultPaieEnCours.getErrors().add("Une paie est en cours");

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isPaieEnCours()).thenReturn(resultPaieEnCours);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		result = service.setDemandeEtat(idAgent, dto);

		assertEquals(1, result.getErrors().size());
		assertEquals("Vous ne pouvez annuler cette demande car un calcul de salaire est en cours. Merci de réessayer ultérieurement.",
				result.getErrors().get(0).toString());
		Mockito.verify(demande, Mockito.times(0)).addEtatDemande(Mockito.isA(EtatDemande.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setDemandeEtat_setDemandeEtatAnnule_RC_ok_PasIncidencePaie() {

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
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.REPOS_COMP.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.REPOS_COMP.getValue());
		type.setGroupe(groupe);

		DemandeReposComp demande = Mockito.spy(new DemandeReposComp());
		demande.setDuree(10);
		demande.setIdAgent(idAgent);
		demande.setEtatsDemande(listEtat);
		demande.setType(type);
		demande.setDateDebut(new Date());
		demande.setDateFin(new Date());

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(demande);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isApprobateurOrDelegataireOfAgent(idAgent, demande.getIdAgent())).thenReturn(true);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		}).when(absDataConsistencyRules).checkEtatsDemandeAnnulee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(List.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		}).when(absDataConsistencyRules).checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(), Mockito.anyString());

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		ICounterService counterService = Mockito.mock(ICounterService.class);
		Mockito.when(
				counterService.majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(DemandeEtatChangeDto.class)))
				.thenReturn(srm);

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(counterServiceFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande.getType().getIdRefTypeAbsence()))
				.thenReturn(counterService);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(ReturnMessageDto.class)))
				.thenReturn(new ReturnMessageDto());

		ReturnMessageDto resultPaieEnCours = new ReturnMessageDto();

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isPaieEnCours()).thenReturn(resultPaieEnCours);

		Spcarr carr = new Spcarr(5138, 20140101);
		carr.setDateFin(0);
		carr.setCdcate(8);

		HelperService helper = Mockito.mock(HelperService.class);
		Mockito.when(helper.isContractuel(carr)).thenReturn(true);
		Mockito.when(helper.isConventionCollective(carr)).thenReturn(false);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(carr);

		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(demande.getIdAgent())).thenReturn(5138);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "counterServiceFactory", counterServiceFactory);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeService);
		ReflectionTestUtils.setField(service, "helperService", helper);

		result = service.setDemandeEtat(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		assertEquals("La demande est annulée.", result.getInfos().get(0));
		Mockito.verify(demande, Mockito.times(1)).addEtatDemande(Mockito.isA(EtatDemande.class));
		Mockito.verify(sirhRepository, Mockito.times(0)).persistEntity(Mockito.isA(Spcc.class));
		Mockito.verify(sirhRepository, Mockito.times(0)).persistEntity(Mockito.isA(Spmatr.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setDemandeEtat_setDemandeEtatAnnule_CA_AucunSPCC() {

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
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.CONGE_ANNUEL.getValue());
		type.setGroupe(groupe);

		DemandeCongesAnnuels demande = Mockito.spy(new DemandeCongesAnnuels());
		demande.setDuree(10.0);
		demande.setIdAgent(idAgent);
		demande.setEtatsDemande(listEtat);
		demande.setType(type);
		demande.setDateDebut(new Date());
		demande.setDateFin(new Date());

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(demande);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isApprobateurOrDelegataireOfAgent(idAgent, demande.getIdAgent())).thenReturn(true);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		}).when(absDataConsistencyRules).checkEtatsDemandeAnnulee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(List.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		}).when(absDataConsistencyRules).checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(), Mockito.anyString());

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		ICounterService counterService = Mockito.mock(ICounterService.class);
		Mockito.when(
				counterService.majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(DemandeEtatChangeDto.class)))
				.thenReturn(srm);

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(counterServiceFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande.getType().getIdRefTypeAbsence()))
				.thenReturn(counterService);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(ReturnMessageDto.class)))
				.thenReturn(new ReturnMessageDto());

		ReturnMessageDto resultPaieEnCours = new ReturnMessageDto();

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isPaieEnCours()).thenReturn(resultPaieEnCours);

		Spcarr carr = new Spcarr(5138, 20140101);
		carr.setDateFin(0);
		carr.setCdcate(8);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(carr);

		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(demande.getIdAgent())).thenReturn(5138);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "counterServiceFactory", counterServiceFactory);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeService);

		result = service.setDemandeEtat(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		assertEquals("La demande est annulée.", result.getInfos().get(0));
		Mockito.verify(demande, Mockito.times(1)).addEtatDemande(Mockito.isA(EtatDemande.class));
		Mockito.verify(sirhRepository, Mockito.times(0)).removeEntity(Mockito.isA(Spcc.class));
		Mockito.verify(sirhRepository, Mockito.times(0)).persistEntity(Mockito.isA(Spcc.class));
		Mockito.verify(sirhRepository, Mockito.times(0)).persistEntity(Mockito.isA(Spmatr.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setDemandeEtat_setDemandeEtatAnnule_CA_1deleteSPCC() {

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
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.CONGE_ANNUEL.getValue());
		type.setGroupe(groupe);

		DemandeCongesAnnuels demande = Mockito.spy(new DemandeCongesAnnuels());
		demande.setDuree(10.0);
		demande.setIdAgent(idAgent);
		demande.setEtatsDemande(listEtat);
		demande.setType(type);
		demande.setDateDebut(new Date());
		demande.setDateFin(new Date());

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(demande);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isApprobateurOrDelegataireOfAgent(idAgent, demande.getIdAgent())).thenReturn(true);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		}).when(absDataConsistencyRules).checkEtatsDemandeAnnulee(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(List.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return new ReturnMessageDto();
			}
		}).when(absDataConsistencyRules).checkChampMotifPourEtatDonne(Mockito.isA(ReturnMessageDto.class), Mockito.anyInt(), Mockito.anyString());

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		ICounterService counterService = Mockito.mock(ICounterService.class);
		Mockito.when(
				counterService.majCompteurToAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class), Mockito.isA(DemandeEtatChangeDto.class)))
				.thenReturn(srm);

		CounterServiceFactory counterServiceFactory = Mockito.mock(CounterServiceFactory.class);
		Mockito.when(counterServiceFactory.getFactory(demande.getType().getGroupe().getIdRefGroupeAbsence(), demande.getType().getIdRefTypeAbsence()))
				.thenReturn(counterService);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(ReturnMessageDto.class)))
				.thenReturn(new ReturnMessageDto());

		ReturnMessageDto resultPaieEnCours = new ReturnMessageDto();

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isPaieEnCours()).thenReturn(resultPaieEnCours);

		Spcarr carr = new Spcarr(5138, 20140101);
		carr.setDateFin(0);
		carr.setCdcate(8);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(carr);
		Mockito.when(sirhRepository.getSpcc(Mockito.anyInt(), Mockito.any(Date.class), Mockito.anyInt())).thenReturn(new Spcc());

		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(demande.getIdAgent())).thenReturn(5138);

		HelperService helperService = Mockito.mock(HelperService.class);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "counterServiceFactory", counterServiceFactory);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeService);
		ReflectionTestUtils.setField(service, "helperService", helperService);

		result = service.setDemandeEtat(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		assertEquals("La demande est annulée.", result.getInfos().get(0));
		Mockito.verify(demande, Mockito.times(1)).addEtatDemande(Mockito.isA(EtatDemande.class));
		Mockito.verify(sirhRepository, Mockito.times(1)).removeEntity(Mockito.isA(Spcc.class));
		Mockito.verify(sirhRepository, Mockito.times(0)).persistEntity(Mockito.isA(Spcc.class));
		Mockito.verify(sirhRepository, Mockito.times(1)).persistEntity(Mockito.isA(Spmatr.class));
	}

	@Test
	public void getListeMoisAlimAutoCongeAnnuel_ZeroMois() {

		ICongesAnnuelsRepository congeAnnuelRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congeAnnuelRepository.getListeMoisAlimAutoCongeAnnuel()).thenReturn(new ArrayList<Date>());

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "congeAnnuelRepository", congeAnnuelRepository);

		List<MoisAlimAutoCongesAnnuelsDto> result = service.getListeMoisAlimAutoCongeAnnuel();

		assertEquals(0, result.size());
	}

	@Test
	public void getListeMoisAlimAutoCongeAnnuel_PlusieursMois() {
		List<Date> list = new ArrayList<Date>();
		DateTime dateMonth = new DateTime(2014, 12, 1, 0, 0, 0);
		DateTime dateMonth2 = new DateTime(2014, 11, 1, 0, 0, 0);
		list.add(dateMonth.toDate());
		list.add(dateMonth2.toDate());

		ICongesAnnuelsRepository congeAnnuelRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congeAnnuelRepository.getListeMoisAlimAutoCongeAnnuel()).thenReturn(list);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "congeAnnuelRepository", congeAnnuelRepository);

		List<MoisAlimAutoCongesAnnuelsDto> result = service.getListeMoisAlimAutoCongeAnnuel();

		assertEquals(2, result.size());
		assertEquals(dateMonth.toDate(), result.get(0).getDateMois());
		assertEquals(dateMonth2.toDate(), result.get(1).getDateMois());
	}

	@Test
	public void getListeAlimAutoCongeAnnuel_ZeroMois() {
		DateTime dateMonth = new DateTime(2014, 12, 1, 0, 0, 0);

		ICongesAnnuelsRepository congeAnnuelRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congeAnnuelRepository.getListeAlimAutoCongeAnnuelByMois(dateMonth.toDate(), false)).thenReturn(new ArrayList<CongeAnnuelAlimAutoHisto>());

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "congeAnnuelRepository", congeAnnuelRepository);

		List<MoisAlimAutoCongesAnnuelsDto> result = service.getListeAlimAutoCongeAnnuelByMois(dateMonth.toDate(), false);

		assertEquals(0, result.size());
	}

	@Test
	public void getListeAlimAutoCongeAnnuel_PlusieursMois() {
		List<CongeAnnuelAlimAutoHisto> list = new ArrayList<CongeAnnuelAlimAutoHisto>();
		DateTime dateMonth = new DateTime(2014, 12, 1, 0, 0, 0);

		CongeAnnuelAlimAutoHisto c2 = new CongeAnnuelAlimAutoHisto();
		c2.setIdAgent(9005138);
		c2.setDateMonth(dateMonth.toDate());
		CongeAnnuelAlimAutoHisto c = new CongeAnnuelAlimAutoHisto();
		c.setIdAgent(9005156);
		c.setDateMonth(dateMonth.toDate());
		list.add(c);
		list.add(c2);

		AgentGeneriqueDto ag2 = new AgentGeneriqueDto();
		ag2.setIdAgent(c2.getIdAgent());
		AgentGeneriqueDto ag = new AgentGeneriqueDto();
		ag.setIdAgent(c.getIdAgent());

		ICongesAnnuelsRepository congeAnnuelRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congeAnnuelRepository.getListeAlimAutoCongeAnnuelByMois(dateMonth.toDate(), false)).thenReturn(list);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(9005138)).thenReturn(ag);
		Mockito.when(sirhWSConsumer.getAgent(9005156)).thenReturn(ag2);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "congeAnnuelRepository", congeAnnuelRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		List<MoisAlimAutoCongesAnnuelsDto> result = service.getListeAlimAutoCongeAnnuelByMois(dateMonth.toDate(), false);

		assertEquals(2, result.size());
		assertEquals(dateMonth.toDate(), result.get(0).getDateMois());
		assertEquals(c2.getIdAgent(), result.get(0).getAgent().getIdAgent());
		assertEquals(dateMonth.toDate(), result.get(1).getDateMois());
		assertEquals(c.getIdAgent(), result.get(1).getAgent().getIdAgent());
	}

	@Test
	public void getListeIdAgentConcerneRestitutionMassive_zeroAgent() {
		DateTime dateMonth = new DateTime(2014, 12, 1, 0, 0, 0);
		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setDateRestitution(dateMonth.toDate());

		ICongesAnnuelsRepository congeAnnuelRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congeAnnuelRepository.getListeDemandesCongesAnnuelsPrisesForDate(dto.getDateRestitution())).thenReturn(new ArrayList<Integer>());

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "congeAnnuelRepository", congeAnnuelRepository);

		List<Integer> result = service.getListeIdAgentConcerneRestitutionMassive(dto);

		assertEquals(0, result.size());
	}

	@Test
	public void getListeIdAgentConcerneRestitutionMassive_1Agent_ErrorWithBaseC() {
		DateTime dateMonth = new DateTime(2014, 12, 1, 0, 0, 0);
		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setDateRestitution(dateMonth.toDate());

		List<Integer> listIdAgents = new ArrayList<Integer>();
		listIdAgents.add(9005138);
		listIdAgents.add(9005131);
		listIdAgents.add(9003041);

		RefTypeSaisiCongeAnnuelDto dto5138 = new RefTypeSaisiCongeAnnuelDto();
		dto5138.setIdRefTypeSaisiCongeAnnuel(1);
		RefTypeSaisiCongeAnnuel ref5138 = new RefTypeSaisiCongeAnnuel();
		ref5138.setCodeBaseHoraireAbsence("A");
		RefTypeSaisiCongeAnnuelDto dto3041 = new RefTypeSaisiCongeAnnuelDto();
		dto3041.setIdRefTypeSaisiCongeAnnuel(2);
		RefTypeSaisiCongeAnnuel ref3041 = new RefTypeSaisiCongeAnnuel();
		ref3041.setCodeBaseHoraireAbsence("C");

		ICongesAnnuelsRepository congeAnnuelRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congeAnnuelRepository.getListeDemandesCongesAnnuelsPrisesForDate(dto.getDateRestitution())).thenReturn(listIdAgents);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getBaseHoraireAbsence(9005138, dto.getDateRestitution())).thenReturn(dto5138);
		Mockito.when(sirhWSConsumer.getBaseHoraireAbsence(9005131, dto.getDateRestitution())).thenReturn(null);
		Mockito.when(sirhWSConsumer.getBaseHoraireAbsence(9003041, dto.getDateRestitution())).thenReturn(dto3041);

		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(typeAbsenceRepository.getEntity(RefTypeSaisiCongeAnnuel.class, dto5138.getIdRefTypeSaisiCongeAnnuel())).thenReturn(ref5138);
		Mockito.when(typeAbsenceRepository.getEntity(RefTypeSaisiCongeAnnuel.class, dto3041.getIdRefTypeSaisiCongeAnnuel())).thenReturn(ref3041);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "congeAnnuelRepository", congeAnnuelRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);

		List<Integer> result = service.getListeIdAgentConcerneRestitutionMassive(dto);

		assertEquals(1, result.size());
		assertEquals(new Integer(9005138), result.get(0));
	}

	@Test
	public void miseAJourSpsold_ErrorNoSolde() {

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, 9005138)).thenReturn(null);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		ReturnMessageDto result = service.miseAJourSpsold(9005138);

		assertEquals(1, result.getInfos().size());
		assertEquals(0, result.getErrors().size());
		assertEquals("Le compteur de congé annuel n'existe pas.", result.getInfos().get(0));
	}

	@Test
	public void miseAJourSpsold_OK_with1SamediOffert() {
		Integer idAgent = 9005138;
		AgentCongeAnnuelCount count = new AgentCongeAnnuelCount();
		count.setIdAgent(idAgent);
		count.setTotalJours(0.0);
		count.setTotalJoursAnneeN1(12.0);

		SpSold soldeConge = Mockito.spy(new SpSold());
		soldeConge.setNomatr(5138);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, idAgent)).thenReturn(count);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getSpsold(idAgent)).thenReturn(soldeConge);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getNombreSamediOffertSurAnnee(idAgent, new DateTime(new Date()).getYear(), null)).thenReturn(0);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		ReturnMessageDto result = service.miseAJourSpsold(idAgent);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(sirhRepository, Mockito.times(1)).persistEntity(Mockito.isA(SpSold.class));
		assertEquals(1, soldeConge.getSoldeSamediOffert().intValue());
	}

	@Test
	public void miseAJourSpsold_OK_with0SamediOffert() {
		Integer idAgent = 9005138;
		AgentCongeAnnuelCount count = new AgentCongeAnnuelCount();
		count.setIdAgent(idAgent);
		count.setTotalJours(0.0);
		count.setTotalJoursAnneeN1(12.0);

		SpSold soldeConge = Mockito.spy(new SpSold());
		soldeConge.setNomatr(5138);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, idAgent)).thenReturn(count);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getSpsold(idAgent)).thenReturn(soldeConge);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getNombreSamediOffertSurAnnee(idAgent, new DateTime(new Date()).getYear(), null)).thenReturn(1);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		ReturnMessageDto result = service.miseAJourSpsold(idAgent);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(sirhRepository, Mockito.times(1)).persistEntity(Mockito.isA(SpSold.class));
		assertEquals(0, soldeConge.getSoldeSamediOffert().intValue());
	}

	@Test
	public void miseAJourSpsorc_ErrorFonctionnaire() {
		Date dateJ = new Date();

		Spcarr carr = new Spcarr();
		carr.setCdcate(12);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(dateJ);

		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(9005138)).thenReturn(5138);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(5138, dateJ)).thenReturn(carr);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeService);
		ReflectionTestUtils.setField(service, "helperService", helperService);

		ReturnMessageDto result = service.miseAJourSpsorc(9005138);

		assertEquals(0, result.getErrors().size());
		assertEquals(1, result.getInfos().size());
		assertEquals(
				"L'agent [9005138] ne peut pas avoir de repos compensateur. Les repos compensateurs sont pour les contractuels ou les conventions collectives.",
				result.getInfos().get(0));
	}

	@Test
	public void miseAJourSpsorc_ErrorNoSold() {
		Date dateJ = new Date();

		Spcarr carr = new Spcarr();
		carr.setCdcate(4);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(dateJ);

		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(9005138)).thenReturn(5138);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(5138, dateJ)).thenReturn(carr);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentReposCompCount.class, 9005138)).thenReturn(null);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeService);
		ReflectionTestUtils.setField(service, "helperService", helperService);

		ReturnMessageDto result = service.miseAJourSpsorc(9005138);

		assertEquals(1, result.getInfos().size());
		assertEquals(0, result.getErrors().size());
		assertEquals("Le compteur de repos compensateur n'existe pas.", result.getInfos().get(0));
		Mockito.verify(sirhRepository, Mockito.times(0)).persistEntity(Mockito.isA(SpSorc.class));
	}

	@Test
	public void miseAJourSpsorc_OK_NewSporc() {
		Date dateJ = new Date();

		Spcarr carr = new Spcarr();
		carr.setCdcate(4);

		AgentReposCompCount count = new AgentReposCompCount();
		count.setIdAgent(9005138);
		count.setTotalMinutes(0);
		count.setTotalMinutesAnneeN1(12);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(dateJ);

		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(9005138)).thenReturn(5138);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(5138, dateJ)).thenReturn(carr);
		Mockito.when(sirhRepository.getSpsorc(9005138)).thenReturn(null);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentReposCompCount.class, 9005138)).thenReturn(count);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeService);
		ReflectionTestUtils.setField(service, "helperService", helperService);

		ReturnMessageDto result = service.miseAJourSpsorc(9005138);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(sirhRepository, Mockito.times(1)).persistEntity(Mockito.isA(SpSorc.class));
	}

	@Test
	public void miseAJourSpsorc_OK_OldSporc() {
		Date dateJ = new Date();

		Spcarr carr = new Spcarr();
		carr.setCdcate(4);

		AgentReposCompCount count = new AgentReposCompCount();
		count.setIdAgent(9005138);
		count.setTotalMinutes(0);
		count.setTotalMinutesAnneeN1(12);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(dateJ);

		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(9005138)).thenReturn(5138);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(5138, dateJ)).thenReturn(carr);
		Mockito.when(sirhRepository.getSpsorc(9005138)).thenReturn(new SpSorc());

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentReposCompCount.class, 9005138)).thenReturn(count);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeService);
		ReflectionTestUtils.setField(service, "helperService", helperService);

		ReturnMessageDto result = service.miseAJourSpsorc(9005138);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(sirhRepository, Mockito.times(1)).persistEntity(Mockito.isA(SpSorc.class));
	}

	@Test
	public void getHistoAlimAutoCongeAnnuel_ZeroMois() {
		Integer idAgent = 9005138;

		ICongesAnnuelsRepository congeAnnuelRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congeAnnuelRepository.getListeAlimAutoCongeAnnuelByAgent(idAgent)).thenReturn(new ArrayList<CongeAnnuelAlimAutoHisto>());

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "congeAnnuelRepository", congeAnnuelRepository);

		List<MoisAlimAutoCongesAnnuelsDto> result = service.getHistoAlimAutoCongeAnnuel(idAgent);

		assertEquals(0, result.size());
	}

	@Test
	public void getHistoAlimAutoCongeAnnuel_PlusieursMois() {
		List<CongeAnnuelAlimAutoHisto> list = new ArrayList<CongeAnnuelAlimAutoHisto>();
		DateTime dateMonth = new DateTime(2014, 12, 1, 0, 0, 0);
		DateTime dateMonth2 = new DateTime(2014, 11, 1, 0, 0, 0);
		Integer idAgent = 9005138;

		CongeAnnuelAlimAutoHisto c2 = new CongeAnnuelAlimAutoHisto();
		c2.setIdAgent(idAgent);
		c2.setDateMonth(dateMonth.toDate());
		CongeAnnuelAlimAutoHisto c = new CongeAnnuelAlimAutoHisto();
		c.setIdAgent(idAgent);
		c.setDateMonth(dateMonth2.toDate());
		list.add(c2);
		list.add(c);

		AgentGeneriqueDto ag = new AgentGeneriqueDto();
		ag.setIdAgent(c.getIdAgent());

		AgentWeekCongeAnnuel w1 = new AgentWeekCongeAnnuel();
		w1.setJours(2.5);

		ICongesAnnuelsRepository congeAnnuelRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congeAnnuelRepository.getListeAlimAutoCongeAnnuelByAgent(idAgent)).thenReturn(list);
		Mockito.when(congeAnnuelRepository.getWeekHistoForAgentAndDate(idAgent, dateMonth.toDate())).thenReturn(w1);
		Mockito.when(congeAnnuelRepository.getWeekHistoForAgentAndDate(idAgent, dateMonth2.toDate())).thenReturn(null);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(9005138)).thenReturn(ag);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "congeAnnuelRepository", congeAnnuelRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		List<MoisAlimAutoCongesAnnuelsDto> result = service.getHistoAlimAutoCongeAnnuel(idAgent);

		assertEquals(2, result.size());
		assertEquals(dateMonth.toDate(), result.get(0).getDateMois());
		assertEquals(new Double(2.5), result.get(0).getNbJours());
		assertEquals(dateMonth2.toDate(), result.get(1).getDateMois());
		assertEquals(new Double(0.0), result.get(1).getNbJours());
	}

	@Test
	public void createRefAlimCongeAnnuelAnnee_Error_NoRefAlim() {
		ICongesAnnuelsRepository congeAnnuelRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congeAnnuelRepository.getListeRefAlimCongeAnnuelByYear(2014)).thenReturn(null);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "congeAnnuelRepository", congeAnnuelRepository);

		ReturnMessageDto result = service.createRefAlimCongeAnnuelAnnee(2015);

		assertEquals(1, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
		assertEquals("Aucun paramétrage trouvé pour l'année précédente. Merci de contacter le responsable du projet.", result.getErrors().get(0));
	}

	@Test
	public void createRefAlimCongeAnnuelAnnee_Error_NoSameCount() {
		RefAlimCongeAnnuel r1 = new RefAlimCongeAnnuel();

		List<RefAlimCongeAnnuel> listeRef = new ArrayList<RefAlimCongeAnnuel>();
		listeRef.add(r1);

		ICongesAnnuelsRepository congeAnnuelRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congeAnnuelRepository.getListeRefAlimCongeAnnuelByYear(2014)).thenReturn(listeRef);

		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(typeAbsenceRepository.getListeTypeSaisiCongeAnnuel()).thenReturn(new ArrayList<RefTypeSaisiCongeAnnuel>());

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "congeAnnuelRepository", congeAnnuelRepository);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);

		ReturnMessageDto result = service.createRefAlimCongeAnnuelAnnee(2015);

		assertEquals(1, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
		assertEquals("Aucun paramétrage trouvé pour l'année précédente. Merci de contacter le responsable du projet.", result.getErrors().get(0));
	}

	@Test
	public void createRefAlimCongeAnnuelAnnee_OK() {
		RefAlimCongeAnnuelId id = new RefAlimCongeAnnuelId();
		id.setIdRefTypeSaisiCongeAnnuel(1);
		id.setAnnee(2014);
		RefAlimCongeAnnuel r1 = new RefAlimCongeAnnuel();
		r1.setJanvier(1.0);
		r1.setId(id);

		List<RefAlimCongeAnnuel> listeRef = new ArrayList<RefAlimCongeAnnuel>();
		listeRef.add(r1);

		RefTypeSaisiCongeAnnuel t1 = new RefTypeSaisiCongeAnnuel();

		List<RefTypeSaisiCongeAnnuel> listeTypeAbs = new ArrayList<RefTypeSaisiCongeAnnuel>();
		listeTypeAbs.add(t1);

		ICongesAnnuelsRepository congeAnnuelRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congeAnnuelRepository.getListeRefAlimCongeAnnuelByYear(2014)).thenReturn(listeRef);

		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(typeAbsenceRepository.getListeTypeSaisiCongeAnnuel()).thenReturn(listeTypeAbs);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				RefAlimCongeAnnuel obj = (RefAlimCongeAnnuel) args[0];

				assertEquals(2015, obj.getId().getAnnee().intValue());
				assertEquals(new Double(1), obj.getJanvier());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(RefAlimCongeAnnuel.class));

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "congeAnnuelRepository", congeAnnuelRepository);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		ReturnMessageDto result = service.createRefAlimCongeAnnuelAnnee(2015);

		assertEquals(0, result.getErrors().size());
		assertEquals(1, result.getInfos().size());
		assertEquals("Alimentation des congés annuels sauvegardée.", result.getInfos().get(0));
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(RefAlimCongeAnnuel.class));
	}

	@Test
	public void getHistoAlimAutoRecup_ZeroResult() {
		Integer idAgent = 9005138;

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);

		IRecuperationRepository recuperationRepository = Mockito.mock(IRecuperationRepository.class);
		Mockito.when(recuperationRepository.getListeAlimAutoRecupByAgent(idAgent)).thenReturn(new ArrayList<AgentWeekRecup>());

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "recuperationRepository", recuperationRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		List<MoisAlimAutoCongesAnnuelsDto> result = service.getHistoAlimAutoRecup(idAgent);

		assertEquals(0, result.size());
	}

	@Test
	public void getHistoAlimAutoRecup_PlusieursResult() {
		Integer idAgent = 9005138;

		List<AgentWeekRecup> list = new ArrayList<AgentWeekRecup>();

		AgentWeekRecup c2 = new AgentWeekRecup();
		c2.setIdAgent(idAgent);
		c2.setDateDay(new Date());
		list.add(c2);

		AgentGeneriqueDto ag = new AgentGeneriqueDto();
		ag.setIdAgent(idAgent);

		IRecuperationRepository recuperationRepository = Mockito.mock(IRecuperationRepository.class);
		Mockito.when(recuperationRepository.getListeAlimAutoRecupByAgent(idAgent)).thenReturn(list);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(idAgent)).thenReturn(ag);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "recuperationRepository", recuperationRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		List<MoisAlimAutoCongesAnnuelsDto> result = service.getHistoAlimAutoRecup(idAgent);

		assertEquals(1, result.size());
		assertEquals(c2.getIdAgent(), result.get(0).getAgent().getIdAgent());
	}

	@Test
	public void getHistoAlimAutoReposComp_ZeroResult() {
		Integer idAgent = 9005138;

		IReposCompensateurRepository reposCompensateurRepository = Mockito.mock(IReposCompensateurRepository.class);
		Mockito.when(reposCompensateurRepository.getListeAlimAutoReposCompByAgent(idAgent)).thenReturn(new ArrayList<AgentWeekReposComp>());

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "reposCompensateurRepository", reposCompensateurRepository);

		List<MoisAlimAutoCongesAnnuelsDto> result = service.getHistoAlimAutoReposComp(idAgent);

		assertEquals(0, result.size());
	}

	@Test
	public void getHistoAlimAutoReposComp_PlusieursResult() {
		Integer idAgent = 9005138;

		List<AgentWeekReposComp> list = new ArrayList<AgentWeekReposComp>();

		AgentWeekReposComp c2 = new AgentWeekReposComp();
		c2.setIdAgent(idAgent);
		list.add(c2);

		AgentGeneriqueDto ag = new AgentGeneriqueDto();
		ag.setIdAgent(idAgent);

		IReposCompensateurRepository reposCompensateurRepository = Mockito.mock(IReposCompensateurRepository.class);
		Mockito.when(reposCompensateurRepository.getListeAlimAutoReposCompByAgent(idAgent)).thenReturn(list);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(idAgent)).thenReturn(ag);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "reposCompensateurRepository", reposCompensateurRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		List<MoisAlimAutoCongesAnnuelsDto> result = service.getHistoAlimAutoReposComp(idAgent);

		assertEquals(1, result.size());
		assertEquals(c2.getIdAgent(), result.get(0).getAgent().getIdAgent());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getListeDemandesSIRH_return1Liste_WithCongeAnnuelAndRestitutionMassive() {

		List<Demande> listdemande = new ArrayList<Demande>();
		Demande d = new Demande();
		d.setIdDemande(1);
		listdemande.add(d);

		List<DemandeDto> listdemandeDto = new ArrayList<DemandeDto>();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());

		AgentWithServiceDto agentWithServiceDto = new AgentWithServiceDto();
		agentWithServiceDto.setIdAgent(9005138);

		DemandeDto dto = new DemandeDto();
		dto.setAgentWithServiceDto(agentWithServiceDto);
		dto.setIdDemande(1);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.CONGE_ANNUEL.getValue());
		dto.setGroupeAbsence(groupeAbsence);
		dto.setDateDebut(new DateTime(2015, 6, 1, 0, 0, 0).toDate());

		listdemandeDto.add(dto);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.listeDemandesSIRH(null, null, null, null, null, null)).thenReturn(listdemande);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.when(absDataConsistencyRules.filtreDateAndEtatDemandeFromList(listdemande, null, null, true)).thenReturn(listdemandeDto);
		Mockito.when(absDataConsistencyRules.filtreDroitOfDemandeSIRH(dto)).thenReturn(dto);
		Mockito.when(absDataConsistencyRules.checkDepassementCompteurAgent(Mockito.any(DemandeDto.class), Mockito.any(CheckCompteurAgentVo.class)))
				.thenReturn(true);
		Mockito.when(absDataConsistencyRules.checkDepassementMultipleAgent(dto)).thenReturn(true);

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		CongeAnnuelRestitutionMassive restitutionMassive = new CongeAnnuelRestitutionMassive();
		restitutionMassive.setDateRestitution(new DateTime(2015, 6, 5, 0, 0, 0).toDate());

		CongeAnnuelRestitutionMassiveHisto restitutionMassiveHisto = new CongeAnnuelRestitutionMassiveHisto();
		restitutionMassiveHisto.setIdAgent(9002990);
		restitutionMassiveHisto.setRestitutionMassive(restitutionMassive);

		List<CongeAnnuelRestitutionMassiveHisto> listRestitutionMassive = new ArrayList<CongeAnnuelRestitutionMassiveHisto>();
		listRestitutionMassive.add(restitutionMassiveHisto);

		ICongesAnnuelsRepository congeAnnuelRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congeAnnuelRepository.getListRestitutionMassiveByIdAgent(null, null, null)).thenReturn(listRestitutionMassive);

		IAgentService agentService = Mockito.mock(IAgentService.class);
		Mockito.when(agentService.getAgentOptimise(Mockito.anyList(), Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(new AgentWithServiceDto());

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "congeAnnuelRepository", congeAnnuelRepository);
		ReflectionTestUtils.setField(service, "agentService", agentService);

		List<DemandeDto> listResult = service.getListeDemandesSIRH(null, null, null, null, null, null, null, null, null);

		assertEquals(2, listResult.size());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getListRestitutionMassiveByIdAgent_return1Demande() {

		CongeAnnuelRestitutionMassive restitutionMassive = new CongeAnnuelRestitutionMassive();
		restitutionMassive.setDateRestitution(new DateTime(2015, 6, 5, 0, 0, 0).toDate());

		CongeAnnuelRestitutionMassiveHisto restitutionMassiveHisto = new CongeAnnuelRestitutionMassiveHisto();
		restitutionMassiveHisto.setIdAgent(9002990);
		restitutionMassiveHisto.setRestitutionMassive(restitutionMassive);

		List<CongeAnnuelRestitutionMassiveHisto> listRestitutionMassive = new ArrayList<CongeAnnuelRestitutionMassiveHisto>();
		listRestitutionMassive.add(restitutionMassiveHisto);

		ICongesAnnuelsRepository congeAnnuelRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congeAnnuelRepository.getListRestitutionMassiveByIdAgent(Arrays.asList(9005138), null, null)).thenReturn(listRestitutionMassive);

		IAgentService agentService = Mockito.mock(IAgentService.class);
		Mockito.when(agentService.getAgentOptimise(Mockito.anyList(), Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(new AgentWithServiceDto());

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "agentService", agentService);
		ReflectionTestUtils.setField(service, "congeAnnuelRepository", congeAnnuelRepository);

		List<DemandeDto> listResult = service.getListRestitutionMassiveByIdAgent(Arrays.asList(9005138), null, null, null, null);

		assertEquals(1, listResult.size());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getListRestitutionMassiveByIdAgent_testEtat() {

		CongeAnnuelRestitutionMassive restitutionMassive = new CongeAnnuelRestitutionMassive();
		restitutionMassive.setDateRestitution(new DateTime(2015, 6, 5, 0, 0, 0).toDate());

		CongeAnnuelRestitutionMassiveHisto restitutionMassiveHisto = new CongeAnnuelRestitutionMassiveHisto();
		restitutionMassiveHisto.setIdAgent(9002990);
		restitutionMassiveHisto.setRestitutionMassive(restitutionMassive);

		List<CongeAnnuelRestitutionMassiveHisto> listRestitutionMassive = new ArrayList<CongeAnnuelRestitutionMassiveHisto>();
		listRestitutionMassive.add(restitutionMassiveHisto);

		ICongesAnnuelsRepository congeAnnuelRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congeAnnuelRepository.getListRestitutionMassiveByIdAgent(Arrays.asList(9005138), null, null)).thenReturn(listRestitutionMassive);

		IAgentService agentService = Mockito.mock(IAgentService.class);
		Mockito.when(agentService.getAgentOptimise(Mockito.anyList(), Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(new AgentWithServiceDto());

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "agentService", agentService);
		ReflectionTestUtils.setField(service, "congeAnnuelRepository", congeAnnuelRepository);

		for (int i = 0; i < 12; i++) {
			List<DemandeDto> listResult = service.getListRestitutionMassiveByIdAgent(Arrays.asList(9005138), null, null, null, Arrays.asList(i));

			if (i == RefEtatEnum.APPROUVEE.getCodeEtat() || i == RefEtatEnum.VALIDEE.getCodeEtat() || i == RefEtatEnum.PRISE.getCodeEtat()) {
				assertEquals(1, listResult.size());
			} else {
				assertEquals(0, listResult.size());
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getListRestitutionMassiveByIdAgent_testTypeAbsence() {

		CongeAnnuelRestitutionMassive restitutionMassive = new CongeAnnuelRestitutionMassive();
		restitutionMassive.setDateRestitution(new DateTime(2015, 6, 5, 0, 0, 0).toDate());

		CongeAnnuelRestitutionMassiveHisto restitutionMassiveHisto = new CongeAnnuelRestitutionMassiveHisto();
		restitutionMassiveHisto.setIdAgent(9002990);
		restitutionMassiveHisto.setRestitutionMassive(restitutionMassive);

		List<CongeAnnuelRestitutionMassiveHisto> listRestitutionMassive = new ArrayList<CongeAnnuelRestitutionMassiveHisto>();
		listRestitutionMassive.add(restitutionMassiveHisto);

		ICongesAnnuelsRepository congeAnnuelRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congeAnnuelRepository.getListRestitutionMassiveByIdAgent(Arrays.asList(9005138), null, null)).thenReturn(listRestitutionMassive);

		IAgentService agentService = Mockito.mock(IAgentService.class);
		Mockito.when(agentService.getAgentOptimise(Mockito.anyList(), Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(new AgentWithServiceDto());

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "agentService", agentService);
		ReflectionTestUtils.setField(service, "congeAnnuelRepository", congeAnnuelRepository);

		for (int i = 1; i < 6; i++) {
			List<DemandeDto> listResult = service.getListRestitutionMassiveByIdAgent(Arrays.asList(9005138), null, null, i, Arrays.asList(4));

			if (i == RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue()) {
				assertEquals(1, listResult.size());
			} else {
				assertEquals(0, listResult.size());
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getListRestitutionMassiveByIdAgent_pasRestitutionEnBDD() {

		CongeAnnuelRestitutionMassive restitutionMassive = new CongeAnnuelRestitutionMassive();
		restitutionMassive.setDateRestitution(new DateTime(2015, 6, 5, 0, 0, 0).toDate());

		CongeAnnuelRestitutionMassiveHisto restitutionMassiveHisto = new CongeAnnuelRestitutionMassiveHisto();
		restitutionMassiveHisto.setIdAgent(9002990);
		restitutionMassiveHisto.setRestitutionMassive(restitutionMassive);

		List<CongeAnnuelRestitutionMassiveHisto> listRestitutionMassive = new ArrayList<CongeAnnuelRestitutionMassiveHisto>();
		listRestitutionMassive.add(restitutionMassiveHisto);

		ICongesAnnuelsRepository congeAnnuelRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congeAnnuelRepository.getListRestitutionMassiveByIdAgent(Arrays.asList(9005138), null, null)).thenReturn(null);

		IAgentService agentService = Mockito.mock(IAgentService.class);
		Mockito.when(agentService.getAgentOptimise(Mockito.anyList(), Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(new AgentWithServiceDto());

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "agentService", agentService);
		ReflectionTestUtils.setField(service, "congeAnnuelRepository", congeAnnuelRepository);

		List<DemandeDto> listResult = service.getListRestitutionMassiveByIdAgent(Arrays.asList(9005138), null, null, 5, Arrays.asList(4));

		assertEquals(0, listResult.size());
	}

	@Test
	public void getListDemandesCAToAddOrRemoveOnAgentCounter_forbidden() {

		Integer idAgent = 9005138;
		Integer idAgentConcerne = 9005131;

		ReturnMessageDto rmd = new ReturnMessageDto();
		rmd.getErrors().add("forbidden");

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(rmd);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		try {
			service.getListDemandesCAToAddOrRemoveOnAgentCounter(idAgent, idAgentConcerne);
		} catch (AccessForbiddenException e) {
			assertNotNull(e);
			return;
		}

		fail();
	}

	@Test
	public void getListDemandesCAToAddOrRemoveOnAgentCounter() {

		Integer idAgent = 9005138;
		Integer idAgentConcerne = 9005131;

		EtatDemandeCongesAnnuels etatCA1 = new EtatDemandeCongesAnnuels();
		etatCA1.setDemande(new Demande());
		etatCA1.setDate(new DateTime(2015, 7, 1, 0, 0, 0).toDate());
		etatCA1.setDateDebut(new DateTime(2015, 7, 2, 0, 0, 0).toDate());
		etatCA1.setDateFin(new DateTime(2015, 7, 4, 0, 0, 0).toDate());
		etatCA1.setEtat(RefEtatEnum.APPROUVEE);
		etatCA1.setNbSamediOffert(1.0);
		etatCA1.setDuree(1.0);
		etatCA1.setDureeAnneeN1(2.0);
		etatCA1.setTotalJoursAnneeN1Old(2.0);
		etatCA1.setTotalJoursOld(2.0);
		etatCA1.setTotalJoursAnneeN1New(0.0);
		etatCA1.setTotalJoursNew(1.0);

		EtatDemandeCongesAnnuels etatCA2 = new EtatDemandeCongesAnnuels();
		etatCA2.setDemande(new Demande());
		etatCA2.setDate(new DateTime(2015, 7, 1, 0, 0, 0).toDate());
		etatCA2.setDateDebut(new DateTime(2015, 7, 2, 0, 0, 0).toDate());
		etatCA2.setDateFin(new DateTime(2015, 7, 4, 0, 0, 0).toDate());
		etatCA2.setEtat(RefEtatEnum.VALIDEE);
		etatCA2.setNbSamediOffert(0.0);
		etatCA2.setDuree(3.0);
		etatCA2.setDureeAnneeN1(4.0);
		etatCA2.setTotalJoursAnneeN1Old(4.0);
		etatCA2.setTotalJoursOld(10.0);
		etatCA2.setTotalJoursAnneeN1New(0.0);
		etatCA2.setTotalJoursNew(7.0);

		List<EtatDemandeCongesAnnuels> listEtatCA = new ArrayList<EtatDemandeCongesAnnuels>();
		listEtatCA.add(etatCA1);
		listEtatCA.add(etatCA2);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(new ReturnMessageDto());

		ICongesAnnuelsRepository congeAnnuelRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congeAnnuelRepository.getListEtatDemandeCongesAnnuelsApprouveValideAndAnnuleByIdAgent(idAgentConcerne)).thenReturn(listEtatCA);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "congeAnnuelRepository", congeAnnuelRepository);

		List<DemandeDto> result = service.getListDemandesCAToAddOrRemoveOnAgentCounter(idAgent, idAgentConcerne);

		assertEquals(2, result.size());
	}

	@Test
	public void getHistoAlimAutoRecup() {

		Integer convertedIdAgent = 9005138;

		AgentWeekRecup agentRecup = new AgentWeekRecup();
		agentRecup.setDateDay(new DateTime(2016, 6, 20, 10, 0, 0).toDate());
		agentRecup.setDateMonday(null);
		agentRecup.setIdAgent(convertedIdAgent);
		agentRecup.setIdPointage(10);
		agentRecup.setLastModification(new DateTime(2016, 6, 30, 21, 2, 0).toDate());
		agentRecup.setMinutes(50);

		AgentWeekRecup agentRecup2 = new AgentWeekRecup();
		agentRecup2.setDateDay(null);
		agentRecup2.setDateMonday(new DateTime(2016, 6, 10, 0, 0, 0).toDate());
		agentRecup2.setIdAgent(convertedIdAgent);
		agentRecup2.setIdPointage(25);
		agentRecup2.setLastModification(new DateTime(2016, 6, 28, 10, 5, 0).toDate());
		agentRecup2.setMinutes(95);

		List<AgentWeekRecup> listAgentRecup = new ArrayList<AgentWeekRecup>();
		listAgentRecup.add(agentRecup);
		listAgentRecup.add(agentRecup2);

		IRecuperationRepository recuperationRepository = Mockito.mock(IRecuperationRepository.class);
		Mockito.when(recuperationRepository.getListeAlimAutoRecupByAgent(convertedIdAgent)).thenReturn(listAgentRecup);

		AgentGeneriqueDto agentDto = new AgentGeneriqueDto();
		agentDto.setIdAgent(convertedIdAgent);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(convertedIdAgent)).thenReturn(agentDto);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "recuperationRepository", recuperationRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		List<MoisAlimAutoCongesAnnuelsDto> result = service.getHistoAlimAutoRecup(convertedIdAgent);

		assertEquals(2, result.size());
		assertNotNull(result.get(0).getAgent());
		assertEquals(result.get(0).getDateModification(), agentRecup.getLastModification());
		assertEquals(result.get(0).getDateMois(), agentRecup.getDateDay());
		assertEquals(result.get(0).getNbJours().intValue(), agentRecup.getMinutes());
		assertEquals(result.get(0).getStatus(), "Pointage du 20/06/2016 10:00:00");

		assertNotNull(result.get(0).getAgent());
		assertEquals(result.get(1).getDateModification(), agentRecup2.getLastModification());
		assertEquals(result.get(1).getDateMois(), agentRecup2.getDateMonday());
		assertEquals(result.get(1).getNbJours().intValue(), agentRecup2.getMinutes());
		assertEquals(result.get(1).getStatus(), "Issu de la ventilation de la semaine du 10/06/2016");
	}

	@Test
	public void saveCommentaireDRH_NoDemande_ReturnError() {
		Integer idDemande = 12;

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, idDemande)).thenReturn(null);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		ReturnMessageDto result = service.saveCommentaireDRH(idDemande, "comm");

		assertEquals(1, result.getErrors().size());
		assertEquals("La demande n'existe pas.", result.getErrors().get(0));
		assertEquals(0, result.getInfos().size());
		assertNotNull(result);
	}

	@Test
	public void saveCommentaireDRH_OK() {
		Integer idDemande = 12;
		Demande dem = new Demande();

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, idDemande)).thenReturn(dem);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		ReturnMessageDto result = service.saveCommentaireDRH(idDemande, "comm");

		assertEquals(0, result.getErrors().size());
		assertEquals(1, result.getInfos().size());
		assertEquals("Le commentaire DRH est bien sauvegardé.", result.getInfos().get(0));
		assertEquals("comm", dem.getCommentaireDRH());
		assertNotNull(result);
	}

	@Test
	public void saveCommentaireDRH_WithNull_OK() {
		Integer idDemande = 12;
		Demande dem = new Demande();
		dem.setCommentaireDRH("comm");

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, idDemande)).thenReturn(dem);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		ReturnMessageDto result = service.saveCommentaireDRH(idDemande, null);

		assertEquals(0, result.getErrors().size());
		assertEquals(1, result.getInfos().size());
		assertEquals("Le commentaire DRH est bien sauvegardé.", result.getInfos().get(0));
		assertNull(dem.getCommentaireDRH());
		assertNotNull(result);
	}

	@Test
	public void saveDemandeMaladie_OK_avecEtatProvisoire() {

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.MALADIES.getValue());

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10.5);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.MALADIE_AT.getValue());
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

		LightUser user = new LightUser();
		user.setMail("nono@test");

		List<LightUser> listDest = new ArrayList<>();
		listDest.add(user);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isUserOperateur(idAgent)).thenReturn(true);
		Mockito.when(accessRightsRepository.getAgentDroitFetchAgents(idAgent)).thenReturn(droitOperateur);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(DemandeMaladies.class, dto.getIdDemande())).thenReturn(new DemandeMaladies());
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeMaladies obj = (DemandeMaladies) args[0];

				assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(10, obj.getDuree().intValue());
				assertTrue(obj.isDateDebutAM());
				assertTrue(obj.isDateFinAM());
				assertFalse(obj.isDateDebutPM());
				assertFalse(obj.isDateFinPM());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(DemandeMaladies.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getDateFin(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyDouble(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(dateFin);
		Mockito.when(helperService.getDateDebut(Mockito.any(RefTypeSaisi.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(dateDebut);
		Mockito.when(helperService.calculNombreJoursArrondiDemiJournee(dateDebut, dateFin)).thenReturn(dto.getDuree());

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(absDataConsistencyRules).processDataConsistencyDemande(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
				Mockito.isA(DemandeMaladies.class), Mockito.anyBoolean());

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
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(absDataConsistencyRules);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(accessRightsService.getIdApprobateurOfDelegataire(Mockito.anyInt(), Mockito.anyInt())).thenReturn(null);
		Mockito.when(accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(ReturnMessageDto.class)))
				.thenReturn(new ReturnMessageDto());

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(dto.getIdTypeDemande())).thenReturn(typeSaisi);

		IAlfrescoCMISService alfrescoCMISService = Mockito.mock(IAlfrescoCMISService.class);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(Mockito.anyInt())).thenReturn(new AgentGeneriqueDto());
		Mockito.when(sirhWSConsumer.getEmailDestinataire()).thenReturn(listDest);

		JavaMailSender mailSender = Mockito.mock(JavaMailSender.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(mailSender).send(Mockito.isA(MimeMessagePreparator.class));

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		ReflectionTestUtils.setField(service, "alfrescoCMISService", alfrescoCMISService);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "mailSender", mailSender);
		ReflectionTestUtils.setField(service, "typeEnvironnement", "DEV");

		result = service.saveDemande(idAgent, dto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).persistEntity(Mockito.isA(Demande.class));
		// Le mail ne doit pas être envoyé, car la demande possède un id => c'est une modification
		verify(mailSender, times(0)).send(Mockito.isA(MimeMessagePreparator.class));
	}
	
	@Test
	public void countDemandesAViserOuApprouver_approbateur_1result() throws Exception {
		
		Integer idAgent = 9005138;
		List<Integer> idAgentConcerne = Arrays.asList(9005000);
		
		DemandeDto demandeDto = new DemandeDto();
		demandeDto.setModifierApprobation(true);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		IAbsenceDataConsistencyRules absenceDataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		
		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.countListeDemandesForListAgent(idAgent, idAgentConcerne, null, null, null, null, null)).thenReturn(1);
		
		DemandeRecursiveTaskSimple multiTaskSimple = PowerMockito.mock(DemandeRecursiveTaskSimple.class);
		PowerMockito.whenNew(DemandeRecursiveTaskSimple.class)
				.withArguments(Mockito.anyList(), Mockito.anyInt(), Mockito.anyList(), Mockito.anyBoolean()).thenReturn(multiTaskSimple);

		ForkJoinPool pool = PowerMockito.mock(ForkJoinPool.class);
		PowerMockito.whenNew(ForkJoinPool.class).withNoArguments().thenReturn(pool);
		PowerMockito.when(pool.invoke(multiTaskSimple)).thenReturn(Arrays.asList(demandeDto));

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absenceDataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		int result = service.countDemandesAViserOuApprouver(idAgent, idAgentConcerne, false, true);
		
		assertEquals(result, 1);
	}
	
	@Test
	public void countDemandesAViserOuApprouver_approbateur_0result() throws Exception {
		
		Integer idAgent = 9005138;
		List<Integer> idAgentConcerne = Arrays.asList(9005000);
		
		DemandeDto demandeDto = new DemandeDto();
		demandeDto.setModifierApprobation(false);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		IAbsenceDataConsistencyRules absenceDataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		
		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.countListeDemandesForListAgent(idAgent, idAgentConcerne, null, null, null, null, null)).thenReturn(1);
		
		DemandeRecursiveTaskSimple multiTaskSimple = PowerMockito.mock(DemandeRecursiveTaskSimple.class);
		PowerMockito.whenNew(DemandeRecursiveTaskSimple.class)
				.withArguments(Mockito.anyList(), Mockito.anyInt(), Mockito.anyList(), Mockito.anyBoolean()).thenReturn(multiTaskSimple);

		ForkJoinPool pool = PowerMockito.mock(ForkJoinPool.class);
		PowerMockito.whenNew(ForkJoinPool.class).withNoArguments().thenReturn(pool);
		PowerMockito.when(pool.invoke(multiTaskSimple)).thenReturn(Arrays.asList(demandeDto));

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absenceDataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		int result = service.countDemandesAViserOuApprouver(idAgent, idAgentConcerne, false, true);
		
		assertEquals(result, 0);
	}
	
	@Test
	public void countDemandesAViserOuApprouver_viseur_1result() throws Exception {
		
		Integer idAgent = 9005138;
		List<Integer> idAgentConcerne = Arrays.asList(9005000);
		
		DemandeDto demandeDto = new DemandeDto();
		demandeDto.setModifierVisa(true);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		IAbsenceDataConsistencyRules absenceDataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		
		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.countListeDemandesForListAgent(idAgent, idAgentConcerne, null, null, null, null, null)).thenReturn(1);
		
		DemandeRecursiveTaskSimple multiTaskSimple = PowerMockito.mock(DemandeRecursiveTaskSimple.class);
		PowerMockito.whenNew(DemandeRecursiveTaskSimple.class)
				.withArguments(Mockito.anyList(), Mockito.anyInt(), Mockito.anyList(), Mockito.anyBoolean()).thenReturn(multiTaskSimple);

		ForkJoinPool pool = PowerMockito.mock(ForkJoinPool.class);
		PowerMockito.whenNew(ForkJoinPool.class).withNoArguments().thenReturn(pool);
		PowerMockito.when(pool.invoke(multiTaskSimple)).thenReturn(Arrays.asList(demandeDto));

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absenceDataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		int result = service.countDemandesAViserOuApprouver(idAgent, idAgentConcerne, true, false);
		
		assertEquals(result, 1);
	}
	
	@Test
	public void countDemandesAViserOuApprouver_viseur_0result() throws Exception {
		
		Integer idAgent = 9005138;
		List<Integer> idAgentConcerne = Arrays.asList(9005000);
		
		DemandeDto demandeDto = new DemandeDto();
		demandeDto.setModifierVisa(false);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		IAbsenceDataConsistencyRules absenceDataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		
		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.countListeDemandesForListAgent(idAgent, idAgentConcerne, null, null, null, null, null)).thenReturn(1);
		
		DemandeRecursiveTaskSimple multiTaskSimple = PowerMockito.mock(DemandeRecursiveTaskSimple.class);
		PowerMockito.whenNew(DemandeRecursiveTaskSimple.class)
				.withArguments(Mockito.anyList(), Mockito.anyInt(), Mockito.anyList(), Mockito.anyBoolean()).thenReturn(multiTaskSimple);

		ForkJoinPool pool = PowerMockito.mock(ForkJoinPool.class);
		PowerMockito.whenNew(ForkJoinPool.class).withNoArguments().thenReturn(pool);
		PowerMockito.when(pool.invoke(multiTaskSimple)).thenReturn(Arrays.asList(demandeDto));

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "absenceDataConsistencyRulesImpl", absenceDataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		int result = service.countDemandesAViserOuApprouver(idAgent, idAgentConcerne, true, false);
		
		assertEquals(result, 0);
	}

}
