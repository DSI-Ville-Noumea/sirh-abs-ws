package nc.noumea.mairie.abs.service.impl;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nc.noumea.mairie.abs.domain.Droit;
import nc.noumea.mairie.abs.domain.DroitDroitsAgent;
import nc.noumea.mairie.abs.domain.DroitProfil;
import nc.noumea.mairie.abs.domain.DroitsAgent;
import nc.noumea.mairie.abs.domain.Profil;
import nc.noumea.mairie.abs.domain.ProfilEnum;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.AccessRightsRepository;
import nc.noumea.mairie.abs.repository.IAccessRightsRepository;

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
}
