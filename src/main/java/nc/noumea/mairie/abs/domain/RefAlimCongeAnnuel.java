package nc.noumea.mairie.abs.domain;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "ABS_REF_ALIM_CONGE_ANNUEL")
@PersistenceUnit(unitName = "absPersistenceUnit")
public class RefAlimCongeAnnuel {

	@EmbeddedId
	private RefAlimCongeAnnuelId id;

	@NotNull
	@Column(name = "JANVIER", nullable = false, columnDefinition = "numeric")
	private Double janvier;

	@NotNull
	@Column(name = "FEVRIER", nullable = false, columnDefinition = "numeric")
	private Double fevrier;

	@NotNull
	@Column(name = "MARS", nullable = false, columnDefinition = "numeric")
	private Double mars;

	@NotNull
	@Column(name = "AVRIL", nullable = false, columnDefinition = "numeric")
	private Double avril;

	@NotNull
	@Column(name = "MAI", nullable = false, columnDefinition = "numeric")
	private Double mai;

	@NotNull
	@Column(name = "JUIN", nullable = false, columnDefinition = "numeric")
	private Double juin;

	@NotNull
	@Column(name = "JUILLET", nullable = false, columnDefinition = "numeric")
	private Double juillet;

	@NotNull
	@Column(name = "AOUT", nullable = false, columnDefinition = "numeric")
	private Double aout;

	@NotNull
	@Column(name = "SEPTEMBRE", nullable = false, columnDefinition = "numeric")
	private Double septembre;

	@NotNull
	@Column(name = "OCTOBRE", nullable = false, columnDefinition = "numeric")
	private Double octobre;

	@NotNull
	@Column(name = "NOVEMBRE", nullable = false, columnDefinition = "numeric")
	private Double novembre;

	@NotNull
	@Column(name = "DECEMBRE", nullable = false, columnDefinition = "numeric")
	private Double decembre;

	public Double getJanvier() {
		return janvier;
	}

	public void setJanvier(Double janvier) {
		this.janvier = janvier;
	}

	public Double getFevrier() {
		return fevrier;
	}

	public void setFevrier(Double fevrier) {
		this.fevrier = fevrier;
	}

	public Double getMars() {
		return mars;
	}

	public void setMars(Double mars) {
		this.mars = mars;
	}

	public Double getAvril() {
		return avril;
	}

	public void setAvril(Double avril) {
		this.avril = avril;
	}

	public Double getMai() {
		return mai;
	}

	public void setMai(Double mai) {
		this.mai = mai;
	}

	public Double getJuin() {
		return juin;
	}

	public void setJuin(Double juin) {
		this.juin = juin;
	}

	public Double getJuillet() {
		return juillet;
	}

	public void setJuillet(Double juillet) {
		this.juillet = juillet;
	}

	public Double getAout() {
		return aout;
	}

	public void setAout(Double aout) {
		this.aout = aout;
	}

	public Double getSeptembre() {
		return septembre;
	}

	public void setSeptembre(Double septembre) {
		this.septembre = septembre;
	}

	public Double getOctobre() {
		return octobre;
	}

	public void setOctobre(Double octobre) {
		this.octobre = octobre;
	}

	public Double getNovembre() {
		return novembre;
	}

	public void setNovembre(Double novembre) {
		this.novembre = novembre;
	}

	public Double getDecembre() {
		return decembre;
	}

	public void setDecembre(Double decembre) {
		this.decembre = decembre;
	}

	public RefAlimCongeAnnuelId getId() {
		return id;
	}

	public void setId(RefAlimCongeAnnuelId id) {
		this.id = id;
	}

}
