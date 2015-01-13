package nc.noumea.mairie.abs.dto;

import java.util.Date;

public class JourDto {

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
