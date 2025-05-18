/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sire.ws.service;

import com.sire.entities.PryProyecto;
import com.sire.entities.PryProyectoPK;
import com.sire.entities.PrySubproyecto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import org.sql2o.Query;
import org.sql2o.Sql2o;

/**
 *
 * @author publio
 */
@Stateless
@Path("com.sire.entities.pryproyecto")
public class PryProyectoFacadeREST extends AbstractFacade<PryProyecto> {

    @PersistenceContext(unitName = "com.sire_SIRE-WS_war_1.0.0PU")
    private EntityManager em;

    private PryProyectoPK getPrimaryKey(PathSegment pathSegment) {
        /*
         * pathSemgent represents a URI path segment and any associated matrix parameters.
         * URI path part is supposed to be in form of 'somePath;codEmpresa=codEmpresaValue;codProyecto=codProyectoValue'.
         * Here 'somePath' is a result of getPath() method invocation and
         * it is ignored in the following code.
         * Matrix parameters are used as field names to build a primary key instance.
         */
        com.sire.entities.PryProyectoPK key = new com.sire.entities.PryProyectoPK();
        javax.ws.rs.core.MultivaluedMap<String, String> map = pathSegment.getMatrixParameters();
        java.util.List<String> codEmpresa = map.get("codEmpresa");
        if (codEmpresa != null && !codEmpresa.isEmpty()) {
            key.setCodEmpresa(codEmpresa.get(0));
        }
        java.util.List<String> codProyecto = map.get("codProyecto");
        if (codProyecto != null && !codProyecto.isEmpty()) {
            key.setCodProyecto(new java.lang.Integer(codProyecto.get(0)));
        }
        return key;
    }

    public PryProyectoFacadeREST() {
        super(PryProyecto.class);
    }

    @POST
    @Override
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void create(PryProyecto entity) {
        super.create(entity);
    }

    @PUT
    @Path("{id}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void edit(@PathParam("id") PathSegment id, PryProyecto entity) {
        super.edit(entity);
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") PathSegment id) {
        com.sire.entities.PryProyectoPK key = getPrimaryKey(id);
        super.remove(super.find(key));
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public PryProyecto find(@PathParam("id") PathSegment id) {
        com.sire.entities.PryProyectoPK key = getPrimaryKey(id);
        return super.find(key);
    }

    @GET
    @Override
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<PryProyecto> findAll() {
        return super.findAll();
    }

    @GET
    @Path("{from}/{to}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<PryProyecto> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        return super.findRange(new int[]{from, to});
    }

    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public String countREST() {
        return String.valueOf(super.count());
    }

    @GET
    @Path("empresa/{codEmpresa}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<PryProyecto> findByCodEmpresa(@PathParam("codEmpresa") String codEmpresa) {
        TypedQuery<PryProyecto> query = em.createNamedQuery("PryProyecto.findByCodEmpresa", PryProyecto.class);
        query.setParameter("codEmpresa", codEmpresa);
        query.setParameter("estadoProyecto", 'A');

        List<PryProyecto> tmpPryProyectos = new ArrayList<>();

        for (PryProyecto pryProyecto : query.getResultList()) {
            tmpPryProyectos.add(new PryProyecto(
                    pryProyecto.getPryProyectoPK().getCodEmpresa(),
                    pryProyecto.getPryProyectoPK().getCodProyecto()
                    )
            );
        }

        try {
            return tmpPryProyectos;
        } catch (Exception ex) {
            return null;
        }
    }

    @GET
    @Path("proyecto/{codProyecto}/empresa/{codEmpresa}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<PrySubproyecto> findSubByCodProyectoCodEmpresa(@PathParam("codProyecto") String codProyecto, @PathParam("codEmpresa") String codEmpresa) {
        Sql2o slSql2o = new Sql2o("jdbc/sire");
        Map<String, String> colMaps = new HashMap<>();
        colMaps.put("COD_EMPRESA", "codEmpresa");
        colMaps.put("COD_PROYECTO", "codProyecto");
        colMaps.put("COD_SUBPROYECTO", "codSubproyecto");
        colMaps.put("COD_LINEA_NEGOCIO", "codLineaNegocio");
        colMaps.put("DESC_SUBPROYECTO", "descSubproyecto");
        colMaps.put("NOMBRE_USUARIO", "nombreUsuario");
        colMaps.put("COD_SUPERVISOR", "codSupervisor");
        colMaps.put("ESTADO", "estado");
        colMaps.put("FECHA_ESTADO", "fechaEstado");

        slSql2o.setDefaultColumnMappings(colMaps);
        List resulset;
        try (Query query = slSql2o.createQuery("SELECT * FROM PRY_SUBPROYECTO WHERE COD_EMPRESA = :codEmpresa and COD_PROYECTO = :codProyecto")) {
            query.addParameter("codEmpresa", codEmpresa);
            query.addParameter("codProyecto", codProyecto);
            resulset = query.executeAndFetch(PrySubproyecto.class);
        }
        return resulset;
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

}
