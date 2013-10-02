package nc.noumea.mairie.abs.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AgentNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5519081357309153348L;

}
