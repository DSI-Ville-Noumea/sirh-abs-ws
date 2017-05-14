package nc.noumea.mairie.abs.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "ABS_CONTROLE_MEDICAL")
@PersistenceUnit(unitName = "absPersistenceUnit")
public class ControleMedical {

	@Id
	@Column(name = "ID_ABS_CONTROLE_MEDICAL")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@NotNull
	@JoinColumn(name="ID_DEMANDE", table="ABS_DEMANDE_MALADIES")
	private Integer idDemandeMaladie;

	@NotNull
	@Column(name = "DATE_DEMANDE_CONTROLE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date date;

	@NotNull
	@Column(name = "ID_AGENT_DEMANDEUR")
	private Integer idAgent;
	
	@NotNull
	@Column(name = "COMMENTAIRE")
	private String commentaire;
	
	public ControleMedical() {
		super();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getIdDemandeMaladie() {
		return idDemandeMaladie;
	}

	public void setIdDemandeMaladie(Integer idDemandeMaladie) {
		this.idDemandeMaladie = idDemandeMaladie;
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

	public String getCommentaire() {
		return commentaire;
	}

	public void setCommentaire(String commentaire) {
		this.commentaire = commentaire;
	}

}
