package nc.noumea.mairie.abs.service.counter.impl;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.EtatDemande;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.service.impl.HelperService;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class AsaCounterServiceImplTest extends AbstractCounterServiceTest {

	protected AsaCounterServiceImpl service = new AsaCounterServiceImpl();
	
	@Test
	public void allTest() {
		
		calculJoursAlimAutoCompteur_etatValide();
		calculJoursAlimAutoCompteur_etatPrise_and_etatPrcdValide();
		calculJoursAlimAutoCompteur_etatAnnule_and_etatPrcdValide();
		calculJoursAlimAutoCompteur_etatRejete_and_etatPrcdApprouve();
		calculMinutesAlimAutoCompteur_etatValide();
		calculMinutesAlimAutoCompteur_etatPrise_and_etatPrcdValide();
		calculMinutesAlimAutoCompteur_etatAnnule_and_etatPrcdValide();
		calculMinutesAlimAutoCompteur_etatRejete_and_etatPrcdApprouve();
		
		super.allTest(service);
	}
	
	@Test
	public void calculJoursAlimAutoCompteur_etatValide() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
			demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());

		Demande demande = new Demande();
		
		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreJoursArrondiDemiJournee(Mockito.isA(Date.class), Mockito.isA(Date.class))).thenReturn(10.0);
	
		ReflectionTestUtils.setField(service, "helperService", helperService);

		Double result = service.calculJoursAlimAutoCompteur(demandeEtatChangeDto, demande, new Date(), new Date());

		assertEquals(result.floatValue(), -8,5);
	}

	@Test
	public void calculJoursAlimAutoCompteur_etatPrise_and_etatPrcdValide() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());

		EtatDemande etatDemande = new EtatDemande();
		etatDemande.setEtat(RefEtatEnum.PRISE);

		Demande demande = new Demande();
			demande.addEtatDemande(etatDemande);

		HelperService helperService = Mockito.mock(HelperService.class);
			Mockito.when(helperService.calculNombreJoursArrondiDemiJournee(Mockito.isA(Date.class), Mockito.isA(Date.class))).thenReturn(10.0);
		
		ReflectionTestUtils.setField(service, "helperService", helperService);

		Double result = service.calculJoursAlimAutoCompteur(demandeEtatChangeDto, demande, new Date(), new Date());

		assertEquals(result.floatValue(), 8,5);
	}
	
	@Test
	public void calculJoursAlimAutoCompteur_etatAnnule_and_etatPrcdValide() {
		
		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());

		EtatDemande etatDemande = new EtatDemande();
		etatDemande.setEtat(RefEtatEnum.VALIDEE);

		Demande demande = new Demande();
			demande.addEtatDemande(etatDemande);

		HelperService helperService = Mockito.mock(HelperService.class);
			Mockito.when(helperService.calculNombreJoursArrondiDemiJournee(Mockito.isA(Date.class), Mockito.isA(Date.class))).thenReturn(10.0);
		
		ReflectionTestUtils.setField(service, "helperService", helperService);

		Double result = service.calculJoursAlimAutoCompteur(demandeEtatChangeDto, demande, new Date(), new Date());

		assertEquals(result.floatValue(), 8,5);
	}

	@Test
	public void calculJoursAlimAutoCompteur_etatRejete_and_etatPrcdApprouve() {

		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.REJETE.getCodeEtat());

		EtatDemande etatDemande = new EtatDemande();
		etatDemande.setEtat(RefEtatEnum.APPROUVEE);

		Demande demande = new Demande();
			demande.addEtatDemande(etatDemande);

		HelperService helperService = Mockito.mock(HelperService.class);
			Mockito.when(helperService.calculNombreJoursArrondiDemiJournee(Mockito.isA(Date.class), Mockito.isA(Date.class))).thenReturn(10.0);
		
		ReflectionTestUtils.setField(service, "helperService", helperService);

		Double result = service.calculJoursAlimAutoCompteur(demandeEtatChangeDto, demande, new Date(), new Date());

		assertEquals(result.floatValue(), 0,0);
	}
	
	@Test
	public void calculMinutesAlimAutoCompteur_etatValide() {

		Date dateDebut = new Date();
		Date dateFin = new Date();
		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
			demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.VALIDEE.getCodeEtat());

		Demande demande = new Demande();
		
		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreMinutes(dateDebut, dateFin)).thenReturn(8);
	
		ReflectionTestUtils.setField(service, "helperService", helperService);

		int result = service.calculMinutesAlimAutoCompteur(demandeEtatChangeDto, demande, dateDebut, dateFin);

		assertEquals(result, -8);
	}

	@Test
	public void calculMinutesAlimAutoCompteur_etatPrise_and_etatPrcdValide() {

		Date dateDebut = new Date();
		Date dateFin = new Date();
		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());

		EtatDemande etatDemande = new EtatDemande();
		etatDemande.setEtat(RefEtatEnum.PRISE);

		Demande demande = new Demande();
			demande.addEtatDemande(etatDemande);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreMinutes(dateDebut, dateFin)).thenReturn(10);
		
		ReflectionTestUtils.setField(service, "helperService", helperService);

		int result = service.calculMinutesAlimAutoCompteur(demandeEtatChangeDto, demande, dateDebut, dateFin);

		assertEquals(result, 10);
	}
	
	@Test
	public void calculMinutesAlimAutoCompteur_etatAnnule_and_etatPrcdValide() {
		
		Date dateDebut = new Date();
		Date dateFin = new Date();
		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.ANNULEE.getCodeEtat());

		EtatDemande etatDemande = new EtatDemande();
		etatDemande.setEtat(RefEtatEnum.VALIDEE);

		Demande demande = new Demande();
			demande.addEtatDemande(etatDemande);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreMinutes(dateDebut, dateFin)).thenReturn(10);
		
		ReflectionTestUtils.setField(service, "helperService", helperService);

		int result = service.calculMinutesAlimAutoCompteur(demandeEtatChangeDto, demande, dateDebut, dateFin);

		assertEquals(result, 8,5);
	}

	@Test
	public void calculMinutesAlimAutoCompteur_etatRejete_and_etatPrcdApprouve() {

		Date dateDebut = new Date();
		Date dateFin = new Date();
		DemandeEtatChangeDto demandeEtatChangeDto = new DemandeEtatChangeDto();
		demandeEtatChangeDto.setIdRefEtat(RefEtatEnum.REJETE.getCodeEtat());

		EtatDemande etatDemande = new EtatDemande();
		etatDemande.setEtat(RefEtatEnum.APPROUVEE);

		Demande demande = new Demande();
			demande.addEtatDemande(etatDemande);

		HelperService helperService = Mockito.mock(HelperService.class);
		Mockito.when(helperService.calculNombreMinutes(dateDebut, dateFin)).thenReturn(10);
		
		ReflectionTestUtils.setField(service, "helperService", helperService);

		int result = service.calculMinutesAlimAutoCompteur(demandeEtatChangeDto, demande, dateDebut, dateFin);

		assertEquals(result, 0,0);
	}
}
