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
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.domain.RefTypeSaisi;
import nc.noumea.mairie.abs.dto.RefEtatDto;
import nc.noumea.mairie.abs.dto.RefTypeAbsenceDto;
import nc.noumea.mairie.abs.dto.RefTypeSaisiDto;
import nc.noumea.mairie.abs.repository.IFiltreRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.domain.Spcarr;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class FiltreServiceTest {

	@Test
	public void getRefTypesAbsence_noIdAgent() {

		RefTypeAbsence ASA_A55 = new RefTypeAbsence();
		ASA_A55.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A55.getValue());
		RefTypeAbsence ASA_A54 = new RefTypeAbsence();
		ASA_A54.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A54.getValue());
		RefTypeAbsence ASA_A48 = new RefTypeAbsence();
		ASA_A48.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A48.getValue());
		RefTypeAbsence AUTRES = new RefTypeAbsence();
		AUTRES.setIdRefTypeAbsence(RefTypeAbsenceEnum.AUTRES.getValue());
		RefTypeAbsence CONGE_ANNUEL = new RefTypeAbsence();
		CONGE_ANNUEL.setIdRefTypeAbsence(RefTypeAbsenceEnum.CONGE_ANNUEL.getValue());
		RefTypeAbsence MALADIES = new RefTypeAbsence();
		MALADIES.setIdRefTypeAbsence(RefTypeAbsenceEnum.MALADIES.getValue());
		RefTypeAbsence RECUP = new RefTypeAbsence();
		RECUP.setIdRefTypeAbsence(RefTypeAbsenceEnum.RECUP.getValue());
		RefTypeAbsence REPOS_COMP = new RefTypeAbsence();
		REPOS_COMP.setIdRefTypeAbsence(RefTypeAbsenceEnum.REPOS_COMP.getValue());

		List<RefTypeAbsence> refTypeAbs = new ArrayList<RefTypeAbsence>();
		refTypeAbs.addAll(Arrays.asList(ASA_A55, ASA_A54, ASA_A48, AUTRES, CONGE_ANNUEL, MALADIES, RECUP, REPOS_COMP));

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findAllRefTypeAbsences()).thenReturn(refTypeAbs);

		FiltreService service = new FiltreService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);

		List<RefTypeAbsenceDto> result = service.getRefTypesAbsence(null);

		assertEquals(0, result.size());
	}

	@Test
	public void getRefTypesAbsence_Fonctionnaire() {

		RefTypeAbsence ASA_A55 = new RefTypeAbsence();
		ASA_A55.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A55.getValue());
		RefTypeAbsence ASA_A54 = new RefTypeAbsence();
		ASA_A54.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A54.getValue());
		RefTypeAbsence ASA_A48 = new RefTypeAbsence();
		ASA_A48.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A48.getValue());
		RefTypeAbsence AUTRES = new RefTypeAbsence();
		AUTRES.setIdRefTypeAbsence(RefTypeAbsenceEnum.AUTRES.getValue());
		RefTypeAbsence CONGE_ANNUEL = new RefTypeAbsence();
		CONGE_ANNUEL.setIdRefTypeAbsence(RefTypeAbsenceEnum.CONGE_ANNUEL.getValue());
		RefTypeAbsence MALADIES = new RefTypeAbsence();
		MALADIES.setIdRefTypeAbsence(RefTypeAbsenceEnum.MALADIES.getValue());
		RefTypeAbsence RECUP = new RefTypeAbsence();
		RECUP.setIdRefTypeAbsence(RefTypeAbsenceEnum.RECUP.getValue());
		RefTypeAbsence REPOS_COMP = new RefTypeAbsence();
		REPOS_COMP.setIdRefTypeAbsence(RefTypeAbsenceEnum.REPOS_COMP.getValue());

		List<RefTypeAbsence> refTypeAbs = new ArrayList<RefTypeAbsence>();
		refTypeAbs.addAll(Arrays.asList(ASA_A55, ASA_A54, ASA_A48, AUTRES, CONGE_ANNUEL, MALADIES, RECUP, REPOS_COMP));

		Spcarr carr = new Spcarr();
		carr.setCdcate(24);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findAllRefTypeAbsences()).thenReturn(refTypeAbs);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(9005138, helperService.getCurrentDate())).thenReturn(carr);

		FiltreService service = new FiltreService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);

		List<RefTypeAbsenceDto> result = service.getRefTypesAbsence(9005138);

		assertEquals(7, result.size());
	}

	@Test
	public void getRefTypesAbsence_CC() {

		RefTypeAbsence ASA_A55 = new RefTypeAbsence();
		ASA_A55.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A55.getValue());
		RefTypeAbsence ASA_A54 = new RefTypeAbsence();
		ASA_A54.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A54.getValue());
		RefTypeAbsence ASA_A48 = new RefTypeAbsence();
		ASA_A48.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A48.getValue());
		RefTypeAbsence AUTRES = new RefTypeAbsence();
		AUTRES.setIdRefTypeAbsence(RefTypeAbsenceEnum.AUTRES.getValue());
		RefTypeAbsence CONGE_ANNUEL = new RefTypeAbsence();
		CONGE_ANNUEL.setIdRefTypeAbsence(RefTypeAbsenceEnum.CONGE_ANNUEL.getValue());
		RefTypeAbsence MALADIES = new RefTypeAbsence();
		MALADIES.setIdRefTypeAbsence(RefTypeAbsenceEnum.MALADIES.getValue());
		RefTypeAbsence RECUP = new RefTypeAbsence();
		RECUP.setIdRefTypeAbsence(RefTypeAbsenceEnum.RECUP.getValue());
		RefTypeAbsence REPOS_COMP = new RefTypeAbsence();
		REPOS_COMP.setIdRefTypeAbsence(RefTypeAbsenceEnum.REPOS_COMP.getValue());

		List<RefTypeAbsence> refTypeAbs = new ArrayList<RefTypeAbsence>();
		refTypeAbs.addAll(Arrays.asList(ASA_A55, ASA_A54, ASA_A48, AUTRES, CONGE_ANNUEL, MALADIES, RECUP, REPOS_COMP));

		Spcarr carr = new Spcarr();
		carr.setCdcate(4);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findAllRefTypeAbsences()).thenReturn(refTypeAbs);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(9005138, helperService.getCurrentDate())).thenReturn(carr);

		FiltreService service = new FiltreService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);

		List<RefTypeAbsenceDto> result = service.getRefTypesAbsence(9005138);

		assertEquals(7, result.size());
	}

	@Test
	public void getRefTypesAbsence_C() {

		RefTypeAbsence ASA_A55 = new RefTypeAbsence();
		ASA_A55.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A55.getValue());
		RefTypeAbsence ASA_A54 = new RefTypeAbsence();
		ASA_A54.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A54.getValue());
		RefTypeAbsence ASA_A48 = new RefTypeAbsence();
		ASA_A48.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A48.getValue());
		RefTypeAbsence AUTRES = new RefTypeAbsence();
		AUTRES.setIdRefTypeAbsence(RefTypeAbsenceEnum.AUTRES.getValue());
		RefTypeAbsence CONGE_ANNUEL = new RefTypeAbsence();
		CONGE_ANNUEL.setIdRefTypeAbsence(RefTypeAbsenceEnum.CONGE_ANNUEL.getValue());
		RefTypeAbsence MALADIES = new RefTypeAbsence();
		MALADIES.setIdRefTypeAbsence(RefTypeAbsenceEnum.MALADIES.getValue());
		RefTypeAbsence RECUP = new RefTypeAbsence();
		RECUP.setIdRefTypeAbsence(RefTypeAbsenceEnum.RECUP.getValue());
		RefTypeAbsence REPOS_COMP = new RefTypeAbsence();
		REPOS_COMP.setIdRefTypeAbsence(RefTypeAbsenceEnum.REPOS_COMP.getValue());

		List<RefTypeAbsence> refTypeAbs = new ArrayList<RefTypeAbsence>();
		refTypeAbs.addAll(Arrays.asList(ASA_A55, ASA_A54, ASA_A48, AUTRES, CONGE_ANNUEL, MALADIES, RECUP, REPOS_COMP));

		Spcarr carr = new Spcarr();
		carr.setCdcate(7);

		IFiltreRepository filtreRepository = Mockito.mock(IFiltreRepository.class);
		Mockito.when(filtreRepository.findAllRefTypeAbsences()).thenReturn(refTypeAbs);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(9005138, helperService.getCurrentDate())).thenReturn(carr);

		FiltreService service = new FiltreService();
		ReflectionTestUtils.setField(service, "filtreRepository", filtreRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);

		List<RefTypeAbsenceDto> result = service.getRefTypesAbsence(9005138);

		assertEquals(7, result.size());
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

		List<RefEtat> result = service.getListeEtatsByOnglet("TOUTES", idRefEtat);

		assertEquals(1, result.size());
		assertEquals(0, result.get(0).getIdRefEtat().intValue());
	}

	@Test
	public void getListeEtatsByOnglet_TOUTES_withoutIdRefEtat() {

		Integer idRefEtat = null;

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

		List<RefEtat> result = service.getListeEtatsByOnglet("TOUTES", idRefEtat);

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
		rts.setIdRefTypeAbsence(1);
		rts.setCalendarDateDebut(true);
		rts.setCalendarHeureDebut(true);
		rts.setChkDateDebut(true);
		rts.setDuree(true);

		RefTypeSaisi rts2 = new RefTypeSaisi();
		rts2.setType(type);
		rts2.setIdRefTypeAbsence(2);
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
		rts.setIdRefTypeAbsence(1);
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
}