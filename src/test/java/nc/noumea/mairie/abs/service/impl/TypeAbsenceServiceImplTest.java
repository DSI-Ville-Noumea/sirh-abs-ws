package nc.noumea.mairie.abs.service.impl;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import nc.noumea.mairie.abs.domain.RefGroupeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeSaisi;
import nc.noumea.mairie.abs.domain.RefTypeSaisiCongeAnnuel;
import nc.noumea.mairie.abs.domain.RefUnitePeriodeQuota;
import nc.noumea.mairie.abs.dto.RefGroupeAbsenceDto;
import nc.noumea.mairie.abs.dto.RefTypeAbsenceDto;
import nc.noumea.mairie.abs.dto.RefTypeSaisiCongeAnnuelDto;
import nc.noumea.mairie.abs.dto.RefTypeSaisiDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.dto.UnitePeriodeQuotaDto;
import nc.noumea.mairie.abs.repository.ITypeAbsenceRepository;
import nc.noumea.mairie.abs.service.IAbsenceDataConsistencyRules;
import nc.noumea.mairie.abs.service.rules.impl.DataConsistencyRulesFactory;
import nc.noumea.mairie.ws.ISirhWSConsumer;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

public class TypeAbsenceServiceImplTest {

	@Test
	public void getListeTypAbsence_return2Results() {

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(1);
		type.setLabel("label");

		RefTypeAbsence type2 = new RefTypeAbsence();
		type2.setIdRefTypeAbsence(2);
		type2.setLabel("label 2");

		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(typeAbsenceRepository.getListeTypAbsence(null)).thenReturn(Arrays.asList(type, type2));

		TypeAbsenceServiceImpl service = new TypeAbsenceServiceImpl();
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);

		List<RefTypeAbsenceDto> result = service.getListeTypAbsence(null);

		assertEquals(2, result.size());
		assertEquals("label", result.get(0).getLibelle());
		assertEquals(1, result.get(0).getIdRefTypeAbsence().intValue());
		assertEquals("label 2", result.get(1).getLibelle());
		assertEquals(2, result.get(1).getIdRefTypeAbsence().intValue());
	}

	@Test
	public void getListeTypAbsence_returnNoResult() {

		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(typeAbsenceRepository.getListeTypAbsence(null)).thenReturn(null);

		TypeAbsenceServiceImpl service = new TypeAbsenceServiceImpl();
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);

		List<RefTypeAbsenceDto> result = service.getListeTypAbsence(null);

		assertEquals(0, result.size());
	}

	@Test
	public void setTypAbsence_UserNonHabilite() {

		ReturnMessageDto isSirhDto = new ReturnMessageDto();
		isSirhDto.getErrors().add("erreur");

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(9005138)).thenReturn(isSirhDto);

		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);

		TypeAbsenceServiceImpl service = new TypeAbsenceServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);

		RefTypeAbsenceDto typeAbsenceDto = new RefTypeAbsenceDto();

		ReturnMessageDto result = service.setTypAbsence(9005138, typeAbsenceDto);

		Mockito.verify(typeAbsenceRepository, Mockito.times(0)).persistEntity(Mockito.isA(RefTypeAbsence.class));
		assertEquals(result.getErrors().get(0), TypeAbsenceServiceImpl.AGENT_NON_HABILITE);
	}

	@Test
	public void setTypAbsence_TypeAbsenceInexistant() {

		RefTypeAbsenceDto typeAbsenceDto = new RefTypeAbsenceDto();
		typeAbsenceDto.setIdRefTypeAbsence(1);

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(9005138)).thenReturn(new ReturnMessageDto());

		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(typeAbsenceRepository.getEntity(RefTypeAbsence.class, typeAbsenceDto.getIdRefTypeAbsence()))
				.thenReturn(null);

		TypeAbsenceServiceImpl service = new TypeAbsenceServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);

		ReturnMessageDto result = service.setTypAbsence(9005138, typeAbsenceDto);

		Mockito.verify(typeAbsenceRepository, Mockito.times(0)).persistEntity(Mockito.isA(RefTypeAbsence.class));
		assertEquals(result.getErrors().get(0), TypeAbsenceServiceImpl.TYPE_ABSENCE_INEXISTANT);
	}

	@Test
	public void setTypAbsence_TypeAbsenceModifie() {

		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		groupeAbsence.setIdRefGroupeAbsence(1);

		RefTypeAbsenceDto typeAbsenceDto = new RefTypeAbsenceDto();
		typeAbsenceDto.setIdRefTypeAbsence(1);
		typeAbsenceDto.setLibelle("lib1");
		typeAbsenceDto.setGroupeAbsence(groupeAbsence);
		typeAbsenceDto.setTypeSaisiCongeAnnuelDto(new RefTypeSaisiCongeAnnuelDto());

		RefTypeAbsence typeAbsence = new RefTypeAbsence();
		typeAbsence.setTypeSaisi(new RefTypeSaisi());

		RefGroupeAbsence groupe = new RefGroupeAbsence();

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(9005138)).thenReturn(new ReturnMessageDto());

		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(typeAbsenceRepository.getEntity(RefTypeAbsence.class, typeAbsenceDto.getIdRefTypeAbsence()))
				.thenReturn(typeAbsence);
		Mockito.when(
				typeAbsenceRepository.getEntity(RefGroupeAbsence.class, typeAbsenceDto.getGroupeAbsence()
						.getIdRefGroupeAbsence())).thenReturn(groupe);

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				return args[2];
			}
		})
				.when(absDataConsistencyRules)
				.checkSaisiNewTypeAbsence(Mockito.isA(RefTypeSaisi.class), Mockito.isA(RefTypeSaisiCongeAnnuel.class),
						Mockito.isA(ReturnMessageDto.class));

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(
				dataConsistencyRulesFactory.getFactory(groupe.getIdRefGroupeAbsence(),
						typeAbsence.getIdRefTypeAbsence())).thenReturn(absDataConsistencyRules);

		TypeAbsenceServiceImpl service = new TypeAbsenceServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		ReturnMessageDto result = service.setTypAbsence(9005138, typeAbsenceDto);

		Mockito.verify(typeAbsenceRepository, Mockito.times(1)).persistEntity(Mockito.isA(RefTypeAbsence.class));
		assertEquals(result.getInfos().get(0), TypeAbsenceServiceImpl.TYPE_ABSENCE_MODIFIE);
	}

	@Test
	public void setTypAbsence_GroupeInexistant() {
		RefGroupeAbsenceDto groupeDto = new RefGroupeAbsenceDto();
		groupeDto.setCode("groupe");
		groupeDto.setLibelle("lib groupe");
		groupeDto.setIdRefGroupeAbsence(1);

		RefTypeAbsenceDto typeAbsenceDto = new RefTypeAbsenceDto();
		typeAbsenceDto.setIdRefTypeAbsence(1);
		typeAbsenceDto.setGroupeAbsence(groupeDto);

		RefTypeAbsence typeAbsence = new RefTypeAbsence();

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(9005138)).thenReturn(new ReturnMessageDto());

		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(typeAbsenceRepository.getEntity(RefTypeAbsence.class, typeAbsenceDto.getIdRefTypeAbsence()))
				.thenReturn(typeAbsence);
		Mockito.when(typeAbsenceRepository.getEntity(RefGroupeAbsence.class, typeAbsenceDto.getGroupeAbsence()))
				.thenReturn(null);

		TypeAbsenceServiceImpl service = new TypeAbsenceServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);

		ReturnMessageDto result = service.setTypAbsence(9005138, typeAbsenceDto);

		Mockito.verify(typeAbsenceRepository, Mockito.times(0)).persistEntity(Mockito.isA(RefTypeAbsence.class));
		assertEquals(result.getErrors().get(0), TypeAbsenceServiceImpl.TYPE_GROUPE_INEXISTANT);
	}

	@Test
	public void setTypAbsence_UnitePeriodeQuotaInexistant() {

		UnitePeriodeQuotaDto unitePeriodeQuotaDto = new UnitePeriodeQuotaDto();
		unitePeriodeQuotaDto.setIdRefUnitePeriodeQuota(1);

		RefTypeSaisiDto typeSaisiDto = new RefTypeSaisiDto();
		typeSaisiDto.setUnitePeriodeQuotaDto(unitePeriodeQuotaDto);

		RefGroupeAbsenceDto groupeDto = new RefGroupeAbsenceDto();
		groupeDto.setCode("groupe");
		groupeDto.setLibelle("lib groupe");
		groupeDto.setIdRefGroupeAbsence(1);

		RefTypeAbsenceDto typeAbsenceDto = new RefTypeAbsenceDto();
		typeAbsenceDto.setIdRefTypeAbsence(1);
		typeAbsenceDto.setLibelle("Libl");
		typeAbsenceDto.setGroupeAbsence(groupeDto);
		typeAbsenceDto.setTypeSaisiDto(typeSaisiDto);

		RefTypeAbsence typeAbsence = new RefTypeAbsence();

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(9005138)).thenReturn(new ReturnMessageDto());

		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(typeAbsenceRepository.getEntity(RefTypeAbsence.class, typeAbsenceDto.getIdRefTypeAbsence()))
				.thenReturn(typeAbsence);
		Mockito.when(
				typeAbsenceRepository.getEntity(RefGroupeAbsence.class, typeAbsenceDto.getGroupeAbsence()
						.getIdRefGroupeAbsence())).thenReturn(new RefGroupeAbsence());
		Mockito.when(
				typeAbsenceRepository.getEntity(RefUnitePeriodeQuota.class, typeSaisiDto.getUnitePeriodeQuotaDto()
						.getIdRefUnitePeriodeQuota())).thenReturn(null);
		;

		TypeAbsenceServiceImpl service = new TypeAbsenceServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);

		ReturnMessageDto result = service.setTypAbsence(9005138, typeAbsenceDto);

		Mockito.verify(typeAbsenceRepository, Mockito.times(0)).persistEntity(Mockito.isA(RefTypeAbsence.class));
		assertEquals(result.getErrors().get(0), TypeAbsenceServiceImpl.UNITE_PERIODE_QUOTA_INEXISTANT);
	}

	@Test
	public void setTypAbsence_typeAbsenceModifie() {

		UnitePeriodeQuotaDto unitePeriodeQuotaDto = new UnitePeriodeQuotaDto();
		unitePeriodeQuotaDto.setIdRefUnitePeriodeQuota(1);

		RefTypeSaisiDto typeSaisiDto = new RefTypeSaisiDto();
		typeSaisiDto.setUnitePeriodeQuotaDto(unitePeriodeQuotaDto);

		RefGroupeAbsenceDto groupeDto = new RefGroupeAbsenceDto();
		groupeDto.setCode("groupe");
		groupeDto.setLibelle("lib groupe");
		groupeDto.setIdRefGroupeAbsence(1);

		RefTypeAbsenceDto typeAbsenceDto = new RefTypeAbsenceDto();
		typeAbsenceDto.setIdRefTypeAbsence(1);
		typeAbsenceDto.setLibelle("Libl");
		typeAbsenceDto.setGroupeAbsence(groupeDto);
		typeAbsenceDto.setTypeSaisiDto(typeSaisiDto);

		RefTypeAbsence typeAbsence = new RefTypeAbsence();
		RefGroupeAbsence groupe = new RefGroupeAbsence();

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(9005138)).thenReturn(new ReturnMessageDto());

		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(typeAbsenceRepository.getEntity(RefTypeAbsence.class, typeAbsenceDto.getIdRefTypeAbsence()))
				.thenReturn(typeAbsence);
		Mockito.when(
				typeAbsenceRepository.getEntity(RefGroupeAbsence.class, typeAbsenceDto.getGroupeAbsence()
						.getIdRefGroupeAbsence())).thenReturn(groupe);
		Mockito.when(
				typeAbsenceRepository.getEntity(RefUnitePeriodeQuota.class, typeSaisiDto.getUnitePeriodeQuotaDto()
						.getIdRefUnitePeriodeQuota())).thenReturn(new RefUnitePeriodeQuota());
		;

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				return args[2];
			}
		})
				.when(absDataConsistencyRules)
				.checkSaisiNewTypeAbsence(Mockito.isA(RefTypeSaisi.class), Mockito.isA(RefTypeSaisiCongeAnnuel.class),
						Mockito.isA(ReturnMessageDto.class));

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(
				dataConsistencyRulesFactory.getFactory(groupe.getIdRefGroupeAbsence(),
						typeAbsence.getIdRefTypeAbsence())).thenReturn(absDataConsistencyRules);

		TypeAbsenceServiceImpl service = new TypeAbsenceServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		ReturnMessageDto result = service.setTypAbsence(9005138, typeAbsenceDto);

		Mockito.verify(typeAbsenceRepository, Mockito.times(1)).persistEntity(Mockito.isA(RefTypeAbsence.class));
		assertEquals(result.getInfos().get(0), TypeAbsenceServiceImpl.TYPE_ABSENCE_MODIFIE);
	}

	@Test
	public void setTypAbsence_typeAbsenceCree() {

		UnitePeriodeQuotaDto unitePeriodeQuotaDto = new UnitePeriodeQuotaDto();
		unitePeriodeQuotaDto.setIdRefUnitePeriodeQuota(1);

		RefTypeSaisiDto typeSaisiDto = new RefTypeSaisiDto();
		typeSaisiDto.setUnitePeriodeQuotaDto(unitePeriodeQuotaDto);

		RefGroupeAbsenceDto groupeDto = new RefGroupeAbsenceDto();
		groupeDto.setCode("groupe");
		groupeDto.setLibelle("lib groupe");
		groupeDto.setIdRefGroupeAbsence(1);

		RefTypeAbsenceDto typeAbsenceDto = new RefTypeAbsenceDto();
		typeAbsenceDto.setGroupeAbsence(groupeDto);
		typeAbsenceDto.setLibelle("Libl");
		typeAbsenceDto.setTypeSaisiDto(typeSaisiDto);

		RefTypeAbsence typeAbsence = new RefTypeAbsence();
		RefGroupeAbsence groupe = new RefGroupeAbsence();

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(9005138)).thenReturn(new ReturnMessageDto());

		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(typeAbsenceRepository.getEntity(RefTypeAbsence.class, typeAbsenceDto.getIdRefTypeAbsence()))
				.thenReturn(typeAbsence);
		Mockito.when(
				typeAbsenceRepository.getEntity(RefGroupeAbsence.class, typeAbsenceDto.getGroupeAbsence()
						.getIdRefGroupeAbsence())).thenReturn(groupe);
		Mockito.when(
				typeAbsenceRepository.getEntity(RefUnitePeriodeQuota.class, typeSaisiDto.getUnitePeriodeQuotaDto()
						.getIdRefUnitePeriodeQuota())).thenReturn(new RefUnitePeriodeQuota());
		;

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				return args[2];
			}
		})
				.when(absDataConsistencyRules)
				.checkSaisiNewTypeAbsence(Mockito.isA(RefTypeSaisi.class), Mockito.isA(RefTypeSaisiCongeAnnuel.class),
						Mockito.isA(ReturnMessageDto.class));

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(
				dataConsistencyRulesFactory.getFactory(groupe.getIdRefGroupeAbsence(),
						typeAbsence.getIdRefTypeAbsence())).thenReturn(absDataConsistencyRules);

		TypeAbsenceServiceImpl service = new TypeAbsenceServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		ReturnMessageDto result = service.setTypAbsence(9005138, typeAbsenceDto);

		Mockito.verify(typeAbsenceRepository, Mockito.times(1)).persistEntity(Mockito.isA(RefTypeAbsence.class));
		assertEquals(result.getInfos().get(0), TypeAbsenceServiceImpl.TYPE_ABSENCE_CREE);
	}

	@Test
	public void setTypAbsence_CongeAnnuel_ok_changingLib() {

		UnitePeriodeQuotaDto unitePeriodeQuotaDto = new UnitePeriodeQuotaDto();
		unitePeriodeQuotaDto.setIdRefUnitePeriodeQuota(1);

		RefTypeSaisiDto typeSaisiDto = new RefTypeSaisiDto();
		typeSaisiDto.setUnitePeriodeQuotaDto(unitePeriodeQuotaDto);

		RefGroupeAbsenceDto groupeDto = new RefGroupeAbsenceDto();
		groupeDto.setCode("groupe");
		groupeDto.setLibelle("lib groupe");
		groupeDto.setIdRefGroupeAbsence(1);

		RefTypeAbsenceDto typeAbsenceDto = new RefTypeAbsenceDto();
		typeAbsenceDto.setGroupeAbsence(groupeDto);
		typeAbsenceDto.setTypeSaisiDto(typeSaisiDto);

		RefTypeAbsence typeAbsence = new RefTypeAbsence();
		typeAbsence.setLabel("labelAbs");
		RefGroupeAbsence groupe = new RefGroupeAbsence();

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(9005138)).thenReturn(new ReturnMessageDto());

		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(typeAbsenceRepository.getEntity(RefTypeAbsence.class, typeAbsenceDto.getIdRefTypeAbsence()))
				.thenReturn(typeAbsence);
		Mockito.when(
				typeAbsenceRepository.getEntity(RefGroupeAbsence.class, typeAbsenceDto.getGroupeAbsence()
						.getIdRefGroupeAbsence())).thenReturn(groupe);
		Mockito.when(
				typeAbsenceRepository.getEntity(RefUnitePeriodeQuota.class, typeSaisiDto.getUnitePeriodeQuotaDto()
						.getIdRefUnitePeriodeQuota())).thenReturn(new RefUnitePeriodeQuota());
		;

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				return args[2];
			}
		})
				.when(absDataConsistencyRules)
				.checkSaisiNewTypeAbsence(Mockito.isA(RefTypeSaisi.class), Mockito.isA(RefTypeSaisiCongeAnnuel.class),
						Mockito.isA(ReturnMessageDto.class));

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(
				dataConsistencyRulesFactory.getFactory(groupe.getIdRefGroupeAbsence(),
						typeAbsence.getIdRefTypeAbsence())).thenReturn(absDataConsistencyRules);

		TypeAbsenceServiceImpl service = new TypeAbsenceServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		ReturnMessageDto result = service.setTypAbsence(9005138, typeAbsenceDto);

		Mockito.verify(typeAbsenceRepository, Mockito.times(1)).persistEntity(Mockito.isA(RefTypeAbsence.class));

		assertEquals(result.getErrors().size(), 0);
		assertEquals(typeAbsenceDto.getLibelle(), "labelAbs");
	}

	@Test
	public void setTypAbsence_CongeAnnuel_ok() {

		UnitePeriodeQuotaDto unitePeriodeQuotaDto = new UnitePeriodeQuotaDto();
		unitePeriodeQuotaDto.setIdRefUnitePeriodeQuota(1);

		RefTypeSaisiDto typeSaisiDto = new RefTypeSaisiDto();
		typeSaisiDto.setUnitePeriodeQuotaDto(unitePeriodeQuotaDto);

		RefGroupeAbsenceDto groupeDto = new RefGroupeAbsenceDto();
		groupeDto.setCode("groupe");
		groupeDto.setIdRefGroupeAbsence(1);

		RefTypeAbsenceDto typeAbsenceDto = new RefTypeAbsenceDto();
		typeAbsenceDto.setGroupeAbsence(groupeDto);
		typeAbsenceDto.setLibelle("Libl");
		typeAbsenceDto.setTypeSaisiDto(typeSaisiDto);

		RefTypeAbsence typeAbsence = new RefTypeAbsence();
		RefGroupeAbsence groupe = new RefGroupeAbsence();

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(9005138)).thenReturn(new ReturnMessageDto());

		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(typeAbsenceRepository.getEntity(RefTypeAbsence.class, typeAbsenceDto.getIdRefTypeAbsence()))
				.thenReturn(typeAbsence);
		Mockito.when(
				typeAbsenceRepository.getEntity(RefGroupeAbsence.class, typeAbsenceDto.getGroupeAbsence()
						.getIdRefGroupeAbsence())).thenReturn(groupe);
		Mockito.when(
				typeAbsenceRepository.getEntity(RefUnitePeriodeQuota.class, typeSaisiDto.getUnitePeriodeQuotaDto()
						.getIdRefUnitePeriodeQuota())).thenReturn(new RefUnitePeriodeQuota());
		;

		IAbsenceDataConsistencyRules absDataConsistencyRules = Mockito.mock(IAbsenceDataConsistencyRules.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				return args[2];
			}
		})
				.when(absDataConsistencyRules)
				.checkSaisiNewTypeAbsence(Mockito.isA(RefTypeSaisi.class), Mockito.isA(RefTypeSaisiCongeAnnuel.class),
						Mockito.isA(ReturnMessageDto.class));

		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(
				dataConsistencyRulesFactory.getFactory(groupe.getIdRefGroupeAbsence(),
						typeAbsence.getIdRefTypeAbsence())).thenReturn(absDataConsistencyRules);

		TypeAbsenceServiceImpl service = new TypeAbsenceServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);

		ReturnMessageDto result = service.setTypAbsence(9005138, typeAbsenceDto);

		Mockito.verify(typeAbsenceRepository, Mockito.times(1)).persistEntity(Mockito.isA(RefTypeAbsence.class));

		assertEquals(result.getErrors().size(), 0);
		assertEquals(typeAbsenceDto.getLibelle(), "Libl");
	}

	@Test
	public void setTypAbsence_CongeAnnuel() {
		RefGroupeAbsenceDto groupeDto = new RefGroupeAbsenceDto();
		groupeDto.setCode("groupe");
		groupeDto.setLibelle("lib groupe");
		groupeDto.setIdRefGroupeAbsence(1);

		RefTypeSaisiCongeAnnuelDto typeSaisiCongeAnnuelDto = new RefTypeSaisiCongeAnnuelDto();

		RefTypeAbsenceDto typeAbsenceDto = new RefTypeAbsenceDto();
		typeAbsenceDto.setIdRefTypeAbsence(1);
		typeAbsenceDto.setGroupeAbsence(groupeDto);
		typeAbsenceDto.setTypeSaisiCongeAnnuelDto(typeSaisiCongeAnnuelDto);

		RefTypeAbsence typeAbsence = new RefTypeAbsence();

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(9005138)).thenReturn(new ReturnMessageDto());

		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(typeAbsenceRepository.getEntity(RefTypeAbsence.class, typeAbsenceDto.getIdRefTypeAbsence()))
				.thenReturn(typeAbsence);
		Mockito.when(typeAbsenceRepository.getEntity(RefGroupeAbsence.class, typeAbsenceDto.getGroupeAbsence()))
				.thenReturn(null);

		TypeAbsenceServiceImpl service = new TypeAbsenceServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);

		ReturnMessageDto result = service.setTypAbsence(9005138, typeAbsenceDto);

		Mockito.verify(typeAbsenceRepository, Mockito.times(0)).persistEntity(Mockito.isA(RefTypeAbsence.class));
		assertEquals(result.getErrors().get(0), TypeAbsenceServiceImpl.TYPE_GROUPE_INEXISTANT);
	}

	@Test
	public void inactiveTypeAbsence_UserNonHabilite() {

		ReturnMessageDto isSirhDto = new ReturnMessageDto();
		isSirhDto.getErrors().add("erreur");

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(9005138)).thenReturn(isSirhDto);

		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);

		TypeAbsenceServiceImpl service = new TypeAbsenceServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);

		ReturnMessageDto result = service.inactiveTypeAbsence(9005138, 1);

		Mockito.verify(typeAbsenceRepository, Mockito.times(0)).persistEntity(Mockito.isA(RefTypeAbsence.class));
		assertEquals(result.getErrors().get(0), TypeAbsenceServiceImpl.AGENT_NON_HABILITE);
	}

	@Test
	public void inactiveTypeAbsence_TypeAbsenceInexistant() {

		Integer idTypeDemande = 1;

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(9005138)).thenReturn(new ReturnMessageDto());

		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(typeAbsenceRepository.getEntity(RefTypeAbsence.class, idTypeDemande)).thenReturn(null);

		TypeAbsenceServiceImpl service = new TypeAbsenceServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);

		ReturnMessageDto result = service.inactiveTypeAbsence(9005138, idTypeDemande);

		Mockito.verify(typeAbsenceRepository, Mockito.times(0)).persistEntity(Mockito.isA(RefTypeAbsence.class));
		assertEquals(result.getErrors().get(0), TypeAbsenceServiceImpl.TYPE_ABSENCE_INEXISTANT);
	}

	@Test
	public void inactiveTypeAbsence_OK() {

		Integer idTypeDemande = 1;

		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(9005138)).thenReturn(new ReturnMessageDto());

		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(typeAbsenceRepository.getEntity(RefTypeAbsence.class, idTypeDemande)).thenReturn(
				new RefTypeAbsence());

		TypeAbsenceServiceImpl service = new TypeAbsenceServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);

		ReturnMessageDto result = service.inactiveTypeAbsence(9005138, idTypeDemande);

		Mockito.verify(typeAbsenceRepository, Mockito.times(1)).persistEntity(Mockito.isA(RefTypeAbsence.class));
		assertEquals(result.getInfos().get(0), TypeAbsenceServiceImpl.TYPE_ABSENCE_INACTIVE);
	}

	@Test
	public void getTypeAbsenceByBaseHoraire_BaseA() {
		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(1);

		RefTypeSaisiCongeAnnuel refSaisie = new RefTypeSaisiCongeAnnuel();
		refSaisie.setIdRefTypeSaisiCongeAnnuel(1);
		refSaisie.setCodeBaseHoraireAbsence("A");
		refSaisie.setType(type);

		type.setTypeSaisiCongeAnnuel(refSaisie);

		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(
				typeAbsenceRepository.getEntity(RefTypeSaisiCongeAnnuel.class, refSaisie.getIdRefTypeSaisiCongeAnnuel()))
				.thenReturn(refSaisie);
		Mockito.when(typeAbsenceRepository.getEntity(RefTypeAbsence.class, refSaisie.getType().getIdRefTypeAbsence()))
				.thenReturn(type);

		TypeAbsenceServiceImpl service = new TypeAbsenceServiceImpl();
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);

		RefTypeAbsenceDto result = service.getTypeAbsenceByBaseHoraire(1);

		assertEquals(result.getTypeSaisiCongeAnnuelDto().getCodeBaseHoraireAbsence(),
				refSaisie.getCodeBaseHoraireAbsence());
	}

}
