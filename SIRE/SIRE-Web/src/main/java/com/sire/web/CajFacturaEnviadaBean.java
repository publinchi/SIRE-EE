/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sire.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sire.entities.CajFacturaEnviada;
import com.sire.entities.CajFacturaEnviadaPK;
import com.sire.entities.CajRubro;
import com.sire.entities.PryProyecto;
import com.sire.entities.PrySubproyecto;
import com.sire.entities.PrySupervisorUsuario;
import com.sire.errorhandling.ErrorMessage;
import com.sire.rs.client.CajFacturaEnviadaFacadeREST;
import com.sire.rs.client.CajRubroFacadeREST;
import com.sire.rs.client.PryProyectoFacadeREST;
import com.sire.rs.client.PrySupervisorUsuarioFacadeREST;
import com.sire.utils.Round;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.ws.rs.core.Response;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FilenameUtils;
import org.primefaces.model.UploadedFile;

/**
 *
 * @author publio
 */
@ManagedBean(name = "cajFacturaEnviadaBean")
@SessionScoped
@Getter
@Setter
public class CajFacturaEnviadaBean {

    private static final Logger LOGGER = Logger.getLogger(CajFacturaEnviadaBean.class.getName());

    private UploadedFile file;
    private String cliente;
    private CajFacturaEnviada cajFacturaEnviada;
    @ManagedProperty(value = "#{user}")
    private UserManager userManager;
    private List<PryProyecto> proyectos;
    private List<PrySubproyecto> subProyectos;
    private List<CajRubro> rubros;
    private final Gson gson;
    private CajFacturaEnviadaFacadeREST cajFacturaEnviadaFacadeREST;
    private PryProyectoFacadeREST pryProyectoFacadeREST;
    private CajRubroFacadeREST cajRubroFacadeREST;
    private PrySupervisorUsuarioFacadeREST prySupervisorUsuarioFacadeREST;
    private Double iva;
    private Date maxDate;
//    @Past
    private Date fechaDocumento;
    private String fileName;

    public CajFacturaEnviadaBean() {
        GsonBuilder builder = new GsonBuilder();
        gson = builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
        pryProyectoFacadeREST = new PryProyectoFacadeREST();
        cajRubroFacadeREST = new CajRubroFacadeREST();
        cajFacturaEnviadaFacadeREST = new CajFacturaEnviadaFacadeREST();
        prySupervisorUsuarioFacadeREST = new PrySupervisorUsuarioFacadeREST();
        CajFacturaEnviadaPK cajFacturaEnviadaPK = new CajFacturaEnviadaPK();
        this.cajFacturaEnviada = new CajFacturaEnviada();
        this.cajFacturaEnviada.setCajFacturaEnviadaPK(cajFacturaEnviadaPK);
    }

    public void enviar() {
        if (!file.getFileName().isEmpty()) {
            LOGGER.log(Level.INFO, "file: {0}", file.getFileName());
            fileName = String.valueOf(Calendar.getInstance().getTimeInMillis()) + "." + FilenameUtils.getExtension(file.getFileName());
        } else {
            addMessage("Advertencia", "Imágen requerida.", FacesMessage.SEVERITY_WARN);
            FacesContext context = FacesContext.getCurrentInstance();
            context.getExternalContext().getFlash().setKeepMessages(true);
            return;
        }

        LOGGER.log(Level.INFO, "cajFacturaEnviada: {0}", cajFacturaEnviada);

        cajFacturaEnviada.getCajFacturaEnviadaPK().setCodEmpresa(obtenerEmpresa());
        cajFacturaEnviada.getCajFacturaEnviadaPK().setSecuencial(null);
        cajFacturaEnviada.setFechaEstado(Calendar.getInstance().getTime());
        cajFacturaEnviada.setIdFoto(fileName);
        cajFacturaEnviada.setNombreUsuario(userManager.getCurrent());
        cajFacturaEnviada.getCajFacturaEnviadaPK().setCodSupervisor(obtenerPrySupervisorUsuario().getPrySupervisor().getPrySupervisorPK().getCodSupervisor());
        cajFacturaEnviada.setEstado("G");
        cajFacturaEnviada.setFechaDocumento(fechaDocumento);
        Response response = cajFacturaEnviadaFacadeREST.save_JSON(cajFacturaEnviada);
        if (response.getStatus() == 200) {
            savePicture();
            addMessage("Factura enviada exitosamente.", "Num. Factura: " + cajFacturaEnviada.getCajFacturaEnviadaPK().getNumDocumento(), FacesMessage.SEVERITY_INFO);
            FacesContext context = FacesContext.getCurrentInstance();
            context.getExternalContext().getFlash().setKeepMessages(true);
        } else if (response.getStatus() == 404) {
            savePicture();
            String developerMessage = response.readEntity(ErrorMessage.class).getDeveloperMessage();
            LOGGER.log(Level.SEVERE, developerMessage);
            addMessage("Advertencia", developerMessage, FacesMessage.SEVERITY_WARN);
            FacesContext context = FacesContext.getCurrentInstance();
            context.getExternalContext().getFlash().setKeepMessages(true);
        }
        limpiar();
    }

    public void findProyectos() {
        LOGGER.info("findProyectos()");
        String codEmpresa = obtenerEmpresa();
        LOGGER.log(Level.INFO, "codEmpresa: {0}", codEmpresa);
        if ((codEmpresa != null && proyectos == null) || (codEmpresa != null && proyectos.isEmpty())) {
            String proyectosString = pryProyectoFacadeREST.findByCodEmpresa_JSON(String.class, obtenerEmpresa());
            proyectos = gson.fromJson(proyectosString, new TypeToken<java.util.List<PryProyecto>>() {
            }.getType());
        }
    }

    public List<PryProyecto> getProyectos() {
        findProyectos();
        return proyectos;
    }

    public void findSubProyecto() {
        LOGGER.log(Level.INFO, "findSubProyecto()");
        LOGGER.log(Level.INFO, "CodProyecto: {0}", cajFacturaEnviada.getCajFacturaEnviadaPK().getCodProyecto());
        String subProyectosString = pryProyectoFacadeREST.findSubByCodProyectoCodEmpresa_JSON(String.class, String.valueOf(cajFacturaEnviada.getCajFacturaEnviadaPK().getCodProyecto()), obtenerEmpresa());
        subProyectos = gson.fromJson(subProyectosString, new TypeToken<java.util.List<PrySubproyecto>>() {
        }.getType());
    }

    public void findRubros() {
        LOGGER.info("findRubros()");
        String codEmpresa = obtenerEmpresa();
        LOGGER.log(Level.INFO, "codEmpresa: {0}", codEmpresa);
        if ((codEmpresa != null && rubros == null) || (codEmpresa != null && rubros.isEmpty())) {
            String rubrosString = cajRubroFacadeREST.cajRubroByCodEmpresa(String.class, obtenerEmpresa());
            rubros = gson.fromJson(rubrosString, new TypeToken<java.util.List<CajRubro>>() {
            }.getType());
        }
    }

    public List<CajRubro> getRubros() {
        findRubros();
        return rubros;
    }

    public void calcularTotalDocumento() {
        LOGGER.log(Level.INFO, "iva: {0}", iva);
        if (iva.equals(0.0) || cajFacturaEnviada.getTotalConIva() == null) {
            cajFacturaEnviada.setTotalConIva(0.0);
        }

        if (iva != null) {
            cajFacturaEnviada.setIvaDocumento(Round.round((cajFacturaEnviada.getTotalConIva() * iva), 2));
            if (cajFacturaEnviada.getTotalSinIva() == null) {
                cajFacturaEnviada.setTotalSinIva(0.0);
            }
            if (cajFacturaEnviada.getIvaDocumento() != null) {
                cajFacturaEnviada.setTotalDocumento(Round.round(cajFacturaEnviada.getTotalConIva() + cajFacturaEnviada.getIvaDocumento() + cajFacturaEnviada.getTotalSinIva(), 2));
            }
        }
    }

    private void savePicture() {
        if (file != null) {
            try {
                BufferedImage originalImage = ImageIO.read(file.getInputstream());

                BufferedImage outputImage = new BufferedImage((int) (originalImage.getWidth() * 0.25),
                        (int) (originalImage.getHeight() * 0.25), originalImage.getType());

                Graphics2D g2d = outputImage.createGraphics();
                g2d.drawImage(originalImage, 0, 0, (int) (originalImage.getWidth() * 0.25), (int) (originalImage.getHeight() * 0.25), null);
                g2d.dispose();

                String imagesFolder = System.getProperty("imagesFolder");

                if (imagesFolder == null) {
                    String currentUsersHomeDir = System.getProperty("user.home");
                    imagesFolder = currentUsersHomeDir + File.separator + "photos";
                }

                Iterator iter = ImageIO.getImageWritersByFormatName("jpeg");
                ImageWriter writer = (ImageWriter) iter.next();
                ImageWriteParam iwp = writer.getDefaultWriteParam();

                iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                float quality = 1.0f;  // reduce quality by 0%  
                iwp.setCompressionQuality(quality);

                File f = new File(imagesFolder + File.separator + fileName);
                try (FileImageOutputStream output = new FileImageOutputStream(f)) {
                    writer.setOutput(output);

                    IIOImage image = new IIOImage(outputImage, null, null);
                    writer.write(null, image, iwp);
                    writer.dispose();
                }
            } catch (IOException ex) {
                LOGGER.severe(ex.getMessage());
            }
        }
    }

    private void limpiar() {
        this.file = null;
        this.cajFacturaEnviada = null;
        CajFacturaEnviadaPK cajFacturaEnviadaPK = new CajFacturaEnviadaPK();
        this.cajFacturaEnviada = new CajFacturaEnviada();
        this.cajFacturaEnviada.setCajFacturaEnviadaPK(cajFacturaEnviadaPK);
        proyectos.clear();
        iva = null;
    }

    private void addMessage(String summary, String detail, FacesMessage.Severity severity) {
        FacesMessage message = new FacesMessage(severity, summary, detail);
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    private String obtenerEmpresa() {
        return userManager.getGnrEmpresa().getCodEmpresa();
    }

    private PrySupervisorUsuario obtenerPrySupervisorUsuario() {
        return prySupervisorUsuarioFacadeREST.find_JSON(PrySupervisorUsuario.class, obtenerEmpresa(), userManager.getUserName());
    }

    public Date getMaxDate() {
        return Calendar.getInstance().getTime();
    }
}
