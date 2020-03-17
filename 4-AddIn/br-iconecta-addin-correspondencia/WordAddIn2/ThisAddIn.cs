using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Xml.Linq;
using Word = Microsoft.Office.Interop.Word;
using Application = Microsoft.Office.Interop.Word.Application;
using System.IO;
using System.Collections;
using System.Windows.Forms;
using System.Net;
using Microsoft.Office.Interop.Word;
using System.Configuration;
using System.Reflection;
using System.Xml;
using System.Security.Cryptography;
using System.Drawing;
using System.Diagnostics;
using System.Data;

namespace CorrWordAddIn
{
    public partial class ThisAddIn
    {
        private Microsoft.Office.Tools.Word.RichTextContentControl richTextControlNew;
        Microsoft.Office.Interop.Word.Document currentDocument;

        //Variable en la que se recupera la versión actual del AddIn a parir del archivo AssemblyInfo.cs
        String versionAddin = Assembly.GetExecutingAssembly().GetName().Version.ToString();

        string sourcePath;
        string pathManual;
        object passdDoc;

        string targetPath = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), "CorrInstaller");

        //Variables en las cuales se guarda las últimas versiones de AddIn y Planillas (a partir de plconf)
        string versionActualAddin;
        string versionActualPlantillaME;
        string versionActualPlantillaCA;


        string targetPathLogs = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), "CorrInstaller\\Logs\\");
        string destFile;
        string destFileConf;
        string destFilePaises;
        string destFileCiudades;
        
        int iContVisitasForm = 1;
        int imaxRemitente;
        Microsoft.Office.Interop.Word.Table oTable;
        DestinoCa destinoCa;
        Empleado empleado;
        List<DestinoCa> listUsuariosCa = new List<DestinoCa>();
        List<DestinoCa> listCopiasCa = new List<DestinoCa>();
        List<DestinoMe> listUsuariosMe = new List<DestinoMe>();
        List<DestinoMe> listCopiasMe = new List<DestinoMe>();
        List<Firmante> listFirmantes = new List<Firmante>();
        List<Empleado> listEmpleados = new List<Empleado>();
        List<Pais> paises = new List<Pais>();
        List<Ciudad> ciudades = new List<Ciudad>();
        bool primerMarcaAgregada = false;
        bool flag = false;
        
        /// <summary>
        /// Metodo en el que se inicializan los eventos y objetos del add in
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void ThisAddIn_Startup(object sender, System.EventArgs e)
        {
        }

        /// <summary>
        /// Consulta y recupera datos del archivo de configuracion
        /// </summary>  
        /// <exception cref=""> Lanzada cuando no encuentra la librería que contiene el archivo de configuración</exception>
        public void leerArchivoConfiguracion()
        {
            try
            {
                var dllPath = new Uri(Assembly.GetExecutingAssembly().GetName().CodeBase).LocalPath;
                Configuration config = ConfigurationManager.OpenExeConfiguration(dllPath);

                var appSettings = (AppSettingsSection)config.GetSection("appSettings");
                ConfigurationSection section = appSettings;
                sourcePath = appSettings.Settings["sourcePath"].Value;

                //Cifra el archivo para restringir el acceso a su información
                if (!section.SectionInformation.IsProtected)
                {
                    section.SectionInformation.ProtectSection("DataProtectionConfigurationProvider");
                    section.SectionInformation.ForceSave = true;
                    config.Save(ConfigurationSaveMode.Full);
                }
            }
            catch (System.Configuration.ConfigurationErrorsException error)
            {
                escribirLog(error.ToString());
            }
            catch (Exception error)
            {
                MessageBox.Show("Archivo de configuración corrupto, favor contactar a la línea de soporte 2000");
                escribirLog(error.ToString());
            }
        }

        /// <summary>
        /// Crea y registra los log en su respectivo fichero
        /// </summary>
        /// <param name="strLog">Log a registrar</param>
        public void escribirLog(string strLog)
        {
            StreamWriter log;
            FileStream fileStream = null;
            DirectoryInfo logDirInfo = null;
            FileInfo logFileInfo;
            string logFilePath = targetPathLogs;
            logFilePath = logFilePath + "Log-" + System.DateTime.Now.ToString("MM-dd-yyyy") + "." + "txt";
            logFileInfo = new FileInfo(logFilePath);
            logDirInfo = new DirectoryInfo(logFileInfo.DirectoryName);

            if (!logDirInfo.Exists) logDirInfo.Create();
            if (!logFileInfo.Exists)
            {
                fileStream = logFileInfo.Create();
            }
            else
            {
                fileStream = new FileStream(logFilePath, FileMode.Append);
            }
            log = new StreamWriter(fileStream);
            log.WriteLine(System.DateTime.Now.Hour.ToString("D2") + ":" + System.DateTime.Now.Minute.ToString("D2") + ":" + System.DateTime.Now.Second.ToString("D2") + " - Error - " + strLog);
            log.Close();
        }

        /// <summary>
        /// Elimina los ficheros logs creados la semana anterior
        /// </summary>
        public void eliminarLog()
        {
            for (int x = -25; x < -6; x++)
            {
                eliminarArchivos(targetPathLogs + "Log-" + System.DateTime.Now.AddDays(x).ToString("MM-dd-yyyy") + "." + "txt");
            }
        }

        /// <summary>
        /// Copia el archivo plconf desde una unidad de red
        /// </summary>
        /// <returns>devuelve rtaArchivoPropiedades=true si se descarga exitosamente los archivos</returns> 
        /// <exception cref="WebException">Exepcion cuando ocurre un problema al descargar el archivo</exception>       
        public bool copiarArchivoPropiedades()
        {
            bool rtaArchivoPropiedades = false;
            string sourceFile = System.IO.Path.Combine(sourcePath, Constantes.fileNamePlconf);
            destFileConf = System.IO.Path.Combine(targetPath, Constantes.fileNamePlconf);

            if (!System.IO.Directory.Exists(targetPath))
            {
                System.IO.Directory.CreateDirectory(targetPath);
            }
            try
            {
                WebClient webClient = new WebClient();
                webClient.DownloadFile(sourceFile, destFileConf);
                leerPropiedades();

                rtaArchivoPropiedades = true;
                eliminarArchivos(destFileConf);
            }
            catch (WebException error)
            {
                rtaArchivoPropiedades = false;
                escribirLog(error.ToString());
                MessageBox.Show("Archivo de propiedades no disponible, favor contactar a personal de soporte iConecta.");
            }
            return rtaArchivoPropiedades;
        }


        /// <summary>
        /// Copia el archivo con los usuario desde una unidad de red
        /// </summary>
        /// <returns>devuelve rtaArchivo=true si se descarga exitosamente los archivos.</returns> 
        /// <exception cref="WebException">Exepcion cuando ocurre un problema al descargar el archivo</exception>
        public bool copiarArchivo()
        {
            bool rtaArchivo = false;
            // Use Path class to manipulate file and directory paths.     

            string sourceFile = System.IO.Path.Combine(sourcePath, Constantes.fileName);
            destFile = System.IO.Path.Combine(targetPath, Constantes.fileName);

            // Create a new target folder, if necessary.
            if (!System.IO.Directory.Exists(targetPath))
            {
                System.IO.Directory.CreateDirectory(targetPath);
            }
            try
            {
                WebClient webClient = new WebClient();
                webClient.DownloadFile(sourceFile, destFile);
                cargarUsuarios();
                rtaArchivo = true;
                eliminarArchivos(destFile);
            }
            catch (WebException error)
            {
                rtaArchivo = false;
                escribirLog(error.ToString());
                MessageBox.Show("Archivo de usuarios no disponible, favor contactar a personal de soporte iConecta.");
            }
            return rtaArchivo;
        }

        /// <summary>
        /// Copia el archivo de paises y ciudades desde una unidad de red
        /// </summary>
        /// <returns>devuelve rtaArchivoPaises=true si se descarga exitosamente los archivos</returns>
        /// <exception cref="WebException">Exepcion cuando ocurre un problema al descargar el archivo</exception>
        public bool copiarArchivoPaises()
        {
            bool rtaArchivoPaises = false;
            string sourceFilePaises = System.IO.Path.Combine(sourcePath, Constantes.fileNamePaises);
            destFilePaises = System.IO.Path.Combine(targetPath, Constantes.fileNamePaises);
            string sourceFileCiudades = System.IO.Path.Combine(sourcePath, Constantes.fileNameCiudades);
            destFileCiudades = System.IO.Path.Combine(targetPath, Constantes.fileNameCiudades);

            if (!System.IO.Directory.Exists(targetPath))
            {
                System.IO.Directory.CreateDirectory(targetPath);
            }
            try
            {
                WebClient webClient = new WebClient();
                webClient.DownloadFile(sourceFilePaises, destFilePaises);
                webClient.DownloadFile(sourceFileCiudades, destFileCiudades);
                cargarPaises();
                cargarCiudades();
                rtaArchivoPaises = true;
            }
            catch (WebException error)
            {
                rtaArchivoPaises = false;
                escribirLog(error.ToString());
                MessageBox.Show("Lista de Países/Ciudades no disponible, favor contactar a personal de soporte iConecta.");
            }
            return rtaArchivoPaises;
        }

        /// <summary>
        /// Elimina el archivo de usuarios, para evitar que los usuarios visualicen su información
        /// </summary>
        /// <param name="sFile"> Archivo a eliminar</param>
        public void eliminarArchivos(string sFile)
        {
            if (System.IO.File.Exists(sFile))
            {
                System.IO.File.Delete(sFile);
            }
        }

        /// <summary>
        /// Extrae los valores que hay en el archivo plconf
        /// </summary>          
        private void leerPropiedades()
        {
            string dictionaryFilePath = destFileConf;

            using (StreamReader fileStream = new StreamReader(dictionaryFilePath, System.Text.Encoding.UTF8, false))
            {
                if (fileStream == null)
                {
                    return;
                }
                string readLine = string.Empty;

                while (readLine != null)
                {
                    readLine = fileStream.ReadLine();
                    if (!String.IsNullOrEmpty(readLine))
                    {
                        String[] substrings = readLine.Split(';');
                        imaxRemitente = int.Parse(substrings[0]);
                        passdDoc = substrings[1].Replace('?', '$').Replace('(', '2').Replace('#', 'r');
                        pathManual = substrings[2];
                        versionActualAddin = substrings[3];
                        versionActualPlantillaME = substrings[4];
                        versionActualPlantillaCA = substrings[5];
                    }
                }
            }
        }



        /// <summary> 
        /// Carga los datos del archivo con la información de los usuarios a un arreglo para su facil manipulación  
        /// </summary>  
        /// <exception cref="System.IndexOutOfRangeException">Exepcion si el archivo abierto no tiene las columnas establecidas</exception>
        /// <exception cref="System.IO.FileNotFoundException">Exepcion si no se encuentra el archivo en el repositorio local</exception>
        private void cargarUsuarios()
        {
            try
            {
                string dictionaryFilePath = destFile;
                listEmpleados.Clear();

                using (StreamReader fileStream = new StreamReader(dictionaryFilePath, System.Text.Encoding.UTF8, false))
                {
                    if (fileStream == null)
                    {
                        return;
                    }
                    string readLine = string.Empty;
                    while (readLine != null)
                    {
                        readLine = fileStream.ReadLine();
                        if (!String.IsNullOrEmpty(readLine))
                        {
                            String[] substrings = readLine.Split(';');
                            empleado = new Empleado();
                            empleado.idFirmante = substrings[0];
                            empleado.nombre = substrings[1];
                            empleado.cargo = substrings[2];
                            empleado.cargoIngles = substrings[3];
                            empleado.firmaInt = Int32.Parse(substrings[4]);
                            empleado.firmaExt = Int32.Parse(substrings[5]);
                            empleado.sigla = substrings[6];
                            empleado.dependencia = substrings[7];
                            empleado.dependenciaIngles = substrings[8];
                            empleado.fondoIndependiente = Int32.Parse(substrings[9]);
                            empleado.pcrNormal = substrings[10];
                            empleado.idPcrNormal = substrings[11];
                            empleado.pcrConfidencial = substrings[12];
                            empleado.idPcrConfidencial = substrings[13];
                            empleado.cdd = substrings[14];
                            empleado.idCdd = substrings[15];
                            empleado.ciudad = substrings[16];
                            empleado.direccion = substrings[17];
                            empleado.telefono = substrings[18];

                            listEmpleados.Add(empleado);
                            
                        }
                    }
                }
            }
            catch (System.IndexOutOfRangeException error)
            {
                escribirLog(error.ToString());
                MessageBox.Show("Archivo de usuarios con información incompleta o corrupta, favor contactar a personal de soporte iConecta.");
            }
            catch (System.IO.FileNotFoundException error)
            {
                escribirLog(error.ToString());
                MessageBox.Show("Archivo de usuarios no encontrado, favor llamar a la línea de soporte 2000.");
            }
        }

        /// <summary> 
        /// Carga los archivos con la informacion de los paises a un arreglo para su posterior consulta  
        /// </summary> 
        private void cargarPaises()
        {
            paises.Clear();
            int StringNumber;
            string dictionaryFilePath = destFilePaises;

            using (StreamReader fileStream = new StreamReader(dictionaryFilePath, System.Text.Encoding.UTF8, false))
            {
                if (fileStream == null)
                {
                    return;
                }
                string readLine = string.Empty;
                StringNumber = 0;

                while (readLine != null)
                {
                    readLine = fileStream.ReadLine();
                    if (!String.IsNullOrEmpty(readLine))
                    {
                        String[] substrings = readLine.Split(';');
                        Pais pais = new Pais(substrings[0], substrings[1]);
                        paises.Add(pais);
                        StringNumber++;
                    }
                }
            }
            StringNumber = paises.Count;
        }

        /// <summary> 
        /// Carga los archivos con la información de las ciudades a un arreglo para su posterior consulta  
        /// </summary>  
        public void cargarCiudades(string value = "Colombia")
        {
            ciudades.Clear();
            int StringNumber;
            string dictionaryFilePath = destFileCiudades;

            using (StreamReader fileStream = new StreamReader(dictionaryFilePath, System.Text.Encoding.UTF8, false))
            {
                if (fileStream == null)
                {
                    return;
                }
                string readLine = string.Empty;
                StringNumber = 0;
                while (readLine != null)
                {
                    readLine = fileStream.ReadLine();
                    if (!String.IsNullOrEmpty(readLine))
                    {
                        String[] substrings = readLine.Split(';');
                        if (substrings[1] == value)
                        {
                            Ciudad ciudad = new Ciudad(substrings[0], substrings[1], substrings[2]);
                            ciudades.Add(ciudad);

                            //Organiza todas las ciudades alfabeticamente
                            ciudades.Sort(delegate (Ciudad z, Ciudad y)
                            {
                                if (z.nombreCiudad == null && y.nombreCiudad == null) return 0;
                                else if (z.nombreCiudad == null) return -1;
                                else if (y.nombreCiudad == null) return 1;
                                else return z.nombreCiudad.CompareTo(y.nombreCiudad);
                            });

                        }
                        StringNumber++;
                    }
                }

            }
            StringNumber = ciudades.Count;
        }

        /// <summary> 
        /// Abre el navegador prederminado redireccionando a la url donde se encuentra el manual  
        /// </summary>  
        /// <exception cref="System.InvalidOperationException">Exepcion cuando no encuentra la ruta a abrir</exception>
        public void abrirUrlManual()
        {
            try
            {
                System.Diagnostics.Process.Start(pathManual);
            }
            catch (System.InvalidOperationException error)
            {
                escribirLog(error.ToString());
            }
        }

        /// <summary>
        /// Desprotege el archivo para permitir la edición de campos protegidos
        /// </summary>
        /// <exception cref="System.Runtime.InteropServices.COMException">Exepcion cuando el documento tiene otra contraseña o en su defecto no tiene</exception>
        public void desprotegerArchivo()
        {
            try
            {
                Application.ActiveDocument.Unprotect(passdDoc);
            }
            catch (System.Runtime.InteropServices.COMException error)
            {
                escribirLog(error.ToString());
            }
        }

        /// <summary>
        /// Protege el archivo para evitar la edición de campos protegidos
        /// </summary>
        public void protegerArchivo()
        {
            object noReset = false;
            object useIRM = false;
            object enforceStyleLock = false;
            Application.ActiveDocument.Protect(Word.WdProtectionType.wdAllowOnlyReading, ref noReset, ref passdDoc, ref useIRM, ref enforceStyleLock);

            //vamaya: Habilitar revisiones, nota adicional: Se comentó porque no era lo esperado
            //Application.ActiveDocument.TrackRevisions = true;

        }

        /// <summary>
        /// Inserta en un control de contenido el texto indicado
        /// </summary>
        /// <param name="sTag">Tag del control de contenido</param>
        /// <param name="sTexto">Texto a insertar en el Tag indicado</param>
        /// <param name="bReferencia">Indicador si la etiqueta ingresada contiene una referencia adicional </param>
        public void insertarTexto(String sTag, String sTexto, bool bReferencia)
        {
            for (int iPar = 1; iPar < Application.ActiveDocument.Paragraphs.Count; iPar++)
            {
                if (Application.ActiveDocument.Paragraphs[iPar].Range.ContentControls.Count > 0)
                {
                    foreach (Word.ContentControl contentcontrol in Application.ActiveDocument.Paragraphs[iPar].Range.ContentControls)
                    {
                        if (contentcontrol.Tag == sTag)
                        {
                            Globals.ThisAddIn.desprotegerArchivo();
                            contentcontrol.LockContentControl = false;
                            contentcontrol.SetPlaceholderText(null, null, sTexto.Remove(sTexto.LastIndexOf("*")));

                            Application.ActiveDocument.Paragraphs[iPar].Range.Font.Name = "Calibri";
                            Application.ActiveDocument.Paragraphs[iPar].Range.Font.Size = 12;
                            Application.ActiveDocument.Paragraphs[iPar].Range.Font.Color = WdColor.wdColorBlack;

                            if (bReferencia)
                            {
                                string[] newvalor = sTag.Split('_');
                                string resultado = newvalor[0];
                                var newrange = Application.ActiveDocument.Paragraphs[iPar + 1].Range.Previous();
                                newrange.InsertBefore("des_" + resultado);

                                object findStr = "des_" + resultado;

                                while (newrange.Find.Execute(ref findStr))
                                {

                                    newrange.Font.Size = 10;
                                    newrange.Font.Bold = 0;
                                    newrange.Font.ColorIndex = Microsoft.Office.Interop.Word.WdColorIndex.wdWhite;
                                }
                            }
                            Globals.ThisAddIn.protegerArchivo();
                            break;
                        }
                    }
                }
            }
        }

        /// <summary>
        /// Inserta en un control de contenido el texto indicado
        /// </summary>
        /// <param name="sTag">Tag del control de contenido</param>
        /// <param name="sTexto">Texto a insertar en el Tag indicado</param>
        /// <param name="iSize">Tamaño del texto . </param>
        /// <param name="color">Color del texto . </param>
        public void ajustarTexto(string sTag, string sTexto, int iSize, WdColor color)
        {
            for (int iPar = 1; iPar < Application.ActiveDocument.Paragraphs.Count; iPar++)
            {
                if (Application.ActiveDocument.Paragraphs[iPar].Range.ContentControls.Count > 0)
                {
                    foreach (Word.ContentControl contentcontrol in Application.ActiveDocument.Paragraphs[iPar].Range.ContentControls)
                    {
                        if (contentcontrol.Tag == sTag)
                        {
                            Globals.ThisAddIn.desprotegerArchivo();
                            contentcontrol.LockContentControl = false;
                            contentcontrol.SetPlaceholderText(null, null, sTexto.Remove(sTexto.LastIndexOf("*")));

                            Application.ActiveDocument.Paragraphs[iPar].Range.Font.Name = "Calibri";
                            Application.ActiveDocument.Paragraphs[iPar].Range.Font.Size = iSize;
                            Application.ActiveDocument.Paragraphs[iPar].Range.Font.Color = color;

                            Application.ActiveDocument.Paragraphs[iPar + 1].Range.Font.Name = "Calibri";
                            Application.ActiveDocument.Paragraphs[iPar + 1].Range.Font.Size = iSize;
                            Application.ActiveDocument.Paragraphs[iPar + 1].Range.Font.Color = color;

                            Globals.ThisAddIn.protegerArchivo();
                            break;
                        }
                    }
                }
            }
        }


        //Vamaya: Se añade funcióon para consultar valor de los tags en el documento
        /// <summary>
        /// Consulta el valor de un tag dado
        /// </summary>
        /// <param name="sTag">Tag del control de contenido</param>
        public String consultarTexto(string sTag)
        {
            for (int iPar = 1; iPar < Application.ActiveDocument.Paragraphs.Count; iPar++)
            {
                if (Application.ActiveDocument.Paragraphs[iPar].Range.ContentControls.Count > 0)
                {
                    foreach (Word.ContentControl contentcontrol in Application.ActiveDocument.Paragraphs[iPar].Range.ContentControls)
                    {
                        if (contentcontrol.Tag == sTag)
                        {
                            Globals.ThisAddIn.desprotegerArchivo();
                            contentcontrol.LockContentControl = false;
                            //JLG: Se agrega línea que obtiene y escribe en consola el valor del nombre remitente
                            //se comenta messagebox para que no estorbe al abrir el addin
                            //Console.WriteLine(contentcontrol.PlaceholderText.Value);
                            //MessageBox.Show(contentcontrol.PlaceholderText.Value);
                            //contentcontrol.SetPlaceholderText(null, null, sTexto.Remove(sTexto.LastIndexOf("*")));
                            //Fin JLG
                            String textoTag = contentcontrol.PlaceholderText.Value;
                            Globals.ThisAddIn.protegerArchivo();
                            return textoTag;
                        }
                    }
                }
            }
            return null;
        }
        //Fin Vamaya

        /// <summary>
        /// Obtiene el reglon donde se puede ingresar al proximo destinatario
        /// </summary>
        /// <param name="iNum">Numero de </param>        
        public int obtenerIndexDestinos(int iDestino)
        {
            int iIndex = 0;
            int iDepDestino = iDestino-1;

            for (int iPar = 1; iPar < Application.ActiveDocument.Paragraphs.Count; iPar++)
            {
                if (Application.ActiveDocument.Paragraphs[iPar].Range.ContentControls.Count > 0)
                {
                    foreach (Word.ContentControl contentcontrol in Application.ActiveDocument.Paragraphs[iPar].Range.ContentControls)
                    {
                        if (contentcontrol.Tag == (iDepDestino.ToString("00") + Constantes.tagCiudadDestino))
                        {
                            iIndex = iPar + 1;
                            break;
                        }
                        if (contentcontrol.Tag == (iDepDestino.ToString("00") + Constantes.tagDependenciaDestino))
                        {
                            iIndex = iPar + 1;
                            break;
                        }
                    }
                }
            }
            return iIndex;
        }

        /// <summary>
        /// Obtiene el parrafo donde termina el bloque del primer remitente ingresado 
        /// </summary>
        public int obtenerIndexRemitentes()
        {
            int iIndex = 0;
            int iDepRemitente = listaFirmantes.Count - 1;

            for (int iPar = 1; iPar < Application.ActiveDocument.Paragraphs.Count; iPar++)
            {
                if (Application.ActiveDocument.Paragraphs[iPar].Range.ContentControls.Count > 0)
                {
                    foreach (Word.ContentControl contentcontrol in Application.ActiveDocument.Paragraphs[iPar].Range.ContentControls)
                    {
                        if (contentcontrol.Tag == (iDepRemitente.ToString("00") + Constantes.tagDependenciaFirmante))
                        {
                            iIndex = iPar + 1;
                            break;
                        }
                    }
                }
            }
            return iIndex;
        }

        /// <summary>
        /// Obtiene el parrafo donde termina el bloque de la ultima copia ingresada
        /// </summary>
        /// <returns>retorna iIndex con el numero del parrafo </returns>
        public int obtenerIndexCopias(int iCopia)
        {
            int iIndex = 0;
            int iCampoCopia = iCopia - 1;

            for (int iPar = 1; iPar < Application.ActiveDocument.Paragraphs.Count; iPar++)
            {
                if (Application.ActiveDocument.Paragraphs[iPar].Range.ContentControls.Count > 0)
                {
                    foreach (Word.ContentControl contentcontrol in Application.ActiveDocument.Paragraphs[iPar].Range.ContentControls)
                    {
                        if (contentcontrol.Tag == (iCampoCopia.ToString("00") + Constantes.tagCopias))
                        {
                            iIndex = iPar + 1;
                            break;
                        }
                    }
                }
            }
            return iIndex;
        }

        /// <summary>
        /// Edita el contenido de un determinado marcador(Plantilla)
        /// </summary>
        ///<param name="sMarcador">Nombre del marcador</param>
        ///<param name="sValorM">Valor a insertar en el marcador</param>
        ///<exception cref="System.Runtime.InteropServices.COMException">Exepcion cuando el marcador a editar no existe</exception>
        ///
        public void EditarTextoMarcador(String sMarcador, String sValorM)
        {
            try
            {
                Globals.ThisAddIn.desprotegerArchivo();
                object ObjMiss = System.Reflection.Missing.Value;
                object nombre1 = sMarcador;
                currentDocument = Application.ActiveDocument;
                Word.Range nom = currentDocument.Bookmarks.get_Item(ref nombre1).Range;
                nom.Text = sValorM;
                object rango1 = nom;
                currentDocument.Bookmarks.Add(sMarcador, ref rango1);
                Globals.ThisAddIn.protegerArchivo();
            }
            catch (System.Runtime.InteropServices.COMException error)
            {
                escribirLog(error.ToString());
            }
        }

        /// <summary>
        /// Inserta en la plantilla la imagen de los logos del banco, la cual la obtiene de un servidor en la red.
        /// </summary>
        public void insertarLogo()
        {
            string sourceFileLogoPpal = System.IO.Path.Combine(sourcePath, Constantes.fileNameLogoPpal);
            string sourceFileLogoSecundario = System.IO.Path.Combine(sourcePath, Constantes.fileNameLogoSecundario);
            string destFileLogoPpal = System.IO.Path.Combine(targetPath, Constantes.fileNameLogoPpal);
            string destFileLogoSecundario = System.IO.Path.Combine(targetPath, Constantes.fileNameLogoSecundario);

            try
            {
                WebClient webClient = new WebClient();
                webClient.DownloadFile(sourceFileLogoPpal, destFileLogoPpal);
                webClient.DownloadFile(sourceFileLogoSecundario, destFileLogoSecundario);

                Globals.ThisAddIn.desprotegerArchivo();
                object ObjMiss = System.Reflection.Missing.Value;
                currentDocument = Application.ActiveDocument;

                var shape = currentDocument.Bookmarks[Constantes.marcadorLogoPagPpal].Range.InlineShapes.AddPicture(destFileLogoPpal, false, true);
                var shape2 = currentDocument.Bookmarks[Constantes.MarcadorLogoOtrasPag].Range.InlineShapes.AddPicture(destFileLogoSecundario, false, true);
                Globals.ThisAddIn.protegerArchivo();
            }
            catch (WebException error)
            {
                escribirLog(error.ToString());
                MessageBox.Show("Archivos de imágenes no disponibles, favor contactar a personal de soporte iConecta.");
            }
            catch (System.IO.FileNotFoundException e)
            {
                escribirLog(e.ToString());
                MessageBox.Show("Archivo de imágenes no encontrados, favor llamar a la línea de soporte 2000.");
            }
        }


        /// <summary>
        /// Remplaza el formato de una determinada palabra en Carta.
        /// </summary>
        /// <param name="sFind">Palabra a buscar.</param>
        /// <param name="SReplace">Palabra a remplazar.</param>
        public bool RemplazarFormatoTexto(string sFind, string SReplace)
        {
            int iIndexCopias = obtenerIndexCopias(listaCopiasCa.Count) - 1;
            bool hasFound = false;
            Word.Find findObject = Application.ActiveDocument.Paragraphs[iIndexCopias].Range.Find;
            findObject.ClearFormatting();
            findObject.Replacement.ClearFormatting();
            findObject.Replacement.Font.ColorIndex = Word.WdColorIndex.wdWhite;
            findObject.Replacement.Font.Size = 1;
            findObject.Text = sFind;
            findObject.Replacement.Text = SReplace;
            findObject.Forward = true;
            findObject.Wrap = Word.WdFindWrap.wdFindStop;
            findObject.Format = true;
            hasFound = findObject.Execute(Replace: Word.WdReplace.wdReplaceAll);
            return hasFound;
        }

        /// <summary>
        /// Agrega una marca de agua en las diferentes secciones del documento
        /// </summary>
        /// <param name="Footer">Seccion pie de pagina del documento</param>
        /// <param name="sName">Nombre o identificador de la marca de agua a insertar</param>
        /// <param name="Scontenido">Valor que tendra la marca de agua</param>
        public void agregarMarcaAgua(WdHeaderFooterIndex Footer, string sName, string Scontenido)
        {
            Globals.ThisAddIn.desprotegerArchivo();

            currentDocument = Application.ActiveDocument;
            foreach (Section section in currentDocument.Sections)
            {
                Word.Application wdApplication = Globals.ThisAddIn.Application;
                object missing = Type.Missing;
                string sWatermarkText = Scontenido;
                string sFontName = "ZapfHumnst BT";
                float fFontSize = 8;
                float fPositionLeft = 3; //15
                float fPositionTop = 520;

                Word.Shape watermark = section.Headers[Footer].Shapes.AddTextEffect(
                 Microsoft.Office.Core.MsoPresetTextEffect.msoTextEffect1,
                 sWatermarkText,
                 sFontName,
                 fFontSize,
                 Microsoft.Office.Core.MsoTriState.msoTrue,
                 Microsoft.Office.Core.MsoTriState.msoFalse,
                 fPositionLeft,
                 fPositionTop,
                 missing);
                watermark.Name = sName;
                watermark.Line.Visible = Microsoft.Office.Core.MsoTriState.msoFalse;
                watermark.Fill.Visible = Microsoft.Office.Core.MsoTriState.msoTrue;
                watermark.Fill.ForeColor.RGB = (Int32)Word.WdColor.wdColorBlack;
                watermark.Fill.Transparency = 0.9f;
                watermark.Rotation = 315;
                watermark.Height = Application.InchesToPoints(1.00f);
                watermark.Width = Application.InchesToPoints(8.00f);
                wdApplication.ActiveWindow.ActivePane.View.SeekView = Word.WdSeekView.wdSeekMainDocument;
                Globals.ThisAddIn.protegerArchivo();
            }
        }


        /// <summary>
        /// Actualiza el valor de las marcas de aguas insertadas
        /// </summary>
        /// <param name="sName">Identificador marca de agua a editar</param>
        /// <param name="Scontenido">Texto que tendra la marca de agua</param>
        public void actualizarMarcaAgua(string sName, string Scontenido)
        {
            currentDocument = Application.ActiveDocument;
            Word.Shapes shapeCollection = Globals.ThisAddIn.Application.ActiveDocument.Shapes;
            foreach (Word.Shape watermark in currentDocument.Sections[1].Headers[WdHeaderFooterIndex.wdHeaderFooterPrimary].Shapes)
            {
                if (watermark.Name == sName)
                {
                    watermark.TextEffect.Text = Scontenido;
                }
            }
        }

        /// <summary>
        /// Consulta y devuelve el valor o contenido de determinada etiqueta.
        /// </summary>
        /// <param name="sTag">Nombre de la etiqueta a consultar el valor</param>
        /// <returns>devuelve sPlaceholder con el contenido de la etiqueta </returns>
        public string consultarPlaceholderEtiqueta(string sTag)
        {
            string sPlaceholder = "";
            for (int iPar = 1; iPar < Application.ActiveDocument.Paragraphs.Count; iPar++)
            {
                if (Application.ActiveDocument.Paragraphs[iPar].Range.ContentControls.Count > 0)
                {
                    foreach (Word.ContentControl contentcontrol in Application.ActiveDocument.Paragraphs[iPar].Range.ContentControls)
                    {
                        if (contentcontrol.Tag == string.Concat(sTag))
                        {
                            sPlaceholder = contentcontrol.PlaceholderText.Value.ToString();
                        }
                    }
                }
            }
            return sPlaceholder;
        }

        /// <summary>
        /// Inserta un nuevo bloque de remitente.
        /// </summary>
        public void insertarRemitente()
        {
            int iIndex = obtenerIndexRemitentes();
            Globals.ThisAddIn.desprotegerArchivo();
            int iRemitente = listaFirmantes.Count;

            currentDocument = Application.ActiveDocument;
            currentDocument.Paragraphs[iIndex].Range.Font.Size = 12;
            Microsoft.Office.Tools.Word.Document extendedDocument = Globals.Factory.GetVstoObject(currentDocument);
            currentDocument.Paragraphs[iIndex].Range.ParagraphFormat.Alignment = WdParagraphAlignment.wdAlignParagraphCenter;
            currentDocument.Paragraphs[iIndex].Range.InsertParagraphAfter();
            currentDocument.Paragraphs[iIndex].Range.Select();
            richTextControlNew = extendedDocument.Controls.AddRichTextContentControl(currentDocument.Paragraphs[iIndex].Range, "richTextControlR" + iIndex.ToString());
            richTextControlNew.PlaceholderText = " ";
            richTextControlNew.Tag = iRemitente.ToString("00") + "_FIRMA";
            iIndex++;

            currentDocument.Paragraphs[iIndex].Range.ParagraphFormat.Alignment = WdParagraphAlignment.wdAlignParagraphCenter;
            currentDocument.Paragraphs[iIndex].Range.InsertParagraphAfter();
            iIndex++;

            currentDocument.Paragraphs[iIndex].Range.InsertParagraphAfter();
            currentDocument.Paragraphs[iIndex].Range.ParagraphFormat.Alignment = WdParagraphAlignment.wdAlignParagraphCenter;
            currentDocument.Paragraphs[iIndex].Range.Select();
            richTextControlNew = extendedDocument.Controls.AddRichTextContentControl(currentDocument.Paragraphs[iIndex].Range, "richTextControlR" + iIndex.ToString());
            richTextControlNew.PlaceholderText = "Nombre Destinatario.";
            richTextControlNew.Tag = iRemitente.ToString("00") + Constantes.tagNombreFirmante;
            iIndex++;

            currentDocument.Paragraphs[iIndex].Range.InsertParagraphAfter();
            currentDocument.Paragraphs[iIndex].Range.ParagraphFormat.Alignment = WdParagraphAlignment.wdAlignParagraphCenter;
            currentDocument.Paragraphs[iIndex].Range.Select();
            richTextControlNew = extendedDocument.Controls.AddRichTextContentControl(currentDocument.Paragraphs[iIndex].Range, "richTextControlR" + iIndex.ToString());
            richTextControlNew.PlaceholderText = "Cargo Destinatario.";
            richTextControlNew.Tag = iRemitente.ToString("00") + Constantes.tagCargoFirmante;
            iIndex++;

            currentDocument.Paragraphs[iIndex].Range.InsertParagraphAfter();
            currentDocument.Paragraphs[iIndex].Range.ParagraphFormat.Alignment = WdParagraphAlignment.wdAlignParagraphCenter;
            currentDocument.Paragraphs[iIndex].Range.Select();
            richTextControlNew = extendedDocument.Controls.AddRichTextContentControl(currentDocument.Paragraphs[iIndex].Range, "richTextControlR" + iIndex.ToString());
            richTextControlNew.PlaceholderText = "Dependencia Destinatario.";
            richTextControlNew.Tag = iRemitente.ToString("00") + Constantes.tagDependenciaFirmante;


            currentDocument.Range(currentDocument.Paragraphs[iIndex - 10].Range.Start, currentDocument.Paragraphs[iIndex].Range.End).Select();
            Word.Selection wordSelection = this.Application.Selection;

            currentDocument.Range(wordSelection.Start, wordSelection.Start).InsertBreak(Word.WdBreakType.wdSectionBreakContinuous);
            currentDocument.Range(wordSelection.End, wordSelection.End).InsertBreak(Word.WdBreakType.wdSectionBreakContinuous);

            currentDocument.Sections[currentDocument.Sections.Count - 1].PageSetup.TextColumns.SetCount(2);
            currentDocument.Sections[currentDocument.Sections.Count - 1].PageSetup.TextColumns.Spacing = (float)1;

            Globals.ThisAddIn.protegerArchivo();
        }

        /// <summary>
        /// Elimina el ultimo bloque de remitente.
        /// </summary>
        public void EliminarRemitente()
        {
            int iIndex = obtenerIndexRemitentes();
            int iIndexBloqueAnterior = iIndex - 5;
            int iDiferenciaBloques = iIndex - iIndexBloqueAnterior;
            int iRemitente = listaFirmantes.Count;

            if (iRemitente == 1)
            {
                MessageBox.Show("El documento debe tener mínimo un remitente.");
            }
            else
            {
                Globals.ThisAddIn.desprotegerArchivo();
                currentDocument.PageSetup.TextColumns.SetCount(1);
                iRemitente--;
                eliminarValorPropiedad(String.Concat(iRemitente.ToString("00"), Constantes.propiedadIdFirmante));
                eliminarValorPropiedad(String.Concat(iRemitente.ToString("00"), Constantes.propiedadDependenciaFirmante));
                currentDocument = Application.ActiveDocument;
                Microsoft.Office.Tools.Word.Document extendedDocument = Globals.Factory.GetVstoObject(currentDocument);

                for (int i = iIndexBloqueAnterior; i < iIndex; i++)
                {
                    extendedDocument.Controls.Remove("richTextControlR" + i.ToString());
                }

                for (int i = 1; i <= iDiferenciaBloques; i++)
                {
                    extendedDocument.Paragraphs[iIndex - i].Range.Delete();
                }

                Globals.ThisAddIn.protegerArchivo();
            }

        }

        /// <summary>
        /// Inserta un nuevo bloque de destinatarios Memorando.
        /// </summary>
        public void insertarDestinoMemorando()
        {

            int iIndex = obtenerIndexDestinos(listaDestinosMe.Count);
            int iDepDestino = listaDestinosMe.Count;

            Globals.ThisAddIn.desprotegerArchivo();

            currentDocument = Application.ActiveDocument;
            currentDocument.Paragraphs[iIndex].Range.Font.Size = 12;
            Microsoft.Office.Tools.Word.Document extendedDocument = Globals.Factory.GetVstoObject(currentDocument);

            currentDocument.Paragraphs[iIndex].Range.InsertParagraphAfter();
            currentDocument.Paragraphs[iIndex].Range.Select();
            richTextControlNew = extendedDocument.Controls.AddRichTextContentControl(currentDocument.Paragraphs[iIndex].Range, "richTextControlD" + iIndex.ToString());
            richTextControlNew.PlaceholderText = " ";
            iIndex++;

            currentDocument.Paragraphs[iIndex].Range.InsertParagraphAfter();
            currentDocument.Paragraphs[iIndex].Range.Select();
            richTextControlNew = extendedDocument.Controls.AddRichTextContentControl(currentDocument.Paragraphs[iIndex].Range, "richTextControlD" + iIndex.ToString());
            richTextControlNew.PlaceholderText = "Nombre Destinatario.";
            richTextControlNew.Tag = iDepDestino.ToString("00") + Constantes.tagNombreDestino;
            iIndex++;

            currentDocument.Paragraphs[iIndex].Range.InsertParagraphAfter();
            currentDocument.Paragraphs[iIndex].Range.Select();
            richTextControlNew = extendedDocument.Controls.AddRichTextContentControl(currentDocument.Paragraphs[iIndex].Range, "richTextControlD" + iIndex.ToString());
            richTextControlNew.PlaceholderText = "Cargo Destinatario.";
            richTextControlNew.Tag = iDepDestino.ToString("00") + Constantes.tagCargoDestino;
            iIndex++;

            currentDocument.Paragraphs[iIndex].Range.InsertParagraphAfter();
            currentDocument.Paragraphs[iIndex].Range.Select();
            richTextControlNew = extendedDocument.Controls.AddRichTextContentControl(currentDocument.Paragraphs[iIndex].Range, "richTextControlD" + iIndex.ToString());
            richTextControlNew.PlaceholderText = "Dependencia Destinatario.";
            richTextControlNew.Tag = iDepDestino.ToString("00") + Constantes.tagDependenciaDestino;
            Globals.ThisAddIn.protegerArchivo();

        }


        //JLG : se añade función para validar la lista de PCRs destino en memorando
        /// <summary>
        /// Valida lista de usuarios de Memorando.
        /// </summary>
        public Boolean validarDestinosMemorando()
        {
            int iDepDestino = listaDestinosMe.Count;

            for (int ides=0; ides < iDepDestino; ides++)
            {
                String sTagPropiedad = ides.ToString("00") + Constantes.propiedadPcrDestino;
                Microsoft.Office.Core.DocumentProperties properties;
                properties = (Microsoft.Office.Core.DocumentProperties)Globals.ThisAddIn.Application.ActiveDocument.CustomDocumentProperties;
                if (properties[sTagPropiedad].Value==sTagPropiedad)
                {
                    return false;
                }
            }
            return true;
        }
        //JLG:Fin

        /// <summary>
        /// Inserta un nuevo bloque de destinatarios de carta, agregando las etiquetas de Nombre,Cargo,Dependencia y Ciudad.
        /// </summary>
        /// <param name="bPrimer">Indica si el bloque a agregar es del primer destinatario o no</param>
        /// /// <param name="rbtFisico">Indica si el la duirección a insertar es fisica o correo</param>
        public void insertarDestinoCarta(bool bPrimer)
        {
            int iDepDestino = listaDestinosCa.Count;

            if (bPrimer == false)
            {
                int iIndex = obtenerIndexDestinos(iDepDestino);
                Globals.ThisAddIn.desprotegerArchivo();
                currentDocument = Application.ActiveDocument;
                currentDocument.Paragraphs[iIndex].Range.Font.Size = 12;
                Microsoft.Office.Tools.Word.Document extendedDocument = Globals.Factory.GetVstoObject(currentDocument);

                currentDocument.Paragraphs[iIndex].Range.InsertParagraphAfter();
                currentDocument.Paragraphs[iIndex].Range.Select();
                richTextControlNew = extendedDocument.Controls.AddRichTextContentControl(currentDocument.Paragraphs[iIndex].Range, "richTextControlD" + iIndex.ToString());
                richTextControlNew.PlaceholderText = " ";
                var newRange = currentDocument.Paragraphs[iIndex + 1].Range.Previous();
                int newDestino = iDepDestino;
                newRange.InsertBefore("des_0" + newDestino.ToString());
                object findStr = "des_0" + newDestino;

                while (newRange.Find.Execute(ref findStr))
                {
                    newRange.Font.Size = 10;
                    newRange.Font.Bold = 0;
                    newRange.Font.ColorIndex = Microsoft.Office.Interop.Word.WdColorIndex.wdWhite;
                }
                iIndex++;

                currentDocument.Paragraphs[iIndex].Range.InsertParagraphAfter();
                currentDocument.Paragraphs[iIndex].Range.Select();
                richTextControlNew = extendedDocument.Controls.AddRichTextContentControl(currentDocument.Paragraphs[iIndex].Range, "richTextControlD" + iIndex.ToString());
                richTextControlNew.PlaceholderText = "Nombre Destinatario.";
                richTextControlNew.Tag = iDepDestino.ToString("00") + Constantes.tagNombreDestino;
                iIndex++;

                currentDocument.Paragraphs[iIndex].Range.InsertParagraphAfter();
                currentDocument.Paragraphs[iIndex].Range.Select();
                richTextControlNew = extendedDocument.Controls.AddRichTextContentControl(currentDocument.Paragraphs[iIndex].Range, "richTextControlD" + iIndex.ToString());
                richTextControlNew.PlaceholderText = "Cargo Destinatario.";
                richTextControlNew.Tag = iDepDestino.ToString("00") + Constantes.tagCargoDestino;
                iIndex++;

                currentDocument.Paragraphs[iIndex].Range.InsertParagraphAfter();
                currentDocument.Paragraphs[iIndex].Range.Select();
                richTextControlNew = extendedDocument.Controls.AddRichTextContentControl(currentDocument.Paragraphs[iIndex].Range, "richTextControlD" + iIndex.ToString());
                richTextControlNew.PlaceholderText = "Entidad Destinatario.";
                richTextControlNew.Tag = iDepDestino.ToString("00") + Constantes.tagDependenciaDestino;
                iIndex++;

                currentDocument.Paragraphs[iIndex].Range.InsertParagraphAfter();
                currentDocument.Paragraphs[iIndex].Range.Select();
                richTextControlNew = extendedDocument.Controls.AddRichTextContentControl(currentDocument.Paragraphs[iIndex].Range, "richTextControlD" + iIndex.ToString());
                richTextControlNew.PlaceholderText = "Ciudad Destinatario.";
                richTextControlNew.Tag = iDepDestino.ToString("00") + Constantes.tagCiudadDestino;

                Globals.ThisAddIn.protegerArchivo();
            }
            else
            {
                int iIndex = Globals.ThisAddIn.obtenerParrafoDestino(0, Constantes.tagTitulo);                

                Globals.ThisAddIn.desprotegerArchivo();
                currentDocument = Application.ActiveDocument;
                currentDocument.Paragraphs[iIndex].Range.Font.Size = 12;
                Microsoft.Office.Tools.Word.Document extendedDocument = Globals.Factory.GetVstoObject(currentDocument);
                iIndex++;

                currentDocument.Paragraphs[iIndex].Range.InsertParagraphAfter();
                currentDocument.Paragraphs[iIndex].Range.Select();
                richTextControlNew = extendedDocument.Controls.AddRichTextContentControl(currentDocument.Paragraphs[iIndex].Range, "richTextControlD" + iIndex.ToString());
                richTextControlNew.PlaceholderText = "Nombre Destinatario.";
                richTextControlNew.Tag = iDepDestino.ToString("00") + Constantes.tagNombreDestino;
                iIndex++;

                currentDocument.Paragraphs[iIndex].Range.InsertParagraphAfter();
                currentDocument.Paragraphs[iIndex].Range.Select();
                richTextControlNew = extendedDocument.Controls.AddRichTextContentControl(currentDocument.Paragraphs[iIndex].Range, "richTextControlD" + iIndex.ToString());
                richTextControlNew.PlaceholderText = "Cargo Destinatario.";
                richTextControlNew.Tag = iDepDestino.ToString("00") + Constantes.tagCargoDestino;
                iIndex++;

                currentDocument.Paragraphs[iIndex].Range.InsertParagraphAfter();
                currentDocument.Paragraphs[iIndex].Range.Select();
                richTextControlNew = extendedDocument.Controls.AddRichTextContentControl(currentDocument.Paragraphs[iIndex].Range, "richTextControlD" + iIndex.ToString());
                richTextControlNew.PlaceholderText = "Entidad Destinatario.";
                richTextControlNew.Tag = iDepDestino.ToString("00") + Constantes.tagDependenciaDestino;
                iIndex++;

                currentDocument.Paragraphs[iIndex].Range.InsertParagraphAfter();
                currentDocument.Paragraphs[iIndex].Range.Select();
                richTextControlNew = extendedDocument.Controls.AddRichTextContentControl(currentDocument.Paragraphs[iIndex].Range, "richTextControlD" + iIndex.ToString());
                richTextControlNew.PlaceholderText = "Ciudad Destinatario.";
                richTextControlNew.Tag = iDepDestino.ToString("00") + Constantes.tagCiudadDestino;


                Globals.ThisAddIn.protegerArchivo();
            }

        }

        //JLG : se añade función para validar la lista de direcciones destino en carta normal sin opción de correspondencia combinada
        /// <summary>
        /// Valida lista de usuarios de Carta.
        /// </summary>
        public Boolean validarDestinosCarta()
        {
            int iDepDestino = listaDestinosCa.Count;

            for (int ides = 0; ides < iDepDestino; ides++)
            {
                String sTagPropiedad = ides.ToString("00") + Constantes.propiedadPcrDestino;
                Microsoft.Office.Core.DocumentProperties properties;
                properties = (Microsoft.Office.Core.DocumentProperties)Globals.ThisAddIn.Application.ActiveDocument.CustomDocumentProperties;
                if (properties[sTagPropiedad].Value == sTagPropiedad)
                {
                    return false;
                }
            }
            return true;
        }
        //JLG:Fin

        //JLG : se añade función para validar el tag de los destinatarios cuando se selecciona carta con correspondencia combinada
        /// <summary>
        /// Valida lista de usuarios de Carta Combinada.
        /// </summary>
        public Boolean validarDestinosCartaComb()
        {
            String sTagValue = "00" + Constantes.tagNombreDestino;
            String Valor = consultarTexto(sTagValue);
                if (Valor.Equals(" "))
                {
                    return false;
                }
            return true;
        }
        //JLG:Fin

        /// <summary>
        /// Inserta un nuevo bloque de destinatarios de carta, agregando las etiquetas de Ciudad y 2 mas
        /// </summary>
        /// <param name="sTag1">Indica si el nombre de la primera etiqueta a agregar</param>
        /// <param name="sTag2">Indica si el nombre de la segunda etiqueta a agregar</param>
        /// <param name="sContenido1">Indica el valor que tendra la primera etiqueta insertada</param>
        /// <param name="sContenido2">Indica el valor que tendra la segunda etiqueta insertada</param>
        /// <param name="bPrimer">Indica si el bloque a agregar es del primer destinatario o no</param>
        public void insertarDestinoDosDatos(string sTag1, string sTag2, string sContenido1, string sContenido2, bool bPrimer = false)
        {
            int iDepDestino = listaDestinosCa.Count;
            if (bPrimer == false)
            {
                int iIndex = obtenerIndexDestinos(iDepDestino);
                Globals.ThisAddIn.desprotegerArchivo();   
                currentDocument = Application.ActiveDocument;
                currentDocument.Paragraphs[iIndex].Range.Font.Size = 12;
                Microsoft.Office.Tools.Word.Document extendedDocument = Globals.Factory.GetVstoObject(currentDocument);

                currentDocument.Paragraphs[iIndex].Range.InsertParagraphAfter();
                currentDocument.Paragraphs[iIndex].Range.Select();
                richTextControlNew = extendedDocument.Controls.AddRichTextContentControl(currentDocument.Paragraphs[iIndex].Range, "richTextControlD" + iIndex.ToString());
                richTextControlNew.PlaceholderText = " ";
                var newRange = currentDocument.Paragraphs[iIndex + 1].Range.Previous();
                int newDestino = iDepDestino;
                newRange.InsertBefore("des_0" + newDestino.ToString());
                object findStr = "des_0" + newDestino;

                while (newRange.Find.Execute(ref findStr))
                {
                    newRange.Font.Size = 10;
                    newRange.Font.Bold = 0;
                    newRange.Font.ColorIndex = Microsoft.Office.Interop.Word.WdColorIndex.wdWhite;
                }
                iIndex++;

                currentDocument.Paragraphs[iIndex].Range.InsertParagraphAfter();
                currentDocument.Paragraphs[iIndex].Range.Select();
                richTextControlNew = extendedDocument.Controls.AddRichTextContentControl(currentDocument.Paragraphs[iIndex].Range, "richTextControlD" + iIndex.ToString());
                richTextControlNew.PlaceholderText = sContenido1;
                richTextControlNew.Tag = iDepDestino.ToString("00") + sTag1;
                iIndex++;

                currentDocument.Paragraphs[iIndex].Range.InsertParagraphAfter();
                currentDocument.Paragraphs[iIndex].Range.Select();
                richTextControlNew = extendedDocument.Controls.AddRichTextContentControl(currentDocument.Paragraphs[iIndex].Range, "richTextControlD" + iIndex.ToString());
                richTextControlNew.PlaceholderText = sContenido2;
                richTextControlNew.Tag = iDepDestino.ToString("00") + sTag2;
                iIndex++;

                currentDocument.Paragraphs[iIndex].Range.InsertParagraphAfter();
                currentDocument.Paragraphs[iIndex].Range.Select();
                richTextControlNew = extendedDocument.Controls.AddRichTextContentControl(currentDocument.Paragraphs[iIndex].Range, "richTextControlD" + iIndex.ToString());
                richTextControlNew.PlaceholderText = "Ciudad Destinatario.";
                richTextControlNew.Tag = iDepDestino.ToString("00") + Constantes.tagCiudadDestino;

                Globals.ThisAddIn.protegerArchivo();
            }
            else
            {
                int iIndex = Globals.ThisAddIn.obtenerParrafoDestino(0, Constantes.tagTitulo) + 1; 

                Globals.ThisAddIn.desprotegerArchivo();

                currentDocument = Application.ActiveDocument;
                currentDocument.Paragraphs[iIndex].Range.Font.Size = 12;
                Microsoft.Office.Tools.Word.Document extendedDocument = Globals.Factory.GetVstoObject(currentDocument);

                currentDocument.Paragraphs[iIndex].Range.InsertParagraphAfter();
                currentDocument.Paragraphs[iIndex].Range.Select();
                richTextControlNew = extendedDocument.Controls.AddRichTextContentControl(currentDocument.Paragraphs[iIndex].Range, "richTextControlD" + iIndex.ToString());
                richTextControlNew.PlaceholderText = sContenido1;
                richTextControlNew.Tag = iDepDestino.ToString("00") + sTag1;
                iIndex++;

                currentDocument.Paragraphs[iIndex].Range.InsertParagraphAfter();
                currentDocument.Paragraphs[iIndex].Range.Select();
                richTextControlNew = extendedDocument.Controls.AddRichTextContentControl(currentDocument.Paragraphs[iIndex].Range, "richTextControlD" + iIndex.ToString());
                richTextControlNew.PlaceholderText = sContenido2;
                richTextControlNew.Tag = iDepDestino.ToString("00") + sTag2;
                iIndex++;

                currentDocument.Paragraphs[iIndex].Range.InsertParagraphAfter();
                currentDocument.Paragraphs[iIndex].Range.Select();
                richTextControlNew = extendedDocument.Controls.AddRichTextContentControl(currentDocument.Paragraphs[iIndex].Range, "richTextControlD" + iIndex.ToString());
                richTextControlNew.PlaceholderText = "Ciudad Destinatario.";
                richTextControlNew.Tag = iDepDestino.ToString("00") + Constantes.tagCiudadDestino;

                Globals.ThisAddIn.protegerArchivo();
            }
        }


        /// <summary>
        /// Inserta un nuevo bloque de destinatarios de carta, agregando las etiquetas de Ciudad y 1 mas
        /// </summary>
        /// <param name="sTag1">Indica si el nombre de la etiqueta a agregar</param>       
        /// <param name="sContenido1">Indica el valor que tendra la etiqueta insertada</param>       
        /// <param name="bPrimer">Indica si el bloque a agregar es del primer destinatario o no</param>
        public void insertarDestinoUnDato(string sTag, string sContenido, bool bPrimer = false)
        {
            int iDepDestino = listaDestinosCa.Count;
            if (bPrimer == false)
            {
                int iIndex = obtenerIndexDestinos(iDepDestino);
                
                Globals.ThisAddIn.desprotegerArchivo();
                currentDocument = Application.ActiveDocument;
                currentDocument.Paragraphs[iIndex].Range.Font.Size = 12;
                Microsoft.Office.Tools.Word.Document extendedDocument = Globals.Factory.GetVstoObject(currentDocument);

                currentDocument.Paragraphs[iIndex].Range.InsertParagraphAfter();
                currentDocument.Paragraphs[iIndex].Range.Select();
                richTextControlNew = extendedDocument.Controls.AddRichTextContentControl(currentDocument.Paragraphs[iIndex].Range, "richTextControlD" + iIndex.ToString());
                richTextControlNew.PlaceholderText = " ";

                var newRange = currentDocument.Paragraphs[iIndex + 1].Range.Previous();
                newRange.InsertBefore("des_0" + iDepDestino.ToString());
                object findStr = "des_0" + iDepDestino;

                while (newRange.Find.Execute(ref findStr))
                {
                    newRange.Font.Size = 10;
                    newRange.Font.Bold = 0;
                    newRange.Font.ColorIndex = Microsoft.Office.Interop.Word.WdColorIndex.wdWhite;
                }

                iIndex++;

                currentDocument.Paragraphs[iIndex].Range.InsertParagraphAfter();
                currentDocument.Paragraphs[iIndex].Range.Select();
                richTextControlNew = extendedDocument.Controls.AddRichTextContentControl(currentDocument.Paragraphs[iIndex].Range, "richTextControlD" + iIndex.ToString());
                richTextControlNew.PlaceholderText = sContenido;
                richTextControlNew.Tag = iDepDestino.ToString("00") + sTag;
                iIndex++;

                currentDocument.Paragraphs[iIndex].Range.InsertParagraphAfter();
                currentDocument.Paragraphs[iIndex].Range.Select();
                richTextControlNew = extendedDocument.Controls.AddRichTextContentControl(currentDocument.Paragraphs[iIndex].Range, "richTextControlD" + iIndex.ToString());
                richTextControlNew.PlaceholderText = "Ciudad Destinatario.";
                richTextControlNew.Tag = iDepDestino.ToString("00") + Constantes.tagCiudadDestino;

                Globals.ThisAddIn.protegerArchivo();
            }
            else
            {
                int iIndex = Globals.ThisAddIn.obtenerParrafoDestino(0, Constantes.tagTitulo);

                Globals.ThisAddIn.desprotegerArchivo();
                currentDocument = Application.ActiveDocument;
                currentDocument.Paragraphs[iIndex].Range.Font.Size = 12;
                Microsoft.Office.Tools.Word.Document extendedDocument = Globals.Factory.GetVstoObject(currentDocument);
                iIndex++;

                currentDocument.Paragraphs[iIndex].Range.InsertParagraphAfter();
                currentDocument.Paragraphs[iIndex].Range.Select();
                richTextControlNew = extendedDocument.Controls.AddRichTextContentControl(currentDocument.Paragraphs[iIndex].Range, "richTextControlD" + iIndex.ToString());
                richTextControlNew.PlaceholderText = sContenido;
                richTextControlNew.Tag = iDepDestino.ToString("00") + sTag;
                iIndex++;

                currentDocument.Paragraphs[iIndex].Range.InsertParagraphAfter();
                currentDocument.Paragraphs[iIndex].Range.Select();
                richTextControlNew = extendedDocument.Controls.AddRichTextContentControl(currentDocument.Paragraphs[iIndex].Range, "richTextControlD" + iIndex.ToString());
                richTextControlNew.PlaceholderText = "Ciudad Destinatario.";
                richTextControlNew.Tag = iDepDestino.ToString("00") + Constantes.tagCiudadDestino;

                Globals.ThisAddIn.protegerArchivo();
            }

        }

        /// <summary>
        /// Inserta un nuevo bloque de copias.
        /// </summary>
        public void insertarCopia(int iCopia)
        {
            int iIndex = obtenerIndexCopias(iCopia);
            int iCampoCopia = iCopia;

            Globals.ThisAddIn.desprotegerArchivo();
            iCopia++;
            currentDocument = Application.ActiveDocument;
            currentDocument.Paragraphs[iIndex].Range.InsertParagraphAfter();
            currentDocument.Paragraphs[iIndex].Range.Select();
            Microsoft.Office.Tools.Word.Document extendedDocument = Globals.Factory.GetVstoObject(currentDocument);
            richTextControlNew = extendedDocument.Controls.AddRichTextContentControl(currentDocument.Paragraphs[iIndex].Range, "richTextControlC" + iCampoCopia.ToString());
            richTextControlNew.PlaceholderText = ".";
            richTextControlNew.Tag = iCampoCopia.ToString("00") + Constantes.tagCopias;
            iIndex++;
            Globals.ThisAddIn.protegerArchivo();
        }

        /// <summary>
        /// Elimina el ultimo bloque de copias ingresada
        /// </summary>
        public bool eliminarCopia(int iCopia)
        {
            bool bEliminado = false;
            int iIndex = obtenerIndexCopias(iCopia);
            Globals.ThisAddIn.desprotegerArchivo();

            if (iCopia == 1)
            {
                eliminarValorPropiedad(String.Concat("00", Constantes.propiedadPcrCopias));
            }
            else
            {
                iCopia--;
                eliminarValorPropiedad(String.Concat(iCopia.ToString("00"), Constantes.propiedadPcrCopias));

                currentDocument = Application.ActiveDocument;
                Microsoft.Office.Tools.Word.Document extendedDocument = Globals.Factory.GetVstoObject(currentDocument);
                int iCampoCopia = iCopia;
                extendedDocument.Controls.Remove("richTextControlC" + iCampoCopia.ToString());
                int iPar = extendedDocument.Paragraphs.Count - 1;
                extendedDocument.Paragraphs[iPar].Range.Delete();
                bEliminado = true;
            }

            if (listaCopiasCa.Any())
                listaCopiasCa.RemoveAt(listaCopiasCa.Count - 1);

            

            Globals.ThisAddIn.protegerArchivo();
            return bEliminado;
        }


        /// <summary>
        /// Elimina el ultimo bloque de destinos ingresado
        /// </summary>
        public void eliminarDestino(int tDestino)
        {
            int iIndex = obtenerIndexDestinos(tDestino);
            int iIndexBloqueAnterior = obtenerIndexDestinos(tDestino - 1);

            int iDiferenciaBloques = iIndex - iIndexBloqueAnterior;

            if (tDestino == 1)
            {
                MessageBox.Show("El documento debe tener mínimo un destinatario.");
            }
            else
            {
                Globals.ThisAddIn.desprotegerArchivo();
                tDestino--;
                eliminarValorPropiedad(String.Concat(tDestino.ToString("00"), Constantes.propiedadPcrDestino));
                currentDocument = Application.ActiveDocument;
                Microsoft.Office.Tools.Word.Document extendedDocument = Globals.Factory.GetVstoObject(currentDocument);
                for (int i = iIndexBloqueAnterior; i < iIndex; i++)
                {
                    extendedDocument.Controls.Remove("richTextControlD" + i.ToString());
                }
                for (int i = 1; i <= iDiferenciaBloques; i++)
                {
                    extendedDocument.Paragraphs[iIndex - i].Range.Delete();
                }
                if (listaDestinosCa.Any())
                    listaDestinosCa.RemoveAt(listaDestinosCa.Count - 1);

                protegerArchivo();
            }
        }


        /// <summary>
        /// Elimina el primer bloque de destinos
        /// </summary>
        public void eliminarPrimerDestino()
        {
            currentDocument = Application.ActiveDocument;
            int iParIniDestino = Globals.ThisAddIn.obtenerParrafoDestino(0, Constantes.tagTitulo) + 1;
            int iParFinDestino = Globals.ThisAddIn.obtenerParrafoDestino(0, Constantes.tagCiudadDestino);
            int iDestino = listaDestinosCa.Count;
            Microsoft.Office.Tools.Word.Document extendedDocument = Globals.Factory.GetVstoObject(currentDocument);
            Globals.ThisAddIn.desprotegerArchivo();

            while (iParFinDestino >= iParIniDestino)
            {
                extendedDocument.Controls.Remove("richTextControlD" + iParFinDestino);
                extendedDocument.Paragraphs[iParFinDestino].Range.Delete();
                iParFinDestino--;
            }
            eliminarValorPropiedad(String.Concat(iDestino.ToString("00"), Constantes.propiedadPcrDestino));
            //iDestino--;
            if(listaDestinosCa.Any())
            listaDestinosCa.RemoveAt(listaDestinosCa.Count - 1);
            Globals.ThisAddIn.protegerArchivo();
        }

        /// <summary>
        /// Inserta un valor en una determinada propiedad
        /// </summary>
        /// <param name="sTagPropiedad">Nombre propiedad a insertar</param>
        /// <param name="sValor">Valor a insertar</param>
        public void insertarValorPropiedad(string sTagPropiedad, string sValor)
        {
            Microsoft.Office.Core.DocumentProperties properties;
            properties = (Microsoft.Office.Core.DocumentProperties)Globals.ThisAddIn.Application.ActiveDocument.CustomDocumentProperties;
            properties[sTagPropiedad].Value = sValor;
        }

        /// <summary>
        /// Elimina el valor ingresado en la propiedad, remplazandolo por el valor por defecto
        /// </summary>
        /// <param name="sTagPropiedad">Propiedad a eliminar</param>
        public void eliminarValorPropiedad(string sTagPropiedad)
        {
            Microsoft.Office.Core.DocumentProperties properties;
            properties = (Microsoft.Office.Core.DocumentProperties)Globals.ThisAddIn.Application.ActiveDocument.CustomDocumentProperties;
            properties[sTagPropiedad].Value = sTagPropiedad;
        }

        /// <summary>
        /// Consulta el valor que tiene una determinada propiedad
        /// </summary>
        /// <param name="sTagPropiedad">Propiedad a consultar</param>
        /// <returns>Devuelve sValorPropiedad con el valor que contiene la propiedad </returns>
        /// <exception cref="System.ArgumentException">Exepcion cuando la propiedad a consultar no existe</exception>
        public string consultarValorPropiedad(string sTagPropiedad)
        {
            Microsoft.Office.Core.DocumentProperties properties;
            properties = (Microsoft.Office.Core.DocumentProperties)Globals.ThisAddIn.Application.ActiveDocument.CustomDocumentProperties;
            string sValorPropiedad = null;
            try
            {
                sValorPropiedad = properties[sTagPropiedad].Value;
            }
            catch (System.ArgumentException error)
            {
                escribirLog(error.ToString());
            }
            return sValorPropiedad;
        }


        /// <summary>
        /// Eliminar Destinos tabla Personalizada
        /// </summary>
        /// <param name="usuario">Objeto usuario a eliminar de la tabla personalizada</param>
        public void eliminarDestinoMasivo(Usuario usuario)
        {
            int index = 0;
            flag = false;
            desprotegerArchivo();

            foreach (DestinoCa user in listaDestinosCa)
            {
                if (user.nombre == usuario.nombre && user.cargo == usuario.cargo && user.dependencia == usuario.dependencia)
                {
                    foreach (Row dataRow in oTable.Rows)
                    {
                        if (user.nombre == dataRow.Cells[1].Range.Text.Substring(0, dataRow.Cells[1].Range.Text.Length - 2)
                            && user.cargo == dataRow.Cells[2].Range.Text.Substring(0, dataRow.Cells[2].Range.Text.Length - 2)
                            && user.dependencia == dataRow.Cells[3].Range.Text.Substring(0, dataRow.Cells[3].Range.Text.Length - 2))
                        {
                            dataRow.Delete();
                            index = listaDestinosCa.IndexOf(user);
                            flag = true;
                            break;
                        }
                    }
                }
            }
            if (flag)
                listUsuariosCa.RemoveAt(index);

            if (!flag)
                MessageBox.Show("Usuario No Encontrado");

            protegerArchivo();

        }

        /// <summary>
        /// Insertar Tabla y pagina Opc. Personalizado 
        /// </summary>
        /// <param name="usu">Objeto Usuario a insertar en tabla</param>
        public void InsertTable(DestinoCa usu)
        {

            Globals.ThisAddIn.desprotegerArchivo();

            if (oTable == null)
            {
                insertNewPage();
                oTable = createTable();
                insertRow(oTable, usu);
            }
            else
            {
                insertRow(oTable, usu);
            }
            Globals.ThisAddIn.protegerArchivo();
        }

        /// <summary>
        /// Insertar nueva pagina Final doc. Opc.Personalizado
        /// </summary>
        public void insertNewPage()
        {

            Object nullobj = System.Reflection.Missing.Value;
            Object objUnit = Word.WdUnits.wdStory;

            this.Application.Selection.EndKey(ref objUnit, ref nullobj);
            this.Application.Selection.InsertNewPage();
            int totalParrafos = Application.ActiveDocument.Paragraphs.Count;
            Application.ActiveDocument.Paragraphs[totalParrafos].Range.Text = Constantes.txtTabla;
            Application.ActiveDocument.Paragraphs[totalParrafos].Range.InsertParagraphAfter();
            Application.ActiveDocument.Paragraphs[totalParrafos].Next().Range.Text = " ";
            Application.ActiveDocument.Paragraphs[totalParrafos].Next().Range.Font.Size = 12;
            Application.ActiveDocument.Paragraphs[totalParrafos].Range.Font.Color = WdColor.wdColorBlack;
            Application.ActiveDocument.Paragraphs[totalParrafos].Range.ParagraphFormat.Alignment = WdParagraphAlignment.wdAlignParagraphCenter;
            Application.ActiveDocument.Paragraphs[totalParrafos].Range.Font.Size = 13;

        }

        /// <summary>
        /// Eliminar pagina final doc. Opc.Personalizado
        /// </summary>
        public void eliminarNewPage()
        {
            int totalParrafos = Application.ActiveDocument.Paragraphs.Count - 1;
            flag = false;

            while (!flag)
            {
                if (Application.ActiveDocument.Paragraphs[totalParrafos].Range.ContentControls.Count > 0)
                {
                    flag = true;
                    break;
                }
                Application.ActiveDocument.Paragraphs[totalParrafos].Range.Delete();
                totalParrafos--;
            }
        }

        /// <summary>
        /// Crear Tabla Final doc. Opc.Personalizado
        /// </summary>
        /// <returns></returns>
        public Table createTable()
        {
            object _Start = Application.ActiveDocument.Content.End - 1;
            object _End = Application.ActiveDocument.Content.End;
            Word.Range tableLocation = this.Application.ActiveDocument.Range(_Start, _End);

            oTable = this.Application.ActiveDocument.Tables.Add(
            tableLocation, 1, 4);
            oTable.Borders.OutsideLineStyle = WdLineStyle.wdLineStyleSingle;
            oTable.Borders.InsideLineStyle = WdLineStyle.wdLineStyleSingle;
            oTable.Range.Font.Color = WdColor.wdColorBlack;
            oTable.Range.Font.Size = 9;

            oTable.Rows[1].Range.Font.Bold = 1;
            oTable.Rows[1].Range.ParagraphFormat.Alignment = WdParagraphAlignment.wdAlignParagraphCenter;
            oTable.Rows[1].Cells[1].Range.Text = Constantes.txtNombre;
            oTable.Rows[1].Cells[2].Range.Text = Constantes.txtCargo;
            oTable.Rows[1].Cells[3].Range.Text = Constantes.txtEntidad;
            oTable.Rows[1].Cells[4].Range.Text = Constantes.txtDireccion;

            return oTable;

        }

        /*
         * 
         */
        /// <summary>
        /// Insertar fila a tabla Opc.Personalizado
        /// </summary>
        /// <param name="tabla">Objeto tabla en la cual se insertara la fila</param>
        /// <param name="usuario">Objeto Usuario que se agregara a la fila</param>
        private void insertRow(Table tabla, DestinoCa usuario)
        {
            flag = false;
            Row dataRow;

            foreach (DestinoCa user in listUsuariosCa)
            {
                if (user.nombre == usuario.nombre && user.cargo == usuario.cargo && user.dependencia == usuario.dependencia)
                {
                    MessageBox.Show("El destinatario ya fúe incluido.");
                    flag = true;
                }
            }

            if (!flag)
            {
                dataRow = tabla.Rows.Add();
                dataRow.Range.ParagraphFormat.Alignment = WdParagraphAlignment.wdAlignParagraphLeft;
                dataRow.Range.Font.Bold = 0;
                dataRow.Cells[1].Range.Text = usuario.nombre;
                dataRow.Cells[2].Range.Text = usuario.cargo;
                dataRow.Cells[3].Range.Text = usuario.dependencia;
                dataRow.Cells[4].Range.Text = usuario.direccion;
                listUsuariosCa.Add(usuario);
            }
        }

        /// <summary>
        /// Insertar texto debajo del Titulo Opc. Personalizado 
        /// </summary>
        public void insertTextPersonalizado()
        {
            desprotegerArchivo();
            int iParTitulo = obtenerParrafoDestino(0, Constantes.tagTitulo);

            Application.ActiveDocument.Paragraphs[iParTitulo].Range.InsertParagraphAfter();
            iParTitulo++;
            Application.ActiveDocument.Paragraphs[iParTitulo].Range.Text = Constantes.txtTitulo;
            Application.ActiveDocument.Paragraphs[iParTitulo].Range.Font.Name = "Calibri";
            Application.ActiveDocument.Paragraphs[iParTitulo].Range.Font.Size = 12;
            Application.ActiveDocument.Paragraphs[iParTitulo].Range.Font.Bold = 0;
            Application.ActiveDocument.Paragraphs[iParTitulo].Range.Font.Color = WdColor.wdColorBlack;

            protegerArchivo();
        }

        /// <summary>
        /// Insertar Etiquetas Destinatario
        /// </summary>
        /// <param name="check"></param>
        /// <param name="tag">etiqueta desde la cual se insertara nuevo Destino</param>
        /// <param name="idDestino">identificador del destino a insertar</param>
        public void insertTagDestinoCarta(bool check, string tag, int idDestino)
        {
            int iIndex = obtenerParrafoDestino(idDestino, tag);
            desprotegerArchivo();

            if (check)
                iIndex++;


            currentDocument = Application.ActiveDocument;
            Microsoft.Office.Tools.Word.Document extendedDocument = Globals.Factory.GetVstoObject(currentDocument);
            iIndex++;

            currentDocument.Paragraphs[iIndex].Range.InsertParagraphAfter();
            currentDocument.Paragraphs[iIndex].Range.Select();
            richTextControlNew = extendedDocument.Controls.AddRichTextContentControl(currentDocument.Paragraphs[iIndex].Range, "richTextControlD" + iIndex.ToString());
            richTextControlNew.PlaceholderText = " ";
            richTextControlNew.Tag = idDestino.ToString("00") + Constantes.tagNombreDestino;
            iIndex++;

            currentDocument.Paragraphs[iIndex].Range.InsertParagraphAfter();
            currentDocument.Paragraphs[iIndex].Range.Select();
            richTextControlNew = extendedDocument.Controls.AddRichTextContentControl(currentDocument.Paragraphs[iIndex].Range, "richTextControlD" + iIndex.ToString());
            richTextControlNew.PlaceholderText = " ";
            richTextControlNew.Tag = idDestino.ToString("00") + Constantes.tagCargoDestino;
            iIndex++;

            currentDocument.Paragraphs[iIndex].Range.InsertParagraphAfter();
            currentDocument.Paragraphs[iIndex].Range.Select();
            richTextControlNew = extendedDocument.Controls.AddRichTextContentControl(currentDocument.Paragraphs[iIndex].Range, "richTextControlD" + iIndex.ToString());
            richTextControlNew.PlaceholderText = " ";
            richTextControlNew.Tag = idDestino.ToString("00") + Constantes.tagDependenciaDestino;
            iIndex++;

            currentDocument.Paragraphs[iIndex].Range.InsertParagraphAfter();
            currentDocument.Paragraphs[iIndex].Range.Select();
            richTextControlNew = extendedDocument.Controls.AddRichTextContentControl(currentDocument.Paragraphs[iIndex].Range, "richTextControlD" + iIndex.ToString());
            richTextControlNew.PlaceholderText = " ";
            richTextControlNew.Tag = idDestino.ToString("00") + Constantes.tagCiudadDestino;
            protegerArchivo();
        }

        /*
         * Cambiar las etiquetas del primerDestino Opc.Personalizado
         */

        public void changeTags(int idDestino)
        {
            desprotegerArchivo();

            int iParIniDestino = obtenerParrafoDestino(idDestino, Constantes.tagNombreDestino);
            int iParFinDestino = obtenerParrafoDestino(idDestino, Constantes.tagCiudadDestino);

            while (iParFinDestino >= iParIniDestino)
            {
                var newrange = Application.ActiveDocument.Paragraphs[iParFinDestino + 1].Range.Previous();

                Application.ActiveDocument.Paragraphs[iParFinDestino].Range.Font.Name = "Calibri";
                Application.ActiveDocument.Paragraphs[iParFinDestino].Range.Font.Size = 12;
                Application.ActiveDocument.Paragraphs[iParFinDestino].Range.Font.Color = WdColor.wdColorBlack;

                newrange.InsertBefore(Constantes.marcadorDestino);

                object findStr = Constantes.marcadorDestino;

                while (newrange.Find.Execute(ref findStr))
                {
                    newrange.Font.Size = 10;
                    newrange.Font.Bold = 0;
                    newrange.Font.ColorIndex = Microsoft.Office.Interop.Word.WdColorIndex.wdWhite;
                }

                iParFinDestino--;
            }
            protegerArchivo();
        }

        /*
         * Eliminar Etiquetas destinos Opc.Personalizado
         */

        public void eliminarTags(int idTagIni, string tagIni, int idTagFin, string tagFin)
        {
            desprotegerArchivo();

            int idDestino = idTagFin == 0 ? idTagFin : idTagFin - 1;

            int iParIniDestino = obtenerParrafoDestino(idTagIni, tagIni);
            int iParFinDestino = obtenerParrafoDestino(idDestino, tagFin);

            currentDocument = Application.ActiveDocument;
            Microsoft.Office.Tools.Word.Document extendedDocument = Globals.Factory.GetVstoObject(currentDocument);

            while (iParFinDestino > iParIniDestino)
            {
                extendedDocument.Controls.Remove("richTextControlD" + (iParFinDestino));
                Application.ActiveDocument.Paragraphs[iParFinDestino].Range.Delete();
                iParFinDestino--;
            }           

            for(int i = 0; i < listaDestinosCa.Count; i++)
            {
                eliminarValorPropiedad(String.Concat(i.ToString("00"), Constantes.propiedadPcrDestino));
            }

            listaDestinosCa.Clear();
            protegerArchivo();
        }

        /*
         * Eliminar tabla Opc.Personalizado
         */

        public void eliminarTablaPersonalizada()
        {
            desprotegerArchivo();

            if (oTable != null)
            {
                oTable.Delete();
                oTable = null;
                eliminarNewPage();
            }

            listUsuariosCa.Clear();
            protegerArchivo();
        }

        /*
         * Verificar Extencion del doc actual
         */

        public bool verificarExtencion()
        {
            bool validacion = true;
     
            String properties = Globals.ThisAddIn.Application.ActiveDocument.Name;
            int format = Globals.ThisAddIn.Application.ActiveDocument.SaveFormat;

            if (format != 12)
            {
                MessageBox.Show("Se encuentra trabajando sobre la Plantilla, por favor guarde el archivo como un documento .docx y vuelva a intentarlo.");
                validacion = false;
            }

            return validacion;
        }

        /*
         * Retorna el numero de parrafo para el Tag indicado
         */

        public int obtenerParrafoDestino(int idDestino, string Tag)
        {
            int iPar = 1;
            flag = false;

            while (iPar < Application.ActiveDocument.Paragraphs.Count && !flag)
            {
                if (Application.ActiveDocument.Paragraphs[iPar].Range.ContentControls.Count > 0)
                {
                    foreach (Word.ContentControl contentcontrol in Application.ActiveDocument.Paragraphs[iPar].Range.ContentControls)
                    {
                        if (contentcontrol.Tag == (idDestino.ToString("00") + Tag))
                        {
                            flag = true;
                            break;
                        }
                    }
                }
                iPar++;
            }
            return iPar - 1;
        }

        /*
        * Recorre tabla Personalizada y crea una Lista
        */
        public void leerDestinosPers()
        {
            listUsuariosCa.Clear();
            int indexTabLa = Application.ActiveDocument.Tables.Count;

            foreach (Row fila in Application.ActiveDocument.Tables[indexTabLa].Rows)
            {
                if (!fila.IsFirst)
                {
                    destinoCa = new DestinoCa();
                    destinoCa.nombre = fila.Cells[1].Range.Text.Remove(fila.Cells[1].Range.Text.Count() - 2);
                    destinoCa.cargo = fila.Cells[2].Range.Text.Remove(fila.Cells[2].Range.Text.Count() - 2);
                    destinoCa.dependencia = fila.Cells[3].Range.Text.Remove(fila.Cells[3].Range.Text.Count() - 2);
                    destinoCa.direccion = fila.Cells[4].Range.Text.Remove(fila.Cells[4].Range.Text.Count() - 2);

                    listUsuariosCa.Add(destinoCa);
                }
            }
            oTable = Application.ActiveDocument.Tables[indexTabLa];
        }


        /// <summary>
        /// Elimina el primer bloque de destinos
        /// </summary>
        public void eliminarPrimerDestinoPersonalizado()
        {
            currentDocument = Application.ActiveDocument;
            int iParIniDestino = Globals.ThisAddIn.obtenerParrafoDestino(0, Constantes.tagTitulo) + 1;
            int iParFinDestino = Globals.ThisAddIn.obtenerParrafoDestino(0, Constantes.tagCiudadDestino);
            int iDestino = listaDestinosCa.Count;
            Microsoft.Office.Tools.Word.Document extendedDocument = Globals.Factory.GetVstoObject(currentDocument);
            Globals.ThisAddIn.desprotegerArchivo();

            while (iParFinDestino >= iParIniDestino)
            {
                extendedDocument.Controls.Remove("richTextControlD" + (iParFinDestino - 1));
                extendedDocument.Paragraphs[iParFinDestino].Range.Delete();
                iParFinDestino--;
            }
            eliminarValorPropiedad(String.Concat(iDestino.ToString("00"), Constantes.propiedadPcrDestino));
            Globals.ThisAddIn.protegerArchivo();
        }

        public void verificarAmbiente()
        {
            string txt = "";

            if (sourcePath.Contains(Constantes.ambDesa))
                txt = "Desarrollo";
            
            if (sourcePath.Contains(Constantes.ambPrue))
                txt = "Pruebas";

            if (sourcePath.Contains(Constantes.ambProd))
                txt = "Producción";

            insertarValorPropiedad(Constantes.propiedadAmbiente, txt);
        }


        /// <summary>
        /// Filtra la tabla de usuarios con los valores escritos en los textbox
        /// </summary>
        public void buscarUsuarios(string txtNombre, string txtCargo, string txtDependendcia, DataView vistaUsuarios)
        {
            string outputInfo = "";

            outputInfo = "(NombreBuscar LIKE '%" + txtNombre.Replace('á', 'a').Replace('é', 'e').Replace('í', 'i').Replace('ó', 'o').Replace('ú', 'u').Replace('ñ', 'n') + "%') " +
                "AND ( CargoBuscar LIKE '%" + txtCargo.Replace('á', 'a').Replace('é', 'e').Replace('í', 'i').Replace('ó', 'o').Replace('ú', 'u').Replace('ñ', 'n') + "%') " +
                "AND ( DepartamentoBuscar LIKE '%" + txtDependendcia.Replace('á', 'a').Replace('é', 'e').Replace('í', 'i').Replace('ó', 'o').Replace('ú', 'u').Replace('ñ', 'n') + "%')";

            //Applies the filter to the DataView
            if (outputInfo.Length > 0)
                vistaUsuarios.RowFilter = outputInfo;
        }



        public List<Pais> listViewPaises
        {
            get
            {
                return paises;
            }
        }

        public List<Ciudad> listViewCiudades
        {
            get
            {
                return ciudades;
            }
        }

        public List<Empleado> listaEmpleados
        {
            get
            {
                return listEmpleados;
            }
        }

        public List<DestinoCa> listaDestinosCa
        {
            get
            {
                return listUsuariosCa;
            }            
        }

        public List<DestinoMe> listaDestinosMe
        {
            get
            {
                return listUsuariosMe;
            }
        }

        public List<DestinoCa> listaCopiasCa
        {
            get
            {
                return listCopiasCa;
            }
        }

        public List<DestinoMe> listaCopiasMe
        {
            get
            {
                return listCopiasMe;
            }
        }

        public List<Firmante> listaFirmantes
        {
            get
            {
                return listFirmantes;
            }
        }


        public int iMaxRemitentes
        {
            get
            {
                return imaxRemitente;
            }
            set
            {
                imaxRemitente = value;
            }
        }

        public Table oTablePers
        {
            get
            {
                return oTable;
            }
            set
            {
                oTable = value;
            }
        }

        public int iContVisitasForms
        {
            get
            {
                return iContVisitasForm;
            }
            set
            {
                iContVisitasForm = value;
            }
        }

        public bool iprimerMarcaAgregada
        {
            get
            {
                return primerMarcaAgregada;
            }
            set
            {
                primerMarcaAgregada = value;
            }
        }

        public string ActAddin
        {
            get
            {
                return versionActualAddin;
            }
        }
        public string VersThisAddin
        {
            get
            {
                return versionAddin;
            }
        }


        public string ActPlanME
        {
            get
            {
                return versionActualPlantillaME;
            }
        }

        public string ActPlanCA
        {
            get
            {
                return versionActualPlantillaCA;
            }
        }

        private void ThisAddIn_Shutdown(object sender, System.EventArgs e)
        {
        }

        void Application_DocumentBeforeSave(Word.Document Doc, ref bool SaveAsUI, ref bool Cancel)
        {
            //  Doc.Paragraphs[1].Range.InsertParagraphBefore();
            // Doc.Paragraphs[1].Range.Text = "Texto a insertar";
        }

        #region Código generado por VSTO

        /// <summary>
        /// Método necesario para admitir el Diseñador. No se puede modificar
        /// el contenido de este método con el editor de código.
        /// </summary>
        private void InternalStartup()
        {
            this.Startup += new System.EventHandler(ThisAddIn_Startup);
            this.Shutdown += new System.EventHandler(ThisAddIn_Shutdown);
        }

        #endregion
    }

}


