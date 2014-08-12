package nc.noumea.mairie.abs.service.counter.impl;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.RefGroupeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeGroupeAbsenceEnum;
import nc.noumea.mairie.abs.domain.RefTypeSaisi;
import nc.noumea.mairie.abs.dto.SoldeSpecifiqueDto;
import nc.noumea.mairie.abs.repository.ICongesExceptionnelsRepository;
import nc.noumea.mairie.abs.repository.IDemandeRepository;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class CongesExcepCounterServiceImplTest extends AbstractCounterServiceTest {

	@Test
	public void testMethodeParenteHeritage() {
		super.allTest(new CongesExcepCounterServiceImpl());
	}
	
	@Test
	public void getListAgentCounterByDate() {
		
		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		
		RefTypeAbsence typeAbs1 = new RefTypeAbsence();
		typeAbs1.setIdRefTypeAbsence(1);
		typeAbs1.setLabel("label 1");
		typeAbs1.setTypeSaisi(typeSaisi);
		
		RefTypeAbsence typeAbs2 = new RefTypeAbsence();
		typeAbs2.setIdRefTypeAbsence(2);
		typeAbs2.setLabel("label 2");
		typeAbs2.setTypeSaisi(typeSaisi);
		
		RefTypeAbsence typeAbsNoCounter = new RefTypeAbsence();
		typeAbsNoCounter.setIdRefTypeAbsence(3);
		typeAbsNoCounter.setLabel("label 3");
		
		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setListeTypeAbsence(Arrays.asList(typeAbs1, typeAbs2, typeAbsNoCounter));
		
		Integer idAgent = 9005138;
		Date dateDebut = new Date(); 
		Date dateFin = new Date();
		
		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getEntity(RefGroupeAbsence.class, RefTypeGroupeAbsenceEnum.CONGES_EXCEP.getValue())).thenReturn(groupe);

		ICongesExceptionnelsRepository congesExceptionnelsRepository = Mockito.mock(ICongesExceptionnelsRepository.class);
		Mockito.when(congesExceptionnelsRepository.countDureeByPeriodeAndTypeDemande(idAgent, dateDebut, dateFin, typeAbs1.getIdRefTypeAbsence())).thenReturn(10.0);
		Mockito.when(congesExceptionnelsRepository.countDureeByPeriodeAndTypeDemande(idAgent, dateDebut, dateFin, typeAbs2.getIdRefTypeAbsence())).thenReturn(20.0);
		Mockito.when(congesExceptionnelsRepository.countDureeByPeriodeAndTypeDemande(idAgent, dateDebut, dateFin, typeAbsNoCounter.getIdRefTypeAbsence())).thenReturn(0.0);
		
		CongesExcepCounterServiceImpl impl = new CongesExcepCounterServiceImpl();
		ReflectionTestUtils.setField(impl, "demandeRepository", demandeRepository);
		ReflectionTestUtils.setField(impl, "congesExceptionnelsRepository", congesExceptionnelsRepository);
		
		List<SoldeSpecifiqueDto> result = impl.getListAgentCounterByDate(idAgent, dateDebut, dateFin);
		
		assertEquals(2, result.size());
		assertEquals(result.get(0).getLibelle(), "label 1");
		assertEquals(result.get(0).getSolde().doubleValue(), 10,0);
		assertEquals(result.get(1).getLibelle(), "label 2");
		assertEquals(result.get(1).getSolde().doubleValue(), 20,0);
	}
}
