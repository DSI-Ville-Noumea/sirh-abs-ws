package nc.noumea.mairie.abs.dto;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import nc.noumea.mairie.abs.domain.CongeAnnuelRestitutionMassiveHisto;

public class RestitutionMassiveHistoDtoTest {

	@Test
	public void ctor_RestitutionMassiveHistoDto() {
	
		CongeAnnuelRestitutionMassiveHisto histo = new CongeAnnuelRestitutionMassiveHisto();
		histo.setIdAgent(9005138);
		histo.setJours(1.0);
		histo.setStatus("status");
		
		RestitutionMassiveHistoDto dto = new RestitutionMassiveHistoDto(histo);
		
		assertEquals(dto.getIdAgent(), histo.getIdAgent());
		assertEquals(dto.getJours(), histo.getJours());
		assertEquals(dto.getStatus(), histo.getStatus());
	}
}
