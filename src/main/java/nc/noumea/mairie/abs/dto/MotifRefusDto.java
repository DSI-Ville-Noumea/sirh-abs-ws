package nc.noumea.mairie.abs.dto;

import nc.noumea.mairie.abs.domain.MotifRefus;

public class MotifRefusDto {

	private Integer idMotifRefus;
	private String libelle;
	private Integer idRefTypeAbsence;

	public MotifRefusDto() {
	}

	public MotifRefusDto(MotifRefus motif) {
		super();
		this.idMotifRefus = motif.getIdMotifRefus();
		this.libelle = motif.getLibelle();
	}

	public String getLibelle() {
		return libelle;
	}

	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}

	public Integer getIdMotifRefus() {
		return idMotifRefus;
	}

	public void setIdMotifRefus(Integer idMotifRefus) {
		this.idMotifRefus = idMotifRefus;
	}

	public Integer getIdRefTypeAbsence() {
		return idRefTypeAbsence;
	}

	public void setIdRefTypeAbsence(Integer idRefTypeAbsence) {
		this.idRefTypeAbsence = idRefTypeAbsence;
	}
}
