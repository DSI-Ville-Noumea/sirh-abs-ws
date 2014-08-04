package nc.noumea.mairie.abs.service.rules.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeCongesExceptionnels;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeSaisi;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

import org.junit.Test;
import org.springframework.mock.staticmock.MockStaticEntityMethods;

@MockStaticEntityMethods
public class AbsCongesExcepDataConsistencyRulesImplTest extends DefaultAbsenceDataConsistencyRulesImplTest {

	protected AbsCongesExcepDataConsistencyRulesImpl impl = new AbsCongesExcepDataConsistencyRulesImpl();
	
	@Test
	public void testMethodeParenteHeritage() throws Throwable {
		
		checkChampMotifDemandeSaisi_ok_motifNonObligatoire();
		checkChampMotifDemandeSaisi_ok_motifSaisi();
		checkChampMotifDemandeSaisi_ko_motifNull();
		checkChampMotifDemandeSaisi_ko_motifVide();
		isAfficherBoutonImprimer();
		isAfficherBoutonAnnuler_isOperateur();
		isAfficherBoutonAnnuler_isNotOperateur();
		checkEtatsDemandeAnnulee_isValidee();
		checkEtatsDemandeAnnulee_isAttente();
		checkEtatsDemandeAnnulee_isPrise();
		checkEtatsDemandeAnnulee_isRejete();
		
		super.impl = new AbsCongesExcepDataConsistencyRulesImpl();
		super.allTest(impl);
	}
	
	@Test
	public void checkChampMotifDemandeSaisi_ok_motifNonObligatoire() {
		
		ReturnMessageDto srm = new ReturnMessageDto();
		
		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setMotif(false);
		
		RefTypeAbsence type = new RefTypeAbsence();
		type.setTypeSaisi(typeSaisi);
		
		DemandeCongesExceptionnels demande = new DemandeCongesExceptionnels();
		demande.setCommentaire(null);
		demande.setType(type);
		
		srm = impl.checkChampMotifDemandeSaisi(srm, demande);
		
		assertEquals(0, srm.getErrors().size());
	}
	
	@Test
	public void checkChampMotifDemandeSaisi_ok_motifSaisi() {

		ReturnMessageDto srm = new ReturnMessageDto();
		
		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setMotif(true);
		
		RefTypeAbsence type = new RefTypeAbsence();
		type.setTypeSaisi(typeSaisi);
		
		DemandeCongesExceptionnels demande = new DemandeCongesExceptionnels();
		demande.setCommentaire("commentaire");
		demande.setType(type);
		
		srm = impl.checkChampMotifDemandeSaisi(srm, demande);
		
		assertEquals(0, srm.getErrors().size());
	}
	
	@Test
	public void checkChampMotifDemandeSaisi_ko_motifNull() {
		
		ReturnMessageDto srm = new ReturnMessageDto();
		
		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setMotif(true);
		
		RefTypeAbsence type = new RefTypeAbsence();
		type.setTypeSaisi(typeSaisi);
		
		DemandeCongesExceptionnels demande = new DemandeCongesExceptionnels();
		demande.setCommentaire(null);
		demande.setType(type);
		
		srm = impl.checkChampMotifDemandeSaisi(srm, demande);
		
		assertEquals(1, srm.getErrors().size());
		assertEquals(srm.getErrors().get(0), AbsCongesExcepDataConsistencyRulesImpl.CHAMP_COMMENTAIRE_OBLIGATOIRE);
	}
	
	@Test
	public void checkChampMotifDemandeSaisi_ko_motifVide() {
		
		ReturnMessageDto srm = new ReturnMessageDto();
		
		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setMotif(true);
		
		RefTypeAbsence type = new RefTypeAbsence();
		type.setTypeSaisi(typeSaisi);
		
		DemandeCongesExceptionnels demande = new DemandeCongesExceptionnels();
		demande.setCommentaire("");
		demande.setType(type);
		
		srm = impl.checkChampMotifDemandeSaisi(srm, demande);
		
		assertEquals(1, srm.getErrors().size());
		assertEquals(srm.getErrors().get(0), AbsCongesExcepDataConsistencyRulesImpl.CHAMP_COMMENTAIRE_OBLIGATOIRE);
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
