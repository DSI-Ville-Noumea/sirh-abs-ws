package nc.noumea.mairie.abs.service.counter.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentCount;
import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;
import nc.noumea.mairie.abs.domain.AgentRecupCount;
import nc.noumea.mairie.abs.domain.AgentWeekRecup;
import nc.noumea.mairie.abs.domain.DemandeRecup;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.MotifCompteur;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.MotifCompteurDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IAccessRightsRepository;
import nc.noumea.mairie.abs.repository.ICounterRepository;
import nc.noumea.mairie.abs.service.AgentNotFoundException;
import nc.noumea.mairie.abs.service.NotAMondayException;
import nc.noumea.mairie.abs.service.impl.HelperService;
import nc.noumea.mairie.ws.ISirhWSConsumer;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class RecupCounterServiceImplTest extends AbstractCounterServiceTest {

	private RecupCounterServiceImpl service = new RecupCounterServiceImpl();
	
	@Test
	public void testMethodeParenteHeritage() {
		super.allTest(new RecupCounterServiceImpl());
	}
	
	@Test
	public void calculMinutesCompteur_etatApprouve() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());

		DemandeRecup demande = new DemandeRecup();
		demande.setDuree(10);

		int minutes = service.calculMinutesCompteur(demandeEtatChangeDto, demande);

		assertEquals(-10, minutes);
	}

	@Test
	public void calculMinutesCompteur_etatRefuse_and_etatPrcdApprouve() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.REFUSEE.getCodeEtat());

		EtatDemande etatDemande = new EtatDemande();
		etatDemande.setEtat(RefEtatEnum.APPROUVEE);

		DemandeRecup demande = new DemandeRecup();
		demande.setDuree(10);
		demande.addEtatDemande(etatDemande);
		
		int minutes = service.calculMinutesCompteur(demandeEtatChangeDto, demande);

		assertEquals(10, minutes);
	}

	@Test
	public void calculMinutesCompteur_etatRefuse_and_etatPrcdVisee() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.REFUSEE.getCodeEtat());

		EtatDemande etatDemande = new EtatDemande();
		etatDemande.setEtat(RefEtatEnum.VISEE_FAVORABLE);

		DemandeRecup demande = new DemandeRecup();
		demande.setDuree(10);
		demande.addEtatDemande(etatDemande);

		int minutes = service.calculMinutesCompteur(demandeEtatChangeDto, demande);

		assertEquals(0, minutes);
	}
	
	@Test
	public void addRecuperationToAgent_AgentDoesNotExists_ThrowAgentNotFoundException() {

		// Given
		Integer idAgent = 9008765;
		Date dateMonday = new LocalDate(2013, 9, 30).toDate();

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(idAgent)).thenReturn(null);

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
	public void addRecuperationToAgent_DateIsNotAMonday_ThrowDateIsNotAMondayException() {

		// Given
		Integer idAgent = 9008765;
		Date dateMonday = new LocalDate(2013, 9, 29).toDate();

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(idAgent)).thenReturn(new AgentGeneriqueDto());

		HelperService hS = Mockito.mock(HelperService.class);
		Mockito.when(hS.isDateAMonday(dateMonday)).thenReturn(false);

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
	public void addRecuperationToAgent_AgentHasNoRecupForThatWeek_AgentHasNoAccount_CreateItemsAndAddQteToAccount() {

		// Given
		Integer idAgent = 9008765;
		Date dateMonday = new LocalDate(2013, 9, 30).toDate();

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(idAgent)).thenReturn(new AgentGeneriqueDto());

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
		Mockito.when(rr.getWeekHistoForAgentAndDate(AgentWeekRecup.class, idAgent, dateMonday)).thenReturn(null);
		Mockito.when(rr.getAgentCounter(AgentRecupCount.class, idAgent)).thenReturn(null);

		HelperService hS = Mockito.mock(HelperService.class);
		Mockito.when(hS.getCurrentDate()).thenReturn(new DateTime(2013, 4, 2, 8, 56, 12).toDate());
		Mockito.when(hS.isDateAMonday(dateMonday)).thenReturn(true);

		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "helperService", hS);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

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

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(idAgent)).thenReturn(new AgentGeneriqueDto());

		AgentWeekRecup awr = new AgentWeekRecup();
		awr.setMinutes(80);
		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
		Mockito.when(rr.getWeekHistoForAgentAndDate(AgentWeekRecup.class, idAgent, dateMonday)).thenReturn(awr);
		AgentRecupCount arc = new AgentRecupCount();
		arc.setTotalMinutes(10);
		Mockito.when(rr.getAgentCounter(AgentRecupCount.class, idAgent)).thenReturn(arc);

		HelperService hS = Mockito.mock(HelperService.class);
		Mockito.when(hS.isDateAMonday(dateMonday)).thenReturn(true);

		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
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

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(idAgent)).thenReturn(new AgentGeneriqueDto());

		AgentWeekRecup awr = new AgentWeekRecup();
		awr.setMinutes(80);
		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
		Mockito.when(rr.getWeekHistoForAgentAndDate(AgentWeekRecup.class, idAgent, dateMonday)).thenReturn(awr);
		AgentRecupCount arc = new AgentRecupCount();
		arc.setTotalMinutes(10);
		Mockito.when(rr.getAgentCounter(AgentRecupCount.class, idAgent)).thenReturn(arc);

		HelperService hS = Mockito.mock(HelperService.class);
		Mockito.when(hS.isDateAMonday(dateMonday)).thenReturn(true);

		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", hS);

		// When
		int result = service.addToAgentForPTG(idAgent, dateMonday, 70);

		// Then
		assertEquals(0, result);
		assertEquals(0, (int) arc.getTotalMinutes());
		assertEquals(70, (int) awr.getMinutes());
	}
	
	@Test
	public void addProvisoireToAgentForPTG_counterNotExist() {

		// Given
		Integer idAgent = 9008765;
		Date date = new LocalDate(2013, 9, 30).toDate();

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(idAgent)).thenReturn(new AgentGeneriqueDto());

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
		Mockito.when(rr.getAgentCounter(AgentRecupCount.class, idAgent)).thenReturn(null);

		HelperService hS = Mockito.mock(HelperService.class);

		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", hS);

		// When
		int result = service.addToAgentForPTG(idAgent, date, 90, 1, null);

		// Then
		assertEquals(90, result);
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(AgentRecupCount.class));
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(AgentWeekRecup.class));
	}
	
	@Test
	public void addProvisoireToAgentForPTG_counterExist() {

		// Given
		Integer idAgent = 9008765;
		Date date = new LocalDate(2013, 9, 30).toDate();

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(idAgent)).thenReturn(new AgentGeneriqueDto());

		AgentRecupCount arcTemp = new AgentRecupCount();
		arcTemp.setTotalMinutes(50);
		
		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
		Mockito.when(rr.getAgentCounter(AgentRecupCount.class, idAgent)).thenReturn(arcTemp);

		HelperService hS = Mockito.mock(HelperService.class);

		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", hS);

		// When
		int result = service.addToAgentForPTG(idAgent, date, 90, 1, null);

		// Then
		assertEquals(140, result);
		assertEquals(140, arcTemp.getTotalMinutes());
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(AgentRecupCount.class));
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(AgentWeekRecup.class));
	}
	
	@Test
	public void addProvisoireToAgentForPTG_counterExistAndUpdatedYet() {

		// Given
		Integer idAgent = 9008765;
		Integer idPointage = 1;
		Date date = new LocalDate(2013, 9, 30).toDate();

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(idAgent)).thenReturn(new AgentGeneriqueDto());

		AgentRecupCount arcTemp = new AgentRecupCount();
		arcTemp.setTotalMinutes(50);
		
		AgentWeekRecup weekTemp = new AgentWeekRecup();
		weekTemp.setMinutes(50);
		
		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
		Mockito.when(rr.getAgentCounter(AgentRecupCount.class, idAgent)).thenReturn(arcTemp);
		Mockito.when(rr.getWeekHistoRecupCountByIdAgentAndIdPointage(idAgent, idPointage)).thenReturn(weekTemp);

		HelperService hS = Mockito.mock(HelperService.class);

		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", hS);

		// When
		int result = service.addToAgentForPTG(idAgent, date, 90, idPointage, null);

		// Then
		assertEquals(90, result);
		assertEquals(90, arcTemp.getTotalMinutes());
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(AgentRecupCount.class));
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(AgentWeekRecup.class));
	}

	@Test
	public void majCompteurRecupToAgent_compteurInexistant() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());

		ReturnMessageDto result = new ReturnMessageDto();
		DemandeRecup demande = new DemandeRecup();
		demande.setIdAgent(9008765);
		demande.setDuree(-10);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(demande.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
		Mockito.when(rr.getAgentCounter(AgentRecupCount.class, demande.getIdAgent())).thenReturn(null);

		HelperService hS = Mockito.mock(HelperService.class);

		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "helperService", hS);

		result = service.majCompteurToAgent(result, demande, demandeEtatChangeDto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(AgentCount.class));
	}

	// #17063 evol
	@Test
	public void majCompteurRecupToAgent_compteurNegatif_debit() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());

		ReturnMessageDto result = new ReturnMessageDto();
		DemandeRecup demande = new DemandeRecup();
		demande.setIdAgent(9008765);
		demande.setDuree(11);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(demande.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
		AgentRecupCount arc = new AgentRecupCount();
		arc.setTotalMinutes(10);
		Mockito.when(rr.getAgentCounter(AgentRecupCount.class, demande.getIdAgent())).thenReturn(arc);

		HelperService hS = Mockito.mock(HelperService.class);

		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "helperService", hS);

		result = service.majCompteurToAgent(result, demande, demandeEtatChangeDto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(AgentCount.class));
	}

	@Test
	public void majCompteurRecupToAgent_compteurNegatif_credit() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.REFUSEE.getCodeEtat());

		EtatDemande e = new EtatDemande();
		e.setEtat(RefEtatEnum.APPROUVEE);
		List<EtatDemande> etatsDemande = new ArrayList<EtatDemande>();
		etatsDemande.add(e);

		ReturnMessageDto result = new ReturnMessageDto();
		DemandeRecup demande = new DemandeRecup();
		demande.setIdAgent(9008765);
		demande.setDuree(11);
		demande.setEtatsDemande(etatsDemande);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(demande.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
		AgentRecupCount arc = new AgentRecupCount();
		arc.setTotalMinutes(-15);
		Mockito.when(rr.getAgentCounter(AgentRecupCount.class, demande.getIdAgent())).thenReturn(arc);

		HelperService hS = Mockito.mock(HelperService.class);

		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "helperService", hS);

		result = service.majCompteurToAgent(result, demande, demandeEtatChangeDto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(AgentCount.class));
	}

	@Test
	public void majCompteurRecupToAgent_debitOk() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());

		ReturnMessageDto result = new ReturnMessageDto();
		DemandeRecup demande = new DemandeRecup();
		demande.setIdAgent(9008765);
		demande.setDuree(-11);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(demande.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
		AgentRecupCount arc = new AgentRecupCount();
		arc.setTotalMinutes(12);
		Mockito.when(rr.getAgentCounter(AgentRecupCount.class, demande.getIdAgent())).thenReturn(arc);

		HelperService hS = Mockito.mock(HelperService.class);

		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "helperService", hS);

		result = service.majCompteurToAgent(result, demande, demandeEtatChangeDto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(AgentCount.class));
	}

	@Test
	public void majManuelleCompteurRecupToAgent_agentInexistant() {

		super.service = new RecupCounterServiceImpl();
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
	public void majManuelleCompteurRecupToAgent_CompteurInexistant_And_SoldeNegatif() {
		
		super.service = new RecupCounterServiceImpl();
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
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentRecupCount.class));
	}

	@Test
	public void majManuelleCompteurRecupToAgent_CompteurExistant_And_SoldeNegatif() {

		super.service = new RecupCounterServiceImpl();
		super.majManuelleCompteurToAgent_prepareData();
		
		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		CompteurDto compteurDto = new CompteurDto();
		compteurDto.setIdAgent(9005151);
		compteurDto.setDureeARetrancher(10.0);
		MotifCompteurDto motifDto = new MotifCompteurDto();
		motifDto.setIdMotifCompteur(1);
		compteurDto.setMotifCompteurDto(motifDto);

		AgentRecupCount arc = new AgentRecupCount();
		arc.setTotalMinutes(5);

		Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(true);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculAlimManuelleCompteur(compteurDto)).thenReturn(-10.0);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(result);
		Mockito.when(sirhWSConsumer.getAgent(compteurDto.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		Mockito.when(counterRepository.getAgentCounter(AgentRecupCount.class, compteurDto.getIdAgent()))
				.thenReturn(arc);

		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		result = service.majManuelleCompteurToAgent(idAgent, compteurDto);

		assertEquals(1, result.getErrors().size());
		assertEquals("Le solde du compteur de l'agent ne peut pas être négatif.", result.getErrors().get(0).toString());
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
		MotifCompteurDto motifDto = new MotifCompteurDto();
		motifDto.setIdMotifCompteur(1);
		compteurDto.setMotifCompteurDto(motifDto);

		AgentRecupCount arc = new AgentRecupCount();
		arc.setTotalMinutes(15);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(true);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculAlimManuelleCompteur(compteurDto)).thenReturn(-10.0);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentRecupCount.class, compteurDto.getIdAgent()))
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
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentRecupCount.class));
	}

	@Test
	public void majManuelleCompteurRecupToAgent_OK_sansCompteurExistant() {

		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		CompteurDto compteurDto = new CompteurDto();
		compteurDto.setIdAgent(9005151);
		compteurDto.setDureeARetrancher(10.0);
		MotifCompteurDto motifDto = new MotifCompteurDto();
		motifDto.setIdMotifCompteur(1);
		compteurDto.setMotifCompteurDto(motifDto);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(true);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculAlimManuelleCompteur(compteurDto)).thenReturn(10.0);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentRecupCount.class, compteurDto.getIdAgent())).thenReturn(
				null);
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
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentRecupCount.class));
	}

}
