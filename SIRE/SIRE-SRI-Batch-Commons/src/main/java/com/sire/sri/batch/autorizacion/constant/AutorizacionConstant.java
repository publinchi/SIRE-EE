/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sire.sri.batch.autorizacion.constant;

import com.sire.sri.batch.constant.Constant;

/**
 *
 * @author pestupinan
 */
public class AutorizacionConstant {

    public static String codEmpresa = "COD_EMPRESA = ? AND ";

    public static String FACTURA_SQL = Constant.FACTURA_C_SQL
            + "V_FACTURA_ELECTRONICA_C "
            + "WHERE " + codEmpresa
            + "CLAVE_ACCESO_LOTE = ? AND (ESTADO_SRI='RECIBIDA' "
            + "OR ESTADO_SRI='EN PROCESAMIENTO') ";

    public static String LIQUIDACION_COMPRA_SQL = Constant.LIQUIDACION_COMPRA_C_SQL
            + "V_LIQUIDACION_COMPRA_C "
            + "WHERE " + codEmpresa
            + "CLAVE_ACCESO_LOTE = ? AND (ESTADO_SRI='RECIBIDA' "
            + "OR ESTADO_SRI='EN PROCESAMIENTO') ";

    public static String RETENCION_SQL = Constant.RETENCION_C_SQL_ORACLE
            + codEmpresa + "CLAVE_ACCESO_LOTE = ? AND "
            + "(ESTADO_SRI='RECIBIDA' OR ESTADO_SRI='EN PROCESAMIENTO') ";

    public static String NOTA_CREDITO_SQL_MYSQL = Constant.NOTA_CREDITO_C_SQL_MYSQL
            + codEmpresa + "CLAVE_ACCESO_LOTE = ? AND "
            + "(ESTADO_SRI='RECIBIDA' OR ESTADO_SRI='EN PROCESAMIENTO') ";

    public static String NOTA_CREDITO_SQL_ORACLE = Constant.NOTA_CREDITO_C_SQL_ORACLE
            + codEmpresa + "CLAVE_ACCESO_LOTE = ? AND "
            + "(ESTADO_SRI='RECIBIDA' OR ESTADO_SRI='EN PROCESAMIENTO') ";

    public static String NOTA_CREDITO_SQL_MICROSOFT_SQL_SERVER = Constant.NOTA_CREDITO_C_SQL_MICROSOFT_SQL_SERVER
            + codEmpresa + "CLAVE_ACCESO_LOTE = ? AND "
            + "(ESTADO_SRI='RECIBIDA' OR ESTADO_SRI='EN PROCESAMIENTO') ";

    public static String NOTA_DEBITO_SQL_MYSQL = Constant.NOTA_DEBITO_C_SQL_MYSQL
            + codEmpresa + "CLAVE_ACCESO_LOTE = ? AND "
            + "(ESTADO_SRI='RECIBIDA' OR ESTADO_SRI='EN PROCESAMIENTO') ";

    public static String NOTA_DEBITO_SQL_ORACLE = Constant.NOTA_DEBITO_C_SQL_ORACLE
            + codEmpresa + "CLAVE_ACCESO_LOTE = ? AND "
            + "(ESTADO_SRI='RECIBIDA' OR ESTADO_SRI='EN PROCESAMIENTO') ";

    public static String NOTA_DEBITO_SQL_MICROSOFT_SQL_SERVER = Constant.NOTA_DEBITO_C_SQL_MICROSOFT_SQL_SERVER
            + codEmpresa + "CLAVE_ACCESO_LOTE = ? AND "
            + "(ESTADO_SRI='RECIBIDA' OR ESTADO_SRI='EN PROCESAMIENTO') ";

    public static String GUIA_REMISION_SQL = Constant.GUIA_REMISION_C_SQL
            + codEmpresa + "CLAVE_ACCESO_LOTE = ? AND "
            + "(ESTADO_SRI='RECIBIDA' OR ESTADO_SRI='EN PROCESAMIENTO') ";
}
