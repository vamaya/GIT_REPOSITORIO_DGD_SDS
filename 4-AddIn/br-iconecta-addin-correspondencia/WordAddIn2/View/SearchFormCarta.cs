using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using System.IO;
using System.Collections;
using Word = Microsoft.Office.Interop.Word;
using Microsoft.Office.Interop.Word;
using System.Text.RegularExpressions;

namespace CorrWordAddIn
{
    public partial class SearchFormCarta : Form
    {
        public bool primerDestAgregado = false;
        public bool primerCopiaAgregado = false;
        public bool primerRemitenteAgregado = false;
        DataView vistaUsuarios;

        public SearchFormCarta()
        {
            InitializeComponent();
            cargarGrilla();
        }

        /// <summary>
        /// Carga la tabla con la información del arreglo arrUsuarios
        /// </summary>
        public void cargarGrilla()
        {
            // Create one DataTable with one column.
            System.Data.DataTable tblUsuarios = new System.Data.DataTable("table");
            DataColumn colIdFirmanteBuscar = new DataColumn("IdFirmanteBuscar", Type.GetType("System.String"));
            DataColumn colNombre = new DataColumn("Nombre", Type.GetType("System.String"));
            DataColumn colCargo = new DataColumn("Cargo", Type.GetType("System.String"));
            DataColumn colCargoIngles = new DataColumn("CargoIngles", Type.GetType("System.String"));
            DataColumn colFirmanteBuscar = new DataColumn("FirmanteBuscar", Type.GetType("System.String"));
            DataColumn colFirmanteCartaBuscar = new DataColumn("FirmanteCartaBuscar", Type.GetType("System.String"));
            DataColumn colSiglaFirmanteBuscar = new DataColumn("SiglaFirmanteBuscar", Type.GetType("System.String"));
            DataColumn colDpto = new DataColumn("Departamento", Type.GetType("System.String"));
            DataColumn colDptoIngles = new DataColumn("DepartamentoIngles", Type.GetType("System.String"));
            DataColumn colPCRBuscar = new DataColumn("PCRBuscar", Type.GetType("System.String"));
            DataColumn colIdPcrBuscar = new DataColumn("IdPcrBuscar", Type.GetType("System.String"));
            DataColumn colPCRConfidencialBuscar = new DataColumn("PCRConfidencialBuscar", Type.GetType("System.String"));
            DataColumn colIdPcrConfidencialBuscar = new DataColumn("IdPcrConfidencialBuscar", Type.GetType("System.String"));
            DataColumn colFondoIndependiente = new DataColumn("FondoIndependiente", Type.GetType("System.String"));
            DataColumn colCddBuscar = new DataColumn("CddBuscar", Type.GetType("System.String"));
            DataColumn colIdCddBuscar = new DataColumn("IdCddBuscar", Type.GetType("System.String"));
            DataColumn colCiudadDepartamento = new DataColumn("CiudadDepartamento", Type.GetType("System.String"));
            DataColumn colDireccionDepartamento = new DataColumn("DireccionDepartamento", Type.GetType("System.String"));
            DataColumn colTelefonoDepartamento = new DataColumn("TelefonoDepartamento", Type.GetType("System.String"));
            DataColumn colNombreBuscar = new DataColumn("NombreBuscar", Type.GetType("System.String"));
            DataColumn colCargoBuscar = new DataColumn("CargoBuscar", Type.GetType("System.String"));
            DataColumn colDptoBuscar = new DataColumn("DepartamentoBuscar", Type.GetType("System.String"));

            tblUsuarios.Columns.Add(colIdFirmanteBuscar);
            tblUsuarios.Columns.Add(colNombre);
            tblUsuarios.Columns.Add(colCargo);
            tblUsuarios.Columns.Add(colCargoIngles);
            tblUsuarios.Columns.Add(colFirmanteBuscar);
            tblUsuarios.Columns.Add(colFirmanteCartaBuscar);
            tblUsuarios.Columns.Add(colSiglaFirmanteBuscar);
            tblUsuarios.Columns.Add(colDpto);
            tblUsuarios.Columns.Add(colDptoIngles);
            tblUsuarios.Columns.Add(colPCRBuscar);
            tblUsuarios.Columns.Add(colIdPcrBuscar);
            tblUsuarios.Columns.Add(colPCRConfidencialBuscar);
            tblUsuarios.Columns.Add(colIdPcrConfidencialBuscar);
            tblUsuarios.Columns.Add(colFondoIndependiente);
            tblUsuarios.Columns.Add(colCddBuscar);
            tblUsuarios.Columns.Add(colIdCddBuscar);
            tblUsuarios.Columns.Add(colCiudadDepartamento);
            tblUsuarios.Columns.Add(colDireccionDepartamento);
            tblUsuarios.Columns.Add(colTelefonoDepartamento);
            tblUsuarios.Columns.Add(colNombreBuscar);
            tblUsuarios.Columns.Add(colCargoBuscar);
            tblUsuarios.Columns.Add(colDptoBuscar);

            // Add five items.
            DataRow NewRow;
            int i;
            for (i = 0; i < Globals.ThisAddIn.listaEmpleados.Count; i++)
            {
                NewRow = tblUsuarios.NewRow();
                NewRow["Nombre"] = "Item " + i;
                tblUsuarios.Rows.Add(NewRow);
            }
            // Change the values in the table.
            tblUsuarios.AcceptChanges();

            ServicesCarta.insertDataTable(tblUsuarios, Globals.ThisAddIn.listaEmpleados);

            // Create two DataView objects with the same table.
            vistaUsuarios = new DataView(tblUsuarios);

            dgvUsuarios.DataSource = vistaUsuarios;
            dgvUsuarios.Columns["Nombre"].Width = 215;
            dgvUsuarios.Columns["Cargo"].Width = 214;
            dgvUsuarios.Columns["Departamento"].Width = 230;
            dgvUsuarios.Columns["NombreBuscar"].Visible = false;
            dgvUsuarios.Columns["CargoBuscar"].Visible = false;
            dgvUsuarios.Columns["CargoIngles"].Visible = false;
            dgvUsuarios.Columns["IdFirmanteBuscar"].Visible = false;
            dgvUsuarios.Columns["SiglaFirmanteBuscar"].Visible = false;
            dgvUsuarios.Columns["FirmanteBuscar"].Visible = false;
            dgvUsuarios.Columns["FirmanteCartaBuscar"].Visible = false;
            dgvUsuarios.Columns["DepartamentoBuscar"].Visible = false;
            dgvUsuarios.Columns["DepartamentoIngles"].Visible = false;
            dgvUsuarios.Columns["PCRBuscar"].Visible = false;
            dgvUsuarios.Columns["IdPcrBuscar"].Visible = false;
            dgvUsuarios.Columns["PCRConfidencialBuscar"].Visible = false;
            dgvUsuarios.Columns["IdPcrConfidencialBuscar"].Visible = false;
            dgvUsuarios.Columns["FondoIndependiente"].Visible = false;
            dgvUsuarios.Columns["CddBuscar"].Visible = false;
            dgvUsuarios.Columns["IdCddBuscar"].Visible = false;
            dgvUsuarios.Columns["CiudadDepartamento"].Visible = false;
            dgvUsuarios.Columns["DireccionDepartamento"].Visible = false;
            dgvUsuarios.Columns["TelefonoDepartamento"].Visible = false;
        }

        /// <summary>
        /// Validaciones que se realizan al cargar el formulario
        /// </summary>
        private void SearchFormCarta_Load(object sender, EventArgs e)
        {
            try
            {
                ServicesCarta.llenarComboPaises(cbPais, Globals.ThisAddIn.listViewPaises);
                ServicesCarta.llenarComboCiudades(cbCiudad, Globals.ThisAddIn.listViewCiudades);     

                Globals.ThisAddIn.Application.ScreenUpdating = false;
                if (!Globals.ThisAddIn.consultarValorPropiedad("00" + Constantes.propiedadPcrDestino).Equals("00" + Constantes.propiedadPcrDestino))
                {
                    primerDestAgregado = true;
                    btnActualizarPara.Enabled = true;
                    btnEliminarPara.Enabled = true;
                    btnVerificarDatos.Enabled = true;                    

                    ServicesCarta.loadDestinos();

                    if (Globals.ThisAddIn.listaDestinosCa.Count == Constantes.maxDestinatariosCarta)
                    {
                        btInsertarPara.Enabled = false;
                    }
                }
                else
                {
                    Globals.ThisAddIn.listaDestinosCa.Clear();
                }

                if (!Globals.ThisAddIn.consultarValorPropiedad("00" + Constantes.propiedadPcrCopias).Equals("00" + Constantes.propiedadPcrCopias))
                {
                    primerCopiaAgregado = true;
                    btnActualizarCopia.Enabled = true;
                    btnEliminarCopia.Enabled = true;

                    ServicesCarta.loadCopias();

                    if (Globals.ThisAddIn.listaCopiasCa.Count == Constantes.maxCopiasCarta)
                    {
                        btnInsertarCopia.Enabled = false;
                    }
                }
                else
                {
                    chbCopias.Checked = false;
                    Globals.ThisAddIn.listaCopiasCa.Clear();
                }

                if (!Globals.ThisAddIn.consultarValorPropiedad("00" + Constantes.propiedadIdFirmante).Equals("00" + Constantes.propiedadIdFirmante))
                {
                    primerRemitenteAgregado = true;
                    ServicesCarta.loadFirmantes();
                    btnInsertarRemitente.Enabled = false;
                }
                else
                {
                    btnInsertarRemitente.Enabled = true;
                    Globals.ThisAddIn.listaFirmantes.Clear();
                }

                if (Globals.ThisAddIn.consultarValorPropiedad(Constantes.propiedadSegundoRemitente).Equals("True"))
                {
                    chbLogo.Enabled = false;
                }
                else
                {
                    if (Globals.ThisAddIn.consultarValorPropiedad(Constantes.propiedadLogo).Equals("False"))
                    {
                        chbLogo.Checked = false;
                    }
                }               

                if (!Globals.ThisAddIn.consultarValorPropiedad("01" + Constantes.propiedadIdFirmante).Equals("01" + Constantes.propiedadIdFirmante))
                {
                    btnEliminarRemitente.Enabled = true;
                }

                if (Globals.ThisAddIn.consultarValorPropiedad(Constantes.propiedadPrimerMarcaAgua).Equals("True"))
                {
                    Globals.ThisAddIn.iprimerMarcaAgregada = true;
                }
                else
                {
                    Globals.ThisAddIn.iprimerMarcaAgregada = false;
                }

                if (Globals.ThisAddIn.consultarValorPropiedad(Constantes.propiedadTipo).Equals("Confidencial"))
                {
                    rbtConfidencial.Checked = true;
                }

                if (Globals.ThisAddIn.consultarValorPropiedad(Constantes.propiedadImpresionArea).Equals("True"))
                {
                    chbImpresionArea.Checked = true;
                }

                if (Globals.ThisAddIn.consultarValorPropiedad(Constantes.propiedadAnexosFisicos).Equals("False"))
                {
                    chbAFisico.Checked = false;
                }

                if (Globals.ThisAddIn.consultarValorPropiedad(Constantes.propiedadAnexosElectronicos).Equals("True"))
                {
                    chbAElectronico.Checked = true;
                }

                if (Globals.ThisAddIn.consultarValorPropiedad(Constantes.propiedadIdiomaIngles).Equals("True"))
                {
                    chbIngles.Checked = true;
                }

                if (Globals.ThisAddIn.consultarValorPropiedad(Constantes.propiedadPersonalizada).Equals("True"))
                {
                    chbPersonalizado.Checked = true;
                    Globals.ThisAddIn.leerDestinosPers();
                    btnEliminarPara.Enabled = true;
                    btnVerificarDatos.Enabled = true;

                    if (Globals.ThisAddIn.listaDestinosCa.Count == Constantes.maxDestinatariosCarta)
                    {
                        btInsertarPara.Enabled = false;
                    }

                }
                else
                {
                    Globals.ThisAddIn.oTablePers = null;
                }

                if (Globals.ThisAddIn.consultarValorPropiedad(Constantes.propiedadMedioEnvio).Equals("Directo"))
                {
                    rbtDirecto.Checked = true;
                }

                if (Globals.ThisAddIn.consultarValorPropiedad(Constantes.propiedadCorreoCertificado).Equals("True"))
                {
                    rbtCorreoCertificado.Checked = true;
                }

                if (!Globals.ThisAddIn.consultarValorPropiedad(Constantes.propiedadCorreoCopia).Equals(Constantes.propiedadCorreoCopia))
                {
                    txtCopiaCorreo.Text = Globals.ThisAddIn.consultarValorPropiedad(Constantes.propiedadCorreoCopia);
                }

                Globals.ThisAddIn.Application.ScreenUpdating = true;
            }
            catch (Exception error)
            {
                Globals.ThisAddIn.Application.ScreenUpdating = true;
                Globals.ThisAddIn.escribirLog(error.ToString());
                MessageBox.Show("Error al tratar de recuperar documento antiguo, manipule el complemento desde un documento nuevo.");
                this.Close();
            }

            /*
             * Vamaya: Se añade este try para encontrar si la plantilla no tiene la propiedad añadida
             */
            try
            {
                /*
                 * Vamaya: Se añade esta validación para verificar el estado del chb Referencia
                 */
                if (Globals.ThisAddIn.consultarValorPropiedad(Constantes.propiedadRef).Equals("False"))
                {
                    chbReferencias.Checked = false;
                }
                /*
                 * Fin lineas de código insertadas
                 */
            }
            catch (Exception error)
            {
                Globals.ThisAddIn.Application.ScreenUpdating = true;
                Globals.ThisAddIn.escribirLog(error.ToString());
                MessageBox.Show("Error, está usando una versión de plantilla desactualizada, por favor descargue la versión más reciente desde iConecta");
                this.Close();
            }
            /*
             * Fin lineas de código insertadas
             */
        }

        /// <summary>
        /// Limpia y restablece los valores por defecto de los elementos del formulario
        /// </summary>
        void LimpiarFormulario()
        {
            txtNombre.Text = "";
            txtNombreexterno.Text = "";
            txtCargoExterno.Text = "";
            txtEmpresa.Text = "";
            TxtDireccion.Text = "";
            cbPais.SelectedValue = "Colombia";
            //JLG: Se cambia la ciudad por defecto luego de insertar destinatario
            if (rbtFisica.Checked)
            {
                cbCiudad.SelectedValue = "Bogotá D.C. ";
            }
            else
            {
                cbCiudad.SelectedValue = "";
            }
            
            dgvUsuarios.ClearSelection();
            //Vamaya: se comenta linea que escogia dirección fisica de nuevo
            //rbtFisica.Checked = true;
        }

        /// <summary>
        /// Metodo para insertar destinos - Evento activado por el Boton "Insertar Destinatario" 
        /// </summary>
        private void btInsertarPara_Click(object sender, EventArgs e)
        {

            /*
             * Vamaya: Se añade esta validación para verificar el estado del los destino de carta sin opción de correspondencia combinada
             */
            if (chbPersonalizado.Checked == false && Globals.ThisAddIn.validarDestinosCarta() == false)
            {
                Globals.ThisAddIn.Application.ScreenUpdating = true;
                MessageBox.Show("Error: Problemas de caché en el Add-In, la lista de destinatarios está corrupta. Abra el Add-In nuevamente.");
                this.Close();
                //Se sale de la función para evitar validaciones adicionales depués de este paso
                return;
            }
            else if(chbPersonalizado.Checked && Globals.ThisAddIn.validarDestinosCartaComb()) //Para validar opción de problemas de caché en correspopndencia comb
            {
                Globals.ThisAddIn.Application.ScreenUpdating = true;
                MessageBox.Show("Error: Problemas de caché en el Add-In, la lista de destinatarios está corrupta. Abra el Add-In nuevamente.");
                this.Close();
                //Se sale de la función para evitar validaciones adicionales depués de este paso
                return;
            }
            /*
             * Fin lineas de código insertadas
             */

            if (ServicesCarta.validarCampos(TxtDireccion.Text, rbtFisica.Checked, rbtCorreo.Checked, cbCiudad.Text))
            {
                return;
            }

            Globals.ThisAddIn.Application.ScreenUpdating = false;            
            int iDestino = Globals.ThisAddIn.listaDestinosCa.Count;
            
            if (chbPersonalizado.Checked)
            {
                if (dgvUsuarios.SelectedRows.Count > 0)
                {
                    string sNewDepartamento = dgvUsuarios.CurrentRow.Cells["Departamento"].FormattedValue.ToString();
                    if (!dgvUsuarios.CurrentRow.Cells["CiudadDepartamento"].Value.ToString().Contains("Bogotá"))
                    {
                        string[] sDepartamento = dgvUsuarios.CurrentRow.Cells["Departamento"].FormattedValue.ToString().Split('-');
                        sNewDepartamento = sDepartamento[0];
                    }

                    ServicesCarta.insertDestinoPersonalizado(dgvUsuarios.CurrentRow.Cells["Nombre"].FormattedValue.ToString(), dgvUsuarios.CurrentRow.Cells["Cargo"].FormattedValue.ToString(), sNewDepartamento, TxtDireccion.Text, rbtFisica.Checked, cbCiudad.Text, cbPais.Text);

                    btnEliminarPara.Enabled = true;
                }
                else
                {
                    if (string.IsNullOrEmpty(txtNombreexterno.Text) && string.IsNullOrEmpty(txtEmpresa.Text))
                    {
                        MessageBox.Show("Favor diligenciar el campo Nombre o Entidad.");
                        return;
                    }

                    ServicesCarta.insertDestinoPersonalizado(txtNombreexterno.Text, txtCargoExterno.Text, txtEmpresa.Text, TxtDireccion.Text, rbtFisica.Checked, cbCiudad.Text, cbPais.Text);

                    btnEliminarPara.Enabled = true;
                }
                btnVerificarDatos.Enabled = true;
                btnActualizarPara.Enabled = false;
                LimpiarFormulario();
            }
            else
            {
                if (dgvUsuarios.SelectedRows.Count > 0)
                {
                    if (primerDestAgregado)
                    {
                        Globals.ThisAddIn.insertarDestinoCarta(false);
                    }
                    else
                    {
                        primerDestAgregado = true;
                        btnActualizarPara.Enabled = true;
                        btnEliminarPara.Enabled = true;
                        btnVerificarDatos.Enabled = true;
                    }

                    string sNewDepartamento = dgvUsuarios.CurrentRow.Cells["Departamento"].FormattedValue.ToString();

                    if (!dgvUsuarios.CurrentRow.Cells["CiudadDepartamento"].Value.ToString().Contains("Bogotá"))
                    {
                        string[] sDepartamento = dgvUsuarios.CurrentRow.Cells["Departamento"].FormattedValue.ToString().Split('-');
                        sNewDepartamento = sDepartamento[0];
                    }

                    ServicesCarta.insertDestino(dgvUsuarios.CurrentRow.Cells["Nombre"].FormattedValue.ToString(), dgvUsuarios.CurrentRow.Cells["Cargo"].FormattedValue.ToString(), sNewDepartamento, TxtDireccion.Text, rbtFisica, cbCiudad, cbPais);

                    LimpiarFormulario();

                }
                else
                {
                    if (string.IsNullOrEmpty(txtNombreexterno.Text) && string.IsNullOrEmpty(txtEmpresa.Text))
                    {
                        MessageBox.Show("Favor diligenciar el campo Nombre o Entidad.");
                        return;
                    }

                    if (primerDestAgregado)
                    {
                        ServicesCarta.InsertarDatosCarta(false, txtNombreexterno.Text, txtCargoExterno.Text, txtEmpresa.Text);
                        chbPersonalizado.Enabled = true;
                    }
                    else
                    {
                        Globals.ThisAddIn.eliminarPrimerDestino();
                        ServicesCarta.InsertarDatosCarta(true, txtNombreexterno.Text, txtCargoExterno.Text, txtEmpresa.Text);
                        primerDestAgregado = true;
                        btnActualizarPara.Enabled = true;
                        btnEliminarPara.Enabled = true;
                        btnVerificarDatos.Enabled = true;
                    }

                    ServicesCarta.insertDestino(txtNombreexterno.Text, txtCargoExterno.Text, txtEmpresa.Text, TxtDireccion.Text, rbtFisica, cbCiudad, cbPais);

                    LimpiarFormulario();

                }
            }

            if (Globals.ThisAddIn.listaDestinosCa.Count == Constantes.maxDestinatariosCarta)
            {
                btInsertarPara.Enabled = false;
            }
            else
            {
                btInsertarPara.Enabled = true;
            }

            Globals.ThisAddIn.Application.ScreenUpdating = true;
        }


        /// <summary>
        /// Metodo para actualizar destinos - Evento activado por el Boton "Actualizar Destinatario" 
        /// </summary>
        private void btnActualizarPara_Click(object sender, EventArgs e)
        {

            /*
             * Vamaya: Se añade esta validación para verificar el estado del los destino de carta sin opción de correspondencia combinada
             */
            if (chbPersonalizado.Checked == false && Globals.ThisAddIn.validarDestinosCarta() == false)
            {
                Globals.ThisAddIn.Application.ScreenUpdating = true;
                MessageBox.Show("Error: Problemas de caché en el Add-In, la lista de destinatarios está corrupta. Abra el Add-In nuevamente.");
                this.Close();
                //Se sale de la función para evitar validaciones adicionales depués de este paso
                return;
            }
            else if (chbPersonalizado.Checked && Globals.ThisAddIn.validarDestinosCartaComb()) //Para validar opción de problemas de caché en correspopndencia comb
            {
                Globals.ThisAddIn.Application.ScreenUpdating = true;
                MessageBox.Show("Error: Problemas de caché en el Add-In, la lista de destinatarios está corrupta. Abra el Add-In nuevamente.");
                this.Close();
                //Se sale de la función para evitar validaciones adicionales depués de este paso
                return;
            }
            /*
             * Fin lineas de código insertadas
             */

            if (ServicesCarta.validarCampos(TxtDireccion.Text, rbtFisica.Checked, rbtCorreo.Checked, cbCiudad.Text))
            {
                return;
            }

            Globals.ThisAddIn.Application.ScreenUpdating = false;
            string sNewDireccion = ServicesCarta.estandarizarNomenclaturaDireccion(TxtDireccion.Text);
            int iDestino = Globals.ThisAddIn.listaDestinosCa.Count;

            if (dgvUsuarios.SelectedRows.Count > 0)
            {
                if (iDestino == 1)
                {
                    Globals.ThisAddIn.eliminarPrimerDestino();
                    Globals.ThisAddIn.insertarDestinoCarta(true);
                }
                else
                {
                    Globals.ThisAddIn.eliminarDestino(iDestino);
                    Globals.ThisAddIn.insertarDestinoCarta(false);

                }

                string sNewDepartamento = dgvUsuarios.CurrentRow.Cells["Departamento"].FormattedValue.ToString();
                if (!dgvUsuarios.CurrentRow.Cells["CiudadDepartamento"].Value.ToString().Contains("Bogotá"))
                {
                    string[] sDepartamento = dgvUsuarios.CurrentRow.Cells["Departamento"].FormattedValue.ToString().Split('-');
                    sNewDepartamento = sDepartamento[0];
                }

                ServicesCarta.insertDestino(dgvUsuarios.CurrentRow.Cells["Nombre"].FormattedValue.ToString(), dgvUsuarios.CurrentRow.Cells["Cargo"].FormattedValue.ToString(), sNewDepartamento, TxtDireccion.Text, rbtFisica, cbCiudad, cbPais);

                LimpiarFormulario();

            }
            else
            {
                if (string.IsNullOrEmpty(txtNombreexterno.Text) && string.IsNullOrEmpty(txtEmpresa.Text))
                {
                    MessageBox.Show("Favor diligenciar el campo Nombre o Entidad.");
                    return;
                }

                if (iDestino == 1)
                {
                    Globals.ThisAddIn.eliminarPrimerDestino();
                    ServicesCarta.InsertarDatosCarta(true, txtNombreexterno.Text, txtCargoExterno.Text, txtEmpresa.Text);
                }
                else
                {
                    Globals.ThisAddIn.eliminarDestino(iDestino);
                    ServicesCarta.InsertarDatosCarta(false, txtNombreexterno.Text, txtCargoExterno.Text, txtEmpresa.Text);
                }

                ServicesCarta.insertDestino(txtNombreexterno.Text, txtCargoExterno.Text, txtEmpresa.Text, TxtDireccion.Text, rbtFisica, cbCiudad, cbPais);

                LimpiarFormulario();
            }

            if (Globals.ThisAddIn.listaDestinosCa.Count == Constantes.maxDestinatariosCarta)
            {
                btInsertarPara.Enabled = false;
            }
            else
            {
                btInsertarPara.Enabled = true;
            }

            Globals.ThisAddIn.Application.ScreenUpdating = true;
        }


        /// <summary>
        /// Metodo para eliminar destinos - Evento activado por el Boton "Eliminar Destinatario" 
        /// </summary>
        private void btnEliminarPara_Click(object sender, EventArgs e)
        {

            /*
             * Vamaya: Se añade esta validación para verificar el estado del los destino de carta sin opción de correspondencia combinada
             */
            if (chbPersonalizado.Checked == false && Globals.ThisAddIn.validarDestinosCarta() == false)
            {
                Globals.ThisAddIn.Application.ScreenUpdating = true;
                MessageBox.Show("Error: Problemas de caché en el Add-In, la lista de destinatarios está corrupta. Abra el Add-In nuevamente.");
                this.Close();
                //Se sale de la función para evitar validaciones adicionales depués de este paso
                return;
            }
            else if (chbPersonalizado.Checked && Globals.ThisAddIn.validarDestinosCartaComb()) //Para validar opción de problemas de caché en correspopndencia comb
            {
                Globals.ThisAddIn.Application.ScreenUpdating = true;
                MessageBox.Show("Error: Problemas de caché en el Add-In, la lista de destinatarios está corrupta. Abra el Add-In nuevamente.");
                this.Close();
                //Se sale de la función para evitar validaciones adicionales depués de este paso
                return;
            }
            /*
             * Fin lineas de código insertadas
             */

            Globals.ThisAddIn.Application.ScreenUpdating = false;
            int tDestinos = Globals.ThisAddIn.listaDestinosCa.Count;
            if (chbPersonalizado.Checked)
            {
                if (dgvUsuarios.SelectedRows.Count > 0)
                {
                    ServicesCarta.eliminarDestinoPersonalizado(dgvUsuarios.CurrentRow.Cells["Nombre"].FormattedValue.ToString(), dgvUsuarios.CurrentRow.Cells["Cargo"].FormattedValue.ToString(), dgvUsuarios.CurrentRow.Cells["Departamento"].FormattedValue.ToString());                   
                }
                else
                {
                    ServicesCarta.eliminarDestinoPersonalizado(txtNombreexterno.Text, txtCargoExterno.Text, txtEmpresa.Text);                   
                }
            }
            else
            {
                Globals.ThisAddIn.eliminarDestino(tDestinos);
                btInsertarPara.Enabled = true;
            }

            if (!Globals.ThisAddIn.listaDestinosCa.Any())
            {
                btnEliminarPara.Enabled = false;
            }

            if(Globals.ThisAddIn.listaDestinosCa.Count == Constantes.maxDestinatariosCarta)
            {
                btInsertarPara.Enabled = false;
            }
            else
            {
                btInsertarPara.Enabled = true;
            }                         

            Globals.ThisAddIn.Application.ScreenUpdating = true;
        }


        /// <summary>
        /// Metodo para activar botones "Insertar Remitente" y "Actualizar Remitente" - Evento activado por la seleccion de algún usuario" 
        /// </summary>
        private void dgvUsuarios_SelectionChanged(object sender, EventArgs e)
        {
            string tipoFondo = Globals.ThisAddIn.consultarValorPropiedad(Constantes.propiedadTipoFondo);

            if (dgvUsuarios.SelectedRows.Count > 0)
            {

                if (tipoFondo != null)
                {
                    if (dgvUsuarios.CurrentRow.Cells["FirmanteCartaBuscar"].Value.ToString().Equals("1") && dgvUsuarios.CurrentRow.Cells["SiglaFirmanteBuscar"].Value.ToString().Equals(tipoFondo))
                    {
                        btnInsertarRemitente.Enabled = true;
                        btnActualizarRemitente.Enabled = true;
                    }
                    else
                    {
                        btnInsertarRemitente.Enabled = false;
                        btnActualizarRemitente.Enabled = false;
                    }
                }
                else if (dgvUsuarios.CurrentRow.Cells["FirmanteCartaBuscar"].Value.ToString().Equals("1"))
                {
                    btnInsertarRemitente.Enabled = true;
                    btnActualizarRemitente.Enabled = true;
                }
                else
                {
                    btnInsertarRemitente.Enabled = false;
                    btnActualizarRemitente.Enabled = false;
                }

                if (Globals.ThisAddIn.listaFirmantes.Count == Globals.ThisAddIn.iMaxRemitentes)
                {
                    btnInsertarRemitente.Enabled = false;
                }

                if (primerRemitenteAgregado == false)
                {
                    btnEliminarRemitente.Enabled = false;
                    btnActualizarRemitente.Enabled = false;
                }
                string SegundoRemitente = Globals.ThisAddIn.consultarValorPropiedad(Constantes.propiedadSegundoRemitente);
                if (SegundoRemitente.Equals("True"))
                {
                    btnInsertarRemitente.Enabled = false;
                }
            }

        }


        /// <summary>
        /// Metodo para insertar Remitente - Evento activado por el botón "Insertar Remitente" 
        /// </summary>
        private void btnInsRemitente_Click(object sender, EventArgs e)
        {
            Globals.ThisAddIn.Application.ScreenUpdating = false;

            if (dgvUsuarios.SelectedRows.Count > 0)
            {
                if (primerRemitenteAgregado)
                {
                    ServicesCarta.manipularMarcaAgua(Globals.ThisAddIn.iprimerMarcaAgregada, rbtNormal.Checked, rbtConfidencial.Checked, chbIngles.Checked);
                    Globals.ThisAddIn.insertarRemitente();
                    btnEliminarRemitente.Enabled = true;
                    Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadSegundoRemitente, "True");
                    chbLogo.Enabled = false;
                }

                bool isInsert = ServicesCarta.insertarRemitente(dgvUsuarios, chbIngles.Checked,true);

                if (!isInsert)
                {
                    if (chbIngles.Checked)
                        chbIngles.Checked = false;
                    Globals.ThisAddIn.Application.ScreenUpdating = true;
                    return;
                }                    

                primerRemitenteAgregado = true;

                if (Globals.ThisAddIn.listaFirmantes.Count-1 == 0)
                {
                    ServicesCarta.cambiarPropRemitente(dgvUsuarios, rbtNormal.Checked);
                }
                
                if(Globals.ThisAddIn.iMaxRemitentes == Globals.ThisAddIn.listaFirmantes.Count)
                    btnInsertarRemitente.Enabled = false;
                
                LimpiarFormulario();
            }
            else
            {
                MessageBox.Show("Por favor seleccione una fila.");
            }
            Globals.ThisAddIn.Application.ScreenUpdating = true;
        }


        /// <summary>
        /// Metodo para actualizar Remitente - Evento activado por el botón "Actualizar Remitente" 
        /// </summary>
        private void btnActualizarRemitente_Click(object sender, EventArgs e)
        {
            Globals.ThisAddIn.Application.ScreenUpdating = false;

            if (dgvUsuarios.SelectedRows.Count > 0)
            {                
                
                bool isInsert = ServicesCarta.insertarRemitente(dgvUsuarios, chbIngles.Checked, false);

                if (!isInsert)
                {
                    if (chbIngles.Checked)
                        chbIngles.Checked = false;
                    Globals.ThisAddIn.Application.ScreenUpdating = true;
                    return;
                }                   
                
                if (Globals.ThisAddIn.listaFirmantes.Count-1 == 0)
                {
                    ServicesCarta.cambiarPropRemitente(dgvUsuarios, rbtNormal.Checked);
                }

                LimpiarFormulario();
            }
            else
            {
                MessageBox.Show("Por favor seleccione una fila.");
            }
            Globals.ThisAddIn.Application.ScreenUpdating = true;
        }


        /// <summary>
        /// Metodo para eliminar Remitente - Evento activado por el botón "Eliminar Remitente" 
        /// </summary>
        private void btnEliminarRemitente_Click(object sender, EventArgs e)
        {
            Globals.ThisAddIn.EliminarRemitente();
            btnEliminarRemitente.Enabled = false;
        }


        /// <summary>
        /// Metodo para insertar destino Copia - Evento activado por el botón "Insertar Copia" 
        /// </summary>
        private void btnInsertarCopia_Click(object sender, EventArgs e)
        {
            if (ServicesCarta.validarCampos(TxtDireccion.Text, rbtFisica.Checked, rbtCorreo.Checked, cbCiudad.Text))
            {
                return;
            }

            Globals.ThisAddIn.Application.ScreenUpdating = false;
            int iCopia = Globals.ThisAddIn.listaCopiasCa.Count;

            if (dgvUsuarios.SelectedRows.Count > 0)
            {
                if (primerCopiaAgregado)
                {
                    Globals.ThisAddIn.insertarCopia(iCopia);
                }
                else
                {
                    primerCopiaAgregado = true;
                    btnActualizarCopia.Enabled = true;
                    btnEliminarCopia.Enabled = true;
                }

                string sNewDepartamento = dgvUsuarios.CurrentRow.Cells["Departamento"].FormattedValue.ToString();

                if (!dgvUsuarios.CurrentRow.Cells["CiudadDepartamento"].Value.ToString().Contains("Bogotá"))
                {
                    string[] sDepartamento = dgvUsuarios.CurrentRow.Cells["Departamento"].FormattedValue.ToString().Split('-');
                    sNewDepartamento = sDepartamento[0];
                }

                ServicesCarta.insertarCopia(dgvUsuarios.CurrentRow.Cells["Nombre"].FormattedValue.ToString(), dgvUsuarios.CurrentRow.Cells["Cargo"].FormattedValue.ToString(), sNewDepartamento, TxtDireccion.Text, rbtFisica.Checked, cbCiudad.SelectedValue.ToString(), cbPais.SelectedValue.ToString());

                LimpiarFormulario();
            }
            else
            {
                if (string.IsNullOrEmpty(txtNombreexterno.Text) && string.IsNullOrEmpty(txtEmpresa.Text))
                {
                    MessageBox.Show("Favor diligenciar el campo Nombre o Entidad.");
                    return;
                }

                if (txtNombreexterno.Text.Contains(","))
                {
                    MessageBox.Show("El campo Nombre no puede llevar comas.");
                    return;
                }
                if (txtCargoExterno.Text.Contains(","))
                {
                    MessageBox.Show("El campo Cargo no puede llevar comas.");
                    return;
                }
                if (txtEmpresa.Text.Contains(","))
                {
                    MessageBox.Show("El campo Entidad no puede llevar comas.");
                    return;
                }
                if (primerCopiaAgregado)
                {
                    Globals.ThisAddIn.insertarCopia(iCopia);
                }
                else
                {
                    primerCopiaAgregado = true;
                    btnActualizarCopia.Enabled = true;
                    btnEliminarCopia.Enabled = true;
                }

                ServicesCarta.insertarCopia(txtNombreexterno.Text, txtCargoExterno.Text, txtEmpresa.Text, TxtDireccion.Text, rbtFisica.Checked,cbCiudad.SelectedValue.ToString(), cbPais.SelectedValue.ToString());

                LimpiarFormulario();

            }

            if (Globals.ThisAddIn.listaCopiasCa.Count == Constantes.maxCopiasCarta)
            {
                btnInsertarCopia.Enabled = false;
            }
            else
            {
                btnInsertarCopia.Enabled = true;
            }

            Globals.ThisAddIn.Application.ScreenUpdating = true;
        }


        /// <summary>
        /// Metodo para actualizar destinos Copia - Evento activado por el botón "Actualizar Copia" 
        /// </summary>
        private void btnActualizarCopia_Click(object sender, EventArgs e)
        {
            if (ServicesCarta.validarCampos(TxtDireccion.Text, rbtFisica.Checked, rbtCorreo.Checked, cbCiudad.Text))
            {
                return;
            }

            Globals.ThisAddIn.Application.ScreenUpdating = false;            

            if (dgvUsuarios.SelectedRows.Count > 0)
            {                               
                string sNewDepartamento = dgvUsuarios.CurrentRow.Cells["Departamento"].FormattedValue.ToString();
                if (!dgvUsuarios.CurrentRow.Cells["CiudadDepartamento"].Value.ToString().Contains("Bogotá"))
                {
                    string[] sDepartamento = dgvUsuarios.CurrentRow.Cells["Departamento"].FormattedValue.ToString().Split('-');
                    sNewDepartamento = sDepartamento[0];
                }

                Globals.ThisAddIn.listaCopiasCa.RemoveAt(Globals.ThisAddIn.listaCopiasCa.Count - 1);

                ServicesCarta.insertarCopia(dgvUsuarios.CurrentRow.Cells["Nombre"].FormattedValue.ToString(), dgvUsuarios.CurrentRow.Cells["Cargo"].FormattedValue.ToString(), sNewDepartamento, TxtDireccion.Text, rbtFisica.Checked, cbCiudad.SelectedValue.ToString(), cbPais.SelectedValue.ToString());    
                
                LimpiarFormulario();
            }
            else
            {
                if (string.IsNullOrEmpty(txtNombreexterno.Text) && string.IsNullOrEmpty(txtEmpresa.Text))
                {
                    MessageBox.Show("Favor diligenciar el campo Nombre o Entidad.");
                    return;
                }

                if (txtNombreexterno.Text.Contains(","))
                {
                    MessageBox.Show("El campo Nombre no puede llevar comas.");
                    return;
                }
                if (txtCargoExterno.Text.Contains(","))
                {
                    MessageBox.Show("El campo Cargo no puede llevar comas.");
                    return;
                }
                if (txtEmpresa.Text.Contains(","))
                {
                    MessageBox.Show("El campo Entidad no puede llevar comas.");
                    return;
                }

                Globals.ThisAddIn.listaCopiasCa.RemoveAt(Globals.ThisAddIn.listaCopiasCa.Count - 1);

                ServicesCarta.insertarCopia(txtNombreexterno.Text, txtCargoExterno.Text, txtEmpresa.Text, TxtDireccion.Text, rbtFisica.Checked, cbCiudad.SelectedValue.ToString(), cbPais.SelectedValue.ToString());
                
                LimpiarFormulario();
            }

            if (Globals.ThisAddIn.listaCopiasCa.Count == Constantes.maxCopiasCarta)
            {
                btnInsertarCopia.Enabled = false;
            }
            else
            {
                btnInsertarCopia.Enabled = true;
            }

            Globals.ThisAddIn.Application.ScreenUpdating = true;
        }


        /// <summary>
        /// Metodo para eliminar destinos Copia - Evento activado por el botón "Eliminar Copia" 
        /// </summary>
        private void btnEliminarCopia_Click(object sender, EventArgs e)
        {

            btnInsertarCopia.Enabled = true;
            if (!Globals.ThisAddIn.eliminarCopia(Globals.ThisAddIn.listaCopiasCa.Count))
            {
                Globals.ThisAddIn.insertarTexto("00" + Constantes.tagCopias, "Nombre, Cargo, Dependencia (Copia). *" + DateTime.Now.Second.ToString(), false);
                btnActualizarCopia.Enabled = false;
                btnEliminarCopia.Enabled = false;
                primerCopiaAgregado = false;
            }

            if (Globals.ThisAddIn.listaCopiasCa.Count == Constantes.maxCopiasCarta)
            {
                btnInsertarCopia.Enabled = false;
            }
            else
            {
                btnInsertarCopia.Enabled = true;
            }

        }


        /// <summary>
        /// Metodo para activar/desactivar destinos Copia - Evento activado por el check "Copias" 
        /// </summary>
        private void chbCopias_CheckStateChanged(object sender, EventArgs e)
        {

            Globals.ThisAddIn.Application.ScreenUpdating = false;
            if (chbCopias.Checked)
            {
                if (!chbIngles.Checked)
                {
                    Globals.ThisAddIn.ajustarTexto(Constantes.tagTituloCopias, "Copias: *" + DateTime.Now.Second.ToString(), 12, WdColor.wdColorBlack);
                }
                else
                {
                    Globals.ThisAddIn.ajustarTexto(Constantes.tagTituloCopias, "Copies: *" + DateTime.Now.Second.ToString(), 12, WdColor.wdColorBlack);
                }
                Globals.ThisAddIn.ajustarTexto("00" + Constantes.tagCopias, "Nombre, Cargo, Entidad (Copia). *" + DateTime.Now.Second.ToString(), 12, WdColor.wdColorBlack);
                btnInsertarCopia.Enabled = true;
            }
            else
            {
                int iCopiasActuales = Globals.ThisAddIn.listaCopiasCa.Count;

                for (; iCopiasActuales > 0; iCopiasActuales--)
                {
                    Globals.ThisAddIn.eliminarCopia(iCopiasActuales);
                }

                Globals.ThisAddIn.ajustarTexto("00" + Constantes.tagCopias, ",, *" + DateTime.Now.Second.ToString(), 1, WdColor.wdColorWhite);
                Globals.ThisAddIn.ajustarTexto(Constantes.tagTituloCopias, " *" + DateTime.Now.Second.ToString(), 1, WdColor.wdColorWhite);
                btnInsertarCopia.Enabled = false;
                btnActualizarCopia.Enabled = false;
                btnEliminarCopia.Enabled = false;
                primerCopiaAgregado = false;
            }
            Globals.ThisAddIn.Application.ScreenUpdating = true;
        }


        /// <summary>
        /// Metodo para activar/desactivar Carta Normal - Evento activado por el check "Normal" 
        /// </summary>
        private void rbtNormal_CheckedChanged(object sender, EventArgs e)
        {
            Globals.ThisAddIn.Application.ScreenUpdating = false;
            if (rbtNormal.Checked)
            {
                ServicesCarta.editarMarcadorConfidencial(rbtNormal.Checked, rbtConfidencial.Checked, chbIngles.Checked);
                Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadTipo, "Normal");
                ServicesCarta.actualizarPropiedadPcrFirmante(rbtNormal.Checked, false, vistaUsuarios);
                ServicesCarta.manipularMarcaAgua(Globals.ThisAddIn.iprimerMarcaAgregada, rbtNormal.Checked, rbtConfidencial.Checked, chbIngles.Checked);
            }
            Globals.ThisAddIn.Application.ScreenUpdating = true;
        }

        /// <summary>
        /// Metodo para activar/desactivar Carta Confidencial - Evento activado por el check "Confidencial" 
        /// </summary>
        private void rbtConfidencial_CheckedChanged(object sender, EventArgs e)
        {
            try
            {
                Globals.ThisAddIn.Application.ScreenUpdating = false;
                if (rbtConfidencial.Checked)
                {
                    ServicesCarta.editarMarcadorConfidencial(rbtNormal.Checked,rbtConfidencial.Checked,chbIngles.Checked);
                    ServicesCarta.manipularMarcaAgua(Globals.ThisAddIn.iprimerMarcaAgregada, rbtNormal.Checked, rbtConfidencial.Checked, chbIngles.Checked);
                    Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadTipo, "Confidencial");
                    ServicesCarta.actualizarPropiedadPcrFirmante(false, rbtConfidencial.Checked, vistaUsuarios);
                }
                Globals.ThisAddIn.Application.ScreenUpdating = true;
            }
            catch (Exception error)
            {
                Globals.ThisAddIn.Application.ScreenUpdating = true;
                Globals.ThisAddIn.protegerArchivo();
                Globals.ThisAddIn.escribirLog(error.ToString());
                MessageBox.Show("Error al agregar la tipología confidencial a un documento antiguo, manipule el complemento desde un documento nuevo.");
                this.Close();
            }
        }

        /// <summary>
        /// Metodo para activar/desactivar Carta Personalizada - Evento activado por el check "Correspondencia combinada" 
        /// </summary>
        private void chbPersonalizado_CheckedChanged(object sender, EventArgs e)
        {
            Globals.ThisAddIn.Application.ScreenUpdating = false;
            if (chbPersonalizado.Checked && Globals.ThisAddIn.consultarValorPropiedad(Constantes.propiedadPersonalizada).Equals("False"))
            {
                Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadPersonalizada, "True");
                Globals.ThisAddIn.eliminarTags(0, Constantes.tagTitulo, Globals.ThisAddIn.listaDestinosCa.Count, Constantes.tagCiudadDestino);
                Globals.ThisAddIn.insertTextPersonalizado();
                Globals.ThisAddIn.insertTagDestinoCarta(true, Constantes.tagTitulo, 0);
                Globals.ThisAddIn.changeTags(0);
                btnActualizarPara.Enabled = false;
                btnEliminarPara.Enabled = false;

            }
            else if (!chbPersonalizado.Checked && Globals.ThisAddIn.consultarValorPropiedad(Constantes.propiedadPersonalizada).Equals("True"))
            {
                Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadPersonalizada, "False");
                Globals.ThisAddIn.eliminarTags(0, Constantes.tagTitulo, 0, Constantes.tagCiudadDestino);
                Globals.ThisAddIn.insertTagDestinoCarta(false, Constantes.tagTitulo, 0);
                Globals.ThisAddIn.eliminarTablaPersonalizada();
                primerDestAgregado = false;
            }

            if(Globals.ThisAddIn.listaDestinosCa.Count == Constantes.maxDestinatariosCarta)
            {
                btInsertarPara.Enabled = false;
            }
            else
            {
                btInsertarPara.Enabled = true;
            }

            if (!Globals.ThisAddIn.listaDestinosCa.Any())
                btnEliminarPara.Enabled = false;

            Globals.ThisAddIn.Application.ScreenUpdating = true;
        }

        /// <summary>
        /// Metodo para activar/desactivar logo - Evento activado por el check "Logo" 
        /// </summary>
        private void chbLogo_CheckedChanged(object sender, EventArgs e)
        {
            if (chbLogo.Checked)
            {
                Globals.ThisAddIn.insertarLogo();
                Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadLogo, "True");
                ServicesCarta.deshabilitarPropiedadImpresion(chbImpresionArea, chbAFisico, chbLogo);
            }
            else
            {
                Globals.ThisAddIn.Application.ScreenUpdating = false;
                Globals.ThisAddIn.iprimerMarcaAgregada = false;
                Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorLogoPagPpal, " ");
                Globals.ThisAddIn.EditarTextoMarcador(Constantes.MarcadorLogoOtrasPag, " ");
                Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadLogo, "False");
                Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadImpresionArea, "True");
                Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadPrimerMarcaAgua, "False");
                ServicesCarta.manipularMarcaAgua(Globals.ThisAddIn.iprimerMarcaAgregada, rbtNormal.Checked,rbtConfidencial.Checked,chbIngles.Checked);
                Globals.ThisAddIn.Application.ScreenUpdating = true;
            }
        }


        /// <summary>
        /// Metodo para activar/desactivar Carta Ingles - Evento activado por el check "Ingles" 
        /// </summary>
        private void chbIngles_CheckedChanged(object sender, EventArgs e)
        {
            Globals.ThisAddIn.Application.ScreenUpdating = false;

            string sNombreFirmante = Globals.ThisAddIn.consultarPlaceholderEtiqueta("00" + Constantes.tagNombreFirmante);
            string sCargoFirmante = Globals.ThisAddIn.consultarPlaceholderEtiqueta("00" + Constantes.tagCargoFirmante);
            string sNombreFirmante2 = Globals.ThisAddIn.consultarPlaceholderEtiqueta("01" + Constantes.tagNombreFirmante);
            string sCargoFirmante2 = Globals.ThisAddIn.consultarPlaceholderEtiqueta("01" + Constantes.tagCargoFirmante);
            string idFirmante;
            bool isInsert;
            vistaUsuarios.RowFilter = "";

            ServicesCarta.editarMarcadorConfidencial(rbtNormal.Checked, rbtConfidencial.Checked, chbIngles.Checked);
            ServicesCarta.editarMarcadorAnexos(chbAElectronico, chbIngles, chbAFisico);
            ServicesCarta.manipularMarcaAgua(Globals.ThisAddIn.iprimerMarcaAgregada, rbtNormal.Checked, rbtConfidencial.Checked, chbIngles.Checked);

            if (chbIngles.Checked)
            {
                if (!primerRemitenteAgregado)
                {
                    Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorAsunto, "Subject:");
                    Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorDireccionPagPpal, "Address:");
                    Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorDireccionlOtrasPag, "Address:");
                    Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorTelefononPagPpal, "Phone:");
                    Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorTelefonolOtrasPag, "Phone:");
                    Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadIdiomaIngles, "True");
                }
                else
                {
                    Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorAsunto, "Subject:");
                    Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorDireccionPagPpal, "");
                    Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorDireccionlOtrasPag, "");
                    Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorTelefononPagPpal, "Phone:");
                    Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorTelefonolOtrasPag, "Phone:");
                    Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadIdiomaIngles, "True");
                }
                if (chbCopias.Checked)
                {
                    Globals.ThisAddIn.ajustarTexto(Constantes.tagTituloCopias, "Copies: *" + DateTime.Now.Second.ToString(), 12, WdColor.wdColorBlack);
                }                    

                //Colocar para segundo Firmante
            }
            else
            {
                if (!primerRemitenteAgregado)
                {
                    Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorAsunto, "Asunto:");
                    Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorDireccionPagPpal, "Dirección:");
                    Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorDireccionlOtrasPag, "Dirección:");
                    Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorTelefononPagPpal, "Teléfono:");
                    Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorTelefonolOtrasPag, "Teléfono:");
                    Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadIdiomaIngles, "False");
                }
                else
                {
                    Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorAsunto, "Asunto:");
                    Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorDireccionPagPpal, "");
                    Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorDireccionlOtrasPag, "");
                    Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorTelefononPagPpal, "Tel.:");
                    Globals.ThisAddIn.EditarTextoMarcador(Constantes.marcadorTelefonolOtrasPag, "Tel.:");
                    Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadIdiomaIngles, "False");
                }

                if (chbCopias.Checked)
                {
                    Globals.ThisAddIn.ajustarTexto(Constantes.tagTituloCopias, "Copias: *" + DateTime.Now.Second.ToString(), 12, WdColor.wdColorBlack);
                }
            }

            idFirmante = Globals.ThisAddIn.consultarValorPropiedad("00" + Constantes.propiedadIdFirmante);

            if (!idFirmante.Equals("00" + Constantes.propiedadIdFirmante)){

                isInsert = ServicesCarta.cambiarTagsFirmante(chbIngles.Checked, 0, idFirmante, sNombreFirmante, sCargoFirmante);

                if (!isInsert)
                {
                    if (chbIngles.Checked)
                        chbIngles.Checked = false;
                    Globals.ThisAddIn.Application.ScreenUpdating = true;
                    return;
                }
            }

            Globals.ThisAddIn.Application.ScreenUpdating = true;
        }


        /// <summary>
        /// Metodo para activar/desactivar Anexos Físicos - Evento activado por el check "Anexos Físicos" 
        /// </summary>
        private void chbAFisico_CheckedChanged(object sender, EventArgs e)
        {
            if (chbAFisico.Checked)
            {
                Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadAnexosFisicos, "True");
                Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadImpresionArea, "True");
                ServicesCarta.editarMarcadorAnexos(chbAElectronico, chbIngles, chbAFisico);
            }
            else
            {
                Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadAnexosFisicos, "False");
                ServicesCarta.editarMarcadorAnexos(chbAElectronico, chbIngles, chbAFisico);
                ServicesCarta.deshabilitarPropiedadImpresion(chbImpresionArea, chbAFisico, chbLogo);
            }

        }

        /// <summary>
        /// Metodo para activar/desactivar Anexos Electronicos - Evento activado por el check "Anexos Electrónicos" 
        /// </summary>
        private void chbAElectronico_CheckedChanged(object sender, EventArgs e)
        {
            if (chbAElectronico.Checked)
            {
                Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadAnexosElectronicos, "True");
                ServicesCarta.editarMarcadorAnexos(chbAElectronico, chbIngles, chbAFisico);
            }
            else
            {
                Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadAnexosElectronicos, "False");
                ServicesCarta.editarMarcadorAnexos(chbAElectronico, chbIngles, chbAFisico);
            }
        }



        /// <summary>
        /// Metodo para activar/desactivar Referencia - Evento activado por el check "Referencia" 
        /// </summary>
        private void chbReferencias_CheckedChanged(object sender, EventArgs e)
        {
            if (chbReferencias.Checked)
            {
                Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadRef, "True"); //Vamaya: se cambia valor de la propiedad añadida
                Globals.ThisAddIn.ajustarTexto(String.Concat(Constantes.tagTituloReferencia), String.Concat(" ", "*"), 12, WdColor.wdColorBlack);
                Globals.ThisAddIn.ajustarTexto(String.Concat(Constantes.tagReferencia), String.Concat("Incluir referencia.", "*"), 12, WdColor.wdColorBlack);
            }
            else
            {
                Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadRef, "False"); //Vamaya: se cambia valor de la propiedad añadida
                Globals.ThisAddIn.ajustarTexto(String.Concat(Constantes.tagTituloReferencia), String.Concat(" ", "*"), 1, WdColor.wdColorWhite);
                Globals.ThisAddIn.ajustarTexto(String.Concat(Constantes.tagReferencia), String.Concat(" ", "*"), 1, WdColor.wdColorWhite);
            }
        }

        /// <summary>
        /// Metodo para activar/desactivar Impresión en el area - Evento activado por el check "impresión en el Área" 
        /// </summary>
        private void chbImpresionArea_CheckedChanged(object sender, EventArgs e)
        {
            if (chbImpresionArea.Checked)
            {
                Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadImpresionArea, "True");
            }
            else
            {
                ServicesCarta.deshabilitarPropiedadImpresion(chbImpresionArea, chbAFisico, chbLogo);
            }
        }


        /// <summary>
        /// Metodo para activar/desactivar Correo Certificado - Evento activado por el check "Certificado" 
        /// </summary>
        private void rbtCorreoCertificado_CheckedChanged(object sender, EventArgs e)
        {
            if (rbtCorreoCertificado.Checked)
            {
                if (ServicesCarta.validarDireccionesInsertadas())
                {
                    Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadCorreoCertificado, "True");
                    MessageBox.Show("Recuerde que el envío por correo electrónico certificado tiene costo.");
                }
                else
                {
                    MessageBox.Show("Opción deshabilitada, debido a que no ha ingresado ninguna dirección de correo electrónico en los destinos.");
                    rbtCorreoNormal.Checked = true;
                }
            }
        }


        /// <summary>
        /// Metodo para activar/desactivar Correo Normal - Evento activado por el check "Normal" 
        /// </summary>
        private void rbtCorreoNormal_CheckedChanged(object sender, EventArgs e)
        {
            if (rbtCorreoNormal.Checked)
            {
                Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadCorreoCertificado, "False");
            }
        }


        /// <summary>
        /// Metodo para cambiar las ciudades dependiendo el País seleccionado
        /// </summary>
        private void cbPais_SelectedIndexChanged(object sender, EventArgs e)
        {
            int iIdPais = cbPais.SelectedIndex + 1;
            Globals.ThisAddIn.cargarCiudades(iIdPais.ToString());
            ServicesCarta.llenarComboCiudades(cbCiudad, Globals.ThisAddIn.listViewCiudades);
        }


        private void btnVerificarDatos_Click(object sender, EventArgs e)
        {
            DatosDestinosForm frmAbout = new DatosDestinosForm();
            frmAbout.ShowDialog();
        }

        private void rbtDirecto_CheckedChanged(object sender, EventArgs e)
        {
            if (rbtDirecto.Checked)
            {
                Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadMedioEnvio, "Directo");
                chbImpresionArea.Checked = false;
                chbImpresionArea.Enabled = false;
            }
        }

        private void rbtMensajeria_CheckedChanged(object sender, EventArgs e)
        {
            if (rbtMensajeria.Checked)
            {
                Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadMedioEnvio, "Mensajeria");
                chbImpresionArea.Enabled = true;
            }
        }

        private void txtCopiaCorreo_Leave(object sender, EventArgs e)
        {
            string sCorreoCopia = Globals.ThisAddIn.consultarValorPropiedad(Constantes.propiedadCorreoCopia);

            if (ServicesCarta.validarDireccionesInsertadas())
            {
                if (ServicesCarta.ComprobarFormatoEmail(txtCopiaCorreo.Text) == false)
                {
                    txtCopiaCorreo.Text = "";
                    if (!sCorreoCopia.Equals(Constantes.propiedadCorreoCopia))
                    {
                        Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadCorreoCopia, Constantes.propiedadCorreoCopia);
                    }
                    MessageBox.Show("Favor introducir una dirección de correo válida.");
                    return;
                }
                Globals.ThisAddIn.insertarValorPropiedad(Constantes.propiedadCorreoCopia, txtCopiaCorreo.Text);
                MessageBox.Show("Correo buzón actualizado.");
            }
            else
            {
                MessageBox.Show("Opción deshabilitada, debido a que no ha ingresado ninguna dirección de correo electrónico en los destinos.");
                txtCopiaCorreo.Text = "";
            }

        }

        private void dgvUsuarios_RowEnter(object sender, DataGridViewCellEventArgs e)
        {
            txtNombreexterno.Text = "";
            txtCargoExterno.Text = "";
            txtEmpresa.Text = "";
            TxtDireccion.Text = "";

        }

        private void txtNombreexterno_Click(object sender, EventArgs e)
        {
            txtNombreexterno.Enabled = true;
            txtCargoExterno.Enabled = true;
            txtEmpresa.Enabled = true;
            TxtDireccion.Enabled = true;
            TxtDireccion.Text = "";
            dgvUsuarios.ClearSelection();
        }

        private void lblNombreExterno_Click(object sender, EventArgs e)
        {
            txtNombreexterno.Enabled = true;
            txtCargoExterno.Enabled = true;
            txtEmpresa.Enabled = true;
            TxtDireccion.Enabled = true;
            TxtDireccion.Text = "";
            dgvUsuarios.ClearSelection();
        }

        private void lblCargoExterno_Click(object sender, EventArgs e)
        {
            txtNombreexterno.Enabled = true;
            txtCargoExterno.Enabled = true;
            txtEmpresa.Enabled = true;
            TxtDireccion.Enabled = true;
            TxtDireccion.Text = "";
            dgvUsuarios.ClearSelection();
        }

        private void lblEntidad_Click(object sender, EventArgs e)
        {
            txtNombreexterno.Enabled = true;
            txtCargoExterno.Enabled = true;
            txtEmpresa.Enabled = true;
            TxtDireccion.Enabled = true;
            TxtDireccion.Text = "";
            dgvUsuarios.ClearSelection();
        }

        private void btnLimpiar_Click(object sender, EventArgs e)
        {
            txtNombre.Text = "";
            txtCargo.Text = "";
            txtDpto.Text = "";
        }

        private void txtNombre_TextChanged_1(object sender, EventArgs e)
        {
            Globals.ThisAddIn.buscarUsuarios(txtNombre.Text, txtCargo.Text, txtDpto.Text, vistaUsuarios);
        }

        private void txtCargo_TextChanged_1(object sender, EventArgs e)
        {
            Globals.ThisAddIn.buscarUsuarios(txtNombre.Text, txtCargo.Text, txtDpto.Text, vistaUsuarios);
        }

        private void txtDpto_TextChanged_1(object sender, EventArgs e)
        {
            Globals.ThisAddIn.buscarUsuarios(txtNombre.Text, txtCargo.Text, txtDpto.Text, vistaUsuarios);
        }

        private void SearchFormCarta_FormClosing(object sender, FormClosingEventArgs e)
        {
            Globals.ThisAddIn.iContVisitasForms--;
            Globals.ThisAddIn.Application.ScreenUpdating = true;
        }

        private void txtCargoExterno_TextChanged(object sender, EventArgs e)
        {

        }

        /// <summary>
        /// Verifica la versión del AddIn, comparando la versión local, con la especificada en el
        /// archivo plconf
        /// Void
        /// </summary>
        public void verificarAddIn()
        {
            if (Globals.ThisAddIn.ActAddin.Equals(Globals.ThisAddIn.VersThisAddin))
            {
                lblVersionAddIn.ForeColor = System.Drawing.Color.Green;
                lblVersionAddIn.Text = lblVersionAddIn.Text + Globals.ThisAddIn.VersThisAddin + " (Actualizado)";
            }
            else
            {
                lblVersionAddIn.ForeColor = System.Drawing.Color.Red;
                lblVersionAddIn.Text = lblVersionAddIn.Text + Globals.ThisAddIn.VersThisAddin + " (DESACTUALIZADO)";
            }
        }


        /// <summary>
        /// Verifica la versión local de la plantilla de carta, comparándola con la especificada en el archivo plconf
        /// </summary>
        public void verificarPlantilla()
        {
            if (Globals.ThisAddIn.consultarValorPropiedad(Constantes.propiedadVersionPlantilla).Equals(Globals.ThisAddIn.ActPlanCA))
            {
                lblVersionPlantillaCA.ForeColor = System.Drawing.Color.Green;
                lblVersionPlantillaCA.Text = lblVersionPlantillaCA.Text + Globals.ThisAddIn.consultarValorPropiedad(Constantes.propiedadVersionPlantilla) + " (Actualizada)";
            }
            else
            {
                lblVersionPlantillaCA.ForeColor = System.Drawing.Color.Red;
                lblVersionPlantillaCA.Text = lblVersionPlantillaCA.Text + Globals.ThisAddIn.consultarValorPropiedad(Constantes.propiedadVersionPlantilla) + " (DESACTUALIZADA)";
            }
        }


        public void verificarAmbienteTrabajo()
        {
            Globals.ThisAddIn.verificarAmbiente();
        }

        private void groupBox4_Enter(object sender, EventArgs e)
        {

        }

        private void gbxTipo_Enter(object sender, EventArgs e)
        {

        }

        private void txtNombreexterno_TextChanged(object sender, EventArgs e)
        {
            dgvUsuarios.ClearSelection();
        }

        private void txtEmpresa_TextChanged(object sender, EventArgs e)
        {
            dgvUsuarios.ClearSelection();
        }

        //JLG: Se generan los listeners cuando los radiobutton de direccion física y electrónica cambian
        private void rbtFisica_CheckedChanged(object sender, EventArgs e)
        {
            if (rbtFisica.Checked)
            {
                //cbPais.Enabled = true;
                //cbCiudad.Enabled = true;
                cbPais.SelectedValue = "Colombia";
                cbCiudad.SelectedValue = "Bogotá D.C. ";
            }
        }

        private void rbtCorreo_CheckedChanged(object sender, EventArgs e)
        {
            if(rbtCorreo.Checked)
            {
                //cbPais.Enabled = false;
                //cbCiudad.Enabled = false;
                //cbPais.SelectedValue = "";
                cbCiudad.SelectedValue = "";
            }
        }
        //Fin JGL.
    }

}