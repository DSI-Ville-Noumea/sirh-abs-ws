package nc.noumea.mairie.abs.dto;

import java.util.Date;

import nc.noumea.mairie.abs.asa.domain.AgentAsaA48Count;
import nc.noumea.mairie.abs.asa.domain.AgentAsaA52Count;
import nc.noumea.mairie.abs.asa.domain.AgentAsaA53Count;
import nc.noumea.mairie.abs.asa.domain.AgentAsaA54Count;
import nc.noumea.mairie.abs.asa.domain.AgentAsaA55Count;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

public class CompteurAsaDto {

	private Integer idAgent;

	private Double nb;

	@JsonSerialize(using = JsonDateSerializer.class)
	@JsonDeserialize(using = JsonDateDeserializer.class)
	private Date dateDebut;
	@JsonSerialize(using = JsonDateSerializer.class)
	@JsonDeserialize(using = JsonDateDeserializer.class)
	private Date dateFin;
	
	private OrganisationSyndicaleDto organisationSyndicaleDto;

	public CompteurAsaDto(AgentAsaA48Count arc) {
		this.idAgent = arc.getIdAgent();
		this.nb = arc.getTotalJours();
		this.dateDebut = arc.getDateDebut();
		this.dateFin = arc.getDateFin();
	}

	public CompteurAsaDto(AgentAsaA54Count arc) {
		this.idAgent = arc.getIdAgent();
		this.nb = arc.getTotalJours();
		this.dateDebut = arc.getDateDebut();
		this.dateFin = arc.getDateFin();
	}

	public CompteurAsaDto(AgentAsaA55Count arc) {
		this.idAgent = arc.getIdAgent();
		this.nb = (double) arc.getTotalMinutes();
		this.dateDebut = arc.getDateDebut();
		this.dateFin = arc.getDateFin();
	}
	
	public CompteurAsaDto(AgentAsaA52Count arc) {
		this.idAgent = arc.getIdAgent();
		this.nb = (double) arc.getTotalMinutes();
		this.dateDebut = arc.getDateDebut();
		this.dateFin = arc.getDateFin();
		if(null != arc)
			this.organisationSyndicaleDto = new OrganisationSyndicaleDto(arc.getOrganisationSyndicale());
	}
	
	public CompteurAsaDto(AgentAsaA53Count arc) {
		this.idAgent = arc.getIdAgent();
		this.nb = (double) arc.getTotalJours();
		this.dateDebut = arc.getDateDebut();
		this.dateFin = arc.getDateFin();
		if(null != arc)
			this.organisationSyndicaleDto = new OrganisationSyndicaleDto(arc.getOrganisationSyndicale());
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

	public Double getNb() {
		return nb;
	}

	public void setNb(Double nb) {
		this.nb = nb;
	}

	public OrganisationSyndicaleDto getOrganisationSyndicaleDto() {
		return organisationSyndicaleDto;
	}

	public void setOrganisationSyndicaleDto(
			OrganisationSyndicaleDto organisationSyndicaleDto) {
		this.organisationSyndicaleDto = organisationSyndicaleDto;
	}

}
