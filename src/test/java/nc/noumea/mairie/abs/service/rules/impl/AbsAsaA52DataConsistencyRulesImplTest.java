package nc.noumea.mairie.abs.service.rules.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentAsaA52Count;
import nc.noumea.mairie.abs.domain.AgentOrganisationSyndicale;
import nc.noumea.mairie.abs.domain.DemandeAsa;
import nc.noumea.mairie.abs.domain.OrganisationSyndicale;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.OrganisationSyndicaleDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IAsaRepository;
import nc.noumea.mairie.abs.repository.ICounterRepository;
import nc.noumea.mairie.abs.repository.IOrganisationSyndicaleRepository;
import nc.noumea.mairie.abs.service.IAbsenceDataConsistencyRules;
import nc.noumea.mairie.abs.service.impl.HelperService;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.staticmock.MockStaticEntityMethods;
import org.springframework.test.util.ReflectionTestUtils;

@MockStaticEntityMethods
public class AbsAsaA52DataConsistencyRulesImplTest extends AbsAsaDataConsistencyRulesImplTest {

	@Test
	public void testMethodeParenteHeritage() throws Throwable {

		super.impl = new AbsAsaA52DataConsistencyRulesImpl();
		super.allTest();
	}

	@Test
	public void checkDroitCompteurAsaA52_aucunDroit() {

		Date dateDebut = new Date();
		AgentAsaA52Count soldeAsaA52 = null;

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(1);

		DemandeAsa demande = new DemandeAsa();
		demande.setIdAgent(9005138);
		demande.setDateDebut(dateDebut);
		demande.setOrganisationSyndicale(organisationSyndicale);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getOSCounterByDate(AgentAsaA52Count.class, organisationSyndicale.getIdOrganisationSyndicale(), dateDebut)).thenReturn(soldeAsaA52);

		AbsAsaA52DataConsistencyRulesImpl impl = new AbsAsaA52DataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);

		ReturnMessageDto srm = new ReturnMessageDto();

		srm = impl.checkDroitCompteurAsaA52(srm, demande);

		assertEquals(1, srm.getErrors().size());
		assertEquals(srm.getErrors().get(0), "L'agent [9005138] ne possède pas de droit pour les absences syndicales.");
	}

	@Test
	public void checkDroitCompteurAsaA52_aucunDroitWithOrganisationSyndicale() {

		Date dateDebut = new Date();
		AgentAsaA52Count soldeAsaA52 = new AgentAsaA52Count();

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(1);

		DemandeAsa demande = new DemandeAsa();
		demande.setIdAgent(9005138);
		demande.setDateDebut(dateDebut);
		demande.setOrganisationSyndicale(organisationSyndicale);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getOSCounterByDate(AgentAsaA52Count.class, organisationSyndicale.getIdOrganisationSyndicale(), dateDebut)).thenReturn(soldeAsaA52);

		IOrganisationSyndicaleRepository organisationSyndicaleRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(organisationSyndicaleRepository.getAgentOrganisation(Mockito.anyInt(), Mockito.anyInt())).thenReturn(null);

		AbsAsaA52DataConsistencyRulesImpl impl = new AbsAsaA52DataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(impl, "organisationSyndicaleRepository", organisationSyndicaleRepository);

		ReturnMessageDto srm = new ReturnMessageDto();

		srm = impl.checkDroitCompteurAsaA52(srm, demande);

		assertEquals(1, srm.getErrors().size());
		assertEquals(srm.getErrors().get(0), "L'agent [9005138] ne possède pas de droit pour les absences syndicales.");
	}

	@Test
	public void checkDroitCompteurAsaA52_aucunDroitWithOrganisationSyndicale_NotActif() {

		Date dateDebut = new Date();
		AgentAsaA52Count soldeAsaA52 = new AgentAsaA52Count();

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(1);

		DemandeAsa demande = new DemandeAsa();
		demande.setIdAgent(9005138);
		demande.setDateDebut(dateDebut);
		demande.setOrganisationSyndicale(organisationSyndicale);

		AgentOrganisationSyndicale agentOrga = new AgentOrganisationSyndicale();
		agentOrga.setActif(false);
		agentOrga.setIdAgent(9005138);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getOSCounterByDate(AgentAsaA52Count.class, organisationSyndicale.getIdOrganisationSyndicale(), dateDebut)).thenReturn(soldeAsaA52);

		IOrganisationSyndicaleRepository organisationSyndicaleRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(organisationSyndicaleRepository.getAgentOrganisation(Mockito.anyInt(), Mockito.anyInt())).thenReturn(agentOrga);

		AbsAsaA52DataConsistencyRulesImpl impl = new AbsAsaA52DataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(impl, "organisationSyndicaleRepository", organisationSyndicaleRepository);

		ReturnMessageDto srm = new ReturnMessageDto();

		srm = impl.checkDroitCompteurAsaA52(srm, demande);

		assertEquals(1, srm.getErrors().size());
		assertEquals(srm.getErrors().get(0), "L'agent [9005138] ne possède pas de droit pour les absences syndicales.");
	}

	@Test
	public void checkDroitCompteurAsaA52_compteurNegatif() {

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(1);

		Date dateDebut = new Date();
		AgentAsaA52Count soldeAsaA52 = new AgentAsaA52Count();
		soldeAsaA52.setTotalMinutes(0);
		soldeAsaA52.setOrganisationSyndicale(organisationSyndicale);

		DemandeAsa demande = new DemandeAsa();
		demande.setIdAgent(9005138);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(dateDebut);
		demande.setOrganisationSyndicale(organisationSyndicale);
		demande.setDuree(10.0);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getOSCounterByDate(AgentAsaA52Count.class, organisationSyndicale.getIdOrganisationSyndicale(), dateDebut)).thenReturn(soldeAsaA52);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreMinutes(Mockito.isA(Date.class), Mockito.isA(Date.class))).thenReturn(0);

		List<DemandeAsa> listDemandeAsa = new ArrayList<DemandeAsa>();
		listDemandeAsa.addAll(Arrays.asList(new DemandeAsa(), new DemandeAsa()));

		AgentOrganisationSyndicale agentOrga = new AgentOrganisationSyndicale();
		agentOrga.setActif(true);
		agentOrga.setIdAgent(9005138);

		IAsaRepository asaRepository = Mockito.mock(IAsaRepository.class);
		Mockito.when(asaRepository.getListDemandeAsaEnCours(Mockito.anyInt(), Mockito.anyInt(), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyInt())).thenReturn(listDemandeAsa);

		IOrganisationSyndicaleRepository organisationSyndicaleRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(organisationSyndicaleRepository.getAgentOrganisation(Mockito.anyInt(), Mockito.anyInt())).thenReturn(agentOrga);

		AbsAsaA52DataConsistencyRulesImpl impl = new AbsAsaA52DataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(impl, "helperService", helperService);
		ReflectionTestUtils.setField(impl, "asaRepository", asaRepository);
		ReflectionTestUtils.setField(impl, "organisationSyndicaleRepository", organisationSyndicaleRepository);

		ReturnMessageDto srm = new ReturnMessageDto();

		srm = impl.checkDroitCompteurAsaA52(srm, demande);

		assertEquals(0, srm.getInfos().size());
		assertEquals(1, srm.getErrors().size());
		assertEquals(srm.getErrors().get(0), AbsAsaDataConsistencyRulesImpl.DEPASSEMENT_DROITS_ASA_MSG);
	}

	@Test
	public void checkDroitCompteurAsaA52_compteurNegatifBis() {

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(1);

		Date dateDebut = new Date();
		AgentAsaA52Count soldeAsaA52 = new AgentAsaA52Count();
		soldeAsaA52.setTotalMinutes(9);
		soldeAsaA52.setOrganisationSyndicale(organisationSyndicale);

		DemandeAsa demande = new DemandeAsa();
		demande.setIdAgent(9005138);
		demande.setDateDebut(dateDebut);
		demande.setOrganisationSyndicale(organisationSyndicale);
		demande.setDuree(0.0);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getOSCounterByDate(AgentAsaA52Count.class, organisationSyndicale.getIdOrganisationSyndicale(), dateDebut)).thenReturn(soldeAsaA52);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreMinutes(Mockito.isA(Date.class), Mockito.isA(Date.class))).thenReturn(10);

		DemandeAsa demande1 = new DemandeAsa();
		demande1.setDateDebut(new Date());
		demande1.setDateFin(new Date());

		DemandeAsa demande2 = new DemandeAsa();
		demande2.setDateDebut(new Date());
		demande2.setDateFin(new Date());

		AgentOrganisationSyndicale agentOrga = new AgentOrganisationSyndicale();
		agentOrga.setActif(true);
		agentOrga.setIdAgent(9005138);

		List<DemandeAsa> listDemandeAsa = new ArrayList<DemandeAsa>();
		listDemandeAsa.addAll(Arrays.asList(demande1, demande2));

		IAsaRepository asaRepository = Mockito.mock(IAsaRepository.class);
		Mockito.when(asaRepository.getListDemandeAsaPourMoisByOS(Mockito.anyInt(), (Integer) Mockito.any(), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyInt())).thenReturn(
				listDemandeAsa);

		IOrganisationSyndicaleRepository organisationSyndicaleRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(organisationSyndicaleRepository.getAgentOrganisation(Mockito.anyInt(), Mockito.anyInt())).thenReturn(agentOrga);

		AbsAsaA52DataConsistencyRulesImpl impl = new AbsAsaA52DataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(impl, "helperService", helperService);
		ReflectionTestUtils.setField(impl, "asaRepository", asaRepository);
		ReflectionTestUtils.setField(impl, "organisationSyndicaleRepository", organisationSyndicaleRepository);

		ReturnMessageDto srm = new ReturnMessageDto();

		srm = impl.checkDroitCompteurAsaA52(srm, demande);

		assertEquals(0, srm.getInfos().size());
		assertEquals(1, srm.getErrors().size());
		assertEquals(srm.getErrors().get(0), AbsAsaDataConsistencyRulesImpl.DEPASSEMENT_DROITS_ASA_MSG);
	}

	@Test
	public void checkDroitCompteurAsaA52_ok() {

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(1);

		Date dateDebut = new Date();
		AgentAsaA52Count soldeAsaA52 = new AgentAsaA52Count();
		soldeAsaA52.setTotalMinutes(10);
		soldeAsaA52.setOrganisationSyndicale(organisationSyndicale);

		DemandeAsa demande = new DemandeAsa();
		demande.setIdAgent(9005138);
		demande.setDateDebut(dateDebut);
		demande.setDateFin(dateDebut);
		demande.setOrganisationSyndicale(organisationSyndicale);
		demande.setDuree(5.0);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getOSCounterByDate(AgentAsaA52Count.class, organisationSyndicale.getIdOrganisationSyndicale(), dateDebut)).thenReturn(soldeAsaA52);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreMinutes(Mockito.isA(Date.class), Mockito.isA(Date.class))).thenReturn(10);

		List<DemandeAsa> listDemandeAsa = new ArrayList<DemandeAsa>();
		listDemandeAsa.addAll(Arrays.asList(new DemandeAsa(), new DemandeAsa()));

		AgentOrganisationSyndicale agentOrga = new AgentOrganisationSyndicale();
		agentOrga.setActif(true);
		agentOrga.setIdAgent(9005138);

		IAsaRepository asaRepository = Mockito.mock(IAsaRepository.class);
		Mockito.when(asaRepository.getListDemandeAsaEnCours(Mockito.anyInt(), Mockito.anyInt(), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyInt())).thenReturn(listDemandeAsa);

		IOrganisationSyndicaleRepository organisationSyndicaleRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(organisationSyndicaleRepository.getAgentOrganisation(Mockito.anyInt(), Mockito.anyInt())).thenReturn(agentOrga);

		AbsAsaA52DataConsistencyRulesImpl impl = new AbsAsaA52DataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(impl, "helperService", helperService);
		ReflectionTestUtils.setField(impl, "asaRepository", asaRepository);
		ReflectionTestUtils.setField(impl, "organisationSyndicaleRepository", organisationSyndicaleRepository);

		ReturnMessageDto srm = new ReturnMessageDto();

		srm = impl.checkDroitCompteurAsaA52(srm, demande);

		assertEquals(0, srm.getErrors().size());
		assertEquals(0, srm.getInfos().size());
	}

	@Test
	public void checkDepassementCompteurAgent_aucunCompteur() {

		Date dateDebut = new Date();
		AgentAsaA52Count soldeAsaA52 = null;

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
		Mockito.when(counterRepository.getOSCounterByDate(AgentAsaA52Count.class, demande.getOrganisationSyndicale().getIdOrganisation(), dateDebut)).thenReturn(soldeAsaA52);

		AbsAsaA52DataConsistencyRulesImpl impl = new AbsAsaA52DataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);

		boolean srm = impl.checkDepassementCompteurAgent(demande, null);

		assertTrue(srm);
	}

	@Test
	public void checkDepassementCompteurAgent_compteurNegatif() {

		Date dateDebut = new Date();
		AgentAsaA52Count soldeAsaA52 = new AgentAsaA52Count();
		OrganisationSyndicale os = new OrganisationSyndicale();
		os.setIdOrganisationSyndicale(1);
		soldeAsaA52.setDateDebut(new Date());
		soldeAsaA52.setDateFin(new Date());
		soldeAsaA52.setOrganisationSyndicale(os);
		soldeAsaA52.setTotalMinutes(0);

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
		Mockito.when(counterRepository.getOSCounterByDate(AgentAsaA52Count.class, demande.getOrganisationSyndicale().getIdOrganisation(), dateDebut)).thenReturn(soldeAsaA52);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreMinutes(Mockito.isA(Date.class), Mockito.isA(Date.class))).thenReturn(10);

		List<DemandeAsa> listDemandeAsa = new ArrayList<DemandeAsa>();
		listDemandeAsa.addAll(Arrays.asList(new DemandeAsa(), new DemandeAsa()));
		
		IAsaRepository asaRepository = Mockito.mock(IAsaRepository.class);
		Mockito.when(asaRepository.getListDemandeAsaPourMoisByOS(Mockito.anyInt(), Mockito.anyInt(), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyInt())).thenReturn(listDemandeAsa);

		AbsAsaDataConsistencyRulesImpl service = new AbsAsaDataConsistencyRulesImpl();
		ReflectionTestUtils.setField(service, "asaRepository", asaRepository);
		
		AbsAsaA52DataConsistencyRulesImpl impl = new AbsAsaA52DataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(impl, "helperService", helperService);
		ReflectionTestUtils.setField(impl, "asaRepository", asaRepository);

		boolean srm = impl.checkDepassementCompteurAgent(demande, null);

		assertTrue(srm);
	}

	@Test
	public void checkDepassementCompteurAgent_compteurOk() {

		Date dateDebut = new Date();
		AgentAsaA52Count soldeAsaA52 = new AgentAsaA52Count();
		OrganisationSyndicale os = new OrganisationSyndicale();
		os.setIdOrganisationSyndicale(1);
		soldeAsaA52.setOrganisationSyndicale(os);
		soldeAsaA52.setDateDebut(dateDebut);
		soldeAsaA52.setDateFin(dateDebut);
		soldeAsaA52.setTotalMinutes(3);

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
		Mockito.when(counterRepository.getOSCounterByDate(AgentAsaA52Count.class, demande.getOrganisationSyndicale().getIdOrganisation(), dateDebut)).thenReturn(soldeAsaA52);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreMinutes(Mockito.isA(Date.class), Mockito.isA(Date.class))).thenReturn(1);

		List<DemandeAsa> listDemandeAsa = new ArrayList<DemandeAsa>();
		listDemandeAsa.addAll(Arrays.asList(new DemandeAsa(), new DemandeAsa()));
		
		IAsaRepository asaRepository = Mockito.mock(IAsaRepository.class);
		Mockito.when(asaRepository.getListDemandeAsaPourMoisByOS(Mockito.anyInt(), Mockito.anyInt(), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyInt())).thenReturn(listDemandeAsa);

		AbsAsaA52DataConsistencyRulesImpl impl = new AbsAsaA52DataConsistencyRulesImpl();
		ReflectionTestUtils.setField(impl, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(impl, "helperService", helperService);
		ReflectionTestUtils.setField(impl, "asaRepository", asaRepository);

		boolean srm = impl.checkDepassementCompteurAgent(demande, null);

		assertFalse(srm);
	}
}
