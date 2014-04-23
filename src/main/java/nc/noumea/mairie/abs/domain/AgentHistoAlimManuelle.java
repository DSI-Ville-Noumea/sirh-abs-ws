package nc.noumea.mairie.abs.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "ABS_AGENT_HISTO_ALIM_MANUELLE")
@PersistenceUnit(unitName = "absPersistenceUnit")
public class AgentHistoAlimManuelle {

	@Id
	@Column(name = "ID_AGENT_HISTO_ALIM_MANUELLE")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idAgentHistoAlimManuelle;

	@NotNull
	@Column(name = "ID_AGENT")
	private Integer idAgent;

	@Column(name = "DATE_MODIFICATION")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateModification;

	@ManyToOne()
	@JoinColumn(name = "ID_MOTIF_COMPTEUR", referencedColumnName = "ID_MOTIF_COMPTEUR")
	private MotifCompteur motifCompteur;

	@OneToOne(optional = true)
	@JoinColumn(name = "ID_TYPE_DEMANDE")
	private RefTypeAbsence type;

	@Column(name = "MOTIF_TECHNIQUE")
	private String motifTechnique;

	@Column(name = "ID_AGENT_CONCERNE")
	private Integer idAgentConcerne;

	@Column(name = "TEXT", columnDefinition = "text")
	private String text;

	@OneToOne(optional = true)
	@JoinColumn(name = "ID_AGENT_COUNT")
	private AgentCount compteurAgent;

	@Version
	@Column(name = "version")
	private Integer version;

	public Integer getIdAgentHistoAlimManuelle() {
		return idAgentHistoAlimManuelle;
	}

	public void setIdAgentHistoAlimManuelle(Integer idAgentHistoAlimManuelle) {
		this.idAgentHistoAlimManuelle = idAgentHistoAlimManuelle;
	}

	public Integer getIdAgent() {
		return idAgent;
	}

	public void setIdAgent(Integer idAgent) {
		this.idAgent = idAgent;
	}

	public Date getDateModification() {
		return dateModification;
	}

	public void setDateModification(Date dateModification) {
		this.dateModification = dateModification;
	}

	public MotifCompteur getMotifCompteur() {
		return motifCompteur;
	}

	public void setMotifCompteur(MotifCompteur motifCompteur) {
		this.motifCompteur = motifCompteur;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public RefTypeAbsence getType() {
		return type;
	}

	public void setType(RefTypeAbsence type) {
		this.type = type;
	}

	public String getMotifTechnique() {
		return motifTechnique;
	}

	public void setMotifTechnique(String motifTechnique) {
		this.motifTechnique = motifTechnique;
	}

	public Integer getIdAgentConcerne() {
		return idAgentConcerne;
	}

	public void setIdAgentConcerne(Integer idAgentConcerne) {
		this.idAgentConcerne = idAgentConcerne;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public AgentCount getCompteurAgent() {
		return compteurAgent;
	}

	public void setCompteurAgent(AgentCount compteurAgent) {
		this.compteurAgent = compteurAgent;
	}

}
