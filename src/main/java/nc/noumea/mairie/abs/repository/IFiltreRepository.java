package nc.noumea.mairie.abs.repository;

import java.util.List;

import nc.noumea.mairie.abs.domain.RefEtat;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeSaisi;

public interface IFiltreRepository {

	<T> T getEntity(Class<T> Tclass, Object Id);

	List<RefEtat> findAllRefEtats();

	List<RefEtat> findRefEtatNonPris();

	List<RefEtat> findRefEtatEnCours();

	List<RefTypeAbsence> findAllRefTypeAbsences();

	List<RefTypeSaisi> findAllRefTypeSaisi();

	RefTypeSaisi findRefTypeSaisi(Integer idRefTypeAbsence);
}
