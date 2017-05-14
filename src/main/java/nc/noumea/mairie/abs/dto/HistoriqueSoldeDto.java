package nc.noumea.mairie.abs.dto;

import java.util.Date;

import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;
import nc.noumea.mairie.abs.domain.CongeAnnuelRestitutionMassiveHisto;
import nc.noumea.mairie.abs.domain.DemandeMaladies;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class HistoriqueSoldeDto {

	@JsonSerialize(using = JsonDateSerializer.class)
	@JsonDeserialize(using = JsonDateDeserializer.class)
	private Date dateModifcation;
	private MotifCompteurDto motif;
	private Integer idAgentModification;
	private String textModification;
	
	// Maladies
	private String typeAbsence;
	@JsonSerialize(using = JsonDateSerializer.class)
	@JsonDeserialize(using = JsonDateDeserializer.class)
	private Date dateDebut;
	@JsonSerialize(using = JsonDateSerializer.class)
	@JsonDeserialize(using = JsonDateDeserializer.class)
	private Date dateFin;
	private Double duree;
	private Integer totalPris;
	private Integer nombreJoursCoupeDemiSalaire;
	private Integer nombreJoursCoupePleinSalaire;
	private Integer nombreJoursResteAPrendreDemiSalaire;
	private Integer nombreJoursResteAPrendrePleinSalaire;

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
	
	public HistoriqueSoldeDto(DemandeMaladies demande){
		super();
		this.setDateModifcation(demande.getDateDebut());
		this.typeAbsence = demande.getType().getLabel();
		this.dateDebut = demande.getDateDebut();
		this.dateFin = demande.getDateFin();
		this.duree = demande.getDuree();
		this.totalPris = demande.getTotalPris();
		this.nombreJoursCoupeDemiSalaire = demande.getNombreJoursCoupeDemiSalaire();
		this.nombreJoursCoupePleinSalaire = demande.getNombreJoursCoupePleinSalaire();
		this.nombreJoursResteAPrendreDemiSalaire = demande.getNombreJoursResteAPrendreDemiSalaire();
		this.nombreJoursResteAPrendrePleinSalaire = demande.getNombreJoursResteAPrendrePleinSalaire();
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

	public String getTypeAbsence() {
		return typeAbsence;
	}

	public void setTypeAbsence(String typeAbsence) {
		this.typeAbsence = typeAbsence;
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

	public Integer getTotalPris() {
		return totalPris;
	}

	public void setTotalPris(Integer totalPris) {
		this.totalPris = totalPris;
	}

	public Integer getNombreJoursCoupeDemiSalaire() {
		return nombreJoursCoupeDemiSalaire;
	}

	public void setNombreJoursCoupeDemiSalaire(Integer nombreJoursCoupeDemiSalaire) {
		this.nombreJoursCoupeDemiSalaire = nombreJoursCoupeDemiSalaire;
	}

	public Integer getNombreJoursCoupePleinSalaire() {
		return nombreJoursCoupePleinSalaire;
	}

	public void setNombreJoursCoupePleinSalaire(Integer nombreJoursCoupePleinSalaire) {
		this.nombreJoursCoupePleinSalaire = nombreJoursCoupePleinSalaire;
	}

	public Integer getNombreJoursResteAPrendreDemiSalaire() {
		return nombreJoursResteAPrendreDemiSalaire;
	}

	public void setNombreJoursResteAPrendreDemiSalaire(
			Integer nombreJoursResteAPrendreDemiSalaire) {
		this.nombreJoursResteAPrendreDemiSalaire = nombreJoursResteAPrendreDemiSalaire;
	}

	public Integer getNombreJoursResteAPrendrePleinSalaire() {
		return nombreJoursResteAPrendrePleinSalaire;
	}

	public void setNombreJoursResteAPrendrePleinSalaire(
			Integer nombreJoursResteAPrendrePleinSalaire) {
		this.nombreJoursResteAPrendrePleinSalaire = nombreJoursResteAPrendrePleinSalaire;
	}

	public Double getDuree() {
		return duree;
	}

	public void setDuree(Double duree) {
		this.duree = duree;
	}
	
}
