package nc.noumea.mairie.abs.service;

import nc.noumea.mairie.abs.dto.OrganisationSyndicaleDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

public interface IOrganisationSyndicaleService {

	ReturnMessageDto saveOrganisation(OrganisationSyndicaleDto dto);
}
