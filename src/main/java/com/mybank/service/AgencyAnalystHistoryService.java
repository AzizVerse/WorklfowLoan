package com.mybank.service;

import java.util.List;

import com.mybank.model.Agency;
import com.mybank.model.User;
import com.mybank.model.AgencyAnalystHistor;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;


@Stateless
public class AgencyAnalystHistoryService {

    @PersistenceContext(unitName = "mybankPU")
    private EntityManager em;

    public void logAnalystChange(Agency agency, User oldAnalyst, User newAnalyst) {
        AgencyAnalystHistor history = new AgencyAnalystHistor();
        history.setAgency(agency);
        history.setOldAnalyst(oldAnalyst);
        history.setNewAnalyst(newAnalyst);
        

        em.persist(history);
    }
    public List<AgencyAnalystHistor> getHistoryByAgency(Agency agency) {
        return em.createQuery("SELECT h FROM AgencyAnalystHistor h WHERE h.agency = :agency ORDER BY h.id DESC", AgencyAnalystHistor.class)
                 .setParameter("agency", agency)
                 .getResultList();
    }

}
