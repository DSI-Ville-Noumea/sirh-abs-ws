package nc.noumea.mairie.abs.dto;

import java.util.ArrayList;
import java.util.List;

public class InputterDto {

	private AgentDto delegataire;
	private List<AgentDto> operateurs;

	public InputterDto() {
		operateurs = new ArrayList<AgentDto>();
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
