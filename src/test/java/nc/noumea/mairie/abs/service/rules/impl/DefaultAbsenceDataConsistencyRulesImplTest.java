package nc.noumea.mairie.abs.service.rules.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeRecup;
import nc.noumea.mairie.abs.domain.DroitDroitsAgent;
import nc.noumea.mairie.abs.domain.DroitProfil;
import nc.noumea.mairie.abs.domain.DroitsAgent;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.Profil;
import nc.noumea.mairie.abs.domain.ProfilEnum;
import nc.noumea.mairie.abs.domain.RefEtat;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IAccessRightsRepository;
import nc.noumea.mairie.abs.repository.IDemandeRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.abs.service.impl.HelperService;
import nc.noumea.mairie.domain.Spadmn;
import nc.noumea.mairie.domain.Spcarr;
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

	protected AbstractAbsenceDataConsistencyRules impl = new DefaultAbsenceDataConsistencyRulesImpl();
	
	protected DemandeDto result1 = null;
	protected DemandeDto result2 = null;
	protected DemandeDto result3 = null;
	protected DemandeDto result4 = null;
	protected DemandeDto result5 = null;
	protected DemandeDto result6 = null;
	protected DemandeDto result7 = null;
	protected DemandeDto result8 = null;
	protected DemandeDto result9 = null;
	protected DemandeDto result10 = null;
	protected DemandeDto result11 = null;
	
	public void allTest(AbstractAbsenceDataConsistencyRules pImpl) throws Throwable {
		
		impl = pImpl;
		if(null == impl) {
			impl = new DefaultAbsenceDataConsistencyRulesImpl();
		}
		
		checkDemandeDejaSaisieSurMemePeriode_withEtatExistantProvisoireAndRefuse();
		checkDemandeDejaSaisieSurMemePeriode_Ok();
		checkDemandeDejaSaisieSurMemePeriode_DateFinKo();
		checkDemandeDejaSaisieSurMemePeriode_DateDebutKo();
		checkAgentInactivity_AgentActif();
		checkAgentInactivity_AgentInactif();
		checkChampMotifPourEtatDonne_motifVide_Refuse();
		checkChampMotifPourEtatDonne_motifVide_ViseeD();
		checkChampMotifPourEtatDonne_Ok_motifSaisi();
		checkChampMotifPourEtatDonne_Ok_motifNonSaisi_EtatApprouve();
		checkChampMotifPourEtatDonne_Ok_motifSaisi_EtatApprouve();
		verifDemandeExiste_demandeExiste();
		verifDemandeExiste_demandeNotExiste();
		checkEtatsDemandeAcceptes_isProvisoire();
		checkEtatsDemandeAcceptes_isSaisie();
		checkEtatsDemandeAcceptes_isNotSaisie_Et_NotProvisoire();
		filtreDateAndEtatDemandeFromList_WrongParam();
		filtreDateAndEtatDemandeFromList_EtatAndDateDemandeFilter_NoResult_BadEtat();
		filtreDateAndEtatDemandeFromList_WithDateDemandeFilter_1Result();
		filtreDateAndEtatDemandeFromList_EtatAndDateDemandeFilter_1result();
		filtreDateAndEtatDemandeFromList_WithEtatFilter_1result();
		filtreDateAndEtatDemandeFromList_ToutesWithNoFilter_2results();
		filtreDateAndEtatDemandeFromList_DemandesEnCours_4results();
		filtreDateAndEtatDemandeFromList_DemandesNonPrises_1result();
		filtreDroitOfDemandeSIRH();
		isAfficherBoutonAnnuler();
		filtreDroitOfListeDemandesByDemande_Delegataire();
		filtreDroitOfListeDemandesByDemande_Approbateur();
		filtreDroitOfListeDemandesByDemande_Viseur();
		filtreDroitOfListeDemandesByDemande_Operateur();
		filtreDroitOfListeDemandesByDemande_DemandeOfAgent();
		checkStatutAgentFonctionnaire_ok();
		checkStatutAgentFonctionnaire_ko();
	}
	
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
		
		ReflectionTestUtils.setField(impl, "sirhRepository", sirhRepository);

		srm = impl.checkAgentInactivity(srm, idAgent, date);

		assertEquals(1, srm.getErrors().size());
		assertEquals("L'agent n'est pas en activité sur cette période.", srm.getErrors().get(0).toString());
	}

	@Test
	public void checkChampMotifPourEtatDonne_motifVide_Refuse() {

		ReturnMessageDto srm = new ReturnMessageDto();

		srm = impl.checkChampMotifPourEtatDonne(srm, RefEtatEnum.REFUSEE.getCodeEtat(), null);

		assertEquals(1, srm.getErrors().size());
		assertEquals("Le motif est obligatoire.", srm.getErrors().get(0).toString());
	}

	@Test
	public void checkChampMotifPourEtatDonne_motifVide_ViseeD() {

		ReturnMessageDto srm = new ReturnMessageDto();
		
		srm = impl.checkChampMotifPourEtatDonne(srm, RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat(), null);

		assertEquals(1, srm.getErrors().size());
		assertEquals("Le motif est obligatoire.", srm.getErrors().get(0).toString());
	}

	@Test
	public void checkChampMotifPourEtatDonne_Ok_motifSaisi() {

		ReturnMessageDto srm = new ReturnMessageDto();
		
		srm = impl.checkChampMotifPourEtatDonne(srm, RefEtatEnum.REFUSEE.getCodeEtat(), "motif");

		assertEquals(0, srm.getErrors().size());
	}

	@Test
	public void checkChampMotifPourEtatDonne_Ok_motifNonSaisi_EtatApprouve() {

		ReturnMessageDto srm = new ReturnMessageDto();
		
		srm = impl.checkChampMotifPourEtatDonne(srm, RefEtatEnum.APPROUVEE.getCodeEtat(), null);

		assertEquals(0, srm.getErrors().size());
	}

	@Test
	public void checkChampMotifPourEtatDonne_Ok_motifSaisi_EtatApprouve() {

		ReturnMessageDto srm = new ReturnMessageDto();
		
		srm = impl.checkChampMotifPourEtatDonne(srm, RefEtatEnum.APPROUVEE.getCodeEtat(), "motif");

		assertEquals(0, srm.getErrors().size());
	}

	@Test
	public void verifDemandeExiste_demandeExiste() {
		ReturnMessageDto srm = new ReturnMessageDto();
		Demande demande = new Demande();

		srm = impl.verifDemandeExiste(demande, srm);

		assertEquals(0, srm.getErrors().size());
	}

	@Test
	public void verifDemandeExiste_demandeNotExiste() {
		ReturnMessageDto srm = new ReturnMessageDto();
		Demande demande = null;

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

		srm = impl.checkEtatsDemandeAcceptes(srm, demande, Arrays.asList(RefEtatEnum.PROVISOIRE, RefEtatEnum.SAISIE));

		assertEquals(1, srm.getErrors().size());
		assertEquals("La modification de la demande [1] n'est autorisée que si l'état est à [PROVISOIRE SAISIE ].", srm
				.getErrors().get(0).toString());
	}

	@Test
	public void filtreDateAndEtatDemandeFromList_WrongParam() {

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

		ReflectionTestUtils.setField(impl, "absEntityManager", emMock);
		ReflectionTestUtils.setField(impl, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(impl, "helperService", helperService);

		// When
		List<DemandeDto> result = impl.filtreDateAndEtatDemandeFromList(listeDemande, Arrays.asList(etatPris),
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

		
		ReflectionTestUtils.setField(impl, "absEntityManager", emMock);
		ReflectionTestUtils.setField(impl, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(impl, "helperService", helperService);

		// When
		List<DemandeDto> result = impl.filtreDateAndEtatDemandeFromList(listeDemande,
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

		
		ReflectionTestUtils.setField(impl, "absEntityManager", emMock);
		ReflectionTestUtils.setField(impl, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(impl, "helperService", helperService);

		// When
		List<DemandeDto> result = impl.filtreDateAndEtatDemandeFromList(listeDemande, Arrays.asList(etatProvisoire),
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

		
		ReflectionTestUtils.setField(impl, "absEntityManager", emMock);
		ReflectionTestUtils.setField(impl, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(impl, "helperService", helperService);

		// When
		List<DemandeDto> result = impl.filtreDateAndEtatDemandeFromList(listeDemande, Arrays.asList(etatProvisoire),
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

		ReflectionTestUtils.setField(impl, "absEntityManager", emMock);
		ReflectionTestUtils.setField(impl, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(impl, "helperService", helperService);

		// When
		List<DemandeDto> result = impl.filtreDateAndEtatDemandeFromList(listeDemande, null, null);

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

		ReflectionTestUtils.setField(impl, "absEntityManager", emMock);
		ReflectionTestUtils.setField(impl, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(impl, "helperService", helperService);

		// When
		List<DemandeDto> result = impl.filtreDateAndEtatDemandeFromList(listeDemande,
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

		ReflectionTestUtils.setField(impl, "absEntityManager", emMock);
		ReflectionTestUtils.setField(impl, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(impl, "helperService", helperService);

		// When
		List<DemandeDto> result = impl.filtreDateAndEtatDemandeFromList(listeDemande, Arrays.asList(etatSaisie),
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
		
		// When
		result1 = impl.filtreDroitOfDemandeSIRH(demandeDtoProvisoire);
		result2 = impl.filtreDroitOfDemandeSIRH(demandeDtoSaisie);
		result3 = impl.filtreDroitOfDemandeSIRH(demandeDtoApprouve);
		result4 = impl.filtreDroitOfDemandeSIRH(demandeDtoRefusee);
		result5 = impl.filtreDroitOfDemandeSIRH(demandeDtoVisee_F);
		result6 = impl.filtreDroitOfDemandeSIRH(demandeDtoVisee_D);
		result7 = impl.filtreDroitOfDemandeSIRH(demandeDtoPrise);
		result8 = impl.filtreDroitOfDemandeSIRH(demandeDtoAnnulee);
		result9 = impl.filtreDroitOfDemandeSIRH(demandeDtoValidee);
		result10 = impl.filtreDroitOfDemandeSIRH(demandeDtoRejetee);
		result11 = impl.filtreDroitOfDemandeSIRH(demandeDtoEnAttente);

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
		assertFalse(result6.isAffichageBoutonImprimer());
		assertFalse(result6.isAffichageBoutonModifier());
		assertFalse(result6.isAffichageBoutonSupprimer());
		assertFalse(result6.isAffichageBoutonDupliquer());
		assertFalse(result6.isAffichageVisa());
		assertFalse(result6.isModifierApprobation());
		assertFalse(result6.isModifierVisa());
		assertNull(result6.getValeurApprobation());
		assertNull(result6.getValeurVisa());
		assertFalse(result6.isModifierValidation());
		assertNull(result6.getValeurValidation());

		assertEquals(RefEtatEnum.PRISE.getCodeEtat(), result7.getIdRefEtat().intValue());
		assertEquals(9005136, result7.getAgentWithServiceDto().getIdAgent().intValue());
		assertFalse(result7.isAffichageApprobation());
		assertFalse(result7.isAffichageBoutonImprimer());
		assertFalse(result7.isAffichageBoutonModifier());
		assertFalse(result7.isAffichageBoutonSupprimer());
		assertFalse(result7.isAffichageBoutonDupliquer());
		assertFalse(result7.isAffichageVisa());
		assertFalse(result7.isModifierApprobation());
		assertFalse(result7.isModifierVisa());
		assertNull(result7.getValeurApprobation());
		assertNull(result7.getValeurVisa());
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
		assertFalse(result9.isAffichageBoutonImprimer());
		assertFalse(result9.isAffichageBoutonModifier());
		assertFalse(result9.isAffichageBoutonSupprimer());
		assertFalse(result9.isAffichageBoutonDupliquer());
		assertFalse(result9.isAffichageVisa());
		assertFalse(result9.isModifierApprobation());
		assertFalse(result9.isModifierVisa());
		assertNull(result9.getValeurApprobation());
		assertNull(result9.getValeurVisa());
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
		assertFalse(result10.isModifierValidation());
		assertNull(result10.getValeurValidation());

		assertEquals(RefEtatEnum.EN_ATTENTE.getCodeEtat(), result11.getIdRefEtat().intValue());
		assertEquals(9005140, result11.getAgentWithServiceDto().getIdAgent().intValue());
		assertFalse(result11.isAffichageApprobation());
		assertFalse(result11.isAffichageBoutonImprimer());
		assertFalse(result11.isAffichageBoutonModifier());
		assertFalse(result11.isAffichageBoutonSupprimer());
		assertFalse(result11.isAffichageBoutonDupliquer());
		assertFalse(result11.isAffichageVisa());
		assertFalse(result11.isModifierApprobation());
		assertFalse(result11.isModifierVisa());
		assertNull(result11.getValeurApprobation());
		assertNull(result11.getValeurVisa());
		assertNull(result11.getValeurValidation());
	}
	
	@Test
	public void isAfficherBoutonAnnuler() {
		
		DemandeDto demandeDto = new DemandeDto();
			demandeDto.setIdRefEtat(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat());
		
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
		assertFalse(result);
		
		demandeDto.setIdRefEtat(RefEtatEnum.EN_ATTENTE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto);
		assertFalse(result);
		
		demandeDto.setIdRefEtat(RefEtatEnum.PRISE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto);
		assertFalse(result);
		
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
	public void filtreDroitOfListeDemandesByDemande_DemandeOfAgent() {

		Integer idAgentConnecte = 9005138;
		AgentWithServiceDto agDto = new AgentWithServiceDto();
			agDto.setIdAgent(9005138);

		DemandeDto demandeDtoProvisoire = new DemandeDto();
			demandeDtoProvisoire.setIdRefEtat(RefEtatEnum.PROVISOIRE.getCodeEtat());
			demandeDtoProvisoire.setAgentWithServiceDto(agDto);
		DemandeDto demandeDtoSaisie = new DemandeDto();
			demandeDtoSaisie.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());
			demandeDtoSaisie.setAgentWithServiceDto(agDto);
		DemandeDto demandeDtoApprouve = new DemandeDto();
			demandeDtoApprouve.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());
			demandeDtoApprouve.setAgentWithServiceDto(agDto);
		DemandeDto demandeDtoRefusee = new DemandeDto();
			demandeDtoRefusee.setIdRefEtat(RefEtatEnum.REFUSEE.getCodeEtat());
			demandeDtoRefusee.setAgentWithServiceDto(agDto);
		DemandeDto demandeDtoVisee_F = new DemandeDto();
			demandeDtoVisee_F.setIdRefEtat(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat());
			demandeDtoVisee_F.setAgentWithServiceDto(agDto);
		DemandeDto demandeDtoVisee_D = new DemandeDto();
			demandeDtoVisee_D.setIdRefEtat(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat());
			demandeDtoVisee_D.setAgentWithServiceDto(agDto);
		DemandeDto demandeDtoPrise = new DemandeDto();
			demandeDtoPrise.setIdRefEtat(RefEtatEnum.PRISE.getCodeEtat());
			demandeDtoPrise.setAgentWithServiceDto(agDto);
		DemandeDto demandeDtoAnnulee = new DemandeDto();
			demandeDtoAnnulee.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
			demandeDtoAnnulee.setAgentWithServiceDto(agDto);
		DemandeDto demandeDtoValidee = new DemandeDto();
			demandeDtoValidee.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());
			demandeDtoValidee.setAgentWithServiceDto(agDto);
		DemandeDto demandeDtoRejete = new DemandeDto();
			demandeDtoRejete.setIdRefEtat(RefEtatEnum.REJETE.getCodeEtat());
			demandeDtoRejete.setAgentWithServiceDto(agDto);
		DemandeDto demandeDtoEnAttente = new DemandeDto();
			demandeDtoEnAttente.setIdRefEtat(RefEtatEnum.EN_ATTENTE.getCodeEtat());
			demandeDtoEnAttente.setAgentWithServiceDto(agDto);

		List<DroitsAgent> listDroitAgent = new ArrayList<DroitsAgent>();
		
		// When
		result1 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoProvisoire, listDroitAgent);
		result2 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoSaisie, listDroitAgent);
		result3 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoApprouve, listDroitAgent);
		result4 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoRefusee, listDroitAgent);
		result5 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoVisee_F, listDroitAgent);
		result6 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoVisee_D, listDroitAgent);
		result7 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoPrise, listDroitAgent);
		result8 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoAnnulee, listDroitAgent);
		result9 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoValidee, listDroitAgent);
		result10 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoRejete, listDroitAgent);
		result11 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoEnAttente, listDroitAgent);

		// Then
		assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), result1.getIdRefEtat().intValue());
		assertFalse(result1.isAffichageApprobation());
		assertTrue(result1.isAffichageBoutonModifier());
		assertTrue(result1.isAffichageBoutonSupprimer());
		assertFalse(result1.isAffichageBoutonDupliquer());
		assertFalse(result1.isAffichageVisa());
		assertFalse(result1.isModifierApprobation());
		assertFalse(result1.isModifierVisa());
		assertNull(result1.getValeurApprobation());
		assertNull(result1.getValeurVisa());

		assertEquals(RefEtatEnum.SAISIE.getCodeEtat(), result2.getIdRefEtat().intValue());
		assertFalse(result2.isAffichageApprobation());
		assertTrue(result2.isAffichageBoutonModifier());
		assertTrue(result2.isAffichageBoutonSupprimer());
		assertFalse(result2.isAffichageBoutonDupliquer());
		assertFalse(result2.isAffichageVisa());
		assertFalse(result2.isModifierApprobation());
		assertFalse(result2.isModifierVisa());
		assertNull(result2.getValeurApprobation());
		assertNull(result2.getValeurVisa());

		assertEquals(RefEtatEnum.APPROUVEE.getCodeEtat(), result3.getIdRefEtat().intValue());
		assertFalse(result3.isAffichageApprobation());
		assertFalse(result3.isAffichageBoutonModifier());
		assertFalse(result3.isAffichageBoutonSupprimer());
		assertFalse(result3.isAffichageBoutonDupliquer());
		assertFalse(result3.isAffichageVisa());
		assertFalse(result3.isModifierApprobation());
		assertFalse(result3.isModifierVisa());
		assertNull(result3.getValeurApprobation());
		assertNull(result3.getValeurVisa());

		assertEquals(RefEtatEnum.REFUSEE.getCodeEtat(), result4.getIdRefEtat().intValue());
		assertFalse(result4.isAffichageApprobation());
		assertFalse(result4.isAffichageBoutonModifier());
		assertFalse(result4.isAffichageBoutonSupprimer());
		assertFalse(result4.isAffichageBoutonDupliquer());
		assertFalse(result4.isAffichageVisa());
		assertFalse(result4.isModifierApprobation());
		assertFalse(result4.isModifierVisa());
		assertNull(result4.getValeurApprobation());
		assertNull(result4.getValeurVisa());

		assertEquals(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat(), result5.getIdRefEtat().intValue());
		assertFalse(result5.isAffichageApprobation());
		assertFalse(result5.isAffichageBoutonModifier());
		assertFalse(result5.isAffichageBoutonSupprimer());
		assertFalse(result5.isAffichageBoutonDupliquer());
		assertFalse(result5.isAffichageVisa());
		assertFalse(result5.isModifierApprobation());
		assertFalse(result5.isModifierVisa());
		assertNull(result5.getValeurApprobation());
		assertNull(result5.getValeurVisa());

		assertEquals(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat(), result6.getIdRefEtat().intValue());
		assertFalse(result6.isAffichageApprobation());
		assertFalse(result6.isAffichageBoutonModifier());
		assertFalse(result6.isAffichageBoutonSupprimer());
		assertFalse(result6.isAffichageBoutonDupliquer());
		assertFalse(result6.isAffichageVisa());
		assertFalse(result6.isModifierApprobation());
		assertFalse(result6.isModifierVisa());
		assertNull(result6.getValeurApprobation());
		assertNull(result6.getValeurVisa());

		assertEquals(RefEtatEnum.PRISE.getCodeEtat(), result7.getIdRefEtat().intValue());
		assertFalse(result7.isAffichageApprobation());
		assertFalse(result7.isAffichageBoutonModifier());
		assertFalse(result7.isAffichageBoutonSupprimer());
		assertFalse(result7.isAffichageBoutonDupliquer());
		assertFalse(result7.isAffichageVisa());
		assertFalse(result7.isModifierApprobation());
		assertFalse(result7.isModifierVisa());
		assertNull(result7.getValeurApprobation());
		assertNull(result7.getValeurVisa());

		assertEquals(RefEtatEnum.ANNULEE.getCodeEtat(), result8.getIdRefEtat().intValue());
		assertFalse(result8.isAffichageApprobation());
		assertFalse(result8.isAffichageBoutonModifier());
		assertFalse(result8.isAffichageBoutonSupprimer());
		assertFalse(result8.isAffichageBoutonDupliquer());
		assertFalse(result8.isAffichageVisa());
		assertFalse(result8.isModifierApprobation());
		assertFalse(result8.isModifierVisa());
		assertNull(result8.getValeurApprobation());
		assertNull(result8.getValeurVisa());
		
		assertEquals(RefEtatEnum.VALIDEE.getCodeEtat(), result9.getIdRefEtat().intValue());
		assertFalse(result9.isAffichageApprobation());
		assertFalse(result9.isAffichageBoutonModifier());
		assertFalse(result9.isAffichageBoutonSupprimer());
		assertFalse(result9.isAffichageBoutonDupliquer());
		assertFalse(result9.isAffichageVisa());
		assertFalse(result9.isModifierApprobation());
		assertFalse(result9.isModifierVisa());
		assertNull(result9.getValeurApprobation());
		assertNull(result9.getValeurVisa());
		
		assertEquals(RefEtatEnum.REJETE.getCodeEtat(), result10.getIdRefEtat().intValue());
		assertFalse(result10.isAffichageApprobation());
		assertFalse(result10.isAffichageBoutonModifier());
		assertFalse(result10.isAffichageBoutonSupprimer());
		assertFalse(result10.isAffichageBoutonDupliquer());
		assertFalse(result10.isAffichageVisa());
		assertFalse(result10.isModifierApprobation());
		assertFalse(result10.isModifierVisa());
		assertNull(result10.getValeurApprobation());
		assertNull(result10.getValeurVisa());
		
		assertEquals(RefEtatEnum.EN_ATTENTE.getCodeEtat(), result11.getIdRefEtat().intValue());
		assertFalse(result11.isAffichageApprobation());
		assertFalse(result11.isAffichageBoutonModifier());
		assertFalse(result11.isAffichageBoutonSupprimer());
		assertFalse(result11.isAffichageBoutonDupliquer());
		assertFalse(result11.isAffichageVisa());
		assertFalse(result11.isModifierApprobation());
		assertFalse(result11.isModifierVisa());
		assertNull(result11.getValeurApprobation());
		assertNull(result11.getValeurVisa());
	}

	@Test
	public void filtreDroitOfListeDemandesByDemande_Operateur() {

		Integer idAgentConnecte = 9005129;
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
		DemandeDto demandeDtoRejete = new DemandeDto();
			demandeDtoRejete.setIdRefEtat(RefEtatEnum.REJETE.getCodeEtat());
			demandeDtoRejete.setAgentWithServiceDto(agDto10);
		DemandeDto demandeDtoEnAttente = new DemandeDto();
			demandeDtoEnAttente.setIdRefEtat(RefEtatEnum.EN_ATTENTE.getCodeEtat());
			demandeDtoEnAttente.setAgentWithServiceDto(agDto11);

		// les droits
		Profil profil = new Profil();
			profil.setLibelle(ProfilEnum.OPERATEUR.toString());

		DroitProfil droitProfil = new DroitProfil();
			droitProfil.setProfil(profil);

		DroitDroitsAgent dda = new DroitDroitsAgent();
			dda.setDroitProfil(droitProfil);

		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
			droitDroitsAgent.add(dda);

		DroitsAgent da = new DroitsAgent();
			da.setIdAgent(9005130);
			da.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da1 = new DroitsAgent();
			da1.setIdAgent(9005131);
			da1.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da2 = new DroitsAgent();
			da2.setIdAgent(9005132);
			da2.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da3 = new DroitsAgent();
			da3.setIdAgent(9005133);
			da3.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da4 = new DroitsAgent();
			da4.setIdAgent(9005134);
			da4.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da5 = new DroitsAgent();
			da5.setIdAgent(9005135);
			da5.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da6 = new DroitsAgent();
			da6.setIdAgent(9005136);
			da6.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da7 = new DroitsAgent();
			da7.setIdAgent(9005137);
			da7.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da8 = new DroitsAgent();
			da8.setIdAgent(9005138);
			da8.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da9 = new DroitsAgent();
			da9.setIdAgent(9005139);
			da9.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da10 = new DroitsAgent();
			da10.setIdAgent(9005140);
			da10.setDroitDroitsAgent(droitDroitsAgent);

		List<DroitsAgent> listDroitAgent = new ArrayList<DroitsAgent>();
			listDroitAgent.addAll(Arrays.asList(da, da1, da2, da3, da4, da5, da6, da7, da8, da9, da10));

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.getListOfAgentsToInputOrApprove(idAgentConnecte, null)).thenReturn(
				listDroitAgent);

		ReflectionTestUtils.setField(impl, "accessRightsRepository", accessRightsRepository);

		// When
		result1 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoProvisoire, listDroitAgent);
		result2 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoSaisie, listDroitAgent);
		result3 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoApprouve, listDroitAgent);
		result4 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoRefusee, listDroitAgent);
		result5 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoVisee_F, listDroitAgent);
		result6 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoVisee_D, listDroitAgent);
		result7 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoPrise, listDroitAgent);
		result8 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoAnnulee, listDroitAgent);
		result9 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoValidee, listDroitAgent);
		result10 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoRejete, listDroitAgent);
		result11 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoEnAttente, listDroitAgent);

		// Then
		assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), result1.getIdRefEtat().intValue());
		assertEquals(9005130, result1.getAgentWithServiceDto().getIdAgent().intValue());
		assertFalse(result1.isAffichageApprobation());
		assertTrue(result1.isAffichageBoutonModifier());
		assertTrue(result1.isAffichageBoutonSupprimer());
		assertFalse(result1.isAffichageBoutonDupliquer());
		assertFalse(result1.isAffichageVisa());
		assertFalse(result1.isModifierApprobation());
		assertFalse(result1.isModifierVisa());
		assertNull(result1.getValeurApprobation());
		assertNull(result1.getValeurVisa());

		assertEquals(RefEtatEnum.SAISIE.getCodeEtat(), result2.getIdRefEtat().intValue());
		assertEquals(9005131, result2.getAgentWithServiceDto().getIdAgent().intValue());
		assertFalse(result2.isAffichageApprobation());
		assertTrue(result2.isAffichageBoutonModifier());
		assertTrue(result2.isAffichageBoutonSupprimer());
		assertFalse(result2.isAffichageBoutonDupliquer());
		assertFalse(result2.isAffichageVisa());
		assertFalse(result2.isModifierApprobation());
		assertFalse(result2.isModifierVisa());
		assertNull(result2.getValeurApprobation());
		assertNull(result2.getValeurVisa());

		assertEquals(RefEtatEnum.APPROUVEE.getCodeEtat(), result3.getIdRefEtat().intValue());
		assertEquals(9005132, result3.getAgentWithServiceDto().getIdAgent().intValue());
		assertFalse(result3.isAffichageApprobation());
		assertFalse(result3.isAffichageBoutonModifier());
		assertFalse(result3.isAffichageBoutonSupprimer());
		assertFalse(result3.isAffichageBoutonDupliquer());
		assertFalse(result3.isAffichageVisa());
		assertFalse(result3.isModifierApprobation());
		assertFalse(result3.isModifierVisa());
		assertNull(result3.getValeurApprobation());
		assertNull(result3.getValeurVisa());

		assertEquals(RefEtatEnum.REFUSEE.getCodeEtat(), result4.getIdRefEtat().intValue());
		assertEquals(9005133, result4.getAgentWithServiceDto().getIdAgent().intValue());
		assertFalse(result4.isAffichageApprobation());
		assertFalse(result4.isAffichageBoutonModifier());
		assertFalse(result4.isAffichageBoutonSupprimer());
		assertFalse(result4.isAffichageBoutonDupliquer());
		assertFalse(result4.isAffichageVisa());
		assertFalse(result4.isModifierApprobation());
		assertFalse(result4.isModifierVisa());
		assertNull(result4.getValeurApprobation());
		assertNull(result4.getValeurVisa());

		assertEquals(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat(), result5.getIdRefEtat().intValue());
		assertEquals(9005134, result5.getAgentWithServiceDto().getIdAgent().intValue());
		assertFalse(result5.isAffichageApprobation());
		assertFalse(result5.isAffichageBoutonModifier());
		assertFalse(result5.isAffichageBoutonSupprimer());
		assertFalse(result5.isAffichageBoutonDupliquer());
		assertFalse(result5.isAffichageVisa());
		assertFalse(result5.isModifierApprobation());
		assertFalse(result5.isModifierVisa());
		assertNull(result5.getValeurApprobation());
		assertNull(result5.getValeurVisa());

		assertEquals(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat(), result6.getIdRefEtat().intValue());
		assertEquals(9005135, result6.getAgentWithServiceDto().getIdAgent().intValue());
		assertFalse(result6.isAffichageApprobation());
		assertFalse(result6.isAffichageBoutonModifier());
		assertFalse(result6.isAffichageBoutonSupprimer());
		assertFalse(result6.isAffichageBoutonDupliquer());
		assertFalse(result6.isAffichageVisa());
		assertFalse(result6.isModifierApprobation());
		assertFalse(result6.isModifierVisa());
		assertNull(result6.getValeurApprobation());
		assertNull(result6.getValeurVisa());

		assertEquals(RefEtatEnum.PRISE.getCodeEtat(), result7.getIdRefEtat().intValue());
		assertEquals(9005136, result7.getAgentWithServiceDto().getIdAgent().intValue());
		assertFalse(result7.isAffichageApprobation());
		assertFalse(result7.isAffichageBoutonModifier());
		assertFalse(result7.isAffichageBoutonSupprimer());
		assertFalse(result7.isAffichageBoutonDupliquer());
		assertFalse(result7.isAffichageVisa());
		assertFalse(result7.isModifierApprobation());
		assertFalse(result7.isModifierVisa());
		assertNull(result7.getValeurApprobation());
		assertNull(result7.getValeurVisa());

		assertEquals(RefEtatEnum.ANNULEE.getCodeEtat(), result8.getIdRefEtat().intValue());
		assertEquals(9005137, result8.getAgentWithServiceDto().getIdAgent().intValue());
		assertFalse(result8.isAffichageApprobation());
		assertFalse(result8.isAffichageBoutonModifier());
		assertFalse(result8.isAffichageBoutonSupprimer());
		assertTrue(result8.isAffichageBoutonDupliquer());
		assertFalse(result8.isAffichageVisa());
		assertFalse(result8.isModifierApprobation());
		assertFalse(result8.isModifierVisa());
		assertNull(result8.getValeurApprobation());
		assertNull(result8.getValeurVisa());
		
		assertEquals(RefEtatEnum.VALIDEE.getCodeEtat(), result9.getIdRefEtat().intValue());
		assertFalse(result9.isAffichageApprobation());
		assertFalse(result9.isAffichageBoutonModifier());
		assertFalse(result9.isAffichageBoutonSupprimer());
		assertFalse(result9.isAffichageBoutonDupliquer());
		assertFalse(result9.isAffichageVisa());
		assertFalse(result9.isModifierApprobation());
		assertFalse(result9.isModifierVisa());
		assertNull(result9.getValeurApprobation());
		assertNull(result9.getValeurVisa());
		
		assertEquals(RefEtatEnum.REJETE.getCodeEtat(), result10.getIdRefEtat().intValue());
		assertFalse(result10.isAffichageApprobation());
		assertFalse(result10.isAffichageBoutonModifier());
		assertFalse(result10.isAffichageBoutonSupprimer());
		assertFalse(result10.isAffichageBoutonDupliquer());
		assertFalse(result10.isAffichageVisa());
		assertFalse(result10.isModifierApprobation());
		assertFalse(result10.isModifierVisa());
		assertNull(result10.getValeurApprobation());
		assertNull(result10.getValeurVisa());
		
		assertEquals(RefEtatEnum.EN_ATTENTE.getCodeEtat(), result11.getIdRefEtat().intValue());
		assertFalse(result11.isAffichageApprobation());
		assertFalse(result11.isAffichageBoutonModifier());
		assertFalse(result11.isAffichageBoutonSupprimer());
		assertFalse(result11.isAffichageBoutonDupliquer());
		assertFalse(result11.isAffichageVisa());
		assertFalse(result11.isModifierApprobation());
		assertFalse(result11.isModifierVisa());
		assertNull(result11.getValeurApprobation());
		assertNull(result11.getValeurVisa());
	}

	@Test
	public void filtreDroitOfListeDemandesByDemande_Viseur() {

		Integer idAgentConnecte = 9005129;
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
		DemandeDto demandeDtoRejete = new DemandeDto();
			demandeDtoRejete.setIdRefEtat(RefEtatEnum.REJETE.getCodeEtat());
			demandeDtoRejete.setAgentWithServiceDto(agDto10);
		DemandeDto demandeDtoEnAttente = new DemandeDto();
			demandeDtoEnAttente.setIdRefEtat(RefEtatEnum.EN_ATTENTE.getCodeEtat());
			demandeDtoEnAttente.setAgentWithServiceDto(agDto11);

		// les droits
		Profil profil = new Profil();
			profil.setLibelle(ProfilEnum.VISEUR.toString());

		DroitProfil droitProfil = new DroitProfil();
			droitProfil.setProfil(profil);

		DroitDroitsAgent dda = new DroitDroitsAgent();
			dda.setDroitProfil(droitProfil);

		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
			droitDroitsAgent.add(dda);

		DroitsAgent da = new DroitsAgent();
			da.setIdAgent(9005130);
			da.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da1 = new DroitsAgent();
			da1.setIdAgent(9005131);
			da1.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da2 = new DroitsAgent();
			da2.setIdAgent(9005132);
			da2.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da3 = new DroitsAgent();
			da3.setIdAgent(9005133);
			da3.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da4 = new DroitsAgent();
			da4.setIdAgent(9005134);
			da4.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da5 = new DroitsAgent();
			da5.setIdAgent(9005135);
			da5.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da6 = new DroitsAgent();
			da6.setIdAgent(9005136);
			da6.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da7 = new DroitsAgent();
			da7.setIdAgent(9005137);
			da7.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da8 = new DroitsAgent();
			da8.setIdAgent(9005138);
			da8.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da9 = new DroitsAgent();
			da9.setIdAgent(9005139);
			da9.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da10 = new DroitsAgent();
			da10.setIdAgent(9005140);
			da10.setDroitDroitsAgent(droitDroitsAgent);

		List<DroitsAgent> listDroitAgent = new ArrayList<DroitsAgent>();
		listDroitAgent.addAll(Arrays.asList(da, da1, da2, da3, da4, da5, da6, da7, da8, da9, da10));

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.getListOfAgentsToInputOrApprove(idAgentConnecte, null)).thenReturn(
				listDroitAgent);

		ReflectionTestUtils.setField(impl, "accessRightsRepository", accessRightsRepository);

		// When
		DemandeDto result1 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoProvisoire, listDroitAgent);
		DemandeDto result2 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoSaisie, listDroitAgent);
		DemandeDto result3 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoApprouve, listDroitAgent);
		DemandeDto result4 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoRefusee, listDroitAgent);
		DemandeDto result5 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoVisee_F, listDroitAgent);
		DemandeDto result6 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoVisee_D, listDroitAgent);
		DemandeDto result7 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoPrise, listDroitAgent);
		DemandeDto result8 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoAnnulee, listDroitAgent);
		DemandeDto result9 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoValidee, listDroitAgent);
		DemandeDto result10 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoRejete, listDroitAgent);
		DemandeDto result11 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoEnAttente, listDroitAgent);

		// Then
		assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), result1.getIdRefEtat().intValue());
		assertEquals(9005130, result1.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result1.isAffichageApprobation());
		assertFalse(result1.isAffichageBoutonAnnuler());
		assertFalse(result1.isAffichageBoutonImprimer());
		assertFalse(result1.isAffichageBoutonModifier());
		assertFalse(result1.isAffichageBoutonSupprimer());
		assertFalse(result1.isAffichageBoutonDupliquer());
		assertTrue(result1.isAffichageVisa());
		assertFalse(result1.isModifierApprobation());
		assertFalse(result1.isModifierVisa());
		assertNull(result1.getValeurApprobation());
		assertNull(result1.getValeurVisa());

		assertEquals(RefEtatEnum.SAISIE.getCodeEtat(), result2.getIdRefEtat().intValue());
		assertEquals(9005131, result2.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result2.isAffichageApprobation());
		assertFalse(result2.isAffichageBoutonAnnuler());
		assertFalse(result2.isAffichageBoutonImprimer());
		assertFalse(result2.isAffichageBoutonModifier());
		assertFalse(result2.isAffichageBoutonSupprimer());
		assertFalse(result2.isAffichageBoutonDupliquer());
		assertTrue(result2.isAffichageVisa());
		assertFalse(result2.isModifierApprobation());
		assertTrue(result2.isModifierVisa());
		assertNull(result2.getValeurApprobation());
		assertNull(result2.getValeurVisa());

		assertEquals(RefEtatEnum.APPROUVEE.getCodeEtat(), result3.getIdRefEtat().intValue());
		assertEquals(9005132, result3.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result3.isAffichageApprobation());
		assertFalse(result3.isAffichageBoutonAnnuler());
		assertFalse(result3.isAffichageBoutonImprimer());
		assertFalse(result3.isAffichageBoutonModifier());
		assertFalse(result3.isAffichageBoutonSupprimer());
		assertFalse(result3.isAffichageBoutonDupliquer());
		assertTrue(result3.isAffichageVisa());
		assertFalse(result3.isModifierApprobation());
		assertFalse(result3.isModifierVisa());
		assertNull(result3.getValeurApprobation());
		assertNull(result3.getValeurVisa());

		assertEquals(RefEtatEnum.REFUSEE.getCodeEtat(), result4.getIdRefEtat().intValue());
		assertEquals(9005133, result4.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result4.isAffichageApprobation());
		assertFalse(result4.isAffichageBoutonAnnuler());
		assertFalse(result4.isAffichageBoutonImprimer());
		assertFalse(result4.isAffichageBoutonModifier());
		assertFalse(result4.isAffichageBoutonSupprimer());
		assertFalse(result4.isAffichageBoutonDupliquer());
		assertTrue(result4.isAffichageVisa());
		assertFalse(result4.isModifierApprobation());
		assertFalse(result4.isModifierVisa());
		assertNull(result4.getValeurApprobation());
		assertNull(result4.getValeurVisa());

		assertEquals(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat(), result5.getIdRefEtat().intValue());
		assertEquals(9005134, result5.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result5.isAffichageApprobation());
		assertFalse(result5.isAffichageBoutonAnnuler());
		assertFalse(result5.isAffichageBoutonImprimer());
		assertFalse(result5.isAffichageBoutonModifier());
		assertFalse(result5.isAffichageBoutonSupprimer());
		assertFalse(result5.isAffichageBoutonDupliquer());
		assertTrue(result5.isAffichageVisa());
		assertFalse(result5.isModifierApprobation());
		assertTrue(result5.isModifierVisa());
		assertNull(result5.getValeurApprobation());
		assertNull(result5.getValeurVisa());

		assertEquals(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat(), result6.getIdRefEtat().intValue());
		assertEquals(9005135, result6.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result6.isAffichageApprobation());
		assertFalse(result6.isAffichageBoutonAnnuler());
		assertFalse(result6.isAffichageBoutonImprimer());
		assertFalse(result6.isAffichageBoutonModifier());
		assertFalse(result6.isAffichageBoutonSupprimer());
		assertFalse(result6.isAffichageBoutonDupliquer());
		assertTrue(result6.isAffichageVisa());
		assertFalse(result6.isModifierApprobation());
		assertTrue(result6.isModifierVisa());
		assertNull(result6.getValeurApprobation());
		assertNull(result6.getValeurVisa());

		assertEquals(RefEtatEnum.PRISE.getCodeEtat(), result7.getIdRefEtat().intValue());
		assertEquals(9005136, result7.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result7.isAffichageApprobation());
		assertFalse(result7.isAffichageBoutonAnnuler());
		assertFalse(result7.isAffichageBoutonImprimer());
		assertFalse(result7.isAffichageBoutonModifier());
		assertFalse(result7.isAffichageBoutonSupprimer());
		assertFalse(result7.isAffichageBoutonDupliquer());
		assertTrue(result7.isAffichageVisa());
		assertFalse(result7.isModifierApprobation());
		assertFalse(result7.isModifierVisa());
		assertNull(result7.getValeurApprobation());
		assertNull(result7.getValeurVisa());

		assertEquals(RefEtatEnum.ANNULEE.getCodeEtat(), result8.getIdRefEtat().intValue());
		assertEquals(9005137, result8.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result8.isAffichageApprobation());
		assertFalse(result8.isAffichageBoutonAnnuler());
		assertFalse(result8.isAffichageBoutonImprimer());
		assertFalse(result8.isAffichageBoutonModifier());
		assertFalse(result8.isAffichageBoutonSupprimer());
		assertFalse(result8.isAffichageBoutonDupliquer());
		assertTrue(result8.isAffichageVisa());
		assertFalse(result8.isModifierApprobation());
		assertFalse(result8.isModifierVisa());
		assertNull(result8.getValeurApprobation());
		assertNull(result8.getValeurVisa());

		assertEquals(RefEtatEnum.VALIDEE.getCodeEtat(), result9.getIdRefEtat().intValue());
		assertEquals(9005138, result9.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result9.isAffichageApprobation());
		assertFalse(result9.isAffichageBoutonAnnuler());
		assertFalse(result9.isAffichageBoutonImprimer());
		assertFalse(result9.isAffichageBoutonModifier());
		assertFalse(result9.isAffichageBoutonSupprimer());
		assertFalse(result9.isAffichageBoutonDupliquer());
		assertTrue(result9.isAffichageVisa());
		assertFalse(result9.isModifierApprobation());
		assertFalse(result9.isModifierVisa());
		assertNull(result9.getValeurApprobation());
		assertNull(result9.getValeurVisa());

		assertEquals(RefEtatEnum.REJETE.getCodeEtat(), result10.getIdRefEtat().intValue());
		assertEquals(9005139, result10.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result10.isAffichageApprobation());
		assertFalse(result10.isAffichageBoutonAnnuler());
		assertFalse(result10.isAffichageBoutonImprimer());
		assertFalse(result10.isAffichageBoutonModifier());
		assertFalse(result10.isAffichageBoutonSupprimer());
		assertFalse(result10.isAffichageBoutonDupliquer());
		assertTrue(result10.isAffichageVisa());
		assertFalse(result10.isModifierApprobation());
		assertFalse(result10.isModifierVisa());
		assertNull(result10.getValeurApprobation());
		assertNull(result10.getValeurVisa());

		assertEquals(RefEtatEnum.EN_ATTENTE.getCodeEtat(), result11.getIdRefEtat().intValue());
		assertEquals(9005140, result11.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result11.isAffichageApprobation());
		assertFalse(result11.isAffichageBoutonAnnuler());
		assertFalse(result11.isAffichageBoutonImprimer());
		assertFalse(result11.isAffichageBoutonModifier());
		assertFalse(result11.isAffichageBoutonSupprimer());
		assertFalse(result11.isAffichageBoutonDupliquer());
		assertTrue(result11.isAffichageVisa());
		assertFalse(result11.isModifierApprobation());
		assertFalse(result11.isModifierVisa());
		assertNull(result11.getValeurApprobation());
		assertNull(result11.getValeurVisa());
	}

	@Test
	public void filtreDroitOfListeDemandesByDemande_Approbateur() {

		Integer idAgentConnecte = 9005129;
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
		DemandeDto demandeDtoRejete = new DemandeDto();
			demandeDtoRejete.setIdRefEtat(RefEtatEnum.REJETE.getCodeEtat());
			demandeDtoRejete.setAgentWithServiceDto(agDto10);
		DemandeDto demandeDtoEnAttente = new DemandeDto();
			demandeDtoEnAttente.setIdRefEtat(RefEtatEnum.EN_ATTENTE.getCodeEtat());
			demandeDtoEnAttente.setAgentWithServiceDto(agDto11);

		// les droits
		Profil profil = new Profil();
			profil.setLibelle(ProfilEnum.APPROBATEUR.toString());

		DroitProfil droitProfil = new DroitProfil();
			droitProfil.setProfil(profil);

		DroitDroitsAgent dda = new DroitDroitsAgent();
			dda.setDroitProfil(droitProfil);

		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
			droitDroitsAgent.add(dda);

		DroitsAgent da = new DroitsAgent();
			da.setIdAgent(9005130);
			da.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da1 = new DroitsAgent();
			da1.setIdAgent(9005131);
			da1.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da2 = new DroitsAgent();
			da2.setIdAgent(9005132);
			da2.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da3 = new DroitsAgent();
			da3.setIdAgent(9005133);
			da3.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da4 = new DroitsAgent();
			da4.setIdAgent(9005134);
			da4.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da5 = new DroitsAgent();
			da5.setIdAgent(9005135);
			da5.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da6 = new DroitsAgent();
			da6.setIdAgent(9005136);
			da6.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da7 = new DroitsAgent();
			da7.setIdAgent(9005137);
			da7.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da8 = new DroitsAgent();
			da8.setIdAgent(9005138);
			da8.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da9 = new DroitsAgent();
			da9.setIdAgent(9005139);
			da9.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da10 = new DroitsAgent();
			da10.setIdAgent(9005140);
			da10.setDroitDroitsAgent(droitDroitsAgent);

		List<DroitsAgent> listDroitAgent = new ArrayList<DroitsAgent>();
			listDroitAgent.addAll(Arrays.asList(da, da1, da2, da3, da4, da5, da6, da7, da8, da9, da10));

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.getListOfAgentsToInputOrApprove(idAgentConnecte, null)).thenReturn(
				listDroitAgent);

		ReflectionTestUtils.setField(impl, "accessRightsRepository", accessRightsRepository);

		// When
		DemandeDto result1 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoProvisoire, listDroitAgent);
		DemandeDto result2 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoSaisie, listDroitAgent);
		DemandeDto result3 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoApprouve, listDroitAgent);
		DemandeDto result4 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoRefusee, listDroitAgent);
		DemandeDto result5 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoVisee_F, listDroitAgent);
		DemandeDto result6 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoVisee_D, listDroitAgent);
		DemandeDto result7 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoPrise, listDroitAgent);
		DemandeDto result8 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoAnnulee, listDroitAgent);
		DemandeDto result9 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoValidee, listDroitAgent);
		DemandeDto result10 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoRejete, listDroitAgent);
		DemandeDto result11 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoEnAttente, listDroitAgent);

		// Then
		assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), result1.getIdRefEtat().intValue());
		assertEquals(9005130, result1.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result1.isAffichageApprobation());
		assertFalse(result1.isAffichageBoutonAnnuler());
		assertFalse(result1.isAffichageBoutonImprimer());
		assertFalse(result1.isAffichageBoutonModifier());
		assertFalse(result1.isAffichageBoutonSupprimer());
		assertFalse(result1.isAffichageBoutonDupliquer());
		assertTrue(result1.isAffichageVisa());
		assertFalse(result1.isModifierApprobation());
		assertFalse(result1.isModifierVisa());
		assertNull(result1.getValeurApprobation());
		assertNull(result1.getValeurVisa());

		assertEquals(RefEtatEnum.SAISIE.getCodeEtat(), result2.getIdRefEtat().intValue());
		assertEquals(9005131, result2.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result2.isAffichageApprobation());
		assertFalse(result2.isAffichageBoutonAnnuler());
		assertFalse(result2.isAffichageBoutonImprimer());
		assertFalse(result2.isAffichageBoutonModifier());
		assertFalse(result2.isAffichageBoutonSupprimer());
		assertFalse(result2.isAffichageBoutonDupliquer());
		assertTrue(result2.isAffichageVisa());
		assertTrue(result2.isModifierApprobation());
		assertFalse(result2.isModifierVisa());
		assertNull(result2.getValeurApprobation());
		assertNull(result2.getValeurVisa());

		assertEquals(RefEtatEnum.APPROUVEE.getCodeEtat(), result3.getIdRefEtat().intValue());
		assertEquals(9005132, result3.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result3.isAffichageApprobation());
		assertFalse(result3.isAffichageBoutonAnnuler());
		assertFalse(result3.isAffichageBoutonImprimer());
		assertFalse(result3.isAffichageBoutonModifier());
		assertFalse(result3.isAffichageBoutonSupprimer());
		assertFalse(result3.isAffichageBoutonDupliquer());
		assertTrue(result3.isAffichageVisa());
		assertTrue(result3.isModifierApprobation());
		assertFalse(result3.isModifierVisa());
		assertNull(result3.getValeurApprobation());
		assertNull(result3.getValeurVisa());

		assertEquals(RefEtatEnum.REFUSEE.getCodeEtat(), result4.getIdRefEtat().intValue());
		assertEquals(9005133, result4.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result4.isAffichageApprobation());
		assertFalse(result4.isAffichageBoutonAnnuler());
		assertFalse(result4.isAffichageBoutonImprimer());
		assertFalse(result4.isAffichageBoutonModifier());
		assertFalse(result4.isAffichageBoutonSupprimer());
		assertFalse(result4.isAffichageBoutonDupliquer());
		assertTrue(result4.isAffichageVisa());
		assertTrue(result4.isModifierApprobation());
		assertFalse(result4.isModifierVisa());
		assertNull(result4.getValeurApprobation());
		assertNull(result4.getValeurVisa());

		assertEquals(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat(), result5.getIdRefEtat().intValue());
		assertEquals(9005134, result5.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result5.isAffichageApprobation());
		assertFalse(result5.isAffichageBoutonAnnuler());
		assertFalse(result5.isAffichageBoutonImprimer());
		assertFalse(result5.isAffichageBoutonModifier());
		assertFalse(result5.isAffichageBoutonSupprimer());
		assertFalse(result5.isAffichageBoutonDupliquer());
		assertTrue(result5.isAffichageVisa());
		assertTrue(result5.isModifierApprobation());
		assertFalse(result5.isModifierVisa());
		assertNull(result5.getValeurApprobation());
		assertNull(result5.getValeurVisa());

		assertEquals(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat(), result6.getIdRefEtat().intValue());
		assertEquals(9005135, result6.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result6.isAffichageApprobation());
		assertFalse(result6.isAffichageBoutonAnnuler());
		assertFalse(result6.isAffichageBoutonImprimer());
		assertFalse(result6.isAffichageBoutonModifier());
		assertFalse(result6.isAffichageBoutonSupprimer());
		assertFalse(result6.isAffichageBoutonDupliquer());
		assertTrue(result6.isAffichageVisa());
		assertTrue(result6.isModifierApprobation());
		assertFalse(result6.isModifierVisa());
		assertNull(result6.getValeurApprobation());
		assertNull(result6.getValeurVisa());

		assertEquals(RefEtatEnum.PRISE.getCodeEtat(), result7.getIdRefEtat().intValue());
		assertEquals(9005136, result7.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result7.isAffichageApprobation());
		assertFalse(result7.isAffichageBoutonAnnuler());
		assertFalse(result7.isAffichageBoutonImprimer());
		assertFalse(result7.isAffichageBoutonModifier());
		assertFalse(result7.isAffichageBoutonSupprimer());
		assertFalse(result7.isAffichageBoutonDupliquer());
		assertTrue(result7.isAffichageVisa());
		assertFalse(result7.isModifierApprobation());
		assertFalse(result7.isModifierVisa());
		assertNull(result7.getValeurApprobation());
		assertNull(result7.getValeurVisa());

		assertEquals(RefEtatEnum.ANNULEE.getCodeEtat(), result8.getIdRefEtat().intValue());
		assertEquals(9005137, result8.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result8.isAffichageApprobation());
		assertFalse(result8.isAffichageBoutonAnnuler());
		assertFalse(result8.isAffichageBoutonImprimer());
		assertFalse(result8.isAffichageBoutonModifier());
		assertFalse(result8.isAffichageBoutonSupprimer());
		assertFalse(result8.isAffichageBoutonDupliquer());
		assertTrue(result8.isAffichageVisa());
		assertFalse(result8.isModifierApprobation());
		assertFalse(result8.isModifierVisa());
		assertNull(result8.getValeurApprobation());
		assertNull(result8.getValeurVisa());

		assertEquals(RefEtatEnum.VALIDEE.getCodeEtat(), result9.getIdRefEtat().intValue());
		assertEquals(9005138, result9.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result9.isAffichageApprobation());
		assertFalse(result9.isAffichageBoutonAnnuler());
		assertFalse(result9.isAffichageBoutonImprimer());
		assertFalse(result9.isAffichageBoutonModifier());
		assertFalse(result9.isAffichageBoutonSupprimer());
		assertFalse(result9.isAffichageBoutonDupliquer());
		assertTrue(result9.isAffichageVisa());
		assertFalse(result9.isModifierApprobation());
		assertFalse(result9.isModifierVisa());
		assertNull(result9.getValeurApprobation());
		assertNull(result9.getValeurVisa());

		assertEquals(RefEtatEnum.REJETE.getCodeEtat(), result10.getIdRefEtat().intValue());
		assertEquals(9005139, result10.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result10.isAffichageApprobation());
		assertFalse(result10.isAffichageBoutonAnnuler());
		assertFalse(result10.isAffichageBoutonImprimer());
		assertFalse(result10.isAffichageBoutonModifier());
		assertFalse(result10.isAffichageBoutonSupprimer());
		assertFalse(result10.isAffichageBoutonDupliquer());
		assertTrue(result10.isAffichageVisa());
		assertFalse(result10.isModifierApprobation());
		assertFalse(result10.isModifierVisa());
		assertNull(result10.getValeurApprobation());
		assertNull(result10.getValeurVisa());

		assertEquals(RefEtatEnum.EN_ATTENTE.getCodeEtat(), result11.getIdRefEtat().intValue());
		assertEquals(9005140, result11.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result11.isAffichageApprobation());
		assertFalse(result11.isAffichageBoutonAnnuler());
		assertFalse(result11.isAffichageBoutonImprimer());
		assertFalse(result11.isAffichageBoutonModifier());
		assertFalse(result11.isAffichageBoutonSupprimer());
		assertFalse(result11.isAffichageBoutonDupliquer());
		assertTrue(result11.isAffichageVisa());
		assertFalse(result11.isModifierApprobation());
		assertFalse(result11.isModifierVisa());
		assertNull(result11.getValeurApprobation());
		assertNull(result11.getValeurVisa());
	}

	@Test
	public void filtreDroitOfListeDemandesByDemande_Delegataire() {

		Integer idAgentConnecte = 9005129;
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
		DemandeDto demandeDtoRejete = new DemandeDto();
			demandeDtoRejete.setIdRefEtat(RefEtatEnum.REJETE.getCodeEtat());
			demandeDtoRejete.setAgentWithServiceDto(agDto10);
		DemandeDto demandeDtoEnAttente = new DemandeDto();
			demandeDtoEnAttente.setIdRefEtat(RefEtatEnum.EN_ATTENTE.getCodeEtat());
			demandeDtoEnAttente.setAgentWithServiceDto(agDto11);

		// les droits
		Profil profil = new Profil();
			profil.setLibelle(ProfilEnum.DELEGATAIRE.toString());

		DroitProfil droitProfil = new DroitProfil();
			droitProfil.setProfil(profil);

		DroitDroitsAgent dda = new DroitDroitsAgent();
			dda.setDroitProfil(droitProfil);

		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
			droitDroitsAgent.add(dda);

		DroitsAgent da = new DroitsAgent();
			da.setIdAgent(9005130);
			da.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da1 = new DroitsAgent();
			da1.setIdAgent(9005131);
			da1.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da2 = new DroitsAgent();
			da2.setIdAgent(9005132);
			da2.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da3 = new DroitsAgent();
			da3.setIdAgent(9005133);
			da3.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da4 = new DroitsAgent();
			da4.setIdAgent(9005134);
			da4.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da5 = new DroitsAgent();
			da5.setIdAgent(9005135);
			da5.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da6 = new DroitsAgent();
			da6.setIdAgent(9005136);
			da6.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da7 = new DroitsAgent();
			da7.setIdAgent(9005137);
			da7.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da8 = new DroitsAgent();
			da8.setIdAgent(9005138);
			da8.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da9 = new DroitsAgent();
			da9.setIdAgent(9005139);
			da9.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da10 = new DroitsAgent();
			da10.setIdAgent(9005140);
			da10.setDroitDroitsAgent(droitDroitsAgent);

		List<DroitsAgent> listDroitAgent = new ArrayList<DroitsAgent>();
			listDroitAgent.addAll(Arrays.asList(da, da1, da2, da3, da4, da5, da6, da7, da8, da9, da10));

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.getListOfAgentsToInputOrApprove(idAgentConnecte, null)).thenReturn(
				listDroitAgent);

		ReflectionTestUtils.setField(impl, "accessRightsRepository", accessRightsRepository);

		// When
		DemandeDto result1 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoProvisoire, listDroitAgent);
		DemandeDto result2 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoSaisie, listDroitAgent);
		DemandeDto result3 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoApprouve, listDroitAgent);
		DemandeDto result4 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoRefusee, listDroitAgent);
		DemandeDto result5 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoVisee_F, listDroitAgent);
		DemandeDto result6 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoVisee_D, listDroitAgent);
		DemandeDto result7 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoPrise, listDroitAgent);
		DemandeDto result8 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoAnnulee, listDroitAgent);
		DemandeDto result9 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoValidee, listDroitAgent);
		DemandeDto result10 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoRejete, listDroitAgent);
		DemandeDto result11 = impl.filtreDroitOfDemande(idAgentConnecte, demandeDtoEnAttente, listDroitAgent);

		// Then
		assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), result1.getIdRefEtat().intValue());
		assertEquals(9005130, result1.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result1.isAffichageApprobation());
		assertFalse(result1.isAffichageBoutonAnnuler());
		assertFalse(result1.isAffichageBoutonImprimer());
		assertFalse(result1.isAffichageBoutonModifier());
		assertFalse(result1.isAffichageBoutonSupprimer());
		assertFalse(result1.isAffichageBoutonDupliquer());
		assertTrue(result1.isAffichageVisa());
		assertFalse(result1.isModifierApprobation());
		assertFalse(result1.isModifierVisa());
		assertNull(result1.getValeurApprobation());
		assertNull(result1.getValeurVisa());

		assertEquals(RefEtatEnum.SAISIE.getCodeEtat(), result2.getIdRefEtat().intValue());
		assertEquals(9005131, result2.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result2.isAffichageApprobation());
		assertFalse(result2.isAffichageBoutonAnnuler());
		assertFalse(result2.isAffichageBoutonImprimer());
		assertFalse(result2.isAffichageBoutonModifier());
		assertFalse(result2.isAffichageBoutonSupprimer());
		assertFalse(result2.isAffichageBoutonDupliquer());
		assertTrue(result2.isAffichageVisa());
		assertTrue(result2.isModifierApprobation());
		assertFalse(result2.isModifierVisa());
		assertNull(result2.getValeurApprobation());
		assertNull(result2.getValeurVisa());

		assertEquals(RefEtatEnum.APPROUVEE.getCodeEtat(), result3.getIdRefEtat().intValue());
		assertEquals(9005132, result3.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result3.isAffichageApprobation());
		assertFalse(result3.isAffichageBoutonAnnuler());
		assertFalse(result3.isAffichageBoutonImprimer());
		assertFalse(result3.isAffichageBoutonModifier());
		assertFalse(result3.isAffichageBoutonSupprimer());
		assertFalse(result3.isAffichageBoutonDupliquer());
		assertTrue(result3.isAffichageVisa());
		assertTrue(result3.isModifierApprobation());
		assertFalse(result3.isModifierVisa());
		assertNull(result3.getValeurApprobation());
		assertNull(result3.getValeurVisa());

		assertEquals(RefEtatEnum.REFUSEE.getCodeEtat(), result4.getIdRefEtat().intValue());
		assertEquals(9005133, result4.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result4.isAffichageApprobation());
		assertFalse(result4.isAffichageBoutonAnnuler());
		assertFalse(result4.isAffichageBoutonImprimer());
		assertFalse(result4.isAffichageBoutonModifier());
		assertFalse(result4.isAffichageBoutonSupprimer());
		assertFalse(result4.isAffichageBoutonDupliquer());
		assertTrue(result4.isAffichageVisa());
		assertTrue(result4.isModifierApprobation());
		assertFalse(result4.isModifierVisa());
		assertNull(result4.getValeurApprobation());
		assertNull(result4.getValeurVisa());

		assertEquals(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat(), result5.getIdRefEtat().intValue());
		assertEquals(9005134, result5.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result5.isAffichageApprobation());
		assertFalse(result5.isAffichageBoutonAnnuler());
		assertFalse(result5.isAffichageBoutonImprimer());
		assertFalse(result5.isAffichageBoutonModifier());
		assertFalse(result5.isAffichageBoutonSupprimer());
		assertFalse(result5.isAffichageBoutonDupliquer());
		assertTrue(result5.isAffichageVisa());
		assertTrue(result5.isModifierApprobation());
		assertFalse(result5.isModifierVisa());
		assertNull(result5.getValeurApprobation());
		assertNull(result5.getValeurVisa());

		assertEquals(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat(), result6.getIdRefEtat().intValue());
		assertEquals(9005135, result6.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result6.isAffichageApprobation());
		assertFalse(result6.isAffichageBoutonAnnuler());
		assertFalse(result6.isAffichageBoutonImprimer());
		assertFalse(result6.isAffichageBoutonModifier());
		assertFalse(result6.isAffichageBoutonSupprimer());
		assertFalse(result6.isAffichageBoutonDupliquer());
		assertTrue(result6.isAffichageVisa());
		assertTrue(result6.isModifierApprobation());
		assertFalse(result6.isModifierVisa());
		assertNull(result6.getValeurApprobation());
		assertNull(result6.getValeurVisa());

		assertEquals(RefEtatEnum.PRISE.getCodeEtat(), result7.getIdRefEtat().intValue());
		assertEquals(9005136, result7.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result7.isAffichageApprobation());
		assertFalse(result7.isAffichageBoutonAnnuler());
		assertFalse(result7.isAffichageBoutonImprimer());
		assertFalse(result7.isAffichageBoutonModifier());
		assertFalse(result7.isAffichageBoutonSupprimer());
		assertFalse(result7.isAffichageBoutonDupliquer());
		assertTrue(result7.isAffichageVisa());
		assertFalse(result7.isModifierApprobation());
		assertFalse(result7.isModifierVisa());
		assertNull(result7.getValeurApprobation());
		assertNull(result7.getValeurVisa());

		assertEquals(RefEtatEnum.ANNULEE.getCodeEtat(), result8.getIdRefEtat().intValue());
		assertEquals(9005137, result8.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result8.isAffichageApprobation());
		assertFalse(result8.isAffichageBoutonAnnuler());
		assertFalse(result8.isAffichageBoutonImprimer());
		assertFalse(result8.isAffichageBoutonModifier());
		assertFalse(result8.isAffichageBoutonSupprimer());
		assertFalse(result8.isAffichageBoutonDupliquer());
		assertTrue(result8.isAffichageVisa());
		assertFalse(result8.isModifierApprobation());
		assertFalse(result8.isModifierVisa());
		assertNull(result8.getValeurApprobation());
		assertNull(result8.getValeurVisa());

		assertEquals(RefEtatEnum.VALIDEE.getCodeEtat(), result9.getIdRefEtat().intValue());
		assertEquals(9005138, result9.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result9.isAffichageApprobation());
		assertFalse(result9.isAffichageBoutonAnnuler());
		assertFalse(result9.isAffichageBoutonImprimer());
		assertFalse(result9.isAffichageBoutonModifier());
		assertFalse(result9.isAffichageBoutonSupprimer());
		assertFalse(result9.isAffichageBoutonDupliquer());
		assertTrue(result9.isAffichageVisa());
		assertFalse(result9.isModifierApprobation());
		assertFalse(result9.isModifierVisa());
		assertNull(result9.getValeurApprobation());
		assertNull(result9.getValeurVisa());

		assertEquals(RefEtatEnum.REJETE.getCodeEtat(), result10.getIdRefEtat().intValue());
		assertEquals(9005139, result10.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result10.isAffichageApprobation());
		assertFalse(result10.isAffichageBoutonAnnuler());
		assertFalse(result10.isAffichageBoutonImprimer());
		assertFalse(result10.isAffichageBoutonModifier());
		assertFalse(result10.isAffichageBoutonSupprimer());
		assertFalse(result10.isAffichageBoutonDupliquer());
		assertTrue(result10.isAffichageVisa());
		assertFalse(result10.isModifierApprobation());
		assertFalse(result10.isModifierVisa());
		assertNull(result10.getValeurApprobation());
		assertNull(result10.getValeurVisa());

		assertEquals(RefEtatEnum.EN_ATTENTE.getCodeEtat(), result11.getIdRefEtat().intValue());
		assertEquals(9005140, result11.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result11.isAffichageApprobation());
		assertFalse(result11.isAffichageBoutonAnnuler());
		assertFalse(result11.isAffichageBoutonImprimer());
		assertFalse(result11.isAffichageBoutonModifier());
		assertFalse(result11.isAffichageBoutonSupprimer());
		assertFalse(result11.isAffichageBoutonDupliquer());
		assertTrue(result11.isAffichageVisa());
		assertFalse(result11.isModifierApprobation());
		assertFalse(result11.isModifierVisa());
		assertNull(result11.getValeurApprobation());
		assertNull(result11.getValeurVisa());
	}
	
	@Test
	public void checkStatutAgentFonctionnaire_ok() {
		
		ReturnMessageDto srm = new ReturnMessageDto();
		Integer idAgent = 9005138;
		
		Spcarr carr = new Spcarr();
			carr.setCdcate(1);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());
		
		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(Mockito.anyInt(), Mockito.isA(Date.class))).thenReturn(carr);
		
		ReflectionTestUtils.setField(impl, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(impl, "helperService", helperService);
		
		srm = impl.checkStatutAgentFonctionnaire(srm, idAgent);
		
		assertEquals(srm.getErrors().size(), 0);
	}
	
	@Test
	public void checkStatutAgentFonctionnaire_ko() {
		
		ReturnMessageDto srm = new ReturnMessageDto();
		Integer idAgent = 9005138;
		
		Spcarr carr = new Spcarr();
			carr.setCdcate(4);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());
		
		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(Mockito.anyInt(), Mockito.isA(Date.class))).thenReturn(carr);
		
		ReflectionTestUtils.setField(impl, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(impl, "helperService", helperService);
		
		srm = impl.checkStatutAgentFonctionnaire(srm, idAgent);
		
		assertEquals(srm.getErrors().get(0), AbstractAbsenceDataConsistencyRules.STATUT_AGENT_FONCTIONNAIRE);
	}
	
	@Test
	public void checkStatutAgentFonctionnaire_koBis() {
		
		ReturnMessageDto srm = new ReturnMessageDto();
		Integer idAgent = 9005138;
		
		Spcarr carr = new Spcarr();
			carr.setCdcate(7);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());
		
		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(Mockito.anyInt(), Mockito.isA(Date.class))).thenReturn(carr);
		
		ReflectionTestUtils.setField(impl, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(impl, "helperService", helperService);
		
		srm = impl.checkStatutAgentFonctionnaire(srm, idAgent);
		
		assertEquals(srm.getErrors().get(0), AbstractAbsenceDataConsistencyRules.STATUT_AGENT_FONCTIONNAIRE);
	}
}