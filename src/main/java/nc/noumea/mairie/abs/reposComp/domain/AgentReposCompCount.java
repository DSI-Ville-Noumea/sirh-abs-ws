package nc.noumea.mairie.abs.reposComp.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PersistenceUnit;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import nc.noumea.mairie.abs.domain.AgentCount;

@Entity
@Table(name = "ABS_AGENT_REPOS_COMP_COUNT")
@PersistenceUnit(unitName = "absPersistenceUnit")
@PrimaryKeyJoinColumn(name = "ID_AGENT_COUNT")
public class AgentReposCompCount extends AgentCount {

	@NotNull
	@Column(name = "TOTAL_MINUTES")
	private int totalMinutes;

	@NotNull
	@Column(name = "TOTAL_MINUTES_ANNEE_N1")
	private int totalMinutesAnneeN1;

	public int getTotalMinutesAnneeN1() {
		return totalMinutesAnneeN1;
	}

	public void setTotalMinutesAnneeN1(int totalMinutesAnneeN1) {
		this.totalMinutesAnneeN1 = totalMinutesAnneeN1;
	}

	public int getTotalMinutes() {
		return totalMinutes;
	}

	public void setTotalMinutes(int totalMinutes) {
		this.totalMinutes = totalMinutes;
	}

}
