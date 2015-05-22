package nc.noumea.mairie.sirh.comparator;

import java.util.Comparator;

import nc.noumea.mairie.abs.dto.DemandeDto;

public class DemandeDtoComparator implements Comparator<DemandeDto> {

	@Override
	public int compare(DemandeDto o1, DemandeDto o2) {
		// tri par date
		// ajout du "0 -" pour trier en ordre decroissant
		return 0 - o1.getDateDebut().compareTo(o2.getDateDebut());
	}

}
