package nc.noumea.mairie.abs.service.multiThread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.RecursiveTask;

import nc.noumea.mairie.abs.domain.DroitsAgent;
import nc.noumea.mairie.abs.dto.DemandeDto;
import nc.noumea.mairie.abs.service.IAbsenceDataConsistencyRules;
import nc.noumea.mairie.abs.service.rules.impl.DataConsistencyRulesFactory;
import nc.noumea.mairie.abs.vo.CheckCompteurAgentVo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilisation du framework Fork / Join du package java.util.concurrent
 * 
 * Ici nous divisons le traitement en une liste de sous-taches (thread java) :
 *  - si la liste des demandes est superieure à SEUIL_MONO_THREAD (50)
 *  	alors on divise en NOMBRE_THREAD (4) sous taches
 *  
 *   -si la sous tache possede encore une liste de demande superieure à SEUIL_MONO_THREAD (50)
 *   	alors on redivise en une liste de sous-sous-tache
 * 
 * @author rebjo84 nicno85
 *
 */
public class DemandeRecursiveTask extends RecursiveTask<List<DemandeDto>> {

	private final static Logger logger = LoggerFactory.getLogger(DemandeRecursiveTask.class);
	/**
	 * 
	 */
	private static final long						serialVersionUID	= 5273902937393261386L;
	
	private final static int SEUIL_MONO_THREAD = 50;
	// nombre de thread par tache
	private final static int NOMBRE_THREAD = 4;
	
	private final static String ERROR_ATTRIBUT = "Erreur un des attributs listDemandeDto, idAgentConnecte ou listDroitAgent de la classe DemandeRecursiveTask n'est pas renseigné.";
	private final static String TASK_MONO_THREAD = "Lancement de la tâche en monothread car moins de " + SEUIL_MONO_THREAD + " demandes.";
	private final static String TASK_MULTI_THREAD = "Lancement de la tâche en multithread %s demandes divisé en " + NOMBRE_THREAD + "thread.";
	private final static String DECOUPE_TASK = "Création task avec %s demandes : index %s à index %s.";
	

	private HashMap<Integer, CheckCompteurAgentVo>	mapCheckCompteurAgentVo;
	private List<DemandeDto>						listDemandeDto;
	private List<DemandeDto>						results;
	private Integer									idAgentConnecte;
	private List<DroitsAgent>						listDroitAgent;
	private boolean									isAgent;

	private DataConsistencyRulesFactory				dataConsistencyRulesFactory;

	public DemandeRecursiveTask() {
	}

	public DemandeRecursiveTask(HashMap<Integer, CheckCompteurAgentVo> pMapCheckCompteurAgentVo, List<DemandeDto> pListDemandeDto,
			Integer pIdAgentConnecte, List<DroitsAgent> pListDroitsAgent, boolean pIsAgent) {
		this.listDemandeDto = pListDemandeDto;
		this.mapCheckCompteurAgentVo = pMapCheckCompteurAgentVo;
		this.idAgentConnecte = pIdAgentConnecte;
		this.listDroitAgent = pListDroitsAgent;
		this.results = new ArrayList<DemandeDto>();
		this.isAgent = pIsAgent;
		
		dataConsistencyRulesFactory = (DataConsistencyRulesFactory) SpringContext.getApplicationContext().getBean("dataConsistencyRulesFactory");
	}

	/**
	 * Méthode que nous allons utiliser pour les traitements en mode parallèle.
	 * 
	 * @throws ScanException
	 */
	public List<DemandeDto> parallelCheckDemandeDto() {

		if(null == listDemandeDto
				|| null == idAgentConnecte
				|| null == listDroitAgent) {
			logger.debug(ERROR_ATTRIBUT);
			return null;
		}
		
		if(listDemandeDto.size() < SEUIL_MONO_THREAD) {
			logger.debug(TASK_MONO_THREAD);
			return checkDemande(listDemandeDto);
		}
		
		// List d'objet qui contiendra les sous-tâches créées et lancées
		List<DemandeRecursiveTask> list = new ArrayList<DemandeRecursiveTask>();
		
		int tailleListeTask = (int) Math.ceil(new Double(listDemandeDto.size()) / NOMBRE_THREAD);
		
		logger.debug(String.format(TASK_MULTI_THREAD, listDemandeDto.size()));
		
		for(int i=0; i<NOMBRE_THREAD; i++) {
			int beginIndex = tailleListeTask*i;
			int endIndex = (tailleListeTask*(i+1)) > listDemandeDto.size() ? listDemandeDto.size() : tailleListeTask*(i+1);
			
			List<DemandeDto> listDemandeTask = listDemandeDto.subList(beginIndex, endIndex);
			logger.debug(String.format(DECOUPE_TASK, listDemandeTask.size(), beginIndex, endIndex));
			
			DemandeRecursiveTask task = new DemandeRecursiveTask(mapCheckCompteurAgentVo, listDemandeTask, idAgentConnecte, listDroitAgent, isAgent);
			list.add(task);
		}
		
		for (DemandeRecursiveTask task : list)
			task.fork();
		
		// Et, enfin, nous récupérons le résultat de toutes les tâches de fond
		for (DemandeRecursiveTask task : list)
			results.addAll(task.join());

		// Nous renvoyons le résultat final
		return results;
	}

	public List<DemandeDto> checkDemande(List<DemandeDto> listDemandeDto) {

		for (DemandeDto demandeDto : listDemandeDto) {

			IAbsenceDataConsistencyRules absenceDataConsistencyRulesImpl = dataConsistencyRulesFactory.getFactory(demandeDto.getGroupeAbsence()
					.getIdRefGroupeAbsence(), demandeDto.getIdTypeDemande());

			CheckCompteurAgentVo checkCompteurAgentVo = mapCheckCompteurAgentVo.get(demandeDto.getAgentWithServiceDto().getIdAgent());
			
			if (null == checkCompteurAgentVo)
				checkCompteurAgentVo = new CheckCompteurAgentVo();

			demandeDto = absenceDataConsistencyRulesImpl.filtreDroitOfDemande(idAgentConnecte, demandeDto, listDroitAgent, isAgent);
			demandeDto.setDepassementCompteur(absenceDataConsistencyRulesImpl.checkDepassementCompteurAgent(demandeDto, checkCompteurAgentVo));
			demandeDto.setDepassementMultiple(absenceDataConsistencyRulesImpl.checkDepassementMultipleAgent(demandeDto));

			if (mapCheckCompteurAgentVo.containsKey(demandeDto.getAgentWithServiceDto().getIdAgent()))
				mapCheckCompteurAgentVo.remove(demandeDto.getAgentWithServiceDto().getIdAgent());

			mapCheckCompteurAgentVo.put(demandeDto.getAgentWithServiceDto().getIdAgent(), checkCompteurAgentVo);
		}

		return listDemandeDto;
	}

	@Override
	protected List<DemandeDto> compute() {

		List<DemandeDto> result = new ArrayList<DemandeDto>();

		result = this.parallelCheckDemandeDto();

		return result;
	}

}
