package nc.noumea.mairie.abs.service.counter.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import nc.noumea.mairie.abs.domain.DemandeMaladies;
import nc.noumea.mairie.abs.domain.RefDroitsMaladies;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.dto.AgentGeneriqueDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
import nc.noumea.mairie.abs.repository.IMaladiesRepository;
import nc.noumea.mairie.abs.repository.ISirhRepository;
import nc.noumea.mairie.abs.service.IAgentMatriculeConverterService;
import nc.noumea.mairie.abs.service.impl.HelperService;
import nc.noumea.mairie.abs.vo.CalculDroitsMaladiesVo;
import nc.noumea.mairie.domain.Spadmn;
import nc.noumea.mairie.domain.SpadmnId;
import nc.noumea.mairie.domain.Spcarr;
import nc.noumea.mairie.ws.ISirhWSConsumer;

public class MaladieCounterServiceImplTest extends AbstractCounterServiceTest {

	private MaladieCounterServiceImpl service = new MaladieCounterServiceImpl();

	private RefTypeAbsence type;

	IAgentMatriculeConverterService agentMatriculeService = Mockito.mock(IAgentMatriculeConverterService.class);
	IMaladiesRepository maladiesRepository = Mockito.mock(IMaladiesRepository.class);
	ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
	HelperService helperService = Mockito.mock(HelperService.class);
	ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);

	@Before
	public void init() {
		type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.MALADIE.getValue());
		type.setLabel("Maladies");
	}

	@Test
	public void testMethodeParenteHeritage() {
		super.allTest(new RecupCounterServiceImpl());
	}

	private void prepareTests(Integer idAgent, Date dateFinAnneeGlissante,
			RefDroitsMaladies droitsMaladies, List<DemandeMaladies> listMaladies) {
		
		Date dateDebutAnneeGlissante = new DateTime(dateFinAnneeGlissante)
				.minusYears(1).plusDays(1).withMillisOfDay(0).toDate();

		Integer noMatr = idAgent - 9000000;
		Mockito.when(
				agentMatriculeService.fromIdAgentToSIRHNomatrAgent(idAgent))
				.thenReturn(noMatr);
		Spcarr carr = new Spcarr();
		Mockito.when(
				sirhRepository.getAgentCurrentCarriere(agentMatriculeService
						.fromIdAgentToSIRHNomatrAgent(idAgent),
						dateFinAnneeGlissante)).thenReturn(carr);

		Mockito.when(
				maladiesRepository.getDroitsMaladies(Mockito.anyBoolean(),
						Mockito.anyBoolean(), Mockito.anyBoolean(),
						Mockito.anyInt())).thenReturn(droitsMaladies);

		Mockito.when(
				maladiesRepository.getListMaladiesAnneGlissanteByAgent(idAgent,
						dateDebutAnneeGlissante, dateFinAnneeGlissante))
				.thenReturn(listMaladies);
		Mockito.when(
				sirhWSConsumer.isPeriodeEssai(idAgent, dateFinAnneeGlissante))
				.thenReturn(false);

		AgentGeneriqueDto agentDto = new AgentGeneriqueDto();
		agentDto.setIdAgent(idAgent);
		agentDto.setNomatr(idAgent - 9000000);
		agentDto.setDateDerniereEmbauche(new DateTime(2010, 1, 1, 0, 0, 0).toDate());
		
		Mockito.when(sirhWSConsumer.getAgent(idAgent)).thenReturn(agentDto);
		
		Mockito.when(maladiesRepository.getListMaladiesAnneGlissanteRetroactiveByAgent(idAgent, dateDebutAnneeGlissante, dateFinAnneeGlissante, null, false, false))
		.thenReturn(listMaladies);
		
		Mockito.when(maladiesRepository.getListMaladiesAnneGlissanteRetroactiveByAgent(idAgent, dateDebutAnneeGlissante, dateFinAnneeGlissante, null, false, true))
		.thenReturn(listMaladies);

		ReflectionTestUtils.setField(service, "maladiesRepository",
				maladiesRepository);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService",
				agentMatriculeService);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
	}

	// /////////////////////////////////////////////////////////////
	// Tests extraits du fichier excel SFD_maladies_Exemples.xls //
	// /////////////////////////////////////////////////////////////

	// fonctionnaires
	@Test
	public void getSoldeAgent_calculMaladies1() {

		Integer idAgent = 9003309;

		AgentGeneriqueDto agentDto = new AgentGeneriqueDto();
		agentDto.setIdAgent(idAgent);
		agentDto.setNomatr(idAgent - 9000000);
		agentDto.setDateDerniereEmbauche(new DateTime(2010, 1, 1, 0, 0, 0).toDate());

		RefDroitsMaladies droitsMaladies = new RefDroitsMaladies();
		droitsMaladies.setAnneeAnciennete(0);
		droitsMaladies.setFonctionnaire(true);
		droitsMaladies.setNombreJoursPleinSalaire(90);
		droitsMaladies.setNombreJoursDemiSalaire(90);

		List<DemandeMaladies> listMaladies = new ArrayList<DemandeMaladies>();

		DemandeMaladies demandeMaladiePrcdte = new DemandeMaladies();
		demandeMaladiePrcdte.setType(type);
		demandeMaladiePrcdte.setDateDebut(new DateTime(2011, 5, 3, 0, 0, 0).toDate());
		demandeMaladiePrcdte.setDateFin(new DateTime(2011, 5, 10, 23, 59, 59).toDate());
		demandeMaladiePrcdte.setDuree(7.0);
		demandeMaladiePrcdte.setIdAgent(idAgent);
		demandeMaladiePrcdte.setNombreJoursCoupePleinSalaire(0);
		demandeMaladiePrcdte.setNombreJoursCoupeDemiSalaire(0);
		listMaladies.add(demandeMaladiePrcdte);

		// ligne DE1
		DemandeMaladies demandeMaladie1 = new DemandeMaladies();
		demandeMaladie1.setType(type);
		demandeMaladie1.setDateDebut(new DateTime(2011, 10, 3, 0, 0, 0).toDate());
		demandeMaladie1.setDateFin(new DateTime(2011, 10, 5, 23, 59, 59).toDate());
		demandeMaladie1.setDuree(3.0);
		demandeMaladie1.setIdAgent(idAgent);
		demandeMaladie1.setNombreJoursCoupePleinSalaire(0);
		demandeMaladie1.setNombreJoursCoupeDemiSalaire(0);
		
		prepareTests(idAgent, demandeMaladie1.getDateFin(), droitsMaladies, listMaladies);

		listMaladies.add(demandeMaladie1);

		CalculDroitsMaladiesVo result = service.calculDroitsMaladies(idAgent,
				demandeMaladie1.getDateFin(), demandeMaladie1.getIdDemande(),
				demandeMaladie1.getDuree(), false, false);

		assertEquals(result.getDroitsPleinSalaire().intValue(), 90);
		assertEquals(result.getDroitsDemiSalaire().intValue(), 90);
		assertEquals(result.getNombreJoursResteAPrendrePleinSalaire().intValue(), 80);
		assertEquals(result.getNombreJoursResteAPrendreDemiSalaire().intValue(), 90);
		assertEquals(result.getNombreJoursCoupePleinSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupeDemiSalaire().intValue(), 0);
		assertEquals(result.getTotalPris().intValue(), 10);

		// ligne DE2
		DemandeMaladies demandeMaladie2 = new DemandeMaladies();
		demandeMaladie2.setType(type);
		demandeMaladie2.setDateDebut(new DateTime(2012, 6, 4, 0, 0, 0).toDate());
		demandeMaladie2.setDateFin(new DateTime(2012, 6, 5, 23, 59, 59).toDate());
		demandeMaladie2.setDuree(2.0);
		demandeMaladie2.setIdAgent(idAgent);
		demandeMaladie2.setNombreJoursCoupePleinSalaire(0);
		demandeMaladie2.setNombreJoursCoupeDemiSalaire(0);

		prepareTests(idAgent, demandeMaladie2.getDateFin(), droitsMaladies, listMaladies);

		listMaladies.add(demandeMaladie2);

		result = service.calculDroitsMaladies(idAgent,
				demandeMaladie2.getDateFin(), demandeMaladie2.getIdDemande(),
				demandeMaladie2.getDuree(), false, false);

		assertEquals(result.getDroitsPleinSalaire().intValue(), 90);
		assertEquals(result.getDroitsDemiSalaire().intValue(), 90);
		assertEquals(result.getNombreJoursResteAPrendrePleinSalaire().intValue(), 85);
		assertEquals(result.getNombreJoursResteAPrendreDemiSalaire().intValue(), 90);
		assertEquals(result.getNombreJoursCoupePleinSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupeDemiSalaire().intValue(), 0);
		assertEquals(result.getTotalPris().intValue(), 5);

		// ligne DE3
		DemandeMaladies demandeMaladie3 = new DemandeMaladies();
		demandeMaladie3.setType(type);
		demandeMaladie3
				.setDateDebut(new DateTime(2012, 6, 6, 0, 0, 0).toDate());
		demandeMaladie3.setDateFin(new DateTime(2012, 6, 12, 23, 59, 59)
				.toDate());
		demandeMaladie3.setDuree(7.0);
		demandeMaladie3.setIdAgent(idAgent);
		demandeMaladie3.setNombreJoursCoupePleinSalaire(0);
		demandeMaladie3.setNombreJoursCoupeDemiSalaire(0);

		prepareTests(idAgent, demandeMaladie3.getDateFin(), droitsMaladies,
				listMaladies);

		listMaladies.add(demandeMaladie3);

		result = service.calculDroitsMaladies(idAgent,
				demandeMaladie3.getDateFin(), demandeMaladie3.getIdDemande(),
				demandeMaladie3.getDuree(), false, false);

		assertEquals(result.getDroitsPleinSalaire().intValue(), 90);
		assertEquals(result.getDroitsDemiSalaire().intValue(), 90);
		assertEquals(result.getNombreJoursResteAPrendrePleinSalaire()
				.intValue(), 78);
		assertEquals(
				result.getNombreJoursResteAPrendreDemiSalaire().intValue(), 90);
		assertEquals(result.getNombreJoursCoupePleinSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupeDemiSalaire().intValue(), 0);
		assertEquals(result.getTotalPris().intValue(), 12);

		// ligne DE4
		DemandeMaladies demandeMaladie4 = new DemandeMaladies();
		demandeMaladie4.setType(type);
		demandeMaladie4.setDateDebut(new DateTime(2012, 6, 13, 0, 0, 0)
				.toDate());
		demandeMaladie4.setDateFin(new DateTime(2012, 6, 18, 23, 59, 59)
				.toDate());
		demandeMaladie4.setDuree(6.0);
		demandeMaladie4.setIdAgent(idAgent);
		demandeMaladie4.setNombreJoursCoupePleinSalaire(0);
		demandeMaladie4.setNombreJoursCoupeDemiSalaire(0);

		prepareTests(idAgent, demandeMaladie4.getDateFin(), droitsMaladies,
				listMaladies);

		listMaladies.add(demandeMaladie4);

		result = service.calculDroitsMaladies(idAgent,
				demandeMaladie4.getDateFin(), demandeMaladie4.getIdDemande(),
				demandeMaladie4.getDuree(), false, false);

		assertEquals(result.getDroitsPleinSalaire().intValue(), 90);
		assertEquals(result.getDroitsDemiSalaire().intValue(), 90);
		assertEquals(result.getNombreJoursResteAPrendrePleinSalaire()
				.intValue(), 72);
		assertEquals(
				result.getNombreJoursResteAPrendreDemiSalaire().intValue(), 90);
		assertEquals(result.getNombreJoursCoupePleinSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupeDemiSalaire().intValue(), 0);
		assertEquals(result.getTotalPris().intValue(), 18);

		// ligne DE5
		DemandeMaladies demandeMaladie5 = new DemandeMaladies();
		demandeMaladie5.setType(type);
		demandeMaladie5.setDateDebut(new DateTime(2012, 6, 19, 0, 0, 0)
				.toDate());
		demandeMaladie5.setDateFin(new DateTime(2012, 6, 22, 23, 59, 59)
				.toDate());
		demandeMaladie5.setDuree(4.0);
		demandeMaladie5.setIdAgent(idAgent);
		demandeMaladie5.setNombreJoursCoupePleinSalaire(0);
		demandeMaladie5.setNombreJoursCoupeDemiSalaire(0);

		prepareTests(idAgent, demandeMaladie5.getDateFin(), droitsMaladies,
				listMaladies);

		listMaladies.add(demandeMaladie5);

		result = service.calculDroitsMaladies(idAgent,
				demandeMaladie5.getDateFin(), demandeMaladie5.getIdDemande(),
				demandeMaladie5.getDuree(), false, false);

		assertEquals(result.getDroitsPleinSalaire().intValue(), 90);
		assertEquals(result.getDroitsDemiSalaire().intValue(), 90);
		assertEquals(result.getNombreJoursResteAPrendrePleinSalaire()
				.intValue(), 68);
		assertEquals(
				result.getNombreJoursResteAPrendreDemiSalaire().intValue(), 90);
		assertEquals(result.getNombreJoursCoupePleinSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupeDemiSalaire().intValue(), 0);
		assertEquals(result.getTotalPris().intValue(), 22);

		// ligne DE6
		DemandeMaladies demandeMaladie6 = new DemandeMaladies();
		demandeMaladie6.setType(type);
		demandeMaladie6.setDateDebut(new DateTime(2012, 6, 25, 0, 0, 0)
				.toDate());
		demandeMaladie6.setDateFin(new DateTime(2012, 7, 9, 23, 59, 59)
				.toDate());
		demandeMaladie6.setDuree(15.0);
		demandeMaladie6.setIdAgent(idAgent);
		demandeMaladie6.setNombreJoursCoupePleinSalaire(0);
		demandeMaladie6.setNombreJoursCoupeDemiSalaire(0);

		prepareTests(idAgent, demandeMaladie6.getDateFin(), droitsMaladies,
				listMaladies);

		listMaladies.add(demandeMaladie6);

		result = service.calculDroitsMaladies(idAgent,
				demandeMaladie6.getDateFin(), demandeMaladie6.getIdDemande(),
				demandeMaladie6.getDuree(), false, false);

		assertEquals(result.getDroitsPleinSalaire().intValue(), 90);
		assertEquals(result.getDroitsDemiSalaire().intValue(), 90);
		assertEquals(result.getNombreJoursResteAPrendrePleinSalaire()
				.intValue(), 53);
		assertEquals(
				result.getNombreJoursResteAPrendreDemiSalaire().intValue(), 90);
		assertEquals(result.getNombreJoursCoupePleinSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupeDemiSalaire().intValue(), 0);
		assertEquals(result.getTotalPris().intValue(), 37);

		// ligne DE7
		DemandeMaladies demandeMaladie7 = new DemandeMaladies();
		demandeMaladie7.setType(type);
		demandeMaladie7.setDateDebut(new DateTime(2012, 7, 10, 0, 0, 0)
				.toDate());
		demandeMaladie7.setDateFin(new DateTime(2012, 7, 23, 23, 59, 59)
				.toDate());
		demandeMaladie7.setDuree(14.0);
		demandeMaladie7.setIdAgent(idAgent);
		demandeMaladie7.setNombreJoursCoupePleinSalaire(0);
		demandeMaladie7.setNombreJoursCoupeDemiSalaire(0);

		prepareTests(idAgent, demandeMaladie7.getDateFin(), droitsMaladies,
				listMaladies);

		listMaladies.add(demandeMaladie7);

		result = service.calculDroitsMaladies(idAgent,
				demandeMaladie7.getDateFin(), demandeMaladie7.getIdDemande(),
				demandeMaladie7.getDuree(), false, false);

		assertEquals(result.getDroitsPleinSalaire().intValue(), 90);
		assertEquals(result.getDroitsDemiSalaire().intValue(), 90);
		assertEquals(result.getNombreJoursResteAPrendrePleinSalaire()
				.intValue(), 39);
		assertEquals(
				result.getNombreJoursResteAPrendreDemiSalaire().intValue(), 90);
		assertEquals(result.getNombreJoursCoupePleinSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupeDemiSalaire().intValue(), 0);
		assertEquals(result.getTotalPris().intValue(), 51);

		// ligne DE8
		DemandeMaladies demandeMaladie8 = new DemandeMaladies();
		demandeMaladie8.setType(type);
		demandeMaladie8.setDateDebut(new DateTime(2012, 7, 24, 0, 0, 0)
				.toDate());
		demandeMaladie8.setDateFin(new DateTime(2012, 8, 6, 23, 59, 59)
				.toDate());
		demandeMaladie8.setDuree(14.0);
		demandeMaladie8.setIdAgent(idAgent);
		demandeMaladie8.setNombreJoursCoupePleinSalaire(0);
		demandeMaladie8.setNombreJoursCoupeDemiSalaire(0);

		prepareTests(idAgent, demandeMaladie8.getDateFin(), droitsMaladies,
				listMaladies);

		listMaladies.add(demandeMaladie8);

		result = service.calculDroitsMaladies(idAgent,
				demandeMaladie8.getDateFin(), demandeMaladie8.getIdDemande(),
				demandeMaladie8.getDuree(), false, false);

		assertEquals(result.getDroitsPleinSalaire().intValue(), 90);
		assertEquals(result.getDroitsDemiSalaire().intValue(), 90);
		assertEquals(result.getNombreJoursResteAPrendrePleinSalaire()
				.intValue(), 25);
		assertEquals(
				result.getNombreJoursResteAPrendreDemiSalaire().intValue(), 90);
		assertEquals(result.getNombreJoursCoupePleinSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupeDemiSalaire().intValue(), 0);
		assertEquals(result.getTotalPris().intValue(), 65);

		// ligne DE9
		DemandeMaladies demandeMaladie9 = new DemandeMaladies();
		demandeMaladie9.setType(type);
		demandeMaladie9
				.setDateDebut(new DateTime(2012, 8, 7, 0, 0, 0).toDate());
		demandeMaladie9.setDateFin(new DateTime(2012, 9, 6, 23, 59, 59)
				.toDate());
		demandeMaladie9.setDuree(31.0);
		demandeMaladie9.setIdAgent(idAgent);
		demandeMaladie9.setNombreJoursCoupePleinSalaire(0);
		demandeMaladie9.setNombreJoursCoupeDemiSalaire(6);

		prepareTests(idAgent, demandeMaladie9.getDateFin(), droitsMaladies,
				listMaladies);

		listMaladies.add(demandeMaladie9);

		result = service.calculDroitsMaladies(idAgent,
				demandeMaladie9.getDateFin(), demandeMaladie9.getIdDemande(),
				demandeMaladie9.getDuree(), false, false);

		assertEquals(result.getDroitsPleinSalaire().intValue(), 90);
		assertEquals(result.getDroitsDemiSalaire().intValue(), 90);
		assertEquals(result.getNombreJoursResteAPrendrePleinSalaire()
				.intValue(), 0);
		assertEquals(
				result.getNombreJoursResteAPrendreDemiSalaire().intValue(), 84);
		assertEquals(result.getNombreJoursCoupePleinSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupeDemiSalaire().intValue(), 6);
		assertEquals(result.getTotalPris().intValue(), 96);

		// ligne DE10
		DemandeMaladies demandeMaladie10 = new DemandeMaladies();
		demandeMaladie10.setType(type);
		demandeMaladie10.setDateDebut(new DateTime(2012, 9, 7, 0, 0, 0).toDate());
		demandeMaladie10.setDateFin(new DateTime(2012, 10, 5, 23, 59, 59).toDate());
		demandeMaladie10.setDuree(29.0);
		demandeMaladie10.setIdAgent(idAgent);

		prepareTests(idAgent, demandeMaladie10.getDateFin(), droitsMaladies, listMaladies);

		listMaladies.add(demandeMaladie10);

		result = service.calculDroitsMaladies(idAgent,
				demandeMaladie10.getDateFin(), demandeMaladie10.getIdDemande(),
				demandeMaladie10.getDuree(), false, false);

		assertEquals(result.getDroitsPleinSalaire().intValue(), 90);
		assertEquals(result.getDroitsDemiSalaire().intValue(), 90);
		assertEquals(result.getNombreJoursResteAPrendrePleinSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursResteAPrendreDemiSalaire().intValue(), 58);
		assertEquals(result.getNombreJoursCoupePleinSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupeDemiSalaire().intValue(), 29);
		assertEquals(result.getTotalPris().intValue(), 122);
	}

	@Test
	public void getSoldeAgent_withCalculRetroactif() {

		Integer idAgent = 9003309;

		AgentGeneriqueDto agentDto = new AgentGeneriqueDto();
		agentDto.setIdAgent(idAgent);
		agentDto.setNomatr(idAgent - 9000000);
		agentDto.setDateDerniereEmbauche(new DateTime(2013, 10, 1, 0, 0, 0)
				.toDate());

		SpadmnId id2 = new SpadmnId();
		id2.setDatdeb(20140101);
		id2.setNomatr(5138);
		Spadmn pa = new Spadmn();
		pa.setId(id2);
		pa.setCdpadm("50");
		pa.setDatfin(20150101);

		List<Spadmn> listPA50 = new ArrayList<Spadmn>();
		listPA50.add(pa);
		
		Mockito.when(sirhWSConsumer.getAgent(idAgent)).thenReturn(agentDto);

		RefDroitsMaladies droitsMaladies = new RefDroitsMaladies();
		droitsMaladies.setAnneeAnciennete(5);
		droitsMaladies.setConventionCollective(true);
		droitsMaladies.setNombreJoursPleinSalaire(20);
		droitsMaladies.setNombreJoursDemiSalaire(15);

		List<DemandeMaladies> listMaladies = new ArrayList<DemandeMaladies>();

		// ligne DE1
		DemandeMaladies demandeMaladie1 = new DemandeMaladies();
		demandeMaladie1.setType(type);
		demandeMaladie1.setDateDebut(new DateTime(2014, 2, 20, 0, 0, 0).toDate());
		demandeMaladie1.setDateFin(new DateTime(2014, 2, 28, 23, 59, 59).toDate());
		demandeMaladie1.setDuree(9.0);
		demandeMaladie1.setIdAgent(idAgent);
		
		listMaladies.add(demandeMaladie1);

		prepareTests(idAgent, demandeMaladie1.getDateFin(), droitsMaladies, listMaladies);
		CalculDroitsMaladiesVo result = service.calculDroitsMaladies(idAgent,
				demandeMaladie1.getDateFin(), demandeMaladie1.getIdDemande(), demandeMaladie1.getDuree(), false, false);

		assertEquals(20, result.getDroitsPleinSalaire().intValue());
		assertEquals(15, result.getDroitsDemiSalaire().intValue());
		assertEquals(11, result.getNombreJoursResteAPrendrePleinSalaire().intValue());
		assertEquals(15, result.getNombreJoursResteAPrendreDemiSalaire().intValue());
		assertEquals(0, result.getNombreJoursCoupePleinSalaire().intValue());
		assertEquals(0, result.getNombreJoursCoupeDemiSalaire().intValue());
		assertEquals(9, result.getTotalPris().intValue());

		// ligne DE2
		DemandeMaladies demandeMaladie2 = new DemandeMaladies();
		demandeMaladie2.setType(type);
		demandeMaladie2.setDateDebut(new DateTime(2014, 1, 1, 0, 0, 0).toDate());
		demandeMaladie2.setDateFin(new DateTime(2014, 1, 2, 23, 59, 59).toDate());
		demandeMaladie2.setDuree(2.0);
		demandeMaladie2.setIdAgent(idAgent);
		demandeMaladie2.setNombreJoursCoupePleinSalaire(0);
		demandeMaladie2.setNombreJoursCoupeDemiSalaire(0);

		listMaladies.add(demandeMaladie2);
		listMaladies.remove(demandeMaladie1);
		
		prepareTests(idAgent, demandeMaladie2.getDateFin(), droitsMaladies, listMaladies);
		result = service.calculDroitsMaladies(idAgent,
				demandeMaladie2.getDateFin(), demandeMaladie2.getIdDemande(), demandeMaladie2.getDuree(), false, false);

		assertEquals(20, result.getDroitsPleinSalaire().intValue());
		assertEquals(15, result.getDroitsDemiSalaire().intValue());
		assertEquals(18, result.getNombreJoursResteAPrendrePleinSalaire().intValue());
		assertEquals(15, result.getNombreJoursResteAPrendreDemiSalaire().intValue());
		assertEquals(0, result.getNombreJoursCoupePleinSalaire().intValue());
		assertEquals(0, result.getNombreJoursCoupeDemiSalaire().intValue());
		assertEquals(2, result.getTotalPris().intValue());
	}

	@Test
	public void getSoldeRetroactif() {

		Integer idAgent = 9003309;

		AgentGeneriqueDto agentDto = new AgentGeneriqueDto();
		agentDto.setIdAgent(idAgent);
		agentDto.setNomatr(idAgent - 9000000);
		agentDto.setDateDerniereEmbauche(new DateTime(2013, 10, 1, 0, 0, 0).toDate());

		RefDroitsMaladies droitsMaladies = new RefDroitsMaladies();
		droitsMaladies.setAnneeAnciennete(10);
		droitsMaladies.setFonctionnaire(true);
		droitsMaladies.setNombreJoursPleinSalaire(15);
		droitsMaladies.setNombreJoursDemiSalaire(10);

		List<DemandeMaladies> listMaladies = new ArrayList<DemandeMaladies>();

		// ligne DE1
		DemandeMaladies demandeMaladie1 = new DemandeMaladies();
		demandeMaladie1.setType(type);
		demandeMaladie1.setDateDebut(new DateTime(2014, 2, 20, 0, 0, 0).toDate());
		demandeMaladie1.setDateFin(new DateTime(2014, 2, 28, 23, 59, 59).toDate());
		demandeMaladie1.setDuree(8.0);
		demandeMaladie1.setIdAgent(idAgent);
		
		listMaladies.add(demandeMaladie1);

		Date dateDebutAnneeGlissante = new DateTime(demandeMaladie1.getDateFin()).minusYears(1).plusDays(1)
				.withMillisOfDay(0).toDate();

		Integer noMatr = idAgent - 9000000;
		Mockito.when(agentMatriculeService.fromIdAgentToSIRHNomatrAgent(idAgent)).thenReturn(noMatr);
		Spcarr carr = new Spcarr();
		
		Mockito.when(sirhRepository.getAgentCurrentCarriere(agentMatriculeService
						.fromIdAgentToSIRHNomatrAgent(idAgent), demandeMaladie1.getDateFin())).thenReturn(carr);

		Mockito.when(maladiesRepository.getListMaladiesAnneGlissanteRetroactiveByAgent(idAgent,
						dateDebutAnneeGlissante, demandeMaladie1.getDateFin(), demandeMaladie1.getIdDemande(), false, true)).thenReturn(listMaladies);
		
		Mockito.when(sirhWSConsumer.getAgent(idAgent)).thenReturn(agentDto);

		Mockito.when(maladiesRepository.getDroitsMaladies(Mockito.anyBoolean(),
						Mockito.anyBoolean(), Mockito.anyBoolean(),
						Mockito.anyInt())).thenReturn(droitsMaladies);
		
		prepareTests(idAgent, demandeMaladie1.getDateFin(), droitsMaladies, listMaladies);

		ReflectionTestUtils.setField(service, "maladiesRepository", maladiesRepository);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService", agentMatriculeService);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		
		CalculDroitsMaladiesVo result = service.calculDroitsMaladies(idAgent, demandeMaladie1.getDateFin(), 
				demandeMaladie1.getIdDemande(), demandeMaladie1.getDuree(), false, true);

		assertEquals(15, result.getDroitsPleinSalaire().intValue());
		assertEquals(10, result.getDroitsDemiSalaire().intValue());
		assertEquals(7, result.getNombreJoursResteAPrendrePleinSalaire().intValue());
		assertEquals(10, result.getNombreJoursResteAPrendreDemiSalaire().intValue());
		assertEquals(0, result.getNombreJoursCoupePleinSalaire().intValue());
		assertEquals(0, result.getNombreJoursCoupeDemiSalaire().intValue());
		assertEquals(8, result.getTotalPris().intValue());
		
		
		// 2e saisie
		DemandeMaladies demandeMaladie2 = new DemandeMaladies();
		demandeMaladie2.setType(type);
		demandeMaladie2.setDateDebut(new DateTime(2014, 3, 10, 0, 0, 0).toDate());
		demandeMaladie2.setDateFin(new DateTime(2014, 3, 20, 23, 59, 59).toDate());
		demandeMaladie2.setDuree(10.0);
		demandeMaladie2.setIdAgent(idAgent);
		
		listMaladies.add(demandeMaladie2);

		dateDebutAnneeGlissante = new DateTime(demandeMaladie2.getDateFin()).minusYears(1).plusDays(1)
				.withMillisOfDay(0).toDate();
		
		Mockito.when(sirhRepository.getAgentCurrentCarriere(agentMatriculeService
						.fromIdAgentToSIRHNomatrAgent(idAgent), demandeMaladie2.getDateFin())).thenReturn(carr);

		Mockito.when(maladiesRepository.getListMaladiesAnneGlissanteRetroactiveByAgent(idAgent,
						dateDebutAnneeGlissante, demandeMaladie2.getDateFin(), demandeMaladie2.getIdDemande(), false, true)).thenReturn(listMaladies);
		
		result = service.calculDroitsMaladies(idAgent, demandeMaladie2.getDateFin(), 
				demandeMaladie2.getIdDemande(), demandeMaladie2.getDuree(), false, true);

		assertEquals(15, result.getDroitsPleinSalaire().intValue());
		assertEquals(10, result.getDroitsDemiSalaire().intValue());
		assertEquals(0, result.getNombreJoursResteAPrendrePleinSalaire().intValue());
		assertEquals(7, result.getNombreJoursResteAPrendreDemiSalaire().intValue());
		assertEquals(0, result.getNombreJoursCoupePleinSalaire().intValue());
		assertEquals(3, result.getNombreJoursCoupeDemiSalaire().intValue());
		assertEquals(18, result.getTotalPris().intValue());
		
		
		// 3e saisie
		DemandeMaladies demandeMaladie3 = new DemandeMaladies();
		demandeMaladie3.setType(type);
		demandeMaladie3.setDateDebut(new DateTime(2014, 4, 10, 0, 0, 0).toDate());
		demandeMaladie3.setDateFin(new DateTime(2014, 4, 22, 23, 59, 59).toDate());
		demandeMaladie3.setDuree(12.0);
		demandeMaladie3.setIdAgent(idAgent);
		
		listMaladies.add(demandeMaladie3);

		dateDebutAnneeGlissante = new DateTime(demandeMaladie3.getDateFin()).minusYears(1).plusDays(1)
				.withMillisOfDay(0).toDate();
		
		Mockito.when(sirhRepository.getAgentCurrentCarriere(agentMatriculeService
						.fromIdAgentToSIRHNomatrAgent(idAgent), demandeMaladie3.getDateFin())).thenReturn(carr);

		Mockito.when(maladiesRepository.getListMaladiesAnneGlissanteRetroactiveByAgent(idAgent,
						dateDebutAnneeGlissante, demandeMaladie3.getDateFin(), demandeMaladie3.getIdDemande(), false, true)).thenReturn(listMaladies);
		
		result = service.calculDroitsMaladies(idAgent, demandeMaladie3.getDateFin(), 
				demandeMaladie3.getIdDemande(), demandeMaladie3.getDuree(), false, true);

		assertEquals(15, result.getDroitsPleinSalaire().intValue());
		assertEquals(10, result.getDroitsDemiSalaire().intValue());
		assertEquals(0, result.getNombreJoursResteAPrendrePleinSalaire().intValue());
		assertEquals(0, result.getNombreJoursResteAPrendreDemiSalaire().intValue());
		assertEquals(5, result.getNombreJoursCoupePleinSalaire().intValue());
		assertEquals(7, result.getNombreJoursCoupeDemiSalaire().intValue());
		assertEquals(30, result.getTotalPris().intValue());
	}

	// contractuels et conventions coll.
	@Test
	public void getSoldeAgent_calculMaladies3() {

		Integer idAgent = 9003309;

		AgentGeneriqueDto agentDto = new AgentGeneriqueDto();
		agentDto.setIdAgent(idAgent);
		agentDto.setNomatr(idAgent - 9000000);
		agentDto.setDateDerniereEmbauche(new DateTime(2013, 10, 1, 0, 0, 0).toDate());

		RefDroitsMaladies droitsMaladies = new RefDroitsMaladies();
		droitsMaladies.setAnneeAnciennete(10);
		droitsMaladies.setFonctionnaire(true);
		droitsMaladies.setNombreJoursPleinSalaire(15);
		droitsMaladies.setNombreJoursDemiSalaire(0);

		List<DemandeMaladies> listMaladies = new ArrayList<DemandeMaladies>();

		// ligne DE1
		DemandeMaladies demandeMaladie1 = new DemandeMaladies();
		demandeMaladie1.setType(type);
		demandeMaladie1.setDateDebut(new DateTime(2014, 2, 27, 0, 0, 0).toDate());
		demandeMaladie1.setDateFin(new DateTime(2014, 2, 28, 23, 59, 59).toDate());
		demandeMaladie1.setDuree(2.0);
		demandeMaladie1.setIdAgent(idAgent);
		demandeMaladie1.setNombreJoursCoupePleinSalaire(0);
		demandeMaladie1.setNombreJoursCoupeDemiSalaire(0);

		prepareTests(idAgent, demandeMaladie1.getDateFin(), droitsMaladies, listMaladies);

		listMaladies.add(demandeMaladie1);

		CalculDroitsMaladiesVo result = service.calculDroitsMaladies(idAgent,
				demandeMaladie1.getDateFin(), demandeMaladie1.getIdDemande(),
				demandeMaladie1.getDuree(), false, false);

		assertEquals(result.getDroitsPleinSalaire().intValue(), 15);
		assertEquals(result.getDroitsDemiSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursResteAPrendrePleinSalaire().intValue(), 13);
		assertEquals(result.getNombreJoursResteAPrendreDemiSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupePleinSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupeDemiSalaire().intValue(), 0);
		assertEquals(result.getTotalPris().intValue(), 2);

		// ligne DE2
		DemandeMaladies demandeMaladie2 = new DemandeMaladies();
		demandeMaladie2.setType(type);
		demandeMaladie2
				.setDateDebut(new DateTime(2014, 3, 1, 0, 0, 0).toDate());
		demandeMaladie2.setDateFin(new DateTime(2014, 3, 2, 23, 59, 59)
				.toDate());
		demandeMaladie2.setDuree(2.0);
		demandeMaladie2.setIdAgent(idAgent);
		demandeMaladie2.setNombreJoursCoupePleinSalaire(0);
		demandeMaladie2.setNombreJoursCoupeDemiSalaire(0);

		prepareTests(idAgent, demandeMaladie2.getDateFin(), droitsMaladies,
				listMaladies);

		listMaladies.add(demandeMaladie2);

		result = service.calculDroitsMaladies(idAgent,
				demandeMaladie2.getDateFin(), demandeMaladie2.getIdDemande(),
				demandeMaladie2.getDuree(), false, false);

		assertEquals(result.getDroitsPleinSalaire().intValue(), 15);
		assertEquals(result.getDroitsDemiSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursResteAPrendrePleinSalaire()
				.intValue(), 11);
		assertEquals(
				result.getNombreJoursResteAPrendreDemiSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupePleinSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupeDemiSalaire().intValue(), 0);
		assertEquals(result.getTotalPris().intValue(), 4);

		// ligne DE3
		DemandeMaladies demandeMaladie3 = new DemandeMaladies();
		demandeMaladie3.setType(type);
		demandeMaladie3
				.setDateDebut(new DateTime(2014, 4, 4, 0, 0, 0).toDate());
		demandeMaladie3.setDateFin(new DateTime(2014, 4, 11, 23, 59, 59)
				.toDate());
		demandeMaladie3.setDuree(8.0);
		demandeMaladie3.setIdAgent(idAgent);
		demandeMaladie3.setNombreJoursCoupePleinSalaire(0);
		demandeMaladie3.setNombreJoursCoupeDemiSalaire(0);

		prepareTests(idAgent, demandeMaladie3.getDateFin(), droitsMaladies,
				listMaladies);

		listMaladies.add(demandeMaladie3);

		result = service.calculDroitsMaladies(idAgent,
				demandeMaladie3.getDateFin(), demandeMaladie3.getIdDemande(),
				demandeMaladie3.getDuree(), false, false);

		assertEquals(result.getDroitsPleinSalaire().intValue(), 15);
		assertEquals(result.getDroitsDemiSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursResteAPrendrePleinSalaire()
				.intValue(), 3);
		assertEquals(
				result.getNombreJoursResteAPrendreDemiSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupePleinSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupeDemiSalaire().intValue(), 0);
		assertEquals(result.getTotalPris().intValue(), 12);

		// ligne DE4
		DemandeMaladies demandeMaladie4 = new DemandeMaladies();
		demandeMaladie4.setType(type);
		demandeMaladie4.setDateDebut(new DateTime(2014, 5, 10, 0, 0, 0).toDate());
		demandeMaladie4.setDateFin(new DateTime(2014, 5, 12, 23, 59, 59).toDate());
		demandeMaladie4.setDuree(3.0);
		demandeMaladie4.setIdAgent(idAgent);
		demandeMaladie4.setNombreJoursCoupePleinSalaire(0);
		demandeMaladie4.setNombreJoursCoupeDemiSalaire(0);

		prepareTests(idAgent, demandeMaladie4.getDateFin(), droitsMaladies, listMaladies);

		listMaladies.add(demandeMaladie4);

		result = service.calculDroitsMaladies(idAgent,
				demandeMaladie4.getDateFin(), demandeMaladie4.getIdDemande(),
				demandeMaladie4.getDuree(), false, false);

		assertEquals(result.getDroitsPleinSalaire().intValue(), 15);
		assertEquals(result.getDroitsDemiSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursResteAPrendrePleinSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursResteAPrendreDemiSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupePleinSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupeDemiSalaire().intValue(), 0);
		assertEquals(result.getTotalPris().intValue(), 15);

		// ligne DE5
		DemandeMaladies demandeMaladie5 = new DemandeMaladies();
		demandeMaladie5.setType(type);
		demandeMaladie5.setDateDebut(new DateTime(2014, 9, 9, 0, 0, 0).toDate());
		demandeMaladie5.setDateFin(new DateTime(2014, 9, 16, 23, 59, 59).toDate());
		demandeMaladie5.setDuree(8.0);
		demandeMaladie5.setIdAgent(idAgent);

		prepareTests(idAgent, demandeMaladie5.getDateFin(), droitsMaladies, listMaladies);

		listMaladies.add(demandeMaladie5);

		result = service.calculDroitsMaladies(idAgent,
				demandeMaladie5.getDateFin(), demandeMaladie5.getIdDemande(),
				demandeMaladie5.getDuree(), false, false);

		assertEquals(result.getDroitsPleinSalaire().intValue(), 15);
		assertEquals(result.getDroitsDemiSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursResteAPrendrePleinSalaire()
				.intValue(), 0);
		assertEquals(
				result.getNombreJoursResteAPrendreDemiSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupePleinSalaire().intValue(), 8);
		assertEquals(result.getNombreJoursCoupeDemiSalaire().intValue(), 0);
		assertEquals(result.getTotalPris().intValue(), 23);
	}

	// contractuels et conventions coll.
	@Test
	public void getSoldeAgent_periodeEssai_nonFonctionnaire() {

		Integer idAgent = 9003309;

		AgentGeneriqueDto agentDto = new AgentGeneriqueDto();
		agentDto.setIdAgent(idAgent);
		agentDto.setNomatr(idAgent - 9000000);
		agentDto.setDateDerniereEmbauche(new DateTime(2013, 10, 1, 0, 0, 0)
				.toDate());

		RefDroitsMaladies droitsMaladies = new RefDroitsMaladies();
		droitsMaladies.setAnneeAnciennete(10);
		droitsMaladies.setFonctionnaire(true);
		droitsMaladies.setNombreJoursPleinSalaire(15);
		droitsMaladies.setNombreJoursDemiSalaire(0);

		List<DemandeMaladies> listMaladies = new ArrayList<DemandeMaladies>();

		// ligne DE1
		DemandeMaladies demandeMaladie1 = new DemandeMaladies();
		demandeMaladie1.setType(type);
		demandeMaladie1.setDateDebut(new DateTime(2014, 2, 27, 0, 0, 0)
				.toDate());
		demandeMaladie1.setDateFin(new DateTime(2014, 2, 28, 23, 59, 59)
				.toDate());
		demandeMaladie1.setDuree(2.0);
		demandeMaladie1.setIdAgent(idAgent);
		demandeMaladie1.setNombreJoursCoupePleinSalaire(0);
		demandeMaladie1.setNombreJoursCoupeDemiSalaire(0);

		Date dateDebutAnneeGlissante = new DateTime(
				demandeMaladie1.getDateFin()).minusYears(1).plusDays(1)
				.withMillisOfDay(0).toDate();

		Integer noMatr = idAgent - 9000000;
		Mockito.when(
				agentMatriculeService.fromIdAgentToSIRHNomatrAgent(idAgent))
				.thenReturn(noMatr);
		Spcarr carr = new Spcarr();
		Mockito.when(
				sirhRepository.getAgentCurrentCarriere(agentMatriculeService
						.fromIdAgentToSIRHNomatrAgent(noMatr), demandeMaladie1
						.getDateFin())).thenReturn(carr);

		Mockito.when(
				maladiesRepository.getDroitsMaladies(Mockito.anyBoolean(),
						Mockito.anyBoolean(), Mockito.anyBoolean(),
						Mockito.anyInt())).thenReturn(droitsMaladies);

		Mockito.when(
				maladiesRepository.getListMaladiesAnneGlissanteByAgent(idAgent,
						dateDebutAnneeGlissante, demandeMaladie1.getDateFin()))
				.thenReturn(listMaladies);

		ReflectionTestUtils.setField(service, "maladiesRepository",
				maladiesRepository);
		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "agentMatriculeService",
				agentMatriculeService);
		ReflectionTestUtils.setField(service, "helperService", helperService);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		CalculDroitsMaladiesVo result = service.calculDroitsMaladies(idAgent,
				demandeMaladie1.getDateFin(), demandeMaladie1.getIdAgent(),
				demandeMaladie1.getDuree(), false, false);

		assertEquals(result.getDroitsPleinSalaire().intValue(), 0);
		assertEquals(result.getDroitsDemiSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursResteAPrendrePleinSalaire()
				.intValue(), 0);
		assertEquals(
				result.getNombreJoursResteAPrendreDemiSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupePleinSalaire().intValue(), 2);
		assertEquals(result.getNombreJoursCoupeDemiSalaire().intValue(), 0);
		assertEquals(result.getTotalPris().intValue(), 2);
	}

	// contractuels : cas pris en PROD dans SPABSEN : nomatr 4523
	@Test
	public void getSoldeAgent_calculMaladies_prod_4523() {

		Integer idAgent = 9004523;

		AgentGeneriqueDto agentDto = new AgentGeneriqueDto();
		agentDto.setIdAgent(idAgent);
		agentDto.setNomatr(idAgent - 9000000);
		agentDto.setDateDerniereEmbauche(new DateTime(2013, 10, 1, 0, 0, 0)
				.toDate());

		RefDroitsMaladies droitsMaladies = new RefDroitsMaladies();
		droitsMaladies.setAnneeAnciennete(10);
		droitsMaladies.setContractuel(true);
		droitsMaladies.setNombreJoursPleinSalaire(45);
		droitsMaladies.setNombreJoursDemiSalaire(75);

		List<DemandeMaladies> listMaladies = new ArrayList<DemandeMaladies>();

		// ligne DE0
		DemandeMaladies demandeMaladie0 = new DemandeMaladies();
		demandeMaladie0.setType(type);
		demandeMaladie0.setDateDebut(new DateTime(2015, 3, 16, 0, 0, 0)
				.toDate());
		demandeMaladie0.setDateFin(new DateTime(2015, 3, 18, 23, 59, 59)
				.toDate());
		demandeMaladie0.setDuree(3.0);
		demandeMaladie0.setIdAgent(idAgent);
		demandeMaladie0.setNombreJoursCoupeDemiSalaire(0);
		demandeMaladie0.setNombreJoursCoupePleinSalaire(0);
		listMaladies.add(demandeMaladie0);

		// ligne DE1
		DemandeMaladies demandeMaladie1 = new DemandeMaladies();
		demandeMaladie1.setType(type);
		demandeMaladie1
				.setDateDebut(new DateTime(2015, 4, 8, 0, 0, 0).toDate());
		demandeMaladie1.setDateFin(new DateTime(2015, 4, 9, 23, 59, 59)
				.toDate());
		demandeMaladie1.setDuree(2.0);
		demandeMaladie1.setIdAgent(idAgent);
		demandeMaladie1.setNombreJoursCoupeDemiSalaire(0);
		demandeMaladie1.setNombreJoursCoupePleinSalaire(0);
		listMaladies.add(demandeMaladie1);

		// ligne DE2
		DemandeMaladies demandeMaladie2 = new DemandeMaladies();
		demandeMaladie2.setType(type);
		demandeMaladie2.setDateDebut(new DateTime(2015, 5, 18, 0, 0, 0)
				.toDate());
		demandeMaladie2.setDateFin(new DateTime(2015, 5, 21, 23, 59, 59)
				.toDate());
		demandeMaladie2.setDuree(4.0);
		demandeMaladie2.setIdAgent(idAgent);
		demandeMaladie2.setNombreJoursCoupeDemiSalaire(0);
		demandeMaladie2.setNombreJoursCoupePleinSalaire(0);
		listMaladies.add(demandeMaladie2);

		// ligne DE3
		DemandeMaladies demandeMaladie3 = new DemandeMaladies();
		demandeMaladie3.setType(type);
		demandeMaladie3.setDateDebut(new DateTime(2015, 5, 28, 0, 0, 0)
				.toDate());
		demandeMaladie3.setDateFin(new DateTime(2015, 5, 29, 23, 59, 59)
				.toDate());
		demandeMaladie3.setDuree(2.0);
		demandeMaladie3.setIdAgent(idAgent);
		demandeMaladie3.setNombreJoursCoupeDemiSalaire(0);
		demandeMaladie3.setNombreJoursCoupePleinSalaire(0);
		listMaladies.add(demandeMaladie3);

		// ligne DE4
		DemandeMaladies demandeMaladie4 = new DemandeMaladies();
		demandeMaladie4.setType(type);
		demandeMaladie4
				.setDateDebut(new DateTime(2015, 7, 6, 0, 0, 0).toDate());
		demandeMaladie4.setDateFin(new DateTime(2015, 7, 10, 23, 59, 59)
				.toDate());
		demandeMaladie4.setDuree(5.0);
		demandeMaladie4.setIdAgent(idAgent);
		demandeMaladie4.setNombreJoursCoupeDemiSalaire(0);
		demandeMaladie4.setNombreJoursCoupePleinSalaire(0);
		listMaladies.add(demandeMaladie4);

		// ligne DE5
		DemandeMaladies demandeMaladie5 = new DemandeMaladies();
		demandeMaladie5.setType(type);
		demandeMaladie5.setDateDebut(new DateTime(2015, 7, 15, 0, 0, 0)
				.toDate());
		demandeMaladie5.setDateFin(new DateTime(2015, 7, 31, 23, 59, 59)
				.toDate());
		demandeMaladie5.setDuree(17.0);
		demandeMaladie5.setIdAgent(idAgent);
		demandeMaladie5.setNombreJoursCoupeDemiSalaire(12);
		demandeMaladie5.setNombreJoursCoupePleinSalaire(0);
		listMaladies.add(demandeMaladie5);

		// ligne DE6
		DemandeMaladies demandeMaladie6 = new DemandeMaladies();
		demandeMaladie6.setType(type);
		demandeMaladie6.setDateDebut(new DateTime(2015, 8, 17, 0, 0, 0)
				.toDate());
		demandeMaladie6.setDateFin(new DateTime(2015, 8, 24, 23, 59, 59)
				.toDate());
		demandeMaladie6.setDuree(8.0);
		demandeMaladie6.setIdAgent(idAgent);
		demandeMaladie6.setNombreJoursCoupeDemiSalaire(8);
		demandeMaladie6.setNombreJoursCoupePleinSalaire(0);
		listMaladies.add(demandeMaladie6);

		// ligne DE7
		DemandeMaladies demandeMaladie7 = new DemandeMaladies();
		demandeMaladie7.setType(type);
		demandeMaladie7.setDateDebut(new DateTime(2015, 8, 25, 0, 0, 0)
				.toDate());
		demandeMaladie7.setDateFin(new DateTime(2015, 8, 31, 23, 59, 59)
				.toDate());
		demandeMaladie7.setDuree(7.0);
		demandeMaladie7.setIdAgent(idAgent);
		demandeMaladie7.setNombreJoursCoupeDemiSalaire(7);
		demandeMaladie7.setNombreJoursCoupePleinSalaire(0);
		listMaladies.add(demandeMaladie7);

		// ligne DE8
		DemandeMaladies demandeMaladie8 = new DemandeMaladies();
		demandeMaladie8.setType(type);
		demandeMaladie8
				.setDateDebut(new DateTime(2015, 9, 1, 0, 0, 0).toDate());
		demandeMaladie8.setDateFin(new DateTime(2015, 9, 6, 23, 59, 59)
				.toDate());
		demandeMaladie8.setDuree(6.0);
		demandeMaladie8.setIdAgent(idAgent);
		demandeMaladie8.setNombreJoursCoupeDemiSalaire(6);
		demandeMaladie8.setNombreJoursCoupePleinSalaire(0);
		listMaladies.add(demandeMaladie8);

		// ligne DE9
		DemandeMaladies demandeMaladie9 = new DemandeMaladies();
		demandeMaladie9.setType(type);
		demandeMaladie9.setDateDebut(new DateTime(2015, 9, 11, 0, 0, 0)
				.toDate());
		demandeMaladie9.setDateFin(new DateTime(2015, 9, 18, 23, 59, 59)
				.toDate());
		demandeMaladie9.setDuree(8.0);
		demandeMaladie9.setIdAgent(idAgent);
		demandeMaladie9.setNombreJoursCoupeDemiSalaire(8);
		demandeMaladie9.setNombreJoursCoupePleinSalaire(0);
		listMaladies.add(demandeMaladie9);

		// ligne DE10
		DemandeMaladies demandeMaladie10 = new DemandeMaladies();
		demandeMaladie10.setType(type);
		demandeMaladie10.setDateDebut(new DateTime(2015, 9, 19, 0, 0, 0)
				.toDate());
		demandeMaladie10.setDateFin(new DateTime(2015, 9, 27, 23, 59, 59)
				.toDate());
		demandeMaladie10.setDuree(9.0);
		demandeMaladie10.setIdAgent(idAgent);
		demandeMaladie10.setNombreJoursCoupeDemiSalaire(9);
		demandeMaladie10.setNombreJoursCoupePleinSalaire(0);
		listMaladies.add(demandeMaladie10);

		// ligne DE11
		DemandeMaladies demandeMaladie11 = new DemandeMaladies();
		demandeMaladie11.setType(type);
		demandeMaladie11.setDateDebut(new DateTime(2015, 10, 22, 0, 0, 0)
				.toDate());
		demandeMaladie11.setDateFin(new DateTime(2015, 10, 31, 23, 59, 59)
				.toDate());
		demandeMaladie11.setDuree(10.0);
		demandeMaladie11.setIdAgent(idAgent);
		demandeMaladie11.setNombreJoursCoupeDemiSalaire(3);
		demandeMaladie11.setNombreJoursCoupePleinSalaire(0);
		listMaladies.add(demandeMaladie11);

		// ligne DE12
		DemandeMaladies demandeMaladie12 = new DemandeMaladies();
		demandeMaladie12.setType(type);
		demandeMaladie12.setDateDebut(new DateTime(2015, 11, 4, 0, 0, 0)
				.toDate());
		demandeMaladie12.setDateFin(new DateTime(2015, 11, 9, 23, 59, 59)
				.toDate());
		demandeMaladie12.setDuree(6.0);
		demandeMaladie12.setIdAgent(idAgent);
		demandeMaladie12.setNombreJoursCoupeDemiSalaire(6);
		demandeMaladie12.setNombreJoursCoupePleinSalaire(0);
		listMaladies.add(demandeMaladie12);

		// ligne DE13
		DemandeMaladies demandeMaladie13 = new DemandeMaladies();
		demandeMaladie13.setType(type);
		demandeMaladie13.setDateDebut(new DateTime(2015, 11, 10, 0, 0, 0)
				.toDate());
		demandeMaladie13.setDateFin(new DateTime(2015, 11, 16, 23, 59, 59)
				.toDate());
		demandeMaladie13.setDuree(7.0);
		demandeMaladie13.setIdAgent(idAgent);
		demandeMaladie13.setNombreJoursCoupeDemiSalaire(7);
		demandeMaladie13.setNombreJoursCoupePleinSalaire(0);
		listMaladies.add(demandeMaladie13);

		// ligne DE13
		DemandeMaladies demandeMaladie14 = new DemandeMaladies();
		demandeMaladie14.setType(type);
		demandeMaladie14.setDateDebut(new DateTime(2016, 3, 4, 0, 0, 0).toDate());
		demandeMaladie14.setDateFin(new DateTime(2016, 4, 4, 23, 59, 59).toDate());
		demandeMaladie14.setDuree(32.0);
		listMaladies.add(demandeMaladie14);
		
		Mockito.when(sirhWSConsumer.getAgent(idAgent)).thenReturn(agentDto);

		prepareTests(idAgent, demandeMaladie14.getDateFin(), droitsMaladies, listMaladies);
		
		CalculDroitsMaladiesVo result = service.calculDroitsMaladies(idAgent,
				demandeMaladie14.getDateFin(), demandeMaladie14.getIdDemande(),
				demandeMaladie14.getDuree(), false, false);

		assertEquals(result.getDroitsPleinSalaire().intValue(), 45);
		assertEquals(result.getDroitsDemiSalaire().intValue(), 75);
		assertEquals(result.getNombreJoursResteAPrendrePleinSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursResteAPrendreDemiSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupePleinSalaire().intValue(), 3);
		assertEquals(result.getNombreJoursCoupeDemiSalaire().intValue(), 29);
		assertEquals(result.getTotalPris().intValue(), 123);
	}

	// Conv coll : cas pris en PROD dans SPABSEN : nomatr 4080
	@Test
	public void getSoldeAgent_calculMaladies_prod_4080() {

		Integer idAgent = 9004080;

		AgentGeneriqueDto agentDto = new AgentGeneriqueDto();
		agentDto.setIdAgent(idAgent);
		agentDto.setNomatr(idAgent - 9000000);
		agentDto.setDateDerniereEmbauche(new DateTime(2002, 3, 27, 0, 0, 0)
				.toDate());

		RefDroitsMaladies droitsMaladies = new RefDroitsMaladies();
		droitsMaladies.setAnneeAnciennete(15);
		droitsMaladies.setConventionCollective(true);
		droitsMaladies.setNombreJoursPleinSalaire(60);
		droitsMaladies.setNombreJoursDemiSalaire(90);

		List<DemandeMaladies> listMaladies = new ArrayList<DemandeMaladies>();

		// ligne DE0
		DemandeMaladies demandeMaladie0 = new DemandeMaladies();
		demandeMaladie0.setType(type);
		demandeMaladie0
				.setDateDebut(new DateTime(2015, 3, 6, 0, 0, 0).toDate());
		demandeMaladie0.setDateFin(new DateTime(2015, 3, 7, 23, 59, 59)
				.toDate());
		demandeMaladie0.setDuree(2.0);
		demandeMaladie0.setIdAgent(idAgent);
		demandeMaladie0.setNombreJoursCoupeDemiSalaire(0);
		demandeMaladie0.setNombreJoursCoupePleinSalaire(0);
		listMaladies.add(demandeMaladie0);

		// ligne DE1
		DemandeMaladies demandeMaladie1 = new DemandeMaladies();
		demandeMaladie1.setType(type);
		demandeMaladie1
				.setDateDebut(new DateTime(2015, 3, 8, 0, 0, 0).toDate());
		demandeMaladie1.setDateFin(new DateTime(2015, 5, 20, 23, 59, 59)
				.toDate());
		demandeMaladie1.setDuree(74.0);
		demandeMaladie1.setIdAgent(idAgent);
		demandeMaladie1.setNombreJoursCoupeDemiSalaire(18);
		demandeMaladie1.setNombreJoursCoupePleinSalaire(0);
		listMaladies.add(demandeMaladie1);

		// ligne DE2
		DemandeMaladies demandeMaladie2 = new DemandeMaladies();
		demandeMaladie2.setType(type);
		demandeMaladie2.setDateDebut(new DateTime(2015, 5, 21, 0, 0, 0)
				.toDate());
		demandeMaladie2.setDateFin(new DateTime(2015, 7, 21, 23, 59, 59)
				.toDate());
		demandeMaladie2.setDuree(62.0);
		demandeMaladie2.setIdAgent(idAgent);
		demandeMaladie2.setNombreJoursCoupeDemiSalaire(60);
		demandeMaladie2.setNombreJoursCoupePleinSalaire(0);

		prepareTests(idAgent, demandeMaladie2.getDateFin(), droitsMaladies,
				listMaladies);
		
		listMaladies.add(demandeMaladie2);
		
		CalculDroitsMaladiesVo result = service.calculDroitsMaladies(idAgent,
				demandeMaladie2.getDateFin(), demandeMaladie2.getIdDemande(),
				demandeMaladie2.getDuree(), false, false);

		assertEquals(result.getDroitsPleinSalaire().intValue(), 60);
		assertEquals(result.getDroitsDemiSalaire().intValue(), 90);
		assertEquals(result.getNombreJoursResteAPrendrePleinSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursResteAPrendreDemiSalaire().intValue(), 12);
		assertEquals(result.getNombreJoursCoupePleinSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupeDemiSalaire().intValue(), 62);
		assertEquals(result.getTotalPris().intValue(), 138);

		// ligne DE3
		DemandeMaladies demandeMaladie3 = new DemandeMaladies();
		demandeMaladie3.setType(type);
		demandeMaladie3.setDateDebut(new DateTime(2015, 7, 22, 0, 0, 0)
				.toDate());
		demandeMaladie3.setDateFin(new DateTime(2015, 9, 15, 23, 59, 59)
				.toDate());
		demandeMaladie3.setDuree(56.0);
		demandeMaladie3.setIdAgent(idAgent);
		demandeMaladie3.setNombreJoursCoupeDemiSalaire(12);
		demandeMaladie3.setNombreJoursCoupePleinSalaire(44);

		prepareTests(idAgent, demandeMaladie3.getDateFin(), droitsMaladies,
				listMaladies);
		
		listMaladies.add(demandeMaladie3);
		
		result = service.calculDroitsMaladies(idAgent,
				demandeMaladie3.getDateFin(), demandeMaladie3.getIdDemande(),
				demandeMaladie3.getDuree(), false, false);

		assertEquals(result.getDroitsPleinSalaire().intValue(), 60);
		assertEquals(result.getDroitsDemiSalaire().intValue(), 90);
		assertEquals(result.getNombreJoursResteAPrendrePleinSalaire()
				.intValue(), 0);
		assertEquals(
				result.getNombreJoursResteAPrendreDemiSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupePleinSalaire().intValue(), 44);
		assertEquals(result.getNombreJoursCoupeDemiSalaire().intValue(), 12);
		assertEquals(result.getTotalPris().intValue(), 194);

		// ligne DE4
		DemandeMaladies demandeMaladie4 = new DemandeMaladies();
		demandeMaladie4.setType(type);
		demandeMaladie4.setDateDebut(new DateTime(2015, 9, 16, 0, 0, 0)
				.toDate());
		demandeMaladie4.setDateFin(new DateTime(2015, 11, 22, 23, 59, 59)
				.toDate());
		demandeMaladie4.setDuree(68.0);
		demandeMaladie4.setIdAgent(idAgent);
		demandeMaladie4.setNombreJoursCoupeDemiSalaire(0);
		demandeMaladie4.setNombreJoursCoupePleinSalaire(68);

		prepareTests(idAgent, demandeMaladie4.getDateFin(), droitsMaladies,
				listMaladies);
		
		listMaladies.add(demandeMaladie4);
		
		result = service.calculDroitsMaladies(idAgent,
				demandeMaladie4.getDateFin(), demandeMaladie4.getIdDemande(),
				demandeMaladie4.getDuree(), false, false);

		assertEquals(result.getDroitsPleinSalaire().intValue(), 60);
		assertEquals(result.getDroitsDemiSalaire().intValue(), 90);
		assertEquals(result.getNombreJoursResteAPrendrePleinSalaire()
				.intValue(), 0);
		assertEquals(
				result.getNombreJoursResteAPrendreDemiSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupePleinSalaire().intValue(), 68);
		assertEquals(result.getNombreJoursCoupeDemiSalaire().intValue(), 0);
		assertEquals(result.getTotalPris().intValue(), 262);

		// ligne DE5
		DemandeMaladies demandeMaladie5 = new DemandeMaladies();
		demandeMaladie5.setType(type);
		demandeMaladie5.setDateDebut(new DateTime(2015, 11, 23, 0, 0, 0)
				.toDate());
		demandeMaladie5.setDateFin(new DateTime(2016, 1, 31, 23, 59, 59)
				.toDate());
		demandeMaladie5.setDuree(70.0);
		demandeMaladie5.setIdAgent(idAgent);
		demandeMaladie5.setNombreJoursCoupeDemiSalaire(0);
		demandeMaladie5.setNombreJoursCoupePleinSalaire(70);

		prepareTests(idAgent, demandeMaladie5.getDateFin(), droitsMaladies,
				listMaladies);
		
		listMaladies.add(demandeMaladie5);
		
		result = service.calculDroitsMaladies(idAgent,
				demandeMaladie5.getDateFin(), demandeMaladie5.getIdDemande(),
				demandeMaladie5.getDuree(), false, false);

		assertEquals(result.getDroitsPleinSalaire().intValue(), 60);
		assertEquals(result.getDroitsDemiSalaire().intValue(), 90);
		assertEquals(result.getNombreJoursResteAPrendrePleinSalaire()
				.intValue(), 0);
		assertEquals(
				result.getNombreJoursResteAPrendreDemiSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupePleinSalaire().intValue(), 70);
		assertEquals(result.getNombreJoursCoupeDemiSalaire().intValue(), 0);
		assertEquals(result.getTotalPris().intValue(), 332);

		// ligne DE6
		DemandeMaladies demandeMaladie6 = new DemandeMaladies();
		demandeMaladie6.setType(type);
		demandeMaladie6.setDateDebut(new DateTime(2016, 2, 1, 0, 0, 0).toDate());
		demandeMaladie6.setDateFin(new DateTime(2016, 4, 30, 23, 59, 59).toDate());
		demandeMaladie6.setDuree(90.0);
		demandeMaladie6.setIdAgent(idAgent);

		prepareTests(idAgent, demandeMaladie6.getDateFin(), droitsMaladies,
				listMaladies);
		
		listMaladies.add(demandeMaladie6);
		
		result = service.calculDroitsMaladies(idAgent,
				demandeMaladie6.getDateFin(), demandeMaladie6.getIdDemande(),
				demandeMaladie6.getDuree(), false, false);

		assertEquals(result.getDroitsPleinSalaire().intValue(), 60);
		assertEquals(result.getDroitsDemiSalaire().intValue(), 90);
		assertEquals(result.getNombreJoursResteAPrendrePleinSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursResteAPrendreDemiSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupePleinSalaire().intValue(), 90);
		assertEquals(result.getNombreJoursCoupeDemiSalaire().intValue(), 0);
		assertEquals(result.getTotalPris().intValue(), 366);
	}

	// Contractuel : cas pris en PROD dans SPABSEN : nomatr 5699
	@Test
	public void getSoldeAgent_calculMaladies_prod_5699() {

		Integer idAgent = 9005699;

		AgentGeneriqueDto agentDto = new AgentGeneriqueDto();
		agentDto.setIdAgent(idAgent);
		agentDto.setNomatr(idAgent - 9000000);
		agentDto.setDateDerniereEmbauche(new DateTime(2015, 4, 1, 0, 0, 0)
				.toDate());

		RefDroitsMaladies droitsMaladies = new RefDroitsMaladies();
		droitsMaladies.setAnneeAnciennete(1);
		droitsMaladies.setContractuel(true);
		droitsMaladies.setNombreJoursPleinSalaire(15);
		droitsMaladies.setNombreJoursDemiSalaire(0);

		List<DemandeMaladies> listMaladies = new ArrayList<DemandeMaladies>();

		// ligne DE0
		DemandeMaladies demandeMaladie0 = new DemandeMaladies();
		demandeMaladie0.setType(type);
		demandeMaladie0.setDateDebut(new DateTime(2015, 5, 22, 0, 0, 0)
				.toDate());
		demandeMaladie0.setDateFin(new DateTime(2015, 5, 22, 23, 59, 59)
				.toDate());
		demandeMaladie0.setDuree(1.0);
		demandeMaladie0.setIdAgent(idAgent);
		demandeMaladie0.setNombreJoursCoupeDemiSalaire(0);
		demandeMaladie0.setNombreJoursCoupePleinSalaire(0);

		prepareTests(idAgent, demandeMaladie0.getDateFin(), droitsMaladies,
				listMaladies);
		listMaladies.add(demandeMaladie0);
		CalculDroitsMaladiesVo result = service.calculDroitsMaladies(idAgent,
				demandeMaladie0.getDateFin(), demandeMaladie0.getIdDemande(),
				demandeMaladie0.getDuree(), false, false);

		assertEquals(result.getDroitsPleinSalaire().intValue(), 15);
		assertEquals(result.getDroitsDemiSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursResteAPrendrePleinSalaire()
				.intValue(), 14);
		assertEquals(
				result.getNombreJoursResteAPrendreDemiSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupePleinSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupeDemiSalaire().intValue(), 0);
		assertEquals(result.getTotalPris().intValue(), 1);

		// ligne DE1
		DemandeMaladies demandeMaladie1 = new DemandeMaladies();
		demandeMaladie1.setType(type);
		demandeMaladie1.setDateDebut(new DateTime(2015, 7, 21, 0, 0, 0)
				.toDate());
		demandeMaladie1.setDateFin(new DateTime(2015, 7, 22, 23, 59, 59)
				.toDate());
		demandeMaladie1.setDuree(2.0);
		demandeMaladie1.setIdAgent(idAgent);
		demandeMaladie1.setNombreJoursCoupeDemiSalaire(0);
		demandeMaladie1.setNombreJoursCoupePleinSalaire(0);

		prepareTests(idAgent, demandeMaladie1.getDateFin(), droitsMaladies,
				listMaladies);
		
		listMaladies.add(demandeMaladie1);
		
		result = service.calculDroitsMaladies(idAgent,
				demandeMaladie1.getDateFin(), demandeMaladie1.getIdDemande(),
				demandeMaladie1.getDuree(), false, false);

		assertEquals(result.getDroitsPleinSalaire().intValue(), 15);
		assertEquals(result.getDroitsDemiSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursResteAPrendrePleinSalaire()
				.intValue(), 12);
		assertEquals(
				result.getNombreJoursResteAPrendreDemiSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupePleinSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupeDemiSalaire().intValue(), 0);
		assertEquals(result.getTotalPris().intValue(), 3);

		// ligne DE2
		DemandeMaladies demandeMaladie2 = new DemandeMaladies();
		demandeMaladie2.setType(type);
		demandeMaladie2.setDateDebut(new DateTime(2015, 7, 23, 0, 0, 0)
				.toDate());
		demandeMaladie2.setDateFin(new DateTime(2015, 7, 23, 23, 59, 59)
				.toDate());
		demandeMaladie2.setDuree(1.0);
		demandeMaladie2.setIdAgent(idAgent);
		demandeMaladie2.setNombreJoursCoupeDemiSalaire(0);
		demandeMaladie2.setNombreJoursCoupePleinSalaire(0);

		prepareTests(idAgent, demandeMaladie2.getDateFin(), droitsMaladies,
				listMaladies);
		
		listMaladies.add(demandeMaladie2);
		
		result = service.calculDroitsMaladies(idAgent,
				demandeMaladie2.getDateFin(), demandeMaladie2.getIdDemande(),
				demandeMaladie2.getDuree(), false, false);

		assertEquals(result.getDroitsPleinSalaire().intValue(), 15);
		assertEquals(result.getDroitsDemiSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursResteAPrendrePleinSalaire()
				.intValue(), 11);
		assertEquals(
				result.getNombreJoursResteAPrendreDemiSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupePleinSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupeDemiSalaire().intValue(), 0);
		assertEquals(result.getTotalPris().intValue(), 4);

		// ligne DE3
		DemandeMaladies demandeMaladie3 = new DemandeMaladies();
		demandeMaladie3.setType(type);
		demandeMaladie3.setDateDebut(new DateTime(2015, 9, 15, 0, 0, 0)
				.toDate());
		demandeMaladie3.setDateFin(new DateTime(2015, 9, 25, 23, 59, 59)
				.toDate());
		demandeMaladie3.setDuree(11.0);
		demandeMaladie3.setIdAgent(idAgent);
		demandeMaladie3.setNombreJoursCoupeDemiSalaire(0);
		demandeMaladie3.setNombreJoursCoupePleinSalaire(0);

		prepareTests(idAgent, demandeMaladie3.getDateFin(), droitsMaladies,
				listMaladies);
		
		listMaladies.add(demandeMaladie3);
		
		result = service.calculDroitsMaladies(idAgent,
				demandeMaladie3.getDateFin(), demandeMaladie3.getIdDemande(),
				demandeMaladie3.getDuree(), false, false);

		assertEquals(result.getDroitsPleinSalaire().intValue(), 15);
		assertEquals(result.getDroitsDemiSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursResteAPrendrePleinSalaire()
				.intValue(), 0);
		assertEquals(
				result.getNombreJoursResteAPrendreDemiSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupePleinSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupeDemiSalaire().intValue(), 0);
		assertEquals(result.getTotalPris().intValue(), 15);

		// ligne DE4
		DemandeMaladies demandeMaladie4 = new DemandeMaladies();
		demandeMaladie4.setType(type);
		demandeMaladie4.setDateDebut(new DateTime(2016, 2, 26, 0, 0, 0)
				.toDate());
		demandeMaladie4.setDateFin(new DateTime(2016, 2, 26, 23, 59, 59)
				.toDate());
		demandeMaladie4.setDuree(1.0);
		demandeMaladie4.setIdAgent(idAgent);

		prepareTests(idAgent, demandeMaladie4.getDateFin(), droitsMaladies,
				listMaladies);
		
		listMaladies.add(demandeMaladie4);
		
		result = service.calculDroitsMaladies(idAgent,
				demandeMaladie4.getDateFin(), demandeMaladie4.getIdDemande(),
				demandeMaladie4.getDuree(), false, false);

		assertEquals(result.getDroitsPleinSalaire().intValue(), 15);
		assertEquals(result.getDroitsDemiSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursResteAPrendrePleinSalaire()
				.intValue(), 0);
		assertEquals(
				result.getNombreJoursResteAPrendreDemiSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupePleinSalaire().intValue(), 1);
		assertEquals(result.getNombreJoursCoupeDemiSalaire().intValue(), 0);
		assertEquals(result.getTotalPris().intValue(), 16);
	}

	// Fonctionnaire : cas pris en PROD dans SPABSEN : nomatr 5325
	@Test
	public void getSoldeAgent_calculMaladies_prod_5325() {

		Integer idAgent = 9005325;

		AgentGeneriqueDto agentDto = new AgentGeneriqueDto();
		agentDto.setIdAgent(idAgent);
		agentDto.setNomatr(idAgent - 9000000);
		agentDto.setDateDerniereEmbauche(new DateTime(2012, 5, 2, 0, 0, 0)
				.toDate());

		RefDroitsMaladies droitsMaladies = new RefDroitsMaladies();
		droitsMaladies.setAnneeAnciennete(0);
		droitsMaladies.setFonctionnaire(true);
		droitsMaladies.setNombreJoursPleinSalaire(90);
		droitsMaladies.setNombreJoursDemiSalaire(270);

		List<DemandeMaladies> listMaladies = new ArrayList<DemandeMaladies>();

		// ligne DE0
		DemandeMaladies demandeMaladie0 = new DemandeMaladies();
		demandeMaladie0.setType(type);
		demandeMaladie0.setDateDebut(new DateTime(2015, 6, 22, 0, 0, 0)
				.toDate());
		demandeMaladie0.setDateFin(new DateTime(2015, 7, 21, 23, 59, 59)
				.toDate());
		demandeMaladie0.setDuree(30.0);
		demandeMaladie0.setIdAgent(idAgent);
		demandeMaladie0.setNombreJoursCoupeDemiSalaire(0);
		demandeMaladie0.setNombreJoursCoupePleinSalaire(0);

		prepareTests(idAgent, demandeMaladie0.getDateFin(), droitsMaladies,
				listMaladies);
		listMaladies.add(demandeMaladie0);
		CalculDroitsMaladiesVo result = service.calculDroitsMaladies(idAgent,
				demandeMaladie0.getDateFin(), demandeMaladie0.getIdDemande(),
				demandeMaladie0.getDuree(), false, false);

		assertEquals(result.getDroitsPleinSalaire().intValue(), 90);
		assertEquals(result.getDroitsDemiSalaire().intValue(), 270);
		assertEquals(result.getNombreJoursResteAPrendrePleinSalaire()
				.intValue(), 60);
		assertEquals(
				result.getNombreJoursResteAPrendreDemiSalaire().intValue(), 270);
		assertEquals(result.getNombreJoursCoupePleinSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupeDemiSalaire().intValue(), 0);
		assertEquals(result.getTotalPris().intValue(), 30);

		// ligne DE1
		DemandeMaladies demandeMaladie1 = new DemandeMaladies();
		demandeMaladie1.setType(type);
		demandeMaladie1.setDateDebut(new DateTime(2015, 7, 22, 0, 0, 0)
				.toDate());
		demandeMaladie1.setDateFin(new DateTime(2015, 8, 4, 23, 59, 59)
				.toDate());
		demandeMaladie1.setDuree(14.0);
		demandeMaladie1.setIdAgent(idAgent);
		demandeMaladie1.setNombreJoursCoupeDemiSalaire(0);
		demandeMaladie1.setNombreJoursCoupePleinSalaire(0);

		prepareTests(idAgent, demandeMaladie1.getDateFin(), droitsMaladies,
				listMaladies);
		
		listMaladies.add(demandeMaladie1);
		
		result = service.calculDroitsMaladies(idAgent,
				demandeMaladie1.getDateFin(), demandeMaladie1.getIdDemande(),
				demandeMaladie1.getDuree(), false, false);

		assertEquals(result.getDroitsPleinSalaire().intValue(), 90);
		assertEquals(result.getDroitsDemiSalaire().intValue(), 270);
		assertEquals(result.getNombreJoursResteAPrendrePleinSalaire()
				.intValue(), 46);
		assertEquals(
				result.getNombreJoursResteAPrendreDemiSalaire().intValue(), 270);
		assertEquals(result.getNombreJoursCoupePleinSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupeDemiSalaire().intValue(), 0);
		assertEquals(result.getTotalPris().intValue(), 44);

		// ligne DE2
		DemandeMaladies demandeMaladie2 = new DemandeMaladies();
		demandeMaladie2.setType(type);
		demandeMaladie2
				.setDateDebut(new DateTime(2015, 8, 5, 0, 0, 0).toDate());
		demandeMaladie2.setDateFin(new DateTime(2015, 8, 11, 23, 59, 59)
				.toDate());
		demandeMaladie2.setDuree(7.0);
		demandeMaladie2.setIdAgent(idAgent);
		demandeMaladie2.setNombreJoursCoupeDemiSalaire(0);
		demandeMaladie2.setNombreJoursCoupePleinSalaire(0);

		prepareTests(idAgent, demandeMaladie2.getDateFin(), droitsMaladies,
				listMaladies);
		
		listMaladies.add(demandeMaladie2);
		
		result = service.calculDroitsMaladies(idAgent,
				demandeMaladie2.getDateFin(), demandeMaladie2.getIdDemande(),
				demandeMaladie2.getDuree(), false, false);

		assertEquals(result.getDroitsPleinSalaire().intValue(), 90);
		assertEquals(result.getDroitsDemiSalaire().intValue(), 270);
		assertEquals(result.getNombreJoursResteAPrendrePleinSalaire()
				.intValue(), 39);
		assertEquals(
				result.getNombreJoursResteAPrendreDemiSalaire().intValue(), 270);
		assertEquals(result.getNombreJoursCoupePleinSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupeDemiSalaire().intValue(), 0);
		assertEquals(result.getTotalPris().intValue(), 51);

		// ligne DE3
		DemandeMaladies demandeMaladie3 = new DemandeMaladies();
		demandeMaladie3.setType(type);
		demandeMaladie3
				.setDateDebut(new DateTime(2015, 9, 7, 0, 0, 0).toDate());
		demandeMaladie3.setDateFin(new DateTime(2015, 10, 7, 23, 59, 59)
				.toDate());
		demandeMaladie3.setDuree(31.0);
		demandeMaladie3.setIdAgent(idAgent);
		demandeMaladie3.setNombreJoursCoupeDemiSalaire(0);
		demandeMaladie3.setNombreJoursCoupePleinSalaire(0);

		prepareTests(idAgent, demandeMaladie3.getDateFin(), droitsMaladies,
				listMaladies);
		
		listMaladies.add(demandeMaladie3);
		
		result = service.calculDroitsMaladies(idAgent,
				demandeMaladie3.getDateFin(), demandeMaladie3.getIdDemande(),
				demandeMaladie3.getDuree(), false, false);

		assertEquals(result.getDroitsPleinSalaire().intValue(), 90);
		assertEquals(result.getDroitsDemiSalaire().intValue(), 270);
		assertEquals(result.getNombreJoursResteAPrendrePleinSalaire()
				.intValue(), 8);
		assertEquals(
				result.getNombreJoursResteAPrendreDemiSalaire().intValue(), 270);
		assertEquals(result.getNombreJoursCoupePleinSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupeDemiSalaire().intValue(), 0);
		assertEquals(result.getTotalPris().intValue(), 82);

		// ligne DE4
		DemandeMaladies demandeMaladie4 = new DemandeMaladies();
		demandeMaladie4.setType(type);
		demandeMaladie4.setDateDebut(new DateTime(2015, 10, 8, 0, 0, 0)
				.toDate());
		demandeMaladie4.setDateFin(new DateTime(2015, 10, 18, 23, 59, 59)
				.toDate());
		demandeMaladie4.setDuree(11.0);
		demandeMaladie4.setIdAgent(idAgent);
		demandeMaladie4.setNombreJoursCoupeDemiSalaire(3);
		demandeMaladie4.setNombreJoursCoupePleinSalaire(0);

		prepareTests(idAgent, demandeMaladie4.getDateFin(), droitsMaladies,
				listMaladies);
		
		listMaladies.add(demandeMaladie4);
		
		result = service.calculDroitsMaladies(idAgent,
				demandeMaladie4.getDateFin(), demandeMaladie4.getIdDemande(),
				demandeMaladie4.getDuree(), false, false);

		assertEquals(result.getDroitsPleinSalaire().intValue(), 90);
		assertEquals(result.getDroitsDemiSalaire().intValue(), 270);
		assertEquals(result.getNombreJoursResteAPrendrePleinSalaire()
				.intValue(), 0);
		assertEquals(
				result.getNombreJoursResteAPrendreDemiSalaire().intValue(), 267);
		assertEquals(result.getNombreJoursCoupePleinSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupeDemiSalaire().intValue(), 3);
		assertEquals(result.getTotalPris().intValue(), 93);

		// ligne DE5
		DemandeMaladies demandeMaladie5 = new DemandeMaladies();
		demandeMaladie5.setType(type);
		demandeMaladie5.setDateDebut(new DateTime(2015, 10, 19, 0, 0, 0)
				.toDate());
		demandeMaladie5.setDateFin(new DateTime(2015, 10, 31, 23, 59, 59)
				.toDate());
		demandeMaladie5.setDuree(13.0);
		demandeMaladie5.setIdAgent(idAgent);
		demandeMaladie5.setNombreJoursCoupeDemiSalaire(13);
		demandeMaladie5.setNombreJoursCoupePleinSalaire(0);

		prepareTests(idAgent, demandeMaladie5.getDateFin(), droitsMaladies,
				listMaladies);
		
		listMaladies.add(demandeMaladie5);
		
		result = service.calculDroitsMaladies(idAgent,
				demandeMaladie5.getDateFin(), demandeMaladie5.getIdDemande(),
				demandeMaladie5.getDuree(), false, false);

		assertEquals(result.getDroitsPleinSalaire().intValue(), 90);
		assertEquals(result.getDroitsDemiSalaire().intValue(), 270);
		assertEquals(result.getNombreJoursResteAPrendrePleinSalaire()
				.intValue(), 0);
		assertEquals(
				result.getNombreJoursResteAPrendreDemiSalaire().intValue(), 254);
		assertEquals(result.getNombreJoursCoupePleinSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupeDemiSalaire().intValue(), 13);
		assertEquals(result.getTotalPris().intValue(), 106);

		// ligne DE6
		DemandeMaladies demandeMaladie6 = new DemandeMaladies();
		demandeMaladie6.setType(type);
		demandeMaladie6.setDateDebut(new DateTime(2015, 11, 1, 0, 0, 0)
				.toDate());
		demandeMaladie6.setDateFin(new DateTime(2015, 11, 30, 23, 59, 59)
				.toDate());
		demandeMaladie6.setDuree(30.0);
		demandeMaladie6.setIdAgent(idAgent);
		demandeMaladie6.setNombreJoursCoupeDemiSalaire(30);
		demandeMaladie6.setNombreJoursCoupePleinSalaire(0);

		prepareTests(idAgent, demandeMaladie6.getDateFin(), droitsMaladies,
				listMaladies);
		
		listMaladies.add(demandeMaladie6);
		
		result = service.calculDroitsMaladies(idAgent,
				demandeMaladie6.getDateFin(), demandeMaladie6.getIdDemande(),
				demandeMaladie6.getDuree(), false, false);

		assertEquals(result.getDroitsPleinSalaire().intValue(), 90);
		assertEquals(result.getDroitsDemiSalaire().intValue(), 270);
		assertEquals(result.getNombreJoursResteAPrendrePleinSalaire()
				.intValue(), 0);
		assertEquals(
				result.getNombreJoursResteAPrendreDemiSalaire().intValue(), 224);
		assertEquals(result.getNombreJoursCoupePleinSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupeDemiSalaire().intValue(), 30);
		assertEquals(result.getTotalPris().intValue(), 136);

		// ligne DE7
		DemandeMaladies demandeMaladie7 = new DemandeMaladies();
		demandeMaladie7.setType(type);
		demandeMaladie7
				.setDateDebut(new DateTime(2016, 1, 4, 0, 0, 0).toDate());
		demandeMaladie7.setDateFin(new DateTime(2016, 1, 19, 23, 59, 59)
				.toDate());
		demandeMaladie7.setDuree(16.0);
		demandeMaladie7.setIdAgent(idAgent);
		demandeMaladie7.setNombreJoursCoupeDemiSalaire(16);
		demandeMaladie7.setNombreJoursCoupePleinSalaire(0);

		prepareTests(idAgent, demandeMaladie7.getDateFin(), droitsMaladies,
				listMaladies);
		
		listMaladies.add(demandeMaladie7);
		
		result = service.calculDroitsMaladies(idAgent,
				demandeMaladie7.getDateFin(), demandeMaladie7.getIdDemande(),
				demandeMaladie7.getDuree(), false, false);

		assertEquals(result.getDroitsPleinSalaire().intValue(), 90);
		assertEquals(result.getDroitsDemiSalaire().intValue(), 270);
		assertEquals(result.getNombreJoursResteAPrendrePleinSalaire()
				.intValue(), 0);
		assertEquals(
				result.getNombreJoursResteAPrendreDemiSalaire().intValue(), 208);
		assertEquals(result.getNombreJoursCoupePleinSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupeDemiSalaire().intValue(), 16);
		assertEquals(result.getTotalPris().intValue(), 152);

		// ligne DE8
		DemandeMaladies demandeMaladie8 = new DemandeMaladies();
		demandeMaladie8.setType(type);
		demandeMaladie8.setDateDebut(new DateTime(2016, 1, 20, 0, 0, 0)
				.toDate());
		demandeMaladie8.setDateFin(new DateTime(2016, 2, 15, 23, 59, 59)
				.toDate());
		demandeMaladie8.setDuree(27.0);
		demandeMaladie8.setIdAgent(idAgent);
		demandeMaladie8.setNombreJoursCoupeDemiSalaire(27);
		demandeMaladie8.setNombreJoursCoupePleinSalaire(0);

		prepareTests(idAgent, demandeMaladie8.getDateFin(), droitsMaladies,
				listMaladies);
		
		listMaladies.add(demandeMaladie8);
		
		result = service.calculDroitsMaladies(idAgent,
				demandeMaladie8.getDateFin(), demandeMaladie8.getIdDemande(),
				demandeMaladie8.getDuree(), false, false);

		assertEquals(result.getDroitsPleinSalaire().intValue(), 90);
		assertEquals(result.getDroitsDemiSalaire().intValue(), 270);
		assertEquals(result.getNombreJoursResteAPrendrePleinSalaire()
				.intValue(), 0);
		assertEquals(
				result.getNombreJoursResteAPrendreDemiSalaire().intValue(), 181);
		assertEquals(result.getNombreJoursCoupePleinSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupeDemiSalaire().intValue(), 27);
		assertEquals(result.getTotalPris().intValue(), 179);

		// ligne DE9
		DemandeMaladies demandeMaladie9 = new DemandeMaladies();
		demandeMaladie9.setType(type);
		demandeMaladie9.setDateDebut(new DateTime(2016, 2, 19, 0, 0, 0)
				.toDate());
		demandeMaladie9.setDateFin(new DateTime(2016, 2, 24, 23, 59, 59)
				.toDate());
		demandeMaladie9.setDuree(6.0);
		demandeMaladie9.setIdAgent(idAgent);

		prepareTests(idAgent, demandeMaladie9.getDateFin(), droitsMaladies,
				listMaladies);
		
		listMaladies.add(demandeMaladie9);
		
		result = service.calculDroitsMaladies(idAgent,
				demandeMaladie9.getDateFin(), demandeMaladie9.getIdDemande(),
				demandeMaladie9.getDuree(), false, false);

		assertEquals(result.getDroitsPleinSalaire().intValue(), 90);
		assertEquals(result.getDroitsDemiSalaire().intValue(), 270);
		assertEquals(result.getNombreJoursResteAPrendrePleinSalaire()
				.intValue(), 0);
		assertEquals(
				result.getNombreJoursResteAPrendreDemiSalaire().intValue(), 175);
		assertEquals(result.getNombreJoursCoupePleinSalaire().intValue(), 0);
		assertEquals(result.getNombreJoursCoupeDemiSalaire().intValue(), 6);
		assertEquals(result.getTotalPris().intValue(), 185);
	}

	@Test
	public void getNombreAnneeAnciennete_noPA50_2ans8mois() {

		Integer idAgent = 9003309;

		AgentGeneriqueDto agentDto = new AgentGeneriqueDto();
		agentDto.setIdAgent(idAgent);
		agentDto.setNomatr(idAgent - 9000000);
		agentDto.setDateDerniereEmbauche(new DateTime(2013, 10, 1, 0, 0, 0)
				.toDate());

		Date dateFinMaladies = new DateTime(2016, 6, 1, 0, 0, 0).toDate();

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);

		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);

		Integer result = service.getNombreAnneeAnciennete(idAgent,
				dateFinMaladies, agentDto);

		assertEquals(result.intValue(), 2);
	}

	@Test
	public void getNombreAnneeAnciennete_PA50_1ans8mois() {

		Integer idAgent = 9003309;

		AgentGeneriqueDto agentDto = new AgentGeneriqueDto();
		agentDto.setIdAgent(idAgent);
		agentDto.setNomatr(idAgent - 9000000);
		agentDto.setDateDerniereEmbauche(new DateTime(2013, 10, 1, 0, 0, 0)
				.toDate());

		Date dateFinMaladies = new DateTime(2016, 6, 1, 0, 0, 0).toDate();

		SpadmnId id2 = new SpadmnId();
		id2.setDatdeb(20140101);
		id2.setNomatr(5138);
		Spadmn pa = new Spadmn();
		pa.setId(id2);
		pa.setCdpadm("50");
		pa.setDatfin(20150101);

		List<Spadmn> listPA50 = new ArrayList<Spadmn>();
		listPA50.add(pa);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(
				sirhRepository.getPA50OfAgent(agentDto.getNomatr(),
						agentDto.getDateDerniereEmbauche())).thenReturn(
				listPA50);

		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);

		Integer result = service.getNombreAnneeAnciennete(idAgent,
				dateFinMaladies, agentDto);

		assertEquals(result.intValue(), 1);
	}

	@Test
	public void getNombreAnneeAnciennete_2_PA50_4ans8mois() {

		Integer idAgent = 9003309;

		AgentGeneriqueDto agentDto = new AgentGeneriqueDto();
		agentDto.setIdAgent(idAgent);
		agentDto.setNomatr(idAgent - 9000000);
		agentDto.setDateDerniereEmbauche(new DateTime(2010, 1, 1, 0, 0, 0)
				.toDate());

		Date dateFinMaladies = new DateTime(2015, 12, 31, 0, 0, 0).toDate();

		SpadmnId id = new SpadmnId();
		id.setDatdeb(20140101);
		id.setNomatr(5138);
		Spadmn pa = new Spadmn();
		pa.setId(id);
		pa.setCdpadm("50");
		pa.setDatfin(20140630);

		SpadmnId id2 = new SpadmnId();
		id2.setDatdeb(20110101);
		id2.setNomatr(5138);
		Spadmn pa2 = new Spadmn();
		pa2.setId(id2);
		pa2.setCdpadm("50");
		pa2.setDatfin(20111031);

		List<Spadmn> listPA50 = new ArrayList<Spadmn>();
		listPA50.add(pa);
		listPA50.add(pa2);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(
				sirhRepository.getPA50OfAgent(agentDto.getNomatr(),
						agentDto.getDateDerniereEmbauche())).thenReturn(
				listPA50);

		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);

		Integer result = service.getNombreAnneeAnciennete(idAgent,
				dateFinMaladies, agentDto);

		assertEquals(result.intValue(), 4);
	}

	@Test
	public void getNombeJourMaladies() {

		Integer idAgent = 9005138;
		Date dateDebutAnneeGlissante = new DateTime(2014, 1, 18, 0, 0, 0)
				.toDate();
		Date dateFinAnneeGlissante = new DateTime(2015, 1, 17, 0, 0, 0)
				.toDate();

		DemandeMaladies demande1 = new DemandeMaladies();
		demande1.setDateDebut(new DateTime(2014, 1, 10, 0, 0, 0).toDate());
		demande1.setDateFin(new DateTime(2014, 1, 20, 23, 59, 59).toDate());
		demande1.setDuree(10.0);
		demande1.setNombreJoursCoupeDemiSalaire(0);
		demande1.setNombreJoursCoupePleinSalaire(1);

		DemandeMaladies demande2 = new DemandeMaladies();
		demande2.setDateDebut(new DateTime(2014, 6, 7, 0, 0, 0).toDate());
		demande2.setDateFin(new DateTime(2014, 6, 22, 23, 59, 59).toDate());
		demande2.setDuree(15.0);
		demande2.setNombreJoursCoupeDemiSalaire(2);
		demande2.setNombreJoursCoupePleinSalaire(3);

		DemandeMaladies demande3 = new DemandeMaladies();
		demande3.setDateDebut(new DateTime(2014, 7, 7, 0, 0, 0).toDate());
		demande3.setDateFin(new DateTime(2014, 7, 21, 23, 59, 59).toDate());
		demande3.setDuree(14.0);
		demande3.setNombreJoursCoupeDemiSalaire(4);
		demande3.setNombreJoursCoupePleinSalaire(5);

		List<DemandeMaladies> listMaladies = new ArrayList<DemandeMaladies>();
		listMaladies.add(demande1);
		listMaladies.add(demande2);
		listMaladies.add(demande3);

		Integer result = service.getNombeJourMaladies(idAgent,
				dateDebutAnneeGlissante, dateFinAnneeGlissante, listMaladies, null);

		assertEquals(result.intValue(), 32);
	}

	@Test
	public void getNombeJourMaladies_maladiesAnterieuresPeriode() {

		Integer idAgent = 9005138;
		Date dateDebutAnneeGlissante = new DateTime(2014, 1, 18, 0, 0, 0)
				.toDate();
		Date dateFinAnneeGlissante = new DateTime(2015, 1, 17, 0, 0, 0)
				.toDate();

		DemandeMaladies demande1 = new DemandeMaladies();
		demande1.setDateDebut(new DateTime(2014, 1, 10, 0, 0, 0).toDate());
		demande1.setDateFin(new DateTime(2014, 1, 16, 23, 59, 59).toDate());
		demande1.setDuree(10.0);
		demande1.setNombreJoursCoupeDemiSalaire(0);
		demande1.setNombreJoursCoupePleinSalaire(1);

		List<DemandeMaladies> listMaladies = new ArrayList<DemandeMaladies>();
		listMaladies.add(demande1);

		Integer result = service.getNombeJourMaladies(idAgent,
				dateDebutAnneeGlissante, dateFinAnneeGlissante, listMaladies, null);

		assertEquals(result.intValue(), 0);
	}

	@Test
	public void majCompteurRecupToAgent_debitOk() {

		Integer idAgent = 9005325;

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());

		ReturnMessageDto result = new ReturnMessageDto();
		DemandeMaladies demande = new DemandeMaladies();
		demande.setIdAgent(9005325);
		demande.setDuree(-11.0);

		RefDroitsMaladies droitsMaladies = new RefDroitsMaladies();
		droitsMaladies.setAnneeAnciennete(0);
		droitsMaladies.setFonctionnaire(true);
		droitsMaladies.setNombreJoursPleinSalaire(90);
		droitsMaladies.setNombreJoursDemiSalaire(90);

		List<DemandeMaladies> listMaladies = new ArrayList<DemandeMaladies>();

		AgentGeneriqueDto agentDto = new AgentGeneriqueDto();
		agentDto.setIdAgent(idAgent);
		agentDto.setNomatr(idAgent - 9000000);
		agentDto.setDateDerniereEmbauche(new DateTime(2013, 10, 1, 0, 0, 0)
				.toDate());

		SpadmnId id2 = new SpadmnId();
		id2.setDatdeb(20140101);
		id2.setNomatr(5138);
		Spadmn pa = new Spadmn();
		pa.setId(id2);
		pa.setCdpadm("50");
		pa.setDatfin(20150101);

		List<Spadmn> listPA50 = new ArrayList<Spadmn>();
		listPA50.add(pa);

		ISirhRepository sirhRepository = Mockito.mock(ISirhRepository.class);
		Mockito.when(
				sirhRepository.getPA50OfAgent(agentDto.getNomatr(),
						agentDto.getDateDerniereEmbauche())).thenReturn(
				listPA50);
		ISirhWSConsumer sirhWSConsumer = Mockito.mock(ISirhWSConsumer.class);
		Mockito.when(sirhWSConsumer.getAgent(idAgent)).thenReturn(agentDto);

		prepareTests(idAgent, demande.getDateFin(), droitsMaladies,
				listMaladies);

		ReflectionTestUtils.setField(service, "sirhRepository", sirhRepository);
		ReflectionTestUtils.setField(service, "sirhWSConsumer", sirhWSConsumer);

		result = service.majCompteurToAgent(result, demande,
				demandeEtatChangeDto);

		assertEquals(0, result.getErrors().size());
	}

}
