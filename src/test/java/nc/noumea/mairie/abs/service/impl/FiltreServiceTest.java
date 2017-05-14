package nc.noumea.mairie.abs.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.RefEtat;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefGroupeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.domain.RefTypeAccidentTravail;
import nc.noumea.mairie.abs.domain.RefTypeGroupeAbsenceEnum;
import nc.noumea.mairie.abs.domain.RefTypeMaladiePro;
import nc.noumea.mairie.abs.domain.RefTypeSaisi;
import nc.noumea.mairie.abs.domain.RefTypeSiegeLesion;
import nc.noumea.mairie.abs.domain.RefUnitePeriodeQuota;
import nc.noumea.mairie.abs.dto.RefEtatDto;
import nc.noumea.mairie.abs.dto.RefGroupeAbsenceDto;
import nc.noumea.mairie.abs.dto.RefTypeAbsenceDto;
import nc.noumea.mairie.abs.dto.RefTypeDto;
import nc.noumea.mairie.abs.dto.RefTypeSaisiDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.dto.UnitePeriodeQuotaDto;
import nc.noumea.mairie.abs.repository.IFiltreRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.abs.service.IAgentMatriculeConverterService;
import nc.noumea.mairie.domain.Spcarr;
import nc.noumea.mairie.ws.ISirhWSConsumer;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class FiltreServiceTest {

	@Test
	public void getRefTypesAbsence_noIdAgent() {

		RefTypeAbsence ASA_A49 = new RefTypeAbsence();
		ASA_A49.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A49.getValue());
		RefTypeAbsence ASA_A50 = new RefTypeAbsence();
		ASA_A50.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A50.getValue());
		RefTypeAbsence ASA_A52 = new RefTypeAbsence();
		ASA_A52.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A52.getValue());
		RefTypeAbsence ASA_A53 = new RefTypeAbsence();
		ASA_A53.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A53.getValue());
		RefTypeAbsence ASA_A55 = new RefTypeAbsence();
		ASA_A55.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A55.getValue());
		RefTypeAbsence ASA_A54 = new RefTypeAbsence();
		ASA_A54.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A54.getValue());
		RefTypeAbsence ASA_A48 = new RefTypeAbsence();
		ASA_A48.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A48.getValue());
		RefTypeAbsence CONGE_ANNUEL = new RefTypeAbsence();
		CONGE_ANNUEL.setIdRefTypeAbsence(RefTypeAbsenceEnum.CONGE_ANNUEL.getValue());
		RefTypeAbsence MALADIES = new RefTypeAbsence();
		MALADIES.setIdRefTypeAbsence(RefTypeAbsenceEnum.MALADIE.getValue());
		RefTypeAbsence RECUP = new RefTypeAbsence();
		RECUP.setIdRefTypeAbsence(RefTypeAbsenceEnum.RECUP.getValue());
		RefTypeAbsence REPOS_COMP = new RefTypeAbsence();
		REPOS_COMP.setIdRefTypeAbsence(RefTypeAbsenceEnum.REPOS_COMP.getValue());

		List<RefTypeAbsence> refTypeAbs = new ArrayList<RefTypeAbsence>();
		refTypeAbs.addAll(Arrays.asList(ASA_A49, ASA_A50, ASA_A52, ASA_A53, ASA_A55, ASA_A54, ASA_A48, CONGE_ANNUEL,
				MALADIES, RECUP, REPOS_COMP));

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findAllRefTypeAbsences()).thenReturn(refTypeAbs);

		FiltreService service = new FiltreService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);

		List<RefTypeAbsenceDto> result = service.getRefTypesAbsence(null);

		assertEquals(0, result.size());
	}

	@Test
	public void getRefTypesAbsence_Fonctionnaire() {

		RefTypeSaisi typeSaisiFonctionnaire = new RefTypeSaisi();
		typeSaisiFonctionnaire.setFonctionnaire(true);
		typeSaisiFonctionnaire.setContractuel(false);
		typeSaisiFonctionnaire.setConventionCollective(false);

		RefTypeAbsence typeAbsenceFonctionnaire = new RefTypeAbsence();
		typeAbsenceFonctionnaire.setLabel("fonctionnaire");
		typeAbsenceFonctionnaire.setTypeSaisi(typeSaisiFonctionnaire);

		RefTypeSaisi typeSaisiContractuel = new RefTypeSaisi();
		typeSaisiContractuel.setFonctionnaire(false);
		typeSaisiContractuel.setContractuel(true);
		typeSaisiContractuel.setConventionCollective(false);

		RefTypeAbsence typeAbsenceContractuel = new RefTypeAbsence();
		typeAbsenceContractuel.setLabel("contractuel");
		typeAbsenceContractuel.setTypeSaisi(typeSaisiContractuel);

		RefTypeSaisi typeSaisiConventionCollective = new RefTypeSaisi();
		typeSaisiConventionCollective.setFonctionnaire(false);
		typeSaisiConventionCollective.setContractuel(false);
		typeSaisiConventionCollective.setConventionCollective(true);

		RefTypeAbsence typeAbsenceConventionCollective = new RefTypeAbsence();
		typeAbsenceConventionCollective.setLabel("conventionCollective");
		typeAbsenceConventionCollective.setTypeSaisi(typeSaisiConventionCollective);

		List<RefTypeAbsence> refTypeAbs = new ArrayList<RefTypeAbsence>();
		refTypeAbs.addAll(Arrays.asList(typeAbsenceFonctionnaire, typeAbsenceContractuel,
				typeAbsenceConventionCollective));

		Spcarr carr = new Spcarr();
		carr.setCdcate(4);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findAllRefTypeAbsences()).thenReturn(refTypeAbs);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());
		Mockito.when(helperService.isFonctionnaire(carr)).thenReturn(true);
		Mockito.when(helperService.isContractuel(carr)).thenReturn(false);
		Mockito.when(helperService.isConventionCollective(carr)).thenReturn(false);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(5138, helperService.getCurrentDate())).thenReturn(carr);

		IAgentMatriculeConverterService agentMatriculeServ = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeServ.fromIdAgentToSIRHNomatrAgent(9005138)).thenReturn(5138);

		FiltreService service = new FiltreService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeServ);

		List<RefTypeAbsenceDto> result = service.getRefTypesAbsence(9005138);

		assertEquals(1, result.size());
		assertEquals("fonctionnaire", result.get(0).getLibelle());
	}

	@Test
	public void getRefTypesAbsence_CC() {

		RefTypeSaisi typeSaisiFonctionnaire = new RefTypeSaisi();
		typeSaisiFonctionnaire.setFonctionnaire(true);
		typeSaisiFonctionnaire.setContractuel(false);
		typeSaisiFonctionnaire.setConventionCollective(false);

		RefTypeAbsence typeAbsenceFonctionnaire = new RefTypeAbsence();
		typeAbsenceFonctionnaire.setLabel("fonctionnaire");
		typeAbsenceFonctionnaire.setTypeSaisi(typeSaisiFonctionnaire);

		RefTypeSaisi typeSaisiContractuel = new RefTypeSaisi();
		typeSaisiContractuel.setFonctionnaire(false);
		typeSaisiContractuel.setContractuel(true);
		typeSaisiContractuel.setConventionCollective(false);

		RefTypeAbsence typeAbsenceContractuel = new RefTypeAbsence();
		typeAbsenceContractuel.setLabel("contractuel");
		typeAbsenceContractuel.setTypeSaisi(typeSaisiContractuel);

		RefTypeSaisi typeSaisiConventionCollective = new RefTypeSaisi();
		typeSaisiConventionCollective.setFonctionnaire(false);
		typeSaisiConventionCollective.setContractuel(false);
		typeSaisiConventionCollective.setConventionCollective(true);

		RefTypeAbsence typeAbsenceConventionCollective = new RefTypeAbsence();
		typeAbsenceConventionCollective.setLabel("conventionCollective");
		typeAbsenceConventionCollective.setTypeSaisi(typeSaisiConventionCollective);

		List<RefTypeAbsence> refTypeAbs = new ArrayList<RefTypeAbsence>();
		refTypeAbs.addAll(Arrays.asList(typeAbsenceFonctionnaire, typeAbsenceContractuel,
				typeAbsenceConventionCollective));

		Spcarr carr = new Spcarr();
		carr.setCdcate(4);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findAllRefTypeAbsences()).thenReturn(refTypeAbs);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());
		Mockito.when(helperService.isFonctionnaire(carr)).thenReturn(false);
		Mockito.when(helperService.isContractuel(carr)).thenReturn(false);
		Mockito.when(helperService.isConventionCollective(carr)).thenReturn(true);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(5138, helperService.getCurrentDate())).thenReturn(carr);

		IAgentMatriculeConverterService agentMatriculeServ = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeServ.fromIdAgentToSIRHNomatrAgent(9005138)).thenReturn(5138);

		FiltreService service = new FiltreService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeServ);

		List<RefTypeAbsenceDto> result = service.getRefTypesAbsence(9005138);

		assertEquals(1, result.size());
		assertEquals("conventionCollective", result.get(0).getLibelle());
	}

	@Test
	public void getRefTypesAbsence_Contractuels() {

		RefTypeSaisi typeSaisiFonctionnaire = new RefTypeSaisi();
		typeSaisiFonctionnaire.setFonctionnaire(true);
		typeSaisiFonctionnaire.setContractuel(false);
		typeSaisiFonctionnaire.setConventionCollective(false);

		RefTypeAbsence typeAbsenceFonctionnaire = new RefTypeAbsence();
		typeAbsenceFonctionnaire.setLabel("fonctionnaire");
		typeAbsenceFonctionnaire.setTypeSaisi(typeSaisiFonctionnaire);

		RefTypeSaisi typeSaisiContractuel = new RefTypeSaisi();
		typeSaisiContractuel.setFonctionnaire(false);
		typeSaisiContractuel.setContractuel(true);
		typeSaisiContractuel.setConventionCollective(false);

		RefTypeAbsence typeAbsenceContractuel = new RefTypeAbsence();
		typeAbsenceContractuel.setLabel("contractuel");
		typeAbsenceContractuel.setTypeSaisi(typeSaisiContractuel);

		RefTypeSaisi typeSaisiConventionCollective = new RefTypeSaisi();
		typeSaisiConventionCollective.setFonctionnaire(false);
		typeSaisiConventionCollective.setContractuel(false);
		typeSaisiConventionCollective.setConventionCollective(true);

		RefTypeAbsence typeAbsenceConventionCollective = new RefTypeAbsence();
		typeAbsenceConventionCollective.setLabel("conventionCollective");
		typeAbsenceConventionCollective.setTypeSaisi(typeSaisiConventionCollective);

		List<RefTypeAbsence> refTypeAbs = new ArrayList<RefTypeAbsence>();
		refTypeAbs.addAll(Arrays.asList(typeAbsenceFonctionnaire, typeAbsenceContractuel,
				typeAbsenceConventionCollective));

		Spcarr carr = new Spcarr();
		carr.setCdcate(4);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findAllRefTypeAbsences()).thenReturn(refTypeAbs);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());
		Mockito.when(helperService.isFonctionnaire(carr)).thenReturn(false);
		Mockito.when(helperService.isContractuel(carr)).thenReturn(true);
		Mockito.when(helperService.isConventionCollective(carr)).thenReturn(false);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(5138, helperService.getCurrentDate())).thenReturn(carr);

		IAgentMatriculeConverterService agentMatriculeServ = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeServ.fromIdAgentToSIRHNomatrAgent(9005138)).thenReturn(5138);

		FiltreService service = new FiltreService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeServ);

		List<RefTypeAbsenceDto> result = service.getRefTypesAbsence(9005138);

		assertEquals(1, result.size());
		assertEquals("contractuel", result.get(0).getLibelle());
	}

	@Test
	public void getListeEtatsByOnglet_NON_PRISES() {

		RefEtat etatProvisoire = new RefEtat();
		etatProvisoire.setIdRefEtat(0);
		etatProvisoire.setLabel(RefEtatEnum.PROVISOIRE.name());
		RefEtat etatPris = new RefEtat();
		etatPris.setIdRefEtat(6);
		etatPris.setLabel(RefEtatEnum.PRISE.name());
		RefEtat etatSaisie = new RefEtat();
		etatSaisie.setIdRefEtat(1);
		etatSaisie.setLabel(RefEtatEnum.SAISIE.name());

		List<RefEtat> listRefEtatNonPris = new ArrayList<RefEtat>();
		listRefEtatNonPris.addAll(Arrays.asList(etatProvisoire, etatSaisie));
		List<RefEtat> listRefEtatEnCours = new ArrayList<RefEtat>();
		listRefEtatEnCours.addAll(Arrays.asList(etatProvisoire, etatSaisie, etatPris));

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefEtatNonPris()).thenReturn(listRefEtatNonPris);
		Mockito.when(filtreRepository.findRefEtatEnCours()).thenReturn(listRefEtatEnCours);

		FiltreService service = new FiltreService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);

		List<RefEtat> result = service.getListeEtatsByOnglet("NON_PRISES", null);

		assertEquals(2, result.size());
		assertEquals(0, result.get(0).getIdRefEtat().intValue());
		assertEquals(1, result.get(1).getIdRefEtat().intValue());
	}

	@Test
	public void getListeEtatsByOnglet_NON_PRISES_WithIdRefEtat() {
		Integer idRefEtat = 1;

		RefEtat etatProvisoire = new RefEtat();
		etatProvisoire.setIdRefEtat(0);
		etatProvisoire.setLabel(RefEtatEnum.PROVISOIRE.name());
		RefEtat etatPris = new RefEtat();
		etatPris.setIdRefEtat(6);
		etatPris.setLabel(RefEtatEnum.PRISE.name());
		RefEtat etatSaisie = new RefEtat();
		etatSaisie.setIdRefEtat(1);
		etatSaisie.setLabel(RefEtatEnum.SAISIE.name());

		List<RefEtat> listRefEtatNonPris = new ArrayList<RefEtat>();
		listRefEtatNonPris.addAll(Arrays.asList(etatProvisoire, etatSaisie));
		List<RefEtat> listRefEtatEnCours = new ArrayList<RefEtat>();
		listRefEtatEnCours.addAll(Arrays.asList(etatProvisoire, etatSaisie, etatPris));

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.getEntity(RefEtat.class, idRefEtat)).thenReturn(etatProvisoire);

		FiltreService service = new FiltreService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);

		List<RefEtat> result = service.getListeEtatsByOnglet("NON_PRISES", Arrays.asList(idRefEtat));

		assertEquals(1, result.size());
		assertEquals(0, result.get(0).getIdRefEtat().intValue());
	}

	@Test
	public void getListeEtatsByOnglet_EN_COURS() {

		RefEtat etatProvisoire = new RefEtat();
		etatProvisoire.setIdRefEtat(0);
		etatProvisoire.setLabel(RefEtatEnum.PROVISOIRE.name());
		RefEtat etatPris = new RefEtat();
		etatPris.setIdRefEtat(6);
		etatPris.setLabel(RefEtatEnum.PRISE.name());
		RefEtat etatSaisie = new RefEtat();
		etatSaisie.setIdRefEtat(1);
		etatSaisie.setLabel(RefEtatEnum.SAISIE.name());

		List<RefEtat> listRefEtatNonPris = new ArrayList<RefEtat>();
		listRefEtatNonPris.addAll(Arrays.asList(etatProvisoire, etatSaisie));
		List<RefEtat> listRefEtatEnCours = new ArrayList<RefEtat>();
		listRefEtatEnCours.addAll(Arrays.asList(etatProvisoire, etatSaisie, etatPris));

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefEtatNonPris()).thenReturn(listRefEtatNonPris);
		Mockito.when(filtreRepository.findRefEtatEnCours()).thenReturn(listRefEtatEnCours);

		FiltreService service = new FiltreService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);

		List<RefEtat> result = service.getListeEtatsByOnglet("EN_COURS", null);

		assertEquals(3, result.size());
		assertEquals(0, result.get(0).getIdRefEtat().intValue());
		assertEquals(1, result.get(1).getIdRefEtat().intValue());
		assertEquals(6, result.get(2).getIdRefEtat().intValue());
	}

	@Test
	public void getListeEtatsByOnglet_EN_COURS_WithIdRefEtat() {
		Integer idRefEtat = 1;

		RefEtat etatProvisoire = new RefEtat();
		etatProvisoire.setIdRefEtat(0);
		etatProvisoire.setLabel(RefEtatEnum.PROVISOIRE.name());
		RefEtat etatPris = new RefEtat();
		etatPris.setIdRefEtat(6);
		etatPris.setLabel(RefEtatEnum.PRISE.name());
		RefEtat etatSaisie = new RefEtat();
		etatSaisie.setIdRefEtat(1);
		etatSaisie.setLabel(RefEtatEnum.SAISIE.name());

		List<RefEtat> listRefEtatNonPris = new ArrayList<RefEtat>();
		listRefEtatNonPris.addAll(Arrays.asList(etatProvisoire, etatSaisie));
		List<RefEtat> listRefEtatEnCours = new ArrayList<RefEtat>();
		listRefEtatEnCours.addAll(Arrays.asList(etatProvisoire, etatSaisie, etatPris));

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.getEntity(RefEtat.class, idRefEtat)).thenReturn(etatProvisoire);

		FiltreService service = new FiltreService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);

		List<RefEtat> result = service.getListeEtatsByOnglet("EN_COURS", Arrays.asList(idRefEtat));

		assertEquals(1, result.size());
		assertEquals(0, result.get(0).getIdRefEtat().intValue());
	}

	@Test
	public void getListeEtatsByOnglet_TOUTES_withIdRefEtat() {

		Integer idRefEtat = RefEtatEnum.PROVISOIRE.getCodeEtat();

		RefEtat etatProvisoire = new RefEtat();
		etatProvisoire.setIdRefEtat(0);
		etatProvisoire.setLabel(RefEtatEnum.PROVISOIRE.name());
		RefEtat etatPris = new RefEtat();
		etatPris.setIdRefEtat(6);
		etatPris.setLabel(RefEtatEnum.PRISE.name());
		RefEtat etatSaisie = new RefEtat();
		etatSaisie.setIdRefEtat(1);
		etatSaisie.setLabel(RefEtatEnum.SAISIE.name());

		List<RefEtat> listRefEtatNonPris = new ArrayList<RefEtat>();
		listRefEtatNonPris.addAll(Arrays.asList(etatProvisoire, etatSaisie));
		List<RefEtat> listRefEtatEnCours = new ArrayList<RefEtat>();
		listRefEtatEnCours.addAll(Arrays.asList(etatProvisoire, etatSaisie, etatPris));

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefEtatNonPris()).thenReturn(listRefEtatNonPris);
		Mockito.when(filtreRepository.findRefEtatEnCours()).thenReturn(listRefEtatEnCours);
		Mockito.when(filtreRepository.getEntity(RefEtat.class, idRefEtat)).thenReturn(etatProvisoire);

		FiltreService service = new FiltreService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);

		List<RefEtat> result = service.getListeEtatsByOnglet("TOUTES", Arrays.asList(idRefEtat));

		assertEquals(1, result.size());
		assertEquals(0, result.get(0).getIdRefEtat().intValue());
	}

	@Test
	public void getListeEtatsByOnglet_TOUTES_withoutIdRefEtat() {

		RefEtat etatProvisoire = new RefEtat();
		etatProvisoire.setIdRefEtat(0);
		etatProvisoire.setLabel(RefEtatEnum.PROVISOIRE.name());
		RefEtat etatPris = new RefEtat();
		etatPris.setIdRefEtat(6);
		etatPris.setLabel(RefEtatEnum.PRISE.name());
		RefEtat etatSaisie = new RefEtat();
		etatSaisie.setIdRefEtat(1);
		etatSaisie.setLabel(RefEtatEnum.SAISIE.name());

		List<RefEtat> listRefEtatToutes = new ArrayList<RefEtat>();
		listRefEtatToutes.addAll(Arrays.asList(etatProvisoire, etatSaisie, etatPris));

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findAllRefEtats()).thenReturn(listRefEtatToutes);

		FiltreService service = new FiltreService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);

		List<RefEtat> result = service.getListeEtatsByOnglet("TOUTES", null);

		assertEquals(3, result.size());
		assertEquals(0, result.get(0).getIdRefEtat().intValue());
		assertEquals(1, result.get(1).getIdRefEtat().intValue());
		assertEquals(6, result.get(2).getIdRefEtat().intValue());
	}

	@Test
	public void getListeEtatsByOnglet_ongletNonDefini() {

		RefEtat etatProvisoire = new RefEtat();
		etatProvisoire.setIdRefEtat(0);
		etatProvisoire.setLabel(RefEtatEnum.PROVISOIRE.name());
		RefEtat etatPris = new RefEtat();
		etatPris.setIdRefEtat(6);
		etatPris.setLabel(RefEtatEnum.PRISE.name());
		RefEtat etatSaisie = new RefEtat();
		etatSaisie.setIdRefEtat(1);
		etatSaisie.setLabel(RefEtatEnum.SAISIE.name());

		List<RefEtat> listRefEtat = new ArrayList<RefEtat>();
		listRefEtat.addAll(Arrays.asList(etatProvisoire, etatSaisie, etatPris));

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findAllRefEtats()).thenReturn(listRefEtat);

		FiltreService service = new FiltreService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);

		List<RefEtat> result = service.getListeEtatsByOnglet(null, null);

		assertEquals(3, result.size());
		assertEquals(0, result.get(0).getIdRefEtat().intValue());
		assertEquals(1, result.get(1).getIdRefEtat().intValue());
		assertEquals(6, result.get(2).getIdRefEtat().intValue());
	}

	@Test
	public void getRefEtats_NON_PRISES() {

		RefEtat etatProvisoire = new RefEtat();
		etatProvisoire.setIdRefEtat(0);
		etatProvisoire.setLabel(RefEtatEnum.PROVISOIRE.name());
		RefEtat etatPris = new RefEtat();
		etatPris.setIdRefEtat(6);
		etatPris.setLabel(RefEtatEnum.PRISE.name());
		RefEtat etatSaisie = new RefEtat();
		etatSaisie.setIdRefEtat(1);
		etatSaisie.setLabel(RefEtatEnum.SAISIE.name());

		List<RefEtat> listRefEtatNonPris = new ArrayList<RefEtat>();
		listRefEtatNonPris.addAll(Arrays.asList(etatProvisoire, etatSaisie));
		List<RefEtat> listRefEtatEnCours = new ArrayList<RefEtat>();
		listRefEtatEnCours.addAll(Arrays.asList(etatProvisoire, etatSaisie, etatPris));

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefEtatNonPris()).thenReturn(listRefEtatNonPris);
		Mockito.when(filtreRepository.findRefEtatEnCours()).thenReturn(listRefEtatEnCours);

		FiltreService service = new FiltreService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);

		List<RefEtatDto> result = service.getRefEtats("NON_PRISES");

		assertEquals(2, result.size());
		assertEquals(0, result.get(0).getIdRefEtat().intValue());
		assertEquals(1, result.get(1).getIdRefEtat().intValue());
	}

	@Test
	public void getRefEtats_EN_COURS() {

		RefEtat etatProvisoire = new RefEtat();
		etatProvisoire.setIdRefEtat(0);
		etatProvisoire.setLabel(RefEtatEnum.PROVISOIRE.name());
		RefEtat etatPris = new RefEtat();
		etatPris.setIdRefEtat(6);
		etatPris.setLabel(RefEtatEnum.PRISE.name());
		RefEtat etatSaisie = new RefEtat();
		etatSaisie.setIdRefEtat(1);
		etatSaisie.setLabel(RefEtatEnum.SAISIE.name());

		List<RefEtat> listRefEtatNonPris = new ArrayList<RefEtat>();
		listRefEtatNonPris.addAll(Arrays.asList(etatProvisoire, etatSaisie));
		List<RefEtat> listRefEtatEnCours = new ArrayList<RefEtat>();
		listRefEtatEnCours.addAll(Arrays.asList(etatProvisoire, etatSaisie, etatPris));

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefEtatNonPris()).thenReturn(listRefEtatNonPris);
		Mockito.when(filtreRepository.findRefEtatEnCours()).thenReturn(listRefEtatEnCours);

		FiltreService service = new FiltreService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);

		List<RefEtatDto> result = service.getRefEtats("EN_COURS");

		assertEquals(3, result.size());
		assertEquals(0, result.get(0).getIdRefEtat().intValue());
		assertEquals(1, result.get(1).getIdRefEtat().intValue());
		assertEquals(6, result.get(2).getIdRefEtat().intValue());
	}

	@Test
	public void getRefEtats_TOUTES() {

		RefEtat etatProvisoire = new RefEtat();
		etatProvisoire.setIdRefEtat(0);
		etatProvisoire.setLabel(RefEtatEnum.PROVISOIRE.name());
		RefEtat etatPris = new RefEtat();
		etatPris.setIdRefEtat(6);
		etatPris.setLabel(RefEtatEnum.PRISE.name());
		RefEtat etatSaisie = new RefEtat();
		etatSaisie.setIdRefEtat(1);
		etatSaisie.setLabel(RefEtatEnum.SAISIE.name());

		List<RefEtat> listRefEtatToutes = new ArrayList<RefEtat>();
		listRefEtatToutes.addAll(Arrays.asList(etatProvisoire, etatSaisie, etatPris));

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findAllRefEtats()).thenReturn(listRefEtatToutes);

		FiltreService service = new FiltreService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);

		List<RefEtatDto> result = service.getRefEtats("TOUTES");

		assertEquals(3, result.size());
		assertEquals(0, result.get(0).getIdRefEtat().intValue());
		assertEquals(1, result.get(1).getIdRefEtat().intValue());
		assertEquals(6, result.get(2).getIdRefEtat().intValue());
	}

	@Test
	public void getRefEtats_ongletNonDefini() {

		RefEtat etatProvisoire = new RefEtat();
		etatProvisoire.setIdRefEtat(0);
		etatProvisoire.setLabel(RefEtatEnum.PROVISOIRE.name());
		RefEtat etatPris = new RefEtat();
		etatPris.setIdRefEtat(6);
		etatPris.setLabel(RefEtatEnum.PRISE.name());
		RefEtat etatSaisie = new RefEtat();
		etatSaisie.setIdRefEtat(1);
		etatSaisie.setLabel(RefEtatEnum.SAISIE.name());

		List<RefEtat> listRefEtat = new ArrayList<RefEtat>();
		listRefEtat.addAll(Arrays.asList(etatProvisoire, etatSaisie, etatPris));

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findAllRefEtats()).thenReturn(listRefEtat);

		FiltreService service = new FiltreService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);

		List<RefEtatDto> result = service.getRefEtats(null);

		assertEquals(3, result.size());
		assertEquals(0, result.get(0).getIdRefEtat().intValue());
		assertEquals(1, result.get(1).getIdRefEtat().intValue());
		assertEquals(6, result.get(2).getIdRefEtat().intValue());
	}

	@Test
	public void getRefTypeSaisi_all() {

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(1);

		RefTypeSaisi rts = new RefTypeSaisi();
		rts.setType(type);
		rts.setCalendarDateDebut(true);
		rts.setCalendarHeureDebut(true);
		rts.setChkDateDebut(true);
		rts.setDuree(true);

		RefTypeSaisi rts2 = new RefTypeSaisi();
		rts2.setType(type);
		rts2.setCalendarDateFin(true);
		rts2.setCalendarHeureFin(true);
		rts2.setChkDateFin(true);
		rts2.setPieceJointe(true);

		List<RefTypeSaisi> listRefTypeSaisi = new ArrayList<RefTypeSaisi>();
		listRefTypeSaisi.addAll(Arrays.asList(rts, rts2));

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findAllRefTypeSaisi()).thenReturn(listRefTypeSaisi);

		FiltreService service = new FiltreService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);

		List<RefTypeSaisiDto> result = service.getRefTypeSaisi(null);

		assertEquals(2, result.size());
		assertTrue(result.get(0).isCalendarDateDebut());
		assertTrue(result.get(0).isCalendarHeureDebut());
		assertTrue(result.get(0).isChkDateDebut());
		assertTrue(result.get(0).isDuree());
		assertFalse(result.get(0).isCalendarDateFin());
		assertFalse(result.get(0).isCalendarHeureFin());
		assertFalse(result.get(0).isChkDateFin());
		assertFalse(result.get(0).isPieceJointe());

		assertFalse(result.get(1).isCalendarDateDebut());
		assertFalse(result.get(1).isCalendarHeureDebut());
		assertFalse(result.get(1).isChkDateDebut());
		assertFalse(result.get(1).isDuree());
		assertTrue(result.get(1).isCalendarDateFin());
		assertTrue(result.get(1).isCalendarHeureFin());
		assertTrue(result.get(1).isChkDateFin());
		assertTrue(result.get(1).isPieceJointe());
	}

	@Test
	public void getRefTypeSaisi_one() {

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(1);

		RefTypeSaisi rts = new RefTypeSaisi();
		rts.setType(type);
		rts.setCalendarDateDebut(true);
		rts.setCalendarHeureDebut(true);
		rts.setChkDateDebut(true);
		rts.setDuree(true);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefTypeSaisi(1)).thenReturn(rts);

		FiltreService service = new FiltreService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);

		List<RefTypeSaisiDto> result = service.getRefTypeSaisi(1);

		assertEquals(1, result.size());
		assertTrue(result.get(0).isCalendarDateDebut());
		assertTrue(result.get(0).isCalendarHeureDebut());
		assertTrue(result.get(0).isChkDateDebut());
		assertTrue(result.get(0).isDuree());
		assertFalse(result.get(0).isCalendarDateFin());
		assertFalse(result.get(0).isCalendarHeureFin());
		assertFalse(result.get(0).isChkDateFin());
		assertFalse(result.get(0).isPieceJointe());
	}

	@Test
	public void getRefGroupeAbsence_return1result() {

		RefGroupeAbsence rga = new RefGroupeAbsence();
		rga.setIdRefGroupeAbsence(1);
		rga.setCode("code 1");
		rga.setLibelle("libelle 1");

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findRefGroupeAbsence(1)).thenReturn(rga);

		FiltreService service = new FiltreService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);

		List<RefGroupeAbsenceDto> result = service.getRefGroupeAbsence(1);

		assertEquals(1, result.size());
		assertEquals("code 1", result.get(0).getCode());
		assertEquals("libelle 1", result.get(0).getLibelle());

	}

	@Test
	public void getRefGroupeAbsence_returnManyResult() {

		RefGroupeAbsence rga = new RefGroupeAbsence();
		rga.setIdRefGroupeAbsence(1);
		rga.setCode("code 1");
		rga.setLibelle("libelle 1");

		RefGroupeAbsence rga2 = new RefGroupeAbsence();
		rga2.setIdRefGroupeAbsence(2);
		rga2.setCode("code 2");
		rga2.setLibelle("libelle 2");

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findAllRefGroupeAbsence()).thenReturn(Arrays.asList(rga, rga2));

		FiltreService service = new FiltreService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);

		List<RefGroupeAbsenceDto> result = service.getRefGroupeAbsence(null);

		assertEquals(2, result.size());
		assertEquals("code 1", result.get(0).getCode());
		assertEquals("libelle 1", result.get(0).getLibelle());
		assertEquals("code 2", result.get(1).getCode());
		assertEquals("libelle 2", result.get(1).getLibelle());

	}

	@Test
	public void getUnitePeriodeQuota() {

		RefUnitePeriodeQuota rupq = new RefUnitePeriodeQuota();
		rupq.setIdRefUnitePeriodeQuota(1);
		rupq.setUnite("jours");
		rupq.setValeur(10);
		rupq.setGlissant(true);

		RefUnitePeriodeQuota rupq2 = new RefUnitePeriodeQuota();
		rupq2.setIdRefUnitePeriodeQuota(2);
		rupq2.setUnite("minutes");
		rupq2.setValeur(13);
		rupq2.setGlissant(false);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findAllRefUnitePeriodeQuota()).thenReturn(Arrays.asList(rupq, rupq2));

		FiltreService service = new FiltreService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);

		List<UnitePeriodeQuotaDto> result = service.getUnitePeriodeQuota();

		assertEquals(2, result.size());
		assertEquals("jours", result.get(0).getUnite());
		assertEquals(10, result.get(0).getValeur().intValue());
		assertTrue(result.get(0).isGlissant());
		assertEquals("minutes", result.get(1).getUnite());
		assertEquals(13, result.get(1).getValeur().intValue());
		assertFalse(result.get(1).isGlissant());
	}

	@Test
	public void getRefTypesAbsenceSaisieKiosque_Fonctionnaire() {

		RefGroupeAbsence groupeRecup = new RefGroupeAbsence();
		groupeRecup.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.RECUP.getValue());

		RefTypeSaisi typeSaisiFonctionnaire2 = new RefTypeSaisi();
		typeSaisiFonctionnaire2.setFonctionnaire(true);
		typeSaisiFonctionnaire2.setContractuel(true);
		typeSaisiFonctionnaire2.setConventionCollective(false);
		typeSaisiFonctionnaire2.setSaisieKiosque(true);

		RefTypeAbsence typeAbsenceFonctionnaire2 = new RefTypeAbsence();
		typeAbsenceFonctionnaire2.setLabel("fonctionnaire2");
		typeAbsenceFonctionnaire2.setTypeSaisi(typeSaisiFonctionnaire2);
		typeAbsenceFonctionnaire2.setGroupe(groupeRecup);

		RefTypeSaisi typeSaisiFonctionnaire = new RefTypeSaisi();
		typeSaisiFonctionnaire.setFonctionnaire(true);
		typeSaisiFonctionnaire.setContractuel(false);
		typeSaisiFonctionnaire.setConventionCollective(false);
		typeSaisiFonctionnaire.setSaisieKiosque(true);

		RefTypeAbsence typeAbsenceFonctionnaire = new RefTypeAbsence();
		typeAbsenceFonctionnaire.setLabel("fonctionnaire");
		typeAbsenceFonctionnaire.setTypeSaisi(typeSaisiFonctionnaire);
		typeAbsenceFonctionnaire.setGroupe(groupeRecup);

		RefTypeSaisi typeSaisiContractuel = new RefTypeSaisi();
		typeSaisiContractuel.setFonctionnaire(false);
		typeSaisiContractuel.setContractuel(true);
		typeSaisiContractuel.setConventionCollective(false);
		typeSaisiContractuel.setSaisieKiosque(true);

		RefTypeAbsence typeAbsenceContractuel = new RefTypeAbsence();
		typeAbsenceContractuel.setLabel("contractuel");
		typeAbsenceContractuel.setTypeSaisi(typeSaisiContractuel);
		typeAbsenceContractuel.setGroupe(groupeRecup);

		RefTypeSaisi typeSaisiConventionCollective = new RefTypeSaisi();
		typeSaisiConventionCollective.setFonctionnaire(false);
		typeSaisiConventionCollective.setContractuel(false);
		typeSaisiConventionCollective.setConventionCollective(true);
		typeSaisiConventionCollective.setSaisieKiosque(true);

		RefTypeAbsence typeAbsenceConventionCollective = new RefTypeAbsence();
		typeAbsenceConventionCollective.setLabel("conventionCollective");
		typeAbsenceConventionCollective.setTypeSaisi(typeSaisiConventionCollective);
		typeAbsenceConventionCollective.setGroupe(groupeRecup);

		List<RefTypeAbsence> refTypeAbs = new ArrayList<RefTypeAbsence>();
		refTypeAbs.addAll(Arrays.asList(typeAbsenceFonctionnaire, typeAbsenceFonctionnaire2, typeAbsenceContractuel,
				typeAbsenceConventionCollective));

		Spcarr carr = new Spcarr();
		carr.setCdcate(4);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findAllRefTypeAbsences()).thenReturn(refTypeAbs);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());
		Mockito.when(helperService.isFonctionnaire(carr)).thenReturn(true);
		Mockito.when(helperService.isContractuel(carr)).thenReturn(false);
		Mockito.when(helperService.isConventionCollective(carr)).thenReturn(false);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(5138, helperService.getCurrentDate())).thenReturn(carr);

		IAgentMatriculeConverterService agentMatriculeServ = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeServ.fromIdAgentToSIRHNomatrAgent(9005138)).thenReturn(5138);

		FiltreService service = new FiltreService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeServ);

		List<RefTypeAbsenceDto> result = service.getRefTypesAbsenceSaisieKiosque(null, null);

		assertEquals(4, result.size());
	}

	@Test
	public void getRefTypesAbsenceSaisieKiosque_CC() {

		RefTypeSaisi typeSaisiFonctionnaire = new RefTypeSaisi();
		typeSaisiFonctionnaire.setFonctionnaire(true);
		typeSaisiFonctionnaire.setContractuel(false);
		typeSaisiFonctionnaire.setConventionCollective(false);
		typeSaisiFonctionnaire.setSaisieKiosque(true);

		RefGroupeAbsence groupeRecup = new RefGroupeAbsence();
		groupeRecup.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.RECUP.getValue());

		RefTypeAbsence typeAbsenceFonctionnaire = new RefTypeAbsence();
		typeAbsenceFonctionnaire.setLabel("fonctionnaire");
		typeAbsenceFonctionnaire.setTypeSaisi(typeSaisiFonctionnaire);
		typeAbsenceFonctionnaire.setGroupe(groupeRecup);

		RefTypeSaisi typeSaisiContractuel = new RefTypeSaisi();
		typeSaisiContractuel.setFonctionnaire(false);
		typeSaisiContractuel.setContractuel(true);
		typeSaisiContractuel.setConventionCollective(false);
		typeSaisiContractuel.setSaisieKiosque(true);

		RefTypeAbsence typeAbsenceContractuel = new RefTypeAbsence();
		typeAbsenceContractuel.setLabel("contractuel");
		typeAbsenceContractuel.setTypeSaisi(typeSaisiContractuel);
		typeAbsenceContractuel.setGroupe(groupeRecup);

		RefTypeSaisi typeSaisiConventionCollective = new RefTypeSaisi();
		typeSaisiConventionCollective.setFonctionnaire(false);
		typeSaisiConventionCollective.setContractuel(false);
		typeSaisiConventionCollective.setConventionCollective(true);
		typeSaisiConventionCollective.setSaisieKiosque(true);

		RefTypeAbsence typeAbsenceConventionCollective = new RefTypeAbsence();
		typeAbsenceConventionCollective.setLabel("conventionCollective");
		typeAbsenceConventionCollective.setTypeSaisi(typeSaisiConventionCollective);
		typeAbsenceConventionCollective.setGroupe(groupeRecup);

		List<RefTypeAbsence> refTypeAbs = new ArrayList<RefTypeAbsence>();
		refTypeAbs.addAll(Arrays.asList(typeAbsenceFonctionnaire, typeAbsenceContractuel,
				typeAbsenceConventionCollective));

		Spcarr carr = new Spcarr();
		carr.setCdcate(4);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findAllRefTypeAbsences()).thenReturn(refTypeAbs);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());
		Mockito.when(helperService.isFonctionnaire(carr)).thenReturn(false);
		Mockito.when(helperService.isContractuel(carr)).thenReturn(false);
		Mockito.when(helperService.isConventionCollective(carr)).thenReturn(true);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(5138, helperService.getCurrentDate())).thenReturn(carr);

		IAgentMatriculeConverterService agentMatriculeServ = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeServ.fromIdAgentToSIRHNomatrAgent(9005138)).thenReturn(5138);

		FiltreService service = new FiltreService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeServ);

		List<RefTypeAbsenceDto> result = service.getRefTypesAbsenceSaisieKiosque(null, null);

		assertEquals(3, result.size());
	}

	@Test
	public void getRefTypesAbsenceSaisieKiosque_Contractuels() {

		RefGroupeAbsence groupeRecup = new RefGroupeAbsence();
		groupeRecup.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.RECUP.getValue());

		RefTypeSaisi typeSaisiFonctionnaire = new RefTypeSaisi();
		typeSaisiFonctionnaire.setFonctionnaire(true);
		typeSaisiFonctionnaire.setContractuel(false);
		typeSaisiFonctionnaire.setConventionCollective(false);
		typeSaisiFonctionnaire.setSaisieKiosque(true);

		RefTypeAbsence typeAbsenceFonctionnaire = new RefTypeAbsence();
		typeAbsenceFonctionnaire.setLabel("fonctionnaire");
		typeAbsenceFonctionnaire.setTypeSaisi(typeSaisiFonctionnaire);
		typeAbsenceFonctionnaire.setGroupe(groupeRecup);

		RefTypeSaisi typeSaisiContractuel = new RefTypeSaisi();
		typeSaisiContractuel.setFonctionnaire(false);
		typeSaisiContractuel.setContractuel(true);
		typeSaisiContractuel.setConventionCollective(false);
		typeSaisiContractuel.setSaisieKiosque(true);

		RefTypeAbsence typeAbsenceContractuel = new RefTypeAbsence();
		typeAbsenceContractuel.setLabel("contractuel");
		typeAbsenceContractuel.setTypeSaisi(typeSaisiContractuel);
		typeAbsenceContractuel.setGroupe(groupeRecup);

		RefTypeSaisi typeSaisiConventionCollective = new RefTypeSaisi();
		typeSaisiConventionCollective.setFonctionnaire(false);
		typeSaisiConventionCollective.setContractuel(false);
		typeSaisiConventionCollective.setConventionCollective(true);
		typeSaisiConventionCollective.setSaisieKiosque(true);

		RefTypeAbsence typeAbsenceConventionCollective = new RefTypeAbsence();
		typeAbsenceConventionCollective.setLabel("conventionCollective");
		typeAbsenceConventionCollective.setTypeSaisi(typeSaisiConventionCollective);
		typeAbsenceConventionCollective.setGroupe(groupeRecup);

		List<RefTypeAbsence> refTypeAbs = new ArrayList<RefTypeAbsence>();
		refTypeAbs.addAll(Arrays.asList(typeAbsenceFonctionnaire, typeAbsenceContractuel,
				typeAbsenceConventionCollective));

		Spcarr carr = new Spcarr();
		carr.setCdcate(4);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findAllRefTypeAbsences()).thenReturn(refTypeAbs);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());
		Mockito.when(helperService.isFonctionnaire(carr)).thenReturn(false);
		Mockito.when(helperService.isContractuel(carr)).thenReturn(true);
		Mockito.when(helperService.isConventionCollective(carr)).thenReturn(false);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(5138, helperService.getCurrentDate())).thenReturn(carr);

		IAgentMatriculeConverterService agentMatriculeServ = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeServ.fromIdAgentToSIRHNomatrAgent(9005138)).thenReturn(5138);

		FiltreService service = new FiltreService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeServ);

		List<RefTypeAbsenceDto> result = service.getRefTypesAbsenceSaisieKiosque(null, null);

		assertEquals(3, result.size());
	}

	@Test
	public void getRefTypesAbsenceSaisieKiosque_WithGoupe_Fonctionnaire() {
		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(1);

		RefTypeSaisi typeSaisiFonctionnaire2 = new RefTypeSaisi();
		typeSaisiFonctionnaire2.setFonctionnaire(true);
		typeSaisiFonctionnaire2.setContractuel(true);
		typeSaisiFonctionnaire2.setConventionCollective(false);
		typeSaisiFonctionnaire2.setSaisieKiosque(true);

		RefTypeAbsence typeAbsenceFonctionnaire2 = new RefTypeAbsence();
		typeAbsenceFonctionnaire2.setLabel("fonctionnaire2");
		typeAbsenceFonctionnaire2.setTypeSaisi(typeSaisiFonctionnaire2);
		typeAbsenceFonctionnaire2.setGroupe(groupe);

		RefTypeSaisi typeSaisiFonctionnaire = new RefTypeSaisi();
		typeSaisiFonctionnaire.setFonctionnaire(true);
		typeSaisiFonctionnaire.setContractuel(false);
		typeSaisiFonctionnaire.setConventionCollective(false);
		typeSaisiFonctionnaire.setSaisieKiosque(true);

		RefTypeAbsence typeAbsenceFonctionnaire = new RefTypeAbsence();
		typeAbsenceFonctionnaire.setLabel("fonctionnaire");
		typeAbsenceFonctionnaire.setTypeSaisi(typeSaisiFonctionnaire);
		typeAbsenceFonctionnaire.setGroupe(groupe);

		RefTypeSaisi typeSaisiContractuel = new RefTypeSaisi();
		typeSaisiContractuel.setFonctionnaire(false);
		typeSaisiContractuel.setContractuel(true);
		typeSaisiContractuel.setConventionCollective(false);
		typeSaisiContractuel.setSaisieKiosque(true);

		RefTypeAbsence typeAbsenceContractuel = new RefTypeAbsence();
		typeAbsenceContractuel.setLabel("contractuel");
		typeAbsenceContractuel.setTypeSaisi(typeSaisiContractuel);
		typeAbsenceContractuel.setGroupe(groupe);

		RefTypeSaisi typeSaisiConventionCollective = new RefTypeSaisi();
		typeSaisiConventionCollective.setFonctionnaire(false);
		typeSaisiConventionCollective.setContractuel(false);
		typeSaisiConventionCollective.setConventionCollective(true);
		typeSaisiConventionCollective.setSaisieKiosque(true);

		RefTypeAbsence typeAbsenceConventionCollective = new RefTypeAbsence();
		typeAbsenceConventionCollective.setLabel("conventionCollective");
		typeAbsenceConventionCollective.setTypeSaisi(typeSaisiConventionCollective);
		typeAbsenceConventionCollective.setGroupe(groupe);

		List<RefTypeAbsence> refTypeAbs = new ArrayList<RefTypeAbsence>();
		refTypeAbs.addAll(Arrays.asList(typeAbsenceFonctionnaire, typeAbsenceFonctionnaire2, typeAbsenceContractuel,
				typeAbsenceConventionCollective));

		Spcarr carr = new Spcarr();
		carr.setCdcate(4);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findAllRefTypeAbsencesWithGroup(1)).thenReturn(refTypeAbs);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());
		Mockito.when(helperService.isFonctionnaire(carr)).thenReturn(true);
		Mockito.when(helperService.isContractuel(carr)).thenReturn(false);
		Mockito.when(helperService.isConventionCollective(carr)).thenReturn(false);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(5138, helperService.getCurrentDate())).thenReturn(carr);

		IAgentMatriculeConverterService agentMatriculeServ = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeServ.fromIdAgentToSIRHNomatrAgent(9005138)).thenReturn(5138);

		FiltreService service = new FiltreService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeServ);

		List<RefTypeAbsenceDto> result = service.getRefTypesAbsenceSaisieKiosque(1, null);

		assertEquals(4, result.size());
	}

	@Test
	public void getRefTypesAbsenceSaisieKiosque_WithGoupe_CC() {
		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(1);

		RefTypeSaisi typeSaisiFonctionnaire = new RefTypeSaisi();
		typeSaisiFonctionnaire.setFonctionnaire(true);
		typeSaisiFonctionnaire.setContractuel(false);
		typeSaisiFonctionnaire.setConventionCollective(false);
		typeSaisiFonctionnaire.setSaisieKiosque(true);

		RefTypeAbsence typeAbsenceFonctionnaire = new RefTypeAbsence();
		typeAbsenceFonctionnaire.setLabel("fonctionnaire");
		typeAbsenceFonctionnaire.setTypeSaisi(typeSaisiFonctionnaire);
		typeAbsenceFonctionnaire.setGroupe(groupe);

		RefTypeSaisi typeSaisiContractuel = new RefTypeSaisi();
		typeSaisiContractuel.setFonctionnaire(false);
		typeSaisiContractuel.setContractuel(true);
		typeSaisiContractuel.setConventionCollective(false);
		typeSaisiContractuel.setSaisieKiosque(true);

		RefTypeAbsence typeAbsenceContractuel = new RefTypeAbsence();
		typeAbsenceContractuel.setLabel("contractuel");
		typeAbsenceContractuel.setTypeSaisi(typeSaisiContractuel);
		typeAbsenceContractuel.setGroupe(groupe);

		RefTypeSaisi typeSaisiConventionCollective = new RefTypeSaisi();
		typeSaisiConventionCollective.setFonctionnaire(false);
		typeSaisiConventionCollective.setContractuel(false);
		typeSaisiConventionCollective.setConventionCollective(true);
		typeSaisiConventionCollective.setSaisieKiosque(true);

		RefTypeAbsence typeAbsenceConventionCollective = new RefTypeAbsence();
		typeAbsenceConventionCollective.setLabel("conventionCollective");
		typeAbsenceConventionCollective.setTypeSaisi(typeSaisiConventionCollective);
		typeAbsenceConventionCollective.setGroupe(groupe);

		List<RefTypeAbsence> refTypeAbs = new ArrayList<RefTypeAbsence>();
		refTypeAbs.addAll(Arrays.asList(typeAbsenceFonctionnaire, typeAbsenceContractuel,
				typeAbsenceConventionCollective));

		Spcarr carr = new Spcarr();
		carr.setCdcate(4);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findAllRefTypeAbsencesWithGroup(1)).thenReturn(refTypeAbs);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());
		Mockito.when(helperService.isFonctionnaire(carr)).thenReturn(false);
		Mockito.when(helperService.isContractuel(carr)).thenReturn(false);
		Mockito.when(helperService.isConventionCollective(carr)).thenReturn(true);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(5138, helperService.getCurrentDate())).thenReturn(carr);

		IAgentMatriculeConverterService agentMatriculeServ = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeServ.fromIdAgentToSIRHNomatrAgent(9005138)).thenReturn(5138);

		FiltreService service = new FiltreService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeServ);

		List<RefTypeAbsenceDto> result = service.getRefTypesAbsenceSaisieKiosque(1, null);

		assertEquals(3, result.size());
	}

	@Test
	public void getRefTypesAbsenceSaisieKiosque_WithGoupe_Contractuels() {
		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(1);

		RefTypeSaisi typeSaisiFonctionnaire = new RefTypeSaisi();
		typeSaisiFonctionnaire.setFonctionnaire(true);
		typeSaisiFonctionnaire.setContractuel(false);
		typeSaisiFonctionnaire.setConventionCollective(false);
		typeSaisiFonctionnaire.setSaisieKiosque(true);

		RefTypeAbsence typeAbsenceFonctionnaire = new RefTypeAbsence();
		typeAbsenceFonctionnaire.setLabel("fonctionnaire");
		typeAbsenceFonctionnaire.setTypeSaisi(typeSaisiFonctionnaire);
		typeAbsenceFonctionnaire.setGroupe(groupe);

		RefTypeSaisi typeSaisiContractuel = new RefTypeSaisi();
		typeSaisiContractuel.setFonctionnaire(false);
		typeSaisiContractuel.setContractuel(true);
		typeSaisiContractuel.setConventionCollective(false);
		typeSaisiContractuel.setSaisieKiosque(true);

		RefTypeAbsence typeAbsenceContractuel = new RefTypeAbsence();
		typeAbsenceContractuel.setLabel("contractuel");
		typeAbsenceContractuel.setTypeSaisi(typeSaisiContractuel);
		typeAbsenceContractuel.setGroupe(groupe);

		RefTypeSaisi typeSaisiConventionCollective = new RefTypeSaisi();
		typeSaisiConventionCollective.setFonctionnaire(false);
		typeSaisiConventionCollective.setContractuel(false);
		typeSaisiConventionCollective.setConventionCollective(true);
		typeSaisiConventionCollective.setSaisieKiosque(true);

		RefTypeAbsence typeAbsenceConventionCollective = new RefTypeAbsence();
		typeAbsenceConventionCollective.setLabel("conventionCollective");
		typeAbsenceConventionCollective.setTypeSaisi(typeSaisiConventionCollective);
		typeAbsenceConventionCollective.setGroupe(groupe);

		List<RefTypeAbsence> refTypeAbs = new ArrayList<RefTypeAbsence>();
		refTypeAbs.addAll(Arrays.asList(typeAbsenceFonctionnaire, typeAbsenceContractuel,
				typeAbsenceConventionCollective));

		Spcarr carr = new Spcarr();
		carr.setCdcate(4);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findAllRefTypeAbsencesWithGroup(1)).thenReturn(refTypeAbs);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());
		Mockito.when(helperService.isFonctionnaire(carr)).thenReturn(false);
		Mockito.when(helperService.isContractuel(carr)).thenReturn(true);
		Mockito.when(helperService.isConventionCollective(carr)).thenReturn(false);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(5138, helperService.getCurrentDate())).thenReturn(carr);

		IAgentMatriculeConverterService agentMatriculeServ = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeServ.fromIdAgentToSIRHNomatrAgent(9005138)).thenReturn(5138);

		FiltreService service = new FiltreService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeServ);

		List<RefTypeAbsenceDto> result = service.getRefTypesAbsenceSaisieKiosque(1, null);

		assertEquals(3, result.size());
	}
	
	@Test
	public void getAllRefTypeAccidentTravail() {
		
		RefTypeAccidentTravail at = new RefTypeAccidentTravail();
		at.setIdRefAccidentTravail(11);
		at.setLibelle("libelle at 1");
		
		RefTypeAccidentTravail at2 = new RefTypeAccidentTravail();
		at2.setIdRefAccidentTravail(12);
		at2.setLibelle("libelle at 2");
		
		List<RefTypeAccidentTravail> listAT = new ArrayList<RefTypeAccidentTravail>();
		listAT.add(at);
		listAT.add(at2);
		
		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findAllRefTypeAccidentTravail()).thenReturn(listAT);
				
		FiltreService service = new FiltreService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		
		List<RefTypeDto> result = service.getAllRefTypeAccidentTravail();
		
		assertEquals(result.get(0).getIdRefType(), at.getIdRefAccidentTravail());
		assertEquals(result.get(0).getLibelle(), at.getLibelle());
		assertEquals(result.get(1).getIdRefType(), at2.getIdRefAccidentTravail());
		assertEquals(result.get(1).getLibelle(), at2.getLibelle());
	}
	
	@Test
	public void getAllRefTypeSiegeLesion() {
		
		RefTypeSiegeLesion sl = new RefTypeSiegeLesion();
		sl.setIdRefSiegeLesion(11);
		sl.setLibelle("libelle sl 1");
		
		RefTypeSiegeLesion sl2 = new RefTypeSiegeLesion();
		sl2.setIdRefSiegeLesion(12);
		sl2.setLibelle("libelle sl 2");
		
		List<RefTypeSiegeLesion> listSL = new ArrayList<RefTypeSiegeLesion>();
		listSL.add(sl);
		listSL.add(sl2);
		
		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findAllRefTypeSiegeLesion()).thenReturn(listSL);
				
		FiltreService service = new FiltreService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		
		List<RefTypeDto> result = service.getAllRefTypeSiegeLesion();
		
		assertEquals(result.get(0).getIdRefType(), sl.getIdRefSiegeLesion());
		assertEquals(result.get(0).getLibelle(), sl.getLibelle());
		assertEquals(result.get(1).getIdRefType(), sl2.getIdRefSiegeLesion());
		assertEquals(result.get(1).getLibelle(), sl2.getLibelle());
	}
	
	@Test
	public void getAllRefTypeMaladiePro() {
		
		RefTypeMaladiePro mp = new RefTypeMaladiePro();
		mp.setIdRefMaladiePro(11);
		mp.setLibelle("libelle mp 1");
		
		RefTypeMaladiePro mp2 = new RefTypeMaladiePro();
		mp2.setIdRefMaladiePro(12);
		mp2.setLibelle("libelle mp 2");
		
		List<RefTypeMaladiePro> listMP = new ArrayList<RefTypeMaladiePro>();
		listMP.add(mp);
		listMP.add(mp2);
		
		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findAllRefTypeMaladiePro()).thenReturn(listMP);
				
		FiltreService service = new FiltreService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		
		List<RefTypeDto> result = service.getAllRefTypeMaladiePro();
		
		assertEquals(result.get(0).getIdRefType(), mp.getIdRefMaladiePro());
		assertEquals(result.get(0).getLibelle(), mp.getLibelle());
		assertEquals(result.get(1).getIdRefType(), mp2.getIdRefMaladiePro());
		assertEquals(result.get(1).getLibelle(), mp2.getLibelle());
	}
	
	@Test
	public void setTypeGenerique_AT_NotUserSIRH() {
		
		Integer idAgent = 9005138;
		
		RefTypeDto dto = null;

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		
		ReturnMessageDto resultSIRH =  new ReturnMessageDto();
		resultSIRH.getErrors().add("error user SIRH");
		
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(resultSIRH);
		
		FiltreService service = new FiltreService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		
		ReturnMessageDto result = service.setTypeGenerique(idAgent, RefTypeAccidentTravail.class, dto);
		
		assertEquals(result.getErrors().get(0), "L'agent n'est pas habilité.");
	}
	
	@Test
	public void setTypeGenerique_AT_null() {
		
		Integer idAgent = 9005138;
		
		RefTypeDto dto = null;

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(new ReturnMessageDto());
		
		FiltreService service = new FiltreService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		
		ReturnMessageDto result = service.setTypeGenerique(idAgent, RefTypeAccidentTravail.class, dto);
		
		assertEquals(result.getErrors().get(0), "Merci de renseigner les informations nécessaires.");
	}
	
	@Test
	public void setTypeGenerique_AT_notExist() {
		
		Integer idAgent = 9005138;
		
		RefTypeDto dto = new RefTypeDto();
		dto.setIdRefType(1);
		dto.setLibelle("libelle");

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findObject(RefTypeAccidentTravail.class, dto.getIdRefType())).thenReturn(null);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(new ReturnMessageDto());
		
		FiltreService service = new FiltreService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		
		ReturnMessageDto result = service.setTypeGenerique(idAgent, RefTypeAccidentTravail.class, dto);
		
		assertEquals(result.getErrors().get(0), "Le type n'existe pas.");
	}
	
	@Test
	public void setTypeGenerique_AT_modify() {
		
		Integer idAgent = 9005138;
		
		RefTypeDto dto = new RefTypeDto();
		dto.setIdRefType(1);
		dto.setLibelle("libelle");

		RefTypeAccidentTravail at = Mockito.spy(new RefTypeAccidentTravail());
		at.setIdRefAccidentTravail(1);
		at.setLibelle("");
		
		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findObject(RefTypeAccidentTravail.class, dto.getIdRefType())).thenReturn(at);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(new ReturnMessageDto());
		
		FiltreService service = new FiltreService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		
		ReturnMessageDto result = service.setTypeGenerique(idAgent, RefTypeAccidentTravail.class, dto);
		
		assertEquals(result.getErrors().size(), 0);
		assertEquals(result.getInfos().get(0), "Enregistrement effectué.");
		Mockito.verify(filtreRepository, Mockito.times(1)).persist(at);
		assertEquals(at.getLibelle(), dto.getLibelle());
	}
	
	@Test
	public void setTypeGenerique_AT_create() {
		
		Integer idAgent = 9005138;
		
		RefTypeDto dto = new RefTypeDto();
		dto.setIdRefType(null);
		dto.setLibelle("libelle");
		
		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(new ReturnMessageDto());
		
		FiltreService service = new FiltreService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		
		ReturnMessageDto result = service.setTypeGenerique(idAgent, RefTypeAccidentTravail.class, dto);
		
		assertEquals(result.getErrors().size(), 0);
		assertEquals(result.getInfos().get(0), "Enregistrement effectué.");
		Mockito.verify(filtreRepository, Mockito.times(1)).persist(Mockito.isA(RefTypeAccidentTravail.class));
	}
	
	@Test
	public void setTypeGenerique_MaladiePro_modify() {
		
		Integer idAgent = 9005138;
		
		RefTypeDto dto = new RefTypeDto();
		dto.setIdRefType(1);
		dto.setLibelle("libelle");

		RefTypeMaladiePro mp = Mockito.spy(new RefTypeMaladiePro());
		mp.setIdRefMaladiePro(1);
		mp.setLibelle("");
		
		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findObject(RefTypeMaladiePro.class, dto.getIdRefType())).thenReturn(mp);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(new ReturnMessageDto());
		
		FiltreService service = new FiltreService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		
		ReturnMessageDto result = service.setTypeGenerique(idAgent, RefTypeMaladiePro.class, dto);
		
		assertEquals(result.getErrors().size(), 0);
		assertEquals(result.getInfos().get(0), "Enregistrement effectué.");
		Mockito.verify(filtreRepository, Mockito.times(1)).persist(mp);
		assertEquals(mp.getLibelle(), dto.getLibelle());
	}
	
	@Test
	public void setTypeGenerique_SiegeLesion_modify() {
		
		Integer idAgent = 9005138;
		
		RefTypeDto dto = new RefTypeDto();
		dto.setIdRefType(1);
		dto.setLibelle("libelle");

		RefTypeSiegeLesion sl = Mockito.spy(new RefTypeSiegeLesion());
		sl.setIdRefSiegeLesion(1);
		sl.setLibelle("");
		
		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findObject(RefTypeSiegeLesion.class, dto.getIdRefType())).thenReturn(sl);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(idAgent)).thenReturn(new ReturnMessageDto());
		
		FiltreService service = new FiltreService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		
		ReturnMessageDto result = service.setTypeGenerique(idAgent, RefTypeSiegeLesion.class, dto);
		
		assertEquals(result.getErrors().size(), 0);
		assertEquals(result.getInfos().get(0), "Enregistrement effectué.");
		Mockito.verify(filtreRepository, Mockito.times(1)).persist(sl);
		assertEquals(sl.getLibelle(), dto.getLibelle());
	}
}
