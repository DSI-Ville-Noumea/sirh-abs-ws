package nc.noumea.mairie.abs.service.impl;

import static org.junit.Assert.assertEquals;
import nc.noumea.mairie.abs.domain.OrganisationSyndicale;
import nc.noumea.mairie.abs.dto.OrganisationSyndicaleDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IDemandeRepository;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class OrganisationSyndicaleServiceTest {

	@Test
	public void saveOrganisation_newOrganisation() {
		// Given
		OrganisationSyndicaleDto dto = new OrganisationSyndicaleDto();
		dto.setIdOrganisation(null);
		dto.setLibelle("lib");
		dto.setSigle("sigle");
		dto.setActif(true);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(OrganisationSyndicale.class, null)).thenReturn(
				new OrganisationSyndicale());

		OrganisationSyndicaleService service = new OrganisationSyndicaleService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		ReturnMessageDto result = service.saveOrganisation(dto);

		assertEquals(0, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
	}

	@Test
	public void saveOrganisation_updateOrganisation() {
		// Given
		OrganisationSyndicaleDto dto = new OrganisationSyndicaleDto();
		dto.setIdOrganisation(1);
		dto.setLibelle("lib");
		dto.setSigle("sigle");
		dto.setActif(false);

		OrganisationSyndicale org = new OrganisationSyndicale();
		org.setIdOrganisationSyndicale(1);
		org.setLibelle("lib");
		org.setSigle("sigle");
		org.setActif(true);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(OrganisationSyndicale.class, 1)).thenReturn(org);

		OrganisationSyndicaleService service = new OrganisationSyndicaleService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		ReturnMessageDto result = service.saveOrganisation(dto);

		assertEquals(0, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
	}
}
