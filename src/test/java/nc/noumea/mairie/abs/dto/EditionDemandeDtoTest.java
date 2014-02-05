package nc.noumea.mairie.abs.dto;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import nc.noumea.mairie.abs.domain.DemandeReposComp;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.sirh.domain.Agent;

import org.junit.Test;

public class EditionDemandeDtoTest {

	@Test
	public void ctor_withDemandeDto_AgentDto_SoldeDto_approbateurDto() {

		// Given
		Agent ag = new Agent();
			ag.setNomUsage("RAYNAUD");
			ag.setPrenomUsage("Nicolas");
			ag.setIdAgent(9006765);
		AgentWithServiceDto agDto = new AgentWithServiceDto(ag);
			agDto.setCodeService("DCCA");
			agDto.setDirection("DIR");
			agDto.setService("SERV");
			agDto.setStatut("F");

		RefTypeAbsence type = new RefTypeAbsence();
			type.setIdRefTypeAbsence(2);

		EtatDemande etatDemande = new EtatDemande();
			etatDemande.setEtat(RefEtatEnum.APPROUVEE);
			etatDemande.setDate(new Date());

		DemandeReposComp d = new DemandeReposComp();
			d.setIdDemande(1);
			d.setDuree(52);
			d.setDureeAnneeN1(null);
			d.setIdAgent(ag.getIdAgent());
			d.setType(type);
			d.getEtatsDemande().add(etatDemande);

		DemandeDto demandeDto = new DemandeDto(d, ag);

		SoldeDto soldeDto = new SoldeDto();
			soldeDto.setSoldeCongeAnnee((double) 12);
			soldeDto.setSoldeCongeAnneePrec((double) 12.2);
			soldeDto.setSoldeRecup((double) 22);
			soldeDto.setSoldeReposCompAnnee((double) 14);
			soldeDto.setSoldeReposCompAnneePrec((double) 0);

		Agent agAppro = new Agent();
			agAppro.setNomUsage("CHARVET");
			agAppro.setPrenomUsage("Tatiana");
			agAppro.setIdAgent(9005138);
		AgentWithServiceDto approbateurDto = new AgentWithServiceDto(agAppro);

		// When
		EditionDemandeDto result = new EditionDemandeDto(demandeDto, agDto, soldeDto, approbateurDto);

		// Then
		assertEquals("CHARVET", result.getApprobateur().getNom());
		assertEquals("Tatiana", result.getApprobateur().getPrenom());
		assertEquals(9005138, (int) result.getApprobateur().getIdAgent());

		assertEquals("RAYNAUD", result.getAgent().getNom());
		assertEquals("Nicolas", result.getAgent().getPrenom());
		assertEquals(9006765, (int) result.getAgent().getIdAgent());
		assertEquals("DIR", result.getAgent().getDirection());
		assertEquals("SERV", result.getAgent().getService());
		assertEquals("F", result.getAgent().getStatut());

		assertEquals(52, (int) result.getDemande().getDuree());
		assertEquals(9006765, (int) result.getDemande().getIdAgent());
		assertEquals(1, (int) result.getDemande().getIdDemande());
		assertEquals(2, (int) result.getDemande().getIdTypeDemande());

		assertEquals("22.0", result.getSolde().getSoldeRecup().toString());
		assertEquals("14.0", result.getSolde().getSoldeReposCompAnnee().toString());
		assertEquals("0.0", result.getSolde().getSoldeReposCompAnneePrec().toString());
		assertEquals("12.0", result.getSolde().getSoldeCongeAnnee().toString());
		assertEquals("12.2", result.getSolde().getSoldeCongeAnneePrec().toString());
	}
}
