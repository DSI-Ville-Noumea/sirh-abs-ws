package nc.noumea.mairie.alfresco.cmis;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.PieceJointe;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.domain.RefTypeGroupeAbsenceEnum;
import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.PieceJointeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.ws.ISirhWSConsumer;

@Service
public class AlfrescoCMISService implements IAlfrescoCMISService {
	
	private Logger logger = LoggerFactory.getLogger(AlfrescoCMISService.class);
	
	protected static final String ERROR_PJ_ABSENTE = "Une pièce jointe est obligatoire avec la demande.";
	
	@Autowired
	@Qualifier("alfrescoUrl")
	private String alfrescoUrl;
	
	@Autowired
	@Qualifier("alfrescoLogin")
	private String alfrescoLogin;
	
	@Autowired
	@Qualifier("alfrescoPassword")
	private String alfrescoPassword;
	
	private static String staticAlfrescoUrl;
	
	@Autowired
	private ISirhWSConsumer sirhWsConsumer;
	
	@Autowired
	private CreateSession createSession;
	
	@PostConstruct
	public void init() {
		AlfrescoCMISService.staticAlfrescoUrl = alfrescoUrl;
    }
	
	
	@Override
	public ReturnMessageDto uploadDocument(Integer idAgentOperateur, DemandeDto demandeDto, Demande demande, 
			ReturnMessageDto returnDto, boolean isFromKiosqueRH, boolean isFromHSCT) {
		
		if(null == RefTypeGroupeAbsenceEnum.getPathAlfrescoByType(demande.getType().getGroupe().getIdRefGroupeAbsence())) {
			return returnDto;
		}
		
		boolean pieceJointeObligatoire = 
				(demande.getType().getIdRefTypeAbsence() != null &&
				demande.getType().getIdRefTypeAbsence().equals(RefTypeAbsenceEnum.CONGE_ANNUEL.getValue()))
				? 
				(null != demande.getType().getTypeSaisiCongeAnnuel() && demande.getType().getTypeSaisiCongeAnnuel().isPieceJointe())
				: 
				(null != demande.getType().getTypeSaisi() && demande.getType().getTypeSaisi().isPieceJointe());
		
		if(isFromKiosqueRH && pieceJointeObligatoire
				&& (null == demandeDto.getPiecesJointes() || demandeDto.getPiecesJointes().isEmpty())
				&& (null == demande.getPiecesJointes() || demande.getPiecesJointes().isEmpty())
				) {
			logger.debug(ERROR_PJ_ABSENTE);
			returnDto.getErrors().add(ERROR_PJ_ABSENTE);
			return returnDto;
		}
		
		// si pas de fichier, pas d upload
		if((null == demandeDto.getPiecesJointes()
				|| demandeDto.getPiecesJointes().isEmpty())
			&& (null == demande.getPiecesJointes()
					|| demande.getPiecesJointes().isEmpty())) {
			return returnDto;
		}
		
		Session session = null;
		try {
			session = createSession.getSession(alfrescoUrl, alfrescoLogin, alfrescoPassword);
		} catch(CmisConnectionException e) {
			logger.debug("Erreur de connexion a Alfresco CMIS : " + e.getMessage());
			returnDto.getErrors().add("Erreur de connexion à Alfresco CMIS");
			return returnDto;
		}
		
		// suppression des pieces jointes par l utilisateur
		removeDocument(session, returnDto, demande, demandeDto);
	    
		int i = 1;
		for(PieceJointeDto pjDto : demandeDto.getPiecesJointes()) {
			
			// si un nodeRef Alfresco existe deja
			// c est que le document a deja ete uploade vers alfresco
			// nous sommes dans une modification de demande
			if(null != pjDto.getNodeRefAlfresco()
					&& !"".equals(pjDto.getNodeRefAlfresco())) {
				continue;
			}
			
			String nom = demandeDto.getAgentWithServiceDto().getNom();
	    	String prenom = demandeDto.getAgentWithServiceDto().getPrenom();
	    	if(null == nom
	    			|| null == prenom) {
	    		AgentGeneriqueDto agentDto = sirhWsConsumer.getAgent(demandeDto.getAgentWithServiceDto().getIdAgent());
	    		
	    		if(null != agentDto) {
		    		nom = agentDto.getDisplayNom();
		    		prenom = agentDto.getDisplayPrenom();
	    		}
	    	}
			
			// on cherche le repertoire distant 
		    CmisObject object = null;
		    try {
		    	object = session.getObjectByPath(CmisUtils.getPathAbsence(
		    			demande.getIdAgent(), nom, prenom, demande.getType().getGroupe().getIdRefGroupeAbsence(), isFromHSCT));
		    } catch(CmisUnauthorizedException e) {
		    	logger.debug("Probleme d autorisation Alfresco CMIS : " + e.getMessage());
				returnDto.getErrors().add("Erreur Alfresco CMIS : non autorisé");
				return returnDto;
		    } catch(CmisObjectNotFoundException e) {
		    	logger.debug("Le dossier agent n'existe pas sous Alfresco : " + e.getMessage());
				returnDto.getErrors().add("Impossible d'ajouter une pièce jointe : répertoire distant non trouvé.");
				return returnDto;
		    }
		    
		    if(null == object) {
		    	returnDto.getErrors().add(CmisUtils.ERROR_PATH);
		    	return returnDto;
		    }
		    
		    Folder folder = (Folder) object;
		    int maxItemsPerPage = 5;
		    OperationContext operationContext = session.createOperationContext();
		    operationContext.setMaxItemsPerPage(maxItemsPerPage);

		    Document doc = null;
		    boolean isCreated = false;
		    while(!isCreated) {
		    	
		    	String name = CmisUtils.getPatternAbsence(demande.getType().getGroupe().getCode(), nom, 
		    			prenom, demande.getDateDebut(), i);
			    
		    	// properties 
				Map<String, Object> properties = new HashMap<String, Object>();
				properties.put(PropertyIds.NAME, name);
				properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
				properties.put(PropertyIds.DESCRIPTION, getDescriptionOfAbsence(demande));
				
				
				ByteArrayInputStream stream = new ByteArrayInputStream(pjDto.getbFile());
				
			    ContentStream contentStream = new ContentStreamImpl(name, BigInteger.valueOf(pjDto.getbFile().length), pjDto.getTypeFile(), stream);
			    
				// create a major version
			    try {
			    	doc = folder.createDocument(properties, contentStream, VersioningState.MAJOR);
			    	isCreated = true;
			    } catch(CmisContentAlreadyExistsException e) {
			    	logger.debug(e.getMessage());
			    	i++;
			    }
		    }
			
			if(null == doc) {
				returnDto.getErrors().add(CmisUtils.ERROR_UPLOAD);
				return returnDto;
			}
			
			if(null != doc.getProperty("cmis:secondaryObjectTypeIds")) {
				List<Object> aspects = doc.getProperty("cmis:secondaryObjectTypeIds").getValues();
				if (!aspects.contains("P:mairie:customDocumentAspect")) {
					aspects.add("P:mairie:customDocumentAspect");
					HashMap<String, Object> props = new HashMap<String, Object>();
					props.put("cmis:secondaryObjectTypeIds", aspects);
					doc.updateProperties(props);
					logger.debug("Added aspect");
				} else {
					logger.debug("Doc already had aspect");
				}
			}
		 
			HashMap<String, Object> props = new HashMap<String, Object>();
			props.put("mairie:idAgentOwner", demandeDto.getAgentWithServiceDto().getIdAgent());
			props.put("mairie:idAgentCreateur", idAgentOperateur);
			props.put("mairie:commentaire", pjDto.getCommentaire());
			doc.updateProperties(props);
			
			PieceJointe pj = new PieceJointe();
			pj.setNodeRefAlfresco(doc.getProperty("alfcmis:nodeRef").getFirstValue().toString());
			pj.setTitre(doc.getName());
			pj.setDemande(demande);
			pj.setDateModification(new Date());
			pj.setCommentaire(pjDto.getCommentaire());
			pj.setVisibleKiosque(!isFromHSCT);
			pj.setVisibleSirh(true);
			
			demande.getPiecesJointes().add(pj);
			
			i++;
		}
		
		return returnDto;
	}

	//TODO reduire la taille de cette methode
	@Override
	public ReturnMessageDto uploadDocumentWithBuffer(Integer idAgent, Integer idAgentOperateur, InputStream inputStream, 
			Demande demande, ReturnMessageDto returnDto, String typeFile) {
		
		if(null == RefTypeGroupeAbsenceEnum.getPathAlfrescoByType(demande.getType().getGroupe().getIdRefGroupeAbsence())) {
			return returnDto;
		}
		
		// si pas de fichier, pas d upload
		if(null == inputStream) {
			return returnDto;
		}
		
		Session session = null;
		try {
			session = createSession.getSession(alfrescoUrl, alfrescoLogin, alfrescoPassword);
		} catch(CmisConnectionException e) {
			logger.debug("Erreur de connexion a Alfresco CMIS : " + e.getMessage());
			returnDto.getErrors().add("Erreur de connexion à Alfresco CMIS");
			return returnDto;
		}
		
		AgentGeneriqueDto agentDto = sirhWsConsumer.getAgent(demande.getIdAgent());
		String nom = "";
		String prenom = "";
		if(null != agentDto) {
    		nom = agentDto.getDisplayNom();
    		prenom = agentDto.getDisplayPrenom();
		}
		
		// on cherche le repertoire distant 
	    CmisObject object = null;
	    try {
	    	object = session.getObjectByPath(CmisUtils.getPathAbsence(
	    			demande.getIdAgent(), nom, prenom, demande.getType().getGroupe().getIdRefGroupeAbsence(), false));
	    } catch(CmisUnauthorizedException e) {
	    	logger.debug("Probleme d autorisation Alfresco CMIS : " + e.getMessage());
			returnDto.getErrors().add("Erreur Alfresco CMIS : non autorisé");
			return returnDto;
	    } catch(CmisObjectNotFoundException e) {
	    	logger.debug("Le dossier agent n'existe pas sous Alfresco : " + e.getMessage());
			returnDto.getErrors().add("Impossible d'ajouter une pièce jointe : répertoire distant non trouvé.");
			return returnDto;
	    }
	    
	    if(null == object) {
	    	returnDto.getErrors().add(CmisUtils.ERROR_PATH);
	    	return returnDto;
	    }
	    
	    Folder folder = (Folder) object;
	    int maxItemsPerPage = 5;
	    OperationContext operationContext = session.createOperationContext();
	    operationContext.setMaxItemsPerPage(maxItemsPerPage);

	    Document doc = null;
	    boolean isCreated = false;
	    int incrementDoc = 1;
	    
	    //TODO sortir ce bout de code en fonction
	    // creation du nom de fichier
	    boolean nameOk = false;
		String name = null;
	    whileNameOk:while(!nameOk) {
	    	
	    	name = CmisUtils.getPatternAbsence(demande.getType().getGroupe().getCode(), nom, prenom, demande.getDateDebut(), incrementDoc);
		    for(PieceJointe pj : demande.getPiecesJointes()) {
		    	if(name.equals(pj.getTitre())) {
		    		incrementDoc++;
		    		continue whileNameOk;
		    	}
		    }

	    	nameOk= true;
	    }
	    
	    
	    while(!isCreated) {
	    	// properties 
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put(PropertyIds.NAME, name);
			properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
			properties.put(PropertyIds.DESCRIPTION, getDescriptionOfAbsence(demande));
			
			ContentStream contentStream = new ContentStreamImpl(name, null, typeFile, inputStream);
		    
			// create a major version
		    try {
		    	doc = folder.createDocument(properties, contentStream, VersioningState.MAJOR);
		    	isCreated = true;
		    } catch(CmisContentAlreadyExistsException e) {
		    	logger.debug(e.getMessage());
		    	incrementDoc++;
		    	name = CmisUtils.getPatternAbsence(demande.getType().getGroupe().getCode(), nom, prenom, demande.getDateDebut(), incrementDoc);
		    }
	    }
		
		if(null == doc) {
			returnDto.getErrors().add(CmisUtils.ERROR_UPLOAD);
			return returnDto;
		}
		
		if(null != doc.getProperty("cmis:secondaryObjectTypeIds")) {
			List<Object> aspects = doc.getProperty("cmis:secondaryObjectTypeIds").getValues();
			if (!aspects.contains("P:mairie:customDocumentAspect")) {
				aspects.add("P:mairie:customDocumentAspect");
				HashMap<String, Object> props = new HashMap<String, Object>();
				props.put("cmis:secondaryObjectTypeIds", aspects);
				doc.updateProperties(props);
				logger.debug("Added aspect");
			} else {
				logger.debug("Doc already had aspect");
			}
		}
	 
		HashMap<String, Object> props = new HashMap<String, Object>();
		props.put("mairie:idAgentOwner", demande.getIdAgent());
		props.put("mairie:idAgentCreateur", idAgentOperateur);
		doc.updateProperties(props);
		
		PieceJointe pj = new PieceJointe();
		pj.setNodeRefAlfresco(doc.getProperty("alfcmis:nodeRef").getFirstValue().toString());
		pj.setTitre(doc.getName());
		pj.setDemande(demande);
		pj.setDateModification(new Date());
		pj.setVisibleKiosque(true);
		pj.setVisibleSirh(true);
		
		demande.getPiecesJointes().add(pj);
		
		return returnDto;
	}
	
	private String getDescriptionOfAbsence(Demande demande) {
		String description = "";
		if(null != demande.getType().getLabel()) {
			description = demande.getType().getLabel();
		}else if(null != demande.getType().getTypeSaisi()
				&& null != demande.getType().getTypeSaisi().getType()
				&& null != demande.getType().getTypeSaisi().getType().getLabel()) {
			description = demande.getType().getTypeSaisi().getType().getLabel();
		}
		
		return description;
	}

	@Override
	public ReturnMessageDto removeDocument(ReturnMessageDto returnDto, Demande demande, DemandeDto demandeDto) {
		Session session = null;
		try {
			session = createSession.getSession(alfrescoUrl, alfrescoLogin, alfrescoPassword);
		} catch(CmisConnectionException e) {
			logger.debug("Erreur de connexion a Alfresco CMIS : " + e.getMessage());
			returnDto.getErrors().add("Erreur de connexion à Alfresco CMIS");
			return returnDto;
		}
		
		return removeDocument(session, returnDto, demande, demandeDto);
	}
	
	@Override
	public ReturnMessageDto removeDocument(Session session, ReturnMessageDto returnDto, Demande demande, DemandeDto demandeDto) {
		
		if(null != demande.getPiecesJointes()) {
			
			List<PieceJointe> listPJToDelete = new ArrayList<PieceJointe>(); 
			
			for(PieceJointe pj : demande.getPiecesJointes()) {
				
				boolean isExistInDto = false;
				// la pj est elle encore presente dans le DTO
				// si oui on fait rien
				if(null != demandeDto.getPiecesJointes()) {
					for(PieceJointeDto pjDto : demandeDto.getPiecesJointes()) {
						if(pjDto.getIdPieceJointe() == null ||  // #37756 dans le cas d une nouvelle piece jointe, celle ci est creee apres par un nouvel appel de WS demandes/savePieceJointesWithStream
								pjDto.getIdPieceJointe().equals(pj.getIdPieceJointe())) {
							isExistInDto = true;
							break;
						}
					}
				}
				
				// sinon on supprime
				if(!isExistInDto) {
				    
				    listPJToDelete.add(pj);
				    
					CmisObject object = null;
					try {
						object = session.getObject(pj.getNodeRefAlfresco());
					} catch(CmisObjectNotFoundException e) {
						logger.debug("removeDocument : document non trouve dans Alfresco : ID " + pj.getNodeRefAlfresco());
						continue;
					}
					
				    if(null == object) {
						logger.debug("removeDocument : document non trouve dans Alfresco : ID " + pj.getNodeRefAlfresco());
				    	continue;
				    }
				    
				    object.delete();
				}
			}
			
			demande.getPiecesJointes().removeAll(listPJToDelete);
		}
		
		return returnDto;
	}
	
	@Override
	public ReturnMessageDto removeAllDocument(ReturnMessageDto returnDto, Demande demande) {
		
		if(null == demande.getPiecesJointes()
				|| demande.getPiecesJointes().isEmpty()) {
			return returnDto;
		}
		
		Session session = createSession.getSession(alfrescoUrl, alfrescoLogin, alfrescoPassword);
	    
		for(PieceJointe pj : demande.getPiecesJointes()) {
			
			CmisObject object = null;
			try {
				object = session.getObject(pj.getNodeRefAlfresco());
			} catch(CmisObjectNotFoundException e) {
				logger.debug("removeDocument : document non trouve dans Alfresco : ID " + pj.getNodeRefAlfresco());
				continue;
			}
			
		    if(null == object) {
				logger.debug("removeDocument : document non trouve dans Alfresco : ID " + pj.getNodeRefAlfresco());
		    	continue;
		    }
		    
		    object.delete();
		}
		
		return returnDto;
	}
	
	/**
	 * exemple de nodeRef : "workspace://SpacesStore/1a344bd7-6422-45c6-94f7-5640048b20ab"
	 * exemple d URL a retourner :
	 * http://localhost:8080/alfresco/service/api/node/workspace/SpacesStore/418c511a-7c0a-4bb1-95a2-37e5946be726/content
	 * 
	 * @param nodeRef String
	 * @return String l URL pour acceder au document directement a alfresco
	 */
	public static String getUrlOfDocument(String nodeRef) {
		
		return CmisUtils.getUrlOfDocument(staticAlfrescoUrl, nodeRef);
	}

	public ISirhWSConsumer getSirhWsConsumer() {
		return sirhWsConsumer;
	}

	public void setSirhWsConsumer(ISirhWSConsumer sirhWsConsumer) {
		this.sirhWsConsumer = sirhWsConsumer;
	}

}
