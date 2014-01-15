package nc.noumea.mairie.abs.service.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import nc.noumea.mairie.abs.domain.MotifCompteur;
import nc.noumea.mairie.abs.domain.MotifRefus;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.dto.MotifCompteurDto;
import nc.noumea.mairie.abs.dto.MotifRefusDto;
import nc.noumea.mairie.abs.repository.IMotifRepository;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class MotifServiceTest {

	@Test
	public void getListeMotifRefus_returnListe() {

		Integer idRefType = 1;
		RefTypeAbsence typeRecup = new RefTypeAbsence();
		typeRecup.setLabel("RECUP");

		List<MotifRefus> listMotif = new ArrayList<MotifRefus>();
		MotifRefus refus1 = new MotifRefus();
		refus1.setLibelle("motif refus recup");
		refus1.setRefTypeAbsence(typeRecup);
		listMotif.add(refus1);

		IMotifRepository motifRepository = Mockito.mock(IMotifRepository.class);
		Mockito.when(motifRepository.getListeMotifRefus(idRefType)).thenReturn(listMotif);

		MotifService service = new MotifService();
		ReflectionTestUtils.setField(service, "motifRepository", motifRepository);

		List<MotifRefusDto> listResult = service.getListeMotifRefus(idRefType);

		assertEquals(1, listResult.size());
		assertEquals("motif refus recup", listResult.get(0).getLibelle());
	}

	@Test
	public void getListeMotifRefus_wrongType() {

		Integer idRefType = 1;

		IMotifRepository motifRepository = Mockito.mock(IMotifRepository.class);
		Mockito.when(motifRepository.getListeMotifRefus(idRefType)).thenReturn(null);

		MotifService service = new MotifService();
		ReflectionTestUtils.setField(service, "motifRepository", motifRepository);

		List<MotifRefusDto> listResult = service.getListeMotifRefus(idRefType);

		assertEquals(0, listResult.size());
	}

	@Test
	public void getListeMotifCompteur_returnListe() {

		Integer idRefType = 1;
		RefTypeAbsence typeRecup = new RefTypeAbsence();
		typeRecup.setLabel("RECUP");

		List<MotifCompteur> listMotif = new ArrayList<MotifCompteur>();
		MotifCompteur refus1 = new MotifCompteur();
		refus1.setLibelle("motif compteur recup");
		refus1.setRefTypeAbsence(typeRecup);
		listMotif.add(refus1);

		IMotifRepository motifRepository = Mockito.mock(IMotifRepository.class);
		Mockito.when(motifRepository.getListeMotifCompteur(idRefType)).thenReturn(listMotif);

		MotifService service = new MotifService();
		ReflectionTestUtils.setField(service, "motifRepository", motifRepository);

		List<MotifCompteurDto> listResult = service.getListeMotifCompteur(idRefType);

		assertEquals(1, listResult.size());
		assertEquals("motif compteur recup", listResult.get(0).getLibelle());
	}

	@Test
	public void getListeMotifCompteur_wrongType() {

		Integer idRefType = 1;

		IMotifRepository motifRepository = Mockito.mock(IMotifRepository.class);
		Mockito.when(motifRepository.getListeMotifCompteur(idRefType)).thenReturn(null);

		MotifService service = new MotifService();
		ReflectionTestUtils.setField(service, "motifRepository", motifRepository);

		List<MotifCompteurDto> listResult = service.getListeMotifCompteur(idRefType);

		assertEquals(0, listResult.size());
	}
}
