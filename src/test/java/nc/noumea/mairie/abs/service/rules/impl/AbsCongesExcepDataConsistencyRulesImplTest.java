package nc.noumea.mairie.abs.service.rules.impl;

import static org.junit.Assert.assertEquals;
import nc.noumea.mairie.abs.domain.DemandeCongesExceptionnels;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeSaisi;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

import org.junit.Test;
import org.springframework.mock.staticmock.MockStaticEntityMethods;

@MockStaticEntityMethods
public class AbsCongesExcepDataConsistencyRulesImplTest extends DefaultAbsenceDataConsistencyRulesImplTest {

	@Test
	public void testMethodeParenteHeritage() throws Throwable {
		
		checkChampMotifDemandeSaisi_ok_motifNonObligatoire();
		checkChampMotifDemandeSaisi_ok_motifSaisi();
		checkChampMotifDemandeSaisi_ko_motifNull();
		checkChampMotifDemandeSaisi_ko_motifVide();
		
		super.impl = new AbsCongesExcepDataConsistencyRulesImpl();
		super.allTest(impl);
	}
	
	@Test
	public void checkChampMotifDemandeSaisi_ok_motifNonObligatoire() {
		
		ReturnMessageDto srm = new ReturnMessageDto();
		
		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setMotif(false);
		
		RefTypeAbsence type = new RefTypeAbsence();
		type.setTypeSaisi(typeSaisi);
		
		DemandeCongesExceptionnels demande = new DemandeCongesExceptionnels();
		demande.setCommentaire(null);
		demande.setType(type);
		
		AbsCongesExcepDataConsistencyRulesImpl impl = new AbsCongesExcepDataConsistencyRulesImpl();
		srm = impl.checkChampMotifDemandeSaisi(srm, demande);
		
		assertEquals(0, srm.getErrors().size());
	}
	
	@Test
	public void checkChampMotifDemandeSaisi_ok_motifSaisi() {

		ReturnMessageDto srm = new ReturnMessageDto();
		
		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setMotif(true);
		
		RefTypeAbsence type = new RefTypeAbsence();
		type.setTypeSaisi(typeSaisi);
		
		DemandeCongesExceptionnels demande = new DemandeCongesExceptionnels();
		demande.setCommentaire("commentaire");
		demande.setType(type);
		
		AbsCongesExcepDataConsistencyRulesImpl impl = new AbsCongesExcepDataConsistencyRulesImpl();
		srm = impl.checkChampMotifDemandeSaisi(srm, demande);
		
		assertEquals(0, srm.getErrors().size());
	}
	
	@Test
	public void checkChampMotifDemandeSaisi_ko_motifNull() {
		
		ReturnMessageDto srm = new ReturnMessageDto();
		
		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setMotif(true);
		
		RefTypeAbsence type = new RefTypeAbsence();
		type.setTypeSaisi(typeSaisi);
		
		DemandeCongesExceptionnels demande = new DemandeCongesExceptionnels();
		demande.setCommentaire(null);
		demande.setType(type);
		
		AbsCongesExcepDataConsistencyRulesImpl impl = new AbsCongesExcepDataConsistencyRulesImpl();
		srm = impl.checkChampMotifDemandeSaisi(srm, demande);
		
		assertEquals(1, srm.getErrors().size());
		assertEquals(srm.getErrors().get(0), AbsCongesExcepDataConsistencyRulesImpl.CHAMP_COMMENTAIRE_OBLIGATOIRE);
	}
	
	@Test
	public void checkChampMotifDemandeSaisi_ko_motifVide() {
		
		ReturnMessageDto srm = new ReturnMessageDto();
		
		RefTypeSaisi typeSaisi = new RefTypeSaisi();
		typeSaisi.setMotif(true);
		
		RefTypeAbsence type = new RefTypeAbsence();
		type.setTypeSaisi(typeSaisi);
		
		DemandeCongesExceptionnels demande = new DemandeCongesExceptionnels();
		demande.setCommentaire("");
		demande.setType(type);
		
		AbsCongesExcepDataConsistencyRulesImpl impl = new AbsCongesExcepDataConsistencyRulesImpl();
		srm = impl.checkChampMotifDemandeSaisi(srm, demande);
		
		assertEquals(1, srm.getErrors().size());
		assertEquals(srm.getErrors().get(0), AbsCongesExcepDataConsistencyRulesImpl.CHAMP_COMMENTAIRE_OBLIGATOIRE);
	}
}
