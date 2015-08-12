package nc.noumea.mairie.abs.service.rules.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import nc.noumea.mairie.abs.domain.AgentReposCompCount;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeReposComp;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeGroupeAbsenceEnum;
import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.RefGroupeAbsenceDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.ICounterRepository;
import nc.noumea.mairie.abs.repository.IReposCompensateurRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.abs.service.IAgentMatriculeConverterService;
import nc.noumea.mairie.abs.service.impl.HelperService;
import nc.noumea.mairie.domain.Spcarr;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.staticmock.MockStaticEntityMethods;
import org.springframework.test.util.ReflectionTestUtils;

@MockStaticEntityMethods
public class AbsReposCompensateurDataConsistencyRulesImplTest extends DefaultAbsenceDataConsistencyRulesImplTest {

	@Test
	public void allTest() throws Throwable {
		super.impl = new AbsReposCompensateurDataConsistencyRulesImpl();
		super.allTest(impl);
	}

	@Test
	public void filtreDroitOfListeDemandesByDemande_DemandeOfAgent() {

		super.impl = new AbsReposCompensateurDataConsistencyRulesImpl();
		super.filtreDroitOfListeDemandesByDemande_DemandeOfAgent();

		// Then
		// PROVISOIRE
		assertFalse(result1.isAffichageBoutonAnnuler());
		// SAISIE
		assertFalse(result2.isAffichageBoutonAnnuler());
		// APPROUVEE
		assertTrue(result3.isAffichageBoutonAnnuler());
		// REFUSEE
		assertFalse(result4.isAffichageBoutonAnnuler());
		// VISEE_FAVORABLE
		assertTrue(result5.isAffichageBoutonAnnuler());
		// VISEE_DEFAVORABLE
		assertTrue(result6.isAffichageBoutonAnnuler());
		// PRISE
		assertFalse(result7.isAffichageBoutonAnnuler());
		// ANNULEE
		assertFalse(result8.isAffichageBoutonAnnuler());
		// VALIDEE
		assertTrue(result9.isAffichageBoutonAnnuler());
		// REJETE
		assertFalse(result10.isAffichageBoutonAnnuler());
		// EN ATTENTE
		assertTrue(result11.isAffichageBoutonAnnuler());
		// A VALIDER
		assertTrue(result12.isAffichageBoutonAnnuler());
	}

	@Test
	public void filtreDroitOfListeDemandesByDemande_Operateur() {

		super.impl = new AbsReposCompensateurDataConsistencyRulesImpl();
		super.filtreDroitOfListeDemandesByDemande_Operateur();

		// Then
		// Then
		// PROVISOIRE
		assertFalse(result1.isAffichageBoutonAnnuler());
		// SAISIE
		assertFalse(result2.isAffichageBoutonAnnuler());
		// APPROUVEE
		assertTrue(result3.isAffichageBoutonAnnuler());
		// REFUSEE
		assertFalse(result4.isAffichageBoutonAnnuler());
		// VISEE_FAVORABLE
		assertTrue(result5.isAffichageBoutonAnnuler());
		// VISEE_DEFAVORABLE
		assertTrue(result6.isAffichageBoutonAnnuler());
		// PRISE
		assertTrue(result7.isAffichageBoutonAnnuler());
		// ANNULEE
		assertFalse(result8.isAffichageBoutonAnnuler());
		// VALIDEE
		assertTrue(result9.isAffichageBoutonAnnuler());
		// REJETE
		assertFalse(result10.isAffichageBoutonAnnuler());
		// EN ATTENTE
		assertTrue(result11.isAffichageBoutonAnnuler());
		// A VALIDER
		assertTrue(result12.isAffichageBoutonAnnuler());
	}

	@Test
	public void filtreDroitOfListeDemandesByDemande_Approbateur() {

		super.impl = new AbsReposCompensateurDataConsistencyRulesImpl();
		super.filtreDroitOfListeDemandesByDemande_Approbateur();

		// Then
		// PROVISOIRE
		assertFalse(result1.isAffichageBoutonAnnuler());
		// SAISIE
		assertFalse(result2.isAffichageBoutonAnnuler());
		// APPROUVEE
		assertTrue(result3.isAffichageBoutonAnnuler());
		// REFUSEE
		assertFalse(result4.isAffichageBoutonAnnuler());
		// VISEE_FAVORABLE
		assertTrue(result5.isAffichageBoutonAnnuler());
		// VISEE_DEFAVORABLE
		assertTrue(result6.isAffichageBoutonAnnuler());
		// PRISE
		assertTrue(result7.isAffichageBoutonAnnuler());
		// ANNULEE
		assertFalse(result8.isAffichageBoutonAnnuler());
		// VALIDEE
		assertTrue(result9.isAffichageBoutonAnnuler());
		// REJETE
		assertFalse(result10.isAffichageBoutonAnnuler());
		// EN ATTENTE
		assertTrue(result11.isAffichageBoutonAnnuler());
		// A VALIDER
		assertTrue(result12.isAffichageBoutonAnnuler());
	}

	@Test
	public void filtreDroitOfListeDemandesByDemande_Delegataire() {

		super.impl = new AbsReposCompensateurDataConsistencyRulesImpl();
		super.filtreDroitOfListeDemandesByDemande_Delegataire();

		// Then
		// PROVISOIRE
		assertFalse(result1.isAffichageBoutonAnnuler());
		// SAISIE
		assertFalse(result2.isAffichageBoutonAnnuler());
		// APPROUVEE
		assertTrue(result3.isAffichageBoutonAnnuler());
		// REFUSEE
		assertFalse(result4.isAffichageBoutonAnnuler());
		// VISEE_FAVORABLE
		assertTrue(result5.isAffichageBoutonAnnuler());
		// VISEE_DEFAVORABLE
		assertTrue(result6.isAffichageBoutonAnnuler());
		// PRISE
		assertTrue(result7.isAffichageBoutonAnnuler());
		// ANNULEE
		assertFalse(result8.isAffichageBoutonAnnuler());
		// VALIDEE
		assertTrue(result9.isAffichageBoutonAnnuler());
		// REJETE
		assertFalse(result10.isAffichageBoutonAnnuler());
		// EN ATTENTE
		assertTrue(result11.isAffichageBoutonAnnuler());
		// A VALIDER
		assertTrue(result12.isAffichageBoutonAnnuler());
	}

	@Test
	public void filtreDroitOfListeDemandesByDemande_Operateur_ET_ApprobateurSameGroup() {

		super.impl = new AbsReposCompensateurDataConsistencyRulesImpl();
		super.filtreDroitOfListeDemandesByDemande_Operateur_ET_ApprobateurSameGroup();

		// Then
		// Then
		// PROVISOIRE
		assertFalse(result1.isAffichageBoutonAnnuler());
		// SAISIE
		assertFalse(result2.isAffichageBoutonAnnuler());
		// APPROUVEE
		assertTrue(result3.isAffichageBoutonAnnuler());
		// REFUSEE
		assertFalse(result4.isAffichageBoutonAnnuler());
		// VISEE_FAVORABLE
		assertTrue(result5.isAffichageBoutonAnnuler());
		// VISEE_DEFAVORABLE
		assertTrue(result6.isAffichageBoutonAnnuler());
		// PRISE
		assertTrue(result7.isAffichageBoutonAnnuler());
		// ANNULEE
		assertFalse(result8.isAffichageBoutonAnnuler());
		// VALIDEE
		assertTrue(result9.isAffichageBoutonAnnuler());
		// REJETE
		assertFalse(result10.isAffichageBoutonAnnuler());
		// EN ATTENTE
		assertTrue(result11.isAffichageBoutonAnnuler());
		// A VALIDER
		assertTrue(result12.isAffichageBoutonAnnuler());
	}

	@Test
	public void filtreDroitOfDemandeSIRH() {

		super.impl = new AbsReposCompensateurDataConsistencyRulesImpl();
		super.filtreDroitOfDemandeSIRH();

		// APPROUVEE
		assertFalse(result3.isAffichageValidation());
		assertFalse(result3.isModifierValidation());
		assertFalse(result3.isAffichageEnAttente());

		// PRISE
		assertTrue(result7.isAffichageBoutonAnnuler());
		assertFalse(result7.isAffichageValidation());
		assertFalse(result7.isAffichageEnAttente());

		// VALIDEE
		assertTrue(result9.isAffichageBoutonAnnuler());
		assertFalse(result9.isAffichageValidation());
		assertFalse(result9.isAffichageEnAttente());

		// REJETEE
		assertFalse(result10.isAffichageValidation());
		assertFalse(result10.isAffichageEnAttente());

		// EN ATTENTE
		assertTrue(result11.isAffichageBoutonAnnuler());
		assertFalse(result11.isAffichageValidation());
		assertFalse(result11.isModifierValidation());
		assertFalse(result11.isAffichageEnAttente());
	}

	@Test
	public void checkDepassementDroitsAcquis_iOk() {

		ReturnMessageDto srm = new ReturnMessageDto();
		DemandeReposComp demande = new DemandeReposComp();
		demande.setIdAgent(9005138);
		demande.setDuree(40);

		AgentReposCompCount soldeReposComp = new AgentReposCompCount();
		soldeReposComp.setTotalMinutes(50);
		soldeReposComp.setTotalMinutesAnneeN1(20);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentReposCompCount.class, demande.getIdAgent())).thenReturn(
				soldeReposComp);

		IReposCompensateurRepository reposCompRepository = Mockito.mock(IReposCompensateurRepository.class);
		Mockito.when(reposCompRepository.getSommeDureeDemandeReposCompEnCoursSaisieouVisee(demande.getIdAgent(), null))
				.thenReturn(10);

		AbsReposCompensateurDataConsistencyRulesImpl impl = new AbsReposCompensateurDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "reposCompensateurRepository", reposCompRepository);
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);

		srm = impl.checkDepassementDroitsAcquis(srm, demande);

		assertEquals(0, srm.getErrors().size());
	}

	@Test
	public void checkDepassementDroitsAcquis_iKo() {

		ReturnMessageDto srm = new ReturnMessageDto();
		DemandeReposComp demande = new DemandeReposComp();
		demande.setIdAgent(9005138);
		demande.setDuree(59);

		AgentReposCompCount soldeReposComp = new AgentReposCompCount();
		soldeReposComp.setTotalMinutes(50);
		soldeReposComp.setTotalMinutesAnneeN1(20);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getAgentCounter(AgentReposCompCount.class, demande.getIdAgent())).thenReturn(
				soldeReposComp);

		IReposCompensateurRepository reposCompRepository = Mockito.mock(IReposCompensateurRepository.class);
		Mockito.when(reposCompRepository.getSommeDureeDemandeReposCompEnCoursSaisieouVisee(demande.getIdAgent(), null))
				.thenReturn(30);

		AbsReposCompensateurDataConsistencyRulesImpl impl = new AbsReposCompensateurDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "reposCompensateurRepository", reposCompRepository);
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);

		srm = impl.checkDepassementDroitsAcquis(srm, demande);

		assertEquals(1, srm.getErrors().size());
		assertEquals("Le dépassement des droits acquis n'est pas autorisé.", srm.getErrors().get(0).toString());
	}

	@Test
	public void checkStatutAgent_isNotFonctionnaire() {

		ReturnMessageDto srm = new ReturnMessageDto();
		Demande demande = new Demande();
		demande.setIdDemande(1);
		demande.setIdAgent(9005131);

		AgentGeneriqueDto ag = new AgentGeneriqueDto();
		ag.setNomatr(5131);

		Spcarr carr = new Spcarr();
		carr.setCdcate(4);

		Date date = new LocalDate(2013, 9, 29).toDate();

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(5131, date)).thenReturn(carr);

		IAgentMatriculeConverterService agentMatriculeServ = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeServ.fromIdAgentToSIRHNomatrAgent(demande.getIdAgent())).thenReturn(5131);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(date);

		AbsReposCompensateurDataConsistencyRulesImpl impl = new AbsReposCompensateurDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(impl, "helperService", helperService);
		ReflectionTestUtils.setField(impl, "agentMatriculeService", agentMatriculeServ);

		srm = impl.checkStatutAgent(srm, demande.getIdAgent(), false);

		assertEquals(0, srm.getErrors().size());
	}

	@Test
	public void checkStatutAgent_isFonctionnaireFromKiosque() {

		ReturnMessageDto srm = new ReturnMessageDto();
		Demande demande = new Demande();
		demande.setIdDemande(1);
		demande.setIdAgent(9005131);

		AgentGeneriqueDto ag = new AgentGeneriqueDto();
		ag.setNomatr(5131);

		Spcarr carr = new Spcarr();
		carr.setCdcate(2);

		Date date = new LocalDate(2013, 9, 29).toDate();

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(5131, date)).thenReturn(carr);

		IAgentMatriculeConverterService agentMatriculeServ = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeServ.fromIdAgentToSIRHNomatrAgent(demande.getIdAgent())).thenReturn(5131);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(date);

		AbsReposCompensateurDataConsistencyRulesImpl impl = new AbsReposCompensateurDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(impl, "helperService", helperService);
		ReflectionTestUtils.setField(impl, "agentMatriculeService", agentMatriculeServ);

		srm = impl.checkStatutAgent(srm, demande.getIdAgent(), false);

		assertEquals(1, srm.getErrors().size());
		assertEquals(
				"L'agent [9005131] ne peut pas avoir de repos compensateur. Les repos compensateurs sont pour les contractuels ou les conventions collectives.",
				srm.getErrors().get(0).toString());
	}

	@Test
	public void checkStatutAgent_isFonctionnaireFromSIRH() {

		ReturnMessageDto srm = new ReturnMessageDto();
		Demande demande = new Demande();
		demande.setIdDemande(1);
		demande.setIdAgent(9005131);

		AgentGeneriqueDto ag = new AgentGeneriqueDto();
		ag.setNomatr(5131);

		Spcarr carr = new Spcarr();
		carr.setCdcate(2);

		Date date = new LocalDate(2013, 9, 29).toDate();

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(5131, date)).thenReturn(carr);

		IAgentMatriculeConverterService agentMatriculeServ = Mockito.mock(IAgentMatriculeConverterService.class);
		Mockito.when(agentMatriculeServ.fromIdAgentToSIRHNomatrAgent(demande.getIdAgent())).thenReturn(5131);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(date);

		AbsReposCompensateurDataConsistencyRulesImpl impl = new AbsReposCompensateurDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(impl, "helperService", helperService);
		ReflectionTestUtils.setField(impl, "agentMatriculeService", agentMatriculeServ);

		srm = impl.checkStatutAgent(srm, demande.getIdAgent(), true);

		assertEquals(0, srm.getErrors().size());
	}

	@Test
	public void isAfficherBoutonImprimer() {
		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.REPOS_COMP.getValue());

		DemandeDto demandeDto = new DemandeDto();
		demandeDto.setIdRefEtat(RefEtatEnum.PROVISOIRE.getCodeEtat());
		demandeDto.setGroupeAbsence(groupeAbsence);

		AbsReposCompensateurDataConsistencyRulesImpl impl = new AbsReposCompensateurDataConsistencyRulesImpl();
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
		assertTrue(result);

		demandeDto.setIdRefEtat(RefEtatEnum.REJETE.getCodeEtat());
		result = impl.isAfficherBoutonImprimer(demandeDto);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());
		result = impl.isAfficherBoutonImprimer(demandeDto);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.REFUSEE.getCodeEtat());
		result = impl.isAfficherBoutonImprimer(demandeDto);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.EN_ATTENTE.getCodeEtat());
		result = impl.isAfficherBoutonImprimer(demandeDto);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.PRISE.getCodeEtat());
		result = impl.isAfficherBoutonImprimer(demandeDto);
		assertTrue(result);

		demandeDto.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
		result = impl.isAfficherBoutonImprimer(demandeDto);
		assertFalse(result);
	}

	@Test
	public void isAfficherBoutonAnnuler_isOperateur() {

		DemandeDto demandeDto = new DemandeDto();
		demandeDto.setIdRefEtat(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat());

		AbsReposCompensateurDataConsistencyRulesImpl impl = new AbsReposCompensateurDataConsistencyRulesImpl();
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
	}

	@Test
	public void isAfficherBoutonAnnuler_isNotOperateur() {

		DemandeDto demandeDto = new DemandeDto();
		demandeDto.setIdRefEtat(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat());

		AbsReposCompensateurDataConsistencyRulesImpl impl = new AbsReposCompensateurDataConsistencyRulesImpl();
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
	}
}
