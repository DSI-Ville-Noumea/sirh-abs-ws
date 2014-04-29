package nc.noumea.mairie.abs.service.rules.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeRecup;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.RefEtat;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IDemandeRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.abs.service.impl.HelperService;
import nc.noumea.mairie.domain.Spadmn;
import nc.noumea.mairie.sirh.domain.Agent;
import nc.noumea.mairie.ws.ISirhWSConsumer;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.staticmock.MockStaticEntityMethods;
import org.springframework.test.util.ReflectionTestUtils;

@MockStaticEntityMethods
public class DefaultAbsenceDataConsistencyRulesImplTest {

	@Test
	public void checkDemandeDejaSaisieSurMemePeriode_withEtatExistantProvisoireAndRefuse() {

		ReturnMessageDto srm = new ReturnMessageDto();

		Demande demande = new Demande();
		demande.setIdDemande(1);
		demande.setIdAgent(9005138);

		EtatDemande etat = new EtatDemande();
		etat.setEtat(RefEtatEnum.PROVISOIRE);
		demande.getEtatsDemande().add(etat);

		List<Demande> listDemande = new ArrayList<Demande>();
		Demande demandeExist1 = new Demande();
		EtatDemande etat1 = new EtatDemande();
		etat1.setEtat(RefEtatEnum.REFUSEE);
		demandeExist1.getEtatsDemande().add(etat1);
		demandeExist1.setIdDemande(2);
		Demande demandeExist2 = new Demande();
		EtatDemande etat2 = new EtatDemande();
		etat2.setEtat(RefEtatEnum.PROVISOIRE);
		demandeExist2.getEtatsDemande().add(etat2);
		demandeExist2.setIdDemande(3);
		listDemande.addAll(Arrays.asList(demandeExist1, demandeExist2));

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.listeDemandesAgent(null, demande.getIdAgent(), null, null, null)).thenReturn(
				listDemande);

		DefaultAbsenceDataConsistencyRulesImpl impl = new DefaultAbsenceDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "demandeRepository", demandeRepository);

		srm = impl.checkDemandeDejaSaisieSurMemePeriode(srm, demande);

		assertEquals(0, srm.getErrors().size());
	}

	@Test
	public void checkDemandeDejaSaisieSurMemePeriode_Ok() {

		ReturnMessageDto srm = new ReturnMessageDto();

		Demande demande = new Demande();
		demande.setIdDemande(1);
		demande.setIdAgent(9005138);
		demande.setDateDebut(new LocalDateTime(2013, 9, 10, 12, 1).toDate());
		demande.setDateFin(new LocalDateTime(2013, 9, 20, 13, 59).toDate());

		EtatDemande etat = new EtatDemande();
		etat.setEtat(RefEtatEnum.PROVISOIRE);
		demande.getEtatsDemande().add(etat);

		List<Demande> listDemande = new ArrayList<Demande>();
		Demande demandeExist1 = new Demande();
		EtatDemande etat1 = new EtatDemande();
		etat1.setEtat(RefEtatEnum.SAISIE);
		demandeExist1.getEtatsDemande().add(etat1);
		demandeExist1.setDateDebut(new LocalDateTime(2013, 9, 1, 14, 0).toDate());
		demandeExist1.setDateFin(new LocalDateTime(2013, 9, 10, 12, 0).toDate());
		demandeExist1.setIdDemande(2);
		Demande demandeExist2 = new Demande();
		EtatDemande etat2 = new EtatDemande();
		etat2.setEtat(RefEtatEnum.APPROUVEE);
		demandeExist2.getEtatsDemande().add(etat2);
		demandeExist2.setDateDebut(new LocalDateTime(2013, 9, 20, 14, 0).toDate());
		demandeExist2.setDateFin(new LocalDateTime(2013, 9, 30, 0, 0).toDate());
		demandeExist2.setIdDemande(3);
		listDemande.addAll(Arrays.asList(demandeExist1, demandeExist2));

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.listeDemandesAgent(null, demande.getIdAgent(), null, null, null)).thenReturn(
				listDemande);

		DefaultAbsenceDataConsistencyRulesImpl impl = new DefaultAbsenceDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "demandeRepository", demandeRepository);

		srm = impl.checkDemandeDejaSaisieSurMemePeriode(srm, demande);

		assertEquals(0, srm.getErrors().size());
	}

	@Test
	public void checkDemandeDejaSaisieSurMemePeriode_DateFinKo() {

		ReturnMessageDto srm = new ReturnMessageDto();

		Demande demande = new Demande();
		demande.setIdDemande(1);
		demande.setIdAgent(9005138);
		demande.setDateDebut(new LocalDateTime(2013, 9, 10, 12, 1).toDate());
		demande.setDateFin(new LocalDateTime(2013, 9, 20, 14, 0).toDate());

		EtatDemande etat = new EtatDemande();
		etat.setEtat(RefEtatEnum.PROVISOIRE);
		demande.getEtatsDemande().add(etat);

		List<Demande> listDemande = new ArrayList<Demande>();
		Demande demandeExist1 = new Demande();
		EtatDemande etat1 = new EtatDemande();
		etat1.setEtat(RefEtatEnum.SAISIE);
		demandeExist1.getEtatsDemande().add(etat1);
		demandeExist1.setDateDebut(new LocalDateTime(2013, 9, 1, 14, 0).toDate());
		demandeExist1.setDateFin(new LocalDateTime(2013, 9, 10, 12, 0).toDate());
		demandeExist1.setIdDemande(2);
		Demande demandeExist2 = new Demande();
		EtatDemande etat2 = new EtatDemande();
		etat2.setEtat(RefEtatEnum.APPROUVEE);
		demandeExist2.getEtatsDemande().add(etat2);
		demandeExist2.setDateDebut(new LocalDateTime(2013, 9, 20, 14, 0).toDate());
		demandeExist2.setDateFin(new LocalDateTime(2013, 9, 30, 0, 0).toDate());
		demandeExist2.setIdDemande(3);
		listDemande.addAll(Arrays.asList(demandeExist1, demandeExist2));

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.listeDemandesAgent(null, demande.getIdAgent(), null, null, null)).thenReturn(
				listDemande);

		DefaultAbsenceDataConsistencyRulesImpl impl = new DefaultAbsenceDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "demandeRepository", demandeRepository);

		srm = impl.checkDemandeDejaSaisieSurMemePeriode(srm, demande);

		assertEquals(1, srm.getErrors().size());
		assertEquals("La demande ne peut être couverte totalement ou partiellement par une autre absence.", srm
				.getErrors().get(0).toString());
	}

	@Test
	public void checkDemandeDejaSaisieSurMemePeriode_DateDebutKo() {

		ReturnMessageDto srm = new ReturnMessageDto();

		Demande demande = new Demande();
		demande.setIdDemande(1);
		demande.setIdAgent(9005138);
		demande.setDateDebut(new LocalDateTime(2013, 9, 10, 12, 0).toDate());
		demande.setDateFin(new LocalDateTime(2013, 9, 20, 13, 58).toDate());

		EtatDemande etat = new EtatDemande();
		etat.setEtat(RefEtatEnum.PROVISOIRE);
		demande.getEtatsDemande().add(etat);

		List<Demande> listDemande = new ArrayList<Demande>();
		Demande demandeExist1 = new Demande();
		EtatDemande etat1 = new EtatDemande();
		etat1.setEtat(RefEtatEnum.SAISIE);
		demandeExist1.getEtatsDemande().add(etat1);
		demandeExist1.setDateDebut(new LocalDateTime(2013, 9, 1, 14, 0).toDate());
		demandeExist1.setDateFin(new LocalDateTime(2013, 9, 10, 12, 0).toDate());
		demandeExist1.setIdDemande(2);
		Demande demandeExist2 = new Demande();
		EtatDemande etat2 = new EtatDemande();
		etat2.setEtat(RefEtatEnum.APPROUVEE);
		demandeExist2.getEtatsDemande().add(etat2);
		demandeExist2.setDateDebut(new LocalDateTime(2013, 9, 20, 14, 0).toDate());
		demandeExist2.setDateFin(new LocalDateTime(2013, 9, 30, 0, 0).toDate());
		demandeExist2.setIdDemande(3);
		listDemande.addAll(Arrays.asList(demandeExist1, demandeExist2));

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.listeDemandesAgent(null, demande.getIdAgent(), null, null, null)).thenReturn(
				listDemande);

		DefaultAbsenceDataConsistencyRulesImpl impl = new DefaultAbsenceDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "demandeRepository", demandeRepository);

		srm = impl.checkDemandeDejaSaisieSurMemePeriode(srm, demande);

		assertEquals(1, srm.getErrors().size());
		assertEquals("La demande ne peut être couverte totalement ou partiellement par une autre absence.", srm
				.getErrors().get(0).toString());
	}

	@Test
	public void checkAgentInactivity_AgentActif() {

		List<String> activitesCode = Arrays.asList("01", "02", "03", "04", "23", "24", "60", "61", "62", "63", "64",
				"65", "66");

		ReturnMessageDto srm = new ReturnMessageDto();
		Integer idAgent = 9005138;
		Agent ag = new Agent();
		Spadmn adm = new Spadmn();

		Date date = new Date();

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgent(idAgent)).thenReturn(ag);
		Mockito.when(sirhRepository.getAgentCurrentPosition(ag, date)).thenReturn(adm);

		DefaultAbsenceDataConsistencyRulesImpl impl = new DefaultAbsenceDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "sirhRepository", sirhRepository);

		for (String codeAcivite : activitesCode) {
			adm.setCdpadm(codeAcivite);
			srm = impl.checkAgentInactivity(srm, idAgent, date);
		}

		assertEquals(0, srm.getErrors().size());
	}

	@Test
	public void checkAgentInactivity_AgentInactif() {

		ReturnMessageDto srm = new ReturnMessageDto();
		Integer idAgent = 9005138;
		Agent ag = new Agent();
		Spadmn adm = new Spadmn();
		adm.setCdpadm("05");

		Date date = new Date();

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgent(idAgent)).thenReturn(ag);
		Mockito.when(sirhRepository.getAgentCurrentPosition(ag, date)).thenReturn(adm);

		DefaultAbsenceDataConsistencyRulesImpl impl = new DefaultAbsenceDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "sirhRepository", sirhRepository);

		srm = impl.checkAgentInactivity(srm, idAgent, date);

		assertEquals(1, srm.getErrors().size());
		assertEquals("L'agent n'est pas en activité sur cette période.", srm.getErrors().get(0).toString());
	}

	@Test
	public void checkChampMotifPourEtatDonne_motifVide_Refuse() {

		ReturnMessageDto srm = new ReturnMessageDto();

		DefaultAbsenceDataConsistencyRulesImpl impl = new DefaultAbsenceDataConsistencyRulesImpl();
		srm = impl.checkChampMotifPourEtatDonne(srm, RefEtatEnum.REFUSEE.getCodeEtat(), null);

		assertEquals(1, srm.getErrors().size());
		assertEquals("Le motif est obligatoire.", srm.getErrors().get(0).toString());
	}

	@Test
	public void checkChampMotifPourEtatDonne_motifVide_ViseeD() {

		ReturnMessageDto srm = new ReturnMessageDto();

		DefaultAbsenceDataConsistencyRulesImpl impl = new DefaultAbsenceDataConsistencyRulesImpl();
		srm = impl.checkChampMotifPourEtatDonne(srm, RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat(), null);

		assertEquals(1, srm.getErrors().size());
		assertEquals("Le motif est obligatoire.", srm.getErrors().get(0).toString());
	}

	@Test
	public void checkChampMotifPourEtatDonne_Ok_motifSaisi() {

		ReturnMessageDto srm = new ReturnMessageDto();

		DefaultAbsenceDataConsistencyRulesImpl impl = new DefaultAbsenceDataConsistencyRulesImpl();
		srm = impl.checkChampMotifPourEtatDonne(srm, RefEtatEnum.REFUSEE.getCodeEtat(), "motif");

		assertEquals(0, srm.getErrors().size());
	}

	@Test
	public void checkChampMotifPourEtatDonne_Ok_motifNonSaisi_EtatApprouve() {

		ReturnMessageDto srm = new ReturnMessageDto();

		DefaultAbsenceDataConsistencyRulesImpl impl = new DefaultAbsenceDataConsistencyRulesImpl();
		srm = impl.checkChampMotifPourEtatDonne(srm, RefEtatEnum.APPROUVEE.getCodeEtat(), null);

		assertEquals(0, srm.getErrors().size());
	}

	@Test
	public void checkChampMotifPourEtatDonne_Ok_motifSaisi_EtatApprouve() {

		ReturnMessageDto srm = new ReturnMessageDto();

		DefaultAbsenceDataConsistencyRulesImpl impl = new DefaultAbsenceDataConsistencyRulesImpl();
		srm = impl.checkChampMotifPourEtatDonne(srm, RefEtatEnum.APPROUVEE.getCodeEtat(), "motif");

		assertEquals(0, srm.getErrors().size());
	}

	@Test
	public void verifDemandeExiste_demandeExiste() {
		ReturnMessageDto srm = new ReturnMessageDto();
		Demande demande = new Demande();

		DefaultAbsenceDataConsistencyRulesImpl impl = new DefaultAbsenceDataConsistencyRulesImpl();
		srm = impl.verifDemandeExiste(demande, srm);

		assertEquals(0, srm.getErrors().size());
	}

	@Test
	public void verifDemandeExiste_demandeNotExiste() {
		ReturnMessageDto srm = new ReturnMessageDto();
		Demande demande = null;

		DefaultAbsenceDataConsistencyRulesImpl impl = new DefaultAbsenceDataConsistencyRulesImpl();
		srm = impl.verifDemandeExiste(demande, srm);

		assertEquals(1, srm.getErrors().size());
	}

	@Test
	public void checkEtatsDemandeAcceptes_isProvisoire() {

		ReturnMessageDto srm = new ReturnMessageDto();
		Demande demande = new Demande();
		demande.setIdDemande(1);
		EtatDemande etat = new EtatDemande();
		etat.setEtat(RefEtatEnum.PROVISOIRE);
		demande.getEtatsDemande().add(etat);

		DefaultAbsenceDataConsistencyRulesImpl impl = new DefaultAbsenceDataConsistencyRulesImpl();
		srm = impl.checkEtatsDemandeAcceptes(srm, demande, Arrays.asList(RefEtatEnum.PROVISOIRE, RefEtatEnum.SAISIE));

		assertEquals(0, srm.getErrors().size());
	}

	@Test
	public void checkEtatsDemandeAcceptes_isSaisie() {

		ReturnMessageDto srm = new ReturnMessageDto();
		Demande demande = new Demande();
		demande.setIdDemande(1);
		EtatDemande etat = new EtatDemande();
		etat.setEtat(RefEtatEnum.SAISIE);
		demande.getEtatsDemande().add(etat);

		DefaultAbsenceDataConsistencyRulesImpl impl = new DefaultAbsenceDataConsistencyRulesImpl();
		srm = impl.checkEtatsDemandeAcceptes(srm, demande, Arrays.asList(RefEtatEnum.PROVISOIRE, RefEtatEnum.SAISIE));

		assertEquals(0, srm.getErrors().size());
	}

	@Test
	public void checkEtatsDemandeAcceptes_isNotSaisie_Et_NotProvisoire() {

		ReturnMessageDto srm = new ReturnMessageDto();
		Demande demande = new Demande();
		demande.setIdDemande(1);
		EtatDemande etat = new EtatDemande();
		etat.setEtat(RefEtatEnum.APPROUVEE);
		demande.getEtatsDemande().add(etat);

		DefaultAbsenceDataConsistencyRulesImpl impl = new DefaultAbsenceDataConsistencyRulesImpl();
		srm = impl.checkEtatsDemandeAcceptes(srm, demande, Arrays.asList(RefEtatEnum.PROVISOIRE, RefEtatEnum.SAISIE));

		assertEquals(1, srm.getErrors().size());
		assertEquals("La modification de la demande [1] n'est autorisée que si l'état est à [PROVISOIRE SAISIE ].", srm
				.getErrors().get(0).toString());
	}

	@Test
	public void filtreDateAndEtatDemandeFromList_WrongParam() {

		DefaultAbsenceDataConsistencyRulesImpl impl = new DefaultAbsenceDataConsistencyRulesImpl();

		// When
		List<DemandeDto> result = impl.filtreDateAndEtatDemandeFromList(new ArrayList<Demande>(), null, new Date());

		// Then
		assertEquals(0, result.size());
	}

	@Test
	public void filtreDateAndEtatDemandeFromList_EtatAndDateDemandeFilter_NoResult_BadEtat() {

		RefEtat etatProvisoire = new RefEtat();
		etatProvisoire.setIdRefEtat(0);
		etatProvisoire.setLabel("PROVISOIRE");

		RefEtat etatPris = new RefEtat();
		etatPris.setIdRefEtat(6);
		etatPris.setLabel("PRISE");

		RefEtat etatSaisie = new RefEtat();
		etatSaisie.setIdRefEtat(1);
		etatSaisie.setLabel("SAISIE");

		ArrayList<Demande> listeDemande = new ArrayList<Demande>();
		RefTypeAbsence refType = new RefTypeAbsence();
		refType.setIdRefTypeAbsence(3);
		refType.setLabel("RECUP");
		EtatDemande etat1 = new EtatDemande();
		etat1.setDate(new Date());
		etat1.setEtat(RefEtatEnum.SAISIE);
		etat1.setIdEtatDemande(1);
		EtatDemande etat2 = new EtatDemande();
		etat2.setDate(new Date());
		etat2.setEtat(RefEtatEnum.PROVISOIRE);
		etat2.setIdEtatDemande(2);
		DemandeRecup d = new DemandeRecup();
		etat1.setDemande(d);
		d.setIdDemande(1);
		d.setType(refType);
		d.setEtatsDemande(Arrays.asList(etat1));
		d.setDateDebut(new Date());
		d.setDuree(30);
		DemandeRecup d2 = new DemandeRecup();
		etat2.setDemande(d2);
		d2.setIdDemande(2);
		d2.setType(refType);
		d2.setEtatsDemande(Arrays.asList(etat2));
		d2.setDateDebut(new Date());
		d2.setDuree(50);
		listeDemande.add(d);
		listeDemande.add(d2);

		EntityManager emMock = Mockito.mock(EntityManager.class);
		Mockito.when(emMock.find(RefEtat.class, RefEtatEnum.SAISIE.getCodeEtat())).thenReturn(etatSaisie);
		Mockito.when(emMock.find(RefEtat.class, RefEtatEnum.PROVISOIRE.getCodeEtat())).thenReturn(etatProvisoire);
		Mockito.when(emMock.find(RefEtat.class, RefEtatEnum.PRISE.getCodeEtat())).thenReturn(etatPris);

		Date date = new Date();
		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(date);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgentService(d.getIdAgent(), date)).thenReturn(new AgentWithServiceDto());
		Mockito.when(sirhWSConsumer.getAgentService(d2.getIdAgent(), date)).thenReturn(new AgentWithServiceDto());

		DefaultAbsenceDataConsistencyRulesImpl service = new DefaultAbsenceDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(service, "absEntityManager", emMock);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", helperService);

		// When
		List<DemandeDto> result = service.filtreDateAndEtatDemandeFromList(listeDemande, Arrays.asList(etatPris),
				new Date());
		// Then
		assertEquals(0, result.size());
	}

	@Test
	public void filtreDateAndEtatDemandeFromList_WithDateDemandeFilter_1Result() {

		// Given
		RefEtat etatProvisoire = new RefEtat();
		etatProvisoire.setIdRefEtat(0);
		etatProvisoire.setLabel("PROVISOIRE");
		RefEtat etatSaisie = new RefEtat();
		etatSaisie.setIdRefEtat(1);
		etatSaisie.setLabel("SAISIE");

		RefTypeAbsence refType = new RefTypeAbsence();
		refType.setIdRefTypeAbsence(3);
		refType.setLabel("RECUP");

		ArrayList<Demande> listeDemande = new ArrayList<Demande>();
		EtatDemande etat1 = new EtatDemande();
		etat1.setDate(new LocalDate(2014, 1, 1).toDate());
		etat1.setEtat(RefEtatEnum.SAISIE);
		etat1.setIdEtatDemande(1);
		DemandeRecup d = new DemandeRecup();
		etat1.setDemande(d);
		d.setIdDemande(1);
		d.setType(refType);
		d.setEtatsDemande(Arrays.asList(etat1));
		d.setDateDebut(new Date());
		d.setDuree(30);

		EtatDemande etat2 = new EtatDemande();
		etat2.setDate(new LocalDate(2014, 1, 8).toDate());
		etat2.setEtat(RefEtatEnum.PROVISOIRE);
		etat2.setIdEtatDemande(2);
		DemandeRecup d2 = new DemandeRecup();
		etat2.setDemande(d2);
		d2.setIdDemande(2);
		d2.setType(refType);
		d2.setEtatsDemande(Arrays.asList(etat2));
		d2.setDateDebut(new Date());
		d2.setDuree(50);
		listeDemande.add(d);
		listeDemande.add(d2);

		EntityManager emMock = Mockito.mock(EntityManager.class);
		Mockito.when(emMock.find(RefEtat.class, RefEtatEnum.SAISIE.getCodeEtat())).thenReturn(etatSaisie);
		Mockito.when(emMock.find(RefEtat.class, RefEtatEnum.PROVISOIRE.getCodeEtat())).thenReturn(etatProvisoire);

		Date date = new Date();
		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(date);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgentService(d.getIdAgent(), date)).thenReturn(new AgentWithServiceDto());
		Mockito.when(sirhWSConsumer.getAgentService(d2.getIdAgent(), date)).thenReturn(new AgentWithServiceDto());

		DefaultAbsenceDataConsistencyRulesImpl service = new DefaultAbsenceDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(service, "absEntityManager", emMock);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", helperService);

		// When
		List<DemandeDto> result = service.filtreDateAndEtatDemandeFromList(listeDemande,
				Arrays.asList(etatProvisoire, etatSaisie), new LocalDate(2014, 1, 8).toDate());

		// Then
		assertEquals(1, result.size());
		assertEquals("50.0", result.get(0).getDuree().toString());
		assertFalse(result.get(0).isAffichageBoutonImprimer());
		assertFalse(result.get(0).isAffichageBoutonModifier());
		assertFalse(result.get(0).isAffichageBoutonSupprimer());
	}

	@Test
	public void filtreDateAndEtatDemandeFromList_EtatAndDateDemandeFilter_1result() {

		// Given
		Integer idAgent = 9005138;

		RefEtat etatProvisoire = new RefEtat();
		etatProvisoire.setIdRefEtat(0);
		etatProvisoire.setLabel("PROVISOIRE");

		RefEtat etatSaisie = new RefEtat();
		etatSaisie.setIdRefEtat(1);
		etatSaisie.setLabel("SAISIE");

		RefTypeAbsence refType = new RefTypeAbsence();
		refType.setIdRefTypeAbsence(3);
		refType.setLabel("RECUP");

		ArrayList<Demande> listeDemande = new ArrayList<Demande>();
		EtatDemande etat1 = new EtatDemande();
		etat1.setDate(new Date());
		etat1.setEtat(RefEtatEnum.SAISIE);
		etat1.setIdAgent(idAgent);
		etat1.setIdEtatDemande(1);
		EtatDemande etat2 = new EtatDemande();
		etat2.setDate(new Date());
		etat2.setEtat(RefEtatEnum.PROVISOIRE);
		etat2.setIdAgent(idAgent);
		etat2.setIdEtatDemande(2);
		DemandeRecup d = new DemandeRecup();
		etat1.setDemande(d);
		d.setIdDemande(1);
		d.setIdAgent(idAgent);
		d.setType(refType);
		d.setEtatsDemande(Arrays.asList(etat1));
		d.setDateDebut(new Date());
		d.setDuree(30);
		DemandeRecup d2 = new DemandeRecup();
		etat2.setDemande(d2);
		d2.setIdDemande(2);
		d2.setIdAgent(idAgent);
		d2.setType(refType);
		d2.setEtatsDemande(Arrays.asList(etat2));
		d2.setDateDebut(new Date());
		d2.setDuree(50);
		listeDemande.add(d);
		listeDemande.add(d2);

		EntityManager emMock = Mockito.mock(EntityManager.class);
		Mockito.when(emMock.find(RefEtat.class, RefEtatEnum.SAISIE.getCodeEtat())).thenReturn(etatSaisie);
		Mockito.when(emMock.find(RefEtat.class, RefEtatEnum.PROVISOIRE.getCodeEtat())).thenReturn(etatProvisoire);

		Date date = new Date();
		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(date);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgentService(d.getIdAgent(), date)).thenReturn(new AgentWithServiceDto());
		Mockito.when(sirhWSConsumer.getAgentService(d2.getIdAgent(), date)).thenReturn(new AgentWithServiceDto());

		DefaultAbsenceDataConsistencyRulesImpl service = new DefaultAbsenceDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(service, "absEntityManager", emMock);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", helperService);

		// When
		List<DemandeDto> result = service.filtreDateAndEtatDemandeFromList(listeDemande, Arrays.asList(etatProvisoire),
				new Date());

		// Then
		assertEquals(1, result.size());
		assertEquals("50.0", result.get(0).getDuree().toString());
		assertFalse(result.get(0).isAffichageBoutonImprimer());
		assertFalse(result.get(0).isAffichageBoutonModifier());
		assertFalse(result.get(0).isAffichageBoutonSupprimer());
	}

	@Test
	public void filtreDateAndEtatDemandeFromList_WithEtatFilter_1result() {

		// Given
		Integer idAgent = 9005138;

		RefEtat etatProvisoire = new RefEtat();
		etatProvisoire.setIdRefEtat(0);
		etatProvisoire.setLabel("PROVISOIRE");

		RefEtat etatSaisie = new RefEtat();
		etatSaisie.setIdRefEtat(1);
		etatSaisie.setLabel("SAISIE");

		RefTypeAbsence refType = new RefTypeAbsence();
		refType.setIdRefTypeAbsence(3);
		refType.setLabel("RECUP");

		ArrayList<Demande> listeDemande = new ArrayList<Demande>();
		EtatDemande etat1 = new EtatDemande();
		etat1.setDate(new Date());
		etat1.setEtat(RefEtatEnum.SAISIE);
		etat1.setIdAgent(idAgent);
		etat1.setIdEtatDemande(1);
		DemandeRecup d = new DemandeRecup();
		etat1.setDemande(d);
		d.setIdDemande(1);
		d.setIdAgent(idAgent);
		d.setType(refType);
		d.setEtatsDemande(Arrays.asList(etat1));
		d.setDateDebut(new Date());
		d.setDuree(30);

		EtatDemande etat2 = new EtatDemande();
		etat2.setDate(new Date());
		etat2.setEtat(RefEtatEnum.PROVISOIRE);
		etat2.setIdAgent(idAgent);
		etat2.setIdEtatDemande(2);
		DemandeRecup d2 = new DemandeRecup();
		etat2.setDemande(d2);
		d2.setIdDemande(2);
		d2.setIdAgent(idAgent);
		d2.setType(refType);
		d2.setEtatsDemande(Arrays.asList(etat2));
		d2.setDateDebut(new Date());
		d2.setDuree(50);
		listeDemande.add(d);
		listeDemande.add(d2);

		EntityManager emMock = Mockito.mock(EntityManager.class);
		Mockito.when(emMock.find(RefEtat.class, RefEtatEnum.SAISIE.getCodeEtat())).thenReturn(etatSaisie);
		Mockito.when(emMock.find(RefEtat.class, RefEtatEnum.PROVISOIRE.getCodeEtat())).thenReturn(etatProvisoire);

		Date date = new Date();
		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(date);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgentService(d.getIdAgent(), date)).thenReturn(new AgentWithServiceDto());
		Mockito.when(sirhWSConsumer.getAgentService(d2.getIdAgent(), date)).thenReturn(new AgentWithServiceDto());

		DefaultAbsenceDataConsistencyRulesImpl service = new DefaultAbsenceDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(service, "absEntityManager", emMock);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", helperService);

		// When
		List<DemandeDto> result = service.filtreDateAndEtatDemandeFromList(listeDemande, Arrays.asList(etatProvisoire),
				null);

		// Then
		assertEquals(1, result.size());
		assertEquals("50.0", result.get(0).getDuree().toString());
		assertFalse(result.get(0).isAffichageBoutonImprimer());
		assertFalse(result.get(0).isAffichageBoutonModifier());
		assertFalse(result.get(0).isAffichageBoutonSupprimer());
	}

	@Test
	public void filtreDateAndEtatDemandeFromList_ToutesWithNoFilter_2results() {

		// Given
		Integer idAgent = 9005138;

		RefEtat etatProvisoire = new RefEtat();
		etatProvisoire.setIdRefEtat(0);
		etatProvisoire.setLabel("PROVISOIRE");

		RefEtat etatSaisie = new RefEtat();
		etatSaisie.setIdRefEtat(1);
		etatSaisie.setLabel("SAISIE");

		RefTypeAbsence refType = new RefTypeAbsence();
		refType.setIdRefTypeAbsence(3);
		refType.setLabel("RECUP");

		ArrayList<Demande> listeDemande = new ArrayList<Demande>();
		EtatDemande etat1 = new EtatDemande();
		etat1.setDate(new Date());
		etat1.setEtat(RefEtatEnum.SAISIE);
		etat1.setIdAgent(idAgent);
		etat1.setIdEtatDemande(1);
		etat1.setMotif("motif");
		EtatDemande etat2 = new EtatDemande();
		etat2.setDate(new Date());
		etat2.setEtat(RefEtatEnum.PROVISOIRE);
		etat2.setIdAgent(idAgent);
		etat2.setIdEtatDemande(2);
		etat2.setMotif("motif");
		DemandeRecup d = new DemandeRecup();
		etat1.setDemande(d);
		d.setIdDemande(1);
		d.setIdAgent(idAgent);
		d.setType(refType);
		d.setEtatsDemande(Arrays.asList(etat1));
		d.setDateDebut(new Date());
		d.setDuree(30);
		DemandeRecup d2 = new DemandeRecup();
		etat2.setDemande(d2);
		d2.setIdDemande(2);
		d2.setIdAgent(idAgent);
		d2.setType(refType);
		d2.setEtatsDemande(Arrays.asList(etat2));
		d2.setDateDebut(new Date());
		d2.setDuree(50);
		listeDemande.add(d);
		listeDemande.add(d2);

		EntityManager emMock = Mockito.mock(EntityManager.class);
		Mockito.when(emMock.find(RefEtat.class, RefEtatEnum.SAISIE.getCodeEtat())).thenReturn(etatSaisie);
		Mockito.when(emMock.find(RefEtat.class, RefEtatEnum.PROVISOIRE.getCodeEtat())).thenReturn(etatProvisoire);

		Date date = new Date();
		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(date);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgentService(d.getIdAgent(), date)).thenReturn(new AgentWithServiceDto());
		Mockito.when(sirhWSConsumer.getAgentService(d2.getIdAgent(), date)).thenReturn(new AgentWithServiceDto());

		DefaultAbsenceDataConsistencyRulesImpl service = new DefaultAbsenceDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(service, "absEntityManager", emMock);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", helperService);

		// When
		List<DemandeDto> result = service.filtreDateAndEtatDemandeFromList(listeDemande, null, null);

		// Then
		assertEquals(2, result.size());
		assertEquals("30.0", result.get(0).getDuree().toString());
		assertFalse(result.get(0).isAffichageBoutonImprimer());
		assertFalse(result.get(0).isAffichageBoutonModifier());
		assertFalse(result.get(0).isAffichageBoutonSupprimer());
		assertEquals("motif", result.get(0).getMotif());
		assertEquals("50.0", result.get(1).getDuree().toString());
		assertFalse(result.get(1).isAffichageBoutonImprimer());
		assertFalse(result.get(1).isAffichageBoutonModifier());
		assertFalse(result.get(1).isAffichageBoutonSupprimer());
		assertEquals("motif", result.get(1).getMotif());
	}

	@Test
	public void filtreDateAndEtatDemandeFromList_DemandesEnCours_4results() {

		// Given
		Integer idAgent = 9005138;
		RefEtat etatProvisoire = new RefEtat();
		etatProvisoire.setIdRefEtat(0);
		etatProvisoire.setLabel("PROVISOIRE");
		RefEtat etatSaisie = new RefEtat();
		etatSaisie.setIdRefEtat(1);
		etatSaisie.setLabel("SAISIE");
		RefEtat etatVISEE_F = new RefEtat();
		etatVISEE_F.setIdRefEtat(2);
		etatVISEE_F.setLabel("VISEE_F");
		RefEtat etatVISEE_D = new RefEtat();
		etatVISEE_D.setIdRefEtat(3);
		etatVISEE_D.setLabel("VISEE_D");
		RefEtat etatApprouve = new RefEtat();
		etatApprouve.setIdRefEtat(4);
		etatApprouve.setLabel("APPROUVEE");
		RefEtat etatRefusee = new RefEtat();
		etatRefusee.setIdRefEtat(5);
		etatRefusee.setLabel("REFUSEE");
		RefEtat etatPrise = new RefEtat();
		etatPrise.setIdRefEtat(6);
		etatPrise.setLabel("PRISE");
		RefEtat etatAnnulee = new RefEtat();
		etatAnnulee.setIdRefEtat(7);
		etatAnnulee.setLabel("ANNULEE");

		List<RefEtat> refEtatEnCours = new ArrayList<RefEtat>();
		refEtatEnCours.add(etatSaisie);
		refEtatEnCours.add(etatVISEE_F);
		refEtatEnCours.add(etatVISEE_D);
		refEtatEnCours.add(etatApprouve);

		RefTypeAbsence refType = new RefTypeAbsence();
		refType.setIdRefTypeAbsence(3);
		refType.setLabel("RECUP");

		ArrayList<Demande> listeDemande = new ArrayList<Demande>();
		EtatDemande etat1 = new EtatDemande();
		etat1.setDate(new Date());
		etat1.setEtat(RefEtatEnum.SAISIE);
		etat1.setIdAgent(idAgent);
		etat1.setIdEtatDemande(1);
		etat1.setMotif("motif");
		DemandeRecup d = new DemandeRecup();
		etat1.setDemande(d);
		d.setIdDemande(1);
		d.setIdAgent(idAgent);
		d.setType(refType);
		d.setEtatsDemande(Arrays.asList(etat1));
		d.setDateDebut(new Date());
		d.setDuree(30);
		EtatDemande etat2 = new EtatDemande();
		etat2.setDate(new Date());
		etat2.setEtat(RefEtatEnum.PROVISOIRE);
		etat2.setIdAgent(idAgent);
		etat2.setIdEtatDemande(2);
		DemandeRecup d2 = new DemandeRecup();
		etat2.setDemande(d2);
		d2.setIdDemande(2);
		d2.setIdAgent(idAgent);
		d2.setType(refType);
		d2.setEtatsDemande(Arrays.asList(etat2));
		d2.setDateDebut(new Date());
		d2.setDuree(50);
		EtatDemande etat3 = new EtatDemande();
		etat3.setDate(new Date());
		etat3.setEtat(RefEtatEnum.VISEE_FAVORABLE);
		etat3.setIdAgent(idAgent);
		etat3.setIdEtatDemande(3);
		DemandeRecup d3 = new DemandeRecup();
		etat3.setDemande(d3);
		d3.setIdDemande(3);
		d3.setIdAgent(idAgent);
		d3.setType(refType);
		d3.setEtatsDemande(Arrays.asList(etat3));
		d3.setDateDebut(new Date());
		d3.setDuree(50);
		EtatDemande etat4 = new EtatDemande();
		etat4.setDate(new Date());
		etat4.setEtat(RefEtatEnum.VISEE_DEFAVORABLE);
		etat4.setIdAgent(idAgent);
		etat4.setIdEtatDemande(4);
		DemandeRecup d4 = new DemandeRecup();
		etat4.setDemande(d4);
		d4.setIdDemande(4);
		d4.setIdAgent(idAgent);
		d4.setType(refType);
		d4.setEtatsDemande(Arrays.asList(etat4));
		d4.setDateDebut(new Date());
		d4.setDuree(50);
		EtatDemande etat5 = new EtatDemande();
		etat5.setDate(new Date());
		etat5.setEtat(RefEtatEnum.APPROUVEE);
		etat5.setIdAgent(idAgent);
		etat5.setIdEtatDemande(5);
		DemandeRecup d5 = new DemandeRecup();
		etat5.setDemande(d5);
		d5.setIdDemande(5);
		d5.setIdAgent(idAgent);
		d5.setType(refType);
		d5.setEtatsDemande(Arrays.asList(etat5));
		d5.setDateDebut(new Date());
		d5.setDuree(50);
		EtatDemande etat6 = new EtatDemande();
		etat6.setDate(new Date());
		etat6.setEtat(RefEtatEnum.REFUSEE);
		etat6.setIdAgent(idAgent);
		etat6.setIdEtatDemande(6);
		DemandeRecup d6 = new DemandeRecup();
		etat6.setDemande(d6);
		d6.setIdDemande(6);
		d6.setIdAgent(idAgent);
		d6.setType(refType);
		d6.setEtatsDemande(Arrays.asList(etat6));
		d6.setDateDebut(new Date());
		d6.setDuree(50);
		EtatDemande etat7 = new EtatDemande();
		etat7.setDate(new Date());
		etat7.setEtat(RefEtatEnum.PRISE);
		etat7.setIdAgent(idAgent);
		etat7.setIdEtatDemande(7);
		DemandeRecup d7 = new DemandeRecup();
		etat7.setDemande(d7);
		d7.setIdDemande(7);
		d7.setIdAgent(idAgent);
		d7.setType(refType);
		d7.setEtatsDemande(Arrays.asList(etat7));
		d7.setDateDebut(new Date());
		d7.setDuree(50);
		EtatDemande etat8 = new EtatDemande();
		etat8.setDate(new Date());
		etat8.setEtat(RefEtatEnum.ANNULEE);
		etat8.setIdAgent(idAgent);
		etat8.setIdEtatDemande(8);
		DemandeRecup d8 = new DemandeRecup();
		etat8.setDemande(d8);
		d8.setIdDemande(8);
		d8.setIdAgent(idAgent);
		d8.setType(refType);
		d8.setEtatsDemande(Arrays.asList(etat8));
		d8.setDateDebut(new Date());
		d8.setDuree(50);
		listeDemande.add(d);
		listeDemande.add(d2);
		listeDemande.add(d3);
		listeDemande.add(d4);
		listeDemande.add(d5);
		listeDemande.add(d6);
		listeDemande.add(d7);
		listeDemande.add(d8);

		EntityManager emMock = Mockito.mock(EntityManager.class);
		Mockito.when(emMock.find(RefEtat.class, RefEtatEnum.SAISIE.getCodeEtat())).thenReturn(etatSaisie);
		Mockito.when(emMock.find(RefEtat.class, RefEtatEnum.PROVISOIRE.getCodeEtat())).thenReturn(etatProvisoire);
		Mockito.when(emMock.find(RefEtat.class, RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat())).thenReturn(etatVISEE_D);
		Mockito.when(emMock.find(RefEtat.class, RefEtatEnum.VISEE_FAVORABLE.getCodeEtat())).thenReturn(etatVISEE_F);
		Mockito.when(emMock.find(RefEtat.class, RefEtatEnum.APPROUVEE.getCodeEtat())).thenReturn(etatApprouve);
		Mockito.when(emMock.find(RefEtat.class, RefEtatEnum.REFUSEE.getCodeEtat())).thenReturn(etatRefusee);
		Mockito.when(emMock.find(RefEtat.class, RefEtatEnum.PRISE.getCodeEtat())).thenReturn(etatPrise);
		Mockito.when(emMock.find(RefEtat.class, RefEtatEnum.ANNULEE.getCodeEtat())).thenReturn(etatAnnulee);

		Date date = new Date();
		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(date);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgentService(d.getIdAgent(), date)).thenReturn(new AgentWithServiceDto());
		Mockito.when(sirhWSConsumer.getAgentService(d2.getIdAgent(), date)).thenReturn(new AgentWithServiceDto());
		Mockito.when(sirhWSConsumer.getAgentService(d3.getIdAgent(), date)).thenReturn(new AgentWithServiceDto());
		Mockito.when(sirhWSConsumer.getAgentService(d4.getIdAgent(), date)).thenReturn(new AgentWithServiceDto());
		Mockito.when(sirhWSConsumer.getAgentService(d5.getIdAgent(), date)).thenReturn(new AgentWithServiceDto());
		Mockito.when(sirhWSConsumer.getAgentService(d6.getIdAgent(), date)).thenReturn(new AgentWithServiceDto());
		Mockito.when(sirhWSConsumer.getAgentService(d7.getIdAgent(), date)).thenReturn(new AgentWithServiceDto());
		Mockito.when(sirhWSConsumer.getAgentService(d8.getIdAgent(), date)).thenReturn(new AgentWithServiceDto());

		DefaultAbsenceDataConsistencyRulesImpl service = new DefaultAbsenceDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(service, "absEntityManager", emMock);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", helperService);

		// When
		List<DemandeDto> result = service.filtreDateAndEtatDemandeFromList(listeDemande,
				Arrays.asList(etatVISEE_F, etatSaisie, etatVISEE_D, etatApprouve), null);

		// Then
		assertEquals(4, result.size());
		assertEquals("30.0", result.get(0).getDuree().toString());
		assertFalse(result.get(0).isAffichageBoutonImprimer());
		assertFalse(result.get(0).isAffichageBoutonModifier());
		assertFalse(result.get(0).isAffichageBoutonSupprimer());
		assertEquals("motif", result.get(0).getMotif());
	}

	@Test
	public void filtreDateAndEtatDemandeFromList_DemandesNonPrises_1result() {

		// Given
		Integer idAgent = 9005138;
		RefEtat etatPris = new RefEtat();
		etatPris.setIdRefEtat(6);
		etatPris.setLabel("PRISE");

		List<RefEtat> refEtatNonPris = new ArrayList<RefEtat>();
		RefEtat etatSaisie = new RefEtat();
		etatSaisie.setIdRefEtat(1);
		etatSaisie.setLabel("SAISIE");
		refEtatNonPris.add(etatSaisie);

		ArrayList<Demande> listeDemande = new ArrayList<Demande>();
		RefTypeAbsence refType = new RefTypeAbsence();
		refType.setIdRefTypeAbsence(3);
		refType.setLabel("RECUP");
		EtatDemande etat1 = new EtatDemande();
		etat1.setDate(new Date());
		etat1.setEtat(RefEtatEnum.SAISIE);
		etat1.setIdAgent(idAgent);
		etat1.setIdEtatDemande(1);
		EtatDemande etat2 = new EtatDemande();
		etat2.setDate(new Date());
		etat2.setEtat(RefEtatEnum.PRISE);
		etat2.setIdAgent(idAgent);
		etat2.setIdEtatDemande(2);
		DemandeRecup d = new DemandeRecup();
		etat1.setDemande(d);
		d.setIdDemande(1);
		d.setIdAgent(idAgent);
		d.setType(refType);
		d.setEtatsDemande(Arrays.asList(etat1));
		d.setDateDebut(new Date());
		d.setDuree(30);
		DemandeRecup d2 = new DemandeRecup();
		etat2.setDemande(d2);
		d2.setIdDemande(2);
		d2.setIdAgent(idAgent);
		d2.setType(refType);
		d2.setEtatsDemande(Arrays.asList(etat2));
		d2.setDateDebut(new Date());
		d2.setDuree(50);
		listeDemande.add(d);
		listeDemande.add(d2);

		EntityManager emMock = Mockito.mock(EntityManager.class);
		Mockito.when(emMock.find(RefEtat.class, RefEtatEnum.SAISIE.getCodeEtat())).thenReturn(etatSaisie);
		Mockito.when(emMock.find(RefEtat.class, RefEtatEnum.PRISE.getCodeEtat())).thenReturn(etatPris);

		Date date = new Date();
		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(date);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgentService(d.getIdAgent(), date)).thenReturn(new AgentWithServiceDto());
		Mockito.when(sirhWSConsumer.getAgentService(d2.getIdAgent(), date)).thenReturn(new AgentWithServiceDto());

		DefaultAbsenceDataConsistencyRulesImpl service = new DefaultAbsenceDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(service, "absEntityManager", emMock);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", helperService);

		// When
		List<DemandeDto> result = service.filtreDateAndEtatDemandeFromList(listeDemande, Arrays.asList(etatSaisie),
				new Date());

		// Then
		assertEquals(1, result.size());
		assertEquals("30.0", result.get(0).getDuree().toString());
		assertFalse(result.get(0).isAffichageBoutonImprimer());
		assertFalse(result.get(0).isAffichageBoutonModifier());
		assertFalse(result.get(0).isAffichageBoutonSupprimer());
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
		
		DefaultAbsenceDataConsistencyRulesImpl service = new DefaultAbsenceDataConsistencyRulesImpl();

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
		assertFalse(result3.isAffichageValidation());
		assertFalse(result3.isModifierValidation());
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
		assertFalse(result7.isAffichageBoutonAnnuler());
		assertFalse(result7.isAffichageBoutonImprimer());
		assertFalse(result7.isAffichageBoutonModifier());
		assertFalse(result7.isAffichageBoutonSupprimer());
		assertFalse(result7.isAffichageBoutonDupliquer());
		assertFalse(result7.isAffichageVisa());
		assertFalse(result7.isModifierApprobation());
		assertFalse(result7.isModifierVisa());
		assertNull(result7.getValeurApprobation());
		assertNull(result7.getValeurVisa());
		assertFalse(result7.isAffichageValidation());
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
		assertFalse(result9.isAffichageBoutonAnnuler());
		assertFalse(result9.isAffichageBoutonImprimer());
		assertFalse(result9.isAffichageBoutonModifier());
		assertFalse(result9.isAffichageBoutonSupprimer());
		assertFalse(result9.isAffichageBoutonDupliquer());
		assertFalse(result9.isAffichageVisa());
		assertFalse(result9.isModifierApprobation());
		assertFalse(result9.isModifierVisa());
		assertNull(result9.getValeurApprobation());
		assertNull(result9.getValeurVisa());
		assertFalse(result9.isAffichageValidation());
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
		assertFalse(result10.isAffichageValidation());
		assertFalse(result10.isModifierValidation());
		assertNull(result10.getValeurValidation());

		assertEquals(RefEtatEnum.EN_ATTENTE.getCodeEtat(), result11.getIdRefEtat().intValue());
		assertEquals(9005140, result11.getAgentWithServiceDto().getIdAgent().intValue());
		assertFalse(result11.isAffichageApprobation());
		assertFalse(result11.isAffichageBoutonAnnuler());
		assertFalse(result11.isAffichageBoutonImprimer());
		assertFalse(result11.isAffichageBoutonModifier());
		assertFalse(result11.isAffichageBoutonSupprimer());
		assertFalse(result11.isAffichageBoutonDupliquer());
		assertFalse(result11.isAffichageVisa());
		assertFalse(result11.isModifierApprobation());
		assertFalse(result11.isModifierVisa());
		assertNull(result11.getValeurApprobation());
		assertNull(result11.getValeurVisa());
		assertFalse(result11.isAffichageValidation());
		assertFalse(result11.isModifierValidation());
		assertNull(result11.getValeurValidation());
	}
}
