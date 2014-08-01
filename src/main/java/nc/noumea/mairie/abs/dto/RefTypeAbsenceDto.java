package nc.noumea.mairie.abs.dto;

import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeSaisi;

public class RefTypeAbsenceDto {

	private Integer idRefTypeAbsence;
	private String libelle;
	private String groupe;
	
	private RefTypeSaisiDto typeSaisiDto;

	public RefTypeAbsenceDto() {
	}

	public RefTypeAbsenceDto(RefTypeAbsence type) {
		super();
		this.idRefTypeAbsence = type.getIdRefTypeAbsence();
		this.libelle = type.getLabel();
		if(null != type.getGroupe()) {
			this.groupe = type.getGroupe().getCode();
		}
	}
	
	public RefTypeAbsenceDto(RefTypeAbsence type, RefTypeSaisi typeSaisi) {
		super();
		this.idRefTypeAbsence = type.getIdRefTypeAbsence();
		this.libelle = type.getLabel();
		if(null != type.getGroupe()) {
			this.groupe = type.getGroupe().getCode();
		}
		if(null != typeSaisi) {
			this.typeSaisiDto = new RefTypeSaisiDto(typeSaisi);
		}
	}

	public String getLibelle() {
		return libelle;
	}

	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}

	public Integer getIdRefTypeAbsence() {
		return idRefTypeAbsence;
	}

	public void setIdRefTypeAbsence(Integer idRefTypeAbsence) {
		this.idRefTypeAbsence = idRefTypeAbsence;
	}

	public String getGroupe() {
		return groupe;
	}

	public void setGroupe(String groupe) {
		this.groupe = groupe;
	}

	public RefTypeSaisiDto getTypeSaisiDto() {
		return typeSaisiDto;
	}

	public void setTypeSaisiDto(RefTypeSaisiDto typeSaisiDto) {
		this.typeSaisiDto = typeSaisiDto;
	}
	
}
