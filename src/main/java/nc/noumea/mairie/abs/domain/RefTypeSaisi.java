package nc.noumea.mairie.abs.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "ABS_REF_TYPE_SAISI")
@PersistenceUnit(unitName = "absPersistenceUnit")
@NamedQueries({
	@NamedQuery(name = "getRefTypeSaisiByIdTypeDemande", query = "select d from RefTypeSaisi d where d.idRefTypeAbsence = :idRefTypeAbsence"),
	@NamedQuery(name = "getAllRefTypeSaisi", query = "select d from RefTypeSaisi d order by d.idRefTypeAbsence ")
})
public class RefTypeSaisi {

	@Id
	@Column(name = "ID_REF_TYPE_ABSENCE")
	@GeneratedValue(generator = "foreign")
    @GenericGenerator(name = "foreign", strategy = "foreign", parameters =
    { @Parameter(name = "property", value = "type") })
	private Integer idRefTypeAbsence;
	
	@OneToOne(optional = false)
    @PrimaryKeyJoinColumn 
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
	
	@OneToOne(fetch = FetchType.EAGER, optional = true)
	@JoinColumn(name = "ID_REF_UNITE_PERIODE_QUOTA")
	private RefUnitePeriodeQuota refUnitePeriodeQuota;

	@NotNull
	@Column(name = "PRESCRIPTEUR", nullable = false)
	@Type(type = "boolean")
	private boolean prescripteur;

	@NotNull
	@Column(name = "DATE_DECLARATION", nullable = false)
	@Type(type = "boolean")
	private boolean dateDeclaration;

	@NotNull
	@Column(name = "PROLONGATION", nullable = false)
	@Type(type = "boolean")
	private boolean prolongation;

	@NotNull
	@Column(name = "NOM_ENFANT", nullable = false)
	@Type(type = "boolean")
	private boolean nomEnfant;

	@NotNull
	@Column(name = "NOMBRE_ITT", nullable = false)
	@Type(type = "boolean")
	private boolean nombreITT;

	@NotNull
	@Column(name = "SIEGE_LESION", nullable = false)
	@Type(type = "boolean")
	private boolean siegeLesion;

	@NotNull
	@Column(name = "AT_REFERENCE", nullable = false)
	@Type(type = "boolean")
	private boolean atReference;

	@NotNull
	@Column(name = "NATURE_MALADIE_PRO", nullable = false)
	@Type(type = "boolean")
	private boolean maladiePro;
	
	@Column(name = "INFOS_PIECE_JOINTE", nullable = true)
	private String infosPieceJointe;
	

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

	public boolean isPrescripteur() {
		return prescripteur;
	}

	public void setPrescripteur(boolean prescripteur) {
		this.prescripteur = prescripteur;
	}

	public boolean isDateDeclaration() {
		return dateDeclaration;
	}

	public void setDateDeclaration(boolean dateDeclaration) {
		this.dateDeclaration = dateDeclaration;
	}

	public boolean isProlongation() {
		return prolongation;
	}

	public void setProlongation(boolean prolongation) {
		this.prolongation = prolongation;
	}

	public boolean isNomEnfant() {
		return nomEnfant;
	}

	public void setNomEnfant(boolean nomEnfant) {
		this.nomEnfant = nomEnfant;
	}

	public boolean isNombreITT() {
		return nombreITT;
	}

	public void setNombreITT(boolean nombreITT) {
		this.nombreITT = nombreITT;
	}

	public boolean isSiegeLesion() {
		return siegeLesion;
	}

	public void setSiegeLesion(boolean siegeLesion) {
		this.siegeLesion = siegeLesion;
	}

	public boolean isAtReference() {
		return atReference;
	}

	public void setAtReference(boolean atReference) {
		this.atReference = atReference;
	}

	public boolean isMaladiePro() {
		return maladiePro;
	}

	public void setMaladiePro(boolean maladiePro) {
		this.maladiePro = maladiePro;
	}

	public String getInfosPieceJointe() {
		return infosPieceJointe;
	}

	public void setInfosPieceJointe(String infosPieceJointe) {
		this.infosPieceJointe = infosPieceJointe;
	}
	
}
