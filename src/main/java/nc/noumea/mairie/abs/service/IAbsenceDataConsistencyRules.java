package nc.noumea.mairie.abs.service;

import java.util.Date;
import java.util.List;

import nc.noumea.mairie.abs.domain.Demande;
import nc.noumea.mairie.abs.domain.DroitsAgent;
import nc.noumea.mairie.abs.domain.RefEtat;
import nc.noumea.mairie.abs.domain.RefEtatEnum;
import nc.noumea.mairie.abs.domain.RefTypeSaisi;
import nc.noumea.mairie.abs.domain.RefTypeSaisiCongeAnnuel;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.dto.DemandeEtatChangeDto;
import nc.noumea.mairie.abs.dto.ReturnMessageDto;

public interface IAbsenceDataConsistencyRules {

	void processDataConsistencyDemande(ReturnMessageDto srm, Integer idAgent, Demande demande, Date dateLundi,
			boolean isProvenanceSIRH);

	ReturnMessageDto checkDateDebutInferieurDateFin(ReturnMessageDto srm, Date dateDebut, Date dateFin);

	ReturnMessageDto checkEtatsDemandeAcceptes(ReturnMessageDto srm, Demande demande,
			List<RefEtatEnum> listEtatsAcceptes);

	ReturnMessageDto checkEtatsDemandeAnnulee(ReturnMessageDto srm, Demande demande, List<RefEtatEnum> listEtatsAcceptes);

	ReturnMessageDto checkDemandeDejaSaisieSurMemePeriode(ReturnMessageDto srm, Demande demande);

	ReturnMessageDto checkAgentInactivity(ReturnMessageDto srm, Integer idAgent, Date dateLundi);

	ReturnMessageDto checkChampMotifPourEtatDonne(ReturnMessageDto srm, Integer etat, String motif);

	ReturnMessageDto verifDemandeExiste(Demande demande, ReturnMessageDto returnDto);

	List<DemandeDto> filtreDateAndEtatDemandeFromList(List<Demande> listeSansFiltre, List<RefEtat> etats,
			Date dateDemande);

	boolean checkDepassementCompteurAgent(DemandeDto demandeDto);

	boolean checkDepassementMultipleAgent(DemandeDto dto);

	DemandeDto filtreDroitOfDemande(Integer idAgentConnecte, DemandeDto demandeDto, List<DroitsAgent> listDroitAgent);

	DemandeDto filtreDroitOfDemandeSIRH(DemandeDto demandeDto);

	ReturnMessageDto checkSaisieKiosqueAutorisee(ReturnMessageDto srm, RefTypeSaisi typeSaisi, boolean isProvenanceSIRH);

	ReturnMessageDto checkSaisiNewTypeAbsence(RefTypeSaisi typeSaisi, RefTypeSaisiCongeAnnuel typeSaisiCongeAnnuel,
			ReturnMessageDto srm);

	ReturnMessageDto checkDepassementDroitsAcquis(ReturnMessageDto srm, Demande demande);

	void checkSamediOffertToujoursOk(DemandeEtatChangeDto demandeEtatChangeDto,
			Demande demande);
}
