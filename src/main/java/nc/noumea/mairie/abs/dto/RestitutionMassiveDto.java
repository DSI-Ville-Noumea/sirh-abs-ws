package nc.noumea.mairie.abs.dto;

import java.util.Date;

public class RestitutionMassiveDto {

	private Integer idAgent;
	private Date dateRestitution;
	private boolean isMatin;
	private boolean isApresMidi;
	private boolean isJournee;
	private String motif;
	
	public Integer getIdAgent() {
		return idAgent;
	}
	public void setIdAgent(Integer idAgent) {
		this.idAgent = idAgent;
	}
	public Date getDateRestitution() {
		return dateRestitution;
	}
	public void setDateRestitution(Date dateRestitution) {
		this.dateRestitution = dateRestitution;
	}
	public boolean isMatin() {
		return isMatin;
	}
	public void setMatin(boolean isMatin) {
		this.isMatin = isMatin;
	}
	public boolean isApresMidi() {
		return isApresMidi;
	}
	public void setApresMidi(boolean isApresMidi) {
		this.isApresMidi = isApresMidi;
	}
	public boolean isJournee() {
		return isJournee;
	}
	public void setJournee(boolean isJournee) {
		this.isJournee = isJournee;
	}
	public String getMotif() {
		return motif;
	}
	public void setMotif(String motif) {
		this.motif = motif;
	}
	
}
