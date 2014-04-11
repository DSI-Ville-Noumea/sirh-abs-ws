package nc.noumea.mairie.abs.service.impl;

import java.util.ArrayList;
import java.util.List;

import nc.noumea.mairie.abs.domain.OrganisationSyndicale;
import nc.noumea.mairie.abs.dto.OrganisationSyndicaleDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IOrganisationSyndicaleRepository;
import nc.noumea.mairie.abs.service.IOrganisationSyndicaleService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrganisationSyndicaleService implements IOrganisationSyndicaleService {

	private Logger logger = LoggerFactory.getLogger(OrganisationSyndicaleService.class);

	@Autowired
	private IOrganisationSyndicaleRepository organisationRepository;

	@Override
	public ReturnMessageDto saveOrganisation(OrganisationSyndicaleDto dto) {

		logger.info("Trying to save organisation sigle {} ...", dto.getSigle());

		ReturnMessageDto result = new ReturnMessageDto();

		// on cherche la demande
		OrganisationSyndicale organisationSyndicale = getOrganisationSyndicale(OrganisationSyndicale.class,
				dto.getIdOrganisation());
		if (organisationSyndicale == null) {
			organisationSyndicale = new OrganisationSyndicale();
		}
		organisationSyndicale.setLibelle(dto.getLibelle());
		organisationSyndicale.setSigle(dto.getSigle());
		organisationSyndicale.setActif(dto.isActif());

		// insert nouvelle ligne Organisation syndicale
		organisationRepository.persistEntity(organisationSyndicale);

		logger.info("Updated/Added OrganisationSyndicale id {}.", organisationSyndicale.getIdOrganisationSyndicale());

		return result;
	}

	protected <T> T getOrganisationSyndicale(Class<T> Tclass, Integer idOrganisation) {
		if (null != idOrganisation) {
			return organisationRepository.getEntity(Tclass, idOrganisation);
		}

		try {
			return Tclass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			return null;
		}
	}

	@Override
	public List<OrganisationSyndicaleDto> getListOrganisationSyndicale() {
		List<OrganisationSyndicaleDto> res = new ArrayList<OrganisationSyndicaleDto>();
		List<OrganisationSyndicale> listOrganisation = organisationRepository.findAllOrganisation();

		for (OrganisationSyndicale org : listOrganisation) {

			OrganisationSyndicaleDto dto = new OrganisationSyndicaleDto(org);
			res.add(dto);

		}
		return res;
	}

	@Override
	public List<OrganisationSyndicaleDto> getListOrganisationSyndicaleActives() {
		List<OrganisationSyndicaleDto> res = new ArrayList<OrganisationSyndicaleDto>();
		List<OrganisationSyndicale> listOrganisation = organisationRepository.findAllOrganisationActives();

		for (OrganisationSyndicale org : listOrganisation) {

			OrganisationSyndicaleDto dto = new OrganisationSyndicaleDto(org);
			res.add(dto);

		}
		return res;
	}

}
