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
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "ABS_REF_MALADIE_PRO")
@PersistenceUnit(unitName = "absPersistenceUnit")
@NamedQueries({
	@NamedQuery(name = "getRefTypeMaladieProByIdType", query = "select d from RefTypeMaladiePro d where d.idRefMaladiePro = :idRefMaladiePro"),
	@NamedQuery(name = "getAllRefTypeMaladiePro", query = "select d from RefTypeMaladiePro d order by d.libelle ")
})
public class RefTypeMaladiePro extends RefTypeGenerique {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID_REF_MALADIE_PRO")
	private Integer idRefMaladiePro;

	@NotNull
	@Column(name = "CODE", columnDefinition = "NVARCHAR2")
	private String code;
	

	public Integer getIdRefMaladiePro() {
		return idRefMaladiePro;
	}

	public void setIdRefMaladiePro(Integer idRefMaladiePro) {
		this.idRefMaladiePro = idRefMaladiePro;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
}
