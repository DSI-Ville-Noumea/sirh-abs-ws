package nc.noumea.mairie.abs.droit.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "ABS_PROFIL") 
@PersistenceUnit(unitName = "absPersistenceUnit")
public class Profil {

	@Id
	@Column(name = "ID_PROFIL")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idProfil;

	@NotNull
	@Column(name = "LIBELLE", columnDefinition = "NVARCHAR2")
	private String libelle;

	@NotNull
	@Column(name = "SAISIE", nullable = false)
	@Type(type = "boolean")
	private boolean saisie;

	@NotNull
	@Column(name = "MODIFICATION", nullable = false)
	@Type(type = "boolean")
	private boolean modification;

	@NotNull
	@Column(name = "SUPPRESSION", nullable = false)
	@Type(type = "boolean")
	private boolean suppression;

	@NotNull
	@Column(name = "IMPRESSION", nullable = false)
	@Type(type = "boolean")
	private boolean impression;

	@NotNull
	@Column(name = "VISER_VISU", nullable = false)
	@Type(type = "boolean")
	private boolean viserVisu;

	@NotNull
	@Column(name = "VISER_MODIF", nullable = false)
	@Type(type = "boolean")
	private boolean viserModif;

	@NotNull
	@Column(name = "APPROUVER_VISU", nullable = false)
	@Type(type = "boolean")
	private boolean approuverVisu;

	@NotNull
	@Column(name = "APPROUVER_MODIF", nullable = false)
	@Type(type = "boolean")
	private boolean approuverModif;

	@NotNull
	@Column(name = "ANNULER", nullable = false)
	@Type(type = "boolean")
	private boolean annuler;

	@NotNull
	@Column(name = "VISU_SOLDE", nullable = false)
	@Type(type = "boolean")
	private boolean visuSolde;

	@NotNull
	@Column(name = "MAJ_SOLDE", nullable = false)
	@Type(type = "boolean")
	private boolean majSolde;

	@NotNull
	@Column(name = "DROIT_ACCES", nullable = false)
	@Type(type = "boolean")
	private boolean droitAcces;

	@Version
    @Column(name = "version")
	private Integer version;

	public Integer getIdProfil() {
		return idProfil;
	}

	public void setIdProfil(Integer idProfil) {
		this.idProfil = idProfil;
	}

	public String getLibelle() {
		return libelle;
	}

	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}

	public boolean isSaisie() {
		return saisie;
	}

	public void setSaisie(boolean saisie) {
		this.saisie = saisie;
	}

	public boolean isModification() {
		return modification;
	}

	public void setModification(boolean modification) {
		this.modification = modification;
	}

	public boolean isSuppression() {
		return suppression;
	}

	public void setSuppression(boolean suppression) {
		this.suppression = suppression;
	}

	public boolean isImpression() {
		return impression;
	}

	public void setImpression(boolean impression) {
		this.impression = impression;
	}

	public boolean isViserVisu() {
		return viserVisu;
	}

	public void setViserVisu(boolean viserVisu) {
		this.viserVisu = viserVisu;
	}

	public boolean isViserModif() {
		return viserModif;
	}

	public void setViserModif(boolean viserModif) {
		this.viserModif = viserModif;
	}

	public boolean isApprouverVisu() {
		return approuverVisu;
	}

	public void setApprouverVisu(boolean approuverVisu) {
		this.approuverVisu = approuverVisu;
	}

	public boolean isApprouverModif() {
		return approuverModif;
	}

	public void setApprouverModif(boolean approuverModif) {
		this.approuverModif = approuverModif;
	}

	public boolean isAnnuler() {
		return annuler;
	}

	public void setAnnuler(boolean annuler) {
		this.annuler = annuler;
	}

	public boolean isVisuSolde() {
		return visuSolde;
	}

	public void setVisuSolde(boolean visuSolde) {
		this.visuSolde = visuSolde;
	}

	public boolean isMajSolde() {
		return majSolde;
	}

	public void setMajSolde(boolean majSolde) {
		this.majSolde = majSolde;
	}

	public boolean isDroitAcces() {
		return droitAcces;
	}

	public void setDroitAcces(boolean droitAcces) {
		this.droitAcces = droitAcces;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}
	
	
}
