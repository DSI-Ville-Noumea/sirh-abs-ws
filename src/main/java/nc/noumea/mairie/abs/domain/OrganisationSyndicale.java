package nc.noumea.mairie.abs.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "ABS_ORGANISATION_SYNDICALE")
@PersistenceUnit(unitName = "absPersistenceUnit")
public class OrganisationSyndicale {

	@Id
	@Column(name = "ID_ORGANISATION_SYNDICALE")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idOrganisationSyndicale;

	@NotNull
	@Column(name = "LIBELLE", columnDefinition = "NVARCHAR2")
	private String libelle;

	@NotNull
	@Column(name = "SIGLE", columnDefinition = "NVARCHAR2")
	private String sigle;

	@NotNull
	@Column(name = "ACTIF", nullable = false)
	@Type(type = "boolean")
	private boolean actif;

	@OneToMany(mappedBy = "organisationSyndicale", fetch = FetchType.EAGER, orphanRemoval = true, cascade = CascadeType.ALL)
	private List<AgentOrganisationSyndicale> agents = new ArrayList<AgentOrganisationSyndicale>();

	@Version
	@Column(name = "version")
	private Integer version;

	public Integer getIdOrganisationSyndicale() {
		return idOrganisationSyndicale;
	}

	public void setIdOrganisationSyndicale(Integer idOrganisationSyndicale) {
		this.idOrganisationSyndicale = idOrganisationSyndicale;
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

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public List<AgentOrganisationSyndicale> getAgents() {
		return agents;
	}

	public void setAgents(List<AgentOrganisationSyndicale> agents) {
		this.agents = agents;
	}

}
