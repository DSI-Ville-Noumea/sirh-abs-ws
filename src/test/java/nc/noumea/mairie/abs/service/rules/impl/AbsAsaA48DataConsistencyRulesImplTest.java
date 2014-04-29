package nc.noumea.mairie.abs.service.rules.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentAsaA48Count;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeAsa;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IAsaRepository;
import nc.noumea.mairie.abs.repository.ICounterRepository;
import nc.noumea.mairie.abs.service.impl.HelperService;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.staticmock.MockStaticEntityMethods;
import org.springframework.test.util.ReflectionTestUtils;

@MockStaticEntityMethods
public class AbsAsaA48DataConsistencyRulesImplTest {

	@Test
	public void checkDroitCompteurAsaA48_aucunDroit() {

		Date dateDebut = new Date();
		AgentAsaA48Count soldeAsaA48 = null;

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounterByDate(AgentAsaA48Count.class, 9005138, dateDebut)).thenReturn(
				soldeAsaA48);

		AbsAsaA48DataConsistencyRulesImpl impl = new AbsAsaA48DataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);

		ReturnMessageDto srm = new ReturnMessageDto();
		Demande demande = new Demande();
		demande.setIdAgent(9005138);
		demande.setDateDebut(dateDebut);

		srm = impl.checkDroitCompteurAsaA48(srm, demande);

		assertEquals(1, srm.getErrors().size());
		assertEquals(srm.getErrors().get(0), "L'agent [9005138] ne possède pas de droit ASA.");
	}

	@Test
	public void checkDroitCompteurAsaA48_compteurNegatif() {

		Date dateDebut = new Date();
		AgentAsaA48Count soldeAsaA48 = new AgentAsaA48Count();
		soldeAsaA48.setTotalJours(0.0);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounterByDate(AgentAsaA48Count.class, 9005138, dateDebut)).thenReturn(
				soldeAsaA48);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.calculNombreJoursArrondiDemiJournee(Mockito.isA(Date.class), Mockito.isA(Date.class)))
				.thenReturn(10.0);

		List<DemandeAsa> listDemandeAsa = new ArrayList<DemandeAsa>();
		listDemandeAsa.addAll(Arrays.asList(new DemandeAsa(), new DemandeAsa()));

		IAsaRepository asaRepository = Mockito.mock(IAsaRepository.class);
		Mockito.when(asaRepository.getListDemandeAsaEnCours(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(RefTypeAbsenceEnum.class))).thenReturn(
				listDemandeAsa);

		AbsAsaA48DataConsistencyRulesImpl impl = new AbsAsaA48DataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(impl, "helperService", helperService);
		ReflectionTestUtils.setField(impl, "asaRepository", asaRepository);

		ReturnMessageDto srm = new ReturnMessageDto();
		DemandeAsa demande = new DemandeAsa();
		demande.setIdAgent(9005138);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(new Date());
		demande.setDuree(10.5);

		srm = impl.checkDroitCompteurAsaA48(srm, demande);

		assertEquals(0, srm.getErrors().size());
		assertEquals(1, srm.getInfos().size());
		assertEquals(srm.getInfos().get(0), AbsAsaDataConsistencyRulesImpl.DEPASSEMENT_DROITS_ASA_MSG);
	}

	@Test
	public void checkDroitCompteurAsaA48_compteurNegatifBis() {

		Date dateDebut = new Date();
		AgentAsaA48Count soldeAsaA48 = new AgentAsaA48Count();
		soldeAsaA48.setTotalJours(9.0);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounterByDate(AgentAsaA48Count.class, 9005138, dateDebut)).thenReturn(
				soldeAsaA48);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.calculNombreJoursArrondiDemiJournee(Mockito.isA(Date.class), Mockito.isA(Date.class)))
				.thenReturn(10.0);

		List<DemandeAsa> listDemandeAsa = new ArrayList<DemandeAsa>();
		listDemandeAsa.addAll(Arrays.asList(new DemandeAsa(), new DemandeAsa()));

		IAsaRepository asaRepository = Mockito.mock(IAsaRepository.class);
		Mockito.when(asaRepository.getListDemandeAsaEnCours(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(RefTypeAbsenceEnum.class))).thenReturn(
				listDemandeAsa);

		AbsAsaA48DataConsistencyRulesImpl impl = new AbsAsaA48DataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(impl, "helperService", helperService);
		ReflectionTestUtils.setField(impl, "asaRepository", asaRepository);

		ReturnMessageDto srm = new ReturnMessageDto();
		DemandeAsa demande = new DemandeAsa();
		demande.setIdAgent(9005138);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(new Date());
		demande.setDuree(10.5);

		srm = impl.checkDroitCompteurAsaA48(srm, demande);

		assertEquals(0, srm.getErrors().size());
		assertEquals(1, srm.getInfos().size());
		assertEquals(srm.getInfos().get(0), AbsAsaDataConsistencyRulesImpl.DEPASSEMENT_DROITS_ASA_MSG);
	}

	@Test
	public void checkDroitCompteurAsaA48_ok() {

		Date dateDebut = new Date();
		AgentAsaA48Count soldeAsaA48 = new AgentAsaA48Count();
		soldeAsaA48.setTotalJours(10.5);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounterByDate(AgentAsaA48Count.class, 9005138, dateDebut)).thenReturn(
				soldeAsaA48);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.calculNombreJoursArrondiDemiJournee(Mockito.isA(Date.class), Mockito.isA(Date.class)))
				.thenReturn(10.0);

		List<DemandeAsa> listDemandeAsa = new ArrayList<DemandeAsa>();
		listDemandeAsa.addAll(Arrays.asList(new DemandeAsa(), new DemandeAsa()));

		IAsaRepository asaRepository = Mockito.mock(IAsaRepository.class);
		Mockito.when(asaRepository.getListDemandeAsaEnCours(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(RefTypeAbsenceEnum.class))).thenReturn(
				listDemandeAsa);

		AbsAsaA48DataConsistencyRulesImpl impl = new AbsAsaA48DataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(impl, "helperService", helperService);
		ReflectionTestUtils.setField(impl, "asaRepository", asaRepository);

		ReturnMessageDto srm = new ReturnMessageDto();
		DemandeAsa demande = new DemandeAsa();
		demande.setIdAgent(9005138);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(new Date());
		demande.setDuree(10.5);

		srm = impl.checkDroitCompteurAsaA48(srm, demande);

		assertEquals(0, srm.getErrors().size());
		assertEquals(0, srm.getInfos().size());
	}

	@Test
	public void checkEtatsDemandeAnnulee_isValidee() {

		ReturnMessageDto srm = new ReturnMessageDto();
		Demande demande = new Demande();
		demande.setIdDemande(1);
		EtatDemande etat = new EtatDemande();
		etat.setEtat(RefEtatEnum.VALIDEE);
		demande.getEtatsDemande().add(etat);

		AbsAsaA48DataConsistencyRulesImpl impl = new AbsAsaA48DataConsistencyRulesImpl();

		srm = impl.checkEtatsDemandeAnnulee(srm, demande, Arrays.asList(RefEtatEnum.PROVISOIRE, RefEtatEnum.SAISIE));

		assertEquals(0, srm.getErrors().size());
	}

	@Test
	public void checkEtatsDemandeAnnulee_isAttente() {

		ReturnMessageDto srm = new ReturnMessageDto();
		Demande demande = new Demande();
		demande.setIdDemande(1);
		EtatDemande etat = new EtatDemande();
		etat.setEtat(RefEtatEnum.EN_ATTENTE);
		demande.getEtatsDemande().add(etat);

		AbsAsaA48DataConsistencyRulesImpl impl = new AbsAsaA48DataConsistencyRulesImpl();

		srm = impl.checkEtatsDemandeAnnulee(srm, demande, Arrays.asList(RefEtatEnum.PROVISOIRE, RefEtatEnum.SAISIE));

		assertEquals(0, srm.getErrors().size());
	}

	@Test
	public void checkEtatsDemandeAnnulee_isPrise() {

		ReturnMessageDto srm = new ReturnMessageDto();
		Demande demande = new Demande();
		demande.setIdDemande(1);
		EtatDemande etat = new EtatDemande();
		etat.setEtat(RefEtatEnum.PRISE);
		demande.getEtatsDemande().add(etat);

		AbsAsaA48DataConsistencyRulesImpl impl = new AbsAsaA48DataConsistencyRulesImpl();

		srm = impl.checkEtatsDemandeAnnulee(srm, demande, Arrays.asList(RefEtatEnum.PROVISOIRE, RefEtatEnum.SAISIE));

		assertEquals(0, srm.getErrors().size());
	}

	@Test
	public void checkEtatsDemandeAnnulee_isRejete() {

		ReturnMessageDto srm = new ReturnMessageDto();
		Demande demande = new Demande();
		demande.setIdDemande(1);
		EtatDemande etat = new EtatDemande();
		etat.setEtat(RefEtatEnum.REJETE);
		demande.getEtatsDemande().add(etat);

		AbsAsaA48DataConsistencyRulesImpl impl = new AbsAsaA48DataConsistencyRulesImpl();

		srm = impl.checkEtatsDemandeAnnulee(srm, demande, Arrays.asList(RefEtatEnum.PROVISOIRE, RefEtatEnum.SAISIE));

		assertEquals(1, srm.getErrors().size());
	}

	@Test
	public void checkDepassementCompteurAgent_aucunCompteur() {

		Date dateDebut = new Date();
		AgentAsaA48Count soldeAsaA48 = null;

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounterByDate(AgentAsaA48Count.class, 9005138, dateDebut)).thenReturn(
				soldeAsaA48);

		AbsAsaA48DataConsistencyRulesImpl impl = new AbsAsaA48DataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(9005138);
		DemandeDto demande = new DemandeDto();
		demande.setAgentWithServiceDto(agDto);
		demande.setDateDebut(dateDebut);

		boolean srm = impl.checkDepassementCompteurAgent(demande);

		assertTrue(srm);
	}

	@Test
	public void checkDepassementCompteurAgent_compteurNegatif() {

		Date dateDebut = new Date();
		AgentAsaA48Count soldeAsaA48 = new AgentAsaA48Count();
		soldeAsaA48.setTotalJours(0.0);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounterByDate(AgentAsaA48Count.class, 9005138, dateDebut)).thenReturn(
				soldeAsaA48);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.calculNombreJoursArrondiDemiJournee(Mockito.isA(Date.class), Mockito.isA(Date.class)))
				.thenReturn(10.0);

		List<DemandeAsa> listDemandeAsa = new ArrayList<DemandeAsa>();
		listDemandeAsa.addAll(Arrays.asList(new DemandeAsa(), new DemandeAsa()));

		IAsaRepository asaRepository = Mockito.mock(IAsaRepository.class);
		Mockito.when(asaRepository.getListDemandeAsaEnCours(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(RefTypeAbsenceEnum.class))).thenReturn(
				listDemandeAsa);

		AbsAsaA48DataConsistencyRulesImpl impl = new AbsAsaA48DataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(impl, "helperService", helperService);
		ReflectionTestUtils.setField(impl, "asaRepository", asaRepository);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(9005138);
		DemandeDto demande = new DemandeDto();
		demande.setAgentWithServiceDto(agDto);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(new Date());
		demande.setDuree(10.5);
		demande.setIdTypeDemande(7);

		boolean srm = impl.checkDepassementCompteurAgent(demande);

		assertTrue(srm);
	}

	@Test
	public void checkDepassementCompteurAgent_compteurOk() {

		Date dateDebut = new Date();
		AgentAsaA48Count soldeAsaA48 = new AgentAsaA48Count();
		soldeAsaA48.setTotalJours(3.0);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounterByDate(AgentAsaA48Count.class, 9005138, dateDebut)).thenReturn(
				soldeAsaA48);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(
				helperService.calculNombreJoursArrondiDemiJournee(Mockito.isA(Date.class), Mockito.isA(Date.class)))
				.thenReturn(1.0);

		List<DemandeAsa> listDemandeAsa = new ArrayList<DemandeAsa>();
		listDemandeAsa.addAll(Arrays.asList(new DemandeAsa(), new DemandeAsa()));

		IAsaRepository asaRepository = Mockito.mock(IAsaRepository.class);
		Mockito.when(asaRepository.getListDemandeAsaEnCours(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(RefTypeAbsenceEnum.class))).thenReturn(
				listDemandeAsa);

		AbsAsaA48DataConsistencyRulesImpl impl = new AbsAsaA48DataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(impl, "helperService", helperService);
		ReflectionTestUtils.setField(impl, "asaRepository", asaRepository);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(9005138);
		DemandeDto demande = new DemandeDto();
		demande.setAgentWithServiceDto(agDto);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(new Date());
		demande.setDuree(1.5);
		demande.setIdTypeDemande(7);

		boolean srm = impl.checkDepassementCompteurAgent(demande);

		assertFalse(srm);
	}
	

	
	@Test
	public void isAfficherBoutonImprimer() {
		
		DemandeDto demandeDto = new DemandeDto();
			demandeDto.setIdRefEtat(RefEtatEnum.PROVISOIRE.getCodeEtat());
			
		AbsAsaA55DataConsistencyRulesImpl impl = new AbsAsaA55DataConsistencyRulesImpl();
		boolean result = impl.isAfficherBoutonImprimer(demandeDto);
		assertFalse(result);
		
		demandeDto.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());
		result = impl.isAfficherBoutonImprimer(demandeDto);
		assertFalse(result);
		
		demandeDto.setIdRefEtat(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat());
		result = impl.isAfficherBoutonImprimer(demandeDto);
		assertFalse(result);
		
		demandeDto.setIdRefEtat(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat());
		result = impl.isAfficherBoutonImprimer(demandeDto);
		assertFalse(result);
		
		demandeDto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());
		result = impl.isAfficherBoutonImprimer(demandeDto);
		assertFalse(result);
		
		demandeDto.setIdRefEtat(RefEtatEnum.REJETE.getCodeEtat());
		result = impl.isAfficherBoutonImprimer(demandeDto);
		assertFalse(result);
		
		demandeDto.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());
		result = impl.isAfficherBoutonImprimer(demandeDto);
		assertTrue(result);
		
		demandeDto.setIdRefEtat(RefEtatEnum.REFUSEE.getCodeEtat());
		result = impl.isAfficherBoutonImprimer(demandeDto);
		assertFalse(result);
		
		demandeDto.setIdRefEtat(RefEtatEnum.EN_ATTENTE.getCodeEtat());
		result = impl.isAfficherBoutonImprimer(demandeDto);
		assertFalse(result);
		
		demandeDto.setIdRefEtat(RefEtatEnum.PRISE.getCodeEtat());
		result = impl.isAfficherBoutonImprimer(demandeDto);
		assertFalse(result);
		
		demandeDto.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
		result = impl.isAfficherBoutonImprimer(demandeDto);
		assertFalse(result);
	}
	
	@Test
	public void isAfficherBoutonAnnuler() {
		
		DemandeDto demandeDto = new DemandeDto();
			demandeDto.setIdRefEtat(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat());
		
		AbsAsaA55DataConsistencyRulesImpl impl = new AbsAsaA55DataConsistencyRulesImpl();
		boolean result = impl.isAfficherBoutonAnnuler(demandeDto);
		assertTrue(result);
		
		demandeDto.setIdRefEtat(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto);
		assertTrue(result);
		
		demandeDto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto);
		assertTrue(result);
		
		demandeDto.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto);
		assertTrue(result);
		
		demandeDto.setIdRefEtat(RefEtatEnum.EN_ATTENTE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto);
		assertTrue(result);
		
		demandeDto.setIdRefEtat(RefEtatEnum.PRISE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto);
		assertTrue(result);
		
		demandeDto.setIdRefEtat(RefEtatEnum.PROVISOIRE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto);
		assertFalse(result);
		
		demandeDto.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto);
		assertFalse(result);
		
		demandeDto.setIdRefEtat(RefEtatEnum.REFUSEE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto);
		assertFalse(result);
		
		demandeDto.setIdRefEtat(RefEtatEnum.REJETE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto);
		assertFalse(result);
		
		demandeDto.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto);
		assertFalse(result);
	}
	
	@Test
	public void filtreDroitOfDemandeSIRH() {
		
		AgentWithServiceDto agDto11 = new AgentWithServiceDto();
			agDto11.setIdAgent(9005140);
		AgentWithServiceDto agDto10 = new AgentWithServiceDto();
			agDto10.setIdAgent(9005139);
		AgentWithServiceDto agDto9 = new AgentWithServiceDto();
			agDto9.setIdAgent(9005138);
		AgentWithServiceDto agDto8 = new AgentWithServiceDto();
			agDto8.setIdAgent(9005137);
		AgentWithServiceDto agDto7 = new AgentWithServiceDto();
			agDto7.setIdAgent(9005136);
		AgentWithServiceDto agDto6 = new AgentWithServiceDto();
			agDto6.setIdAgent(9005135);
		AgentWithServiceDto agDto5 = new AgentWithServiceDto();
			agDto5.setIdAgent(9005134);
		AgentWithServiceDto agDto4 = new AgentWithServiceDto();
			agDto4.setIdAgent(9005133);
		AgentWithServiceDto agDto3 = new AgentWithServiceDto();
			agDto3.setIdAgent(9005132);
		AgentWithServiceDto agDto2 = new AgentWithServiceDto();
			agDto2.setIdAgent(9005131);
		AgentWithServiceDto agDto1 = new AgentWithServiceDto();
			agDto1.setIdAgent(9005130);
		
		// les demandes
		DemandeDto demandeDtoProvisoire = new DemandeDto();
			demandeDtoProvisoire.setIdRefEtat(RefEtatEnum.PROVISOIRE.getCodeEtat());
			demandeDtoProvisoire.setAgentWithServiceDto(agDto1);
		DemandeDto demandeDtoSaisie = new DemandeDto();
			demandeDtoSaisie.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());
			demandeDtoSaisie.setAgentWithServiceDto(agDto2);
		DemandeDto demandeDtoApprouve = new DemandeDto();
			demandeDtoApprouve.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());
			demandeDtoApprouve.setAgentWithServiceDto(agDto3);
		DemandeDto demandeDtoRefusee = new DemandeDto();
			demandeDtoRefusee.setIdRefEtat(RefEtatEnum.REFUSEE.getCodeEtat());
			demandeDtoRefusee.setAgentWithServiceDto(agDto4);
		DemandeDto demandeDtoVisee_F = new DemandeDto();
			demandeDtoVisee_F.setIdRefEtat(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat());
			demandeDtoVisee_F.setAgentWithServiceDto(agDto5);
		DemandeDto demandeDtoVisee_D = new DemandeDto();
			demandeDtoVisee_D.setIdRefEtat(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat());
			demandeDtoVisee_D.setAgentWithServiceDto(agDto6);
		DemandeDto demandeDtoPrise = new DemandeDto();
			demandeDtoPrise.setIdRefEtat(RefEtatEnum.PRISE.getCodeEtat());
			demandeDtoPrise.setAgentWithServiceDto(agDto7);
		DemandeDto demandeDtoAnnulee = new DemandeDto();
			demandeDtoAnnulee.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
			demandeDtoAnnulee.setAgentWithServiceDto(agDto8);
		DemandeDto demandeDtoValidee = new DemandeDto();
			demandeDtoValidee.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());
			demandeDtoValidee.setAgentWithServiceDto(agDto9);
		DemandeDto demandeDtoRejetee = new DemandeDto();
			demandeDtoRejetee.setIdRefEtat(RefEtatEnum.REJETE.getCodeEtat());
			demandeDtoRejetee.setAgentWithServiceDto(agDto10);
		DemandeDto demandeDtoEnAttente = new DemandeDto();
			demandeDtoEnAttente.setIdRefEtat(RefEtatEnum.EN_ATTENTE.getCodeEtat());
			demandeDtoEnAttente.setAgentWithServiceDto(agDto11);
		
			AbsAsaA55DataConsistencyRulesImpl service = new AbsAsaA55DataConsistencyRulesImpl();

		// When
		DemandeDto result1 = service.filtreDroitOfDemandeSIRH(demandeDtoProvisoire);
		DemandeDto result2 = service.filtreDroitOfDemandeSIRH(demandeDtoSaisie);
		DemandeDto result3 = service.filtreDroitOfDemandeSIRH(demandeDtoApprouve);
		DemandeDto result4 = service.filtreDroitOfDemandeSIRH(demandeDtoRefusee);
		DemandeDto result5 = service.filtreDroitOfDemandeSIRH(demandeDtoVisee_F);
		DemandeDto result6 = service.filtreDroitOfDemandeSIRH(demandeDtoVisee_D);
		DemandeDto result7 = service.filtreDroitOfDemandeSIRH(demandeDtoPrise);
		DemandeDto result8 = service.filtreDroitOfDemandeSIRH(demandeDtoAnnulee);
		DemandeDto result9 = service.filtreDroitOfDemandeSIRH(demandeDtoValidee);
		DemandeDto result10 = service.filtreDroitOfDemandeSIRH(demandeDtoRejetee);
		DemandeDto result11 = service.filtreDroitOfDemandeSIRH(demandeDtoEnAttente);

		// Then
		assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), result1.getIdRefEtat().intValue());
		assertEquals(9005130, result1.getAgentWithServiceDto().getIdAgent().intValue());
		assertFalse(result1.isAffichageApprobation());
		assertFalse(result1.isAffichageBoutonAnnuler());
		assertFalse(result1.isAffichageBoutonImprimer());
		assertFalse(result1.isAffichageBoutonModifier());
		assertFalse(result1.isAffichageBoutonSupprimer());
		assertFalse(result1.isAffichageBoutonDupliquer());
		assertFalse(result1.isAffichageVisa());
		assertFalse(result1.isModifierApprobation());
		assertFalse(result1.isModifierVisa());
		assertNull(result1.getValeurApprobation());
		assertNull(result1.getValeurVisa());
		assertFalse(result1.isAffichageValidation());
		assertFalse(result1.isModifierValidation());
		assertNull(result1.getValeurValidation());

		assertEquals(RefEtatEnum.SAISIE.getCodeEtat(), result2.getIdRefEtat().intValue());
		assertEquals(9005131, result2.getAgentWithServiceDto().getIdAgent().intValue());
		assertFalse(result2.isAffichageApprobation());
		assertFalse(result2.isAffichageBoutonAnnuler());
		assertFalse(result2.isAffichageBoutonImprimer());
		assertFalse(result2.isAffichageBoutonModifier());
		assertFalse(result2.isAffichageBoutonSupprimer());
		assertFalse(result2.isAffichageBoutonDupliquer());
		assertFalse(result2.isAffichageVisa());
		assertFalse(result2.isModifierApprobation());
		assertFalse(result2.isModifierVisa());
		assertNull(result2.getValeurApprobation());
		assertNull(result2.getValeurVisa());
		assertFalse(result2.isAffichageValidation());
		assertFalse(result2.isModifierValidation());
		assertNull(result2.getValeurValidation());

		assertEquals(RefEtatEnum.APPROUVEE.getCodeEtat(), result3.getIdRefEtat().intValue());
		assertEquals(9005132, result3.getAgentWithServiceDto().getIdAgent().intValue());
		assertFalse(result3.isAffichageApprobation());
		assertTrue(result3.isAffichageBoutonAnnuler());
		assertFalse(result3.isAffichageBoutonImprimer());
		assertFalse(result3.isAffichageBoutonModifier());
		assertFalse(result3.isAffichageBoutonSupprimer());
		assertTrue(result3.isAffichageBoutonDupliquer());
		assertFalse(result3.isAffichageVisa());
		assertFalse(result3.isModifierApprobation());
		assertFalse(result3.isModifierVisa());
		assertNull(result3.getValeurApprobation());
		assertNull(result3.getValeurVisa());
		assertTrue(result3.isAffichageValidation());
		assertTrue(result3.isModifierValidation());
		assertNull(result3.getValeurValidation());

		assertEquals(RefEtatEnum.REFUSEE.getCodeEtat(), result4.getIdRefEtat().intValue());
		assertEquals(9005133, result4.getAgentWithServiceDto().getIdAgent().intValue());
		assertFalse(result4.isAffichageApprobation());
		assertFalse(result4.isAffichageBoutonAnnuler());
		assertFalse(result4.isAffichageBoutonImprimer());
		assertFalse(result4.isAffichageBoutonModifier());
		assertFalse(result4.isAffichageBoutonSupprimer());
		assertFalse(result4.isAffichageBoutonDupliquer());
		assertFalse(result4.isAffichageVisa());
		assertFalse(result4.isModifierApprobation());
		assertFalse(result4.isModifierVisa());
		assertNull(result4.getValeurApprobation());
		assertNull(result4.getValeurVisa());
		assertFalse(result4.isAffichageValidation());
		assertFalse(result4.isModifierValidation());
		assertNull(result4.getValeurValidation());

		assertEquals(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat(), result5.getIdRefEtat().intValue());
		assertEquals(9005134, result5.getAgentWithServiceDto().getIdAgent().intValue());
		assertFalse(result5.isAffichageApprobation());
		assertTrue(result5.isAffichageBoutonAnnuler());
		assertFalse(result5.isAffichageBoutonImprimer());
		assertFalse(result5.isAffichageBoutonModifier());
		assertFalse(result5.isAffichageBoutonSupprimer());
		assertFalse(result5.isAffichageBoutonDupliquer());
		assertFalse(result5.isAffichageVisa());
		assertFalse(result5.isModifierApprobation());
		assertFalse(result5.isModifierVisa());
		assertNull(result5.getValeurApprobation());
		assertNull(result5.getValeurVisa());
		assertFalse(result5.isAffichageValidation());
		assertFalse(result5.isModifierValidation());
		assertNull(result5.getValeurValidation());

		assertEquals(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat(), result6.getIdRefEtat().intValue());
		assertEquals(9005135, result6.getAgentWithServiceDto().getIdAgent().intValue());
		assertFalse(result6.isAffichageApprobation());
		assertTrue(result6.isAffichageBoutonAnnuler());
		assertFalse(result6.isAffichageBoutonImprimer());
		assertFalse(result6.isAffichageBoutonModifier());
		assertFalse(result6.isAffichageBoutonSupprimer());
		assertFalse(result6.isAffichageBoutonDupliquer());
		assertFalse(result6.isAffichageVisa());
		assertFalse(result6.isModifierApprobation());
		assertFalse(result6.isModifierVisa());
		assertNull(result6.getValeurApprobation());
		assertNull(result6.getValeurVisa());
		assertFalse(result6.isAffichageValidation());
		assertFalse(result6.isModifierValidation());
		assertNull(result6.getValeurValidation());

		assertEquals(RefEtatEnum.PRISE.getCodeEtat(), result7.getIdRefEtat().intValue());
		assertEquals(9005136, result7.getAgentWithServiceDto().getIdAgent().intValue());
		assertFalse(result7.isAffichageApprobation());
		assertTrue(result7.isAffichageBoutonAnnuler());
		assertFalse(result7.isAffichageBoutonImprimer());
		assertFalse(result7.isAffichageBoutonModifier());
		assertFalse(result7.isAffichageBoutonSupprimer());
		assertFalse(result7.isAffichageBoutonDupliquer());
		assertFalse(result7.isAffichageVisa());
		assertFalse(result7.isModifierApprobation());
		assertFalse(result7.isModifierVisa());
		assertNull(result7.getValeurApprobation());
		assertNull(result7.getValeurVisa());
		assertTrue(result7.isAffichageValidation());
		assertFalse(result7.isModifierValidation());
		assertNull(result7.getValeurValidation());

		assertEquals(RefEtatEnum.ANNULEE.getCodeEtat(), result8.getIdRefEtat().intValue());
		assertEquals(9005137, result8.getAgentWithServiceDto().getIdAgent().intValue());
		assertFalse(result8.isAffichageApprobation());
		assertFalse(result8.isAffichageBoutonAnnuler());
		assertFalse(result8.isAffichageBoutonImprimer());
		assertFalse(result8.isAffichageBoutonModifier());
		assertFalse(result8.isAffichageBoutonSupprimer());
		assertFalse(result8.isAffichageBoutonDupliquer());
		assertFalse(result8.isAffichageVisa());
		assertFalse(result8.isModifierApprobation());
		assertFalse(result8.isModifierVisa());
		assertNull(result8.getValeurApprobation());
		assertNull(result8.getValeurVisa());
		assertFalse(result8.isAffichageValidation());
		assertFalse(result8.isModifierValidation());
		assertNull(result8.getValeurValidation());

		assertEquals(RefEtatEnum.VALIDEE.getCodeEtat(), result9.getIdRefEtat().intValue());
		assertEquals(9005138, result9.getAgentWithServiceDto().getIdAgent().intValue());
		assertFalse(result9.isAffichageApprobation());
		assertTrue(result9.isAffichageBoutonAnnuler());
		assertFalse(result9.isAffichageBoutonImprimer());
		assertFalse(result9.isAffichageBoutonModifier());
		assertFalse(result9.isAffichageBoutonSupprimer());
		assertFalse(result9.isAffichageBoutonDupliquer());
		assertFalse(result9.isAffichageVisa());
		assertFalse(result9.isModifierApprobation());
		assertFalse(result9.isModifierVisa());
		assertNull(result9.getValeurApprobation());
		assertNull(result9.getValeurVisa());
		assertTrue(result9.isAffichageValidation());
		assertFalse(result9.isModifierValidation());
		assertNull(result9.getValeurValidation());

		assertEquals(RefEtatEnum.REJETE.getCodeEtat(), result10.getIdRefEtat().intValue());
		assertEquals(9005139, result10.getAgentWithServiceDto().getIdAgent().intValue());
		assertFalse(result10.isAffichageApprobation());
		assertFalse(result10.isAffichageBoutonAnnuler());
		assertFalse(result10.isAffichageBoutonImprimer());
		assertFalse(result10.isAffichageBoutonModifier());
		assertFalse(result10.isAffichageBoutonSupprimer());
		assertFalse(result10.isAffichageBoutonDupliquer());
		assertFalse(result10.isAffichageVisa());
		assertFalse(result10.isModifierApprobation());
		assertFalse(result10.isModifierVisa());
		assertNull(result10.getValeurApprobation());
		assertNull(result10.getValeurVisa());
		assertTrue(result10.isAffichageValidation());
		assertFalse(result10.isModifierValidation());
		assertNull(result10.getValeurValidation());

		assertEquals(RefEtatEnum.EN_ATTENTE.getCodeEtat(), result11.getIdRefEtat().intValue());
		assertEquals(9005140, result11.getAgentWithServiceDto().getIdAgent().intValue());
		assertFalse(result11.isAffichageApprobation());
		assertTrue(result11.isAffichageBoutonAnnuler());
		assertFalse(result11.isAffichageBoutonImprimer());
		assertFalse(result11.isAffichageBoutonModifier());
		assertFalse(result11.isAffichageBoutonSupprimer());
		assertFalse(result11.isAffichageBoutonDupliquer());
		assertFalse(result11.isAffichageVisa());
		assertFalse(result11.isModifierApprobation());
		assertFalse(result11.isModifierVisa());
		assertNull(result11.getValeurApprobation());
		assertNull(result11.getValeurVisa());
		assertTrue(result11.isAffichageValidation());
		assertTrue(result11.isModifierValidation());
		assertNull(result11.getValeurValidation());
	}
}
