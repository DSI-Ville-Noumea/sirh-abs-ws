package nc.noumea.mairie.abs.transformer;

import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nc.noumea.mairie.abs.domain.Demande;

/**
 * Sorts Demandes by dateDebut field : the oldest Demandes will appear first.
 * The oldest ones will stand at the end of the list.
 * 
 * @author teo
 *
 */
public class DemandeComparator implements Comparator<Demande> {
	
	Logger logger = LoggerFactory.getLogger(DemandeComparator.class);

	@Override
	public int compare(Demande arg0, Demande arg1) {
		
		if (arg0.getDateDebut() == null || arg1.getDateDebut() == null) {
			logger.warn("Can not order Demandes by 'dateDebut' field. One of those dates is null !");
			return 0;
		}
		
		if (arg0.getDateDebut().after(arg1.getDateDebut()))
			return 1;
		else if (arg1.getDateDebut().after(arg0.getDateDebut()))
			return -1;
		else
			return 0;
	}
}
