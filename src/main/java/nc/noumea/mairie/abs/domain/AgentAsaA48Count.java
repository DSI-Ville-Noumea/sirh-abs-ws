package nc.noumea.mairie.abs.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "ABS_AGENT_ASA_A48_COUNT")
@PersistenceUnit(unitName = "absPersistenceUnit")
public class AgentAsaA48Count extends BaseAgentCount {

	@Id
	@Column(name = "ID_AGENT_ASA_A48_COUNT")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idAgentAsaA48Count;

	@NotNull
	@Column(name = "TOTAL_JOURS", columnDefinition = "numeric")
	private Double totalJours;

	@Column(name = "DATE_DEBUT")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateDebut;

	@Column(name = "DATE_FIN")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateFin;

	public Double getTotalJours() {
		return totalJours;
	}

	public void setTotalJours(Double totalJours) {
		this.totalJours = totalJours;
	}

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

	public Integer getIdAgentAsaA48Count() {
		return idAgentAsaA48Count;
	}

	public void setIdAgentAsaA48Count(Integer idAgentAsaA48Count) {
		this.idAgentAsaA48Count = idAgentAsaA48Count;
	}

}
