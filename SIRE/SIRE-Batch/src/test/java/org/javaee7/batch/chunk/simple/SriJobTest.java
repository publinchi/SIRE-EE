package org.javaee7.batch.chunk.simple;

import java.io.File;
import static javax.batch.runtime.BatchRuntime.getJobOperator;
import static javax.batch.runtime.BatchStatus.COMPLETED;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.batch.operations.JobOperator;
import javax.batch.runtime.JobExecution;
import javax.batch.runtime.Metric;
import javax.batch.runtime.StepExecution;

import org.javaee7.util.BatchTestHelper;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * The Batch specification provides a Chunk Oriented processing style. This
 * style is defined by enclosing into a transaction a set of reads, process and
 * write operations via +javax.batch.api.chunk.ItemReader+,
 * +javax.batch.api.chunk.ItemProcessor+ and +javax.batch.api.chunk.ItemWriter+.
 * Items are read one at a time, processed and aggregated. The transaction is
 * then committed when the defined +checkpoint-policy+ is triggered.
 *
 * include::myJob.xml[]
 *
 * A very simple job is defined in the +myJob.xml+ file. Just a single step with
 * a reader, a processor and a writer.
 *
 * @author Roberto Cortez
 */
//@RunWith(Arquillian.class)
public class SriJobTest {

    private Logger log = Logger.getLogger(SriJobTest.class.getName());
    private static Logger logger = Logger.getLogger(SriJobTest.class.getName());

    /**
     * We're just going to deploy the application as a +web archive+. Note the
     * inclusion of the following files:
     *
     * [source,file] ---- /META-INF/batch-jobs/myJob.xml ----
     *
     * The +myJob.xml+ file is needed for running the batch definition.
     */
  //  @Deployment(name = "SIRE-Batch")
    public static WebArchive createDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "SIRE-Batch.war")
                .addClass(BatchTestHelper.class)
                .addPackage("com.sire.sri.batch.recepcion")
                .addPackage("com.sire.service")
                .addPackage("com.sire.event")
                .addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"))
                .addAsResource(new File("/home/pestupinan/NetBeansProjects/SIRE/sire-web/SIRE/SIRE-SRI-Batch-Recepcion/src/main/resources/META-INF/batch-jobs/SriRecepcionJob.xml"))
                .addAsResource(new File("/home/pestupinan/NetBeansProjects/SIRE/sire-web/SIRE/SIRE-SRI-Batch-Autorizacion/src/main/resources/META-INF/batch-jobs/SriAutorizacionJob.xml"))
                .addAsLibraries(
                        new File("/home/pestupinan/.m2/repository/es/mityc/jumbo/adsi/DNIeJCAProvider/1.4/DNIeJCAProvider-1.4.jar"),
                        new File("/home/pestupinan/.m2/repository/es/mityc/jumbo/adsi/MITyCLibAPI/1.1.7/MITyCLibAPI-1.1.7.jar"),
                        new File("/home/pestupinan/.m2/repository/es/mityc/jumbo/adsi/MITyCLibCert/1.1.7/MITyCLibCert-1.1.7.jar"),
                        new File("/home/pestupinan/.m2/repository/es/mityc/jumbo/adsi/MITyCLibCrypt/1.1.7/MITyCLibCrypt-1.1.7.jar"),
                        new File("/home/pestupinan/.m2/repository/es/mityc/jumbo/adsi/MITyCLibOCSP/1.1.7/MITyCLibOCSP-1.1.7.jar"),
                        new File("/home/pestupinan/.m2/repository/es/mityc/jumbo/adsi/MITyCLibPolicy/1.1.7/MITyCLibPolicy-1.1.7.jar"),
                        new File("/home/pestupinan/.m2/repository/es/mityc/jumbo/adsi/MITyCLibTSA/1.1.7/MITyCLibTSA-1.1.7.jar"),
                        new File("/home/pestupinan/.m2/repository/es/mityc/jumbo/adsi/MITyCLibTrust/1.1.7/MITyCLibTrust-1.1.7.jar"),
                        new File("/home/pestupinan/.m2/repository/es/mityc/jumbo/adsi/MITyCLibXADES/1.1.7/MITyCLibXADES-1.1.7.jar"),
                        new File("/home/pestupinan/.m2/repository/org/bouncycastle/bcmail-jdk16/1.45/bcmail-jdk16-1.45.jar"),
                        new File("/home/pestupinan/.m2/repository/org/bouncycastle/bcprov-jdk16/1.45/bcprov-jdk16-1.45.jar"),
                        new File("/home/pestupinan/.m2/repository/org/bouncycastle/bctsp-jdk16/1.45/bctsp-jdk16-1.45.jar"),
                        new File("/home/pestupinan/.m2/repository/commons-logging/commons-logging/1.1.1/commons-logging-1.1.1.jar"),
                        new File("/home/pestupinan/.m2/repository/es/mityc/jumbo/adsi/xmlsec/1.1/xmlsec-1.1.jar"),
                        new File("/home/pestupinan/.m2/repository/com/sire/SIRE-Soap/1.0.0/SIRE-Soap-1.0.0.jar"),
                        new File("/home/pestupinan/.m2/repository/com/sire/sri/SIRE-SRI-WSClient/1.0.0/SIRE-SRI-WSClient-1.0.0.jar"),
                        new File("/home/pestupinan/.m2/repository/com/sire/sri/SIRE-SRI-Entities/1.0.0/SIRE-SRI-Entities-1.0.0.jar"),
                        new File("/home/pestupinan/.m2/repository/com/sire/sri/SIRE-SRI-Signature/1.0.0/SIRE-SRI-Signature-1.0.0.jar"),
                        new File("/home/pestupinan/.m2/repository/com/sire/sri/SIRE-SRI-Batch-Recepcion/1.0.0/SIRE-SRI-Batch-Recepcion-1.0.0.jar"),
                        new File("/home/pestupinan/.m2/repository/com/sire/sri/SIRE-SRI-Batch-Autorizacion/1.0.0/SIRE-SRI-Batch-Autorizacion-1.0.0.jar"),
                        new File("/home/pestupinan/.m2/repository/dom4j/dom4j/1.6.1/dom4j-1.6.1.jar"),
                        new File("/home/pestupinan/.m2/repository/com/sun/xml/bind/jaxb-core/2.3.0/jaxb-core-2.3.0.jar"),
                        new File("/home/pestupinan/.m2/repository/com/sire/sri/SIRE-SRI-Reports/1.0.0/SIRE-SRI-Reports-1.0.0.jar"),
                        new File("/home/pestupinan/.m2/repository/net/sf/jasperreports/jasperreports/5.6.1/jasperreports-5.6.1.jar"),
                        new File("/home/pestupinan/.m2/repository/org/hsqldb/hsqldb/2.4.0/hsqldb-2.4.0.jar"),
                        new File("/home/pestupinan/.m2/repository/net/sf/barcode4j/barcode4j/2.1/barcode4j-2.1.jar"),
                        new File("/home/pestupinan/.m2/repository/commons-collections/commons-collections/3.2.1/commons-collections-3.2.1.jar"),
                        new File("/home/pestupinan/.m2/repository/commons-digester/commons-digester/2.1/commons-digester-2.1.jar"),
                        new File("/home/pestupinan/.m2/repository/org/codehaus/groovy/groovy-all/2.4.3/groovy-all-2.4.3.jar"),
                        new File("/home/pestupinan/.m2/repository/commons-beanutils/commons-beanutils/1.8.0/commons-beanutils-1.8.0.jar"),
                        new File("/home/pestupinan/.m2/repository/org/apache/xmlgraphics/batik-bridge/1.7/batik-bridge-1.7.jar"),
                        new File("/home/pestupinan/.m2/repository/org/apache/xmlgraphics/batik-dom/1.7/batik-dom-1.7.jar"),
                        new File("/home/pestupinan/.m2/repository/org/apache/xmlgraphics/batik-svg-dom/1.7/batik-svg-dom-1.7.jar"),
                        new File("/home/pestupinan/.m2/repository/com/lowagie/itext/2.1.7.js2/itext-2.1.7.js2.jar"),
                        new File("/home/pestupinan/.m2/repository/org/apache/xmlgraphics/batik-util/1.7/batik-util-1.7.jar"),
                        new File("/home/pestupinan/.m2/repository/org/apache/xmlgraphics/batik-css/1.7/batik-css-1.7.jar"),
                        new File("/home/pestupinan/.m2/repository/xml-apis/xml-apis-ext/1.3.04/xml-apis-ext-1.3.04.jar"),
                        new File("/home/pestupinan/.m2/repository/xml-apis/xml-apis/1.4.01/xml-apis-1.4.01.jar"),
                        new File("/home/pestupinan/.m2/repository/org/apache/xmlgraphics/batik-xml/1.7/batik-xml-1.7.jar"),
                        new File("/home/pestupinan/.m2/repository/org/apache/xmlgraphics/batik-parser/1.7/batik-parser-1.7.jar"),
                        new File("/home/pestupinan/.m2/repository/org/apache/xmlgraphics/batik-anim/1.7/batik-anim-1.7.jar"),
                        new File("/home/pestupinan/.m2/repository/org/apache/xmlgraphics/batik-script/1.7/batik-script-1.7.jar"),
                        new File("/home/pestupinan/.m2/repository/org/apache/xmlgraphics/batik-awt-util/1.7/batik-awt-util-1.7.jar"),
                        new File("/home/pestupinan/.m2/repository/org/apache/xmlgraphics/batik-gvt/1.7/batik-gvt-1.7.jar"));

        logger.info(war.toString(true));
        return war;
    }

    /**
     * In the test, we're just going to invoke the batch execution and wait for
     * completion. To validate the test expected behaviour we need to query the
     * +javax.batch.runtime.Metric+ object available in the step execution.
     *
     * The batch process itself will read and process 10 elements from numbers 1
     * to 10, but only write the odd elements. Commits are executed after 3
     * elements are read.
     *
     * @throws Exception an exception if the batch could not complete
     * successfully.
     */
    //@Test
    public void testSriRecepcionJob() throws Exception {
        Properties runtimeParameters = new Properties();
        runtimeParameters.setProperty("pathSignature", "/opt/payara41/SIRE/keystore.p12");
        runtimeParameters.setProperty("passSignature", "Charlie2011");

        JobOperator jobOperator = getJobOperator();
        Long executionId = jobOperator.start("SriRecepcionJob", runtimeParameters);
        JobExecution jobExecution = jobOperator.getJobExecution(executionId);

        BatchTestHelper.keepTestAlive(jobExecution);

        List<StepExecution> stepExecutions = jobOperator.getStepExecutions(executionId);
        for (StepExecution stepExecution : stepExecutions) {
            if (stepExecution.getStepName().equals("f1_chunk1")) {
                Map<Metric.MetricType, Long> metricsMap = BatchTestHelper.getMetricsMap(stepExecution.getMetrics());
                log.info("READ_COUNT: " + metricsMap.get(Metric.MetricType.READ_COUNT).longValue());
//                // <1> The read count should be 10 elements. Check +MyItemReader+.
//                assertEquals(10L, metricsMap.get(Metric.MetricType.READ_COUNT).longValue());
                assertEquals(4L, metricsMap.get(Metric.MetricType.READ_COUNT).longValue());
//
                log.info("WRITE_COUNT: " + metricsMap.get(Metric.MetricType.WRITE_COUNT).longValue());
//                // <2> The write count should be 5. Only half of the elements read are processed to be written.
//                assertEquals(10L / 2L, metricsMap.get(Metric.MetricType.WRITE_COUNT).longValue());
                assertEquals(4L, metricsMap.get(Metric.MetricType.WRITE_COUNT).longValue());
//                
                log.info("COMMIT_COUNT: " + metricsMap.get(Metric.MetricType.COMMIT_COUNT).longValue());
//                // <3> The commit count should be 4. Checkpoint is on every 3rd read, 4 commits for read elements.
//                assertEquals(10L / 3 + (10L % 3 > 0 ? 1 : 0),
//                    metricsMap.get(Metric.MetricType.COMMIT_COUNT).longValue());
                assertEquals(1L, metricsMap.get(Metric.MetricType.COMMIT_COUNT).longValue());
            }
        }

        // <4> Job should be completed.
        assertEquals(jobExecution.getBatchStatus(), COMPLETED);
    }

    /**
     * In the test, we're just going to invoke the batch execution and wait for
     * completion. To validate the test expected behaviour we need to query the
     * +javax.batch.runtime.Metric+ object available in the step execution.
     *
     * The batch process itself will read and process 10 elements from numbers 1
     * to 10, but only write the odd elements. Commits are executed after 3
     * elements are read.
     *
     * @throws Exception an exception if the batch could not complete
     * successfully.
     */
    //@Test
    public void testSriAutorizacionJob() throws Exception {
        JobOperator jobOperator = getJobOperator();
        Long executionId = jobOperator.start("SriAutorizacionJob", null);
        JobExecution jobExecution = jobOperator.getJobExecution(executionId);

        BatchTestHelper.keepTestAlive(jobExecution);

        List<StepExecution> stepExecutions = jobOperator.getStepExecutions(executionId);
        for (StepExecution stepExecution : stepExecutions) {
            if (stepExecution.getStepName().equals("f1_chunk1")) {
                Map<Metric.MetricType, Long> metricsMap = BatchTestHelper.getMetricsMap(stepExecution.getMetrics());
                log.info("READ_COUNT: " + metricsMap.get(Metric.MetricType.READ_COUNT).longValue());
//                // <1> The read count should be 10 elements. Check +MyItemReader+.
//                assertEquals(10L, metricsMap.get(Metric.MetricType.READ_COUNT).longValue());
                assertEquals(4L, metricsMap.get(Metric.MetricType.READ_COUNT).longValue());
//
                log.info("WRITE_COUNT: " + metricsMap.get(Metric.MetricType.WRITE_COUNT).longValue());
//                // <2> The write count should be 5. Only half of the elements read are processed to be written.
//                assertEquals(10L / 2L, metricsMap.get(Metric.MetricType.WRITE_COUNT).longValue());
                assertEquals(4L, metricsMap.get(Metric.MetricType.WRITE_COUNT).longValue());
//                
                log.info("COMMIT_COUNT: " + metricsMap.get(Metric.MetricType.COMMIT_COUNT).longValue());
//                // <3> The commit count should be 4. Checkpoint is on every 3rd read, 4 commits for read elements.
//                assertEquals(10L / 3 + (10L % 3 > 0 ? 1 : 0),
//                    metricsMap.get(Metric.MetricType.COMMIT_COUNT).longValue());
                assertEquals(1L, metricsMap.get(Metric.MetricType.COMMIT_COUNT).longValue());
            }
        }

        // <4> Job should be completed.
        assertEquals(jobExecution.getBatchStatus(), COMPLETED);
    }
}
