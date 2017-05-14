package nc.noumea.mairie.alfresco.cmis;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.PieceJointe;
import nc.noumea.mairie.abs.domain.RefGroupeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeGroupeAbsenceEnum;
import nc.noumea.mairie.abs.domain.RefTypeSaisi;
import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.PieceJointeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.ws.ISirhWSConsumer;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.staticmock.MockStaticEntityMethods;
import org.springframework.test.util.ReflectionTestUtils;

@MockStaticEntityMethods
public class AlfrescoCMISServiceTest {
	
	@Test
	public void uploadDocument_pasPieceJointe_doNothing() {
		
		Integer idAgentOperateur= 9005138;
		DemandeDto demandeDto = new DemandeDto();
		
		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());
		RefTypeAbsence type = new RefTypeAbsence();
		type.setGroupe(groupe);
		Demande demande = Mockito.spy(new Demande());
		demande.setType(type);
		
		ReturnMessageDto returnDto = new ReturnMessageDto();
		
		CreateSession createSession = Mockito.mock(CreateSession.class);
		
		AlfrescoCMISService service = new AlfrescoCMISService();
		ReflectionTestUtils.setField(service, "createSession", createSession);
		
		returnDto = service.uploadDocument(idAgentOperateur, demandeDto, demande, returnDto, false, false);
		
		assertEquals(0, returnDto.getErrors().size());
		assertEquals(0, returnDto.getInfos().size());
		assertEquals(0, demande.getPiecesJointes().size());
		Mockito.verify(createSession, Mockito.never()).getSession(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
	}
	
	@Test
	public void uploadDocument_pasPieceJointe_obligatoire() {
		
		Integer idAgentOperateur= 9005138;
		DemandeDto demandeDto = new DemandeDto();
		
		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setPieceJointe(true);
		
		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());
		RefTypeAbsence type = new RefTypeAbsence();
		type.setTypeSaisi(typeSaisi);
		type.setGroupe(groupe);
		Demande demande = Mockito.spy(new Demande());
		demande.setType(type);
		
		ReturnMessageDto returnDto = new ReturnMessageDto();
		
		CreateSession createSession = Mockito.mock(CreateSession.class);
		
		AlfrescoCMISService service = new AlfrescoCMISService();
		ReflectionTestUtils.setField(service, "createSession", createSession);
		
		returnDto = service.uploadDocument(idAgentOperateur, demandeDto, demande, returnDto, true, false);
		
		assertEquals(AlfrescoCMISService.ERROR_PJ_ABSENTE, returnDto.getErrors().get(0));
		assertEquals(0, returnDto.getInfos().size());
		assertEquals(0, demande.getPiecesJointes().size());
		Mockito.verify(createSession, Mockito.never()).getSession(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
	}
	
	@Test
	public void uploadDocument_pasPieceJointe_errorPathFolder() {
		
		Integer idAgentOperateur= 9005138;
		Integer idAgent= 9005131;
		String idFolder = "idFolder";
		
		PieceJointeDto pjDto = new PieceJointeDto();
		pjDto.setTitre("titre");
		pjDto.setbFile(new String("data").getBytes());
		
		AgentWithServiceDto agentWithServiceDto = new AgentWithServiceDto();
		
		DemandeDto demandeDto = new DemandeDto();
		demandeDto.getPiecesJointes().add(pjDto);
		demandeDto.setAgentWithServiceDto(agentWithServiceDto);
		
		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());
		RefTypeAbsence type = new RefTypeAbsence();
		type.setGroupe(groupe);
		Demande demande = Mockito.spy(new Demande());
		demande.setType(type);
		demande.setIdAgent(idAgent);
		
		ReturnMessageDto returnDto = new ReturnMessageDto();
		
		Session session = Mockito.mock(Session.class);
		Mockito.when(session.getObject(idFolder)).thenReturn(null);
		
		CreateSession createSession = Mockito.mock(CreateSession.class);
		Mockito.when(createSession.getSession(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(session);
		
		CmisService cmisService = Mockito.mock(CmisService.class);
		Mockito.when(cmisService.getIdObjectCmis(Mockito.anyString(), Mockito.any(Session.class))).thenReturn(idFolder);
		
		AgentGeneriqueDto agentDto = new AgentGeneriqueDto();
		agentDto.setIdAgent(idAgent);
		agentDto.setNomUsage("nomUsage");
		agentDto.setPrenomUsage("prenomUsage");
		
		ISirhWSConsumer sirhWsConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWsConsumer.getAgent(demande.getIdAgent())).thenReturn(agentDto);
		
		AlfrescoCMISService service = new AlfrescoCMISService();
		ReflectionTestUtils.setField(service, "createSession", createSession);
		ReflectionTestUtils.setField(service, "cmisService", cmisService);
		ReflectionTestUtils.setField(service, "sirhWsConsumer", sirhWsConsumer);
		
		returnDto = service.uploadDocument(idAgentOperateur, demandeDto, demande, returnDto, false, false);
		
		assertEquals(CmisUtils.ERROR_PATH, returnDto.getErrors().get(0));
		assertEquals(0, returnDto.getInfos().size());
		assertEquals(0, demande.getPiecesJointes().size());
		Mockito.verify(createSession, Mockito.times(1)).getSession(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
	}
	
	@Test
	public void uploadDocument_pasPieceJointe_addOnePJ_errorUpload() {
		
		Integer idAgentOperateur= 9005138;
		Integer idAgent= 9005131;
		
		PieceJointeDto pjDto = new PieceJointeDto();
		pjDto.setTitre("titre");
		pjDto.setbFile(new String("data").getBytes());
		
		AgentWithServiceDto agentWithServiceDto = new AgentWithServiceDto();
		agentWithServiceDto.setNom("CHARVET");
		agentWithServiceDto.setPrenom("TATIANA");
		
		DemandeDto demandeDto = new DemandeDto();
		demandeDto.getPiecesJointes().add(pjDto);
		demandeDto.setAgentWithServiceDto(agentWithServiceDto);
		
		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setCode("CA");
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());
		RefTypeAbsence type = new RefTypeAbsence();
		type.setGroupe(groupe);
		Demande demande = Mockito.spy(new Demande());
		demande.setType(type);
		demande.setIdAgent(idAgent);
		demande.setDateDebut(new Date());
		
		ReturnMessageDto returnDto = new ReturnMessageDto();
		
		Folder folder = Mockito.mock(Folder.class);

	    OperationContext operationContext = Mockito.mock(OperationContext.class);
	    
		Session session = Mockito.mock(Session.class);
		Mockito.when(session.getObjectByPath(Mockito.anyString())).thenReturn(folder);
		Mockito.when(session.createOperationContext()).thenReturn(operationContext);
		
		CreateSession createSession = Mockito.mock(CreateSession.class);
		Mockito.when(createSession.getSession(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(session);
		
		AgentGeneriqueDto agentDto = new AgentGeneriqueDto();
		agentDto.setIdAgent(idAgent);
		agentDto.setNomUsage("nomUsage");
		agentDto.setPrenomUsage("prenomUsage");
		
		ISirhWSConsumer sirhWsConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWsConsumer.getAgent(demande.getIdAgent())).thenReturn(agentDto);
		
		AlfrescoCMISService service = new AlfrescoCMISService();
		ReflectionTestUtils.setField(service, "createSession", createSession);
		ReflectionTestUtils.setField(service, "sirhWsConsumer", sirhWsConsumer);
		
		returnDto = service.uploadDocument(idAgentOperateur, demandeDto, demande, returnDto, false, false);
		
		assertEquals(CmisUtils.ERROR_UPLOAD, returnDto.getErrors().get(0));
		assertEquals(0, returnDto.getInfos().size());
		assertEquals(0, demande.getPiecesJointes().size());
		Mockito.verify(createSession, Mockito.times(1)).getSession(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		Mockito.verify(folder, Mockito.times(1)).createDocument(Mockito.anyMapOf(String.class, Class.class), Mockito.any(ContentStream.class), Mockito.any(VersioningState.class));
	}
	
	@Test
	public void uploadDocument_pasPieceJointe_addOnePJ() {
		
		Integer idAgentOperateur= 9005138;
		Integer idAgent= 9005131;
		
		PieceJointeDto pjDto = new PieceJointeDto();
		pjDto.setTitre("titre");
		pjDto.setbFile(new String("data").getBytes());
		
		AgentWithServiceDto agentWithServiceDto = new AgentWithServiceDto();
		agentWithServiceDto.setIdAgent(idAgent);
		agentWithServiceDto.setNom("CHARVET");
		agentWithServiceDto.setPrenom("TATIANA");
		
		DemandeDto demandeDto = new DemandeDto();
		demandeDto.setAgentWithServiceDto(agentWithServiceDto);
		demandeDto.getPiecesJointes().add(pjDto);
		
		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setCode("CA");
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());
		RefTypeAbsence type = new RefTypeAbsence();
		type.setGroupe(groupe);
		Demande demande = Mockito.spy(new Demande());
		demande.setType(type);
		demande.setIdAgent(idAgent);
		demande.setDateDebut(new Date());
		
		ReturnMessageDto returnDto = new ReturnMessageDto();

		Property<Object> property = Mockito.mock(Property.class);
		Mockito.when(property.getFirstValue()).thenReturn("nodeRef");
		Document doc = Mockito.mock(Document.class);
		doc.getProperties().add(property);
		Mockito.when(doc.getProperty("alfcmis:nodeRef")).thenReturn(property);
		
		Folder folder = Mockito.mock(Folder.class);
		Mockito.when(folder.createDocument(Mockito.anyMapOf(String.class, Class.class), Mockito.any(ContentStream.class), Mockito.any(VersioningState.class)))
				.thenReturn(doc);

	    OperationContext operationContext = Mockito.mock(OperationContext.class);
	    
		Session session = Mockito.mock(Session.class);
		Mockito.when(session.getObjectByPath(Mockito.anyString())).thenReturn(folder);
		Mockito.when(session.createOperationContext()).thenReturn(operationContext);
		
		CreateSession createSession = Mockito.mock(CreateSession.class);
		Mockito.when(createSession.getSession(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(session);
		
		AgentGeneriqueDto agentDto = new AgentGeneriqueDto();
		agentDto.setIdAgent(idAgent);
		agentDto.setNomUsage("nomUsage");
		agentDto.setPrenomUsage("prenomUsage");
		
		ISirhWSConsumer sirhWsConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWsConsumer.getAgent(demande.getIdAgent())).thenReturn(agentDto);
		
		AlfrescoCMISService service = new AlfrescoCMISService();
		ReflectionTestUtils.setField(service, "createSession", createSession);
		ReflectionTestUtils.setField(service, "sirhWsConsumer", sirhWsConsumer);
		
		returnDto = service.uploadDocument(idAgentOperateur, demandeDto, demande, returnDto, false, false);
		
		assertEquals(0, returnDto.getErrors().size());
		assertEquals(0, returnDto.getInfos().size());
		assertEquals(1, demande.getPiecesJointes().size());
		Mockito.verify(createSession, Mockito.times(1)).getSession(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		Mockito.verify(folder, Mockito.times(1)).createDocument(Mockito.anyMapOf(String.class, Class.class), Mockito.any(ContentStream.class), Mockito.any(VersioningState.class));
	}
	
	@Test
	public void uploadDocument_pasPieceJointe_add2PJ_deleteOnePJ() {
		
		Integer idAgentOperateur= 9005138;
		Integer idAgent= 9005131;
		
		PieceJointeDto pjDto = new PieceJointeDto();
		pjDto.setIdPieceJointe(1);
		pjDto.setTitre("titre");
		pjDto.setbFile(new String("data").getBytes());
		
		PieceJointeDto pjDto2 = new PieceJointeDto();
		pjDto2.setIdPieceJointe(2);
		pjDto2.setTitre("titre2");
		pjDto2.setbFile(new String("data2").getBytes());
		
		AgentWithServiceDto agentWithServiceDto = new AgentWithServiceDto();
		agentWithServiceDto.setIdAgent(idAgent);
		agentWithServiceDto.setNom("CHARVET");
		agentWithServiceDto.setPrenom("TATIANA");
		
		DemandeDto demandeDto = new DemandeDto();
		demandeDto.setAgentWithServiceDto(agentWithServiceDto);
		demandeDto.getPiecesJointes().add(pjDto);
		demandeDto.getPiecesJointes().add(pjDto2);
		
		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setCode("CA");
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());
		RefTypeAbsence type = new RefTypeAbsence();
		type.setGroupe(groupe);
		
		PieceJointe pj = new PieceJointe();
		pj.setIdPieceJointe(3);
		pj.setTitre("titre3");
		
		Demande demande = Mockito.spy(new Demande());
		demande.setType(type);
		demande.setIdAgent(idAgent);
		demande.setDateDebut(new Date());
		demande.getPiecesJointes().add(pj);
		
		ReturnMessageDto returnDto = new ReturnMessageDto();

		Property<Object> property = Mockito.mock(Property.class);
		Mockito.when(property.getFirstValue()).thenReturn("nodeRef");
		Document doc = Mockito.mock(Document.class);
		doc.getProperties().add(property);
		Mockito.when(doc.getProperty("alfcmis:nodeRef")).thenReturn(property);
		
		Folder folder = Mockito.mock(Folder.class);
		Mockito.when(folder.createDocument(Mockito.anyMapOf(String.class, Class.class), Mockito.any(ContentStream.class), Mockito.any(VersioningState.class)))
				.thenReturn(doc);

	    OperationContext operationContext = Mockito.mock(OperationContext.class);
	    
		Session session = Mockito.mock(Session.class);
		Mockito.when(session.getObjectByPath(Mockito.anyString())).thenReturn(folder);
		Mockito.when(session.createOperationContext()).thenReturn(operationContext);
		
		CreateSession createSession = Mockito.mock(CreateSession.class);
		Mockito.when(createSession.getSession(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(session);
		
		AgentGeneriqueDto agentDto = new AgentGeneriqueDto();
		agentDto.setIdAgent(idAgent);
		agentDto.setNomUsage("nomUsage");
		agentDto.setPrenomUsage("prenomUsage");
		
		ISirhWSConsumer sirhWsConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWsConsumer.getAgent(demande.getIdAgent())).thenReturn(agentDto);
		
		AlfrescoCMISService service = new AlfrescoCMISService();
		ReflectionTestUtils.setField(service, "createSession", createSession);
		ReflectionTestUtils.setField(service, "sirhWsConsumer", sirhWsConsumer);
		
		returnDto = service.uploadDocument(idAgentOperateur, demandeDto, demande, returnDto, false, false);
		
		assertEquals(0, returnDto.getErrors().size());
		assertEquals(0, returnDto.getInfos().size());
		assertEquals(2, demande.getPiecesJointes().size());
		Mockito.verify(createSession, Mockito.times(1)).getSession(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		Mockito.verify(folder, Mockito.times(2)).createDocument(Mockito.anyMapOf(String.class, Class.class), Mockito.any(ContentStream.class), Mockito.any(VersioningState.class));
	}
	
	@Test
	public void removeAllDocument_noPJ_doNothing() {

		ReturnMessageDto returnDto = new ReturnMessageDto();

		Demande demande = Mockito.spy(new Demande());
		demande.setDateDebut(new Date());
		
		Session session = Mockito.mock(Session.class);
		
		CreateSession createSession = Mockito.mock(CreateSession.class);
		Mockito.when(createSession.getSession(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(session);
		
		AlfrescoCMISService service = new AlfrescoCMISService();
		ReflectionTestUtils.setField(service, "createSession", createSession);
		
		returnDto = service.removeAllDocument(returnDto, demande);

		assertEquals(0, returnDto.getErrors().size());
		assertEquals(0, returnDto.getInfos().size());
		Mockito.verify(createSession, Mockito.never()).getSession(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
	}
	
	@Test
	public void removeAllDocument_2PJ() {

		ReturnMessageDto returnDto = new ReturnMessageDto();

		PieceJointe pj = new PieceJointe();
		pj.setIdPieceJointe(1);
		pj.setTitre("titre1");
		pj.setNodeRefAlfresco("nodeRefAlfresco");

		PieceJointe pj2 = new PieceJointe();
		pj2.setIdPieceJointe(2);
		pj2.setTitre("titre2");
		pj2.setNodeRefAlfresco("nodeRefAlfresco2");
		
		Demande demande = Mockito.spy(new Demande());
		demande.setDateDebut(new Date());
		demande.getPiecesJointes().add(pj);
		demande.getPiecesJointes().add(pj2);
		
		CmisObject object = Mockito.mock(CmisObject.class);
		CmisObject object2 = Mockito.mock(CmisObject.class);
		
		Session session = Mockito.mock(Session.class);
		Mockito.when(session.getObject(pj.getNodeRefAlfresco())).thenReturn(object);
		Mockito.when(session.getObject(pj2.getNodeRefAlfresco())).thenReturn(object2);
		
		CreateSession createSession = Mockito.mock(CreateSession.class);
		Mockito.when(createSession.getSession(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(session);
		
		AlfrescoCMISService service = new AlfrescoCMISService();
		ReflectionTestUtils.setField(service, "createSession", createSession);
		
		returnDto = service.removeAllDocument(returnDto, demande);

		assertEquals(0, returnDto.getErrors().size());
		assertEquals(0, returnDto.getInfos().size());
		Mockito.verify(createSession, Mockito.times(1)).getSession(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		Mockito.verify(object, Mockito.times(1)).delete();
		Mockito.verify(object2, Mockito.times(1)).delete();
	}
}
