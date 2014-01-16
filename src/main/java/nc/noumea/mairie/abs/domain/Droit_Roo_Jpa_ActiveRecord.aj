// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package nc.noumea.mairie.abs.domain;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import nc.noumea.mairie.abs.domain.Droit;
import org.springframework.transaction.annotation.Transactional;

privileged aspect Droit_Roo_Jpa_ActiveRecord {
    
    @PersistenceContext(unitName = "absPersistenceUnit")
    transient EntityManager Droit.entityManager;
    
    public static final List<String> Droit.fieldNames4OrderClauseFilter = java.util.Arrays.asList("idDroit", "idAgent", "dateModification", "droitProfils", "droitDroitsAgent");
    
    public static final EntityManager Droit.entityManager() {
        EntityManager em = new Droit().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }
    
    public static long Droit.countDroits() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Droit o", Long.class).getSingleResult();
    }
    
    public static List<Droit> Droit.findAllDroits() {
        return entityManager().createQuery("SELECT o FROM Droit o", Droit.class).getResultList();
    }
    
    public static List<Droit> Droit.findAllDroits(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM Droit o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, Droit.class).getResultList();
    }
    
    public static Droit Droit.findDroit(Integer idDroit) {
        if (idDroit == null) return null;
        return entityManager().find(Droit.class, idDroit);
    }
    
    public static List<Droit> Droit.findDroitEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Droit o", Droit.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    public static List<Droit> Droit.findDroitEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM Droit o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, Droit.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    @Transactional
    public void Droit.persist() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }
    
    @Transactional
    public void Droit.remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            Droit attached = Droit.findDroit(this.idDroit);
            this.entityManager.remove(attached);
        }
    }
    
    @Transactional
    public void Droit.flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }
    
    @Transactional
    public void Droit.clear() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.clear();
    }
    
    @Transactional
    public Droit Droit.merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Droit merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
    
}
