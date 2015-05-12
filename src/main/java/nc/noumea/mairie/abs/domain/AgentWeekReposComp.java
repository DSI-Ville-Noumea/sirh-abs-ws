package nc.noumea.mairie.abs.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;

@Entity
@Table(name = "ABS_AGENT_WEEK_REPOS_COMP")
@PersistenceUnit(unitName = "absPersistenceUnit")
@NamedQueries({
		@NamedQuery(name = "findAgentWeekReposCompByIdAgentAndDateMonday", query = "select awr from AgentWeekReposComp awr where awr.idAgent = :idAgent and awr.dateMonday = :dateMonday"),
		@NamedQuery(name = "findAgentWeekReposCompByIdAgent", query = "select awr from AgentWeekReposComp awr where awr.idAgent = :idAgent") })
public class AgentWeekReposComp extends BaseAgentWeekHisto {

	@Id
	@Column(name = "ID_AGENT_WEEK_REPOS_COMP")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idAgentWeekReposComp;
}
