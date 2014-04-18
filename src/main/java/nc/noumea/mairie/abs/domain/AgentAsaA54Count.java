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
@Table(name = "ABS_AGENT_ASA_A54_COUNT")
@PersistenceUnit(unitName = "absPersistenceUnit")
public class AgentAsaA54Count extends BaseAgentCount {

	@Id
	@Column(name = "ID_AGENT_ASA_A54_COUNT")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idAgentAsaA54Count;

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

	public Integer getIdAgentAsaA54Count() {
		return idAgentAsaA54Count;
	}

	public void setIdAgentAsaA54Count(Integer idAgentAsaA54Count) {
		this.idAgentAsaA54Count = idAgentAsaA54Count;
	}

}
