// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package nc.noumea.mairie.abs.domain;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import nc.noumea.mairie.abs.domain.EtatDemande;
import org.springframework.transaction.annotation.Transactional;

privileged aspect EtatDemande_Roo_Jpa_ActiveRecord {
    
    @PersistenceContext(unitName = "absPersistenceUnit")
    transient EntityManager EtatDemande.entityManager;
    
    public static final EntityManager EtatDemande.entityManager() {
        EntityManager em = new EtatDemande().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }
    
    public static long EtatDemande.countEtatDemandes() {
        return entityManager().createQuery("SELECT COUNT(o) FROM EtatDemande o", Long.class).getSingleResult();
    }
    
    public static List<EtatDemande> EtatDemande.findAllEtatDemandes() {
        return entityManager().createQuery("SELECT o FROM EtatDemande o", EtatDemande.class).getResultList();
    }
    
    public static EtatDemande EtatDemande.findEtatDemande(Integer idEtatDemande) {
        if (idEtatDemande == null) return null;
        return entityManager().find(EtatDemande.class, idEtatDemande);
    }
    
    public static List<EtatDemande> EtatDemande.findEtatDemandeEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM EtatDemande o", EtatDemande.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    @Transactional
    public void EtatDemande.persist() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }
    
    @Transactional
    public void EtatDemande.remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            EtatDemande attached = EtatDemande.findEtatDemande(this.idEtatDemande);
            this.entityManager.remove(attached);
        }
    }
    
    @Transactional
    public void EtatDemande.flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }
    
    @Transactional
    public void EtatDemande.clear() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.clear();
    }
    
    @Transactional
    public EtatDemande EtatDemande.merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        EtatDemande merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
    
}