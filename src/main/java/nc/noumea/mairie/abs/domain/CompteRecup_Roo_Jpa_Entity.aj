// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package nc.noumea.mairie.abs.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;
import nc.noumea.mairie.abs.domain.CompteRecup;

privileged aspect CompteRecup_Roo_Jpa_Entity {
    
    declare @type: CompteRecup: @Entity;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long CompteRecup.id;
    
    @Version
    @Column(name = "version")
    private Integer CompteRecup.version;
    
    public Long CompteRecup.getId() {
        return this.id;
    }
    
    public void CompteRecup.setId(Long id) {
        this.id = id;
    }
    
    public Integer CompteRecup.getVersion() {
        return this.version;
    }
    
    public void CompteRecup.setVersion(Integer version) {
        this.version = version;
    }
    
}
