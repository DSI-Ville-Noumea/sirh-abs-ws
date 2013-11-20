package nc.noumea.mairie.abs.service.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nc.noumea.mairie.abs.domain.Droit;
import nc.noumea.mairie.abs.domain.Profil;
import nc.noumea.mairie.abs.dto.AccessRightsDto;
import nc.noumea.mairie.abs.repository.IAccessRightsRepository;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.staticmock.MockStaticEntityMethods;
import org.springframework.test.util.ReflectionTestUtils;

@MockStaticEntityMethods
public class AccessRightsServiceTest {

	@Test
	public void getAgentAccessRights_AgentHasNoRights_ReturnFalseEverywhere() {

		// Given
		Integer idAgent = 906543;
		List<Droit> droits = new ArrayList<Droit>();

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentAccessRights(idAgent)).thenReturn(droits);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// When
		AccessRightsDto result = service.getAgentAccessRights(idAgent);

		// Then
		assertFalse(result.isSaisie());
		assertFalse(result.isModification());
		assertFalse(result.isSuppression());
		assertFalse(result.isImpression());
		assertFalse(result.isViserVisu());
		assertFalse(result.isViserModif());
		assertFalse(result.isApprouverVisu());
		assertFalse(result.isApprouverModif());
		assertFalse(result.isAnnuler());
		assertFalse(result.isVisuSolde());
		assertFalse(result.isMajSolde());
		assertFalse(result.isDroitAcces());
	}

	@Test
	public void getAgentAccessRights_AgentHas1Right_ReturnThisRights() {

		// Given
		Integer idAgent = 906543;

		Profil pr = new Profil();
		pr.setSaisie(true);
		pr.setModification(true);
		pr.setSuppression(true);
		pr.setImpression(true);
		pr.setViserVisu(false);
		pr.setViserModif(false);
		pr.setApprouverVisu(false);
		pr.setApprouverModif(false);
		pr.setAnnuler(true);
		pr.setVisuSolde(true);
		pr.setMajSolde(true);
		pr.setDroitAcces(false);

		Droit da = new Droit();
		da.setIdAgent(idAgent);
		da.getProfils().add(pr);

		List<Droit> droits = Arrays.asList(da);

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentAccessRights(idAgent)).thenReturn(droits);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// When
		AccessRightsDto result = service.getAgentAccessRights(idAgent);

		// Then
		assertTrue(result.isSaisie());
		assertTrue(result.isModification());
		assertTrue(result.isSuppression());
		assertTrue(result.isImpression());
		assertFalse(result.isViserVisu());
		assertFalse(result.isViserModif());
		assertFalse(result.isApprouverVisu());
		assertFalse(result.isApprouverModif());
		assertTrue(result.isAnnuler());
		assertTrue(result.isVisuSolde());
		assertTrue(result.isMajSolde());
		assertFalse(result.isDroitAcces());
	}

	@Test
	public void getAgentAccessRights_AgentHas2Rights_ReturnOrLogicRights() {

		// Given
		Integer idAgent = 906543;

		Profil pr = new Profil();
		pr.setSaisie(true);
		pr.setModification(true);
		pr.setSuppression(true);
		pr.setImpression(true);
		pr.setViserVisu(false);
		pr.setViserModif(false);
		pr.setApprouverVisu(false);
		pr.setApprouverModif(false);
		pr.setAnnuler(true);
		pr.setVisuSolde(true);
		pr.setMajSolde(true);
		pr.setDroitAcces(false);

		Droit da = new Droit();
		da.setIdAgent(900);
		da.getProfils().add(pr);

		Profil pr2 = new Profil();
		pr2.setSaisie(true);
		pr2.setModification(true);
		pr2.setSuppression(true);
		pr2.setImpression(true);
		pr2.setViserVisu(false);
		pr2.setViserModif(true);
		pr2.setApprouverVisu(false);
		pr2.setApprouverModif(true);
		pr2.setAnnuler(true);
		pr2.setVisuSolde(true);
		pr2.setMajSolde(true);
		pr2.setDroitAcces(false);

		Droit da2 = new Droit();
		da2.setIdAgent(idAgent);
		da2.getProfils().add(pr2);
		List<Droit> droits = Arrays.asList(da, da2);

		IAccessRightsRepository arRepo = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(arRepo.getAgentAccessRights(idAgent)).thenReturn(droits);

		AccessRightsService service = new AccessRightsService();
		ReflectionTestUtils.setField(service, "accessRightsRepository", arRepo);

		// When
		AccessRightsDto result = service.getAgentAccessRights(idAgent);

		// Then
		assertTrue(result.isSaisie());
		assertTrue(result.isModification());
		assertTrue(result.isSuppression());
		assertTrue(result.isImpression());
		assertFalse(result.isViserVisu());
		assertTrue(result.isViserModif());
		assertFalse(result.isApprouverVisu());
		assertTrue(result.isApprouverModif());
		assertTrue(result.isAnnuler());
		assertTrue(result.isVisuSolde());
		assertTrue(result.isMajSolde());
		assertFalse(result.isDroitAcces());
	}
}
