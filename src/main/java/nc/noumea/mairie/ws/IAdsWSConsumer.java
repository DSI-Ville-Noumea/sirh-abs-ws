package nc.noumea.mairie.ws;

import nc.noumea.mairie.abs.dto.EntiteDto;

public interface IAdsWSConsumer {

	EntiteDto getEntiteWithChildrenByIdEntite(Integer idEntite);

	EntiteDto getInfoSiservByIdEntite(Integer idEntite);

	EntiteDto getEntiteByIdEntite(Integer idEntite);

	EntiteDto getWholeTree();

	EntiteDto getEntiteByIdEntiteOptimiseWithWholeTree(Integer idEntite,
			EntiteDto root);

	EntiteDto getWholeTreeLight();
}
