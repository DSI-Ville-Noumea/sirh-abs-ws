package nc.noumea.mairie.abs.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "ABS_CA_RESTITUTION_MASSIVE")
@PersistenceUnit(unitName = "absPersistenceUnit")
public class CongeAnnuelRestitutionMassive {
	
	@Id
	@Column(name = "ID_CA_RESTITUTION_MASSIVE")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idCongeAnnuelRestitutionMassiveTask;
	
	@NotNull
	@Column(name = "DATE_RESTITUTION")
	@Temporal(TemporalType.DATE)
	private Date dateRestitution;
	
	@NotNull
	@Column(name = "DATE_MODIFICATION")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateModification;
	
	@NotNull
	@Column(name = "STATUS")
	private String status;

	@Column(name = "MOTIF")
	private String motif;

	@NotNull
	@Column(name = "JOURNEE")
	private boolean journee;

	@NotNull
	@Column(name = "AM")
	private boolean matin;

	@NotNull
	@Column(name = "PM")
	private boolean apresMidi;
	
	@OneToMany(mappedBy = "restitutionMassive", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<CongeAnnuelRestitutionMassiveHisto> restitutionMassiveHisto = new ArrayList<CongeAnnuelRestitutionMassiveHisto>();

	public Integer getIdCongeAnnuelRestitutionMassiveTask() {
		return idCongeAnnuelRestitutionMassiveTask;
	}

	public void setIdCongeAnnuelRestitutionMassiveTask(
			Integer idCongeAnnuelRestitutionMassiveTask) {
		this.idCongeAnnuelRestitutionMassiveTask = idCongeAnnuelRestitutionMassiveTask;
	}

	public Date getDateRestitution() {
		return dateRestitution;
	}

	public void setDateRestitution(Date dateRestitution) {
		this.dateRestitution = dateRestitution;
	}

	public Date getDateModification() {
		return dateModification;
	}

	public void setDateModification(Date dateModification) {
		this.dateModification = dateModification;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMotif() {
		return motif;
	}

	public void setMotif(String motif) {
		this.motif = motif;
	}

	public boolean isJournee() {
		return journee;
	}

	public void setJournee(boolean journee) {
		this.journee = journee;
	}

	public boolean isMatin() {
		return matin;
	}

	public void setMatin(boolean matin) {
		this.matin = matin;
	}

	public boolean isApresMidi() {
		return apresMidi;
	}

	public void setApresMidi(boolean apresMidi) {
		this.apresMidi = apresMidi;
	}

	public List<CongeAnnuelRestitutionMassiveHisto> getRestitutionMassiveHisto() {
		return restitutionMassiveHisto;
	}

	public void setRestitutionMassiveHisto(
			List<CongeAnnuelRestitutionMassiveHisto> restitutionMassiveHisto) {
		this.restitutionMassiveHisto = restitutionMassiveHisto;
	}
	
}
