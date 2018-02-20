package nc.noumea.mairie.abs.enums;


/**
 * #44736 : Created to authorize thoses kind of illness to be extended
 * @author teo
 *
 */
public enum AuthorizedMaladiesForProlongationEnum {

		HOSPITALISATION(71),
		CONVALESCENCE(72),
		EVASAN(73),
		CONGE_LONGUE_DUREE(75),
		CONGE_LONGUE_MALADIE(76),
		MALADIE(81),
		CONGE_DE_CONVALESCENCE(83);
	
	private Integer code;

	private AuthorizedMaladiesForProlongationEnum(Integer _code) {
		code = _code;
	}

	public Integer getCode() {
		return code;
	}

	@Override
	public String toString() {
		return code.toString();
	}
}
