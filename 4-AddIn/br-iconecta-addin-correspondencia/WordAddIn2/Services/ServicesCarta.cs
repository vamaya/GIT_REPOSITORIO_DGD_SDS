using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using System.Data;
using word = Microsoft.Office.Interop.Word;
using System.Text.RegularExpressions;

namespace CorrWordAddIn
{
    public static class ServicesCarta
    {
        //Metodo para Insertar Datos Formulario Verificar
        public static void insertDataForm(DataGridView dgvForm, List<DestinoCa> listDestinos)
        {
            foreach (DestinoCa destino in listDestinos)
            {
                dgvForm.Rows.Add(destino.nombre, destino.dependencia, destino.direccion);
            }
        }

        //Metodo para Insertar Datos en Tabla
        public static void insertDataTable(DataTable table, List<Empleado> listEmpleados)
        {
            int i = 0;

            foreach (Empleado empleado in listEmpleados)
            {
                table.Rows[i]["IdFirmanteBuscar"] = empleado.idFirmante;
                table.Rows[i]["Nombre"] = empleado.nombre;
                table.Rows[i]["Cargo"] = empleado.cargo;
                table.Rows[i]["CargoIngles"] = empleado.cargoIngles;
                table.Rows[i]["FirmanteBuscar"] = empleado.firmaInt;
                table.Rows[i]["FirmanteCartaBuscar"] = empleado.firmaExt;
                table.Rows[i]["SiglaFirmanteBuscar"] = empleado.sigla;
                table.Rows[i]["Departamento"] = empleado.dependencia;
                table.Rows[i]["DepartamentoIngles"] = empleado.dependenciaIngles;
                table.Rows[i]["FondoIndependiente"] = empleado.fondoIndependiente;
                table.Rows[i]["PCRBuscar"] = empleado.pcrNormal;
                table.Rows[i]["IdPcrBuscar"] = empleado.idPcrNormal;
                table.Rows[i]["PCRConfidencialBuscar"] = empleado.pcrConfidencial;
                table.Rows[i]["IdPcrConfidencialBuscar"] = empleado.idPcrConfidencial;
                table.Rows[i]["CddBuscar"] = empleado.cdd;
                table.Rows[i]["IdCddBuscar"] = empleado.idCdd;
                table.Rows[i]["CiudadDepartamento"] = empleado.ciudad;
                table.Rows[i]["DireccionDepartamento"] = empleado.direccion;
                table.Rows[i]["TelefonoDepartamento"] = empleado.telefono;
                table.Rows[i]["NombreBuscar"] = empleado.nombre.Replace('á', 'a').Replace('é', 'e').Replace('í', 'i').Replace('ó', 'o').Replace('ú', 'u').Replace('ñ', 'n');
                table.Rows[i]["CargoBuscar"] = empleado.cargo.Replace('á', 'a').Replace('é', 'e').Replace('í', 'i').Replace('ó', 'o').Replace('ú', 'u').Replace('ñ', 'n');
                table.Rows[i]["DepartamentoBuscar"] = empleado.dependencia.Replace('á', 'a').Replace('é', 'e').Replace('í', 'i').Replace('ó', 'o').Replace('ú', 'u').Replace('ñ', 'n');
                i++;
            }
        }


        /// <summary>
        /// Carga el combobox de paises, con la información que contiene el archivo de paises
        /// </summary>
        public static void llenarComboPaises(ComboBox cbPais, List<Pais> listPaises)
        {
            cbPais.DataSource = null;
            cbPais.DataSource = listPaises;
            cbPais.DisplayMember = "nombrePais";
            cbPais.ValueMember = "nombrePais";
            cbPais.SelectedValue = "Colombia";
        }


        /// <summary>
        /// Carga el combobox de ciudades, con la información que contiene el archivo de ciudades
        /// </summary>
        public static void llenarComboCiudades(ComboBox cbCiudad, List<Ciudad> listCiudades)
        {
            cbCiudad.DataSource = null;
            cbCiudad.DataSource = listCiudades;
            cbCiudad.DisplayMember = "nombreCiudad";
            cbCiudad.ValueMember = "nombreCiudad";
            //JLG: Se hace cambio para que la ciudad seleccionada por defecto sea Bogotá
            cbCiudad.SelectedValue = "Bogotá D.C. ";
        }

        /// <summary>
        ///  Actualiza el nombre y id del Pcr de la dependencia del firmante, dependiendo si la comunicacion es normal o confidencial
        /// </summary>
        public static void actualizarPropiedadPcrFirmante(bool rbtNormal, bool rbtConfidencial, DataView vistaUsuarios)
        {
            vistaUsuarios.RowFilter = "";

            if (Globals.ThisAddIn.listaFirmantes.Any())
            {
                Globals.ThisAddIn.desprotegerArchivo();
                String pcr = "";
                String idPcr = "";
                int index;

                foreach (Firmante usu in Globals.ThisAddIn.listaFirmantes)
                {
                    if (Globals.ThisAddIn.consultarValorPropiedad(Constantes.propiedadIdiomaIngles).Equals("True"))
                    {
                        index = Globals.ThisAddIn.listaEmpleados.FindIndex(x => x.idFirmante.Equals(usu.idFirmante) && x.nombre.Equals(usu.nombre) && x.cargoIngles.Equals(usu.cargo) 
                        | x.cargo.Equals(usu.cargo) && x.dependenciaIngles.Contains(usu.dependencia));
                    }
                    else
                    {
                        index = Globals.ThisAddIn.listaEmpleados.FindIndex(x => x.idFirmante.Equals(usu.idFirmante) && x.nombre.Equals(usu.nombre) && x.cargo.Equals(usu.cargo) && x.dependencia.Contains(usu.dependencia));
                    }
                    

                    if (rbtNormal)
                    {
                        pcr = Globals.ThisAddIn.listaEmpleados[index].pcrNormal;
                        idPcr = Globals.ThisAddIn.listaEmpleados[index].idPcrNormal;
                        Globals.ThisAddIn.insertarValorPropiedad(String.Concat(Constantes.propiedadPcrFirmante), pcr);
                        Globals.ThisAddIn.insertarValorPropiedad(String.Concat(Constantes.propiedadIdPcrFirmante), idPcr);
                    }
                    if (rbtConfidencial)
                    {
                        pcr = Globals.ThisAddIn.listaEmpleados[index].pcrConfidencial;
                        idPcr = Globals.ThisAddIn.listaEmpleados[index].idPcrConfidencial;
                        Globals.ThisAddIn.insertarValorPropiedad(String.Concat(Constantes.propiedadPcrFirmante), pcr);
                        Globals.ThisAddIn.insertarValorPropiedad(String.Concat(Constantes.propiedadIdPcrFirmante), idPcr);
                    }
                }

                Globals.ThisAddIn.protegerArchivo();
            }

        }

        /// <summary>
        /// Modifica el valor de la propiedad de word "Impresión en el area" a falso
        /// </summary>
        public static void deshabilitarPropiedadImpresion(CheckBox chbImpresionArea, CheckBox chbAFisico, CheckBox chbLogo)
        {
            if (!chbImpresionArea.Checked && !chbAFisico.Checked && chbLogo.Checked)
            {
                Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadImpresionArea, "False");
            }
        }


        /// <summary>
        /// Edita el contenido del marcador anexos, dependiendo
        /// </summary>
        public static void editarMarcadorAnexos(CheckBox chbAElectronico, CheckBox chbIngles, CheckBox chbAFisico)
        {
            if ((chbAElectronico.Checked || chbAFisico.Checked) && !chbIngles.Checked)
            {
                Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorAnexos, "Anexos:");
                Globals.ThisAddIn.ajustarTexto(String.Concat(Constantes.tagAnexos), String.Concat("Incluso lo anunciado.", "*"), 12, word.WdColor.wdColorBlack);
            }

            if ((chbAElectronico.Checked || chbAFisico.Checked) && chbIngles.Checked)
            {
                Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorAnexos, "Attachments:");
                Globals.ThisAddIn.ajustarTexto(String.Concat(Constantes.tagAnexos), String.Concat("Incluso lo anunciado.", "*"), 12, word.WdColor.wdColorBlack);
            }

            if (!chbAElectronico.Checked && !chbAFisico.Checked)
            {
                Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorAnexos, "");
                Globals.ThisAddIn.ajustarTexto(String.Concat(Constantes.tagAnexos), String.Concat(" ", "*"), 1, word.WdColor.wdColorWhite);
            }
        }


        /// <summary>
        /// Valida que el texto ingresado tenga un formato de email.
        /// </summary>
        /// <param name="seMailAComprobar">texto a comprobar</param>   
        /// <returns>Devuelve true si el texto ingresado tiene el formato de email correcto</returns>
        public static bool ComprobarFormatoEmail(string seMailAComprobar)
        {
            string sFormato;
            sFormato = "\\w+([-+.']\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*";
            if (Regex.IsMatch(seMailAComprobar, sFormato))
            {
                if (Regex.Replace(seMailAComprobar, sFormato, String.Empty).Length == 0)
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
            else
            {
                return false;
            }
        }


        /// <summary>
        /// valida que se diligencie correctamente los campos requeridos
        /// </summary>
        /// <returns>devuelve bIncorrecto = true si los datos estan diligenciados de manera incorrecta</returns>
        public static bool validarCampos(string TxtDireccion, bool rbtFisica, bool rbtCorreo, string cbCiudad)
        {
            bool bIncorrecto = false;
            if (string.IsNullOrEmpty(TxtDireccion))
            {
                MessageBox.Show("Favor diligenciar la dirección.");
                bIncorrecto = true;
            }
            else
            {
                if (rbtFisica && TxtDireccion.Contains("@"))
                {
                    MessageBox.Show("La dirección Física no puede llevar @");
                    bIncorrecto = true;
                }
                if (rbtCorreo && ServicesCarta.ComprobarFormatoEmail(TxtDireccion) == false)
                {
                    MessageBox.Show("Favor introducir una dirección de correo válida.");
                    bIncorrecto = true;
                }
            }
            //JLG: se modifica la condición para que valide ciudad solo si el tipo de dirección es físico
            if (rbtFisica && cbCiudad.Equals(""))
            //Fin JLG
            {
                MessageBox.Show("Favor seleccionar la ciudad.");
                bIncorrecto = true;
            }

            return bIncorrecto;
        }


        /// <summary>
        /// Valida en las propiedades del documento, si se ha insertado una dirección electronica
        /// </summary>
        /// <returns>Devuelve  bCorreo = true, si se ha insertado una direccion electronica</returns>
        public static bool validarDireccionesInsertadas()
        {
            bool bCorreo = false;

            for (int iTotalDestinos = 0; iTotalDestinos < Globals.ThisAddIn.listaDestinosCa.Count; iTotalDestinos++)
            {
                string sNumDestino = Globals.ThisAddIn.consultarValorPropiedad(iTotalDestinos.ToString("00") + Constantes.propiedadPcrDestino);
                if (sNumDestino.Contains("@"))
                {
                    bCorreo = true;
                }
            }

            for (int iTotalCopias = 0; iTotalCopias < Globals.ThisAddIn.listaCopiasCa.Count; iTotalCopias++)
            {
                string sNumCopias = Globals.ThisAddIn.consultarValorPropiedad(iTotalCopias.ToString("00") + Constantes.propiedadPcrCopias);
                if (sNumCopias.Contains("@"))
                {
                    bCorreo = true;
                }
            }

            foreach (DestinoCa user in Globals.ThisAddIn.listaDestinosCa)
            {
                if (user.direccion.Contains("@"))
                {
                    bCorreo = true;
                }
            }
            return bCorreo;
        }


        /// <summary>
        /// Remplaza unas determinadas palabras, por la nomenclatura estandarizada
        /// </summary>
        /// <param name="sDireccion">Dirección a remplazar</param>
        /// <returns>Devuelve sNewDireccion con la direccion estandarizada </returns>
        public static string estandarizarNomenclaturaDireccion(string sDireccion)
        {
            //JLG: se modifica la estandarización de direcciones para evitar cambios indeseados.
            //string sNewDireccion = sDireccion.Replace("No ", "#").Replace("no ", "#").Replace("Número", "#").Replace("número", "#").Replace("numero", "#").Replace("Num", "#").Replace("num", "#").Replace("Nro", "#").Replace("nro", "#").Replace("Transversal", "Transv.").Replace("transversal", "Transv.").Replace("Carrera", "Cra.").Replace("carrera", "Cra.").Replace("crr", "Cra.").Replace("cr", "Cra.").Replace("Kr", "Cra.").Replace("Avenida", "Av.").Replace("avenida", "Av.").Replace("Diagonal", "Dg.").Replace("diagonal", "Dg.").Replace("Manzana", "Mz.").Replace("manzana", "Mz.").Replace("Apartamento", "apto.").Replace("apartamento", "apto.").Replace("Conjunto", "conj.").Replace("conjunto", "conj.").Replace("Cjto", "conj.").Replace("cjto", "conj.").Replace("Cto", "conj.").Replace("cto", "conj.").Replace("Departamento", "dpto.").Replace("departamento", "dpto.");
            string sNewDireccion = sDireccion.Replace("Número", "#").Replace("número", "#").Replace("Numero", "#").Replace("numero", "#").Replace("Transversal", "Transv.").Replace("transversal", "Transv.").Replace("Carrera", "Cra.").Replace("carrera", "Cra.").Replace("Kr", "Cra.").Replace("Avenida", "Av.").Replace("avenida", "Av.").Replace("Diagonal", "Dg.").Replace("diagonal", "Dg.").Replace("Manzana", "Mz.").Replace("manzana", "Mz.").Replace("Apartamento", "apto.").Replace("apartamento", "apto.").Replace("Conjunto", "conj.").Replace("conjunto", "conj.").Replace("Cjto", "conj.").Replace("cjto", "conj.").Replace("Departamento", "dpto.").Replace("departamento", "dpto.");

            return sNewDireccion;
        }

        /// <summary>
        /// Llama a un metodo en especifico, dependiendo de los datos ingresados en el formulario .
        /// </summary>
        /// <param name="bPrimer">Indica si el bloque a insertar es el primero o no</param>
        public static void InsertarDatosCarta(bool bPrimer, string txtNombreexterno, string txtCargoExterno, string txtEmpresa)
        {
            if (!string.IsNullOrEmpty(txtNombreexterno) && !string.IsNullOrEmpty(txtCargoExterno) && !string.IsNullOrEmpty(txtEmpresa))
            {
                Globals.ThisAddIn.insertarDestinoCarta(bPrimer);
            }
            if (string.IsNullOrEmpty(txtNombreexterno) && !string.IsNullOrEmpty(txtCargoExterno) && !string.IsNullOrEmpty(txtEmpresa))
            {
                Globals.ThisAddIn.insertarDestinoDosDatos(Constantes.tagCargoDestino, Constantes.tagDependenciaDestino, "Cargo Destinatario.", "Entidad Destinatario.", bPrimer);
            }
            if (!string.IsNullOrEmpty(txtNombreexterno) && !string.IsNullOrEmpty(txtCargoExterno) && string.IsNullOrEmpty(txtEmpresa))
            {
                Globals.ThisAddIn.insertarDestinoDosDatos(Constantes.tagNombreDestino, Constantes.tagCargoDestino, "Nombre Destinatario.", "Cargo Destinatario.", bPrimer);
            }
            if (!string.IsNullOrEmpty(txtNombreexterno) && string.IsNullOrEmpty(txtCargoExterno) && !string.IsNullOrEmpty(txtEmpresa))
            {
                Globals.ThisAddIn.insertarDestinoDosDatos(Constantes.tagNombreDestino, Constantes.tagDependenciaDestino, "Nombre Destinatario.", "Entidad Destinatario.", bPrimer);
            }
            if (string.IsNullOrEmpty(txtCargoExterno) && string.IsNullOrEmpty(txtEmpresa))
            {
                Globals.ThisAddIn.insertarDestinoUnDato(Constantes.tagNombreDestino, "Nombre Destinatario.", bPrimer);
            }
            if (string.IsNullOrEmpty(txtCargoExterno) && string.IsNullOrEmpty(txtNombreexterno))
            {
                Globals.ThisAddIn.insertarDestinoUnDato(Constantes.tagDependenciaDestino, "Entidad Destinatario.", bPrimer);
            }
        }

        public static void insertDestinoPersonalizado(string nombre, string cargo, string dependencia, string direccion,bool rbtFisica, string cbCiudad, string cbPais)
        {           
            DestinoCa destino = new DestinoCa();

            destino.nombre = nombre;
            destino.cargo = cargo;
            destino.dependencia = dependencia;

            if (rbtFisica)
            {
                string sNewDireccion = estandarizarNomenclaturaDireccion(direccion);
                destino.direccion = String.Concat(sNewDireccion, ",", cbCiudad, ",", cbPais);
            }
            else
            {
                if (!string.IsNullOrEmpty(cbCiudad))
                {
                    destino.direccion = String.Concat(direccion, ",", cbCiudad, ",", cbPais);
                }else{
                    destino.direccion = String.Concat(direccion, ",", cbPais);
                }
            }

            Globals.ThisAddIn.InsertTable(destino);

        }


        //Vamaya: se modifica función para que al insertar correo las validaciones de ciiudad no sean tan estrictas y si no estiste la ciudad se guarde el pais
        public static void insertDestino(string nombre, string cargo, string dependencia, string direccion, RadioButton rbtFisica, ComboBox cbCiudad, ComboBox cbPais)
        {
            int iDestino = Globals.ThisAddIn.listaDestinosCa.Count;
            
            DestinoCa destino = new DestinoCa();

            Globals.ThisAddIn.insertarTexto(String.Concat(iDestino.ToString("00"), Constantes.tagNombreDestino), String.Concat(nombre, "*", DateTime.Now.Second.ToString()), true);
            Globals.ThisAddIn.insertarTexto(String.Concat(iDestino.ToString("00"), Constantes.tagCargoDestino), String.Concat(cargo, "*", DateTime.Now.Second.ToString()), true);
            Globals.ThisAddIn.insertarTexto(String.Concat(iDestino.ToString("00"), Constantes.tagDependenciaDestino), String.Concat(dependencia, "*", DateTime.Now.Second.ToString()), true);

            if (!string.IsNullOrEmpty(cbCiudad.Text.ToString()))
                {
                    Globals.ThisAddIn.insertarTexto(String.Concat(iDestino.ToString("00"), Constantes.tagCiudadDestino), String.Concat(cbCiudad.SelectedValue.ToString(), "*", DateTime.Now.Second.ToString()), true);
                }
                else
                {
                    Globals.ThisAddIn.insertarTexto(String.Concat(iDestino.ToString("00"), Constantes.tagCiudadDestino), String.Concat(cbPais.SelectedValue.ToString(), "*", DateTime.Now.Second.ToString()), true);
                }

            if (rbtFisica.Checked)
            {
                String sNewDireccion = estandarizarNomenclaturaDireccion(direccion);

                Globals.ThisAddIn.insertarValorPropiedad(String.Concat(iDestino.ToString("00"), Constantes.propiedadPcrDestino), String.Concat(sNewDireccion, ", ", cbCiudad.SelectedValue.ToString(), ", ", cbPais.SelectedValue.ToString()));
                destino.direccion = String.Concat(sNewDireccion, ",", cbCiudad.SelectedValue.ToString(), ",", cbPais.SelectedValue.ToString());

            }
            else
            {
                Globals.ThisAddIn.insertarValorPropiedad(String.Concat(iDestino.ToString("00"), Constantes.propiedadPcrDestino), direccion);
                destino.direccion = direccion;
            }
            
            destino.nombre = nombre;
            destino.cargo = cargo;
            destino.dependencia = dependencia;
            
            Globals.ThisAddIn.listaDestinosCa.Add(destino);

        }

        public static void eliminarDestinoPersonalizado(string nombre, string cargo, string dependencia)
        {
            Usuario usuario = new Usuario();

            usuario.nombre = nombre;
            usuario.cargo = cargo;
            usuario.dependencia = dependencia;

            Globals.ThisAddIn.eliminarDestinoMasivo(usuario);

        }


        public static bool insertarRemitente(DataGridView dgvUsuarios, bool chbIngles,bool isInsert)
        {
            int iRemitente;
            string idFirmante = dgvUsuarios.CurrentRow.Cells["IdFirmanteBuscar"].FormattedValue.ToString();
            string nombre = dgvUsuarios.CurrentRow.Cells["Nombre"].FormattedValue.ToString();
            string sNewDepartamento = dgvUsuarios.CurrentRow.Cells["Departamento"].FormattedValue.ToString();
            string ciudad = dgvUsuarios.CurrentRow.Cells["CiudadDepartamento"].Value.ToString();
            string sigla = dgvUsuarios.CurrentRow.Cells["SiglaFirmanteBuscar"].FormattedValue.ToString();
            string departamentoIngles = dgvUsuarios.CurrentRow.Cells["DepartamentoIngles"].Value.ToString();
            string cargoIngles = dgvUsuarios.CurrentRow.Cells["CargoIngles"].FormattedValue.ToString();
            string cargo = dgvUsuarios.CurrentRow.Cells["Cargo"].FormattedValue.ToString();         

            Firmante firmante = new Firmante();


            if (!ciudad.Contains("Bogotá"))
            {
                string[] sDepartamento = sNewDepartamento.Split('-');
                sNewDepartamento = sDepartamento[0];
            }

            if (chbIngles && (cargoIngles.Equals("") || departamentoIngles.Equals("")))
            {
                MessageBox.Show("El usuario no tiene cargo o dependencia registrada en inglés");
                return false;
            }

            if (!isInsert)            
                Globals.ThisAddIn.listaFirmantes.RemoveAt(Globals.ThisAddIn.listaFirmantes.Count - 1);
            

            iRemitente = Globals.ThisAddIn.listaFirmantes.Count;
            

            Globals.ThisAddIn.insertarTexto(iRemitente.ToString("00") + Constantes.tagNombreFirmante, String.Concat(nombre, "*", DateTime.Now.Second.ToString()), false);
            Globals.ThisAddIn.insertarValorPropiedad(iRemitente.ToString("00") + Constantes.propiedadIdFirmante, idFirmante);
            Globals.ThisAddIn.insertarValorPropiedad(iRemitente.ToString("00") + Constantes.propiedadDependenciaFirmante, sigla);

            if (chbIngles && departamentoIngles != null)
            {
                Globals.ThisAddIn.insertarTexto(iRemitente.ToString("00") + Constantes.tagCargoFirmante, String.Concat(cargoIngles, "*", DateTime.Now.Second.ToString()), false);
                Globals.ThisAddIn.insertarTexto(iRemitente.ToString("00") + Constantes.tagDependenciaFirmante, String.Concat(departamentoIngles, "*", DateTime.Now.Second.ToString()), false);

                firmante.cargo = cargoIngles;
                firmante.dependencia = departamentoIngles;
            }
            else
            {
                Globals.ThisAddIn.insertarTexto(iRemitente.ToString("00") + Constantes.tagCargoFirmante, String.Concat(cargo, "*", DateTime.Now.Second.ToString()), false);
                Globals.ThisAddIn.insertarTexto(iRemitente.ToString("00") + Constantes.tagDependenciaFirmante, String.Concat(sNewDepartamento, "*", DateTime.Now.Second.ToString()), false);

                firmante.cargo = cargo;
                firmante.dependencia = sNewDepartamento;
            }

            firmante.idFirmante = idFirmante;
            firmante.nombre = nombre;

            Globals.ThisAddIn.listaFirmantes.Add(firmante);
            return true;

        }

        public static void cambiarPropRemitente(DataGridView dgvUsuarios, bool rbtNormal)
        {
            Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorValorDireccionPagPpal, dgvUsuarios.CurrentRow.Cells["DireccionDepartamento"].FormattedValue.ToString());
            Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorValorDireccionlOtrasPag, dgvUsuarios.CurrentRow.Cells["DireccionDepartamento"].FormattedValue.ToString());
            Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorDireccionPagPpal, "");
            Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorDireccionlOtrasPag, "");
            Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorValorTelefonoPagPpal, dgvUsuarios.CurrentRow.Cells["TelefonoDepartamento"].FormattedValue.ToString());
            Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorValorTelefonolOtrasPag, dgvUsuarios.CurrentRow.Cells["TelefonoDepartamento"].FormattedValue.ToString());
            Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorTelefononPagPpal, "Tel.:");
            Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorTelefonolOtrasPag, "Tel.:");
            Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorCiudad, dgvUsuarios.CurrentRow.Cells["CiudadDepartamento"].FormattedValue.ToString());

            Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadCddFirmante, dgvUsuarios.CurrentRow.Cells["CddBuscar"].FormattedValue.ToString());
            Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadIdCddFirmante, dgvUsuarios.CurrentRow.Cells["IdCddBuscar"].FormattedValue.ToString());

            if (rbtNormal)
            {
                Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadPcrFirmante, dgvUsuarios.CurrentRow.Cells["PCRBuscar"].FormattedValue.ToString());
                Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadIdPcrFirmante, dgvUsuarios.CurrentRow.Cells["IdPcrBuscar"].FormattedValue.ToString());
            }
            else
            {
                Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadPcrFirmante, dgvUsuarios.CurrentRow.Cells["PCRConfidencialBuscar"].FormattedValue.ToString());
                Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadIdPcrFirmante, dgvUsuarios.CurrentRow.Cells["IdPcrConfidencialBuscar"].FormattedValue.ToString());
            }

            if (dgvUsuarios.CurrentRow.Cells["FondoIndependiente"].FormattedValue.ToString() == "1")
            {
                Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadFondoIndependiente, "True");
            }
            else
            {
                Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadFondoIndependiente, "False");
            }
        }


        /// <summary>
        /// Llama al metodo RemplazarFormatoTexto con unos parametros en especificos, dependiendo de los datos ingresados en el formulario.
        /// </summary>
        public static void ActualizarFormatoCopias(string txtNombreexterno, string txtCargoExterno, string txtEmpresa)
        {
            if (string.IsNullOrEmpty(txtNombreexterno) && !string.IsNullOrEmpty(txtCargoExterno) && !string.IsNullOrEmpty(txtEmpresa))
            {
                Globals.ThisAddIn.desprotegerArchivo();
                Globals.ThisAddIn.Application.ActiveDocument.ToggleFormsDesign();
                Globals.ThisAddIn.RemplazarFormatoTexto("., ", ", ");
                Globals.ThisAddIn.RemplazarFormatoTexto(".", " ");
                Globals.ThisAddIn.Application.ActiveDocument.ToggleFormsDesign();
                Globals.ThisAddIn.protegerArchivo();
            }
            if (!string.IsNullOrEmpty(txtNombreexterno) && !string.IsNullOrEmpty(txtCargoExterno) && string.IsNullOrEmpty(txtEmpresa))
            {
                Globals.ThisAddIn.desprotegerArchivo();
                Globals.ThisAddIn.Application.ActiveDocument.ToggleFormsDesign();
                Globals.ThisAddIn.RemplazarFormatoTexto(", .", ", ");
                Globals.ThisAddIn.RemplazarFormatoTexto(".", " ");
                Globals.ThisAddIn.Application.ActiveDocument.ToggleFormsDesign();
                Globals.ThisAddIn.protegerArchivo();
            }
            if (!string.IsNullOrEmpty(txtNombreexterno) && string.IsNullOrEmpty(txtCargoExterno) && !string.IsNullOrEmpty(txtEmpresa))
            {
                Globals.ThisAddIn.desprotegerArchivo();
                Globals.ThisAddIn.Application.ActiveDocument.ToggleFormsDesign();
                Globals.ThisAddIn.RemplazarFormatoTexto(" ,", " ,");
                Globals.ThisAddIn.RemplazarFormatoTexto(".", " ");
                Globals.ThisAddIn.Application.ActiveDocument.ToggleFormsDesign();
                Globals.ThisAddIn.protegerArchivo();
            }
            if (string.IsNullOrEmpty(txtCargoExterno) && string.IsNullOrEmpty(txtEmpresa))
            {
                Globals.ThisAddIn.desprotegerArchivo();
                Globals.ThisAddIn.Application.ActiveDocument.ToggleFormsDesign();
                Globals.ThisAddIn.RemplazarFormatoTexto(", , .", ", , ");
                Globals.ThisAddIn.RemplazarFormatoTexto(".", " ");
                Globals.ThisAddIn.Application.ActiveDocument.ToggleFormsDesign();
                Globals.ThisAddIn.protegerArchivo();
            }

            if (string.IsNullOrEmpty(txtCargoExterno) && string.IsNullOrEmpty(txtNombreexterno))
            {
                Globals.ThisAddIn.desprotegerArchivo();
                Globals.ThisAddIn.Application.ActiveDocument.ToggleFormsDesign();
                Globals.ThisAddIn.RemplazarFormatoTexto("., , ", ", ,");
                Globals.ThisAddIn.RemplazarFormatoTexto(".", " ");
                Globals.ThisAddIn.Application.ActiveDocument.ToggleFormsDesign();
                Globals.ThisAddIn.protegerArchivo();
            }
        }


        public static void insertarCopia(string nombre, string cargo, string dependencia, string direccion, bool rbtFisica, string ciudad, string pais)
        {
            DestinoCa destino = new DestinoCa();
            int iCopia = Globals.ThisAddIn.listaCopiasCa.Count;
            string sNewDireccion = ServicesCarta.estandarizarNomenclaturaDireccion(direccion);

            if (rbtFisica)
            {
                Globals.ThisAddIn.insertarValorPropiedad(String.Concat(iCopia.ToString("00"), Constantes.propiedadPcrCopias), String.Concat(sNewDireccion, ", ", ciudad, ", ", pais));
            }
            else
            {
                Globals.ThisAddIn.insertarValorPropiedad(String.Concat(iCopia.ToString("00"), Constantes.propiedadPcrCopias), direccion);
            }

            if (!string.IsNullOrEmpty(nombre) && !string.IsNullOrEmpty(cargo) && !string.IsNullOrEmpty(dependencia))
            {
                Globals.ThisAddIn.insertarTexto(String.Concat(iCopia.ToString("00"), Constantes.tagCopias), String.Concat(nombre, ", ", cargo, ", ", dependencia, "*", DateTime.Now.Second.ToString()), false);
            }
            else
            {
                Globals.ThisAddIn.insertarTexto(String.Concat(iCopia.ToString("00"), Constantes.tagCopias), String.Concat(".", nombre, ", ", cargo, ", ", dependencia, ".*", DateTime.Now.Second.ToString()), false);
                ActualizarFormatoCopias(nombre, cargo, dependencia);
            }

            destino.nombre = nombre;
            destino.cargo = cargo;
            destino.dependencia = dependencia;
            destino.direccion = direccion;

            Globals.ThisAddIn.listaCopiasCa.Add(destino);

        }


        /// <summary>
        /// Edita el marcador "Confidencial", dependiendo de los valores ingresados en el formulario.
        /// </summary>
        public static void editarMarcadorConfidencial(bool rbtNormal, bool rbtConfidencial, bool chbIngles)
        {
            if (rbtNormal)
            {
                Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorConfidencialPagPpal, "");
                Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorConfidencialOtrasPag, "");
                return;
            }

            if (rbtConfidencial && !chbIngles)
            {
                Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorConfidencialPagPpal, "CONFIDENCIAL");
                Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorConfidencialOtrasPag, "CONFIDENCIAL");
            }

            if (rbtConfidencial && chbIngles)
            {
                Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorConfidencialPagPpal, "CONFIDENTIAL");
                Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorConfidencialOtrasPag, "CONFIDENTIAL");
            }
        }


        /// <summary>
        /// Agrega/Edita la marca de agua del documento, dependiendo de los valores ingresados en el documento
        /// </summary>
        public static void manipularMarcaAgua(bool primerMarcaAgregada, bool rbtNormal, bool rbtConfidencial, bool chbIngles)
        {
            if (primerMarcaAgregada)
            {
                if (rbtNormal)
                {
                    Globals.ThisAddIn.actualizarMarcaAgua("marcaPagPpal", " ");
                    Globals.ThisAddIn.actualizarMarcaAgua("marcaOtrasPag", " ");
                    return;
                }
                if (rbtConfidencial && !chbIngles)
                {
                    Globals.ThisAddIn.actualizarMarcaAgua("marcaPagPpal", "CONFIDENCIAL");
                    Globals.ThisAddIn.actualizarMarcaAgua("marcaOtrasPag", "CONFIDENCIAL");
                }

                if (rbtConfidencial && chbIngles)
                {
                    Globals.ThisAddIn.actualizarMarcaAgua("marcaPagPpal", "CONFIDENTIAL");
                    Globals.ThisAddIn.actualizarMarcaAgua("marcaOtrasPag", "CONFIDENTIAL");
                }
            }
            else
            {
                if (rbtNormal)
                {
                    Globals.ThisAddIn.agregarMarcaAgua(word.WdHeaderFooterIndex.wdHeaderFooterFirstPage, "marcaPagPpal", " ");
                    Globals.ThisAddIn.agregarMarcaAgua(word.WdHeaderFooterIndex.wdHeaderFooterPrimary, "marcaOtrasPag", " ");
                }

                if (rbtConfidencial)
                {
                    if (!chbIngles)
                    {
                        Globals.ThisAddIn.agregarMarcaAgua(word.WdHeaderFooterIndex.wdHeaderFooterFirstPage, "marcaPagPpal", "CONFIDENCIAL");
                        Globals.ThisAddIn.agregarMarcaAgua(word.WdHeaderFooterIndex.wdHeaderFooterPrimary, "marcaOtrasPag", "CONFIDENCIAL");
                    }
                    if (chbIngles)
                    {
                        Globals.ThisAddIn.agregarMarcaAgua(word.WdHeaderFooterIndex.wdHeaderFooterFirstPage, "marcaPagPpal", "CONFIDENTIAL");
                        Globals.ThisAddIn.agregarMarcaAgua(word.WdHeaderFooterIndex.wdHeaderFooterPrimary, "marcaOtrasPag", "CONFIDENTIAL");
                    }

                }
                Globals.ThisAddIn.iprimerMarcaAgregada = true;
                Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadPrimerMarcaAgua, "True");
            }
        }


        public static void loadDestinos()
        {
            Globals.ThisAddIn.listaDestinosCa.Clear();

            for (int iTotalDestinos = 0; iTotalDestinos < Constantes.maxDestinatariosCarta; iTotalDestinos++)
            {
                string sNumDestino = Globals.ThisAddIn.consultarValorPropiedad(iTotalDestinos.ToString("00") + Constantes.propiedadPcrDestino);
                if (sNumDestino.Equals(iTotalDestinos.ToString("00") + Constantes.propiedadPcrDestino))
                {
                    break;
                }
                else
                {
                    DestinoCa destino = new DestinoCa();
                    destino.nombre = Globals.ThisAddIn.consultarPlaceholderEtiqueta(iTotalDestinos.ToString("00") + Constantes.tagNombreDestino);
                    destino.cargo = Globals.ThisAddIn.consultarPlaceholderEtiqueta(iTotalDestinos.ToString("00") + Constantes.tagCargoDestino).ToString();
                    destino.dependencia = Globals.ThisAddIn.consultarPlaceholderEtiqueta(iTotalDestinos.ToString("00") + Constantes.tagDependenciaDestino);
                    destino.direccion = Globals.ThisAddIn.consultarValorPropiedad(iTotalDestinos.ToString("00") + Constantes.propiedadPcrDestino);

                    Globals.ThisAddIn.listaDestinosCa.Add(destino);
                }
            }
        }

        public static void loadCopias()
        {
            Globals.ThisAddIn.listaCopiasCa.Clear();

            for (int iTotalCopias = 0; iTotalCopias < Constantes.maxCopiasCarta; iTotalCopias++)
            {
                string sNumCopias = Globals.ThisAddIn.consultarValorPropiedad(iTotalCopias.ToString("00") + Constantes.propiedadPcrCopias);
                if (sNumCopias.Equals(iTotalCopias.ToString("00") + Constantes.propiedadPcrCopias))
                {
                    break;
                }
                else
                {
                    DestinoCa destino = new DestinoCa();
                    String[] copias = Globals.ThisAddIn.consultarPlaceholderEtiqueta(iTotalCopias.ToString("00") + Constantes.tagCopias).Split(',');

                    destino.nombre = copias[0];
                    destino.cargo = copias[1].TrimStart(' ');
                    destino.dependencia = copias[2].TrimStart(' ');
                    destino.direccion = Globals.ThisAddIn.consultarValorPropiedad(iTotalCopias.ToString("00") + Constantes.propiedadPcrCopias);

                    Globals.ThisAddIn.listaCopiasCa.Add(destino);
                }

            }
        }

        public static void loadFirmantes()
        {
            Globals.ThisAddIn.listaFirmantes.Clear();

            for (int iTotalFirmantes = 0; iTotalFirmantes < Globals.ThisAddIn.iMaxRemitentes; iTotalFirmantes++)
            {
                string sNumDestino = Globals.ThisAddIn.consultarValorPropiedad(iTotalFirmantes.ToString("00") + Constantes.propiedadIdFirmante);

                if (sNumDestino.Equals(iTotalFirmantes.ToString("00") + Constantes.propiedadIdFirmante))
                {
                    break;
                }
                else
                {
                    Firmante firmante = new Firmante();
                    firmante.nombre = Globals.ThisAddIn.consultarPlaceholderEtiqueta(iTotalFirmantes.ToString("00") + Constantes.tagNombreFirmante);
                    firmante.cargo = Globals.ThisAddIn.consultarPlaceholderEtiqueta(iTotalFirmantes.ToString("00") + Constantes.tagCargoFirmante).ToString();
                    firmante.dependencia = Globals.ThisAddIn.consultarPlaceholderEtiqueta(iTotalFirmantes.ToString("00") + Constantes.tagDependenciaFirmante);
                    firmante.idFirmante = Globals.ThisAddIn.consultarValorPropiedad(iTotalFirmantes.ToString("00") + Constantes.propiedadIdFirmante);

                    Globals.ThisAddIn.listaFirmantes.Add(firmante);
                }
            }
        }

        public static bool cambiarTagsFirmante(bool chbIngles, int indexFirmante, string idBuscar, string nombreBuscar, string cargoBuscar)
        {
            bool isInsert = false;

            if (Globals.ThisAddIn.listaFirmantes.Any())
            {
                string cargo = "";
                string departamento = "";
                int index;

                index = Globals.ThisAddIn.listaEmpleados.FindIndex(x => x.idFirmante.Equals(idBuscar) && x.nombre.Equals(nombreBuscar) && x.cargo.Equals(cargoBuscar) |
                    x.cargoIngles.Equals(cargoBuscar));

                if (chbIngles)
                {           
                    if(Globals.ThisAddIn.listaEmpleados[index].cargoIngles.Equals("") || Globals.ThisAddIn.listaEmpleados[index].dependenciaIngles.Equals(""))
                    {
                        MessageBox.Show("El usuario no tiene cargo o dependencia registrada en inglés");
                        return isInsert;
                    }
                    else
                    {
                        cargo = Globals.ThisAddIn.listaEmpleados[index].cargoIngles;
                        departamento = Globals.ThisAddIn.listaEmpleados[index].dependenciaIngles;
                    }
                                        
                }
                else
                {
                    cargo = Globals.ThisAddIn.listaEmpleados[index].cargo;                    
                    departamento = Globals.ThisAddIn.listaEmpleados[index].dependencia;

                    string[] sDepartamento = departamento.Split('-');
                    departamento = sDepartamento[0];
                }               

                Globals.ThisAddIn.insertarTexto(indexFirmante.ToString("00") + Constantes.tagCargoFirmante, String.Concat(cargo, "*", DateTime.Now.Second.ToString()), false);
                Globals.ThisAddIn.insertarTexto(indexFirmante.ToString("00") + Constantes.tagDependenciaFirmante, String.Concat(departamento, "*", DateTime.Now.Second.ToString()), false);

                index = Globals.ThisAddIn.listaFirmantes.FindIndex(x => x.idFirmante.Equals(idBuscar) && x.nombre.Equals(nombreBuscar) && x.cargo.Equals(cargoBuscar));
                Globals.ThisAddIn.listaFirmantes[index].cargo = cargo;
                Globals.ThisAddIn.listaFirmantes[index].dependencia = departamento;

                isInsert = true;
            }

            return isInsert;
        }

    }
}
