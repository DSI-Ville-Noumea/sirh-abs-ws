package nc.noumea.mairie.abs.dto;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class EditionDemandeDto {

	private DemandeDto demande;

	private AgentWithServiceDto approbateur;

	public EditionDemandeDto() {
	}

	public EditionDemandeDto(DemandeDto demandeDto, AgentWithServiceDto approbateurDto) {
		demande = demandeDto;
		approbateur = approbateurDto;
	}

	public DemandeDto getDemande() {
		return demande;
	}

	public void setDemande(DemandeDto demande) {
		this.demande = demande;
	}

	public AgentWithServiceDto getApprobateur() {
		return approbateur;
	}

	public void setApprobateur(AgentWithServiceDto approbateur) {
		this.approbateur = approbateur;
	}

}
