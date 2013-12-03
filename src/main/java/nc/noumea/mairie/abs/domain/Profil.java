package nc.noumea.mairie.abs.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord(persistenceUnit = "absPersistenceUnit", table = "ABS_PROFIL")
public class Profil {

	@Id
	@Column(name = "ID_PROFIL")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idProfil;

	@NotNull
	@Column(name = "LIBELLE", columnDefinition = "NVARCHAR2")
	private String libelle;

	@NotNull
	@Column(name = "SAISIE", nullable = false)
	@Type(type = "boolean")
	private boolean saisie;

	@NotNull
	@Column(name = "MODIFICATION", nullable = false)
	@Type(type = "boolean")
	private boolean modification;

	@NotNull
	@Column(name = "SUPPRESSION", nullable = false)
	@Type(type = "boolean")
	private boolean suppression;

	@NotNull
	@Column(name = "IMPRESSION", nullable = false)
	@Type(type = "boolean")
	private boolean impression;

	@NotNull
	@Column(name = "VISER_VISU", nullable = false)
	@Type(type = "boolean")
	private boolean viserVisu;

	@NotNull
	@Column(name = "VISER_MODIF", nullable = false)
	@Type(type = "boolean")
	private boolean viserModif;

	@NotNull
	@Column(name = "APPROUVER_VISU", nullable = false)
	@Type(type = "boolean")
	private boolean approuverVisu;

	@NotNull
	@Column(name = "APPROUVER_MODIF", nullable = false)
	@Type(type = "boolean")
	private boolean approuverModif;

	@NotNull
	@Column(name = "ANNULER", nullable = false)
	@Type(type = "boolean")
	private boolean annuler;

	@NotNull
	@Column(name = "VISU_SOLDE", nullable = false)
	@Type(type = "boolean")
	private boolean visuSolde;

	@NotNull
	@Column(name = "MAJ_SOLDE", nullable = false)
	@Type(type = "boolean")
	private boolean majSolde;

	@NotNull
	@Column(name = "DROIT_ACCES", nullable = false)
	@Type(type = "boolean")
	private boolean droitAcces;
	
}
