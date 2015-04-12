package nc.noumea.mairie.abs.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;

@Entity
@Table(name = "ABS_REF_GROUPE_ABSENCE")
@PersistenceUnit(unitName = "absPersistenceUnit")
@NamedQueries({
	@NamedQuery(name = "getRefGroupeAbsenceById", query = "select d from RefGroupeAbsence d where d.idRefGroupeAbsence = :idRefGroupeAbsence"),
	@NamedQuery(name = "getAllRefGroupeAbsence", query = "select d from RefGroupeAbsence d order by d.code ")
})
public class RefGroupeAbsence {

	@Id
	@Column(name = "ID_REF_GROUPE_ABSENCE")
	private Integer idRefGroupeAbsence;

	@Column(name = "CODE", columnDefinition = "NVARCHAR2")
	private String code;

	@Column(name = "LIBELLE", columnDefinition = "NVARCHAR2")
	private String libelle;

	@OneToMany(mappedBy = "groupe", fetch = FetchType.EAGER, orphanRemoval = true)
	@OrderBy("label")
	private List<RefTypeAbsence> listeTypeAbsence = new ArrayList<RefTypeAbsence>();

	public Integer getIdRefGroupeAbsence() {
		return idRefGroupeAbsence;
	}

	public void setIdRefGroupeAbsence(Integer idRefGroupeAbsence) {
		this.idRefGroupeAbsence = idRefGroupeAbsence;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getLibelle() {
		return libelle;
	}

	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}

	public List<RefTypeAbsence> getListeTypeAbsence() {
		return listeTypeAbsence;
	}

	public void setListeTypeAbsence(List<RefTypeAbsence> listeTypeAbsence) {
		this.listeTypeAbsence = listeTypeAbsence;
	}
	
	
}
