package nc.noumea.mairie.abs.service.rules.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.recup.domain.AgentRecupCount;
import nc.noumea.mairie.abs.recup.domain.DemandeRecup;
import nc.noumea.mairie.abs.recup.repository.IRecuperationRepository;
import nc.noumea.mairie.abs.recup.service.rules.impl.AbsRecuperationDataConsistencyRulesImpl;
import nc.noumea.mairie.abs.repository.ICounterRepository;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.staticmock.MockStaticEntityMethods;
import org.springframework.test.util.ReflectionTestUtils;

@MockStaticEntityMethods
public class AbsRecuperationDataConsistencyRulesImplTest extends DefaultAbsenceDataConsistencyRulesImplTest  {
	
	@Test
	public void allTest() throws Throwable {
		super.impl = new AbsRecuperationDataConsistencyRulesImpl();
		super.allTest(impl);
		
		super.filtreDroitOfListeDemandesByDemande_Approbateur();
	}

	@Test
	public void filtreDroitOfListeDemandesByDemande_DemandeOfAgent() {

		super.impl = new AbsRecuperationDataConsistencyRulesImpl();
		super.filtreDroitOfListeDemandesByDemande_DemandeOfAgent();

		// Then
		// PROVISOIRE
		assertFalse(result1.isAffichageBoutonAnnuler());
		assertFalse(result1.isAffichageBoutonImprimer());
		// SAISIE
		assertFalse(result2.isAffichageBoutonAnnuler());
		assertFalse(result2.isAffichageBoutonImprimer());
		// APPROUVEE
		assertTrue(result3.isAffichageBoutonAnnuler());
		assertTrue(result3.isAffichageBoutonImprimer());
		// REFUSEE
		assertFalse(result4.isAffichageBoutonAnnuler());
		assertFalse(result4.isAffichageBoutonImprimer());
		// VISEE_FAVORABLE
		assertTrue(result5.isAffichageBoutonAnnuler());
		assertFalse(result5.isAffichageBoutonImprimer());
		// VISEE_DEFAVORABLE
		assertTrue(result6.isAffichageBoutonAnnuler());
		assertFalse(result6.isAffichageBoutonImprimer());
		// PRISE
		assertFalse(result7.isAffichageBoutonAnnuler());
		assertFalse(result7.isAffichageBoutonImprimer());
		// ANNULEE
		assertFalse(result8.isAffichageBoutonAnnuler());
		assertFalse(result8.isAffichageBoutonImprimer());
		// VALIDEE
		assertFalse(result9.isAffichageBoutonAnnuler());
		assertFalse(result9.isAffichageBoutonImprimer());
		// REJETE
		assertFalse(result10.isAffichageBoutonAnnuler());
		assertFalse(result10.isAffichageBoutonImprimer());
		// EN ATTENTE
		assertFalse(result11.isAffichageBoutonAnnuler());
		assertFalse(result11.isAffichageBoutonImprimer());
	}

	@Test
	public void filtreDroitOfListeDemandesByDemande_Operateur() {

		super.impl = new AbsRecuperationDataConsistencyRulesImpl();
		super.filtreDroitOfListeDemandesByDemande_Operateur();

		// Then
		// PROVISOIRE
		assertFalse(result1.isAffichageBoutonAnnuler());
		assertFalse(result1.isAffichageBoutonImprimer());
		// SAISIE
		assertFalse(result2.isAffichageBoutonAnnuler());
		assertFalse(result2.isAffichageBoutonImprimer());
		// APPROUVEE
		assertTrue(result3.isAffichageBoutonAnnuler());
		assertTrue(result3.isAffichageBoutonImprimer());
		// REFUSEE
		assertFalse(result4.isAffichageBoutonAnnuler());
		assertFalse(result4.isAffichageBoutonImprimer());
		// VISEE_FAVORABLE
		assertTrue(result5.isAffichageBoutonAnnuler());
		assertFalse(result5.isAffichageBoutonImprimer());
		// VISEE_DEFAVORABLE
		assertTrue(result6.isAffichageBoutonAnnuler());
		assertFalse(result6.isAffichageBoutonImprimer());
		// PRISE
		assertFalse(result7.isAffichageBoutonAnnuler());
		assertFalse(result7.isAffichageBoutonImprimer());
		// ANNULEE
		assertFalse(result8.isAffichageBoutonAnnuler());
		assertFalse(result8.isAffichageBoutonImprimer());
		// VALIDEE
		assertFalse(result9.isAffichageBoutonAnnuler());
		assertFalse(result9.isAffichageBoutonImprimer());
		// REJETE
		assertFalse(result10.isAffichageBoutonAnnuler());
		assertFalse(result10.isAffichageBoutonImprimer());
		// EN ATTENTE
		assertFalse(result11.isAffichageBoutonAnnuler());
		assertFalse(result11.isAffichageBoutonImprimer());
	}
	
	@Test
	public void filtreDroitOfDemandeSIRH() {
		
		super.impl = new AbsRecuperationDataConsistencyRulesImpl();
		super.filtreDroitOfDemandeSIRH();
		
		// APPROUVEE
		assertFalse(result3.isAffichageValidation());
		assertFalse(result3.isModifierValidation());
		
		// PRISE
		assertFalse(result7.isAffichageBoutonAnnuler());
		assertFalse(result7.isAffichageValidation());
		
		// VALIDEE
		assertFalse(result9.isAffichageBoutonAnnuler());
		assertFalse(result9.isAffichageValidation());
		
		// REJETEE
		assertFalse(result10.isAffichageValidation());
		
		// EN ATTENTE
		assertFalse(result11.isAffichageBoutonAnnuler());
		assertFalse(result11.isAffichageValidation());
		assertFalse(result11.isModifierValidation());
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
		
		DemandeDto demandeDto = new DemandeDto();
			demandeDto.setIdRefEtat(RefEtatEnum.PROVISOIRE.getCodeEtat());
			
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
		assertFalse(result);
		
		demandeDto.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
		result = impl.isAfficherBoutonImprimer(demandeDto);
		assertFalse(result);
	}
}
