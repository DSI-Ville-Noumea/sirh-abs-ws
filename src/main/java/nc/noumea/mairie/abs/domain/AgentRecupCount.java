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
@RooJpaActiveRecord(persistenceUnit = "absPersistenceUnit", table = "ABS_AGENT_RECUP_COUNT")
@NamedQueries({
		@NamedQuery(name = "findAgentRecupCountByIdAgent", query = "select arc from AgentRecupCount arc where arc.idAgent = :idAgent")
})
public class AgentRecupCount {

	@Id
	@Column(name = "ID_AGENT_RECUP_COUNT")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idAgentRecupCount;
	
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
