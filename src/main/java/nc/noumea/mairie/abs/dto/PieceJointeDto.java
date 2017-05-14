package nc.noumea.mairie.abs.dto;

import java.io.Serializable;
import java.util.Date;

import nc.noumea.mairie.abs.domain.PieceJointe;
import nc.noumea.mairie.alfresco.cmis.AlfrescoCMISService;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class PieceJointeDto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6418009304707574279L;
	
	private Integer idPieceJointe;
	private byte[] bFile;
	private String typeFile;
	private String titre;
	private String urlFromAlfresco;
	private String nodeRefAlfresco;
	private String commentaire;
	@JsonSerialize(using = JsonDateSerializer.class)
	@JsonDeserialize(using = JsonDateDeserializer.class)
	private Date dateModification;
	
	public PieceJointeDto() {
	}
	
	public PieceJointeDto(PieceJointe pj) {
		this();
		this.idPieceJointe = pj.getIdPieceJointe();
		this.titre = pj.getTitre();
		this.urlFromAlfresco = AlfrescoCMISService.getUrlOfDocument(pj.getNodeRefAlfresco());
		this.nodeRefAlfresco = pj.getNodeRefAlfresco();
		this.commentaire = pj.getCommentaire();
		this.dateModification = pj.getDateModification();
	}
	
	
	public Integer getIdPieceJointe() {
		return idPieceJointe;
	}

	public void setIdPieceJointe(Integer idPieceJointe) {
		this.idPieceJointe = idPieceJointe;
	}

	public byte[] getbFile() {
		return bFile;
	}
	public void setbFile(byte[] bFile) {
		this.bFile = bFile;
	}
	public String getTypeFile() {
		return typeFile;
	}
	public void setTypeFile(String typeFile) {
		this.typeFile = typeFile;
	}
	public String getTitre() {
		return titre;
	}
	public void setTitre(String titre) {
		this.titre = titre;
	}
	public String getUrlFromAlfresco() {
		return urlFromAlfresco;
	}
	public void setUrlFromAlfresco(String urlFromAlfresco) {
		this.urlFromAlfresco = urlFromAlfresco;
	}

	public String getNodeRefAlfresco() {
		return nodeRefAlfresco;
	}

	public void setNodeRefAlfresco(String nodeRefAlfresco) {
		this.nodeRefAlfresco = nodeRefAlfresco;
	}

	public String getCommentaire() {
		return commentaire;
	}

	public void setCommentaire(String commentaire) {
		this.commentaire = commentaire;
	}

	public Date getDateModification() {
		return dateModification;
	}

	public void setDateModification(Date dateModification) {
		this.dateModification = dateModification;
	}
	
}
