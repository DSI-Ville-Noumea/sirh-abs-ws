package nc.noumea.mairie.abs.dto;

import flexjson.JSONSerializer;

public class SoldeDto implements IJSONSerialize {

	private boolean afficheSoldeConge;
	private Double soldeCongeAnnee;
	private Double soldeCongeAnneePrec;
	private boolean afficheSoldeRecup;
	private Double soldeRecup;
	private boolean afficheSoldeReposComp;
	private Double soldeReposCompAnnee;
	private Double soldeReposCompAnneePrec;
	private boolean afficheSoldeAsaA48;
	private Double soldeAsaA48;

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

	public boolean isAfficheSoldeConge() {
		return afficheSoldeConge;
	}

	public void setAfficheSoldeConge(boolean afficheSoldeConge) {
		this.afficheSoldeConge = afficheSoldeConge;
	}

	public boolean isAfficheSoldeRecup() {
		return afficheSoldeRecup;
	}

	public void setAfficheSoldeRecup(boolean afficheSoldeRecup) {
		this.afficheSoldeRecup = afficheSoldeRecup;
	}

	public boolean isAfficheSoldeReposComp() {
		return afficheSoldeReposComp;
	}

	public void setAfficheSoldeReposComp(boolean afficheSoldeReposComp) {
		this.afficheSoldeReposComp = afficheSoldeReposComp;
	}

	public boolean isAfficheSoldeAsaA48() {
		return afficheSoldeAsaA48;
	}

	public void setAfficheSoldeAsaA48(boolean afficheSoldeAsaA48) {
		this.afficheSoldeAsaA48 = afficheSoldeAsaA48;
	}

	public Double getSoldeAsaA48() {
		return soldeAsaA48;
	}

	public void setSoldeAsaA48(Double soldeAsaA48) {
		this.soldeAsaA48 = soldeAsaA48;
	}

}
