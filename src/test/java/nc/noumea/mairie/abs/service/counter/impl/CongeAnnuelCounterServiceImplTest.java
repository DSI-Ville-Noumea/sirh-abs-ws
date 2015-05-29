package nc.noumea.mairie.abs.service.counter.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentCongeAnnuelCount;
import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;
import nc.noumea.mairie.abs.domain.AgentJoursFeriesGarde;
import nc.noumea.mairie.abs.domain.AgentWeekCongeAnnuel;
import nc.noumea.mairie.abs.domain.CongeAnnuelRestitutionMassive;
import nc.noumea.mairie.abs.domain.CongeAnnuelRestitutionMassiveHisto;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeCongesAnnuels;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.MotifCompteur;
import nc.noumea.mairie.abs.domain.RefAlimCongeAnnuel;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeSaisiCongeAnnuel;
import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.InfosAlimAutoCongesAnnuelsDto;
import nc.noumea.mairie.abs.dto.MotifCompteurDto;
import nc.noumea.mairie.abs.dto.RefTypeSaisiCongeAnnuelDto;
import nc.noumea.mairie.abs.dto.RestitutionMassiveDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.CongesAnnuelsRepository;
import nc.noumea.mairie.abs.repository.DemandeRepository;
import nc.noumea.mairie.abs.repository.IAccessRightsRepository;
import nc.noumea.mairie.abs.repository.IAgentJoursFeriesGardeRepository;
import nc.noumea.mairie.abs.repository.ICongesAnnuelsRepository;
import nc.noumea.mairie.abs.repository.ICounterRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.abs.repository.ITypeAbsenceRepository;
import nc.noumea.mairie.abs.repository.TypeAbsenceRepository;
import nc.noumea.mairie.abs.service.AgentNotFoundException;
import nc.noumea.mairie.abs.service.IAbsenceDataConsistencyRules;
import nc.noumea.mairie.abs.service.IAgentMatriculeConverterService;
import nc.noumea.mairie.abs.service.impl.HelperService;
import nc.noumea.mairie.abs.web.AccessForbiddenException;
import nc.noumea.mairie.abs.web.NotFoundException;
import nc.noumea.mairie.domain.Spcarr;
import nc.noumea.mairie.domain.Spcc;
import nc.noumea.mairie.domain.Spmatr;
import nc.noumea.mairie.domain.TypeChainePaieEnum;
import nc.noumea.mairie.ws.ISirhWSConsumer;

import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

public class CongeAnnuelCounterServiceImplTest extends AbstractCounterServiceTest {

	private CongeAnnuelCounterServiceImpl service = new CongeAnnuelCounterServiceImpl();

	@Test
	public void testMethodeParenteHeritage() {
		super.allTest(new CongeAnnuelCounterServiceImpl());
	}

	@Test
	public void intitCompteurCongeAnnuel_avecCompteurExistant() {

		super.service = new CongeAnnuelCounterServiceImpl();

		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		Integer idAgentConcerne = 9005131;

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());

		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, idAgentConcerne)).thenReturn(
				new AgentCongeAnnuelCount());

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(result);

		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		result = service.initCompteurCongeAnnuel(idAgent, idAgentConcerne);

		assertEquals(1, result.getErrors().size());
		assertEquals("Le compteur existe déjà.", result.getErrors().get(0).toString());
	}

	@Test
	public void intitCompteurCongeAnnuel_sansCompteurExistant() {

		super.service = new CongeAnnuelCounterServiceImpl();

		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		Integer idAgentConcerne = 9005131;

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());

		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, idAgentConcerne)).thenReturn(null);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(result);

		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		result = service.initCompteurCongeAnnuel(idAgent, idAgentConcerne);

		assertEquals(0, result.getErrors().size());

		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentCongeAnnuelCount.class));
	}

	@Test
	public void resetCompteurCongeAnnuel_compteurInexistant() {

		Integer idAgentCongeAnnuelCount = 1;

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getEntity(AgentCongeAnnuelCount.class, idAgentCongeAnnuelCount))
				.thenReturn(null);

		CongeAnnuelCounterServiceImpl service = new CongeAnnuelCounterServiceImpl();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		ReturnMessageDto result = service.resetCompteurCongeAnnuel(idAgentCongeAnnuelCount);

		assertEquals(1, result.getErrors().size());
		assertEquals("Le compteur n'existe pas.", result.getErrors().get(0));

		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentCongeAnnuelCount.class));
	}

	@Test
	public void resetCompteurCongeAnnuel_OK() {

		Integer idAgentReposCompCount = 1;

		AgentCongeAnnuelCount arc = new AgentCongeAnnuelCount();
		arc.setTotalJours(10.0);
		arc.setTotalJoursAnneeN1(25.0);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getEntity(AgentCongeAnnuelCount.class, idAgentReposCompCount)).thenReturn(arc);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				AgentHistoAlimManuelle obj = (AgentHistoAlimManuelle) args[0];

				String textLog = "Retrait de -10.0 jours sur la nouvelle année.";
				assertEquals(textLog, obj.getText());

				return true;
			}
		}).when(counterRepository).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				AgentCongeAnnuelCount obj = (AgentCongeAnnuelCount) args[0];

				assertEquals(new Double(0), new Double(obj.getTotalJours()));
				assertEquals(new Double(35), new Double(obj.getTotalJoursAnneeN1()));

				return true;
			}
		}).when(counterRepository).persistEntity(Mockito.isA(AgentCongeAnnuelCount.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new DateTime(2013, 4, 2, 8, 56, 12).toDate());

		CongeAnnuelCounterServiceImpl service = new CongeAnnuelCounterServiceImpl();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);

		ReturnMessageDto result = service.resetCompteurCongeAnnuel(idAgentReposCompCount);

		assertEquals(0, result.getErrors().size());

		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentCongeAnnuelCount.class));
	}

	@Test
	public void resetCompteurCongeAnnuel_OKBis() {

		Integer idAgentReposCompCount = 1;

		AgentCongeAnnuelCount arc = new AgentCongeAnnuelCount();
		arc.setTotalJours(10.0);
		arc.setTotalJoursAnneeN1(15.0);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getEntity(AgentCongeAnnuelCount.class, idAgentReposCompCount)).thenReturn(arc);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				AgentHistoAlimManuelle obj = (AgentHistoAlimManuelle) args[0];

				String textLog = "Retrait de -10.0 jours sur la nouvelle année.";
				assertEquals(textLog, obj.getText());

				return true;
			}
		}).when(counterRepository).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				AgentCongeAnnuelCount obj = (AgentCongeAnnuelCount) args[0];

				assertEquals(new Double(0), new Double(obj.getTotalJours()));
				assertEquals(new Double(25), new Double(obj.getTotalJoursAnneeN1()));

				return true;
			}
		}).when(counterRepository).persistEntity(Mockito.isA(AgentCongeAnnuelCount.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new DateTime(2013, 4, 2, 8, 56, 12).toDate());

		CongeAnnuelCounterServiceImpl service = new CongeAnnuelCounterServiceImpl();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);

		ReturnMessageDto result = service.resetCompteurCongeAnnuel(idAgentReposCompCount);

		assertEquals(0, result.getErrors().size());

		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentCongeAnnuelCount.class));
	}

	@Test
	public void majCompteurToAgent_compteurInexistant() {

		ReturnMessageDto srm = new ReturnMessageDto();

		EtatDemande e = new EtatDemande();
		e.setEtat(RefEtatEnum.SAISIE);
		List<EtatDemande> etatsDemande = new ArrayList<>();
		etatsDemande.add(e);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setDuree(2.0);
		demande.setDureeAnneeN1(0.0);
		demande.setIdAgent(9005138);
		demande.setEtatsDemande(etatsDemande);

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, demande.getIdAgent())).thenReturn(
				null);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(demande.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		IAbsenceDataConsistencyRules absCongesAnnuelsDataConsistencyRulesImpl = Mockito
				.mock(IAbsenceDataConsistencyRules.class);
		Mockito.when(
				absCongesAnnuelsDataConsistencyRulesImpl.checkDepassementDroitsAcquis(
						Mockito.any(ReturnMessageDto.class), Mockito.any(DemandeCongesAnnuels.class))).thenReturn(
				new ReturnMessageDto());

		CongeAnnuelCounterServiceImpl service = new CongeAnnuelCounterServiceImpl();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "absCongesAnnuelsDataConsistencyRulesImpl",
				absCongesAnnuelsDataConsistencyRulesImpl);

		ReturnMessageDto result = service.majCompteurToAgent(srm, demande, dto);

		assertEquals(1, result.getErrors().size());
		assertEquals("Le compteur n'existe pas.", result.getErrors().get(0));

		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentCongeAnnuelCount.class));
	}

	@Test
	public void majCompteurToAgent_soldeNegatif_enregistrementOk() {

		ReturnMessageDto srm = new ReturnMessageDto();

		EtatDemande e = new EtatDemande();
		e.setEtat(RefEtatEnum.SAISIE);
		List<EtatDemande> etatsDemande = new ArrayList<>();
		etatsDemande.add(e);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setDuree(6.0);
		demande.setDureeAnneeN1(0.0);
		demande.setIdAgent(9005138);
		demande.setEtatsDemande(etatsDemande);

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());

		AgentCongeAnnuelCount arc = new AgentCongeAnnuelCount();
		arc.setTotalJours(0.0);
		arc.setTotalJoursAnneeN1(0.0);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, demande.getIdAgent())).thenReturn(
				arc);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(demande.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		IAbsenceDataConsistencyRules absCongesAnnuelsDataConsistencyRulesImpl = Mockito
				.mock(IAbsenceDataConsistencyRules.class);
		Mockito.when(
				absCongesAnnuelsDataConsistencyRulesImpl.checkDepassementDroitsAcquis(
						Mockito.any(ReturnMessageDto.class), Mockito.any(DemandeCongesAnnuels.class))).thenReturn(
				new ReturnMessageDto());

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());

		CongeAnnuelCounterServiceImpl service = new CongeAnnuelCounterServiceImpl();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "absCongesAnnuelsDataConsistencyRulesImpl",
				absCongesAnnuelsDataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "helperService", helperService);

		ReturnMessageDto result = service.majCompteurToAgent(srm, demande, dto);

		assertEquals(0, result.getErrors().size());

		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentCongeAnnuelCount.class));
	}

	@Test
	public void majCompteurToAgent_OK() {

		ReturnMessageDto srm = new ReturnMessageDto();

		EtatDemande e = new EtatDemande();
		e.setEtat(RefEtatEnum.SAISIE);
		List<EtatDemande> etatsDemande = new ArrayList<>();
		etatsDemande.add(e);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setDuree(2.0);
		demande.setDureeAnneeN1(0.0);
		demande.setIdAgent(9005138);
		demande.setEtatsDemande(etatsDemande);

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());

		AgentCongeAnnuelCount arc = new AgentCongeAnnuelCount();
		arc.setTotalJours(3.0);
		arc.setTotalJoursAnneeN1(0.0);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, demande.getIdAgent())).thenReturn(
				arc);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(demande.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());

		IAbsenceDataConsistencyRules absCongesAnnuelsDataConsistencyRulesImpl = Mockito
				.mock(IAbsenceDataConsistencyRules.class);
		Mockito.when(
				absCongesAnnuelsDataConsistencyRulesImpl.checkDepassementDroitsAcquis(
						Mockito.any(ReturnMessageDto.class), Mockito.any(DemandeCongesAnnuels.class))).thenReturn(
				new ReturnMessageDto());

		CongeAnnuelCounterServiceImpl service = new CongeAnnuelCounterServiceImpl();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absCongesAnnuelsDataConsistencyRulesImpl",
				absCongesAnnuelsDataConsistencyRulesImpl);

		ReturnMessageDto result = service.majCompteurToAgent(srm, demande, dto);

		assertEquals(0, result.getErrors().size());

		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentCongeAnnuelCount.class));
	}

	@Test
	public void calculJoursCompteur_badEtat() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setDuree(10.0);
		demande.setDureeAnneeN1(20.0);

		IAbsenceDataConsistencyRules absCongesAnnuelsDataConsistencyRulesImpl = Mockito
				.mock(IAbsenceDataConsistencyRules.class);
		Mockito.when(
				absCongesAnnuelsDataConsistencyRulesImpl.checkDepassementDroitsAcquis(
						Mockito.any(ReturnMessageDto.class), Mockito.any(DemandeCongesAnnuels.class))).thenReturn(
				new ReturnMessageDto());

		CongeAnnuelCounterServiceImpl service = new CongeAnnuelCounterServiceImpl();
		ReflectionTestUtils.setField(service, "absCongesAnnuelsDataConsistencyRulesImpl",
				absCongesAnnuelsDataConsistencyRulesImpl);

		Double result = service.calculJoursCompteur(demandeEtatChangeDto, demande);

		assertEquals(0, result.intValue());
	}

	@Test
	public void calculJoursCompteur_debit() {
		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());

		EtatDemande e = new EtatDemande();
		e.setEtat(RefEtatEnum.SAISIE);
		List<EtatDemande> etatsDemande = new ArrayList<>();
		etatsDemande.add(e);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setDuree(10.0);
		demande.setDureeAnneeN1(20.0);
		demande.setEtatsDemande(etatsDemande);

		IAbsenceDataConsistencyRules absCongesAnnuelsDataConsistencyRulesImpl = Mockito
				.mock(IAbsenceDataConsistencyRules.class);
		Mockito.when(
				absCongesAnnuelsDataConsistencyRulesImpl.checkDepassementDroitsAcquis(
						Mockito.any(ReturnMessageDto.class), Mockito.any(DemandeCongesAnnuels.class))).thenReturn(
				new ReturnMessageDto());

		CongeAnnuelCounterServiceImpl service = new CongeAnnuelCounterServiceImpl();
		ReflectionTestUtils.setField(service, "absCongesAnnuelsDataConsistencyRulesImpl",
				absCongesAnnuelsDataConsistencyRulesImpl);

		Double result = service.calculJoursCompteur(demandeEtatChangeDto, demande);

		assertEquals(-30, result.intValue());
	}

	@Test
	public void calculJoursCompteur_credit_EtatRefusee() {
		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.REFUSEE.getCodeEtat());

		EtatDemande e = new EtatDemande();
		e.setEtat(RefEtatEnum.APPROUVEE);
		List<EtatDemande> etatsDemande = new ArrayList<>();
		etatsDemande.add(e);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setDuree(10.0);
		demande.setDureeAnneeN1(20.0);
		demande.setEtatsDemande(etatsDemande);

		IAbsenceDataConsistencyRules absCongesAnnuelsDataConsistencyRulesImpl = Mockito
				.mock(IAbsenceDataConsistencyRules.class);
		Mockito.when(
				absCongesAnnuelsDataConsistencyRulesImpl.checkDepassementDroitsAcquis(
						Mockito.any(ReturnMessageDto.class), Mockito.any(DemandeCongesAnnuels.class))).thenReturn(
				new ReturnMessageDto());

		CongeAnnuelCounterServiceImpl service = new CongeAnnuelCounterServiceImpl();
		ReflectionTestUtils.setField(service, "absCongesAnnuelsDataConsistencyRulesImpl",
				absCongesAnnuelsDataConsistencyRulesImpl);

		Double result = service.calculJoursCompteur(demandeEtatChangeDto, demande);

		assertEquals(30, result.intValue());
	}

	@Test
	public void calculJoursCompteur_credit_EtatAnnule() {
		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());

		EtatDemande e = new EtatDemande();
		e.setEtat(RefEtatEnum.APPROUVEE);
		List<EtatDemande> etatsDemande = new ArrayList<>();
		etatsDemande.add(e);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setDuree(10.0);
		demande.setDureeAnneeN1(20.0);
		demande.setEtatsDemande(etatsDemande);

		IAbsenceDataConsistencyRules absCongesAnnuelsDataConsistencyRulesImpl = Mockito
				.mock(IAbsenceDataConsistencyRules.class);
		Mockito.when(
				absCongesAnnuelsDataConsistencyRulesImpl.checkDepassementDroitsAcquis(
						Mockito.any(ReturnMessageDto.class), Mockito.any(DemandeCongesAnnuels.class))).thenReturn(
				new ReturnMessageDto());

		CongeAnnuelCounterServiceImpl service = new CongeAnnuelCounterServiceImpl();
		ReflectionTestUtils.setField(service, "absCongesAnnuelsDataConsistencyRulesImpl",
				absCongesAnnuelsDataConsistencyRulesImpl);

		Double result = service.calculJoursCompteur(demandeEtatChangeDto, demande);

		assertEquals(30, result.intValue());
	}

	@Test
	public void calculJoursCompteur_credit_EtatAnnulee() {
		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());

		EtatDemande e = new EtatDemande();
		e.setEtat(RefEtatEnum.APPROUVEE);
		List<EtatDemande> etatsDemande = new ArrayList<>();
		etatsDemande.add(e);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setDuree(10.0);
		demande.setDureeAnneeN1(20.0);
		demande.setEtatsDemande(etatsDemande);

		IAbsenceDataConsistencyRules absCongesAnnuelsDataConsistencyRulesImpl = Mockito
				.mock(IAbsenceDataConsistencyRules.class);
		Mockito.when(
				absCongesAnnuelsDataConsistencyRulesImpl.checkDepassementDroitsAcquis(
						Mockito.any(ReturnMessageDto.class), Mockito.any(DemandeCongesAnnuels.class))).thenReturn(
				new ReturnMessageDto());

		CongeAnnuelCounterServiceImpl service = new CongeAnnuelCounterServiceImpl();
		ReflectionTestUtils.setField(service, "absCongesAnnuelsDataConsistencyRulesImpl",
				absCongesAnnuelsDataConsistencyRulesImpl);

		Double result = service.calculJoursCompteur(demandeEtatChangeDto, demande);

		assertEquals(30, result.intValue());
	}

	@Test
	public void calculJoursCompteur_credit_EtatPrise_annuleDemande() {
		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());

		EtatDemande e = new EtatDemande();
		e.setEtat(RefEtatEnum.PRISE);
		List<EtatDemande> etatsDemande = new ArrayList<>();
		etatsDemande.add(e);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setDuree(10.0);
		demande.setDureeAnneeN1(20.0);
		demande.setEtatsDemande(etatsDemande);

		IAbsenceDataConsistencyRules absCongesAnnuelsDataConsistencyRulesImpl = Mockito
				.mock(IAbsenceDataConsistencyRules.class);
		Mockito.when(
				absCongesAnnuelsDataConsistencyRulesImpl.checkDepassementDroitsAcquis(
						Mockito.any(ReturnMessageDto.class), Mockito.any(DemandeCongesAnnuels.class))).thenReturn(
				new ReturnMessageDto());

		CongeAnnuelCounterServiceImpl service = new CongeAnnuelCounterServiceImpl();
		ReflectionTestUtils.setField(service, "absCongesAnnuelsDataConsistencyRulesImpl",
				absCongesAnnuelsDataConsistencyRulesImpl);

		Double result = service.calculJoursCompteur(demandeEtatChangeDto, demande);

		assertEquals(30, result.intValue());
	}

	@Test
	public void calculJoursCompteur_debit_EtatApprouve_ET_DepassementCompteur() {
		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());

		EtatDemande e = new EtatDemande();
		e.setEtat(RefEtatEnum.SAISIE);
		List<EtatDemande> etatsDemande = new ArrayList<>();
		etatsDemande.add(e);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setDuree(10.0);
		demande.setDureeAnneeN1(20.0);
		demande.setEtatsDemande(etatsDemande);

		ReturnMessageDto dto = new ReturnMessageDto();
		dto.getInfos().add("depassement compteur");

		IAbsenceDataConsistencyRules absCongesAnnuelsDataConsistencyRulesImpl = Mockito
				.mock(IAbsenceDataConsistencyRules.class);
		Mockito.when(
				absCongesAnnuelsDataConsistencyRulesImpl.checkDepassementDroitsAcquis(
						Mockito.any(ReturnMessageDto.class), Mockito.any(DemandeCongesAnnuels.class))).thenReturn(dto);

		CongeAnnuelCounterServiceImpl service = new CongeAnnuelCounterServiceImpl();
		ReflectionTestUtils.setField(service, "absCongesAnnuelsDataConsistencyRulesImpl",
				absCongesAnnuelsDataConsistencyRulesImpl);

		Double result = service.calculJoursCompteur(demandeEtatChangeDto, demande);

		assertEquals(0, result.intValue());
	}

	@Test
	public void calculJoursCompteur_credit_MauvaisEtat() {
		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());

		EtatDemande e = new EtatDemande();
		e.setEtat(RefEtatEnum.REFUSEE);
		List<EtatDemande> etatsDemande = new ArrayList<>();
		etatsDemande.add(e);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setDuree(10.0);
		demande.setDureeAnneeN1(20.0);
		demande.setEtatsDemande(etatsDemande);

		IAbsenceDataConsistencyRules absCongesAnnuelsDataConsistencyRulesImpl = Mockito
				.mock(IAbsenceDataConsistencyRules.class);
		Mockito.when(
				absCongesAnnuelsDataConsistencyRulesImpl.checkDepassementDroitsAcquis(
						Mockito.any(ReturnMessageDto.class), Mockito.any(DemandeCongesAnnuels.class))).thenReturn(
				new ReturnMessageDto());

		CongeAnnuelCounterServiceImpl service = new CongeAnnuelCounterServiceImpl();
		ReflectionTestUtils.setField(service, "absCongesAnnuelsDataConsistencyRulesImpl",
				absCongesAnnuelsDataConsistencyRulesImpl);

		Double result = service.calculJoursCompteur(demandeEtatChangeDto, demande);

		assertEquals(0, result.intValue());
	}

	@Test
	public void majManuelleCompteurCongeAnnuelToAgent_agentInexistant() {

		super.service = new CongeAnnuelCounterServiceImpl();
		super.majManuelleCompteurToAgent_prepareData();

		Integer idAgent = 9005138;
		CompteurDto compteurDto = new CompteurDto();
		compteurDto.setIdAgent(9005151);
		compteurDto.setDureeARetrancher(10.0);
		MotifCompteurDto motifDto = new MotifCompteurDto();
		motifDto.setIdMotifCompteur(1);
		compteurDto.setMotifCompteurDto(motifDto);

		ReturnMessageDto result = new ReturnMessageDto();

		Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(true);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculAlimManuelleCompteur(compteurDto)).thenReturn(10.0);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(result);
		Mockito.when(sirhWSConsumer.getAgent(compteurDto.getIdAgent())).thenReturn(null);

		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		boolean isAgentNotFoundException = false;
		try {
			service.majManuelleCompteurToAgent(idAgent, compteurDto);
		} catch (AgentNotFoundException e) {
			isAgentNotFoundException = true;
		}

		assertTrue(isAgentNotFoundException);
	}

	@Test
	public void majManuelleCompteurCongeAnnuelToAgent_CompteurInexistant_And_SoldeNegatif() {

		super.service = new CongeAnnuelCounterServiceImpl();
		super.majManuelleCompteurToAgent_prepareData();

		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		CompteurDto compteurDto = new CompteurDto();
		compteurDto.setIdAgent(9005151);
		compteurDto.setDureeARetrancher(10.0);
		MotifCompteurDto motifDto = new MotifCompteurDto();
		motifDto.setIdMotifCompteur(1);
		compteurDto.setMotifCompteurDto(motifDto);

		Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(true);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculAlimManuelleCompteur(compteurDto)).thenReturn(-10.0);

		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, compteurDto.getIdAgent()))
				.thenReturn(new AgentCongeAnnuelCount());

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(result);
		Mockito.when(sirhWSConsumer.getAgent(compteurDto.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		result = service.majManuelleCompteurToAgent(idAgent, compteurDto);

		assertEquals(1, result.getErrors().size());
		assertEquals("Le solde du compteur de l'agent ne peut pas être négatif.", result.getErrors().get(0).toString());
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentCongeAnnuelCount.class));
	}

	@Test
	public void majManuelleCompteurCongeAnnuelToAgent_CompteurExistant_And_SoldeNegatif() {

		super.service = new CongeAnnuelCounterServiceImpl();
		super.majManuelleCompteurToAgent_prepareData();

		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		CompteurDto compteurDto = new CompteurDto();
		compteurDto.setIdAgent(9005151);
		compteurDto.setDureeARetrancher(10.0);
		MotifCompteurDto motifDto = new MotifCompteurDto();
		motifDto.setIdMotifCompteur(1);
		compteurDto.setMotifCompteurDto(motifDto);

		AgentCongeAnnuelCount arc = new AgentCongeAnnuelCount();
		arc.setTotalJours(5.0);

		Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(true);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculAlimManuelleCompteur(compteurDto)).thenReturn(-10.0);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(result);
		Mockito.when(sirhWSConsumer.getAgent(compteurDto.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, compteurDto.getIdAgent()))
				.thenReturn(arc);

		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		result = service.majManuelleCompteurToAgent(idAgent, compteurDto);

		assertEquals(1, result.getErrors().size());
		assertEquals("Le solde du compteur de l'agent ne peut pas être négatif.", result.getErrors().get(0).toString());
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentCongeAnnuelCount.class));
	}

	@Test
	public void majManuelleCompteurCongeAnnuelToAgent_OK_avecCompteurExistant() {

		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		CompteurDto compteurDto = new CompteurDto();
		compteurDto.setIdAgent(9005151);
		compteurDto.setDureeARetrancher(10.0);
		MotifCompteurDto motifDto = new MotifCompteurDto();
		motifDto.setIdMotifCompteur(1);
		compteurDto.setMotifCompteurDto(motifDto);

		AgentCongeAnnuelCount arc = new AgentCongeAnnuelCount();
		arc.setTotalJours(15.0);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(true);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculAlimManuelleCompteur(compteurDto)).thenReturn(-10.0);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, compteurDto.getIdAgent()))
				.thenReturn(arc);
		Mockito.when(
				counterRepository
						.getEntity(MotifCompteur.class, compteurDto.getMotifCompteurDto().getIdMotifCompteur()))
				.thenReturn(new MotifCompteur());

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(result);
		Mockito.when(sirhWSConsumer.getAgent(compteurDto.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		result = service.majManuelleCompteurToAgent(idAgent, compteurDto);

		assertEquals(0, result.getErrors().size());

		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentCongeAnnuelCount.class));
	}

	@Test
	public void majManuelleCompteurCongeAnnuelToAgent_CompteurInexistant() {

		super.service = new CongeAnnuelCounterServiceImpl();
		super.majManuelleCompteurToAgent_prepareData();

		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		CompteurDto compteurDto = new CompteurDto();
		compteurDto.setIdAgent(9005151);
		compteurDto.setDureeARetrancher(10.0);
		MotifCompteurDto motifDto = new MotifCompteurDto();
		motifDto.setIdMotifCompteur(1);
		compteurDto.setMotifCompteurDto(motifDto);

		Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(true);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculAlimManuelleCompteur(compteurDto)).thenReturn(-10.0);

		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, compteurDto.getIdAgent()))
				.thenReturn(null);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(result);
		Mockito.when(sirhWSConsumer.getAgent(compteurDto.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		result = service.majManuelleCompteurToAgent(idAgent, compteurDto);

		assertEquals(1, result.getErrors().size());
		assertEquals("Le compteur n'existe pas.", result.getErrors().get(0).toString());
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentCongeAnnuelCount.class));
	}

	@Test
	public void alimentationAutoCompteur_CompteurInexistant() {

		Date dateDebut = null;
		Date dateFin = null;
		Integer idAgent = 9005138;

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, idAgent)).thenReturn(null);

		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		ReturnMessageDto result = service.alimentationAutoCompteur(idAgent, dateDebut, dateFin);

		assertEquals(result.getErrors().size(), 1);
		assertEquals(result.getErrors().get(0), CongeAnnuelCounterServiceImpl.COMPTEUR_INEXISTANT);
	}

	@Test
	public void alimentationAutoCompteur_CompteurDejaMisAJour() {

		Integer idAgent = 10;
		Date dateDebut = null;
		Date dateFin = null;

		AgentCongeAnnuelCount acac = Mockito.spy(new AgentCongeAnnuelCount());
		acac.setIdAgent(9005138);
		acac.setIdAgentCount(idAgent);
		acac.setTotalJours(20.0);
		acac.setTotalJoursAnneeN1(10.0);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, idAgent)).thenReturn(acac);

		List<InfosAlimAutoCongesAnnuelsDto> listPA = new ArrayList<InfosAlimAutoCongesAnnuelsDto>();

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getListPAPourAlimAutoCongesAnnuels(9005138, dateDebut, dateFin)).thenReturn(listPA);

		CongesAnnuelsRepository congesAnnuelsRepository = Mockito.mock(CongesAnnuelsRepository.class);
		Mockito.when(congesAnnuelsRepository.getWeekHistoForAgentAndDate(idAgent, dateDebut)).thenReturn(
				new AgentWeekCongeAnnuel());

		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "congesAnnuelsRepository", congesAnnuelsRepository);

		ReturnMessageDto result = service.alimentationAutoCompteur(idAgent, dateDebut, dateFin);

		assertEquals(result.getErrors().size(), 1);
		assertEquals(result.getErrors().get(0),
				String.format(CongeAnnuelCounterServiceImpl.COMPTEUR_DEJA_A_JOUR, idAgent));
		assertEquals(acac.getTotalJours().doubleValue(), 20, 0);
		assertEquals(acac.getTotalJoursAnneeN1().doubleValue(), 10, 0);
	}

	@Test
	public void alimentationAutoCompteur_PAInexistant() {

		Integer idAgent = 10;
		Date dateDebut = null;
		Date dateFin = null;
		Date dateMonth = new Date();

		AgentCongeAnnuelCount acac = Mockito.spy(new AgentCongeAnnuelCount());
		acac.setIdAgent(9005138);
		acac.setIdAgentCount(idAgent);
		acac.setTotalJours(20.0);
		acac.setTotalJoursAnneeN1(10.0);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, idAgent)).thenReturn(acac);

		List<InfosAlimAutoCongesAnnuelsDto> listPA = new ArrayList<InfosAlimAutoCongesAnnuelsDto>();

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getListPAPourAlimAutoCongesAnnuels(9005138, dateDebut, dateFin)).thenReturn(listPA);

		CongesAnnuelsRepository congesAnnuelsRepository = Mockito.mock(CongesAnnuelsRepository.class);
		Mockito.when(congesAnnuelsRepository.getWeekHistoForAgentAndDate(idAgent, dateMonth)).thenReturn(null);

		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "congesAnnuelsRepository", congesAnnuelsRepository);

		ReturnMessageDto result = service.alimentationAutoCompteur(idAgent, dateDebut, dateFin);

		assertEquals(result.getErrors().size(), 1);
		assertEquals(result.getErrors().get(0), String.format(CongeAnnuelCounterServiceImpl.PA_INEXISTANT, idAgent));
		assertEquals(acac.getTotalJours().doubleValue(), 20, 0);
		assertEquals(acac.getTotalJoursAnneeN1().doubleValue(), 10, 0);
	}

	@Test
	public void alimentationAutoCompteur_baseCongesNull() {

		Integer idAgent = 10;
		Date dateDebut = null;
		Date dateFin = null;
		Date dateMonth = new Date();

		AgentCongeAnnuelCount acac = Mockito.spy(new AgentCongeAnnuelCount());
		acac.setIdAgent(9005138);
		acac.setIdAgentCount(idAgent);
		acac.setTotalJours(20.0);
		acac.setTotalJoursAnneeN1(10.0);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, idAgent)).thenReturn(acac);

		List<InfosAlimAutoCongesAnnuelsDto> listPA = new ArrayList<InfosAlimAutoCongesAnnuelsDto>();
		InfosAlimAutoCongesAnnuelsDto pa = new InfosAlimAutoCongesAnnuelsDto();
		pa.setDroitConges(true);
		pa.setDureeDroitConges(0);
		pa.setDateDebut(new DateTime(2014, 2, 5, 0, 0, 0).toDate());
		pa.setDateFin(new DateTime(2014, 2, 25, 0, 0, 0).toDate());
		pa.setIdBaseCongeAbsence(1);
		listPA.add(pa);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getListPAPourAlimAutoCongesAnnuels(9005138, dateDebut, dateFin)).thenReturn(listPA);

		CongesAnnuelsRepository congesAnnuelsRepository = Mockito.mock(CongesAnnuelsRepository.class);
		Mockito.when(congesAnnuelsRepository.getWeekHistoForAgentAndDate(idAgent, dateMonth)).thenReturn(null);

		TypeAbsenceRepository typeAbsenceRepository = Mockito.mock(TypeAbsenceRepository.class);
		Mockito.when(typeAbsenceRepository.getEntity(RefTypeSaisiCongeAnnuel.class, pa.getIdBaseCongeAbsence()))
				.thenReturn(null);

		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "congesAnnuelsRepository", congesAnnuelsRepository);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);

		ReturnMessageDto result = service.alimentationAutoCompteur(idAgent, dateDebut, dateFin);

		assertEquals(result.getErrors().size(), 1);
		assertEquals(
				result.getErrors().get(0),
				String.format(CongeAnnuelCounterServiceImpl.BASE_CONGES_ALIM_AUTO_INEXISTANT,
						pa.getIdBaseCongeAbsence()));
		assertEquals(acac.getTotalJours().doubleValue(), 20, 0);
		assertEquals(acac.getTotalJoursAnneeN1().doubleValue(), 10, 0);
	}

	@Test
	public void alimentationAutoCompteur_ok() {

		Integer idAgent = 10;
		Date dateDebut = new DateTime(2014, 2, 5, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 2, 25, 0, 0, 0).toDate();
		Date dateMonth = new Date();

		AgentCongeAnnuelCount acac = Mockito.spy(new AgentCongeAnnuelCount());
		acac.setIdAgent(9005138);
		acac.setIdAgentCount(idAgent);
		acac.setTotalJours(20.0);
		acac.setTotalJoursAnneeN1(10.0);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, idAgent)).thenReturn(acac);

		List<InfosAlimAutoCongesAnnuelsDto> listPA = new ArrayList<InfosAlimAutoCongesAnnuelsDto>();
		InfosAlimAutoCongesAnnuelsDto pa = new InfosAlimAutoCongesAnnuelsDto();
		pa.setDroitConges(true);
		pa.setDureeDroitConges(0);
		pa.setDateDebut(new DateTime(2014, 2, 5, 0, 0, 0).toDate());
		pa.setDateFin(new DateTime(2014, 2, 25, 0, 0, 0).toDate());
		pa.setIdBaseCongeAbsence(1);
		listPA.add(pa);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getListPAPourAlimAutoCongesAnnuels(9005138, dateDebut, dateFin)).thenReturn(listPA);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreJours(dateDebut, dateFin)).thenReturn(20.0);

		RefAlimCongeAnnuel refAlimCongeAnnuel = new RefAlimCongeAnnuel();
		refAlimCongeAnnuel.setFevrier(10.0);

		RefTypeSaisiCongeAnnuel typeCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeCongeAnnuel.setIdRefTypeSaisiCongeAnnuel(1);

		CongesAnnuelsRepository congesAnnuelsRepository = Mockito.mock(CongesAnnuelsRepository.class);
		Mockito.when(congesAnnuelsRepository.getWeekHistoForAgentAndDate(idAgent, dateMonth)).thenReturn(null);
		Mockito.when(
				congesAnnuelsRepository.getRefAlimCongeAnnuel(typeCongeAnnuel.getIdRefTypeSaisiCongeAnnuel(), 2014))
				.thenReturn(refAlimCongeAnnuel);

		TypeAbsenceRepository typeAbsenceRepository = Mockito.mock(TypeAbsenceRepository.class);
		Mockito.when(typeAbsenceRepository.getEntity(RefTypeSaisiCongeAnnuel.class, pa.getIdBaseCongeAbsence()))
				.thenReturn(typeCongeAnnuel);

		DemandeRepository demandeRepository = Mockito.mock(DemandeRepository.class);
		Mockito.when(demandeRepository.listerDemandeCongeUnique(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				new ArrayList<Demande>());

		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "congesAnnuelsRepository", congesAnnuelsRepository);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		ReturnMessageDto result = service.alimentationAutoCompteur(idAgent, dateDebut, dateFin);

		assertEquals(result.getErrors().size(), 0);
		assertEquals(acac.getTotalJours().doubleValue(), 27, 0);
		assertEquals(acac.getTotalJoursAnneeN1().doubleValue(), 10, 0);
	}

	@Test
	public void alimentationAutoCompteur_ok_typeBaseCongeC_pasDeRepos() {

		Integer idAgent = 10;
		Date dateDebut = new DateTime(2014, 2, 5, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 2, 25, 0, 0, 0).toDate();
		Date dateMonth = new Date();

		AgentCongeAnnuelCount acac = Mockito.spy(new AgentCongeAnnuelCount());
		acac.setIdAgent(9005138);
		acac.setIdAgentCount(idAgent);
		acac.setTotalJours(20.0);
		acac.setTotalJoursAnneeN1(10.0);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, idAgent)).thenReturn(acac);

		List<InfosAlimAutoCongesAnnuelsDto> listPA = new ArrayList<InfosAlimAutoCongesAnnuelsDto>();
		InfosAlimAutoCongesAnnuelsDto pa = new InfosAlimAutoCongesAnnuelsDto();
		pa.setDroitConges(true);
		pa.setDureeDroitConges(0);
		pa.setDateDebut(new DateTime(2014, 2, 5, 0, 0, 0).toDate());
		pa.setDateFin(new DateTime(2014, 2, 25, 0, 0, 0).toDate());
		pa.setIdBaseCongeAbsence(1);
		listPA.add(pa);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getListPAPourAlimAutoCongesAnnuels(9005138, dateDebut, dateFin)).thenReturn(listPA);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreJours(dateDebut, dateFin)).thenReturn(20.0);

		RefAlimCongeAnnuel refAlimCongeAnnuel = new RefAlimCongeAnnuel();
		refAlimCongeAnnuel.setFevrier(10.0);

		RefTypeSaisiCongeAnnuel typeCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeCongeAnnuel.setIdRefTypeSaisiCongeAnnuel(3);
		typeCongeAnnuel.setCodeBaseHoraireAbsence("C");

		CongesAnnuelsRepository congesAnnuelsRepository = Mockito.mock(CongesAnnuelsRepository.class);
		Mockito.when(congesAnnuelsRepository.getWeekHistoForAgentAndDate(idAgent, dateMonth)).thenReturn(null);
		Mockito.when(
				congesAnnuelsRepository.getRefAlimCongeAnnuel(typeCongeAnnuel.getIdRefTypeSaisiCongeAnnuel(), 2014))
				.thenReturn(refAlimCongeAnnuel);

		TypeAbsenceRepository typeAbsenceRepository = Mockito.mock(TypeAbsenceRepository.class);
		Mockito.when(typeAbsenceRepository.getEntity(RefTypeSaisiCongeAnnuel.class, pa.getIdBaseCongeAbsence()))
				.thenReturn(typeCongeAnnuel);

		IAgentJoursFeriesGardeRepository agentJoursFeriesGardeRepository = Mockito
				.mock(IAgentJoursFeriesGardeRepository.class);
		Mockito.when(
				agentJoursFeriesGardeRepository
						.getAgentJoursFeriesGardeByIdAgentAndPeriode(idAgent, dateDebut, dateFin)).thenReturn(
				new ArrayList<AgentJoursFeriesGarde>());

		DemandeRepository demandeRepository = Mockito.mock(DemandeRepository.class);
		Mockito.when(demandeRepository.listerDemandeCongeUnique(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				new ArrayList<Demande>());

		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "congesAnnuelsRepository", congesAnnuelsRepository);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);
		ReflectionTestUtils.setField(service, "agentJoursFeriesGardeRepository", agentJoursFeriesGardeRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		ReturnMessageDto result = service.alimentationAutoCompteur(idAgent, dateDebut, dateFin);

		assertEquals(result.getErrors().size(), 0);
		assertEquals(acac.getTotalJours().doubleValue(), 27, 0);
		assertEquals(acac.getTotalJoursAnneeN1().doubleValue(), 10, 0);
	}

	@Test
	public void alimentationAutoCompteur_ok_typeBaseCongeC_2JoursRepos() {

		Integer idAgent = 10;
		Date dateDebut = new DateTime(2014, 2, 5, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 2, 25, 0, 0, 0).toDate();
		Date dateMonth = new Date();

		AgentCongeAnnuelCount acac = Mockito.spy(new AgentCongeAnnuelCount());
		acac.setIdAgent(9005138);
		acac.setIdAgentCount(idAgent);
		acac.setTotalJours(20.0);
		acac.setTotalJoursAnneeN1(10.0);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, idAgent)).thenReturn(acac);

		List<InfosAlimAutoCongesAnnuelsDto> listPA = new ArrayList<InfosAlimAutoCongesAnnuelsDto>();
		InfosAlimAutoCongesAnnuelsDto pa = new InfosAlimAutoCongesAnnuelsDto();
		pa.setDroitConges(true);
		pa.setDureeDroitConges(0);
		pa.setDateDebut(new DateTime(2014, 2, 5, 0, 0, 0).toDate());
		pa.setDateFin(new DateTime(2014, 2, 25, 0, 0, 0).toDate());
		pa.setIdBaseCongeAbsence(1);
		listPA.add(pa);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getListPAPourAlimAutoCongesAnnuels(9005138, dateDebut, dateFin)).thenReturn(listPA);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreJours(dateDebut, dateFin)).thenReturn(20.0);

		RefAlimCongeAnnuel refAlimCongeAnnuel = new RefAlimCongeAnnuel();
		refAlimCongeAnnuel.setFevrier(10.0);

		RefTypeSaisiCongeAnnuel typeCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeCongeAnnuel.setIdRefTypeSaisiCongeAnnuel(2);
		typeCongeAnnuel.setCodeBaseHoraireAbsence("C");

		CongesAnnuelsRepository congesAnnuelsRepository = Mockito.mock(CongesAnnuelsRepository.class);
		Mockito.when(congesAnnuelsRepository.getWeekHistoForAgentAndDate(idAgent, dateMonth)).thenReturn(null);
		Mockito.when(
				congesAnnuelsRepository.getRefAlimCongeAnnuel(typeCongeAnnuel.getIdRefTypeSaisiCongeAnnuel(), 2014))
				.thenReturn(refAlimCongeAnnuel);

		TypeAbsenceRepository typeAbsenceRepository = Mockito.mock(TypeAbsenceRepository.class);
		Mockito.when(typeAbsenceRepository.getEntity(RefTypeSaisiCongeAnnuel.class, pa.getIdBaseCongeAbsence()))
				.thenReturn(typeCongeAnnuel);

		List<AgentJoursFeriesGarde> listGarde = new ArrayList<AgentJoursFeriesGarde>();
		listGarde.add(new AgentJoursFeriesGarde());
		listGarde.add(new AgentJoursFeriesGarde());

		IAgentJoursFeriesGardeRepository agentJoursFeriesGardeRepository = Mockito
				.mock(IAgentJoursFeriesGardeRepository.class);
		Mockito.when(
				agentJoursFeriesGardeRepository
						.getAgentJoursFeriesGardeByIdAgentAndPeriode(idAgent, dateDebut, dateFin))
				.thenReturn(listGarde);

		DemandeRepository demandeRepository = Mockito.mock(DemandeRepository.class);
		Mockito.when(demandeRepository.listerDemandeCongeUnique(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				new ArrayList<Demande>());

		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "congesAnnuelsRepository", congesAnnuelsRepository);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);
		ReflectionTestUtils.setField(service, "agentJoursFeriesGardeRepository", agentJoursFeriesGardeRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		ReturnMessageDto result = service.alimentationAutoCompteur(idAgent, dateDebut, dateFin);

		assertEquals(result.getErrors().size(), 0);
		assertEquals(acac.getTotalJours().doubleValue(), 29, 0);
		assertEquals(acac.getTotalJoursAnneeN1().doubleValue(), 10, 0);
	}

	@Test
	public void alimentationAutoCompteur_ok_DureeDroitConge_DMPEnGarde() {

		Integer idAgent = 10;
		Date dateDebut = new DateTime(2014, 2, 5, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 2, 25, 0, 0, 0).toDate();
		Date dateMonth = new Date();

		AgentCongeAnnuelCount acac = Mockito.spy(new AgentCongeAnnuelCount());
		acac.setIdAgent(9005138);
		acac.setIdAgentCount(idAgent);
		acac.setTotalJours(20.0);
		acac.setTotalJoursAnneeN1(10.0);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, idAgent)).thenReturn(acac);

		List<InfosAlimAutoCongesAnnuelsDto> listPA = new ArrayList<InfosAlimAutoCongesAnnuelsDto>();
		InfosAlimAutoCongesAnnuelsDto pa = new InfosAlimAutoCongesAnnuelsDto();
		pa.setDroitConges(true);
		pa.setDureeDroitConges(12);
		pa.setDateDebut(new DateTime(2014, 2, 5, 0, 0, 0).toDate());
		pa.setDateFin(new DateTime(2014, 2, 25, 0, 0, 0).toDate());
		pa.setIdBaseCongeAbsence(1);
		pa.setIdAgent(9005138);
		listPA.add(pa);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getListPAPourAlimAutoCongesAnnuels(9005138, dateDebut, dateFin)).thenReturn(listPA);
		Mockito.when(sirhWSConsumer.getListPAByAgent(9005138)).thenReturn(listPA);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreJours(Mockito.any(Date.class), Mockito.any(Date.class))).thenReturn(20.0);

		RefAlimCongeAnnuel refAlimCongeAnnuel = new RefAlimCongeAnnuel();
		refAlimCongeAnnuel.setFevrier(10.0);

		RefTypeSaisiCongeAnnuel typeCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeCongeAnnuel.setIdRefTypeSaisiCongeAnnuel(2);
		typeCongeAnnuel.setCodeBaseHoraireAbsence("C");

		CongesAnnuelsRepository congesAnnuelsRepository = Mockito.mock(CongesAnnuelsRepository.class);
		Mockito.when(congesAnnuelsRepository.getWeekHistoForAgentAndDate(idAgent, dateMonth)).thenReturn(null);
		Mockito.when(
				congesAnnuelsRepository.getRefAlimCongeAnnuel(typeCongeAnnuel.getIdRefTypeSaisiCongeAnnuel(), 2014))
				.thenReturn(refAlimCongeAnnuel);

		TypeAbsenceRepository typeAbsenceRepository = Mockito.mock(TypeAbsenceRepository.class);
		Mockito.when(typeAbsenceRepository.getEntity(RefTypeSaisiCongeAnnuel.class, pa.getIdBaseCongeAbsence()))
				.thenReturn(typeCongeAnnuel);

		List<AgentJoursFeriesGarde> listGarde = new ArrayList<AgentJoursFeriesGarde>();
		listGarde.add(new AgentJoursFeriesGarde());
		listGarde.add(new AgentJoursFeriesGarde());

		IAgentJoursFeriesGardeRepository agentJoursFeriesGardeRepository = Mockito
				.mock(IAgentJoursFeriesGardeRepository.class);
		Mockito.when(
				agentJoursFeriesGardeRepository
						.getAgentJoursFeriesGardeByIdAgentAndPeriode(idAgent, dateDebut, dateFin))
				.thenReturn(listGarde);

		DemandeRepository demandeRepository = Mockito.mock(DemandeRepository.class);
		Mockito.when(demandeRepository.listerDemandeCongeUnique(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				new ArrayList<Demande>());

		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "congesAnnuelsRepository", congesAnnuelsRepository);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);
		ReflectionTestUtils.setField(service, "agentJoursFeriesGardeRepository", agentJoursFeriesGardeRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		ReturnMessageDto result = service.alimentationAutoCompteur(idAgent, dateDebut, dateFin);

		assertEquals(result.getErrors().size(), 0);
		assertEquals(acac.getTotalJours().doubleValue(), 29, 0);
		assertEquals(acac.getTotalJoursAnneeN1().doubleValue(), 10, 0);
	}

	@Test
	public void alimentationAutoCompteur_ok_DureeDroitConge() {

		Integer idAgent = 10;
		Date dateDebut = new DateTime(2014, 2, 5, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 2, 25, 0, 0, 0).toDate();
		Date dateMonth = new Date();

		AgentCongeAnnuelCount acac = Mockito.spy(new AgentCongeAnnuelCount());
		acac.setIdAgent(9005138);
		acac.setIdAgentCount(idAgent);
		acac.setTotalJours(20.0);
		acac.setTotalJoursAnneeN1(10.0);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, idAgent)).thenReturn(acac);

		List<InfosAlimAutoCongesAnnuelsDto> listPA = new ArrayList<InfosAlimAutoCongesAnnuelsDto>();
		InfosAlimAutoCongesAnnuelsDto pa = new InfosAlimAutoCongesAnnuelsDto();
		pa.setDroitConges(true);
		pa.setDureeDroitConges(12);
		pa.setDateDebut(new DateTime(2014, 2, 5, 0, 0, 0).toDate());
		pa.setDateFin(new DateTime(2014, 2, 25, 0, 0, 0).toDate());
		pa.setIdBaseCongeAbsence(1);
		pa.setIdAgent(9005138);
		listPA.add(pa);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getListPAPourAlimAutoCongesAnnuels(9005138, dateDebut, dateFin)).thenReturn(listPA);
		Mockito.when(sirhWSConsumer.getListPAByAgent(9005138)).thenReturn(listPA);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreJours(Mockito.any(Date.class), Mockito.any(Date.class))).thenReturn(20.0);

		RefAlimCongeAnnuel refAlimCongeAnnuel = new RefAlimCongeAnnuel();
		refAlimCongeAnnuel.setFevrier(10.0);

		RefTypeSaisiCongeAnnuel typeCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeCongeAnnuel.setIdRefTypeSaisiCongeAnnuel(2);
		typeCongeAnnuel.setCodeBaseHoraireAbsence("D");

		CongesAnnuelsRepository congesAnnuelsRepository = Mockito.mock(CongesAnnuelsRepository.class);
		Mockito.when(congesAnnuelsRepository.getWeekHistoForAgentAndDate(idAgent, dateMonth)).thenReturn(null);
		Mockito.when(
				congesAnnuelsRepository.getRefAlimCongeAnnuel(typeCongeAnnuel.getIdRefTypeSaisiCongeAnnuel(), 2014))
				.thenReturn(refAlimCongeAnnuel);

		TypeAbsenceRepository typeAbsenceRepository = Mockito.mock(TypeAbsenceRepository.class);
		Mockito.when(typeAbsenceRepository.getEntity(RefTypeSaisiCongeAnnuel.class, pa.getIdBaseCongeAbsence()))
				.thenReturn(typeCongeAnnuel);

		List<AgentJoursFeriesGarde> listGarde = new ArrayList<AgentJoursFeriesGarde>();
		listGarde.add(new AgentJoursFeriesGarde());
		listGarde.add(new AgentJoursFeriesGarde());

		IAgentJoursFeriesGardeRepository agentJoursFeriesGardeRepository = Mockito
				.mock(IAgentJoursFeriesGardeRepository.class);
		Mockito.when(
				agentJoursFeriesGardeRepository
						.getAgentJoursFeriesGardeByIdAgentAndPeriode(idAgent, dateDebut, dateFin))
				.thenReturn(listGarde);

		DemandeRepository demandeRepository = Mockito.mock(DemandeRepository.class);
		Mockito.when(demandeRepository.listerDemandeCongeUnique(Mockito.anyInt(), Mockito.anyInt())).thenReturn(
				new ArrayList<Demande>());

		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "congesAnnuelsRepository", congesAnnuelsRepository);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);
		ReflectionTestUtils.setField(service, "agentJoursFeriesGardeRepository", agentJoursFeriesGardeRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		ReturnMessageDto result = service.alimentationAutoCompteur(idAgent, dateDebut, dateFin);

		assertEquals(result.getErrors().size(), 0);
		assertEquals(acac.getTotalJours().doubleValue(), 27, 0);
		assertEquals(acac.getTotalJoursAnneeN1().doubleValue(), 10, 0);
	}

	@Test
	public void getNombreJoursDonnantDroitsAConges_quotaUnTiers() {

		Integer dernierJourMois = 30;
		Double quotaMois = 10.0;
		Double nombreJoursPA = 10.0;

		Double result = service.getNombreJoursDonnantDroitsAConges(dernierJourMois, quotaMois, nombreJoursPA);

		assertEquals(result, 3, 5);
	}

	@Test
	public void getNombreJoursDonnantDroitsAConges_MoisEntier() {

		Integer dernierJourMois = 30;
		Double quotaMois = 10.0;
		Double nombreJoursPA = 31.0;

		Double result = service.getNombreJoursDonnantDroitsAConges(dernierJourMois, quotaMois, nombreJoursPA);

		assertEquals(result, 10, 0);
	}

	@Test
	public void getNombreJoursDonnantDroitsAConges_MoitieMois() {

		Integer dernierJourMois = 30;
		Double quotaMois = 10.0;
		Double nombreJoursPA = 15.0;

		Double result = service.getNombreJoursDonnantDroitsAConges(dernierJourMois, quotaMois, nombreJoursPA);

		assertEquals(result, 5, 0);
	}

	@Test
	public void checkRestitutionMassiveDto_TypeRestitution() {

		ReturnMessageDto srm = new ReturnMessageDto();
		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setApresMidi(false);
		dto.setDateRestitution(new DateTime().withDayOfYear(new DateTime().getDayOfYear() - 1).toDate());
		dto.setMotif("motif");

		srm = service.checkRestitutionMassiveDto(dto, srm);

		assertEquals(1, srm.getErrors().size());
		assertEquals(CongeAnnuelCounterServiceImpl.TYPE_RESTITUTION_OBLIGATOIRE, srm.getErrors().get(0));
	}

	@Test
	public void checkRestitutionMassiveDto_motif() {

		ReturnMessageDto srm = new ReturnMessageDto();
		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setApresMidi(true);
		dto.setDateRestitution(new DateTime().withDayOfYear(new DateTime().getDayOfYear() - 1).toDate());
		dto.setMotif("");

		srm = service.checkRestitutionMassiveDto(dto, srm);

		assertEquals(1, srm.getErrors().size());
		assertEquals(CongeAnnuelCounterServiceImpl.MOTIF_OBLIGATOIRE, srm.getErrors().get(0));
	}

	@Test
	public void checkRestitutionMassiveDto_dateRestitution() {

		ReturnMessageDto srm = new ReturnMessageDto();
		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setJournee(true);
		dto.setDateRestitution(new DateTime().withDayOfYear(new DateTime().getDayOfYear() + 1).toDate());
		dto.setMotif("motif");

		srm = service.checkRestitutionMassiveDto(dto, srm);

		assertEquals(1, srm.getErrors().size());
		assertEquals(CongeAnnuelCounterServiceImpl.DATE_JOUR_RESTITUER_KO, srm.getErrors().get(0));
	}

	@Test
	public void getSamediDecompteARendre_dateRestitution_jeudi() {

		DemandeCongesAnnuels demandeCA = new DemandeCongesAnnuels();
		demandeCA.setNbSamediDecompte(1.0);
		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setDateRestitution(new DateTime(2015, 1, 22, 0, 0, 0).toDate());
		dto.setJournee(true);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isJourHoliday(new DateTime(2015, 1, 23, 0, 0, 0).toDate())).thenReturn(false);

		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		Double result = service.getSamediDecompteARendre(demandeCA, dto);

		assertEquals(result, 0, 0);
	}

	@Test
	public void getSamediDecompteARendre_dateRestitution_jeudi_vendrediFerie() {

		DemandeCongesAnnuels demandeCA = new DemandeCongesAnnuels();
		demandeCA.setNbSamediDecompte(1.0);
		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setDateRestitution(new DateTime(2015, 1, 22, 0, 0, 0).toDate());
		dto.setJournee(true);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isJourHoliday(new DateTime(2015, 1, 23, 0, 0, 0).toDate())).thenReturn(true);
		Mockito.when(sirhWSConsumer.isJourHoliday(new DateTime(2015, 1, 24, 0, 0, 0).toDate())).thenReturn(false);

		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		Double result = service.getSamediDecompteARendre(demandeCA, dto);

		assertEquals(result, 1, 0);
	}

	@Test
	public void getSamediDecompteARendre_dateRestitution_jeudi_vendrediFerieEtSamediChome() {

		DemandeCongesAnnuels demandeCA = new DemandeCongesAnnuels();
		demandeCA.setNbSamediDecompte(1.0);
		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setDateRestitution(new DateTime(2015, 1, 22, 0, 0, 0).toDate());
		dto.setJournee(true);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isJourHoliday(new DateTime(2015, 1, 24, 0, 0, 0).toDate())).thenReturn(true);
		Mockito.when(sirhWSConsumer.isJourHoliday(new DateTime(2015, 1, 25, 0, 0, 0).toDate())).thenReturn(true);

		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		Double result = service.getSamediDecompteARendre(demandeCA, dto);

		assertEquals(result, 0, 0);
	}

	@Test
	public void getSamediDecompteARendre_dateRestitutionVendredi_pasSamediDecompteDansDemandeCA() {

		DemandeCongesAnnuels demandeCA = new DemandeCongesAnnuels();
		demandeCA.setNbSamediDecompte(0.0);
		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setDateRestitution(new DateTime(2015, 1, 23, 0, 0, 0).toDate());
		dto.setJournee(true);

		Double result = service.getSamediDecompteARendre(demandeCA, dto);

		assertEquals(result, 0, 0);
	}

	@Test
	public void getSamediDecompteARendre_DateRestitueJourneeComplete_1samedi() {

		DemandeCongesAnnuels demandeCA = new DemandeCongesAnnuels();
		demandeCA.setNbSamediDecompte(1.0);
		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setDateRestitution(new DateTime(2015, 1, 23, 0, 0, 0).toDate());
		dto.setJournee(true);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isJourHoliday(new DateTime(2015, 1, 24, 0, 0, 0).toDate())).thenReturn(false);

		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		Double result = service.getSamediDecompteARendre(demandeCA, dto);

		assertEquals(result, 1, 0);
	}

	@Test
	public void getSamediDecompteARendre_DateRestitueJourneeComplete_samediFerie() {

		DemandeCongesAnnuels demandeCA = new DemandeCongesAnnuels();
		demandeCA.setNbSamediDecompte(1.0);
		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setDateRestitution(new DateTime(2015, 1, 23, 0, 0, 0).toDate());
		dto.setJournee(true);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isJourHoliday(new DateTime(2015, 1, 24, 0, 0, 0).toDate())).thenReturn(true);

		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		Double result = service.getSamediDecompteARendre(demandeCA, dto);

		assertEquals(result, 0, 0);
	}

	@Test
	public void getSamediDecompteARendre_DateRestituePM_1samedi() {

		DemandeCongesAnnuels demandeCA = new DemandeCongesAnnuels();
		demandeCA.setNbSamediDecompte(1.0);
		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setDateRestitution(new DateTime(2015, 1, 23, 0, 0, 0).toDate());
		dto.setJournee(false);
		dto.setApresMidi(true);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isJourHoliday(new DateTime(2015, 1, 24, 0, 0, 0).toDate())).thenReturn(false);

		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		Double result = service.getSamediDecompteARendre(demandeCA, dto);

		assertEquals(result, 1, 0);
	}

	@Test
	public void getSamediDecompteARendre_DateRestitueAM_0samedi() {

		DemandeCongesAnnuels demandeCA = new DemandeCongesAnnuels();
		demandeCA.setNbSamediDecompte(1.0);
		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setDateRestitution(new DateTime(2015, 1, 23, 0, 0, 0).toDate());
		dto.setJournee(false);
		dto.setApresMidi(false);
		dto.setMatin(true);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isJourHoliday(new DateTime(2015, 1, 24, 0, 0, 0).toDate())).thenReturn(false);

		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		Double result = service.getSamediDecompteARendre(demandeCA, dto);

		assertEquals(result, 0, 5);
	}

	@Test
	public void getSamediOffertARendre_dateRestitution_pasVendredi() {

		DemandeCongesAnnuels demandeCA = new DemandeCongesAnnuels();
		demandeCA.setNbSamediOffert(1.0);
		demandeCA.setNbSamediDecompte(0.0);
		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setDateRestitution(new DateTime(2015, 1, 22, 0, 0, 0).toDate());
		dto.setJournee(true);

		Double result = service.getSamediOffertARendre(demandeCA, dto);

		assertEquals(result, 0, 0);
	}

	@Test
	public void getSamediOffertARendre_dateRestitutionVendredi_pasSamediOffertDansDemandeCA() {

		DemandeCongesAnnuels demandeCA = new DemandeCongesAnnuels();
		demandeCA.setNbSamediOffert(0.0);
		demandeCA.setNbSamediDecompte(0.0);
		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setDateRestitution(new DateTime(2015, 1, 23, 0, 0, 0).toDate());
		dto.setJournee(true);

		Double result = service.getSamediOffertARendre(demandeCA, dto);

		assertEquals(result, 0, 0);
	}

	@Test
	public void getSamediOffertARendre_DateRestitueJourneeComplete_1samedi() {

		DemandeCongesAnnuels demandeCA = new DemandeCongesAnnuels();
		demandeCA.setNbSamediOffert(1.0);
		demandeCA.setNbSamediDecompte(0.0);
		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setDateRestitution(new DateTime(2015, 1, 23, 0, 0, 0).toDate());
		dto.setJournee(true);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isJourHoliday(new DateTime(2015, 1, 24, 0, 0, 0).toDate())).thenReturn(false);

		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		Double result = service.getSamediOffertARendre(demandeCA, dto);

		assertEquals(result, 1, 0);
	}

	// on rendra plutot un samedi decompte qu un samedi offert
	@Test
	public void getSamediOffertARendre_DateRestitueJourneeComplete_Mais1SamediDecompte_0samedi() {

		DemandeCongesAnnuels demandeCA = new DemandeCongesAnnuels();
		demandeCA.setNbSamediOffert(1.0);
		demandeCA.setNbSamediDecompte(1.0);
		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setDateRestitution(new DateTime(2015, 1, 23, 0, 0, 0).toDate());
		dto.setJournee(true);

		Double result = service.getSamediOffertARendre(demandeCA, dto);

		assertEquals(result, 0, 0);
	}

	@Test
	public void getSamediOffertARendre_DateRestituePM_0samedi() {

		DemandeCongesAnnuels demandeCA = new DemandeCongesAnnuels();
		demandeCA.setNbSamediOffert(1.0);
		demandeCA.setNbSamediDecompte(0.0);
		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setDateRestitution(new DateTime(2015, 1, 23, 0, 0, 0).toDate());
		dto.setJournee(false);
		dto.setApresMidi(true);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isJourHoliday(new DateTime(2015, 1, 24, 0, 0, 0).toDate())).thenReturn(false);

		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		Double result = service.getSamediOffertARendre(demandeCA, dto);

		assertEquals(result, 0, 0);
	}

	@Test
	public void getSamediOffertARendre_DateRestitueAM_0samedi() {

		DemandeCongesAnnuels demandeCA = new DemandeCongesAnnuels();
		demandeCA.setNbSamediOffert(1.0);
		demandeCA.setNbSamediDecompte(0.0);
		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setDateRestitution(new DateTime(2015, 1, 23, 0, 0, 0).toDate());
		dto.setJournee(false);
		dto.setApresMidi(false);
		dto.setMatin(true);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isJourHoliday(new DateTime(2015, 1, 24, 0, 0, 0).toDate())).thenReturn(false);

		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		Double result = service.getSamediOffertARendre(demandeCA, dto);

		assertEquals(result, 0, 0);
	}

	@Test
	public void restitutionMassiveCA_utilisateurSIRHNonHabilite() {

		Integer idAgent = 9005138;

		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setDateRestitution(new DateTime().withDayOfMonth(new DateTime().getDayOfMonth() - 1).toDate());
		dto.setJournee(false);
		dto.setApresMidi(false);
		dto.setMatin(true);
		dto.setMotif("motif");

		RefTypeSaisiCongeAnnuel typeConge = new RefTypeSaisiCongeAnnuel();
		typeConge.setCodeBaseHoraireAbsence("A");

		RefTypeSaisiCongeAnnuelDto dtoBase = new RefTypeSaisiCongeAnnuelDto();
		dtoBase.setIdRefTypeSaisiCongeAnnuel(1);

		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(
				typeAbsenceRepository.getEntity(RefTypeSaisiCongeAnnuel.class, dtoBase.getIdRefTypeSaisiCongeAnnuel()))
				.thenReturn(typeConge);

		ReturnMessageDto isSIRH = new ReturnMessageDto();
		isSIRH.getErrors().add("non habilité");

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getBaseHoraireAbsence(idAgent, dto.getDateRestitution())).thenReturn(dtoBase);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(isSIRH);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, idAgent)).thenReturn(null);

		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		ReturnMessageDto result = service.restitutionMassiveCA(idAgent, dto, Arrays.asList(idAgent));

		assertEquals(CongeAnnuelCounterServiceImpl.AGENT_NON_HABILITE, result.getErrors().get(0));
	}

	@Test
	public void restitutionMassiveCA_compteurInexistant() {

		Integer idAgent = 9005138;

		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setDateRestitution(new DateTime().withDayOfMonth(new DateTime().getDayOfMonth() - 1).toDate());
		dto.setJournee(false);
		dto.setApresMidi(false);
		dto.setMatin(true);
		dto.setMotif("motif");

		RefTypeSaisiCongeAnnuel typeConge = new RefTypeSaisiCongeAnnuel();
		typeConge.setCodeBaseHoraireAbsence("A");

		RefTypeSaisiCongeAnnuelDto dtoBase = new RefTypeSaisiCongeAnnuelDto();
		dtoBase.setIdRefTypeSaisiCongeAnnuel(1);

		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(
				typeAbsenceRepository.getEntity(RefTypeSaisiCongeAnnuel.class, dtoBase.getIdRefTypeSaisiCongeAnnuel()))
				.thenReturn(typeConge);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getBaseHoraireAbsence(idAgent, dto.getDateRestitution())).thenReturn(dtoBase);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(new ReturnMessageDto());

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, idAgent)).thenReturn(null);

		ICongesAnnuelsRepository congesAnnuelsRepository = Mockito.mock(ICongesAnnuelsRepository.class);

		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "congesAnnuelsRepository", congesAnnuelsRepository);

		ReturnMessageDto result = service.restitutionMassiveCA(idAgent, dto, Arrays.asList(idAgent));

		assertEquals(String.format(CongeAnnuelCounterServiceImpl.COMPTEUR_CA_RESTITUTION_INEXISTANT, idAgent), result
				.getErrors().get(0));
	}

	@Test
	public void restitutionMassiveCA_aucunCAPourAgent() {

		Integer idAgent = 9005138;

		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setDateRestitution(new DateTime().withDayOfMonth(new DateTime().getDayOfMonth() - 1).toDate());
		dto.setJournee(false);
		dto.setApresMidi(false);
		dto.setMatin(true);
		dto.setMotif("motif");

		RefTypeSaisiCongeAnnuel typeConge = new RefTypeSaisiCongeAnnuel();
		typeConge.setCodeBaseHoraireAbsence("A");

		RefTypeSaisiCongeAnnuelDto dtoBase = new RefTypeSaisiCongeAnnuelDto();
		dtoBase.setIdRefTypeSaisiCongeAnnuel(1);

		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(
				typeAbsenceRepository.getEntity(RefTypeSaisiCongeAnnuel.class, dtoBase.getIdRefTypeSaisiCongeAnnuel()))
				.thenReturn(typeConge);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getBaseHoraireAbsence(idAgent, dto.getDateRestitution())).thenReturn(dtoBase);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(new ReturnMessageDto());

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, idAgent)).thenReturn(
				new AgentCongeAnnuelCount());

		Date fromDate = new Date();

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateDebutCongeAnnuel(Mockito.any(RefTypeSaisiCongeAnnuel.class),
						Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(fromDate);

		List<DemandeCongesAnnuels> listCongesAgentpris = null;

		ICongesAnnuelsRepository congesAnnuelsRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congesAnnuelsRepository.getListeDemandesCongesAnnuelsPrisesByAgent(idAgent, fromDate, fromDate))
				.thenReturn(listCongesAgentpris);

		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "congesAnnuelsRepository", congesAnnuelsRepository);

		ReturnMessageDto result = service.restitutionMassiveCA(idAgent, dto, Arrays.asList(idAgent));

		assertEquals(String.format(CongeAnnuelCounterServiceImpl.AGENT_AUCUN_CA, idAgent), result.getErrors().get(0));
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(
				Mockito.isA(CongeAnnuelRestitutionMassive.class));
	}

	@Test
	public void restitutionMassiveCA_lundiMatin_compteurAnneeN() {

		Integer idAgent = 9005138;

		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setDateRestitution(new DateTime(2014, 12, 8, 0, 0, 0).toDate());
		dto.setJournee(false);
		dto.setApresMidi(false);
		dto.setMatin(true);
		dto.setMotif("motif");

		RefTypeSaisiCongeAnnuel typeConge = new RefTypeSaisiCongeAnnuel();
		typeConge.setCodeBaseHoraireAbsence("A");

		RefTypeSaisiCongeAnnuelDto dtoBase = new RefTypeSaisiCongeAnnuelDto();
		dtoBase.setIdRefTypeSaisiCongeAnnuel(1);

		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(
				typeAbsenceRepository.getEntity(RefTypeSaisiCongeAnnuel.class, dtoBase.getIdRefTypeSaisiCongeAnnuel()))
				.thenReturn(typeConge);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getBaseHoraireAbsence(idAgent, dto.getDateRestitution())).thenReturn(dtoBase);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(new ReturnMessageDto());

		AgentCongeAnnuelCount acac = Mockito.spy(new AgentCongeAnnuelCount());
		acac.setTotalJours(0.0);
		acac.setTotalJoursAnneeN1(0.0);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, idAgent)).thenReturn(acac);

		Date fromDate = new Date();

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateDebutCongeAnnuel(Mockito.any(RefTypeSaisiCongeAnnuel.class),
						Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(fromDate);
		Mockito.when(
				helperService.getDateFinCongeAnnuel(Mockito.any(RefTypeSaisiCongeAnnuel.class),
						Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean(),
						Mockito.any(Date.class))).thenReturn(fromDate);

		DemandeCongesAnnuels demande = Mockito.spy(new DemandeCongesAnnuels());
		demande.setDateDebut(new DateTime(2014, 12, 1, 0, 0, 0).toDate());
		demande.setDateFin(new DateTime(2014, 12, 31, 0, 0, 0).toDate());
		demande.setDuree(25.0);
		demande.setDureeAnneeN1(0.0);
		demande.setNbSamediDecompte(4.0);
		demande.setNbSamediOffert(0.0);

		List<DemandeCongesAnnuels> listCongesAgentpris = Mockito.spy(new ArrayList<DemandeCongesAnnuels>());
		listCongesAgentpris.add(demande);

		ICongesAnnuelsRepository congesAnnuelsRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congesAnnuelsRepository.getListeDemandesCongesAnnuelsPrisesByAgent(idAgent, fromDate, fromDate))
				.thenReturn(listCongesAgentpris);

		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(idAgent)).thenReturn(5138);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);

		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "congesAnnuelsRepository", congesAnnuelsRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);

		ReturnMessageDto result = service.restitutionMassiveCA(idAgent, dto, Arrays.asList(idAgent));

		assertEquals(0, result.getErrors().size());
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(
				Mockito.isA(CongeAnnuelRestitutionMassive.class));
		assertEquals(acac.getTotalJours(), 0, 5);
		assertEquals(acac.getTotalJoursAnneeN1(), 0, 0);
		assertEquals(demande.getDuree(), 25, 0);
		assertEquals(demande.getDureeAnneeN1(), 0, 0);
		assertEquals(demande.getNbSamediDecompte(), 4, 0);
		assertEquals(demande.getNbSamediOffert(), 0, 0);
	}

	@Test
	public void restitutionMassiveCA_Mardi_compteurAnneeN() {

		Integer idAgent = 9005138;

		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setDateRestitution(new DateTime(2014, 12, 9, 0, 0, 0).toDate());
		dto.setJournee(true);
		dto.setApresMidi(false);
		dto.setMatin(false);
		dto.setMotif("motif");

		RefTypeSaisiCongeAnnuel typeConge = new RefTypeSaisiCongeAnnuel();
		typeConge.setCodeBaseHoraireAbsence("A");

		RefTypeSaisiCongeAnnuelDto dtoBase = new RefTypeSaisiCongeAnnuelDto();
		dtoBase.setIdRefTypeSaisiCongeAnnuel(1);

		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(
				typeAbsenceRepository.getEntity(RefTypeSaisiCongeAnnuel.class, dtoBase.getIdRefTypeSaisiCongeAnnuel()))
				.thenReturn(typeConge);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getBaseHoraireAbsence(idAgent, dto.getDateRestitution())).thenReturn(dtoBase);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(new ReturnMessageDto());

		AgentCongeAnnuelCount acac = Mockito.spy(new AgentCongeAnnuelCount());
		acac.setTotalJours(0.0);
		acac.setTotalJoursAnneeN1(0.0);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, idAgent)).thenReturn(acac);

		Date fromDate = new Date();

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateDebutCongeAnnuel(Mockito.any(RefTypeSaisiCongeAnnuel.class),
						Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(fromDate);
		Mockito.when(
				helperService.getDateFinCongeAnnuel(Mockito.any(RefTypeSaisiCongeAnnuel.class),
						Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean(),
						Mockito.any(Date.class))).thenReturn(fromDate);

		DemandeCongesAnnuels demande = Mockito.spy(new DemandeCongesAnnuels());
		demande.setDateDebut(new DateTime(2014, 12, 1, 0, 0, 0).toDate());
		demande.setDateFin(new DateTime(2014, 12, 31, 0, 0, 0).toDate());
		demande.setDuree(25.0);
		demande.setDureeAnneeN1(0.0);
		demande.setNbSamediDecompte(4.0);
		demande.setNbSamediOffert(0.0);

		List<DemandeCongesAnnuels> listCongesAgentpris = Mockito.spy(new ArrayList<DemandeCongesAnnuels>());
		listCongesAgentpris.add(demande);

		ICongesAnnuelsRepository congesAnnuelsRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congesAnnuelsRepository.getListeDemandesCongesAnnuelsPrisesByAgent(idAgent, fromDate, fromDate))
				.thenReturn(listCongesAgentpris);

		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(idAgent)).thenReturn(5138);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);

		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "congesAnnuelsRepository", congesAnnuelsRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);

		ReturnMessageDto result = service.restitutionMassiveCA(idAgent, dto, Arrays.asList(idAgent));

		assertEquals(0, result.getErrors().size());
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(
				Mockito.isA(CongeAnnuelRestitutionMassive.class));
		assertEquals(acac.getTotalJours(), 1, 0);
		assertEquals(acac.getTotalJoursAnneeN1(), 0, 0);
		assertEquals(demande.getDuree(), 25, 0);
		assertEquals(demande.getDureeAnneeN1(), 0, 0);
		assertEquals(demande.getNbSamediDecompte(), 4, 0);
		assertEquals(demande.getNbSamediOffert(), 0, 0);
	}

	@Test
	public void restitutionMassiveCA_Vendredi_compteurAnneeN_jeudiTravaille() {

		Integer idAgent = 9005138;

		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setDateRestitution(new DateTime(2014, 12, 12, 0, 0, 0).toDate());
		dto.setJournee(true);
		dto.setApresMidi(false);
		dto.setMatin(false);
		dto.setMotif("motif");

		RefTypeSaisiCongeAnnuel typeConge = new RefTypeSaisiCongeAnnuel();
		typeConge.setCodeBaseHoraireAbsence("A");

		RefTypeSaisiCongeAnnuelDto dtoBase = new RefTypeSaisiCongeAnnuelDto();
		dtoBase.setIdRefTypeSaisiCongeAnnuel(1);

		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(
				typeAbsenceRepository.getEntity(RefTypeSaisiCongeAnnuel.class, dtoBase.getIdRefTypeSaisiCongeAnnuel()))
				.thenReturn(typeConge);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getBaseHoraireAbsence(idAgent, dto.getDateRestitution())).thenReturn(dtoBase);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(new ReturnMessageDto());

		AgentCongeAnnuelCount acac = Mockito.spy(new AgentCongeAnnuelCount());
		acac.setTotalJours(0.0);
		acac.setTotalJoursAnneeN1(0.0);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, idAgent)).thenReturn(acac);

		Date fromDate = new Date();

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateDebutCongeAnnuel(Mockito.any(RefTypeSaisiCongeAnnuel.class),
						Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(fromDate);
		Mockito.when(
				helperService.getDateFinCongeAnnuel(Mockito.any(RefTypeSaisiCongeAnnuel.class),
						Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean(),
						Mockito.any(Date.class))).thenReturn(fromDate);

		DemandeCongesAnnuels demande = Mockito.spy(new DemandeCongesAnnuels());
		demande.setDateDebut(new DateTime(2014, 12, 12, 0, 0, 0).toDate());
		demande.setDateFin(new DateTime(2014, 12, 31, 0, 0, 0).toDate());
		demande.setDuree(25.0);
		demande.setDureeAnneeN1(0.0);
		demande.setNbSamediDecompte(4.0);
		demande.setNbSamediOffert(0.0);

		List<DemandeCongesAnnuels> listCongesAgentpris = Mockito.spy(new ArrayList<DemandeCongesAnnuels>());
		listCongesAgentpris.add(demande);

		ICongesAnnuelsRepository congesAnnuelsRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congesAnnuelsRepository.getListeDemandesCongesAnnuelsPrisesByAgent(idAgent, fromDate, fromDate))
				.thenReturn(listCongesAgentpris);

		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(idAgent)).thenReturn(5138);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);

		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "congesAnnuelsRepository", congesAnnuelsRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);

		ReturnMessageDto result = service.restitutionMassiveCA(idAgent, dto, Arrays.asList(idAgent));

		assertEquals(0, result.getErrors().size());
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(
				Mockito.isA(CongeAnnuelRestitutionMassive.class));
		// vendredi + samedi decompte
		assertEquals(acac.getTotalJours(), 2, 0);
		assertEquals(acac.getTotalJoursAnneeN1(), 0, 0);
		assertEquals(demande.getDuree(), 25, 0);
		assertEquals(demande.getDureeAnneeN1(), 0, 0);
		assertEquals(demande.getNbSamediDecompte(), 4, 0);
		assertEquals(demande.getNbSamediOffert(), 0, 0);
	}

	// on rend que le vendredi et non le samedi car jeudi en conge
	@Test
	public void restitutionMassiveCA_Vendredi_compteurAnneeN_jeudiNonTravaille() {

		Integer idAgent = 9005138;

		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setDateRestitution(new DateTime(2014, 12, 12, 0, 0, 0).toDate());
		dto.setJournee(true);
		dto.setApresMidi(false);
		dto.setMatin(false);
		dto.setMotif("motif");

		RefTypeSaisiCongeAnnuel typeConge = new RefTypeSaisiCongeAnnuel();
		typeConge.setCodeBaseHoraireAbsence("A");

		RefTypeSaisiCongeAnnuelDto dtoBase = new RefTypeSaisiCongeAnnuelDto();
		dtoBase.setIdRefTypeSaisiCongeAnnuel(1);

		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(
				typeAbsenceRepository.getEntity(RefTypeSaisiCongeAnnuel.class, dtoBase.getIdRefTypeSaisiCongeAnnuel()))
				.thenReturn(typeConge);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getBaseHoraireAbsence(idAgent, dto.getDateRestitution())).thenReturn(dtoBase);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(new ReturnMessageDto());

		AgentCongeAnnuelCount acac = Mockito.spy(new AgentCongeAnnuelCount());
		acac.setTotalJours(0.0);
		acac.setTotalJoursAnneeN1(0.0);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, idAgent)).thenReturn(acac);

		Date fromDate = new Date();

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateDebutCongeAnnuel(Mockito.any(RefTypeSaisiCongeAnnuel.class),
						Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(fromDate);
		Mockito.when(
				helperService.getDateFinCongeAnnuel(Mockito.any(RefTypeSaisiCongeAnnuel.class),
						Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean(),
						Mockito.any(Date.class))).thenReturn(fromDate);

		DemandeCongesAnnuels demande = Mockito.spy(new DemandeCongesAnnuels());
		demande.setDateDebut(new DateTime(2014, 12, 11, 0, 0, 0).toDate());
		demande.setDateFin(new DateTime(2014, 12, 31, 0, 0, 0).toDate());
		demande.setDuree(25.0);
		demande.setDureeAnneeN1(0.0);
		demande.setNbSamediDecompte(4.0);
		demande.setNbSamediOffert(0.0);

		List<DemandeCongesAnnuels> listCongesAgentpris = Mockito.spy(new ArrayList<DemandeCongesAnnuels>());
		listCongesAgentpris.add(demande);

		ICongesAnnuelsRepository congesAnnuelsRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congesAnnuelsRepository.getListeDemandesCongesAnnuelsPrisesByAgent(idAgent, fromDate, fromDate))
				.thenReturn(listCongesAgentpris);

		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(idAgent)).thenReturn(5138);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);

		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "congesAnnuelsRepository", congesAnnuelsRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);

		ReturnMessageDto result = service.restitutionMassiveCA(idAgent, dto, Arrays.asList(idAgent));

		assertEquals(0, result.getErrors().size());
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(
				Mockito.isA(CongeAnnuelRestitutionMassive.class));
		assertEquals(acac.getTotalJours(), 2, 0);
		assertEquals(acac.getTotalJoursAnneeN1(), 0, 0);
		assertEquals(demande.getDuree(), 25, 0);
		assertEquals(demande.getDureeAnneeN1(), 0, 0);
		assertEquals(demande.getNbSamediDecompte(), 4, 0);
		assertEquals(demande.getNbSamediOffert(), 0, 0);
	}

	/**
	 * on rend le vendredi apres-midi et le samedi complet decompte on ne tient
	 * pas compte du vendredi matin travaille ou en conge
	 */
	@Test
	public void restitutionMassiveCA_VendrediPM_compteurAnneeN1_travailleVendrediMatin() {

		Integer idAgent = 9005138;

		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setDateRestitution(new DateTime(2014, 12, 12, 0, 0, 0).toDate());
		dto.setJournee(false);
		dto.setApresMidi(true);
		dto.setMatin(false);
		dto.setMotif("motif");

		RefTypeSaisiCongeAnnuel typeConge = new RefTypeSaisiCongeAnnuel();
		typeConge.setCodeBaseHoraireAbsence("A");

		RefTypeSaisiCongeAnnuelDto dtoBase = new RefTypeSaisiCongeAnnuelDto();
		dtoBase.setIdRefTypeSaisiCongeAnnuel(1);

		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(
				typeAbsenceRepository.getEntity(RefTypeSaisiCongeAnnuel.class, dtoBase.getIdRefTypeSaisiCongeAnnuel()))
				.thenReturn(typeConge);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getBaseHoraireAbsence(idAgent, dto.getDateRestitution())).thenReturn(dtoBase);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(new ReturnMessageDto());

		AgentCongeAnnuelCount acac = Mockito.spy(new AgentCongeAnnuelCount());
		acac.setTotalJours(0.0);
		acac.setTotalJoursAnneeN1(0.0);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, idAgent)).thenReturn(acac);

		Date fromDate = new Date();

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateDebutCongeAnnuel(Mockito.any(RefTypeSaisiCongeAnnuel.class),
						Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(fromDate);
		Mockito.when(
				helperService.getDateFinCongeAnnuel(Mockito.any(RefTypeSaisiCongeAnnuel.class),
						Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean(),
						Mockito.any(Date.class))).thenReturn(fromDate);

		DemandeCongesAnnuels demande = Mockito.spy(new DemandeCongesAnnuels());
		demande.setDateDebut(new DateTime(2014, 12, 12, 12, 0, 0).toDate());
		demande.setDateFin(new DateTime(2014, 12, 31, 0, 0, 0).toDate());
		demande.setDuree(0.0);
		demande.setDureeAnneeN1(25.0);
		demande.setNbSamediDecompte(4.0);
		demande.setNbSamediOffert(0.0);

		List<DemandeCongesAnnuels> listCongesAgentpris = Mockito.spy(new ArrayList<DemandeCongesAnnuels>());
		listCongesAgentpris.add(demande);

		ICongesAnnuelsRepository congesAnnuelsRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congesAnnuelsRepository.getListeDemandesCongesAnnuelsPrisesByAgent(idAgent, fromDate, fromDate))
				.thenReturn(listCongesAgentpris);

		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(idAgent)).thenReturn(5138);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);

		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "congesAnnuelsRepository", congesAnnuelsRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);

		ReturnMessageDto result = service.restitutionMassiveCA(idAgent, dto, Arrays.asList(idAgent));

		assertEquals(0, result.getErrors().size());
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(
				Mockito.isA(CongeAnnuelRestitutionMassive.class));
		assertEquals(acac.getTotalJours(), 0, 0);
		assertEquals(acac.getTotalJoursAnneeN1(), 1, 5);
		assertEquals(demande.getDuree(), 0, 0);
		assertEquals(demande.getDureeAnneeN1(), 25, 0);
		assertEquals(demande.getNbSamediDecompte(), 4, 0);
		assertEquals(demande.getNbSamediOffert(), 0, 0);
	}

	/**
	 * le conge s'arrete le vendredi 12h, donc on rend le vendredi PM et le
	 * samedi
	 */
	@Test
	public void restitutionMassiveCA_VendrediPM_compteurAnneeN1_CongeVendrediMatin() {

		Integer idAgent = 9005138;

		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setDateRestitution(new DateTime(2014, 12, 12, 0, 0, 0).toDate());
		dto.setJournee(false);
		dto.setApresMidi(true);
		dto.setMatin(false);
		dto.setMotif("motif");

		RefTypeSaisiCongeAnnuel typeConge = new RefTypeSaisiCongeAnnuel();
		typeConge.setCodeBaseHoraireAbsence("A");

		RefTypeSaisiCongeAnnuelDto dtoBase = new RefTypeSaisiCongeAnnuelDto();
		dtoBase.setIdRefTypeSaisiCongeAnnuel(1);

		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(
				typeAbsenceRepository.getEntity(RefTypeSaisiCongeAnnuel.class, dtoBase.getIdRefTypeSaisiCongeAnnuel()))
				.thenReturn(typeConge);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getBaseHoraireAbsence(idAgent, dto.getDateRestitution())).thenReturn(dtoBase);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(new ReturnMessageDto());

		AgentCongeAnnuelCount acac = Mockito.spy(new AgentCongeAnnuelCount());
		acac.setTotalJours(0.0);
		acac.setTotalJoursAnneeN1(0.0);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, idAgent)).thenReturn(acac);

		Date fromDate = new Date();

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateDebutCongeAnnuel(Mockito.any(RefTypeSaisiCongeAnnuel.class),
						Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(fromDate);
		Mockito.when(
				helperService.getDateFinCongeAnnuel(Mockito.any(RefTypeSaisiCongeAnnuel.class),
						Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean(),
						Mockito.any(Date.class))).thenReturn(fromDate);

		DemandeCongesAnnuels demande = Mockito.spy(new DemandeCongesAnnuels());
		demande.setDateDebut(new DateTime(2014, 12, 12, 0, 0, 0).toDate());
		demande.setDateFin(new DateTime(2014, 12, 31, 0, 0, 0).toDate());
		demande.setDuree(0.0);
		demande.setDureeAnneeN1(25.0);
		demande.setNbSamediDecompte(4.0);
		demande.setNbSamediOffert(0.0);

		List<DemandeCongesAnnuels> listCongesAgentpris = Mockito.spy(new ArrayList<DemandeCongesAnnuels>());
		listCongesAgentpris.add(demande);

		ICongesAnnuelsRepository congesAnnuelsRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congesAnnuelsRepository.getListeDemandesCongesAnnuelsPrisesByAgent(idAgent, fromDate, fromDate))
				.thenReturn(listCongesAgentpris);

		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(idAgent)).thenReturn(5138);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);

		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "congesAnnuelsRepository", congesAnnuelsRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);

		ReturnMessageDto result = service.restitutionMassiveCA(idAgent, dto, Arrays.asList(idAgent));

		assertEquals(0, result.getErrors().size());
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(
				Mockito.isA(CongeAnnuelRestitutionMassive.class));
		assertEquals(acac.getTotalJours(), 0, 0);
		assertEquals(acac.getTotalJoursAnneeN1(), 1, 5);
		assertEquals(demande.getDuree(), 0, 0);
		assertEquals(demande.getDureeAnneeN1(), 25, 0);
		assertEquals(demande.getNbSamediDecompte(), 4, 0);
		assertEquals(demande.getNbSamediOffert(), 0, 0);
	}

	/**
	 * si on restitue le vendredi matin, c est comme si l agent n avait pose que
	 * le vendredi PM, et donc samedi matin decompte on rend le vendredi matin
	 * et le samedi PM soit un jour
	 */
	@Test
	public void restitutionMassiveCA_VendrediMatin_compteurAnneeN1_travailleJeudi() {

		Integer idAgent = 9005138;

		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setDateRestitution(new DateTime(2014, 12, 12, 0, 0, 0).toDate());
		dto.setJournee(false);
		dto.setApresMidi(false);
		dto.setMatin(true);
		dto.setMotif("motif");

		RefTypeSaisiCongeAnnuel typeConge = new RefTypeSaisiCongeAnnuel();
		typeConge.setCodeBaseHoraireAbsence("A");

		RefTypeSaisiCongeAnnuelDto dtoBase = new RefTypeSaisiCongeAnnuelDto();
		dtoBase.setIdRefTypeSaisiCongeAnnuel(1);

		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(
				typeAbsenceRepository.getEntity(RefTypeSaisiCongeAnnuel.class, dtoBase.getIdRefTypeSaisiCongeAnnuel()))
				.thenReturn(typeConge);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getBaseHoraireAbsence(idAgent, dto.getDateRestitution())).thenReturn(dtoBase);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(new ReturnMessageDto());

		AgentCongeAnnuelCount acac = Mockito.spy(new AgentCongeAnnuelCount());
		acac.setTotalJours(0.0);
		acac.setTotalJoursAnneeN1(0.0);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, idAgent)).thenReturn(acac);

		Date fromDate = new Date();

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateDebutCongeAnnuel(Mockito.any(RefTypeSaisiCongeAnnuel.class),
						Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(fromDate);
		Mockito.when(
				helperService.getDateFinCongeAnnuel(Mockito.any(RefTypeSaisiCongeAnnuel.class),
						Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean(),
						Mockito.any(Date.class))).thenReturn(fromDate);

		DemandeCongesAnnuels demande = Mockito.spy(new DemandeCongesAnnuels());
		demande.setDateDebut(new DateTime(2014, 12, 11, 0, 0, 0).toDate());
		demande.setDateFin(new DateTime(2014, 12, 31, 0, 0, 0).toDate());
		demande.setDuree(0.0);
		demande.setDureeAnneeN1(25.0);
		demande.setNbSamediDecompte(4.0);
		demande.setNbSamediOffert(0.0);

		List<DemandeCongesAnnuels> listCongesAgentpris = Mockito.spy(new ArrayList<DemandeCongesAnnuels>());
		listCongesAgentpris.add(demande);

		ICongesAnnuelsRepository congesAnnuelsRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congesAnnuelsRepository.getListeDemandesCongesAnnuelsPrisesByAgent(idAgent, fromDate, fromDate))
				.thenReturn(listCongesAgentpris);

		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(idAgent)).thenReturn(5138);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);

		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "congesAnnuelsRepository", congesAnnuelsRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);

		ReturnMessageDto result = service.restitutionMassiveCA(idAgent, dto, Arrays.asList(idAgent));

		assertEquals(0, result.getErrors().size());
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(
				Mockito.isA(CongeAnnuelRestitutionMassive.class));
		assertEquals(acac.getTotalJours(), 0, 0);
		assertEquals(acac.getTotalJoursAnneeN1(), 1, 0);
		assertEquals(demande.getDuree(), 0, 0);
		assertEquals(demande.getDureeAnneeN1(), 25, 0);
		assertEquals(demande.getNbSamediDecompte(), 4, 0);
		assertEquals(demande.getNbSamediOffert(), 0, 0);
	}

	/**
	 * si on restitue le vendredi matin, c est comme si l agent n avait pose que
	 * le vendredi PM, et donc samedi matin decompte on rend le vendredi matin
	 * et le samedi PM soit un jour le jeudi ne rentre pas en compte
	 */
	@Test
	public void restitutionMassiveCA_VendrediMatin_compteurAnneeN1_congeJeudi() {

		Integer idAgent = 9005138;

		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setDateRestitution(new DateTime(2014, 12, 12, 0, 0, 0).toDate());
		dto.setJournee(false);
		dto.setApresMidi(false);
		dto.setMatin(true);
		dto.setMotif("motif");

		RefTypeSaisiCongeAnnuel typeConge = new RefTypeSaisiCongeAnnuel();
		typeConge.setCodeBaseHoraireAbsence("A");

		RefTypeSaisiCongeAnnuelDto dtoBase = new RefTypeSaisiCongeAnnuelDto();
		dtoBase.setIdRefTypeSaisiCongeAnnuel(1);

		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(
				typeAbsenceRepository.getEntity(RefTypeSaisiCongeAnnuel.class, dtoBase.getIdRefTypeSaisiCongeAnnuel()))
				.thenReturn(typeConge);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getBaseHoraireAbsence(idAgent, dto.getDateRestitution())).thenReturn(dtoBase);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(new ReturnMessageDto());

		AgentCongeAnnuelCount acac = Mockito.spy(new AgentCongeAnnuelCount());
		acac.setTotalJours(0.0);
		acac.setTotalJoursAnneeN1(0.0);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, idAgent)).thenReturn(acac);

		Date fromDate = new Date();

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateDebutCongeAnnuel(Mockito.any(RefTypeSaisiCongeAnnuel.class),
						Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(fromDate);
		Mockito.when(
				helperService.getDateFinCongeAnnuel(Mockito.any(RefTypeSaisiCongeAnnuel.class),
						Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean(),
						Mockito.any(Date.class))).thenReturn(fromDate);

		DemandeCongesAnnuels demande = Mockito.spy(new DemandeCongesAnnuels());
		demande.setDateDebut(new DateTime(2014, 12, 11, 0, 0, 0).toDate());
		demande.setDateFin(new DateTime(2014, 12, 31, 0, 0, 0).toDate());
		demande.setDuree(0.0);
		demande.setDureeAnneeN1(25.0);
		demande.setNbSamediDecompte(4.0);
		demande.setNbSamediOffert(0.0);

		List<DemandeCongesAnnuels> listCongesAgentpris = Mockito.spy(new ArrayList<DemandeCongesAnnuels>());
		listCongesAgentpris.add(demande);

		ICongesAnnuelsRepository congesAnnuelsRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congesAnnuelsRepository.getListeDemandesCongesAnnuelsPrisesByAgent(idAgent, fromDate, fromDate))
				.thenReturn(listCongesAgentpris);

		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(idAgent)).thenReturn(5138);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);

		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "congesAnnuelsRepository", congesAnnuelsRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);

		ReturnMessageDto result = service.restitutionMassiveCA(idAgent, dto, Arrays.asList(idAgent));

		assertEquals(0, result.getErrors().size());
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(
				Mockito.isA(CongeAnnuelRestitutionMassive.class));
		assertEquals(acac.getTotalJours(), 0, 0);
		assertEquals(acac.getTotalJoursAnneeN1(), 1, 0);
		assertEquals(demande.getDuree(), 0, 0);
		assertEquals(demande.getDureeAnneeN1(), 25, 0);
		assertEquals(demande.getNbSamediDecompte(), 4, 0);
		assertEquals(demande.getNbSamediOffert(), 0, 0);
	}

	// on rend le vendredi matin et le samedi apres-midi
	@Test
	public void restitutionMassiveCA_VendrediMatin_compteurAnneeN_samediOffertEtSamediDecompte() {

		Integer idAgent = 9005138;

		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setDateRestitution(new DateTime(2014, 12, 12, 0, 0, 0).toDate());
		dto.setJournee(false);
		dto.setApresMidi(false);
		dto.setMatin(true);
		dto.setMotif("motif");

		RefTypeSaisiCongeAnnuel typeConge = new RefTypeSaisiCongeAnnuel();
		typeConge.setCodeBaseHoraireAbsence("A");

		RefTypeSaisiCongeAnnuelDto dtoBase = new RefTypeSaisiCongeAnnuelDto();
		dtoBase.setIdRefTypeSaisiCongeAnnuel(1);

		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(
				typeAbsenceRepository.getEntity(RefTypeSaisiCongeAnnuel.class, dtoBase.getIdRefTypeSaisiCongeAnnuel()))
				.thenReturn(typeConge);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getBaseHoraireAbsence(idAgent, dto.getDateRestitution())).thenReturn(dtoBase);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(new ReturnMessageDto());

		AgentCongeAnnuelCount acac = Mockito.spy(new AgentCongeAnnuelCount());
		acac.setTotalJours(0.0);
		acac.setTotalJoursAnneeN1(0.0);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, idAgent)).thenReturn(acac);

		Date fromDate = new Date();

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateDebutCongeAnnuel(Mockito.any(RefTypeSaisiCongeAnnuel.class),
						Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(fromDate);
		Mockito.when(
				helperService.getDateFinCongeAnnuel(Mockito.any(RefTypeSaisiCongeAnnuel.class),
						Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean(),
						Mockito.any(Date.class))).thenReturn(fromDate);

		DemandeCongesAnnuels demande = Mockito.spy(new DemandeCongesAnnuels());
		demande.setDateDebut(new DateTime(2014, 12, 1, 0, 0, 0).toDate());
		demande.setDateFin(new DateTime(2014, 12, 31, 0, 0, 0).toDate());
		demande.setDuree(0.0);
		demande.setDureeAnneeN1(25.0);
		demande.setNbSamediDecompte(4.0);
		demande.setNbSamediOffert(1.0);

		List<DemandeCongesAnnuels> listCongesAgentpris = Mockito.spy(new ArrayList<DemandeCongesAnnuels>());
		listCongesAgentpris.add(demande);

		ICongesAnnuelsRepository congesAnnuelsRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congesAnnuelsRepository.getListeDemandesCongesAnnuelsPrisesByAgent(idAgent, fromDate, fromDate))
				.thenReturn(listCongesAgentpris);

		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(idAgent)).thenReturn(5138);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);

		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "congesAnnuelsRepository", congesAnnuelsRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);

		ReturnMessageDto result = service.restitutionMassiveCA(idAgent, dto, Arrays.asList(idAgent));

		assertEquals(0, result.getErrors().size());
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(
				Mockito.isA(CongeAnnuelRestitutionMassive.class));
		assertEquals(acac.getTotalJours(), 0, 0);
		assertEquals(acac.getTotalJoursAnneeN1(), 1, 0);
		assertEquals(demande.getDuree(), 0, 0);
		assertEquals(demande.getDureeAnneeN1(), 25, 0);
		assertEquals(demande.getNbSamediDecompte(), 4, 0);
		assertEquals(demande.getNbSamediOffert(), 1, 0);
	}

	/**
	 * on rend le vendredi et le samedi offert
	 */
	@Test
	public void restitutionMassiveCA_Vendredi_compteurAnneeN_samediOffert() {

		Integer idAgent = 9005138;

		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setDateRestitution(new DateTime(2014, 12, 12, 0, 0, 0).toDate());
		dto.setJournee(true);
		dto.setApresMidi(false);
		dto.setMatin(false);
		dto.setMotif("motif");

		RefTypeSaisiCongeAnnuel typeConge = new RefTypeSaisiCongeAnnuel();
		typeConge.setCodeBaseHoraireAbsence("A");

		RefTypeSaisiCongeAnnuelDto dtoBase = new RefTypeSaisiCongeAnnuelDto();
		dtoBase.setIdRefTypeSaisiCongeAnnuel(1);

		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(
				typeAbsenceRepository.getEntity(RefTypeSaisiCongeAnnuel.class, dtoBase.getIdRefTypeSaisiCongeAnnuel()))
				.thenReturn(typeConge);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getBaseHoraireAbsence(idAgent, dto.getDateRestitution())).thenReturn(dtoBase);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(new ReturnMessageDto());

		AgentCongeAnnuelCount acac = Mockito.spy(new AgentCongeAnnuelCount());
		acac.setTotalJours(0.0);
		acac.setTotalJoursAnneeN1(0.0);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, idAgent)).thenReturn(acac);

		Date fromDate = new Date();

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateDebutCongeAnnuel(Mockito.any(RefTypeSaisiCongeAnnuel.class),
						Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(fromDate);
		Mockito.when(
				helperService.getDateFinCongeAnnuel(Mockito.any(RefTypeSaisiCongeAnnuel.class),
						Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean(),
						Mockito.any(Date.class))).thenReturn(fromDate);

		DemandeCongesAnnuels demande = Mockito.spy(new DemandeCongesAnnuels());
		demande.setDateDebut(new DateTime(2014, 12, 12, 0, 0, 0).toDate());
		demande.setDateFin(new DateTime(2014, 12, 18, 0, 0, 0).toDate());
		demande.setDuree(0.0);
		demande.setDureeAnneeN1(25.0);
		demande.setNbSamediDecompte(0.0);
		demande.setNbSamediOffert(1.0);

		List<DemandeCongesAnnuels> listCongesAgentpris = Mockito.spy(new ArrayList<DemandeCongesAnnuels>());
		listCongesAgentpris.add(demande);

		ICongesAnnuelsRepository congesAnnuelsRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congesAnnuelsRepository.getListeDemandesCongesAnnuelsPrisesByAgent(idAgent, fromDate, fromDate))
				.thenReturn(listCongesAgentpris);

		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(idAgent)).thenReturn(5138);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);

		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "congesAnnuelsRepository", congesAnnuelsRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);

		ReturnMessageDto result = service.restitutionMassiveCA(idAgent, dto, Arrays.asList(idAgent));

		assertEquals(0, result.getErrors().size());
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(
				Mockito.isA(CongeAnnuelRestitutionMassive.class));
		assertEquals(acac.getTotalJours(), 0, 0);
		assertEquals(acac.getTotalJoursAnneeN1(), 1, 0);
		assertEquals(demande.getDuree(), 0, 0);
		assertEquals(demande.getDureeAnneeN1(), 25, 0);
		assertEquals(demande.getNbSamediDecompte(), 0, 0);
		assertEquals(demande.getNbSamediOffert(), 0, 0); // on decompte (rend)
															// le samedi offert
															// direct sur la
															// demande
	}

	// on rend le samedi decompte et pas le samedi offert
	@Test
	public void restitutionMassiveCA_Vendredi_compteurAnneeN_samediOffertEtSamediDecompte() {

		Integer idAgent = 9005138;

		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setDateRestitution(new DateTime(2014, 12, 12, 0, 0, 0).toDate());
		dto.setJournee(true);
		dto.setApresMidi(false);
		dto.setMatin(false);
		dto.setMotif("motif");

		RefTypeSaisiCongeAnnuel typeConge = new RefTypeSaisiCongeAnnuel();
		typeConge.setCodeBaseHoraireAbsence("A");

		RefTypeSaisiCongeAnnuelDto dtoBase = new RefTypeSaisiCongeAnnuelDto();
		dtoBase.setIdRefTypeSaisiCongeAnnuel(1);

		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(
				typeAbsenceRepository.getEntity(RefTypeSaisiCongeAnnuel.class, dtoBase.getIdRefTypeSaisiCongeAnnuel()))
				.thenReturn(typeConge);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getBaseHoraireAbsence(idAgent, dto.getDateRestitution())).thenReturn(dtoBase);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(new ReturnMessageDto());

		AgentCongeAnnuelCount acac = Mockito.spy(new AgentCongeAnnuelCount());
		acac.setTotalJours(0.0);
		acac.setTotalJoursAnneeN1(0.0);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, idAgent)).thenReturn(acac);

		Date fromDate = new Date();

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateDebutCongeAnnuel(Mockito.any(RefTypeSaisiCongeAnnuel.class),
						Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(fromDate);
		Mockito.when(
				helperService.getDateFinCongeAnnuel(Mockito.any(RefTypeSaisiCongeAnnuel.class),
						Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean(),
						Mockito.any(Date.class))).thenReturn(fromDate);

		DemandeCongesAnnuels demande = Mockito.spy(new DemandeCongesAnnuels());
		demande.setDateDebut(new DateTime(2014, 12, 12, 0, 0, 0).toDate());
		demande.setDateFin(new DateTime(2014, 12, 18, 0, 0, 0).toDate());
		demande.setDuree(0.0);
		demande.setDureeAnneeN1(25.0);
		demande.setNbSamediDecompte(1.0);
		demande.setNbSamediOffert(1.0);

		List<DemandeCongesAnnuels> listCongesAgentpris = Mockito.spy(new ArrayList<DemandeCongesAnnuels>());
		listCongesAgentpris.add(demande);

		ICongesAnnuelsRepository congesAnnuelsRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congesAnnuelsRepository.getListeDemandesCongesAnnuelsPrisesByAgent(idAgent, fromDate, fromDate))
				.thenReturn(listCongesAgentpris);

		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(idAgent)).thenReturn(5138);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);

		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "congesAnnuelsRepository", congesAnnuelsRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);

		ReturnMessageDto result = service.restitutionMassiveCA(idAgent, dto, Arrays.asList(idAgent));

		assertEquals(0, result.getErrors().size());
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(
				Mockito.isA(CongeAnnuelRestitutionMassive.class));
		assertEquals(acac.getTotalJours(), 0, 0);
		assertEquals(acac.getTotalJoursAnneeN1(), 2, 0);
		assertEquals(demande.getDuree(), 0, 0);
		assertEquals(demande.getDureeAnneeN1(), 25, 0);
		assertEquals(demande.getNbSamediDecompte(), 1, 0);
		assertEquals(demande.getNbSamediOffert(), 1, 0); // on laisse le samedi
															// offert decompte
	}

	/**
	 * vendredi ferie, restitue le jeudi donc on restitue le jeudi et un samedi,
	 * soit 2 jours
	 */
	@Test
	public void restitutionMassiveCA_jeudi_VendrediFerie() {

		Integer idAgent = 9005138;

		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setDateRestitution(new DateTime(2014, 12, 11, 0, 0, 0).toDate());
		dto.setJournee(true);
		dto.setApresMidi(false);
		dto.setMatin(false);
		dto.setMotif("motif");

		RefTypeSaisiCongeAnnuel typeConge = new RefTypeSaisiCongeAnnuel();
		typeConge.setCodeBaseHoraireAbsence("A");

		RefTypeSaisiCongeAnnuelDto dtoBase = new RefTypeSaisiCongeAnnuelDto();
		dtoBase.setIdRefTypeSaisiCongeAnnuel(1);

		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(
				typeAbsenceRepository.getEntity(RefTypeSaisiCongeAnnuel.class, dtoBase.getIdRefTypeSaisiCongeAnnuel()))
				.thenReturn(typeConge);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getBaseHoraireAbsence(idAgent, dto.getDateRestitution())).thenReturn(dtoBase);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(new ReturnMessageDto());
		Mockito.when(sirhWSConsumer.isJourHoliday(new DateTime(2014, 12, 12, 0, 0, 0).toDate())).thenReturn(true);

		AgentCongeAnnuelCount acac = Mockito.spy(new AgentCongeAnnuelCount());
		acac.setTotalJours(0.0);
		acac.setTotalJoursAnneeN1(0.0);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, idAgent)).thenReturn(acac);

		Date fromDate = new Date();

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateDebutCongeAnnuel(Mockito.any(RefTypeSaisiCongeAnnuel.class),
						Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(fromDate);
		Mockito.when(
				helperService.getDateFinCongeAnnuel(Mockito.any(RefTypeSaisiCongeAnnuel.class),
						Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean(),
						Mockito.any(Date.class))).thenReturn(fromDate);

		DemandeCongesAnnuels demande = Mockito.spy(new DemandeCongesAnnuels());
		demande.setDateDebut(new DateTime(2014, 12, 11, 0, 0, 0).toDate());
		demande.setDateFin(new DateTime(2014, 12, 18, 0, 0, 0).toDate());
		demande.setDuree(0.0);
		demande.setDureeAnneeN1(25.0);
		demande.setNbSamediDecompte(1.0);
		demande.setNbSamediOffert(1.0);

		List<DemandeCongesAnnuels> listCongesAgentpris = Mockito.spy(new ArrayList<DemandeCongesAnnuels>());
		listCongesAgentpris.add(demande);

		ICongesAnnuelsRepository congesAnnuelsRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congesAnnuelsRepository.getListeDemandesCongesAnnuelsPrisesByAgent(idAgent, fromDate, fromDate))
				.thenReturn(listCongesAgentpris);

		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(idAgent)).thenReturn(5138);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);

		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "congesAnnuelsRepository", congesAnnuelsRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);

		ReturnMessageDto result = service.restitutionMassiveCA(idAgent, dto, Arrays.asList(idAgent));

		assertEquals(0, result.getErrors().size());
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(
				Mockito.isA(CongeAnnuelRestitutionMassive.class));
		assertEquals(acac.getTotalJours(), 0, 0);
		assertEquals(acac.getTotalJoursAnneeN1(), 2, 0);
		assertEquals(demande.getDuree(), 0, 0);
		assertEquals(demande.getDureeAnneeN1(), 25, 0);
		assertEquals(demande.getNbSamediDecompte(), 1, 0);
		assertEquals(demande.getNbSamediOffert(), 1, 0); // on laisse le samedi
															// offert decompte
	}

	/**
	 * vendredi ferie, samedi chome, restitue le jeudi donc on restitue que le
	 * jeudi, soit 1 jour
	 */
	@Test
	public void restitutionMassiveCA_jeudi_VendrediFerieEtSamediChome() {

		Integer idAgent = 9005138;

		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setDateRestitution(new DateTime(2014, 12, 11, 0, 0, 0).toDate());
		dto.setJournee(true);
		dto.setApresMidi(false);
		dto.setMatin(false);
		dto.setMotif("motif");

		RefTypeSaisiCongeAnnuel typeConge = new RefTypeSaisiCongeAnnuel();
		typeConge.setCodeBaseHoraireAbsence("A");

		RefTypeSaisiCongeAnnuelDto dtoBase = new RefTypeSaisiCongeAnnuelDto();
		dtoBase.setIdRefTypeSaisiCongeAnnuel(1);

		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(
				typeAbsenceRepository.getEntity(RefTypeSaisiCongeAnnuel.class, dtoBase.getIdRefTypeSaisiCongeAnnuel()))
				.thenReturn(typeConge);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getBaseHoraireAbsence(idAgent, dto.getDateRestitution())).thenReturn(dtoBase);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(new ReturnMessageDto());
		Mockito.when(sirhWSConsumer.isJourHoliday(new DateTime(2014, 12, 12, 0, 0, 0).toDate())).thenReturn(true);
		Mockito.when(sirhWSConsumer.isJourHoliday(new DateTime(2014, 12, 13, 0, 0, 0).toDate())).thenReturn(true);

		AgentCongeAnnuelCount acac = Mockito.spy(new AgentCongeAnnuelCount());
		acac.setTotalJours(0.0);
		acac.setTotalJoursAnneeN1(0.0);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, idAgent)).thenReturn(acac);

		Date fromDate = new Date();

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.getDateDebutCongeAnnuel(Mockito.any(RefTypeSaisiCongeAnnuel.class),
						Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(fromDate);
		Mockito.when(
				helperService.getDateFinCongeAnnuel(Mockito.any(RefTypeSaisiCongeAnnuel.class),
						Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyBoolean(), Mockito.anyBoolean(),
						Mockito.any(Date.class))).thenReturn(fromDate);

		DemandeCongesAnnuels demande = Mockito.spy(new DemandeCongesAnnuels());
		demande.setDateDebut(new DateTime(2014, 12, 11, 0, 0, 0).toDate());
		demande.setDateFin(new DateTime(2014, 12, 18, 0, 0, 0).toDate());
		demande.setDuree(0.0);
		demande.setDureeAnneeN1(25.0);
		demande.setNbSamediDecompte(1.0);
		demande.setNbSamediOffert(1.0);

		List<DemandeCongesAnnuels> listCongesAgentpris = Mockito.spy(new ArrayList<DemandeCongesAnnuels>());
		listCongesAgentpris.add(demande);

		ICongesAnnuelsRepository congesAnnuelsRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congesAnnuelsRepository.getListeDemandesCongesAnnuelsPrisesByAgent(idAgent, fromDate, fromDate))
				.thenReturn(listCongesAgentpris);

		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(idAgent)).thenReturn(5138);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);

		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "congesAnnuelsRepository", congesAnnuelsRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);

		ReturnMessageDto result = service.restitutionMassiveCA(idAgent, dto, Arrays.asList(idAgent));

		assertEquals(0, result.getErrors().size());
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(
				Mockito.isA(CongeAnnuelRestitutionMassive.class));
		assertEquals(acac.getTotalJours(), 0, 0);
		assertEquals(acac.getTotalJoursAnneeN1(), 1, 0);
		assertEquals(demande.getDuree(), 0, 0);
		assertEquals(demande.getDureeAnneeN1(), 25, 0);
		assertEquals(demande.getNbSamediDecompte(), 1, 0);
		assertEquals(demande.getNbSamediOffert(), 1, 0); // on laisse le samedi
															// offert decompte
	}

	@Test
	public void checkCADejaRestitue_Ok() {

		ReturnMessageDto srm = new ReturnMessageDto();

		CongeAnnuelRestitutionMassiveHisto histo = new CongeAnnuelRestitutionMassiveHisto();
		histo.setIdAgent(9005138);

		CongeAnnuelRestitutionMassiveHisto histo2 = new CongeAnnuelRestitutionMassiveHisto();
		histo2.setIdAgent(9005140);

		CongeAnnuelRestitutionMassiveHisto histo3 = new CongeAnnuelRestitutionMassiveHisto();
		histo3.setIdAgent(9005199);

		List<CongeAnnuelRestitutionMassiveHisto> restitutionMassiveHisto = new ArrayList<CongeAnnuelRestitutionMassiveHisto>();
		restitutionMassiveHisto.addAll(Arrays.asList(histo, histo2, histo3));

		CongeAnnuelRestitutionMassive restitution = new CongeAnnuelRestitutionMassive();
		restitution.setRestitutionMassiveHisto(restitutionMassiveHisto);

		boolean result = service.checkCADejaRestitue(srm, restitution, 9005100);

		assertFalse(result);
	}

	@Test
	public void checkCADejaRestitue_KO() {

		ReturnMessageDto srm = new ReturnMessageDto();

		CongeAnnuelRestitutionMassiveHisto histo = new CongeAnnuelRestitutionMassiveHisto();
		histo.setIdAgent(9005138);

		CongeAnnuelRestitutionMassiveHisto histo2 = new CongeAnnuelRestitutionMassiveHisto();
		histo2.setIdAgent(9005140);

		CongeAnnuelRestitutionMassiveHisto histo3 = new CongeAnnuelRestitutionMassiveHisto();
		histo3.setIdAgent(9005199);

		List<CongeAnnuelRestitutionMassiveHisto> restitutionMassiveHisto = new ArrayList<CongeAnnuelRestitutionMassiveHisto>();
		restitutionMassiveHisto.addAll(Arrays.asList(histo, histo2, histo3));

		CongeAnnuelRestitutionMassive restitution = new CongeAnnuelRestitutionMassive();
		restitution.setRestitutionMassiveHisto(restitutionMassiveHisto);

		boolean result = service.checkCADejaRestitue(srm, restitution, 9005140);

		assertTrue(result);
	}

	@Test
	public void checkAutreRestitutionMemeJour_noListRestitution() {

		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		ReturnMessageDto srm = new ReturnMessageDto();

		ICongesAnnuelsRepository congesAnnuelsRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congesAnnuelsRepository.getListCongeAnnuelRestitutionMassiveByDate(dto)).thenReturn(
				new ArrayList<CongeAnnuelRestitutionMassive>());

		ReflectionTestUtils.setField(service, "congesAnnuelsRepository", congesAnnuelsRepository);

		srm = service.checkAutreRestitutionMemeJour(dto, srm);

		assertEquals(0, srm.getErrors().size());
	}

	@Test
	public void checkAutreRestitutionMemeJour_1SameRestitution_0error() {

		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setMatin(true);
		dto.setApresMidi(false);
		dto.setJournee(false);

		ReturnMessageDto srm = new ReturnMessageDto();

		CongeAnnuelRestitutionMassive restitutionExistante = new CongeAnnuelRestitutionMassive();
		restitutionExistante.setMatin(true);
		restitutionExistante.setApresMidi(false);
		restitutionExistante.setJournee(false);
		List<CongeAnnuelRestitutionMassive> listRestitution = new ArrayList<CongeAnnuelRestitutionMassive>();
		listRestitution.add(restitutionExistante);

		ICongesAnnuelsRepository congesAnnuelsRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congesAnnuelsRepository.getListCongeAnnuelRestitutionMassiveByDate(dto)).thenReturn(
				listRestitution);

		ReflectionTestUtils.setField(service, "congesAnnuelsRepository", congesAnnuelsRepository);

		srm = service.checkAutreRestitutionMemeJour(dto, srm);

		assertEquals(0, srm.getErrors().size());
	}

	@Test
	public void checkAutreRestitutionMemeJour_1Restitution_0error() {

		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setMatin(false);
		dto.setApresMidi(true);
		dto.setJournee(false);

		ReturnMessageDto srm = new ReturnMessageDto();

		CongeAnnuelRestitutionMassive restitutionExistante = new CongeAnnuelRestitutionMassive();
		restitutionExistante.setMatin(true);
		restitutionExistante.setApresMidi(false);
		restitutionExistante.setJournee(false);
		List<CongeAnnuelRestitutionMassive> listRestitution = new ArrayList<CongeAnnuelRestitutionMassive>();
		listRestitution.add(restitutionExistante);

		ICongesAnnuelsRepository congesAnnuelsRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congesAnnuelsRepository.getListCongeAnnuelRestitutionMassiveByDate(dto)).thenReturn(
				listRestitution);

		ReflectionTestUtils.setField(service, "congesAnnuelsRepository", congesAnnuelsRepository);

		srm = service.checkAutreRestitutionMemeJour(dto, srm);

		assertEquals(0, srm.getErrors().size());
	}

	@Test
	public void checkAutreRestitutionMemeJour_Restitution_0error() {

		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setMatin(true);
		dto.setApresMidi(false);
		dto.setJournee(false);

		ReturnMessageDto srm = new ReturnMessageDto();

		CongeAnnuelRestitutionMassive restitutionExistante = new CongeAnnuelRestitutionMassive();
		restitutionExistante.setMatin(false);
		restitutionExistante.setApresMidi(true);
		restitutionExistante.setJournee(false);
		List<CongeAnnuelRestitutionMassive> listRestitution = new ArrayList<CongeAnnuelRestitutionMassive>();
		listRestitution.add(restitutionExistante);

		ICongesAnnuelsRepository congesAnnuelsRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congesAnnuelsRepository.getListCongeAnnuelRestitutionMassiveByDate(dto)).thenReturn(
				listRestitution);

		ReflectionTestUtils.setField(service, "congesAnnuelsRepository", congesAnnuelsRepository);

		srm = service.checkAutreRestitutionMemeJour(dto, srm);

		assertEquals(0, srm.getErrors().size());
	}

	@Test
	public void checkAutreRestitutionMemeJour_1Restitution_1OtherError() {

		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setMatin(false);
		dto.setApresMidi(false);
		dto.setJournee(true);

		ReturnMessageDto srm = new ReturnMessageDto();

		CongeAnnuelRestitutionMassive restitutionExistante = new CongeAnnuelRestitutionMassive();
		restitutionExistante.setMatin(false);
		restitutionExistante.setApresMidi(true);
		restitutionExistante.setJournee(false);
		List<CongeAnnuelRestitutionMassive> listRestitution = new ArrayList<CongeAnnuelRestitutionMassive>();
		listRestitution.add(restitutionExistante);

		ICongesAnnuelsRepository congesAnnuelsRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congesAnnuelsRepository.getListCongeAnnuelRestitutionMassiveByDate(dto)).thenReturn(
				listRestitution);

		ReflectionTestUtils.setField(service, "congesAnnuelsRepository", congesAnnuelsRepository);

		srm = service.checkAutreRestitutionMemeJour(dto, srm);

		assertEquals(1, srm.getErrors().size());
		assertEquals(CongeAnnuelCounterServiceImpl.RESTITUTION_EXISTANTE, srm.getErrors().get(0));
	}

	@Test
	public void checkAutreRestitutionMemeJour_Restitution_1OtherError() {

		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setMatin(false);
		dto.setApresMidi(true);
		dto.setJournee(false);

		ReturnMessageDto srm = new ReturnMessageDto();

		CongeAnnuelRestitutionMassive restitutionExistante = new CongeAnnuelRestitutionMassive();
		restitutionExistante.setMatin(false);
		restitutionExistante.setApresMidi(false);
		restitutionExistante.setJournee(true);
		List<CongeAnnuelRestitutionMassive> listRestitution = new ArrayList<CongeAnnuelRestitutionMassive>();
		listRestitution.add(restitutionExistante);

		ICongesAnnuelsRepository congesAnnuelsRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congesAnnuelsRepository.getListCongeAnnuelRestitutionMassiveByDate(dto)).thenReturn(
				listRestitution);

		ReflectionTestUtils.setField(service, "congesAnnuelsRepository", congesAnnuelsRepository);

		srm = service.checkAutreRestitutionMemeJour(dto, srm);

		assertEquals(1, srm.getErrors().size());
		assertEquals(CongeAnnuelCounterServiceImpl.RESTITUTION_EXISTANTE, srm.getErrors().get(0));
	}

	@Test
	public void checkAutreRestitutionMemeJour_Restitution_SameDay() {

		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setMatin(false);
		dto.setApresMidi(false);
		dto.setJournee(true);

		ReturnMessageDto srm = new ReturnMessageDto();

		CongeAnnuelRestitutionMassive restitutionExistante = new CongeAnnuelRestitutionMassive();
		restitutionExistante.setMatin(false);
		restitutionExistante.setApresMidi(false);
		restitutionExistante.setJournee(true);
		List<CongeAnnuelRestitutionMassive> listRestitution = new ArrayList<CongeAnnuelRestitutionMassive>();
		listRestitution.add(restitutionExistante);

		ICongesAnnuelsRepository congesAnnuelsRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congesAnnuelsRepository.getListCongeAnnuelRestitutionMassiveByDate(dto)).thenReturn(
				listRestitution);

		ReflectionTestUtils.setField(service, "congesAnnuelsRepository", congesAnnuelsRepository);

		srm = service.checkAutreRestitutionMemeJour(dto, srm);

		assertEquals(0, srm.getErrors().size());
	}

	@Test
	public void getHistoRestitutionMassiveCA_UserNonHabilite() {

		Integer idAgentConnecte = 9005138;
		ReturnMessageDto srm = new ReturnMessageDto();
		srm.getErrors().add("non habilite");

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgentConnecte)).thenReturn(srm);

		List<CongeAnnuelRestitutionMassive> listRestitutionCA = new ArrayList<CongeAnnuelRestitutionMassive>();

		ICongesAnnuelsRepository congesAnnuelsRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congesAnnuelsRepository.getHistoRestitutionMassiveOrderByDate()).thenReturn(listRestitutionCA);

		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "congesAnnuelsRepository", congesAnnuelsRepository);

		@SuppressWarnings("unused")
		List<RestitutionMassiveDto> result = null;
		try {
			result = service.getHistoRestitutionMassiveCA(idAgentConnecte);
		} catch (AccessForbiddenException e) {
			return;
		}
		fail();
	}

	@Test
	public void getHistoRestitutionMassiveCA_1result() {

		Integer idAgentConnecte = 9005138;

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgentConnecte)).thenReturn(new ReturnMessageDto());

		CongeAnnuelRestitutionMassive restitution = new CongeAnnuelRestitutionMassive();
		restitution.setDateModification(new Date());
		restitution.setDateRestitution(new Date());
		restitution.setIdCongeAnnuelRestitutionMassiveTask(2);
		restitution.setJournee(true);
		restitution.setMatin(false);
		restitution.setApresMidi(true);
		restitution.setMotif("motif");
		restitution.setStatus("status");

		List<CongeAnnuelRestitutionMassive> listRestitutionCA = new ArrayList<CongeAnnuelRestitutionMassive>();
		listRestitutionCA.add(restitution);

		ICongesAnnuelsRepository congesAnnuelsRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congesAnnuelsRepository.getHistoRestitutionMassiveOrderByDate()).thenReturn(listRestitutionCA);

		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "congesAnnuelsRepository", congesAnnuelsRepository);

		List<RestitutionMassiveDto> result = service.getHistoRestitutionMassiveCA(idAgentConnecte);

		assertEquals(1, result.size());
		assertEquals(result.get(0).getDateModification(), restitution.getDateModification());
		assertEquals(result.get(0).getDateRestitution(), restitution.getDateRestitution());
		assertEquals(result.get(0).getIdRestitutionMassive(), restitution.getIdCongeAnnuelRestitutionMassiveTask());
		assertEquals(result.get(0).getMotif(), restitution.getMotif());
		assertEquals(result.get(0).getStatus(), restitution.getStatus());
		assertEquals(result.get(0).isJournee(), restitution.isJournee());
		assertEquals(result.get(0).isMatin(), restitution.isMatin());
		assertEquals(result.get(0).isApresMidi(), restitution.isApresMidi());
		assertEquals(result.get(0).getListHistoAgents().size(), 0);
	}
	
	@Test
	public void getHistoRestitutionMassiveCAByAgent_noResult() {
		
		CongeAnnuelRestitutionMassive restitution = new CongeAnnuelRestitutionMassive();
		restitution.setApresMidi(true);
		restitution.setJournee(false);
		restitution.setMatin(false);
		restitution.setDateRestitution(new DateTime(2015, 1, 23, 0, 0, 0).toDate());
		restitution.setDateModification(new DateTime(2015, 1, 13, 0, 0, 0).toDate());
		restitution.setMotif("motif");
		
		CongeAnnuelRestitutionMassiveHisto histo = new CongeAnnuelRestitutionMassiveHisto();
		histo.setIdAgent(9005138);
		histo.setJours(1.0);
		histo.setRestitutionMassive(restitution);
		histo.setStatus("OK");
		
		CongeAnnuelRestitutionMassive restitution2 = new CongeAnnuelRestitutionMassive();
		restitution2.setApresMidi(true);
		restitution2.setJournee(false);
		restitution2.setMatin(false);
		restitution2.setDateRestitution(new DateTime(2015, 2, 23, 0, 0, 0).toDate());
		restitution2.setDateModification(new DateTime(2015, 2, 13, 0, 0, 0).toDate());
		restitution2.setMotif("motif 2");
		
		CongeAnnuelRestitutionMassiveHisto histo3 = new CongeAnnuelRestitutionMassiveHisto();
		histo3.setIdAgent(9005138);
		histo3.setJours(0.5);
		histo3.setRestitutionMassive(restitution2);
		histo3.setStatus("OK");
		
		List<CongeAnnuelRestitutionMassiveHisto> listRestitutionCA = new ArrayList<CongeAnnuelRestitutionMassiveHisto>();
		listRestitutionCA.add(histo);
		listRestitutionCA.add(histo3);

		ICongesAnnuelsRepository congesAnnuelsRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congesAnnuelsRepository.getListRestitutionMassiveByIdAgent(Arrays.asList(9005138), null, null)).thenReturn(listRestitutionCA);
		
		ReflectionTestUtils.setField(service, "congesAnnuelsRepository", congesAnnuelsRepository);
		
		List<RestitutionMassiveDto> result = service.getHistoRestitutionMassiveCAByAgent(9005138);
		
		assertEquals(2, result.size());
		assertEquals(new DateTime(2015, 1, 13, 0, 0, 0).toDate(), result.get(0).getDateModification());
		assertEquals(new DateTime(2015, 1, 23, 0, 0, 0).toDate(), result.get(0).getDateRestitution());
		assertEquals("motif", result.get(0).getMotif());
		assertEquals(9005138, result.get(0).getListHistoAgents().get(0).getIdAgent().intValue());
		assertEquals(new Double(1), result.get(0).getListHistoAgents().get(0).getJours());

		assertEquals(new DateTime(2015, 2, 13, 0, 0, 0).toDate(), result.get(1).getDateModification());
		assertEquals(new DateTime(2015, 2, 23, 0, 0, 0).toDate(), result.get(1).getDateRestitution());
		assertEquals("motif 2", result.get(1).getMotif());
		assertEquals(9005138, result.get(1).getListHistoAgents().get(0).getIdAgent().intValue());
		assertEquals(new Double(0.5), result.get(1).getListHistoAgents().get(0).getJours());
	}

	@Test
	public void getDetailsHistoRestitutionMassive_UserNonHabilite() {

		Integer idAgentConnecte = 9005138;
		RestitutionMassiveDto dto = new RestitutionMassiveDto();

		ReturnMessageDto srm = new ReturnMessageDto();
		srm.getErrors().add("non habilite");

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgentConnecte)).thenReturn(srm);

		List<CongeAnnuelRestitutionMassive> listRestitutionCA = new ArrayList<CongeAnnuelRestitutionMassive>();

		ICongesAnnuelsRepository congesAnnuelsRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congesAnnuelsRepository.getHistoRestitutionMassiveOrderByDate()).thenReturn(listRestitutionCA);

		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "congesAnnuelsRepository", congesAnnuelsRepository);

		try {
			dto = service.getDetailsHistoRestitutionMassive(idAgentConnecte, dto);
		} catch (AccessForbiddenException e) {
			return;
		}
		fail();
	}

	@Test
	public void getDetailsHistoRestitutionMassive_notFound() {

		Integer idAgentConnecte = 9005138;
		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setIdRestitutionMassive(2);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgentConnecte)).thenReturn(new ReturnMessageDto());

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getEntity(CongeAnnuelRestitutionMassive.class, dto.getIdRestitutionMassive()))
				.thenReturn(null);

		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		try {
			dto = service.getDetailsHistoRestitutionMassive(idAgentConnecte, dto);
		} catch (NotFoundException e) {
			return;
		}

		fail();
	}

	@Test
	public void getDetailsHistoRestitutionMassive_1result_2agents() {

		Integer idAgentConnecte = 9005138;
		RestitutionMassiveDto dto = new RestitutionMassiveDto();
		dto.setIdRestitutionMassive(2);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgentConnecte)).thenReturn(new ReturnMessageDto());

		CongeAnnuelRestitutionMassiveHisto histo = new CongeAnnuelRestitutionMassiveHisto();
		histo.setIdCongeAnnuelRestitutionMassiveHisto(1);
		histo.setIdAgent(9005138);
		histo.setJours(2.0);
		histo.setStatus("OK");
		CongeAnnuelRestitutionMassiveHisto histo2 = new CongeAnnuelRestitutionMassiveHisto();
		histo.setIdCongeAnnuelRestitutionMassiveHisto(2);
		histo.setIdAgent(9005140);
		histo.setJours(5.0);
		histo.setStatus("KO");

		CongeAnnuelRestitutionMassive restitutionMassive = new CongeAnnuelRestitutionMassive();
		restitutionMassive.getRestitutionMassiveHisto().add(histo);
		restitutionMassive.getRestitutionMassiveHisto().add(histo2);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getEntity(CongeAnnuelRestitutionMassive.class, dto.getIdRestitutionMassive()))
				.thenReturn(restitutionMassive);

		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		try {
			dto = service.getDetailsHistoRestitutionMassive(idAgentConnecte, dto);
		} catch (AccessForbiddenException e) {
			fail();
		}

		assertEquals(2, dto.getListHistoAgents().size());
		assertEquals(dto.getListHistoAgents().get(0).getIdAgent(), histo.getIdAgent());
		assertEquals(dto.getListHistoAgents().get(0).getJours(), histo.getJours());
		assertEquals(dto.getListHistoAgents().get(0).getStatus(), histo.getStatus());
		assertEquals(dto.getListHistoAgents().get(1).getIdAgent(), histo2.getIdAgent());
		assertEquals(dto.getListHistoAgents().get(1).getJours(), histo2.getJours());
		assertEquals(dto.getListHistoAgents().get(1).getStatus(), histo2.getStatus());
	}

	@Test
	public void deleteOrUpdateSpcc_pasSpcc() {

		Integer idAgent = 9005138;
		Date dateJour = new Date();
		boolean isDemiJournee = false;

		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(idAgent)).thenReturn(5138);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);

		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeService);

		service.deleteOrUpdateSpcc(idAgent, dateJour, isDemiJournee);

		Mockito.verify(sirhRepository, Mockito.never()).persistEntity(Mockito.isA(Spmatr.class));
		Mockito.verify(sirhRepository, Mockito.never()).persistEntity(Mockito.isA(Spcc.class));
		Mockito.verify(sirhRepository, Mockito.never()).removeEntity(Mockito.isA(Spcc.class));
	}

	@Test
	public void deleteOrUpdateSpcc_pasDeSpcarr() {

		Integer idAgent = 9005138;
		Date dateJour = new Date();
		boolean isDemiJournee = false;

		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(idAgent)).thenReturn(5138);

		Spcc spcc = new Spcc();

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getSpcc(5138, dateJour)).thenReturn(spcc);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(5138, dateJour)).thenReturn(null);

		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeService);

		service.deleteOrUpdateSpcc(idAgent, dateJour, isDemiJournee);

		Mockito.verify(sirhRepository, Mockito.never()).persistEntity(Mockito.isA(Spmatr.class));
		Mockito.verify(sirhRepository, Mockito.never()).persistEntity(Mockito.isA(Spcc.class));
		Mockito.verify(sirhRepository, Mockito.never()).removeEntity(Mockito.isA(Spcc.class));
	}

	@Test
	public void deleteOrUpdateSpcc_removeSpcc_JourneePleine() {

		Integer idAgent = 9005138;
		Date dateJour = new Date();
		boolean isDemiJournee = false;

		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(idAgent)).thenReturn(5138);

		Spcc spcc = new Spcc();

		Spcarr spcarr = new Spcarr();

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getSpcc(5138, dateJour)).thenReturn(spcc);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(5138, dateJour)).thenReturn(spcarr);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getTypeChainePaieFromStatut(spcarr)).thenReturn(TypeChainePaieEnum.SCV);

		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeService);
		ReflectionTestUtils.setField(service, "helperService", helperService);

		service.deleteOrUpdateSpcc(idAgent, dateJour, isDemiJournee);

		Mockito.verify(sirhRepository, Mockito.times(1)).persistEntity(Mockito.isA(Spmatr.class));
		Mockito.verify(sirhRepository, Mockito.never()).persistEntity(Mockito.isA(Spcc.class));
		Mockito.verify(sirhRepository, Mockito.times(1)).removeEntity(Mockito.isA(Spcc.class));
	}

	@Test
	public void deleteOrUpdateSpcc_removeSpcc_demiJournee() {

		Integer idAgent = 9005138;
		Date dateJour = new Date();
		boolean isDemiJournee = true;

		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(idAgent)).thenReturn(5138);

		Spcc spcc = new Spcc();
		spcc.setCode(2);

		Spcarr spcarr = new Spcarr();

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getSpcc(5138, dateJour)).thenReturn(spcc);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(5138, dateJour)).thenReturn(spcarr);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getTypeChainePaieFromStatut(spcarr)).thenReturn(TypeChainePaieEnum.SCV);

		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeService);
		ReflectionTestUtils.setField(service, "helperService", helperService);

		service.deleteOrUpdateSpcc(idAgent, dateJour, isDemiJournee);

		Mockito.verify(sirhRepository, Mockito.times(1)).persistEntity(Mockito.isA(Spmatr.class));
		Mockito.verify(sirhRepository, Mockito.never()).persistEntity(Mockito.isA(Spcc.class));
		Mockito.verify(sirhRepository, Mockito.times(1)).removeEntity(Mockito.isA(Spcc.class));
	}

	@Test
	public void deleteOrUpdateSpcc_updateSpcc_demiJournee() {

		Integer idAgent = 9005138;
		Date dateJour = new Date();
		boolean isDemiJournee = true;

		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(idAgent)).thenReturn(5138);

		Spcc spcc = Mockito.spy(new Spcc());
		spcc.setCode(1);

		Spcarr spcarr = new Spcarr();

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getSpcc(5138, dateJour)).thenReturn(spcc);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(5138, dateJour)).thenReturn(spcarr);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getTypeChainePaieFromStatut(spcarr)).thenReturn(TypeChainePaieEnum.SCV);

		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeService);
		ReflectionTestUtils.setField(service, "helperService", helperService);

		service.deleteOrUpdateSpcc(idAgent, dateJour, isDemiJournee);

		Mockito.verify(sirhRepository, Mockito.times(1)).persistEntity(Mockito.isA(Spmatr.class));
		Mockito.verify(sirhRepository, Mockito.times(1)).persistEntity(Mockito.isA(Spcc.class));
		Mockito.verify(sirhRepository, Mockito.never()).removeEntity(Mockito.isA(Spcc.class));
		assertEquals(2, spcc.getCode().intValue());
	}
}
