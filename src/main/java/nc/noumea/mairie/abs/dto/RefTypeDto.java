package nc.noumea.mairie.abs.dto;

import java.io.Serializable;

import nc.noumea.mairie.abs.domain.RefTypeAccidentTravail;
import nc.noumea.mairie.abs.domain.RefTypeMaladiePro;
import nc.noumea.mairie.abs.domain.RefTypeSiegeLesion;

public class RefTypeDto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3854094040343020224L;
	
	private Integer idRefType;
	private String code;
	private String libelle;
	
	public RefTypeDto() {
	}
	
	public RefTypeDto(RefTypeAccidentTravail type) {
		this.idRefType = type.getIdRefAccidentTravail();
		this.libelle = type.getLibelle();
	}
	
	public RefTypeDto(RefTypeSiegeLesion type) {
		this.idRefType = type.getIdRefSiegeLesion();
		this.libelle = type.getLibelle();
	}
	
	public RefTypeDto(RefTypeMaladiePro type) {
		this.idRefType = type.getIdRefMaladiePro();
		this.code = type.getCode();
		this.libelle = type.getLibelle();
	}
	
	public Integer getIdRefType() {
		return idRefType;
	}
	public void setIdRefType(Integer idRefType) {
		this.idRefType = idRefType;
	}
	public String getLibelle() {
		return libelle;
	}
	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
}
