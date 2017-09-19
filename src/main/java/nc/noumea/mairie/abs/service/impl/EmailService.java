package nc.noumea.mairie.abs.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nc.noumea.mairie.abs.domain.RefTypeGroupeAbsenceEnum;
import nc.noumea.mairie.abs.dto.AgentDto;
import nc.noumea.mairie.abs.dto.ApprobateurWithAgentDto;
import nc.noumea.mairie.abs.dto.EmailInfoDto;
import nc.noumea.mairie.abs.repository.IDemandeRepository;
import nc.noumea.mairie.abs.service.IEmailService;
import nc.noumea.mairie.ws.ISirhWSConsumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Service
public class EmailService implements IEmailService {

	@Autowired
	private IDemandeRepository demandeRepository;

	@Autowired
	private ISirhWSConsumer sirhWSConsumer;

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
		Map<Integer, List<AgentDto>> table = Maps.newHashMap();
		
		// Liste des agents ayant eu une maladie posée la veille.
		for (Integer idAgent : demandeRepository.getAllMaladiesSaisiesVeille()) {
			AgentDto agentDto = new AgentDto(sirhWSConsumer.getAgent(idAgent));
			
			// Liste des approbateurs associés à cet agent
			List<Integer> listApprobateurs = demandeRepository.getListApprobateursForAgent(idAgent);
			for (Integer idAppro : listApprobateurs) {
				if (!table.containsKey(idAppro)) {
					List<AgentDto> agentList = Lists.newArrayList();
					agentList.add(agentDto);
					table.put(idAppro, agentList);
				} else {
					List<AgentDto> agentExistants = table.get(idAppro);
					if (!agentExistants.contains(agentDto)) {
						table.get(idAppro).add(agentDto);
					}
				}
			}
		}

		List<ApprobateurWithAgentDto> listApprobateursWithAgent = Lists.newArrayList();
		
		// On boucle sur la map pour renvoyer les bons objets
		for (Map.Entry<Integer,List<AgentDto>> entry : table.entrySet()) {
			ApprobateurWithAgentDto approWithAgents = new ApprobateurWithAgentDto();
			
			approWithAgents.setIdApprobateur(entry.getKey());
			approWithAgents.setAgents(entry.getValue());
			
			listApprobateursWithAgent.add(approWithAgents);
		}
		
		dto.setListApprobateursWithAgents(listApprobateursWithAgent);

		return dto;
	}

}
