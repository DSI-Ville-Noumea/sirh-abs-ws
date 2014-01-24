package nc.noumea.mairie.abs.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;

@Entity
@Table(name = "ABS_AGENT_REPOS_COMP_COUNT")
@PersistenceUnit(unitName = "absPersistenceUnit")
public class AgentReposCompCount extends BaseAgentCount {
	
	@Id
	@Column(name = "ID_AGENT_REPOS_COMP_COUNT")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idAgentReposCompCount;

	public Integer getIdAgentReposCompCount() {
		return idAgentReposCompCount;
	}

	public void setIdAgentReposCompCount(Integer idAgentReposCompCount) {
		this.idAgentReposCompCount = idAgentReposCompCount;
	}
	
}
