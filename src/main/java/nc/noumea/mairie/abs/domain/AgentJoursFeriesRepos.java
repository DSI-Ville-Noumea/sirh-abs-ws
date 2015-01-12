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
@Table(name = "ABS_AGENT_JOURS_FERIES_REPOS")
@PersistenceUnit(unitName = "absPersistenceUnit")
@NamedQueries({
	@NamedQuery(name = "findAgentJoursFeriesReposByIdAgentAndJourFerie", query = "select a from AgentJoursFeriesRepos a where a.idAgent = :idAgent and a.jourFerieChome = :jourFerieChome"),
	@NamedQuery(name = "findAgentJoursFeriesReposByIdAgentAndPeriode", query = "select a from AgentJoursFeriesRepos a where a.idAgent = :idAgent and a.jourFerieChome between :dateDebut and :dateFin"),
	@NamedQuery(name = "findAllAgentsJoursFeriesReposByPeriode", query = "select a from AgentJoursFeriesRepos a where a.jourFerieChome between :dateDebut and :dateFin")
})
public class AgentJoursFeriesRepos {
	
	@Id
	@Column(name = "ID_AGENT_JOURS_FERIES_REPOS")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idAgentJoursFeriesRepos;

	@Column(name = "ID_AGENT")
	private Integer idAgent;
	
	@Column(name = "JOUR_FERIE_REPOS")
	@Temporal(TemporalType.DATE)
	private Date jourFerieChome;
	
	@Column(name = "DATE_MODIFICATION")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateModification;

	public Integer getIdAgentJoursFeriesRepos() {
		return idAgentJoursFeriesRepos;
	}

	public void setIdAgentJoursFeriesRepos(Integer idAgentJoursFeriesRepos) {
		this.idAgentJoursFeriesRepos = idAgentJoursFeriesRepos;
	}

	public Integer getIdAgent() {
		return idAgent;
	}

	public void setIdAgent(Integer idAgent) {
		this.idAgent = idAgent;
	}

	public Date getJourFerieChome() {
		return jourFerieChome;
	}

	public void setJourFerieChome(Date jourFerieChome) {
		this.jourFerieChome = jourFerieChome;
	}

	public Date getDateModification() {
		return dateModification;
	}

	public void setDateModification(Date dateModification) {
		this.dateModification = dateModification;
	}
	
}
