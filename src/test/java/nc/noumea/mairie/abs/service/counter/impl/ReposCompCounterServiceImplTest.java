package nc.noumea.mairie.abs.service.counter.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;
import nc.noumea.mairie.abs.domain.AgentRecupCount;
import nc.noumea.mairie.abs.domain.AgentReposCompCount;
import nc.noumea.mairie.abs.domain.AgentWeekReposComp;
import nc.noumea.mairie.abs.domain.DemandeReposComp;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.ICounterRepository;
import nc.noumea.mairie.abs.service.AgentNotFoundException;
import nc.noumea.mairie.abs.service.NotAMondayException;
import nc.noumea.mairie.abs.service.impl.HelperService;
import nc.noumea.mairie.ws.ISirhWSConsumer;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

public class ReposCompCounterServiceImplTest extends AbstractCounterServiceTest {

	private ReposCompCounterServiceImpl service = new ReposCompCounterServiceImpl();

	private SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMdd");

	@Test
	public void testMethodeParenteHeritage() {
		super.allTest(new ReposCompCounterServiceImpl());
	}

	@Test
	public void majManuelleCompteurRCToAgent_OK_sansCompteurExistant_AnneeN() {

		super.service = new RecupCounterServiceImpl();
		super.majManuelleCompteurToAgent_prepareData();

		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		CompteurDto compteurDto = new CompteurDto();
		compteurDto.setIdAgent(9005151);
		compteurDto.setDureeARetrancher(10.0);
		compteurDto.setIdMotifCompteur(1);
		compteurDto.setAnneePrecedente(false);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculAlimManuelleCompteur(compteurDto)).thenReturn(10.0);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());

		Mockito.when(counterRepository.getAgentCounter(AgentRecupCount.class, compteurDto.getIdAgent())).thenReturn(
				null);

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
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentReposCompCount.class));
	}

	@Test
	public void majManuelleCompteurRCToAgent_OK_sansCompteurExistant_AnneeN1() {

		super.service = new RecupCounterServiceImpl();
		super.majManuelleCompteurToAgent_prepareData();

		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		CompteurDto compteurDto = new CompteurDto();
		compteurDto.setIdAgent(9005151);
		compteurDto.setDureeARetrancher(10.0);
		compteurDto.setIdMotifCompteur(1);
		compteurDto.setAnneePrecedente(true);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculAlimManuelleCompteur(compteurDto)).thenReturn(10.0);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());

		Mockito.when(counterRepository.getAgentCounter(AgentRecupCount.class, compteurDto.getIdAgent())).thenReturn(
				null);

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
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentReposCompCount.class));
	}

	@Test
	public void majManuelleCompteurRCToAgent_MotifCompteurInexistant() {

		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		CompteurDto compteurDto = new CompteurDto();
		compteurDto.setIdAgent(9005151);
		compteurDto.setDureeARetrancher(10.0);
		compteurDto.setIdMotifCompteur(1);

		AgentRecupCount arc = new AgentRecupCount();
		arc.setTotalMinutes(15);

		Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(true);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculAlimManuelleCompteur(compteurDto)).thenReturn(10.0);

		Mockito.when(counterRepository.getAgentCounter(AgentRecupCount.class, compteurDto.getIdAgent()))
				.thenReturn(arc);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(result);
		Mockito.when(sirhWSConsumer.getAgent(compteurDto.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		result = service.majManuelleCompteurToAgent(idAgent, compteurDto);

		assertEquals(1, result.getErrors().size());
		assertEquals("Le motif n'existe pas.", result.getErrors().get(0).toString());
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentRecupCount.class));
	}

	@Test
	public void majManuelleCompteurRCToAgent_KO_compteurNegatif_AnneeN1() {

		super.service = new RecupCounterServiceImpl();
		super.majManuelleCompteurToAgent_prepareData();

		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		CompteurDto compteurDto = new CompteurDto();
		compteurDto.setIdAgent(9005151);
		compteurDto.setDureeARetrancher(10.0);
		compteurDto.setIdMotifCompteur(1);
		compteurDto.setAnneePrecedente(true);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculAlimManuelleCompteur(compteurDto)).thenReturn(-10.0);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());

		Mockito.when(counterRepository.getAgentCounter(AgentRecupCount.class, compteurDto.getIdAgent())).thenReturn(
				null);

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
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentReposCompCount.class));
	}

	@Test
	public void majManuelleCompteurRCToAgent_KO_compteurNegatif_AnneeN0() {

		super.service = new RecupCounterServiceImpl();
		super.majManuelleCompteurToAgent_prepareData();

		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		CompteurDto compteurDto = new CompteurDto();
		compteurDto.setIdAgent(9005151);
		compteurDto.setDureeARetrancher(10.0);
		compteurDto.setIdMotifCompteur(1);
		compteurDto.setAnneePrecedente(false);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculAlimManuelleCompteur(compteurDto)).thenReturn(-10.0);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());

		Mockito.when(counterRepository.getAgentCounter(AgentRecupCount.class, compteurDto.getIdAgent())).thenReturn(
				null);

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
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentReposCompCount.class));
	}

	@Test
	public void resetCompteurRCAnneePrecedente_compteurInexistant() {

		Integer idAgentReposCompCount = 1;

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getEntity(AgentReposCompCount.class, idAgentReposCompCount)).thenReturn(null);

		ReposCompCounterServiceImpl service = new ReposCompCounterServiceImpl();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		ReturnMessageDto result = service.resetCompteurRCAnneePrecedente(idAgentReposCompCount);

		assertEquals(1, result.getErrors().size());
		assertEquals("Le compteur n'existe pas.", result.getErrors().get(0));

		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentReposCompCount.class));
	}

	@Test
	public void resetCompteurRCAnneePrecedente_OK() {

		Integer idAgentReposCompCount = 1;

		AgentReposCompCount arc = new AgentReposCompCount();
		arc.setTotalMinutes(10);
		arc.setTotalMinutesAnneeN1(250);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getEntity(AgentReposCompCount.class, idAgentReposCompCount)).thenReturn(arc);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				AgentHistoAlimManuelle obj = (AgentHistoAlimManuelle) args[0];

				String textLog = "Retrait de -250 minutes sur l'année précédente.";
				assertEquals(textLog, obj.getText());

				return true;
			}
		}).when(counterRepository).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				AgentReposCompCount obj = (AgentReposCompCount) args[0];

				assertEquals(10, obj.getTotalMinutes());
				assertEquals(0, obj.getTotalMinutesAnneeN1());

				return true;
			}
		}).when(counterRepository).persistEntity(Mockito.isA(AgentReposCompCount.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new DateTime(2013, 4, 2, 8, 56, 12).toDate());

		ReposCompCounterServiceImpl service = new ReposCompCounterServiceImpl();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);

		ReturnMessageDto result = service.resetCompteurRCAnneePrecedente(idAgentReposCompCount);

		assertEquals(0, result.getErrors().size());

		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentReposCompCount.class));
	}

	@Test
	public void resetCompteurRCAnneePrecedente_OKBis() {

		Integer idAgentReposCompCount = 1;

		AgentReposCompCount arc = new AgentReposCompCount();
		arc.setTotalMinutes(10);
		arc.setTotalMinutesAnneeN1(150);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getEntity(AgentReposCompCount.class, idAgentReposCompCount)).thenReturn(arc);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				AgentHistoAlimManuelle obj = (AgentHistoAlimManuelle) args[0];

				String textLog = "Retrait de -150 minutes sur l'année précédente.";
				assertEquals(textLog, obj.getText());

				return true;
			}
		}).when(counterRepository).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				AgentReposCompCount obj = (AgentReposCompCount) args[0];

				assertEquals(10, obj.getTotalMinutes());
				assertEquals(0, obj.getTotalMinutesAnneeN1());

				return true;
			}
		}).when(counterRepository).persistEntity(Mockito.isA(AgentReposCompCount.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new DateTime(2013, 4, 2, 8, 56, 12).toDate());

		ReposCompCounterServiceImpl service = new ReposCompCounterServiceImpl();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);

		ReturnMessageDto result = service.resetCompteurRCAnneePrecedente(idAgentReposCompCount);

		assertEquals(0, result.getErrors().size());

		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentReposCompCount.class));
	}

	@Test
	public void resetCompteurRCAnneenCours_compteurInexistant() {

		Integer idAgentReposCompCount = 1;

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getEntity(AgentReposCompCount.class, idAgentReposCompCount)).thenReturn(null);

		ReposCompCounterServiceImpl service = new ReposCompCounterServiceImpl();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		ReturnMessageDto result = service.resetCompteurRCAnneenCours(idAgentReposCompCount);

		assertEquals(1, result.getErrors().size());
		assertEquals("Le compteur n'existe pas.", result.getErrors().get(0));

		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentReposCompCount.class));
	}

	@Test
	public void resetCompteurRCAnneenCours_OK() {

		Integer idAgentReposCompCount = 1;

		AgentReposCompCount arc = new AgentReposCompCount();
		arc.setTotalMinutes(20);
		arc.setTotalMinutesAnneeN1(10);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getEntity(AgentReposCompCount.class, idAgentReposCompCount)).thenReturn(arc);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				AgentHistoAlimManuelle obj = (AgentHistoAlimManuelle) args[0];

				String textLog = "Retrait de -20 minutes sur l'année.";
				assertEquals(textLog, obj.getText());

				return true;
			}
		}).when(counterRepository).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				AgentReposCompCount obj = (AgentReposCompCount) args[0];

				assertEquals(0, obj.getTotalMinutes());
				assertEquals(30, obj.getTotalMinutesAnneeN1());

				return true;
			}
		}).when(counterRepository).persistEntity(Mockito.isA(AgentReposCompCount.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new DateTime(2013, 4, 2, 8, 56, 12).toDate());

		ReposCompCounterServiceImpl service = new ReposCompCounterServiceImpl();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);

		ReturnMessageDto result = service.resetCompteurRCAnneenCours(idAgentReposCompCount);

		assertEquals(0, result.getErrors().size());

		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentReposCompCount.class));
	}

	@Test
	public void calculMinutesCompteur_badEtat() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());

		DemandeReposComp demande = new DemandeReposComp();
		demande.setDuree(10);
		demande.setDureeAnneeN1(20);

		ReposCompCounterServiceImpl service = new ReposCompCounterServiceImpl();

		int result = service.calculMinutesCompteur(demandeEtatChangeDto, demande);

		assertEquals(0, result);
	}

	@Test
	public void calculMinutesCompteur_debit() {
		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());

		DemandeReposComp demande = new DemandeReposComp();
		demande.setDuree(10);
		demande.setDureeAnneeN1(20);

		ReposCompCounterServiceImpl service = new ReposCompCounterServiceImpl();

		int result = service.calculMinutesCompteur(demandeEtatChangeDto, demande);

		assertEquals(-10, result);
	}

	@Test
	public void calculMinutesCompteur_credit_EtatRefusee() {
		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.REFUSEE.getCodeEtat());

		EtatDemande e = new EtatDemande();
		e.setEtat(RefEtatEnum.APPROUVEE);
		List<EtatDemande> etatsDemande = new ArrayList<>();
		etatsDemande.add(e);

		DemandeReposComp demande = new DemandeReposComp();
		demande.setDuree(10);
		demande.setDureeAnneeN1(20);
		demande.setEtatsDemande(etatsDemande);

		ReposCompCounterServiceImpl service = new ReposCompCounterServiceImpl();

		int result = service.calculMinutesCompteur(demandeEtatChangeDto, demande);

		assertEquals(30, result);
	}

	@Test
	public void calculMinutesCompteur_credit_EtatAnnulee() {
		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());

		EtatDemande e = new EtatDemande();
		e.setEtat(RefEtatEnum.APPROUVEE);
		List<EtatDemande> etatsDemande = new ArrayList<>();
		etatsDemande.add(e);

		DemandeReposComp demande = new DemandeReposComp();
		demande.setDuree(10);
		demande.setDureeAnneeN1(20);
		demande.setEtatsDemande(etatsDemande);

		ReposCompCounterServiceImpl service = new ReposCompCounterServiceImpl();

		int result = service.calculMinutesCompteur(demandeEtatChangeDto, demande);

		assertEquals(30, result);
	}

	@Test
	public void calculMinutesCompteur_credit_MauvaisEtat() {
		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat());

		EtatDemande e = new EtatDemande();
		e.setEtat(RefEtatEnum.APPROUVEE);
		List<EtatDemande> etatsDemande = new ArrayList<>();
		etatsDemande.add(e);

		DemandeReposComp demande = new DemandeReposComp();
		demande.setDuree(10);
		demande.setDureeAnneeN1(20);
		demande.setEtatsDemande(etatsDemande);

		ReposCompCounterServiceImpl service = new ReposCompCounterServiceImpl();

		int result = service.calculMinutesCompteur(demandeEtatChangeDto, demande);

		assertEquals(0, result);
	}

	@Test
	public void majCompteurToAgent_AgentNotFound() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());

		ReturnMessageDto result = new ReturnMessageDto();
		DemandeReposComp demande = new DemandeReposComp();
		demande.setIdAgent(9008765);
		demande.setDuree(10);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(demande.getIdAgent())).thenReturn(null);

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
		Mockito.when(rr.getAgentCounter(AgentReposCompCount.class, demande.getIdAgent())).thenReturn(null);

		ReposCompCounterServiceImpl service = new ReposCompCounterServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "counterRepository", rr);

		boolean isException = false;
		try {
			service.majCompteurToAgent(result, demande, demandeEtatChangeDto);
		} catch (AgentNotFoundException e) {
			isException = true;
		}

		assertTrue(isException);
		Mockito.verify(rr, Mockito.times(0)).persistEntity(Mockito.isA(AgentReposCompCount.class));
		Mockito.verify(rr, Mockito.times(0)).persistEntity(Mockito.isA(DemandeReposComp.class));
	}

	@Test
	public void majCompteurToAgent_compteurInexistant() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.REFUSEE.getCodeEtat());

		EtatDemande e = new EtatDemande();
		e.setEtat(RefEtatEnum.APPROUVEE);
		List<EtatDemande> etatsDemande = new ArrayList<EtatDemande>();
		etatsDemande.add(e);

		ReturnMessageDto result = new ReturnMessageDto();
		DemandeReposComp demande = new DemandeReposComp();
		demande.setIdAgent(9008765);
		demande.setDuree(10);
		demande.setDureeAnneeN1(0);
		demande.setEtatsDemande(etatsDemande);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(demande.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
		Mockito.when(rr.getAgentCounter(AgentReposCompCount.class, demande.getIdAgent())).thenReturn(null);

		HelperService hS = Mockito.mock(HelperService.class);

		ReposCompCounterServiceImpl service = new ReposCompCounterServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "helperService", hS);

		result = service.majCompteurToAgent(result, demande, demandeEtatChangeDto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(AgentReposCompCount.class));
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(DemandeReposComp.class));
	}

	@Test
	public void majCompteurToAgent_compteurNegatif_debit() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());

		ReturnMessageDto result = new ReturnMessageDto();

		DemandeReposComp demande = new DemandeReposComp();
		demande.setIdAgent(9008765);
		demande.setDuree(11);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(demande.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
		AgentReposCompCount arc = new AgentReposCompCount();
		arc.setTotalMinutes(8);
		arc.setTotalMinutesAnneeN1(2);
		Mockito.when(rr.getAgentCounter(AgentReposCompCount.class, demande.getIdAgent())).thenReturn(arc);

		HelperService hS = Mockito.mock(HelperService.class);

		ReposCompCounterServiceImpl service = new ReposCompCounterServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "helperService", hS);

		result = service.majCompteurToAgent(result, demande, demandeEtatChangeDto);

		assertEquals(1, result.getErrors().size());
		assertEquals("Le solde du compteur de l'agent ne peut pas être négatif.", result.getErrors().get(0));
		Mockito.verify(rr, Mockito.times(0)).persistEntity(Mockito.isA(AgentReposCompCount.class));
		Mockito.verify(rr, Mockito.times(0)).persistEntity(Mockito.isA(DemandeReposComp.class));
	}

	@Test
	public void majCompteurToAgent_compteurNegatif_credit() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.REFUSEE.getCodeEtat());

		ReturnMessageDto result = new ReturnMessageDto();

		EtatDemande e = new EtatDemande();
		e.setEtat(RefEtatEnum.APPROUVEE);
		List<EtatDemande> etatsDemande = new ArrayList<EtatDemande>();
		etatsDemande.add(e);

		DemandeReposComp demande = new DemandeReposComp();
		demande.setIdAgent(9008765);
		demande.setDuree(8);
		demande.setDureeAnneeN1(9);
		demande.setEtatsDemande(etatsDemande);

		AgentReposCompCount arc = new AgentReposCompCount();
		arc.setTotalMinutes(11);
		arc.setTotalMinutesAnneeN1(0);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(demande.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);

		Mockito.when(rr.getAgentCounter(AgentReposCompCount.class, demande.getIdAgent())).thenReturn(arc);

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeReposComp obj = (DemandeReposComp) args[0];

				assertEquals(17, obj.getDuree().intValue());
				assertEquals(0, obj.getDureeAnneeN1().intValue());

				return true;
			}
		}).when(rr).persistEntity(Mockito.isA(DemandeReposComp.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				AgentReposCompCount obj = (AgentReposCompCount) args[0];

				assertEquals(19, obj.getTotalMinutes());
				assertEquals(9, obj.getTotalMinutesAnneeN1());

				return true;
			}
		}).when(rr).persistEntity(Mockito.isA(AgentReposCompCount.class));

		HelperService hS = Mockito.mock(HelperService.class);

		ReposCompCounterServiceImpl service = new ReposCompCounterServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "helperService", hS);

		result = service.majCompteurToAgent(result, demande, demandeEtatChangeDto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(AgentReposCompCount.class));
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(DemandeReposComp.class));
	}

	@Test
	public void majCompteurToAgent_compteurNegatif_credit_2eCas() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());

		ReturnMessageDto result = new ReturnMessageDto();

		EtatDemande e = new EtatDemande();
		e.setEtat(RefEtatEnum.APPROUVEE);
		List<EtatDemande> etatsDemande = new ArrayList<EtatDemande>();
		etatsDemande.add(e);

		DemandeReposComp demande = new DemandeReposComp();
		demande.setIdAgent(9008765);
		demande.setDuree(0);
		demande.setDureeAnneeN1(17);
		demande.setEtatsDemande(etatsDemande);

		AgentReposCompCount arc = new AgentReposCompCount();
		arc.setTotalMinutes(11);
		arc.setTotalMinutesAnneeN1(0);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(demande.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);

		Mockito.when(rr.getAgentCounter(AgentReposCompCount.class, demande.getIdAgent())).thenReturn(arc);

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeReposComp obj = (DemandeReposComp) args[0];

				assertEquals(17, obj.getDuree().intValue());
				assertEquals(0, obj.getDureeAnneeN1().intValue());

				return true;
			}
		}).when(rr).persistEntity(Mockito.isA(DemandeReposComp.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				AgentReposCompCount obj = (AgentReposCompCount) args[0];

				assertEquals(11, obj.getTotalMinutes());
				assertEquals(17, obj.getTotalMinutesAnneeN1());

				return true;
			}
		}).when(rr).persistEntity(Mockito.isA(AgentReposCompCount.class));

		HelperService hS = Mockito.mock(HelperService.class);

		ReposCompCounterServiceImpl service = new ReposCompCounterServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "helperService", hS);

		result = service.majCompteurToAgent(result, demande, demandeEtatChangeDto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(AgentReposCompCount.class));
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(DemandeReposComp.class));
	}

	@Test
	public void majCompteurToAgent_compteurNegatif_credit_3eCas() {

		ReturnMessageDto result = new ReturnMessageDto();

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.REFUSEE.getCodeEtat());

		EtatDemande e = new EtatDemande();
		e.setEtat(RefEtatEnum.APPROUVEE);
		List<EtatDemande> etatsDemande = new ArrayList<EtatDemande>();
		etatsDemande.add(e);

		DemandeReposComp demande = new DemandeReposComp();
		demande.setIdAgent(9008765);
		demande.setDuree(17);
		demande.setDureeAnneeN1(0);
		demande.setEtatsDemande(etatsDemande);

		AgentReposCompCount arc = new AgentReposCompCount();
		arc.setTotalMinutes(11);
		arc.setTotalMinutesAnneeN1(0);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(demande.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);

		Mockito.when(rr.getAgentCounter(AgentReposCompCount.class, demande.getIdAgent())).thenReturn(arc);

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeReposComp obj = (DemandeReposComp) args[0];

				assertEquals(17, obj.getDuree().intValue());
				assertEquals(0, obj.getDureeAnneeN1().intValue());

				return true;
			}
		}).when(rr).persistEntity(Mockito.isA(DemandeReposComp.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				AgentReposCompCount obj = (AgentReposCompCount) args[0];

				assertEquals(28, obj.getTotalMinutes());
				assertEquals(0, obj.getTotalMinutesAnneeN1());

				return true;
			}
		}).when(rr).persistEntity(Mockito.isA(AgentReposCompCount.class));

		HelperService hS = Mockito.mock(HelperService.class);

		ReposCompCounterServiceImpl service = new ReposCompCounterServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "helperService", hS);

		result = service.majCompteurToAgent(result, demande, demandeEtatChangeDto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(AgentReposCompCount.class));
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(DemandeReposComp.class));
	}

	@Test
	public void majCompteurToAgent_debitOk() {

		ReturnMessageDto result = new ReturnMessageDto();

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());

		DemandeReposComp demande = new DemandeReposComp();
		demande.setIdAgent(9008765);
		demande.setDuree(11);

		AgentReposCompCount arc = new AgentReposCompCount();
		arc.setTotalMinutes(12);
		arc.setTotalMinutesAnneeN1(5);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(demande.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
		Mockito.when(rr.getAgentCounter(AgentReposCompCount.class, demande.getIdAgent())).thenReturn(arc);

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeReposComp obj = (DemandeReposComp) args[0];

				assertEquals(6, obj.getDuree().intValue());
				assertEquals(5, obj.getDureeAnneeN1().intValue());

				return true;
			}
		}).when(rr).persistEntity(Mockito.isA(DemandeReposComp.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				AgentReposCompCount obj = (AgentReposCompCount) args[0];

				assertEquals(6, obj.getTotalMinutes());
				assertEquals(0, obj.getTotalMinutesAnneeN1());

				return true;
			}
		}).when(rr).persistEntity(Mockito.isA(AgentReposCompCount.class));

		HelperService hS = Mockito.mock(HelperService.class);

		ReposCompCounterServiceImpl service = new ReposCompCounterServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "helperService", hS);

		result = service.majCompteurToAgent(result, demande, demandeEtatChangeDto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(AgentReposCompCount.class));
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(DemandeReposComp.class));
	}

	@Test
	public void majCompteurToAgent_debitOk_2eCas() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());

		ReturnMessageDto result = new ReturnMessageDto();

		DemandeReposComp demande = new DemandeReposComp();
		demande.setIdAgent(9008765);
		demande.setDuree(11);

		AgentReposCompCount arc = new AgentReposCompCount();
		arc.setTotalMinutes(12);
		arc.setTotalMinutesAnneeN1(12);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(demande.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
		Mockito.when(rr.getAgentCounter(AgentReposCompCount.class, demande.getIdAgent())).thenReturn(arc);

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeReposComp obj = (DemandeReposComp) args[0];

				assertEquals(0, obj.getDuree().intValue());
				assertEquals(11, obj.getDureeAnneeN1().intValue());

				return true;
			}
		}).when(rr).persistEntity(Mockito.isA(DemandeReposComp.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				AgentReposCompCount obj = (AgentReposCompCount) args[0];

				assertEquals(12, obj.getTotalMinutes());
				assertEquals(1, obj.getTotalMinutesAnneeN1());

				return true;
			}
		}).when(rr).persistEntity(Mockito.isA(AgentReposCompCount.class));

		HelperService hS = Mockito.mock(HelperService.class);

		ReposCompCounterServiceImpl service = new ReposCompCounterServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "helperService", hS);

		result = service.majCompteurToAgent(result, demande, demandeEtatChangeDto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(AgentReposCompCount.class));
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(DemandeReposComp.class));
	}

	@Test
	public void addToAgentForPTG_AgentDoesNotExists_ThrowAgentNotFoundException() {

		// Given
		Integer idAgent = 9008765;
		Date dateMonday = new LocalDate(2013, 9, 30).toDate();

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(idAgent)).thenReturn(null);

		ReposCompCounterServiceImpl service = new ReposCompCounterServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		// When
		try {
			service.addToAgentForPTG(idAgent, dateMonday, 90);
		} catch (AgentNotFoundException ex) {
			return;
		}

		fail("Should have thrown an AgentNotFoundException");
	}

	@Test
	public void addToAgentForPTG_DateIsNotAMonday_ThrowDateIsNotAMondayException() {

		// Given
		Integer idAgent = 9008765;
		Date dateMonday = new LocalDate(2013, 9, 29).toDate();

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(idAgent)).thenReturn(new AgentGeneriqueDto());

		HelperService hS = Mockito.mock(HelperService.class);
		Mockito.when(hS.isDateAMonday(dateMonday)).thenReturn(false);

		ReposCompCounterServiceImpl service = new ReposCompCounterServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", hS);

		// When
		try {
			service.addToAgentForPTG(idAgent, dateMonday, 90);
		} catch (NotAMondayException ex) {
			return;
		}

		fail("Should have thrown an NotAMondayException");
	}

	@Test
	public void addToAgentForPTG_CounterNotFount_createCounterOK_AnneePrcdt() {

		// Given
		Integer idAgent = 9008765;

		GregorianCalendar calStr1 = new GregorianCalendar();
		calStr1.setTime(new Date());
		calStr1.add(GregorianCalendar.YEAR, -1);
		Date dateMonday = calStr1.getTime();

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(idAgent)).thenReturn(new AgentGeneriqueDto());

		HelperService hS = Mockito.mock(HelperService.class);
		Mockito.when(hS.isDateAMonday(dateMonday)).thenReturn(true);
		Mockito.when(hS.getCurrentDate()).thenReturn(new DateTime(2013, 4, 2, 8, 56, 12).toDate());

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getWeekHistoForAgentAndDate(AgentWeekReposComp.class, idAgent, dateMonday))
				.thenReturn(null);
		Mockito.when(counterRepository.getAgentCounter(AgentReposCompCount.class, idAgent)).thenReturn(null);

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				AgentWeekReposComp obj = (AgentWeekReposComp) args[0];

				GregorianCalendar calStr1 = new GregorianCalendar();
				calStr1.setTime(new Date());
				calStr1.add(GregorianCalendar.YEAR, -1);

				assertEquals(90, obj.getMinutes());
				assertEquals(sdf.format(calStr1.getTime()), sdf.format(obj.getDateMonday()));

				return true;
			}
		}).when(counterRepository).persistEntity(Mockito.isA(AgentWeekReposComp.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				AgentReposCompCount obj = (AgentReposCompCount) args[0];

				assertEquals(0, obj.getTotalMinutes());
				assertEquals(90, obj.getTotalMinutesAnneeN1());

				return true;
			}
		}).when(counterRepository).persistEntity(Mockito.isA(AgentReposCompCount.class));

		ReposCompCounterServiceImpl service = new ReposCompCounterServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", hS);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		// When
		int result = service.addToAgentForPTG(idAgent, dateMonday, 90);

		// Then
		assertEquals(0, result);

		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentWeekReposComp.class));
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentReposCompCount.class));
	}

	@Test
	public void addToAgentForPTG_CounterNotFount_createCounterOK_AnneeEnCours() {

		// Given
		Integer idAgent = 9008765;
		Date dateMonday = new Date();

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(idAgent)).thenReturn(new AgentGeneriqueDto());

		HelperService hS = Mockito.mock(HelperService.class);
		Mockito.when(hS.isDateAMonday(dateMonday)).thenReturn(true);
		Mockito.when(hS.getCurrentDate()).thenReturn(new DateTime(2013, 4, 2, 8, 56, 12).toDate());

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getWeekHistoForAgentAndDate(AgentWeekReposComp.class, idAgent, dateMonday))
				.thenReturn(null);
		Mockito.when(counterRepository.getAgentCounter(AgentReposCompCount.class, idAgent)).thenReturn(null);

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				AgentWeekReposComp obj = (AgentWeekReposComp) args[0];

				assertEquals(90, obj.getMinutes());
				assertEquals(sdf.format(new Date()), sdf.format(obj.getDateMonday()));

				return true;
			}
		}).when(counterRepository).persistEntity(Mockito.isA(AgentWeekReposComp.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				AgentReposCompCount obj = (AgentReposCompCount) args[0];

				assertEquals(90, obj.getTotalMinutes());
				assertEquals(0, obj.getTotalMinutesAnneeN1());

				return true;
			}
		}).when(counterRepository).persistEntity(Mockito.isA(AgentReposCompCount.class));

		ReposCompCounterServiceImpl service = new ReposCompCounterServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", hS);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		// When
		int result = service.addToAgentForPTG(idAgent, dateMonday, 90);

		// Then
		assertEquals(90, result);

		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentWeekReposComp.class));
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentReposCompCount.class));
	}

	@Test
	public void addToAgentForPTG_OK_AnneePrcdtCredite() {

		// Given
		Integer idAgent = 9008765;
		GregorianCalendar calStr1 = new GregorianCalendar();
		calStr1.setTime(new Date());
		calStr1.add(GregorianCalendar.YEAR, -1);
		Date dateMonday = calStr1.getTime();

		AgentWeekReposComp awrc = new AgentWeekReposComp();
		awrc.setDateMonday(dateMonday);
		awrc.setMinutes(70);

		AgentReposCompCount arcc = new AgentReposCompCount();
		arcc.setTotalMinutes(50);
		arcc.setTotalMinutesAnneeN1(50);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(idAgent)).thenReturn(new AgentGeneriqueDto());

		HelperService hS = Mockito.mock(HelperService.class);
		Mockito.when(hS.isDateAMonday(dateMonday)).thenReturn(true);
		Mockito.when(hS.getCurrentDate()).thenReturn(new DateTime(2013, 4, 2, 8, 56, 12).toDate());

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getWeekHistoForAgentAndDate(AgentWeekReposComp.class, idAgent, dateMonday))
				.thenReturn(awrc);
		Mockito.when(counterRepository.getAgentCounter(AgentReposCompCount.class, idAgent)).thenReturn(arcc);

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				AgentWeekReposComp obj = (AgentWeekReposComp) args[0];

				GregorianCalendar calStr1 = new GregorianCalendar();
				calStr1.setTime(new Date());
				calStr1.add(GregorianCalendar.YEAR, -1);

				assertEquals(90, obj.getMinutes());
				assertEquals(sdf.format(calStr1.getTime()), sdf.format(obj.getDateMonday()));

				return true;
			}
		}).when(counterRepository).persistEntity(Mockito.isA(AgentWeekReposComp.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				AgentReposCompCount obj = (AgentReposCompCount) args[0];

				assertEquals(50, obj.getTotalMinutes());
				assertEquals(70, obj.getTotalMinutesAnneeN1());

				return true;
			}
		}).when(counterRepository).persistEntity(Mockito.isA(AgentReposCompCount.class));

		ReposCompCounterServiceImpl service = new ReposCompCounterServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", hS);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		// When
		service.addToAgentForPTG(idAgent, dateMonday, 90);

		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentWeekReposComp.class));
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentReposCompCount.class));
	}

	@Test
	public void addToAgentForPTG_OK_AnneePrcdtDebitee() {

		// Given
		Integer idAgent = 9008765;
		GregorianCalendar calStr1 = new GregorianCalendar();
		calStr1.setTime(new Date());
		calStr1.add(GregorianCalendar.YEAR, -1);
		Date dateMonday = calStr1.getTime();

		AgentWeekReposComp awrc = new AgentWeekReposComp();
		awrc.setDateMonday(dateMonday);
		awrc.setMinutes(70);

		AgentReposCompCount arcc = new AgentReposCompCount();
		arcc.setTotalMinutes(50);
		arcc.setTotalMinutesAnneeN1(50);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(idAgent)).thenReturn(new AgentGeneriqueDto());

		HelperService hS = Mockito.mock(HelperService.class);
		Mockito.when(hS.isDateAMonday(dateMonday)).thenReturn(true);
		Mockito.when(hS.getCurrentDate()).thenReturn(new DateTime(2013, 4, 2, 8, 56, 12).toDate());

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getWeekHistoForAgentAndDate(AgentWeekReposComp.class, idAgent, dateMonday))
				.thenReturn(awrc);
		Mockito.when(counterRepository.getAgentCounter(AgentReposCompCount.class, idAgent)).thenReturn(arcc);

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				AgentWeekReposComp obj = (AgentWeekReposComp) args[0];

				GregorianCalendar calStr1 = new GregorianCalendar();
				calStr1.setTime(new Date());
				calStr1.add(GregorianCalendar.YEAR, -1);

				assertEquals(20, obj.getMinutes());
				assertEquals(sdf.format(calStr1.getTime()), sdf.format(obj.getDateMonday()));

				return true;
			}
		}).when(counterRepository).persistEntity(Mockito.isA(AgentWeekReposComp.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				AgentReposCompCount obj = (AgentReposCompCount) args[0];

				assertEquals(50, obj.getTotalMinutes());
				assertEquals(0, obj.getTotalMinutesAnneeN1());

				return true;
			}
		}).when(counterRepository).persistEntity(Mockito.isA(AgentReposCompCount.class));

		ReposCompCounterServiceImpl service = new ReposCompCounterServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", hS);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		// When
		service.addToAgentForPTG(idAgent, dateMonday, 20);

		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentWeekReposComp.class));
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentReposCompCount.class));
	}

	@Test
	public void addToAgentForPTG_OK_AnneePrcdtDebitee_ET_AnneeEnCoursDebitee() {

		// Given
		Integer idAgent = 9008765;
		GregorianCalendar calStr1 = new GregorianCalendar();
		calStr1.setTime(new Date());
		calStr1.add(GregorianCalendar.YEAR, -1);
		Date dateMonday = calStr1.getTime();

		AgentWeekReposComp awrc = new AgentWeekReposComp();
		awrc.setDateMonday(dateMonday);
		awrc.setMinutes(70);

		AgentReposCompCount arcc = new AgentReposCompCount();
		arcc.setTotalMinutes(50);
		arcc.setTotalMinutesAnneeN1(50);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(idAgent)).thenReturn(new AgentGeneriqueDto());

		HelperService hS = Mockito.mock(HelperService.class);
		Mockito.when(hS.isDateAMonday(dateMonday)).thenReturn(true);
		Mockito.when(hS.getCurrentDate()).thenReturn(new DateTime(2013, 4, 2, 8, 56, 12).toDate());

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getWeekHistoForAgentAndDate(AgentWeekReposComp.class, idAgent, dateMonday))
				.thenReturn(awrc);
		Mockito.when(counterRepository.getAgentCounter(AgentReposCompCount.class, idAgent)).thenReturn(arcc);

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				AgentWeekReposComp obj = (AgentWeekReposComp) args[0];

				GregorianCalendar calStr1 = new GregorianCalendar();
				calStr1.setTime(new Date());
				calStr1.add(GregorianCalendar.YEAR, -1);

				assertEquals(0, obj.getMinutes());
				assertEquals(sdf.format(calStr1.getTime()), sdf.format(obj.getDateMonday()));

				return true;
			}
		}).when(counterRepository).persistEntity(Mockito.isA(AgentWeekReposComp.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				AgentReposCompCount obj = (AgentReposCompCount) args[0];

				assertEquals(30, obj.getTotalMinutes());
				assertEquals(0, obj.getTotalMinutesAnneeN1());

				return true;
			}
		}).when(counterRepository).persistEntity(Mockito.isA(AgentReposCompCount.class));

		ReposCompCounterServiceImpl service = new ReposCompCounterServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", hS);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		// When
		service.addToAgentForPTG(idAgent, dateMonday, 0);

		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentWeekReposComp.class));
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentReposCompCount.class));
	}

	@Test
	public void addToAgentForPTG_OK_AnneeEnCoursDebitee() {

		// Given
		Integer idAgent = 9008765;
		Date dateMonday = new Date();

		AgentWeekReposComp awrc = new AgentWeekReposComp();
		awrc.setDateMonday(dateMonday);
		awrc.setMinutes(70);

		AgentReposCompCount arcc = new AgentReposCompCount();
		arcc.setTotalMinutes(50);
		arcc.setTotalMinutesAnneeN1(0);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(idAgent)).thenReturn(new AgentGeneriqueDto());

		HelperService hS = Mockito.mock(HelperService.class);
		Mockito.when(hS.isDateAMonday(dateMonday)).thenReturn(true);
		Mockito.when(hS.getCurrentDate()).thenReturn(new DateTime(2013, 4, 2, 8, 56, 12).toDate());

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getWeekHistoForAgentAndDate(AgentWeekReposComp.class, idAgent, dateMonday))
				.thenReturn(awrc);
		Mockito.when(counterRepository.getAgentCounter(AgentReposCompCount.class, idAgent)).thenReturn(arcc);

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				AgentWeekReposComp obj = (AgentWeekReposComp) args[0];

				assertEquals(0, obj.getMinutes());
				assertEquals(sdf.format(new Date()), sdf.format(obj.getDateMonday()));

				return true;
			}
		}).when(counterRepository).persistEntity(Mockito.isA(AgentWeekReposComp.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				AgentReposCompCount obj = (AgentReposCompCount) args[0];

				assertEquals(-20, obj.getTotalMinutes());
				assertEquals(0, obj.getTotalMinutesAnneeN1());

				return true;
			}
		}).when(counterRepository).persistEntity(Mockito.isA(AgentReposCompCount.class));

		ReposCompCounterServiceImpl service = new ReposCompCounterServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", hS);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		// When
		service.addToAgentForPTG(idAgent, dateMonday, 0);

		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentWeekReposComp.class));
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentReposCompCount.class));
	}

	@Test
	public void addToAgentForPTG_OK_AnneeEnCoursCreditee() {

		// Given
		Integer idAgent = 9008765;
		Date dateMonday = new Date();

		AgentWeekReposComp awrc = new AgentWeekReposComp();
		awrc.setDateMonday(dateMonday);
		awrc.setMinutes(70);

		AgentReposCompCount arcc = new AgentReposCompCount();
		arcc.setTotalMinutes(50);
		arcc.setTotalMinutesAnneeN1(0);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(idAgent)).thenReturn(new AgentGeneriqueDto());

		HelperService hS = Mockito.mock(HelperService.class);
		Mockito.when(hS.isDateAMonday(dateMonday)).thenReturn(true);
		Mockito.when(hS.getCurrentDate()).thenReturn(new DateTime(2013, 4, 2, 8, 56, 12).toDate());

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getWeekHistoForAgentAndDate(AgentWeekReposComp.class, idAgent, dateMonday))
				.thenReturn(awrc);
		Mockito.when(counterRepository.getAgentCounter(AgentReposCompCount.class, idAgent)).thenReturn(arcc);

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				AgentWeekReposComp obj = (AgentWeekReposComp) args[0];

				assertEquals(100, obj.getMinutes());
				assertEquals(sdf.format(new Date()), sdf.format(obj.getDateMonday()));

				return true;
			}
		}).when(counterRepository).persistEntity(Mockito.isA(AgentWeekReposComp.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				AgentReposCompCount obj = (AgentReposCompCount) args[0];

				assertEquals(80, obj.getTotalMinutes());
				assertEquals(0, obj.getTotalMinutesAnneeN1());

				return true;
			}
		}).when(counterRepository).persistEntity(Mockito.isA(AgentReposCompCount.class));

		ReposCompCounterServiceImpl service = new ReposCompCounterServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", hS);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		// When
		service.addToAgentForPTG(idAgent, dateMonday, 100);

		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentWeekReposComp.class));
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentReposCompCount.class));
	}

}
