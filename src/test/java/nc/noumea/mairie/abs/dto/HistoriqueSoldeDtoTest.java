package nc.noumea.mairie.abs.dto;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import nc.noumea.mairie.abs.domain.AgentHistoAlimManuelle;
import nc.noumea.mairie.abs.domain.CongeAnnuelRestitutionMassive;
import nc.noumea.mairie.abs.domain.CongeAnnuelRestitutionMassiveHisto;
import nc.noumea.mairie.abs.domain.DemandeMaladies;
import nc.noumea.mairie.abs.domain.MotifCompteur;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;

import org.junit.Test;

public class HistoriqueSoldeDtoTest {

	@Test
	public void HistoriqueSoldeDto_constructAgentHistoAlimManuelle() {
		
		MotifCompteur motif = new MotifCompteur();
		motif.setLibelle("libelleMotif");
		motif.setRefTypeAbsence(new RefTypeAbsence());
		
		AgentHistoAlimManuelle histo = new AgentHistoAlimManuelle();
		histo.setDateModification(new Date());
		histo.setMotifTechnique("motifTechnique");
		histo.setMotifCompteur(motif);
		histo.setIdAgent(9005138);
		histo.setIdAgentConcerne(9005131);
		histo.setText("text");
		
		HistoriqueSoldeDto result = new HistoriqueSoldeDto(histo);
		assertEquals(histo.getDateModification(), result.getDateModifcation());
		assertEquals(motif.getLibelle(), result.getMotif().getLibelle());
		assertEquals(histo.getIdAgent(), result.getIdAgentModification());
		assertEquals(histo.getText(), result.getTextModification());
	}

	@Test
	public void HistoriqueSoldeDto_constructCongeAnnuelRestitutionMassiveHisto() {
		
		CongeAnnuelRestitutionMassive restitutionMassive = new CongeAnnuelRestitutionMassive();
		restitutionMassive.setDateModification(new Date());
		restitutionMassive.setDateRestitution(new Date());
		restitutionMassive.setMotif("motif");
		
		CongeAnnuelRestitutionMassiveHisto histo = new CongeAnnuelRestitutionMassiveHisto();
		histo.setRestitutionMassive(restitutionMassive);
		histo.setIdAgent(9005138);
		
		HistoriqueSoldeDto result = new HistoriqueSoldeDto(histo);
		assertEquals(restitutionMassive.getDateRestitution(), result.getDateModifcation());
		assertEquals(restitutionMassive.getMotif(), result.getMotif().getLibelle());
		assertEquals(histo.getIdAgent(), result.getIdAgentModification());
		assertEquals("Restitution massive de congés annuels", result.getTextModification());
	}

	@Test
	public void HistoriqueSoldeDto_constructDemandeMaladies() {
		
		RefTypeAbsence type = new RefTypeAbsence();
		type.setLabel("Maladie Convalescence");
		
		DemandeMaladies demande = new DemandeMaladies();
		demande.setDateDebut(new Date());
		demande.setDateFin(new Date());
		demande.setType(type);
		demande.setDuree(10.0);
		demande.setTotalPris(1);
		demande.setNombreJoursCoupeDemiSalaire(2);
		demande.setNombreJoursCoupePleinSalaire(3);
		demande.setNombreJoursResteAPrendreDemiSalaire(4);
		demande.setNombreJoursResteAPrendrePleinSalaire(5);
		
		HistoriqueSoldeDto result = new HistoriqueSoldeDto(demande);

		assertEquals(result.getDateModifcation(), demande.getDateDebut());
		assertEquals(result.getDateDebut(), demande.getDateDebut());
		assertEquals(result.getDateFin(), demande.getDateFin());
		assertEquals(result.getTypeAbsence(), demande.getType().getLabel());
		assertEquals(result.getDuree(), demande.getDuree());
		assertEquals(result.getTotalPris(), demande.getTotalPris());
		assertEquals(result.getNombreJoursCoupeDemiSalaire(), demande.getNombreJoursCoupeDemiSalaire());
		assertEquals(result.getNombreJoursCoupePleinSalaire(), demande.getNombreJoursCoupePleinSalaire());
		assertEquals(result.getNombreJoursResteAPrendreDemiSalaire(), demande.getNombreJoursResteAPrendreDemiSalaire());
		assertEquals(result.getNombreJoursResteAPrendrePleinSalaire(), demande.getNombreJoursResteAPrendrePleinSalaire());
	}
}
