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
	
	@Column(name = "MINUTES")
	private int minutes;
	
	@Column(name = "MINUTES_ANNEE_N1")
	private int minutesAnneeN1;
	
	@ManyToOne()
	@JoinColumn(name = "ID_MOTIF_COMPTEUR", referencedColumnName = "ID_MOTIF_COMPTEUR")
	private MotifCompteur motifCompteur;
	
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

	public int getMinutes() {
		return minutes;
	}

	public void setMinutes(int minutes) {
		this.minutes = minutes;
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

	public int getMinutesAnneeN1() {
		return minutesAnneeN1;
	}

	public void setMinutesAnneeN1(int minutesAnneeN1) {
		this.minutesAnneeN1 = minutesAnneeN1;
	}
	
	
}
