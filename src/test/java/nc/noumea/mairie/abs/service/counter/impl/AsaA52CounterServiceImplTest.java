package nc.noumea.mairie.abs.service.counter.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentAsaA52Count;
import nc.noumea.mairie.abs.domain.AgentCount;
import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;
import nc.noumea.mairie.abs.domain.AgentOrganisationSyndicale;
import nc.noumea.mairie.abs.domain.DemandeAsa;
import nc.noumea.mairie.abs.domain.MotifCompteur;
import nc.noumea.mairie.abs.domain.OrganisationSyndicale;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.AgentOrganisationSyndicaleDto;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.MotifCompteurDto;
import nc.noumea.mairie.abs.dto.OrganisationSyndicaleDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IAccessRightsRepository;
import nc.noumea.mairie.abs.repository.ICounterRepository;
import nc.noumea.mairie.abs.repository.IOrganisationSyndicaleRepository;
import nc.noumea.mairie.abs.service.AgentNotFoundException;
import nc.noumea.mairie.abs.service.impl.HelperService;
import nc.noumea.mairie.ws.ISirhWSConsumer;

import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class AsaA52CounterServiceImplTest extends AsaCounterServiceImplTest {

	@Test
	public void testMethodeParenteHeritage() throws Throwable {
		super.service = new AsaA52CounterServiceImpl();
		super.allTest();
	}

	@Test
	public void majManuelleCompteurAsaA52ToAgent_NonHabilite() {

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

		AsaA52CounterServiceImpl service = new AsaA52CounterServiceImpl();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);

		result = service.majManuelleCompteurToAgent(idAgent, compteurDto, false);

		assertEquals(1, result.getErrors().size());
		assertEquals("Vous n'êtes pas habilité à mettre à jour le compteur de cet agent.", result.getErrors().get(0)
				.toString());
	}

	@Test
	public void majManuelleCompteurAsaA52ToAgent_UtilisateurSIRHHabilite_MotifCompteurInexistant() {

		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		CompteurDto compteurDto = new CompteurDto();
		compteurDto.setIdAgent(9005151);
		compteurDto.setDureeAAjouter(10.0);
		compteurDto.setDateDebut(new DateTime(2013, 1, 1, 0, 0, 0).toDate());
		compteurDto.setDateDebut(new DateTime(2013, 12, 31, 0, 0, 0).toDate());

		AgentAsaA52Count arc = new AgentAsaA52Count();
		arc.setTotalMinutes(15 * 60);

		ISirhWSConsumer wsMock = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(wsMock.isUtilisateurSIRH(idAgent)).thenReturn(result);
		Mockito.when(wsMock.getAgent(compteurDto.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculAlimManuelleCompteur(compteurDto)).thenReturn(10.0);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(false);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(
				counterRepository.getAgentCounterByDate(AgentAsaA52Count.class, compteurDto.getIdAgent(),
						compteurDto.getDateDebut())).thenReturn(arc);

		AsaA52CounterServiceImpl service = new AsaA52CounterServiceImpl();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		result = service.majManuelleCompteurToAgent(idAgent, compteurDto, false);

		assertEquals(1, result.getErrors().size());
		assertEquals("Le motif n'existe pas.", result.getErrors().get(0).toString());
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentAsaA52Count.class));
	}

	@Test
	public void majManuelleCompteurAsaA52ToAgent_agentInexistant() {

		super.service = new AsaA48CounterServiceImpl();
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

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculAlimManuelleCompteur(compteurDto)).thenReturn(10.0);

		ISirhWSConsumer wsMock = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(wsMock.isUtilisateurSIRH(idAgent)).thenReturn(result);
		Mockito.when(wsMock.getAgent(compteurDto.getIdAgent())).thenReturn(null);

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
	public void majManuelleCompteurAsaA52ToAgent_OSInexistant() {

		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		CompteurDto compteurDto = new CompteurDto();
		compteurDto.setIdAgent(9005151);
		compteurDto.setDureeAAjouter(10.0);
		MotifCompteurDto motifDto = new MotifCompteurDto();
		motifDto.setIdMotifCompteur(1);
		compteurDto.setMotifCompteurDto(motifDto);
		compteurDto.setDateDebut(new DateTime(2013, 1, 1, 0, 0, 0).toDate());
		compteurDto.setDateFin(new DateTime(2013, 12, 31, 0, 0, 0).toDate());
		OrganisationSyndicaleDto oragDto = new OrganisationSyndicaleDto();
		oragDto.setIdOrganisation(1);
		compteurDto.setOrganisationSyndicaleDto(oragDto);

		AgentAsaA52Count arc = new AgentAsaA52Count();
		arc.setTotalMinutes(15 * 60);

		IOrganisationSyndicaleRepository OSRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(
				OSRepository.getEntity(OrganisationSyndicale.class, compteurDto.getOrganisationSyndicaleDto()
						.getIdOrganisation())).thenReturn(null);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(true);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculAlimManuelleCompteur(compteurDto)).thenReturn(-10.0);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(
				counterRepository
						.getEntity(MotifCompteur.class, compteurDto.getMotifCompteurDto().getIdMotifCompteur()))
				.thenReturn(new MotifCompteur());

		ISirhWSConsumer wsMock = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(wsMock.isUtilisateurSIRH(idAgent)).thenReturn(result);
		Mockito.when(wsMock.getAgent(compteurDto.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		AsaA52CounterServiceImpl service = new AsaA52CounterServiceImpl();
		ReflectionTestUtils.setField(service, "OSRepository", OSRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);

		result = service.majManuelleCompteurToAgent(idAgent, compteurDto, false);

		assertEquals(1, result.getErrors().size());
		assertEquals(AbstractCounterService.OS_INEXISTANT, result.getErrors().get(0).toString());

		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentAsaA52Count.class));
	}

	@Test
	public void majManuelleCompteurAsaA52ToAgent_OSInactive() {

		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		CompteurDto compteurDto = new CompteurDto();
		compteurDto.setIdAgent(9005151);
		compteurDto.setDureeAAjouter(10.0);
		MotifCompteurDto motifDto = new MotifCompteurDto();
		motifDto.setIdMotifCompteur(1);
		compteurDto.setMotifCompteurDto(motifDto);
		compteurDto.setDateDebut(new DateTime(2013, 1, 1, 0, 0, 0).toDate());
		compteurDto.setDateFin(new DateTime(2013, 12, 31, 0, 0, 0).toDate());
		OrganisationSyndicaleDto oragDto = new OrganisationSyndicaleDto();
		oragDto.setIdOrganisation(1);
		compteurDto.setOrganisationSyndicaleDto(oragDto);

		AgentAsaA52Count arc = new AgentAsaA52Count();
		arc.setTotalMinutes(15 * 60);

		IOrganisationSyndicaleRepository OSRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(
				OSRepository.getEntity(OrganisationSyndicale.class, compteurDto.getOrganisationSyndicaleDto()
						.getIdOrganisation())).thenReturn(new OrganisationSyndicale());

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(true);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculAlimManuelleCompteur(compteurDto)).thenReturn(-10.0);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(
				counterRepository
						.getEntity(MotifCompteur.class, compteurDto.getMotifCompteurDto().getIdMotifCompteur()))
				.thenReturn(new MotifCompteur());

		ISirhWSConsumer wsMock = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(wsMock.isUtilisateurSIRH(idAgent)).thenReturn(result);
		Mockito.when(wsMock.getAgent(compteurDto.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		AsaA52CounterServiceImpl service = new AsaA52CounterServiceImpl();
		ReflectionTestUtils.setField(service, "OSRepository", OSRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);

		result = service.majManuelleCompteurToAgent(idAgent, compteurDto, false);

		assertEquals(1, result.getErrors().size());
		assertEquals(AbstractCounterService.OS_INACTIVE, result.getErrors().get(0).toString());

		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentAsaA52Count.class));
	}

	@Test
	public void majManuelleCompteurAsaA52ToAgent_OK_avecCompteurExistant() {

		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		CompteurDto compteurDto = new CompteurDto();
		compteurDto.setIdAgent(9005151);
		compteurDto.setDureeAAjouter(10.0);
		MotifCompteurDto motifDto = new MotifCompteurDto();
		motifDto.setIdMotifCompteur(1);
		compteurDto.setMotifCompteurDto(motifDto);
		compteurDto.setDateDebut(new DateTime(2013, 1, 1, 0, 0, 0).toDate());
		compteurDto.setDateFin(new DateTime(2013, 12, 31, 0, 0, 0).toDate());
		OrganisationSyndicaleDto oragDto = new OrganisationSyndicaleDto();
		oragDto.setIdOrganisation(1);
		compteurDto.setOrganisationSyndicaleDto(oragDto);

		AgentAsaA52Count arc = new AgentAsaA52Count();
		arc.setTotalMinutes(15 * 60);

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setActif(true);

		IOrganisationSyndicaleRepository OSRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(
				OSRepository.getEntity(OrganisationSyndicale.class, compteurDto.getOrganisationSyndicaleDto()
						.getIdOrganisation())).thenReturn(organisationSyndicale);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(true);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculAlimManuelleCompteur(compteurDto)).thenReturn(-10.0);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(
				counterRepository.getAgentCounterByDate(AgentAsaA52Count.class, compteurDto.getIdAgent(),
						compteurDto.getDateDebut())).thenReturn(arc);
		Mockito.when(
				counterRepository
						.getEntity(MotifCompteur.class, compteurDto.getMotifCompteurDto().getIdMotifCompteur()))
				.thenReturn(new MotifCompteur());

		ISirhWSConsumer wsMock = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(wsMock.isUtilisateurSIRH(idAgent)).thenReturn(result);
		Mockito.when(wsMock.getAgent(compteurDto.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		AsaA52CounterServiceImpl service = new AsaA52CounterServiceImpl();
		ReflectionTestUtils.setField(service, "OSRepository", OSRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);

		result = service.majManuelleCompteurToAgent(idAgent, compteurDto, false);

		assertEquals(0, result.getErrors().size());

		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentAsaA52Count.class));
	}

	@Test
	public void majManuelleCompteurAsaA52ToAgent_OK_sansCompteurExistant() {

		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		CompteurDto compteurDto = new CompteurDto();
		compteurDto.setIdAgent(9005151);
		compteurDto.setDureeAAjouter(10.0);
		MotifCompteurDto motifDto = new MotifCompteurDto();
		motifDto.setIdMotifCompteur(1);
		compteurDto.setMotifCompteurDto(motifDto);
		compteurDto.setDateDebut(new DateTime(2013, 1, 1, 0, 0, 0).toDate());
		compteurDto.setDateFin(new DateTime(2013, 12, 31, 0, 0, 0).toDate());
		OrganisationSyndicaleDto oragDto = new OrganisationSyndicaleDto();
		oragDto.setIdOrganisation(1);
		compteurDto.setOrganisationSyndicaleDto(oragDto);

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setActif(true);

		IOrganisationSyndicaleRepository OSRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(
				OSRepository.getEntity(OrganisationSyndicale.class, compteurDto.getOrganisationSyndicaleDto()
						.getIdOrganisation())).thenReturn(organisationSyndicale);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(false);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculAlimManuelleCompteur(compteurDto)).thenReturn(10.0);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(
				counterRepository.getAgentCounterByDate(AgentAsaA52Count.class, compteurDto.getIdAgent(),
						compteurDto.getDateDebut())).thenReturn(null);
		Mockito.when(
				counterRepository
						.getEntity(MotifCompteur.class, compteurDto.getMotifCompteurDto().getIdMotifCompteur()))
				.thenReturn(new MotifCompteur());

		ISirhWSConsumer wsMock = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(wsMock.isUtilisateurSIRH(idAgent)).thenReturn(result);
		Mockito.when(wsMock.getAgent(compteurDto.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		AsaA52CounterServiceImpl service = new AsaA52CounterServiceImpl();
		ReflectionTestUtils.setField(service, "OSRepository", OSRepository);
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);

		result = service.majManuelleCompteurToAgent(idAgent, compteurDto, false);

		assertEquals(0, result.getErrors().size());

		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentAsaA52Count.class));
	}

	@Test
	public void getListeCompteur_noResult() {

		List<CompteurDto> result = new ArrayList<CompteurDto>();

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getListCounter(AgentAsaA52Count.class)).thenReturn(
				new ArrayList<AgentAsaA52Count>());

		AsaA52CounterServiceImpl service = new AsaA52CounterServiceImpl();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		result = service.getListeCompteur(1, null);

		assertEquals(0, result.size());

		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentAsaA52Count.class));
	}

	@Test
	public void getListeCompteur_1Result() {

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setLibelle("libelle OS");
		organisationSyndicale.setIdOrganisationSyndicale(1);

		List<CompteurDto> result = new ArrayList<CompteurDto>();
		AgentAsaA52Count e = new AgentAsaA52Count();
		e.setTotalMinutes(12 * 60);
		e.setOrganisationSyndicale(organisationSyndicale);
		List<AgentAsaA52Count> list = new ArrayList<AgentAsaA52Count>();
		list.add(e);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getListCounterByOrganisation(AgentAsaA52Count.class, 1)).thenReturn(list);

		AsaA52CounterServiceImpl service = new AsaA52CounterServiceImpl();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		result = service.getListeCompteur(1, null);

		assertEquals(1, result.size());
		assertEquals(12 * 60, result.get(0).getDureeAAjouter().intValue());
		assertEquals(organisationSyndicale.getIdOrganisationSyndicale(), result.get(0).getOrganisationSyndicaleDto()
				.getIdOrganisation());

		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentAsaA52Count.class));
	}

	@Test
	public void majCompteurAsaA52ToAgent_AgentNotFound() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());

		ReturnMessageDto result = new ReturnMessageDto();

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(1);

		DemandeAsa demande = new DemandeAsa();
		demande.setIdAgent(9008765);
		demande.setDateDebut(new Date());
		demande.setDateFin(new Date());
		demande.setOrganisationSyndicale(organisationSyndicale);

		int minutes = 10 * 60;

		ISirhWSConsumer sR = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sR.getAgent(demande.getIdAgent())).thenReturn(null);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreMinutes(Mockito.isA(Date.class), Mockito.isA(Date.class))).thenReturn(
				minutes);

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);

		AsaA52CounterServiceImpl service = new AsaA52CounterServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sR);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "counterRepository", rr);

		boolean isAgentNotFound = false;
		try {
			result = service.majCompteurToAgent(result, demande, demandeEtatChangeDto);
		} catch (AgentNotFoundException e) {
			isAgentNotFound = true;
		}

		assertTrue(isAgentNotFound);
		Mockito.verify(rr, Mockito.times(0)).persistEntity(Mockito.isA(AgentCount.class));
	}

	@Test
	public void majCompteurAsaA52ToAgent_OSInexistant() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());

		ReturnMessageDto result = new ReturnMessageDto();

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(1);

		DemandeAsa demande = new DemandeAsa();
		demande.setIdAgent(9008765);
		demande.setDateDebut(new Date());
		demande.setDateFin(new Date());
		demande.setOrganisationSyndicale(organisationSyndicale);

		int minutes = 10 * 60;

		IOrganisationSyndicaleRepository OSRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(
				OSRepository.getEntity(OrganisationSyndicale.class, organisationSyndicale.getIdOrganisationSyndicale()))
				.thenReturn(null);

		ISirhWSConsumer sR = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sR.getAgent(demande.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreMinutes(Mockito.isA(Date.class), Mockito.isA(Date.class))).thenReturn(
				minutes);

		AsaA52CounterServiceImpl service = new AsaA52CounterServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sR);
		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "OSRepository", OSRepository);

		result = service.majCompteurToAgent(result, demande, demandeEtatChangeDto);

		assertEquals(1, result.getErrors().size());
		assertEquals(AbstractCounterService.OS_INEXISTANT, result.getErrors().get(0));
		Mockito.verify(rr, Mockito.times(0)).persistEntity(Mockito.isA(AgentCount.class));
	}

	@Test
	public void majCompteurAsaA52ToAgent_OSInactif() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());

		ReturnMessageDto result = new ReturnMessageDto();

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(1);

		DemandeAsa demande = new DemandeAsa();
		demande.setIdAgent(9008765);
		demande.setDateDebut(new Date());
		demande.setDateFin(new Date());
		demande.setOrganisationSyndicale(organisationSyndicale);

		int minutes = 10 * 60;

		IOrganisationSyndicaleRepository OSRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(
				OSRepository.getEntity(OrganisationSyndicale.class, organisationSyndicale.getIdOrganisationSyndicale()))
				.thenReturn(new OrganisationSyndicale());

		ISirhWSConsumer sR = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sR.getAgent(demande.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreMinutes(Mockito.isA(Date.class), Mockito.isA(Date.class))).thenReturn(
				minutes);

		AsaA52CounterServiceImpl service = new AsaA52CounterServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sR);
		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "OSRepository", OSRepository);

		result = service.majCompteurToAgent(result, demande, demandeEtatChangeDto);

		assertEquals(1, result.getErrors().size());
		assertEquals(AbstractCounterService.OS_INACTIVE, result.getErrors().get(0));
		Mockito.verify(rr, Mockito.times(0)).persistEntity(Mockito.isA(AgentCount.class));
	}

	@Test
	public void majCompteurAsaA52ToAgent_compteurInexistant() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());

		ReturnMessageDto result = new ReturnMessageDto();

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(1);

		DemandeAsa demande = new DemandeAsa();
		demande.setIdAgent(9008765);
		demande.setDateDebut(new Date());
		demande.setDateFin(new Date());
		demande.setOrganisationSyndicale(organisationSyndicale);

		int minutes = 10 * 60;

		IOrganisationSyndicaleRepository OSRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(
				OSRepository.getEntity(OrganisationSyndicale.class, organisationSyndicale.getIdOrganisationSyndicale()))
				.thenReturn(new OrganisationSyndicale());

		ISirhWSConsumer sR = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sR.getAgent(demande.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
		Mockito.when(
				rr.getOSCounterByDate(AgentAsaA52Count.class, organisationSyndicale.getIdOrganisationSyndicale(),
						demande.getDateDebut())).thenReturn(null);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreMinutes(Mockito.isA(Date.class), Mockito.isA(Date.class))).thenReturn(
				minutes);

		AsaA52CounterServiceImpl service = new AsaA52CounterServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sR);
		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "OSRepository", OSRepository);

		result = service.majCompteurToAgent(result, demande, demandeEtatChangeDto);

		assertEquals(1, result.getErrors().size());
		Mockito.verify(rr, Mockito.times(0)).persistEntity(Mockito.isA(AgentCount.class));
	}

	@Test
	public void majCompteurAsaA52ToAgent_compteurNegatif_debit() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());

		ReturnMessageDto result = new ReturnMessageDto();

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(1);
		organisationSyndicale.setActif(true);

		DemandeAsa demande = new DemandeAsa();
		demande.setIdAgent(9008765);
		demande.setDateDebut(new Date());
		demande.setDateFin(new Date());
		demande.setOrganisationSyndicale(organisationSyndicale);

		int minutes = 11 * 60;

		ISirhWSConsumer sR = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sR.getAgent(demande.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		IOrganisationSyndicaleRepository OSRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(
				OSRepository.getEntity(OrganisationSyndicale.class, organisationSyndicale.getIdOrganisationSyndicale()))
				.thenReturn(organisationSyndicale);

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
		AgentAsaA52Count arc = new AgentAsaA52Count();
		arc.setTotalMinutes(10 * 60);
		Mockito.when(
				rr.getOSCounterByDate(AgentAsaA52Count.class, organisationSyndicale.getIdOrganisationSyndicale(),
						demande.getDateDebut())).thenReturn(arc);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreMinutes(Mockito.isA(Date.class), Mockito.isA(Date.class))).thenReturn(
				minutes);

		AsaA52CounterServiceImpl service = new AsaA52CounterServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sR);
		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "OSRepository", OSRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);

		result = service.majCompteurToAgent(result, demande, demandeEtatChangeDto);

		assertEquals(1, result.getInfos().size());
		assertEquals("Le solde du compteur de l'agent est négatif.", result.getInfos().get(0));
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(AgentCount.class));
	}

	@Test
	public void majCompteurAsaA52ToAgent_debitOk() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());

		ReturnMessageDto result = new ReturnMessageDto();

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(1);
		organisationSyndicale.setActif(true);

		DemandeAsa demande = new DemandeAsa();
		demande.setIdAgent(9008765);
		demande.setDateDebut(new Date());
		demande.setDateFin(new Date());
		demande.setOrganisationSyndicale(organisationSyndicale);

		int minutes = 11 * 60;

		ISirhWSConsumer sR = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sR.getAgent(demande.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		IOrganisationSyndicaleRepository OSRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(
				OSRepository.getEntity(OrganisationSyndicale.class, organisationSyndicale.getIdOrganisationSyndicale()))
				.thenReturn(organisationSyndicale);

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
		AgentAsaA52Count arc = new AgentAsaA52Count();
		arc.setTotalMinutes(12 * 60);
		Mockito.when(
				rr.getOSCounterByDate(AgentAsaA52Count.class, organisationSyndicale.getIdOrganisationSyndicale(),
						demande.getDateDebut())).thenReturn(arc);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreMinutes(Mockito.isA(Date.class), Mockito.isA(Date.class))).thenReturn(
				minutes);

		AsaA52CounterServiceImpl service = new AsaA52CounterServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sR);
		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "OSRepository", OSRepository);

		result = service.majCompteurToAgent(result, demande, demandeEtatChangeDto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(AgentCount.class));
	}

	@Test
	public void saveRepresentantA52_OSInexistant() {

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(1);
		organisationSyndicale.setActif(true);

		IOrganisationSyndicaleRepository OSRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(
				OSRepository.getEntity(OrganisationSyndicale.class, organisationSyndicale.getIdOrganisationSyndicale()))
				.thenReturn(null);

		AsaA52CounterServiceImpl service = new AsaA52CounterServiceImpl();
		ReflectionTestUtils.setField(service, "OSRepository", OSRepository);

		ReturnMessageDto result = service.saveRepresentantA52(organisationSyndicale.getIdOrganisationSyndicale(), null);

		assertEquals(1, result.getErrors().size());
		assertEquals("L'organisation syndicale n'existe pas.", result.getErrors().get(0));
	}

	@Test
	public void saveRepresentantA52_OSInactif() {

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(1);
		organisationSyndicale.setActif(false);

		IOrganisationSyndicaleRepository OSRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(
				OSRepository.getEntity(OrganisationSyndicale.class, organisationSyndicale.getIdOrganisationSyndicale()))
				.thenReturn(organisationSyndicale);

		AsaA52CounterServiceImpl service = new AsaA52CounterServiceImpl();
		ReflectionTestUtils.setField(service, "OSRepository", OSRepository);

		ReturnMessageDto result = service.saveRepresentantA52(organisationSyndicale.getIdOrganisationSyndicale(), null);

		assertEquals(1, result.getErrors().size());
		assertEquals("L'organisation syndicale n'est pas active.", result.getErrors().get(0));
	}

	@Test
	public void saveRepresentantA52_Insert_Ok() {

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(1);
		organisationSyndicale.setActif(true);

		AgentOrganisationSyndicaleDto agDto = new AgentOrganisationSyndicaleDto();
		agDto.setIdAgent(9005138);
		agDto.setActif(true);

		List<AgentOrganisationSyndicaleDto> listAgDto = new ArrayList<>();
		listAgDto.add(agDto);

		List<AgentOrganisationSyndicale> listAg = new ArrayList<>();

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);

		IOrganisationSyndicaleRepository OSRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(
				OSRepository.getEntity(OrganisationSyndicale.class, organisationSyndicale.getIdOrganisationSyndicale()))
				.thenReturn(organisationSyndicale);
		Mockito.when(OSRepository.getAgentOrganisation(agDto.getIdAgent())).thenReturn(listAg);
		Mockito.when(OSRepository.getListeAgentOrganisation(organisationSyndicale.getIdOrganisationSyndicale()))
				.thenReturn(listAg);

		AsaA52CounterServiceImpl service = new AsaA52CounterServiceImpl();
		ReflectionTestUtils.setField(service, "OSRepository", OSRepository);
		ReflectionTestUtils.setField(service, "counterRepository", rr);

		ReturnMessageDto result = service.saveRepresentantA52(organisationSyndicale.getIdOrganisationSyndicale(),
				listAgDto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(AgentOrganisationSyndicale.class));
	}

	@Test
	public void saveRepresentantA52_Update_Ok() {

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

		AgentOrganisationSyndicale ag = new AgentOrganisationSyndicale();
		ag.setIdAgent(9005138);
		ag.setActif(false);
		ag.setOrganisationSyndicale(organisationSyndicaleDto);
		List<AgentOrganisationSyndicale> listAg = new ArrayList<>();
		listAg.add(ag);

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);

		IOrganisationSyndicaleRepository OSRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(
				OSRepository.getEntity(OrganisationSyndicale.class, organisationSyndicale.getIdOrganisationSyndicale()))
				.thenReturn(organisationSyndicale);
		Mockito.when(OSRepository.getAgentOrganisation(agDto.getIdAgent())).thenReturn(listAg);
		Mockito.when(OSRepository.getListeAgentOrganisation(organisationSyndicale.getIdOrganisationSyndicale()))
				.thenReturn(listAg);

		AsaA52CounterServiceImpl service = new AsaA52CounterServiceImpl();
		ReflectionTestUtils.setField(service, "OSRepository", OSRepository);
		ReflectionTestUtils.setField(service, "counterRepository", rr);

		ReturnMessageDto result = service.saveRepresentantA52(organisationSyndicale.getIdOrganisationSyndicale(),
				listAgDto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(AgentOrganisationSyndicale.class));
	}

	@Test
	public void saveRepresentantA52_Update_NonOk_AgentActif() {

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

		AgentOrganisationSyndicale ag = new AgentOrganisationSyndicale();
		ag.setIdAgent(9005138);
		ag.setActif(true);
		ag.setOrganisationSyndicale(organisationSyndicaleDto);
		List<AgentOrganisationSyndicale> listAg = new ArrayList<>();
		listAg.add(ag);

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);

		IOrganisationSyndicaleRepository OSRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(
				OSRepository.getEntity(OrganisationSyndicale.class, organisationSyndicale.getIdOrganisationSyndicale()))
				.thenReturn(organisationSyndicale);
		Mockito.when(OSRepository.getAgentOrganisation(agDto.getIdAgent())).thenReturn(listAg);
		Mockito.when(OSRepository.getListeAgentOrganisation(organisationSyndicale.getIdOrganisationSyndicale()))
				.thenReturn(listAg);

		AsaA52CounterServiceImpl service = new AsaA52CounterServiceImpl();
		ReflectionTestUtils.setField(service, "OSRepository", OSRepository);
		ReflectionTestUtils.setField(service, "counterRepository", rr);

		ReturnMessageDto result = service.saveRepresentantA52(organisationSyndicale.getIdOrganisationSyndicale(),
				listAgDto);

		assertEquals(1, result.getErrors().size());
		assertEquals("L'agent [9005138] fait déja partie d'une autre organisation syndicale.", result.getErrors()
				.get(0));
	}

	@Test
	public void saveRepresentantA52_DeleteOk() {

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

		AgentOrganisationSyndicale ag2 = new AgentOrganisationSyndicale();
		ag2.setIdAgent(9003069);
		ag2.setActif(true);
		AgentOrganisationSyndicale ag = new AgentOrganisationSyndicale();
		ag.setIdAgent(9005138);
		ag.setActif(true);
		ag.setOrganisationSyndicale(organisationSyndicaleDto);

		organisationSyndicale.getAgents().add(ag);
		organisationSyndicale.getAgents().add(ag2);

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);

		IOrganisationSyndicaleRepository OSRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(
				OSRepository.getEntity(OrganisationSyndicale.class, organisationSyndicale.getIdOrganisationSyndicale()))
				.thenReturn(organisationSyndicale);
		Mockito.when(OSRepository.getAgentOrganisation(agDto.getIdAgent())).thenReturn(Arrays.asList(ag));
		Mockito.when(OSRepository.getListeAgentOrganisation(organisationSyndicale.getIdOrganisationSyndicale()))
				.thenReturn(organisationSyndicale.getAgents());

		AsaA52CounterServiceImpl service = new AsaA52CounterServiceImpl();
		ReflectionTestUtils.setField(service, "OSRepository", OSRepository);
		ReflectionTestUtils.setField(service, "counterRepository", rr);

		ReturnMessageDto result = service.saveRepresentantA52(organisationSyndicale.getIdOrganisationSyndicale(),
				listAgDto);

		assertEquals(1, result.getErrors().size());
		assertEquals("L'agent [9005138] fait déja partie d'une autre organisation syndicale.", result.getErrors()
				.get(0));
		assertEquals(1, organisationSyndicale.getAgents().size());
	}

}
