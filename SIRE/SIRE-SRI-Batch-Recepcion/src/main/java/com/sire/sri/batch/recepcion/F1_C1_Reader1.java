package com.sire.sri.batch.recepcion;

import com.sire.sri.batch.commons.CommonsItemReader;
import com.sire.sri.batch.constant.Constant;
import com.sire.sri.batch.recepcion.constant.RecepcionConstant;
import org.apache.logging.log4j.Level;
import com.sire.logger.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.NamingException;

@Named
public class F1_C1_Reader1 extends CommonsItemReader {

    @Inject
    private JobContext jobCtx;
    private Iterator iterator;
    static int COUNT = 0;

    private static final Logger log = LogManager.getLogger(F1_C1_Reader1.class);

    @Override
    public Object readItem() {
        if (iterator != null && iterator.hasNext()) {
            COUNT++;
            return iterator.next();
        }
        return null;
    }

    @Override
    public void open(Serializable checkpoint) throws Exception {
        Properties runtimeParams = BatchRuntime.getJobOperator().getParameters(jobCtx.getExecutionId());
        String tipoComprobante = runtimeParams.getProperty(Constant.TIPO_COMPROBANTE);
        codEmpresa = runtimeParams.getProperty(Constant.COD_EMPRESA);

        log.info("tipoComprobante -> {}", tipoComprobante);
        log.info("codEmpresa -> {}", codEmpresa);

        if (codEmpresa != null && !codEmpresa.isEmpty()) {
            RecepcionConstant.codEmpresa = "COD_EMPRESA = '" + codEmpresa + "' AND ";
        }

        buildComprobantes(tipoComprobante);
    }

    private void buildComprobantes(String tipoComprobante) {
        String databaseProductName = getDatabaseProductName();

        log.info("databaseProductName -> {}", databaseProductName);
        List comprobantes = new ArrayList();

        String comprobanteSQL = null;

        switch (tipoComprobante) {
            case Constant.CERO_UNO:
                if(Constant.MYSQL.equals(databaseProductName))
                    comprobanteSQL = RecepcionConstant.FACTURA_SQL_MYSQL;
                else if(Constant.ORACLE.equals(databaseProductName))
                    comprobanteSQL = RecepcionConstant.FACTURA_SQL_ORACLE;
                else if(Constant.MICROSOFT_SQL_SERVER.equals(databaseProductName))
                    comprobanteSQL = RecepcionConstant.FACTURA_SQL_MICROSOFT_SQL_SERVER;
                break;
            case Constant.CERO_TRES:
                if(Constant.MYSQL.equals(databaseProductName))
                    comprobanteSQL = RecepcionConstant.LIQUIDACION_COMPRA_SQL_MYSQL;
                else if(Constant.ORACLE.equals(databaseProductName))
                    comprobanteSQL = RecepcionConstant.LIQUIDACION_COMPRA_SQL_ORACLE;
                else if(Constant.MICROSOFT_SQL_SERVER.equals(databaseProductName))
                    comprobanteSQL = RecepcionConstant.LIQUIDACION_COMPRA_SQL_MICROSOFT_SQL_SERVER;
                break;
            case Constant.CERO_CUATRO:
                if(Constant.MYSQL.equals(databaseProductName))
                    comprobanteSQL = RecepcionConstant.NOTA_CREDITO_SQL_MYSQL;
                else if(Constant.ORACLE.equals(databaseProductName))
                    comprobanteSQL = RecepcionConstant.NOTA_CREDITO_SQL_ORACLE;
                else if(Constant.MICROSOFT_SQL_SERVER.equals(databaseProductName))
                    comprobanteSQL = RecepcionConstant.NOTA_CREDITO_SQL_MICROSOFT_SQL_SERVER;
                break;
            case Constant.CERO_CINCO:
                if(Constant.MYSQL.equals(databaseProductName))
                    comprobanteSQL = RecepcionConstant.NOTA_DEBITO_SQL_MYSQL;
                else if(Constant.ORACLE.equals(databaseProductName))
                    comprobanteSQL = RecepcionConstant.NOTA_DEBITO_SQL_ORACLE;
                else if(Constant.MICROSOFT_SQL_SERVER.equals(databaseProductName))
                    comprobanteSQL = RecepcionConstant.NOTA_DEBITO_SQL_MICROSOFT_SQL_SERVER;
                break;
            case Constant.CERO_SEIS:
                comprobanteSQL = RecepcionConstant.GUIA_REMISION_SQL;
                break;
            case Constant.CERO_SIETE:
                if(Constant.MYSQL.equals(databaseProductName))
                    comprobanteSQL = RecepcionConstant.RETENCION_SQL_MYSQL;
                else if(Constant.ORACLE.equals(databaseProductName))
                    comprobanteSQL = RecepcionConstant.RETENCION_SQL_ORACLE;
                else if(Constant.MICROSOFT_SQL_SERVER.equals(databaseProductName))
                    comprobanteSQL = RecepcionConstant.RETENCION_SQL_MICROSOFT_SQL_SERVER;
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
            connection =  getDatasourceService().getConnection();
            preparedStatement = connection.prepareStatement(comprobanteSQL);
            preparedStatement.setString(1, codEmpresa);
            resultSet = preparedStatement.executeQuery();
            validarTipoComprobante(tipoComprobante, resultSet, comprobantes);
            iterator = comprobantes.iterator();
        } catch (SQLException | NamingException e) {
            log.log(Level.ERROR, e);
        } finally {
            closeConnections(connection, preparedStatement, resultSet);
        }
    }
}