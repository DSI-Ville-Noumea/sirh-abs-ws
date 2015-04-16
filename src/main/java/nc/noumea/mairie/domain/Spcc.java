package nc.noumea.mairie.domain;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "SPCC")
@NamedQueries({ @NamedQuery(name = "getSpccByNomatrAndDateAndCode", query = "select spcc from Spcc spcc where spcc.id.nomatr = :nomatr and spcc.id.datjou = :dateFormatMairie and spcc.code = :code)"),
	@NamedQuery(name = "getSpccByNomatrAndDate", query = "select spcc from Spcc spcc where spcc.id.nomatr = :nomatr and spcc.id.datjou = :dateFormatMairie )")})
public class Spcc {

	@EmbeddedId
	private SpccId id;

	public Spcc() {
	}

	public Spcc(Integer nomatr, Integer datJou) {
		this.id = new SpccId(nomatr, datJou);
	}

	@NotNull
	@Column(name = "CODE", columnDefinition = "numeric")
	private Integer code;

	public SpccId getId() {
		return id;
	}

	public void setId(SpccId id) {
		this.id = id;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

}
