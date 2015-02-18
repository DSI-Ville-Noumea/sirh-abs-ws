package nc.noumea.mairie.abs.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "ABS_CA_RESTITUTION_MASSIVE_HISTO")
@PersistenceUnit(unitName = "absPersistenceUnit")
public class CongeAnnuelRestitutionMassiveHisto {

	@Id
	@Column(name = "ID_CA_RESTITUTION_MASSIVE_HISTO")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idCongeAnnuelRestitutionMassiveHisto;
	
	@NotNull
	@Column(name = "ID_AGENT")
	private Integer idAgent;
	
	@NotNull
	@Column(name = "STATUS")
	private String status;
	
	@Column(name = "JOURS", columnDefinition = "numeric")
	private Double jours;
	
	@ManyToOne
	@JoinColumn(name = "ID_CA_RESTITUTION_MASSIVE", referencedColumnName = "ID_CA_RESTITUTION_MASSIVE", updatable = true, insertable = true)
	private CongeAnnuelRestitutionMassive restitutionMassive;

	public Integer getIdCongeAnnuelRestitutionMassiveHisto() {
		return idCongeAnnuelRestitutionMassiveHisto;
	}

	public void setIdCongeAnnuelRestitutionMassiveHisto(
			Integer idCongeAnnuelRestitutionMassiveHisto) {
		this.idCongeAnnuelRestitutionMassiveHisto = idCongeAnnuelRestitutionMassiveHisto;
	}

	public Integer getIdAgent() {
		return idAgent;
	}

	public void setIdAgent(Integer idAgent) {
		this.idAgent = idAgent;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public CongeAnnuelRestitutionMassive getRestitutionMassive() {
		return restitutionMassive;
	}

	public void setRestitutionMassive(CongeAnnuelRestitutionMassive restitutionMassive) {
		this.restitutionMassive = restitutionMassive;
	}

	public Double getJours() {
		return jours;
	}

	public void setJours(Double jours) {
		this.jours = jours;
	}
	
}
