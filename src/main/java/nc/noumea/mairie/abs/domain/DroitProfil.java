package nc.noumea.mairie.abs.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = "ABS_DROIT_PROFIL") 
@PersistenceUnit(unitName = "absPersistenceUnit")
@NamedQueries({
		@NamedQuery(name = "getInputterDroitProfilOfApprobateurByLibelle", query = "select dp from DroitProfil dp where dp.droitApprobateur.idAgent= :idAgentApprobateur and dp.droit.idAgent= :idAgent and dp.droit.idAgent!= :idAgentApprobateur and dp.profil.libelle= :libelle "),
		@NamedQuery(name = "getDroitProfilByAgent", query = "select dp from DroitProfil dp where dp.droitApprobateur.idAgent = :idAgentApprobateur and dp.droit.idAgent = :idAgent"),
		@NamedQuery(name = "getDroitProfilApprobateur", query = "select dp from DroitProfil dp where dp.droitApprobateur.idAgent = :idAgentApprobateur and dp.droit.idAgent = :idAgentApprobateur"),
		@NamedQuery(name = "getDroitProfilByLibelleProfil", query = "select dp from DroitProfil dp where dp.droit.idAgent= :idAgent and dp.profil.libelle= :libelle ")
})
public class DroitProfil {

	@Id
	@Column(name = "ID_DROIT_PROFIL")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idDroitProfil;

	@ManyToOne()
	@JoinColumn(name = "ID_DROIT", referencedColumnName = "ID_DROIT")
	private Droit droit;

	@ManyToOne()
	@JoinColumn(name = "ID_PROFIL", referencedColumnName = "ID_PROFIL")
	private Profil profil;

	@ManyToOne()
	@JoinColumn(name = "ID_DROIT_APPROBATEUR", referencedColumnName = "ID_DROIT")
	private Droit droitApprobateur;
	
	@OneToMany(mappedBy = "droitProfil", fetch = FetchType.LAZY)
	private Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();
	
	@Version
    @Column(name = "version")
	private Integer version;

	public Integer getIdDroitProfil() {
		return idDroitProfil;
	}

	public void setIdDroitProfil(Integer idDroitProfil) {
		this.idDroitProfil = idDroitProfil;
	}

	public Droit getDroit() {
		return droit;
	}

	public void setDroit(Droit droit) {
		this.droit = droit;
	}

	public Profil getProfil() {
		return profil;
	}

	public void setProfil(Profil profil) {
		this.profil = profil;
	}

	public Droit getDroitApprobateur() {
		return droitApprobateur;
	}

	public void setDroitApprobateur(Droit droitApprobateur) {
		this.droitApprobateur = droitApprobateur;
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
