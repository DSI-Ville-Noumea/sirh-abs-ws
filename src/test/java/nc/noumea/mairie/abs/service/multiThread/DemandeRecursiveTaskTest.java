package nc.noumea.mairie.abs.service.multiThread;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nc.noumea.mairie.abs.domain.DroitsAgent;
import nc.noumea.mairie.abs.dto.AgentWithServiceDto;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.RefGroupeAbsenceDto;
import nc.noumea.mairie.abs.service.rules.impl.DataConsistencyRulesFactory;
import nc.noumea.mairie.abs.service.rules.impl.DefaultAbsenceDataConsistencyRulesImpl;
import nc.noumea.mairie.abs.vo.CheckCompteurAgentVo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SpringContext.class, DemandeRecursiveTask.class})
public class DemandeRecursiveTaskTest {

	private DemandeRecursiveTask construct(List<DemandeDto> pListDemandeDto) {
		HashMap<Integer, CheckCompteurAgentVo> pMapCheckCompteurAgentVo = new HashMap<Integer, CheckCompteurAgentVo>();
		Integer pIdAgentConnecte = 9005151;
		List<DroitsAgent> pListDroitsAgent = new ArrayList<DroitsAgent>();
		boolean pIsAgent = true;

		ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
		PowerMockito.mockStatic(SpringContext.class);
		PowerMockito.when(SpringContext.getApplicationContext()).thenReturn(applicationContext);

		return new DemandeRecursiveTask(pMapCheckCompteurAgentVo, pListDemandeDto, pIdAgentConnecte, pListDroitsAgent, pIsAgent);
	}
	
	@Test
	public void DemandeRecursiveTask_constructor() {
		assertNotNull(construct(new ArrayList<DemandeDto>()));
	}

	@Test
	public void parallelCheckDemandeDto_ERROR_ATTRIBUT() {

		HashMap<Integer, CheckCompteurAgentVo> pMapCheckCompteurAgentVo = new HashMap<Integer, CheckCompteurAgentVo>();
		List<DemandeDto> pListDemandeDto = new ArrayList<DemandeDto>();
		Integer pIdAgentConnecte = 9005151;
		List<DroitsAgent> pListDroitsAgent = new ArrayList<DroitsAgent>();
		boolean pIsAgent = true;

		ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
		PowerMockito.mockStatic(SpringContext.class);
		PowerMockito.when(SpringContext.getApplicationContext()).thenReturn(applicationContext);

		DemandeRecursiveTask service = new DemandeRecursiveTask(pMapCheckCompteurAgentVo, null, pIdAgentConnecte, pListDroitsAgent, pIsAgent);
		assertNull(service.parallelCheckDemandeDto());

		service = new DemandeRecursiveTask(pMapCheckCompteurAgentVo, pListDemandeDto, null, pListDroitsAgent, pIsAgent);
		assertNull(service.parallelCheckDemandeDto());

		service = new DemandeRecursiveTask(pMapCheckCompteurAgentVo, pListDemandeDto, pIdAgentConnecte, null, pIsAgent);
		assertNull(service.parallelCheckDemandeDto());
	}

	@Test
	public void parallelCheckDemandeDto() throws Exception {
		
		// on prepare 220 demandes
		List<DemandeDto> listDemande = new ArrayList<DemandeDto>();
		
		for(int i=0; i<220; i++) {
			AgentWithServiceDto agentWithServiceDto = new AgentWithServiceDto();
			
			RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
			
			DemandeDto demandeDto = new DemandeDto();
			demandeDto.setGroupeAbsence(groupeAbsence);
			demandeDto.setAgentWithServiceDto(agentWithServiceDto);
			
			listDemande.add(demandeDto);
		}
		
		
		/////////// PARTIE CHECK DEMANDE ////////////
		DefaultAbsenceDataConsistencyRulesImpl defaultAbsenceDataConsistencyRulesImpl = PowerMockito.mock(DefaultAbsenceDataConsistencyRulesImpl.class);
		
		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(Mockito.anyInt(), Mockito.anyInt())).thenReturn(defaultAbsenceDataConsistencyRulesImpl);

		PowerMockito.when(defaultAbsenceDataConsistencyRulesImpl.filtreDroitOfDemande(Mockito.anyInt(), Mockito.any(DemandeDto.class), Mockito.anyListOf(DroitsAgent.class), Mockito.anyBoolean()))
			.thenReturn(new DemandeDto());
		PowerMockito.when(defaultAbsenceDataConsistencyRulesImpl.checkDepassementCompteurAgent(Mockito.any(DemandeDto.class), Mockito.any(CheckCompteurAgentVo.class)))
			.thenReturn(true);
		PowerMockito.when(defaultAbsenceDataConsistencyRulesImpl.checkDepassementMultipleAgent(Mockito.any(DemandeDto.class)))
			.thenReturn(true);
		/////////// PARTIE CHECK DEMANDE ////////////
		
		DemandeRecursiveTask service = construct(listDemande);
		
		DemandeRecursiveTask task = PowerMockito.mock(DemandeRecursiveTask.class);
		PowerMockito.whenNew(DemandeRecursiveTask.class).withArguments(Mockito.anyMap(), Mockito.anyList(), Mockito.anyInt(), Mockito.anyList(), Mockito.anyBoolean())
			.thenReturn(task);
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				
				return null;
			}
		}).when(task).fork();
		
		Mockito.doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				
				return new ArrayList<DemandeDto>();
			}
		}).when(task).join();
		
		service.parallelCheckDemandeDto();
		
		Mockito.verify(task, Mockito.times(4)).fork();
		Mockito.verify(task, Mockito.times(4)).join();
	}

	@Test
	public void checkDemande() {

		AgentWithServiceDto agentWithServiceDto = new AgentWithServiceDto();
		
		RefGroupeAbsenceDto groupeAbsence = new RefGroupeAbsenceDto();
		
		DemandeDto demandeDto = new DemandeDto();
		demandeDto.setGroupeAbsence(groupeAbsence);
		demandeDto.setAgentWithServiceDto(agentWithServiceDto);

		List<DemandeDto> listDemandeDto = new ArrayList<DemandeDto>();
		listDemandeDto.add(demandeDto);

		DefaultAbsenceDataConsistencyRulesImpl defaultAbsenceDataConsistencyRulesImpl = PowerMockito.mock(DefaultAbsenceDataConsistencyRulesImpl.class);
		
		DataConsistencyRulesFactory dataConsistencyRulesFactory = Mockito.mock(DataConsistencyRulesFactory.class);
		Mockito.when(dataConsistencyRulesFactory.getFactory(demandeDto.getGroupeAbsence()
					.getIdRefGroupeAbsence(), demandeDto.getIdTypeDemande())).thenReturn(defaultAbsenceDataConsistencyRulesImpl);

		PowerMockito.when(defaultAbsenceDataConsistencyRulesImpl.filtreDroitOfDemande(Mockito.anyInt(), Mockito.any(DemandeDto.class), Mockito.anyListOf(DroitsAgent.class), Mockito.anyBoolean()))
			.thenReturn(demandeDto);
		PowerMockito.when(defaultAbsenceDataConsistencyRulesImpl.checkDepassementCompteurAgent(Mockito.any(DemandeDto.class), Mockito.any(CheckCompteurAgentVo.class)))
			.thenReturn(true);
		PowerMockito.when(defaultAbsenceDataConsistencyRulesImpl.checkDepassementMultipleAgent(Mockito.any(DemandeDto.class)))
			.thenReturn(true);
		
		DemandeRecursiveTask service = construct(new ArrayList<DemandeDto>());
		ReflectionTestUtils.setField(service, "dataConsistencyRulesFactory", dataConsistencyRulesFactory);
		
		List<DemandeDto> result = service.checkDemande(listDemandeDto);
		
		assertEquals(1, result.size());
		assertTrue(result.get(0).isDepassementCompteur());
		assertTrue(result.get(0).isDepassementMultiple());
	}
}
