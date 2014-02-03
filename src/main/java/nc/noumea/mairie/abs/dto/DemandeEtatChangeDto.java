package nc.noumea.mairie.abs.dto;

import java.util.Date;

public class DemandeEtatChangeDto {

	private Integer idDemande;
	private Integer idRefEtat;
	private Date dateAvis;
	private Integer idMotifAvis;
	private String motifViseur;

	public Integer getIdDemande() {
		return idDemande;
	}

	public void setIdDemande(Integer idPointage) {
		this.idDemande = idPointage;
	}

	public Integer getIdRefEtat() {
		return idRefEtat;
	}

	public void setIdRefEtat(Integer idRefEtat) {
		this.idRefEtat = idRefEtat;
	}

	public Date getDateAvis() {
		return dateAvis;
	}

	public void setDateAvis(Date dateAvis) {
		this.dateAvis = dateAvis;
	}

	public Integer getIdMotifAvis() {
		return idMotifAvis;
	}

	public void setIdMotifAvis(Integer idMotifAvis) {
		this.idMotifAvis = idMotifAvis;
	}

	public String getMotifViseur() {
		return motifViseur;
	}

	public void setMotifViseur(String motifViseur) {
		this.motifViseur = motifViseur;
	}
}
