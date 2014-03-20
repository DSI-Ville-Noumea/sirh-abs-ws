package nc.noumea.mairie.abs.dto;

import java.util.Date;

public class CompteurDto {

	private Integer idAgent;

	private Integer dureeAAjouter;

	private Integer dureeARetrancher;

	private Integer idMotifCompteur;

	private boolean isAnneePrecedente;

	private Date dateDebut;

	private Date dateFin;

	public Integer getIdAgent() {
		return idAgent;
	}

	public void setIdAgent(Integer idAgent) {
		this.idAgent = idAgent;
	}

	public Integer getDureeAAjouter() {
		return dureeAAjouter;
	}

	public void setDureeAAjouter(Integer dureeAAjouter) {
		this.dureeAAjouter = dureeAAjouter;
	}

	public Integer getDureeARetrancher() {
		return dureeARetrancher;
	}

	public void setDureeARetrancher(Integer dureeARetrancher) {
		this.dureeARetrancher = dureeARetrancher;
	}

	public Integer getIdMotifCompteur() {
		return idMotifCompteur;
	}

	public void setIdMotifCompteur(Integer idMotifCompteur) {
		this.idMotifCompteur = idMotifCompteur;
	}

	public boolean isAnneePrecedente() {
		return isAnneePrecedente;
	}

	public void setAnneePrecedente(boolean isAnneePrecedente) {
		this.isAnneePrecedente = isAnneePrecedente;
	}

	public Date getDateDebut() {
		return dateDebut;
	}

	public void setDateDebut(Date dateDebut) {
		this.dateDebut = dateDebut;
	}

	public Date getDateFin() {
		return dateFin;
	}

	public void setDateFin(Date dateFin) {
		this.dateFin = dateFin;
	}

}
