package nc.noumea.mairie.abs.dto;

import java.util.Date;

public class InfosAlimAutoCongesAnnuelsDto {

	private Integer idAgent;
	
	private Integer idBaseCongeAbsence;
	// SPPOSA.DROITC
	private boolean droitConges;
	// SPPOSA.DUREE
	private Integer dureeDroitConges;
	
	private Date dateDebut;
	private Date dateFin;
	
	
	public Integer getIdBaseCongeAbsence() {
		return idBaseCongeAbsence;
	}
	public void setIdBaseCongeAbsence(Integer idBaseCongeAbsence) {
		this.idBaseCongeAbsence = idBaseCongeAbsence;
	}
	public Integer getIdAgent() {
		return idAgent;
	}
	public void setIdAgent(Integer idAgent) {
		this.idAgent = idAgent;
	}
	public boolean isDroitConges() {
		return droitConges;
	}
	public void setDroitConges(boolean droitConges) {
		this.droitConges = droitConges;
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
	public Integer getDureeDroitConges() {
		return dureeDroitConges;
	}
	public void setDureeDroitConges(Integer dureeDroitConges) {
		this.dureeDroitConges = dureeDroitConges;
	}
	
}
