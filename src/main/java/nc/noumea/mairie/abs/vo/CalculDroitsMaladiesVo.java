package nc.noumea.mairie.abs.vo;

import java.io.Serializable;

public class CalculDroitsMaladiesVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1450289502831337309L;
	
	// nombre de jour maladies pris sur une annee glissante
	private Integer totalPris;
	// nombre de jours coupes sur salaire
	private Integer nombreJoursCoupeDemiSalaire;
	private Integer nombreJoursCoupePleinSalaire;
	// nombre de jours restant dans les droits
	private Integer nombreJoursResteAPrendreDemiSalaire;
	private Integer nombreJoursResteAPrendrePleinSalaire;
	// droits en jours en plein salaire
	private Integer droitsPleinSalaire;
	// droits en jours en demi salaire
	private Integer droitsDemiSalaire;
	
	public Integer getTotalPris() {
		return totalPris;
	}
	public void setTotalPris(Integer totalPris) {
		this.totalPris = totalPris;
	}
	public Integer getNombreJoursCoupeDemiSalaire() {
		return nombreJoursCoupeDemiSalaire;
	}
	public void setNombreJoursCoupeDemiSalaire(Integer nombreJoursCoupeDemiSalaire) {
		this.nombreJoursCoupeDemiSalaire = nombreJoursCoupeDemiSalaire;
	}
	public Integer getNombreJoursCoupePleinSalaire() {
		return nombreJoursCoupePleinSalaire;
	}
	public void setNombreJoursCoupePleinSalaire(Integer nombreJoursCoupePleinSalaire) {
		this.nombreJoursCoupePleinSalaire = nombreJoursCoupePleinSalaire;
	}
	public Integer getNombreJoursResteAPrendreDemiSalaire() {
		return nombreJoursResteAPrendreDemiSalaire;
	}
	public void setNombreJoursResteAPrendreDemiSalaire(
			Integer nombreJoursResteAPrendreDemiSalaire) {
		this.nombreJoursResteAPrendreDemiSalaire = nombreJoursResteAPrendreDemiSalaire;
	}
	public Integer getNombreJoursResteAPrendrePleinSalaire() {
		return nombreJoursResteAPrendrePleinSalaire;
	}
	public void setNombreJoursResteAPrendrePleinSalaire(
			Integer nombreJoursResteAPrendrePleinSalaire) {
		this.nombreJoursResteAPrendrePleinSalaire = nombreJoursResteAPrendrePleinSalaire;
	}
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
	
	@Override
	public boolean equals(Object obj) {
		CalculDroitsMaladiesVo o = (CalculDroitsMaladiesVo) obj;
		return o.getDroitsDemiSalaire().equals(droitsDemiSalaire) &&
				o.getDroitsPleinSalaire().equals(droitsPleinSalaire) &&
				o.getNombreJoursCoupeDemiSalaire().equals(nombreJoursCoupeDemiSalaire) &&
				o.getNombreJoursCoupePleinSalaire().equals(nombreJoursCoupePleinSalaire) &&
				o.getNombreJoursResteAPrendrePleinSalaire().equals(nombreJoursResteAPrendrePleinSalaire) &&
				o.getNombreJoursResteAPrendreDemiSalaire().equals(nombreJoursResteAPrendreDemiSalaire) &&
				o.getTotalPris().equals(totalPris);
	}

}
