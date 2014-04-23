package nc.noumea.mairie.abs.dto;

import java.util.ArrayList;
import java.util.List;

public class ViseursDto {

	private List<AgentDto> viseurs;

	public ViseursDto() {
		viseurs = new ArrayList<AgentDto>();
	}

	public List<AgentDto> getViseurs() {
		return viseurs;
	}

	public void setViseurs(List<AgentDto> viseurs) {
		this.viseurs = viseurs;
	}
}
