package nc.noumea.mairie.abs.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "ABS_AGENT_ASA_A52_COUNT")
@PersistenceUnit(unitName = "absPersistenceUnit")
@PrimaryKeyJoinColumn(name = "ID_AGENT_COUNT")
public class AgentAsaA52Count extends AgentCount {

	@NotNull
	@Column(name = "TOTAL_MINUTES", columnDefinition = "numeric")
	private Double totalMinutes;

	@Column(name = "DATE_DEBUT")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateDebut;

	@Column(name = "DATE_FIN")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateFin;
	
	@OneToOne(optional = true)
	@JoinColumn(name = "ID_ORGANISATION_SYNDICALE")
	private OrganisationSyndicale organisationSyndicale;

	public Double getTotalMinutes() {
		return totalMinutes;
	}

	public void setTotalMinutes(Double totalMinutes) {
		this.totalMinutes = totalMinutes;
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

	public OrganisationSyndicale getOrganisationSyndicale() {
		return organisationSyndicale;
	}

	public void setOrganisationSyndicale(OrganisationSyndicale organisationSyndicale) {
		this.organisationSyndicale = organisationSyndicale;
	}

}
