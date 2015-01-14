package nc.noumea.mairie.abs.dto;

import java.util.ArrayList;
import java.util.List;

public class SaisieReposDto {
	
	private List<AgentJoursFeriesReposDto> listAgentAvecRepos;
	private List<JourDto> joursFerieHeader;
	
	public SaisieReposDto() {
		this.joursFerieHeader = new ArrayList<JourDto>();
		this.listAgentAvecRepos = new ArrayList<AgentJoursFeriesReposDto>();
	}
	
	public List<AgentJoursFeriesReposDto> getListAgentAvecRepos() {
		return listAgentAvecRepos;
	}
	public void setListAgentAvecRepos(
			List<AgentJoursFeriesReposDto> listAgentAvecRepos) {
		this.listAgentAvecRepos = listAgentAvecRepos;
	}
	public List<JourDto> getJoursFerieHeader() {
		return joursFerieHeader;
	}
	public void setJoursFerieHeader(List<JourDto> joursFerieHeader) {
		this.joursFerieHeader = joursFerieHeader;
	}
}
