package nc.noumea.mairie.abs.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PersistenceUnit;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "ABS_AGENT_ASA_AMICALE_COUNT")
@PersistenceUnit(unitName = "absPersistenceUnit")
@PrimaryKeyJoinColumn(name = "ID_AGENT_COUNT")
public class AgentAsaAmicaleCount extends AgentCount {

	@NotNull
	@Column(name = "TOTAL_MINUTES", columnDefinition = "numeric")
	private int totalMinutes;

	@Column(name = "DATE_DEBUT")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateDebut;

	@Column(name = "DATE_FIN")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateFin;

	@NotNull
	@Column(name = "ACTIF", nullable = false)
	@Type(type = "boolean")
	private boolean actif;

	public Date getDateDebut() {
		return dateDebut;
	}

	public void setDateDebut(Date dateDebut) {
		this.dateDebut = dateDebut;
	}

	public Date getDateFin() {
		return dateFin;
	}

	public void setDateFin(Date dateFin) {
		this.dateFin = dateFin;
	}

	public int getTotalMinutes() {
		return totalMinutes;
	}

	public void setTotalMinutes(int totalMinutes) {
		this.totalMinutes = totalMinutes;
	}

	public boolean isActif() {
		return actif;
	}

	public void setActif(boolean actif) {
		this.actif = actif;
	}

}
