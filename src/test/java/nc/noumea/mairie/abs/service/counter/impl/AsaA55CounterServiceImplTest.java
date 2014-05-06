package nc.noumea.mairie.abs.service.counter.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentAsaA55Count;
import nc.noumea.mairie.abs.domain.AgentCount;
import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.MotifCompteur;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.dto.CompteurAsaDto;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IAccessRightsRepository;
import nc.noumea.mairie.abs.repository.ICounterRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.abs.service.AgentNotFoundException;
import nc.noumea.mairie.abs.service.impl.HelperService;
import nc.noumea.mairie.sirh.domain.Agent;
import nc.noumea.mairie.ws.ISirhWSConsumer;

import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class AsaA55CounterServiceImplTest {

	@Test
	public void majManuelleCompteurAsaA55ToAgent_NonHabilite() {

		ReturnMessageDto result = new ReturnMessageDto();
		result.getErrors().add("L'agent 9005138 n'existe pas dans l'AD.");
		Integer idAgent = 9005138;
		CompteurDto compteurDto = new CompteurDto();
		compteurDto.setIdAgent(9005151);
		compteurDto.setDateDebut(new DateTime(2013, 4, 2, 0, 0, 0).toDate());

		ISirhWSConsumer wsMock = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(wsMock.isUtilisateurSIRH(idAgent)).thenReturn(result);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculMinutesAlimManuelleCompteur(compteurDto)).thenReturn(10.0);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(false);

		AsaA55CounterServiceImpl service = new AsaA55CounterServiceImpl();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);

		result = service.majManuelleCompteurToAgent(idAgent, compteurDto);

		assertEquals(1, result.getErrors().size());
		assertEquals("Vous n'êtes pas habilité à mettre à jour le compteur de cet agent.", result.getErrors().get(0)
				.toString());
	}

	@Test
	public void majManuelleCompteurAsaA55ToAgent_UtilisateurSIRHHabilite_MotifCompteurInexistant() {

		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		CompteurDto compteurDto = new CompteurDto();
		compteurDto.setIdAgent(9005151);
		compteurDto.setDureeAAjouter(10.0);
		compteurDto.setDateDebut(new DateTime(2013, 1, 1, 0, 0, 0).toDate());
		compteurDto.setDateDebut(new DateTime(2013, 12, 31, 0, 0, 0).toDate());

		AgentAsaA55Count arc = new AgentAsaA55Count();
		arc.setTotalMinutes(15 * 60);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgent(compteurDto.getIdAgent())).thenReturn(new Agent());

		ISirhWSConsumer wsMock = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(wsMock.isUtilisateurSIRH(idAgent)).thenReturn(result);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculMinutesAlimManuelleCompteur(compteurDto)).thenReturn(10.0);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(false);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(
				counterRepository.getAgentCounterByDate(AgentAsaA55Count.class, compteurDto.getIdAgent(),
						compteurDto.getDateDebut())).thenReturn(arc);

		AsaA55CounterServiceImpl service = new AsaA55CounterServiceImpl();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		result = service.majManuelleCompteurToAgent(idAgent, compteurDto);

		assertEquals(1, result.getErrors().size());
		assertEquals("Le motif n'existe pas.", result.getErrors().get(0).toString());
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentAsaA55Count.class));
	}

	@Test
	public void majManuelleCompteurAsaA55ToAgent_OK_avecCompteurExistant() {

		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		CompteurDto compteurDto = new CompteurDto();
		compteurDto.setIdAgent(9005151);
		compteurDto.setDureeAAjouter(10.0);
		compteurDto.setIdMotifCompteur(1);
		compteurDto.setDateDebut(new DateTime(2013, 1, 1, 0, 0, 0).toDate());
		compteurDto.setDateFin(new DateTime(2013, 12, 31, 0, 0, 0).toDate());

		AgentAsaA55Count arc = new AgentAsaA55Count();
		arc.setTotalMinutes(15 * 60);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(true);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculMinutesAlimManuelleCompteur(compteurDto)).thenReturn(-10.0);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgent(compteurDto.getIdAgent())).thenReturn(new Agent());

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(
				counterRepository.getAgentCounterByDate(AgentAsaA55Count.class, compteurDto.getIdAgent(),
						compteurDto.getDateDebut())).thenReturn(arc);
		Mockito.when(counterRepository.getEntity(MotifCompteur.class, compteurDto.getIdMotifCompteur())).thenReturn(
				new MotifCompteur());

		ISirhWSConsumer wsMock = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(wsMock.isUtilisateurSIRH(idAgent)).thenReturn(result);

		AsaA55CounterServiceImpl service = new AsaA55CounterServiceImpl();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);

		result = service.majManuelleCompteurToAgent(idAgent, compteurDto);

		assertEquals(0, result.getErrors().size());

		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentAsaA55Count.class));
	}

	@Test
	public void majManuelleCompteurAsaA55ToAgent_OK_sansCompteurExistant() {

		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		CompteurDto compteurDto = new CompteurDto();
		compteurDto.setIdAgent(9005151);
		compteurDto.setDureeAAjouter(10.0);
		compteurDto.setIdMotifCompteur(1);
		compteurDto.setDateDebut(new DateTime(2013, 1, 1, 0, 0, 0).toDate());
		compteurDto.setDateFin(new DateTime(2013, 12, 31, 0, 0, 0).toDate());

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(false);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculMinutesAlimManuelleCompteur(compteurDto)).thenReturn(10.0);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgent(compteurDto.getIdAgent())).thenReturn(new Agent());

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(
				counterRepository.getAgentCounterByDate(AgentAsaA55Count.class, compteurDto.getIdAgent(),
						compteurDto.getDateDebut())).thenReturn(null);
		Mockito.when(counterRepository.getEntity(MotifCompteur.class, compteurDto.getIdMotifCompteur())).thenReturn(
				new MotifCompteur());

		ISirhWSConsumer wsMock = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(wsMock.isUtilisateurSIRH(idAgent)).thenReturn(result);

		AsaA55CounterServiceImpl service = new AsaA55CounterServiceImpl();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);

		result = service.majManuelleCompteurToAgent(idAgent, compteurDto);

		assertEquals(0, result.getErrors().size());

		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentAsaA55Count.class));
	}

	@Test
	public void getListeCompteur_noResult() {

		List<CompteurAsaDto> result = new ArrayList<CompteurAsaDto>();

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getListCounter(AgentAsaA55Count.class)).thenReturn(
				new ArrayList<AgentAsaA55Count>());

		AsaA55CounterServiceImpl service = new AsaA55CounterServiceImpl();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		result = service.getListeCompteur();

		assertEquals(0, result.size());

		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentAsaA55Count.class));
	}

	@Test
	public void getListeCompteur_1Result() {

		List<CompteurAsaDto> result = new ArrayList<CompteurAsaDto>();
		AgentAsaA55Count e = new AgentAsaA55Count();
		e.setTotalMinutes(12 * 60);
		List<AgentAsaA55Count> list = new ArrayList<AgentAsaA55Count>();
		list.add(e);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getListCounter(AgentAsaA55Count.class)).thenReturn(list);

		AsaA55CounterServiceImpl service = new AsaA55CounterServiceImpl();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		result = service.getListeCompteur();

		assertEquals(1, result.size());
		assertEquals(12 * 60, result.get(0).getNb().intValue());

		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentAsaA55Count.class));
	}

	@Test
	public void majCompteurAsaA55ToAgent_compteurInexistant() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());

		ReturnMessageDto result = new ReturnMessageDto();
		Demande demande = new Demande();
		demande.setIdAgent(9008765);
		demande.setDateDebut(new Date());
		demande.setDateFin(new Date());
		int minutes = 10 * 60;

		ISirhRepository sR = Mockito.mock(ISirhRepository.class);
		Mockito.when(sR.getAgent(demande.getIdAgent())).thenReturn(new Agent());

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
		Mockito.when(rr.getAgentCounter(AgentAsaA55Count.class, demande.getIdAgent())).thenReturn(null);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreMinutes(Mockito.isA(Date.class), Mockito.isA(Date.class))).thenReturn(
				minutes);

		AsaA55CounterServiceImpl service = new AsaA55CounterServiceImpl();
		ReflectionTestUtils.setField(service, "sirhRepository", sR);
		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "helperService", helperService);

		result = service.majCompteurToAgent(result, demande, demandeEtatChangeDto);

		assertEquals(1, result.getErrors().size());
		Mockito.verify(rr, Mockito.times(0)).persistEntity(Mockito.isA(AgentCount.class));
	}

	@Test
	public void majCompteurAsaA55ToAgent_compteurNegatif_debit() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());

		ReturnMessageDto result = new ReturnMessageDto();
		Demande demande = new Demande();
		demande.setIdAgent(9008765);
		demande.setDateDebut(new Date());
		demande.setDateFin(new Date());
		int minutes = 11 * 60;

		ISirhRepository sR = Mockito.mock(ISirhRepository.class);
		Mockito.when(sR.getAgent(demande.getIdAgent())).thenReturn(new Agent());

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
		AgentAsaA55Count arc = new AgentAsaA55Count();
		arc.setTotalMinutes(10 * 60);
		Mockito.when(rr.getAgentCounter(AgentAsaA55Count.class, demande.getIdAgent())).thenReturn(arc);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreMinutes(Mockito.isA(Date.class), Mockito.isA(Date.class))).thenReturn(
				minutes);

		AsaA55CounterServiceImpl service = new AsaA55CounterServiceImpl();
		ReflectionTestUtils.setField(service, "sirhRepository", sR);
		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "helperService", helperService);

		result = service.majCompteurToAgent(result, demande, demandeEtatChangeDto);

		assertEquals(1, result.getInfos().size());
		assertEquals("Le solde du compteur de l'agent est négatif.", result.getInfos().get(0));
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(AgentCount.class));
	}

	@Test
	public void majCompteurAsaA55ToAgent_debitOk() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());

		ReturnMessageDto result = new ReturnMessageDto();
		Demande demande = new Demande();
		demande.setIdAgent(9008765);
		demande.setDateDebut(new Date());
		demande.setDateFin(new Date());
		int minutes = 11 * 60;

		ISirhRepository sR = Mockito.mock(ISirhRepository.class);
		Mockito.when(sR.getAgent(demande.getIdAgent())).thenReturn(new Agent());

		ICounterRepository rr = Mockito.mock(ICounterRepository.class);
		AgentAsaA55Count arc = new AgentAsaA55Count();
		arc.setTotalMinutes(12 * 60);
		Mockito.when(rr.getAgentCounter(AgentAsaA55Count.class, demande.getIdAgent())).thenReturn(arc);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreMinutes(Mockito.isA(Date.class), Mockito.isA(Date.class))).thenReturn(
				minutes);

		AsaA55CounterServiceImpl service = new AsaA55CounterServiceImpl();
		ReflectionTestUtils.setField(service, "sirhRepository", sR);
		ReflectionTestUtils.setField(service, "counterRepository", rr);
		ReflectionTestUtils.setField(service, "helperService", helperService);

		result = service.majCompteurToAgent(result, demande, demandeEtatChangeDto);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(rr, Mockito.times(1)).persistEntity(Mockito.isA(AgentCount.class));
	}

	@Test
	public void calculJoursAlimAutoCompteur_etatValide() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());

		Demande demande = new Demande();

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreMinutes(Mockito.isA(Date.class), Mockito.isA(Date.class))).thenReturn(
				10 * 60);

		AsaA55CounterServiceImpl service = new AsaA55CounterServiceImpl();
		ReflectionTestUtils.setField(service, "helperService", helperService);

		int result = service.calculMinutesAlimAutoCompteur(demandeEtatChangeDto, demande, new Date(), new Date());

		assertEquals(result, -10 * 60);
	}

	@Test
	public void calculJoursAlimAutoCompteur_etatPrise_and_etatPrcdValide() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());

		EtatDemande etatDemande = new EtatDemande();
		etatDemande.setEtat(RefEtatEnum.PRISE);

		Demande demande = new Demande();
		demande.addEtatDemande(etatDemande);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreMinutes(Mockito.isA(Date.class), Mockito.isA(Date.class))).thenReturn(
				10 * 60);

		AsaA55CounterServiceImpl service = new AsaA55CounterServiceImpl();
		ReflectionTestUtils.setField(service, "helperService", helperService);

		int result = service.calculMinutesAlimAutoCompteur(demandeEtatChangeDto, demande, new Date(), new Date());

		assertEquals(result, 10 * 60);
	}

	@Test
	public void calculJoursAlimAutoCompteur_etatAnnule_and_etatPrcdValide() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());

		EtatDemande etatDemande = new EtatDemande();
		etatDemande.setEtat(RefEtatEnum.VALIDEE);

		Demande demande = new Demande();
		demande.addEtatDemande(etatDemande);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreMinutes(Mockito.isA(Date.class), Mockito.isA(Date.class))).thenReturn(
				10 * 60);

		AsaA55CounterServiceImpl service = new AsaA55CounterServiceImpl();
		ReflectionTestUtils.setField(service, "helperService", helperService);

		int result = service.calculMinutesAlimAutoCompteur(demandeEtatChangeDto, demande, new Date(), new Date());

		assertEquals(result, 10 * 60);
	}

	@Test
	public void calculJoursAlimAutoCompteur_etatRejete_and_etatPrcdApprouve() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.REJETE.getCodeEtat());

		EtatDemande etatDemande = new EtatDemande();
		etatDemande.setEtat(RefEtatEnum.APPROUVEE);

		Demande demande = new Demande();
		demande.addEtatDemande(etatDemande);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreMinutes(Mockito.isA(Date.class), Mockito.isA(Date.class))).thenReturn(
				10 * 60);

		AsaA55CounterServiceImpl service = new AsaA55CounterServiceImpl();
		ReflectionTestUtils.setField(service, "helperService", helperService);

		int result = service.calculMinutesAlimAutoCompteur(demandeEtatChangeDto, demande, new Date(), new Date());

		assertEquals(result, 0);
	}

}
