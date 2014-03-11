package nc.noumea.mairie.abs.service.impl;

import java.util.ArrayList;
import java.util.List;

import nc.noumea.mairie.abs.domain.RefTypeAbsenceEnum;
import nc.noumea.mairie.abs.dto.EmailInfoDto;
import nc.noumea.mairie.abs.repository.IDemandeRepository;
import nc.noumea.mairie.abs.service.IEmailService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailService implements IEmailService {

	private Logger logger = LoggerFactory.getLogger(EmailService.class);

	@Autowired
	private IDemandeRepository demandeRepository;

	@Override
	public EmailInfoDto getListIdDestinatairesEmailInfo() {

		EmailInfoDto dto = new EmailInfoDto();

		List<Integer> listeTypes = new ArrayList<Integer>();
		listeTypes.add(RefTypeAbsenceEnum.RECUP.getValue());
		listeTypes.add(RefTypeAbsenceEnum.REPOS_COMP.getValue());

		dto.setListViseurs(demandeRepository.getListViseursDemandesSaisiesJourDonne(listeTypes));

		dto.setListApprobateurs(demandeRepository.getListApprobateursDemandesSaisiesViseesJourDonne(listeTypes));

		return dto;
	}

}
