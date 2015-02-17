package nc.noumea.mairie.abs.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PersistenceUnit;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Entity
@Table(name = "ABS_DEMANDE_REPOS_COMP")
@PersistenceUnit(unitName = "absPersistenceUnit")
@PrimaryKeyJoinColumn(name = "ID_DEMANDE")
public class DemandeReposComp extends Demande {
	
	@Column(name = "DUREE")
	private Integer duree;
	
	@Column(name = "DUREE_ANNEE_N1")
	private Integer dureeAnneeN1;
	
	// solde annee en cours avant decompte de la demande
	@Column(name = "TOTAL_MINUTES_OLD", columnDefinition = "numeric")
	private Integer totalMinutesOld;
	
	// solde annee en cours apres decompte de la demande
	@Column(name = "TOTAL_MINUTES_NEW", columnDefinition = "numeric")
	private Integer totalMinutesNew;
	
	// solde annee N-1 avant decompte de la demande
	@Column(name = "TOTAL_MINUTES_ANNEE_N1_OLD", columnDefinition = "numeric")
	private Integer totalMinutesAnneeN1Old;
	
	// solde annee N-1 apres decompte de la demande
	@Column(name = "TOTAL_MINUTES_ANNEE_N1_NEW", columnDefinition = "numeric")
	private Integer totalMinutesAnneeN1New;

	public DemandeReposComp() {
	}

	public DemandeReposComp(Demande demande) {
		super(demande);
	}

	public Integer getDuree() {
		return duree;
	}

	public void setDuree(Integer duree) {
		this.duree = duree;
	}

	public Integer getDureeAnneeN1() {
		return dureeAnneeN1;
	}

	public void setDureeAnneeN1(Integer dureeAnneeN1) {
		this.dureeAnneeN1 = dureeAnneeN1;
	}

	public Integer getTotalMinutesOld() {
		return totalMinutesOld;
	}

	public void setTotalMinutesOld(Integer totalMinutesOld) {
		this.totalMinutesOld = totalMinutesOld;
	}

	public Integer getTotalMinutesNew() {
		return totalMinutesNew;
	}

	public void setTotalMinutesNew(Integer totalMinutesNew) {
		this.totalMinutesNew = totalMinutesNew;
	}

	public Integer getTotalMinutesAnneeN1Old() {
		return totalMinutesAnneeN1Old;
	}

	public void setTotalMinutesAnneeN1Old(Integer totalMinutesAnneeN1Old) {
		this.totalMinutesAnneeN1Old = totalMinutesAnneeN1Old;
	}

	public Integer getTotalMinutesAnneeN1New() {
		return totalMinutesAnneeN1New;
	}

	public void setTotalMinutesAnneeN1New(Integer totalMinutesAnneeN1New) {
		this.totalMinutesAnneeN1New = totalMinutesAnneeN1New;
	}

}
