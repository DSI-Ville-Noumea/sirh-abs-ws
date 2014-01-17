package nc.noumea.mairie.abs.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord(persistenceUnit = "absPersistenceUnit", table = "ABS_AGENT_WEEK_ALIM_MANUELLE")
public class AgentWeekAlimManuelle {

	@Id
	@Column(name = "ID_AGENT_WEEK_ALIM_MANUELLE")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idAgentWeekAlimManuelle;

	@NotNull
	@Column(name = "ID_AGENT")
	private Integer idAgent;

	@Column(name = "DATE_MODIFICATION")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateModification;
	
	@NotNull
	@Column(name = "MINUTES")
	private int minutes;
	
	@ManyToOne()
	@JoinColumn(name = "ID_MOTIF_COMPTEUR", referencedColumnName = "ID_MOTIF_COMPTEUR")
	private MotifCompteur motifCompteur;
}
