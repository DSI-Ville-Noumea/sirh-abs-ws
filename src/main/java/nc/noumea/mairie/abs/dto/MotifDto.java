package nc.noumea.mairie.abs.dto;

import nc.noumea.mairie.abs.domain.Motif;

public class MotifDto {

	private Integer idMotif;
	private String libelle;

	public MotifDto() {
	}

	public MotifDto(Motif motif) {
		super();
		this.idMotif = motif.getIdMotif();
		this.libelle = motif.getLibelle();
	}

	public Integer getIdMotif() {
		return idMotif;
	}

	public void setIdMotif(Integer idMotif) {
		this.idMotif = idMotif;
	}

	public String getLibelle() {
		return libelle;
	}

	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}
}
