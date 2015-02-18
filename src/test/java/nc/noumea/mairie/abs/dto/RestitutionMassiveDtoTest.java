package nc.noumea.mairie.abs.dto;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import nc.noumea.mairie.abs.domain.CongeAnnuelRestitutionMassive;

import org.junit.Test;

public class RestitutionMassiveDtoTest {

	@Test
	public void ctor_RestitutionMassiveDto() {
		
		CongeAnnuelRestitutionMassive restitution = new CongeAnnuelRestitutionMassive();
		restitution.setDateModification(new Date());
		restitution.setDateRestitution(new Date());
		restitution.setIdCongeAnnuelRestitutionMassiveTask(2);
		restitution.setJournee(true);
		restitution.setMatin(false);
		restitution.setApresMidi(true);
		restitution.setMotif("motif");
		restitution.setStatus("status");
		
		RestitutionMassiveDto dto = new RestitutionMassiveDto(restitution);
		
		assertEquals(dto.getIdRestitutionMassive(), restitution.getIdCongeAnnuelRestitutionMassiveTask());
		assertEquals(dto.getDateModification(), restitution.getDateModification());
		assertEquals(dto.getDateRestitution(), restitution.getDateRestitution());
		assertEquals(dto.getMotif(), restitution.getMotif());
		assertEquals(dto.getStatus(), restitution.getStatus());
		assertEquals(dto.isJournee(), restitution.isJournee());
		assertEquals(dto.isMatin(), restitution.isMatin());
		assertEquals(dto.isApresMidi(), restitution.isApresMidi());
		assertEquals(dto.getListHistoAgents().size(), 0);
	}
}
