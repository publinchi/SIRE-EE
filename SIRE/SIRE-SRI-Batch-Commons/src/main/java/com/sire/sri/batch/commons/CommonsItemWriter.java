package com.sire.sri.batch.commons;

import javax.batch.api.chunk.AbstractItemWriter;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.*;

import com.sire.service.IDatasourceService;
import com.sun.xml.bind.marshaller.DataWriter;
import com.sire.sri.batch.util.JaxbCharacterEscapeHandler;
import org.apache.logging.log4j.Level;
import com.sire.logger.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class CommonsItemWriter extends AbstractItemWriter {

    public abstract JAXBContext getContextInstance(Class objectClass) throws JAXBException;

    private static final Logger log = LogManager.getLogger(CommonsItemWriter.class);

    protected String object2xmlUnicode(Object item, Class clazz, String pck, String name) throws JAXBException {
        JAXBContext jaxbContext = getContextInstance(item.getClass());
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        DataWriter dataWriter = new DataWriter(printWriter, "UTF-8", new JaxbCharacterEscapeHandler());

        if(clazz != null && pck != null && name != null){
            QName qName = new QName(pck, name);
            JAXBElement root = new JAXBElement<>(qName, clazz, item);
            jaxbMarshaller.marshal(root, dataWriter);
        } else
            jaxbMarshaller.marshal(item, dataWriter);

        return stringWriter.toString();
    }

    protected void executeSql(String sql, String... params){
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try{
            connection = getConnection();
            preparedStatement = connection.prepareStatement(sql);
            int i=1;
            for (String param: params) {
                preparedStatement.setString(i, param);
                i++;
            }
            preparedStatement.executeUpdate();
        } catch (SQLException | NamingException e) {
            log.log(Level.ERROR, e);
        } finally {
            closeConnections(connection, preparedStatement, null, null, null);
        }
    }

    protected Connection getConnection() throws SQLException, NamingException {
        InitialContext ic = new InitialContext();
        IDatasourceService datasourceService = (IDatasourceService) ic.lookup("java:global/SIRE-EE/SIRE-Services/DatasourceService!com.sire.service.IDatasourceService");
        Connection connection = datasourceService.getConnection();
        return connection;
    }

    protected void closeConnections(Connection connection, PreparedStatement preparedStatement, ResultSet resultSet,
                                    CallableStatement callableStatement, Statement statement) {
        CommonsItem.closeConnections(connection, preparedStatement, resultSet, callableStatement, statement);
    }
}
