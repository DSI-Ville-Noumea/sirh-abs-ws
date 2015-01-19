package nc.noumea.mairie.abs.service.impl;

import nc.noumea.mairie.abs.service.AgentMatriculeConverterServiceException;
import nc.noumea.mairie.abs.service.IAgentMatriculeConverterService;

import org.springframework.stereotype.Service;

@Service
public class AgentMatriculeConverterService implements IAgentMatriculeConverterService {

	@Override
	public Integer fromADIdAgentToSIRHIdAgent(Integer adIdAgent) throws AgentMatriculeConverterServiceException {

		if (adIdAgent.toString().length() != 6)
			throw new AgentMatriculeConverterServiceException(String.format(
					"Impossible de convertir le matricule '%d' en matricule SIRH.", adIdAgent));

		return addMissingDigit(adIdAgent);
	}

	@Override
	public Integer tryConvertFromADIdAgentToSIRHIdAgent(Integer adIdAgent) {

		if (adIdAgent == null)
			return adIdAgent;

		if(adIdAgent.toString().length() == 4)
			return convertNoMatrToIdAgent(adIdAgent);
		
		if(adIdAgent.toString().length() == 6)
			return addMissingDigit(adIdAgent);
		
		return adIdAgent;
	}

	private Integer addMissingDigit(Integer adIdAgent) {

		StringBuilder newIdSb = new StringBuilder();
		newIdSb.append(adIdAgent.toString().substring(0, 2));
		newIdSb.append("0");
		newIdSb.append(adIdAgent.toString().substring(2, 6));

		return Integer.parseInt(newIdSb.toString());
	}
	
	private Integer convertNoMatrToIdAgent(Integer noMatr) {
		if(noMatr.toString().length() == 4) {
			return 9000000 + noMatr;
		}
		return noMatr;
	}

	@Override
	public Integer fromIdAgentToSIRHNomatrAgent(Integer idAgent) throws AgentMatriculeConverterServiceException {

		if (idAgent.toString().length() != 7)
			throw new AgentMatriculeConverterServiceException(String.format(
					"Impossible de convertir l'idAgent '%d' en matricule MAIRIE.", idAgent));

		return removeDigit(idAgent);
	}

	private Integer removeDigit(Integer idAgent) {

		StringBuilder newIdSb = new StringBuilder();
		newIdSb.append(idAgent.toString().substring(3, 7));

		return Integer.parseInt(newIdSb.toString());
	}

}
