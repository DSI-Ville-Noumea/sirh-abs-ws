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

}
