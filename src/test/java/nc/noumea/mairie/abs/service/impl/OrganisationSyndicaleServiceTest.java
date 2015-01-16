package nc.noumea.mairie.abs.service.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentOrganisationSyndicale;
import nc.noumea.mairie.abs.domain.OrganisationSyndicale;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.dto.OrganisationSyndicaleDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IOrganisationSyndicaleRepository;

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

		IOrganisationSyndicaleRepository organisationRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(organisationRepository.getEntity(OrganisationSyndicale.class, null)).thenReturn(
				new OrganisationSyndicale());

		OrganisationSyndicaleService service = new OrganisationSyndicaleService();
		ReflectionTestUtils.setField(service, "organisationRepository", organisationRepository);

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

		IOrganisationSyndicaleRepository organisationRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(organisationRepository.getEntity(OrganisationSyndicale.class, 1)).thenReturn(org);

		OrganisationSyndicaleService service = new OrganisationSyndicaleService();
		ReflectionTestUtils.setField(service, "organisationRepository", organisationRepository);

		ReturnMessageDto result = service.saveOrganisation(dto);

		assertEquals(0, result.getErrors().size());
		assertEquals(0, result.getInfos().size());
	}

	@Test
	public void getListOrganisationSyndicale_Return1List() {
		// Given
		OrganisationSyndicale org = new OrganisationSyndicale();
		org.setIdOrganisationSyndicale(1);
		org.setLibelle("lib");
		org.setSigle("sigle");
		org.setActif(true);

		List<OrganisationSyndicale> listOrg = new ArrayList<OrganisationSyndicale>();
		listOrg.add(org);

		IOrganisationSyndicaleRepository organisationRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(organisationRepository.findAllOrganisation()).thenReturn(listOrg);

		OrganisationSyndicaleService service = new OrganisationSyndicaleService();
		ReflectionTestUtils.setField(service, "organisationRepository", organisationRepository);

		List<OrganisationSyndicaleDto> result = service.getListOrganisationSyndicale();

		assertEquals(1, result.size());
		assertEquals(org.getLibelle(), result.get(0).getLibelle());
		assertEquals(org.getSigle(), result.get(0).getSigle());
		assertEquals(1, (int) result.get(0).getIdOrganisation());
		assertEquals(org.isActif(), result.get(0).isActif());
	}

	@Test
	public void getListOrganisationSyndicaleActives_ReturnEmptyList() {
		// Given

		IOrganisationSyndicaleRepository organisationRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(organisationRepository.findAllOrganisationActives()).thenReturn(
				new ArrayList<OrganisationSyndicale>());

		OrganisationSyndicaleService service = new OrganisationSyndicaleService();
		ReflectionTestUtils.setField(service, "organisationRepository", organisationRepository);

		List<OrganisationSyndicaleDto> result = service.getListOrganisationSyndicaleActives(9005138,
				RefTypeAbsenceEnum.ASA_A53.getValue());

		assertEquals(0, result.size());
	}

	@Test
	public void getListOrganisationSyndicaleActives_Return1List() {
		// Given
		OrganisationSyndicale org = new OrganisationSyndicale();
		org.setIdOrganisationSyndicale(1);
		org.setLibelle("lib");
		org.setSigle("sigle");
		org.setActif(true);

		List<OrganisationSyndicale> listOrg = new ArrayList<OrganisationSyndicale>();
		listOrg.add(org);

		IOrganisationSyndicaleRepository organisationRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(organisationRepository.findAllOrganisationActives()).thenReturn(listOrg);

		OrganisationSyndicaleService service = new OrganisationSyndicaleService();
		ReflectionTestUtils.setField(service, "organisationRepository", organisationRepository);

		List<OrganisationSyndicaleDto> result = service.getListOrganisationSyndicaleActives(9005138,
				RefTypeAbsenceEnum.ASA_A53.getValue());

		assertEquals(1, result.size());
		assertEquals(org.getLibelle(), result.get(0).getLibelle());
		assertEquals(org.getSigle(), result.get(0).getSigle());
		assertEquals(1, (int) result.get(0).getIdOrganisation());
		assertEquals(org.isActif(), result.get(0).isActif());
	}

	@Test
	public void getListOrganisationSyndicaleActives_Return1List_A52() {
		// Given
		OrganisationSyndicale org = new OrganisationSyndicale();
		org.setIdOrganisationSyndicale(1);
		org.setLibelle("lib");
		org.setSigle("sigle");
		org.setActif(true);

		AgentOrganisationSyndicale agent = new AgentOrganisationSyndicale();
		agent.setOrganisationSyndicale(org);

		List<AgentOrganisationSyndicale> listAgent = new ArrayList<AgentOrganisationSyndicale>();
		listAgent.add(agent);

		IOrganisationSyndicaleRepository organisationRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(organisationRepository.getAgentOrganisationActif(9005138)).thenReturn(listAgent);

		OrganisationSyndicaleService service = new OrganisationSyndicaleService();
		ReflectionTestUtils.setField(service, "organisationRepository", organisationRepository);

		List<OrganisationSyndicaleDto> result = service.getListOrganisationSyndicaleActives(9005138,
				RefTypeAbsenceEnum.ASA_A52.getValue());

		assertEquals(1, result.size());
		assertEquals(org.getLibelle(), result.get(0).getLibelle());
		assertEquals(org.getSigle(), result.get(0).getSigle());
		assertEquals(1, (int) result.get(0).getIdOrganisation());
		assertEquals(org.isActif(), result.get(0).isActif());
	}
}
