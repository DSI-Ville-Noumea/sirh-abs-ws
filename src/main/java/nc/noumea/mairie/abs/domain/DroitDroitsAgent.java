package nc.noumea.mairie.abs.domain;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord(persistenceUnit = "absPersistenceUnit", table = "ABS_DROIT_DROITS_AGENT")
public class DroitDroitsAgent {
	
	
	@Id
	@Column(name = "ID_DROIT_DROITS_AGENT")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idDroitDroitsAgent;
	
	@ManyToOne()
	@JoinColumn(name = "ID_DROIT", referencedColumnName = "ID_DROIT")
	private Droit droit;

	@ManyToOne()
	@JoinColumn(name = "ID_DROITS_AGENT", referencedColumnName = "ID_DROITS_AGENT")
	private DroitsAgent droitsAgent;

	@ManyToOne()
	@JoinColumn(name = "ID_DROIT_PROFIL", referencedColumnName = "ID_DROIT_PROFIL")
	private DroitProfil droitProfil;
	
}
