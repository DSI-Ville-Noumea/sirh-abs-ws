package nc.noumea.mairie.abs.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.CongeAnnuelRestitutionMassive;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

public class RestitutionMassiveDto {

	private Integer idRestitutionMassive;
	@JsonSerialize(using = JsonDateSerializer.class)
	@JsonDeserialize(using = JsonDateDeserializer.class)
	private Date dateRestitution;
	@JsonSerialize(using = JsonDateSerializer.class)
	@JsonDeserialize(using = JsonDateDeserializer.class)
	private Date dateModification;
	private String status;
	private boolean isMatin;
	private boolean isApresMidi;
	private boolean isJournee;
	private String motif;
	
	private List<RestitutionMassiveHistoDto> listHistoAgents;
	
	public RestitutionMassiveDto() {
		this.listHistoAgents = new ArrayList<RestitutionMassiveHistoDto>();
	}
			
	public RestitutionMassiveDto(CongeAnnuelRestitutionMassive histo) {
		this();
		this.idRestitutionMassive  = histo.getIdCongeAnnuelRestitutionMassiveTask();
		this.dateRestitution = histo.getDateRestitution();
		this.dateModification = histo.getDateModification();
		this.motif = histo.getMotif();
		this.isMatin = histo.isMatin();
		this.isApresMidi = histo.isApresMidi();
		this.isJournee = histo.isJournee();
		this.status = histo.getStatus();
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

	public Integer getIdRestitutionMassive() {
		return idRestitutionMassive;
	}

	public void setIdRestitutionMassive(Integer idRestitutionMassive) {
		this.idRestitutionMassive = idRestitutionMassive;
	}

	public Date getDateModification() {
		return dateModification;
	}

	public void setDateModification(Date dateModification) {
		this.dateModification = dateModification;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<RestitutionMassiveHistoDto> getListHistoAgents() {
		return listHistoAgents;
	}

	public void setListHistoAgents(List<RestitutionMassiveHistoDto> listHistoAgents) {
		this.listHistoAgents = listHistoAgents;
	}

}
