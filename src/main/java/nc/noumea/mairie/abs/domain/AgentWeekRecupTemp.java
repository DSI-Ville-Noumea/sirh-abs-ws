package nc.noumea.mairie.abs.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "ABS_AGENT_WEEK_RECUP_TEMP")
@PersistenceUnit(unitName = "absPersistenceUnit")
@NamedQueries({ 
	@NamedQuery(name = "findAgentWeekRecupTempByIdAgent", query = "select awr from AgentWeekRecupTemp awr where awr.idAgent = :idAgent"),
	@NamedQuery(name = "getWeekHistoRecupCountTempByIdAgentAndDate", query = "select awr from AgentWeekRecupTemp awr where awr.idAgent = :idAgent and awr.date = :date")
})
public class AgentWeekRecupTemp {

	@Id
	@Column(name = "ID_AGENT_WEEK_RECUP_TEMP")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idAgentWeekRecupTemp;
	
	@NotNull
	@Column(name = "ID_AGENT")
	private Integer idAgent;
	
	@NotNull
	@Column(name = "ID_POINTAGE")
	private Integer idPointage;

	@NotNull
	@Column(name = "MINUTES")
	private int minutes;

	@Column(name = "DATE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date date;

	@Column(name = "DATE_MODIFICATION")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastModification;
	
	@Version
    @Column(name = "version")
	private Integer version;

	public Integer getIdAgentWeekRecupTemp() {
		return idAgentWeekRecupTemp;
	}

	public void setIdAgentWeekRecupTemp(Integer idAgentWeekRecupTemp) {
		this.idAgentWeekRecupTemp = idAgentWeekRecupTemp;
	}

	public Integer getIdAgent() {
		return idAgent;
	}

	public void setIdAgent(Integer idAgent) {
		this.idAgent = idAgent;
	}

	public int getMinutes() {
		return minutes;
	}

	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Date getLastModification() {
		return lastModification;
	}

	public void setLastModification(Date lastModification) {
		this.lastModification = lastModification;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public Integer getIdPointage() {
		return idPointage;
	}

	public void setIdPointage(Integer idPointage) {
		this.idPointage = idPointage;
	}

}
