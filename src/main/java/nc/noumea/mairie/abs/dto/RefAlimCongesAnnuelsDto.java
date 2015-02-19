package nc.noumea.mairie.abs.dto;

import nc.noumea.mairie.abs.domain.RefAlimCongeAnnuel;
import flexjson.JSONSerializer;

public class RefAlimCongesAnnuelsDto {

	private Integer idRefTypeSaisiCongeAnnuel;
	private Integer annee;
	private Double janvier;
	private Double fevrier;
	private Double mars;
	private Double avril;
	private Double mai;
	private Double juin;
	private Double juillet;
	private Double aout;
	private Double septembre;
	private Double octobre;
	private Double novembre;
	private Double decembre;

	public RefAlimCongesAnnuelsDto(RefAlimCongeAnnuel ref) {
		this.idRefTypeSaisiCongeAnnuel = ref.getId().getIdRefTypeSaisiCongeAnnuel();
		this.annee = ref.getId().getAnnee();
		this.janvier = ref.getJanvier();
		this.fevrier = ref.getFevrier();
		this.mars = ref.getMars();
		this.avril = ref.getAvril();
		this.mai = ref.getMai();
		this.juin = ref.getJuin();
		this.juillet = ref.getJuillet();
		this.aout = ref.getAout();
		this.septembre = ref.getSeptembre();
		this.octobre = ref.getOctobre();
		this.novembre = ref.getNovembre();
		this.decembre = ref.getDecembre();
	}

	public RefAlimCongesAnnuelsDto() {
		super();
	}

	public Integer getAnnee() {
		return annee;
	}

	public void setAnnee(Integer annee) {
		this.annee = annee;
	}

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

	public String getDtoToString(RefAlimCongesAnnuelsDto dto) {
		String json = new JSONSerializer().exclude("*.class").deepSerialize(dto);
		return json;
	}

	public Integer getIdRefTypeSaisiCongeAnnuel() {
		return idRefTypeSaisiCongeAnnuel;
	}

	public void setIdRefTypeSaisiCongeAnnuel(Integer idRefTypeSaisiCongeAnnuel) {
		this.idRefTypeSaisiCongeAnnuel = idRefTypeSaisiCongeAnnuel;
	}

}
