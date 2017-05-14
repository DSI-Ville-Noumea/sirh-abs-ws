package nc.noumea.mairie.abs.domain;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "ABS_REF_TYPE_ABSENCE")
@PersistenceUnit(unitName = "absPersistenceUnit")
public class RefTypeAbsence {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID_REF_TYPE_ABSENCE")
	private Integer idRefTypeAbsence;

	@Column(name = "LABEL", columnDefinition = "NVARCHAR2")
	private String label;

	@OneToOne(fetch = FetchType.EAGER, optional = true)
	@JoinColumn(name = "ID_REF_GROUPE_ABSENCE")
	private RefGroupeAbsence groupe;

	@OneToOne(fetch = FetchType.EAGER, optional = true, cascade = CascadeType.ALL)
	@JoinColumn(name = "ID_REF_TYPE_ABSENCE")
	private RefTypeSaisi typeSaisi;

	@OneToOne(fetch = FetchType.EAGER, optional = true, cascade = CascadeType.ALL)
	@JoinColumn(name = "ID_REF_TYPE_ABSENCE")
	private RefTypeSaisiCongeAnnuel	typeSaisiCongeAnnuel;

	@NotNull
	@Column(name = "IS_ACTIF", nullable = false)
	@Type(type = "boolean")
	private boolean					actif;

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

	public RefGroupeAbsence getGroupe() {
		return groupe;
	}

	public void setGroupe(RefGroupeAbsence groupe) {
		this.groupe = groupe;
	}

	public RefTypeSaisi getTypeSaisi() {
		return typeSaisi;
	}

	public void setTypeSaisi(RefTypeSaisi typeSaisi) {
		this.typeSaisi = typeSaisi;
	}

	public RefTypeSaisiCongeAnnuel getTypeSaisiCongeAnnuel() {
		return typeSaisiCongeAnnuel;
	}

	public void setTypeSaisiCongeAnnuel(RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel) {
		this.typeSaisiCongeAnnuel = typeSaisiCongeAnnuel;
	}

	public boolean isActif() {
		return actif;
	}

	public void setActif(boolean actif) {
		this.actif = actif;
	}

}
