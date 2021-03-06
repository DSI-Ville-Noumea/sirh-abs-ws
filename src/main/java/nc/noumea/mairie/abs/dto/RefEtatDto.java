package nc.noumea.mairie.abs.dto;

import nc.noumea.mairie.abs.domain.RefEtat;

public class RefEtatDto {

	private Integer idRefEtat;
	private String libelle;

	public RefEtatDto() {
	}

	public RefEtatDto(RefEtat etat) {
		super();
		this.idRefEtat = etat.getIdRefEtat();
		this.libelle = etat.getLabel();

	}

	public Integer getIdRefEtat() {
		return idRefEtat;
	}

	public void setIdRefEtat(Integer idRefEtat) {
		this.idRefEtat = idRefEtat;
	}

	public String getLibelle() {
		return libelle;
	}

	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}
}
