package nc.noumea.mairie.abs.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "ABS_AGENT_REPOS_COMP_COUNT")
@PersistenceUnit(unitName = "absPersistenceUnit")
public class AgentReposCompCount extends BaseAgentCount {

	@Id
	@Column(name = "ID_AGENT_REPOS_COMP_COUNT")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idAgentReposCompCount;

	@NotNull
	@Column(name = "TOTAL_MINUTES_ANNEE_N1")
	private int totalMinutesAnneeN1;

	public Integer getIdAgentReposCompCount() {
		return idAgentReposCompCount;
	}

	public void setIdAgentReposCompCount(Integer idAgentReposCompCount) {
		this.idAgentReposCompCount = idAgentReposCompCount;
	}

	public int getTotalMinutesAnneeN1() {
		return totalMinutesAnneeN1;
	}

	public void setTotalMinutesAnneeN1(int totalMinutesAnneeN1) {
		this.totalMinutesAnneeN1 = totalMinutesAnneeN1;
	}

}
