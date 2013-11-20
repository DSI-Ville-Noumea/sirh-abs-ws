package nc.noumea.mairie.abs.repository;

import java.util.List;

import nc.noumea.mairie.abs.domain.Droit;

public interface IAccessRightsRepository {

	List<Droit> getAgentAccessRights(int idAgent);
}
