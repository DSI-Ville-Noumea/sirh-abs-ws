package nc.noumea.mairie.abs.domain;

public enum RefTypeAbsenceEnum {

	CONGE_ANNUEL(1), REPOS_COMP(2), RECUP(3), MALADIES(6), ASA_A48(7), ASA_A54(8), ASA_A55(9), ASA_A52(10), ASA_A53(11), ASA_A49(12), ASA_A50(
			13), ASA_AMICALE(69), CE_CONGE_UNIQUE(44), CE_CONGE_UNIQUE_CCSP(45);

	private int type;

	private RefTypeAbsenceEnum(int _type) {
		type = _type;
	}

	public int getValue() {
		return type;
	}

	public static RefTypeAbsenceEnum getRefTypeAbsenceEnum(Integer type) {

		if (type == null)
			return null;

		switch (type) {
			case 1:
				return CONGE_ANNUEL;
			case 2:
				return REPOS_COMP;
			case 3:
				return RECUP;
			case 6:
				return MALADIES;
			case 7:
				return ASA_A48;
			case 8:
				return ASA_A54;
			case 9:
				return ASA_A55;
			case 10:
				return ASA_A52;
			case 11:
				return ASA_A53;
			case 12:
				return ASA_A49;
			case 13:
				return ASA_A50;
			case 69:
				return ASA_AMICALE;
			case 44:
				return CE_CONGE_UNIQUE;
			case 45:
				return CE_CONGE_UNIQUE_CCSP;
			default:
				return null;
		}
	}
}
