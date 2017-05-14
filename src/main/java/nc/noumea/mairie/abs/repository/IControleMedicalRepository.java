package nc.noumea.mairie.abs.repository;

import javax.persistence.FlushModeType;

import nc.noumea.mairie.abs.domain.ControleMedical;

public interface IControleMedicalRepository {

	void persistEntity(Object obj);

	void setFlushMode(FlushModeType flushMode);
	
	ControleMedical findByDemandeId(Integer Id);

	void clear();

	void flush();

	void removeEntity(Object obj);

}
