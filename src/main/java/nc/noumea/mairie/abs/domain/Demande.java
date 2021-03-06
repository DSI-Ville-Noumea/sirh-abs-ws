package nc.noumea.mairie.abs.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import nc.noumea.mairie.abs.dto.DemandeDto;

@Entity
@Table(name = "ABS_DEMANDE")
@PersistenceUnit(unitName = "absPersistenceUnit")
@Inheritance(strategy = InheritanceType.JOINED)
public class Demande {

	@Id
	@Column(name = "ID_DEMANDE")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idDemande;

	@NotNull
	@Column(name = "ID_AGENT")
	private Integer idAgent;

	@OneToOne(fetch = FetchType.LAZY, optional = true, orphanRemoval = false)
	@JoinColumn(name = "ID_TYPE_DEMANDE")
	private RefTypeAbsence type;

	@Column(name = "DATE_DEBUT")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateDebut;

	@Column(name = "DATE_FIN")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateFin;

	@OneToMany(mappedBy = "demande", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
	@OrderBy("idEtatDemande desc")
	private List<EtatDemande> etatsDemande = new ArrayList<EtatDemande>();

	@OneToMany(mappedBy = "demande", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
	private List<PieceJointe> piecesJointes = new ArrayList<PieceJointe>();

	@Column(name = "COMMENTAIRE")
	private String commentaire;

	@Column(name = "COMMENTAIRE_DRH")
	private String commentaireDRH;
	
    @OneToOne
    @Transient
    @JoinColumn(referencedColumnName = "id_abs_controle_medical")
    protected ControleMedical controleMedical;

	@Transient
	public EtatDemande getLatestEtatDemande() {
		if (!etatsDemande.isEmpty()) {
			return etatsDemande.iterator().next();
		}
		return null;
	}

	@Transient
	public void addEtatDemande(EtatDemande etatDemande) {
		etatDemande.setDemande(this);
		this.getEtatsDemande().add(etatDemande);
	}

	public Demande() {
	}

	public Demande(Demande demande) {
		super();
		this.idDemande = demande.getIdDemande();
		this.idAgent = demande.getIdAgent();
		this.type = demande.getType();
		this.dateDebut = demande.getDateDebut();
		this.dateFin = demande.getDateFin();
		this.etatsDemande = demande.getEtatsDemande();
		this.commentaire = demande.getCommentaire();
	}

	@Transient
	public static Demande mappingDemandeDtoToDemande(DemandeDto demandeDto, Demande demande) {

		// on mappe le DTO dans la Demande generique
		demande.setDateDebut(demandeDto.getDateDebut());
		demande.setIdAgent(demandeDto.getAgentWithServiceDto().getIdAgent());
		if (demande.getType() == null || demandeDto.getTypeSaisi() == null || demande.getType().getIdRefTypeAbsence() != demandeDto.getTypeSaisi().getIdRefTypeDemande()) {
			RefTypeAbsence rta = new RefTypeAbsence();
			rta.setIdRefTypeAbsence(demandeDto.getIdTypeDemande());
			demande.setType(rta);
		}

		return demande;
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

	public RefTypeAbsence getType() {
		return type;
	}

	public void setType(RefTypeAbsence type) {
		this.type = type;
	}

	public Date getDateDebut() {
		return dateDebut;
	}

	public void setDateDebut(Date dateDebut) {
		this.dateDebut = dateDebut;
	}

	public Date getDateFin() {
		return dateFin;
	}

	public void setDateFin(Date dateFin) {
		this.dateFin = dateFin;
	}

	public List<EtatDemande> getEtatsDemande() {
		return etatsDemande;
	}

	public void setEtatsDemande(List<EtatDemande> etatsDemande) {
		this.etatsDemande = etatsDemande;
	}

	public List<PieceJointe> getPiecesJointes() {
		return piecesJointes;
	}

	public void setPiecesJointes(List<PieceJointe> piecesJointes) {
		this.piecesJointes = piecesJointes;
	}

	public String getCommentaire() {
		return commentaire;
	}

	public void setCommentaire(String commentaire) {
		this.commentaire = commentaire;
	}

	public String getCommentaireDRH() {
		return commentaireDRH;
	}

	public void setCommentaireDRH(String commentaireDRH) {
		this.commentaireDRH = commentaireDRH;
	}

	public ControleMedical getControleMedical() {
		return controleMedical;
	}

	public void setControleMedical(ControleMedical controleMedical) {
		this.controleMedical = controleMedical;
	}


}
