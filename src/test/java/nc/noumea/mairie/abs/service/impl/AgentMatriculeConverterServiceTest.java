package nc.noumea.mairie.abs.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import nc.noumea.mairie.abs.service.AgentMatriculeConverterServiceException;

import org.junit.Test;

public class AgentMatriculeConverterServiceTest {

	@Test
	public void testfromADIdAgentToEAEIdAgent_withIdNot5digits_throwException() {

		// Given
		int theIdToConvert = 89;
		AgentMatriculeConverterService service = new AgentMatriculeConverterService();

		try {
			// When
			service.fromADIdAgentToSIRHIdAgent(theIdToConvert);
		} catch (AgentMatriculeConverterServiceException ex) {
			// Then
			assertEquals("Impossible de convertir le matricule '89' en matricule SIRH.", ex.getMessage());
		}

	}

	@Test
	public void testfromADIdAgentToEAEIdAgent_withIdIs5digits_convertItTo6Digits()
			throws AgentMatriculeConverterServiceException {

		// Given
		int theIdToConvert = 906898;
		AgentMatriculeConverterService service = new AgentMatriculeConverterService();

		// When
		int result = service.fromADIdAgentToSIRHIdAgent(theIdToConvert);

		// Then
		assertEquals(9006898, result);
	}

	@Test
	public void testfromIdAgentToSIRHNomatrAgent_withIdNot7digits_throwException() {

		// Given
		int theIdToConvert = 89;
		AgentMatriculeConverterService service = new AgentMatriculeConverterService();

		try {
			// When
			service.fromIdAgentToSIRHNomatrAgent(theIdToConvert);
		} catch (AgentMatriculeConverterServiceException ex) {
			// Then
			assertEquals("Impossible de convertir l'idAgent '89' en matricule MAIRIE.", ex.getMessage());
		}

	}

	@Test
	public void testfromIdAgentToSIRHNomatrAgent_withIdIs7digits_convertItTo4Digits()
			throws AgentMatriculeConverterServiceException {

		// Given
		int theIdToConvert = 9006898;
		AgentMatriculeConverterService service = new AgentMatriculeConverterService();

		// When
		int result = service.fromIdAgentToSIRHNomatrAgent(theIdToConvert);

		// Then
		assertEquals(6898, result);
	}
	
	@Test
	public void tryConvertFromADIdAgentToSIRHIdAgent_idAgentNull() {
		
		Integer adIdAgent = null;
		
		AgentMatriculeConverterService service = new AgentMatriculeConverterService();
		assertNull(service.tryConvertFromADIdAgentToSIRHIdAgent(adIdAgent));
	}
	
	@Test
	public void tryConvertFromADIdAgentToSIRHIdAgent_NoMatr() {
		
		Integer adIdAgent = 5138;
		
		AgentMatriculeConverterService service = new AgentMatriculeConverterService();
		assertEquals(9005138, service.tryConvertFromADIdAgentToSIRHIdAgent(adIdAgent).intValue());
	}
	
	@Test
	public void tryConvertFromADIdAgentToSIRHIdAgent_idAgent() {
		
		Integer adIdAgent = 9005138;
		
		AgentMatriculeConverterService service = new AgentMatriculeConverterService();
		assertEquals(9005138, service.tryConvertFromADIdAgentToSIRHIdAgent(adIdAgent).intValue());
	}
	
	@Test
	public void tryConvertFromADIdAgentToSIRHIdAgent_905138() {
		
		Integer adIdAgent = 905138;
		
		AgentMatriculeConverterService service = new AgentMatriculeConverterService();
		assertEquals(9005138, service.tryConvertFromADIdAgentToSIRHIdAgent(adIdAgent).intValue());
	}
}
