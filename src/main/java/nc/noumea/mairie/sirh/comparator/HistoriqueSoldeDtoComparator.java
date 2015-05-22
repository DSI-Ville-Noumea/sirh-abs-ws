package nc.noumea.mairie.sirh.comparator;

import java.util.Comparator;

import nc.noumea.mairie.abs.dto.HistoriqueSoldeDto;

public class HistoriqueSoldeDtoComparator implements Comparator<HistoriqueSoldeDto> {

	@Override
	public int compare(HistoriqueSoldeDto o1, HistoriqueSoldeDto o2) {
		// tri par date
		// ajout du "0 -" pour trier en ordre decroissant
		return 0 - o1.getDateModifcation().compareTo(o2.getDateModifcation());
	}
}
