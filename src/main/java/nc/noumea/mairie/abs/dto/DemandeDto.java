package nc.noumea.mairie.abs.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.CongeAnnuelRestitutionMassiveHisto;
import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeAsa;
import nc.noumea.mairie.abs.domain.DemandeCongesAnnuels;
import nc.noumea.mairie.abs.domain.DemandeCongesExceptionnels;
import nc.noumea.mairie.abs.domain.DemandeMaladies;
import nc.noumea.mairie.abs.domain.DemandeRecup;
import nc.noumea.mairie.abs.domain.DemandeReposComp;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.EtatDemandeAsa;
import nc.noumea.mairie.abs.domain.EtatDemandeCongesAnnuels;
import nc.noumea.mairie.abs.domain.EtatDemandeCongesExceptionnels;
import nc.noumea.mairie.abs.domain.EtatDemandeRecup;
import nc.noumea.mairie.abs.domain.EtatDemandeReposComp;
import nc.noumea.mairie.abs.domain.PieceJointe;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefGroupeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeGroupeAbsenceEnum;
import nc.noumea.mairie.abs.transformer.MSDateTransformer;

import org.joda.time.DateTime;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import flexjson.JSONSerializer;

public class DemandeDto {

	private AgentWithServiceDto agentWithServiceDto;

	private Integer idDemande;
	private Integer idTypeDemande;
	private String libelleTypeDemande;
	private RefGroupeAbsenceDto groupeAbsence;
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
	private boolean forceSaisieManuelleDuree;
	private boolean isSamediOffert;
	@JsonSerialize(using = JsonDateSerializer.class)
	@JsonDeserialize(using = JsonDateDeserializer.class)
	private Date dateReprise;

	private Integer idRefEtat;
	private RefEtatDto etatDto;
	@JsonSerialize(using = JsonDateSerializer.class)
	@JsonDeserialize(using = JsonDateDeserializer.class)
	private Date dateSaisie;
	private String motif;
	private AgentWithServiceDto agentEtat;

	// permet d'afficher ou non les icones correspondants
	private boolean isAffichageBoutonModifier;
	private boolean isAffichageBoutonSupprimer;
	private boolean isAffichageBoutonImprimer;
	private boolean isAffichageBoutonAnnuler;
	private boolean isAffichageValidation;
	private boolean isAffichageBoutonRejeter;
	private boolean isAffichageEnAttente;
	private boolean isAffichageBoutonDupliquer;
	// permet de viser ou approuver
	private boolean isModifierVisa;
	private boolean isModifierApprobation;
	// valeur du visa et approbation de la demande
	private Boolean isValeurVisa = null;
	private Boolean isValeurApprobation = null;
	private Boolean isValeurValidation = null;
	// depassement de droits
	private boolean isDepassementCompteur;
	private boolean isDepassementMultiple;
	private boolean isDepassementITT;

	private OrganisationSyndicaleDto organisationSyndicale;

	private String commentaire;
	private String commentaireDRH;

	private RefTypeSaisiDto typeSaisi;
	private RefTypeSaisiCongeAnnuelDto typeSaisiCongeAnnuel;

	// Pour les soldes des demandes
	private Double totalJoursNew;
	private Double totalJoursOld;
	private Double totalJoursAnneeN1New;
	private Double totalJoursAnneeN1Old;
	private Integer totalMinutesNew;
	private Integer totalMinutesOld;
	private Integer totalMinutesAnneeN1New;
	private Integer totalMinutesAnneeN1Old;
	
	// #15586 restitution massive
	private boolean affichageBoutonHistorique = true;
	
	// Maladies
	private Double nombreITT;
	private String prescripteur;
	private String nomEnfant;
	@JsonSerialize(using = JsonDateSerializer.class)
	@JsonDeserialize(using = JsonDateDeserializer.class)
	private Date dateDeclaration;
	@JsonSerialize(using = JsonDateSerializer.class)
	@JsonDeserialize(using = JsonDateDeserializer.class)
	private Date dateAccidentTravail;
	private boolean sansArretTravail;
	private boolean prolongation;
	private RefTypeDto typeAccidentTravail;
	private RefTypeDto typeSiegeLesion;
	private RefTypeDto typeMaladiePro;
	private DemandeDto accidentTravailReference;
	@JsonSerialize(using = JsonDateSerializer.class)
	@JsonDeserialize(using = JsonDateDeserializer.class)
	private Date dateTransmissionCafat;
	@JsonSerialize(using = JsonDateSerializer.class)
	@JsonDeserialize(using = JsonDateDeserializer.class)
	private Date dateDecisionCafat;
	@JsonSerialize(using = JsonDateSerializer.class)
	@JsonDeserialize(using = JsonDateDeserializer.class)
	private Date dateCommissionAptitude;
	private Double tauxCafat;
	private Boolean avisCommissionAptitude;
	
	// pieces jointes
	private List<PieceJointeDto> piecesJointes;
	
	// provient ou non de HSCT : les PJ doivent etre traitees differemment
	private boolean isFromHSCT;
	
	// #32238 : Controle médical
	private ControleMedicalDto controleMedical;

	public DemandeDto() {
		piecesJointes = new ArrayList<PieceJointeDto>();
	}

	public DemandeDto(Demande d, AgentWithServiceDto agentWithServiceDto, boolean isFromSIRH) {
		this(d, isFromSIRH);
		if (agentWithServiceDto != null) {
			this.agentWithServiceDto = agentWithServiceDto;
		} else {
			this.agentWithServiceDto = new AgentWithServiceDto();
			this.agentWithServiceDto.setIdAgent(d.getIdAgent());
		}
	}
	// bug #30042
	public DemandeDto(Demande d, EtatDemande etat, AgentWithServiceDto agentWithServiceDto, boolean isFromSIRH) {
		this(d, isFromSIRH);
		if (agentWithServiceDto != null) {
			this.agentWithServiceDto = agentWithServiceDto;
		} else {
			this.agentWithServiceDto = new AgentWithServiceDto();
			this.agentWithServiceDto.setIdAgent(etat.getIdAgent());
		}
	}

	public DemandeDto(Demande d, AgentGeneriqueDto agentDto, boolean isFromSIRH) {
		this(d, isFromSIRH);
		if (agentDto != null) {
			this.agentWithServiceDto =  new AgentWithServiceDto(agentDto);
		} else {
			this.agentWithServiceDto = new AgentWithServiceDto();
			this.agentWithServiceDto.setIdAgent(d.getIdAgent());
		}
	}

	public DemandeDto(Demande d, boolean isFromSIRH) {
		this();
		AgentWithServiceDto agentDto = new AgentWithServiceDto();
		agentDto.setIdAgent(d.getIdAgent());
		this.agentWithServiceDto = agentDto;
		this.idDemande = d.getIdDemande();
		this.idTypeDemande = d.getType().getIdRefTypeAbsence();
		this.libelleTypeDemande = d.getType().getLabel();
		if (null != d.getType().getGroupe()) {
			this.groupeAbsence = new RefGroupeAbsenceDto(d.getType().getGroupe());
		}
		if (d.getLatestEtatDemande().getEtat() != null)
			this.idRefEtat = d.getLatestEtatDemande().getEtat().getCodeEtat();
		this.dateDebut = d.getDateDebut();
		this.dateFin = d.getDateFin();
		this.dateDemande = d.getLatestEtatDemande().getDate();
		this.motif = d.getLatestEtatDemande().getMotif();
		this.commentaire = d.getCommentaire();
		this.commentaireDRH = d.getCommentaireDRH();
		if (null != d.getType() && null != d.getType().getTypeSaisi())
			this.typeSaisi = new RefTypeSaisiDto(d.getType().getTypeSaisi());

		if (null != d.getType() && d.getType().getTypeSaisi() == null && d.getType().getTypeSaisiCongeAnnuel() != null) {
			DemandeCongesAnnuels demandeCongeAnnu = (DemandeCongesAnnuels) d;
			this.typeSaisiCongeAnnuel = new RefTypeSaisiCongeAnnuelDto(demandeCongeAnnu.getTypeSaisiCongeAnnuel());
		}

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

		switch (RefTypeGroupeAbsenceEnum.getRefTypeGroupeAbsenceEnum(groupeAbsence.getIdRefGroupeAbsence())) {
			case REPOS_COMP:
				Integer dureeAnneeReposComp = ((DemandeReposComp) d).getDuree() == null ? 0 : ((DemandeReposComp) d)
						.getDuree();
				Integer dureeAnneePrecReposComp = ((DemandeReposComp) d).getDureeAnneeN1() == null ? 0
						: ((DemandeReposComp) d).getDureeAnneeN1();
				this.duree = (double) (dureeAnneeReposComp + dureeAnneePrecReposComp);
				this.totalMinutesOld = ((DemandeReposComp) d).getTotalMinutesOld();
				this.totalMinutesNew = ((DemandeReposComp) d).getTotalMinutesNew();
				this.totalMinutesAnneeN1Old = ((DemandeReposComp) d).getTotalMinutesAnneeN1Old();
				this.totalMinutesAnneeN1New = ((DemandeReposComp) d).getTotalMinutesAnneeN1New();
				break;
			case RECUP:
				this.duree = (double) (((DemandeRecup) d).getDuree());
				this.totalMinutesOld = ((DemandeRecup) d).getTotalMinutesOld();
				this.totalMinutesNew = ((DemandeRecup) d).getTotalMinutesNew();
				break;
			case AS:
				this.duree = ((DemandeAsa) d).getDuree();
				this.isDateDebutAM = ((DemandeAsa) d).isDateDebutAM();
				this.isDateDebutPM = ((DemandeAsa) d).isDateDebutPM();
				this.isDateFinAM = ((DemandeAsa) d).isDateFinAM();
				this.isDateFinPM = ((DemandeAsa) d).isDateFinPM();
				if (null != ((DemandeAsa) d).getOrganisationSyndicale())
					this.organisationSyndicale = new OrganisationSyndicaleDto(
							((DemandeAsa) d).getOrganisationSyndicale());
				this.totalMinutesOld = ((DemandeAsa) d).getTotalMinutesOld();
				this.totalMinutesNew = ((DemandeAsa) d).getTotalMinutesNew();
				this.totalJoursOld = ((DemandeAsa) d).getTotalJoursOld();
				this.totalJoursNew = ((DemandeAsa) d).getTotalJoursNew();
				break;
			case CONGES_EXCEP:
				this.duree = ((DemandeCongesExceptionnels) d).getDuree();
				this.isDateDebutAM = ((DemandeCongesExceptionnels) d).isDateDebutAM();
				this.isDateDebutPM = ((DemandeCongesExceptionnels) d).isDateDebutPM();
				this.isDateFinAM = ((DemandeCongesExceptionnels) d).isDateFinAM();
				this.isDateFinPM = ((DemandeCongesExceptionnels) d).isDateFinPM();
				break;
			case CONGES_ANNUELS:
				Double dureeAnneeCongeAnnuel = ((DemandeCongesAnnuels) d).getDuree() == null ? 0
						: ((DemandeCongesAnnuels) d).getDuree();
				Double dureeAnneePrecCongeAnnuel = ((DemandeCongesAnnuels) d).getDureeAnneeN1() == null ? 0
						: ((DemandeCongesAnnuels) d).getDureeAnneeN1();
				this.duree = (double) (dureeAnneeCongeAnnuel + dureeAnneePrecCongeAnnuel);
				this.isSamediOffert = ((DemandeCongesAnnuels) d).getNbSamediOffert() >= 1.0 ? true : false;
				this.isDateDebutAM = ((DemandeCongesAnnuels) d).isDateDebutAM();
				this.isDateDebutPM = ((DemandeCongesAnnuels) d).isDateDebutPM();
				this.isDateFinAM = ((DemandeCongesAnnuels) d).isDateFinAM();
				this.isDateFinPM = ((DemandeCongesAnnuels) d).isDateFinPM();
				this.totalJoursOld = ((DemandeCongesAnnuels) d).getTotalJoursOld();
				this.totalJoursNew = ((DemandeCongesAnnuels) d).getTotalJoursNew();
				this.totalJoursAnneeN1Old = ((DemandeCongesAnnuels) d).getTotalJoursAnneeN1Old();
				this.totalJoursAnneeN1New = ((DemandeCongesAnnuels) d).getTotalJoursAnneeN1New();
				
				if(null != d.getDateFin()) 
					this.dateReprise = new DateTime(d.getDateFin()).plusDays(1).toDate(); 
				break;
			case MALADIES:
				Double duree = ((DemandeMaladies) d).getDuree() == null ? 0
						: ((DemandeMaladies) d).getDuree();
				this.duree = (double) duree;
				// #32371 maladie enfant - saisie possible à la demi-journée
				this.isDateDebutAM = ((DemandeMaladies) d).isDateDebutAM();
				this.isDateDebutPM = ((DemandeMaladies) d).isDateDebutPM();
				this.isDateFinAM = ((DemandeMaladies) d).isDateFinAM();
				this.isDateFinPM = ((DemandeMaladies) d).isDateFinPM();
				this.nombreITT = ((DemandeMaladies) d).getNombreITT();
				this.prescripteur = ((DemandeMaladies) d).getPrescripteur();
				this.nomEnfant = ((DemandeMaladies) d).getNomEnfant();
				this.dateDeclaration = ((DemandeMaladies) d).getDateDeclaration();
				this.dateAccidentTravail = ((DemandeMaladies) d).getDateAccidentTravail();
				this.sansArretTravail = ((DemandeMaladies) d).isSansArretTravail();
				this.prolongation = ((DemandeMaladies) d).isProlongation();
				
				if(null != ((DemandeMaladies) d).getTypeAccidentTravail()) 
					this.typeAccidentTravail = new RefTypeDto(((DemandeMaladies) d).getTypeAccidentTravail());
				
				if(null != ((DemandeMaladies) d).getTypeSiegeLesion()) 
					this.typeSiegeLesion = new RefTypeDto(((DemandeMaladies) d).getTypeSiegeLesion());
				
				if(null != ((DemandeMaladies) d).getTypeMaladiePro()) 
					this.typeMaladiePro = new RefTypeDto(((DemandeMaladies) d).getTypeMaladiePro());
				
				if(null != ((DemandeMaladies) d).getAccidentTravailReference())
					this.accidentTravailReference = new DemandeDto(((DemandeMaladies) d).getAccidentTravailReference(), isFromSIRH);
				
				this.dateTransmissionCafat = ((DemandeMaladies) d).getDateTransmissionCafat();
				this.dateDecisionCafat = ((DemandeMaladies) d).getDateDecisionCafat();
				this.dateCommissionAptitude = ((DemandeMaladies) d).getDateCommissionAptitude();
				this.tauxCafat = ((DemandeMaladies) d).getTauxCafat();
				this.avisCommissionAptitude = ((DemandeMaladies) d).isAvisCommissionAptitude();
				this.controleMedical = new ControleMedicalDto(d.getControleMedical());
				
				break;
			default:
				break;
		}
		
		if(null != d.getPiecesJointes()
				&& !d.getPiecesJointes().isEmpty()) {
			for(PieceJointe pj : d.getPiecesJointes()) {
				if(isFromSIRH
						|| pj.isVisibleKiosque()) {
					PieceJointeDto pjDto = new PieceJointeDto(pj);
					this.getPiecesJointes().add(pjDto);
				}
			}
		}
	}

	public DemandeDto(CongeAnnuelRestitutionMassiveHisto restitution, AgentWithServiceDto agentDto) {
		this(restitution);
		if (agentDto != null) {
			this.agentWithServiceDto = agentDto;
		} else {
			this.agentWithServiceDto = new AgentWithServiceDto();
			this.agentWithServiceDto.setIdAgent(restitution.getIdAgent());
		}
	}
	
	public DemandeDto(CongeAnnuelRestitutionMassiveHisto restitution) {
		
		AgentWithServiceDto agentDto = new AgentWithServiceDto();
		agentDto.setIdAgent(restitution.getIdAgent());
		
		this.agentWithServiceDto = agentDto;
		this.libelleTypeDemande = "Restitution massive de congés annuels";
		this.dateDebut = restitution.getRestitutionMassive().getDateRestitution();
		this.dateFin = restitution.getRestitutionMassive().getDateRestitution();
		this.dateDemande = restitution.getRestitutionMassive().getDateModification();
		this.dateSaisie = restitution.getRestitutionMassive().getDateModification();
		this.motif = restitution.getRestitutionMassive().getMotif();
		this.duree = restitution.getJours();
		this.idTypeDemande = 0;

		this.isDateDebutAM = restitution.getRestitutionMassive().isMatin() || restitution.getRestitutionMassive().isJournee();
		this.isDateDebutPM = restitution.getRestitutionMassive().isApresMidi();
		
		this.isDateFinAM = restitution.getRestitutionMassive().isMatin();
		this.isDateFinPM = restitution.getRestitutionMassive().isApresMidi() || restitution.getRestitutionMassive().isJournee();
		
		this.affichageBoutonHistorique = false;
	}

	public void updateEtat(EtatDemande etat, AgentWithServiceDto agentDto, RefGroupeAbsence groupe) {
		idRefEtat = etat.getEtat().getCodeEtat();
		dateSaisie = etat.getDate();
		motif = etat.getMotif();
		agentEtat = agentDto;
		dateDebut = etat.getDateDebut();
		dateFin = etat.getDateFin();
		commentaire = etat.getCommentaire();

		switch (RefTypeGroupeAbsenceEnum.getRefTypeGroupeAbsenceEnum(groupe.getIdRefGroupeAbsence())) {
			case REPOS_COMP:
				Integer dureeAnneeReposComp = ((EtatDemandeReposComp) etat).getDuree() == null ? 0
						: ((EtatDemandeReposComp) etat).getDuree();
				Integer dureeAnneePrecReposComp = ((EtatDemandeReposComp) etat).getDureeAnneeN1() == null ? 0
						: ((EtatDemandeReposComp) etat).getDureeAnneeN1();
				this.duree = (double) (dureeAnneeReposComp + dureeAnneePrecReposComp);
				break;
			case RECUP:
				this.duree = (double) (((EtatDemandeRecup) etat).getDuree());
				break;
			case AS:
				this.duree = ((EtatDemandeAsa) etat).getDuree();
				this.isDateDebutAM = ((EtatDemandeAsa) etat).isDateDebutAM();
				this.isDateDebutPM = ((EtatDemandeAsa) etat).isDateDebutPM();
				this.isDateFinAM = ((EtatDemandeAsa) etat).isDateFinAM();
				this.isDateFinPM = ((EtatDemandeAsa) etat).isDateFinPM();
				if (null != ((EtatDemandeAsa) etat).getOrganisationSyndicale())
					this.organisationSyndicale = new OrganisationSyndicaleDto(
							((EtatDemandeAsa) etat).getOrganisationSyndicale());
				break;
			case CONGES_EXCEP:
				this.duree = ((EtatDemandeCongesExceptionnels) etat).getDuree();
				this.isDateDebutAM = ((EtatDemandeCongesExceptionnels) etat).isDateDebutAM();
				this.isDateDebutPM = ((EtatDemandeCongesExceptionnels) etat).isDateDebutPM();
				this.isDateFinAM = ((EtatDemandeCongesExceptionnels) etat).isDateFinAM();
				this.isDateFinPM = ((EtatDemandeCongesExceptionnels) etat).isDateFinPM();
				break;
			case CONGES_ANNUELS:
				Double dureeAnneeCongeAnnuel = ((EtatDemandeCongesAnnuels) etat).getDuree() == null ? 0
						: ((EtatDemandeCongesAnnuels) etat).getDuree();
				Double dureeAnneePrecCongeAnnuel = ((EtatDemandeCongesAnnuels) etat).getDureeAnneeN1() == null ? 0
						: ((EtatDemandeCongesAnnuels) etat).getDureeAnneeN1();
				this.duree = (double) (dureeAnneeCongeAnnuel + dureeAnneePrecCongeAnnuel);
				this.isSamediOffert = ((EtatDemandeCongesAnnuels) etat).getNbSamediOffert() >= 1.0 ? true : false;
				this.isDateDebutAM = ((EtatDemandeCongesAnnuels) etat).isDateDebutAM();
				this.isDateDebutPM = ((EtatDemandeCongesAnnuels) etat).isDateDebutPM();
				this.isDateFinAM = ((EtatDemandeCongesAnnuels) etat).isDateFinAM();
				this.isDateFinPM = ((EtatDemandeCongesAnnuels) etat).isDateFinPM();
				break;
			default:
				break;
		}
	}
	
	public DemandeDto(EtatDemandeCongesAnnuels etatCA){
		this();
		this.idDemande = etatCA.getDemande().getIdDemande();
		this.dateDebut = etatCA.getDateDebut();
		this.dateFin = etatCA.getDateFin();
		this.dateSaisie = etatCA.getDate();
		Double dureeAnneeCongeAnnuel = etatCA.getDuree() == null ? 0 : etatCA.getDuree();
		Double dureeAnneePrecCongeAnnuel = etatCA.getDureeAnneeN1() == null ? 0
				: etatCA.getDureeAnneeN1();
		this.duree = (double) (dureeAnneeCongeAnnuel + dureeAnneePrecCongeAnnuel);
		this.idRefEtat = etatCA.getEtat().getCodeEtat();
		this.isSamediOffert = etatCA.getNbSamediOffert() >= 1.0 ? true : false;
		this.isDateDebutAM = etatCA.isDateDebutAM();
		this.isDateDebutPM = etatCA.isDateDebutPM();
		this.isDateFinAM = etatCA.isDateFinAM();
		this.isDateFinPM = etatCA.isDateFinPM();
		this.commentaire = etatCA.getCommentaire();
		this.totalJoursOld = etatCA.getTotalJoursOld();
		this.totalJoursNew = etatCA.getTotalJoursNew();
		this.totalJoursAnneeN1Old = etatCA.getTotalJoursAnneeN1Old();
		this.totalJoursAnneeN1New = etatCA.getTotalJoursAnneeN1New();
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

	public RefGroupeAbsenceDto getGroupeAbsence() {
		return groupeAbsence;
	}

	public void setGroupeAbsence(RefGroupeAbsenceDto groupeAbsence) {
		this.groupeAbsence = groupeAbsence;
	}

	public String getCommentaire() {
		return commentaire;
	}

	public void setCommentaire(String commentaire) {
		this.commentaire = commentaire;
	}

	public RefTypeSaisiDto getTypeSaisi() {
		return typeSaisi;
	}

	public void setTypeSaisi(RefTypeSaisiDto typeSaisi) {
		this.typeSaisi = typeSaisi;
	}

	public String getLibelleTypeDemande() {
		return libelleTypeDemande;
	}

	public void setLibelleTypeDemande(String libelleTypeDemande) {
		this.libelleTypeDemande = libelleTypeDemande;
	}

	public String getDtoToString(DemandeDto dto) {
		String json = new JSONSerializer().exclude("*.class").transform(new MSDateTransformer(), Date.class)
				.deepSerialize(dto);
		return json;
	}

	public AgentWithServiceDto getAgentEtat() {
		return agentEtat;
	}

	public void setAgentEtat(AgentWithServiceDto agentEtat) {
		this.agentEtat = agentEtat;
	}

	public Date getDateReprise() {
		return dateReprise;
	}

	public void setDateReprise(Date dateReprise) {
		this.dateReprise = dateReprise;
	}

	public RefTypeSaisiCongeAnnuelDto getTypeSaisiCongeAnnuel() {
		return typeSaisiCongeAnnuel;
	}

	public void setTypeSaisiCongeAnnuel(RefTypeSaisiCongeAnnuelDto typeSaisiCongeAnnuel) {
		this.typeSaisiCongeAnnuel = typeSaisiCongeAnnuel;
	}

	public boolean isDepassementMultiple() {
		return isDepassementMultiple;
	}

	public void setDepassementMultiple(boolean isDepassementMultiple) {
		this.isDepassementMultiple = isDepassementMultiple;
	}

	public boolean isSamediOffert() {
		return isSamediOffert;
	}

	public void setSamediOffert(boolean isSamediOffert) {
		this.isSamediOffert = isSamediOffert;
	}

	public Double getTotalJoursNew() {
		return totalJoursNew;
	}

	public void setTotalJoursNew(Double totalJoursNew) {
		this.totalJoursNew = totalJoursNew;
	}

	public Double getTotalJoursOld() {
		return totalJoursOld;
	}

	public void setTotalJoursOld(Double totalJoursOld) {
		this.totalJoursOld = totalJoursOld;
	}

	public Double getTotalJoursAnneeN1New() {
		return totalJoursAnneeN1New;
	}

	public void setTotalJoursAnneeN1New(Double totalJoursAnneeN1New) {
		this.totalJoursAnneeN1New = totalJoursAnneeN1New;
	}

	public Double getTotalJoursAnneeN1Old() {
		return totalJoursAnneeN1Old;
	}

	public void setTotalJoursAnneeN1Old(Double totalJoursAnneeN1Old) {
		this.totalJoursAnneeN1Old = totalJoursAnneeN1Old;
	}

	public Integer getTotalMinutesNew() {
		return totalMinutesNew;
	}

	public void setTotalMinutesNew(Integer totalMinutesNew) {
		this.totalMinutesNew = totalMinutesNew;
	}

	public Integer getTotalMinutesOld() {
		return totalMinutesOld;
	}

	public void setTotalMinutesOld(Integer totalMinutesOld) {
		this.totalMinutesOld = totalMinutesOld;
	}

	public Integer getTotalMinutesAnneeN1New() {
		return totalMinutesAnneeN1New;
	}

	public void setTotalMinutesAnneeN1New(Integer totalMinutesAnneeN1New) {
		this.totalMinutesAnneeN1New = totalMinutesAnneeN1New;
	}

	public Integer getTotalMinutesAnneeN1Old() {
		return totalMinutesAnneeN1Old;
	}

	public void setTotalMinutesAnneeN1Old(Integer totalMinutesAnneeN1Old) {
		this.totalMinutesAnneeN1Old = totalMinutesAnneeN1Old;
	}

	public boolean isAffichageBoutonHistorique() {
		return affichageBoutonHistorique;
	}

	public void setAffichageBoutonHistorique(boolean affichageBoutonHistorique) {
		this.affichageBoutonHistorique = affichageBoutonHistorique;
	}

	public RefEtatDto getEtatDto() {
		return etatDto;
	}

	public void setEtatDto(RefEtatDto etatDto) {
		this.etatDto = etatDto;
	}

	public boolean isForceSaisieManuelleDuree() {
		return forceSaisieManuelleDuree;
	}

	public void setForceSaisieManuelleDuree(boolean forceSaisieManuelleDuree) {
		this.forceSaisieManuelleDuree = forceSaisieManuelleDuree;
	}

	public Double getNombreITT() {
		return nombreITT;
	}

	public void setNombreITT(Double nombreITT) {
		this.nombreITT = nombreITT;
	}

	public String getPrescripteur() {
		return prescripteur;
	}

	public void setPrescripteur(String prescripteur) {
		this.prescripteur = prescripteur;
	}

	public String getNomEnfant() {
		return nomEnfant;
	}

	public void setNomEnfant(String nomEnfant) {
		this.nomEnfant = nomEnfant;
	}

	public Date getDateDeclaration() {
		return dateDeclaration;
	}

	public void setDateDeclaration(Date dateDeclaration) {
		this.dateDeclaration = dateDeclaration;
	}

	public boolean isProlongation() {
		return prolongation;
	}

	public void setProlongation(boolean prolongation) {
		this.prolongation = prolongation;
	}

	public RefTypeDto getTypeAccidentTravail() {
		return typeAccidentTravail;
	}

	public void setTypeAccidentTravail(RefTypeDto typeAccidentTravail) {
		this.typeAccidentTravail = typeAccidentTravail;
	}

	public RefTypeDto getTypeSiegeLesion() {
		return typeSiegeLesion;
	}

	public void setTypeSiegeLesion(RefTypeDto typeSiegeLesion) {
		this.typeSiegeLesion = typeSiegeLesion;
	}

	public RefTypeDto getTypeMaladiePro() {
		return typeMaladiePro;
	}

	public void setTypeMaladiePro(RefTypeDto typeMaladiePro) {
		this.typeMaladiePro = typeMaladiePro;
	}

	public DemandeDto getAccidentTravailReference() {
		return accidentTravailReference;
	}

	public void setAccidentTravailReference(DemandeDto accidentTravailReference) {
		this.accidentTravailReference = accidentTravailReference;
	}

	public boolean isAffichageBoutonRejeter() {
		return isAffichageBoutonRejeter;
	}

	public void setAffichageBoutonRejeter(boolean isAffichageBoutonRejeter) {
		this.isAffichageBoutonRejeter = isAffichageBoutonRejeter;
	}

	public Date getDateTransmissionCafat() {
		return dateTransmissionCafat;
	}

	public void setDateTransmissionCafat(Date dateTransmissionCafat) {
		this.dateTransmissionCafat = dateTransmissionCafat;
	}

	public Date getDateDecisionCafat() {
		return dateDecisionCafat;
	}

	public void setDateDecisionCafat(Date dateDecisionCafat) {
		this.dateDecisionCafat = dateDecisionCafat;
	}

	public Date getDateCommissionAptitude() {
		return dateCommissionAptitude;
	}

	public void setDateCommissionAptitude(Date dateCommissionAptitude) {
		this.dateCommissionAptitude = dateCommissionAptitude;
	}

	public Double getTauxCafat() {
		return tauxCafat;
	}

	public void setTauxCafat(Double tauxCafat) {
		this.tauxCafat = tauxCafat;
	}

	public Boolean getAvisCommissionAptitude() {
		return avisCommissionAptitude;
	}

	public void setAvisCommissionAptitude(Boolean avisCommissionAptitude) {
		this.avisCommissionAptitude = avisCommissionAptitude;
	}

	public List<PieceJointeDto> getPiecesJointes() {
		return piecesJointes;
	}

	public void setPiecesJointes(List<PieceJointeDto> piecesJointes) {
		this.piecesJointes = piecesJointes;
	}

	public boolean isFromHSCT() {
		return isFromHSCT;
	}

	public void setFromHSCT(boolean isFromHSCT) {
		this.isFromHSCT = isFromHSCT;
	}

	public boolean isDepassementITT() {
		return isDepassementITT;
	}

	public void setDepassementITT(boolean isDepassementITT) {
		this.isDepassementITT = isDepassementITT;
	}

	public String getCommentaireDRH() {
		return commentaireDRH;
	}

	public void setCommentaireDRH(String commentaireDRH) {
		this.commentaireDRH = commentaireDRH;
	}

	public ControleMedicalDto getControleMedical() {
		return controleMedical;
	}

	public void setControleMedical(ControleMedicalDto controleMedical) {
		this.controleMedical = controleMedical;
	}

	public Date getDateAccidentTravail() {
		return dateAccidentTravail;
	}

	public void setDateAccidentTravail(Date dateAccidentTravail) {
		this.dateAccidentTravail = dateAccidentTravail;
	}

	public boolean isSansArretTravail() {
		return sansArretTravail;
	}

	public void setSansArretTravail(boolean sansArretTravail) {
		this.sansArretTravail = sansArretTravail;
	}
	
}
