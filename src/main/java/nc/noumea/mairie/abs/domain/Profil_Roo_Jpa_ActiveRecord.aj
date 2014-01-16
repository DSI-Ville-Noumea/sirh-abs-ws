// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package nc.noumea.mairie.abs.domain;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import nc.noumea.mairie.abs.domain.Profil;
import org.springframework.transaction.annotation.Transactional;

privileged aspect Profil_Roo_Jpa_ActiveRecord {
    
    @PersistenceContext(unitName = "absPersistenceUnit")
    transient EntityManager Profil.entityManager;
    
    public static final List<String> Profil.fieldNames4OrderClauseFilter = java.util.Arrays.asList("idProfil", "libelle", "saisie", "modification", "suppression", "impression", "viserVisu", "viserModif", "approuverVisu", "approuverModif", "annuler", "visuSolde", "majSolde", "droitAcces");
    
    public static final EntityManager Profil.entityManager() {
        EntityManager em = new Profil().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }
    
    public static long Profil.countProfils() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Profil o", Long.class).getSingleResult();
    }
    
    public static List<Profil> Profil.findAllProfils() {
        return entityManager().createQuery("SELECT o FROM Profil o", Profil.class).getResultList();
    }
    
    public static List<Profil> Profil.findAllProfils(String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM Profil o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, Profil.class).getResultList();
    }
    
    public static Profil Profil.findProfil(Integer idProfil) {
        if (idProfil == null) return null;
        return entityManager().find(Profil.class, idProfil);
    }
    
    public static List<Profil> Profil.findProfilEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Profil o", Profil.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    public static List<Profil> Profil.findProfilEntries(int firstResult, int maxResults, String sortFieldName, String sortOrder) {
        String jpaQuery = "SELECT o FROM Profil o";
        if (fieldNames4OrderClauseFilter.contains(sortFieldName)) {
            jpaQuery = jpaQuery + " ORDER BY " + sortFieldName;
            if ("ASC".equalsIgnoreCase(sortOrder) || "DESC".equalsIgnoreCase(sortOrder)) {
                jpaQuery = jpaQuery + " " + sortOrder;
            }
        }
        return entityManager().createQuery(jpaQuery, Profil.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
    @Transactional
    public void Profil.persist() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }
    
    @Transactional
    public void Profil.remove() {
        if (this.entityManager == null) this.entityManager = entityManager();
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            Profil attached = Profil.findProfil(this.idProfil);
            this.entityManager.remove(attached);
        }
    }
    
    @Transactional
    public void Profil.flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }
    
    @Transactional
    public void Profil.clear() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.clear();
    }
    
    @Transactional
    public Profil Profil.merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        Profil merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
    
}
