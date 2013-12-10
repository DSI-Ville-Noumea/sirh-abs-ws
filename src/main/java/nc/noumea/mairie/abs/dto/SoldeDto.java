package nc.noumea.mairie.abs.dto;

import flexjson.JSONSerializer;

public class SoldeDto implements IJSONSerialize {

	private Integer soldeRecup;

	@Override
	public String serializeInJSON() {
		return new JSONSerializer().exclude("*.class").serialize(this);
	}

	public Integer getSoldeRecup() {
		return soldeRecup;
	}

	public void setSoldeRecup(Integer soldeRecup) {
		this.soldeRecup = soldeRecup;
	}
}
