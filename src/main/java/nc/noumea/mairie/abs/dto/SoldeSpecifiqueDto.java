package nc.noumea.mairie.abs.dto;

public class SoldeSpecifiqueDto {
	
	private String libelle;
	private Double solde;
	private String uniteDecompte;
	
	public String getLibelle() {
		return libelle;
	}
	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}
	public Double getSolde() {
		return solde;
	}
	public void setSolde(Double solde) {
		this.solde = solde;
	}
	public String getUniteDecompte() {
		return uniteDecompte;
	}
	public void setUniteDecompte(String uniteDecompte) {
		this.uniteDecompte = uniteDecompte;
	}
	
}
