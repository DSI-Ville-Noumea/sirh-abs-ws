package nc.noumea.mairie.abs.dto;

import nc.noumea.mairie.abs.domain.RefUnitePeriodeQuota;


public class UnitePeriodeQuotaDto {

	private Integer idRefUnitePeriodeQuota;
	
	private Integer valeur;
	
	private String unite;
	
	/**
	 * defini si la periode est par exemple sur :
	 * - 12 mois glissant
	 * - ou sur une année civile
	 */
	private boolean glissant;
	
	public UnitePeriodeQuotaDto(RefUnitePeriodeQuota UPQ) {
		this.idRefUnitePeriodeQuota = UPQ.getIdRefUnitePeriodeQuota();
		this.valeur = UPQ.getValeur();
		this.unite = UPQ.getUnite();
		this.glissant = UPQ.isGlissant();
	}
	

	public UnitePeriodeQuotaDto() {
	}


	public Integer getIdRefUnitePeriodeQuota() {
		return idRefUnitePeriodeQuota;
	}

	public void setIdRefUnitePeriodeQuota(Integer idRefUnitePeriodeQuota) {
		this.idRefUnitePeriodeQuota = idRefUnitePeriodeQuota;
	}

	public Integer getValeur() {
		return valeur;
	}

	public void setValeur(Integer valeur) {
		this.valeur = valeur;
	}

	public String getUnite() {
		return unite;
	}

	public void setUnite(String unite) {
		this.unite = unite;
	}

	public boolean isGlissant() {
		return glissant;
	}

	public void setGlissant(boolean glissant) {
		this.glissant = glissant;
	}
	
	
}
