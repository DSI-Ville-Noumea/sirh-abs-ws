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
@Table(name = "ABS_AGENT_WEEK_CONGE_ANNUEL")
@PersistenceUnit(unitName = "absPersistenceUnit")
@NamedQueries({
	@NamedQuery(name = "findAgentWeekCongeAnnuelByIdAgentAndDateMonth", query = "select awr from AgentWeekCongeAnnuel awr where awr.idAgent = :idAgent and awr.dateMonth = :dateMonth")
})
public class AgentWeekCongeAnnuel {
	
	@Id
	@Column(name = "ID_AGENT_WEEK_CONGE_ANNUEL")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idAgentWeekCongeAnnuel;
	
	@NotNull
	@Column(name = "ID_AGENT")
	private Integer idAgent;

	@NotNull
	@Column(name = "JOURS", columnDefinition = "numeric")
	private Double jours;

	@Column(name = "DATE_MONTH")
	@Temporal(TemporalType.DATE)
	private Date dateMonth;

	@Column(name = "DATE_MODIFICATION")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastModification;
	
	@Version
    @Column(name = "version")
	private Integer version;

	public Integer getIdAgentWeekCongeAnnuel() {
		return idAgentWeekCongeAnnuel;
	}

	public void setIdAgentWeekCongeAnnuel(Integer idAgentWeekCongeAnnuel) {
		this.idAgentWeekCongeAnnuel = idAgentWeekCongeAnnuel;
	}

	public Integer getIdAgent() {
		return idAgent;
	}

	public void setIdAgent(Integer idAgent) {
		this.idAgent = idAgent;
	}

	public Double getJours() {
		return jours;
	}

	public void setJours(Double jours) {
		this.jours = jours;
	}

	public Date getDateMonth() {
		return dateMonth;
	}

	public void setDateMonth(Date dateMonth) {
		this.dateMonth = dateMonth;
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
	
	
}
