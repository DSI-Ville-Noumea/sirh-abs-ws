package nc.noumea.mairie.abs.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;

import nc.noumea.mairie.abs.domain.DemandeAsa;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.sirh.domain.Agent;

import org.junit.Test;

public class DemandeDtoTest {

	@Test
	public void ctor_withDemande_Agent() {

		// Given
		Date dateDemande = new Date();

		Agent ag = new Agent();
		ag.setNomUsage("RAYNAUD");
		ag.setPrenomUsage("Nicolas");
		ag.setIdAgent(9006765);

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(7);

		EtatDemande etatDemande = new EtatDemande();
		etatDemande.setEtat(RefEtatEnum.APPROUVEE);
		etatDemande.setDate(dateDemande);

		DemandeAsa d = new DemandeAsa();
		d.setDateFin(dateDemande);
		d.setIdDemande(1);
		d.setIdAgent(ag.getIdAgent());
		d.setType(type);
		d.getEtatsDemande().add(etatDemande);

		// When
		DemandeDto result = new DemandeDto(d, ag);
		result.updateEtat(etatDemande);

		// Then

		assertEquals("RAYNAUD", result.getNomAgent());
		assertEquals("Nicolas", result.getPrenomAgent());
		assertEquals(9006765, (int) result.getIdAgent());

		assertNull(result.getDuree());
		assertEquals(9006765, (int) result.getIdAgent());
		assertEquals(1, (int) result.getIdDemande());
		assertEquals(7, (int) result.getIdTypeDemande());
		assertEquals(4, (int) result.getIdRefEtat());
		assertEquals(dateDemande, result.getDateSaisie());
		assertEquals(dateDemande, result.getDateFin());
	}
}
