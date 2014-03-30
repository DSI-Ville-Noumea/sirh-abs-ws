package nc.noumea.mairie.abs.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "ABS_MOTIF")
@PersistenceUnit(unitName = "absPersistenceUnit")
public class Motif {

	@Id
	@Column(name = "ID_MOTIF")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idMotif;

	@NotNull
	@Column(name = "LIBELLE")
	private String libelle;

	@Version
	@Column(name = "version")
	private Integer version;

	public Integer getIdMotif() {
		return idMotif;
	}

	public void setIdMotif(Integer idMotif) {
		this.idMotif = idMotif;
	}

	public String getLibelle() {
		return libelle;
	}

	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

}
