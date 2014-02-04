package nc.noumea.mairie.abs.service.counter.impl;

import static org.junit.Assert.assertEquals;
import nc.noumea.mairie.abs.domain.DemandeRecup;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.dto.CompteurDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

import org.junit.Test;

public class AbstractCounterServiceTest {

	@Test
	public void calculMinutesCompteur_etatApprouve() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat());

		DemandeRecup demande = new DemandeRecup();
		demande.setDuree(10);

		RecupCounterServiceImpl service = new RecupCounterServiceImpl();

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

		RecupCounterServiceImpl service = new RecupCounterServiceImpl();

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

		RecupCounterServiceImpl service = new RecupCounterServiceImpl();

		int minutes = service.calculMinutesCompteur(demandeEtatChangeDto, demande);

		assertEquals(0, minutes);
	}
	

	@Test
	public void controlSaisieAlimManuelleCompteur_minutesNonSaisie() {
		
		CompteurDto compteurDto = new CompteurDto();
		ReturnMessageDto result = new ReturnMessageDto();
		
		RecupCounterServiceImpl service = new RecupCounterServiceImpl();
		service.controlSaisieAlimManuelleCompteur(compteurDto, result);
		
		assertEquals(1, result.getErrors().size());
		assertEquals("La durée à ajouter ou retrancher n'est pas saisie.", result.getErrors().get(0).toString());
	}
	
	@Test
	public void controlSaisieAlimManuelleCompteur_minutesErreurSaisie() {
		
		CompteurDto compteurDto = new CompteurDto();
			compteurDto.setDureeAAjouter(1);
			compteurDto.setDureeARetrancher(1);
		ReturnMessageDto result = new ReturnMessageDto();
		
		RecupCounterServiceImpl service = new RecupCounterServiceImpl();
		service.controlSaisieAlimManuelleCompteur(compteurDto, result);
		
		assertEquals(1, result.getErrors().size());
		assertEquals("Un seul des champs Durée à ajouter ou Durée à retrancher doit être saisi.", result.getErrors().get(0).toString());
	}
	
	@Test
	public void controlSaisieAlimManuelleCompteur_ajoutOK() {
		
		CompteurDto compteurDto = new CompteurDto();
			compteurDto.setDureeAAjouter(1);
		ReturnMessageDto result = new ReturnMessageDto();
		
		RecupCounterServiceImpl service = new RecupCounterServiceImpl();
			service.controlSaisieAlimManuelleCompteur(compteurDto, result);
		
		assertEquals(0, result.getErrors().size());
	}
	
	@Test
	public void controlSaisieAlimManuelleCompteur_retireOK() {
		CompteurDto compteurDto = new CompteurDto();
			compteurDto.setDureeARetrancher(1);
		ReturnMessageDto result = new ReturnMessageDto();
		
		RecupCounterServiceImpl service = new RecupCounterServiceImpl();
		service.controlSaisieAlimManuelleCompteur(compteurDto, result);
		
		assertEquals(0, result.getErrors().size());
	}
}
