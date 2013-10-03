package nc.noumea.mairie.abs.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Given Date is not a monday")
public class NotAMondayException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2981536413524197722L;

}
