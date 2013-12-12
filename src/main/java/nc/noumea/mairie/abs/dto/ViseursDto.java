package nc.noumea.mairie.abs.dto;

import java.util.ArrayList;
import java.util.List;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

public class ViseursDto implements IJSONSerialize, IJSONDeserialize<ViseursDto> {

	private List<AgentDto> viseurs;

	public ViseursDto() {
		viseurs = new ArrayList<AgentDto>();
	}

	@Override
	public String serializeInJSON() {
		return new JSONSerializer().exclude("*.class").include("viseurs.*").serialize(this);
	}

	@Override
	public ViseursDto deserializeFromJSON(String json) {
		return new JSONDeserializer<ViseursDto>().deserializeInto(json, this);
	}

	public List<AgentDto> getViseurs() {
		return viseurs;
	}

	public void setViseurs(List<AgentDto> viseurs) {
		this.viseurs = viseurs;
	}
}
