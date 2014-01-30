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
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "ABS_MOTIF_COMPTEUR")
@PersistenceUnit(unitName = "absPersistenceUnit")
public class MotifCompteur {

	@Id
	@Column(name = "ID_MOTIF_COMPTEUR")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idMotifCompteur;

	@NotNull
	@Column(name = "LIBELLE")
	private String libelle;

	@ManyToOne()
	@JoinColumn(name = "ID_REF_TYPE_ABSENCE", referencedColumnName = "ID_REF_TYPE_ABSENCE")
	private RefTypeAbsence refTypeAbsence;

	@Version
	@Column(name = "version")
	private Integer version;

	public Integer getIdMotifCompteur() {
		return idMotifCompteur;
	}

	public void setIdMotifCompteur(Integer idMotifCompteur) {
		this.idMotifCompteur = idMotifCompteur;
	}

	public String getLibelle() {
		return libelle;
	}

	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}

	public RefTypeAbsence getRefTypeAbsence() {
		return refTypeAbsence;
	}

	public void setRefTypeAbsence(RefTypeAbsence refTypeAbsence) {
		this.refTypeAbsence = refTypeAbsence;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

}
