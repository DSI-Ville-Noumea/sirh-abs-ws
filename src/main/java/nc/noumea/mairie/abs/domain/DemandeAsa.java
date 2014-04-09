package nc.noumea.mairie.abs.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PersistenceUnit;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "ABS_DEMANDE_ASA")
@PersistenceUnit(unitName = "absPersistenceUnit")
@PrimaryKeyJoinColumn(name = "ID_DEMANDE") 
public class DemandeAsa extends Demande {
	
	@Column(name = "DUREE")
	private Integer duree;
	
	@NotNull
	@Column(name = "DATE_DEBUT_AM")
	private boolean dateDebutAM;
	
	@NotNull
	@Column(name = "DATE_DEBUT_PM")
	private boolean dateDebutPM;
	
	@NotNull
	@Column(name = "DATE_FIN_AM")
	private boolean dateFinAM;
	
	@NotNull
	@Column(name = "DATE_FIN_PM")
	private boolean dateFinPM;

	public boolean isDateDebutAM() {
		return dateDebutAM;
	}

	public void setDateDebutAM(boolean dateDebutAM) {
		this.dateDebutAM = dateDebutAM;
	}

	public boolean isDateDebutPM() {
		return dateDebutPM;
	}

	public void setDateDebutPM(boolean dateDebutPM) {
		this.dateDebutPM = dateDebutPM;
	}

	public boolean isDateFinAM() {
		return dateFinAM;
	}

	public void setDateFinAM(boolean dateFinAM) {
		this.dateFinAM = dateFinAM;
	}

	public boolean isDateFinPM() {
		return dateFinPM;
	}

	public void setDateFinPM(boolean dateFinPM) {
		this.dateFinPM = dateFinPM;
	}

	public Integer getDuree() {
		return duree;
	}

	public void setDuree(Integer duree) {
		this.duree = duree;
	}
	
	
}
