package nc.noumea.mairie.abs.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import nc.noumea.mairie.abs.domain.MotifCompteur;
import nc.noumea.mairie.abs.domain.MotifRefus;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.dto.MotifCompteurDto;
import nc.noumea.mairie.abs.dto.MotifRefusDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;
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
	
	@Test
	public void getRefTypeAbsence_idRefTypNull() {
		
		Integer idRefTypeAbs = null;
		ReturnMessageDto message = new ReturnMessageDto();
		RefTypeAbsence result = null;
		
		MotifService service = new MotifService();
		result = service.getRefTypeAbsence(idRefTypeAbs, message);
		
		assertEquals(1, message.getErrors().size());
		assertEquals("Le type d'absence saisi n'existe pas.", message.getErrors().get(0).toString());
		assertNull(result);
	}
	
	@Test
	public void getRefTypeAbsence_RefTyp_inexistant() {
		
		Integer idRefTypeAbs = 1;
		ReturnMessageDto message = new ReturnMessageDto();
		RefTypeAbsence result = null;
		
		IMotifRepository motifRepository = Mockito.mock(IMotifRepository.class);
			Mockito.when(motifRepository.getEntity(RefTypeAbsence.class, idRefTypeAbs)).thenReturn(null);
		
		MotifService service = new MotifService();
		ReflectionTestUtils.setField(service, "motifRepository", motifRepository);
		
		result = service.getRefTypeAbsence(idRefTypeAbs, message);
		
		assertEquals(1, message.getErrors().size());
		assertEquals("Le type d'absence saisi n'existe pas.", message.getErrors().get(0).toString());
		assertNull(result);
	}
	
	@Test
	public void getRefTypeAbsence_OK() {
		
		Integer idRefTypeAbs = 1;
		ReturnMessageDto message = new ReturnMessageDto();
		RefTypeAbsence result = null;
		
		IMotifRepository motifRepository = Mockito.mock(IMotifRepository.class);
			Mockito.when(motifRepository.getEntity(RefTypeAbsence.class, idRefTypeAbs)).thenReturn(new RefTypeAbsence());
		
		MotifService service = new MotifService();
		ReflectionTestUtils.setField(service, "motifRepository", motifRepository);
		
		result = service.getRefTypeAbsence(idRefTypeAbs, message);
		
		assertEquals(0, message.getErrors().size());
		assertNotNull(result);
	}
	
	@Test
	public void controlLibelleMotif_libelleNull() {
		
		String libelle = null;
		ReturnMessageDto message = new ReturnMessageDto();
		
		MotifService service = new MotifService();
		boolean result = service.controlLibelleMotif(libelle, message);

		assertFalse(result);
	}
	
	@Test
	public void controlLibelleMotif_libelleVide() {
		
		String libelle = "";
		ReturnMessageDto message = new ReturnMessageDto();
		
		MotifService service = new MotifService();
		boolean result = service.controlLibelleMotif(libelle, message);

		assertFalse(result);
	}
	
	@Test
	public void controlLibelleMotif_OK() {
		
		String libelle = "OK";
		ReturnMessageDto message = new ReturnMessageDto();
		
		MotifService service = new MotifService();
		boolean result = service.controlLibelleMotif(libelle, message);

		assertTrue(result);
	}
	
	@Test
	public void setMotifRefus_motifInexistant() {
		
		MotifRefusDto motifRefusDto = new MotifRefusDto();
			motifRefusDto.setIdMotifRefus(1);
		
		ReturnMessageDto message = new ReturnMessageDto();
		
		IMotifRepository motifRepository = Mockito.mock(IMotifRepository.class);
			Mockito.when(motifRepository.getEntity(MotifRefus.class, motifRefusDto.getIdMotifRefus())).thenReturn(null);
		
		MotifService service = new MotifService();
			ReflectionTestUtils.setField(service, "motifRepository", motifRepository);
		
		message = service.setMotifRefus(motifRefusDto);
		
		assertEquals(1, message.getErrors().size());
		Mockito.verify(motifRepository, Mockito.times(0)).persistEntity(Mockito.isA(MotifRefus.class));
		assertEquals("Le motif à modifier n'existe pas.", message.getErrors().get(0).toString());
	}
	
	@Test
	public void setMotifRefus_typeAbsenceInexistant() {
		
		MotifRefusDto motifRefusDto = new MotifRefusDto();
			motifRefusDto.setIdMotifRefus(1);
		
		ReturnMessageDto message = new ReturnMessageDto();
		
		IMotifRepository motifRepository = Mockito.mock(IMotifRepository.class);
			Mockito.when(motifRepository.getEntity(MotifRefus.class, motifRefusDto.getIdMotifRefus())).thenReturn(new MotifRefus());
		
		MotifService service = new MotifService();
			ReflectionTestUtils.setField(service, "motifRepository", motifRepository);
		
		message = service.setMotifRefus(motifRefusDto);
		
		assertEquals(1, message.getErrors().size());
		Mockito.verify(motifRepository, Mockito.times(0)).persistEntity(Mockito.isA(MotifRefus.class));
		assertEquals("Le type d'absence saisi n'existe pas.", message.getErrors().get(0).toString());
	}
	
	@Test
	public void setMotifRefus_libelleVide() {
		
		MotifRefusDto motifRefusDto = new MotifRefusDto();
			motifRefusDto.setIdMotifRefus(1);
			motifRefusDto.setIdRefTypeAbsence(1);
		
		ReturnMessageDto message = new ReturnMessageDto();
		
		IMotifRepository motifRepository = Mockito.mock(IMotifRepository.class);
			Mockito.when(motifRepository.getEntity(MotifRefus.class, motifRefusDto.getIdMotifRefus())).thenReturn(new MotifRefus());
			Mockito.when(motifRepository.getEntity(RefTypeAbsence.class, motifRefusDto.getIdRefTypeAbsence())).thenReturn(new RefTypeAbsence());
		
		MotifService service = new MotifService();
			ReflectionTestUtils.setField(service, "motifRepository", motifRepository);
		
		message = service.setMotifRefus(motifRefusDto);
		
		assertEquals(1, message.getErrors().size());
		Mockito.verify(motifRepository, Mockito.times(0)).persistEntity(Mockito.isA(MotifRefus.class));
		assertEquals("Le libellé du motif n'est pas saisi.", message.getErrors().get(0).toString());
	}
	
	@Test
	public void setMotifRefus_modifOK() {
		
		MotifRefusDto motifRefusDto = new MotifRefusDto();
			motifRefusDto.setIdMotifRefus(1);
			motifRefusDto.setIdRefTypeAbsence(1);
			motifRefusDto.setLibelle("TEST");
		
		ReturnMessageDto message = new ReturnMessageDto();
		
		IMotifRepository motifRepository = Mockito.mock(IMotifRepository.class);
			Mockito.when(motifRepository.getEntity(MotifRefus.class, motifRefusDto.getIdMotifRefus())).thenReturn(new MotifRefus());
			Mockito.when(motifRepository.getEntity(RefTypeAbsence.class, motifRefusDto.getIdRefTypeAbsence())).thenReturn(new RefTypeAbsence());
		
		MotifService service = new MotifService();
			ReflectionTestUtils.setField(service, "motifRepository", motifRepository);
		
		message = service.setMotifRefus(motifRefusDto);
		
		assertEquals(0, message.getErrors().size());
		Mockito.verify(motifRepository, Mockito.times(1)).persistEntity(Mockito.isA(MotifRefus.class));
		assertEquals("Le motif est bien modifié.", message.getInfos().get(0).toString());
	}
	
	@Test
	public void setMotifRefus_creationOK() {
		
		MotifRefusDto motifRefusDto = new MotifRefusDto();
			motifRefusDto.setIdMotifRefus(null);
			motifRefusDto.setIdRefTypeAbsence(1);
			motifRefusDto.setLibelle("TEST");
		
		ReturnMessageDto message = new ReturnMessageDto();
		
		IMotifRepository motifRepository = Mockito.mock(IMotifRepository.class);
			Mockito.when(motifRepository.getEntity(MotifRefus.class, motifRefusDto.getIdMotifRefus())).thenReturn(new MotifRefus());
			Mockito.when(motifRepository.getEntity(RefTypeAbsence.class, motifRefusDto.getIdRefTypeAbsence())).thenReturn(new RefTypeAbsence());
		
		MotifService service = new MotifService();
			ReflectionTestUtils.setField(service, "motifRepository", motifRepository);
		
		message = service.setMotifRefus(motifRefusDto);
		
		assertEquals(0, message.getErrors().size());
		Mockito.verify(motifRepository, Mockito.times(1)).persistEntity(Mockito.isA(MotifRefus.class));
		assertEquals("Le motif est bien créé.", message.getInfos().get(0).toString());
	}
	
	@Test
	public void setMotifCompteur_motifInexistant() {
		
		MotifCompteurDto motifCompteurDto = new MotifCompteurDto();
			motifCompteurDto.setIdMotifCompteur(1);
		
		ReturnMessageDto message = new ReturnMessageDto();
		
		IMotifRepository motifRepository = Mockito.mock(IMotifRepository.class);
			Mockito.when(motifRepository.getEntity(MotifCompteur.class, motifCompteurDto.getIdMotifCompteur())).thenReturn(null);
		
		MotifService service = new MotifService();
			ReflectionTestUtils.setField(service, "motifRepository", motifRepository);
		
		message = service.setMotifCompteur(motifCompteurDto);
		
		assertEquals(1, message.getErrors().size());
		Mockito.verify(motifRepository, Mockito.times(0)).persistEntity(Mockito.isA(MotifCompteur.class));
		assertEquals("Le motif à modifier n'existe pas.", message.getErrors().get(0).toString());
	}
	
	@Test
	public void setMotifCompteur_typeAbsenceInexistant() {
		
		MotifCompteurDto motifCompteurDto = new MotifCompteurDto();
			motifCompteurDto.setIdMotifCompteur(1);
		
		ReturnMessageDto message = new ReturnMessageDto();
		
		IMotifRepository motifRepository = Mockito.mock(IMotifRepository.class);
			Mockito.when(motifRepository.getEntity(MotifCompteur.class, motifCompteurDto.getIdMotifCompteur())).thenReturn(new MotifCompteur());
		
		MotifService service = new MotifService();
			ReflectionTestUtils.setField(service, "motifRepository", motifRepository);
		
		message = service.setMotifCompteur(motifCompteurDto);
		
		assertEquals(1, message.getErrors().size());
		Mockito.verify(motifRepository, Mockito.times(0)).persistEntity(Mockito.isA(MotifCompteur.class));
		assertEquals("Le type d'absence saisi n'existe pas.", message.getErrors().get(0).toString());
	}
	
	@Test
	public void setMotifCompteur_libelleVide() {
		
		MotifCompteurDto motifCompteurDto = new MotifCompteurDto();
			motifCompteurDto.setIdMotifCompteur(1);
			motifCompteurDto.setIdRefTypeAbsence(1);
		
		ReturnMessageDto message = new ReturnMessageDto();
		
		IMotifRepository motifRepository = Mockito.mock(IMotifRepository.class);
			Mockito.when(motifRepository.getEntity(MotifCompteur.class, motifCompteurDto.getIdMotifCompteur())).thenReturn(new MotifCompteur());
			Mockito.when(motifRepository.getEntity(RefTypeAbsence.class, motifCompteurDto.getIdRefTypeAbsence())).thenReturn(new RefTypeAbsence());
		
		MotifService service = new MotifService();
			ReflectionTestUtils.setField(service, "motifRepository", motifRepository);
		
		message = service.setMotifCompteur(motifCompteurDto);
		
		assertEquals(1, message.getErrors().size());
		Mockito.verify(motifRepository, Mockito.times(0)).persistEntity(Mockito.isA(MotifCompteur.class));
		assertEquals("Le libellé du motif n'est pas saisi.", message.getErrors().get(0).toString());
	}
	
	@Test
	public void setMotifCompteur_modifOK() {
		
		MotifCompteurDto motifCompteurDto = new MotifCompteurDto();
			motifCompteurDto.setIdMotifCompteur(1);
			motifCompteurDto.setIdRefTypeAbsence(1);
			motifCompteurDto.setLibelle("TEST");
		
		ReturnMessageDto message = new ReturnMessageDto();
		
		IMotifRepository motifRepository = Mockito.mock(IMotifRepository.class);
			Mockito.when(motifRepository.getEntity(MotifCompteur.class, motifCompteurDto.getIdMotifCompteur())).thenReturn(new MotifCompteur());
			Mockito.when(motifRepository.getEntity(RefTypeAbsence.class, motifCompteurDto.getIdRefTypeAbsence())).thenReturn(new RefTypeAbsence());
		
		MotifService service = new MotifService();
			ReflectionTestUtils.setField(service, "motifRepository", motifRepository);
		
		message = service.setMotifCompteur(motifCompteurDto);
		
		assertEquals(0, message.getErrors().size());
		Mockito.verify(motifRepository, Mockito.times(1)).persistEntity(Mockito.isA(MotifCompteur.class));
		assertEquals("Le motif est bien modifié.", message.getInfos().get(0).toString());
	}
	
	@Test
	public void setMotifCompteur_creationOK() {
		
		MotifCompteurDto motifCompteurDto = new MotifCompteurDto();
			motifCompteurDto.setIdMotifCompteur(null);
			motifCompteurDto.setIdRefTypeAbsence(1);
			motifCompteurDto.setLibelle("TEST");
		
		ReturnMessageDto message = new ReturnMessageDto();
		
		IMotifRepository motifRepository = Mockito.mock(IMotifRepository.class);
			Mockito.when(motifRepository.getEntity(MotifCompteur.class, motifCompteurDto.getIdMotifCompteur())).thenReturn(new MotifCompteur());
			Mockito.when(motifRepository.getEntity(RefTypeAbsence.class, motifCompteurDto.getIdRefTypeAbsence())).thenReturn(new RefTypeAbsence());
		
		MotifService service = new MotifService();
			ReflectionTestUtils.setField(service, "motifRepository", motifRepository);
		
		message = service.setMotifCompteur(motifCompteurDto);
		
		assertEquals(0, message.getErrors().size());
		Mockito.verify(motifRepository, Mockito.times(1)).persistEntity(Mockito.isA(MotifCompteur.class));
		assertEquals("Le motif est bien créé.", message.getInfos().get(0).toString());
	}
}
