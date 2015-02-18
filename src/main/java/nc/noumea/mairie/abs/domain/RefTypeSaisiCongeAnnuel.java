package nc.noumea.mairie.abs.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "ABS_REF_TYPE_SAISI_CONGE_ANNUEL")
@PersistenceUnit(unitName = "absPersistenceUnit")
public class RefTypeSaisiCongeAnnuel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID_REF_TYPE_SAISI_CONGE_ANNUEL")
	private Integer idRefTypeSaisiCongeAnnuel;

	@NotNull
	@Column(name = "CODE_BASE_HORAIRE_ABSENCE", nullable = false)
	private String codeBaseHoraireAbsence;

	@Column(name = "DESCRIPTION")
	private String description;

	@ManyToOne
	@JoinColumn(name = "ID_REF_TYPE_ABSENCE", referencedColumnName = "ID_REF_TYPE_ABSENCE")
	private RefTypeAbsence type;

	@NotNull
	@Column(name = "CALENDAR_DATE_DEBUT", nullable = false)
	@Type(type = "boolean")
	private boolean calendarDateDebut;

	@NotNull
	@Column(name = "CHK_DATE_DEBUT", nullable = false)
	@Type(type = "boolean")
	private boolean chkDateDebut;

	@NotNull
	@Column(name = "CALENDAR_DATE_FIN", nullable = false)
	@Type(type = "boolean")
	private boolean calendarDateFin;

	@NotNull
	@Column(name = "CHK_DATE_FIN", nullable = false)
	@Type(type = "boolean")
	private boolean chkDateFin;

	@NotNull
	@Column(name = "CALENDAR_DATE_REPRISE", nullable = false)
	@Type(type = "boolean")
	private boolean calendarDateReprise;

	@Column(name = "QUOTA_MULTIPLE")
	private Integer quotaMultiple;

	@Column(name = "QUOTA_DECOMPTE")
	private Integer quotaDecompte;

	@NotNull
	@Column(name = "DECOMPTE_SAMEDI", nullable = false)
	@Type(type = "boolean")
	private boolean decompteSamedi;

	@NotNull
	@Column(name = "CONSECUTIF", nullable = false)
	@Type(type = "boolean")
	private boolean consecutif;

	public Integer getIdRefTypeSaisiCongeAnnuel() {
		return idRefTypeSaisiCongeAnnuel;
	}

	public void setIdRefTypeSaisiCongeAnnuel(Integer idRefTypeSaisiCongeAnnuel) {
		this.idRefTypeSaisiCongeAnnuel = idRefTypeSaisiCongeAnnuel;
	}

	public String getCodeBaseHoraireAbsence() {
		return codeBaseHoraireAbsence;
	}

	public void setCodeBaseHoraireAbsence(String codeBaseHoraireAbsence) {
		this.codeBaseHoraireAbsence = codeBaseHoraireAbsence;
	}

	public RefTypeAbsence getType() {
		return type;
	}

	public void setType(RefTypeAbsence type) {
		this.type = type;
	}

	public boolean isCalendarDateDebut() {
		return calendarDateDebut;
	}

	public void setCalendarDateDebut(boolean calendarDateDebut) {
		this.calendarDateDebut = calendarDateDebut;
	}

	public boolean isChkDateDebut() {
		return chkDateDebut;
	}

	public void setChkDateDebut(boolean chkDateDebut) {
		this.chkDateDebut = chkDateDebut;
	}

	public boolean isCalendarDateFin() {
		return calendarDateFin;
	}

	public void setCalendarDateFin(boolean calendarDateFin) {
		this.calendarDateFin = calendarDateFin;
	}

	public boolean isChkDateFin() {
		return chkDateFin;
	}

	public void setChkDateFin(boolean chkDateFin) {
		this.chkDateFin = chkDateFin;
	}

	public boolean isCalendarDateReprise() {
		return calendarDateReprise;
	}

	public void setCalendarDateReprise(boolean calendarDateReprise) {
		this.calendarDateReprise = calendarDateReprise;
	}

	public Integer getQuotaMultiple() {
		return quotaMultiple;
	}

	public void setQuotaMultiple(Integer quotaMultiple) {
		this.quotaMultiple = quotaMultiple;
	}

	public boolean isDecompteSamedi() {
		return decompteSamedi;
	}

	public void setDecompteSamedi(boolean decompteSamedi) {
		this.decompteSamedi = decompteSamedi;
	}

	public boolean isConsecutif() {
		return consecutif;
	}

	public void setConsecutif(boolean consecutif) {
		this.consecutif = consecutif;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getQuotaDecompte() {
		return quotaDecompte;
	}

	public void setQuotaDecompte(Integer quotaDecompte) {
		this.quotaDecompte = quotaDecompte;
	}

}
