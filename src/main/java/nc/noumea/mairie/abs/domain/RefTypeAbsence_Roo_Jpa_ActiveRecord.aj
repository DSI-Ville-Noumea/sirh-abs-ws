// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package nc.noumea.mairie.abs.domain;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import nc.noumea.mairie.abs.domain.RefTypeAbsence;
import org.springframework.transaction.annotation.Transactional;

privileged aspect RefTypeAbsence_Roo_Jpa_ActiveRecord {
    
    @PersistenceContext(unitName = "absPersistenceUnit")
    transient EntityManager RefTypeAbsence.entityManager;
    
    public static final List<String> RefTypeAbsence.fieldNames4OrderClauseFilter = java.util.Arrays.asList("idRefTypeAbsence", "label");
    
    public static final EntityManager RefTypeAbsence.entityManager() {
        EntityManager em = new RefTypeAbsence().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }
    
    public static long RefTypeAbsence.countRefTypeAbsences() {
        return entityManager().createQuery("SELECT COUNT(o) FROM RefTypeAbsence o", Long.class).getSingleResult();
    }
    
    public static List<RefTypeAbsence> RefTypeAbsence.findAllRefTypeAbsences() {
        return entityManager().createQuery("SELECT o FROM RefTypeAbsence o", RefTypeAbsence.class).getResultList();
    }
    
    public static List<RefTypeAbsence> RefTypeAbsence.findAllRefTypeAbsences(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM RefTypeAbsence o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, RefTypeAbsence.class).getResultList();
    }
    
    public static RefTypeAbsence RefTypeAbsence.findRefTypeAbsence(Integer idRefTypeAbsence) {
        if (idRefTypeAbsence == null) return null;
        return entityManager().find(RefTypeAbsence.class, idRefTypeAbsence);
    }
    
    public static List<RefTypeAbsence> RefTypeAbsence.findRefTypeAbsenceEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM RefTypeAbsence o", RefTypeAbsence.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    public static List<RefTypeAbsence> RefTypeAbsence.findRefTypeAbsenceEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM RefTypeAbsence o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, RefTypeAbsence.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    @Transactional
    public void RefTypeAbsence.persist() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }
    
    @Transactional
    public void RefTypeAbsence.remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            RefTypeAbsence attached = RefTypeAbsence.findRefTypeAbsence(this.idRefTypeAbsence);
            this.entityManager.remove(attached);
        }
    }
    
    @Transactional
    public void RefTypeAbsence.flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }
    
    @Transactional
    public void RefTypeAbsence.clear() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.clear();
    }
    
    @Transactional
    public RefTypeAbsence RefTypeAbsence.merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        RefTypeAbsence merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
    
}
