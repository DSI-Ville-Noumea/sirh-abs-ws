package nc.noumea.mairie.abs.dto;

import java.util.ArrayList;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentOrganisationSyndicale;
import nc.noumea.mairie.abs.domain.OrganisationSyndicale;

public class OrganisationSyndicaleDto {

	private Integer idOrganisation;
	private String libelle;
	private String sigle;
	private boolean actif;
	private List<AgentOrganisationSyndicaleDto> listeAgents;

	public OrganisationSyndicaleDto() {
	}

	public OrganisationSyndicaleDto(OrganisationSyndicale org) {
		super();
		this.idOrganisation = org.getIdOrganisationSyndicale();
		this.libelle = org.getLibelle();
		this.sigle = org.getSigle();
		this.actif = org.isActif();
		List<AgentOrganisationSyndicaleDto> listAg = new ArrayList<AgentOrganisationSyndicaleDto>();
		for (AgentOrganisationSyndicale ag : org.getAgents()) {
			AgentOrganisationSyndicaleDto a = new AgentOrganisationSyndicaleDto(ag);
			listAg.add(a);
		}
		this.listeAgents = listAg;
	}
	public Integer getIdOrganisation() {
		return idOrganisation;
	}

	public void setIdOrganisation(Integer idOrganisation) {
		this.idOrganisation = idOrganisation;
	}

	public String getLibelle() {
		return libelle;
	}

	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}

	public String getSigle() {
		return sigle;
	}

	public void setSigle(String sigle) {
		this.sigle = sigle;
	}

	public boolean isActif() {
		return actif;
	}

	public void setActif(boolean actif) {
		this.actif = actif;
	}

	public List<AgentOrganisationSyndicaleDto> getListeAgents() {
		return listeAgents;
	}

	public void setListeAgents(List<AgentOrganisationSyndicaleDto> listeAgents) {
		this.listeAgents = listeAgents;
	}

}
