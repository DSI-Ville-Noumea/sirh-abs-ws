package nc.noumea.mairie.abs.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PersistenceUnit;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "ABS_AGENT_RECUP_COUNT_TEMP")
@PersistenceUnit(unitName = "absPersistenceUnit")
@PrimaryKeyJoinColumn(name = "ID_AGENT_COUNT")
@NamedQueries({
	@NamedQuery(name = "getAgentRecupCountTempByIdAgent", query = "select d from AgentRecupCountTemp d where d.idAgent = :idAgent")
})
public class AgentRecupCountTemp extends AgentCount {

	@NotNull
	@Column(name = "TOTAL_MINUTES")
	private int totalMinutes;

	public int getTotalMinutes() {
		return totalMinutes;
	}

	public void setTotalMinutes(int totalMinutes) {
		this.totalMinutes = totalMinutes;
	}

}
