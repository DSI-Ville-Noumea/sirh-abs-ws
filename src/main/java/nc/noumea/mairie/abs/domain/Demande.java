package nc.noumea.mairie.abs.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

@Entity
@Table(name = "ABS_DEMANDE")
@Inheritance(strategy=InheritanceType.JOINED)
@RooJavaBean
@RooToString
public class Demande implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id 
	@Column(name = "ID_DEMANDE")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idDemande;
	
	@NotNull
	@Column(name = "ID_AGENT")
	private Integer idAgent;
	
	@OneToOne(optional = true)
	@JoinColumn(name = "ID_TYPE_DEMANDE")
	private RefTypeAbsence type;
	
	@Column(name = "DATE_DEBUT")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateDebut;
	
	@OneToMany(mappedBy = "demande", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
	@OrderBy("date desc")
	private List<EtatDemande> etatsDemande = new ArrayList<EtatDemande>();
	
}
