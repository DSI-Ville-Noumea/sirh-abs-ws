package nc.noumea.mairie.abs.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord(persistenceUnit = "absPersistenceUnit", table = "ABS_ETAT_DEMANDE", versionField = "")
public class EtatDemande {

	@Id 
	@Column(name = "ID_ETAT_DEMANDE")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idEtatDemande;
	
	@Column(name = "DATE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date date;
	
	@NotNull
	@Column(name = "ID_AGENT")
	private Integer idAgent;
	
	@ManyToOne
	@JoinColumn(name = "ID_DEMANDE", referencedColumnName = "ID_DEMANDE", updatable = true, insertable = true)
	private Demande demande;
	
	@NotNull
	@Column(name = "ID_REF_ETAT")
	@Enumerated(EnumType.ORDINAL)
	private RefEtatEnum etat;
	
	@Column(name = "ID_MOTIF_REFUS")
	private Integer idMotifRefus;
}
