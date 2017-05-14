package nc.noumea.mairie.abs.dto;

import static org.junit.Assert.assertEquals;
import nc.noumea.mairie.abs.domain.RefGroupeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeSaisi;
import nc.noumea.mairie.abs.domain.RefUnitePeriodeQuota;

import org.junit.Test;

public class RefTypeSaisiDtoTest {
	
	@Test
	public void ctor_withUnitePeriodeQuotaDto() {
		
		RefTypeSaisi typeSaisi = new RefTypeSaisi();
			typeSaisi.setAlerte(true);
			typeSaisi.setCalendarDateDebut(false);
			typeSaisi.setCalendarDateFin(true);
			typeSaisi.setCalendarHeureDebut(false);
			typeSaisi.setCalendarHeureFin(true);
			typeSaisi.setChkDateDebut(false);
			typeSaisi.setChkDateFin(true);
			typeSaisi.setCompteurCollectif(false);
			typeSaisi.setContractuel(true);
			typeSaisi.setConventionCollective(false);
			typeSaisi.setDescription("description");
			typeSaisi.setDuree(true);
			typeSaisi.setFonctionnaire(false);
			typeSaisi.setInfosComplementaires("infosComplementaires");
			typeSaisi.setMessageAlerte("messageAlerte");
			typeSaisi.setPieceJointe(true);
			typeSaisi.setQuotaMax(13);
			typeSaisi.setSaisieKiosque(false);
			typeSaisi.setUniteDecompte("uniteDecompte");
			
			// MALADIES
			typeSaisi.setPrescripteur(true);
			typeSaisi.setDateDeclaration(false);
			typeSaisi.setProlongation(true);
			typeSaisi.setNomEnfant(false);
			typeSaisi.setNombreITT(true);
			typeSaisi.setSiegeLesion(false);
			typeSaisi.setAtReference(true);
			typeSaisi.setMaladiePro(false);
			
		RefUnitePeriodeQuota refUnitePeriodeQuota = new RefUnitePeriodeQuota();
		
		RefGroupeAbsence groupe = new RefGroupeAbsence();
			groupe.setCode("groupe");
	
		RefTypeAbsence ref = new RefTypeAbsence();
			ref.setIdRefTypeAbsence(12);
			ref.setLabel("test lib");
			ref.setGroupe(groupe);
		
		typeSaisi.setRefUnitePeriodeQuota(refUnitePeriodeQuota);
		typeSaisi.setType(ref);
		
		RefTypeSaisiDto result = new RefTypeSaisiDto(typeSaisi);
		
		assertEquals(typeSaisi.isAlerte(), result.isAlerte());
		assertEquals(typeSaisi.isCalendarDateDebut(), result.isCalendarDateDebut());
		assertEquals(typeSaisi.isCalendarDateFin(), result.isCalendarDateFin());
		assertEquals(typeSaisi.isCalendarHeureDebut(), result.isCalendarHeureDebut());
		assertEquals(typeSaisi.isCalendarHeureFin(), result.isCalendarHeureFin());
		assertEquals(typeSaisi.isChkDateDebut(), result.isChkDateDebut());
		assertEquals(typeSaisi.isChkDateFin(), result.isChkDateFin());
		assertEquals(typeSaisi.isCompteurCollectif(), result.isCompteurCollectif());
		assertEquals(typeSaisi.isContractuel(), result.isContractuel());
		assertEquals(typeSaisi.isConventionCollective(), result.isConventionCollective());
		assertEquals(typeSaisi.getDescription(), result.getDescription());
		assertEquals(typeSaisi.isDuree(), result.isDuree());
		assertEquals(typeSaisi.isFonctionnaire(), result.isFonctionnaire());
		assertEquals(typeSaisi.getType().getIdRefTypeAbsence(), result.getIdRefTypeDemande());
		assertEquals(typeSaisi.getInfosComplementaires(), result.getInfosComplementaires());
		assertEquals(typeSaisi.getMessageAlerte(), result.getMessageAlerte());
		assertEquals(typeSaisi.isPieceJointe(), result.isPieceJointe());
		assertEquals(typeSaisi.getQuotaMax(), result.getQuotaMax());
		assertEquals(typeSaisi.isSaisieKiosque(), result.isSaisieKiosque());
		assertEquals(typeSaisi.getUniteDecompte(), result.getUniteDecompte());
		assertEquals(typeSaisi.getRefUnitePeriodeQuota().getIdRefUnitePeriodeQuota(), result.getUnitePeriodeQuotaDto().getIdRefUnitePeriodeQuota());
		assertEquals(typeSaisi.isPrescripteur(), result.isPrescripteur());
		assertEquals(typeSaisi.isDateDeclaration(), result.isDateDeclaration());
		assertEquals(typeSaisi.isProlongation(), result.isProlongation());
		assertEquals(typeSaisi.isNomEnfant(), result.isNomEnfant());
		assertEquals(typeSaisi.isNombreITT(), result.isNombreITT());
		assertEquals(typeSaisi.isSiegeLesion(), result.isSiegeLesion());
		assertEquals(typeSaisi.isAtReference(), result.isAtReference());
		assertEquals(typeSaisi.isMaladiePro(), result.isMaladiePro());
	}
}
