package nc.noumea.mairie.abs.dto;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import nc.noumea.mairie.abs.domain.DemandeReposComp;
import nc.noumea.mairie.abs.domain.EtatDemandeReposComp;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefGroupeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeGroupeAbsenceEnum;

import org.junit.Test;

public class EditionDemandeDtoTest {

	@Test
	public void ctor_withDemandeDto_AgentDto_SoldeDto_approbateurDto() {

		// Given
		Date dateDemande = new Date();

		AgentGeneriqueDto ag = new AgentGeneriqueDto();
		ag.setNomUsage("RAYNAUD");
		ag.setPrenomUsage("Nicolas");
		ag.setIdAgent(9006765);
		AgentWithServiceDto agDto = new AgentWithServiceDto(ag);
		agDto.setIdServiceADS(1);
		agDto.setService("SERV");
		agDto.setStatut("F");

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.REPOS_COMP.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(2);
		type.setGroupe(groupe);

		EtatDemandeReposComp etatDemande = new EtatDemandeReposComp();
		etatDemande.setEtat(RefEtatEnum.APPROUVEE);
		etatDemande.setDate(dateDemande);
		etatDemande.setDuree(52);
		etatDemande.setDureeAnneeN1(null);

		DemandeReposComp d = new DemandeReposComp();
		d.setIdDemande(1);
		d.setDuree(52);
		d.setDureeAnneeN1(null);
		d.setIdAgent(ag.getIdAgent());
		d.setType(type);
		d.getEtatsDemande().add(etatDemande);
		d.setTotalMinutesOld(12);
		d.setTotalMinutesNew(14);
		d.setTotalMinutesAnneeN1Old(16);
		d.setTotalMinutesAnneeN1New(17);

		DemandeDto demandeDto = new DemandeDto(d, agDto, false);
		demandeDto.updateEtat(d.getLatestEtatDemande(), new AgentWithServiceDto(), d.getType().getGroupe());

		AgentGeneriqueDto agAppro = new AgentGeneriqueDto();
		agAppro.setNomUsage("CHARVET");
		agAppro.setPrenomUsage("Tatiana");
		agAppro.setIdAgent(9005138);
		AgentWithServiceDto approbateurDto = new AgentWithServiceDto(agAppro);

		// When
		EditionDemandeDto result = new EditionDemandeDto(demandeDto, approbateurDto);

		// Then
		assertEquals("CHARVET", result.getApprobateur().getNom());
		assertEquals("Tatiana", result.getApprobateur().getPrenom());
		assertEquals(9005138, (int) result.getApprobateur().getIdAgent());

		assertEquals("RAYNAUD", result.getDemande().getAgentWithServiceDto().getNom());
		assertEquals("Nicolas", result.getDemande().getAgentWithServiceDto().getPrenom());
		assertEquals(9006765, (int) result.getDemande().getAgentWithServiceDto().getIdAgent());
		assertEquals(1, (int) result.getDemande().getAgentWithServiceDto().getIdServiceADS());
		assertEquals("SERV", result.getDemande().getAgentWithServiceDto().getService());
		assertEquals("F", result.getDemande().getAgentWithServiceDto().getStatut());

		assertEquals("52.0", result.getDemande().getDuree().toString());
		assertEquals(9006765, (int) result.getDemande().getAgentWithServiceDto().getIdAgent());
		assertEquals(1, (int) result.getDemande().getIdDemande());
		assertEquals(2, (int) result.getDemande().getIdTypeDemande());
		assertEquals(4, (int) result.getDemande().getIdRefEtat());
		assertEquals(dateDemande, result.getDemande().getDateSaisie());

		assertEquals("14", result.getDemande().getTotalMinutesNew().toString());
		assertEquals("12", result.getDemande().getTotalMinutesOld().toString());
		assertEquals("17", result.getDemande().getTotalMinutesAnneeN1New().toString());
		assertEquals("16", result.getDemande().getTotalMinutesAnneeN1Old().toString());
	}
}
