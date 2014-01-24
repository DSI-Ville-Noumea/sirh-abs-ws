package nc.noumea.mairie.abs.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

@MappedSuperclass
public abstract class BaseAgentWeekHisto {

	@NotNull
	@Column(name = "ID_AGENT")
	private Integer idAgent;

	@NotNull
	@Column(name = "MINUTES")
	private int minutes;

	@Column(name = "DATE_MONDAY")
	@Temporal(TemporalType.DATE)
	private Date dateMonday;

	@Column(name = "DATE_MODIFICATION")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastModification;
	
	@Version
    @Column(name = "version")
	private Integer version;

	public Integer getIdAgent() {
		return idAgent;
	}

	public void setIdAgent(Integer idAgent) {
		this.idAgent = idAgent;
	}

	public int getMinutes() {
		return minutes;
	}

	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}

	public Date getDateMonday() {
		return dateMonday;
	}

	public void setDateMonday(Date dateMonday) {
		this.dateMonday = dateMonday;
	}

	public Date getLastModification() {
		return lastModification;
	}

	public void setLastModification(Date lastModification) {
		this.lastModification = lastModification;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}
	
	
}
