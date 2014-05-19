package nc.noumea.mairie.abs.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "ABS_DROIT")
@PersistenceUnit(unitName = "absPersistenceUnit")
@NamedQueries({
		@NamedQuery(name = "getAgentAccessRights", query = "from Droit d where d.idAgent = :idAgent"),
		@NamedQuery(name = "getAgentsApprobateurs", query = "select d from Droit d inner join d.droitProfils dp inner join dp.profil p where p.libelle = 'APPROBATEUR'"),
		@NamedQuery(name = "getDroitByProfilAndAgent", query = "select d from Droit d inner join d.droitProfils dp inner join dp.profil p where p.libelle = :libelle and d.idAgent= :idAgent") })
public class Droit {

	@Id
	@Column(name = "ID_DROIT")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idDroit;

	@NotNull
	@Column(name = "ID_AGENT")
	private Integer idAgent;

	@Column(name = "DATE_MODIFICATION")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateModification;

	@OneToMany(mappedBy = "droit", fetch = FetchType.LAZY,  cascade = CascadeType.ALL)
	private Set<DroitProfil> droitProfils = new HashSet<DroitProfil>();
	
	@OneToMany(mappedBy = "droit", fetch = FetchType.LAZY)
	private Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();

	@Version
    @Column(name = "version")
	private Integer version;

	public Integer getIdDroit() {
		return idDroit;
	}

	public void setIdDroit(Integer idDroit) {
		this.idDroit = idDroit;
	}

	public Integer getIdAgent() {
		return idAgent;
	}

	public void setIdAgent(Integer idAgent) {
		this.idAgent = idAgent;
	}

	public Date getDateModification() {
		return dateModification;
	}

	public void setDateModification(Date dateModification) {
		this.dateModification = dateModification;
	}

	public Set<DroitProfil> getDroitProfils() {
		return droitProfils;
	}

	public void setDroitProfils(Set<DroitProfil> droitProfils) {
		this.droitProfils = droitProfils;
	}

	public Set<DroitDroitsAgent> getDroitDroitsAgent() {
		return droitDroitsAgent;
	}

	public void setDroitDroitsAgent(Set<DroitDroitsAgent> droitDroitsAgent) {
		this.droitDroitsAgent = droitDroitsAgent;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}
}
