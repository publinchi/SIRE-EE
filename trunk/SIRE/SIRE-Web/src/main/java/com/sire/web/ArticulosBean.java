/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sire.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sire.entities.CxcCliente;
import com.sire.entities.FacCatalogoPrecioD;
import com.sire.entities.FacDescVol;
import com.sire.entities.FacDescVolPK;
import com.sire.entities.FacParametros;
import com.sire.entities.FacTmpFactC;
import com.sire.entities.FacTmpFactCPK;
import com.sire.entities.FacTmpFactD;
import com.sire.entities.FacTmpFactDPK;
import com.sire.entities.GnrDivisa;
import com.sire.entities.GnrUsuarios;
import com.sire.entities.InvArticulo;
import com.sire.entities.InvBodegaArt;
import com.sire.entities.InvBodegaArtPK;
import com.sire.entities.InvInventario;
import com.sire.entities.InvIva;
import com.sire.entities.InvMovimientoCab;
import com.sire.entities.InvMovimientoCabPK;
import com.sire.entities.InvMovimientoDtll;
import com.sire.entities.InvMovimientoDtllPK;
import com.sire.entities.InvTransacciones;
import com.sire.entities.InvUnidadAlternativa;
import com.sire.entities.Pedido;
import com.sire.entities.VCliente;
import com.sire.exception.EmptyException;
import com.sire.rs.client.FacCatalogoPrecioDFacadeREST;
import com.sire.rs.client.FacDescVolFacadeREST;
import com.sire.rs.client.FacParametrosFacadeREST;
import com.sire.rs.client.GnrContadorDocFacadeREST;
import com.sire.rs.client.InvArticuloFacadeREST;
import com.sire.rs.client.InvIvaFacadeREST;
import com.sire.rs.client.InvMovimientoCabFacadeREST;
import com.sire.rs.client.InvUnidadAlternativaFacadeREST;
import com.sire.rs.client.VClienteFacadeREST;
import com.sire.utils.Round;
import com.sire.utils.bodega.BodegaUtil;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.ws.rs.ClientErrorException;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.mobile.event.SwipeEvent;

/**
 *
 * @author pestupinan
 */
@ManagedBean(name = "articulosBean")
@SessionScoped
@Getter
@Setter
public class ArticulosBean {

    private static final Logger logger = Logger.getLogger(ArticulosBean.class.getName());

    private final InvArticuloFacadeREST invArticuloFacadeREST;
    private final VClienteFacadeREST vClienteFacadeREST;
    private final FacDescVolFacadeREST facDescVolFacadeREST;
    private final InvMovimientoCabFacadeREST invMovimientoCabFacadeREST;
    private final InvIvaFacadeREST invIvaFacadeREST;
    private final InvUnidadAlternativaFacadeREST invUnidadAlternativaFacadeREST;
    private final FacParametrosFacadeREST facParametrosFacadeREST;
    private final GsonBuilder builder;
    private final Gson gson;
    private String input;
    private List<InvArticulo> articulos;
    private List<InvMovimientoDtll> invMovimientoDtlls;
    private FacTmpFactC facTmpFactC;
    private List<FacTmpFactD> facTmpFactDs;
    private InvMovimientoCab invMovimientoCab;
    private InvMovimientoDtll invMovimientoDtllSeleccionado;
    private String formaPago;

    //
    private Double existencia;
    private boolean agregarBloqueado = true;

    // Atributos de articulo a ser agregado a la lista
    private String codInventario;
    private int codArticulo;
    private InvArticulo invArticuloSeleccionado;
    private Double totalIVA;

    private FacCatalogoPrecioD facCatalogoPrecioD;

    @ManagedProperty(value = "#{user}")
    private UserManager userManager;
    @ManagedProperty("#{cliente}")
    private CustomerBean cliente;
    @ManagedProperty("#{customers}")
    private CustomersBean clientes;
    @ManagedProperty("#{mapa}")
    private MapaBean mapa;

    //Resumen
    Double subTotal, iva, total, totalSinIva, totalConIva;

    //Mensaje
    private String cantidadExcedida, colorCantidadExcedida = "black";

    public ArticulosBean() {
        codInventario = "01";
        facTmpFactC = new FacTmpFactC();
        facTmpFactDs = new ArrayList<>();
        facTmpFactC.setFacTmpFactDList(facTmpFactDs);
        invMovimientoDtlls = new ArrayList<>();
        invMovimientoCab = new InvMovimientoCab();
        invMovimientoCab.setInvMovimientoDtllList(invMovimientoDtlls);
        invArticuloFacadeREST = new InvArticuloFacadeREST();
        vClienteFacadeREST = new VClienteFacadeREST();
        facDescVolFacadeREST = new FacDescVolFacadeREST();
        invMovimientoCabFacadeREST = new InvMovimientoCabFacadeREST();
        invIvaFacadeREST = new InvIvaFacadeREST();
        invUnidadAlternativaFacadeREST = new InvUnidadAlternativaFacadeREST();
        facParametrosFacadeREST = new FacParametrosFacadeREST();
        builder = new GsonBuilder();
        gson = builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
    }

    // Medotos del negocio
    public void findArticulos() {
        logger.log(Level.INFO, "Invocando findArticulos: {0}", input);

        String articulosString;
        String codEmpresa = obtenerEmpresa();
        try {
            articulosString = invArticuloFacadeREST.findParaVenta(String.class, input.toUpperCase(Locale.ENGLISH).replace("%AC", "$WC"), codEmpresa);
            articulos = gson.fromJson(articulosString, new TypeToken<java.util.List<InvArticulo>>() {
            }.getType());
            logger.log(Level.INFO, "# articulos: {0}", articulos.size());
        } catch (ClientErrorException cee) {
            articulos = null;
        }
    }

    public void tapArticulo(SelectEvent event) {
        logger.log(Level.INFO, "\u00b7\u00b7 tapArticulo \u00b7\u00b7 {0}", event.getObject());

        invArticuloSeleccionado = ((InvArticulo) event.getObject());
        logger.log(Level.INFO, "Articulo seleccionado: {0}", invArticuloSeleccionado.getNombreArticulo());
        logger.log(Level.INFO, "codUnidad: {0}", invArticuloSeleccionado.getCodUnidad().getCodUnidad());
        setCodArticulo(invArticuloSeleccionado.getInvArticuloPK().getCodArticulo());

        InvMovimientoDtll invMovimientoDtll = new InvMovimientoDtll();

        // TODO cargar esta bodega
        InvBodegaArt invBodegaArt = new InvBodegaArt();
        InvBodegaArtPK invBodegaArtPK = new InvBodegaArtPK();

        invBodegaArtPK.setCodEmpresa(obtenerEmpresa());

        invBodegaArtPK.setCodArticulo(invArticuloSeleccionado.getInvArticuloPK().getCodArticulo());

        invBodegaArt.setInvBodegaArtPK(invBodegaArtPK);
        invBodegaArt.setExistencia(BigDecimal.ZERO);
        invBodegaArt.setExistPendEnt(BigDecimal.ZERO);
        invBodegaArt.setExistPendSal(BigDecimal.ZERO);
        InvMovimientoDtllPK invMovimientoDtllPK = new InvMovimientoDtllPK();
        invMovimientoDtllPK.setCodEmpresa(obtenerEmpresa());
        invMovimientoDtllPK.setCodDocumento("SAI");
        int count = invMovimientoDtlls.size() + 1;
        invMovimientoDtll.setPosicion(count);
        invMovimientoDtllPK.setNumLinea(Short.valueOf(String.valueOf(count)));
        invMovimientoDtllPK.setNumDocumento(0);

        invMovimientoDtll.setInvMovimientoDtllPK(invMovimientoDtllPK);

        invMovimientoDtll.setInvBodegaArt(invBodegaArt);
        invMovimientoDtll.setInvArticulo(invArticuloSeleccionado);
        invMovimientoDtll.setCodUnidad(invArticuloSeleccionado.getCodUnidad().getCodUnidad());

        if (invArticuloSeleccionado.getExistencia().doubleValue() > 0) {
            invMovimientoDtlls.add(invMovimientoDtll);
            input = null;
            articulos.clear();
            RequestContext.getCurrentInstance().update("pedido:accordionPanel:formArticles:buscar");
            RequestContext.getCurrentInstance().update("pedido:accordionPanel:formArticles:dataListArticulo");
        } else {
            addMessage("Advertencia", "Producto no disponible", FacesMessage.SEVERITY_INFO);
        }
    }

    public void tapArticuloFinal(SelectEvent event) {
        try {
            setInvMovimientoDtllSeleccionado(((InvMovimientoDtll) event.getObject()));
            InvMovimientoDtll invMovimientoDtll = obtenerMovimientoSeleccionado();

            String codArticle = String.valueOf(invMovimientoDtll.getInvBodegaArt().getInvBodegaArtPK().getCodArticulo());
            logger.log(Level.INFO, "Articulo seleccionado final: {0}", codArticle);

            String codEmpresa = obtenerEmpresa();

            StringBuilder id = new StringBuilder();

            id.append("find;codEmpresa=");
            id.append(codEmpresa);
            id.append(";codCatalogo=01;codArticulo=");
            id.append(codArticle);

            FacCatalogoPrecioDFacadeREST facCatalogoPrecioDFacadeREST = new FacCatalogoPrecioDFacadeREST();
            String response = facCatalogoPrecioDFacadeREST.find_JSON(String.class, id.toString());

            facCatalogoPrecioD = gson.fromJson(response, new TypeToken<FacCatalogoPrecioD>() {
            }.getType());

            invMovimientoDtll.setPrecioVenta(facCatalogoPrecioD.getPrecioVenta1());

            logger.log(Level.INFO, "invMovimientoDtll.getPrecioVenta(): {0}", invMovimientoDtll.getPrecioVenta());

            if (invArticuloSeleccionado.getDescuento() == null) {
                if (obtenerCliente() != null) {
                    invArticuloSeleccionado.setDescuento(buscarDescuento());
                }
            }

            invArticuloSeleccionado.setIva(findIva());

            loadPrecioUnitarioByUnidadMedida();
        } catch (EmptyException ex) {
            addMessage("Advertencia", ex.getMessage(), FacesMessage.SEVERITY_INFO);
        }
    }

    public void swipeleft(SwipeEvent event) {
        setInvMovimientoDtllSeleccionado(((InvMovimientoDtll) event.getData()));
        InvMovimientoDtll invMovimientoDtll = obtenerMovimientoSeleccionado();

        logger.log(Level.INFO, "Posicion: {0}", invMovimientoDtll.getPosicion());

        InvMovimientoDtll forDelete;
        for (InvMovimientoDtll invMovimientoDtll1 : invMovimientoDtlls) {
            if (invMovimientoDtll1.getPosicion() == invMovimientoDtll.getPosicion()) {
                forDelete = invMovimientoDtll1;
                invMovimientoDtlls.remove(forDelete);
                logger.log(Level.INFO, "Articulo removido: {0}", forDelete.getPosicion());

                break;
            }
        }

        if (invMovimientoDtll.getCantidad() != null) {
            calcularResumen();
        }
    }

    public void loadInventariosByBodega() {
        RequestContext.getCurrentInstance().update("pedido:accordionPanel:formArticulo:inv");
    }

    public void calcularTotalRegistro() {
        RequestContext.getCurrentInstance().update("pedido:accordionPanel:formArticulo:totalRegistro");
    }

    public void agregarArticulo() {
        InvMovimientoDtll movimientoSeleccionado = obtenerMovimientoSeleccionado();
        String codBodega = movimientoSeleccionado.getInvBodegaArt().getInvBodegaArtPK().getCodBodega();
        String codUnidad = movimientoSeleccionado.getCodUnidad();
        BigDecimal cantidad = movimientoSeleccionado.getCantidad();

        logger.info("Articulo a ser agregado: ");
        logger.log(Level.INFO, "codBodega: {0}", codBodega);
        logger.log(Level.INFO, "codInventario: {0}", codInventario);
        logger.log(Level.INFO, "codUnidad: {0}", codUnidad);
        logger.log(Level.INFO, "cantidad: {0}", cantidad);
        logger.log(Level.INFO, "precioUnitario: {0}", movimientoSeleccionado.getCostoUnitario());
        logger.log(Level.INFO, "descuento: {0}", invArticuloSeleccionado.getDescArticulo());
        logger.log(Level.INFO, "total: {0}", movimientoSeleccionado.getCostoTotal());
        logger.log(Level.INFO, "iva: {0}", invArticuloSeleccionado.getCodIva());
        logger.log(Level.INFO, "totalIVA: {0}", totalIVA);

        // codBodega
        movimientoSeleccionado.getInvBodegaArt().getInvBodegaArtPK().setCodBodega(codBodega);
        // codInventario
        movimientoSeleccionado.getInvBodegaArt().getInvBodegaArtPK().setCodInventario(codInventario);
        // codUnidad
        movimientoSeleccionado.setCodUnidad(codUnidad);
        // cantidad
        movimientoSeleccionado.setCantidad(cantidad);
        // descuento
        movimientoSeleccionado.setPorcDesc1(invArticuloSeleccionado.getDescuento());
        // estado
        movimientoSeleccionado.setEstado("G");
        // fecha_estado
        movimientoSeleccionado.setFechaEstado(Calendar.getInstance().getTime());
        // total
        movimientoSeleccionado.setCostoTotal(movimientoSeleccionado.getCostoTotal());
        // precio unitario
        movimientoSeleccionado.setCostoUnitario(movimientoSeleccionado.getCostoUnitario());
        // iva
        // total iva
        // auxCantidad

        InvUnidadAlternativa invUnidadAlternativa = obtenerInvUnidadAlternativa();
        BigDecimal factor = BigDecimal.ZERO;
        String operador = "";

        if (invUnidadAlternativa != null) {
            factor = invUnidadAlternativa.getFactor();
            operador = invUnidadAlternativa.getOperador();
        }

        Double auxCantidad = 0.0;
        switch (operador) {
            case "+":
                logger.info("1:::");
                auxCantidad = cantidad.doubleValue() + factor.doubleValue();
                break;
            case "-":
                logger.info("2:::");
                auxCantidad = cantidad.doubleValue() - factor.doubleValue();
                break;
            case "*":
                logger.info("3:::");
                auxCantidad = cantidad.doubleValue() / factor.doubleValue();
                break;
            case "/":
                logger.info("4::");
                auxCantidad = cantidad.doubleValue() * factor.doubleValue();
                break;
            default:
                break;
        }
        logger.log(Level.INFO, "5::: {0}", auxCantidad);
        movimientoSeleccionado.setAuxCantidad(auxCantidad);
        movimientoSeleccionado.setDescuento(invArticuloSeleccionado.getDescuento());
        movimientoSeleccionado.setFactor(facCatalogoPrecioD.getFactor());

        movimientoSeleccionado.setOperador(operador);
        BigDecimal porcDesc1 = movimientoSeleccionado.getPorcDesc1();
        if (porcDesc1 != null) {
            movimientoSeleccionado.setPorcDesc1(porcDesc1);
        } else {
            movimientoSeleccionado.setPorcDesc1(BigDecimal.ZERO);
        }
        movimientoSeleccionado.setPorcDesc2(BigDecimal.ZERO);
        movimientoSeleccionado.setPorcDesc3(BigDecimal.ZERO);

        movimientoSeleccionado.setPorcentajeIva(invArticuloSeleccionado.getIva());

        movimientoSeleccionado.setValorCompra(BigInteger.ZERO);

        InvInventario invInventario = new InvInventario(obtenerEmpresa(), codBodega, codInventario);
        movimientoSeleccionado.getInvBodegaArt().setInvInventario(invInventario);

        calcularResumen();

        // TODO terminar el mapeo
        RequestContext
                .getCurrentInstance().update("pedido:accordionPanel:formTablaArticulos");

    }

    public void loadPrecioTotalByCantidad() {
        logger.log(Level.INFO, "precio venta: {0}", invArticuloSeleccionado.getPrecio());

        InvMovimientoDtll movimientoSeleccionado = obtenerMovimientoSeleccionado();

        Double existence = this.existencia;
        logger.log(Level.INFO, "existencia: {0}", existence);

        BigDecimal cantidad = movimientoSeleccionado.getCantidad();
        logger.log(Level.INFO, "cantidad: {0}", cantidad.doubleValue());
        if (existence >= cantidad.doubleValue()) {

            Double precioTotal;
            Double descuento = 0.0;
            if (invArticuloSeleccionado.getDescuento() != null) {
                descuento = (movimientoSeleccionado.getCostoUnitario() * cantidad.doubleValue() * invArticuloSeleccionado.getDescuento().doubleValue()) / 100;
            }

            logger.log(Level.INFO, "descuento: {0}", descuento);

            precioTotal = (movimientoSeleccionado.getCostoUnitario() * cantidad.doubleValue()) - descuento;

            precioTotal = Round.round(precioTotal, 2);

            logger.log(Level.INFO, "precioTotal: {0}", precioTotal);
            movimientoSeleccionado.setCostoTotal(precioTotal);

            Double totalPlusIVA = precioTotal * (1 + invArticuloSeleccionado.getIva().doubleValue() / 100);
            logger.log(Level.INFO, "totalPlusIVA: {0}", totalPlusIVA);
            invArticuloSeleccionado.setTotalPlusIVA(totalPlusIVA);

            agregarBloqueado = false;

            RequestContext.getCurrentInstance().update("pedido:accordionPanel:formArticulo:totalRegistro");
            RequestContext.getCurrentInstance().update("pedido:accordionPanel:formArticulo:totalIva");
            cantidadExcedida = "";
            colorCantidadExcedida = "black";
            RequestContext.getCurrentInstance().update("pedido:accordionPanel:formArticulo:cantidadLabel");
            RequestContext.getCurrentInstance().update("pedido:accordionPanel:formArticulo:botonAgregar");

        } else {
            cantidadExcedida = "Excedida";
            colorCantidadExcedida = "red";

            movimientoSeleccionado.setCantidad(null);
            movimientoSeleccionado.getInvBodegaArt().getInvBodegaArtPK().setCodBodega(null);
            movimientoSeleccionado.setCostoTotal(null);
            invArticuloSeleccionado.setTotalPlusIVA(null);

            agregarBloqueado = true;

            RequestContext.getCurrentInstance().update("pedido:accordionPanel:formArticulo:totalRegistro");
            RequestContext.getCurrentInstance().update("pedido:accordionPanel:formArticulo:totalIva");
            RequestContext.getCurrentInstance().update("pedido:accordionPanel:formArticulo:cantidadLabel");
            RequestContext.getCurrentInstance().update("pedido:accordionPanel:formArticulo:botonAgregar");
        }
    }

    public void loadPrecioUnitarioByUnidadMedida() {
        InvMovimientoDtll invMovimientoDtll = obtenerMovimientoSeleccionado();
        String codUnidad;
        if (invMovimientoDtll.getCodUnidad() != null) {
            codUnidad = invMovimientoDtll.getCodUnidad();
        } else {
            codUnidad = obtenerMovimientoSeleccionado().getCodUnidad();
        }
        String codEmpresa = obtenerEmpresa();
        int codArticle = invMovimientoDtll.getInvArticulo().getInvArticuloPK().getCodArticulo();

        StringBuilder id = new StringBuilder();

        id.append("find;codEmpresa=");
        id.append(codEmpresa);
        id.append(";");
        id.append("codUnidad=");
        id.append(codUnidad);
        id.append(";");
        id.append("codArticulo=");
        id.append(codArticle);
        InvUnidadAlternativa invUnidadAlternativa = invUnidadAlternativaFacadeREST.find_JSON(InvUnidadAlternativa.class, id.toString());
        invMovimientoDtll.setInvUnidadAlternativa(invUnidadAlternativa);
        BigDecimal auxPrecio = BigDecimal.ZERO;
        BigDecimal factor = BigDecimal.ZERO;
        String operador = "";

        if (invUnidadAlternativa != null) {
            auxPrecio = facCatalogoPrecioD.getAuxPrecio();
            factor = invUnidadAlternativa.getFactor();
            operador = invUnidadAlternativa.getOperador();
        }

        logger.log(Level.INFO, "auxPrecio: {0}", auxPrecio);
        logger.log(Level.INFO, "factor: {0}", factor);
        logger.log(Level.INFO, "operador: {0}", operador);

        Double precio;
        switch (operador) {
            case "+":
                precio = auxPrecio.doubleValue() + factor.doubleValue();
                existencia = Round.round(invArticuloSeleccionado.getExistencia().doubleValue() + factor.doubleValue(), 4);
                break;
            case "-":
                precio = auxPrecio.doubleValue() - factor.doubleValue();
                existencia = Round.round(invArticuloSeleccionado.getExistencia().doubleValue() - factor.doubleValue(), 4);
                break;
            case "*":
                precio = auxPrecio.doubleValue() / factor.doubleValue();
                existencia = Round.round(invArticuloSeleccionado.getExistencia().doubleValue() * factor.doubleValue(), 4);
                break;
            case "/":
                precio = auxPrecio.doubleValue() * factor.doubleValue();
                existencia = Round.round(invArticuloSeleccionado.getExistencia().doubleValue() / factor.doubleValue(), 4);
                break;
            default:
                precio = auxPrecio.doubleValue();
        }

        invMovimientoDtll.setCostoUnitario(Round.round(precio, 4));

        logger.log(Level.INFO, "$$$$$$$$$$ precio venta: {0}", invMovimientoDtll.getCostoUnitario());

        if (invMovimientoDtll.getCantidad() != null) {
            loadPrecioTotalByCantidad();
        }

        RequestContext.getCurrentInstance().update("pedido:accordionPanel:formArticulo:precioUnitario");
        RequestContext.getCurrentInstance().update("pedido:accordionPanel:formArticulo:existencia");
    }

    public String enviar() {
        try {
            GnrContadorDocFacadeREST gnrContadorDocFacadeREST = new GnrContadorDocFacadeREST();
            BigDecimal numDocumentoResp = gnrContadorDocFacadeREST.numDocumento(BigDecimal.class, "01", "03", "SAI");
            prepararInvMovimientoCab(numDocumentoResp);
            prepararInvMovimientoDtlls(numDocumentoResp);

            prepararFacTmpFactC();
            prepararFacTmpFactDs(numDocumentoResp);

            Pedido pedido = new Pedido();

            pedido.setFacTmpFactC(facTmpFactC);
            pedido.setInvMovimientoCab(invMovimientoCab);

            logger.info("Enviando Documento ...");
            invMovimientoCabFacadeREST.create_JSON(pedido);
            logger.info("Documento Enviado.");

            limpiar();

            addMessage("Pedido enviado exitosamente.", "Num. Pedido: " + numDocumentoResp, FacesMessage.SEVERITY_INFO);
            FacesContext context = FacesContext.getCurrentInstance();
            context.getExternalContext().getFlash().setKeepMessages(true);
            return "pedido?faces-redirect=true";
        } catch (EmptyException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            addMessage("Advertencia", ex.getMessage(), FacesMessage.SEVERITY_INFO);
            return "pedido?faces-redirect=true";
        } catch (NullPointerException ex) {
            logger.log(Level.SEVERE, "Por favor validar registro(s).", ex);
            addMessage("Advertencia", "Por favor validar registro(s).", FacesMessage.SEVERITY_INFO);
            return "pedido?faces-redirect=true";
        }
    }

    public void loadTipoPago() {
        invMovimientoCab.setFormaPago(formaPago);
    }

    private void addMessage(String summary, String detail, Severity severity) {
        FacesMessage message = new FacesMessage(severity, summary, detail);
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    private void limpiar() {
        codArticulo = 0;
        invArticuloSeleccionado = null;
        totalIVA = null;
        input = null;
        mapa.setDireccion("");

        iva = null;
        subTotal = null;
        total = null;

        invMovimientoCab = null;
        invMovimientoCab = new InvMovimientoCab();
        invMovimientoDtlls.clear();
        invMovimientoCab.setInvMovimientoDtllList(invMovimientoDtlls);

        facTmpFactC = null;
        facTmpFactC = new FacTmpFactC();
        facTmpFactDs.clear();
        facTmpFactC.setFacTmpFactDList(facTmpFactDs);

        articulos.clear();
        invMovimientoDtllSeleccionado = null;

        formaPago = null;

        clientes.limpiar();
        cliente.limpiar();
    }

    private CxcCliente obtenerCliente() throws EmptyException {
        if (cliente.getCliente() == null) {
            throw new EmptyException("Por favor seleccione el cliente.");
        }

        return new CxcCliente(obtenerEmpresa(), cliente.getCliente().getCodCliente());
    }

    private VCliente obtenerVCliente() throws EmptyException {
        if (cliente.getCliente() == null) {
            throw new EmptyException("Por favor seleccione el cliente.");
        }

        return cliente.getCliente();
    }

    private BigDecimal buscarDescuento() throws EmptyException {
        String articulosString = invArticuloFacadeREST.findByArticuloEmpresa(String.class, String.valueOf(invArticuloSeleccionado.getInvArticuloPK().getCodArticulo()), obtenerEmpresa());
        List<InvArticulo> listaArticulos = gson.fromJson(articulosString, new TypeToken<java.util.List<InvArticulo>>() {
        }.getType());

        String clientesString = vClienteFacadeREST.findByClienteEmpresa(String.class, String.valueOf(obtenerVCliente().getCodCliente()), obtenerEmpresa());

        List<VCliente> listaClientes = gson.fromJson(clientesString, new TypeToken<java.util.List<VCliente>>() {
        }.getType());

        FacDescVolPK facDescVolPK = new FacDescVolPK();

        facDescVolPK.setCodEmpresa(obtenerEmpresa());
        facDescVolPK.setCodGrupo(listaClientes.get(0).getCodGrupo());
        facDescVolPK.setCodTipo(listaClientes.get(0).getCodTipo());
        facDescVolPK.setCodGrupo1(listaArticulos.get(0).getInvGrupo3().getInvGrupo3PK().getCodGrupo1());
        facDescVolPK.setCodGrupo2(listaArticulos.get(0).getInvGrupo3().getInvGrupo3PK().getCodGrupo2());
        facDescVolPK.setCodGrupo3(listaArticulos.get(0).getInvGrupo3().getInvGrupo3PK().getCodGrupo3());

        StringBuilder id = new StringBuilder();

        id.append(
                "find;codEmpresa=");
        id.append(facDescVolPK.getCodEmpresa());
        id.append(
                ";");
        id.append(
                "codGrupo=");
        id.append(facDescVolPK.getCodGrupo());
        id.append(
                ";");
        id.append(
                "codTipo=");
        id.append(facDescVolPK.getCodTipo());
        id.append(
                ";");
        id.append(
                "codGrupo1=");
        id.append(facDescVolPK.getCodGrupo1());
        id.append(
                ";");
        id.append(
                "codGrupo2=");
        id.append(facDescVolPK.getCodGrupo2());
        id.append(
                ";");
        id.append(
                "codGrupo3=");
        id.append(facDescVolPK.getCodGrupo3());

        FacDescVol facDescVol = facDescVolFacadeREST.find_JSON(FacDescVol.class, id.toString());

        if (facDescVol
                != null) {
            invArticuloSeleccionado.setDescuento(facDescVol.getPorcDescuento());
        }

        return invArticuloSeleccionado.getDescuento();
    }

    private BigDecimal findIva() {
        StringBuilder id = new StringBuilder();

        id.append("find;codEmpresa=");
        id.append(obtenerEmpresa());
        id.append(";");
        id.append("codIva=");
        id.append(invArticuloSeleccionado.getCodIva());

        InvIva invIva = invIvaFacadeREST.find_JSON(InvIva.class, id.toString());

        return invIva.getValor();
    }

    private List<InvMovimientoDtll> obtenerMovimientos() {
        return invMovimientoDtlls;
    }

    private InvMovimientoDtll obtenerMovimientoSeleccionado() {
        return invMovimientoDtlls.get(invMovimientoDtlls.indexOf(getInvMovimientoDtllSeleccionado()));
    }

    private void calcularResumen() {
        subTotal = 0.0;
        iva = 0.0;
        total = 0.0;
        totalSinIva = 0.0;
        totalConIva = 0.0;
        for (InvMovimientoDtll invMovimientoDtll1 : obtenerMovimientos()) {
            BigDecimal cantidad = BigDecimal.ZERO;
            if (invMovimientoDtll1.getCantidad() != null) {
                cantidad = invMovimientoDtll1.getCantidad();
                logger.info(cantidad.toString());
            }

            Double descuento = 0.0;
            if (invMovimientoDtll1.getDescuento() != null) {
                descuento = (invMovimientoDtll1.getCostoUnitario() * cantidad.doubleValue() * invMovimientoDtll1.getDescuento().doubleValue()) / 100;
                logger.info(descuento.toString());
            }

            Double _subTotal = Round.round(invMovimientoDtll1.getCostoUnitario() * (invMovimientoDtll1.getCantidad().intValue()) - descuento, 2);
            logger.log(Level.INFO, "_subTotal: {0}", _subTotal);

            Double _iva = invMovimientoDtll1.getCostoUnitario() * (invMovimientoDtll1.getPorcentajeIva().doubleValue() / 100);
            logger.log(Level.INFO, "_iva: {0}", _iva);

            subTotal += Round.round(_subTotal, 2);
            logger.log(Level.INFO, "subTotal: {0}", subTotal);
            iva += Round.round(_iva, 2);
            logger.log(Level.INFO, "iva: {0}", iva);
            total += Round.round((_subTotal + _iva), 2);
            logger.log(Level.INFO, "total: {0}", total);
        }
        subTotal = Round.round(subTotal, 2);
    }

    private String obtenerEmpresa() {
        return userManager.getGnrEmpresa().getCodEmpresa();
    }

    private void prepararFacTmpFactC() {
        Calendar c = Calendar.getInstance();
        facTmpFactC.setCodCliente(invMovimientoCab.getCxcCliente().getCxcClientePK().getCodCliente().longValue());
        facTmpFactC.setCodDivisa("01");
        facTmpFactC.setCodDocumento("FAC");
        facTmpFactC.setCodPago(invMovimientoCab.getFormaPago());
        facTmpFactC.setCodVendedor(invMovimientoCab.getCodVendedor());
        facTmpFactC.setContCred(invMovimientoCab.getDiasPlazo().toString());
        facTmpFactC.setDescuentos(invMovimientoCab.getDescuentos());
        facTmpFactC.setEstado("G");
        facTmpFactC.setFechaEstado(c.getTime());
        facTmpFactC.setFechaFactura(c.getTime());
        facTmpFactC.setGnrEmpresa(invMovimientoCab.getGnrEmpresa());
        facTmpFactC.setIva(invMovimientoCab.getIva());
        facTmpFactC.setNombreUsuario(invMovimientoCab.getNombreUsuario());
        facTmpFactC.setNumFactura(null);
        facTmpFactC.setOtrDescuentos(BigInteger.ZERO);
        facTmpFactC.setPorcComision(BigDecimal.ZERO);
        facTmpFactC.setRazonSocial(invMovimientoCab.getRazonSocial());
        facTmpFactC.setRecargos(BigInteger.ZERO);
        facTmpFactC.setTipoFactura("A");
        facTmpFactC.setTotalConIva(invMovimientoCab.getTConIva().longValue());
        facTmpFactC.setTotalFactura(new BigDecimal(invMovimientoCab.getTotalDocumento()));
        facTmpFactC.setTotalSinIva(invMovimientoCab.getTSinIva());
        facTmpFactC.setValorDivisa(BigInteger.ONE);
        facTmpFactC.setUbicacionPedido(mapa.getDireccion());
        FacTmpFactCPK facTmpFactCPK = new FacTmpFactCPK(invMovimientoCab.getInvMovimientoCabPK().getCodEmpresa(), Integer.parseInt(String.valueOf(invMovimientoCab.getInvMovimientoCabPK().getNumDocumento())), "SAI");
        facTmpFactC.setFacTmpFactCPK(facTmpFactCPK);
    }

    private void prepararFacTmpFactDs(BigDecimal numDocumentoResp) {

        for (InvMovimientoDtll invMovimientoDtll : invMovimientoDtlls) {
            FacTmpFactDPK facTmpFactDPK = new FacTmpFactDPK();
            facTmpFactDPK.setAuxiliar(invMovimientoDtll.getInvMovimientoDtllPK().getNumLinea());
            facTmpFactDPK.setCodEmpresa(obtenerEmpresa());
            facTmpFactDPK.setEgresoInv(numDocumentoResp.intValue());
            facTmpFactDPK.setEi("SAI");

            FacTmpFactD facTmpFactD = new FacTmpFactD();
            facTmpFactD.setAuxCantidad(invMovimientoDtll.getAuxCantidad());
            facTmpFactD.setCantidad(invMovimientoDtll.getCantidad().doubleValue());
            facTmpFactD.setCantidadDevuelta(BigInteger.ZERO); // TODO
            facTmpFactD.setCodBodega(invMovimientoDtll.getInvBodegaArt().getInvBodegaArtPK().getCodBodega());
            facTmpFactD.setCodInventario(codInventario);
            facTmpFactD.setDetalle("Web Channel"); // TODO
            facTmpFactD.setEntregado(null); //TODO
            facTmpFactD.setFacTmpFactDPK(facTmpFactDPK);
            facTmpFactD.setFactor(new BigDecimal(invMovimientoDtll.getFactor()));
            facTmpFactD.setInvUnidadAlternativa(invMovimientoDtll.getInvUnidadAlternativa());
            facTmpFactD.setOperador(invMovimientoDtll.getOperador());
            facTmpFactD.setPorcDescPago(BigDecimal.ZERO); //TODO
            facTmpFactD.setPorcDescProm(BigDecimal.ZERO); //TODO
            facTmpFactD.setPorcDescVol(invMovimientoDtll.getPorcDesc1()); //TODO
            facTmpFactD.setPorcentajeIva(invMovimientoDtll.getPorcentajeIva());
            logger.log(Level.INFO, "CostoUnitario: {0}", invMovimientoDtll.getCostoUnitario());
            facTmpFactD.setPrecioUnitario(invMovimientoDtll.getCostoUnitario()); //TODO
            facTmpFactD.setPromocion(null); //TODO
            facTmpFactD.setSerie(null); //TODO
            facTmpFactD.setTotalReg(invMovimientoDtll.getCostoTotal()); //TODO

            facTmpFactDs.add(facTmpFactD);
        }
    }

    private void prepararInvMovimientoCab(BigDecimal numDocumentoResp) throws EmptyException {
        CxcCliente cxcCliente = obtenerCliente();
        invMovimientoCab.setCxcCliente(cxcCliente);

        if (invMovimientoCab.getFormaPago() == null) {
            throw new EmptyException("Por favor seleccione la forma de pago.");
        }

        if (mapa.getDireccion() == null) {
            throw new EmptyException("Por favor active el GPS y seleccione Geolocalizar.");
        }

        InvMovimientoCabPK invMovimientoCabPK = new InvMovimientoCabPK();
        invMovimientoCabPK.setCodEmpresa(obtenerEmpresa());
        invMovimientoCabPK.setCodDocumento("SAI");
        invMovimientoCabPK.setNumDocumento(numDocumentoResp.longValue());
        invMovimientoCab.setAutoContDoc(null);
        invMovimientoCab.setAutoContImprDoc(null);
        invMovimientoCab.setDescuentos(BigInteger.ZERO);
        invMovimientoCab.setDetalle(new StringBuilder("FAC ").append(obtenerVCliente().getRazonSocial()).toString());
        invMovimientoCab.setDigitadoPor(userManager.getUserName());
        invMovimientoCab.setDiasPlazo(1);
        invMovimientoCab.setEstado("G");
        invMovimientoCab.setFechEmisDoc(null);
        invMovimientoCab.setFechCaduDoc(null);
        invMovimientoCab.setFechaVencimiento(null);
        invMovimientoCab.setFechaEmision(Calendar.getInstance().getTime());
        invMovimientoCab.setFechaEstado(Calendar.getInstance().getTime());
        invMovimientoCab.setFletes(BigInteger.ZERO);

        GnrDivisa gnrDivisa = new GnrDivisa("01", obtenerEmpresa());
        invMovimientoCab.setGnrDivisa(gnrDivisa);
        InvTransacciones invTransacciones = new InvTransacciones(obtenerEmpresa(), "20");
        invMovimientoCab.setInvTransacciones(invTransacciones);
        invMovimientoCab.setInvMovimientoCabPK(invMovimientoCabPK);
        invMovimientoCab.setInvProveedor(null);
        invMovimientoCab.setIva(BigInteger.ZERO);
        invMovimientoCab.setNumRetencion(null);
        invMovimientoCab.setNumFactRete(null);
        invMovimientoCab.setNroCuotas(null);
        invMovimientoCab.setReferencia("FAC");
        invMovimientoCab.setOtrDescuentos(BigInteger.ZERO);
        invMovimientoCab.setOtrCargos(BigInteger.ZERO);
        invMovimientoCab.setRetencion(BigInteger.ZERO);
        invMovimientoCab.setRecargos(BigInteger.ZERO);
        invMovimientoCab.setTConIva(totalConIva);
        invMovimientoCab.setTSinIva(totalSinIva);
        invMovimientoCab.setTotalDocumento(total);

        invMovimientoCab.setCodVendedor(obtenerVendedor());
        invMovimientoCab.setNombreUsuario(obtenerUsuario());
        invMovimientoCab.setRazonSocial(clientes.getCliente().getCliente().getRazonSocial());

        logger.log(Level.INFO, "enviar ::::::::::::::: {0} articulos", invMovimientoCab.getInvMovimientoDtllList().size());
    }

    private void prepararInvMovimientoDtlls(BigDecimal numDocumentoResp) throws EmptyException {
        if (invMovimientoDtlls.isEmpty()) {
            throw new EmptyException("Por favor seleccione al menos un artículo.");
        }

        Double subtotal = 0.0;
        for (InvMovimientoDtll invMovimientoDtll : invMovimientoDtlls) {
            InvBodegaArt invBodegaArt = invMovimientoDtll.getInvBodegaArt();

            String codBodega = new BodegaUtil().obtenerCodBodega(invMovimientoDtll.getInvArticulo().getInvArticuloPK().getCodArticulo());

            logger.log(Level.INFO, "codBodega recuperado: {0}", codBodega);

            invBodegaArt.getInvBodegaArtPK().setCodBodega(codBodega);

            subtotal = subtotal + invMovimientoDtll.getCostoTotal();

            InvMovimientoDtllPK invMovimientoDtllPK = new InvMovimientoDtllPK(obtenerEmpresa(), "SAI", numDocumentoResp.longValue(), Short.valueOf(String.valueOf(invMovimientoDtlls.indexOf(invMovimientoDtll) + 1)));
            invMovimientoDtll.setInvMovimientoDtllPK(invMovimientoDtllPK);

            if (invMovimientoDtll.getDescuento() == null) {
                invMovimientoDtll.setDescuento(BigDecimal.ZERO);
            }

            if (invMovimientoDtll.getInvMovimientoDtllPK() != null) {
                logger.log(Level.INFO, "InvMovimientoDtllPK: {0}", invMovimientoDtll.getInvMovimientoDtllPK().toString());
            } else {
                logger.info("InvMovimientoDtllPK: null");
            }
        }

        invMovimientoCab.setSubtotal(subtotal);
    }

    private FacParametros obtenerFacParametros() {
        String facParametrosString = facParametrosFacadeREST.findAll_JSON(String.class);
        List<FacParametros> listaFacParametros = gson.fromJson(facParametrosString, new TypeToken<java.util.List<FacParametros>>() {
        }.getType());

        for (FacParametros facParametros : listaFacParametros) {
            if (facParametros.getFacParametrosPK().getNombreUsuario().toLowerCase().
                    equals(userManager.getCurrent().getNombreUsuario().toLowerCase())
                    && facParametros.getFacParametrosPK().getCodEmpresa().
                    equals(obtenerEmpresa())) {
                logger.log(Level.INFO, "facParametros: {0}", facParametros);
                return facParametros;
            }
        }
        return null;
    }

    private Integer obtenerVendedor() {
        Integer defCodVendedor = obtenerFacParametros().getDefCodVendedor();
        logger.log(Level.INFO, "codVendedor: {0}", defCodVendedor);
        return defCodVendedor;
    }

    private GnrUsuarios obtenerUsuario() {
        GnrUsuarios gnrUsuarios = obtenerFacParametros().getGnrUsuarios();
        logger.log(Level.INFO, "gnrUsuarios: {0}", gnrUsuarios);
        return gnrUsuarios;
    }

    private String obtenerOperador() {
        InvUnidadAlternativa invUnidadAlternativa = obtenerInvUnidadAlternativa();
        BigDecimal auxPrecio = BigDecimal.ZERO;
        BigDecimal factor = BigDecimal.ZERO;
        String operador = "";

        if (invUnidadAlternativa != null) {
            auxPrecio = facCatalogoPrecioD.getAuxPrecio();
            factor = invUnidadAlternativa.getFactor();
            operador = invUnidadAlternativa.getOperador();
        }
        return operador;
    }

    private InvUnidadAlternativa obtenerInvUnidadAlternativa() {
        InvMovimientoDtll invMovimientoDtll = obtenerMovimientoSeleccionado();
        String codUnidad;
        if (invMovimientoDtll.getCodUnidad() != null) {
            codUnidad = invMovimientoDtll.getCodUnidad();
        } else {
            codUnidad = obtenerMovimientoSeleccionado().getCodUnidad();
        }
        String codEmpresa = obtenerEmpresa();
        int codArticle = invMovimientoDtll.getInvArticulo().getInvArticuloPK().getCodArticulo();

        StringBuilder id = new StringBuilder();

        id.append("find;codEmpresa=");
        id.append(codEmpresa);
        id.append(";");
        id.append("codUnidad=");
        id.append(codUnidad);
        id.append(";");
        id.append("codArticulo=");
        id.append(codArticle);

        InvUnidadAlternativa invUnidadAlternativa = invUnidadAlternativaFacadeREST.find_JSON(InvUnidadAlternativa.class, id.toString());
        invMovimientoDtll.setInvUnidadAlternativa(invUnidadAlternativa);

        return invUnidadAlternativa;
    }
}
