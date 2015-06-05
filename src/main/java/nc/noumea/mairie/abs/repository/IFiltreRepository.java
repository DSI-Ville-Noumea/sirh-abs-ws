package nc.noumea.mairie.abs.repository;

import java.util.List;

import nc.noumea.mairie.abs.domain.RefEtat;
import nc.noumea.mairie.abs.domain.RefGroupeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import nc.noumea.mairie.abs.domain.RefTypeSaisi;
import nc.noumea.mairie.abs.domain.RefUnitePeriodeQuota;

public interface IFiltreRepository {

	<T> T getEntity(Class<T> Tclass, Object Id);

	List<RefEtat> findAllRefEtats();

	List<RefEtat> findRefEtatNonPris();

	List<RefEtat> findRefEtatEnCours();

	List<RefTypeAbsence> findAllRefTypeAbsences();

	List<RefTypeSaisi> findAllRefTypeSaisi();

	RefTypeSaisi findRefTypeSaisi(Integer idRefTypeAbsence);

	List<RefGroupeAbsence> findAllRefGroupeAbsence();

	RefGroupeAbsence findRefGroupeAbsence(Integer idRefGroupeAbsence);

	List<RefUnitePeriodeQuota> findAllRefUnitePeriodeQuota();

	List<RefTypeAbsence> findAllRefTypeAbsencesWithGroup(Integer idRefGroupeAbsence);

	List<RefEtat> findRefEtatAValider();

	List<RefEtat> findRefEtatPlanning();
}
