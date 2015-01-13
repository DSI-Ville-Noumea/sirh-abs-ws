package nc.noumea.mairie.abs.dto;

import java.util.Date;

public class JoursFeriesSaisiesReposDto {

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
