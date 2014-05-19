package nc.noumea.mairie.abs.recup.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PersistenceUnit;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import nc.noumea.mairie.abs.domain.AgentCount;

@Entity
@Table(name = "ABS_AGENT_RECUP_COUNT")
@PersistenceUnit(unitName = "absPersistenceUnit")
@PrimaryKeyJoinColumn(name = "ID_AGENT_COUNT")
public class AgentRecupCount extends AgentCount {

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
