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
@Table(name = "ABS_AGENT_ASA_A55_COUNT")
@PersistenceUnit(unitName = "absPersistenceUnit")
public class AgentAsaA55Count extends BaseAgentCount {

	@Id
	@Column(name = "ID_AGENT_ASA_A55_COUNT")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idAgentAsaA55Count;

	@NotNull
	@Column(name = "TOTAL_HEURE", columnDefinition = "numeric")
	private Double totalHeures;

	@Column(name = "DATE_DEBUT")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateDebut;

	@Column(name = "DATE_FIN")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateFin;

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

	public Integer getIdAgentAsaA55Count() {
		return idAgentAsaA55Count;
	}

	public void setIdAgentAsaA55Count(Integer idAgentAsaA55Count) {
		this.idAgentAsaA55Count = idAgentAsaA55Count;
	}

	public Double getTotalHeures() {
		return totalHeures;
	}

	public void setTotalHeures(Double totalHeures) {
		this.totalHeures = totalHeures;
	}

}
