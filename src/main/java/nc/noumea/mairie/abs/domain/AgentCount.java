package nc.noumea.mairie.abs.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "ABS_AGENT_COUNT")
@PersistenceUnit(unitName = "absPersistenceUnit")
@Inheritance(strategy = InheritanceType.JOINED)
public class AgentCount {

	@Id
	@Column(name = "ID_AGENT_COUNT")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idAgentCount;

	@Column(name = "ID_AGENT")
	private Integer idAgent;

	@Column(name = "DATE_MODIFICATION")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastModification;

	public Integer getIdAgentCount() {
		return idAgentCount;
	}

	public void setIdAgentCount(Integer idAgentCount) {
		this.idAgentCount = idAgentCount;
	}

	public Integer getIdAgent() {
		return idAgent;
	}

	public void setIdAgent(Integer idAgent) {
		this.idAgent = idAgent;
	}

	public Date getLastModification() {
		return lastModification;
	}

	public void setLastModification(Date lastModification) {
		this.lastModification = lastModification;
	}

}
