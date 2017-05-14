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
@Table(name = "ABS_REF_SIEGE_LESION")
@PersistenceUnit(unitName = "absPersistenceUnit")
@NamedQueries({
	@NamedQuery(name = "getRefTypeSiegeLesionByIdType", query = "select d from RefTypeSiegeLesion d where d.idRefSiegeLesion = :idRefSiegeLesion"),
	@NamedQuery(name = "getAllRefTypeSiegeLesion", query = "select d from RefTypeSiegeLesion d order by d.libelle ")
})
public class RefTypeSiegeLesion extends RefTypeGenerique {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID_REF_SIEGE_LESION")
	private Integer idRefSiegeLesion;
	
	public Integer getIdRefSiegeLesion() {
		return idRefSiegeLesion;
	}

	public void setIdRefSiegeLesion(Integer idRefSiegeLesion) {
		this.idRefSiegeLesion = idRefSiegeLesion;
	}
}
