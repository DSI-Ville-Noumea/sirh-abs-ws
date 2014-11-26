package nc.noumea.mairie.abs.dto;

import java.util.ArrayList;
import java.util.List;

import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeSaisi;
import nc.noumea.mairie.abs.domain.RefTypeSaisiCongeAnnuel;

public class RefTypeAbsenceDto {

	private Integer idRefTypeAbsence;
	private String libelle;
	private RefGroupeAbsenceDto groupeAbsence;
	private RefTypeSaisiDto typeSaisiDto;
	private List<RefTypeSaisiCongeAnnuelDto> listeTypeSaisiCongeAnnuelDto;

	public RefTypeAbsenceDto() {
	}

	public RefTypeAbsenceDto(RefTypeAbsence type) {
		super();
		this.idRefTypeAbsence = type.getIdRefTypeAbsence();
		this.libelle = type.getLabel();
		if (null != type.getGroupe()) {
			this.groupeAbsence = new RefGroupeAbsenceDto(type.getGroupe());
		}
	}

	public RefTypeAbsenceDto(RefTypeAbsence type, RefTypeSaisi typeSaisi,
			List<RefTypeSaisiCongeAnnuel> listeTypeSaisieCongeAnnuel) {
		super();
		this.idRefTypeAbsence = type.getIdRefTypeAbsence();
		this.libelle = type.getLabel();
		if (null != type.getGroupe()) {
			this.groupeAbsence = new RefGroupeAbsenceDto(type.getGroupe());
		}
		if (null != typeSaisi) {
			this.typeSaisiDto = new RefTypeSaisiDto(typeSaisi);
		}
		if (null != listeTypeSaisieCongeAnnuel) {
			this.listeTypeSaisiCongeAnnuelDto = new ArrayList<RefTypeSaisiCongeAnnuelDto>();
			for (RefTypeSaisiCongeAnnuel t : listeTypeSaisieCongeAnnuel) {
				this.listeTypeSaisiCongeAnnuelDto.add(new RefTypeSaisiCongeAnnuelDto(t));
			}
		}
	}

	public String getLibelle() {
		return libelle;
	}

	public void setLibelle(String libelle) {
		this.libelle = libelle;
	}

	public Integer getIdRefTypeAbsence() {
		return idRefTypeAbsence;
	}

	public void setIdRefTypeAbsence(Integer idRefTypeAbsence) {
		this.idRefTypeAbsence = idRefTypeAbsence;
	}

	public RefTypeSaisiDto getTypeSaisiDto() {
		return typeSaisiDto;
	}

	public void setTypeSaisiDto(RefTypeSaisiDto typeSaisiDto) {
		this.typeSaisiDto = typeSaisiDto;
	}

	public RefGroupeAbsenceDto getGroupeAbsence() {
		return groupeAbsence;
	}

	public void setGroupeAbsence(RefGroupeAbsenceDto groupeAbsence) {
		this.groupeAbsence = groupeAbsence;
	}

	public List<RefTypeSaisiCongeAnnuelDto> getListeTypeSaisiCongeAnnuelDto() {
		return listeTypeSaisiCongeAnnuelDto;
	}

	public void setListeTypeSaisiCongeAnnuelDto(List<RefTypeSaisiCongeAnnuelDto> listeTypeSaisiCongeAnnuelDto) {
		this.listeTypeSaisiCongeAnnuelDto = listeTypeSaisiCongeAnnuelDto;
	}

}
