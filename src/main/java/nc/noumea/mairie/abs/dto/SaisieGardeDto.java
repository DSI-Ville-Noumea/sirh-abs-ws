package nc.noumea.mairie.abs.dto;

import java.util.ArrayList;
import java.util.List;

public class SaisieGardeDto {
	
	private List<AgentJoursFeriesGardeDto> listAgentAvecGarde;
	private List<JourDto> joursFerieHeader;
	
	public SaisieGardeDto() {
		this.joursFerieHeader = new ArrayList<JourDto>();
		this.listAgentAvecGarde = new ArrayList<AgentJoursFeriesGardeDto>();
	}
	
	public List<AgentJoursFeriesGardeDto> getListAgentAvecGarde() {
		return listAgentAvecGarde;
	}
	public void setListAgentAvecGarde(
			List<AgentJoursFeriesGardeDto> listAgentAvecGarde) {
		this.listAgentAvecGarde = listAgentAvecGarde;
	}
	public List<JourDto> getJoursFerieHeader() {
		return joursFerieHeader;
	}
	public void setJoursFerieHeader(List<JourDto> joursFerieHeader) {
		this.joursFerieHeader = joursFerieHeader;
	}
}
