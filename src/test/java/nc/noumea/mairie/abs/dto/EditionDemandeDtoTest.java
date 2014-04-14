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
		Date dateDemande = new Date();

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
		etatDemande.setDate(dateDemande);

		DemandeReposComp d = new DemandeReposComp();
		d.setIdDemande(1);
		d.setDuree(52);
		d.setDureeAnneeN1(null);
		d.setIdAgent(ag.getIdAgent());
		d.setType(type);
		d.getEtatsDemande().add(etatDemande);

		DemandeDto demandeDto = new DemandeDto(d, agDto);
		demandeDto.updateEtat(d.getLatestEtatDemande());

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
		EditionDemandeDto result = new EditionDemandeDto(demandeDto, soldeDto, approbateurDto);

		// Then
		assertEquals("CHARVET", result.getApprobateur().getNom());
		assertEquals("Tatiana", result.getApprobateur().getPrenom());
		assertEquals(9005138, (int) result.getApprobateur().getIdAgent());

		assertEquals("RAYNAUD", result.getDemande().getAgentWithServiceDto().getNom());
		assertEquals("Nicolas", result.getDemande().getAgentWithServiceDto().getPrenom());
		assertEquals(9006765, (int) result.getDemande().getAgentWithServiceDto().getIdAgent());
		assertEquals("DIR", result.getDemande().getAgentWithServiceDto().getDirection());
		assertEquals("SERV", result.getDemande().getAgentWithServiceDto().getService());
		assertEquals("F", result.getDemande().getAgentWithServiceDto().getStatut());

		assertEquals("52.0", result.getDemande().getDuree().toString());
		assertEquals(9006765, (int) result.getDemande().getAgentWithServiceDto().getIdAgent());
		assertEquals(1, (int) result.getDemande().getIdDemande());
		assertEquals(2, (int) result.getDemande().getIdTypeDemande());
		assertEquals(4, (int) result.getDemande().getIdRefEtat());
		assertEquals(dateDemande, result.getDemande().getDateSaisie());

		assertEquals("22.0", result.getSolde().getSoldeRecup().toString());
		assertEquals("14.0", result.getSolde().getSoldeReposCompAnnee().toString());
		assertEquals("0.0", result.getSolde().getSoldeReposCompAnneePrec().toString());
		assertEquals("12.0", result.getSolde().getSoldeCongeAnnee().toString());
		assertEquals("12.2", result.getSolde().getSoldeCongeAnneePrec().toString());
	}
}
