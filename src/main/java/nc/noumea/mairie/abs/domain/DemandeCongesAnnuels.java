package nc.noumea.mairie.abs.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "ABS_DEMANDE_CONGES_ANNUELS")
@PersistenceUnit(unitName = "absPersistenceUnit")
@PrimaryKeyJoinColumn(name = "ID_DEMANDE")
public class DemandeCongesAnnuels extends Demande {

	@Column(name = "DUREE", columnDefinition = "numeric")
	private Double duree;

	@Column(name = "DUREE_ANNEE_N1", columnDefinition = "numeric")
	private Double dureeAnneeN1;

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

	@NotNull
	@Column(name = "NB_SAMEDI_DECOMPTE", columnDefinition = "numeric")
	private Double nbSamediDecompte;

	@NotNull
	@Column(name = "NB_SAMEDI_OFFERT", columnDefinition = "numeric")
	private Double nbSamediOffert;

	@OneToOne(fetch = FetchType.EAGER, optional = true, orphanRemoval = false)
	@JoinColumn(name = "ID_REF_TYPE_SAISI_CONGE_ANNUEL")
	private RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel;
	
	// solde annee en cours avant decompte de la demande
	@Column(name = "TOTAL_JOURS_OLD", columnDefinition = "numeric")
	private Double totalJoursOld;
	
	// solde annee en cours apres decompte de la demande
	@Column(name = "TOTAL_JOURS_NEW", columnDefinition = "numeric")
	private Double totalJoursNew;
	
	// solde annee N-1 avant decompte de la demande
	@Column(name = "TOTAL_JOURS_ANNEE_N1_OLD", columnDefinition = "numeric")
	private Double totalJoursAnneeN1Old;
	
	// solde annee N-1 apres decompte de la demande
	@Column(name = "TOTAL_JOURS_ANNEE_N1_NEW", columnDefinition = "numeric")
	private Double totalJoursAnneeN1New;

	public Double getDuree() {
		return duree;
	}

	public void setDuree(Double duree) {
		this.duree = duree;
	}

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

	public RefTypeSaisiCongeAnnuel getTypeSaisiCongeAnnuel() {
		return typeSaisiCongeAnnuel;
	}

	public void setTypeSaisiCongeAnnuel(RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel) {
		this.typeSaisiCongeAnnuel = typeSaisiCongeAnnuel;
	}

	public Double getDureeAnneeN1() {
		return dureeAnneeN1;
	}

	public void setDureeAnneeN1(Double dureeAnneeN1) {
		this.dureeAnneeN1 = dureeAnneeN1;
	}

	public Double getNbSamediDecompte() {
		return nbSamediDecompte;
	}

	public void setNbSamediDecompte(Double nbSamediDecompte) {
		this.nbSamediDecompte = nbSamediDecompte;
	}

	public Double getNbSamediOffert() {
		return nbSamediOffert;
	}

	public void setNbSamediOffert(Double nbSamediOffert) {
		this.nbSamediOffert = nbSamediOffert;
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

	public Double getTotalJoursAnneeN1Old() {
		return totalJoursAnneeN1Old;
	}

	public void setTotalJoursAnneeN1Old(Double totalJoursAnneeN1Old) {
		this.totalJoursAnneeN1Old = totalJoursAnneeN1Old;
	}

	public Double getTotalJoursAnneeN1New() {
		return totalJoursAnneeN1New;
	}

	public void setTotalJoursAnneeN1New(Double totalJoursAnneeN1New) {
		this.totalJoursAnneeN1New = totalJoursAnneeN1New;
	}

}
