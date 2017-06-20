package nc.noumea.mairie.abs.service.rules.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeMaladies;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.Profil;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.domain.RefTypeGroupeAbsenceEnum;
import nc.noumea.mairie.abs.domain.RefTypeSaisi;
import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.RefGroupeAbsenceDto;
import nc.noumea.mairie.abs.dto.RefTypeSaisiDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.abs.service.IAgentMatriculeConverterService;
import nc.noumea.mairie.abs.service.ICounterService;
import nc.noumea.mairie.abs.service.impl.HelperService;
import nc.noumea.mairie.abs.vo.CalculDroitsMaladiesVo;
import nc.noumea.mairie.abs.vo.CheckCompteurAgentVo;
import nc.noumea.mairie.ws.IPtgWsConsumer;
import nc.noumea.mairie.ws.ISirhWSConsumer;

import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.staticmock.MockStaticEntityMethods;
import org.springframework.test.util.ReflectionTestUtils;

@MockStaticEntityMethods
public class AbsMaladiesDataConsistencyRulesImplTest extends DefaultAbsenceDataConsistencyRulesImplTest {

	protected AbsMaladiesDataConsistencyRulesImpl	impl	= new AbsMaladiesDataConsistencyRulesImpl();

	@Test
	public void testMethodeParenteHeritage() throws Throwable {
		super.impl = new AbsMaladiesDataConsistencyRulesImpl();
		super.allTest(impl);
	}

	@Test
	public void filtreDroitOfListeDemandesByDemande_DemandeOfAgent() {

		super.impl = new AbsMaladiesDataConsistencyRulesImpl();
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
		assertFalse(result5.isAffichageBoutonAnnuler());
		// VISEE_DEFAVORABLE
		assertFalse(result6.isAffichageBoutonAnnuler());
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

		super.impl = new AbsMaladiesDataConsistencyRulesImpl();
		super.filtreDroitOfListeDemandesByDemande_Operateur();

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
		assertFalse(result5.isAffichageBoutonAnnuler());
		// VISEE_DEFAVORABLE
		assertFalse(result6.isAffichageBoutonAnnuler());
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
	public void filtreDroitOfListeDemandesByDemande_Approbateur() {

		super.impl = new AbsMaladiesDataConsistencyRulesImpl();
		super.filtreDroitOfListeDemandesByDemande_Approbateur();

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
		assertFalse(result5.isAffichageBoutonAnnuler());
		// VISEE_DEFAVORABLE
		assertFalse(result6.isAffichageBoutonAnnuler());
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
	public void filtreDroitOfListeDemandesByDemande_Delegataire() {

		super.impl = new AbsMaladiesDataConsistencyRulesImpl();
		super.filtreDroitOfListeDemandesByDemande_Delegataire();

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
		assertFalse(result5.isAffichageBoutonAnnuler());
		// VISEE_DEFAVORABLE
		assertFalse(result6.isAffichageBoutonAnnuler());
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
	public void filtreDroitOfListeDemandesByDemande_Operateur_ET_ApprobateurSameGroup() {

		super.impl = new AbsMaladiesDataConsistencyRulesImpl();
		super.filtreDroitOfListeDemandesByDemande_Operateur_ET_ApprobateurSameGroup();

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
		assertFalse(result5.isAffichageBoutonAnnuler());
		// VISEE_DEFAVORABLE
		assertFalse(result6.isAffichageBoutonAnnuler());
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

	protected void checkBoutonSupprimer() {
		// A VALIDER
		assertTrue(result12.isAffichageBoutonSupprimer());
	}

	protected void checkBoutonSupprimer_ForAgent() {
		assertFalse(result1.isAffichageBoutonSupprimer());
		assertFalse(result2.isAffichageBoutonSupprimer());
	}

	@Test
	public void checkChampMotif_ok_motifNonObligatoire() {

		ReturnMessageDto srm = new ReturnMessageDto();

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setMotif(false);

		RefTypeAbsence type = new RefTypeAbsence();
		type.setTypeSaisi(typeSaisi);

		DemandeMaladies demande = new DemandeMaladies();
		demande.setCommentaire(null);
		demande.setType(type);

		srm = impl.checkChampMotif(srm, demande);

		assertEquals(0, srm.getErrors().size());
	}

	@Test
	public void checkChampMotif_ok_motifSaisi() {

		ReturnMessageDto srm = new ReturnMessageDto();

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setMotif(true);

		RefTypeAbsence type = new RefTypeAbsence();
		type.setTypeSaisi(typeSaisi);

		DemandeMaladies demande = new DemandeMaladies();
		demande.setCommentaire("commentaire");
		demande.setType(type);

		srm = impl.checkChampMotif(srm, demande);

		assertEquals(0, srm.getErrors().size());
	}

	@Test
	public void checkChampMotif_ko_motifNull() {

		ReturnMessageDto srm = new ReturnMessageDto();

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setMotif(true);

		RefTypeAbsence type = new RefTypeAbsence();
		type.setTypeSaisi(typeSaisi);

		DemandeMaladies demande = new DemandeMaladies();
		demande.setCommentaire(null);
		demande.setType(type);

		srm = impl.checkChampMotif(srm, demande);

		assertEquals(1, srm.getErrors().size());
		assertEquals(srm.getErrors().get(0), AbsMaladiesDataConsistencyRulesImpl.CHAMP_COMMENTAIRE_OBLIGATOIRE);
	}

	@Test
	public void checkChampMotif_ko_motifVide() {

		ReturnMessageDto srm = new ReturnMessageDto();

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setMotif(true);

		RefTypeAbsence type = new RefTypeAbsence();
		type.setTypeSaisi(typeSaisi);

		DemandeMaladies demande = new DemandeMaladies();
		demande.setCommentaire("");
		demande.setType(type);

		srm = impl.checkChampMotif(srm, demande);

		assertEquals(1, srm.getErrors().size());
		assertEquals(srm.getErrors().get(0), AbsMaladiesDataConsistencyRulesImpl.CHAMP_COMMENTAIRE_OBLIGATOIRE);
	}

	@Test
	public void isAfficherBoutonImprimer() {
		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_EXCEP.getValue());

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

	@Test
	public void isAfficherBoutonAnnuler_isOperateur() {

		DemandeDto demandeDto = new DemandeDto();
		demandeDto.setIdRefEtat(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat());

		boolean result = impl.isAfficherBoutonAnnuler(demandeDto, true, false);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, true, false);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, true, false);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, true, false);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.EN_ATTENTE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, true, false);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.PRISE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, true, false);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.PROVISOIRE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, true, false);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, true, false);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.REFUSEE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, true, false);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.REJETE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, true, false);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, true, false);
		assertFalse(result);
	}

	@Test
	public void isAfficherBoutonAnnuler_isNotOperateur() {

		DemandeDto demandeDto = new DemandeDto();
		demandeDto.setIdRefEtat(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat());

		boolean result = impl.isAfficherBoutonAnnuler(demandeDto, false, false);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, false, false);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, false, false);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, false, false);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.EN_ATTENTE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, false, false);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.PRISE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, false, false);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.PROVISOIRE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, false, false);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, false, false);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.REFUSEE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, false, false);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.REJETE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, false, false);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto, false, false);
		assertFalse(result);
	}

	@Test
	public void isAfficherBoutonModifier_isNotOperateur() {

		Profil currentProfil = new Profil();
		currentProfil.setModification(false);

		DemandeDto demandeDto = new DemandeDto();
		demandeDto.setIdRefEtat(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat());

		boolean result = impl.isAfficherBoutonModifier(demandeDto, false, currentProfil);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat());
		result = impl.isAfficherBoutonModifier(demandeDto, false, currentProfil);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());
		result = impl.isAfficherBoutonModifier(demandeDto, false, currentProfil);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());
		result = impl.isAfficherBoutonModifier(demandeDto, false, currentProfil);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.EN_ATTENTE.getCodeEtat());
		result = impl.isAfficherBoutonModifier(demandeDto, false, currentProfil);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.PRISE.getCodeEtat());
		result = impl.isAfficherBoutonModifier(demandeDto, false, currentProfil);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.PROVISOIRE.getCodeEtat());
		result = impl.isAfficherBoutonModifier(demandeDto, false, currentProfil);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());
		result = impl.isAfficherBoutonModifier(demandeDto, false, currentProfil);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.REFUSEE.getCodeEtat());
		result = impl.isAfficherBoutonModifier(demandeDto, false, currentProfil);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.REJETE.getCodeEtat());
		result = impl.isAfficherBoutonModifier(demandeDto, false, currentProfil);
		assertFalse(result);

		demandeDto.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
		result = impl.isAfficherBoutonModifier(demandeDto, false, currentProfil);
		assertFalse(result);
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
		demande.setDateDebut(new Date());
		EtatDemande etat = new EtatDemande();
		etat.setEtat(RefEtatEnum.EN_ATTENTE);
		demande.getEtatsDemande().add(etat);

		srm = impl.checkEtatsDemandeAnnulee(srm, demande, Arrays.asList(RefEtatEnum.PROVISOIRE, RefEtatEnum.SAISIE));

		assertEquals(1, srm.getErrors().size());
	}

	@Test
	public void checkEtatsDemandeAnnulee_isPrise() {

		ReturnMessageDto srm = new ReturnMessageDto();
		Demande demande = new Demande();
		demande.setIdDemande(1);
		demande.setDateDebut(new Date());
		EtatDemande etat = new EtatDemande();
		etat.setEtat(RefEtatEnum.PRISE);
		demande.getEtatsDemande().add(etat);

		srm = impl.checkEtatsDemandeAnnulee(srm, demande, Arrays.asList(RefEtatEnum.PROVISOIRE, RefEtatEnum.SAISIE));

		assertEquals(1, srm.getErrors().size());
	}

	@Test
	public void checkEtatsDemandeAnnulee_isRejete() {

		ReturnMessageDto srm = new ReturnMessageDto();
		Demande demande = new Demande();
		demande.setIdDemande(1);
		demande.setIdAgent(21);
		demande.setDateDebut(new DateTime(2010, 01, 01, 0, 0, 0).toDate());
		EtatDemande etat = new EtatDemande();
		etat.setEtat(RefEtatEnum.PRISE);
		demande.getEtatsDemande().add(etat);

		srm = impl.checkEtatsDemandeAnnulee(srm, demande, Arrays.asList(RefEtatEnum.PROVISOIRE, RefEtatEnum.SAISIE));

		assertEquals(1, srm.getErrors().size());
	}

	@Test
	public void filtreDroitOfDemandeSIRH() {

		super.impl = new AbsMaladiesDataConsistencyRulesImpl();
		super.filtreDroitOfDemandeSIRH();

		// SAISIE
		assertFalse(result2.isAffichageValidation());

		// APPROUVEE
		assertFalse(result3.isAffichageValidation());
		assertFalse(result3.isAffichageEnAttente());

		// REFUSEE
		assertFalse(result4.isAffichageValidation());

		// VISEE FAVORABLE
		assertFalse(result5.isAffichageValidation());

		// VISEE DEFAVORABLE
		assertFalse(result6.isAffichageValidation());

		// PRISE
		assertTrue(result7.isAffichageBoutonAnnuler());
		assertFalse(result7.isAffichageValidation());
		assertFalse(result7.isAffichageEnAttente());

		// ANNULEE
		assertTrue(result8.isAffichageBoutonDupliquer());

		// VALIDEE
		assertTrue(result9.isAffichageBoutonAnnuler());
		assertFalse(result9.isAffichageValidation());
		assertFalse(result9.isAffichageEnAttente());

		// REJETEE
		assertTrue(result10.isAffichageBoutonAnnuler());
		assertFalse(result10.isAffichageValidation());
		assertFalse(result10.isAffichageEnAttente());

		// EN ATTENTE
		assertFalse(result11.isAffichageBoutonAnnuler());
		assertFalse(result11.isAffichageValidation());
		assertFalse(result11.isAffichageEnAttente());

		// A VALIDER
		assertFalse(result12.isAffichageBoutonAnnuler());
		assertTrue(result12.isAffichageValidation());
		assertFalse(result12.isAffichageEnAttente());
	}

	@Test
	public void checkEtatDemandePourDepassementCompteurAgent() {

		DemandeDto demandeDtoPROVISOIRE = new DemandeDto();
		demandeDtoPROVISOIRE.setIdDemande(1);
		demandeDtoPROVISOIRE.setIdRefEtat(RefEtatEnum.PROVISOIRE.getCodeEtat());
		DemandeDto demandeDtoREJETE = new DemandeDto();
		demandeDtoREJETE.setIdDemande(2);
		demandeDtoREJETE.setIdRefEtat(RefEtatEnum.REJETE.getCodeEtat());
		DemandeDto demandeDtoANNULEE = new DemandeDto();
		demandeDtoANNULEE.setIdDemande(3);
		demandeDtoANNULEE.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
		DemandeDto demandeDtoPRISE = new DemandeDto();
		demandeDtoPRISE.setIdDemande(4);
		demandeDtoPRISE.setIdRefEtat(RefEtatEnum.PRISE.getCodeEtat());

		List<DemandeDto> listDto = new ArrayList<DemandeDto>();
		listDto.addAll(Arrays.asList(demandeDtoPROVISOIRE, demandeDtoREJETE, demandeDtoANNULEE, demandeDtoPRISE));

		for (DemandeDto demandeDto : listDto) {
			if (impl.checkEtatDemandePourDepassementCompteurAgent(demandeDto)) {
				fail("Bad Etat Demande for checkDepassementCompteurAgent");
			}
		}

		DemandeDto demandeDtoSAISIE = new DemandeDto();
		demandeDtoSAISIE.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());
		DemandeDto demandeDtoVISEE_FAVORABLE = new DemandeDto();
		demandeDtoVISEE_FAVORABLE.setIdRefEtat(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat());
		DemandeDto demandeDtoVISEE_DEFAVORABLE = new DemandeDto();
		demandeDtoVISEE_DEFAVORABLE.setIdRefEtat(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat());
		DemandeDto demandeDtoAPPROUVEE = new DemandeDto();
		demandeDtoAPPROUVEE.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());
		DemandeDto demandeDtoEN_ATTENTE = new DemandeDto();
		demandeDtoEN_ATTENTE.setIdRefEtat(RefEtatEnum.EN_ATTENTE.getCodeEtat());
		DemandeDto demandeDtoREFUSEE = new DemandeDto();
		demandeDtoREFUSEE.setIdRefEtat(RefEtatEnum.REFUSEE.getCodeEtat());
		DemandeDto demandeDtoVALIDEE = new DemandeDto();
		demandeDtoVALIDEE.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());

		listDto = new ArrayList<DemandeDto>();
		listDto.addAll(Arrays.asList(demandeDtoSAISIE, demandeDtoVISEE_FAVORABLE, demandeDtoVISEE_DEFAVORABLE, demandeDtoAPPROUVEE, demandeDtoEN_ATTENTE,
				demandeDtoVALIDEE, demandeDtoREFUSEE));

		for (DemandeDto demandeDto : listDto) {
			if (!impl.checkEtatDemandePourDepassementCompteurAgent(demandeDto)) {
				fail("Bad Etat Demande " + demandeDto.getIdRefEtat() + " for checkDepassementCompteurAgent");
			}
		}
	}

	protected void checkBoutonModifier_filtreDroitOfListeDemandesByDemande_delegataire() {
		// Then
		// PROVISOIRE
		assertTrue(result1.isAffichageBoutonModifier());
		// SAISIE
		assertTrue(result2.isAffichageBoutonModifier());
		// APPROUVEE
		assertFalse(result3.isAffichageBoutonModifier());
		// REFUSEE
		assertFalse(result4.isAffichageBoutonModifier());
		// VISEE_FAVORABLE
		assertFalse(result5.isAffichageBoutonModifier());
		// VISEE_DEFAVORABLE
		assertFalse(result6.isAffichageBoutonModifier());
		// PRISE
		assertFalse(result7.isAffichageBoutonModifier());
		// ANNULEE
		assertFalse(result8.isAffichageBoutonModifier());
		// VALIDEE
		assertFalse(result9.isAffichageBoutonModifier());
		// REJETE
		assertFalse(result10.isAffichageBoutonModifier());
		// EN ATTENTE
		assertFalse(result11.isAffichageBoutonModifier());
		// A VALIDER
		assertTrue(result12.isAffichageBoutonModifier());
	}

	protected void checkBoutonModifier_filtreDroitOfListeDemandesByDemande_demandeOfAgent() {
		// Then
		// PROVISOIRE
		assertFalse(result1.isAffichageBoutonModifier());
		// SAISIE
		assertFalse(result2.isAffichageBoutonModifier());
		// APPROUVEE
		assertFalse(result3.isAffichageBoutonModifier());
		// REFUSEE
		assertFalse(result4.isAffichageBoutonModifier());
		// VISEE_FAVORABLE
		assertFalse(result5.isAffichageBoutonModifier());
		// VISEE_DEFAVORABLE
		assertFalse(result6.isAffichageBoutonModifier());
		// PRISE
		assertFalse(result7.isAffichageBoutonModifier());
		// ANNULEE
		assertFalse(result8.isAffichageBoutonModifier());
		// VALIDEE
		assertFalse(result9.isAffichageBoutonModifier());
		// REJETE
		assertFalse(result10.isAffichageBoutonModifier());
		// EN ATTENTE
		assertFalse(result11.isAffichageBoutonModifier());
		// A VALIDER
		assertFalse(result12.isAffichageBoutonModifier());
	}

	protected void checkBoutonModifier_filtreDroitOfListeDemandesByDemande_approbateur() {
		// Then
		// PROVISOIRE
		assertTrue(result1.isAffichageBoutonModifier());
		// SAISIE
		assertTrue(result2.isAffichageBoutonModifier());
		// APPROUVEE
		assertFalse(result3.isAffichageBoutonModifier());
		// REFUSEE
		assertFalse(result4.isAffichageBoutonModifier());
		// VISEE_FAVORABLE
		assertFalse(result5.isAffichageBoutonModifier());
		// VISEE_DEFAVORABLE
		assertFalse(result6.isAffichageBoutonModifier());
		// PRISE
		assertFalse(result7.isAffichageBoutonModifier());
		// ANNULEE
		assertFalse(result8.isAffichageBoutonModifier());
		// VALIDEE
		assertFalse(result9.isAffichageBoutonModifier());
		// REJETE
		assertFalse(result10.isAffichageBoutonModifier());
		// EN ATTENTE
		assertFalse(result11.isAffichageBoutonModifier());
		// A VALIDER
		assertTrue(result12.isAffichageBoutonModifier());
	}

	protected void checkBoutonModifier_filtreDroitOfListeDemandesByDemande_operateur() {
		// Then
		// PROVISOIRE
		assertTrue(result1.isAffichageBoutonModifier());
		// SAISIE
		assertTrue(result2.isAffichageBoutonModifier());
		// APPROUVEE
		assertFalse(result3.isAffichageBoutonModifier());
		// REFUSEE
		assertFalse(result4.isAffichageBoutonModifier());
		// VISEE_FAVORABLE
		assertFalse(result5.isAffichageBoutonModifier());
		// VISEE_DEFAVORABLE
		assertFalse(result6.isAffichageBoutonModifier());
		// PRISE
		assertFalse(result7.isAffichageBoutonModifier());
		// ANNULEE
		assertFalse(result8.isAffichageBoutonModifier());
		// VALIDEE
		assertFalse(result9.isAffichageBoutonModifier());
		// REJETE
		assertFalse(result10.isAffichageBoutonModifier());
		// EN ATTENTE
		assertFalse(result11.isAffichageBoutonModifier());
		// A VALIDER
		assertTrue(result12.isAffichageBoutonModifier());
	}

	@Test
	public void checkSaisiNewTypeAbsence() {

		ReturnMessageDto srm = new ReturnMessageDto();
		RefTypeSaisi typeSaisi = new RefTypeSaisi();

		srm = impl.checkSaisiNewTypeAbsence(typeSaisi, null, srm);
		assertEquals(srm.getErrors().get(0), "La date de début est obligatoire.");

		typeSaisi.setCalendarDateDebut(true);
		srm.getErrors().clear();
		srm = impl.checkSaisiNewTypeAbsence(typeSaisi, null, srm);
		assertEquals(srm.getErrors().get(0), "Vous devez sélectionner au moins un statut d'agent.");

		typeSaisi.setConventionCollective(true);
		typeSaisi.setAlerte(true);
		srm.getErrors().clear();
		srm = impl.checkSaisiNewTypeAbsence(typeSaisi, null, srm);
		assertEquals(srm.getErrors().get(0), "Le message d'alerte est obligatoire si le champ Alerte est coché.");

		typeSaisi.setMessageAlerte("message");
		srm.getErrors().clear();
		srm = impl.checkSaisiNewTypeAbsence(typeSaisi, null, srm);
		assertEquals(srm.getErrors().size(), 0);
	}

	@Test
	public void checkDepassementCompteurAgent_checkEtatDemande() {

		AgentWithServiceDto agentWithServiceDto = new AgentWithServiceDto();
		agentWithServiceDto.setIdAgent(9005138);

		DemandeDto demandeDto = new DemandeDto();
		demandeDto.setAgentWithServiceDto(agentWithServiceDto);
		demandeDto.setIdDemande(1);
		CheckCompteurAgentVo checkCompteurAgentVo = new CheckCompteurAgentVo();

		CalculDroitsMaladiesVo vo = new CalculDroitsMaladiesVo();
		vo.setNombreJoursCoupePleinSalaire(1);
		ICounterService maladieCounterServiceImpl = Mockito.mock(ICounterService.class);
		Mockito.when(maladieCounterServiceImpl.calculDroitsMaladiesForDemandeMaladies(Mockito.anyInt(), Mockito.any(DemandeDto.class))).thenReturn(vo);

		ReflectionTestUtils.setField(impl, "maladieCounterServiceImpl", maladieCounterServiceImpl);

		demandeDto.setIdRefEtat(RefEtatEnum.PROVISOIRE.getCodeEtat());
		assertFalse(impl.checkDepassementCompteurAgent(demandeDto, checkCompteurAgentVo));

		demandeDto.setIdRefEtat(RefEtatEnum.REJETE.getCodeEtat());
		assertFalse(impl.checkDepassementCompteurAgent(demandeDto, checkCompteurAgentVo));

		demandeDto.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
		assertFalse(impl.checkDepassementCompteurAgent(demandeDto, checkCompteurAgentVo));

		demandeDto.setIdRefEtat(RefEtatEnum.PRISE.getCodeEtat());
		assertFalse(impl.checkDepassementCompteurAgent(demandeDto, checkCompteurAgentVo));

		demandeDto.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());
		assertTrue(impl.checkDepassementCompteurAgent(demandeDto, checkCompteurAgentVo));

		demandeDto.setIdRefEtat(RefEtatEnum.A_VALIDER.getCodeEtat());
		assertTrue(impl.checkDepassementCompteurAgent(demandeDto, checkCompteurAgentVo));

		demandeDto.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());
		assertTrue(impl.checkDepassementCompteurAgent(demandeDto, checkCompteurAgentVo));
	}

	@Test
	public void checkDepassementCompteurAgent_false() {

		AgentWithServiceDto agentWithServiceDto = new AgentWithServiceDto();
		agentWithServiceDto.setIdAgent(9005138);

		DemandeDto demandeDto = new DemandeDto();
		demandeDto.setAgentWithServiceDto(agentWithServiceDto);
		CheckCompteurAgentVo checkCompteurAgentVo = new CheckCompteurAgentVo();

		CalculDroitsMaladiesVo vo = new CalculDroitsMaladiesVo();
		vo.setNombreJoursCoupePleinSalaire(0);
		ICounterService maladieCounterServiceImpl = Mockito.mock(ICounterService.class);
		Mockito.when(maladieCounterServiceImpl.calculDroitsMaladiesForDemandeMaladies(Mockito.anyInt(), Mockito.any(DemandeDto.class))).thenReturn(vo);

		ReflectionTestUtils.setField(impl, "maladieCounterServiceImpl", maladieCounterServiceImpl);

		demandeDto.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());
		assertFalse(impl.checkDepassementCompteurAgent(demandeDto, checkCompteurAgentVo));

		demandeDto.setIdRefEtat(RefEtatEnum.A_VALIDER.getCodeEtat());
		assertFalse(impl.checkDepassementCompteurAgent(demandeDto, checkCompteurAgentVo));

		demandeDto.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());
		assertFalse(impl.checkDepassementCompteurAgent(demandeDto, checkCompteurAgentVo));
	}

	@Test
	public void processDataConsistencyDemande_AT() {

		ReturnMessageDto srm = new ReturnMessageDto();
		Integer idAgent = 9005138;

		RefTypeSaisi typeSaisi = new RefTypeSaisi();

		RefTypeAbsence type = new RefTypeAbsence();
		type.setTypeSaisi(typeSaisi);
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.MALADIE_AT.getValue());

		DemandeMaladies demande = new DemandeMaladies();
		demande.setType(type);
		demande.setIdAgent(9005138);

		boolean isProvenanceSIRH = false;

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(demande.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		HelperService helperService = Mockito.mock(HelperService.class);
		IPtgWsConsumer ptgWSConsumer = Mockito.mock(IPtgWsConsumer.class);
		Mockito.when(ptgWSConsumer.checkPointage(demande.getIdAgent(), demande.getDateDebut(), demande.getDateFin())).thenReturn(new ReturnMessageDto());

		ReflectionTestUtils.setField(impl, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(impl, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(impl, "agentMatriculeService", agentMatriculeService);
		ReflectionTestUtils.setField(impl, "helperService", helperService);
		ReflectionTestUtils.setField(impl, "ptgWSConsumer", ptgWSConsumer);

		impl.processDataConsistencyDemande(srm, idAgent, demande, isProvenanceSIRH);

		// on verifie qu on ne check pas les pointages pour les AT
		Mockito.verify(ptgWSConsumer, Mockito.never()).checkPointage(demande.getIdAgent(), demande.getDateDebut(), demande.getDateFin());
	}

	@Test
	public void processDataConsistencyDemande_RECHUTE_AT() {

		ReturnMessageDto srm = new ReturnMessageDto();
		Integer idAgent = 9005138;

		RefTypeSaisi typeSaisi = new RefTypeSaisi();

		RefTypeAbsence type = new RefTypeAbsence();
		type.setTypeSaisi(typeSaisi);
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.MALADIE_AT_RECHUTE.getValue());

		DemandeMaladies demande = new DemandeMaladies();
		demande.setType(type);
		demande.setIdAgent(9005138);

		boolean isProvenanceSIRH = false;

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(demande.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		HelperService helperService = Mockito.mock(HelperService.class);
		IPtgWsConsumer ptgWSConsumer = Mockito.mock(IPtgWsConsumer.class);
		Mockito.when(ptgWSConsumer.checkPointage(demande.getIdAgent(), demande.getDateDebut(), demande.getDateFin())).thenReturn(new ReturnMessageDto());

		ReflectionTestUtils.setField(impl, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(impl, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(impl, "agentMatriculeService", agentMatriculeService);
		ReflectionTestUtils.setField(impl, "helperService", helperService);
		ReflectionTestUtils.setField(impl, "ptgWSConsumer", ptgWSConsumer);

		impl.processDataConsistencyDemande(srm, idAgent, demande, isProvenanceSIRH);

		// on verifie qu on ne check pas les pointages pour les AT
		Mockito.verify(ptgWSConsumer, Mockito.never()).checkPointage(demande.getIdAgent(), demande.getDateDebut(), demande.getDateFin());
	}

	@Test
	public void processDataConsistencyDemande_otherType() {

		ReturnMessageDto srm = new ReturnMessageDto();
		Integer idAgent = 9005138;

		RefTypeSaisi typeSaisi = new RefTypeSaisi();

		RefTypeAbsence type = new RefTypeAbsence();
		type.setTypeSaisi(typeSaisi);
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.MALADIE.getValue());

		DemandeMaladies demande = new DemandeMaladies();
		demande.setType(type);
		demande.setIdAgent(9005138);

		boolean isProvenanceSIRH = false;

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(demande.getIdAgent())).thenReturn(new AgentGeneriqueDto());

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
		HelperService helperService = Mockito.mock(HelperService.class);
		IPtgWsConsumer ptgWSConsumer = Mockito.mock(IPtgWsConsumer.class);
		Mockito.when(ptgWSConsumer.checkPointage(demande.getIdAgent(), demande.getDateDebut(), demande.getDateFin())).thenReturn(new ReturnMessageDto());

		ReflectionTestUtils.setField(impl, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(impl, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(impl, "agentMatriculeService", agentMatriculeService);
		ReflectionTestUtils.setField(impl, "helperService", helperService);
		ReflectionTestUtils.setField(impl, "ptgWSConsumer", ptgWSConsumer);

		impl.processDataConsistencyDemande(srm, idAgent, demande, isProvenanceSIRH);

		// on verifie qu on ne check pas les pointages pour les AT
		Mockito.verify(sirhRepository, Mockito.times(1)).getAgentCurrentPosition(Mockito.anyInt(), Mockito.any(Date.class));
	}

	@Test
	public void checkNombreJoursITT_ko() {

		ReturnMessageDto srm = new ReturnMessageDto();

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setNombreITT(true);

		RefTypeAbsence type = new RefTypeAbsence();
		type.setTypeSaisi(typeSaisi);

		DemandeMaladies demande = new DemandeMaladies();
		demande.setCommentaire(null);
		demande.setType(type);
		demande.setNombreITT(2.0);
		demande.setDateDebut(new DateTime(2010, 01, 01, 0, 0, 0).toDate());
		demande.setDateFin(new DateTime(2010, 01, 01, 23, 59, 59).toDate());

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreJours(demande.getDateDebut(), demande.getDateFin())).thenReturn(1.0);
		ReflectionTestUtils.setField(impl, "helperService", helperService);

		srm = impl.checkNombreJoursITT(srm, demande);

		assertEquals(1, srm.getErrors().size());
		assertEquals(srm.getErrors().get(0), AbsMaladiesDataConsistencyRulesImpl.NB_JOURS_ITT_TROP_ELEVE);
	}

	@Test
	public void checkNombreJoursITT_ok() {

		ReturnMessageDto srm = new ReturnMessageDto();

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setNombreITT(true);

		RefTypeAbsence type = new RefTypeAbsence();
		type.setTypeSaisi(typeSaisi);

		DemandeMaladies demande = new DemandeMaladies();
		demande.setCommentaire(null);
		demande.setType(type);
		demande.setNombreITT(2.0);
		demande.setDateDebut(new DateTime(2010, 01, 01, 0, 0, 0).toDate());
		demande.setDateFin(new DateTime(2010, 01, 02, 23, 59, 59).toDate());

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreJours(demande.getDateDebut(), demande.getDateFin())).thenReturn(2.0);
		ReflectionTestUtils.setField(impl, "helperService", helperService);

		srm = impl.checkNombreJoursITT(srm, demande);

		assertEquals(0, srm.getInfos().size());
	}

	@Test
	public void checkDepassementITT_KO() {

		RefTypeSaisiDto typeSaisi = new RefTypeSaisiDto();
		typeSaisi.setNombreITT(true);

		AgentWithServiceDto agentWithServiceDto = new AgentWithServiceDto();
		agentWithServiceDto.setIdAgent(9005138);

		DemandeDto demandeDto = new DemandeDto();
		demandeDto.setAgentWithServiceDto(agentWithServiceDto);
		demandeDto.setDateDebut(new DateTime(2010, 01, 01, 0, 0, 0).toDate());
		demandeDto.setDateFin(new DateTime(2010, 01, 02, 23, 59, 59).toDate());
		demandeDto.setNombreITT(2.0);
		demandeDto.setTypeSaisi(typeSaisi);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreJours(demandeDto.getDateDebut(), demandeDto.getDateFin())).thenReturn(2.0);

		ReflectionTestUtils.setField(impl, "helperService", helperService);

		assertFalse(impl.checkDepassementITT(demandeDto));
	}

	@Test
	public void checkDepassementITT_OK() {

		RefTypeSaisiDto typeSaisi = new RefTypeSaisiDto();
		typeSaisi.setNombreITT(true);

		AgentWithServiceDto agentWithServiceDto = new AgentWithServiceDto();
		agentWithServiceDto.setIdAgent(9005138);

		DemandeDto demandeDto = new DemandeDto();
		demandeDto.setAgentWithServiceDto(agentWithServiceDto);
		demandeDto.setDateDebut(new DateTime(2010, 01, 01, 0, 0, 0).toDate());
		demandeDto.setDateFin(new DateTime(2010, 01, 01, 23, 59, 59).toDate());
		demandeDto.setNombreITT(2.0);
		demandeDto.setTypeSaisi(typeSaisi);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreJours(demandeDto.getDateDebut(), demandeDto.getDateFin())).thenReturn(1.0);

		ReflectionTestUtils.setField(impl, "helperService", helperService);

		assertTrue(impl.checkDepassementITT(demandeDto));
	}

	@Test
	public void checkDateFutur_ko() {

		ReturnMessageDto srm = new ReturnMessageDto();

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setNombreITT(true);

		RefTypeAbsence type = new RefTypeAbsence();
		type.setTypeSaisi(typeSaisi);

		DemandeMaladies demande = new DemandeMaladies();
		demande.setCommentaire(null);
		demande.setType(type);
		demande.setNombreITT(2.0);
		demande.setDateDebut(new DateTime(2050, 01, 01, 0, 0, 0).toDate());
		demande.setDateFin(new DateTime(2050, 01, 01, 23, 59, 59).toDate());

		srm = impl.checkDateFutur(srm, demande);

		assertEquals(1, srm.getErrors().size());
		assertEquals(srm.getErrors().get(0), AbsMaladiesDataConsistencyRulesImpl.DEMANDE_DATE_FUTUR_MSG);
	}

	@Test
	public void checkDateFutur_ok() {

		ReturnMessageDto srm = new ReturnMessageDto();

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setNombreITT(true);

		RefTypeAbsence type = new RefTypeAbsence();
		type.setTypeSaisi(typeSaisi);

		DemandeMaladies demande = new DemandeMaladies();
		demande.setCommentaire(null);
		demande.setType(type);
		demande.setNombreITT(2.0);
		demande.setDateDebut(new DateTime(2010, 01, 01, 0, 0, 0).toDate());
		demande.setDateFin(new DateTime(2010, 01, 02, 23, 59, 59).toDate());

		srm = impl.checkDateFutur(srm, demande);

		assertEquals(0, srm.getErrors().size());
	}

	@Test
	public void checkDateFutur_ok_WithDateJour() {

		ReturnMessageDto srm = new ReturnMessageDto();

		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setNombreITT(true);

		RefTypeAbsence type = new RefTypeAbsence();
		type.setTypeSaisi(typeSaisi);

		DemandeMaladies demande = new DemandeMaladies();
		demande.setCommentaire(null);
		demande.setType(type);
		demande.setNombreITT(2.0);
		DateTime dateDeb = new DateTime();
		dateDeb = dateDeb.hourOfDay().setCopy(0);
		dateDeb = dateDeb.minuteOfHour().setCopy(0);
		demande.setDateDebut(dateDeb.toDate());
		demande.setDateFin(new DateTime(2050, 01, 02, 23, 59, 59).toDate());

		srm = impl.checkDateFutur(srm, demande);

		assertEquals(0, srm.getErrors().size());
	}
}
