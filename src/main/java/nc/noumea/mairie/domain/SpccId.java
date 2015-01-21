package nc.noumea.mairie.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class SpccId implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	public SpccId() {
	}

	public SpccId(Integer nomatr, Integer datJou) {
		this.nomatr = nomatr;
		this.datjou = datJou;
	}

	@Column(name = "NOMATR", insertable = false, updatable = false, columnDefinition = "numeric")
	private Integer nomatr;

	@Column(name = "DATJOU", insertable = false, updatable = false, columnDefinition = "numeric")
	private Integer datjou;

	public Integer getNomatr() {
		return nomatr;
	}

	public void setNomatr(Integer nomatr) {
		this.nomatr = nomatr;
	}

	public Integer getDatjou() {
		return datjou;
	}

	public void setDatjou(Integer datjou) {
		this.datjou = datjou;
	}

}
