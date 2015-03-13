package nc.noumea.mairie.abs.service.rules.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import nc.noumea.mairie.abs.domain.AgentRecupCount;
import nc.noumea.mairie.abs.domain.DemandeRecup;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeGroupeAbsenceEnum;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.RefGroupeAbsenceDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.ICounterRepository;
import nc.noumea.mairie.abs.repository.IRecuperationRepository;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.staticmock.MockStaticEntityMethods;
import org.springframework.test.util.ReflectionTestUtils;

@MockStaticEntityMethods
public class AbsRecuperationDataConsistencyRulesImplTest extends DefaultAbsenceDataConsistencyRulesImplTest {

	@Test
	public void allTest() throws Throwable {
		super.impl = new AbsRecuperationDataConsistencyRulesImpl();
		super.allTest(impl);
	}

	@Test
	public void filtreDroitOfListeDemandesByDemande_DemandeOfAgent() {

		super.impl = new AbsRecuperationDataConsistencyRulesImpl();
		super.filtreDroitOfListeDemandesByDemande_DemandeOfAgent();

		// Then
		super.checkBoutonAnnuler_filtreDroitOfListeDemandesByDemande_DemandeOfAgent();
	}

	@Test
	public void filtreDroitOfListeDemandesByDemande_Operateur() {

		super.impl = new AbsRecuperationDataConsistencyRulesImpl();
		super.filtreDroitOfListeDemandesByDemande_Operateur();

		// Then
		super.checkBoutonAnnuler_filtreDroitOfListeDemandesByDemande_Operateur();
	}
	
	@Test
	public void filtreDroitOfListeDemandesByDemande_Approbateur() {

		super.impl = new AbsRecuperationDataConsistencyRulesImpl();
		super.filtreDroitOfListeDemandesByDemande_Approbateur();

		// Then
		super.checkBoutonAnnuler_filtreDroitOfListeDemandesByDemande_Approbateur();
	}
	
	@Test
	public void filtreDroitOfListeDemandesByDemande_Delegataire() {

		super.impl = new AbsRecuperationDataConsistencyRulesImpl();
		super.filtreDroitOfListeDemandesByDemande_Delegataire();

		// Then
		super.checkBoutonAnnuler_filtreDroitOfListeDemandesByDemande_Delegataire();
	}
	
	@Test
	public void filtreDroitOfListeDemandesByDemande_Operateur_ET_ApprobateurSameGroup() {

		super.impl = new AbsRecuperationDataConsistencyRulesImpl();
		super.filtreDroitOfListeDemandesByDemande_Operateur_ET_ApprobateurSameGroup();

		// Then
		super.checkBoutonAnnuler_filtreDroitOfListeDemandesByDemande_Approbateur();
	}

	@Test
	public void filtreDroitOfDemandeSIRH() {

		super.impl = new AbsRecuperationDataConsistencyRulesImpl();
		super.filtreDroitOfDemandeSIRH();

		// APPROUVEE
		assertFalse(result3.isAffichageValidation());
		assertFalse(result3.isModifierValidation());
		assertFalse(result3.isAffichageEnAttente());

		// PRISE
		assertFalse(result7.isAffichageBoutonAnnuler());
		assertFalse(result7.isAffichageValidation());
		assertFalse(result7.isAffichageEnAttente());

		// VALIDEE
		assertFalse(result9.isAffichageBoutonAnnuler());
		assertFalse(result9.isAffichageValidation());
		assertFalse(result9.isAffichageEnAttente());

		// REJETEE
		assertFalse(result10.isAffichageValidation());
		assertFalse(result10.isAffichageEnAttente());

		// EN ATTENTE
		assertFalse(result11.isAffichageBoutonAnnuler());
		assertFalse(result11.isAffichageValidation());
		assertFalse(result11.isModifierValidation());
		assertFalse(result11.isAffichageEnAttente());
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
		Mockito.when(counterRepository.getAgentCounter(AgentRecupCount.class, demande.getIdAgent())).thenReturn(
				soldeRecup);

		IRecuperationRepository recuperationRepository = Mockito.mock(IRecuperationRepository.class);
		Mockito.when(recuperationRepository.getSommeDureeDemandeRecupEnCoursSaisieouVisee(demande.getIdAgent(), null))
				.thenReturn(10);

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
		Mockito.when(counterRepository.getAgentCounter(AgentRecupCount.class, demande.getIdAgent())).thenReturn(
				soldeRecup);

		IRecuperationRepository recuperationRepository = Mockito.mock(IRecuperationRepository.class);
		Mockito.when(recuperationRepository.getSommeDureeDemandeRecupEnCoursSaisieouVisee(demande.getIdAgent(), null))
				.thenReturn(10);

		AbsRecuperationDataConsistencyRulesImpl impl = new AbsRecuperationDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "recuperationRepository", recuperationRepository);
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);

		srm = impl.checkDepassementDroitsAcquis(srm, demande);

		assertEquals(1, srm.getErrors().size());
		assertEquals("Le dépassement des droits acquis n'est pas autorisé.", srm.getErrors().get(0).toString());
	}

	@Test
	public void isAfficherBoutonImprimer() {
		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.RECUP.getValue());
		DemandeDto demandeDto = new DemandeDto();
		demandeDto.setIdRefEtat(RefEtatEnum.PROVISOIRE.getCodeEtat());
		demandeDto.setGroupeAbsence(groupeAbsence);

		AbsRecuperationDataConsistencyRulesImpl impl = new AbsRecuperationDataConsistencyRulesImpl();
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
}
