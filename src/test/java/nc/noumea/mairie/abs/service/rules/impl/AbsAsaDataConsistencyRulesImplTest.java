package nc.noumea.mairie.abs.service.rules.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

import org.junit.Test;
import org.springframework.mock.staticmock.MockStaticEntityMethods;

@MockStaticEntityMethods
public class AbsAsaDataConsistencyRulesImplTest extends DefaultAbsenceDataConsistencyRulesImplTest {

	protected AbsAsaDataConsistencyRulesImpl impl = new AbsAsaDataConsistencyRulesImpl();
	
	@Test
	public void allTest() throws Throwable {
		
		isAfficherBoutonImprimer();
		isAfficherBoutonAnnuler();
		filtreDroitOfDemandeSIRH();
		checkEtatsDemandeAnnulee_isValidee();
		checkEtatsDemandeAnnulee_isAttente();
		checkEtatsDemandeAnnulee_isPrise();
		checkEtatsDemandeAnnulee_isRejete();
		
		super.impl = impl;
		super.allTest(impl);
	}
	
	@Test
	public void filtreDroitOfListeDemandesByDemande_DemandeOfAgent() {

		super.impl = new AbsAsaDataConsistencyRulesImpl();
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
		assertFalse(result3.isAffichageBoutonImprimer());
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
		assertTrue(result7.isAffichageBoutonAnnuler());
		assertFalse(result7.isAffichageBoutonImprimer());
		// ANNULEE
		assertFalse(result8.isAffichageBoutonAnnuler());
		assertFalse(result8.isAffichageBoutonImprimer());
		// VALIDEE
		assertTrue(result9.isAffichageBoutonAnnuler());
		assertTrue(result9.isAffichageBoutonImprimer());
		// REJETE
		assertFalse(result10.isAffichageBoutonAnnuler());
		assertFalse(result10.isAffichageBoutonImprimer());
		// EN ATTENTE
		assertTrue(result11.isAffichageBoutonAnnuler());
		assertFalse(result11.isAffichageBoutonImprimer());
	}

	@Test
	public void filtreDroitOfListeDemandesByDemande_Operateur() {

		super.impl = new AbsAsaDataConsistencyRulesImpl();
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
		assertFalse(result3.isAffichageBoutonImprimer());
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
		assertTrue(result7.isAffichageBoutonAnnuler());
		assertFalse(result7.isAffichageBoutonImprimer());
		// ANNULEE
		assertFalse(result8.isAffichageBoutonAnnuler());
		assertFalse(result8.isAffichageBoutonImprimer());
		// VALIDEE
		assertTrue(result9.isAffichageBoutonAnnuler());
		assertTrue(result9.isAffichageBoutonImprimer());
		// REJETE
		assertFalse(result10.isAffichageBoutonAnnuler());
		assertFalse(result10.isAffichageBoutonImprimer());
		// EN ATTENTE
		assertTrue(result11.isAffichageBoutonAnnuler());
		assertFalse(result11.isAffichageBoutonImprimer());
	}
	
	@Test
	public void isAfficherBoutonImprimer() {
		
		DemandeDto demandeDto = new DemandeDto();
			demandeDto.setIdRefEtat(RefEtatEnum.PROVISOIRE.getCodeEtat());
		
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
		assertFalse(result);
		
		demandeDto.setIdRefEtat(RefEtatEnum.REJETE.getCodeEtat());
		result = impl.isAfficherBoutonImprimer(demandeDto);
		assertFalse(result);
		
		demandeDto.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());
		result = impl.isAfficherBoutonImprimer(demandeDto);
		assertTrue(result);
		
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
		assertTrue(result);
		
		demandeDto.setIdRefEtat(RefEtatEnum.EN_ATTENTE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto);
		assertTrue(result);
		
		demandeDto.setIdRefEtat(RefEtatEnum.PRISE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto);
		assertTrue(result);
		
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
	public void filtreDroitOfDemandeSIRH() {
		
		super.impl = new AbsAsaDataConsistencyRulesImpl();
		super.filtreDroitOfDemandeSIRH();
		
		// APPROUVEE
		assertTrue(result3.isAffichageValidation());
		assertTrue(result3.isModifierValidation());
		
		// PRISE
		assertTrue(result7.isAffichageBoutonAnnuler());
		assertTrue(result7.isAffichageValidation());
		
		// VALIDEE
		assertTrue(result9.isAffichageBoutonAnnuler());
		assertTrue(result9.isAffichageValidation());
		
		// REJETEE
		assertTrue(result10.isAffichageValidation());
		
		// EN ATTENTE
		assertTrue(result11.isAffichageBoutonAnnuler());
		assertTrue(result11.isAffichageValidation());
		assertTrue(result11.isModifierValidation());
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
