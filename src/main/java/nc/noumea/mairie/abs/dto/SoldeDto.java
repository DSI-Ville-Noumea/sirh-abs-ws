package nc.noumea.mairie.abs.dto;

import java.util.ArrayList;
import java.util.List;

public class SoldeDto {

	private boolean afficheSoldeConge;
	private boolean samediOffert;
	private Double soldeCongeAnnee;
	private Double soldeCongeAnneePrec;
	private boolean afficheSoldeRecup;
	private Double soldeRecup;
	private boolean afficheSoldeReposComp;
	private Double soldeReposCompAnnee;
	private Double soldeReposCompAnneePrec;
	private boolean afficheSoldeAsaA48;
	private Double soldeAsaA48;
	private boolean afficheSoldeAsaA54;
	private Double soldeAsaA54;
	private boolean afficheSoldeAsaA55;
	private Double soldeAsaA55;
	private List<SoldeMonthDto> listeSoldeAsaA55;
	private boolean afficheSoldeAsaA52;
	private Double soldeAsaA52;
	private OrganisationSyndicaleDto organisationA52;
	private List<SoldeMonthDto> listeSoldeAsaA52;

	private boolean afficheSoldeCongesExcep;
	private List<SoldeSpecifiqueDto> listeSoldeCongesExcep = new ArrayList<SoldeSpecifiqueDto>();

	public SoldeDto() {
		listeSoldeAsaA55 = new ArrayList<SoldeMonthDto>();
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

	public boolean isAfficheSoldeAsaA54() {
		return afficheSoldeAsaA54;
	}

	public void setAfficheSoldeAsaA54(boolean afficheSoldeAsaA54) {
		this.afficheSoldeAsaA54 = afficheSoldeAsaA54;
	}

	public Double getSoldeAsaA54() {
		return soldeAsaA54;
	}

	public void setSoldeAsaA54(Double soldeAsaA54) {
		this.soldeAsaA54 = soldeAsaA54;
	}

	public boolean isAfficheSoldeAsaA55() {
		return afficheSoldeAsaA55;
	}

	public void setAfficheSoldeAsaA55(boolean afficheSoldeAsaA55) {
		this.afficheSoldeAsaA55 = afficheSoldeAsaA55;
	}

	public List<SoldeMonthDto> getListeSoldeAsaA55() {
		return listeSoldeAsaA55;
	}

	public void setListeSoldeAsaA55(List<SoldeMonthDto> listeSoldeAsaA55) {
		this.listeSoldeAsaA55 = listeSoldeAsaA55;
	}

	public Double getSoldeAsaA55() {
		return soldeAsaA55;
	}

	public void setSoldeAsaA55(Double soldeAsaA55) {
		this.soldeAsaA55 = soldeAsaA55;
	}

	public boolean isAfficheSoldeCongesExcep() {
		return afficheSoldeCongesExcep;
	}

	public void setAfficheSoldeCongesExcep(boolean afficheSoldeCongesExcep) {
		this.afficheSoldeCongesExcep = afficheSoldeCongesExcep;
	}

	public List<SoldeSpecifiqueDto> getListeSoldeCongesExcep() {
		return listeSoldeCongesExcep;
	}

	public void setListeSoldeCongesExcep(List<SoldeSpecifiqueDto> listeSoldeCongesExcep) {
		this.listeSoldeCongesExcep = listeSoldeCongesExcep;
	}

	public boolean isAfficheSoldeAsaA52() {
		return afficheSoldeAsaA52;
	}

	public void setAfficheSoldeAsaA52(boolean afficheSoldeAsaA52) {
		this.afficheSoldeAsaA52 = afficheSoldeAsaA52;
	}

	public Double getSoldeAsaA52() {
		return soldeAsaA52;
	}

	public void setSoldeAsaA52(Double soldeAsaA52) {
		this.soldeAsaA52 = soldeAsaA52;
	}

	public List<SoldeMonthDto> getListeSoldeAsaA52() {
		return listeSoldeAsaA52;
	}

	public void setListeSoldeAsaA52(List<SoldeMonthDto> listeSoldeAsaA52) {
		this.listeSoldeAsaA52 = listeSoldeAsaA52;
	}

	public OrganisationSyndicaleDto getOrganisationA52() {
		return organisationA52;
	}

	public void setOrganisationA52(OrganisationSyndicaleDto organisationA52) {
		this.organisationA52 = organisationA52;
	}

	public boolean isSamediOffert() {
		return samediOffert;
	}

	public void setSamediOffert(boolean samediOffert) {
		this.samediOffert = samediOffert;
	}

}
