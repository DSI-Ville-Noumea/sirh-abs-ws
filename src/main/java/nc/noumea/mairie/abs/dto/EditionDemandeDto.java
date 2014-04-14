package nc.noumea.mairie.abs.dto;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class EditionDemandeDto {

	private DemandeDto demande;

	private AgentWithServiceDto approbateur;

	private SoldeDto solde;

	public EditionDemandeDto() {
	}

	public EditionDemandeDto(DemandeDto demandeDto, SoldeDto soldeDto, AgentWithServiceDto approbateurDto) {
		demande = demandeDto;
		solde = soldeDto;
		approbateur = approbateurDto;
	}

	public DemandeDto getDemande() {
		return demande;
	}

	public void setDemande(DemandeDto demande) {
		this.demande = demande;
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
