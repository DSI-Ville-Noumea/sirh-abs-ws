package nc.noumea.mairie.abs.service.counter.impl;

import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Date;

import nc.noumea.mairie.abs.domain.AgentCongeAnnuelCount;
import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;
import nc.noumea.mairie.abs.domain.AgentReposCompCount;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.service.impl.HelperService;
import nc.noumea.mairie.ws.ISirhWSConsumer;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class CongeAnnuelCounterServiceImplTest extends AbstractCounterServiceTest {

	private CongeAnnuelCounterServiceImpl service = new CongeAnnuelCounterServiceImpl();

	private SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMdd");

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

}
