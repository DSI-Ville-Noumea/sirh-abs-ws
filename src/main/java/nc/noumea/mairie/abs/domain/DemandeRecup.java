package nc.noumea.mairie.abs.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PersistenceUnit;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "ABS_DEMANDE_RECUP")
@PersistenceUnit(unitName = "absPersistenceUnit")
@PrimaryKeyJoinColumn(name = "ID_DEMANDE") 
public class DemandeRecup extends Demande {

	@NotNull
	@Column(name = "DUREE")
	private Integer duree;
	
	// solde annee en cours avant decompte de la demande
	@Column(name = "TOTAL_MINUTES_OLD", columnDefinition = "numeric")
	private Integer totalMinutesOld;
	
	// solde annee en cours apres decompte de la demande
	@Column(name = "TOTAL_MINUTES_NEW", columnDefinition = "numeric")
	private Integer totalMinutesNew;

	public DemandeRecup() {
	}

	public DemandeRecup(Demande demande) {
		super(demande);
	}

	public Integer getDuree() {
		return duree;
	}

	public void setDuree(Integer duree) {
		this.duree = duree;
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
	
}
