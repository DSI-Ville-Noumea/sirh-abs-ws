package nc.noumea.mairie.abs.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

@Embeddable
public class RefAlimCongeAnnuelId implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Column(name = "ID_REF_TYPE_SAISI_CONGE_ANNUEL")
	private Integer idRefTypeSaisiCongeAnnuel;

	@NotNull
	@Column(name = "ANNEE", nullable = false)
	private Integer annee;

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	public Integer getIdRefTypeSaisiCongeAnnuel() {
		return idRefTypeSaisiCongeAnnuel;
	}

	public void setIdRefTypeSaisiCongeAnnuel(Integer idRefTypeSaisiCongeAnnuel) {
		this.idRefTypeSaisiCongeAnnuel = idRefTypeSaisiCongeAnnuel;
	}

	public Integer getAnnee() {
		return annee;
	}

	public void setAnnee(Integer annee) {
		this.annee = annee;
	}

}