package nc.noumea.mairie.abs.dto;

import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import nc.noumea.mairie.abs.domain.AgentA48OrganisationSyndicale;
import nc.noumea.mairie.abs.domain.AgentA54OrganisationSyndicale;
import nc.noumea.mairie.abs.domain.AgentAsaA48Count;
import nc.noumea.mairie.abs.domain.AgentAsaA52Count;
import nc.noumea.mairie.abs.domain.AgentAsaA53Count;
import nc.noumea.mairie.abs.domain.AgentAsaA54Count;
import nc.noumea.mairie.abs.domain.AgentAsaA55Count;
import nc.noumea.mairie.abs.domain.AgentAsaAmicaleCount;
import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;

public class CompteurDto {

	private Integer						idCompteur;

	private Integer						idAgent;

	private Double						dureeAAjouter;

	private Double						dureeARetrancher;

	private MotifCompteurDto			motifCompteurDto;

	private boolean						isAnneePrecedente;

	@JsonSerialize(using = JsonDateSerializer.class)
	@JsonDeserialize(using = JsonDateDeserializer.class)
	private Date						dateDebut;

	@JsonSerialize(using = JsonDateSerializer.class)
	@JsonDeserialize(using = JsonDateDeserializer.class)
	private Date						dateFin;

	private OrganisationSyndicaleDto	organisationSyndicaleDto;

	// #18726
	private boolean						actif;

	public Integer getIdAgent() {
		return idAgent;
	}

	public void setIdAgent(Integer idAgent) {
		this.idAgent = idAgent;
	}

	public Double getDureeAAjouter() {
		return dureeAAjouter;
	}

	public void setDureeAAjouter(Double dureeAAjouter) {
		this.dureeAAjouter = dureeAAjouter;
	}

	public Double getDureeARetrancher() {
		return dureeARetrancher;
	}

	public void setDureeARetrancher(Double dureeARetrancher) {
		this.dureeARetrancher = dureeARetrancher;
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

	public CompteurDto(AgentAsaA48Count arc, AgentHistoAlimManuelle histo, AgentA48OrganisationSyndicale agentOrga) {
		this.idCompteur = arc.getIdAgentCount();
		this.idAgent = arc.getIdAgent();
		this.dureeAAjouter = arc.getTotalJours();
		this.dateDebut = arc.getDateDebut();
		this.dateFin = arc.getDateFin();
		if (null != agentOrga)
			this.organisationSyndicaleDto = new OrganisationSyndicaleDto(agentOrga.getOrganisationSyndicale());
		if (histo != null && histo.getMotifCompteur() != null) {
			MotifCompteurDto dto = new MotifCompteurDto(histo.getMotifCompteur());
			this.motifCompteurDto = dto;
		}
		this.actif = arc.isActif();
	}

	public CompteurDto(AgentAsaA54Count arc, AgentHistoAlimManuelle histo, AgentA54OrganisationSyndicale agentOrga) {
		this.idCompteur = arc.getIdAgentCount();
		this.idAgent = arc.getIdAgent();
		this.dureeAAjouter = arc.getTotalJours();
		this.dateDebut = arc.getDateDebut();
		this.dateFin = arc.getDateFin();
		if (null != agentOrga)
			this.organisationSyndicaleDto = new OrganisationSyndicaleDto(agentOrga.getOrganisationSyndicale());
		if (histo != null && histo.getMotifCompteur() != null) {
			MotifCompteurDto dto = new MotifCompteurDto(histo.getMotifCompteur());
			this.motifCompteurDto = dto;
		}
		this.actif = arc.isActif();
	}

	public CompteurDto(AgentAsaA53Count arc, AgentHistoAlimManuelle histo) {
		this.idCompteur = arc.getIdAgentCount();
		this.idAgent = arc.getIdAgent();
		this.dureeAAjouter = arc.getTotalJours();
		this.dateDebut = arc.getDateDebut();
		this.dateFin = arc.getDateFin();
		if (null != arc)
			this.organisationSyndicaleDto = new OrganisationSyndicaleDto(arc.getOrganisationSyndicale());
		if (histo != null) {
			MotifCompteurDto dto = new MotifCompteurDto(histo.getMotifCompteur());
			this.motifCompteurDto = dto;
		}
	}

	public CompteurDto(AgentAsaA52Count arc, AgentHistoAlimManuelle histo) {
		this.idCompteur = arc.getIdAgentCount();
		this.idAgent = arc.getIdAgent();
		this.dureeAAjouter = (double) arc.getTotalMinutes();
		this.dateDebut = arc.getDateDebut();
		this.dateFin = arc.getDateFin();
		if (null != arc)
			this.organisationSyndicaleDto = new OrganisationSyndicaleDto(arc.getOrganisationSyndicale());
		if (histo != null && histo.getMotifCompteur() != null) {
			MotifCompteurDto dto = new MotifCompteurDto(histo.getMotifCompteur());
			this.motifCompteurDto = dto;
		}
	}

	public CompteurDto(AgentAsaA55Count arc, AgentHistoAlimManuelle histo) {
		this.idCompteur = arc.getIdAgentCount();
		this.idAgent = arc.getIdAgent();
		this.dureeAAjouter = (double) arc.getTotalMinutes();
		this.dateDebut = arc.getDateDebut();
		this.dateFin = arc.getDateFin();
		if (histo != null && histo.getMotifCompteur() != null) {
			MotifCompteurDto dto = new MotifCompteurDto(histo.getMotifCompteur());
			this.motifCompteurDto = dto;
		}
	}

	public CompteurDto() {
		super();
	}

	public CompteurDto(AgentAsaAmicaleCount arc, AgentHistoAlimManuelle histo) {
		this.idCompteur = arc.getIdAgentCount();
		this.idAgent = arc.getIdAgent();
		this.dureeAAjouter = (double) arc.getTotalMinutes();
		this.dateDebut = arc.getDateDebut();
		this.dateFin = arc.getDateFin();
		if (histo != null && histo.getMotifCompteur() != null) {
			MotifCompteurDto dto = new MotifCompteurDto(histo.getMotifCompteur());
			this.motifCompteurDto = dto;
		}
		this.actif = arc.isActif();
	}

	public OrganisationSyndicaleDto getOrganisationSyndicaleDto() {
		return organisationSyndicaleDto;
	}

	public void setOrganisationSyndicaleDto(OrganisationSyndicaleDto organisationSyndicaleDto) {
		this.organisationSyndicaleDto = organisationSyndicaleDto;
	}

	public MotifCompteurDto getMotifCompteurDto() {
		return motifCompteurDto;
	}

	public void setMotifCompteurDto(MotifCompteurDto motifCompteurDto) {
		this.motifCompteurDto = motifCompteurDto;
	}

	public Integer getIdCompteur() {
		return idCompteur;
	}

	public void setIdCompteur(Integer idCompteur) {
		this.idCompteur = idCompteur;
	}

	public boolean isActif() {
		return actif;
	}

	public void setActif(boolean actif) {
		this.actif = actif;
	}

}
