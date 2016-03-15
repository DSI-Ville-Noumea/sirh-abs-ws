package nc.noumea.mairie.abs.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PersistenceUnit;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.envers.AuditOverride;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "ABS_AGENT_CONGE_ANNUEL_COUNT")
@Audited
@AuditOverride(forClass = AgentCount.class)
@PersistenceUnit(unitName = "absPersistenceUnit")
@PrimaryKeyJoinColumn(name = "ID_AGENT_COUNT")
public class AgentCongeAnnuelCount extends AgentCount {

	@NotNull
	@Column(name = "TOTAL_JOURS", columnDefinition = "numeric")
	private Double totalJours;

	@NotNull
	@Column(name = "TOTAL_JOURS_ANNEE_N1", columnDefinition = "numeric")
	private Double totalJoursAnneeN1;

	public Double getTotalJours() {
		return totalJours;
	}

	public void setTotalJours(Double totalJours) {
		this.totalJours = totalJours;
	}

	public Double getTotalJoursAnneeN1() {
		return totalJoursAnneeN1;
	}

	public void setTotalJoursAnneeN1(Double totalJoursAnneeN1) {
		this.totalJoursAnneeN1 = totalJoursAnneeN1;
	}

}
