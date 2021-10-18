package ec.gob.sri.comprobantes.modelo.liquidacion;

import ec.gob.sri.comprobantes.modelo.InfoTributaria;
import ec.gob.sri.comprobantes.modelo.factura.Impuesto;

import javax.xml.bind.annotation.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"infoTributaria", "infoLiquidacionCompra", "detalles", "infoAdicional"})
@XmlRootElement(name = "liquidacionCompra")
public class Liquidacion {


    @XmlElement(required = true)
    protected InfoTributaria infoTributaria;
    @XmlElement(required = true)
    protected InfoLiquidacionCompra infoLiquidacionCompra;
    @XmlElement(required = true)
    protected Detalles detalles;
    protected InfoAdicional infoAdicional;
    @XmlAttribute
    protected String id;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String version;

    public InfoTributaria getInfoTributaria() {
        return this.infoTributaria;
    }

    public void setInfoTributaria(InfoTributaria value) {
        this.infoTributaria = value;
    }

    public InfoLiquidacionCompra getInfoLiquidacionCompra() {
        return this.infoLiquidacionCompra;
    }

    public void setInfoLiquidacionCompra(InfoLiquidacionCompra value) {
        this.infoLiquidacionCompra = value;
    }

    public Detalles getDetalles() {
        return this.detalles;
    }

    public void setDetalles(Detalles value) {
        this.detalles = value;
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

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {"detalle"})
    public static class Detalles {

        @XmlElement(required = true)
        protected List<Detalle> detalle;

        public List<Detalle> getDetalle() {
            if (this.detalle == null) {
                this.detalle = new ArrayList();
            }
            return this.detalle;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {"codigoPrincipal", "codigoAuxiliar", "descripcion", "cantidad", "precioUnitario", "precioSinSubsidio", "descuento", "precioTotalSinImpuesto", "detallesAdicionales", "impuestos"})
        public static class Detalle {

            @XmlElement(required = true)
            protected String codigoPrincipal;
            protected String codigoAuxiliar;
            @XmlElement(required = true)
            protected String descripcion;
            @XmlElement(required = true)
            protected BigDecimal cantidad;
            @XmlElement(required = true)
            protected BigDecimal precioUnitario;
            @XmlElement(required = true)
            protected BigDecimal precioSinSubsidio;
            @XmlElement(required = true)
            protected BigDecimal descuento;
            @XmlElement(required = true)
            protected BigDecimal precioTotalSinImpuesto;
            protected DetallesAdicionales detallesAdicionales;
            @XmlElement(required = true)
            protected Impuestos impuestos;
            @XmlTransient
            protected String codigoBarras;

            public String getCodigoBarras() {
                return codigoBarras;
            }

            public void setCodigoBarras(String codigoBarras) {
                this.codigoBarras = codigoBarras;
            }

            public String getCodigoPrincipal() {
                return this.codigoPrincipal;
            }

            public void setCodigoPrincipal(String value) {
                this.codigoPrincipal = value;
            }

            public String getCodigoAuxiliar() {
                return this.codigoAuxiliar;
            }

            public void setCodigoAuxiliar(String value) {
                this.codigoAuxiliar = value;
            }

            public String getDescripcion() {
                return this.descripcion;
            }

            public void setDescripcion(String value) {
                this.descripcion = value;
            }

            public BigDecimal getCantidad() {
                return this.cantidad;
            }

            public void setCantidad(BigDecimal value) {
                this.cantidad = value;
            }

            public BigDecimal getPrecioUnitario() {
                return this.precioUnitario;
            }

            public void setPrecioSinSubsidio(BigDecimal value) {
                this.precioSinSubsidio = value;
            }

            public BigDecimal getPrecioSinSubsidio() {
                return this.precioSinSubsidio;
            }

            public void setPrecioUnitario(BigDecimal value) {
                this.precioUnitario = value;
            }

            public BigDecimal getDescuento() {
                return this.descuento;
            }

            public void setDescuento(BigDecimal value) {
                this.descuento = value;
            }

            public BigDecimal getPrecioTotalSinImpuesto() {
                return this.precioTotalSinImpuesto;
            }

            public void setPrecioTotalSinImpuesto(BigDecimal value) {
                this.precioTotalSinImpuesto = value;
            }

            public DetallesAdicionales getDetallesAdicionales() {
                return this.detallesAdicionales;
            }

            public void setDetallesAdicionales(DetallesAdicionales value) {
                this.detallesAdicionales = value;
            }

            public Impuestos getImpuestos() {
                return this.impuestos;
            }

            public void setImpuestos(Impuestos value) {
                this.impuestos = value;
            }

            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {"detAdicional"})
            public static class DetallesAdicionales {

                @XmlElement(required = true)
                protected List<DetAdicional> detAdicional;

                public List<DetAdicional> getDetAdicional() {
                    if (this.detAdicional == null) {
                        this.detAdicional = new ArrayList();
                    }
                    return this.detAdicional;
                }

                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "")
                public static class DetAdicional {

                    @XmlAttribute
                    protected String nombre;
                    @XmlAttribute
                    protected String valor;

                    public String getNombre() {
                        return this.nombre;
                    }

                    public void setNombre(String value) {
                        this.nombre = value;
                    }

                    public String getValor() {
                        return this.valor;
                    }

                    public void setValor(String value) {
                        this.valor = value;
                    }
                }
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
    @XmlType(name = "", propOrder = {"fechaEmision", "dirEstablecimiento", "contribuyenteEspecial", "obligadoContabilidad", "tipoIdentificacionProveedor", "guiaRemision", "razonSocialProveedor", "identificacionProveedor", "direccionProveedor", "totalSinImpuestos", "totalDescuento", "codDocReembolso","totalComprobantesReembolso","totalBaseImponibleReembolso","totalImpuestoReembolso","totalConImpuestos","importeTotal", "moneda", "pagos"})
    public static class InfoLiquidacionCompra {

        @XmlElement(required = true)
        protected String fechaEmision;
        @XmlElement(required = true)
        protected String dirEstablecimiento;
        protected String contribuyenteEspecial;
        protected String obligadoContabilidad;
        @XmlElement(required = true)
        protected String tipoIdentificacionProveedor;
        protected String guiaRemision;
        @XmlElement(required = true)
        protected String razonSocialProveedor;
        @XmlElement(required = true)
        protected String identificacionProveedor;
        protected String direccionProveedor;
        @XmlElement(required = true)
        protected BigDecimal totalSinImpuestos;
        @XmlElement(required = true)
        protected BigDecimal totalDescuento;
        @XmlElement(required = true)
        protected BigDecimal codDocReembolso;
        @XmlElement(required = true)
        protected BigDecimal totalComprobantesReembolso;
        @XmlElement(required = true)
        protected BigDecimal totalBaseImponibleReembolso;
        @XmlElement(required = true)
        protected BigDecimal totalImpuestoReembolso;
        @XmlElement(required = true)
        protected TotalConImpuestos totalConImpuestos;
        @XmlElement(required = true)
        protected BigDecimal importeTotal;
        protected String moneda;
        protected Pago pagos;

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

        public String getGuiaRemision() {
            return this.guiaRemision;
        }

        public void setGuiaRemision(String value) {
            this.guiaRemision = value;
        }

        public BigDecimal getTotalSinImpuestos() {
            return this.totalSinImpuestos;
        }

        public void setTotalSinImpuestos(BigDecimal value) {
            this.totalSinImpuestos = value;
        }

        public BigDecimal getTotalDescuento() {
            return this.totalDescuento;
        }

        public void setTotalDescuento(BigDecimal value) {
            this.totalDescuento = value;
        }

        public TotalConImpuestos getTotalConImpuestos() {
            return this.totalConImpuestos;
        }

        public void setTotalConImpuestos(TotalConImpuestos value) {
            this.totalConImpuestos = value;
        }

        public Pago getPagos() {
            return this.pagos;
        }

        public void setPagos(Pago pagos) {
            this.pagos = pagos;
        }

        public BigDecimal getImporteTotal() {
            return this.importeTotal;
        }

        public void setImporteTotal(BigDecimal value) {
            this.importeTotal = value;
        }

        public String getMoneda() {
            return this.moneda;
        }

        public void setMoneda(String value) {
            this.moneda = value;
        }

        public String getTipoIdentificacionProveedor() {
            return tipoIdentificacionProveedor;
        }

        public void setTipoIdentificacionProveedor(String tipoIdentificacionProveedor) {
            this.tipoIdentificacionProveedor = tipoIdentificacionProveedor;
        }

        public String getRazonSocialProveedor() {
            return razonSocialProveedor;
        }

        public void setRazonSocialProveedor(String razonSocialProveedor) {
            this.razonSocialProveedor = razonSocialProveedor;
        }

        public String getIdentificacionProveedor() {
            return identificacionProveedor;
        }

        public void setIdentificacionProveedor(String identificacionProveedor) {
            this.identificacionProveedor = identificacionProveedor;
        }

        public String getDireccionProveedor() {
            return direccionProveedor;
        }

        public void setDireccionProveedor(String direccionProveedor) {
            this.direccionProveedor = direccionProveedor;
        }

        public BigDecimal getCodDocReembolso() {
            return codDocReembolso;
        }

        public void setCodDocReembolso(BigDecimal codDocReembolso) {
            this.codDocReembolso = codDocReembolso;
        }

        public BigDecimal getTotalComprobantesReembolso() {
            return totalComprobantesReembolso;
        }

        public void setTotalComprobantesReembolso(BigDecimal totalComprobantesReembolso) {
            this.totalComprobantesReembolso = totalComprobantesReembolso;
        }

        public BigDecimal getTotalBaseImponibleReembolso() {
            return totalBaseImponibleReembolso;
        }

        public void setTotalBaseImponibleReembolso(BigDecimal totalBaseImponibleReembolso) {
            this.totalBaseImponibleReembolso = totalBaseImponibleReembolso;
        }

        public BigDecimal getTotalImpuestoReembolso() {
            return totalImpuestoReembolso;
        }

        public void setTotalImpuestoReembolso(BigDecimal totalImpuestoReembolso) {
            this.totalImpuestoReembolso = totalImpuestoReembolso;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {"totalImpuesto"})
        public static class TotalConImpuestos {

            @XmlElement(required = true)
            protected List<TotalImpuesto> totalImpuesto;

            public List<TotalImpuesto> getTotalImpuesto() {
                if (this.totalImpuesto == null) {
                    this.totalImpuesto = new ArrayList();
                }
                return this.totalImpuesto;
            }

            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {"codigo", "codigoPorcentaje", "baseImponible", "tarifa", "valor"})
            public static class TotalImpuesto {

                @XmlElement(required = true)
                protected String codigo;
                @XmlElement(required = true)
                protected String codigoPorcentaje;
                @XmlElement(required = true)
                protected BigDecimal baseImponible;
                protected BigDecimal tarifa;
                @XmlElement(required = true)
                protected BigDecimal valor;

                public String getCodigo() {
                    return this.codigo;
                }

                public void setCodigo(String value) {
                    this.codigo = value;
                }

                public String getCodigoPorcentaje() {
                    return this.codigoPorcentaje;
                }

                public void setCodigoPorcentaje(String value) {
                    this.codigoPorcentaje = value;
                }

                public BigDecimal getBaseImponible() {
                    return this.baseImponible;
                }

                public void setBaseImponible(BigDecimal value) {
                    this.baseImponible = value;
                }

                public BigDecimal getTarifa() {
                    return this.tarifa;
                }

                public void setTarifa(BigDecimal value) {
                    this.tarifa = value;
                }

                public BigDecimal getValor() {
                    return this.valor;
                }

                public void setValor(BigDecimal value) {
                    this.valor = value;
                }
            }
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {"compensacion"})
        public static class compensacion {

            @XmlElement(required = true)
            protected List<detalleCompensaciones> compensacion;

            public List<detalleCompensaciones> getCompensaciones() {
                if (this.compensacion == null) {
                    this.compensacion = new ArrayList();
                }
                return this.compensacion;
            }

            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {"codigo", "tarifa", "valor"})
            public static class detalleCompensaciones {

                @XmlElement(required = true)
                protected int codigo;
                @XmlElement(required = true)
                protected int tarifa;
                @XmlElement(required = true)
                protected BigDecimal valor;

                public int getCodigo() {
                    return this.codigo;
                }

                public void setCodigo(int value) {
                    this.codigo = value;
                }

                public int getTarifa() {
                    return this.tarifa;
                }

                public void setTarifa(int value) {
                    this.tarifa = value;
                }

                public BigDecimal getValor() {
                    return this.valor;
                }

                public void setValor(BigDecimal valor) {
                    this.valor = valor;
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
                return this.pago;
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
