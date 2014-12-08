package nc.noumea.mairie.abs.service.counter.impl;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import nc.noumea.mairie.abs.domain.AgentCongeAnnuelCount;
import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.ICounterRepository;
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

		result = service.intitCompteurCongeAnnuel(idAgent, idAgentConcerne);

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

		result = service.intitCompteurCongeAnnuel(idAgent, idAgentConcerne);

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

}
