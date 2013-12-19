package nc.noumea.mairie.abs.domain;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord(persistenceUnit = "absPersistenceUnit", table = "ABS_AGENT_WEEK_REPOS_COMP")
@NamedQueries({
	@NamedQuery(name = "findAgentWeekReposCompByIdAgentAndDateMonday", query = "select awr from AgentWeekReposComp awr where awr.idAgent = :idAgent and awr.dateMonday = :dateMonday")
})
public class AgentWeekReposComp extends BaseAgentWeekHisto {

	@Id
	@Column(name = "ID_AGENT_WEEK_REPOS_COMP")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idAgentWeekReposComp;
}
