package nc.noumea.mairie.abs.service.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeAsa;
import nc.noumea.mairie.abs.domain.DemandeRecup;
import nc.noumea.mairie.abs.domain.DemandeReposComp;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IAccessRightsRepository;
import nc.noumea.mairie.abs.repository.IDemandeRepository;
import nc.noumea.mairie.abs.service.IAbsenceDataConsistencyRules;
import nc.noumea.mairie.abs.service.IAccessRightsService;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

public class SuppressionServiceTest {

	@Test
	public void supprimerDemande_demandeNotExiste() {

		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		Integer idDemande = 1;

		Demande demande = null;

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, idDemande)).thenReturn(demande);

		IAbsenceDataConsistencyRules absRecupDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				Demande obj = (Demande) args[0];
				ReturnMessageDto result = (ReturnMessageDto) args[1];
				if (null == obj) {
					result.getErrors().add("La demande n'existe pas.");
				}
				return result;
			}
		}).when(absRecupDataConsistencyRules)
				.verifDemandeExiste(Mockito.any(Demande.class), Mockito.isA(ReturnMessageDto.class));

		SuppressionService service = new SuppressionService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "defaultAbsenceDataConsistencyRulesImpl", absRecupDataConsistencyRules);

		result = service.supprimerDemande(idAgent, idDemande);

		assertEquals(1, result.getErrors().size());
		assertEquals("La demande n'existe pas.", result.getErrors().get(0).toString());
		Mockito.verify(demandeRepository, Mockito.times(0)).removeEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void supprimerDemande_notAccessRight() {

		ReturnMessageDto result = new ReturnMessageDto();
		result.getErrors().add("Vous n'êtes pas opérateur. Vous ne pouvez pas saisir de demandes.");
		Integer idAgent = 9005138;
		Integer idDemande = 1;

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(3);

		DemandeRecup demande = new DemandeRecup();
		demande.setIdAgent(9005131);
		demande.setType(type);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(DemandeRecup.class, idDemande)).thenReturn(demande);
		Mockito.when(demandeRepository.getEntity(Demande.class, idDemande)).thenReturn(demande);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				Demande obj = (Demande) args[0];
				ReturnMessageDto result = (ReturnMessageDto) args[1];
				if (null == obj) {
					result.getErrors().add("La demande n'existe pas.");
				}
				return result;
			}
		}).when(absDataConsistencyRules)
				.verifDemandeExiste(Mockito.any(Demande.class), Mockito.isA(ReturnMessageDto.class));

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isUserOperateur(idAgent)).thenReturn(false);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.isA(ReturnMessageDto.class))).thenReturn(result);

		SuppressionService service = new SuppressionService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "defaultAbsenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "absRecupDataConsistencyRules", absDataConsistencyRules);

		result = service.supprimerDemande(idAgent, idDemande);

		assertEquals(1, result.getErrors().size());
		assertEquals("Vous n'êtes pas opérateur. Vous ne pouvez pas saisir de demandes.", result.getErrors().get(0)
				.toString());
		Mockito.verify(demandeRepository, Mockito.times(0)).removeEntity(Mockito.isA(Demande.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void supprimerDemande_checkEtatDemande() {

		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		Integer idDemande = 1;

		EtatDemande etatDemande = new EtatDemande();
		etatDemande.setEtat(RefEtatEnum.APPROUVEE);
		List<EtatDemande> listEtat = new ArrayList<EtatDemande>();
		listEtat.add(etatDemande);

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(3);

		DemandeRecup demande = new DemandeRecup();
		demande.setIdAgent(9005138);
		demande.setEtatsDemande(listEtat);
		demande.setIdDemande(1);
		demande.setType(type);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, idDemande)).thenReturn(demande);
		Mockito.when(demandeRepository.getEntity(DemandeRecup.class, idDemande)).thenReturn(demande);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				Demande obj = (Demande) args[0];
				ReturnMessageDto result = (ReturnMessageDto) args[1];
				if (null == obj) {
					result.getErrors().add("La demande n'existe pas.");
				}
				return result;
			}
		}).when(absDataConsistencyRules)
				.verifDemandeExiste(Mockito.any(Demande.class), Mockito.isA(ReturnMessageDto.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				Demande demande = (Demande) args[1];
				List<RefEtatEnum> listEtatsAcceptes = (List<RefEtatEnum>) args[2];

				if (null != demande.getLatestEtatDemande()
						&& !listEtatsAcceptes.contains(demande.getLatestEtatDemande().getEtat())) {
					result.getErrors().add("Erreur etat incorrect");
				}

				return result;
			}
		})
				.when(absDataConsistencyRules)
				.checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(List.class));

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.isA(ReturnMessageDto.class))).thenReturn(new ReturnMessageDto());

		SuppressionService service = new SuppressionService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "defaultAbsenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "absRecupDataConsistencyRules", absDataConsistencyRules);

		result = service.supprimerDemande(idAgent, idDemande);

		assertEquals(1, result.getErrors().size());
		assertEquals("Erreur etat incorrect", result.getErrors().get(0).toString());
		Mockito.verify(demandeRepository, Mockito.times(0)).removeEntity(Mockito.isA(Demande.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void supprimerDemande_ok_etatSaisie() {

		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		Integer idDemande = 1;

		EtatDemande etatDemande = new EtatDemande();
		etatDemande.setEtat(RefEtatEnum.SAISIE);
		List<EtatDemande> listEtat = new ArrayList<EtatDemande>();
		listEtat.add(etatDemande);

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(3);

		DemandeRecup demande = new DemandeRecup();
		demande.setIdAgent(9005138);
		demande.setEtatsDemande(listEtat);
		demande.setIdDemande(1);
		demande.setType(type);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, idDemande)).thenReturn(demande);
		Mockito.when(demandeRepository.getEntity(DemandeRecup.class, idDemande)).thenReturn(demande);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(demandeRepository).removeEntity(Mockito.isA(Demande.class));

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				Demande obj = (Demande) args[0];
				ReturnMessageDto result = (ReturnMessageDto) args[1];
				if (null == obj) {
					result.getErrors().add("La demande n'existe pas.");
				}
				return result;
			}
		}).when(absDataConsistencyRules)
				.verifDemandeExiste(Mockito.any(Demande.class), Mockito.isA(ReturnMessageDto.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				Demande demande = (Demande) args[1];
				List<RefEtatEnum> listEtatsAcceptes = (List<RefEtatEnum>) args[2];

				if (null != demande.getLatestEtatDemande()
						&& !listEtatsAcceptes.contains(demande.getLatestEtatDemande().getEtat())) {
					result.getErrors().add("Erreur etat incorrect");
				}

				return result;
			}
		})
				.when(absDataConsistencyRules)
				.checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(List.class));

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.isA(ReturnMessageDto.class))).thenReturn(new ReturnMessageDto());

		SuppressionService service = new SuppressionService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "defaultAbsenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "absRecupDataConsistencyRules", absDataConsistencyRules);

		result = service.supprimerDemande(idAgent, idDemande);

		assertEquals(0, result.getErrors().size());
		assertEquals("La demande est supprimée.", result.getInfos().get(0));
		Mockito.verify(demandeRepository, Mockito.times(1)).removeEntity(Mockito.isA(Demande.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void supprimerDemande_ok_etatProvisoire() {

		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		Integer idDemande = 1;

		EtatDemande etatDemande = new EtatDemande();
		etatDemande.setEtat(RefEtatEnum.PROVISOIRE);
		List<EtatDemande> listEtat = new ArrayList<EtatDemande>();
		listEtat.add(etatDemande);

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(3);

		DemandeRecup demande = new DemandeRecup();
		demande.setIdAgent(9005138);
		demande.setEtatsDemande(listEtat);
		demande.setIdDemande(1);
		demande.setType(type);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, idDemande)).thenReturn(demande);
		Mockito.when(demandeRepository.getEntity(DemandeRecup.class, idDemande)).thenReturn(demande);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(demandeRepository).removeEntity(Mockito.isA(Demande.class));

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				Demande obj = (Demande) args[0];
				ReturnMessageDto result = (ReturnMessageDto) args[1];
				if (null == obj) {
					result.getErrors().add("La demande n'existe pas.");
				}
				return result;
			}
		}).when(absDataConsistencyRules)
				.verifDemandeExiste(Mockito.any(Demande.class), Mockito.isA(ReturnMessageDto.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				Demande demande = (Demande) args[1];
				List<RefEtatEnum> listEtatsAcceptes = (List<RefEtatEnum>) args[2];

				if (null != demande.getLatestEtatDemande()
						&& !listEtatsAcceptes.contains(demande.getLatestEtatDemande().getEtat())) {
					result.getErrors().add("Erreur etat incorrect");
				}

				return result;
			}
		})
				.when(absDataConsistencyRules)
				.checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(List.class));

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.isA(ReturnMessageDto.class))).thenReturn(new ReturnMessageDto());

		SuppressionService service = new SuppressionService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "defaultAbsenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "absRecupDataConsistencyRules", absDataConsistencyRules);

		result = service.supprimerDemande(idAgent, idDemande);

		assertEquals(0, result.getErrors().size());
		assertEquals("La demande est supprimée.", result.getInfos().get(0));
		Mockito.verify(demandeRepository, Mockito.times(1)).removeEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void supprimerDemandeEtatProvisoire_EtatIncorrect_ReturnError() {

		RefTypeAbsence typeRecup = new RefTypeAbsence();
		typeRecup.setLabel("Récupération");
		typeRecup.setIdRefTypeAbsence(3);

		EtatDemande etat1 = new EtatDemande();
		DemandeRecup demande1 = new DemandeRecup();
		etat1.setDemande(demande1);
		etat1.setEtat(RefEtatEnum.SAISIE);
		demande1.getEtatsDemande().add(etat1);
		demande1.setType(typeRecup);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, 1)).thenReturn(demande1);

		SuppressionService service = new SuppressionService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		// When
		ReturnMessageDto result = service.supprimerDemandeEtatProvisoire(1);

		// Then
		assertEquals(1, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
		assertEquals("La demande 1 n'est pas à l'état PROVISOIRE mais SAISIE.", result.getErrors().get(0).toString());
		Mockito.verify(demandeRepository, Mockito.never()).removeEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void supprimerDemandeEtatProvisoire_DemandeDoesNotExist_ReturnError() {

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, 1)).thenReturn(null);

		SuppressionService service = new SuppressionService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		// When
		ReturnMessageDto result = service.supprimerDemandeEtatProvisoire(1);

		// Then
		assertEquals(1, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
		assertEquals("La demande 1 n'existe pas.", result.getErrors().get(0).toString());
		Mockito.verify(demandeRepository, Mockito.never()).removeEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void supprimerDemandeEtatProvisoire_EtatIsOk_DeleteDemande() {

		RefTypeAbsence typeRecup = new RefTypeAbsence();
		typeRecup.setLabel("Récupération");
		typeRecup.setIdRefTypeAbsence(3);

		EtatDemande etat1 = new EtatDemande();
		DemandeRecup demande1 = new DemandeRecup();
		etat1.setDemande(demande1);
		etat1.setEtat(RefEtatEnum.PROVISOIRE);
		demande1.getEtatsDemande().add(etat1);
		demande1.setType(typeRecup);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, 1)).thenReturn(demande1);

		SuppressionService service = new SuppressionService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		// When
		ReturnMessageDto result = service.supprimerDemandeEtatProvisoire(1);

		// Then
		assertEquals(0, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).removeEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void supprimerDemandeReposComp_demandeNotExiste() {

		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		Integer idDemande = 1;

		Demande demande = null;

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, idDemande)).thenReturn(demande);

		IAbsenceDataConsistencyRules absReposCompDataConsistencyRules = Mockito
				.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				Demande obj = (Demande) args[0];
				ReturnMessageDto result = (ReturnMessageDto) args[1];
				if (null == obj) {
					result.getErrors().add("La demande n'existe pas.");
				}
				return result;
			}
		}).when(absReposCompDataConsistencyRules)
				.verifDemandeExiste(Mockito.any(Demande.class), Mockito.isA(ReturnMessageDto.class));

		SuppressionService service = new SuppressionService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "defaultAbsenceDataConsistencyRulesImpl",
				absReposCompDataConsistencyRules);

		result = service.supprimerDemande(idAgent, idDemande);

		assertEquals(1, result.getErrors().size());
		assertEquals("La demande n'existe pas.", result.getErrors().get(0).toString());
		Mockito.verify(demandeRepository, Mockito.times(0)).removeEntity(Mockito.isA(Demande.class));
	}

	@Test
	public void supprimerDemandeReposComp_notAccessRight() {

		ReturnMessageDto result = new ReturnMessageDto();
		result.getErrors().add("Vous n'êtes pas opérateur. Vous ne pouvez pas saisir de demandes.");
		Integer idAgent = 9005138;
		Integer idDemande = 1;

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(2);

		DemandeReposComp demande = new DemandeReposComp();
		demande.setIdAgent(9005131);
		demande.setType(type);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(DemandeReposComp.class, idDemande)).thenReturn(demande);
		Mockito.when(demandeRepository.getEntity(Demande.class, idDemande)).thenReturn(demande);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				Demande obj = (Demande) args[0];
				ReturnMessageDto result = (ReturnMessageDto) args[1];
				if (null == obj) {
					result.getErrors().add("La demande n'existe pas.");
				}
				return result;
			}
		}).when(absDataConsistencyRules)
				.verifDemandeExiste(Mockito.any(Demande.class), Mockito.isA(ReturnMessageDto.class));

		IAccessRightsRepository accessRightsRepository = Mockito.mock(IAccessRightsRepository.class);
		Mockito.when(accessRightsRepository.isUserOperateur(idAgent)).thenReturn(false);

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.isA(ReturnMessageDto.class))).thenReturn(result);

		SuppressionService service = new SuppressionService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "defaultAbsenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "absReposCompDataConsistencyRules", absDataConsistencyRules);

		result = service.supprimerDemande(idAgent, idDemande);

		assertEquals(1, result.getErrors().size());
		assertEquals("Vous n'êtes pas opérateur. Vous ne pouvez pas saisir de demandes.", result.getErrors().get(0)
				.toString());
		Mockito.verify(demandeRepository, Mockito.times(0)).removeEntity(Mockito.isA(Demande.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void supprimerDemandeReposComp_checkEtatDemande() {

		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		Integer idDemande = 1;

		EtatDemande etatDemande = new EtatDemande();
		etatDemande.setEtat(RefEtatEnum.APPROUVEE);
		List<EtatDemande> listEtat = new ArrayList<EtatDemande>();
		listEtat.add(etatDemande);

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(2);

		DemandeReposComp demande = new DemandeReposComp();
		demande.setIdAgent(9005138);
		demande.setEtatsDemande(listEtat);
		demande.setIdDemande(1);
		demande.setType(type);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, idDemande)).thenReturn(demande);
		Mockito.when(demandeRepository.getEntity(DemandeReposComp.class, idDemande)).thenReturn(demande);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				Demande obj = (Demande) args[0];
				ReturnMessageDto result = (ReturnMessageDto) args[1];
				if (null == obj) {
					result.getErrors().add("La demande n'existe pas.");
				}
				return result;
			}
		}).when(absDataConsistencyRules)
				.verifDemandeExiste(Mockito.any(Demande.class), Mockito.isA(ReturnMessageDto.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				Demande demande = (Demande) args[1];
				List<RefEtatEnum> listEtatsAcceptes = (List<RefEtatEnum>) args[2];

				if (null != demande.getLatestEtatDemande()
						&& !listEtatsAcceptes.contains(demande.getLatestEtatDemande().getEtat())) {
					result.getErrors().add("Erreur etat incorrect");
				}

				return result;
			}
		})
				.when(absDataConsistencyRules)
				.checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(List.class));

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.isA(ReturnMessageDto.class))).thenReturn(new ReturnMessageDto());

		SuppressionService service = new SuppressionService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "defaultAbsenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "absReposCompDataConsistencyRules", absDataConsistencyRules);

		result = service.supprimerDemande(idAgent, idDemande);

		assertEquals(1, result.getErrors().size());
		assertEquals("Erreur etat incorrect", result.getErrors().get(0).toString());
		Mockito.verify(demandeRepository, Mockito.times(0)).removeEntity(Mockito.isA(Demande.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void supprimerDemandeReposComp_ok_etatSaisie() {

		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		Integer idDemande = 1;

		EtatDemande etatDemande = new EtatDemande();
		etatDemande.setEtat(RefEtatEnum.SAISIE);
		List<EtatDemande> listEtat = new ArrayList<EtatDemande>();
		listEtat.add(etatDemande);

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(2);

		DemandeReposComp demande = new DemandeReposComp();
		demande.setIdAgent(9005138);
		demande.setEtatsDemande(listEtat);
		demande.setIdDemande(1);
		demande.setType(type);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, idDemande)).thenReturn(demande);
		Mockito.when(demandeRepository.getEntity(DemandeReposComp.class, idDemande)).thenReturn(demande);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(demandeRepository).removeEntity(Mockito.isA(Demande.class));

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				Demande obj = (Demande) args[0];
				ReturnMessageDto result = (ReturnMessageDto) args[1];
				if (null == obj) {
					result.getErrors().add("La demande n'existe pas.");
				}
				return result;
			}
		}).when(absDataConsistencyRules)
				.verifDemandeExiste(Mockito.any(Demande.class), Mockito.isA(ReturnMessageDto.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				Demande demande = (Demande) args[1];
				List<RefEtatEnum> listEtatsAcceptes = (List<RefEtatEnum>) args[2];

				if (null != demande.getLatestEtatDemande()
						&& !listEtatsAcceptes.contains(demande.getLatestEtatDemande().getEtat())) {
					result.getErrors().add("Erreur etat incorrect");
				}

				return result;
			}
		})
				.when(absDataConsistencyRules)
				.checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(List.class));

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.isA(ReturnMessageDto.class))).thenReturn(new ReturnMessageDto());

		SuppressionService service = new SuppressionService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "defaultAbsenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "absReposCompDataConsistencyRules", absDataConsistencyRules);

		result = service.supprimerDemande(idAgent, idDemande);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).removeEntity(Mockito.isA(Demande.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void supprimerDemandeReposComp_ok_etatProvisoire() {

		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		Integer idDemande = 1;

		EtatDemande etatDemande = new EtatDemande();
		etatDemande.setEtat(RefEtatEnum.PROVISOIRE);
		List<EtatDemande> listEtat = new ArrayList<EtatDemande>();
		listEtat.add(etatDemande);

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(2);

		DemandeReposComp demande = new DemandeReposComp();
		demande.setIdAgent(9005138);
		demande.setEtatsDemande(listEtat);
		demande.setIdDemande(1);
		demande.setType(type);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, idDemande)).thenReturn(demande);
		Mockito.when(demandeRepository.getEntity(DemandeReposComp.class, idDemande)).thenReturn(demande);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(demandeRepository).removeEntity(Mockito.isA(Demande.class));

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				Demande obj = (Demande) args[0];
				ReturnMessageDto result = (ReturnMessageDto) args[1];
				if (null == obj) {
					result.getErrors().add("La demande n'existe pas.");
				}
				return result;
			}
		}).when(absDataConsistencyRules)
				.verifDemandeExiste(Mockito.any(Demande.class), Mockito.isA(ReturnMessageDto.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				Demande demande = (Demande) args[1];
				List<RefEtatEnum> listEtatsAcceptes = (List<RefEtatEnum>) args[2];

				if (null != demande.getLatestEtatDemande()
						&& !listEtatsAcceptes.contains(demande.getLatestEtatDemande().getEtat())) {
					result.getErrors().add("Erreur etat incorrect");
				}

				return result;
			}
		})
				.when(absDataConsistencyRules)
				.checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(List.class));

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.isA(ReturnMessageDto.class))).thenReturn(new ReturnMessageDto());

		SuppressionService service = new SuppressionService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "defaultAbsenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "absReposCompDataConsistencyRules", absDataConsistencyRules);

		result = service.supprimerDemande(idAgent, idDemande);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).removeEntity(Mockito.isA(Demande.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void supprimerDemandeAsa_ok_etatSaisie() {

		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		Integer idDemande = 1;

		EtatDemande etatDemande = new EtatDemande();
		etatDemande.setEtat(RefEtatEnum.SAISIE);
		List<EtatDemande> listEtat = new ArrayList<EtatDemande>();
		listEtat.add(etatDemande);

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A48.getValue());

		DemandeAsa demande = new DemandeAsa();
			demande.setIdAgent(9005138);
			demande.setEtatsDemande(listEtat);
			demande.setIdDemande(1);
			demande.setType(type);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, idDemande)).thenReturn(demande);
		Mockito.when(demandeRepository.getEntity(DemandeAsa.class, idDemande)).thenReturn(demande);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(demandeRepository).removeEntity(Mockito.isA(Demande.class));

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				Demande obj = (Demande) args[0];
				ReturnMessageDto result = (ReturnMessageDto) args[1];
				if (null == obj) {
					result.getErrors().add("La demande n'existe pas.");
				}
				return result;
			}
		}).when(absDataConsistencyRules)
				.verifDemandeExiste(Mockito.any(Demande.class), Mockito.isA(ReturnMessageDto.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				Demande demande = (Demande) args[1];
				List<RefEtatEnum> listEtatsAcceptes = (List<RefEtatEnum>) args[2];

				if (null != demande.getLatestEtatDemande()
						&& !listEtatsAcceptes.contains(demande.getLatestEtatDemande().getEtat())) {
					result.getErrors().add("Erreur etat incorrect");
				}

				return result;
			}
		})
				.when(absDataConsistencyRules)
				.checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(List.class));

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.isA(ReturnMessageDto.class))).thenReturn(new ReturnMessageDto());

		SuppressionService service = new SuppressionService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "defaultAbsenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "absAsaA48DataConsistencyRulesImpl", absDataConsistencyRules);

		result = service.supprimerDemande(idAgent, idDemande);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).removeEntity(Mockito.isA(Demande.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void supprimerDemandeAsa_ok_etatProvisoire() {

		ReturnMessageDto result = new ReturnMessageDto();
		Integer idAgent = 9005138;
		Integer idDemande = 1;

		EtatDemande etatDemande = new EtatDemande();
			etatDemande.setEtat(RefEtatEnum.PROVISOIRE);
		List<EtatDemande> listEtat = new ArrayList<EtatDemande>();
			listEtat.add(etatDemande);

		RefTypeAbsence type = new RefTypeAbsence();
			type.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A48.getValue());

		DemandeAsa demande = new DemandeAsa();
			demande.setIdAgent(9005138);
			demande.setEtatsDemande(listEtat);
			demande.setIdDemande(1);
			demande.setType(type);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(Demande.class, idDemande)).thenReturn(demande);
		Mockito.when(demandeRepository.getEntity(DemandeAsa.class, idDemande)).thenReturn(demande);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}
		}).when(demandeRepository).removeEntity(Mockito.isA(Demande.class));

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				Demande obj = (Demande) args[0];
				ReturnMessageDto result = (ReturnMessageDto) args[1];
				if (null == obj) {
					result.getErrors().add("La demande n'existe pas.");
				}
				return result;
			}
		}).when(absDataConsistencyRules)
				.verifDemandeExiste(Mockito.any(Demande.class), Mockito.isA(ReturnMessageDto.class));

		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				Demande demande = (Demande) args[1];
				List<RefEtatEnum> listEtatsAcceptes = (List<RefEtatEnum>) args[2];

				if (null != demande.getLatestEtatDemande()
						&& !listEtatsAcceptes.contains(demande.getLatestEtatDemande().getEtat())) {
					result.getErrors().add("Erreur etat incorrect");
				}

				return result;
			}
		})
				.when(absDataConsistencyRules)
				.checkEtatsDemandeAcceptes(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Demande.class),
						Mockito.isA(List.class));

		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
		Mockito.when(
				accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(),
						Mockito.isA(ReturnMessageDto.class))).thenReturn(new ReturnMessageDto());

		SuppressionService service = new SuppressionService();
		ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "defaultAbsenceDataConsistencyRulesImpl", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "absAsaA48DataConsistencyRulesImpl", absDataConsistencyRules);

		result = service.supprimerDemande(idAgent, idDemande);

		assertEquals(0, result.getErrors().size());
		Mockito.verify(demandeRepository, Mockito.times(1)).removeEntity(Mockito.isA(Demande.class));
	}
}
