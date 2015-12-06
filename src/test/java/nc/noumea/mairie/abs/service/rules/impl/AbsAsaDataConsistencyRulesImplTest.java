package nc.noumea.mairie.abs.service.rules.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeAsa;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.OrganisationSyndicale;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeGroupeAbsenceEnum;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.RefGroupeAbsenceDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.mock.staticmock.MockStaticEntityMethods;

@MockStaticEntityMethods
public class AbsAsaDataConsistencyRulesImplTest extends DefaultAbsenceDataConsistencyRulesImplTest {

	protected AbsAsaDataConsistencyRulesImpl impl = new AbsAsaDataConsistencyRulesImpl();

	@Test
	public void allTest() throws Throwable {

		isAfficherBoutonImprimer();
		isAfficherBoutonAnnuler_isOperateur();
		isAfficherBoutonAnnuler_isNotOperateur();
		filtreDroitOfDemandeSIRH();
		checkEtatsDemandeAnnulee_isValidee();
		checkEtatsDemandeAnnulee_isAttente();
		checkEtatsDemandeAnnulee_isPrise();
		checkEtatsDemandeAnnulee_isRejete();
		checkOrganisationSyndicale_OSInexistant();
		checkOrganisationSyndicale_OSInactif();
		checkOrganisationSyndicale_ok();

		filtreDroitOfListeDemandesByDemande_DemandeOfAgent();
		filtreDroitOfListeDemandesByDemande_Operateur();
		filtreDroitOfListeDemandesByDemande_Approbateur();
		filtreDroitOfListeDemandesByDemande_Delegataire();
		filtreDroitOfListeDemandesByDemande_Operateur_ET_ApprobateurSameGroup();

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
		// SAISIE
		assertFalse(result2.isAffichageBoutonAnnuler());
		// APPROUVEE
		assertFalse(result3.isAffichageBoutonAnnuler());
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
		assertFalse(result9.isAffichageBoutonAnnuler());
		// REJETE
		assertFalse(result10.isAffichageBoutonAnnuler());
		// EN ATTENTE
		assertFalse(result11.isAffichageBoutonAnnuler());
		// A VALIDER
		assertFalse(result12.isAffichageBoutonAnnuler());
	}

	@Test
	public void filtreDroitOfListeDemandesByDemande_Operateur() {

		super.impl = new AbsAsaDataConsistencyRulesImpl();
		super.filtreDroitOfListeDemandesByDemande_Operateur();

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
		assertFalse(result12.isAffichageBoutonAnnuler());
	}

	@Test
	public void filtreDroitOfListeDemandesByDemande_Approbateur() {

		super.impl = new AbsAsaDataConsistencyRulesImpl();
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
		assertFalse(result12.isAffichageBoutonAnnuler());
	}

	@Test
	public void filtreDroitOfListeDemandesByDemande_Delegataire() {

		super.impl = new AbsAsaDataConsistencyRulesImpl();
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
		assertFalse(result12.isAffichageBoutonAnnuler());
	}

	@Test
	public void filtreDroitOfListeDemandesByDemande_Operateur_ET_ApprobateurSameGroup() {

		super.impl = new AbsAsaDataConsistencyRulesImpl();
		super.filtreDroitOfListeDemandesByDemande_Operateur_ET_ApprobateurSameGroup();

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
		assertFalse(result12.isAffichageBoutonAnnuler());
	}

	@Test
	public void isAfficherBoutonImprimer() {
		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

		DemandeDto demandeDto = new DemandeDto();
		demandeDto.setIdRefEtat(RefEtatEnum.PROVISOIRE.getCodeEtat());
		demandeDto.setGroupeAbsence(groupeAbsence);

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
		assertTrue(result);

		demandeDto.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
		result = impl.isAfficherBoutonImprimer(demandeDto);
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
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.A_VALIDER.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, false);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, false);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.EN_ATTENTE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, false);
		assertFalse(result);

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

	@Test
	public void filtreDroitOfDemandeSIRH() {

		super.impl = new AbsAsaDataConsistencyRulesImpl();
		super.filtreDroitOfDemandeSIRH();

		// APPROUVEE
		assertTrue(result3.isAffichageValidation());
		assertTrue(result3.isModifierValidation());
		assertTrue(result3.isAffichageEnAttente());

		// PRISE
		assertTrue(result7.isAffichageBoutonAnnuler());
		assertTrue(result7.isAffichageValidation());
		assertFalse(result7.isAffichageEnAttente());

		// VALIDEE
		assertTrue(result9.isAffichageBoutonAnnuler());
		assertTrue(result9.isAffichageValidation());
		assertFalse(result9.isAffichageEnAttente());

		// REJETEE
		assertTrue(result10.isAffichageValidation());
		assertFalse(result10.isAffichageEnAttente());

		// EN ATTENTE
		assertTrue(result11.isAffichageBoutonAnnuler());
		assertTrue(result11.isAffichageValidation());
		assertTrue(result11.isModifierValidation());
		assertFalse(result11.isAffichageEnAttente());
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
		demande.setIdAgent(21);
		demande.setDateDebut(new DateTime(2010, 01, 01, 0, 0, 0).toDate());
		EtatDemande etat = new EtatDemande();
		etat.setEtat(RefEtatEnum.REJETE);
		demande.getEtatsDemande().add(etat);

		srm = impl.checkEtatsDemandeAnnulee(srm, demande, Arrays.asList(RefEtatEnum.PROVISOIRE, RefEtatEnum.SAISIE));

		assertEquals(1, srm.getErrors().size());
	}

	@Test
	public void checkOrganisationSyndicale_OSInexistant() {

		ReturnMessageDto srm = new ReturnMessageDto();
		DemandeAsa demande = new DemandeAsa();

		impl.checkOrganisationSyndicale(srm, demande);

		assertEquals(AbsAsaDataConsistencyRulesImpl.OS_INEXISTANT, srm.getErrors().get(0));
	}

	@Test
	public void checkOrganisationSyndicale_OSInactif() {

		ReturnMessageDto srm = new ReturnMessageDto();

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setActif(false);

		DemandeAsa demande = new DemandeAsa();
		demande.setOrganisationSyndicale(organisationSyndicale);

		impl.checkOrganisationSyndicale(srm, demande);

		assertEquals(AbsAsaDataConsistencyRulesImpl.OS_INACTIVE, srm.getErrors().get(0));
	}

	@Test
	public void checkOrganisationSyndicale_ok() {

		ReturnMessageDto srm = new ReturnMessageDto();

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setActif(true);

		DemandeAsa demande = new DemandeAsa();
		demande.setOrganisationSyndicale(organisationSyndicale);

		impl.checkOrganisationSyndicale(srm, demande);

		assertEquals(0, srm.getErrors().size());
	}

	@Test
	public void checkEtatDemandePourDepassementCompteurAgent() {

		DemandeDto demandeDtoVALIDEE = new DemandeDto();
		demandeDtoVALIDEE.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());
		DemandeDto demandeDtoREJETE = new DemandeDto();
		demandeDtoREJETE.setIdRefEtat(RefEtatEnum.REJETE.getCodeEtat());
		DemandeDto demandeDtoANNULEE = new DemandeDto();
		demandeDtoANNULEE.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
		DemandeDto demandeDtoPRISE = new DemandeDto();
		demandeDtoPRISE.setIdRefEtat(RefEtatEnum.PRISE.getCodeEtat());

		List<DemandeDto> listDto = new ArrayList<DemandeDto>();
		listDto.addAll(Arrays.asList(demandeDtoVALIDEE, demandeDtoREJETE, demandeDtoANNULEE, demandeDtoPRISE));

		for (DemandeDto demandeDto : listDto) {
			if (impl.checkEtatDemandePourDepassementCompteurAgent(demandeDto)) {
				fail("Bad Etat Demande for checkDepassementCompteurAgent");
			}
		}

		DemandeDto demandeDtoPROVISOIRE = new DemandeDto();
		demandeDtoPROVISOIRE.setIdRefEtat(RefEtatEnum.PROVISOIRE.getCodeEtat());
		DemandeDto demandeDtoSAISIE = new DemandeDto();
		demandeDtoSAISIE.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());
		DemandeDto demandeDtoVISEE_FAVORABLE = new DemandeDto();
		demandeDtoVISEE_FAVORABLE.setIdRefEtat(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat());
		DemandeDto demandeDtoVISEE_DEFAVORABLE = new DemandeDto();
		demandeDtoVISEE_DEFAVORABLE.setIdRefEtat(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat());
		DemandeDto demandeDtoAPPROUVEE = new DemandeDto();
		demandeDtoAPPROUVEE.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());
		DemandeDto demandeDtoREFUSEE = new DemandeDto();
		demandeDtoREFUSEE.setIdRefEtat(RefEtatEnum.REFUSEE.getCodeEtat());
		DemandeDto demandeDtoEN_ATTENTE = new DemandeDto();
		demandeDtoEN_ATTENTE.setIdRefEtat(RefEtatEnum.EN_ATTENTE.getCodeEtat());

		listDto = new ArrayList<DemandeDto>();
		listDto.addAll(Arrays.asList(demandeDtoPROVISOIRE, demandeDtoSAISIE, demandeDtoVISEE_FAVORABLE, demandeDtoVISEE_DEFAVORABLE, demandeDtoAPPROUVEE, demandeDtoREFUSEE, demandeDtoEN_ATTENTE));

		for (DemandeDto demandeDto : listDto) {
			if (!impl.checkEtatDemandePourDepassementCompteurAgent(demandeDto)) {
				fail("Bad Etat Demande for checkDepassementCompteurAgent");
			}
		}
	}
}
