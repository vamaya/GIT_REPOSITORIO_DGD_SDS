package co.gov.banrep.iconecta.ssm.firma.respuestapf;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para anonymous complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="iduser" type="{http://www.w3.org/2001/XMLSchema}byte"/>
 *         &lt;element name="guid" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="motivo" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="estado" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="nif" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="documentsInfo">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="document" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="guid" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="nombre" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="tipodoc" type="{http://www.w3.org/2001/XMLSchema}byte"/>
 *                             &lt;element name="hash" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="tipo" type="{http://www.w3.org/2001/XMLSchema}byte"/>
 *                             &lt;element name="metadatos">
 *                               &lt;complexType>
 *                                 &lt;complexContent>
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                     &lt;sequence>
 *                                       &lt;element name="metadato" maxOccurs="unbounded" minOccurs="0">
 *                                         &lt;complexType>
 *                                           &lt;complexContent>
 *                                             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                                               &lt;sequence>
 *                                                 &lt;element name="clave" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                                                 &lt;element name="valor" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                                               &lt;/sequence>
 *                                             &lt;/restriction>
 *                                           &lt;/complexContent>
 *                                         &lt;/complexType>
 *                                       &lt;/element>
 *                                     &lt;/sequence>
 *                                   &lt;/restriction>
 *                                 &lt;/complexContent>
 *                               &lt;/complexType>
 *                             &lt;/element>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="adjuntos" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="metadatosDocument">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="metadato" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="idinstancia" type="{http://www.w3.org/2001/XMLSchema}short"/>
 *         &lt;element name="idcancellationres" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ffin" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="version" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 *  @author <a href="mailto:jjrojassa@banrep.gov.co">John Jairo Rojas S.</a>
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "iduser",
    "guid",
    "motivo",
    "estado",
    "nif",
    "documentsInfo",
    "adjuntos",
    "metadatosDocument",
    "idinstancia",
    "idcancellationres",
    "ffin",
    "version"
})
//@Component
@XmlRootElement(name = "hmCircuitResponse")
public class HmCircuitResponse {

    protected byte iduser;
    @XmlElement(required = true)
    protected String guid;
    @XmlElement(required = true)
    protected String motivo;
    @XmlElement(required = true)
    protected String estado;
    protected int nif;
    @XmlElement(required = true)
    protected HmCircuitResponse.DocumentsInfo documentsInfo;
    @XmlElement(required = true)
    protected String adjuntos;
    @XmlElement(required = true)
    protected HmCircuitResponse.MetadatosDocument metadatosDocument;
    protected long idinstancia;
    @XmlElement(required = true)
    protected String idcancellationres;
    @XmlElement(required = true)
    protected String ffin;
    protected float version;

    /**
     * Obtiene el valor de la propiedad iduser.
     * 
     */
    public byte getIduser() {
        return iduser;
    }

    /**
     * Define el valor de la propiedad iduser.
     * 
     */
    public void setIduser(byte value) {
        this.iduser = value;
    }

    /**
     * Obtiene el valor de la propiedad guid.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGuid() {
        return guid;
    }

    /**
     * Define el valor de la propiedad guid.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGuid(String value) {
        this.guid = value;
    }

    /**
     * Obtiene el valor de la propiedad motivo.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMotivo() {
        return motivo;
    }

    /**
     * Define el valor de la propiedad motivo.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMotivo(String value) {
        this.motivo = value;
    }

    /**
     * Obtiene el valor de la propiedad estado.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEstado() {
        return estado;
    }

    /**
     * Define el valor de la propiedad estado.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEstado(String value) {
        this.estado = value;
    }

    /**
     * Obtiene el valor de la propiedad nif.
     * 
     */
    public int getNif() {
        return nif;
    }

    /**
     * Define el valor de la propiedad nif.
     * 
     */
    public void setNif(int value) {
        this.nif = value;
    }

    /**
     * Obtiene el valor de la propiedad documentsInfo.
     * 
     * @return
     *     possible object is
     *     {@link HmCircuitResponse.DocumentsInfo }
     *     
     */
    public HmCircuitResponse.DocumentsInfo getDocumentsInfo() {
        return documentsInfo;
    }

    /**
     * Define el valor de la propiedad documentsInfo.
     * 
     * @param value
     *     allowed object is
     *     {@link HmCircuitResponse.DocumentsInfo }
     *     
     */
    public void setDocumentsInfo(HmCircuitResponse.DocumentsInfo value) {
        this.documentsInfo = value;
    }

    /**
     * Obtiene el valor de la propiedad adjuntos.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAdjuntos() {
        return adjuntos;
    }

    /**
     * Define el valor de la propiedad adjuntos.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAdjuntos(String value) {
        this.adjuntos = value;
    }

    /**
     * Obtiene el valor de la propiedad metadatosDocument.
     * 
     * @return
     *     possible object is
     *     {@link HmCircuitResponse.MetadatosDocument }
     *     
     */
    public HmCircuitResponse.MetadatosDocument getMetadatosDocument() {
        return metadatosDocument;
    }

    /**
     * Define el valor de la propiedad metadatosDocument.
     * 
     * @param value
     *     allowed object is
     *     {@link HmCircuitResponse.MetadatosDocument }
     *     
     */
    public void setMetadatosDocument(HmCircuitResponse.MetadatosDocument value) {
        this.metadatosDocument = value;
    }

    /**
     * Obtiene el valor de la propiedad idinstancia.
     * 
     */
    public long getIdinstancia() {
        return idinstancia;
    }

    /**
     * Define el valor de la propiedad idinstancia.
     * 
     */
    public void setIdinstancia(long value) {
        this.idinstancia = value;
    }

    /**
     * Obtiene el valor de la propiedad idcancellationres.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdcancellationres() {
        return idcancellationres;
    }

    /**
     * Define el valor de la propiedad idcancellationres.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdcancellationres(String value) {
        this.idcancellationres = value;
    }

    /**
     * Obtiene el valor de la propiedad ffin.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFfin() {
        return ffin;
    }

    /**
     * Define el valor de la propiedad ffin.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFfin(String value) {
        this.ffin = value;
    }

    /**
     * Obtiene el valor de la propiedad version.
     * 
     */
    public float getVersion() {
        return version;
    }

    /**
     * Define el valor de la propiedad version.
     * 
     */
    public void setVersion(float value) {
        this.version = value;
    }


    /**
     * <p>Clase Java para anonymous complex type.
     * 
     * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="document" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="guid" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                   &lt;element name="nombre" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                   &lt;element name="tipodoc" type="{http://www.w3.org/2001/XMLSchema}byte"/>
     *                   &lt;element name="hash" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                   &lt;element name="tipo" type="{http://www.w3.org/2001/XMLSchema}byte"/>
     *                   &lt;element name="metadatos">
     *                     &lt;complexType>
     *                       &lt;complexContent>
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                           &lt;sequence>
     *                             &lt;element name="metadato" maxOccurs="unbounded" minOccurs="0">
     *                               &lt;complexType>
     *                                 &lt;complexContent>
     *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                                     &lt;sequence>
     *                                       &lt;element name="clave" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                                       &lt;element name="valor" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                                     &lt;/sequence>
     *                                   &lt;/restriction>
     *                                 &lt;/complexContent>
     *                               &lt;/complexType>
     *                             &lt;/element>
     *                           &lt;/sequence>
     *                         &lt;/restriction>
     *                       &lt;/complexContent>
     *                     &lt;/complexType>
     *                   &lt;/element>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "document"
    })
    public static class DocumentsInfo {

        protected List<HmCircuitResponse.DocumentsInfo.Document> document;

        /**
         * Gets the value of the document property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the document property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getDocument().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link HmCircuitResponse.DocumentsInfo.Document }
         * 
         * 
         */
        public List<HmCircuitResponse.DocumentsInfo.Document> getDocument() {
            if (document == null) {
                document = new ArrayList<HmCircuitResponse.DocumentsInfo.Document>();
            }
            return this.document;
        }


        /**
         * <p>Clase Java para anonymous complex type.
         * 
         * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;sequence>
         *         &lt;element name="guid" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *         &lt;element name="nombre" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *         &lt;element name="tipodoc" type="{http://www.w3.org/2001/XMLSchema}byte"/>
         *         &lt;element name="hash" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *         &lt;element name="tipo" type="{http://www.w3.org/2001/XMLSchema}byte"/>
         *         &lt;element name="metadatos">
         *           &lt;complexType>
         *             &lt;complexContent>
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                 &lt;sequence>
         *                   &lt;element name="metadato" maxOccurs="unbounded" minOccurs="0">
         *                     &lt;complexType>
         *                       &lt;complexContent>
         *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *                           &lt;sequence>
         *                             &lt;element name="clave" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *                             &lt;element name="valor" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *                           &lt;/sequence>
         *                         &lt;/restriction>
         *                       &lt;/complexContent>
         *                     &lt;/complexType>
         *                   &lt;/element>
         *                 &lt;/sequence>
         *               &lt;/restriction>
         *             &lt;/complexContent>
         *           &lt;/complexType>
         *         &lt;/element>
         *       &lt;/sequence>
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "guid",
            "nombre",
            "tipodoc",
            "hash",
            "tipo",
            "metadatos"
        })
        public static class Document {

            @XmlElement(required = true)
            protected String guid;
            @XmlElement(required = true)
            protected String nombre;
            protected byte tipodoc;
            @XmlElement(required = true)
            protected String hash;
            protected byte tipo;
            @XmlElement(required = true)
            protected HmCircuitResponse.DocumentsInfo.Document.Metadatos metadatos;

            /**
             * Obtiene el valor de la propiedad guid.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getGuid() {
                return guid;
            }

            /**
             * Define el valor de la propiedad guid.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setGuid(String value) {
                this.guid = value;
            }

            /**
             * Obtiene el valor de la propiedad nombre.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getNombre() {
                return nombre;
            }

            /**
             * Define el valor de la propiedad nombre.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setNombre(String value) {
                this.nombre = value;
            }

            /**
             * Obtiene el valor de la propiedad tipodoc.
             * 
             */
            public byte getTipodoc() {
                return tipodoc;
            }

            /**
             * Define el valor de la propiedad tipodoc.
             * 
             */
            public void setTipodoc(byte value) {
                this.tipodoc = value;
            }

            /**
             * Obtiene el valor de la propiedad hash.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getHash() {
                return hash;
            }

            /**
             * Define el valor de la propiedad hash.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setHash(String value) {
                this.hash = value;
            }

            /**
             * Obtiene el valor de la propiedad tipo.
             * 
             */
            public byte getTipo() {
                return tipo;
            }

            /**
             * Define el valor de la propiedad tipo.
             * 
             */
            public void setTipo(byte value) {
                this.tipo = value;
            }

            /**
             * Obtiene el valor de la propiedad metadatos.
             * 
             * @return
             *     possible object is
             *     {@link HmCircuitResponse.DocumentsInfo.Document.Metadatos }
             *     
             */
            public HmCircuitResponse.DocumentsInfo.Document.Metadatos getMetadatos() {
                return metadatos;
            }

            /**
             * Define el valor de la propiedad metadatos.
             * 
             * @param value
             *     allowed object is
             *     {@link HmCircuitResponse.DocumentsInfo.Document.Metadatos }
             *     
             */
            public void setMetadatos(HmCircuitResponse.DocumentsInfo.Document.Metadatos value) {
                this.metadatos = value;
            }


            /**
             * <p>Clase Java para anonymous complex type.
             * 
             * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
             * 
             * <pre>
             * &lt;complexType>
             *   &lt;complexContent>
             *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *       &lt;sequence>
             *         &lt;element name="metadato" maxOccurs="unbounded" minOccurs="0">
             *           &lt;complexType>
             *             &lt;complexContent>
             *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
             *                 &lt;sequence>
             *                   &lt;element name="clave" type="{http://www.w3.org/2001/XMLSchema}string"/>
             *                   &lt;element name="valor" type="{http://www.w3.org/2001/XMLSchema}string"/>
             *                 &lt;/sequence>
             *               &lt;/restriction>
             *             &lt;/complexContent>
             *           &lt;/complexType>
             *         &lt;/element>
             *       &lt;/sequence>
             *     &lt;/restriction>
             *   &lt;/complexContent>
             * &lt;/complexType>
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                "metadato"
            })
            public static class Metadatos {

                protected List<HmCircuitResponse.DocumentsInfo.Document.Metadatos.Metadato> metadato;

                /**
                 * Gets the value of the metadato property.
                 * 
                 * <p>
                 * This accessor method returns a reference to the live list,
                 * not a snapshot. Therefore any modification you make to the
                 * returned list will be present inside the JAXB object.
                 * This is why there is not a <CODE>set</CODE> method for the metadato property.
                 * 
                 * <p>
                 * For example, to add a new item, do as follows:
                 * <pre>
                 *    getMetadato().add(newItem);
                 * </pre>
                 * 
                 * 
                 * <p>
                 * Objects of the following type(s) are allowed in the list
                 * {@link HmCircuitResponse.DocumentsInfo.Document.Metadatos.Metadato }
                 * 
                 * 
                 */
                public List<HmCircuitResponse.DocumentsInfo.Document.Metadatos.Metadato> getMetadato() {
                    if (metadato == null) {
                        metadato = new ArrayList<HmCircuitResponse.DocumentsInfo.Document.Metadatos.Metadato>();
                    }
                    return this.metadato;
                }


                /**
                 * <p>Clase Java para anonymous complex type.
                 * 
                 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
                 * 
                 * <pre>
                 * &lt;complexType>
                 *   &lt;complexContent>
                 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
                 *       &lt;sequence>
                 *         &lt;element name="clave" type="{http://www.w3.org/2001/XMLSchema}string"/>
                 *         &lt;element name="valor" type="{http://www.w3.org/2001/XMLSchema}string"/>
                 *       &lt;/sequence>
                 *     &lt;/restriction>
                 *   &lt;/complexContent>
                 * &lt;/complexType>
                 * </pre>
                 * 
                 * 
                 */
                @XmlAccessorType(XmlAccessType.FIELD)
                @XmlType(name = "", propOrder = {
                    "clave",
                    "valor"
                })
                public static class Metadato {

                    @XmlElement(required = true)
                    protected String clave;
                    @XmlElement(required = true)
                    protected String valor;

                    /**
                     * Obtiene el valor de la propiedad clave.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getClave() {
                        return clave;
                    }

                    /**
                     * Define el valor de la propiedad clave.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setClave(String value) {
                        this.clave = value;
                    }

                    /**
                     * Obtiene el valor de la propiedad valor.
                     * 
                     * @return
                     *     possible object is
                     *     {@link String }
                     *     
                     */
                    public String getValor() {
                        return valor;
                    }

                    /**
                     * Define el valor de la propiedad valor.
                     * 
                     * @param value
                     *     allowed object is
                     *     {@link String }
                     *     
                     */
                    public void setValor(String value) {
                        this.valor = value;
                    }

                }

            }

        }

    }


    /**
     * <p>Clase Java para anonymous complex type.
     * 
     * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="metadato" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "metadato"
    })
    public static class MetadatosDocument {

        protected List<String> metadato;

        /**
         * Gets the value of the metadato property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the metadato property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getMetadato().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link String }
         * 
         * 
         */
        public List<String> getMetadato() {
            if (metadato == null) {
                metadato = new ArrayList<String>();
            }
            return this.metadato;
        }

    }

}