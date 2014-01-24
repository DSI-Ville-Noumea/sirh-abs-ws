package nc.noumea.mairie.abs.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;

@Entity
@Table(name = "ABS_REF_ETAT") 
@PersistenceUnit(unitName = "absPersistenceUnit")
public class RefEtat {

	@Id
	@Column(name = "ID_REF_ETAT")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idRefEtat;

	@Column(name = "LABEL", columnDefinition = "NVARCHAR2")
	private String label;

	@Override
	public boolean equals(Object obj) {
		return idRefEtat.equals(((RefEtat) obj).getIdRefEtat());
	}

	public Integer getIdRefEtat() {
		return idRefEtat;
	}

	public void setIdRefEtat(Integer idRefEtat) {
		this.idRefEtat = idRefEtat;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	
}
