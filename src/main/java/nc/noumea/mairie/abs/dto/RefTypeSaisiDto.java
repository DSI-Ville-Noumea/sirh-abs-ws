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
	
	private boolean fonctionnaire;
	private boolean contractuel;
	private boolean conventionCollective;
	private boolean compteurCollectif;
	private boolean saisieKiosque;
	private String description;
	private String infosComplementaires;
	private boolean alerte;
	private String messageAlerte;
	private Integer quotaMax;
	private UnitePeriodeQuotaDto unitePeriodeQuotaDto;
	private String uniteDecompte;
	
	public RefTypeSaisiDto() {
	}
	
	public RefTypeSaisiDto(RefTypeSaisi typeSaisi){
		this();
		if(null != typeSaisi && null != typeSaisi.getType()) 
			this.idRefTypeDemande = typeSaisi.getType().getIdRefTypeAbsence();
		
		this.calendarDateDebut = typeSaisi.isCalendarDateDebut();
		this.calendarHeureDebut = typeSaisi.isCalendarHeureDebut();
		this.chkDateDebut = typeSaisi.isChkDateDebut();
		this.calendarDateFin = typeSaisi.isCalendarDateFin();
		this.calendarHeureFin = typeSaisi.isCalendarHeureFin();
		this.chkDateFin = typeSaisi.isChkDateFin();
		this.duree = typeSaisi.isDuree();
		this.pieceJointe = typeSaisi.isPieceJointe();
		
		this.fonctionnaire = typeSaisi.isFonctionnaire();
		this.contractuel = typeSaisi.isContractuel();
		this.conventionCollective = typeSaisi.isConventionCollective();
		this.compteurCollectif = typeSaisi.isCompteurCollectif();
		this.saisieKiosque = typeSaisi.isSaisieKiosque();
		this.description = typeSaisi.getDescription();
		this.infosComplementaires = typeSaisi.getInfosComplementaires();
		this.alerte = typeSaisi.isAlerte();
		this.messageAlerte = typeSaisi.getMessageAlerte();
		this.quotaMax = typeSaisi.getQuotaMax();
		this.uniteDecompte = typeSaisi.getUniteDecompte();
		
		if(null != typeSaisi.getRefUnitePeriodeQuota()) {
			this.unitePeriodeQuotaDto = new UnitePeriodeQuotaDto(typeSaisi.getRefUnitePeriodeQuota());
		}
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

	public boolean isFonctionnaire() {
		return fonctionnaire;
	}

	public void setFonctionnaire(boolean fonctionnaire) {
		this.fonctionnaire = fonctionnaire;
	}

	public boolean isContractuel() {
		return contractuel;
	}

	public void setContractuel(boolean contractuel) {
		this.contractuel = contractuel;
	}

	public boolean isConventionCollective() {
		return conventionCollective;
	}

	public void setConventionCollective(boolean conventionCollective) {
		this.conventionCollective = conventionCollective;
	}

	public boolean isSaisieKiosque() {
		return saisieKiosque;
	}

	public void setSaisieKiosque(boolean saisieKiosque) {
		this.saisieKiosque = saisieKiosque;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getInfosComplementaires() {
		return infosComplementaires;
	}

	public void setInfosComplementaires(String infosComplementaires) {
		this.infosComplementaires = infosComplementaires;
	}

	public boolean isAlerte() {
		return alerte;
	}

	public void setAlerte(boolean alerte) {
		this.alerte = alerte;
	}

	public String getMessageAlerte() {
		return messageAlerte;
	}

	public void setMessageAlerte(String messageAlerte) {
		this.messageAlerte = messageAlerte;
	}

	public Integer getQuotaMax() {
		return quotaMax;
	}

	public void setQuotaMax(Integer quotaMax) {
		this.quotaMax = quotaMax;
	}

	public UnitePeriodeQuotaDto getUnitePeriodeQuotaDto() {
		return unitePeriodeQuotaDto;
	}

	public void setUnitePeriodeQuotaDto(UnitePeriodeQuotaDto unitePeriodeQuotaDto) {
		this.unitePeriodeQuotaDto = unitePeriodeQuotaDto;
	}

	public String getUniteDecompte() {
		return uniteDecompte;
	}

	public void setUniteDecompte(String uniteDecompte) {
		this.uniteDecompte = uniteDecompte;
	}

	public boolean isCompteurCollectif() {
		return compteurCollectif;
	}

	public void setCompteurCollectif(boolean compteurCollectif) {
		this.compteurCollectif = compteurCollectif;
	}
	
	
}
