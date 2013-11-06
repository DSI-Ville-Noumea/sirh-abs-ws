package nc.noumea.mairie.abs.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
@RooJpaActiveRecord(persistenceUnit = "absPersistenceUnit", table = "ABS_AGENT_WEEK_RECUP")
@NamedQueries({
	@NamedQuery(name = "findAgentWeekRecupByIdAgentAndDateMonday", query = "select awr from AgentWeekRecup awr where awr.idAgent = :idAgent and awr.dateMonday = :dateMonday")
})
public class AgentWeekRecup {

	@Id
	@Column(name = "ID_AGENT_WEEK_RECUP")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idAgentWeekRecup;
	
	@NotNull
	@Column(name = "ID_AGENT")
	private Integer idAgent;
	
	@NotNull
	@Column(name = "MINUTES_RECUP")
	private int minutesRecup;
	
	@Column(name = "DATE_MONDAY")
	@Temporal(TemporalType.DATE)
	private Date dateMonday;
	
	@Column(name = "DATE_MODIFICATION")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastModification;
}
