package nc.noumea.mairie.abs.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

@Entity
@Table(name = "ABS_DEMANDE_RECUP")
@PrimaryKeyJoinColumn(name = "ID_DEMANDE")
@RooJavaBean
@RooToString
public class DemandeRecup extends Demande {
	
	@NotNull
	@Column(name = "DUREE")
	private Integer duree;
	
	public DemandeRecup(){
	}
			
	public DemandeRecup(Demande demande) {
		super(demande);
	}
}
