using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CorrWordAddIn
{
    public static class Constantes
    {
        public const int maxDestinatariosMemorando = 10;
        public const int maxCopiasMemorando = 20;
        public const int maxDestinatariosCarta = 20;
        public const int maxCopiasCarta = 10;

        // Constantes Marcadores plantillas    
        public const string marcadorLogoPagPpal = "LogoPagPpal";
        public const string MarcadorLogoOtrasPag = "LogoOtrasPag";
        public const string marcadorConfidencialPagPpal = "TituloConfidencialPagPPal";
        public const string marcadorConfidencialOtrasPag = "TituloConfidencialOtrasPag";
        public const string marcadorDireccionPagPpal = "TituloDireccionPagPpal";
        public const string marcadorDireccionlOtrasPag = "TituloDireccionOtrasPag";
        public const string marcadorValorDireccionPagPpal = "ValorDireccionPagPpal";
        public const string marcadorValorDireccionlOtrasPag = "ValorDireccionOtrasPag";
        public const string marcadorTelefononPagPpal = "TituloTelefonoPagPpal";
        public const string marcadorTelefonolOtrasPag = "TituloTelefonoOtrasPag";
        public const string marcadorValorTelefonoPagPpal = "ValorTelefonoPagPpal";
        public const string marcadorValorTelefonolOtrasPag = "ValorTelefonoOtrasPag";
        public const string marcadorCiudad = "TituloCiudad";
        public const string marcadorAsunto = "TituloAsunto";
        public const string marcadorAnexos = "TituloAnexos";
        public const string marcadorDestino = "des_00"; // Javier H. Insert 26072018

        // Constantes Etiquetas plantilla
        public const string tagTitulo = "_TITULO"; //Javier H. Insert 26072018
        public const string tagNombreDestino = "_NOMBRE_DESTINO";
        public const string tagCargoDestino = "_CARGO_DESTINO";
        public const string tagDependenciaDestino = "_DEPENDENCIA_DESTINO";
        public const string tagCiudadDestino = "_CIUDAD_DESTINO";
        public const string tagTituloCopias = "0_COPIAS";
        public const string tagCopias = "_COPIAS";
        public const string tagNombreFirmante = "_NOMBRE_FIRMANTE";
        public const string tagCargoFirmante = "_CARGO_FIRMANTE";
        public const string tagDependenciaFirmante = "_DEPENDENCIA_FIRMANTE";
        public const string tagTituloReferencia = "0_REFERENCIA";
        public const string tagReferencia = "REFERENCIA";
        public const string tagTituloAnexos = "0_ANEXOS";
        public const string tagAnexos = "00_ANEXOS";
        public const string tagAsunto = "ASUNTO"; //Vamaya: Se añade tag para identificar el asunto

        // Constantes Propiedades plantilla
        public const string propiedadVersionPlantilla = "iConectaVersionPlantilla";
        public const string propiedadTipo = "iConectaTipo";
        public const string propiedadTipologia = "iConectaTipologia";
        public const string propiedadMedioEnvio = "Envio";
        public const string propiedadPersonalizada = "Personalizada";
        public const string propiedadAnexosFisicos = "AFisico";
        public const string propiedadAnexosElectronicos = "AElectronico";
        public const string propiedadIdiomaIngles = "Ingles";
        public const string propiedadPcrDestino = "_PCR_DESTINO";
        public const string propiedadPcrCopias = "_PCR_COPIAS";
        public const string propiedadIdFirmante = "_ID_FIRMANTE";
        public const string propiedadDependenciaFirmante = "_SGR_FIRMANTE";
        public const string propiedadPcrFirmante = "00_PCR_FIRMANTE";
        public const string propiedadIdPcrFirmante = "00_ID_PCR_FIRMANTE";
        public const string propiedadCddFirmante = "00_CDD_FIRMANTE";
        public const string propiedadIdCddFirmante = "00_ID_CDD_FIRMANTE";
        public const string propiedadFondoIndependiente = "Fondo_Independiente";
        public const string propiedadCorreoCertificado = "CorreoCertificado";
        public const string propiedadCorreoCopia = "CorreoCopia";
        public const string propiedadImpresionArea = "ImpresionArea";
        public const string propiedadPrimerMarcaAgua = "PrimerMarcaAgua";
        public const string propiedadSegundoRemitente = "SegundoRemitente";
        public const string propiedadLogo = "Logo";
        public const string propiedadTipoFondo = "TipoFondo";
        public const string propiedadAmbiente = "iConectaAmbiente";
        public const string propiedadRef = "Referencia"; //Vamaya: se añade una nueva propiedad para poder guardar la referencia, a su vez se debe añadir la propiedad en la plantilla

        // Textos para Carta Personalizado
        public const string txtTitulo = "Según relación adjunta\n";
        public const string txtTabla = "Relación adjunta destinatarios carta";
        public const string txtNombre = "Nombre";
        public const string txtCargo = "Cargo";
        public const string txtEntidad = "Entidad";
        public const string txtDireccion = "Dirección";

        // Constantes archivos de configuración externos
        public const string fileName = "pl01";
        public const string fileNamePlconf = "plconf";
        public const string fileNamePaises = "pl02";
        public const string fileNameCiudades = "pl03";
        public const string fileNameLogoPpal = "LogoPpal.jpg";
        public const string fileNameLogoSecundario = "LogoSecundario.jpg";

        //Ambientes
        public const string ambDesa = "172.23.30.67";
        public const string ambPrue = "192.168.12.126";
        public const string ambProd = "192.168.185.39";
    }
}
