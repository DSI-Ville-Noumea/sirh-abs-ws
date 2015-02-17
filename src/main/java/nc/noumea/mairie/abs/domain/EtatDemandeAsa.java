package nc.noumea.mairie.abs.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "ABS_ETAT_DEMANDE_ASA")
@PersistenceUnit(unitName = "absPersistenceUnit")
@PrimaryKeyJoinColumn(name = "ID_ETAT_DEMANDE")
public class EtatDemandeAsa extends EtatDemande {

	@NotNull
	@Column(name = "DUREE", columnDefinition = "numeric")
	private Double duree;

	@NotNull
	@Column(name = "DATE_DEBUT_AM")
	private boolean dateDebutAM;

	@NotNull
	@Column(name = "DATE_DEBUT_PM")
	private boolean dateDebutPM;

	@NotNull
	@Column(name = "DATE_FIN_AM")
	private boolean dateFinAM;

	@NotNull
	@Column(name = "DATE_FIN_PM")
	private boolean dateFinPM;

	@OneToOne(optional = true)
	@JoinColumn(name = "ID_ORGANISATION_SYNDICALE")
	private OrganisationSyndicale organisationSyndicale;
	
	// solde avant decompte de la demande
	@Column(name = "TOTAL_MINUTES_OLD", columnDefinition = "numeric")
	private Integer totalMinutesOld;
	
	// solde apres decompte de la demande
	@Column(name = "TOTAL_MINUTES_NEW", columnDefinition = "numeric")
	private Integer totalMinutesNew;
	
	// solde avant decompte de la demande
	@Column(name = "TOTAL_JOURS_OLD", columnDefinition = "numeric")
	private Double totalJoursOld;
	
	// solde apres decompte de la demande
	@Column(name = "TOTAL_JOURS_NEW", columnDefinition = "numeric")
	private Double totalJoursNew;

	public boolean isDateDebutAM() {
		return dateDebutAM;
	}

	public void setDateDebutAM(boolean dateDebutAM) {
		this.dateDebutAM = dateDebutAM;
	}

	public boolean isDateDebutPM() {
		return dateDebutPM;
	}

	public void setDateDebutPM(boolean dateDebutPM) {
		this.dateDebutPM = dateDebutPM;
	}

	public boolean isDateFinAM() {
		return dateFinAM;
	}

	public void setDateFinAM(boolean dateFinAM) {
		this.dateFinAM = dateFinAM;
	}

	public boolean isDateFinPM() {
		return dateFinPM;
	}

	public void setDateFinPM(boolean dateFinPM) {
		this.dateFinPM = dateFinPM;
	}

	public Double getDuree() {
		return duree;
	}

	public void setDuree(Double duree) {
		this.duree = duree;
	}

	public OrganisationSyndicale getOrganisationSyndicale() {
		return organisationSyndicale;
	}

	public void setOrganisationSyndicale(OrganisationSyndicale organisationSyndicale) {
		this.organisationSyndicale = organisationSyndicale;
	}

	public Integer getTotalMinutesOld() {
		return totalMinutesOld;
	}

	public void setTotalMinutesOld(Integer totalMinutesOld) {
		this.totalMinutesOld = totalMinutesOld;
	}

	public Integer getTotalMinutesNew() {
		return totalMinutesNew;
	}

	public void setTotalMinutesNew(Integer totalMinutesNew) {
		this.totalMinutesNew = totalMinutesNew;
	}

	public Double getTotalJoursOld() {
		return totalJoursOld;
	}

	public void setTotalJoursOld(Double totalJoursOld) {
		this.totalJoursOld = totalJoursOld;
	}

	public Double getTotalJoursNew() {
		return totalJoursNew;
	}

	public void setTotalJoursNew(Double totalJoursNew) {
		this.totalJoursNew = totalJoursNew;
	}

}
