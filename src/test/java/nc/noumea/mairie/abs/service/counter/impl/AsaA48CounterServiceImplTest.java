package nc.noumea.mairie.abs.service.counter.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import nc.noumea.mairie.abs.domain.AgentA48OrganisationSyndicale;
import nc.noumea.mairie.abs.domain.AgentAsaA48Count;
import nc.noumea.mairie.abs.domain.AgentCount;
import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;
import nc.noumea.mairie.abs.domain.DemandeAsa;
import nc.noumea.mairie.abs.domain.MotifCompteur;
import nc.noumea.mairie.abs.domain.OrganisationSyndicale;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.AgentOrganisationSyndicaleDto;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.MotifCompteurDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IAccessRightsRepository;
import nc.noumea.mairie.abs.repository.ICounterRepository;
import nc.noumea.mairie.abs.repository.IOrganisationSyndicaleRepository;
import nc.noumea.mairie.abs.service.impl.HelperService;
import nc.noumea.mairie.ws.ISirhWSConsumer;

public class AsaA48CounterServiceImplTest extends AsaCounterServiceImplTest {

	@Test
	public void testMethodeParenteHeritage() throws Throwable {

		super.service = new AsaA48CounterServiceImpl();
		super.allTest();
	}

	@Test
	public void majManuelleCompteurAsaA48ToAgent_NonHabilite() {

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

		AsaA48CounterServiceImpl service = new AsaA48CounterServiceImpl();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);

		result = service.majManuelleCompteurToAgent(idAgent, compteurDto, false);

		assertEquals(1, result.getErrors().size());
		assertEquals("Vous n'êtes pas habilité à mettre à jour le compteur de cet agent.", result.getErrors().get(0).toString());
	}

	@Test
	public void majManuelleCompteurAsaA48ToAgent_UtilisateurSIRHHabilite_MotifCompteurInexistant() {

		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		CompteurDto compteurDto = new CompteurDto();
		compteurDto.setIdAgent(9005151);
		compteurDto.setDureeAAjouter(10.0);
		compteurDto.setDateDebut(new DateTime(2013, 1, 1, 0, 0, 0).toDate());
		compteurDto.setDateDebut(new DateTime(2013, 12, 31, 0, 0, 0).toDate());

		AgentAsaA48Count arc = new AgentAsaA48Count();
		arc.setTotalJours(15.0);

		ISirhWSConsumer wsMock = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(wsMock.isUtilisateurSIRH(idAgent)).thenReturn(result);
		Mockito.when(wsMock.getAgent(compteurDto.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculAlimManuelleCompteur(compteurDto)).thenReturn(10.0);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(false);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounterByDate(AgentAsaA48Count.class, compteurDto.getIdAgent(), compteurDto.getDateDebut()))
				.thenReturn(arc);

		AsaA48CounterServiceImpl service = new AsaA48CounterServiceImpl();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		result = service.majManuelleCompteurToAgent(idAgent, compteurDto, false);

		assertEquals(1, result.getErrors().size());
		assertEquals("Le motif n'existe pas.", result.getErrors().get(0).toString());
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentAsaA48Count.class));
	}

	@Test
	public void majManuelleCompteurAsaA48ToAgent_OK_avecCompteurExistant() {

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

		AgentAsaA48Count arc = new AgentAsaA48Count();
		arc.setTotalJours(15.0);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(true);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculAlimManuelleCompteur(compteurDto)).thenReturn(-10.0);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounterByDate(AgentAsaA48Count.class, compteurDto.getIdAgent(), compteurDto.getDateDebut()))
				.thenReturn(arc);
		Mockito.when(counterRepository.getEntity(MotifCompteur.class, compteurDto.getMotifCompteurDto().getIdMotifCompteur()))
				.thenReturn(new MotifCompteur());

		ISirhWSConsumer wsMock = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(wsMock.isUtilisateurSIRH(idAgent)).thenReturn(result);
		Mockito.when(wsMock.getAgent(compteurDto.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		AsaA48CounterServiceImpl service = new AsaA48CounterServiceImpl();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);

		result = service.majManuelleCompteurToAgent(idAgent, compteurDto, false);

		assertEquals(0, result.getErrors().size());

		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentAsaA48Count.class));
	}

	@Test
	public void majManuelleCompteurAsaA48ToAgent_OK_sansCompteurExistant() {

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
		Mockito.when(counterRepository.getAgentCounterByDate(AgentAsaA48Count.class, compteurDto.getIdAgent(), compteurDto.getDateDebut()))
				.thenReturn(null);
		Mockito.when(counterRepository.getEntity(MotifCompteur.class, compteurDto.getMotifCompteurDto().getIdMotifCompteur()))
				.thenReturn(new MotifCompteur());

		ISirhWSConsumer wsMock = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(wsMock.isUtilisateurSIRH(idAgent)).thenReturn(result);
		Mockito.when(wsMock.getAgent(compteurDto.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		AsaA48CounterServiceImpl service = new AsaA48CounterServiceImpl();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);

		result = service.majManuelleCompteurToAgent(idAgent, compteurDto, false);

		assertEquals(0, result.getErrors().size());

		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentAsaA48Count.class));
	}

	@Test
	public void getListeCompteur_noResult() {

		List<CompteurDto> result = new ArrayList<CompteurDto>();

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getListCounterByAnnee(AgentAsaA48Count.class, null)).thenReturn(new ArrayList<AgentAsaA48Count>());

		AsaA48CounterServiceImpl service = new AsaA48CounterServiceImpl();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		result = service.getListeCompteur(null, null);

		assertEquals(0, result.size());

		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentAsaA48Count.class));
	}

	@Test
	public void getListeCompteur_WithYear_1Result() {

		List<CompteurDto> result = new ArrayList<CompteurDto>();
		AgentAsaA48Count e = new AgentAsaA48Count();
		e.setIdAgent(9005138);
		e.setTotalJours(12.0);
		List<AgentAsaA48Count> list = new ArrayList<AgentAsaA48Count>();
		list.add(e);

		IOrganisationSyndicaleRepository OSRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(OSRepository.getAgentA48Organisation(e.getIdAgent())).thenReturn(null);
		
		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getListCounterByAnnee(AgentAsaA48Count.class, 2015)).thenReturn(list);

		AsaA48CounterServiceImpl service = new AsaA48CounterServiceImpl();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "OSRepository", OSRepository);

		result = service.getListeCompteur(null, 2015);

		assertEquals(1, result.size());
		assertEquals(12, result.get(0).getDureeAAjouter().intValue());

		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentAsaA48Count.class));
	}

	@Test
	public void getListeCompteur_1Result() {

		List<CompteurDto> result = new ArrayList<CompteurDto>();
		AgentAsaA48Count e = new AgentAsaA48Count();
		e.setIdAgent(9005138);
		e.setTotalJours(12.0);
		List<AgentAsaA48Count> list = new ArrayList<AgentAsaA48Count>();
		list.add(e);

		IOrganisationSyndicaleRepository OSRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(OSRepository.getAgentA48Organisation(e.getIdAgent())).thenReturn(null);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getListCounterByAnnee(AgentAsaA48Count.class, null)).thenReturn(list);

		AsaA48CounterServiceImpl service = new AsaA48CounterServiceImpl();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "OSRepository", OSRepository);

		result = service.getListeCompteur(null, null);

		assertEquals(1, result.size());
		assertEquals(12, result.get(0).getDureeAAjouter().intValue());

		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentAsaA48Count.class));
	}

	@Test
	public void majCompteurAsaA48ToAgent_compteurInexistant() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());

		ReturnMessageDto result = new ReturnMessageDto();
		DemandeAsa demande = new DemandeAsa();
		demande.setIdAgent(9008765);
		demande.setDateDebut(new Date());
		demande.setDateFin(new Date());
		Double minutes = 10.0;

		ISirhWSConsumer sR = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sR.getAgent(demande.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
		Mockito.when(rr.getAgentCounter(AgentAsaA48Count.class, demande.getIdAgent())).thenReturn(null);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreJoursArrondiDemiJournee(Mockito.isA(Date.class), Mockito.isA(Date.class))).thenReturn(minutes);

		AsaA48CounterServiceImpl service = new AsaA48CounterServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sR);
		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "helperService", helperService);

		result = service.majCompteurToAgent(result, demande, demandeEtatChangeDto);

		assertEquals(1, result.getErrors().size());
		Mockito.verify(rr, Mockito.times(0)).persistEntity(Mockito.isA(AgentCount.class));
	}

	@Test
	public void majCompteurAsaA48ToAgent_compteurNegatif_debit() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());

		ReturnMessageDto result = new ReturnMessageDto();
		DemandeAsa demande = new DemandeAsa();
		demande.setIdAgent(9008765);
		demande.setDateDebut(new Date());
		demande.setDateFin(new Date());
		Double minutes = 11.0;

		ISirhWSConsumer sR = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sR.getAgent(demande.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
		AgentAsaA48Count arc = new AgentAsaA48Count();
		arc.setTotalJours(10.0);
		Mockito.when(rr.getAgentCounterByDate(AgentAsaA48Count.class, demande.getIdAgent(), demande.getDateDebut())).thenReturn(arc);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreJoursArrondiDemiJournee(Mockito.isA(Date.class), Mockito.isA(Date.class))).thenReturn(minutes);

		AsaA48CounterServiceImpl service = new AsaA48CounterServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sR);
		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "helperService", helperService);

		result = service.majCompteurToAgent(result, demande, demandeEtatChangeDto);

		assertEquals(1, result.getInfos().size());
		assertEquals("Le solde du compteur de l'agent est négatif.", result.getInfos().get(0));
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(AgentCount.class));
	}

	@Test
	public void majCompteurAsaA48ToAgent_debitOk() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());

		ReturnMessageDto result = new ReturnMessageDto();
		DemandeAsa demande = new DemandeAsa();
		demande.setIdAgent(9008765);
		demande.setDateDebut(new Date());
		demande.setDateFin(new Date());
		Double minutes = 11.0;

		ISirhWSConsumer sR = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sR.getAgent(demande.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
		AgentAsaA48Count arc = new AgentAsaA48Count();
		arc.setTotalJours(12.0);
		Mockito.when(rr.getAgentCounterByDate(AgentAsaA48Count.class, demande.getIdAgent(), demande.getDateDebut())).thenReturn(arc);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreJoursArrondiDemiJournee(Mockito.isA(Date.class), Mockito.isA(Date.class))).thenReturn(minutes);

		AsaA48CounterServiceImpl service = new AsaA48CounterServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sR);
		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "helperService", helperService);

		result = service.majCompteurToAgent(result, demande, demandeEtatChangeDto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(AgentCount.class));
	}

	@Test
	public void saveRepresentantA48_OSInexistant() {

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(1);
		organisationSyndicale.setActif(true);

		IOrganisationSyndicaleRepository OSRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(OSRepository.getEntity(OrganisationSyndicale.class, organisationSyndicale.getIdOrganisationSyndicale())).thenReturn(null);

		AsaA48CounterServiceImpl service = new AsaA48CounterServiceImpl();
		ReflectionTestUtils.setField(service, "OSRepository", OSRepository);

		ReturnMessageDto result = service.saveRepresentantA48(organisationSyndicale.getIdOrganisationSyndicale(), null);

		assertEquals(1, result.getErrors().size());
		assertEquals("L'organisation syndicale n'existe pas.", result.getErrors().get(0));
	}

	@Test
	public void saveRepresentantA48_OSInactif() {

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(1);
		organisationSyndicale.setActif(false);

		IOrganisationSyndicaleRepository OSRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(OSRepository.getEntity(OrganisationSyndicale.class, organisationSyndicale.getIdOrganisationSyndicale()))
				.thenReturn(organisationSyndicale);

		AsaA48CounterServiceImpl service = new AsaA48CounterServiceImpl();
		ReflectionTestUtils.setField(service, "OSRepository", OSRepository);

		ReturnMessageDto result = service.saveRepresentantA48(organisationSyndicale.getIdOrganisationSyndicale(), null);

		assertEquals(1, result.getErrors().size());
		assertEquals("L'organisation syndicale n'est pas active.", result.getErrors().get(0));
	}

	@Test
	public void saveRepresentantA48_Insert_Ok() {

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(1);
		organisationSyndicale.setActif(true);

		AgentOrganisationSyndicaleDto agDto = new AgentOrganisationSyndicaleDto();
		agDto.setIdAgent(9005138);
		agDto.setActif(true);

		List<AgentOrganisationSyndicaleDto> listAgDto = new ArrayList<>();
		listAgDto.add(agDto);

		List<AgentA48OrganisationSyndicale> listAg = new ArrayList<>();

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);

		IOrganisationSyndicaleRepository OSRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(OSRepository.getEntity(OrganisationSyndicale.class, organisationSyndicale.getIdOrganisationSyndicale()))
				.thenReturn(organisationSyndicale);
		Mockito.when(OSRepository.getAgentA48Organisation(agDto.getIdAgent())).thenReturn(listAg);
		Mockito.when(OSRepository.getListeAgentA48Organisation(organisationSyndicale.getIdOrganisationSyndicale())).thenReturn(listAg);

		AsaA48CounterServiceImpl service = new AsaA48CounterServiceImpl();
		ReflectionTestUtils.setField(service, "OSRepository", OSRepository);
		ReflectionTestUtils.setField(service, "counterRepository", rr);

		ReturnMessageDto result = service.saveRepresentantA48(organisationSyndicale.getIdOrganisationSyndicale(), listAgDto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(AgentA48OrganisationSyndicale.class));
	}

	@Test
	public void saveRepresentantA48_Update_Ok() {

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(2);
		organisationSyndicale.setActif(true);

		AgentOrganisationSyndicaleDto agDto = new AgentOrganisationSyndicaleDto();
		agDto.setIdAgent(9005138);

		List<AgentOrganisationSyndicaleDto> listAgDto = new ArrayList<>();
		listAgDto.add(agDto);

		AgentA48OrganisationSyndicale ag = new AgentA48OrganisationSyndicale();
		ag.setIdAgent(9005138);
		ag.setOrganisationSyndicale(organisationSyndicale);
		List<AgentA48OrganisationSyndicale> listAg = new ArrayList<>();
		listAg.add(ag);

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);

		IOrganisationSyndicaleRepository OSRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(OSRepository.getEntity(OrganisationSyndicale.class, organisationSyndicale.getIdOrganisationSyndicale()))
				.thenReturn(organisationSyndicale);
		Mockito.when(OSRepository.getAgentA48Organisation(agDto.getIdAgent())).thenReturn(listAg);
		Mockito.when(OSRepository.getListeAgentA48Organisation(organisationSyndicale.getIdOrganisationSyndicale())).thenReturn(listAg);

		AsaA48CounterServiceImpl service = new AsaA48CounterServiceImpl();
		ReflectionTestUtils.setField(service, "OSRepository", OSRepository);
		ReflectionTestUtils.setField(service, "counterRepository", rr);

		ReturnMessageDto result = service.saveRepresentantA48(organisationSyndicale.getIdOrganisationSyndicale(), listAgDto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(AgentA48OrganisationSyndicale.class));
	}

	@Test
	public void saveRepresentantA48_Update_NonOk_AgentActif() {

		OrganisationSyndicale organisationSyndicaleDto = new OrganisationSyndicale();
		organisationSyndicaleDto.setIdOrganisationSyndicale(2);
		organisationSyndicaleDto.setActif(true);

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(1);
		organisationSyndicale.setActif(true);

		AgentOrganisationSyndicaleDto agDto = new AgentOrganisationSyndicaleDto();
		agDto.setIdAgent(9005138);

		List<AgentOrganisationSyndicaleDto> listAgDto = new ArrayList<>();
		listAgDto.add(agDto);

		AgentA48OrganisationSyndicale ag = new AgentA48OrganisationSyndicale();
		ag.setIdAgent(9005138);
		ag.setOrganisationSyndicale(organisationSyndicaleDto);
		List<AgentA48OrganisationSyndicale> listAg = new ArrayList<>();
		listAg.add(ag);

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);

		IOrganisationSyndicaleRepository OSRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(OSRepository.getEntity(OrganisationSyndicale.class, organisationSyndicale.getIdOrganisationSyndicale()))
				.thenReturn(organisationSyndicale);
		Mockito.when(OSRepository.getAgentA48Organisation(agDto.getIdAgent())).thenReturn(listAg);
		Mockito.when(OSRepository.getListeAgentA48Organisation(organisationSyndicale.getIdOrganisationSyndicale())).thenReturn(listAg);

		AsaA48CounterServiceImpl service = new AsaA48CounterServiceImpl();
		ReflectionTestUtils.setField(service, "OSRepository", OSRepository);
		ReflectionTestUtils.setField(service, "counterRepository", rr);

		ReturnMessageDto result = service.saveRepresentantA48(organisationSyndicale.getIdOrganisationSyndicale(), listAgDto);

		assertEquals(1, result.getErrors().size());
		assertEquals("L'agent [9005138] fait déja partie d'une autre organisation syndicale.", result.getErrors().get(0));
	}

	@Test
	public void saveRepresentantA48_DeleteOk() {

		OrganisationSyndicale organisationSyndicaleDto = new OrganisationSyndicale();
		organisationSyndicaleDto.setIdOrganisationSyndicale(2);
		organisationSyndicaleDto.setActif(true);

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(1);
		organisationSyndicale.setActif(true);

		AgentOrganisationSyndicaleDto agDto = new AgentOrganisationSyndicaleDto();
		agDto.setIdAgent(9005138);
		agDto.setActif(true);

		List<AgentOrganisationSyndicaleDto> listAgDto = new ArrayList<>();
		listAgDto.add(agDto);

		AgentA48OrganisationSyndicale ag2 = new AgentA48OrganisationSyndicale();
		ag2.setIdAgent(9003069);
		AgentA48OrganisationSyndicale ag = new AgentA48OrganisationSyndicale();
		ag.setIdAgent(9005138);
		ag.setOrganisationSyndicale(organisationSyndicaleDto);

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);

		IOrganisationSyndicaleRepository OSRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(OSRepository.getEntity(OrganisationSyndicale.class, organisationSyndicale.getIdOrganisationSyndicale()))
				.thenReturn(organisationSyndicale);
		Mockito.when(OSRepository.getAgentA48Organisation(agDto.getIdAgent())).thenReturn(Arrays.asList(ag));
		Mockito.when(OSRepository.getListeAgentA48Organisation(organisationSyndicale.getIdOrganisationSyndicale()))
				.thenReturn(Arrays.asList(ag2, ag));

		AsaA48CounterServiceImpl service = new AsaA48CounterServiceImpl();
		ReflectionTestUtils.setField(service, "OSRepository", OSRepository);
		ReflectionTestUtils.setField(service, "counterRepository", rr);

		ReturnMessageDto result = service.saveRepresentantA48(organisationSyndicale.getIdOrganisationSyndicale(), listAgDto);

		assertEquals(1, result.getErrors().size());
		assertEquals("L'agent [9005138] fait déja partie d'une autre organisation syndicale.", result.getErrors().get(0));
	}

}
