package nc.noumea.mairie.abs.dto;

import java.util.Date;

import nc.noumea.mairie.abs.domain.AgentAsaA48Count;

public class CompteurAsaDto {

	private Integer idAgent;

	private Integer nb;

	private Date dateDebut;

	private Date dateFin;

	public CompteurAsaDto(AgentAsaA48Count arc) {
		this.idAgent = arc.getIdAgent();
		this.nb = arc.getTotalJours();
		this.dateDebut = arc.getDateDebut();
		this.dateFin = arc.getDateFin();
	}

	public Integer getIdAgent() {
		return idAgent;
	}

	public void setIdAgent(Integer idAgent) {
		this.idAgent = idAgent;
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

	public Integer getNb() {
		return nb;
	}

	public void setNb(Integer nb) {
		this.nb = nb;
	}

}
