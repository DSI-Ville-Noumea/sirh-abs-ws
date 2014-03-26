package nc.noumea.mairie.abs.dto;

public class OrganisationSyndicaleDto {

	private Integer idOrganisation;
	private String libelle;
	private String sigle;
	private boolean actif;

	public OrganisationSyndicaleDto() {
	}

	public Integer getIdOrganisation() {
		return idOrganisation;
	}

	public void setIdOrganisation(Integer idOrganisation) {
		this.idOrganisation = idOrganisation;
	}

	public String getLibelle() {
		return libelle;
	}

	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}

	public String getSigle() {
		return sigle;
	}

	public void setSigle(String sigle) {
		this.sigle = sigle;
	}

	public boolean isActif() {
		return actif;
	}

	public void setActif(boolean actif) {
		this.actif = actif;
	}

}
