package nc.noumea.mairie.abs.vo;

import java.io.Serializable;

public class CheckCompteurAgentVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7188700163106197792L;

	private Integer idAgent;
	
	private Double dureeDemandeEnCoursCongesAnnuels;
	private Double compteurCongesAnnuels;
	
	private Integer dureeDemandeEnCoursRecup;
	private Integer compteurRecup;
	
	private Integer dureeDemandeEnCoursReposComp;
	private Integer compteurReposComp;
	
	private Double dureeDemandeEnCoursA48;
	private Double compteurA48;
	
	private Double dureeDemandeEnCoursA52;
	private Double compteurA52;
	
	private Double dureeDemandeEnCoursA53;
	private Double compteurA53;
	
	private Double dureeDemandeEnCoursA54;
	private Double compteurA54;
	
	private Double dureeDemandeEnCoursA55;
	private Double compteurA55;
	
	public Integer getIdAgent() {
		return idAgent;
	}
	public void setIdAgent(Integer idAgent) {
		this.idAgent = idAgent;
	}
	public Double getDureeDemandeEnCoursCongesAnnuels() {
		return dureeDemandeEnCoursCongesAnnuels;
	}
	public void setDureeDemandeEnCoursCongesAnnuels(
			Double dureeDemandeEnCoursCongesAnnuels) {
		this.dureeDemandeEnCoursCongesAnnuels = dureeDemandeEnCoursCongesAnnuels;
	}
	public Double getCompteurCongesAnnuels() {
		return compteurCongesAnnuels;
	}
	public void setCompteurCongesAnnuels(Double compteurCongesAnnuels) {
		this.compteurCongesAnnuels = compteurCongesAnnuels;
	}
	public Integer getDureeDemandeEnCoursRecup() {
		return dureeDemandeEnCoursRecup;
	}
	public void setDureeDemandeEnCoursRecup(Integer dureeDemandeEnCoursRecup) {
		this.dureeDemandeEnCoursRecup = dureeDemandeEnCoursRecup;
	}
	public Integer getCompteurRecup() {
		return compteurRecup;
	}
	public void setCompteurRecup(Integer compteurRecup) {
		this.compteurRecup = compteurRecup;
	}
	public Integer getDureeDemandeEnCoursReposComp() {
		return dureeDemandeEnCoursReposComp;
	}
	public void setDureeDemandeEnCoursReposComp(Integer dureeDemandeEnCoursReposComp) {
		this.dureeDemandeEnCoursReposComp = dureeDemandeEnCoursReposComp;
	}
	public Integer getCompteurReposComp() {
		return compteurReposComp;
	}
	public void setCompteurReposComp(Integer compteurReposComp) {
		this.compteurReposComp = compteurReposComp;
	}
	public Double getDureeDemandeEnCoursA48() {
		return dureeDemandeEnCoursA48;
	}
	public void setDureeDemandeEnCoursA48(Double dureeDemandeEnCoursA48) {
		this.dureeDemandeEnCoursA48 = dureeDemandeEnCoursA48;
	}
	public Double getCompteurA48() {
		return compteurA48;
	}
	public void setCompteurA48(Double compteurA48) {
		this.compteurA48 = compteurA48;
	}
	public Double getDureeDemandeEnCoursA52() {
		return dureeDemandeEnCoursA52;
	}
	public void setDureeDemandeEnCoursA52(Double dureeDemandeEnCoursA52) {
		this.dureeDemandeEnCoursA52 = dureeDemandeEnCoursA52;
	}
	public Double getCompteurA52() {
		return compteurA52;
	}
	public void setCompteurA52(Double compteurA52) {
		this.compteurA52 = compteurA52;
	}
	public Double getDureeDemandeEnCoursA53() {
		return dureeDemandeEnCoursA53;
	}
	public void setDureeDemandeEnCoursA53(Double dureeDemandeEnCoursA53) {
		this.dureeDemandeEnCoursA53 = dureeDemandeEnCoursA53;
	}
	public Double getCompteurA53() {
		return compteurA53;
	}
	public void setCompteurA53(Double compteurA53) {
		this.compteurA53 = compteurA53;
	}
	public Double getDureeDemandeEnCoursA54() {
		return dureeDemandeEnCoursA54;
	}
	public void setDureeDemandeEnCoursA54(Double dureeDemandeEnCoursA54) {
		this.dureeDemandeEnCoursA54 = dureeDemandeEnCoursA54;
	}
	public Double getCompteurA54() {
		return compteurA54;
	}
	public void setCompteurA54(Double compteurA54) {
		this.compteurA54 = compteurA54;
	}
	public Double getDureeDemandeEnCoursA55() {
		return dureeDemandeEnCoursA55;
	}
	public void setDureeDemandeEnCoursA55(Double dureeDemandeEnCoursA55) {
		this.dureeDemandeEnCoursA55 = dureeDemandeEnCoursA55;
	}
	public Double getCompteurA55() {
		return compteurA55;
	}
	public void setCompteurA55(Double compteurA55) {
		this.compteurA55 = compteurA55;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((compteurA48 == null) ? 0 : compteurA48.hashCode());
		result = prime * result
				+ ((compteurA52 == null) ? 0 : compteurA52.hashCode());
		result = prime * result
				+ ((compteurA53 == null) ? 0 : compteurA53.hashCode());
		result = prime * result
				+ ((compteurA54 == null) ? 0 : compteurA54.hashCode());
		result = prime * result
				+ ((compteurA55 == null) ? 0 : compteurA55.hashCode());
		result = prime
				* result
				+ ((compteurCongesAnnuels == null) ? 0 : compteurCongesAnnuels
						.hashCode());
		result = prime * result
				+ ((compteurRecup == null) ? 0 : compteurRecup.hashCode());
		result = prime
				* result
				+ ((compteurReposComp == null) ? 0 : compteurReposComp
						.hashCode());
		result = prime
				* result
				+ ((dureeDemandeEnCoursA48 == null) ? 0
						: dureeDemandeEnCoursA48.hashCode());
		result = prime
				* result
				+ ((dureeDemandeEnCoursA52 == null) ? 0
						: dureeDemandeEnCoursA52.hashCode());
		result = prime
				* result
				+ ((dureeDemandeEnCoursA53 == null) ? 0
						: dureeDemandeEnCoursA53.hashCode());
		result = prime
				* result
				+ ((dureeDemandeEnCoursA54 == null) ? 0
						: dureeDemandeEnCoursA54.hashCode());
		result = prime
				* result
				+ ((dureeDemandeEnCoursA55 == null) ? 0
						: dureeDemandeEnCoursA55.hashCode());
		result = prime
				* result
				+ ((dureeDemandeEnCoursCongesAnnuels == null) ? 0
						: dureeDemandeEnCoursCongesAnnuels.hashCode());
		result = prime
				* result
				+ ((dureeDemandeEnCoursRecup == null) ? 0
						: dureeDemandeEnCoursRecup.hashCode());
		result = prime
				* result
				+ ((dureeDemandeEnCoursReposComp == null) ? 0
						: dureeDemandeEnCoursReposComp.hashCode());
		result = prime * result + ((idAgent == null) ? 0 : idAgent.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CheckCompteurAgentVo other = (CheckCompteurAgentVo) obj;
		if (idAgent == null) {
			if (other.idAgent != null)
				return false;
		} else if (!idAgent.equals(other.idAgent))
			return false;
		return true;
	}

}
