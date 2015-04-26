package nc.noumea.mairie.abs.dto;

import java.util.ArrayList;
import java.util.List;

public class AgentJoursFeriesGardeDto {

	private AgentDto agent;
	private List<JoursFeriesSaisiesGardeDto> joursFeriesEnGarde;

	public AgentJoursFeriesGardeDto() {
		this.joursFeriesEnGarde = new ArrayList<JoursFeriesSaisiesGardeDto>();
	}

	public AgentDto getAgent() {
		return agent;
	}

	public void setAgent(AgentDto agent) {
		this.agent = agent;
	}

	public List<JoursFeriesSaisiesGardeDto> getJoursFeriesEnGarde() {
		return joursFeriesEnGarde;
	}

	public void setJoursFeriesEnGarde(List<JoursFeriesSaisiesGardeDto> joursFeriesEnGarde) {
		this.joursFeriesEnGarde = joursFeriesEnGarde;
	}
}
