package nc.noumea.mairie.abs.dto;

import java.io.Serializable;

public class SoldeMaladiesDto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5431411920902425859L;

	// droits en jours en plein salaire
	private Integer droitsPleinSalaire;
	// droits en jours en demi salaire
	private Integer droitsDemiSalaire;
	// reste a prendre en jours en plein salaire
	private Integer rapPleinSalaire;
	// reste a prendre en jours en demi salaire
	private Integer rapDemiSalaire;
	// nombre jours totaux pris
	private Integer totalPris;
	
	public Integer getDroitsPleinSalaire() {
		return droitsPleinSalaire;
	}
	public void setDroitsPleinSalaire(Integer droitsPleinSalaire) {
		this.droitsPleinSalaire = droitsPleinSalaire;
	}
	public Integer getDroitsDemiSalaire() {
		return droitsDemiSalaire;
	}
	public void setDroitsDemiSalaire(Integer droitsDemiSalaire) {
		this.droitsDemiSalaire = droitsDemiSalaire;
	}
	public Integer getTotalPris() {
		return totalPris;
	}
	public void setTotalPris(Integer totalPris) {
		this.totalPris = totalPris;
	}
	public Integer getRapPleinSalaire() {
		return rapPleinSalaire;
	}
	public void setRapPleinSalaire(Integer rapPleinSalaire) {
		this.rapPleinSalaire = rapPleinSalaire;
	}
	public Integer getRapDemiSalaire() {
		return rapDemiSalaire;
	}
	public void setRapDemiSalaire(Integer rapDemiSalaire) {
		this.rapDemiSalaire = rapDemiSalaire;
	}
	
}
