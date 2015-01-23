package nc.noumea.mairie.abs.dto;

import java.util.Date;

import nc.noumea.mairie.abs.transformer.MSDateTransformer;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import flexjson.JSONSerializer;

public class MoisAlimAutoCongesAnnuelsDto {

	@JsonSerialize(using = JsonDateSerializer.class)
	@JsonDeserialize(using = JsonDateDeserializer.class)
	private Date dateMois;
	private AgentDto agent;
	@JsonSerialize(using = JsonDateSerializer.class)
	@JsonDeserialize(using = JsonDateDeserializer.class)
	private Date dateModification;
	private String status;

	public Date getDateMois() {
		return dateMois;
	}

	public void setDateMois(Date dateMois) {
		this.dateMois = dateMois;
	}

	public String getDtoToString(MoisAlimAutoCongesAnnuelsDto dto) {
		String json = new JSONSerializer().exclude("*.class").transform(new MSDateTransformer(), Date.class)
				.deepSerialize(dto);
		return json;
	}

	public AgentDto getAgent() {
		return agent;
	}

	public void setAgent(AgentDto agent) {
		this.agent = agent;
	}

	public Date getDateModification() {
		return dateModification;
	}

	public void setDateModification(Date dateModification) {
		this.dateModification = dateModification;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
