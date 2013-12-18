package nc.noumea.mairie.abs.service.impl;

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
import nc.noumea.mairie.abs.domain.Droit;
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
import nc.noumea.mairie.abs.repository.IDemandeRepository;
import nc.noumea.mairie.abs.service.IAbsenceDataConsistencyRules;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.mock.staticmock.MockStaticEntityMethods;
import org.springframework.test.util.ReflectionTestUtils;

@MockStaticEntityMethods
public class AbsenceServiceTest {

	@Test
	public void verifAccessRightSaveDemande_AgentNotOperateur() {

		// Given
		ReturnMessageDto returnDto = new ReturnMessageDto();
		Integer idAgent = 9006543;

		DemandeDto dto = new DemandeDto();
		dto.setIdAgent(9005138);

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.isUserOperateur(idAgent)).thenReturn(false);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// When
		service.verifAccessRightSaveDemande(idAgent, dto, returnDto);

		// Then
		assertEquals(1, returnDto.getErrors().size());
		assertEquals("Vous n'êtes pas opérateur. Vous ne pouvez pas saisir de demandes.", returnDto.getErrors().get(0));
	}

	@Test
	public void verifAccessRightSaveDemande_Operateur_NotAgentOfOperateur() {

		// Given
		ReturnMessageDto returnDto = new ReturnMessageDto();
		Integer idAgent = 9006543;

		DemandeDto dto = new DemandeDto();
		dto.setIdAgent(9005138);

		Profil p = new Profil();
		p.setLibelle(ProfilEnum.OPERATEUR.toString());

		DroitProfil dpOpe = new DroitProfil();
		dpOpe.setDroitApprobateur(new Droit());
		dpOpe.setProfil(p);
		Set<DroitProfil> setDroitProfil = new HashSet<DroitProfil>();
		setDroitProfil.add(dpOpe);

		DroitsAgent da = new DroitsAgent();
		da.setIdAgent(9005131);

		DroitDroitsAgent dda = new DroitDroitsAgent();
		dda.setDroitProfil(dpOpe);
		dda.setDroitsAgent(da);
		Set<DroitDroitsAgent> setDroitDroitsAgent = new HashSet<DroitDroitsAgent>();
		setDroitDroitsAgent.add(dda);

		Droit d = new Droit();
		d.setIdAgent(idAgent);
		d.setIdDroit(1);
		d.setDroitProfils(setDroitProfil);
		d.setDroitDroitsAgent(setDroitDroitsAgent);

		dpOpe.setDroit(d);
		dda.setDroit(d);

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.isUserOperateur(idAgent)).thenReturn(true);
		Mockito.when(arRepo.getAgentDroitFetchAgents(idAgent)).thenReturn(d);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// When
		service.verifAccessRightSaveDemande(idAgent, dto, returnDto);

		// Then
		assertEquals(1, returnDto.getErrors().size());
		assertEquals("Vous n'êtes pas opérateur de l'agent 9005138. Vous ne pouvez pas saisir de demandes.", returnDto
				.getErrors().get(0));
	}

	@Test
	public void verifAccessRightSaveDemande_AllOk() {

		// Given
		ReturnMessageDto returnDto = new ReturnMessageDto();
		Integer idAgent = 9006543;

		DemandeDto dto = new DemandeDto();
		dto.setIdAgent(9005138);

		Profil p = new Profil();
		p.setLibelle(ProfilEnum.OPERATEUR.toString());

		DroitProfil dpOpe = new DroitProfil();
		dpOpe.setDroitApprobateur(new Droit());
		dpOpe.setProfil(p);
		Set<DroitProfil> setDroitProfil = new HashSet<DroitProfil>();
		setDroitProfil.add(dpOpe);

		DroitsAgent da = new DroitsAgent();
		da.setIdAgent(9005138);

		DroitDroitsAgent dda = new DroitDroitsAgent();
		dda.setDroitProfil(dpOpe);
		dda.setDroitsAgent(da);
		Set<DroitDroitsAgent> setDroitDroitsAgent = new HashSet<DroitDroitsAgent>();
		setDroitDroitsAgent.add(dda);

		Droit d = new Droit();
		d.setIdAgent(idAgent);
		d.setIdDroit(1);
		d.setDroitProfils(setDroitProfil);
		d.setDroitDroitsAgent(setDroitDroitsAgent);

		dpOpe.setDroit(d);
		dda.setDroit(d);

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.isUserOperateur(idAgent)).thenReturn(true);
		Mockito.when(arRepo.getAgentDroitFetchAgents(idAgent)).thenReturn(d);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// When
		service.verifAccessRightSaveDemande(idAgent, dto, returnDto);

		// Then
		assertEquals(0, returnDto.getErrors().size());
	}

	@Test
	public void getListeDemandesAgent_WrongParam() {

		// Given

		AbsenceService service = new AbsenceService();

		// When
		List<DemandeDto> result = service.getListeDemandesAgent(9005138, "TEST", new Date(), new Date(), new Date(),
				null, null);

		// Then
		assertEquals(0, result.size());
	}

	@Test
	public void getListeDemandesAgent_ToutesWithEtatAndDAteDemandeFilter_NoResult() {

		// Given
		Integer idAgent = 9005138;
		
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
		Mockito.when(emMock.find(RefEtat.class, RefEtatEnum.PRISE.getCodeEtat())).thenReturn(etatPris);

		IDemandeRepository demRepo = Mockito.mock(IDemandeRepository.class);
		Mockito.when(
				demRepo.listeDemandesAgent(Mockito.eq(idAgent), Mockito.isA(Date.class), Mockito.isA(Date.class),
						Mockito.eq(3))).thenReturn(listeDemande);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demRepo);
		ReflectionTestUtils.setField(service, "absEntityManager", emMock);

		// When
		List<DemandeDto> result = service.getListeDemandesAgent(idAgent, "TOUTES", new Date(), new Date(), new Date(),
				6, 3);

		// Then
		assertEquals(0, result.size());
	}

	@Test
	public void getListeDemandesAgent_ToutesWithEtatAndDAteDemandeFilter() {

		// Given
		Integer idAgent = 9005138;
		
		RefEtat etatProvisoire = new RefEtat();
		etatProvisoire.setIdRefEtat(0);
		etatProvisoire.setLabel("PROVISOIRE");

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

		IDemandeRepository demRepo = Mockito.mock(IDemandeRepository.class);
		Mockito.when(
				demRepo.listeDemandesAgent(Mockito.eq(idAgent), Mockito.isA(Date.class), Mockito.isA(Date.class),
						Mockito.eq(3))).thenReturn(listeDemande);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demRepo);
		ReflectionTestUtils.setField(service, "absEntityManager", emMock);

		// When
		List<DemandeDto> result = service.getListeDemandesAgent(idAgent, "TOUTES", new Date(), new Date(), new Date(),
				0, 3);

		// Then
		assertEquals(1, result.size());
		assertEquals("50", result.get(0).getDuree().toString());
	}

	@Test
	public void getListeDemandesAgent_ToutesWithEtatFilter() {

		// Given
		Integer idAgent = 9005138;
		
		RefEtat etatProvisoire = new RefEtat();
		etatProvisoire.setIdRefEtat(0);
		etatProvisoire.setLabel("PROVISOIRE");

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

		IDemandeRepository demRepo = Mockito.mock(IDemandeRepository.class);
		Mockito.when(
				demRepo.listeDemandesAgent(Mockito.eq(idAgent), Mockito.isA(Date.class), Mockito.isA(Date.class),
						Mockito.eq(3))).thenReturn(listeDemande);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demRepo);
		ReflectionTestUtils.setField(service, "absEntityManager", emMock);

		// When
		List<DemandeDto> result = service.getListeDemandesAgent(idAgent, "TOUTES", new Date(), new Date(), null,
				0, 3);

		// Then
		assertEquals(1, result.size());
		assertEquals("50", result.get(0).getDuree().toString());
	}

	@Test
	public void getListeDemandesAgent_ToutesWithNoFilter() {

		// Given
		Integer idAgent = 9005138;
		
		RefEtat etatProvisoire = new RefEtat();
		etatProvisoire.setIdRefEtat(0);
		etatProvisoire.setLabel("PROVISOIRE");

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

		IDemandeRepository demRepo = Mockito.mock(IDemandeRepository.class);
		Mockito.when(
				demRepo.listeDemandesAgent(Mockito.eq(idAgent), Mockito.isA(Date.class), Mockito.isA(Date.class),
						Mockito.eq(3))).thenReturn(listeDemande);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demRepo);
		ReflectionTestUtils.setField(service, "absEntityManager", emMock);

		// When
		List<DemandeDto> result = service.getListeDemandesAgent(idAgent, "TOUTES", new Date(), new Date(), null,
				null, 3);

		// Then
		assertEquals(2, result.size());
		assertEquals("30", result.get(0).getDuree().toString());
		assertEquals("50", result.get(1).getDuree().toString());
	}

	@Test
	public void getListeDemandesAgent_DemandesEnCours() {

		// Given
		Integer idAgent = 9005138;
		RefEtat etatProvisoire = new RefEtat();
		etatProvisoire.setIdRefEtat(0);
		etatProvisoire.setLabel("PROVISOIRE");

		List<RefEtat> refEtatEnCours = new ArrayList<RefEtat>();
		RefEtat etatSaisie = new RefEtat();
		etatSaisie.setIdRefEtat(1);
		etatSaisie.setLabel("SAISIE");
		refEtatEnCours.add(etatSaisie);

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

		IDemandeRepository demRepo = Mockito.mock(IDemandeRepository.class);
		Mockito.when(
				demRepo.listeDemandesAgent(Mockito.eq(idAgent), Mockito.isA(Date.class), Mockito.isA(Date.class),
						Mockito.eq(3))).thenReturn(listeDemande);
		Mockito.when(demRepo.findRefEtatEnCours()).thenReturn(refEtatEnCours);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demRepo);
		ReflectionTestUtils.setField(service, "absEntityManager", emMock);

		// When
		List<DemandeDto> result = service.getListeDemandesAgent(idAgent, "EN_COURS", new Date(), new Date(), null,
				null, 3);

		// Then
		assertEquals(1, result.size());
		assertEquals("30", result.get(0).getDuree().toString());
	}

	@Test
	public void getListeDemandesAgent_DemandesNonPrises() {

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

		IDemandeRepository demRepo = Mockito.mock(IDemandeRepository.class);
		Mockito.when(
				demRepo.listeDemandesAgent(Mockito.eq(idAgent), Mockito.isA(Date.class), Mockito.isA(Date.class),
						Mockito.eq(3))).thenReturn(listeDemande);
		Mockito.when(demRepo.findRefEtatNonPris()).thenReturn(refEtatNonPris);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demRepo);
		ReflectionTestUtils.setField(service, "absEntityManager", emMock);

		// When
		List<DemandeDto> result = service.getListeDemandesAgent(idAgent, "NON_PRISES", new Date(), new Date(),
				new Date(), null, 3);

		// Then
		assertEquals(1, result.size());
		assertEquals("30", result.get(0).getDuree().toString());
	}

	@Test
	public void getDemande_Recup_WithResult_isEtatDefinitif() {

		Date dateDebut = new Date();
		Date dateFin = new Date();
		Date dateMaj = new Date();
		Date dateMaj2 = new Date();
		Integer idDemande = 1;
		Integer idTypeDemande = RefTypeAbsenceEnum.RECUP.getValue();

		RefTypeAbsence rta = new RefTypeAbsence();
		rta.setIdRefTypeAbsence(3);

		DemandeRecup dr = new DemandeRecup();

		List<EtatDemande> listEtatDemande = new ArrayList<EtatDemande>();
		EtatDemande ed = new EtatDemande();
		ed.setDate(dateMaj);
		ed.setDemande((Demande) dr);
		ed.setEtat(RefEtatEnum.PROVISOIRE);
		ed.setIdAgent(9005138);
		ed.setIdEtatDemande(1);
		EtatDemande ed2 = new EtatDemande();
		ed2.setDate(dateMaj2);
		ed2.setDemande((Demande) dr);
		ed2.setEtat(RefEtatEnum.SAISIE);
		ed2.setIdAgent(9005138);
		ed2.setIdEtatDemande(2);
		listEtatDemande.addAll(Arrays.asList(ed2, ed));

		dr.setDateDebut(dateDebut);
		dr.setDateFin(dateFin);
		dr.setDuree(10);
		dr.setEtatsDemande(listEtatDemande);
		dr.setIdAgent(9005138);
		dr.setIdDemande(idDemande);
		dr.setType(rta);

		IDemandeRepository demandeRepo = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepo.getEntity(DemandeRecup.class, idDemande)).thenReturn(dr);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepo);

		// When
		DemandeDto result = service.getDemande(idDemande, idTypeDemande);

		// Then
		assertEquals(result.getDateDebut(), dateDebut);
		assertEquals(result.getDuree(), dr.getDuree());
		assertEquals(result.getIdAgent(), new Integer(9005138));
		assertEquals(result.getIdDemande(), idDemande);
		assertEquals(result.getIdRefEtat().intValue(), RefEtatEnum.SAISIE.getCodeEtat());
		assertEquals(result.getIdTypeDemande().intValue(), RefTypeAbsenceEnum.RECUP.getValue());
		assertTrue(result.isEtatDefinitif());
	}

	@Test
	public void getDemande_Recup_WithResult_isNoEtatDefinitif() {

		Date dateDebut = new Date();
		Date dateFin = new Date();
		Date dateMaj = new Date();
		Date dateMaj2 = new Date();
		Integer idDemande = 1;
		Integer idTypeDemande = RefTypeAbsenceEnum.RECUP.getValue();

		RefTypeAbsence rta = new RefTypeAbsence();
		rta.setIdRefTypeAbsence(3);

		DemandeRecup dr = new DemandeRecup();

		List<EtatDemande> listEtatDemande = new ArrayList<EtatDemande>();
		EtatDemande ed = new EtatDemande();
		ed.setDate(dateMaj);
		ed.setDemande((Demande) dr);
		ed.setEtat(RefEtatEnum.SAISIE);
		ed.setIdAgent(9005138);
		ed.setIdEtatDemande(1);
		EtatDemande ed2 = new EtatDemande();
		ed2.setDate(dateMaj2);
		ed2.setDemande((Demande) dr);
		ed2.setEtat(RefEtatEnum.APPROUVEE);
		ed2.setIdAgent(9005138);
		ed2.setIdEtatDemande(2);
		listEtatDemande.addAll(Arrays.asList(ed2, ed));

		dr.setDateDebut(dateDebut);
		dr.setDateFin(dateFin);
		dr.setDuree(10);
		dr.setEtatsDemande(listEtatDemande);
		dr.setIdAgent(9005138);
		dr.setIdDemande(idDemande);
		dr.setType(rta);

		IDemandeRepository demandeRepo = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepo.getEntity(DemandeRecup.class, idDemande)).thenReturn(dr);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepo);

		// When
		DemandeDto result = service.getDemande(idDemande, idTypeDemande);

		// Then
		assertEquals(result.getDateDebut(), dateDebut);
		assertEquals(result.getDuree(), dr.getDuree());
		assertEquals(result.getIdAgent(), new Integer(9005138));
		assertEquals(result.getIdDemande(), idDemande);
		assertEquals(result.getIdRefEtat().intValue(), RefEtatEnum.APPROUVEE.getCodeEtat());
		assertEquals(result.getIdTypeDemande().intValue(), RefTypeAbsenceEnum.RECUP.getValue());
		assertFalse(result.isEtatDefinitif());
	}

	@Test
	public void getDemande_Recup_WithNoResult_OtherType() {

		Date dateDebut = new Date();
		Date dateFin = new Date();
		Date dateMaj = new Date();
		Date dateMaj2 = new Date();
		Integer idDemande = 1;

		RefTypeAbsence rta = new RefTypeAbsence();
		rta.setIdRefTypeAbsence(3);

		DemandeRecup dr = new DemandeRecup();

		List<EtatDemande> listEtatDemande = new ArrayList<EtatDemande>();
		EtatDemande ed = new EtatDemande();
		ed.setDate(dateMaj);
		ed.setDemande((Demande) dr);
		ed.setEtat(RefEtatEnum.SAISIE);
		ed.setIdAgent(9005138);
		ed.setIdEtatDemande(1);
		EtatDemande ed2 = new EtatDemande();
		ed2.setDate(dateMaj2);
		ed2.setDemande((Demande) dr);
		ed2.setEtat(RefEtatEnum.APPROUVEE);
		ed2.setIdAgent(9005138);
		ed2.setIdEtatDemande(2);
		listEtatDemande.addAll(Arrays.asList(ed2, ed));

		dr.setDateDebut(dateDebut);
		dr.setDateFin(dateFin);
		dr.setDuree(10);
		dr.setEtatsDemande(listEtatDemande);
		dr.setIdAgent(9005138);
		dr.setIdDemande(idDemande);
		dr.setType(rta);

		IDemandeRepository demandeRepo = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepo.getEntity(DemandeRecup.class, idDemande)).thenReturn(dr);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepo);

		// When
		DemandeDto result = service.getDemande(idDemande, RefTypeAbsenceEnum.MALADIES.getValue());

		// Then
		assertNull(result);
	}

	@Test
	public void saveDemande_OK_avecEtatProvisoire() {

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		dto.setIdAgent(idAgent);

		Droit droitOperateur = new Droit();
		DroitDroitsAgent droitDroitAgent = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(idAgent);
		droitDroitAgent.setDroitsAgent(droitsAgent);
		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.add(droitDroitAgent);
		droitOperateur.setDroitDroitsAgent(droitDroitsAgent);
		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isUserOperateur(idAgent)).thenReturn(true);
		Mockito.when(accessRightsRepository.getAgentDroitFetchAgents(idAgent)).thenReturn(droitOperateur);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(null);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeRecup obj = (DemandeRecup) args[0];

				assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(10, obj.getDuree().intValue());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(Demande.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getDateFin(dto.getDateDebut(), dto.getDuree())).thenReturn(dateFin);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemandeRecup(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeRecup.class), Mockito.isA(Date.class));

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absDataConsistencyRules", absDataConsistencyRules);

		result = service.saveDemande(idAgent, dto);

		assertEquals(0, result.getErrors().size());
	}

	@Test
	public void saveDemande_OK_avecEtatSaisie() {

		ReturnMessageDto result = new ReturnMessageDto();

		Integer idAgent = 9005138;
		Date dateDebut = new Date();
		Date dateFin = new Date();

		DemandeDto dto = new DemandeDto();
		dto.setIdDemande(1);
		dto.setDateDebut(dateDebut);
		dto.setDuree(10);
		dto.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		dto.setIdAgent(idAgent);
		dto.setEtatDefinitif(true);

		Droit droitOperateur = new Droit();
		DroitDroitsAgent droitDroitAgent = new DroitDroitsAgent();
		DroitsAgent droitsAgent = new DroitsAgent();
		droitsAgent.setIdAgent(idAgent);
		droitDroitAgent.setDroitsAgent(droitsAgent);
		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.add(droitDroitAgent);
		droitOperateur.setDroitDroitsAgent(droitDroitsAgent);
		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isUserOperateur(idAgent)).thenReturn(true);
		Mockito.when(accessRightsRepository.getAgentDroitFetchAgents(idAgent)).thenReturn(droitOperateur);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, dto.getIdDemande())).thenReturn(null);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				DemandeRecup obj = (DemandeRecup) args[0];

				assertEquals(RefEtatEnum.SAISIE.getCodeEtat(), obj.getEtatsDemande().get(0).getEtat().getCodeEtat());
				assertEquals(9005138, obj.getIdAgent().intValue());
				assertEquals(10, obj.getDuree().intValue());

				return true;
			}
		}).when(demandeRepository).persistEntity(Mockito.isA(Demande.class));

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getDateFin(dto.getDateDebut(), dto.getDuree())).thenReturn(dateFin);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		})
				.when(absDataConsistencyRules)
				.processDataConsistencyDemandeRecup(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class),
						Mockito.isA(DemandeRecup.class), Mockito.isA(Date.class));

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", accessRightsRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "absDataConsistencyRules", absDataConsistencyRules);

		result = service.saveDemande(idAgent, dto);

		assertEquals(0, result.getErrors().size());
	}
}
