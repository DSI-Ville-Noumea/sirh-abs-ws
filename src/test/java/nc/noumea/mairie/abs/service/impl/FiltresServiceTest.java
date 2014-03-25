package nc.noumea.mairie.abs.service.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.RefEtat;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.dto.RefEtatDto;
import nc.noumea.mairie.abs.dto.RefTypeAbsenceDto;
import nc.noumea.mairie.abs.repository.IDemandeRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.domain.Spcarr;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class FiltresServiceTest {

	@Test
	public void getRefTypesAbsence_noIdAgent() {

		RefTypeAbsence ASA = new RefTypeAbsence();
		ASA.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A48.getValue());
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
		refTypeAbs.addAll(Arrays.asList(ASA, AUTRES, CONGE_ANNUEL, MALADIES, RECUP, REPOS_COMP));

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.findAllRefTypeAbsences()).thenReturn(refTypeAbs);

		FiltresService service = new FiltresService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		List<RefTypeAbsenceDto> result = service.getRefTypesAbsence(null);

		assertEquals(6, result.size());
	}

	@Test
	public void getRefTypesAbsence_Fonctionnaire() {

		RefTypeAbsence ASA = new RefTypeAbsence();
		ASA.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A48.getValue());
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
		refTypeAbs.addAll(Arrays.asList(ASA, AUTRES, CONGE_ANNUEL, MALADIES, RECUP, REPOS_COMP));

		Spcarr carr = new Spcarr();
		carr.setCdcate(24);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.findAllRefTypeAbsences()).thenReturn(refTypeAbs);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(9005138, helperService.getCurrentDate())).thenReturn(carr);

		FiltresService service = new FiltresService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);

		List<RefTypeAbsenceDto> result = service.getRefTypesAbsence(9005138);

		assertEquals(5, result.size());
	}

	@Test
	public void getRefTypesAbsence_CC() {

		RefTypeAbsence ASA = new RefTypeAbsence();
		ASA.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A48.getValue());
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
		refTypeAbs.addAll(Arrays.asList(ASA, AUTRES, CONGE_ANNUEL, MALADIES, RECUP, REPOS_COMP));

		Spcarr carr = new Spcarr();
		carr.setCdcate(4);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.findAllRefTypeAbsences()).thenReturn(refTypeAbs);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(9005138, helperService.getCurrentDate())).thenReturn(carr);

		FiltresService service = new FiltresService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);

		List<RefTypeAbsenceDto> result = service.getRefTypesAbsence(9005138);

		assertEquals(6, result.size());
	}

	@Test
	public void getRefTypesAbsence_C() {

		RefTypeAbsence ASA = new RefTypeAbsence();
		ASA.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A48.getValue());
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
		refTypeAbs.addAll(Arrays.asList(ASA, AUTRES, CONGE_ANNUEL, MALADIES, RECUP, REPOS_COMP));

		Spcarr carr = new Spcarr();
		carr.setCdcate(7);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.findAllRefTypeAbsences()).thenReturn(refTypeAbs);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(new Date());

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(9005138, helperService.getCurrentDate())).thenReturn(carr);

		FiltresService service = new FiltresService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);

		List<RefTypeAbsenceDto> result = service.getRefTypesAbsence(9005138);

		assertEquals(6, result.size());
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

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.findRefEtatNonPris()).thenReturn(listRefEtatNonPris);
		Mockito.when(demandeRepository.findRefEtatEnCours()).thenReturn(listRefEtatEnCours);

		FiltresService service = new FiltresService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

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

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.findRefEtatNonPris()).thenReturn(listRefEtatNonPris);
		Mockito.when(demandeRepository.findRefEtatEnCours()).thenReturn(listRefEtatEnCours);

		FiltresService service = new FiltresService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

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

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.findRefEtatNonPris()).thenReturn(listRefEtatNonPris);
		Mockito.when(demandeRepository.findRefEtatEnCours()).thenReturn(listRefEtatEnCours);
		Mockito.when(demandeRepository.getEntity(RefEtat.class, idRefEtat)).thenReturn(etatProvisoire);

		FiltresService service = new FiltresService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

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

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.findAllRefEtats()).thenReturn(listRefEtatToutes);

		FiltresService service = new FiltresService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

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

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.findAllRefEtats()).thenReturn(listRefEtat);

		FiltresService service = new FiltresService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

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

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.findRefEtatNonPris()).thenReturn(listRefEtatNonPris);
		Mockito.when(demandeRepository.findRefEtatEnCours()).thenReturn(listRefEtatEnCours);

		FiltresService service = new FiltresService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

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

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.findRefEtatNonPris()).thenReturn(listRefEtatNonPris);
		Mockito.when(demandeRepository.findRefEtatEnCours()).thenReturn(listRefEtatEnCours);

		FiltresService service = new FiltresService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

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

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.findAllRefEtats()).thenReturn(listRefEtatToutes);

		FiltresService service = new FiltresService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

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

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.findAllRefEtats()).thenReturn(listRefEtat);

		FiltresService service = new FiltresService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		List<RefEtatDto> result = service.getRefEtats(null);

		assertEquals(3, result.size());
		assertEquals(0, result.get(0).getIdRefEtat().intValue());
		assertEquals(1, result.get(1).getIdRefEtat().intValue());
		assertEquals(6, result.get(2).getIdRefEtat().intValue());
	}
}