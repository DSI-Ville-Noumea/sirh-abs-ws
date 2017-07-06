package nc.noumea.mairie.abs.service.impl;

import java.util.ArrayList;
import java.util.List;

import nc.noumea.mairie.abs.domain.RefTypeGroupeAbsenceEnum;
import nc.noumea.mairie.abs.dto.EmailInfoDto;
import nc.noumea.mairie.abs.repository.IDemandeRepository;
import nc.noumea.mairie.abs.service.IEmailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailService implements IEmailService {

	@Autowired
	private IDemandeRepository demandeRepository;

	@Override
	@Transactional(readOnly = true)
	public EmailInfoDto getListIdDestinatairesEmailInfo() {

		EmailInfoDto dto = new EmailInfoDto();

		List<Integer> listeTypes = new ArrayList<Integer>();
		listeTypes.add(RefTypeGroupeAbsenceEnum.RECUP.getValue());
		listeTypes.add(RefTypeGroupeAbsenceEnum.REPOS_COMP.getValue());
		listeTypes.add(RefTypeGroupeAbsenceEnum.AS.getValue());
		listeTypes.add(RefTypeGroupeAbsenceEnum.CONGES_EXCEP.getValue());
		listeTypes.add(RefTypeGroupeAbsenceEnum.CONGES_ANNUELS.getValue());

		dto.setListViseurs(demandeRepository.getListViseursDemandesSaisiesJourDonne(listeTypes));

		dto.setListApprobateurs(demandeRepository.getListApprobateursDemandesSaisiesViseesJourDonne(listeTypes));

		return dto;
	}

	@Override
	@Transactional(readOnly = true)
	public EmailInfoDto getListIdApprobateursEmailMaladie() {

		EmailInfoDto dto = new EmailInfoDto();

		List<Integer> listeTypes = new ArrayList<Integer>();
		listeTypes.add(RefTypeGroupeAbsenceEnum.MALADIES.getValue());

		dto.setListApprobateurs(demandeRepository.getListApprobateursMaladiesSaisiesViseesVeille(listeTypes));

		return dto;
	}

}
