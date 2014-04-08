package nc.noumea.mairie.abs.dto;

import nc.noumea.mairie.abs.domain.RefTypeSaisi;

public class RefTypeSaisiDto {

	private Integer idRefTypeDemande; 
	private boolean calendarDateDebut;
	private boolean calendarHeureDebut;
	private boolean chkDateDebut;
	private boolean calendarDateFin;
	private boolean calendarHeureFin;
	private boolean chkDateFin;
	private boolean duree;
	private boolean pieceJointe;
	
	public RefTypeSaisiDto() {
	}
	
	public RefTypeSaisiDto(RefTypeSaisi typeSaisi){
		this();
		this.idRefTypeDemande = typeSaisi.getType().getIdRefTypeAbsence();
		this.calendarDateDebut = typeSaisi.isCalendarDateDebut();
		this.calendarHeureDebut = typeSaisi.isCalendarHeureDebut();
		this.chkDateDebut = typeSaisi.isChkDateDebut();
		this.calendarDateFin = typeSaisi.isCalendarDateFin();
		this.calendarHeureFin = typeSaisi.isCalendarHeureFin();
		this.chkDateFin = typeSaisi.isChkDateFin();
		this.duree = typeSaisi.isDuree();
		this.pieceJointe = typeSaisi.isPieceJointe();
	}

	public Integer getIdRefTypeDemande() {
		return idRefTypeDemande;
	}

	public void setIdRefTypeDemande(Integer idRefTypeDemande) {
		this.idRefTypeDemande = idRefTypeDemande;
	}

	public boolean isCalendarDateDebut() {
		return calendarDateDebut;
	}

	public void setCalendarDateDebut(boolean calendarDateDebut) {
		this.calendarDateDebut = calendarDateDebut;
	}

	public boolean isCalendarHeureDebut() {
		return calendarHeureDebut;
	}

	public void setCalendarHeureDebut(boolean calendarHeureDebut) {
		this.calendarHeureDebut = calendarHeureDebut;
	}

	public boolean isChkDateDebut() {
		return chkDateDebut;
	}

	public void setChkDateDebut(boolean chkDateDebut) {
		this.chkDateDebut = chkDateDebut;
	}

	public boolean isCalendarDateFin() {
		return calendarDateFin;
	}

	public void setCalendarDateFin(boolean calendarDateFin) {
		this.calendarDateFin = calendarDateFin;
	}

	public boolean isCalendarHeureFin() {
		return calendarHeureFin;
	}

	public void setCalendarHeureFin(boolean calendarHeureFin) {
		this.calendarHeureFin = calendarHeureFin;
	}

	public boolean isChkDateFin() {
		return chkDateFin;
	}

	public void setChkDateFin(boolean chkDateFin) {
		this.chkDateFin = chkDateFin;
	}

	public boolean isDuree() {
		return duree;
	}

	public void setDuree(boolean duree) {
		this.duree = duree;
	}

	public boolean isPieceJointe() {
		return pieceJointe;
	}

	public void setPieceJointe(boolean pieceJointe) {
		this.pieceJointe = pieceJointe;
	}
	
	
}
