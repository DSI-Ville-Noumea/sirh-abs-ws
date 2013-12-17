package nc.noumea.mairie.abs.service.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeRecup;
import nc.noumea.mairie.abs.domain.Droit;
import nc.noumea.mairie.abs.domain.DroitDroitsAgent;
import nc.noumea.mairie.abs.domain.DroitProfil;
import nc.noumea.mairie.abs.domain.DroitsAgent;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.Profil;
import nc.noumea.mairie.abs.domain.ProfilEnum;
import nc.noumea.mairie.abs.domain.RefEtat;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.AccessRightsRepository;
import nc.noumea.mairie.abs.repository.IAccessRightsRepository;
import nc.noumea.mairie.abs.repository.IDemandeRepository;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.staticmock.MockStaticEntityMethods;
import org.springframework.test.util.ReflectionTestUtils;

@MockStaticEntityMethods
public class AbsenceServiceTest {

	@Autowired
	AccessRightsRepository repository;

	@PersistenceContext(unitName = "absPersistenceUnit")
	private EntityManager absEntityManager;

	@Test
	public void verifAccessRightSaveDemande_AgentNotOperateur() {

		// Given
		ReturnMessageDto returnDto = new ReturnMessageDto();
		Integer idAgent = 9006543;

		DemandeDto dto = new DemandeDto();
		dto.setIdAgent(9005138);

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.isUserOperateur(idAgent)).thenReturn(false);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// When
		service.verifAccessRightSaveDemande(idAgent, dto, returnDto);

		// Then
		assertEquals(1, returnDto.getErrors().size());
		assertEquals("Vous n'êtes pas opérateur. Vous ne pouvez pas saisir de demandes.", returnDto.getErrors().get(0));
	}

	@Test
	public void verifAccessRightSaveDemande_Operateur_NotAgentOfOperateur() {

		// Given
		ReturnMessageDto returnDto = new ReturnMessageDto();
		Integer idAgent = 9006543;

		DemandeDto dto = new DemandeDto();
		dto.setIdAgent(9005138);

		Profil p = new Profil();
		p.setLibelle(ProfilEnum.OPERATEUR.toString());

		DroitProfil dpOpe = new DroitProfil();
		dpOpe.setDroitApprobateur(new Droit());
		dpOpe.setProfil(p);
		Set<DroitProfil> setDroitProfil = new HashSet<DroitProfil>();
		setDroitProfil.add(dpOpe);

		DroitsAgent da = new DroitsAgent();
		da.setIdAgent(9005131);

		DroitDroitsAgent dda = new DroitDroitsAgent();
		dda.setDroitProfil(dpOpe);
		dda.setDroitsAgent(da);
		Set<DroitDroitsAgent> setDroitDroitsAgent = new HashSet<DroitDroitsAgent>();
		setDroitDroitsAgent.add(dda);

		Droit d = new Droit();
		d.setIdAgent(idAgent);
		d.setIdDroit(1);
		d.setDroitProfils(setDroitProfil);
		d.setDroitDroitsAgent(setDroitDroitsAgent);

		dpOpe.setDroit(d);
		dda.setDroit(d);

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.isUserOperateur(idAgent)).thenReturn(true);
		Mockito.when(arRepo.getAgentDroitFetchAgents(idAgent)).thenReturn(d);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// When
		service.verifAccessRightSaveDemande(idAgent, dto, returnDto);

		// Then
		assertEquals(1, returnDto.getErrors().size());
		assertEquals("Vous n'êtes pas opérateur de l'agent 9005138. Vous ne pouvez pas saisir de demandes.", returnDto
				.getErrors().get(0));
	}

	@Test
	public void verifAccessRightSaveDemande_AllOk() {

		// Given
		ReturnMessageDto returnDto = new ReturnMessageDto();
		Integer idAgent = 9006543;

		DemandeDto dto = new DemandeDto();
		dto.setIdAgent(9005138);

		Profil p = new Profil();
		p.setLibelle(ProfilEnum.OPERATEUR.toString());

		DroitProfil dpOpe = new DroitProfil();
		dpOpe.setDroitApprobateur(new Droit());
		dpOpe.setProfil(p);
		Set<DroitProfil> setDroitProfil = new HashSet<DroitProfil>();
		setDroitProfil.add(dpOpe);

		DroitsAgent da = new DroitsAgent();
		da.setIdAgent(9005138);

		DroitDroitsAgent dda = new DroitDroitsAgent();
		dda.setDroitProfil(dpOpe);
		dda.setDroitsAgent(da);
		Set<DroitDroitsAgent> setDroitDroitsAgent = new HashSet<DroitDroitsAgent>();
		setDroitDroitsAgent.add(dda);

		Droit d = new Droit();
		d.setIdAgent(idAgent);
		d.setIdDroit(1);
		d.setDroitProfils(setDroitProfil);
		d.setDroitDroitsAgent(setDroitDroitsAgent);

		dpOpe.setDroit(d);
		dda.setDroit(d);

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.isUserOperateur(idAgent)).thenReturn(true);
		Mockito.when(arRepo.getAgentDroitFetchAgents(idAgent)).thenReturn(d);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// When
		service.verifAccessRightSaveDemande(idAgent, dto, returnDto);

		// Then
		assertEquals(0, returnDto.getErrors().size());
	}

	@Test
	public void getListeDemandesAgent_WrongParam() {

		// Given

		AbsenceService service = new AbsenceService();

		// When
		List<DemandeDto> result = service.getListeDemandesAgent(9005138, "TEST", new Date(), new Date(), new Date(),
				null, null);

		// Then
		assertEquals(0, result.size());
	}

	@Test
	public void getListeDemandesAgent_DemandesNonPrises() {

		// Given
		Integer idAgent = 9005138;

		List<RefEtat> refEtatNonPris = new ArrayList<RefEtat>();
		RefEtat etatProvisoire = new RefEtat();
		etatProvisoire.setIdRefEtat(0);
		etatProvisoire.setLabel("PROVISOIRE");
		RefEtat etatSaisie = new RefEtat();
		etatSaisie.setIdRefEtat(1);
		etatSaisie.setLabel("SAISIE");
		refEtatNonPris.add(etatProvisoire);
		refEtatNonPris.add(etatSaisie);

		ArrayList<Demande> listeDemande = new ArrayList<Demande>();
		RefTypeAbsence refType = new RefTypeAbsence();
		refType.setIdRefTypeAbsence(3);
		refType.setLabel("RECUP");
		EtatDemande etat1 = new EtatDemande();
		etat1.setDate(new Date());
		etat1.setEtat(RefEtatEnum.SAISIE);
		etat1.setIdAgent(idAgent);
		etat1.setIdEtatDemande(1);
		EtatDemande etat2 = new EtatDemande();
		etat2.setDate(new Date());
		etat2.setEtat(RefEtatEnum.SAISIE);
		etat2.setIdAgent(idAgent);
		etat2.setIdEtatDemande(2);
		DemandeRecup d = new DemandeRecup();
		etat1.setDemande(d);
		d.setIdDemande(1);
		d.setIdAgent(idAgent);
		d.setType(refType);
		d.setEtatsDemande(Arrays.asList(etat1));
		d.setDateDebut(new Date());
		DemandeRecup d2 = new DemandeRecup();
		etat2.setDemande(d2);
		d2.setIdDemande(2);
		d2.setIdAgent(idAgent);
		d2.setType(refType);
		d2.setEtatsDemande(Arrays.asList(etat2));
		d2.setDateDebut(new Date());
		listeDemande.add(d);
		listeDemande.add(d2);

		IDemandeRepository demRepo = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demRepo.listeDemandesAgent(Mockito.eq(idAgent), Mockito.isA(Date.class), Mockito.isA(Date.class), Mockito.eq(3))).thenReturn(listeDemande);
		Mockito.when(demRepo.findRefEtatNonPris()).thenReturn(refEtatNonPris);

		AbsenceService service = new AbsenceService();
		ReflectionTestUtils.setField(service, "demandeRepository", demRepo);

		// When
		List<DemandeDto> result = service.getListeDemandesAgent(idAgent, "NON_PRISES", new Date(), new Date(),
				new Date(), 3, 3);

		// Then
		assertEquals(1, result.size());
	}
}
