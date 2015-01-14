package nc.noumea.mairie.abs.dto;

public class AccessRightsDto {

	private boolean saisie;
	private boolean modification;
	private boolean suppression;
	private boolean impression;
	private boolean viserVisu;
	private boolean viserModif;
	private boolean approuverVisu;
	private boolean approuverModif;
	private boolean annuler;
	private boolean visuSolde;
	private boolean majSolde;
	private boolean droitAcces;
	
	private boolean saisieRepos;

	public boolean isSaisie() {
		return saisie;
	}

	public void setSaisie(boolean saisie) {
		this.saisie = saisie;
	}

	public boolean isModification() {
		return modification;
	}

	public void setModification(boolean modification) {
		this.modification = modification;
	}

	public boolean isSuppression() {
		return suppression;
	}

	public void setSuppression(boolean suppression) {
		this.suppression = suppression;
	}

	public boolean isImpression() {
		return impression;
	}

	public void setImpression(boolean impression) {
		this.impression = impression;
	}

	public boolean isViserVisu() {
		return viserVisu;
	}

	public void setViserVisu(boolean viserVisu) {
		this.viserVisu = viserVisu;
	}

	public boolean isViserModif() {
		return viserModif;
	}

	public void setViserModif(boolean viserModif) {
		this.viserModif = viserModif;
	}

	public boolean isApprouverVisu() {
		return approuverVisu;
	}

	public void setApprouverVisu(boolean approuverVisu) {
		this.approuverVisu = approuverVisu;
	}

	public boolean isApprouverModif() {
		return approuverModif;
	}

	public void setApprouverModif(boolean approuverModif) {
		this.approuverModif = approuverModif;
	}

	public boolean isAnnuler() {
		return annuler;
	}

	public void setAnnuler(boolean annuler) {
		this.annuler = annuler;
	}

	public boolean isVisuSolde() {
		return visuSolde;
	}

	public void setVisuSolde(boolean visuSolde) {
		this.visuSolde = visuSolde;
	}

	public boolean isMajSolde() {
		return majSolde;
	}

	public void setMajSolde(boolean majSolde) {
		this.majSolde = majSolde;
	}

	public boolean isDroitAcces() {
		return droitAcces;
	}

	public void setDroitAcces(boolean droitAcces) {
		this.droitAcces = droitAcces;
	}

	public boolean isSaisieRepos() {
		return saisieRepos;
	}

	public void setSaisieRepos(boolean saisieRepos) {
		this.saisieRepos = saisieRepos;
	}
	
}
