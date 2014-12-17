package nc.noumea.mairie.abs.service;

import java.util.List;

import nc.noumea.mairie.abs.dto.OrganisationSyndicaleDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

public interface IOrganisationSyndicaleService {

	ReturnMessageDto saveOrganisation(OrganisationSyndicaleDto dto);

	List<OrganisationSyndicaleDto> getListOrganisationSyndicale();

	List<OrganisationSyndicaleDto> getListOrganisationSyndicaleActives(Integer idAgent, Integer idRefTypeAbsence);
}
