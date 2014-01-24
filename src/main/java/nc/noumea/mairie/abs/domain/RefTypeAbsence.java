package nc.noumea.mairie.abs.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;

@Entity
@Table(name = "ABS_REF_TYPE_ABSENCE")
@PersistenceUnit(unitName = "absPersistenceUnit")
public class RefTypeAbsence {

	@Id
	@Column(name = "ID_REF_TYPE_ABSENCE")
	private Integer idRefTypeAbsence;

	@Column(name = "LABEL", columnDefinition = "NVARCHAR2")
	private String label;

	public Integer getIdRefTypeAbsence() {
		return idRefTypeAbsence;
	}

	public void setIdRefTypeAbsence(Integer idRefTypeAbsence) {
		this.idRefTypeAbsence = idRefTypeAbsence;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
}
