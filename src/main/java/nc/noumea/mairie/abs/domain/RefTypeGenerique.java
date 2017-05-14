package nc.noumea.mairie.abs.domain;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class RefTypeGenerique {

	@Column(name = "LIBELLE", columnDefinition = "NVARCHAR2")
	private String libelle;

	public String getLibelle() {
		return libelle;
	}

	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}
}
