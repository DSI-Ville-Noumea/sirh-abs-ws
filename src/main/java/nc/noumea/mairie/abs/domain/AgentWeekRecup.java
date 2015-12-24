package nc.noumea.mairie.abs.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "ABS_AGENT_WEEK_RECUP")
@PersistenceUnit(unitName = "absPersistenceUnit")
@NamedQueries({
		@NamedQuery(name = "findAgentWeekRecupByIdAgentAndDateMonday", query = "select awr from AgentWeekRecup awr where awr.idAgent = :idAgent and awr.dateMonday = :dateMonday"),
		@NamedQuery(name = "findAgentWeekRecupByIdAgent", query = "select awr from AgentWeekRecup awr where awr.idAgent = :idAgent order by awr.dateDay ") ,
		@NamedQuery(name = "getWeekHistoRecupCountByIdAgentAndIdPointage", query = "select awr from AgentWeekRecup awr where awr.idAgent = :idAgent and awr.idPointage = :idPointage")
})
public class AgentWeekRecup extends BaseAgentWeekHisto {

	@Id
	@Column(name = "ID_AGENT_WEEK_RECUP")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idAgentWeekRecup;
	
	@Column(name = "ID_POINTAGE")
	private Integer idPointage;

	@Column(name = "DATE_DAY")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateDay;

	public Integer getIdAgentWeekRecup() {
		return idAgentWeekRecup;
	}

	public void setIdAgentWeekRecup(Integer idAgentWeekRecup) {
		this.idAgentWeekRecup = idAgentWeekRecup;
	}

	public Integer getIdPointage() {
		return idPointage;
	}

	public void setIdPointage(Integer idPointage) {
		this.idPointage = idPointage;
	}

	public Date getDateDay() {
		return dateDay;
	}

	public void setDateDay(Date dateDay) {
		this.dateDay = dateDay;
	}
}
