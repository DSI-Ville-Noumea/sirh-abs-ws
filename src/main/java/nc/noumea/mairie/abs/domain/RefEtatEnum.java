package nc.noumea.mairie.abs.domain;

public enum RefEtatEnum {
	
	PROVISOIRE(0), SAISIE(1), VISEE_FAVORABLE(2), VISEE_DEFAVORABLE(3),
	APPROUVEE(4), REFUSEE(5), PRISE(6);

	private int codeEtat;

	RefEtatEnum(int _value) {
		codeEtat = _value;
	}

	public int getCodeEtat() {
		return codeEtat;
	}

	@Override
	public String toString() {
		return String.valueOf(codeEtat);
	}

	public static RefEtatEnum getRefEtatEnum(Integer codeEtat) {

		if (codeEtat == null)
			return null;
		
		switch (codeEtat) {
			case 0:
				return PROVISOIRE;
			case 1:
				return SAISIE;
			case 2:
				return VISEE_FAVORABLE;
			case 3:
				return VISEE_DEFAVORABLE;
			case 4:
				return APPROUVEE;
			case 5:
				return REFUSEE;
			case 6:
				return PRISE;
			default:
				return null;
		}
	}
}