package nc.noumea.mairie.abs.service.impl;

import static org.junit.Assert.fail;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IDemandeRepository;
import nc.noumea.mairie.abs.service.IAccessRightsService;
import nc.noumea.mairie.abs.web.AccessForbiddenException;
import nc.noumea.mairie.abs.web.NoContentException;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class ReportingServiceTest {

	@Test
	public void getDemandeReportAsByteArray_NoContentException() {
		
		Integer idAgent = 9005138;
		Integer idDemande = 1;
		
		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
			Mockito.when(demandeRepository.getEntity(Demande.class, idDemande)).thenReturn(null);
		
		ReportingService service = new ReportingService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		try {
			service.getDemandeReportAsByteArray(idAgent, idDemande);
		} catch (NoContentException e) {
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		fail("Should have thrown an NoContentException");
	}
	
	@Test
	public void getDemandeReportAsByteArray_AccessForbiddenException() {
		
		Integer idAgent = 9005138;
		Integer idDemande = 1;
		ReturnMessageDto returnDto = new ReturnMessageDto();
			returnDto.getErrors().add("erreur");
		
		Demande demande = new Demande();
			demande.setIdAgent(9005138);
		
		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
			Mockito.when(demandeRepository.getEntity(Demande.class, idDemande)).thenReturn(demande);
		
		IAccessRightsService accessRightsService = Mockito.mock(IAccessRightsService.class);
			Mockito.when(accessRightsService.verifAccessRightDemande(Mockito.anyInt(), Mockito.anyInt(), Mockito.isA(ReturnMessageDto.class))).thenReturn(returnDto);
		
		ReportingService service = new ReportingService();
			ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
			ReflectionTestUtils.setField(service, "accessRightsService", accessRightsService);
			
		try {
			service.getDemandeReportAsByteArray(idAgent, idDemande);
		} catch (NoContentException e) {
		} catch (AccessForbiddenException e) {
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		fail("Should have thrown an AccessForbiddenException");
	}
}
