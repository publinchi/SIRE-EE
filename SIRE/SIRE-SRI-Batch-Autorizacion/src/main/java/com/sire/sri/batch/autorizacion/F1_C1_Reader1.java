package com.sire.sri.batch.autorizacion;

import com.sire.sri.batch.constant.Constant;
import ec.gob.sri.comprobantes.modelo.LoteXml;
import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.inject.Named;
import com.sire.sri.batch.autorizacion.constant.AutorizacionConstant;
import com.sire.sri.batch.commons.CommonsItemReader;

import java.util.Properties;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;
import javax.naming.NamingException;

import org.apache.logging.log4j.Level;
import com.sire.logger.LogManager;
import org.apache.logging.log4j.Logger;

@Named
public class F1_C1_Reader1 extends CommonsItemReader {

    @Inject
    private JobContext jobCtx;
    private List<LoteXml> lotes;
    private Iterator<LoteXml> iterator;
    static int count = 0;

    private static final Logger log = LogManager.getLogger(F1_C1_Reader1.class);

    @Override
    public Object readItem() {
        if (iterator != null && iterator.hasNext()) {
            LoteXml lote = iterator.next();
            count++;
            return lote;
        }
        return null;
    }

    @Override
    public void open(Serializable checkpoint) throws Exception {
        Properties runtimeParams = BatchRuntime.getJobOperator().getParameters(jobCtx.getExecutionId());
        String tipoComprobante = runtimeParams.getProperty("tipoComprobante");
        codEmpresa = runtimeParams.getProperty("codEmpresa");
        lotes = new ArrayList<>();

        log.log(Level.INFO, "tipoComprobante -> {}, codEmpresa -> {}", tipoComprobante, codEmpresa);

        if (codEmpresa != null && !codEmpresa.isEmpty()) {
            AutorizacionConstant.codEmpresa = "COD_EMPRESA = '" + codEmpresa + "' AND ";
        }

        StringBuilder loteSQL = new StringBuilder();

        String databaseProductName = getDatabaseProductName();

        String subString = null;

        if(Constant.MYSQL.equals(databaseProductName))
            subString = "SUBSTR";
        else if(Constant.ORACLE.equals(databaseProductName))
            subString = "SUBSTR";
        else if(Constant.MICROSOFT_SQL_SERVER.equals(databaseProductName))
            subString = "SUBSTRING";

        if(Constant.MYSQL.equals(databaseProductName))
            loteSQL.append("SELECT COD_EMPRESA, SECUENCIAL, COD_DOCUMENTO, ")
                    .append("CLAVE_ACCESO, ESTADO_SRI, FECHA_ESTADO ")
                    .append("FROM CEL_LOTE_AUTORIZADO WHERE COD_EMPRESA = ? AND ESTADO_SRI = 'RECIBIDA' ")
                    .append("AND ").append(subString).append("(CLAVE_ACCESO,9,2) = ? ")
                    .append("LIMIT 1");
        else if(Constant.ORACLE.equals(databaseProductName))
            loteSQL.append("SELECT COD_EMPRESA, SECUENCIAL, COD_DOCUMENTO, ")
                    .append("CLAVE_ACCESO, ESTADO_SRI, FECHA_ESTADO ")
                    .append("FROM CEL_LOTE_AUTORIZADO WHERE COD_EMPRESA = ? AND ESTADO_SRI = 'RECIBIDA' ")
                    .append("AND ").append(subString).append("(CLAVE_ACCESO,9,2) = ? AND ROWNUM <= 1");

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try{
            connection = getDatasourceService().getConnection();
            preparedStatement = connection.prepareStatement(loteSQL.toString());
            resultSet = getResultSet(tipoComprobante, preparedStatement);
            while (resultSet.next()) {
                String claveAccesoLote = resultSet.getString("CLAVE_ACCESO");
                buildComprobantes(claveAccesoLote, tipoComprobante);
            }
            iterator = lotes.iterator();
        } catch (SQLException | NamingException e){
            log.log(Level.ERROR, e);
        } finally {
            closeConnections(connection, preparedStatement, resultSet);
        }
    }

    private void buildComprobantes(String claveAccesoLote, String tipoComprobante) {
        String databaseProductName = getDatabaseProductName();
        List<Object> comprobantes = new ArrayList<>();
        LoteXml lote = new LoteXml();
        lote.setClaveAcceso(claveAccesoLote);
        String comprobanteSQL = null;

        log.log(Level.INFO, "tipoComprobante -> {}", tipoComprobante);

        switch (tipoComprobante) {
            case "01":
                comprobanteSQL = AutorizacionConstant.FACTURA_SQL;
                break;
            case "03":
                comprobanteSQL = AutorizacionConstant.LIQUIDACION_COMPRA_SQL;
                break;
            case "04":
                if(Constant.MYSQL.equals(databaseProductName))
                    comprobanteSQL = AutorizacionConstant.NOTA_CREDITO_SQL_MYSQL;
                else if(Constant.ORACLE.equals(databaseProductName))
                    comprobanteSQL = AutorizacionConstant.NOTA_CREDITO_SQL_ORACLE;
                else if(Constant.MICROSOFT_SQL_SERVER.equals(databaseProductName))
                    comprobanteSQL = AutorizacionConstant.NOTA_CREDITO_SQL_MICROSOFT_SQL_SERVER;
                break;
            case "05":
                if(Constant.MYSQL.equals(databaseProductName))
                    comprobanteSQL = AutorizacionConstant.NOTA_DEBITO_SQL_MYSQL;
                else if(Constant.ORACLE.equals(databaseProductName))
                    comprobanteSQL = AutorizacionConstant.NOTA_DEBITO_SQL_ORACLE;
                else if(Constant.MICROSOFT_SQL_SERVER.equals(databaseProductName))
                    comprobanteSQL = AutorizacionConstant.NOTA_DEBITO_SQL_MICROSOFT_SQL_SERVER;
                break;
            case "06":
                comprobanteSQL = AutorizacionConstant.GUIA_REMISION_SQL;
                break;
            case "07":
                comprobanteSQL = AutorizacionConstant.RETENCION_SQL;
                break;
            default:
                throw new RuntimeException("No se ha encontrado ninguna sentencia sql "
                        + "asociada al tipo de comprobante " + tipoComprobante);
        }

        log.log(Level.INFO, "comprobanteSQL -> {}", comprobanteSQL);

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try{
            connection = getDatasourceService().getConnection();
            preparedStatement = connection.prepareStatement(comprobanteSQL);
            resultSet = getResultSet(claveAccesoLote, preparedStatement);
            validarTipoComprobante(tipoComprobante, resultSet, comprobantes);
            lote.setComprobantes(comprobantes);
            lotes.add(lote);
        } catch (SQLException | NamingException e) {
            log.log(Level.ERROR, e);
        } finally {
            closeConnections(connection, preparedStatement, resultSet);
        }
    }

    private ResultSet getResultSet(String claveAccesoLote, PreparedStatement comprobantePreparedStatement) throws SQLException {
        comprobantePreparedStatement.setString(1, codEmpresa);
        comprobantePreparedStatement.setString(2, claveAccesoLote);
        return comprobantePreparedStatement.executeQuery();
    }
}