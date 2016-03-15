package nc.noumea.mairie.abs.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.envers.Audited;

@Entity
@Table(name = "ABS_DROIT_DROITS_AGENT") 
@Audited
@PersistenceUnit(unitName = "absPersistenceUnit")
public class DroitDroitsAgent {
	
	
	@Id
	@Column(name = "ID_DROIT_DROITS_AGENT")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idDroitDroitsAgent;
	
	@ManyToOne()
	@JoinColumn(name = "ID_DROIT", referencedColumnName = "ID_DROIT")
	private Droit droit;

	@ManyToOne()
	@JoinColumn(name = "ID_DROITS_AGENT", referencedColumnName = "ID_DROITS_AGENT")
	private DroitsAgent droitsAgent;

	@ManyToOne()
	@JoinColumn(name = "ID_DROIT_PROFIL", referencedColumnName = "ID_DROIT_PROFIL")
	private DroitProfil droitProfil;
	
	@Version
    @Column(name = "version")
	private Integer version;

	public Integer getIdDroitDroitsAgent() {
		return idDroitDroitsAgent;
	}

	public void setIdDroitDroitsAgent(Integer idDroitDroitsAgent) {
		this.idDroitDroitsAgent = idDroitDroitsAgent;
	}

	public Droit getDroit() {
		return droit;
	}

	public void setDroit(Droit droit) {
		this.droit = droit;
	}

	public DroitsAgent getDroitsAgent() {
		return droitsAgent;
	}

	public void setDroitsAgent(DroitsAgent droitsAgent) {
		this.droitsAgent = droitsAgent;
	}

	public DroitProfil getDroitProfil() {
		return droitProfil;
	}

	public void setDroitProfil(DroitProfil droitProfil) {
		this.droitProfil = droitProfil;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}
	
	
}
