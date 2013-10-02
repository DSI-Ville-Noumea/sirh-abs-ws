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
@RooJpaActiveRecord(persistenceUnit = "absPersistenceUnit", identifierColumn = "ID_AGENT_RECUP_COUNT", identifierField = "idAgentRecupCount", identifierType = Integer.class, table = "ABS_AGENT_RECUP_COUNT", sequenceName = "ABS_S_AGENT_RECUP_COUNT")
public class AgentRecupCount {

	@NotNull
	@Column(name = "ID_AGENT")
	private Integer idAgent;
	
	@NotNull
	@Column(name = "TOTAL_MINUTES")
	private int totalMinutes;
	
	@Column(name = "DATE_MODIFICATION")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastModification;
	
}
