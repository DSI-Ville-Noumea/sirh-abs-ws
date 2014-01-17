package nc.noumea.mairie.abs.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;

import nc.noumea.mairie.abs.domain.AgentRecupCount;
import nc.noumea.mairie.abs.domain.AgentWeekAlimManuelle;
import nc.noumea.mairie.abs.domain.AgentWeekRecup;
import nc.noumea.mairie.abs.domain.BaseAgentCount;
import nc.noumea.mairie.abs.domain.MotifCompteur;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IAccessRightsRepository;
import nc.noumea.mairie.abs.repository.ICounterRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.abs.service.AgentNotFoundException;
import nc.noumea.mairie.abs.service.NotAMondayException;
import nc.noumea.mairie.sirh.domain.Agent;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class CounterServiceTest {

	@Test
	public void addRecuperationToAgent_AgentDoesNotExists_ThrowAgentNotFoundException() {

		// Given
		Integer idAgent = 9008765;
		Date dateMonday = new LocalDate(2013, 9, 30).toDate();

		ISirhRepository sR = Mockito.mock(ISirhRepository.class);
		Mockito.when(sR.getAgent(idAgent)).thenReturn(null);

		CounterService service = new CounterService();
		ReflectionTestUtils.setField(service, "sirhRepository", sR);

		// When
		try {
			service.addRecuperationToAgentForPTG(idAgent, dateMonday, 90);
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

		CounterService service = new CounterService();
		ReflectionTestUtils.setField(service, "sirhRepository", sR);
		ReflectionTestUtils.setField(service, "helperService", hS);

		// When
		try {
			service.addRecuperationToAgentForPTG(idAgent, dateMonday, 90);
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

		CounterService service = new CounterService();
		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "helperService", hS);
		ReflectionTestUtils.setField(service, "sirhRepository", sR);

		// When
		int result = service.addRecuperationToAgentForPTG(idAgent, dateMonday, 90);

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

		CounterService service = new CounterService();
		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "sirhRepository", sR);
		ReflectionTestUtils.setField(service, "helperService", hS);

		// When
		int result = service.addRecuperationToAgentForPTG(idAgent, dateMonday, 90);

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

		CounterService service = new CounterService();
		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "sirhRepository", sR);
		ReflectionTestUtils.setField(service, "helperService", hS);

		// When
		int result = service.addRecuperationToAgentForPTG(idAgent, dateMonday, 70);

		// Then
		assertEquals(0, result);
		assertEquals(0, (int) arc.getTotalMinutes());
		assertEquals(70, (int) awr.getMinutes());
	}
	
	@Test
	public void majCompteurRecupToAgent_compteurInexistant() {
		
		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9008765;
		int minutes = 10;
		
		ISirhRepository sR = Mockito.mock(ISirhRepository.class);
		Mockito.when(sR.getAgent(idAgent)).thenReturn(new Agent());
		
		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
		Mockito.when(rr.getAgentCounter(AgentRecupCount.class, idAgent)).thenReturn(null);
		
		HelperService hS = Mockito.mock(HelperService.class);
		
		CounterService service = new CounterService();
		ReflectionTestUtils.setField(service, "sirhRepository", sR);
		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "helperService", hS);
		
		result = service.majCompteurRecupToAgent(result, idAgent, minutes);
		
		assertEquals(0, result.getErrors().size());
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(BaseAgentCount.class));
	}
	
	@Test
	public void majCompteurRecupToAgent_compteurNegatif_debit() {
		
		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9008765;
		int minutes = -11;
		
		ISirhRepository sR = Mockito.mock(ISirhRepository.class);
		Mockito.when(sR.getAgent(idAgent)).thenReturn(new Agent());
		
		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
		AgentRecupCount arc = new AgentRecupCount();
		arc.setTotalMinutes(10);
		Mockito.when(rr.getAgentCounter(AgentRecupCount.class, idAgent)).thenReturn(arc);
		
		HelperService hS = Mockito.mock(HelperService.class);
		
		CounterService service = new CounterService();
		ReflectionTestUtils.setField(service, "sirhRepository", sR);
		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "helperService", hS);
		
		result = service.majCompteurRecupToAgent(result, idAgent, minutes);
		
		assertEquals(1, result.getErrors().size());
		assertEquals("Le solde du compteur de l'agent ne peut pas être négatif.", result.getErrors().get(0));
		Mockito.verify(rr, Mockito.times(0)).persistEntity(Mockito.isA(BaseAgentCount.class));
	}
	
	@Test
	public void majCompteurRecupToAgent_compteurNegatif_credit() {
		
		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9008765;
		int minutes = 11;
		
		ISirhRepository sR = Mockito.mock(ISirhRepository.class);
		Mockito.when(sR.getAgent(idAgent)).thenReturn(new Agent());
		
		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
		AgentRecupCount arc = new AgentRecupCount();
		arc.setTotalMinutes(-15);
		Mockito.when(rr.getAgentCounter(AgentRecupCount.class, idAgent)).thenReturn(arc);
		
		HelperService hS = Mockito.mock(HelperService.class);
		
		CounterService service = new CounterService();
		ReflectionTestUtils.setField(service, "sirhRepository", sR);
		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "helperService", hS);
		
		result = service.majCompteurRecupToAgent(result, idAgent, minutes);
		
		assertEquals(0, result.getErrors().size());
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(BaseAgentCount.class));
	}
	
	@Test
	public void majCompteurRecupToAgent_debitOk() {
		
		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9008765;
		int minutes = -11;
		
		ISirhRepository sR = Mockito.mock(ISirhRepository.class);
		Mockito.when(sR.getAgent(idAgent)).thenReturn(new Agent());
		
		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
		AgentRecupCount arc = new AgentRecupCount();
		arc.setTotalMinutes(12);
		Mockito.when(rr.getAgentCounter(AgentRecupCount.class, idAgent)).thenReturn(arc);
		
		HelperService hS = Mockito.mock(HelperService.class);
		
		CounterService service = new CounterService();
		ReflectionTestUtils.setField(service, "sirhRepository", sR);
		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "helperService", hS);
		
		result = service.majCompteurRecupToAgent(result, idAgent, minutes);
		
		assertEquals(0, result.getErrors().size());
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(BaseAgentCount.class));
	}
	
	@Test
	public void controlSaisieAlimManuelleCompteur_minutesNonSaisie() {
		
		CompteurDto compteurDto = new CompteurDto();
		ReturnMessageDto result = new ReturnMessageDto();
		
		CounterService service = new CounterService();
		service.controlSaisieAlimManuelleCompteur(compteurDto, result);
		
		assertEquals(1, result.getErrors().size());
		assertEquals("La durée à ajouter ou retrancher n'est pas saisie.", result.getErrors().get(0).toString());
	}
	
	@Test
	public void controlSaisieAlimManuelleCompteur_minutesErreurSaisie() {
		
		CompteurDto compteurDto = new CompteurDto();
			compteurDto.setDureeAAjouter(1);
			compteurDto.setDureeARetrancher(1);
		ReturnMessageDto result = new ReturnMessageDto();
		
		CounterService service = new CounterService();
		service.controlSaisieAlimManuelleCompteur(compteurDto, result);
		
		assertEquals(1, result.getErrors().size());
		assertEquals("Un seul des champs Durée à ajouter ou Durée à retrancher doit être saisi.", result.getErrors().get(0).toString());
	}
	
	@Test
	public void controlSaisieAlimManuelleCompteur_ajoutOK() {
		
		CompteurDto compteurDto = new CompteurDto();
			compteurDto.setDureeAAjouter(1);
		ReturnMessageDto result = new ReturnMessageDto();
		
		CounterService service = new CounterService();
			service.controlSaisieAlimManuelleCompteur(compteurDto, result);
		
		assertEquals(0, result.getErrors().size());
	}
	
	@Test
	public void controlSaisieAlimManuelleCompteur_retireOK() {
		CompteurDto compteurDto = new CompteurDto();
			compteurDto.setDureeARetrancher(1);
		ReturnMessageDto result = new ReturnMessageDto();
		
		CounterService service = new CounterService();
		service.controlSaisieAlimManuelleCompteur(compteurDto, result);
		
		assertEquals(0, result.getErrors().size());
	}
	
	@Test
	public void majManuelleCompteurRecupToAgent_OperateurNonHabilite() {
		
		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138; 
		CompteurDto compteurDto = new CompteurDto();
			compteurDto.setIdAgent(9005151);
		
		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
			Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(false);
		
		CounterService service = new CounterService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		
		result = service.majManuelleCompteurRecupToAgent(idAgent, compteurDto);
		
		assertEquals(1, result.getErrors().size());
		assertEquals("Vous n'êtes pas opérateur de cette agent.", result.getErrors().get(0).toString());
	}
	
	@Test
	public void majManuelleCompteurRecupToAgent_agentInexistant() {
		
		Integer idAgent = 9005138; 
		CompteurDto compteurDto = new CompteurDto();
			compteurDto.setIdAgent(9005151);
		
		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
			Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(true);
		
		HelperService helperService = Mockito.mock(HelperService.class);
			Mockito.when(helperService.calculMinutesAlimManuelleCompteur(compteurDto)).thenReturn(10);
			
		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
			Mockito.when(sirhRepository.getAgent(compteurDto.getIdAgent())).thenReturn(null);
		
		CounterService service = new CounterService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		
		boolean isAgentNotFoundException = false;
		try {
			service.majManuelleCompteurRecupToAgent(idAgent, compteurDto);
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
			compteurDto.setDureeARetrancher(10);
		
		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
			Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(true);
		
		HelperService helperService = Mockito.mock(HelperService.class);
			Mockito.when(helperService.calculMinutesAlimManuelleCompteur(compteurDto)).thenReturn(-10);
			
		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
			Mockito.when(sirhRepository.getAgent(compteurDto.getIdAgent())).thenReturn(new Agent());
		
		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
			Mockito.when(counterRepository.getAgentCounter(AgentRecupCount.class, compteurDto.getIdAgent())).thenReturn(null);
		
		CounterService service = new CounterService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		
		result = service.majManuelleCompteurRecupToAgent(idAgent, compteurDto);

		assertEquals(1, result.getErrors().size());
		assertEquals("Le solde du compteur de l'agent ne peut pas être négatif.", result.getErrors().get(0).toString());
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentWeekAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentRecupCount.class));
	}
	
	@Test
	public void majManuelleCompteurRecupToAgent_CompteurExistant_And_SoldeNegatif() {
		
		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138; 
		CompteurDto compteurDto = new CompteurDto();
			compteurDto.setIdAgent(9005151);
			compteurDto.setDureeARetrancher(10);
		
		AgentRecupCount arc = new AgentRecupCount();
			arc.setTotalMinutes(5);
					
		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
			Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(true);
		
		HelperService helperService = Mockito.mock(HelperService.class);
			Mockito.when(helperService.calculMinutesAlimManuelleCompteur(compteurDto)).thenReturn(-10);
			
		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
			Mockito.when(sirhRepository.getAgent(compteurDto.getIdAgent())).thenReturn(new Agent());
		
		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
			Mockito.when(counterRepository.getAgentCounter(AgentRecupCount.class, compteurDto.getIdAgent())).thenReturn(arc);
		
		CounterService service = new CounterService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		
		result = service.majManuelleCompteurRecupToAgent(idAgent, compteurDto);

		assertEquals(1, result.getErrors().size());
		assertEquals("Le solde du compteur de l'agent ne peut pas être négatif.", result.getErrors().get(0).toString());
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentWeekAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentRecupCount.class));
	}
	
	@Test
	public void majManuelleCompteurRecupToAgent_MotifCompteurExistant() {
		
		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138; 
		CompteurDto compteurDto = new CompteurDto();
			compteurDto.setIdAgent(9005151);
			compteurDto.setDureeARetrancher(10);
			compteurDto.setIdMotifCompteur(1);
		
		AgentRecupCount arc = new AgentRecupCount();
			arc.setTotalMinutes(15);
					
		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
			Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(true);
		
		HelperService helperService = Mockito.mock(HelperService.class);
			Mockito.when(helperService.calculMinutesAlimManuelleCompteur(compteurDto)).thenReturn(-10);
			
		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
			Mockito.when(sirhRepository.getAgent(compteurDto.getIdAgent())).thenReturn(new Agent());
		
		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
			Mockito.when(counterRepository.getAgentCounter(AgentRecupCount.class, compteurDto.getIdAgent())).thenReturn(arc);
			Mockito.when(counterRepository.getEntity(MotifCompteur.class, compteurDto.getIdMotifCompteur())).thenReturn(null);
		
		CounterService service = new CounterService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		
		result = service.majManuelleCompteurRecupToAgent(idAgent, compteurDto);

		assertEquals(1, result.getErrors().size());
		assertEquals("Le motif n'existe pas.", result.getErrors().get(0).toString());
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentWeekAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentRecupCount.class));
	}
	
	@Test
	public void majManuelleCompteurRecupToAgent_OK_avecCompteurExistant() {
		
		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138; 
		CompteurDto compteurDto = new CompteurDto();
			compteurDto.setIdAgent(9005151);
			compteurDto.setDureeARetrancher(10);
			compteurDto.setIdMotifCompteur(1);
		
		AgentRecupCount arc = new AgentRecupCount();
			arc.setTotalMinutes(15);
					
		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
			Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(true);
		
		HelperService helperService = Mockito.mock(HelperService.class);
			Mockito.when(helperService.calculMinutesAlimManuelleCompteur(compteurDto)).thenReturn(-10);
			Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());
			
		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
			Mockito.when(sirhRepository.getAgent(compteurDto.getIdAgent())).thenReturn(new Agent());
		
		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
			Mockito.when(counterRepository.getAgentCounter(AgentRecupCount.class, compteurDto.getIdAgent())).thenReturn(arc);
			Mockito.when(counterRepository.getEntity(MotifCompteur.class, compteurDto.getIdMotifCompteur())).thenReturn(new MotifCompteur());
		
		CounterService service = new CounterService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		
		result = service.majManuelleCompteurRecupToAgent(idAgent, compteurDto);

		assertEquals(0, result.getErrors().size()); 
		
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentWeekAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentRecupCount.class));
	}
	
	@Test
	public void majManuelleCompteurRecupToAgent_OK_sansCompteurExistant() {
		
		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138; 
		CompteurDto compteurDto = new CompteurDto();
			compteurDto.setIdAgent(9005151);
			compteurDto.setDureeARetrancher(10);
			compteurDto.setIdMotifCompteur(1);
		
		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
			Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(true);
		
		HelperService helperService = Mockito.mock(HelperService.class);
			Mockito.when(helperService.calculMinutesAlimManuelleCompteur(compteurDto)).thenReturn(10);
			Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());
			
		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
			Mockito.when(sirhRepository.getAgent(compteurDto.getIdAgent())).thenReturn(new Agent());
		
		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
			Mockito.when(counterRepository.getAgentCounter(AgentRecupCount.class, compteurDto.getIdAgent())).thenReturn(null);
			Mockito.when(counterRepository.getEntity(MotifCompteur.class, compteurDto.getIdMotifCompteur())).thenReturn(new MotifCompteur());
		
		CounterService service = new CounterService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		
		result = service.majManuelleCompteurRecupToAgent(idAgent, compteurDto);

		assertEquals(0, result.getErrors().size()); 
		
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentWeekAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentRecupCount.class));
	}
}
