package nc.noumea.mairie.abs.domain;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord(persistenceUnit = "absPersistenceUnit", table = "ABS_MOTIF_REFUS")
public class MotifRefus {
	

	@Id
	@Column(name = "ID_MOTIF_REFUS")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idMotifRefus;

	@NotNull
	@Column(name = "LIBELLE")
	private String libelle;

	@ManyToOne()
	@JoinColumn(name = "ID_REF_TYPE_ABSENCE", referencedColumnName = "ID_REF_TYPE_ABSENCE")
	private RefTypeAbsence refTypeAbsence;

}
