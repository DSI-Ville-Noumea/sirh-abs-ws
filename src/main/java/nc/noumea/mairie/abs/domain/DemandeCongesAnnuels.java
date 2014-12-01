package nc.noumea.mairie.abs.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PersistenceUnit;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "ABS_DEMANDE_CONGES_ANNUELS")
@PersistenceUnit(unitName = "absPersistenceUnit")
@PrimaryKeyJoinColumn(name = "ID_DEMANDE")
public class DemandeCongesAnnuels extends Demande {

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

	@NotNull
	@Column(name = "SAMEDI_DECOMPTE")
	private boolean samediDecompte;

	@NotNull
	@Column(name = "SAMEDI_OFFERT")
	private boolean samediOffert;

	@Column(name = "COMMENTAIRE")
	private String commentaire;

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

	public String getCommentaire() {
		return commentaire;
	}

	public void setCommentaire(String commentaire) {
		this.commentaire = commentaire;
	}

	public boolean isSamediDecompte() {
		return samediDecompte;
	}

	public void setSamediDecompte(boolean samediDecompte) {
		this.samediDecompte = samediDecompte;
	}

	public boolean isSamediOffert() {
		return samediOffert;
	}

	public void setSamediOffert(boolean samediOffert) {
		this.samediOffert = samediOffert;
	}

}
