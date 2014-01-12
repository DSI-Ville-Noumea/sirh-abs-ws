package nc.noumea.mairie.abs.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import nc.noumea.mairie.abs.domain.DemandeRecup;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;

import org.joda.time.LocalDate;
import org.junit.Test;

public class HelperServiceTest {

	@Test
	public void isDateAMonday_DateIsMonday_ReturnTrue() {
		HelperService service = new HelperService();

		assertTrue(service.isDateAMonday(new LocalDate(2013, 7, 1).toDate()));
	}

	@Test
	public void isDateAMonday_DateIsNotAMonday_ReturnFalse() {
		HelperService service = new HelperService();

		assertFalse(service.isDateAMonday(new LocalDate(2013, 7, 2).toDate()));
	}
	
	@Test
	public void getDateFin_resultOk(){
		
		HelperService service = new HelperService();
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		java.util.Date dateFinAttendue = new java.util.Date();
	    java.util.Date dateDebut = new java.util.Date();
	    try {
	    	dateDebut = sdf.parse("2013-12-17 10:00:00");
	    	dateFinAttendue = sdf.parse("2013-12-17 10:22:00");
	    } catch (ParseException pe){
	        pe.printStackTrace();
	    }
	    
	    Date result = service.getDateFin(dateDebut, 22);
	    
	    assertEquals(result, dateFinAttendue);
	}
	
	@Test
	public void getDateFin_resultKo(){
		
		HelperService service = new HelperService();
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		java.util.Date dateFinAttendue = new java.util.Date();
	    java.util.Date dateDebut = new java.util.Date();
	    try {
	    	dateDebut = sdf.parse("2013-12-17 10:00:00");
	    	dateFinAttendue = sdf.parse("2013-12-17 10:22:00");
	    } catch (ParseException pe){
	        pe.printStackTrace();
	    }
	    
	    Date result = service.getDateFin(dateDebut, 21);
	    
	    assertNotEquals(result, dateFinAttendue);
	}
	
	@Test
	public void calculMinutesCompteur_etatApprouve() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());

		DemandeRecup demande = new DemandeRecup();
		demande.setDuree(10);

		HelperService service = new HelperService();

		int minutes = service.calculMinutesCompteur(demandeEtatChangeDto, demande);

		assertEquals(-10, minutes);
	}

	@Test
	public void calculMinutesCompteur_etatRefuse_and_etatPrcdApprouve() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.REFUSEE.getCodeEtat());

		EtatDemande etatDemande = new EtatDemande();
		etatDemande.setEtat(RefEtatEnum.APPROUVEE);

		DemandeRecup demande = new DemandeRecup();
		demande.setDuree(10);
		demande.addEtatDemande(etatDemande);

		HelperService service = new HelperService();

		int minutes = service.calculMinutesCompteur(demandeEtatChangeDto, demande);

		assertEquals(10, minutes);
	}

	@Test
	public void calculMinutesCompteur_etatRefuse_and_etatPrcdVisee() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.REFUSEE.getCodeEtat());

		EtatDemande etatDemande = new EtatDemande();
		etatDemande.setEtat(RefEtatEnum.VISEE_FAVORABLE);

		DemandeRecup demande = new DemandeRecup();
		demande.setDuree(10);
		demande.addEtatDemande(etatDemande);

		HelperService service = new HelperService();

		int minutes = service.calculMinutesCompteur(demandeEtatChangeDto, demande);

		assertEquals(0, minutes);
	}
}
