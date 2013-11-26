package nc.noumea.mairie.abs.domain;

public enum ProfilEnum {

	OPERATEUR("OPERATEUR"), VISEUR("VISEUR"), APPROBATEUR("APPROBATEUR"), DELEGATAIRE("DELEGATAIRE");

	private String profilName;

	private ProfilEnum(String _profilName) {
		profilName = _profilName;
	}

	@Override
	public String toString() {
		return profilName;
	}
}
