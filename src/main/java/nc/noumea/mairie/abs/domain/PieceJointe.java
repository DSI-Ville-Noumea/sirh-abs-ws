package nc.noumea.mairie.abs.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "ABS_PIECE_JOINTE")
@PersistenceUnit(unitName = "absPersistenceUnit")
public class PieceJointe {

	@Id
	@Column(name = "ID_PIECE_JOINTE")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idPieceJointe;
	
	@ManyToOne
	@JoinColumn(name = "ID_DEMANDE", referencedColumnName = "ID_DEMANDE", updatable = true, insertable = true)
	private Demande demande;

	@NotNull
	@Column(name = "NODE_REF_ALFRESCO")
	private String nodeRefAlfresco;

	@NotNull
	@Column(name = "TITRE")
	private String titre;
	
	@Column(name = "DATE_MODIFICATION")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateModification;

	@Column(name = "COMMENTAIRE")
	private String commentaire;

	@Column(name = "IS_VISIBLE_KIOSQUE")
	private boolean isVisibleKiosque;

	@Column(name = "IS_VISIBLE_SIRH")
	private boolean isVisibleSirh;

	public Integer getIdPieceJointe() {
		return idPieceJointe;
	}

	public void setIdPieceJointe(Integer idPieceJointe) {
		this.idPieceJointe = idPieceJointe;
	}

	public Demande getDemande() {
		return demande;
	}

	public void setDemande(Demande demande) {
		this.demande = demande;
	}

	public String getNodeRefAlfresco() {
		return nodeRefAlfresco;
	}

	public void setNodeRefAlfresco(String nodeRefAlfresco) {
		this.nodeRefAlfresco = nodeRefAlfresco;
	}

	public String getTitre() {
		return titre;
	}

	public void setTitre(String titre) {
		this.titre = titre;
	}

	public Date getDateModification() {
		return dateModification;
	}

	public void setDateModification(Date dateModification) {
		this.dateModification = dateModification;
	}

	public String getCommentaire() {
		return commentaire;
	}

	public void setCommentaire(String commentaire) {
		this.commentaire = commentaire;
	}

	public boolean isVisibleKiosque() {
		return isVisibleKiosque;
	}

	public void setVisibleKiosque(boolean isVisibleKiosque) {
		this.isVisibleKiosque = isVisibleKiosque;
	}

	public boolean isVisibleSirh() {
		return isVisibleSirh;
	}

	public void setVisibleSirh(boolean isVisibleSirh) {
		this.isVisibleSirh = isVisibleSirh;
	}
	
}
