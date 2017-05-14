package nc.noumea.mairie.abs.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "ABS_REF_DROITS_MALADIES")
@PersistenceUnit(unitName = "absPersistenceUnit")
public class RefDroitsMaladies {

	@Id
	@Column(name = "ID_REF_DROITS_MALADIES")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idRefDroitsMaladies;
	
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
	@Column(name = "DROITS_PS")
	private Integer nombreJoursPleinSalaire;

	@NotNull
	@Column(name = "DROITS_DS")
	private Integer nombreJoursDemiSalaire;

	@NotNull
	@Column(name = "ANNEE_ANCIENNETE")
	private Integer anneeAnciennete;

	public Integer getIdRefDroitsMaladies() {
		return idRefDroitsMaladies;
	}

	public void setIdRefDroitsMaladies(Integer idRefDroitsMaladies) {
		this.idRefDroitsMaladies = idRefDroitsMaladies;
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

	public Integer getNombreJoursPleinSalaire() {
		return nombreJoursPleinSalaire;
	}

	public void setNombreJoursPleinSalaire(Integer nombreJoursPleinSalaire) {
		this.nombreJoursPleinSalaire = nombreJoursPleinSalaire;
	}

	public Integer getNombreJoursDemiSalaire() {
		return nombreJoursDemiSalaire;
	}

	public void setNombreJoursDemiSalaire(Integer nombreJoursDemiSalaire) {
		this.nombreJoursDemiSalaire = nombreJoursDemiSalaire;
	}

	public Integer getAnneeAnciennete() {
		return anneeAnciennete;
	}

	public void setAnneeAnciennete(Integer anneeAnciennete) {
		this.anneeAnciennete = anneeAnciennete;
	}
	
}
