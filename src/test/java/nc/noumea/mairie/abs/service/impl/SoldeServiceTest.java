package nc.noumea.mairie.abs.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.AgentAsaA48Count;
import nc.noumea.mairie.abs.domain.AgentAsaA52Count;
import nc.noumea.mairie.abs.domain.AgentAsaA54Count;
import nc.noumea.mairie.abs.domain.AgentAsaA55Count;
import nc.noumea.mairie.abs.domain.AgentAsaAmicaleCount;
import nc.noumea.mairie.abs.domain.AgentCongeAnnuelCount;
import nc.noumea.mairie.abs.domain.AgentCount;
import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;
import nc.noumea.mairie.abs.domain.AgentOrganisationSyndicale;
import nc.noumea.mairie.abs.domain.AgentRecupCount;
import nc.noumea.mairie.abs.domain.AgentReposCompCount;
import nc.noumea.mairie.abs.domain.CongeAnnuelRestitutionMassive;
import nc.noumea.mairie.abs.domain.CongeAnnuelRestitutionMassiveHisto;
import nc.noumea.mairie.abs.domain.DemandeAsa;
import nc.noumea.mairie.abs.domain.DemandeMaladies;
import nc.noumea.mairie.abs.domain.MotifCompteur;
import nc.noumea.mairie.abs.domain.OrganisationSyndicale;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.HistoriqueSoldeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.dto.SoldeDto;
import nc.noumea.mairie.abs.dto.SoldeMaladiesDto;
import nc.noumea.mairie.abs.dto.SoldeSpecifiqueDto;
import nc.noumea.mairie.abs.repository.IAsaRepository;
import nc.noumea.mairie.abs.repository.ICongesAnnuelsRepository;
import nc.noumea.mairie.abs.repository.ICounterRepository;
import nc.noumea.mairie.abs.repository.IDemandeRepository;
import nc.noumea.mairie.abs.repository.IOrganisationSyndicaleRepository;
import nc.noumea.mairie.abs.repository.IRecuperationRepository;
import nc.noumea.mairie.abs.repository.IReposCompensateurRepository;
import nc.noumea.mairie.abs.service.IAbsenceDataConsistencyRules;
import nc.noumea.mairie.abs.service.ICounterService;
import nc.noumea.mairie.abs.service.counter.impl.CongesExcepCounterServiceImpl;
import nc.noumea.mairie.abs.service.counter.impl.MaladieCounterServiceImpl;
import nc.noumea.mairie.abs.service.rules.impl.AbsReposCompensateurDataConsistencyRulesImpl;

import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

public class SoldeServiceTest {

	@Test
	public void getAgentSolde_ZeroSolde() {

		// Given
		Date dateJour = new Date();
		Integer idAgent = 9008765;
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		Integer annee = cal.get(Calendar.YEAR);

		ICounterRepository cr = Mockito.mock(ICounterRepository.class);
		Mockito.when(cr.getAgentCounter(AgentRecupCount.class, idAgent)).thenReturn(null);
		Mockito.when(cr.getAgentCounterByDate(AgentAsaA48Count.class, idAgent, new DateTime(annee, 1, 1, 0, 0, 0).toDate())).thenReturn(null);
		Mockito.when(cr.getAgentCounterByDate(AgentAsaA54Count.class, idAgent, new DateTime(annee, 1, 1, 0, 0, 0).toDate())).thenReturn(null);
		Mockito.when(cr.getAgentCounterByDate(AgentAsaA55Count.class, idAgent, new DateTime(annee, 1, 1, 0, 0, 0).toDate())).thenReturn(null);

		AbsReposCompensateurDataConsistencyRulesImpl absDataConsistencyRules = Mockito.mock(AbsReposCompensateurDataConsistencyRulesImpl.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				return result;
			}
		}).when(absDataConsistencyRules).checkStatutAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class), Mockito.isA(Boolean.class));

		List<SoldeSpecifiqueDto> listeSoldeSpecifiqueDto = new ArrayList<SoldeSpecifiqueDto>();

		CongesExcepCounterServiceImpl congesExcepCounterServiceImpl = Mockito.mock(CongesExcepCounterServiceImpl.class);
		Mockito.when(congesExcepCounterServiceImpl.getListAgentCounterByDate(idAgent, null, null)).thenReturn(listeSoldeSpecifiqueDto);

		IOrganisationSyndicaleRepository organisationSyndicaleRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(organisationSyndicaleRepository.getAgentOrganisationActif(idAgent)).thenReturn(new ArrayList<AgentOrganisationSyndicale>());

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getNombreSamediOffertSurAnnee(idAgent, 2015, null)).thenReturn(0);

		ICongesAnnuelsRepository congeAnnuelRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		IReposCompensateurRepository reposCompensateurRepository = Mockito.mock(IReposCompensateurRepository.class);
		IRecuperationRepository recuperationRepository = Mockito.mock(IRecuperationRepository.class);
		IAbsenceDataConsistencyRules absAsaA48DataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		IAbsenceDataConsistencyRules absAsaA52DataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		IAbsenceDataConsistencyRules absAsaA54DataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		IAbsenceDataConsistencyRules absAsaA55DataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		IAbsenceDataConsistencyRules absAsaAmicaleDataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);

		SoldeMaladiesDto soldeMaladies = new SoldeMaladiesDto();
		soldeMaladies.setDroitsPleinSalaire(90);
		soldeMaladies.setDroitsDemiSalaire(75);
		soldeMaladies.setTotalPris(20);
		soldeMaladies.setRapPleinSalaire(80);
		soldeMaladies.setRapDemiSalaire(50);
		
		MaladieCounterServiceImpl maladieCounterServiceImpl = Mockito.mock(MaladieCounterServiceImpl.class);
		Mockito.when(maladieCounterServiceImpl.getSoldeByAgent(Mockito.anyInt(), Mockito.any(Date.class), (AgentGeneriqueDto)Mockito.any())).thenReturn(soldeMaladies);
		
		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "counterRepository", cr);
		ReflectionTestUtils.setField(service, "absReposCompDataConsistencyRules", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "congesExcepCounterServiceImpl", congesExcepCounterServiceImpl);
		ReflectionTestUtils.setField(service, "organisationSyndicaleRepository", organisationSyndicaleRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "maladieCounterServiceImpl", maladieCounterServiceImpl);
		ReflectionTestUtils.setField(service, "congeAnnuelRepository", congeAnnuelRepository);
		ReflectionTestUtils.setField(service, "reposCompensateurRepository", reposCompensateurRepository);
		ReflectionTestUtils.setField(service, "recuperationRepository", recuperationRepository);
		ReflectionTestUtils.setField(service, "absAsaA48DataConsistencyRulesImpl", absAsaA48DataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "absAsaA52DataConsistencyRulesImpl", absAsaA52DataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "absAsaA54DataConsistencyRulesImpl", absAsaA54DataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "absAsaA55DataConsistencyRulesImpl", absAsaA55DataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "absAsaAmicaleDataConsistencyRulesImpl", absAsaAmicaleDataConsistencyRulesImpl);

		// When
		SoldeDto dto = service.getAgentSolde(idAgent, null, null, null, dateJour);

		assertEquals("0.0", dto.getSoldeCongeAnnee().toString());
		assertEquals("0.0", dto.getSoldeCongeAnneePrec().toString());
		assertEquals("0.0", dto.getSoldeRecup().toString());
		assertEquals("0.0", dto.getSoldeReposCompAnnee().toString());
		assertEquals("0.0", dto.getSoldeReposCompAnneePrec().toString());
		assertEquals("0.0", dto.getSoldeAsaA48().toString());
		assertEquals("0.0", dto.getSoldeAsaA54().toString());
		assertEquals("0.0", dto.getSoldeAsaA55().toString());
		assertEquals("0.0", dto.getSoldeAsaA52().toString());
		assertTrue(dto.isAfficheSoldeConge());
		assertTrue(dto.isAfficheSoldeRecup());
		assertTrue(dto.isAfficheSoldeReposComp());
		assertFalse(dto.isAfficheSoldeAsaA48());
		assertFalse(dto.isAfficheSoldeAsaA54());
		assertFalse(dto.isAfficheSoldeAsaA55());
		assertFalse(dto.isAfficheSoldeAsaA52());
		assertFalse(dto.isAfficheSoldeCongesExcep());
		assertEquals(soldeMaladies.getDroitsPleinSalaire(), dto.getSoldeMaladies().getDroitsPleinSalaire());
		assertEquals(soldeMaladies.getDroitsDemiSalaire(), dto.getSoldeMaladies().getDroitsDemiSalaire());
		assertEquals(soldeMaladies.getRapPleinSalaire(), dto.getSoldeMaladies().getRapPleinSalaire());
		assertEquals(soldeMaladies.getRapDemiSalaire(), dto.getSoldeMaladies().getRapDemiSalaire());
		assertEquals(soldeMaladies.getTotalPris(), dto.getSoldeMaladies().getTotalPris());
	}

	@Test
	public void getAgentSolde_GetAllSolde() throws ParseException {

		// Given

		Date dateDeb = new DateTime(2014, 1, 1, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 31, 23, 59, 0).toDate();
		Integer idAgent = 9008765;
		double cotaSoldeAnnee = 62.0;
		double cotaSoldeAnneePrec = 25.5;

		AgentRecupCount arc = new AgentRecupCount();
		arc.setIdAgent(idAgent);
		arc.setTotalMinutes(72);

		AgentReposCompCount arcc = new AgentReposCompCount();
		arcc.setIdAgent(idAgent);
		arcc.setTotalMinutes(12);
		arcc.setTotalMinutesAnneeN1(10);

		AgentAsaA48Count arccc = new AgentAsaA48Count();
		arccc.setIdAgent(idAgent);
		arccc.setTotalJours(12.0);
		arccc.setDateDebut(dateDeb);
		arccc.setDateFin(dateFin);
		arccc.setActif(true);

		AgentAsaAmicaleCount arcAmicale = new AgentAsaAmicaleCount();
		arcAmicale.setIdAgent(idAgent);
		arcAmicale.setTotalMinutes(600);
		arcAmicale.setDateDebut(dateDeb);
		arcAmicale.setDateFin(dateFin);
		arcAmicale.setActif(true);

		AgentAsaA54Count arc54 = new AgentAsaA54Count();
		arc54.setIdAgent(idAgent);
		arc54.setTotalJours(12.0);
		arc54.setDateDebut(dateDeb);
		arc54.setDateFin(dateFin);
		arc54.setActif(true);

		AgentAsaA55Count arc55 = new AgentAsaA55Count();
		arc55.setIdAgent(idAgent);
		arc55.setTotalMinutes(12 * 60);
		arc55.setDateDebut(new DateTime(2014, 1, 1, 0, 0, 0).toDate());
		arc55.setDateFin(new DateTime(2014, 1, 31, 23, 59, 0).toDate());

		AgentAsaA55Count arc55bis = new AgentAsaA55Count();
		arc55bis.setIdAgent(idAgent);
		arc55bis.setTotalMinutes(2 * 60);
		arc55bis.setDateDebut(new DateTime(2014, 3, 1, 0, 0, 0).toDate());
		arc55bis.setDateFin(new DateTime(2014, 3, 31, 23, 59, 0).toDate());

		List<AgentAsaA55Count> listeArc55 = new ArrayList<AgentAsaA55Count>();
		listeArc55.add(arc55);
		listeArc55.add(arc55bis);

		AgentCongeAnnuelCount soldeCongeAnnu = new AgentCongeAnnuelCount();
		soldeCongeAnnu.setIdAgent(idAgent);
		soldeCongeAnnu.setTotalJours(cotaSoldeAnnee);
		soldeCongeAnnu.setTotalJoursAnneeN1(cotaSoldeAnneePrec);

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(1);

		AgentAsaA52Count arc52 = new AgentAsaA52Count();
		arc52.setIdAgent(idAgent);
		arc52.setOrganisationSyndicale(organisationSyndicale);
		arc52.setTotalMinutes(12 * 60);
		arc52.setDateDebut(new DateTime(2014, 1, 1, 0, 0, 0).toDate());
		arc52.setDateFin(new DateTime(2014, 1, 31, 23, 59, 0).toDate());

		AgentAsaA52Count arc52bis = new AgentAsaA52Count();
		arc52bis.setIdAgent(idAgent);
		arc52bis.setOrganisationSyndicale(organisationSyndicale);
		arc52bis.setTotalMinutes(2 * 60);
		arc52bis.setDateDebut(new DateTime(2014, 3, 1, 0, 0, 0).toDate());
		arc52bis.setDateFin(new DateTime(2014, 3, 31, 23, 59, 0).toDate());

		List<AgentAsaA52Count> listeArc52 = new ArrayList<AgentAsaA52Count>();
		listeArc52.add(arc52);
		listeArc52.add(arc52bis);

		AgentOrganisationSyndicale ag = new AgentOrganisationSyndicale();
		ag.setOrganisationSyndicale(organisationSyndicale);
		List<AgentOrganisationSyndicale> list = new ArrayList<>();
		list.add(ag);

		Date dateJour = new Date();

		ICounterRepository cr = Mockito.mock(ICounterRepository.class);
		Mockito.when(cr.getAgentCounter(AgentRecupCount.class, idAgent)).thenReturn(arc);
		Mockito.when(cr.getAgentCounter(AgentReposCompCount.class, idAgent)).thenReturn(arcc);
		Mockito.when(cr.getAgentCounterByDate(AgentAsaA48Count.class, 9008765, dateDeb)).thenReturn(arccc);
		Mockito.when(cr.getAgentCounterByDate(AgentAsaA54Count.class, 9008765, dateDeb)).thenReturn(arc54);
		Mockito.when(cr.getAgentCounterByDate(AgentAsaAmicaleCount.class, 9008765, dateDeb)).thenReturn(arcAmicale);
		Mockito.when(cr.getAgentCounterByDate(AgentAsaA55Count.class, 9008765, dateJour)).thenReturn(arc55);
		Mockito.when(cr.getAgentCounter(AgentCongeAnnuelCount.class, 9008765)).thenReturn(soldeCongeAnnu);
		Mockito.when(cr.getListAgentCounterA55ByDate(9008765, dateDeb, dateFin)).thenReturn(listeArc55);
		Mockito.when(cr.getOSCounterByDate(AgentAsaA52Count.class, list.get(0).getOrganisationSyndicale().getIdOrganisationSyndicale(), dateJour)).thenReturn(arc52);
		Mockito.when(cr.getListOSCounterByDateAndOrganisation(list.get(0).getOrganisationSyndicale().getIdOrganisationSyndicale(), dateDeb, dateFin, null)).thenReturn(listeArc52);

		AbsReposCompensateurDataConsistencyRulesImpl absDataConsistencyRules = Mockito.mock(AbsReposCompensateurDataConsistencyRulesImpl.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				// result.getErrors().add("L'agent [%d] ne peut pas avoir de repos compensateur. Les repos compensateurs sont pour les contractuels ou les conventions collectives.");
				return result;
			}
		}).when(absDataConsistencyRules).checkStatutAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class), Mockito.isA(Boolean.class));

		SoldeSpecifiqueDto soldeSpecifiqueDto = new SoldeSpecifiqueDto();
		soldeSpecifiqueDto.setLibelle("libelle 1");
		soldeSpecifiqueDto.setSolde(10.0);

		SoldeSpecifiqueDto soldeSpecifiqueDto2 = new SoldeSpecifiqueDto();
		soldeSpecifiqueDto2.setLibelle("libelle 2");
		soldeSpecifiqueDto2.setSolde(20.0);

		List<SoldeSpecifiqueDto> listeSoldeSpecifiqueDto = new ArrayList<SoldeSpecifiqueDto>();
		listeSoldeSpecifiqueDto.addAll(Arrays.asList(soldeSpecifiqueDto, soldeSpecifiqueDto2));

		CongesExcepCounterServiceImpl congesExcepCounterServiceImpl = Mockito.mock(CongesExcepCounterServiceImpl.class);
		Mockito.when(congesExcepCounterServiceImpl.getListAgentCounterByDate(idAgent, dateDeb, dateFin)).thenReturn(listeSoldeSpecifiqueDto);

		IOrganisationSyndicaleRepository organisationSyndicaleRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(organisationSyndicaleRepository.getAgentOrganisationActif(idAgent)).thenReturn(list);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getNombreSamediOffertSurAnnee(idAgent, 2015, null)).thenReturn(1);

		List<DemandeAsa> listAsa = new ArrayList<DemandeAsa>();
		IAsaRepository asaRepository = Mockito.mock(IAsaRepository.class);
		Mockito.when(asaRepository.getListDemandeAsaPourMoisByOS(
				Mockito.anyInt(), (Integer) Mockito.any(), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyInt()))
				.thenReturn(listAsa);

		SoldeMaladiesDto soldeMaladies = new SoldeMaladiesDto();
		soldeMaladies.setDroitsPleinSalaire(90);
		soldeMaladies.setDroitsDemiSalaire(75);
		soldeMaladies.setTotalPris(20);
		soldeMaladies.setRapPleinSalaire(80);
		soldeMaladies.setRapDemiSalaire(50);
		
		MaladieCounterServiceImpl maladieCounterServiceImpl = Mockito.mock(MaladieCounterServiceImpl.class);
		Mockito.when(maladieCounterServiceImpl.getSoldeByAgent(Mockito.anyInt(), Mockito.any(Date.class), (AgentGeneriqueDto)Mockito.any())).thenReturn(soldeMaladies);

		ICongesAnnuelsRepository congeAnnuelRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		IReposCompensateurRepository reposCompensateurRepository = Mockito.mock(IReposCompensateurRepository.class);
		IRecuperationRepository recuperationRepository = Mockito.mock(IRecuperationRepository.class);
		IAbsenceDataConsistencyRules absAsaA48DataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		IAbsenceDataConsistencyRules absAsaA52DataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		IAbsenceDataConsistencyRules absAsaA54DataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		IAbsenceDataConsistencyRules absAsaA55DataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		IAbsenceDataConsistencyRules absAsaAmicaleDataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		
		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "counterRepository", cr);
		ReflectionTestUtils.setField(service, "absReposCompDataConsistencyRules", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "congesExcepCounterServiceImpl", congesExcepCounterServiceImpl);
		ReflectionTestUtils.setField(service, "organisationSyndicaleRepository", organisationSyndicaleRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "asaRepository", asaRepository);
		ReflectionTestUtils.setField(service, "maladieCounterServiceImpl", maladieCounterServiceImpl);
		ReflectionTestUtils.setField(service, "congeAnnuelRepository", congeAnnuelRepository);
		ReflectionTestUtils.setField(service, "reposCompensateurRepository", reposCompensateurRepository);
		ReflectionTestUtils.setField(service, "recuperationRepository", recuperationRepository);
		ReflectionTestUtils.setField(service, "absAsaA48DataConsistencyRulesImpl", absAsaA48DataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "absAsaA52DataConsistencyRulesImpl", absAsaA52DataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "absAsaA54DataConsistencyRulesImpl", absAsaA54DataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "absAsaA55DataConsistencyRulesImpl", absAsaA55DataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "absAsaAmicaleDataConsistencyRulesImpl", absAsaAmicaleDataConsistencyRulesImpl);

		// When
		SoldeDto dto = service.getAgentSolde(idAgent, dateDeb, dateFin, null, dateJour);

		assertEquals("72.0", dto.getSoldeRecup().toString());
		assertEquals("62.0", dto.getSoldeCongeAnnee().toString());
		assertEquals("25.5", dto.getSoldeCongeAnneePrec().toString());
		assertEquals("12.0", dto.getSoldeReposCompAnnee().toString());
		assertEquals("10.0", dto.getSoldeReposCompAnneePrec().toString());
		assertEquals(12, dto.getSoldeAsaA48().intValue());
		assertEquals(12, dto.getSoldeAsaA54().intValue());
		assertEquals(600, dto.getSoldeAsaAmicale().intValue());
		assertEquals(12 * 60, dto.getSoldeAsaA55().intValue());
		assertEquals(12 * 60, dto.getSoldeAsaA52().intValue());
		assertTrue(dto.isAfficheSoldeConge());
		assertTrue(dto.isAfficheSoldeRecup());
		assertTrue(dto.isAfficheSoldeReposComp());
		assertTrue(dto.isAfficheSoldeAsaA48());
		assertTrue(dto.isAfficheSoldeAsaA54());
		assertTrue(dto.isAfficheSoldeAsaAmicale());
		assertTrue(dto.isAfficheSoldeAsaA55());
		assertTrue(dto.isAfficheSoldeAsaA52());
		assertEquals(2, dto.getListeSoldeAsaA55().size());
		assertEquals(2, dto.getListeSoldeAsaA52().size());
		assertEquals(2, dto.getListeSoldeCongesExcep().size());
		assertTrue(dto.isAfficheSoldeCongesExcep());
		assertEquals(soldeMaladies.getDroitsPleinSalaire(), dto.getSoldeMaladies().getDroitsPleinSalaire());
		assertEquals(soldeMaladies.getDroitsDemiSalaire(), dto.getSoldeMaladies().getDroitsDemiSalaire());
		assertEquals(soldeMaladies.getRapPleinSalaire(), dto.getSoldeMaladies().getRapPleinSalaire());
		assertEquals(soldeMaladies.getRapDemiSalaire(), dto.getSoldeMaladies().getRapDemiSalaire());
		assertEquals(soldeMaladies.getTotalPris(), dto.getSoldeMaladies().getTotalPris());
	}

	@Test
	public void getAgentSolde_GetAllSolde_WithDate() throws ParseException {

		// Given
		Integer idAgent = 9008765;
		double cotaSoldeAnnee = 62.0;
		double cotaSoldeAnneePrec = 25.5;

		AgentRecupCount arc = new AgentRecupCount();
		arc.setIdAgent(idAgent);
		arc.setTotalMinutes(72);

		AgentReposCompCount arcc = new AgentReposCompCount();
		arcc.setIdAgent(idAgent);
		arcc.setTotalMinutes(12);
		arcc.setTotalMinutesAnneeN1(10);

		AgentAsaA48Count arccc = new AgentAsaA48Count();
		arccc.setIdAgent(idAgent);
		arccc.setTotalJours(12.0);
		arccc.setDateDebut(new DateTime(2014, 1, 1, 0, 0, 0).toDate());
		arccc.setDateFin(new DateTime(2014, 12, 31, 23, 59, 0).toDate());

		AgentAsaA54Count arcc54 = new AgentAsaA54Count();
		arcc54.setIdAgent(idAgent);
		arcc54.setTotalJours(12.0);
		arcc54.setDateDebut(new DateTime(2014, 1, 1, 0, 0, 0).toDate());
		arcc54.setDateFin(new DateTime(2014, 12, 31, 23, 59, 0).toDate());

		AgentAsaA55Count arcc55 = new AgentAsaA55Count();
		arcc55.setIdAgent(idAgent);
		arcc55.setTotalMinutes(12 * 60);
		arcc55.setDateDebut(new DateTime(2014, 1, 1, 0, 0, 0).toDate());
		arcc55.setDateFin(new DateTime(2014, 1, 31, 23, 59, 0).toDate());

		AgentAsaA55Count arc55bis = new AgentAsaA55Count();
		arc55bis.setIdAgent(idAgent);
		arc55bis.setTotalMinutes(2 * 60);
		arc55bis.setDateDebut(new DateTime(2013, 3, 1, 0, 0, 0).toDate());
		arc55bis.setDateFin(new DateTime(2013, 3, 31, 23, 59, 0).toDate());

		List<AgentAsaA55Count> listeArc55 = new ArrayList<AgentAsaA55Count>();
		listeArc55.add(arcc55);
		listeArc55.add(arc55bis);

		AgentCongeAnnuelCount soldeCongeAnnu = new AgentCongeAnnuelCount();
		soldeCongeAnnu.setIdAgent(idAgent);
		soldeCongeAnnu.setTotalJours(cotaSoldeAnnee);
		soldeCongeAnnu.setTotalJoursAnneeN1(cotaSoldeAnneePrec);

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(1);

		AgentAsaA52Count arc52 = new AgentAsaA52Count();
		arc52.setIdAgent(idAgent);
		arc52.setOrganisationSyndicale(organisationSyndicale);
		arc52.setTotalMinutes(12 * 60);
		arc52.setDateDebut(new DateTime(2014, 1, 1, 0, 0, 0).toDate());
		arc52.setDateFin(new DateTime(2014, 1, 31, 23, 59, 0).toDate());

		AgentAsaA52Count arc52bis = new AgentAsaA52Count();
		arc52bis.setIdAgent(idAgent);
		arc52bis.setOrganisationSyndicale(organisationSyndicale);
		arc52bis.setTotalMinutes(2 * 60);
		arc52bis.setDateDebut(new DateTime(2014, 3, 1, 0, 0, 0).toDate());
		arc52bis.setDateFin(new DateTime(2014, 3, 31, 23, 59, 0).toDate());

		List<AgentAsaA52Count> listeArc52 = new ArrayList<AgentAsaA52Count>();
		listeArc52.add(arc52);
		listeArc52.add(arc52bis);

		AgentOrganisationSyndicale ag = new AgentOrganisationSyndicale();
		ag.setOrganisationSyndicale(organisationSyndicale);
		List<AgentOrganisationSyndicale> list = new ArrayList<>();
		list.add(ag);
		Date dateJour = new Date();

		ICounterRepository cr = Mockito.mock(ICounterRepository.class);
		Mockito.when(cr.getAgentCounter(AgentRecupCount.class, idAgent)).thenReturn(arc);
		Mockito.when(cr.getAgentCounter(AgentReposCompCount.class, idAgent)).thenReturn(arcc);
		Mockito.when(cr.getAgentCounterByDate(AgentAsaA48Count.class, 9008765, new DateTime(2014, 1, 1, 0, 0, 0).toDate())).thenReturn(arccc);
		Mockito.when(cr.getAgentCounterByDate(AgentAsaA54Count.class, 9008765, new DateTime(2014, 1, 1, 0, 0, 0).toDate())).thenReturn(arcc54);
		Mockito.when(cr.getAgentCounterByDate(AgentAsaA55Count.class, 9008765, new DateTime(2014, 1, 1, 0, 0, 0).toDate())).thenReturn(arcc55);
		Mockito.when(cr.getAgentCounter(AgentCongeAnnuelCount.class, 9008765)).thenReturn(soldeCongeAnnu);
		Mockito.when(cr.getListAgentCounterA55ByDate(9008765, new DateTime(2014, 1, 1, 0, 0, 0).toDate(), new DateTime(2014, 12, 31, 23, 59, 0).toDate())).thenReturn(listeArc55);
		Mockito.when(cr.getOSCounterByDate(AgentAsaA52Count.class, list.get(0).getOrganisationSyndicale().getIdOrganisationSyndicale(), dateJour)).thenReturn(arc52);
		Mockito.when(
				cr.getListOSCounterByDateAndOrganisation(list.get(0).getOrganisationSyndicale().getIdOrganisationSyndicale(), new DateTime(2013, 1, 1, 0, 0, 0).toDate(), new DateTime(2014, 12, 31,
						23, 59, 0).toDate(), null)).thenReturn(listeArc52);

		AbsReposCompensateurDataConsistencyRulesImpl absDataConsistencyRules = Mockito.mock(AbsReposCompensateurDataConsistencyRulesImpl.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				// result.getErrors().add("L'agent [%d] ne peut pas avoir de repos compensateur. Les repos compensateurs sont pour les contractuels ou les conventions collectives.");
				return result;
			}
		}).when(absDataConsistencyRules).checkStatutAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class), Mockito.isA(Boolean.class));

		List<SoldeSpecifiqueDto> listeSoldeSpecifiqueDto = new ArrayList<SoldeSpecifiqueDto>();

		CongesExcepCounterServiceImpl congesExcepCounterServiceImpl = Mockito.mock(CongesExcepCounterServiceImpl.class);
		Mockito.when(congesExcepCounterServiceImpl.getListAgentCounterByDate(idAgent, null, null)).thenReturn(listeSoldeSpecifiqueDto);

		IOrganisationSyndicaleRepository organisationSyndicaleRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(organisationSyndicaleRepository.getAgentOrganisationActif(idAgent)).thenReturn(list);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getNombreSamediOffertSurAnnee(idAgent, 2015, null)).thenReturn(1);

		List<DemandeAsa> listAsa = new ArrayList<DemandeAsa>();
		IAsaRepository asaRepository = Mockito.mock(IAsaRepository.class);
		Mockito.when(asaRepository.getListDemandeAsaPourMoisByOS(
				Mockito.anyInt(), (Integer) Mockito.any(), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyInt()))
				.thenReturn(listAsa);

		SoldeMaladiesDto soldeMaladies = new SoldeMaladiesDto();
		soldeMaladies.setDroitsPleinSalaire(90);
		soldeMaladies.setDroitsDemiSalaire(75);
		soldeMaladies.setTotalPris(20);
		soldeMaladies.setRapPleinSalaire(80);
		soldeMaladies.setRapDemiSalaire(50);
		
		MaladieCounterServiceImpl maladieCounterServiceImpl = Mockito.mock(MaladieCounterServiceImpl.class);
		Mockito.when(maladieCounterServiceImpl.getSoldeByAgent(Mockito.anyInt(), Mockito.any(Date.class), (AgentGeneriqueDto)Mockito.any())).thenReturn(soldeMaladies);

		ICongesAnnuelsRepository congeAnnuelRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		IReposCompensateurRepository reposCompensateurRepository = Mockito.mock(IReposCompensateurRepository.class);
		IRecuperationRepository recuperationRepository = Mockito.mock(IRecuperationRepository.class);
		IAbsenceDataConsistencyRules absAsaA48DataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		IAbsenceDataConsistencyRules absAsaA52DataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		IAbsenceDataConsistencyRules absAsaA54DataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		IAbsenceDataConsistencyRules absAsaA55DataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		IAbsenceDataConsistencyRules absAsaAmicaleDataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		
		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "counterRepository", cr);
		ReflectionTestUtils.setField(service, "absReposCompDataConsistencyRules", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "congesExcepCounterServiceImpl", congesExcepCounterServiceImpl);
		ReflectionTestUtils.setField(service, "organisationSyndicaleRepository", organisationSyndicaleRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "asaRepository", asaRepository);
		ReflectionTestUtils.setField(service, "maladieCounterServiceImpl", maladieCounterServiceImpl);
		ReflectionTestUtils.setField(service, "congeAnnuelRepository", congeAnnuelRepository);
		ReflectionTestUtils.setField(service, "reposCompensateurRepository", reposCompensateurRepository);
		ReflectionTestUtils.setField(service, "recuperationRepository", recuperationRepository);
		ReflectionTestUtils.setField(service, "absAsaA48DataConsistencyRulesImpl", absAsaA48DataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "absAsaA52DataConsistencyRulesImpl", absAsaA52DataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "absAsaA54DataConsistencyRulesImpl", absAsaA54DataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "absAsaA55DataConsistencyRulesImpl", absAsaA55DataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "absAsaAmicaleDataConsistencyRulesImpl", absAsaAmicaleDataConsistencyRulesImpl);

		Date dateDeb = new DateTime(2013, 1, 1, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 31, 23, 59, 0).toDate();
		SoldeDto dto = service.getAgentSolde(idAgent, dateDeb, dateFin, null, dateJour);

		assertEquals("72.0", dto.getSoldeRecup().toString());
		assertEquals("62.0", dto.getSoldeCongeAnnee().toString());
		assertEquals("25.5", dto.getSoldeCongeAnneePrec().toString());
		assertEquals("12.0", dto.getSoldeReposCompAnnee().toString());
		assertEquals("10.0", dto.getSoldeReposCompAnneePrec().toString());
		assertEquals(0, dto.getSoldeAsaA48().intValue());
		assertEquals(0, dto.getSoldeAsaA54().intValue());
		assertEquals(0, dto.getSoldeAsaA55().intValue());
		assertTrue(dto.isAfficheSoldeConge());
		assertTrue(dto.isAfficheSoldeRecup());
		assertTrue(dto.isAfficheSoldeReposComp());
		assertFalse(dto.isAfficheSoldeAsaA48());
		assertFalse(dto.isAfficheSoldeAsaA54());
		assertFalse(dto.isAfficheSoldeAsaA55());
		assertEquals(0, dto.getListeSoldeAsaA55().size());
		assertFalse(dto.isAfficheSoldeCongesExcep());
		assertEquals(0, dto.getListeSoldeCongesExcep().size());
		assertEquals(12 * 60, dto.getSoldeAsaA52().intValue());
		assertTrue(dto.isAfficheSoldeAsaA52());
		assertEquals(2, dto.getListeSoldeAsaA52().size());
		assertEquals(soldeMaladies.getDroitsPleinSalaire(), dto.getSoldeMaladies().getDroitsPleinSalaire());
		assertEquals(soldeMaladies.getDroitsDemiSalaire(), dto.getSoldeMaladies().getDroitsDemiSalaire());
		assertEquals(soldeMaladies.getRapPleinSalaire(), dto.getSoldeMaladies().getRapPleinSalaire());
		assertEquals(soldeMaladies.getRapDemiSalaire(), dto.getSoldeMaladies().getRapDemiSalaire());
		assertEquals(soldeMaladies.getTotalPris(), dto.getSoldeMaladies().getTotalPris());
	}

	@Test
	public void getAgentSolde_AgentExists_NoAsaA48() {

		// Given
		Integer idAgent = 9008765;
		double cotaSoldeAnnee = 62.0;
		double cotaSoldeAnneePrec = 25.5;
		ReturnMessageDto srm = new ReturnMessageDto();
		srm.getErrors().add("erreur");

		AgentRecupCount arc = new AgentRecupCount();
		arc.setIdAgent(idAgent);
		arc.setTotalMinutes(72);

		AgentReposCompCount arcc = new AgentReposCompCount();
		arcc.setIdAgent(idAgent);
		arcc.setTotalMinutes(12);
		arcc.setTotalMinutesAnneeN1(10);

		AgentCongeAnnuelCount soldeCongeAnnu = new AgentCongeAnnuelCount();
		soldeCongeAnnu.setIdAgent(idAgent);
		soldeCongeAnnu.setTotalJours(cotaSoldeAnnee);
		soldeCongeAnnu.setTotalJoursAnneeN1(cotaSoldeAnneePrec);

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(1);

		AgentOrganisationSyndicale ag = new AgentOrganisationSyndicale();
		ag.setOrganisationSyndicale(organisationSyndicale);
		List<AgentOrganisationSyndicale> list = new ArrayList<>();

		ICounterRepository cr = Mockito.mock(ICounterRepository.class);
		Mockito.when(cr.getAgentCounter(AgentRecupCount.class, idAgent)).thenReturn(arc);
		Mockito.when(cr.getAgentCounter(AgentReposCompCount.class, idAgent)).thenReturn(arcc);
		Mockito.when(cr.getAgentCounter(AgentCongeAnnuelCount.class, idAgent)).thenReturn(soldeCongeAnnu);
		Mockito.when(cr.getAgentCounterByDate(AgentAsaA48Count.class, 9008765, new DateTime(2014, 1, 1, 0, 0, 0).toDate())).thenReturn(null);

		AbsReposCompensateurDataConsistencyRulesImpl absDataConsistencyRules = Mockito.mock(AbsReposCompensateurDataConsistencyRulesImpl.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				result.getErrors().add("L'agent [%d] ne peut pas avoir de repos compensateur. Les repos compensateurs sont pour les contractuels ou les conventions collectives.");
				return result;
			}
		}).when(absDataConsistencyRules).checkStatutAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class), Mockito.isA(Boolean.class));

		List<SoldeSpecifiqueDto> listeSoldeSpecifiqueDto = new ArrayList<SoldeSpecifiqueDto>();

		CongesExcepCounterServiceImpl congesExcepCounterServiceImpl = Mockito.mock(CongesExcepCounterServiceImpl.class);
		Mockito.when(congesExcepCounterServiceImpl.getListAgentCounterByDate(idAgent, null, null)).thenReturn(listeSoldeSpecifiqueDto);

		IOrganisationSyndicaleRepository organisationSyndicaleRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(organisationSyndicaleRepository.getAgentOrganisationActif(idAgent)).thenReturn(list);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getNombreSamediOffertSurAnnee(idAgent, 2015, null)).thenReturn(1);

		Date dateJour = new Date();

		SoldeMaladiesDto soldeMaladies = new SoldeMaladiesDto();
		soldeMaladies.setDroitsPleinSalaire(90);
		soldeMaladies.setDroitsDemiSalaire(75);
		soldeMaladies.setTotalPris(20);
		soldeMaladies.setRapPleinSalaire(80);
		soldeMaladies.setRapDemiSalaire(50);
		
		MaladieCounterServiceImpl maladieCounterServiceImpl = Mockito.mock(MaladieCounterServiceImpl.class);
		Mockito.when(maladieCounterServiceImpl.getSoldeByAgent(Mockito.anyInt(), Mockito.any(Date.class), (AgentGeneriqueDto)Mockito.any())).thenReturn(soldeMaladies);

		ICongesAnnuelsRepository congeAnnuelRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		IReposCompensateurRepository reposCompensateurRepository = Mockito.mock(IReposCompensateurRepository.class);
		IRecuperationRepository recuperationRepository = Mockito.mock(IRecuperationRepository.class);
		IAbsenceDataConsistencyRules absAsaA48DataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		IAbsenceDataConsistencyRules absAsaA52DataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		IAbsenceDataConsistencyRules absAsaA54DataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		IAbsenceDataConsistencyRules absAsaA55DataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		IAbsenceDataConsistencyRules absAsaAmicaleDataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		
		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "counterRepository", cr);
		ReflectionTestUtils.setField(service, "absReposCompDataConsistencyRules", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "congesExcepCounterServiceImpl", congesExcepCounterServiceImpl);
		ReflectionTestUtils.setField(service, "organisationSyndicaleRepository", organisationSyndicaleRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "maladieCounterServiceImpl", maladieCounterServiceImpl);
		ReflectionTestUtils.setField(service, "congeAnnuelRepository", congeAnnuelRepository);
		ReflectionTestUtils.setField(service, "reposCompensateurRepository", reposCompensateurRepository);
		ReflectionTestUtils.setField(service, "recuperationRepository", recuperationRepository);
		ReflectionTestUtils.setField(service, "absAsaA48DataConsistencyRulesImpl", absAsaA48DataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "absAsaA52DataConsistencyRulesImpl", absAsaA52DataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "absAsaA54DataConsistencyRulesImpl", absAsaA54DataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "absAsaA55DataConsistencyRulesImpl", absAsaA55DataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "absAsaAmicaleDataConsistencyRulesImpl", absAsaAmicaleDataConsistencyRulesImpl);

		// When
		SoldeDto dto = service.getAgentSolde(idAgent, null, null, null, dateJour);

		assertEquals("72.0", dto.getSoldeRecup().toString());
		assertEquals("62.0", dto.getSoldeCongeAnnee().toString());
		assertEquals("25.5", dto.getSoldeCongeAnneePrec().toString());
		assertEquals("12.0", dto.getSoldeReposCompAnnee().toString());
		assertEquals("10.0", dto.getSoldeReposCompAnneePrec().toString());
		assertEquals(0, dto.getSoldeAsaA48().intValue());
		assertTrue(dto.isAfficheSoldeConge());
		assertTrue(dto.isAfficheSoldeRecup());
		assertFalse(dto.isAfficheSoldeReposComp());
		assertFalse(dto.isAfficheSoldeAsaA48());
		assertEquals(0, dto.getListeSoldeAsaA55().size());
		assertFalse(dto.isAfficheSoldeCongesExcep());
		assertEquals(0, dto.getListeSoldeCongesExcep().size());
		assertEquals(0, dto.getSoldeAsaA52().intValue());
		assertFalse(dto.isAfficheSoldeAsaA52());
		assertEquals(0, dto.getListeSoldeAsaA52().size());
		assertEquals(soldeMaladies.getDroitsPleinSalaire(), dto.getSoldeMaladies().getDroitsPleinSalaire());
		assertEquals(soldeMaladies.getDroitsDemiSalaire(), dto.getSoldeMaladies().getDroitsDemiSalaire());
		assertEquals(soldeMaladies.getRapPleinSalaire(), dto.getSoldeMaladies().getRapPleinSalaire());
		assertEquals(soldeMaladies.getRapDemiSalaire(), dto.getSoldeMaladies().getRapDemiSalaire());
		assertEquals(soldeMaladies.getTotalPris(), dto.getSoldeMaladies().getTotalPris());
	}

	@Test
	public void getAgentSolde_AgentExists_NoAsaA54() {

		// Given
		Integer idAgent = 9008765;
		double cotaSoldeAnnee = 62.0;
		double cotaSoldeAnneePrec = 25.5;
		ReturnMessageDto srm = new ReturnMessageDto();
		srm.getErrors().add("erreur");

		AgentRecupCount arc = new AgentRecupCount();
		arc.setIdAgent(idAgent);
		arc.setTotalMinutes(72);

		AgentReposCompCount arcc = new AgentReposCompCount();
		arcc.setIdAgent(idAgent);
		arcc.setTotalMinutes(12);
		arcc.setTotalMinutesAnneeN1(10);

		AgentCongeAnnuelCount soldeCongeAnnu = new AgentCongeAnnuelCount();
		soldeCongeAnnu.setIdAgent(idAgent);
		soldeCongeAnnu.setTotalJours(cotaSoldeAnnee);
		soldeCongeAnnu.setTotalJoursAnneeN1(cotaSoldeAnneePrec);

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(1);

		AgentOrganisationSyndicale ag = new AgentOrganisationSyndicale();
		ag.setOrganisationSyndicale(organisationSyndicale);
		List<AgentOrganisationSyndicale> list = new ArrayList<>();

		ICounterRepository cr = Mockito.mock(ICounterRepository.class);
		Mockito.when(cr.getAgentCounter(AgentRecupCount.class, idAgent)).thenReturn(arc);
		Mockito.when(cr.getAgentCounter(AgentReposCompCount.class, idAgent)).thenReturn(arcc);
		Mockito.when(cr.getAgentCounter(AgentCongeAnnuelCount.class, idAgent)).thenReturn(soldeCongeAnnu);
		Mockito.when(cr.getAgentCounterByDate(AgentAsaA54Count.class, 9008765, new DateTime(2014, 1, 1, 0, 0, 0).toDate())).thenReturn(null);

		AbsReposCompensateurDataConsistencyRulesImpl absDataConsistencyRules = Mockito.mock(AbsReposCompensateurDataConsistencyRulesImpl.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				result.getErrors().add("L'agent [%d] ne peut pas avoir de repos compensateur. Les repos compensateurs sont pour les contractuels ou les conventions collectives.");
				return result;
			}
		}).when(absDataConsistencyRules).checkStatutAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class), Mockito.isA(Boolean.class));

		List<SoldeSpecifiqueDto> listeSoldeSpecifiqueDto = new ArrayList<SoldeSpecifiqueDto>();

		CongesExcepCounterServiceImpl congesExcepCounterServiceImpl = Mockito.mock(CongesExcepCounterServiceImpl.class);
		Mockito.when(congesExcepCounterServiceImpl.getListAgentCounterByDate(idAgent, null, null)).thenReturn(listeSoldeSpecifiqueDto);

		IOrganisationSyndicaleRepository organisationSyndicaleRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(organisationSyndicaleRepository.getAgentOrganisationActif(idAgent)).thenReturn(list);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getNombreSamediOffertSurAnnee(idAgent, 2015, null)).thenReturn(1);

		SoldeMaladiesDto soldeMaladies = new SoldeMaladiesDto();
		soldeMaladies.setDroitsPleinSalaire(90);
		soldeMaladies.setDroitsDemiSalaire(75);
		soldeMaladies.setTotalPris(20);
		soldeMaladies.setRapPleinSalaire(80);
		soldeMaladies.setRapDemiSalaire(50);

		Date dateJour = new Date();
		
		MaladieCounterServiceImpl maladieCounterServiceImpl = Mockito.mock(MaladieCounterServiceImpl.class);
		Mockito.when(maladieCounterServiceImpl.getSoldeByAgent(Mockito.anyInt(), Mockito.any(Date.class), (AgentGeneriqueDto)Mockito.any())).thenReturn(soldeMaladies);

		ICongesAnnuelsRepository congeAnnuelRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		IReposCompensateurRepository reposCompensateurRepository = Mockito.mock(IReposCompensateurRepository.class);
		IRecuperationRepository recuperationRepository = Mockito.mock(IRecuperationRepository.class);
		IAbsenceDataConsistencyRules absAsaA48DataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		IAbsenceDataConsistencyRules absAsaA52DataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		IAbsenceDataConsistencyRules absAsaA54DataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		IAbsenceDataConsistencyRules absAsaA55DataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		IAbsenceDataConsistencyRules absAsaAmicaleDataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		
		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "counterRepository", cr);
		ReflectionTestUtils.setField(service, "absReposCompDataConsistencyRules", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "congesExcepCounterServiceImpl", congesExcepCounterServiceImpl);
		ReflectionTestUtils.setField(service, "organisationSyndicaleRepository", organisationSyndicaleRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "maladieCounterServiceImpl", maladieCounterServiceImpl);
		ReflectionTestUtils.setField(service, "congeAnnuelRepository", congeAnnuelRepository);
		ReflectionTestUtils.setField(service, "reposCompensateurRepository", reposCompensateurRepository);
		ReflectionTestUtils.setField(service, "recuperationRepository", recuperationRepository);
		ReflectionTestUtils.setField(service, "absAsaA48DataConsistencyRulesImpl", absAsaA48DataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "absAsaA52DataConsistencyRulesImpl", absAsaA52DataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "absAsaA54DataConsistencyRulesImpl", absAsaA54DataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "absAsaA55DataConsistencyRulesImpl", absAsaA55DataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "absAsaAmicaleDataConsistencyRulesImpl", absAsaAmicaleDataConsistencyRulesImpl);

		// When
		SoldeDto dto = service.getAgentSolde(idAgent, null, null, null, dateJour);

		assertEquals("72.0", dto.getSoldeRecup().toString());
		assertEquals("62.0", dto.getSoldeCongeAnnee().toString());
		assertEquals("25.5", dto.getSoldeCongeAnneePrec().toString());
		assertEquals("12.0", dto.getSoldeReposCompAnnee().toString());
		assertEquals("10.0", dto.getSoldeReposCompAnneePrec().toString());
		assertEquals(0, dto.getSoldeAsaA54().intValue());
		assertTrue(dto.isAfficheSoldeConge());
		assertTrue(dto.isAfficheSoldeRecup());
		assertFalse(dto.isAfficheSoldeReposComp());
		assertFalse(dto.isAfficheSoldeAsaA54());
		assertEquals(0, dto.getListeSoldeAsaA55().size());
		assertFalse(dto.isAfficheSoldeCongesExcep());
		assertEquals(0, dto.getListeSoldeCongesExcep().size());
		assertEquals(0, dto.getSoldeAsaA52().intValue());
		assertFalse(dto.isAfficheSoldeAsaA52());
		assertEquals(0, dto.getListeSoldeAsaA52().size());
		assertEquals(soldeMaladies.getDroitsPleinSalaire(), dto.getSoldeMaladies().getDroitsPleinSalaire());
		assertEquals(soldeMaladies.getDroitsDemiSalaire(), dto.getSoldeMaladies().getDroitsDemiSalaire());
		assertEquals(soldeMaladies.getRapPleinSalaire(), dto.getSoldeMaladies().getRapPleinSalaire());
		assertEquals(soldeMaladies.getRapDemiSalaire(), dto.getSoldeMaladies().getRapDemiSalaire());
		assertEquals(soldeMaladies.getTotalPris(), dto.getSoldeMaladies().getTotalPris());
	}

	@Test
	public void getAgentSolde_AgentExists_NoAsaA55() {

		// Given
		Integer idAgent = 9008765;
		double cotaSoldeAnnee = 62.0;
		double cotaSoldeAnneePrec = 25.5;
		ReturnMessageDto srm = new ReturnMessageDto();
		srm.getErrors().add("erreur");

		AgentRecupCount arc = new AgentRecupCount();
		arc.setIdAgent(idAgent);
		arc.setTotalMinutes(72);

		AgentReposCompCount arcc = new AgentReposCompCount();
		arcc.setIdAgent(idAgent);
		arcc.setTotalMinutes(12);
		arcc.setTotalMinutesAnneeN1(10);

		AgentCongeAnnuelCount soldeCongeAnnu = new AgentCongeAnnuelCount();
		soldeCongeAnnu.setIdAgent(idAgent);
		soldeCongeAnnu.setTotalJours(cotaSoldeAnnee);
		soldeCongeAnnu.setTotalJoursAnneeN1(cotaSoldeAnneePrec);

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(1);

		AgentOrganisationSyndicale ag = new AgentOrganisationSyndicale();
		ag.setOrganisationSyndicale(organisationSyndicale);
		List<AgentOrganisationSyndicale> list = new ArrayList<>();

		ICounterRepository cr = Mockito.mock(ICounterRepository.class);
		Mockito.when(cr.getAgentCounter(AgentRecupCount.class, idAgent)).thenReturn(arc);
		Mockito.when(cr.getAgentCounter(AgentReposCompCount.class, idAgent)).thenReturn(arcc);
		Mockito.when(cr.getAgentCounter(AgentCongeAnnuelCount.class, idAgent)).thenReturn(soldeCongeAnnu);
		Mockito.when(cr.getAgentCounterByDate(AgentAsaA55Count.class, 9008765, new DateTime(2014, 1, 1, 0, 0, 0).toDate())).thenReturn(null);

		AbsReposCompensateurDataConsistencyRulesImpl absDataConsistencyRules = Mockito.mock(AbsReposCompensateurDataConsistencyRulesImpl.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				result.getErrors().add("L'agent [%d] ne peut pas avoir de repos compensateur. Les repos compensateurs sont pour les contractuels ou les conventions collectives.");
				return result;
			}
		}).when(absDataConsistencyRules).checkStatutAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class), Mockito.isA(Boolean.class));

		List<SoldeSpecifiqueDto> listeSoldeSpecifiqueDto = new ArrayList<SoldeSpecifiqueDto>();

		CongesExcepCounterServiceImpl congesExcepCounterServiceImpl = Mockito.mock(CongesExcepCounterServiceImpl.class);
		Mockito.when(congesExcepCounterServiceImpl.getListAgentCounterByDate(idAgent, null, null)).thenReturn(listeSoldeSpecifiqueDto);

		IOrganisationSyndicaleRepository organisationSyndicaleRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(organisationSyndicaleRepository.getAgentOrganisationActif(idAgent)).thenReturn(list);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getNombreSamediOffertSurAnnee(idAgent, 2015, null)).thenReturn(1);

		SoldeMaladiesDto soldeMaladies = new SoldeMaladiesDto();
		soldeMaladies.setDroitsPleinSalaire(90);
		soldeMaladies.setDroitsDemiSalaire(75);
		soldeMaladies.setTotalPris(20);
		soldeMaladies.setRapPleinSalaire(80);
		soldeMaladies.setRapDemiSalaire(50);

		Date dateJour = new Date();
		
		MaladieCounterServiceImpl maladieCounterServiceImpl = Mockito.mock(MaladieCounterServiceImpl.class);
		Mockito.when(maladieCounterServiceImpl.getSoldeByAgent(Mockito.anyInt(), Mockito.any(Date.class), (AgentGeneriqueDto)Mockito.any())).thenReturn(soldeMaladies);

		ICongesAnnuelsRepository congeAnnuelRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		IReposCompensateurRepository reposCompensateurRepository = Mockito.mock(IReposCompensateurRepository.class);
		IRecuperationRepository recuperationRepository = Mockito.mock(IRecuperationRepository.class);
		IAbsenceDataConsistencyRules absAsaA48DataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		IAbsenceDataConsistencyRules absAsaA52DataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		IAbsenceDataConsistencyRules absAsaA54DataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		IAbsenceDataConsistencyRules absAsaA55DataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		IAbsenceDataConsistencyRules absAsaAmicaleDataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		
		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "counterRepository", cr);
		ReflectionTestUtils.setField(service, "absReposCompDataConsistencyRules", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "congesExcepCounterServiceImpl", congesExcepCounterServiceImpl);
		ReflectionTestUtils.setField(service, "organisationSyndicaleRepository", organisationSyndicaleRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "maladieCounterServiceImpl", maladieCounterServiceImpl);
		ReflectionTestUtils.setField(service, "congeAnnuelRepository", congeAnnuelRepository);
		ReflectionTestUtils.setField(service, "reposCompensateurRepository", reposCompensateurRepository);
		ReflectionTestUtils.setField(service, "recuperationRepository", recuperationRepository);
		ReflectionTestUtils.setField(service, "absAsaA48DataConsistencyRulesImpl", absAsaA48DataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "absAsaA52DataConsistencyRulesImpl", absAsaA52DataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "absAsaA54DataConsistencyRulesImpl", absAsaA54DataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "absAsaA55DataConsistencyRulesImpl", absAsaA55DataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "absAsaAmicaleDataConsistencyRulesImpl", absAsaAmicaleDataConsistencyRulesImpl);

		// When
		SoldeDto dto = service.getAgentSolde(idAgent, null, null, null, dateJour);

		assertEquals("72.0", dto.getSoldeRecup().toString());
		assertEquals("62.0", dto.getSoldeCongeAnnee().toString());
		assertEquals("25.5", dto.getSoldeCongeAnneePrec().toString());
		assertEquals("12.0", dto.getSoldeReposCompAnnee().toString());
		assertEquals("10.0", dto.getSoldeReposCompAnneePrec().toString());
		assertEquals(0, dto.getSoldeAsaA55().intValue());
		assertTrue(dto.isAfficheSoldeConge());
		assertTrue(dto.isAfficheSoldeRecup());
		assertFalse(dto.isAfficheSoldeReposComp());
		assertFalse(dto.isAfficheSoldeAsaA55());
		assertEquals(0, dto.getListeSoldeAsaA55().size());
		assertFalse(dto.isAfficheSoldeCongesExcep());
		assertEquals(0, dto.getListeSoldeCongesExcep().size());
		assertEquals(0, dto.getSoldeAsaA52().intValue());
		assertFalse(dto.isAfficheSoldeAsaA52());
		assertEquals(0, dto.getListeSoldeAsaA52().size());
		assertEquals(soldeMaladies.getDroitsPleinSalaire(), dto.getSoldeMaladies().getDroitsPleinSalaire());
		assertEquals(soldeMaladies.getDroitsDemiSalaire(), dto.getSoldeMaladies().getDroitsDemiSalaire());
		assertEquals(soldeMaladies.getRapPleinSalaire(), dto.getSoldeMaladies().getRapPleinSalaire());
		assertEquals(soldeMaladies.getRapDemiSalaire(), dto.getSoldeMaladies().getRapDemiSalaire());
		assertEquals(soldeMaladies.getTotalPris(), dto.getSoldeMaladies().getTotalPris());
	}

	private void prepareDataTest(SoldeService service, Integer idAgent) {
		double cotaSoldeAnnee = 62.0;
		double cotaSoldeAnneePrec = 25.5;
		ReturnMessageDto srm = new ReturnMessageDto();
		srm.getErrors().add("erreur");

		AgentRecupCount arc = new AgentRecupCount();
		arc.setIdAgent(idAgent);
		arc.setTotalMinutes(72);

		AgentReposCompCount arcc = new AgentReposCompCount();
		arcc.setIdAgent(idAgent);
		arcc.setTotalMinutes(12);
		arcc.setTotalMinutesAnneeN1(10);

		AgentCongeAnnuelCount soldeCongeAnnu = new AgentCongeAnnuelCount();
		soldeCongeAnnu.setIdAgent(idAgent);
		soldeCongeAnnu.setTotalJours(cotaSoldeAnnee);
		soldeCongeAnnu.setTotalJoursAnneeN1(cotaSoldeAnneePrec);

		ICounterRepository cr = Mockito.mock(ICounterRepository.class);
		Mockito.when(cr.getAgentCounter(AgentRecupCount.class, idAgent)).thenReturn(arc);
		Mockito.when(cr.getAgentCounter(AgentReposCompCount.class, idAgent)).thenReturn(arcc);
		Mockito.when(cr.getAgentCounter(AgentCongeAnnuelCount.class, idAgent)).thenReturn(soldeCongeAnnu);
		Mockito.when(cr.getAgentCounterByDate(AgentAsaA55Count.class, 9008765, new DateTime(2014, 1, 1, 0, 0, 0).toDate())).thenReturn(null);

		AbsReposCompensateurDataConsistencyRulesImpl absDataConsistencyRules = Mockito.mock(AbsReposCompensateurDataConsistencyRulesImpl.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				result.getErrors().add("L'agent [%d] ne peut pas avoir de repos compensateur. Les repos compensateurs sont pour les contractuels ou les conventions collectives.");
				return result;
			}
		}).when(absDataConsistencyRules).checkStatutAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class), Mockito.isA(Boolean.class));

		List<SoldeSpecifiqueDto> listeSoldeSpecifiqueDto = new ArrayList<SoldeSpecifiqueDto>();

		CongesExcepCounterServiceImpl congesExcepCounterServiceImpl = Mockito.mock(CongesExcepCounterServiceImpl.class);
		Mockito.when(congesExcepCounterServiceImpl.getListAgentCounterByDate(idAgent, null, null)).thenReturn(listeSoldeSpecifiqueDto);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getNombreSamediOffertSurAnnee(idAgent, 2015, null)).thenReturn(1);

		ICongesAnnuelsRepository congeAnnuelRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		IReposCompensateurRepository reposCompensateurRepository = Mockito.mock(IReposCompensateurRepository.class);
		IRecuperationRepository recuperationRepository = Mockito.mock(IRecuperationRepository.class);
		IAbsenceDataConsistencyRules absAsaA48DataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		IAbsenceDataConsistencyRules absAsaA52DataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		IAbsenceDataConsistencyRules absAsaA54DataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		IAbsenceDataConsistencyRules absAsaA55DataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		IAbsenceDataConsistencyRules absAsaAmicaleDataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		
		ReflectionTestUtils.setField(service, "counterRepository", cr);
		ReflectionTestUtils.setField(service, "absReposCompDataConsistencyRules", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "congesExcepCounterServiceImpl", congesExcepCounterServiceImpl);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "congeAnnuelRepository", congeAnnuelRepository);
		ReflectionTestUtils.setField(service, "reposCompensateurRepository", reposCompensateurRepository);
		ReflectionTestUtils.setField(service, "recuperationRepository", recuperationRepository);
		ReflectionTestUtils.setField(service, "absAsaA48DataConsistencyRulesImpl", absAsaA48DataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "absAsaA52DataConsistencyRulesImpl", absAsaA52DataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "absAsaA54DataConsistencyRulesImpl", absAsaA54DataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "absAsaA55DataConsistencyRulesImpl", absAsaA55DataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "absAsaAmicaleDataConsistencyRulesImpl", absAsaAmicaleDataConsistencyRulesImpl);
	}

	@Test
	public void getAgentSolde_AgentExists_WithTypeDemande() {

		// Given
		Integer idAgent = 9008765;
		SoldeService service = new SoldeService();

		prepareDataTest(service, idAgent);

		// When
		Date dateJour = new Date();
		SoldeDto dto = service.getAgentSolde(idAgent, null, null, RefTypeAbsenceEnum.RECUP.getValue(), dateJour);

		assertEquals("72.0", dto.getSoldeRecup().toString());
		assertNull(dto.getSoldeCongeAnnee());
		assertNull(dto.getSoldeCongeAnneePrec());
		assertNull(dto.getSoldeReposCompAnnee());
		assertNull(dto.getSoldeReposCompAnneePrec());
		assertNull(dto.getSoldeAsaA55());
		assertFalse(dto.isAfficheSoldeConge());
		assertTrue(dto.isAfficheSoldeRecup());
		assertFalse(dto.isAfficheSoldeReposComp());
		assertFalse(dto.isAfficheSoldeAsaA55());
		assertEquals(0, dto.getListeSoldeAsaA55().size());
		assertFalse(dto.isAfficheSoldeCongesExcep());
		assertEquals(0, dto.getListeSoldeCongesExcep().size());
	}

	@Test
	public void getAgentSolde_AgentExists_WithTypeDemandeReposComp() {

		// Given
		Integer idAgent = 9008765;
		SoldeService service = new SoldeService();

		prepareDataTest(service, idAgent);
		// When
		Date dateJour = new Date();
		SoldeDto dto = service.getAgentSolde(idAgent, null, null, RefTypeAbsenceEnum.REPOS_COMP.getValue(), dateJour);

		assertNull(dto.getSoldeRecup());
		assertNull(dto.getSoldeCongeAnnee());
		assertNull(dto.getSoldeCongeAnneePrec());
		assertEquals("12.0", dto.getSoldeReposCompAnnee().toString());
		assertEquals("10.0", dto.getSoldeReposCompAnneePrec().toString());
		assertNull(dto.getSoldeAsaA55());
		assertFalse(dto.isAfficheSoldeConge());
		assertFalse(dto.isAfficheSoldeRecup());
		assertFalse(dto.isAfficheSoldeReposComp());// car fonctionnaire
		assertFalse(dto.isAfficheSoldeAsaA55());
		assertEquals(0, dto.getListeSoldeAsaA55().size());
		assertFalse(dto.isAfficheSoldeCongesExcep());
		assertEquals(0, dto.getListeSoldeCongesExcep().size());
	}

	@Test
	public void getAgentSolde_AgentExists_WithTypeDemandeCongesAnnuels() {

		// Given
		Integer idAgent = 9008765;
		SoldeService service = new SoldeService();

		prepareDataTest(service, idAgent);
		// When
		Date dateJour = new Date();
		SoldeDto dto = service.getAgentSolde(idAgent, null, null, RefTypeAbsenceEnum.CONGE_ANNUEL.getValue(), dateJour);

		assertNull(dto.getSoldeRecup());
		assertEquals("62.0", dto.getSoldeCongeAnnee().toString());
		assertEquals("25.5", dto.getSoldeCongeAnneePrec().toString());
		assertNull(dto.getSoldeReposCompAnnee());
		assertNull(dto.getSoldeReposCompAnneePrec());
		assertNull(dto.getSoldeAsaA55());
		assertTrue(dto.isAfficheSoldeConge());
		assertFalse(dto.isAfficheSoldeRecup());
		assertFalse(dto.isAfficheSoldeReposComp());
		assertFalse(dto.isAfficheSoldeAsaA55());
		assertEquals(0, dto.getListeSoldeAsaA55().size());
		assertFalse(dto.isAfficheSoldeCongesExcep());
		assertEquals(0, dto.getListeSoldeCongesExcep().size());
	}

	@Test
	public void getAgentSolde_AgentExists_WithTypeDemandeAsa50() {

		// Given
		Integer idAgent = 9008765;
		SoldeService service = new SoldeService();

		prepareDataTest(service, idAgent);
		// When
		Date dateJour = new Date();
		SoldeDto dto = service.getAgentSolde(idAgent, null, null, RefTypeAbsenceEnum.ASA_A50.getValue(), dateJour);

		assertNull(dto.getSoldeRecup());
		assertNull(dto.getSoldeCongeAnnee());
		assertNull(dto.getSoldeCongeAnneePrec());
		assertNull(dto.getSoldeReposCompAnnee());
		assertNull(dto.getSoldeReposCompAnneePrec());
		assertNull(dto.getSoldeAsaA55());
		assertFalse(dto.isAfficheSoldeConge());
		assertFalse(dto.isAfficheSoldeRecup());
		assertFalse(dto.isAfficheSoldeReposComp());
		assertFalse(dto.isAfficheSoldeAsaA55());
		assertEquals(0, dto.getListeSoldeAsaA55().size());
		assertFalse(dto.isAfficheSoldeCongesExcep());
		assertEquals(0, dto.getListeSoldeCongesExcep().size());
	}

	@Test
	public void getHistoriqueSoldeAgentByTypeAbsence_returnEmptyListe() {
		AgentCount compteurAgent = new AgentCount();
		compteurAgent.setIdAgentCount(1);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getListHisto(9005138, compteurAgent)).thenReturn(new ArrayList<AgentHistoAlimManuelle>());

		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		List<HistoriqueSoldeDto> listResult = service.getHistoriqueSoldeAgent(9005138, 7, new Date(), null, false);

		assertEquals(0, listResult.size());
	}

	@Test
	public void getHistoriqueSoldeAgent_return1Liste_A48() {
		// Given

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(7);
		AgentAsaA48Count compteurAgent = new AgentAsaA48Count();
		compteurAgent.setIdAgentCount(1);
		compteurAgent.setDateDebut(new DateTime(2014, 1, 1, 0, 0, 0).toDate());
		compteurAgent.setDateFin(new DateTime(2014, 1, 31, 0, 0, 0).toDate());
		MotifCompteur motifCompteur = new MotifCompteur();
		motifCompteur.setLibelle("lib motif");
		motifCompteur.setRefTypeAbsence(type);
		AgentHistoAlimManuelle e = new AgentHistoAlimManuelle();
		e.setIdAgent(9005138);
		e.setIdAgentConcerne(9005138);
		e.setType(type);
		e.setText("texte test");
		e.setMotifCompteur(motifCompteur);
		e.setCompteurAgent(compteurAgent);
		List<AgentHistoAlimManuelle> list = new ArrayList<AgentHistoAlimManuelle>();
		list.add(e);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getListHisto(9005138, compteurAgent)).thenReturn(list);
		Mockito.when(counterRepository.getAgentCounterByDate(AgentAsaA48Count.class, 9005138, new DateTime(2014, 1, 24, 0, 0, 0).toDate())).thenReturn(compteurAgent);

		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		List<HistoriqueSoldeDto> listResult = service.getHistoriqueSoldeAgent(9005138, 7, new DateTime(2014, 1, 24, 0, 0, 0).toDate(), null, false);

		assertEquals(1, listResult.size());
		assertEquals(e.getText(), listResult.get(0).getTextModification());
		assertEquals(e.getDateModification(), listResult.get(0).getDateModifcation());
		assertEquals(e.getIdAgent(), listResult.get(0).getIdAgentModification());
		assertEquals(motifCompteur.getLibelle(), listResult.get(0).getMotif().getLibelle());
	}

	@Test
	public void getHistoriqueSoldeAgent_return1Liste_A54() {
		// Given

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(8);
		AgentAsaA54Count compteurAgent = new AgentAsaA54Count();
		compteurAgent.setIdAgentCount(1);
		compteurAgent.setDateDebut(new DateTime(2014, 1, 1, 0, 0, 0).toDate());
		compteurAgent.setDateFin(new DateTime(2014, 1, 31, 0, 0, 0).toDate());
		MotifCompteur motifCompteur = new MotifCompteur();
		motifCompteur.setLibelle("lib motif");
		motifCompteur.setRefTypeAbsence(type);
		AgentHistoAlimManuelle e = new AgentHistoAlimManuelle();
		e.setIdAgent(9005138);
		e.setIdAgentConcerne(9005138);
		e.setType(type);
		e.setText("texte test");
		e.setMotifCompteur(motifCompteur);
		e.setCompteurAgent(compteurAgent);
		List<AgentHistoAlimManuelle> list = new ArrayList<AgentHistoAlimManuelle>();
		list.add(e);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getListHisto(9005138, compteurAgent)).thenReturn(list);
		Mockito.when(counterRepository.getAgentCounterByDate(AgentAsaA54Count.class, 9005138, new DateTime(2014, 1, 24, 0, 0, 0).toDate())).thenReturn(compteurAgent);

		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		List<HistoriqueSoldeDto> listResult = service.getHistoriqueSoldeAgent(9005138, 8, new DateTime(2014, 1, 24, 0, 0, 0).toDate(), null, false);

		assertEquals(1, listResult.size());
		assertEquals(e.getText(), listResult.get(0).getTextModification());
		assertEquals(e.getDateModification(), listResult.get(0).getDateModifcation());
		assertEquals(e.getIdAgent(), listResult.get(0).getIdAgentModification());
		assertEquals(motifCompteur.getLibelle(), listResult.get(0).getMotif().getLibelle());
	}

	@Test
	public void getHistoriqueSoldeAgent_return1Liste_CongeAnnuel() {
		// Given

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(1);
		AgentCongeAnnuelCount compteurAgent = new AgentCongeAnnuelCount();
		compteurAgent.setIdAgentCount(1);
		MotifCompteur motifCompteur = new MotifCompteur();
		motifCompteur.setLibelle("lib motif");
		motifCompteur.setRefTypeAbsence(type);
		AgentHistoAlimManuelle e = new AgentHistoAlimManuelle();
		e.setIdAgent(9005138);
		e.setIdAgentConcerne(9005138);
		e.setType(type);
		e.setText("texte test");
		e.setMotifCompteur(motifCompteur);
		e.setCompteurAgent(compteurAgent);
		List<AgentHistoAlimManuelle> list = new ArrayList<AgentHistoAlimManuelle>();
		list.add(e);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getListHisto(9005138, compteurAgent)).thenReturn(list);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, 9005138)).thenReturn(compteurAgent);

		ICongesAnnuelsRepository congeAnnuelRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congeAnnuelRepository.getListRestitutionMassiveByIdAgent(Arrays.asList(9005138), null, null)).thenReturn(null);

		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "congeAnnuelRepository", congeAnnuelRepository);

		List<HistoriqueSoldeDto> listResult = service.getHistoriqueSoldeAgent(9005138, 1, new DateTime(2014, 1, 24, 0, 0, 0).toDate(), null, false);

		assertEquals(1, listResult.size());
		assertEquals(e.getText(), listResult.get(0).getTextModification());
		assertEquals(e.getDateModification(), listResult.get(0).getDateModifcation());
		assertEquals(e.getIdAgent(), listResult.get(0).getIdAgentModification());
		assertEquals(motifCompteur.getLibelle(), listResult.get(0).getMotif().getLibelle());
	}

	@Test
	public void getHistoriqueSoldeAgent_return1Liste_Maladies() {
		
		Date currentDay = new Date();
		
		// Given
		RefTypeAbsence type = new RefTypeAbsence();
		type.setLabel("Maladie Convalescence");
		
		DemandeMaladies demande = new DemandeMaladies();
		demande.setDuree(10.0);
		demande.setDateDebut(currentDay);
		demande.setDateFin(currentDay);
		demande.setType(type);
		demande.setTotalPris(1);
		demande.setNombreJoursCoupeDemiSalaire(2);
		demande.setNombreJoursCoupePleinSalaire(3);
		demande.setNombreJoursResteAPrendreDemiSalaire(4);
		demande.setNombreJoursResteAPrendrePleinSalaire(5);

		RefTypeAbsence type2 = new RefTypeAbsence();
		type2.setLabel("Maladie Convalescence");
		
		DemandeMaladies demande2 = new DemandeMaladies();
		demande2.setDuree(11.0);
		demande2.setDateDebut(currentDay);
		demande2.setDateFin(currentDay);
		demande2.setType(type2);
		demande2.setTotalPris(6);
		demande2.setNombreJoursCoupeDemiSalaire(7);
		demande2.setNombreJoursCoupePleinSalaire(8);
		demande2.setNombreJoursResteAPrendreDemiSalaire(9);
		demande2.setNombreJoursResteAPrendrePleinSalaire(10);
		
		List<DemandeMaladies> listMaladies = new ArrayList<DemandeMaladies>();
		listMaladies.add(demande);
		listMaladies.add(demande2);

		ICounterService maladieCounterServiceImpl = Mockito.mock(ICounterService.class);
		Mockito.when(maladieCounterServiceImpl.getHistoriqueMaladiesWithDroits(9005138, new DateTime(2014, 1, 24, 0, 0, 0).toDate()))
			.thenReturn(listMaladies);

		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "maladieCounterServiceImpl", maladieCounterServiceImpl);

		List<HistoriqueSoldeDto> listResult = service.getHistoriqueSoldeAgent(
				9005138, RefTypeAbsenceEnum.MALADIE.getValue(), new DateTime(2014, 1, 24, 0, 0, 0).toDate(), null, false);

		assertEquals(2, listResult.size());
		assertEquals(listResult.get(0).getDateDebut(), demande.getDateDebut());
		assertEquals(listResult.get(0).getDateFin(), demande.getDateFin());
		assertEquals(listResult.get(0).getTypeAbsence(), demande.getType().getLabel());
		assertEquals(listResult.get(0).getDuree(), demande.getDuree());
		assertEquals(listResult.get(0).getTotalPris(), demande.getTotalPris());
		assertEquals(listResult.get(0).getNombreJoursCoupeDemiSalaire(), demande.getNombreJoursCoupeDemiSalaire());
		assertEquals(listResult.get(0).getNombreJoursCoupePleinSalaire(), demande.getNombreJoursCoupePleinSalaire());
		assertEquals(listResult.get(0).getNombreJoursResteAPrendreDemiSalaire(), demande.getNombreJoursResteAPrendreDemiSalaire());
		assertEquals(listResult.get(0).getNombreJoursResteAPrendrePleinSalaire(), demande.getNombreJoursResteAPrendrePleinSalaire());

		assertEquals(listResult.get(1).getDateDebut(), demande2.getDateDebut());
		assertEquals(listResult.get(1).getDateFin(), demande2.getDateFin());
		assertEquals(listResult.get(1).getTypeAbsence(), demande2.getType().getLabel());
		assertEquals(listResult.get(1).getDuree(), demande2.getDuree());
		assertEquals(listResult.get(1).getTotalPris(), demande2.getTotalPris());
		assertEquals(listResult.get(1).getNombreJoursCoupeDemiSalaire(), demande2.getNombreJoursCoupeDemiSalaire());
		assertEquals(listResult.get(1).getNombreJoursCoupePleinSalaire(), demande2.getNombreJoursCoupePleinSalaire());
		assertEquals(listResult.get(1).getNombreJoursResteAPrendreDemiSalaire(), demande2.getNombreJoursResteAPrendreDemiSalaire());
		assertEquals(listResult.get(1).getNombreJoursResteAPrendrePleinSalaire(), demande2.getNombreJoursResteAPrendrePleinSalaire());
	}

	@Test
	public void getHistoriqueSoldeAgent_return1Liste_CongeAnnuel_WithRestitutionMassive() {
		// Given

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(1);
		AgentCongeAnnuelCount compteurAgent = new AgentCongeAnnuelCount();
		compteurAgent.setIdAgentCount(1);
		MotifCompteur motifCompteur = new MotifCompteur();
		motifCompteur.setLibelle("lib motif");
		motifCompteur.setRefTypeAbsence(type);
		AgentHistoAlimManuelle e = new AgentHistoAlimManuelle();
		e.setIdAgent(9005138);
		e.setIdAgentConcerne(9005138);
		e.setType(type);
		e.setText("texte test");
		e.setMotifCompteur(motifCompteur);
		e.setCompteurAgent(compteurAgent);
		e.setDateModification(new DateTime(2015, 3, 23, 0, 0, 0).toDate());
		List<AgentHistoAlimManuelle> list = new ArrayList<AgentHistoAlimManuelle>();
		list.add(e);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getListHisto(9005138, compteurAgent)).thenReturn(list);
		Mockito.when(counterRepository.getAgentCounter(AgentCongeAnnuelCount.class, 9005138)).thenReturn(compteurAgent);

		CongeAnnuelRestitutionMassive restitution = new CongeAnnuelRestitutionMassive();
		restitution.setApresMidi(true);
		restitution.setJournee(false);
		restitution.setMatin(false);
		restitution.setDateRestitution(new DateTime(2015, 1, 23, 0, 0, 0).toDate());
		restitution.setMotif("motif");

		CongeAnnuelRestitutionMassiveHisto histo = new CongeAnnuelRestitutionMassiveHisto();
		histo.setIdAgent(9005138);
		histo.setJours(1.0);
		histo.setRestitutionMassive(restitution);
		histo.setStatus("OK");

		CongeAnnuelRestitutionMassive restitution2 = new CongeAnnuelRestitutionMassive();
		restitution2.setApresMidi(true);
		restitution2.setJournee(false);
		restitution2.setMatin(false);
		restitution2.setDateRestitution(new DateTime(2015, 2, 23, 0, 0, 0).toDate());
		restitution2.setMotif("motif 2");

		CongeAnnuelRestitutionMassiveHisto histo3 = new CongeAnnuelRestitutionMassiveHisto();
		histo3.setIdAgent(9005138);
		histo3.setJours(1.0);
		histo3.setRestitutionMassive(restitution2);
		histo3.setStatus("OK");

		List<CongeAnnuelRestitutionMassiveHisto> listRestitutionMassive = new ArrayList<CongeAnnuelRestitutionMassiveHisto>();
		listRestitutionMassive.add(histo);
		listRestitutionMassive.add(histo3);

		ICongesAnnuelsRepository congeAnnuelRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		Mockito.when(congeAnnuelRepository.getListRestitutionMassiveByIdAgent(Arrays.asList(9005138), null, null)).thenReturn(listRestitutionMassive);

		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);
		ReflectionTestUtils.setField(service, "congeAnnuelRepository", congeAnnuelRepository);

		List<HistoriqueSoldeDto> listResult = service.getHistoriqueSoldeAgent(9005138, 1, new DateTime(2014, 1, 24, 0, 0, 0).toDate(), null, false);

		assertEquals(3, listResult.size());
		assertEquals(e.getText(), listResult.get(0).getTextModification());
		assertEquals(e.getDateModification(), listResult.get(0).getDateModifcation());
		assertEquals(e.getIdAgent(), listResult.get(0).getIdAgentModification());
		assertEquals(motifCompteur.getLibelle(), listResult.get(0).getMotif().getLibelle());

		assertEquals("Restitution massive de congés annuels", listResult.get(1).getTextModification());
		assertEquals(new DateTime(2015, 2, 23, 0, 0, 0).toDate(), listResult.get(1).getDateModifcation());
		assertEquals(histo3.getIdAgent(), listResult.get(1).getIdAgentModification());
		assertEquals(histo3.getRestitutionMassive().getMotif(), listResult.get(1).getMotif().getLibelle());

		assertEquals("Restitution massive de congés annuels", listResult.get(2).getTextModification());
		assertEquals(new DateTime(2015, 1, 23, 0, 0, 0).toDate(), listResult.get(2).getDateModifcation());
		assertEquals(histo.getIdAgent(), listResult.get(2).getIdAgentModification());
		assertEquals(histo.getRestitutionMassive().getMotif(), listResult.get(2).getMotif().getLibelle());
	}

	@Test
	public void getHistoriqueSoldeAgent_return1Liste_Recup() {
		// Given

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(3);
		AgentRecupCount compteurAgent = new AgentRecupCount();
		compteurAgent.setIdAgentCount(1);
		MotifCompteur motifCompteur = new MotifCompteur();
		motifCompteur.setLibelle("lib motif");
		motifCompteur.setRefTypeAbsence(type);
		AgentHistoAlimManuelle e = new AgentHistoAlimManuelle();
		e.setIdAgent(9005138);
		e.setIdAgentConcerne(9005138);
		e.setType(type);
		e.setText("texte test");
		e.setMotifCompteur(motifCompteur);
		e.setCompteurAgent(compteurAgent);
		List<AgentHistoAlimManuelle> list = new ArrayList<AgentHistoAlimManuelle>();
		list.add(e);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getListHisto(9005138, compteurAgent)).thenReturn(list);
		Mockito.when(counterRepository.getAgentCounter(AgentRecupCount.class, 9005138)).thenReturn(compteurAgent);

		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		List<HistoriqueSoldeDto> listResult = service.getHistoriqueSoldeAgent(9005138, 3, new DateTime(2014, 1, 24, 0, 0, 0).toDate(), null, false);

		assertEquals(1, listResult.size());
		assertEquals(e.getText(), listResult.get(0).getTextModification());
		assertEquals(e.getDateModification(), listResult.get(0).getDateModifcation());
		assertEquals(e.getIdAgent(), listResult.get(0).getIdAgentModification());
		assertEquals(motifCompteur.getLibelle(), listResult.get(0).getMotif().getLibelle());
	}

	@Test
	public void getHistoriqueSoldeAgent_return1Liste_ReposComp() {
		// Given

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(2);
		AgentReposCompCount compteurAgent = new AgentReposCompCount();
		compteurAgent.setIdAgentCount(1);
		MotifCompteur motifCompteur = new MotifCompteur();
		motifCompteur.setLibelle("lib motif");
		motifCompteur.setRefTypeAbsence(type);
		AgentHistoAlimManuelle e = new AgentHistoAlimManuelle();
		e.setIdAgent(9005138);
		e.setIdAgentConcerne(9005138);
		e.setType(type);
		e.setText("texte test");
		e.setMotifCompteur(motifCompteur);
		e.setCompteurAgent(compteurAgent);
		List<AgentHistoAlimManuelle> list = new ArrayList<AgentHistoAlimManuelle>();
		list.add(e);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getListHisto(9005138, compteurAgent)).thenReturn(list);
		Mockito.when(counterRepository.getAgentCounter(AgentReposCompCount.class, 9005138)).thenReturn(compteurAgent);

		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		List<HistoriqueSoldeDto> listResult = service.getHistoriqueSoldeAgent(9005138, 2, new DateTime(2014, 1, 24, 0, 0, 0).toDate(), null, false);

		assertEquals(1, listResult.size());
		assertEquals(e.getText(), listResult.get(0).getTextModification());
		assertEquals(e.getDateModification(), listResult.get(0).getDateModifcation());
		assertEquals(e.getIdAgent(), listResult.get(0).getIdAgentModification());
		assertEquals(motifCompteur.getLibelle(), listResult.get(0).getMotif().getLibelle());
	}

	@Test
	public void getHistoriqueSoldeAgent_return1Liste_A55() {
		// Given

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(9);
		AgentAsaA55Count compteurAgent = new AgentAsaA55Count();
		compteurAgent.setIdAgentCount(1);
		compteurAgent.setDateDebut(new DateTime(2014, 1, 1, 0, 0, 0).toDate());
		compteurAgent.setDateFin(new DateTime(2014, 1, 31, 0, 0, 0).toDate());
		MotifCompteur motifCompteur = new MotifCompteur();
		motifCompteur.setLibelle("lib motif");
		motifCompteur.setRefTypeAbsence(type);
		AgentHistoAlimManuelle e = new AgentHistoAlimManuelle();
		e.setIdAgent(9005138);
		e.setIdAgentConcerne(9005138);
		e.setType(type);
		e.setText("texte test");
		e.setMotifCompteur(motifCompteur);
		e.setCompteurAgent(compteurAgent);
		List<AgentHistoAlimManuelle> list = new ArrayList<AgentHistoAlimManuelle>();
		list.add(e);

		List<AgentAsaA55Count> listCompteurAgent = new ArrayList<AgentAsaA55Count>();
		listCompteurAgent.add(compteurAgent);

		ICounterRepository counterRepository = Mockito.mock(ICounterRepository.class);
		Mockito.when(counterRepository.getListHisto(9005138, compteurAgent)).thenReturn(list);
		Mockito.when(counterRepository.getListAgentCounterA55ByDate(9005138, new DateTime(2014, 1, 1, 0, 0, 0).toDate(), new DateTime(2014, 12, 31, 23, 59, 59).toDate()))
				.thenReturn(listCompteurAgent);

		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "counterRepository", counterRepository);

		List<HistoriqueSoldeDto> listResult = service.getHistoriqueSoldeAgent(9005138, 9, new DateTime(2014, 1, 1, 0, 0, 0).toDate(), new DateTime(2014, 12, 31, 23, 59, 59).toDate(), false);

		assertEquals(1, listResult.size());
		assertEquals(e.getText(), listResult.get(0).getTextModification());
		assertEquals(e.getDateModification(), listResult.get(0).getDateModifcation());
		assertEquals(e.getIdAgent(), listResult.get(0).getIdAgentModification());
		assertEquals(motifCompteur.getLibelle(), listResult.get(0).getMotif().getLibelle());
	}

	@Test
	public void getAgentSolde_GetAllSolde_WithDateDemande() throws ParseException {

		// Given
		Date dateDemande = new DateTime(2015, 03, 26, 12, 55, 0).toDate();
		Integer idAgent = 9008765;
		double cotaSoldeAnnee = 62.0;
		double cotaSoldeAnneePrec = 25.5;

		AgentRecupCount arc = new AgentRecupCount();
		arc.setIdAgent(idAgent);
		arc.setTotalMinutes(72);

		AgentReposCompCount arcc = new AgentReposCompCount();
		arcc.setIdAgent(idAgent);
		arcc.setTotalMinutes(12);
		arcc.setTotalMinutesAnneeN1(10);

		AgentAsaA48Count arccc = new AgentAsaA48Count();
		arccc.setIdAgent(idAgent);
		arccc.setTotalJours(12.0);
		arccc.setDateDebut(new DateTime(2014, 1, 1, 0, 0, 0).toDate());
		arccc.setDateFin(new DateTime(2014, 12, 31, 23, 59, 0).toDate());
		arccc.setActif(true);

		AgentAsaAmicaleCount arccAmicale = new AgentAsaAmicaleCount();
		arccAmicale.setIdAgent(idAgent);
		arccAmicale.setTotalMinutes(600);
		arccAmicale.setDateDebut(new DateTime(2014, 1, 1, 0, 0, 0).toDate());
		arccAmicale.setDateFin(new DateTime(2014, 12, 31, 23, 59, 0).toDate());
		arccAmicale.setActif(true);

		AgentAsaA54Count arcc54 = new AgentAsaA54Count();
		arcc54.setIdAgent(idAgent);
		arcc54.setTotalJours(12.0);
		arcc54.setDateDebut(new DateTime(2014, 1, 1, 0, 0, 0).toDate());
		arcc54.setDateFin(new DateTime(2014, 12, 31, 23, 59, 0).toDate());
		arcc54.setActif(true);

		AgentAsaA55Count arcc55 = new AgentAsaA55Count();
		arcc55.setIdAgent(idAgent);
		arcc55.setTotalMinutes(12 * 60);
		arcc55.setDateDebut(new DateTime(2015, 1, 1, 0, 0, 0).toDate());
		arcc55.setDateFin(new DateTime(2015, 1, 31, 23, 59, 0).toDate());

		AgentAsaA55Count arc55bis = new AgentAsaA55Count();
		arc55bis.setIdAgent(idAgent);
		arc55bis.setTotalMinutes(2 * 60);
		arc55bis.setDateDebut(new DateTime(2013, 3, 1, 0, 0, 0).toDate());
		arc55bis.setDateFin(new DateTime(2013, 3, 31, 23, 59, 0).toDate());

		List<AgentAsaA55Count> listeArc55 = new ArrayList<AgentAsaA55Count>();
		listeArc55.add(arcc55);
		listeArc55.add(arc55bis);

		AgentCongeAnnuelCount soldeCongeAnnu = new AgentCongeAnnuelCount();
		soldeCongeAnnu.setIdAgent(idAgent);
		soldeCongeAnnu.setTotalJours(cotaSoldeAnnee);
		soldeCongeAnnu.setTotalJoursAnneeN1(cotaSoldeAnneePrec);

		OrganisationSyndicale organisationSyndicale = new OrganisationSyndicale();
		organisationSyndicale.setIdOrganisationSyndicale(1);

		AgentAsaA52Count arc52 = new AgentAsaA52Count();
		arc52.setIdAgent(idAgent);
		arc52.setOrganisationSyndicale(organisationSyndicale);
		arc52.setTotalMinutes(12 * 60);
		arc52.setDateDebut(new DateTime(2014, 1, 1, 0, 0, 0).toDate());
		arc52.setDateFin(new DateTime(2014, 1, 31, 23, 59, 0).toDate());

		AgentAsaA52Count arc52bis = new AgentAsaA52Count();
		arc52bis.setIdAgent(idAgent);
		arc52bis.setOrganisationSyndicale(organisationSyndicale);
		arc52bis.setTotalMinutes(2 * 60);
		arc52bis.setDateDebut(new DateTime(2014, 3, 1, 0, 0, 0).toDate());
		arc52bis.setDateFin(new DateTime(2014, 3, 31, 23, 59, 0).toDate());

		List<AgentAsaA52Count> listeArc52 = new ArrayList<AgentAsaA52Count>();
		listeArc52.add(arc52);
		listeArc52.add(arc52bis);

		AgentOrganisationSyndicale ag = new AgentOrganisationSyndicale();
		ag.setOrganisationSyndicale(organisationSyndicale);
		List<AgentOrganisationSyndicale> list = new ArrayList<>();
		list.add(ag);

		ICounterRepository cr = Mockito.mock(ICounterRepository.class);
		Mockito.when(cr.getAgentCounter(AgentRecupCount.class, idAgent)).thenReturn(arc);
		Mockito.when(cr.getAgentCounter(AgentReposCompCount.class, idAgent)).thenReturn(arcc);
		Mockito.when(cr.getAgentCounterByDate(AgentAsaA48Count.class, 9008765, new DateTime(2014, 1, 1, 0, 0, 0).toDate())).thenReturn(arccc);
		Mockito.when(cr.getAgentCounterByDate(AgentAsaA54Count.class, 9008765, new DateTime(2014, 1, 1, 0, 0, 0).toDate())).thenReturn(arcc54);
		Mockito.when(cr.getAgentCounterByDate(AgentAsaAmicaleCount.class, 9008765, new DateTime(2013, 1, 1, 0, 0, 0).toDate())).thenReturn(arccAmicale);
		Mockito.when(cr.getAgentCounterByDate(AgentAsaA55Count.class, 9008765, dateDemande)).thenReturn(arcc55);
		Mockito.when(cr.getAgentCounter(AgentCongeAnnuelCount.class, 9008765)).thenReturn(soldeCongeAnnu);
		Mockito.when(cr.getListAgentCounterA55ByDate(9008765, new DateTime(2014, 1, 1, 0, 0, 0).toDate(), new DateTime(2014, 12, 31, 23, 59, 0).toDate())).thenReturn(listeArc55);
		Mockito.when(cr.getOSCounterByDate(AgentAsaA52Count.class, list.get(0).getOrganisationSyndicale().getIdOrganisationSyndicale(), dateDemande)).thenReturn(arc52);
		Mockito.when(
				cr.getListOSCounterByDateAndOrganisation(list.get(0).getOrganisationSyndicale().getIdOrganisationSyndicale(), new DateTime(2013, 1, 1, 0, 0, 0).toDate(), new DateTime(2014, 12, 31,
						23, 59, 0).toDate(), null)).thenReturn(listeArc52);

		AbsReposCompensateurDataConsistencyRulesImpl absDataConsistencyRules = Mockito.mock(AbsReposCompensateurDataConsistencyRulesImpl.class);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ReturnMessageDto result = (ReturnMessageDto) args[0];
				// result.getErrors().add("L'agent [%d] ne peut pas avoir de repos compensateur. Les repos compensateurs sont pour les contractuels ou les conventions collectives.");
				return result;
			}
		}).when(absDataConsistencyRules).checkStatutAgent(Mockito.isA(ReturnMessageDto.class), Mockito.isA(Integer.class), Mockito.isA(Boolean.class));

		List<SoldeSpecifiqueDto> listeSoldeSpecifiqueDto = new ArrayList<SoldeSpecifiqueDto>();

		SoldeMaladiesDto soldeMaladies = new SoldeMaladiesDto();
		soldeMaladies.setDroitsPleinSalaire(90);
		soldeMaladies.setDroitsDemiSalaire(75);
		soldeMaladies.setTotalPris(20);
		soldeMaladies.setRapPleinSalaire(80);
		soldeMaladies.setRapDemiSalaire(50);
		
		MaladieCounterServiceImpl maladieCounterServiceImpl = Mockito.mock(MaladieCounterServiceImpl.class);
		Mockito.when(maladieCounterServiceImpl.getSoldeByAgent(Mockito.anyInt(), Mockito.any(Date.class), (AgentGeneriqueDto)Mockito.any())).thenReturn(soldeMaladies);

		CongesExcepCounterServiceImpl congesExcepCounterServiceImpl = Mockito.mock(CongesExcepCounterServiceImpl.class);
		Mockito.when(congesExcepCounterServiceImpl.getListAgentCounterByDate(idAgent, null, null)).thenReturn(listeSoldeSpecifiqueDto);

		IOrganisationSyndicaleRepository organisationSyndicaleRepository = Mockito.mock(IOrganisationSyndicaleRepository.class);
		Mockito.when(organisationSyndicaleRepository.getAgentOrganisationActif(idAgent)).thenReturn(list);

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getNombreSamediOffertSurAnnee(idAgent, 2015, null)).thenReturn(1);

		List<DemandeAsa> listAsa = new ArrayList<DemandeAsa>();
		IAsaRepository asaRepository = Mockito.mock(IAsaRepository.class);
		Mockito.when(asaRepository.getListDemandeAsaPourMoisByOS(Mockito.anyInt(), (Integer) Mockito.any(), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.anyInt())).thenReturn(listAsa);

		ICongesAnnuelsRepository congeAnnuelRepository = Mockito.mock(ICongesAnnuelsRepository.class);
		IReposCompensateurRepository reposCompensateurRepository = Mockito.mock(IReposCompensateurRepository.class);
		IRecuperationRepository recuperationRepository = Mockito.mock(IRecuperationRepository.class);
		IAbsenceDataConsistencyRules absAsaA48DataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		IAbsenceDataConsistencyRules absAsaA52DataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		IAbsenceDataConsistencyRules absAsaA54DataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		IAbsenceDataConsistencyRules absAsaA55DataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		IAbsenceDataConsistencyRules absAsaAmicaleDataConsistencyRulesImpl = Mockito.mock(IAbsenceDataConsistencyRules.class);
		
		SoldeService service = new SoldeService();
		ReflectionTestUtils.setField(service, "counterRepository", cr);
		ReflectionTestUtils.setField(service, "absReposCompDataConsistencyRules", absDataConsistencyRules);
		ReflectionTestUtils.setField(service, "congesExcepCounterServiceImpl", congesExcepCounterServiceImpl);
		ReflectionTestUtils.setField(service, "organisationSyndicaleRepository", organisationSyndicaleRepository);
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(service, "asaRepository", asaRepository);
		ReflectionTestUtils.setField(service, "maladieCounterServiceImpl", maladieCounterServiceImpl);
		ReflectionTestUtils.setField(service, "congeAnnuelRepository", congeAnnuelRepository);
		ReflectionTestUtils.setField(service, "reposCompensateurRepository", reposCompensateurRepository);
		ReflectionTestUtils.setField(service, "recuperationRepository", recuperationRepository);
		ReflectionTestUtils.setField(service, "absAsaA48DataConsistencyRulesImpl", absAsaA48DataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "absAsaA52DataConsistencyRulesImpl", absAsaA52DataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "absAsaA54DataConsistencyRulesImpl", absAsaA54DataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "absAsaA55DataConsistencyRulesImpl", absAsaA55DataConsistencyRulesImpl);
		ReflectionTestUtils.setField(service, "absAsaAmicaleDataConsistencyRulesImpl", absAsaAmicaleDataConsistencyRulesImpl);

		Date dateDeb = new DateTime(2013, 1, 1, 0, 0, 0).toDate();
		Date dateFin = new DateTime(2014, 12, 31, 23, 59, 0).toDate();
		SoldeDto dto = service.getAgentSolde(idAgent, dateDeb, dateFin, null, dateDemande);

		assertEquals("72.0", dto.getSoldeRecup().toString());
		assertEquals("62.0", dto.getSoldeCongeAnnee().toString());
		assertEquals("25.5", dto.getSoldeCongeAnneePrec().toString());
		assertEquals("12.0", dto.getSoldeReposCompAnnee().toString());
		assertEquals("10.0", dto.getSoldeReposCompAnneePrec().toString());
		assertEquals(0, dto.getSoldeAsaA48().intValue());
		assertEquals(0, dto.getSoldeAsaA54().intValue());
		assertEquals(720, dto.getSoldeAsaA55().intValue());
		assertTrue(dto.isAfficheSoldeConge());
		assertTrue(dto.isAfficheSoldeRecup());
		assertTrue(dto.isAfficheSoldeReposComp());
		assertFalse(dto.isAfficheSoldeAsaA48());
		assertFalse(dto.isAfficheSoldeAsaA54());
		assertTrue(dto.isAfficheSoldeAsaA55());
		assertTrue(dto.isAfficheSoldeAsaAmicale());
		assertEquals(600, dto.getSoldeAsaAmicale().intValue());
		assertEquals(0, dto.getListeSoldeAsaA55().size());
		assertFalse(dto.isAfficheSoldeCongesExcep());
		assertEquals(0, dto.getListeSoldeCongesExcep().size());
		assertEquals(12 * 60, dto.getSoldeAsaA52().intValue());
		assertTrue(dto.isAfficheSoldeAsaA52());
		assertEquals(2, dto.getListeSoldeAsaA52().size());
		assertEquals(soldeMaladies.getDroitsPleinSalaire(), dto.getSoldeMaladies().getDroitsPleinSalaire());
		assertEquals(soldeMaladies.getDroitsDemiSalaire(), dto.getSoldeMaladies().getDroitsDemiSalaire());
		assertEquals(soldeMaladies.getRapPleinSalaire(), dto.getSoldeMaladies().getRapPleinSalaire());
		assertEquals(soldeMaladies.getRapDemiSalaire(), dto.getSoldeMaladies().getRapDemiSalaire());
		assertEquals(soldeMaladies.getTotalPris(), dto.getSoldeMaladies().getTotalPris());
	}
}
