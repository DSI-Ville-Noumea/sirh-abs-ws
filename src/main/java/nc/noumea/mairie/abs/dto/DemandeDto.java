package nc.noumea.mairie.abs.dto;

import java.util.Date;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeRecup;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;

public class DemandeDto {

	private Integer idDemande;
	private Integer idAgent;
	private Integer idTypeDemande;
	private Date dateDebut;
	private Integer duree;
	private Integer idRefEtat;
	private boolean isEtatDefinitif;
	private Date dateDemande;
	private boolean isDemandeModifer;
	private boolean isDemandeSupprimer;
	private boolean isDemandeImprimer;

	public DemandeDto() {
	}

	public DemandeDto(Demande d) {
		super();
		this.idDemande = d.getIdDemande();
		this.idAgent = d.getIdAgent();
		this.idTypeDemande = d.getType().getIdRefTypeAbsence();
		this.dateDebut = d.getDateDebut();
		this.idRefEtat = d.getLatestEtatDemande().getEtat().getCodeEtat();
		this.dateDemande = d.getLatestEtatDemande().getDate();
		this.isEtatDefinitif = d.getLatestEtatDemande().getEtat().getCodeEtat() == 1;

		this.isDemandeModifer = d.getLatestEtatDemande().getEtat().getCodeEtat() == 0
				|| d.getLatestEtatDemande().getEtat().getCodeEtat() == 1;
		this.isDemandeSupprimer = d.getLatestEtatDemande().getEtat().getCodeEtat() == 0
				|| d.getLatestEtatDemande().getEtat().getCodeEtat() == 1;
		this.isDemandeImprimer = d.getLatestEtatDemande().getEtat().getCodeEtat() == 4;
		switch (RefTypeAbsenceEnum.getRefTypeAbsenceEnum(idTypeDemande)) {
			case CONGE_ANNUEL:
				// TODO
				break;
			case REPOS_COMP:
				// TODO
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

	public boolean isEtatDefinitif() {
		return isEtatDefinitif;
	}

	public void setEtatDefinitif(boolean isEtatDefinitif) {
		this.isEtatDefinitif = isEtatDefinitif;
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

	public boolean isDemandeModifer() {
		return isDemandeModifer;
	}

	public void setDemandeModifer(boolean isDemandeModifer) {
		this.isDemandeModifer = isDemandeModifer;
	}

	public boolean isDemandeSupprimer() {
		return isDemandeSupprimer;
	}

	public void setDemandeSupprimer(boolean isDemandeSupprimer) {
		this.isDemandeSupprimer = isDemandeSupprimer;
	}

	public boolean isDemandeImprimer() {
		return isDemandeImprimer;
	}

	public void setDemandeImprimer(boolean isDemandeImprimer) {
		this.isDemandeImprimer = isDemandeImprimer;
	}

}
