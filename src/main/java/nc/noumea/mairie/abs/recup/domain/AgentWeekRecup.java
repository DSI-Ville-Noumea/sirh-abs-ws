package nc.noumea.mairie.abs.recup.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;

import nc.noumea.mairie.abs.domain.BaseAgentWeekHisto;

@Entity
@Table(name = "ABS_AGENT_WEEK_RECUP")
@PersistenceUnit(unitName = "absPersistenceUnit")
@NamedQueries({
	@NamedQuery(name = "findAgentWeekRecupByIdAgentAndDateMonday", query = "select awr from AgentWeekRecup awr where awr.idAgent = :idAgent and awr.dateMonday = :dateMonday")
})
public class AgentWeekRecup extends BaseAgentWeekHisto {

	@Id
	@Column(name = "ID_AGENT_WEEK_RECUP")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idAgentWeekRecup;

}
