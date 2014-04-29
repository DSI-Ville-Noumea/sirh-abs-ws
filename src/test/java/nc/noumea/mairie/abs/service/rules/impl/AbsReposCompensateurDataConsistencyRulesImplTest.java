package nc.noumea.mairie.abs.service.rules.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nc.noumea.mairie.abs.domain.AgentReposCompCount;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeReposComp;
import nc.noumea.mairie.abs.domain.DroitDroitsAgent;
import nc.noumea.mairie.abs.domain.DroitProfil;
import nc.noumea.mairie.abs.domain.DroitsAgent;
import nc.noumea.mairie.abs.domain.Profil;
import nc.noumea.mairie.abs.domain.ProfilEnum;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.ICounterRepository;
import nc.noumea.mairie.abs.repository.IReposCompensateurRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.abs.service.impl.HelperService;
import nc.noumea.mairie.domain.Spcarr;
import nc.noumea.mairie.sirh.domain.Agent;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.staticmock.MockStaticEntityMethods;
import org.springframework.test.util.ReflectionTestUtils;

@MockStaticEntityMethods
public class AbsReposCompensateurDataConsistencyRulesImplTest {

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
	public void filtreDroitOfListeDemandesByDemande_DemandeOfAgent() {

		Integer idAgentConnecte = 9005138;
		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(9005138);

		DemandeDto demandeDtoProvisoire = new DemandeDto();
		demandeDtoProvisoire.setIdRefEtat(RefEtatEnum.PROVISOIRE.getCodeEtat());
		demandeDtoProvisoire.setAgentWithServiceDto(agDto);
		demandeDtoProvisoire.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		DemandeDto demandeDtoSaisie = new DemandeDto();
		demandeDtoSaisie.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());
		demandeDtoSaisie.setAgentWithServiceDto(agDto);
		demandeDtoSaisie.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		DemandeDto demandeDtoApprouve = new DemandeDto();
		demandeDtoApprouve.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());
		demandeDtoApprouve.setAgentWithServiceDto(agDto);
		demandeDtoApprouve.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		DemandeDto demandeDtoRefusee = new DemandeDto();
		demandeDtoRefusee.setIdRefEtat(RefEtatEnum.REFUSEE.getCodeEtat());
		demandeDtoRefusee.setAgentWithServiceDto(agDto);
		demandeDtoRefusee.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		DemandeDto demandeDtoVisee_F = new DemandeDto();
		demandeDtoVisee_F.setIdRefEtat(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat());
		demandeDtoVisee_F.setAgentWithServiceDto(agDto);
		demandeDtoVisee_F.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		DemandeDto demandeDtoVisee_D = new DemandeDto();
		demandeDtoVisee_D.setIdRefEtat(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat());
		demandeDtoVisee_D.setAgentWithServiceDto(agDto);
		demandeDtoVisee_D.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		DemandeDto demandeDtoPrise = new DemandeDto();
		demandeDtoPrise.setIdRefEtat(RefEtatEnum.PRISE.getCodeEtat());
		demandeDtoPrise.setAgentWithServiceDto(agDto);
		demandeDtoPrise.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		DemandeDto demandeDtoAnnulee = new DemandeDto();
		demandeDtoAnnulee.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
		demandeDtoAnnulee.setAgentWithServiceDto(agDto);
		demandeDtoAnnulee.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		List<DemandeDto> resultListDto = new ArrayList<DemandeDto>();
		resultListDto.addAll(Arrays.asList(demandeDtoProvisoire, demandeDtoSaisie, demandeDtoApprouve,
				demandeDtoRefusee, demandeDtoVisee_F, demandeDtoVisee_D, demandeDtoPrise, demandeDtoAnnulee));

		AbsReposCompensateurDataConsistencyRulesImpl service = new AbsReposCompensateurDataConsistencyRulesImpl();

		List<DroitsAgent> listDroitAgent = new ArrayList<DroitsAgent>();
		
		// When
		DemandeDto result1 = service.filtreDroitOfDemande(idAgentConnecte, demandeDtoProvisoire, listDroitAgent);
		DemandeDto result2 = service.filtreDroitOfDemande(idAgentConnecte, demandeDtoSaisie, listDroitAgent);
		DemandeDto result3 = service.filtreDroitOfDemande(idAgentConnecte, demandeDtoApprouve, listDroitAgent);
		DemandeDto result4 = service.filtreDroitOfDemande(idAgentConnecte, demandeDtoRefusee, listDroitAgent);
		DemandeDto result5 = service.filtreDroitOfDemande(idAgentConnecte, demandeDtoVisee_F, listDroitAgent);
		DemandeDto result6 = service.filtreDroitOfDemande(idAgentConnecte, demandeDtoVisee_D, listDroitAgent);
		DemandeDto result7 = service.filtreDroitOfDemande(idAgentConnecte, demandeDtoPrise, listDroitAgent);
		DemandeDto result8 = service.filtreDroitOfDemande(idAgentConnecte, demandeDtoAnnulee, listDroitAgent);

		// Then
		assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), result1.getIdRefEtat().intValue());
		assertFalse(result1.isAffichageApprobation());
		assertFalse(result1.isAffichageBoutonAnnuler());
		assertFalse(result1.isAffichageBoutonImprimer());
		assertTrue(result1.isAffichageBoutonModifier());
		assertTrue(result1.isAffichageBoutonSupprimer());
		assertFalse(result1.isAffichageBoutonDupliquer());
		assertFalse(result1.isAffichageVisa());
		assertFalse(result1.isModifierApprobation());
		assertFalse(result1.isModifierVisa());
		assertNull(result1.getValeurApprobation());
		assertNull(result1.getValeurVisa());

		assertEquals(RefEtatEnum.SAISIE.getCodeEtat(), result2.getIdRefEtat().intValue());
		assertFalse(result2.isAffichageApprobation());
		assertFalse(result2.isAffichageBoutonAnnuler());
		assertFalse(result2.isAffichageBoutonImprimer());
		assertTrue(result2.isAffichageBoutonModifier());
		assertTrue(result2.isAffichageBoutonSupprimer());
		assertFalse(result2.isAffichageBoutonDupliquer());
		assertFalse(result2.isAffichageVisa());
		assertFalse(result2.isModifierApprobation());
		assertFalse(result2.isModifierVisa());
		assertNull(result2.getValeurApprobation());
		assertNull(result2.getValeurVisa());

		assertEquals(RefEtatEnum.APPROUVEE.getCodeEtat(), result3.getIdRefEtat().intValue());
		assertFalse(result3.isAffichageApprobation());
		assertTrue(result3.isAffichageBoutonAnnuler());
		assertTrue(result3.isAffichageBoutonImprimer());
		assertFalse(result3.isAffichageBoutonModifier());
		assertFalse(result3.isAffichageBoutonSupprimer());
		assertFalse(result3.isAffichageBoutonDupliquer());
		assertFalse(result3.isAffichageVisa());
		assertFalse(result3.isModifierApprobation());
		assertFalse(result3.isModifierVisa());
		assertNull(result3.getValeurApprobation());
		assertNull(result3.getValeurVisa());

		assertEquals(RefEtatEnum.REFUSEE.getCodeEtat(), result4.getIdRefEtat().intValue());
		assertFalse(result4.isAffichageApprobation());
		assertFalse(result4.isAffichageBoutonAnnuler());
		assertFalse(result4.isAffichageBoutonImprimer());
		assertFalse(result4.isAffichageBoutonModifier());
		assertFalse(result4.isAffichageBoutonSupprimer());
		assertFalse(result4.isAffichageBoutonDupliquer());
		assertFalse(result4.isAffichageVisa());
		assertFalse(result4.isModifierApprobation());
		assertFalse(result4.isModifierVisa());
		assertNull(result4.getValeurApprobation());
		assertNull(result4.getValeurVisa());

		assertEquals(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat(), result5.getIdRefEtat().intValue());
		assertFalse(result5.isAffichageApprobation());
		assertTrue(result5.isAffichageBoutonAnnuler());
		assertFalse(result5.isAffichageBoutonImprimer());
		assertFalse(result5.isAffichageBoutonModifier());
		assertFalse(result5.isAffichageBoutonSupprimer());
		assertFalse(result5.isAffichageBoutonDupliquer());
		assertFalse(result5.isAffichageVisa());
		assertFalse(result5.isModifierApprobation());
		assertFalse(result5.isModifierVisa());
		assertNull(result5.getValeurApprobation());
		assertNull(result5.getValeurVisa());

		assertEquals(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat(), result6.getIdRefEtat().intValue());
		assertFalse(result6.isAffichageApprobation());
		assertTrue(result6.isAffichageBoutonAnnuler());
		assertFalse(result6.isAffichageBoutonImprimer());
		assertFalse(result6.isAffichageBoutonModifier());
		assertFalse(result6.isAffichageBoutonSupprimer());
		assertFalse(result6.isAffichageBoutonDupliquer());
		assertFalse(result6.isAffichageVisa());
		assertFalse(result6.isModifierApprobation());
		assertFalse(result6.isModifierVisa());
		assertNull(result6.getValeurApprobation());
		assertNull(result6.getValeurVisa());

		assertEquals(RefEtatEnum.PRISE.getCodeEtat(), result7.getIdRefEtat().intValue());
		assertFalse(result7.isAffichageApprobation());
		assertFalse(result7.isAffichageBoutonAnnuler());
		assertFalse(result7.isAffichageBoutonImprimer());
		assertFalse(result7.isAffichageBoutonModifier());
		assertFalse(result7.isAffichageBoutonSupprimer());
		assertFalse(result7.isAffichageBoutonDupliquer());
		assertFalse(result7.isAffichageVisa());
		assertFalse(result7.isModifierApprobation());
		assertFalse(result7.isModifierVisa());
		assertNull(result7.getValeurApprobation());
		assertNull(result7.getValeurVisa());

		assertEquals(RefEtatEnum.ANNULEE.getCodeEtat(), result8.getIdRefEtat().intValue());
		assertFalse(result8.isAffichageApprobation());
		assertFalse(result8.isAffichageBoutonAnnuler());
		assertFalse(result8.isAffichageBoutonImprimer());
		assertFalse(result8.isAffichageBoutonModifier());
		assertFalse(result8.isAffichageBoutonSupprimer());
		assertFalse(result8.isAffichageBoutonDupliquer());
		assertFalse(result8.isAffichageVisa());
		assertFalse(result8.isModifierApprobation());
		assertFalse(result8.isModifierVisa());
		assertNull(result8.getValeurApprobation());
		assertNull(result8.getValeurVisa());
	}

	@Test
	public void filtreDroitOfListeDemandesByDemande_Operateur() {

		Integer idAgentConnecte = 9005138;
		AgentWithServiceDto agDto8 = new AgentWithServiceDto();
		agDto8.setIdAgent(9005137);
		AgentWithServiceDto agDto7 = new AgentWithServiceDto();
		agDto7.setIdAgent(9005136);
		AgentWithServiceDto agDto6 = new AgentWithServiceDto();
		agDto6.setIdAgent(9005135);
		AgentWithServiceDto agDto5 = new AgentWithServiceDto();
		agDto5.setIdAgent(9005134);
		AgentWithServiceDto agDto4 = new AgentWithServiceDto();
		agDto4.setIdAgent(9005133);
		AgentWithServiceDto agDto3 = new AgentWithServiceDto();
		agDto3.setIdAgent(9005132);
		AgentWithServiceDto agDto2 = new AgentWithServiceDto();
		agDto2.setIdAgent(9005131);
		AgentWithServiceDto agDto1 = new AgentWithServiceDto();
		agDto1.setIdAgent(9005130);

		// les demandes
		DemandeDto demandeDtoProvisoire = new DemandeDto();
		demandeDtoProvisoire.setIdRefEtat(RefEtatEnum.PROVISOIRE.getCodeEtat());
		demandeDtoProvisoire.setAgentWithServiceDto(agDto1);
		demandeDtoProvisoire.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		DemandeDto demandeDtoSaisie = new DemandeDto();
		demandeDtoSaisie.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());
		demandeDtoSaisie.setAgentWithServiceDto(agDto2);
		demandeDtoSaisie.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		DemandeDto demandeDtoApprouve = new DemandeDto();
		demandeDtoApprouve.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());
		demandeDtoApprouve.setAgentWithServiceDto(agDto3);
		demandeDtoApprouve.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		DemandeDto demandeDtoRefusee = new DemandeDto();
		demandeDtoRefusee.setIdRefEtat(RefEtatEnum.REFUSEE.getCodeEtat());
		demandeDtoRefusee.setAgentWithServiceDto(agDto4);
		demandeDtoRefusee.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		DemandeDto demandeDtoVisee_F = new DemandeDto();
		demandeDtoVisee_F.setIdRefEtat(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat());
		demandeDtoVisee_F.setAgentWithServiceDto(agDto5);
		demandeDtoVisee_F.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		DemandeDto demandeDtoVisee_D = new DemandeDto();
		demandeDtoVisee_D.setIdRefEtat(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat());
		demandeDtoVisee_D.setAgentWithServiceDto(agDto6);
		demandeDtoVisee_D.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		DemandeDto demandeDtoPrise = new DemandeDto();
		demandeDtoPrise.setIdRefEtat(RefEtatEnum.PRISE.getCodeEtat());
		demandeDtoPrise.setAgentWithServiceDto(agDto7);
		demandeDtoPrise.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		DemandeDto demandeDtoAnnulee = new DemandeDto();
		demandeDtoAnnulee.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
		demandeDtoAnnulee.setAgentWithServiceDto(agDto8);
		demandeDtoAnnulee.setIdTypeDemande(RefTypeAbsenceEnum.RECUP.getValue());
		List<DemandeDto> resultListDto = new ArrayList<DemandeDto>();
		resultListDto.addAll(Arrays.asList(demandeDtoProvisoire, demandeDtoSaisie, demandeDtoApprouve,
				demandeDtoRefusee, demandeDtoVisee_F, demandeDtoVisee_D, demandeDtoPrise, demandeDtoAnnulee));

		// les droits
		Profil profil = new Profil();
		profil.setLibelle(ProfilEnum.OPERATEUR.toString());

		DroitProfil droitProfil = new DroitProfil();
		droitProfil.setProfil(profil);

		DroitDroitsAgent dda = new DroitDroitsAgent();
		dda.setDroitProfil(droitProfil);

		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.add(dda);

		DroitsAgent da = new DroitsAgent();
		da.setIdAgent(9005130);
		da.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da1 = new DroitsAgent();
		da1.setIdAgent(9005131);
		da1.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da2 = new DroitsAgent();
		da2.setIdAgent(9005132);
		da2.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da3 = new DroitsAgent();
		da3.setIdAgent(9005133);
		da3.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da4 = new DroitsAgent();
		da4.setIdAgent(9005134);
		da4.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da5 = new DroitsAgent();
		da5.setIdAgent(9005135);
		da5.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da6 = new DroitsAgent();
		da6.setIdAgent(9005136);
		da6.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da7 = new DroitsAgent();
		da7.setIdAgent(9005137);
		da7.setDroitDroitsAgent(droitDroitsAgent);

		List<DroitsAgent> listDroitAgent = new ArrayList<DroitsAgent>();
		listDroitAgent.addAll(Arrays.asList(da, da1, da2, da3, da4, da5, da6, da7));

		AbsReposCompensateurDataConsistencyRulesImpl service = new AbsReposCompensateurDataConsistencyRulesImpl();

		resultListDto.addAll(Arrays.asList(demandeDtoProvisoire, demandeDtoSaisie, demandeDtoApprouve,
				demandeDtoRefusee, demandeDtoVisee_F, demandeDtoVisee_D, demandeDtoPrise, demandeDtoAnnulee));

		// When
		DemandeDto result1 = service.filtreDroitOfDemande(idAgentConnecte, demandeDtoProvisoire, listDroitAgent);
		DemandeDto result2 = service.filtreDroitOfDemande(idAgentConnecte, demandeDtoSaisie, listDroitAgent);
		DemandeDto result3 = service.filtreDroitOfDemande(idAgentConnecte, demandeDtoApprouve, listDroitAgent);
		DemandeDto result4 = service.filtreDroitOfDemande(idAgentConnecte, demandeDtoRefusee, listDroitAgent);
		DemandeDto result5 = service.filtreDroitOfDemande(idAgentConnecte, demandeDtoVisee_F, listDroitAgent);
		DemandeDto result6 = service.filtreDroitOfDemande(idAgentConnecte, demandeDtoVisee_D, listDroitAgent);
		DemandeDto result7 = service.filtreDroitOfDemande(idAgentConnecte, demandeDtoPrise, listDroitAgent);
		DemandeDto result8 = service.filtreDroitOfDemande(idAgentConnecte, demandeDtoAnnulee, listDroitAgent);

		// Then
		assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), result1.getIdRefEtat().intValue());
		assertEquals(9005130, result1.getAgentWithServiceDto().getIdAgent().intValue());
		assertFalse(result1.isAffichageApprobation());
		assertFalse(result1.isAffichageBoutonAnnuler());
		assertFalse(result1.isAffichageBoutonImprimer());
		assertTrue(result1.isAffichageBoutonModifier());
		assertTrue(result1.isAffichageBoutonSupprimer());
		assertFalse(result1.isAffichageBoutonDupliquer());
		assertFalse(result1.isAffichageVisa());
		assertFalse(result1.isModifierApprobation());
		assertFalse(result1.isModifierVisa());
		assertNull(result1.getValeurApprobation());
		assertNull(result1.getValeurVisa());

		assertEquals(RefEtatEnum.SAISIE.getCodeEtat(), result2.getIdRefEtat().intValue());
		assertEquals(9005131, result2.getAgentWithServiceDto().getIdAgent().intValue());
		assertFalse(result2.isAffichageApprobation());
		assertFalse(result2.isAffichageBoutonAnnuler());
		assertFalse(result2.isAffichageBoutonImprimer());
		assertTrue(result2.isAffichageBoutonModifier());
		assertTrue(result2.isAffichageBoutonSupprimer());
		assertFalse(result2.isAffichageBoutonDupliquer());
		assertFalse(result2.isAffichageVisa());
		assertFalse(result2.isModifierApprobation());
		assertFalse(result2.isModifierVisa());
		assertNull(result2.getValeurApprobation());
		assertNull(result2.getValeurVisa());

		assertEquals(RefEtatEnum.APPROUVEE.getCodeEtat(), result3.getIdRefEtat().intValue());
		assertEquals(9005132, result3.getAgentWithServiceDto().getIdAgent().intValue());
		assertFalse(result3.isAffichageApprobation());
		assertTrue(result3.isAffichageBoutonAnnuler());
		assertTrue(result3.isAffichageBoutonImprimer());
		assertFalse(result3.isAffichageBoutonModifier());
		assertFalse(result3.isAffichageBoutonSupprimer());
		assertFalse(result3.isAffichageBoutonDupliquer());
		assertFalse(result3.isAffichageVisa());
		assertFalse(result3.isModifierApprobation());
		assertFalse(result3.isModifierVisa());
		assertNull(result3.getValeurApprobation());
		assertNull(result3.getValeurVisa());

		assertEquals(RefEtatEnum.REFUSEE.getCodeEtat(), result4.getIdRefEtat().intValue());
		assertEquals(9005133, result4.getAgentWithServiceDto().getIdAgent().intValue());
		assertFalse(result4.isAffichageApprobation());
		assertFalse(result4.isAffichageBoutonAnnuler());
		assertFalse(result4.isAffichageBoutonImprimer());
		assertFalse(result4.isAffichageBoutonModifier());
		assertFalse(result4.isAffichageBoutonSupprimer());
		assertFalse(result4.isAffichageBoutonDupliquer());
		assertFalse(result4.isAffichageVisa());
		assertFalse(result4.isModifierApprobation());
		assertFalse(result4.isModifierVisa());
		assertNull(result4.getValeurApprobation());
		assertNull(result4.getValeurVisa());

		assertEquals(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat(), result5.getIdRefEtat().intValue());
		assertEquals(9005134, result5.getAgentWithServiceDto().getIdAgent().intValue());
		assertFalse(result5.isAffichageApprobation());
		assertTrue(result5.isAffichageBoutonAnnuler());
		assertFalse(result5.isAffichageBoutonImprimer());
		assertFalse(result5.isAffichageBoutonModifier());
		assertFalse(result5.isAffichageBoutonSupprimer());
		assertFalse(result5.isAffichageBoutonDupliquer());
		assertFalse(result5.isAffichageVisa());
		assertFalse(result5.isModifierApprobation());
		assertFalse(result5.isModifierVisa());
		assertNull(result5.getValeurApprobation());
		assertNull(result5.getValeurVisa());

		assertEquals(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat(), result6.getIdRefEtat().intValue());
		assertEquals(9005135, result6.getAgentWithServiceDto().getIdAgent().intValue());
		assertFalse(result6.isAffichageApprobation());
		assertTrue(result6.isAffichageBoutonAnnuler());
		assertFalse(result6.isAffichageBoutonImprimer());
		assertFalse(result6.isAffichageBoutonModifier());
		assertFalse(result6.isAffichageBoutonSupprimer());
		assertFalse(result6.isAffichageBoutonDupliquer());
		assertFalse(result6.isAffichageVisa());
		assertFalse(result6.isModifierApprobation());
		assertFalse(result6.isModifierVisa());
		assertNull(result6.getValeurApprobation());
		assertNull(result6.getValeurVisa());

		assertEquals(RefEtatEnum.PRISE.getCodeEtat(), result7.getIdRefEtat().intValue());
		assertEquals(9005136, result7.getAgentWithServiceDto().getIdAgent().intValue());
		assertFalse(result7.isAffichageApprobation());
		assertFalse(result7.isAffichageBoutonAnnuler());
		assertFalse(result7.isAffichageBoutonImprimer());
		assertFalse(result7.isAffichageBoutonModifier());
		assertFalse(result7.isAffichageBoutonSupprimer());
		assertFalse(result7.isAffichageBoutonDupliquer());
		assertFalse(result7.isAffichageVisa());
		assertFalse(result7.isModifierApprobation());
		assertFalse(result7.isModifierVisa());
		assertNull(result7.getValeurApprobation());
		assertNull(result7.getValeurVisa());

		assertEquals(RefEtatEnum.ANNULEE.getCodeEtat(), result8.getIdRefEtat().intValue());
		assertEquals(9005137, result8.getAgentWithServiceDto().getIdAgent().intValue());
		assertFalse(result8.isAffichageApprobation());
		assertFalse(result8.isAffichageBoutonAnnuler());
		assertFalse(result8.isAffichageBoutonImprimer());
		assertFalse(result8.isAffichageBoutonModifier());
		assertFalse(result8.isAffichageBoutonSupprimer());
		assertTrue(result8.isAffichageBoutonDupliquer());
		assertFalse(result8.isAffichageVisa());
		assertFalse(result8.isModifierApprobation());
		assertFalse(result8.isModifierVisa());
		assertNull(result8.getValeurApprobation());
		assertNull(result8.getValeurVisa());
	}

	@Test
	public void filtreDroitOfListeDemandesByDemande_Viseur() {

		Integer idAgentConnecte = 9005138;
		AgentWithServiceDto agDto8 = new AgentWithServiceDto();
		agDto8.setIdAgent(9005137);
		AgentWithServiceDto agDto7 = new AgentWithServiceDto();
		agDto7.setIdAgent(9005136);
		AgentWithServiceDto agDto6 = new AgentWithServiceDto();
		agDto6.setIdAgent(9005135);
		AgentWithServiceDto agDto5 = new AgentWithServiceDto();
		agDto5.setIdAgent(9005134);
		AgentWithServiceDto agDto4 = new AgentWithServiceDto();
		agDto4.setIdAgent(9005133);
		AgentWithServiceDto agDto3 = new AgentWithServiceDto();
		agDto3.setIdAgent(9005132);
		AgentWithServiceDto agDto2 = new AgentWithServiceDto();
		agDto2.setIdAgent(9005131);
		AgentWithServiceDto agDto1 = new AgentWithServiceDto();
		agDto1.setIdAgent(9005130);

		// les demandes
		DemandeDto demandeDtoProvisoire = new DemandeDto();
		demandeDtoProvisoire.setIdRefEtat(RefEtatEnum.PROVISOIRE.getCodeEtat());
		demandeDtoProvisoire.setAgentWithServiceDto(agDto1);
		DemandeDto demandeDtoSaisie = new DemandeDto();
		demandeDtoSaisie.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());
		demandeDtoSaisie.setAgentWithServiceDto(agDto2);
		DemandeDto demandeDtoApprouve = new DemandeDto();
		demandeDtoApprouve.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());
		demandeDtoApprouve.setAgentWithServiceDto(agDto3);
		DemandeDto demandeDtoRefusee = new DemandeDto();
		demandeDtoRefusee.setIdRefEtat(RefEtatEnum.REFUSEE.getCodeEtat());
		demandeDtoRefusee.setAgentWithServiceDto(agDto4);
		DemandeDto demandeDtoVisee_F = new DemandeDto();
		demandeDtoVisee_F.setIdRefEtat(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat());
		demandeDtoVisee_F.setAgentWithServiceDto(agDto5);
		DemandeDto demandeDtoVisee_D = new DemandeDto();
		demandeDtoVisee_D.setIdRefEtat(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat());
		demandeDtoVisee_D.setAgentWithServiceDto(agDto6);
		DemandeDto demandeDtoPrise = new DemandeDto();
		demandeDtoPrise.setIdRefEtat(RefEtatEnum.PRISE.getCodeEtat());
		demandeDtoPrise.setAgentWithServiceDto(agDto7);
		DemandeDto demandeDtoAnnulee = new DemandeDto();
		demandeDtoAnnulee.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
		demandeDtoAnnulee.setAgentWithServiceDto(agDto8);
		List<DemandeDto> resultListDto = new ArrayList<DemandeDto>();
		resultListDto.addAll(Arrays.asList(demandeDtoProvisoire, demandeDtoSaisie, demandeDtoApprouve,
				demandeDtoRefusee, demandeDtoVisee_F, demandeDtoVisee_D, demandeDtoPrise, demandeDtoAnnulee));

		// les droits
		Profil profil = new Profil();
		profil.setLibelle(ProfilEnum.VISEUR.toString());

		DroitProfil droitProfil = new DroitProfil();
		droitProfil.setProfil(profil);

		DroitDroitsAgent dda = new DroitDroitsAgent();
		dda.setDroitProfil(droitProfil);

		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.add(dda);

		DroitsAgent da = new DroitsAgent();
		da.setIdAgent(9005130);
		da.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da1 = new DroitsAgent();
		da1.setIdAgent(9005131);
		da1.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da2 = new DroitsAgent();
		da2.setIdAgent(9005132);
		da2.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da3 = new DroitsAgent();
		da3.setIdAgent(9005133);
		da3.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da4 = new DroitsAgent();
		da4.setIdAgent(9005134);
		da4.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da5 = new DroitsAgent();
		da5.setIdAgent(9005135);
		da5.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da6 = new DroitsAgent();
		da6.setIdAgent(9005136);
		da6.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da7 = new DroitsAgent();
		da7.setIdAgent(9005137);
		da7.setDroitDroitsAgent(droitDroitsAgent);

		List<DroitsAgent> listDroitAgent = new ArrayList<DroitsAgent>();
		listDroitAgent.addAll(Arrays.asList(da, da1, da2, da3, da4, da5, da6, da7));

		AbsReposCompensateurDataConsistencyRulesImpl service = new AbsReposCompensateurDataConsistencyRulesImpl();

		// When
		DemandeDto result1 = service.filtreDroitOfDemande(idAgentConnecte, demandeDtoProvisoire, listDroitAgent);
		DemandeDto result2 = service.filtreDroitOfDemande(idAgentConnecte, demandeDtoSaisie, listDroitAgent);
		DemandeDto result3 = service.filtreDroitOfDemande(idAgentConnecte, demandeDtoApprouve, listDroitAgent);
		DemandeDto result4 = service.filtreDroitOfDemande(idAgentConnecte, demandeDtoRefusee, listDroitAgent);
		DemandeDto result5 = service.filtreDroitOfDemande(idAgentConnecte, demandeDtoVisee_F, listDroitAgent);
		DemandeDto result6 = service.filtreDroitOfDemande(idAgentConnecte, demandeDtoVisee_D, listDroitAgent);
		DemandeDto result7 = service.filtreDroitOfDemande(idAgentConnecte, demandeDtoPrise, listDroitAgent);
		DemandeDto result8 = service.filtreDroitOfDemande(idAgentConnecte, demandeDtoAnnulee, listDroitAgent);

		// Then
		assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), result1.getIdRefEtat().intValue());
		assertEquals(9005130, result1.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result1.isAffichageApprobation());
		assertFalse(result1.isAffichageBoutonAnnuler());
		assertFalse(result1.isAffichageBoutonImprimer());
		assertFalse(result1.isAffichageBoutonModifier());
		assertFalse(result1.isAffichageBoutonSupprimer());
		assertFalse(result1.isAffichageBoutonDupliquer());
		assertTrue(result1.isAffichageVisa());
		assertFalse(result1.isModifierApprobation());
		assertFalse(result1.isModifierVisa());
		assertNull(result1.getValeurApprobation());
		assertNull(result1.getValeurVisa());

		assertEquals(RefEtatEnum.SAISIE.getCodeEtat(), result2.getIdRefEtat().intValue());
		assertEquals(9005131, result2.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result2.isAffichageApprobation());
		assertFalse(result2.isAffichageBoutonAnnuler());
		assertFalse(result2.isAffichageBoutonImprimer());
		assertFalse(result2.isAffichageBoutonModifier());
		assertFalse(result2.isAffichageBoutonSupprimer());
		assertFalse(result2.isAffichageBoutonDupliquer());
		assertTrue(result2.isAffichageVisa());
		assertFalse(result2.isModifierApprobation());
		assertTrue(result2.isModifierVisa());
		assertNull(result2.getValeurApprobation());
		assertNull(result2.getValeurVisa());

		assertEquals(RefEtatEnum.APPROUVEE.getCodeEtat(), result3.getIdRefEtat().intValue());
		assertEquals(9005132, result3.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result3.isAffichageApprobation());
		assertFalse(result3.isAffichageBoutonAnnuler());
		assertFalse(result3.isAffichageBoutonImprimer());
		assertFalse(result3.isAffichageBoutonModifier());
		assertFalse(result3.isAffichageBoutonSupprimer());
		assertFalse(result3.isAffichageBoutonDupliquer());
		assertTrue(result3.isAffichageVisa());
		assertFalse(result3.isModifierApprobation());
		assertFalse(result3.isModifierVisa());
		assertNull(result3.getValeurApprobation());
		assertNull(result3.getValeurVisa());

		assertEquals(RefEtatEnum.REFUSEE.getCodeEtat(), result4.getIdRefEtat().intValue());
		assertEquals(9005133, result4.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result4.isAffichageApprobation());
		assertFalse(result4.isAffichageBoutonAnnuler());
		assertFalse(result4.isAffichageBoutonImprimer());
		assertFalse(result4.isAffichageBoutonModifier());
		assertFalse(result4.isAffichageBoutonSupprimer());
		assertFalse(result4.isAffichageBoutonDupliquer());
		assertTrue(result4.isAffichageVisa());
		assertFalse(result4.isModifierApprobation());
		assertFalse(result4.isModifierVisa());
		assertNull(result4.getValeurApprobation());
		assertNull(result4.getValeurVisa());

		assertEquals(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat(), result5.getIdRefEtat().intValue());
		assertEquals(9005134, result5.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result5.isAffichageApprobation());
		assertFalse(result5.isAffichageBoutonAnnuler());
		assertFalse(result5.isAffichageBoutonImprimer());
		assertFalse(result5.isAffichageBoutonModifier());
		assertFalse(result5.isAffichageBoutonSupprimer());
		assertFalse(result5.isAffichageBoutonDupliquer());
		assertTrue(result5.isAffichageVisa());
		assertFalse(result5.isModifierApprobation());
		assertTrue(result5.isModifierVisa());
		assertNull(result5.getValeurApprobation());
		assertNull(result5.getValeurVisa());

		assertEquals(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat(), result6.getIdRefEtat().intValue());
		assertEquals(9005135, result6.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result6.isAffichageApprobation());
		assertFalse(result6.isAffichageBoutonAnnuler());
		assertFalse(result6.isAffichageBoutonImprimer());
		assertFalse(result6.isAffichageBoutonModifier());
		assertFalse(result6.isAffichageBoutonSupprimer());
		assertFalse(result6.isAffichageBoutonDupliquer());
		assertTrue(result6.isAffichageVisa());
		assertFalse(result6.isModifierApprobation());
		assertTrue(result6.isModifierVisa());
		assertNull(result6.getValeurApprobation());
		assertNull(result6.getValeurVisa());

		assertEquals(RefEtatEnum.PRISE.getCodeEtat(), result7.getIdRefEtat().intValue());
		assertEquals(9005136, result7.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result7.isAffichageApprobation());
		assertFalse(result7.isAffichageBoutonAnnuler());
		assertFalse(result7.isAffichageBoutonImprimer());
		assertFalse(result7.isAffichageBoutonModifier());
		assertFalse(result7.isAffichageBoutonSupprimer());
		assertFalse(result7.isAffichageBoutonDupliquer());
		assertTrue(result7.isAffichageVisa());
		assertFalse(result7.isModifierApprobation());
		assertFalse(result7.isModifierVisa());
		assertNull(result7.getValeurApprobation());
		assertNull(result7.getValeurVisa());

		assertEquals(RefEtatEnum.ANNULEE.getCodeEtat(), result8.getIdRefEtat().intValue());
		assertEquals(9005137, result8.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result8.isAffichageApprobation());
		assertFalse(result8.isAffichageBoutonAnnuler());
		assertFalse(result8.isAffichageBoutonImprimer());
		assertFalse(result8.isAffichageBoutonModifier());
		assertFalse(result8.isAffichageBoutonSupprimer());
		assertFalse(result8.isAffichageBoutonDupliquer());
		assertTrue(result8.isAffichageVisa());
		assertFalse(result8.isModifierApprobation());
		assertFalse(result8.isModifierVisa());
		assertNull(result8.getValeurApprobation());
		assertNull(result8.getValeurVisa());
	}

	@Test
	public void filtreDroitOfListeDemandesByDemande_Approbateur() {

		Integer idAgentConnecte = 9005138;
		AgentWithServiceDto agDto8 = new AgentWithServiceDto();
		agDto8.setIdAgent(9005137);
		AgentWithServiceDto agDto7 = new AgentWithServiceDto();
		agDto7.setIdAgent(9005136);
		AgentWithServiceDto agDto6 = new AgentWithServiceDto();
		agDto6.setIdAgent(9005135);
		AgentWithServiceDto agDto5 = new AgentWithServiceDto();
		agDto5.setIdAgent(9005134);
		AgentWithServiceDto agDto4 = new AgentWithServiceDto();
		agDto4.setIdAgent(9005133);
		AgentWithServiceDto agDto3 = new AgentWithServiceDto();
		agDto3.setIdAgent(9005132);
		AgentWithServiceDto agDto2 = new AgentWithServiceDto();
		agDto2.setIdAgent(9005131);
		AgentWithServiceDto agDto1 = new AgentWithServiceDto();
		agDto1.setIdAgent(9005130);

		// les demandes
		DemandeDto demandeDtoProvisoire = new DemandeDto();
		demandeDtoProvisoire.setIdRefEtat(RefEtatEnum.PROVISOIRE.getCodeEtat());
		demandeDtoProvisoire.setAgentWithServiceDto(agDto1);
		DemandeDto demandeDtoSaisie = new DemandeDto();
		demandeDtoSaisie.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());
		demandeDtoSaisie.setAgentWithServiceDto(agDto2);
		DemandeDto demandeDtoApprouve = new DemandeDto();
		demandeDtoApprouve.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());
		demandeDtoApprouve.setAgentWithServiceDto(agDto3);
		DemandeDto demandeDtoRefusee = new DemandeDto();
		demandeDtoRefusee.setIdRefEtat(RefEtatEnum.REFUSEE.getCodeEtat());
		demandeDtoRefusee.setAgentWithServiceDto(agDto4);
		DemandeDto demandeDtoVisee_F = new DemandeDto();
		demandeDtoVisee_F.setIdRefEtat(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat());
		demandeDtoVisee_F.setAgentWithServiceDto(agDto5);
		DemandeDto demandeDtoVisee_D = new DemandeDto();
		demandeDtoVisee_D.setIdRefEtat(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat());
		demandeDtoVisee_D.setAgentWithServiceDto(agDto6);
		DemandeDto demandeDtoPrise = new DemandeDto();
		demandeDtoPrise.setIdRefEtat(RefEtatEnum.PRISE.getCodeEtat());
		demandeDtoPrise.setAgentWithServiceDto(agDto7);
		DemandeDto demandeDtoAnnulee = new DemandeDto();
		demandeDtoAnnulee.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
		demandeDtoAnnulee.setAgentWithServiceDto(agDto8);
		List<DemandeDto> resultListDto = new ArrayList<DemandeDto>();
		resultListDto.addAll(Arrays.asList(demandeDtoProvisoire, demandeDtoSaisie, demandeDtoApprouve,
				demandeDtoRefusee, demandeDtoVisee_F, demandeDtoVisee_D, demandeDtoPrise, demandeDtoAnnulee));

		// les droits
		Profil profil = new Profil();
		profil.setLibelle(ProfilEnum.APPROBATEUR.toString());

		DroitProfil droitProfil = new DroitProfil();
		droitProfil.setProfil(profil);

		DroitDroitsAgent dda = new DroitDroitsAgent();
		dda.setDroitProfil(droitProfil);

		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.add(dda);

		DroitsAgent da = new DroitsAgent();
		da.setIdAgent(9005130);
		da.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da1 = new DroitsAgent();
		da1.setIdAgent(9005131);
		da1.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da2 = new DroitsAgent();
		da2.setIdAgent(9005132);
		da2.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da3 = new DroitsAgent();
		da3.setIdAgent(9005133);
		da3.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da4 = new DroitsAgent();
		da4.setIdAgent(9005134);
		da4.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da5 = new DroitsAgent();
		da5.setIdAgent(9005135);
		da5.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da6 = new DroitsAgent();
		da6.setIdAgent(9005136);
		da6.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da7 = new DroitsAgent();
		da7.setIdAgent(9005137);
		da7.setDroitDroitsAgent(droitDroitsAgent);

		List<DroitsAgent> listDroitAgent = new ArrayList<DroitsAgent>();
		listDroitAgent.addAll(Arrays.asList(da, da1, da2, da3, da4, da5, da6, da7));

		AbsReposCompensateurDataConsistencyRulesImpl service = new AbsReposCompensateurDataConsistencyRulesImpl();

		// When
		DemandeDto result1 = service.filtreDroitOfDemande(idAgentConnecte, demandeDtoProvisoire, listDroitAgent);
		DemandeDto result2 = service.filtreDroitOfDemande(idAgentConnecte, demandeDtoSaisie, listDroitAgent);
		DemandeDto result3 = service.filtreDroitOfDemande(idAgentConnecte, demandeDtoApprouve, listDroitAgent);
		DemandeDto result4 = service.filtreDroitOfDemande(idAgentConnecte, demandeDtoRefusee, listDroitAgent);
		DemandeDto result5 = service.filtreDroitOfDemande(idAgentConnecte, demandeDtoVisee_F, listDroitAgent);
		DemandeDto result6 = service.filtreDroitOfDemande(idAgentConnecte, demandeDtoVisee_D, listDroitAgent);
		DemandeDto result7 = service.filtreDroitOfDemande(idAgentConnecte, demandeDtoPrise, listDroitAgent);
		DemandeDto result8 = service.filtreDroitOfDemande(idAgentConnecte, demandeDtoAnnulee, listDroitAgent);

		// Then
		assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), result1.getIdRefEtat().intValue());
		assertEquals(9005130, result1.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result1.isAffichageApprobation());
		assertFalse(result1.isAffichageBoutonAnnuler());
		assertFalse(result1.isAffichageBoutonImprimer());
		assertFalse(result1.isAffichageBoutonModifier());
		assertFalse(result1.isAffichageBoutonSupprimer());
		assertFalse(result1.isAffichageBoutonDupliquer());
		assertTrue(result1.isAffichageVisa());
		assertFalse(result1.isModifierApprobation());
		assertFalse(result1.isModifierVisa());
		assertNull(result1.getValeurApprobation());
		assertNull(result1.getValeurVisa());

		assertEquals(RefEtatEnum.SAISIE.getCodeEtat(), result2.getIdRefEtat().intValue());
		assertEquals(9005131, result2.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result2.isAffichageApprobation());
		assertFalse(result2.isAffichageBoutonAnnuler());
		assertFalse(result2.isAffichageBoutonImprimer());
		assertFalse(result2.isAffichageBoutonModifier());
		assertFalse(result2.isAffichageBoutonSupprimer());
		assertFalse(result2.isAffichageBoutonDupliquer());
		assertTrue(result2.isAffichageVisa());
		assertTrue(result2.isModifierApprobation());
		assertFalse(result2.isModifierVisa());
		assertNull(result2.getValeurApprobation());
		assertNull(result2.getValeurVisa());

		assertEquals(RefEtatEnum.APPROUVEE.getCodeEtat(), result3.getIdRefEtat().intValue());
		assertEquals(9005132, result3.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result3.isAffichageApprobation());
		assertFalse(result3.isAffichageBoutonAnnuler());
		assertFalse(result3.isAffichageBoutonImprimer());
		assertFalse(result3.isAffichageBoutonModifier());
		assertFalse(result3.isAffichageBoutonSupprimer());
		assertFalse(result3.isAffichageBoutonDupliquer());
		assertTrue(result3.isAffichageVisa());
		assertTrue(result3.isModifierApprobation());
		assertFalse(result3.isModifierVisa());
		assertNull(result3.getValeurApprobation());
		assertNull(result3.getValeurVisa());

		assertEquals(RefEtatEnum.REFUSEE.getCodeEtat(), result4.getIdRefEtat().intValue());
		assertEquals(9005133, result4.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result4.isAffichageApprobation());
		assertFalse(result4.isAffichageBoutonAnnuler());
		assertFalse(result4.isAffichageBoutonImprimer());
		assertFalse(result4.isAffichageBoutonModifier());
		assertFalse(result4.isAffichageBoutonSupprimer());
		assertFalse(result4.isAffichageBoutonDupliquer());
		assertTrue(result4.isAffichageVisa());
		assertTrue(result4.isModifierApprobation());
		assertFalse(result4.isModifierVisa());
		assertNull(result4.getValeurApprobation());
		assertNull(result4.getValeurVisa());

		assertEquals(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat(), result5.getIdRefEtat().intValue());
		assertEquals(9005134, result5.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result5.isAffichageApprobation());
		assertFalse(result5.isAffichageBoutonAnnuler());
		assertFalse(result5.isAffichageBoutonImprimer());
		assertFalse(result5.isAffichageBoutonModifier());
		assertFalse(result5.isAffichageBoutonSupprimer());
		assertFalse(result5.isAffichageBoutonDupliquer());
		assertTrue(result5.isAffichageVisa());
		assertTrue(result5.isModifierApprobation());
		assertFalse(result5.isModifierVisa());
		assertNull(result5.getValeurApprobation());
		assertNull(result5.getValeurVisa());

		assertEquals(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat(), result6.getIdRefEtat().intValue());
		assertEquals(9005135, result6.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result6.isAffichageApprobation());
		assertFalse(result6.isAffichageBoutonAnnuler());
		assertFalse(result6.isAffichageBoutonImprimer());
		assertFalse(result6.isAffichageBoutonModifier());
		assertFalse(result6.isAffichageBoutonSupprimer());
		assertFalse(result6.isAffichageBoutonDupliquer());
		assertTrue(result6.isAffichageVisa());
		assertTrue(result6.isModifierApprobation());
		assertFalse(result6.isModifierVisa());
		assertNull(result6.getValeurApprobation());
		assertNull(result6.getValeurVisa());

		assertEquals(RefEtatEnum.PRISE.getCodeEtat(), result7.getIdRefEtat().intValue());
		assertEquals(9005136, result7.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result7.isAffichageApprobation());
		assertFalse(result7.isAffichageBoutonAnnuler());
		assertFalse(result7.isAffichageBoutonImprimer());
		assertFalse(result7.isAffichageBoutonModifier());
		assertFalse(result7.isAffichageBoutonSupprimer());
		assertFalse(result7.isAffichageBoutonDupliquer());
		assertTrue(result7.isAffichageVisa());
		assertFalse(result7.isModifierApprobation());
		assertFalse(result7.isModifierVisa());
		assertNull(result7.getValeurApprobation());
		assertNull(result7.getValeurVisa());

		assertEquals(RefEtatEnum.ANNULEE.getCodeEtat(), result8.getIdRefEtat().intValue());
		assertEquals(9005137, result8.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result8.isAffichageApprobation());
		assertFalse(result8.isAffichageBoutonAnnuler());
		assertFalse(result8.isAffichageBoutonImprimer());
		assertFalse(result8.isAffichageBoutonModifier());
		assertFalse(result8.isAffichageBoutonSupprimer());
		assertFalse(result8.isAffichageBoutonDupliquer());
		assertTrue(result8.isAffichageVisa());
		assertFalse(result8.isModifierApprobation());
		assertFalse(result8.isModifierVisa());
		assertNull(result8.getValeurApprobation());
		assertNull(result8.getValeurVisa());
	}

	@Test
	public void filtreDroitOfListeDemandesByDemande_Delegataire() {

		Integer idAgentConnecte = 9005138;
		AgentWithServiceDto agDto8 = new AgentWithServiceDto();
		agDto8.setIdAgent(9005137);
		AgentWithServiceDto agDto7 = new AgentWithServiceDto();
		agDto7.setIdAgent(9005136);
		AgentWithServiceDto agDto6 = new AgentWithServiceDto();
		agDto6.setIdAgent(9005135);
		AgentWithServiceDto agDto5 = new AgentWithServiceDto();
		agDto5.setIdAgent(9005134);
		AgentWithServiceDto agDto4 = new AgentWithServiceDto();
		agDto4.setIdAgent(9005133);
		AgentWithServiceDto agDto3 = new AgentWithServiceDto();
		agDto3.setIdAgent(9005132);
		AgentWithServiceDto agDto2 = new AgentWithServiceDto();
		agDto2.setIdAgent(9005131);
		AgentWithServiceDto agDto1 = new AgentWithServiceDto();
		agDto1.setIdAgent(9005130);

		// les demandes
		DemandeDto demandeDtoProvisoire = new DemandeDto();
		demandeDtoProvisoire.setIdRefEtat(RefEtatEnum.PROVISOIRE.getCodeEtat());
		demandeDtoProvisoire.setAgentWithServiceDto(agDto1);
		DemandeDto demandeDtoSaisie = new DemandeDto();
		demandeDtoSaisie.setIdRefEtat(RefEtatEnum.SAISIE.getCodeEtat());
		demandeDtoSaisie.setAgentWithServiceDto(agDto2);
		DemandeDto demandeDtoApprouve = new DemandeDto();
		demandeDtoApprouve.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());
		demandeDtoApprouve.setAgentWithServiceDto(agDto3);
		DemandeDto demandeDtoRefusee = new DemandeDto();
		demandeDtoRefusee.setIdRefEtat(RefEtatEnum.REFUSEE.getCodeEtat());
		demandeDtoRefusee.setAgentWithServiceDto(agDto4);
		DemandeDto demandeDtoVisee_F = new DemandeDto();
		demandeDtoVisee_F.setIdRefEtat(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat());
		demandeDtoVisee_F.setAgentWithServiceDto(agDto5);
		DemandeDto demandeDtoVisee_D = new DemandeDto();
		demandeDtoVisee_D.setIdRefEtat(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat());
		demandeDtoVisee_D.setAgentWithServiceDto(agDto6);
		DemandeDto demandeDtoPrise = new DemandeDto();
		demandeDtoPrise.setIdRefEtat(RefEtatEnum.PRISE.getCodeEtat());
		demandeDtoPrise.setAgentWithServiceDto(agDto7);
		DemandeDto demandeDtoAnnulee = new DemandeDto();
		demandeDtoAnnulee.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
		demandeDtoAnnulee.setAgentWithServiceDto(agDto8);
		List<DemandeDto> resultListDto = new ArrayList<DemandeDto>();
		resultListDto.addAll(Arrays.asList(demandeDtoProvisoire, demandeDtoSaisie, demandeDtoApprouve,
				demandeDtoRefusee, demandeDtoVisee_F, demandeDtoVisee_D, demandeDtoPrise, demandeDtoAnnulee));

		// les droits
		Profil profil = new Profil();
		profil.setLibelle(ProfilEnum.DELEGATAIRE.toString());

		DroitProfil droitProfil = new DroitProfil();
		droitProfil.setProfil(profil);

		DroitDroitsAgent dda = new DroitDroitsAgent();
		dda.setDroitProfil(droitProfil);

		Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
		droitDroitsAgent.add(dda);

		DroitsAgent da = new DroitsAgent();
		da.setIdAgent(9005130);
		da.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da1 = new DroitsAgent();
		da1.setIdAgent(9005131);
		da1.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da2 = new DroitsAgent();
		da2.setIdAgent(9005132);
		da2.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da3 = new DroitsAgent();
		da3.setIdAgent(9005133);
		da3.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da4 = new DroitsAgent();
		da4.setIdAgent(9005134);
		da4.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da5 = new DroitsAgent();
		da5.setIdAgent(9005135);
		da5.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da6 = new DroitsAgent();
		da6.setIdAgent(9005136);
		da6.setDroitDroitsAgent(droitDroitsAgent);
		DroitsAgent da7 = new DroitsAgent();
		da7.setIdAgent(9005137);
		da7.setDroitDroitsAgent(droitDroitsAgent);

		List<DroitsAgent> listDroitAgent = new ArrayList<DroitsAgent>();
		listDroitAgent.addAll(Arrays.asList(da, da1, da2, da3, da4, da5, da6, da7));

		AbsReposCompensateurDataConsistencyRulesImpl service = new AbsReposCompensateurDataConsistencyRulesImpl();

		// When
		DemandeDto result1 = service.filtreDroitOfDemande(idAgentConnecte, demandeDtoProvisoire, listDroitAgent);
		DemandeDto result2 = service.filtreDroitOfDemande(idAgentConnecte, demandeDtoSaisie, listDroitAgent);
		DemandeDto result3 = service.filtreDroitOfDemande(idAgentConnecte, demandeDtoApprouve, listDroitAgent);
		DemandeDto result4 = service.filtreDroitOfDemande(idAgentConnecte, demandeDtoRefusee, listDroitAgent);
		DemandeDto result5 = service.filtreDroitOfDemande(idAgentConnecte, demandeDtoVisee_F, listDroitAgent);
		DemandeDto result6 = service.filtreDroitOfDemande(idAgentConnecte, demandeDtoVisee_D, listDroitAgent);
		DemandeDto result7 = service.filtreDroitOfDemande(idAgentConnecte, demandeDtoPrise, listDroitAgent);
		DemandeDto result8 = service.filtreDroitOfDemande(idAgentConnecte, demandeDtoAnnulee, listDroitAgent);

		// Then
		assertEquals(RefEtatEnum.PROVISOIRE.getCodeEtat(), result1.getIdRefEtat().intValue());
		assertEquals(9005130, result1.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result1.isAffichageApprobation());
		assertFalse(result1.isAffichageBoutonAnnuler());
		assertFalse(result1.isAffichageBoutonImprimer());
		assertFalse(result1.isAffichageBoutonModifier());
		assertFalse(result1.isAffichageBoutonSupprimer());
		assertFalse(result1.isAffichageBoutonDupliquer());
		assertTrue(result1.isAffichageVisa());
		assertFalse(result1.isModifierApprobation());
		assertFalse(result1.isModifierVisa());
		assertNull(result1.getValeurApprobation());
		assertNull(result1.getValeurVisa());

		assertEquals(RefEtatEnum.SAISIE.getCodeEtat(), result2.getIdRefEtat().intValue());
		assertEquals(9005131, result2.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result2.isAffichageApprobation());
		assertFalse(result2.isAffichageBoutonAnnuler());
		assertFalse(result2.isAffichageBoutonImprimer());
		assertFalse(result2.isAffichageBoutonModifier());
		assertFalse(result2.isAffichageBoutonSupprimer());
		assertFalse(result2.isAffichageBoutonDupliquer());
		assertTrue(result2.isAffichageVisa());
		assertTrue(result2.isModifierApprobation());
		assertFalse(result2.isModifierVisa());
		assertNull(result2.getValeurApprobation());
		assertNull(result2.getValeurVisa());

		assertEquals(RefEtatEnum.APPROUVEE.getCodeEtat(), result3.getIdRefEtat().intValue());
		assertEquals(9005132, result3.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result3.isAffichageApprobation());
		assertFalse(result3.isAffichageBoutonAnnuler());
		assertFalse(result3.isAffichageBoutonImprimer());
		assertFalse(result3.isAffichageBoutonModifier());
		assertFalse(result3.isAffichageBoutonSupprimer());
		assertFalse(result3.isAffichageBoutonDupliquer());
		assertTrue(result3.isAffichageVisa());
		assertTrue(result3.isModifierApprobation());
		assertFalse(result3.isModifierVisa());
		assertNull(result3.getValeurApprobation());
		assertNull(result3.getValeurVisa());

		assertEquals(RefEtatEnum.REFUSEE.getCodeEtat(), result4.getIdRefEtat().intValue());
		assertEquals(9005133, result4.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result4.isAffichageApprobation());
		assertFalse(result4.isAffichageBoutonAnnuler());
		assertFalse(result4.isAffichageBoutonImprimer());
		assertFalse(result4.isAffichageBoutonModifier());
		assertFalse(result4.isAffichageBoutonSupprimer());
		assertFalse(result4.isAffichageBoutonDupliquer());
		assertTrue(result4.isAffichageVisa());
		assertTrue(result4.isModifierApprobation());
		assertFalse(result4.isModifierVisa());
		assertNull(result4.getValeurApprobation());
		assertNull(result4.getValeurVisa());

		assertEquals(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat(), result5.getIdRefEtat().intValue());
		assertEquals(9005134, result5.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result5.isAffichageApprobation());
		assertFalse(result5.isAffichageBoutonAnnuler());
		assertFalse(result5.isAffichageBoutonImprimer());
		assertFalse(result5.isAffichageBoutonModifier());
		assertFalse(result5.isAffichageBoutonSupprimer());
		assertFalse(result5.isAffichageBoutonDupliquer());
		assertTrue(result5.isAffichageVisa());
		assertTrue(result5.isModifierApprobation());
		assertFalse(result5.isModifierVisa());
		assertNull(result5.getValeurApprobation());
		assertNull(result5.getValeurVisa());

		assertEquals(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat(), result6.getIdRefEtat().intValue());
		assertEquals(9005135, result6.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result6.isAffichageApprobation());
		assertFalse(result6.isAffichageBoutonAnnuler());
		assertFalse(result6.isAffichageBoutonImprimer());
		assertFalse(result6.isAffichageBoutonModifier());
		assertFalse(result6.isAffichageBoutonSupprimer());
		assertFalse(result6.isAffichageBoutonDupliquer());
		assertTrue(result6.isAffichageVisa());
		assertTrue(result6.isModifierApprobation());
		assertFalse(result6.isModifierVisa());
		assertNull(result6.getValeurApprobation());
		assertNull(result6.getValeurVisa());

		assertEquals(RefEtatEnum.PRISE.getCodeEtat(), result7.getIdRefEtat().intValue());
		assertEquals(9005136, result7.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result7.isAffichageApprobation());
		assertFalse(result7.isAffichageBoutonAnnuler());
		assertFalse(result7.isAffichageBoutonImprimer());
		assertFalse(result7.isAffichageBoutonModifier());
		assertFalse(result7.isAffichageBoutonSupprimer());
		assertFalse(result7.isAffichageBoutonDupliquer());
		assertTrue(result7.isAffichageVisa());
		assertFalse(result7.isModifierApprobation());
		assertFalse(result7.isModifierVisa());
		assertNull(result7.getValeurApprobation());
		assertNull(result7.getValeurVisa());

		assertEquals(RefEtatEnum.ANNULEE.getCodeEtat(), result8.getIdRefEtat().intValue());
		assertEquals(9005137, result8.getAgentWithServiceDto().getIdAgent().intValue());
		assertTrue(result8.isAffichageApprobation());
		assertFalse(result8.isAffichageBoutonAnnuler());
		assertFalse(result8.isAffichageBoutonImprimer());
		assertFalse(result8.isAffichageBoutonModifier());
		assertFalse(result8.isAffichageBoutonSupprimer());
		assertFalse(result8.isAffichageBoutonDupliquer());
		assertTrue(result8.isAffichageVisa());
		assertFalse(result8.isModifierApprobation());
		assertFalse(result8.isModifierVisa());
		assertNull(result8.getValeurApprobation());
		assertNull(result8.getValeurVisa());
	}

	@Test
	public void checkStatutAgent_isNotFonctionnaire() {

		ReturnMessageDto srm = new ReturnMessageDto();
		Demande demande = new Demande();
		demande.setIdDemande(1);
		demande.setIdAgent(9005131);

		Agent ag = new Agent();
		ag.setNomatr(5131);

		Spcarr carr = new Spcarr();
		carr.setCdcate(4);

		Date date = new LocalDate(2013, 9, 29).toDate();

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(demande.getIdAgent(), date)).thenReturn(carr);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(date);

		AbsReposCompensateurDataConsistencyRulesImpl impl = new AbsReposCompensateurDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(impl, "helperService", helperService);

		srm = impl.checkStatutAgent(srm, demande.getIdAgent());

		assertEquals(0, srm.getErrors().size());
	}

	@Test
	public void checkStatutAgent_isFonctionnaire() {

		ReturnMessageDto srm = new ReturnMessageDto();
		Demande demande = new Demande();
		demande.setIdDemande(1);
		demande.setIdAgent(9005131);

		Agent ag = new Agent();
		ag.setNomatr(5131);

		Spcarr carr = new Spcarr();
		carr.setCdcate(2);

		Date date = new LocalDate(2013, 9, 29).toDate();

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgentCurrentCarriere(demande.getIdAgent(), date)).thenReturn(carr);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.getCurrentDate()).thenReturn(date);

		AbsReposCompensateurDataConsistencyRulesImpl impl = new AbsReposCompensateurDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(impl, "helperService", helperService);

		srm = impl.checkStatutAgent(srm, demande.getIdAgent());

		assertEquals(1, srm.getErrors().size());
		assertEquals(
				"L'agent [9005131] ne peut pas avoir de repos compensateur. Les repos compensateurs sont pour les contractuels ou les conventions collectives.",
				srm.getErrors().get(0).toString());
	}
	

	
	@Test
	public void isAfficherBoutonImprimer() {
		
		DemandeDto demandeDto = new DemandeDto();
			demandeDto.setIdRefEtat(RefEtatEnum.PROVISOIRE.getCodeEtat());
			
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
		assertFalse(result);
		
		demandeDto.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());
		result = impl.isAfficherBoutonImprimer(demandeDto);
		assertFalse(result);
	}
	
	@Test
	public void isAfficherBoutonAnnuler() {
		
		DemandeDto demandeDto = new DemandeDto();
			demandeDto.setIdRefEtat(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat());
		
		AbsReposCompensateurDataConsistencyRulesImpl impl = new AbsReposCompensateurDataConsistencyRulesImpl();
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
		assertFalse(result);
		
		demandeDto.setIdRefEtat(RefEtatEnum.EN_ATTENTE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto);
		assertFalse(result);
		
		demandeDto.setIdRefEtat(RefEtatEnum.PRISE.getCodeEtat());
		result = impl.isAfficherBoutonAnnuler(demandeDto);
		assertFalse(result);
		
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
}
