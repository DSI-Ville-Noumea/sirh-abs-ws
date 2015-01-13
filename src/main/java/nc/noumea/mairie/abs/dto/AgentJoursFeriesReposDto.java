package nc.noumea.mairie.abs.dto;

import java.util.ArrayList;
import java.util.List;

public class AgentJoursFeriesReposDto {

	private AgentDto agent;
	private List<JoursFeriesSaisiesReposDto> joursFeriesEnRepos;
	
	public AgentJoursFeriesReposDto() {
		this.joursFeriesEnRepos = new ArrayList<JoursFeriesSaisiesReposDto>();
	}
	
	public AgentDto getAgent() {
		return agent;
	}
	public void setAgent(AgentDto agent) {
		this.agent = agent;
	}
	public List<JoursFeriesSaisiesReposDto> getJoursFeriesEnRepos() {
		return joursFeriesEnRepos;
	}
	public void setJoursFeriesEnRepos(
			List<JoursFeriesSaisiesReposDto> joursFeriesEnRepos) {
		this.joursFeriesEnRepos = joursFeriesEnRepos;
	}
	
	
}
