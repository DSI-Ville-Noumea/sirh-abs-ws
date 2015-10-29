package nc.noumea.mairie.abs.service.rules.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentAsaA53Count;
import nc.noumea.mairie.abs.domain.DemandeAsa;
import nc.noumea.mairie.abs.domain.OrganisationSyndicale;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.OrganisationSyndicaleDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IAsaRepository;
import nc.noumea.mairie.abs.repository.ICounterRepository;
import nc.noumea.mairie.abs.service.impl.HelperService;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.staticmock.MockStaticEntityMethods;
import org.springframework.test.util.ReflectionTestUtils;

@MockStaticEntityMethods
public class AbsAsaA53DataConsistencyRulesImplTest extends AbsAsaDataConsistencyRulesImplTest {

	@Test
	public void testMethodeParenteHeritage() throws Throwable {

		super.impl = new AbsAsaA53DataConsistencyRulesImpl();
		super.allTest();
	}

	@Test
	public void checkDroitCompteurAsaA53_aucunDroit() {

		Date dateDebut = new Date();
		AgentAsaA53Count soldeAsaA53 = null;

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(1);

		DemandeAsa demande = new DemandeAsa();
		demande.setIdAgent(9005138);
		demande.setDateDebut(dateDebut);
		demande.setOrganisationSyndicale(organisationSyndicale);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getOSCounterByDate(AgentAsaA53Count.class, demande.getOrganisationSyndicale().getIdOrganisationSyndicale(), dateDebut)).thenReturn(soldeAsaA53);

		AbsAsaA53DataConsistencyRulesImpl impl = new AbsAsaA53DataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);

		ReturnMessageDto srm = new ReturnMessageDto();

		srm = impl.checkDroitCompteurAsaA53(srm, demande);

		assertEquals(1, srm.getErrors().size());
		assertEquals(srm.getErrors().get(0), "L'agent [9005138] ne possède pas de droit pour les absences syndicales.");
	}

	@Test
	public void checkDroitCompteurAsaA53_compteurNegatif() {

		Date dateDebut = new Date();
		AgentAsaA53Count soldeAsaA53 = new AgentAsaA53Count();
		soldeAsaA53.setTotalJours(0.0);

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(1);

		DemandeAsa demande = new DemandeAsa();
		demande.setIdAgent(9005138);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(new Date());
		demande.setDuree(10.5);
		demande.setOrganisationSyndicale(organisationSyndicale);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getOSCounterByDate(AgentAsaA53Count.class, demande.getOrganisationSyndicale().getIdOrganisationSyndicale(), dateDebut)).thenReturn(soldeAsaA53);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreJoursArrondiDemiJournee(Mockito.isA(Date.class), Mockito.isA(Date.class))).thenReturn(10.0);

		List<DemandeAsa> listDemandeAsa = new ArrayList<DemandeAsa>();
		listDemandeAsa.addAll(Arrays.asList(new DemandeAsa(), new DemandeAsa()));

		IAsaRepository asaRepository = Mockito.mock(IAsaRepository.class);
		Mockito.when(asaRepository.getListDemandeAsaEnCours(Mockito.anyInt(), Mockito.anyInt(), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyInt())).thenReturn(listDemandeAsa);

		AbsAsaA53DataConsistencyRulesImpl impl = new AbsAsaA53DataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(impl, "helperService", helperService);
		ReflectionTestUtils.setField(impl, "asaRepository", asaRepository);

		ReturnMessageDto srm = new ReturnMessageDto();

		srm = impl.checkDroitCompteurAsaA53(srm, demande);

		assertEquals(0, srm.getInfos().size());
		assertEquals(1, srm.getErrors().size());
		assertEquals(srm.getErrors().get(0), AbsAsaDataConsistencyRulesImpl.DEPASSEMENT_DROITS_ASA_MSG);
	}

	@Test
	public void checkDroitCompteurAsaA53_compteurNegatifBis() {

		Date dateDebut = new Date();
		AgentAsaA53Count soldeAsaA53 = new AgentAsaA53Count();
		soldeAsaA53.setTotalJours(9.0);

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(1);

		DemandeAsa demande = new DemandeAsa();
		demande.setIdAgent(9005138);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(new Date());
		demande.setDuree(10.5);
		demande.setOrganisationSyndicale(organisationSyndicale);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getOSCounterByDate(AgentAsaA53Count.class, demande.getOrganisationSyndicale().getIdOrganisationSyndicale(), dateDebut)).thenReturn(soldeAsaA53);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreJoursArrondiDemiJournee(Mockito.isA(Date.class), Mockito.isA(Date.class))).thenReturn(10.0);

		List<DemandeAsa> listDemandeAsa = new ArrayList<DemandeAsa>();
		listDemandeAsa.addAll(Arrays.asList(new DemandeAsa(), new DemandeAsa()));

		IAsaRepository asaRepository = Mockito.mock(IAsaRepository.class);
		Mockito.when(asaRepository.getListDemandeAsaEnCours(Mockito.anyInt(), Mockito.anyInt(), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyInt())).thenReturn(listDemandeAsa);

		AbsAsaA53DataConsistencyRulesImpl impl = new AbsAsaA53DataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(impl, "helperService", helperService);
		ReflectionTestUtils.setField(impl, "asaRepository", asaRepository);

		ReturnMessageDto srm = new ReturnMessageDto();

		srm = impl.checkDroitCompteurAsaA53(srm, demande);

		assertEquals(0, srm.getInfos().size());
		assertEquals(1, srm.getErrors().size());
		assertEquals(srm.getErrors().get(0), AbsAsaDataConsistencyRulesImpl.DEPASSEMENT_DROITS_ASA_MSG);
	}

	@Test
	public void checkDroitCompteurAsaA53_ok() {

		Date dateDebut = new Date();
		AgentAsaA53Count soldeAsaA53 = new AgentAsaA53Count();
		soldeAsaA53.setTotalJours(10.5);

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(1);

		DemandeAsa demande = new DemandeAsa();
		demande.setIdAgent(9005138);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(new Date());
		demande.setDuree(10.5);
		demande.setOrganisationSyndicale(organisationSyndicale);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getOSCounterByDate(AgentAsaA53Count.class, demande.getOrganisationSyndicale().getIdOrganisationSyndicale(), dateDebut)).thenReturn(soldeAsaA53);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreJoursArrondiDemiJournee(Mockito.isA(Date.class), Mockito.isA(Date.class))).thenReturn(10.0);

		List<DemandeAsa> listDemandeAsa = new ArrayList<DemandeAsa>();
		listDemandeAsa.addAll(Arrays.asList(new DemandeAsa(), new DemandeAsa()));

		IAsaRepository asaRepository = Mockito.mock(IAsaRepository.class);
		Mockito.when(asaRepository.getListDemandeAsaEnCours(Mockito.anyInt(), Mockito.anyInt(), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyInt())).thenReturn(listDemandeAsa);

		AbsAsaA53DataConsistencyRulesImpl impl = new AbsAsaA53DataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(impl, "helperService", helperService);
		ReflectionTestUtils.setField(impl, "asaRepository", asaRepository);

		ReturnMessageDto srm = new ReturnMessageDto();

		srm = impl.checkDroitCompteurAsaA53(srm, demande);

		assertEquals(0, srm.getErrors().size());
		assertEquals(0, srm.getInfos().size());
	}

	@Test
	public void checkDepassementCompteurAgent_aucunCompteur() {

		Date dateDebut = new Date();
		AgentAsaA53Count soldeAsaA53 = null;

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(9005138);

		OrganisationSyndicaleDto orgaDto = new OrganisationSyndicaleDto();
		orgaDto.setIdOrganisation(1);

		DemandeDto demande = new DemandeDto();
		demande.setAgentWithServiceDto(agDto);
		demande.setDateDebut(dateDebut);
		demande.setOrganisationSyndicale(orgaDto);
		demande.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getOSCounterByDate(AgentAsaA53Count.class, demande.getOrganisationSyndicale().getIdOrganisation(), dateDebut)).thenReturn(soldeAsaA53);

		AbsAsaA53DataConsistencyRulesImpl impl = new AbsAsaA53DataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);

		boolean srm = impl.checkDepassementCompteurAgent(demande, null);

		assertTrue(srm);
	}

	@Test
	public void checkDepassementCompteurAgent_compteurNegatif() {

		Date dateDebut = new Date();
		AgentAsaA53Count soldeAsaA53 = new AgentAsaA53Count();
		soldeAsaA53.setTotalJours(0.0);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(9005138);

		OrganisationSyndicaleDto orgaDto = new OrganisationSyndicaleDto();
		orgaDto.setIdOrganisation(1);

		DemandeDto demande = new DemandeDto();
		demande.setAgentWithServiceDto(agDto);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(new Date());
		demande.setDuree(10.5);
		demande.setIdTypeDemande(7);
		demande.setOrganisationSyndicale(orgaDto);
		demande.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getOSCounterByDate(AgentAsaA53Count.class, demande.getOrganisationSyndicale().getIdOrganisation(), dateDebut)).thenReturn(soldeAsaA53);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreJoursArrondiDemiJournee(Mockito.isA(Date.class), Mockito.isA(Date.class))).thenReturn(10.0);

		List<DemandeAsa> listDemandeAsa = new ArrayList<DemandeAsa>();
		listDemandeAsa.addAll(Arrays.asList(new DemandeAsa(), new DemandeAsa()));

		IAsaRepository asaRepository = Mockito.mock(IAsaRepository.class);
		Mockito.when(asaRepository.getListDemandeAsaEnCours(Mockito.anyInt(), Mockito.anyInt(), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyInt())).thenReturn(listDemandeAsa);

		AbsAsaA53DataConsistencyRulesImpl impl = new AbsAsaA53DataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(impl, "helperService", helperService);
		ReflectionTestUtils.setField(impl, "asaRepository", asaRepository);

		boolean srm = impl.checkDepassementCompteurAgent(demande, null);

		assertTrue(srm);
	}

	@Test
	public void checkDepassementCompteurAgent_compteurOk() {

		Date dateDebut = new Date();
		AgentAsaA53Count soldeAsaA53 = new AgentAsaA53Count();
		soldeAsaA53.setTotalJours(3.0);

		AgentWithServiceDto agDto = new AgentWithServiceDto();
		agDto.setIdAgent(9005138);

		OrganisationSyndicaleDto orgaDto = new OrganisationSyndicaleDto();
		orgaDto.setIdOrganisation(1);

		DemandeDto demande = new DemandeDto();
		demande.setAgentWithServiceDto(agDto);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(new Date());
		demande.setDuree(1.5);
		demande.setIdTypeDemande(7);
		demande.setOrganisationSyndicale(orgaDto);
		demande.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getOSCounterByDate(AgentAsaA53Count.class, demande.getOrganisationSyndicale().getIdOrganisation(), dateDebut)).thenReturn(soldeAsaA53);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreJoursArrondiDemiJournee(Mockito.isA(Date.class), Mockito.isA(Date.class))).thenReturn(1.0);

		List<DemandeAsa> listDemandeAsa = new ArrayList<DemandeAsa>();
		listDemandeAsa.addAll(Arrays.asList(new DemandeAsa(), new DemandeAsa()));

		IAsaRepository asaRepository = Mockito.mock(IAsaRepository.class);
		Mockito.when(asaRepository.getListDemandeAsaEnCours(Mockito.anyInt(), Mockito.anyInt(), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyInt())).thenReturn(listDemandeAsa);

		AbsAsaA53DataConsistencyRulesImpl impl = new AbsAsaA53DataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(impl, "helperService", helperService);
		ReflectionTestUtils.setField(impl, "asaRepository", asaRepository);

		boolean srm = impl.checkDepassementCompteurAgent(demande, null);

		assertFalse(srm);
	}
}
