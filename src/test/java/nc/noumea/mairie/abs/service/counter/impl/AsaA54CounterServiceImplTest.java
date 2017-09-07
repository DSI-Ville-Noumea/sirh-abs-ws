package nc.noumea.mairie.abs.service.counter.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import nc.noumea.mairie.abs.domain.AgentA54OrganisationSyndicale;
import nc.noumea.mairie.abs.domain.AgentAsaA54Count;
import nc.noumea.mairie.abs.domain.AgentCount;
import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;
import nc.noumea.mairie.abs.domain.DemandeAsa;
import nc.noumea.mairie.abs.domain.MotifCompteur;
import nc.noumea.mairie.abs.domain.OrganisationSyndicale;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.MotifCompteurDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IAccessRightsRepository;
import nc.noumea.mairie.abs.repository.ICounterRepository;
import nc.noumea.mairie.abs.repository.IOrganisationSyndicaleRepository;
import nc.noumea.mairie.abs.service.AgentNotFoundException;
import nc.noumea.mairie.abs.service.impl.HelperService;
import nc.noumea.mairie.ws.ISirhWSConsumer;

public class AsaA54CounterServiceImplTest extends AsaCounterServiceImplTest {

	@Test
	public void testMethodeParenteHeritage() throws Throwable {
		super.service = new AsaA54CounterServiceImpl();
		super.allTest();
	}

	@Test
	public void majManuelleCompteurAsaA54ToAgent_NonHabilite() {

		ReturnMessageDto result = new ReturnMessageDto();
		result.getErrors().add("L'agent 9005138 n'existe pas dans l'AD.");
		Integer idAgent = 9005138;
		CompteurDto compteurDto = new CompteurDto();
		compteurDto.setIdAgent(9005151);
		compteurDto.setDateDebut(new DateTime(2013, 4, 2, 0, 0, 0).toDate());

		ISirhWSConsumer wsMock = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(wsMock.isUtilisateurSIRH(idAgent)).thenReturn(result);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculAlimManuelleCompteur(compteurDto)).thenReturn(10.0);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(false);

		AsaA54CounterServiceImpl service = new AsaA54CounterServiceImpl();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);

		result = service.majManuelleCompteurToAgent(idAgent, compteurDto, false);

		assertEquals(1, result.getErrors().size());
		assertEquals("Vous n'êtes pas habilité à mettre à jour le compteur de cet agent.", result.getErrors().get(0).toString());
	}

	@Test
	public void majManuelleCompteurAsaA54ToAgent_UtilisateurSIRHHabilite_MotifCompteurInexistant() {

		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		CompteurDto compteurDto = new CompteurDto();
		MotifCompteurDto motifCompteurDto = new MotifCompteurDto();
		motifCompteurDto.setIdMotifCompteur(1);
		compteurDto.setMotifCompteurDto(motifCompteurDto);
		compteurDto.setIdAgent(9005151);
		compteurDto.setDureeAAjouter(10.0);
		compteurDto.setDateDebut(new DateTime(2013, 1, 1, 0, 0, 0).toDate());
		compteurDto.setDateDebut(new DateTime(2013, 12, 31, 0, 0, 0).toDate());

		AgentAsaA54Count arc = new AgentAsaA54Count();
		arc.setTotalJours(15.0);

		ISirhWSConsumer wsMock = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(wsMock.isUtilisateurSIRH(idAgent)).thenReturn(result);
		Mockito.when(wsMock.getAgent(compteurDto.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculAlimManuelleCompteur(compteurDto)).thenReturn(10.0);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(false);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounterByDate(AgentAsaA54Count.class, compteurDto.getIdAgent(), compteurDto.getDateDebut()))
				.thenReturn(arc);
		Mockito.when(counterRepository.getEntity(MotifCompteur.class, compteurDto.getMotifCompteurDto().getIdMotifCompteur())).thenReturn(null);

		AsaA54CounterServiceImpl service = new AsaA54CounterServiceImpl();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		result = service.majManuelleCompteurToAgent(idAgent, compteurDto, false);

		assertEquals(1, result.getErrors().size());
		assertEquals("Le motif n'existe pas.", result.getErrors().get(0).toString());
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentAsaA54Count.class));
	}

	@Test
	public void majManuelleCompteurAsaA54ToAgent_agentInexistant() {

		super.service = new AsaA54CounterServiceImpl();
		super.majManuelleCompteurToAgent_prepareData();

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		CompteurDto compteurDto = new CompteurDto();
		compteurDto.setIdAgent(9005151);
		compteurDto.setDureeAAjouter(10.0);
		compteurDto.setDateDebut(new DateTime(2013, 4, 2, 0, 0, 0).toDate());
		MotifCompteurDto motifDto = new MotifCompteurDto();
		motifDto.setIdMotifCompteur(1);
		compteurDto.setMotifCompteurDto(motifDto);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(false);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculAlimManuelleCompteur(compteurDto)).thenReturn(10.0);

		ISirhWSConsumer wsMock = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(wsMock.isUtilisateurSIRH(idAgent)).thenReturn(result);
		Mockito.when(wsMock.getAgent(compteurDto.getIdAgent())).thenReturn(null);

		AsaA54CounterServiceImpl service = new AsaA54CounterServiceImpl();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		boolean isAgentNotFoundException = false;
		try {
			service.majManuelleCompteurToAgent(idAgent, compteurDto, false);
		} catch (AgentNotFoundException e) {
			isAgentNotFoundException = true;
		}

		assertTrue(isAgentNotFoundException);
	}

	@Test
	public void majManuelleCompteurAsaA54ToAgent_OK_avecCompteurExistant() {

		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		CompteurDto compteurDto = new CompteurDto();
		compteurDto.setIdAgent(9005151);
		compteurDto.setDureeAAjouter(10.0);
		MotifCompteurDto motifDto = new MotifCompteurDto();
		motifDto.setIdMotifCompteur(1);
		compteurDto.setMotifCompteurDto(motifDto);
		compteurDto.setDateDebut(new DateTime(2013, 1, 1, 0, 0, 0).toDate());
		compteurDto.setDateDebut(new DateTime(2013, 12, 31, 0, 0, 0).toDate());

		AgentAsaA54Count arc = new AgentAsaA54Count();
		arc.setTotalJours(15.0);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(true);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculAlimManuelleCompteur(compteurDto)).thenReturn(-10.0);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounterByDate(AgentAsaA54Count.class, compteurDto.getIdAgent(), compteurDto.getDateDebut()))
				.thenReturn(arc);
		Mockito.when(counterRepository.getEntity(MotifCompteur.class, compteurDto.getMotifCompteurDto().getIdMotifCompteur()))
				.thenReturn(new MotifCompteur());

		ISirhWSConsumer wsMock = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(wsMock.isUtilisateurSIRH(idAgent)).thenReturn(result);
		Mockito.when(wsMock.getAgent(compteurDto.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		AsaA54CounterServiceImpl service = new AsaA54CounterServiceImpl();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);

		result = service.majManuelleCompteurToAgent(idAgent, compteurDto, false);

		assertEquals(0, result.getErrors().size());

		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentAsaA54Count.class));
	}

	@Test
	public void majManuelleCompteurAsaA54ToAgent_OK_sansCompteurExistant() {

		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		CompteurDto compteurDto = new CompteurDto();
		compteurDto.setIdAgent(9005151);
		compteurDto.setDureeAAjouter(10.0);
		MotifCompteurDto motifDto = new MotifCompteurDto();
		motifDto.setIdMotifCompteur(1);
		compteurDto.setMotifCompteurDto(motifDto);
		compteurDto.setDateDebut(new DateTime(2013, 1, 1, 0, 0, 0).toDate());
		compteurDto.setDateDebut(new DateTime(2013, 12, 31, 0, 0, 0).toDate());

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(false);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculAlimManuelleCompteur(compteurDto)).thenReturn(10.0);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounterByDate(AgentAsaA54Count.class, compteurDto.getIdAgent(), compteurDto.getDateDebut()))
				.thenReturn(null);
		Mockito.when(counterRepository.getEntity(MotifCompteur.class, compteurDto.getMotifCompteurDto().getIdMotifCompteur()))
				.thenReturn(new MotifCompteur());

		ISirhWSConsumer wsMock = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(wsMock.isUtilisateurSIRH(idAgent)).thenReturn(result);
		Mockito.when(wsMock.getAgent(compteurDto.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		AsaA54CounterServiceImpl service = new AsaA54CounterServiceImpl();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);

		result = service.majManuelleCompteurToAgent(idAgent, compteurDto, false);

		assertEquals(0, result.getErrors().size());

		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentAsaA54Count.class));
	}

	@Test
	public void getListeCompteur_noResult() {

		List<CompteurDto> result = new ArrayList<CompteurDto>();

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getListCounterByAnnee(AgentAsaA54Count.class, null)).thenReturn(new ArrayList<AgentAsaA54Count>());

		AsaA54CounterServiceImpl service = new AsaA54CounterServiceImpl();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		result = service.getListeCompteur(null, null);

		assertEquals(0, result.size());

		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentAsaA54Count.class));
	}

	@Test
	public void getListeCompteur_1Result() {

		List<CompteurDto> result = new ArrayList<CompteurDto>();
		AgentAsaA54Count e = new AgentAsaA54Count();
		e.setIdAgent(9005138);
		e.setTotalJours(12.0);
		List<AgentAsaA54Count> list = new ArrayList<AgentAsaA54Count>();
		list.add(e);

		IOrganisationSyndicaleRepository OSRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(OSRepository.getAgentA54Organisation(e.getIdAgent())).thenReturn(null);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getListCounterByAnnee(AgentAsaA54Count.class, null, null, null)).thenReturn(list);

		AsaA54CounterServiceImpl service = new AsaA54CounterServiceImpl();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "OSRepository", OSRepository);

		result = service.getListeCompteur(null, null);

		assertEquals(1, result.size());
		assertEquals(12, result.get(0).getDureeAAjouter().intValue());

		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentAsaA54Count.class));
	}

	@Test
	public void getListeCompteur_WithYear_1Result() {

		List<CompteurDto> result = new ArrayList<CompteurDto>();
		AgentAsaA54Count e = new AgentAsaA54Count();
		e.setIdAgent(9005138);
		e.setTotalJours(12.0);
		List<AgentAsaA54Count> list = new ArrayList<AgentAsaA54Count>();
		list.add(e);

		IOrganisationSyndicaleRepository OSRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(OSRepository.getAgentA54Organisation(e.getIdAgent())).thenReturn(null);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getListCounterByAnnee(AgentAsaA54Count.class, 2015, null, null)).thenReturn(list);

		AsaA54CounterServiceImpl service = new AsaA54CounterServiceImpl();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "OSRepository", OSRepository);

		result = service.getListeCompteur(null, 2015);

		assertEquals(1, result.size());
		assertEquals(12, result.get(0).getDureeAAjouter().intValue());

		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentAsaA54Count.class));
	}

	@Test
	public void getListeCompteur_WithYear_WithOS_1Result() {

		List<CompteurDto> result = new ArrayList<CompteurDto>();
		AgentAsaA54Count e = new AgentAsaA54Count();
		e.setIdAgent(9005138);
		e.setTotalJours(12.0);
		List<AgentAsaA54Count> list = new ArrayList<AgentAsaA54Count>();
		list.add(e);

		OrganisationSyndicale orga = new OrganisationSyndicale();
		orga.setIdOrganisationSyndicale(1);

		AgentA54OrganisationSyndicale agOrga = new AgentA54OrganisationSyndicale();
		agOrga.setIdAgent(e.getIdAgent());
		agOrga.setOrganisationSyndicale(orga);

		orga.getAgentsA54().add(agOrga);

		IOrganisationSyndicaleRepository OSRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(OSRepository.getAgentA54Organisation(e.getIdAgent())).thenReturn(null);
		Mockito.when(OSRepository.getAgentA54OrganisationByOS(1)).thenReturn(orga.getAgentsA54());

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounterByDate(AgentAsaA54Count.class, agOrga.getIdAgent(), new DateTime(2015, 1, 1, 0, 0, 0).toDate()))
				.thenReturn(e);

		AsaA54CounterServiceImpl service = new AsaA54CounterServiceImpl();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "OSRepository", OSRepository);

		result = service.getListeCompteur(1, 2015);

		assertEquals(1, result.size());
		assertEquals(12, result.get(0).getDureeAAjouter().intValue());

		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentAsaA54Count.class));
	}

	@Test
	public void majCompteurAsaA54ToAgent_compteurInexistant() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());

		ReturnMessageDto result = new ReturnMessageDto();
		DemandeAsa demande = new DemandeAsa();
		demande.setIdAgent(9008765);
		demande.setDateDebut(new Date());
		demande.setDateFin(new Date());
		Double minutes = 10.0;

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(demande.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
		Mockito.when(rr.getAgentCounter(AgentAsaA54Count.class, demande.getIdAgent())).thenReturn(null);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreJoursArrondiDemiJournee(Mockito.isA(Date.class), Mockito.isA(Date.class))).thenReturn(minutes);

		AsaA54CounterServiceImpl service = new AsaA54CounterServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "helperService", helperService);

		result = service.majCompteurToAgent(result, demande, demandeEtatChangeDto);

		assertEquals(1, result.getErrors().size());
		Mockito.verify(rr, Mockito.times(0)).persistEntity(Mockito.isA(AgentCount.class));
	}

	@Test
	public void majCompteurAsaA54ToAgent_compteurNegatif_debit() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());

		ReturnMessageDto result = new ReturnMessageDto();
		DemandeAsa demande = new DemandeAsa();
		demande.setIdAgent(9008765);
		demande.setDateDebut(new Date());
		demande.setDateFin(new Date());
		Double minutes = 11.0;

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(demande.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
		AgentAsaA54Count arc = new AgentAsaA54Count();
		arc.setTotalJours(10.0);
		Mockito.when(rr.getAgentCounterByDate(AgentAsaA54Count.class, demande.getIdAgent(), demande.getDateDebut())).thenReturn(arc);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreJoursArrondiDemiJournee(Mockito.isA(Date.class), Mockito.isA(Date.class))).thenReturn(minutes);

		AsaA54CounterServiceImpl service = new AsaA54CounterServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "helperService", helperService);

		result = service.majCompteurToAgent(result, demande, demandeEtatChangeDto);

		assertEquals(1, result.getInfos().size());
		assertEquals("Le solde du compteur de l'agent est négatif.", result.getInfos().get(0));
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(AgentCount.class));
	}

	@Test
	public void majCompteurAsaA54ToAgent_debitOk() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());

		ReturnMessageDto result = new ReturnMessageDto();
		DemandeAsa demande = new DemandeAsa();
		demande.setIdAgent(9008765);
		demande.setDateDebut(new Date());
		demande.setDateFin(new Date());
		Double minutes = 11.0;

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(demande.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
		AgentAsaA54Count arc = new AgentAsaA54Count();
		arc.setTotalJours(12.0);
		Mockito.when(rr.getAgentCounterByDate(AgentAsaA54Count.class, demande.getIdAgent(), demande.getDateDebut())).thenReturn(arc);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreJoursArrondiDemiJournee(Mockito.isA(Date.class), Mockito.isA(Date.class))).thenReturn(minutes);

		AsaA54CounterServiceImpl service = new AsaA54CounterServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "helperService", helperService);

		result = service.majCompteurToAgent(result, demande, demandeEtatChangeDto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(AgentCount.class));
	}

	@Test
	public void majManuelleCompteurAsaA54ToListAgent_NonHabilite() {

		ReturnMessageDto result = new ReturnMessageDto();
		result.getErrors().add("L'agent 9005138 n'existe pas dans l'AD.");
		Integer idAgent = 9005138;
		List<CompteurDto> liste = new ArrayList<>();
		CompteurDto compteurDto = new CompteurDto();
		compteurDto.setIdAgent(9005151);
		compteurDto.setDateDebut(new DateTime(2013, 4, 2, 0, 0, 0).toDate());
		liste.add(compteurDto);

		ISirhWSConsumer wsMock = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(wsMock.isUtilisateurSIRH(idAgent)).thenReturn(result);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculAlimManuelleCompteur(compteurDto)).thenReturn(10.0);

		AsaA54CounterServiceImpl service = new AsaA54CounterServiceImpl();
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);

		result = service.majManuelleCompteurToListAgent(idAgent, liste, true);

		assertEquals(1, result.getErrors().size());
		assertEquals("Vous n'êtes pas habilité à mettre à jour le compteur de cet agent.", result.getErrors().get(0).toString());
	}

	@Test
	public void majManuelleCompteurAsaA54ToListAgent_UtilisateurSIRHHabilite_MotifCompteurInexistant() {

		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		List<CompteurDto> liste = new ArrayList<>();
		CompteurDto compteurDto = new CompteurDto();
		MotifCompteurDto motifCompteurDto = new MotifCompteurDto();
		motifCompteurDto.setIdMotifCompteur(1);
		compteurDto.setMotifCompteurDto(motifCompteurDto);
		compteurDto.setIdAgent(9005151);
		compteurDto.setDureeAAjouter(10.0);
		compteurDto.setDateDebut(new DateTime(2013, 1, 1, 0, 0, 0).toDate());
		compteurDto.setDateDebut(new DateTime(2013, 12, 31, 0, 0, 0).toDate());
		liste.add(compteurDto);

		AgentAsaA54Count arc = new AgentAsaA54Count();
		arc.setTotalJours(15.0);

		ISirhWSConsumer wsMock = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(wsMock.isUtilisateurSIRH(idAgent)).thenReturn(result);
		Mockito.when(wsMock.getAgent(compteurDto.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculAlimManuelleCompteur(compteurDto)).thenReturn(10.0);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounterByDate(AgentAsaA54Count.class, compteurDto.getIdAgent(), compteurDto.getDateDebut()))
				.thenReturn(arc);
		Mockito.when(counterRepository.getEntity(MotifCompteur.class, compteurDto.getMotifCompteurDto().getIdMotifCompteur())).thenReturn(null);

		AsaA54CounterServiceImpl service = new AsaA54CounterServiceImpl();
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		result = service.majManuelleCompteurToListAgent(idAgent, liste, true);

		assertEquals(1, result.getErrors().size());
		assertEquals("Le motif n'existe pas.", result.getErrors().get(0).toString());
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentAsaA54Count.class));
	}

	@Test
	public void majManuelleCompteurAsaA54ToListAgent_agentInexistant() {

		super.service = new AsaA54CounterServiceImpl();
		super.majManuelleCompteurToAgent_prepareData();

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		List<CompteurDto> liste = new ArrayList<>();
		CompteurDto compteurDto = new CompteurDto();
		compteurDto.setIdAgent(9005151);
		compteurDto.setDureeAAjouter(10.0);
		compteurDto.setDateDebut(new DateTime(2013, 4, 2, 0, 0, 0).toDate());
		MotifCompteurDto motifDto = new MotifCompteurDto();
		motifDto.setIdMotifCompteur(1);
		compteurDto.setMotifCompteurDto(motifDto);
		liste.add(compteurDto);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculAlimManuelleCompteur(compteurDto)).thenReturn(10.0);

		ISirhWSConsumer wsMock = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(wsMock.isUtilisateurSIRH(idAgent)).thenReturn(result);
		Mockito.when(wsMock.getAgent(compteurDto.getIdAgent())).thenReturn(null);

		AsaA54CounterServiceImpl service = new AsaA54CounterServiceImpl();
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		boolean isAgentNotFoundException = false;
		try {
			service.majManuelleCompteurToListAgent(idAgent, liste, true);
		} catch (AgentNotFoundException e) {
			isAgentNotFoundException = true;
		}

		assertTrue(isAgentNotFoundException);
	}

	@Test
	public void majManuelleCompteurAsaA54ToListAgent_1KO_avecCompteurExistant_1OKavecCompteurInexistant() {

		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		MotifCompteurDto motifDto = new MotifCompteurDto();
		motifDto.setIdMotifCompteur(1);
		List<CompteurDto> liste = new ArrayList<>();
		CompteurDto compteurDto2 = new CompteurDto();
		compteurDto2.setIdAgent(9005158);
		compteurDto2.setDureeAAjouter(10.0);
		compteurDto2.setMotifCompteurDto(motifDto);
		compteurDto2.setDateDebut(new DateTime(2013, 1, 1, 0, 0, 0).toDate());
		compteurDto2.setDateDebut(new DateTime(2013, 12, 31, 0, 0, 0).toDate());
		CompteurDto compteurDto1 = new CompteurDto();
		compteurDto1.setIdAgent(9005151);
		compteurDto1.setDureeAAjouter(10.0);
		compteurDto1.setMotifCompteurDto(motifDto);
		compteurDto1.setDateDebut(new DateTime(2013, 1, 1, 0, 0, 0).toDate());
		compteurDto1.setDateDebut(new DateTime(2013, 12, 31, 0, 0, 0).toDate());
		liste.add(compteurDto1);
		liste.add(compteurDto2);

		AgentAsaA54Count arc = new AgentAsaA54Count();
		arc.setTotalJours(15.0);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculAlimManuelleCompteur(compteurDto1)).thenReturn(-10.0);
		Mockito.when(helperService.calculAlimManuelleCompteur(compteurDto2)).thenReturn(-10.0);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounterByDate(AgentAsaA54Count.class, compteurDto1.getIdAgent(), compteurDto1.getDateDebut()))
				.thenReturn(arc);
		Mockito.when(counterRepository.getAgentCounterByDate(AgentAsaA54Count.class, compteurDto2.getIdAgent(), compteurDto2.getDateDebut()))
				.thenReturn(null);
		Mockito.when(counterRepository.getEntity(MotifCompteur.class, compteurDto1.getMotifCompteurDto().getIdMotifCompteur()))
				.thenReturn(new MotifCompteur());
		Mockito.when(counterRepository.getEntity(MotifCompteur.class, compteurDto2.getMotifCompteurDto().getIdMotifCompteur()))
				.thenReturn(new MotifCompteur());

		ISirhWSConsumer wsMock = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(wsMock.isUtilisateurSIRH(idAgent)).thenReturn(result);
		Mockito.when(wsMock.getAgent(compteurDto1.getIdAgent())).thenReturn(new AgentGeneriqueDto());
		Mockito.when(wsMock.getAgent(compteurDto2.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		AsaA54CounterServiceImpl service = new AsaA54CounterServiceImpl();
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);

		result = service.majManuelleCompteurToListAgent(idAgent, liste, true);

		assertEquals(1, result.getErrors().size());
		assertEquals("Le compteur existe déjà pour l'agent 9005151.", result.getErrors().get(0).toString());

		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentAsaA54Count.class));
	}

	@Test
	public void majManuelleCompteurAsaA54ToListAgent_KO_avecCompteurExistant() {

		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		List<CompteurDto> liste = new ArrayList<>();
		CompteurDto compteurDto = new CompteurDto();
		compteurDto.setIdAgent(9005151);
		compteurDto.setDureeAAjouter(10.0);
		MotifCompteurDto motifDto = new MotifCompteurDto();
		motifDto.setIdMotifCompteur(1);
		compteurDto.setMotifCompteurDto(motifDto);
		compteurDto.setDateDebut(new DateTime(2013, 1, 1, 0, 0, 0).toDate());
		compteurDto.setDateDebut(new DateTime(2013, 12, 31, 0, 0, 0).toDate());
		liste.add(compteurDto);

		AgentAsaA54Count arc = new AgentAsaA54Count();
		arc.setTotalJours(15.0);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculAlimManuelleCompteur(compteurDto)).thenReturn(-10.0);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounterByDate(AgentAsaA54Count.class, compteurDto.getIdAgent(), compteurDto.getDateDebut()))
				.thenReturn(arc);
		Mockito.when(counterRepository.getEntity(MotifCompteur.class, compteurDto.getMotifCompteurDto().getIdMotifCompteur()))
				.thenReturn(new MotifCompteur());

		ISirhWSConsumer wsMock = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(wsMock.isUtilisateurSIRH(idAgent)).thenReturn(result);
		Mockito.when(wsMock.getAgent(compteurDto.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		AsaA54CounterServiceImpl service = new AsaA54CounterServiceImpl();
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);

		result = service.majManuelleCompteurToListAgent(idAgent, liste, true);

		assertEquals(1, result.getErrors().size());
		assertEquals("Le compteur existe déjà pour l'agent 9005151.", result.getErrors().get(0).toString());

		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentAsaA54Count.class));
	}

	@Test
	public void majManuelleCompteurAsaA54ToListAgent_OK_avecCompteurInexistant() {

		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		List<CompteurDto> liste = new ArrayList<>();
		CompteurDto compteurDto = new CompteurDto();
		compteurDto.setIdAgent(9005151);
		compteurDto.setDureeAAjouter(10.0);
		MotifCompteurDto motifDto = new MotifCompteurDto();
		motifDto.setIdMotifCompteur(1);
		compteurDto.setMotifCompteurDto(motifDto);
		compteurDto.setDateDebut(new DateTime(2013, 1, 1, 0, 0, 0).toDate());
		compteurDto.setDateDebut(new DateTime(2013, 12, 31, 0, 0, 0).toDate());
		liste.add(compteurDto);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculAlimManuelleCompteur(compteurDto)).thenReturn(-10.0);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounterByDate(AgentAsaA54Count.class, compteurDto.getIdAgent(), compteurDto.getDateDebut()))
				.thenReturn(null);
		Mockito.when(counterRepository.getEntity(MotifCompteur.class, compteurDto.getMotifCompteurDto().getIdMotifCompteur()))
				.thenReturn(new MotifCompteur());

		ISirhWSConsumer wsMock = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(wsMock.isUtilisateurSIRH(idAgent)).thenReturn(result);
		Mockito.when(wsMock.getAgent(compteurDto.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		AsaA54CounterServiceImpl service = new AsaA54CounterServiceImpl();
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);

		result = service.majManuelleCompteurToListAgent(idAgent, liste, true);

		assertEquals(0, result.getErrors().size());

		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentAsaA54Count.class));
	}

	@Test
	public void majManuelleCompteurAsaA54ToListAgent_OK_sansCompteurExistant() {

		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		List<CompteurDto> liste = new ArrayList<>();
		CompteurDto compteurDto = new CompteurDto();
		compteurDto.setIdAgent(9005151);
		compteurDto.setDureeAAjouter(10.0);
		liste.add(compteurDto);
		MotifCompteurDto motifDto = new MotifCompteurDto();
		motifDto.setIdMotifCompteur(1);
		compteurDto.setMotifCompteurDto(motifDto);
		compteurDto.setDateDebut(new DateTime(2013, 1, 1, 0, 0, 0).toDate());
		compteurDto.setDateDebut(new DateTime(2013, 12, 31, 0, 0, 0).toDate());

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculAlimManuelleCompteur(compteurDto)).thenReturn(10.0);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounterByDate(AgentAsaA54Count.class, compteurDto.getIdAgent(), compteurDto.getDateDebut()))
				.thenReturn(null);
		Mockito.when(counterRepository.getEntity(MotifCompteur.class, compteurDto.getMotifCompteurDto().getIdMotifCompteur()))
				.thenReturn(new MotifCompteur());

		ISirhWSConsumer wsMock = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(wsMock.isUtilisateurSIRH(idAgent)).thenReturn(result);
		Mockito.when(wsMock.getAgent(compteurDto.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		AsaA54CounterServiceImpl service = new AsaA54CounterServiceImpl();
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);

		result = service.majManuelleCompteurToListAgent(idAgent, liste, true);

		assertEquals(0, result.getErrors().size());

		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentAsaA54Count.class));
	}

	@Test
	public void saveRepresentantA54_OSInexistant() {

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(1);
		organisationSyndicale.setActif(true);

		IOrganisationSyndicaleRepository OSRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(OSRepository.getEntity(OrganisationSyndicale.class, organisationSyndicale.getIdOrganisationSyndicale())).thenReturn(null);

		AsaA54CounterServiceImpl service = new AsaA54CounterServiceImpl();
		ReflectionTestUtils.setField(service, "OSRepository", OSRepository);

		ReturnMessageDto result = service.saveRepresentantA54(organisationSyndicale.getIdOrganisationSyndicale(), null);

		assertEquals(1, result.getErrors().size());
		assertEquals("L'organisation syndicale n'existe pas.", result.getErrors().get(0));
	}

	@Test
	public void saveRepresentantA54_OSInactif() {

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(1);
		organisationSyndicale.setActif(false);

		IOrganisationSyndicaleRepository OSRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(OSRepository.getEntity(OrganisationSyndicale.class, organisationSyndicale.getIdOrganisationSyndicale()))
				.thenReturn(organisationSyndicale);

		AsaA54CounterServiceImpl service = new AsaA54CounterServiceImpl();
		ReflectionTestUtils.setField(service, "OSRepository", OSRepository);

		ReturnMessageDto result = service.saveRepresentantA54(organisationSyndicale.getIdOrganisationSyndicale(), null);

		assertEquals(1, result.getErrors().size());
		assertEquals("L'organisation syndicale n'est pas active.", result.getErrors().get(0));
	}

	@Test
	public void saveRepresentantA54_Insert_Ok() {

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(1);
		organisationSyndicale.setActif(true);

		List<AgentA54OrganisationSyndicale> listAg = new ArrayList<>();

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);

		IOrganisationSyndicaleRepository OSRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(OSRepository.getEntity(OrganisationSyndicale.class, organisationSyndicale.getIdOrganisationSyndicale()))
				.thenReturn(organisationSyndicale);
		Mockito.when(OSRepository.getAgentA54Organisation(9005138)).thenReturn(listAg);

		AsaA54CounterServiceImpl service = new AsaA54CounterServiceImpl();
		ReflectionTestUtils.setField(service, "OSRepository", OSRepository);
		ReflectionTestUtils.setField(service, "counterRepository", rr);

		ReturnMessageDto result = service.saveRepresentantA54(organisationSyndicale.getIdOrganisationSyndicale(), 9005138);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(AgentA54OrganisationSyndicale.class));
	}

	@Test
	public void saveRepresentantA54_Update_Ok() {

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(2);
		organisationSyndicale.setActif(true);

		AgentA54OrganisationSyndicale ag = new AgentA54OrganisationSyndicale();
		ag.setIdAgent(9005138);
		ag.setOrganisationSyndicale(organisationSyndicale);
		List<AgentA54OrganisationSyndicale> listAg = new ArrayList<>();
		listAg.add(ag);

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);

		IOrganisationSyndicaleRepository OSRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(OSRepository.getEntity(OrganisationSyndicale.class, organisationSyndicale.getIdOrganisationSyndicale()))
				.thenReturn(organisationSyndicale);
		Mockito.when(OSRepository.getAgentA54Organisation(9005138)).thenReturn(listAg);

		AsaA54CounterServiceImpl service = new AsaA54CounterServiceImpl();
		ReflectionTestUtils.setField(service, "OSRepository", OSRepository);
		ReflectionTestUtils.setField(service, "counterRepository", rr);

		ReturnMessageDto result = service.saveRepresentantA54(organisationSyndicale.getIdOrganisationSyndicale(), 9005138);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(AgentA54OrganisationSyndicale.class));
	}

	@Test
	public void saveRepresentantA54_Update_NonOk_AgentActif() {

		OrganisationSyndicale organisationSyndicaleDto = new OrganisationSyndicale();
		organisationSyndicaleDto.setIdOrganisationSyndicale(2);
		organisationSyndicaleDto.setActif(true);

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(1);
		organisationSyndicale.setActif(true);

		AgentA54OrganisationSyndicale ag = new AgentA54OrganisationSyndicale();
		ag.setIdAgent(9005138);
		ag.setOrganisationSyndicale(organisationSyndicaleDto);
		List<AgentA54OrganisationSyndicale> listAg = new ArrayList<>();
		listAg.add(ag);
		listAg.add(ag);

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);

		IOrganisationSyndicaleRepository OSRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(OSRepository.getEntity(OrganisationSyndicale.class, organisationSyndicale.getIdOrganisationSyndicale()))
				.thenReturn(organisationSyndicale);
		Mockito.when(OSRepository.getAgentA54Organisation(9005138)).thenReturn(listAg);

		AsaA54CounterServiceImpl service = new AsaA54CounterServiceImpl();
		ReflectionTestUtils.setField(service, "OSRepository", OSRepository);
		ReflectionTestUtils.setField(service, "counterRepository", rr);

		ReturnMessageDto result = service.saveRepresentantA54(organisationSyndicale.getIdOrganisationSyndicale(), 9005138);

		assertEquals(1, result.getErrors().size());
		assertEquals("L'agent [9005138] fait déja partie d'une autre organisation syndicale.", result.getErrors().get(0));
	}

	@Test
	public void saveRepresentantA54_DeleteKo() {

		OrganisationSyndicale organisationSyndicaleDto = new OrganisationSyndicale();
		organisationSyndicaleDto.setIdOrganisationSyndicale(2);
		organisationSyndicaleDto.setActif(true);

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(1);
		organisationSyndicale.setActif(true);

		AgentA54OrganisationSyndicale ag2 = new AgentA54OrganisationSyndicale();
		ag2.setIdAgent(9003069);
		AgentA54OrganisationSyndicale ag = new AgentA54OrganisationSyndicale();
		ag.setIdAgent(9005138);
		ag.setOrganisationSyndicale(organisationSyndicaleDto);

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);

		IOrganisationSyndicaleRepository OSRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(OSRepository.getEntity(OrganisationSyndicale.class, organisationSyndicaleDto.getIdOrganisationSyndicale()))
				.thenReturn(organisationSyndicale);
		Mockito.when(OSRepository.getAgentA54Organisation(9005138)).thenReturn(Arrays.asList(ag, ag2));

		AsaA54CounterServiceImpl service = new AsaA54CounterServiceImpl();
		ReflectionTestUtils.setField(service, "OSRepository", OSRepository);
		ReflectionTestUtils.setField(service, "counterRepository", rr);

		ReturnMessageDto result = service.saveRepresentantA54(0, 9005138);

		assertEquals(1, result.getErrors().size());
		assertEquals("L'agent [9005138] fait déja partie d'une autre organisation syndicale.", result.getErrors().get(0));
	}

	@Test
	public void saveRepresentantA54_DeleteOk() {

		OrganisationSyndicale organisationSyndicaleDto = new OrganisationSyndicale();
		organisationSyndicaleDto.setIdOrganisationSyndicale(2);
		organisationSyndicaleDto.setActif(true);

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(1);
		organisationSyndicale.setActif(true);

		AgentA54OrganisationSyndicale ag2 = new AgentA54OrganisationSyndicale();
		ag2.setIdAgent(9003069);
		AgentA54OrganisationSyndicale ag = new AgentA54OrganisationSyndicale();
		ag.setIdAgent(9005138);
		ag.setOrganisationSyndicale(organisationSyndicaleDto);

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);

		IOrganisationSyndicaleRepository OSRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(OSRepository.getEntity(OrganisationSyndicale.class, organisationSyndicaleDto.getIdOrganisationSyndicale()))
				.thenReturn(organisationSyndicale);
		Mockito.when(OSRepository.getAgentA54Organisation(9005138)).thenReturn(Arrays.asList(ag));

		AsaA54CounterServiceImpl service = new AsaA54CounterServiceImpl();
		ReflectionTestUtils.setField(service, "OSRepository", OSRepository);
		ReflectionTestUtils.setField(service, "counterRepository", rr);

		ReturnMessageDto result = service.saveRepresentantA54(0, 9005138);

		assertEquals(0, result.getErrors().size());
	}
}
