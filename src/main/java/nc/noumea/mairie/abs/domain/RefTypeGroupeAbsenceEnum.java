package nc.noumea.mairie.abs.domain;

public enum RefTypeGroupeAbsenceEnum {

	RECUP(1), REPOS_COMP(2), ASA(3), CONGES_EXCEP(4), CONGE_ANNUEL(5), AUTRES(6), MALADIES(7);

	private int type;

	private RefTypeGroupeAbsenceEnum(int _type) {
		type = _type;
	}

	public int getValue() {
		return type;
	}

	public static RefTypeGroupeAbsenceEnum getRefTypeGroupeAbsenceEnum(Integer type) {

		if (type == null)
			return null;

		switch (type) {
			case 1:
				return RECUP;
			case 2:
				return REPOS_COMP;
			case 3:
				return ASA;
			case 4:
				return CONGES_EXCEP;
			case 5:
				return CONGE_ANNUEL;
			case 6:
				return AUTRES;
			case 7:
				return MALADIES;
			default:
				return null;
		}
	}
}
