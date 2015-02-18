package nc.noumea.mairie.abs.dto;

import nc.noumea.mairie.abs.domain.CongeAnnuelRestitutionMassiveHisto;

public class RestitutionMassiveHistoDto {
	
	private Integer idAgent;
	private String status;
	private Double jours;
	
	public RestitutionMassiveHistoDto(CongeAnnuelRestitutionMassiveHisto histo) {
		this.idAgent = histo.getIdAgent();
		this.status = histo.getStatus();
		this.jours = histo.getJours();
	}

	public Integer getIdAgent() {
		return idAgent;
	}

	public void setIdAgent(Integer idAgent) {
		this.idAgent = idAgent;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Double getJours() {
		return jours;
	}

	public void setJours(Double jours) {
		this.jours = jours;
	}
	
}
