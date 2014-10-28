package nc.noumea.mairie.abs.service.rules.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeCongesExceptionnels;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeSaisi;
import nc.noumea.mairie.abs.domain.RefUnitePeriodeQuota;
import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.ICongesExceptionnelsRepository;
import nc.noumea.mairie.abs.repository.IDemandeRepository;
import nc.noumea.mairie.abs.service.impl.HelperService;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.staticmock.MockStaticEntityMethods;
import org.springframework.test.util.ReflectionTestUtils;

@MockStaticEntityMethods
public class AbsCongesExcepDataConsistencyRulesImplTest extends DefaultAbsenceDataConsistencyRulesImplTest {

	protected AbsCongesExcepDataConsistencyRulesImpl impl = new AbsCongesExcepDataConsistencyRulesImpl();
	
	@Test
	public void testMethodeParenteHeritage() throws Throwable {
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
		assertTrue(result);
		
		demandeDto.setIdRefEtat(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat());
		result = impl.isAfficherBoutonImprimer(demandeDto);
		assertTrue(result);
		
		demandeDto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());
		result = impl.isAfficherBoutonImprimer(demandeDto);
		assertTrue(result);
		
		demandeDto.setIdRefEtat(RefEtatEnum.REJETE.getCodeEtat());
		result = impl.isAfficherBoutonImprimer(demandeDto);
		assertTrue(result);
		
		demandeDto.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());
		result = impl.isAfficherBoutonImprimer(demandeDto);
		assertTrue(result);
		
		demandeDto.setIdRefEtat(RefEtatEnum.REFUSEE.getCodeEtat());
		result = impl.isAfficherBoutonImprimer(demandeDto);
		assertTrue(result);
		
		demandeDto.setIdRefEtat(RefEtatEnum.EN_ATTENTE.getCodeEtat());
		result = impl.isAfficherBoutonImprimer(demandeDto);
		assertTrue(result);
		
		demandeDto.setIdRefEtat(RefEtatEnum.PRISE.getCodeEtat());
		result = impl.isAfficherBoutonImprimer(demandeDto);
		assertTrue(result);
		
		demandeDto.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
		result = impl.isAfficherBoutonImprimer(demandeDto);
		assertTrue(result);
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
	
	@Test
	public void filtreDroitOfDemandeSIRH() {
		
		super.impl = new AbsCongesExcepDataConsistencyRulesImpl();
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
	public void checkDepassementCompteurAgent_quotaZero() {
		
		AgentWithServiceDto agentWithServiceDto = new AgentWithServiceDto();
		
		DemandeDto demandeDto = new DemandeDto();
			demandeDto.setIdTypeDemande(1);
			demandeDto.setAgentWithServiceDto(agentWithServiceDto);
		
		RefTypeSaisi typeSaisi = new RefTypeSaisi();
			typeSaisi.setQuotaMax(0);
		
		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(RefTypeSaisi.class, demandeDto.getIdTypeDemande())).thenReturn(
				typeSaisi);

		ReflectionTestUtils.setField(impl, "demandeRepository", demandeRepository);
		
		assertTrue(impl.checkDepassementCompteurAgent(demandeDto));
	}
	
	@Test
	public void checkDepassementCompteurAgent_false() {
		
		RefUnitePeriodeQuota refUnitePeriodeQuota = new RefUnitePeriodeQuota();
		
		AgentWithServiceDto agentWithServiceDto = new AgentWithServiceDto();
			agentWithServiceDto.setIdAgent(9005138);
		
		DemandeDto demandeDto = new DemandeDto();
			demandeDto.setIdTypeDemande(1);
			demandeDto.setDateDebut(new Date());
			demandeDto.setAgentWithServiceDto(agentWithServiceDto);
			demandeDto.setDuree(10.0);
		
		RefTypeSaisi typeSaisi = new RefTypeSaisi();
			typeSaisi.setQuotaMax(10);
			typeSaisi.setRefUnitePeriodeQuota(refUnitePeriodeQuota);
		
		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(RefTypeSaisi.class, demandeDto.getIdTypeDemande())).thenReturn(
				typeSaisi);

		Date fromDate = new Date();
		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getDateDebutByUnitePeriodeQuotaAndDebutDemande(
				typeSaisi.getRefUnitePeriodeQuota(), demandeDto.getDateDebut())).thenReturn(fromDate);
		
		ICongesExceptionnelsRepository congesExceptionnelsRepository = Mockito.mock(ICongesExceptionnelsRepository.class);
		Mockito.when(congesExceptionnelsRepository.countDureeByPeriodeAndTypeDemande(
				demandeDto.getAgentWithServiceDto().getIdAgent(), fromDate, demandeDto.getDateDebut(), demandeDto.getIdTypeDemande())).thenReturn(0.0);
		
		ReflectionTestUtils.setField(impl, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(impl, "helperService", helperService);
		ReflectionTestUtils.setField(impl, "congesExceptionnelsRepository", congesExceptionnelsRepository);
		
		assertFalse(impl.checkDepassementCompteurAgent(demandeDto));
	}
	
	@Test
	public void checkDepassementCompteurAgent_true() {
		
		RefUnitePeriodeQuota refUnitePeriodeQuota = new RefUnitePeriodeQuota();
		
		AgentWithServiceDto agentWithServiceDto = new AgentWithServiceDto();
			agentWithServiceDto.setIdAgent(9005138);
		
		DemandeDto demandeDto = new DemandeDto();
			demandeDto.setIdTypeDemande(1);
			demandeDto.setDateDebut(new Date());
			demandeDto.setAgentWithServiceDto(agentWithServiceDto);
			demandeDto.setDuree(10.0);
		
		RefTypeSaisi typeSaisi = new RefTypeSaisi();
			typeSaisi.setQuotaMax(10);
			typeSaisi.setRefUnitePeriodeQuota(refUnitePeriodeQuota);
		
		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(RefTypeSaisi.class, demandeDto.getIdTypeDemande())).thenReturn(
				typeSaisi);

		Date fromDate = new Date();
		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getDateDebutByUnitePeriodeQuotaAndDebutDemande(
				typeSaisi.getRefUnitePeriodeQuota(), demandeDto.getDateDebut())).thenReturn(fromDate);
		
		ICongesExceptionnelsRepository congesExceptionnelsRepository = Mockito.mock(ICongesExceptionnelsRepository.class);
		Mockito.when(congesExceptionnelsRepository.countDureeByPeriodeAndTypeDemande(
				demandeDto.getAgentWithServiceDto().getIdAgent(), fromDate, demandeDto.getDateDebut(), demandeDto.getIdTypeDemande())).thenReturn(1.0);
		
		ReflectionTestUtils.setField(impl, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(impl, "helperService", helperService);
		ReflectionTestUtils.setField(impl, "congesExceptionnelsRepository", congesExceptionnelsRepository);
		
		assertTrue(impl.checkDepassementCompteurAgent(demandeDto));
	}
	
	@Test
	public void checkMessageAlerteDepassementDroit_AlerteFalseEtDepasstCompteurTrue() {

		RefUnitePeriodeQuota refUnitePeriodeQuota = new RefUnitePeriodeQuota();
		
		RefTypeSaisi typeSaisiBis = new RefTypeSaisi();
			typeSaisiBis.setAlerte(true);
	
		RefTypeAbsence type = new RefTypeAbsence();
			type.setIdRefTypeAbsence(1);
			type.setTypeSaisi(typeSaisiBis);
		
		DemandeCongesExceptionnels demande = new DemandeCongesExceptionnels();
			demande.setType(type);
			demande.setDateDebut(new Date());
			demande.setIdAgent(9005138);
			demande.setDuree(10.0);
		
		RefTypeSaisi typeSaisi = new RefTypeSaisi();
			typeSaisi.setQuotaMax(10);
			typeSaisi.setRefUnitePeriodeQuota(refUnitePeriodeQuota);
		
		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(RefTypeSaisi.class, demande.getType().getIdRefTypeAbsence())).thenReturn(
				typeSaisi);

		Date fromDate = new Date();
		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getDateDebutByUnitePeriodeQuotaAndDebutDemande(
				typeSaisi.getRefUnitePeriodeQuota(), demande.getDateDebut())).thenReturn(fromDate);
		
		ICongesExceptionnelsRepository congesExceptionnelsRepository = Mockito.mock(ICongesExceptionnelsRepository.class);
		Mockito.when(congesExceptionnelsRepository.countDureeByPeriodeAndTypeDemande(
				demande.getIdAgent(), fromDate, demande.getDateDebut(), demande.getType().getIdRefTypeAbsence())).thenReturn(0.0);
		
		ReflectionTestUtils.setField(impl, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(impl, "helperService", helperService);
		ReflectionTestUtils.setField(impl, "congesExceptionnelsRepository", congesExceptionnelsRepository);
		
		ReturnMessageDto result = new ReturnMessageDto();
		result = impl.checkMessageAlerteDepassementDroit(result, demande);
		
		assertEquals(0, result.getErrors().size());
	}
	
	@Test
	public void checkMessageAlerteDepassementDroit_AlerteTrueEtDepasstCompteurTrue() {
		
		RefUnitePeriodeQuota refUnitePeriodeQuota = new RefUnitePeriodeQuota();
		
		RefTypeSaisi typeSaisiBis = new RefTypeSaisi();
			typeSaisiBis.setAlerte(true);
		
		RefTypeAbsence type = new RefTypeAbsence();
			type.setIdRefTypeAbsence(1);
			type.setTypeSaisi(typeSaisiBis);
		
		DemandeCongesExceptionnels demande = new DemandeCongesExceptionnels();
			demande.setType(type);
			demande.setDateDebut(new Date());
			demande.setIdAgent(9005138);
			demande.setDuree(10.0);
		
		RefTypeSaisi typeSaisi = new RefTypeSaisi();
			typeSaisi.setQuotaMax(10);
			typeSaisi.setRefUnitePeriodeQuota(refUnitePeriodeQuota);
		
		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(RefTypeSaisi.class, demande.getType().getIdRefTypeAbsence())).thenReturn(
				typeSaisi);

		Date fromDate = new Date();
		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getDateDebutByUnitePeriodeQuotaAndDebutDemande(
				typeSaisi.getRefUnitePeriodeQuota(), demande.getDateDebut())).thenReturn(fromDate);
		
		ICongesExceptionnelsRepository congesExceptionnelsRepository = Mockito.mock(ICongesExceptionnelsRepository.class);
		Mockito.when(congesExceptionnelsRepository.countDureeByPeriodeAndTypeDemande(
				demande.getIdAgent(), fromDate, demande.getDateDebut(), demande.getType().getIdRefTypeAbsence())).thenReturn(0.0);
		
		ReflectionTestUtils.setField(impl, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(impl, "helperService", helperService);
		ReflectionTestUtils.setField(impl, "congesExceptionnelsRepository", congesExceptionnelsRepository);
		
		ReturnMessageDto result = new ReturnMessageDto();
		result = impl.checkMessageAlerteDepassementDroit(result, demande);
	}
	
	@Test
	public void checkSaisiNewTypeAbsence() {
		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		
		ReturnMessageDto result = new ReturnMessageDto();
		result = impl.checkSaisiNewTypeAbsence(typeSaisi, result);
		
		assertEquals(3, result.getErrors().size());
		assertEquals(result.getErrors().get(0), "La date de début est obligatoire.");
		assertEquals(result.getErrors().get(1), "Vous devez sélectionner au moins un statut d'agent.");
		assertEquals(result.getErrors().get(2), "L'unité de décompte est obligatoire.");
	}
	
	@Test
	public void checkSaisiNewTypeAbsence_ErreurMotif() {
		
		RefUnitePeriodeQuota refUnitePeriodeQuota = new RefUnitePeriodeQuota();
		refUnitePeriodeQuota.setIdRefUnitePeriodeQuota(1);
		
		RefTypeSaisi typeSaisi = new RefTypeSaisi();
			typeSaisi.setCalendarDateDebut(true);
			typeSaisi.setFonctionnaire(true);
			typeSaisi.setRefUnitePeriodeQuota(refUnitePeriodeQuota);
			typeSaisi.setUniteDecompte(HelperService.UNITE_DECOMPTE_JOURS);
			typeSaisi.setQuotaMax(1);
			typeSaisi.setMotif(true);
		
		ReturnMessageDto result = new ReturnMessageDto();
		result = impl.checkSaisiNewTypeAbsence(typeSaisi, result);
		
		assertEquals(1, result.getErrors().size());
		assertEquals(result.getErrors().get(0), "Les informations complémentaires sont obligatoires si le champ Motif est coché.");
	}
	
	@Test
	public void checkSaisiNewTypeAbsence_ErreurAlerte() {
		
		RefUnitePeriodeQuota refUnitePeriodeQuota = new RefUnitePeriodeQuota();
		refUnitePeriodeQuota.setIdRefUnitePeriodeQuota(1);
		
		RefTypeSaisi typeSaisi = new RefTypeSaisi();
			typeSaisi.setCalendarDateDebut(true);
			typeSaisi.setFonctionnaire(true);
			typeSaisi.setRefUnitePeriodeQuota(refUnitePeriodeQuota);
			typeSaisi.setUniteDecompte(HelperService.UNITE_DECOMPTE_JOURS);
			typeSaisi.setQuotaMax(1);
			typeSaisi.setMotif(true);
			typeSaisi.setInfosComplementaires("");
			typeSaisi.setAlerte(true);
		
		ReturnMessageDto result = new ReturnMessageDto();
		result = impl.checkSaisiNewTypeAbsence(typeSaisi, result);
		
		assertEquals(1, result.getErrors().size());
		assertEquals(result.getErrors().get(0), "Le message d'alerte est obligatoire si le champ Alerte est coché.");
	}
	
	@Test
	public void checkSaisiNewTypeAbsence_ErreurDateFin() {
		
		RefUnitePeriodeQuota refUnitePeriodeQuota = new RefUnitePeriodeQuota();
		refUnitePeriodeQuota.setIdRefUnitePeriodeQuota(1);
		
		RefTypeSaisi typeSaisi = new RefTypeSaisi();
			typeSaisi.setCalendarDateDebut(true);
			typeSaisi.setFonctionnaire(true);
			typeSaisi.setRefUnitePeriodeQuota(refUnitePeriodeQuota);
			typeSaisi.setUniteDecompte(HelperService.UNITE_DECOMPTE_JOURS);
			typeSaisi.setQuotaMax(1);
			typeSaisi.setMotif(true);
			typeSaisi.setInfosComplementaires("");
			typeSaisi.setAlerte(true);
			typeSaisi.setMessageAlerte("");
			typeSaisi.setChkDateDebut(true);
			typeSaisi.setChkDateFin(true);
		
		ReturnMessageDto result = new ReturnMessageDto();
		result = impl.checkSaisiNewTypeAbsence(typeSaisi, result);
		
		assertEquals(1, result.getErrors().size());
		assertEquals(result.getErrors().get(0), "La date de fin est obligatoire si le radio bouton Matin/Après-midi de fin ou l'heure de fin sont sélectionnés.");
	}
	
	@Test
	public void checkSaisiNewTypeAbsence_ErreurDateFin_bis() {
		
		RefUnitePeriodeQuota refUnitePeriodeQuota = new RefUnitePeriodeQuota();
		refUnitePeriodeQuota.setIdRefUnitePeriodeQuota(1);
		
		RefTypeSaisi typeSaisi = new RefTypeSaisi();
			typeSaisi.setCalendarDateDebut(true);
			typeSaisi.setFonctionnaire(true);
			typeSaisi.setRefUnitePeriodeQuota(refUnitePeriodeQuota);
			typeSaisi.setUniteDecompte(HelperService.UNITE_DECOMPTE_MINUTES);
			typeSaisi.setQuotaMax(1);
			typeSaisi.setMotif(true);
			typeSaisi.setInfosComplementaires("");
			typeSaisi.setAlerte(true);
			typeSaisi.setMessageAlerte("");
			typeSaisi.setChkDateDebut(false);
			typeSaisi.setChkDateFin(false);
			typeSaisi.setCalendarHeureFin(true);
			typeSaisi.setCalendarDateFin(false);
			typeSaisi.setCalendarHeureDebut(true);
		
		ReturnMessageDto result = new ReturnMessageDto();
		result = impl.checkSaisiNewTypeAbsence(typeSaisi, result);
		
		assertEquals(2, result.getErrors().size());
		assertEquals(result.getErrors().get(0), "La date de fin est obligatoire si le radio bouton Matin/Après-midi de fin ou l'heure de fin sont sélectionnés.");
	}
	
	@Test
	public void checkSaisiNewTypeAbsence_ErreurQuotaMax() {
		
		RefUnitePeriodeQuota refUnitePeriodeQuota = new RefUnitePeriodeQuota();
		refUnitePeriodeQuota.setIdRefUnitePeriodeQuota(1);
		
		RefTypeSaisi typeSaisi = new RefTypeSaisi();
			typeSaisi.setCalendarDateDebut(true);
			typeSaisi.setFonctionnaire(true);
			typeSaisi.setRefUnitePeriodeQuota(refUnitePeriodeQuota);
			typeSaisi.setUniteDecompte(HelperService.UNITE_DECOMPTE_JOURS);
			typeSaisi.setQuotaMax(0);
			typeSaisi.setMotif(true);
			typeSaisi.setInfosComplementaires("");
			typeSaisi.setAlerte(true);
			typeSaisi.setMessageAlerte("");
		
		ReturnMessageDto result = new ReturnMessageDto();
		result = impl.checkSaisiNewTypeAbsence(typeSaisi, result);
		
		assertEquals(1, result.getErrors().size());
		assertEquals(result.getErrors().get(0), "Le Quota max est obligatoire si l'Unité de période pour le quota est sélectionnée.");
	}
	
	@Test
	public void checkSaisiNewTypeAbsence_ErreurQuotaMaxBis() {
		
		RefUnitePeriodeQuota refUnitePeriodeQuota = new RefUnitePeriodeQuota();
		
		RefTypeSaisi typeSaisi = new RefTypeSaisi();
			typeSaisi.setCalendarDateDebut(true);
			typeSaisi.setFonctionnaire(true);
			typeSaisi.setRefUnitePeriodeQuota(refUnitePeriodeQuota);
			typeSaisi.setUniteDecompte(HelperService.UNITE_DECOMPTE_JOURS);
			typeSaisi.setQuotaMax(1);
			typeSaisi.setMotif(true);
			typeSaisi.setInfosComplementaires("");
			typeSaisi.setAlerte(true);
			typeSaisi.setMessageAlerte("");
		
		ReturnMessageDto result = new ReturnMessageDto();
		result = impl.checkSaisiNewTypeAbsence(typeSaisi, result);
		
		assertEquals(1, result.getErrors().size());
		assertEquals(result.getErrors().get(0), "Le Quota max est obligatoire si l'Unité de période pour le quota est sélectionnée.");
	}
	
	@Test
	public void checkSaisiNewTypeAbsence_DecompteJours_ErreurHeureFin() {
		
		RefUnitePeriodeQuota refUnitePeriodeQuota = new RefUnitePeriodeQuota();
		refUnitePeriodeQuota.setIdRefUnitePeriodeQuota(1);
		
		RefTypeSaisi typeSaisi = new RefTypeSaisi();
			typeSaisi.setCalendarDateDebut(true);
			typeSaisi.setFonctionnaire(true);
			typeSaisi.setRefUnitePeriodeQuota(refUnitePeriodeQuota);
			typeSaisi.setUniteDecompte(HelperService.UNITE_DECOMPTE_JOURS);
			typeSaisi.setQuotaMax(1);
			typeSaisi.setMotif(true);
			typeSaisi.setInfosComplementaires("");
			typeSaisi.setAlerte(true);
			typeSaisi.setMessageAlerte("");
			typeSaisi.setCalendarHeureDebut(true);
		
		ReturnMessageDto result = new ReturnMessageDto();
		result = impl.checkSaisiNewTypeAbsence(typeSaisi, result);
		
		assertEquals(1, result.getErrors().size());
		assertEquals(result.getErrors().get(0), "L'heure de début ou de fin n'est pas valide pour une unité de décompte en JOURS.");
	}
	
	@Test
	public void checkSaisiNewTypeAbsence_DecompteJours_ErreurChkDate() {
		
		RefUnitePeriodeQuota refUnitePeriodeQuota = new RefUnitePeriodeQuota();
		refUnitePeriodeQuota.setIdRefUnitePeriodeQuota(1);
		
		RefTypeSaisi typeSaisi = new RefTypeSaisi();
			typeSaisi.setCalendarDateDebut(true);
			typeSaisi.setFonctionnaire(true);
			typeSaisi.setRefUnitePeriodeQuota(refUnitePeriodeQuota);
			typeSaisi.setUniteDecompte(HelperService.UNITE_DECOMPTE_JOURS);
			typeSaisi.setQuotaMax(1);
			typeSaisi.setMotif(true);
			typeSaisi.setInfosComplementaires("");
			typeSaisi.setAlerte(true);
			typeSaisi.setMessageAlerte("");
			typeSaisi.setCalendarHeureDebut(false);
			typeSaisi.setCalendarHeureFin(false);
			typeSaisi.setChkDateDebut(true);
		
		ReturnMessageDto result = new ReturnMessageDto();
		result = impl.checkSaisiNewTypeAbsence(typeSaisi, result);
		
		assertEquals(1, result.getErrors().size());
		assertEquals(result.getErrors().get(0), "Les radio boutons Matin/Après-midi doivent être sélectionnés ensemble.");
	}
	
	@Test
	public void checkSaisiNewTypeAbsence_DecompteJours_ok() {
		
		RefUnitePeriodeQuota refUnitePeriodeQuota = new RefUnitePeriodeQuota();
		refUnitePeriodeQuota.setIdRefUnitePeriodeQuota(1);
		
		RefTypeSaisi typeSaisi = new RefTypeSaisi();
			typeSaisi.setCalendarDateDebut(true);
			typeSaisi.setCalendarDateFin(true);
			typeSaisi.setFonctionnaire(true);
			typeSaisi.setRefUnitePeriodeQuota(refUnitePeriodeQuota);
			typeSaisi.setUniteDecompte(HelperService.UNITE_DECOMPTE_JOURS);
			typeSaisi.setQuotaMax(1);
			typeSaisi.setMotif(true);
			typeSaisi.setInfosComplementaires("");
			typeSaisi.setAlerte(true);
			typeSaisi.setMessageAlerte("");
			typeSaisi.setCalendarHeureDebut(false);
			typeSaisi.setCalendarHeureFin(false);
			typeSaisi.setChkDateDebut(true);
			typeSaisi.setChkDateFin(true);
		
		ReturnMessageDto result = new ReturnMessageDto();
		result = impl.checkSaisiNewTypeAbsence(typeSaisi, result);
		
		assertEquals(0, result.getErrors().size());
	}
	
	@Test
	public void checkSaisiNewTypeAbsence_DecompteMinutes_erreurHeureFin() {
		
		RefUnitePeriodeQuota refUnitePeriodeQuota = new RefUnitePeriodeQuota();
			refUnitePeriodeQuota.setIdRefUnitePeriodeQuota(1);
		
		RefTypeSaisi typeSaisi = new RefTypeSaisi();
			typeSaisi.setCalendarDateDebut(true);
			typeSaisi.setFonctionnaire(true);
			typeSaisi.setRefUnitePeriodeQuota(refUnitePeriodeQuota);
			typeSaisi.setUniteDecompte(HelperService.UNITE_DECOMPTE_MINUTES);
			typeSaisi.setQuotaMax(1);
			typeSaisi.setMotif(true);
			typeSaisi.setInfosComplementaires("");
			typeSaisi.setAlerte(true);
			typeSaisi.setMessageAlerte("");
			typeSaisi.setCalendarHeureDebut(true);
			typeSaisi.setCalendarHeureFin(false);
			typeSaisi.setChkDateDebut(false);
			typeSaisi.setChkDateFin(false);
			typeSaisi.setCalendarDateFin(true);
		
		ReturnMessageDto result = new ReturnMessageDto();
		result = impl.checkSaisiNewTypeAbsence(typeSaisi, result);
		
		assertEquals(1, result.getErrors().size());
		assertEquals(result.getErrors().get(0), "Pour une unité de décompte en MINUTES, l'heure de fin est obligatoire si la date de fin est sélectionnée.");
	}
	
	@Test
	public void checkSaisiNewTypeAbsence_DecompteMinutes_erreurHeureDebut() {
		
		RefUnitePeriodeQuota refUnitePeriodeQuota = new RefUnitePeriodeQuota();
		refUnitePeriodeQuota.setIdRefUnitePeriodeQuota(1);
		
		RefTypeSaisi typeSaisi = new RefTypeSaisi();
			typeSaisi.setCalendarDateDebut(true);
			typeSaisi.setFonctionnaire(true);
			typeSaisi.setRefUnitePeriodeQuota(refUnitePeriodeQuota);
			typeSaisi.setUniteDecompte(HelperService.UNITE_DECOMPTE_MINUTES);
			typeSaisi.setQuotaMax(1);
			typeSaisi.setMotif(true);
			typeSaisi.setInfosComplementaires("");
			typeSaisi.setAlerte(true);
			typeSaisi.setMessageAlerte("");
			typeSaisi.setCalendarHeureDebut(false);
			typeSaisi.setCalendarHeureFin(true);
			typeSaisi.setChkDateDebut(false);
			typeSaisi.setChkDateFin(false);
			typeSaisi.setCalendarDateFin(true);
		
		ReturnMessageDto result = new ReturnMessageDto();
		result = impl.checkSaisiNewTypeAbsence(typeSaisi, result);
		
		assertEquals(1, result.getErrors().size());
		assertEquals(result.getErrors().get(0), "L'heure de début est obligatoire pour une unité de décompte en MINUTES.");
	}
	
	@Test
	public void checkSaisiNewTypeAbsence_DecompteMinutes_erreurChkDate() {
		
		RefUnitePeriodeQuota refUnitePeriodeQuota = new RefUnitePeriodeQuota();
		refUnitePeriodeQuota.setIdRefUnitePeriodeQuota(1);
		
		RefTypeSaisi typeSaisi = new RefTypeSaisi();
			typeSaisi.setCalendarDateDebut(true);
			typeSaisi.setFonctionnaire(true);
			typeSaisi.setRefUnitePeriodeQuota(refUnitePeriodeQuota);
			typeSaisi.setUniteDecompte(HelperService.UNITE_DECOMPTE_MINUTES);
			typeSaisi.setQuotaMax(1);
			typeSaisi.setMotif(true);
			typeSaisi.setInfosComplementaires("");
			typeSaisi.setAlerte(true);
			typeSaisi.setMessageAlerte("");
			typeSaisi.setCalendarHeureDebut(true);
			typeSaisi.setCalendarHeureFin(true);
			typeSaisi.setChkDateDebut(false);
			typeSaisi.setChkDateFin(true);
			typeSaisi.setCalendarDateFin(true);
		
		ReturnMessageDto result = new ReturnMessageDto();
		result = impl.checkSaisiNewTypeAbsence(typeSaisi, result);
		
		assertEquals(1, result.getErrors().size());
		assertEquals(result.getErrors().get(0), "Les radio boutons Matin/Après-midi ne sont pas valides pour une unité de décompte en MINUTES.");
	}
	
	@Test
	public void checkSaisiNewTypeAbsence_DecompteMinutes_ok() {
		
		RefUnitePeriodeQuota refUnitePeriodeQuota = new RefUnitePeriodeQuota();
		refUnitePeriodeQuota.setIdRefUnitePeriodeQuota(1);
		
		RefTypeSaisi typeSaisi = new RefTypeSaisi();
			typeSaisi.setCalendarDateDebut(true);
			typeSaisi.setFonctionnaire(true);
			typeSaisi.setRefUnitePeriodeQuota(refUnitePeriodeQuota);
			typeSaisi.setUniteDecompte(HelperService.UNITE_DECOMPTE_MINUTES);
			typeSaisi.setQuotaMax(1);
			typeSaisi.setMotif(true);
			typeSaisi.setInfosComplementaires("");
			typeSaisi.setAlerte(true);
			typeSaisi.setMessageAlerte("");
			typeSaisi.setCalendarHeureDebut(true);
			typeSaisi.setCalendarHeureFin(true);
			typeSaisi.setChkDateDebut(false);
			typeSaisi.setChkDateFin(false);
			typeSaisi.setCalendarDateFin(true);
		
		ReturnMessageDto result = new ReturnMessageDto();
		result = impl.checkSaisiNewTypeAbsence(typeSaisi, result);
		
		assertEquals(0, result.getErrors().size());
	}
}
