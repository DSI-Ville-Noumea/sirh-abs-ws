package nc.noumea.mairie.abs.service.rules.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;

import nc.noumea.mairie.abs.domain.AgentCongeAnnuelCount;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeCongesAnnuels;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeSaisiCongeAnnuel;
import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.RefTypeSaisiCongeAnnuelDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IAccessRightsRepository;
import nc.noumea.mairie.abs.repository.ICongesAnnuelsRepository;
import nc.noumea.mairie.abs.repository.ICounterRepository;
import nc.noumea.mairie.abs.repository.IDemandeRepository;
import nc.noumea.mairie.abs.service.impl.HelperService;
import nc.noumea.mairie.ws.ISirhWSConsumer;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.staticmock.MockStaticEntityMethods;
import org.springframework.test.util.ReflectionTestUtils;

@MockStaticEntityMethods
public class AbsCongesAnnuelsDataConsistencyRulesImplTest extends DefaultAbsenceDataConsistencyRulesImplTest {

	protected AbsCongesAnnuelsDataConsistencyRulesImpl impl = new AbsCongesAnnuelsDataConsistencyRulesImpl();

	@Test
	public void testMethodeParenteHeritage() throws Throwable {
		super.impl = new AbsCongesExcepDataConsistencyRulesImpl();
		super.allTest(impl);
	}

	@Test
	public void checkSaisiNewTypeAbsence() {
		RefTypeSaisiCongeAnnuel typeSaisi = new RefTypeSaisiCongeAnnuel();

		ReturnMessageDto result = new ReturnMessageDto();
		result = impl.checkSaisiNewTypeAbsence(null, typeSaisi, result);

		assertEquals(1, result.getErrors().size());
		assertEquals(result.getErrors().get(0),
				"La saisie de nouveau type d'absence pour ce groupe d'absence n'est pas autorisée.");
	}

	@Test
	public void checkSaisiNewTypeAbsence_ErreurDateDebut() {
		RefTypeSaisiCongeAnnuel typeSaisi = new RefTypeSaisiCongeAnnuel();
		typeSaisi.setIdRefTypeSaisiCongeAnnuel(1);
		typeSaisi.setCalendarDateDebut(false);

		ReturnMessageDto result = new ReturnMessageDto();
		result = impl.checkSaisiNewTypeAbsence(null, typeSaisi, result);

		assertEquals(3, result.getErrors().size());
		assertEquals(result.getErrors().get(0), "La date de début est obligatoire.");
		assertEquals(result.getErrors().get(1), "Si date de reprise est à non, alors date de fin doit être à oui.");
		assertEquals(result.getErrors().get(2), "Si consécutif est à non, alors décompte du samedi doit être à oui.");
	}

	@Test
	public void checkSaisiNewTypeAbsence_ErreurDateFin_Reprise() {
		RefTypeSaisiCongeAnnuel typeSaisi = new RefTypeSaisiCongeAnnuel();
		typeSaisi.setIdRefTypeSaisiCongeAnnuel(1);
		typeSaisi.setCalendarDateDebut(true);
		typeSaisi.setCalendarDateFin(true);
		typeSaisi.setCalendarDateReprise(true);

		ReturnMessageDto result = new ReturnMessageDto();
		result = impl.checkSaisiNewTypeAbsence(null, typeSaisi, result);

		assertEquals(2, result.getErrors().size());
		assertEquals(result.getErrors().get(0), "Si date de reprise est à oui, alors date de fin doit être à non.");
		assertEquals(result.getErrors().get(1), "Si consécutif est à non, alors décompte du samedi doit être à oui.");
	}

	@Test
	public void checkSaisiNewTypeAbsence_ErreurDateFin_Reprise_Bis() {
		RefTypeSaisiCongeAnnuel typeSaisi = new RefTypeSaisiCongeAnnuel();
		typeSaisi.setIdRefTypeSaisiCongeAnnuel(1);
		typeSaisi.setCalendarDateDebut(true);
		typeSaisi.setCalendarDateFin(false);
		typeSaisi.setCalendarDateReprise(false);

		ReturnMessageDto result = new ReturnMessageDto();
		result = impl.checkSaisiNewTypeAbsence(null, typeSaisi, result);

		assertEquals(2, result.getErrors().size());
		assertEquals(result.getErrors().get(0), "Si date de reprise est à non, alors date de fin doit être à oui.");
		assertEquals(result.getErrors().get(1), "Si consécutif est à non, alors décompte du samedi doit être à oui.");
	}

	@Test
	public void checkSaisiNewTypeAbsence_ErreurDecompteSamed_Consecutif() {
		RefTypeSaisiCongeAnnuel typeSaisi = new RefTypeSaisiCongeAnnuel();
		typeSaisi.setIdRefTypeSaisiCongeAnnuel(1);
		typeSaisi.setCalendarDateDebut(true);
		typeSaisi.setCalendarDateFin(true);
		typeSaisi.setCalendarDateReprise(false);
		typeSaisi.setDecompteSamedi(true);
		typeSaisi.setConsecutif(true);

		ReturnMessageDto result = new ReturnMessageDto();
		result = impl.checkSaisiNewTypeAbsence(null, typeSaisi, result);

		assertEquals(1, result.getErrors().size());
		assertEquals(result.getErrors().get(0), "Si consécutif est à oui, alors décompte du samedi doit être à non.");
	}

	@Test
	public void checkSaisiNewTypeAbsence_ErreurDecompteSamed_Consecutif_Bis() {
		RefTypeSaisiCongeAnnuel typeSaisi = new RefTypeSaisiCongeAnnuel();
		typeSaisi.setIdRefTypeSaisiCongeAnnuel(1);
		typeSaisi.setCalendarDateDebut(true);
		typeSaisi.setCalendarDateFin(true);
		typeSaisi.setCalendarDateReprise(false);
		typeSaisi.setDecompteSamedi(false);
		typeSaisi.setConsecutif(false);

		ReturnMessageDto result = new ReturnMessageDto();
		result = impl.checkSaisiNewTypeAbsence(null, typeSaisi, result);

		assertEquals(1, result.getErrors().size());
		assertEquals(result.getErrors().get(0), "Si consécutif est à non, alors décompte du samedi doit être à oui.");
	}

	@Test
	public void checkSaisiNewTypeAbsence_ok() {
		RefTypeSaisiCongeAnnuel typeSaisi = new RefTypeSaisiCongeAnnuel();
		typeSaisi.setIdRefTypeSaisiCongeAnnuel(1);
		typeSaisi.setCalendarDateDebut(true);
		typeSaisi.setCalendarDateFin(true);
		typeSaisi.setCalendarDateReprise(false);
		typeSaisi.setDecompteSamedi(false);
		typeSaisi.setConsecutif(true);

		ReturnMessageDto result = new ReturnMessageDto();
		result = impl.checkSaisiNewTypeAbsence(null, typeSaisi, result);

		assertEquals(0, result.getErrors().size());
	}

	@Test
	public void checkChampMotifDemandeSaisi_ok_motifSaisi() {

		ReturnMessageDto srm = new ReturnMessageDto();

		RefTypeAbsence type = new RefTypeAbsence();

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setCommentaire("commentaire");
		demande.setType(type);

		srm = impl.checkChampMotifDemandeSaisi(srm, demande);

		assertEquals(0, srm.getErrors().size());
	}

	@Test
	public void checkChampMotifDemandeSaisi_ko_motifNull() {

		ReturnMessageDto srm = new ReturnMessageDto();

		RefTypeAbsence type = new RefTypeAbsence();

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setCommentaire(null);
		demande.setType(type);

		srm = impl.checkChampMotifDemandeSaisi(srm, demande);

		assertEquals(1, srm.getErrors().size());
		assertEquals(srm.getErrors().get(0), AbsCongesExcepDataConsistencyRulesImpl.CHAMP_COMMENTAIRE_OBLIGATOIRE);
	}

	@Test
	public void checkChampMotifDemandeSaisi_ko_motifVide() {

		ReturnMessageDto srm = new ReturnMessageDto();

		RefTypeAbsence type = new RefTypeAbsence();

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setCommentaire("");
		demande.setType(type);

		srm = impl.checkChampMotifDemandeSaisi(srm, demande);

		assertEquals(1, srm.getErrors().size());
		assertEquals(srm.getErrors().get(0), AbsCongesExcepDataConsistencyRulesImpl.CHAMP_COMMENTAIRE_OBLIGATOIRE);
	}

	@Test
	public void checkDepassementDroitsAcquis_false() {

		ReturnMessageDto srm = new ReturnMessageDto();

		AgentWithServiceDto agentWithServiceDto = new AgentWithServiceDto();
		agentWithServiceDto.setIdAgent(9005138);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setIdDemande(1);
		demande.setDateDebut(new Date());
		demande.setIdAgent(agentWithServiceDto.getIdAgent());
		demande.setDuree(10.0);

		AgentCongeAnnuelCount soldeCongeAnnuel = new AgentCongeAnnuelCount();
		soldeCongeAnnuel.setTotalJours(2.0);
		soldeCongeAnnuel.setTotalJoursAnneeN1(0.0);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, demande.getIdAgent())).thenReturn(
				soldeCongeAnnuel);

		ICongesAnnuelsRepository congesAnnuelsRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(
				congesAnnuelsRepository.getSommeDureeDemandeCongeAnnuelEnCoursSaisieouViseeouAValider(
						demande.getIdAgent(), demande.getIdDemande())).thenReturn(0.0);

		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(impl, "congesAnnuelsRepository", congesAnnuelsRepository);

		srm = impl.checkDepassementDroitsAcquis(srm, demande);

		assertEquals(1, srm.getErrors().size());
	}

	@Test
	public void checkDepassementDroitsAcquis_true() {

		ReturnMessageDto srm = new ReturnMessageDto();

		AgentWithServiceDto agentWithServiceDto = new AgentWithServiceDto();
		agentWithServiceDto.setIdAgent(9005138);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setIdDemande(1);
		demande.setDateDebut(new Date());
		demande.setIdAgent(agentWithServiceDto.getIdAgent());
		demande.setDuree(10.0);

		AgentCongeAnnuelCount soldeCongeAnnuel = new AgentCongeAnnuelCount();
		soldeCongeAnnuel.setTotalJours(20.0);
		soldeCongeAnnuel.setTotalJoursAnneeN1(0.0);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, demande.getIdAgent())).thenReturn(
				soldeCongeAnnuel);

		ICongesAnnuelsRepository congesAnnuelsRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(
				congesAnnuelsRepository.getSommeDureeDemandeCongeAnnuelEnCoursSaisieouViseeouAValider(
						demande.getIdAgent(), demande.getIdDemande())).thenReturn(0.0);

		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(impl, "congesAnnuelsRepository", congesAnnuelsRepository);

		srm = impl.checkDepassementDroitsAcquis(srm, demande);

		assertEquals(0, srm.getErrors().size());
	}

	@Test
	public void checkBaseHoraireAbsenceAgent_false() {

		ReturnMessageDto srm = new ReturnMessageDto();
		Integer idAgent = 9005138;
		Date dateDebut = new Date();

		RefTypeSaisiCongeAnnuelDto typeSaisieCongeAnnuel = new RefTypeSaisiCongeAnnuelDto();

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getBaseHoraireAbsence(idAgent, dateDebut)).thenReturn(typeSaisieCongeAnnuel);

		ReflectionTestUtils.setField(impl, "sirhWSConsumer", sirhWSConsumer);

		srm = impl.checkBaseHoraireAbsenceAgent(srm, idAgent, dateDebut);

		assertEquals(1, srm.getErrors().size());
	}

	@Test
	public void checkBaseHoraireAbsenceAgent_true() {

		ReturnMessageDto srm = new ReturnMessageDto();
		Integer idAgent = 9005138;
		Date dateDebut = new Date();

		RefTypeSaisiCongeAnnuelDto typeSaisieCongeAnnuel = new RefTypeSaisiCongeAnnuelDto();
		typeSaisieCongeAnnuel.setIdRefTypeSaisiCongeAnnuel(1);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getBaseHoraireAbsence(idAgent, dateDebut)).thenReturn(typeSaisieCongeAnnuel);

		ReflectionTestUtils.setField(impl, "sirhWSConsumer", sirhWSConsumer);

		srm = impl.checkBaseHoraireAbsenceAgent(srm, idAgent, dateDebut);

		assertEquals(0, srm.getErrors().size());
	}

	@Test
	public void checkMultipleCycle_withNoMultiple() {
		Integer idOperateur = 9005138;

		ReturnMessageDto srm = new ReturnMessageDto();

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeSaisiCongeAnnuel.setCodeBaseHoraireAbsence("A");
		typeSaisiCongeAnnuel.setQuotaMultiple(null);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setCommentaire(null);
		demande.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);

		srm = impl.checkMultipleCycle(srm, demande, idOperateur);

		assertEquals(0, srm.getErrors().size());
	}

	@Test
	public void checkMultipleCycle_withMultiple() {
		Integer idOperateur = 9005138;

		ReturnMessageDto srm = new ReturnMessageDto();

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeSaisiCongeAnnuel.setCodeBaseHoraireAbsence("E");
		typeSaisiCongeAnnuel.setQuotaMultiple(5);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setCommentaire(null);
		demande.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);
		demande.setIdAgent(9003041);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreJours(Mockito.any(Date.class), Mockito.any(Date.class))).thenReturn(4.0);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isOperateurOfAgent(idOperateur, demande.getIdAgent())).thenReturn(false);

		ReflectionTestUtils.setField(impl, "helperService", helperService);
		ReflectionTestUtils.setField(impl, "accessRightsRepository", accessRightsRepository);
		srm = impl.checkMultipleCycle(srm, demande, idOperateur);

		assertEquals(1, srm.getErrors().size());
		assertEquals(srm.getErrors().get(0),
				"Pour la base congé E, la durée du congé doit être un multiple de 5 jours.");
	}

	@Test
	public void checkMultipleCycle_withMultiple_ForOperateur() {
		Integer idOperateur = 9005138;

		ReturnMessageDto srm = new ReturnMessageDto();

		RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuel();
		typeSaisiCongeAnnuel.setCodeBaseHoraireAbsence("E");
		typeSaisiCongeAnnuel.setQuotaMultiple(5);

		DemandeCongesAnnuels demande = new DemandeCongesAnnuels();
		demande.setCommentaire(null);
		demande.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);
		demande.setIdAgent(9003041);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreJours(Mockito.any(Date.class), Mockito.any(Date.class))).thenReturn(4.0);

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isOperateurOfAgent(idOperateur, demande.getIdAgent())).thenReturn(true);

		ReflectionTestUtils.setField(impl, "helperService", helperService);
		ReflectionTestUtils.setField(impl, "accessRightsRepository", accessRightsRepository);
		srm = impl.checkMultipleCycle(srm, demande, idOperateur);

		assertEquals(1, srm.getInfos().size());
		assertEquals(srm.getInfos().get(0), "Pour la base congé E, la durée du congé doit être un multiple de 5 jours.");
	}

	@Test
	public void checkDepassementMultipleAgent_Ok() {
		RefTypeSaisiCongeAnnuel typeSaisi = new RefTypeSaisiCongeAnnuel();
		typeSaisi.setIdRefTypeSaisiCongeAnnuel(1);
		typeSaisi.setCalendarDateDebut(true);
		typeSaisi.setCalendarDateFin(true);
		typeSaisi.setCalendarDateReprise(false);
		typeSaisi.setDecompteSamedi(false);
		typeSaisi.setConsecutif(true);
		typeSaisi.setQuotaMultiple(3);

		RefTypeSaisiCongeAnnuelDto typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuelDto(typeSaisi);

		DemandeDto demandeDto = new DemandeDto();
		demandeDto.setDuree(5.0);
		demandeDto.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(
				demandeRepository.getEntity(RefTypeSaisiCongeAnnuel.class, typeSaisi.getIdRefTypeSaisiCongeAnnuel()))
				.thenReturn(typeSaisi);

		ReflectionTestUtils.setField(impl, "demandeRepository", demandeRepository);
		boolean result = impl.checkDepassementMultipleAgent(demandeDto);

		assertTrue(result);
	}

	@Test
	public void checkDepassementMultipleAgent_Ko() {
		RefTypeSaisiCongeAnnuel typeSaisi = new RefTypeSaisiCongeAnnuel();
		typeSaisi.setIdRefTypeSaisiCongeAnnuel(1);
		typeSaisi.setCalendarDateDebut(true);
		typeSaisi.setCalendarDateFin(true);
		typeSaisi.setCalendarDateReprise(false);
		typeSaisi.setDecompteSamedi(false);
		typeSaisi.setConsecutif(true);
		typeSaisi.setQuotaMultiple(5);

		RefTypeSaisiCongeAnnuelDto typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuelDto(typeSaisi);

		DemandeDto demandeDto = new DemandeDto();
		demandeDto.setDuree(5.0);
		demandeDto.setTypeSaisiCongeAnnuel(typeSaisiCongeAnnuel);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(
				demandeRepository.getEntity(RefTypeSaisiCongeAnnuel.class, typeSaisi.getIdRefTypeSaisiCongeAnnuel()))
				.thenReturn(typeSaisi);

		ReflectionTestUtils.setField(impl, "demandeRepository", demandeRepository);
		boolean result = impl.checkDepassementMultipleAgent(demandeDto);

		assertFalse(result);
	}

	@Test
	public void isAfficherBoutonAnnuler_isOperateur() {

		DemandeDto demandeDto = new DemandeDto();
		demandeDto.setIdRefEtat(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat());

		boolean result = impl.isAfficherBoutonAnnuler(demandeDto, true);
		assertTrue(result);

		demandeDto.setIdRefEtat(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, true);
		assertTrue(result);

		demandeDto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, true);
		assertTrue(result);

		demandeDto.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, true);
		assertTrue(result);

		demandeDto.setIdRefEtat(RefEtatEnum.EN_ATTENTE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, true);
		assertTrue(result);

		demandeDto.setIdRefEtat(RefEtatEnum.PRISE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, true);
		assertTrue(result);

		demandeDto.setIdRefEtat(RefEtatEnum.PROVISOIRE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, true);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, true);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.REFUSEE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, true);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.REJETE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, true);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, true);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.A_VALIDER.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, true);
		assertTrue(result);
	}

	@Test
	public void isAfficherBoutonAnnuler_isNotOperateur() {

		DemandeDto demandeDto = new DemandeDto();
		demandeDto.setIdRefEtat(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat());

		boolean result = impl.isAfficherBoutonAnnuler(demandeDto, false);
		assertTrue(result);

		demandeDto.setIdRefEtat(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, false);
		assertTrue(result);

		demandeDto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, false);
		assertTrue(result);

		demandeDto.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, false);
		assertTrue(result);

		demandeDto.setIdRefEtat(RefEtatEnum.EN_ATTENTE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, false);
		assertTrue(result);

		demandeDto.setIdRefEtat(RefEtatEnum.PRISE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, false);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.PROVISOIRE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, false);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, false);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.REFUSEE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, false);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.REJETE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, false);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, false);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.A_VALIDER.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, true);
		assertTrue(result);
	}


	@Test
	public void checkEtatsDemandeAnnulee_isValidee() {

		ReturnMessageDto srm = new ReturnMessageDto();
		Demande demande = new Demande();
		demande.setIdDemande(1);
		EtatDemande etat = new EtatDemande();
		etat.setEtat(RefEtatEnum.VALIDEE);
		demande.getEtatsDemande().add(etat);

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

		srm = impl.checkEtatsDemandeAnnulee(srm, demande, Arrays.asList(RefEtatEnum.PROVISOIRE, RefEtatEnum.SAISIE));

		assertEquals(1, srm.getErrors().size());
	}
}
