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

import nc.noumea.mairie.abs.domain.AgentRecupCount;
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
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IAccessRightsRepository;
import nc.noumea.mairie.abs.repository.ICounterRepository;
import nc.noumea.mairie.abs.repository.IDemandeRepository;
import nc.noumea.mairie.abs.repository.IRecuperationRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.domain.Spadmn;
import nc.noumea.mairie.sirh.domain.Agent;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.staticmock.MockStaticEntityMethods;
import org.springframework.test.util.ReflectionTestUtils;

@MockStaticEntityMethods
public class AbsRecuperationDataConsistencyRulesImplTest {

	@Test
	public void checkEtatsDemandeAcceptes_isProvisoire() {

		ReturnMessageDto srm = new ReturnMessageDto();
		Demande demande = new Demande();
		demande.setIdDemande(1);
		EtatDemande etat = new EtatDemande();
		etat.setEtat(RefEtatEnum.PROVISOIRE);
		demande.getEtatsDemande().add(etat);
		
		AbsRecuperationDataConsistencyRulesImpl impl = new AbsRecuperationDataConsistencyRulesImpl();
		
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
		
		AbsRecuperationDataConsistencyRulesImpl impl = new AbsRecuperationDataConsistencyRulesImpl();
		
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
		
		AbsRecuperationDataConsistencyRulesImpl impl = new AbsRecuperationDataConsistencyRulesImpl();
		
		srm = impl.checkEtatsDemandeAcceptes(srm, demande, Arrays.asList(RefEtatEnum.PROVISOIRE, RefEtatEnum.SAISIE));
		
		assertEquals(1, srm.getErrors().size());
		assertEquals("La modification de la demande [1] n'est autorisée que si l'état est à [PROVISOIRE SAISIE ].", srm.getErrors().get(0).toString());
	}
	
	@Test
	public void checkDepassementDroitsAcquis_iOk() {
		
		ReturnMessageDto srm = new ReturnMessageDto();
		DemandeRecup demande = new DemandeRecup();
		demande.setIdAgent(9005138);
		demande.setDuree(40);
		
		AgentRecupCount soldeRecup = new AgentRecupCount();
		soldeRecup.setTotalMinutes(50);
		
		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentRecupCount.class, demande.getIdAgent())).thenReturn(soldeRecup);
		
		IRecuperationRepository recuperationRepository = Mockito.mock(IRecuperationRepository.class);
		Mockito.when(recuperationRepository.getSommeDureeDemandeRecupEnCoursSaisieouVisee(demande.getIdAgent(), null)).thenReturn(10);
				
		AbsRecuperationDataConsistencyRulesImpl impl = new AbsRecuperationDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "recuperationRepository", recuperationRepository);
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);
		
		srm = impl.checkDepassementDroitsAcquis(srm, demande);
		
		assertEquals(0, srm.getErrors().size());
	}
	
	@Test
	public void checkDepassementDroitsAcquis_iKo() {
		
		ReturnMessageDto srm = new ReturnMessageDto();
		DemandeRecup demande = new DemandeRecup();
		demande.setIdAgent(9005138);
		demande.setDuree(41);
		
		AgentRecupCount soldeRecup = new AgentRecupCount();
		soldeRecup.setTotalMinutes(50);
		
		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentRecupCount.class, demande.getIdAgent())).thenReturn(soldeRecup);
		
		IRecuperationRepository recuperationRepository = Mockito.mock(IRecuperationRepository.class);
		Mockito.when(recuperationRepository.getSommeDureeDemandeRecupEnCoursSaisieouVisee(demande.getIdAgent(), null)).thenReturn(10);
				
		AbsRecuperationDataConsistencyRulesImpl impl = new AbsRecuperationDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "recuperationRepository", recuperationRepository);
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);
		
		srm = impl.checkDepassementDroitsAcquis(srm, demande);
		
		assertEquals(1, srm.getErrors().size());
		assertEquals("Le dépassement des droits acquis n'est pas autorisé.", srm.getErrors().get(0).toString());
	}
	
	@Test
	public void checkDemandeDejaSaisieSurMemePeriode_withEtatExistantProvisoireAndRefuse(){
		
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
		Mockito.when(demandeRepository.listeDemandesAgent(null, demande.getIdAgent(), null, null, null)).thenReturn(listDemande);
		
		AbsRecuperationDataConsistencyRulesImpl impl = new AbsRecuperationDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "demandeRepository", demandeRepository);
		
		srm = impl.checkDemandeDejaSaisieSurMemePeriode(srm, demande);
		
		assertEquals(0, srm.getErrors().size());
	}
	
	@Test
	public void checkDemandeDejaSaisieSurMemePeriode_Ok(){
		
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
		Mockito.when(demandeRepository.listeDemandesAgent(null, demande.getIdAgent(), null, null, null)).thenReturn(listDemande);
		
		AbsRecuperationDataConsistencyRulesImpl impl = new AbsRecuperationDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "demandeRepository", demandeRepository);
		
		srm = impl.checkDemandeDejaSaisieSurMemePeriode(srm, demande);
		
		assertEquals(0, srm.getErrors().size());
	}
	
	@Test
	public void checkDemandeDejaSaisieSurMemePeriode_DateFinKo(){
		
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
		Mockito.when(demandeRepository.listeDemandesAgent(null, demande.getIdAgent(), null, null, null)).thenReturn(listDemande);
		
		AbsRecuperationDataConsistencyRulesImpl impl = new AbsRecuperationDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "demandeRepository", demandeRepository);
		
		srm = impl.checkDemandeDejaSaisieSurMemePeriode(srm, demande);
		
		assertEquals(1, srm.getErrors().size());
		assertEquals("La demande ne peut être couverte totalement ou partiellement par une autre absence.", srm.getErrors().get(0).toString());
	}
	
	@Test
	public void checkDemandeDejaSaisieSurMemePeriode_DateDebutKo(){
		
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
		Mockito.when(demandeRepository.listeDemandesAgent(null, demande.getIdAgent(), null, null, null)).thenReturn(listDemande);
		
		AbsRecuperationDataConsistencyRulesImpl impl = new AbsRecuperationDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "demandeRepository", demandeRepository);
		
		srm = impl.checkDemandeDejaSaisieSurMemePeriode(srm, demande);
		
		assertEquals(1, srm.getErrors().size());
		assertEquals("La demande ne peut être couverte totalement ou partiellement par une autre absence.", srm.getErrors().get(0).toString());
	}
	
	@Test
	public void checkAgentInactivity_AgentActif(){
		
		List<String> activitesCode = Arrays.asList("01", "02", "03", "04", "23", "24", "60", "61",
				"62", "63", "64", "65", "66");
		
		ReturnMessageDto srm = new ReturnMessageDto();
		Integer idAgent = 9005138;
		Agent ag = new Agent();
		Spadmn adm = new Spadmn();
		
		Date date = new Date();
		
		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgent(idAgent)).thenReturn(ag);
		Mockito.when(sirhRepository.getAgentCurrentPosition(ag, date)).thenReturn(adm);
		
		AbsRecuperationDataConsistencyRulesImpl impl = new AbsRecuperationDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "sirhRepository", sirhRepository);
		
		for(String codeAcivite : activitesCode) {
			adm.setCdpadm(codeAcivite);
			srm = impl.checkAgentInactivity(srm, idAgent, date);
		}
		
		assertEquals(0, srm.getErrors().size());
	}
	
	@Test
	public void checkAgentInactivity_AgentInactif(){
		
		ReturnMessageDto srm = new ReturnMessageDto();
		Integer idAgent = 9005138;
		Agent ag = new Agent();
		Spadmn adm = new Spadmn();
		adm.setCdpadm("05");
		
		Date date = new Date();
		
		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgent(idAgent)).thenReturn(ag);
		Mockito.when(sirhRepository.getAgentCurrentPosition(ag, date)).thenReturn(adm);
		
		AbsRecuperationDataConsistencyRulesImpl impl = new AbsRecuperationDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "sirhRepository", sirhRepository);
		
		srm = impl.checkAgentInactivity(srm, idAgent, date);
		
		assertEquals(1, srm.getErrors().size());
		assertEquals("L'agent n'est pas en activité sur cette période.", srm.getErrors().get(0).toString());
	}
	
	@Test
	public void checkChampMotifPourEtatDonne_motifVide_Refuse() {
		
		ReturnMessageDto srm = new ReturnMessageDto();
		
		AbsRecuperationDataConsistencyRulesImpl impl = new AbsRecuperationDataConsistencyRulesImpl();
		srm = impl.checkChampMotifPourEtatDonne(srm, RefEtatEnum.REFUSEE.getCodeEtat(), null);
		
		assertEquals(1, srm.getErrors().size());
		assertEquals("Le motif est obligatoire.", srm.getErrors().get(0).toString());
	}
	
	@Test
	public void checkChampMotifPourEtatDonne_motifVide_ViseeD() {
		
		ReturnMessageDto srm = new ReturnMessageDto();
		
		AbsRecuperationDataConsistencyRulesImpl impl = new AbsRecuperationDataConsistencyRulesImpl();
		srm = impl.checkChampMotifPourEtatDonne(srm, RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat(), null);
		
		assertEquals(1, srm.getErrors().size());
		assertEquals("Le motif est obligatoire.", srm.getErrors().get(0).toString());
	}
	
	@Test
	public void checkChampMotifPourEtatDonne_Ok_motifSaisi() {
		
		ReturnMessageDto srm = new ReturnMessageDto();
		
		AbsRecuperationDataConsistencyRulesImpl impl = new AbsRecuperationDataConsistencyRulesImpl();
		srm = impl.checkChampMotifPourEtatDonne(srm, RefEtatEnum.REFUSEE.getCodeEtat(), "motif");
		
		assertEquals(0, srm.getErrors().size());
	}
	
	@Test
	public void checkChampMotifPourEtatDonne_Ok_motifNonSaisi_EtatApprouve() {
		
		ReturnMessageDto srm = new ReturnMessageDto();
		
		AbsRecuperationDataConsistencyRulesImpl impl = new AbsRecuperationDataConsistencyRulesImpl();
		srm = impl.checkChampMotifPourEtatDonne(srm, RefEtatEnum.APPROUVEE.getCodeEtat(), null);
		
		assertEquals(0, srm.getErrors().size());
	}
	
	@Test
	public void checkChampMotifPourEtatDonne_Ok_motifSaisi_EtatApprouve() {
		
		ReturnMessageDto srm = new ReturnMessageDto();
		
		AbsRecuperationDataConsistencyRulesImpl impl = new AbsRecuperationDataConsistencyRulesImpl();
		srm = impl.checkChampMotifPourEtatDonne(srm, RefEtatEnum.APPROUVEE.getCodeEtat(), "motif");
		
		assertEquals(0, srm.getErrors().size());
	}
	
	@Test
	public void verifDemandeExiste_demandeExiste() {
		ReturnMessageDto srm = new ReturnMessageDto();
		Demande demande = new Demande();
		
		AbsRecuperationDataConsistencyRulesImpl impl = new AbsRecuperationDataConsistencyRulesImpl();
		srm = impl.verifDemandeExiste(demande, srm);
		
		assertEquals(0, srm.getErrors().size());
	}
	
	@Test
	public void verifDemandeExiste_demandeNotExiste() {
		ReturnMessageDto srm = new ReturnMessageDto();
		Demande demande = null;
		
		AbsRecuperationDataConsistencyRulesImpl impl = new AbsRecuperationDataConsistencyRulesImpl();
		srm = impl.verifDemandeExiste(demande, srm);
		
		assertEquals(1, srm.getErrors().size());
	}
	
	
	@Test
	public void filtreDateAndEtatDemandeFromList_WrongParam() {
	
		AbsRecuperationDataConsistencyRulesImpl service = new AbsRecuperationDataConsistencyRulesImpl();

		// When
		List<DemandeDto> result = service.filtreDateAndEtatDemandeFromList(new ArrayList<Demande>(), null, new Date());

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

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
			Mockito.when(sirhRepository.getAgent(d.getIdAgent())).thenReturn(new Agent());
			Mockito.when(sirhRepository.getAgent(d2.getIdAgent())).thenReturn(new Agent());
		
		AbsRecuperationDataConsistencyRulesImpl service = new AbsRecuperationDataConsistencyRulesImpl();
			ReflectionTestUtils.setField(service, "absEntityManager", emMock);
			ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);

		// When
		List<DemandeDto> result = service.filtreDateAndEtatDemandeFromList(listeDemande, Arrays.asList(etatPris), new Date());
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

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
			Mockito.when(sirhRepository.getAgent(d.getIdAgent())).thenReturn(new Agent());
			Mockito.when(sirhRepository.getAgent(d2.getIdAgent())).thenReturn(new Agent());
		
		EntityManager emMock = Mockito.mock(EntityManager.class);
		Mockito.when(emMock.find(RefEtat.class, RefEtatEnum.SAISIE.getCodeEtat())).thenReturn(etatSaisie);
		Mockito.when(emMock.find(RefEtat.class, RefEtatEnum.PROVISOIRE.getCodeEtat())).thenReturn(etatProvisoire);
				
		AbsRecuperationDataConsistencyRulesImpl service = new AbsRecuperationDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(service, "absEntityManager", emMock);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);

		// When
		List<DemandeDto> result = service.filtreDateAndEtatDemandeFromList(listeDemande, Arrays.asList(etatProvisoire, etatSaisie), new LocalDate(2014, 1, 8).toDate());
		
		// Then
		assertEquals(1, result.size());
		assertEquals("50", result.get(0).getDuree().toString());
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

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
			Mockito.when(sirhRepository.getAgent(d.getIdAgent())).thenReturn(new Agent());
			Mockito.when(sirhRepository.getAgent(d2.getIdAgent())).thenReturn(new Agent());
		
		AbsRecuperationDataConsistencyRulesImpl service = new AbsRecuperationDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(service, "absEntityManager", emMock);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);

		// When
		List<DemandeDto> result = service.filtreDateAndEtatDemandeFromList(listeDemande, Arrays.asList(etatProvisoire), new Date());

		// Then
		assertEquals(1, result.size());
		assertEquals("50", result.get(0).getDuree().toString());
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

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
			Mockito.when(sirhRepository.getAgent(d.getIdAgent())).thenReturn(new Agent());
			Mockito.when(sirhRepository.getAgent(d2.getIdAgent())).thenReturn(new Agent());
		
		AbsRecuperationDataConsistencyRulesImpl service = new AbsRecuperationDataConsistencyRulesImpl();
			ReflectionTestUtils.setField(service, "absEntityManager", emMock);
			ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);

		// When
		List<DemandeDto> result = service.filtreDateAndEtatDemandeFromList(listeDemande, Arrays.asList(etatProvisoire), null);

		// Then
		assertEquals(1, result.size());
		assertEquals("50", result.get(0).getDuree().toString());
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

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
			Mockito.when(sirhRepository.getAgent(d.getIdAgent())).thenReturn(new Agent());
			Mockito.when(sirhRepository.getAgent(d2.getIdAgent())).thenReturn(new Agent());
		
		AbsRecuperationDataConsistencyRulesImpl service = new AbsRecuperationDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(service, "absEntityManager", emMock);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);

		// When
		List<DemandeDto> result = service.filtreDateAndEtatDemandeFromList(listeDemande, null, null);
		
		// Then
		assertEquals(2, result.size());
		assertEquals("30", result.get(0).getDuree().toString());
		assertFalse(result.get(0).isAffichageBoutonImprimer());
		assertFalse(result.get(0).isAffichageBoutonModifier());
		assertFalse(result.get(0).isAffichageBoutonSupprimer());
		assertEquals("motif",result.get(0).getMotif());
		assertEquals("50", result.get(1).getDuree().toString());
		assertFalse(result.get(1).isAffichageBoutonImprimer());
		assertFalse(result.get(1).isAffichageBoutonModifier());
		assertFalse(result.get(1).isAffichageBoutonSupprimer());
		assertEquals("motif",result.get(1).getMotif());
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

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
			Mockito.when(sirhRepository.getAgent(d.getIdAgent())).thenReturn(new Agent());
			Mockito.when(sirhRepository.getAgent(d2.getIdAgent())).thenReturn(new Agent());
			Mockito.when(sirhRepository.getAgent(d3.getIdAgent())).thenReturn(new Agent());
			Mockito.when(sirhRepository.getAgent(d4.getIdAgent())).thenReturn(new Agent());
			Mockito.when(sirhRepository.getAgent(d5.getIdAgent())).thenReturn(new Agent());
			Mockito.when(sirhRepository.getAgent(d6.getIdAgent())).thenReturn(new Agent());
			Mockito.when(sirhRepository.getAgent(d7.getIdAgent())).thenReturn(new Agent());
			Mockito.when(sirhRepository.getAgent(d8.getIdAgent())).thenReturn(new Agent());
		
		AbsRecuperationDataConsistencyRulesImpl service = new AbsRecuperationDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(service, "absEntityManager", emMock);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);

		// When
		List<DemandeDto> result = service.filtreDateAndEtatDemandeFromList(listeDemande, Arrays.asList(etatVISEE_F, etatSaisie, etatVISEE_D, etatApprouve), null);

		// Then
		assertEquals(4, result.size());
		assertEquals("30", result.get(0).getDuree().toString());
		assertFalse(result.get(0).isAffichageBoutonImprimer());
		assertFalse(result.get(0).isAffichageBoutonModifier());
		assertFalse(result.get(0).isAffichageBoutonSupprimer());
		assertEquals("motif",result.get(0).getMotif());
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
		
		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
			Mockito.when(sirhRepository.getAgent(d.getIdAgent())).thenReturn(new Agent());
			Mockito.when(sirhRepository.getAgent(d2.getIdAgent())).thenReturn(new Agent());
		
		AbsRecuperationDataConsistencyRulesImpl service = new AbsRecuperationDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(service, "absEntityManager", emMock);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);

		// When
		List<DemandeDto> result = service.filtreDateAndEtatDemandeFromList(listeDemande, Arrays.asList(etatSaisie), new Date());

		// Then
		assertEquals(1, result.size());
		assertEquals("30", result.get(0).getDuree().toString());
		assertFalse(result.get(0).isAffichageBoutonImprimer());
		assertFalse(result.get(0).isAffichageBoutonModifier());
		assertFalse(result.get(0).isAffichageBoutonSupprimer());
	}
	
	@Test
	public void filtreDroitOfListeDemandesByDemande_DemandeOfAgent() {
		
		Integer idAgentConnecte = 9005138;
		Integer idAgentConcerne = 9005138;
		
		DemandeDto demandeDtoProvisoire = new DemandeDto();
			demandeDtoProvisoire.setIdRefEtat(RefEtatEnum.PROVISOIRE.getCodeEtat());
			demandeDtoProvisoire.setIdAgent(9005138);
			demandeDtoProvisoire.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		DemandeDto demandeDtoSaisie = new DemandeDto();
			demandeDtoSaisie.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());
			demandeDtoSaisie.setIdAgent(9005138);
			demandeDtoSaisie.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		DemandeDto demandeDtoApprouve = new DemandeDto();
			demandeDtoApprouve.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());
			demandeDtoApprouve.setIdAgent(9005138);
			demandeDtoApprouve.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		DemandeDto demandeDtoRefusee = new DemandeDto();
			demandeDtoRefusee.setIdRefEtat(RefEtatEnum.REFUSEE.getCodeEtat());
			demandeDtoRefusee.setIdAgent(9005138);
			demandeDtoRefusee.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		DemandeDto demandeDtoVisee_F = new DemandeDto();
			demandeDtoVisee_F.setIdRefEtat(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat());
			demandeDtoVisee_F.setIdAgent(9005138);
			demandeDtoVisee_F.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		DemandeDto demandeDtoVisee_D = new DemandeDto();
			demandeDtoVisee_D.setIdRefEtat(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat());
			demandeDtoVisee_D.setIdAgent(9005138);
			demandeDtoVisee_D.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		DemandeDto demandeDtoPrise = new DemandeDto();
			demandeDtoPrise.setIdRefEtat(RefEtatEnum.PRISE.getCodeEtat());
			demandeDtoPrise.setIdAgent(9005138);
			demandeDtoPrise.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		DemandeDto demandeDtoAnnulee = new DemandeDto();
			demandeDtoAnnulee.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
			demandeDtoAnnulee.setIdAgent(9005138);
			demandeDtoAnnulee.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		List<DemandeDto> resultListDto = new ArrayList<DemandeDto>();
			resultListDto.addAll(Arrays.asList(demandeDtoProvisoire, demandeDtoSaisie, demandeDtoApprouve, demandeDtoRefusee, demandeDtoVisee_F, demandeDtoVisee_D, demandeDtoPrise, demandeDtoAnnulee));
		
		AbsRecuperationDataConsistencyRulesImpl service = new AbsRecuperationDataConsistencyRulesImpl();
		
		// When
		List<DemandeDto> result = service.filtreDroitOfListeDemandesByDemande(idAgentConnecte, idAgentConcerne, resultListDto);
		
		// Then
		assertEquals(8, result.size());
		
		assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), result.get(0).getIdRefEtat().intValue());
		assertFalse(result.get(0).isAffichageApprobation());
		assertFalse(result.get(0).isAffichageBoutonAnnuler());
		assertFalse(result.get(0).isAffichageBoutonImprimer());
		assertTrue(result.get(0).isAffichageBoutonModifier());
		assertTrue(result.get(0).isAffichageBoutonSupprimer());
		assertFalse(result.get(0).isAffichageVisa());
		assertFalse(result.get(0).isModifierApprobation());
		assertFalse(result.get(0).isModifierVisa());
		assertNull(result.get(0).getValeurApprobation());
		assertNull(result.get(0).getValeurVisa());
		assertEquals(null,result.get(0).getMotif());

		assertEquals(RefEtatEnum.SAISIE.getCodeEtat(), result.get(1).getIdRefEtat().intValue());
		assertFalse(result.get(1).isAffichageApprobation());
		assertFalse(result.get(1).isAffichageBoutonAnnuler());
		assertFalse(result.get(1).isAffichageBoutonImprimer());
		assertTrue(result.get(1).isAffichageBoutonModifier());
		assertTrue(result.get(1).isAffichageBoutonSupprimer());
		assertFalse(result.get(1).isAffichageVisa());
		assertFalse(result.get(1).isModifierApprobation());
		assertFalse(result.get(1).isModifierVisa());
		assertNull(result.get(1).getValeurApprobation());
		assertNull(result.get(1).getValeurVisa());
		assertEquals(null,result.get(1).getMotif());

		assertEquals(RefEtatEnum.APPROUVEE.getCodeEtat(), result.get(2).getIdRefEtat().intValue());
		assertFalse(result.get(2).isAffichageApprobation());
		assertTrue(result.get(2).isAffichageBoutonAnnuler());
		assertTrue(result.get(2).isAffichageBoutonImprimer());
		assertFalse(result.get(2).isAffichageBoutonModifier());
		assertFalse(result.get(2).isAffichageBoutonSupprimer());
		assertFalse(result.get(2).isAffichageVisa());
		assertFalse(result.get(2).isModifierApprobation());
		assertFalse(result.get(2).isModifierVisa());
		assertNull(result.get(2).getValeurApprobation());
		assertNull(result.get(2).getValeurVisa());
		assertEquals(null,result.get(2).getMotif());

		assertEquals(RefEtatEnum.REFUSEE.getCodeEtat(), result.get(3).getIdRefEtat().intValue());
		assertFalse(result.get(3).isAffichageApprobation());
		assertFalse(result.get(3).isAffichageBoutonAnnuler());
		assertFalse(result.get(3).isAffichageBoutonImprimer());
		assertFalse(result.get(3).isAffichageBoutonModifier());
		assertFalse(result.get(3).isAffichageBoutonSupprimer());
		assertFalse(result.get(3).isAffichageVisa());
		assertFalse(result.get(3).isModifierApprobation());
		assertFalse(result.get(3).isModifierVisa());
		assertNull(result.get(3).getValeurApprobation());
		assertNull(result.get(3).getValeurVisa());
		assertEquals(null,result.get(3).getMotif());

		assertEquals(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat(), result.get(4).getIdRefEtat().intValue());
		assertFalse(result.get(4).isAffichageApprobation());
		assertTrue(result.get(4).isAffichageBoutonAnnuler());
		assertFalse(result.get(4).isAffichageBoutonImprimer());
		assertFalse(result.get(4).isAffichageBoutonModifier());
		assertFalse(result.get(4).isAffichageBoutonSupprimer());
		assertFalse(result.get(4).isAffichageVisa());
		assertFalse(result.get(4).isModifierApprobation());
		assertFalse(result.get(4).isModifierVisa());
		assertNull(result.get(4).getValeurApprobation());
		assertNull(result.get(4).getValeurVisa());

		assertEquals(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat(), result.get(5).getIdRefEtat().intValue());
		assertFalse(result.get(5).isAffichageApprobation());
		assertTrue(result.get(5).isAffichageBoutonAnnuler());
		assertFalse(result.get(5).isAffichageBoutonImprimer());
		assertFalse(result.get(5).isAffichageBoutonModifier());
		assertFalse(result.get(5).isAffichageBoutonSupprimer());
		assertFalse(result.get(5).isAffichageVisa());
		assertFalse(result.get(5).isModifierApprobation());
		assertFalse(result.get(5).isModifierVisa());
		assertNull(result.get(5).getValeurApprobation());
		assertNull(result.get(5).getValeurVisa());
		
		assertEquals(RefEtatEnum.PRISE.getCodeEtat(), result.get(6).getIdRefEtat().intValue());
		assertFalse(result.get(6).isAffichageApprobation());
		assertFalse(result.get(6).isAffichageBoutonAnnuler());
		assertFalse(result.get(6).isAffichageBoutonImprimer());
		assertFalse(result.get(6).isAffichageBoutonModifier());
		assertFalse(result.get(6).isAffichageBoutonSupprimer());
		assertFalse(result.get(6).isAffichageVisa());
		assertFalse(result.get(6).isModifierApprobation());
		assertFalse(result.get(6).isModifierVisa());
		assertNull(result.get(6).getValeurApprobation());
		assertNull(result.get(6).getValeurVisa());

		assertEquals(RefEtatEnum.ANNULEE.getCodeEtat(), result.get(7).getIdRefEtat().intValue());
		assertFalse(result.get(7).isAffichageApprobation());
		assertFalse(result.get(7).isAffichageBoutonAnnuler());
		assertFalse(result.get(7).isAffichageBoutonImprimer());
		assertFalse(result.get(7).isAffichageBoutonModifier());
		assertFalse(result.get(7).isAffichageBoutonSupprimer());
		assertFalse(result.get(7).isAffichageVisa());
		assertFalse(result.get(7).isModifierApprobation());
		assertFalse(result.get(7).isModifierVisa());
		assertNull(result.get(7).getValeurApprobation());
		assertNull(result.get(7).getValeurVisa());
	}
	
	@Test
	public void filtreDroitOfListeDemandesByDemande_Operateur() {
		
		Integer idAgentConnecte = 9005138;
		
		// les demandes
		DemandeDto demandeDtoProvisoire = new DemandeDto();
			demandeDtoProvisoire.setIdRefEtat(RefEtatEnum.PROVISOIRE.getCodeEtat());
			demandeDtoProvisoire.setIdAgent(9005130);
			demandeDtoProvisoire.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		DemandeDto demandeDtoSaisie = new DemandeDto();
			demandeDtoSaisie.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());
			demandeDtoSaisie.setIdAgent(9005131);
			demandeDtoSaisie.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		DemandeDto demandeDtoApprouve = new DemandeDto();
			demandeDtoApprouve.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());
			demandeDtoApprouve.setIdAgent(9005132);
			demandeDtoApprouve.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		DemandeDto demandeDtoRefusee = new DemandeDto();
			demandeDtoRefusee.setIdRefEtat(RefEtatEnum.REFUSEE.getCodeEtat());
			demandeDtoRefusee.setIdAgent(9005133);
			demandeDtoRefusee.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		DemandeDto demandeDtoVisee_F = new DemandeDto();
			demandeDtoVisee_F.setIdRefEtat(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat());
			demandeDtoVisee_F.setIdAgent(9005134);
			demandeDtoVisee_F.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		DemandeDto demandeDtoVisee_D = new DemandeDto();
			demandeDtoVisee_D.setIdRefEtat(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat());
			demandeDtoVisee_D.setIdAgent(9005135);
			demandeDtoVisee_D.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		DemandeDto demandeDtoPrise = new DemandeDto();
			demandeDtoPrise.setIdRefEtat(RefEtatEnum.PRISE.getCodeEtat());
			demandeDtoPrise.setIdAgent(9005136);
			demandeDtoPrise.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		DemandeDto demandeDtoAnnulee = new DemandeDto();
			demandeDtoAnnulee.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
			demandeDtoAnnulee.setIdAgent(9005137);
			demandeDtoAnnulee.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		List<DemandeDto> resultListDto = new ArrayList<DemandeDto>();
			resultListDto.addAll(Arrays.asList(demandeDtoProvisoire, demandeDtoSaisie, demandeDtoApprouve, demandeDtoRefusee, demandeDtoVisee_F, demandeDtoVisee_D, demandeDtoPrise, demandeDtoAnnulee));
		
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
		
		List<DroitsAgent> listDroitAgent = new ArrayList<DroitsAgent>();
		listDroitAgent.addAll(Arrays.asList(da, da1, da2, da3, da4, da5, da6, da7));
		
		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.getListOfAgentsToInputOrApprove(idAgentConnecte, null)).thenReturn(listDroitAgent);
		
		AbsRecuperationDataConsistencyRulesImpl service = new AbsRecuperationDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		
		// When
		List<DemandeDto> result = service.filtreDroitOfListeDemandesByDemande(idAgentConnecte, null, resultListDto);
		
		// Then
		assertEquals(8, result.size());
		
		assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), result.get(0).getIdRefEtat().intValue());
		assertEquals(9005130, result.get(0).getIdAgent().intValue());
		assertFalse(result.get(0).isAffichageApprobation());
		assertFalse(result.get(0).isAffichageBoutonAnnuler());
		assertFalse(result.get(0).isAffichageBoutonImprimer());
		assertTrue(result.get(0).isAffichageBoutonModifier());
		assertTrue(result.get(0).isAffichageBoutonSupprimer());
		assertFalse(result.get(0).isAffichageVisa());
		assertFalse(result.get(0).isModifierApprobation());
		assertFalse(result.get(0).isModifierVisa());
		assertNull(result.get(0).getValeurApprobation());
		assertNull(result.get(0).getValeurVisa());

		assertEquals(RefEtatEnum.SAISIE.getCodeEtat(), result.get(1).getIdRefEtat().intValue());
		assertEquals(9005131, result.get(1).getIdAgent().intValue());
		assertFalse(result.get(1).isAffichageApprobation());
		assertFalse(result.get(1).isAffichageBoutonAnnuler());
		assertFalse(result.get(1).isAffichageBoutonImprimer());
		assertTrue(result.get(1).isAffichageBoutonModifier());
		assertTrue(result.get(1).isAffichageBoutonSupprimer());
		assertFalse(result.get(1).isAffichageVisa());
		assertFalse(result.get(1).isModifierApprobation());
		assertFalse(result.get(1).isModifierVisa());
		assertNull(result.get(1).getValeurApprobation());
		assertNull(result.get(1).getValeurVisa());

		assertEquals(RefEtatEnum.APPROUVEE.getCodeEtat(), result.get(2).getIdRefEtat().intValue());
		assertEquals(9005132, result.get(2).getIdAgent().intValue());
		assertFalse(result.get(2).isAffichageApprobation());
		assertTrue(result.get(2).isAffichageBoutonAnnuler());
		assertTrue(result.get(2).isAffichageBoutonImprimer());
		assertFalse(result.get(2).isAffichageBoutonModifier());
		assertFalse(result.get(2).isAffichageBoutonSupprimer());
		assertFalse(result.get(2).isAffichageVisa());
		assertFalse(result.get(2).isModifierApprobation());
		assertFalse(result.get(2).isModifierVisa());
		assertNull(result.get(2).getValeurApprobation());
		assertNull(result.get(2).getValeurVisa());

		assertEquals(RefEtatEnum.REFUSEE.getCodeEtat(), result.get(3).getIdRefEtat().intValue());
		assertEquals(9005133, result.get(3).getIdAgent().intValue());
		assertFalse(result.get(3).isAffichageApprobation());
		assertFalse(result.get(3).isAffichageBoutonAnnuler());
		assertFalse(result.get(3).isAffichageBoutonImprimer());
		assertFalse(result.get(3).isAffichageBoutonModifier());
		assertFalse(result.get(3).isAffichageBoutonSupprimer());
		assertFalse(result.get(3).isAffichageVisa());
		assertFalse(result.get(3).isModifierApprobation());
		assertFalse(result.get(3).isModifierVisa());
		assertNull(result.get(3).getValeurApprobation());
		assertNull(result.get(3).getValeurVisa());

		assertEquals(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat(), result.get(4).getIdRefEtat().intValue());
		assertEquals(9005134, result.get(4).getIdAgent().intValue());
		assertFalse(result.get(4).isAffichageApprobation());
		assertTrue(result.get(4).isAffichageBoutonAnnuler());
		assertFalse(result.get(4).isAffichageBoutonImprimer());
		assertFalse(result.get(4).isAffichageBoutonModifier());
		assertFalse(result.get(4).isAffichageBoutonSupprimer());
		assertFalse(result.get(4).isAffichageVisa());
		assertFalse(result.get(4).isModifierApprobation());
		assertFalse(result.get(4).isModifierVisa());
		assertNull(result.get(4).getValeurApprobation());
		assertNull(result.get(4).getValeurVisa());

		assertEquals(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat(), result.get(5).getIdRefEtat().intValue());
		assertEquals(9005135, result.get(5).getIdAgent().intValue());
		assertFalse(result.get(5).isAffichageApprobation());
		assertTrue(result.get(5).isAffichageBoutonAnnuler());
		assertFalse(result.get(5).isAffichageBoutonImprimer());
		assertFalse(result.get(5).isAffichageBoutonModifier());
		assertFalse(result.get(5).isAffichageBoutonSupprimer());
		assertFalse(result.get(5).isAffichageVisa());
		assertFalse(result.get(5).isModifierApprobation());
		assertFalse(result.get(5).isModifierVisa());
		assertNull(result.get(5).getValeurApprobation());
		assertNull(result.get(5).getValeurVisa());
		
		assertEquals(RefEtatEnum.PRISE.getCodeEtat(), result.get(6).getIdRefEtat().intValue());
		assertEquals(9005136, result.get(6).getIdAgent().intValue());
		assertFalse(result.get(6).isAffichageApprobation());
		assertFalse(result.get(6).isAffichageBoutonAnnuler());
		assertFalse(result.get(6).isAffichageBoutonImprimer());
		assertFalse(result.get(6).isAffichageBoutonModifier());
		assertFalse(result.get(6).isAffichageBoutonSupprimer());
		assertFalse(result.get(6).isAffichageVisa());
		assertFalse(result.get(6).isModifierApprobation());
		assertFalse(result.get(6).isModifierVisa());
		assertNull(result.get(6).getValeurApprobation());
		assertNull(result.get(6).getValeurVisa());

		assertEquals(RefEtatEnum.ANNULEE.getCodeEtat(), result.get(7).getIdRefEtat().intValue());
		assertEquals(9005137, result.get(7).getIdAgent().intValue());
		assertFalse(result.get(7).isAffichageApprobation());
		assertFalse(result.get(7).isAffichageBoutonAnnuler());
		assertFalse(result.get(7).isAffichageBoutonImprimer());
		assertFalse(result.get(7).isAffichageBoutonModifier());
		assertFalse(result.get(7).isAffichageBoutonSupprimer());
		assertFalse(result.get(7).isAffichageVisa());
		assertFalse(result.get(7).isModifierApprobation());
		assertFalse(result.get(7).isModifierVisa());
		assertNull(result.get(7).getValeurApprobation());
		assertNull(result.get(7).getValeurVisa());
	}
	
	@Test
	public void filtreDroitOfListeDemandesByDemande_Viseur() {
		
		Integer idAgentConnecte = 9005138;
		
		// les demandes
		DemandeDto demandeDtoProvisoire = new DemandeDto();
			demandeDtoProvisoire.setIdRefEtat(RefEtatEnum.PROVISOIRE.getCodeEtat());
			demandeDtoProvisoire.setIdAgent(9005130);
		DemandeDto demandeDtoSaisie = new DemandeDto();
			demandeDtoSaisie.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());
			demandeDtoSaisie.setIdAgent(9005131);
		DemandeDto demandeDtoApprouve = new DemandeDto();
			demandeDtoApprouve.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());
			demandeDtoApprouve.setIdAgent(9005132);
		DemandeDto demandeDtoRefusee = new DemandeDto();
			demandeDtoRefusee.setIdRefEtat(RefEtatEnum.REFUSEE.getCodeEtat());
			demandeDtoRefusee.setIdAgent(9005133);
		DemandeDto demandeDtoVisee_F = new DemandeDto();
			demandeDtoVisee_F.setIdRefEtat(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat());
			demandeDtoVisee_F.setIdAgent(9005134);
		DemandeDto demandeDtoVisee_D = new DemandeDto();
			demandeDtoVisee_D.setIdRefEtat(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat());
			demandeDtoVisee_D.setIdAgent(9005135);
		DemandeDto demandeDtoPrise = new DemandeDto();
			demandeDtoPrise.setIdRefEtat(RefEtatEnum.PRISE.getCodeEtat());
			demandeDtoPrise.setIdAgent(9005136);
		DemandeDto demandeDtoAnnulee = new DemandeDto();
			demandeDtoAnnulee.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
			demandeDtoAnnulee.setIdAgent(9005137);
		List<DemandeDto> resultListDto = new ArrayList<DemandeDto>();
			resultListDto.addAll(Arrays.asList(demandeDtoProvisoire, demandeDtoSaisie, demandeDtoApprouve, demandeDtoRefusee, demandeDtoVisee_F, demandeDtoVisee_D, demandeDtoPrise, demandeDtoAnnulee));
		
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
		
		List<DroitsAgent> listDroitAgent = new ArrayList<DroitsAgent>();
		listDroitAgent.addAll(Arrays.asList(da, da1, da2, da3, da4, da5, da6, da7));
		
		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.getListOfAgentsToInputOrApprove(idAgentConnecte, null)).thenReturn(listDroitAgent);
		
		AbsRecuperationDataConsistencyRulesImpl service = new AbsRecuperationDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		
		// When
		List<DemandeDto> result = service.filtreDroitOfListeDemandesByDemande(idAgentConnecte, null, resultListDto);
		
		// Then
		assertEquals(8, result.size());
		
		assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), result.get(0).getIdRefEtat().intValue());
		assertEquals(9005130, result.get(0).getIdAgent().intValue());
		assertTrue(result.get(0).isAffichageApprobation());
		assertFalse(result.get(0).isAffichageBoutonAnnuler());
		assertFalse(result.get(0).isAffichageBoutonImprimer());
		assertFalse(result.get(0).isAffichageBoutonModifier());
		assertFalse(result.get(0).isAffichageBoutonSupprimer());
		assertTrue(result.get(0).isAffichageVisa());
		assertFalse(result.get(0).isModifierApprobation());
		assertFalse(result.get(0).isModifierVisa());
		assertNull(result.get(0).getValeurApprobation());
		assertNull(result.get(0).getValeurVisa());

		assertEquals(RefEtatEnum.SAISIE.getCodeEtat(), result.get(1).getIdRefEtat().intValue());
		assertEquals(9005131, result.get(1).getIdAgent().intValue());
		assertTrue(result.get(1).isAffichageApprobation());
		assertFalse(result.get(1).isAffichageBoutonAnnuler());
		assertFalse(result.get(1).isAffichageBoutonImprimer());
		assertFalse(result.get(1).isAffichageBoutonModifier());
		assertFalse(result.get(1).isAffichageBoutonSupprimer());
		assertTrue(result.get(1).isAffichageVisa());
		assertFalse(result.get(1).isModifierApprobation());
		assertTrue(result.get(1).isModifierVisa());
		assertNull(result.get(1).getValeurApprobation());
		assertNull(result.get(1).getValeurVisa());

		assertEquals(RefEtatEnum.APPROUVEE.getCodeEtat(), result.get(2).getIdRefEtat().intValue());
		assertEquals(9005132, result.get(2).getIdAgent().intValue());
		assertTrue(result.get(2).isAffichageApprobation());
		assertFalse(result.get(2).isAffichageBoutonAnnuler());
		assertFalse(result.get(2).isAffichageBoutonImprimer());
		assertFalse(result.get(2).isAffichageBoutonModifier());
		assertFalse(result.get(2).isAffichageBoutonSupprimer());
		assertTrue(result.get(2).isAffichageVisa());
		assertFalse(result.get(2).isModifierApprobation());
		assertFalse(result.get(2).isModifierVisa());
		assertNull(result.get(2).getValeurApprobation());
		assertNull(result.get(2).getValeurVisa());

		assertEquals(RefEtatEnum.REFUSEE.getCodeEtat(), result.get(3).getIdRefEtat().intValue());
		assertEquals(9005133, result.get(3).getIdAgent().intValue());
		assertTrue(result.get(3).isAffichageApprobation());
		assertFalse(result.get(3).isAffichageBoutonAnnuler());
		assertFalse(result.get(3).isAffichageBoutonImprimer());
		assertFalse(result.get(3).isAffichageBoutonModifier());
		assertFalse(result.get(3).isAffichageBoutonSupprimer());
		assertTrue(result.get(3).isAffichageVisa());
		assertFalse(result.get(3).isModifierApprobation());
		assertFalse(result.get(3).isModifierVisa());
		assertNull(result.get(3).getValeurApprobation());
		assertNull(result.get(3).getValeurVisa());

		assertEquals(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat(), result.get(4).getIdRefEtat().intValue());
		assertEquals(9005134, result.get(4).getIdAgent().intValue());
		assertTrue(result.get(4).isAffichageApprobation());
		assertFalse(result.get(4).isAffichageBoutonAnnuler());
		assertFalse(result.get(4).isAffichageBoutonImprimer());
		assertFalse(result.get(4).isAffichageBoutonModifier());
		assertFalse(result.get(4).isAffichageBoutonSupprimer());
		assertTrue(result.get(4).isAffichageVisa());
		assertFalse(result.get(4).isModifierApprobation());
		assertTrue(result.get(4).isModifierVisa());
		assertNull(result.get(4).getValeurApprobation());
		assertNull(result.get(4).getValeurVisa());

		assertEquals(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat(), result.get(5).getIdRefEtat().intValue());
		assertEquals(9005135, result.get(5).getIdAgent().intValue());
		assertTrue(result.get(5).isAffichageApprobation());
		assertFalse(result.get(5).isAffichageBoutonAnnuler());
		assertFalse(result.get(5).isAffichageBoutonImprimer());
		assertFalse(result.get(5).isAffichageBoutonModifier());
		assertFalse(result.get(5).isAffichageBoutonSupprimer());
		assertTrue(result.get(5).isAffichageVisa());
		assertFalse(result.get(5).isModifierApprobation());
		assertTrue(result.get(5).isModifierVisa());
		assertNull(result.get(5).getValeurApprobation());
		assertNull(result.get(5).getValeurVisa());
		
		assertEquals(RefEtatEnum.PRISE.getCodeEtat(), result.get(6).getIdRefEtat().intValue());
		assertEquals(9005136, result.get(6).getIdAgent().intValue());
		assertTrue(result.get(6).isAffichageApprobation());
		assertFalse(result.get(6).isAffichageBoutonAnnuler());
		assertFalse(result.get(6).isAffichageBoutonImprimer());
		assertFalse(result.get(6).isAffichageBoutonModifier());
		assertFalse(result.get(6).isAffichageBoutonSupprimer());
		assertTrue(result.get(6).isAffichageVisa());
		assertFalse(result.get(6).isModifierApprobation());
		assertFalse(result.get(6).isModifierVisa());
		assertNull(result.get(6).getValeurApprobation());
		assertNull(result.get(6).getValeurVisa());

		assertEquals(RefEtatEnum.ANNULEE.getCodeEtat(), result.get(7).getIdRefEtat().intValue());
		assertEquals(9005137, result.get(7).getIdAgent().intValue());
		assertTrue(result.get(7).isAffichageApprobation());
		assertFalse(result.get(7).isAffichageBoutonAnnuler());
		assertFalse(result.get(7).isAffichageBoutonImprimer());
		assertFalse(result.get(7).isAffichageBoutonModifier());
		assertFalse(result.get(7).isAffichageBoutonSupprimer());
		assertTrue(result.get(7).isAffichageVisa());
		assertFalse(result.get(7).isModifierApprobation());
		assertFalse(result.get(7).isModifierVisa());
		assertNull(result.get(7).getValeurApprobation());
		assertNull(result.get(7).getValeurVisa());
	}
	
	@Test
	public void filtreDroitOfListeDemandesByDemande_Approbateur() {
		
		Integer idAgentConnecte = 9005138;
		
		// les demandes
		DemandeDto demandeDtoProvisoire = new DemandeDto();
			demandeDtoProvisoire.setIdRefEtat(RefEtatEnum.PROVISOIRE.getCodeEtat());
			demandeDtoProvisoire.setIdAgent(9005130);
		DemandeDto demandeDtoSaisie = new DemandeDto();
			demandeDtoSaisie.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());
			demandeDtoSaisie.setIdAgent(9005131);
		DemandeDto demandeDtoApprouve = new DemandeDto();
			demandeDtoApprouve.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());
			demandeDtoApprouve.setIdAgent(9005132);
		DemandeDto demandeDtoRefusee = new DemandeDto();
			demandeDtoRefusee.setIdRefEtat(RefEtatEnum.REFUSEE.getCodeEtat());
			demandeDtoRefusee.setIdAgent(9005133);
		DemandeDto demandeDtoVisee_F = new DemandeDto();
			demandeDtoVisee_F.setIdRefEtat(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat());
			demandeDtoVisee_F.setIdAgent(9005134);
		DemandeDto demandeDtoVisee_D = new DemandeDto();
			demandeDtoVisee_D.setIdRefEtat(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat());
			demandeDtoVisee_D.setIdAgent(9005135);
		DemandeDto demandeDtoPrise = new DemandeDto();
			demandeDtoPrise.setIdRefEtat(RefEtatEnum.PRISE.getCodeEtat());
			demandeDtoPrise.setIdAgent(9005136);
		DemandeDto demandeDtoAnnulee = new DemandeDto();
			demandeDtoAnnulee.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
			demandeDtoAnnulee.setIdAgent(9005137);
		List<DemandeDto> resultListDto = new ArrayList<DemandeDto>();
			resultListDto.addAll(Arrays.asList(demandeDtoProvisoire, demandeDtoSaisie, demandeDtoApprouve, demandeDtoRefusee, demandeDtoVisee_F, demandeDtoVisee_D, demandeDtoPrise, demandeDtoAnnulee));
		
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
		
		List<DroitsAgent> listDroitAgent = new ArrayList<DroitsAgent>();
		listDroitAgent.addAll(Arrays.asList(da, da1, da2, da3, da4, da5, da6, da7));
		
		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.getListOfAgentsToInputOrApprove(idAgentConnecte, null)).thenReturn(listDroitAgent);
		
		AbsRecuperationDataConsistencyRulesImpl service = new AbsRecuperationDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		
		// When
		List<DemandeDto> result = service.filtreDroitOfListeDemandesByDemande(idAgentConnecte, null, resultListDto);
		
		// Then
		assertEquals(8, result.size());
		
		assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), result.get(0).getIdRefEtat().intValue());
		assertEquals(9005130, result.get(0).getIdAgent().intValue());
		assertTrue(result.get(0).isAffichageApprobation());
		assertFalse(result.get(0).isAffichageBoutonAnnuler());
		assertFalse(result.get(0).isAffichageBoutonImprimer());
		assertFalse(result.get(0).isAffichageBoutonModifier());
		assertFalse(result.get(0).isAffichageBoutonSupprimer());
		assertTrue(result.get(0).isAffichageVisa());
		assertFalse(result.get(0).isModifierApprobation());
		assertFalse(result.get(0).isModifierVisa());
		assertNull(result.get(0).getValeurApprobation());
		assertNull(result.get(0).getValeurVisa());

		assertEquals(RefEtatEnum.SAISIE.getCodeEtat(), result.get(1).getIdRefEtat().intValue());
		assertEquals(9005131, result.get(1).getIdAgent().intValue());
		assertTrue(result.get(1).isAffichageApprobation());
		assertFalse(result.get(1).isAffichageBoutonAnnuler());
		assertFalse(result.get(1).isAffichageBoutonImprimer());
		assertFalse(result.get(1).isAffichageBoutonModifier());
		assertFalse(result.get(1).isAffichageBoutonSupprimer());
		assertTrue(result.get(1).isAffichageVisa());
		assertTrue(result.get(1).isModifierApprobation());
		assertFalse(result.get(1).isModifierVisa());
		assertNull(result.get(1).getValeurApprobation());
		assertNull(result.get(1).getValeurVisa());

		assertEquals(RefEtatEnum.APPROUVEE.getCodeEtat(), result.get(2).getIdRefEtat().intValue());
		assertEquals(9005132, result.get(2).getIdAgent().intValue());
		assertTrue(result.get(2).isAffichageApprobation());
		assertFalse(result.get(2).isAffichageBoutonAnnuler());
		assertFalse(result.get(2).isAffichageBoutonImprimer());
		assertFalse(result.get(2).isAffichageBoutonModifier());
		assertFalse(result.get(2).isAffichageBoutonSupprimer());
		assertTrue(result.get(2).isAffichageVisa());
		assertTrue(result.get(2).isModifierApprobation());
		assertFalse(result.get(2).isModifierVisa());
		assertNull(result.get(2).getValeurApprobation());
		assertNull(result.get(2).getValeurVisa());

		assertEquals(RefEtatEnum.REFUSEE.getCodeEtat(), result.get(3).getIdRefEtat().intValue());
		assertEquals(9005133, result.get(3).getIdAgent().intValue());
		assertTrue(result.get(3).isAffichageApprobation());
		assertFalse(result.get(3).isAffichageBoutonAnnuler());
		assertFalse(result.get(3).isAffichageBoutonImprimer());
		assertFalse(result.get(3).isAffichageBoutonModifier());
		assertFalse(result.get(3).isAffichageBoutonSupprimer());
		assertTrue(result.get(3).isAffichageVisa());
		assertTrue(result.get(3).isModifierApprobation());
		assertFalse(result.get(3).isModifierVisa());
		assertNull(result.get(3).getValeurApprobation());
		assertNull(result.get(3).getValeurVisa());

		assertEquals(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat(), result.get(4).getIdRefEtat().intValue());
		assertEquals(9005134, result.get(4).getIdAgent().intValue());
		assertTrue(result.get(4).isAffichageApprobation());
		assertFalse(result.get(4).isAffichageBoutonAnnuler());
		assertFalse(result.get(4).isAffichageBoutonImprimer());
		assertFalse(result.get(4).isAffichageBoutonModifier());
		assertFalse(result.get(4).isAffichageBoutonSupprimer());
		assertTrue(result.get(4).isAffichageVisa());
		assertTrue(result.get(4).isModifierApprobation());
		assertFalse(result.get(4).isModifierVisa());
		assertNull(result.get(4).getValeurApprobation());
		assertNull(result.get(4).getValeurVisa());

		assertEquals(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat(), result.get(5).getIdRefEtat().intValue());
		assertEquals(9005135, result.get(5).getIdAgent().intValue());
		assertTrue(result.get(5).isAffichageApprobation());
		assertFalse(result.get(5).isAffichageBoutonAnnuler());
		assertFalse(result.get(5).isAffichageBoutonImprimer());
		assertFalse(result.get(5).isAffichageBoutonModifier());
		assertFalse(result.get(5).isAffichageBoutonSupprimer());
		assertTrue(result.get(5).isAffichageVisa());
		assertTrue(result.get(5).isModifierApprobation());
		assertFalse(result.get(5).isModifierVisa());
		assertNull(result.get(5).getValeurApprobation());
		assertNull(result.get(5).getValeurVisa());
		
		assertEquals(RefEtatEnum.PRISE.getCodeEtat(), result.get(6).getIdRefEtat().intValue());
		assertEquals(9005136, result.get(6).getIdAgent().intValue());
		assertTrue(result.get(6).isAffichageApprobation());
		assertFalse(result.get(6).isAffichageBoutonAnnuler());
		assertFalse(result.get(6).isAffichageBoutonImprimer());
		assertFalse(result.get(6).isAffichageBoutonModifier());
		assertFalse(result.get(6).isAffichageBoutonSupprimer());
		assertTrue(result.get(6).isAffichageVisa());
		assertFalse(result.get(6).isModifierApprobation());
		assertFalse(result.get(6).isModifierVisa());
		assertNull(result.get(6).getValeurApprobation());
		assertNull(result.get(6).getValeurVisa());

		assertEquals(RefEtatEnum.ANNULEE.getCodeEtat(), result.get(7).getIdRefEtat().intValue());
		assertEquals(9005137, result.get(7).getIdAgent().intValue());
		assertTrue(result.get(7).isAffichageApprobation());
		assertFalse(result.get(7).isAffichageBoutonAnnuler());
		assertFalse(result.get(7).isAffichageBoutonImprimer());
		assertFalse(result.get(7).isAffichageBoutonModifier());
		assertFalse(result.get(7).isAffichageBoutonSupprimer());
		assertTrue(result.get(7).isAffichageVisa());
		assertFalse(result.get(7).isModifierApprobation());
		assertFalse(result.get(7).isModifierVisa());
		assertNull(result.get(7).getValeurApprobation());
		assertNull(result.get(7).getValeurVisa());
	}
	
	@Test
	public void filtreDroitOfListeDemandesByDemande_Delegataire() {
		
		Integer idAgentConnecte = 9005138;
		
		// les demandes
		DemandeDto demandeDtoProvisoire = new DemandeDto();
			demandeDtoProvisoire.setIdRefEtat(RefEtatEnum.PROVISOIRE.getCodeEtat());
			demandeDtoProvisoire.setIdAgent(9005130);
		DemandeDto demandeDtoSaisie = new DemandeDto();
			demandeDtoSaisie.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());
			demandeDtoSaisie.setIdAgent(9005131);
		DemandeDto demandeDtoApprouve = new DemandeDto();
			demandeDtoApprouve.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());
			demandeDtoApprouve.setIdAgent(9005132);
		DemandeDto demandeDtoRefusee = new DemandeDto();
			demandeDtoRefusee.setIdRefEtat(RefEtatEnum.REFUSEE.getCodeEtat());
			demandeDtoRefusee.setIdAgent(9005133);
		DemandeDto demandeDtoVisee_F = new DemandeDto();
			demandeDtoVisee_F.setIdRefEtat(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat());
			demandeDtoVisee_F.setIdAgent(9005134);
		DemandeDto demandeDtoVisee_D = new DemandeDto();
			demandeDtoVisee_D.setIdRefEtat(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat());
			demandeDtoVisee_D.setIdAgent(9005135);
		DemandeDto demandeDtoPrise = new DemandeDto();
			demandeDtoPrise.setIdRefEtat(RefEtatEnum.PRISE.getCodeEtat());
			demandeDtoPrise.setIdAgent(9005136);
		DemandeDto demandeDtoAnnulee = new DemandeDto();
			demandeDtoAnnulee.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
			demandeDtoAnnulee.setIdAgent(9005137);
		List<DemandeDto> resultListDto = new ArrayList<DemandeDto>();
			resultListDto.addAll(Arrays.asList(demandeDtoProvisoire, demandeDtoSaisie, demandeDtoApprouve, demandeDtoRefusee, demandeDtoVisee_F, demandeDtoVisee_D, demandeDtoPrise, demandeDtoAnnulee));
		
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
		
		List<DroitsAgent> listDroitAgent = new ArrayList<DroitsAgent>();
		listDroitAgent.addAll(Arrays.asList(da, da1, da2, da3, da4, da5, da6, da7));
		
		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.getListOfAgentsToInputOrApprove(idAgentConnecte, null)).thenReturn(listDroitAgent);
		
		AbsRecuperationDataConsistencyRulesImpl service = new AbsRecuperationDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		
		// When
		List<DemandeDto> result = service.filtreDroitOfListeDemandesByDemande(idAgentConnecte, null, resultListDto);
		
		// Then
		assertEquals(8, result.size());
		
		assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), result.get(0).getIdRefEtat().intValue());
		assertEquals(9005130, result.get(0).getIdAgent().intValue());
		assertTrue(result.get(0).isAffichageApprobation());
		assertFalse(result.get(0).isAffichageBoutonAnnuler());
		assertFalse(result.get(0).isAffichageBoutonImprimer());
		assertFalse(result.get(0).isAffichageBoutonModifier());
		assertFalse(result.get(0).isAffichageBoutonSupprimer());
		assertTrue(result.get(0).isAffichageVisa());
		assertFalse(result.get(0).isModifierApprobation());
		assertFalse(result.get(0).isModifierVisa());
		assertNull(result.get(0).getValeurApprobation());
		assertNull(result.get(0).getValeurVisa());

		assertEquals(RefEtatEnum.SAISIE.getCodeEtat(), result.get(1).getIdRefEtat().intValue());
		assertEquals(9005131, result.get(1).getIdAgent().intValue());
		assertTrue(result.get(1).isAffichageApprobation());
		assertFalse(result.get(1).isAffichageBoutonAnnuler());
		assertFalse(result.get(1).isAffichageBoutonImprimer());
		assertFalse(result.get(1).isAffichageBoutonModifier());
		assertFalse(result.get(1).isAffichageBoutonSupprimer());
		assertTrue(result.get(1).isAffichageVisa());
		assertTrue(result.get(1).isModifierApprobation());
		assertFalse(result.get(1).isModifierVisa());
		assertNull(result.get(1).getValeurApprobation());
		assertNull(result.get(1).getValeurVisa());

		assertEquals(RefEtatEnum.APPROUVEE.getCodeEtat(), result.get(2).getIdRefEtat().intValue());
		assertEquals(9005132, result.get(2).getIdAgent().intValue());
		assertTrue(result.get(2).isAffichageApprobation());
		assertFalse(result.get(2).isAffichageBoutonAnnuler());
		assertFalse(result.get(2).isAffichageBoutonImprimer());
		assertFalse(result.get(2).isAffichageBoutonModifier());
		assertFalse(result.get(2).isAffichageBoutonSupprimer());
		assertTrue(result.get(2).isAffichageVisa());
		assertTrue(result.get(2).isModifierApprobation());
		assertFalse(result.get(2).isModifierVisa());
		assertNull(result.get(2).getValeurApprobation());
		assertNull(result.get(2).getValeurVisa());

		assertEquals(RefEtatEnum.REFUSEE.getCodeEtat(), result.get(3).getIdRefEtat().intValue());
		assertEquals(9005133, result.get(3).getIdAgent().intValue());
		assertTrue(result.get(3).isAffichageApprobation());
		assertFalse(result.get(3).isAffichageBoutonAnnuler());
		assertFalse(result.get(3).isAffichageBoutonImprimer());
		assertFalse(result.get(3).isAffichageBoutonModifier());
		assertFalse(result.get(3).isAffichageBoutonSupprimer());
		assertTrue(result.get(3).isAffichageVisa());
		assertTrue(result.get(3).isModifierApprobation());
		assertFalse(result.get(3).isModifierVisa());
		assertNull(result.get(3).getValeurApprobation());
		assertNull(result.get(3).getValeurVisa());

		assertEquals(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat(), result.get(4).getIdRefEtat().intValue());
		assertEquals(9005134, result.get(4).getIdAgent().intValue());
		assertTrue(result.get(4).isAffichageApprobation());
		assertFalse(result.get(4).isAffichageBoutonAnnuler());
		assertFalse(result.get(4).isAffichageBoutonImprimer());
		assertFalse(result.get(4).isAffichageBoutonModifier());
		assertFalse(result.get(4).isAffichageBoutonSupprimer());
		assertTrue(result.get(4).isAffichageVisa());
		assertTrue(result.get(4).isModifierApprobation());
		assertFalse(result.get(4).isModifierVisa());
		assertNull(result.get(4).getValeurApprobation());
		assertNull(result.get(4).getValeurVisa());

		assertEquals(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat(), result.get(5).getIdRefEtat().intValue());
		assertEquals(9005135, result.get(5).getIdAgent().intValue());
		assertTrue(result.get(5).isAffichageApprobation());
		assertFalse(result.get(5).isAffichageBoutonAnnuler());
		assertFalse(result.get(5).isAffichageBoutonImprimer());
		assertFalse(result.get(5).isAffichageBoutonModifier());
		assertFalse(result.get(5).isAffichageBoutonSupprimer());
		assertTrue(result.get(5).isAffichageVisa());
		assertTrue(result.get(5).isModifierApprobation());
		assertFalse(result.get(5).isModifierVisa());
		assertNull(result.get(5).getValeurApprobation());
		assertNull(result.get(5).getValeurVisa());
		
		assertEquals(RefEtatEnum.PRISE.getCodeEtat(), result.get(6).getIdRefEtat().intValue());
		assertEquals(9005136, result.get(6).getIdAgent().intValue());
		assertTrue(result.get(6).isAffichageApprobation());
		assertFalse(result.get(6).isAffichageBoutonAnnuler());
		assertFalse(result.get(6).isAffichageBoutonImprimer());
		assertFalse(result.get(6).isAffichageBoutonModifier());
		assertFalse(result.get(6).isAffichageBoutonSupprimer());
		assertTrue(result.get(6).isAffichageVisa());
		assertFalse(result.get(6).isModifierApprobation());
		assertFalse(result.get(6).isModifierVisa());
		assertNull(result.get(6).getValeurApprobation());
		assertNull(result.get(6).getValeurVisa());

		assertEquals(RefEtatEnum.ANNULEE.getCodeEtat(), result.get(7).getIdRefEtat().intValue());
		assertEquals(9005137, result.get(7).getIdAgent().intValue());
		assertTrue(result.get(7).isAffichageApprobation());
		assertFalse(result.get(7).isAffichageBoutonAnnuler());
		assertFalse(result.get(7).isAffichageBoutonImprimer());
		assertFalse(result.get(7).isAffichageBoutonModifier());
		assertFalse(result.get(7).isAffichageBoutonSupprimer());
		assertTrue(result.get(7).isAffichageVisa());
		assertFalse(result.get(7).isModifierApprobation());
		assertFalse(result.get(7).isModifierVisa());
		assertNull(result.get(7).getValeurApprobation());
		assertNull(result.get(7).getValeurVisa());
	}
}
