package nc.noumea.mairie.abs.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import nc.noumea.mairie.abs.dto.CompteurDto;

import org.joda.time.LocalDate;
import org.junit.Test;

public class HelperServiceTest {

	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
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
	public void calculMinutesAlimManuelleCompteur_ajoutMinutes() {
		
		CompteurDto compteurDto = new CompteurDto();
			compteurDto.setDureeAAjouter(10);
		
		HelperService service = new HelperService();
		int minutes = service.calculMinutesAlimManuelleCompteur(compteurDto);
		
		assertEquals(10, minutes);
	}
	
	@Test
	public void calculMinutesAlimManuelleCompteur_retireMinutes() {
		
		CompteurDto compteurDto = new CompteurDto();
			compteurDto.setDureeARetrancher(10);
		
		HelperService service = new HelperService();
		int minutes = service.calculMinutesAlimManuelleCompteur(compteurDto);
		
		assertEquals(-10, minutes);
	}
	
	@Test
	public void calculMinutesAlimManuelleCompteur_zero() {
		
		CompteurDto compteurDto = new CompteurDto();
		
		HelperService service = new HelperService();
		int minutes = service.calculMinutesAlimManuelleCompteur(compteurDto);
		
		assertEquals(0, minutes);
	}
	
	@Test
	public void getCurrentDateMoinsUnAn() {
		
		GregorianCalendar calStr1 = new GregorianCalendar(); 
			calStr1.setTime(new Date()); 
			calStr1.add(GregorianCalendar.YEAR, -1);
		
		HelperService service = new HelperService();
		Date result = service.getCurrentDateMoinsUnAn();
		
		assertEquals(sdf.format(result), sdf.format(calStr1.getTime()));
	}
}
