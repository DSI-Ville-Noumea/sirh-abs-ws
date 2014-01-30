package nc.noumea.mairie.abs.dto;

import nc.noumea.mairie.abs.domain.MotifCompteur;

public class MotifCompteurDto {

	private Integer idMotifCompteur;
	private String libelle;
	private Integer idRefTypeAbsence;
	private boolean motifTechnique;

	public MotifCompteurDto() {
	}

	public MotifCompteurDto(MotifCompteur motif) {
		super();
		this.idMotifCompteur = motif.getIdMotifCompteur();
		this.libelle = motif.getLibelle();
		this.idRefTypeAbsence = motif.getRefTypeAbsence().getIdRefTypeAbsence();
		this.motifTechnique = motif.isMotifTechnique();
	}

	public String getLibelle() {
		return libelle;
	}

	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}

	public Integer getIdMotifCompteur() {
		return idMotifCompteur;
	}

	public void setIdMotifCompteur(Integer idMotifCompteur) {
		this.idMotifCompteur = idMotifCompteur;
	}

	public Integer getIdRefTypeAbsence() {
		return idRefTypeAbsence;
	}

	public void setIdRefTypeAbsence(Integer idRefTypeAbsence) {
		this.idRefTypeAbsence = idRefTypeAbsence;
	}

	public boolean isMotifTechnique() {
		return motifTechnique;
	}

	public void setMotifTechnique(boolean motifTechnique) {
		this.motifTechnique = motifTechnique;
	}
}
