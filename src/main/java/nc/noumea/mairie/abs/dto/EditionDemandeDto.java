package nc.noumea.mairie.abs.dto;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class EditionDemandeDto {

	private DemandeDto demande;

	private AgentWithServiceDto agent;

	private AgentWithServiceDto approbateur;

	private SoldeDto solde;

	public EditionDemandeDto() {
	}

	public EditionDemandeDto(DemandeDto demandeDto, AgentWithServiceDto agentDto, SoldeDto soldeDto,
			AgentWithServiceDto approbateurDto) {
		demande = demandeDto;
		agent = agentDto;
		solde = soldeDto;
		approbateur = approbateurDto;
	}

	public DemandeDto getDemande() {
		return demande;
	}

	public void setDemande(DemandeDto demande) {
		this.demande = demande;
	}

	public AgentWithServiceDto getAgent() {
		return agent;
	}

	public void setAgent(AgentWithServiceDto agent) {
		this.agent = agent;
	}

	public SoldeDto getSolde() {
		return solde;
	}

	public void setSolde(SoldeDto solde) {
		this.solde = solde;
	}

	public AgentWithServiceDto getApprobateur() {
		return approbateur;
	}

	public void setApprobateur(AgentWithServiceDto approbateur) {
		this.approbateur = approbateur;
	}

}
