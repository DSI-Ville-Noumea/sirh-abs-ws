package nc.noumea.mairie.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "SPSORC")
@PersistenceUnit(unitName = "sirhPersistenceUnit")
public class SpSorc {

	@Id
	@Column(name = "NOMATR", columnDefinition = "numeric")
	private Integer nomatr;

	@NotNull
	@Column(name = "SOLDE1", columnDefinition = "decimal")
	private Double soldeAnneeEnCours;

	@NotNull
	@Column(name = "SOLDE2", columnDefinition = "decimal")
	private Double soldeAnneePrec;

	@NotNull
	@Column(name = "NBRCP", columnDefinition = "decimal")
	private Double nombrePris;

	public Integer getNomatr() {
		return nomatr;
	}

	public void setNomatr(Integer nomatr) {
		this.nomatr = nomatr;
	}

	public Double getSoldeAnneeEnCours() {
		return soldeAnneeEnCours;
	}

	public void setSoldeAnneeEnCours(Double soldeAnneeEnCours) {
		this.soldeAnneeEnCours = soldeAnneeEnCours;
	}

	public Double getSoldeAnneePrec() {
		return soldeAnneePrec;
	}

	public void setSoldeAnneePrec(Double soldeAnneePrec) {
		this.soldeAnneePrec = soldeAnneePrec;
	}

	public Double getNombrePris() {
		return nombrePris;
	}

	public void setNombrePris(Double nombrePris) {
		this.nombrePris = nombrePris;
	}

}
