package nc.noumea.mairie.abs.dto;

import java.io.Serializable;

public class SoldeEnfantMaladeDto implements Serializable {

	private static final long serialVersionUID = -478994736354541922L;

	public static final Integer QUOTA_ENFANT_MALADE = 3;

	private Integer totalPris;
	private Integer totalRestant;
	
	public Integer getTotalRestant() {
		return totalRestant;
	}
	public void setTotalRestant(Integer totalRestant) {
		this.totalRestant = totalRestant;
	}
	public Integer getTotalPris() {
		return totalPris;
	}
	public void setTotalPris(Integer totalPris) {
		this.totalPris = totalPris;
	}
	
	public boolean hasRights() {
		return this.totalPris < QUOTA_ENFANT_MALADE;
	}
	
}
