// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package nc.noumea.mairie.abs.domain;

import javax.persistence.Entity;
import javax.persistence.Table;
import nc.noumea.mairie.abs.domain.EtatDemande;

privileged aspect EtatDemande_Roo_Jpa_Entity {
    
    declare @type: EtatDemande: @Entity;
    
    declare @type: EtatDemande: @Table(name = "ABS_ETAT_DEMANDE");
    
}
