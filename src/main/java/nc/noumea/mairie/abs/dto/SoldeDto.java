package nc.noumea.mairie.abs.dto;

import flexjson.JSONSerializer;

public class SoldeDto implements IJSONSerialize {

	private Double solde;

	@Override
	public String serializeInJSON() {
		return new JSONSerializer().exclude("*.class").serialize(this);
	}

	public Double getSolde() {
		return solde;
	}

	public void setSolde(Double solde) {
		this.solde = solde;
	}

}
