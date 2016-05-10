package nc.noumea.mairie.abs.service;

import nc.noumea.mairie.abs.dto.EditionDemandeDto;

public interface ITitreDemandeReportingService {

	byte[] getTitreDemandeAsByteArray(EditionDemandeDto dto) throws Exception;
}
