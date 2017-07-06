package nc.noumea.mairie.abs.service;

import nc.noumea.mairie.abs.dto.EmailInfoDto;

public interface IEmailService {

	EmailInfoDto getListIdDestinatairesEmailInfo();
	
	EmailInfoDto getListIdApprobateursEmailMaladie();
}
