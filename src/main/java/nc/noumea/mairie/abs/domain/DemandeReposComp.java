package nc.noumea.mairie.abs.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PersistenceUnit;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "ABS_DEMANDE_REPOS_COMP")
@PersistenceUnit(unitName = "absPersistenceUnit")
@PrimaryKeyJoinColumn(name = "ID_DEMANDE")
public class DemandeReposComp extends Demande {

	@NotNull
	@Column(name = "DUREE")
	private Integer duree;

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

}
