package nc.noumea.mairie.abs.service.impl;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.RefGroupeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefUnitePeriodeQuota;
import nc.noumea.mairie.abs.dto.RefTypeAbsenceDto;
import nc.noumea.mairie.abs.dto.RefTypeSaisiDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.dto.UnitePeriodeQuotaDto;
import nc.noumea.mairie.abs.repository.ITypeAbsenceRepository;
import nc.noumea.mairie.ws.ISirhWSConsumer;

import org.junit.Test;
import org.mockito.Mockito;
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
		Mockito.when(typeAbsenceRepository.getListeTypAbsence()).thenReturn(Arrays.asList(type, type2));
		
		TypeAbsenceServiceImpl service = new  TypeAbsenceServiceImpl();
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);
		
		List<RefTypeAbsenceDto> result = service.getListeTypAbsence();
		
		assertEquals(2, result.size());
		assertEquals("label", result.get(0).getLibelle());
		assertEquals(1, result.get(0).getIdRefTypeAbsence().intValue());
		assertEquals("label 2", result.get(1).getLibelle());
		assertEquals(2, result.get(1).getIdRefTypeAbsence().intValue());
	}
	
	@Test
	public void getListeTypAbsence_returnNoResult() {
		
		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(typeAbsenceRepository.getListeTypAbsence()).thenReturn(null);
		
		TypeAbsenceServiceImpl service = new  TypeAbsenceServiceImpl();
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);
		
		List<RefTypeAbsenceDto> result = service.getListeTypAbsence();
		
		assertEquals(0, result.size());
	}
	
	@Test
	public void setTypAbsence_UserNonHabilite() {
		
		ReturnMessageDto isSirhDto = new ReturnMessageDto();
		isSirhDto.getErrors().add("erreur");
		
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(9005138)).thenReturn(isSirhDto);
		
		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		
		TypeAbsenceServiceImpl service = new  TypeAbsenceServiceImpl();
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
		Mockito.when(typeAbsenceRepository.getEntity(RefTypeAbsence.class, typeAbsenceDto.getIdRefTypeAbsence())).thenReturn(null);
		
		TypeAbsenceServiceImpl service = new  TypeAbsenceServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);
		
		ReturnMessageDto result = service.setTypAbsence(9005138, typeAbsenceDto);

		Mockito.verify(typeAbsenceRepository, Mockito.times(0)).persistEntity(Mockito.isA(RefTypeAbsence.class));
		assertEquals(result.getErrors().get(0), TypeAbsenceServiceImpl.TYPE_ABSENCE_INEXISTANT);
	}
	
	@Test
	public void setTypAbsence_TypeAbsenceModifie() {
		
		RefTypeAbsenceDto typeAbsenceDto = new RefTypeAbsenceDto();
			typeAbsenceDto.setIdRefTypeAbsence(1);
		
		RefTypeAbsence typeAbsence = new RefTypeAbsence();
			
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(9005138)).thenReturn(new ReturnMessageDto());

		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(typeAbsenceRepository.getEntity(RefTypeAbsence.class, typeAbsenceDto.getIdRefTypeAbsence())).thenReturn(typeAbsence);
		
		TypeAbsenceServiceImpl service = new  TypeAbsenceServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);
		
		ReturnMessageDto result = service.setTypAbsence(9005138, typeAbsenceDto);

		Mockito.verify(typeAbsenceRepository, Mockito.times(1)).persistEntity(Mockito.isA(RefTypeAbsence.class));
		assertEquals(result.getInfos().get(0), TypeAbsenceServiceImpl.TYPE_ABSENCE_MODIFIE);
	}
	
	@Test
	public void setTypAbsence_GroupeInexistant() {
		
		RefTypeAbsenceDto typeAbsenceDto = new RefTypeAbsenceDto();
			typeAbsenceDto.setIdRefTypeAbsence(1);
			typeAbsenceDto.setGroupe("groupe");
		
		RefTypeAbsence typeAbsence = new RefTypeAbsence();
			
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(9005138)).thenReturn(new ReturnMessageDto());

		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(typeAbsenceRepository.getEntity(RefTypeAbsence.class, typeAbsenceDto.getIdRefTypeAbsence())).thenReturn(typeAbsence);
		Mockito.when(typeAbsenceRepository.getEntity(RefGroupeAbsence.class, typeAbsenceDto.getGroupe())).thenReturn(null);
		
		TypeAbsenceServiceImpl service = new  TypeAbsenceServiceImpl();
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
		
		RefTypeAbsenceDto typeAbsenceDto = new RefTypeAbsenceDto();
			typeAbsenceDto.setIdRefTypeAbsence(1);
			typeAbsenceDto.setGroupe("groupe");
			typeAbsenceDto.setTypeSaisiDto(typeSaisiDto);
		
		RefTypeAbsence typeAbsence = new RefTypeAbsence();
			
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(9005138)).thenReturn(new ReturnMessageDto());

		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(typeAbsenceRepository.getEntity(RefTypeAbsence.class, typeAbsenceDto.getIdRefTypeAbsence())).thenReturn(typeAbsence);
		Mockito.when(typeAbsenceRepository.getEntity(RefGroupeAbsence.class, typeAbsenceDto.getGroupe())).thenReturn(new RefGroupeAbsence());
		Mockito.when(typeAbsenceRepository.getEntity(
				RefUnitePeriodeQuota.class, 
				typeSaisiDto.getUnitePeriodeQuotaDto().getIdRefUnitePeriodeQuota())).thenReturn(null);;
		
		TypeAbsenceServiceImpl service = new  TypeAbsenceServiceImpl();
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
		
		RefTypeAbsenceDto typeAbsenceDto = new RefTypeAbsenceDto();
			typeAbsenceDto.setIdRefTypeAbsence(1);
			typeAbsenceDto.setGroupe("groupe");
			typeAbsenceDto.setTypeSaisiDto(typeSaisiDto);
		
		RefTypeAbsence typeAbsence = new RefTypeAbsence();
			
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(9005138)).thenReturn(new ReturnMessageDto());

		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(typeAbsenceRepository.getEntity(RefTypeAbsence.class, typeAbsenceDto.getIdRefTypeAbsence())).thenReturn(typeAbsence);
		Mockito.when(typeAbsenceRepository.getEntity(RefGroupeAbsence.class, typeAbsenceDto.getGroupe())).thenReturn(new RefGroupeAbsence());
		Mockito.when(typeAbsenceRepository.getEntity(
				RefUnitePeriodeQuota.class, 
				typeSaisiDto.getUnitePeriodeQuotaDto().getIdRefUnitePeriodeQuota())).thenReturn(new RefUnitePeriodeQuota());;
		
		TypeAbsenceServiceImpl service = new  TypeAbsenceServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);
		
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
		
		RefTypeAbsenceDto typeAbsenceDto = new RefTypeAbsenceDto();
			typeAbsenceDto.setGroupe("groupe");
			typeAbsenceDto.setTypeSaisiDto(typeSaisiDto);
		
		RefTypeAbsence typeAbsence = new RefTypeAbsence();
			
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(9005138)).thenReturn(new ReturnMessageDto());

		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(typeAbsenceRepository.getEntity(RefTypeAbsence.class, typeAbsenceDto.getIdRefTypeAbsence())).thenReturn(typeAbsence);
		Mockito.when(typeAbsenceRepository.getEntity(RefGroupeAbsence.class, typeAbsenceDto.getGroupe())).thenReturn(new RefGroupeAbsence());
		Mockito.when(typeAbsenceRepository.getEntity(
				RefUnitePeriodeQuota.class, 
				typeSaisiDto.getUnitePeriodeQuotaDto().getIdRefUnitePeriodeQuota())).thenReturn(new RefUnitePeriodeQuota());;
		
		TypeAbsenceServiceImpl service = new  TypeAbsenceServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);
		
		ReturnMessageDto result = service.setTypAbsence(9005138, typeAbsenceDto);

		Mockito.verify(typeAbsenceRepository, Mockito.times(1)).persistEntity(Mockito.isA(RefTypeAbsence.class));
		assertEquals(result.getInfos().get(0), TypeAbsenceServiceImpl.TYPE_ABSENCE_CREE);
	}
	
	@Test
	public void deleteTypeAbsence_UserNonHabilite() {
		
		ReturnMessageDto isSirhDto = new ReturnMessageDto();
		isSirhDto.getErrors().add("erreur");
		
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(9005138)).thenReturn(isSirhDto);
		
		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		
		TypeAbsenceServiceImpl service = new  TypeAbsenceServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);
		
		ReturnMessageDto result = service.deleteTypeAbsence(9005138, 1);

		Mockito.verify(typeAbsenceRepository, Mockito.times(0)).removeEntity(Mockito.isA(RefTypeAbsence.class));
		assertEquals(result.getErrors().get(0), TypeAbsenceServiceImpl.AGENT_NON_HABILITE);
	}
	
	@Test
	public void deleteTypeAbsence_TypeAbsenceInexistant() {
		
		Integer idTypeDemande = 1;
		
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(9005138)).thenReturn(new ReturnMessageDto());
		
		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(typeAbsenceRepository.getEntity(RefTypeAbsence.class, idTypeDemande)).thenReturn(null);
		
		TypeAbsenceServiceImpl service = new  TypeAbsenceServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);
		
		ReturnMessageDto result = service.deleteTypeAbsence(9005138, idTypeDemande);

		Mockito.verify(typeAbsenceRepository, Mockito.times(0)).removeEntity(Mockito.isA(RefTypeAbsence.class));
		assertEquals(result.getErrors().get(0), TypeAbsenceServiceImpl.TYPE_ABSENCE_INEXISTANT);
	}
	
	@Test
	public void deleteTypeAbsence_OK() {
		
		Integer idTypeDemande = 1;
		
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.isUtilisateurSIRH(9005138)).thenReturn(new ReturnMessageDto());
		
		ITypeAbsenceRepository typeAbsenceRepository = Mockito.mock(ITypeAbsenceRepository.class);
		Mockito.when(typeAbsenceRepository.getEntity(RefTypeAbsence.class, idTypeDemande)).thenReturn(new RefTypeAbsence());
		
		TypeAbsenceServiceImpl service = new  TypeAbsenceServiceImpl();
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "typeAbsenceRepository", typeAbsenceRepository);
		
		ReturnMessageDto result = service.deleteTypeAbsence(9005138, idTypeDemande);

		Mockito.verify(typeAbsenceRepository, Mockito.times(1)).removeEntity(Mockito.isA(RefTypeAbsence.class));
		assertEquals(result.getInfos().get(0), TypeAbsenceServiceImpl.TYPE_ABSENCE_SUPPRIME);
	}
	
	
}
