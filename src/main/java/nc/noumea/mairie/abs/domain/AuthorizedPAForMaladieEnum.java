package nc.noumea.mairie.abs.domain;

/**
 * Cette enum contient les codes des PA pour lesquelles il est possible de renseigner une absence maladie.
 * Cette liste est alimentée avec la liste de la redmine #39402 :
 * 
 * https://redmine.ville-noumea.nc/issues/39402
 * 
 * @author teo
 */

public enum AuthorizedPAForMaladieEnum {

	ACTIVITE_NORMALE("01"),
	ACTIVITE_NORMALE_DESINDEXEE("04"),
	MI_TEMPS_THERAPEUTIQUE_CV("23"),
	MI_TEMPS_THERAPEUTIQUE("24"),
	CONGE_MALADIE_DEMI_SALAIRE("25"),
	CONGE_MALADIE_NON_REMUNERE("26"),
	CONGE_DE_CONVALESCENCE("28"),
	CONGE_MALADIE_LONGUE_DUREE("30"),
	CONGE_MALADIE_LONGUE_DUREE_NON_REMUNERE("31"),
	CONGE_MALADIE_LONGUE_DUREE_DEMI_SALAIRE("32"),
	CONGE_LONGUE_MALADIE("35"),
	CONGE_LONGUE_MALADIE_DEMI_SALAIRE("36"),
	CONGE_LONGUE_MALADIE_NON_REMUNERE("37"),
	MALADIE_PROFESSIONNELLE("40"),
	ACCIDENT_DU_TRAVAIL("41"),
	CONGE_PRENATAL_POUR_GROSSESSE_DIFFICILE("44"),
	CONGE_POST_NATAL("47"),
	MISE_A_DISPOSITION_NOUMEA("56"),
	MISSION("60"),
	STAGE_FRANCE_PLEIN_SALAIRE("61"),
	STAGE_FRANCE_MI_SALAIRE("62"),
	STAGE_NON_REMUNERE("63"),
	STAGE_ETRANGER("64"),
	STAGE_FRANCE("65");
	
	private String code;

	private AuthorizedPAForMaladieEnum(String _code) {
		code = _code;
	}

	public String getCode() {
		return code;
	}

	@Override
	public String toString() {
		return code;
	}
	
}
