package nc.noumea.mairie.abs.dto;

import java.util.Date;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeRecup;
import nc.noumea.mairie.abs.domain.DemandeReposComp;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.sirh.domain.Agent;

public class DemandeDto {

	private Integer idDemande;
	
	private Integer idAgent;
	private String nomAgent;
	private String prenomAgent;
	private String service;
	private String codeService;
	
	private Integer idTypeDemande;
	private Date dateDebut;
	private Integer duree;
	private Integer idRefEtat;
	private Date dateDemande;

	// permet d'afficher ou non les icones correspondants
	private boolean isAffichageBoutonModifier;
	private boolean isAffichageBoutonSupprimer;
	private boolean isAffichageBoutonImprimer;
	private boolean isAffichageBoutonAnnuler;
	private boolean isAffichageVisa;
	private boolean isAffichageApprobation;
	// permet de viser ou approuver
	private boolean isModifierVisa;
	private boolean isModifierApprobation;
	private String motifViseur;
	// valeur du visa et approbation de la demande
	private Boolean isValeurVisa = null;
	private Boolean isValeurApprobation = null;
	private Integer idMotifRefus;

	public DemandeDto() {
	}
	
	public DemandeDto(Demande d, AgentWithServiceDto agent){
		this(d);
		this.nomAgent = agent.getNom();
		this.prenomAgent = agent.getPrenom();
		this.codeService = agent.getCodeService();
		this.service = agent.getService();
	}
	
	public DemandeDto(Demande d, Agent agent) {
		this(d);
		this.nomAgent = agent.getDisplayNom();
		this.prenomAgent = agent.getDisplayPrenom();
	}

	public DemandeDto(Demande d) {
		super();
		this.idDemande = d.getIdDemande();
		this.idAgent = d.getIdAgent();
		this.idTypeDemande = d.getType().getIdRefTypeAbsence();
		this.dateDebut = d.getDateDebut();
		this.idRefEtat = d.getLatestEtatDemande().getEtat().getCodeEtat();
		this.dateDemande = d.getLatestEtatDemande().getDate();
		this.motifViseur = d.getLatestEtatDemande().getMotifViseur();
		this.idMotifRefus = d.getLatestEtatDemande().getIdMotifRefus();

		for (EtatDemande etat : d.getEtatsDemande()) {
			if (this.isValeurVisa == null && etat.getEtat().equals(RefEtatEnum.VISEE_FAVORABLE)) {
				this.isValeurVisa = Boolean.TRUE;
				continue;
			}
			if (this.isValeurVisa == null && etat.getEtat().equals(RefEtatEnum.VISEE_DEFAVORABLE)) {
				this.isValeurVisa = Boolean.FALSE;
				continue;
			}
			if (this.isValeurApprobation == null && etat.getEtat().equals(RefEtatEnum.APPROUVEE)) {
				this.isValeurApprobation = Boolean.TRUE;
				continue;
			}
			if (this.isValeurApprobation == null && etat.getEtat().equals(RefEtatEnum.REFUSEE)) {
				this.isValeurApprobation = Boolean.FALSE;
				continue;
			}
		}

		switch (RefTypeAbsenceEnum.getRefTypeAbsenceEnum(idTypeDemande)) {
			case CONGE_ANNUEL:
				// TODO
				break;
			case REPOS_COMP:
				Integer dureeAnnee = ((DemandeReposComp) d).getDuree() == null ? 0 : ((DemandeReposComp) d).getDuree();
				Integer dureeAnneePrec = ((DemandeReposComp) d).getDureeAnneeN1() == null ? 0 : ((DemandeReposComp) d)
						.getDureeAnneeN1();
				this.duree = dureeAnnee + dureeAnneePrec;
				break;
			case RECUP:
				this.duree = ((DemandeRecup) d).getDuree();
				break;
			case ASA:
				// TODO
				break;
			case AUTRES:
				// TODO
				break;
			case MALADIES:
				// TODO
				break;
		}
	}

	public Integer getIdDemande() {
		return idDemande;
	}

	public void setIdDemande(Integer idDemande) {
		this.idDemande = idDemande;
	}

	public Integer getIdAgent() {
		return idAgent;
	}

	public void setIdAgent(Integer idAgent) {
		this.idAgent = idAgent;
	}

	public String getNomAgent() {
		return nomAgent;
	}

	public void setNomAgent(String nomAgent) {
		this.nomAgent = nomAgent;
	}

	public String getPrenomAgent() {
		return prenomAgent;
	}

	public void setPrenomAgent(String prenomAgent) {
		this.prenomAgent = prenomAgent;
	}

	public Integer getIdTypeDemande() {
		return idTypeDemande;
	}

	public void setIdTypeDemande(Integer idTypeDemande) {
		this.idTypeDemande = idTypeDemande;
	}

	public Date getDateDebut() {
		return dateDebut;
	}

	public void setDateDebut(Date dateDebut) {
		this.dateDebut = dateDebut;
	}

	public Integer getDuree() {
		return duree;
	}

	public void setDuree(Integer duree) {
		this.duree = duree;
	}

	public Integer getIdRefEtat() {
		return idRefEtat;
	}

	public void setIdRefEtat(Integer idRefEtat) {
		this.idRefEtat = idRefEtat;
	}

	@Override
	public boolean equals(Object obj) {
		return idDemande.equals(((DemandeDto) obj).getIdDemande());
	}

	public Date getDateDemande() {
		return dateDemande;
	}

	public void setDateDemande(Date dateDemande) {
		this.dateDemande = dateDemande;
	}

	public boolean isAffichageBoutonModifier() {
		return isAffichageBoutonModifier;
	}

	public void setAffichageBoutonModifier(boolean isAffichageBoutonModifier) {
		this.isAffichageBoutonModifier = isAffichageBoutonModifier;
	}

	public boolean isAffichageBoutonSupprimer() {
		return isAffichageBoutonSupprimer;
	}

	public void setAffichageBoutonSupprimer(boolean isAffichageBoutonSupprimer) {
		this.isAffichageBoutonSupprimer = isAffichageBoutonSupprimer;
	}

	public boolean isAffichageBoutonImprimer() {
		return isAffichageBoutonImprimer;
	}

	public void setAffichageBoutonImprimer(boolean isAffichageBoutonImprimer) {
		this.isAffichageBoutonImprimer = isAffichageBoutonImprimer;
	}

	public boolean isAffichageVisa() {
		return isAffichageVisa;
	}

	public void setAffichageVisa(boolean isAffichageVisa) {
		this.isAffichageVisa = isAffichageVisa;
	}

	public boolean isAffichageApprobation() {
		return isAffichageApprobation;
	}

	public void setAffichageApprobation(boolean isAffichageApprobation) {
		this.isAffichageApprobation = isAffichageApprobation;
	}

	public boolean isAffichageBoutonAnnuler() {
		return isAffichageBoutonAnnuler;
	}

	public void setAffichageBoutonAnnuler(boolean isAffichageBoutonAnnuler) {
		this.isAffichageBoutonAnnuler = isAffichageBoutonAnnuler;
	}

	public Boolean getValeurVisa() {
		return isValeurVisa;
	}

	public void setValeurVisa(Boolean isValeurVisa) {
		this.isValeurVisa = isValeurVisa;
	}

	public Boolean getValeurApprobation() {
		return isValeurApprobation;
	}

	public void setValeurApprobation(Boolean isValeurApprobation) {
		this.isValeurApprobation = isValeurApprobation;
	}

	public boolean isModifierVisa() {
		return isModifierVisa;
	}

	public void setModifierVisa(boolean isModifierVisa) {
		this.isModifierVisa = isModifierVisa;
	}

	public boolean isModifierApprobation() {
		return isModifierApprobation;
	}

	public void setModifierApprobation(boolean isModifierApprobation) {
		this.isModifierApprobation = isModifierApprobation;
	}

	public String getMotifViseur() {
		return motifViseur;
	}

	public void setMotifViseur(String motifViseur) {
		this.motifViseur = motifViseur;
	}

	public Integer getIdMotifRefus() {
		return idMotifRefus;
	}

	public void setIdMotifRefus(Integer idMotifRefus) {
		this.idMotifRefus = idMotifRefus;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getCodeService() {
		return codeService;
	}

	public void setCodeService(String codeService) {
		this.codeService = codeService;
	}

}
