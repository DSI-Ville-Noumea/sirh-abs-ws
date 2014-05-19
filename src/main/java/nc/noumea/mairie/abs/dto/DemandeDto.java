package nc.noumea.mairie.abs.dto;

import java.util.Date;

import nc.noumea.mairie.abs.asa.domain.DemandeAsa;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.recup.domain.DemandeRecup;
import nc.noumea.mairie.abs.reposComp.domain.DemandeReposComp;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

public class DemandeDto {

	private AgentWithServiceDto agentWithServiceDto;

	private Integer idDemande;
	private Integer idTypeDemande;
	@JsonSerialize(using = JsonDateSerializer.class)
	@JsonDeserialize(using = JsonDateDeserializer.class)
	private Date dateDemande;
	@JsonSerialize(using = JsonDateSerializer.class)
	@JsonDeserialize(using = JsonDateDeserializer.class)
	private Date dateDebut;
	private boolean isDateDebutAM;
	private boolean isDateDebutPM;
	@JsonSerialize(using = JsonDateSerializer.class)
	@JsonDeserialize(using = JsonDateDeserializer.class)
	private Date dateFin;
	private boolean isDateFinAM;
	private boolean isDateFinPM;
	private Double duree;

	private Integer idRefEtat;
	@JsonSerialize(using = JsonDateSerializer.class)
	@JsonDeserialize(using = JsonDateDeserializer.class)
	private Date dateSaisie;
	private String motif;

	// permet d'afficher ou non les icones correspondants
	private boolean isAffichageBoutonModifier;
	private boolean isAffichageBoutonSupprimer;
	private boolean isAffichageBoutonImprimer;
	private boolean isAffichageBoutonAnnuler;
	private boolean isAffichageVisa;
	private boolean isAffichageApprobation;
	private boolean isAffichageValidation;
	private boolean isAffichageEnAttente;
	private boolean isAffichageBoutonDupliquer;
	// permet de viser ou approuver
	private boolean isModifierVisa;
	private boolean isModifierApprobation;
	private boolean isModifierValidation;
	// valeur du visa et approbation de la demande
	private Boolean isValeurVisa = null;
	private Boolean isValeurApprobation = null;
	private Boolean isValeurValidation = null;
	// depassement de droits
	private boolean isDepassementCompteur;

	private OrganisationSyndicaleDto organisationSyndicale;

	public DemandeDto() {
	}

	public DemandeDto(Demande d, AgentWithServiceDto agentWithServiceDto) {
		this(d);
		this.agentWithServiceDto = agentWithServiceDto;
	}

	public DemandeDto(Demande d) {
		super();
		AgentWithServiceDto agentDto = new AgentWithServiceDto();
		agentDto.setIdAgent(d.getIdAgent());
		this.agentWithServiceDto = agentDto;
		this.idDemande = d.getIdDemande();
		this.idTypeDemande = d.getType().getIdRefTypeAbsence();
		this.dateDebut = d.getDateDebut();
		this.dateFin = d.getDateFin();
		this.dateDemande = d.getLatestEtatDemande().getDate();
		this.motif = d.getLatestEtatDemande().getMotif();

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
			if (this.isValeurValidation == null && etat.getEtat().equals(RefEtatEnum.VALIDEE)) {
				this.isValeurValidation = Boolean.TRUE;
				continue;
			}
			if (this.isValeurValidation == null && etat.getEtat().equals(RefEtatEnum.REJETE)) {
				this.isValeurValidation = Boolean.FALSE;
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
				this.duree = (double) (dureeAnnee + dureeAnneePrec);
				break;
			case RECUP:
				this.duree = (double) (((DemandeRecup) d).getDuree());
				break;
			case ASA_A48:
			case ASA_A52:
			case ASA_A53:
			case ASA_A54:
			case ASA_A55:
			case ASA_A49:
			case ASA_A50:
				this.duree = ((DemandeAsa) d).getDuree();
				this.isDateDebutAM = ((DemandeAsa) d).isDateDebutAM();
				this.isDateDebutPM = ((DemandeAsa) d).isDateDebutPM();
				this.isDateFinAM = ((DemandeAsa) d).isDateFinAM();
				this.isDateFinPM = ((DemandeAsa) d).isDateFinPM();
				if (null != ((DemandeAsa) d).getOrganisationSyndicale())
					this.organisationSyndicale = new OrganisationSyndicaleDto(
							((DemandeAsa) d).getOrganisationSyndicale());
				break;
			case AUTRES:
				// TODO
				break;
			case MALADIES:
				// TODO
				break;
		}
	}

	public void updateEtat(EtatDemande etat) {
		idRefEtat = etat.getEtat().getCodeEtat();
		dateSaisie = etat.getDate();
		motif = etat.getMotif();
	}

	public Integer getIdDemande() {
		return idDemande;
	}

	public void setIdDemande(Integer idDemande) {
		this.idDemande = idDemande;
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

	public Double getDuree() {
		return duree;
	}

	public void setDuree(Double duree) {
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

	public String getMotif() {
		return motif;
	}

	public void setMotif(String motif) {
		this.motif = motif;
	}

	public Boolean getIsValeurVisa() {
		return isValeurVisa;
	}

	public void setIsValeurVisa(Boolean isValeurVisa) {
		this.isValeurVisa = isValeurVisa;
	}

	public Boolean getIsValeurApprobation() {
		return isValeurApprobation;
	}

	public void setIsValeurApprobation(Boolean isValeurApprobation) {
		this.isValeurApprobation = isValeurApprobation;
	}

	public Date getDateFin() {
		return dateFin;
	}

	public void setDateFin(Date dateFin) {
		this.dateFin = dateFin;
	}

	public boolean isDateDebutAM() {
		return isDateDebutAM;
	}

	public void setDateDebutAM(boolean isDateDebutAM) {
		this.isDateDebutAM = isDateDebutAM;
	}

	public boolean isDateDebutPM() {
		return isDateDebutPM;
	}

	public void setDateDebutPM(boolean isDateDebutPM) {
		this.isDateDebutPM = isDateDebutPM;
	}

	public boolean isDateFinAM() {
		return isDateFinAM;
	}

	public void setDateFinAM(boolean isDateFinAM) {
		this.isDateFinAM = isDateFinAM;
	}

	public boolean isDateFinPM() {
		return isDateFinPM;
	}

	public void setDateFinPM(boolean isDateFinPM) {
		this.isDateFinPM = isDateFinPM;
	}

	public Date getDateSaisie() {
		return dateSaisie;
	}

	public void setDateSaisie(Date dateSaisie) {
		this.dateSaisie = dateSaisie;
	}

	public AgentWithServiceDto getAgentWithServiceDto() {
		return agentWithServiceDto;
	}

	public void setAgentWithServiceDto(AgentWithServiceDto agentWithServiceDto) {
		this.agentWithServiceDto = agentWithServiceDto;
	}

	public boolean isDepassementCompteur() {
		return isDepassementCompteur;
	}

	public void setDepassementCompteur(boolean isDepassementCompteur) {
		this.isDepassementCompteur = isDepassementCompteur;
	}

	public boolean isAffichageValidation() {
		return isAffichageValidation;
	}

	public void setAffichageValidation(boolean isAffichageValidation) {
		this.isAffichageValidation = isAffichageValidation;
	}

	public boolean isModifierValidation() {
		return isModifierValidation;
	}

	public void setModifierValidation(boolean isModifierValidation) {
		this.isModifierValidation = isModifierValidation;
	}

	public Boolean getValeurValidation() {
		return isValeurValidation;
	}

	public void setValeurValidation(Boolean isValeurValidation) {
		this.isValeurValidation = isValeurValidation;
	}

	public boolean isAffichageEnAttente() {
		return isAffichageEnAttente;
	}

	public void setAffichageEnAttente(boolean isAffichageEnAttente) {
		this.isAffichageEnAttente = isAffichageEnAttente;
	}

	public boolean isAffichageBoutonDupliquer() {
		return isAffichageBoutonDupliquer;
	}

	public void setAffichageBoutonDupliquer(boolean isAffichageBoutonDupliquer) {
		this.isAffichageBoutonDupliquer = isAffichageBoutonDupliquer;
	}

	public OrganisationSyndicaleDto getOrganisationSyndicale() {
		return organisationSyndicale;
	}

	public void setOrganisationSyndicale(OrganisationSyndicaleDto organisationSyndicale) {
		this.organisationSyndicale = organisationSyndicale;
	}

}
