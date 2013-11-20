package nc.noumea.mairie.abs.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord(persistenceUnit = "absPersistenceUnit", table = "ABS_DROIT")
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

	@Column(name = "ID_AGENT_DELEGATAIRE")
	private Integer idAgentDelegataire;

	@ManyToOne(optional = true)
	@JoinColumn(name = "ID_DROIT_APPROBATEUR", referencedColumnName = "ID_DROIT")
	private Droit droitApprobateur;

	@OneToMany(mappedBy = "droitApprobateur", orphanRemoval = true, cascade = CascadeType.ALL)
	private Set<Droit> operateurs = new HashSet<Droit>();

	@ManyToMany
	@JoinTable(name = "ABS_DROIT_DROITS_AGENT", joinColumns = @JoinColumn(name = "ID_DROIT"), inverseJoinColumns = @JoinColumn(name = "ID_DROITS_AGENT"))
	private Set<DroitsAgent> agents = new HashSet<DroitsAgent>();

	@ManyToMany
	@JoinTable(name = "ABS_DROIT_PROFIL", joinColumns = @JoinColumn(name = "ID_DROIT"), inverseJoinColumns = @JoinColumn(name = "ID_PROFIL"))
	private Set<Profil> profils = new HashSet<Profil>();
}
