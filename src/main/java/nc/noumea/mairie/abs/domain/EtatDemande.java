package nc.noumea.mairie.abs.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "ABS_ETAT_DEMANDE")
@PersistenceUnit(unitName = "absPersistenceUnit")
@Inheritance(strategy = InheritanceType.JOINED)
public class EtatDemande {

	@Id
	@Column(name = "ID_ETAT_DEMANDE")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idEtatDemande;

	@Column(name = "DATE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date date;

	@NotNull
	@Column(name = "ID_AGENT")
	private Integer idAgent;

	@Column(name = "DATE_DEBUT")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateDebut;

	@Column(name = "DATE_FIN")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateFin;

	@ManyToOne
	@JoinColumn(name = "ID_DEMANDE", referencedColumnName = "ID_DEMANDE", updatable = true, insertable = true)
	private Demande demande;

	@NotNull
	@Column(name = "ID_REF_ETAT")
	@Enumerated(EnumType.ORDINAL)
	private RefEtatEnum etat;

	@Column(name = "MOTIF", columnDefinition = "text")
	private String motif;

	@Column(name = "COMMENTAIRE")
	private String commentaire;

	public Integer getIdEtatDemande() {
		return idEtatDemande;
	}

	public void setIdEtatDemande(Integer idEtatDemande) {
		this.idEtatDemande = idEtatDemande;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Integer getIdAgent() {
		return idAgent;
	}

	public void setIdAgent(Integer idAgent) {
		this.idAgent = idAgent;
	}

	public Demande getDemande() {
		return demande;
	}

	public void setDemande(Demande demande) {
		this.demande = demande;
	}

	public RefEtatEnum getEtat() {
		return etat;
	}

	public void setEtat(RefEtatEnum etat) {
		this.etat = etat;
	}

	public String getMotif() {
		return motif;
	}

	public void setMotif(String motif) {
		this.motif = motif;
	}

	public Date getDateDebut() {
		return dateDebut;
	}

	public void setDateDebut(Date dateDebut) {
		this.dateDebut = dateDebut;
	}

	public Date getDateFin() {
		return dateFin;
	}

	public void setDateFin(Date dateFin) {
		this.dateFin = dateFin;
	}

	public String getCommentaire() {
		return commentaire;
	}

	public void setCommentaire(String commentaire) {
		this.commentaire = commentaire;
	}

}
