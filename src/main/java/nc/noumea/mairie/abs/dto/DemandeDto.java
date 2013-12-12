package nc.noumea.mairie.abs.dto;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DemandeDto {

	private Integer idDemande;
	private Integer idAgent;
	private Integer idTypeDemande;
	private Date dateDebut;
	private Integer duree;
	private Integer idRefEtat;
	
	public Integer getIdDemande() {
		return idDemande;
	}
	public void setIdDemande(Integer idDemande) {
		this.idDemande = idDemande;
	}
	public Integer getIdAgent() {
		return idAgent;
	}
	public void setIdAgent(Integer idAgent) {
		this.idAgent = idAgent;
	}
	public Integer getIdTypeDemande() {
		return idTypeDemande;
	}
	public void setIdTypeDemande(Integer idTypeDemande) {
		this.idTypeDemande = idTypeDemande;
	}
	public Date getDateDebut() {
		return dateDebut;
	}
	public void setDateDebut(Date dateDebut) {
		this.dateDebut = dateDebut;
	}
	public Integer getDuree() {
		return duree;
	}
	public void setDuree(Integer duree) {
		this.duree = duree;
	}
	public Integer getIdRefEtat() {
		return idRefEtat;
	}
	public void setIdRefEtat(Integer idRefEtat) {
		this.idRefEtat = idRefEtat;
	}
	
	public DemandeDto(){
	}
	
	public DemandeDto(Integer idDemande, Integer idAgent,
			Integer idTypeDemande, Date dateDebut, Integer idRefEtat) {
		super();
		this.idDemande = idDemande;
		this.idAgent = idAgent;
		this.idTypeDemande = idTypeDemande;
		this.dateDebut = dateDebut;
		this.idRefEtat = idRefEtat;
	}
	
	
}
