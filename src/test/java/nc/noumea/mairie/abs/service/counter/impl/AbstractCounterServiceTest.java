package nc.noumea.mairie.abs.service.counter.impl;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import nc.noumea.mairie.abs.domain.AgentCount;
import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;
import nc.noumea.mairie.abs.domain.MotifCompteur;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.droit.repository.IAccessRightsRepository;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.recup.domain.AgentRecupCount;
import nc.noumea.mairie.abs.repository.ICounterRepository;
import nc.noumea.mairie.abs.service.impl.HelperService;
import nc.noumea.mairie.ws.ISirhWSConsumer;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class AbstractCounterServiceTest {

	protected AbstractCounterService service = new DefaultCounterServiceImpl();

	protected ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
	protected IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);

	public void allTest(AbstractCounterService pService) {

		service = pService;
		if (null == service) {
			service = new DefaultCounterServiceImpl();
		}

		controlSaisieAlimManuelleCompteur_minutesNonSaisie();
		controlSaisieAlimManuelleCompteur_minutesErreurSaisie();
		controlSaisieAlimManuelleCompteur_ajoutOK();
		controlSaisieAlimManuelleCompteur_retireOK();
		controlCompteurPositif_negatif();
		controlCompteurPositif_OkZero();
		controlCompteurPositif_Ok();
		majAgentHistoAlimManuelle();
		majManuelleCompteurToAgent_OperateurNonHabilite();
		majManuelleCompteurToAgent_MotifCompteurInexistant();
	}

	@Test
	public void controlSaisieAlimManuelleCompteur_minutesNonSaisie() {

		CompteurDto compteurDto = new CompteurDto();
		ReturnMessageDto result = new ReturnMessageDto();

		service.controlSaisieAlimManuelleCompteur(compteurDto, result);

		assertEquals(1, result.getErrors().size());
		assertEquals("La durée à ajouter ou retrancher n'est pas saisie.", result.getErrors().get(0).toString());
	}

	@Test
	public void controlSaisieAlimManuelleCompteur_minutesErreurSaisie() {

		CompteurDto compteurDto = new CompteurDto();
		compteurDto.setDureeAAjouter(1.0);
		compteurDto.setDureeARetrancher(1.0);
		ReturnMessageDto result = new ReturnMessageDto();

		service.controlSaisieAlimManuelleCompteur(compteurDto, result);

		assertEquals(1, result.getErrors().size());
		assertEquals("Un seul des champs Durée à ajouter ou Durée à retrancher doit être saisi.", result.getErrors()
				.get(0).toString());
	}

	@Test
	public void controlSaisieAlimManuelleCompteur_ajoutOK() {

		CompteurDto compteurDto = new CompteurDto();
		compteurDto.setDureeAAjouter(1.0);
		ReturnMessageDto result = new ReturnMessageDto();

		service.controlSaisieAlimManuelleCompteur(compteurDto, result);

		assertEquals(0, result.getErrors().size());
	}

	@Test
	public void controlSaisieAlimManuelleCompteur_retireOK() {
		CompteurDto compteurDto = new CompteurDto();
		compteurDto.setDureeARetrancher(1.0);
		ReturnMessageDto result = new ReturnMessageDto();

		service.controlSaisieAlimManuelleCompteur(compteurDto, result);

		assertEquals(0, result.getErrors().size());
	}

	@Test
	public void controlCompteurPositif_negatif() {

		Integer minutes = -10;
		Double totalMinutes = 0.0;
		ReturnMessageDto result = new ReturnMessageDto();

		service.controlCompteurPositif(minutes, totalMinutes, result);

		assertEquals(1, result.getErrors().size());
		assertEquals(AbstractCounterService.SOLDE_COMPTEUR_NEGATIF, result.getErrors().get(0).toString());
	}

	@Test
	public void controlCompteurPositif_OkZero() {

		Integer minutes = -10;
		Double totalMinutes = 10.0;
		ReturnMessageDto result = new ReturnMessageDto();

		service.controlCompteurPositif(minutes, totalMinutes, result);

		assertEquals(0, result.getErrors().size());
	}

	@Test
	public void controlCompteurPositif_Ok() {

		Integer minutes = -10;
		Double totalMinutes = 10.5;
		ReturnMessageDto result = new ReturnMessageDto();

		service.controlCompteurPositif(minutes, totalMinutes, result);

		assertEquals(0, result.getErrors().size());
	}

	@Test
	public void majAgentHistoAlimManuelle() {

		Integer idAgentOperateur = 9005138;
		Integer idAgentConcerne = 9002990;
		MotifCompteur motifCompteur = new MotifCompteur();
		String textLog = "textLog";
		AgentCount compteurAgent = new AgentCount();
		Integer idRefTypeAbsence = RefTypeAbsenceEnum.ASA_A48.getValue();

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);

		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		service.majAgentHistoAlimManuelle(idAgentOperateur, idAgentConcerne, motifCompteur, textLog, compteurAgent,
				idRefTypeAbsence);

		Mockito.verify(counterRepository, Mockito.times(1)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
	}

	@Test
	public void majManuelleCompteurToAgent_OperateurNonHabilite() {

		ReturnMessageDto result = new ReturnMessageDto();
		result.getErrors().add("L'agent 9005138 n'existe pas dans l'AD.");
		Integer idAgent = 9005138;
		CompteurDto compteurDto = new CompteurDto();
		compteurDto.setIdAgent(9005151);

		ISirhWSConsumer wsMock = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(wsMock.isUtilisateurSIRH(idAgent)).thenReturn(result);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(false);

		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", wsMock);

		result = service.majManuelleCompteurToAgent(idAgent, compteurDto);

		assertEquals(1, result.getErrors().size());
		assertEquals("Vous n'êtes pas habilité à mettre à jour le compteur de cet agent.", result.getErrors().get(0)
				.toString());
	}

	@Test
	public void majManuelleCompteurToAgent_MotifCompteurInexistant() {

		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		CompteurDto compteurDto = new CompteurDto();
		compteurDto.setIdAgent(9005151);
		compteurDto.setDureeARetrancher(10.0);
		compteurDto.setIdMotifCompteur(1);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isOperateurOfAgent(idAgent, compteurDto.getIdAgent())).thenReturn(true);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getEntity(MotifCompteur.class, compteurDto.getIdMotifCompteur())).thenReturn(
				null);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(result);

		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		result = service.majManuelleCompteurToAgent(idAgent, compteurDto);

		assertEquals(1, result.getErrors().size());
		assertEquals("Le motif n'existe pas.", result.getErrors().get(0).toString());
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentHistoAlimManuelle.class));
		Mockito.verify(counterRepository, Mockito.times(0)).persistEntity(Mockito.isA(AgentRecupCount.class));
	}

	public void majManuelleCompteurToAgent_prepareData() {

		Mockito.when(accessRightsRepository.isOperateurOfAgent(Mockito.anyInt(), Mockito.anyInt())).thenReturn(true);

		Mockito.when(counterRepository.getEntity(MotifCompteur.class, 1)).thenReturn(new MotifCompteur());
	}
}
