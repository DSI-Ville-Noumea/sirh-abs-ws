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
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "ABS_DROITS_AGENT") 
@PersistenceUnit(unitName = "absPersistenceUnit")
@NamedQueries({
	@NamedQuery(name = "getListOfAgentsToInputOrApprove", query = "from DroitsAgent da INNER JOIN FETCH da.droitDroitsAgent dda INNER JOIN FETCH dda.droit d INNER JOIN FETCH dda.droitProfil dp where d.idAgent = :idAgent and dp.idDroitProfil = :idDroitProfil "),
	@NamedQuery(name = "getDroitsAgent", query = "from DroitsAgent da where da.idAgent = :idAgent"),
	@NamedQuery(name = "getListOfAgentsToInputOrApproveByService", query = "from DroitsAgent da INNER JOIN FETCH da.droitDroitsAgent dda INNER JOIN FETCH dda.droit d INNER JOIN FETCH dda.droitProfil dp where (d.idAgent = :idAgent) and da.codeService = :codeService and dp.idDroitProfil = :idDroitProfil "),
	@NamedQuery(name = "getListOfAgentsToInputOrApproveWithoutProfil", query = "from DroitsAgent da INNER JOIN FETCH da.droitDroitsAgent dda INNER JOIN FETCH dda.droit d where d.idAgent = :idAgent "),
	@NamedQuery(name = "getListOfAgentsToInputOrApproveByServiceWithoutProfil", query = "from DroitsAgent da INNER JOIN FETCH da.droitDroitsAgent dda INNER JOIN FETCH dda.droit d where (d.idAgent = :idAgent) and da.codeService = :codeService ")
})
public class DroitsAgent {

	@Id
	@Column(name = "ID_DROITS_AGENT")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idDroitsAgent;

	@NotNull
	@Column(name = "ID_AGENT")
	private Integer idAgent;

	@Column(name = "CODE_SERVICE")
	private String codeService;

	@Column(name = "LIBELLE_SERVICE")
	private String libelleService;

	@Column(name = "DATE_MODIFICATION")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateModification;
	
	@OneToMany(mappedBy = "droitsAgent", fetch = FetchType.LAZY, orphanRemoval = true, cascade = {CascadeType.ALL, CascadeType.REMOVE})
	private Set<DroitDroitsAgent> droitDroitsAgent = new HashSet<DroitDroitsAgent>();

	public Integer getIdDroitsAgent() {
		return idDroitsAgent;
	}

	public void setIdDroitsAgent(Integer idDroitsAgent) {
		this.idDroitsAgent = idDroitsAgent;
	}

	public Integer getIdAgent() {
		return idAgent;
	}

	public void setIdAgent(Integer idAgent) {
		this.idAgent = idAgent;
	}

	public String getCodeService() {
		return codeService;
	}

	public void setCodeService(String codeService) {
		this.codeService = codeService;
	}

	public String getLibelleService() {
		return libelleService;
	}

	public void setLibelleService(String libelleService) {
		this.libelleService = libelleService;
	}

	public Date getDateModification() {
		return dateModification;
	}

	public void setDateModification(Date dateModification) {
		this.dateModification = dateModification;
	}

	public Set<DroitDroitsAgent> getDroitDroitsAgent() {
		return droitDroitsAgent;
	}

	public void setDroitDroitsAgent(Set<DroitDroitsAgent> droitDroitsAgent) {
		this.droitDroitsAgent = droitDroitsAgent;
	}
	
	
}
