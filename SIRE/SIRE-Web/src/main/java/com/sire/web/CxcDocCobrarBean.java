/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sire.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sire.entities.BanCtaCte;
import com.sire.entities.CxcAbonoC;
import com.sire.entities.CxcAbonoCPK;
import com.sire.entities.CxcAbonoD;
import com.sire.entities.CxcAbonoDPK;
import com.sire.entities.CxcCheque;
import com.sire.entities.CxcChequePK;
import com.sire.entities.CxcCliente;
import com.sire.entities.CxcDocCobrar;
import com.sire.entities.CxcPagoContado;
import com.sire.entities.CxcPagoContadoPK;
import com.sire.entities.FacParametros;
import com.sire.entities.GnrLogHistorico;
import com.sire.entities.GnrLogHistoricoPK;
import com.sire.entities.Pago;
import com.sire.entities.VCliente;
import com.sire.exception.ClienteException;
import com.sire.exception.GPSException;
import com.sire.exception.MailException;
import com.sire.exception.RestException;
import com.sire.exception.VendedorException;
import com.sire.rs.client.BanCtaCteFacadeREST;
import com.sire.rs.client.CxcDocCobrarFacadeREST;
import com.sire.rs.client.FacParametrosFacadeREST;
import com.sire.rs.client.GnrContadorDocFacadeREST;
import com.sire.utils.Round;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.core.Response;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.mobile.event.SwipeEvent;

/**
 *
 * @author publio
 */
@ManagedBean(name = "cxcDocCobrarBean")
@SessionScoped

public class CxcDocCobrarBean {
    
    private static final Logger LOGGER = Logger.getLogger(CxcDocCobrarBean.class.getName());
    
    private final FacParametrosFacadeREST facParametrosFacadeREST;
    private final Gson gson;
    
    @Setter
    private List<CxcDocCobrar> cxcDocCobrarList;
    
    @Getter
    @Setter
    @ManagedProperty("#{cliente}")
    private CustomerBean cliente;
    
    @Getter
    @Setter
    @ManagedProperty(value = "#{user}")
    private UserManager userManager;
    @Getter
    @Setter
    @ManagedProperty("#{mapa}")
    private MapaBean mapa;
    
    @Getter
    @Setter
    private Double totalSaldo, totalCapital, diferencia, valorCheque = 0.0;
    
    @Setter
    private Double retencion = 0.0, retencionIVA = 0.0, efectivo = 0.0, deposito = 0.0,
            otros = 0.0, totalCheques = 0.0;
    
    @Getter
    @Setter
    private VCliente client;
    
    @Getter
    @Setter
    private CxcDocCobrar cxcDocCobrarSeleccionado;
    
    @Getter
    @Setter
    private boolean botonEnviarBloqueado = true, botonAgegarChequeBloqueado = true;
    
    @Getter
    @Setter
    private String numeroCuenta, codBanco, numCuenta, ctaCorriente;
    
    @Getter
    @Setter
    private Date fechaCheque;
    
    @Getter
    @Setter
    private BigInteger numCheque;
    
    @Getter
    @Setter
    private List<BanCtaCte> banCtaCtes;
    
    @Getter
    @Setter
    private List<CxcCheque> cxcCheques;
    
    @Resource(name = "mail/gmail")
    private Session mailSession;
    private CxcDocCobrarFacadeREST cxcDocCobrarFacadeREST;
    
    public CxcDocCobrarBean() {
        facParametrosFacadeREST = new FacParametrosFacadeREST();
        GsonBuilder builder = new GsonBuilder();
        gson = builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
        
        cxcCheques = new ArrayList<>();
        loadBanCtaCtes();
    }
    
    public void findDocCobrar() {
//        VCliente c = cliente.getCliente();
//
//        if (c != null) {
//            c.getCodCliente();
//        }

    }
    
    public List<CxcDocCobrar> getCxcDocCobrarList() {
        if (cliente.getCliente() != null) {
            loadCxcDocCobrarList();
        }
        return cxcDocCobrarList;
    }
    
    public void seleccionarCxcDocCobrar(SelectEvent event) {
        CxcDocCobrar cxcDocCobrar = (CxcDocCobrar) event.getObject();
        LOGGER.log(Level.INFO, "CxcDocCobrar 1 {0}", cxcDocCobrar.getCxcDocCobrarPK().getNumDocumento());
        cxcDocCobrarSeleccionado = cxcDocCobrarList.get(cxcDocCobrarList.indexOf(cxcDocCobrar));
        LOGGER.log(Level.INFO, "CxcDocCobrar 2 {0}", cxcDocCobrar.getCxcDocCobrarPK().getNumDocumento());
    }
    
    public void calcularSaldo() {
        Double nuevoSaldo;
        
        LOGGER.log(Level.INFO, "capital: {0}", cxcDocCobrarSeleccionado.getCapital());
        
        if (cxcDocCobrarSeleccionado.getSaldoOri() == null) {
            cxcDocCobrarSeleccionado.setSaldoOri(cxcDocCobrarSeleccionado.getSaldoDocumento());
        } else {
            cxcDocCobrarSeleccionado.setSaldoDocumento(cxcDocCobrarSeleccionado.getSaldoOri());
        }
        
        LOGGER.log(Level.INFO, "antiguoSaldo: {0}", cxcDocCobrarSeleccionado.getSaldoDocumento());
        
        if (cxcDocCobrarSeleccionado.getCapital() != null && cxcDocCobrarSeleccionado.getSaldoDocumento() >= cxcDocCobrarSeleccionado.getCapital()) {
            LOGGER.info("## nuevoSaldo ##");
            nuevoSaldo = cxcDocCobrarSeleccionado.getSaldoDocumento() - cxcDocCobrarSeleccionado.getCapital();
            cxcDocCobrarSeleccionado.setSaldoDocumento(nuevoSaldo);
        }
        
        LOGGER.log(Level.INFO, "nuevoSaldo: {0}", cxcDocCobrarSeleccionado.getSaldoDocumento());
        
        diferencia = cxcDocCobrarSeleccionado.getCapital();
        calcularTotales();
//        calcularFormaPago();
    }
    
    public void calcularFormaPago(String formaPago) {
        LOGGER.info("calcularFormaPago()");
        Double sumFormaPagos = getRetencion() + getRetencionIVA() + getEfectivo() + getDeposito() + getOtros() + getTotalCheques();
        if (sumFormaPagos == null
                || cxcDocCobrarSeleccionado == null
                || cxcDocCobrarSeleccionado.getCapital() == null) {
            return;
        }
        if (sumFormaPagos > cxcDocCobrarSeleccionado.getCapital()) {
            switch (formaPago) {
                case "retencion":
                    retencion = 0.0;
                    break;
                case "retencionIVA":
                    retencionIVA = 0.0;
                    break;
                case "efectivo":
                    efectivo = 0.0;
                    break;
                case "deposito":
                    deposito = 0.0;
                    break;
                case "otros":
                    otros = 0.0;
                    break;
                case "totalCheques":
                    totalCheques = 0.0;
                    break;
            }
            addMessage("Advertencia.", "Monto ingresado excede a la diferencia", FacesMessage.SEVERITY_WARN);
            return;
        }
        diferencia = cxcDocCobrarSeleccionado.getCapital() - sumFormaPagos;
        diferencia = Round.round(diferencia, 2);
        LOGGER.log(Level.INFO, "diferencia: {0}", diferencia);
        if (diferencia == 0 && validarCheque()) {
            botonAgegarChequeBloqueado = true;
            botonEnviarBloqueado = false;
        } else if (diferencia >= 0) {
            botonAgegarChequeBloqueado = false;
            botonEnviarBloqueado = true;
        } else {
            botonAgegarChequeBloqueado = true;
            botonEnviarBloqueado = true;
        }
    }
    
    public String enviar() {
        LOGGER.info("enviar()");
        BigDecimal numDocumentoResp = null;
        try {
            if (mapa.getDireccion() == null) {
                throw new GPSException("Por favor active el GPS y seleccione Geolocalizar.");
            }
            
            GnrContadorDocFacadeREST gnrContadorDocFacadeREST = new GnrContadorDocFacadeREST();
            numDocumentoResp = gnrContadorDocFacadeREST.numDocumento(BigDecimal.class, "01", "06", "CIN", userManager.getCurrent().getNombreUsuario());
            
            CxcAbonoC cxcAbonoC = new CxcAbonoC();
            CxcAbonoCPK cxcAbonoCPK = new CxcAbonoCPK();
            cxcAbonoCPK.setCodDocumento("CIN");
            cxcAbonoCPK.setCodEmpresa(obtenerEmpresa());
            cxcAbonoCPK.setNumAbono(numDocumentoResp.toBigInteger());
            cxcAbonoC.setCxcAbonoCPK(cxcAbonoCPK);
            cxcAbonoC.setCodDivisa("01");
            cxcAbonoC.setCodMotivo("");
            cxcAbonoC.setCodVendedor(obtenerVendedor());
            cxcAbonoC.setCxcCliente(obtenerCliente());
            cxcAbonoC.setCxcInforme(null);
            cxcAbonoC.setDetalle(userManager.getCurrent().getNombreUsuario());
            cxcAbonoC.setEstado("G");
            cxcAbonoC.setFechaAbono(Calendar.getInstance().getTime());
            cxcAbonoC.setFechaEstado(Calendar.getInstance().getTime());
            cxcAbonoC.setNombreUsuario(userManager.getCurrent());
            cxcAbonoC.setTotalCapital(cxcDocCobrarSeleccionado.getCapital());
            cxcAbonoC.setTotalMora(BigInteger.ZERO);
            
            List<CxcAbonoD> cxcAbonoDList = new ArrayList<>();
            int i = 1;
            for (CxcDocCobrar cxcDocCobrar : cxcDocCobrarList) {
                if (cxcDocCobrar.getCxcDocCobrarPK().getNumDocumento().equals(cxcDocCobrarSeleccionado.getCxcDocCobrarPK().getNumDocumento())) {
                    if (cxcDocCobrar.getSaldoOri() == null) {
                        cxcDocCobrar.setSaldoOri(cxcDocCobrarSeleccionado.getSaldoOri());
                    }
                    
                    cxcDocCobrar.setSaldoDocumento(cxcDocCobrarSeleccionado.getSaldoDocumento());
                    cxcDocCobrar.setCapital(cxcDocCobrarSeleccionado.getCapital());
                    
                    LOGGER.info("Pago: " + numDocumentoResp
                            + ", cxcDocCobrar: " + cxcDocCobrar
                            + ", cxcDocCobrarSeleccionado.getSaldoDocumento(): " + cxcDocCobrarSeleccionado.getSaldoDocumento()
                            + ", cxcDocCobrar.getSaldoOri(): " + cxcDocCobrar.getSaldoOri()
                            + ", cxcDocCobrar.getSaldoDocumento(): " + cxcDocCobrar.getSaldoDocumento());
                    if (cxcDocCobrar.getSaldoOri() != null && !Objects.equals(cxcDocCobrar.getSaldoOri(), cxcDocCobrar.getSaldoDocumento())) {
                        CxcAbonoD cxcAbonoD = new CxcAbonoD();
                        CxcAbonoDPK cxcAbonoDPK = new CxcAbonoDPK();
                        cxcAbonoDPK.setAuxiliar(i);
                        cxcAbonoDPK.setCodDocumento("CIN");
                        cxcAbonoDPK.setCodEmpresa(cxcDocCobrar.getCxcDocCobrarPK().getCodEmpresa());
                        cxcAbonoDPK.setNumAbono(numDocumentoResp.intValue());
                        cxcAbonoD.setCxcAbonoDPK(cxcAbonoDPK);
                        cxcAbonoD.setCapital(cxcDocCobrar.getCapital());
                        cxcAbonoD.setCodAbono(cxcDocCobrar.getCxcDocCobrarPK().getCodDocumento());
                        cxcAbonoD.setDias(0);
                        cxcAbonoD.setNumDocumento(cxcDocCobrar.getCxcDocCobrarPK().getNumDocumento().intValue());
                        cxcAbonoD.setNumeroCuota(cxcDocCobrar.getCxcDocCobrarPK().getNumeroCuota().intValue());
                        cxcAbonoD.setPorcComision(BigDecimal.ZERO);
                        cxcAbonoD.setValorMora(BigInteger.ZERO);
                        
                        cxcAbonoDList.add(cxcAbonoD);
                        i++;
                    }
                }
            }
            
            LOGGER.info("Pago: " + numDocumentoResp + ", cxcAbonoDList.size: " + cxcAbonoDList.size());
            cxcAbonoC.setCxcAbonoDList(cxcAbonoDList);
            
            CxcPagoContado cxcPagoContado = new CxcPagoContado();
            CxcPagoContadoPK cxcPagoContadoPK = new CxcPagoContadoPK();
            cxcPagoContadoPK.setCodDocumento("CIN");
            cxcPagoContadoPK.setCodEmpresa(obtenerEmpresa());
            cxcPagoContadoPK.setNumPago(new BigInteger(numDocumentoResp.toString()));
            cxcPagoContado.setCxcPagoContadoPK(cxcPagoContadoPK);
            cxcPagoContado.setCheques(totalCheques);
            cxcPagoContado.setCodTarjeta(null);
            cxcPagoContado.setCodVendedor(obtenerVendedor());
            cxcPagoContado.setCredito(BigInteger.ZERO);
            cxcPagoContado.setCtaCorriente(ctaCorriente);
            cxcPagoContado.setCxcCliente(obtenerCliente());
            cxcPagoContado.setDeposito(deposito);
            cxcPagoContado.setDetalle("Pago Web");
            cxcPagoContado.setEfectivo(efectivo);
            cxcPagoContado.setFechaDocumento(Calendar.getInstance().getTime());
            cxcPagoContado.setNombreUsuario(userManager.getCurrent().getNombreUsuario());
            cxcPagoContado.setOtros(otros);
            cxcPagoContado.setPagoTotal(cxcDocCobrarSeleccionado.getCapital());
            cxcPagoContado.setRetencion(retencion);
            cxcPagoContado.setRetencionIva(retencionIVA);
            cxcPagoContado.setTarjeta(BigInteger.ZERO);
            
            Pago pago = new Pago();
            pago.setCxcAbonoC(cxcAbonoC);
            for (CxcCheque cheque : cxcCheques) {
                cheque.getCxcChequePK().setNumDocumento(numDocumentoResp.intValue());
            }
            pago.setCxcAbonoDList(cxcAbonoC.getCxcAbonoDList());
            pago.setCxcChequeList(cxcCheques);
            pago.setCxcDocCobrarList(cxcDocCobrarList);
            pago.setCxcPagoContado(cxcPagoContado);
            agregarLog(pago);
            LOGGER.info("Enviando Pago " + numDocumentoResp + " ...");
            cxcDocCobrarFacadeREST = new CxcDocCobrarFacadeREST();
            Response response = cxcDocCobrarFacadeREST.save_JSON(pago);

            LOGGER.info("Pago: " + numDocumentoResp + ", Response: " + response.toString() + ", Status: " + response.getStatus() + ", Status info: " + response.getStatusInfo().getReasonPhrase());

            if (response.getStatus() != 200) {
                throw new RestException("No se pudo realizar el pago, por favor contacte al administrador.");
            }

            LOGGER.info("Pago " + numDocumentoResp + " Enviado.");
            
            LOGGER.info("Enviando Mail ...");
            enviarMail(pago);
            LOGGER.info("Mail Enviado.");
            
            limpiar();
            
            addMessage("Cobro relizado exitosamente.", "Num. Cobro: " + numDocumentoResp, FacesMessage.SEVERITY_INFO);
            FacesContext context = FacesContext.getCurrentInstance();
            context.getExternalContext().getFlash().setKeepMessages(true);
            return "index?faces-redirect=true";
        } catch (RestException | ClienteException | GPSException | VendedorException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            addMessage("Advertencia", ex.getMessage(), FacesMessage.SEVERITY_INFO);
            return "cobro?faces-redirect=true";
        } catch (MessagingException | MailException ex) {
            limpiar();
            LOGGER.log(Level.WARNING, ex.getMessage());
            addMessage("Cobro relizado exitosamente.", "Num. Cobro: " + numDocumentoResp + ",  pero no se pudo enviar e-mail.", FacesMessage.SEVERITY_WARN);
            FacesContext context = FacesContext.getCurrentInstance();
            context.getExternalContext().getFlash().setKeepMessages(true);
            return "index?faces-redirect=true";
        } finally {
            if (cxcDocCobrarFacadeREST != null) {
                cxcDocCobrarFacadeREST.close();
            }
        }
    }
    
    public void calcularCheques() {
        LOGGER.info("calcularCheques()");
        if (valorCheque > 0.0) {
            LOGGER.info("cxcDocCobrarSeleccionado.getCapital(): " + cxcDocCobrarSeleccionado.getCapital()
                    + ", retencion: " + retencion
                    + ", retencionIVA: " + retencionIVA + ", efectivo: " + efectivo
                    + ", deposito: " + deposito + ", otros: " + otros
                    + ", totalCheques:" + totalCheques + ", valorCheque: " + valorCheque);
            Double dif = cxcDocCobrarSeleccionado.getCapital() - retencion - retencionIVA - efectivo - deposito - otros - totalCheques - valorCheque;
            dif = Round.round(dif, 2);
            LOGGER.log(Level.INFO, "dif: {0}", dif);
            if (dif.equals(0.0) && validarCheque()) {
                botonAgegarChequeBloqueado = false;
                botonEnviarBloqueado = false;
            } else if (dif >= 0.0) {
                botonAgegarChequeBloqueado = false;
                botonEnviarBloqueado = true;
            } else {
                botonAgegarChequeBloqueado = true;
                botonEnviarBloqueado = true;
            }
        } else {
            botonAgegarChequeBloqueado = true;
        }
        LOGGER.log(Level.INFO, "botonAgegarChequeBloqueado: {0}", botonAgegarChequeBloqueado);
        RequestContext.getCurrentInstance().update("cobro:accordionPanel:chequesForm:agregarChequeButton");
    }
    
    public void agregarCheque() {
        LOGGER.log(Level.INFO, "codBanco: {0}", codBanco);
        LOGGER.log(Level.INFO, "fechaCheque: {0}", fechaCheque);
        LOGGER.log(Level.INFO, "numCheque: {0}", numCheque);
        LOGGER.log(Level.INFO, "numCuenta: {0}", numCuenta);
        LOGGER.log(Level.INFO, "valorCheque: {0}", valorCheque);
        
        CxcCheque cheque = new CxcCheque();
        cheque.setCodBanco(codBanco);
        cheque.setCodDeposito(null);
        cheque.setCodDivisa("01");
        CxcChequePK cxcChequePK = new CxcChequePK();
        cxcChequePK.setAuxiliar(cxcCheques.size() + 1);
        cxcChequePK.setCodDocumento("CIN");
        cxcChequePK.setCodEmpresa(obtenerEmpresa());
//        cxcChequePK.setNumDocumento(cxcCheques.size() + 1);
        cheque.setCxcChequePK(cxcChequePK);
        try {
            cheque.setCxcCliente(obtenerCliente());
        } catch (ClienteException ex) {
            addMessage("Advertencia", ex.getMessage(), FacesMessage.SEVERITY_INFO);
        }
        cheque.setDetalle(null);
        cheque.setEstado("G");
        
        cheque.setFechaCheque(fechaCheque);
        cheque.setFechaEstado(Calendar.getInstance().getTime());
        cheque.setFechaRecepcion(Calendar.getInstance().getTime());
        cheque.setNumCheque(numCheque);
        cheque.setNumCuenta(numCuenta);
        cheque.setNumDeposito(0L);
        cheque.setReferencia(null);
        cheque.setValorCheque(valorCheque);
        
        cxcCheques.add(cheque);
        totalCheques += valorCheque;
        
        calcularFormaPago("cheque");
        limpiarFormaCheque();
        botonAgegarChequeBloqueado = true;
        RequestContext.getCurrentInstance().update("cobro:accordionPanel:formaPagoForm:enviarButton");
    }
    
    private void loadCxcDocCobrarList() {
        CxcDocCobrarFacadeREST cxcDocCobrarFacadeREST = new CxcDocCobrarFacadeREST();
        client = cliente.getCliente();
        BigInteger codCliente = cliente.getCliente().getCodCliente();
        String cxcDocCobrarListString;
        try {
            cxcDocCobrarListString = cxcDocCobrarFacadeREST.findByCodClienteCodVendedor(String.class,
                    codCliente.toString(), obtenerVendedor().toString());
            //        String cxcDocCobrarListString = cxcDocCobrarFacadeREST.findAll_JSON(String.class);
            cxcDocCobrarList = gson.fromJson(cxcDocCobrarListString, new TypeToken<List<CxcDocCobrar>>() {
            }.getType());
            calcularTotales();
        } catch (VendedorException ex) {
            Logger.getLogger(CxcDocCobrarBean.class.getName()).log(Level.SEVERE, null, ex);
            addMessage("Advertencia", ex.getMessage(), FacesMessage.SEVERITY_INFO);
        }
    }
    
    private void calcularTotales() {
        totalSaldo = 0.0;
        totalCapital = 0.0;
        for (CxcDocCobrar cxcDocCobrar : cxcDocCobrarList) {
            totalSaldo += cxcDocCobrar.getSaldoDocumento();
            
            if (cxcDocCobrar.getCapital() != null) {
                totalCapital += cxcDocCobrar.getCapital();
            }
        }
        
        totalSaldo = Round.round(totalSaldo, 2);
        totalCapital = Round.round(totalCapital, 2);
    }
    
    private void loadBanCtaCtes() {
        BanCtaCteFacadeREST banCtaCteFacadeREST = new BanCtaCteFacadeREST();
        String banCtaCtesString = banCtaCteFacadeREST.findAll_JSON(String.class
        );
        banCtaCtes = gson.fromJson(banCtaCtesString, new TypeToken<List<BanCtaCte>>() {
        }.getType());
        RequestContext.getCurrentInstance().update("cobro:accordionPanel:formaPagoForm:bloqueC:cuentaBancaria");
    }
    
    private String obtenerEmpresa() {
        return userManager.getGnrEmpresa().getCodEmpresa();
    }
    
    private CxcCliente obtenerCliente() throws ClienteException {
        if (cliente.getCliente() == null) {
            throw new ClienteException("Por favor seleccione el cliente.");
        }
        
        return new CxcCliente(obtenerEmpresa(), cliente.getCliente().getCodCliente());
    }
    
    private BigInteger obtenerVendedor() throws VendedorException {
        FacParametros facParametros = obtenerFacParametros();
        
        if (facParametros == null) {
            throw new VendedorException("Vendedor no asociado a facturación.");
        }
        
        Integer defCodVendedor = facParametros.getDefCodVendedor();
        
        if (defCodVendedor == null) {
            throw new VendedorException("Vendedor no asociado a facturación.");
        }
        
        return BigInteger.valueOf(defCodVendedor);
    }
    
    private FacParametros obtenerFacParametros() {
        FacParametros facParametros = facParametrosFacadeREST.find_JSON(
                FacParametros.class, "id;codEmpresa=" + obtenerEmpresa()
                + ";nombreUsuario=" + userManager.getCurrent().getNombreUsuario());
        return facParametros;
    }
    
    private void addMessage(String summary, String detail, FacesMessage.Severity severity) {
        FacesMessage message = new FacesMessage(severity, summary, detail);
        FacesContext.getCurrentInstance().addMessage(null, message);
    }
    
    public boolean validarCheque() {
//        cxcChequeFacadeREST.findByCodClienteCodEmpresaMes(String.class, obtenerCliente(), obtenerEmpresa(), codBanco)
        botonEnviarBloqueado = true;
        RequestContext.getCurrentInstance().update("cobro:accordionPanel:formaPagoForm:pagoTotal");
        return true;
    }
    
    public void swipeleft(SwipeEvent event) {
//        setInvMovimientoDtllSeleccionado(((InvMovimientoDtll) event.getData()));
        CxcCheque cxcCheque = (CxcCheque) event.getData();
        
        totalCheques = totalCheques - cxcCheque.getValorCheque();
        calcularFormaPago("cheque");
        cxcCheques.remove(cxcCheque);
    }
    
    private void limpiar() {
        mapa.limpiar();
        cxcCheques.clear();
        cxcDocCobrarList = null;
        numeroCuenta = null;
        botonEnviarBloqueado = true;
        botonAgegarChequeBloqueado = true;
        cxcDocCobrarSeleccionado = null;
        retencion = 0.0;
        retencionIVA = 0.0;
        efectivo = 0.0;
        deposito = 0.0;
        otros = 0.0;
        totalCheques = 0.0;
        totalSaldo = null;
        totalCapital = null;
        diferencia = null;
        cliente.limpiar();
        limpiarFormaCheque();
    }
    
    public void limpiarFormaPago() {
        mapa.limpiar();
        cxcCheques.clear();
        cxcDocCobrarList = null;
        numeroCuenta = null;
        botonEnviarBloqueado = true;
        botonAgegarChequeBloqueado = true;
        cxcDocCobrarSeleccionado = null;
        retencion = 0.0;
        retencionIVA = 0.0;
        efectivo = 0.0;
        deposito = 0.0;
        otros = 0.0;
        totalCheques = 0.0;
        totalSaldo = null;
        totalCapital = null;
        diferencia = null;
        limpiarFormaCheque();
    }
    
    private void agregarLog(Pago pago) throws VendedorException {
        GnrLogHistorico gnrLogHistorico = new GnrLogHistorico();
        gnrLogHistorico.setDispositivo("Tablet");
        gnrLogHistorico.setEstado("G");
        gnrLogHistorico.setFechaDocumento(Calendar.getInstance().getTime());
        gnrLogHistorico.setFechaEstado(Calendar.getInstance().getTime());
        GnrLogHistoricoPK gnrLogHistoricoPK = new GnrLogHistoricoPK();
        gnrLogHistoricoPK.setCodDocumento(pago.getCxcAbonoC().getCxcAbonoCPK().getCodDocumento());
        gnrLogHistoricoPK.setCodEmpresa(obtenerEmpresa());
        gnrLogHistoricoPK.setNumDocumento(pago.getCxcAbonoC().getCxcAbonoCPK().getNumAbono().intValue());
        gnrLogHistorico.setGnrLogHistoricoPK(gnrLogHistoricoPK);
        gnrLogHistorico.setNombreUsuario(userManager.getCurrent().getNombreUsuario());
        gnrLogHistorico.setUbicacionGeografica(mapa.getDireccion());
        gnrLogHistorico.setLatitud(Double.valueOf(mapa.getLat()));
        gnrLogHistorico.setLongitud(Double.valueOf(mapa.getLng()));
        pago.setGnrLogHistorico(gnrLogHistorico);
    }
    
    private void limpiarFormaCheque() {
        codBanco = null;
        fechaCheque = null;
        numCheque = null;
        numCuenta = null;
        valorCheque = 0.0;
    }
    
    private void enviarMail(Pago pago) throws MessagingException, MailException {
        Message message = new MimeMessage(mailSession);
        
        if (cliente.getCliente().getMail() != null && !cliente.getCliente().getMail().isEmpty()) {
            String toUser = cliente.getCliente().getMail();
            String sub = "Cobro.";
            String saltoLinea = System.getProperty("line.separator");
            StringBuilder msg = new StringBuilder();
            msg.append("Documento Nº: ");
            msg.append(pago.getGnrLogHistorico().getGnrLogHistoricoPK().getNumDocumento());
            msg.append(saltoLinea);
            msg.append("Monto: ");
            msg.append(pago.getCxcAbonoC().getTotalCapital());
            msg.append(saltoLinea);
            msg.append("Ret. Fuente: ");
            msg.append(pago.getCxcPagoContado().getRetencion());
            msg.append(saltoLinea);
            msg.append("Dev. Descuento: ");
            msg.append("");
            msg.append(saltoLinea);
            msg.append("Cheques: ");
            msg.append(saltoLinea);
            for (CxcCheque cxcCheque : pago.getCxcChequeList()) {
                msg.append("Banco: ");
                msg.append(cxcCheque.getCodBanco());
                msg.append(saltoLinea);
                msg.append("Cheque Nº: ");
                msg.append(cxcCheque.getNumCheque());
                msg.append(saltoLinea);
                msg.append("Valor: ");
                msg.append(cxcCheque.getValorCheque());
            }
            
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toUser));
            message.setText(msg.toString());
            message.setSubject(sub);
            Transport.send(message);
        } else {
            throw new MailException("Cliente no tiene e-mail");
        }
    }
    
    public Double getRetencion() {
        if (retencion == null) {
            retencion = 0.0;
        }
        return retencion;
    }
    
    public Double getRetencionIVA() {
        if (retencionIVA == null) {
            retencionIVA = 0.0;
        }
        return retencionIVA;
    }
    
    public Double getEfectivo() {
        if (efectivo == null) {
            efectivo = 0.0;
        }
        return efectivo;
    }
    
    public Double getDeposito() {
        if (deposito == null) {
            deposito = 0.0;
        }
        return deposito;
    }
    
    public Double getOtros() {
        if (otros == null) {
            otros = 0.0;
        }
        return otros;
    }
    
    public Double getTotalCheques() {
        if (totalCheques == null) {
            totalCheques = 0.0;
        }
        return totalCheques;
    }
    
    public void loadCuentaBancaria() {
        ctaCorriente = numeroCuenta;
    }
}
