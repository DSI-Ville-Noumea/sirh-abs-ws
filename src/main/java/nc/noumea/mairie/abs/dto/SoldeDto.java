package nc.noumea.mairie.abs.dto;

import flexjson.JSONSerializer;

public class SoldeDto implements IJSONSerialize {

	private Double soldeCongeAnnee;
	private Double soldeCongeAnneePrec;
	private Double soldeRecup;
	private Double soldeReposCompAnnee;
	private Double soldeReposCompAnneePrec;

	@Override
	public String serializeInJSON() {
		return new JSONSerializer().exclude("*.class").serialize(this);
	}

	public Double getSoldeCongeAnnee() {
		return soldeCongeAnnee;
	}

	public void setSoldeCongeAnnee(Double soldeCongeAnnee) {
		this.soldeCongeAnnee = soldeCongeAnnee;
	}

	public Double getSoldeCongeAnneePrec() {
		return soldeCongeAnneePrec;
	}

	public void setSoldeCongeAnneePrec(Double soldeCongeAnneePrec) {
		this.soldeCongeAnneePrec = soldeCongeAnneePrec;
	}

	public Double getSoldeRecup() {
		return soldeRecup;
	}

	public void setSoldeRecup(Double soldeRecup) {
		this.soldeRecup = soldeRecup;
	}

	public Double getSoldeReposCompAnnee() {
		return soldeReposCompAnnee;
	}

	public void setSoldeReposCompAnnee(Double soldeReposCompAnnee) {
		this.soldeReposCompAnnee = soldeReposCompAnnee;
	}

	public Double getSoldeReposCompAnneePrec() {
		return soldeReposCompAnneePrec;
	}

	public void setSoldeReposCompAnneePrec(Double soldeReposCompAnneePrec) {
		this.soldeReposCompAnneePrec = soldeReposCompAnneePrec;
	}

}
