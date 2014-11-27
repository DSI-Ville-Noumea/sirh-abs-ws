package nc.noumea.mairie.abs.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PersistenceUnit;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "ABS_AGENT_CONGE_ANNUEL_COUNT")
@PersistenceUnit(unitName = "absPersistenceUnit")
@PrimaryKeyJoinColumn(name = "ID_AGENT_COUNT")
public class AgentCongeAnnuelCount extends AgentCount {

	@NotNull
	@Column(name = "TOTAL_JOURS")
	private int totalJours;

	@NotNull
	@Column(name = "TOTAL_JOURS_ANNEE_N1")
	private int totalJoursAnneeN1;

	public int getTotalJours() {
		return totalJours;
	}

	public void setTotalJours(int totalJours) {
		this.totalJours = totalJours;
	}

	public int getTotalJoursAnneeN1() {
		return totalJoursAnneeN1;
	}

	public void setTotalJoursAnneeN1(int totalJoursAnneeN1) {
		this.totalJoursAnneeN1 = totalJoursAnneeN1;
	}

}
