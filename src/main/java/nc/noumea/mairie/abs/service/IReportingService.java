package nc.noumea.mairie.abs.service;

public interface IReportingService {

	byte[] getDemandeReportAsByteArray(Integer idAgent, Integer idDemande) throws Exception;
}
