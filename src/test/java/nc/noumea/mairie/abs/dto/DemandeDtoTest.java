package nc.noumea.mairie.abs.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DemandeAsa;
import nc.noumea.mairie.abs.domain.DemandeCongesAnnuels;
import nc.noumea.mairie.abs.domain.DemandeCongesExceptionnels;
import nc.noumea.mairie.abs.domain.DemandeRecup;
import nc.noumea.mairie.abs.domain.DemandeReposComp;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.EtatDemandeAsa;
import nc.noumea.mairie.abs.domain.PieceJointe;
import nc.noumea.mairie.abs.domain.EtatDemandeCongesAnnuels;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefGroupeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.domain.RefTypeGroupeAbsenceEnum;

import org.joda.time.DateTime;
import org.junit.Test;

public class DemandeDtoTest {

	@Test
	public void ctor_withDemande_Agent() {

		// Given
		Date dateDemande = new Date();

		AgentGeneriqueDto ag = new AgentGeneriqueDto();
		ag.setNomUsage("RAYNAUD");
		ag.setPrenomUsage("Nicolas");
		ag.setIdAgent(9006765);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A48.getValue());
		type.setGroupe(groupe);

		EtatDemandeAsa etatDemande = new EtatDemandeAsa();
		etatDemande.setEtat(RefEtatEnum.APPROUVEE);
		etatDemande.setDate(dateDemande);
		etatDemande.setIdAgent(9005131);
		etatDemande.setDateFin(dateDemande);
		AgentWithServiceDto agentEtat = new AgentWithServiceDto();
		agentEtat.setIdAgent(9005131);

		DemandeAsa d = new DemandeAsa();
		d.setDateFin(dateDemande);
		d.setIdDemande(1);
		d.setIdAgent(ag.getIdAgent());
		d.setType(type);
		d.getEtatsDemande().add(etatDemande);

		// When
		DemandeDto result = new DemandeDto(d, new AgentWithServiceDto(ag), false);
		result.updateEtat(etatDemande, agentEtat, d.getType().getGroupe());

		// Then

		assertEquals("RAYNAUD", result.getAgentWithServiceDto().getNom());
		assertEquals("Nicolas", result.getAgentWithServiceDto().getPrenom());
		assertEquals(9006765, (int) result.getAgentWithServiceDto().getIdAgent());

		assertNull(result.getDuree());
		assertEquals(9006765, (int) result.getAgentWithServiceDto().getIdAgent());
		assertEquals(1, (int) result.getIdDemande());
		assertEquals(7, (int) result.getIdTypeDemande());
		assertEquals(4, (int) result.getIdRefEtat());
		assertEquals(dateDemande, result.getDateSaisie());
		assertEquals(dateDemande, result.getDateFin());
		assertEquals(9005131, (int) result.getAgentEtat().getIdAgent());
	}

	@Test
	public void ctor_DemandeAsaA48() {

		// Given
		Date dateDebut = new Date();
		Date dateFin = new Date();
		Date dateDemande = new Date();

		AgentGeneriqueDto ag = new AgentGeneriqueDto();
		ag.setNomUsage("RAYNAUD");
		ag.setPrenomUsage("Nicolas");
		ag.setIdAgent(9006765);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A48.getValue());
		type.setGroupe(groupe);

		EtatDemande etatDemandeApprouve = new EtatDemande();
		etatDemandeApprouve.setEtat(RefEtatEnum.APPROUVEE);
		etatDemandeApprouve.setDate(dateDemande);
		etatDemandeApprouve.setMotif("motif approuve");

		EtatDemande etatDemandeRefuse = new EtatDemande();
		etatDemandeRefuse.setEtat(RefEtatEnum.REFUSEE);
		etatDemandeRefuse.setDate(dateDemande);
		etatDemandeRefuse.setMotif("motif refuse");

		EtatDemande etatDemandeVisaF = new EtatDemande();
		etatDemandeVisaF.setEtat(RefEtatEnum.VISEE_FAVORABLE);
		etatDemandeVisaF.setDate(dateDemande);
		etatDemandeVisaF.setMotif("motif visa f");

		EtatDemande etatDemandeVisaD = new EtatDemande();
		etatDemandeVisaD.setEtat(RefEtatEnum.VISEE_DEFAVORABLE);
		etatDemandeVisaD.setDate(dateDemande);
		etatDemandeVisaD.setMotif("motif visa d");

		DemandeAsa d = new DemandeAsa();
		d.setDateDebut(dateDebut);
		d.setDateFin(dateFin);
		d.setIdDemande(1);
		d.setIdAgent(ag.getIdAgent());
		d.setType(type);
		d.getEtatsDemande().add(etatDemandeApprouve);
		d.getEtatsDemande().add(etatDemandeVisaF);
		d.getEtatsDemande().add(etatDemandeRefuse);
		d.getEtatsDemande().add(etatDemandeVisaD);
		d.setDateDebutAM(true);
		d.setDateDebutPM(true);
		d.setDateFinAM(true);
		d.setDateFinPM(true);
		d.setDuree(10.0);

		// When
		DemandeDto result = new DemandeDto(d, new AgentWithServiceDto(ag), false);

		// Then
		assertEquals("RAYNAUD", result.getAgentWithServiceDto().getNom());
		assertEquals("Nicolas", result.getAgentWithServiceDto().getPrenom());
		assertEquals(9006765, (int) result.getAgentWithServiceDto().getIdAgent());

		assertEquals(new Double(10.0), result.getDuree());
		assertEquals(9006765, (int) result.getAgentWithServiceDto().getIdAgent());
		assertEquals(1, (int) result.getIdDemande());
		assertEquals(RefTypeAbsenceEnum.ASA_A48.getValue(), (int) result.getIdTypeDemande());
		assertEquals(dateDebut, result.getDateDebut());
		assertEquals(dateFin, result.getDateFin());
		assertEquals("motif approuve", result.getMotif());
		assertTrue(result.getIsValeurApprobation());
		assertTrue(result.getIsValeurVisa());
		assertTrue(result.isDateDebutAM());
		assertTrue(result.isDateDebutPM());
		assertTrue(result.isDateFinAM());
		assertTrue(result.isDateFinPM());
	}

	@Test
	public void ctor_DemandeRecup() {

		// Given
		Date dateDebut = new Date();
		Date dateFin = new Date();
		Date dateDemande = new Date();

		AgentGeneriqueDto ag = new AgentGeneriqueDto();
		ag.setNomUsage("RAYNAUD");
		ag.setPrenomUsage("Nicolas");
		ag.setIdAgent(9006765);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.RECUP.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.RECUP.getValue());
		type.setGroupe(groupe);

		EtatDemande etatDemandeApprouve = new EtatDemande();
		etatDemandeApprouve.setEtat(RefEtatEnum.APPROUVEE);
		etatDemandeApprouve.setDate(dateDemande);
		etatDemandeApprouve.setMotif("motif approuve");

		EtatDemande etatDemandeRefuse = new EtatDemande();
		etatDemandeRefuse.setEtat(RefEtatEnum.REFUSEE);
		etatDemandeRefuse.setDate(dateDemande);
		etatDemandeRefuse.setMotif("motif refuse");

		EtatDemande etatDemandeVisaF = new EtatDemande();
		etatDemandeVisaF.setEtat(RefEtatEnum.VISEE_FAVORABLE);
		etatDemandeVisaF.setDate(dateDemande);
		etatDemandeVisaF.setMotif("motif visa f");

		EtatDemande etatDemandeVisaD = new EtatDemande();
		etatDemandeVisaD.setEtat(RefEtatEnum.VISEE_DEFAVORABLE);
		etatDemandeVisaD.setDate(dateDemande);
		etatDemandeVisaD.setMotif("motif visa d");

		DemandeRecup d = new DemandeRecup();
		d.setDateDebut(dateDebut);
		d.setDateFin(dateFin);
		d.setIdDemande(1);
		d.setIdAgent(ag.getIdAgent());
		d.setType(type);
		d.getEtatsDemande().add(etatDemandeRefuse);
		d.getEtatsDemande().add(etatDemandeApprouve);
		d.getEtatsDemande().add(etatDemandeVisaD);
		d.getEtatsDemande().add(etatDemandeVisaF);
		d.setDuree(10);

		// When
		DemandeDto result = new DemandeDto(d, new AgentWithServiceDto(ag), false);

		// Then
		assertEquals("RAYNAUD", result.getAgentWithServiceDto().getNom());
		assertEquals("Nicolas", result.getAgentWithServiceDto().getPrenom());
		assertEquals(9006765, (int) result.getAgentWithServiceDto().getIdAgent());

		assertEquals(10, result.getDuree().intValue());
		assertEquals(9006765, (int) result.getAgentWithServiceDto().getIdAgent());
		assertEquals(1, (int) result.getIdDemande());
		assertEquals(RefTypeAbsenceEnum.RECUP.getValue(), (int) result.getIdTypeDemande());
		assertEquals(dateDebut, result.getDateDebut());
		assertEquals(dateFin, result.getDateFin());
		assertEquals("motif refuse", result.getMotif());
		assertFalse(result.getIsValeurApprobation());
		assertFalse(result.getIsValeurVisa());
		assertFalse(result.isDateDebutAM());
		assertFalse(result.isDateDebutPM());
		assertFalse(result.isDateFinAM());
		assertFalse(result.isDateFinPM());
	}

	@Test
	public void ctor_DemandeRepoComp() {

		// Given
		Date dateDebut = new Date();
		Date dateFin = new Date();
		Date dateDemande = new Date();

		AgentGeneriqueDto ag = new AgentGeneriqueDto();
		ag.setNomUsage("RAYNAUD");
		ag.setPrenomUsage("Nicolas");
		ag.setIdAgent(9006765);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.REPOS_COMP.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.REPOS_COMP.getValue());
		type.setGroupe(groupe);

		EtatDemande etatDemandeApprouve = new EtatDemande();
		etatDemandeApprouve.setEtat(RefEtatEnum.APPROUVEE);
		etatDemandeApprouve.setDate(dateDemande);
		etatDemandeApprouve.setMotif("motif approuve");

		EtatDemande etatDemandeRefuse = new EtatDemande();
		etatDemandeRefuse.setEtat(RefEtatEnum.REFUSEE);
		etatDemandeRefuse.setDate(dateDemande);
		etatDemandeRefuse.setMotif("motif refuse");

		EtatDemande etatDemandeVisaF = new EtatDemande();
		etatDemandeVisaF.setEtat(RefEtatEnum.VISEE_FAVORABLE);
		etatDemandeVisaF.setDate(dateDemande);
		etatDemandeVisaF.setMotif("motif visa f");

		EtatDemande etatDemandeVisaD = new EtatDemande();
		etatDemandeVisaD.setEtat(RefEtatEnum.VISEE_DEFAVORABLE);
		etatDemandeVisaD.setDate(dateDemande);
		etatDemandeVisaD.setMotif("motif visa d");

		DemandeReposComp d = new DemandeReposComp();
		d.setDateDebut(dateDebut);
		d.setDateFin(dateFin);
		d.setIdDemande(1);
		d.setIdAgent(ag.getIdAgent());
		d.setType(type);
		d.getEtatsDemande().add(etatDemandeApprouve);
		d.getEtatsDemande().add(etatDemandeVisaF);
		d.getEtatsDemande().add(etatDemandeRefuse);
		d.getEtatsDemande().add(etatDemandeVisaD);
		d.setDuree(10);
		d.setDureeAnneeN1(10);

		// When
		DemandeDto result = new DemandeDto(d, new AgentWithServiceDto(ag), false);

		// Then
		assertEquals("RAYNAUD", result.getAgentWithServiceDto().getNom());
		assertEquals("Nicolas", result.getAgentWithServiceDto().getPrenom());
		assertEquals(9006765, (int) result.getAgentWithServiceDto().getIdAgent());

		assertEquals(20, result.getDuree().intValue());
		assertEquals(9006765, (int) result.getAgentWithServiceDto().getIdAgent());
		assertEquals(1, (int) result.getIdDemande());
		assertEquals(RefTypeAbsenceEnum.REPOS_COMP.getValue(), (int) result.getIdTypeDemande());
		assertEquals(dateDebut, result.getDateDebut());
		assertEquals(dateFin, result.getDateFin());
		assertEquals("motif approuve", result.getMotif());
		assertTrue(result.getIsValeurApprobation());
		assertTrue(result.getIsValeurVisa());
		assertNull(result.getValeurValidation());
		assertFalse(result.isDateDebutAM());
		assertFalse(result.isDateDebutPM());
		assertFalse(result.isDateFinAM());
		assertFalse(result.isDateFinPM());
	}

	@Test
	public void ctor_DemandeAsaA54() {

		// Given
		Date dateDebut = new Date();
		Date dateFin = new Date();
		Date dateDemande = new Date();

		AgentGeneriqueDto ag = new AgentGeneriqueDto();
		ag.setNomUsage("RAYNAUD");
		ag.setPrenomUsage("Nicolas");
		ag.setIdAgent(9006765);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A54.getValue());
		type.setGroupe(groupe);

		EtatDemande etatDemandeApprouve = new EtatDemande();
		etatDemandeApprouve.setEtat(RefEtatEnum.APPROUVEE);
		etatDemandeApprouve.setDate(dateDemande);
		etatDemandeApprouve.setMotif("motif approuve");

		EtatDemande etatDemandeRefuse = new EtatDemande();
		etatDemandeRefuse.setEtat(RefEtatEnum.REFUSEE);
		etatDemandeRefuse.setDate(dateDemande);
		etatDemandeRefuse.setMotif("motif refuse");

		EtatDemande etatDemandeVisaF = new EtatDemande();
		etatDemandeVisaF.setEtat(RefEtatEnum.VISEE_FAVORABLE);
		etatDemandeVisaF.setDate(dateDemande);
		etatDemandeVisaF.setMotif("motif visa f");

		EtatDemande etatDemandeVisaD = new EtatDemande();
		etatDemandeVisaD.setEtat(RefEtatEnum.VISEE_DEFAVORABLE);
		etatDemandeVisaD.setDate(dateDemande);
		etatDemandeVisaD.setMotif("motif visa d");

		EtatDemande etatDemandeValide = new EtatDemande();
		etatDemandeValide.setEtat(RefEtatEnum.VALIDEE);
		etatDemandeValide.setDate(dateDemande);
		etatDemandeValide.setMotif("motif valide");

		EtatDemande etatDemandeRejete = new EtatDemande();
		etatDemandeRejete.setEtat(RefEtatEnum.REJETE);
		etatDemandeRejete.setDate(dateDemande);
		etatDemandeRejete.setMotif("motif rejete");

		DemandeAsa d = new DemandeAsa();
		d.setDateDebut(dateDebut);
		d.setDateFin(dateFin);
		d.setIdDemande(1);
		d.setIdAgent(ag.getIdAgent());
		d.setType(type);
		d.getEtatsDemande().add(etatDemandeApprouve);
		d.getEtatsDemande().add(etatDemandeVisaF);
		d.getEtatsDemande().add(etatDemandeRefuse);
		d.getEtatsDemande().add(etatDemandeVisaD);
		d.getEtatsDemande().add(etatDemandeRejete);
		d.getEtatsDemande().add(etatDemandeValide);
		d.setDateDebutAM(true);
		d.setDateDebutPM(true);
		d.setDateFinAM(true);
		d.setDateFinPM(true);
		d.setDuree(10.0);

		// When
		DemandeDto result = new DemandeDto(d, new AgentWithServiceDto(ag), false);

		// Then
		assertEquals("RAYNAUD", result.getAgentWithServiceDto().getNom());
		assertEquals("Nicolas", result.getAgentWithServiceDto().getPrenom());
		assertEquals(9006765, (int) result.getAgentWithServiceDto().getIdAgent());

		assertEquals(new Double(10.0), result.getDuree());
		assertEquals(9006765, (int) result.getAgentWithServiceDto().getIdAgent());
		assertEquals(1, (int) result.getIdDemande());
		assertEquals(RefTypeAbsenceEnum.ASA_A54.getValue(), (int) result.getIdTypeDemande());
		assertEquals(dateDebut, result.getDateDebut());
		assertEquals(dateFin, result.getDateFin());
		assertEquals("motif approuve", result.getMotif());
		assertTrue(result.getIsValeurApprobation());
		assertFalse(result.getValeurValidation());
		assertTrue(result.getIsValeurVisa());
		assertTrue(result.isDateDebutAM());
		assertTrue(result.isDateDebutPM());
		assertTrue(result.isDateFinAM());
		assertTrue(result.isDateFinPM());
	}

	@Test
	public void ctor_DemandeAsaA55() {

		// Given
		Date dateDebut = new Date();
		Date dateFin = new Date();
		Date dateDemande = new Date();

		AgentGeneriqueDto ag = new AgentGeneriqueDto();
		ag.setNomUsage("RAYNAUD");
		ag.setPrenomUsage("Nicolas");
		ag.setIdAgent(9006765);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A55.getValue());
		type.setGroupe(groupe);

		EtatDemande etatDemandeApprouve = new EtatDemande();
		etatDemandeApprouve.setEtat(RefEtatEnum.APPROUVEE);
		etatDemandeApprouve.setDate(dateDemande);
		etatDemandeApprouve.setMotif("motif approuve");

		EtatDemande etatDemandeRefuse = new EtatDemande();
		etatDemandeRefuse.setEtat(RefEtatEnum.REFUSEE);
		etatDemandeRefuse.setDate(dateDemande);
		etatDemandeRefuse.setMotif("motif refuse");

		EtatDemande etatDemandeVisaF = new EtatDemande();
		etatDemandeVisaF.setEtat(RefEtatEnum.VISEE_FAVORABLE);
		etatDemandeVisaF.setDate(dateDemande);
		etatDemandeVisaF.setMotif("motif visa f");

		EtatDemande etatDemandeVisaD = new EtatDemande();
		etatDemandeVisaD.setEtat(RefEtatEnum.VISEE_DEFAVORABLE);
		etatDemandeVisaD.setDate(dateDemande);
		etatDemandeVisaD.setMotif("motif visa d");

		EtatDemande etatDemandeValide = new EtatDemande();
		etatDemandeValide.setEtat(RefEtatEnum.VALIDEE);
		etatDemandeValide.setDate(dateDemande);
		etatDemandeValide.setMotif("motif valide");

		EtatDemande etatDemandeRejete = new EtatDemande();
		etatDemandeRejete.setEtat(RefEtatEnum.REJETE);
		etatDemandeRejete.setDate(dateDemande);
		etatDemandeRejete.setMotif("motif rejete");

		DemandeAsa d = new DemandeAsa();
		d.setDateDebut(dateDebut);
		d.setDateFin(dateFin);
		d.setIdDemande(1);
		d.setIdAgent(ag.getIdAgent());
		d.setType(type);
		d.getEtatsDemande().add(etatDemandeApprouve);
		d.getEtatsDemande().add(etatDemandeVisaF);
		d.getEtatsDemande().add(etatDemandeRefuse);
		d.getEtatsDemande().add(etatDemandeVisaD);
		d.getEtatsDemande().add(etatDemandeValide);
		d.getEtatsDemande().add(etatDemandeRejete);
		d.setDuree(10.0);

		// When
		DemandeDto result = new DemandeDto(d, new AgentWithServiceDto(ag), false);

		// Then
		assertEquals("RAYNAUD", result.getAgentWithServiceDto().getNom());
		assertEquals("Nicolas", result.getAgentWithServiceDto().getPrenom());
		assertEquals(9006765, (int) result.getAgentWithServiceDto().getIdAgent());

		assertEquals(new Double(10.0), result.getDuree());
		assertEquals(9006765, (int) result.getAgentWithServiceDto().getIdAgent());
		assertEquals(1, (int) result.getIdDemande());
		assertEquals(RefTypeAbsenceEnum.ASA_A55.getValue(), (int) result.getIdTypeDemande());
		assertEquals(dateDebut, result.getDateDebut());
		assertEquals(dateFin, result.getDateFin());
		assertEquals("motif approuve", result.getMotif());
		assertTrue(result.getIsValeurApprobation());
		assertTrue(result.getIsValeurVisa());
		assertTrue(result.getValeurValidation());
		assertFalse(result.isDateDebutAM());
		assertFalse(result.isDateDebutPM());
		assertFalse(result.isDateFinAM());
		assertFalse(result.isDateFinPM());
	}

	@Test
	public void ctor_DemandeAsaA52() {

		// Given
		Date dateDebut = new Date();
		Date dateFin = new Date();
		Date dateDemande = new Date();

		AgentGeneriqueDto ag = new AgentGeneriqueDto();
		ag.setNomUsage("RAYNAUD");
		ag.setPrenomUsage("Nicolas");
		ag.setIdAgent(9006765);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A52.getValue());
		type.setGroupe(groupe);

		EtatDemande etatDemandeApprouve = new EtatDemande();
		etatDemandeApprouve.setEtat(RefEtatEnum.APPROUVEE);
		etatDemandeApprouve.setDate(dateDemande);
		etatDemandeApprouve.setMotif("motif approuve");

		EtatDemande etatDemandeRefuse = new EtatDemande();
		etatDemandeRefuse.setEtat(RefEtatEnum.REFUSEE);
		etatDemandeRefuse.setDate(dateDemande);
		etatDemandeRefuse.setMotif("motif refuse");

		EtatDemande etatDemandeVisaF = new EtatDemande();
		etatDemandeVisaF.setEtat(RefEtatEnum.VISEE_FAVORABLE);
		etatDemandeVisaF.setDate(dateDemande);
		etatDemandeVisaF.setMotif("motif visa f");

		EtatDemande etatDemandeVisaD = new EtatDemande();
		etatDemandeVisaD.setEtat(RefEtatEnum.VISEE_DEFAVORABLE);
		etatDemandeVisaD.setDate(dateDemande);
		etatDemandeVisaD.setMotif("motif visa d");

		EtatDemande etatDemandeValide = new EtatDemande();
		etatDemandeValide.setEtat(RefEtatEnum.VALIDEE);
		etatDemandeValide.setDate(dateDemande);
		etatDemandeValide.setMotif("motif valide");

		EtatDemande etatDemandeRejete = new EtatDemande();
		etatDemandeRejete.setEtat(RefEtatEnum.REJETE);
		etatDemandeRejete.setDate(dateDemande);
		etatDemandeRejete.setMotif("motif rejete");

		DemandeAsa d = new DemandeAsa();
		d.setDateDebut(dateDebut);
		d.setDateFin(dateFin);
		d.setIdDemande(1);
		d.setIdAgent(ag.getIdAgent());
		d.setType(type);
		d.getEtatsDemande().add(etatDemandeApprouve);
		d.getEtatsDemande().add(etatDemandeVisaF);
		d.getEtatsDemande().add(etatDemandeRefuse);
		d.getEtatsDemande().add(etatDemandeVisaD);
		d.getEtatsDemande().add(etatDemandeValide);
		d.getEtatsDemande().add(etatDemandeRejete);
		d.setDuree(10.0);

		// When
		DemandeDto result = new DemandeDto(d, new AgentWithServiceDto(ag), false);

		// Then
		assertEquals("RAYNAUD", result.getAgentWithServiceDto().getNom());
		assertEquals("Nicolas", result.getAgentWithServiceDto().getPrenom());
		assertEquals(9006765, (int) result.getAgentWithServiceDto().getIdAgent());

		assertEquals(new Double(10.0), result.getDuree());
		assertEquals(9006765, (int) result.getAgentWithServiceDto().getIdAgent());
		assertEquals(1, (int) result.getIdDemande());
		assertEquals(RefTypeAbsenceEnum.ASA_A52.getValue(), (int) result.getIdTypeDemande());
		assertEquals(dateDebut, result.getDateDebut());
		assertEquals(dateFin, result.getDateFin());
		assertEquals("motif approuve", result.getMotif());
		assertTrue(result.getIsValeurApprobation());
		assertTrue(result.getIsValeurVisa());
		assertTrue(result.getValeurValidation());
		assertFalse(result.isDateDebutAM());
		assertFalse(result.isDateDebutPM());
		assertFalse(result.isDateFinAM());
		assertFalse(result.isDateFinPM());
	}

	@Test
	public void ctor_DemandeAsaA53() {

		// Given
		Date dateDebut = new Date();
		Date dateFin = new Date();
		Date dateDemande = new Date();

		AgentGeneriqueDto ag = new AgentGeneriqueDto();
		ag.setNomUsage("RAYNAUD");
		ag.setPrenomUsage("Nicolas");
		ag.setIdAgent(9006765);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A53.getValue());
		type.setGroupe(groupe);

		EtatDemande etatDemandeApprouve = new EtatDemande();
		etatDemandeApprouve.setEtat(RefEtatEnum.APPROUVEE);
		etatDemandeApprouve.setDate(dateDemande);
		etatDemandeApprouve.setMotif("motif approuve");

		EtatDemande etatDemandeRefuse = new EtatDemande();
		etatDemandeRefuse.setEtat(RefEtatEnum.REFUSEE);
		etatDemandeRefuse.setDate(dateDemande);
		etatDemandeRefuse.setMotif("motif refuse");

		EtatDemande etatDemandeVisaF = new EtatDemande();
		etatDemandeVisaF.setEtat(RefEtatEnum.VISEE_FAVORABLE);
		etatDemandeVisaF.setDate(dateDemande);
		etatDemandeVisaF.setMotif("motif visa f");

		EtatDemande etatDemandeVisaD = new EtatDemande();
		etatDemandeVisaD.setEtat(RefEtatEnum.VISEE_DEFAVORABLE);
		etatDemandeVisaD.setDate(dateDemande);
		etatDemandeVisaD.setMotif("motif visa d");

		EtatDemande etatDemandeValide = new EtatDemande();
		etatDemandeValide.setEtat(RefEtatEnum.VALIDEE);
		etatDemandeValide.setDate(dateDemande);
		etatDemandeValide.setMotif("motif valide");

		EtatDemande etatDemandeRejete = new EtatDemande();
		etatDemandeRejete.setEtat(RefEtatEnum.REJETE);
		etatDemandeRejete.setDate(dateDemande);
		etatDemandeRejete.setMotif("motif rejete");

		DemandeAsa d = new DemandeAsa();
		d.setDateDebut(dateDebut);
		d.setDateFin(dateFin);
		d.setIdDemande(1);
		d.setIdAgent(ag.getIdAgent());
		d.setType(type);
		d.getEtatsDemande().add(etatDemandeApprouve);
		d.getEtatsDemande().add(etatDemandeVisaF);
		d.getEtatsDemande().add(etatDemandeRefuse);
		d.getEtatsDemande().add(etatDemandeVisaD);
		d.getEtatsDemande().add(etatDemandeValide);
		d.getEtatsDemande().add(etatDemandeRejete);
		d.setDuree(10.0);

		// When
		DemandeDto result = new DemandeDto(d, new AgentWithServiceDto(ag), false);

		// Then
		assertEquals("RAYNAUD", result.getAgentWithServiceDto().getNom());
		assertEquals("Nicolas", result.getAgentWithServiceDto().getPrenom());
		assertEquals(9006765, (int) result.getAgentWithServiceDto().getIdAgent());

		assertEquals(new Double(10.0), result.getDuree());
		assertEquals(9006765, (int) result.getAgentWithServiceDto().getIdAgent());
		assertEquals(1, (int) result.getIdDemande());
		assertEquals(RefTypeAbsenceEnum.ASA_A53.getValue(), (int) result.getIdTypeDemande());
		assertEquals(dateDebut, result.getDateDebut());
		assertEquals(dateFin, result.getDateFin());
		assertEquals("motif approuve", result.getMotif());
		assertTrue(result.getIsValeurApprobation());
		assertTrue(result.getIsValeurVisa());
		assertTrue(result.getValeurValidation());
		assertFalse(result.isDateDebutAM());
		assertFalse(result.isDateDebutPM());
		assertFalse(result.isDateFinAM());
		assertFalse(result.isDateFinPM());
	}

	@Test
	public void ctor_DemandeAsaA50() {

		// Given
		Date dateDebut = new Date();
		Date dateFin = new Date();
		Date dateDemande = new Date();

		AgentGeneriqueDto ag = new AgentGeneriqueDto();
		ag.setNomUsage("RAYNAUD");
		ag.setPrenomUsage("Nicolas");
		ag.setIdAgent(9006765);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A50.getValue());
		type.setGroupe(groupe);

		EtatDemande etatDemandeApprouve = new EtatDemande();
		etatDemandeApprouve.setEtat(RefEtatEnum.APPROUVEE);
		etatDemandeApprouve.setDate(dateDemande);
		etatDemandeApprouve.setMotif("motif approuve");

		EtatDemande etatDemandeRefuse = new EtatDemande();
		etatDemandeRefuse.setEtat(RefEtatEnum.REFUSEE);
		etatDemandeRefuse.setDate(dateDemande);
		etatDemandeRefuse.setMotif("motif refuse");

		EtatDemande etatDemandeVisaF = new EtatDemande();
		etatDemandeVisaF.setEtat(RefEtatEnum.VISEE_FAVORABLE);
		etatDemandeVisaF.setDate(dateDemande);
		etatDemandeVisaF.setMotif("motif visa f");

		EtatDemande etatDemandeVisaD = new EtatDemande();
		etatDemandeVisaD.setEtat(RefEtatEnum.VISEE_DEFAVORABLE);
		etatDemandeVisaD.setDate(dateDemande);
		etatDemandeVisaD.setMotif("motif visa d");

		EtatDemande etatDemandeValide = new EtatDemande();
		etatDemandeValide.setEtat(RefEtatEnum.VALIDEE);
		etatDemandeValide.setDate(dateDemande);
		etatDemandeValide.setMotif("motif valide");

		EtatDemande etatDemandeRejete = new EtatDemande();
		etatDemandeRejete.setEtat(RefEtatEnum.REJETE);
		etatDemandeRejete.setDate(dateDemande);
		etatDemandeRejete.setMotif("motif rejete");

		DemandeAsa d = new DemandeAsa();
		d.setDateDebut(dateDebut);
		d.setDateFin(dateFin);
		d.setIdDemande(1);
		d.setIdAgent(ag.getIdAgent());
		d.setType(type);
		d.getEtatsDemande().add(etatDemandeApprouve);
		d.getEtatsDemande().add(etatDemandeVisaF);
		d.getEtatsDemande().add(etatDemandeRefuse);
		d.getEtatsDemande().add(etatDemandeVisaD);
		d.getEtatsDemande().add(etatDemandeValide);
		d.getEtatsDemande().add(etatDemandeRejete);
		d.setDuree(10.0);

		// When
		DemandeDto result = new DemandeDto(d, new AgentWithServiceDto(ag), false);

		// Then
		assertEquals("RAYNAUD", result.getAgentWithServiceDto().getNom());
		assertEquals("Nicolas", result.getAgentWithServiceDto().getPrenom());
		assertEquals(9006765, (int) result.getAgentWithServiceDto().getIdAgent());

		assertEquals(new Double(10.0), result.getDuree());
		assertEquals(9006765, (int) result.getAgentWithServiceDto().getIdAgent());
		assertEquals(1, (int) result.getIdDemande());
		assertEquals(RefTypeAbsenceEnum.ASA_A50.getValue(), (int) result.getIdTypeDemande());
		assertEquals(dateDebut, result.getDateDebut());
		assertEquals(dateFin, result.getDateFin());
		assertEquals("motif approuve", result.getMotif());
		assertTrue(result.getIsValeurApprobation());
		assertTrue(result.getIsValeurVisa());
		assertTrue(result.getValeurValidation());
		assertFalse(result.isDateDebutAM());
		assertFalse(result.isDateDebutPM());
		assertFalse(result.isDateFinAM());
		assertFalse(result.isDateFinPM());
	}

	@Test
	public void ctor_DemandeAsaA49() {

		// Given
		Date dateDebut = new Date();
		Date dateFin = new Date();
		Date dateDemande = new Date();

		AgentGeneriqueDto ag = new AgentGeneriqueDto();
		ag.setNomUsage("RAYNAUD");
		ag.setPrenomUsage("Nicolas");
		ag.setIdAgent(9006765);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A49.getValue());
		type.setGroupe(groupe);

		EtatDemande etatDemandeApprouve = new EtatDemande();
		etatDemandeApprouve.setEtat(RefEtatEnum.APPROUVEE);
		etatDemandeApprouve.setDate(dateDemande);
		etatDemandeApprouve.setMotif("motif approuve");

		EtatDemande etatDemandeRefuse = new EtatDemande();
		etatDemandeRefuse.setEtat(RefEtatEnum.REFUSEE);
		etatDemandeRefuse.setDate(dateDemande);
		etatDemandeRefuse.setMotif("motif refuse");

		EtatDemande etatDemandeVisaF = new EtatDemande();
		etatDemandeVisaF.setEtat(RefEtatEnum.VISEE_FAVORABLE);
		etatDemandeVisaF.setDate(dateDemande);
		etatDemandeVisaF.setMotif("motif visa f");

		EtatDemande etatDemandeVisaD = new EtatDemande();
		etatDemandeVisaD.setEtat(RefEtatEnum.VISEE_DEFAVORABLE);
		etatDemandeVisaD.setDate(dateDemande);
		etatDemandeVisaD.setMotif("motif visa d");

		EtatDemande etatDemandeValide = new EtatDemande();
		etatDemandeValide.setEtat(RefEtatEnum.VALIDEE);
		etatDemandeValide.setDate(dateDemande);
		etatDemandeValide.setMotif("motif valide");

		EtatDemande etatDemandeRejete = new EtatDemande();
		etatDemandeRejete.setEtat(RefEtatEnum.REJETE);
		etatDemandeRejete.setDate(dateDemande);
		etatDemandeRejete.setMotif("motif rejete");

		DemandeAsa d = new DemandeAsa();
		d.setDateDebut(dateDebut);
		d.setDateFin(dateFin);
		d.setIdDemande(1);
		d.setIdAgent(ag.getIdAgent());
		d.setType(type);
		d.getEtatsDemande().add(etatDemandeApprouve);
		d.getEtatsDemande().add(etatDemandeVisaF);
		d.getEtatsDemande().add(etatDemandeRefuse);
		d.getEtatsDemande().add(etatDemandeVisaD);
		d.getEtatsDemande().add(etatDemandeValide);
		d.getEtatsDemande().add(etatDemandeRejete);
		d.setDuree(10.0);

		// When
		DemandeDto result = new DemandeDto(d, new AgentWithServiceDto(ag), false);

		// Then
		assertEquals("RAYNAUD", result.getAgentWithServiceDto().getNom());
		assertEquals("Nicolas", result.getAgentWithServiceDto().getPrenom());
		assertEquals(9006765, (int) result.getAgentWithServiceDto().getIdAgent());

		assertEquals(new Double(10.0), result.getDuree());
		assertEquals(9006765, (int) result.getAgentWithServiceDto().getIdAgent());
		assertEquals(1, (int) result.getIdDemande());
		assertEquals(RefTypeAbsenceEnum.ASA_A49.getValue(), (int) result.getIdTypeDemande());
		assertEquals(dateDebut, result.getDateDebut());
		assertEquals(dateFin, result.getDateFin());
		assertEquals("motif approuve", result.getMotif());
		assertTrue(result.getIsValeurApprobation());
		assertTrue(result.getIsValeurVisa());
		assertTrue(result.getValeurValidation());
		assertFalse(result.isDateDebutAM());
		assertFalse(result.isDateDebutPM());
		assertFalse(result.isDateFinAM());
		assertFalse(result.isDateFinPM());
	}

	@Test
	public void ctor_DemandeCongeExcep() {

		// Given
		Date dateDebut = new Date();
		Date dateFin = new Date();
		Date dateDemande = new Date();

		AgentGeneriqueDto ag = new AgentGeneriqueDto();
		ag.setNomUsage("RAYNAUD");
		ag.setPrenomUsage("Nicolas");
		ag.setIdAgent(9006765);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_EXCEP.getValue());

		EtatDemande etatDemandeApprouve = new EtatDemande();
		etatDemandeApprouve.setEtat(RefEtatEnum.APPROUVEE);
		etatDemandeApprouve.setDate(dateDemande);
		etatDemandeApprouve.setMotif("motif approuve");

		EtatDemande etatDemandeRefuse = new EtatDemande();
		etatDemandeRefuse.setEtat(RefEtatEnum.REFUSEE);
		etatDemandeRefuse.setDate(dateDemande);
		etatDemandeRefuse.setMotif("motif refuse");

		EtatDemande etatDemandeVisaF = new EtatDemande();
		etatDemandeVisaF.setEtat(RefEtatEnum.VISEE_FAVORABLE);
		etatDemandeVisaF.setDate(dateDemande);
		etatDemandeVisaF.setMotif("motif visa f");

		EtatDemande etatDemandeVisaD = new EtatDemande();
		etatDemandeVisaD.setEtat(RefEtatEnum.VISEE_DEFAVORABLE);
		etatDemandeVisaD.setDate(dateDemande);
		etatDemandeVisaD.setMotif("motif visa d");

		RefTypeAbsence type = new RefTypeAbsence();
		type.setGroupe(groupe);
		type.setIdRefTypeAbsence(24);

		DemandeCongesExceptionnels d = new DemandeCongesExceptionnels();
		d.setDateDebut(dateDebut);
		d.setDateFin(dateFin);
		d.setIdDemande(1);
		d.setIdAgent(ag.getIdAgent());
		d.getEtatsDemande().add(etatDemandeApprouve);
		d.getEtatsDemande().add(etatDemandeVisaF);
		d.getEtatsDemande().add(etatDemandeRefuse);
		d.getEtatsDemande().add(etatDemandeVisaD);
		d.setDuree(10.0);
		d.setType(type);

		// When
		DemandeDto result = new DemandeDto(d, new AgentWithServiceDto(ag), false);

		// Then
		assertEquals("RAYNAUD", result.getAgentWithServiceDto().getNom());
		assertEquals("Nicolas", result.getAgentWithServiceDto().getPrenom());
		assertEquals(9006765, (int) result.getAgentWithServiceDto().getIdAgent());

		assertEquals(10, result.getDuree().intValue());
		assertEquals(9006765, (int) result.getAgentWithServiceDto().getIdAgent());
		assertEquals(1, (int) result.getIdDemande());
		assertEquals(dateDebut, result.getDateDebut());
		assertEquals(dateFin, result.getDateFin());
		assertEquals("motif approuve", result.getMotif());
		assertTrue(result.getIsValeurApprobation());
		assertTrue(result.getIsValeurVisa());
		assertNull(result.getValeurValidation());
		assertFalse(result.isDateDebutAM());
		assertFalse(result.isDateDebutPM());
		assertFalse(result.isDateFinAM());
		assertFalse(result.isDateFinPM());
	}

	@Test
	public void ctor_DemandeCongeAnnuel() {

		// Given
		Date dateDebut = new Date();
		Date dateFin = new Date();
		Date dateDemande = new Date();

		AgentGeneriqueDto ag = new AgentGeneriqueDto();
		ag.setNomUsage("RAYNAUD");
		ag.setPrenomUsage("Nicolas");
		ag.setIdAgent(9006765);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.CONGE_ANNUEL.getValue());
		type.setGroupe(groupe);

		EtatDemande etatDemandeApprouve = new EtatDemande();
		etatDemandeApprouve.setEtat(RefEtatEnum.APPROUVEE);
		etatDemandeApprouve.setDate(dateDemande);
		etatDemandeApprouve.setMotif("motif approuve");

		EtatDemande etatDemandeRefuse = new EtatDemande();
		etatDemandeRefuse.setEtat(RefEtatEnum.REFUSEE);
		etatDemandeRefuse.setDate(dateDemande);
		etatDemandeRefuse.setMotif("motif refuse");

		EtatDemande etatDemandeVisaF = new EtatDemande();
		etatDemandeVisaF.setEtat(RefEtatEnum.VISEE_FAVORABLE);
		etatDemandeVisaF.setDate(dateDemande);
		etatDemandeVisaF.setMotif("motif visa f");

		EtatDemande etatDemandeVisaD = new EtatDemande();
		etatDemandeVisaD.setEtat(RefEtatEnum.VISEE_DEFAVORABLE);
		etatDemandeVisaD.setDate(dateDemande);
		etatDemandeVisaD.setMotif("motif visa d");

		DemandeCongesAnnuels d = new DemandeCongesAnnuels();
		d.setDateDebut(dateDebut);
		d.setDateFin(dateFin);
		d.setIdDemande(1);
		d.setIdAgent(ag.getIdAgent());
		d.setType(type);
		d.getEtatsDemande().add(etatDemandeApprouve);
		d.getEtatsDemande().add(etatDemandeVisaF);
		d.getEtatsDemande().add(etatDemandeRefuse);
		d.getEtatsDemande().add(etatDemandeVisaD);
		d.setDuree(10.0);
		d.setNbSamediOffert(1.0);

		// When
		DemandeDto result = new DemandeDto(d, new AgentWithServiceDto(ag), false);

		// Then
		assertEquals("RAYNAUD", result.getAgentWithServiceDto().getNom());
		assertEquals("Nicolas", result.getAgentWithServiceDto().getPrenom());
		assertEquals(9006765, (int) result.getAgentWithServiceDto().getIdAgent());

		assertEquals(10, result.getDuree().intValue());
		assertEquals(9006765, (int) result.getAgentWithServiceDto().getIdAgent());
		assertEquals(1, (int) result.getIdDemande());
		assertEquals(RefTypeAbsenceEnum.CONGE_ANNUEL.getValue(), (int) result.getIdTypeDemande());
		assertEquals(dateDebut, result.getDateDebut());
		assertEquals(dateFin, result.getDateFin());
		assertEquals("motif approuve", result.getMotif());
		assertTrue(result.getIsValeurApprobation());
		assertTrue(result.getIsValeurVisa());
		assertNull(result.getValeurValidation());
		assertFalse(result.isDateDebutAM());
		assertFalse(result.isDateDebutPM());
		assertFalse(result.isDateFinAM());
		assertFalse(result.isDateFinPM());
		assertTrue(result.isSamediOffert());
	}

	@Test
	public void ctor_DemandeCongeAnnuel_WithAnneN1() {

		// Given
		Date dateDebut = new Date();
		Date dateFin = new Date();
		Date dateDemande = new Date();

		AgentGeneriqueDto ag = new AgentGeneriqueDto();
		ag.setNomUsage("RAYNAUD");
		ag.setPrenomUsage("Nicolas");
		ag.setIdAgent(9006765);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.CONGE_ANNUEL.getValue());
		type.setGroupe(groupe);

		EtatDemande etatDemandeApprouve = new EtatDemande();
		etatDemandeApprouve.setEtat(RefEtatEnum.APPROUVEE);
		etatDemandeApprouve.setDate(dateDemande);
		etatDemandeApprouve.setMotif("motif approuve");

		EtatDemande etatDemandeRefuse = new EtatDemande();
		etatDemandeRefuse.setEtat(RefEtatEnum.REFUSEE);
		etatDemandeRefuse.setDate(dateDemande);
		etatDemandeRefuse.setMotif("motif refuse");

		EtatDemande etatDemandeVisaF = new EtatDemande();
		etatDemandeVisaF.setEtat(RefEtatEnum.VISEE_FAVORABLE);
		etatDemandeVisaF.setDate(dateDemande);
		etatDemandeVisaF.setMotif("motif visa f");

		EtatDemande etatDemandeVisaD = new EtatDemande();
		etatDemandeVisaD.setEtat(RefEtatEnum.VISEE_DEFAVORABLE);
		etatDemandeVisaD.setDate(dateDemande);
		etatDemandeVisaD.setMotif("motif visa d");

		DemandeCongesAnnuels d = new DemandeCongesAnnuels();
		d.setDateDebut(dateDebut);
		d.setDateFin(dateFin);
		d.setIdDemande(1);
		d.setIdAgent(ag.getIdAgent());
		d.setType(type);
		d.getEtatsDemande().add(etatDemandeApprouve);
		d.getEtatsDemande().add(etatDemandeVisaF);
		d.getEtatsDemande().add(etatDemandeRefuse);
		d.getEtatsDemande().add(etatDemandeVisaD);
		d.setDuree(10.0);
		d.setDureeAnneeN1(5.0);
		d.setNbSamediOffert(0.0);
		;

		// When
		DemandeDto result = new DemandeDto(d, new AgentWithServiceDto(ag), false);

		// Then
		assertEquals("RAYNAUD", result.getAgentWithServiceDto().getNom());
		assertEquals("Nicolas", result.getAgentWithServiceDto().getPrenom());
		assertEquals(9006765, (int) result.getAgentWithServiceDto().getIdAgent());

		assertEquals(15, result.getDuree().intValue());
		assertEquals(9006765, (int) result.getAgentWithServiceDto().getIdAgent());
		assertEquals(1, (int) result.getIdDemande());
		assertEquals(RefTypeAbsenceEnum.CONGE_ANNUEL.getValue(), (int) result.getIdTypeDemande());
		assertEquals(dateDebut, result.getDateDebut());
		assertEquals(dateFin, result.getDateFin());
		assertEquals("motif approuve", result.getMotif());
		assertTrue(result.getIsValeurApprobation());
		assertTrue(result.getIsValeurVisa());
		assertNull(result.getValeurValidation());
		assertFalse(result.isDateDebutAM());
		assertFalse(result.isDateDebutPM());
		assertFalse(result.isDateFinAM());
		assertFalse(result.isDateFinPM());
		assertFalse(result.isSamediOffert());
	}
	
	@Test
	public void ctor_DemandeDto_withPJ(){

		// Given
		Date dateDemande = new Date();

		AgentGeneriqueDto ag = new AgentGeneriqueDto();
		ag.setNomUsage("RAYNAUD");
		ag.setPrenomUsage("Nicolas");
		ag.setIdAgent(9006765);

		RefGroupeAbsence groupe = new RefGroupeAbsence();
		groupe.setIdRefGroupeAbsence(RefTypeGroupeAbsenceEnum.AS.getValue());

		RefTypeAbsence type = new RefTypeAbsence();
		type.setIdRefTypeAbsence(RefTypeAbsenceEnum.ASA_A48.getValue());
		type.setGroupe(groupe);

		EtatDemandeAsa etatDemande = new EtatDemandeAsa();
		etatDemande.setEtat(RefEtatEnum.APPROUVEE);
		etatDemande.setDate(dateDemande);
		etatDemande.setIdAgent(9005131);
		etatDemande.setDateFin(dateDemande);
		AgentWithServiceDto agentEtat = new AgentWithServiceDto();
		agentEtat.setIdAgent(9005131);

		DemandeAsa d = new DemandeAsa();
		d.setDateFin(dateDemande);
		d.setIdDemande(1);
		d.setIdAgent(ag.getIdAgent());
		d.setType(type);
		d.getEtatsDemande().add(etatDemande);
		
		PieceJointe pj = new PieceJointe();
		pj.setTitre("titrePJ");
		pj.setVisibleKiosque(false);
		
		d.getPiecesJointes().add(pj);

		// When
		DemandeDto result = new DemandeDto(d, new AgentWithServiceDto(ag), false);
		result.updateEtat(etatDemande, agentEtat, d.getType().getGroupe());

		// Then

		assertEquals("RAYNAUD", result.getAgentWithServiceDto().getNom());
		assertEquals("Nicolas", result.getAgentWithServiceDto().getPrenom());
		assertEquals(9006765, (int) result.getAgentWithServiceDto().getIdAgent());

		assertNull(result.getDuree());
		assertEquals(9006765, (int) result.getAgentWithServiceDto().getIdAgent());
		assertEquals(1, (int) result.getIdDemande());
		assertEquals(7, (int) result.getIdTypeDemande());
		assertEquals(4, (int) result.getIdRefEtat());
		assertEquals(dateDemande, result.getDateSaisie());
		assertEquals(dateDemande, result.getDateFin());
		assertEquals(9005131, (int) result.getAgentEtat().getIdAgent());
		
		assertEquals(0, result.getPiecesJointes().size());
		
		result = new DemandeDto(d, new AgentWithServiceDto(ag), true);
		
		assertEquals(1, result.getPiecesJointes().size());

		pj.setVisibleKiosque(true);
		result = new DemandeDto(d, new AgentWithServiceDto(ag), false);
		
		assertEquals(1, result.getPiecesJointes().size());
	}
	
	@Test
	public void ctor_EtatDemandeCongesAnnuels() {
		
		EtatDemandeCongesAnnuels etatCA = new EtatDemandeCongesAnnuels();
		etatCA.setDemande(new Demande());
		etatCA.setDate(new DateTime(2015,7,1,0,0,0).toDate());
		etatCA.setDateDebut(new DateTime(2015,7,2,0,0,0).toDate());
		etatCA.setDateFin(new DateTime(2015,7,4,0,0,0).toDate());
		etatCA.setEtat(RefEtatEnum.APPROUVEE);
		etatCA.setNbSamediOffert(1.0);
		etatCA.setDuree(1.0);
		etatCA.setDureeAnneeN1(2.0);
		etatCA.setTotalJoursAnneeN1Old(2.0);
		etatCA.setTotalJoursOld(2.0);
		etatCA.setTotalJoursAnneeN1New(0.0);
		etatCA.setTotalJoursNew(1.0);
		etatCA.setCommentaire("commentaire jojo");
		etatCA.setDateDebutAM(true);
		etatCA.setDateDebutPM(false);
		etatCA.setDateFinAM(true);
		etatCA.setDateFinPM(false);
		
		DemandeDto result = new DemandeDto(etatCA);
		
		assertEquals(3, result.getDuree().intValue());
		assertEquals(etatCA.getDemande().getIdDemande(), result.getIdDemande());
		assertEquals(new DateTime(2015,7,1,0,0,0).toDate(), result.getDateSaisie());
		assertEquals(new DateTime(2015,7,2,0,0,0).toDate(), result.getDateDebut());
		assertEquals(new DateTime(2015,7,4,0,0,0).toDate(), result.getDateFin());
		assertEquals(etatCA.getCommentaire(), result.getCommentaire());
		assertTrue(result.isDateDebutAM());
		assertFalse(result.isDateDebutPM());
		assertTrue(result.isDateFinAM());
		assertFalse(result.isDateFinPM());
		assertTrue(result.isSamediOffert());
		assertEquals(etatCA.getTotalJoursAnneeN1New(), result.getTotalJoursAnneeN1New());
		assertEquals(etatCA.getTotalJoursAnneeN1Old(), result.getTotalJoursAnneeN1Old());
		assertEquals(etatCA.getTotalJoursOld(), result.getTotalJoursOld());
		assertEquals(etatCA.getTotalJoursNew(), result.getTotalJoursNew());
	}
}
