package nc.noumea.mairie.abs.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ResultListDemandeDto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3286235189363458429L;

	private List<DemandeDto> listDemandesDto = new ArrayList<DemandeDto>();
	
	/**
	 * Indique si les resultats retournés sont tronqués/limités
	 */
	private boolean resultatsLimites;
	
	private String messageInfoResultatsLimites;
	
	/**
	 * Constructeur
	 */
	public ResultListDemandeDto() {
		
	}

	public List<DemandeDto> getListDemandesDto() {
		return listDemandesDto;
	}

	public void setListDemandesDto(List<DemandeDto> listDemandesDto) {
		this.listDemandesDto = listDemandesDto;
	}

	public boolean isResultatsLimites() {
		return resultatsLimites;
	}

	public void setResultatsLimites(boolean resultatsLimites) {
		this.resultatsLimites = resultatsLimites;
	}

	public String getMessageInfoResultatsLimites() {
		return messageInfoResultatsLimites;
	}

	public void setMessageInfoResultatsLimites(String messageInfoResultatsLimites) {
		this.messageInfoResultatsLimites = messageInfoResultatsLimites;
	}
	
}
