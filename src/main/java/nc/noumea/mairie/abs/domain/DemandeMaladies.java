package nc.noumea.mairie.abs.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "ABS_DEMANDE_MALADIES")
@PersistenceUnit(unitName = "absPersistenceUnit")
@PrimaryKeyJoinColumn(name = "ID_DEMANDE")
public class DemandeMaladies extends Demande implements Cloneable {

	@Column(name = "DUREE", columnDefinition = "numeric")
	private Double					duree;

	// #32371 maladie enfant - saisie possible à la demi-journée
	@NotNull
	@Column(name = "DATE_DEBUT_AM")
	private boolean					dateDebutAM;

	@NotNull
	@Column(name = "DATE_DEBUT_PM")
	private boolean					dateDebutPM;

	@NotNull
	@Column(name = "DATE_FIN_AM")
	private boolean					dateFinAM;

	@NotNull
	@Column(name = "DATE_FIN_PM")
	private boolean					dateFinPM;

	@Column(name = "NOMBRE_ITT", columnDefinition = "numeric")
	private Double					nombreITT;

	@NotNull
	@Column(name = "PRESCRIPTEUR")
	private String					prescripteur;

	@Column(name = "NOM_ENFANT")
	private String					nomEnfant;

	@Column(name = "DATE_DECLARATION")
	@Temporal(TemporalType.DATE)
	private Date					dateDeclaration;

	@Column(name = "DATE_ACCIDENT_TRAVAIL")
	@Temporal(TemporalType.DATE)
	private Date					dateAccidentTravail;

	@Column(name = "SANS_AT")
	private boolean					sansArretTravail;

	@Column(name = "PROLONGATION")
	private boolean					prolongation;

	@OneToOne(fetch = FetchType.LAZY, optional = true, orphanRemoval = false)
	@JoinColumn(name = "ID_REF_ACCIDENT_TRAVAIL")
	private RefTypeAccidentTravail	typeAccidentTravail;

	@OneToOne(fetch = FetchType.LAZY, optional = true, orphanRemoval = false)
	@JoinColumn(name = "ID_REF_SIEGE_LESION")
	private RefTypeSiegeLesion		typeSiegeLesion;

	@OneToOne(fetch = FetchType.LAZY, optional = true, orphanRemoval = false)
	@JoinColumn(name = "ID_REF_MALADIE_PRO")
	private RefTypeMaladiePro		typeMaladiePro;

	@OneToOne(optional = true)
	@JoinColumn(name = "ID_AT_REFERENCE")
	private DemandeMaladies			accidentTravailReference;

	///////////////////
	// specifique aux AT et Maladie Pro dans SIRH > Agent > HSCT
	@Column(name = "DATE_TRANSMISSION_CAFAT")
	@Temporal(TemporalType.DATE)
	private Date					dateTransmissionCafat;

	@Column(name = "DATE_DECISION_CAFAT")
	@Temporal(TemporalType.DATE)
	private Date					dateDecisionCafat;

	@Column(name = "DATE_COMMISSION_APTITUDE")
	@Temporal(TemporalType.DATE)
	private Date					dateCommissionAptitude;

	@Column(name = "TAUX_CAFAT", columnDefinition = "numeric")
	private Double					tauxCafat;

	@Column(name = "AVIS_COMMISSION_APTITUDE")
	private Boolean					avisCommissionAptitude;

	// FIN specifique aux AT et Maladie Pro dans SIRH > Agent > HSCT
	///////////////////

	@Column(name = "TOTAL_PRIS")
	private Integer					totalPris;

	@Column(name = "NOMBRE_JOUR_COUPE_DS")
	private Integer					nombreJoursCoupeDemiSalaire;

	@Column(name = "NOMBRE_JOUR_COUPE_PS")
	private Integer					nombreJoursCoupePleinSalaire;

	@Column(name = "NOMBRE_JOUR_RAP_DS")
	private Integer					nombreJoursResteAPrendreDemiSalaire;

	@Column(name = "NOMBRE_JOUR_RAP_PS")
	private Integer					nombreJoursResteAPrendrePleinSalaire;

	public Double getDuree() {
		return duree;
	}

	public void setDuree(Double duree) {
		this.duree = duree;
	}

	public Double getNombreITT() {
		return nombreITT;
	}

	public void setNombreITT(Double nombreITT) {
		this.nombreITT = nombreITT;
	}

	public String getPrescripteur() {
		return prescripteur;
	}

	public void setPrescripteur(String prescripteur) {
		this.prescripteur = prescripteur;
	}

	public String getNomEnfant() {
		return nomEnfant;
	}

	public void setNomEnfant(String nomEnfant) {
		this.nomEnfant = nomEnfant;
	}

	public Date getDateDeclaration() {
		return dateDeclaration;
	}

	public void setDateDeclaration(Date dateDeclaration) {
		this.dateDeclaration = dateDeclaration;
	}

	public boolean isProlongation() {
		return prolongation;
	}

	public void setProlongation(boolean prolongation) {
		this.prolongation = prolongation;
	}

	public RefTypeAccidentTravail getTypeAccidentTravail() {
		return typeAccidentTravail;
	}

	public void setTypeAccidentTravail(RefTypeAccidentTravail typeAccidentTravail) {
		this.typeAccidentTravail = typeAccidentTravail;
	}

	public RefTypeSiegeLesion getTypeSiegeLesion() {
		return typeSiegeLesion;
	}

	public void setTypeSiegeLesion(RefTypeSiegeLesion typeSiegeLesion) {
		this.typeSiegeLesion = typeSiegeLesion;
	}

	public RefTypeMaladiePro getTypeMaladiePro() {
		return typeMaladiePro;
	}

	public void setTypeMaladiePro(RefTypeMaladiePro typeMaladiePro) {
		this.typeMaladiePro = typeMaladiePro;
	}

	public DemandeMaladies getAccidentTravailReference() {
		return accidentTravailReference;
	}

	public void setAccidentTravailReference(DemandeMaladies accidentTravailReference) {
		this.accidentTravailReference = accidentTravailReference;
	}

	public Date getDateTransmissionCafat() {
		return dateTransmissionCafat;
	}

	public void setDateTransmissionCafat(Date dateTransmissionCafat) {
		this.dateTransmissionCafat = dateTransmissionCafat;
	}

	public Date getDateDecisionCafat() {
		return dateDecisionCafat;
	}

	public void setDateDecisionCafat(Date dateDecisionCafat) {
		this.dateDecisionCafat = dateDecisionCafat;
	}

	public Date getDateCommissionAptitude() {
		return dateCommissionAptitude;
	}

	public void setDateCommissionAptitude(Date dateCommissionAptitude) {
		this.dateCommissionAptitude = dateCommissionAptitude;
	}

	public Double getTauxCafat() {
		return tauxCafat;
	}

	public void setTauxCafat(Double tauxCafat) {
		this.tauxCafat = tauxCafat;
	}

	public Boolean isAvisCommissionAptitude() {
		return avisCommissionAptitude;
	}

	public void setAvisCommissionAptitude(Boolean avisCommissionAptitude) {
		this.avisCommissionAptitude = avisCommissionAptitude;
	}

	public Integer getTotalPris() {
		return totalPris;
	}

	public void setTotalPris(Integer totalPris) {
		this.totalPris = totalPris;
	}

	public Integer getNombreJoursCoupeDemiSalaire() {
		return nombreJoursCoupeDemiSalaire;
	}

	public void setNombreJoursCoupeDemiSalaire(Integer nombreJoursCoupeDemiSalaire) {
		this.nombreJoursCoupeDemiSalaire = nombreJoursCoupeDemiSalaire;
	}

	public Integer getNombreJoursCoupePleinSalaire() {
		return nombreJoursCoupePleinSalaire;
	}

	public void setNombreJoursCoupePleinSalaire(Integer nombreJoursCoupePleinSalaire) {
		this.nombreJoursCoupePleinSalaire = nombreJoursCoupePleinSalaire;
	}

	public Integer getNombreJoursResteAPrendreDemiSalaire() {
		return nombreJoursResteAPrendreDemiSalaire;
	}

	public void setNombreJoursResteAPrendreDemiSalaire(Integer nombreJoursResteAPrendreDemiSalaire) {
		this.nombreJoursResteAPrendreDemiSalaire = nombreJoursResteAPrendreDemiSalaire;
	}

	public Integer getNombreJoursResteAPrendrePleinSalaire() {
		return nombreJoursResteAPrendrePleinSalaire;
	}

	public void setNombreJoursResteAPrendrePleinSalaire(Integer nombreJoursResteAPrendrePleinSalaire) {
		this.nombreJoursResteAPrendrePleinSalaire = nombreJoursResteAPrendrePleinSalaire;
	}

	public boolean isDateDebutAM() {
		return dateDebutAM;
	}

	public void setDateDebutAM(boolean dateDebutAM) {
		this.dateDebutAM = dateDebutAM;
	}

	public boolean isDateDebutPM() {
		return dateDebutPM;
	}

	public void setDateDebutPM(boolean dateDebutPM) {
		this.dateDebutPM = dateDebutPM;
	}

	public boolean isDateFinAM() {
		return dateFinAM;
	}

	public void setDateFinAM(boolean dateFinAM) {
		this.dateFinAM = dateFinAM;
	}

	public boolean isDateFinPM() {
		return dateFinPM;
	}

	public void setDateFinPM(boolean dateFinPM) {
		this.dateFinPM = dateFinPM;
	}

	public Date getDateAccidentTravail() {
		return dateAccidentTravail;
	}

	public void setDateAccidentTravail(Date dateAccidentTravail) {
		this.dateAccidentTravail = dateAccidentTravail;
	}

	public boolean isSansArretTravail() {
		return sansArretTravail;
	}

	public void setSansArretTravail(boolean sansArretTravail) {
		this.sansArretTravail = sansArretTravail;
	}

	public Object clone() {
		Object o = null;
		try {
			o = super.clone();
		} catch (CloneNotSupportedException cnse) {
			cnse.printStackTrace(System.err);
		}
		// on renvoie le clone
		return o;
	}

}
