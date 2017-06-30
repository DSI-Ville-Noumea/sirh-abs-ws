package nc.noumea.mairie.abs.dto;

/**
 * Ce DTO est consommé par le KiosqueRH. Lors de la creation d une demande, le
 * Kiosque a besoin de récupérer l ID de la demande, pour ensuite ajouter les
 * pièces jointes.
 * 
 * @author teo
 *
 */
public class ReturnMessageDemandeDto extends ReturnMessageDto {

	private Integer idDemande;
	
	public ReturnMessageDemandeDto() {
		
	}
	
	public ReturnMessageDemandeDto(ReturnMessageDto rmd) {
		this.setErrors(rmd.getErrors());
		this.setInfos(rmd.getInfos());
	}

	public Integer getIdDemande() {
		return idDemande;
	}

	public void setIdDemande(Integer idDemande) {
		this.idDemande = idDemande;
	}

}
