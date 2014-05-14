package nc.noumea.mairie.abs.service.impl;

import java.util.ArrayList;
import java.util.List;

import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
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
		listeTypes.add(RefTypeAbsenceEnum.RECUP.getValue());
		listeTypes.add(RefTypeAbsenceEnum.REPOS_COMP.getValue());
		listeTypes.add(RefTypeAbsenceEnum.ASA_A48.getValue());
		listeTypes.add(RefTypeAbsenceEnum.ASA_A54.getValue());
		listeTypes.add(RefTypeAbsenceEnum.ASA_A55.getValue());
		listeTypes.add(RefTypeAbsenceEnum.ASA_A53.getValue());
		listeTypes.add(RefTypeAbsenceEnum.ASA_A52.getValue());
		listeTypes.add(RefTypeAbsenceEnum.ASA_A49.getValue());
		listeTypes.add(RefTypeAbsenceEnum.ASA_A50.getValue());

		dto.setListViseurs(demandeRepository.getListViseursDemandesSaisiesJourDonne(listeTypes));

		dto.setListApprobateurs(demandeRepository.getListApprobateursDemandesSaisiesViseesJourDonne(listeTypes));

		return dto;
	}

}
