package nc.noumea.mairie.abs.service.counter.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentCongeAnnuelCount;
import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;
import nc.noumea.mairie.abs.domain.AgentWeekCongeAnnuel;
import nc.noumea.mairie.abs.domain.DemandeCongesAnnuels;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.MotifCompteur;
import nc.noumea.mairie.abs.domain.RefAlimCongeAnnuel;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.InfosAlimAutoCongesAnnuelsDto;
import nc.noumea.mairie.abs.dto.MotifCompteurDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.CongesAnnuelsRepository;
import nc.noumea.mairie.abs.repository.IAccessRightsRepository;
import nc.noumea.mairie.abs.repository.ICounterRepository;
import nc.noumea.mairie.abs.repository.TypeAbsenceRepository;
import nc.noumea.mairie.abs.service.AgentNotFoundException;
import nc.noumea.mairie.abs.service.impl.HelperService;
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

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setDuree(2.0);
		demande.setDureeAnneeN1(0.0);
		demande.setIdAgent(9005138);

		DemandeEtatChangeDto dto = new DemandeEtatChangeDto();
		dto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, demande.getIdAgent())).thenReturn(
				null);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(demande.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		CongeAnnuelCounterServiceImpl service = new CongeAnnuelCounterServiceImpl();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		ReturnMessageDto result = service.majCompteurToAgent(srm, demande, dto);

		assertEquals(1, result.getErrors().size());
		assertEquals("Le compteur n'existe pas.", result.getErrors().get(0));

		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentCongeAnnuelCount.class));
	}

	@Test
	public void majCompteurToAgent_soldeNegatif() {

		ReturnMessageDto srm = new ReturnMessageDto();

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setDuree(2.0);
		demande.setDureeAnneeN1(0.0);
		demande.setIdAgent(9005138);

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

		CongeAnnuelCounterServiceImpl service = new CongeAnnuelCounterServiceImpl();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		ReturnMessageDto result = service.majCompteurToAgent(srm, demande, dto);

		assertEquals(1, result.getErrors().size());
		assertEquals("Le solde du compteur de l'agent ne peut pas être négatif.", result.getErrors().get(0));

		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentCongeAnnuelCount.class));
	}

	@Test
	public void majCompteurToAgent_OK() {

		ReturnMessageDto srm = new ReturnMessageDto();

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setDuree(2.0);
		demande.setDureeAnneeN1(0.0);
		demande.setIdAgent(9005138);

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

		CongeAnnuelCounterServiceImpl service = new CongeAnnuelCounterServiceImpl();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", helperService);

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

		CongeAnnuelCounterServiceImpl service = new CongeAnnuelCounterServiceImpl();

		Double result = service.calculJoursCompteur(demandeEtatChangeDto, demande);

		assertEquals(0, result.intValue());
	}

	@Test
	public void calculJoursCompteur_debit() {
		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setDuree(10.0);
		demande.setDureeAnneeN1(20.0);

		CongeAnnuelCounterServiceImpl service = new CongeAnnuelCounterServiceImpl();

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

		CongeAnnuelCounterServiceImpl service = new CongeAnnuelCounterServiceImpl();

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

		CongeAnnuelCounterServiceImpl service = new CongeAnnuelCounterServiceImpl();

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

		CongeAnnuelCounterServiceImpl service = new CongeAnnuelCounterServiceImpl();

		Double result = service.calculJoursCompteur(demandeEtatChangeDto, demande);

		assertEquals(30, result.intValue());
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

		CongeAnnuelCounterServiceImpl service = new CongeAnnuelCounterServiceImpl();

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
		Mockito.when(counterRepository.getEntity(MotifCompteur.class, compteurDto.getMotifCompteurDto().getIdMotifCompteur())).thenReturn(
				new MotifCompteur());

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
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, idAgent))
				.thenReturn(null);

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
		Date dateMonth = new Date();
		
		AgentCongeAnnuelCount acac = new AgentCongeAnnuelCount();
			acac.setIdAgent(9005138);
			acac.setIdAgentCount(idAgent);
			acac.setTotalJours(20.0);
			acac.setTotalJoursAnneeN1(10.0);
		
		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, idAgent))
				.thenReturn(acac);
		
		List<InfosAlimAutoCongesAnnuelsDto> listPA = new ArrayList<InfosAlimAutoCongesAnnuelsDto>();
		
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getListPAPourAlimAutoCongesAnnuels(9005138, dateDebut, dateFin)).thenReturn(listPA);
		
		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getFirstMondayOfCurrentMonth()).thenReturn(dateMonth);
		
		CongesAnnuelsRepository congesAnnuelsRepository = Mockito.mock(CongesAnnuelsRepository.class);
		Mockito.when(congesAnnuelsRepository.getWeekHistoForAgentAndDate(idAgent, dateMonth)).thenReturn(new AgentWeekCongeAnnuel());
		
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "congesAnnuelsRepository", congesAnnuelsRepository);
		
		ReturnMessageDto result = service.alimentationAutoCompteur(idAgent, dateDebut, dateFin);
		
		assertEquals(result.getErrors().size(), 1);
		assertEquals(result.getErrors().get(0), String.format(CongeAnnuelCounterServiceImpl.COMPTEUR_DEJA_A_JOUR, idAgent));
	}
	
	@Test
	public void alimentationAutoCompteur_PAInexistant() {
		
		Integer idAgent = 10;
		Date dateDebut = null;
		Date dateFin = null;
		Date dateMonth = new Date();
		
		AgentCongeAnnuelCount acac = new AgentCongeAnnuelCount();
			acac.setIdAgent(9005138);
			acac.setIdAgentCount(idAgent);
			acac.setTotalJours(20.0);
			acac.setTotalJoursAnneeN1(10.0);
		
		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, idAgent))
				.thenReturn(acac);
		
		List<InfosAlimAutoCongesAnnuelsDto> listPA = new ArrayList<InfosAlimAutoCongesAnnuelsDto>();
		
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getListPAPourAlimAutoCongesAnnuels(9005138, dateDebut, dateFin)).thenReturn(listPA);
		
		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getFirstMondayOfCurrentMonth()).thenReturn(dateMonth);
		
		CongesAnnuelsRepository congesAnnuelsRepository = Mockito.mock(CongesAnnuelsRepository.class);
		Mockito.when(congesAnnuelsRepository.getWeekHistoForAgentAndDate(idAgent, dateMonth)).thenReturn(null);
		
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "congesAnnuelsRepository", congesAnnuelsRepository);
		
		
		ReturnMessageDto result = service.alimentationAutoCompteur(idAgent, dateDebut, dateFin);
		
		assertEquals(result.getErrors().size(), 1);
		assertEquals(result.getErrors().get(0), String.format(CongeAnnuelCounterServiceImpl.PA_INEXISTANT, idAgent));
	}
	
	@Test
	public void alimentationAutoCompteur_baseCongesNull() {
		
		Integer idAgent = 10;
		Date dateDebut = null;
		Date dateFin = null;
		Date dateMonth = new Date();
		
		AgentCongeAnnuelCount acac = new AgentCongeAnnuelCount();
			acac.setIdAgent(9005138);
			acac.setIdAgentCount(idAgent);
			acac.setTotalJours(20.0);
			acac.setTotalJoursAnneeN1(10.0);
		
		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, idAgent))
				.thenReturn(acac);
		
		List<InfosAlimAutoCongesAnnuelsDto> listPA = new ArrayList<InfosAlimAutoCongesAnnuelsDto>();
		InfosAlimAutoCongesAnnuelsDto pa = new InfosAlimAutoCongesAnnuelsDto();
		pa.setDroitConges(true);
		pa.setDateDebut(new DateTime(2014, 2, 5, 0, 0, 0).toDate());
		pa.setDateFin(new DateTime(2014, 2, 25, 0, 0, 0).toDate());
		pa.setIdBaseCongeAbsence(1);
		listPA.add(pa);
		
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getListPAPourAlimAutoCongesAnnuels(9005138, dateDebut, dateFin)).thenReturn(listPA);
		
		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getFirstMondayOfCurrentMonth()).thenReturn(dateMonth);
		
		CongesAnnuelsRepository congesAnnuelsRepository = Mockito.mock(CongesAnnuelsRepository.class);
		Mockito.when(congesAnnuelsRepository.getWeekHistoForAgentAndDate(idAgent, dateMonth)).thenReturn(null);
		
		TypeAbsenceRepository typeAbsenceRepository = Mockito.mock(TypeAbsenceRepository.class);
		Mockito.when(typeAbsenceRepository.getEntity(RefAlimCongeAnnuel.class, pa.getIdBaseCongeAbsence())).thenReturn(null);
		
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "congesAnnuelsRepository", congesAnnuelsRepository);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);
		
		ReturnMessageDto result = service.alimentationAutoCompteur(idAgent, dateDebut, dateFin);
		
		assertEquals(result.getErrors().size(), 1);
		assertEquals(result.getErrors().get(0), String.format(CongeAnnuelCounterServiceImpl.BASE_CONGES_ALIM_AUTO_INEXISTANT, pa.getIdBaseCongeAbsence()));
	}
	
	@Test
	public void alimentationAutoCompteur_ok() {
		
		Integer idAgent = 10;
		Date dateDebut = new DateTime(2014, 2, 5, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 2, 25, 0, 0, 0).toDate();
		Date dateMonth = new Date();
		
		AgentCongeAnnuelCount acac = new AgentCongeAnnuelCount();
			acac.setIdAgent(9005138);
			acac.setIdAgentCount(idAgent);
			acac.setTotalJours(20.0);
			acac.setTotalJoursAnneeN1(10.0);
		
		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, idAgent))
				.thenReturn(acac);
		
		List<InfosAlimAutoCongesAnnuelsDto> listPA = new ArrayList<InfosAlimAutoCongesAnnuelsDto>();
		InfosAlimAutoCongesAnnuelsDto pa = new InfosAlimAutoCongesAnnuelsDto();
		pa.setDroitConges(true);
		pa.setDateDebut(new DateTime(2014, 2, 5, 0, 0, 0).toDate());
		pa.setDateFin(new DateTime(2014, 2, 25, 0, 0, 0).toDate());
		pa.setIdBaseCongeAbsence(1);
		listPA.add(pa);
		
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getListPAPourAlimAutoCongesAnnuels(9005138, dateDebut, dateFin)).thenReturn(listPA);
		
		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getFirstMondayOfCurrentMonth()).thenReturn(dateMonth);
		Mockito.when(helperService.calculNombreJours(dateDebut, dateFin)).thenReturn(20.0);
		
		CongesAnnuelsRepository congesAnnuelsRepository = Mockito.mock(CongesAnnuelsRepository.class);
		Mockito.when(congesAnnuelsRepository.getWeekHistoForAgentAndDate(idAgent, dateMonth)).thenReturn(null);
		
		RefAlimCongeAnnuel refAlimCongeAnnuel = new RefAlimCongeAnnuel();
		refAlimCongeAnnuel.setFevrier(10.0);
		
		TypeAbsenceRepository typeAbsenceRepository = Mockito.mock(TypeAbsenceRepository.class);
		Mockito.when(typeAbsenceRepository.getEntity(RefAlimCongeAnnuel.class, pa.getIdBaseCongeAbsence())).thenReturn(refAlimCongeAnnuel);
		
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "congesAnnuelsRepository", congesAnnuelsRepository);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);
		
		ReturnMessageDto result = service.alimentationAutoCompteur(idAgent, dateDebut, dateFin);
		
		assertEquals(result.getErrors().size(), 0);
	}
	
	@Test 
	public void getNombreJoursDonnantDroitsAConges_quotaUnTiers() {
		
		Integer dernierJourMois = 30;
		Double quotaMois = 10.0;
		Double nombreJoursPA = 10.0;
		
		Double result = service.getNombreJoursDonnantDroitsAConges(dernierJourMois, quotaMois, nombreJoursPA);
		
		assertEquals(result, 3,5);
	}
	
	@Test 
	public void getNombreJoursDonnantDroitsAConges_MoisEntier() {
		
		Integer dernierJourMois = 30;
		Double quotaMois = 10.0;
		Double nombreJoursPA = 31.0;
		
		Double result = service.getNombreJoursDonnantDroitsAConges(dernierJourMois, quotaMois, nombreJoursPA);
		
		assertEquals(result, 10,0);
	}
	
	@Test 
	public void getNombreJoursDonnantDroitsAConges_MoitieMois() {
		
		Integer dernierJourMois = 30;
		Double quotaMois = 10.0;
		Double nombreJoursPA = 15.0;
		
		Double result = service.getNombreJoursDonnantDroitsAConges(dernierJourMois, quotaMois, nombreJoursPA);
		
		assertEquals(result, 5,0);
	}

}
