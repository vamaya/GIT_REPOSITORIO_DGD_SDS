using CorrWordAddIn;
using Microsoft.Office.Interop.Word;
using System;
using System.Collections.Generic;
using System.Data;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace CorrWordAddIn
{
    public static class ServicesMemorando
    {
        public static void insertDataTable(System.Data.DataTable tblUsuarios, List<CorrWordAddIn.Empleado> listaEmpleados)
        {
            int i = 0;

            foreach (Empleado empleado in listaEmpleados)
            {
                tblUsuarios.Rows[i]["IdFirmanteBuscar"] = empleado.idFirmante;
                tblUsuarios.Rows[i]["Nombre"] = empleado.nombre;
                tblUsuarios.Rows[i]["Cargo"] = empleado.cargo;
                tblUsuarios.Rows[i]["FirmanteBuscar"] = empleado.firmaInt;
                tblUsuarios.Rows[i]["SiglaFirmanteBuscar"] = empleado.sigla;
                tblUsuarios.Rows[i]["Departamento"] = empleado.dependencia;
                tblUsuarios.Rows[i]["FondoIndependiente"] = empleado.fondoIndependiente;
                tblUsuarios.Rows[i]["PCRBuscar"] = empleado.pcrNormal;
                tblUsuarios.Rows[i]["PCRConfidencialBuscar"] = empleado.pcrConfidencial;
                tblUsuarios.Rows[i]["CiudadDepartamento"] = empleado.ciudad;
                tblUsuarios.Rows[i]["NombreBuscar"] = empleado.nombre.Replace('á', 'a').Replace('é', 'e').Replace('í', 'i').Replace('ó', 'o').Replace('ú', 'u').Replace('ñ', 'n');
                tblUsuarios.Rows[i]["CargoBuscar"] = empleado.cargo.Replace('á', 'a').Replace('é', 'e').Replace('í', 'i').Replace('ó', 'o').Replace('ú', 'u').Replace('ñ', 'n');
                tblUsuarios.Rows[i]["DepartamentoBuscar"] = empleado.dependencia.Replace('á', 'a').Replace('é', 'e').Replace('í', 'i').Replace('ó', 'o').Replace('ú', 'u').Replace('ñ', 'n');
                i++;
            }


        }

        /// <summary>
        ///  Actualiza el nombre y id del Pcr de la dependencia del firmante, dependiendo si la comunicacion es normal o confidencial
        /// <param name="sPropiedad">Propiedad a consulatr y actualizar</param>
        /// <param name="iVariable">Max de propiedades a consultar y actualizar</param>
        /// </summary>
        public static void actualizarRolPropiedad(string sPropiedad, List<DestinoMe> listaDestinos, RadioButton rbtNormal, RadioButton rbtConfidencial, DataView vistaUsuarios)
        {

            vistaUsuarios.RowFilter = "";            

            if (listaDestinos.Any())
            {
                Globals.ThisAddIn.desprotegerArchivo();
                int indexDest = 0;
                String pcr = "";

                foreach (Usuario usu in listaDestinos)
                {
                    int index = Globals.ThisAddIn.listaEmpleados.FindIndex(x => x.nombre.Equals(usu.nombre) && x.cargo.Equals(usu.cargo) && x.dependencia.Contains(usu.dependencia));

                    if (rbtNormal.Checked)
                    {
                        pcr = Globals.ThisAddIn.listaEmpleados[index].pcrNormal;
                        Globals.ThisAddIn.insertarValorPropiedad(String.Concat(indexDest.ToString("00"), sPropiedad), pcr);
                    }
                    if (rbtConfidencial.Checked)
                    {
                        pcr = Globals.ThisAddIn.listaEmpleados[index].pcrConfidencial;
                        Globals.ThisAddIn.insertarValorPropiedad(String.Concat(indexDest.ToString("00"), sPropiedad), pcr);
                    }
                    indexDest++;
                }

                Globals.ThisAddIn.protegerArchivo();
            }

        }

        /// <summary>
        /// Inserta un destinatario al documento
        /// </summary>
        public static void insertarRegistro(DataGridView dgvUsuarios, RadioButton rbtConfidencial, TextBox txtNombre, Button btInsertarPara)
        {
            DestinoMe usuario = new DestinoMe();
            int iDestino = Globals.ThisAddIn.listaDestinosMe.Count;
            string nombre = dgvUsuarios.CurrentRow.Cells["Nombre"].FormattedValue.ToString();
            string cargo = dgvUsuarios.CurrentRow.Cells["Cargo"].FormattedValue.ToString();
            string dependencia = dgvUsuarios.CurrentRow.Cells["Departamento"].FormattedValue.ToString();
            string ciudad = dgvUsuarios.CurrentRow.Cells["CiudadDepartamento"].Value.ToString();
            string pcrConfidencial = dgvUsuarios.CurrentRow.Cells["PCRConfidencialBuscar"].FormattedValue.ToString();
            string pcrNormal = dgvUsuarios.CurrentRow.Cells["PCRBuscar"].FormattedValue.ToString();
            string sNewDepartamento = dependencia;

            if (!ciudad.Contains("Bogotá"))
            {
                string[] sDepartamento = dependencia.Split('-');
                sNewDepartamento = sDepartamento[0];
            }

            Globals.ThisAddIn.insertarTexto(String.Concat(iDestino.ToString("00"), Constantes.tagNombreDestino), String.Concat(nombre, "*", DateTime.Now.Second.ToString()), false);
            Globals.ThisAddIn.insertarTexto(String.Concat(iDestino.ToString("00"), Constantes.tagCargoDestino), String.Concat(cargo, "*", DateTime.Now.Second.ToString()), false);
            Globals.ThisAddIn.insertarTexto(String.Concat(iDestino.ToString("00"), Constantes.tagDependenciaDestino), String.Concat(sNewDepartamento, "*", DateTime.Now.Second.ToString()), false);

            if (rbtConfidencial.Checked)
            {
                Globals.ThisAddIn.insertarValorPropiedad(String.Concat(iDestino.ToString("00"), Constantes.propiedadPcrDestino), pcrConfidencial);
                usuario.pcrConfidencial = pcrConfidencial;
            }
            else
            {
                Globals.ThisAddIn.insertarValorPropiedad(String.Concat(iDestino.ToString("00"), Constantes.propiedadPcrDestino), pcrNormal);
                usuario.pcrNormal = pcrNormal;
            }

            txtNombre.Text = "";

            if (iDestino == Constantes.maxDestinatariosMemorando - 1)
            {
                btInsertarPara.Enabled = false;
            }

            usuario.nombre = nombre;
            usuario.cargo = cargo;
            usuario.dependencia = dependencia;

            Globals.ThisAddIn.listaDestinosMe.Add(usuario);

        }


        /// <summary>
        /// Actualiza remitente del documento
        /// </summary>
        public static void actualizarRemitente(DataGridView dgvUsuarios, int iRemitentes)
        {
            string idFirmante = dgvUsuarios.CurrentRow.Cells["IdFirmanteBuscar"].FormattedValue.ToString();
            string nombre = dgvUsuarios.CurrentRow.Cells["Nombre"].FormattedValue.ToString();
            string cargo = dgvUsuarios.CurrentRow.Cells["Cargo"].FormattedValue.ToString();
            string newdepartamento = dgvUsuarios.CurrentRow.Cells["Departamento"].FormattedValue.ToString();
            string ciudad = dgvUsuarios.CurrentRow.Cells["CiudadDepartamento"].Value.ToString();
            string sigla = dgvUsuarios.CurrentRow.Cells["SiglaFirmanteBuscar"].FormattedValue.ToString();
            Firmante firmante = new Firmante();

            if (!ciudad.Contains("Bogotá"))
            {
                string[] departamento = newdepartamento.Split('-');
                newdepartamento = departamento[0];
            }

            Globals.ThisAddIn.insertarTexto(iRemitentes.ToString("00") + Constantes.tagNombreFirmante, String.Concat(nombre, "*", DateTime.Now.Second.ToString()), false);
            Globals.ThisAddIn.insertarTexto(iRemitentes.ToString("00") + Constantes.tagCargoFirmante, String.Concat(cargo, "*", DateTime.Now.Second.ToString()), false);
            Globals.ThisAddIn.insertarTexto(iRemitentes.ToString("00") + Constantes.tagDependenciaFirmante, String.Concat(newdepartamento, "*", DateTime.Now.Second.ToString()), false);
            Globals.ThisAddIn.insertarValorPropiedad(iRemitentes.ToString("00") + Constantes.propiedadIdFirmante, idFirmante);
            Globals.ThisAddIn.insertarValorPropiedad(iRemitentes.ToString("00") + Constantes.propiedadDependenciaFirmante, sigla);

            firmante.idFirmante = idFirmante;
            firmante.nombre = nombre;
            firmante.cargo = cargo;
            firmante.dependencia = newdepartamento;

            Globals.ThisAddIn.listaFirmantes.Add(firmante);
        }

        /// <summary>
        /// inserta una copia al documento
        /// </summary>
        public static void insertarCopia(DataGridView dgvUsuarios, RadioButton rbtConfidencial, Button btnInsertarCopia, TextBox txtNombre)
        {
            DestinoMe usuario = new DestinoMe();
            int iCopia = Globals.ThisAddIn.listaCopiasMe.Count;
            string nombre = dgvUsuarios.CurrentRow.Cells["Nombre"].FormattedValue.ToString();
            string cargo = dgvUsuarios.CurrentRow.Cells["Cargo"].FormattedValue.ToString();
            string dependencia = dgvUsuarios.CurrentRow.Cells["Departamento"].FormattedValue.ToString();
            string ciudad = dgvUsuarios.CurrentRow.Cells["CiudadDepartamento"].Value.ToString();
            string pcrConfidencial = dgvUsuarios.CurrentRow.Cells["PCRConfidencialBuscar"].FormattedValue.ToString();
            string pcrNormal = dgvUsuarios.CurrentRow.Cells["PCRBuscar"].FormattedValue.ToString();
            string sNewDepartamento = dependencia;

            if (!ciudad.Contains("Bogotá"))
            {
                string[] sDepartamento = dependencia.Split('-');
                sNewDepartamento = sDepartamento[0];
            }

            Globals.ThisAddIn.insertarTexto(String.Concat(iCopia.ToString("00"), Constantes.tagCopias), String.Concat(nombre, ", ", cargo, ", ", sNewDepartamento, "*", DateTime.Now.Second.ToString()), false);

            if (rbtConfidencial.Checked)
            {
                Globals.ThisAddIn.insertarValorPropiedad(String.Concat(iCopia.ToString("00"), Constantes.propiedadPcrCopias), pcrConfidencial);
            }
            else
            {
                Globals.ThisAddIn.insertarValorPropiedad(String.Concat(iCopia.ToString("00"), Constantes.propiedadPcrCopias), pcrNormal);
            }

            if (iCopia == Constantes.maxCopiasMemorando - 1)
            {
                btnInsertarCopia.Enabled = false;
            }

            usuario.nombre = nombre;
            usuario.cargo = cargo;
            usuario.dependencia = dependencia;

            Globals.ThisAddIn.listaCopiasMe.Add(usuario);

            txtNombre.Text = "";


        }

        /// <summary>
        /// habilita componentes de acuerdo al estado check de copias
        /// </summary>
        public static void estadoCopias(Button btnInsertarCopia, Button btnActualizarCopia, Button btnEliminarCopia)
        {

            int iCopiasActuales = Globals.ThisAddIn.listaCopiasMe.Count;
            for (; iCopiasActuales > 0; iCopiasActuales--)
            {
                Globals.ThisAddIn.eliminarCopia(iCopiasActuales);
            }

            Globals.ThisAddIn.ajustarTexto("00" + Constantes.tagCopias, ",, *" + DateTime.Now.Second.ToString(), 1, WdColor.wdColorWhite);
            Globals.ThisAddIn.ajustarTexto(Constantes.tagTituloCopias, " *" + DateTime.Now.Second.ToString(), 1, WdColor.wdColorWhite);
            btnInsertarCopia.Enabled = false;
            btnActualizarCopia.Enabled = false;
            btnEliminarCopia.Enabled = false;

        }

        /// <summary>
        /// Agrega/Edita la marca de agua del documento, dependiendo de los valores ingresados en el documento.
        /// </summary>
        public static bool manipularMarcaAgua(bool primerMarcaAgregada, RadioButton rbtNormal)
        {
            if (primerMarcaAgregada)
            {
                if (rbtNormal.Checked)
                {
                    Globals.ThisAddIn.actualizarMarcaAgua("marcaPagPpal", " ");
                    Globals.ThisAddIn.actualizarMarcaAgua("marcaOtrasPag", " ");

                }
                else
                {
                    Globals.ThisAddIn.actualizarMarcaAgua("marcaPagPpal", "CONFIDENCIAL");
                    Globals.ThisAddIn.actualizarMarcaAgua("marcaOtrasPag", "CONFIDENCIAL");
                }
            }
            else
            {
                if (rbtNormal.Checked)
                {
                    Globals.ThisAddIn.agregarMarcaAgua(WdHeaderFooterIndex.wdHeaderFooterFirstPage, "marcaPagPpal", " ");
                    Globals.ThisAddIn.agregarMarcaAgua(WdHeaderFooterIndex.wdHeaderFooterPrimary, "marcaOtrasPag", " ");
                }
                else
                {
                    Globals.ThisAddIn.agregarMarcaAgua(WdHeaderFooterIndex.wdHeaderFooterFirstPage, "marcaPagPpal", "CONFIDENCIAL");
                    Globals.ThisAddIn.agregarMarcaAgua(WdHeaderFooterIndex.wdHeaderFooterPrimary, "marcaOtrasPag", "CONFIDENCIAL");
                }

                primerMarcaAgregada = true;
                Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadPrimerMarcaAgua, "True");
            }

            return primerMarcaAgregada;
        }

        /// <summary>
        /// Agrega/Edita el marcador de "Anexos", dependiendo de los valores ingresados en el documento.
        /// </summary>
        public static void editarMarcadorAnexos(System.Windows.Forms.CheckBox chbAFisico, System.Windows.Forms.CheckBox chbAElectronico)
        {
            if (chbAElectronico.Checked || chbAFisico.Checked)
            {
                Globals.ThisAddIn.ajustarTexto(String.Concat(Constantes.tagTituloAnexos), String.Concat("Anexos:", "*"), 12, WdColor.wdColorBlack);
                Globals.ThisAddIn.ajustarTexto(String.Concat(Constantes.tagAnexos), String.Concat("Incluso lo anunciado.", "*"), 12, WdColor.wdColorBlack);
            }

            if (!chbAElectronico.Checked && !chbAFisico.Checked)
            {
                Globals.ThisAddIn.ajustarTexto(String.Concat(Constantes.tagTituloAnexos), String.Concat(" ", "*"), 1, WdColor.wdColorWhite);
                Globals.ThisAddIn.ajustarTexto(String.Concat(Constantes.tagAnexos), String.Concat(" ", "*"), 1, WdColor.wdColorWhite);
            }
        }

        /// <summary>
        /// Carga la lista de destinos
        /// </summary>
        public static void loadUsuarios()
        {
            Globals.ThisAddIn.listaDestinosMe.Clear();

            for (int iTotalDestinos = 0; iTotalDestinos < Constantes.maxDestinatariosMemorando; iTotalDestinos++)
            {
                string sNumDestino = Globals.ThisAddIn.consultarValorPropiedad(iTotalDestinos.ToString("00") + Constantes.propiedadPcrDestino);
                if (sNumDestino.Equals(iTotalDestinos.ToString("00") + Constantes.propiedadPcrDestino))
                {
                    break;
                }
                else
                {
                    DestinoMe usuario = new DestinoMe();
                    usuario.nombre = Globals.ThisAddIn.consultarPlaceholderEtiqueta(iTotalDestinos.ToString("00") + Constantes.tagNombreDestino);
                    usuario.cargo = Globals.ThisAddIn.consultarPlaceholderEtiqueta(iTotalDestinos.ToString("00") + Constantes.tagCargoDestino).ToString();
                    usuario.dependencia = Globals.ThisAddIn.consultarPlaceholderEtiqueta(iTotalDestinos.ToString("00") + Constantes.tagDependenciaDestino);

                    Globals.ThisAddIn.listaDestinosMe.Add(usuario);
                }
            }

        }


        /// <summary>
        /// Carga la lista de copias
        /// </summary>
        public static void loadCopias()
        {
            Globals.ThisAddIn.listaCopiasMe.Clear();
            for (int iTotalCopias = 0; iTotalCopias < Constantes.maxCopiasMemorando; iTotalCopias++)
            {
                string sNumCopias = Globals.ThisAddIn.consultarValorPropiedad(iTotalCopias.ToString("00") + Constantes.propiedadPcrCopias);
                if (sNumCopias.Equals(iTotalCopias.ToString("00") + Constantes.propiedadPcrCopias))
                {
                    break;
                }
                else
                {
                    DestinoMe usuario = new DestinoMe();
                    String[] copias = Globals.ThisAddIn.consultarPlaceholderEtiqueta(iTotalCopias.ToString("00") + Constantes.tagCopias).Split(',');

                    usuario.nombre = copias[0];
                    usuario.cargo = copias[1].TrimStart(' ');
                    usuario.dependencia = copias[2].TrimStart(' ');

                    Globals.ThisAddIn.listaCopiasMe.Add(usuario);
                }
            }
        }


    }
}
