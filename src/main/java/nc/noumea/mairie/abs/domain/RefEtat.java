package nc.noumea.mairie.abs.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord(persistenceUnit = "absPersistenceUnit", table = "ABS_REF_ETAT", versionField = "")
public class RefEtat {

	@Id
	@Column(name = "ID_REF_ETAT")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idRefEtat;

	@Column(name = "LABEL", columnDefinition = "NVARCHAR2")
	private String label;

	@Override
	public boolean equals(Object obj) {
		return idRefEtat.equals(((RefEtat) obj).getIdRefEtat());
	}

	public static List<RefEtat> findRefEtatNonPris() {
		List<RefEtat> res = new ArrayList<RefEtat>();
		res = RefEtat.findAllRefEtats();
		RefEtat etatPris = RefEtat.findRefEtat(RefEtatEnum.PRISE.getCodeEtat());
		res.remove(etatPris);
		return res;
	}

	public static List<RefEtat> findRefEtatEnCours() {
		List<RefEtat> res = new ArrayList<RefEtat>();
		res.add(RefEtat.findRefEtat(RefEtatEnum.SAISIE.getCodeEtat()));
		res.add(RefEtat.findRefEtat(RefEtatEnum.VISEE_FAVORABLE.getCodeEtat()));
		res.add(RefEtat.findRefEtat(RefEtatEnum.VISEE_DEFAVORABLE.getCodeEtat()));
		res.add(RefEtat.findRefEtat(RefEtatEnum.APPROUVEE.getCodeEtat()));
		return res;
	}
}
