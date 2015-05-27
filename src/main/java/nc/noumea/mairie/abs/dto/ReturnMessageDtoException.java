package nc.noumea.mairie.abs.dto;

public class ReturnMessageDtoException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7722385301562786677L;
	
	ReturnMessageDto erreur;

	public ReturnMessageDtoException() {
		super();
	}

	public ReturnMessageDtoException(ReturnMessageDto returnDto) {
		erreur = returnDto;
	}

	public ReturnMessageDto getErreur() {
		return erreur;
	}

	public void setErreur(ReturnMessageDto erreur) {
		this.erreur = erreur;
	}
}
