package nc.noumea.mairie.abs.domain;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord(persistenceUnit = "absPersistenceUnit", table = "ABS_DROIT_PROFIL")
@NamedQueries({ @NamedQuery(name = "getInputterDroitProfilOfApprobateurByLibelle", query = "select dp from DroitProfil dp where dp.droitApprobateur.idAgent= :idAgentApprobateur and dp.droit.idAgent= :idAgent and dp.droit.idAgent!= :idAgentApprobateur and dp.profil.libelle= :libelle ") })
public class DroitProfil {

	@Id
	@Column(name = "ID_DROIT_PROFIL")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idDroitProfil;

	@ManyToOne()
	@JoinColumn(name = "ID_DROIT", referencedColumnName = "ID_DROIT")
	private Droit droit;

	@ManyToOne()
	@JoinColumn(name = "ID_PROFIL", referencedColumnName = "ID_PROFIL")
	private Profil profil;

	@ManyToOne()
	@JoinColumn(name = "ID_DROIT_APPROBATEUR", referencedColumnName = "ID_DROIT")
	private Droit droitApprobateur;
}
