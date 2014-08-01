package nc.noumea.mairie.abs.dto;

import nc.noumea.mairie.abs.domain.RefGroupeAbsence;

public class RefGroupeAbsenceDto {

	private Integer idRefGroupeAbsence;
	private String code;
	private String libelle;
	
	public RefGroupeAbsenceDto(RefGroupeAbsence groupe) {
		this.idRefGroupeAbsence = groupe.getIdRefGroupeAbsence();
		this.code = groupe.getCode();
		this.libelle = groupe.getLibelle();
	}
	
	public Integer getIdRefGroupeAbsence() {
		return idRefGroupeAbsence;
	}
	public void setIdRefGroupeAbsence(Integer idRefGroupeAbsence) {
		this.idRefGroupeAbsence = idRefGroupeAbsence;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getLibelle() {
		return libelle;
	}
	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}
	
	
}
