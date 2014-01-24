package nc.noumea.mairie.abs.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;

@Entity
@Table(name = "ABS_AGENT_RECUP_COUNT")
@PersistenceUnit(unitName = "absPersistenceUnit")
public class AgentRecupCount extends BaseAgentCount {

	@Id
	@Column(name = "ID_AGENT_RECUP_COUNT")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idAgentRecupCount;

	public Integer getIdAgentRecupCount() {
		return idAgentRecupCount;
	}

	public void setIdAgentRecupCount(Integer idAgentRecupCount) {
		this.idAgentRecupCount = idAgentRecupCount;
	}
	
	
	
}
