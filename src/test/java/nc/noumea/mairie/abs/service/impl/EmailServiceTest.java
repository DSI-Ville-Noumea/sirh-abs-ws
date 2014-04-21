package nc.noumea.mairie.abs.service.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.dto.EmailInfoDto;
import nc.noumea.mairie.abs.repository.IDemandeRepository;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class EmailServiceTest {

	@Test
	public void getListIdDestinatairesEmailInfo() {

		List<Integer> listViseurs = new ArrayList<Integer>();
		listViseurs.add(1);
		List<Integer> listApprobateurs = new ArrayList<Integer>();
		listApprobateurs.add(2);

		List<Integer> listeTypes = new ArrayList<Integer>();
		listeTypes.add(RefTypeAbsenceEnum.RECUP.getValue());
		listeTypes.add(RefTypeAbsenceEnum.REPOS_COMP.getValue());
		listeTypes.add(RefTypeAbsenceEnum.ASA_A48.getValue());
		listeTypes.add(RefTypeAbsenceEnum.ASA_A54.getValue());

		IDemandeRepository demandeRepository = Mockito.mock(IDemandeRepository.class);
		Mockito.when(demandeRepository.getListViseursDemandesSaisiesJourDonne(listeTypes)).thenReturn(listViseurs);
		Mockito.when(demandeRepository.getListApprobateursDemandesSaisiesViseesJourDonne(listeTypes)).thenReturn(
				listApprobateurs);

		EmailService service = new EmailService();
		ReflectionTestUtils.setField(service, "demandeRepository", demandeRepository);

		EmailInfoDto dto = service.getListIdDestinatairesEmailInfo();

		assertEquals(1, dto.getListApprobateurs().size());
		assertEquals(2, dto.getListApprobateurs().get(0).intValue());
		assertEquals(1, dto.getListViseurs().size());
		assertEquals(1, dto.getListViseurs().get(0).intValue());
	}
}
