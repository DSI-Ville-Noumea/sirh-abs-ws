package nc.noumea.mairie.abs.dto;

import java.util.Date;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

public class JourDto {

	@JsonSerialize(using = JsonDateSerializer.class)
	@JsonDeserialize(using = JsonDateDeserializer.class)
	private Date jour;
	private boolean isFerie;
	private boolean isChome;
	
	public Date getJour() {
		return jour;
	}
	public void setJour(Date jour) {
		this.jour = jour;
	}
	public boolean isFerie() {
		return isFerie;
	}
	public void setFerie(boolean isFerie) {
		this.isFerie = isFerie;
	}
	public boolean isChome() {
		return isChome;
	}
	public void setChome(boolean isChome) {
		this.isChome = isChome;
	}
	
}
