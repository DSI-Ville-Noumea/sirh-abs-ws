package nc.noumea.mairie.abs.service.counter.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.RefGroupeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeGroupeAbsenceEnum;
import nc.noumea.mairie.abs.dto.SoldeSpecifiqueDto;
import nc.noumea.mairie.abs.repository.ICongesExceptionnelsRepository;
import nc.noumea.mairie.abs.repository.IDemandeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("CongesExcepCounterServiceImpl")
public class CongesExcepCounterServiceImpl extends AbstractCounterService {
	
	@Autowired
	private ICongesExceptionnelsRepository congesExceptionnelsRepository;
	
	@Autowired
	private IDemandeRepository demandeRepository;
	
	@Override
	public List<SoldeSpecifiqueDto> getListAgentCounterByDate(Integer idAgent, Date dateDebut, Date dateFin) {
		
		List<SoldeSpecifiqueDto> result = new ArrayList<SoldeSpecifiqueDto>();
		
		RefGroupeAbsence groupeAbsence = demandeRepository.getEntity(RefGroupeAbsence.class, RefTypeGroupeAbsenceEnum.CONGES_EXCEP.getValue());
		
		if(null != groupeAbsence) {
			for(RefTypeAbsence typeAbsence : groupeAbsence.getListeTypeAbsence()) {
				
				Double dureeDejaPris = congesExceptionnelsRepository.countDureeByPeriodeAndTypeDemande(
						idAgent, dateDebut, dateFin, typeAbsence.getIdRefTypeAbsence(), null);
				
				if(0.0 != dureeDejaPris) {
					SoldeSpecifiqueDto dto = new SoldeSpecifiqueDto();
						dto.setLibelle(typeAbsence.getLabel());
						dto.setSolde(dureeDejaPris);
						dto.setUniteDecompte(typeAbsence.getTypeSaisi().getUniteDecompte());
					
					result.add(dto);
				}
			}
		}
		
		return result;
	}
}
