package nc.noumea.mairie.abs.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.springframework.roo.addon.javabean.RooJavaBean;

@RooJavaBean
@MappedSuperclass
public abstract class BaseAgentWeekHisto {

	@NotNull
	@Column(name = "ID_AGENT")
	private Integer idAgent;

	@NotNull
	@Column(name = "MINUTES")
	private int minutes;

	@Column(name = "DATE_MONDAY")
	@Temporal(TemporalType.DATE)
	private Date dateMonday;

	@Column(name = "DATE_MODIFICATION")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastModification;
}
