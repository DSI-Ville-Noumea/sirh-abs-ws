package nc.noumea.mairie.abs.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "ABS_REF_TYPE_SAISI")
@PersistenceUnit(unitName = "absPersistenceUnit")
@NamedQueries({
	@NamedQuery(name = "getRefTypeSaisiByIdTypeDemande", query = "from RefTypeSaisi d where d.type.idRefTypeAbsence = :idRefTypeAbsence"),
	@NamedQuery(name = "getAllRefTypeSaisi", query = "from RefTypeSaisi d order by d.type.idRefTypeAbsence ")
})
public class RefTypeSaisi {

	@Id
	@Column(name = "ID_REF_TYPE_ABSENCE")
	private Integer idRefTypeAbsence;
	
	@OneToOne(optional = false)
	@JoinColumn(name = "ID_REF_TYPE_ABSENCE")
	private RefTypeAbsence type;
	
	@NotNull
	@Column(name = "CALENDAR_DATE_DEBUT", nullable = false)
	@Type(type = "boolean")
	private boolean calendarDateDebut;
	
	@NotNull
	@Column(name = "CALENDAR_HEURE_DEBUT", nullable = false)
	@Type(type = "boolean")
	private boolean calendarHeureDebut;
	
	@NotNull
	@Column(name = "CHK_DATE_DEBUT", nullable = false)
	@Type(type = "boolean")
	private boolean chkDateDebut;
	
	@NotNull
	@Column(name = "CALENDAR_DATE_FIN", nullable = false)
	@Type(type = "boolean")
	private boolean calendarDateFin;
	
	@NotNull
	@Column(name = "CALENDAR_HEURE_FIN", nullable = false)
	@Type(type = "boolean")
	private boolean calendarHeureFin;
	
	@NotNull
	@Column(name = "CHK_DATE_FIN", nullable = false)
	@Type(type = "boolean")
	private boolean chkDateFin;
	
	@NotNull
	@Column(name = "DUREE", nullable = false)
	@Type(type = "boolean")
	private boolean duree;
	
	@NotNull
	@Column(name = "PIECE_JOINTE", nullable = false)
	@Type(type = "boolean")
	private boolean pieceJointe;

	@NotNull
	@Column(name = "UNITE_DECOMPTE", nullable = false)
	private String uniteDecompte;
	
	@NotNull
	@Column(name = "COMPTEUR_COLLECTIF", nullable = false)
	@Type(type = "boolean")
	private boolean compteurCollectif;
	
	@NotNull
	@Column(name = "F", nullable = false)
	@Type(type = "boolean")
	private boolean fonctionnaire;
	
	@NotNull
	@Column(name = "C", nullable = false)
	@Type(type = "boolean")
	private boolean contractuel;
	
	@NotNull
	@Column(name = "CC", nullable = false)
	@Type(type = "boolean")
	private boolean conventionCollective;
	
	@NotNull
	@Column(name = "SAISIE_KIOSQUE", nullable = false)
	@Type(type = "boolean")
	private boolean saisieKiosque;

	@NotNull
	@Column(name = "MOTIF", nullable = false)
	@Type(type = "boolean")
	private boolean motif;
	
	@Column(name = "DESCRIPTION", columnDefinition = "text")
	private String description;
	
	@Column(name = "INFOS_COMPL")
	private String infosComplementaires;
	
	@NotNull
	@Column(name = "ALERTE", nullable = false)
	@Type(type = "boolean")
	private boolean alerte;
	
	@Column(name = "MESSAGE_ALERTE")
	private String messageAlerte;
	
	@Column(name = "QUOTA_MAX")
	private Integer quotaMax;
	
	@OneToOne(optional = true)
	@JoinColumn(name = "ID_REF_UNITE_PERIODE_QUOTA")
	private RefUnitePeriodeQuota refUnitePeriodeQuota;

	public Integer getIdRefTypeAbsence() {
		return idRefTypeAbsence;
	}

	public void setIdRefTypeAbsence(Integer idRefTypeAbsence) {
		this.idRefTypeAbsence = idRefTypeAbsence;
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

	public boolean isCalendarHeureDebut() {
		return calendarHeureDebut;
	}

	public void setCalendarHeureDebut(boolean calendarHeureDebut) {
		this.calendarHeureDebut = calendarHeureDebut;
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

	public boolean isCalendarHeureFin() {
		return calendarHeureFin;
	}

	public void setCalendarHeureFin(boolean calendarHeureFin) {
		this.calendarHeureFin = calendarHeureFin;
	}

	public boolean isChkDateFin() {
		return chkDateFin;
	}

	public void setChkDateFin(boolean chkDateFin) {
		this.chkDateFin = chkDateFin;
	}

	public boolean isDuree() {
		return duree;
	}

	public void setDuree(boolean duree) {
		this.duree = duree;
	}

	public boolean isPieceJointe() {
		return pieceJointe;
	}

	public void setPieceJointe(boolean pieceJointe) {
		this.pieceJointe = pieceJointe;
	}

	public String getUniteDecompte() {
		return uniteDecompte;
	}

	public void setUniteDecompte(String uniteDecompte) {
		this.uniteDecompte = uniteDecompte;
	}

	public boolean isCompteurCollectif() {
		return compteurCollectif;
	}

	public void setCompteurCollectif(boolean compteurCollectif) {
		this.compteurCollectif = compteurCollectif;
	}

	public boolean isFonctionnaire() {
		return fonctionnaire;
	}

	public void setFonctionnaire(boolean fonctionnaire) {
		this.fonctionnaire = fonctionnaire;
	}

	public boolean isContractuel() {
		return contractuel;
	}

	public void setContractuel(boolean contractuel) {
		this.contractuel = contractuel;
	}

	public boolean isConventionCollective() {
		return conventionCollective;
	}

	public void setConventionCollective(boolean conventionCollective) {
		this.conventionCollective = conventionCollective;
	}

	public boolean isSaisieKiosque() {
		return saisieKiosque;
	}

	public void setSaisieKiosque(boolean saisieKiosque) {
		this.saisieKiosque = saisieKiosque;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getInfosComplementaires() {
		return infosComplementaires;
	}

	public void setInfosComplementaires(String infosComplementaires) {
		this.infosComplementaires = infosComplementaires;
	}

	public boolean isAlerte() {
		return alerte;
	}

	public void setAlerte(boolean alerte) {
		this.alerte = alerte;
	}

	public String getMessageAlerte() {
		return messageAlerte;
	}

	public void setMessageAlerte(String messageAlerte) {
		this.messageAlerte = messageAlerte;
	}

	public Integer getQuotaMax() {
		return quotaMax;
	}

	public void setQuotaMax(Integer quotaMax) {
		this.quotaMax = quotaMax;
	}

	public RefUnitePeriodeQuota getRefUnitePeriodeQuota() {
		return refUnitePeriodeQuota;
	}

	public void setRefUnitePeriodeQuota(RefUnitePeriodeQuota refUnitePeriodeQuota) {
		this.refUnitePeriodeQuota = refUnitePeriodeQuota;
	}

	public boolean isMotif() {
		return motif;
	}

	public void setMotif(boolean motif) {
		this.motif = motif;
	}
	
}
