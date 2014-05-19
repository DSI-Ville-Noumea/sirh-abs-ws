package nc.noumea.mairie.abs.asa.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.OrganisationSyndicale;

@Entity
@Table(name = "ABS_DEMANDE_ASA")
@PersistenceUnit(unitName = "absPersistenceUnit")
@PrimaryKeyJoinColumn(name = "ID_DEMANDE")
public class DemandeAsa extends Demande {

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

}
