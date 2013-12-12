package nc.noumea.mairie.abs.dto;

import java.util.ArrayList;
import java.util.List;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

public class InputterDto implements IJSONSerialize, IJSONDeserialize<InputterDto> {

	private AgentDto delegataire;
	private List<AgentDto> operateurs;

	public InputterDto() {
		operateurs = new ArrayList<AgentDto>();
	}

	@Override
	public String serializeInJSON() {
		return new JSONSerializer().exclude("*.class").include("delegataire").include("operateurs.*").serialize(this);
	}

	@Override
	public InputterDto deserializeFromJSON(String json) {
		return new JSONDeserializer<InputterDto>().deserializeInto(json, this);
	}

	public AgentDto getDelegataire() {
		return delegataire;
	}

	public void setDelegataire(AgentDto delegataire) {
		this.delegataire = delegataire;
	}

	public List<AgentDto> getOperateurs() {
		return operateurs;
	}

	public void setOperateurs(List<AgentDto> operateurs) {
		this.operateurs = operateurs;
	}
}
