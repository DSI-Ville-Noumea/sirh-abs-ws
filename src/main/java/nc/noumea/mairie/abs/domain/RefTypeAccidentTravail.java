package nc.noumea.mairie.abs.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;

@Entity
@Table(name = "ABS_REF_ACCIDENT_TRAVAIL")
@PersistenceUnit(unitName = "absPersistenceUnit")
@NamedQueries({
	@NamedQuery(name = "getRefTypeAccidentTravailByIdType", query = "select d from RefTypeAccidentTravail d where d.idRefAccidentTravail = :idRefAccidentTravail"),
	@NamedQuery(name = "getAllRefTypeAccidentTravail", query = "select d from RefTypeAccidentTravail d order by d.libelle ")
})
public class RefTypeAccidentTravail extends RefTypeGenerique {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID_REF_ACCIDENT_TRAVAIL")
	private Integer idRefAccidentTravail;
	

	public Integer getIdRefAccidentTravail() {
		return idRefAccidentTravail;
	}

	public void setIdRefAccidentTravail(Integer idRefAccidentTravail) {
		this.idRefAccidentTravail = idRefAccidentTravail;
	}
}
