package nc.noumea.mairie.abs.domain;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord(persistenceUnit = "absPersistenceUnit", table = "ABS_AGENT_RECUP_COUNT")
public class AgentRecupCount extends BaseAgentCount {

	@Id
	@Column(name = "ID_AGENT_RECUP_COUNT")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idAgentRecupCount;
	
}
