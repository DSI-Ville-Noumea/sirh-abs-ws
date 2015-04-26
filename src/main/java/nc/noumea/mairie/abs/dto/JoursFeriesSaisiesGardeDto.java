package nc.noumea.mairie.abs.dto;

import java.util.Date;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

public class JoursFeriesSaisiesGardeDto {

	@JsonSerialize(using = JsonDateSerializer.class)
	@JsonDeserialize(using = JsonDateDeserializer.class)
	private Date jourFerie;
	private boolean check;
	
	public Date getJourFerie() {
		return jourFerie;
	}
	public void setJourFerie(Date jourFerie) {
		this.jourFerie = jourFerie;
	}
	public boolean isCheck() {
		return check;
	}
	public void setCheck(boolean check) {
		this.check = check;
	}
	
	
}
