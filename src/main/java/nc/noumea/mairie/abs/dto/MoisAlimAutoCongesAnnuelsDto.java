package nc.noumea.mairie.abs.dto;

import java.util.Date;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

public class MoisAlimAutoCongesAnnuelsDto {

	@JsonSerialize(using = JsonDateSerializer.class)
	@JsonDeserialize(using = JsonDateDeserializer.class)
	private Date dateMois;

	public Date getDateMois() {
		return dateMois;
	}

	public void setDateMois(Date dateMois) {
		this.dateMois = dateMois;
	}

}
