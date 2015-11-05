package nc.noumea.mairie.abs.dto;

import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;
import nc.noumea.mairie.abs.domain.CongeAnnuelRestitutionMassiveHisto;

public class HistoriqueSoldeDto {

	@JsonSerialize(using = JsonDateSerializer.class)
	@JsonDeserialize(using = JsonDateDeserializer.class)
	private Date dateModifcation;
	private MotifCompteurDto motif;
	private Integer idAgentModification;
	private String textModification;

	public HistoriqueSoldeDto(AgentHistoAlimManuelle histo) {
		super();
		this.dateModifcation = histo.getDateModification();
		if (histo.getMotifCompteur() != null)
			this.motif = new MotifCompteurDto(histo.getMotifCompteur());
		this.idAgentModification = histo.getIdAgent();
		this.textModification = histo.getText();
	}
	
	public HistoriqueSoldeDto(CongeAnnuelRestitutionMassiveHisto histo){
		super();
		this.dateModifcation = histo.getRestitutionMassive().getDateRestitution();
		if (null != histo.getRestitutionMassive().getMotif())
			this.motif = new MotifCompteurDto(histo.getRestitutionMassive().getMotif());
		this.idAgentModification = histo.getIdAgent();
		this.textModification = "Restitution massive de congés annuels";
	}

	public Date getDateModifcation() {
		return dateModifcation;
	}

	public void setDateModifcation(Date dateModifcation) {
		this.dateModifcation = dateModifcation;
	}

	public MotifCompteurDto getMotif() {
		return motif;
	}

	public void setMotif(MotifCompteurDto motif) {
		this.motif = motif;
	}

	public Integer getIdAgentModification() {
		return idAgentModification;
	}

	public void setIdAgentModification(Integer idAgentModification) {
		this.idAgentModification = idAgentModification;
	}

	public String getTextModification() {
		return textModification;
	}

	public void setTextModification(String textModification) {
		this.textModification = textModification;
	}

}
