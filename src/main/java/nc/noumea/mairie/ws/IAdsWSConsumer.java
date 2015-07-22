package nc.noumea.mairie.ws;

import java.util.List;

import nc.noumea.mairie.abs.dto.EntiteDto;
import nc.noumea.mairie.abs.dto.ReferenceDto;

public interface IAdsWSConsumer {

	EntiteDto getEntiteWithChildrenByIdEntite(Integer idEntite);

	EntiteDto getDirection(Integer idEntite);

	List<ReferenceDto> getListTypeEntite();

	EntiteDto getParentOfEntiteByTypeEntite(Integer idEntite, Integer idTypeEntite);
}
