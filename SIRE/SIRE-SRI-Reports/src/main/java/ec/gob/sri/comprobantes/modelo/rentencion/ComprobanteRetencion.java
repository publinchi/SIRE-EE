package ec.gob.sri.comprobantes.modelo.rentencion;

import ec.gob.sri.comprobantes.modelo.InfoTributaria;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"infoTributaria", "infoCompRetencion", "docsSustento", "infoAdicional"})
@XmlRootElement(name = "comprobanteRetencion")
public class ComprobanteRetencion {

    @XmlElement(required = true)
    protected InfoTributaria infoTributaria;
    @XmlElement(required = true)
    protected InfoCompRetencion infoCompRetencion;
    @XmlElement(required = true)
    protected DocsSustento docsSustento;
    @XmlElement(required = true)
    protected InfoAdicional infoAdicional;
    @XmlAttribute
    protected String id;
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NMTOKEN")
    protected String version;

    public InfoTributaria getInfoTributaria() {
        return this.infoTributaria;
    }

    public void setInfoTributaria(InfoTributaria value) {
        this.infoTributaria = value;
    }

    public InfoCompRetencion getInfoCompRetencion() {
        return this.infoCompRetencion;
    }

    public void setInfoCompRetencion(InfoCompRetencion value) {
        this.infoCompRetencion = value;
    }

    public InfoAdicional getInfoAdicional() {
        return this.infoAdicional;
    }

    public void setInfoAdicional(InfoAdicional value) {
        this.infoAdicional = value;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String value) {
        this.id = value;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String value) {
        this.version = value;
    }

    public DocsSustento getDocsSustento() {
        return docsSustento;
    }

    public void setDocsSustento(DocsSustento docsSustento) {
        this.docsSustento = docsSustento;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {"impuesto"})
    public static class Impuestos {

        @XmlElement(required = true)
        protected List<Impuesto> impuesto;

        public List<Impuesto> getImpuesto() {
            if (this.impuesto == null) {
                this.impuesto = new ArrayList();
            }
            return this.impuesto;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {"campoAdicional"})
    public static class InfoAdicional {

        @XmlElement(required = true)
        protected List<CampoAdicional> campoAdicional;

        public List<CampoAdicional> getCampoAdicional() {
            if (this.campoAdicional == null) {
                this.campoAdicional = new ArrayList();
            }
            return this.campoAdicional;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {"value"})
        public static class CampoAdicional {

            @XmlValue
            protected String value;
            @XmlAttribute
            protected String nombre;

            public String getValue() {
                return this.value;
            }

            public void setValue(String value) {
                this.value = value;
            }

            public String getNombre() {
                return this.nombre;
            }

            public void setNombre(String value) {
                this.nombre = value;
            }
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {"fechaEmision", "dirEstablecimiento", "contribuyenteEspecial", "obligadoContabilidad", "tipoIdentificacionSujetoRetenido", "tipoSujetoRetenido", "parteRel", "razonSocialSujetoRetenido", "identificacionSujetoRetenido", "periodoFiscal"})
    public static class InfoCompRetencion {

        @XmlElement(required = true)
        protected String fechaEmision;
        protected String dirEstablecimiento;
        protected String contribuyenteEspecial;
        protected String obligadoContabilidad;
        @XmlElement(required = true)
        protected String tipoIdentificacionSujetoRetenido;
        @XmlElement(required = true)
        protected String tipoSujetoRetenido;
        @XmlElement(required = true)
        protected String parteRel;
        @XmlElement(required = true)
        protected String razonSocialSujetoRetenido;
        @XmlElement(required = true)
        protected String identificacionSujetoRetenido;
        @XmlElement(required = true)
        protected String periodoFiscal;

        public String getFechaEmision() {
            return this.fechaEmision;
        }

        public void setFechaEmision(String value) {
            this.fechaEmision = value;
        }

        public String getDirEstablecimiento() {
            return this.dirEstablecimiento;
        }

        public void setDirEstablecimiento(String value) {
            this.dirEstablecimiento = value;
        }

        public String getContribuyenteEspecial() {
            return this.contribuyenteEspecial;
        }

        public void setContribuyenteEspecial(String value) {
            this.contribuyenteEspecial = value;
        }

        public String getObligadoContabilidad() {
            return this.obligadoContabilidad;
        }

        public void setObligadoContabilidad(String value) {
            this.obligadoContabilidad = value;
        }

        public String getTipoIdentificacionSujetoRetenido() {
            return this.tipoIdentificacionSujetoRetenido;
        }

        public void setTipoIdentificacionSujetoRetenido(String value) {
            this.tipoIdentificacionSujetoRetenido = value;
        }

        public String getRazonSocialSujetoRetenido() {
            return this.razonSocialSujetoRetenido;
        }

        public void setRazonSocialSujetoRetenido(String value) {
            this.razonSocialSujetoRetenido = value;
        }

        public String getIdentificacionSujetoRetenido() {
            return this.identificacionSujetoRetenido;
        }

        public void setIdentificacionSujetoRetenido(String value) {
            this.identificacionSujetoRetenido = value;
        }

        public String getPeriodoFiscal() {
            return this.periodoFiscal;
        }

        public void setPeriodoFiscal(String value) {
            this.periodoFiscal = value;
        }

        public String getTipoSujetoRetenido() {
            return tipoSujetoRetenido;
        }

        public void setTipoSujetoRetenido(String tipoSujetoRetenido) {
            this.tipoSujetoRetenido = tipoSujetoRetenido;
        }

        public String getParteRel() {
            return parteRel;
        }

        public void setParteRel(String parteRel) {
            this.parteRel = parteRel;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {"docSustento"})
    public static class DocsSustento {

        @XmlElement(required = true)
        List<DocSustento> docSustento;

        public List<DocSustento> getDocSustento() {
            if (this.docSustento == null) {
                this.docSustento = new ArrayList();
            }
            return docSustento;
        }

        public void setDocSustento(List<DocSustento> docSustento) {
            this.docSustento = docSustento;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {"codSustento", "codDocSustento", "numDocSustento", "fechaEmisionDocSustento", "fechaRegistroContable", "numAutDocSustento", "pagoLocExt", "tipoRegi", "totalComprobantesReembolso", "totalBaseImponibleReembolso", "totalImpuestoReembolso", "totalSinImpuestos", "importeTotal", "impuestosDocSustento", "retenciones", "pagos"})
        public static class DocSustento {
            @XmlElement(required = true)
            protected String codSustento;
            @XmlElement(required = true)
            protected String codDocSustento;
            @XmlElement(required = true)
            protected String numDocSustento;
            @XmlElement(required = true)
            protected String fechaEmisionDocSustento;
            @XmlElement(required = true)
            protected String fechaRegistroContable;
            @XmlElement(required = true)
            protected String numAutDocSustento;
            @XmlElement(required = true)
            protected String pagoLocExt;
            @XmlElement(required = true)
            protected String tipoRegi;
            @XmlElement(required = true)
            protected String totalComprobantesReembolso;
            @XmlElement(required = true)
            protected String totalBaseImponibleReembolso;
            @XmlElement(required = true)
            protected String totalImpuestoReembolso;
            @XmlElement(required = true)
            protected String totalSinImpuestos;
            @XmlElement(required = true)
            protected String importeTotal;
            @XmlElement(required = true)
            protected ImpuestosDocSustento impuestosDocSustento;
            @XmlElement(required = true)
            protected Retenciones retenciones;
            @XmlElement(required = true)
            protected Pago pagos;

            public String getCodSustento() {
                return codSustento;
            }

            public void setCodSustento(String codSustento) {
                this.codSustento = codSustento;
            }

            public String getCodDocSustento() {
                return codDocSustento;
            }

            public void setCodDocSustento(String codDocSustento) {
                this.codDocSustento = codDocSustento;
            }

            public String getNumDocSustento() {
                return numDocSustento;
            }

            public void setNumDocSustento(String numDocSustento) {
                this.numDocSustento = numDocSustento;
            }

            public String getFechaEmisionDocSustento() {
                return fechaEmisionDocSustento;
            }

            public void setFechaEmisionDocSustento(String fechaEmisionDocSustento) {
                this.fechaEmisionDocSustento = fechaEmisionDocSustento;
            }

            public String getFechaRegistroContable() {
                return fechaRegistroContable;
            }

            public void setFechaRegistroContable(String fechaRegistroContable) {
                this.fechaRegistroContable = fechaRegistroContable;
            }

            public String getNumAutDocSustento() {
                return numAutDocSustento;
            }

            public void setNumAutDocSustento(String numAutDocSustento) {
                this.numAutDocSustento = numAutDocSustento;
            }

            public String getPagoLocExt() {
                return pagoLocExt;
            }

            public void setPagoLocExt(String pagoLocExt) {
                this.pagoLocExt = pagoLocExt;
            }

            public String getTipoRegi() {
                return tipoRegi;
            }

            public void setTipoRegi(String tipoRegi) {
                this.tipoRegi = tipoRegi;
            }

            public String getTotalComprobantesReembolso() {
                return totalComprobantesReembolso;
            }

            public void setTotalComprobantesReembolso(String totalComprobantesReembolso) {
                this.totalComprobantesReembolso = totalComprobantesReembolso;
            }

            public String getTotalBaseImponibleReembolso() {
                return totalBaseImponibleReembolso;
            }

            public void setTotalBaseImponibleReembolso(String totalBaseImponibleReembolso) {
                this.totalBaseImponibleReembolso = totalBaseImponibleReembolso;
            }

            public String getTotalImpuestoReembolso() {
                return totalImpuestoReembolso;
            }

            public void setTotalImpuestoReembolso(String totalImpuestoReembolso) {
                this.totalImpuestoReembolso = totalImpuestoReembolso;
            }

            public String getTotalSinImpuestos() {
                return totalSinImpuestos;
            }

            public void setTotalSinImpuestos(String totalSinImpuestos) {
                this.totalSinImpuestos = totalSinImpuestos;
            }

            public String getImporteTotal() {
                return importeTotal;
            }

            public void setImporteTotal(String importeTotal) {
                this.importeTotal = importeTotal;
            }

            public ImpuestosDocSustento getImpuestosDocSustento() {
                return impuestosDocSustento;
            }

            public void setImpuestosDocSustento(ImpuestosDocSustento impuestosDocSustento) {
                this.impuestosDocSustento = impuestosDocSustento;
            }

            public Retenciones getRetenciones() {
                return retenciones;
            }

            public void setRetenciones(Retenciones retenciones) {
                this.retenciones = retenciones;
            }

            public Pago getPagos() {
                return pagos;
            }

            public void setPagos(Pago pagos) {
                this.pagos = pagos;
            }

            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {"impuestoDocSustento"})
            public static class ImpuestosDocSustento {
                @XmlElement(required = true)
                protected ImpuestoDocSustento impuestoDocSustento;

                public ImpuestoDocSustento getImpuestoDocSustento() {
                    return impuestoDocSustento;
                }

                public void setImpuestoDocSustento(ImpuestoDocSustento impuestoDocSustento) {
                    this.impuestoDocSustento = impuestoDocSustento;
                }

                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "", propOrder = {"codImpuestoDocSustento", "codigoPorcentaje", "baseImponible", "tarifa", "valorImpuesto"})
                public static class ImpuestoDocSustento {
                    @XmlElement(required = true)
                    protected String codImpuestoDocSustento;
                    @XmlElement(required = true)
                    protected String codigoPorcentaje;
                    @XmlElement(required = true)
                    protected String baseImponible;
                    @XmlElement(required = true)
                    protected String tarifa;
                    @XmlElement(required = true)
                    protected String valorImpuesto;

                    public String getCodImpuestoDocSustento() {
                        return codImpuestoDocSustento;
                    }

                    public void setCodImpuestoDocSustento(String codImpuestoDocSustento) {
                        this.codImpuestoDocSustento = codImpuestoDocSustento;
                    }

                    public String getCodigoPorcentaje() {
                        return codigoPorcentaje;
                    }

                    public void setCodigoPorcentaje(String codigoPorcentaje) {
                        this.codigoPorcentaje = codigoPorcentaje;
                    }

                    public String getBaseImponible() {
                        return baseImponible;
                    }

                    public void setBaseImponible(String baseImponible) {
                        this.baseImponible = baseImponible;
                    }

                    public String getTarifa() {
                        return tarifa;
                    }

                    public void setTarifa(String tarifa) {
                        this.tarifa = tarifa;
                    }

                    public String getValorImpuesto() {
                        return valorImpuesto;
                    }

                    public void setValorImpuesto(String valorImpuesto) {
                        this.valorImpuesto = valorImpuesto;
                    }
                }
            }

            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {"retencion"})
            public static class Retenciones {
                @XmlElement(required = true)
                protected List<Retencion> retencion;

                public List<Retencion> getRetencion() {
                    if (this.retencion == null) {
                        this.retencion = new ArrayList();
                    }
                    return retencion;
                }

                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "", propOrder = {"codigo", "codigoRetencion", "baseImponible", "porcentajeRetener", "valorRetenido", "dividendos", "compraCajBanano"})
                public static class Retencion {
                    @XmlElement(required = true)
                    protected String codigo;
                    @XmlElement(required = true)
                    protected String codigoRetencion;
                    @XmlElement(required = true)
                    protected String baseImponible;
                    @XmlElement(required = true)
                    protected String porcentajeRetener;
                    @XmlElement(required = true)
                    protected String valorRetenido;
                    @XmlElement(required = true)
                    protected Dividendos dividendos;
                    @XmlElement(required = true)
                    protected CompraCajBanano compraCajBanano;

                    public String getCodigo() {
                        return codigo;
                    }

                    public void setCodigo(String codigo) {
                        this.codigo = codigo;
                    }

                    public String getCodigoRetencion() {
                        return codigoRetencion;
                    }

                    public void setCodigoRetencion(String codigoRetencion) {
                        this.codigoRetencion = codigoRetencion;
                    }

                    public String getBaseImponible() {
                        return baseImponible;
                    }

                    public void setBaseImponible(String baseImponible) {
                        this.baseImponible = baseImponible;
                    }

                    public String getPorcentajeRetener() {
                        return porcentajeRetener;
                    }

                    public void setPorcentajeRetener(String porcentajeRetener) {
                        this.porcentajeRetener = porcentajeRetener;
                    }

                    public String getValorRetenido() {
                        return valorRetenido;
                    }

                    public void setValorRetenido(String valorRetenido) {
                        this.valorRetenido = valorRetenido;
                    }

                    public Dividendos getDividendos() {
                        return dividendos;
                    }

                    public void setDividendos(Dividendos dividendos) {
                        this.dividendos = dividendos;
                    }

                    public CompraCajBanano getCompraCajBanano() {
                        return compraCajBanano;
                    }

                    public void setCompraCajBanano(CompraCajBanano compraCajBanano) {
                        this.compraCajBanano = compraCajBanano;
                    }

                    @XmlAccessorType(XmlAccessType.FIELD)
                    @XmlType(name = "", propOrder = {"fechaPagoDiv", "imRentaSoc", "ejerFisUtDiv"})
                    public static class Dividendos {
                        @XmlElement(required = true)
                        protected String fechaPagoDiv;
                        @XmlElement(required = true)
                        protected String imRentaSoc;
                        @XmlElement(required = true)
                        protected String ejerFisUtDiv;

                        public String getFechaPagoDiv() {
                            return fechaPagoDiv;
                        }

                        public void setFechaPagoDiv(String fechaPagoDiv) {
                            this.fechaPagoDiv = fechaPagoDiv;
                        }

                        public String getImRentaSoc() {
                            return imRentaSoc;
                        }

                        public void setImRentaSoc(String imRentaSoc) {
                            this.imRentaSoc = imRentaSoc;
                        }

                        public String getEjerFisUtDiv() {
                            return ejerFisUtDiv;
                        }

                        public void setEjerFisUtDiv(String ejerFisUtDiv) {
                            this.ejerFisUtDiv = ejerFisUtDiv;
                        }
                    }

                    @XmlAccessorType(XmlAccessType.FIELD)
                    @XmlType(name = "", propOrder = {"numCajBan", "precCajBan"})
                    public static class CompraCajBanano {
                        @XmlElement(required = true)
                        protected String numCajBan;
                        @XmlElement(required = true)
                        protected String precCajBan;

                        public String getNumCajBan() {
                            return numCajBan;
                        }

                        public void setNumCajBan(String numCajBan) {
                            this.numCajBan = numCajBan;
                        }

                        public String getPrecCajBan() {
                            return precCajBan;
                        }

                        public void setPrecCajBan(String precCajBan) {
                            this.precCajBan = precCajBan;
                        }
                    }
                }
            }

            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {"pago"})
            public static class Pago {
                @XmlElement(required = true)
                protected List<DetallePago> pago;

                public List<DetallePago> getPagos() {
                    if (this.pago == null) {
                        this.pago = new ArrayList();
                    }
                    return pago;
                }

                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "", propOrder = {"formaPago", "total", "plazo", "unidadTiempo"})
                public static class DetallePago {

                    @XmlElement(required = true)
                    protected String formaPago;
                    @XmlElement(required = true)
                    protected BigDecimal total;
                    protected String plazo;
                    protected String unidadTiempo;

                    public String getFormaPago() {
                        return this.formaPago;
                    }

                    public void setFormaPago(String formaPago) {
                        this.formaPago = formaPago;
                    }

                    public BigDecimal getTotal() {
                        return this.total;
                    }

                    public void setTotal(BigDecimal total) {
                        this.total = total;
                    }

                    public String getPlazo() {
                        return this.plazo;
                    }

                    public void setPlazo(String plazo) {
                        this.plazo = plazo;
                    }

                    public String getUnidadTiempo() {
                        return this.unidadTiempo;
                    }

                    public void setUnidadTiempo(String unidadTiempo) {
                        this.unidadTiempo = unidadTiempo;
                    }
                }
            }
        }
    }
}
