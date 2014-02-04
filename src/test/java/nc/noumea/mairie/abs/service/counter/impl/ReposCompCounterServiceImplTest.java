package nc.noumea.mairie.abs.service.counter.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;
import nc.noumea.mairie.abs.domain.AgentRecupCount;
import nc.noumea.mairie.abs.domain.AgentReposCompCount;
import nc.noumea.mairie.abs.domain.DemandeReposComp;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.MotifCompteur;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IAccessRightsRepository;
import nc.noumea.mairie.abs.repository.ICounterRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.abs.service.AgentNotFoundException;
import nc.noumea.mairie.abs.service.impl.HelperService;
import nc.noumea.mairie.sirh.domain.Agent;

import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

public class ReposCompCounterServiceImplTest {

	@Test
	public void majManuelleCompteurRCToAgent_OK_sansCompteurExistant_AnneeN() {
		
		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138; 
		CompteurDto compteurDto = new CompteurDto();
			compteurDto.setIdAgent(9005151);
			compteurDto.setDureeARetrancher(10);
			compteurDto.setIdMotifCompteur(1);
			compteurDto.setAnneePrécedente(false);
		
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
		
		ReposCompCounterServiceImpl service = new ReposCompCounterServiceImpl();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		
		result = service.majManuelleCompteurToAgent(idAgent, compteurDto);

		assertEquals(0, result.getErrors().size()); 
		
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentReposCompCount.class));
	}
	
	@Test
	public void majManuelleCompteurRCToAgent_OK_sansCompteurExistant_AnneeN1() {
		
		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138; 
		CompteurDto compteurDto = new CompteurDto();
			compteurDto.setIdAgent(9005151);
			compteurDto.setDureeARetrancher(10);
			compteurDto.setIdMotifCompteur(1);
			compteurDto.setAnneePrécedente(true);
		
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
		
		ReposCompCounterServiceImpl service = new ReposCompCounterServiceImpl();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		
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
			compteurDto.setDureeARetrancher(10);
			compteurDto.setIdMotifCompteur(1);
		
		AgentRecupCount arc = new AgentRecupCount();
			arc.setTotalMinutes(15);
					
		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
			Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(true);
		
		HelperService helperService = Mockito.mock(HelperService.class);
			Mockito.when(helperService.calculMinutesAlimManuelleCompteur(compteurDto)).thenReturn(10);
			
		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
			Mockito.when(sirhRepository.getAgent(compteurDto.getIdAgent())).thenReturn(new Agent());
		
		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
			Mockito.when(counterRepository.getAgentCounter(AgentRecupCount.class, compteurDto.getIdAgent())).thenReturn(arc);
			Mockito.when(counterRepository.getEntity(MotifCompteur.class, compteurDto.getIdMotifCompteur())).thenReturn(null);
		
		ReposCompCounterServiceImpl service = new ReposCompCounterServiceImpl();
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
	public void majManuelleCompteurRCToAgent_KO_compteurNegatif_AnneeN1() {
		
		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138; 
		CompteurDto compteurDto = new CompteurDto();
			compteurDto.setIdAgent(9005151);
			compteurDto.setDureeARetrancher(10);
			compteurDto.setIdMotifCompteur(1);
			compteurDto.setAnneePrécedente(true);
		
		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
			Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(true);
		
		HelperService helperService = Mockito.mock(HelperService.class);
			Mockito.when(helperService.calculMinutesAlimManuelleCompteur(compteurDto)).thenReturn(-10);
			Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());
			
		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
			Mockito.when(sirhRepository.getAgent(compteurDto.getIdAgent())).thenReturn(new Agent());
		
		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
			Mockito.when(counterRepository.getAgentCounter(AgentRecupCount.class, compteurDto.getIdAgent())).thenReturn(null);
			Mockito.when(counterRepository.getEntity(MotifCompteur.class, compteurDto.getIdMotifCompteur())).thenReturn(new MotifCompteur());
		
		ReposCompCounterServiceImpl service = new ReposCompCounterServiceImpl();
			ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
			ReflectionTestUtils.setField(service, "helperService", helperService);
			ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
			ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		
		result = service.majManuelleCompteurToAgent(idAgent, compteurDto);

		assertEquals(1, result.getErrors().size()); 
		assertEquals("Le solde du compteur de l'agent ne peut pas être négatif.", result.getErrors().get(0).toString());
		
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentReposCompCount.class));
	}
	
	@Test
	public void majManuelleCompteurRCToAgent_KO_compteurNegatif_AnneeN0() {
		
		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		CompteurDto compteurDto = new CompteurDto();
			compteurDto.setIdAgent(9005151);
			compteurDto.setDureeARetrancher(10);
			compteurDto.setIdMotifCompteur(1);
			compteurDto.setAnneePrécedente(false);
		
		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
			Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(true);
		
		HelperService helperService = Mockito.mock(HelperService.class);
			Mockito.when(helperService.calculMinutesAlimManuelleCompteur(compteurDto)).thenReturn(-10);
			Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());
			
		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
			Mockito.when(sirhRepository.getAgent(compteurDto.getIdAgent())).thenReturn(new Agent());
		
		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
			Mockito.when(counterRepository.getAgentCounter(AgentRecupCount.class, compteurDto.getIdAgent())).thenReturn(null);
			Mockito.when(counterRepository.getEntity(MotifCompteur.class, compteurDto.getIdMotifCompteur())).thenReturn(new MotifCompteur());
		
		ReposCompCounterServiceImpl service = new ReposCompCounterServiceImpl();
			ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
			ReflectionTestUtils.setField(service, "helperService", helperService);
			ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
			ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		
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
			Mockito.when(counterRepository.getAgentReposCompCountByIdCounter(idAgentReposCompCount)).thenReturn(null);
		
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
			Mockito.when(counterRepository.getAgentReposCompCountByIdCounter(idAgentReposCompCount)).thenReturn(arc);
			Mockito.doAnswer(new Answer<Object>() {
				public Object answer(InvocationOnMock invocation) {
					Object[] args = invocation.getArguments();
					AgentHistoAlimManuelle obj = (AgentHistoAlimManuelle) args[0];

					assertEquals(10, obj.getMinutes().intValue() );
					assertEquals(-250, obj.getMinutesAnneeN1().intValue());

					return true;
				}
			}).when(counterRepository).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
			
			Mockito.doAnswer(new Answer<Object>() {
				public Object answer(InvocationOnMock invocation) {
					Object[] args = invocation.getArguments();
					AgentReposCompCount obj = (AgentReposCompCount) args[0];

					assertEquals(20, obj.getTotalMinutes());
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
			Mockito.when(counterRepository.getAgentReposCompCountByIdCounter(idAgentReposCompCount)).thenReturn(arc);
			Mockito.doAnswer(new Answer<Object>() {
				public Object answer(InvocationOnMock invocation) {
					Object[] args = invocation.getArguments();
					AgentHistoAlimManuelle obj = (AgentHistoAlimManuelle) args[0];

					assertEquals(150, obj.getMinutes().intValue() );
					assertEquals(-150, obj.getMinutesAnneeN1().intValue());

					return true;
				}
			}).when(counterRepository).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
			
			Mockito.doAnswer(new Answer<Object>() {
				public Object answer(InvocationOnMock invocation) {
					Object[] args = invocation.getArguments();
					AgentReposCompCount obj = (AgentReposCompCount) args[0];

					assertEquals(160, obj.getTotalMinutes());
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
			Mockito.when(counterRepository.getAgentReposCompCountByIdCounter(idAgentReposCompCount)).thenReturn(null);
		
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
			Mockito.when(counterRepository.getAgentReposCompCountByIdCounter(idAgentReposCompCount)).thenReturn(arc);
			Mockito.doAnswer(new Answer<Object>() {
				public Object answer(InvocationOnMock invocation) {
					Object[] args = invocation.getArguments();
					AgentHistoAlimManuelle obj = (AgentHistoAlimManuelle) args[0];

					assertEquals(-20, obj.getMinutes().intValue() );
					assertEquals(20, obj.getMinutesAnneeN1().intValue());

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
		
		ReturnMessageDto result = new ReturnMessageDto();
		DemandeReposComp demande = new DemandeReposComp();
			demande.setIdAgent(9008765);
		int minutes = 10;
		
		ISirhRepository sR = Mockito.mock(ISirhRepository.class);
			Mockito.when(sR.getAgent(demande.getIdAgent())).thenReturn(null);
		
		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
			Mockito.when(rr.getAgentCounter(AgentReposCompCount.class, demande.getIdAgent())).thenReturn(null);
		
		ReposCompCounterServiceImpl service = new ReposCompCounterServiceImpl();
			ReflectionTestUtils.setField(service, "sirhRepository", sR);
			ReflectionTestUtils.setField(service, "counterRepository", rr);
		
		boolean isException = false;
		try {
			service.majCompteurToAgent(result, demande, minutes);
		}catch (AgentNotFoundException e) {
			isException = true;
		}

		assertTrue(isException);
		Mockito.verify(rr, Mockito.times(0)).persistEntity(Mockito.isA(AgentReposCompCount.class));
		Mockito.verify(rr, Mockito.times(0)).persistEntity(Mockito.isA(DemandeReposComp.class));
	}
	
	@Test
	public void majCompteurToAgent_compteurInexistant() {
		
		ReturnMessageDto result = new ReturnMessageDto();
		DemandeReposComp demande = new DemandeReposComp();
			demande.setIdAgent(9008765);
		int minutes = 10;
		
		ISirhRepository sR = Mockito.mock(ISirhRepository.class);
			Mockito.when(sR.getAgent(demande.getIdAgent())).thenReturn(new Agent());
		
		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
			Mockito.when(rr.getAgentCounter(AgentReposCompCount.class, demande.getIdAgent())).thenReturn(null);
		
		HelperService hS = Mockito.mock(HelperService.class);
		
		ReposCompCounterServiceImpl service = new ReposCompCounterServiceImpl();
			ReflectionTestUtils.setField(service, "sirhRepository", sR);
			ReflectionTestUtils.setField(service, "counterRepository", rr);
			ReflectionTestUtils.setField(service, "helperService", hS);
		
		result = service.majCompteurToAgent(result, demande, minutes);
		
		assertEquals(0, result.getErrors().size());
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(AgentReposCompCount.class));
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(DemandeReposComp.class));
	}
	
	@Test
	public void majCompteurToAgent_compteurNegatif_debit() {
		
		ReturnMessageDto result = new ReturnMessageDto();
		DemandeReposComp demande = new DemandeReposComp();
			demande.setIdAgent(9008765);
		int minutes = -11;
		
		ISirhRepository sR = Mockito.mock(ISirhRepository.class);
			Mockito.when(sR.getAgent(demande.getIdAgent())).thenReturn(new Agent());
		
		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
			AgentReposCompCount arc = new AgentReposCompCount();
				arc.setTotalMinutes(8);
				arc.setTotalMinutesAnneeN1(2);
			Mockito.when(rr.getAgentCounter(AgentReposCompCount.class, demande.getIdAgent())).thenReturn(arc);
		
		HelperService hS = Mockito.mock(HelperService.class);
		
		ReposCompCounterServiceImpl service = new ReposCompCounterServiceImpl();
			ReflectionTestUtils.setField(service, "sirhRepository", sR);
			ReflectionTestUtils.setField(service, "counterRepository", rr);
			ReflectionTestUtils.setField(service, "helperService", hS);
		
		result = service.majCompteurToAgent(result, demande, minutes);
		
		assertEquals(1, result.getErrors().size());
		assertEquals("Le solde du compteur de l'agent ne peut pas être négatif.", result.getErrors().get(0));
		Mockito.verify(rr, Mockito.times(0)).persistEntity(Mockito.isA(AgentReposCompCount.class));
		Mockito.verify(rr, Mockito.times(0)).persistEntity(Mockito.isA(DemandeReposComp.class));
	}
	
	@Test
	public void majCompteurToAgent_compteurNegatif_credit() {
		
		ReturnMessageDto result = new ReturnMessageDto();
		
		DemandeReposComp demande = new DemandeReposComp();
			demande.setIdAgent(9008765);
			demande.setDuree(8);
			demande.setDureeAnneeN1(9);
			
		AgentReposCompCount arc = new AgentReposCompCount();
			arc.setTotalMinutes(11);
			arc.setTotalMinutesAnneeN1(0);
			
		int minutes = 17;
		
		ISirhRepository sR = Mockito.mock(ISirhRepository.class);
			Mockito.when(sR.getAgent(demande.getIdAgent())).thenReturn(new Agent());
		
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
			ReflectionTestUtils.setField(service, "sirhRepository", sR);
			ReflectionTestUtils.setField(service, "counterRepository", rr);
			ReflectionTestUtils.setField(service, "helperService", hS);
		
		result = service.majCompteurToAgent(result, demande, minutes);
		
		assertEquals(0, result.getErrors().size());
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(AgentReposCompCount.class));
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(DemandeReposComp.class));
	}
	
	@Test
	public void majCompteurToAgent_compteurNegatif_credit_2eCas() {
		
		ReturnMessageDto result = new ReturnMessageDto();
		
		DemandeReposComp demande = new DemandeReposComp();
			demande.setIdAgent(9008765);
			demande.setDuree(0);
			demande.setDureeAnneeN1(17);
		
		AgentReposCompCount arc = new AgentReposCompCount();
			arc.setTotalMinutes(11);
			arc.setTotalMinutesAnneeN1(0);
		
		int minutes = 17;
		
		ISirhRepository sR = Mockito.mock(ISirhRepository.class);
			Mockito.when(sR.getAgent(demande.getIdAgent())).thenReturn(new Agent());
		
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
			ReflectionTestUtils.setField(service, "sirhRepository", sR);
			ReflectionTestUtils.setField(service, "counterRepository", rr);
			ReflectionTestUtils.setField(service, "helperService", hS);
		
		result = service.majCompteurToAgent(result, demande, minutes);
		
		assertEquals(0, result.getErrors().size());
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(AgentReposCompCount.class));
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(DemandeReposComp.class));
	}
	
	@Test
	public void majCompteurToAgent_compteurNegatif_credit_3eCas() {
		
		ReturnMessageDto result = new ReturnMessageDto();
		
		DemandeReposComp demande = new DemandeReposComp();
			demande.setIdAgent(9008765);
			demande.setDuree(17);
			demande.setDureeAnneeN1(0);
		
		AgentReposCompCount arc = new AgentReposCompCount();
			arc.setTotalMinutes(11);
			arc.setTotalMinutesAnneeN1(0);
		
		int minutes = 17;
		
		ISirhRepository sR = Mockito.mock(ISirhRepository.class);
			Mockito.when(sR.getAgent(demande.getIdAgent())).thenReturn(new Agent());
		
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
			ReflectionTestUtils.setField(service, "sirhRepository", sR);
			ReflectionTestUtils.setField(service, "counterRepository", rr);
			ReflectionTestUtils.setField(service, "helperService", hS);
		
		result = service.majCompteurToAgent(result, demande, minutes);
		
		assertEquals(0, result.getErrors().size());
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(AgentReposCompCount.class));
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(DemandeReposComp.class));
	}
	
	@Test
	public void majCompteurToAgent_debitOk() {
		
		ReturnMessageDto result = new ReturnMessageDto();
		DemandeReposComp demande = new DemandeReposComp();
			demande.setIdAgent(9008765);
			demande.setDuree(11);
			
		int minutes = -11;
		
		AgentReposCompCount arc = new AgentReposCompCount();
			arc.setTotalMinutes(12);
			arc.setTotalMinutesAnneeN1(5);
		
		ISirhRepository sR = Mockito.mock(ISirhRepository.class);
			Mockito.when(sR.getAgent(demande.getIdAgent())).thenReturn(new Agent());
		
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
			ReflectionTestUtils.setField(service, "sirhRepository", sR);
			ReflectionTestUtils.setField(service, "counterRepository", rr);
			ReflectionTestUtils.setField(service, "helperService", hS);
		
		result = service.majCompteurToAgent(result, demande, minutes);
		
		assertEquals(0, result.getErrors().size());
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(AgentReposCompCount.class));
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(DemandeReposComp.class));
	}
	
	@Test
	public void majCompteurToAgent_debitOk_2eCas() {
		
		ReturnMessageDto result = new ReturnMessageDto();
		DemandeReposComp demande = new DemandeReposComp();
			demande.setIdAgent(9008765);
			demande.setDuree(11);
			
		int minutes = -11;
		
		AgentReposCompCount arc = new AgentReposCompCount();
			arc.setTotalMinutes(12);
			arc.setTotalMinutesAnneeN1(12);
		
		ISirhRepository sR = Mockito.mock(ISirhRepository.class);
			Mockito.when(sR.getAgent(demande.getIdAgent())).thenReturn(new Agent());
		
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
			ReflectionTestUtils.setField(service, "sirhRepository", sR);
			ReflectionTestUtils.setField(service, "counterRepository", rr);
			ReflectionTestUtils.setField(service, "helperService", hS);
		
		result = service.majCompteurToAgent(result, demande, minutes);
		
		assertEquals(0, result.getErrors().size());
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(AgentReposCompCount.class));
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(DemandeReposComp.class));
	}
}
