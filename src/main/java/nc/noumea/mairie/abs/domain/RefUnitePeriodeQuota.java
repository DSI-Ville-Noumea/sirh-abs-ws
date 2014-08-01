package nc.noumea.mairie.abs.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "ABS_REF_UNITE_PERIODE_QUOTA")
@PersistenceUnit(unitName = "absPersistenceUnit")
@NamedQueries({
	@NamedQuery(name = "findAllRefUnitePeriodeQuota", query = "from RefUnitePeriodeQuota d ")
})
public class RefUnitePeriodeQuota {
	
	@Id
	@Column(name = "ID_REF_UNITE_PERIODE_QUOTA")
	private Integer idRefUnitePeriodeQuota;
	
	@Column(name = "VALEUR")
	private Integer valeur;
	
	@NotNull
	@Column(name = "UNITE")
	private String unite;
	
	/**
	 * defini si la periode est par exemple sur :
	 * - 12 mois glissant
	 * - ou sur une année civile
	 */
	@NotNull
	@Column(name = "GLISSANT", nullable = false)
	@Type(type = "boolean")
	private boolean glissant;

	public Integer getIdRefUnitePeriodeQuota() {
		return idRefUnitePeriodeQuota;
	}

	public void setIdRefUnitePeriodeQuota(Integer idRefUnitePeriodeQuota) {
		this.idRefUnitePeriodeQuota = idRefUnitePeriodeQuota;
	}

	public Integer getValeur() {
		return valeur;
	}

	public void setValeur(Integer valeur) {
		this.valeur = valeur;
	}

	public String getUnite() {
		return unite;
	}

	public void setUnite(String unite) {
		this.unite = unite;
	}

	public boolean isGlissant() {
		return glissant;
	}

	public void setGlissant(boolean glissant) {
		this.glissant = glissant;
	}
	
	
}
