package nc.noumea.mairie.abs.service.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentRecupCount;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeRecup;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.ICounterRepository;
import nc.noumea.mairie.abs.repository.IDemandeRepository;
import nc.noumea.mairie.abs.repository.IRecuperationRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.domain.Spadmn;
import nc.noumea.mairie.sirh.domain.Agent;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.staticmock.MockStaticEntityMethods;
import org.springframework.test.util.ReflectionTestUtils;

@MockStaticEntityMethods
public class AbsRecuperationDataConsistencyRulesImplTest {

	@Test
	public void checkEtatsDemandeAcceptes_isProvisoire() {

		ReturnMessageDto srm = new ReturnMessageDto();
		Demande demande = new Demande();
		demande.setIdDemande(1);
		EtatDemande etat = new EtatDemande();
		etat.setEtat(RefEtatEnum.PROVISOIRE);
		demande.getEtatsDemande().add(etat);
		
		AbsRecuperationDataConsistencyRulesImpl impl = new AbsRecuperationDataConsistencyRulesImpl();
		
		srm = impl.checkEtatsDemandeAcceptes(srm, demande, Arrays.asList(RefEtatEnum.PROVISOIRE, RefEtatEnum.SAISIE));
		
		assertEquals(0, srm.getErrors().size());
	}
	
	@Test
	public void checkEtatsDemandeAcceptes_isSaisie() {

		ReturnMessageDto srm = new ReturnMessageDto();
		Demande demande = new Demande();
		demande.setIdDemande(1);
		EtatDemande etat = new EtatDemande();
		etat.setEtat(RefEtatEnum.SAISIE);
		demande.getEtatsDemande().add(etat);
		
		AbsRecuperationDataConsistencyRulesImpl impl = new AbsRecuperationDataConsistencyRulesImpl();
		
		srm = impl.checkEtatsDemandeAcceptes(srm, demande, Arrays.asList(RefEtatEnum.PROVISOIRE, RefEtatEnum.SAISIE));
		
		assertEquals(0, srm.getErrors().size());
	}
	
	@Test
	public void checkEtatsDemandeAcceptes_isNotSaisie_Et_NotProvisoire() {

		ReturnMessageDto srm = new ReturnMessageDto();
		Demande demande = new Demande();
		demande.setIdDemande(1);
		EtatDemande etat = new EtatDemande();
		etat.setEtat(RefEtatEnum.APPROUVEE);
		demande.getEtatsDemande().add(etat);
		
		AbsRecuperationDataConsistencyRulesImpl impl = new AbsRecuperationDataConsistencyRulesImpl();
		
		srm = impl.checkEtatsDemandeAcceptes(srm, demande, Arrays.asList(RefEtatEnum.PROVISOIRE, RefEtatEnum.SAISIE));
		
		assertEquals(1, srm.getErrors().size());
		assertEquals("La modification de la demande [1] n'est autorisée que si l'état est à Provisoire ou Saisie.", srm.getErrors().get(0).toString());
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
		Mockito.when(counterRepository.getAgentCounter(AgentRecupCount.class, demande.getIdAgent())).thenReturn(soldeRecup);
		
		IRecuperationRepository recuperationRepository = Mockito.mock(IRecuperationRepository.class);
		Mockito.when(recuperationRepository.getSommeDureeDemandeRecupEnCoursSaisieouVisee(demande.getIdAgent())).thenReturn(10);
				
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
		Mockito.when(counterRepository.getAgentCounter(AgentRecupCount.class, demande.getIdAgent())).thenReturn(soldeRecup);
		
		IRecuperationRepository recuperationRepository = Mockito.mock(IRecuperationRepository.class);
		Mockito.when(recuperationRepository.getSommeDureeDemandeRecupEnCoursSaisieouVisee(demande.getIdAgent())).thenReturn(10);
				
		AbsRecuperationDataConsistencyRulesImpl impl = new AbsRecuperationDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "recuperationRepository", recuperationRepository);
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);
		
		srm = impl.checkDepassementDroitsAcquis(srm, demande);
		
		assertEquals(1, srm.getErrors().size());
		assertEquals("Le dépassement des droits acquis n'est pas autorisé.", srm.getErrors().get(0).toString());
	}
	
	@Test
	public void checkDemandeDejaSaisieSurMemePeriode_withEtatExistantProvisoireAndRefuse(){
		
		ReturnMessageDto srm = new ReturnMessageDto();
		
		Demande demande = new Demande();
		demande.setIdDemande(1);
		demande.setIdAgent(9005138);
		
		EtatDemande etat = new EtatDemande();
		etat.setEtat(RefEtatEnum.PROVISOIRE);
		demande.getEtatsDemande().add(etat);
		
		List<Demande> listDemande = new ArrayList<Demande>();
		Demande demandeExist1 = new Demande();
		EtatDemande etat1 = new EtatDemande();
		etat1.setEtat(RefEtatEnum.REFUSEE);
		demandeExist1.getEtatsDemande().add(etat1);
		demandeExist1.setIdDemande(2);
		Demande demandeExist2 = new Demande();
		EtatDemande etat2 = new EtatDemande();
		etat2.setEtat(RefEtatEnum.PROVISOIRE);
		demandeExist2.getEtatsDemande().add(etat2);
		demandeExist2.setIdDemande(3);
		listDemande.addAll(Arrays.asList(demandeExist1, demandeExist2));
		
		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.listeDemandesAgent(demande.getIdAgent(), null, null, null)).thenReturn(listDemande);
		
		AbsRecuperationDataConsistencyRulesImpl impl = new AbsRecuperationDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "demandeRepository", demandeRepository);
		
		srm = impl.checkDemandeDejaSaisieSurMemePeriode(srm, demande);
		
		assertEquals(0, srm.getErrors().size());
	}
	
	@Test
	public void checkDemandeDejaSaisieSurMemePeriode_Ok(){
		
		ReturnMessageDto srm = new ReturnMessageDto();
		
		Demande demande = new Demande();
		demande.setIdDemande(1);
		demande.setIdAgent(9005138);
		demande.setDateDebut(new LocalDateTime(2013, 9, 10, 12, 1).toDate());
		demande.setDateFin(new LocalDateTime(2013, 9, 20, 13, 59).toDate());
		
		EtatDemande etat = new EtatDemande();
		etat.setEtat(RefEtatEnum.PROVISOIRE);
		demande.getEtatsDemande().add(etat);
		
		List<Demande> listDemande = new ArrayList<Demande>();
		Demande demandeExist1 = new Demande();
		EtatDemande etat1 = new EtatDemande();
		etat1.setEtat(RefEtatEnum.SAISIE);
		demandeExist1.getEtatsDemande().add(etat1);
		demandeExist1.setDateDebut(new LocalDateTime(2013, 9, 1, 14, 0).toDate());
		demandeExist1.setDateFin(new LocalDateTime(2013, 9, 10, 12, 0).toDate());
		demandeExist1.setIdDemande(2);
		Demande demandeExist2 = new Demande();
		EtatDemande etat2 = new EtatDemande();
		etat2.setEtat(RefEtatEnum.APPROUVEE);
		demandeExist2.getEtatsDemande().add(etat2);
		demandeExist2.setDateDebut(new LocalDateTime(2013, 9, 20, 14, 0).toDate());
		demandeExist2.setDateFin(new LocalDateTime(2013, 9, 30, 0, 0).toDate());
		demandeExist2.setIdDemande(3);
		listDemande.addAll(Arrays.asList(demandeExist1, demandeExist2));
		
		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.listeDemandesAgent(demande.getIdAgent(), null, null, null)).thenReturn(listDemande);
		
		AbsRecuperationDataConsistencyRulesImpl impl = new AbsRecuperationDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "demandeRepository", demandeRepository);
		
		srm = impl.checkDemandeDejaSaisieSurMemePeriode(srm, demande);
		
		assertEquals(0, srm.getErrors().size());
	}
	
	@Test
	public void checkDemandeDejaSaisieSurMemePeriode_DateFinKo(){
		
		ReturnMessageDto srm = new ReturnMessageDto();
		
		Demande demande = new Demande();
		demande.setIdDemande(1);
		demande.setIdAgent(9005138);
		demande.setDateDebut(new LocalDateTime(2013, 9, 10, 12, 1).toDate());
		demande.setDateFin(new LocalDateTime(2013, 9, 20, 14, 0).toDate());
		
		EtatDemande etat = new EtatDemande();
		etat.setEtat(RefEtatEnum.PROVISOIRE);
		demande.getEtatsDemande().add(etat);
		
		List<Demande> listDemande = new ArrayList<Demande>();
		Demande demandeExist1 = new Demande();
		EtatDemande etat1 = new EtatDemande();
		etat1.setEtat(RefEtatEnum.SAISIE);
		demandeExist1.getEtatsDemande().add(etat1);
		demandeExist1.setDateDebut(new LocalDateTime(2013, 9, 1, 14, 0).toDate());
		demandeExist1.setDateFin(new LocalDateTime(2013, 9, 10, 12, 0).toDate());
		demandeExist1.setIdDemande(2);
		Demande demandeExist2 = new Demande();
		EtatDemande etat2 = new EtatDemande();
		etat2.setEtat(RefEtatEnum.APPROUVEE);
		demandeExist2.getEtatsDemande().add(etat2);
		demandeExist2.setDateDebut(new LocalDateTime(2013, 9, 20, 14, 0).toDate());
		demandeExist2.setDateFin(new LocalDateTime(2013, 9, 30, 0, 0).toDate());
		demandeExist2.setIdDemande(3);
		listDemande.addAll(Arrays.asList(demandeExist1, demandeExist2));
		
		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.listeDemandesAgent(demande.getIdAgent(), null, null, null)).thenReturn(listDemande);
		
		AbsRecuperationDataConsistencyRulesImpl impl = new AbsRecuperationDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "demandeRepository", demandeRepository);
		
		srm = impl.checkDemandeDejaSaisieSurMemePeriode(srm, demande);
		
		assertEquals(1, srm.getErrors().size());
		assertEquals("La demande ne peut être couverte totalement ou partiellement par une autre absence.", srm.getErrors().get(0).toString());
	}
	
	@Test
	public void checkDemandeDejaSaisieSurMemePeriode_DateDebutKo(){
		
		ReturnMessageDto srm = new ReturnMessageDto();
		
		Demande demande = new Demande();
		demande.setIdDemande(1);
		demande.setIdAgent(9005138);
		demande.setDateDebut(new LocalDateTime(2013, 9, 10, 12, 0).toDate());
		demande.setDateFin(new LocalDateTime(2013, 9, 20, 13, 58).toDate());
		
		EtatDemande etat = new EtatDemande();
		etat.setEtat(RefEtatEnum.PROVISOIRE);
		demande.getEtatsDemande().add(etat);
		
		List<Demande> listDemande = new ArrayList<Demande>();
		Demande demandeExist1 = new Demande();
		EtatDemande etat1 = new EtatDemande();
		etat1.setEtat(RefEtatEnum.SAISIE);
		demandeExist1.getEtatsDemande().add(etat1);
		demandeExist1.setDateDebut(new LocalDateTime(2013, 9, 1, 14, 0).toDate());
		demandeExist1.setDateFin(new LocalDateTime(2013, 9, 10, 12, 0).toDate());
		demandeExist1.setIdDemande(2);
		Demande demandeExist2 = new Demande();
		EtatDemande etat2 = new EtatDemande();
		etat2.setEtat(RefEtatEnum.APPROUVEE);
		demandeExist2.getEtatsDemande().add(etat2);
		demandeExist2.setDateDebut(new LocalDateTime(2013, 9, 20, 14, 0).toDate());
		demandeExist2.setDateFin(new LocalDateTime(2013, 9, 30, 0, 0).toDate());
		demandeExist2.setIdDemande(3);
		listDemande.addAll(Arrays.asList(demandeExist1, demandeExist2));
		
		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.listeDemandesAgent(demande.getIdAgent(), null, null, null)).thenReturn(listDemande);
		
		AbsRecuperationDataConsistencyRulesImpl impl = new AbsRecuperationDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "demandeRepository", demandeRepository);
		
		srm = impl.checkDemandeDejaSaisieSurMemePeriode(srm, demande);
		
		assertEquals(1, srm.getErrors().size());
		assertEquals("La demande ne peut être couverte totalement ou partiellement par une autre absence.", srm.getErrors().get(0).toString());
	}
	
	@Test
	public void checkAgentInactivity_AgentActif(){
		
		List<String> activitesCode = Arrays.asList("01", "02", "03", "04", "23", "24", "60", "61",
				"62", "63", "64", "65", "66");
		
		ReturnMessageDto srm = new ReturnMessageDto();
		Integer idAgent = 9005138;
		Agent ag = new Agent();
		Spadmn adm = new Spadmn();
		
		Date date = new Date();
		
		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgent(idAgent)).thenReturn(ag);
		Mockito.when(sirhRepository.getAgentCurrentPosition(ag, date)).thenReturn(adm);
		
		AbsRecuperationDataConsistencyRulesImpl impl = new AbsRecuperationDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "sirhRepository", sirhRepository);
		
		for(String codeAcivite : activitesCode) {
			adm.setCdpadm(codeAcivite);
			srm = impl.checkAgentInactivity(srm, idAgent, date);
		}
		
		assertEquals(0, srm.getErrors().size());
	}
	
	@Test
	public void checkAgentInactivity_AgentInactif(){
		
		ReturnMessageDto srm = new ReturnMessageDto();
		Integer idAgent = 9005138;
		Agent ag = new Agent();
		Spadmn adm = new Spadmn();
		adm.setCdpadm("05");
		
		Date date = new Date();
		
		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(sirhRepository.getAgent(idAgent)).thenReturn(ag);
		Mockito.when(sirhRepository.getAgentCurrentPosition(ag, date)).thenReturn(adm);
		
		AbsRecuperationDataConsistencyRulesImpl impl = new AbsRecuperationDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "sirhRepository", sirhRepository);
		
		srm = impl.checkAgentInactivity(srm, idAgent, date);
		
		assertEquals(1, srm.getErrors().size());
		assertEquals("L'agent n'est pas en activité sur cette période.", srm.getErrors().get(0).toString());
	}
	
	@Test
	public void checkChampMotifPourEtatDonne_motifVide() {
		
		ReturnMessageDto srm = new ReturnMessageDto();
		
		AbsRecuperationDataConsistencyRulesImpl impl = new AbsRecuperationDataConsistencyRulesImpl();
		srm = impl.checkChampMotifPourEtatDonne(srm, RefEtatEnum.REFUSEE.getCodeEtat(), "");
		
		assertEquals(1, srm.getErrors().size());
		assertEquals("Le motif est obligatoire pour un avis Refusé.", srm.getErrors().get(0).toString());
	}
	
	@Test
	public void checkChampMotifPourEtatDonne_Ok_motifSaisi() {
		
		ReturnMessageDto srm = new ReturnMessageDto();
		
		AbsRecuperationDataConsistencyRulesImpl impl = new AbsRecuperationDataConsistencyRulesImpl();
		srm = impl.checkChampMotifPourEtatDonne(srm, RefEtatEnum.REFUSEE.getCodeEtat(), "test");
		
		assertEquals(0, srm.getErrors().size());
	}
	
	@Test
	public void checkChampMotifPourEtatDonne_Ok_motifNonSaisi_EtatApprouve() {
		
		ReturnMessageDto srm = new ReturnMessageDto();
		
		AbsRecuperationDataConsistencyRulesImpl impl = new AbsRecuperationDataConsistencyRulesImpl();
		srm = impl.checkChampMotifPourEtatDonne(srm, RefEtatEnum.APPROUVEE.getCodeEtat(), "test");
		
		assertEquals(0, srm.getErrors().size());
	}
}
