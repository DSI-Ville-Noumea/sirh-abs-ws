package nc.noumea.mairie.abs.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import nc.noumea.mairie.abs.domain.RefUnitePeriodeQuota;

import org.junit.Test;

public class UnitePeriodeQuotaDtoTest {
	
	@Test
	public void ctor() {
		
		RefUnitePeriodeQuota rupq = new RefUnitePeriodeQuota();
		rupq.setIdRefUnitePeriodeQuota(1);
		rupq.setUnite("jours");
		rupq.setValeur(10);
		rupq.setGlissant(true);
		
		UnitePeriodeQuotaDto result = new UnitePeriodeQuotaDto(rupq);

		assertEquals(1, result.getIdRefUnitePeriodeQuota().intValue());
		assertEquals("jours", result.getUnite());
		assertEquals(10, result.getValeur().intValue());
		assertTrue(result.isGlissant());
	}
	
}
