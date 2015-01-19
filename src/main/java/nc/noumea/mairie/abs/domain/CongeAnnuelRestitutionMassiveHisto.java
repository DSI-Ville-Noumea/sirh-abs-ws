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
@Table(name = "ABS_CA_RESTITUTION_MASSIVE_HISTO")
@PersistenceUnit(unitName = "absPersistenceUnit")
public class CongeAnnuelRestitutionMassiveHisto {

	@Id
	@Column(name = "ID_CA_RESTITUTION_MASSIVE_HISTO")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idCongeAnnuelRestitutionMassiveTask;
	
	@NotNull
	@Column(name = "ID_AGENT")
	private Integer idAgent;
	
	@NotNull
	@Column(name = "DATE_RESTITUTION")
	@Temporal(TemporalType.DATE)
	private Date dateRestitution;
	
	@NotNull
	@Column(name = "DATE_MODIFICATION")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateModification;
	
	@NotNull
	@Column(name = "STATUS")
	private String status;
	
	@NotNull
	@Column(name = "JOURS", columnDefinition = "numeric")
	private Double jours;

	public Integer getIdCongeAnnuelRestitutionMassiveTask() {
		return idCongeAnnuelRestitutionMassiveTask;
	}

	public void setIdCongeAnnuelRestitutionMassiveTask(
			Integer idCongeAnnuelRestitutionMassiveTask) {
		this.idCongeAnnuelRestitutionMassiveTask = idCongeAnnuelRestitutionMassiveTask;
	}

	public Integer getIdAgent() {
		return idAgent;
	}

	public void setIdAgent(Integer idAgent) {
		this.idAgent = idAgent;
	}
	
	public Date getDateRestitution() {
		return dateRestitution;
	}

	public void setDateRestitution(Date dateRestitution) {
		this.dateRestitution = dateRestitution;
	}

	public Date getDateModification() {
		return dateModification;
	}

	public void setDateModification(Date dateModification) {
		this.dateModification = dateModification;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Double getJours() {
		return jours;
	}

	public void setJours(Double jours) {
		this.jours = jours;
	}
	
}
