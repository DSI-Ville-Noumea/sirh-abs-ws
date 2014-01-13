package nc.noumea.mairie.abs.service;

public interface IReportingService {

	byte[] getDemandeRecuperationReportAsByteArray(Integer idAgent, Integer idDemande) throws Exception;
}
