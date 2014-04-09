package nc.noumea.mairie.abs.service.counter.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;

import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;
import nc.noumea.mairie.abs.domain.AgentRecupCount;
import nc.noumea.mairie.abs.domain.AgentWeekRecup;
import nc.noumea.mairie.abs.domain.BaseAgentCount;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.MotifCompteur;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IAccessRightsRepository;
import nc.noumea.mairie.abs.repository.ICounterRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.abs.service.AgentNotFoundException;
import nc.noumea.mairie.abs.service.NotAMondayException;
import nc.noumea.mairie.abs.service.impl.HelperService;
import nc.noumea.mairie.sirh.domain.Agent;
import nc.noumea.mairie.ws.ISirhWSConsumer;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class RecupCounterServiceImplTest {

	@Test
	public void addRecuperationToAgent_AgentDoesNotExists_ThrowAgentNotFoundException() {

		// Given
		Integer idAgent = 9008765;
		Date dateMonday = new LocalDate(2013, 9, 30).toDate();

		ISirhRepository sR = Mockito.mock(ISirhRepository.class);
		Mockito.when(sR.getAgent(idAgent)).thenReturn(null);

		RecupCounterServiceImpl service = new RecupCounterServiceImpl();
		ReflectionTestUtils.setField(service, "sirhRepository", sR);

		// When
		try {
			service.addToAgentForPTG(idAgent, dateMonday, 90);
		} catch (AgentNotFoundException ex) {
			return;
		}

		fail("Should have thrown an AgentNotFoundException");
	}

	@Test
	public void addRecuperationToAgent_DateIsNotAMonday_ThrowDateIsNotAMondayException() {

		// Given
		Integer idAgent = 9008765;
		Date dateMonday = new LocalDate(2013, 9, 29).toDate();

		ISirhRepository sR = Mockito.mock(ISirhRepository.class);
		Mockito.when(sR.getAgent(idAgent)).thenReturn(new Agent());

		HelperService hS = Mockito.mock(HelperService.class);
		Mockito.when(hS.isDateAMonday(dateMonday)).thenReturn(false);

		RecupCounterServiceImpl service = new RecupCounterServiceImpl();
		ReflectionTestUtils.setField(service, "sirhRepository", sR);
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
	public void addRecuperationToAgent_AgentHasNoRecupForThatWeek_AgentHasNoAccount_CreateItemsAndAddQteToAccount() {

		// Given
		Integer idAgent = 9008765;
		Date dateMonday = new LocalDate(2013, 9, 30).toDate();

		ISirhRepository sR = Mockito.mock(ISirhRepository.class);
		Mockito.when(sR.getAgent(idAgent)).thenReturn(new Agent());

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
		Mockito.when(rr.getWeekHistoForAgentAndDate(AgentWeekRecup.class, idAgent, dateMonday)).thenReturn(null);
		Mockito.when(rr.getAgentCounter(AgentRecupCount.class, idAgent)).thenReturn(null);

		HelperService hS = Mockito.mock(HelperService.class);
		Mockito.when(hS.getCurrentDate()).thenReturn(new DateTime(2013, 4, 2, 8, 56, 12).toDate());
		Mockito.when(hS.isDateAMonday(dateMonday)).thenReturn(true);

		RecupCounterServiceImpl service = new RecupCounterServiceImpl();
		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "helperService", hS);
		ReflectionTestUtils.setField(service, "sirhRepository", sR);

		// When
		int result = service.addToAgentForPTG(idAgent, dateMonday, 90);

		// Then
		assertEquals(90, result);

		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(AgentWeekRecup.class));
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(AgentRecupCount.class));
	}

	@Test
	public void addRecuperationToAgent_AgentHasARecupForThatWeek_UpdateItemAndAddQteToAccount() {

		// Given
		Integer idAgent = 9008765;
		Date dateMonday = new LocalDate(2013, 9, 30).toDate();

		ISirhRepository sR = Mockito.mock(ISirhRepository.class);
		Mockito.when(sR.getAgent(idAgent)).thenReturn(new Agent());

		AgentWeekRecup awr = new AgentWeekRecup();
		awr.setMinutes(80);
		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
		Mockito.when(rr.getWeekHistoForAgentAndDate(AgentWeekRecup.class, idAgent, dateMonday)).thenReturn(awr);
		AgentRecupCount arc = new AgentRecupCount();
		arc.setTotalMinutes(10);
		Mockito.when(rr.getAgentCounter(AgentRecupCount.class, idAgent)).thenReturn(arc);

		HelperService hS = Mockito.mock(HelperService.class);
		Mockito.when(hS.isDateAMonday(dateMonday)).thenReturn(true);

		RecupCounterServiceImpl service = new RecupCounterServiceImpl();
		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "sirhRepository", sR);
		ReflectionTestUtils.setField(service, "helperService", hS);

		// When
		int result = service.addToAgentForPTG(idAgent, dateMonday, 90);

		// Then
		assertEquals(20, result);
		assertEquals(20, (int) arc.getTotalMinutes());
		assertEquals(90, (int) awr.getMinutes());
	}

	@Test
	public void addRecuperationToAgent_AgentHasARecupForThatWeek_NewValueIsLower_UpdateItemAndAddQteToAccount() {

		// Given
		Integer idAgent = 9008765;
		Date dateMonday = new LocalDate(2013, 9, 30).toDate();

		ISirhRepository sR = Mockito.mock(ISirhRepository.class);
		Mockito.when(sR.getAgent(idAgent)).thenReturn(new Agent());

		AgentWeekRecup awr = new AgentWeekRecup();
		awr.setMinutes(80);
		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
		Mockito.when(rr.getWeekHistoForAgentAndDate(AgentWeekRecup.class, idAgent, dateMonday)).thenReturn(awr);
		AgentRecupCount arc = new AgentRecupCount();
		arc.setTotalMinutes(10);
		Mockito.when(rr.getAgentCounter(AgentRecupCount.class, idAgent)).thenReturn(arc);

		HelperService hS = Mockito.mock(HelperService.class);
		Mockito.when(hS.isDateAMonday(dateMonday)).thenReturn(true);

		RecupCounterServiceImpl service = new RecupCounterServiceImpl();
		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "sirhRepository", sR);
		ReflectionTestUtils.setField(service, "helperService", hS);

		// When
		int result = service.addToAgentForPTG(idAgent, dateMonday, 70);

		// Then
		assertEquals(0, result);
		assertEquals(0, (int) arc.getTotalMinutes());
		assertEquals(70, (int) awr.getMinutes());
	}
	
	@Test
	public void majCompteurRecupToAgent_compteurInexistant() {
		
		ReturnMessageDto result = new ReturnMessageDto();
		Demande demande = new Demande();
			demande.setIdAgent(9008765);
		int minutes = 10;
		
		ISirhRepository sR = Mockito.mock(ISirhRepository.class);
		Mockito.when(sR.getAgent(demande.getIdAgent())).thenReturn(new Agent());
		
		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
		Mockito.when(rr.getAgentCounter(AgentRecupCount.class, demande.getIdAgent())).thenReturn(null);
		
		HelperService hS = Mockito.mock(HelperService.class);
		
		RecupCounterServiceImpl service = new RecupCounterServiceImpl();
		ReflectionTestUtils.setField(service, "sirhRepository", sR);
		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "helperService", hS);
		
		result = service.majCompteurToAgent(result, demande, minutes);
		
		assertEquals(0, result.getErrors().size());
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(BaseAgentCount.class));
	}
	
	@Test
	public void majCompteurRecupToAgent_compteurNegatif_debit() {
		
		ReturnMessageDto result = new ReturnMessageDto();
		Demande demande = new Demande();
			demande.setIdAgent(9008765);
		int minutes = -11;
		
		ISirhRepository sR = Mockito.mock(ISirhRepository.class);
		Mockito.when(sR.getAgent(demande.getIdAgent())).thenReturn(new Agent());
		
		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
		AgentRecupCount arc = new AgentRecupCount();
		arc.setTotalMinutes(10);
		Mockito.when(rr.getAgentCounter(AgentRecupCount.class, demande.getIdAgent())).thenReturn(arc);
		
		HelperService hS = Mockito.mock(HelperService.class);
		
		RecupCounterServiceImpl service = new RecupCounterServiceImpl();
		ReflectionTestUtils.setField(service, "sirhRepository", sR);
		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "helperService", hS);
		
		result = service.majCompteurToAgent(result, demande, minutes);
		
		assertEquals(1, result.getErrors().size());
		assertEquals("Le solde du compteur de l'agent ne peut pas être négatif.", result.getErrors().get(0));
		Mockito.verify(rr, Mockito.times(0)).persistEntity(Mockito.isA(BaseAgentCount.class));
	}
	
	@Test
	public void majCompteurRecupToAgent_compteurNegatif_credit() {
		
		ReturnMessageDto result = new ReturnMessageDto();
		Demande demande = new Demande();
			demande.setIdAgent(9008765);
		int minutes = 11;
		
		ISirhRepository sR = Mockito.mock(ISirhRepository.class);
		Mockito.when(sR.getAgent(demande.getIdAgent())).thenReturn(new Agent());
		
		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
		AgentRecupCount arc = new AgentRecupCount();
		arc.setTotalMinutes(-15);
		Mockito.when(rr.getAgentCounter(AgentRecupCount.class, demande.getIdAgent())).thenReturn(arc);
		
		HelperService hS = Mockito.mock(HelperService.class);
		
		RecupCounterServiceImpl service = new RecupCounterServiceImpl();
		ReflectionTestUtils.setField(service, "sirhRepository", sR);
		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "helperService", hS);
		
		result = service.majCompteurToAgent(result, demande, minutes);
		
		assertEquals(0, result.getErrors().size());
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(BaseAgentCount.class));
	}
	
	@Test
	public void majCompteurRecupToAgent_debitOk() {
		
		ReturnMessageDto result = new ReturnMessageDto();
		Demande demande = new Demande();
			demande.setIdAgent(9008765);
		int minutes = -11;
		
		ISirhRepository sR = Mockito.mock(ISirhRepository.class);
		Mockito.when(sR.getAgent(demande.getIdAgent())).thenReturn(new Agent());
		
		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
		AgentRecupCount arc = new AgentRecupCount();
		arc.setTotalMinutes(12);
		Mockito.when(rr.getAgentCounter(AgentRecupCount.class, demande.getIdAgent())).thenReturn(arc);
		
		HelperService hS = Mockito.mock(HelperService.class);
		
		RecupCounterServiceImpl service = new RecupCounterServiceImpl();
		ReflectionTestUtils.setField(service, "sirhRepository", sR);
		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "helperService", hS);
		
		result = service.majCompteurToAgent(result, demande, minutes);
		
		assertEquals(0, result.getErrors().size());
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(BaseAgentCount.class));
	}
	
	@Test
	public void majManuelleCompteurRecupToAgent_OperateurNonHabilite() {
		
		ReturnMessageDto result = new ReturnMessageDto();
			result.getErrors().add("L'agent 9005138 n'existe pas dans SIIDMA.");
		Integer idAgent = 9005138; 
		CompteurDto compteurDto = new CompteurDto();
			compteurDto.setIdAgent(9005151);
		
		ISirhWSConsumer wsMock = Mockito.mock(ISirhWSConsumer.class);
			Mockito.when(wsMock.isUtilisateurSIRH(idAgent)).thenReturn(result);
		
		HelperService helperService = Mockito.mock(HelperService.class);
			Mockito.when(helperService.calculMinutesAlimManuelleCompteur(compteurDto)).thenReturn(10.0);
		
		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
			Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(false);
		
		RecupCounterServiceImpl service = new RecupCounterServiceImpl();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);
		
		result = service.majManuelleCompteurToAgent(idAgent, compteurDto);
		
		assertEquals(1, result.getErrors().size());
		assertEquals("Vous n'êtes pas habilité à mettre à jour le compteur de cet agent.", result.getErrors().get(0).toString());
	}
	
	@Test
	public void majManuelleCompteurRecupToAgent_agentInexistant() {
		
		Integer idAgent = 9005138; 
		CompteurDto compteurDto = new CompteurDto();
			compteurDto.setIdAgent(9005151);
			compteurDto.setDureeARetrancher(10.0);
		
		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
			Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(true);
		
		HelperService helperService = Mockito.mock(HelperService.class);
			Mockito.when(helperService.calculMinutesAlimManuelleCompteur(compteurDto)).thenReturn(10.0);
			
		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
			Mockito.when(sirhRepository.getAgent(compteurDto.getIdAgent())).thenReturn(null);
		
		RecupCounterServiceImpl service = new RecupCounterServiceImpl();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		
		boolean isAgentNotFoundException = false;
		try {
			service.majManuelleCompteurToAgent(idAgent, compteurDto);
		} catch (AgentNotFoundException e) {
			isAgentNotFoundException = true;
		}
		
		assertTrue(isAgentNotFoundException);
	}
	
	@Test
	public void majManuelleCompteurRecupToAgent_CompteurInexistant_And_SoldeNegatif() {
		
		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138; 
		CompteurDto compteurDto = new CompteurDto();
			compteurDto.setIdAgent(9005151);
			compteurDto.setDureeARetrancher(10.0);
		
		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
			Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(true);
		
		HelperService helperService = Mockito.mock(HelperService.class);
			Mockito.when(helperService.calculMinutesAlimManuelleCompteur(compteurDto)).thenReturn(-10.0);
			
		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
			Mockito.when(sirhRepository.getAgent(compteurDto.getIdAgent())).thenReturn(new Agent());
		
		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
			Mockito.when(counterRepository.getAgentCounter(AgentRecupCount.class, compteurDto.getIdAgent())).thenReturn(null);
		
		RecupCounterServiceImpl service = new RecupCounterServiceImpl();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		
		result = service.majManuelleCompteurToAgent(idAgent, compteurDto);

		assertEquals(1, result.getErrors().size());
		assertEquals("Le solde du compteur de l'agent ne peut pas être négatif.", result.getErrors().get(0).toString());
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentRecupCount.class));
	}
	
	@Test
	public void majManuelleCompteurRecupToAgent_CompteurExistant_And_SoldeNegatif() {
		
		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138; 
		CompteurDto compteurDto = new CompteurDto();
			compteurDto.setIdAgent(9005151);
			compteurDto.setDureeARetrancher(10.0);
		
		AgentRecupCount arc = new AgentRecupCount();
			arc.setTotalMinutes(5);
					
		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
			Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(true);
		
		HelperService helperService = Mockito.mock(HelperService.class);
			Mockito.when(helperService.calculMinutesAlimManuelleCompteur(compteurDto)).thenReturn(-10.0);
			
		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
			Mockito.when(sirhRepository.getAgent(compteurDto.getIdAgent())).thenReturn(new Agent());
		
		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
			Mockito.when(counterRepository.getAgentCounter(AgentRecupCount.class, compteurDto.getIdAgent())).thenReturn(arc);
		
		RecupCounterServiceImpl service = new RecupCounterServiceImpl();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		
		result = service.majManuelleCompteurToAgent(idAgent, compteurDto);

		assertEquals(1, result.getErrors().size());
		assertEquals("Le solde du compteur de l'agent ne peut pas être négatif.", result.getErrors().get(0).toString());
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentRecupCount.class));
	}
	
	@Test
	public void majManuelleCompteurRecupToAgent_MotifCompteurInexistant() {
		
		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138; 
		CompteurDto compteurDto = new CompteurDto();
			compteurDto.setIdAgent(9005151);
			compteurDto.setDureeARetrancher(10.0);
			compteurDto.setIdMotifCompteur(1);
		
		AgentRecupCount arc = new AgentRecupCount();
			arc.setTotalMinutes(15);
					
		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
			Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(true);
		
		HelperService helperService = Mockito.mock(HelperService.class);
			Mockito.when(helperService.calculMinutesAlimManuelleCompteur(compteurDto)).thenReturn(-10.0);
			
		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
			Mockito.when(sirhRepository.getAgent(compteurDto.getIdAgent())).thenReturn(new Agent());
		
		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
			Mockito.when(counterRepository.getAgentCounter(AgentRecupCount.class, compteurDto.getIdAgent())).thenReturn(arc);
			Mockito.when(counterRepository.getEntity(MotifCompteur.class, compteurDto.getIdMotifCompteur())).thenReturn(null);
		
		RecupCounterServiceImpl service = new RecupCounterServiceImpl();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		
		result = service.majManuelleCompteurToAgent(idAgent, compteurDto);

		assertEquals(1, result.getErrors().size());
		assertEquals("Le motif n'existe pas.", result.getErrors().get(0).toString());
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentRecupCount.class));
	}
	
	@Test
	public void majManuelleCompteurRecupToAgent_OK_avecCompteurExistant() {
		
		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138; 
		CompteurDto compteurDto = new CompteurDto();
			compteurDto.setIdAgent(9005151);
			compteurDto.setDureeARetrancher(10.0);
			compteurDto.setIdMotifCompteur(1);
		
		AgentRecupCount arc = new AgentRecupCount();
			arc.setTotalMinutes(15);
					
		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
			Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(true);
		
		HelperService helperService = Mockito.mock(HelperService.class);
			Mockito.when(helperService.calculMinutesAlimManuelleCompteur(compteurDto)).thenReturn(-10.0);
			Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());
			
		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
			Mockito.when(sirhRepository.getAgent(compteurDto.getIdAgent())).thenReturn(new Agent());
		
		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
			Mockito.when(counterRepository.getAgentCounter(AgentRecupCount.class, compteurDto.getIdAgent())).thenReturn(arc);
			Mockito.when(counterRepository.getEntity(MotifCompteur.class, compteurDto.getIdMotifCompteur())).thenReturn(new MotifCompteur());
		
		RecupCounterServiceImpl service = new RecupCounterServiceImpl();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		
		result = service.majManuelleCompteurToAgent(idAgent, compteurDto);

		assertEquals(0, result.getErrors().size()); 
		
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentRecupCount.class));
	}
	
	@Test
	public void majManuelleCompteurRecupToAgent_OK_sansCompteurExistant() {
		
		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138; 
		CompteurDto compteurDto = new CompteurDto();
			compteurDto.setIdAgent(9005151);
			compteurDto.setDureeARetrancher(10.0);
			compteurDto.setIdMotifCompteur(1);
		
		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
			Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(true);
		
		HelperService helperService = Mockito.mock(HelperService.class);
			Mockito.when(helperService.calculMinutesAlimManuelleCompteur(compteurDto)).thenReturn(10.0);
			Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());
			
		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
			Mockito.when(sirhRepository.getAgent(compteurDto.getIdAgent())).thenReturn(new Agent());
		
		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
			Mockito.when(counterRepository.getAgentCounter(AgentRecupCount.class, compteurDto.getIdAgent())).thenReturn(null);
			Mockito.when(counterRepository.getEntity(MotifCompteur.class, compteurDto.getIdMotifCompteur())).thenReturn(new MotifCompteur());
		
		RecupCounterServiceImpl service = new RecupCounterServiceImpl();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		
		result = service.majManuelleCompteurToAgent(idAgent, compteurDto);

		assertEquals(0, result.getErrors().size()); 
		
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentRecupCount.class));
	}
	
}
