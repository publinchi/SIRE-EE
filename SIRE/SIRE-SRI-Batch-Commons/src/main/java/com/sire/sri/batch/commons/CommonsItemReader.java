/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sire.sri.batch.commons;

import ec.gob.sri.comprobantes.modelo.guia.Destinatario;
import ec.gob.sri.comprobantes.modelo.guia.GuiaRemision;
import ec.gob.sri.comprobantes.modelo.liquidacion.Liquidacion;
import ec.gob.sri.comprobantes.modelo.notacredito.NotaCredito;
import ec.gob.sri.comprobantes.modelo.notadebito.NotaDebito;
import ec.gob.sri.comprobantes.modelo.rentencion.ComprobanteRetencion;
import ec.gob.sri.comprobantes.modelo.InfoTributaria;
import ec.gob.sri.comprobantes.modelo.factura.Factura;
import ec.gob.sri.comprobantes.modelo.factura.Factura.Detalles;
import ec.gob.sri.comprobantes.modelo.factura.Factura.Detalles.Detalle;
import ec.gob.sri.comprobantes.modelo.factura.Factura.Detalles.Detalle.Impuestos;
import ec.gob.sri.comprobantes.modelo.factura.Factura.InfoAdicional;
import ec.gob.sri.comprobantes.modelo.factura.Factura.InfoAdicional.CampoAdicional;
import ec.gob.sri.comprobantes.modelo.factura.Factura.InfoFactura;
import ec.gob.sri.comprobantes.modelo.factura.Factura.InfoFactura.TotalConImpuestos;
import ec.gob.sri.comprobantes.modelo.factura.Factura.InfoFactura.TotalConImpuestos.TotalImpuesto;
import ec.gob.sri.comprobantes.modelo.factura.Impuesto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.Level;
import com.sire.logger.LogManager;
import org.apache.logging.log4j.Logger;
import javax.batch.api.chunk.AbstractItemReader;
import javax.naming.NamingException;
import com.sire.service.IDatasourceService;
import com.sire.sri.batch.constant.Constant;
import javax.naming.InitialContext;

/**
 *
 * @author pestupinan
 */
public abstract class CommonsItemReader extends AbstractItemReader {

    private static final Logger log = LogManager.getLogger(CommonsItemReader.class);
    protected String codEmpresa;
    private IDatasourceService datasourceService;
    private String databaseProductName;

    protected void buildFacturas(ResultSet rs, List<Factura> comprobantes) throws SQLException,
            NamingException {
        String numFacturaInterno = rs.getString(Constant.NUM_FACTURA_INTERNO);

        Factura factura = new Factura();

        InfoAdicional infoAdicional = new InfoAdicional();
        CampoAdicional direccion = new CampoAdicional();
        direccion.setValue(rs.getString(Constant.DIRECCION_COMPRADOR));
        direccion.setNombre(Constant.DIRECCION_CC);
        CampoAdicional telefono = new CampoAdicional();
        telefono.setValue(rs.getString(Constant.TELEFONO_COMPRADOR));
        telefono.setNombre(Constant.TELEFONO_CC);
        CampoAdicional email = new CampoAdicional();
        email.setValue(rs.getString(Constant.EMAIL_COMPRADOR));
        email.setNombre(Constant.EMAIL);
        CampoAdicional observacion = new CampoAdicional();
        observacion.setValue(rs.getString(Constant.OBSERVACION));
        observacion.setNombre(Constant.OBSERVACION_CC);
        CampoAdicional placa = new CampoAdicional();
        placa.setValue(rs.getString(Constant.PLACA));
        placa.setNombre("Placa");

        addCampoAdicional(infoAdicional, direccion);
        addCampoAdicional(infoAdicional, telefono);
        addCampoAdicional(infoAdicional, email);
        addCampoAdicional(infoAdicional, observacion);
        addCampoAdicional(infoAdicional, placa);

        factura.setInfoAdicional(infoAdicional);

        InfoFactura infoFactura = new InfoFactura();
        infoFactura.setContribuyenteEspecial(rs.getString(Constant.CONTRIBUYENTE_ESPECIAL));
        infoFactura.setDireccionComprador(rs.getString(Constant.DIRECCION_COMPRADOR));
        infoFactura.setDirEstablecimiento(rs.getString(Constant.DIRECCION_ESTABLECIMIENTO));
        String oldDate = rs.getString(Constant.FECHA_FACTURA);
        LocalDateTime datetime = transformDate(oldDate);
        String newDate = datetime.format(DateTimeFormatter.ofPattern(Constant.DD_MM_YYYY));
        infoFactura.setFechaEmision(newDate);
        infoFactura.setIdentificacionComprador(rs.getString(Constant.IDENTIFICACION_COMPRADOR));
        infoFactura.setImporteTotal(rs.getBigDecimal(Constant.IMPORTE_TOTAL));
        infoFactura.setMoneda(rs.getString(Constant.MONEDA));
        infoFactura.setObligadoContabilidad(rs.getString(Constant.LLEVA_CONTABILIDAD));
        infoFactura.setPropina(rs.getBigDecimal(Constant.PROPINA));
        infoFactura.setRazonSocialComprador(rs.getString(Constant.RAZON_SOCIAL_COMPRADOR));
        infoFactura.setTipoIdentificacionComprador(rs
                .getString(Constant.TIPO_IDENTIFICACION_COMPRADOR));
        infoFactura.setTotalSubsidio(rs.getBigDecimal(Constant.TOTAL_SUBSIDIO));
        TotalConImpuestos totalConImpuestos = new TotalConImpuestos();

        if (!Objects.equals(Constant.CODIGO_PORCENTAJE_TXT, rs.getString(Constant.CODIGO_PORCENTAJE))) {
            TotalImpuesto totalImpuesto1 = new TotalImpuesto();
            totalImpuesto1.setBaseImponible(rs.getBigDecimal(Constant.BASE_IMPONIBLE));
            totalImpuesto1.setCodigo(rs.getString(Constant.CODIGO_IMPUESTO));
            totalImpuesto1.setCodigoPorcentaje(rs.getString(Constant.CODIGO_PORCENTAJE));
            totalImpuesto1.setValor(rs.getBigDecimal(Constant.VALOR));
            totalConImpuestos.getTotalImpuesto().add(totalImpuesto1);
        }

        TotalImpuesto totalImpuesto2 = new TotalImpuesto();
        totalImpuesto2.setBaseImponible(rs.getBigDecimal(Constant.BASE_IMPONIBLE_SIN_IVA));
        totalImpuesto2.setCodigo(rs.getString(Constant.CODIGO_IMPUESTO_SIN_IVA));
        totalImpuesto2.setCodigoPorcentaje(rs.getString(Constant.CODIGO_PORCENTAJE_SIN_IVA));
        totalImpuesto2.setTarifa(rs.getBigDecimal(Constant.TARIFA_IVA_SIN_IVA));
        totalImpuesto2.setValor(rs.getBigDecimal(Constant.VALOR_IVA_SIN_IVA));
        totalConImpuestos.getTotalImpuesto().add(totalImpuesto2);

        infoFactura.setTotalConImpuestos(totalConImpuestos);
        infoFactura.setTotalDescuento(rs.getBigDecimal(Constant.TOTAL_DESCUENTOS));
        infoFactura.setTotalSinImpuestos(rs.getBigDecimal(Constant.TOTAL_SIN_IMPUESTOS));

        String pagosSQL = Constant.FACTURA_PAGO_SQL + "NUM_FACTURA = ? AND COD_EMPRESA = ?";

        if (log.isTraceEnabled()) {
            log.trace("pagosSQL -> {}", pagosSQL);
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = getDatasourceService().getConnection();
            preparedStatement = connection.prepareStatement(pagosSQL);
            preparedStatement.setString(1, numFacturaInterno);
            preparedStatement.setString(2, codEmpresa);
            resultSet = preparedStatement.executeQuery();
            InfoFactura.Pago pagos = new InfoFactura.Pago();
            while (resultSet.next()) {
                InfoFactura.Pago.DetallePago detallePago = new InfoFactura.Pago.DetallePago();
                detallePago.setFormaPago(resultSet.getString(Constant.CODIGO));
                detallePago.setPlazo(resultSet.getString(Constant.PLAZO));
                detallePago.setTotal(resultSet.getBigDecimal(Constant.VALOR_FORMA_PAGO));
                detallePago.setUnidadTiempo(resultSet.getString(Constant.TIEMPO));
                pagos.getPagos().add(detallePago);
            }
            infoFactura.setPagos(pagos);
        } catch (SQLException | NamingException e) {
            log.log(Level.ERROR, e);
            return;
        } finally {
            closeConnections(connection, preparedStatement, resultSet);
        }

        factura.setInfoFactura(infoFactura);

        InfoTributaria infoTributaria = new InfoTributaria();
        infoTributaria.setClaveAcceso(rs.getString(Constant.CLAVE_ACCESO));
        infoTributaria.setAmbiente(infoTributaria.getClaveAcceso().substring(23, 24));
        infoTributaria.setCodDoc(rs.getString(Constant.COD_DOCUMENTO));
        infoTributaria.setDirMatriz(rs.getString(Constant.DIRECCION_MATRIZ));
        infoTributaria.setEstab(rs.getString(Constant.ESTABLECIMIENTO));
        infoTributaria.setNombreComercial(rs.getString(Constant.NOMBRE_COMERCIAL));
        infoTributaria.setPtoEmi(rs.getString(Constant.PUNTO_EMISION));
        infoTributaria.setRazonSocial(rs.getString(Constant.RAZON_SOCIAL_EMPRESA));
        infoTributaria.setRuc(rs.getString(Constant.RUC_EMPRESA));
        infoTributaria.setSecuencial(rs.getString("SECUENCIAL"));
        infoTributaria.setTipoEmision("1");
        infoTributaria.setRegimenMicroempresas(rs.getString(Constant.REGIMEN_MICROEMPRESAS));
        infoTributaria.setAgenteRetencion(rs.getString(Constant.AGENTE_RETENCION));
        factura.setInfoTributaria(infoTributaria);

        factura.setId(Constant.COMPROBANTE);
        factura.setVersion(Constant.VERSION);

        Detalles detalles = new Detalles();

        String detalleSQL = Constant.FACTURA_D_SQL
                + "NUM_DOCUMENTO_INTERNO = ? AND COD_EMPRESA = ?";

        if (log.isTraceEnabled()) {
            log.trace(Constant.DETALLE_SQL, detalleSQL);
        }

        connection = null;
        preparedStatement = null;
        resultSet = null;
        try {
            connection = getDatasourceService().getConnection();
            preparedStatement = connection.prepareStatement(detalleSQL);
            preparedStatement.setString(1, numFacturaInterno);
            preparedStatement.setString(2, codEmpresa);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Detalle detalle = new Detalle();
                detalle.setCantidad(resultSet.getBigDecimal(Constant.CANTIDAD));
                detalle.setCodigoPrincipal(resultSet.getString(Constant.COD_ARTICULO));
                detalle.setDescripcion(resultSet.getString(Constant.NOMBRE_ARTICULO));
                detalle.setDescuento(resultSet.getBigDecimal(Constant.DESCUENTO));
                Impuestos impuestos = new Impuestos();
                Impuesto impuesto = new Impuesto();
                impuesto.setBaseImponible(resultSet.getBigDecimal(Constant.BASE_IMPONIBLE));
                impuesto.setCodigo(resultSet.getString(Constant.CODIGO_IMPUESTO));
                impuesto.setCodigoPorcentaje(resultSet.getString(Constant.CODIGO_PORCENTAJE));
                impuesto.setTarifa(resultSet.getBigDecimal(Constant.TARIFA));
                impuesto.setValor(resultSet.getBigDecimal(Constant.VALOR));
                impuestos.getImpuesto().add(impuesto);
                detalle.setImpuestos(impuestos);
                detalle.setPrecioSinSubsidio(resultSet.getBigDecimal(Constant.PRECIO_SIN_SUBSIDIO));
                detalle.setPrecioTotalSinImpuesto(resultSet
                        .getBigDecimal(Constant.PRECIO_TOTAL_SIN_IMPUESTOS));
                detalle.setPrecioUnitario(resultSet.getBigDecimal(Constant.PRECIO_UNITARIO));
                detalle.setCodigoBarras(resultSet.getString(Constant.CODIGO_BARRAS));
                detalles.getDetalle().add(detalle);
            }
            factura.setDetalles(detalles);

            comprobantes.add(factura);
        } catch (SQLException | NamingException e) {
            log.log(Level.ERROR, e);
            return;
        } finally {
            closeConnections(connection, preparedStatement, resultSet);
        }
    }

    private void addCampoAdicional(InfoAdicional infoAdicional, CampoAdicional campoAdicional) {
        if (campoAdicional.getValue() != null && !campoAdicional.getValue().isEmpty()) {
            infoAdicional.getCampoAdicional().add(campoAdicional);
        }
    }

    private void addCampoAdicional(GuiaRemision.InfoAdicional infoAdicional
            , GuiaRemision.InfoAdicional.CampoAdicional campoAdicional) {
        if (campoAdicional.getValue() != null && !campoAdicional.getValue().isEmpty()) {
            infoAdicional.getCampoAdicional().add(campoAdicional);
        }
    }

    protected void buildLiquidaciones(ResultSet rs, List<Liquidacion> comprobantes) throws SQLException,
            NamingException {
        String numFacturaInterno = rs.getString(Constant.NUM_LIQUIDACION_INTERNO);

        Liquidacion liquidacion = new Liquidacion();

        Liquidacion.InfoAdicional infoAdicional = new Liquidacion.InfoAdicional();
        Liquidacion.InfoAdicional.CampoAdicional direccion =
                new Liquidacion.InfoAdicional.CampoAdicional();
        direccion.setValue(rs.getString(Constant.DIRECCION_PROVEEDOR));
        direccion.setNombre(Constant.DIRECCION_CC);
        Liquidacion.InfoAdicional.CampoAdicional telefono =
                new Liquidacion.InfoAdicional.CampoAdicional();
        telefono.setValue(rs.getString(Constant.TELEFONO_PROVEEDOR));
        telefono.setNombre(Constant.TELEFONO_CC);
        Liquidacion.InfoAdicional.CampoAdicional email =
                new Liquidacion.InfoAdicional.CampoAdicional();
        email.setValue(rs.getString(Constant.EMAIL_PROVEEDOR));
        email.setNombre(Constant.EMAIL);
        Liquidacion.InfoAdicional.CampoAdicional observacion =
                new Liquidacion.InfoAdicional.CampoAdicional();
        observacion.setValue(rs.getString(Constant.OBSERVACION));
        observacion.setNombre(Constant.OBSERVACION_CC);
        if (direccion.getValue() != null && !direccion.getValue().isEmpty()) {
            infoAdicional.getCampoAdicional().add(direccion);
        }
        if (telefono.getValue() != null && !telefono.getValue().isEmpty()) {
            infoAdicional.getCampoAdicional().add(telefono);
        }
        if (email.getValue() != null && !email.getValue().isEmpty()) {
            infoAdicional.getCampoAdicional().add(email);
        }
        if (observacion.getValue() != null && !observacion.getValue().isEmpty()) {
            infoAdicional.getCampoAdicional().add(observacion);
        }
        liquidacion.setInfoAdicional(infoAdicional);

        Liquidacion.InfoLiquidacionCompra infoLiquidacionCompra = new Liquidacion.InfoLiquidacionCompra();
        infoLiquidacionCompra.setContribuyenteEspecial(rs.getString(Constant.CONTRIBUYENTE_ESPECIAL));
        infoLiquidacionCompra.setDireccionProveedor(rs.getString(Constant.DIRECCION_PROVEEDOR));
        infoLiquidacionCompra.setDirEstablecimiento(rs.getString(Constant.DIRECCION_ESTABLECIMIENTO));
        String oldDate = rs.getString(Constant.FECHA_FACTURA);
        LocalDateTime datetime = transformDate(oldDate);
        String newDate = datetime.format(DateTimeFormatter.ofPattern(Constant.DD_MM_YYYY));
        infoLiquidacionCompra.setFechaEmision(newDate);
        infoLiquidacionCompra.setIdentificacionProveedor(rs.getString(Constant.IDENTIFICACION_PROVEEDOR));
        infoLiquidacionCompra.setImporteTotal(rs.getBigDecimal(Constant.IMPORTE_TOTAL));
        infoLiquidacionCompra.setMoneda(rs.getString(Constant.MONEDA));
        infoLiquidacionCompra.setObligadoContabilidad(rs.getString(Constant.LLEVA_CONTABILIDAD));
        infoLiquidacionCompra.setRazonSocialProveedor(rs.getString(Constant.RAZON_SOCIAL_PROVEEDOR));
        infoLiquidacionCompra.setTipoIdentificacionProveedor(
                rs.getString(Constant.TIPO_IDENTIFICACION_PROVEEDOR));
        Liquidacion.InfoLiquidacionCompra.TotalConImpuestos totalConImpuestos =
                new Liquidacion.InfoLiquidacionCompra.TotalConImpuestos();

        if (!Objects.equals(Constant.CODIGO_PORCENTAJE_TXT,
                rs.getString(Constant.CODIGO_PORCENTAJE))) {
            Liquidacion.InfoLiquidacionCompra.TotalConImpuestos.TotalImpuesto totalImpuesto1 =
                    new Liquidacion.InfoLiquidacionCompra.TotalConImpuestos.TotalImpuesto();
            totalImpuesto1.setBaseImponible(rs.getBigDecimal(Constant.BASE_IMPONIBLE));
            totalImpuesto1.setCodigo(rs.getString(Constant.CODIGO_IMPUESTO));
            totalImpuesto1.setCodigoPorcentaje(rs.getString(Constant.CODIGO_PORCENTAJE));
            totalImpuesto1.setValor(rs.getBigDecimal(Constant.VALOR));
            totalConImpuestos.getTotalImpuesto().add(totalImpuesto1);
        }

        infoLiquidacionCompra.setTotalConImpuestos(totalConImpuestos);
        infoLiquidacionCompra.setTotalSinImpuestos(rs.getBigDecimal(Constant.TOTAL_SIN_IMPUESTOS));
        infoLiquidacionCompra.setTotalDescuento(rs.getBigDecimal(Constant.TOTAL_DESCUENTOS));

        String pagosSQL = Constant.LIQUIDACION_PAGO_SQL + "NUM_LIQUIDACION = ? AND COD_EMPRESA = ?";

        if (log.isTraceEnabled()) {
            log.trace("pagosSQL -> {}", pagosSQL);
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = getDatasourceService().getConnection();
            preparedStatement = connection.prepareStatement(pagosSQL);
            preparedStatement.setString(1, numFacturaInterno);
            preparedStatement.setString(2, codEmpresa);
            resultSet = preparedStatement.executeQuery();
            Liquidacion.InfoLiquidacionCompra.Pago pagos = new Liquidacion.InfoLiquidacionCompra.Pago();
            while (resultSet.next()) {
                Liquidacion.InfoLiquidacionCompra.Pago.DetallePago detallePago =
                        new Liquidacion.InfoLiquidacionCompra.Pago.DetallePago();
                detallePago.setFormaPago(resultSet.getString(Constant.CODIGO));
                detallePago.setPlazo(resultSet.getString(Constant.PLAZO));
                detallePago.setTotal(resultSet.getBigDecimal(Constant.VALOR_FORMA_PAGO));
                detallePago.setUnidadTiempo(resultSet.getString(Constant.TIEMPO));
                pagos.getPagos().add(detallePago);
            }
            infoLiquidacionCompra.setPagos(pagos);
        } catch (SQLException | NamingException e) {
            log.log(Level.ERROR, e);
            return;
        } finally {
            closeConnections(connection, preparedStatement, resultSet);
        }

        liquidacion.setInfoLiquidacionCompra(infoLiquidacionCompra);

        InfoTributaria infoTributaria = new InfoTributaria();
        infoTributaria.setClaveAcceso(rs.getString(Constant.CLAVE_ACCESO));
        infoTributaria.setAmbiente(infoTributaria.getClaveAcceso().substring(23, 24));
        infoTributaria.setCodDoc(rs.getString(Constant.COD_DOCUMENTO));
        infoTributaria.setDirMatriz(rs.getString(Constant.DIRECCION_MATRIZ));
        infoTributaria.setEstab(rs.getString(Constant.ESTABLECIMIENTO));
        infoTributaria.setNombreComercial(rs.getString(Constant.NOMBRE_COMERCIAL));
        infoTributaria.setPtoEmi(rs.getString(Constant.PUNTO_EMISION));
        infoTributaria.setRazonSocial(rs.getString(Constant.RAZON_SOCIAL_EMPRESA));
        infoTributaria.setRuc(rs.getString(Constant.RUC_EMPRESA));
        infoTributaria.setSecuencial(rs.getString("SECUENCIAL"));
        infoTributaria.setTipoEmision("1");
        infoTributaria.setRegimenMicroempresas(rs.getString(Constant.REGIMEN_MICROEMPRESAS));
        infoTributaria.setAgenteRetencion(rs.getString(Constant.AGENTE_RETENCION));
        liquidacion.setInfoTributaria(infoTributaria);

        liquidacion.setId(Constant.COMPROBANTE);
        liquidacion.setVersion(Constant.VERSION);

        Liquidacion.Detalles detalles = new Liquidacion.Detalles();

        String detalleSQL = Constant.LIQUIDACION_D_SQL
                + "NUM_LIQUIDACION_INTERNO = ? AND COD_EMPRESA = ?";

        if (log.isTraceEnabled()) {
            log.trace(Constant.DETALLE_SQL, detalleSQL);
        }

        connection = null;
        preparedStatement = null;
        resultSet = null;
        try {
            connection = getDatasourceService().getConnection();
            preparedStatement = connection.prepareStatement(detalleSQL);
            preparedStatement.setString(1, numFacturaInterno);
            preparedStatement.setString(2, codEmpresa);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Liquidacion.Detalles.Detalle detalle = new Liquidacion.Detalles.Detalle();
                detalle.setCantidad(resultSet.getBigDecimal(Constant.CANTIDAD));
                detalle.setCodigoPrincipal(resultSet.getString(Constant.COD_ARTICULO));
                detalle.setDescripcion(resultSet.getString(Constant.NOMBRE_ARTICULO));
                detalle.setDescuento(resultSet.getBigDecimal(Constant.DESCUENTO));
                Liquidacion.Detalles.Detalle.Impuestos impuestos = new Liquidacion.Detalles.Detalle.Impuestos();
                Impuesto impuesto = new Impuesto();
                impuesto.setBaseImponible(resultSet.getBigDecimal(Constant.BASE_IMPONIBLE));
                impuesto.setCodigo(resultSet.getString(Constant.CODIGO_IMPUESTO));
                impuesto.setCodigoPorcentaje(resultSet.getString(Constant.CODIGO_PORCENTAJE));
                impuesto.setTarifa(resultSet.getBigDecimal(Constant.TARIFA));
                impuesto.setValor(resultSet.getBigDecimal(Constant.VALOR));
                impuestos.getImpuesto().add(impuesto);
                detalle.setImpuestos(impuestos);
                detalle.setPrecioSinSubsidio(resultSet.getBigDecimal(Constant.PRECIO_SIN_SUBSIDIO));
                detalle.setPrecioTotalSinImpuesto(
                        resultSet.getBigDecimal(Constant.PRECIO_TOTAL_SIN_IMPUESTOS));
                detalle.setPrecioUnitario(resultSet.getBigDecimal(Constant.PRECIO_UNITARIO));
                detalle.setCodigoBarras(resultSet.getString(Constant.CODIGO_BARRAS));
                detalles.getDetalle().add(detalle);
            }
            liquidacion.setDetalles(detalles);

            comprobantes.add(liquidacion);
        } catch (SQLException | NamingException e) {
            log.log(Level.ERROR, e);
            return;
        } finally {
            closeConnections(connection, preparedStatement, resultSet);
        }
    }

    protected void buildNotasCredito(ResultSet rs, List<NotaCredito> comprobantes)
            throws SQLException, NamingException {
        String numNotaCreditoInterno = rs.getString(Constant.NUM_FACTURA_INTERNO);

        NotaCredito notaCredito = new NotaCredito();
        NotaCredito.InfoAdicional infoAdicional = new NotaCredito.InfoAdicional();
        NotaCredito.InfoAdicional.CampoAdicional direccion =
                new NotaCredito.InfoAdicional.CampoAdicional();
        direccion.setValue(rs.getString(Constant.DIRECCION_COMPRADOR));
        direccion.setNombre(Constant.DIRECCION_CC);
        NotaCredito.InfoAdicional.CampoAdicional telefono =
                new NotaCredito.InfoAdicional.CampoAdicional();
        telefono.setValue(rs.getString(Constant.TELEFONO_COMPRADOR));
        telefono.setNombre(Constant.TELEFONO_CC);
        NotaCredito.InfoAdicional.CampoAdicional email =
                new NotaCredito.InfoAdicional.CampoAdicional();
        email.setValue(rs.getString(Constant.EMAIL_COMPRADOR));
        email.setNombre(Constant.EMAIL);
        NotaCredito.InfoAdicional.CampoAdicional observacion =
                new NotaCredito.InfoAdicional.CampoAdicional();
        observacion.setValue(rs.getString(Constant.OBSERVACION));
        observacion.setNombre(Constant.OBSERVACION_CC);
        if (direccion.getValue() != null && !direccion.getValue().isEmpty()) {
            infoAdicional.getCampoAdicional().add(direccion);
        }
        if (telefono.getValue() != null && !telefono.getValue().isEmpty()) {
            infoAdicional.getCampoAdicional().add(telefono);
        }
        if (email.getValue() != null && !email.getValue().isEmpty()) {
            infoAdicional.getCampoAdicional().add(email);
        }
        if (observacion.getValue() != null && !observacion.getValue().isEmpty()) {
            infoAdicional.getCampoAdicional().add(observacion);
        }

        NotaCredito.InfoNotaCredito infoNotaCredito = new NotaCredito.InfoNotaCredito();

        String oldDate = rs.getString(Constant.FECHA_EMISION);
        LocalDateTime datetime = transformDate(oldDate);
        String newDate = datetime.format(DateTimeFormatter.ofPattern(Constant.DD_MM_YYYY));
        infoNotaCredito.setFechaEmision(newDate);

        infoNotaCredito.setDirEstablecimiento(rs.getString(Constant.DIRECCION_ESTABLECIMIENTO));
        infoNotaCredito.setTipoIdentificacionComprador(
                rs.getString(Constant.TIPO_IDENTIFICACION_COMPRADOR));
        infoNotaCredito.setRazonSocialComprador(rs.getString(Constant.RAZON_SOCIAL_COMPRADOR));
        infoNotaCredito.setIdentificacionComprador(rs.getString(Constant.IDENTIFICACION_COMPRADOR));
        infoNotaCredito.setContribuyenteEspecial(rs.getString(Constant.CONTRIBUYENTE_ESPECIAL));
        infoNotaCredito.setObligadoContabilidad(rs.getString(Constant.LLEVA_CONTABILIDAD));
        infoNotaCredito.setRise(rs.getString(Constant.RISE));
        infoNotaCredito.setCodDocModificado(rs.getString(Constant.COD_DOC_MODIFICADO));
        infoNotaCredito.setNumDocModificado(rs.getString(Constant.NUM_DOC_MODIFICADO));
        infoNotaCredito.setFechaEmisionDocSustento(
                rs.getString(Constant.FECHA_EMISION_DOCSUSTENTO));
        infoNotaCredito.setTotalSinImpuestos(rs.getBigDecimal(Constant.TOTAL_SIN_IMPUESTOS));
        infoNotaCredito.setValorModificacion(rs.getBigDecimal(Constant.VALOR_MODIFICADO));
        infoNotaCredito.setMoneda(rs.getString(Constant.MONEDA));

        ec.gob.sri.comprobantes.modelo.notacredito.TotalConImpuestos totalConImpuestos =
                new ec.gob.sri.comprobantes.modelo.notacredito.TotalConImpuestos();
        ec.gob.sri.comprobantes.modelo.notacredito.TotalConImpuestos.TotalImpuesto totalImpuesto =
                new ec.gob.sri.comprobantes.modelo.notacredito.TotalConImpuestos.TotalImpuesto();
        totalImpuesto.setBaseImponible(rs.getBigDecimal(Constant.BASE_IMPONIBLE));
        totalImpuesto.setCodigo(rs.getString(Constant.CODIGO_IMPUESTO));
        totalImpuesto.setCodigoPorcentaje(rs.getString(Constant.CODIGO_PORCENTAJE));
        totalImpuesto.setValor(rs.getBigDecimal(Constant.VALOR));
        totalConImpuestos.getTotalImpuesto().add(totalImpuesto);
        infoNotaCredito.setTotalConImpuestos(totalConImpuestos);

        infoNotaCredito.setMotivo(rs.getString(Constant.MOTIVO));

        notaCredito.setInfoNotaCredito(infoNotaCredito);

        InfoTributaria infoTributaria = new InfoTributaria();
        infoTributaria.setClaveAcceso(rs.getString(Constant.CLAVE_ACCESO));
        infoTributaria.setAmbiente(infoTributaria.getClaveAcceso().substring(23, 24));
        infoTributaria.setCodDoc(rs.getString(Constant.COD_DOCUMENTO));
        infoTributaria.setDirMatriz(rs.getString(Constant.DIRECCION_MATRIZ));
        infoTributaria.setEstab(rs.getString(Constant.ESTABLECIMIENTO));
        infoTributaria.setNombreComercial(rs.getString(Constant.NOMBRE_COMERCIAL));
        infoTributaria.setPtoEmi(rs.getString(Constant.PUNTO_EMISION));
        infoTributaria.setRazonSocial(rs.getString(Constant.RAZON_SOCIAL_EMPRESA));
        infoTributaria.setRuc(rs.getString(Constant.RUC_EMPRESA));
        infoTributaria.setSecuencial(rs.getString(Constant.SECUENCIAL));
        infoTributaria.setTipoEmision("1");
        infoTributaria.setRegimenMicroempresas(rs.getString(Constant.REGIMEN_MICROEMPRESAS));
        infoTributaria.setAgenteRetencion(rs.getString(Constant.AGENTE_RETENCION));
        notaCredito.setInfoTributaria(infoTributaria);

        notaCredito.setId(Constant.COMPROBANTE);
        notaCredito.setVersion(Constant.VERSION);

        NotaCredito.Detalles detalles = new NotaCredito.Detalles();

        String detalleSQL = Constant.NOTA_CREDITO_D_SQL
                + "NUM_DOCUMENTO_INTERNO = ? AND COD_EMPRESA = ?";

        if (log.isTraceEnabled()) {
            log.trace(Constant.DETALLE_SQL, detalleSQL);
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = getDatasourceService().getConnection();
            preparedStatement = connection.prepareStatement(detalleSQL);
            preparedStatement.setString(1, numNotaCreditoInterno);
            preparedStatement.setString(2, codEmpresa);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                NotaCredito.Detalles.Detalle detalle = new NotaCredito.Detalles.Detalle();
                detalle.setCodigoInterno(resultSet.getString(Constant.COD_ARTICULO));
                detalle.setDescripcion(resultSet.getString(Constant.NOMBRE_ARTICULO));
                detalle.setCantidad(resultSet.getBigDecimal(Constant.CANTIDAD));
                detalle.setPrecioUnitario(resultSet.getBigDecimal(Constant.PRECIO_UNITARIO));
                detalle.setDescuento(resultSet.getBigDecimal(Constant.DESCUENTO));
                detalle.setPrecioTotalSinImpuesto(
                        resultSet.getBigDecimal(Constant.PRECIO_TOTAL_SIN_IMPUESTOS));

                NotaCredito.Detalles.Detalle.Impuestos impuestos =
                        new NotaCredito.Detalles.Detalle.Impuestos();
                ec.gob.sri.comprobantes.modelo.notacredito.Impuesto impuesto =
                        new ec.gob.sri.comprobantes.modelo.notacredito.Impuesto();
                impuesto.setCodigo(resultSet.getString(Constant.CODIGO_IMPUESTO));
                impuesto.setCodigoPorcentaje(resultSet.getString(Constant.CODIGO_PORCENTAJE));
                impuesto.setTarifa(resultSet.getBigDecimal(Constant.TARIFA));
                impuesto.setBaseImponible(resultSet.getBigDecimal(Constant.BASE_IMPONIBLE));
                impuesto.setValor(resultSet.getBigDecimal(Constant.VALOR));

                impuestos.getImpuesto().add(impuesto);
                detalle.setImpuestos(impuestos);
                detalles.getDetalle().add(detalle);
            }
            notaCredito.setDetalles(detalles);
            notaCredito.setInfoAdicional(infoAdicional);

            comprobantes.add(notaCredito);
        } catch (SQLException | NamingException e) {
            log.log(Level.ERROR, e);
            return;
        } finally {
            closeConnections(connection, preparedStatement, resultSet);
        }
    }

    protected void buildNotasDebito(ResultSet rs, List<NotaDebito> comprobantes) throws SQLException,
            NamingException {
        String numDocumentoInterno = rs.getString(Constant.NUM_DOCUMENTO_INTERNO);

        /* Nota de Débito */
        NotaDebito notaDebito = new NotaDebito();
        notaDebito.setId(Constant.COMPROBANTE);
        notaDebito.setVersion("1.0.0");

        /* Información Tributaria */
        InfoTributaria infoTributaria = new InfoTributaria();
        infoTributaria.setTipoEmision("1");
        infoTributaria.setClaveAcceso(rs.getString(Constant.CLAVE_ACCESO));
        infoTributaria.setAmbiente(infoTributaria.getClaveAcceso().substring(23, 24));
        infoTributaria.setRazonSocial(rs.getString(Constant.RAZON_SOCIAL_EMPRESA));
        infoTributaria.setNombreComercial(rs.getString(Constant.NOMBRE_COMERCIAL));
        infoTributaria.setRuc(rs.getString(Constant.RUC_EMPRESA));
        infoTributaria.setCodDoc(rs.getString(Constant.COD_DOCUMENTO));
        infoTributaria.setEstab(rs.getString(Constant.ESTABLECIMIENTO));
        infoTributaria.setPtoEmi(rs.getString(Constant.PUNTO_EMISION));
        infoTributaria.setSecuencial(rs.getString(Constant.SECUENCIAL));
        infoTributaria.setDirMatriz(rs.getString(Constant.DIRECCION_MATRIZ));
        infoTributaria.setRegimenMicroempresas(rs.getString(Constant.REGIMEN_MICROEMPRESAS));
        infoTributaria.setAgenteRetencion(rs.getString(Constant.AGENTE_RETENCION));
        notaDebito.setInfoTributaria(infoTributaria);

        /* Información Nota de Débito */
        NotaDebito.InfoNotaDebito infoNotaDebito = new NotaDebito.InfoNotaDebito();
        String oldDate = rs.getString(Constant.FECHA_EMISION);
        LocalDateTime datetime = transformDate(oldDate);
        String newDate = datetime.format(DateTimeFormatter.ofPattern(Constant.DD_MM_YYYY));
        infoNotaDebito.setFechaEmision(newDate);
        infoNotaDebito.setDirEstablecimiento(rs.getString(Constant.DIRECCION_ESTABLECIMIENTO));
        infoNotaDebito.setTipoIdentificacionComprador(
                rs.getString(Constant.TIPO_IDENTIFICACION_COMPRADOR));
        infoNotaDebito.setRazonSocialComprador(rs.getString(Constant.RAZON_SOCIAL_COMPRADOR));
        infoNotaDebito.setIdentificacionComprador(rs.getString(Constant.IDENTIFICACION_COMPRADOR));
        infoNotaDebito.setContribuyenteEspecial(rs.getString(Constant.CONTRIBUYENTE_ESPECIAL));
        infoNotaDebito.setObligadoContabilidad(rs.getString(Constant.LLEVA_CONTABILIDAD));
        infoNotaDebito.setCodDocModificado(rs.getString(Constant.COD_DOC_MODIFICADO));
        infoNotaDebito.setNumDocModificado(rs.getString(Constant.NUM_DOC_MODIFICADO));
        String oldDate1 = rs.getString(Constant.FECHA_EMISION_DOCSUSTENTO);
        LocalDateTime datetime1 = transformDate(oldDate1);
        String newDate1 = datetime1.format(DateTimeFormatter.ofPattern(Constant.DD_MM_YYYY));
        infoNotaDebito.setFechaEmisionDocSustento(newDate1);
        infoNotaDebito.setTotalSinImpuestos(rs.getBigDecimal(Constant.TOTAL_SIN_IMPUESTOS));

        //Impuestos
        NotaDebito.InfoNotaDebito.Impuestos impuestos = new NotaDebito.InfoNotaDebito.Impuestos();
        ec.gob.sri.comprobantes.modelo.notadebito.Impuesto impuesto;
        impuesto = new ec.gob.sri.comprobantes.modelo.notadebito.Impuesto();
        impuesto.setCodigo(rs.getString(Constant.CODIGO_IMPUESTO));
        impuesto.setCodigoPorcentaje(rs.getString(Constant.CODIGO_PORCENTAJE));
        impuesto.setTarifa(rs.getBigDecimal(Constant.TARIFA));
        impuesto.setBaseImponible(rs.getBigDecimal(Constant.BASE_IMPONIBLE));
        impuesto.setValor(rs.getBigDecimal(Constant.VALOR));
        impuestos.getImpuesto().add(impuesto);
        infoNotaDebito.setImpuestos(impuestos);
        infoNotaDebito.setValorTotal(rs.getBigDecimal(Constant.VALOR_TOTAL));

        String pagoSQL = Constant.NOTA_DEBITO_PAGO_SQL + "NUM_DOCUMENTO_INTERNO = "
                + numDocumentoInterno;

        //Pagos
        NotaDebito.InfoNotaDebito.Pago pagos = new NotaDebito.InfoNotaDebito.Pago();

        if (log.isTraceEnabled()) {
            log.trace("pagoSQL -> {}", pagoSQL);
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = getDatasourceService().getConnection();
            preparedStatement = connection.prepareStatement(pagoSQL);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                NotaDebito.InfoNotaDebito.Pago.DetallePago detallePago =
                        new NotaDebito.InfoNotaDebito.Pago.DetallePago();
                detallePago.setFormaPago(resultSet.getString(Constant.FORMA_PAGO));
                detallePago.setTotal(resultSet.getBigDecimal(Constant.VALOR_FORMA_PAGO));
                detallePago.setPlazo(resultSet.getString(Constant.PLAZO));
                detallePago.setUnidadTiempo(resultSet.getString(Constant.TIEMPO));
                pagos.getPagos().add(detallePago);
            }
            infoNotaDebito.setPagos(pagos);
            notaDebito.setInfoNotaDebito(infoNotaDebito);
        } catch (SQLException | NamingException e) {
            log.log(Level.ERROR, e);
            return;
        } finally {
            closeConnections(connection, preparedStatement, resultSet);
        }

        /* Motivos */
        NotaDebito.Motivos motivos = new NotaDebito.Motivos();
        NotaDebito.Motivos.Motivo motivo = new NotaDebito.Motivos.Motivo();
        motivo.setRazon(rs.getString(Constant.RAZON));
        motivo.setValor(rs.getBigDecimal(Constant.TOTAL_SIN_IMPUESTOS));
        motivos.getMotivo().add(motivo);
        notaDebito.setMotivos(motivos);

        /* Información Adicional */
        NotaDebito.InfoAdicional infoAdicional = new NotaDebito.InfoAdicional();
        NotaDebito.InfoAdicional.CampoAdicional direccion =
                new NotaDebito.InfoAdicional.CampoAdicional();
        direccion.setValue(rs.getString(Constant.DIRECCION_COMPRADOR));
        direccion.setNombre("Dirección");

        NotaDebito.InfoAdicional.CampoAdicional email =
                new NotaDebito.InfoAdicional.CampoAdicional();
        email.setValue(rs.getString(Constant.EMAIL_COMPRADOR));
        email.setNombre(Constant.EMAIL);

        NotaDebito.InfoAdicional.CampoAdicional telefono =
                new NotaDebito.InfoAdicional.CampoAdicional();
        telefono.setValue(rs.getString(Constant.TELEFONO_COMPRADOR));
        telefono.setNombre("Teléfono");

        if (direccion.getValue() != null && !direccion.getValue().isEmpty()) {
            infoAdicional.getCampoAdicional().add(direccion);
        }

        if (email.getValue() != null && !email.getValue().isEmpty()) {
            infoAdicional.getCampoAdicional().add(email);
        }

        if (telefono.getValue() != null && !telefono.getValue().isEmpty()) {
            infoAdicional.getCampoAdicional().add(telefono);
        }

        notaDebito.setInfoAdicional(infoAdicional);
        comprobantes.add(notaDebito);
    }

    protected void buildGuiasRemision(ResultSet rs, List<GuiaRemision> comprobantes)
            throws SQLException, NamingException {
        String numDespachoInterno = rs.getString(Constant.NUM_DESPACHO_INTERNO);
        GuiaRemision guiaRemision = new GuiaRemision();
        guiaRemision.setId(Constant.COMPROBANTE);
        guiaRemision.setVersion(Constant.VERSION);

        /* Información Tributaria */
        InfoTributaria infoTributaria = new InfoTributaria();
        infoTributaria.setClaveAcceso(rs.getString(Constant.CLAVE_ACCESO));
        infoTributaria.setAmbiente(infoTributaria.getClaveAcceso().substring(23, 24));
        infoTributaria.setTipoEmision("1");
        infoTributaria.setRazonSocial(rs.getString(Constant.RAZON_SOCIAL_EMPRESA));
        infoTributaria.setNombreComercial(rs.getString(Constant.NOMBRE_COMERCIAL));
        infoTributaria.setRuc(rs.getString(Constant.RUC_EMPRESA));
        infoTributaria.setCodDoc(rs.getString(Constant.COD_DOCUMENTO));
        infoTributaria.setEstab(rs.getString(Constant.ESTABLECIMIENTO));
        infoTributaria.setPtoEmi(rs.getString(Constant.PUNTO_EMISION));
        infoTributaria.setSecuencial(rs.getString(Constant.SECUENCIAL));
        infoTributaria.setDirMatriz(rs.getString(Constant.DIRECCION_MATRIZ));
        infoTributaria.setRegimenMicroempresas(rs.getString(Constant.REGIMEN_MICROEMPRESAS));
        infoTributaria.setAgenteRetencion(rs.getString(Constant.AGENTE_RETENCION));
        guiaRemision.setInfoTributaria(infoTributaria);

        /* Información Guia de Remisión */
        GuiaRemision.InfoGuiaRemision infoGuiaRemision = new GuiaRemision.InfoGuiaRemision();
        infoGuiaRemision.setDirEstablecimiento(rs.getString(Constant.DIRECCION_ESTABLECIMIENTO));
        infoGuiaRemision.setDirPartida(rs.getString(Constant.DIR_PARTIDA));
        infoGuiaRemision.setRazonSocialTransportista(
                rs.getString(Constant.RAZON_SOCIAL_TRANSPORTISTA));
        infoGuiaRemision.setTipoIdentificacionTransportista(
                rs.getString(Constant.TIPO_IDENT_TRANSPORTISTA));
        infoGuiaRemision.setRucTransportista(rs.getString(Constant.RUC_TRANSPORTISTA));
        infoGuiaRemision.setRise(rs.getString(Constant.RISE));
        infoGuiaRemision.setObligadoContabilidad(rs.getString(Constant.LLEVA_CONTABILIDAD));
        infoGuiaRemision.setContribuyenteEspecial(rs.getString(Constant.CONTRIBUYENTE_ESPECIAL));

        String oldDate = rs.getString(Constant.FECHA_INICIO_TRANSPORTE);
        LocalDateTime datetime = transformDate(oldDate);
        String newDate = datetime.format(DateTimeFormatter.ofPattern(Constant.DD_MM_YYYY));
        infoGuiaRemision.setFechaIniTransporte(newDate);

        String oldDate1 = rs.getString(Constant.FECHA_FIN_TRANSPORTE);
        LocalDateTime datetime1 = transformDate(oldDate1);
        String newDate1 = datetime1.format(DateTimeFormatter.ofPattern(Constant.DD_MM_YYYY));
        infoGuiaRemision.setFechaFinTransporte(newDate1);

        infoGuiaRemision.setPlaca(rs.getString(Constant.PLACA));
        guiaRemision.setInfoGuiaRemision(infoGuiaRemision);

        /* Destinatarios */
        GuiaRemision.Destinatarios destinatarios = new GuiaRemision.Destinatarios();

        /* Detalles */
        Destinatario.Detalles detalles = new Destinatario.Detalles();

        String articuloSQL = Constant.GUIA_REMISION_ARTICULO_SQL
                + "NUM_DESPACHO_INTERNO = " + numDespachoInterno;
        String detalleSQL = Constant.GUIA_REMISION_D_SQL
                + "NUM_DESPACHO_INTERNO = ? AND COD_EMPRESA = ?";

        if (log.isTraceEnabled()) {
            log.trace("articuloSQL -> {}", articuloSQL);
            log.trace(Constant.DETALLE_SQL, detalleSQL);
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            try (PreparedStatement articuloPreparedStatement =
                         getDatasourceService().getConnection().prepareStatement(articuloSQL)) {
                ResultSet rsa = articuloPreparedStatement.executeQuery();
                while (rsa.next()) {
                    ec.gob.sri.comprobantes.modelo.guia.Detalle detalle =
                            new ec.gob.sri.comprobantes.modelo.guia.Detalle();
                    detalle.setCodigoInterno(rsa.getString(Constant.CODIGOINTERNO));
                    detalle.setDescripcion(rsa.getString(Constant.DESCRIPCION));
                    detalle.setCantidad(rsa.getBigDecimal(Constant.CANTIDAD));
                    detalles.getDetalle().add(detalle);
                }
            }
        } catch (SQLException | NamingException e) {
            log.log(Level.ERROR, e);
            return;
        } finally {
            closeConnections(connection, preparedStatement, resultSet);
        }

        connection = null;
        preparedStatement = null;
        resultSet = null;
        try {
            connection = getDatasourceService().getConnection();
            preparedStatement = connection.prepareStatement(detalleSQL);
            preparedStatement.setString(1, numDespachoInterno);
            preparedStatement.setString(2, codEmpresa);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {

                Destinatario destinatario = new Destinatario();
                destinatario.setIdentificacionDestinatario(
                        resultSet.getString(Constant.IDENTIFICACION_DESTINATARIO));
                destinatario.setDirDestinatario(resultSet.getString(Constant.DIRDESTINATARIO));
                destinatario.setMotivoTraslado(resultSet.getString(Constant.MOTIVOTRASLADO));
                destinatario.setDocAduaneroUnico(resultSet.getString(Constant.DOCADUANEROUNICO));
                destinatario.setCodEstabDestino(resultSet.getString(Constant.COD_ESTAB_DESTINO));
                destinatario.setRuta(resultSet.getString(Constant.RUTA));
                destinatario.setCodDocSustento(resultSet.getString(Constant.CODDOSUSTENTO));
                destinatario.setNumDocSustento(resultSet.getString(Constant.NUMDOCSUSTENTO));
                destinatario.setNumAutDocSustento(resultSet.getString(Constant.NUMAUTDOCSUSTENTO));
                String oldDate2 = resultSet.getString(Constant.FECHAEMISIONDOCSUSTENTO);
                if (oldDate2 != null) {
                    LocalDateTime datetime2 = transformDate(oldDate2);
                    String newDate2 = datetime2.format(DateTimeFormatter.ofPattern(Constant.DD_MM_YYYY));
                    destinatario.setFechaEmisionDocSustento(newDate2);
                }
                destinatario.setRazonSocialDestinatario(
                        resultSet.getString(Constant.RAZONSOCIALDESTINATARIO));
                destinatario.setDetalles(detalles);

                destinatarios.getDestinatario().add(destinatario);

                /* Información Adicional */
                GuiaRemision.InfoAdicional infoAdicional = new GuiaRemision.InfoAdicional();

                GuiaRemision.InfoAdicional.CampoAdicional telefono =
                        new GuiaRemision.InfoAdicional.CampoAdicional();
                telefono.setValue(resultSet.getString(Constant.TELDESTINATARIO));
                telefono.setNombre("TELEFONO");

                GuiaRemision.InfoAdicional.CampoAdicional email =
                        new GuiaRemision.InfoAdicional.CampoAdicional();
                email.setValue(resultSet.getString(Constant.MAILDESTINATARIO));
                email.setNombre(Constant.EMAIL);

                GuiaRemision.InfoAdicional.CampoAdicional sucursal =
                        new GuiaRemision.InfoAdicional.CampoAdicional();
                sucursal.setValue(resultSet.getString(Constant.DIRDESTINATARIO));
                sucursal.setNombre("DIRECCION");

                if (sucursal.getValue() != null && !sucursal.getValue().isEmpty()) {
                    infoAdicional.getCampoAdicional().add(sucursal);
                }

                addCampoAdicional(infoAdicional, sucursal);
                addCampoAdicional(infoAdicional, email);
                addCampoAdicional(infoAdicional, telefono);

                guiaRemision.setInfoAdicional(infoAdicional);
            }

            guiaRemision.setDestinatarios(destinatarios);

            comprobantes.add(guiaRemision);
        } catch (SQLException | NamingException e) {
            log.log(Level.ERROR, e);
            return;
        } finally {
            closeConnections(connection, preparedStatement, resultSet);
        }
    }

    protected void buildRetenciones(ResultSet rs, List<ComprobanteRetencion> comprobantes)
            throws SQLException, NamingException {
        String numRetencionInterno = rs.getString("NUM_RETENCION_INTERNO");

        ComprobanteRetencion comprobanteRetencion = new ComprobanteRetencion();
        comprobanteRetencion.setId(Constant.COMPROBANTE);
        ComprobanteRetencion.Impuestos impuestos = new ComprobanteRetencion.Impuestos();

        String detalleSQL = Constant.RETENCION_D_SQL
                + "NUM_RETENCION_INTERNO = ? AND COD_EMPRESA = ?";

        if (log.isTraceEnabled()) {
            log.trace(Constant.DETALLE_SQL, detalleSQL);
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = getDatasourceService().getConnection();
            preparedStatement = connection.prepareStatement(detalleSQL);
            preparedStatement.setString(1, numRetencionInterno);
            preparedStatement.setString(2, codEmpresa);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                ec.gob.sri.comprobantes.modelo.rentencion.Impuesto impuesto =
                        new ec.gob.sri.comprobantes.modelo.rentencion.Impuesto();
                impuesto.setBaseImponible(resultSet.getBigDecimal(Constant.BASEIMPONIBLE)
                        .setScale(2));
                impuesto.setCodDocSustento(resultSet.getString(Constant.CODDOCSUSTENTO));
                impuesto.setCodigo(resultSet.getString(Constant.CODIGO));
                impuesto.setCodigoRetencion(resultSet.getString(Constant.CODIGORETENCION));
                String oldDate = resultSet.getString(Constant.FECHAEMISIONDOCSUSTENTO);
                LocalDateTime datetime = transformDate(oldDate);
                String newDate = datetime.format(DateTimeFormatter.ofPattern(Constant.DD_MM_YYYY));
                impuesto.setFechaEmisionDocSustento(newDate);
                impuesto.setNumDocSustento(resultSet.getString(Constant.NUMDOCSUSTENTO));
                impuesto.setPorcentajeRetener(resultSet.getBigDecimal(Constant.PORCENTAJERETENR));
                impuesto.setValorRetenido(resultSet.getBigDecimal(Constant.VALORRETENIDO)
                        .setScale(2));
                impuestos.getImpuesto().add(impuesto);
            }
            comprobanteRetencion.setImpuestos(impuestos);

            ComprobanteRetencion.InfoAdicional infoAdicional =
                    new ComprobanteRetencion.InfoAdicional();
            ComprobanteRetencion.InfoAdicional.CampoAdicional direccion =
                    new ComprobanteRetencion.InfoAdicional.CampoAdicional();
            direccion.setNombre(Constant.DIRECCION_CC);
            direccion.setValue(rs.getString(Constant.DIRECCION_RETENIDO));
            ComprobanteRetencion.InfoAdicional.CampoAdicional telefono =
                    new ComprobanteRetencion.InfoAdicional.CampoAdicional();
            telefono.setValue(rs.getString(Constant.TELEFONO_RETENIDO));
            telefono.setNombre(Constant.TELEFONO_CC);
            ComprobanteRetencion.InfoAdicional.CampoAdicional email =
                    new ComprobanteRetencion.InfoAdicional.CampoAdicional();
            email.setValue(rs.getString(Constant.EMAIL_RETENIDO));
            email.setNombre(Constant.EMAIL);
            if (direccion.getValue() != null && !direccion.getValue().isEmpty()) {
                infoAdicional.getCampoAdicional().add(direccion);
            }
            if (telefono.getValue() != null && !telefono.getValue().isEmpty()) {
                infoAdicional.getCampoAdicional().add(telefono);
            }
            if (email.getValue() != null && !email.getValue().isEmpty()) {
                infoAdicional.getCampoAdicional().add(email);
            }
            comprobanteRetencion.setInfoAdicional(infoAdicional);

            ComprobanteRetencion.InfoCompRetencion infoCompRetencion =
                    new ComprobanteRetencion.InfoCompRetencion();
            infoCompRetencion.setContribuyenteEspecial(
                    rs.getString(Constant.CONTRIBUYENTE_ESPECIAL));
            infoCompRetencion.setDirEstablecimiento(
                    rs.getString(Constant.DIRECCION_ESTABLECIMIENTO));
            String oldDate = rs.getString(Constant.FECHA_RETENCION);
            LocalDateTime datetime = transformDate(oldDate);
            String newDate = datetime.format(DateTimeFormatter.ofPattern(Constant.DD_MM_YYYY));
            infoCompRetencion.setFechaEmision(newDate);
            infoCompRetencion.setIdentificacionSujetoRetenido(
                    rs.getString(Constant.IDENTIFICACION_SUJETO_RETENIDO));
            infoCompRetencion.setObligadoContabilidad(rs.getString(Constant.LLEVA_CONTABILIDAD));
            infoCompRetencion.setPeriodoFiscal(rs.getString(Constant.PERIODO_FISCAL));
            infoCompRetencion.setRazonSocialSujetoRetenido(
                    rs.getString(Constant.RAZON_SOCIAL_SUJETO_RETENIDO));
            infoCompRetencion.setTipoIdentificacionSujetoRetenido(
                    rs.getString(Constant.TIPO_IDENT_SUJETO_RETENIDO));
            comprobanteRetencion.setInfoCompRetencion(infoCompRetencion);

            InfoTributaria infoTributaria = new InfoTributaria();
            infoTributaria.setClaveAcceso(rs.getString(Constant.CLAVE_ACCESO));
            infoTributaria.setAmbiente(infoTributaria.getClaveAcceso().substring(23, 24));
            infoTributaria.setCodDoc(rs.getString(Constant.COD_DOCUMENTO));
            infoTributaria.setDirMatriz(rs.getString(Constant.DIRECCION_MATRIZ));
            infoTributaria.setEstab(rs.getString(Constant.ESTABLECIMIENTO));
            infoTributaria.setNombreComercial(rs.getString(Constant.NOMBRE_COMERCIAL));
            infoTributaria.setPtoEmi(rs.getString(Constant.PUNTO_EMISION));
            infoTributaria.setRazonSocial(rs.getString(Constant.RAZON_SOCIAL_EMPRESA));
            infoTributaria.setRuc(rs.getString(Constant.RUC_EMPRESA));
            infoTributaria.setSecuencial(rs.getString(Constant.SECUENCIAL));
            infoTributaria.setTipoEmision("1");
            infoTributaria.setRegimenMicroempresas(rs.getString(Constant.REGIMEN_MICROEMPRESAS));
            infoTributaria.setAgenteRetencion(rs.getString(Constant.AGENTE_RETENCION));
            comprobanteRetencion.setInfoTributaria(infoTributaria);
            comprobanteRetencion.setVersion("1.0.0");

            comprobantes.add(comprobanteRetencion);
        } catch (SQLException | NamingException e) {
            log.log(Level.ERROR, e);
            return;
        } finally {
            closeConnections(connection, preparedStatement, resultSet);
        }
    }

    protected IDatasourceService getDatasourceService() {
        try {
            if (Objects.isNull(datasourceService)) {
                InitialContext ic = new InitialContext();
                datasourceService = (IDatasourceService) ic.lookup("java:global/SIRE-EE/SIRE-Services/DatasourceService!com.sire.service.IDatasourceService");
            }
        } catch (NamingException e) {
            log.log(Level.ERROR, e);
        }
        return datasourceService;
    }

    protected String getDatabaseProductName() {
        try {
            if(Objects.isNull(databaseProductName)) {
                databaseProductName = getDatasourceService().getDatabaseProductName();
            }
        } catch (SQLException | NamingException e) {
            log.log(Level.ERROR, e);
        }
        return databaseProductName;
    }

    protected synchronized void validarTipoComprobante(String tipoComprobante, ResultSet rs,
                                                       List<?> comprobantes) {
        try {
            while (rs.next()) {
                switch (tipoComprobante) {
                    case Constant.CERO_UNO:
                        buildFacturas(rs, (List<Factura>) comprobantes);
                        break;
                    case Constant.CERO_TRES:
                        buildLiquidaciones(rs, (List<Liquidacion>) comprobantes);
                        break;
                    case Constant.CERO_CUATRO:
                        buildNotasCredito(rs, (List<NotaCredito>) comprobantes);
                        break;
                    case Constant.CERO_CINCO:
                        buildNotasDebito(rs, (List<NotaDebito>) comprobantes);
                        break;
                    case Constant.CERO_SEIS:
                        buildGuiasRemision(rs, (List<GuiaRemision>) comprobantes);
                        break;
                    case Constant.CERO_SIETE:
                        buildRetenciones(rs, (List<ComprobanteRetencion>) comprobantes);
                        break;
                    default:
                        break;
                }
            }
        } catch (SQLException | NamingException e){
            log.log(Level.ERROR, e);
        }
    }

    private LocalDateTime transformDate(CharSequence oldDate){
        LocalDateTime datetime;
        try {
            datetime = LocalDateTime.parse(oldDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"));
        } catch (java.time.format.DateTimeParseException dtpe) {
            datetime = LocalDateTime.parse(oldDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        return datetime;
    }

    protected void closeConnections(Connection connection, PreparedStatement preparedStatement,
                                    ResultSet resultSet) {
        CommonsItem.closeConnections(connection, preparedStatement, resultSet, null, null);
    }
}