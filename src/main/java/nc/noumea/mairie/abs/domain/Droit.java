package nc.noumea.mairie.abs.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord(persistenceUnit = "absPersistenceUnit", table = "ABS_DROIT")
@NamedQueries({
		@NamedQuery(name = "getAgentAccessRights", query = "from Droit d where d.idAgent = :idAgent"),
		@NamedQuery(name = "getAgentsApprobateurs", query = "select d from Droit d inner join d.profils p where p.libelle = 'APPROBATEUR'"),
		@NamedQuery(name = "getDroitByProfilAndAgent", query = "select d from Droit d inner join d.profils p where p.libelle = :libelle and d.idAgent= :idAgent") })
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

	@ManyToMany
	@JoinTable(name = "ABS_DROIT_DROITS_AGENT", joinColumns = @JoinColumn(name = "ID_DROIT"), inverseJoinColumns = @JoinColumn(name = "ID_DROITS_AGENT"))
	private Set<DroitsAgent> agents = new HashSet<DroitsAgent>();

	@ManyToMany
	@JoinTable(name = "ABS_DROIT_PROFIL", joinColumns = @JoinColumn(name = "ID_DROIT"), inverseJoinColumns = @JoinColumn(name = "ID_PROFIL"))
	private Set<Profil> profils = new HashSet<Profil>();
}
