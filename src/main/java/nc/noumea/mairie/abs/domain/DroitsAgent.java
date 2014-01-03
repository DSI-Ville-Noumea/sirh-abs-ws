package nc.noumea.mairie.abs.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord(persistenceUnit = "absPersistenceUnit", table = "ABS_DROITS_AGENT")
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
	
}
