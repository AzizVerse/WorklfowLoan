package com.mybank.service;

import com.mybank.model.Agency;
import com.mybank.model.User;

import jakarta.ejb.Stateless;
import jakarta.persistence.*;

import java.util.List;

@Stateless
public class AgencyService {

    @PersistenceContext(unitName = "mybankPU")
    private EntityManager em;

    public void createAgency(Agency agency) {
        em.persist(agency);
    }

    public Agency findByCode(String code) {
        try {
            return em.createQuery("SELECT a FROM Agency a WHERE a.code = :code", Agency.class)
                     .setParameter("code", code)
                     .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Agency findById(Long id) {
        return em.find(Agency.class, id);
    }

    public List<Agency> findAllAgencies() {
        return em.createQuery("SELECT a FROM Agency a", Agency.class).getResultList();
    }
    
    public void updateAgency(Agency agency) {
        em.merge(agency);
    }
    public List<Agency> findAgenciesByResponsableSuccursale(User user) {
        return em.createQuery("SELECT a FROM Agency a WHERE a.responsableSuccursale = :user", Agency.class)
                 .setParameter("user", user)
                 .getResultList();
    }
    
    

}
