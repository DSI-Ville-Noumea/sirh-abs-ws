package nc.noumea.mairie.abs.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord(persistenceUnit = "absPersistenceUnit", identifierColumn = "ID_AGENT_WEEK_RECUP", identifierField = "idAgentWeekRecup", identifierType = Integer.class, table = "ABS_AGENT_WEEK_RECUP", sequenceName = "ABS_S_AGENT_WEEK_RECUP")
public class AgentWeekRecup {

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
